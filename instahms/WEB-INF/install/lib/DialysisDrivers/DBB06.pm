package DialysisDrivers::DBB06;

use strict;
use warnings;
use IO::Socket;
use IO::Handle;
use 5.010;
# no warnings 'experimental'; replace the Switch with 'given when' keywords, and don't give warning for this.

# Monitor data Mapping with their address, length and units
my %valueDesc = (
	1 => {name=>"dialysis_time",					length=>4, afterDecimal=>0, units=>"min"},
	3 => {name=>"uf_removed",					length=>4, afterDecimal=>2, units=>"L"},
	5 => {name=>"blood_pump_rate",				length=>3, afterDecimal=>0, units=>"ml/min"},
	7 => {name=>"heparin_rate",					length=>5, afterDecimal=>1, units=>"ml/hr"},
	8 => {name=>"uf_rate",					length=>3, afterDecimal=>2, units=>"L/hr"},
	9 => {name=>"venous_pressure",				length=>3, afterDecimal=>0, units=>"mmHg"},
	10 => {name=>"dialysate_pressure",				length=>3, afterDecimal=>0, units=>"mmHg"},
	11 => {name=>"tmp",						length=>3, afterDecimal=>0, units=>"mmHg"},
	15 => {name=>"dialysate_cond",				length=>3, afterDecimal=>1, units=>"mS/cm"},
	17 => {name=>"dialysate_temp",				length=>3, afterDecimal=>1, units=>"C"},
	18 => {name=>"dialysate_rate",				length=>3, afterDecimal=>0, units=>"ml/min"},	
	55 => {name=>"uf_goal",					length=>4, afterDecimal=>2, units=>"L"},
	56 => {name=>"treatment_mode",				length=>1, afterDecimal=>0, units=>""},
	57 => {name=>"subst_rate",					length=>4, afterDecimal=>2, units=>"l/hr"},
	58 => {name=>"subst_goal",					length=>4, afterDecimal=>2, units=>"l/hr"},
	59 => {name=>"subst_transit_val",				length=>4, afterDecimal=>2, units=>"L"},
);

my %unreachable = ( polled_status => 'X' );
my %error = ( polled_status => 'E' );
my %noResponse = ( polled_status => 'R' );

my %results = ();

my $count = 17;

my $datacmd = "";
my $checksum = "";
my $cmd = "";

my $sno = "";
my $dsc = "";

my $desc = "";
my $len = "";
my $ad = 0;

my $lno = "";
my $class = "";
my $code = "";

our $stx = "02";	# in hexadecimal
our $etx = "03";	# in hexadecimal

# function for getting only monitor data results only
sub monitorDataResults {
	my $self = shift;
	my ($machine) = @_;
	my $ip = $machine->{network_address};
	my $port = $machine->{network_port};
	my $name = $machine->{machine_name} . "@" . $machine->{network_address};

	my $dno = $machine->{d_no};	# in String format and need to convert it in hexadecimal
	my $dnohex = unpack('H*', $dno); # Conversion of DNO into Hexadecimal

	my @keys = sort keys %valueDesc;

	my $adr = "";
	my $num = "01";

	my $flag = 0;

	my $time = localtime;
	
	my $ecft = 0;
	print "========= Time start for monitor data : $time\n";
	while(@keys) {
		$cmd = "41";
		$adr = pop(@keys);
		$port = 1401 unless ($port);			# default value is 1401 for Nikkiso
		my $remote = IO::Socket::INET->new(
				Proto => "tcp",
				PeerAddr => $ip,
				PeerPort => $port,
				Timeout => 10
		);
		
		if($ecft >= 3){
			last;
		}		

		unless ($remote) {
			print "$name: Unable to connect to $ip:$port - $!\n";
			#return \%unreachable;
			$results{polled_status} = 'X';
			$ecft++;
			next;				
		}

		$sno = sprintf("%02X", $count);
		#print "SNO in Monitor Data : " . $sno . "\n";
		$count++;
		$dsc = $dnohex . $sno . $cmd;
		$datacmd = monitorDataInput($dsc, $adr, $num);

		my $hex = requestToServer($datacmd, $remote, $name);
		
		unless ($hex) {
			print "$name: ERROR: No response, raw data: ", $hex, "\n";
			#return ($dno, \%noResponse);
			$results{polled_status} = 'R';
			$ecft++;
			next;			
		}

		# Parsing of Response Data
		my ( $sta2ndByte, $sta1stByteBinary, @array ) = commonParseSTA($hex, $cmd, $sno );
		given ($sta2ndByte) {
			# If these occurs more than or equal to 3 then control goes outside the loop.
			when ($_ eq "01" || $_ eq "03" || $_ eq "04" || $_ eq "06" || $_ eq "07") {
				$ecft++;
			}
			# End Code : mismatch of Dialysis machine identification number
			when ($_ eq "02") {
				$dnohex = substr($hex, 2, 16);
				$dno = pack('H*', $dnohex);
				print "Correct DNO : ", pack('H*', $dnohex), "\n";
			}
			# Data Count Error, So I tried to increase the NUM value..
			when ($_ eq "05") {
				$ecft++;
				$num = sprintf("%u", $num);
				$num++;
				$num = sprintf("%02s", $num);
			}
			default { $ecft = 0; }
		}
		# SNO should be 11h to FFh.
		if($count > 255){
			$count = 17;
		}

		$flag = monitorDataOutput($sta1stByteBinary, $sta2ndByte, \@array, $adr);
		if($flag){
			last;			
		}
	}
	$time = localtime;
	print "========= Time finished for monitor data : $time\n";
	if ($results{polled_status} eq 1) {
		$results{polled_status} = 'D';
		logDataReadOutAndDeletion($ip, $port, $name, $dno);
	} elsif($results{polled_status} eq 0) {
		$results{polled_status} = 'N';
	}

	return ($dno, \%results);
}

# function for getting log data results only
sub logDataReadOutAndDeletion {
	
	#my $self = shift;
	my ($ip, $port, $name, $dno) = @_;
	#my $ip = $machine->{network_address};
	print "IP : $ip\n";
	#my $port = $machine->{network_port};
	print "PORT : $port\n";
	#my $name = $machine->{machine_name} . "@" . $machine->{network_address};
	print "NAME : $name\n";
	#my $dno = $machine->{d_no};	# in String format and need to convert it in hexadecimal
	print "DNO : $dno\n";
	my $dnohex = unpack('H*', $dno); # Conversion of DNO into Hexadecimal

	my $time = localtime;
	print "========= Time start for log data : $time\n";

	# By default alarm value set
	$results{"tmp_alarm"} = 0; # class - 83 code - All
	$results{"air_alarm"} = 0; # class - 80 code - 01/02/05/06
	$results{"blood_leak_alarm"} = 0; # class - 85 code - 00
	$results{"other_alarm"} = 0; # There are so many alarms in this category such as arterial pressure alarm, pressure loss alarm, 					#bicarbonate conductivity alarm, initial UF-factor alarm, UF-factor alarm
	$results{"bp_alarm"} = 0; # class - 98 code - 01/02
	$results{"temperature_alarm"} = 0; # class - 86, code - 01,02,03,04
	$results{"conductivity_alarm"} = 0; #class - 8A, 8B code - All
	$results{"venous_pressure_alarm"} = 0; # class - 81 code - All
	$results{"dialysate_pressure_alarm"} = 0; # class - 84 code - All

	my $evod = 0;
	my $ecft = 0;
	my $t1 = time;
	my $t2;
	FIRST: while(1) {

		$port = 1401 unless ($port);			# default value is 1401 for Nikkiso
		my $remote = IO::Socket::INET->new(
			Proto => "tcp",
			PeerAddr => $ip,
			PeerPort => $port,
			Timeout => 10
		);
		
		if($ecft >= 3){
			last FIRST;
		}

		unless ($remote) {
			print "$name: Unable to connect to $ip:$port - $!\n";
			#return \%unreachable;
			$results{polled_status} = 'X';
			$ecft++;
			next;				
		}
		# SNO should be 11h to FFh.
		if($count > 255){
			$count = 17;
		}

		$sno = sprintf("%02X", $count);
		#print "SNO in Log Data : " . $sno . "\n";
		$count++;

		if ($evod % 2 == 0) {
			$cmd = "4C";
			$dsc = $dnohex . $sno . $cmd;
			$datacmd = logDataInput($dsc);	
			my $hex = requestToServer($datacmd, $remote, $name);

			unless ($hex) {
				print "$name: ERROR: No response, raw data: ", $hex, "\n";
				#return \%noResponse;
				$results{polled_status} = 'R';
				$ecft++;
				next;
			}

			my ( $sta2ndByte, $sta1stByteBinary, @array ) = commonParseSTA($hex, $cmd, $sno );
			given($sta2ndByte){
				# If these occurs more than or equal to 3 then control goes outside the loop.
				when ($_ eq "01" || $_ eq "03" || $_ eq "06" || $_ eq "07") {
					$ecft++;
				}
				# End Code : mismatch of Dialysis machine identification number
				when ($_ eq "02") {
					$dnohex = substr($hex, 2, 16);
					$dno = pack('H*', $dnohex);
					print "Correct DNO : ", pack('H*', $dnohex), "\n";
				}
				# no log data found.
				when ($_ eq "08") {
					last FIRST;
				}
				default { $ecft = 0; }
			}
			if($ecft >= 3){
				last FIRST;
			}
			if (substr($sta1stByteBinary, 0, 1) eq 0) {
				last FIRST;
			}
			logDataOutput(\@array);
			print "Polled_status Value in Logdata read-out of sno $sno : ", $results{polled_status}, "\n";
			if ($results{polled_status} eq 1) {
				$results{polled_status} = 'D';
			} elsif($results{polled_status} eq 0) {
				$results{polled_status} = 'N';
			}
			$evod++;
		}else {
			$cmd = "5C";
			$dsc = $dnohex . $sno . $cmd;
			$datacmd = logDataDeletionInput($dsc, $lno, $class, $code);
			my $hex = requestToServer($datacmd, $remote, $name);
			
			unless ($hex) {
				print "$name: ERROR: No response, raw data: ", $hex, "\n";
				#return \%noResponse;
				$results{polled_status} = 'R';
				$ecft++;
				next;
			}

			my ( $sta2ndByte, $sta1stByteBinary, @array ) = commonParseSTA($hex, $cmd, $sno );
			given($sta2ndByte){
				# If these occurs more than or equal to 3 then control goes outside the loop.
				when ($_ eq "01" || $_ eq "03" || $_ eq "06" || $_ eq "07") {
					$ecft++;
				}
				when ($_ eq "09") {
					$ecft++;
				}
				# End Code : mismatch of Dialysis machine identification number
				when ($_ eq "02") {
					$dnohex = substr($hex, 2, 16);
					$dno = pack('H*', $dnohex);
					print "Correct DNO : ", pack('H*', $dnohex), "\n";
				}
				default { $ecft = 0; }
			}
			$evod++;
		}
		$t2 = time;
		if( ($t2 - $t1) >= 180 ){
			last FIRST;
		}
	}
	$time = localtime;
	print "========= Time end for log data : $time\n";
	print "Polled_status Value at last : ", $results{polled_status}, "\n";
	if ($results{polled_status} eq 1) {
		$results{polled_status} = 'D';
	} elsif($results{polled_status} eq 0) {
		$results{polled_status} = 'N';
	}
=pod
	# sometimes we get invalid BP time (has / in it): if we do, then ignore the BP alone
	unless ($results{bp_time} =~ /\d\d\d\d\d\d/) {
		delete $results{bp_time};
		delete $results{bp_high};
		delete $results{bp_low};
		delete $results{pulse_rate};
		print "$name: Warning: invalid values for BP, ignoring BP related values only\n";
		print "$name: Raw message: ", $msg;
	}
=cut
	if ($results{polled_status} eq 'N') {
		# make sure we nullify BP related values, or it can get reused
		# the minute we start dialyzing.
		$results{bp_time} = undef;
		$results{bp_high} = undef;
		$results{bp_low} = undef;
		$results{pulse_rate} = undef;
	}

	return \%results;

}


sub monitorDataInput {
	my( $dsc, $adr, $num ) = @_;
	$adr = sprintf("%04X", $adr);
	$num = sprintf("%04X", $num);
	# Code for Monitor data
	my $input = $dsc . $adr . $num;

	$desc = $valueDesc{hex $adr};
	$len = $desc -> {length};
	$ad = $desc -> {afterDecimal};	

	$checksum = calcChecksum( $input );
	$datacmd = $stx . $dsc . convertForInput($adr) . convertForInput($num) . convertForInput(substr(sprintf("%X",$checksum), -2)). $etx;
	return $datacmd;

}

sub logDataInput {
	my( $dsc ) = @_;
	$checksum = calcChecksum( $dsc );
	$datacmd = $stx . $dsc . convertForInput(substr(sprintf("%X",$checksum), -2)) . $etx;
	return $datacmd;
}

sub commonParseSTA {
	my( $hex, $cmd, $sno ) = @_;
	print "Hex value : $hex\n";
	my @excp = ("1012", "1013", "1010");
	my $data = $hex;
	my $fix = substr($data, 0, 22);
	$data =~ s/$fix//;
	my @array = ();
	foreach my $substr (@excp) {
		my $forChecking = "";
		my $flag = '';
		my $temp = "";
		while(length($data) > 2){
			$forChecking = substr($data, 0, 2);
			$flag = check($forChecking);
			if ($flag) {
				$temp = substr($data, 0, 4);
				push @array, $temp;
				$data =~ s/$temp//;
			}else{
				$temp = substr($data, 0, 2);
				push @array, $temp;
				$data =~ s/$temp//;
			}
		}
	}
	$stx = substr($hex, 0, 2);
	my $dno = substr($hex, 2, 16);
	$dno = pack('H*', $dno);
	$sno = substr($hex, 18, 2);
	$cmd = substr($hex, 20, 2);
	my $sta = "";
	$sta = convertForOutput("@array[0,1]");

	my $sta1stByte = substr($sta, 0, 2);
	my $sta2ndByte = substr($sta, 2, 2);

	my $sta1stByteBinary = sprintf("%08b", hex $sta1stByte);
	my @sta1stByteBinaryAr = split(//, $sta1stByteBinary);

	print "sta1stByteBinary : $sta1stByteBinary\n";

	if($sta1stByteBinaryAr[0] eq 0){ # 7
		print "No log Data\n";
	}else{
		print "Log Data available\n";
	}

	if($sta1stByteBinaryAr[1] eq 1){ # 6
		print "Status : after information processing (verification of information )\n";
	}

	if($sta1stByteBinaryAr[2] eq 1){ # 5
		print "Status : during information occurring\n";
	}

	if($sta1stByteBinaryAr[3] eq 1){ # 4
		print "Status : after alarm processing (verification of alarm)\n";
	}

	if($sta1stByteBinaryAr[4] eq 1){ # 3
		print "Status : during alarm occurring\n";
	}

	if($sta1stByteBinaryAr[5] eq 1){ # 2
		print "Status : during stand-by\n";
	}

	if($sta1stByteBinaryAr[6] eq 1){ # 1
		print "Status : during cleaning\n";
	}

	if($sta1stByteBinaryAr[7] eq 1){ # 0
		print "Status : during treatment (PC can use this for the judgment of start / stop of treatment)\n";
		$results{polled_status} = 1;
	}elsif($sta1stByteBinaryAr[7] eq 0){
		print "Status : Not treatment mode\n";
		$results{polled_status} = 0;
	}

	given($sta2ndByte) {
		when ($_ eq "00") {print "End Code : normal completion\n";}
		when ($_ eq "01") {print "End Code : CRC error\n";}
		when ($_ eq "02") {print "End Code : mismatch of Dialysis machine identification number\n";}
		when ($_ eq "03") {print "End Code : illegal command\n";}
		when ($_ eq "04") {print "End Code : address error\n";}
		when ($_ eq "05") {print "End Code : data count error\n";}
		when ($_ eq "06") {print "End Code : data abnormality\n";}
		when ($_ eq "07") {print "End Code : write failure\n";}
		when ($_ eq "08") {print "End Code : no log data\n";}
		when ($_ eq "09") {print "End Code : log data deletion error\n";}
		default {print "Invalid End Code\n";}
	}
	return ( $sta2ndByte, $sta1stByteBinary, @array );
}

sub requestToServer {
	my( $datacmd, $remote, $name ) = @_;
	print "Request of $name : $datacmd\n";
	my $dt = localtime;
	print "Start : $dt\n";
	$datacmd = pack("H*", $datacmd); # important!
	#print "$datacmd\n";
	print $remote "$datacmd";
	my @msg = <$remote>; # Response
	my $msg = join('', @msg);
	my( $hex ) = unpack( 'H*', $msg ); # Convert Response in hexadecimal for parsing
	print "Response of $name : $hex\n";
	$dt = localtime;
	print "End : $dt\n";
	$remote->close();
	return $hex;
}

sub monitorDataOutput {
	my( $sta1stByteBinary, $sta2ndByte, $arr, $adr ) = @_;
	my @array = @{ $arr };
	if ($sta2ndByte eq "00") {
		my $data = convertForOutput("@array[2,3]");	
		$data = unpack('s', pack('S', hex($data))); # It gets positive and negative decimal of hexadecimal
		my $l = length($data);
		$data = sprintf("%0".$len."d", $data);
		if ($ad > 0) {
			if ($l > $len) { # This for -ve decimal value.
				$data = substr($data, 0, ($len + 1) - $ad) . "." . substr($data, -$ad);
			}else {
				$data = substr($data, 0, $len - $ad) . "." . substr($data, -$ad);
			}
		}
		#if(substr($sta1stByteBinary, -1) eq 1) {
			$results{$desc->{name}} = $data;
		#}
		return 0;
	}else{
		return 1;
	}
}

sub logDataOutput {

	my( $arr ) = @_;
	my @array = @{ $arr };
	
	# $lno, $class, $code is used for deletion of log data and $time is required for bp_time if BP measured.
	$lno = convertForOutput($array[2]); 
	$class = convertForOutput($array[3]);
	$code = convertForOutput($array[4]);
	my $date = convertForOutput("@array[5..8]");
	my $time = convertForOutput("@array[9..11]");
	my $elaptime = convertForOutput("@array[12,13]");
	my $reldata = convertForOutput("@array[14..21]");
	my $crc = convertForOutput($array[22]);

	given($class){
		when ($_ eq "01"){
			given($code){
				when ($_ eq "01") {
					# bp_time
					$results{"bp_time"} = $time;
					print "Log Data : Blood pressure measurement = $time\n";
					# bp_high
					my $sys = unpack('s', pack('S', hex(substr($reldata, 0, 4))));
					print "Systolic blood pressure = $sys\n";
					$results{"bp_high"} = $sys;
					# bp_low
					my $dia = unpack('s', pack('S', hex(substr($reldata, 4, 4))));
					print "Diastolic blood pressure = $dia\n";
					$results{"bp_low"} = $dia;

					my $map = unpack('s', pack('S', hex(substr($reldata, 8, 4))));
					print "Map : $map\n";
					# pulse_rate				
					my $pulse = unpack('s', pack('S', hex(substr($reldata, -4))));
					print "Pulse rate = $pulse\n";
					$results{"pulse_rate"} = $pulse;
				}
				default {print "Invalid code of class $class\n"}
			}
		}
		
		when ($_ eq "80"){
			given($code){
				when ($_ eq "01" || $_ eq "02" || $_ eq "05" || $_ eq "06"){
					$results{"air_alarm"} = 1;				
				}
				default {print "Invalid code of class $class or may be not required\n";}
			}
		}

		when ($_ eq "81"){
			$results{"venous_pressure_alarm"} = 1;
			print "======= venous_pressure_alarm found for saving in DB =======\n";
		}

		when ($_ eq "82"){
			$results{"other_alarm"} = 1;	
		}

		when ($_ eq "83"){
			$results{"tmp_alarm"} = 1;
			print "======= tmp_alarm found for saving in DB =======\n";
		}	
			
		when ($_ eq "84"){
			$results{"dialysate_pressure_alarm"} = 1;
			print "======= dialysate_pressure_alarm found for saving in DB =======\n";	
		}

		when ($_ eq "85"){
			given($code){
				when ($_ eq "00") {
					$results{"blood_leak_alarm"} = 1;
					print "======= blood_leak_alarm found for saving in DB =======\n";	
				}
				default {print "Not required code : $code\n";}
			}
		}
		
		when ($_ eq "86"){
				given($code){
					when ($_ eq "01" || $_ eq "02" || $_ eq "03" || $_ eq "04") {
						$results{"temperature_alarm"} = 1;
						print "======= temperature_alarm found for saving in DB =======\n";	
					}
					default {print "Invalid code of class $class or not required\n";}
				}
			}

		when ($_ eq "90"){
			$results{"other_alarm"} = 1;
			print "======= other_alarm found for saving in DB =======\n";	
		}

		when ($_ eq "94"){
			$results{"other_alarm"} = 1;
			print "======= other_alarm found for saving in DB =======\n";	
		}
	
		when ($_ eq "8a"){
			$results{"conductivity_alarm"} = 1;
			print "======= conductivity_alarm found for saving in DB =======\n";		
		}

		when ($_ eq "8b"){
			$results{"conductivity_alarm"} = 1;
			print "======= conductivity_alarm found for saving in DB =======\n";		
		}

		when ($_ eq "98"){
			given($code){
				when ($_ eq "00" || $_ eq "01") {
					$results{"bp_alarm"} = 1;
					print "======= bp_alarm found for saving in DB =======\n";
				}
				default {print "Not required code : $code\n";}
			}
		}
		#default { print "Not part of Insta hms codes\n";}
	}

}


sub convertForInput {

	my ( $value ) = @_;

	if ($value eq '0003') {
		return '001013';
	}elsif ($value eq '0002') {
		return '001012';
	}elsif ($value eq '0010') {
		return '001010';
	}elsif ($value eq '10') {
		return '1010';
	}elsif ($value eq '03') {
		return '1013';
	}elsif ($value eq '02') {
		return '1012';
	}else {
		return $value;
	}
}

sub calcChecksum {
	# Checksum calculation
	my ( $input ) = @_;
	my @data = split(//, $input);	
	my $checksum = 0;
	while (scalar(@data) > 0){
		$checksum += hex join('', splice(@data, 0, 2));
	}
	return $checksum;
}

sub logDataDeletionInput {
	my($dsc, $lno, $class, $code) = @_;
	
	my $input = $dsc . $lno . $class . $code;
	$checksum = calcChecksum( $input );
	$datacmd = $stx . $dsc . convertForInput($lno) . convertForInput($class) . convertForInput($code) . convertForInput(substr(sprintf("%X",$checksum), -2)) . $etx;
	return $datacmd;
}

sub convertForOutput {
	my ( $value ) = @_;
	my @array = split(/ /, $value);
	my $num = @array;
	my $temp ="";
	foreach my $value1 (@array){
		if ($value1 eq '1013') {
			$temp .= '03';
		}elsif ($value1 eq '1012') {
			$temp .= '02';
		}elsif ($value1 eq '1010') {
			$temp .= '10';
		}else {
			$temp .= $value1;
		}
	}
	return $temp;
}

sub check {
	my( $rec ) = @_;
	if ($rec eq '10') {
		return 1;
	} else {
		return 0;	
	}
}

1;
