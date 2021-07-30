package com.insta.hms.mdm.stockadjustmentreason;
/*
 * Owner : Ashok Pal, 7th Aug 2017
 */

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class StockAdjustmentReasonService.
 */
@Service
public class StockAdjustmentReasonService extends MasterService {

  /**
   * Instantiates a new stock adjustment reason service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public StockAdjustmentReasonService(
      StockAdjustmentReasonRepository repo, StockAdjustmentReasonValidator validator) {
    super(repo, validator);
    // TODO Auto-generated constructor stub
  }

  /**
   * Gets the adds the edit page data.
   *
   * @return the adds the edit page data
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> refData = new HashMap<String, List<BasicDynaBean>>();
    refData.put("StockAdjust", lookup(false));
    return refData;
  }
  
  /**
   * find the bean by key map.
   * @param filterMap the filterMap
   * @return basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return getRepository().findByKey(filterMap);
  }
}
