package com.insta.hms.emraccess;

import com.insta.hms.emr.EMRDoc;
import com.insta.hms.master.EMRAccessRight.EMRAccessRightDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The Class EMRAccessRuleProcessor.
 */
public class EMRAccessRuleProcessor {
  static Logger log = LoggerFactory.getLogger(EMRAccessRuleProcessor.class);
  private static EMRAccessRightDAO accessRightDAO = new EMRAccessRightDAO();

  /**
   * Gets the doc rule bean.
   *
   * @param doc
   *          the doc
   * @param docAttrs
   *          the doc attrs
   * @return the doc rule bean
   * @throws SQLException
   *           the SQL exception
   */
  private BasicDynaBean getDocRuleBean(EMRDoc doc, Map docAttrs) throws SQLException {
    String docDeptId = "";
    if (docAttrs != null) {
      if ((null != docAttrs.get("dept_id")) && (!"".equals(docAttrs.get("dept_id")))) {
        docDeptId = (String) docAttrs.get("dept_id");
      }
    }
    BasicDynaBean docRuleBean = accessRightDAO.getRuleDetailsBasedonDocType(doc, docDeptId);
    return docRuleBean;
  }

  /**
   * Check doc centers access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param docRuleBean
   *          the doc rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkDocCentersAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean docRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkDocCentersAccess::roleId=" + roleId + " docTypeId="
        + doc.getType());
    // EMRAccessRightDAO accessRightDAO= new EMRAccessRightDAO();
    // check the center_access from the bean
    boolean accessRight = false;
    String userCenterId = "";
    String docCenterId = "";
    String docDeptId = "";
    try {
      // Map userAttrs = accessRightDAO.getUserAttributesMap(userId, centerId, roleId);
      if (userAttrs != null) {
        if ((null != userAttrs.get("center_id"))
            && (!"".equals(String.valueOf(userAttrs.get("center_id"))))) {
          userCenterId = String.valueOf(userAttrs.get("center_id"));
        }
      }
      // Map docAttrs = accessRightDAO.getDocumentAttributesMap(doc, userId, centerId, roleId);
      if (docAttrs != null) {
        if ((null != docAttrs.get("center_id"))
            && (!"".equals(String.valueOf(docAttrs.get("center_id"))))) {
          docCenterId = String.valueOf(docAttrs.get("center_id"));
        }
        if ((null != docAttrs.get("dept_id")) && (!"".equals(docAttrs.get("dept_id")))) {
          docDeptId = (String) docAttrs.get("dept_id");
        }
      }
      // BasicDynaBean docRuleBean = accessRightDAO.getRuleDetailsBasedonDocType(doc,docDeptId);
      if (null == docRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkDocCentersAccess::center_access="
            + docRuleBean.get("center_access") + " rule_id=" + docRuleBean.get("rule_id"));
        if ("0".equals(docRuleBean.get("center_access"))) {
          return true;
        }
        // RC : if docCenter or userCenter = 0 then allow access
        if ("0".equals(docCenterId)) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any centers
        if ("3".equals(docRuleBean.get("center_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        if ("2".equals(docRuleBean.get("center_access"))) {
          // RC : If docCenterId is null or empty return true
          log.debug(":==docCenterId==>" + docCenterId + " :==userCenterId==>" + userCenterId);
          if ("".equals(docCenterId)) {
            return true;
          }
          if (userCenterId.equals(docCenterId)) { // same user
            accessRight = true;
          }
        }
        // if it is 1 - then
        if ("1".equals(docRuleBean.get("center_access"))) {
          List<BasicDynaBean> allowedCenters = accessRightDAO
              .getAllowedCenters((String) docRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedCenters) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase(userCenterId)) {
              log.debug("EMRAccessRuleProcessor:checkDocCentersAccess::allowedCenters=" + entityId
                  + " userCenterId=" + userCenterId);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Check doc departments access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param docRuleBean
   *          the doc rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkDocDepartmentsAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean docRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkDocDepartmentsAccess::docTypeId=" + doc.getType());
    // check the center_access from the bean
    boolean accessRight = false;
    String userDepartmentId = "";
    String docDepartmentId = "";
    try {
      // Map userAttrs = accessRightDAO.getUserAttributesMap(userId,centerId, roleId);
      if (userAttrs != null) {
        if ((null != userAttrs.get("dept_id")) && (!"".equals(userAttrs.get("dept_id")))) {
          userDepartmentId = (String) userAttrs.get("dept_id");
        }
      }
      // Map docAttrs = accessRightDAO.getDocumentAttributesMap(doc, userId, centerId, roleId);
      if (docAttrs != null) {
        if ((null != docAttrs.get("dept_id")) && (!"".equals(docAttrs.get("dept_id")))) {
          docDepartmentId = (String) docAttrs.get("dept_id");
        }
      }

      // BasicDynaBean docRuleBean =
      // accessRightDAO.getRuleDetailsBasedonDocType(doc,docDepartmentId);
      if (null == docRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkDocDepartmentsAccess::dept_access="
            + docRuleBean.get("dept_access") + " rule_id=" + docRuleBean.get("rule_id"));
        if ("0".equals(docRuleBean.get("dept_access"))) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any Department
        if ("3".equals(docRuleBean.get("dept_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        // RC : same as center check
        if ("2".equals(docRuleBean.get("dept_access"))) {
          log.debug(":==docDepartmentId==>" + docDepartmentId + " :==userDepartmentId==>"
              + userDepartmentId);
          if (null == docDepartmentId || "".equals(docDepartmentId)) {
            return true;
          }
          if (userDepartmentId.equals(docDepartmentId)) { // same user
            accessRight = true;
          }

        }
        // if it is 1 - then
        if ("1".equals(docRuleBean.get("dept_access"))) {
          List<BasicDynaBean> allowedDepartments = accessRightDAO
              .getAllowedDepartments((String) docRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedDepartments) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase(userDepartmentId)) {
              log.debug("EMRAccessRuleProcessor:checkDocDepartmentsAccess::allowedDepartments="
                  + entityId + " userDepartmentId=" + userDepartmentId);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Check doc role access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param docRuleBean
   *          the doc rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkDocRoleAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean docRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkDocRoleAccess::docTypeId=" + doc.getType());
    // check the center_access from the bean
    boolean accessRight = false;
    String userRoleId = "";
    String docRoleId = "";
    String docDeptId = "";
    try {
      // Map userAttrs = accessRightDAO.getUserAttributesMap(userId, centerId, roleId);
      if (userAttrs != null) {
        if ((null != userAttrs.get("role_id"))
            && (!"".equals(String.valueOf(userAttrs.get("role_id"))))) {
          userRoleId = String.valueOf(userAttrs.get("role_id"));
        }
      }
      // Map docAttrs = accessRightDAO.getDocumentAttributesMap(doc, userId, centerId, roleId);
      if (docAttrs != null) {
        if ((null != docAttrs.get("role_id"))
            && (!"".equals(String.valueOf(docAttrs.get("role_id"))))) {
          docRoleId = String.valueOf(docAttrs.get("role_id"));
        }
        if ((null != docAttrs.get("dept_id")) && (!"".equals(docAttrs.get("dept_id")))) {
          docDeptId = (String) docAttrs.get("dept_id");
        }
      }
      if (roleId == 1 || roleId == 2) {
        return true;
      }
      // BasicDynaBean docRuleBean = accessRightDAO.getRuleDetailsBasedonDocType(doc,docDeptId);
      if (null == docRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkDocRoleAccess::role_access="
            + docRuleBean.get("role_access") + " rule_id=" + docRuleBean.get("rule_id"));
        if ("0".equals(docRuleBean.get("role_access"))) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any Role
        if ("3".equals(docRuleBean.get("role_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        // RC : same as center check
        if ("2".equals(docRuleBean.get("role_access"))) {
          String ruleRoleId = (String) docRuleBean.get("role_id");
          if (!ruleRoleId.equals(String.valueOf(roleId))) {
            return false;
          }
          log.debug(":==docRoleId==>" + docRoleId + " :==userRoleId==>" + userRoleId);
          if ("".equals(docRoleId)) {
            return true;
          }
          if (userRoleId.equals(docRoleId)) { // same user
            accessRight = true;
          }
        }
        // if it is 1 - then
        if ("1".equals(docRuleBean.get("role_access"))) {
          List<BasicDynaBean> allowedRoles = accessRightDAO
              .getAllowedRoles((String) docRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedRoles) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase(userRoleId)) {
              log.debug("EMRAccessRuleProcessor:checkDocRoleAccess::allowedRoles=" + entityId
                  + " userRoleId=" + userRoleId);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Check doc user access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param docRuleBean
   *          the doc rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkDocUserAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean docRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkDocUserAccess::docTypeId=" + doc.getType());
    // check the center_access from the bean
    boolean accessRight = false;
    String username = "";
    String docUsername = "";
    String docDeptId = "";
    try {
      // Map userAttrs = accessRightDAO.getUserAttributesMap(userId, centerId, roleId);
      if (userAttrs != null) {
        if ((null != userAttrs.get("user_id")) && (!"".equals(userAttrs.get("user_id")))) {
          username = (String) userAttrs.get("user_id");
        }
      }
      // Map docAttrs = accessRightDAO.getDocumentAttributesMap(doc, userId, centerId, roleId);
      if (docAttrs != null) {
        if ((null != docAttrs.get("user_id")) && (!"".equals(docAttrs.get("user_id")))) {
          docUsername = (String) docAttrs.get("user_id");
        }
        if ((null != docAttrs.get("dept_id")) && (!"".equals(docAttrs.get("dept_id")))) {
          docDeptId = (String) docAttrs.get("dept_id");
        }
      }

      // BasicDynaBean docRuleBean = accessRightDAO.getRuleDetailsBasedonDocType(doc,docDeptId);
      if (null == docRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkDocUserAccess::user_access="
            + docRuleBean.get("user_access") + " rule_id=" + docRuleBean.get("rule_id"));
        if ("0".equals(docRuleBean.get("user_access"))) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any Users
        if ("3".equals(docRuleBean.get("user_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        // check for docUserName is null - same as center check
        if ("2".equals(docRuleBean.get("user_access"))) {
          log.debug(":==docUsername==>" + docUsername + "::::::::::::::==username==>" + username);
          if (null == docUsername || "".equals(docUsername)) {
            return true;
          }
          if (username.equals(docUsername)) { // same user
            accessRight = true;
          }
        }
        // if it is 1 - then
        if ("1".equals(docRuleBean.get("user_access"))) {
          List<BasicDynaBean> allowedUsers = accessRightDAO
              .getAllowedUsers((String) docRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedUsers) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase(username)) {
              log.debug("EMRAccessRuleProcessor:checkDocUserAccess::allowedUsers=" + entityId
                  + " username=" + username);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Check role centers access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param roleRuleBean
   *          the role rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkRoleCentersAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean roleRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkRoleCentersAccess::docTypeId=" + doc.getType());
    // check the center_access from the bean
    boolean accessRight = false;
    String userCenterId = "";
    String docCenterId = "";
    try {
      // Map userAttrs = accessRightDAO.getUserAttributesMap(userId, centerId, roleId);
      if (userAttrs != null) {
        if ((null != userAttrs.get("center_id"))
            && (!"".equals(String.valueOf(userAttrs.get("center_id"))))) {
          userCenterId = String.valueOf(userAttrs.get("center_id"));
        }
      }
      // Map docAttrs = accessRightDAO.getDocumentAttributesMap(doc, userId, centerId, roleId);
      if (docAttrs != null) {
        if ((null != docAttrs.get("center_id"))
            && (!"".equals(String.valueOf(docAttrs.get("center_id"))))) {
          docCenterId = String.valueOf(docAttrs.get("center_id"));
        }
      }

      if (null == roleRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkRoleCentersAccess::center_access="
            + roleRuleBean.get("center_access") + " rule_id=" + roleRuleBean.get("rule_id"));
        if ("0".equals(roleRuleBean.get("center_access"))) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any centers
        if ("3".equals(roleRuleBean.get("center_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        if ("2".equals(roleRuleBean.get("center_access"))) {
          log.debug(":==docCenterId==>" + docCenterId + " :==userCenterId==>" + userCenterId);
          if (userCenterId.equals(docCenterId)) { // same user
            accessRight = true;
          }
        }
        // if it is 1 - then
        if ("1".equals(roleRuleBean.get("center_access"))) {
          List<BasicDynaBean> allowedCenters = accessRightDAO
              .getAllowedCenters((String) roleRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedCenters) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase(docCenterId)) {
              log.debug("EMRAccessRuleProcessor:checkRoleCentersAccess::allowedCenters=" + entityId
                  + " docCenterId=" + docCenterId);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Check role departments access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param roleRuleBean
   *          the role rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkRoleDepartmentsAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean roleRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkRoleDepartmentsAccess::docTypeId=" + doc.getType());
    // check the center_access from the bean
    boolean accessRight = false;
    String userDepartmentId = "";
    String docDepartmentId = "";
    try {
      if (userAttrs != null) {
        if ((null != (String) userAttrs.get("dept_id"))
            && (!"".equals((String) userAttrs.get("dept_id")))) {
          userDepartmentId = (String) userAttrs.get("dept_id");
        }
      }
      if (docAttrs != null) {
        if ((null != (String) docAttrs.get("dept_id"))
            && (!"".equals((String) docAttrs.get("dept_id")))) {
          docDepartmentId = (String) docAttrs.get("dept_id");
        }
      }
      if (null == roleRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkRoleDepartmentsAccess::dept_access="
            + roleRuleBean.get("dept_access") + " rule_id=" + roleRuleBean.get("rule_id"));
        if ("0".equals(roleRuleBean.get("dept_access"))) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any Departments
        if ("3".equals(roleRuleBean.get("dept_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        if ("2".equals(roleRuleBean.get("dept_access"))) {
          log.debug(":==docDepartmentId==>" + docDepartmentId + " :==userDepartmentId==>"
              + userDepartmentId);
          if (userDepartmentId.equals((String) docDepartmentId)) { // same user
            accessRight = true;
          }
        }
        // if it is 1 - then
        if ("1".equals(roleRuleBean.get("dept_access"))) {
          List<BasicDynaBean> allowedDepartments = accessRightDAO
              .getAllowedDepartments((String) roleRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedDepartments) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase((String) docDepartmentId)) {
              log.debug("EMRAccessRuleProcessor:checkRoleDepartmentsAccess::allowedDepartments="
                  + entityId + " docDepartmentId=" + docDepartmentId);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Check role document access.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @param roleRuleBean
   *          the role rule bean
   * @return true, if successful
   * @throws Exception
   *           the exception
   */
  private boolean checkRoleDocumentAccess(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId,
      BasicDynaBean roleRuleBean) throws Exception {
    log.debug("EMRAccessRuleProcessor:checkRoleDocumentAccess::docTypeId=" + doc.getType());
    // check the center_access from the bean
    boolean accessRight = false;
    String docTypeId = doc.getType();
    String docUsername = "";
    String username = "";
    try {
      if (userAttrs != null) {
        if ((null != (String) userAttrs.get("user_id"))
            && (!"".equals((String) userAttrs.get("user_id")))) {
          username = (String) userAttrs.get("user_id");
        }
      }
      if (docAttrs != null) {
        if ((null != (String) docAttrs.get("user_id"))
            && (!"".equals((String) docAttrs.get("user_id")))) {
          docUsername = (String) docAttrs.get("user_id");
        }
      }
      if (null == roleRuleBean) {
        return true;
      } else {
        log.debug("EMRAccessRuleProcessor:checkRoleDocumentAccess::doc_access="
            + roleRuleBean.get("doc_access") + " rule_id=" + roleRuleBean.get("rule_id"));
        if ("0".equals(roleRuleBean.get("doc_access"))) {
          return true;
        }
        // if it is 3 - no further check required, return true // Any Documents
        if ("3".equals(roleRuleBean.get("doc_access"))) {
          accessRight = true;
        }
        // if it is 2 - then
        if ("2".equals(roleRuleBean.get("doc_access"))) {
          log.debug(":==docUsername==>" + docUsername + " :==username==>" + username);
          if (null == docUsername || "".equals(docUsername)) {
            return true;
          }
          if (username.equals(docUsername)) { // same user
            accessRight = true;
          }
        }
        // if it is 1 - then
        if ("1".equals(roleRuleBean.get("doc_access"))) {
          List<BasicDynaBean> allowedDocuments = accessRightDAO
              .getAllowedDocumentTypes((String) roleRuleBean.get("rule_id"));
          String entityId = "";
          for (BasicDynaBean bean : allowedDocuments) {
            entityId = (String) bean.get("entity_id");
            if (entityId.equalsIgnoreCase(docTypeId)) {
              log.debug("EMRAccessRuleProcessor:checkRoleDocumentAccess::allowedDocuments="
                  + entityId + " docTypeId=" + docTypeId);
              accessRight = true;
            }
          }
        }
      }

    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Doc access check.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @return true, if successful
   */
  private boolean docAccessCheck(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId) {
    boolean accessRight = false;
    try {

      if (roleId == 1 || roleId == 2) {
        return true;
      }
      BasicDynaBean ruleBean = getDocRuleBean(doc, docAttrs);
      boolean checkDocCentersAccess = checkDocCentersAccess(doc, userAttrs, docAttrs, roleId,
          ruleBean);
      if (!checkDocCentersAccess) {
        return false;
      }

      boolean checkDocDepartmentsAccess = checkDocDepartmentsAccess(doc, userAttrs, docAttrs,
          roleId, ruleBean);
      if (!checkDocDepartmentsAccess) {
        return false;
      }
      boolean checkDocRoleAccess = checkDocRoleAccess(doc, userAttrs, docAttrs, roleId, ruleBean);
      if (!checkDocRoleAccess) {
        return false;
      }

      boolean checkDocUserAccess = checkDocUserAccess(doc, userAttrs, docAttrs, roleId, ruleBean);
      if (!checkDocUserAccess) {
        return false;
      }
      log.debug("=checkDocCentersAccess=>" + checkDocCentersAccess + "=checkDocDepartmentsAccess=>"
          + checkDocDepartmentsAccess);
      log.debug("=checkDocRoleAccess=>" + checkDocRoleAccess + "=checkDocUserAccess=>"
          + checkDocUserAccess);

      accessRight = true;

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return accessRight;
  }

  /**
   * Role access check.
   *
   * @param doc
   *          the doc
   * @param userAttrs
   *          the user attrs
   * @param docAttrs
   *          the doc attrs
   * @param roleId
   *          the role id
   * @return true, if successful
   */
  private boolean roleAccessCheck(EMRDoc doc, Map userAttrs, Map docAttrs, int roleId) {
    boolean accessRight = false;
    try {
      if (roleId == 1 || roleId == 2) {
        return true;
      }

      BasicDynaBean roleRuleBean = accessRightDAO
          .getRuleDetailsBasedonRoleId(String.valueOf(roleId));

      boolean checkRoleCentersAccess = checkRoleCentersAccess(doc, userAttrs, docAttrs, roleId,
          roleRuleBean);
      if (!checkRoleCentersAccess) {
        return false;
      }

      boolean checkRoleDepartmentsAccess = checkRoleDepartmentsAccess(doc, userAttrs, docAttrs,
          roleId, roleRuleBean);
      if (!checkRoleDepartmentsAccess) {
        return false;
      }

      boolean checkRoleDocumentAccess = checkRoleDocumentAccess(doc, userAttrs, docAttrs, roleId,
          roleRuleBean);
      if (!checkRoleDocumentAccess) {
        return false;
      }

      log.debug("=checkRoleCentersAccess=>" + checkRoleCentersAccess
          + "=checkRoleDepartmentsAccess=>" + checkRoleDepartmentsAccess);
      log.debug("=checkRoleDocumentAccess=>" + checkRoleDocumentAccess);

      accessRight = true;

    } catch (Exception ex) {
      log.error("Error Processing EMR Access Rules : " + ex.getMessage());
    }

    return accessRight;
  }

  /**
   * Process rules.
   *
   * @param doc
   *          the doc
   * @param userId
   *          the user id
   * @param centerId
   *          the center id
   * @param roleId
   *          the role id
   * @param userAttrs
   *          the user attrs
   * @param checkRoleAccess
   *          the check role access
   * @param checkDocAccess
   *          the check doc access
   * @return true, if successful
   */
  public boolean processRules(EMRDoc doc, String userId, int centerId, int roleId, Map userAttrs,
      boolean checkRoleAccess, boolean checkDocAccess) {
    try {
      log.debug("getting attribute maps...");
      Map docAttrs = accessRightDAO.getDocumentAttributesMap(doc, userId, centerId, roleId);

      log.debug("checking rules...");
      boolean docAccessRight = true;
      boolean roleAccessRight = true;
      if (checkDocAccess) {
        docAccessRight = docAccessCheck(doc, userAttrs, docAttrs, roleId);
      }
      if (checkRoleAccess) {
        roleAccessRight = roleAccessCheck(doc, userAttrs, docAttrs, roleId);
      }
      return (docAccessRight && roleAccessRight);
    } catch (SQLException ex) {
      log.error("Error Processing EMR Access Rules : " + ex.getMessage());
      return false;
    }
  }

}
