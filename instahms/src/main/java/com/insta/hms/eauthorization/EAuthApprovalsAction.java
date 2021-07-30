/**
 *
 */

package com.insta.hms.eauthorization;

import com.bob.hms.common.CenterHelper;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.eauthorization.priorauth.EAuthAuthorizationXmlParser;
import com.insta.hms.eauthorization.priorauth.EAuthXmlFilesParser;
import com.insta.hms.eauthorization.priorauth.PriorAuthorization;
import com.insta.hms.eauthorization.priorauth.XMLFile;
import com.insta.hms.eauthorization.priorauth.XMLFiles;
import com.insta.hms.eservice.EResponse;
import com.insta.hms.eservice.EResponseProcessor;
import com.insta.hms.eservice.EResult;
import com.insta.hms.integration.insurance.InsuranceCaseDetails;
import com.insta.hms.integration.insurance.InsurancePlugin;
import com.insta.hms.integration.insurance.InsurancePluginManager;
import com.insta.hms.integration.insurance.PriorAuthRequestResults;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.pbmauthorization.PriorAuthorizationHelper;
import com.insta.hms.pbmauthorization.generated.Webservices;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
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
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceClient;

/**
 * The Class EAuthApprovalsAction.
 *
 * @author lakshmi
 */
public class EAuthApprovalsAction extends BaseAction {

  /**
   * The log.
   */
  static Logger log = LoggerFactory.getLogger(EAuthApprovalsAction.class);

  /**
   * The e auth presc DAO.
   */
  private static EAuthPrescriptionDAO eauthPrescDAO;

  /**
   * The eauthapprdao.
   */
  private static EAuthApprovalsDAO eauthapprdao;

  /**
   * The prior auth helper.
   */
  private static PriorAuthorizationHelper priorAuthHelper;

  /**
   * The preauth req app DAO.
   */
  private static GenericDAO preauthReqAppDAO;

  /**
   * Instantiates a new e auth approvals action.
   */
  public EAuthApprovalsAction() {
    eauthPrescDAO = new EAuthPrescriptionDAO();
    eauthapprdao = new EAuthApprovalsDAO();
    priorAuthHelper = new PriorAuthorizationHelper();
    preauthReqAppDAO = new GenericDAO("preauth_request_approval_details");
  }

  /**
   * Gets the approvals.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the approvals
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  @SuppressWarnings("unchecked")
  @IgnoreConfidentialFilters
  public ActionForward getApprovals(ActionMapping mapping, ActionForm fm,
                                    HttpServletRequest request, HttpServletResponse response)
      throws SQLException, Exception {

    Integer userCenterId = RequestContext.getCenterId();
    String errorMsg = CenterHelper.authenticateCenterUser(userCenterId);
    if (errorMsg != null) {
      request.setAttribute("errorMsg", errorMsg);
      return mapping.findForward("list");
    }

    Map<Object, Object> map = getParameterMap(request);
    JSONSerializer js = new JSONSerializer().exclude("class");
    PagedList list = eauthapprdao.searchEAuthApprovalList(map,
        ConversionUtils.getListingParameter(map));
    request.setAttribute("pagedList", list);

    request.setAttribute("insCompList",
        js.serialize(ConversionUtils
            .listBeanToListMap(new InsuCompMasterDAO().listAll(null,
                "status", "A", "insurance_co_name"))));

    request.setAttribute("insCategoryList",
        js.serialize(ConversionUtils.listBeanToListMap(
            new GenericDAO("insurance_category_master").listAll(
                null, "status", "A", "category_name"))));

    request.setAttribute("tpaList",
        js.serialize(ConversionUtils
            .listBeanToListMap(new GenericDAO("tpa_master")
                .listAll(null, "status", "A", "tpa_name"))));
    
    return mapping.findForward("list");
  }

  /**
   * Gets the e authorization approval.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the e authorization approval
   * @throws SQLException the SQL exception
   * @throws Exception    the exception
   */
  public ActionForward getEAuthorizationApproval(ActionMapping mapping,
                                                 ActionForm fm, HttpServletRequest request,
                                                 HttpServletResponse response) throws SQLException,
      Exception {

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("listRedirect"));
    // redirect.addParameter("preauth_status", "S");
    // redirect.addParameter("preauth_status", "D");
    // redirect.addParameter("preauth_status", "C");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    String message = "No Transactions found to download.";
    String errStr = null;

    Integer centerId = RequestContext.getCenterId();
    BasicDynaBean centerMaster = new CenterMasterDAO()
        .findByKey("center_id", centerId);
    if (null == centerMaster) {
      errStr = "Unknown Center.";
      flash.error(errStr);
      return redirect;
      // Forward to errors page.
    }
    String shafafiyaEAuthActive = (String) centerMaster
        .get("shafafiya_preauth_active");

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
      flash.error(errStr);
      return redirect;
    }
    HashMap<String, String> configMap = new HashMap<String, String>();
    configMap.put("userName", userName);
    configMap.put("passWord", passWord);
    configMap.put("disposition", shafafiyaEAuthActive);
    plugin.setConfiguration(configMap);

    FileOutputStream dataOutPutStream = null;
    try {
      PriorAuthRequestResults priorAuthResult = plugin
          .priorAuthApprovalFileList();
      EResponse eauthResponse = new EAuthResponse(
          priorAuthResult.getTxnResult().value,
          priorAuthResult.getErrorMessage().value,
          priorAuthResult.getXmlTransactions().value);
      if (eauthResponse.isError()) {
        String errorMessage = eauthResponse.getErrorMessage();
        if (errorMessage != null && !errorMessage.equals("")) {
          flash.error(errorMessage);
          return redirect;
        }
      }
      if ("".equals(priorAuthResult.getXmlTransactions().value)) {
        flash.error(message);
        return redirect;
      }
      EAuthXmlFilesParser parser = new EAuthXmlFilesParser();

      EResponseProcessor.XmlStreamProcessor xmlprocessor =
          new EResponseProcessor.XmlStreamProcessor(
              parser);

      XMLFiles xmlfiles = (XMLFiles) xmlprocessor.process(eauthResponse);
      ArrayList<XMLFile> xmlfilesList = xmlfiles.getFiles();
      boolean isZipFile;

      for (XMLFile xmlfile : xmlfilesList) {
        String xmlfileName = xmlfile.getFileName();
        String senderId = xmlfile.getSenderId();
        String receiverId = xmlfile.getReceiverId();
        String transactionDate = xmlfile.getTransactionDate();
        String isDownloaded = xmlfile.getIsDownloaded();
        isZipFile = false;

        log.debug("Prior Authorization XML file List: " + xmlfileName
            + " senderId: " + senderId + " receiverId: "
            + receiverId);

        String[] recdfileStrArr = xmlfileName.split("\\.");
        String recdFileName = recdfileStrArr[0] + ".xml";

        if (xmlfileName.endsWith(".zip")) {
          isZipFile = true;
        }

        log.debug("value of isZipFile = " + isZipFile);
        String fileId = xmlfile.getFileId();

        // Use the fileId to download the data.
        PriorAuthRequestResults xmlPAResult = plugin
            .priorAuthApprovalFile(fileId, false);
        EResponse xmleResponse = new EAuthResponse(
            xmlPAResult.getTxnResult().value,
            xmlPAResult.getErrorMessage().value,
            xmlPAResult.getFile().value);
        // EResponse xmleResponse = retriever.getApprovalFile(fileId,
        // false);

        log.info("After Downloading Transaction file with File Id: "
            + fileId + " <br/>" + "  with Error message: "
            + xmleResponse.getErrorMessage());

        if (xmleResponse.isError()) {
          String errorMessage = xmleResponse.getErrorMessage();
          if (errorMessage != null && !errorMessage.equals("")) {
            flash.error(flash.get("error") == null ? errorMessage
                : (String) flash.get("error") + ", <br/> "
                + errorMessage);
            log.error(errorMessage);
            continue;
          }
        }

        byte[] fileBytes = IOUtils
            .toByteArray(xmleResponse.getInputStream());
        // String strFileName =
        // xmleResponse.getResultParams()[0].toString();

        File dataFile = File.createTempFile("tempPriorauthXMLFile", "");
        dataOutPutStream = new FileOutputStream(dataFile);
        dataOutPutStream.write(fileBytes);

        File xmlDataFile = File
            .createTempFile("tempPriorauthXMLDataFile", "");
        FileOutputStream outputStream = new FileOutputStream(
            xmlDataFile);

        if (isZipFile) {
          String err = priorAuthHelper.unzipErrorReportFile(dataFile,
              outputStream);

          if (err != null) {
            flash.error(flash.get("error") == null ? err
                : (String) flash.get("error") + ", <br/> "
                + err);
            log.error(err);
            continue;
          }
        } else { // not zip file
          IOUtils.write(fileBytes, outputStream);
        }

        if (log.isDebugEnabled()) {
          String authXmlStr = FileUtils.readFileToString(xmlDataFile);
          log.debug("Processing PBM Prior Auth XML for fileID = "
              + fileId + ", fileName = " + xmlfileName
              + " is ... : " + authXmlStr);
        }

        try (InputStream is = new FileInputStream(xmlDataFile)) {

          /*
           * EAuthAuthorizationXmlParser priorAuthParser = new
           * EAuthAuthorizationXmlParser();
           * EResponseProcessor.XmlStreamProcessor priorAuthXmlprocessor =
           * new EResponseProcessor.XmlStreamProcessor(priorAuthParser);
           * // Digest the Prior Auth XML content. PriorAuthorization desc
           * = (PriorAuthorization)priorAuthXmlprocessor.process(
           * xmleResponse);
           */

          EAuthAuthorizationXmlParser priorAuthParser = new EAuthAuthorizationXmlParser();
          PriorAuthorization desc = (PriorAuthorization) priorAuthParser
              .parse(is);

          String eauthId = null;
          if (null != desc && !desc.getAuthorization()
              .getAuthorizationID().trim().equals("")) {
            eauthId = (String) desc.getAuthorization()
                .getAuthorizationID();
          } else {
            continue;
          }
          String recordCount = xmlfile.getRecordCount();

          BasicDynaBean eauthRequestBean = eauthPrescDAO
              .searchEAuthPrescByID(eauthId, senderId, receiverId,
                  recordCount);

          String eauthRequestId = (eauthRequestBean != null
              && eauthRequestBean.get("preauth_request_id") != null)
              ? (String) eauthRequestBean
              .get("preauth_request_id")
              : null;
          String eauthRequestFileId = (eauthRequestBean != null
              && eauthRequestBean.get("file_id") != null)
              ? (String) eauthRequestBean.get("file_id")
              : null;

          // File Id is case sensitive.
          if (eauthRequestId != null && !eauthRequestId.trim().equals("")
              && (eauthRequestFileId == null || !eauthRequestFileId
              .trim().equals(fileId.trim()))) {

            log.info("Found Prior Auth Request Id :" + eauthRequestId
                + "... with File Name: " + recdFileName + " <br/>"
                + "   Sender Id: " + senderId + ",   Receiver Id: "
                + receiverId + ",   Transaction Date: "
                + transactionDate);

            boolean fileUpdate = eauthPrescDAO
                .updateEAuthRequestFileId(eauthRequestId, fileId);
            if (!fileUpdate) {
              String errMsg = "Error while updating Prior Auth Request File Id: "
                  + fileId + " for Request Id: " + eauthRequestId;
              flash.error(flash.get("error") == null ? errMsg
                  : (String) flash.get("error") + ", <br/> "
                  + errMsg);
              log.error(errMsg);
              continue;
            }
          } else {
            continue;
          }

          // PriorAuthorization desc =
          // priorXMLProvider.getPriorAuthorizationMetaDataDescription(is);
          // PriorAuthorization desc =
          // (PriorAuthorization)priorAuthParser.parse(is);

          BasicDynaBean eauthBean = eauthPrescDAO
              .getPreauthPrescriptionBean(eauthRequestId);
          if (eauthBean == null) {
            String errMsg = "Invalid Auth. ID (or) No Request exists with ID: "
                + desc.getAuthorization().getAuthorizationID();
            flash.error(flash.get("error") == null ? errMsg
                : (String) flash.get("error") + ", <br/> "
                + errMsg);
            log.error(errMsg);
            continue;
          }

          // Need to check if Prior Auth. of Prescription is valid or not
          // and then save details.
          String errorMsg = desc == null
              ? "Prior Auth XML parsing failed: Incorrectly formatted values supplied"
              : priorAuthParser.validatePriorAuthorizationXml(desc,
              eauthBean, eauthRequestId);

          if (!errorMsg.equals("")) {
            flash.error(flash.get("error") == null ? errorMsg
                : (String) flash.get("error") + ", <br/> "
                + errorMsg);
            log.error(errorMsg);
            continue;
          }

          boolean success = eauthapprdao.updateEAuthApprovalDetails(desc, fileId);
          if (!success) {
            errorMsg = "Error while updating Prior Authorization approval details.";
            flash.error(flash.get("error") == null ? errorMsg
                : (String) flash.get("error") + ", <br/> "
                + errorMsg);
            log.error(errorMsg);
            continue;
          }

          // After updating the approval details, mark the fileId as
          // downloaded.
          PriorAuthRequestResults xmllPAResult = plugin
              .priorAuthApprovalFile(fileId, true);
          xmleResponse = new EAuthResponse(
              xmllPAResult.getTxnResult().value,
              xmllPAResult.getErrorMessage().value,
              xmllPAResult.getFile().value);

          // xmleResponse = retriever.getApprovalFile(fileId, true);
          log.info("After Marking as downloaded Prior Auth Request Id :"
              + eauthRequestId + "... with File Id: " + fileId
              + " <br/>" + "  with Error message: "
              + xmleResponse.getErrorMessage());

          if (xmleResponse.isError()) {
            String errorMessage = xmleResponse.getErrorMessage();
            if (errorMessage != null && !errorMessage.equals("")) {
              flash.error(flash.get("error") == null ? errorMessage
                  : (String) flash.get("error") + ", <br/> "
                  + errorMessage);
              log.error(errorMessage);
              continue;
            }
          }

          message = "Downloaded and updated Prior Authorization Approval details.";
        }
      }
    } catch (ConnectException connectException) {
      String err = "";
      err = "Client server is Down/Response is corrupted..... Cannot connect to "
          + plugin.getWebservicesHost();
      log.error(err);
      flash.info(err);
      return redirect;
    } finally {
      if (dataOutPutStream != null) {
        dataOutPutStream.close();
      }
    }
    flash.info(message);
    return redirect;
  }

  /**
   * Sets the transaction downloaded.
   *
   * @param mapping  the mapping
   * @param fm       the fm
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward setTransactionDownloaded(ActionMapping mapping,
                                                ActionForm fm, HttpServletRequest request,
                                                HttpServletResponse response) throws Exception {

    String eauthRequestId = request.getParameter("preauth_request_id");

    FlashScope flash = FlashScope.getScope(request);
    ActionRedirect redirect = new ActionRedirect(
        mapping.findForward("listRedirect"));
    // redirect.addParameter("preauth_status", "S");
    // redirect.addParameter("preauth_status", "D");
    // redirect.addParameter("preauth_status", "C");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    String message = "No Transaction file found to download.";
    HttpSession session = request.getSession(false);
    String errStr = null;

    String serviceUser = (String) session
        .getAttribute("shafafiya_preauth_user");
    String servicePwd = (String) session
        .getAttribute("shafafiya_preauth_password");
    String activeMode = (String) session
        .getAttribute("shafafiya_preauth_active");
    boolean serviceTesting = (activeMode.equals("N"));

    EAuthApprovalsRetriever retriever = new EAuthApprovalsRetriever(
        serviceUser, servicePwd, serviceTesting);

    try {
      if (serviceTesting) {
        retriever.getTestWebService();
      } else {
        retriever.getWebService();
      }

    } catch (Exception exception) {
      errStr = "Shafafiya Server Connection error: "
          + "System Downtime(Provider, Daman, Shafafiya). <br/>  Cannot connect to "
          + Webservices.class.getAnnotation(WebServiceClient.class)
          .targetNamespace()
          + " <br/> ERROR : " + exception.getMessage();
      flash.error(errStr);
      return redirect;
    }

    FileOutputStream dataOutPutStream = null;
    try {
      BasicDynaBean eauthBean = eauthPrescDAO
          .getPreauthPrescriptionBean(eauthRequestId);
      if (eauthBean == null) {
        String errMsg = "Invalid Auth. ID (or) No Request exists with ID: "
            + eauthRequestId;
        flash.error(errMsg);
        return redirect;
      }

      String fileId = null;
      String filename = null;
      BasicDynaBean preauthReqAppBean = preauthReqAppDAO
          .findByKey("preauth_request_id", eauthRequestId);
      if (preauthReqAppBean != null) {
        fileId = preauthReqAppBean.get("file_id") != null
            ? (String) preauthReqAppBean.get("file_id")
            : null;
        filename = preauthReqAppBean.get("file_name") != null
            ? (String) preauthReqAppBean.get("file_name")
            : null;
      }

      if (fileId == null || fileId.trim().equals("")) {
        String errMsg = "No File ID to download Prior Authorization with Prior Auth "
            + "Request Id: " + eauthRequestId;
        flash.error(errMsg);
        return redirect;
      }

      // Use the fileId to download the data.
      EResponse xmleResponse = retriever.getApprovalFile(fileId, false);

      log.info(
          "After Downloading Transaction file with Prior Auth Request Id :"
              + eauthRequestId + "... with File Id: " + fileId
              + " <br/>" + "  with Error message: "
              + xmleResponse.getErrorMessage());

      if (xmleResponse.isError()) {
        String errorMessage = xmleResponse.getErrorMessage();
        if (errorMessage != null && !errorMessage.equals("")) {
          flash.error(errorMessage);
          return redirect;
        }
      }

      EResponseProcessor.ZipStreamProcessor zipProcessor =
          new EResponseProcessor.ZipStreamProcessor();
      ByteArrayOutputStream os = new ByteArrayOutputStream();

      byte[] fileBytes = IOUtils
          .toByteArray(xmleResponse.getInputStream());

      File dataFile = File.createTempFile("tempPBMXMLFile", "");
      dataOutPutStream = new FileOutputStream(dataFile);
      dataOutPutStream.write(fileBytes);

      File xmlDataFile = File.createTempFile("tempPBMXMLDataFile", "");
      FileOutputStream outputStream = new FileOutputStream(xmlDataFile);

      // not zip file
      IOUtils.write(fileBytes, outputStream);

      if (log.isDebugEnabled()) {
        String authXmlStr = FileUtils.readFileToString(xmlDataFile);
        log.debug("Processing PBM Prior Auth XML for PBM Request Id: "
            + eauthRequestId + ", fileID = " + fileId
            + ", fileName = " + filename + " is ... : "
            + authXmlStr);
      }

      try (InputStream is = new FileInputStream(xmlDataFile)) {
        EAuthAuthorizationXmlParser priorAuthParser = new EAuthAuthorizationXmlParser();
        EResult result = priorAuthParser.parse(is);
        PriorAuthorization desc = null;
        if (result instanceof PriorAuthorization) {
          // EResponseProcessor.XmlStreamProcessor priorAuthXmlprocessor =
          // new EResponseProcessor.XmlStreamProcessor(priorAuthParser);
          // Digest the Prior Auth XML content.
          desc = (PriorAuthorization) result; // priorAuthXmlprocessor.process(xmleResponse);
          // PriorAuthorization desc =
          // (PriorAuthorization)priorAuthParser.parse(os.toString("UTF-8"));
        }
        // Need to check if Prior Auth. of Prescription is valid or not and
        // then save details.
        String errorMsg = desc == null
            ? "Prior Auth XML parsing failed: Incorrectly formatted values supplied"
            : priorAuthParser.validatePriorAuthorizationXml(desc,
            eauthBean, eauthRequestId);

        if (!errorMsg.equals("")) {
          flash.error(errorMsg);
          log.error(errorMsg);
          return redirect;
        }

        boolean success = eauthapprdao.updateEAuthApprovalDetails(desc, fileId);
        if (!success) {
          errorMsg = "Error while updating Prior Authorization approval details.";
          flash.error(errorMsg);
          log.error(errorMsg);
          return redirect;
        }

        // After updating the approval details, mark the fileId as
        // downloaded.
        xmleResponse = retriever.getApprovalFile(fileId, true);

        log.info("After Marking as downloaded Prior Auth Request Id :"
            + eauthRequestId + "... with File Id: " + fileId + " <br/>"
            + "  with Error message: "
            + xmleResponse.getErrorMessage());

        if (xmleResponse.isError()) {
          String errorMessage = xmleResponse.getErrorMessage();
          if (errorMessage != null && !errorMessage.equals("")) {
            flash.error(errorMessage);
            return redirect;
          }
        }
      }
    } catch (Exception exception) {
      log.error("Exception occurred due to: " + exception.getMessage());
    } finally {
      if (dataOutPutStream != null) {
        dataOutPutStream.close();
      }
    }
    message = "Downloaded and updated Prior Authorization Approval details with Request Id: "
        + eauthRequestId;

    flash.info(message);
    return redirect;
  }
}
