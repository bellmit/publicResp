/*
 * Copyright (c) 2007-2009 Insta Health Solutions Pvt Ltd.  All rights reserved.
 */

package com.insta.hms.usermanager;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CounterMaster.CounterMasterDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.ServiceMaster.ServiceDepartmentDAO;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupService;
import com.insta.hms.mdm.confidentialitygrpmaster.UserConfidentialityAssociationService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.mdm.usercentercounters.UserBillingCenterCounterMappingService;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * The Class UserAction.
 */
public class UserAction extends DispatchAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(UserAction.class);

  /** The counter dao. */
  CounterMasterDAO counterDao = new CounterMasterDAO();

  /** The userhosp role DAO. */
  UserHospitalRoleMasterDAO userhospRoleDAO = new UserHospitalRoleMasterDAO();

  /** The userservicedept DAO. */
  UserServiceDeptDAO userservicedeptDAO = new UserServiceDeptDAO();

  /** THE usertheatre DAO. */
  UserTheatreDAO usertheatreDAO = new UserTheatreDAO();

  /** THE theatremaster DAO. */
  TheatreMasterDAO theatreMasterDAO = new TheatreMasterDAO();

  /** The confidentialityGroupService. */
  private static ConfidentialityGroupService confidentialityGroupService =
      ApplicationContextProvider.getApplicationContext().getBean(
          ConfidentialityGroupService.class);

  /** The userConfidentialityAssociationService. */
  private static UserConfidentialityAssociationService userConfidentialityAssociationService =
      ApplicationContextProvider.getApplicationContext().getBean(
          UserConfidentialityAssociationService.class);
  
  private static UserBillingCenterCounterMappingService counterMappingService = 
      ApplicationContextProvider.getBean(UserBillingCenterCounterMappingService.class);

  /** The hospitalRoleService. */
  private static HospitalRoleService hospitalRoleService =
      ApplicationContextProvider.getApplicationContext().getBean(HospitalRoleService.class);
  
  private static final GenericDAO passwordHistoryDAO = new GenericDAO("password_history");
  private static final GenericDAO passwordRuleDAO = new GenericDAO("password_rule");

  /**
   * Get the user screen and show it.
   *
   * @param mapping
   *          the mapping
   * @param af
   *          the af
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the user screen
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getUserScreen(ActionMapping mapping, ActionForm af,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

    List<String> userConfidentialityGroupIds = new ArrayList<>();
    String user = request.getParameter("userName");
    if (user != null) {
      List<BasicDynaBean> rolesList = UserHospitalRoleMasterDAO.getUserHospRoleIds(user);
      String[] roleIds = new String[rolesList.size()];
      int index = 0;
      for (BasicDynaBean bean : rolesList) {
        roleIds[index] = bean.getMap().get("hosp_role_id").toString();
        index++;
      }
      String roles = Arrays.toString(roleIds);
      request.setAttribute("userHospitalRoleIds", CommonUtils
          .getStringArrayFromCommaSeparatedString(roles.substring(1, roles.length() - 1)));
      List<BasicDynaBean> userConfidentialityGroups = confidentialityGroupService
          .getUserConfidentialityGroups(user);
      for (BasicDynaBean group : userConfidentialityGroups) {
        Integer groupId = (int) group.get("confidentiality_grp_id");
        if (groupId != 0) { // Don't include General Patient group
          userConfidentialityGroupIds.add(groupId.toString());
        }
      }
    }
    request.setAttribute("userConfidentialityGroupIds",
        userConfidentialityGroupIds.toArray(new String[0]));
    if (user != null) {
      List<BasicDynaBean> servdeptList = ServiceDepartmentDAO.getUserServiceDepts(user);
      String[] servdeptIds = new String[servdeptList.size()];
      int index = 0;
      for (BasicDynaBean bean : servdeptList) {
        servdeptIds[index] = bean.getMap().get("serv_dept_id").toString();
        index++;
      }
      String serviceDept = Arrays.toString(servdeptIds);
      request.setAttribute("userServiceDeptsIds",
          CommonUtils.getStringArrayFromCommaSeparatedString(
              serviceDept.substring(1, serviceDept.length() - 1)));
    }

    if (user != null) {
      List<BasicDynaBean> userTheatreList = theatreMasterDAO.getUserTheatres(user);
      String[] theatreIds = new String[userTheatreList.size()];
      int index = 0;
      for (BasicDynaBean bean : userTheatreList) {
        theatreIds[index] = bean.getMap().get("theatre_id").toString();
        index++;
      }
      String theatre = Arrays.toString(theatreIds);
      request.setAttribute("userTheatreIds", CommonUtils
          .getStringArrayFromCommaSeparatedString(theatre.substring(1, theatre.length() - 1)));
    }

    List docDeptNameList = new DoctorMasterDAO().getSchedulableDoctorDepartmentNames(null);
    request.setAttribute("docDeptNameList",
        new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));
    request.setAttribute("confidentialityGroups",
        confidentialityGroupService.getUserDefinedConfidentialityGroups());

    setAttributes(request);
    return mapping.findForward("getUserScreen");
  }

  /**
   * Save user.
   * Action to create or modify user based on operation, called from user.jsp submit of userForm.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SQLException
   *           the SQL exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   * @throws NoSuchAlgorithmException
   *           the no such algorithm exception
   */
  public ActionForward saveUser(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException, SQLException,
      IllegalAccessException, InvocationTargetException, NoSuchAlgorithmException {

    Connection con = null;
    boolean success = true;
    String name = null;
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");

    UserForm userForm = (UserForm) form;
    String[] confidentialityGroupIds = request.getParameterValues("confidentialityGroupIds");
    String[] hospRoles = request.getParameterValues("hospitalRoleIds");
    String[] serdeptid = request.getParameterValues("serdeptid");
    String[] theatreid = request.getParameterValues("multiTheatreId");
    String defaultTheateId = (String) request.getParameter("defaultTheatresId");
    BasicDynaBean userHospbean = userhospRoleDAO.getBean();
    BasicDynaBean servicedeptBean = userservicedeptDAO.getBean();
    BasicDynaBean usertheatreBean;
    ArrayList<BasicDynaBean> theatresList = new ArrayList<BasicDynaBean>();
    UserBO userBO = new UserBO();
    FlashScope flash = FlashScope.getScope(request);
    Map resultMap = null;
    User objDto = new User();
    BeanUtils.copyProperties(objDto, userForm);
    objDto.setModUser(userName);
    objDto.setSerdeptid(serdeptid);
    boolean isForcePasswordChange = request.getParameter("forcePasswordChange") != null
            && request.getParameter("forcePasswordChange").equals("on");
    objDto.setForcePasswordChange(isForcePasswordChange);
    String password = userForm.getPassword();
    boolean ssoOnlyUser = request.getParameter("ssoOnlyUser") != null
        && request.getParameter("ssoOnlyUser").equals("on");
    objDto.setSsoOnlyUser(ssoOnlyUser);
    if (ssoOnlyUser) {
      objDto.setName(objDto.getName() != null ? objDto.getName().toLowerCase() : "");
      password = UserDAO.generateRandomPassword(32);
    }
    String message = null;
    BasicDynaBean bean = null;
    String currentPwd = null;
    boolean checkStrength = true;
    if (userForm.getOp().equalsIgnoreCase("edit")) {
      name = userForm.getSelUserName();
    } else {
      name = objDto.getName();
    }

    boolean user = false;
    user = userhospRoleDAO.exist("u_user", name);
    if (userForm.getOp().equalsIgnoreCase("edit")) {
      bean = new GenericDAO("u_user").findByKey("emp_username", userForm.getSelUserName());
      currentPwd = (String) bean.get("emp_password");
      if (currentPwd.equals(password)) {
        checkStrength = false;
      }
    }
    if (checkStrength && !ssoOnlyUser) {
      message = checkPasswordStrength(password);
    }

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));

    if (message == null) {
      if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
        objDto.setSampleCollectionCenter(
            objDto.getUserCenter() == 0 || objDto.getSampleCollectionCenter() == 0 ? -1
                : objDto.getSampleCollectionCenter());
      } else {
        objDto.setSampleCollectionCenter(
            objDto.getSampleCollectionCenter() == 0 ? -1 : objDto.getSampleCollectionCenter());
      }

      if (userForm.getOp().equalsIgnoreCase("create")) {
        password = PasswordEncoder.encode(password);
        objDto.setPassword(password);
        objDto.setEncryptAlgo("BCRYPT");
        resultMap = userBO.createUser(objDto);
        if (!(Boolean) resultMap.get("isSuccess")) {
          flash.put("error", resultMap.get("message"));
        }

      }
      if (hospRoles != null || user) {
        try {
          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);
          if (user) {
            success = userhospRoleDAO.delete(con, "u_user", name);
          }
          if (hospRoles != null) {
            for (int i = 0; i < hospRoles.length; i++) {
              String roleId = hospRoles[i];
              userHospbean.set("hosp_role_user_id", userhospRoleDAO.getNextSequence());
              userHospbean.set("hosp_role_id", Integer.parseInt(roleId));
              userHospbean.set("u_user", name);
              success = userhospRoleDAO.insert(con, userHospbean);
            }
          }
        } finally {
          DataBaseUtil.commitClose(con, success);
        }
      }
      user = userservicedeptDAO.exist("emp_username", name);
      if (serdeptid != null || user) {
        try {
          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);
          if (user) {
            success = userservicedeptDAO.delete(con, "emp_username", name);
          }
          if (serdeptid != null) {
            for (int i = 0; i < serdeptid.length; i++) {
              String servdeptid = serdeptid[i];
              servicedeptBean.set("serv_dept_id", Integer.parseInt(servdeptid));
              servicedeptBean.set("emp_username", name);
              success = userservicedeptDAO.insert(con, servicedeptBean);
            }
          }
        } finally {
          DataBaseUtil.commitClose(con, success);
        }
      }

      user = usertheatreDAO.exist("emp_username", name);
      if (theatreid != null || user) {
        try {
          con = DataBaseUtil.getConnection();
          con.setAutoCommit(false);
          if (user) {
            success = usertheatreDAO.delete(con, "emp_username", name);
          }
          if (theatreid != null) {
            for (int i = 0; i < theatreid.length; i++) {
              usertheatreBean = usertheatreDAO.getBean();
              String usertheatreid = theatreid[i];
              boolean defaultTheatre = false;
              if (usertheatreid.equals(defaultTheateId)) {
                defaultTheatre = true;
              }
              usertheatreBean.set("theatre_id", usertheatreid);
              usertheatreBean.set("emp_username", name);
              usertheatreBean.set("default_theatre", defaultTheatre);
              theatresList.add(usertheatreBean);
            }
            success &= usertheatreDAO.insertAll(con, theatresList);
          }
        } finally {
          DataBaseUtil.commitClose(con, success);
        }
      }

      if (userForm.getOp().equalsIgnoreCase("edit")) {
        objDto.setName(userForm.getSelUserName());
        boolean isEncrypted = (Boolean) bean.get("is_encrypted");
        currentPwd = (String) bean.get("emp_password");
        if (isEncrypted && (ssoOnlyUser || !currentPwd.equals(password))) {
          password = PasswordEncoder.encode(password);
        }
        objDto.setPassword(password);
        objDto.setEncryptAlgo("BCRYPT");
        User existingUser = new UserDAO(con).getUser(name);
        resultMap = userBO.modifyUser(objDto);
        if (!(Boolean) resultMap.get("isSuccess")) {
          flash.put("error", resultMap.get("message"));
          redirect.addParameter("userName", objDto.getName());
          redirect.addParameter("roleId", objDto.getRoleId());
        } else {
          if (existingUser.getUserCenter() != objDto.getUserCenter()) {
            counterMappingService.deleteMappedCounter(name);
          }
        }
      }
      // Make confidentiality group associations
      ArrayList<Integer> integerConfidentialityGroupIds = new ArrayList<>();
      if (null != confidentialityGroupIds) {
        for (int i = 0; i < confidentialityGroupIds.length; i++) {
          integerConfidentialityGroupIds.add(Integer.parseInt(confidentialityGroupIds[i]));
        }
      }
      userConfidentialityAssociationService.update(objDto.getName(),
          integerConfidentialityGroupIds);
      userForm.reset(mapping, request);
      setAttributes(request);
      if (!(Boolean) resultMap.get("isSuccess") && !(Boolean) success) {
        flash.put("error", resultMap.get("message"));
      }
      if ((Boolean) resultMap.get("isSuccess")) {
        redirect.addParameter("userName", objDto.getName());
        redirect.addParameter("roleId", objDto.getRoleId());
      }
    } else {
      flash.put("error", message);
      redirect.addParameter("userName", objDto.getFullname());
    }
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;
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
          for (char specialChar : specialChars) {
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
      BasicDynaBean userBean = userDao.getUserBean(userName);
      for (BasicDynaBean bean : list) {
        String prevPassword = (String) bean.get("prev_password");
        if (PasswordEncoder.matches(password, prevPassword, userBean)) {
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
   * Sets the attributes.
   *
   * @param request
   *          the new attributes
   * @throws SQLException
   *           the SQL exception
   */
  /*
   * Saves common attributes in the request for the JSP page to refer to and build the forms
   * correctly
   */
  private void setAttributes(HttpServletRequest request) throws SQLException {
    RoleBO roleBO = new RoleBO();
    UserBO userBO = new UserBO();
    JSONSerializer js = new JSONSerializer().exclude("class");

    List rolesList = roleBO.getAllRoles();
    request.setAttribute("reqRolesList", rolesList);

    List usersList = userBO.getAllUsers();
    request.setAttribute("reqUsersList", usersList);

    List objListCounters = counterDao.getCountersList();
    request.setAttribute("billing_counters",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(objListCounters)));

    List objPharmacyListCounters = counterDao.getCountersPharmacyList();
    request.setAttribute("pharmacy_counters",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(objPharmacyListCounters)));

    List objPharmacyStores = userBO.getpharmacyStoresList();
    request.setAttribute("reqPharmacyStoreList", objPharmacyStores);
    request.setAttribute("storesJSON", js.deepSerialize(objPharmacyStores));

    List objBillingAuthorizer = userBO.getBillingAuthorizer();
    request.setAttribute("reqBillingAuthorizerList", objBillingAuthorizer);
    request.setAttribute("billingAuthorizerJSON", js.deepSerialize(objBillingAuthorizer));

    DepartmentMasterDAO dao = new DepartmentMasterDAO();
    List objSpecificationNames = DepartmentMasterDAO.getAvalDeptnames();
    request.setAttribute("reqSpecificationsList", objSpecificationNames);

    List objLabDeptNames = new DiagnosticDepartmentMasterDAO()
        .getAllDiagnosticDepartmentsInArrayList();
    request.setAttribute("reqLabDeptNamesList", objLabDeptNames);

    List objAllDeptNames = DepartmentMasterDAO.getAllDepartmentsList();
    request.setAttribute("reqAllDeptNamesList", objAllDeptNames);

    request.setAttribute("userName", request.getParameter("userName"));

    UserSignatureDAO sigDAO = new UserSignatureDAO();
    BasicDynaBean sigBean = sigDAO.getBean();
    sigDAO.loadByteaRecords(sigBean, "emp_username", request.getParameter("userName"));
    request.setAttribute("signature_username", sigBean.get("emp_username"));

    HashMap userMap = new HashMap();
    for (Iterator i = usersList.iterator(); i.hasNext();) {
      User user = (User) i.next();
      userMap.put(user.getName(), user);
    }
    request.setAttribute("userDetailsMapJSON", js.exclude("class").deepSerialize(userMap));

    UserTheatreDAO userTheatreDAO = new UserTheatreDAO();
    List<BasicDynaBean> defaultTheatreList = userTheatreDAO
        .getUserDefaultTheatre(request.getParameter("userName"));
    String defaultTheatreId = null;
    if (defaultTheatreList.size() > 0) {
      defaultTheatreId = defaultTheatreList.get(0).get("theatre_id").toString();
    }
    request.setAttribute("defaultTheatreId", defaultTheatreId);

    String user = request.getParameter("userName");
    List<BasicDynaBean> userTheatreList = theatreMasterDAO.getUserTheatres(user);
    List<String> userTheatreids = new ArrayList<>();
    for (BasicDynaBean theatres : userTheatreList) {
      userTheatreids.add((String) theatres.get("theatre_id"));
    }
    request.setAttribute("userTheatreList", js.serialize(userTheatreids));

    List theatresList = theatreMasterDAO.getTheatresList();
    request.setAttribute("reqTheatresList", theatresList);
    request.setAttribute("theatresJSON", js.deepSerialize(theatresList));

    HashMap roleMap = new HashMap();
    for (Iterator i = rolesList.iterator(); i.hasNext();) {
      Role role = (Role) i.next();
      roleMap.put(role.getRoleId(), role.getRemarks());
    }
    /* We need only the remarks attribute of the role */
    request.setAttribute("roleDetailsMapJSON", js.serialize(roleMap));

    List<BasicDynaBean> malaffiMappedHospitalRoles =
        hospitalRoleService.getMalaffiRoleMappedHospitalRoles();
    List<Integer> malaffiMappedHospitalRoleIds = new ArrayList<>();
    for (BasicDynaBean role : malaffiMappedHospitalRoles) {
      malaffiMappedHospitalRoleIds.add((int) role.get("hosp_role_id"));
    }
    request.setAttribute("malaffiMappedHospitalRoleIds",
        js.serialize(malaffiMappedHospitalRoleIds));
    UserDAO userDao = new UserDAO();
    GenericDAO passwordRulesDao = passwordRuleDAO;
    request.setAttribute("sampleCollectionCentersJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(userDao.getCollectionCenters())));
    request.setAttribute("passwordRules",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(passwordRulesDao.listAll())));
    HttpSession session = request.getSession();

    int centerId = (Integer) session.getAttribute("centerId");
    List<BasicDynaBean> collectionCenters = new GenericDAO("sample_collection_centers")
        .findAllByKey("center_id", centerId);
    request.setAttribute("collectionCenters", collectionCenters);
  }

  /**
   * View signature.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward viewSignature(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    String empUserName = request.getParameter("empUserName");
    UserSignatureDAO sigDAO = new UserSignatureDAO();
    BasicDynaBean sigBean = sigDAO.getBean();
    sigDAO.loadByteaRecords(sigBean, "emp_username", empUserName);

    OutputStream stream = response.getOutputStream();
    response.setContentType((String) sigBean.get("signature_content_type"));
    stream.write(DataBaseUtil.readInputStream((InputStream) sigBean.get("signature")));
    stream.flush();
    stream.close();

    return null;
  }

}
