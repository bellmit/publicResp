#!/usr/bin/perl -w
package Insta::HL7Formatter;
use strict;
use Hl7::Message;


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
sub getORC {
##################################################
	
	my $self = shift;
	my ($data, $orderNum, $ctrlCode, $orderStatus, $responseFlag) = @_;
	
	my $orc = new Hl7::Segment('ORC', {ctrl=>$ctrlCode, placerOrderNum=>[$orderNum, 'InstaHMS'],
		placerGrpNum=>$data->{common_order_id}, orderSts=>$orderStatus, respFlag=>$responseFlag});
		
	return $orc;	
}

##################################################
sub getOBX {
##################################################

	my $self = shift;
	my ($order, $orderNum, $obsResultStatus, $interface) = @_;
	my $conductedInRptFormat = $order->{conducted_in_reportformat};
	my $reportValue;
	my $valueType = 'ST';
	# for template based we should have template content and for value based will have result value in obsValue field
	if ($conductedInRptFormat eq 'Y') {
		$reportValue = $order->{patient_report_file};
		$reportValue =~ s/\n//g;
    	$reportValue =~ s/\r//g;
    	$valueType = 'TX';
	} else {
		$reportValue = $order->{report_value};
	}
	# Order cancellations do not have any OBR segments
	# Add OBR
	my $obx;
	if ($interface->{'send_result_as'} eq 'V') {	
		$obx = new Hl7::Segment('OBX', {sid=>1,
			valueType=>$valueType,
			obsId=>[$orderNum, 'InstaHMS'],obsSubId=>1,obsValue=>$reportValue,
			units=>$order->{units}, obsResultSts=>$obsResultStatus, obsTime=>$order->{report_date},
			accessChecks=>$order->{withinnormal}
		});
	} elsif ($interface->{'send_result_as'} eq 'R') {
		$obx = new Hl7::Segment('OBX', {sid=>1,
				valueType=>'RP',
				obsId=>$order->{test_names},obsSubId=>1,
				obsValue=>[$order->{patient_id}.'_'.$order->{report_id}.'.pdf', , 'Application', 'application/pdf'],
				units=>$order->{units}, obsResultSts=>$obsResultStatus, obsTime=>$order->{report_date}
			});	
	}
	
	return $obx;
}

##################################################
sub getNTE {
##################################################

	my $self = shift;
	my ($order, $orderNum) = @_;
	my $nte = new Hl7::Segment('NTE', {sid=>1, comment=>$order->{amendment_reason}, commentType=>'1R'});
	
	return $nte;
}

##################################################
sub getOBR {
##################################################

	my $self = shift;
	my ($order, $orderNum, $format) = @_;

	# Order cancellations do not have any OBR segments
		# Add OBR
	my $serviceId = $order->{hl7_export_code};
	if (!defined($serviceId) || $serviceId eq '') {
		$serviceId = $order->{test_id};
	}
	if ($format eq 'V' && defined($order->{resultlabel_id})) {
		$serviceId = $order->{resultlabel_id};
	}

	my $scheduledStart = $order->{scheduled_time_start} || $order->{pres_date};
	my $obr = new Hl7::Segment('OBR', {sid=>1,
			serviceId=>[$serviceId, $order->{resultlabel}||$order->{test_name},
			'InstaHMS', '', $order->{equipment_code} || ''],
			placerOrderNum=>[$orderNum, 'InstaHMS'],fillerOrderNum=>$orderNum,
			reqTime=>$order->{pres_date}, specimenAction=>'O', specimenTime=>$order->{sample_date},
			reason=>$order->{diagnosis},
			diagServId=>$order->{equipment_code}
		});
	if (defined($order->{scheduled_time_start})) {
		$obr->{qtyTiming} = [1,'','',$scheduledStart, $order->{scheduled_time_end}];
#		$log->info("scheduled start time : $scheduledStart, $order->{scheduled_time_start}, $order->{pres_date}");
	}
	if (defined($order->{start_date_time})) {
		$obr->{scheduledDtTime} = $order->{start_date_time};
	}
	if (defined($order->{sample_sno})) {
		$obr->{placer1} =  $order->{sample_sno};
	}else {
		$obr->{placer1} = '01';
		$obr->{filler2} = '01';
	}

	if(defined($order->{prescription_doctor_id})) {
		$obr->{ordProvider} = [$order->{prescription_doctor_id},$order->{prescription_doctor_name}];
	}
	
	return $obr;
}

##################################################
sub getOrderNum {
##################################################
	my $this = shift;
	my ($order) = @_;

	my $orderNum = $order->{prescribed_id};
	my $conductedInRptFormat = $order->{conducted_in_reportformat};
    if ($conductedInRptFormat eq 'Y' && defined($order->{format_name}) && $order->{format_name} ne '') {
		$orderNum .= ".". $order->{format_name};
	} elsif(defined($order->{resultlabel_id}) && $order->{resultlabel_id} ne '') {
		$orderNum .= ".". $order->{resultlabel_id};
	}
	if (defined($order->{sample_sno})) {
		$orderNum .= ".". $order->{sample_sno};
	}
	return $orderNum;
}

##################################################
sub getReconductedStatus {
##################################################
	my $self = shift;
	my ($prescId) = @_;
	my $dbh = $self->{'db'};
	my $reconductedDetails = $dbh->selectrow_hashref("SELECT conducted FROM tests_prescribed WHERE prescribed_id=?", {}, $prescId);
	return $reconductedDetails->{conducted};
}

##################################################
sub getMSH {
##################################################

	my $self = shift;
	my ($interface, $data, $type, $subtype) = @_;
	##########################################################
	#**Standard message header attributes for all ORM exports
	##########################################################
	
	my $dbh = $self->{'db'};
	my $msgId = ($dbh->selectrow_array(qq{SELECT nextval('hl7_msgid_sequence') AS hl7_msg_id}))[0];
	$self->{'log'}->debug("Message Id :".$msgId);
	my %ORM_MSH = (
		sendApp=>'InstaHMS', sendFac=>'Diag',
		msgType=>[$type,$subtype], procId=>'P', ver=>'2.3',
		controlId=>$msgId, recvApp=>$interface->{sending_app},
		recvFac=>$interface->{sending_facility}
	);

	my $msg = new Hl7::Message(\%ORM_MSH);

#	if ($data->{center_id} != 0) {
#		$msg->{MSH}{sendFac} = $data->{center_name};
#	}
	
	return $msg;
}

##################################################
sub getPID {
##################################################
	my $self = shift;
	my ($data) = @_;
	# Add PID
	# $self->{'log'}->debug("Patient address ". $data->{patient_address});
	# my ($line1, $line2) = split('\r{0,1}\n', $data->{patient_address});
	my $line1 = '';
	my $line2 = '';
	my $pid = new Hl7::Segment('PID', {
			sid=> 1,
			pidList=>[$data->{mr_no},'InstaHMS'],
			name=> [$data->{last_name} || '-', $data->{patient_name}, '', '', $data->{salutation}],
			dob=> $data->{expected_dob}, sex=> $data->{patient_gender},
			addr=> [$line1 || '', $line2 || '', $data->{cityname}, $data->{state_code}, '',
				$data->{country_code} || $data->{country_name}],
			phHome => [$data->{patient_phone},'','',$data->{patient_email_id}]
		});
	if ($data->{oldmrno}) {
		$pid->{'pidList~'} = [[$data->{oldmrno}, 'Alternate']];
	}
	
	return $pid;
}

##################################################
sub getPV1 {
##################################################
	my $self = shift;
	my ($data) = @_;
	# Add PV1
	my $pv = new Hl7::Segment('PV1', {
			sid=>1,  visitNum => $data->{patient_id},
			admitDate=>$data->{reg_date}, assgndLoc=> [$data->{prescription_department}||'', $data->{ward_name}||'', $data->{bed_type}]
		});

	if($data->{visit_type} eq 'o') {
		$pv->{patientClass} = 'O';
	}else {
		$pv->{patientClass} = 'I';
	}
	if ($data->{refdoctorname}) {
		$pv->{refDoctor} = [$data->{reference_docto_id}, $data->{refdoctorname}];
	}

	if ($data->{doctor_name}) {
		$pv->{admitDoctor} = [$data->{doctor}, $data->{doctor_name}];
	}
	
	if(defined($data->{conducting_doctor_id})) {
		$pv->{attdDoctor} = [$data->{conducting_doctor_id},$data->{conducting_doctor_name}];
	}
	
	return $pv;
}		

return 1;
