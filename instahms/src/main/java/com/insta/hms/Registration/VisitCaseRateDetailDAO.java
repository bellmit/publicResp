package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

public class VisitCaseRateDetailDAO extends GenericDAO {

  public VisitCaseRateDetailDAO() {
    super("visit_case_rate_detail");
  }

  /**
   * @param visitId
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public Boolean deleteVisitCaseRateDetails(String visitId) throws SQLException, IOException {
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      success = delete(con, "visit_id", visitId);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  private static final String INSERT_VISIT_CASE_RATE_DETAILS =
      "INSERT INTO visit_case_rate_detail (visit_id, case_rate_detail_id, "
          + " case_rate_id, insurance_category_id, amount) "
          + " SELECT ?, case_rate_detail_id, case_rate_id, insurance_category_id, amount "
          + " FROM case_rate_detail crd "
          + " WHERE crd.case_rate_id = ? ";

  /**
   * @param visitId
   * @param priCaseRateId
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public Boolean insertVisitCaseRateDetails(String visitId, Integer priCaseRateId)
      throws SQLException, IOException {
    Connection con = null;
    PreparedStatement ps = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      ps = con.prepareStatement(INSERT_VISIT_CASE_RATE_DETAILS);
      ps.setString(1, visitId);
      ps.setInt(2, priCaseRateId);
      success = ps.executeUpdate() >= 0;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  private static final String GET_CASE_RATE_CATEGORY_LIMITS =
      "SELECT vcd.visit_id, vcd.case_rate_detail_id, vcd.case_rate_id, "
          + " vcd.insurance_category_id, vcd.amount, iic.insurance_category_name "
          + " FROM visit_case_rate_detail vcd "
          + " JOIN item_insurance_categories iic ON(vcd.insurance_category_id = iic.insurance_category_id) "
          + " WHERE vcd.visit_id = ?  AND vcd.case_rate_id = ? ORDER BY iic.insurance_category_name ";

  /**
   * @param visitId
   * @param caserateId
   * @return
   * @throws SQLException
   */
  public List<BasicDynaBean> getCaseRateCategoryLimits(String visitId, int caserateId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(
        GET_CASE_RATE_CATEGORY_LIMITS, new Object[] {visitId, caserateId});
  }

  private static final String GET_MASTER_CASE_RATE_CATEGORY_LIMITS =
      "SELECT cd.case_rate_detail_id, cd.case_rate_id, "
          + " cd.insurance_category_id, cd.amount, iic.insurance_category_name "
          + " FROM case_rate_detail cd "
          + " JOIN item_insurance_categories iic ON(cd.insurance_category_id = iic.insurance_category_id) "
          + " WHERE cd.case_rate_id = ? ORDER BY iic.insurance_category_name ";

  /**
   * @param caseRateId
   * @return
   * @throws SQLException
   */
  public List<BasicDynaBean> getCaseRateCategoryLimitsFromMaster(int caseRateId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_MASTER_CASE_RATE_CATEGORY_LIMITS, new Object[] {caseRateId});
  }

  /**
   * @param existingPriCaseRate
   * @param visitId
   * @return
   * @throws SQLException
   */
  public Boolean deleteCaseRatedetails(Integer existingPriCaseRate, String visitId)
      throws SQLException {
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      LinkedHashMap<String, Object> keys = new LinkedHashMap<>();
      keys.put("case_rate_id", existingPriCaseRate);
      keys.put("visit_id", visitId);
      success = delete(con, keys);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  private static final String GET_VISIT_CASE_RATE_DETAILS =
      "SELECT vcd.visit_id, vcd.case_rate_detail_id, "
          + " vcd.case_rate_id::text as case_rate_id, "
          + " vcd.insurance_category_id, vcd.amount, iic.insurance_category_name, "
          + " crm.code, crm.code_description "
          + " FROM visit_case_rate_detail vcd "
          + " JOIN item_insurance_categories iic ON(vcd.insurance_category_id = iic.insurance_category_id) "
          + " JOIN case_rate_main crm ON(crm.case_rate_id = vcd.case_rate_id) "
          + " WHERE vcd.visit_id = ? ";

  /**
   * Method to get visit case rate details.
   * 
   * @param visitId
   * @return
   * @throws SQLException
   */
  public List<BasicDynaBean> getVisitCaseRateDetails(String visitId) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_VISIT_CASE_RATE_DETAILS, new Object[] {visitId});
  }
}
