#!/usr/bin/perl -w

#
# Monitor the access log file, and take some action if
# the time taken for a response is consistently larger than what is
# expected.
#

use strict;
use IO::Handle;
use Getopt::Long;

our $opt_threshold = 5;		# for taking detailed action
our $opt_num_requests = 5;
our $opt_log_threshold = 1;		# for logging the request
our $opt_interval = 15;
our $opt_wait = 300;
our $opt_foreground = 0;
our $opt_tomcat_home = "/usr/local/tomcat-9";

GetOptions("tomcat_home=s", "threshold=i", "log_threshold=i", "num_requests=i", "interval=i", "foreground!") or die usage();

sub usage {
    print "Usage: logmon.pl [OPTIONS]\n";
    print "Monitor a log file and take some actions when requests start taking too long continuously\n";
	print " eg, if 5 or more requests take more than 20 seconds within 15 seconds of each other,\n";
	print " then initiate an action\n";
	print "Options:\n";
	print " --tomcat_home=<path>: seconds above which it is considered too long (20)\n";
	print " --threshold=<n>: seconds above which it is considered too long (20)\n";
	print " --num_requests=<n>: number of requests taking too long crosses this limit (5)\n";
	print " --log_threshold=<n>: seconds above which we log the request (5)\n";
	print " --interval=<n>: number of seconds between successive conditions to qualify as continuous (15)\n";
	print " --wait=<n>: number of seconds to wait between multiple actions (300)\n";
	print " --foreground=: run in foreground, ie, don't daemonize\n";
}

#
# Daemonize if required, but first check if we are already running. This program
# can therefore be called as a cron-job to install itself if not already running
# as well as restart itself if it has crashed.
#
my $logfile = "/var/log/insta/logmon.log";
my $pidfile = "/var/run/logmon.pid";

unless ($opt_foreground) {

	my $exists = open(PIDCHECK, $pidfile);
	if ($exists) {
		my $pidcheck = <PIDCHECK>;
		chomp $pidcheck;
		close PIDCHECK;
		if (-e "/proc/$pidcheck") {
			print "Already running: $pidcheck\n";
			exit 0;
		} else {
			print "Warning: PID file exists but no process: $pidcheck\n";
		}
	}

	if (my $pid = fork()) {
		# parent process, write the child pid and exit
		open PIDFILE, "> $pidfile" or die "Unable to open PID File: $pidfile: ", $!, "\n";
		print PIDFILE $pid, "\n";
		close PIDFILE;
		exit 0;
	}

	chdir "/tmp";
	rotateLog();
}

my $last_action = 0;
my $num = 0;
my $max_seconds = 0;
my $last_threshold_time = 0;
my $newLogStarted = 0;

my $accessLogDir = $opt_tomcat_home . '/logs/access';

sub do_action() {
	my $now = time;
	if ($last_action + $opt_wait > $now) {
		print "Action requested too soon: ", $last_action + $opt_wait, ", ", $now, "\n";
		return;
	}

	my $now_string = localtime();
	print "Action requested at ", $now_string, ", Max seconds: $max_seconds\n";
	system("top -bn2");
	system("netstat -pant");
	system("sudo kill -QUIT `cat /var/run/tomcat.pid`");

	$last_action = time;
}

####################

for (;;) {
	my $accessLogFile = `ls -t $accessLogDir | head -1`;
	chomp($accessLogFile);
	$accessLogFile = $accessLogDir . "/" . $accessLogFile;

	open ALOG, $accessLogFile or die "Cannot open $accessLogFile: $!";
	print "Started monitoring file: $accessLogFile\n\n";
	# read up the entire file, start only from the tail
	while (<ALOG>) {};

	for (;;) {
		# clear error flag so that it will say more is available for reading
		my $line;
		ALOG->clearerr();
		while ($line = <ALOG>) {
			my @fields = split(' ', $line);


		        if(! defined $fields[11]) {
		        	    next;
	                  }
			my $seconds = $fields[11]/1000;
			if ($seconds > $opt_threshold) {
				my $now = time;
				if ($now < ($last_threshold_time + $opt_interval)) {
					# qualifies as an increment
					$num++;
				} else {
					# reset the counter
					$num = 1;
				}
				$last_threshold_time = $now;
				# print the occurance anyway
				print "Exceeded threshold ($num): $seconds $fields[0] $fields[3] $fields[5] $fields[6]\n";
			} elsif ($seconds > $opt_log_threshold) {
				print "Exceeded log threshold: $seconds $fields[0] $fields[3] $fields[5] $fields[6]\n";
			}
			if ($num >= $opt_num_requests) {
				do_action();
				$num = 0;
				$max_seconds = 0;
			}
		}

		# pause
		sleep 1;

		# check if crossed over midnight. If so, start reading/logging afresh
		my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime();

		if ($hour == 0 && $min == 5) {
			unless ($newLogStarted) {
				$newLogStarted = 1;
				ALOG->close();
				rotateLog();
				last;
			}
		} else {
			$newLogStarted = 0;
		}
	}
}

#
# Rotate log file
#
sub rotateLog {
	return if ($opt_foreground);

	# rotate old logs
	for (1..8) {
		my $index = 9 - $_;		# 8..1
		if (-f "$logfile.$index") {
			rename($logfile.".".$index, $logfile.".".($index+1));
		}
	}

	# close the stdout (current log)
	close(STDIN); close(STDOUT); close(STDERR);

	# rename current log
	if (-f $logfile) {
		rename($logfile, "$logfile.1");
	}

	# start the new one
	open(STDIN,  "+>/dev/null");
	open(STDOUT, ">> $logfile");
	open(STDERR, "+>&STDOUT");
	STDOUT->autoflush(1);
}

