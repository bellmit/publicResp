package com.insta.hms.mdm.centerpreferences;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class CenterPreferencesRepository extends MasterRepository<Integer> {

  /** The Constant GET_VIEW_QUERY. */
  private static final String GET_VIEW_QUERY = 
      "select hcm.*, cm.country_name, sm.state_name, c.city_name, "
      + "cm.country_code from hospital_center_master as hcm "
      + "LEFT JOIN country_master as cm ON cm.country_id = hcm.country_id "
      + "LEFT JOIN state_master as sm ON sm.state_id = hcm.state_id "
      + "LEFT JOIN city as c ON c.city_id = hcm.city_id " + "where hcm.center_id = ?";

  public CenterPreferencesRepository() {
    super("center_preferences", "center_id");
  }

  @Override
  public String getViewQuery() {
    return GET_VIEW_QUERY;
  }

  @Override
  public boolean supportsAutoId() {
    return false;
  }

}
