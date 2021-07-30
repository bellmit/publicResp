package com.insta.hms.core.clinical.order.otheritmes;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OtherOrderItemRepository extends OrderItemRepository {

  /** The Constant GENERATE_NEXT_SEQUENCE_QUERY. */
  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  public OtherOrderItemRepository() {
    super("other_services_prescribed");
  }

  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY, "ip_other_services_sequence");
  }

  private static final String OTHER_CHARGES_QUERY = " SELECT 'Other Charge' AS type, "
      + "  charge_name as id, charge_name AS name, "
      + "  othercharge_code as code, '' AS department, "
      + "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid, "
      + "  'N' as prior_auth_required, "
      + "  insurance_category_id,false as conduction_applicable,false as conducting_doc_mandatory, "
      + "  false as results_entry_applicable,'N' as tooth_num_required, "
      + "  false as multi_visit_package, -1 as center_id, '-1' as tpa_id, "
      + "  'N' as mandate_additional_info, '' as additional_info_reqts "
      + "  FROM common_charges_master cm "
      + "  JOIN service_sub_groups s using(service_sub_group_id) ";

  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList) {
    return super.getItemDetails(OTHER_CHARGES_QUERY, entityIdList, "cm.charge_name", null);
  }

  private static final String ORDERED_ITEMS = "SELECT osp.patient_id, "
      + " osp.prescribed_id as order_id, osp.common_order_id,"
      + " 'Other Charge' as type, service_group as sub_type, "
      + " chc.chargehead_name as sub_type_name, 'OTC' as activity_code,"
      + " service_name as item_id, service_name as item_name, NULL as item_code,"
      + " osp.doctor_id as pres_doctor_id, pd.doctor_name as pres_doctor_name, "
      + " pres_time as pres_timestamp, COALESCE(bc.posted_date, pres_time) as posted_date, "
      + " osp.remarks as remarks,TO_CHAR(pres_time,'dd-mm-yyyy') as pres_date, "
      + " b.bill_rate_plan_id as org_id, b.is_tpa, b.bill_type, "
      + " b.is_primary_bill, osp.quantity as qty, null, null,"
      + "  osp.quantity::text || ' No(s)' AS details, operation_ref, package_ref, "
      + " b.bill_no, b.status as bill_status, bc.amount, bc.tax_amt, "
      + " bc.insurance_claim_amount, bc.sponsor_tax_amt, "
      + " (CASE WHEN cancel_status = 'C' THEN 'X' ELSE 'U' END) as status,'N' as sample_collected,"
      + " 'U' as finalization_status,bc.prior_auth_id, "
      + " bc.prior_auth_mode_id,  bc.first_of_category,"
      + " '' as cond_doctor_name,null as cond_doctor_id,null as labno,true as canclebill, "
      + " '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,"
      + " '' as urgent,null as tooth_number, bc.insurance_category_id, bc.charge_id, "
      + " null AS outsource_dest_prescribed_id,"
      + " 'N' as mandate_additional_info, '' as additional_info_reqts, "
      + " COALESCE(pm.multi_visit_package, false) AS multi_visit_package, pm.package_id, "
      + " pp.pat_package_id, bc.item_excluded_from_doctor, "
      + " bc.item_excluded_from_doctor_remarks,bc.charge_head,bc.charge_group, bc.preauth_act_id, "
      + " ppc.content_id_ref, ppc.patient_package_content_id "
      + " FROM other_services_prescribed osp"
      + " LEFT OUTER JOIN doctors pd on pd.doctor_id = osp.doctor_id"
      + " JOIN chargehead_constants chc ON chargehead_id=osp.service_group"
      + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = osp.package_ref) "
      + " LEFT JOIN patient_package_content_consumed ppcc "
      + " ON (ppcc.prescription_id = osp.prescribed_id AND "
      + "   ppcc.item_type IN ('Other Charge')) "
      + " LEFT JOIN patient_package_contents ppc "
      + " ON (ppcc.patient_package_content_id = ppc.patient_package_content_id) "
      + " LEFT JOIN packages pm ON CASE WHEN package_ref IS NULL "
      + "   THEN (pm.package_id::text = service_name) "
      + "   ELSE (pm.package_id = pp.package_id) END "
      + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=osp.prescribed_id::text "
      + " AND bac.activity_code='OTC'"
      + " LEFT JOIN bill_charge bc USING (charge_id)" 
      + " LEFT JOIN bill b USING (bill_no)"
      + " WHERE osp.patient_id = ? ";

  /**
   * get ordered items.
   * 
   * @param visitId the visitId
   * @param operationRef the operationRef
   * @param packageRef the packageRef
   * @return list of basic dyan bean
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

  private static final String GET_PACKAGE_OPERATION_REF = "SELECT prescribed_id as order_id, "
      + " 'Other Charge' as type FROM other_services_prescribed WHERE patient_id = ? ";

  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

  @Override
  public String getOperationRefQuery() {
    return GET_PACKAGE_OPERATION_REF;
  }

}
