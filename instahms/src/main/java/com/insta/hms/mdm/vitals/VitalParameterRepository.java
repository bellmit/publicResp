package com.insta.hms.mdm.vitals;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class VitalParameterRepository.
 *
 * @author yashwant
 */
@Repository
public class VitalParameterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new vital parameter repository.
   */
  public VitalParameterRepository() {
    super("vital_parameter_master", "param_id");
  }

  /** The filter param or vital. */
  private static final String FILTER_PARAM_OR_VITAL = " SELECT param_label, param_order "
      + "from vital_parameter_master WHERE  param_label = ? OR param_order = ?";

  /**
   * Filter vital param or order.
   *
   * @param filterValues
   *          the filter values
   * @return the list
   */
  List<BasicDynaBean> filterVitalParamOrOrder(Object[] filterValues) {
    return DatabaseHelper.queryToDynaList(FILTER_PARAM_OR_VITAL, filterValues);
  }
}
