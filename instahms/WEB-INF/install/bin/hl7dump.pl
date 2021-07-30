#!/usr/bin/perl -w

#
# Simple script to dumpt the contents of a HL7 message (as a file)
# in a readable format to stdout.
# (Use Insta Hl7 perl modules)
#

use strict;
use Hl7::Message;
use Hl7::Segment;

$/ = undef;
my $messageStr = <>;

my $msg = Hl7::Message->new($messageStr);

$msg->dump();
print $msg->toString(1);

