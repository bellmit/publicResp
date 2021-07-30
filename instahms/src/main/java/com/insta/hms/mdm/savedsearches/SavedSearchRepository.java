package com.insta.hms.mdm.savedsearches;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class SavedSearchRepository.
 *
 * @author krishnat
 */
@Repository
public class SavedSearchRepository extends GenericRepository {

  /** The Constant table. */
  private static final String table = "saved_searches";

  /**
   * Instantiates a new saved search repository.
   */
  public SavedSearchRepository() {
    super(table);
  }

  /**
   * Gets the saved searches.
   *
   * @param flowId the flow id
   * @return the saved searches
   */
  public List<BasicDynaBean> getSavedSearches(String flowId) {
    int roleId = RequestContext.getRoleId();

    // returns saved searches created by logged in user.
    return DatabaseHelper.queryToDynaList(
        "SELECT * FROM saved_searches WHERE flow_id=?  "
            + " and created_by=case when search_type='System' then created_by else ? end "
            + " ORDER BY is_default desc, search_type desc, display_order, created_time desc",
        new Object[] {flowId, RequestContext.getUserName()});
  }

  /**
   * Gets the default search.
   *
   * @param flowId the flow id
   * @return the default search
   */
  public BasicDynaBean getDefaultSearch(String flowId) {
    return DatabaseHelper.queryToDynaBean(
        "SELECT * FROM saved_searches WHERE flow_id=? and is_default=true", new Object[] {flowId});
  }

  /**
   * Search exists.
   *
   * @param flowId the flow id
   * @param searchName the search name
   * @param searchId the search id
   * @return true, if successful
   */
  public boolean searchExists(String flowId, String searchName, Integer searchId) {
    String searchExists =
        "SELECT search_name FROM "
            + table
            + " WHERE flow_id=? AND UPPER(search_name)=UPPER(?) AND search_id !=? AND created_by=?";
    List<BasicDynaBean> resultList =
        DatabaseHelper.queryToDynaList(
            searchExists, flowId, searchName, searchId, RequestContext.getUserName());
    if (resultList.size() != 0) {
      return true;
    }
    return false;
  }

  /**
   * Search exists.
   *
   * @param flowId the flow id
   * @param searchName the search name
   * @return true, if successful
   */
  public boolean searchExists(String flowId, String searchName) {
    String searchExists =
        "SELECT search_name FROM "
            + table
            + " WHERE flow_id=? AND UPPER(search_name)=UPPER(?) AND created_by=?";
    List<BasicDynaBean> resultList =
        DatabaseHelper.queryToDynaList(
            searchExists, flowId, searchName, RequestContext.getUserName());
    if (resultList.size() != 0) {
      return true;
    }
    return false;
  }
}
