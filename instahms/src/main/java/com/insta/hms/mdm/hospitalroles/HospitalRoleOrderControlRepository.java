package com.insta.hms.mdm.hospitalroles;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * The Class HospitalRoleRepository.
 *
 * @author anil.n
 */
@Repository
public class HospitalRoleOrderControlRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new hospital role order control repository.
   */
  public HospitalRoleOrderControlRepository() {
    super("hospital_roles_order_controls", "role_id");
  }

  /** GET_SERVICE_SUB_GROUP_ITEMS. */
  private static final String GET_SERVICE_SUB_GROUP_ITEMS =
      " SELECT hroc.item_id, hroc.service_group_id, ssg.service_sub_group_name, "
      + " hroc.service_sub_group_id, oi.item_name, hroc.entity, hroc.hospital_role_control_id"
      + " FROM hospital_roles_order_controls hroc"
      + " JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=hroc.service_sub_group_id)"
      + " JOIN orderable_item oi ON (oi.service_sub_group_id=hroc.service_sub_group_id"
      + " AND oi.service_group_id=hroc.service_group_id AND (oi.entity=hroc.entity"
      + " OR (oi.entity = 'MultiVisitPackage' AND hroc.entity = 'Package')) "
      + " AND oi.entity_id=hroc.item_id) "
      + " WHERE hroc.role_id = ? "
      + " GROUP BY hroc.service_group_id, hroc.item_id, hroc.service_sub_group_id,"
      + " ssg.service_sub_group_name, oi.item_name, hroc.entity, hroc.hospital_role_control_id;";

  /** GET_SERVICE_SUB_GROUPS. */
  private static final String GET_SERVICE_SUB_GROUPS =
      " SELECT hroc.service_group_id, hroc.service_sub_group_id, sg.service_group_name,"
      + " CASE WHEN hroc.service_sub_group_id = -9 "
      + " THEN 'All' ELSE ssg.service_sub_group_name END as service_sub_group_name,"
      + " hroc.hospital_role_control_id"
      + " FROM hospital_roles_order_controls hroc"
      + " JOIN service_groups sg ON (sg.service_group_id=hroc.service_group_id)"
      + " LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id=hroc.service_sub_group_id)"
      + " WHERE hroc.role_id = ? AND hroc.item_id = '*'"
      + " GROUP BY hroc.service_group_id, hroc.service_sub_group_id,"
      + " sg.service_group_name, ssg.service_sub_group_name, hroc.hospital_role_control_id;";

  /**
   * Gets the service sub groups by role id.
   *
   * @param roleId the role id
   * @return the service sub groups by role id
   */
  public List<BasicDynaBean> getServiceSubGroupsByRoleId(Integer roleId) {
    return DatabaseHelper.queryToDynaList(GET_SERVICE_SUB_GROUPS, new Object[] { roleId });
  }

  /**
   * Gets the service sub group items by role id.
   *
   * @param roleId the role id
   * @return the service sub group items by role id
   */
  public List<BasicDynaBean> getServiceSubGroupItemsByRoleId(Integer roleId) {
    return DatabaseHelper.queryToDynaList(GET_SERVICE_SUB_GROUP_ITEMS, new Object[] { roleId });
  }
  

  
  /**
   * Gets the order control rules.
   *
   * @return the order control rules
   */
  private static final String GET_ALL_RULES_FOR_ROLE = " select * from "
      + " hospital_roles_order_controls ";
  
  /**
   * Gets the order control rules.
   *
   * @param userId the user id
   * @return the order control rules
   */
  public List<BasicDynaBean> getOrderControlRules(List userId) {
    String[] placeholdersArr = new String[userId.size()];
    Arrays.fill(placeholdersArr, "?");
    StringBuilder query = new StringBuilder();
    query.append(GET_ALL_RULES_FOR_ROLE);
    query.append("WHERE role_id in (")
      .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
    return DatabaseHelper.queryToDynaList(query.toString(), userId.toArray());
  }
  
}
