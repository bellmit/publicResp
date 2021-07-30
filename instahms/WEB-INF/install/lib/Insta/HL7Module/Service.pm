#!/usr/bin/perl -w
package Insta::HL7Module::Service;
use strict;

use parent ("Insta::HL7Module");

sub name { "'SERVICE'" };
sub exportType { "'F','S','B'"};



##########################################################
#**Standard message header attributes for all ORM exports
##########################################################
my %ORM_MSH = (
	sendApp=>['InstaHMS','SERVICE'], sendFac=>'SERVICE',
	msgType=>['ORM','O01'], procId=>'P', ver=>'2.3'
);



##################################################
sub updateExportableOrders {
##################################################

	my $self = shift;	
	my $dbh = $self->{'db'};

	
	# Update the bill_paid (actually ready_for_export) status for existing rows in hl7_export_items.
	# This will take effect in case the bill was paid, or sample collected after the order was placed,
	# for orders already present in hl7_export_items.
	#
	# todo: rename bill_paid to ready_for_export since this considers sample collection as well
	# todo: need updated_ts when we actually set it to ready for export.
	
	my $sql = qq{
	
			UPDATE hl7_export_items ei SET bill_paid = 'Y'
			FROM services_prescribed sp, bill_activity_charge bac, bill_charge bc, bill b, services s, hl7_lab_interfaces hli 
			WHERE bill_paid = 'N'  
			AND ei.export_status = 'N' 
			AND item_type = 'SERVICE'
			AND (sp.prescription_id = ei.item_id::integer)
			AND (bac.activity_id = sp.prescription_id::text AND bac.activity_code = 'SER')
			AND (bc.charge_id = bac.charge_id)
			AND (b.bill_no = bc.bill_no)
			AND (s.service_id = sp.service_id)
	 		AND (sp.prescription_id = ei.item_id::integer)
			AND (hli.interface_name = ei.interface_name)
			AND (hli.conducting_doctor_mandatory != 'Y' OR coalesce(bc.payee_doctor_id, '') != '')
			AND (b.payment_status = 'P' OR b.bill_type = 'C')

		};
	
	$self->{'log'}->info("Updating Bill Paid : ");
	$self->{'log'}->debug("\n\t\t\t $sql");
	my $sth = $dbh->prepare($sql);
	$sth->execute();
	$self->{'log'}->info("No of bill paid  = ". $sth->rows);
	return $sth->rows();
}




##################################################
sub getOrderSql {
##################################################

	my $self = shift;
	
	my ($ifName) = @_;

	my $sql = qq{
		SELECT ei.*, sp.patient_id as patient_id, sp.prescription_id as prescribed_id, s.service_name, s.hl7_export_code,
            s.service_id, to_char(sp.presc_date, 'YYYYMMDDhh24mi') as presc_date, sp.common_order_id,
	    to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, to_char(coalesce(sched_equipment.appointment_time, 
		sp.presc_date), 'YYYYMMDDhh24miss') as start_date_time,
            to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi')
                as scheduled_time_end, COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
                COALESCE(pd.mr_no) as mr_no,pd.oldmrno,
			pd.last_name as last_name,
			COALESCE(pd.patient_name,'') as patient_name, sm.salutation,
			COALESCE(pd.patient_gender) as patient_gender,
           to_char(coalesce(pd.dateofbirth, (pd.expected_dob + interval '6 months')::date), 'YYYYMMDD')
				as expected_dob,
            COALESCE(pd.patient_address,'') as patient_address,
			COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') as city_code,
			COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') as state_code,
			COALESCE(cnm.country_name,'') as country_name, COALESCE(cnm.country_code,'') as country_code,
			COALESCE(pd.patient_phone) AS patient_phone,
            to_char(COALESCE(pr.reg_date),'YYYYMMDD') as reg_date,
			pr.doctor, substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name,
			pr.reference_docto_id,pr.visit_type, 'SERV' AS conduction_format,
			substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
            	COALESCE(pr.center_id) AS center_id, COALESCE(hcm.center_name) AS center_name,
			con_doc.doctor_id as conducting_doctor_id,con_doc.doctor_name as conducting_doctor_name,
			presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name, 
			presc_doc_dept.dept_name as prescription_department, COALESCE(drs.doctor_license_number, rd.clinician_id) AS referral_license_number, con_doc.doctor_license_number AS conducting_doctor_license_number,
			dr.doctor_license_number AS admit_doctor_license_number, presc_doc.doctor_license_number AS presc_doctor_license_number,
			COALESCE(psp.service_remarks, sp.remarks, '') as remarks
		FROM hl7_export_items ei
			JOIN services_prescribed sp ON (sp.prescription_id = ei.item_id::integer)
			LEFT JOIN patient_activities pa ON (pa.order_no = sp.prescription_id)
			LEFT JOIN patient_service_prescriptions psp ON (psp.op_service_pres_id = pa.prescription_id)
			JOIN services s ON (s.service_id = sp.service_id)
			LEFT JOIN patient_registration pr ON (pr.patient_id = sp.patient_id)
			LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
			LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
			LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
			LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
			LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
			LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
			LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
			LEFT JOIN city ci ON pd.patient_city = ci.city_id
			LEFT JOIN state_master st ON pd.patient_state = st.state_id
			LEFT JOIN country_master cnm ON pd.country = cnm.country_id
			LEFT JOIN bill_activity_charge bac ON (bac.activity_id = sp.prescription_id::text and activity_code = 'SER')
			LEFT JOIN bill_charge bc ON (bc.charge_id = bac.charge_id)
			LEFT JOIN doctors con_doc ON (con_doc.doctor_id = bc.payee_doctor_id)
			LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = sp.doctor_id)
			LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
			LEFT JOIN (
					SELECT sch.appointment_id, sch.appointment_time, sch.duration, schi.resource_id as eq_id, srm.hl7_export_code
					FROM scheduler_appointments sch
					LEFT JOIN scheduler_appointment_items schi 
						ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'SRID')
					LEFT JOIN service_resource_master srm ON (srm.serv_res_id::text = schi.resource_id)
				)  as sched_equipment ON (sp.appointment_id = sched_equipment.appointment_id)
			LEFT JOIN (
					SELECT center_id, hl7_export_code, serv_res_id, serv_dept_id
					FROM service_resource_master WHERE hl7_export_code != ''
				) AS dept_equipment ON (dept_equipment.serv_dept_id = s.serv_dept_id AND dept_equipment.center_id = pr.center_id)
		WHERE ei.bill_paid = 'Y' 
				AND ei.export_status IN ('N','F') 
				AND ei.item_type = 'SERVICE'
				AND ei.interface_name=?};

	return $sql;
}

##################################################
sub getOrderMessage {
##################################################

	my $self = shift;
	my ($order, $interface) = @_;

	my $dbh = $self->{'db'};
	my $msgId = ($dbh->selectrow_array(qq{SELECT nextval('hl7_msgid_sequence') AS hl7_msg_id}))[0];
	
	my $msg = new Hl7::Message(\%ORM_MSH);
	$msg->{MSH}{controlId} = $msgId;
	$msg->{MSH}{recvApp} = $interface->{sending_app};
	$msg->{MSH}{recvFac} = $interface->{sending_facility};
	my $sendPidPv1ForCancel = $interface->{send_pidpv1_for_cancel};	


	if ($order->{center_id} != 0) {
		$msg->{MSH}{sendFac} = $order->{center_name};
	}

	# Order cancellations do not require a PID / PV1

	if ($order->{op_code} ne 'C' || $sendPidPv1ForCancel eq 'Y' ) {
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
		if ($order->{oldmrno}) {
			$pid->{'pidList~'} = [[$order->{oldmrno}, 'Alternate']];
		}
		$msg->addSegment($pid);

		# Add PV1
		my $pv = new Hl7::Segment('PV1', {
				sid=>1,  visitNum => $order->{patient_id},
				admitDate=>$order->{reg_date}, assgndLoc=> [$order->{prescription_department}||'', $order->{ward_name}||'', $order->{bed_type}]
			});

		if($order->{visit_type} eq 'o') {
			$pv->{patientClass} = 'O';
		}else {
			$pv->{patientClass} = 'I';
		}
		if ($interface->{doctor_identifier} eq 'I') {
			if ($order->{refdoctorname}) {
				$pv->{refDoctor} = [$order->{reference_docto_id}, $order->{refdoctorname}];
			}
	
			if ($order->{doctor_name}) {
				$pv->{admitDoctor} = [$order->{doctor}, $order->{doctor_name}];
			}
			
			if(defined($order->{conducting_doctor_id})) {
				$pv->{attdDoctor} = [$order->{conducting_doctor_id},$order->{conducting_doctor_name}];
			}
		} else {
			if ($order->{referral_license_number}) {
				$pv->{refDoctor} = [$order->{referral_license_number}, $order->{refdoctorname}];
			}
	
			if ($order->{admit_doctor_license_number}) {
				$pv->{admitDoctor} = [$order->{admit_doctor_license_number}, $order->{doctor_name}];
			}
			
			if(defined($order->{conducting_doctor_license_number})) {
				$pv->{attdDoctor} = [$order->{conducting_doctor_license_number},$order->{conducting_doctor_name}];
			}
		}
		
	    $msg->addSegment($pv);
	}

	# Add ORC - placerOrderNum should be separated by dot(.) instead of with ampersand(&).
	my $orderNum = $order->{prescribed_id};
#	my $format = $order->{conduction_format};
#	if ($format eq 'V' && defined($order->{resultlabel_id})) {
#		$orderNum .= ".". $order->{resultlabel_id};
#	}
#	if (defined($order->{sample_sno})) {
#		$orderNum .= ".". $order->{sample_sno};
#	}

	my $ctrlCode = undef;

	if($order->{op_code} eq 'N') {
		$ctrlCode = 'NW';
	}
	
	if($order->{op_code} eq 'C') {
		$ctrlCode = 'CA';
	}

	if($order->{op_code} eq 'U') {
		$ctrlCode = 'XO';
	}
	


	my $orc = new Hl7::Segment('ORC', {ctrl=>$ctrlCode, placerOrderNum=>[$orderNum, 'InstaHMS'],
		placerGrpNum=>$order->{common_order_id} });

	$msg->addSegment($orc);

	# Order cancellations do not have any OBR segments
		# Add OBR
	my $serviceId = $order->{hl7_export_code};
	if (!defined($serviceId) || $serviceId eq '') {
		$serviceId = $order->{service_id};
	}

#	if ($format eq 'V' && defined($order->{resultlabel_id})) {
#		$serviceId = $order->{resultlabel_id};
#	}

	my $scheduledStart = $order->{scheduled_time_start} || $order->{presc_date};
	my $obr = new Hl7::Segment('OBR', {sid=>1,
			serviceId=>[$serviceId, $order->{service_name},
			'InstaHMS', '', $order->{equipment_code} || ''],
			placerOrderNum=>[$orderNum, 'InstaHMS'],fillerOrderNum=>$orderNum,
			reqTime=>$order->{presc_date}, specimenAction=>'O', specimenTime=>$order->{sample_date},
#			reason=>$order->{diagnosis},
			diagServId=>$order->{equipment_code},
			clinicalInfo=>$order->{remarks}
		});
	if (defined($order->{scheduled_time_start})) {
		$obr->{qtyTiming} = [1,'','',$scheduledStart, $order->{scheduled_time_end}];
		$self->{'log'}->info("scheduled start time : $scheduledStart, $order->{scheduled_time_start}, $order->{presc_date}");
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
	
	if ($interface->{doctor_identifier} eq 'I') {
		if(defined($order->{prescription_doctor_id})) {
			$obr->{ordProvider} = [$order->{prescription_doctor_id},$order->{prescription_doctor_name}];
		}
	} else {
		if(defined($order->{presc_doctor_license_number})) {
			$obr->{ordProvider} = [$order->{presc_doctor_license_number},$order->{prescription_doctor_name}];
		}	
	}

	$msg->addSegment($obr);
	my $nte1 = new Hl7::Segment('NTE', {sid=>1, comment=>$order->{remarks}});
	$msg->addSegment($nte1);
	getVisitVitalsDetails($order, $dbh, $msg);
	
	return $msg;
}

sub visitVitalSql {
	my $vitalsQuery = qq {
		SELECT vv.patient_id, to_char(vv.date_time, 'YYYYMMDDhh24mi') date_time, vr.vital_reading_id, vr.param_value, vr.param_remarks, vpm.param_id, vpm.param_label,
		vpm.param_uom, vpm.param_container 
		FROM visit_vitals as vv
		JOIN vital_reading vr ON (vv.vital_reading_id = vr.vital_reading_id)
		JOIN vital_parameter_master vpm ON (vr.param_id = vpm.param_id AND vpm.param_id in ('5', '6'))
		WHERE patient_id = ? ORDER by date_time desc limit 2;
	};
	return $vitalsQuery;
}

sub getVisitVitalsDetails {

	my ($order, $dbh, $msg) = @_;
	my $visit_vitals_sql = visitVitalSql();
	my $visit_vitals = $dbh->selectall_arrayref($visit_vitals_sql, {Slice=>{}}, $order->{patient_id});
	my $count = 1;
	foreach my $vitals (@$visit_vitals) {
		my $obx = new Hl7::Segment('OBX', {sid=>$count++, valueType=>'ST', obsId=>[$vitals->{param_id}, $vitals->{param_label}],
			obsValue=>$vitals->{param_value}, units=>$vitals->{param_uom}, obsTime=>$vitals->{date_time}});
		$msg->addSegment($obx);
		my $nte1 = new Hl7::Segment('NTE', {sid=>1, comment=>$vitals->{param_remarks}});
		$msg->addSegment($nte1);
	}
}

1

