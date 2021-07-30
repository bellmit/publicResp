package com.insta.hms.integration.insurance.remittance;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.insurance.InsuranceService;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.insurance.RemittanceAdvice;
import com.insta.hms.insurance.RemittanceAdviceActivity;
import com.insta.hms.insurance.RemittanceAdviceClaim;
import com.insta.hms.insurance.RemittanceAdviceHeader;
import com.insta.hms.insurance.RemittanceAdviceResubmission;
import com.insta.hms.integration.insurance.ClaimContext;
import com.insta.hms.integration.insurance.ClaimReference;
import com.insta.hms.integration.insurance.ClaimRemittance;
import com.insta.hms.integration.insurance.InsuranceCaseDetails;
import com.insta.hms.integration.insurance.InsurancePlugin;
import com.insta.hms.integration.insurance.InsurancePluginManager;
import com.insta.hms.integration.insurance.RemittanceFilter;
import com.insta.hms.integration.insurance.submission.ClaimSubmissionsService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.BeanConversionService;
import com.insta.hms.mdm.accounting.AccountingGroupService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.tpacenters.TpaCenterService;
import com.insta.hms.mdm.tpas.TpaService;
import com.insta.hms.pbmauthorization.PriorAuthXmlFileFormatProvider;
import com.insta.hms.pbmauthorization.XMLFile;
import com.insta.hms.pbmauthorization.XMLFiles;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.digester.Digester;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The Class RemittanceService that handles all remittance related tasks.
 * 
 * 
 * @author junaid.a
 */

@Service
public class RemittanceService implements BeanConversionService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(RemittanceService.class);

  /** The gen pref service. */
  @LazyAutowired
  private GenericPreferencesService genPrefService;

  /** The insurance remittance repository. */
  @LazyAutowired
  private InsuranceRemittanceRepository insuranceRemittanceRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;
  /** The insurance remittance details repository. */
  @LazyAutowired
  private InsuranceRemittanceDetailsRepository irdRepository;

  /** The insurance remittance activity details repository. */
  @LazyAutowired
  private InsuranceRemittanceActivityDetailsRepository iradRepository;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The tpa service. */
  @LazyAutowired
  private TpaService tpaService;

  /** The accountingGroup service. */
  @LazyAutowired
  private AccountingGroupService agService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The insurance service. */
  @LazyAutowired
  private InsuranceService insuranceService;

  /** The sales service. */
  @LazyAutowired
  private SalesService salesService;

  /** The claim submissions service. */
  @LazyAutowired
  private ClaimSubmissionsService claimSubmissionsService;

  /** The job service. */
  @LazyAutowired
  private JobService jobService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The insurance comapny service. */
  @LazyAutowired
  private InsuranceCompanyService insuranceComapnyService;

  /** The tpa center service. */
  @LazyAutowired
  private TpaCenterService tpaCenterService;

  /** The account group service. */
  @LazyAutowired
  private AccountingGroupService accountGroupService;

  @LazyAutowired
  private RemittanceHistoryService remittanceHistoryService;

  /** The xmlfileprovider. */
  private PriorAuthXmlFileFormatProvider xmlfileprovider = new PriorAuthXmlFileFormatProvider();

  /**
   * Inserts XML into DB. claims level details go into insurance_remittance_details, and activities
   * into insurance_remittance_activity_details
   *
   * @param remittanceId the remittance id
   * @param centerID     the center ID
   * @param isr          the isr
   * @param digester     the digester
   * @param desc         the desc
   * @throws ParseException the parse exception
   */
  public void insertXML(Integer remittanceId, Integer centerID, BOMInputStream isr,
      Digester digester, RemittanceAdvice desc) throws ParseException {
    ArrayList<RemittanceAdviceClaim> claimsList = new ArrayList<RemittanceAdviceClaim>();
    claimsList.addAll(desc.getClaim());
    // Insert Claims into insurance_remittance_details
    List<BasicDynaBean> remittanceDetails = new ArrayList<>();
    List<BasicDynaBean> remittanceActivityDetails = new ArrayList<>();

    for (RemittanceAdviceClaim remittanceAdviceClaim : claimsList) {
      RemittanceAdviceResubmission resubmission;
      BasicDynaBean insuranceRemittanceDetailsBean = irdRepository.getBean();
      resubmission = remittanceAdviceClaim.getResubmission();
      // Set bean values
      insuranceRemittanceDetailsBean.set("claim_id", remittanceAdviceClaim.getClaimID());
      insuranceRemittanceDetailsBean.set("settlement_date",
          DateUtil.stringToTimestamp(remittanceAdviceClaim.getDateSettlement()));
      insuranceRemittanceDetailsBean.set("payer_id", remittanceAdviceClaim.getIdPayer());
      insuranceRemittanceDetailsBean.set("payment_reference",
          remittanceAdviceClaim.getPaymentReference());
      insuranceRemittanceDetailsBean.set("provider_id", remittanceAdviceClaim.getProviderID());
      insuranceRemittanceDetailsBean.set("remittance_id", remittanceId);
      if (resubmission != null) {
        insuranceRemittanceDetailsBean.set("resubmission_type", resubmission.getType());
        insuranceRemittanceDetailsBean.set("resubmission_comments", resubmission.getComment());
      }
      remittanceDetails.add(insuranceRemittanceDetailsBean);

      // insert activities into insurance_remittance_activity_details
      ArrayList<RemittanceAdviceActivity> activityList;
      activityList = remittanceAdviceClaim.getActivities();
      for (RemittanceAdviceActivity activity : activityList) {
        BasicDynaBean insuranceRemittanceActivityDetailsBean = iradRepository.getBean();

        // insert corresponding remittance claim id which this activity is a part of
        insuranceRemittanceActivityDetailsBean.set("start_date",
            DateUtil.stringToTimestamp(activity.getStart()));
        insuranceRemittanceActivityDetailsBean.set("denial_remarks", activity.getDenialRemarks());
        insuranceRemittanceActivityDetailsBean.set("denial_code", activity.getActivityDenialCode());
        insuranceRemittanceActivityDetailsBean.set("activity_id", activity.getActivityID());
        insuranceRemittanceActivityDetailsBean.set("code_type", activity.getType());
        insuranceRemittanceActivityDetailsBean.set("code", activity.getCode());
        insuranceRemittanceActivityDetailsBean.set("quantity", activity.getQuantity());
        insuranceRemittanceActivityDetailsBean.set("clinician", activity.getClinician());
        insuranceRemittanceActivityDetailsBean.set("payment_amount", activity.getPaymentAmount());
        // reference to insurance_remittance_details entry
        insuranceRemittanceActivityDetailsBean.set("claim_id", remittanceAdviceClaim.getClaimID());
        insuranceRemittanceActivityDetailsBean.set("remittance_id",
            (Integer) insuranceRemittanceDetailsBean.get("remittance_id"));
        // Add activities to insurance_remittance_activity_details and
        // insurance_remittance_details
        remittanceActivityDetails.add(insuranceRemittanceActivityDetailsBean);
      }
    }
    irdRepository.batchInsert(remittanceDetails);
    iradRepository.batchInsert(remittanceActivityDetails);
  }

  /**
   * Extract the first xml file from given zip Multipart File.
   *
   * @param zip the zip
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private File extractXmlFromZip(MultipartFile zip) throws IOException {
    byte[] buffer = new byte[1024];
    ZipInputStream zis = new ZipInputStream(zip.getInputStream());
    ZipEntry zipEntry = zis.getNextEntry();
    while (zipEntry != null) {
      String fileName = zipEntry.getName();
      if (fileName.toUpperCase().endsWith(".XML")) {
        File newFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        } catch (IOException exception) {
          logger.error("", exception);
        }
        return newFile;
      }
      zipEntry = zis.getNextEntry();
    }
    zis.closeEntry();
    zis.close();
    return null;
  }

  /**
   * Processes the remittance upload and starts the remittance job if validate sucessfully. This
   * method is shared by both manual remittance upload and auto process.
   *
   * @param parameters the parameters
   * @param file       the file
   * @param centerId   the center id
   * @return the string
   * @throws SAXException   the SAX exception
   * @throws ParseException the parse exception
   * @throws IOException    Signals that an I/O exception has occurred.
   */
  public String create(Map<String, String[]> parameters, MultipartFile file, int centerId,
      String schema) throws SAXException, ParseException, IOException {

    XMLRemittanceDigester xmlRemittanceDigester = new XMLRemittanceDigester();
    BasicDynaBean requestBean = toBean(parameters);
    String centerOrAccGroup = null;
    // We use BOMinputstream to exclude Byte order mark that is sometimes included
    // during UTF encoding
    BOMInputStream isr = null;
    String fileName = file.getOriginalFilename();
    if (fileName.equals("")) {
      fileName = file.getName();
    }
    // Check if file is a zip file
    if (fileName.endsWith(".zip")) {
      File extractedFile = extractXmlFromZip(file);
      if (extractedFile != null) {
        isr = new BOMInputStream(new FileInputStream(extractedFile));
      }
    } else {
      isr = new BOMInputStream(file.getInputStream());
    }

    // validate xml header and insert xml into db
    Digester digester = xmlRemittanceDigester.getDigester(centerId, centerService);
    // RemittanceId of the xml once inserted
    int remitId = 0;
    String msg = "";
    if (digester == null) {
      msg = "Unknown Health authority cannot parse the remittance advice file. ";
      return msg;
    } else {
      RemittanceAdvice desc = null;
      try {
        desc = (RemittanceAdvice) digester.parse(isr);
      } catch (Exception exception) {
        logger.error("", exception);
        msg = "Invalid XML header, please check XML tags";
        return msg;
      }
      if (desc == null) {
        msg = "File syntax not compliant with mentioned Health Authority Guidelines.";
        return msg;
      }

      // if create() is being called from manual upload screen do the following checks
      if (null != parameters.get("center_or_account_group")) {
        // setting values based on center/account group dropdown in RA Upload screen
        centerOrAccGroup = parameters.get("center_or_account_group")[0];
        RemittanceValidation.setAccountDetails(desc, centerOrAccGroup, requestBean);

        // Header validation
        msg = RemittanceValidation.validateHeaderXML(desc, requestBean, centerService, agService,
            tpaService, centerId);
        // if Header is valid && same file name hasnt already been uploaded
      }

      if ("".equals(msg)) {

        // setting the center_id value if being called online and not manually
        if (requestBean.get("center_id") == null) {
          requestBean.set("center_id", centerId);
        }
        // Insert into insurance_remittance table
        remitId = insertHeader(desc, requestBean, fileName);
        logger.debug("Header validation completed.");
        if (remitId != 0) {
          logger.debug("Remittance file inserted successfully. Attempting to start job");
          msg = "File uploaded successfully and is being processed.";
          // Insert remittance xml into db
          insertXML(remitId, centerId, isr, digester, desc);
        } else {
          msg = "Remittance advice file upload failed.";
        }
        // Register remittance processing job
        processRemittanceJob(remitId, schema);

      }
    }
    if (isr != null) {
      isr.close();
    }
    return msg;
  }

  /**
   * Inserts header level details into insurance_remittance table.
   *
   * @param desc        the desc
   * @param requestBean the request bean
   * @param fileName    the file name
   * @return the int
   * @throws ParseException the parse exception
   */
  private int insertHeader(RemittanceAdvice desc, BasicDynaBean requestBean, String fileName)
      throws ParseException {
    Integer remittanceId = insuranceRemittanceRepository.getNextSequence();

    requestBean.set("file_name", fileName);
    requestBean.set("remittance_id", remittanceId);

    // set transaction date
    ArrayList hdrz = desc.getHeader();
    RemittanceAdviceHeader adviceHeader = (RemittanceAdviceHeader) hdrz.get(0);
    if (null != adviceHeader.getTransactionDate()) {
      requestBean.set("transaction_date",
          DateUtil.stringToTimestamp(adviceHeader.getTransactionDate()));
    }

    if (null == requestBean.get("received_date") && null != adviceHeader.getTransactionDate()) {
      requestBean.set("received_date", DateUtil.stringToDate(adviceHeader.getTransactionDate()));
    } else if (null == requestBean.get("received_date")
        && null == adviceHeader.getTransactionDate()) {
      requestBean.set("received_date",
          new java.sql.Date(Calendar.getInstance().getTime().getTime()));
    }
    insuranceRemittanceRepository.insert(requestBean);
    return remittanceId;
  }

  /**
   * Updation of hospital and pharmacy charges. Handles the following cases:
   * aggregate_amt_on_remittance Generic Preference, is_recovery, other charge updates
   *
   * @param remittanceId the remittance id
   */
  public void updateCharges(Integer remittanceId) {
    // check if remittance is_recovery
    BasicDynaBean remitBean = insuranceRemittanceRepository.findByKey("remittance_id",
        remittanceId);
    String isRecovery = (String) remitBean.get("is_recovery");
    // check aggregate_amt_on_remittance preference for charge updation
    BasicDynaBean genPrefBean = genPrefService.getPreferences();
    String aggAmtRemit = (String) genPrefBean.get("aggregate_amt_on_remittance");

    // if aggAmtRemit preference is set then we added all charges received in the
    // remittance to the existing charges (no charges are overridden)
    if (aggAmtRemit.equals("Y")) {
      billService.updateAggRemitCharges(remittanceId);
      salesService.updateAggRemitCharges(remittanceId);

    } else if (isRecovery.equals("Y")) {

      billService.updateRemitRecoveryCharges(remittanceId);
      salesService.updateRemitRecoveryCharges(remittanceId);

    } else {

      billService.updateRemittanceCharges(remittanceId);
      salesService.updateRemittanceCharges(remittanceId);
    }

  }

  /**
   * Updates the item/charge statuses based on remittance.
   *
   * @param remittanceId the remittance id
   */
  public void updateStatus(Integer remittanceId) {
    billService.updateRemittanceStatusBillCharge(remittanceId);
    salesService.updateRemittanceStatus(remittanceId);
    insuranceService.updateRemittanceStatus(remittanceId);
    claimSubmissionsService.updateDenied(remittanceId);
    billService.updateRemittanceStatusForBills(remittanceId);
  }

  /**
   * Validate XML.
   *
   * @param remittanceId the remittance id
   * @return true, if successful
   */
  public boolean validateXML(Integer remittanceId) {
    boolean noErrorsExist;
    // returns true if no errors
    noErrorsExist = RemittanceValidation.validateXML(remittanceId, irdRepository, iradRepository);
    return noErrorsExist;
  }

  /**
   * Process remittance job.
   *
   * @param remitId the remit id
   * @param schema the schema
   */
  private void processRemittanceJob(int remitId, String schema) {
    if (schema == null) {
      Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
      schema = (String) sessionAttributes.get("sesHospitalId");
    }
    final String jobName = "RemittanceJob-" + remitId;
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("remitId", remitId);
    jobData.put("schema", schema);

    // Here we are going to update the Schedule processing_status.
    BasicDynaBean bean = this.getBean();
    bean.set("processing_status", "S");
    bean.set("remittance_id", remitId);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("remittance_id", remitId);
    this.update(bean, keys);

    jobService.scheduleImmediate(buildJob(jobName, RemittanceJob.class, jobData));
  }

  /**
   * For given remitId get all claim and activity level errors and warnings.
   *
   * @param remittanceId the remittance id
   * @return the list
   */
  public List<String[]> processErrors(int remittanceId) {

    List<BasicDynaBean> claimErrorList = irdRepository.getErrorClaims(remittanceId);
    List<BasicDynaBean> claimActErrorList = iradRepository.getClaimsWithActErr(remittanceId);
    List<BasicDynaBean> actErrorList = iradRepository.getActsWithErr(remittanceId);
    List<String[]> rows = new ArrayList<>();
    String lastClaimId = "";
    String claimId;
    String activityId;
    Integer claimError;
    Integer activityError;
    Integer claimWarning;
    Integer activityWarning;

    /*
     * claimErrorList contains claims that dont have any activities with an error. We process them
     * separately to avoid duplicate error messages.
     */
    for (BasicDynaBean bean : claimErrorList) {
      claimId = (String) bean.get("claim_id");
      claimError = (Integer) bean.get("claim_error");
      rows.addAll(getClaimErrors(claimError, claimId));
    }
    for (BasicDynaBean bean : claimActErrorList) {
      claimId = (String) bean.get("claim_id");
      claimError = (Integer) bean.get("claim_error");

      // to avoid processing errors already logged
      if (!lastClaimId.equals(claimId)) {
        rows.addAll(getClaimErrors(claimError, claimId));
      }
      lastClaimId = claimId;
      activityId = (String) bean.get("activity_id");
      activityError = (Integer) bean.get("activity_error");
      rows.addAll(getActivityErrors(activityError, activityId, claimId));
    }
    for (BasicDynaBean bean : actErrorList) {
      claimId = (String) bean.get("claim_id");
      activityId = (String) bean.get("activity_id");
      activityError = (Integer) bean.get("activity_error");
      rows.addAll(getActivityErrors(activityError, activityId, claimId));
    }

    // Process warnings for claims and activities
    List<BasicDynaBean> claimWarningList = irdRepository.getClaimsWithWarning(remittanceId);
    for (BasicDynaBean bean : claimWarningList) {
      claimId = (String) bean.get("claim_id");
      claimWarning = (Integer) bean.get("claim_warning");
      rows.addAll(getClaimWarnings(claimWarning, claimId));
    }

    List<BasicDynaBean> activityWarningList = iradRepository.getActivitiesWithWarning(remittanceId);
    for (BasicDynaBean bean : activityWarningList) {
      activityId = (String) bean.get("activity_id");
      claimId = (String) bean.get("claim_id");
      activityWarning = (Integer) bean.get("activity_warning");
      rows.addAll(getActivityWarnings(activityWarning, activityId, claimId));
    }

    return rows;
  }

  /**
   * Gets claim error text using the error code.
   *
   * @param errCode the err code
   * @param claimId the claim id
   * @return the claim errors
   */
  private ArrayList<String[]> getClaimErrors(int errCode, String claimId) {

    ArrayList<String[]> errMessage = new ArrayList<String[]>();

    setClaimErrMsg(errMessage, ClaimErrorType.ACTIVITY_NOT_FOUND, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.ID_PAYER_NOT_FOUND, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.ID_PROVIDER_NOT_FOUND, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.INVALID_BATCH_ID, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.INVALID_BATCH_NOT_SENT, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.INVALID_CLAIM_ID, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.DUPLICATE_PAYMENT_REF, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.NO_ID_CLAIM_FOUND, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.PAYMENT_REF_NOT_FOUND, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.INVALID_PROVIDER_ID, errCode, claimId);
    setClaimErrMsg(errMessage, ClaimErrorType.DUPLICATE_REMITTANCE_FOR_CLAIM, errCode, claimId);
    return errMessage;
  }

  /**
   * Sets the Claim err message by doing bitwise comparison of error Code with ClaimErrorType.
   *
   * @param errMessage   the err message
   * @param claimErrType the claim err type
   * @param errCode      the err code
   * @param claimId      the claim id
   */
  private void setClaimErrMsg(List<String[]> errMessage, ClaimErrorType claimErrType, int errCode,
      String claimId) {

    int errExists = claimErrType.getCode() & errCode;
    if (errExists > 0) {
      errMessage.add(new String[] { claimId, "", claimErrType.getMessage() });
    }
  }

  /**
   * Gets the activity errors.
   *
   * @param errCode the err code
   * @param actId   the act id
   * @param claimId the claim id
   * @return the activity errors
   */
  private List<String[]> getActivityErrors(int errCode, String actId, String claimId) {

    List<String[]> errMessage = new ArrayList<String[]>();

    setActErrMsg(errMessage, ActivityErrorType.ACTIVITY_ID_NOT_FOUND, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.CLINICIAN_VALUE_NOT_FOUND, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.CODE_VALUE_NOT_FOUND, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.INVALID_ACTIVITY, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.INVALID_ACTIVITY_ID, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.INVALID_DENIAL_CODE, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.INVALID_RESUB_BATCH_ID, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.INVALID_SALE_ITEM_ID, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.INVALID_START_DATE, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.NET_VALUE_NOT_FOUND, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.PAYMENT_VALUE_NOT_FOUND, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.QUANTITY_VALUE_NOT_FOUND, errCode, actId, claimId);
    setActErrMsg(errMessage, ActivityErrorType.TYPE_VALUE_NOT_FOUND, errCode, actId, claimId);
    return errMessage;
  }

  /**
   * Sets the activity err message by doing bitwise comparison of error Code with ActivityErrorType.
   * Gets the claim warnings.
   *
   * @param warningCode the warning code
   * @param claimId     the claim id
   * @return the claim warnings
   */
  private List<String[]> getClaimWarnings(Integer warningCode, String claimId) {
    List<String[]> warningMessage = new ArrayList<>();

    setClaimWarningMsg(warningMessage, ClaimWarningType.CLAIM_ID_NOT_FOUND_SKIPPED, warningCode,
        claimId);
    return warningMessage;
  }

  /**
   * Sets the claim warning message by doing bitwise comparison of error Code with ClaimWarningType.
   *
   * @param warningMessage   the warning message
   * @param claimWarningType the claim warning type
   * @param warningCode      the warning code
   * @param claimId          the claim id
   */
  private void setClaimWarningMsg(List<String[]> warningMessage, ClaimWarningType claimWarningType,
      int warningCode, String claimId) {

    int warningExists = claimWarningType.getCode() & warningCode;
    if (warningExists > 0) {
      warningMessage.add(new String[] { claimId, null, claimWarningType.getMessage() });
    }
  }

  /**
   * Gets the activity warnings.
   *
   * @param warningCode the warning code
   * @param activityId  the activity id
   * @param claimId     the claim id
   * @return the activity warnings
   */
  private List<String[]> getActivityWarnings(Integer warningCode, String activityId,
      String claimId) {
    List<String[]> warningMessage = new ArrayList<>();

    setActivityWarningMsg(warningMessage, ActivityWarningType.ACTIVITY_ID_NOT_FOUND_SKIPPED,
        warningCode, activityId, claimId);
    return warningMessage;
  }

  /**
   * Sets the activity warning message by doing bitwise comparison of error Code with
   * ActivityWarningType.
   *
   * @param warningMessage      the warning message
   * @param activityWarningType the activity warning type
   * @param warningCode         the warning code
   * @param activityId          the activity id
   * @param claimId             the claim id
   */
  private void setActivityWarningMsg(List<String[]> warningMessage,
      ActivityWarningType activityWarningType, int warningCode, String activityId, String claimId) {

    int warningExists = activityWarningType.getCode() & warningCode;
    if (warningExists > 0) {
      warningMessage.add(new String[] { claimId, activityId, activityWarningType.getMessage() });
    }
  }

  /**
   * Sets the activity err message by doing bitwise comparison of error Code with ActivityErrorType.
   *
   * @param errMessage the err message
   * @param actErrType the act err type
   * @param errCode    the err code
   * @param actId      the act id
   * @param claimId    the claim id
   */
  private void setActErrMsg(List<String[]> errMessage, ActivityErrorType actErrType, int errCode,
      String actId, String claimId) {

    int errExists = actErrType.getCode() & errCode;
    if (errExists > 0) {
      errMessage.add(new String[] { claimId, actId, actErrType.getMessage() });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.BeanConversionService#toBean(java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams) {
    return toBean(requestParams, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.BeanConversionService#toBean(java.util.Map, java.util.Map)
   */
  @Override
  public BasicDynaBean toBean(Map<String, String[]> requestParams,
      Map<String, MultipartFile> fileMap) {
    List<String> errorFields = new ArrayList<String>();
    Map<String, Object> multipartRequestParameters = new HashMap<String, Object>(requestParams);
    if (null != fileMap && !(fileMap.isEmpty())) {
      multipartRequestParameters.putAll(fileMap);
    }
    BasicDynaBean bean = insuranceRemittanceRepository.getBean();
    ConversionUtils.copyToDynaBean(multipartRequestParameters, bean, errorFields);
    return bean;
  }

  /**
   * Update claim_submissions that remittance has been received for a claim.
   *
   * @param remittanceId the remittance id
   */
  public void updateClaimRecvd(Integer remittanceId) {
    claimSubmissionsService.updateReceived(remittanceId);
  }

  /**
   * Search.
   *
   * @param parameters the parameters
   * @return the paged list
   */
  public PagedList search(Map<String, String[]> parameters) {
    Integer centerId = RequestContext.getCenterId();
    if (centerId != null && centerId != 0) {
      String[] centerIdArray = {String.valueOf(centerId)};
      parameters.put("center_id", centerIdArray);
      parameters.put("center_id@type", new String[]{"integer"});
    }
    return insuranceRemittanceRepository.getRemittanceDetails(parameters,
        ConversionUtils.getListingParameter(parameters));
  }

  /**
   * Gets the health authority tpa code.
   *
   * @param tpaId           the tpa id
   * @param healthAuthority the health authority
   * @return the health authority tpa code
   */
  // returns tpa_code associated with a specific tpa_id
  public String getHealthAuthorityTpaCode(String tpaId, String healthAuthority) {
    Map<String, Object> filter = new HashMap<String, Object>();
    if (tpaId != null && !"".equals(tpaId)) {
      filter.put("health_authority", healthAuthority);
      filter.put("tpa_id", tpaId);
      List<BasicDynaBean> resultSet = tpaService.haTpaCodeListAllBy(filter);
      if (resultSet.size() > 0) {
        return (String) (resultSet.get(0)).get("tpa_code");
      }
    }
    return null;
  }

  /**
   * Parses the given date filters in startDate and endDate and returns them as fromDate and toDate.
   *
   * @param startDate the start date
   * @param endDate   the end date
   * @return the list
   */
  private List<java.sql.Date> setDateFilter(String startDate, String endDate) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    java.sql.Date fromDate = null;
    java.sql.Date toDate = null;
    // dateList[0] = fromDate, dateList[1] = toDate
    /**
     * If we dont have any start date and end date, then we should get last 7 days records, IF we
     * have start and end date's then we should get that from params and add filters in query
     * assembler.
     * 
     */
    if ((startDate == null || startDate.trim().isEmpty())
        && (endDate == null || endDate.trim().isEmpty())) {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.DATE, -7);
      // We clear the time to get all records for that specified date
      fromDate = new java.sql.Date(calendar.getTime().getTime());
      fromDate = new java.sql.Date(DateUtil.removeTimeFromDate(fromDate).getTime());
      toDate = new java.sql.Date(Calendar.getInstance().getTime().getTime());
      toDate = new java.sql.Date(DateUtil.removeTimeFromDate(toDate).getTime());
    } else if (endDate == null || endDate.trim().isEmpty()) {
      try {
        fromDate = new java.sql.Date(dateFormat.parse(startDate).getTime());
        toDate = new java.sql.Date(Calendar.getInstance().getTime().getTime());
      } catch (ParseException parseException) {
        parseException.printStackTrace();
      }

    } else if (startDate != null && endDate != null && !startDate.trim().isEmpty()
        && !endDate.trim().isEmpty()) {
      try {
        fromDate = new java.sql.Date(dateFormat.parse(startDate).getTime());
        toDate = new java.sql.Date(dateFormat.parse(endDate).getTime());
      } catch (ParseException parseException) {
        parseException.printStackTrace();
      }
    } else if (startDate == null || startDate.trim().isEmpty()) {
      try {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        fromDate = new java.sql.Date(cal.getTime().getTime());
        toDate = new java.sql.Date(dateFormat.parse(endDate).getTime());
      } catch (ParseException parseException) {
        parseException.printStackTrace();
      }
    }
    // Adding a one day to the toDate as the DHA/HAAD takes date as until
    // i.e. the toDate in not inclusive so as to make it inclusive adding one more day.
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(toDate);
    calendar.add(Calendar.DATE, 1);
    toDate = new java.sql.Date(calendar.getTime().getTime());
    toDate = new java.sql.Date(DateUtil.removeTimeFromDate(toDate).getTime());

    List<java.sql.Date> dateList = new ArrayList<>();
    dateList.add(fromDate);
    dateList.add(toDate);
    return dateList;
  }

  /**
   * Radownloadlist.
   *
   * @param parameters the parameters
   * @return the paged list
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   * @throws SQLException the SQL exception
   */
  public PagedList radownloadlist(Map<String, String[]> parameters, Integer centerId)
      throws IOException, SAXException, SQLException {

    String startDate = null;
    String endDate = null;
    // defines whether we want new files or files that have already been downloaded
    String fileType = null;
    String tpaId = null;
    String accountGroupId = null;
    String[] processingStatus = null;
    String transactionFileName = null;
    if (parameters.get("received_start_date") != null) {
      startDate = parameters.get("received_start_date")[0];
    }
    if (parameters.get("received_end_date") != null) {
      endDate = parameters.get("received_end_date")[0];
    }
    if (parameters.get("status") != null) {
      fileType = parameters.get("status")[0];
    }
    if (parameters.get("primary_sponsor_id") != null) {
      tpaId = parameters.get("primary_sponsor_id")[0];
    }
    if (parameters.get("processing_status") != null) {
      processingStatus = parameters.get("processing_status");
    }
    if (parameters.get("account_group_id") != null) {
      accountGroupId = parameters.get("account_group_id")[0];
    } else {
      accountGroupId = "1";// setting to hospital account group
    }
    if (parameters.get("rm_file_name") != null) {
      transactionFileName = parameters.get("rm_file_name")[0];
    }
    String serviceRegNo;

    Map filterMap = new HashMap<>();
    filterMap.put("account_group_id", accountGroupId);
    BasicDynaBean accountGroupBean = accountGroupService.findByPk(filterMap);
    if (centerId == null) {
      Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
      centerId = (Integer) sessionAttributes.get("centerId");
    }
    BasicDynaBean centerBean = centerService.getBean();
    Map keyMap = new HashMap<String, Integer>();
    keyMap.clear();
    keyMap.put("center_id", centerId);
    centerBean = centerService.findByPk(keyMap);
    if (accountGroupId.equals("1")) {
      serviceRegNo = (String) centerBean.get("hospital_center_service_reg_no");
    } else {
      serviceRegNo = (String) accountGroupBean.get("account_group_service_reg_no");
    }
    // parse and set the date filter to right format
    // datelist[0] is startDate and dateList[1] is the endDate
    List<java.sql.Date> dateList = setDateFilter(startDate, endDate);
    List<Remittance> remittancefilesList = new ArrayList<Remittance>();
    Boolean newTransactionsOnly = false;
    // can potentially have two values, new or downloaded
    if (fileType != null && ("new").equals(fileType) && !"".equals(fileType)) {
      newTransactionsOnly = true;
    }
    String testing = isRaTesting(centerBean);
    String healthAuthority = (String) centerBean.get("health_authority");
    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    String msg = "Unknown Health authority";
    if (plugin != null) {
      /*
       * Validate that the date difference is not greater than 60 days. Shafafiya and Eclaim do not
       * support more than 90 days. This is just a safety measure
       */
      if (DateUtil.compareDateDayDifference(dateList.get(0), dateList.get(1), 60) < 0) {
        String errMsg = "From and To date are more than 60 days apart.";
        logger.error(errMsg);
        throw new ValidationException(errMsg);
      }
      ClaimContext claimContext = plugin.getClaimContext();

      // set credentials into claimContext based on which account group ID
      setClaimContextCredentials(claimContext, accountGroupId, centerBean);

      claimContext.put("center_id", centerId);
      claimContext.put("eclaim_testing", testing);
      SimpleDateFormat timeStampFormatterSecs = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

      String transactionFromDate = timeStampFormatterSecs.format(dateList.get(0));
      String transactionToDate = timeStampFormatterSecs.format(dateList.get(1));

      RemittanceFilter filter = new RemittanceFilter();
      filter.setTransactionFromDate(transactionFromDate);
      filter.setTransactionToDate(transactionToDate);
      filter.setTransactionFileName(transactionFileName);
      ClaimReference reference = new ClaimReference();
      ClaimRemittance claimRemittance = new ClaimRemittance();

      // Send request to HAAD/DHA with set filters
      claimRemittance = raListRequest(claimRemittance, plugin, filter, reference, claimContext,
          newTransactionsOnly);

      if (claimRemittance != null) {
        String errMsg = "";
        if (claimRemittance.getErrorMessage() != null) {
          errMsg = claimRemittance.getErrorMessage().value;
        }
        if (claimRemittance.getTxnResult() != null && errMsg != null && !errMsg.equals("")
            && (claimRemittance.getTxnResult().value < 0
                || claimRemittance.getTxnResult().value == 2)) {

          logger.error(errMsg);
        }
        // Get the file list xml
        if (null != claimRemittance.getXmlTransactions()
            && null != claimRemittance.getXmlTransactions().value
            && !"".equals(claimRemittance.getXmlTransactions().value)) {
          StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          builder.append("\n");
          StringWriter writer = new StringWriter();
          IOUtils.copy(getResultStream(claimRemittance.getXmlTransactions().value), writer,
              ("UTF-8"));
          builder.append(writer.toString());
          logger.debug("xml received from web service = " + builder.toString());
          // If no errors then get Auth XMLs
          String foundTxnsXMLStr = builder.toString();
          XMLFiles xmlfiles = xmlfileprovider.getPriorAuthXmlFileFormatMetaData(foundTxnsXMLStr);
          ArrayList<XMLFile> xmlfilesList = xmlfiles != null ? xmlfiles.getFiles() : null;
          Remittance rem = null;

          // filter based on sender_id (tpa_code)
          String tpaCode = getHealthAuthorityTpaCode(tpaId, healthAuthority);

          // get the processing status map for the files that have previously been processed
          Map<String, String> statusMap = getProcessingStatus(xmlfilesList);

          for (XMLFile xmlfile : xmlfilesList) {
            // return only files that match the given tpaCode
            if ((tpaCode == null || xmlfile.getSenderId().equals(tpaCode))
                && xmlfile.getReceiverId().equals(serviceRegNo)
                && filterByProcessingStatus(statusMap.get(xmlfile.getFileId()), processingStatus)) {
              rem = new Remittance();
              logger.info("\n\n\n" + xmlfile.getFileId() + "--" + xmlfile.getFileName() + "--"
                  + xmlfile.getSenderId() + "--" + xmlfile.getReceiverId() + "--"
                  + xmlfile.getTransactionDate() + "--" + xmlfile.getRecordCount() + "--"
                  + xmlfile.getIsDownloaded());
              rem.setFileId(xmlfile.getFileId());
              rem.setFileName(xmlfile.getFileName());
              if (statusMap != null && statusMap.containsKey(xmlfile.getFileId())) {
                rem.setProcessingStatus(statusMap.get(xmlfile.getFileId()));
              } else {
                rem.setProcessingStatus("N");
              }
              rem.setAccountGroupId(Integer.parseInt(accountGroupId));
              rem.setIsDownloaded(xmlfile.getIsDownloaded());
              rem.setReceiverId(xmlfile.getReceiverId());
              rem.setRecordCount(xmlfile.getRecordCount());
              rem.setSenderId(xmlfile.getSenderId());
              BasicDynaBean tpaInfo = insuranceRemittanceRepository
                  .getTPAInfo(xmlfile.getSenderId(), healthAuthority);
              rem.setTpaName(tpaInfo != null ? (String) tpaInfo.get("tpa_name") : "");
              rem.setTpaId(tpaInfo != null ? (String) tpaInfo.get("tpa_id") : "");
              rem.setTransactionDate(xmlfile.getTransactionDate());
              remittancefilesList.add(rem);
            }
          }
        }
      }
    }
    return new PagedList(remittancefilesList, remittancefilesList.size(), 20, 1);
  }

  /**
   * Filters the remittance files by processing status. Processing status array[] comes from the
   * search dashboard We check if the remittances have been processed before and if their status
   * matches the processingStatus from search filter. if filesprocessingStatus is null. this
   * indicates the file has not been processed before and is not to be filtered by the search
   * filter.
   *
   * @param filesProcessingStatus the files processing status
   * @param processingStatus      the processing status
   * @return true, if successful
   */
  private boolean filterByProcessingStatus(String filesProcessingStatus,
      String[] processingStatus) {

    if (processingStatus == null) {
      return true;
    }
    for (int i = 0; i < processingStatus.length; i++) {
      if (processingStatus[i].isEmpty() || processingStatus[i].equals(filesProcessingStatus)
          || (filesProcessingStatus == null && processingStatus[i].equals("N"))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Takes the list of XML Files. Using the list of fileIds in the XML file list we get a list of
   * the associated processing statuses.
   * 
   * @param xmlfilesList the xmlfiles list
   * @return a map of fileId,processing status
   */
  private Map<String, String> getProcessingStatus(ArrayList<XMLFile> xmlfilesList) {
    Map<String, String> statusMap = new HashMap<>();
    List<String> fileIdList = new ArrayList<>();
    for (XMLFile xmlfile : xmlfilesList) {
      fileIdList.add(xmlfile.getFileId());
    }
    if (!fileIdList.isEmpty()) {
      List<BasicDynaBean> beanList = insuranceRemittanceRepository
          .getProcessingStatuses(fileIdList);
      for (BasicDynaBean bean : beanList) {
        statusMap.put((String) bean.get("file_id"), (String) bean.get("processing_status"));
      }
      return statusMap;
    }
    return new HashMap<>();
  }

  /**
   * Sets the correct credentials into claimContext for claim request.
   *
   * @param claimContext   the claim context provided by the insurance plugin
   * @param accountGroupId the account group id
   * @param centerBean     the center bean
   */
  private void setClaimContextCredentials(ClaimContext claimContext, String accountGroupId,
      BasicDynaBean centerBean) {
    if (accountGroupId != null && Integer.parseInt(accountGroupId) != 0
        && Integer.parseInt(accountGroupId) != 1) {
      claimContext.put("eclaim_user_id", centerBean.get("shafafiya_user_id"));
      claimContext.put("eclaim_password", centerBean.get("shafafiya_password"));
      Map filterMap = new HashMap<>();
      filterMap.put("account_group_id", accountGroupId);
      BasicDynaBean accountGroupBean = accountGroupService.findByPk(filterMap);
      claimContext.put("service_registration_number",
          accountGroupBean.get("account_group_service_reg_no"));
    } else {
      claimContext.put("eclaim_user_id", centerBean.get("ha_username"));
      claimContext.put("eclaim_password", centerBean.get("ha_password"));
      claimContext.put("service_registration_number",
          centerBean.get("hospital_center_service_reg_no"));

    }
  }

  /**
   * Ra download file.
   *
   * @param parameters the parameters
   * @return the multipart file
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public MultipartFile raDownloadFile(Map<String, String[]> parameters)
      throws SQLException, IOException {

    String fileId = null;
    String isFileDownloaded = parameters.get("is_downloaded") != null
        ? parameters.get("is_downloaded")[0]
        : null;
    if (parameters.get("fileId") != null) {
      fileId = parameters.get("fileId")[0];
    }
    String accountGroupId = null;
    if (parameters.get("account_group_id") != null) {
      accountGroupId = parameters.get("account_group_id")[0];
    } else {
      // setting to hospital account group
      accountGroupId = "1";
    }

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    String msg = "";
    Map keyMap = new HashMap<String, Integer>();
    keyMap.clear();
    keyMap.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(keyMap);
    String testing = isRaTesting(centerBean);
    String healthAuthority = (String) centerBean.get("health_authority");
    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin != null) {
      ClaimContext claimContext = plugin.getClaimContext();
      claimContext.put("center_id", centerId);
      setClaimContextCredentials(claimContext, accountGroupId, centerBean);
      claimContext.put("eclaim_testing", testing);

      // setting filter by options
      RemittanceFilter filter = new RemittanceFilter();
      filter.setFileId(fileId);
      ClaimRemittance claimRemittance = new ClaimRemittance();
      Boolean markFileAsDownloaded = false;
      // Send request to HAAD/DHA with set filters
      // If request is coming from screen with new transactions only then mark them as
      // downloaded
      if (isFileDownloaded != null && !"".equals(isFileDownloaded)
          && "N".equals(isFileDownloaded)) {
        markFileAsDownloaded = true;
      }
      claimRemittance = raDownloadRequest(claimRemittance, plugin, filter, claimContext,
          healthAuthority, markFileAsDownloaded);
      logger.info("After Downloading Transaction file" + "... with File Id: " + fileId + " <br/>"
          + "  with Error message: " + claimRemittance.getErrorMessage().value);
      msg = "";
      if (claimRemittance.getTxnResult().value < 0) {
        msg = claimRemittance.getErrorMessage().value;
        if (msg != null && !msg.equals("")) {
          logger.error(msg);
        }
      } else {
        MultipartFile xmlFile = new MockMultipartFile(claimRemittance.getFileName().value,
            getResultStream(claimRemittance.getFile().value));
        String authXmlStr = new String(claimRemittance.getFile().value);
        logger.info("Processing XML for Eclaim Remittance:, fileID = " + fileId + " is ... :\n "
            + authXmlStr);
        if (authXmlStr != null && !"".equals(authXmlStr)) {
          return xmlFile;
        }
      }
    }
    return null;
  }

  /**
   * Ra list request.
   *
   * @param claimRemittance     the claim remittance
   * @param plugin              the plugin
   * @param filter              the filter
   * @param reference           the reference
   * @param claimContext        the claim context
   * @param newTransactionsOnly the new transactions only
   * @return the claim remittance
   * @throws ConnectException the connect exception
   */
  public ClaimRemittance raListRequest(ClaimRemittance claimRemittance, InsurancePlugin plugin,
      RemittanceFilter filter, ClaimReference reference, ClaimContext claimContext,
      Boolean newTransactionsOnly) throws ConnectException {
    String msg = "";
    try {
      claimRemittance = plugin.getRemittance(null, filter, reference, claimContext,
          newTransactionsOnly);
    } catch (ConnectException connectException) {
      msg = "Client server is Down/Response is corrupted..... Cannot connect to "
          + plugin.getWebservicesHost();
      logger.error(msg);
    }
    return claimRemittance;
  }

  /**
   * Sends a request to download a remittanceFile based on the given claimContext.
   *
   * @param claimRemittance  the claim remittance
   * @param plugin           the plugin
   * @param filter           the filter
   * @param claimContext     the claim context
   * @param healthAuthority  the health authority
   * @param markAsDownloaded marks the file as downloaded
   * @return the claim remittance
   * @throws ConnectException the connect exception
   */
  public ClaimRemittance raDownloadRequest(ClaimRemittance claimRemittance, InsurancePlugin plugin,
      RemittanceFilter filter, ClaimContext claimContext, String healthAuthority,
      Boolean markAsDownloaded) throws ConnectException {
    String msg = "";
    try {
      claimRemittance = plugin.downloadFile(filter, claimContext, markAsDownloaded);
    } catch (ConnectException connectException) {
      msg = "Client server is Down/Response is corrupted..... Cannot download from "
          + plugin.getWebservicesHost();
      logger.error(msg);
    }
    return claimRemittance;
  }

  /**
   * Ra download process file.
   *
   * @param parameters the parameters
   * @return the string
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   * @throws SAXException   the SAX exception
   */
  public String raDownloadProcessFile(Map<String, String[]> parameters, Integer centerId,
      String schema)
      throws IOException, ParseException, SQLException, SAXException {
    String fileId = parameters.get("fileId") != null ? parameters.get("fileId")[0] : null;
    String fileName = parameters.get("file_name") != null ? parameters.get("file_name")[0] : "";
    // inserting fileID as it uniquely identifies a given remittance file
    parameters.put("file_id", new String[] { fileId });
    parameters.remove("fileId");

    String accountGroupId = null;
    if (parameters.get("account_group_id") != null) {
      accountGroupId = parameters.get("account_group_id")[0];
    } else {
      // setting to hospital account group
      accountGroupId = "1";
    }
    if (centerId == null) {
      Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
      centerId = (Integer) sessionAttributes.get("centerId");
    }
    BasicDynaBean centerBean = centerService.getBean();
    String msg = "";
    Map keyMap = new HashMap<String, Integer>();
    keyMap.clear();
    keyMap.put("center_id", centerId);
    centerBean = centerService.findByPk(keyMap);
    String testing = isRaTesting(centerBean);
    String healthAuthority = (String) centerBean.get("health_authority");
    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin != null) {
      ClaimContext claimContext = plugin.getClaimContext();
      claimContext.put("center_id", centerId);
      setClaimContextCredentials(claimContext, accountGroupId, centerBean);
      claimContext.put("eclaim_testing", testing);

      RemittanceFilter filter = new RemittanceFilter();
      filter.setFileId(fileId);
      ClaimRemittance claimRemittance = new ClaimRemittance();
      // Send request to HAAD/DHA with set filters
      claimRemittance = raDownloadRequest(claimRemittance, plugin, filter, claimContext,
          healthAuthority, true);
      logger.info("After Downloading Transaction file" + "... with File Id: " + fileId + " <br/>"
          + "  with Error message: " + claimRemittance.getErrorMessage().value);
      msg = "";
      if (claimRemittance.getTxnResult().value < 0) {
        msg = claimRemittance.getErrorMessage().value;
        if (msg != null && !msg.equals("")) {
          logger.error(msg);
        }
      } else {
        getResultStream(claimRemittance.getFile().value);
        MultipartFile xmlFile = new MockMultipartFile(claimRemittance.getFileName().value,
            getResultStream(claimRemittance.getFile().value));
        if (fileName.endsWith(".zip")) {
          File extractedFile = extractXmlFromZip(xmlFile);
          if (extractedFile != null) {
            MultipartFile multipartFile = new MockMultipartFile(extractedFile.getName(),
                new FileInputStream(extractedFile));
            msg = create(parameters, multipartFile, centerId, schema);
            logger.info("Processing XML for Eclaim Remittance:, fileID = " + fileId + " is ... :\n "
                + FileUtils.readFileToByteArray(extractedFile));
          }
        } else {
          msg = create(parameters, xmlFile, centerId, schema);
          String authXmlStr = new String(xmlFile.getBytes());
          logger.info("Processing XML for Eclaim Remittance:, fileID = " + fileId + " is ... :\n "
              + authXmlStr);
        }
      }
    }
    return msg;
  }

  /**
   * Checks if is being requested in testing mode.
   *
   * @param centerBean the center bean
   * @return the string
   * @throws SQLException the SQL exception
   */
  private String isRaTesting(BasicDynaBean centerBean) throws SQLException {
    String testing = "";

    if (centerBean != null && "N".equals(centerBean.get("eclaim_active"))) { // Active Mode
      testing = "Y";
    } else {
      testing = "N";
    }
    return testing;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.BeanConversionService#toBeanList(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public Map<String, List<BasicDynaBean>> toBeanList(Map<String, String[]> requestParams,
      BasicDynaBean type) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the insu comp list.
   *
   * @return the insu comp list
   */
  public List<BasicDynaBean> getInsuCompList() {
    return insuranceComapnyService.lookup(true);
  }

  /**
   * Company tpa XML list.
   *
   * @return the list
   */
  public List<BasicDynaBean> companyTpaXMLList() {
    return tpaService.companyTpaXmlList();
  }

  /**
   * Xml tpa list.
   *
   * @return the list
   */
  public List<BasicDynaBean> xmlTpaList() {
    return tpaService.xmlTpaList();
  }

  /**
   * Tpa center list.
   *
   * @return the list
   */
  public List<BasicDynaBean> tpaCenterList() {
    return tpaCenterService.tpaCenterList();
  }

  /**
   * Accountgrp and center view.
   *
   * @param userCenterId the user center id
   * @return the list
   */
  public List<BasicDynaBean> accountgrpAndCenterView(int userCenterId) {
    return accountGroupService.accountGroupCenterView(userCenterId);
  }

  /**
   * Gets the file name.
   *
   * @param remittanceId the remittance id
   * @return the file name
   */
  public BasicDynaBean getFileName(int remittanceId) {
    return insuranceRemittanceRepository.getFileName(remittanceId);
  }

  /**
   * All xml tpa list.
   *
   * @return the list
   */
  public List<BasicDynaBean> allXmlTpaList() {
    return tpaService.allXmlTpaList();
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return insuranceRemittanceRepository.getBean();
  }

  /**
   * Gets all generic preferences map.
   *
   * @return the generic preferences
   */
  public Map getGenericPreferences() {
    return genPrefService.getAllPreferences().getMap();
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return insuranceRemittanceRepository.update(bean, keys);
  }

  /**
   * Gets the result stream.
   *
   * @param content the content
   * @return the result stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private InputStream getResultStream(Object content) throws IOException {
    if (content instanceof InputStream) {
      return (InputStream) content;
    }
    if (content instanceof byte[]) {
      return toStream((byte[]) content);
    }
    if (content instanceof String) {
      return toStream((String) content);
    }
    return null;
  }

  /**
   * To stream.
   *
   * @param bytes the bytes
   * @return the input stream
   */
  private InputStream toStream(byte[] bytes) {
    return new ByteArrayInputStream(bytes);
  }

  /**
   * To stream.
   *
   * @param result the result
   * @return the input stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private InputStream toStream(String result) throws IOException {
    return new ByteArrayInputStream(result.getBytes("UTF-8"));

  }

  /**
   * Do warnings exist.
   *
   * @param remitId the remit id
   * @return true, if warnings exists for any claims given the remittance Id
   */
  public boolean doWarningsExist(Integer remitId) {
    return RemittanceValidation.doWarningsExist(remitId, irdRepository, iradRepository);
  }

  /**
   * Post remittance history.
   *
   * @param remitId the remit id
   */
  public void postRemittanceHistory(Integer remitId) {
    remittanceHistoryService.postRemittanceHistory(remitId);
  }

}
