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
use Insta::Import;
use Insta::Util;
use Insta::Logger;

# Database arg

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
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

my $util = Insta::Util->new();
my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=>basename($0),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});
$log->rotateLog(10*1024*1024);

$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");

END { $log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");}


# Create a lock file and ensure we don't run two instances of ourselves
# at any point in time.



##################################################								
sub init {
##################################################								

	
	my $pidfile = "/tmp/hl7_import_$opt_db.$opt_schema.running";

 	$util->isProcess($pidfile);

 	my @modules = $util->getImportModules();

	foreach my $module (@modules) {
		eval{
			processModule($module);	
		};

		if($@){
			$log->error("Processing failed for [$module] due to  : $@");
		}
	}

	$util->pidFileCleanUp($pidfile);

}


##################################################
sub processModule {
##################################################

	my $module = shift;
	
	my $mod_obj = undef;

	$log->info("[$module] Processing .......");
	
	my $interfaces;

	if($module eq "DIAGImport") {

		$log->debug("Creating DIAGImport Object for Laboratory");
		my $modLoc = "Insta::Import";
		$mod_obj = $modLoc->new({test_dept=>'DEPT_LAB',log=>$log});
		$log->debug("Getting interfaces ...");
	        my $dbh = getConnection();
		$interfaces = getInterface($dbh);
		$dbh->disconnect();

	}
	
	if($module eq "RADImport") {
		$log->debug("Creating RADImport Object for Radiology");
		my $modLoc = "Insta::Import";
		$mod_obj = $modLoc->new({test_dept=>'DEPT_RAD',log=>$log});
		$log->debug("Getting interfaces ...");
	        my $dbh = getConnection();
		$interfaces = getInterface($dbh);
		$dbh->disconnect();

	}

	
        if(!defined $interfaces || !%$interfaces){
             $log->warn("No any interface configured");
             return ;
        }

	foreach my $ifName (keys %$interfaces) {
		$log->info("Reding all file for [$ifName] interface ... ");
		my $if = $interfaces->{$ifName};
		processInterface($if,$mod_obj);
	}

}



sub processInterface {

	my ($interfaceObj, $mod_obj) = @_;

	my $baseDir = $interfaceObj->{results_import_dir};

	my $inputDir = "$baseDir/in";		# location for input files
	my $doneDir  = "$baseDir/done";		# moved here after successful processing
	my $errorDir = "$baseDir/error";	# moved here if any errors. Move back to in to reprocess

	$log->info("Opening input directory for reading a file inputDir = $inputDir");
	opendir(DIR, $inputDir) || die "Unable to opendir $inputDir: $!\n";
	my $file;
	while ($file = readdir(DIR)) {
		
		next if (($file eq ".") || ($file eq ".."));
		my ($error, $errMsg,$dbh);
		eval {
			local $/ = undef;
			$log->info("Reading hl7 file = $inputDir\/$file\n");
			open (MSGF, "$inputDir/$file") || die "Unable to open $file: $!\n";
			my $messageStr = <MSGF>;
			$log->debug($messageStr);
			my $msg = Hl7::Message->new($messageStr);
			close MSGF;

        		$dbh = getConnection();

			$errMsg = $mod_obj->processMessage($interfaceObj, $msg, $dbh);

		};

		if ($@) {
			$log->error($@);
			$errMsg = $@;
		}
		if ($errMsg) {

				eval{
					if(defined $dbh) {
			                	$log->error("Rolling back due to error: $errMsg\n");
						$dbh->rollback;
						$dbh->disconnect();

					}
				};
				if($@) {
					$log->error("$@");
				}
				$log->error("File $inputDir\/$file moving in error folder $errorDir ");
				rename("$inputDir/$file", "$errorDir/$file");
		} else {

			eval{
				if(defined $dbh) {

					$dbh->commit;
					$dbh->disconnect();
					$log->info("Successfully commited all process");
				}
			};
			if($@) {
				$log->info($@);
			}

				$log->info("File $inputDir/$file  processed successfully , moving to done folder $doneDir");
				rename("$inputDir/$file", "$doneDir/$file");
			}

		$log->info("<<<<<<<<<<<<<<<<<<<<>>>>**********************************>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
	}
	closedir(DIR);
}





##################################################
sub getInterface {
##################################################

	my $dbh = shift;

	# get all interfaces configured
	my $interfaces = $dbh->selectall_hashref(
		"SELECT hli.*,hci.*, (hli.interface_name||'-'|| hci.center_id)::character varying as interface_center
		 FROM hl7_lab_interfaces hli
		 JOIN hl7_center_interfaces hci USING(interface_name)
		 WHERE status='A' AND export_type IN ('S','F','B')",
		'interface_center');
	
	return $interfaces;
}

##################################################
sub getConnection {
##################################################

	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port;", $opt_username, $opt_password,
		{AutoCommit => 0, RaiseError =>1, ShowErrorStatement => 1});
	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}


init;
