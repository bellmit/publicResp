#!/usr/bin/perl -w

#
# Sends an HL7 message to a server and prints the ack if any from the server.
# The HL7 message is read from a file.
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

sub usage {
	return "Usage: $0 [OPTIONS] <file>\n" .
	" OPTIONS: \n" .
	"  -h|--host <send to host>: (localhost)\n" .
	"  -p|--port <send to port>: (5900)\n" ;
}

GetOptions("host=s", "port=i") or die usage();

my $sendfile = $ARGV[0];
unless (defined($sendfile)) {
	print "Missing file\n";
	die usage();
}

open (F, $sendfile) or die "Couldn't open $sendfile: $!";
$/ = undef;			# slurp mode
my $msgStr = <F>;

my $conn = IO::Socket::INET->new (
	Proto    => "tcp",
	PeerAddr => $opt_host,
	PeerPort => $opt_port,
	timeout  => 10
);

unless ($conn) {
	die "Unable to connect to host $opt_host at port $opt_port";
}

my $msg = new Hl7::Message($msgStr);
print "Sending message: \n";
$msg->toString(1);

print $conn "\x0B", $msg->toString(), "\x1C\x0D";
$conn->flush();

print "Waiting for Ack: \n";
$/ = "\x1C\x0D";
my $resp = <$conn>;
$resp =~ tr/\x0B\x0D\x1C/\n\n./;
print "Recd Commit Ack: \n";
print $resp;


