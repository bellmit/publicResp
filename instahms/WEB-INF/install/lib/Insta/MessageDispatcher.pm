#!/usr/bin/perl -w
package Insta::MessageDispatcher;
use strict;
use IO::Socket::INET;

##################################################
sub new {
##################################################

	my $class = shift;
	my $params = shift;
	
	my $self = {};
	foreach my $key (keys(%$params)) {
	   $self->{$key} = $params->{$key};
	}
	
	$class->init($self);
	bless $self, $class;
}

##################################################
sub init {
##################################################

}

##################################################
sub exportMessage {
##################################################

	my $self = shift;
	my ($order, $msg, $ifDes) = @_;
	my $isMainTransport = ($ifDes->{export_type} eq 'F');
	my $msgId = $msg->{MSH}{controlId};
	my $patientId = $order->{patient_id};
	my $interfaceName = $order->{interface_name} . "_" . $order->{center_id};
	my $orderNum = $order->{prescribed_id};
	my $format = $order->{conduction_format} || '';
	if ($format eq 'V' && defined($order->{resultlabel_id})) {
		$orderNum .= ".". $order->{resultlabel_id};
	}
	if (defined($order->{sample_sno})) {
		$orderNum .= ".". $order->{sample_sno};
	}

	$self->{'log'}->info("Exporting message $msgId" );
	if(Insta::Util->getProperty('log.level') eq 4 ) {
		$self->{'log'}->debug($msg->dump());
	}

	my $export_file_name =$self->getExportFileName($order, $msg, $ifDes);
	
	unless (open(FH, "> $ifDes->{orders_export_dir}/$export_file_name")) {
		my $failureMsg = "Folder is not configured properly. Could not create file $ifDes->{orders_export_dir}/$export_file_name";
		$self->{'log'}->error($failureMsg);
		if ($ifDes->{send_result_as} eq 'V') {
			$self->updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F') if ($isMainTransport);
		} elsif ($ifDes->{send_result_as} eq 'R') {
			$self->updateExportedFailureForReport($order->{export_id}, $msgId, $failureMsg, 'F') if ($isMainTransport);
		}
		return 0;
	}

	print FH $msg->toString(0);
	close FH;
	if ($ifDes->{send_result_as} eq 'V') {
		$self->updateExportedSuccess($order->{export_id}, $msgId) if ($isMainTransport);
	} elsif ($ifDes->{send_result_as} eq 'R') {
		$self->updateExportedSuccessForReport($order->{export_id}, $msgId) if ($isMainTransport);
	}
	$self->{'log'}->info(" File created [ $export_file_name ]");
	return 1;
}

##################################################
sub sendMessage {
##################################################

	my $self = shift;
	my ($order, $msg, $ifDes ) = @_;
	my $ipAddr = $ifDes->{orders_export_ip_addr};
	my $port = $ifDes->{orders_export_port};
	my $msgId = $msg->{MSH}{controlId};

	$self->{'log'}->info("Sending message: $msgId\n". $msg->dump());
	
	my $conn = IO::Socket::INET->new (
		Proto    => "tcp",
		PeerAddr => $ipAddr,
		PeerPort => $port,
		timeout  => 10
	);

	unless ($conn) {
		my $failureMsg = "Unable to connect to host $ipAddr at port $port: $!";
		$self->{'log'}->error($failureMsg);
		$self->updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F');
		return 0;
	}

	$self->{log}->info("Writing into a socket ...");
	print $conn "\x0B". $msg->toString(). "\x1C\x0D";
	$conn->flush();
	$self->{log}->info("Message sent successfully");

	# Read the response:
	$/ = "\x1C\x0D";	# file separator, carriage return
	my $resp;
	my $respMsg;
	eval {
		local $SIG{ALRM} = sub { die "Timed Out" };
		alarm 10;
		$resp = <$conn>;
		alarm 0;
	};
	if ($@) {
		my $failureMsg;
		if ($@ =~ /Timed Out/) {
			$failureMsg = "ERROR: Reading response timed out";
		} else {
			$failureMsg = "ERROR: Failed to read response: $@";
		}
		$self->{'log'}->error($failureMsg);
		$self->updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F');
		$conn->close();
		return 0;
	}

	eval {
		$respMsg = Hl7::Message->new($resp);
		$self->{'log'}->info("Recd commit message acknowledgement:");
		$self->{'log'}->debug($respMsg->toString(1));
	};
	if ($@) {
		my $clean = $resp;
		$clean =~ tr/\x0B\x0D\x1C/\n\n./;
		my $failureMsg = "Could not parse response as HL7: $clean";
		$self->{'log'}->error($failureMsg);
		$self->updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'R');
		return 0;
	}
	$conn->close();

	if ($respMsg->{MSA}{code} ne 'AA') {
		my $failureMsg = "Receiver rejected the message due to: " . $respMsg->{MSA}{msgText};
		$self->{'log'}->error($failureMsg);
		$self->updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'R');
		return 0;
	}

	$self->{'log'}->info("Sending message succeeded: ", $respMsg->{MSA}{code}, ", updating success.");
	$self->updateExportedSuccess($order->{export_id}, $msgId);
	return 1;
}

##################################################
sub updateExportedSuccess {
##################################################

	my $self = shift;
        my ($exportId, $msgId) = @_;

        my $dbh = $self->{'db'};
        my $sth = $dbh->prepare(qq{
                UPDATE hl7_export_items set exported_ts=current_timestamp, export_status='S', export_msg_id=?
                WHERE export_id=?});
        $sth->execute($msgId, $exportId);
#        $dbh->disconnect();
}


##################################################
sub updateExportedSuccessForReport {
##################################################
		my $self = shift;
        my ($exportId, $msgId) = @_;
		my @split_parts = split(/\,/, $exportId);
		my @exportid_array = ();
		foreach my $part (@split_parts) {
			push(@exportid_array, ltrim($part));
		}
        my $dbh = $self->{'db'};
        my $sql = "UPDATE hl7_export_items set exported_ts=current_timestamp, export_status='S', export_msg_id=? WHERE export_id::text in ("
           . join(",", map { $dbh->quote($_) } @exportid_array)
           . ")";
        my $sth = $dbh->prepare($sql);
        $sth->execute($msgId);
#       $dbh->disconnect();
}

##################################################
sub updateExportedFailure {
##################################################
	my $self = shift;
	my ($exportId, $msgId, $failureMsg, $failType) = @_;

	my $dbh = $self->{'db'};
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items set export_status=?, export_msg_id=?, export_failure_msg=?
		WHERE export_id=?});
	$sth->execute($failType, $msgId, $failureMsg, $exportId);
#	$dbh->disconnect();
}

##################################################
sub updateExportedFailureForReport {
##################################################
	my $self = shift;
	my ($exportId, $msgId, $failureMsg, $failType) = @_;
	my @split_parts = split(/\,/, $exportId);
	my @exportid_array = ();
	foreach my $part (@split_parts) {
		push(@exportid_array, ltrim($part));
	}
	
	my $dbh = $self->{'db'};
	my $sql = "UPDATE hl7_export_items set export_status=?, export_msg_id=?, export_failure_msg = ? WHERE export_id::text IN ("
	   . join(",", map { $dbh->quote($_) } @exportid_array)
	   . ")";
	my $sth = $dbh->prepare($sql);
	print("\n $sql \n");
	$sth->execute($failType, $msgId, $failureMsg);
#   $dbh->disconnect();
}

sub ltrim { 
	my $s = shift; 
	$s =~ s/^\s+//;       
	return $s 
};

return 1;
