package com.insta.hms.core.userwarddashbord;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.security.usermanager.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserWardDashboardService {
  private static Logger log = LoggerFactory.getLogger(UserWardDashboardService.class);
  @LazyAutowired
  private UserService userService;

  /**
   * Gets the all users.
   *
   * @return the all users
   */
  public List getAllUsers() {
    List<String> fieldName = new ArrayList<String>();
    fieldName.add("emp_username");
    return userService.getUserNames(fieldName);
  }

  /**
   * Gets the all roles.
   *
   * @return the all roles
   */
  public List getAllRoles() {
    List<String> fieldName = new ArrayList<String>();
    fieldName.add("role_name");
    return userService.getRole(fieldName);
  }

  public Map<String, Object> getUserDeatilList(Map<String, String[]> params) throws ParseException {
    return userService.getUserDeatilList(params);
  }

}
