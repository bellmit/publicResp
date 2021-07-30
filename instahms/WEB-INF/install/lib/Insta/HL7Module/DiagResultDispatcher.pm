#!/usr/bin/perl -w
package Insta::HL7Module::DiagResultDispatcher;
use strict;
use parent ("Insta::MessageDispatcher");

##################################################
sub dispatchMessage() {
##################################################

	my $self = shift;
	my ($order, $msg, $ifDes) = @_;
	my $status = 0;
	
	if (($ifDes->{export_type} eq 'F') || ($ifDes->{export_type} eq 'B')) {
		$status = $self->exportMessage($order, $msg, $ifDes);
	}
	if (($ifDes->{export_type} eq 'S') || ($ifDes->{export_type} eq 'B')) {
	# this could fork and do the job in a different process, so don't rely on return value
		$status = $self->sendMessage($order, $msg, $ifDes);
	}
	# otherwise, the interface wouldn't have been selected
	if($status eq 1) {
		$self->{'log'}->info("Message Exported Successfully  Item_Id : ". $order->{item_id}. ", message_id : ".$msg->{MSH}{controlId}."\n");
		$self->{'log'}->info("--------------------------------[MSG-END] ** [EXPORTED SUCCESSFULLY]------------------------\n");
	}else {
		$self->{'log'}->error("Message Export failed due to previous error !!! item_id : ". $order->{item_id}. ", message_id : ".$msg->{MSH}{controlId});
		$self->{'log'}->info("--------------------------------[MSG-END] ** [EXPORT FAILED]------------------------\n");
	}
	return $status;
}

sub getExportFileName() {
	my $self = shift;
	my ($data, $msg, $ifDes) = @_;
#	$ifDes->{interface_type}.'_'. 
	return $msg->{MSH}{controlId}.'_'.$data->{interface_name} . "_" . $data->{center_id}.'_'.$data->{patient_id}.'_'.$data->{item_id}.'.hl7';
}
return 1;
