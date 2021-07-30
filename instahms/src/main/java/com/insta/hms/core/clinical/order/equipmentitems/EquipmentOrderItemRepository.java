package com.insta.hms.core.clinical.order.equipmentitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Repository
public class EquipmentOrderItemRepository extends OrderItemRepository {

  public EquipmentOrderItemRepository() {
    super("equipment_prescribed");
  }

  private static final String EQUIPMENT_QUERY = " SELECT 'Equipment' AS type, e.eq_id AS id, "
      + "  e.equipment_name AS name, e.equipment_code AS code, "
      + "  dept.dept_name AS department, e.dept_id, e.status, "
      + "  coalesce(e.min_duration,0) as min_duration, e.slab_1_threshold, "
      + "  e.duration_unit_minutes, coalesce(e.incr_duration,0) as incr_duration, e.charge_basis, "
      + "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid, "
      + "  'N' as prior_auth_required, "
      + "  insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, "
      + "  false as results_entry_applicable,'N' as tooth_num_required, "
      + "  false as multi_visit_package,-1 as center_id, '-1' as tpa_id, "
      + "  'N' as mandate_additional_info,'' as additional_info_reqts "
      + "  FROM equipment_master e " + "  JOIN department dept USING (dept_id) "
      + "  JOIN service_sub_groups s using(service_sub_group_id) ";

  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(EQUIPMENT_QUERY, entityIdList, "e.eq_id", null);
  }

  private static final String ORDERED_ITEMS = "SELECT ep.patient_id, ep.prescribed_id as order_id, "
      + " ep.common_order_id, 'Equipment' as type, "
      + " '' as sub_type, '' as sub_type_name, 'EQU' as activity_code, ep.units, "
      + " COALESCE(bc.posted_date, ep.date) as posted_date, "
      + " ep.eq_id as item_id, e.equipment_name as item_name, e.equipment_code as item_code,"
      + " ep.doctor_id as pres_doctor_id, pd.doctor_name as pres_doctor_name, "
      + " ep.date as pres_timestamp, ep.remarks as remarks,TO_CHAR(date,'dd-mm-yyyy') as pres_date,"
      + " b.bill_rate_plan_id as org_id,  b.is_tpa, b.bill_type, b.is_primary_bill, "
      + " duration as qty, used_from as from_timestamp, used_till as to_timestamp,"
      + " (CASE WHEN used_from::date = used_till::date THEN "
      + " to_char(used_from, 'DD-MM-YYYY HH24:MI - ') || to_char(used_till, 'HH24:MI')"
      + " ELSE to_char(used_from, 'DD-MM-YYYY HH24:MI - ') || "
      + " to_char(used_till, 'DD-MM-YYYY HH24:MI') END) AS details,"
      + " operation_ref, package_ref, b.bill_no, b.status as bill_status,bc.amount, bc.tax_amt, "
      + " bc.insurance_claim_amount, bc.sponsor_tax_amt, "
      + " (CASE WHEN cancel_status = 'C' THEN 'X' ELSE 'U' END) as status,'N' as sample_collected,"
      + " finalization_status,bc.prior_auth_id, bc.prior_auth_mode_id, bc.first_of_category,"
      + "'' as cond_doctor_name,null as cond_doctor_id,null as labno,true as canclebill, "
      + " '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,"
      + " '' as urgent,null as tooth_number, bc.insurance_category_id, bc.charge_id, "
      + " null AS outsource_dest_prescribed_id,"
      + " 'N' as mandate_additional_info, '' as additional_info_reqts, "
      + " COALESCE(pm.multi_visit_package, false) AS multi_visit_package, pm.package_id, "
      + " pp.pat_package_id, bc.item_excluded_from_doctor, "
      + " bc.item_excluded_from_doctor_remarks,bc.charge_head,bc.charge_group, "
      + " ppc.content_id_ref, ppc.patient_package_content_id "
      + " FROM equipment_prescribed ep" + " JOIN equipment_master e ON e.eq_id = ep.eq_id"
      + " LEFT OUTER JOIN doctors pd on pd.doctor_id = ep.doctor_id"
      + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = ep.package_ref) "
      + " LEFT JOIN patient_package_content_consumed ppcc "
      + " ON (ppcc.prescription_id = ep.prescribed_id AND ppcc.item_type='Equipment') "
      + " LEFT JOIN patient_package_contents ppc "
      + " ON (ppcc.patient_package_content_id = ppc.patient_package_content_id) "
      + " LEFT JOIN packages pm ON " + " CASE WHEN package_ref IS NULL "
      + "   THEN (pm.package_id::text = ep.eq_id) " + " ELSE (pm.package_id = pp.package_id) END "
      + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=ep.prescribed_id::text AND "
      + " bac.activity_code='EQU'"
      + " LEFT JOIN bill_charge bc USING (charge_id)" + " LEFT JOIN bill b USING (bill_no)"
      + " WHERE ep.patient_id = ? ";


  /**
   * Gets the ordered items.
   *
   * @param visitId the visit id
   * @param operationRef the operation ref
   * @param packageRef the package ref
   * @return the ordered items
   */
  public List<BasicDynaBean> getOrderedItems(String visitId, Integer operationRef,
      Boolean packageRef) {
    Object[] values;
    String packageRefCondition = IGNORE_MVP_ITEM_CONDITION;
    if (packageRef != null && packageRef) {
      packageRefCondition = GET_MVP_ITEM_CONDITION;
    }

    String operationRefCondition;
    if (operationRef == null) {
      operationRefCondition = IGNORE_OPERATION_ITEM_CONDITION;
      values = new Object[] { visitId };
    } else {
      operationRefCondition = GET_OPERATION_ITEM_CONDITION;
      values = new Object[] { visitId, operationRef };
    }

    return DatabaseHelper
        .queryToDynaList(ORDERED_ITEMS + operationRefCondition + packageRefCondition, values);
  }

  private static final String GET_PACKAGE_REF = "SELECT prescribed_id as order_id, "
      + " 'Equipment' as type FROM equipment_prescribed WHERE patient_id = ? ";

  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_REF;
  }

  @Override
  public String getOperationRefQuery() {
    return GET_PACKAGE_REF;
  }

  private static final String GET_FINALIZATION_STATUS = "SELECT ep.prescribed_id, "
      + " ep.finalization_status, ep.eq_id, ep.units, b.bill_no FROM equipment_prescribed ep "
      + " JOIN bill_activity_charge bac ON bac.activity_id=ep.prescribed_id::text AND "
      + " bac.activity_code='EQU' "
      + " LEFT JOIN bill_charge bc USING (charge_id) LEFT JOIN bill b USING (bill_no) ";

  /**
   * get finalization status of prescribed equipment.
   * @param prescribedIdsList the prescribedIdsList
   * @return list of basic dyana bean
   */
  public List<BasicDynaBean> getFinalizationStatus(List<Integer> prescribedIdsList) {
    StringBuilder query = new StringBuilder(GET_FINALIZATION_STATUS);

    if (prescribedIdsList != null && !prescribedIdsList.isEmpty()) {
      String[] placeholdersArr = new String[prescribedIdsList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" WHERE prescribed_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      return DatabaseHelper.queryToDynaList(query.toString(), prescribedIdsList.toArray());
    }

    return Collections.emptyList();
  }
}
