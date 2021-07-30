#!/usr/bin/perl -w
 
use strict;
use DBI;
use POSIX qw(strftime);
use File::Basename;
use IO::Socket::INET;
use Getopt::Long;
use Sys::Hostname;
use POSIX ":sys_wait_h";
use IO::Handle;

use lib dirname($0)."/../lib";	# find our Hl7 module. Needed for the next two linesvice
use Hl7::Message;
use Hl7::Segment;
use Insta::DiagDBExporter;
use Insta::Util;
 
our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;

our $opt_dest_db ="";
our $opt_dest_username = "";
our $opt_dest_password = "";
our $opt_dest_schema = "";
 
sub usage {
    return "Usage: $0 [OPTIONS] <schema>\n" .
    " OPTIONS: \n" .
    "  -d|--db <database>: which database to connect to (hms)\n" .
    "  -s|--schema <schema>: which schema to operate on (hostname)\n" .
    "  -h|--host <host> : database host\n".
    "  -U|--username <username> : database username\n".
    "  -W|--password <password> : database password\n".
    "  -o|--port <port_no> : database port no".
    "  -n|--foreground: print to console instead of logfile\n";
}
 
GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", "password|W=s", "foreground|n!", 
		"dest_db|ddb=s", "dest_username|ddU=s", "dest_password|dp=s","dest_schema|ds=s" ) or die usage();
		
$opt_db = 'hms' unless($opt_db);
$opt_host = '127.0.0.1' unless($opt_host);


unless ($opt_schema) {
	$opt_schema = hostname;
	$opt_schema =~ s/instahms-//;
}

$opt_dest_db = $opt_db unless($opt_dest_db) ;
$opt_dest_username = $opt_username unless($opt_dest_username);
$opt_dest_password = $opt_password unless($opt_dest_password);
$opt_dest_schema = $opt_schema unless($opt_dest_schema);


my $logfile = "/var/log/insta/$opt_db/hl7_patient_export.$opt_schema.log";
my $pidfile = "/tmp/hl7_patient_export.$opt_db.$opt_schema.pid";
 
 my $interfaces;
 rotateLog(10*1024*1024);

 eval {
	readConfig();
};
if ($@) {
	print "ERROR reading config: ", $@, "\n";
	exit 1;
}
my $nowString = localtime;
print "\n", $nowString, ": === Starting to look for orders ===\n "; 
if (keys(%$interfaces) < 1) {
	print "Either interface not configured OR There is no orders to export.\n";
}
# iterate through the interfaces
foreach my $ifName (keys %$interfaces) {
eval {
		my $if = $interfaces->{$ifName};
		print "Getting Patient Demographic changes for integration_name : $if->{integration_name} \n";
		my $patientDemoorders = getInterfacepatientOrders($if->{integration_id});
		my $exporter = getExporter('D');
		my $handle = $exporter->open($if->{host}, $if->{port});
		my $targetpatient = $exporter->initpatientdetails($handle);
		
		# inserting patient details
			  foreach my $patientorder (@$patientDemoorders) {
			  print "\n******** POSTING TO hl7_patient_details TABLE************\n";	
			  print "Processing order details for mr_no : ", $patientorder->{mr_no},"\n";
			  
			  		$exporter->exportpatientdetails($targetpatient, $patientorder);		  	  
			  			
			  updateExportedSuccess($patientorder->{mr_no});	
			  print "\n*********** END_POSTING **************\n";
			  }
		$exporter->close($handle);
	};
	if ($@) {
		print "ERROR while opening connection:\n", $@, "\n";
	}
	
}
 
sub readConfig {
    my $dbh = getConnection();
    print "Reading Hl7 configuration \n";
    # get all interfaces configured
    $interfaces = $dbh->selectall_hashref(
                "SELECT ii.integration_id,ii.host,ii.port, ii.integration_name
                    FROM hl7_export_patient hep
                    JOIN insta_integration ii ON (ii.integration_id = hep.integration_id)
                    WHERE hep.export_status IN ('N', 'F')
                    GROUP BY ii.integration_id",
                'integration_id');
    $dbh->disconnect();
}
sub getConnection {
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $opt_schema");
	return $dbh;
}

sub getExporter {
	my ($exportType) = @_;
	if ($exportType eq 'D') {
		my $dbExporter = Insta::DiagDBExporter->new( {
			db=>$opt_dest_db, schema=>$opt_dest_schema, db_username=>$opt_dest_username,db_password=>$opt_dest_password
		});
		return $dbExporter;
	}
}

sub getInterfacepatientOrders {
	my ($ifName) = @_;
	my $dbh = getConnection();
	my $patientDemoorders = $dbh->selectall_arrayref(qq{
	SELECT   ep.item_id as mr_no, pd.oldmrno, pd.patient_name,pd.middle_name, pd.last_name, sm.salutation, pd.patient_gender,
				to_char(coalesce(pd.dateofbirth, pd.expected_dob), 'YYYYMMDD')
				as expected_dob, pd.patient_address, ci.city_name as cityname, substring(ci.city_id, 4, 3) as city_code,
				st.state_name as statename, substring(st.state_id, 4, 3) as state_code, cnm.country_name, 
				cnm.country_code, pd.patient_phone, max(ep.inserted_ts) as inserted_ts,pd.email_id, 
				'DM' as patient_details_status 	   		    

	FROM hl7_export_patient ep
		JOIN patient_details pd ON (pd.mr_no = ep.item_id)
		LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
		LEFT JOIN city ci ON (pd.patient_city = ci.city_id)
		LEFT JOIN state_master st ON (pd.patient_state = st.state_id)
		LEFT JOIN country_master cnm ON (pd.country = cnm.country_id)
	Where ep.export_status IN ('N','F') AND ep.item_type = 'PATIENT'
			AND ep.integration_id= ? GROUP BY ep.item_id,pd.oldmrno, pd.patient_name,pd.middle_name, pd.last_name,
			sm.salutation, pd.patient_gender,pd.dateofbirth,pd.expected_dob, pd.patient_address, ci.city_name,
			st.state_name, cnm.country_name, cnm.country_code, pd.patient_phone, pd.email_id, ci.city_id,st.state_id},
		{Slice=>{}}, $ifName);

	$dbh->disconnect();
	return $patientDemoorders;
}
sub updateExportedSuccess {
	my ($mr_no) = @_;

	my $dbh = getConnection();
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_patient set exported_ts=current_timestamp, export_status='S' 
		WHERE export_status IN ('N','F') AND item_id=?});
	$sth->execute($mr_no);
	$dbh->disconnect();
}
sub rotateLog {
	my $maxSize = shift;
	my $fileSize = 0;

	return if ($opt_foreground);
	print "logfile : $logfile \n";
	if (-f $logfile) {
		$fileSize = (stat($logfile))[7];
		if ($fileSize > $maxSize) {
			# rotate old logs
			for (1..8) {
				my $index = 9 - $_;		# 8..1
				if (-f "$logfile.$index") {
					rename($logfile.".".$index, $logfile.".".($index+1));
				}
			}
			rename($logfile, "$logfile.1");
		}
	}

	# close the stdout (current log)
	close(STDIN); close(STDOUT); close(STDERR);
	
	# rename current log
	# start the new one
	open(STDIN,  "+>/dev/null");
	open(STDOUT, ">> $logfile");
	open(STDERR, "+>&STDOUT");
	STDOUT->autoflush(1);
}

