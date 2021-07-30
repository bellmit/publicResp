package com.insta.hms.mdm.diagtestresultcenters;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class DiagTestResultCenterRepository.
 *
 * @author anil.n
 */
@Repository
public class DiagTestResultCenterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new diag test result center repository.
   */
  public DiagTestResultCenterRepository() {
    super("test_results_center", "result_center_id");
  }

  /** The Constant DELETE_RESULTLABEL_CENTER. */
  private static final String DELETE_RESULTLABEL_CENTER = "DELETE FROM test_results_center"
      + " WHERE resultlabel_id = ? AND center_id = ?";

  /**
   * Delete centers.
   *
   * @param resultLabelId
   *          the result label id
   * @param centerId
   *          the center id
   * @return the int
   */
  public int deleteCenters(int resultLabelId, int centerId) {
    return DatabaseHelper.delete(DELETE_RESULTLABEL_CENTER,
        new Object[] { resultLabelId, centerId });
  }
}
