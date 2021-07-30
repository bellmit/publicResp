package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientConsultationPrescriptionsRepository.
 */
@Repository
public class PatientConsultationPrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient consultation prescriptions repository.
   */
  public PatientConsultationPrescriptionsRepository() {
    super("patient_consultation_prescriptions");
  }

  /** The Constant PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS. */
  private static final String[] PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS = new String[] {
      "(prpa.consultation_id = pp.consultation_id " + " and prpa.visit_id = dc.patient_id"
          + " and prpa.status='A' " //
          + " and pp.patient_presc_id = prpa.patient_pres_id " //
          + " and prpa.preauth_act_item_id= dt.doctor_id " //
          + " and prpa.rem_qty>0) ",
      //

      "(prpa.consultation_id = pp.consultation_id " + " and prpa.visit_id = pp.visit_id "
          + " and prpa.status='A' " //
          + " and pp.patient_presc_id = prpa.patient_pres_id " //
          + " and prpa.preauth_act_item_id= d.doctor_id " //
          + " and prpa.rem_qty>0) "
  }; //

  private static final String PREAUTH_PRESCRIPTION_APPROVAL_DETAILS =
      " LEFT JOIN preauth_request_approval_details prad ON "
      + " (prep.preauth_request_id=prad.preauth_request_id)"
      + " LEFT JOIN scheduler_appointments sa ON (sa.visit_id = pr.patient_id "
      + " AND sa.patient_presc_id = pp.patient_presc_id) ";

  /** The Constant CONSULTATION_PRESCRIPTIONS. */
  private static final String CONSULTATION_PRESCRIPTIONS_WHERE =
      " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)=0 AND pp.visit_id=? "
      + " order by pres_id ";
  /** The Constant CONSULTATION_PRESCRIPTIONS. */
  private static final String CONSULTATION_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE =
      " WHERE pp.status='P' AND pp.consultation_id=? AND cp.presc_activity_type='DOC'"
      + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null) order by pres_id ";

  private static final String PATIENT_CONSULTATION_PRESCRIPTIONS =
      " SELECT dc.patient_id, cp.prescription_id AS pres_id, dt.doctor_id, dt.doctor_name,"
      + " d.doctor_name AS name, cp.doctor_id as cross_cons_doctor_id, "
      + " case when pr.visit_type='o' then -1 when visit_type='i' then -3 end as head,"
      + " cons_remarks as remarks, pp.consultation_id, pp.prescribed_date as prescription_date,"
      + " pp.status as added_to_bill, cp.username, ct.consultation_type, dp.dept_name,"
      + " pp.pri_pre_auth_no, pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no,"
      + " pp.sec_pre_auth_mode_id, pp.item_excluded_from_doctor,"
      + " pp.item_excluded_from_doctor_remarks, pr.primary_sponsor_id as preauth_sponsor_id,"
      + " pr.center_id as center_id, prpa.preauth_id as preauth_number, prpa.preauth_mode,"
      + " prpa.rem_qty as preauth_rem_qty,"
      + " prpa.preauth_act_id as preauth_item_id, prpa.preauth_presc_id,"
      + " date(prad.end_date) as preauth_end_date, prep.preauth_status, "
      + " sa.appointment_time, sa.appointment_status, date(pp.start_datetime) as start_datetime,"
      + " prpa.preauth_act_status, date(pp.prescribed_date) as prescribed_date, "
      + " prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number "
      + " FROM patient_consultation_prescriptions cp "
      + " JOIN patient_prescription pp ON (pp.patient_presc_id=cp.prescription_id) "
      + " JOIN doctor_consultation dc USING (consultation_id)  "
      + " JOIN patient_registration pr ON (pr.patient_id=dc.patient_id) "
      + " JOIN doctors d ON (cp.doctor_id = d.doctor_id)  "
      + " JOIN department dp ON(d.dept_id = dp.dept_id)  "
      + " LEFT JOIN doctors dt ON (dc.doctor_name = dt.doctor_id) "
      + " LEFT JOIN consultation_types ct ON (dc.head = ct.consultation_type_id::text) "
      + " LEFT JOIN preauth_prescription_activities prpa ON "
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS[0]
      + " LEFT JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) ";

  
  private static final String PREAUTH_PRESCRIPTIONS =
      " UNION ALL "
      + " SELECT DISTINCT prpa.visit_id AS patient_id,  0 as pres_id, dt.doctor_id, "
      + " dt.doctor_name, oi.doctor_name as name, prpa.preauth_act_item_id AS cross_cons_doctor_id,"
      + " case when pr.visit_type='o' then -1 when pr.visit_type='i' then -3 end as head,"
      + " prpa.preauth_act_item_remarks  as remarks, prpa.consultation_id, "
      + " prpa.mod_time as prescription_date, "
      + " CASE WHEN prpa.added_to_bill='N' THEN 'O' ELSE 'P' END AS added_to_bill, prpa.username, "
      + " ct.consultation_type, dp.dept_name, null AS pri_pre_auth_no, "
      + " 0 AS pri_pre_auth_mode_id, null AS sec_pre_auth_no, 0 AS sec_pre_auth_mode_id,"
      + " 'N' AS item_excluded_from_doctor, null AS item_excluded_from_doctor_remarks, "
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id, "
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, "
      + " prpa.rem_qty as preauth_rem_qty , prpa.preauth_act_id as preauth_item_id, "
      + " prpa.preauth_presc_id, null::DATE as preauth_end_date, "
      + " prep.preauth_status, NULL::TIMESTAMP AS appointment_time, "
      + " '' AS appointment_status,date(prpa.mod_time) as start_datetime, prpa.preauth_act_status,"
      + " date(prpa.mod_time) as prescribed_date, prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number" 
      + " FROM preauth_prescription_activities prpa "
      + " JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) "
      + " JOIN patient_registration pr ON (prpa.visit_id = pr.patient_id) "
      + " JOIN doctors oi ON (prpa.preauth_act_item_id = oi.doctor_id)  "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=prpa.consultation_id) "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + " LEFT JOIN department dp ON(d.dept_id = dp.dept_id)  "
      + " LEFT JOIN consultation_types ct ON (dc.head = ct.consultation_type_id::text) "
      + " LEFT JOIN doctors dt ON (dc.doctor_name = dt.doctor_id) "
      + "  WHERE prpa.consultation_id = ? AND prpa.rem_qty > 0 "
      + " AND prpa.prescribed_date >= "
      + "  (select current_timestamp - (prescription_validity || ' days ')::interval "
      + "    from generic_preferences)"
      + " AND prpa.patient_pres_id = 0"
      + " AND prpa.preauth_act_type = 'DOC'"
      + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null) ";

  private static final String PATIENT_CONSULTATION_PRESCRIPTIONS_BY_VISIT_ID_WHERE = 
      " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)!=0 AND dc.patient_id=? ";
  private static final String PATIENT_CONSULTATION_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE = 
      " WHERE pp.status='P' AND pp.consultation_id=? "
      + " AND cp.presc_activity_type='DOC' AND (prep.is_cloned = 'N' OR prep.is_cloned is null) ";
  private static final String VISIT_CONSULTATION_PRESCRIPTIONS =
      " UNION ALL " + " SELECT pp.visit_id as patient_id, cp.prescription_id AS pres_id, "
      + " null as doctor_id, null as doctor_name, d.doctor_name AS name, cp.doctor_id as "
      + " cross_cons_doctor_id, case when pr.visit_type='o' then -1 when visit_type='i' then -3 "
      + " end as head, cons_remarks as remarks, pp.consultation_id, pp.prescribed_date as "
      + " prescription_date, pp.status as added_to_bill, cp.username, "
      + " ct.consultation_type, dp.dept_name, pp.pri_pre_auth_no,  "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, "
      + " pr.primary_sponsor_id as preauth_sponsor_id , pr.center_id as center_id,"
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, prpa.rem_qty as preauth_rem_qty,"
      + " prpa.preauth_act_id as preauth_item_id, prpa.preauth_presc_id,"
      + " date(prad.end_date) as preauth_end_date, prep.preauth_status, "
      + " sa.appointment_time, sa.appointment_status, date(pp.start_datetime) as start_datetime,"
      + " prpa.preauth_act_status, date(pp.prescribed_date) as prescribed_date, "
      + " prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number "
      + " FROM patient_consultation_prescriptions cp "
      + " JOIN patient_prescription pp ON (pp.patient_presc_id=cp.prescription_id) "
      + " JOIN patient_registration pr ON (pr.patient_id=pp.visit_id)  "
      + " JOIN doctors d ON (cp.doctor_id = d.doctor_id)  "
      + " JOIN department dp ON(d.dept_id = dp.dept_id)  "
      + " LEFT JOIN consultation_types ct ON ((case when pr.visit_type='o' then -1 when "
      + "   visit_type='i' then -3 end)::text = ct.consultation_type_id::text) "
      + " LEFT JOIN preauth_prescription_activities prpa ON "
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS[1]
      + " LEFT JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) ";


  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    String consultationPrescription =  PATIENT_CONSULTATION_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_CONSULTATION_PRESCRIPTIONS_BY_VISIT_ID_WHERE
        + VISIT_CONSULTATION_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + CONSULTATION_PRESCRIPTIONS_WHERE;
    return DatabaseHelper.queryToDynaList(consultationPrescription, patientId, patientId);
  }

  /**
   * Gets the prescriptions by consultation id.
   *
   * @param consultationId the consultation id
   * @return the prescriptions by consultation id
   */
  public List<BasicDynaBean> getPrescriptionsByConsultationId(Integer consultationId) {
    String testPrescription =  PATIENT_CONSULTATION_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_CONSULTATION_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE
        + PREAUTH_PRESCRIPTIONS
        + VISIT_CONSULTATION_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + CONSULTATION_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE;
    return DatabaseHelper.queryToDynaList(testPrescription,
        new Object[] {consultationId, consultationId, consultationId});
  }

  /** The Constant GET_PRESCRIBED_CONSULTATION_FOR_CONSULTATION. */
  private static final String GET_PRESCRIBED_CONSULTATION_FOR_CONSULTATION =
      " SELECT pp.prescribed_date, d.doctor_name as item_name, d.doctor_id, pcp.prescription_id,"
      + " cons_remarks as item_remarks, pp.status as added_to_bill, '' as consumption_uom,"
      + " 'item_master' as master, 'Doctor' as item_type, false as ispackage, activity_due_date,"
      + " mod_time, -1 as route_id, '' as route_name,'' AS category_name,'' as manf_name,"
      + " '' as manf_mnemonic, 0 as lblcount,0 as issue_base_unit, '' as prior_auth_required,"
      + " 0 as item_form_id, '' as item_strength, '' as item_form_name, 0 as charge,"
      + " 0 as discount, '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number, "
      + " '' as tooth_fdi_number, 0 as service_qty, '' as service_code,false as non_hosp_medicine,"
      + " 0 as duration, '' as duration_units, 0 as item_strength_units, '' as unit_name, "
      + " '' as erx_status, '' as erx_denial_code, '' as erx_denial_remarks,"
      + " '' as denial_code_status, '' as denial_code_type,  pcp.admin_strength,"
      + " 'N' as granular_units, pp.special_instr, '' as denial_desc, '' as example,"
      + " preauth_required, dept.dept_name, 'N' as send_for_erx, '' as item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, d.doctor_name as cons_doctor_name,"
      + " cons_remarks , '' as drug_code "
      + " FROM patient_prescription pp "
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (pcp.doctor_id = d.doctor_id) "
      + " JOIN department dept ON (dept.dept_id=d.dept_id) WHERE pp.consultation_id=? ";

  /**
   * Gets the presc consultations for consultation.
   *
   * @param consultationId the consultation id
   * @return the presc consultations for consultation
   */
  public List<BasicDynaBean> getPrescConsultationsForConsultation(int consultationId) {
    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_CONSULTATION_FOR_CONSULTATION,
        new Object[] {consultationId});
  }

  /** The Constant GET_PRESCRIBED_CONSULTATION_FOR_TREATMENTSHEET. */
  private static final String GET_PRESCRIBED_CONSULTATION_FOR_TREATMENTSHEET =
      " SELECT pp.consultation_id, d.doctor_name as cons_doctor_name, cons_remarks,"
      + " pp.status as added_to_bill, prescribed_date, mod_time, activity_due_date "
      + " FROM patient_prescription pp "
      + " JOIN patient_consultation_prescriptions pcp ON (pp.patient_presc_id=pcp.prescription_id)"
      + " JOIN doctors d ON (d.doctor_id=pcp.doctor_id) "
      + " WHERE consultation_id=? ORDER BY prescription_id ";

  /**
   * Gets the presc consultation for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the presc consultation for treatment sheet
   */
  public List<BasicDynaBean> getPrescConsultationForTreatmentSheet(int consultationId) {
    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_CONSULTATION_FOR_TREATMENTSHEET,
        new Object[] {consultationId});
  }

}
