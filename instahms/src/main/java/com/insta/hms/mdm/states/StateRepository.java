package com.insta.hms.mdm.states;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StateRepository extends MasterRepository<String> {

  public StateRepository() {
    super("state_master", "state_id", "state_name",
        new String[] { "state_id", "state_name", "country_id" });
  }

  private static String STATE_SEARCH_TABLES = " FROM (SELECT s.state_id,s.state_name,s.status,"
      + " c.country_id,c.country_name FROM state_master s "
      + " LEFT OUTER JOIN country_master c ON(c.country_id=s.country_id)) AS foo";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(STATE_SEARCH_TABLES);
  }

  public static final String GET_STATES_LIST = "SELECT state_id, state_name, country_id, status "
      + "FROM state_master WHERE country_id=? ORDER BY state_name ASC";

  public List<BasicDynaBean> getStatesList(String countryId) {
    return DatabaseHelper.queryToDynaList(GET_STATES_LIST, new Object[] { countryId });
  }
  
  private static String STATE_LOOKUP_QUERY = "SELECT * " + " FROM (SELECT "
      + " sm.*, cm.country_name " + " FROM state_master sm "
      + " LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
      + " where sm.status='A') as foo";
  
  @Override
  public String getLookupQuery() {
    return STATE_LOOKUP_QUERY;
  }
}
