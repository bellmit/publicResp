package com.insta.hms.core.clinical.order.testvisitreports;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class TestVisitReportsService.
 */
@Service
public class TestVisitReportsService {

  /** The test visits reports repo. */
  @LazyAutowired
  private TestVisitReportsRepository testVisitsReportsRepo;

  /**
   * Find by key.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String keyColumn, Object identifier) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(keyColumn, identifier);
    return findByKey(filterMap);
  }

  /**
   * Find by key.
   *
   * @param filterMap the filter map
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(Map<String, Object> filterMap) {
    return testVisitsReportsRepo.findByKey(filterMap);
  }
  
  /**
   * Get sign off details.
   * 
   * @param reportIds the id
   * @return list
   */
  public List<BasicDynaBean> getTestDetails(String[] reportIds) {
    return testVisitsReportsRepo.getTestDetails(reportIds);
  }
}
