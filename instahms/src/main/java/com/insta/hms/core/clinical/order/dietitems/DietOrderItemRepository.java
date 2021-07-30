package com.insta.hms.core.clinical.order.dietitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DietOrderItemRepository extends OrderItemRepository {

  public DietOrderItemRepository() {
    super("diet_prescribed");
  }

  private static final String DIET_QUERY = " SELECT 'Meal' AS type, diet_id::text as id, "
      + "  meal_name AS name, null as code, '' AS department, s.service_sub_group_id as subGrpId, "
      + "  s.service_group_id as groupid,'N' as prior_auth_required,"
      + "  insurance_category_id, false as conduction_applicable,false as conducting_doc_mandatory,"
      + "  false as results_entry_applicable,'N' as tooth_num_required, "
      + "  false as multi_visit_package, -1 as center_id, '-1' as tpa_id,  "
      + "  'N' as mandate_additional_info ,'' as additional_info_reqts" 
      + " FROM diet_master dm "
      + "  JOIN service_sub_groups s using(service_sub_group_id) ";

  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(DIET_QUERY, convertStringToInteger(entityIdList), "dm.diet_id",
        null);
  }

  private static final String ORDERED_ITEMS = "SELECT dp.visit_id as patient_id, "
      + " dp.ordered_id as order_id, dp.common_order_id, "
      + " 'Meal' as type, '' as sub_type, '' as sub_type_name, 'DIE' as activity_code, "
      + " b.bill_rate_plan_id as org_id, b.is_tpa, b.bill_type, "
      + " dp.diet_id::text as item_id, dm.meal_name AS item_name, NULL as item_code,"
      + " dp.ordered_by as pres_doctor_id, pd.doctor_name as pres_doctor_name, "
      + " ordered_time as pres_timestamp, COALESCE(bc.posted_date, ordered_time) as posted_date, "
      + " special_instructions as remarks,TO_CHAR(ordered_time,'dd-mm-yyyy') as pres_date, "
      + " 1 AS qty, meal_date + COALESCE(meal_time,'00:00:00') as from_timestamp, "
      + " meal_timing, null as to_timestamp,"
      + " (CASE WHEN meal_timing = 'Spl' THEN to_char(meal_date + meal_time,'DD-MM-YYYY HH:MI')"
      + " ELSE to_char(meal_date, 'DD-MM-YYYY ') || meal_timing END) AS details,"
      + " null as operation_ref, package_ref, b.is_primary_bill, b.bill_no,b.status as bill_status,"
      + " bc.amount, bc.tax_amt, bc.insurance_claim_amount, bc.sponsor_tax_amt, "
      + " (CASE WHEN dp.status = 'Y' THEN 'C' ELSE dp.status END) AS status,"
      + " 'N' as sample_collected, 'U' as finalization_status, "
      + " bc.prior_auth_id,  bc.prior_auth_mode_id, bc.first_of_category,"
      + " '' as cond_doctor_name,null as cond_doctor_id,null as labno,true as canclebill, "
      + " '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,"
      + " '' as urgent,null as tooth_number, bc.insurance_category_id, bc.charge_id, "
      + " null AS outsource_dest_prescribed_id, 'N' as mandate_additional_info, "
      + " '' as additional_info_reqts, "
      + " COALESCE(pm.multi_visit_package, false) AS multi_visit_package, pm.package_id, "
      + " pp.pat_package_id, bc.item_excluded_from_doctor, "
      + " bc.item_excluded_from_doctor_remarks,bc.charge_head,bc.charge_group"
      + " FROM diet_prescribed dp" + " JOIN diet_master dm ON dm.diet_id = dp.diet_id"
      + " LEFT JOIN doctors pd ON pd.doctor_id = dp.ordered_by"
      + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = dp.package_ref) "
      + " LEFT JOIN packages pm ON CASE WHEN package_ref IS NULL "
      + "   THEN (pm.package_id::text = dp.diet_id::text) "
      + "   ELSE (pm.package_id = pp.package_id) END "
      + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dp.ordered_id::text AND "
      + " bac.activity_code='DIE'" 
      + " LEFT JOIN bill_charge bc USING (charge_id)"
      + " LEFT JOIN bill b USING (bill_no) WHERE dp.visit_id = ?  ";

  /**
   * get Ordered Items.
   * 
   * @param visitId
   *          the visitId
   * @param packageRef
   *          the packageRef
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getOrderedItems(String visitId, Boolean packageRef) {
    String packageRefCondition = IGNORE_MVP_ITEM_CONDITION;
    if (packageRef != null && packageRef) {
      packageRefCondition = GET_MVP_ITEM_CONDITION;
    }
    return DatabaseHelper.queryToDynaList(ORDERED_ITEMS + packageRefCondition, visitId);
  }

  private static final String CHARGE_QUERY = "SELECT "
      + " dc.charge, dc.discount, dm.service_tax, dm.diet_id, dm.meal_name,dm.service_sub_group_id,"
      + " dm.insurance_category_id, billing_group_id " 
      + " FROM  diet_charges dc "
      + " JOIN diet_master dm ON (dm.diet_id = dc.diet_id) "
      + " WHERE dc.diet_id=? AND bed_type=? AND org_id=? ";

  public BasicDynaBean getDietChargesBean(int dietId, String bedType, String orgId) {
    return DatabaseHelper.queryToDynaBean(CHARGE_QUERY, new Object[] { dietId, bedType, orgId });
  }

  private static final String ALL_CHARGE_QUERY = "SELECT "
      + " charge, discount, bed_type "
      + " FROM  diet_charges "
      + " WHERE diet_id=? AND org_id=? ";

  public List<BasicDynaBean> getAllDietChargesBean(int dietId, String orgId) {
    return DatabaseHelper.queryToDynaList(ALL_CHARGE_QUERY, new Object[] {dietId, orgId});
  }

  private static final String GET_PACKAGE_OPERATION_REF = "SELECT ordered_id as order_id, "
      + " 'Meal' as type FROM diet_prescribed WHERE visit_id = ? ";

  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

  @Override
  public String getOperationRefQuery() {
    return null;
  }

}
