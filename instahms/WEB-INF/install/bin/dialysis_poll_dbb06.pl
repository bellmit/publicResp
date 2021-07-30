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
use threads;
use DialysisDrivers::DBB06;
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

my $isChild      = 0;
my $FORK_POOL    = {};    ## Hash Map for storing live pid
my $MACHINE_POOL = {};
## Hash Map for storing machine   Using this because Bi-directional map is not available

our $opt_db   = 'hms';
our $opt_port = 5432;
our $opt_schema;
our $opt_host       = '127.0.0.1';
our $opt_username   = 'postgres';
our $opt_password   = '';
our $opt_foreground = 0;
our $opt_poll       = 20;

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

unless ($opt_schema) {
    $opt_schema = hostname;
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
my $pidfile = "/tmp/dialysis_poll_dbb06.$opt_schema.pid";
$util->isProcess($pidfile);

sub interrupt {

    # kill INT to exit.
    if ($isChild) {
        $log->warn("Called to forcefully exit, exiting as a child: $$");
        delete $FORK_POOL->{$$};
        exit 0;
    }
    else {
        $log->warn("Received INT signal, exiting");
        kill( "TERM", $_ ) foreach ( keys $FORK_POOL );
        doExit();
    }
}

sub childExited {
    my $pid = 0;
    do {
        $pid = waitpid( -1, WNOHANG );
        $log->info("**********CHILD_END ($pid ) ****************");
        if ( $pid > 0 ) {
            my $m_id = $FORK_POOL->{$pid};
            $log->info("Child Process Exited PID :$pid ,MachineId : $m_id");
            delete $MACHINE_POOL->{$m_id};
            delete $FORK_POOL->{$pid};
        }
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
$log->info(
"###############>>>>>>>>>>>>>>>>>INIT<<<<<<<<<<<<<<<<<<<<<####################"
);

sub poll {
    my ( $machine, $driver ) = @_;
    my $machineName = $machine->{machine_name};
    my ( $dno1, $result1 ) = $driver->monitorDataResults($machine);
    $log->info("Dno : $dno1");
    saveDnoIfDiff( $dno1, $machine );
    saveResults( $machine, $result1 );
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

sub saveDnoIfDiff {
    my ( $newDno, $machine ) = @_;
    my @chars  = split( //, $newDno );
    my $length = scalar(@chars);
    my $oldDno = $machine->{d_no};
    if ( $length eq 8 ) {
        if ( $oldDno ne $newDno ) {
            my $dbh = getConnection();
            my $dnoChange =
"UPDATE dialysis_machine_master SET d_no='$newDno' WHERE d_no='$oldDno'";
            $dbh->do($dnoChange);
            $dbh->disconnect();
        }
    }
}

sub saveResults {
    $log->info("saveResults...");
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
                print " $field=$value";
            }
            else {
                print " $field=(null)";
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

sub doExit {
    $log->warn("Exiting due to termination signal");
    unlink $pidfile;
    exit 0;
}

sub getMachines() {
    $log->info("Reading machine from database ....");
    my $dbh      = getConnection();
    my $machines = $dbh->selectall_arrayref(
        qq{
        SELECT * FROM dialysis_machine_master dmm                           
        WHERE status='A' AND model_number ='DBB06'  
        }, { Slice => {} }
    );
    return $machines;
}

END {
    $log->info(
"#################>>>>>>>>>>>>>>>>>END<<<<<<<<<<<<<<<<<<<<<##################"
    );
}

sub initdbb06() {
    $log->info("**********INITDB06 Called PID - ($$)***************");
  NEVER_END:
    my $machines     = undef;
    my $machineCount = 0;

    $log->info("################# PROCESS_AUDIT ###########################");
    my $f_pool_count = keys $FORK_POOL;                   ## Process POLL COUNT
    my $m_pool_count = keys $MACHINE_POOL;                ## Machine POOL COUNT
    my @ps_pid       = join( ", ", keys $FORK_POOL );
    my @ps_m         = join( ", ", values $FORK_POOL );
    $log->info(
"TOTAL_PROCESS in FORK_POOL : $f_pool_count, MACHINE_POOL : $m_pool_count "
    );
    $log->info("LIST_OF_PIDs in FORK_POOL : @ps_pid");
    $log->info("LIST_OF_MACHINE_IDs in FORK_POOL : @ps_m");
    $log->info("################# PROCESS_AUDIT ###########################");
    $log->info("Will sleep ...");
    sleep(15);
    ## Degrading CPU useases. If this will not then label will call very often.
    eval {
        $machines     = getMachines();
        $machineCount = @$machines;
    };

    if ( $@ || $machineCount == 0 ) {
        $log->error(
"Reading machine from DB. Check db connectivity or data inside dialysis master"
        );
        $log->error($@);
        doExit();
    }

    $log->info("TOTAL_MACHINE_IN_DB : $machineCount");
    foreach my $machine (@$machines) {
        my $driver = "DialysisDrivers::" . $machine->{model_number};
        my $id     = $machine->{machine_id};
        my $name = $machine->{machine_name} . "@" . $machine->{network_address};
        my $pid  = $MACHINE_POOL->{$id};
        $log->info(
"$name: INITIATING_FORK request, MachineID : $id, name : Name\@Net : $name "
        );
        if ( $pid && $FORK_POOL->{$pid} && ( kill 0, $pid ) ) {
            $log->info(
"Previous child is still alive for MachineId : $id , PID : $pid "
            );
            cleanLongRunningPID( $pid, $machine );
            next;
        }
        elsif ( ( scalar keys $FORK_POOL < $opt_poll ) || waitForChild() ) {
            $pid = fork;
        }
        else {
            $log->info("No work to do. Will check next machine");
            next;
        }

        if ( not defined $pid ) {
            warn 'Could not fork';
            next;
        }

        if ( not $pid ) {
            $isChild = 1;
            $log->info(
                "!!!!!!!!!!!!!!!!!!!!!!!( $$ )!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            );
            $log->info(
"NEW CHILD MachineId : $machine->{machine_id}, MachineName: $machine->{machine_name}, PID: ( $$ ) STARTED"
            );

            #sleep 5;
            poll( $machine, $driver );
            $log->info(
"MachineId : $machine->{machine_id} , Name : $machine->{machine_name}, PID - $$, FINISHED"
            );
            $log->info(
" Exiting PID ($$), MachineID : $machine->{machine_id}, Name : $machine->{machine_name}, IP : $machine->{network_address}"
            );
            $log->info(
                "iiiiiiiiiiiiiiii-$$-iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
            exit 0;
        }
        else {
            $MACHINE_POOL->{$id} = $pid;
            $FORK_POOL->{$pid}   = $id;
            $log->info(
"In the parent process PID : [ $$ ] , Child pid :( $pid ), Number of fork child processes :"
                  . keys $FORK_POOL );
        }
    }
    $log->info("########### Parent ($$) Ending ####################");
    goto NEVER_END;

}

sub waitForChild() {
    $log->info("Waiting for child to exit......");
    my $forkSize = scalar keys $FORK_POOL;
    $log->info("POOL SIZE before exiting fork : $forkSize");
    my $pid = wait();
    delete $FORK_POOL->{$pid};
    $forkSize = scalar keys $FORK_POOL;
    $log->info("POOL SIZE after exiting fork : $forkSize");

    #sleep 2;
    $log->info("Child exited Parent PID:  < $$ > , CHPID : < $pid >)");
    return $pid;
}

sub cleanLongRunningPID() {
    my ( $running_pid, $machine ) = @_;

    my $ps = `ps -o etime= -p $running_pid`;
    my $t_h = ( split( /:/, $ps ) )[0];
    print "\n PID is alive from : $ps \n";
    if ( $t_h >= 35 ) {
        print "\nForce Killing Process (PID): $running_pid\n";
        kill( 'TERM', $running_pid );
        saveResults( $machine, { polled_status => 'R' } );
        $log->warn("$machine->{machine_name} : Skipping one request");
    }
}

initdbb06();

