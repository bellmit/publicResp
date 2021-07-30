#!/usr/bin/perl -w

use strict;
use IO::Socket;
use Sys::Hostname;
use Getopt::Long;

our $opt_port = 1401;
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
	LocalAddr => "0.0.0.0",
	Listen    => SOMAXCONN,
	Reuse     => 1);

die "Can't setup server: $!" unless $server;
print "[Server started on $opt_port]\n";

my %fieldDesc = (
	"UF Goal"=> 					{code=>"A", length=>5, units=>"L"},
	"UF Removed"=> 					{code=>"B", length=>5, units=>"L"},
	"UF Rate"=> 					{code=>"C", length=>5, units=>"L/hr"},
	"Blood pump Flow Rate"=> 		{code=>"D", length=>5, units=>"ml/min"},
	"Heparin pump Infusion Rate"=> 	{code=>"E", length=>5, units=>"ml/hr"},
	"Dialysate Temperature"=> 		{code=>"F", length=>5, units=>"C"},
	"Dialysate Conductivity"=> 		{code=>"G", length=>5, units=>"mS/cm"},
	"Venous Pressure"=> 			{code=>"H", length=>5, units=>"mmHg"},
	"Dialysate Pressure"=> 			{code=>"I", length=>5, units=>"mmHg"},
	"TMP"=> 						{code=>"J", length=>5, units=>"mmHg"},
	"Dialysis Time"=> 				{code=>"K", length=>5, units=>"min"},
	"Dialysate Flow Rate"=> 		{code=>"L", length=>5, units=>"ml/min"},
	"Under-treatment Flag"=> 		{code=>"M", length=>1, units=>""},
	"Treatment Mode"=> 				{code=>"N", length=>1, units=>""},
	"Substitution Goal"=> 			{code=>"O", length=>5, units=>"L"},
	"Substitution Transit Value"=> 	{code=>"P", length=>5, units=>"L"},
	"Substitution Rate"=> 			{code=>"Q", length=>5, units=>"L/hr"},
	"Substitution Temperature"=> 	{code=>"R", length=>5, units=>"C"},
	"Time of BP Measurement"=>	 	{code=>"S", length=>6, units=>""},
	"Systolic BP"=> 				{code=>"T", length=>5, units=>"mmHg"},
	"Diastolic BP"=> 				{code=>"U", length=>5, units=>"mmHg"},
	"Pulse"=> 						{code=>"V", length=>5, units=>"bpm"},

	"Temperature alarm"=> 			{code=>"a", length=>1, units=>"!"},
	"Conductivity alarm"=> 			{code=>"b", length=>1, units=>"!"},
	"Venous Pressure alarm"=> 		{code=>"c", length=>1, units=>"!"},
	"Dialysate Pressure alarm"=>	{code=>"d", length=>1, units=>"!"},
	"TMP alarm"=> 					{code=>"e", length=>1, units=>"!"},
	"Air alarm"=> 					{code=>"f", length=>1, units=>"!"},
	"Blood Leak alarm"=> 			{code=>"g", length=>1, units=>"!"},
	"Other alarms"=> 				{code=>"h", length=>1, units=>"!"},
	"BP alarm"=> 					{code=>"i", length=>1, units=>"!"},
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
            process($client);
            exit;
        }
    } else {
        process($client);
    }
	$requests++;
}

sub process {
    my $requestor = shift;
    $requestor->autoflush(1);
    printf "Accepting ${requests}:[from %s:%s]\n", $requestor->peerhost, $requestor->peerport;

    while (my $received = <$requestor>) {
		print $requests, " says: ", $received;
		print "Replying ... $requests\n";
		my $reply = getReply();
		print "reply is: ", $reply;
		print $requestor $reply;
	}

    close $requestor;
    print $requests, ":", "Client closed connection\n";
}

sub getReply {
	open MSG, "$opt_port.msg";
	my $param;
	my $totalLength = 0;
	my $data = '';
	while ($param = <MSG>) {
		chomp($param);
		next if ($param eq '' || $param =~ /^#/);
		my ($field, $value) = split('=', $param);
		$value =~ s/ //g;
		my $code = $fieldDesc{$field}->{code};
		my $length = $fieldDesc{$field}->{length};
		$data .=  $code . sprintf("%".$length."s", $value);
	}

	# prepend ID, version, length
	$data = "K2" . length($data). $data;

	# get checksum
	my @chars = split(//,$data);
	my $checksum = 0;
	foreach my $char (@chars) {
		$checksum += ord($char);
	}

	return $data.substr(sprintf("%x",$checksum), -2)."\r\n";

}

