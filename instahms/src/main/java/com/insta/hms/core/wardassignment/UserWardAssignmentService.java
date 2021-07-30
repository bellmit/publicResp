package com.insta.hms.core.wardassignment;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.mdm.ward.WardService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserWardAssignmentService {
  private static Logger log = LoggerFactory.getLogger(UserWardAssignmentService.class);
  @LazyAutowired
  WardService wardService;
  @Autowired
  GenericPreferencesService genericPreferencesService;
  @LazyAutowired
  UserWardAssignmentRepository userWardAssignmentRepository;

  /**
   * Gets the all ward names.
   *
   * @param centerId the center id
   * @return the all ward names
   */
  public List getAllWardNames(int centerId) {
    BasicDynaBean genprefs = genericPreferencesService.getAllPreferences();
    boolean multicentered = (Integer) genprefs.get("max_centers_inc_default") > 1;
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("center_id", centerId);
    if (multicentered && centerId != 0) {
      return wardService.lookup(true, map);
    } else {
      return wardService.lookup(true);
    }

  }

  public List<BasicDynaBean> getUserWardDetails(String empUserName, int centerId) {
    return userWardAssignmentRepository.getUserWardDetails(empUserName, centerId);
  }

  public boolean updateUserwards(Map params) throws SQLException, IOException {
    return userWardAssignmentRepository.updateUserWardDetails(params);
  }

  public List<BasicDynaBean> nurseWardAssignmentVisitList(String mrNo, String empUserName) {
    return userWardAssignmentRepository.nurseWardAssignmentVisitList(mrNo, empUserName);
  }

}
