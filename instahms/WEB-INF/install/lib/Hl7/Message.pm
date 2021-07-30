package Hl7::Message;

use strict;
use warnings;
use Hl7::Segment;
use POSIX qw(strftime);

our $obxCount=0;

sub new {
	my $class = shift;
	my $self = {};
	bless $self, $class;

	my $arg = shift;
	if (!ref($arg)) {
		$self->parse($arg);
	} elsif (ref($arg) eq 'HASH') {
		$self->init($arg);
	}

	return $self;
}

#
# init: to be used when constructing a fresh message from scratch
# args: hashref of MSH fields.
# eg: Hl7::Message->init({fieldSep=>'|', ECHARS=>'^~\\&'});
#
sub init {
	my ($self, $mshFields) = @_;

	$self->{_SEGS} = [];

	#
	# set some defaults in the MSH if not supplied
	#
	$mshFields->{fieldSep} = '|' unless($mshFields->{fieldSep});
	$mshFields->{encoding} = '^~\\&' unless($mshFields->{encoding});
	$mshFields->{msgTimest} = strftime("%Y%m%d%H%M%S", localtime) unless ($mshFields->{msgTimest});
	$mshFields->{ver} = '2.4';

	#
	# parse and save the encoding parameters
	#
	$self->{_FIELDSEP} = $mshFields->{fieldSep};
	$self->{_ENCODING} = $mshFields->{encoding};
	$self->parseEncoding();

	#
	# create the MSH segment and add it
	#
	my $mshSeg = $self->newSegment('MSH', $mshFields);
	$self->addSegment($mshSeg);
}

sub parseEncoding {
	my $self = shift;
	my @encoding = split('',$self->{_ENCODING});

	$self->{_COMPSEP} = $encoding[0] || '^';
	$self->{_REPEATSEP} = $encoding[1] || '~';
	$self->{_ESCCHAR} = $encoding[2] || '\\';
	$self->{_SUBCOMPSEP} = $encoding[3] || '&';
}

#
# Create a new segment belonging to this message, but don't add it
#
sub newSegment {
	my $self = shift;
	my ($type, $fields) = @_;

	return Hl7::Segment->new($type, $fields, $self);
}

sub parse {
	my ($self, $msgStr) = @_;

	my @segmentStrs = split /\x0A|\x0D/, $msgStr;
	my $mshStr = shift @segmentStrs;

	#
	# remove any non-printable chars in the message header. Typically, these
	# are message separators which get prepended to the MSH.
	#
	$mshStr =~ s/[\x00-\x1F]//g;

	#
	# parse the separator values from the MSH
	#
	$mshStr =~ /^MSH(.)(.{1,4})\1/ || die "Could not parse MSH";

	$self->{_FIELDSEP} = $1;
	$self->{_ENCODING} = $2;
	$self->parseEncoding();

	#
	# parse and add the MSH segment to self, set some defaults
	#
	my $mshSeg = Hl7::Segment->new('', $mshStr, $self);
	$mshSeg->{MSG_TIMEST} = strftime("%Y%m%d%H%M%S", localtime);

	$self->{MSH} = $mshSeg;
	$self->addSegment($mshSeg);

	#
	# Add all the other segments
	#
	foreach my $segStr (@segmentStrs) {
		$self->addSegment(Hl7::Segment->new('', $segStr, $self));
	}
}

sub addSegment {
	my $self = shift;
	my ($seg) = @_;
	return unless($seg);

	# ensure seg refers back to us, the msg
	$seg->{_MSG} = $self;

	# add it to our list of segs
	push(@{$self->{_SEGS}}, $seg);

	my $type = $seg->{_TYPE};
	
	if ($type eq 'OBX') {
		$obxCount++;
	}

	#
	# Add it to our hash for direct reference to each segment by type.
	# If multiple segments of same type exist, only the first is stored.
	# Eg, the MSH segment of a message can be accessed like $msg->{MSH}
	#
	my $existingSeg = $self->{$type};
	unless ($existingSeg) {
		$self->{$type} = $seg;
	}

	#
	# Add it to another hash as an array, even if only one segment exists. Can be accessed like
	#   foreach my $seg (@{$msg->{PIDs})
	# to iterate through all the PID segments
	#
	my $types = '';
	if ($type eq 'NTE' && $obxCount > 0) {
		$types = $type . $obxCount;
	} else {
		$types = $type . "s";
	}
	my $existingSegList = $self->{$types};
	unless ($existingSegList) {
		$existingSegList = [];
		$self->{$types} = $existingSegList;
	}
	push @$existingSegList, $seg;

	return $seg;
}

#
# Removes the Nth segment from the message. Index is 0 based,
# and includes the message header.
#
sub removeSegment {
	my $self = shift;
	my ($segIndex) = @_;
	return unless($segIndex);

	splice(@{$self->{_SEGS}}, $segIndex, 1);
}

sub toString {
	my $self = shift;
	my ($pretty) = @_;

	my $out;
	foreach my $seg (@{$self->{_SEGS}}) {
		$out .= $seg->toString();
		if ($pretty) {
			$out .= "\r\n";
		} else {
			$out .= "\r";
		}
	}
	return $out;
}

sub dump {
	my $self = shift;
	my $fh = shift;

	foreach my $seg (@{$self->{_SEGS}}) {
		$seg->dump($fh);
		if (defined $fh) {
			print $fh "======================\n";
		} else {
			print "======================\n";
		}
	}
}

1;

