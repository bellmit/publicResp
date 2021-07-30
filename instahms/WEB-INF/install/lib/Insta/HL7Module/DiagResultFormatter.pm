#!/usr/bin/perl -w
package Insta::HL7Module::DiagResultFormatter;
use strict;
use parent ("Insta::HL7Formatter");


##################################################
sub getMessage {
##################################################

	my $self = shift;
	my ($data, $interface) = @_;
	$self->{'log'}->debug("Processing Item for interface :".$interface);
	my $msg;
	my $orc;
	my $orderNum = $self->getOrderNum($data);
	$self->{'log'}->debug("Order Number :".$orderNum);	

	if ($data->{op_code} eq 'RS' || $data->{op_code} eq 'RC' || $data->{op_code} eq 'AM') {
		$msg = $self->getMSH($interface, $data, "ORM", "O01");
		if ($data->{op_code} eq 'RS' || $data->{op_code} eq 'AM') {
			$orc = $self->getORC($data, $orderNum, "SC", "IP", "");
		} else {
			$orc = $self->getORC($data, $orderNum, "SC", "RP", "");
		} 
	} else {
		$msg = $self->getMSH($interface, $data, "ORU", "R01");
		if ($data->{op_code} eq 'N' && $data->{re_conduction}) {
			my $reconductedStatus = $self->getReconductedStatus($data->{reference_pres});
			if ($reconductedStatus eq 'RBS') {
				$orc = $self->getORC($data, $orderNum, "RE", "", "");
			} else {
				$orc = $self->getORC($data, $orderNum, "RU", "", "");
			}				
		} elsif($data->{op_code} eq 'N' && (defined($data->{original_test_details_id}) && $data->{original_test_details_id} ne '0')
				|| $data->{test_detail_status} eq 'A') {
			$orc = $self->getORC($data, $orderNum, "RU", "", "E");
		} else {
			$orc = $self->getORC($data, $orderNum, "RE", "", "");
		}
	}	
	if ($data->{op_code} ne 'C') {
	
		my $pid = $self->getPID($data);
		$msg->addSegment($pid);

		my $pv = $self->getPV1($data);
	    $msg->addSegment($pv);
	}
	
	$msg->addSegment($orc);

	my $obr = $self->getOBR($data, $orderNum);		
	$msg->addSegment($obr);
	
	if ($data->{op_code} eq 'C' || $data->{op_code} eq 'N') {
		my $obx;
		if ($data->{test_detail_status} eq 'A') {
			$obx = $self->getOBX($data, $orderNum, "D", $interface);
		} elsif(defined($data->{original_test_details_id}) && $data->{original_test_details_id} ne '0') {
			$obx = $self->getOBX($data, $orderNum, "C", $interface);
		} else {
			$obx = $self->getOBX($data, $orderNum, "F", $interface);
		}		
		$msg->addSegment($obx);
	}

	if ($data->{test_detail_status} eq 'A') {
		my $nte = $self->getNTE($data, $orderNum);		
		$msg->addSegment($nte);
	}
	
	return $msg;
}

return 1;
