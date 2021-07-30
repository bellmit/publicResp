package com.insta.hms.insurance;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class EstimationBO.
 *
 * @author lakshmi.p
 */
public class EstimationBO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(EstimationBO.class);

  /**
   * Update estimate details.
   *
   * @param est the est
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String updateEstimateDetails(Estimate est) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean success = false;
    String message = null;

    try {
      EstimationDAO estimateDAOObj = new EstimationDAO(con);

      if (!est.getInsertEstimationChargeList().isEmpty()) {
        success = estimateDAOObj.insertCharges(est);
      }

      if (!est.getUpdateEstimationChargeList().isEmpty()) {
        success = estimateDAOObj.updateChargeAmountsList(est.getEstimateID(),
            est.getUpdateEstimationChargeList());
      }
      if (!est.getDeleteEstimationChargeLIst().isEmpty()) {
        success = estimateDAOObj.deleteCharges(est.getDeleteEstimationChargeLIst());
      }
      // Commented as per Insurance Redesign
      // success = estimateDAOObj.updateInsurance(est);
    } catch (SQLException sqlException) {
      success = false;
      throw sqlException;
    } finally {
      if (con != null) {
        if (success) {
          message = "Estimation Details Saved";
          con.commit();
          con.close();
        } else {
          message = "Failed";
          con.rollback();
          con.close();
        }
      }
    }
    return message;
  }

  /**
   * Gets the bill details.
   *
   * @param id       the id
   * @param moduleID the module ID
   * @return the bill details
   * @throws SQLException the SQL exception
   */
  public List getBillDetails(String id, String moduleID) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    List estChargeList = null;
    try {
      EstimationDAO estimateDAO = new EstimationDAO(con);
      String estimateID = getEstimateID(id, moduleID);
      estChargeList = estimateDAO.getChargeList(estimateID);

      return estChargeList;
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the estimate ID.
   *
   * @param id       the id
   * @param moduleID the module ID
   * @return the estimate ID
   * @throws SQLException the SQL exception
   */
  public String getEstimateID(String id, String moduleID) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    try {
      EstimationDAO estimateDAO = new EstimationDAO(con);

      String estimateID = estimateDAO.getEstimateID(id, moduleID);
      return estimateID;
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the charge head const names.
   *
   * @return the charge head const names
   * @throws SQLException the SQL exception
   */
  public List getChargeHeadConstNames() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return (new EstimationDAO(con)).getChargeHeadConstNames();
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the charge group const names.
   *
   * @return the charge group const names
   * @throws SQLException the SQL exception
   */
  public List getChargeGroupConstNames() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return (new EstimationDAO(con)).getChargeGroupConstNames();
    } finally {
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the preferences.
   *
   * @param orgId the org id
   * @return the preferences
   */
  public List getPreferences(String orgId) {
    String query = " SELECT ORG_ID,IP_REG_CHARGE, OP_REG_CHARGE, "
        + " MRCHARGE, MLCCHARGE, GEN_REG_CHARGE "
        + " FROM REGISTRATION_CHARGES WHERE ORG_ID=?";
    List results = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      ps.setString(1, orgId);
      results = DataBaseUtil.queryToArrayList(ps);
    } catch (Exception ex) {
      log.error("", ex);
    }
    return results;
  }

}
