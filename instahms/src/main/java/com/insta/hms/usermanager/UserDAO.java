/*
 * Copyright (c) 2007-2009 Insta Health Solutions Pvt Ltd.  All rights reserved.
 */

package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class UserDAO.
 */
public class UserDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(UserDAO.class);

  private static char[] PASS_CHARS =
      ("abcedfghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" + "01234567890@#!^-=_+<>:;{}[]|")
          .toCharArray();
  /** The con. */
  private Connection con;

  /**
   * Instantiates a new user DAO.
   *
   * @param con
   *          the con
   */
  public UserDAO(Connection con) {
    this.con = con;
  }

  /**
   * Instantiates a new user DAO.
   */
  public UserDAO() {

  }

  /*
   * TODO: move this out, we should be getting it from counter DAO
   */

  /** The Constant GET_STORES_LIST_FOR_PHARAMACY. */
  public static final String GET_STORES_LIST_FOR_PHARAMACY = "SELECT DEPT_ID, DEPT_NAME, "
      + " CENTER_NAME, s.CENTER_ID from stores s JOIN hospital_center_master hcm ON "
      + " (hcm.center_id=s.center_id) WHERE s.STATUS='A' order by dept_name";

  /**
   * Gets the pharmacy stores list.
   *
   * @return the pharmacy stores list
   * @throws SQLException
   *           the SQL exception
   */
  public List getpharmacyStoresList() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_STORES_LIST_FOR_PHARAMACY);
    ArrayList ret = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    return ret;
  }

  /** The Constant GET_BILLING_DISCOUNT_AUTHORIZER. */
  public static final String GET_BILLING_DISCOUNT_AUTHORIZER = " SELECT disc_auth_id, "
      + " disc_auth_name,status,center_id FROM discount_authorizer WHERE status='A' order by "
      + " disc_auth_name ";

  /**
   * Gets the billing authorizer.
   *
   * @return the billing authorizer
   * @throws SQLException
   *           the SQL exception
   */
  public List getBillingAuthorizer() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_BILLING_DISCOUNT_AUTHORIZER);
    ArrayList ret = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    return ret;
  }

  /** The Constant CREATE_USER. */
  public static final String CREATE_USER = "INSERT INTO u_user"
      + " (emp_username, emp_password, emp_usrremk, emp_status, is_shared_login, role_id, "
      + "  counter_id, specialization_id, lab_dept_id,pharmacy_counter_id,hosp_user,"
      + "  pharmacy_store_id,temp_username,doctor_id,inventory_store_id,scheduler_dept_id, "
      + "  sch_default_doctor,multi_store,bed_view_default_ward,mod_user,mod_date,"
      + "  sample_collection_center,po_approval_upto,email_id,mobile_no,center_id, "
      + "  allow_sig_usage_by_others, password_change_date, is_encrypted, writeoff_limit, "
      + "  permissible_discount_cap, disc_auth_id, report_center_id, sso_only_user, "
      + "  login_controls_applicable, user_first_name, user_middle_name, user_last_name, "
      + "  user_gender, employee_id, profession, employee_category,"
      + "  employee_major,force_password_change, allow_bill_fnlz_with_pat_due) "
      + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
      + "  ?,?,?,?,?,?,?,?,?,?,?) ";

  /**
   * Creates the user.
   *
   * @param user
   *          the user
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean createUser(User user) throws SQLException {

    PreparedStatement ps = con.prepareStatement(CREATE_USER);
    int index = 1;
    ps.setString(index++, user.getName());
    ps.setString(index++, user.getPassword());
    ps.setString(index++, user.getRemarks());
    ps.setString(index++, user.getStatus());
    ps.setString(index++, user.getSharedLogin());
    ps.setInt(index++, user.getRoleId());
    ps.setString(index++, user.getCounterId());
    ps.setString(index++, user.getSpecialization());
    ps.setString(index++, user.getLabDepartment());
    ps.setString(index++, user.getPharmacycounterId());
    if (user.getIsHospUser() != null) {
      ps.setString(index++, user.getIsHospUser());
    } else {
      ps.setString(index++, "Y");
    }
    ps.setString(index++, user.getPharmacyStoreId());
    ps.setString(index++, user.getFullname());
    ps.setString(index++, user.getDoctorId());
    ps.setString(index++, user.getInventoryStoreId());
    ps.setString(index++, user.getSchedulerDepartment());
    ps.setString(index++, user.getSchedulerDefaultDoctor());
    String[] ids = user.getMultiStoreId();
    String store = "";
    for (String s : ids) {
      store = store + s + ",";
    }
    ps.setString(index++, store.equals("") ? null : store.substring(0, store.length() - 1));
    ps.setString(index++, user.getBedViewDefaultWard());
    ps.setString(index++, user.getModUser());
    ps.setTimestamp(index++, DateUtil.getCurrentTimestamp());
    ps.setInt(index++, user.getSampleCollectionCenter());
    ps.setBigDecimal(index++,
        !user.getPoApprovalLimit().equals("") ? new BigDecimal(user.getPoApprovalLimit()) : null);
    ps.setString(index++, user.getEmailId());
    ps.setString(index++, user.getMobileNo());
    ps.setInt(index++, user.getUserCenter());
    ps.setString(index++, user.getAllow_sig_usage_by_others());
    ps.setTimestamp(index++, DateUtil.getCurrentTimestamp());
    ps.setBoolean(index++, true);
    ps.setBigDecimal(index++,
        !user.getWriteOffLimit().equals("") ? new BigDecimal(user.getWriteOffLimit()) : null);
    ps.setBigDecimal(index++,
        user.getPermissibleDiscountCap() != null && !user.getPermissibleDiscountCap().equals("")
            ? new BigDecimal(user.getPermissibleDiscountCap()) : BigDecimal.ZERO);
    ps.setInt(index++, user.getDiscAuthorizer());
    if (user.getReportCenter() != null) {
      ps.setObject(index++, user.getReportCenter());
    } else {
      ps.setObject(index++, null);
    }
    ps.setBoolean(index++, user.isSsoOnlyUser());
    ps.setString(index++, user.getLoginControlsApplicable());
    ps.setString(index++, user.getFirstName());
    ps.setString(index++, user.getMiddleName());
    ps.setString(index++, user.getLastName());
    ps.setString(index++, user.getGender());
    ps.setString(index++, user.getEmployeeId());
    ps.setString(index++, user.getProfession());
    ps.setString(index++, user.getEmployeeCategory());
    ps.setString(index++, user.getEmployeeMajor());
    ps.setBoolean(index++, user.getForcePasswordChange());
    ps.setString(index, user.getAllowBillFinalization());
    int count = 0;
    count = ps.executeUpdate();
    ps.close();
    return count == 1;
  }

  /** The Constant MODIFY_USER. */
  /* Filtered on Hospital users only ---hide Insta users from UI - Murali - */
  public static final String MODIFY_USER = "UPDATE u_user SET "
      + " emp_password=?, emp_usrremk=?, role_id=?, counter_id=?, specialization_id=?, "
      + " lab_dept_id=?, emp_status=?,pharmacy_counter_id=?,pharmacy_store_id=?, hosp_user='Y', "
      + " temp_username=?,doctor_id=?, inventory_store_id=?,scheduler_dept_id=?,"
      + " sch_default_doctor=?,multi_store=?,bed_view_default_ward = ?,mod_user=?, mod_date=? ,"
      + " sample_collection_center = ?,po_approval_upto=?, email_id=?, mobile_no=?, center_id=?, "
      + " is_shared_login=?, allow_sig_usage_by_others=?, writeoff_limit=?, disc_auth_id=?, "
      + " report_center_id = ?,encrypt_algo = ?,permissible_discount_cap = ?, sso_only_user=?, "
      + " login_controls_applicable = ?, user_first_name = ?, user_middle_name = ?, "
      + " user_last_name = ?, user_gender = ?, employee_id = ?, profession = ?, "
      + " employee_category = ?, employee_major = ?, "
      + " force_password_change = ?, allow_bill_fnlz_with_pat_due = ? WHERE emp_username=?";

  /**
   * Modify user.
   *
   * @param user
   *          the user
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean modifyUser(User user) throws SQLException {

    PreparedStatement ps = con.prepareStatement(MODIFY_USER);
    ps.setString(1, user.getPassword());
    ps.setString(2, user.getRemarks());
    ps.setInt(3, user.getRoleId());
    ps.setString(4, user.getCounterId());
    ps.setString(5, user.getSpecialization());
    ps.setString(6, user.getLabDepartment());
    ps.setString(7, user.getStatus());
    ps.setString(8, user.getPharmacycounterId());
    ps.setString(9, user.getPharmacyStoreId());
    ps.setString(10, user.getFullname());
    ps.setString(11, user.getDoctorId());
    ps.setString(12, user.getInventoryStoreId());
    ps.setString(13, user.getSchedulerDepartment());
    ps.setString(14, user.getSchedulerDefaultDoctor());
    String[] ids = user.getMultiStoreId();
    String store = "";
    for (String s : ids) {
      store = store + s + ",";
    }
    ps.setString(15, store.equals("") ? null : store.substring(0, store.length() - 1));
    ps.setString(16, user.getBedViewDefaultWard());
    ps.setString(17, user.getModUser());
    ps.setTimestamp(18, DateUtil.getCurrentTimestamp());
    ps.setInt(19, user.getSampleCollectionCenter());
    ps.setBigDecimal(20,
        !user.getPoApprovalLimit().equals("") ? new BigDecimal(user.getPoApprovalLimit()) : null);
    ps.setString(21, user.getEmailId());
    ps.setString(22, user.getMobileNo());
    ps.setInt(23, user.getUserCenter());
    ps.setString(24, user.getSharedLogin());
    ps.setString(25, user.getAllow_sig_usage_by_others());
    ps.setBigDecimal(26,
        !user.getWriteOffLimit().equals("") ? new BigDecimal(user.getWriteOffLimit()) : null);
    ps.setInt(27, user.getDiscAuthorizer());
    if (user.getReportCenter() != null) {
      ps.setObject(28, user.getReportCenter());
    } else {
      ps.setObject(28, null);
    }
    ps.setString(29, user.getEncryptAlgo());
    ps.setBigDecimal(30, !user.getPermissibleDiscountCap().equals("")
        ? new BigDecimal(user.getPermissibleDiscountCap()) : BigDecimal.ZERO);
    // htis is for ? symbol after where condition (emp_username col)
    ps.setBoolean(31, user.isSsoOnlyUser());
    ps.setString(32, user.getLoginControlsApplicable());
    ps.setString(33, user.getFirstName());
    ps.setString(34, user.getMiddleName());
    ps.setString(35, user.getLastName());
    ps.setString(36, user.getGender());
    ps.setString(37, user.getEmployeeId());
    ps.setString(38, user.getProfession());
    ps.setString(39, user.getEmployeeCategory());
    ps.setString(40, user.getEmployeeMajor());
    ps.setBoolean(41, user.getForcePasswordChange());
    ps.setString(42, user.getAllowBillFinalization());
    ps.setString(43, user.getName());
    int count = ps.executeUpdate();
    ps.close();
    return count == 1;
  }

  /** The Constant GET_ALL_USERS. */
  /* Filtered on Hospital users only ---hide Insta users from UI - Murali - */
  public static final String GET_ALL_USERS = "SELECT emp_username, emp_password, encrypt_algo, "
      + " emp_usrremk, counter_id, specialization_id,prescription_note_taker, "
      + " lab_dept_id, emp_status, pharmacy_counter_id, pharmacy_store_id, inventory_store_id, "
      + " temp_username, u_user.doctor_id, "
      + " u_user.role_id, u_role.role_name, u_role.role_remk, scheduler_dept_id, "
      + " sch_default_doctor,multi_store,bed_view_default_ward,po_approval_upto,email_id, "
      + " mobile_no, u_user.center_id, u_user.allow_sig_usage_by_others, "
      + " is_shared_login, d.doctor_name as sch_doctor_name, u_user.mod_user, "
      + " to_char(u_user.mod_date, 'dd-mm-yyyy HH24:mi'), "
      + " sample_collection_center, writeoff_limit, permissible_discount_cap, "
      + " u_user.disc_auth_id, report_center_id, sso_only_user, allow_bill_fnlz_with_pat_due "
      + " FROM u_user JOIN u_role USING(role_id) "
      + " LEFT JOIN doctors d ON (d.doctor_id = sch_default_doctor) "
      + " WHERE u_user.hosp_user = 'Y' and u_role.portal_id='N'";

  /** The Constant GET_ALL_HOSPITAL_USERS. */
  public static final String GET_ALL_HOSPITAL_USERS = " SELECT emp_username, emp_password, "
      + " encrypt_algo, emp_usrremk, counter_id, specialization_id,prescription_note_taker, "
      + " lab_dept_id, emp_status, pharmacy_counter_id, pharmacy_store_id, inventory_store_id, "
      + " temp_username, u_user.doctor_id, u_user.role_id, u_role.role_name, u_role.role_remk, "
      + " scheduler_dept_id, sch_default_doctor, d.doctor_name as sch_doctor_name, "
      + " multi_store,bed_view_default_ward,u_user.mod_user,u_user.login_controls_applicable, "
      + " user_first_name, user_middle_name, user_last_name, user_gender, employee_id, "
      + " profession, employee_category, employee_major, "
      + " to_char(u_user.mod_date, 'dd-mm-yyyy HH24:mi'),sample_collection_center,"
      + " po_approval_upto,email_id, mobile_no, u_user.center_id, is_shared_login, "
      + " u_user.allow_sig_usage_by_others, writeoff_limit, permissible_discount_cap, "
      + " u_user.disc_auth_id, is_encrypted, report_center_id, sso_only_user,"
      + " force_password_change, allow_bill_fnlz_with_pat_due FROM u_user  "
      + " JOIN u_role USING(role_id) "
      + " LEFT JOIN doctors d ON (d.doctor_id = sch_default_doctor) " + " WHERE  ";

  /** The Constant GET_ALL_HOSPITAL_USERS_Y. */
  public static final String GET_ALL_HOSPITAL_USERS_Y = " u_user.hosp_user = 'Y' ";

  /** The Constant GET_ALL_HOSPITAL_USERS_N. */
  public static final String GET_ALL_HOSPITAL_USERS_N = " u_user.hosp_user = 'N' ";

  /**
   * Gets the all users.
   *
   * @return the all users
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllUsers() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_ALL_HOSPITAL_USERS + GET_ALL_HOSPITAL_USERS_Y);
    ResultSet rs = ps.executeQuery();
    List allUsers = new ArrayList();

    while (rs.next()) {
      User user = new User();
      populateUser(user, rs);
      allUsers.add(user);
    }
    rs.close();
    ps.close();
    return allUsers;
  }

  /** The get all users. */
  public String getAllUsers = GET_ALL_HOSPITAL_USERS + " emp_username=?";

  /**
   * Gets the all users.
   *
   * @param con
   *          the con
   * @param name
   *          the name
   * @return the all users
   * @throws SQLException
   *           the SQL exception
   */
  public User getAllUsers(Connection con, String name) throws SQLException {
    PreparedStatement ps = con.prepareStatement(getAllUsers);
    ps.setString(1, name);
    ResultSet rs = ps.executeQuery();
    User user = null;

    if (rs.next()) {
      user = new User();
      populateUser(user, rs);
    }
    rs.close();
    ps.close();
    return user;
  }

  /** The Constant GET_ALL_ACTIVE_USERS. */
  public static final String GET_ALL_ACTIVE_USERS = GET_ALL_USERS + " AND emp_status='A' ";

  /**
   * Gets the all active users.
   *
   * @return the all active users
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllActiveUsers() throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_ALL_ACTIVE_USERS);
    ResultSet rs = ps.executeQuery();
    List allActiveUsers = new ArrayList();

    while (rs.next()) {
      User user = new User();
      populateUser(user, rs);
      allActiveUsers.add(user);
    }
    rs.close();
    ps.close();
    return allActiveUsers;
  }

  /**
   * Gets the record.
   *
   * @param userName
   *          the user name
   * @return the record
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getRecord(String userName) throws SQLException {
    return DataBaseUtil
        .queryToDynaBean("SELECT u.*, d.doctor_name,specialization FROM u_user u left join "
            + " doctors d on (u.doctor_id=d.doctor_id) WHERE emp_username=?", userName);
  }

  /**
   * Gets the logging doctor record.
   *
   * @param userName
   *          the user name
   * @return the logging doctor record
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getLoggingDoctorRecord(String userName) throws SQLException {
    return DataBaseUtil
        .queryToDynaBean("SELECT u.*, d.doctor_name,dt.dept_name,d.schedule FROM u_user u "
            + " left join doctors d on (u.doctor_id=d.doctor_id) "
            + "left join department dt using(dept_id) WHERE emp_username=?", userName);
  }

  /**
   * Gets the all store of center.
   *
   * @param centerId
   *          the center id
   * @return the all store of center
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllStoreOfCenter(int centerId) throws SQLException {
    return DataBaseUtil.queryToDynaList("select dept_id from stores s where center_id =?",
        centerId);
  }

  /** The Constant GET_ALL_ACTIVE_USERS_LIST. */
  public static final String GET_ALL_ACTIVE_USERS_LIST =
      "SELECT * FROM u_user WHERE emp_status='A' AND hosp_user='Y'";

  /**
   * Gets the all active users dyna list.
   *
   * @return the all active users dyna list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getAllActiveUsersDynaList() throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_ACTIVE_USERS_LIST);
  }

  /** The Constant GET_ALL_ACTIVE_USERS_LIST_EXCEPT. */
  public static final String GET_ALL_ACTIVE_USERS_LIST_EXCEPT =
      "SELECT * FROM u_user WHERE emp_status='A' AND hosp_user='Y' AND emp_username!=?";

  /**
   * Gets the all active users dyna list.
   *
   * @param userName
   *          the user name
   * @return the all active users dyna list
   * @throws SQLException
   *           the SQL exception
   */
  /*
   * returns the all the hospital active users excluding the user passed to the method.
   */
  public List<BasicDynaBean> getAllActiveUsersDynaList(String userName) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_ALL_ACTIVE_USERS_LIST_EXCEPT, userName);
  }

  /** The Constant GET_USER. */
  public static final String GET_USER =
      GET_ALL_HOSPITAL_USERS + GET_ALL_HOSPITAL_USERS_Y + " AND emp_username=?";

  /**
   * Gets the user.
   *
   * @param name
   *          the name
   * @return the user
   * @throws SQLException
   *           the SQL exception
   */
  public User getUser(String name) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return getUser(con, name);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the user.
   *
   * @param con
   *          the con
   * @param name
   *          the name
   * @return the user
   * @throws SQLException
   *           the SQL exception
   */
  public User getUser(Connection con, String name) throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_USER);
    ps.setString(1, name);
    ResultSet rs = ps.executeQuery();
    User user = null;

    if (rs.next()) {
      user = new User();
      populateUser(user, rs);
    }
    rs.close();
    ps.close();
    return user;
  }

  /**
   * Gets the user details.
   *
   * @param name
   *          the name
   * @return the user details
   * @throws SQLException
   *           the SQL exception
   */
  public User getUserDetails(String name) throws SQLException {
    Connection con = null;
    ResultSet rs = null;
    PreparedStatement ps = null;
    User user = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_USER);
      ps.setString(1, name);
      rs = ps.executeQuery();

      if (rs.next()) {
        user = new User();
        populateUser(user, rs);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return user;
  }

  /**
   * Populate user.
   *
   * @param user
   *          the u
   * @param rs
   *          the rs
   * @throws SQLException
   *           the SQL exception
   */
  private void populateUser(User user, ResultSet rs) throws SQLException {
    user.setName(rs.getString("emp_username"));
    user.setPassword(rs.getString("emp_password"));
    String remarks = rs.getString("emp_usrremk");
    logger.debug("Remarks: " + remarks);
    user.setRemarks(remarks);
    user.setRoleId(rs.getInt("role_id"));
    user.setCounterId(rs.getString("counter_id"));
    user.setPharmacycounterId(rs.getString("pharmacy_counter_id"));
    user.setSpecialization(rs.getString("specialization_id"));
    user.setLabDepartment(rs.getString("lab_dept_id"));
    user.setStatus(rs.getString("emp_status"));
    user.setSharedLogin(rs.getString("is_shared_login"));
    user.setRoleName(rs.getString("role_name"));
    user.setRoleRemarks(rs.getString("role_remk"));
    user.setPharmacyStoreId(rs.getString("pharmacy_store_id"));
    user.setInventoryStoreId(rs.getString("inventory_store_id"));
    String username = rs.getString("temp_username");
    user.setFullname(username);
    user.setDoctorId(rs.getString("doctor_id"));
    user.setSchedulerDepartment(rs.getString("scheduler_dept_id"));
    user.setSchedulerDefaultDoctor(rs.getString("sch_default_doctor"));
    user.setSchedulerDoctorName(rs.getString("sch_doctor_name"));
    user.setPrescription_note_taker(rs.getString("prescription_note_taker"));
    user.setMultiStoreId(
        rs.getString("multi_store") != null ? rs.getString("multi_store").split(",") : null);
    user.setBedViewDefaultWard(rs.getString("bed_view_default_ward"));
    user.setModUser(rs.getString("mod_user"));
    user.setModDate(rs.getString("to_char"));
    user.setSampleCollectionCenter(rs.getInt("sample_collection_center"));
    user.setPoApprovalLimit(rs.getString("po_approval_upto"));
    user.setEmailId(rs.getString("email_id"));
    user.setMobileNo(rs.getString("mobile_no"));
    user.setUserCenter(rs.getInt("center_id"));
    user.setAllow_sig_usage_by_others(rs.getString("allow_sig_usage_by_others"));
    user.setWriteOffLimit(rs.getString("writeoff_limit"));
    user.setPermissibleDiscountCap(rs.getString("permissible_discount_cap"));
    user.setDiscAuthorizer(rs.getInt("disc_auth_id"));
    user.setEncrypted((rs.getBoolean("is_encrypted")));
    user.setReportCenter((rs.getInt("report_center_id")));
    user.setEncryptAlgo(rs.getString("encrypt_algo"));
    user.setSsoOnlyUser(rs.getBoolean("sso_only_user"));
    user.setForcePasswordChange(rs.getBoolean("force_password_change"));
    user.setLoginControlsApplicable(rs.getString("login_controls_applicable"));
    user.setFirstName(rs.getString("user_first_name"));
    user.setMiddleName(rs.getString("user_middle_name"));
    user.setLastName(rs.getString("user_last_name"));
    user.setGender(rs.getString("user_gender"));
    user.setEmployeeId(rs.getString("employee_id"));
    user.setProfession(rs.getString("profession"));
    user.setEmployeeCategory(rs.getString("employee_category"));
    user.setEmployeeMajor(rs.getString("employee_major"));
    user.setAllowBillFinalization(rs.getString("allow_bill_fnlz_with_pat_due"));
  }

  /**
   * Delete uuser. patient entry deleting from user table in case of patientPortals.
   *
   * @param con
   *          the con
   * @param mrNo
   *          the mr no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean deleteUuser(Connection con, String mrNo) throws SQLException {

    PreparedStatement ps = null;
    int count = 0;
    try {
      ps = con.prepareStatement("delete from u_user where emp_username = ?");
      ps.setString(1, mrNo);
      count = ps.executeUpdate();

    } finally {
      ps.close();
      con.close();
    }

    logger.debug("User deletion from UserDAO ");
    return count > 0;
  }

  /** The Constant GET_PRESCRIPTION_NOTE_TAKER. */
  public static final String GET_PRESCRIPTION_NOTE_TAKER =
      "SELECT prescription_note_taker FROM u_user WHERE emp_username=?";

  /**
   * Gets the prescription note taker.
   *
   * @param userId
   *          the user id
   * @return the prescription note taker
   * @throws SQLException
   *           the SQL exception
   */
  public String getPrescriptionNoteTaker(String userId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PRESCRIPTION_NOTE_TAKER);
      ps.setString(1, userId);
      String prescriptionNoteTaker = DataBaseUtil.getStringValueFromDb(ps);
      return prescriptionNoteTaker;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_COLLECTION_CENTERS. */
  public static final String GET_COLLECTION_CENTERS = "SELECT * FROM sample_collection_centers "
      + " WHERE collection_center_id != ? AND status = ? ";

  /**
   * Gets the collection centers.
   *
   * @return the collection centers
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getCollectionCenters() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_COLLECTION_CENTERS);
      ps.setInt(1, -1);
      ps.setString(2, "A");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_LATEST_PASSWORD_LIST. */
  public static final String GET_LATEST_PASSWORD_LIST = " SELECT prev_password, password_seq_no "
      + " FROM password_history " + " WHERE username=? ORDER BY password_seq_no  ";

  /**
   * Gets the password list.
   *
   * @param lastPasswordFreq
   *          the last password freq
   * @param userName
   *          the user name
   * @param sortorder
   *          the sortorder
   * @return the password list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getPasswordList(int lastPasswordFreq, String userName,
      String sortorder) throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    try {
      if (sortorder.equals("desc")) {
        ps = con.prepareStatement(GET_LATEST_PASSWORD_LIST + "DESC LIMIT ? ");
      } else {
        ps = con.prepareStatement(GET_LATEST_PASSWORD_LIST + "ASC LIMIT ? ");
      }
      ps.setString(1, userName);
      ps.setInt(2, lastPasswordFreq);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the password list.
   *
   * @param con
   *          the con
   * @param lastPasswordFreq
   *          the last password freq
   * @param userName
   *          the user name
   * @param sortorder
   *          the sortorder
   * @return the password list
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getPasswordList(Connection con, int lastPasswordFreq, String userName,
      String sortorder) throws SQLException {
    PreparedStatement ps = null;
    try {
      if (sortorder == "desc") {
        ps = con.prepareStatement(GET_LATEST_PASSWORD_LIST + "DESC LIMIT ? ");
      } else {
        ps = con.prepareStatement(GET_LATEST_PASSWORD_LIST + "ASC LIMIT ? ");
      }
      ps.setString(1, userName);
      ps.setInt(2, lastPasswordFreq);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant DELETE_OLD_PASSWORD. */
  public static final String DELETE_OLD_PASSWORD =
      " DELETE FROM password_history " + "WHERE username=? and password_seq_no=? ";

  /**
   * Delete old password history.
   *
   * @param passwordSeqNo
   *          the password seq no
   * @param username
   *          the username
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteOldPasswordHistory(int passwordSeqNo, String username) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DELETE_OLD_PASSWORD);
      ps.setString(1, username);
      ps.setInt(2, passwordSeqNo);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Delete old password history.
   *
   * @param con
   *          the con
   * @param passwordSeqNo
   *          the password seq no
   * @param username
   *          the username
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean deleteOldPasswordHistory(Connection con, int passwordSeqNo, String username)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DELETE_OLD_PASSWORD);
      ps.setString(1, username);
      ps.setInt(2, passwordSeqNo);
      int rowsDeleted = ps.executeUpdate();
      return (rowsDeleted != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /** The Constant INSERT_PASSWORD_HISTORY. */
  public static final String INSERT_PASSWORD_HISTORY =
      " INSERT INTO password_history VALUES(?, ?, ?)";

  /**
   * Insert new pasword history.
   *
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param passwordseqNo
   *          the passwordseq no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean insertNewPaswordHistory(String userName, String password, int passwordseqNo)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(INSERT_PASSWORD_HISTORY);
      ps.setString(1, userName);
      ps.setString(2, password);
      ps.setInt(3, passwordseqNo);
      int row = ps.executeUpdate();
      return (row != 0);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Insert new pasword history.
   *
   * @param con
   *          the con
   * @param userName
   *          the user name
   * @param password
   *          the password
   * @param passwordseqNo
   *          the passwordseq no
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean insertNewPaswordHistory(Connection con, String userName, String password,
      int passwordseqNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(INSERT_PASSWORD_HISTORY);
      ps.setString(1, userName);
      ps.setString(2, password);
      ps.setInt(3, passwordseqNo);
      int row = ps.executeUpdate();
      return (row != 0);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }

  /**
   * Gets the user bean.
   *
   * @param userId
   *          the user id
   * @return the user bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getUserBean(String userId) throws SQLException {
    return DataBaseUtil.queryToDynaBean("select * from u_user where emp_username=?", userId);
  }

  /**
   * Gets the user bean.
   *
   * @param con
   *          the con
   * @param userId
   *          the user id
   * @return the user bean
   * @throws SQLException
   *           the SQL exception
   */
  public static BasicDynaBean getUserBean(Connection con, String userId) throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<BasicDynaBean> list;
    try {
      ps = con.prepareStatement("select * from u_user where emp_username=?");
      ps.setString(1, userId);
      rs = ps.executeQuery();
      RowSetDynaClass rsd = new RowSetDynaClass(rs);
      list = rsd.getRows();
    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
    if ((list != null) && (list.size() > 0)) {
      return list.get(0);
    }
    return null;
  }

  /** The Constant DOCTORS_NAME_EMP_USERNAME_. */
  public static final String DOCTORS_NAME_EMP_USERNAME_ = "select u.emp_username as login_name, "
      + " case when u.doctor_id IS NULL or u.doctor_id = '' then u.emp_username else "
      + " d.doctor_name end as name from u_user u "
      + " LEFT JOIN doctors d ON u.doctor_id = d.doctor_id order by u.emp_username ASC ";

  /**
   * Gets the doctors name emp name.
   *
   * @return the doctors name emp name
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getDoctorsNameEmpName() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(DOCTORS_NAME_EMP_USERNAME_);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /** The Constant Stock_Adjust_Reason. */
  public static final String Stock_Adjust_Reason = "select adjustment_reason_id,adjustment_reason "
      + " from stock_adjustment_reason_master where status='A' order by adjustment_reason  ";

  /**
   * Gets the stock adjust reason.
   *
   * @return the stock adjust reason
   * @throws SQLException
   *           the SQL exception
   */
  public List<BasicDynaBean> getStockAdjustReason() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(Stock_Adjust_Reason);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /** The Constant GET_ALL_HOSPITAL_ROLE_SPECIFIC_USERS. */
  public static final String GET_ALL_HOSPITAL_ROLE_SPECIFIC_USERS = " select distinct emp_username"
      + " from u_user uu JOIN user_hosp_role_master uhrm ON(uhrm.u_user =uu.emp_username)";

  /**
   * Gets the all hospital specific users.
   *
   * @param hospRoles
   *          the hosp roles
   * @return the all hospital specific users
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllHospitalSpecificUsers(String[] hospRoles)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int index = 1;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_ALL_HOSPITAL_ROLE_SPECIFIC_USERS);
      List<Integer> usersList = new ArrayList<Integer>();
      if (hospRoles != null) {
        for (String hospRole : hospRoles) {
          if (hospRole != null && !hospRole.isEmpty()) {
            usersList.add(Integer.parseInt(hospRole));
          }
        }
      }
      if (usersList != null && usersList.size() > 0) {
        DataBaseUtil.addWhereFieldInList(query, "hosp_role_id", usersList, false);
        ps = con.prepareStatement(query.toString());
        query.append("AND emp_status = ?");
        if (RequestContext.getCenterId() > 0) {
          query.append("AND center_id = ?");
        }
        ps = con.prepareStatement(query.toString());
        for (Integer userRole : usersList) {
          ps.setInt(index++, userRole);
        }
        ps.setString(index++, "A");
        if (RequestContext.getCenterId() > 0) {
          ps.setInt(index++, RequestContext.getCenterId());
        }
        return DataBaseUtil.queryToDynaList(ps);
      } else {
        return null;
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all hospital specific users.
   *
   * @param hospRoles
   *          the hosp roles
   * @param deptId
   *          the dept id
   * @return the all hospital specific users
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getAllHospitalSpecificUsers(String[] hospRoles, String deptId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int index = 1;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_ALL_HOSPITAL_ROLE_SPECIFIC_USERS);
      List<Integer> usersList = new ArrayList<Integer>();
      if (hospRoles != null) {
        for (String hospRole : hospRoles) {
          if (hospRole != null && !hospRole.isEmpty()) {
            usersList.add(Integer.parseInt(hospRole));
          }
        }
      }
      if (usersList != null && usersList.size() > 0) {
        DataBaseUtil.addWhereFieldInList(query, "hosp_role_id", usersList, false);
        query.append(" AND lab_dept_id = ?");
        query.append("AND emp_status = ?");
        if (RequestContext.getCenterId() > 0) {
          query.append("AND (center_id = 0 OR center_id = ?)");
        }
        ps = con.prepareStatement(query.toString());
        for (Integer userRole : usersList) {
          ps.setInt(index++, userRole);
        }
        ps.setString(index++, deptId);
        ps.setString(index++, "A");
        if (RequestContext.getCenterId() > 0) {
          ps.setInt(index++, RequestContext.getCenterId());
        }
        return DataBaseUtil.queryToDynaList(ps);
      } else {
        return null;
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CENTERWISE_GET_LAB_TECHNICIAN. */
  public static final String CENTERWISE_GET_LAB_TECHNICIAN =
      " select  * from u_user where lab_dept_id = ? and (center_id = ? OR center_id = 0)";

  /** The Constant GET_LAB_TECHNICIAN. */
  public static final String GET_LAB_TECHNICIAN = " select  * from u_user where lab_dept_id = ? ";

  /**
   * Gets the lab technician.
   *
   * @param centerId
   *          the center id
   * @param deptId
   *          the dept id
   * @return the lab technician
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> getLabTechnician(int centerId, String deptId)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        ps = con.prepareStatement(CENTERWISE_GET_LAB_TECHNICIAN);
        ps.setString(1, deptId);
        ps.setInt(2, centerId);
      } else {
        ps = con.prepareStatement(GET_LAB_TECHNICIAN);
        ps.setString(1, deptId);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the center id.
   *
   * @param deptId
   *          the dept id
   * @return the center id
   * @throws SQLException
   *           the SQL exception
   */
  /* This method return the centerId of passed store */
  public static BasicDynaBean getCenterId(int deptId) throws SQLException {
    return DataBaseUtil.queryToDynaBean("select center_id from stores s where dept_id =?", deptId);
  }

  /**
   * Change password.
   *
   * @param con
   *          the con
   * @param userId
   *          the user id
   * @param oldPassword
   *          the old password
   * @param newPassword
   *          the new password
   * @return the string
   */
  public String changePassword(Connection con, String userId, String oldPassword,
      String newPassword) {

    String target = "success";
    PreparedStatement ps = null;

    try {
      String updatePassword = "UPDATE U_USER SET EMP_PASSWORD=?,PASSWORD_CHANGE_DATE=now() WHERE "
          + " EMP_USERNAME=? AND EMP_PASSWORD=?";
      ps = con.prepareStatement(updatePassword);
      ps.setString(1, newPassword);
      ps.setString(2, userId);
      ps.setString(3, oldPassword);
      int rows = ps.executeUpdate();
      if (rows == 0) {
        target = "failure";
      }
      ps.close();
    } catch (Exception exception) {
      exception.printStackTrace();
      logger.error("Exception Occured while updating password", exception);
      target = "failure";
    }
    return target;
  }

  /**
   * Gets the store users.
   *
   * @return the store users
   * @throws SQLException
   *           the SQL exception
   */
  public static List<String> getStoreUsers() throws SQLException {
    return DataBaseUtil.queryToArrayList1(
        "select emp_username from u_user where pharmacy_store_id IS NOT NULL AND "
            + " pharmacy_store_id <> '' AND emp_status = 'A'  order by emp_username");
  }

  /**
   * Gets the store users with auto PO.
   *
   * @return the store users with auto PO
   * @throws SQLException
   *           the SQL exception
   */
  public static List<String> getStoreUsersWithAutoPO() throws SQLException {
    return DataBaseUtil.queryToArrayList1(
        "select emp_username from u_user where ((pharmacy_store_id IS NOT NULL and "
            + " pharmacy_store_id <> '') or  emp_username='auto_po') AND emp_status = 'A' "
            + " order by emp_username");
  }

  public static String generateRandomPassword(int length) {
    return RandomStringUtils.random(length, 0, PASS_CHARS.length, false, false, PASS_CHARS);

  }

}