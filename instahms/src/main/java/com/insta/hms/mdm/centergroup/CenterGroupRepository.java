package com.insta.hms.mdm.centergroup;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class CenterGroupRepository.
 */
@Repository
public class CenterGroupRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new center group repository.
   */
  public CenterGroupRepository() {
    super("center_group_master", "center_group_id", "center_group_name");
  }

  /** The Constant GET_CENTERS_FIELDS. */
  public static final String GET_CENTERS_FIELDS = 
      " SELECT c.*, s.*,hcm.*,acm.*,acm.center_group_id as group_center_id, "
      + "cgd.center_id as cg_center_id, cgd.status as cg_status ";
  
  /** The Constant GET_CENTERS_JOINS. */
  public static final String GET_CENTERS_JOINS = 
      " LEFT JOIN center_group_details cgd ON (cgd.center_group_id=acm.center_group_id) "
      + "LEFT JOIN hospital_center_master hcm ON (hcm.center_id=cgd.center_id) "
      + "LEFT JOIN city c ON (c.city_id=hcm.city_id) LEFT JOIN state_master s "
      + "ON (s.state_id=c.state_id) where acm.center_group_id = ?";

  /** The Constant GET_CENTERS_ORDER_BY. */
  public static final String GET_CENTERS_ORDER_BY = " ORDER BY s.state_name, "
      + "c.city_name, hcm.center_name";

  /**
   * Gets the associated centers.
   *
   * @param centerGroupId the center group id
   * @return the associated centers
   */
  public List getAssociatedCenters(Object centerGroupId) {
    String table = getTable();
    StringBuilder query = new StringBuilder(GET_CENTERS_FIELDS);
    query.append(" FROM " + table + " acm ");
    query.append(GET_CENTERS_JOINS);
    query.append(GET_CENTERS_ORDER_BY);
    return DatabaseHelper.queryToDynaList(query.toString(), centerGroupId);
  }

  /** The center details. */
  private static String CENTER_DETAILS = 
      " SELECT city_name, state_name, center_name, s.state_id, c.city_id, center_id, "
      + "hcm.status " + "FROM hospital_center_master hcm  "
      + "LEFT JOIN city c ON (c.city_id=hcm.city_id) "
      + "LEFT JOIN state_master s ON (c.state_id=s.state_id) WHERE hcm.center_id != 0 "
      + "ORDER BY center_name ";

  /**
   * Gets the center list.
   *
   * @return the center list
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getCenterList() {
    return DatabaseHelper.queryToDynaList(CENTER_DETAILS);
  }

  /** The user center group details. */
  private static String USER_CENTER_GROUP_DETAILS = 
      "select report_center_id from u_user where report_center_id is not null";

  /**
   * Gets the center groups.
   *
   * @return the center groups
   */
  public List<BasicDynaBean> getCenterGroups() {
    return DatabaseHelper.queryToDynaList(USER_CENTER_GROUP_DETAILS);
  }
}
