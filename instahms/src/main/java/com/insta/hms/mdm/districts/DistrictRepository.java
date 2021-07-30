package com.insta.hms.mdm.districts;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class DistrictRepository extends MasterRepository<String> {

  public DistrictRepository() {
    super("district_master", "district_id", null, new String[] { "district_id", "district_name",
        "state_id" });
  }

  private static String DISTRICT_SEARCH_TABLES = " FROM (SELECT dm.district_id,dm.district_name,"
      + " dm.status as districtstatus, dm.state_id, s.state_name FROM district_master dm "
      + " LEFT OUTER JOIN state_master s ON(dm.state_id=s.state_id)) AS foo";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(DISTRICT_SEARCH_TABLES);
  }
  
  private static String DISTRICT_LOOKUP_QUERY = "SELECT * " + " FROM (SELECT dm.district_id, "
      + " dm.district_name, sm.state_id, sm.state_name, cm.country_id, cm.country_name  "
      + " FROM district_master dm " + " LEFT JOIN state_master sm ON (sm.state_id = dm.state_id)"
      + " LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
      + " where dm.status='A') as foo";

  @Override
  public String getLookupQuery() {
    return DISTRICT_LOOKUP_QUERY;
  }
  
  private static final String GET_DISTRICT_DETAILS = "SELECT dm.* FROM district_master dm "
      + " JOIN city ct ON (ct.district_id = dm.district_id AND ct.city_id = ?) "
      + " JOIN state_master sm ON (sm.state_id = dm.state_id AND sm.state_id = ?) ";

  public BasicDynaBean getDistrictDetails(String cityId, String stateId) {
    return DatabaseHelper.queryToDynaBean(GET_DISTRICT_DETAILS, new Object[] { cityId, stateId });
  }

}
