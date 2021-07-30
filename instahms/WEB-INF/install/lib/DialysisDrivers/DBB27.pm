package DialysisDrivers::DBB27;

use strict;
use warnings;
use IO::Socket;
use IO::Handle;

my %valueDesc = (
	A => {name=>"uf_goal",						length=>5, validate=>"numeric", units=>"L"},
	B => {name=>"uf_removed",					length=>5, validate=>"numeric", units=>"L"},
	C => {name=>"uf_rate",						length=>5, validate=>"numeric", units=>"L/hr"},
	D => {name=>"blood_pump_rate",				length=>5, validate=>"integer", units=>"ml/min"},
	E => {name=>"heparin_rate",					length=>5, validate=>"numeric", units=>"ml/hr"},
	F => {name=>"dialysate_temp",				length=>5, validate=>"numeric", units=>"C"},
	G => {name=>"dialysate_cond",				length=>5, validate=>"numeric", units=>"mS/cm"},
	H => {name=>"venous_pressure",				length=>5, validate=>"integer", units=>"mmHg"},
	I => {name=>"dialysate_pressure",			length=>5, validate=>"integer", units=>"mmHg"},
	J => {name=>"tmp",							length=>5, validate=>"integer", units=>"mmHg"},
	K => {name=>"dialysis_time",				length=>5, validate=>"integer", units=>"min"},
	L => {name=>"dialysate_rate",				length=>5, validate=>"integer", units=>"ml/min"},
	M => {name=>"polled_status",				length=>1, units=>""},
	N => {name=>"treatment_mode",				length=>1, units=>""},
	O => {name=>"subst_goal",					length=>5, validate=>"numeric", units=>"L"},
	P => {name=>"subst_transit_val",			length=>5, validate=>"numeric", units=>"L"},
	Q => {name=>"subst_rate",					length=>5, validate=>"numeric", units=>"L/hr"},
	R => {name=>"subst_temp",					length=>5, validate=>"numeric", units=>"C"},
	S => {name=>"bp_time",						length=>6, validate=>"integer", units=>""},
	T => {name=>"bp_high",						length=>5, validate=>"integer", units=>"mmHg"},
	U => {name=>"bp_low",						length=>5, validate=>"integer", units=>"mmHg"},
	V => {name=>"pulse_rate",					length=>5, validate=>"integer", units=>"bpm"},

	a => {name=>"temperature_alarm",			length=>1, units=>"!"},
	b => {name=>"conductivity_alarm",			length=>1, units=>"!"},
	c => {name=>"venous_pressure_alarm",		length=>1, units=>"!"},
	d => {name=>"dialysate_pressure_alarm",		length=>1, units=>"!"},
	e => {name=>"tmp_alarm",					length=>1, units=>"!"},
	f => {name=>"air_alarm",					length=>1, units=>"!"},
	g => {name=>"blood_leak_alarm",				length=>1, units=>"!"},
	h => {name=>"other_alarm",					length=>1, units=>"!"},
	i => {name=>"bp_alarm",						length=>1, units=>"!"},
);

my %unreachable = ( polled_status => 'X' );
my %error = ( polled_status => 'E' );
my %noResponse = ( polled_status => 'R' );

sub poll {
	my $self = shift;
	my ($machine) = @_;
	my $ip = $machine->{network_address};
	my $port = $machine->{network_port};
	my $name = $machine->{machine_name} . "@" . $machine->{network_address};

	my %results = ();

	$port = 1401 unless ($port);			# default value is 1401 for Nikkiso
	my $remote = IO::Socket::INET->new(
			Proto => "tcp",
			PeerAddr => $ip,
			PeerPort => $port,
			Timeout => 10
	);

	unless ($remote) {
		print "$name: Unable to connect to $ip:$port - $!\n";
		return \%unreachable;
	}

	#my $nowString = localtime;
	#print "$nowString: Fetching data\n";

	print $remote "K\r\n";
	my $msg = <$remote>;
	$remote->close();

	unless ($msg) {
		print "$name: ERROR: No response, raw data: ", $msg, "\n";
		return \%noResponse;
	}
 
	# split the message into chars, since we will process it char by char
	my @chars = split(//,$msg);

	my $cksum = 0;

	my @stx = splice(@chars, 0, 2);		# K2
	unless ($stx[0] eq 'K' && $stx[1] eq '2') {
		print "$name: ERROR: Signature not found, raw data: ", $msg, "\n";
		return \%error ;
	}

	$cksum += ord($_) foreach (@stx);
	my @len = splice(@chars, 0, 3);		# length of message
	$cksum += ord($_) foreach (@len);

	my $len = join('', @len);

	while ($len) {

		my $code = shift(@chars);		# code of field, eg, 'A'
		$len--;
		$cksum += ord($code);

		my $desc = $valueDesc{$code};

		my @value = splice (@chars, 0, $desc->{length});		# get $desc->{length} characters
		$len -= $desc->{length};
		$cksum += ord($_) foreach (@value);

		my $value = join('', @value);							# join the characters to get the value

		#print "[$code] ", $desc->{name}, ": ", $value, " ", $desc->{units}, "\n";
		$results{$desc->{name}} = $value;						# save it in the results
	}

	# what's left is the checksum: 2 chars and CR and LF
	my $check = join('',@chars[0..1]);
	chomp ($check);
	my $ckstring = sprintf("%x", $cksum);

	if (substr($ckstring, -2) ne $check) {
		print "$name: ERROR: checksum does not match. Expecting ", substr($ckstring,-2), ", got $check\n";
		# foreach (split(//,$check)) {
		# print " ", ord($_), "\n";
		# }
		print "$name: Raw message: ", $msg;
		return \%error;

	} else {
		if ($results{polled_status} == 1) {
			$results{polled_status} = 'D';
		} else {
			$results{polled_status} = 'N';
		}

		# sometimes we get invalid BP time (has / in it): if we do, then ignore the BP alone
		unless ($results{bp_time} =~ /\d\d\d\d\d\d/) {
			delete $results{bp_time};
			delete $results{bp_high};
			delete $results{bp_low};
			delete $results{pulse_rate};
			print "$name: Warning: invalid values for BP, ignoring BP related values only\n";
			print "$name: Raw message: ", $msg;
		}

		if ($results{polled_status} eq 'N') {
			# make sure we nullify BP related values, or it can get reused
			# the minute we start dialyzing.
			$results{bp_time} = undef;
			$results{bp_high} = undef;
			$results{bp_low} = undef;
			$results{pulse_rate} = undef;
		}

		# validate the other results: if any are invalid, reject the entire record.
		for my $descKey (keys %valueDesc) {
			my $resultKey = $valueDesc{$descKey}->{name};
			if ($valueDesc{$descKey}->{validate} && $results{$resultKey}) {
				if ($valueDesc{$descKey}->{validate} eq 'integer') {
					unless ($results{$resultKey} =~ /^ *-*\d*$/) {
						print "$name: Ignoring results due to validation error",
							$resultKey, ": ", $results{$resultKey}, "\n";
						print "$name: Raw message: ", $msg;
						return \%error;
					}
				}
				if ($valueDesc{$descKey}->{validate} eq 'numeric') {
					unless ($results{$resultKey} =~ /^ *-*\d*\.\d*$/) {
						print "$name: Ignoring results due to validation error",
							$resultKey, ": ", $results{$resultKey}, "\n";
						print "$name: Raw message: ", $msg;
						return \%error;
					}
				}
			}
		}
	}

	return \%results;
}

1;

