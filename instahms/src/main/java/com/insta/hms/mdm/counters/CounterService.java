package com.insta.hms.mdm.counters;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryAssembler;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CounterService.
 *
 * @author yashwant
 */
@Service
public class CounterService extends MasterService {

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Instantiates a new counter service.
   *
   * @param counterRepository
   *          the counter repository
   * @param counterValidator
   *          the counter validator
   */
  public CounterService(CounterRepository counterRepository, CounterValidator counterValidator) {
    super(counterRepository, counterValidator);
  }

  /**
   * Gets the pharmacy active counters.
   *
   * @return the pharmacy active counters
   */
  public List<BasicDynaBean> getPharmacyActiveCounters() {
    return ((CounterRepository) getRepository()).getPharmacyActiveCounters();
  }

  /**
   * Gets the all counters.
   *
   * @return the all counters
   */
  public List<BasicDynaBean> getAllCounters() {
    return ((CounterRepository) getRepository()).getAllCounters();
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params
   *          the params
   * @return the adds the edit page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> referenceMap = new HashMap<String, List<BasicDynaBean>>();
    referenceMap.put("countersLists", getAllCounters());
    List<BasicDynaBean> centers = centerService.listAll(false);
    referenceMap.put("centers", centers);
    return referenceMap;
  }

  /**
   * Gets the list page data.
   *
   * @param params
   *          the params
   * @return the list page data
   */
  @SuppressWarnings("rawtypes")
  public Map<String, List<BasicDynaBean>> getListPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    List<BasicDynaBean> centers = centerService.listAll(false);
    map.put("centers", centers);
    return map;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public PagedList search(Map params, Map<LISTING, Object> listingParams,
      boolean filterByLoggedInCenter) {
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      return super.search(params, listingParams, true);
    } else {
      return super.search(params, listingParams, filterByLoggedInCenter);
    }

  }
  
  public List<BasicDynaBean> getActiveBillingCounters(Integer centerId) {
    return ((CounterRepository) getRepository()).getActiveBillingCounters(centerId);
  }
  
  public List<BasicDynaBean> getAllActiveBillingCounters() {
    return ((CounterRepository) getRepository()).getAllActiveBillingCounters();
  }

  /**
   * Autocomplete.
   *
   * @param match the match
   * @param parameters the parameters
   * @return the list
   */
  @Override
  public List<BasicDynaBean> autocomplete(String match, Map<String, String[]> parameters) {
    return autocomplete("counter_no", match, false, parameters);
  }

  /**
   * Gets the lookup query assembler.
   *
   * @param lookupQuery the lookup query
   * @param parameters the parameters
   * @return the lookup query assembler
   */
  @Override
  public SearchQueryAssembler getLookupQueryAssembler(
      String lookupQuery, Map<String, String[]> parameters) {
    SearchQueryAssembler qb = new SearchQueryAssembler(
            lookupQuery, null, null, ConversionUtils.getListingParameter(parameters));
    Integer centerId = null;
    if (parameters.get("center_id") != null ) {
      centerId = Integer.valueOf(parameters.get("center_id")[0]);
    } else {
      BasicDynaBean genericPrefs = genericPreferencesService.getPreferences();
      if ((Integer) genericPrefs.get("max_centers_inc_default") > 1) {
        centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
      }
    }
    if (parameters.get("filterText") != null ) {
      qb.addFilter(QueryAssembler.STRING, "counter_no", 
            "ILIKE", parameters.get("filterText")[0]);
    }
    if ( centerId != null && centerId != 0 ) {
      qb.addFilter(QueryAssembler.INTEGER, "center_id","=",centerId);
    }
    return qb;
  }

  public List<BasicDynaBean> getAllActiveCounters(Integer centerId) {
    return ((CounterRepository) getRepository()).getAllActiveCounters(centerId);
  }
}
