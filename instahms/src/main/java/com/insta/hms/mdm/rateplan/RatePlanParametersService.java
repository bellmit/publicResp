package com.insta.hms.mdm.rateplan;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/*
 * @author eshwar-chandra
 */
@Service
public class RatePlanParametersService {

  @LazyAutowired
  private RatePlanParametersRepository ratePlanParametersRepository;

  public BasicDynaBean findByKey(Map<String, Object> keys) {
    return ratePlanParametersRepository.findByKey(keys);
  }

  public List<BasicDynaBean> listAll(List<String> columns, Map<String, Object> keys,
      String sortColumn) {
    return ratePlanParametersRepository.listAll(columns, keys, sortColumn);
  }

  /**
   * Gets the derived rate plan ids.
   *
   * @param baseRateSheetId the base rate sheet id
   * @return the derived rate plan ids
   */
  public List<BasicDynaBean> getDerivedRatePlanIds(String baseRateSheetId) {
    return ratePlanParametersRepository.getDerivedRatePlanIds(baseRateSheetId);

  }
}
