package com.insta.hms.mdm.areas;

import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.springframework.stereotype.Repository;

@Repository
public class AreaRepository extends MasterRepository<String> {

  public AreaRepository() {
    super("area_master", "area_id", null,
        new String[] { "area_id", "area_name", "city_id", "status" });
  }

  private static final String AREA_SEARCH_TABLES = " FROM ( SELECT area_id, area_name, city_name, "
      + " am.city_id,am.status FROM area_master am JOIN city USING (city_id) ) as foo ";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(AREA_SEARCH_TABLES);
  }

  private static final String AREA_LOOKUP_QUERY = "SELECT * "
      + "FROM (SELECT am.area_id, am.area_name, ct.city_id, ct.city_name, sm.state_id, "
      + "sm.state_name,cm.country_id, cm.country_name,dm.district_id,dm.district_name "
      + "FROM area_master am " + "LEFT JOIN city  ct ON (ct.city_id = am.city_id) "
      + "LEFT JOIN state_master sm ON (sm.state_id = ct.state_id) "
      + "LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
      + "LEFT JOIN district_master dm ON (dm.district_id = ct.district_id) "
      + "where am.status='A') as foo";

  @Override
  public String getLookupQuery() {
    return AREA_LOOKUP_QUERY;
  }
}
