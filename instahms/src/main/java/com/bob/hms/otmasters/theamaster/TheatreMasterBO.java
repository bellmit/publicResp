package com.bob.hms.otmasters.theamaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TheatreMasterBO {

  private static TheatreMasterDAO dao = new TheatreMasterDAO();

  public ArrayList getTheatreMastDetails() {
    return dao.getTheatreMastDetails();
  }

  /**
   * Add new theatre.
   * @param th theatre details
   * @param con Database connection
   * @param userName username of creator
   * @return true or false indicating success of operation
   * @throws Exception generic exception
   */
  public boolean addNewTheatre(Theatre th, Connection con, String userName) throws Exception {
    boolean status = true;
    do {
      status = dao.addOrEditTheatreDef(con, th);
      if (!status) {
        break;
      }
    } while (false);

    return status;
  }

  /**
   * map theatre to all rate plans.
   * @param th theatre details
   * @param userName username of creator
   * @return true or false indicating success of operation
   * @throws Exception generic exception
   */
  public boolean addTheatreToAllOrgs(Theatre th, String userName) throws Exception {
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      GenericDAO theatreOrgDetailsDao = new GenericDAO("theatre_org_details");
      BasicDynaBean theatreOrgBean = theatreOrgDetailsDao.getBean();
      theatreOrgBean.set("theatre_id", th.getTheatreId());
      theatreOrgBean.set("org_id", "ORG0001");
      theatreOrgBean.set("applicable", true);
      theatreOrgBean.set("username", userName);
      theatreOrgBean.set("mod_time", DateUtil.getCurrentTimestamp());

      success = theatreOrgDetailsDao.insert(con, theatreOrgBean);
      dao.copyTheatreDetailsToAllOrgs(con, th.getTheatreId());
      return success;
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
  }


  /**
   * bulk updates charges for theatre.
   * @param orgId rate plan id
   * @param bedTypes List of bed types
   * @param theatreArr List of theatres
   * @param groupUpdate group update
   * @param amount amount
   * @param isPercentage is percentage?
   * @param roundOff Round off amount
   * @param updateTable table to be updated
   * @return true or false indicating success of operation
   * @throws SQLException Query execution exception
   */
  public boolean groupUpdateCharges(String orgId, List<String> bedTypes, List<String> theatreArr,
      String groupUpdate, BigDecimal amount, boolean isPercentage, BigDecimal roundOff,
      String updateTable) throws SQLException {
    boolean status = true;
    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    dao.groupUpdateTheatres(con, orgId, bedTypes, theatreArr, groupUpdate, amount, isPercentage,
        roundOff, updateTable);
    DataBaseUtil.commitClose(con, status);
    return status;
  }

}
