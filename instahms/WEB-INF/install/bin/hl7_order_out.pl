#!/usr/bin/perl -w
package hl7_order_out;
use strict;

use File::Basename;
use Getopt::Long;
use DBI;
use POSIX qw(strftime);
use IO::Socket::INET;
use Sys::Hostname;
use POSIX ":sys_wait_h";
use IO::Handle;


use lib dirname($0)."/../lib";	# find our Hl7 module. Needs to be before the next two lines

use Insta::Util;
use Insta::Logger;
use Insta::HL7Module::LIS;
use Insta::HL7Module::Service;
use Insta::HL7Module::Consultation;
# Database configuration

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;
our $opt_module ='';


sub usage {
	return "Usage: $0 [OPTIONS] <schema>\n" .
	" OPTIONS: \n" .
	"  -d|--db <database>: which database to connect to (hms)\n" .
	"  -s|--schema <schema>: which schema to operate on (hostname)\n" .
	"  -h|--host <host> : datbase host\n".
	"  -U|--username <username> : database username\n".
	"  -W|--password <password> : database password\n".
	"  -o|--port <port_no> : database port no".
	"  -f|--foreground: print to console instead of logfile\n".
	"  -m|--module : hl7 module is required like(LIS, RIS, SERVICES, CONSULTATION)" ;
}

GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", 'password|W=s', "foreground!", "module|m=s") or die usage();

my $util = Insta::Util->new();

my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=> ($opt_module .'_'. basename($0)),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});

$log->rotateLog(10*1024*1024);


$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");

my $pidfile = "/tmp/$opt_module.hl7_order_out.$opt_db.$opt_schema.pid";


END { 
	$util->pidFileCleanWithoutExit($pidfile);
	$log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");
}

##################################################								
sub main {
##################################################
	$util->isProcess($pidfile);	
	
	if($opt_module eq '') {
		$log->info("OOps ! Please specify the hl7 module....\n");
		exit 0;
	}

	my $mod =  getModuleLocation($opt_module); 
	my $dbh = undef;
	eval{
		$dbh = getConnection();
		my $modObj = $mod->new({log=>$log,db=>$dbh});
		$modObj->processModule();
	};

	if($@) {

		$log->info("Error while processing Module : $@");
		if(defined $dbh) {
			$dbh->disconnect();	
			$dbh=undef;
		}
	}

	if(defined($dbh)){
		$dbh->disconnect;	
	}
}

##################################################
sub getModuleLocation {
##################################################
	return Insta::Util->getModuleLocation(shift);
}

##################################################
sub getConnection {
##################################################
	my $dbh = DBI->connect("dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port;", $opt_username, $opt_password,
		{AutoCommit => 1, RaiseError =>1, ShowErrorStatement => 1});
	$dbh->do("SET search_path TO $opt_schema");
	$dbh->do("SET application.username TO '_system'");
	return $dbh;
}

main;
