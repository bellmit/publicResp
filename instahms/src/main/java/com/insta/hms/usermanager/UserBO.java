/*
 * Copyright (c) 2007-2009 Insta Health Solutions Pvt Ltd.  All rights reserved.
 */

package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class UserBO.
 */
public class UserBO {
  
  private static final GenericDAO passwordHistoryDAO = new GenericDAO("password_history");
  private static final GenericDAO passwordRuleDAO = new GenericDAO("password_rule");

  /**
   * Gets the pharmacy stores list.
   *
   * @return the pharmacy stores list
   * @throws SQLException
   *           the SQL exception
   */
  public List getpharmacyStoresList() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    UserDAO dao = new UserDAO(con);
    List list = dao.getpharmacyStoresList();
    con.close();
    return list;
  }

  /**
   * Gets the billing authorizer.
   *
   * @return the billing authorizer
   * @throws SQLException
   *           the SQL exception
   */
  public List getBillingAuthorizer() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    UserDAO dao = new UserDAO(con);
    List list = dao.getBillingAuthorizer();
    con.close();
    return list;
  }

  /**
   * Creates the user.
   *
   * @param user
   *          the user
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Map createUser(User user) throws SQLException, FileNotFoundException, IOException {
    Connection con = DataBaseUtil.getConnection();
    UserDAO dao = new UserDAO(con);
    List activeUsers = dao.getAllActiveUsersDynaList();
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
    Map resultMap = new HashMap();
    if (genPrefs.get("max_active_hosp_users") != null
        && (Integer) genPrefs.get("max_active_hosp_users") <= activeUsers.size()) {
      resultMap.put("message",
          "Number of active users exceeded. Current active users: " + activeUsers.size()
              + "; Allowed active users: " + (Integer) genPrefs.get("max_active_hosp_users"));
      resultMap.put("isSuccess", false);
      return resultMap;
    }
    // User existing = dao.getUser(user.getName());
    User existing = dao.getAllUsers(con, user.getName());
    if (existing != null) {
      con.close();
      resultMap.put("message", "User Already Exists");
      resultMap.put("isSuccess", false);
      return resultMap;
    }

    user.setStatus("A");
    boolean success = genPrefs.get("max_active_hosp_users") != null ? dao.createUser(user) : false;
    if (success && user.getUserSignature().getFileName() != null
        && !user.getUserSignature().getFileName().equals("")) {
      UserSignatureDAO sigDAO = new UserSignatureDAO();
      BasicDynaBean sigBean = sigDAO.getBean();
      sigBean.set("emp_username", user.getName());
      sigBean.set("signature", user.getUserSignature().getInputStream());
      sigBean.set("signature_content_type", user.getUserSignature().getContentType());
      success = sigDAO.insert(con, sigBean);
    }

    /*
     * if(success && user.getSerdeptid() != null && !user.getSerdeptid().equals("")){
     * UserServiceDeptDAO userservicedeptDAO =new UserServiceDeptDAO(); BasicDynaBean
     * servicedeptBean = userservicedeptDAO.getBean(); for (int i=0; i<user.getSerdeptid().length;
     * i++) { servicedeptBean.set("emp_username",user.getName());
     * servicedeptBean.set("serv_dept_id", Integer.parseInt(user.getSerdeptid()[i])); success =
     * userservicedeptDAO.insert(con, servicedeptBean); } }
     */

    con.close();

    resultMap.put("message",
        success
            ? "Successful in adding new user.  Current active users: " + activeUsers.size()
                + "; Allowed active users: " + (Integer) genPrefs.get("max_active_hosp_users")
            : "User creation failed");
    resultMap.put("isSuccess", success);

    return resultMap;
  }

  /**
   * Modify user.
   *
   * @param user
   *          the user
   * @return the map
   * @throws SQLException
   *           the SQL exception
   * @throws FileNotFoundException
   *           the file not found exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public Map modifyUser(User user) throws SQLException, FileNotFoundException, IOException {
    Connection con = null;
    Map resultMap = new HashMap();
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      UserDAO dao = new UserDAO(con);
      BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
      List activeUsers = dao.getAllActiveUsersDynaList(user.getName());

      if (genPrefs.get("max_active_hosp_users") != null && user.getStatus().equals("A")
          && (Integer) genPrefs.get("max_active_hosp_users") <= activeUsers.size()) {
        resultMap.put("message",
            "Number of active users exceeded. Current active users: " + activeUsers.size()
                + "; Allowed active users: " + (Integer) genPrefs.get("max_active_hosp_users"));
        resultMap.put("isSuccess", false);
        return resultMap;
      }
      success = genPrefs.get("max_active_hosp_users") != null ? dao.modifyUser(user) : false;
      if (success) {

        UserSignatureDAO sigDAO = new UserSignatureDAO();
        BasicDynaBean sigBean = sigDAO.getBean();
        sigDAO.loadByteaRecords(sigBean, "emp_username", user.getName());
        if (sigBean.get("emp_username") == null) {
          if (!user.getUserSignature().getFileName().equals("")) {
            sigBean.set("emp_username", user.getName());
            sigBean.set("signature", user.getUserSignature().getInputStream());
            sigBean.set("signature_content_type", user.getUserSignature().getContentType());
            success = sigDAO.insert(con, sigBean);
          }
        } else {
          if (!user.getUserSignature().getFileName().equals("")) {
            sigBean.set("emp_username", user.getName());
            sigBean.set("signature", user.getUserSignature().getInputStream());
            sigBean.set("signature_content_type", user.getUserSignature().getContentType());
            success = sigDAO.update(con, sigBean.getMap(), "emp_username", user.getName()) == 1;
          } else {
            // ignore it if is empty.
          }
        }

      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    resultMap.put("message",
        success ? "User details edited successfully" : "User deatils editing failed");
    resultMap.put("isSuccess", success);
    return resultMap;
  }

  /**
   * Gets the all users.
   *
   * @return the all users
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllUsers() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    UserDAO dao = new UserDAO(con);
    List list = dao.getAllUsers();
    con.close();
    return list;
  }

  /**
   * Gets the all active users.
   *
   * @return the all active users
   * @throws SQLException
   *           the SQL exception
   */
  public List getAllActiveUsers() throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    UserDAO dao = new UserDAO(con);
    List list = dao.getAllActiveUsers();
    con.close();
    return list;
  }

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
    UserDAO dao = new UserDAO(con);
    User user = dao.getUser(name);
    con.close();
    return user;
  }

  /**
   * Check password strength.
   *
   * @param password
   *          the password
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String checkPasswordStrength(String password) throws SQLException {
    String msg = null;
    BasicDynaBean bean = passwordRuleDAO.getRecord();
    String specailCharList = (String) bean.get("specail_char_list");
    char[] passwordCharArray = password.toCharArray();

    if ( specailCharList != null && !(specailCharList.isEmpty()) ) {
      List<Character> specialCharsList = new ArrayList<>();
      for ( char specialChar: specailCharList.toCharArray() ) {
        specialCharsList.add(specialChar);
      }
      //check if every password special chars present in specialCharsList
      for ( char passwordChar: passwordCharArray ) {
        if (!Character.isLetterOrDigit(passwordChar) && !specialCharsList.contains(passwordChar)) {
          msg = "Password should only contain special characters from " + specailCharList;
          return msg;
        }
      }
    }

    int passwordLen = password.length();
    int minLen = (Integer) bean.get("min_len");
    int minLower = (Integer) bean.get("min_lower");
    int minUpper = (Integer) bean.get("min_upper");
    int minDigits = (Integer) bean.get("min_digits");
    int minSpecialChars = (Integer) bean.get("min_special_chars");

    int lowerCaseCount = 0;
    int upperCaseCount = 0;
    int digitsCount = 0;
    int specialCharsCount = 0;

    for (int i = 0; i < passwordCharArray.length; i++) {
      if (Character.isLowerCase(passwordCharArray[i])) {
        lowerCaseCount++;
      }
      if (Character.isUpperCase(passwordCharArray[i])) {
        upperCaseCount++;
      }
      if (Character.isDigit(passwordCharArray[i])) {
        digitsCount++;
      }

      if (specailCharList != null && !specailCharList.equals("")) {
        if (specailCharList.contains(Character.toString(passwordCharArray[i]))) {
          char[] specialChars = specailCharList.toCharArray();
          for ( char specialChar : specialChars ) {
            if (passwordCharArray[i] == specialChar) {
              specialCharsCount++;
              break;
            }
          }
        }
      }
    }

    if (passwordLen < minLen) {
      msg = "Password length should not be less than " + minLen + " .";
      return msg;
    } else if (lowerCaseCount < minLower) {
      msg = "Password should contain atleast " + minLower + " lower case letters.";
      return msg;
    } else if (upperCaseCount < minUpper) {
      msg = "Password should contain atleast " + minUpper + " upper case letters.";
      return msg;
    } else if (digitsCount < minDigits) {
      msg = "Password should contain atleast " + minDigits + " numbers.";
      return msg;
    } else if (specialCharsCount < minSpecialChars) {
      String specialChars = specailCharList != null ? specailCharList : " ";
      msg = "Password should contain atleast " + minSpecialChars + " special characters from "
          + specialChars;
      return msg;
    }

    return msg;
  }

  /**
   * Check password strength.
   *
   * @param con
   *          the con
   * @param password
   *          the password
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String checkPasswordStrength(Connection con, String password) throws SQLException {
    String msg = null;
    BasicDynaBean bean = passwordRuleDAO.getRecord(con);
    String specailCharList = (String) bean.get("specail_char_list");
    char[] passwordCharArray = password.toCharArray();

    if ( specailCharList != null && !(specailCharList.isEmpty()) ) {
      List<Character> specialCharsList = new ArrayList<>();
      for ( char specialChar: specailCharList.toCharArray() ) {
        specialCharsList.add(specialChar);
      }
      //check if every password special chars present in specialCharsList
      for ( char passwordChar: passwordCharArray ) {
        if (!Character.isLetterOrDigit(passwordChar) && !specialCharsList.contains(passwordChar)) {
          msg = "Password should only contain special characters from " + specailCharList;
          return msg;
        }
      }
    }

    int passwordLen = password.length();
    int minLen = (Integer) bean.get("min_len");
    int minLower = (Integer) bean.get("min_lower");
    int minUpper = (Integer) bean.get("min_upper");
    int minDigits = (Integer) bean.get("min_digits");
    int minSpecialChars = (Integer) bean.get("min_special_chars");

    int lowerCaseCount = 0;
    int upperCaseCount = 0;
    int digitsCount = 0;
    int specialCharsCount = 0;

    for (int i = 0; i < passwordCharArray.length; i++) {
      if (Character.isLowerCase(passwordCharArray[i])) {
        lowerCaseCount++;
      }
      if (Character.isUpperCase(passwordCharArray[i])) {
        upperCaseCount++;
      }
      if (Character.isDigit(passwordCharArray[i])) {
        digitsCount++;
      }

      if (specailCharList != null && !specailCharList.equals("")) {
        if (specailCharList.contains(Character.toString(passwordCharArray[i]))) {
          char[] specialChars = specailCharList.toCharArray();
          for ( char specialChar : specialChars ) {
            if (passwordCharArray[i] == specialChar) {
              specialCharsCount++;
              break;
            }
          }
        }
      }
    }

    if (passwordLen < minLen) {
      msg = "Password length should not be less than " + minLen + " .";
      return msg;
    } else if (lowerCaseCount < minLower) {
      msg = "Password should contain atleast " + minLower + " lower case letters.";
      return msg;
    } else if (upperCaseCount < minUpper) {
      msg = "Password should contain atleast " + minUpper + " upper case letters.";
      return msg;
    } else if (digitsCount < minDigits) {
      msg = "Password should contain atleast " + minDigits + " numbers.";
      return msg;
    } else if (specialCharsCount < minSpecialChars) {
      String specialChars = specailCharList != null ? specailCharList : " ";
      msg = "Password should contain atleast " + minSpecialChars + " special characters from "
          + specialChars;
      return msg;
    }

    return msg;
  }

  /**
   * Check password frequency.
   *
   * @param password
   *          the password
   * @param userName
   *          the user name
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String checkPasswordFrequency(String password, String userName) throws SQLException {
    String msg = null;
    UserDAO userDao = new UserDAO();
    BasicDynaBean passwordRuleBean = passwordRuleDAO.getRecord();
    List<BasicDynaBean> list = passwordHistoryDAO.findAllByKey("username", userName);
    List<BasicDynaBean> latestPasswordlist = null;
    List<BasicDynaBean> deletePasswordlist = null;
    int listSize = list.size();
    int lastPasswordFrequency = (Integer) passwordRuleBean.get("last_password_frequency");

    if (list != null) {

      if (listSize > lastPasswordFrequency) {
        int deleteCount = listSize - lastPasswordFrequency;
        deletePasswordlist = userDao.getPasswordList(deleteCount, userName, "asc");
        for (int i = 0; i < deletePasswordlist.size(); i++) {
          BasicDynaBean record = list.get(i);
          userDao.deleteOldPasswordHistory((Integer) record.get("password_seq_no"), userName);
        }
        latestPasswordlist = userDao.getPasswordList(lastPasswordFrequency, userName, "desc");
        list = latestPasswordlist != null ? latestPasswordlist : list;
      }

      for (BasicDynaBean bean : list) {
        String prevPassword = (String) bean.get("prev_password");
        if (password.equals(prevPassword)) {
          msg = "Please do not enter last " + lastPasswordFrequency + " passwords.";
          break;
        }
      }
    }
    return msg;
  }

  /**
   * Check password frequency.
   *
   * @param con
   *          the con
   * @param password
   *          the password
   * @param userName
   *          the user name
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public String checkPasswordFrequency(Connection con, String password, String userName)
      throws SQLException {
    String msg = null;
    UserDAO userDao = new UserDAO();
    BasicDynaBean passwordRuleBean = passwordRuleDAO.getRecord(con);
    List<BasicDynaBean> list = passwordHistoryDAO.findAllByKey(con, "username", userName);
    List<BasicDynaBean> latestPasswordlist = null;
    List<BasicDynaBean> deletePasswordlist = null;
    int listSize = list.size();
    int lastPasswordFrequency = (Integer) passwordRuleBean.get("last_password_frequency");

    if (list != null) {

      if (listSize > lastPasswordFrequency) {
        int deleteCount = listSize - lastPasswordFrequency;
        deletePasswordlist = userDao.getPasswordList(con, deleteCount, userName, "asc");
        for (int i = 0; i < deletePasswordlist.size(); i++) {
          BasicDynaBean record = list.get(i);
          userDao.deleteOldPasswordHistory(con, (Integer) record.get("password_seq_no"), userName);
        }
        latestPasswordlist = userDao.getPasswordList(lastPasswordFrequency, userName, "desc");
        list = latestPasswordlist != null ? latestPasswordlist : list;
      }

      for (BasicDynaBean bean : list) {
        String prevPassword = (String) bean.get("prev_password");
        if (PasswordEncoder.matches(password, prevPassword)) {
          msg = "Please do not enter last " + lastPasswordFrequency + " passwords.";
          break;
        }
      }
    }
    return msg;
  }

  /**
   * Update password history.
   *
   * @param userName
   *          the user name
   * @param oldPassword
   *          the old password
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updatePasswordHistory(String userName, String oldPassword) throws SQLException {
    UserDAO userDao = new UserDAO();
    List<BasicDynaBean> list = passwordHistoryDAO.findAllByKey("username",
        userName);
    int lastPasswordFrequency = (Integer) passwordRuleDAO.getRecord()
        .get("last_password_frequency");
    int listSize = list.size();
    boolean success = false;
    List<BasicDynaBean> deletePasswordlist = null;
    if (listSize == lastPasswordFrequency) {
      deletePasswordlist = userDao.getPasswordList(1, userName, "asc");
      for (int i = 0; i < deletePasswordlist.size(); i++) {
        BasicDynaBean record = list.get(i);
        success = userDao.deleteOldPasswordHistory((Integer) record.get("password_seq_no"),
            userName);
      }
    }
    if (lastPasswordFrequency != 0) {
      success = userDao.insertNewPaswordHistory(userName, oldPassword,
          DataBaseUtil.getNextSequence("password_seq"));
    }

    return success;
  }

  /**
   * Update password history.
   *
   * @param con
   *          the con
   * @param userName
   *          the user name
   * @param oldPassword
   *          the old password
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public boolean updatePasswordHistory(Connection con, String userName, String oldPassword)
      throws SQLException {
    UserDAO userDao = new UserDAO();
    List<BasicDynaBean> list = passwordHistoryDAO.findAllByKey(con, "username",
        userName);
    int lastPasswordFrequency = (Integer) passwordRuleDAO.getRecord(con)
        .get("last_password_frequency");
    int listSize = list.size();
    boolean success = false;
    List<BasicDynaBean> deletePasswordlist = null;
    if (listSize == lastPasswordFrequency) {
      deletePasswordlist = userDao.getPasswordList(con, 1, userName, "asc");
      for (int i = 0; i < deletePasswordlist.size(); i++) {
        BasicDynaBean record = list.get(i);
        success = userDao.deleteOldPasswordHistory(con, (Integer) record.get("password_seq_no"),
            userName);
      }
    }
    if (lastPasswordFrequency != 0) {
      success = userDao.insertNewPaswordHistory(con, userName, oldPassword,
          DataBaseUtil.getNextSequence(con, "password_seq"));
    }

    return success;
  }

}
