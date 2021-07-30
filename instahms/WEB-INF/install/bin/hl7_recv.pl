#!/usr/bin/perl -w

#
# Generic HL7 receiving server. Returns an ACK to whatever message was sent by 
# the client.
#

use strict;
use IO::Socket;
use Sys::Hostname;
use Getopt::Long;
use File::Basename;

use lib dirname($0)."/../lib";

use Hl7::Message;
use Hl7::Segment;

my $opt_port = 5901;
my $opt_fork = 1;
my $client;
my $requests = 1;

GetOptions( "port=i", "fork!") or die
	"Usage: $0 [-p|-port <our port>] [-f|-fork]\n" .
	" Defaults: -port 5901 -fork\n";

# set up the server
my $server = IO::Socket::INET->new(
	Proto => 'tcp',
	LocalPort => $opt_port,
	LocalAddr => "0.0.0.0",
	Listen    => SOMAXCONN,
	Reuse     => 1);

die "Can't setup server: $!" unless $server;
print "[Server started on $opt_port]\n";
print "== server started: ", `date`, "\n";

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
	$/ = "\x0B";
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
    my $client = shift;
    $client->autoflush(1);
    printf "Accepting request ${requests}: [from %s:%s]\n", $client->peerhost, $client->peerport;

	my $msgStr = <$client>;
	my $clean = $msgStr;
	$clean =~ tr/\x0B\x0D\x1C/\n\n./;
	my $msg = new Hl7::Message($msgStr);
	print "Received on listening port: \n";
	print $clean, "\n";

	# send back the app level ack ack
	my $ack = new Hl7::Message({
			sendApp=>'Insta', sendFac=>'hl7_recv', recvApp=>'r-app', recvFac=>'r-fac',
			msgType=>'ACK', procId=>'T', ackType=>'NE', appAckType=>'NE', 
			controlId=>time});

	$ack->addSegment(new Hl7::Segment('MSA', {code=>'AA', controlId=>$msg->{MSH}{controlId},
				msgText=>'Success'}));

	print "Sending ack from listening port: \n";
	print $ack->toString(1);
	print "\n";

	print $client $ack->toString(), "\x0B";

    close $client;
    print $requests, ":", "Client closed connection\n";
}

