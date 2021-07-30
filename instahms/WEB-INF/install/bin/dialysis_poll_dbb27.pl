#!/usr/bin/perl -w

use strict;
use DBI;
use POSIX qw(strftime);
use File::Basename;
use Getopt::Long;
use Sys::Hostname;
use POSIX ":sys_wait_h";
use lib dirname($0) . "/../lib";
use IO::Handle;
use DialysisDrivers::DBB27;
use Insta::Util;
use Insta::Logger;

#
# Goes through a list of active dialysis machines, and depending on the type of machine,
# starts up a driver and gets parameters from the driver. The parameters are saved
# into the dialysis_maschine_status table.
#
# This program needs to be started up during boot up process (or a cron-job), and it will run
# as a daemon. Polling intervals are handled by this program, by distributing the load across
# the number of machines.
#

my $server       = "127.0.0.1";
my $reqExit      = 0;
my $isChild      = 0;
my %childPid     = ();
my %processAlive = ();
my $rotated      = 0;

our $opt_db   = 'hms';
our $opt_port = 5432;
our $opt_schema;
our $opt_host       = '127.0.0.1';
our $opt_username   = 'postgres';
our $opt_password   = '';
our $opt_foreground = 0;
our $opt_poll       = 60;

GetOptions(
    "port|o=i",     "db=s",         "schema=s", "host=s",
    "username|U=s", 'password|W=s', "poll=s",   "foreground!"
  )
  or die "Usage: $0 [OPTIONS]\n"
  . "  -o|--port <port_no> : database port no"
  . "  -d|--db <database>: which database to connect to (hms)\n"
  . "  -s|--schem <schema>: schema to connect to (hostname)\n"
  . "  -h|--host <host> : datbase host\n"
  . "  -U|--username <username> : database username\n"
  . "  -W|--password <password> : database password\n"
  . "  -p|--poll <interval>: number of seconds between each poll\n"
  . "  -f|--foreground: run in foreground, don't daemonize\n";

$opt_db = 'hms' unless ($opt_db);

unless ($opt_schema) {
    $opt_schema = hostname;
    $opt_schema =~ s/instahms-//;
}

my $util = Insta::Util->new();
my $log  = Insta::Logger->new(
    {
        level          => $util->getProperty('log.level'),
        base_name      => basename($0),
        db             => $opt_db,
        schema         => $opt_schema,
        isConsilePrint => $opt_foreground
    }
);

$log->rotateLog( 10 * 1024 * 1024 );

my $pidfile = "/tmp/dialysis_poll_ddb27.$opt_schema.pid";
$util->isProcess($pidfile);

#
# Daemonize if required, but first check if we are already running. This program
# can therefore be called as a cron-job to install itself if not already running
# as well as restart itself if it has crashed.
#

sub doExit {
    $log->warn("Exiting due to termination signal");
    unlink $pidfile;
    exit 0;
}

sub interrupt {

    # kill INT to exit.
    if ($isChild) {
        $log->warn("Called to forcefully exit, exiting as a child: $$");
        exit 0;
    }
    else {
        $log->warn("Received INT signal, exiting");
        kill( "TERM", $_ ) foreach ( keys %processAlive );
        doExit();
    }
}

sub childExited {
    my $pid = 0;
    do {
        $pid = waitpid( -1, WNOHANG );
        delete $processAlive{$pid};
    } while ( $pid > 0 );

    # need to reinstall the handler again.
    $SIG{CHLD} = \&childExited;
}

$SIG{INT}  = \&interrupt;
$SIG{TERM} = \&interrupt;
$SIG{CHLD} = \&childExited;

#
# Start the poll loop
#
my $machines;
my $interval;
my $loopCount = 0;

my $numMachines;

$log->info(
"#####################>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<#################"
);

sub main() {

    while (1) {

        my $loopStart = time;
        $loopCount++;

# Get the list of active dialysis machines: once per 15 loops (it may change, but
# changes will not be reflected for 15 minutes)
        $interval = int( $opt_poll / $numMachines ) if ($numMachines);
        if ( !defined($machines) || ( $loopCount % 15 ) == 0 ) {
            $log->info("Refreshing machine list: loop count is $loopCount");

            eval {
                $machines    = getMachines();
                $numMachines = @$machines;
            };

            if ( $@ || $numMachines == 0 ) {
                $log->error(
"Reading machine from database. Check db connectivity or data inside dialysis master"
                );
                $log->error($@);
                doExit();
            }
            $log->info("Num machines active: $numMachines");

            # distribute the poll across the total interval
            $interval = int( $opt_poll / $numMachines ) if ($numMachines);
            $log->info("Interval between machine polls is: $interval");
        }

        my $nextPoll        = $loopStart + $opt_poll;
        my $nextMachinePoll = $loopStart + $interval;

        # iterate over machines
        $log->info(
"############ LOOPING pool, Machine Count : $numMachines, Loop Count :  $loopCount #################"
        );
        foreach my $machine (@$machines) {

            # instantiate the driver associated with this machine
            my $driver = "DialysisDrivers::" . $machine->{model_number};
            eval("use $driver");

            my $id = $machine->{machine_id};
            my $name =
              $machine->{machine_name} . "@" . $machine->{network_address};

            # Get results from the driver

# To protect the main loop, we'll spawn off a new process to do the actual polling.
# DBB27 machines have a peculiar habit of hanging after sending a request.
# We also need to keep track of how many children we've spawned off and kill the errant ones
# that are hanging before we start the next poll.

            my $pid = $childPid{$id};
            if ( $pid && $processAlive{$pid} && ( -e "/proc/$pid" ) ) {
                $log->info(
"$name: Previous child process ($pid) still alive, killing forcefully\n"
                );
                kill( "TERM", $pid );
                saveResults( $machine, { polled_status => 'R' } )
                  ;    # Not Responding
                 # skip the poll for this time, give the machine some time to recover
                $log->warn("$name: Skipping one request");

            }
            else {
                $log->info("$name: Initiating request: fork");
                if ( $pid = fork() ) {

                    # parent process: store the pid and continue the loop
                    $childPid{$id}      = $pid;
                    $processAlive{$pid} = 1;

                }
                else {
                    # child process: get results, save results, exit.
                    # $results is a hash of field_name => value
                    $isChild = 1;
                    my $results = $driver->poll($machine);
                    saveResults( $machine, $results );
                    exit 0;
                }
            }
            my $now = time();

            # we may be interrupted by sigchlds, so do this in a loop.
            while ( $now < $nextMachinePoll ) {

          # print "Sleeping additional ", $nextMachinePoll - $now, " seconds\n";
                sleep( $nextMachinePoll - $now );
                $now = time();
            }

            $nextMachinePoll += $interval;
        }

        my $now = time();
        while ( $now < $nextPoll ) {

       # print "Sleeping main loop additional ", $nextPoll - $now, " seconds\n";
            sleep( $nextPoll - $now );
            $now = time();
        }

    }
}

##################################################
sub getConnection {
##################################################
    my $dbh = DBI->connect(
        "dbi:Pg:dbname=$opt_db;host=$opt_host;port=$opt_port;",
        $opt_username,
        $opt_password,
        { AutoCommit => 1, RaiseError => 1, ShowErrorStatement => 1 }
    );
    $dbh->do("SET search_path TO $opt_schema");
    return $dbh;
}

sub saveResults {
    my ( $machine, $results ) = @_;
    my $id   = $machine->{machine_id};
    my $name = $machine->{machine_name} . "@" . $machine->{network_address};
    my $dbh  = getConnection();

    my $query =
      "UPDATE dialysis_machine_status SET last_polled_time=current_timestamp";
    my $condition = " WHERE machine_id=?";

    my @values = ();
    if ($results) {

        # save all the attributes of results using the key as the field name.
        $log->info("$name: Result:");
        foreach my $field ( keys %$results ) {
            my $value = $results->{$field};
            if ( defined($value) ) {
                $log->info(" $field=$value");
            }
            else {
                $log->info(" $field=(null)");
            }
            $query .= ", $field=?";
            push @values, $value;
        }

        # set the last successful results timestamp only if there is no errors.
        unless ( $results->{polled_status} eq 'X'
            || $results->{polled_status} eq 'E' )
        {
            $query .= ", last_results_time=current_timestamp";
        }
        print "\n";
    }
    else {
        # unreachable
        $log->info("$name: no results received, setting polled_status to X");
        $query .= ", polled_status=?";
        push @values, 'X';
    }

    $dbh->do( $query . $condition, undef, @values, $id );
    $dbh->disconnect();
}

sub getMachines() {
    $log->info("Reading machine from database ...");
    my $dbh      = getConnection();
    my $machines = $dbh->selectall_arrayref(
        qq{
				SELECT * FROM dialysis_machine_master WHERE status = 'A' AND model_number = 'DBB27'
				}, { Slice => {} }
    );
    return $machines;
}

END {
    $log->info(
"#####################>>>>>>>>>>>>>>>>>END<<<<<<<<<<<<<<<<<<<<<#################"
    );
}

main();
