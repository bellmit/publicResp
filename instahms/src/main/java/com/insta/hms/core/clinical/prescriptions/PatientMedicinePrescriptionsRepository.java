package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientMedicinePrescriptionsRepository.
 *
 * @author anup vishwas
 */

@Repository
public class PatientMedicinePrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient medicine prescriptions repository.
   */
  public PatientMedicinePrescriptionsRepository() {
    super("patient_medicine_prescriptions");
  }

  /** The Constant GET_PRESCRIBED_MEDICINES. */
  private static final String GET_PRESCRIBED_MEDICINES =
      " SELECT pp.prescribed_date,sid.medicine_name as item_name,sid.medicine_id::text as item_id,"
      + " medicine_quantity, op_medicine_pres_id as item_prescribed_id, frequency as "
      + " medicine_dosage, frequency, strength, duration as medicine_days, medicine_remarks as "
      + " item_remarks, pp.status as issued, cum.consumption_uom, g.generic_name, g.generic_code,"
      + " 'item_master' as master, 'Medicine' as item_type, false as ispackage, activity_due_date,"
      + " mod_time, mr.route_id, mr.route_name, icm.category AS category_name,mm.manf_name,"
      + " mm.manf_mnemonic,0 as lblcount,issue_base_unit, sid.prior_auth_required,"
      + " coalesce(pmp.item_form_id, 0) as item_form_id, pmp.item_strength, if.item_form_name, "
      + " 0 as charge, 0 as discount, '' as test_category, 'N' as tooth_num_required,"
      + " '' as tooth_unv_number, '' as tooth_fdi_number, 0 as service_qty, '' as service_code,"
      + " false as non_hosp_medicine, duration, duration_units, pmp.item_strength_units,"
      + " su.unit_name, pmp.erx_status, pmp.erx_denial_code, pmp.erx_denial_remarks, "
      + " idc.status as denial_code_status, idc.type as denial_code_type, pmp.admin_strength,"
      + " if.granular_units, pp.special_instr, idc.code_description as denial_desc, idc.example,"
      + " '' as preauth_required, '' as dept_name, pmp.send_for_erx, '' as item_code, "
      // legacy support variabled in print
      + " sid.medicine_name, medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name,  '' as cons_remarks,"
      + " sic.item_code as drug_code "
      + " FROM patient_prescription pp"
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id)"
      + " LEFT JOIN item_form_master if ON (pmp.item_form_id=if.item_form_id) "
      + " LEFT OUTER JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + " LEFT OUTER JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + " LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name "
      + " LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id "
      + " LEFT OUTER JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) "
      + " LEFT JOIN insurance_denial_codes idc ON (idc.denial_code = pmp.erx_denial_code) "
      // Drug Code is added
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
      + "   AND hict.health_authority= ? ) "
      + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
      + "   AND sic.code_type = hict.code_type) "
      + " WHERE pp.consultation_id=? ";

  /**
   * Gets the presc medicines for consultation.
   *
   * @param consultationId the consultation id
   * @param helathAuthority the helath authority
   * @return the presc medicines for consultation
   */
  public List<BasicDynaBean> getPrescMedicinesForConsultation(int consultationId,
      String helathAuthority) {
    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_MEDICINES,
        new Object[] {helathAuthority, consultationId});

  }

  /** The Constant GET_PRESC_MEDICINE_FOR_TREATMENT_SHEET. */
  private static final String GET_PRESC_MEDICINE_FOR_TREATMENT_SHEET =
      " SELECT pp.consultation_id, sid.medicine_name, frequency as medicine_dosage, strength,"
      + " medicine_remarks, medicine_quantity, prescribed_date, mod_time, activity_due_date,"
      + " route_id, mr.route_name, pp.status as issued, g.generic_name, g.generic_code,"
      + " cm.consumption_uom, duration, duration_units "
      + " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) "
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + " LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code) "
      + " LEFT JOIN consumption_uom_master cm ON (cm.cons_uom_id = pmp.cons_uom_id) "
      + " WHERE pp.consultation_id=? ORDER BY op_medicine_pres_id ";

  /**
   * Gets the pres med for treatment sheet.
   *
   * @param consultationId the consultation id
   * @return the pres med for treatment sheet
   */
  public List<BasicDynaBean> getPresMedForTreatmentSheet(int consultationId) {
    return DatabaseHelper.queryToDynaList(GET_PRESC_MEDICINE_FOR_TREATMENT_SHEET,
        new Object[] {consultationId});
  }

  /** The Constant UPDATE_ADD_TO_BILL_IN_ORDER. */
  private static final String UPDATE_ADD_TO_BILL_IN_ORDER =
      " UPDATE patient_prescription set status=?, username=?" + " WHERE patient_presc_id=?";

  /**
   * Update prescription.
   *
   * @param status the status
   * @param userId the user id
   * @param presId the pres id
   * @return the integer
   */
  public Integer updatePrescription(String status, String userId, Integer presId) {
    return DatabaseHelper.update(UPDATE_ADD_TO_BILL_IN_ORDER,
        new Object[] {status, userId, presId});
  }

  private static final String GET_MEDICINE_PRESC_IDS_OF_IS_DRUG_CATEGORY_QUERY = "SELECT"
      + " DISTINCT pmp.op_medicine_pres_id"
      + " FROM patient_medicine_prescriptions pmp"
      + " LEFT JOIN generic_name gn ON (pmp.generic_code = gn.generic_code)"
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id = pmp.medicine_id)"
      + " LEFT JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)"
      + " WHERE scm.is_drug = 'Y' AND pmp.op_medicine_pres_id IN (:prescIds)";
  
  /**
   * Get medicine presc ids.
   * 
   * @param prescIds the list of prescids
   * @return list
   */
  public List<BasicDynaBean> filterMedicinePrescOfIsDrugCategory(List<Integer> prescIds,
      boolean isPrescDeleted) {
    StringBuilder query = new StringBuilder((isPrescDeleted) 
        ? GET_MEDICINE_PRESC_IDS_OF_IS_DRUG_CATEGORY_QUERY.replace("patient_medicine_prescriptions",
          "patient_medicine_prescriptions_audit") :
        GET_MEDICINE_PRESC_IDS_OF_IS_DRUG_CATEGORY_QUERY);
    query.append(" ORDER BY pmp.op_medicine_pres_id");
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("prescIds", prescIds);
    return DatabaseHelper.queryToDynaList(query.toString(), parameters);
  }
}
