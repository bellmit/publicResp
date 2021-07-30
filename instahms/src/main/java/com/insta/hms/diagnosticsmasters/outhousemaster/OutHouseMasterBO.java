package com.insta.hms.diagnosticsmasters.outhousemaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class OutHouseMasterBO.
 *
 * @author hanumanth
 */
public class OutHouseMasterBO {
  
  /** The dao. */
  OutHouseMasterDAO dao = new OutHouseMasterDAO();

  /**
   * Update out house details.
   *
   * @param ohid the ohid
   * @param ohname the ohname
   * @param outhousename the outhousename
   * @param ohstatus the ohstatus
   * @param templateName the template name
   * @param cliaNo the clia no
   * @param address the address
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateOutHouseDetails(String ohid, String ohname, String outhousename,
      String ohstatus, String templateName, String cliaNo, String address) throws SQLException {
    int res = 0;
    boolean status = false;
    Connection con = null;
    con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    res = dao.updateOutHouseDetails(con, ohid, "0", ohname, outhousename, ohstatus, templateName,
        cliaNo, address);
    if (res > 0) {
      status = true;
    }
    DataBaseUtil.commitClose(con, status);
    return status;
  }

  /**
   * Gets the outsources respect to center.
   *
   * @return the outsources respect to center
   * @throws SQLException the SQL exception
   */
  public static List<Map> getOutsourcesRespectToCenter() throws SQLException {

    return ConversionUtils.copyListDynaBeansToMap(OutHouseMasterDAO.getOutsourcesRespectToCenter());
  }

  /**
   * Save tests and outsource details.
   *
   * @param requestMap the request map
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean saveTestsAndOutsourceDetails(Map requestMap) throws SQLException, IOException {

    String[] diagOutsourceDetailIds = (String[]) requestMap.get("diag_outsource_detail_id");
    String[] outsourceDestIds = (String[]) requestMap.get("outsource_dest_id");
    String[] sourceCenterIds = (String[]) requestMap.get("source_center_id");
    String[] status = (String[]) requestMap.get("status");
    String[] defaultOutsources = (String[]) requestMap.get("default_outsource");
    String testId = ((String[]) requestMap.get("test_id"))[0];
    String[] charges = (String[]) requestMap.get("charge");

    String[] added = (String[]) requestMap.get("added");
    String[] edited = (String[]) requestMap.get("edited");
    String[] delItem = (String[]) requestMap.get("delItem");

    GenericDAO outsourceDetailsDAO = new GenericDAO("diag_outsource_detail");
    BasicDynaBean outsourceDetailsBean = null;
    boolean success = true;
    boolean allSuccess = false;
    Connection con = null;
    LinkedHashMap<String, Object> keys = new LinkedHashMap<String, Object>();

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      for (int i = 0; i < outsourceDestIds.length; i++) {
        if (null != outsourceDestIds[i] && !outsourceDestIds[i].equals("")) {
          outsourceDetailsBean = outsourceDetailsDAO.getBean();
          outsourceDetailsBean.set("source_center_id", Integer.parseInt(sourceCenterIds[i]));
          outsourceDetailsBean.set("outsource_dest_id", Integer.parseInt(outsourceDestIds[i]));
          outsourceDetailsBean.set("status", status[i]);
          outsourceDetailsBean.set("charge",
              (null != charges[i] && !charges[i].equals("")) ? new BigDecimal(charges[i])
                  : new BigDecimal(0));
          outsourceDetailsBean.set("default_outsource", defaultOutsources[i]);
          outsourceDetailsBean.set("test_id", testId);

          if (added[i].equalsIgnoreCase("true") && delItem[i].equalsIgnoreCase("false")) {
            success &= outsourceDetailsDAO.insert(con, outsourceDetailsBean);

          } else if (added[i].equalsIgnoreCase("false") && delItem[i].equalsIgnoreCase("true")) {
            keys.put("diag_outsource_detail_id", Integer.parseInt(diagOutsourceDetailIds[i]));
            success &= outsourceDetailsDAO.delete(con, keys);

          } else {
            keys.put("diag_outsource_detail_id", Integer.parseInt(diagOutsourceDetailIds[i]));
            success &= outsourceDetailsDAO.update(con, outsourceDetailsBean.getMap(), keys) > 0;

          }
          if (!success) {
            break;
          }
        }
      }
      allSuccess = true;
      allSuccess &= success;
    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }

    return allSuccess;
  }

  /** The Constant IS_OUTSOURCE_ASSOCIATED_WITH_TEST. */
  private static final String IS_OUTSOURCE_ASSOCIATED_WITH_TEST = "SELECT dod.outsource_dest_id "
      + " FROM diag_outsource_detail  dod "
      + " JOIN diag_outsource_master  dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " WHERE dom.outsource_dest = ? LIMIT 1";

  /**
   * Checks if is outsource associated with test.
   *
   * @param outsourceDestID the outsource dest ID
   * @return true, if is outsource associated with test
   * @throws SQLException the SQL exception
   */
  public static boolean isOutsourceAssociatedWithTest(String outsourceDestID) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(IS_OUTSOURCE_ASSOCIATED_WITH_TEST);
      pstmt.setString(1, outsourceDestID);

      return DataBaseUtil.queryToDynaBean(pstmt) != null;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

}
