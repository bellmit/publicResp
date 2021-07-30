#!/usr/bin/perl -w

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
use Insta::PrescriptionImport;
use Insta::Util;
use Insta::Logger;



# Database configuration

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;

my $this_interface = 'Praxify';

sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -f|--foreground: print to console instead of logfile\n" ;
}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", 'password|W=s', "foreground!") or die usage();

my $util = Insta::Util->new();
my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=>basename($0),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});

$log->rotateLog(10*1024*1024);

$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");

$log->info("Starting HL7 Lab imports, pid : ", $$);

	# Create a lock file and ensure we don't run two instances of ourselves
	# at any point in time.
	
my $pidfile = "/tmp/hl7_prescription_in_$opt_db.$opt_schema.pid";

$util->isProcess($pidfile);


# these will be read from from the db itself
my $userId;
my $interfaces;

eval {
	readConfig();
};
if ($@) {
	$log->error("Reading config: " . $@);
	exit 1;
}

my $prescImport = Insta::PrescriptionImport->new( {
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


############################
# main ends, subs follow
############################

##################################################
sub getConnection {
##################################################
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port;", "$opt_username", "$opt_password",
			{AutoCommit => 0, RaiseError =>1, ShowErrorStatement => 1});

	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username = '_system'");
	return $dbh;
}

##################################################
sub readConfig {
##################################################
	my $dbh = getConnection();
	$interfaces = $dbh->selectall_hashref(
		"SELECT * FROM hl7_lab_interfaces WHERE status='A' AND coalesce(results_import_dir, '') != '' ",
		'interface_name');
	my $hl7Prefs = $dbh->selectrow_hashref("SELECT * from hosp_hl7_prefs");
	$userId = 'auto_update';
	$dbh->disconnect();
}

##################################################
sub processInterface {
##################################################
	my ($ifName) = @_;


	return 	if $ifName ne $this_interface;
	my $baseDir = $interfaces->{$ifName}{results_import_dir};

	my $inputDir = "$baseDir/in";		# location for input files
	my $doneDir  = "$baseDir/done";		# moved here after successful processing
	my $errorDir = "$baseDir/error";	# moved here if any errors. Move back to in to reprocess

	opendir(DIR, $inputDir) || die "Unable to opendir $inputDir: $!\n";
	my $file;
	$/ = undef;

	while ($file = readdir(DIR)) {

		$log->info("########### HL7  ADT Message #######################");

		next if (($file eq ".") || ($file eq ".."));

		my ($error, $errMsg);
		my $dbh = getConnection();
		eval {
			$log->info("Processing $file");
			open (MSGF, "$inputDir/$file") || die "Unable to open $file: $!\n";
			my $messageStr = <MSGF>;
			my $msg = Hl7::Message->new($messageStr);
			close MSGF;
			$errMsg = $prescImport->processMessage($interfaces->{$ifName}, $msg,$dbh);
			$dbh->commit
		};
		if ($@) {
			$log->error($@);
			$errMsg = $@;
			$dbh->rollback;
		}
		if ($errMsg) {
			$log->error("$file has errors, moving to error");
			rename("$inputDir/$file", "$errorDir/$file");
		} else {
			$log->info("$file processed, moving to done");
			rename("$inputDir/$file", "$doneDir/$file");
		}

		$dbh->disconnect();

	}
	closedir(DIR);
}


END { $log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");}

=head1 NAME

hl7_prescription_in - Imports doctor prescription into INSTA HMS from specified directory.

Prescriptions can be Medicine, Test or Services Order by doctor.

=head1 SYNOPSIS

	perl hl7_prescription_in.pl

=head1 CONFIGURATION

=head1 SUBROUTINES

=over 4

=item * usage -

=item * getConnection -  Returns the database handler.

=item * readConfig -

=item * processInterface -

=back

=head1 DESCRIPTION

=head1 BUGS		: Bug#40731

=head1 AUTHOR	: INSTA developer team

=head1 COPYRIGHT	: @INSTA

=cut
