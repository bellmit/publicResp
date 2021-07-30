package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OhsrsdohgovphMetaDataRepository extends GenericRepository {

  private static final String COLUMN_OHSRS_FUNCTION = "ohsrs_function";
  private static final String COLUMN_FIELD = "field";
  private static final String COLUMN_VALUE = "value";
  private static final String COLUMN_DESCRIPTION = "description";

  public OhsrsdohgovphMetaDataRepository() {
    super("ohsrsdohgovph_meta_data");
  }

  /**
   * Get meta data for lookup of OHSRS  redefined dataset from DB.
   * @param ohsrsFunction OHSRS function
   * @return Map of predefined data sets
   */
  public Map<String, Map<String, String>> getLookupMetaMap(String ohsrsFunction) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    List<BasicDynaBean> rows = findByCriteria(filters); 
    Map<String, Map<String, String>> map = new HashMap<>();
    for (BasicDynaBean row : rows) {
      String field = (String) row.get(COLUMN_FIELD);
      String value = (String) row.get(COLUMN_VALUE);
      String description = (String) row.get(COLUMN_DESCRIPTION);
      if (!map.containsKey(field)) {
        map.put(field, new HashMap<String,String>());
      }
      Map<String,String> fieldMap = map.get(field);
      fieldMap.put(description.toLowerCase(), value);
    }
    return map;
  }

}
