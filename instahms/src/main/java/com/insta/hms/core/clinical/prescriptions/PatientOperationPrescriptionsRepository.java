package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientOperationPrescriptionsRepository.
 *
 * @author anup vishwas
 */
@Repository
public class PatientOperationPrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient operation prescriptions repository.
   */
  public PatientOperationPrescriptionsRepository() {
    super("patient_operation_prescriptions");
  }

  /** The Constant GET_PRESCRIBED_OPERATION. */
  private static final String GET_PRESCRIBED_OPERATION =
      " SELECT pp.prescribed_date, om.operation_name as item_name, om.op_id as item_id,"
      + " prescription_id, pop.remarks as item_remarks, pp.status as added_to_bill,"
      + "  '' as consumption_uom, 'item_master' as master, 'Operation' as item_type, "
      + " false as ispackage, null::date as activity_due_date, mod_time, -1 as route_id,"
      + " '' as route_name, '' AS category_name,'' as manf_name, '' as manf_mnemonic,"
      + " 0 as lblcount,0 as issue_base_unit, om.prior_auth_required, 0 as item_form_id,"
      + " '' as item_strength, '' as item_form_name, surg_asstance_charge as charge, "
      + " surg_asst_discount as discount, '' as test_category, 'N' as tooth_num_required,"
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code,"
      + " false as non_hosp_medicine, 0 as duration, 'D' as duration_units, "
      + " 0 as item_strength_units, '' as master_item_strength_units, '' as erx_status,"
      + " '' as erx_denial_code, '' as erx_denial_remarks, '' as denial_code_status,"
      + " '' as denial_code_type,  pop.admin_strength, 'N' as granular_units, pp.special_instr, "
      + " '' as denial_desc, '' as example, preauth_required, dept.dept_name, 'N' as send_for_erx,"
      + " ood.item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name,  '' as cons_remarks,"
      + " '' as drug_code "
      + " FROM patient_prescription pp "
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id)"
      + " JOIN operation_master om ON (pop.operation_id=om.op_id) "
      + " JOIN department dept ON (dept.dept_id=om.dept_id) "
      + " JOIN operation_charges oc ON (oc.op_id=pop.operation_id and org_id=? and bed_type=?) "
      + " LEFT JOIN operation_org_details ood ON ood.org_id=? AND ood.operation_id=om.op_id "
      + " WHERE pp.consultation_id=? ";

  /**
   * Gets the presc operations for consultation.
   *
   * @param consultationId the consultation id
   * @param orgId the org id
   * @param bedType the bed type
   * @return the presc operations for consultation
   */
  public List<BasicDynaBean> getPrescOperationsForConsultation(int consultationId, String orgId,
      String bedType) {

    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_OPERATION, orgId, bedType, orgId,
        consultationId);
  }

  /** The Constant PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS. */
  private static final String PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS =
      "  (prpa.consultation_id = pp.consultation_id "
          + " and prpa.visit_id = coalesce(dc.patient_id, pp.visit_id) and prpa.status='A' " //
          + " and pp.patient_presc_id = prpa.patient_pres_id " //
          + " and prpa.preauth_act_item_id= pop.operation_id " //
          + " and prpa.rem_qty>0) "; //

  /** The Constant OPERATION_PRESCRIPTIONS. */
  private static final String OPERATION_PRESCRIPTIONS = "SELECT coalesce(dc.patient_id, "
      + " pp.visit_id) as patient_id, operation_id, prescription_id as pres_id, operation_name, "
      + " pp.pri_pre_auth_no,pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks, "
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id,"
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, prpa.rem_qty as preauth_rem_qty, "
      + " prpa.preauth_act_id as preauth_item_id, prpa.preauth_presc_id, prad.end_date,"
      + " sa.appointment_time, sa.appointment_status, prpa.preauth_act_status, "
      + " date(prad.end_date) as preauth_end_date, date(pp.prescribed_date) as prescribed_date, "
      + " date(pp.start_datetime) as start_datetime, prep.preauth_status, prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number"
      + " FROM patient_prescription pp "
      + " JOIN patient_operation_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=pp.consultation_id) "
      + " JOIN operation_master op ON (op.op_id=pop.operation_id) "
      + " LEFT JOIN preauth_prescription_activities prpa ON "
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS
      + " LEFT JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) "
      + " JOIN patient_registration pr ON (pr.patient_id = coalesce(dc.patient_id, pp.visit_id)) "
      + " LEFT JOIN preauth_request_approval_details prad ON "
      + " (prep.preauth_request_id=prad.preauth_request_id)"
      + " LEFT JOIN scheduler_appointments sa ON (sa.visit_id = pr.patient_id "
      + "   AND sa.patient_presc_id = pp.patient_presc_id) ";

  private static final String OPERATION_PRESCRIPTIONS_BY_VISIT_ID = 
      " WHERE ( pp.status='P' and coalesce(pp.consultation_id, 0)!=0 and dc.patient_id = ?) "
      + "   OR (pp.status='P' and coalesce(pp.consultation_id, 0)=0 and pp.visit_id = ?) ;";
  private static final String OPERATION_PRESCRIPTIONS_BY_CONSULTATION_ID =
      " WHERE ( pp.status='P' and dc.consultation_id = ? "
          + "and (prep.is_cloned = 'N' OR prep.is_cloned is null))";

  private static final String PREAUTH_PRESCRIPTIONS =
      " UNION ALL "
      + " SELECT DISTINCT prpa.visit_id AS patient_id, prpa.preauth_act_item_id AS operation_id,"
      + " 0 as pres_id, oi.operation_name, null AS pri_pre_auth_no, 0 AS pri_pre_auth_mode_id, "
      + " null AS sec_pre_auth_no, 0 AS sec_pre_auth_mode_id,'N' AS item_excluded_from_doctor,"
      + " null AS item_excluded_from_doctor_remarks,  pr.primary_sponsor_id as preauth_sponsor_id," 
      + " pr.center_id as center_id,  prpa.preauth_id as preauth_number, prpa.preauth_mode, "
      + " prpa.rem_qty as preauth_rem_qty , prpa.preauth_act_id as preauth_item_id, "
      + " prpa.preauth_presc_id, null::DATE as end_date, NULL::TIMESTAMP AS appointment_time, "
      + " '' AS appointment_status, prpa.preauth_act_status, "
      + " null::DATE as preauth_end_date, date(prpa.mod_time) as prescribed_date, "
      + " date(prpa.mod_time) as start_datetime, "
      + " prep.preauth_status, prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number" 
      + " FROM preauth_prescription_activities prpa "
      + " JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) "
      + " JOIN patient_registration pr ON (prpa.visit_id = pr.patient_id) "
      + " JOIN operation_master oi ON (oi.op_id=prpa.preauth_act_item_id) "
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=prpa.consultation_id) "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
      + " WHERE prpa.consultation_id = ? AND prpa.rem_qty > 0 "
      + " AND prpa.prescribed_date >= "
      + "  (select current_timestamp - (prescription_validity || ' days ')::interval "
      + "     from generic_preferences) "
      + " AND prpa.patient_pres_id = 0"
      + " AND prpa.preauth_act_type =  'OPE'"
      + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null) ";

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    return DatabaseHelper.queryToDynaList(
        OPERATION_PRESCRIPTIONS + OPERATION_PRESCRIPTIONS_BY_VISIT_ID, patientId, patientId);
  }


  /**
   * Gets the prescriptions by consultation id.
   *
   * @param consultationId the consultation id
   * @return the prescriptions by consultation id
   */
  public List<BasicDynaBean> getPrescriptionsByConsultationId(Integer consultationId) {
    return DatabaseHelper.queryToDynaList(OPERATION_PRESCRIPTIONS 
    + OPERATION_PRESCRIPTIONS_BY_CONSULTATION_ID
    + PREAUTH_PRESCRIPTIONS,
    new Object[]{consultationId, consultationId});
  }
  
  
}
