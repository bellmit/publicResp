package com.insta.hms.mdm.centers;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class CenterRepository.
 *
 * @author yashwant
 * 
 *         CenterRepository is having default class not a public because it will only called by
 *         CenterService class which is having in same package.
 * 
 *         Having all the feature of MasterRepository
 */

@Repository
public class CenterRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new center repository.
   */
  public CenterRepository() {
    super("hospital_center_master", "center_id", "center_name");
  }

  /** The Constant HOSPITAL_CENTER_LOOKUP_QUERY. */
  private static final String HOSPITAL_CENTER_LOOKUP_QUERY = "SELECT * "
      + "FROM (SELECT hcm.center_id, hcm.city_id, hcm.state_id, "
      + "hcm.center_name, ct.city_name, sm.state_name, hcm.status, cm.country_code "
      + "FROM hospital_center_master hcm " + "LEFT JOIN city  ct ON (ct.city_id = hcm.city_id) "
      + "LEFT JOIN state_master sm ON (sm.state_id = hcm.state_id) "
      + "LEFT JOIN country_master cm ON (cm.country_id = hcm.country_id) "
      + "WHERE hcm.status = 'A') as foo";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  /*
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  @Override
  public String getLookupQuery() {
    return HOSPITAL_CENTER_LOOKUP_QUERY;
  }

  /** The Constant CENTER_QUERY. */
  private static final String CENTER_QUERY = " SELECT hc.*,"
      + " c.city_name, s.state_name, ctry.country_name, ctry.country_code  "
      + " FROM hospital_center_master hc " + " LEFT JOIN city c ON (c.city_id = hc.city_id) "
      + " LEFT JOIN state_master s ON (s.state_id = c.state_id ) "
      + " LEFT JOIN country_master ctry ON (ctry.country_id = s.country_id) "
      + " WHERE hc.center_id = ? ";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getViewQuery()
   */
  @Override
  public String getViewQuery() {
    return CENTER_QUERY;
  }

  /** The Constant GET_CENTERS_LIST. */
  private static final String GET_CENTERS_LIST = " SELECT city_name,"
      + " state_name, center_name, s.state_id, c.city_id, center_id, hcm.status"
      + " FROM hospital_center_master hcm LEFT JOIN city c ON (c.city_id=hcm.city_id)"
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id) WHERE hcm.center_id != 0"
      + " ORDER BY center_name";

  /**
   * Gets the centers list.
   *
   * @return the centers list
   */
  public List<BasicDynaBean> getCentersList() {
    return DatabaseHelper.queryToDynaList(GET_CENTERS_LIST);
  }

  private static final String GET_ACTIVE_CENTERS_LIST = " SELECT city_name, "
      + " state_name, center_name, s.state_id, c.city_id, center_id, hcm.status "
      + " FROM hospital_center_master hcm LEFT JOIN city c ON (c.city_id=hcm.city_id) "
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id) "
      + " WHERE hcm.status = 'A' "
      + " ORDER BY center_name ";

  /**
   * Gets the centers list.
   *
   * @return the centers list
   */
  public List<BasicDynaBean> getActiveCentersList() {
    return DatabaseHelper.queryToDynaList(GET_ACTIVE_CENTERS_LIST);
  }

  /** The Constant GET_AVAILABLE_CENTERS. */
  private static final String GET_AVAILABLE_CENTERS = " SELECT"
      + " distinct(hcm.center_id), hcm.center_name " + " FROM hospital_center_master hcm ";

  /**
   * Gets the all centers L list.
   *
   * @return the all centers L list
   */
  public List<BasicDynaBean> getAllCentersLList() {
    return DatabaseHelper.queryToDynaList(GET_AVAILABLE_CENTERS);
  }

  /**
   * Gets the center details list.
   *
   * @param centerNames the center names
   * @return the center details list
   */
  public List<BasicDynaBean> getCenterDetailsList(String[] centerNames) {
    Object[] centerNameWithoutSpace = new Object[centerNames.length];
    String query = GET_AVAILABLE_CENTERS + " WHERE hcm.center_name IN (";
    for (int i = 0; i < centerNames.length; i++) {
      if (i == centerNames.length - 1) {
        query = query + "?)";
      } else {
        query = query + "?,";
      }
      centerNameWithoutSpace[i] = centerNames[i].trim();
    }
    return DatabaseHelper.queryToDynaList(query, centerNameWithoutSpace);
  }

  /** The Constant GET_SAVED_CENTERS. */
  private static final String GET_SAVED_CENTERS = " SELECT hcm.center_name "
      + " FROM hospital_center_master hcm "
      + " JOIN test_results_center trc ON (trc.center_id = hcm.center_id) "
      + " WHERE trc.resultlabel_id = ? ";


  /**
   * Gets the saved centers.
   *
   * @param resultLabelId the result label id
   * @return the saved centers
   */
  public List<BasicDynaBean> getSavedCenters(Integer resultLabelId) {
    return DatabaseHelper.queryToDynaList(GET_SAVED_CENTERS, new Object[] {resultLabelId});
  }

  /**
   * Gets the all centers details.
   *
   * @return the all centers details
   */
  public static List<BasicDynaBean> getAllCentersDetails() {
    List list = null;
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      list =
          DatabaseHelper.queryToDynaList("SELECT"
              + " * FROM hospital_center_master where  center_id = ? and center_id != 0",
              new Object[] {centerId});
    } else {
      list =
          DatabaseHelper.queryToDynaList("SELECT"
              + " * FROM hospital_center_master WHERE status='A' order by center_name");
    }
    return list;
  }

  /** The Constant GET_ALL_CENTERS_EXC_SUPER. */
  private static final String GET_ALL_CENTERS_EXC_SUPER = " SELECT"
      + " * FROM hospital_center_master WHERE center_id=0 AND status='A'"
      + " UNION ALL (SELECT * FROM hospital_center_master WHERE center_id!=0"
      + " AND status='A' ORDER BY center_name)";

  /**
   * Gets the all centers and super center as first.
   *
   * @return the all centers and super center as first
   */
  public List<BasicDynaBean> getAllCentersAndSuperCenterAsFirst() {
    return DatabaseHelper.queryToDynaList(GET_ALL_CENTERS_EXC_SUPER);
  }

  /** The Constant GET_CENTERS_DETAILS. */
  private static final String GET_CENTERS_DETAILS = " SELECT city_name,"
      + " state_name, center_name, s.state_id, c.city_id, center_id, hcm.status"
      + " FROM hospital_center_master hcm" + " LEFT JOIN city c ON (c.city_id=hcm.city_id)"
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id) WHERE hcm.center_id = ? ";

  /**
   * Gets the center details.
   *
   * @param centerId the center id
   * @return the center details
   */
  public BasicDynaBean getCenterDetails(int centerId) {
    return DatabaseHelper.queryToDynaBean(GET_CENTERS_DETAILS, new Object[] {centerId});
  }

  /** The Constant visitCenterQuery. */
  private static final String visitCenterQuery = " SELECT"
      + " hcm.dhpo_facility_user_id, hcm.dhpo_facility_password "
      + " FROM patient_registration  pr "
      + " JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id)"
      + " WHERE pr.patient_id=? ";

  /**
   * Gets the dhpo info center wise.
   *
   * @param visitId the visit id
   * @return the dhpo info center wise
   */
  public BasicDynaBean getDhpoInfoCenterWise(String visitId) {
    return DatabaseHelper.queryToDynaBean(visitCenterQuery, new Object[] {visitId});
  }
  
  /**
   * Gets the all centers except super.
   *
   * @return the all centers except super
   */
  public List<BasicDynaBean> getAllCentersExceptSuper() {
    return DatabaseHelper.queryToDynaList("SELECT * FROM hospital_center_master WHERE status='A' "
        + "and center_id!=0 order by center_name");
  }

  /** The Constant GET_ALL_CENTERS. */
  private static final String GET_ALL_CENTERS = "SELECT"
      + " city_name, state_name, center_name, s.state_id, c.city_id, center_id, hcm.status"
      + " FROM hospital_center_master hcm LEFT JOIN city c ON (c.city_id=hcm.city_id)"
      + " LEFT JOIN state_master s ON (c.state_id=s.state_id)"
      + " WHERE hcm.center_id != 0 ORDER BY center_name";

  /**
   * Gets the all centres.
   *
   * @return the all centres
   */
  public List<BasicDynaBean> getAllCentres() {
    return DatabaseHelper.queryToDynaList(GET_ALL_CENTERS);
  }

  /** The Constant GET_ALL_CENTERS_DATA. */
  private static final String GET_ALL_CENTERS_DATA = "Select center_id, center_name,"
      + "health_authority,accounting_company_name,center_address,hospital_center_service_reg_no,"
      + "center_contact_phone, region_id, city_id, state_id, country_id, center_timezone, "
      + " to_char(created_timestamp AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT"
      + " TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS created_timestamp,"
      + " to_char(updated_timestamp AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT"
      + " TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS updated_timestamp,"
      + "center_code, status from hospital_center_master @ order by center_name";

  /**
   * Gets the all centers data.
   *
   * @param sendOnlyActiveData the send only active data
   * @return the all centers data
   */
  public List<BasicDynaBean> getAllCentersData(boolean sendOnlyActiveData) {
    String query = GET_ALL_CENTERS_DATA;
    if (sendOnlyActiveData) {
      query = query.replace("@", "where status = 'A' ");
    } else {
      query = query.replace("@", "");
    }
    return DatabaseHelper.queryToDynaList(query);
  }

  /** The Constant CENTERS_INC_DEFAULT_FIRST. */
  private static final String CENTERS_INC_DEFAULT_FIRST =
      "SELECT * FROM hospital_center_master WHERE center_id=0 AND status='A' "
          + "   UNION ALL (SELECT * FROM hospital_center_master "
          + " WHERE center_id!=0 AND status='A' ORDER BY center_name)";

  /**
   * Gets the centers inc default first.
   *
   * @return the centers inc default first
   */
  public List<BasicDynaBean> getCentersIncDefaultFirst() {
    return DatabaseHelper.queryToDynaList(CENTERS_INC_DEFAULT_FIRST);
  }
  
  private static final String CENTER_DEFAULTS_QUERY = " SELECT hc.start_of_week, hc.city_id,"
      + " hc.state_id, hc.country_id, "
      + " c.city_name, s.state_name, ctry.country_name, ctry.country_code  "
      + " FROM hospital_center_master hc LEFT JOIN city c ON (c.city_id = hc.city_id) "
      + " LEFT JOIN state_master s ON (s.state_id = c.state_id ) "
      + " LEFT JOIN country_master ctry ON (ctry.country_id = s.country_id) "
      + " WHERE hc.center_id = ? ";
  
  public BasicDynaBean getCenterDefaults(Integer centerId) {
    return DatabaseHelper.queryToDynaBean(CENTER_DEFAULTS_QUERY, new Object[] {centerId});
  }
}
