#!/usr/bin/perl -w
package Insta::HL7Module::LIS;
use strict;
use parent ("Insta::HL7Module");

sub name { "'LIS','RIS'" };
sub exportType { "'F','S','B'"};


##################################################
sub getMSH {
##################################################
	my $this = shift;
	##########################################################
	#**Standard message header attributes for all ORM exports
	##########################################################
	my %ORM_MSH = (
		sendApp=>'InstaHMS', sendFac=>'Diag',
		msgType=>['ORM','O01'], procId=>'P', ver=>'2.3'
	);
	return %ORM_MSH;
}


##################################################
sub reportNonExportableOrders {
##################################################
	my $this = shift;
	my $dbh = $this->{"db"};

	my $nonExportableOrders = $dbh->selectall_arrayref(qq{SELECT item_id, interface_name FROM hl7_export_items WHERE bill_paid = 'N' });
	if (@$nonExportableOrders >0) {
		$this->{'log'}->debug("Reporting non-exportable orders");
		$this->{'log'}->debug("The following items are not exported because one of the following reasons: 
			Sample not collected, Bill not yet paid OR Conducting doctor setting mismatch between item and interface level.");
	}
	foreach my $nonExportableOrder (@$nonExportableOrders) {
		my ($item_id, $interface_name) = @$nonExportableOrder;
		$this->{'log'}->debug("Item id $item_id For the interface $interface_name");
	}	

}
##################################################
sub updateExportableOrders {
##################################################
	my $this = shift;
	my $dbh = $this->{"db"};

	# Update the bill_paid (actually ready_for_export) status for existing rows in hl7_export_items.
	# This will take effect in case the bill was paid, or sample collected after the order was placed,
	# for orders already present in hl7_export_items.
	#
	# todo: rename bill_paid to ready_for_export since this considers sample collection as well
	# todo: need updated_ts when we actually set it to ready for export.
	
	my $sql = qq{
		UPDATE hl7_export_items ei SET bill_paid = 'Y'
		FROM tests_prescribed tp, bill_activity_charge bac, bill_charge bc, bill b, diagnostics d,
		hl7_lab_interfaces hli
		WHERE bill_paid = 'N' AND item_type IN ('TEST', 'TESTTEMPLATE') AND ei.export_status = 'N'
			AND (b.payment_status = 'P' OR b.bill_type = 'C') AND (d.sample_needed = 'n' OR tp.sflag = '1')
			AND (bac.activity_id = tp.prescribed_id::text AND bac.activity_code = 'DIA')
			AND (bc.charge_id = bac.charge_id)
			AND (b.bill_no = bc.bill_no)
			AND (d.test_id = tp.test_id)
			AND (tp.prescribed_id = ei.item_id::integer)
			AND (hli.hl7_lab_interface_id = ei.hl7_lab_interface_id)
			AND ((hli.conducting_doctor_mandatory = 'N')
				OR (COALESCE(bac.doctor_id, '') <> '' AND hli.conducting_doctor_mandatory = 'Y')
				)
		};
	$this->{'log'}->info("Updating Bill Paid : ");
	$this->{'log'}->debug("\n\t\t\t $sql");
	my $sth = $dbh->prepare($sql);
	$sth->execute();
	$this->{'log'}->info("No of bill paid  = ". $sth->rows);
	
	return $sth->rows();
}

##################################################
sub getOrderSql {
##################################################
	my $self = shift;
	my ($send_orm) = @_;
	my $sql = undef;
	if (defined($send_orm) && ($send_orm eq 'T')) {
		$sql = qq{
			SELECT ei.*, tp.pat_id as patient_id, tp.prescribed_id, d.test_name, d.hl7_export_code, d.diag_code,
	            d.test_id, to_char(tp.pres_date, 'YYYYMMDDhh24mi') as pres_date, tp.common_order_id, tp.clinical_notes,
		    to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, to_char(coalesce(sched_equipment.appointment_time, tp.pres_date), 'YYYYMMDDhh24miss') as start_date_time,
	            to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi')
	                as scheduled_time_end, COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
	            COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_sno, to_char(sc.sample_date, 'YYYYMMDDhh24missms') as sample_date,
	            sc.sample_type_id as sample_type_id, dd.category,
	            COALESCE(pd.mr_no,isr.incoming_visit_id) as mr_no, pd.oldmrno,
				COALESCE(pd.last_name, '') as last_name, COALESCE(pd.middle_name, '') as middle_name,
				COALESCE(pd.patient_name,isr.patient_name) as patient_name, sm.salutation,
				COALESCE(pd.patient_gender,isr.patient_gender) as patient_gender,
				to_char(coalesce(pd.dateofbirth,pd.expected_dob, isr.isr_dateofbirth, 
				(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age * 365.25)::integer
				WHEN isr.age_unit = 'M' THEN (isr.patient_age * 30.43)::integer ELSE (isr.patient_age*1) END) )), 'YYYYMMDD') as expected_dob,
	            COALESCE(pd.patient_address,isr.address,'') as patient_address,
				COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') as city_code,
				COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') as state_code,
				COALESCE(cnm.country_name,'') as country_name, COALESCE(cnm.country_code,'') as country_code,
				COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	            to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') as reg_date,
				pr.doctor, substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name,
				pr.reference_docto_id, mrd.icd_code as diagnosis_code, mrd.description as diagnosis,
				CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type,
				substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	            d.conduction_format, COALESCE(pr.center_id,isr.center_id) AS center_id, COALESCE(hcm.center_name,hcm_isr.center_name) AS center_name,
				cd.doctor_id as conducting_doctor_id,cd.doctor_name as conducting_doctor_name, COALESCE(tp.coll_prescribed_id, tp.prescribed_id) AS coll_prescribedid,
				presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name,
				presc_doc_dept.dept_name as prescription_department, pd.email_id as patient_email_id, stype.sample_type, hci.hl7_center_interface_id,
				tp.priority, COALESCE(drs.doctor_license_number, rd.clinician_id) AS referral_license_number, cd.doctor_license_number AS conducting_doctor_license_number,
				dr.doctor_license_number AS admit_doctor_license_number, presc_doc.doctor_license_number AS presc_doctor_license_number,
				COALESCE(ptp.clinical_note_for_conduction, '') as clinical_note_for_conduction, COALESCE(ptp.clinical_justification_for_item, '') as clinical_justification_for_item,
				to_char(coalesce(sc.sample_date,tp.pres_date), 'YYYYMMDDHH24MISS') AS sample_or_order_date
			FROM hl7_export_items ei
				JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
				LEFT JOIN patient_test_prescriptions ptp ON (tp.doc_presc_id = ptp.op_test_pres_id)
				LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
				JOIN diagnostics d ON (d.test_id = tp.test_id)
				JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)
				JOIN hl7_center_interfaces hci ON (hci.hl7_lab_interface_id = ei.hl7_lab_interface_id)
				LEFT JOIN sample_type stype ON (stype.sample_type_id = COALESCE(sc.sample_type_id, d.sample_type_id))
				LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
				LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = tp.pat_id AND diag_type = 'P')
				LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
				LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
				LEFT JOIN hospital_center_master hcm_isr ON (hcm_isr.center_id = isr.center_id)
				LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
				LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
				LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
				LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
				LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
				LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
				LEFT JOIN city ci ON pd.patient_city = ci.city_id
				LEFT JOIN state_master st ON pd.patient_state = st.state_id
				LEFT JOIN country_master cnm ON pd.country = cnm.country_id
				LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text and activity_code = 'DIA')
				LEFT JOIN doctors cd ON (cd.doctor_id = bac.doctor_id)
				LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = tp.pres_doctor)
				LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
				LEFT JOIN
					(SELECT sch.appointment_id, sch.appointment_time, sch.duration,
						schi.resource_id as eq_id, tem.hl7_export_code
					FROM scheduler_appointments sch
					LEFT JOIN scheduler_appointment_items schi
						ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'EQID')
					LEFT JOIN test_equipment_master tem
						ON (tem.eq_id::text = schi.resource_id))
					AS sched_equipment ON (tp.appointment_id = sched_equipment.appointment_id)
				LEFT JOIN
					(SELECT DISTINCT ON (center_id, ddept_id) center_id, ddept_id, hl7_export_code
						FROM test_equipment_master WHERE hl7_export_code != '' AND status = 'A')
					AS dept_equipment ON (dept_equipment.ddept_id = d.ddept_id AND
	                                   dept_equipment.center_id = COALESCE(pr.center_id, isr.center_id))
			WHERE ei.bill_paid = 'Y' 
					AND ei.export_status IN ('N','F') 
					AND ei.item_type = 'TEST'
					AND (d.sample_needed = 'n' OR sc.sample_receive_status = 'R')
					AND ei.hl7_lab_interface_id = ?
					AND ((COALESCE(pr.center_id, isr.center_id) = hci.center_id) OR (0 = hci.center_id))
					AND ((hci.center_id = ?) OR (0 = hci.center_id)) ORDER BY ei.export_id	
			};
	} else {
		$sql = qq{
			SELECT ei.*, tp.pat_id as patient_id, tp.prescribed_id, d.test_name, d.hl7_export_code, d.diag_code,
	            d.test_id, to_char(tp.pres_date, 'YYYYMMDDhh24mi') as pres_date, tp.common_order_id, tp.clinical_notes,
		    to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, to_char(coalesce(sched_equipment.appointment_time, tp.pres_date), 'YYYYMMDDhh24miss') as start_date_time,
	            to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi')
	                as scheduled_time_end, COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
	            COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_sno, to_char(sc.sample_date, 'YYYYMMDDhh24missms') as sample_date,
	            sc.sample_type_id as sample_type_id, dd.category,
	            COALESCE(pd.mr_no,isr.incoming_visit_id) as mr_no, pd.oldmrno,
				COALESCE(pd.last_name, '') as last_name, COALESCE(pd.middle_name, '') as middle_name,
				COALESCE(pd.patient_name,isr.patient_name) as patient_name, sm.salutation,
				COALESCE(pd.patient_gender,isr.patient_gender) as patient_gender,
				to_char(coalesce(pd.dateofbirth,pd.expected_dob, isr.isr_dateofbirth, 
				(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
				WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )), 'YYYYMMDD') as expected_dob,
	            COALESCE(pd.patient_address,isr.address,'') as patient_address,
				COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') as city_code,
				COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') as state_code,
				COALESCE(cnm.country_name,'') as country_name, COALESCE(cnm.country_code,'') as country_code,
				COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
	            to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') as reg_date,
				pr.doctor, substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name,
				pr.reference_docto_id, mrd.icd_code as diagnosis_code, mrd.description as diagnosis,
				CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type,
				substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
	            d.conduction_format, COALESCE(tm.hl7_export_code ,tm.resultlabel_id::character varying) as resultlabel_id, tm.resultlabel,
				COALESCE(pr.center_id,isr.center_id) AS center_id, COALESCE(hcm.center_name,hcm_isr.center_name) AS center_name,
				cd.doctor_id as conducting_doctor_id,cd.doctor_name as conducting_doctor_name, COALESCE(tp.coll_prescribed_id, tp.prescribed_id) AS coll_prescribedid,
				presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name, 
				presc_doc_dept.dept_name as prescription_department, pd.email_id as patient_email_id, hci.hl7_center_interface_id,
				tp.priority, COALESCE(drs.doctor_license_number, rd.clinician_id) AS referral_license_number, cd.doctor_license_number AS conducting_doctor_license_number,
				dr.doctor_license_number AS admit_doctor_license_number, presc_doc.doctor_license_number AS presc_doctor_license_number,
				COALESCE(ptp.clinical_note_for_conduction, '') as clinical_note_for_conduction, COALESCE(ptp.clinical_justification_for_item, '') as clinical_justification_for_item,
				to_char(coalesce(sc.sample_date,tp.pres_date), 'YYYYMMDDHH24MISS') AS sample_or_order_date
			FROM hl7_export_items ei
				JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
				LEFT JOIN patient_test_prescriptions ptp ON (tp.doc_presc_id = ptp.op_test_pres_id)
				LEFT JOIN test_results_master tm ON (tm.test_id = tp.test_id)
				LEFT JOIN test_results_center trc on (tm.resultlabel_id = trc.resultlabel_id)
				LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
				JOIN diagnostics d ON (d.test_id = tp.test_id)
				JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)
				JOIN hl7_center_interfaces hci ON (hci.hl7_lab_interface_id = ei.hl7_lab_interface_id)
				LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
				LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = tp.pat_id AND diag_type = 'P')
				LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
				LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
				LEFT JOIN hospital_center_master hcm_isr ON (hcm_isr.center_id = isr.center_id)
				LEFT JOIN hospital_center_master hcm_trc on (trc.center_id = hcm_trc.center_id)
				LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
				LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
				LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
				LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
				LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
				LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
				LEFT JOIN city ci ON pd.patient_city = ci.city_id
				LEFT JOIN state_master st ON pd.patient_state = st.state_id
				LEFT JOIN country_master cnm ON pd.country = cnm.country_id
				LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text and activity_code = 'DIA')
				LEFT JOIN doctors cd ON (cd.doctor_id = bac.doctor_id)
				LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = tp.pres_doctor)
				LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
				LEFT JOIN
					(SELECT sch.appointment_id, sch.appointment_time, sch.duration,
						schi.resource_id as eq_id, tem.hl7_export_code
					FROM scheduler_appointments sch
					LEFT JOIN scheduler_appointment_items schi
						ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'EQID')
					LEFT JOIN test_equipment_master tem
						ON (tem.eq_id::text = schi.resource_id))
					AS sched_equipment ON (tp.appointment_id = sched_equipment.appointment_id)
				LEFT JOIN
					(SELECT DISTINCT ON (center_id, ddept_id) center_id, ddept_id, hl7_export_code
						FROM test_equipment_master WHERE hl7_export_code != '' AND status = 'A')
					AS dept_equipment ON (dept_equipment.ddept_id = d.ddept_id AND
	                                   dept_equipment.center_id = COALESCE(pr.center_id, isr.center_id))
			WHERE ei.bill_paid = 'Y' 
					AND ei.export_status IN ('N','F') 
					AND ei.item_type = 'TEST'
					AND (trc.center_id is null or trc.center_id='0' or trc.center_id in (pr.center_id,isr.center_id))
					AND (trc.status = 'A' or trc.status is null)
					AND (d.sample_needed = 'n' OR sc.sample_receive_status = 'R')
					AND ei.hl7_lab_interface_id=?
					AND ((COALESCE(pr.center_id, isr.center_id) = hci.center_id) OR (0 = hci.center_id))
					AND ((hci.center_id = ?) OR (0 = hci.center_id)) ORDER BY ei.export_id
				};
			
			}
		

		return $sql;
	}
	
##################################################
sub getOrderSqlForTemplate {
##################################################
	my $sql = qq{
		SELECT ei.*, tp.pat_id as patient_id, tp.prescribed_id, d.test_name, d.hl7_export_code, d.diag_code,
            d.test_id, to_char(tp.pres_date, 'YYYYMMDDhh24mi') as pres_date, tp.common_order_id, tp.clinical_notes,
	    to_char(sched_equipment.appointment_time, 'YYYYMMDDhh24mi') as scheduled_time_start, to_char(coalesce(sched_equipment.appointment_time, tp.pres_date), 'YYYYMMDDhh24miss') as start_date_time,
            to_char(sched_equipment.appointment_time + (sched_equipment.duration::text||'mins')::interval, 'YYYYMMDDhh24mi')
                as scheduled_time_end, COALESCE(sched_equipment.hl7_export_code, dept_equipment.hl7_export_code) as equipment_code,
            COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_sno, to_char(sc.sample_date, 'YYYYMMDDhh24missms') as sample_date,
            sc.sample_type_id as sample_type_id, dd.category,
            COALESCE(pd.mr_no,isr.incoming_visit_id) as mr_no, pd.oldmrno,
			COALESCE(pd.last_name, '') as last_name, COALESCE(pd.middle_name, '') as middle_name,
			COALESCE(pd.patient_name,isr.patient_name) as patient_name, sm.salutation,
			COALESCE(pd.patient_gender,isr.patient_gender) as patient_gender,
			to_char(coalesce(pd.dateofbirth,pd.expected_dob, isr.isr_dateofbirth, 
			(current_date-(CASE WHEN isr.age_unit = 'Y' THEN (isr.patient_age*365.25)::integer
			WHEN isr.age_unit = 'M' THEN (isr.patient_age*30.43)::integer ELSE isr.patient_age*1 END) )), 'YYYYMMDD') as expected_dob,
            COALESCE(pd.patient_address,isr.address,'') as patient_address,
			COALESCE(ci.city_name,'') AS cityname, COALESCE(substring(ci.city_id, 4, 3),'') as city_code,
			COALESCE(st.state_name,'') AS statename, COALESCE(substring(st.state_id, 4, 3),'') as state_code,
			COALESCE(cnm.country_name,'') as country_name, COALESCE(cnm.country_code,'') as country_code,
			COALESCE(pd.patient_phone, isr.phone_no) AS patient_phone,
            to_char(COALESCE(pr.reg_date, isr.date), 'YYYYMMDD') as reg_date,
			pr.doctor, substring(dr.doctor_name,1,50) as doctor_name, pr.bed_type, wn.ward_name,
			pr.reference_docto_id, mrd.icd_code as diagnosis_code, mrd.description as diagnosis,
			CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type,
			substring(COALESCE(drs.doctor_name, rd.referal_name),1,50) AS refdoctorname,
            d.conduction_format, '' as resultlabel_id, '' as resultlabel,
			COALESCE(pr.center_id,isr.center_id) AS center_id, COALESCE(hcm.center_name,hcm_isr.center_name) AS center_name,
			cd.doctor_id as conducting_doctor_id,cd.doctor_name as conducting_doctor_name, COALESCE(tp.coll_prescribed_id, tp.prescribed_id) AS coll_prescribedid,
			presc_doc.doctor_id as prescription_doctor_id, presc_doc.doctor_name as prescription_doctor_name, 
			presc_doc_dept.dept_name as prescription_department, pd.email_id as patient_email_id, hci.hl7_center_interface_id,
			tp.priority, COALESCE(drs.doctor_license_number, rd.clinician_id) AS referral_license_number, cd.doctor_license_number AS conducting_doctor_license_number,
			dr.doctor_license_number AS admit_doctor_license_number, presc_doc.doctor_license_number AS presc_doctor_license_number,
			COALESCE(ptp.clinical_note_for_conduction, '') as clinical_note_for_conduction, COALESCE(ptp.clinical_justification_for_item, '') as clinical_justification_for_item,
			to_char(coalesce(sc.sample_date,tp.pres_date), 'YYYYMMDDHH24MISS') AS sample_or_order_date
		FROM hl7_export_items ei
			JOIN tests_prescribed tp ON (tp.prescribed_id = ei.item_id::integer)
			LEFT JOIN patient_test_prescriptions ptp ON (tp.doc_presc_id = ptp.op_test_pres_id)
			LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)
			JOIN diagnostics d ON (d.test_id = tp.test_id)
			JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)
			JOIN hl7_center_interfaces hci ON (hci.hl7_lab_interface_id = ei.hl7_lab_interface_id)
			LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
			LEFT JOIN mrd_diagnosis mrd ON (mrd.visit_id = tp.pat_id AND diag_type = 'P')
			LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)
			LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id)
			LEFT JOIN hospital_center_master hcm_isr ON (hcm_isr.center_id = isr.center_id)
			LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)
			LEFT JOIN doctors drs ON pr.reference_docto_id = drs.doctor_id
			LEFT JOIN referral rd ON pr.reference_docto_id = rd.referal_no
			LEFT JOIN doctors dr ON dr.doctor_id = pr.doctor
			LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)
			LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)
			LEFT JOIN city ci ON pd.patient_city = ci.city_id
			LEFT JOIN state_master st ON pd.patient_state = st.state_id
			LEFT JOIN country_master cnm ON pd.country = cnm.country_id
			LEFT JOIN bill_activity_charge bac ON (bac.activity_id = tp.prescribed_id::text and activity_code = 'DIA')
			LEFT JOIN doctors cd ON (cd.doctor_id = bac.doctor_id)
			LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = tp.pres_doctor)
			LEFT JOIN department presc_doc_dept ON (presc_doc_dept.dept_id = presc_doc.dept_id)
			LEFT JOIN
				(SELECT sch.appointment_id, sch.appointment_time, sch.duration,
					schi.resource_id as eq_id, tem.hl7_export_code
				FROM scheduler_appointments sch
				LEFT JOIN scheduler_appointment_items schi
					ON (sch.appointment_id = schi.appointment_id AND schi.resource_type = 'EQID')
				LEFT JOIN test_equipment_master tem
					ON (tem.eq_id::text = schi.resource_id))
				AS sched_equipment ON (tp.appointment_id = sched_equipment.appointment_id)
			LEFT JOIN
				(SELECT DISTINCT ON (center_id, ddept_id) center_id, ddept_id, hl7_export_code
					FROM test_equipment_master WHERE hl7_export_code != '' AND status = 'A')
				AS dept_equipment ON (dept_equipment.ddept_id = d.ddept_id AND
                                   dept_equipment.center_id = COALESCE(pr.center_id, isr.center_id))
		WHERE ei.bill_paid = 'Y' 
				AND ei.export_status IN ('N','F') 
				AND ei.item_type = 'TESTTEMPLATE'
				AND (d.sample_needed = 'n' OR sc.sample_receive_status = 'R')
				AND ei.hl7_lab_interface_id=?
				AND ((COALESCE(pr.center_id, isr.center_id) = hci.center_id) OR (0 = hci.center_id))
				AND ((hci.center_id = ?) OR (0 = hci.center_id)) ORDER BY ei.export_id
			};

		return $sql;
	}


##################################################
sub getOrderMessage {
##################################################
	my $self = shift;
	my ($order, $interface) = @_;
	my $dbh = $self->{'db'};
	my $msgId = ($dbh->selectrow_array(qq{SELECT nextval('hl7_msgid_sequence') AS hl7_msg_id}))[0];
	my %ORM_MSH = $self->getMSH();
	my $msg = new Hl7::Message(\%ORM_MSH);
	$msg->{MSH}{controlId} = $msgId;
	$msg->{MSH}{recvApp} = [$interface->{sending_app}, $interface->{hl7_lab_interface_id}];
	$msg->{MSH}{recvFac} = $interface->{sending_facility};
	my $send_orm = $interface->{send_orm};

	my $sendOrc4 = $interface->{send_orc4};
	my $sendPidPv1ForCancel = $interface->{send_pidpv1_for_cancel};

	if ($order->{center_id} != 0) {
		$msg->{MSH}{sendFac} = $order->{center_name};
	}

	# Order cancellations do not require a PID / PV1 based on interface preferences

	if ($order->{op_code} ne 'C' || $sendPidPv1ForCancel eq 'Y') {
		# Add PID
		my ($line1, $line2) = split('\r{0,1}\n', $order->{patient_address});
		my $pid = new Hl7::Segment('PID', {
				sid=> 1,
				pidList=>[$order->{mr_no},'InstaHMS'],
				name=> [$order->{last_name} || '-', $order->{patient_name}, $order->{middle_name}, '', $order->{salutation}],
				dob=> $order->{expected_dob}, sex=> $order->{patient_gender},
				addr=> [$line1 || '', $line2 || '', $order->{cityname}, $order->{state_code}, '',
					$order->{country_code} || $order->{country_name}],
				phHome => [$order->{patient_phone},'','',$order->{patient_email_id}]
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
		} elsif($order->{visit_type} eq 't') {
			$pv->{patientClass} = 't';
		} else {
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
	
	if ($interface->{'send_insurance_segments'} eq 'Y') {
		addInsuranceSegments($order, $dbh, $msg);
	}
	
	# Add ORC - placerOrderNum should be separated by dot(.) instead of with ampersand(&).
	my $orderNum = $order->{prescribed_id};
	my $format = $order->{conduction_format};
	if ($format eq 'V' && defined($order->{resultlabel_id})) {
  		$orderNum .= ".". $order->{resultlabel_id};
  	}	
	if (defined($order->{sample_sno})) {
		$orderNum .= ".". $order->{sample_sno};
 	}
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

	# Check whether ORC4 need to be send or not based on interface master
	my $grpNum;
	
	if ($sendOrc4 eq 'Y') {
	
		$grpNum = $order->{common_order_id};
	} 
	
	my $orc = new Hl7::Segment('ORC', {ctrl=>$ctrlCode, placerOrderNum=>[$orderNum, $format], fillerOrderNum=>$orderNum,
		 placerGrpNum=>$grpNum, qty=>['','','','','',$order->{priority}], timest=>$order->{sample_or_order_date}});

	$msg->addSegment($orc);

	# Order cancellations do not have any OBR segments
		# Add OBR
	my $serviceId = $order->{hl7_export_code};
	if (!defined($serviceId) || $serviceId eq '') {
		$serviceId = $order->{test_id};
	}
	if ($format eq 'V' && defined($order->{resultlabel_id})) {
		$serviceId = $order->{resultlabel_id};
	}
	if (defined($send_orm) && $send_orm eq 'T') {
		if($order->{hl7_export_code} && length($order->{hl7_export_code}) > 0) {
			$serviceId = $order->{hl7_export_code};
		}else {
			$serviceId = $order->{test_id};
		}
	}

	my $scheduledStart = $order->{scheduled_time_start} || $order->{pres_date};
	my $clinicalNotes = $order->{clinical_notes};
	$clinicalNotes =~ s/\n/ /g;
	$clinicalNotes =~ s/\r//g;
	my $obr = new Hl7::Segment('OBR', {sid=>1,
			serviceId=>[$serviceId, $order->{resultlabel}||$order->{test_name},
			'InstaHMS', '', $order->{equipment_code} || ''],
			placerOrderNum=>[$orderNum, $format],fillerOrderNum=>$orderNum,
			reqTime=>$order->{pres_date}, specimenAction=>'O', specimenTime=>$order->{sample_date},
			reason=>$order->{diagnosis},
			diagServId=>$order->{equipment_code}, clinicalInfo=>$clinicalNotes.'^'.$order->{clinical_justification_for_item}, specimenSource=>$order->{sample_type}
		});
	if (defined($order->{scheduled_time_start})) {
		$obr->{qtyTiming} = [1,'','',$scheduledStart, $order->{scheduled_time_end}];
	}
	if (defined($order->{start_date_time})) {
		$obr->{scheduledDtTime} = $order->{start_date_time};
	}
	
	if (defined($order->{sample_sno})) {
		$obr->{placer1} =  $order->{sample_sno};
	} elsif ($interface->{send_center_id} eq 'Y' && $order->{category} eq 'DEP_RAD') {
		$obr->{placer1} = $order->{center_id};
		$obr->{filler2} = $order->{center_id};
	} else {
		$obr->{placer1} = '01';
		$obr->{filler2} = '01';
	}
	
	if (defined($order->{sample_type_id})) {
		$obr->{placer2} = $order->{sample_type_id};
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
	
	my $nte1 = new Hl7::Segment('NTE', {sid=>1, comment=>$order->{clinical_note_for_conduction}});
	$msg->addSegment($nte1);
	my $nte2 = new Hl7::Segment('NTE', {sid=>2, comment=>$order->{clinical_justification_for_item}});
	$msg->addSegment($nte2);
	my $nte3 = new Hl7::Segment('NTE', {sid=>3, comment=>$clinicalNotes});
	$msg->addSegment($nte3);
	
	return $msg;
}

##################################################
sub getInsuranceQuery {
##################################################
	my $sql = qq{
		SELECT od.org_name, picm.insurance_co_name, ptpa.tpa_name, pipm.interface_code AS plan_interface_code, pipm.plan_id, picam.category_name, 
			   pipm.plan_name , pipm.plan_notes, picm.interface_code AS ins_co_interface_code, picm.insurance_co_id,
			   pipm.plan_exclusions, ppip.priority, (case when modact.activation_status = 'Y' then ppd.member_id else ins.policy_no end) as member_id,
			   (case when modact.activation_status = 'Y' then ppd.policy_number else ins.insurance_no end) as policy_number,
			   to_char((case when modact.activation_status = 'Y' then ppd.policy_validity_start else ins.policy_validity_start end), 'YYYYMMDDhh24miss') as policy_validity_start,
			   to_char((case when modact.activation_status = 'Y' then ppd.policy_validity_end else ins.policy_validity_end end), 'YYYYMMDDhh24miss') as policy_validity_end,
			   (case when modact.activation_status = 'Y' then ppd.policy_holder_name else ins.policy_holder_name end) as policy_holder_name,
			   (case when modact.activation_status = 'Y' then ppd.patient_relationship else ins.patient_relationship end) as patient_relationship,
			   picm.insurance_co_address, picm.insurance_co_phone, ppip.plan_limit, tp.pat_id
		FROM  tests_prescribed tp
			  LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)
			  LEFT JOIN organization_details od ON(pr.org_id = od.org_id)
			  LEFT JOIN insurance_case ins ON (pr.insurance_id = ins.insurance_id)
			  LEFT JOIN patient_insurance_plans ppip ON(ppip.patient_id = pr.patient_id AND ppip.priority = 1)
			  LEFT JOIN insurance_plan_main pipm ON(pipm.plan_id = ppip.plan_id)
			  LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id)
			  LEFT JOIN insurance_company_master picm ON(picm.insurance_co_id = pipm.insurance_co_id)
			  LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = ppip.sponsor_id)
			  LEFT JOIN insurance_category_master picam ON (picam.category_id=pipm.category_id)
			  LEFT JOIN modules_activated modact ON (modact.module_id = 'mod_adv_ins')
		WHERE tp.prescribed_id = ? ;
		};

	return $sql;
}

sub getDiagnosisQuery {

	my $diagnosis_query = qq {
		SELECT mrd.icd_code as diagnosis_code, mrd.description as diagnosis 
		FROM mrd_diagnosis mrd
		WHERE mrd.visit_id = ? AND mrd.diag_type IS NOT NULL AND mrd.diag_type != '' ORDER BY diag_type;
	};
	
	return $diagnosis_query;
}


sub addInsuranceSegments {
	my ($order, $dbh, $msg) = @_;
	my $ins_query = getInsuranceQuery();
	my $ins_orders = $dbh->selectall_arrayref($ins_query, {Slice=>{}}, $order->{coll_prescribedid});
	my ($line1, $line2) = split('\r{0,1}\n', $order->{patient_address});
	my %gender_map = (M=>'Male', F=>'Female', O=>'Other', C=>'Couple');
	
	my $visit_id = undef;

	foreach my $ins_order (@$ins_orders) {
		my $external_insurace_id = undef;
		my $external_plan_id = undef;
		
		if (defined ($ins_order->{ins_co_interface_code}) && $ins_order->{ins_co_interface_code} ne '') {
			$external_insurace_id = $ins_order->{ins_co_interface_code};
		} else {
			$external_insurace_id = $ins_order->{insurance_co_id};
		}
		
		if (defined ($ins_order->{plan_interface_code}) && $ins_order->{plan_interface_code} ne '') {
			$external_plan_id = $ins_order->{plan_interface_code};
		} else {
			$external_plan_id = $ins_order->{plan_id};
		}
		
		my $IN1 = new Hl7::Segment('IN1', {sid=>1, insPlanId=>${external_plan_id}, insCoId=>${external_insurace_id}, insCoName=>$ins_order->{insurance_co_name},
						insCoAddr=>[$ins_order->{insurance_co_address}, $ins_order->{insurance_co_city}, $ins_order->{insurance_co_state}, $ins_order->{insurance_co_country}],
						insCoPhNum=>$ins_order->{insurance_co_phone}, 
					    planEffectiveDate=>$ins_order->{policy_validity_start}, planExpireDate=>$ins_order->{policy_validity_end}, 
						insuredName=>[$order->{last_name} || '-', $order->{patient_name}, $order->{middle_name}, '', $order->{salutation}],
						insuredDob=>$order->{expected_dob}, insuredAddr=>[$line1 || '', $line2 || '', $order->{cityname}, $order->{state_code},
						$order->{country_code} || $order->{country_name}], 
						insuredsAdministrativeSex=>$gender_map{$order->{patient_gender}}
					});
		
		my $IN2 = new Hl7::Segment('IN2', {payorId=>$ins_order->{member_id}, patientMemberNum=>$ins_order->{member_id}, insuredsHomePhNum=>$order->{patient_phone}});
					
		$msg->addSegment($IN1);
		$msg->addSegment($IN2);
		$visit_id = $ins_order->{pat_id};
	}
	my $diagnosis_details_list = $dbh->selectall_arrayref(getDiagnosisQuery(), {Slice=>{}}, $visit_id);
	
	my $index = 1;
	foreach my $diagnosis_details (@$diagnosis_details_list) {
		my $DG1 = new Hl7::Segment('DG1', {sid=>$index, diagnosisCode=>$diagnosis_details->{diagnosis_code}, diagnosisDescription=>$diagnosis_details->{diagnosis}});
		$msg->addSegment($DG1);
		
		$index++;
	}
	
}

return 1;
