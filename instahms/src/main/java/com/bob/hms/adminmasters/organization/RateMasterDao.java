package com.bob.hms.adminmasters.organization;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * The Class RateMasterDao.
 */
public class RateMasterDao {

  /** The rate plan fields. */
  private static String RATE_PLAN_FIELDS = "SELECT * ";

  /** The rate plan count. */
  private static String RATE_PLAN_COUNT = "SELECT count(*)";

  /** The rate plan tables. */
  private static String RATE_PLAN_TABLES = "FROM ( " + " SELECT org_name,org_contact_person,"
      + "org_mailid,status, " + " org_phone,org_address,case when has_date_validity then "
      + "valid_from_date " + " else null end as valid_from_date, case when has_date_validity "
      + "then valid_to_date " + " else null end as valid_to_date, rate_variation, "
      + "eligible_to_earn_points ";

  /** The rate sheet join. */
  private static String RATE_SHEET_JOIN = ",od.org_id , rp.base_rate_sheet_id FROM "
      + "organization_details od " + " JOIN rate_plan_parameters rp ON(rp.org_id = od.org_id)" + ""
      + " where is_rate_sheet='N') as foo";

  /** The rate plan filter. */
  private static String RATE_PLAN_FILTER = ",org_id FROM organization_details where "
      + "is_rate_sheet='N') as foo";

  /**
   * Gets the rate plan details.
   *
   * @param requestParams the request params
   * @param pagingParams  the paging params
   * @return the rate plan details
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public PagedList getRatePlanDetails(Map requestParams, Map<LISTING, Object> pagingParams)
      throws ParseException, SQLException {

    Connection con = null;
    SearchQueryBuilder qb = null;
    String query;

    try {
      con = DataBaseUtil.getReadOnlyConnection();

      if (null != requestParams.get("base_rate_sheet_id")
          && ((String[]) requestParams.get("base_rate_sheet_id")).length > 0
          && null != ((String[]) requestParams.get("base_rate_sheet_id"))[0]
          && !((String[]) requestParams.get("base_rate_sheet_id"))[0].equals("")) {
        query = RATE_PLAN_TABLES + RATE_SHEET_JOIN;
      } else {
        query = RATE_PLAN_TABLES + RATE_PLAN_FILTER;
      }

      qb = new SearchQueryBuilder(con, RATE_PLAN_FIELDS, RATE_PLAN_COUNT, query, pagingParams);
      qb.addFilterFromParamMap(requestParams);
      qb.addSecondarySort("org_id");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      if (qb != null) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant RATE_SHEET_LIST. */
  private static final String RATE_SHEET_LIST = "SELECT org_name AS rate_sheet,org_id AS "
      + "rate_sheet_id " + " FROM organization_details WHERE is_rate_sheet='Y' and status='A'"
      + " order by org_name";

  /**
   * Gets the rate sheet list.
   *
   * @return the rate sheet list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public List<BasicDynaBean> getRateSheetList() throws SQLException, ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(RATE_SHEET_LIST);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_RATE_SHEET_DETAILS. */
  private static final String GET_RATE_SHEET_DETAILS = " SELECT rpm.org_id, base_rate_sheet_id,"
      + " " + " org.org_name as base_rate_sheet,rate_variation_percent,round_off_amount,"
      + "priority " + " FROM rate_plan_parameters rpm " + " LEFT JOIN organization_details "
      + "org on (rpm.base_rate_sheet_id = org.org_id) where rpm.org_id=? ";

  /**
   * Gets the rate sheet details.
   *
   * @param orgId the org id
   * @return the rate sheet details
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public List<BasicDynaBean> getRateSheetDetails(String orgId) throws SQLException, ParseException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_RATE_SHEET_DETAILS);
      ps.setString(1, orgId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The charge category fields. */
  private static String CHARGE_CATEGORY_FIELDS = "SELECT * ";

  /** The charge category count. */
  private static String CHARGE_CATEGORY_COUNT = "SELECT count(*)";

  /** The services. */
  private static String SERVICES = "FROM ( " + " SELECT s.service_id,service_name,s"
      + ".service_sub_group_id,ssg.service_sub_group_name, " + " 'services'::text as "
      + "chargeCategory,sod.org_id,sod.applicable,s.status, " + " sod.item_code,s"
      + ".serv_dept_id, sd.department as dept_name, sod.code_type,od.org_name " + " FROM "
      + "services s " + " JOIN service_org_details sod on (sod.service_id=s.service_id) " + ""
      + " JOIN services_departments sd on (sd.serv_dept_id = s.serv_dept_id) " + " JOIN "
      + "organization_details od on (sod.org_id = od.org_id) " + " LEFT JOIN "
      + "service_sub_groups ssg ON(s.service_sub_group_id = ssg.service_sub_group_id)" + " ) "
      + "as foo";

  /** The anesthesia. */
  private static String ANESTHESIA = "FROM ( " + " SELECT atm.anesthesia_type_id,"
      + "anesthesia_type_name,atm.service_sub_group_id,ssg.service_sub_group_name, " + " "
      + "'anesthesia'::text as chargeCategory, atod.org_id, atod.applicable,atm.status,od"
      + ".org_name, atod.is_override " + " FROM anesthesia_type_master atm " + " JOIN "
      + "anesthesia_type_org_details atod ON(atm.anesthesia_type_id= atod.anesthesia_type_id)" + " "
      + " JOIN organization_details od on(od.org_id = atod.org_id) " + " LEFT JOIN "
      + "service_sub_groups ssg ON(atm.service_sub_group_id = ssg.service_sub_group_id)" + " "
      + ")as foo";

  /** The consultation. */
  private static String CONSULTATION = "FROM ( " + " SELECT ct.consultation_type_id,"
      + "consultation_type,ct.service_sub_group_id,ssg.service_sub_group_name, " + " "
      + "'consultation'::text as chargeCategory,cod.org_id, cod.applicable,ct.status,ct"
      + ".consultation_code, " + " ct.patient_type,od.org_name " + " FROM consultation_types "
      + "ct " + " JOIN consultation_org_details cod on (ct.consultation_type_id = cod"
      + ".consultation_type_id) " + " JOIN organization_details od on(od.org_id = cod.org_id)" + " "
      + " LEFT JOIN service_sub_groups ssg ON(ct.service_sub_group_id = ssg"
      + ".service_sub_group_id)" + " ) as foo";

  /** The diagnostics. */
  private static String DIAGNOSTICS = "FROM ( " + " SELECT d.test_id,test_name,d"
      + ".service_sub_group_id, ssg.service_sub_group_name, " + " 'diagnostics'::text as "
      + "chargeCategory,tod.org_id, tod.applicable,d.status, " + " tod.item_code,d.ddept_id, "
      + "dd.ddept_name,od.org_name " + " FROM diagnostics d " + " JOIN test_org_details tod "
      + "on (tod.test_id = d.test_id) " + " JOIN diagnostics_departments dd on (dd.ddept_id=d"
      + ".ddept_id) " + " JOIN organization_details od on(od.org_id = tod.org_id) " + " LEFT "
      + "JOIN service_sub_groups ssg ON(d.service_sub_group_id = ssg.service_sub_group_id)"
      + " ) as foo";

  /** The packages. */
  private static String PACKAGES = "FROM ( " + " SELECT pm.package_id,package_name,pm"
      + ".service_sub_group_id, ssg.service_sub_group_name, " + " 'packages'::text as "
      + "chargeCategory,pod.org_id,pod.applicable, (case when "
      + " pm.visit_applicability='*' AND pm.type='D' then 'd' else pm.visit_applicability end)"
      + "  as package_type, pm.status as status,'P' as type, od.org_name "
      + " FROM packages pm JOIN pack_org_details pod on"
      + "(pm.package_id=pod.package_id) " + " JOIN organization_details od on(od.org_id = pod"
      + ".org_id) " + " LEFT JOIN service_sub_groups ssg ON(pm.service_sub_group_id = ssg"
      + ".service_sub_group_id)" + " ) as foo";

  /** The dynamicpackages. */
  private static String DYNAMICPACKAGES = "FROM ( " + " SELECT dp.dyna_package_id,"
      + "dyna_package_name, " + " 'dynapackages'::text as chargeCategory,dpo.org_id,dpo"
      + ".applicable,dp.status,od.org_name " + " FROM dyna_packages dp " + " JOIN "
      + "dyna_package_org_details dpo on (dpo.dyna_package_id=dp.dyna_package_id) " + " JOIN "
      + "organization_details od on(od.org_id = dpo.org_id) " + " ) as foo";

  /** The operations. */
  private static String OPERATIONS = "FROM ( " + " SELECT op_id,operation_name,om"
      + ".service_sub_group_id, ssg.service_sub_group_name, " + " 'operations'::text as "
      + "chargeCategory,ood.org_id,ood.applicable,om.status, " + " ood.item_code, d"
      + ".dept_name,om.dept_id,ood.code_type,od.org_name " + " FROM operation_master om " + ""
      + " JOIN operation_org_details ood on(ood.operation_id=om.op_id) " + " JOIN department "
      + "d on (d.dept_id = om.dept_id) " + " JOIN organization_details od on(od.org_id = ood"
      + ".org_id) " + " LEFT JOIN service_sub_groups ssg ON(om.service_sub_group_id = ssg"
      + ".service_sub_group_id)" + " ) as foo";

  /**
   * Gets the category charges list.
   *
   * @param orgId          the org id
   * @param chargeCategory the charge category
   * @param requestParams  the request params
   * @param pagingParams   the paging params
   * @return the category charges list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static PagedList getCategoryChargesList(String orgId, String chargeCategory,
      Map requestParams, Map<LISTING, Object> pagingParams) throws SQLException, ParseException {
    Connection con = null;
    SearchQueryBuilder qb = null;
    String chargeCategoryTables = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();

      if (chargeCategory.equalsIgnoreCase("services")) {
        chargeCategoryTables = SERVICES;
      } else if (chargeCategory.equalsIgnoreCase("anesthesia")) {
        chargeCategoryTables = ANESTHESIA;
      } else if (chargeCategory.equalsIgnoreCase("consultation")) {
        chargeCategoryTables = CONSULTATION;
      } else if (chargeCategory.equalsIgnoreCase("diagnostics")) {
        chargeCategoryTables = DIAGNOSTICS;
      } else if (chargeCategory.equalsIgnoreCase("packages")) {
        chargeCategoryTables = PACKAGES;
      } else if (chargeCategory.equalsIgnoreCase("dynapackages")) {
        chargeCategoryTables = DYNAMICPACKAGES;
      } else if (chargeCategory.equalsIgnoreCase("operations")) {
        chargeCategoryTables = OPERATIONS;
      }

      qb = new SearchQueryBuilder(con, CHARGE_CATEGORY_FIELDS, CHARGE_CATEGORY_COUNT,
          chargeCategoryTables, pagingParams);
      qb.addFilterFromParamMap(requestParams);
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the items excluded count.
   *
   * @param tblName the tbl name
   * @param orgId   the org id
   * @return the items excluded count
   * @throws SQLException the SQL exception
   */
  public int getItemsExcludedCount(String tblName, String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(
          "SELECT COUNT(*) FROM " + tblName + " WHERE org_id=? AND " + "applicable='f' ");
      ps.setString(1, orgId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the items overrided count.
   *
   * @param tblName the tbl name
   * @param orgId   the org id
   * @return the items overrided count
   * @throws SQLException the SQL exception
   */
  public int getItemsOverridedCount(String tblName, String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(
          "SELECT COUNT(*) FROM " + tblName + " WHERE org_id=? AND " + "is_override='Y' ");
      ps.setString(1, orgId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the total count.
   *
   * @param tblName the tbl name
   * @param orgId   the org id
   * @return the total count
   * @throws SQLException the SQL exception
   */
  public int getTotalCount(String tblName, String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("SELECT COUNT(*) FROM " + tblName + " WHERE org_id=? ");
      ps.setString(1, orgId);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PACKAGE_COUNT. */
  private static final String GET_PACKAGE_COUNT = "SELECT COUNT(*) FROM packages WHERE type "
      + "='P' ";

  /**
   * Gets the packages count.
   *
   * @return the packages count
   * @throws SQLException the SQL exception
   */
  public int getPackagesCount() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PACKAGE_COUNT);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_BED_TYPE_TOTAL_COUNT. */
  private static final String GET_BED_TYPE_TOTAL_COUNT = "SELECT COUNT(*) FROM bed_types ";

  /** The Constant GET_BED_TYPE_OVERRIDED_COUNT. */
  private static final String GET_BED_TYPE_OVERRIDED_COUNT = "SELECT COUNT(*) FROM bed_details "
      + "WHERE organization=? and is_override='Y'";

  /** The Constant GET_ICU_BED_OVERRIDED_COUNT. */
  private static final String GET_ICU_BED_OVERRIDED_COUNT = "SELECT count(*) FROM "
      + "icu_bed_charges WHERE organization=? " + " AND is_override='Y' AND "
      + "bed_type='GENERAL' ";

  /**
   * Gets the bed type total count.
   *
   * @return the bed type total count
   * @throws SQLException the SQL exception
   */
  public int getbedTypeTotalCount() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BED_TYPE_TOTAL_COUNT);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the bed type overrided count.
   *
   * @param orgId the org id
   * @return the bed type overrided count
   * @throws SQLException the SQL exception
   */
  public int getbedTypeOverridedCount(String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    PreparedStatement ps1 = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_BED_TYPE_OVERRIDED_COUNT);
      ps.setString(1, orgId);
      int overidedCount = DataBaseUtil.getIntValueFromDb(ps);
      ps1 = con.prepareStatement(GET_ICU_BED_OVERRIDED_COUNT);
      ps1.setString(1, orgId);
      overidedCount = overidedCount + DataBaseUtil.getIntValueFromDb(ps1);
      return overidedCount;
    } finally {
      if (ps1 != null) {
        ps1.close();
      }
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_DERIVED_RATE_PLAN_IDS. */
  private static final String GET_DERIVED_RATE_PLAN_IDS = "select org_id from "
      + "rate_plan_parameters " + " where base_rate_sheet_id =?";

  /**
   * Gets the derived rate plan ids.
   *
   * @param baseRateSheetId the base rate sheet id
   * @return the derived rate plan ids
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getDerivedRatePlanIds(String baseRateSheetId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_DERIVED_RATE_PLAN_IDS);
      ps.setString(1, baseRateSheetId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}
