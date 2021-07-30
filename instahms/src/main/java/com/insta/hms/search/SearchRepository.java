package com.insta.hms.search;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The Class SearchRepository.
 *
 * @author aditya
 */

@Repository
public class SearchRepository extends GenericRepository {

  private static final String table = "search_parameters";

  /**
   * Instantiates a new search repository.
   */
  public SearchRepository() {
    super(table);
  }

  private static final String SEARCH_EXISTS = "SELECT search_name FROM " + table
      + " WHERE user_name=? AND action_id=? AND UPPER(search_name)=UPPER(?) AND search_id !=?";

  /**
   * Search exists.
   *
   * @param username the username
   * @param actionId the action id
   * @param searchName the search name
   * @param searchId the search id
   * @return true, if successful
   */
  public boolean searchExists(String username, String actionId, String searchName,
      Integer searchId) {
    List<BasicDynaBean> resultList = DatabaseHelper.queryToDynaList(SEARCH_EXISTS, username,
        actionId, searchName, searchId);
    return !resultList.isEmpty();
  }

  private static String SEARCH_FIELDS = " SELECT search_name, user_name, screen_name, search_id  ";

  private static String SEARCH_COUNT = " SELECT count(*) ";

  private static String SEARCH_TABLES = " FROM search_parameters ";

  /**
   * Gets the saved searches.
   *
   * @param pagingParams the paging params
   * @return the saved searches
   */
  public PagedList getSavedSearches(@SuppressWarnings("rawtypes") Map pagingParams) {

    SearchQueryAssembler queryAssembler = new SearchQueryAssembler(SEARCH_FIELDS, SEARCH_COUNT,
        SEARCH_TABLES, ConversionUtils.getListingParameter(pagingParams));
    queryAssembler.addFilterFromParamMap(pagingParams);
    queryAssembler.addSecondarySort("search_id", false);
    queryAssembler.build();

    return queryAssembler.getMappedPagedList();
  }

  private static final String MYSEARCHES = "SELECT * FROM " + table + " WHERE action_id=?";

  /**
   * Gets the my searches.
   *
   * @param actionId the action id
   * @return the my searches
   */
  public static List<BasicDynaBean> getMySearches(String actionId) {
    return DatabaseHelper.queryToDynaList(MYSEARCHES, actionId);
  }
}
