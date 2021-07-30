#!/usr/bin/perl
package SCM::Email;
use Net::SMTP;
use strict;
use warnings;

my $host        = 'mail.nmc.ae';
my $port        = 2525;
my $user        = 'ERP1@nmc.ae';
my $password    = 'ERP1$M$2014';

my $from = 'ERP1@nmc.ae';
my $to="roopesh.k\@nmc.ae,nagraj.y\@nmc.ae,alexy.r\@nmc.ae,kamraj.shetty\@re.ae,vyshali.rai\@re.ae";


sub sendMail {

	my $smtp = Net::SMTP->new(Host=>$host, Port=>$port, Timeout => 10, Debug => 0);

	die "Could not connect to server!\n" unless $smtp;

	my $subject = shift(@_);
	my $body = shift(@_);

	$smtp->auth($user,$password);

	# Email Header
	#

	$smtp->mail($from . "\n");

	my @recepients = split(/,/, $to);

	foreach my $recp (@recepients) {
	     $smtp->to($recp . "\n");
	}

	#$smtp->cc('example@abc.com');
	#$smtp->bcc('example@abc.com');
	$smtp->data;
	$smtp->datasend("From: " . $from . "\n");
	$smtp->datasend("To: " . $to . "\n");
	$smtp->datasend("Subject: " . $subject ."\n");

	$smtp->datasend("Content-Type: text/html \n");

	#Send Body

	$body = $body . "\n\nFor more details about SCM please check the log.";
	$body =~ s/\n/<br>/g;
	$smtp->datasend($body . "\n\n");
	$smtp->dataend;
	$smtp->quit;

}

