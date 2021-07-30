package com.insta.hms.mdm.hospitalroles;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class HospitalRoleRepository.
 *
 * @author anil.n
 */
@Repository
public class HospitalRoleRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new hospital role repository.
   */
  public HospitalRoleRepository() {
    super("hospital_roles_master", "hosp_role_id", "hosp_role_name");
  }
  

  private static final String PAGE_SIZE = "page_size";
  private static final String PAGE_NUMBER = "page_number";

  private static final String BASE_QUERY = "SELECT *, count(hosp_role_id) OVER (PARTITION BY 1)"
      + " FROM hospital_roles_master hrm & "
      + " ORDER BY hosp_role_id";
 

  /** The Constant GET_HOSPITAL_USERS. */
  private static final String GET_HOSPITAL_USERS = " SELECT distinct uu.emp_username as user_name, "
      + " uu.emp_username as user_id"
      + " FROM u_user uu"
      + " JOIN user_hosp_role_master uhm ON (uu.emp_username = uhm.u_user)"
      + " WHERE uu.emp_status = 'A' AND uhm.hosp_role_id = ? ";
  
  /**
   * Gets the hospital users.
   *
   * @param hospRoleId the hosp role id
   * @param centerId the center id
   * @param centersIncDefault the centers inc default
   * @return the hospital users
   */
  public List<BasicDynaBean> getHospitalUsers(Integer hospRoleId, Integer centerId, 
      int centersIncDefault) {
    String query = GET_HOSPITAL_USERS;
    List<Object> queryParams = new ArrayList<Object>();
    queryParams.add(hospRoleId);
    if (centersIncDefault > 1 && centerId != null && centerId != 0) {
      query += " AND uu.center_id =? ORDER BY user_name";
      queryParams.add(centerId);
    } else {
      query += " ORDER BY user_name";
    }
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());
  }

  /**
   * Find hospital roles by filters.
   *
   * @param params filter map
   * @return hospital roles response map
   */
  public Map<String, Object> findByFilters(Map<String, String> params) {

    StringBuilder query = new StringBuilder().append(BASE_QUERY);
    List<Object> queryArguments = new ArrayList<>();
    List<String> filters = new ArrayList<>();

    String roleName = params.get("role_name");
    if (!StringUtils.isEmpty(roleName)) {
      filters.add(" hosp_role_name ILIKE '%" + roleName.trim() + "%' ");
    }

    String malaffiRole = params.get("malaffi_role");
    if (!StringUtils.isEmpty(malaffiRole)) {
      filters.add(" malaffi_role = ? ");
      queryArguments.add(malaffiRole.trim());
    }

    String status = params.get("status");
    if (!StringUtils.isEmpty(status)) {
      filters.add(" status =? ");
      queryArguments.add(status.trim());
    }

    if (!filters.isEmpty()) {
      query = new StringBuilder(query.toString().replaceAll("&",
          "WHERE " + StringUtils.collectionToDelimitedString(filters, " AND")));
    } else {
      query = new StringBuilder(query.toString().replaceAll("&", ""));
    }

    Integer pageSize;
    if (!StringUtils.isEmpty(params.get(PAGE_SIZE))) {
      pageSize = Integer.parseInt(params.get(PAGE_SIZE));
    } else {
      pageSize = 0;
    }
    if (pageSize != 0) {
      query.append(" LIMIT ?");
      queryArguments.add(pageSize);
    }

    Integer pageNum;
    if (!StringUtils.isEmpty(params.get(PAGE_NUMBER))) {
      pageNum = Integer.parseInt(params.get(PAGE_NUMBER));
    } else {
      pageNum = 0;
    }
    if (pageNum != 0) {
      query.append(" OFFSET ?");
      queryArguments.add((pageNum) * pageSize);
    }

    Map<String, Object> resultMap = new HashMap<>();
    List<BasicDynaBean> results = DatabaseHelper.queryToDynaList(query.toString(),
        queryArguments.toArray());
    resultMap.put(PAGE_SIZE, pageSize);
    resultMap.put(PAGE_NUMBER, pageNum);
    resultMap.put("hospital_roles", ConversionUtils.listBeanToListMap(results));
    resultMap.put("total_records", !results.isEmpty() ? results.get(0).get("count") : 0);

    return resultMap;
  }
  
  private static final String GET_HOSPITAL_ROLE_IDS = "SELECT hosp_role_id"
      + " FROM user_hosp_role_master WHERE u_user=?";

  /**
   * Gets the hospital role ids.
   *
   * @param userName the user name
   * @return the hospital role ids
   */
  public List<BasicDynaBean> getHospitalRoleIds(String userName) {
    return DatabaseHelper.queryToDynaList(GET_HOSPITAL_ROLE_IDS, userName);
  }
  
  private static final String GET_SELECTED_HOSPITAL_ROLES = "SELECT hosp_role_id, hosp_role_name "
      + " FROM hospital_roles_master WHERE hosp_role_id IN (:roleIds) AND status = 'A' "
      + " ORDER BY hosp_role_name";
  
  /**
   * Gets the hospital role ids.
   *
   * @param roleIds 
   *           the user name
   * @return the hospital role ids
   */
  public List<BasicDynaBean> getSelectedHospitalRoles(List<Integer> roleIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("roleIds", roleIds);
    return DatabaseHelper.queryToDynaList(GET_SELECTED_HOSPITAL_ROLES, parameters);
  }

  private static final String GET_MALAFFI_ROLE_MAPPED_HOSPITAL_ROLES = "SELECT "
      + " hosp_role_id FROM hospital_roles_master WHERE malaffi_role IS NOT NULL";

  /**
   * Gets the hospital role ids mapped to any malaffi role.
   *
   * @return the hospital role ids
   */
  public List<BasicDynaBean> getMalaffiRoleMappedHospitalRoles() {
    return DatabaseHelper.queryToDynaList(GET_MALAFFI_ROLE_MAPPED_HOSPITAL_ROLES);
  }

}
