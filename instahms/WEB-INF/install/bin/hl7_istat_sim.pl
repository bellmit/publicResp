#!/usr/bin/perl -w

#
# Simulates an i-stat device, sends a message and prints response
# from the Insta server. Also opens up a port for listening for the "App"
# level ack and prints transactions on that port as well.
#

use strict;
use File::Basename;
use IO::Socket;
use Getopt::Long;

use lib dirname($0)."/../lib";

use Hl7::Message;
use Hl7::Segment;

our $opt_port = 5900;
our $opt_host = 'localhost';
our $opt_listen = 5999;

sub usage {
	return "Usage: $0 [OPTIONS] <file>\n" .
	" OPTIONS: \n" .
	"  -h|--host <send to host>: (localhost)\n" .
	"  -p|--port <send to port>: (5900)\n" .
	"  -l|--listen <listen on port>: (5999)\n" ;
}

GetOptions( "listen=i", "host=s", "port=i") or die usage();

my $sendfile = $ARGV[0];
unless (defined($sendfile)) {
	print "Missing file\n";
	die usage();
}

open (F, $sendfile) or die "Couldn't open $sendfile: $!";
$/ = undef;			# slurp mode
my $msgStr = <F>;

my $gAckControlId = 12345;
my $serverPid;

startServer();
sendMessage();
sleep 10;
stopServer();
exit;

sub sendMessage {
	my $conn = IO::Socket::INET->new (
		Proto    => "tcp",
		PeerAddr => $opt_host,
		PeerPort => $opt_port,
		timeout  => 10
	);

	unless ($conn) {
		stopServer();
		die "Unable to connect to host $opt_host at port $opt_port";
	}

	my $msg = new Hl7::Message($msgStr);
	print "Sending message: \n";
	$msg->toString(1);

	print $conn "\x0B", $msg->toString(), "\x1C\x0D";
	$conn->flush();

	print "Waiting for Commit Ack: \n";
	$/ = "\x1C\x0D";
	my $resp = <$conn>;
	$resp =~ tr/\x0B\x0D\x1C/\n\n./;
	print "Recd Commit Ack: \n";
	print $resp;
}

sub startServer {
	# need to fork and create a listening socket
	if ($serverPid = fork()) {
		# parent process: just return
		return;
	}

	# child process: start the server
	my $server = IO::Socket::INET->new(
		Proto => 'tcp',
		LocalPort => $opt_listen,
		LocalAddr => "0.0.0.0",
		Listen    => SOMAXCONN,
		Reuse     => 1);

	$/ = "\x1C\x0D";
	while (my $client = $server->accept()) {
		my $msgStr = <$client>;
		my $clean = $msgStr;
		$clean =~ tr/\x0B\x0D\x1C/\n\n./;
		my $msg = new Hl7::Message($msgStr);
		print "Received on listening port: \n";
		print $clean, "\n";

		# send back the app level ack ack
		my $ack = new Hl7::Message({
				sendApp=>'Insta', sendFac=>'Diag', sendApp=>'i-STAT', sendFac=>'i-STAT',
				msgType=>'ACK', procId=>'T', ackType=>'NE', appAckType=>'NE', 
				controlId=>time});

		$ack->addSegment(new Hl7::Segment('MSA', {code=>'CA', controlId=>$msg->{MSH}{controlId}}));
		print $client "\x0B", $ack->toString(), "\x1C\x0D";
		print "Sending ack from listening port: \n";
		print $ack->toString(1);
	}
}

sub stopServer {
	if ($serverPid) {
		kill 'TERM', $serverPid;
	}
}

