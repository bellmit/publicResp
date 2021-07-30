package com.insta.instaapi.customer.visitemr;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emraccess.EMRAccessRuleProcessor;
import com.insta.hms.master.EMRAccessRight.EMRAccessRightDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EMRHelper {
  // TODO : replace this method with EMRDocFilter.
  // api should not have any business logic. modify EMRDocFilter such that it can be used from api.
  static Logger log = LoggerFactory.getLogger(EMRHelper.class);
  private EMRAccessRightDAO accessRightDAO = new EMRAccessRightDAO();

  /**
   * Filter EMR documents user has access to.
   * 
   * @param allDocs     List of all documents
   * @param accessRules List of access rules
   * @param userId      User ID
   * @param centerId    Center ID
   * @param roleId      Role ID
   * @return List of filtered documents
   * @throws ParseException parse exception
   */
  public List<EMRDoc> applyFilter(List<EMRDoc> allDocs, List accessRules, String userId,
      Integer centerId, Integer roleId) throws ParseException {

    String indocType = null;
    String exdocType = null;
    String fromDate = null;
    String toDate = null;
    boolean accessRight = true;

    String filterType = "visits";
    if (accessRules != null && !accessRules.isEmpty()) {
      Iterator<EMRDoc> it = accessRules.iterator();
      Map userAttrs = null;
      boolean checkRoleAccess = true;
      boolean checkDocAccess = true;
      Map ruleMap = null;
      try {
        userAttrs = accessRightDAO.getUserAttributesMap(userId, centerId, roleId);
        if (roleId != 1 && roleId != 2) {
          List<BasicDynaBean> rules = accessRightDAO.getRules(roleId);
          ruleMap = ConversionUtils.groupByColumn(rules, "rule_type");
          if (!ruleMap.containsKey("ROLE") || !ruleMap.containsKey("DOC")) {
            checkRoleAccess = false;
          }
        }
      } catch (SQLException ex) {
        log.error("Error Processing EMR Access Rules : " + ex.getMessage());
      }
      while (it.hasNext()) {
        EMRDoc doc = it.next();
        // docAccessRight = EMRAccessRuleProcessor.docAccessCheck(p,userId,centerId,roleId);
        // roleAccessRight = EMRAccessRuleProcessor.roleAccessCheck(p,userId,centerId,roleId);
        // accessRight = (docAccessRight && roleAccessRight)?true:false;
        log.debug("Processing EMR access rules...");
        accessRight = new EMRAccessRuleProcessor().processRules(doc, userId, centerId, roleId,
            userAttrs, checkRoleAccess, checkDocAccess);
        log.debug("Done processing EMR access rules...");

        if (!accessRight) {
          log.debug(" docType : " + doc.getType() + " title : " + doc.getTitle() + "accessRight : "
              + accessRight);
          continue;
        } else {
          if (filterType.equals("visits")) {
            if ((indocType != null && exdocType != null) || (indocType != null && exdocType == null)
                || (indocType == null && exdocType != null)) {
              if (doc.getType().equals(indocType) && exdocType == null) {
                allDocs.add(doc);
              }
              if (!doc.getType().equals(exdocType) && indocType == null) {
                allDocs.add(doc);
              }
            } else {
              allDocs.add(doc);
            }
          } else {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            if ((!fromDate.equals("") && !toDate.equals(""))
                || (fromDate.equals("") && !toDate.equals(""))
                || (!fromDate.equals("") && toDate.equals(""))) {
              if (doc.getDate() == null) {
                // skip the document. when the document date is null, but searching within a date
                // range.
                continue;
              }
              if (!toDate.equals("") && fromDate.equals("")) {
                java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
                int docDateToCompare = new Date(
                    dateformat.parse(doc.getDate().toString()).getTime()).compareTo(valtoDate);
                if (docDateToCompare == -1 || docDateToCompare == 0) {
                  allDocs.add(doc);
                }
              }
              if (!fromDate.equals("") && toDate.equals("")) {
                java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
                int docDateFromCompare = doc.getDate().compareTo(valfromDate);
                if (docDateFromCompare == 1 || docDateFromCompare == 0) {
                  allDocs.add(doc);
                }
              }
              if (!fromDate.equals("") && !toDate.equals("")) {
                java.util.Date valtoDate = DataBaseUtil.parseDate(toDate);
                java.util.Date valfromDate = DataBaseUtil.parseDate(fromDate);
                if (doc.getDate() == null) {
                  allDocs.add(doc);
                } else {
                  int docDateCompareEnd = new Date(
                      dateformat.parse(doc.getDate().toString()).getTime()).compareTo(valtoDate);
                  int docDateCompareStart = new Date(
                      dateformat.parse(doc.getDate().toString()).getTime()).compareTo(valfromDate);
                  if ((docDateCompareEnd == -1 || docDateCompareEnd == 0)
                      && (docDateCompareStart == 1 || docDateCompareStart == 0)) {
                    allDocs.add(doc);
                  }
                }
              }
            } else {
              allDocs.add(doc);
            }
          }
        }
      }
    }
    return allDocs;
  }
}
