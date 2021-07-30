package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientTestPrescriptionsRepository.
 */
@Repository
public class PatientTestPrescriptionsRepository extends GenericRepository {

  /** The Constant PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS. */
  private static final String[] PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS = new String[] {
      "  (prpa.visit_id = dc.patient_id " + "and prpa.status='A' " //
          + "and pp.patient_presc_id = prpa.patient_pres_id " //
          + "and prpa.preauth_act_item_id= tp.test_id " //
          + "and prpa.rem_qty>0) ",
      //
      "  (prpa.visit_id = pp.visit_id " + "and prpa.status='A' " //
          + "and pp.patient_presc_id = prpa.patient_pres_id " //
          + "and prpa.preauth_act_item_id= tp.test_id " //
          + "and prpa.rem_qty > 0) "}; //

  /** The Constant PREAUTH_PRESCRIPTION_ACTIVITIES_COLUMNS. */
  private static final String PREAUTH_PRESCRIPTION_ACTIVITIES_COLUMNS =
      " prpa.preauth_id as preauth_number, prpa.preauth_mode , prpa.rem_qty as preauth_rem_qty,"
      + " prpa.preauth_act_id as preauth_item_id  ";

  /**
   * Instantiates a new patient test prescriptions repository.
   */
  public PatientTestPrescriptionsRepository() {
    super("patient_test_prescriptions");
  }

  /** The Constant GET_PRESCRIPTIONS. */
  private static final String GET_PRESCRIPTIONS_BY_VISIT_ID_WHERE =
      " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)=0 AND pp.visit_id=?"
      + " order by pres_id";

  /** The Constant GET_PRESCRIPTIONS. */
  private static final String GET_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE =
      " WHERE pp.status='P' AND pp.consultation_id=?"
      + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null)"
      + " order by pres_id";

  private static final String PATIENT_TEST_PRESCRIPTIONS = " SELECT dc.patient_id, "
      + " tp.op_test_pres_id as pres_id, tp.priority, d.doctor_id, d.doctor_name, "
      + " atp.test_name as name, tp.test_remarks as remarks, tp.test_id, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id, "
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, "
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id, "
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, prpa.rem_qty as preauth_rem_qty ,"
      + " prpa.preauth_act_id as preauth_item_id, prpa.preauth_presc_id,"
      + " date(prad.end_date) as preauth_end_date,"
      + " sa.appointment_time, sa.appointment_status, date(pp.start_datetime) as start_datetime,"
      + " prpa.preauth_act_status, prep.preauth_status, "
      + " oi.entity as type, date(pp.prescribed_date) as prescribed_date, prpa.preauth_required,"
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number "
      + " FROM patient_test_prescriptions tp "
      + " JOIN patient_prescription pp ON (pp.patient_presc_id=tp.op_test_pres_id)  "
      + " JOIN doctor_consultation dc ON (dc.consultation_id = pp.consultation_id)  "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id)  "
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + " JOIN all_tests_pkgs_view atp ON (tp.test_id = atp.test_id) "
      + " LEFT JOIN orderable_item oi ON (tp.test_id = oi.entity_id) "
      + " LEFT JOIN preauth_prescription_activities prpa ON "
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS[0]
      + " LEFT JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) ";

  public static final String PATIENT_TEST_PRESCRIPTIONS_BY_VISIT_ID_WHERE = 
      " WHERE pp.status='P' AND coalesce(pp.consultation_id, 0)!=0 AND dc.patient_id=? ";
  
  public static final String PATIENT_TEST_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE =
      " WHERE pp.status='P' AND pp.consultation_id=? "
          + "AND (prep.is_cloned = 'N' OR prep.is_cloned is null)";
  
  private static final String VISIT_TEST_PRESCRIPTIONS =
      " UNION ALL " //
      + " SELECT pp.visit_id as patient_id, "
      + " tp.op_test_pres_id as pres_id, tp.priority, null as doctor_id, null as doctor_name, "
      + " atp.test_name as name, tp.test_remarks as remarks, tp.test_id, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id,"
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, prpa.rem_qty as preauth_rem_qty ,"
      + " prpa.preauth_act_id as preauth_item_id,prpa.preauth_presc_id,"
      + " date(prad.end_date) as preauth_end_date,"
      + " sa.appointment_time, sa.appointment_status, date(pp.start_datetime) as start_datetime,"
      + " prpa.preauth_act_status, prep.preauth_status, oi.entity as type,"
      + " date(pp.prescribed_date) as prescribed_date, prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number "
      + " FROM patient_test_prescriptions  tp  "
      + " JOIN patient_prescription pp ON (pp.patient_presc_id=tp.op_test_pres_id) "
      + " JOIN all_tests_pkgs_view atp ON (tp.test_id = atp.test_id)  "
      + " JOIN patient_registration pr ON (pr.patient_id = pp.visit_id) "
      + " LEFT JOIN orderable_item oi ON (tp.test_id = oi.entity_id) "
      + " LEFT JOIN preauth_prescription_activities prpa ON "
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS[1] 
      + " JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) ";
  
  private static final String PREAUTH_PRESCRIPTIONS =
      " UNION ALL "
      + " SELECT DISTINCT prpa.visit_id AS patient_id,  0 as pres_id, '' AS priority, d.doctor_id,"
      + " d.doctor_name, t.test_name as name, prpa.preauth_act_item_remarks  as remarks, "
      + " prpa.preauth_act_item_id AS test_id, null AS pri_pre_auth_no, "
      + " 0 AS pri_pre_auth_mode_id, null AS sec_pre_auth_no, 0 AS sec_pre_auth_mode_id, "
      + " 'N' AS item_excluded_from_doctor, null AS item_excluded_from_doctor_remarks, "
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id, prpa.preauth_id "
      + " AS preauth_number, prpa.preauth_mode,prpa.rem_qty as preauth_rem_qty,prpa.preauth_act_id"
      + " as preauth_item_id, prpa.preauth_presc_id, null::DATE AS preauth_end_date, "
      + " null::TIMESTAMP AS appointment_time,'' AS appointment_status, "
      + " date(prpa.mod_time) as start_datetime, prpa.preauth_act_status, prep.preauth_status, "
      + " oi.entity as type, date(prpa.mod_time) as prescribed_date, prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number "
      + " FROM preauth_prescription_activities prpa "
      + " JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) "
      + " JOIN patient_registration pr ON (prpa.visit_id = pr.patient_id) "
      + " JOIN all_tests_pkgs_view t ON (t.test_id = prpa.preauth_act_item_id) "
      + " LEFT JOIN orderable_item oi ON (t.test_id = oi.entity_id) "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=prpa.consultation_id) "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + " WHERE prpa.consultation_id = ? "
      + " AND prpa.rem_qty > 0 "
      + " AND prpa.prescribed_date >= "
      + "  (select current_timestamp - (prescription_validity || ' days ')::interval "
      + "    from generic_preferences)"
      + " AND prpa.patient_pres_id = 0"
      + " AND prpa.preauth_act_type = 'DIA'"
      + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null) ";

  private static final String PREAUTH_PRESCRIPTION_APPROVAL_DETAILS =
      " LEFT JOIN preauth_request_approval_details prad ON "
      + " (prep.preauth_request_id=prad.preauth_request_id)"
      + " LEFT JOIN scheduler_appointments sa ON (sa.visit_id = pr.patient_id "
      + " AND sa.patient_presc_id = pp.patient_presc_id) " ;

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    String testPrescription =  PATIENT_TEST_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_TEST_PRESCRIPTIONS_BY_VISIT_ID_WHERE
        + VISIT_TEST_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + GET_PRESCRIPTIONS_BY_VISIT_ID_WHERE;
    return DatabaseHelper.queryToDynaList(testPrescription, patientId, patientId);
  }


  /**
   * Gets the prescriptions by consultation id.
   *
   * @param consultationId the consultation id
   * @return the prescriptions by consultation id
   */
  public List<BasicDynaBean> getPrescriptionsByConsultationId(Integer consultationId) {
    String testPrescription =  PATIENT_TEST_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_TEST_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE
        + PREAUTH_PRESCRIPTIONS
        + VISIT_TEST_PRESCRIPTIONS
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + GET_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE;
    return DatabaseHelper.queryToDynaList(testPrescription,
        new Object[] {consultationId, consultationId, consultationId});
  }

  /** The Constant GET_PRESCRIBED_TESTS_FOR_CONSULTATION. */
  private static final String GET_PRESCRIBED_TESTS_FOR_CONSULTATION =
      " SELECT pp.prescribed_date, atp.test_name as item_name, atp.test_id as item_id,"
      + " op_test_pres_id, test_remarks as item_remarks, pp.status as added_to_bill,"
      + " '' as consumption_uom, 'item_master' as master, 'Inv.' as item_type, ispkg as ispackage,"
      + " activity_due_date, mod_time, -1 as route_id, '' as route_name,'' AS category_name,"
      + " '' as manf_name, '' as manf_mnemonic, 0 as lblcount,0 as issue_base_unit,"
      + " atp.prior_auth_required, 0 as item_form_id, '' as item_strength, '' as item_form_name, "
      + " coalesce(dc.charge, pc.charge, 0) as charge, coalesce(dc.discount, pc.discount, 0) as "
      + " discount, dd.category as test_category, 'N' as tooth_num_required,"
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code, "
      + " false as non_hosp_medicine, 0 as duration, '' as duration_units,"
      + " 0 as item_strength_units, '' as unit_name, '' as erx_status, '' as erx_denial_code,"
      + " '' as erx_denial_remarks, '' as denial_code_status, '' as denial_code_type,"
      + " ptp.admin_strength, 'N' as granular_units, pp.special_instr, '' as denial_desc,"
      + " '' as example, preauth_required, dd.ddept_name as dept_name, 'N' as send_for_erx, "
      + " coalesce(tod.item_code, pod.item_code) as item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, atp.test_name, test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name, '' as cons_remarks,"
      + " '' as drug_code " //
      + " FROM patient_prescription pp" //
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id)"
      + " JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id) "
      + " LEFT OUTER JOIN test_org_details tod ON tod.org_id=? AND "
      + "   atp.test_id=tod.test_id "
      + " LEFT OUTER JOIN pack_org_details pod ON atp.test_id=pod.package_id::text "
      + "   AND pod.org_id=? "
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id=atp.ddept_id)"
      + " LEFT JOIN diagnostic_charges dc ON (dc.test_id=atp.test_id and dc.org_name=? "
      + "   and dc.bed_type=?) "
      + " LEFT JOIN package_charges pc ON (pc.package_id::text=atp.test_id and pc.org_id=? "
      + "   and pc.bed_type=?) "
      + " WHERE pp.consultation_id=? ";

  /**
   * Gets the presc tests for consultation.
   *
   * @param orgId the org id
   * @param bedType the bed type
   * @param consultationId the consultation id
   * @return the presc tests for consultation
   */
  public List<BasicDynaBean> getPrescTestsForConsultation(String orgId, String bedType,
      int consultationId) {

    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_TESTS_FOR_CONSULTATION, orgId, orgId,
        orgId, bedType, orgId, bedType, consultationId);
  }

  /** The Constant GET_PRESCRIBED_TESTS_FOR_TREATMENTSHEET. */
  private static final String GET_PRESCRIBED_TESTS_FOR_TREATMENTSHEET =
      " SELECT pp.consultation_id, atp.test_name, test_remarks, pp.status as added_to_bill,"
      + " prescribed_date, mod_time, ispackage, activity_due_date "
      + " FROM patient_prescription pp "
      + " JOIN patient_test_prescriptions ptp ON (pp.patient_presc_id=ptp.op_test_pres_id) "
      + " JOIN all_tests_pkgs_view atp ON (atp.test_id = ptp.test_id)"
      + " WHERE consultation_id=? ORDER BY op_test_pres_id ";

  /**
   * Gets the presc tests for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the presc tests for treatment sheet
   */
  public List<BasicDynaBean> getPrescTestsForTreatmentSheet(int consultationId) {

    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_TESTS_FOR_TREATMENTSHEET,
        new Object[] {consultationId});
  }

}
