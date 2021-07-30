package com.insta.hms.integration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Repository for Center specific Integration details.
 */

@Repository
public class CenterIntegrationRepository extends GenericRepository {

  /**
   * Instantiates a new center integration repository.
   */
  CenterIntegrationRepository() {
    super("center_integration_details");
  }

  /**
   * Insert the Map into DB.
   *
   * @param beanMap
   *          - The map containing (key,value) pairs to form INSERT query out of it and execute it
   * @return the integer
   */
  public Integer insert(Map<String, Object> beanMap) {
    String query = getInsertQuery(beanMap);
    List<Object> values = new ArrayList<Object>(beanMap.values());
    return DatabaseHelper.insert(query, values.toArray());
  }

  /**
   * Update the center integration details with the values in valuesMap.
   *
   * @param centerBean
   *          - The bean which holds the DB record
   * @param filterMap
   *          - Filters which records to be updated
   * @param valuesMap
   *          - The map contains the updated values of keys in centerBean
   * @return the integer
   */
  public Integer update(BasicDynaBean centerBean, Map<String, Object> filterMap,
      Map<String, Object> valuesMap) {
    for (Entry<String, Object> mapEntry : valuesMap.entrySet()) {
      centerBean.set(mapEntry.getKey(), mapEntry.getValue());

    }
    return update(centerBean, filterMap);

  }

}
