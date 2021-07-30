#!/usr/bin/perl -w
package Insta::HL7Module::Consultation;

use parent ("Insta::HL7Module");


use strict;

sub name { "'CONSULTATION'" };
sub exportType { "'F','S','B'"};

=comment
##################################################
sub new {
##################################################

	my $class = shift;
	my $params = shift;

	my $self = {};
	foreach my $key (keys(%$params)) {
		$self->{$key} = $params->{$key};
	}

	bless $self, $class;
}

=cut


##################################################
sub getMSH {
##################################################

	#
	# Standard message header attributes for all ORM exports
	#
	my %ORM_MSH = (
		sendApp=>'InstaHMS', sendFac=>'Export.CONSULTATION',
		msgType=>['ORM','O01'], procId=>'P', ver=>'2.3'
	);
	return %ORM_MSH;
}


##################################################
sub updateExportableOrders {
##################################################

=begin comment

	my $this = shift;
	my $dbh = $self->{'db'};

	#
	# Update the bill_paid (actually ready_for_export) status for existing rows in hl7_export_items.
	# This will take effect in case the bill was paid, or sample collected after the order was placed,
	# for orders already present in hl7_export_items.
	#
	# todo: rename bill_paid to ready_for_export since this considers sample collection as well
	# todo: need updated_ts when we actually set it to ready for export.
	#
	my $sth = $dbh->prepare(qq{
		UPDATE hl7_export_items ei SET bill_paid = 'Y'
		FROM doctor_consultation dc, bill_charge bc, bill b, doctors
		WHERE bill_paid = 'N' AND item_type = 'TEST'
			AND (b.payment_status = 'P' OR b.bill_type = 'C')
			AND (b.bill_no = bc.bill_no)
			AND (d.test_id = tp.test_id)
			AND (tp.prescribed_id = ei.item_id::integer)
	});

	$sth->execute();
	
	return $sth->rows;

=end comment

=cut	
	
}

sub getOrderSql {

	my $self = shift;
	my ($ifName) = @_;
	my $sql = qq{
		SELECT ei.*, dc.patient_id as patient_id, dc.consultation_id as prescribed_id, dr.doctor_name, '' as hl7_export_code,
            dr.doctor_id, to_char(dc.presc_date, 'YYYYMMDDhh24mi') as pres_date, dc.common_order_id,
            coalesce(to_char(sched.appointment_time, 'YYYYMMDDhh24mi'), to_char(sched.arrival_time, 'YYYYMMDDhh24mi'),
            	to_char(dc.consultation_complete_time, 'YYYYMMDDhh24mi')) as scheduled_time_start,
            coalesce(to_char(sched.completed_time, 'YYYYMMDDhh24mi'), to_char(dc.consultation_complete_time, 'YYYYMMDDhh24mi'))
                as scheduled_time_end,
            pd.mr_no as mr_no, pd.oldmrno,
			pd.last_name as last_name,
			COALESCE(pd.patient_name,'') as patient_name, sm.salutation,
			pd.patient_gender as patient_gender,
           	to_char(coalesce(pd.dateofbirth, (pd.expected_dob + interval '6 months')::date), 'YYYYMMDD')
				as expected_dob,
            COALESCE(pd.patient_address, '') as patient_address,
			COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') as city_code,
			COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') as state_code,
			COALESCE(cnm.country_name,'') as country_name, COALESCE(cnm.country_code,'') as country_code,
			COALESCE(pd.patient_phone, '') AS patient_phone,
            to_char((pr.reg_date + pr.reg_time)::timestamp, 'YYYYMMDDhh24mi') as reg_date,
			substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name,
			pr.reference_docto_id, mrd.icd_code as diagnosis_code, mrd.description as diagnosis,
			substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
			pr.center_id AS center_id, hcm.center_name
		FROM hl7_export_items ei
			JOIN doctor_consultation dc ON (dc.patient_id = ei.item_id AND ei.item_type = 'CONSULTATION' AND dc.status ='A')
			LEFT JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)
			LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = dc.patient_id AND diag_type = 'P')
			LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
			LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
			LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
			LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
			LEFT JOIN doctors dr ON dr.doctor_id = dc.doctor_name
			LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
			LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
			LEFT JOIN city ci ON pd.patient_city = ci.city_id
			LEFT JOIN state_master st ON pd.patient_state = st.state_id
			LEFT JOIN country_master cnm ON pd.country = cnm.country_id
			LEFT JOIN scheduler_appointments sched ON (sched.appointment_id = dc.appointment_id)
			LEFT JOIN scheduler_appointment_items schi ON (sched.appointment_id = schi.appointment_id 
			AND schi.resource_type IN ('DOC', 'OPDOC'))
		WHERE ei.bill_paid = 'Y' AND ei.export_status IN ('N','F') AND ei.interface_name=?};

	return $sql;
}



sub getOrderMessage {
	
	my $self = shift;
	my ($order, $interface ) = @_;
	
	my $dbh = $self->{'db'};

	my $msgId = ($dbh->selectrow_array(qq{SELECT nextval('hl7_msgid_sequence') AS hl7_msg_id}))[0];
	my %ORM_MSH = $self->getMSH(); 
	my $msg = new Hl7::Message(\%ORM_MSH);
	$msg->{MSH}{controlId} = $msgId;
	$msg->{MSH}{recvApp} = $interface->{sending_app};
	$msg->{MSH}{recvFac} = $interface->{sending_facility};

	if ($order->{center_id} != 0) {
		$msg->{MSH}{sendFac} = $order->{center_name};
	}

	# Order cancellations do not require a PID / PV1

	if ($order->{op_code} ne 'C') {
		# Add PID
		my ($line1, $line2) = split('\r{0,1}\n', $order->{patient_address});
		my $pid = new Hl7::Segment('PID', {
				sid=> 1,
				pidList=>[$order->{mr_no},'InstaHMS'],
				name=> [$order->{last_name} || '-', $order->{patient_name}, '', '', $order->{salutation}],
				dob=> $order->{expected_dob}, sex=> $order->{patient_gender},
				addr=> [$line1 || '', $line2 || '', $order->{cityname}, $order->{state_code}, '',
					$order->{country_code} || $order->{country_name}],
				phHome => $order->{patient_phone} || ''
			});

# Bug#: 	44654
#
#		if ($order->{oldmrno}) {
#			$pid->{'pidList~'} = [[$order->{oldmrno}, 'Alternate']];
#		}
		$msg->addSegment($pid);

		# Add PV1
		my $pv = new Hl7::Segment('PV1', {
				sid=>1, patientClass=>'O', visitNum => $order->{patient_id},
				admitTimest=>$order->{reg_date}, assgndLoc=> ['', $order->{ward_name}||'', $order->{bed_type}]
			});

		if ($order->{refdoctorname}) {
			$pv->{refDoctor} = [$order->{reference_docto_id}, $order->{refdoctorname}];
		}

		if ($order->{doctor_name}) {
			$pv->{admitDoctor} = [$order->{doctor_id}, $order->{doctor_name}];
		}
	    $msg->addSegment($pv);
	}

	# Add ORC - placerOrderNum should be separated by dot(.) instead of with ampersand(&).
	my $orderNum = $order->{prescribed_id};
	my $format = $order->{conduction_format} || '';
	if ($format eq 'V' && defined($order->{resultlabel_id})) {
		$orderNum .= ".". $order->{resultlabel_id};
	}
	if (defined($order->{sample_sno})) {
		$orderNum .= ".". $order->{sample_sno};
	}

	my $ctrlCode = $order->{op_code} eq 'C' ? 'CA' : 'NW';

	my $orc = new Hl7::Segment('ORC', {ctrl=>$ctrlCode, placerOrderNum=>[$orderNum, 'InstaHMS'],
		placerGrpNum=>$order->{common_order_id} });

	$msg->addSegment($orc);

	# Order cancellations do not have any OBR segments
	if ($order->{op_code} ne 'C') {
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
				# serviceId=>[$serviceId, $order->{resultlabel}||$order->{test_name},
				#	'InstaHMS', '', $order->{equipment_code} || ''],
				placerOrderNum=>[$orderNum, 'InstaHMS'],
				reqTime=>$order->{pres_date}, specimenAction=>'O', specimenTime=>$order->{sample_date},
				reason=>$order->{diagnosis}
			});
		if (defined $order->{scheduled_time_start}) {
			$obr->{qtyTiming} = [1,'','',$scheduledStart, $order->{scheduled_time_end}]
		}
		if (defined($order->{sample_sno})) {
			$obr->{placer1} =  $order->{sample_sno};
		}
		$msg->addSegment($obr);
	}
	return $msg;
}




return 1;
