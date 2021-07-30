package com.insta.hms.integration.insurance.submission;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.billing.ClaimProcessor;
import com.insta.hms.billing.Eclaim;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.UrlUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeRepository;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.StoreSalesDetailsService;
import com.insta.hms.core.clinical.diagnosisdetails.HospitalClaimDiagnosisRepository;
import com.insta.hms.core.clinical.order.chargehead.ChargeHeadOrderItemRepository;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsRepository;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.integration.insurance.ClaimContext;
import com.insta.hms.integration.insurance.ClaimDocument;
import com.insta.hms.integration.insurance.ClaimSubmissionResult;
import com.insta.hms.integration.insurance.InsuranceCaseDetails;
import com.insta.hms.integration.insurance.InsurancePlugin;
import com.insta.hms.integration.insurance.InsurancePluginManager;
import com.insta.hms.integration.insurance.VisitClassificationType;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.accounting.AccountingGroupService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesRepository;
import com.insta.hms.mdm.icdsupportedcodes.IcdSupportedCodeTypesRepository;
import com.insta.hms.mdm.mrdcodes.MrdCodeRepository;
import com.insta.hms.mdm.services.ServicesRepository;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.pbmauthorization.PriorAuthorizationHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * The Class PersonRegisterSubmissionService. Refer
 * https://practo.atlassian.net/wiki/spaces/HIMS/pages/2355920900/UAE+Market+Requirement+Person+Register+Upload
 */
@Service
public class PersonRegisterSubmissionService {

  /** The Person Register repository. */
  @LazyAutowired
  PersonRegisterSubmissionRepository personRegisterRepository;

  /** The Person Register Details repository. */
  @LazyAutowired
  PersonRegisterSubmissionDetailsRepository personRegisterDetailsRepository;

  /** The account group service. */
  @LazyAutowired
  AccountingGroupService accountGroupService;

  @LazyAutowired
  HospitalClaimDiagnosisRepository hospitalClaimDiagRepo;

  @LazyAutowired
  MRDDiagnosisService mrdDiagnosisService;

  /** The health authority preferences repository. */
  @LazyAutowired
  HealthAuthorityPreferencesRepository healthAuthorityPreferencesRepository;

  /** The services repository. */
  @LazyAutowired
  ServicesRepository servicesRepository;

  /** The gen pref service. */
  @LazyAutowired
  GenericPreferencesService genPrefService;

  /** The charge head constants repository. */
  @LazyAutowired
  ChargeHeadOrderItemRepository chargeHeadConstantsRepository;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The bill charge repository. */
  @LazyAutowired
  BillChargeRepository billChargeRepository;

  /** The job service. */
  @LazyAutowired
  JobService jobService;

  /** The store sales details service. */
  @LazyAutowired
  StoreSalesDetailsService storeSalesDetailsService;

  /** The mrd observation repository. */
  @LazyAutowired
  MRDObservationsRepository mrdObservationRepository;

  /** The mrd code support repository. */
  @LazyAutowired
  IcdSupportedCodeTypesRepository icdSupportedCodeTypesRepository;

  /** The registration preferences service. */
  @LazyAutowired
  RegistrationPreferencesService registrationPreferencesService;

  /** The center service. */
  @LazyAutowired
  CenterService centerService;

  /** The bill service. */
  @LazyAutowired
  BillService billService;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(PersonRegisterSubmissionService.class);

  /** The Constant PERSONREGISTER_BATCH_ID. */
  private static final String PERSONREGISTER_BATCH_ID = "personregister_batch_id";
  private static final String PERSONREGISTER_Details_BATCH_ID = "personregister_batch_details_id";
  private static final String URL_RIGHTS_MAP = "urlRightsMap";
  private static final String ACTION_URL_MAP = "actionUrlMap";
  private static final String GOVT_IDENTIFIER_LABEL = "government_identifier_label";
  private static final String DIAGNOSIS = "diagnosis";

  /**
   * Process person register job and schedules the job immediately.
   *
   * @param personRegisterBatchId the person register batch id
   * @param contextPath           the context path
   * @return the string
   * @throws Exception the exception which is not handled
   */
  // creates submission batch with given criteria and starts a job if visits
  public String processPersonRegisterJob(String personRegisterBatchId, String contextPath)
      throws Exception {

    // set the status of the job as processing
    BasicDynaBean personRegisterBean = personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID,
        personRegisterBatchId);
    personRegisterBean.set("processing_status", "P");// sets the status from N(not
    // scheduled) to
    // P(In-progress)
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(PERSONREGISTER_BATCH_ID, personRegisterBatchId);
    personRegisterRepository.update(personRegisterBean, filterMap);

    // set Job Data and schedule the job
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String schema = (String) sessionAttributes.get("sesHospitalId");
    HttpSession session = RequestContext.getSession();
    HashMap urlRightsMap = (HashMap) session.getAttribute(URL_RIGHTS_MAP);
    HashMap actionUrlMap = (HashMap) session.getServletContext().getAttribute(ACTION_URL_MAP);
    Map<String, Object> jobData = new HashMap<>();
    jobData.put(PERSONREGISTER_BATCH_ID, personRegisterBatchId);
    jobData.put(URL_RIGHTS_MAP, urlRightsMap);
    jobData.put(ACTION_URL_MAP, actionUrlMap);
    jobData.put("path", contextPath);
    jobData.put("schema", schema);
    String jobName = "PersonRegisterXMLJob-" + personRegisterBatchId;
    jobService.scheduleImmediate(buildJob(jobName, PersonRegisterXmlJob.class, jobData));
    return "Job successfully queued for processing.";
  }

  /**
   * Returns the account group list to be shown in the page dropdown of the person
   * register submissions batch creation screen.
   *
   * @return the map
   */
  // matching given criteria is found
  public Map<String, Object> create() {
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");

    List<BasicDynaBean> accountGrpAndCenterList = accountGroupService
        .accountGroupCenterView(centerId);

    // get request parameters
    response.put("accountGrpAndCenterList",
        ConversionUtils.listBeanToListMap(accountGrpAndCenterList));
    return response;
  }

  /**
   * Creates the submission batch in person_register_submission_batch table.
   *
   * @param parameters the parameters
   * @return the map
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> createSubmission(Map<String, String[]> parameters)
      throws SQLException, ParseException {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Map<String, Object> response = new HashMap<>();
    // end of first landing
    String msg = "No Person matched the given filter criteria.";
    String[] visitType = parameters.get("visit_type");
    String[] centerOrAccountGroup = parameters.get("center_or_account_group");

    String[] registrationDate = parameters.get("_reg_date");
    if (registrationDate.length == 0) {
      response.put("info", "Please select date range");
      return response;
    }
    String[] registrationTime = parameters.get("_reg_time");
    Date[] registrationDateArray = new Date[2];
    Date[] dischargeDateArray = new Date[2];

    if (visitType != null) {
      if ("i".equalsIgnoreCase(visitType[0])) {
        setDischargeDate(registrationDate, registrationTime, dischargeDateArray);
      } else {
        setRegistrationDate(registrationDate, registrationTime, registrationDateArray);
      }
    }

    // get details from center Id selected.
    Integer centerId = 0;
    String userId = sessionAttributes.get("userId").toString();
    String accGrpOrCenterIdStr = (centerOrAccountGroup == null
        || "".equals(centerOrAccountGroup[0])) ? "" : (centerOrAccountGroup[0]);
    Integer accountGroupId = 0;
    if (!"".equals(accGrpOrCenterIdStr)) {
      if (accGrpOrCenterIdStr.startsWith("A")) {
        accountGroupId = Integer
            .parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
        centerId = (Integer) sessionAttributes.get("centerId");
      } else if (accGrpOrCenterIdStr.startsWith("C")) {
        centerId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
      }
    }
    Map filter = setFilter(parameters, centerId, accountGroupId, registrationDateArray,
        dischargeDateArray);

    // get list of patients based on the filters given

    List<BasicDynaBean> patients = personRegisterRepository.searchMrNosForSubmission(filter);
    List<BasicDynaBean> newPatients = new ArrayList<BasicDynaBean>();

    for (BasicDynaBean patient : patients) {
      List<BasicDynaBean> registeredPatients = personRegisterRepository
          .isPatientAlreadyRegistered(patient.get("mr_no"), centerId, accountGroupId);
      if (registeredPatients.isEmpty() && registeredPatients.size() == 0) {
        newPatients.add(patient);
      }
    }
    // add mapping in patient_register_submission_batch

    if (newPatients != null && !newPatients.isEmpty()) {

      BasicDynaBean personRegisterBean = personRegisterRepository.getBean();
      Integer batchId = personRegisterRepository.getNextSequence();
      personRegisterBean.set(PERSONREGISTER_BATCH_ID, "PR" + batchId);
      personRegisterBean.set("visit_type", filter.get("visit_type"));
      personRegisterBean.set("account_group_id", accountGroupId);
      personRegisterBean.set("created_by", userId);
      personRegisterBean.set("center_id", centerId);
      if (registrationDateArray[0] != null) {
        personRegisterBean.set("batch_from_date",
            new java.sql.Timestamp(registrationDateArray[0].getTime()));
        personRegisterBean.set("batch_to_date",
            new java.sql.Timestamp(registrationDateArray[1].getTime()));
      } else if (dischargeDateArray[0] != null) {
        personRegisterBean.set("batch_from_date",
            new java.sql.Timestamp(dischargeDateArray[0].getTime()));
        personRegisterBean.set("batch_to_date",
            new java.sql.Timestamp(dischargeDateArray[1].getTime()));
      }
      personRegisterRepository.insert(personRegisterBean);
      for (BasicDynaBean patient : newPatients) {
        BasicDynaBean personRegisterDetailsBean = personRegisterDetailsRepository.getBean();
        Integer batchDetailsId = personRegisterDetailsRepository.getNextSequence();
        personRegisterDetailsBean.set(PERSONREGISTER_Details_BATCH_ID, batchDetailsId);
        personRegisterDetailsBean.set(PERSONREGISTER_BATCH_ID, "PR" + batchId);
        personRegisterDetailsBean.set("mr_no", patient.get("mr_no"));
        personRegisterDetailsRepository.insert(personRegisterDetailsBean);
      }
      msg = "Person Register Batch Successfully Created " + "PR" + batchId + " with "
          + newPatients.size() + " patients.";

    }

    response.put("info", msg);
    return response;
  }

  /**
   * Sets the registration date.
   *
   * @param registrationDate      the registration date
   * @param registrationTime      the registration time
   * @param registrationDateArray the registration date array
   * @throws ParseException the parse exception
   */
  private void setRegistrationDate(String[] registrationDate, String[] registrationTime,
      Date[] registrationDateArray) throws ParseException {
    if (registrationDate != null && !"".equals(registrationDate[0])) {

      for (int i = 0; i < registrationDate.length; i++) {
        if (!registrationDate[0].isEmpty() && registrationTime[0].isEmpty()) {
          registrationTime[0] = "00:00";
        }
        if (!registrationDate[1].isEmpty() && registrationTime[1].isEmpty()) {
          registrationTime[1] = "23:59";
        }
        registrationDateArray[i] = DateUtil.parseDate(registrationDate[i]);
      }
    }
  }

  /**
   * Sets the discharge date.
   *
   * @param dischargeDate      the discharge date
   * @param dischargeTime      the discharge time
   * @param dischargeDateArray the discharge date array
   * @throws ParseException the parse exception
   */
  private void setDischargeDate(String[] dischargeDate, String[] dischargeTime,
      Date[] dischargeDateArray) throws ParseException {
    if (dischargeDate != null && !"".equals(dischargeDate[0]) && !"".equals(dischargeDate[1])) {
      for (int i = 0; i < dischargeDate.length; i++) {
        if (!dischargeDate[0].isEmpty() && dischargeTime[0].isEmpty()) {
          dischargeTime[0] = "00:00";
        }
        if (!dischargeDate[1].isEmpty() && dischargeTime[1].isEmpty()) {
          dischargeTime[1] = "23:59";
        }
        dischargeDateArray[i] = DateUtil.parseDate(dischargeDate[i]);
      }
    }
  }
  
  /**
   * Generates the xml, this function is trigger by personregisterXMLJob.
   *
   * @param personRegisterBatchId the person register batch id
   * @param urlRightsMap          the url rights map
   * @param actionUrlMap          the action url map
   * @param path                  the path
   * @return the map
   * @throws Exception the exception
   */
  public boolean generate(String personRegisterBatchId, HashMap urlRightsMap, HashMap actionUrlMap,
      String path) throws Exception {

    Map<String, StringBuilder> errorsMap = new HashMap<>();
    BasicDynaBean submissionbean = personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID,
        personRegisterBatchId);
    Integer centerId = (Integer) submissionbean.get("center_id");
    BasicDynaBean centerBean = centerService.findByKey(centerId);
    String healthAuthority = (String) centerBean.get("health_authority");
    BasicDynaBean regPref = registrationPreferencesService.getRegistrationPreferences();

    String govenmtIdLabel = (String) (regPref.get(GOVT_IDENTIFIER_LABEL) != null
        ? regPref.get(GOVT_IDENTIFIER_LABEL)
        : "Emirates ID");

    String govenmtIdTypeLabel = (String) (regPref.get(GOVT_IDENTIFIER_LABEL) != null
        ? regPref.get(GOVT_IDENTIFIER_LABEL)
        : "Emirates ID Type");

    StringBuilder govtIdNoErr = new StringBuilder("<br/> EMIRATES ID ERROR: Patients without "
        + govenmtIdLabel + " (or) " + govenmtIdTypeLabel + ".<br/> " + "Please check the "
        + govenmtIdLabel + " (or) " + govenmtIdTypeLabel + " for Patients: <br/>");


    List<BasicDynaBean> patients;
    patients = personRegisterRepository.getPatientDetails(personRegisterBatchId, healthAuthority);

    // add the claim header first
    BasicDynaBean headerBean = personRegisterRepository.getHaadXmlHeaderFields(submissionbean);
    headerBean.set("testing", "N");
    XmlGeneratorHelper helper = new XmlGeneratorHelper();
    File claimBodyFile = File.createTempFile("tempClaimBodyFile", "");
    List<Map> allPatientMap = new ArrayList<>();
    try (OutputStream stream = new FileOutputStream(claimBodyFile)) {

      // begin bill validation
      for (BasicDynaBean patient : patients) {
        String emiratesIdNumber = patient.get("emirates_id_number") != null
            ? (String) patient.get("emirates_id_number")
            : null;
        if (emiratesIdNumber == null || emiratesIdNumber.equals("")) {
          errorsMap.put("EMIRATES ID ERROR:", govtIdNoErr.append(urlString(path, "pre-registration",
              (String) patient.get("patient_id"), null, urlRightsMap, actionUrlMap)));
          govtIdNoErr.append("  ,  ");
        }
        String gender = ((String) patient.get("patient_gender")).equalsIgnoreCase("M") ? "1"
            : ((String) patient.get("patient_gender")).equalsIgnoreCase("F") ? "0" : "9";
        Map<String,String> bodyMap = new HashMap<String,String>();
        // It is better to separate the FTLs for HAAD and DHA
        bodyMap.put("unified_number", (String) patient.get("unified_number"));
        bodyMap.put("first_name_en", (String) patient.get("patient_name"));
        bodyMap.put("middle_name_en", (String) patient.get("middle_name"));
        bodyMap.put("last_name_en", (String) patient.get("last_name"));
        bodyMap.put("birth_date", (String) patient.get("date_of_birth"));
        bodyMap.put("gender", gender);
        bodyMap.put("nationality", (String) patient.get("nationality"));
        bodyMap.put("contact_number", (String) patient.get("patient_phone"));
        bodyMap.put("nationality_code", (String) patient.get("nationality_code"));
        bodyMap.put("city", (String) patient.get("patient_city"));
        bodyMap.put("city_code", (String) patient.get("city_code"));
        bodyMap.put("country_of_residence", (String) patient.get("country"));
        bodyMap.put("emirate_of_residence", (String) patient.get("emirate_of_residence_code"));
        bodyMap.put("passport_number", (String) patient.get("passport_no"));
        bodyMap.put("emirates_id_number", (String) patient.get("emirates_id_number"));
        // Setting the memberId
        bodyMap.put("member_id",
            (String) headerBean.get("provider_id") + "#" + (String) patient.get("patient_id"));
        allPatientMap.add(bodyMap);
      }

      String claimBodyTemplate = "";
      claimBodyTemplate = "/Eclaim/PersonRegisterSelfpayBody.ftl";
      for (Map entry : allPatientMap) {
        helper.addClaimBody(stream, entry, claimBodyTemplate);
      }
    }

    // End of Persons list

    /*
     * Write Header followed by claim Body to file. This is mainly being done
     * because record/claim count is calculated while person register are being processed.
     */
    byte[] data = Files.readAllBytes(Paths.get(claimBodyFile.getPath()));
    File claimFile = File.createTempFile("tempClaimFile", "");
    OutputStream claimStream = new FileOutputStream(claimFile);
    headerBean.set("claims_count", allPatientMap.size());
    Map headerMap = headerBean.getMap();
    // adding header
    helper.addClaimBody(claimStream, headerMap, "/Eclaim/PersonRegisterSelfpayHeader.ftl");
    try {
      Files.write(Paths.get(claimFile.getPath()), data, StandardOpenOption.APPEND);
    } catch (IOException exception) {
      logger.error("ERROR while merging Claim Body and Header" + exception);
    } finally {
      claimBodyFile.delete();
    }

    // update fileName in db
    String fileName = personRegisterBatchId + "_" + DataBaseUtil.getCurrentDate() + ".xml";
    // Trimming white spaces
    String trimmedFileName = fileName.replaceAll("\\s+", "");
    submissionbean.set("file_name", trimmedFileName);
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.clear();
    filterMap.put(PERSONREGISTER_BATCH_ID, personRegisterBatchId);
    personRegisterRepository.update(submissionbean, filterMap);

    if (errorsMap.isEmpty()) {
      // generate a filename
      logger.info("XML processing ends");
      File claimXmlFile = new File("/var/log/insta/insta-ia-sync/" + trimmedFileName);

      claimXmlFile.getParentFile().mkdirs();
      claimXmlFile.createNewFile();

      try (FileOutputStream claimXmlStream = new FileOutputStream(claimXmlFile)) {
        FileInputStream claimXmlInStream = new FileInputStream(claimFile);
        claimXmlStream.write(DataBaseUtil.readInputStream(claimXmlInStream));
        // adding footer
        helper.addClaimBody(claimXmlStream, new HashMap(), "/Eclaim/PersonRegisterFooter.ftl");
        claimXmlStream.flush();
        claimXmlInStream.close();
      }
      return true;
    } else {
      /*
       * set the status as failed in person_register_submission_batch and set errors
       * as json in db
       */
      String json = new ObjectMapper().writeValueAsString(errorsMap);

      submissionbean.set("errors", json);
      filterMap.clear();
      filterMap.put(PERSONREGISTER_BATCH_ID, personRegisterBatchId);
      personRegisterRepository.update(submissionbean, filterMap);
      return false;
    }
  }

  /**
   * Sets the filter used to bring selfpay person register records from the DB.
   *
   * @param parameters            the parameters
   * @param centerId              the center id
   * @param accGrpId              the acc grp id
   * @param registrationDateArray the registration date array
   * @return the map
   */
  private Map setFilter(Map<String, String[]> parameters, Integer centerId, Integer accGrpId,
      Date[] registrationDateArray, Date[] dischargeDateArray) {
    // String[] ignoreOpenBills = parameters.get("ignore_open_bills");
    Map filter = new HashMap<>();

    // if (ignoreOpenBills != null) {
    // filter.put("ignore_open_bills", true);
    // }

    if (accGrpId != 0) {
      filter.put("account_group", accGrpId);
    } else {
      filter.put("account_group", 1);
    }
    filter.put("center_id", centerId);

    String[] visitType = parameters.get("visit_type");
    // check if visit type works properly,
    if (visitType != null) {
      if (visitType.length == 2) {
        filter.put("visit_type", "*");
      } else {
        filter.put("visit_type", visitType[0]);
      }
    }
    if (registrationDateArray != null && null != registrationDateArray[0]
        && null != registrationDateArray[1]) {
      filter.put("reg_date_time", registrationDateArray);
    }
    if (dischargeDateArray != null && null != dischargeDateArray[0]
        && null != dischargeDateArray[1]) {
      filter.put("discharge_date_time", dischargeDateArray);
    }
    String[] codificationStatus = parameters.get("codification_status");
    if (codificationStatus != null) {
      filter.put("codification_status", codificationStatus);
    }

    String[] departmentId = parameters.get("dept_id");
    if (departmentId != null) {
      filter.put("dept_id", departmentId);
    }
    return filter;
  }


  /**
   * Url string.
   *
   * @param path         the path
   * @param type         the type
   * @param id           the id
   * @param name         the name
   * @param urlRightsMap the url rights map
   * @param actionUrlMap the action url map
   * @return the string
   */
  public String urlString(String path, String type, String id, String name, HashMap urlRightsMap,
      HashMap actionUrlMap) {

    String url = "";
    path = path + "/";
    final String newTabHref = "<b><a target='_blank' href='";
    final String hrefEnd = "</a></b>";

    if (type == null) {
      return url;
    }
    if (type.equals("pre-registration")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("reg_general").equals("A")) {
        url = (String) actionUrlMap.get("reg_general");
        url = newTabHref + path + url + "?_method=show&regType=regd&mr_no=" + id + "&mrno=" + id
            + "'>" + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }
    }
    return url;
  }

  /**
   * List submissions based on filters.
   *
   * @param parameters the parameters
   * @return the paged list
   */
  public PagedList listSubmission(Map<String, String[]> parameters) {
    String[] centerOrAccountGroup = parameters.get("center_or_account_group");
    parameters.remove("center_or_account_group");
    parameters.remove("submissionType");
    String accGrpOrCenterIdStr = (centerOrAccountGroup == null
        || centerOrAccountGroup[0].equals("")) ? "" : (centerOrAccountGroup[0]);
    int accGrpId = 0;
    String[] centerIds = parameters.get("center_id");
    Integer centerId = Integer
        .parseInt((centerIds == null || centerIds[0].equals("")) ? "0" : centerIds[0]);
    // comment1: if center_or_account_group filter is selectd then it will
    // filters
    if (!accGrpOrCenterIdStr.equals("")) {
      if (accGrpOrCenterIdStr.startsWith("A")) {
        accGrpId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
      } else if (accGrpOrCenterIdStr.startsWith("C")) {
        centerId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
      }

      if (accGrpId != 0) {
        parameters.put("account_group_id", new String[] { Integer.toString(accGrpId) });
        parameters.put("account_group_id@type", new String[] { "integer" });
      } else {
        parameters.put("center_id", new String[] { centerId + "" });
        parameters.put("center_id@type", new String[] { "integer" });
      }
    } else {
      // bring records for all centers and account groups
      parameters.remove("center_id");
      parameters.remove("account_group_id");
    }

    List<BasicDynaBean> accGrpAndCenterList = accountgrpAndCenterView(centerId);
    Map accGrpAndCenterType = null;
    List<String> accGrpFilter = new ArrayList<>();
    List<String> centerFilter = new ArrayList<>();

    if (centerId != 0) {
      for (int i = 0; i < accGrpAndCenterList.size(); i++) {
        accGrpAndCenterType = (accGrpAndCenterList.get(i)).getMap();
        String type = (String) accGrpAndCenterType.get("type");
        if (type.equals("C")) {
          int accountGroupId = Integer.parseInt(accGrpAndCenterType.get("ac_id") + "");
          if (accountGroupId == centerId) {
            centerFilter.add(accGrpAndCenterType.get("ac_id") + "");
          }
        } else {
          if (null != accGrpAndCenterType.get("store_center_id")) {
            int storeCenterId = (Integer) accGrpAndCenterType.get("store_center_id");
            if (centerId == storeCenterId) {
              accGrpFilter.add(accGrpAndCenterType.get("ac_id") + "");
            }
          } else {
            accGrpFilter.add(accGrpAndCenterType.get("ac_id") + "");
          }
        }
      }
    } else {
      /*
       * brings records from all centers if center id 0 is chosen. ie default
       * center/corporate center
       */
      parameters.remove("center_id");
    }
    return personRegisterRepository.listSubmissionDetails(parameters,
        ConversionUtils.getListingParameter(parameters), accGrpFilter, centerFilter);
  }

  /**
   * Delete.
   *
   * @param parameters the parameters
   * @return the string
   */
  @Transactional(rollbackFor = Exception.class)
  public String delete(Map<String, String[]> parameters) {
    List<Object> keys = new ArrayList<>();
    String personRegisterBatchId = (parameters.get("batch_id")[0]);
    String msg = "Failed to delete submission batch";
    int success;
    success = personRegisterDetailsRepository.delete(PERSONREGISTER_BATCH_ID,personRegisterBatchId);
    if (success != 0) {
      success = personRegisterRepository.delete(PERSONREGISTER_BATCH_ID,personRegisterBatchId);
      if (success != 0) {
        msg = "Sucessfully deleted selfpay submission batch with id: " + personRegisterBatchId;
      }
    }
    return msg;
  }

  /**
   * Mark as sent.
   *
   * @param parameters the parameters
   * @return the string
   */
  public String markAsSent(Map<String, String[]> parameters) {
    // mark the submission batch as sent, setting status to S (Sent)
    String personRegisterBatchId = null;
    String msg = "Invalid/No submission batch with id: ";
    if (null != parameters.get("batch_id")) {
      personRegisterBatchId = parameters.get("batch_id")[0];
    }
    if (personRegisterBatchId == null) {
      return null;
    }
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(PERSONREGISTER_BATCH_ID, personRegisterBatchId);
    BasicDynaBean submissionBean = personRegisterRepository.getBean();
    submissionBean.set("status", "S");
    submissionBean.set("submission_date", new java.sql.Timestamp((new java.util.Date()).getTime()));
    if (personRegisterRepository.update(submissionBean, filterMap) > 0) {
      msg = "Batch successfully marked as Sent with id: ";
    }
    return msg + personRegisterBatchId;
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return personRegisterRepository.getBean();
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   */
  public void update(BasicDynaBean bean, Map<String, Object> keys) {
    personRegisterRepository.update(bean, keys);
  }

  /**
   * Accountgrp and center view.
   *
   * @param userCenterId the user center id
   * @return the list
   */
  public List<BasicDynaBean> accountgrpAndCenterView(Integer userCenterId) {
    return accountGroupService.accountGroupCenterView(userCenterId);
  }

  /**
   * Gets the file name.
   *
   * @param personRegisterBatchId the batch id
   * @return the file name
   */
  public BasicDynaBean getFileName(String personRegisterBatchId) {
    return personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID, personRegisterBatchId);
  }

  /**
   * Download XML.
   *
   * @param parameters the parameters
   * @param res        the res
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String downloadXml(Map<String, String[]> parameters, HttpServletResponse res)
      throws IOException {
    String personRegisterBatchId = parameters.get("batch_id")[0];

    BasicDynaBean submissionBean = personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID,
        personRegisterBatchId);
    String fileName = (String) submissionBean.get("file_name");
    fileName = fileName.replaceAll("\\s+", "");// removing white spaces
    File claimXmlFile = new File("/var/log/insta/insta-ia-sync/" + fileName);

    if (claimXmlFile.exists()) {
      res.setContentType("text/xml");
      res.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
      OutputStream eclaimXmlOutStream = res.getOutputStream();
      FileInputStream eclaimXmlInstream = new FileInputStream(claimXmlFile);
      eclaimXmlOutStream.write(DataBaseUtil.readInputStream(eclaimXmlInstream));
      eclaimXmlOutStream.flush();
      eclaimXmlOutStream.close();
      return null;
    }
    // if file with matching selfpay batch id is not found
    return "Requested Resource No longer Available For Submission Batch : " + personRegisterBatchId;
  }

  /**
   * Gets the errors.
   *
   * @param personRegisterBatchId the per batch id
   * @return the errors
   * @throws JsonParseException   the json parse exception
   * @throws JsonMappingException the json mapping exception
   * @throws IOException          Signals that an I/O exception has occurred.
   */
  public Map getErrors(String personRegisterBatchId) throws IOException {
    BasicDynaBean submissionBean = personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID,
        personRegisterBatchId);
    ObjectMapper mapper = new ObjectMapper();
    if (submissionBean.get("errors") != null) {
      return mapper.readValue((String) submissionBean.get("errors"), HashMap.class);
    } else {
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("Unexpected Error: ", "Please try regenerating the batch again.");
      return errorMap;
    }
  }

  /**
   * Gets the file to string.
   *
   * @param personRegisterBatchId the batch id
   * @return the file to string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getFileToString(String personRegisterBatchId) throws IOException {
    BasicDynaBean submissionBean = personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID,
        personRegisterBatchId);
    String fileName = (String) submissionBean.get("file_name");
    String trimmedFileName = fileName.replaceAll("\\s+", "");
    File file = new File("/var/log/insta/insta-ia-sync/" + trimmedFileName);
    if (file.exists()) {
      return FileUtils.readFileToString(file);
    } else {
      return null;
    }
  }

  /**
   * Upload claim to respective webservice.
   *
   * @param parameters the parameters
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String uploadClaim(Map<String, String[]> parameters, HttpServletResponse res)
      throws IOException {
    // check if file generated still exists
    String personRegisterBatchId = parameters.get("batch_id")[0];
    String xmlString = getFileToString(personRegisterBatchId);
    if (xmlString == null) {
      return "Requested Resource No longer Available For Submission Batch : "
          + personRegisterBatchId;
    }

    /*
     * This value is hardcoded as personregister is being released for HAAD only
     * Health authority should actually come from the center.
     */
    String healthAuthority = "HAAD";

    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin == null) {
      return "Unknown Health authority.";
    }

    BasicDynaBean submissionbean = personRegisterRepository.findByKey(PERSONREGISTER_BATCH_ID,
        personRegisterBatchId);
    Integer centerId = (Integer) submissionbean.get("center_id");
    BasicDynaBean centerbean = centerService.findByKey(centerId);

    logger.info("Starting Person Register Selfpay Upload as file exists");
    if (centerbean.get("ha_username") == null || centerbean.get("ha_password") == null) {
      return "Invalid username or password. Please check center credentials";
    }
    ClaimContext claimContext = plugin.getClaimContext();
    claimContext.put("eclaim_user_id", centerbean.get("ha_username"));
    claimContext.put("eclaim_password", centerbean.get("ha_password"));
    claimContext.put("eclaim_testing", "N");
    ClaimDocument claimDocument = new ClaimDocument();
    // set the file content as a byte format
    String fileName = ((String) submissionbean.get("file_name")).trim();

    claimDocument.setContent(xmlString.getBytes());
    claimDocument.setFileName(fileName);
    // Try to send RA Request and submit files
    try {
      // send a request to webservice endpoint to upload the file
      ClaimSubmissionResult subResult = plugin.submitClaim(claimDocument, claimContext);
      int txnResult = subResult.getUploadTxnResult().value;
      PriorAuthorizationHelper.TransactionResults txn = PriorAuthorizationHelper.TransactionResults
          .getTxnResultMessage(txnResult);
      if (txnResult >= 0) {
        String msg = txn.getResultMsg();
        logger.debug(" EClaim upload successful. " + msg);
        // Mark the claim as sent if its been successfully uploaded.
        markAsSent(parameters);
        return "Person Register Batch Successfully Uploaded And Marked As Sent";
      } else {
        PriorAuthorizationHelper priorAuthHelper = new PriorAuthorizationHelper();
        logger.debug(txn.getResultMsg());
        logger.debug(subResult.getErrorMessage().value);
        String errMsg = subResult.getErrorMessage().value;
        byte[] errorReportBytes = subResult.getErrorReport().value;
        String errorReportStr = priorAuthHelper.getErrorReportbase64String(errorReportBytes);
        // Encoded string
        String errorFileName = fileName + "_" + DataBaseUtil.getCurrentDate();
        File decodedDataFile = File.createTempFile("tempPBMErrorReportFile", "");
        String err = priorAuthHelper.decodeErrorReportbase64ToFile(errorReportStr, decodedDataFile);
        if (err == null) {
          res.setContentType("application/vnd.ms-excel");
          res.setHeader("Content-disposition",
              "attachment; filename=\"" + errorFileName + ".xls" + "\"");

          OutputStream outputStream = res.getOutputStream();
          // Read the zip file as write to output stream. The file zipped content has an
          // excel
          // sheet.
          err = priorAuthHelper.unzipErrorReportFile(decodedDataFile, outputStream);
        }
        return errMsg;
      }
    } catch (ConnectException exception) {
      String msg = "Client server is Down/Response is corrupted..... Cannot connect to "
          + plugin.getWebservicesHost();
      logger.error(msg);
      logger.error("", exception);
      return msg;
    }
  }
}
