/**
 *
 */

package com.bob.hms.common;

import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.SQLException;

/**
 * The Class CenterHelper.
 *
 * @author mithun.saha
 */
public class CenterHelper {

  private static String visitCenterQuery = " SELECT  hcm.dhpo_facility_user_id,"
      + " hcm.dhpo_facility_password "
      + " FROM patient_registration  pr " + " JOIN hospital_center_master hcm ON "
      + " (hcm.center_id=pr.center_id) WHERE pr.patient_id=? ";

  private static String userCenterQuery = " SELECT  hcm.dhpo_facility_user_id,"
      + " hcm.dhpo_facility_password "
      + " FROM hospital_center_master hcm WHERE hcm.center_id= ? ";

  /**
   * Gets the dhpo info center wise.
   *
   * @param visitId  the visit id
   * @param centerId the center id
   * @return the dhpo info center wise
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDhpoInfoCenterWise(String visitId, Integer centerId)
      throws SQLException {
    if (visitId != null && !visitId.equals("")) {
      return DataBaseUtil.queryToDynaBean(visitCenterQuery, visitId);
    } else {
      return DataBaseUtil.queryToDynaBean(userCenterQuery, centerId);
    }
  }

  /**
   * Authenticate center user.
   *
   * @param userCenterId the user center id
   * @return the string
   * @throws Exception the exception
   */
  public static String authenticateCenterUser(Integer userCenterId) throws Exception {
    String errorMsg = null;
    Integer maxCentersIncDefault = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    Integer healthAuthorityCount = HealthAuthorityPreferencesDAO.getHealthAuthorityCount();
    if (maxCentersIncDefault > 1 && userCenterId == 0 && healthAuthorityCount > 1) {
      errorMsg = "This feature is available only for a specific center";
    }
    return errorMsg;
  }

  /**
   * Center user applicability.
   *
   * @param userCenterId the user center id
   * @return the string
   * @throws Exception the exception
   */
  public static String centerUserApplicability(Integer userCenterId) throws Exception {
    String errorMsg = null;
    Integer maxCentersIncDevelop = (Integer) GenericPreferencesDAO.getAllPrefs()
        .get("max_centers_inc_default");
    if (maxCentersIncDevelop > 1 && userCenterId == 0) {
      errorMsg = "This feature is available only for a specific center";
    }
    return errorMsg;
  }
}
