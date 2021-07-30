package com.insta.hms.core.clinical.order.packageitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageOrderItemRepository.
 */
@Repository
public class PackageOrderItemRepository extends OrderItemRepository {

  /** The Constant GENERATE_NEXT_SEQUENCE_QUERY. */
  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  /**
   * Instantiates a new package order item repository.
   */
  public PackageOrderItemRepository() {
    super("package_prescribed");
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#getNextSequence()
   */
  @Override
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY, "package_prescribed_sequence");
  }

  /** The Constant PACKAGES_QUERY. */
  private static final String PACKAGES_QUERY =
      " SELECT (CASE WHEN pm.type = 'P' THEN 'Package' ELSE 'Template' END) AS type, "
      + "  pm.package_id::text AS id, package_name AS name, package_code AS code, "
      + "  CASE WHEN visit_applicability = 'i' then 'IP' when visit_applicability = 'o' THEN 'OP'"
      + "    when visit_applicability = '*' THEN 'OP and IP' ELSE 'DIAG' END AS  department, "
      + "  s.service_sub_group_id as subGrpId, s.service_group_id as groupid, "
      + "  pm.prior_auth_required, pm.insurance_category_id, "
      + "  false as conduction_applicable,false as conducting_doc_mandatory, "
      + "  false AS results_entry_applicable,'N' AS tooth_num_required, "
      + "  pm.multi_visit_package, cpa.center_id, psm.tpa_id, ppm.plan_id,"
      + "  'N' AS mandate_additional_info, '' as additional_info_reqts, pm.submission_batch_type " 
      + " FROM packages pm "
      + "  JOIN service_sub_groups s using(service_sub_group_id) "
      + "  JOIN center_package_applicability cpa ON (pm.package_id=cpa.package_id "
      + "  and cpa.status='A' and (cpa.center_id=? or cpa.center_id=-1)) "
      + "  JOIN dept_package_applicability dpa ON (pm.package_id=dpa.package_id "
      + "  and (dpa.dept_id=? or dpa.dept_id='*')) ";

  /**
   * Gets the item details.
   *
   * @param entityIdList the entity id list
   * @param tpaId the tpa id
   * @param planId the plan id
   * @param centerId the center id
   * @return the item details
   */
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList, List<Object> tpaId,
      List<Object> planId, Integer centerId, String departmentId) {

    StringBuffer query = new StringBuffer();
    query = query.append(PACKAGES_QUERY);
    String[] tpaplaceholdersArr = new String[tpaId.size()];
    Arrays.fill(tpaplaceholdersArr, "?");
    query.append(
        " JOIN package_sponsor_master psm ON (pm.package_id=psm.pack_id "
        + " and psm.status='A' and (psm.tpa_id in (")
        .append(StringUtils.arrayToCommaDelimitedString(tpaplaceholdersArr));
    query.append(") OR psm.tpa_id = '-1'))");
    List<Object> otherParams = new ArrayList<Object>();
    otherParams.add(centerId);
    otherParams.add(departmentId);
    otherParams.addAll(tpaId);
    String[] planplaceholdersArr = new String[planId.size()];
    Arrays.fill(planplaceholdersArr, "?");
    query.append(
        " JOIN package_plan_master ppm ON (pm.package_id=ppm.pack_id "
        + " and ppm.status='A' and (ppm.plan_id in (")
        .append(StringUtils.arrayToCommaDelimitedString(planplaceholdersArr));
    query.append(") OR ppm.plan_id = '-1'))");
    otherParams.addAll(convertStringToInteger(planId));
    if (entityIdList != null && entityIdList.size() > 0) {
      String[] placeholdersArr = new String[entityIdList.size()];
      Arrays.fill(placeholdersArr, "?");
      query.append(" WHERE pm.package_id IN ( ")
          .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
      otherParams.addAll(convertStringToInteger(entityIdList));
    }

    if (otherParams.size() > 0) {
      return DatabaseHelper.queryToDynaList(query.toString(), otherParams.toArray());
    }
    return DatabaseHelper.queryToDynaList(query.toString());
  }

  /** The Constant GET_PACKAGE_APPLICABILITY. */
  private static final String GET_PACKAGE_APPLICABILITY = " SELECT * from packages pm "
      + " JOIN package_sponsor_master psm on (psm.pack_id = pm.package_id)"
      + " JOIN package_plan_master ppm on (ppm.pack_id = pm.package_id)"
      + " WHERE (psm.tpa_id = ? OR tpa_id = '-1') and (ppm.plan_id = ? "
      + " OR ppm.plan_id = -1) AND pm.package_id = ?";

  /**
   * Gets the package applicability.
   *
   * @param packId the pack id
   * @param tpaId the tpa id
   * @param planId the plan id
   * @return the package applicability
   */
  public boolean getPackageApplicability(Integer packId, String tpaId, Integer planId) {
    List<Object> params = new ArrayList<Object>();
    params.add(tpaId);
    params.add(planId);
    params.add(packId);
    List<BasicDynaBean> applicable = new ArrayList<>();
    applicable = DatabaseHelper.queryToDynaList(GET_PACKAGE_APPLICABILITY, params.toArray());
    if (null != applicable && !applicable.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  /** The Constant ORDERED_ITEMS. */
  private static final String ORDERED_ITEMS =
      "SELECT pps.patient_id, pps.prescription_id as order_id, pps.common_order_id,"
      + " 'Package' AS type, "
      + " CASE when pm.package_category_id = -2 THEN 'd' WHEN pm.visit_applicability = 'i'"
      + " THEN 'i' WHEN pm.visit_applicability = 'o' THEN 'o' END as sub_type, "
      + " '' as sub_type_name, 'PKG' as activity_code, "
      + " pps.package_id::text AS item_id, "
      + " COALESCE(bc.act_description, pm.package_name) AS item_name, NULL as item_code,"
      + " COALESCE(bc.posted_date, presc_date) as posted_date, "
      + " pd.doctor_id as pres_doctor_id, pd.doctor_name as pres_doctor_name, "
      + " presc_date AS pres_timestamp, pps.remarks AS remarks, "
      + " TO_CHAR(presc_date,'dd-mm-yyyy') as pres_date, b.bill_rate_plan_id as org_id,  "
      + " b.is_tpa, b.bill_type,b.is_primary_bill, 1 as qty, null as from_timestamp,"
      + " null as to_timestamp, "
      + " COALESCE(pm.multi_visit_package, false) AS multi_visit_package, "
      + " '' AS details, null as operation_ref, null as package_ref, b.bill_no, "
      + " b.status as bill_status,bc.amount,bc.discount, bc.tax_amt, "
      + "  bc.insurance_claim_amount, bc.sponsor_tax_amt, bc.preauth_act_id, (SELECT  "
      + " ( CASE WHEN foo.cancled > 0 THEN 'X' WHEN foo.actual = foo.notconducted  then 'N' "
      + " WHEN foo.actual = foo.conducted  THEN 'C' "
      + " WHEN  foo.notconducted < foo.actual then 'P' END   ) FROM (SELECT ("
      + " SELECT COUNT(*) FROM tests_prescribed tp WHERE tp.package_ref = pp.prescription_id and "
      + " conducted NOT IN ( 'U','X' ) and tp.pat_id = ? ) +"
      + " (SELECT COUNT(*) FROM services_prescribed sp where sp.package_ref = pp.prescription_id "
      + " and conducted NOT IN ( 'U','X' ) and sp.patient_id = ? ) +"
      + " (SELECT COUNT(*) FROM bed_operation_schedule op where op.package_ref = "
      + " pp.prescription_id and status NOT IN ( 'U','X' ) and op.patient_id = ? ) +"
      + " (SELECT COUNT(*) FROM doctor_consultation dc where dc.package_ref = pp.prescription_id "
      + " and status NOT IN ( 'U','X' ) and dc.patient_id = ? ) +"
      + " (SELECT COUNT(*) FROM services_prescribed spp JOIN bed_operation_schedule opp ON "
      + " (opp.prescribed_id=spp.operation_ref) "
      + " WHERE opp.package_ref = pp.prescription_id and conducted NOT IN ( 'U','X' ) and "
      + " spp.patient_id = ?) as actual,"
      + " (SELECT COUNT(*) FROM tests_prescribed tp WHERE tp.package_ref = pp.prescription_id and "
      + " conducted IN ('N','NRN') and tp.outsource_dest_prescribed_id IS NULL and tp.pat_id = ?) "
      + " + (SELECT COUNT(*) FROM services_prescribed sp "
      + " where sp.package_ref = pp.prescription_id "
      + " and conducted IN ('N') and sp.patient_id = ?) +"
      + " (SELECT COUNT(*) FROM bed_operation_schedule op where op.package_ref = "
      + " pp.prescription_id and status IN ('N') and op.patient_id = ?) +"
      + " (SELECT COUNT(*) FROM doctor_consultation dc where dc.package_ref = pp.prescription_id "
      + " and status IN ('A') and dc.patient_id = ?) +"
      + " (SELECT COUNT(*) FROM services_prescribed spp JOIN bed_operation_schedule opp "
      + " ON (opp.prescribed_id=spp.operation_ref)"
      + " WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('N') "
      + " and spp.patient_id = ? ) as notconducted,"
      + " (SELECT COUNT(*) FROM tests_prescribed tp WHERE tp.package_ref = pp.prescription_id "
      + " and conducted IN ('P','RP') and tp.pat_id = ? ) +"
      + " (SELECT COUNT(*) FROM tests_prescribed tp1 WHERE tp1.package_ref = pp.prescription_id "
      + " and conducted IN ('N', 'NRN')"
      + " and tp1.outsource_dest_prescribed_id IS NOT NULL and tp1.pat_id = ? ) +"
      + " (SELECT COUNT(*) FROM services_prescribed sp where sp.package_ref = pp.prescription_id "
      + " and conducted IN ('P') and sp.patient_id = ? ) +"
      + " (SELECT COUNT(*) FROM bed_operation_schedule op where op.package_ref = "
      + " pp.prescription_id and status IN ('P') and op.patient_id = ?) +"
      + " (SELECT COUNT(*) FROM doctor_consultation dc where dc.package_ref = pp.prescription_id "
      + " and status IN ('P') and dc.patient_id = ? ) +"
      + " (SELECT COUNT(*) FROM services_prescribed spp JOIN bed_operation_schedule opp ON "
      + " (opp.prescribed_id=spp.operation_ref)"
      + "  WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('P') "
      + " and spp.patient_id = ? ) as partially,"
      + " (SELECT COUNT(*) FROM tests_prescribed tp WHERE tp.package_ref = pp.prescription_id and "
      + " conducted IN ('C','V','S','RC','RV','CRN') and tp.pat_id = ?) +"
      + "  (SELECT COUNT(*) FROM services_prescribed sp where sp.package_ref = pp.prescription_id "
      + " and conducted IN ('C') and sp.patient_id = ?) +"
      + " (SELECT COUNT(*) FROM bed_operation_schedule op where op.package_ref = "
      + " pp.prescription_id and status IN ('C') and op.patient_id = ?) +"
      + " (SELECT COUNT(*) FROM doctor_consultation dc where dc.package_ref = pp.prescription_id "
      + " and status IN ('C') and dc.patient_id = ? ) +"
      + " (SELECT COUNT(*) FROM services_prescribed spp JOIN bed_operation_schedule opp ON "
      + " (opp.prescribed_id=spp.operation_ref)"
      + " WHERE opp.package_ref = pp.prescription_id and spp.conducted IN ('C') and "
      + " spp.patient_id = ? ) as conducted,"
      + "  (SELECT COUNT(*) FROM package_prescribed ppp where ppp.prescription_id = "
      + " pp.prescription_id and status IN ('X') and ppp.patient_id = ?) as cancled,"
      + " bc.item_excluded_from_doctor, bc.item_excluded_from_doctor_remarks, "
      + " bc.charge_head,bc.charge_group FROM package_prescribed pp"
      + " LEFT JOIN tests_prescribed tp ON (tp.package_ref = pp.prescription_id)"
      + " LEFT JOIN services_prescribed sp ON (sp.package_ref = pp.prescription_id )"
      + " LEFT JOIN bed_operation_schedule op ON (op.package_ref = pp.prescription_id)"
      + " WHERE pp.prescription_id = pps.prescription_id and pp.patient_id = ?"
      + " group by pp.prescription_id) AS foo "
      + " ) as status,'N' as sample_collected,'U' AS finalization_status,bc.prior_auth_id,  "
      + " bc.prior_auth_mode_id, bc.first_of_category,"
      + " '' as cond_doctor_name,null as cond_doctor_id,null as labno,true as canclebill, "
      + " '' AS isdialysis, '' AS dialysis_status, '' AS completion_status,"
      + " '' as urgent,null as tooth_number, bc.insurance_category_id, bc.charge_id, "
      + " null AS outsource_dest_prescribed_id, 'N' as mandate_additional_info, "
      + " '' as additional_info_reqts FROM package_prescribed pps"
      + " JOIN packages pm on pm.package_id = pps.package_id"
      + " LEFT OUTER JOIN doctors pd on pd.doctor_id = pps.doctor_id"
      + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=pps.prescription_id::text "
      + " AND bac.payment_charge_head='PKGPKG' LEFT JOIN bill_charge bc USING(charge_id)"
      + " LEFT JOIN bill b USING (bill_no)"
      + " WHERE pps.patient_id = ? AND multi_visit_package = false ";

  /**
   * get ordered items.
   *
   * @param visitId the visitId
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getOrderedItems(String visitId) {
    List<Object> params = new ArrayList<Object>();
    for (int i = 0; i < 24; i++) {
      params.add(visitId);
    }
    return DatabaseHelper.queryToDynaList(ORDERED_ITEMS, params.toArray());
  }

  /* (non-Javadoc)
   * @see com.insta.hms.core.clinical.order.master.OrderItemRepository#getPackageRefQuery()
   */
  @Override
  public String getPackageRefQuery() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.core.clinical.order.master.OrderItemRepository#getOperationRefQuery()
   */
  @Override
  public String getOperationRefQuery() {
    return null;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.core.clinical.order.master.
   * OrderItemRepository#getPackageRefOrders(java.lang.String, java.util.List)
   */
  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    return Collections.emptyList();
  }

}
