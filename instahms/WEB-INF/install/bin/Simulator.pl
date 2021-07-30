#!/usr/bin/perl -w

use strict;
use IO::Socket;
use Sys::Hostname;
use Getopt::Long;
our $opt_port = 1402; # static port, we can change port
our $opt_fork = 1;
GetOptions( "port=i", "fork!") or die
	"Usage: $0 [-p|-port <our port>] [-f|--fork|--nofork]\n" .
	" Defaults: -port 1401 -fork\n";

my $client;
my $requests = 0;

# set up the server
my $server = IO::Socket::INET->new(
	Proto => 'tcp',
	LocalPort => $opt_port,
	LocalAddr => "127.0.0.1",
	Listen    => SOMAXCONN,
	Reuse     => 1);

die "Can't setup server: $!" unless $server;
print "[Server started on $opt_port]\n";

my %fieldDesc = (
	"Dialysis Time"=>				{code=>1, length=>5, units=>"min"},
	"UF Volume"=> 					{code=>3, length=>5, units=>"L"},
	"Blood Flow Rate"=> 				{code=>5, length=>5, units=>"ml/min"},
	"Infusion Pump Rate"=> 				{code=>7, length=>5, units=>"ml/hr"},
	"UF Rate"=> 					{code=>8, length=>5, units=>"L/hr"},
	"Venous Pressure"=> 				{code=>9, length=>5, units=>"mmHg"},
	"Dialysate Pressure"=> 				{code=>10, length=>5, units=>"mmHg"},
	"TMP"=> 					{code=>11, length=>5, units=>"mmHg"},
	"Dialysate Conductivity"=>			{code=>15, length=>5, units=>"mS/cm"},
	"Dialysate Temperature"=> 			{code=>17, length=>5, units=>"C"},
	"Dialysate Flow Rate"=> 			{code=>18, length=>5, units=>"ml/min"},
	"UF Goal"=> 					{code=>55, length=>5, units=>"L"},
	"Treatment Mode"=> 				{code=>56, length=>1, units=>""},
	"Substitution Rate"=>				{code=>57, length=>5, units=>"l/hr"},
	"Substitution Goal"=>				{code=>58, length=>5, units=>"l/hr"},
	"Substitution Transit Value"=>			{code=>59, length=>5, units=>"L"},
	"STATUS"=>					{code=>0, length=>4, units=>""},
);

# close when user presses ctrl-C
local $SIG{INT} = sub {
	print "Exiting ...\n";
	close $client if defined($client);
	close $server;
	exit;
};

$SIG{CHLD} = 'IGNORE';

while ($client = $server->accept()) {
	# fork a child to respond to this request
	if ($opt_fork) {
		unless (fork()) {
			my $reply = process($client);
			print $client $reply;
			exit;
		}
	} else {
		my $reply = process($client);
		print $client $reply;
	}
	close $client;
	$requests++;
}

sub process {
	my $requestor = shift;
	$requestor->autoflush(1);
	printf "Accepting ${requests}:[from %s:%s]\n", $requestor->peerhost, $requestor->peerport;
	$requestor->recv(my $received, 1024); # receiving data in pack format of hexadecimal request
	$received = unpack("H*", $received); # unpack the request in Hexadecimal
	
	print $requests, " says: ", $received, "\n";
	print "Replying ... $requests\n";
	my $reply = getReply($received);
	print "reply is: ", $reply, "\n";
	$reply = pack("H*", $reply); # pack the response before replying
	print $requests, ":", "Client closed connection\n";
	return $reply;
}

# STX DNO SNO CMD ADR NUM CRC ETX (Monitor Data request example)
# 02 5037303036323032 11 41 0009 0001 0D 03

# STX DNO SNO CMD STA RES-DATA CRC ETX (Monitor Data response example)
# 02 5037303036323032 11 41 0100 00a8 ac 03

# STX DNO SNO CMD CRC ETX (Log Data request example)
# 02 5037303036323032 31 4C 2E 03

# STX DNO SNO CMD STA LNO CLASS CODE DATE TIME ET DATA CRC ETX (Log Data response example) 
# 02 5037303036323032 31 4c 8100 44 f4 67 20170106 131501 000c 0000000000000000 c1 03
sub getReply{
	sleep 10;
	my ($getValue) = @_;
	#print "getValue = $getValue\n";
	# 0250373030363230321141000900010D03
	my $stx = substr($getValue, 0, 2);
	#print "stx : $stx\n";
	my $dno = substr($getValue, 2, 16);
	#print "DNo : $dno\n";
	my $sno = substr($getValue, 18, 2);
	#print "sno : $sno\n";
	my $cmd = substr($getValue, 20, 2);
	#print "cmd : $cmd\n";

	my $sta = ""; 
	my $data = '';
	my $returnValue = '';
	my $checksum = '';

	# For Monitor Data
	if ($cmd eq "41"){
		my $v = substr($getValue, 22, 4);
		my $adr = "";
		if(check($v)){
			$adr = substr($getValue, 22, 6);
			if($adr eq "001013"){$adr = '3';}
			else{$adr = '10';}
			#print "adr = $adr\n";
			my $num = hex substr($getValue, 28, 4);
			#print "num = $num\n";
		}else{
			$adr = hex substr($getValue, 22, 4);
			#print "adr = $adr\n";
			my $num = hex substr($getValue, 26, 4);
			#print "num = $num\n";
		}
		

		# Reading monitor data from $opt_port.msg file as dummy
		open MSG, "$opt_port.msg";
		my $param;
		while ($param = <MSG>) {
			chomp($param);
			next if ($param eq '' || $param =~ /^#/);
			my ($field, $value) = split('=', $param);
			$value =~ s/ //g;
			$value =~ s/\.//g;
			my $code = $fieldDesc{$field}->{code};
			if (defined $code && $code eq $adr){
				#print "value = $value\n";
				$data = substr(sprintf("%04x", $value), -4);
				#print "$adr === $data\n";	
			}elsif(defined $code && $code eq 0){
				$sta = $value;
			}
		}
		#print "STA = $sta\n";
		$returnValue = $stx . $dno . $sno . $cmd . $sta . $data;
	# for Log Data readout
	}elsif ($cmd eq "4C" || $cmd eq "4c"){
		open my $file, '<', "logdata.msg"; # Reading logdata one line at a time from logdata.msg file, and attach it from final result
		my $firstLine = <$file>;
		# $firstLine = 810026460120170106131116000a0000000000000000;
		# 810026460120170106131116000a0000000000000000 = 8100(STA) 26(LNO) 46(Class) 01(Code) 20170106(Date) 131116(Time) 000a(Elapse 
		# time) 0000000000000000(Related Data)
		chomp($firstLine);
		close $file;
		$returnValue = $stx . $dno . $sno . $cmd . $firstLine;
	# For Log Data Deletion
	}elsif ($cmd eq "5C" || $cmd eq "5c"){
		# input - 02 5037303036323032 22 5C 21 f4 12 56 03
		# output - 02 5037303036323032 22 5c 0100 30 03
		open MSG, "logdata.msg"; # Reading logdata from logdata.msg file, and delete that line from file.
		my @bar = <MSG>;
		close MSG;
		my $param = shift @bar;
		# For STA process start
		my $temp = substr($param, 0, 2);
		$temp = sprintf("%08b", hex $temp);
		my @temp = split(//, $temp);
		shift @temp;
		$temp = join('', @temp);
		$temp = bin2dec($temp);
		$temp = sprintf("%02x", $temp);
		$sta = $temp . substr($param, 2, 2);
		#print "final sta = $sta\n";
		#For STA process end
		unlink "logdata.msg";
		my $filename = "logdata.msg";
		open(my $fh, '>>', $filename) or die "Could not open file '$filename' $!";
		while(@bar){
			my $data = shift @bar;
			print $fh $data;
		}
		close $fh;
		$returnValue = $stx . $dno . $sno . $cmd . $sta;
	}
	$checksum = calcChecksum($returnValue);
	#print "Checksum = $checksum\n";
	$returnValue .= $checksum . "03";
	return $returnValue;
}

sub calcChecksum {
	# Checksum calculation
	my ( $input ) = @_;
	my @data = split(//, $input);	
	my $checksum = 0;
	while (scalar(@data) > 0){
		$checksum += hex join('', splice(@data, 0, 2));
	}
	#print "Before Hex checksum = $checksum\n";
	$checksum = substr(sprintf("%X",$checksum), -2);
	return $checksum;
}

sub check {
	my( $rec ) = @_;
	if ($rec eq '0010') {
		return 1;
	} else {
		return 0;	
	}
}



sub bin2dec {
    return unpack("N", pack("B32", substr("0" x 32 . shift, -32)));
}


