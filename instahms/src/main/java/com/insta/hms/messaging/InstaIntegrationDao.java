package com.insta.hms.messaging;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class InstaIntegrationDao.
 */
@Deprecated
public class InstaIntegrationDao extends GenericDAO {

  /** The table name. */
  private static String tableName = "insta_integration";

  /** The Constant GET_CENTER_INTEGRATION_DETAILS. */
  private static final String GET_CENTER_INTEGRATION_DETAILS =
      "SELECT * from center_integration_details cid  "
          + "WHERE cid.integration_id = ? AND cid.center_id = ? ";

  /**
   * Instantiates a new insta integration dao.
   */
  public InstaIntegrationDao() {
    super(tableName);
  }

  /**
   * Gets the active bean.
   *
   * @param integrationName
   *          the integration name
   * @return the active bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getActiveBean(String integrationName) throws SQLException {
    List<BasicDynaBean> paytmDetailsBeanList = findAllByKey("integration_name", integrationName);
    BasicDynaBean activeBean = null;
    for (BasicDynaBean bean : paytmDetailsBeanList) {
      if (bean.get("status") != null && bean.get("status").equals("A")) {
        activeBean = bean;
        break;
      }
    }
    return activeBean;
  }
  
  /**
   * Gets the active bean.
   *
   * @param con the connection
   * @param integrationName the integration name
   * @return the active bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getActiveBean(Connection con, String integrationName) throws SQLException {
    List<BasicDynaBean> paytmDetailsBeanList = 
        findAllByKey(con,"integration_name", integrationName);
    BasicDynaBean activeBean = null;
    for (BasicDynaBean bean : paytmDetailsBeanList) {
      if (bean.get("status") != null && bean.get("status").equals("A")) {
        activeBean = bean;
        break;
      }
    }
    return activeBean;
  }


  /**
   * Gets the center integration details.
   *
   * @param centerId
   *          the center id
   * @param integrationName
   *          the integration name
   * @return the center integration details
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getCenterIntegrationDetails(int centerId, String integrationName)
      throws SQLException {
    BasicDynaBean integrationBean = getActiveBean(integrationName);
    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = DataBaseUtil.getConnection();
      ps = conn.prepareStatement(GET_CENTER_INTEGRATION_DETAILS);
      int index = 1;
      ps.setInt(index++, (Integer) integrationBean.get("integration_id"));
      ps.setInt(index++, centerId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(conn, ps);
    }
  }
  
  /**
   * Gets the center integration details.
   *
   * @param conn the connection
   * @param centerId the center id
   * @param integrationName the integration name
   * @return the center integration details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getCenterIntegrationDetails(Connection conn, 
      int centerId, String integrationName) throws SQLException {
    BasicDynaBean integrationBean = getActiveBean(integrationName);
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(GET_CENTER_INTEGRATION_DETAILS);
      int index = 1;
      ps.setInt(index++, (Integer) integrationBean.get("integration_id"));
      ps.setInt(index++, centerId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant INTEGRATION_DETAILS_FOR_CENTER. */
  public static final String INTEGRATION_DETAILS_FOR_CENTER =
      "select * from center_integration_details where integration_id =? ";

  /** The Constant CENTER_FILTER. */
  public static final String CENTER_FILTER = " and center_id=?";

  /**
   * Gets the integration detail for center.
   *
   * @param integrationId
   *          the integration id
   * @return the integration detail for center
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean getIntegrationDetailForCenter(int integrationId) throws SQLException {
    return DataBaseUtil.queryToDynaBean(INTEGRATION_DETAILS_FOR_CENTER + CENTER_FILTER,
        new Object[] { integrationId, RequestContext.getCenterId() });
  }

  /**
   * Gets the integration detail for all center.
   *
   * @param integrationId
   *          the integration id
   * @return the integration detail for all center
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getIntegrationDetailForAllCenter(int integrationId)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(INTEGRATION_DETAILS_FOR_CENTER,
        new Object[] { integrationId });
  }
}
