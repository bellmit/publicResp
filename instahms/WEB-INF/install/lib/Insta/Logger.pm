package Insta::Logger;

use strict;
use warnings;
use POSIX qw(strftime);
use File::Basename;
use lib dirname($0)."/../lib";

#
# Functions that are used to process a single HL7 lab import. The message can be
# received in any fashion (which is handled by the caller.) Once we have a message,
# the process() function will import the results into the Insta tables.
#

##################################################
sub new {
##################################################

	my $class = shift;
	my $params = shift;

	my $util = Insta::Util->new();

	if(!defined $params->{'level'}) {
		$params->{'level'} = $util->getProperty('log.level');	
	}

	if(!defined $params->{'base_name'}) {
		$params->{'base_name'} = basename($0);	
	}

	my $self = {};
	foreach my $key (keys(%$params)) {
		$self->{$key} = $params->{$key};
	}
	
	# Adding Util object into a logger object to access any where inside logger method
	$self->{'util'} = $util;

	bless $self, $class;
	
	return $self;
}

##################################################
sub debug {
##################################################

	my $self = shift;
	my ($msg) = @_;
	$self->logWrite(4, $msg);
}

##################################################
sub info {
##################################################

	my $self = shift;
	my ($msg) = @_;
	$self->logWrite(3, $msg);
}

##################################################
sub warn {
##################################################

	my $self = shift;
	my ($msg) = @_;
	$self->logWrite(2, $msg);
}
##################################################
sub error {
##################################################

	my $self = shift;
	my ($msg) = @_;
	$self->logWrite(1, $msg);
}

##################################################
sub fatal {
##################################################

	my $self = shift;
	my ($msg) = @_;
	$self->logWrite(0, $msg);
}

##################################################
sub logWrite {
##################################################

	my $self = shift;
	
	my ($lv, $msg) = @_;
	
	my $base_name ="";
	if(defined $self->{base_name}){
		$base_name = '['.$self->{base_name}.']';
	}
	
	my %levels = (0 => 'FATAL', 1 => 'ERROR', 2 => 'WARNING', 3=> 'INFO', 4 => 'DEBUG');
	
	if ($lv <= $self->{level}) {
		print getLogDateTime() .$base_name . " $levels{$lv} :: $msg\n";
	}
	
}


	#Rotate log file if size is greater than specified size.
##################################################
sub rotateLog {
##################################################

	my $self = shift;
	my $maxSize = shift;
	my $fileSize = 0;
	
	my $logfile =  $self->getLogFile();

	return if ($self->{'isConsilePrint'});

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

##################################################
sub getLogFile {
##################################################
	
	my $self = shift;
	my $logFolder = $self->{'util'}->getProperty('log.folder.location');
	my $logDB = (defined $self->{'db'})?$self->{'db'}.'/':'hms/';
	my $logSchema = (defined $self->{'schema'})?$self->{'schema'}:'';
	my $logFile = (defined $self->{'base_name'})?$self->{'base_name'}:'hl7';	
	##Removing Extention
	$logFile =~ s{\.[^.]+$}{};
	$logFile = $logFile . '_'.$logSchema.'.log';

	return $logFolder.$logDB.$logFile;
}
##################################################
sub getLogDateTime {

	return strftime('%Y-%m-%d %H:%M:%S',localtime).' ';
}


1;

