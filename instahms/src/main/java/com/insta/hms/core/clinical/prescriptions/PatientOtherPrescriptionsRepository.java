package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientOtherPrescriptionsRepository.
 *
 * @author anup vishwas
 */
@Repository
public class PatientOtherPrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient other prescriptions repository.
   */
  public PatientOtherPrescriptionsRepository() {
    super("patient_other_prescriptions");
  }

  /** The Constant GET_PRESCRIBED_OTHERS_FOR_CONSULTATION. */
  private static final String GET_PRESCRIBED_OTHERS_FOR_CONSULTATION =
      " SELECT pp.prescribed_date, item_name, '' as item_id, medicine_quantity, prescription_id,"
      + " frequency as medicine_dosage, frequency, strength, duration as medicine_days, "
      + " item_remarks, 'P' as issued, pop.consumption_uom, '', '', 'op' as master, "
      + " case when non_hosp_medicine then 'Medicine' else 'NonHospital' end as item_type,"
      + " false as ispackage, activity_due_date, mod_time, -1 as route_id, '' as route_name,"
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic , 0 as lblcount,"
      + " 0 as issue_base_unit, '' as prior_auth_required, pop.item_form_id, pop.item_strength,"
      + " if.item_form_name, 0 as charge, 0 as discount,  '' as test_category,"
      + " 'N' as tooth_num_required, '' as tooth_unv_number, '' as tooth_fdi_number,"
      + " 0 as service_qty, '' as service_code, non_hosp_medicine, duration, duration_units, "
      + " item_strength_units, su.unit_name, '' as erx_status, '' as erx_denial_code,"
      + " '' as erx_denial_remarks, '' as denial_code_status, '' as denial_code_type,"
      + "  pop.admin_strength, if.granular_units, pp.special_instr, '' as denial_desc,"
      + " '' as example, '' as preauth_required, '' as dept_name, 'N' as send_for_erx, "
      + " '' as item_code, "
      // legacy support variables in print
      + " '' as medicine_name, '' as medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name,"
      + " '' as cons_remarks , '' as drug_code "
      + " FROM patient_prescription pp "
      + " JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id) "
      + " LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) "
      + " LEFT JOIN item_form_master if ON (pop.item_form_id=if.item_form_id)"
      + " WHERE pp.consultation_id=? ";


  /**
   * Gets the presc others for consultation.
   *
   * @param consultationId the consultation id
   * @return the presc others for consultation
   */
  public List<BasicDynaBean> getPrescOthersForConsultation(int consultationId) {

    return DatabaseHelper.queryToDynaList(GET_PRESCRIBED_OTHERS_FOR_CONSULTATION,
        new Object[] {consultationId});
  }

  /** The Constant GET_OTHER_PRESCRIBED_MEDICINE. */
  private static final String GET_OTHER_PRESCRIBED_MEDICINE =
      " SELECT pp.prescribed_date,pomp.medicine_name as item_name,'' as item_id,medicine_quantity,"
      + " prescription_id as item_prescribed_id, frequency as medicine_dosage, frequency,strength,"
      + " duration as medicine_days, medicine_remarks as item_remarks, 'P' as issued,"
      + " pomp.consumption_uom, g.generic_name, g.generic_code, 'op' as master, 'Medicine' as "
      + " item_type, false as ispackage, activity_due_date, mod_time, mr.route_id, mr.route_name,"
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic, 0 as lblcount,"
      + " 0 as issue_base_unit, '' as prior_auth_required, coalesce(pomp.item_form_id, 0) as "
      + " item_form_id, pomp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount,"
      + " '' as test_category, 'N' as tooth_num_required, '' as tooth_unv_number,"
      + " '' as tooth_fdi_number, 0 as service_qty, '' as service_code,false as non_hosp_medicine,"
      + "  duration, duration_units, pomp.item_strength_units, su.unit_name, '' as erx_status,"
      + " '' as erx_denial_code, '' as erx_denial_remarks, '' as denial_code_status, "
      + " '' as denial_code_type, pomp.admin_strength, ifm.granular_units, pp.special_instr, "
      + " '' as denial_desc, '' as example, '' as preauth_required, '' as dept_name,"
      + " 'N' as send_for_erx, '' as item_code, "
      // legacy support variables in print
      + " pomp.medicine_name, medicine_remarks, '' as test_name, '' as test_remarks,"
      + " '' as service_name, '' as service_remarks, '' as cons_doctor_name,"
      + " '' as cons_remarks , '' as drug_code "
      + " FROM patient_prescription pp"
      + " JOIN patient_other_medicine_prescriptions pomp "
      + "   ON (pp.patient_presc_id=pomp.prescription_id)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pms.item_strength_units) "
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) "
      + " WHERE pp.consultation_id=?";

  /**
   * Gets the presc other medicines for consultation.
   *
   * @param consultationId the consultation id
   * @return the presc other medicines for consultation
   */
  public List<BasicDynaBean> getPrescOtherMedicinesForConsultation(int consultationId) {

    return DatabaseHelper.queryToDynaList(GET_OTHER_PRESCRIBED_MEDICINE,
        new Object[] {consultationId});
  }

}
