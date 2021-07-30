/**
 *
 */
package com.insta.hms.erxprescription;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.erxprescription.erxauthorization.ERxAuthorizationXmlFilesParser;
import com.insta.hms.erxprescription.erxauthorization.ERxAuthorizationXmlParser;
import com.insta.hms.erxprescription.erxauthorization.PriorAuthorization;
import com.insta.hms.erxprescription.erxauthorization.XMLFile;
import com.insta.hms.erxprescription.erxauthorization.XMLFiles;
import com.insta.hms.eservice.EResponse;
import com.insta.hms.eservice.EResponseProcessor;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;
import com.insta.hms.stores.GenericMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
public class ERxApprovalAction extends BaseAction {

  static Logger log = LoggerFactory.getLogger(ERxApprovalAction.class);

  private static ERxPrescriptionDAO erxdao = new ERxPrescriptionDAO();
  private static PBMPrescriptionsDAO pbmprescdao = new PBMPrescriptionsDAO();

  @IgnoreConfidentialFilters
  public ActionForward getERxList(ActionMapping mapping, ActionForm fm, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, Exception {

    List<BasicDynaBean> list = PBMPrescriptionsDAO.getPbmPrescriptions();
    for (BasicDynaBean bean : list) {
      String pbmPrescIdStr = String.valueOf(bean.get("pbm_presc_id"));
      String consIdStr = String.valueOf(bean.get("consultation_id"));
      String patientId = String.valueOf(bean.get("patient_id"));
      if (null != pbmPrescIdStr && !pbmPrescIdStr.equals("")) {
        request.setAttribute("pbm_presc_id", pbmPrescIdStr);
        if(!StringUtils.isEmpty(consIdStr) && !consIdStr.equals("null")) {
          request.setAttribute("consultation_id", consIdStr);
        } else if(!StringUtils.isEmpty(patientId)) {
          request.setAttribute("patient_id", patientId);
        }
        getERxPriorAuthResponse(mapping, fm, request, response);
      }
    }
    ActionForward forwardAction = mapping.findForward("erxListRedirect");
    HttpSession session = request.getSession(false);
    boolean modEclaimPbm = (Boolean)session.getAttribute("mod_eclaim_pbm");
    boolean modEclaimErx = (Boolean)session.getAttribute("mod_eclaim_erx");
    if (modEclaimErx && modEclaimPbm) {
      forwardAction = mapping.findForward("pbmListRedirect");
    }
    ActionRedirect redirect = new ActionRedirect(forwardAction);
    return redirect;

  }

  @IgnoreConfidentialFilters
  public ActionForward getERxPriorAuthResponse(ActionMapping mapping, ActionForm fm,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

    HttpSession session = request.getSession(false);
    String userid = (String) session.getAttribute("userid");
    Integer centerid = (Integer) session.getAttribute("centerId");

    boolean mod_eclaim_pbm = (Boolean) session.getAttribute("mod_eclaim_pbm");
    boolean mod_eclaim_erx = (Boolean) session.getAttribute("mod_eclaim_erx");

    FlashScope flash = FlashScope.getScope(request);
    ActionForward forwardAction = mapping.findForward("erxListRedirect");
    if (mod_eclaim_erx && mod_eclaim_pbm)
      forwardAction = mapping.findForward("pbmListRedirect");
    ActionRedirect redirect = new ActionRedirect(forwardAction);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    String pbmPrescIdStr = request.getParameter("pbm_presc_id");
    String consIdStr = request.getParameter("consultation_id");
    String patientId = request.getParameter("patient_id");
    
    if (StringUtils.isEmpty(consIdStr))
      consIdStr = (String) request.getAttribute("consultation_id");
    if (StringUtils.isEmpty(pbmPrescIdStr))
      pbmPrescIdStr = (String) request.getAttribute("pbm_presc_id");
    if (StringUtils.isEmpty(patientId))
      patientId = (String) request.getAttribute("patient_id");

    int consId = consIdStr != null && !consIdStr.equalsIgnoreCase("null") ? Integer.parseInt(consIdStr) : 0;
    int pbmPrescId = Integer.parseInt(pbmPrescIdStr);

    BasicDynaBean bean = CenterHelper.getDhpoInfoCenterWise(null, centerid);
    if (bean == null || (null == bean.get("dhpo_facility_user_id")
        || bean.get("dhpo_facility_user_id").equals(""))) {
      String errStr = "This feature is available only for a specific center or Please enter facility username and password. ";
      flash.error(errStr);
      redirect.addParameter("pbm_finalized", "N");
      return redirect;
    }

    String dhpo_facility_user = (String) bean.get("dhpo_facility_user_id");
    String dhpo_facility_password = (String) bean.get("dhpo_facility_password");
    String doctorId = "";
    if(consId != 0) {
      BasicDynaBean consBean = new DoctorConsultationDAO().findByKey("consultation_id", consId);
      if (consBean == null || consBean.get("doctor_name") == null) {
        return redirect;
      }
      doctorId = (String) consBean.get("doctor_name");
    } else if(!StringUtils.isEmpty(patientId)){
      Map<String,Object> filterMap = new HashMap<>();
      filterMap.put("patient_id", patientId);
      BasicDynaBean patientRegistrationBean = new PatientRegistrationRepository().findByKey(filterMap);
      if(patientRegistrationBean == null || patientRegistrationBean.get("doctor") == null) {
        return redirect;
      }
      doctorId = (String)patientRegistrationBean.get("doctor");
    }

    BasicDynaBean doctorBean = DoctorMasterDAO.getDoctorDetails(doctorId);
    String dhpo_clinician_user = (String) doctorBean.get("dhpo_clinician_user_id");
    String dhpo_clinician_password = (String) doctorBean.get("dhpo_clinician_password");

    try {
      new ERxWebService().getRemoteService();
    } catch (Exception ex) {
      String errStr = "DHPO Server Connection error: " + ex.getMessage();
      flash.error(errStr);
      redirect.addParameter("pbm_finalized", "N");
      return redirect;
    }

    ERxApprovalsRetriever retriever = new ERxApprovalsRetriever(dhpo_facility_user,
        dhpo_facility_password, dhpo_clinician_user, dhpo_clinician_password);

    // Get erx cons bean.
    BasicDynaBean erxConsBean = erxdao.getConsErxDetails(pbmPrescId);
    if (null == erxConsBean.get("erx_request_date")
        || null == erxConsBean.get("erx_reference_no")) {
      flash.error("No Erx Consultation found for Prescription Id: " + pbmPrescId);
      return redirect;
    }

    String memberID = (String) erxConsBean.get("member_id");
    Timestamp erxRequestDate = (Timestamp) erxConsBean.get("erx_request_date");
    String transactionFromDate = DateUtil
        .formatTimestampWithSlash(DateUtil.addDays(erxRequestDate, -1));
    String transactionToDate = DateUtil
        .formatTimestampWithSlash(DateUtil.addDays(erxRequestDate, 15));

    String eRxReferenceNo = (String) erxConsBean.get("erx_reference_no");
    int erxRefNo = new Integer(eRxReferenceNo);
    EResponse eResponse = retriever.getApprovalFileList(erxRefNo, memberID, transactionFromDate,
        transactionToDate);

    if (eResponse.isError()) {
      String errorMessage = eResponse.getErrorMessage();
      Object errorReport = eResponse.getErrorReport();
      if (errorMessage != null && !errorMessage.equals("")) {
        flash.error(errorMessage);
        redirect.addParameter("pbm_finalized", "N");
        return redirect;
      }
      // TODO : need to parse the error report.
    } else if (null == eResponse.getInputStream()) {
      log.error("No File Received for ERx Ref. No. " + erxRefNo);
      flash.error("No File Received for ERx Ref. No. " + erxRefNo);
      redirect.addParameter("pbm_finalized", "N");
      return redirect;
    }

    /*
     * StringBuffer xml = new StringBuffer();
     * xml.append("<?xml version=\'1.0\' encoding=\'UTF-8\'?>").append("\n"); String foundTxnsXMLStr
     * = eResponse.getErrorReport().toString(); xml.append(foundTxnsXMLStr); foundTxnsXMLStr =
     * xml.toString();
     */

    ERxAuthorizationXmlFilesParser parser = new ERxAuthorizationXmlFilesParser();

    EResponseProcessor.XmlStreamProcessor xmlprocessor = new ERxResponseProcessor.XmlStreamProcessor(
        parser);

    XMLFiles xmlfiles = (XMLFiles) xmlprocessor.process(eResponse);
    ArrayList<XMLFile> xmlfilesList = xmlfiles.getFiles();

    String erxRequestId = null;
    String erxFileId = null;
    String insuCompId = null;
    Integer centerId = null;
    String helathAuthority = null;
    String tpaId = null;

    for (XMLFile xmlfile : xmlfilesList) {

      String fileId = xmlfile.getFileId();
      String xmlfileName = xmlfile.getFileName();
      String responseSenderId = xmlfile.getSenderId();
      String responseReceiverId = xmlfile.getReceiverId();
      String transactionDate = xmlfile.getTransactionDate();
      String recordCount = xmlfile.getRecordCount();
      String isDownloaded = xmlfile.getIsDownloaded();

      // MF2222-A001-20140223162422.From.A001.To.MF2222.File.1.xml.zip

      log.debug("ERx Authorization XML file List: " + xmlfileName + " senderId: " + responseSenderId
          + " receiverId: " + responseReceiverId);

      String[] recdfileStrArr = xmlfileName.split("\\.");
      String recdFileName = recdfileStrArr[0] + ".xml";

      tpaId = (String) erxConsBean.get("primary_sponsor_id");
      insuCompId = (String) erxConsBean.get("primary_insurance_co");
      centerId = (Integer) erxConsBean.get("erx_center_id");
      helathAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

      BasicDynaBean erxheaderBean = erxdao.getERxHeaderFields(pbmPrescId, doctorId, tpaId,
          insuCompId, helathAuthority);
      String erxSenderID = (String) erxheaderBean.get("provider_id");
      String erxReceiverID = (String) erxheaderBean.get("receiver_id");
      String erxFileName = (String) erxheaderBean.get("erx_file_name");
      log.debug("Erx file name in database : " + erxFileName);
      if (null != erxFileName && !erxFileName.trim().equals("")) {
        if (erxFileName.endsWith(".xml")) {
          int xmlIndex = erxFileName.lastIndexOf(".xml"); // knock off the xml extn
          erxFileName = erxFileName.substring(0, xmlIndex);
        }
        log.debug("Erx file name without extn : " + erxFileName);
        int prescIdIndex = erxFileName.lastIndexOf("-");
        erxFileName = erxFileName.substring(0, prescIdIndex); // knock off the presc id
        log.debug("Erx file name without presc id : " + erxFileName);
      }

      // Search ERx prescription by comparing file name and other parameters.
      if (erxSenderID.equals(responseReceiverId) && erxReceiverID.equals(responseSenderId)) {
        // Use the fileId to download the data.
        EResponse xmleResponse = retriever.getApprovalFile(fileId, false);

        if (!xmleResponse.isError()) {
          ERxAuthorizationXmlParser priorAuthParser = new ERxAuthorizationXmlParser();
          EResponseProcessor.XmlStreamProcessor priorAuthXmlprocessor = new ERxResponseProcessor.XmlStreamProcessor(
              priorAuthParser);

          PriorAuthorization desc = null;
          try {
            // Digest the Prior Auth XML content.
            desc = (PriorAuthorization) priorAuthXmlprocessor.process(xmleResponse, false);
          } catch (Exception ex) {
            String errStr = "Not able to parse the received XML";
            log.error(errStr);
            continue;
          }
          if (desc.getAuthorization().getAuthorizationID()
              .equals((String) erxheaderBean.get("erx_presc_id"))) {
            erxRequestId = erxdao.searchERxPresc(recdFileName, pbmPrescId, eRxReferenceNo,
                recordCount);
            if (erxRequestId != null && !erxRequestId.trim().equals("")) {

              log.info(
                  "Found ERx Request Id :" + erxRequestId + "... with File Name: " + xmlfileName
                      + " <br/>" + "   Sender Id: " + responseSenderId + ",   Receiver Id: "
                      + responseReceiverId + ",   Transaction Date: " + transactionDate);

              boolean fileUpdate = erxdao.updateERXRequestFileId(erxRequestId, fileId);
              if (!fileUpdate) {
                String errMsg = "Error while updating PBM Request File Id: " + fileId;
                flash.error(errMsg);
                log.error(errMsg);
                return redirect;
              }

              erxFileId = fileId;
              break;
            }
          }
        }
      }
    }

    String errorMsg = null;
    if (null == erxFileId || erxFileId.trim().equals("")) {
      errorMsg = "No matching approvals available on DHPO for Download with the Prescription ID : "
          + pbmPrescIdStr;
      flash.error(errorMsg);
      log.error(errorMsg);
      return redirect;
    }

    // Use the fileId to download the data.
    EResponse xmleResponse = retriever.getApprovalFile(erxFileId, true);

    if (xmleResponse.isError()) {
      String errorMessage = xmleResponse.getErrorMessage();
      Object errorReport = xmleResponse.getErrorReport();

      if (errorMessage != null && !errorMessage.equals("")) {
        flash.error(errorMessage);
        redirect.addParameter("pbm_finalized", "N");
        return redirect;
      }
    }

    // The response is not zipped

//		EResponseProcessor.ZipStreamProcessor zipProcessor = new EResponseProcessor.ZipStreamProcessor();
    // File dataFile = File.createTempFile("tempErxXMLFile", "");
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		zipProcessor.process(xmleResponse, os);

    ERxAuthorizationXmlParser priorAuthParser = new ERxAuthorizationXmlParser();
    EResponseProcessor.XmlStreamProcessor priorAuthXmlprocessor = new ERxResponseProcessor.XmlStreamProcessor(
        priorAuthParser);

    // Digest the Prior Auth XML content.
    PriorAuthorization desc = (PriorAuthorization) priorAuthXmlprocessor.process(xmleResponse,
        false);
    // PriorAuthorization desc = (PriorAuthorization)priorAuthParser.parse(os.toString("UTF-8"));
    List<String> columns = new ArrayList<String>();
    columns.add("pbm_presc_id");
    columns.add("erx_reference_no");
    columns.add("erx_presc_id");
    columns.add("erx_center_id");
    Map<String, Object> field = new HashMap<String, Object>();
    field.put("erx_presc_id", desc.getAuthorization().getAuthorizationID());
    BasicDynaBean pbmPriorAuthBean = pbmprescdao.findByKey(columns, field);

    if (pbmPriorAuthBean == null) {
      String errMsg = "Invalid Auth. ID (or) No ERx Request exists with ID: "
          + desc.getAuthorization().getAuthorizationID();
      flash.error(errMsg);
      log.error(errMsg);
      return redirect;
    }

    // Need to check if ERx Prior Auth. of Prescription is valid or not and then save details.
    errorMsg = (desc == null)
        ? "Prior Auth XML parsing failed: Incorrectly formatted values supplied"
        : priorAuthParser.validatePriorAuthorizationXml(desc, pbmPriorAuthBean, insuCompId, tpaId,
            centerId);
    if (!errorMsg.equals("")) {
      flash.error(errorMsg);
      log.error(errorMsg);
      return redirect;
    }

//		String errorMsg = null;
    boolean success = erxdao.updateERxApprovalDetails(desc);
    if (!success) {
      errorMsg = "Error while updating ERx Prior Authorization approval status for ERx Request Id: "
          + erxRequestId;
      flash.error(errorMsg);
      log.error(errorMsg);
      return redirect;
    }

    String msg = "Downloaded and updated ERx Prior Auth. Approval details with ";
    msg += "<br/> ERx Request Id: " + erxRequestId;
    flash.info(msg);
    redirect.addParameter("pbm_finalized", "N");
    return redirect;
  }
}
