#!/usr/bin/perl -w
# use strict;

use File::Basename;
use Getopt::Long;
use DBI;
use POSIX qw(strftime);
use Sys::Hostname;
use POSIX ":sys_wait_h";


use lib dirname($0)."/../lib";	# find our Hl7 module. Needs to be before the next two lines

use Insta::Util;
use Insta::Logger;
use Insta::HL7Module::DiagResultExporter;

# Database configuration

our $opt_db = 'hms';
our $opt_port = 5432;
our $opt_schema ='' ;
our $opt_host = 'localhost';
our $opt_username = 'postgres';
our $opt_password ='';
our $opt_foreground = 0;
our $opt_module ='';
our $opt_apiuser = '';
our $opt_apipwd ='';
our $opt_apihost='localhost';
our $opt_apiport='80';
our $opt_apiname='instaapi';


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
	"  -m|--module : hl7 module is required like(LIS, RIS, SERVICES, CONSULTATION)".
	"  -au|--apiuser :  API application username\n".
	"  -ap|--apipwd : API application password \n" .
	"  -ah|--apihost : API application host \n" .
	"  -ap|--apiport : API application port \n" .
	"  -an|--apiname : API application name \n" ;
}


GetOptions( "port|o=i", "db=s", "schema=s", "host=s", "username|U=s", 'password|W=s', "foreground!", "module|m=s", "apiuser=s", "apipwd=s",
		"apihost=s", "apiport=s", "apiname=s") or die usage();

my $util = Insta::Util->new();

my %api_params = (
	schema=>"$opt_schema",apiuser=>"$opt_apiuser",apipwd=>"$opt_apipwd",
	apihost=>"$opt_apihost",apiport=>"$opt_apiport",apiname=>"$opt_apiname");

#unless ($opt_module) {
#       $opt_module = 'DIAGRESULT';
#}

my $log = Insta::Logger->new({level=>$util->getProperty('log.level'),base_name=> ($opt_module .'_'. basename($0)),db=>$opt_db,
								schema=>$opt_schema,isConsilePrint=>$opt_foreground});

$log->rotateLog(10*1024*1024);


$log->info("#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################");

my $pidfile = "/tmp/$opt_module.hl7_result_out.$opt_db.$opt_schema.pid";


END { 
	$util->pidFileCleanWithoutExit($pidfile);

	$log->info("#####################<<<<<<<<<<<<<<<<<END>>>>>>>>>>>>>>>>>>>>>>#################\n");
}



##################################################								
sub main {
##################################################

	$log->debug("Entering main...");
	$util->isProcess($pidfile);	
	
#	if($opt_module eq '') {
#		$log->info("Missing Hl7 module, please specify the module to run");
#		exit 0;
#	}
	
	my $transType = 'Result';
	my $format = 'HL7';
	
	my $dbh = undef;
	eval{
		$dbh = getConnection();
		my $exporters =  getExporters($opt_module); # right now only opt_module is used, other 2 parameters are ignored
		for my $key (keys %$exporters) {
			$log->debug("exported key : ".$key." module:".$exporters->{$key}); 
			
			my $modObj = $exporters->{$key}->new({log=>$log,db=>$dbh});
#			$modObj->init();
			$modObj->export(\%api_params);
		}
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
sub getExporters {
##################################################

	my $key = shift;
	$log->debug("exporter key:".$key."xxx");
	my $href = {
		"DIAGRESULT" 	=> Insta::HL7Module::DiagResultExporter
	};
	
	if((not defined $key) || ($key eq '')) {
		$log->debug("module not specified: returning all modules");
		return $href;
	}
	
	my $ret = {$key=>$href->{$key}};
	return $ret;
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
