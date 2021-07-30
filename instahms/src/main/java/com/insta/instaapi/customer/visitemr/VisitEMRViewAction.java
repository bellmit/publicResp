package com.insta.instaapi.customer.visitemr;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.emr.DocHolder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRDocFilter;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.emr.EMRService;
import com.insta.hms.emr.Filter;
import com.insta.hms.emr.FilterFactory;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DocumentTypeMaster.DocumentTypeMasterDAO;
import com.insta.instaapi.common.ApiUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.instaapi.common.ScreenRights;
import com.insta.instaapi.common.ServletContextUtil;

import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.EnumUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VisitEMRViewAction extends DispatchAction {

  static Logger logger = LoggerFactory.getLogger(VisitEMRViewAction.class);
  private static final VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();
  private static final JSONSerializer js = JsonProcessor.getJSONParser();
  private static final CenterMasterDAO centerDao = new CenterMasterDAO();
  private static final DocumentTypeMasterDAO documentTypeDao = new DocumentTypeMasterDAO();

  /**
   * Get Visit EMR.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception exception
   */
  public ActionForward getVisitEMR(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    logger.info("getting visit emr reports");

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map<String,Object> sessionParameters = null;
    String successMsg = "";

    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    Map<String, Object> visitDetailsDataMap = new HashMap<>();

    if (!isAValidRequest) {
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");

      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      visitDetailsDataMap.put("return_code", "1001");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Visit EMR Reports List screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "visitEMRReports");
    if (!isScreenRights) {
      Map<String,Object> labReportsData = new HashMap<>();
      successMsg = "Permission Denied. Please check with Administrator.";
      labReportsData.put("return_code", "1003");
      labReportsData.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(labReportsData));
      response.flushBuffer();
      return null;
    }

    String patientId = request.getParameter("visitId");
    String mrNo = request.getParameter("mr_no");
    boolean isPatientLogin = (boolean) sessionParameters.get("patient_login");

    if (isPatientLogin) {
      mrNo = (String) sessionParameters.get("customer_user_id");
    }

    if ((patientId == null || patientId.isEmpty()) && (mrNo == null || mrNo.isEmpty())) {
      successMsg = "Mandatory fields are not supplied";
      visitDetailsDataMap.put("return_code", "1002");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    
    String schema = (String) sessionParameters.get("hospital_name");
    
    // set connection details with default values.
    RequestContext.setConnectionDetails(new String[] { "", "", schema , "", "0" });

    BasicDynaBean visitBean = null;
    if (patientId != null) {
      Map<String, Object> identifiers = new HashMap<>();
      identifiers.put("patient_id", patientId);
      if (isPatientLogin) {
        identifiers.put("mr_no", mrNo);  
      }
      visitBean = visitDetailsDao.findByKey(identifiers);
    }

    if (patientId != null && visitBean == null) {
      successMsg = "Invalid input parameters supplied";
      visitDetailsDataMap.put("return_code", "1021");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    if (mrNo != null && new PatientDetailsDAO().findByKey("mr_no", mrNo) == null) {
      successMsg = "Invalid input parameters supplied for mr_no";
      visitDetailsDataMap.put("return_code", "1021");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    BasicDynaBean centerBean = null;
    String userName = (String) sessionParameters.get("customer_user_id");
    String roleId = null;

    if ((boolean) sessionParameters.get("patient_login")) {
      centerBean = centerDao.findByKey("center_id", 0);
      roleId = "-1"; //Hack to skip role checks for patient in 
    } else {
      GenericDAO userDAO = new GenericDAO("u_user");
      BasicDynaBean userBean = userDAO.findByKey("emp_username", userName);
      roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
      centerBean = centerDao.findByKey("center_id", userBean.get("center_id"));
    }

    // set connection details with the userid and centerid
    RequestContext.setConnectionDetails(new String[] { "", "", schema, userName,
        String.valueOf((Integer) centerBean.get("center_id")), 
        (String) centerBean.get("center_name"), roleId });

    Map<String, Object> responseData = new HashMap<String, Object>();
    String returnCode = "";
    // EMRHelper emrHelper = new EMRHelper();
    EMRDocFilter docFilter = new EMRDocFilter();
    List<EMRDoc> allDocs = new ArrayList<EMRDoc>();
    Set<String> providerNames = new HashSet<>();
    boolean getVisitLevelDocs = patientId != null;
    Map map = DocumentTypeMasterDAO.getPatientShareableDocTypes();
    Set<String> docTypes = map.keySet();
    List<EMRDoc> list = new ArrayList<EMRDoc>();
    for (String s : docTypes) {
      if (EnumUtils.isValidEnum(DocTypesProvider.class, s)) {
        providerNames.addAll(DocTypesProvider.valueOf(s).getProviders());
      }
    }
    for (String provider : providerNames) {
      if (getVisitLevelDocs) {
        list = EMRInterface.Provider.valueOf(provider).getProviderImpl()
            .listDocumentsByVisit(patientId);
      } else {
        list = EMRInterface.Provider.valueOf(provider).getProviderImpl().listDocumentsByMrno(mrNo);
      }
      if (list != null && !list.isEmpty()) {
        allDocs = docFilter.applyFilter(allDocs, list, request, true);
      }
    }
    for (EMRInterface.Provider provider : EMRInterface.Provider.values()) {
      if (provider.getProviderName().equalsIgnoreCase("GenericDocumentsProvider")
          || provider.getProviderName().equalsIgnoreCase("GenericInstaFormProvider")
          || provider.getProviderName().equalsIgnoreCase("OphthalmologyProvider")
          || provider.getProviderName().equalsIgnoreCase("PlanCardProvider")
          || provider.getProviderName().equalsIgnoreCase("CorporateCardProvider")
          || provider.getProviderName().equalsIgnoreCase("NationalCardProvider")
          || provider.getProviderName().equalsIgnoreCase("MLCFormProvider")) {
        if (getVisitLevelDocs) {
          list = provider.getProviderImpl().listDocumentsByVisit(patientId);
        } else {
          list = provider.getProviderImpl().listDocumentsByMrno(mrNo);
        }
        if (list != null && !list.isEmpty()) {
          /*
           * allDocs = emrHelper.applyFilter(allDocs, list, (String) userBean.get("emp_username"),
           * (Integer) userBean.get("center_id"), ((BigDecimal)
           * userBean.get("role_id")).intValue());
           */
          allDocs = docFilter.applyFilter(allDocs, list, request, true);
        }
      }
    }
    boolean success = false;
    logger.info("got all visit related emr data...");
    success = true;
    if (success) {
      responseData.put("return_message", "Success");
      returnCode = "2001";
    } else {
      responseData.put("return_message", "fail to get emr data.");
      returnCode = "1022";
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    responseData.put("return_code", returnCode);
    String logoHeader = request.getParameter("logoHeader");
    List filteredDocs = getDocs(allDocs, requestHandalerKey, logoHeader);
    if (null != filteredDocs && !filteredDocs.isEmpty()) {
      responseData.put("patient_visit_emr_documents", filteredDocs.get(0));
    } else {
      responseData.put("patient_visit_emr_documents", filteredDocs);
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    // response.setHeader("Access-Control-Allow-Origin", "*");
    response.getWriter().write(js.deepSerialize(responseData));
    response.flushBuffer();

    return null;
  }

  /**
   * Get Visit EMR for Patient.
   * 
   * @param mapping  Action Mapping
   * @param form     Action Form
   * @param request  Servlet Request Object
   * @param response Servlet Response Object
   * @return Response
   * @throws Exception exception
   */
  public ActionForward getVisitEMRForPatient(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    logger.info("getting visit emr reports sharable to patient");

    String requestHandalerKey = ApiUtil.getRequestKey(request);
    boolean isAValidRequest = false;
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    logger.info("getting session related data from conetxt" + sessionMap);
    Map<String, Object> sessionParameters = null;
    String successMsg = "";

    java.sql.Timestamp loginTime = null;
    MessageResources msgResource = getResources(request);
    String tokenValidation = msgResource.getMessage("token.validation.duration");
    int validDuration = Integer.parseInt(tokenValidation);
    // getting current time
    Calendar calendar = Calendar.getInstance();
    java.util.Date now = calendar.getTime();
    java.sql.Timestamp currentTime = new java.sql.Timestamp(now.getTime());
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(requestHandalerKey);
      if (sessionParameters != null && !sessionParameters.isEmpty()) {
        // getting login time
        loginTime = (java.sql.Timestamp) sessionParameters.get("login_time");
        isAValidRequest = (currentTime.getTime() - loginTime.getTime()) / 60000 <= validDuration;
      }
    }

    Map<String, Object> visitDetailsDataMap = new HashMap<String, Object>();

    if (!isAValidRequest) {
      response.setContentType("application/json");
      response.setHeader("Cache-Control", "no-cache");
      // response.setHeader("Access-Control-Allow-Origin", "*");

      successMsg = "invalid request token,please login again";
      logger.info("invalid request token,please login again");
      logger.info("sending the response back to the requesting server");
      visitDetailsDataMap.put("return_code", "1001");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    // Check the rights for Visit EMR Reports List screen
    boolean isScreenRights = ScreenRights.getScreenRights(requestHandalerKey, ctx,
        "visitEMRReports");
    if (!isScreenRights) {
      Map<String, Object> labReportsData = new HashMap<>();
      successMsg = "Permission Denied. Please check with Administrator.";
      labReportsData.put("return_code", "1003");
      labReportsData.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(labReportsData));
      response.flushBuffer();
      return null;
    }

    String patientId = request.getParameter("visit_id");
    if (patientId == null || patientId.isEmpty()) {
      successMsg = "Mandatory fields are not supplied";
      visitDetailsDataMap.put("return_code", "1002");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }
    // set connection details with default values.
    RequestContext.setConnectionDetails(
        new String[] { "", "", (String) sessionParameters.get("hospital_name"), "", "0" });

    if (!visitDetailsDao.exist("patient_id", patientId)) {
      successMsg = "Invalid input parameters supplied";
      visitDetailsDataMap.put("return_code", "1021");
      visitDetailsDataMap.put("return_message", successMsg);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write(js.deepSerialize(visitDetailsDataMap));
      response.flushBuffer();
      return null;
    }

    BasicDynaBean centerBean = null;
    String userName = (String) sessionParameters.get("customer_user_id");
    String roleId = null;

    if ((boolean) sessionParameters.get("patient_login")) {
      centerBean = centerDao.findByKey("center_id", 0);
      roleId = "-1"; //Hack to skip role checks for patient in 
    } else {
      List<String> columns = new ArrayList<String>();
      columns.add("center_id");
      columns.add("emp_username");
      columns.add("role_id");
      Map<String, Object> identifiers = new HashMap<String, Object>();
      identifiers.put("emp_username", userName);
      GenericDAO userDAO = new GenericDAO("u_user");
      BasicDynaBean userBean = userDAO.findByKey(columns, identifiers);
      centerBean = centerDao.findByKey("center_id",
          userBean.get("center_id"));
      roleId = String.valueOf(((BigDecimal) userBean.get("role_id")));
    }

    // set connection details with the userid and centerid
    RequestContext.setConnectionDetails(new String[] { "", "",
        (String) sessionParameters.get("hospital_name"), userName,
        String.valueOf((Integer) centerBean.get("center_id")), 
        (String) centerBean.get("center_name"),
        roleId });

    Map<String, Object> responseData = new HashMap<String, Object>();
    EMRDocFilter docFilter = new EMRDocFilter();
    List<EMRDoc> allDocs = new ArrayList<EMRDoc>();

    List<EMRDoc> listDoc = EMRService.getPatientViewEMR(patientId);
    if (listDoc != null && !listDoc.isEmpty()) {
      allDocs = docFilter.applyFilter(allDocs, listDoc, request, true);
    }

    logger.info("got all visit related emr data...");
    responseData.put("return_message", "Success");
    responseData.put("return_code", "2001");
    String logoHeader = request.getParameter("logoHeader");
    List filteredDocs = getDocs(allDocs, requestHandalerKey, logoHeader);
    if (null != filteredDocs && !filteredDocs.isEmpty()) {
      responseData.put("patient_visit_emr_documents", filteredDocs.get(0));
    } else {
      responseData.put("patient_visit_emr_documents", filteredDocs);
    }
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(js.deepSerialize(responseData));
    response.flushBuffer();

    return null;
  }

  /**
   * Get list of EMR documents.
   * 
   * @param allDocs            List of all documents
   * @param requestHandalerKey Request handler key
   * @param logoHeader         Logo header
   * @return List of document
   * @throws ParseException Parse Exception
   */
  public List getDocs(List<EMRDoc> allDocs, String requestHandalerKey, String logoHeader)
      throws ParseException {
    String logoHeaderParam = "";

    if (logoHeader != null && !logoHeader.equals("")
        && (logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
            || logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
      // override the priter preferences with this value.
      logoHeaderParam = "&logoHeader=" + logoHeader.toUpperCase();
    } else {
      logoHeaderParam = "";
    }
    Filter filter = FilterFactory.getFilter("patientDocType");
    if (!allDocs.isEmpty()) {
      allDocs = filter.applyFilter(allDocs, "");
    }
    String filterType = "visits";
    filter = FilterFactory.getFilter(filterType);
    List filteredDocs = Collections.EMPTY_LIST;
    if (!allDocs.isEmpty()) {
      filteredDocs = filter.applyFilter(allDocs, "");
    }
    if (!filteredDocs.isEmpty()) {
      Iterator<ArrayList> it = filteredDocs.iterator();
      while (it.hasNext()) {
        List docHolderList = (ArrayList) it.next();
        for (int i = 0; i < docHolderList.size(); i++) {
          DocHolder docHolder = (DocHolder) docHolderList.get(i);
          String label = docHolder.getLabel();
          if (label != null && !label.equals("")) {
            Date date = DateUtil.parseDate(label.substring(0, label.indexOf(" ")));
            String str = DateUtil.formatIso8601Date(date) + " "
                + label.substring(label.indexOf(" "));
            docHolder.setLabel(str);
          }
          List<EMRDoc> list = docHolder.getViewDocs();
          Iterator<EMRDoc> iter = list.iterator();
          if (list != null && list.size() > 0) {
            List newList = new ArrayList();
            while (iter.hasNext()) {
              EMRDoc doc = iter.next();
              if (!doc.isAuthorized()) {
                continue;
              }
              Map temp = new HashMap();
              temp.put("annotation", doc.getAnotation());
              temp.put("contentType", doc.getContentType());
              temp.put("description", doc.getDescription());
              if (doc.getType().equalsIgnoreCase("SYS_CONSULT")
                  || doc.getType().equalsIgnoreCase("SYS_TRIAGE")) {
                temp.put("displayUrl", "/api" + doc.getDisplayUrl() + "&request_handler_key="
                    + requestHandalerKey + logoHeaderParam);
              } else {
                temp.put("displayUrl", doc.getDisplayUrl() + "&request_handler_key="
                    + requestHandalerKey + logoHeaderParam);
              }
              temp.put("docid", doc.getDocid());
              temp.put("doctor", doc.getDoctor());
              temp.put("pdfSupported", doc.isPdfSupported());
              if (doc.getDate() != null) {
                temp.put("date",
                    DateUtil.formatIso8601Timestamp((new java.sql.Date(doc.getDate().getTime()))));
              }
              if (doc.getProvider() != null) {
                temp.put("provider", doc.getProvider().getProviderName());
              }

              temp.put("title", doc.getTitle());
              temp.put("type", doc.getType());
              if (doc.getVisitDate() != null) {
                temp.put("visitDate", DateUtil
                    .formatIso8601Timestamp(new java.sql.Date(doc.getVisitDate().getTime())));
              }
              temp.put("visitid", doc.getVisitid());
              newList.add(temp);
            }
            docHolder.setViewDocs(newList);
          }
        }
      }
    }
    return filteredDocs;
  }

}
