package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientServicePrescriptionsRepository.
 */
@Repository
public class PatientServicePrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient service prescriptions repository.
   */
  public PatientServicePrescriptionsRepository() {
    super("patient_service_prescriptions");
  }

  /** The Constant PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS. */
  private static final String[] PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS = new String[] {
      "  (prpa.consultation_id = pp.consultation_id " + " and prpa.visit_id = dc.patient_id "
          + " and prpa.status='A' " //
          + " and pp.patient_presc_id = prpa.patient_pres_id " //
          + " and prpa.preauth_act_item_id= sp.service_id " //
          + " and prpa.rem_qty >= 0) ", //
      //
      "  (prpa.consultation_id = pp.consultation_id " + " and prpa.visit_id = pp.visit_id "
          + " and prpa.status='A' " //
          + " and pp.patient_presc_id = prpa.patient_pres_id " //
          + " and prpa.preauth_act_item_id= sp.service_id " //
          + " and prpa.rem_qty >= 0) "
  };

  private static final String PREAUTH_PRESCRIPTION_APPROVAL_DETAILS =
      " LEFT JOIN preauth_request_approval_details prad ON "
      + " (prep.preauth_request_id=prad.preauth_request_id)"
      + " LEFT JOIN scheduler_appointments sa ON (sa.visit_id = pr.patient_id "
      + " AND sa.patient_presc_id = pp.patient_presc_id) ";

  /** The Constant SERVICE_PRESCRIPTIONS. */
  private static final String SERVICE_PRESCRIPTIONS_WHERE =
      " WHERE pp.status in ('P', 'PA') AND coalesce(pp.consultation_id, 0)=0 AND pp.visit_id=?"
      + " order by pres_id ";

  /** The Constant SERVICE_PRESCRIPTIONS. */
  private static final String SERVICE_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE =
      " WHERE pp.status in ('P', 'PA') AND pp.consultation_id=?"
          + " order by pres_id ";

  private static final String PATIENT_SERVICE_PRESCRIPTION = " SELECT dc.patient_id, "
      + " sp.op_service_pres_id as pres_id, d.doctor_id, d.doctor_name, s.service_name as name,"
      + " sp.service_remarks as remarks, sp.service_id, sp.tooth_unv_number, sp.tooth_fdi_number, "
      + " s.qty_split_in_pending_presc, "
      + " s.tooth_num_required, LEAST(sp.qty - (select coalesce(sum(sep.quantity), 0) "
      + " from services_prescribed sep where sep.doc_presc_id = sp.op_service_pres_id),"
      + " prpa.rem_qty) as service_qty, pp.pri_pre_auth_no,"
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id,"
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id, "
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, prpa.rem_qty as preauth_rem_qty ,"
      + " prpa.preauth_act_id as preauth_item_id, prep.preauth_status, "
      + " prpa.preauth_presc_id, date(prad.end_date) as preauth_end_date,"
      + " sa.appointment_time, sa.appointment_status, date(pp.start_datetime) as start_datetime,"
      + " prpa.preauth_act_status, date(pp.prescribed_date) as prescribed_date, sp.priority,"
      + " prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number,prpa.approved_qty AS preauth_approved_qty,"
      + " prpa.rem_approved_qty AS preauth_rem_approved_qty "
      + " FROM patient_service_prescriptions sp "
      + " JOIN patient_prescription pp  ON (pp.patient_presc_id=sp.op_service_pres_id) "
      + " JOIN doctor_consultation dc ON (dc.consultation_id = pp.consultation_id)  "
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id)  "
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + " JOIN services s ON (sp.service_id = s.service_id) "
      + " LEFT JOIN preauth_prescription_activities prpa  ON " //
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS[0]
      + " LEFT JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) ";


  
  private static final String PATIENT_SERVICE_PRESCRIPTIONS_BY_VISIT_ID_WHERE  = 
      " WHERE pp.status in ('P', 'PA') AND coalesce(pp.consultation_id, 0)!=0 AND dc.patient_id=? ";
  
  private static final String PATIENT_SERVICE_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE  =
      " WHERE pp.status in ('P', 'PA') AND pp.consultation_id=? "
        + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null) ";

  private static final String PREAUTH_PRESCRIPTIONS =
      " UNION ALL "
          + " SELECT DISTINCT prpa.visit_id AS patient_id,  0 as pres_id, d.doctor_id, "
          + " d.doctor_name, s.service_name as name, prpa.preauth_act_item_remarks  as remarks, "
          + " prpa.preauth_act_item_id AS service_id, "
          + " null AS tooth_unv_number, null as tooth_fdi_number, s.qty_split_in_pending_presc, "
          + " null as tooth_num_required, "
          + " prpa.rem_qty AS service_qty, null AS pri_pre_auth_no, 0 AS pri_pre_auth_mode_id, "
          + " null AS sec_pre_auth_no, 0 AS sec_pre_auth_mode_id, 'N' AS "
          + " item_excluded_from_doctor, null AS item_excluded_from_doctor_remarks, "
          + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id, "
          + " prpa.preauth_id as preauth_number, prpa.preauth_mode,prpa.rem_qty as preauth_rem_qty"
          + " , prpa.preauth_act_id as preauth_item_id, prep.preauth_status, "
          + " prpa.preauth_presc_id, null::DATE as preauth_end_date, "
          + " NULL::TIMESTAMP AS appointment_time, '' AS appointment_status, "
          + " date(prpa.mod_time) as start_datetime, prpa.preauth_act_status, "
          + " date(prpa.mod_time) as prescribed_date, '' as priority, prpa.preauth_required, "
          + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
          + " AS presc_preauth_number,prpa.approved_qty AS preauth_approved_qty, "
          + " prpa.rem_approved_qty AS preauth_rem_approved_qty "
          + " FROM preauth_prescription_activities prpa "
          + " JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) "
          + " JOIN patient_registration pr ON (prpa.visit_id = pr.patient_id) "
          + " JOIN services s ON (s.service_id = prpa.preauth_act_item_id)"
          + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id=prpa.consultation_id) "
          + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id) "
          + " WHERE prpa.consultation_id = ? "
          + " AND prpa.rem_qty > 0"
          + " AND prpa.prescribed_date >= "
          + "  (select current_timestamp - (prescription_validity || ' days ')::interval "
          + "    from generic_preferences)"
          + " AND prpa.patient_pres_id = 0"
          + " AND prpa.preauth_act_type =  'SER'"
          + " AND (prep.is_cloned = 'N' OR prep.is_cloned is null) ";

  private static final String VISIT_SERVICE_PRESCRIPTION =
      
      " UNION ALL " + " SELECT pp.visit_id as patient_id, "
      + " sp.op_service_pres_id as pres_id, null as doctor_id, null as doctor_name, "
      + " s.service_name as name, sp.service_remarks as remarks, sp.service_id,"
      + " sp.tooth_unv_number, sp.tooth_fdi_number, s.qty_split_in_pending_presc, "
      + " s.tooth_num_required,"
      + " LEAST(sp.qty-(select sum(coalesce(services_prescribed.quantity, 0))"
      + " from services_prescribed where services_prescribed.doc_presc_id ="
      + " sp.op_service_pres_id),prpa.rem_qty) as service_qty, pp.pri_pre_auth_no, "
      + " pp.pri_pre_auth_mode_id, pp.sec_pre_auth_no, pp.sec_pre_auth_mode_id, "
      + " pp.item_excluded_from_doctor, pp.item_excluded_from_doctor_remarks,"
      + " pr.primary_sponsor_id as preauth_sponsor_id, pr.center_id as center_id, "
      + " prpa.preauth_id as preauth_number, prpa.preauth_mode, prpa.rem_qty as preauth_rem_qty ,"
      + " prpa.preauth_act_id as preauth_item_id, prep.preauth_status, " 
      + " prpa.preauth_presc_id, date(prad.end_date) as preauth_end_date,"
      + " sa.appointment_time, sa.appointment_status, date(pp.start_datetime) as start_datetime,"
      + " prpa.preauth_act_status, date(pp.prescribed_date) as prescribed_date, sp.priority, "
      + " prpa.preauth_required, "
      + " CONCAT(COALESCE(prpa.preauth_act_id,0),'_',COALESCE(prpa.patient_pres_id,0)) "
      + " AS presc_preauth_number,prpa.approved_qty AS preauth_approved_qty, "
      + " prpa.rem_approved_qty AS preauth_rem_approved_qty "
      + " FROM patient_service_prescriptions sp "
      + " JOIN  patient_prescription pp ON (pp.patient_presc_id=sp.op_service_pres_id) "
      + " JOIN services s ON (sp.service_id = s.service_id)  "
      + " JOIN patient_registration pr ON (pr.patient_id = pp.visit_id) "
      + " LEFT JOIN preauth_prescription_activities prpa  ON " //
      + PREAUTH_PRESCRIPTION_ACTIVITIES_CONDITIONS[1]
      + " LEFT JOIN preauth_prescription prep ON (prpa.preauth_presc_id = prep.preauth_presc_id) ";


  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(final String patientId) {
    String servicePrescription =  PATIENT_SERVICE_PRESCRIPTION
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_SERVICE_PRESCRIPTIONS_BY_VISIT_ID_WHERE
        + VISIT_SERVICE_PRESCRIPTION
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + SERVICE_PRESCRIPTIONS_WHERE;
    return DatabaseHelper.queryToDynaList(servicePrescription, patientId, patientId);
  }

  /**
   * Gets the prescriptions by consultation id.
   *
   * @param consultationId the consultation id
   * @return the prescriptions by consultation id
   */
  public List<BasicDynaBean> getPrescriptionsByConsultationId(Integer consultationId) {
    String servicePrescription =  PATIENT_SERVICE_PRESCRIPTION
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_SERVICE_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE
        + PREAUTH_PRESCRIPTIONS
        + VISIT_SERVICE_PRESCRIPTION
        + PREAUTH_PRESCRIPTION_APPROVAL_DETAILS
        + PATIENT_SERVICE_PRESCRIPTIONS_BY_CONSULTATION_ID_WHERE;
    return DatabaseHelper.queryToDynaList(servicePrescription,
        new Object[] {consultationId, consultationId, consultationId});
  }

  /** The Constant GET_PRESCRIBED_SERVICES_FOR_CONSULTATION. */
  private static final String GET_PRESCRIBED_SERVICES_FOR_CONSULTATION =
      " SELECT pp.prescribed_date, s.service_name as item_name, s.service_id as item_id,"
      + " op_service_pres_id, service_remarks as item_remarks, pp.status as added_to_bill,"
      + " '' as consumption_uom, 'item_master' as master, 'Service' as item_type, false as "
      + " ispackage, activity_due_date, mod_time, -1 as  route_id, '' as route_name,"
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic, 0 as lblcount,"
      + " 0 as issue_base_unit, s.prior_auth_required, 0 as item_form_id, '' as item_strength,"
      + " '' as item_form_name, smc.unit_charge as charge, smc.discount as discount,"
      + " '' as test_category, s.tooth_num_required, psp.tooth_unv_number, psp.tooth_fdi_number,"
      + " qty as service_qty, service_code, false as non_hosp_medicine, 0 as duration,"
      + " '' as duration_units, 0 as item_strength_units, '' as unit_name, '' as erx_status,"
      + " '' as erx_denial_code, '' as erx_denial_remarks, '' as denial_code_status,"
      + " '' as denial_code_type,  psp.admin_strength, 'N' as granular_units, pp.special_instr, "
      + " '' as denial_desc, '' as example, preauth_required, sd.department as dept_name,"
      + " 'N' as send_for_erx, sod.item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " s.service_name, service_remarks, '' as cons_doctor_name, '' as cons_remarks,"
      + " '' as drug_code "
      + " FROM patient_prescription pp "
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id) "
      + " JOIN services s ON (s.service_id = psp.service_id) "
      + " JOIN services_departments sd ON (sd.serv_dept_id=s.serv_dept_id) "
      + " JOIN service_master_charges smc ON (smc.service_id=s.service_id"
      + "   and org_id=? and bed_type=?) "
      + " LEFT JOIN service_org_details sod ON sod.org_id=? AND sod.service_id=s.service_id "
      + " WHERE pp.consultation_id=? ";

  /**
   * Gets the pres services for consultation.
   *
   * @param orgId the org id
   * @param bedType the bed type
   * @param consultationId the consultation id
   * @return the pres services for consultation
   */
  public List<BasicDynaBean> getPresServicesForConsultation(final String orgId,
      final String bedType, final int consultationId) {
    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_SERVICES_FOR_CONSULTATION, orgId, bedType,
        orgId, consultationId);
  }

  /** The Constant GET_PRESCRIBED_SERVICES_FOR_TREATMENTSHEET. */
  private static final String GET_PRESCRIBED_SERVICES_FOR_TREATMENTSHEET =
      " SELECT pp.consultation_id, s.service_name, service_remarks, pp.status as added_to_bill,"
      + " prescribed_date, mod_time, activity_due_date "
      + " FROM patient_prescription pp "
      + " JOIN patient_service_prescriptions psp ON (pp.patient_presc_id=psp.op_service_pres_id) "
      + " JOIN services s ON (s.service_id = psp.service_id)"
      + " WHERE consultation_id=? ORDER BY op_service_pres_id ";

  /**
   * Gets the pres services for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the pres services for treatment sheet
   */
  public List<BasicDynaBean> getPresServicesForTreatmentSheet(final int consultationId) {
    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_SERVICES_FOR_TREATMENTSHEET,
        new Object[] {consultationId});
  }

}
