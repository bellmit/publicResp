package com.insta.hms.mdm.districts;

import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.cities.CityService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DistrictService extends MasterService {

  @LazyAutowired
  private DistrictRepository districtRepository;
  
  /** The city service. */
  @LazyAutowired
  private CityService cityService;

  public DistrictService(DistrictRepository repository, DistrictValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params
   *          the request params
   * @return Map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("districtList", lookup(false));
    
    if (((String[]) params.get("district_id")) != null) {
      String districtId = String.valueOf(((String[]) params.get("district_id"))[0]);
      map.put("citiesList", cityService.getCitiesUnderDistrict(districtId));
    }
    
    return map;
  }
  
  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("district_name", match, false, parameters);
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
  }
  
  /**
   * Gets the district details.
   *
   * @param cityId
   *          the city id
   * @param stateId
   *          the state id
   * @return the district details
   */
  public BasicDynaBean getDistrictDetails(String cityId, String stateId) {
    return districtRepository.getDistrictDetails(cityId, stateId);

  }

}
