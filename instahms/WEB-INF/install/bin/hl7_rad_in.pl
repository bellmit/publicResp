#!/usr/bin/perl -w

#
# Imports lab results into Insta HMS, generates a report for each test
# that is completed (all results have arrived) for file based import of results.
# For socket based import, we need a daemon specific to each interface, as the
# socket protocol might differ.
#

use strict;
use DBI;
use POSIX qw(strftime);
use File::Basename;
use IO::Socket::INET;
use Sys::Hostname;
use Getopt::Long;
use LWP;
use HTTP::Request::Common;
use HTTP::Cookies;

use lib dirname($0)."/../lib";	# find our Hl7 module. Needs to be before the next two lines
use Hl7::Message;
use Hl7::Segment;
use Insta::RadImport;
use Insta::Util;
use Insta::Logger;

# args

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = '127.0.0.1';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;

sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--Password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -f|--foreground: print to console instead of logfile\n" ;
}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", 'password|W=s', "foreground!") or die usage();



unless ($opt_schema) {
	$opt_schema = hostname;
	$opt_schema =~ s/instahms-//;
}

my $util = Insta::Util->new();
my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=>basename($0),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});
$log->rotateLog(10*1024*1024);

$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");



my $this_interface = "Infinitt";


# Create a lock file and ensure we don't run two instances of ourselves
# at any point in time.
my $pidfile = "/tmp/hl7_rad_in_$opt_db.$opt_schema.pid";


$util->isProcess($pidfile);

# these will be read from from the db itself
my $userId;
my $interfaces;

eval {
	readConfig();
};
if ($@) {
	$log->error("Reading config: ", $@);
	print "$DBI::errstr\n";
	exit 1;
}

my $diagProcessor = Insta::RadImport->new( {
		db=>$opt_db, schema=>$opt_schema, db_port=>$opt_port,db_username=>$opt_username,db_password=>$opt_password,
		userId=>$userId, host=>$opt_host
});

# do the job for each interface
foreach my $if (keys %$interfaces) {
	eval {
		$log->info("Processing interface $if");
		processInterface($if);
	};
	if ($@) {
		$log->error("Processing interface $if: ", $@);
		# continue with the next one, if any
	}
}

#
# Cleanup and exit
#

$util->pidFileCleanUp($pidfile);
# main ends, subs follow

sub getConnection {

	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port", "$opt_username", "$opt_password",
		{AutoCommit => 1, RaiseError =>1});

	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

sub readConfig {

	my $dbh = getConnection();

	$interfaces = $dbh->selectall_hashref(
		"SELECT * FROM hl7_lab_interfaces WHERE status='A' AND coalesce(results_import_dir, '') != '' ",
		'interface_name');

	my $hl7Prefs = $dbh->selectrow_hashref("SELECT * from hosp_hl7_prefs");
	$userId = 'auto_update';
	$dbh->disconnect();
}

sub processInterface {
	my ($ifName) = @_;
	return if ($ifName ne $this_interface);

	my $baseDir = $interfaces->{$ifName}{results_import_dir};

	my $inputDir = "$baseDir/in";		# location for input files
	my $doneDir  = "$baseDir/done";		# moved here after successful processing
	my $errorDir = "$baseDir/error";	# moved here if any errors. Move back to in to reprocess

	opendir(DIR, $inputDir) || die "Unable to opendir $inputDir: $!\n";
	my $file;
	local $/ = undef;

	while ($file = readdir(DIR)) {
		next if (($file eq ".") || ($file eq ".."));
		print $file;
		my ($error, $errMsg);
		eval {
			print "Processing $file\n";
			open (MSGF, "$inputDir/$file") || die "Unable to open $file: $!\n";
			my $messageStr = <MSGF>;
			my $msg = Hl7::Message->new($messageStr);
			close MSGF;
			$log->debug($messageStr);
			$errMsg = $diagProcessor->processMessage($interfaces->{$ifName}, $msg);
		};
		if ($@) {
			$log->error($@);
			$errMsg = $@;
		}
		if ($errMsg) {
			$log->error("$file has errors, moving to error");
			rename("$inputDir/$file", "$errorDir/$file");
		} else {
			$log->info("$file processed, moving to done");
			rename("$inputDir/$file", "$doneDir/$file");
		}
	}
	closedir(DIR);
}

END { $log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");}
