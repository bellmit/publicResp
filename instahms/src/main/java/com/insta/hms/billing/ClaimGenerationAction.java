/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.insurance.claimhistory.ClaimHistoryService;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.integration.insurance.ClaimContext;
import com.insta.hms.integration.insurance.ClaimDocument;
import com.insta.hms.integration.insurance.ClaimSubmissionResult;
import com.insta.hms.integration.insurance.InsuranceCaseDetails;
import com.insta.hms.integration.insurance.InsurancePlugin;
import com.insta.hms.integration.insurance.InsurancePluginManager;
import com.insta.hms.jobs.JobService;
import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.pbmauthorization.PriorAuthorizationHelper;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class ClaimGenerationAction.
 *
 * @author lakshmi
 */
public class ClaimGenerationAction extends BaseAction {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ClaimGenerationAction.class);
  
  /** The submitdao. */
  ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
  
  /** The claimdao. */
  ClaimDAO claimdao = new ClaimDAO();
  
  /** The claim activity DAO. */
  ClaimActivityDAO claimActivityDAO = new ClaimActivityDAO();
  
  /** The reconcildao. */
  ClaimReconciliationDAO reconcildao = new ClaimReconciliationDAO();
  
  /** The helper. */
  ClaimGeneratorHelper helper = new ClaimGeneratorHelper();
  
  /** The gendao. */
  GenericDocumentsDAO gendao = new GenericDocumentsDAO();
  
  /** The claim activity processor. */
  ClaimActivityProcessor claimActivityProcessor = new ClaimActivityProcessor();
  
  /** The claim process. */
  ClaimProcessor claimProcess = new ClaimProcessor();
  
  /** The alternate obs codes. */
  InternalCodeObservationProcessor alternateObsCodes = new InternalCodeObservationProcessor();
  
  /** The prior auth helper. */
  private PriorAuthorizationHelper priorAuthHelper = new PriorAuthorizationHelper();

  /** The claim history service. */
  ClaimHistoryService claimHistoryService =
      ApplicationContextProvider.getBean(ClaimHistoryService.class);

  /** The Constant XML_GENERATION_IN_PROGRESS. */
  private static final String XML_GENERATION_IN_PROGRESS =
      "Generation is in progress , Please wait for the completion of action.";

  /**
   * E claim.
   *
   * @param mapping the mapping
   * @param f the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward eClaim(ActionMapping mapping, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws IOException, TemplateException, SQLException, Exception {
    String testing = req.getParameter("testing");
    String submission_batch_id = req.getParameter("submission_batch_id");
    boolean isAccumed = BooleanUtils.toBoolean(req.getParameter("isAccumed"));
    boolean isNewClaim = BooleanUtils.toBoolean(req.getParameter("New"));
    String path = req.getContextPath();
    HttpSession session = RequestContext.getSession();
    HashMap actionUrlMap = (HashMap) session.getServletContext().getAttribute("actionUrlMap");

    BasicDynaBean submissionbean =
        submitdao.findByKey("submission_batch_id", submission_batch_id);
    String processing_status = (String) submissionbean.get("processing_status");
    if ("P".equalsIgnoreCase(processing_status)) {
      req.setAttribute("error",
          "Submission Batch: " + submission_batch_id + " already scheduled");
      return mapping.findForward("reportErrors");
    }
    submissionbean.set("processing_status", "P");
    if (testing.equalsIgnoreCase("Y")) {
      submissionbean.set("processing_type", "T");
    } else {
      submissionbean.set("processing_type", "P");
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("submission_batch_id", submission_batch_id);
    submitdao.update(submissionbean.getMap(), keys);

    scheduleEclaimGeneration(submission_batch_id, testing, path, actionUrlMap, isAccumed,
        isNewClaim);

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("success"));
    flash.info(getFlashMessageForClaimGenertaion(testing, isAccumed, isNewClaim));

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("sortOrder", "created_date");
    redirect.addParameter("sortReverse", "true");
    redirect.addParameter("status", "O");
    return redirect;

  }

  /**
   * Gets the flash message for claim genertaion.
   *
   * @param testing the testing
   * @param isAccumed the is accumed
   * @param isNewClaim the is new claim
   * @return the flash message for claim genertaion
   */
  private String getFlashMessageForClaimGenertaion(String testing, boolean isAccumed,
      boolean isNewClaim) {
    StringBuilder sb = new StringBuilder();
    if (isAccumed) {
      if (isNewClaim) {
        sb.append("New Accumed ");
      } else {
        sb.append("Update Accumed ");
      }
    } else {
      if (testing.equalsIgnoreCase("Y")) {
        sb.append("TEST XML ");
      } else {
        sb.append("PRODUCTION XML ");
      }
    }
    sb.append(XML_GENERATION_IN_PROGRESS);
    return sb.toString();
  }

  /**
   * Upload E claim.
   *
   * @param mapping the mapping
   * @param f the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward uploadEClaim(ActionMapping mapping, ActionForm f,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, TemplateException, SQLException, Exception {

    String submission_batch_id = req.getParameter("submission_batch_id");
    BasicDynaBean submissionbean =
        submitdao.findByKey("submission_batch_id", submission_batch_id);

    String file_name = submissionbean.get("file_name") != null
        ? (String) submissionbean.get("file_name") + ".xml" : "eClaim.xml";
    Integer centerId = (Integer) submissionbean.get("center_id");

    BasicDynaBean centerbean = new CenterMasterDAO().findByKey("center_id", centerId);
    if (null == centerbean) {
      req.setAttribute("error", "Unknown Center.");
      // Forward to errors page.
      return mapping.findForward("reportErrors");
    }
    String processing_type = (String) submissionbean.get("processing_type");
    String healthAuthority = (String) centerbean.get("health_authority");
    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin == null) {
      req.setAttribute("error", "Unknown Health authority.");
      return mapping.findForward("reportErrors");
    }

    String xmlStr = null;
    String testing = req.getParameter("testing");
    if ("N".equals(centerbean.get("eclaim_active"))) { // Active Mode
      testing = "Y";
    } else {
      testing = "N";
    }
    logger.debug("XML file reading starts");

    File file = new File("/var/log/insta/insta-ia-sync/" + file_name);
    if (file.exists()) {
      xmlStr = FileUtils.readFileToString(file);
    } else {
      req.setAttribute("error",
          "Requested Resource No longer Available For Submission Batch : "
              + submission_batch_id);
      return mapping.findForward("reportErrors");
    }

    logger.info("ClaimGenerationAction.eClaim()\n" + xmlStr);
    logger.info("upload E-claim start...\n");

    ClaimContext claimContext = plugin.getClaimContext();
    claimContext.put("center_id", centerId);
    claimContext.put("eclaim_user_id", centerbean.get("ha_username"));
    claimContext.put("eclaim_password", centerbean.get("ha_password"));
    claimContext.put("eclaim_testing", testing);
    ClaimDocument claimDocument = new ClaimDocument();
    // set the file content as a byte format
    claimDocument.setContent(xmlStr.getBytes());
    claimDocument.setFileName(file_name);

    try {

      ClaimSubmissionResult subResult = plugin.submitClaim(claimDocument, claimContext);
      int txnResult = subResult.getUploadTxnResult().value;
      PriorAuthorizationHelper.TransactionResults txn =
          PriorAuthorizationHelper.TransactionResults.getTxnResultMessage(txnResult);
      if (txnResult >= 0) {
        String msg = txn.getResultMsg();
        logger.debug(" EClaim upload successful. " + msg);
        // Mark EClaim as Sent request. when processing_type is Production, in case of
        // processing_type == Test, don't update
        // update status column value to 'S' in both the table insurance_submission_batch &
        // claim_submissions
        if (!"T".equals(processing_type)) {
          boolean success = submitdao.updateInsuranceSubmissionBatch(submission_batch_id);
          if (!success) {
            req.setAttribute("error", "Error while marking EClaim Request as Sent.");
            // Forward to errors page.
            return mapping.findForward("reportErrors");
          } else {
            success = submitdao.updateClaimSubmission(submission_batch_id);
            if (!success) {
              req.setAttribute("error", "Error while marking EClaim Request as Sent.");
              // Forward to errors page.
              return mapping.findForward("reportErrors");
            }
          }
        }
      } else {
        String errMsg = subResult.getErrorMessage().value;
        byte[] errorReportBytes = subResult.getErrorReport().value;
        String errorReportStr = priorAuthHelper.getErrorReportbase64String(errorReportBytes); // Encoded
                                                                                              // string
        String errorFileName = file_name + "_" + DataBaseUtil.getCurrentDate();
        File decodedDataFile = File.createTempFile("tempPBMErrorReportFile", "");

        // Decode the error report into a file (this is a zip file).
        if (errorReportStr != null) {
          if ("HAAD".equals(healthAuthority)) {
            String err =
                priorAuthHelper.decodeErrorReportbase64ToFile(errorReportStr, decodedDataFile);
            if (err == null) {
              res.setContentType("application/vnd.ms-excel");
              res.setHeader("Content-disposition",
                  "attachment; filename=\"" + errorFileName + ".xls" + "\"");
              OutputStream outputStream = res.getOutputStream();
              // Read the zip file as write to output stream. The file zipped content has an
              // excel sheet.
              err = priorAuthHelper.unzipErrorReportFile(decodedDataFile, outputStream);
              if (err != null) {
                req.setAttribute("error", err);
                // Forward to errors page.
                return mapping.findForward("reportErrors");
              }
              return null;
            }
          } else {
            String err =
                priorAuthHelper.decodeErrorReportbase64ToFile(errorReportStr, decodedDataFile);
            if (err != null) {
              req.setAttribute("error", err);
              // Forward to errors page.
              return mapping.findForward("reportErrors");
            }
            res.setContentType("text/plain");
            res.setHeader("Content-disposition",
                "attachment; filename=\"" + errorFileName + ".txt" + "\"");
            OutputStream outputStream = res.getOutputStream();
            outputStream.write(Base64.decodeBase64(errorReportStr.getBytes()));
            outputStream.flush();
            outputStream.close();
            return null;
          }
        } else {
          if (errMsg == null || "".equals(errMsg)) {
            errMsg = txn.getResultMsg();
          }
        }
        req.setAttribute("error", errMsg);
        return mapping.findForward("reportErrors");
      }
    } catch (ConnectException e) {
      String msg = "Client server is Down/Response is corrupted..... Cannot connect to "
          + plugin.getWebservicesHost();
      logger.error(msg);
      req.setAttribute("error", msg);
      return mapping.findForward("reportErrors");
    }

    claimHistoryService.createClaimHistory(submission_batch_id);

    FlashScope flash = FlashScope.getScope(req);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("success"));
    flash.info("EClaim upload successful...");
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    redirect.addParameter("sortOrder", "created_date");
    redirect.addParameter("sortReverse", "true");
    redirect.addParameter("status", "O");
    return redirect;
  }

  /**
   * Show E claim error.
   *
   * @param mapping the mapping
   * @param f the f
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws SQLException the SQL exception
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward showEClaimError(ActionMapping mapping, ActionForm f,
      HttpServletRequest req, HttpServletResponse res)
      throws IOException, TemplateException, SQLException, Exception {

    String submission_batch_id = req.getParameter("submission_batch_id");
    BasicDynaBean insSubmsnBean =
        submitdao.findExistsByKey("submission_batch_id", submission_batch_id);
    String errStr = "";
    String processing_status = "";
    if (null != insSubmsnBean) {

      errStr = (String) insSubmsnBean.get("processing_error");
      processing_status = (String) insSubmsnBean.get("processing_status");
    }
    if ("F".equalsIgnoreCase(processing_status) && !"".equalsIgnoreCase(errStr)
        && null != errStr) {
      req.setAttribute("error", errStr.toString());
    } else {
      req.setAttribute("error", "No error info. found");
    }

    req.setAttribute("isEncoded", "false");
    return mapping.findForward("reportErrors");

  }

  /**
   * Schedule eclaim generation.
   *
   * @param submissionBatchId the submission batch id
   * @param testing the testing
   * @param path the path
   * @param actionUrlMap the action url map
   * @param isAccumed the is accumed
   * @param isNewClaim the is new claim
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  private void scheduleEclaimGeneration(String submissionBatchId, String testing, String path,
      HashMap actionUrlMap, boolean isAccumed, boolean isNewClaim)
      throws ParseException, SQLException, IOException {

    String centerId = RequestContext.getCenterId().toString();
    String jobName = "EclaimGenerationJob-" + submissionBatchId;
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("submission_batch_id", submissionBatchId);
    jobData.put("testing", testing);
    jobData.put("centerId", centerId);
    jobData.put("actionUrlMap", actionUrlMap);
    jobData.put("path", path);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("isAccumed", isAccumed);
    jobData.put("isNewClaim", isNewClaim);
    JobService jobService = JobSchedulingService.getJobService();
    jobService.scheduleImmediate(buildJob(jobName, ClaimGenerationJob.class, jobData));

  }
}
