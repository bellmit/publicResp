package com.insta.hms.mdm.healthauthoritypreferences;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.SearchQuery;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/** The Class HealthAuthorityPreferencesRepository. */
@Repository
public class HealthAuthorityPreferencesRepository extends MasterRepository<String> {

  /** The health authority tables. */
  private static String HEALTH_AUTHORITY_TABLES =
      " FROM (SELECT  * FROM health_authority_preferences JOIN health_authority_master "
      + " USING(health_authority)) AS foo";

  /** Instantiates a new health authority preferences repository. */
  public HealthAuthorityPreferencesRepository() {
    super("health_authority_preferences", "health_authority");
  }

  /**
   * This method prepare search query.
   *
   * @return SearchQuery
   */
  @Override
  public SearchQuery getSearchQuery() {
    return new SearchQuery(HEALTH_AUTHORITY_TABLES);
  }

  /** The get preferences by center id. */
  private static String GET_PREFERENCES_BY_CENTER_ID =
      "SELECT hap.* "
          + " FROM hospital_center_master as hcp "
          + " LEFT JOIN health_authority_preferences hap ON "
          + " (hcp.health_authority = hap.health_authority)"
          + " WHERE hcp.center_id = ? ";

  /**
   * Get center details.
   *
   * @param centerId the center id
   * @return the BasicDynaBean
   */
  public BasicDynaBean listBycenterId(Integer centerId) {
    return DatabaseHelper.queryToDynaBean(GET_PREFERENCES_BY_CENTER_ID, new Object[] {centerId});
  }
  
  /** The Constant CENTER_HEALTH_AUTH_BASE_RATE_PLAN. */
  private static final String CENTER_HEALTH_AUTH_BASE_RATE_PLAN = " SELECT  base_rate_plan "
      + " FROM health_authority_preferences hap "
      + " JOIN health_authority_master ham ON (ham.health_authority=hap.health_authority) "
      + " JOIN hospital_center_master hcm ON (hcm.health_authority=hap.health_authority) "
      + " WHERE hcm.center_id=? ";

  /**
   * Gets the center health auth base rate plan.
   *
   * @return the center health auth base rate plan
   */
  public String getCenterHealthAuthBaseRatePlan() {
    return DatabaseHelper.getString(CENTER_HEALTH_AUTH_BASE_RATE_PLAN,
        RequestContext.getCenterId());
  }
  
  private static final String GET_PRESCRIBE_BY_GENERIC_PREFERENCE_CENTER_ID = "SELECT"
      + " prescriptions_by_generics"
      + " FROM health_authority_preferences hap"
      + " LEFT JOIN hospital_center_master hcm ON (hap.health_authority = hcm.health_authority)"
      + " WHERE hcm.center_id = ?";
  
  /**
   * Get Prescribe By Generics Prefernce By CenterId.
   * 
   * @param centerId the center id
   * @return string
   */
  public String getPrescribeByGenericsPrefernceByCenterId(Integer centerId) {
    return DatabaseHelper.getString(GET_PRESCRIBE_BY_GENERIC_PREFERENCE_CENTER_ID,
        new Object[] {centerId});
  }
}
