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
use Insta::DiagDBImporter;
use Insta::DiagImport;
use Insta::Util;
use Insta::Logger;



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

my $userId = 'HL7_update';

# TODO change this to production parameters


sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -n|--foreground: print to console instead of logfile\n" .
	"-ddb|--dest_db : destination database".
	"-ddU|--dest_username : destination db username".
	" -dp|--dest_password : destination db password".
	" -ds|--dest_schema : destination schema";
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


my $util = Insta::Util->new();                                                  

my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=>basename($0),db=>$opt_db,
		schema=>$opt_schema,isConsilePrint=>$opt_foreground});


$log->rotateLog(10*1024*1024);                                                  

$log->info("###########>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<##############");



my $logfile = "/var/log/insta/$opt_db/hl7_db_lab_in.$opt_schema.log";
my $pidfile = "/tmp/hl7_db_lab_in.$opt_db.$opt_schema.pid";



my %ORU_MSH = (
	sendApp=>'InstaHMS', recvApp=> 'InstaHMS',
	msgType=>['ORU','R01'], procId=>'P', ver=>'2.3'
);
my %ORM_MSH = (
	sendApp=>'InstaHMS', recvApp=> 'InstaHMS',
	msgType=>['ORM','O01'], procId=>'P', ver=>'2.4'
);
#
# Read the config from the database: Do this before daemonizing so that we don't need
# to run if we cannot read the config, or if the config says there's nothing to do.
#


$util->isProcess($pidfile);

my $interfaces;
eval {
	readConfig();
};
if ($@) {
	$log->error("Reading config:  $@ ");
	exit 1;
}

if (keys(%$interfaces) < 1) {
	$log->error("No interfaces configured for exporting orders, exiting.");
	exit 2;
}

# get the set of orders to be exported into hl7_export_items
# (could also be a trigger ... later)


$log->info("Starting to look for reports ");

my $diagProcessor = Insta::DiagImport->new( {
		db=>$opt_db, schema=>$opt_schema, db_port=>$opt_port,db_username=>$opt_username,db_password=>$opt_password,
		userId=>$userId, host=>$opt_host
});

# iterate through the interfaces
foreach my $ifName (keys %$interfaces) {
	eval {
		my $if = $interfaces->{$ifName};
		# get the exporter for the export type
		my $importer = getImporter($if->{export_type});
		my $handler = $importer->open($if->{orders_export_ip_addr}, $if->{orders_export_port});
		my $reports = $importer->importData($handler, $if->{interface_name});
		my $numReports = @$reports;

		$log->info("Found $numReports reports for interface = $ifName");

		foreach my $report (@$reports) {
			my $errMsg = undef;
			eval {
				my $msg;
				my $resultSts;			
				if($report->{status} eq 'SC'){
					$msg = getORMmessage($report);
				}else{
					$msg = getMessage($report);
				}
				$log->debug($msg->toString(1));
				$log->info("Message object created for prescribed_id  : $report->{prescribed_id} , result_label_id : $report->{resultlabel_id}");
				$log->info("Inserting message in database.....");
				$errMsg = $diagProcessor->processMessage($if, $msg);
				$log->info("insertion done.");
			};
			if ($@) {
				$log->error($@);
				$errMsg = $@;
			}
			if ($errMsg) {
				$log->error("Updating failure message in sender table");
				$importer->updateFailure($handler,$report->{id}, $errMsg);
			} else {
				$log->info("Updating success message in sender table");
				$importer->updateSuccess($handler, $report->{id}, '');
			}

		}
		$importer->close($handler);
	};
	if ($@) {
		print "ERROR: ", $@, "\n";
	}
}

#clean up
END{
	$log->info("##############>>>>>>>>>>>>>> END <<<<<<<<<<<<<<<<<###################");
	$util->pidFileCleanUp($pidfile);
}


####################
# Subs
####################

sub readConfig {
	my $dbh = getConnection();
	$log->info("Reading Hl7 configuration");
	# get all interfaces configured
	$interfaces = $dbh->selectall_hashref(
		"SELECT hli.*,hci.*, (hli.interface_name||'-'|| hci.center_id)::character varying as interface_center
		 FROM hl7_lab_interfaces hli
		 JOIN hl7_center_interfaces hci USING(hl7_lab_interface_id)
		 WHERE status='A' AND hci.export_type IN ('D')",
		'interface_center');
	$dbh->disconnect();
}

sub getImporter {
	my ($exportType) = @_;

	if ($exportType eq 'D') {
		my $dbImporter = Insta::DiagDBImporter->new( {
			db=>$opt_dest_db, schema=>$opt_dest_schema, db_username=>$opt_dest_username,db_password=>$opt_dest_password
		});

		return $dbImporter; 
	}
}

sub getConnection {
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});
	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

sub getMessage {
	my $report = shift;

	my $msg = new Hl7::Message(\%ORU_MSH);
	my $orc = new Hl7::Segment('ORC', { 
					placerOrderNum=> [ $report->{prescribed_id}.'.' . $report->{resultlabel_id} .'.'. 
					$report->{sample_no} || '', 'InstaHMS' ]
				});
	$msg->addSegment($orc);
	my $obr = new Hl7::Segment('OBR', { 
					sid=>1, 
					placerOrderNum=> [ $report->{prescribed_id} .'.' . $report->{resultlabel_id} .'.'. 
					$report->{sample_no} || '', 'InstaHMS' ],
					resultSts=>$report->{status}
				});

	$msg->addSegment($obr);
	my $obx = new Hl7::Segment('OBX', {
					sid=>1,
					valueType=>$report->{value_type} || 'ST',
					obsValue=>$report->{report_value}
				});

	$msg->addSegment($obx);
	return $msg;
}
sub getORMmessage {
	my $report = shift;

	my $msg = new Hl7::Message(\%ORM_MSH);
	my $orc = new Hl7::Segment('ORC', { 
					placerOrderNum=> [ $report->{prescribed_id}.'.' . $report->{resultlabel_id} .'.'. 
					$report->{sample_no} || '', 'InstaHMS' ],
					ctrl=>'SC',
					orderSts=>'RP'
				});
	$msg->addSegment($orc);
	my $obr = new Hl7::Segment('OBR', { 
					sid=>1, 
					placerOrderNum=> [ $report->{prescribed_id} .'.' . $report->{resultlabel_id} .'.'. 
					$report->{sample_no} || '', 'InstaHMS' ],
					resultSts=>$report->{status}
				});

	$msg->addSegment($obr);
	return $msg;
}
