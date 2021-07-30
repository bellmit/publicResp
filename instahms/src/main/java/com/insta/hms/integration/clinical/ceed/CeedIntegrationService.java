package com.insta.hms.integration.clinical.ceed;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Class handles all CEED related tasks.
 *
 * @author teja
 */
@Service
public class CeedIntegrationService {

  /** The ceed details repository. */
  @LazyAutowired
  private CeedDetailsRepository ceedDetailsRepository;

  /** The ceed integration repository. */
  @LazyAutowired
  private CeedIntegrationRepository ceedIntegrationRepository;

  /**
   * Gets the response details.
   *
   * @param consId
   *          the cons id
   * @return the response details
   */
  public List<BasicDynaBean> getResponseDetails(int consId) {
    return ceedDetailsRepository.getResponseDetails(consId);
  }

  /**
   * Check if ceed check done.
   *
   * @param consId
   *          the cons id
   * @return the basic dyna bean
   */
  public BasicDynaBean checkIfCeedCheckDone(int consId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("consultation_id", consId);
    filterMap.put("status", "A");
    return ceedIntegrationRepository.findByKey(filterMap);
  }

}
