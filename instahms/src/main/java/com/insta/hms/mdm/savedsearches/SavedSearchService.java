package com.insta.hms.mdm.savedsearches;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.search.SearchUtil;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class SavedSearchService.
 *
 * @author krishnat
 */
@Service
public class SavedSearchService {

  Logger log =  LoggerFactory.getLogger(MasterService.class);
  
  /** The user service. */
  @LazyAutowired UserService userService;
  
  /** The repository. */
  SavedSearchRepository repository;
  
  /** The validator. */
  SavedSearchValidator validator;
  
  /** The flow instance map. */
  Map<String, SearchParameters> flowInstanceMap;
  
  /** Clinical Preferences. */
  @LazyAutowired private ClinicalPreferencesService clinicalPrefService;

  /**
   * Instantiates a new saved search service.
   *
   * @param savedRepo the r
   * @param savedValidator the v
   */
  public SavedSearchService(SavedSearchRepository savedRepo, SavedSearchValidator savedValidator) {
    repository = savedRepo;
    validator = savedValidator;
    buildFlowInstanceMap();
  }

  /**
   * Builds the flow instance map.
   */
  public void buildFlowInstanceMap() {
    Reflections reflections = new Reflections("com.insta.hms");

    Set<Class<? extends SearchParameters>> subTypes =
        reflections.getSubTypesOf(SearchParameters.class);

    Iterator<Class<? extends SearchParameters>> flowItr = subTypes.iterator();
    flowInstanceMap = new HashMap<>();

    while (flowItr.hasNext()) {
      Class<? extends SearchParameters> flowClassName = flowItr.next();
      Annotation[] annotations = flowClassName.getAnnotations();
      for (Annotation annotation : annotations) {
        if (!(annotation instanceof SavedSearch)) {
          continue;
        }
        SavedSearch agg = (SavedSearch) annotation;
        if (agg.value() != null && !agg.value().equals("")) {
          try {
            flowInstanceMap.put(agg.value(), flowClassName.newInstance());
          } catch (InstantiationException | IllegalAccessException illAccException) {
            log.error(illAccException.getMessage());
          }
        }
      }
    }
  }

  /**
   * Split query.
   *
   * @param searchId the search id
   * @return the map
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public Map<String, String[]> splitQuery(int searchId) throws UnsupportedEncodingException {
    BasicDynaBean bean = repository.findByKey("search_id", searchId);
    return splitQuery(bean);
  }
  
  /**
   * Split query.
   *
   * @param bean the bean
   * @return the map
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public Map<String, String[]> splitQuery(BasicDynaBean bean) throws UnsupportedEncodingException {
    if (bean == null) {
      return null;
    }

    String queryParams = (String) bean.get("query_params");
    queryParams = queryParams == null ? "" : queryParams;

    if ("System".equals((String) bean.get("search_type"))) {
      if (queryParams.contains("#loggedInDoctor#")) {
        String doctor = (String) userService.getLoggedUser().get("doctor_id");
        queryParams = queryParams.replace("#loggedInDoctor#", doctor == null ? "" : doctor);
      }
    }

    Map<String, String[]> queryPairs = new LinkedHashMap<String, String[]>();
    List<String> keys = new ArrayList<String>();
    String[] pairs = queryParams.split("&");
    List<String> values = null;
    int index = 0;
    String prevKey = null;
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      String key = idx > 0 ? pair.substring(0, idx) : pair;
      if (!keys.contains(key)) {
        if (values != null) {
          queryPairs.put(prevKey, values.toArray(new String[values.size()]));
        }
        values = new ArrayList<String>();
        keys.add(key);
        prevKey = new String(key);
      }
      String value = idx > 0 && pair.length() > idx + 1 ? pair.substring(idx + 1) : null;
      values.add(value);

      if (index == pairs.length - 1) {
        queryPairs.put(key, values.toArray(new String[values.size()]));
      }
      index++;
    }
    return queryPairs;
  }

  /**
   * Gets the default search.
   *
   * @param flowId the flow id
   * @return the default search
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public Map<String, String[]> getDefaultSearch(String flowId) throws UnsupportedEncodingException {
    return splitQuery(repository.getDefaultSearch(flowId));
  }

  /**
   * Gets the single instance of SavedSearchService.
   *
   * @param flowId the flow id
   * @return single instance of SavedSearchService
   */
  public SearchParameters getInstance(String flowId) {
    return flowInstanceMap.get(flowId);
  }

  /**
   * Gets the saved searches.
   *
   * @param flowId the flow id
   * @return the saved searches
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public List<Map<String, Object>> getSavedSearches(String flowId)
      throws UnsupportedEncodingException {
    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    List<BasicDynaBean> searches = repository.getSavedSearches(flowId);
    if ("IP Flow".equals(flowId)) {
      searches = searchListBasedOnLoggedUser(searches);
    }
    SearchParameters filter = getInstance(flowId);
    for (BasicDynaBean bean : searches) {
      list.add(getSavedSearch(bean, filter));
    }
    return list;
  }

  /**
   * Gets the saved search.
   *
   * @param searchId the search id
   * @return the saved search
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public Map<String, Object> getSavedSearch(int searchId) throws UnsupportedEncodingException {
    BasicDynaBean bean = repository.findByKey("search_id", searchId);
    if (bean == null) {
      return null;
    }

    SearchParameters filter = getInstance((String) bean.get("flow_id"));
    return getSavedSearch(bean, filter);
  }
  
  /**
   * Gets the saved search.
   *
   * @param bean the b
   * @param filter the filter
   * @return the saved search
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  public Map<String, Object> getSavedSearch(BasicDynaBean bean, SearchParameters filter)
      throws UnsupportedEncodingException {
    if (bean == null) {
      return null;
    }

    Map<String, Object> map = new HashMap<String, Object>(bean.getMap());
    Map<String, String[]> queryParams = splitQuery(bean);

    for (Map.Entry<String, Parameter> entry : filter.getParameters().entrySet()) {
      String[] val = queryParams.get(entry.getKey());

      if (entry.getValue().isMultiple()) {
        map.put(entry.getKey(), val == null ? new String[] {} : val);
      } else {
        map.put(entry.getKey(), (val == null || val[0] == null ? null : val[0]));
      }
    }
    return map;
  }

  /**
   * Find by key.
   *
   * @param searchId the search id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(int searchId) {
    return repository.findByKey("search_id", searchId);
  }

  /**
   * Update search.
   *
   * @param parameters the parameters
   * @return the int
   */
  @Transactional
  public int updateSearch(Map<String, String[]> parameters) {
    int success = 0;
    List<String> errors = new ArrayList<String>();
    BasicDynaBean bean = repository.getBean();

    String queryParams = SearchUtil.getSearchCriteria(parameters);
    ConversionUtils.copyToDynaBean(parameters, bean, errors);
    bean.set("query_params", queryParams);
    bean.set("mod_time", DateUtil.getCurrentTimestamp());
    validator.validateInsert(bean);
    // TODO: Validation
    Integer searchId = null;
    try {
      searchId = Integer.parseInt(parameters.get("search_id")[0]);
    } catch (NumberFormatException exception) {
      errors.add("search_id is null or not integer");
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("search_id", searchId);
    if (errors.isEmpty()) {
      boolean exists =
          repository.searchExists(
              (String) bean.get("flow_id"),
              (String) bean.get("search_name"),
              (Integer) bean.get("search_id"));
      if (exists) {
        throw new DuplicateEntityException(
            new String[] {"Filter", "name", (String) bean.get("search_name")});
      }
      success = repository.update(bean, keys);
    } else {
      throw new ConversionException(errors);
    }

    return success;
  }

  /**
   * Insert search.
   *
   * @param parameters the parameters
   * @param userId the user id
   * @return the basic dyna bean
   */
  public BasicDynaBean insertSearch(Map<String, String[]> parameters, String userId) {

    BasicDynaBean bean = repository.getBean();

    int searchId = repository.getNextSequence();
    String searchName = parameters.get("search_name")[0];
    String flowId = parameters.get("flow_id")[0];
    String queryParams = SearchUtil.getSearchCriteria(parameters);

    bean.set("search_id", searchId);
    bean.set("search_name", searchName);
    bean.set("created_by", userId);
    bean.set("updated_by", userId);
    bean.set("flow_id", flowId);
    bean.set("search_type", "Saved");
    bean.set("query_params", queryParams);

    validator.validateInsert(bean);

    if (repository.searchExists(flowId, searchName)) {
      throw new DuplicateEntityException(
          new String[] {"Filter", "name", (String) bean.get("search_name")});
    }

    int success = repository.insert(bean);

    if (success != 0) {
      return bean;
    }

    return null;
  }

  /**
   * Delete search.
   *
   * @param searchId the search id
   * @return the int
   */
  public int deleteSearch(int searchId) {
    return repository.delete("search_id", searchId);
  }
  
  /**
   * Search list based on logged user.
   * Used this for Ip flow, it gives the lists based on logged in user
   * @param searchList the search list
   * @return the list
   */
  
  public List<BasicDynaBean> searchListBasedOnLoggedUser(List<BasicDynaBean> searchList) {
    HttpServletRequest request = RequestContext.getHttpRequest();
    BasicDynaBean clinicalPrefsBean = clinicalPrefService.getClinicalPreferences();
    List<BasicDynaBean> loggedUserSerachList = new ArrayList<BasicDynaBean>();

    BasicDynaBean userBean = userService.getLoggedUser();
    String loggedUserName = (String) userBean.get("emp_username");
    String doctorId = (String) userBean.get("doctor_id");
    int roleId = (Integer) request.getSession().getAttribute("roleId");
    boolean isDoctorLogin = false;
    boolean applyNurseRules = false;
    if (clinicalPrefsBean.get("ip_cases_across_doctors").equals("N")
        && doctorId != null
        && !doctorId.equals("")) {
      // if the doctor logged into the application, show patients of that doctor in IP list.
      //params.put("doctor_id", new String[]{doctorId+""});
      isDoctorLogin = true;
    }
    if (!isDoctorLogin) {
      applyNurseRules = clinicalPrefsBean.get("nurse_staff_ward_assignments_applicable")
          .equals("Y") && roleId > 2;
    }
    
    for (int i = 0; i < searchList.size(); i++) {
      BasicDynaBean bean = searchList.get(i);
      String searchName = (String) bean.get("search_name");
      String searchType = (String) bean.get("search_type");
      if (searchType.equals("System")) {
        if (applyNurseRules) {
          if (searchName.equals("My Ward")) {
            bean.set("is_default", true);
            loggedUserSerachList.add(bean);
          }

        } else if (isDoctorLogin) {
          if (searchName.equals("My Patients")) {
            bean.set("is_default", true);
            loggedUserSerachList.add(bean);
          }
        } else {
          if (searchName.equals("Active Patients")) {
            bean.set("is_default", true);
            loggedUserSerachList.add(bean);
          }
        }
      } else {
        loggedUserSerachList.add(bean);
      }
    }
    return loggedUserSerachList;
  }
}
