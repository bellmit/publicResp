package com.insta.hms.core.clinical.dischargemedication;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DischargeMedicationRepository.
 */
@Repository
public class DischargeMedicationRepository extends GenericRepository {

  /**
   * Instantiates a new discharge medication repository.
   */
  public DischargeMedicationRepository() {
    super("discharge_medication");
  }

  // Below methods used for prints in Discharge Summary screen
  private static final String GET_STORE_MEDICINE_PRESCRIPTIONS_PRINTS = "SELECT "
      + " pp.prescribed_date, sid.medicine_name as item_name, sid.medicine_id::text as item_id, "
      + " pmp.medicine_quantity, op_medicine_pres_id as item_prescribed_id, "
      + " frequency as medicine_dosage, pmp.frequency, pmp.strength, duration as medicine_days, "
      + " medicine_remarks as item_remarks, pp.status as issued, pmp.cons_uom_id,"
      + " cum.consumption_uom, "
      + " g.generic_name, g.generic_code, 'item_master' as master, 'Medicine' as item_type, "
      + " mod_time,  mr.route_id, mr.route_name, "
      + " '' AS category_name, '' AS manf_name, '' AS manf_mnemonic, "
      + " coalesce(pmp.item_form_id, 0) as item_form_id, "
      + " pmp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount, '' as doctor_id, "
      + " pmp.duration, pmp.duration_units, " + " pmp.item_strength_units, su.unit_name,"
      + " to_char(pmp.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, "
      + " pmp.admin_strength, ifm.granular_units, pp.special_instr, "
      + " sic.item_code as drug_code" + " FROM patient_prescription pp " + " #JOIN_TABLE# "
      + "JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id "
      + "AND is_discharge_medication = true) "
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id=pmp.cons_uom_id) "
      + "LEFT JOIN store_item_details sid ON (pmp.medicine_id=sid.medicine_id) "
      + "JOIN organization_details od ON (od.org_id=?) "
      + "LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id  "
      + "         AND sir.store_rate_plan_id=od.store_rate_plan_id) "
      + "LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id) "
      + "LEFT JOIN generic_name g ON (pmp.generic_code = g.generic_code) "
      + "LEFT JOIN strength_units su ON (pmp.item_strength_units=su.unit_id) "
      + "LEFT JOIN item_form_master ifm ON (pmp.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin) "
      + "LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
      + "AND hict.health_authority= ? ) "
      + "LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
      + "AND sic.code_type = hict.code_type) " + "WHERE #filter#=? ";

  private static final String GET_OTHER_MEDICINE_PRESCRIPTIONS_PRINT =
      "SELECT "
      + " pp.prescribed_date, pomp.medicine_name as item_name, '' as item_id, "
      + " pomp.medicine_quantity, prescription_id as item_prescribed_id, "
      + " pomp.frequency as medicine_dosage, pomp.frequency, pomp.strength, "
      + " pomp.duration as medicine_days, pomp.medicine_remarks as item_remarks, "
      + " 'P' as issued, pomp.cons_uom_id, cum.consumption_uom, g.generic_name, "
      + " g.generic_code, 'op' as master, 'Medicine' as item_type,"
      + " pomp.mod_time, mr.route_id, mr.route_name, '' AS category_name, "
      + " '' as manf_name, '' as manf_mnemonic, "
      + " coalesce(pomp.item_form_id, 0) as item_form_id, "
      + " pomp.item_strength, ifm.item_form_name, 0 as charge, 0 as discount, '' as doctor_id,"
      + " pomp.duration, pomp.duration_units, pomp.item_strength_units, su.unit_name, "
      + " to_char(pmp.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, pomp.admin_strength,"
      + " ifm.granular_units, pp.special_instr, " + " '' as drug_code "
      + " FROM patient_prescription pp" + " #JOIN_TABLE# "
      + " JOIN patient_other_medicine_prescriptions pomp"
      + " ON (pp.patient_presc_id=pomp.prescription_id"
      + " AND is_discharge_medication = true)"
      + " LEFT JOIN item_form_master ifm ON (pomp.item_form_id = ifm.item_form_id)"
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN strength_units su ON (su.unit_id=pms.item_strength_units) "
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) "
      + " LEFT JOIN patient_medicine_prescriptions pmp"
      + " ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id=pmp.medicine_id) "
      + " LEFT JOIN store_item_controltype sic ON (sic.control_type_id = sid.control_type_id)"
      + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = pomp.cons_uom_id)"
      + " WHERE #filter#=?";

  private static final String GET_PRESCRIPTIONS_PRINTS = "UNION ALL "
      + "SELECT " // Non-Hospital
      + " pp.prescribed_date, item_name, '' as item_id, medicine_quantity, prescription_id"
      + " as item_prescribed_id, "
      + " frequency as medicine_dosage, frequency, strength, duration as medicine_days, "
      + " item_remarks, 'P' as issued, pop.cons_uom_id, cum.consumption_uom, '', '',"
      + " 'op' as master, "
      + " case when non_hosp_medicine then 'Medicine' else 'NonHospital' end as item_type, "
      + " mod_time, -1 as route_id, '' as route_name, "
      + " '' AS category_name,'' as manf_name, '' as manf_mnemonic , pop.item_form_id, "
      + " pop.item_strength, ifm.item_form_name, 0 as charge, 0 as discount, "
      + " '' as doctor_id, duration, duration_units, item_strength_units, su.unit_name, "
      + " to_char(pop.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, pop.admin_strength, "
      + " ifm.granular_units, pp.special_instr, " + " '' as drug_code "
      + "FROM patient_prescription pp " + " #JOIN_TABLE# "
      + "JOIN patient_other_prescriptions pop ON (pp.patient_presc_id=pop.prescription_id "
      + "AND is_discharge_medication = true) "
      + "LEFT JOIN strength_units su ON (su.unit_id=pop.item_strength_units) "
      + "LEFT JOIN item_form_master ifm ON (pop.item_form_id=ifm.item_form_id) "
      + "LEFT JOIN consumption_uom_master cum ON (pop.cons_uom_id = cum.cons_uom_id) "
      + "WHERE #filter#=? ";

  /**
   * Gets list of discharge medication details.
   * 
   * @param visitId the visit id
   * @param healthAuthority the health authority
   * @param presFromStores indicator for store prescriptions
   * @param visitType the visit type
   * @param orgId the org id
   * @return list of discharge medication bean
   */
  public List<BasicDynaBean> getDischargeMedicationDetails(String visitId, String healthAuthority,
      Boolean presFromStores, String visitType, String orgId) {
    if (presFromStores) {
      String query = (GET_STORE_MEDICINE_PRESCRIPTIONS_PRINTS + GET_PRESCRIPTIONS_PRINTS)
          .replace("#JOIN_TABLE#", visitType.equals("i")
              ? ""
                  : "JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id)")
          .replace("#filter#", visitType.equals("i") ? "pp.visit_id" : "dc.patient_id");
      return DatabaseHelper.queryToDynaList(query,
          new Object[] {orgId, healthAuthority, visitId, visitId});
    } else {
      String query = (GET_OTHER_MEDICINE_PRESCRIPTIONS_PRINT + GET_PRESCRIPTIONS_PRINTS)
          .replace("#JOIN_TABLE#", visitType.equals("i")
              ? ""
                  : "JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id)")
          .replace("#filter#", visitType.equals("i") ? "pp.visit_id" : "dc.patient_id");
      return DatabaseHelper.queryToDynaList(query, new Object[] {visitId, visitId});
    }
  }

  /** The Constant ALL_DISCHARGE_MEDICATION_PHARMA_MEDICINES. */
  private static final String ALL_DISCHARGE_MEDICATION_PHARMA_MEDICINES =
      " SELECT dm.medication_id, sid.medicine_name as item_name, "
          + " sid.medicine_id::text as item_id, medicine_quantity, "
          + " medicine_presc_id as item_prescribed_id, frequency as medicine_dosage, "
          + " frequency, strength, duration as medicine_days,"
          + " medicine_remarks as item_remarks, dmd.cons_uom_id, cum.consumption_uom, "
          + " g.generic_name, g.generic_code, 'item_master' as master, "
          + " dmd.mod_time, mr.route_id, mr.route_name,dmd.issued, "
          + " icm.category AS category_name,mm.manf_name, mm.manf_mnemonic, "
          + " coalesce(dmd.item_form_id, 0) as item_form_id, dmd.item_strength, "
          + " if.item_form_name, 0 as charge, 0 as discount,doc.doctor_name,"
          + " dm.doctor_id, duration, duration_units, dmd.item_strength_units, "
          + " su.unit_name, to_char(dmd.mod_time, 'YYYY-MM-DD HH24:MI:SS') as presc_time, "
          + " dmd.admin_strength, if.granular_units, dmd.special_instr, "
          + " sic.item_code as drug_code "
          + " FROM discharge_medication dm "
          + " JOIN discharge_medication_details dmd ON (dm.medication_id=dmd.medication_id) "
          + " LEFT JOIN doctors doc ON (doc.doctor_id=dm.doctor_id) "
          + " LEFT JOIN store_item_details sid ON (dmd.medicine_id=sid.medicine_id) "
          + " LEFT JOIN item_form_master if ON (dmd.item_form_id=if.item_form_id) "
          + " LEFT OUTER JOIN generic_name g ON (dmd.generic_code = g.generic_code) "
          + " LEFT OUTER JOIN medicine_route mr ON (mr.route_id=dmd.route_of_admin)"
          + " LEFT OUTER JOIN manf_master mm on mm.manf_code=sid.manf_name "
          + " LEFT OUTER JOIN store_category_master icm on sid.med_category_id=icm.category_id "
          + " LEFT OUTER JOIN strength_units su ON (dmd.item_strength_units=su.unit_id) "
          + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
          + " AND hict.health_authority= ? ) "
          + " LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
          + " AND sic.code_type = hict.code_type) "
          + " LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = dmd.cons_uom_id)"
          + " WHERE dm.visit_id=? ";

  public static List<BasicDynaBean> getDischargeMedicationDetails(String visitId,
      String healthAuthority) {
    return DatabaseHelper.queryToDynaList(ALL_DISCHARGE_MEDICATION_PHARMA_MEDICINES,
        healthAuthority, visitId);
  }
  
  private static final String GET_MEDICINE_PRESC_IDS_FOR_HL7 = "SELECT"
      + " DISTINCT dmda.medicine_presc_id"
      + " FROM discharge_medication_details_audit dmda"
      + " LEFT JOIN generic_name gn ON (dmda.generic_code = gn.generic_code)"
      + " LEFT JOIN store_item_details sid ON (sid.medicine_id = dmda.medicine_id)"
      + " LEFT JOIN store_category_master scm ON (sid.med_category_id = scm.category_id)"
      + " WHERE dmda.medicine_presc_id IN (:prescIds)";
  
  /**
   * Get medicine presc ids.
   * 
   * @param prescIds the list of prescids
   * @return list
   */
  public List<BasicDynaBean> getMedicinePrescIdsForHl7(List<Integer> prescIds,
      boolean prescByGenerics) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("prescIds", prescIds);
    StringBuilder query = new StringBuilder(GET_MEDICINE_PRESC_IDS_FOR_HL7);
    if (!prescByGenerics) {
      query.append(" AND scm.is_drug = 'Y'");
    }
    query.append(" ORDER BY dmda.medicine_presc_id");
    return DatabaseHelper.queryToDynaList(query.toString(), parameters);
  }
}
