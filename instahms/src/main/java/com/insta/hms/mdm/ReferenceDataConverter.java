package com.insta.hms.mdm;

import com.insta.hms.common.ConversionUtils;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ReferenceDataConverter.
 */
@Component
public class ReferenceDataConverter implements
    Converter<Map<String, List<BasicDynaBean>>, Map<String, List<Map>>> {

  /**
   * convert.
   *
   * @param lookupMaps
   *          the lookupMaps
   */
  public Map<String, List<Map>> convert(Map<String, List<BasicDynaBean>> lookupMaps) {
    Map<String, List<Map>> lookupListMap = new HashMap<String, List<Map>>();
    if (null != lookupMaps) {
      for (String lookupKey : lookupMaps.keySet()) {
        List mapList = ConversionUtils.listBeanToListMap(lookupMaps.get(lookupKey));
        lookupListMap.put(lookupKey, mapList);
      }
    }
    return lookupListMap;
  }
}
