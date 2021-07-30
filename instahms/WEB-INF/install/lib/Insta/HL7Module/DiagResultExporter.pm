#!/usr/bin/perl -w
package Insta::HL7Module::DiagResultExporter;
use strict;
use Insta::HL7Module::DiagResultExtractor;
use Insta::HL7Module::DiagResultFormatter;
use Insta::HL7Module::DiagResultDispatcher;
use parent ("Insta::HL7Exporter");

sub name { "'LIS','RIS'" };
sub exportType { "'F','S','B'"};


sub init {
	my ($class, $self) = @_;
	
	$self->{'extractor'} = Insta::HL7Module::DiagResultExtractor->new($self);
	$self->{'formatter'} = Insta::HL7Module::DiagResultFormatter->new($self);
	$self->{'dispatcher'} = Insta::HL7Module::DiagResultDispatcher->new($self);
}

##################################################
sub getItemType {
##################################################
	my $self = shift;
	return "TESTRESULT";
}

return 1;
