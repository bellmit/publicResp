/**
 *
 */
package com.insta.hms.erxprescription;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.eservice.EResponseProcessor;
import com.insta.hms.eservice.EResult;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionHelper;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;
import com.insta.hms.util.MapWrapper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class ERxRequestAction extends BaseAction {

  static Logger log = LoggerFactory.getLogger(ERxRequestAction.class);

  private static ERxPrescriptionDAO erxdao = new ERxPrescriptionDAO();
  private static PBMPrescriptionsDAO pbmprescdao = new PBMPrescriptionsDAO();
  private static PBMPrescriptionHelper pbmhelper = new PBMPrescriptionHelper();
  
  private static final GenericDAO erxResponseDAO = new GenericDAO("erx_response");

  public ActionForward sendERxRequest(ActionMapping mapping, ActionForm fm,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("sendRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    log.debug("Getting parameters");
    String consIdStr = request.getParameter("consultation_id");
    String visitId = request.getParameter("visit_id");
    Integer consId = Integer.parseInt(consIdStr);
    boolean testMode = getTestMode(request);
    String requestType = "eRxRequest";
    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    Integer centerid = (Integer) session.getAttribute("centerId");

    ERxRequestGenerator generator = new ERxRequestGenerator(testMode);
    List<String> errorList = new ArrayList<String>();
    log.debug("getting consultation id...");
    int pbmPrescId = erxdao.getErxConsPBMId(consId);
    String userAction = "Request";

    // Validate Facility ID and Payer ID
    String errStr = validateErx(pbmPrescId);
    log.debug("Error String :" + errStr);
    if (errStr != null)
      return sendRedirectErrors(consIdStr, request, mapping, errStr);

    // Generate Erx Presc. Id and save erx_presc_id and center_id before generating XML.
    boolean success = erxdao.saveErxRequestDetails(pbmPrescId, userid, requestType, userAction);
    if (!success) {
      errStr = "Error while generating/saving ERx Request data.";
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    String xml = generator.generateRequestXML(consId, requestType, errorList);

    if (null == xml) {
      errStr = getErrorString(errorList);
      return sendErrors(request, mapping, errStr);
    }

    BasicDynaBean bean = CenterHelper.getDhpoInfoCenterWise(visitId, centerid);
    if (bean == null || (null == bean.get("dhpo_facility_user_id")
        || bean.get("dhpo_facility_user_id").equals(""))) {
      errStr = "This feature is available only for a specific center or Please enter facility username and password. ";
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    String dhpo_facility_user = (String) bean.get("dhpo_facility_user_id");
    String dhpo_facility_password = (String) bean.get("dhpo_facility_password");

    BasicDynaBean consBean = new DoctorConsultationDAO().findByKey("consultation_id", consId);
    String doctorId = (String) consBean.get("doctor_name");

    BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorDetails(doctorId);
    String dhpo_clinician_user = (String) doctorBean.get("dhpo_clinician_user_id");
    String dhpo_clinician_password = (String) doctorBean.get("dhpo_clinician_password");

    try {
      new ERxWebService().getRemoteService();
    } catch (Exception ex) {
      errStr = "DHPO Server Connection error: " + ex.getMessage();
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    ERxRequestSender sender = new ERxRequestSender(dhpo_facility_user, dhpo_facility_password,
        dhpo_clinician_user, dhpo_clinician_password);

    List<String> columns = new ArrayList<String>();
    columns.add("pbm_presc_id");
    columns.add("erx_file_name");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmprescdao.findByKey(columns, key);

    // Uploads ERx Request and returns the ERx Response
    String xmlFileName = pbmPresBean.get("erx_file_name") != null
        ? (String) pbmPresBean.get("erx_file_name")
        : "TEST.XML";
    ERxResponse eRxresponse = sender.sendErxRequest(xml, xmlFileName);

    if (eRxresponse.isError()) {
      String errorMessage = eRxresponse.getErrorMessage();
      Object errorReport = eRxresponse.getErrorReport();

      // From PBM, we have known that the error report would be a excel file.
      // Hence, here we are not encoding and decoding error report as a file for user to take
      // action.
      // Instead, read the first file entry and write to the browser directly
      // (or) show the error message if any.
      // While framework changes are done in PBM, need to follow the same.

      if (errorReport != null) {
        EResponseProcessor.CsvStreamProcessor errProcessor = new EResponseProcessor.CsvStreamProcessor();

        String errorFileName = xmlFileName + "_" + DataBaseUtil.getCurrentDate();

        // saves the sent error file from erx webservice to database.
        saveErrorFile(eRxresponse, errProcessor, errorFileName, request, flash);

        redirect.addParameter("consultation_id", consIdStr);
        return redirect;
      } else if (errorMessage != null && !errorMessage.equals("")) {
        flash.error(errorMessage);
        redirect.addParameter("consultation_id", consIdStr);
        return redirect;
      }
    }

    // we use this for responses which return a bunch of simple properties as out parameters
    EResponseProcessor.SimpleParameterProcessor processor = new ERxResponseProcessor.SimpleParameterProcessor(
        new String[] { "eRxReferenceNo" });
    EResult result = processor.process(eRxresponse);
    if (result != null && result instanceof MapWrapper) {
      log.info("Result retrieved: result is a map");
      MapWrapper resultObj = (MapWrapper) result;
      resultObj.put("pbm_presc_id", pbmPrescId);
      success = erxdao.saveResponse(resultObj.getMap()); // save the eRxReference no. to the
                                                         // database

      if (!success) {
        flash.error("Error while marking ERx prescription Request as Sent.");
        redirect.addParameter("consultation_id", consIdStr);
        return redirect;
      }
    }

    if (success) {
      updatePBMPrescriptionId(consId, pbmPrescId);
    }

    flash.info("ERx prescription Request is Sent successfully.");
    redirect.addParameter("consultation_id", consIdStr);
    return redirect;
  }

  private boolean updatePBMPrescriptionId(Integer consId, Integer pbmPrescId)
      throws SQLException, IOException {
    Connection con = null;
    boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Map map = new HashMap();
      map.put("consultation_id", consId);
      map.put("pbm_presc_id", pbmPrescId);
      int updated = erxResponseDAO.updateWithNames(con, new String[] { "pbm_presc_id" }, map,
          new String[] { "consultation_id" });
      success = (updated >= 0);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    return success;
  }

  public String validateErx(int pbmPrescId) throws SQLException {

    String path = RequestContext.getHttpRequest().getContextPath();
    BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();

    List<String> columns = new ArrayList<String>();
    columns.add("pbm_presc_id");
    columns.add("erx_center_id");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmprescdao.findByKey(columns, key);

    if (null == pbmPresBean) {
      return "ERx PBM ID ERROR: Invalid/No Medicine Prescriptions. Please prescribe/save medicines.";
    }

    Integer centerId = RequestContext.getCenterId();
    Integer erxCenterId = centerId;
    if (null != pbmPresBean.get("erx_center_id"))
      erxCenterId = (Integer) pbmPresBean.get("erx_center_id");

    String facilityId = null;

    if (erxCenterId != null && erxCenterId != 0) {
      BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", centerId);
      facilityId = centerbean.get("hospital_center_service_reg_no") != null
          ? (String) centerbean.get("hospital_center_service_reg_no")
          : "";
      String msgTxt = "Center: " + (pbmhelper.urlString(path, "center-name",
          Integer.toString(centerId), (String) centerbean.get("center_name")));

      if (facilityId == null || facilityId.trim().equals("")) {
        return "FACILITY ID ERROR: Service Reg No. cannot be null. " + msgTxt;
      }
    } else {
      facilityId = genPrefs.get("hospital_service_regn_no") != null
          ? (String) genPrefs.get("hospital_service_regn_no")
          : "";
      String msgTxt = "Generic Preferences";

      if (facilityId == null || facilityId.trim().equals("")) {
        return "FACILITY ID ERROR: Service Reg No. cannot be null. " + msgTxt;
      }
    }

    BasicDynaBean erxConsBean = erxdao.getConsErxDetails(pbmPrescId);

    if (null == erxConsBean) {
      return "ERx CONS. ID ERROR: Invalid/No Medicine Prescriptions. Please prescribe/save medicines.";
    }
    Boolean isSelfpay = false;
    if ((Boolean) erxConsBean.get("is_selfpay_sponsor")
        || erxConsBean.get("primary_sponsor_id") == null) {
      isSelfpay = true;
    }
    if (!isSelfpay) {
      String patient_id = (String) erxConsBean.get("patient_id");
      int plan_id = erxConsBean.get("plan_id") != null ? (Integer) erxConsBean.get("plan_id") : 0;
      String insurance_co_id = erxConsBean.get("primary_insurance_co") != null
          ? (String) erxConsBean.get("primary_insurance_co")
          : null;
      String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(erxCenterId);
      BasicDynaBean insubean = DataBaseUtil.queryToDynaBean(
          "SELECT hic.insurance_co_code, icm.insurance_co_name FROM insurance_company_master icm "
              + " LEFT JOIN ha_ins_company_code hic ON(hic.insurance_co_id=icm.insurance_co_id "
              + "AND health_authority = ?) WHERE icm.insurance_co_id=? ",
          new Object[] { healthAuthority, insurance_co_id });
      String insurance_co_name = insubean != null ? (String) insubean.get("insurance_co_name") : "";
      String payerId = insubean != null && insubean.get("insurance_co_code") != null
          ? (String) insubean.get("insurance_co_code")
          : "@" + insurance_co_name;

      if (plan_id == 0) {
        String msgTxt = pbmhelper.urlString(path, "insurance", patient_id, patient_id);
        return "PLAN ID ERROR: Patient has no Plan " + msgTxt;
      }

      if (insurance_co_id == null || insurance_co_id.trim().equals("")) {
        String msgTxt = pbmhelper.urlString(path, "insurance", patient_id, patient_id);
        return "INSURANCE COMPANY ID ERROR: Patient has no Insurance company " + msgTxt;
      }

      if (payerId == null || payerId.trim().equals("")) {
        String msgTxt = "Insurance Company: "
            + pbmhelper.urlString(path, "company", insurance_co_id, insurance_co_name);
        return "PAYER ID ERROR: Insurance company code cannot be null. " + msgTxt;
      }
    }
    return null;
  }

  public ActionForward cancelERxRequest(ActionMapping mapping, ActionForm fm,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("cancelRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    String visitId = request.getParameter("visit_id");
    String consIdStr = request.getParameter("consultation_id");
    Integer consId = Integer.parseInt(consIdStr);
    boolean testing = getTestMode(request);
    String requestType = "eRxCancellation";
    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    Integer centerid = (Integer) session.getAttribute("centerId");

    ERxRequestGenerator generator = new ERxRequestGenerator(testing);
    List<String> errorList = new ArrayList<String>();

    int pbmPrescId = erxdao.getErxConsPBMId(consId);
    String userAction = "Cancel";

    // Validate Facility ID and Payer ID
    String errStr = validateErx(pbmPrescId);
    if (errStr != null)
      return sendRedirectErrors(consIdStr, request, mapping, errStr);

    // Generate Erx Presc. Id and save erx_presc_id and center_id before generating XML.
    boolean success = erxdao.saveErxRequestDetails(pbmPrescId, userid, requestType, userAction);
    if (!success) {
      errStr = "Error while saving ERx Cancellation data.";
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    String xml = generator.generateRequestXML(consId, requestType, errorList);

    if (null == xml) {
      errStr = getErrorString(errorList);
      return sendErrors(request, mapping, errStr);
    }

    BasicDynaBean bean = CenterHelper.getDhpoInfoCenterWise(visitId, centerid);
    if (bean == null || (null == bean.get("dhpo_facility_user_id")
        || bean.get("dhpo_facility_user_id").equals(""))) {
      errStr = "This feature is available only for a specific center or Please enter facility username and password. ";
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    String dhpo_facility_user = (String) bean.get("dhpo_facility_user_id");
    String dhpo_facility_password = (String) bean.get("dhpo_facility_password");

    BasicDynaBean consBean = new DoctorConsultationDAO().findByKey("consultation_id", consId);
    String doctorId = (String) consBean.get("doctor_name");

    BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorDetails(doctorId);
    String dhpo_clinician_user = (String) doctorBean.get("dhpo_clinician_user_id");
    String dhpo_clinician_password = (String) doctorBean.get("dhpo_clinician_password");

    try {
      new ERxWebService().getRemoteService();
    } catch (Exception ex) {
      errStr = "DHPO Server Connection error: " + ex.getMessage();
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    ERxRequestSender sender = new ERxRequestSender(dhpo_facility_user, dhpo_facility_password,
        dhpo_clinician_user, dhpo_clinician_password);

    List<String> columns = new ArrayList<String>();
    columns.add("pbm_presc_id");
    columns.add("erx_file_name");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmprescdao.findByKey(columns, key);

    // Uploads ERx Request and returns the ERx Response
    String xmlFileName = pbmPresBean.get("erx_file_name") != null
        ? (String) pbmPresBean.get("erx_file_name")
        : "TEST.XML";

    // File name cannot be the same as the original request, DHPO errors out. So we append with the
    // Cancel as an indicator and
    // the timestamp so that there are no duplicates in case of multiple cancellation

    if (requestType.equalsIgnoreCase("eRxCancellation") && null != xmlFileName
        && !xmlFileName.trim().equals("")) {
      xmlFileName = "Cancel_" + (new Date().getTime()) + "_" + xmlFileName;
    }

    ERxResponse eRxresponse = sender.sendErxRequest(xml, xmlFileName);

    if (eRxresponse.isError()) {
      String errorMessage = eRxresponse.getErrorMessage();
      Object errorReport = eRxresponse.getErrorReport();

      // From PBM, we have known that the error report would be a excel file.
      // Hence, here we are not encoding and decoding error report as a file for user to take
      // action.
      // Instead, read the first file entry and write to the browser directly
      // (or) show the error message if any.
      // While framework changes are done in PBM, need to follow the same.

      if (errorReport != null) {
        EResponseProcessor.CsvStreamProcessor errProcessor = new EResponseProcessor.CsvStreamProcessor();

        String errorFileName = xmlFileName + "_" + DataBaseUtil.getCurrentDate();
        // saves the sent error file from erx webservice to database.
        saveErrorFile(eRxresponse, errProcessor, errorFileName, request, flash);

        redirect.addParameter("consultation_id", consIdStr);
        return redirect;

      } else if (errorMessage != null && !errorMessage.equals("")) {
        flash.error(errorMessage);
        redirect.addParameter("consultation_id", consIdStr);
        return redirect;
      }
    }

    success = erxdao.saveCancelResponse(consId, pbmPrescId); // Empty the erx ref. no and de-attach
                                                             // prescriptions from request.
    if (!success) {
      flash.error("Error while marking ERx prescription Request as Closed.");
      redirect.addParameter("consultation_id", consIdStr);
      return redirect;
    }

    flash.info("ERx prescription cancellation request is sent successfully.");
    redirect.addParameter("consultation_id", consIdStr);
    return redirect;
  }

  public boolean saveErrorFile(ERxResponse eRxresponse,
      EResponseProcessor.CsvStreamProcessor errProcessor, String errorFileName,
      HttpServletRequest request, FlashScope flash) throws SQLException, IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    errProcessor.process(eRxresponse, outputStream);
    // Erx Response Error file is storing into db (erx_response) and giving a flash message

    Connection con = DataBaseUtil.getConnection();
    con.setAutoCommit(false);
    boolean result = true;
    int responseId = 0;
    try {
      BasicDynaBean mbean = erxResponseDAO.getBean();
      responseId = erxResponseDAO.getNextSequence();
      mbean.set("request_id", "");
      mbean.set("response_id", responseId);// auto sequence
      mbean.set("response_content", new ByteArrayInputStream(outputStream.toByteArray()));
      mbean.set("response_content_type", "application/vnd.ms-excel");
      mbean.set("response_file_name", errorFileName);
      result = erxResponseDAO.insert(con, mbean);
    } finally {
      DataBaseUtil.commitClose(con, result);
    }
    if (result)
      flash.error("DHPO return an error <a href = " + request.getContextPath()
          + "/ERxPrescription/ERxRequest.do?_method=erxResponsFile&responseId=" + responseId + ">"
          + " Click here </a> to Download the error file");
    else
      flash.error("Failed to store the error file..");

    return result;
  }

  // Erx error response file
  @IgnoreConfidentialFilters
  public ActionForward erxResponsFile(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String responseId = request.getParameter("responseId");
    BasicDynaBean bean = erxResponseDAO.getBean();
    erxResponseDAO.loadByteaRecords(bean, "response_id", Integer.parseInt(responseId));
    response.setContentType((String) bean.get("response_content_type"));
    response.setHeader("Content-Disposition",
        "attachment;filename=" + bean.get("response_file_name"));
    try {
      InputStream in = (InputStream) bean.get("response_content");
      OutputStream os = response.getOutputStream();
      org.apache.commons.io.IOUtils.copy(in, os); // This will copy from the input stream to output
                                                  // stream
      in.close();
      os.flush();
      os.close();
    } catch (Exception e) {
      log.error("Erx Response Exception", e);
    }
    return null;
  }

  public ActionForward viewERxRequest(ActionMapping mapping, ActionForm fm,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    String consIdStr = request.getParameter("consultation_id");
    Integer consId = Integer.parseInt(consIdStr);
    boolean testing = getTestMode(request);
    String requestType = "eRxRequest";
    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");

    ERxRequestGenerator generator = new ERxRequestGenerator(testing);
    List<String> errorList = new ArrayList<String>();

    int pbmPrescId = erxdao.getErxConsPBMId(consId);
    String userAction = "View";

    // Validate Facility ID and Payer ID
    String errStr = validateErx(pbmPrescId);
    if (errStr != null)
      return sendRedirectErrors(consIdStr, request, mapping, errStr);

    List<String> columns = new ArrayList<String>();
    columns.add("erx_request_type");

    Map<String, Object> key = new HashMap<String, Object>();
    key.put("pbm_presc_id", pbmPrescId);
    BasicDynaBean pbmPresBean = pbmprescdao.findByKey(columns, key);

    String erxRequestType = requestType;
    if (null != pbmPresBean.get("erx_request_type"))
      erxRequestType = (String) pbmPresBean.get("erx_request_type");

    // Generate Erx Presc. Id and save erx_presc_id and center_id before generating XML.
    boolean result = erxdao.saveErxRequestDetails(pbmPrescId, userid, erxRequestType, userAction);
    if (!result) {
      errStr = "Error while generating/saving ERx Request data.";
      return sendRedirectErrors(consIdStr, request, mapping, errStr);
    }

    String xml = generator.generateRequestXML(consIdStr, erxRequestType, errorList);

    if (null == xml) {
      errStr = getErrorString(errorList);
      return sendErrors(request, mapping, errStr);
    } else {
      response.setContentType("text/xml");
      OutputStream outputStream = response.getOutputStream();
      outputStream.write(xml.getBytes());
      outputStream.flush();
      outputStream.close();
    }
    return null;
  }

  public ActionForward sendRedirectErrors(String consIdStr, HttpServletRequest request,
      ActionMapping mapping, String errStr) {
    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("sendRedirect"));
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    flash.error(errStr);
    redirect.addParameter("consultation_id", consIdStr);
    return redirect;
  }

  public ActionForward sendErrors(HttpServletRequest request, ActionMapping mapping,
      String errStr) {
    request.setAttribute("error", errStr);
    if (request.getHeader("Referer") != null)
      request.setAttribute("referer", request.getHeader("Referer")
          .replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
    return mapping.findForward("listReportErrors");
  }

  private String getErrorString(List<String> errorList) {

    StringBuilder errStr = new StringBuilder(
        "Error(s) while XML data check. " + "ERx Request XML could not be generated.<br/>"
            + "Please correct (or) update the following.<br/>");

    for (String err : errorList) {
      errStr.append("<br/>" + err);
    }
    return errStr.toString();
  }

  private boolean getTestMode(HttpServletRequest request) {
    return false;
  }

}
