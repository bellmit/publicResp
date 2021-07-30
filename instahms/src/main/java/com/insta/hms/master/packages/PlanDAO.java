package com.insta.hms.master.packages;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.sun.istack.Nullable;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class PlanDAO.
 */
public class PlanDAO extends GenericDAO {
  
  /**
   * Instantiates a new plan DAO.
   */
  public PlanDAO() {
    super("package_plan_master");
  }

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericDAO#getNextSequence()
   */
  @Override
  public int getNextSequence() throws SQLException {
    String query = "SELECT nextval('package_plan_master_seq')";
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(query);
      return DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the plans.
   *
   * @param packageId the package id
   * @return the plans
   * @throws SQLException the SQL exception
   */
  public List getPlans(int packageId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(" SELECT plan_name, ppm.plan_id, ppm.status, package_plan_id "
          + " FROM package_plan_master ppm	"
          + "	LEFT JOIN insurance_plan_main ipm ON (ipm.plan_id=ppm.plan_id)"
          + " WHERE pack_id=?" + " ORDER BY plan_name");
      ps.setInt(1, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ELIGIBLE_PLANS_FOR_SPONSOR. */
  private static final String GET_ELIGIBLE_PLANS_FOR_SPONSOR = "select * from (SELECT"
      + " icm.insurance_co_name as insurance_company_name,"
      + " icm.insurance_co_id as insurance_company_id," + "ipm.plan_id as plan_id,"
      + " ipm.plan_name as plan_name," + "icatm.category_name as insurance_category_name,"
      + " icatm.category_id as category_id"
      + " FROM insurance_plan_main ipm JOIN insurance_company_master icm "
      + " ON (icm.insurance_co_id = ipm.insurance_co_id AND icm.status='A') "
      + " JOIN insurance_category_master icatm "
      + " ON (ipm.category_id=icatm.category_id and icatm.status='A') "
      + " WHERE ipm.sponsor_id IN (:tpaIdArray)"
      + " AND ((ipm.insurance_validity_start_date IS null OR ipm.insurance_validity_end_date  is null) "
      + " OR (now()::date between  ipm.insurance_validity_start_date AND ipm.insurance_validity_end_date))" //
      + " UNION  "
      + " SELECT icm.insurance_co_name as insurance_company_name,"
      + " icm.insurance_co_id as insurance_company_id," + "ipm.plan_id as plan_id,"
      + " ipm.plan_name as plan_name," + "icatm.category_name as insurance_category_name,"
      + " icatm.category_id as category_id"
      + " FROM insurance_plan_main ipm JOIN insurance_company_master icm ON "
      + " (icm.insurance_co_id = ipm.insurance_co_id AND icm.status='A') "
      + " JOIN insurance_category_master icatm ON (ipm.category_id=icatm.category_id and icatm.status='A') "
      + " JOIN insurance_company_tpa_master ictm ON (ictm.insurance_co_id = ipm.insurance_co_id) "
      + " WHERE ictm.tpa_id IN(:tpaIdArray)"
      + " AND ((ipm.insurance_validity_start_date IS null OR ipm.insurance_validity_end_date  is null) "
      + " OR (now()::date between  ipm.insurance_validity_start_date AND ipm.insurance_validity_end_date))"
      + " ) AS foo where foo.plan_name ilike :filterText";

  /** The Constant GET_ALL_PLANS_BY_NAME. */
  private static final String GET_ALL_PLANS_BY_NAME = "select * from (SELECT"
      + " icm.insurance_co_name as insurance_company_name,"
      + " icm.insurance_co_id as insurance_company_id," + "ipm.plan_id as plan_id,"
      + " ipm.plan_name as plan_name," + "icatm.category_name as insurance_category_name,"
      + " icatm.category_id as category_id"
      + " FROM insurance_plan_main ipm JOIN insurance_company_master icm "
      + " ON (icm.insurance_co_id = ipm.insurance_co_id AND icm.status='A') "
      + " JOIN insurance_category_master icatm "
      + " ON (ipm.category_id=icatm.category_id and icatm.status='A') "
      + " WHERE ((ipm.insurance_validity_start_date IS null OR ipm.insurance_validity_end_date  is null) "
      + " OR (now()::date between  ipm.insurance_validity_start_date AND ipm.insurance_validity_end_date))" //
      + " UNION " //
      + " SELECT icm.insurance_co_name as insurance_company_name,"
      + " icm.insurance_co_id as insurance_company_id," + "ipm.plan_id as plan_id,"
      + " ipm.plan_name as plan_name," + "icatm.category_name as insurance_category_name,"
      + " icatm.category_id as category_id"
      + " FROM insurance_plan_main ipm JOIN insurance_company_master icm ON "
      + " (icm.insurance_co_id = ipm.insurance_co_id AND icm.status='A') "
      + " JOIN insurance_category_master icatm ON (ipm.category_id=icatm.category_id and icatm.status='A') "
      + " JOIN insurance_company_tpa_master ictm ON (ictm.insurance_co_id = ipm.insurance_co_id) "
      + " WHERE ((ipm.insurance_validity_start_date IS null OR ipm.insurance_validity_end_date  is null) "
      + " OR (now()::date between  ipm.insurance_validity_start_date AND ipm.insurance_validity_end_date))"
      + " ) AS foo where foo.plan_name ilike :filterText";

  
  /**
   * Gets the eligible plans.
   *
   * @param tpaIdArray the tpa id array
   * @param filterText the filter text
   * @return the eligible plans
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getEligiblePlans(String[] tpaIdArray,@Nullable String filterText)
      throws SQLException {
    MapSqlParameterSource parameterMap = new MapSqlParameterSource();
    if (ArrayUtils.isNotEmpty(tpaIdArray)) {
      parameterMap.addValue("tpaIdArray", Arrays.asList(tpaIdArray));
      parameterMap.addValue("filterText", "%" + StringUtils.trimToEmpty(filterText) + "%");
      return DatabaseHelper.queryToDynaList(GET_ELIGIBLE_PLANS_FOR_SPONSOR, parameterMap);
    } else {
      parameterMap.addValue("filterText", StringUtils.trimToEmpty("%" + filterText) + "%");
      return DatabaseHelper.queryToDynaList(GET_ALL_PLANS_BY_NAME, parameterMap);
    }
  }

  /**
   * Gets the eligible plans.
   *
   * @param packageId the package id
   * @param tpaIdArray the tpa id array
   * @param filterText the filter text
   * @return the eligible plans
   * @throws SQLException the SQL exception
   */
  public List getEligiblePlans(int packageId, String tpaIdArray[], String filterText)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    StringBuilder params = new StringBuilder();
    for (int i = 0; i < tpaIdArray.length; i++) {
      params.append("?,");
    }

    StringBuilder query = new StringBuilder(" SELECT ipm.plan_name, ipm.plan_id"
        + " FROM insurance_plan_main ipm " + " WHERE  ipm.status = 'A' AND "
        + " ((ipm.insurance_validity_start_date IS null OR ipm.insurance_validity_end_date  is null) "
        + " OR (now()::date between  ipm.insurance_validity_start_date AND ipm.insurance_validity_end_date))");
    if (StringUtils.isNotBlank(filterText)) {
      query.append(" AND ipm.plan_name ilike ? ");
    }
    query.append(" AND insurance_co_id IN (SELECT icm.insurance_co_id"
        + " FROM insurance_company_master icm LEFT JOIN insurance_company_tpa_master ictm"
        + " ON ictm.insurance_co_id = icm.insurance_co_id"
        + " WHERE CASE WHEN (select count(tpa_id) from insurance_company_tpa_master ictm "
        + " JOIN insurance_company_master icm ON (ictm.insurance_co_id = icm.insurance_co_id) "
        + " WHERE ");
    query.append(" ictm.tpa_id IN (" + params.deleteCharAt(params.length() - 1).toString()
        + " ) and icm.status = 'A') = 0 " + " then true else ictm.tpa_id IN ("
        + params.toString() + " ) END) ORDER BY plan_name");


    /*
     * try { ps = con.prepareStatement( " SELECT ipm.plan_name, ipm.plan_id" +
     * " FROM insurance_plan_main ipm " +
     * " WHERE insurance_co_id IN (SELECT icm.insurance_co_id" +
     * " FROM insurance_company_master icm LEFT JOIN insurance_company_tpa_master ictm" +
     * " ON ictm.insurance_co_id = icm.insurance_co_id" +
     * " WHERE CASE WHEN (select count(tpa_id) from insurance_company_tpa_master ictm " +
     * " JOIN insurance_company_master icm ON (ictm.insurance_co_id = icm.insurance_co_id) " +
     * " WHERE ictm.tpa_id IN (?) and icm.status = 'A') = 0 " +
     * " then true else ictm.tpa_id IN (?) END) " + " ORDER BY plan_name");
     */
    try {
      ps = con.prepareStatement(query.toString());
      int index = 1;
      if (StringUtils.isNotBlank(filterText)) {
        ps.setString(index++, "%" + filterText + "%");
      }
      for (String tpaId : tpaIdArray) {
        ps.setString(index++, tpaId);
      }
      for (String tpaId : tpaIdArray) {
        ps.setString(index++, tpaId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Delete.
   *
   * @param con the con
   * @param packId the pack id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean delete(Connection con, Integer packId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(
          "DELETE FROM package_plan_master where pack_id=? and plan_id != '-1'");
      ps.setInt(1, packId);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted >= 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Delete.
   *
   * @param con the con
   * @param packId the pack id
   * @param planId the plan id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean delete(Connection con, Integer packId, Integer planId) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("DELETE FROM package_plan_master where pack_id=? and plan_id = ?");
      ps.setInt(1, packId);
      ps.setInt(2, planId);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted >= 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant GET_PLAN_DETAILS_FROM_PLAN_ID. */
  private static final String GET_PLAN_DETAILS_FROM_PLAN_ID =
      "SELECT ppm.pack_id, ppm.plan_id, ipm.plan_name, icm.category_name , "
          + " icom.insurance_co_name FROM package_plan_master ppm "
          + " JOIN insurance_plan_main ipm ON (ipm.plan_id = ppm.plan_id) "
          + " JOIN insurance_category_master icm ON (icm.category_id = ipm.category_id) "
          + " JOIN insurance_company_master icom ON "
          + " (icm.insurance_co_id=icom.insurance_co_id) WHERE ppm.plan_id=?";

  /**
   * Gets the plan details.
   *
   * @param planId the plan id
   * @return the plan details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPlanDetails(Integer planId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(GET_PLAN_DETAILS_FROM_PLAN_ID, planId);
  }

}
