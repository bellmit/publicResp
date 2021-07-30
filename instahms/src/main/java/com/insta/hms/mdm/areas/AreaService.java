package com.insta.hms.mdm.areas;

import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AreaService extends MasterService {

  public AreaService(AreaRepository repo, AreaValidator validatior) {
    super(repo, validatior);
  }

  /**
   * Add Edit Page related Data.
   *
   * @return Map
   */
  public Map<String, List<BasicDynaBean>> getAddEditPageData() {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("areaNames", lookup(false));
    return map;
  }

  public BasicDynaBean getBean() {
    return ((AreaRepository) this.getRepository()).getBean();
  }

  /**
   * List cities by Area.
   *
   * @param cityId
   *          String
   * @param areaName
   *          String
   * @return Bean
   */
  public BasicDynaBean listAreaByCity(String cityId, String areaName) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("city_id", cityId);
    filterMap.put("area_name", areaName);
    List<BasicDynaBean> dynaList = ((AreaRepository) this.getRepository()).listAll(null, filterMap,
        null);

    if (dynaList != null && !dynaList.isEmpty()) {
      return dynaList.get(0);
    }
    return null;
  }

  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("area_name", match, false, parameters);
  }
  
  @Override
  public void addFilterForLookUp(SearchQueryAssembler qb, String likeValue, String matchField,
      boolean contains, Map<String, String[]> parameters) {
    if (!likeValue.trim().isEmpty()) {
      String filterText = likeValue.trim() + "%";
      if (contains) {
        filterText = "%" + likeValue.trim() + "%";
      }
      qb.addFilter(QueryAssembler.STRING, matchField, "ILIKE", filterText);
    }
    String countryId = (null != parameters && parameters.containsKey("country_id")) ? parameters
        .get("country_id")[0] : null;
    if (countryId != null && !"".equals(countryId.trim())) {
      qb.addFilter(QueryAssembler.STRING, "country_id", "=", countryId);
    }
    String stateId = (null != parameters && parameters.containsKey("state_id")) ? parameters
        .get("state_id")[0] : null;
    if (stateId != null && !"".equals(stateId.trim())) {
      qb.addFilter(QueryAssembler.STRING, "state_id", "=", stateId);
    }
    String districtId = (null != parameters && parameters.containsKey("district_id")) ? parameters
        .get("district_id")[0] : null;
    if (districtId != null && !"".equals(districtId.trim())) {
      qb.addFilter(QueryAssembler.STRING, "district_id", "=", districtId);
    }
    String cityId = (null != parameters && parameters.containsKey("city_id")) ? parameters
        .get("city_id")[0] : null;
    if (cityId != null && !"".equals(cityId.trim())) {
      qb.addFilter(QueryAssembler.STRING, "city_id", "=", cityId);
    }
  }
}
