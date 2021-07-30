/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.StringUtil;
import com.insta.hms.eservice.EResponse;
import com.insta.hms.eservice.EResponseProcessor;
import com.insta.hms.integration.insurance.InsuranceCaseDetails;
import com.insta.hms.integration.insurance.InsurancePlugin;
import com.insta.hms.integration.insurance.InsurancePluginManager;
import com.insta.hms.integration.insurance.PriorAuthDocument;
import com.insta.hms.integration.insurance.PriorAuthRequestResults;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionHelper;
import com.insta.hms.pbmauthorization.PriorAuthorizationHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class EAuthRequestsAction.
 *
 * @author lakshmi
 */
public class EAuthRequestsAction extends BaseAction {

  /**
   * The log.
   */
  static Logger log = LoggerFactory.getLogger(EAuthRequestsAction.class);

  /**
   * The reqdao.
   */
  private static EAuthRequestsDAO reqdao;

  /**
   * The eauthprescdao.
   */
  private static EAuthPrescriptionDAO eauthprescdao;

  /**
   * The generator.
   */
  private static EAuthRequestXMLGenerator generator;

  /**
   * The pbmhelper.
   */
  private static PBMPrescriptionHelper pbmhelper;

  /**
   * Instantiates a new e auth requests action.
   */
  public EAuthRequestsAction() {
    reqdao = new EAuthRequestsDAO();
    eauthprescdao = new EAuthPrescriptionDAO();
    generator = new EAuthRequestXMLGenerator();
    pbmhelper = new PBMPrescriptionHelper();
  }

  /**
   * View E auth request XML.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward viewEAuthRequestXML(ActionMapping mapping,
                                           ActionForm fm, HttpServletRequest request,
                                           HttpServletResponse response) throws SQLException,
      Exception {

    String preauthPrescIdStr = request.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPrescIdStr);
    String insuranceCoId = request.getParameter("insurance_co_id");
    String errStr = null;
    BasicDynaBean preauthPrescBean = eauthprescdao
        .getEAuthPresc(preauthPrescId, insuranceCoId);

    if (null == preauthPrescBean) {
      errStr = "Prior Auth PRESCRIPTION ERROR: Invalid/No Valid Prescriptions."
          + " Please prescribe/save some items.";
    }

    if (errStr != null) {
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;

    if (null != preauthPrescBean.get("center_id")) { // Visit Center
      preauthCenterId = (Integer) preauthPrescBean.get("center_id");
    }

    if (null != preauthPrescBean.get("preauth_center_id")) { // Prior Auth
      // Request
      // Center
      preauthCenterId = (Integer) preauthPrescBean
          .get("preauth_center_id");
    }

    // Validate Facility ID and Payer ID
    HttpSession session = request.getSession(false);
    String shafafiyaEAuthActive = (String) session
        .getAttribute("shafafiya_preauth_active");
    errStr = validateEAuth(preauthPrescBean, preauthCenterId,
        shafafiyaEAuthActive);
    if (errStr != null) {
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    String requestType = reqdao.getEAuthRequestType(preauthPrescId);
    String userAction = "View";
    // Generate and save EAuth Request Id before generating XML.
    String userid = (String) session.getAttribute("userid");
    boolean result = reqdao.saveEAuthRequestDetails(preauthPrescId,
        preauthCenterId, insuranceCoId, userid, requestType,
        userAction);
    if (!result) {
      errStr = "Error while generating/saving Prior Auth Request data.";
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    boolean testing = getTestMode(request);
    String xml = generator.generateRequestXML(preauthPrescId, insuranceCoId,
        requestType, testing, shafafiyaEAuthActive);

    if (xml != null && xml.startsWith("Error")) {
      errStr = xml;
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    } else {
      response.setContentType("text/xml");
      OutputStream outputStream = response.getOutputStream();
      if (xml != null) {
        outputStream.write(xml.getBytes());
      }
      outputStream.flush();
      outputStream.close();
    }
    return null;
  }

  /**
   * Validate E auth.
   *
   * @param preauthPrescBean     the preauth presc bean
   * @param preauthCenterId      the preauth center id
   * @param shafafiyaEAuthActive the shafafiya E auth active
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String validateEAuth(BasicDynaBean preauthPrescBean,
                              Integer preauthCenterId, String shafafiyaEAuthActive)
      throws SQLException {

    String path = RequestContext.getHttpRequest().getContextPath();
    String testingProviderId = null; // "MF2222"

    Integer userCenterId = RequestContext.getCenterId();
    userCenterId = userCenterId == null ? 0 : userCenterId;
    BasicDynaBean centerBean = new CenterMasterDAO().findByKey("center_id",
        userCenterId);
    if (centerBean != null) {
      String shafafiyaPreauthTestProviderId = centerBean
          .get("shafafiya_preauth_test_provider_id") != null
          ? ((String) centerBean
          .get("shafafiya_preauth_test_provider_id"))
          .trim()
          : "";
      testingProviderId = shafafiyaPreauthTestProviderId;
    }

    String facilityId = null;
    String facilityMsgTxt = null;
    String healthAuthority = CenterMasterDAO
        .getHealthAuthorityForCenter(preauthCenterId);
    if (preauthCenterId != null) {

      BasicDynaBean centerbean = new CenterMasterDAO()
          .findByKey("center_id", preauthCenterId);

      if (healthAuthority == null || healthAuthority.trim().equals("")
          || !(healthAuthority.equals("HAAD")
          || healthAuthority.equals("DHA"))) {
        String healthAuthorityMsgTxt = "Center: "
            + (pbmhelper.urlString(path, "center-name",
            preauthCenterId.toString(),
            (String) centerbean.get("center_name")));

        if (facilityId == null || facilityId.trim().equals("")) {
          return "HEALTH_AUTHORITY ERROR: Invalid or No Prior Auth Health Authority for Center "
              + healthAuthorityMsgTxt;
        }
      }

      facilityId = centerbean
          .get("hospital_center_service_reg_no") != null
          ? (String) centerbean
          .get("hospital_center_service_reg_no")
          : "";
      facilityMsgTxt = "Center: " + (pbmhelper.urlString(path,
          "center-name", preauthCenterId.toString(),
          (String) centerbean.get("center_name")));

      if (facilityId == null || facilityId.trim().equals("")) {
        return "FACILITY ID ERROR: Service Reg No. cannot be null. "
            + facilityMsgTxt;
      }
    }

    if (shafafiyaEAuthActive.equals("N") && testingProviderId.equals("")
        && !healthAuthority.equals("DHA")) {
      return "FACILITY ID ERROR: Prior Auth Request cannot be sent. Test Provider No. cannot be "
          + "null. " + facilityMsgTxt;
    }

    if (facilityId != null && shafafiyaEAuthActive.equals("N")
        && !healthAuthority.equals("DHA")
        && !facilityId.trim().equals(testingProviderId)) {
      return "FACILITY ID ERROR: Prior Auth Request cannot be sent. Service Reg No. needs to be: "
          + "<b> " + testingProviderId + "</b> for " + facilityMsgTxt;
    }

    String patientId = (String) preauthPrescBean.get("patient_id");
    String tpaId = preauthPrescBean.get("tpa_id") != null
        ? (String) preauthPrescBean.get("tpa_id")
        : null;

    if (tpaId == null || tpaId.trim().equals("")) {
      String msgTxt = pbmhelper.urlString(path, "insurance", patientId,
          patientId);
      return "TPA/SPONSOR ID ERROR: Patient has no TPA/Sponsor " + msgTxt;
    }

    BasicDynaBean tpabean = DataBaseUtil.queryToDynaBean(
        "SELECT htc.tpa_code, tm.tpa_name FROM " + "tpa_master tm "
            + " LEFT JOIN ha_tpa_code htc ON(htc.tpa_id=tm.tpa_id AND health_authority = ?) WHERE"
            + " tm"
            + ".tpa_id = ? ",
        new Object[] {healthAuthority, tpaId});
    String tpaName = tpabean != null ? (String) tpabean.get("tpa_name") : "";
    String receiverId = null;
    if (tpabean != null) {
      receiverId = tpabean.get("tpa_code") != null ? (String) tpabean.get("tpa_code") :
          "@" + (String) tpabean.get("tpa_name");
    }
    if (receiverId == null || receiverId.trim().equals("")) {
      String msgTxt = "TPA/Sponsor: " + pbmhelper.urlString(path, "sponsor", tpaId, tpaName);
      return "RECEIVER ID ERROR: TPA/Sponsor code cannot be null. " + msgTxt;
    }

    String insuranceCoId = preauthPrescBean.get("insurance_co_id") != null
        ? (String) preauthPrescBean.get("insurance_co_id")
        : null;

    if (insuranceCoId == null || insuranceCoId.trim().equals("")) {
      String msgTxt = pbmhelper.urlString(path, "insurance", patientId,
          patientId);
      return "INSURANCE COMPANY ID ERROR: Patient has no Insurance company "
          + msgTxt;
    }

    BasicDynaBean insubean = DataBaseUtil.queryToDynaBean("SELECT hic.insurance_co_code, icm"
        + ".insurance_co_name FROM insurance_company_master icm "
        + " LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id AND "
        + "health_authority = ?) WHERE icm.insurance_co_id = ? ", new Object[] {healthAuthority,
          insuranceCoId});
    String insuranceCoName = insubean != null ? (String) insubean.get("insurance_co_name") : "";
    String payerId = insubean != null && insubean.get("insurance_co_code") != null
        ? (String) insubean.get("insurance_co_code") : "@" + insuranceCoName;

    if (payerId == null || payerId.trim().equals("")) {
      String msgTxt = "Insurance Company: " + pbmhelper.urlString(path,
          "company", insuranceCoId, insuranceCoName);
      return "PAYER ID ERROR: Insurance company code cannot be null. "
          + msgTxt;
    }

    int planId = preauthPrescBean.get("plan_id") != null
        ? (Integer) preauthPrescBean.get("plan_id")
        : 0;

    if (planId == 0) {
      String msgTxt = pbmhelper.urlString(path, "insurance", patientId,
          patientId);
      return "PLAN ID ERROR: Patient has no Plan " + msgTxt;
    }
    return null;
  }

  /**
   * Send redirect errors.
   *
   * @param preauthPrescIdStr the preauth presc id str
   * @param insuranceCoId     the insurance co id
   * @param request           the request
   * @param mapping           the mapping
   * @param errStr            the err str
   * @return the action forward
   */
  public ActionForward sendRedirectErrors(String preauthPrescIdStr,
                                          String insuranceCoId, HttpServletRequest request,
                                          ActionMapping mapping, String errStr) {
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("sendRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.error(errStr);
    redirect.addParameter("insurance_co_id", insuranceCoId);
    redirect.addParameter("preauth_presc_id", preauthPrescIdStr);
    return redirect;
  }

  /**
   * Send errors.
   *
   * @param request the request
   * @param mapping the mapping
   * @param errStr  the err str
   * @return the action forward
   */
  public ActionForward sendErrors(HttpServletRequest request,
                                  ActionMapping mapping, String errStr) {
    request.setAttribute("error", errStr);
    request.setAttribute("referer", request.getHeader("Referer")
        .replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    return mapping.findForward("listReportErrors");
  }

  /**
   * Gets the test mode.
   *
   * @param request the request
   * @return the test mode
   */
  private boolean getTestMode(HttpServletRequest request) {
    return false; // Prior Auth is in Production Mode.
    /*
     * String testing = request.getParameter("testing"); return (null !=
     * testing && "Y".equalsIgnoreCase(testing));
     */
  }

  /**
   * Send E auth request.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward sendEAuthRequest(ActionMapping mapping, ActionForm fm,
                                        HttpServletRequest request, HttpServletResponse response)
      throws SQLException, Exception {

    String preauthPrescIdStr = request.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPrescIdStr);
    String insuranceCoId = request.getParameter("insurance_co_id");
    String errStr = null;

    BasicDynaBean preauthPrescBean = eauthprescdao
        .getEAuthPresc(preauthPrescId, insuranceCoId);

    if (null == preauthPrescBean) {
      errStr = "Prior Auth PRESCRIPTION ERROR: Invalid/No Valid Prescriptions."
          + " Please prescribe/save some items.";
    }

    if (errStr != null) {
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;

    if (null != preauthPrescBean.get("center_id")) { // Visit Center
      preauthCenterId = (Integer) preauthPrescBean.get("center_id");
    }

    if (null != preauthPrescBean.get("preauth_center_id")) { // Prior Auth
      // Request
      // Center
      preauthCenterId = (Integer) preauthPrescBean
          .get("preauth_center_id");
    }

    BasicDynaBean centerMaster = new CenterMasterDAO()
        .findByKey("center_id", preauthCenterId);
    if (null == centerMaster) {
      errStr = "Unknown Center.";
      // Forward to errors page.
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }
    String shafafiyaEAuthActive = (String) centerMaster
        .get("shafafiya_preauth_active");
    // Validate Facility ID and Payer ID
    errStr = validateEAuth(preauthPrescBean, preauthCenterId,
        shafafiyaEAuthActive);
    if (errStr != null) {
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    String userAction = "Request";
    String requestType = "Authorization";
    // Generate and save EAuth Request Id before generating XML.
    boolean result = reqdao.saveEAuthRequestDetails(preauthPrescId,
        preauthCenterId, insuranceCoId, userid, requestType,
        userAction);
    if (!result) {
      errStr = "Error while generating/saving Prior Auth Request data.";
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    boolean testing = getTestMode(request);
    String xml = generator.generateRequestXML(preauthPrescId, insuranceCoId,
        requestType, testing, shafafiyaEAuthActive);

    if (xml != null && xml.startsWith("Error")) {
      errStr = xml;
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    log.info("Prior Auth Request XML Content for Prior Auth Id: "
        + preauthPrescId + " is ... : " + xml);

    preauthPrescBean = eauthprescdao.getEAuthPresc(preauthPrescId,
        insuranceCoId); // retrieve the file name which is recently
    // saved. BUG 44931

    // Upload eAuth Request, return error report file or error message using eAuth Response.
    EAuthResponse eauthresponse = null;//sender.sendEAuthRequest(xml, xmlFileName);

    //Upload eAuth Request from New plugin code start
    String healthAuthority = centerMaster.get("health_authority") != null
        ? ((String) centerMaster.get("health_authority")).trim() : "";
    String userName = centerMaster.get("ha_username") != null
        ? ((String) centerMaster.get("ha_username")).trim() : "";
    String passWord = centerMaster.get("ha_password") != null
        ? ((String) centerMaster.get("ha_password")).trim() : "";

    log.info("UserName:" + userName + "--Password: " + passWord + "--Active status: "
        + shafafiyaEAuthActive);
    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin == null) {
      errStr = "Unknown Health authority.";
      // Forward to errors page.
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, errStr);
    }
    HashMap<String, String> configMap = new HashMap<String, String>();
    configMap.put("userName", userName);
    configMap.put("passWord", passWord);
    configMap.put("disposition", shafafiyaEAuthActive);
    plugin.setConfiguration(configMap);
    PriorAuthDocument priorAuthDocument = new PriorAuthDocument();
    //set the file content as a byte format
    if (xml != null) {
      priorAuthDocument.setContent(xml.getBytes());
    }

    String xmlFileName = preauthPrescBean.get("file_name") != null
        ? (String) preauthPrescBean.get("file_name")
        : "TEST.XML";

    priorAuthDocument.setFileName(xmlFileName);
    try {
      PriorAuthRequestResults prioorAuthResult = plugin.sendPriorAuthRequest(priorAuthDocument);
      eauthresponse = new EAuthResponse(prioorAuthResult.getTxnResult().value,
          prioorAuthResult.getErrorMessage().value, prioorAuthResult.getErrorReport().value);
    } catch (ConnectException connectException) {
      String err =
          "Client server is Down/Response is corrupted..... Cannot connect to "
              + plugin.getWebservicesHost();
      log.error(err);
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, err);
    }

    if (eauthresponse.isError()) {
      String errorMessage = eauthresponse.getErrorMessage();
      Object errorReport = eauthresponse.getErrorReport();
      if (errorReport != null) {
        if ("HAAD".equals(healthAuthority)) {
          EResponseProcessor.ZipStreamProcessor errProcessor =
              new EResponseProcessor.ZipStreamProcessor();
          String errorFileName = "ERRORS_" + xmlFileName + "_" + DataBaseUtil.getCurrentDate();
          response.setContentType("application/vnd.ms-excel");
          response.setHeader("Content-disposition", "attachment; filename=\"" + errorFileName
              + ".xls" + "\"");

          OutputStream outputStream = response.getOutputStream();
          errProcessor.process(eauthresponse, outputStream);
          return null;
        } else {
          PriorAuthorizationHelper priorAuthHelper = new PriorAuthorizationHelper();
          String errorFileName = "ERRORS_" + xmlFileName + "_" + DataBaseUtil.getCurrentDate();
          byte[] errorReportBytes = (byte[]) errorReport;
          File decodedDataFile = File.createTempFile("tempPriorAuthErrorReportFile", "");
          String errorReportStr = priorAuthHelper.getErrorReportbase64String(errorReportBytes);
          // Encoded string
          String err = priorAuthHelper.decodeErrorReportbase64ToFile(errorReportStr,
              decodedDataFile);
          if (err != null) {
            return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, err);
          }
          response.setContentType("text/plain");
          response.setHeader("Content-disposition", "attachment; filename=\"" + errorFileName
              + ".txt" + "\"");
          OutputStream outputStream = response.getOutputStream();
          outputStream.write(Base64.decodeBase64(errorReportStr.getBytes()));
          outputStream.flush();
          outputStream.close();
          return null;
        }
      } else if (errorMessage != null && !errorMessage.equals("")) {
        return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, errorMessage);
      } else {
        return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, "Upload "
            + "Failed: no error report in response.");
      }
    }

    log.info(
        " Prior Auth prescription transaction upload successful for Prior Auth Id: "
            + preauthPrescId);
    // mark the item activity status to sent.
    boolean success = new EAuthPrescriptionActivitiesDAO()
        .markItemAsSent(preauthPrescBean);
    if (!success) {
      String err = "Error while marking Prior Auth Prescription "
          + preauthPrescId + " Activities as Sent.";
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, err);
    }

    // Mark Prior Auth Prescription as Sent.
    success = eauthprescdao.markEAuthPrescSent(preauthPrescId);
    if (!success) {
      String err = "Error while marking Prior Auth Prescription "
          + preauthPrescId + " as Sent.";
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, err);
    }

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("showRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.info("Prior Auth Request Sent successfully.");
    redirect.addParameter("preauth_presc_id", preauthPrescIdStr);
    redirect.addParameter("insurance_co_id", insuranceCoId);
    String priorityStr = request.getParameter("priority");
    int priority = (priorityStr != null && !priorityStr.equals(""))
        ? Integer.parseInt(priorityStr)
        : 0;

    redirect.addParameter("priority", priority);
    return redirect;
  }

  /**
   * Cancel E auth request.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward cancelEAuthRequest(ActionMapping mapping,
                                          ActionForm fm, HttpServletRequest request,
                                          HttpServletResponse response) throws SQLException,
      Exception {

    String preauthPrescIdStr = request.getParameter("preauth_presc_id");
    int preauthPrescId = Integer.parseInt(preauthPrescIdStr);
    String insuranceCoId = request.getParameter("insurance_co_id");
    String errStr = null;

    BasicDynaBean preauthPrescBean = eauthprescdao
        .getEAuthPresc(preauthPrescId, insuranceCoId);

    if (null == preauthPrescBean) {
      errStr = "Prior Auth PRESCRIPTION ERROR: Invalid/No Valid Prescriptions."
          + " Please prescribe/save some items.";
    }

    if (errStr != null) {
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    Integer centerId = RequestContext.getCenterId();
    Integer preauthCenterId = centerId;

    if (null != preauthPrescBean.get("center_id")) { // Visit Center
      preauthCenterId = (Integer) preauthPrescBean.get("center_id");
    }

    if (null != preauthPrescBean.get("preauth_center_id")) { // Prior Auth
      // Request
      // Center
      preauthCenterId = (Integer) preauthPrescBean
          .get("preauth_center_id");
    }

    BasicDynaBean centerMaster = new CenterMasterDAO()
        .findByKey("center_id", preauthCenterId);
    if (null == centerMaster) {
      errStr = "Unknown Center.";
      // Forward to errors page.
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }
    String shafafiyaEAuthActive = (String) centerMaster
        .get("shafafiya_preauth_active");
    // Validate Facility ID and Payer ID
    errStr = validateEAuth(preauthPrescBean, preauthCenterId,
        shafafiyaEAuthActive);
    if (errStr != null) {
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    // Generate and save EAuth Request Id before generating XML.
    HttpSession session = request.getSession(false);
    String userAction = "Cancel";
    String userid = (String) session.getAttribute("userid");
    String requestType = "Cancellation";
    boolean result = reqdao.saveEAuthRequestDetails(preauthPrescId,
        preauthCenterId, insuranceCoId, userid, requestType,
        userAction);
    if (!result) {
      errStr = "Error while generating/saving Prior Auth Request data.";
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    boolean testing = getTestMode(request);
    String xml = generator.generateRequestXML(preauthPrescId, insuranceCoId,
        requestType, testing, shafafiyaEAuthActive);

    if (xml != null && xml.startsWith("Error")) {
      errStr = xml;
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }

    log.info("Prior Auth Cancel Request XML Content for Prior Auth Id: "
        + preauthPrescId + " is ... : " + xml);

    String xmlFileName = preauthPrescBean.get("file_name") != null
        ? (String) preauthPrescBean.get("file_name")
        : "TEST.XML";
    if (xmlFileName.contains(".xml")) {
      xmlFileName = xmlFileName.replace(".xml", "_cancel.xml");
    } else if (xmlFileName.contains(".XML")) {
      xmlFileName = xmlFileName.replace(".XML", "_cancel.xml");
    }
    String healthAuthority = centerMaster.get("health_authority") != null
        ? ((String) centerMaster.get("health_authority")).trim()
        : "";
    String userName = centerMaster.get("ha_username") != null
        ? ((String) centerMaster.get("ha_username")).trim()
        : "";
    String passWord = centerMaster.get("ha_password") != null
        ? ((String) centerMaster.get("ha_password")).trim()
        : "";

    log.info("UserName:" + userName + "--Password: " + passWord
        + "--Active status: " + shafafiyaEAuthActive);

    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin == null) {
      errStr = "Unknown Health authority.";
      // Forward to errors page.
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, errStr);
    }
    HashMap<String, String> configMap = new HashMap<String, String>();
    configMap.put("userName", userName);
    configMap.put("passWord", passWord);
    configMap.put("disposition", shafafiyaEAuthActive);
    plugin.setConfiguration(configMap);
    PriorAuthDocument priorAuthDocument = new PriorAuthDocument();
    // set the file content as a byte format
    priorAuthDocument.setContent(xml.getBytes());
    priorAuthDocument.setFileName(xmlFileName);
    EAuthResponse eauthresponse = null;
    try {
      PriorAuthRequestResults prioorAuthResult = plugin
          .sendPriorAuthRequest(priorAuthDocument);
      // Upload eAuth Cancel Request, return error report file or error
      // message using eAuth Response.
      eauthresponse = new EAuthResponse(
          prioorAuthResult.getTxnResult().value,
          prioorAuthResult.getErrorMessage().value,
          prioorAuthResult.getErrorReport().value);
    } catch (ConnectException connectException) {
      String err = "";
      err = "Client server is Down/Response is corrupted..... Cannot connect to "
          + plugin.getWebservicesHost();
      log.error(err);
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request,
          mapping, err);
    }

    if (eauthresponse.isError()) {
      result = new EAuthApprovalsDAO().updatePreauthReqApprovalType(preauthPrescId);
      if (!result) {
        errStr = "Error while updating Preauth request type in hms..";
        return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, errStr);
      }
      String errorMessage = eauthresponse.getErrorMessage();
      Object errorReport = eauthresponse.getErrorReport();
      if ("HAAD".equals(healthAuthority)) {
        if (errorReport != null) {
          EResponseProcessor.ZipStreamProcessor errProcessor =
              new EResponseProcessor.ZipStreamProcessor();

          String errorFileName = "ERRORS_" + xmlFileName + "_" + DataBaseUtil.getCurrentDate();
          response.setContentType("application/vnd.ms-excel");
          response.setHeader("Content-disposition", "attachment; filename=\"" + errorFileName
              + ".xls" + "\"");

          OutputStream outputStream = response.getOutputStream();
          errProcessor.process(eauthresponse, outputStream);
          return null;

        } else if (errorMessage != null && !errorMessage.equals("")) {
          return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping,
              errorMessage);
        }
      } else {
        PriorAuthorizationHelper priorAuthHelper = new PriorAuthorizationHelper();
        String errorFileName = "ERRORS_" + xmlFileName + "_" + DataBaseUtil.getCurrentDate();
        byte[] errorReportBytes = (byte[]) errorReport;
        File decodedDataFile = File.createTempFile("tempPriorAuthErrorReportFile", "");
        String errorReportStr = priorAuthHelper.getErrorReportbase64String(errorReportBytes); //
        // Encoded string
        String err = priorAuthHelper.decodeErrorReportbase64ToFile(errorReportStr, decodedDataFile);
        if (err != null) {
          return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, err);
        }
        response.setContentType("text/plain");
        response.setHeader("Content-disposition", "attachment; filename=\"" + errorFileName
            + ".txt" + "\"");
        OutputStream outputStream = response.getOutputStream();
        outputStream.write(Base64.decodeBase64(errorReportStr.getBytes()));
        outputStream.flush();
        outputStream.close();
        return null;
      }
    }
    result = new EAuthApprovalsDAO().updateToCancellationStatus(preauthPrescId);
    if (!result) {
      errStr = "Error while updating Cancellation statuses in hms..";
      return sendRedirectErrors(preauthPrescIdStr, insuranceCoId, request, mapping, errStr);
    }
    log.info(" Prior Auth cancel transaction upload successful for Prior Auth Id: "
        + preauthPrescId);

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("showRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.info("Prior Auth Prescription Request is Cancelled.");
    redirect.addParameter("preauth_presc_id", preauthPrescIdStr);
    redirect.addParameter("insurance_co_id", insuranceCoId);
    String priorityStr = request.getParameter("priority");
    int priority = (priorityStr != null && !priorityStr.equals(""))
        ? Integer.parseInt(priorityStr) : 0;
    redirect.addParameter("priority", priority);
    return redirect;
  } 
  
  /**
   * Download EAuth Response File.
   *
   * @param mapping  The mapping
   * @param form     The form
   * @param request  The request
   * @param response The response
   * @return The action forward
   * @throws SQLException The SQLException
   * @throws Exception    The Exception
   */
  public ActionForward downloadXMLResponseFile(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
    Integer preauthPrescId = !StringUtil.isNullOrEmpty(request.getParameter("preauth_presc_id"))
        ? Integer.parseInt(request.getParameter("preauth_presc_id"))
        : null;
    String preauthRequestId = !StringUtil.isNullOrEmpty(request.getParameter("preauth_request_id"))
        ? request.getParameter("preauth_request_id")
        : "";
    String insuranceCoId = request.getParameter("insurance_co_id");
    String downloadKey = !StringUtil.isNullOrEmpty(request.getParameter("download_key"))
        ? request.getParameter("download_key")
        : "";
    String errStr = null;
    Integer centerId = RequestContext.getCenterId();
    BasicDynaBean centerMaster = new CenterMasterDAO().findByKey("center_id", centerId);
    if (null == centerMaster) {
      errStr = "Unknown Center.";
      // Forward to errors page.
      return sendRedirectErrors(preauthPrescId.toString(), insuranceCoId, request, mapping, errStr);
    }
    if (StringUtil.isNullOrEmpty(downloadKey)) {
      errStr = "Please Click on Download button";
      return sendRedirectErrors(preauthPrescId.toString(), insuranceCoId, request, mapping, errStr);
    }
    String shafafiyaEAuthActive = (String) centerMaster.get("shafafiya_preauth_active");

    String healthAuthority = centerMaster.get("health_authority") != null
        ? ((String) centerMaster.get("health_authority")).trim()
        : "";

    String userName = centerMaster.get("ha_username") != null
        ? ((String) centerMaster.get("ha_username")).trim()
        : "";

    String passWord = centerMaster.get("ha_password") != null
        ? ((String) centerMaster.get("ha_password")).trim()
        : "";

    log.info("UserName:" + userName + "--Password: " + passWord + "--Active status: "
        + shafafiyaEAuthActive);

    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin == null) {
      errStr = "Unknown Health authority.";
      return sendRedirectErrors(preauthPrescId.toString(), insuranceCoId, request, mapping, errStr);
    }
    HashMap<String, String> configMap = new HashMap<String, String>();
    configMap.put("userName", userName);
    configMap.put("passWord", passWord);
    configMap.put("disposition", shafafiyaEAuthActive);
    plugin.setConfiguration(configMap);
    FileOutputStream dataOutPutStream = null;
    try {
      List<BasicDynaBean> fileids = reqdao.getFileIds(preauthPrescId, preauthRequestId,
          downloadKey);
      if (fileids.size() <= 0) {
        errStr = "No response Found";
        return sendRedirectErrors(preauthPrescId.toString(), insuranceCoId, request, mapping,
            errStr);
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(baos);
      for (BasicDynaBean fileid : fileids) {
        String fileidvalue = (String) fileid.get("file_id");
        // Use the fileId to download the data.
        PriorAuthRequestResults xmlPAResult = plugin.getPriorAuthApprovalFile(fileidvalue);
        String errorMessage = xmlPAResult.getErrorMessage().value;
        if (errorMessage != null && !errorMessage.equals("")) {
          return sendRedirectErrors(preauthPrescId.toString(), insuranceCoId, request, mapping,
              errorMessage);
        }
        File tempFile = File.createTempFile(xmlPAResult.getFileName().value, "");
        dataOutPutStream = new FileOutputStream(tempFile);
        dataOutPutStream.write(xmlPAResult.getFile().value);
        InputStream is = new FileInputStream(tempFile);
        zos.putNextEntry(new ZipEntry(xmlPAResult.getFileName().value));
        zos.write(DataBaseUtil.readInputStream(is));

        if (!tempFile.delete()) { // delete temp file
          log.info("could not delete temporary file" + tempFile.getName());
        }
      }
      zos.closeEntry();
      zos.flush();
      zos.close();
      ServletOutputStream sos = response.getOutputStream();
      response.setContentType("application/zip");
      String zipFileName = "Eauth Response" + "_" + DataBaseUtil.getCurrentDate();
      response.setHeader("Content-disposition",
          "attachment; filename=\"" + zipFileName + ".zip" + "\"");
      sos.write(baos.toByteArray());
      sos.flush();
      sos.close();
    } catch (ConnectException connectException) {
      String err = "Client server is Down/Response is corrupted..... Cannot connect to "
          + plugin.getWebservicesHost();
      log.error(err);
      return sendRedirectErrors(preauthPrescId.toString(), insuranceCoId, request, mapping, err);
    }
    return null;

  }
}
