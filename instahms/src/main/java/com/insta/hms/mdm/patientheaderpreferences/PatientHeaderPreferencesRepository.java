package com.insta.hms.mdm.patientheaderpreferences;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * This is repository for patient header preferences.
 *
 * @author sainathbatthala
 */
@Repository
public class PatientHeaderPreferencesRepository extends GenericRepository {

  /** The Constant GET_PREFERENCES. */
  private static final String GET_PREFERENCES = "SELECT * FROM patient_header_preferences "
      + "WHERE display = :display AND data_level = :data_level AND visit_type IN (:visitType) AND "
      + "data_category IN (:dataCategory)";

  /**
   * The constructor.
   */
  public PatientHeaderPreferencesRepository() {
    super("patient_header_preferences");
  }

  /**
   * This method gets preferences to display.
   *
   * @return preferences to display
   */
  public List<BasicDynaBean> getPreferencesToDisplay(String dataLevel, String[] visitType,
      String[] dataCategory) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("display", "Y");
    parameters.addValue("data_level", dataLevel);
    parameters.addValue("visitType", visitType == null ? null : Arrays.asList(visitType));
    parameters.addValue("dataCategory", dataCategory == null ? "" : Arrays.asList(dataCategory));

    return DatabaseHelper.queryToDynaList(GET_PREFERENCES, parameters);
  }

}
