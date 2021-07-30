#!/usr/bin/perl

package SCM::Log;

#
# Rotate log file if size is greater than specified size.
#

my $logfile = "/var/log/insta/scm/scm_sql.log";

sub rotateLog {
	my $maxSize = shift;
	my $fileSize = 0;

	return if ($opt_foreground);

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