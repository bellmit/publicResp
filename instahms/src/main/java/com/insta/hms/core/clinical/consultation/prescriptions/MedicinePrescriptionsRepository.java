package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author teja.
 *
 */
@Repository
public class MedicinePrescriptionsRepository extends GenericRepository {

  public MedicinePrescriptionsRepository() {
    super("patient_medicine_prescriptions");
  }

  private String presGenerics = "SELECT gn.generic_name AS item_name, "
      + " '' AS order_code, generic_code as item_id,"
      + " 0 AS qty, gn.generic_code, gn.generic_name, '' AS drug_code, 'item_master' AS master,"
      + " 'Medicine' AS item_type, '' AS route_of_admin, "
      + " '' AS consumption_uom, '' AS prior_auth_required,"
      + " 0 AS item_form_id, '' AS item_strength, "
      + " 0 AS item_strength_units, 'N' AS granular_units,"
      + " 0 AS insurance_category_id, '' AS insurance_category_name, "
      + " '' AS batch_no, 0 AS item_batch_id,"
      + " 0 AS issue_base_unit, 0 AS selling_price, 0 AS meddisc, "
      + " '' AS category_payable, '' AS category,"
      + " 0 AS charge, 0 AS discount, 0 AS control_type_id, true as applicable "
      + "FROM generic_name gn "
      + "LEFT JOIN store_item_details sid ON (sid.generic_name = gn.generic_code) "
      + "LEFT JOIN store_category_master mc ON (mc.category_id = sid.med_category_id "
      + "AND mc.prescribable) "
      + "WHERE gn.status ='A' AND (gn.generic_name ilike ? OR gn.generic_name ilike ?) "
      + "GROUP BY gn.generic_code "
      + "ORDER BY generic_name "
      + "LIMIT ?";

  private String presNonHospitalMedicines = "SELECT medicine_name as item_name, "
      + " '' AS order_code, '' as item_id,"
      + " 0 as qty, g.generic_code, g.generic_name, '' AS drug_code, "
      + " 'op' as master, 'Medicine' as item_type,"
      + " pmm.route_of_admin AS route_of_admin, '' AS consumption_uom, "
      + " '' AS prior_auth_required, item_form_id, item_strength,"
      + " item_strength_units, 'N' AS granular_units, "
      + " 0 AS insurance_category_id, '' as insurance_category_name,"
      + " '' AS batch_no, 0 AS item_batch_id, "
      + " 0 AS issue_base_unit, 0 AS selling_price, 0 AS meddisc,"
      + " '' AS category_payable, '' AS category, 0 AS charge, "
      + " 0 AS discount, 0 AS control_type_id, true as applicable "
      + "FROM prescribed_medicines_master pmm "
      + "LEFT JOIN generic_name g ON (pmm.generic_name=g.generic_code) "
      + "WHERE pmm.status='A' AND (medicine_name ilike ? OR medicine_name ilike ?) "
      + "ORDER BY medicine_name LIMIT ?";

  private String presPharmaMedicines = "SELECT sid.medicine_name as item_name, "
      + " sid.cust_item_code AS order_code, sid.medicine_id::text as item_id,"
      + " ssd.qty, g.generic_code, cum.cons_uom_id, cum.consumption_uom,"
      + " g.generic_name, sic.item_code AS drug_code, "
      + " 'item_master' as master,'Medicine' as item_type,sid.cons_uom_id,"
      + " sid.route_of_admin, cum.consumption_uom, sid.prior_auth_required, sid.issue_base_unit, "
      + " coalesce(sid.item_form_id, 0) AS item_form_id, "
      + " sid.item_strength,  sid.item_strength_units,"
      + " COALESCE(ifm.granular_units, 'N') as granular_units, "
      + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id, "
      + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name,"
      + " COALESCE(cat.category_payable,'N') AS category_payable, "
      + " COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$') "
      + "     then sir.selling_price_expr::decimal else null end) , "
      + " ssd.mrp, sid.item_selling_price, 0) AS charge, "
      + " (COALESCE((case when textregexeq(sir.selling_price_expr, '^[0-9]+\\.?[0-9]*$') "
      + "     then sir.selling_price_expr::decimal else null end), "
      + " ssd.mrp, sid.item_selling_price, 0) * COALESCE(mc.discount, 0) / 100) "
      + " AS discount, sid.control_type_id, true as applicable " + "FROM store_item_details sid "
      + "JOIN LATERAL (select sum(qty) AS qty, max(sibd.mrp) AS mrp "
      + " from store_stock_details ssd, stores s, store_item_batch_details sibd "
      + " WHERE s.dept_id=ssd.dept_id and "
      + " auto_fill_prescriptions and ssd.medicine_id = sid.medicine_id "
      + " AND ssd.item_batch_id=sibd.item_batch_id "
      + " AND s.center_id=? ) AS ssd ON ssd.qty IS NOT NULL "
      + "LEFT JOIN generic_name g ON (sid.generic_name=g.generic_code) "
      + "JOIN organization_details od ON (od.org_id=?) "
      + "JOIN store_category_master mc ON (mc.category_id = sid.med_category_id) "
      + "LEFT JOIN LATERAL (SELECT sic.medicine_id AS medicine_id, sic.insurance_category_id, "
      + " iic.insurance_category_name, ipd.category_payable "
      + " FROM store_items_insurance_category_mapping sic " + " JOIN item_insurance_categories iic "
      + "    ON(sic.insurance_category_id = iic.insurance_category_id) "
      + " JOIN insurance_plan_details ipd "
      + "    ON(ipd.insurance_category_id = iic.insurance_category_id "
      + " AND ipd.patient_type = ? AND ipd.plan_id=?) "
      + " WHERE sid.medicine_id = sic.medicine_id "
      + " ORDER BY iic.priority LIMIT 1) as cat ON(cat.medicine_id = sid.medicine_id) "
      + "LEFT JOIN item_form_master ifm ON (sid.item_form_id = ifm.item_form_id) "
      + "LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
      + "AND hict.health_authority=? ) "
      + "LEFT JOIN store_item_codes sic ON (sic.medicine_id = hict.medicine_id "
      + "AND sic.code_type = hict.code_type) "
      + "LEFT JOIN store_item_rates sir ON (sir.medicine_id=sid.medicine_id "
      + "AND sir.store_rate_plan_id=od.store_rate_plan_id) "
      + "LEFT JOIN consumption_uom_master cum ON (cum.cons_uom_id = sid.cons_uom_id)"
      + "WHERE sid.status='A' AND mc.prescribable AND (sid.medicine_name ilike ? "
      + "OR sid.medicine_name ilike ? OR g.generic_name ilike ? ) " + "ORDER BY sid.medicine_name "
      + "LIMIT ?";

  /**
   * Get medicines for prescriptions.
   * 
   * @param generics the boolean
   * @param orgId the string
   * @param patientype the string
   * @param insPlanId the integer
   * @param nonHosptl the boolean
   * @param healthAuthority the string
   * @param centerId the integer
   * @param searchQuery the string
   * @param limit the integer
   * @return the list of basic dyna bean
   */
  public List<BasicDynaBean> getMedicinesForPrescription(Boolean generics, String orgId,
      String patientype, Integer insPlanId, Boolean nonHosptl, String healthAuthority,
      Integer centerId, String searchQuery, Integer limit) {
    if (nonHosptl) {
      if (generics) {
        return DatabaseHelper.queryToDynaList(presGenerics,
            new Object[] {searchQuery + "%", "% " + searchQuery + "%", limit});
      } else {
        String query = presPharmaMedicines;
        if (patientype.equals("i")) {
          query = query.replace("auto_fill_prescriptions", "auto_fill_indents");
        }
        return DatabaseHelper.queryToDynaList(query,
            new Object[] {centerId, orgId, patientype, insPlanId, healthAuthority,
                searchQuery + "%", "% " + searchQuery + "%", searchQuery + "%", limit});
      }
    } else {
      return DatabaseHelper.queryToDynaList(presNonHospitalMedicines,
          new Object[] {searchQuery + "%", "% " + searchQuery + "%", limit});
    }
  }
  
  /** getPrescFromConsultationID query. */
  private final String getPrescFromConsultationID = "SELECT COALESCE(pbm_presc_id, 0) "
      + " as pbm_presc_id FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp ON (pmp.op_medicine_pres_id=pp.patient_presc_id) "
      + " WHERE  #filter#=? ORDER BY pp.patient_presc_id LIMIT 1";

  /**
   * returns pbm prescribed id.
   * @param consId consultation id
   * @return pbm prescribed id for the consultation id
   */
  public Integer getPrescriptions(Object consId) {
    String query = getPrescFromConsultationID.replace("#filter#",
        (consId instanceof String) ? "is_discharge_medication=true AND pp.visit_id"
            : "pp.consultation_id");
    BasicDynaBean bean =
        DatabaseHelper.queryToDynaBean(query, new Object[] {consId});
    return (Integer) ((bean == null) ? 0 : bean.get("pbm_presc_id"));
  }

  private final String getLatestPbmPrescID = " SELECT pbmp.* "
      + " FROM patient_prescription pp "
      + " JOIN patient_medicine_prescriptions pmp ON (pp.patient_presc_id=pmp.op_medicine_pres_id) "
      + " JOIN pbm_prescription pbmp USING(pbm_presc_id) "
      + " WHERE pbmp.status = 'O' AND (pbm_request_id IS NULL OR pbm_request_id = '') "
      + " AND #filter# = ? ORDER BY pbm_presc_id DESC LIMIT 1";

  /**
   * Gets latest PBM presc ids.
   * 
   * @param consId the integer
   * @return the integer
   */
  public BasicDynaBean getLatestPBMPresId(Object consId) {
    String query = getLatestPbmPrescID.replace("#filter#",
        (consId instanceof String) ? "is_discharge_medication=true AND pp.visit_id"
            : "pp.consultation_id");
    return DatabaseHelper.queryToDynaBean(query, new Object[] {consId});
  }

  // Need to Look for below code
  public static final String DEATTACH_PBM_FROM_ERX = "UPDATE patient_medicine_prescriptions pmp "
      + " SET pbm_presc_id = NULL from patient_prescription pp "
      + " WHERE pmp.op_medicine_pres_id=patient_presc_id and #filter# = ? ";

  /**
   * Indicator for deattaching pbm from erx is successful.
   * 
   * @param consId the visit id
   * @return indicator for deattaching pbm from erx is successful
   */
  public boolean deAttachPbmFromERX(Object consId) {
    String query = DEATTACH_PBM_FROM_ERX.replace("#filter#",
        (consId instanceof String) ? "is_discharge_medication=true AND pp.visit_id"
            : "pp.consultation_id");
    return DatabaseHelper.update(query, new Object[] {consId}) > 0;
  }

  public static final String ATTACH_PBM_TO_ERX = "UPDATE patient_medicine_prescriptions pmp "
      + " SET pbm_presc_id = ? from patient_prescription pp "
      + " WHERE pmp.op_medicine_pres_id=patient_presc_id and #filter# = ? "
      + " and #dmFilter# pbm_presc_id is NULL";

  /**
   * Updates pbm prescription id for discharge medication.
   * 
   * @param consId the visit id
   * @param pbmPrescId the pbm prescription id
   * @return true if pbmPrescId is set
   */
  public boolean attachPbmToERX(Object consId, Integer pbmPrescId) {
    String query = ATTACH_PBM_TO_ERX
        .replace("#filter#", (consId instanceof String) ? " pp.visit_id" : " pp.consultation_id ")
        .replace("#dmFilter#", "");
    return DatabaseHelper.update(query, new Object[] {pbmPrescId, consId}) > 0;
  }
  
  /**
   * Updates pbm prescription id for discharge medication.
   * 
   * @param consId visitId/consultationId
   * @param pbmPrescId pbmPrescId
   * @param isDischargeMedication whether from discharge medication screen
   * @return true if pbmPrescId is set
   */
  public boolean attachPbmToERX(Object consId, Integer pbmPrescId, boolean isDischargeMedication) {
    String query = ATTACH_PBM_TO_ERX
        .replace("#filter#", (consId instanceof String) ? "pp.visit_id" : "pp.consultation_id")
        .replace("#dmFilter#", (isDischargeMedication) ? "is_discharge_medication=true and " : "");
    return DatabaseHelper.update(query, new Object[] {pbmPrescId, consId}) > 0;
  }

  private static final String GET_PBM_PRESC_ITEMS_WITH_DRUG_COUNT =
      "SELECT pmp.op_medicine_pres_id, "
      + " pmp.frequency, pmp.medicine_quantity, pmp.medicine_remarks, pmp.mod_time, "
      + " pmp.activity_due_date, pmp.medicine_id, pmp.route_of_admin, pmp.strength, "
      + " pmp.generic_code, pmp.item_form_id, pmp.item_strength, pmp.pbm_presc_id, "
      + " pmp.duration_units, pmp.duration, pmp.item_strength_units, pmp.erx_status, "
      + " pmp.erx_denial_code, pmp.erx_denial_remarks, pmp.erx_approved_quantity, "
      + " pmp.cons_uom_id, cum.consumption_uom, pmp.send_for_erx, pp.special_instr,"
      + " pmp.issued_qty, pp.prescribed_date "
      + " FROM patient_medicine_prescriptions pmp "
      + " JOIN patient_prescription pp ON (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " JOIN pbm_prescription USING (pbm_presc_id) "
      + " LEFT JOIN consumption_uom_master cum ON (pmp.cons_uom_id = cum.cons_uom_id)"
      + " WHERE pmp.pbm_presc_id=? AND drug_count=? AND presc_type='Medicine' AND store_item='t'";

  public List<BasicDynaBean> getPbmPrescItemsWithDrugCount(int pbmPrescId, int drugCount) {
    return DatabaseHelper
        .queryToDynaList(GET_PBM_PRESC_ITEMS_WITH_DRUG_COUNT, new Object[] {pbmPrescId, drugCount});
  }
}
