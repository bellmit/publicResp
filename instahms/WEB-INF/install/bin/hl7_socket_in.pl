#!/usr/bin/perl -w

#
# Receive messages from an i-STAT device.
#

use strict;
use IO::Socket;
use Sys::Hostname;
use File::Basename;
use Getopt::Long;

use lib dirname($0)."/../lib";
use Hl7::Message;
use Hl7::Segment;
use Insta::RadImport;
use Insta::ServiceImport;
use Insta::DiagImport;
use Insta::Util;
use Insta::Logger;
use REST::Client;
use JSON;

our $opt_db = 'hms';
our $opt_listen = 5900;
our $opt_fork = 1;
our $opt_foreground = 0;
our $opt_schema;
our $opt_host = '127.0.0.1';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_port = 5432;
our $opt_apihost='localhost';
our $opt_apiport='';
our $opt_apiname='';
our $opt_apiuser='';
our $opt_apipwd='';
my $restClient = REST::Client->new();
use URI::Escape;

my $client;
my $requests = 1;
my $pidfile = "/var/run/hl7_socket_in.pid";

sub usage {
	return "Usage: $0 [OPTIONS]\n" .
	" OPTIONS: \n" .
	"  -l|--listen <listening port>: (5900)\n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -f|--fork : whether to fork a process for every request (fork)\n" .
	"  -n|--foreground: run in foreground, don't daemonize (noforeground)\n" .
	"  -o|--port <port> : port not to connect to database\n".
	"  -U|--username <username> : Database user name \n".
	"  -W|--password <password> : Database password \n";
}


GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", 'password|W=s', "foreground|n!","listen=i","fork!", "debug|g!",
	"apiuser=s", "apipwd=s", "apihost=s", "apiport=s", "apiname=s") or die usage();


unless ($opt_schema) {
	$opt_schema = hostname;
	$opt_schema =~ s/instahms-//;
}

my $apiurl = '';
if ($opt_apiport) {
	$apiurl = "http://$opt_apihost:$opt_apiport/$opt_apiname";
} else {
	$apiurl = "http://$opt_apihost/$opt_apiname";
}

my %apiCredMap = ();
$apiCredMap{'apiUrl'} = $apiurl;
$apiCredMap{'schema'} = $opt_schema;
$apiCredMap{'apiUser'} = $opt_apiuser;
$apiCredMap{'apiPwd'} = $opt_apipwd;


my $util = Insta::Util->new();
my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=>basename($0),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});
$log->rotateLog(10*1024*1024);

$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");


# these will be read from from the db
my $userId;

#
# Read the config from the database: exit if we are not successful, before
# daemonizing itself.
#
eval {
	readConfig();
};
if ($@) {
	$log->error("Reading config: ", $@);
	exit 1;
}

#
# Daemonize if required, but first check if we are already running. This program
# can therefore be called as a cron-job to install itself if not already running
# as well as restart itself if it has crashed.
#
unless ($opt_foreground) {

	my $exists = open(PIDCHECK, $pidfile);
	if ($exists) {
		my $pidcheck = <PIDCHECK>;
		chomp $pidcheck;
		close PIDCHECK;
		if (-e "/proc/$pidcheck") {
			$log->error("Already running: $pidcheck");
			exit 0;
		} else {
			$log->warn("PID file exists but no process: $pidcheck");
		}
	}

	if (my $pid = fork()) {
		# parent process, write the child pid and exit
		open PIDFILE, "> $pidfile" or die "Unable to open PID File: $pidfile: ", $!, "\n";
		print PIDFILE $pid, "\n";
		close PIDFILE;
		exit 0;
	}

	chdir "/";
}

# set up the server
my $server = IO::Socket::INET->new(
	Proto => 'tcp',
	LocalPort => $opt_listen,
	LocalAddr => "0.0.0.0",
	Listen    => SOMAXCONN,
	Reuse     => 1);

die "Can't setup server: $!" unless $server;

$log->info("[Server started on $opt_listen] ");

$SIG{CHLD} = 'IGNORE';
$SIG{INT} = \&interrupt;
$SIG{TERM} = \&interrupt;

#
# Instantiate the diag import processor
#

my $serviceProcessor = Insta::ServiceImport->new( {
		db=>$opt_db, schema=>$opt_schema, db_port=>$opt_port,db_username=>$opt_username,db_password=>$opt_password,
		userId=>$userId, host=>$opt_host
});

my $diagProcessor = Insta::DiagImport->new( {
		db=>$opt_db, schema=>$opt_schema, db_port=>$opt_port,db_username=>$opt_username,db_password=>$opt_password,
		userId=>$userId, host=>$opt_host
});

my $gAckControlId = time;
my $interfaces;
#
# Star the Main loop
#
while ($client = $server->accept()) {
	$log->info("Connected to the client :");
    # fork a child to respond to this request
    if ($opt_fork) {
        unless (fork()) {
            receive($client);
            exit;
        }
    } else {
        receive($client);
    }
	$requests++;
}

# ###############
#    Subs
# ###############

#
# receive and process one connection
#

sub loginReq {
##################################################
	# RC : Login request should not be hard coded  
	my $login = "$apiurl/Customer/Login.do";
	my $loginQueryStr = "_method=login&hospital_name=$opt_schema&customer_user_id=$opt_apiuser&customer_user_password=$opt_apipwd";
	my $url = $login.'?'.$loginQueryStr;
	# $log->info("Login Request : $url");
	$restClient->GET($url);
	return $restClient->responseContent();
}

##################################################
sub statusChangeRequest {
##################################################
	my $queryStr = "$apiurl/api/message/inbound.json";
	my $isrQueryStr = undef;
	$isrQueryStr = shift;
	my $query = $queryStr."?".$isrQueryStr;
	$log->debug("Status Change URL : ". $queryStr);	
	$log->info("Sending request to : api/message/inbound.json");
	$restClient->GET($query);
    return $restClient->responseContent();
}

sub receive {
    my $requestor = shift;
    $requestor->autoflush(1);
    printf("Accepting req ${requests}: [from %s:%s]\n",
		$requestor->peerhost, $requestor->peerport);

    local $/ = "\x1C\x0D";	# file separator, carriage return
    while (my $received = <$requestor>) {
		my $clean = $received;

		my $client_host =  $requestor->peerhost();
		my $client_port = $requestor->peerport();
		$log->info("Client host : $client_host, client_port : $client_port");
		$clean =~ tr/\x0B\x0D\x1C/\n\n./;
		chomp $clean;
		chomp $received;

		$log->info("req $requests data: ", $clean);

		#
		# parse the message
		#
		my $msg;
		eval { $msg = Hl7::Message->new($received); };

		my $msgString = $msg->toString(1);
		$log->info($msgString);
	
		my $msgCtrlId = $msg->{MSH}{controlId};
		my $recFacility = 'PACS';
		if (defined($msg->{MSH}{recvFac})) {
			$recFacility = $msg->{MSH}{recvFac};
		}
		$log->info("Interface ack trim_ack_type  = $interfaces->{$recFacility}->{'trim_ack_type'}");
		if ($@) {
			# message couldn't be parsed at all: send CE (Commit Error) ack
			
			$log->error("Unparsable message");
			$log->error($@);

			if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'CA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
				sendCommitAck($requestor, 'CE', 'Unparseable message', $msgCtrlId);
			}
			next;
		}

		my $msgType = $msg->{MSH}{msgType};

		$log->info(" mesgType->[0]  : $msgType->[0] ,msgType->[1] : $msgType->[1] ");
 
		if(($msgType->[0] ne 'ORU' || ($msgType->[1] ne 'R01' && $msgType->[1] ne 'R03'))  ) {
			my $supportedStatus = '1';
			if ($msgType->[0] eq 'ORM') {
				if (defined($interfaces->{$recFacility}->{exclude_orm_status}) && $interfaces->{$recFacility}->{exclude_orm_status} ne '') {
					my @statusSplit = split(',', $interfaces->{$recFacility}->{exclude_orm_status});
					foreach my $status (@statusSplit) {
						if ($status eq $msg->{ORC}{ctrl}) {
							$supportedStatus = '0';
							last;
						}
					}
				}

				if ($supportedStatus) {
					my $encodedString = uri_escape($msgString);
					my @formField = ();
					my $res_l =  decode_json(loginReq());
			        if(!defined $res_l->{request_handler_key}) {
			                $log->error("Please enter correct username and password for API login");
			                exit 1;
			        }
			        $log->info("Login Response : $res_l->{return_message}");
			        $log->debug("Login response : " . encode_json($res_l));
			        ################# API LOGIN SUCCESSFUL ###############
			        push(@formField, "request_handler_key=$res_l->{request_handler_key}");
			        push(@formField, "inboundMessage=$encodedString");
			        push(@formField, "parserType=hl7");
			        my $str = join("&",@formField);
					
			        my $res = decode_json(statusChangeRequest($str));
			        my $errorTxt = '';
			        if (!$res->{success}) {
			        	if (defined($res->{error})) {
			        		$errorTxt = "Message parsing failed";
			        		$log->info($errorTxt);
							$log->info("Will send CE(commit error) code if CA ack_type is enabled else No ack for failure");
			        	} else {
			        		$errorTxt = "Status updation failed";
			        		$log->info($errorTxt);
			        	}
			        	if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'CA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
							sendCommitAck($requestor, 'CE', $errorTxt.' '.$msgType->[0].'^'.$msgType->[1],
									$msgCtrlId);
						}
						next;
			        } else {
						$log->info("Status updated successfully");
						if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'CA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
							$log->info("Sending Message accepted CA(Commit accept)  ACK ....");	
							sendCommitAck($requestor, 'CA', '', $msgCtrlId);
						}
						next;  
			        }
				}
			}
			# uknown type of message: send CE (commit error) ack
			$log->info("Unknown type message : sending CE");
			
			$log->info("Message parsing failed");
			$log->info("Will send CE(commit error) code if CA ack_type is enabled else No ack for failure");

			if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'CA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
				sendCommitAck($requestor, 'CE', 'Unknown message type: '.$msgType->[0].'^'.$msgType->[1],
						$msgCtrlId);
			}
			next;
		}

		if (defined($interfaces->{$recFacility}->{exclude_oru_status}) && $interfaces->{$recFacility}->{exclude_oru_status} ne '') {
			my @excludeOruStatuses = split(',', $interfaces->{$recFacility}->{exclude_oru_status});
			my $isExcludedMsg = 0;
			foreach my $status (@excludeOruStatuses) {
				if ($status eq $msg->{ORC}{ctrl}) {
					$isExcludedMsg = 1;
					$log->info("Unknown type message : sending CE");
			
					$log->info("Message parsing failed");
					$log->info("Will send CE(commit error) code if CA ack_type is enabled else No ack for failure");

					if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'CA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
						sendCommitAck($requestor, 'CE', 'Unknown message type: '.$msgType->[0].'^'.$msgType->[1],
								$msgCtrlId);
					}
					last;
				}
			}
			if ($isExcludedMsg) {
				next;
			}
		}

		$log->info("Message  parsed successfully");
		$log->info("Will send  CA(commit accept) code if ack_type CA is enabled");	
		# all is well: send CA (commit accept) message
		if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'CA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
		$log->info("Sending Message accepted CA(Commit accept)  ACK ....");	
			sendCommitAck($requestor, 'CA', '', $msgCtrlId);
		}

		my $errMsg;
		my $conductionFormat =  $diagProcessor->getPlacerOrderNum($msg, 1) || '';

		eval {
			if((ref($msg->{MSH}{recvApp})  eq 'ARRAY') and ($msg->{MSH}{recvApp}->[1] eq 'SERVICE') || $conductionFormat eq 'SERV') {
				$log->info("Processing parsed message Service...");
				$errMsg = $serviceProcessor->processMessage($interfaces->{$recFacility}, $msg);
			} elsif($msg->{MSH}{recvFac} eq 'VITALS') { 
					print "\n********************************\n";
					print "\nRecvFacility : $msg->{MSH}{recvFac}\n";
					print "\n********************************\n";
					my @formField = ();
					my $res_l =  decode_json(loginReq());
			        if(!defined $res_l->{request_handler_key}) {
			                $log->error("Please enter correct username and password for API login");
			                exit 1;
			        }
			        $log->info("Login Response : $res_l->{return_message}");
			        $log->debug("Login response : " . encode_json($res_l));
			        ################# API LOGIN SUCCESSFUL ###############
			        my $encodedString = uri_escape($msgString);
			        push(@formField, "request_handler_key=$res_l->{request_handler_key}");
			        push(@formField, "inboundMessage=$encodedString");
			        push(@formField, "parserType=hl7");
			        my $str = join("&",@formField);
			        my $res = decode_json(statusChangeRequest($str));
			
			} else {
				$log->info("Processing parsed message Diag...");
				$errMsg = $diagProcessor->processMessage($interfaces->{$recFacility}, $msg, \%apiCredMap);
			}
		};
		
		if ($@) {
			$log->error(" Failed to process for the following reason: $@");
			$errMsg = 'Failed to process message';
		}
		
		$log->info("Message processed trying to send [AA(AE/AA)] ACK ...");
		$log->info(" AA(Application Accepted), AE(Application Error)");
		#
		# Send application level message with success/failure of processing
		#
		my $aackSocket = undef;
		
		if($interfaces->{$recFacility}->{'trim_ack_type'} eq 'AA') {
			$aackSocket = $requestor;
		}
		
		if($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B') {
			$aackSocket = IO::Socket::INET->new (
							Proto    => "tcp",
							PeerAddr => $requestor->peerhost,
							PeerPort => 5999,
							timeout  => 10
						);
		}
		if(($interfaces->{$recFacility}->{'trim_ack_type'} eq 'AA') or ($interfaces->{$recFacility}->{'trim_ack_type'} eq 'B')) {
			if ($errMsg) {
				my $errCondition = ($errMsg =~ /Invalid MR No/) ? '1010' : '9999';
				sendAppAck($aackSocket, $msgCtrlId, 'AE', $errCondition, $errMsg, $interfaces->{$recFacility}->{'close_client_con'});
			} else {
				sendAppAck($aackSocket, $msgCtrlId, 'AA', 0, ['',$errMsg], $interfaces->{$recFacility}->{'close_client_con'});
			}
		}
	}

    close $requestor;
    $log->info($requests, ":", " Client closed connection");
}

sub getConnection {
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});

	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

#
# Read config: the userId for updating stuff
#
sub readConfig {
	my $dbh = getConnection();

	my $sql = "SELECT hli.*,trim(hli.ack_type) as trim_ack_type, upper(hli.sending_facility) as map_key 
		FROM hl7_lab_interfaces hli
		JOIN hl7_center_interfaces hci USING (hl7_lab_interface_id)
		WHERE hli.status='A' AND hci.export_type = 'S'";
	 
        $interfaces = $dbh->selectall_hashref($sql,'map_key');
        my $hl7Prefs = $dbh->selectrow_hashref("SELECT * from hosp_hl7_prefs");
	$userId = 'auto_update';
	$dbh->disconnect();
}

#
# Send commit level ack: this is sent on the same connection
#
sub sendCommitAck {

	my ($conn, $code, $msgText, $msgControlId) = @_;

	$log->info("Sending ack_type CA , msg_code = $code and msg_txt =  $msgText" );

	my $ack = new Hl7::Message({sendApp=>'Insta', sendFac=>'Diag', recvApp=>'MWL', 	recvFac=>'MWL',
			msgType=>'ACK', procId=>'T', ackType=>'NE', appAckType=>'NE', controlId=>$gAckControlId++});

	# msgControlId is the control id of the message that we received.
	$ack->addSegment(new Hl7::Segment('MSA', {code=>$code, msgText=>$msgText, controlId=>$msgControlId++}));

	print $conn "\x0B", $ack->toString(), "\x1C\x0D";
	$conn->flush();
	$log->info("Sent commit ack: ", $ack->toString(1));
}

#
# Send application level ack: initiates a connection to the i-STAT CDS, which
# is listening for the application level acknowledgement.
#
sub sendAppAck {
#	my ($requestor, $appCtrlId, $code, $errCondition, $msgText) = @_;
	my ($conn, $appCtrlId, $code, $errCondition, $msgText, $closeClientCon) = @_;

#	unless ($conn) {
#		print "Unable to connect to host " . $requestor->peerhost . " at port 5999 \n";
#		return;
#	}

	$log->info("Sending ack_type = AA, ack_code = $code, ack_msg = $msgText");

	my $ack = new Hl7::Message({sendApp=>'Insta', sendFac=>'Diag', recvApp=>'MWL', recvFac=>'MWL',
			msgType=>['ACK','R01'], procId=>'T', ackType=>'AL', appAckType=>'NE',
			controlId=>$gAckControlId++});

	$ack->addSegment(new Hl7::Segment('MSA', {code=>$code, controlId=>$appCtrlId,
				msgText=>$msgText, errorCondition=>$errCondition}));

	print "\n=====================================\n";


	print "\nack  : $ack\n";
	print "\n---------\n";
	print $ack->toString();

	print "\n=====================================\n";


	print $conn "\x0B", $ack->toString(), "\x1C\x0D";
	$conn->flush();
	$log->info("Sent app ack:");
	$log->info($ack->toString(1));

	#
	# Read the response: nothing to be done with it, just read and print in log.
	#
	# Some interfaces close the connection from their end once they receive Ack
	if ($closeClientCon eq 'Y') {
		my $resp = <$conn>;
		my $respMsg = Hl7::Message->new($resp);
		$log->info("Read commit ack for app ack:");
		$log->info($respMsg->toString(1));
		$conn->close();
	}
}

sub hexstr {
	my ($str) = @_;
	my $s = sprintf "%vx", $str;
	return $s;
#	print $s;
}
#
# close when user presses ctrl-C, also remove pidfile
#
sub interrupt {
	$log->info("Received signal, exiting ...");
	close $client if defined($client);
	close $server;
	unlink $pidfile;
	exit 0;
};

sub decodeResultText {
        my ($result) = @_;
        $result =~ s/\\X0D\\/<br\/>/;
        $result =~ s/\\X0A\\/<br\/>/;
        $result =~ s/\\S\\/^/;
        $result =~ s/\\T\\/&/;
        $result =~ s/\\R\\/~/;
        $result =~ s/\\H\\/<b>/;
        $result =~ s/\\N\\/<\/b>/;
        $result =~ s/\\F\\/|/;
        
        return $result;
}

END { $log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");}

=comment

\H\  start highlighting  			:  <b>

\N\  normal text (end highlighting) :  </b>

\F\ field separator    				:	|   

\S\ component separator   			:	^      

\T\ subcomponent separator   		:	&

\R\ repetition separator  			:	~

\E\  escape character  				:	\\


 \Xdddd...\   hexadecimal data

\Zdddd...\    locally defined escape sequence

=cut
