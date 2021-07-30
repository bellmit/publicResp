#!/usr/bin/perl
package Insta::HL7Module;
use Insta::Util;
use Hl7::Message;
use strict;

##################################################
sub new {
##################################################
    my $class = shift;
    my $params = shift;

    my $self = {};
    foreach my $key (keys(%$params)) {
        $self->{$key} = $params->{$key};
    }

	$class->init();
    bless $self, $class;
}

sub init {

}

##################################################
sub processModule {
##################################################
	my $self = shift;
	my $interfaceType = $self->name;
	my ($lisInterface, $risInterface) = split(/\,/, $interfaceType, 2);
	$lisInterface =~ s/'//g;
	$risInterface =~ s/'//g;

	my $interfaces = $self->getModuleInterface();
	
	$self->updateExportableOrders();
	if ($lisInterface eq 'LIS' || $risInterface eq 'RIS') {
		$self->reportNonExportableOrders();
	}
	foreach my $ifName (keys %$interfaces)  {
		my $rowHRef = $interfaces->{$ifName};
		$self->processOrders($rowHRef);
	}
}

sub processOrders {
	my $self = shift;
	my $ifDes = shift;

	$self->{'log'}->info("Processing Order :interface_type =  $ifDes->{interface_type}, interface_name : $ifDes->{interface_name}, 
			center_id: $ifDes->{center_id}, Item Type: $ifDes->{item_type}");
	my $orders = $self->getInterfaceOrders($ifDes);

	foreach my $order (@$orders) {
		$self->{'log'}->info("************************[ Posting message ]**************************");
		$self->{'log'}->info("\tinterface_name = $ifDes->{interface_name}\t center_id = $ifDes->{center_id}");
		$self->{'log'}->info("\texport_type = $ifDes->{export_type}\t prescription_id = $order->{prescribed_id}");
		$self->{'log'}->info("\tOP_CODE : $order->{op_code}");
		$self->{'log'}->info("*********************************************************************");		

		my $msg = $self->getOrderMessage($order, $ifDes);

		# Send the HL7 message according to interface preference (and also update
		# hl7_export_items to indicate done/failed status)
	
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
		} else {
			$self->{'log'}->error("Message Export failed due to previous error !!! item_id : ". $order->{item_id}. ", message_id : ".$msg->{MSH}{controlId});
			$self->{'log'}->info("--------------------------------[MSG-END] ** [EXPORT FAILED]------------------------\n");
		}
	}
}

sub getInterfaceOrders {
	my $self = shift;
	my $ifDesc = shift;
	my $sql = undef;
	my $orders = undef;
	my $interfaceType = $self->name;
	my ($lisInterface, $risInterface) = split(/\,/, $interfaceType, 2);
	$lisInterface =~ s/'//g;
	$risInterface =~ s/'//g;
	
	if ($lisInterface eq 'LIS' || $risInterface eq 'RIS') {
		if($ifDesc->{item_type} eq 'TEST') {
			$sql = $self->getOrderSql($ifDesc->{send_orm});
		}
		elsif($ifDesc->{item_type} eq 'TESTTEMPLATE') {
			$sql = $self->getOrderSqlForTemplate();
		}
	} else {
		$sql = $self->getOrderSql();
	}	
	my $dbh = $self->{'db'};
	$self->{'log'}->info("Geting Orders from database ");
	$self->{'log'}->debug("\n $sql \n");
	$self->{'log'}->debug("Interface Name : $ifDesc->{interface_name}");
	if ($ifDesc->{interface_type} eq 'LIS' || $ifDesc->{interface_type} eq 'RIS') {
		$orders = $dbh->selectall_arrayref($sql, {Slice=>{}}, $ifDesc->{hl7_lab_interface_id}, $ifDesc->{center_id});
	} else {
		$orders = $dbh->selectall_arrayref($sql, {Slice=>{}}, $ifDesc->{interface_name});
	}
	return $orders;
}

##################################################
sub getModuleInterface {
##################################################
	my $self = shift;

	my $modInterfaceSql = $self->getExportInterfaceSql();
	$self->{'log'}->info("Getting list of module interface sql :");
	$self->{'log'}->debug("\n\n\t\t\t $modInterfaceSql"."\n");
	my $dbh = $self->{"db"};
    # get all interfaces configured
    my $interfaces = $dbh->selectall_hashref($modInterfaceSql,'interface_center');
	if(!%$interfaces){
		$self->{'log'}->info("Either interface not configured OR There is no orders to export");
	    exit 0;
	}
	return $interfaces;
}

# Find how to get lab interfaces only.

##################################################
sub getInterfaceSql {
##################################################
	my $class = shift;
	my $export_type = $class->exportType;
	my $interface_type = $class->name;
	my $sql = qq{SELECT hli.*,hci.*, (hli.interface_name||'-'|| hci.center_id)::character varying as interface_center
                		 FROM hl7_lab_interfaces hli
		                 JOIN hl7_center_interfaces hci USING(interface_name)
                		 WHERE status='A' 
				 AND export_type IN ($export_type)
	 			 AND interface_type IN ($interface_type)};
	return $sql;		
}

##################################################
sub getExportInterfaceSql {
##################################################
	my $class = shift;
	my $export_type = $class->exportType;
	my $interface_type = $class->name;
	my $sql = qq{SELECT hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
					hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx, hli.send_orc4,
					hli.send_result_as, hli.send_pidpv1_for_cancel, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
					hci.center_id, (hli.interface_name||'-'||hei.item_type||'-'||hci.center_id)::character varying as interface_center,
					hli.send_orm, hci.hl7_lab_interface_id, hli.send_insurance_segments, hli.doctor_identifier, hli.send_center_id
					FROM hl7_export_items hei 
					JOIN hl7_lab_interfaces hli ON (hei.export_status IN ('N', 'F') AND hei.item_type != 'VISIT' 
						AND hli.interface_type IN ($interface_type) AND hli.status = 'A' AND hli.hl7_lab_interface_id = hei.hl7_lab_interface_id) 
					JOIN hl7_center_interfaces hci ON (hci.export_type IN ($export_type) AND (hci.hl7_lab_interface_id = hli.hl7_lab_interface_id)
						AND (hci.center_id = hei.center_id OR 0 = hci.center_id))
				GROUP BY hei.item_type, hli.interface_name, hli.sending_app, hli.interface_type, hci.export_type,
				hli.sending_facility, hli.append_doctor_signature, hli.consolidate_multiple_obx, hli.send_orc4,
				hli.send_result_as, hli.send_pidpv1_for_cancel, hci.orders_export_dir, hci.orders_export_ip_addr, hci.orders_export_port,
				hci.center_id, hli.send_orm, hci.hl7_lab_interface_id, hli.send_insurance_segments, hli.doctor_identifier, hli.send_center_id ORDER BY hli.interface_name, hci.center_id};
	return $sql;		
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

	my $export_file_name =$ifDes->{interface_type}.'_'. $interfaceName.'_'.$patientId.'_'.$order->{item_id}.'_'.$msgId.'.hl7';
	
	unless (open(FH, "> $ifDes->{orders_export_dir}/$export_file_name")) {
		my $failureMsg = "Folder is not configured properly. Could not create file $ifDes->{orders_export_dir}/$export_file_name";
		$self->{'log'}->error($failureMsg);
		$self->updateExportedFailure($order->{export_id}, $msgId, $failureMsg, 'F') if ($isMainTransport);
		return 0;
	}

	print FH $msg->toString(0);
	close FH;

	$self->updateExportedSuccess($order->{export_id}, $msgId) if ($isMainTransport);
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
}

1;
