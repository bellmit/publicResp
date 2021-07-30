package com.insta.hms.mdm.cities;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CityRepository extends MasterRepository<String> {

  public CityRepository() {
    super("city", "city_id", null, new String[] { "city_id", "city_name", "state_id" });
  }

  private static String CITY_SEARCH_TABLES = " FROM (SELECT c.city_id,c.city_name,c.status as "
      + " citystatus , c.state_id, s.state_name, d.district_id, d.district_name FROM city c "
      + " LEFT OUTER JOIN state_master s ON(c.state_id=s.state_id) "
      + " LEFT OUTER JOIN district_master d ON(d.district_id = c.district_id) ) AS foo";

  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(CITY_SEARCH_TABLES);
  }

  public static final String GET_CITY_LIST = "SELECT CITY_ID,CITY_NAME, STATE_ID, STATUS FROM CITY "
      + "WHERE STATE_ID=? ORDER BY CITY_NAME ASC";

  public List<BasicDynaBean> getCitiesList(String stateId) {
    return DatabaseHelper.queryToDynaList(GET_CITY_LIST, new Object[] { stateId });
  }
  
  public static final String GET_CITY_LIST_UNDER_DISTRICT = " SELECT city_id,city_name, state_id, "
      + " district_id, status FROM city WHERE district_id=? ORDER BY city_name ASC ";

  public List<BasicDynaBean> getCitiesUnderDistrict(String districtId) {
    return DatabaseHelper
        .queryToDynaList(GET_CITY_LIST_UNDER_DISTRICT, new Object[] { districtId });
  }

  private static final String CITY_STATE_COUNTRY_LIST = " SELECT c.city_name, c.city_id,"
      + " s.state_name, s.state_id, t.country_name, t.country_id,"
      + " c.city_name ||' - '||s.state_name||' - '||t.country_name AS city_state_country_name "
      + " FROM city c JOIN state_master s ON (s.state_id=c.state_id) "
      + " JOIN country_master t ON (t.country_id=s.country_id) WHERE c.status = 'A'";

  public List<BasicDynaBean> getCityStateCountryList() {
    return DatabaseHelper.queryToDynaList(CITY_STATE_COUNTRY_LIST);
  }

  private static String CITY_LOOKUP_QUERY = "SELECT * " + " FROM (SELECT ct.city_id, ct.city_name,"
      + " sm.state_id, sm.state_name,cm.country_id, cm.country_name, dm.district_id, "
      + " dm.district_name FROM city ct "
      + " LEFT JOIN state_master sm ON (sm.state_id = ct.state_id) "
      + " LEFT JOIN district_master dm ON (dm.district_id = ct.district_id "
      + "       AND dm.state_id = sm.state_id) "
      + " LEFT JOIN country_master cm ON (cm.country_id = sm.country_id) "
      + " where ct.status='A') as foo";

  @Override
  public String getLookupQuery() {
    return CITY_LOOKUP_QUERY;
  }
  
  public static final String GET_CITY_ID = "SELECT * FROM city c "
      + "LEFT JOIN state_master s ON s.state_id=c.state_id "
      + "LEFT JOIN country_master cn ON cn.country_id=s.country_id "
      + "WHERE UPPER(city_name) IN (UPPER(?),'OTHER') "
      + "ORDER BY city_name='OTHER', state_name='OTHER', country_name='OTHER' LIMIT 1 ";
  
  public BasicDynaBean getCityAndCountryId(String city) {
    return DatabaseHelper.queryToDynaBean(GET_CITY_ID, new Object[] { city});
  }
}
