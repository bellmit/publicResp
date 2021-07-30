package com.insta.hms.security.usermanager;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class UserService.
 */
@Service
public class UserService {

  /** The user repository. */
  @LazyAutowired
  private UserRepository userRepository;

  /** The role repository. */
  @LazyAutowired
  private RoleRepository roleRepository;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return userRepository.getBean();
  }

  /**
   * Find by key.
   *
   * @param column
   *          the column
   * @param value
   *          the value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String column, String value) {
    return userRepository.findByKey(column, value);
  }

  /**
   * Update.
   *
   * @param userBean
   *          the user bean
   * @param keys
   *          the keys
   * @return the int
   */
  public int update(BasicDynaBean userBean, Map<String, Object> keys) {
    return userRepository.update(userBean, keys);
  }

  /**
   * Insert.
   *
   * @param userBean
   *          the user bean
   * @return the int
   */
  public int insert(BasicDynaBean userBean) {
    return userRepository.insert(userBean);
  }

  /**
   * Gets the logged user.
   *
   * @return the logged user
   */
  public BasicDynaBean getLoggedUser() {
    return userRepository.findByKey("emp_username", RequestContext.getUserName());
  }

  /**
   * Gets the all usernames.
   *
   * @param activeOnly
   *          the active only
   * @return the all usernames
   */
  public List<String> getAllUsernames(boolean activeOnly) {
    ArrayList<String> columns = new ArrayList<>();
    columns.add("emp_username");
    Map<String, Object> filterMap = new HashMap<String, Object>();
    if (activeOnly) {
      filterMap.put("emp_status", "A");
    }
    List<BasicDynaBean> users = userRepository.listAll(columns, filterMap, "emp_username");
    ArrayList<String> usernames = new ArrayList<>();
    for (BasicDynaBean user : users) {
      usernames.add((String) user.get("emp_username"));
    }
    return usernames;
  }

  /**
   * Gets the user names.
   *
   * @return the user names
   */
  public List getUserNames() {
    return getUserNames(null);
  }

  /**
   * Gets the user names.
   *
   * @param fields
   *          the fields
   * @return the user names
   */
  public List getUserNames(List<String> fields) {
    Map filterMap = new HashMap<String, Object>();
    filterMap.put("hosp_user", "Y");
    Integer centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      filterMap.put("center_id", RequestContext.getCenterId());
    }
    return userRepository.listAll(fields, filterMap, "emp_username");
  }

  /**
   * Gets the active employees.
   *
   * @param fields
   *          the fields
   * @return the active employees
   */
  public List getActiveEmployees(List<String> fields) {
    Map filterMap = new HashMap<String, Object>();
    filterMap.put("hosp_user", "Y");
    filterMap.put("emp_status", "A");
    Integer centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      filterMap.put("center_id", RequestContext.getCenterId());
    }
    return userRepository.listAll(fields, filterMap, "emp_username");
  }

  /**
   * Gets the role.
   *
   * @return the role
   */
  public List getRole() {
    return getRole(null);
  }

  /**
   * Gets the role.
   *
   * @param fields
   *          the fields
   * @return the role
   */
  public List getRole(List<String> fields) {
    return roleRepository.listAll(fields, "role_name");
  }

  /**
   * Gets the user deatil list.
   *
   * @param params
   *          the params
   * @return the user deatil list
   * @throws ParseException
   *           the parse exception
   */
  public Map<String, Object> getUserDeatilList(Map params) throws ParseException {
    PagedList userdetails = userRepository.getUserDeatilsMap(params);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("userdetails", userdetails);
    return map;
  }

  /**
   * Gets the user with default center.
   *
   * @return the user with default center
   */
  public List<BasicDynaBean> getUserWithDefaultCenter(Integer centerId) {
    return userRepository.getUsersOfCenter(centerId);
  }

  /**
   * Gets the user with default center.
   *
   * @return the user with default center
   */
  public List<BasicDynaBean> getUsersWithDefaultCenter(Integer centerId, String portalType) {
    return userRepository.getHospitalUsersOfCenter(centerId, portalType);
  }

  /**
   * Gets the user with default center.
   *
   * @return the user with default center
   */
  public List<BasicDynaBean> getUsersWithDefaultCenter(Integer centerId, String portalType,
      List<String> userIdFilter) {
    return userRepository.getHospitalUsersOfCenter(centerId, portalType, userIdFilter);
  }

  /**
   * List all.
   *
   * @return the list
   */
  public List<BasicDynaBean> listAll() {
    return userRepository.listAll();
  }

  /**
   * Get the User display Name.
   *
   * @return the user display name
   */
  public String getUserDisplayName(String empUserName) {
    BasicDynaBean userBean = userRepository.findByKey("emp_username", empUserName);
    String displayUserName = (userBean.get("temp_username").equals("")
        || userBean.get("temp_username") == null) ? empUserName
            : userBean.get("temp_username").toString();
    return displayUserName;
  }

  /**
   * Get the user's full name.
   *
   * @return the user's full name
   */
  public String getUserFullName(BasicDynaBean user) {
    String firstName = (String) user.get("user_first_name");
    if (firstName == null) {
      firstName = "";
    }
    String middleName = (String) user.get("user_middle_name");
    if (middleName == null) {
      middleName = "";
    } else if (!firstName.isEmpty()) {
      middleName = " " + middleName;
    }
    String lastName = (String) user.get("user_last_name");
    if (lastName == null) {
      lastName = "";
    } else if (!middleName.isEmpty() || !firstName.isEmpty()) {
      lastName = " " + lastName;
    }
    return firstName + middleName + lastName;
  }

  /**
   * Get the user's gender.
   *
   * @return the user's gender
   */
  public String getUserGender(BasicDynaBean user) {
    String gender = (String) user.get("user_gender");
    if (gender == null) {
      return null;
    } else if (gender.equals("M")) {
      return "Male";
    } else if (gender.equals("F")) {
      return "Female";
    } else if (gender.equals("O")) {
      return "Other";
    }
    return null;
  }

  /**
   * Get the user's role Ids.
   *
   * @return the user's role Ids
   */
  public List<Integer> getUserHospitalRoleIds(String username) {
    List<Integer> roleIds = new ArrayList<>();
    for (BasicDynaBean bean:userRepository.getUserHospitalRoleIds(username) ) {
      roleIds.add((Integer) bean.get("hosp_role_id"));
    }
    return roleIds;
  }

  /**
   * Get the user's malaffi role.
   *
   * @return the user's malaffi role
   */
  public String getUserMalaffiRole(String username) {
    return userRepository.getUserMalaffiRole(username);
  }

}
