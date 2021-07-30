package com.insta.hms.testequipmentmasters;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class EquipmentResultsDAO.
 */
public class EquipmentResultsDAO extends GenericDAO {

  /**
   * Instantiates a new equipment results DAO.
   */
  public EquipmentResultsDAO() {
    super("equipment_test_result");
  }

  /** The Constant EQUIPEMT_RESULTS. */
  private static final String EQUIPEMT_RESULTS = " SELECT * FROM equipment_test_result ";

  /** The Constant EQUIPEMT_RESULTS_JOIN. */
  private static final String EQUIPEMT_RESULTS_JOIN = EQUIPEMT_RESULTS
      + "  JOIN test_results_master USING (resultlabel_id)    ";

  /**
   * Gets the equipement results.
   *
   * @param equipmentId
   *          the equipment id
   * @return the equipement results
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getEquipementResults(String equipmentId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(EQUIPEMT_RESULTS_JOIN + " WHERE equipment_id = ? ");
      ps.setInt(1, Integer.parseInt(equipmentId));
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Find by key.
   *
   * @param con
   *          the con
   * @param equipmentId
   *          the equipment id
   * @param resultlabelId
   *          the resultlabel id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean findByKey(Connection con, int equipmentId, Integer resultlabelId)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement(EQUIPEMT_RESULTS + " WHERE equipment_id = ? AND resultlabel_id = ?");
      ps.setInt(1, equipmentId);
      ps.setInt(2, resultlabelId);
      return DataBaseUtil.queryToDynaBean(ps) != null;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

}
