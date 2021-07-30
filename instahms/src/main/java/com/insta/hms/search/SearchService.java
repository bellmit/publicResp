package com.insta.hms.search;

import com.bob.hms.common.ScreenRightsHelper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SearchService.
 *
 * @author aditya Service class for search
 */

@Service
public class SearchService {
  @Autowired
  private SearchRepository repository;

  /**
   * Gets the saved searches.
   *
   * @param map the map
   * @return the saved searches
   */
  public PagedList getSavedSearches(Map<String, String[]> map) {
    return repository.getSavedSearches(map);
  }

  /**
   * Gets the search.
   *
   * @param keyColumn the key column
   * @param identifier the identifier
   * @return the search
   */
  public BasicDynaBean getSearch(String keyColumn, String identifier) {
    return repository.findByKey(keyColumn, Integer.parseInt(identifier));
  }

  /**
   * Gets the my search.
   *
   * @param parameters the parameters
   * @return the my search
   * @throws ValidationException the validation exception
   */
  public BasicDynaBean getMySearch(Map<String, String[]> parameters) {
    String mySearch = parameters.get("_mysearch")[0];
    if (mySearch == null || mySearch.equals("")) {
      throw new ValidationException("exception.not.empty", new String[] { "Search ID" });
    } else {
      return getSearch("search_id", mySearch);
    }

  }


  /**
   * This method is called from find.tag
   *
   * @param actionId the action id
   * @return List of BasicDynaBeans
   */
  public static List<BasicDynaBean> getMySearches(String actionId) {
    return SearchRepository.getMySearches(actionId);
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
    List<String> errors = new ArrayList<>();
    BasicDynaBean bean = repository.getBean();

    ConversionUtils.copyToDynaBean(parameters, bean, errors);
    // TODO: Validation
    Integer searchId = null;
    try {
      searchId = Integer.parseInt(parameters.get("search_id")[0]);
    } catch (NumberFormatException exception) {
      errors.add("search_id is null or not integer");
    }

    Map<String, Object> keys = new HashMap<>();
    keys.put("search_id", searchId);
    if (errors.isEmpty()) {
      boolean exists = repository.searchExists((String) bean.get("user_name"),
          (String) bean.get("action_id"), (String) bean.get("search_name"),
          (Integer) bean.get("search_id"));
      if (exists) {
        throw new DuplicateEntityException(
            new String[] { "Search", "name", (String) bean.get("search_name") });
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
    String searchName = parameters.get("_search_name")[0];
    String actionId = parameters.get("_actionId")[0];
    String queryParams = SearchUtil.getSearchCriteria(parameters);
    String screenName = ScreenRightsHelper.getMenuItemName(actionId);

    bean.set("search_id", searchId);
    bean.set("search_name", searchName);
    bean.set("user_name", userId);
    bean.set("action_id", actionId);
    bean.set("query_params", queryParams);
    bean.set("islast", true);
    bean.set("screen_name", screenName == null ? "" : screenName);

    if (repository.searchExists(userId, actionId, searchName, searchId)) {
      throw new DuplicateEntityException(
          new String[] { "Search", "name", (String) bean.get("search_name") });
    }

    int success = repository.insert(bean);

    if (success != 0) {
      return bean;
    }

    return null;
  }

  /**
   * Batch delete searches.
   *
   * @param key the key
   * @param ids the ids
   * @return true, if successful
   */
  public boolean batchDeleteSearches(String key, String[] ids) {
    List<Object> values = new ArrayList<Object>(Arrays.asList(ids));
    int[] updates = repository.batchDelete(key, values, Types.INTEGER);
    boolean success = true;
    for (int update : updates) {
      if (update < 1) {
        success = false;
      }
    }
    return success;
  }

}
