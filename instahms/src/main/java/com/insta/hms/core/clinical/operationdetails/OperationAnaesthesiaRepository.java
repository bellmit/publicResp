package com.insta.hms.core.clinical.operationdetails;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class OperationAnaesthesiaRepository.
 *
 * @author anup vishwas
 */

@Repository
public class OperationAnaesthesiaRepository extends GenericRepository {

  /**
   * Instantiates a new operation anaesthesia repository.
   */
  public OperationAnaesthesiaRepository() {
    super("operation_anaesthesia_details");
  }

  /** The Constant GET_OPERATION_ANAESTHESIA_DETAILS_FOR_FTL. */
  private static final String GET_OPERATION_ANAESTHESIA_DETAILS_FOR_FTL = "SELECT "
      + " atm.anesthesia_type_name,"
      + " atm.status, atm.duration_unit_minutes, atm.min_duration, atm.slab_1_threshold,"
      + " atm.incr_duration, atm.base_unit, anaes_start_datetime as anaesthesia_start,"
      + " anaes_end_datetime as anaesthesia_end"
      + " FROM operation_anaesthesia_details oad "
      + " JOIN anesthesia_type_master atm ON (atm.anesthesia_type_id = oad.anesthesia_type)  "
      + " WHERE oad.operation_details_id = ? ORDER BY anesthesia_type_name ";

  /**
   * Gets the operation anaesthesia details.
   *
   * @param opDetailsId
   *          the op details id
   * @return the operation anaesthesia details
   */
  public List<BasicDynaBean> getOperationAnaesthesiaDetails(Integer opDetailsId) {

    return DatabaseHelper.queryToDynaList(GET_OPERATION_ANAESTHESIA_DETAILS_FOR_FTL,
        new Object[] { opDetailsId });
  }
}
