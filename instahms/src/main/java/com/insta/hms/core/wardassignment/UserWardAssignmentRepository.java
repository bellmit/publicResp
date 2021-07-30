package com.insta.hms.core.wardassignment;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;


import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserWardAssignmentRepository extends GenericRepository {
  public UserWardAssignmentRepository() {
    super("nurse_ward_assignments");
  }

  public static final String GET_USER_WARD_DETAILS = " Select nwa.emp_username,nwa.ward_id,"
      + "nwa.username,nwa.mod_time,uu.role_id,ur.role_name,wn.ward_name,nwa.ward_id as ward_no "
      + " From nurse_ward_assignments nwa "
      + " LEFT JOIN u_user uu ON(nwa.emp_username = uu.emp_username) "
      + " RIGHT JOIN u_role ur ON(ur.role_id=uu.role_id AND hosp_user='Y') "
      + " JOIN ward_names wn ON (nwa.ward_id = wn.ward_no) " + " where nwa.emp_username=? ";

  /**
   * Gets the user ward details.
   *
   * @param empUserName the emp user name
   * @param centerId the center id
   * @return the user ward details
   */
  public List<BasicDynaBean> getUserWardDetails(String empUserName, int centerId) {
    List<BasicDynaBean> userWardDetails = null;
    String query = GET_USER_WARD_DETAILS;
    if (centerId != 0) {
      query = query.concat("AND wn.center_id=?");
      userWardDetails = DatabaseHelper.queryToDynaList(query,
          new Object[] { empUserName, centerId });
    } else {
      userWardDetails = DatabaseHelper.queryToDynaList(query, empUserName);
    }

    return userWardDetails;
  }

  /**
   * Update user ward details.
   *
   * @param params the params
   * @return true, if successful
   */
  public boolean updateUserWardDetails(Map params) {
    String[] wardId = (String[]) params.get("h_ward_id");
    String[] isAdded = (String[]) params.get("h_isadded");
    String[] isdeleted = (String[]) params.get("h_delItem");
    String[] empNames = (String[]) params.get("empusername");
    String empUserName = empNames[0];
    String userName = (String) RequestContext.getSession().getAttribute("userId");
    List<String> columns = new ArrayList<String>();
    LinkedHashMap<String, Object> keys = new LinkedHashMap<String, Object>();
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("emp_username", empUserName);
    List<BasicDynaBean> wardAssignBean = listAll(columns, filterMap, null);
    boolean flag = false;

    for (int i = 0; i < wardId.length; i++) {
      Boolean check = false;
      if (isAdded[i].equalsIgnoreCase("true")) {
        for (int k = 0; k < wardAssignBean.size(); k++) {
          BasicDynaBean wardBean = (BasicDynaBean) wardAssignBean.get(k);
          String oldWardId = (String) wardBean.get("ward_id");
          if (oldWardId.equals(wardId[i])) {
            String newWardId = wardId[i];
            updateWardAssignment(empUserName, oldWardId, newWardId, userName);
            check = true;
            flag = true;
            break;
          }
        }
        if (!check) {
          BasicDynaBean userWardBean = getBean();
          userWardBean.set("emp_username", empUserName);
          userWardBean.set("ward_id", wardId[i]);
          userWardBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          userWardBean.set("username", userName);
          flag = insert(userWardBean) > 0;
          if (!flag) {
            break;
          }
        }
      } else {
        if (isdeleted[i].equalsIgnoreCase("true")) {
          keys.put("emp_username", empUserName);
          keys.put("ward_id", wardId[i]);
          flag = delete(keys) > 0;
          if (!flag) {
            break;
          }

        }
      }
    }
    return flag;
  }

  /**
   * Update ward assignment.
   *
   * @param empUserName the emp user name
   * @param oldWardId the old ward id
   * @param newWardId the new ward id
   * @param userName the user name
   */
  public void updateWardAssignment(String empUserName, String oldWardId, String newWardId,
      String userName)   {
    java.util.Date parsedDate = new java.util.Date();
    final java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());
    LinkedHashMap<String, Object> keys = new LinkedHashMap<String, Object>();
    keys.put("emp_username", empUserName);
    keys.put("ward_id", oldWardId);
    BasicDynaBean userWardBean = getBean();
    userWardBean.set("emp_username", empUserName);
    userWardBean.set("ward_id", newWardId);
    userWardBean.set("username", userName);
    userWardBean.set("mod_time", new Timestamp(datetime.getTime()));
    update(userWardBean, keys);
  }

  private static final String WARD_NURSE_ASSIGNMENT_VISIT_LIST = " SELECT pr.patient_id"
      + " FROM patient_registration pr"
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)"
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)"
      + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no)"
      + " LEFT JOIN nurse_ward_assignments nwa ON(COALESCE(bn.ward_no,pr.ward_id) = nwa.ward_id)"
      + " WHERE pr.mr_no = ? AND nwa.emp_username = ?";

  public List<BasicDynaBean> nurseWardAssignmentVisitList(String mrNo, String empUserName) {
    return DatabaseHelper.queryToDynaList(WARD_NURSE_ASSIGNMENT_VISIT_LIST, mrNo, empUserName);
  }
}
