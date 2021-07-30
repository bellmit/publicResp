
package com.bob.hms.adminmasters.organization;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * The Class OrgMasterDao.
 */
public class OrgMasterDao extends GenericDAO {

  /**
   * Instantiates a new org master dao.
   */
  public OrgMasterDao() {
    super("organization_details");
  }

  /** The logger. */

  static Logger logger = LoggerFactory.getLogger(OrgMasterDao.class);

  /** The Constant GET_ALL_ORGANIZATIONS. */
  private static final String GET_ALL_ORGANIZATIONS = "SELECT org_id,org_name,status,"
      + " org_contact_person,org_mailid,"
      + "org_phone,org_address FROM organization_details order by org_name ";

  /**
   * Gets the organiaztions.
   *
   * @return the organiaztions
   * @throws SQLException the SQL exception
   */
  public ArrayList getOrganiaztions() throws SQLException {
    ArrayList orgs = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(GET_ALL_ORGANIZATIONS);) {
      orgs = DataBaseUtil.queryToArrayList(ps);
    }
    return orgs;
  }

  /** The org fields. */
  private static String ORG_FIELDS = " SELECT org_id,org_name,org_contact_person,org_mailid,status,"
      + " org_phone,org_address,case when has_date_validity then valid_from_date"
      + " else null end as valid_from_date,"
      + "case when has_date_validity then valid_to_date else null end as valid_to_date, "
      + "rate_variation, eligible_to_earn_points ";

  /** The org count. */
  private static String ORG_COUNT = "SELECT count(*)";

  /** The org tables. */
  private static String ORG_TABLES = " FROM organization_details";

  /** The init where. */
  private static String initWhere = " WHERE  is_rate_sheet = 'Y' ";

  /**
   * Gets the org detail pages.
   *
   * @param requestParams the request params
   * @param pagingParams  the paging params
   * @return the org detail pages
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public PagedList getOrgDetailPages(Map requestParams, Map<LISTING, Object> pagingParams)
      throws ParseException, SQLException {

    Connection con = null;
    SearchQueryBuilder qb = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      qb = new SearchQueryBuilder(con, ORG_FIELDS, ORG_COUNT, ORG_TABLES, initWhere, pagingParams);
      qb.addFilterFromParamMap(requestParams);
      qb.addSecondarySort("org_id");
      qb.build();

      return qb.getMappedPagedList();
    } finally {
      if (null != qb) {
        qb.close();
      }
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the next org id.
   *
   * @return the next org id
   * @throws SQLException the SQL exception
   */
  public String getNextOrgId() throws SQLException {
    String orgId = null;

    orgId = AutoIncrementId.getNewIncrId("ORG_ID", "ORGANIZATION_DETAILS", "ORG");

    return orgId;
  }

  /** The get dup org. */
  private static String GET_DUP_ORG = "SELECT org_id,org_name,status FROM organization_details"
      + " WHERE org_name = ? ";

  /** The Constant INSERT_ORGANIZATION. */
  private static final String INSERT_ORGANIZATION = "INSERT INTO organization_details(org_id,"
      + " org_name,org_address,org_mailid,org_contact_person,status,org_phone,"
      + "cons_code, revisit_code, private_cons_code, "
      + "private_cons_revisit_code, modd_cons_code, "
      + "modd_cons_revisit_code, spl_cons_code, spl_cons_revisit_code,"
      + "pharmacy_discount_percentage, pharmacy_discount_type, "
      + "has_date_validity, rate_variation, valid_from_date, valid_to_date,store_rate_plan_id,"
      + "eligible_to_earn_points, updated_timestamp)"
      + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

  /**
   * Save new organization.
   *
   * @param con the con
   * @param dto the dto
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public boolean saveNewOrganization(Connection con, Organization dto)
      throws SQLException, ParseException {
    boolean status = false;
    try (PreparedStatement ps = con.prepareStatement(INSERT_ORGANIZATION)) {
      ps.setString(1, dto.getOrgId());
      ps.setString(2, dto.getOrgName());
      ps.setString(3, dto.getAddress());
      ps.setString(4, dto.getEmail());
      ps.setString(5, dto.getContactPerson());
      ps.setString(6, dto.getStatus());
      ps.setString(7, dto.getPhone());

      ps.setString(8, dto.getOpconsvisitcode());
      ps.setString(9, dto.getOpconsrevisitcode());
      ps.setString(10, dto.getPrivateconsvisitcode());
      ps.setString(11, dto.getPrivateconsrevisitcode());
      ps.setString(12, dto.getDutyconsvisitcode());
      ps.setString(13, dto.getDutyconsrevisitcode());
      ps.setString(14, dto.getSplconsvisitcode());
      ps.setString(15, dto.getSplconsrevisitcode());
      ps.setDouble(16, dto.getDiscperc());
      ps.setString(17, dto.getDiscType());
      ps.setBoolean(18, dto.getHasDateValidity());
      ps.setString(19, dto.getRateVariation());
      SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
      java.util.Date parseDate = null;
      if (dto.getFromDate() != null && !dto.getFromDate().equals("")) {
        parseDate = sm.parse(dto.getFromDate());
        ps.setDate(20, new java.sql.Date(parseDate.getTime()));
      } else {
        ps.setDate(20, null);
      }
      if (dto.getToDate() != null && !dto.getToDate().equals("")) {
        parseDate = sm.parse(dto.getToDate());
        ps.setDate(21, new java.sql.Date(parseDate.getTime()));
      } else {
        ps.setDate(21, null);
      }
      if (dto.getStore_rate_plan_id().isEmpty()) {
        ps.setNull(22, Types.INTEGER);
      } else {
        ps.setInt(22, Integer.parseInt(dto.getStore_rate_plan_id()));
      }
      ps.setString(23,
          dto.getEligible_to_earn_points() != null ? dto.getEligible_to_earn_points() : "N");
      ps.setTimestamp(24, DateUtil.getCurrentTimestamp());

      int result = ps.executeUpdate();
      if (result > 0) {
        status = true;
      }
    }
    return status;
  }

  /**
   * Checks if is duplicate org name.
   *
   * @param orgName the org name
   * @return true, if is duplicate org name
   * @throws SQLException the SQL exception
   */
  public boolean isDuplicateOrgName(String orgName) throws SQLException {
    boolean isRowpresent = false;
    try (Connection con = DataBaseUtil.getConnection(true);
        PreparedStatement checkDup = con.prepareStatement(GET_DUP_ORG)) {
      checkDup.setString(1, orgName);
      try (ResultSet rs = checkDup.executeQuery()) {
        if (rs.next()) {
          isRowpresent = true;
        }
      }
    }
    return isRowpresent;
  }

  /** The Constant GET_ORG_DETAILS. */
  private static final String GET_ORG_DETAILS = "SELECT org_name,org_id,org_contact_person,"
      + " org_mailid,org_phone,org_address,status,cons_code, revisit_code, private_cons_code, "
      + " private_cons_revisit_code, modd_cons_code, modd_cons_revisit_code, "
      + " spl_cons_code, spl_cons_revisit_code, "
      + " pharmacy_discount_percentage, pharmacy_discount_type, "
      + " has_date_validity, valid_from_date, rate_variation, "
      + " valid_to_date,store_rate_plan_id,eligible_to_earn_points"
      + "   FROM organization_details WHERE org_id=?";

  /**
   * Gets the orgdetails.
   *
   * @param orgId the org id
   * @return the orgdetails
   * @throws SQLException the SQL exception
   */
  public ArrayList<Hashtable<String, String>> getOrgdetails(String orgId) throws SQLException {
    ArrayList orgs = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ORG_DETAILS);
      ps.setString(1, orgId);
      orgs = DataBaseUtil.queryToArrayList(ps);
    } finally {
      if (null != ps) {
        ps.close();
      }
      if (null != con) {
        con.close();
      }
    }

    return orgs;
  }

  /**
   * Gets the orgdetails dyna bean.
   *
   * @param orgId the org id
   * @return the orgdetails dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getOrgdetailsDynaBean(String orgId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List orgs = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ORG_DETAILS);
      ps.setString(1, orgId);

      orgs = DataBaseUtil.queryToDynaList(ps);

      if (orgs != null && orgs.size() > 0) {
        return (BasicDynaBean) orgs.get(0);
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  /** The Constant UPDATE_ORG_DETATILS. */
  private static final String UPDATE_ORG_DETATILS = "UPDATE organization_details SET org_name=?,"
      + " org_contact_person=?,"
      + " org_mailid=?,org_phone=?,org_address=?,status=?, "
      + "cons_code = ?, revisit_code=?, private_cons_code=?, private_cons_revisit_code=?,"
      + " modd_cons_code=?, "
      + "modd_cons_revisit_code=?, spl_cons_code=?, spl_cons_revisit_code=?,"
      + "pharmacy_discount_percentage=?, pharmacy_discount_type=?, "
      + "has_date_validity=?,rate_variation=?,valid_from_date=?, valid_to_date=?, "
      + "store_rate_plan_id = ?, eligible_to_earn_points = ?, updated_timestamp = ? "
      + "WHERE org_id=?";

  /**
   * Update O rg details.
   *
   * @param con the con
   * @param dto the dto
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public boolean updateORgDetails(Connection con, Organization dto)
      throws SQLException, ParseException {
    boolean status = false;

    try (PreparedStatement checkDup = con.prepareStatement(GET_DUP_ORG)) {
      checkDup.setString(1, dto.getOrgName());
      try (ResultSet rs = checkDup.executeQuery()) {
        if (rs.next() && !(rs.getString("org_id").equals(dto.getOrgId()))) {
          return status;
        }
      }
    }

    try (PreparedStatement ps = con.prepareStatement(UPDATE_ORG_DETATILS)) {

      ps.setString(1, dto.getOrgName());
      ps.setString(2, dto.getContactPerson());
      ps.setString(3, dto.getEmail());
      ps.setString(4, dto.getPhone());
      ps.setString(5, dto.getAddress());
      ps.setString(6, dto.getStatus());

      ps.setString(7, dto.getOpconsvisitcode());
      ps.setString(8, dto.getOpconsrevisitcode());
      ps.setString(9, dto.getPrivateconsvisitcode());
      ps.setString(10, dto.getPrivateconsrevisitcode());
      ps.setString(11, dto.getDutyconsvisitcode());
      ps.setString(12, dto.getDutyconsrevisitcode());
      ps.setString(13, dto.getSplconsvisitcode());
      ps.setString(14, dto.getSplconsrevisitcode());
      ps.setDouble(15, dto.getDiscperc());
      ps.setString(16, dto.getDiscType());
      ps.setBoolean(17, dto.getHasDateValidity());
      ps.setString(18, dto.getRateVariation());

      SimpleDateFormat sm = new SimpleDateFormat("dd-MM-yyyy");
      java.util.Date parseDate = null;
      if (dto.getFromDate() != null && !dto.getFromDate().equals("")) {
        parseDate = sm.parse(dto.getFromDate());
        ps.setDate(19, new java.sql.Date(parseDate.getTime()));
      } else {
        ps.setDate(19, null);
      }

      if (dto.getToDate() != null && !dto.getToDate().equals("")) {
        parseDate = sm.parse(dto.getToDate());
        ps.setDate(20, new java.sql.Date(parseDate.getTime()));
      } else {
        ps.setDate(20, null);
      }
      if (dto.getStore_rate_plan_id().isEmpty()) {
        ps.setNull(21, Types.INTEGER);
      } else {
        ps.setInt(21, Integer.parseInt(dto.getStore_rate_plan_id()));
      }
      ps.setString(22,
          dto.getEligible_to_earn_points() != null ? dto.getEligible_to_earn_points() : "N");

      ps.setTimestamp(23, DateUtil.getCurrentTimestamp());

      ps.setString(24, dto.getOrgId());

      int result = ps.executeUpdate();

      if (result > 0) {
        status = true;
      }
    }
    return status;
  }

  /** The Constant ORG_NAMES. */
  // Do not change this query
  private static final String ORG_NAMES = "SELECT org_id,org_name FROM organization_details ";

  /**
   * Gets the all orgs.
   *
   * @return the all orgs
   * @throws SQLException the SQL exception
   */
  public ArrayList<Hashtable<String, String>> getAllOrgs() throws SQLException {
    ArrayList<Hashtable<String, String>> orgs = null;

    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con.prepareStatement(ORG_NAMES);) {
      orgs = DataBaseUtil.queryToArrayList(ps);
    }
    return orgs;
  }

  /** The Constant RATE_PLAN_NAMES. */
  private static final String RATE_PLAN_NAMES = "SELECT org_id,org_name FROM organization_details"
      + " where is_rate_sheet='N' ";

  /**
   * Gets the rate plan list.
   *
   * @return the rate plan list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getRatePlanList() throws SQLException {
    List<BasicDynaBean> list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(RATE_PLAN_NAMES);
      list = DataBaseUtil.queryToDynaList(ps);
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all org id names.
   *
   * @return the all org id names
   * @throws SQLException the SQL exception
   */
  public static List getAllOrgIdNames() throws SQLException {
    return DataBaseUtil.queryToDynaList(ORG_NAMES + "ORDER BY org_name");
  }

  /**
   * Gets the active org id names.
   *
   * @return the active org id names
   * @throws SQLException the SQL exception
   */
  public static List getActiveOrgIdNames() throws SQLException {
    return ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ORG_NAMES + " WHERE status='A' "));
  }

  /** The Constant GET_ACTIVE_ORGID_NAMES_EXCLUDE_ORG. */
  private static final String GET_ACTIVE_ORGID_NAMES_EXCLUDE_ORG = ORG_NAMES
      + " WHERE status='A' AND is_rate_sheet = 'Y' " + "AND org_id != ? ORDER BY org_name  ";

  /**
   * Gets the active org id names exclude org.
   *
   * @param orgId the org id
   * @return the active org id names exclude org
   * @throws SQLException the SQL exception
   */
  public static List getActiveOrgIdNamesExcludeOrg(String orgId) throws SQLException {
    return ConversionUtils.copyListDynaBeansToMap(
        DataBaseUtil.queryToDynaList(GET_ACTIVE_ORGID_NAMES_EXCLUDE_ORG, orgId));
  }

  /**
   * Gets the organizations.
   *
   * @return the organizations
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOrganizations() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> orgs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(" SELECT org_name,org_id,status from organization_details "
          + " WHERE status='A' AND ( (has_date_validity AND "
          + " current_date BETWEEN valid_from_date AND valid_to_date ) OR (NOT has_date_validity)) "
          + " ORDER BY org_name ");
      orgs = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return orgs;
  }

  /**
   * Gets the organizations.
   *
   * @param centerId the center id
   * @return the organizations
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOrganizations(int centerId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> orgs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(" SELECT * FROM patient_category_master pcm"
          + " WHERE pcm.center_id=? AND pcm.status='A' ");
      ps.setInt(1, centerId);
      orgs = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return orgs;
  }

  /**
   * Gets the organizations.
   *
   * @param visitId the visit id
   * @return the organizations
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOrganizations(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> orgs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(" SELECT org_name,org_id,status FROM organization_details "
          + " WHERE status='A' AND ( (has_date_validity AND "
          + " (SELECT reg_date FROM patient_registration WHERE patient_id=?) "
          + " BETWEEN valid_from_date AND valid_to_date ) OR (NOT has_date_validity)) ");
      ps.setString(1, visitId);
      orgs = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return orgs;
  }

  /**
   * Gets the orgnames.
   *
   * @return the orgnames
   * @throws SQLException the SQL exception
   */
  public static ArrayList getorgnames() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList orgquery = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("SELECT org_name,Org_ID FROM organization_details "
          + " WHERE status='A' AND ( (has_date_validity AND "
          + " current_date BETWEEN valid_from_date AND valid_to_date ) "
          + " OR (NOT has_date_validity))");
      orgquery = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return orgquery;
  }

  /** The Constant GET_PATIENT_ORG_DETAILS. */
  private static final String GET_PATIENT_ORG_DETAILS = "SELECT * FROM patient_registration"
      + " WHERE patient_id=?";

  /**
   * Gets the patient org details.
   *
   * @param con     the con
   * @param visitid the visitid
   * @return the patient org details
   * @throws SQLException the SQL exception
   */
  public static DynaBean getPatientOrgDetails(Connection con, String visitid) throws SQLException {

    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PATIENT_ORG_DETAILS);
      ps.setString(1, visitid);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (!list.isEmpty()) {
        return (DynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return null;
  }

  /** The Constant GET_VAT_CESS. */
  private static final String GET_VAT_CESS = "SELECT vat_applicable ,cess_applicable"
      + "  FROM generic_preferences";

  /**
   * Gets the vat and cess values.
   *
   * @return the vat and cess values
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getVatAndCessValues() throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_VAT_CESS);
      return DataBaseUtil.queryToDynaBean(pstmt);

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant ORG_NAMESAND_IDS. */
  private static final String ORG_NAMESAND_IDS = "SELECT org_id,org_name from organization_details";

  /**
   * Gets the org names and ids map.
   *
   * @return the org names and ids map
   * @throws SQLException the SQL exception
   */
  public static Map getOrgNamesAndIdsMap() throws SQLException {
    List<BasicDynaBean> orgs = DataBaseUtil.queryToDynaList(ORG_NAMESAND_IDS);
    Map orgMap = new HashMap();
    for (BasicDynaBean org : orgs) {
      orgMap.put(org.get("org_name"), org.get("org_id"));
    }
    return orgMap;
  }
}
