package com.insta.hms.mdm.ordersets;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository("PackagesRepository")
public class PackagesRepository extends MasterRepository<Integer> {

  @LazyAutowired
  private SessionService sessionService;

  public PackagesRepository() {
    super("packages", "package_id", "package_name");
  }

  private static final String PACKAGE_DETAILS = "SELECT p.*, ssg.service_group_id "
      + " FROM packages p " + " LEFT JOIN service_sub_groups ssg using(service_sub_group_id) "
      + " WHERE p.package_id = ? ";

  @Override
  public String getViewQuery() {
    return PACKAGE_DETAILS;
  }

  /**
   * Gets the packages.
   *
   * @param params
   *          the params
   * @param context
   *          the context
   * @return the packages
   * @throws ParseException
   *           the parse exception
  */
  public Map<String, Object> getPackages(InstaLinkedMultiValueMap<String, Object> params,
      String context) throws ParseException {

    Integer centerId = ((String) params.getFirst("center_applicability") != null
            && !((String) params.getFirst("center_applicability")).isEmpty())
            ? Integer.parseInt((String) params.getFirst("center_applicability")) : null;
    List<Object> queryArgs = new ArrayList<>();
    List<String> filters = new ArrayList<>();
    StringBuilder query = new StringBuilder(
        "SELECT p.package_id, p.package_name, p.visit_applicability, p.valid_till, p.status,"
            + " p.package_category_id, p.package_code, "
            + " count(p.package_id) OVER (PARTITION BY 1)");
    /* All the select fields */
    if (!context.equals("O")) {
      query.append(" ,pc.charge as bed_charges ");
    }
    /* From Clause */
    query.append(" FROM packages p ");

    /* All the join conditions */
    if (!context.equals("O")) {
      query.append(
          " LEFT JOIN package_charges pc using(package_id)");
    }
    Integer currentCenterId = (Integer) sessionService.getSessionAttributes().get("centerId");
    if (currentCenterId == 0) {
      // if current center id default then filter only if the url params has centerId.
      if (centerId != null) {
        query.append(" JOIN center_package_applicability ca on (ca.package_id = p.package_id "
            + " AND (ca.center_id = ? OR ca.center_id = -1 ))");
        queryArgs.add(centerId);
      }
    } else if (currentCenterId != 0) {
      // if current center is not default then filter on currentCenterId.
      query.append(" JOIN center_package_applicability ca on (ca.package_id = p.package_id "
          + " AND (ca.center_id = ? OR ca.center_id = -1 ))");
      queryArgs.add(currentCenterId);
    }

    String deptId = (String) params.getFirstOrDefault("dept_applicability", null);
    if (deptId != null) {
      query.append(" JOIN dept_package_applicability da on (da.package_id = p.package_id "
          + " AND da.dept_id = ?)");
      queryArgs.add(deptId);
    }

    String packageName = (String) params.getFirstOrDefault("package_name", null);
    String packageCode = (String) params.getFirstOrDefault("package_code", null);
    Integer packageId = ((String) params.getFirst("package_id") != null
            && !((String) params.getFirst("package_id")).isEmpty())
            ? Integer.parseInt((String) params.getFirst("package_id")) : null;

    /* All the Filters. */
    if (packageName != null) {
      filters.add(" package_name ilike ? ");
      queryArgs.add("%" + packageName.trim() + "%");
    }
    if (packageId != null) {
      filters.add(" p.package_id = ? ");
      queryArgs.add(packageId);
    }
    if (packageCode != null) {
      filters.add(" package_code ilike ? ");
      queryArgs.add("%" + packageCode.trim() + "%");
    }
    Integer packageCategoryId = (String) params.getFirst("package_category_id") != null
        ? Integer.parseInt((String) params.getFirstOrDefault("package_category_id", null)) : null;
    if (packageCategoryId != null) {
      filters.add(" package_category_id = ? ");
      queryArgs.add(packageCategoryId);
    }

    Boolean multiVisitPackage = (String) params.getFirst("multi_visit_package") != null
        ? Boolean.parseBoolean((String) params.getFirstOrDefault("multi_visit_package", null))
        : null;

    if (multiVisitPackage != null) {
      filters.add(" multi_visit_package = ? ");
      queryArgs.add(multiVisitPackage);
    }
    LinkedList<String> approvalStatuses = ((LinkedList) params.get("approval_statuses"));
    if (approvalStatuses != null && !approvalStatuses.isEmpty()) {
      String[] approvalStatusesPlaceholderArray = new String[approvalStatuses.size()];
      Arrays.fill(approvalStatusesPlaceholderArray, "?");
      String approvalStatusesPlaceholder = StringUtils
          .arrayToCommaDelimitedString(approvalStatusesPlaceholderArray);
      filters.add(" approval_status IN (" + approvalStatusesPlaceholder + ")");
      queryArgs.addAll(approvalStatuses);
    }

    String noSponsorFlag = (String) params.getFirstOrDefault("no_sponsor_flag", "N");
    String tpaId = (String) params.getFirstOrDefault("tpa_id", null);

    if (noSponsorFlag.equals("Y")) {
      filters.add(" EXISTS (SELECT tpa_id from package_sponsor_master "
                  + "WHERE pack_id = p.package_id AND tpa_id = '0')");
    }

    if (tpaId != null) {
      filters.add(" EXISTS (SELECT tpa_id from package_sponsor_master "
                  + " WHERE pack_id = p.package_id AND tpa_id = ?)");
      queryArgs.add(tpaId);
    }

    String visitApplicability = (String) params.getFirstOrDefault("visit_applicability", null);
    if (visitApplicability != null) {
      filters.add(" visit_applicability = ? ");
      queryArgs.add(visitApplicability);
    }

    String genderApplicability = (String) params.getFirstOrDefault("gender_applicability", null);
    if (genderApplicability != null) {
      filters.add(" gender_applicability = ? ");
      queryArgs.add(genderApplicability);
    }

    String validFrom = (String) params.getFirstOrDefault("valid_from", null);
    if (validFrom != null) {
      filters.add(" ( valid_from <= ? OR valid_from is NULL ) ");
      queryArgs.add(DateUtil.parseDate(validFrom));
    }

    String validTill = (String) params.getFirstOrDefault("valid_till", null);
    if (validTill != null) {
      filters.add(" ( valid_till >= ? OR valid_till is NULL ) ");
      queryArgs.add(DateUtil.parseDate(validTill));
    }

    String status = (String) params.getFirstOrDefault("status", null);

    if (status != null) {
      filters.add(" p.status = ? ");
      queryArgs.add(status);
    }

    if (context != null && !"".equals(context)) {
      filters.add(" type = ? ");
      queryArgs.add(context);
    }
    if (!context.equals("O")) {
      String orgId = (String) params.getFirstOrDefault("org_id", null);
      if (orgId == null) {
        filters.add(" org_id = ? ");
        queryArgs.add("ORG0001");
      }

      String bedType = (String) params.getFirstOrDefault("bed_type", null);
      if (bedType == null) {
        filters.add(" bed_type = ? ");
        queryArgs.add("GENERAL");
      }
    }

    if (!filters.isEmpty()) {
      query.append(" WHERE " + StringUtils.collectionToDelimitedString(filters, " AND"));
    }

    query.append(" ORDER BY lower(package_name)");

    Integer pageSize = Integer.parseInt((String) params.getFirstOrDefault("page_size", "15"));
    if (pageSize != 0) {
      query.append(" LIMIT ?");
      queryArgs.add(pageSize);
    }

    Integer pageNum = Integer.parseInt((String) params.getFirstOrDefault("page_num", "0"));
    if (pageNum != 0) {
      query.append(" OFFSET ?");
      queryArgs.add((pageNum) * pageSize);
    }
    Map<String, Object> resultMap = new HashMap<>();
    List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(query.toString(),
        queryArgs.toArray());
    resultMap.put("page_size", pageSize);
    resultMap.put("page_num", pageNum);
    resultMap.put("result_list", ConversionUtils.listBeanToListMap(results));
    resultMap.put("total_records", !results.isEmpty() ? results.get(0).get("count") : 0);
    return resultMap;
  }

  private static final String GET_PACKAGE_COMPONENTS = "SELECT type, activity_id, "
      + " activity_type AS item_type, pc.dept_id, pc.doctor_id, p.package_id,'' as operation_id, "
      + " coalesce(test.test_name, s.service_name, doc.doctor_name,"
      + "    d.dept_name, em.equipment_name, 'Doctor') as activity_description,"
      + " charge_head, chargehead_name, activity_qty, activity_qty_uom, p.description, "
      + " p.service_sub_group_id,consultation_type_id,0 as activity_charge,"
      + " '' as insurance_category_id,'' as package_category_id, package_content_id as pack_ob_id,"
      + " false as multi_visit_package,p.package_name, "
      + " (case when coalesce(test.conducting_doc_mandatory, s.conducting_doc_mandatory, 'N') = 'O'"
      + "    then true else false end) as conducting_doc_mandatory, coalesce(test.test_name,"
      + " s.service_name, '') as item_name, pc.activity_remarks as remarks, "
      + " coalesce(test.mandate_additional_info, 'N') as mandate_additional_info, "
      + " coalesce(test.additional_info_reqts, '') as additional_info_reqts" + " FROM packages p "
      + " LEFT JOIN package_contents pc ON p.package_id = pc.package_id "
      + " LEFT JOIN doctors doc ON (pc.doctor_id = doc.doctor_id AND pc.activity_type = 'Doctor') "
      + " LEFT JOIN services s ON (pc.activity_id=s.service_id AND pc.activity_type = 'Service') "
      + " LEFT JOIN diagnostics test ON (test.test_id=pc.activity_id AND"
      + "     (pc.activity_type = 'Laboratory' OR pc.activity_type = 'Radiology')) "
      + " LEFT JOIN equipment_master em ON (em.eq_id = pc.activity_id AND "
      + "     pc.activity_type = 'Equipment')"
      + " LEFT JOIN department d ON (pc.dept_id = d.dept_id AND pc.activity_type = 'Department')"
      + " LEFT JOIN chargehead_constants cc ON cc.chargehead_id = charge_head "
      + " WHERE p.package_id = ? ORDER BY pc.display_order";

  public List<BasicDynaBean> getPackageComponents(Integer packageId) {
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_COMPONENTS, new Object[] { packageId });
  }

  private static final String PACKAGE_CONTENT_DETAIL = "SELECT * FROM package_contents"
      + " WHERE package_content_id = ? and package_id=? " ;

  public BasicDynaBean getPackageContentDetail(Integer packageId, Integer packageContentId) {
    return DatabaseHelper.queryToDynaBean(PACKAGE_CONTENT_DETAIL,
        new Object[] { packageContentId, packageId });
  }

  private static final String GET_ORDER_SETS_FOR_PRESCRIPTION = "Select "
      + " p.package_name AS item_name, p.package_code AS order_code,"
      + " p.package_id::text AS item_id, p.description, "
      + " (case when exists (select * from package_contents "
      + "   where package_id=p.package_id and (activity_type in ('Meal','Bed', 'Equipment', "
      + "   'Department') or (activity_type='Doctor' AND doctor_id is NULL))) then false "
      + "   else true end) as is_prescribable,"
      + " 'Order Sets' AS item_type, 0 AS charge, 0 AS discount From packages p "
      + "JOIN center_package_applicability cpa ON (cpa.center_id IN (-1, ?) "
      + " AND cpa.package_id=p.package_id) "
      + "JOIN dept_package_applicability dpa ON (dpa.dept_id IN ('*', ?) "
      + " AND dpa.package_id=p.package_id) "
      + " WHERE p.type='O' AND NOT p.multi_visit_package AND p.status='A' "
      + " AND p.gender_applicability IN (?, '*') AND "
      + " p.visit_applicability IN (?, '*') AND (p.valid_from is NULL OR (p.valid_from < ?)) AND "
      + " (p.valid_till is NULL OR (p.valid_till > ?))"
      + "  AND (p.package_name ilike ? OR p.package_name ilike ? OR p.package_code ilike ?)";

  /**
   * Gets the ordersets for prescription.
   *
   * @param visitType the visit type
   * @param gender the gender
   * @param centerId the center id
   * @param deptId the dept id
   * @param fromDate the from date
   * @param toDate the to date
   * @param searchQuery the search query
   * @return the ordersets for prescription
   */
  public List<BasicDynaBean> getOrdersetsForPrescription(String visitType, String gender,
      Integer centerId, String deptId, Date fromDate, Date toDate, String searchQuery) {
    return DatabaseHelper.queryToDynaList(GET_ORDER_SETS_FOR_PRESCRIPTION,
        new Object[] { centerId, deptId, gender, visitType, fromDate, toDate, searchQuery + "%",
            "% " + searchQuery + "%", searchQuery + "%" });
  }

  private static final String ACTIVE_PACKAGES_FOR_BILLS =
      "SELECT bc.bill_no, bc.charge_id, p.package_id, p.insurance_category_id, pp.pat_package_id,"
      + " p.package_name, to_char(pkp.presc_date,'YYYY-MM-DD HH24:MI:SS') AS presc_date"
      + " FROM bill_charge bc JOIN bill_activity_charge bac ON "
      + " (bc.charge_id =  bac.charge_id AND bac.payment_charge_head = 'PKGPKG') JOIN "
      + " package_prescribed pkp ON (bac.activity_id = CAST(pkp.prescription_id AS varchar)) "
      + "JOIN patient_packages pp ON (  pkp.pat_package_id =pp.pat_package_id)  JOIN packages "
      + " p ON (pp.package_id  = p.package_id )  WHERE bc.charge_head = "
      + " 'PKGPKG' AND pp.status IN('P')  AND p.multi_visit_package = false AND bc.status != 'X' "
      + " AND bc.bill_no IN (:bills) ";

  /**
   * Gets the active packages for bills.
   *
   * @param billNos the bill nos
   * @return the active packages for bills
   */
  public List<BasicDynaBean> getActivePackagesForBills(List<String> billNos) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("bills", billNos);

    return DatabaseHelper.queryToDynaList(ACTIVE_PACKAGES_FOR_BILLS, parameters);

  }

  private static final String GET_PACKAGE_INVENTORY_DETAILS = "SELECT "
      + "pc.patient_package_content_id, pc.patient_package_id, "
      + " pc.package_content_id as pack_ob_id,"
      + " pc.activity_id as medicine_id, COALESCE(pc.activity_qty-SUM(ppc.quantity),"
      + " pc.activity_qty)  as quantity, activity_qty as total_quantity, pc.activity_type"
      + " as type, pcc.charge, p.insurance_category_id "
      + " FROM patient_package_contents pc  "
      + " JOIN package_content_charges pcc"
      + " ON (pcc.package_content_id = pc.package_content_id) "
      + " LEFT JOIN patient_package_content_consumed ppc "
      + " ON (ppc.patient_package_content_id = pc.patient_package_content_id) "
      + " LEFT JOIN packages p ON(p.package_id = pc.package_id) "
      + " WHERE pc.patient_package_id= ?"
      + " AND pcc.org_id= ? AND pcc.bed_type= ?  AND pc.charge_head = 'INVITE'"
      + " GROUP BY pc.patient_package_content_id, pcc.charge, p.insurance_category_id";

  public List<BasicDynaBean> getPkgInvDetails(int patPkgId, String orgId, String bedType) {
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_INVENTORY_DETAILS,
        new Object[] {patPkgId, orgId, bedType});
  }

  private static final String PACKAGE_ITEM_DETAILS = "SELECT medicine_id, medicine_name, "
      + " issue_base_unit, issue_units as uom_display, CASE  WHEN issue_units != "
      + " package_uom AND package_uom != NULL THEN 'P' ELSE 'I' END as qty_unit, package_uom "
      + " FROM store_item_details st WHERE medicine_id IN (:medicines)";

  /**
   * Gets the pkg items.
   *
   * @param medicineIds the medicine ids
   * @return the pkg items
   */
  public List<BasicDynaBean> getPkgItems(List<Integer> medicineIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("medicines", medicineIds);

    return DatabaseHelper.queryToDynaList(PACKAGE_ITEM_DETAILS, parameters);
  }

  private static final String GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS = "SELECT "
      + " isg.item_subgroup_id, isg.subgroup_code, isg.item_subgroup_name, ig.group_code "
      + " FROM package_item_sub_groups pisg "
      + " JOIN item_sub_groups isg ON(pisg.item_subgroup_id = isg.item_subgroup_id) "
      + " JOIN item_groups ig ON (ig.item_group_id = isg.item_group_id) "
      + " WHERE pisg.package_id = ? ";

  /**
   * Gets the package item sub group tax details.
   *
   * @param packageId the package id
   * @return the package item sub group tax details
   */
  public List<BasicDynaBean> getPackageItemSubGroupTaxDetails(Integer packageId) {
    return DatabaseHelper.queryToDynaList(GET_PACKAGE_ITEM_SUB_GROUP_TAX_DETAILS,
        new Object[] { packageId });
  }

  private static final String GET_MULTI_PACKAGE_INV_DETAILS =
      "SELECT pc.patient_package_content_id, pc.patient_package_id, pc.package_content_id as "
      + " pack_ob_id, pc.activity_id as medicine_id, COALESCE(pc.activity_qty-SUM(ppc.quantity),"
      + " pc.activity_qty) as quantity, pc.activity_qty as total_quantity, pc.activity_type"
      + " as type, pcc.charge, p.insurance_category_id FROM patient_package_contents pc LEFT JOIN "
      + " patient_package_content_consumed ppc  ON (ppc.patient_package_content_id ="
      + " pc.patient_package_content_id)  JOIN patient_package_content_charges pcc ON"
      + " (pcc.patient_package_content_id = pc.patient_package_content_id) "
      + " JOIN patient_packages pp ON (pp.pat_package_id = pc.patient_package_id) "
      + " JOIN packages p ON(p.package_id = pp.package_id) "
      + " WHERE pp.pat_package_id= ? AND pc.charge_head = 'INVITE' GROUP BY "
      + " pc.patient_package_content_id,pcc.charge, p.insurance_category_id ";

  /**
   * Gets the multi pkg details.
   *
   * @param patPkgId the patient package id
   * @return the multi pkg details
   */
  public List<BasicDynaBean> getMultiPkgDetails(int patPkgId) {
    return DatabaseHelper.queryToDynaList(GET_MULTI_PACKAGE_INV_DETAILS,
        new Object[] {patPkgId});
  }

}
