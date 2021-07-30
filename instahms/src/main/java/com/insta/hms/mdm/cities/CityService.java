package com.insta.hms.mdm.cities;

import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.areas.AreaService;
import com.insta.hms.mdm.districts.DistrictService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CityService extends MasterService {

  @LazyAutowired
  private AreaService areaService;
  
  @LazyAutowired
  private CityRepository cityRepository;
  
  /** The district service. */
  @LazyAutowired
  private DistrictService districtService;

  public CityService(CityRepository repo, CityValidator validator) {
    super(repo, validator);
  }

  /**
   * List all centers.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAllCenters() {

    List<BasicDynaBean> citiesList = new ArrayList<BasicDynaBean>();
    citiesList.addAll(getRepository().listAll(null, "status", "A", "city_name"));
    return citiesList;
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the request params
   * @return Map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    map.put("cityList", lookup(false));
    map.put("districtList", districtService.lookup(true));
    Map<String, Object> filterMap = new HashMap<>();
    if (((String[]) params.get("city_id")) != null) {
      filterMap.put("city_id", ((String[]) params.get("city_id"))[0]);
      map.put("areaList", areaService.lookup(false, filterMap));
    } else {
      map.put("areaList", Collections.EMPTY_LIST);
    }
    return map;
  }

  /**
   * Gets the cities list.
   *
   * @param stateid the stateid
   * @return the cities list
   */
  public List<BasicDynaBean> getCitiesList(String stateid) {
    return ((CityRepository) getRepository()).getCitiesList(stateid);
  }
  
  /**
   * Gets the cities list by district.
   *
   * @param districtid
   *          the districtid
   * @return the cities list
   */
  public List<BasicDynaBean> getCitiesUnderDistrict(String districtid) {
    return ((CityRepository) getRepository()).getCitiesUnderDistrict(districtid);
  }

  /**
   * Gets the city state country list.
   *
   * @return the city state country list
   */
  public List<BasicDynaBean> getCityStateCountryList() {
    return ((CityRepository) getRepository()).getCityStateCountryList();
  }

  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    String filterText = (null != parameters && parameters.containsKey("filterText"))
        ? parameters.get("filterText")[0] : null;
    if (filterText == null || filterText.equals("")) {
      filterText = (null != parameters && parameters.containsKey("filter_text"))
          ? parameters.get("filter_text")[0] : filterText;
    }

    String columnNameSearch = (null != parameters && parameters.containsKey("columnName"))
        ? parameters.get("columnName")[0] : null;
    if (columnNameSearch == null || columnNameSearch.equals("")) {
      columnNameSearch = (null != parameters && parameters.containsKey("column_name"))
          ? parameters.get("column_name")[0] : null;
    }
    if (columnNameSearch == null || columnNameSearch.equals("")) {
      columnNameSearch = "city_name";
    }

    return (null != filterText) ? autocomplete(columnNameSearch, filterText, false, parameters)
        : lookup(true);
  }

  
  /**
   * Get city id and country id.
   *  
   * @param city is city name
   * @return return a map array. cityId, countryId and countryName, by default OTHER
   */
  public Map<String,String> getCityAndCountryId(String city) {
    Map<String,String> returnMap = new HashMap<String, String>();
    returnMap.put("cityId", "OTHER");
    returnMap.put("countryId", "OTHER");
    returnMap.put("countryName", "OTHER");
    BasicDynaBean cityResult = cityRepository.getCityAndCountryId(city);
    if (cityResult != null) {
      returnMap.put("cityId", (String) cityResult.get("city_id"));
      returnMap.put("countryId", (String) cityResult.get("country_id"));
      returnMap.put("countryName", (String) cityResult.get("country_name"));
    }
    return returnMap;
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
  }
}
