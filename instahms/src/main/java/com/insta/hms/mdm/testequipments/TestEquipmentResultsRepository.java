package com.insta.hms.mdm.testequipments;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;


/**
 * The Class TestEquipmentResultsRepository.
 */
@Repository
public class TestEquipmentResultsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new test equipment results repository.
   */
  public TestEquipmentResultsRepository() {
    super("equipment_test_result", "equipment_id");
  }

  /** The Constant EQUIPEMT_RESULTS. */
  private static final String EQUIPEMT_RESULTS = "    SELECT * FROM equipment_test_result ";

  /** The Constant EQUIPEMT_RESULTS_JOIN. */
  private static final String EQUIPEMT_RESULTS_JOIN = EQUIPEMT_RESULTS
      + "  JOIN test_results_master USING (resultlabel_id) ";
  
  
  /**
   * Gets the equipement results.
   *
   * @param equipmentId the equipment id
   * @return the equipement results
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getEquipementResults(String equipmentId) throws SQLException {

    return DatabaseHelper.queryToDynaList(EQUIPEMT_RESULTS_JOIN + " WHERE equipment_id = ? ",
        new Object[] { Integer.parseInt(equipmentId) });

  }

  /**
   * Find by key.
   *
   * @param equipmentId
   *          the equipment id
   * @param resultlabelId
   *          the resultlabel id
   * @return true, if successful
   */
  public boolean findByKey(Integer equipmentId, Integer resultlabelId) {
    List<BasicDynaBean> rlist = DatabaseHelper.queryToDynaList(
        EQUIPEMT_RESULTS + " WHERE equipment_id = ? AND resultlabel_id = ?",
        new Object[] { equipmentId, resultlabelId });

    if (!(rlist.isEmpty())) {
      return true;
    } else {
      return false;
    }
  }
}
