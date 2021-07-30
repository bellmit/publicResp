package com.insta.hms.mdm.states;

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

/**
 * The Class StateService.
 */
@Service
public class StateService extends MasterService {

  /** The city service. */
  @LazyAutowired
  private CityService cityService;

  /**
   * Instantiates a new state service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public StateService(StateRepository repo, StateValidator validator) {
    super(repo, validator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params the params
   * @return the adds the edit page data
   */
  @SuppressWarnings({ "rawtypes" })
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<>();

    if (((String[]) params.get("state_id")) != null) {
      String stateid = String.valueOf(((String[]) params.get("state_id"))[0]);
      map.put("citiesList", cityService.getCitiesList(stateid));
    }

    return map;
  }

  /**
   * Gets the state list.
   *
   * @param countryId the country id
   * @return the state list
   */
  public List<BasicDynaBean> getStateList(String countryId) {
    return ((StateRepository) getRepository()).getStatesList(countryId);
  }
  
  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("state_name", match, false, parameters);
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
  }
}
