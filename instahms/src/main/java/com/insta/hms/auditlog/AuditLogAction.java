package com.insta.hms.auditlog;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import flexjson.JSONSerializer;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuditLogAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(AuditLogAction.class);

  /**
   * Method to get the searchable fields and the values for those fields for a given table. Expects
   * a requuest parameter al_table and returns a JSON string that can be used in the resulting page
   * for optional search parameters.
   *
   * @param actionMapping
   *          the action mapping
   * @param form
   *          the action form
   * @param req
   *          the request
   * @param res
   *          the response
   * @return the search params
   * @throws Exception
   *           the exception
   */
  public ActionForward getSearchParams(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String auditType = actionMapping.getProperty("audit_type");
    if (logger.isDebugEnabled()) {
      logger.debug("getSearchParams: al_table=" + req.getParameter("al_table"));
    }

    String tableName = req.getParameter("al_table");
    if (null != tableName && tableName.trim().length() > 0) {

      AuditLogDesc desc = getAuditLogDescriptor(auditType, tableName);
      List fieldList = desc.getSearchableFieldList();

      Map valueMap = new LinkedHashMap();

      Iterator fieldListIter = fieldList.iterator();
      while (fieldListIter.hasNext()) {

        Map fieldEntry = (Map) fieldListIter.next();
        String fieldName = (String) fieldEntry.get("name");

        if (null != fieldName) {
          List fieldValueList = desc.getLookupValueList(fieldName, null);
          if (null != fieldValueList && !fieldValueList.isEmpty()) {
            valueMap.put(fieldName, fieldValueList);
          }
        }
      }

      Map fieldData = new HashMap();
      fieldData.put("fieldList", fieldList);
      fieldData.put("fieldValueMap", valueMap);

      JSONSerializer js = new JSONSerializer().exclude("class");
      res.setContentType("text/plain");
      res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
      js.deepSerialize(fieldData, res.getWriter());
      res.flushBuffer();

      if (logger.isDebugEnabled()) {
        logger.debug("getSearchParams: fieldData=" + fieldData);
      }
    }
    return null;
  }

  /**
   * Method to get the initial Audit Log search screen.
   *
   * @param actionMapping
   *          the action mapping
   * @param form
   *          the form
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the search screen
   * @throws Exception
   *           the exception
   */
  public ActionForward getSearchScreen(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    String auditType = actionMapping.getProperty("audit_type");
    req.setAttribute("auditType", auditType);

    Map<String, String> tableMap = getAuditLogTables(auditType, true);
    req.setAttribute("auditLogTables", tableMap);
    return actionMapping.findForward("searchScreen");
  }

  private Map<String, String> getAuditLogTables(String auditType, boolean searchableOnly)
      throws Exception {
    Map<String, String> tableMap = null;
    if (null != auditType && auditType.trim().length() > 0) {
      tableMap = AuditLogHelper.getAuditLogTables(auditType);
      if (searchableOnly) {
        List<String> removeKeys = new ArrayList<>();
        for (String tableName : tableMap.keySet()) {
          AuditLogDesc desc = getAuditLogDescriptor(auditType, tableName);
          List fieldList = desc.getSearchableFieldList();
          if (null == fieldList || fieldList.isEmpty()) {
            removeKeys.add(tableName);
          }
        }
        tableMap.keySet().removeAll(removeKeys);
      }
    }
    return tableMap;
  }

  /**
   * Method to get the Audit Log List page given the search parameters. Expects the request
   * parameter al_table to identify the audit log table against which the search has to be run.
   * Expects the request parameters to contain all the search parameters. Uses the
   * SearchQueryBuilder and Listing parameters to fetch the results. So the form fields should match
   * the database fields.
   *
   * @param actionMapping the m
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the audit log list
   * @throws Exception the exception
   */
  public ActionForward getAuditLogList(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("getAuditLogList: al_table=" + req.getParameter("al_table"));
    }

    String auditType = actionMapping.getProperty("audit_type");
    Map<String, String> tableMap = null;

    if (null != auditType && auditType.trim().length() > 0) {
      req.setAttribute("auditType", auditType);
      tableMap = getAuditLogTables(auditType, true);
      req.setAttribute("auditLogTables", tableMap);
    }

    setAuditLogData(actionMapping, req, res);
    return actionMapping.findForward("auditLogList");
  }

  /**
   * Gets the audit log details.
   *
   * @param actionMapping the action mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the audit log details
   * @throws Exception the exception
   */
  public ActionForward getAuditLogDetails(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws Exception {

    if (logger.isDebugEnabled()) {
      logger.debug("getAuditLogDetails: al_table=" + req.getParameter("al_table"));
    }

    String auditType = actionMapping.getProperty("audit_type");
    Map<String, String> tableMap = null;

    if (null != auditType && auditType.trim().length() > 0) {
      req.setAttribute("auditType", auditType);
      tableMap = getAuditLogTables(auditType, false);
      req.setAttribute("auditLogTables", tableMap);
    }

    setAuditLogData(actionMapping, req, res);
    return actionMapping.findForward("auditLogDetails");
  }

  /**
   * Sets the audit log data.
   *
   * @param actionMapping the action mapping
   * @param req the req
   * @param resp the resp
   * @throws Exception the exception
   */
  private void setAuditLogData(ActionMapping actionMapping, HttpServletRequest req,
      HttpServletResponse resp) throws Exception {

    String auditType = actionMapping.getProperty("audit_type");
    String tableName = req.getParameter("al_table");
    Map<String, String> tableMap = null;

    if (null != auditType && auditType.trim().length() > 0) {
      tableMap = AuditLogHelper.getAuditLogTables(auditType);
      // req.setAttribute("auditLogTables", tableMap);
      // req.setAttribute("auditType", auditType);
    }

    if (null != tableName && !tableName.isEmpty() && null != tableMap
        && tableMap.containsKey(tableName)) {
      AuditLogDesc desc = getAuditLogDescriptor(auditType, tableName);
      Map fieldNames = desc.getFieldNameMap();
      List<String> keyFieldList = desc.getKeyFieldList();

      Map valueMap = new LinkedHashMap();

      if (null != fieldNames) {
        for (Object field : fieldNames.keySet()) {
          String fieldName = (String) field;
          Map fieldValueMap = desc.getLookupValueMap(fieldName, null);
          if (null != fieldValueMap && !fieldValueMap.isEmpty()) {
            valueMap.put(fieldName, fieldValueMap);
          }
        }
      }

      // get all the parameters from the request which match the key fields in the table.
      Map params = req.getParameterMap();
      Map filter = new HashMap();
      String keyFieldOp = null; 
      String keyFieldType = null; 
      String keyFieldCast = null;
      for (String keyField : keyFieldList) {
        if (params.get(keyField) != null) {
          filter.put(keyField, params.get(keyField));
          keyFieldOp = keyField + "@op";
          keyFieldType = keyField + "@type";
          keyFieldCast = keyField + "@cast";

          if (null != params.get(keyFieldOp)) {
            filter.put(keyFieldOp, params.get(keyFieldOp));
          }

          if (null != params.get(keyFieldType)) {
            filter.put(keyFieldType, params.get(keyFieldType));
          }

          if (null != params.get(keyFieldCast)) {
            filter.put(keyFieldCast, params.get(keyFieldCast));
          } else {
            filter.put(keyField + "@cast", new String[] { "y" });
          }
        }
      }

      // get all the parameters in the request, which match the standard audit log fields, their
      // opeartors and types.
      String fieldOp = null; 
      String fieldType = null;
      String fieldCast = null;
      for (String auditLogField : AuditLogDesc.STD_AUDIT_LOG_FIELDS) {
        if (null != params.get(auditLogField)) {
          filter.put(auditLogField, params.get(auditLogField));
        }

        fieldOp = auditLogField + "@op";
        fieldType = auditLogField + "@type";
        fieldCast = auditLogField + "@cast";

        if (null != params.get(fieldOp)) {
          filter.put(fieldOp, params.get(fieldOp));
        }

        if (null != params.get(fieldType)) {
          filter.put(fieldType, params.get(fieldType));
        }

        if (null != params.get(fieldCast)) {
          filter.put(fieldCast, params.get(fieldCast));
        }
      }

      Map listParams = ConversionUtils.getListingParameter(params);

      AuditLogDao dao = getAuditLogDao(auditType, tableName);
      PagedList pagedList = dao.getAuditLogList(filter, listParams);

      req.setAttribute("pagedList", pagedList);
      req.setAttribute("fieldMap", fieldNames);
      req.setAttribute("valueMap", valueMap);
      req.setAttribute("keyFieldList", keyFieldList);
      req.setAttribute("aldesc", desc);
    }

  }

  /**
   * Method to get the DAO for the audit log table. The default implementation creates an instance
   * of the generic AuditLogDAO class. Sub classes can override the method, if they want to use a
   * specialized AuditLogDAO class.
   *
   * @param auditType the audit type
   * @param tableName          - Name of the audit log table
   * @return AuditLogDAO - for the specific audit log table
   * @throws Exception the exception
   */
  protected AuditLogDao getAuditLogDao(String auditType, String tableName) throws Exception {
    return new AuditLogDao(auditType, tableName);
  }

  /**
   * Method to get the meta data descriptor for the audit log data. The default implementation
   * creates and uses an instanace of the AuditLogDescProvider implementation class associated with
   * the table to get the AuditLogDesc instance. Subclasses can override the method if they want to
   * use a specialized AuditLogDesc instance.
   *
   * @param auditType the audit type
   * @param tableName the table name
   * @return AuditLogDesc
   * @throws Exception the exception
   */
  protected AuditLogDesc getAuditLogDescriptor(String auditType, String tableName)
      throws Exception {
    Map<String, String> providers = AuditLogHelper.getAuditLogDescProviders();
    String descProviderName = providers.get(tableName);
    if (null == descProviderName) {
      descProviderName = AuditLogHelper.DEFAULT_AUDITLOG_DESC_PROVIDER;
    }

    AuditLogDescProvider provider = null;
    provider = (AuditLogDescProvider) Class.forName(descProviderName).newInstance();

    return provider.getAuditLogDesc(tableName);

  }
}
