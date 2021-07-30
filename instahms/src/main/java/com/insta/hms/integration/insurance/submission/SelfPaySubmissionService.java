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
 * The Class SelfPaySubmissionService. Refer
 * https://practo.atlassian.net/wiki/spaces/HIMS/pages/458424325/HAAD+compliance-+Self+Pay+Claims
 */
@Service
public class SelfPaySubmissionService {

  /** The self pay repository. */
  @LazyAutowired
  SelfPaySubmissionRepository selfPayRepository;

  /** The account group service. */
  @LazyAutowired
  AccountingGroupService accountGroupService;
  
  @LazyAutowired
  HospitalClaimDiagnosisRepository hospitalClaimDiagRepo;

  /** The mrd code repository. */
  @LazyAutowired
  MrdCodeRepository mrdCodeRepository;
  
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
  static Logger logger = LoggerFactory
      .getLogger(SelfPaySubmissionService.class);

  /** The Constant SELFPAY_BATCH_ID. */
  private static final String SELFPAY_BATCH_ID = "selfpay_batch_id";
  private static final String URL_RIGHTS_MAP = "urlRightsMap";
  private static final String ACTION_URL_MAP = "actionUrlMap";
  private static final String GOVT_IDENTIFIER_LABEL = "government_identifier_label";
  private static final String DIAGNOSIS = "diagnosis";

  /**
   * Process selfpay job and schedules the job immediately.
   *
   * @param selfpayBatchId the selfpay batch id
   * @param contextPath    the context path
   * @return the string
   */
  // creates submission batch with given criteria and starts a job if visits
  public String processSelfpayJob(Integer selfpayBatchId, String contextPath) {

    // set the status of the job as processing
    BasicDynaBean selfpayBean = selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
    selfpayBean.set("processing_status", "P");// sets the status from N(not
    // scheduled) to
    // P(In-progress)
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(SELFPAY_BATCH_ID, selfpayBatchId);
    selfPayRepository.update(selfpayBean, filterMap);

    // set Job Data and schedule the job
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String schema = (String) sessionAttributes.get("sesHospitalId");
    HttpSession session = RequestContext.getSession();
    HashMap urlRightsMap = (HashMap) session.getAttribute(URL_RIGHTS_MAP);
    HashMap actionUrlMap = (HashMap) session.getServletContext().getAttribute(ACTION_URL_MAP);
    Map<String, Object> jobData = new HashMap<>();
    jobData.put(SELFPAY_BATCH_ID, selfpayBatchId);
    jobData.put(URL_RIGHTS_MAP, urlRightsMap);
    jobData.put(ACTION_URL_MAP, actionUrlMap);
    jobData.put("path", contextPath);
    jobData.put("schema", schema);
    String jobName = "SelfpayXMLJob-" + selfpayBatchId;
    jobService.scheduleImmediate(buildJob(jobName, SelfPayXmlJob.class, jobData));
    return "Job successfully queued for processing.";
  }

  /**
   * Returns the account group list to be shown in the page dropdown of the selfpay submissions
   * batch creation screen.
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
   * Creates the submission batch in selfpay_submission_batch table.
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
    String msg = "No bills matched the given filter criteria.";
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

    // get list of cash bills based on the filters given

    List<BasicDynaBean> bills = selfPayRepository.searchBillsForSubmission(filter);
    // create new batch if list
    // add mapping in selfpay_submission_batch

    if (bills != null && !bills.isEmpty()) {

      BasicDynaBean selfPayBean = selfPayRepository.getBean();
      Integer batchId = selfPayRepository.getNextSequence();

      selfPayBean.set(SELFPAY_BATCH_ID, batchId);
      selfPayBean.set("visit_type", filter.get("visit_type"));
      selfPayBean.set("account_group_id", accountGroupId);
      selfPayBean.set("created_by", userId);
      selfPayBean.set("center_id", centerId);
      if (dischargeDateArray[0] != null) {
        selfPayBean.set("batch_from_date", new java.sql.Timestamp(dischargeDateArray[0].getTime()));
        selfPayBean.set("batch_to_date", new java.sql.Timestamp(dischargeDateArray[1].getTime()));
      } else if (registrationDateArray[0] != null) {
        selfPayBean.set("batch_from_date",
            new java.sql.Timestamp(registrationDateArray[0].getTime()));
        selfPayBean.set("batch_to_date",
            new java.sql.Timestamp(registrationDateArray[1].getTime()));
      }
      // insert bills mapping into selfpay_submission_batch table
      selfPayRepository.insert(selfPayBean);

      // update batch ID against cash bills given the filters in bill

      List<Object> updateKeys = new ArrayList<>();
      List<BasicDynaBean> updateBeans = new ArrayList<>();
      BasicDynaBean bean = billService.getBean();
      for (BasicDynaBean bill : bills) {
        bean.set(SELFPAY_BATCH_ID, batchId);
        updateBeans.add(bean);
        updateKeys.add(bill.get("bill_no"));
      }
      Map<String, Object> updateKeysMap = new HashMap<>();
      updateKeysMap.put("bill_no", updateKeys);
      billService.batchUpdate(updateBeans, updateKeysMap);

      // set flash info message
      msg = "Selfpay Batch Successfully Created " + batchId + " with " + bills.size() + " bills.";
      
      insertRecordsIntoHospDiagnosis(batchId);
    }
    
    
    
    response.put("info", msg);
    return response;
  }
  
  /**
   * Insert records into hospital_claim_diagnosis that
   * haven't been copied from mrd_diagnosis before.
   * This method ensures that when claim is generated 
   * the codes that it is generated with is saved from
   * doctor modification at a later stage.
   *
   * @param selfpayBatchId the submission batch id
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void insertRecordsIntoHospDiagnosis(Integer selfpayBatchId) {
    try {
      // get the visit ids that have not been copied to hospital_claim_diagnosis from
      // mrd_diagnosis
      List<BasicDynaBean> visitIdsBeanList = hospitalClaimDiagRepo
          .getSelfpayVisitsNotCopied(selfpayBatchId);
      // copy records matching visitIdsList from mrd_diagnosis to
      // hospital_claim_diagnosis
      List<String> visitIdsList = new ArrayList<>();
      for (BasicDynaBean bean : visitIdsBeanList) {
        visitIdsList.add((String) bean.get("visit_id"));
      }
      List<BasicDynaBean> diagnosisList = MRDDiagnosisDAO.getListWhereVisit(visitIdsList);
      if (diagnosisList != null && !diagnosisList.isEmpty()) {
        hospitalClaimDiagRepo.batchInsert(diagnosisList);
      }
      return;
    } catch (SQLException exception) {
      logger.error(
          "Unable to copy records from mrd_diagnosis to hospital_claim_diagnosis" + exception);
    }
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
   * Generates the xml, this function is trigger by selfpayXMLJob.
   *
   * @param selfpayBatchId the selfpay batch id
   * @param urlRightsMap   the url rights map
   * @param actionUrlMap   the action url map
   * @param path           the path
   * @return the map
   * @throws Exception the exception
   */
  public boolean generate(Integer selfpayBatchId, HashMap urlRightsMap, HashMap actionUrlMap,
      String path) throws Exception {

    Map<String, StringBuilder> errorsMap = new HashMap<>();
    BasicDynaBean submissionbean = selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
    Integer accountGroupId = (Integer) submissionbean.get("account_group_id");
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

    String encTypePref = (String) (regPref.get("encntr_type_reqd") != null
        ? regPref.get("encntr_type_reqd")
        : "RQ");

    String encStartEndTypePref = (String) (regPref.get("encntr_start_and_end_reqd") != null
        ? regPref.get("encntr_start_and_end_reqd")
        : "RQ");

    StringBuilder noCliniciansErr = new StringBuilder(
        "<br/> NO CLINICIANS ERROR: Bills without clinician. "
            + "Please enter clinician for Patients : <br/>");

    StringBuilder clinicianIdErr = new StringBuilder(
        "<br/> CLINICIAN ERROR: Bills without clinician Id. "
            + "Please enter clinician id for Doctors : <br/>");

    StringBuilder orderingClinicianErr = new StringBuilder(
        "<br/> ORDERING CLINICIAN ERROR: Bills without ordering clinician. "
            + "Please enter ordering clinician for Doctors : <br/>");

    StringBuilder encountersErr = new StringBuilder(
        "<br/> ENCOUNTERS ERROR: Bills without encounter types. <br/>"
            + "Please enter encounter types for Patients : <br/>");

    StringBuilder encountersStartErr = new StringBuilder(
        "<br/> ENCOUNTERS START ERROR: Bills without encounter start types. <br/>"
            + "Please enter encounter start types for Patients : <br/>");

    StringBuilder encountersEndErr = new StringBuilder(
        "<br/> ENCOUNTERS END ERROR: Bills without encounter end types. <br/>"
            + "Please enter encounter end types for Patients : <br/>");

    StringBuilder encountersEndDateErr = new StringBuilder(
        "<br/> ENCOUNTERS END DATE ERROR: Bills without encounter end date. <br/>"
            + "Please enter encounter end date for Patients : <br/>");

    StringBuilder diagnosisCodesErr = new StringBuilder(
        "<br/> DIAGNOSIS ERROR: Bills without diagnosis codes. <br/>"
            + "Please enter diagnosis codes for Patients : <br/>");

    StringBuilder billStatusErr = new StringBuilder(
        "<br/> BILLS OPEN ERROR: Bills which have Open bills. <br/>"
            + "Please check the Bills with Open Bills: <br/>");

    StringBuilder codesErr = new StringBuilder(
        "<br/> CODES ERROR: Bills found without activity codes. <br/>"
            + "Please check the Bills : <br/>");

    StringBuilder consCodesErr = new StringBuilder(
        "<br/> CONS CODES ERROR: Bills found without observation codes (or) values.<br/> "
            + "Please check the Consultation(s) for Patients : <br/>");

    StringBuilder consComplaintCodesErr = new StringBuilder(
        "<br/> CONS COMPLAINT CODES ERROR: Bills found without presenting-complaint "
            + "observation codes (or) values.<br/> "
            + "Please check the Consultation(s) for Patients : <br/>");

    StringBuilder toothCodesErr = new StringBuilder(
        "<br/> TOOTH NUMBER ERROR: Bills found without tooth number observation(s).<br/> "
            + "Please check the Service(s) for for Patients : <br/>");

    StringBuilder toothNoCodesErr = new StringBuilder(
        "<br/> TOOTH NUMBER CODES ERROR: Bills found without tooth number "
            + "observation code(s).<br/> "
            + "Please check the Observation(s) for Service(s) : <br/>");

    StringBuilder toothNoUniversalCodeErr = new StringBuilder(
        "<br/> TOOTH NUMBER NO UNIVERSAL CODE ERROR: Bills found without universal code.<br/> "
            + "Please check the Observation(s) for Service(s) : <br/>");

    StringBuilder diagCodesErr = new StringBuilder(
        "<br/> DIAG CODES ERROR: Bills found without observation codes (or) values.<br/> "
            + "Please check the Test(s) for Patients : <br/>");

    StringBuilder govtIdNoErr = new StringBuilder("<br/> EMIRATES ID ERROR: Bills without "
        + govenmtIdLabel + " (or) " + govenmtIdTypeLabel + ".<br/> " + "Please check the "
        + govenmtIdLabel + " (or) " + govenmtIdTypeLabel + " for Patients: <br/>");

    StringBuilder presentOnAdmissionError = new StringBuilder(
        ClaimProcessor.PRESENT_ON_ADMISSION_ERROR);

    List<BasicDynaBean> diagnosis;
    List<BasicDynaBean> bills;
    List<BasicDynaBean> charges;
    Map<String, List> observationsMap = new HashMap<>();
    List<String> visitIdList = new ArrayList<>();
    List<String> cliniciansList = new ArrayList<>();
    List<String> clinicianIdsList = new ArrayList<>();
    List<String> orderingCliniciansList = new ArrayList<>();

    bills = selfPayRepository.getBillDetails(selfpayBatchId);
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put("health_authority", "HAAD");
    BasicDynaBean healthAuthPref = healthAuthorityPreferencesRepository.findByKey(filterMap);
    List<BasicDynaBean> accountGrpList = accountGroupService.listAll();

    // add the claim header first
    BasicDynaBean headerBean = selfPayRepository.getHaadXmlHeaderFields(submissionbean);
    headerBean.set("testing", "N");
    // get number of zero claim bills
    Integer zeroClaimBills = 0;
    Map<String, Map> visitIdMap = new HashMap<>();
    XmlGeneratorHelper helper = new XmlGeneratorHelper();
    File claimBodyFile = File.createTempFile("tempClaimBodyFile", "");
    try (OutputStream stream = new FileOutputStream(claimBodyFile)) {
      List<Map> allBillMap = new ArrayList<>();
      // begin bill validation
      for (BasicDynaBean bill : bills) {
        Eclaim eclaim = new Eclaim();

        String billNo = (String) bill.get("bill_no");
        String mrNo = (String) bill.get("patient_id");
        String visitId = (String) bill.get("visit_id");
        String emiratesIdNumber = bill.get("emirates_id_number") != null
            ? (String) bill.get("emirates_id_number")
            : null;
        String startDateString = (String) bill.get("start_date");
        java.util.Date startDate = new java.util.Date();
        Calendar cal = Calendar.getInstance();
        if (StringUtils.isNotEmpty(startDateString)) {
          SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
          startDate = formatter.parse(startDateString);
          /*
           * This is date HAAD regulations for Present on Admission go Live(31st Aug 2018). All
           * claims with encounter type 3/4 generated after this date will require the additional
           * present on admission check.
           */
          cal.set(2018, 7, 31);
        }

        String encounterStartType = bill.get("encounter_start_type") != null
            ? ((Integer) bill.get("encounter_start_type")).toString()
            : null;
        String encounterEndType = bill.get("encounter_end_type") != null
            ? ((Integer) bill.get("encounter_end_type")).toString()
            : null;

        eclaim.setDrgAdjustmentAmt(BigDecimal.ZERO);

        if (emiratesIdNumber == null || emiratesIdNumber.equals("")) {
          errorsMap.put("EMIRATES ID ERROR:", govtIdNoErr
              .append(urlString(path, "pre-registration", mrNo, null, urlRightsMap, actionUrlMap)));
          govtIdNoErr.append("  ,  ");
        }

        String encounterType = bill.get("encounter_type") != null
            ? ((Integer) bill.get("encounter_type")).toString()
            : null;
        String visitType = (String) bill.get("visit_type");
        if (((visitType.equals("i") && encTypePref.equals("IP"))
            || (visitType.equals("o") && encTypePref.equals("OP")) || encTypePref.equals("RQ"))
            && (encounterType == null || encounterType.equals("0"))) {
          errorsMap.put("ENCOUNTERS ERROR:", encountersErr
              .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
          encountersErr.append("  ,  ");
        }

        if ((visitType.equals("i") && encStartEndTypePref.equals("IP"))
            || (visitType.equals("o") && encStartEndTypePref.equals("OP"))
            || encStartEndTypePref.equals("RQ")) {

          if (encounterStartType == null || encounterStartType.equals("0")) {
            errorsMap.put("ENCOUNTERS START ERROR:", encountersStartErr
                .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
            encountersStartErr.append("  ,  ");
          }

          if (encounterEndType == null || encounterEndType.equals("0")) {
            errorsMap.put("ENCOUNTERS END ERROR:", encountersEndErr
                .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
            encountersEndErr.append("  ,  ");
          }
        }

        String endDate = bill.get("end_date") != null ? ((String) bill.get("end_date")) : null;
        if (visitType != null && ("i").equals(visitType)
            && (endDate == null || endDate.trim().equals(""))) {
          errorsMap.put("ENCOUNTERS END DATE ERROR:", encountersEndDateErr
              .append(urlString(path, "adt", mrNo, null, urlRightsMap, actionUrlMap)));
          encountersEndDateErr.append("  ,  ");
        }

        String status = bill.get("status") != null ? ((String) bill.get("status")) : null;
        if (status.equals("A")) {
          errorsMap.put("BILLS OPEN ERROR:", billStatusErr
              .append(urlString(path, "bill", billNo, null, urlRightsMap, actionUrlMap)));
          billStatusErr.append(" , ");
        }

        List<BasicDynaBean> allBillCharges = new ArrayList<>();
        charges = storeSalesDetailsService.findAllCharges(billNo);
        if (charges.isEmpty()) {
          zeroClaimBills++;
          continue;
        }
        allBillCharges.addAll(charges);

        diagnosis = findAllDiagnosis(visitId, accountGroupId);
        eclaim.setDiagnosis(diagnosis);

        if (diagnosis == null || diagnosis.isEmpty()) {
          errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr
              .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
          diagnosisCodesErr.append("  ,  ");
        } else {
          for (BasicDynaBean diag : diagnosis) {
            String icdCode = (String) diag.get("icd_code");
            Integer yearOfOnset = (Integer) diag.get("year_of_onset");
            String presentOnAdmission = (String) diag.get("present_on_admission");

            if (icdCode == null || icdCode.equals("")) {
              errorsMap.put("DIAGNOSIS ERROR:", diagnosisCodesErr
                  .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
              diagnosisCodesErr.append(" , ");
            }
            /*
             * Encounter type 3 & 4 are applicable only for IP visits. Present on Admission field is
             * mandated only for these encounter types. Only claims created past 31st August 2018
             * are also affected by this validation. (As per HAAD notice)
             */
            if (("3".equals(encounterType) || "4".equals(encounterType))
                && !StringUtils.isNotEmpty(presentOnAdmission)
                && (startDate.compareTo(cal.getTime()) > 0) && "i".equals(visitType)) {
              errorsMap.put("PRESENT ON ADMISSION ERROR:", presentOnAdmissionError
                  .append(urlString(path, "diagnosis", visitId, null, urlRightsMap, actionUrlMap)));
              presentOnAdmissionError.append("  ,  ");
            }
          }
        }
        eclaim.setBills(bills);
        /* Check for codes for each charge */
        for (BasicDynaBean charge : charges) {
          String itemCode = (String) charge.get("item_code");
          String activityDescription = (String) charge.get("act_description");
          String activityDescriptionId = (String) charge.get("act_description_id");
          String postedDate = (String) charge.get("posted_date");

          String doctorLicenseNumber = (String) charge.get("doctor_license_number");
          String doctorId = (String) charge.get("doctor_id");
          String doctorName = (String) charge.get("doctor_name");
          String doctorType = (String) charge.get("doctor_type");

          String prescribingDoctorLicenseNumber = (String) charge
              .get("prescribing_doctor_license_number");
          String prescribingDoctorName = (String) charge.get("prescribing_doctor_name");
          String prescribingDoctorId = (String) charge.get("prescribing_doctor_id");

          if (doctorName == null || doctorName.trim().isEmpty()) {

            if (!cliniciansList.contains(doctorName) && !visitIdList.contains(visitId)) {
              errorsMap.put("NO CLINICIANS ERROR:", noCliniciansErr
                  .append(urlString(path, "patient", visitId, null, urlRightsMap, actionUrlMap)));
              noCliniciansErr.append(" , ");

              if (doctorName != null) {
                cliniciansList.add(doctorName);
              }
              visitIdList.add(visitId);
            }

          } else if (doctorLicenseNumber == null || doctorLicenseNumber.trim().equals("")) {

            if (!clinicianIdsList.contains(doctorLicenseNumber) && !visitIdList.contains(visitId)) {
              if (doctorType.equals("Doctor")) {
                errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(
                    urlString(path, "doctor", doctorId, doctorName, urlRightsMap, actionUrlMap)));
              } else {
                errorsMap.put("CLINICIAN ERROR:", clinicianIdErr.append(
                    urlString(path, "referral", doctorId, doctorName, urlRightsMap, actionUrlMap)));
              }
              clinicianIdErr.append(" , ");

              clinicianIdsList.add(doctorLicenseNumber);
              visitIdList.add(visitId);
            }
          } else if (prescribingDoctorName != null
              && (prescribingDoctorLicenseNumber == null
                  || prescribingDoctorLicenseNumber.trim().equals(""))
              && (!orderingCliniciansList.contains(prescribingDoctorLicenseNumber)
                  && !visitIdList.contains(visitId))) {
            errorsMap.put("ORDERING CLINICIAN ERROR:", orderingClinicianErr.append(urlString(path,
                "doctor", prescribingDoctorId, prescribingDoctorName, urlRightsMap, actionUrlMap)));
            orderingClinicianErr.append(" , ");
            orderingCliniciansList.add(prescribingDoctorLicenseNumber);
            visitIdList.add(visitId);
          }
          String chargeHead = (String) charge.get("charge_head");

          filterMap.clear();
          filterMap.put("chargehead_id", chargeHead);
          BasicDynaBean chrgBean = chargeHeadConstantsRepository.findByKey(filterMap);
          if (chrgBean != null) {
            String codificationSupported = chrgBean.get("codification_supported") != null
                && !chrgBean.get("codification_supported").equals("")
                    ? (String) chrgBean.get("codification_supported")
                    : "N";

            if (itemCode == null || itemCode.trim().equals("")) {
              if (codificationSupported != null && codificationSupported.equals("Y")) {

                errorsMap.put("CODES ERROR:", codesErr
                    .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                codesErr.append(
                    "( " + activityDescription + ", Posted Date: " + postedDate + " ), <br/> ");
              } else {
                errorsMap.put("CODES ERROR:", codesErr
                    .append(urlString(path, "bill", billNo, null, urlRightsMap, actionUrlMap)));
                codesErr.append(
                    "( " + activityDescription + ", Posted Date: " + postedDate + " ), <br/> ");
              }
            }
          }
          String chargeId = (String) charge.get("charge_id");
          List<BasicDynaBean> observations;
          observations = mrdObservationRepository.findAllObservations(chargeId);

          /*
           * Check for tooth number if required if dental service i.e SNP
           */
          String chargeGroup = (String) charge.get("charge_group");
          if (chargeGroup.equalsIgnoreCase("SNP")) {
            BasicDynaBean service = servicesRepository.findByKey("service_id",
                activityDescriptionId);
            if (service != null && service.get("tooth_num_required").equals("Y")) {
              if (observations == null || observations.isEmpty()) {
                errorsMap.put("TOOTH NUMBER ERROR:", toothCodesErr
                    .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                toothCodesErr.append("<br/>Service :" + billNo + "( " + activityDescription
                    + ", Posted Date: " + postedDate + " ), <br/> ");
              }
              int numOfDentalCodes = 0;
              if (observations != null && !observations.isEmpty()) {
                for (BasicDynaBean observation : observations) {
                  String resultCode = (String) observation.get("code");
                  String resultType = (String) observation.get("type");
                  BasicDynaBean haadBean = icdSupportedCodeTypesRepository.findByKey("code_type",
                      resultType);
                  Integer haadCode = haadBean == null ? null : (Integer) haadBean.get("haad_code");
                  if (haadCode != null && haadCode.intValue() == 16) {
                    ++numOfDentalCodes;
                  }
                  if (resultCode == null || resultCode.equals("")) {
                    errorsMap.put("TOOTH NUMBER CODES ERROR:", toothNoCodesErr.append(
                        urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                    toothNoCodesErr.append("<br/>Service :" + billNo + "( " + activityDescription
                        + ", Posted Date: " + postedDate + " ), <br/> ");
                  }
                }
              }
              if (numOfDentalCodes == 0) {
                errorsMap.put("TOOTH NUMBER NO UNIVERSAL CODE ERROR:", toothNoUniversalCodeErr
                    .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                toothNoUniversalCodeErr.append("<br/>Service :" + billNo + "( "
                    + activityDescription + ", Posted Date: " + postedDate + " ), <br/> ");
              }
            }
          }
          if (observations != null && !observations.isEmpty()) {

            /*
             * Check for value for each observation if consultation i.e DOC
             */
            if (chargeGroup.equals("DOC")) {
              boolean hasPresentingComplaint = false;
              for (BasicDynaBean observation : observations) {
                String resultCode = (String) observation.get("code");
                String resultValue = (String) observation.get("value");
                if (resultCode != null && resultCode.equals("Presenting-Complaint")) {
                  hasPresentingComplaint = true;
                }
                if ((resultCode != null && !resultCode.equals(""))
                    && (resultValue == null || resultValue.equals(""))) {
                  errorsMap.put("CONS CODES ERROR:", consCodesErr.append(
                      urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                  consCodesErr.append("<br/>Consultation :" + billNo + "( " + activityDescription
                      + ", Posted Date: " + postedDate + " ), <br/> ");
                }
              }
              if (!visitType.equals("i") && !hasPresentingComplaint
                  && healthAuthority.equals("DHA")) {
                errorsMap.put("CONS COMPLAINT CODES ERROR:", consComplaintCodesErr
                    .append(urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                consComplaintCodesErr.append("<br/>Consultation :" + billNo + "( "
                    + activityDescription + ", Posted Date: " + postedDate + " ), <br/> ");
              }
            }

            /*
             * Check for codes for each observation if lab test i.e LTDIA
             */
            if (chargeHead.equals("LTDIA")) {
              for (BasicDynaBean observation : observations) {
                String resultCode = (String) observation.get("code");
                String resultValue = (String) observation.get("value");

                if ((resultCode != null && !resultCode.equals(""))
                    && (resultValue == null || resultValue.equals(""))) {

                  errorsMap.put("DIAG CODES ERROR:", diagCodesErr.append(
                      urlString(path, DIAGNOSIS, visitId, null, urlRightsMap, actionUrlMap)));
                  diagCodesErr.append("<br/>Test :" + billNo + "( " + activityDescription
                      + ", Posted Date: " + postedDate + " ), <br/> ");
                }
              }
            }
            if (!observationsMap.containsKey(chargeId)) {
              observationsMap.put(chargeId, observations);
            } else {
              List<BasicDynaBean> obs = observationsMap.get(chargeId);
              obs.addAll(observations);
              observationsMap.put(chargeId, obs);
            }
          }
          // End of Observations
        }
        // End of Charges
        // get details according to accountgroup or center id
        // Setting the memberId
        bill.set("member_id",
            (String) headerBean.get("provider_id") + "#" + (String) bill.get("patient_id"));
        eclaim.setClaim(bill);
        eclaim.setCharges(allBillCharges);
        eclaim.setObservationsMap(observationsMap);

        // set payerId details.
        setPayerId(bill, healthAuthPref);
        Map bodyMap = new HashMap();
        // It is better to separate the FTLs for HAAD and DHA
        bodyMap.put("eclaim", eclaim);
        bodyMap.put("visit_id", visitId);
        bodyMap.put("accGrpList", accountGrpList);
        bodyMap.put("eclaim_xml_schema", "HAADSELFPAY");
        bodyMap.put("healthHAADPref", healthAuthPref.get("presc_doctor_as_ordering_clinician"));
        bodyMap.put("claim_id", billNo);
        bodyMap.put("priority", 1);
        allBillMap.add(bodyMap);
      }
      // combine charges, observations and diagnoses for same visitIds into a single object
      visitIdMap = combineCommonVisitBills(allBillMap);

      String claimBodyTemplate = "";
      claimBodyTemplate = "/Eclaim/SelfpayBody.ftl";
      for (Entry entry : visitIdMap.entrySet()) {
        helper.addClaimBody(stream, (Map) entry.getValue(), claimBodyTemplate);
      }
    }

    // End of Bills

    /*
     * Write Header followed by claim Body to file. This is mainly being done because record/claim
     * count is calculated while bills are being processed.
     */
    byte[] data = Files.readAllBytes(Paths.get(claimBodyFile.getPath()));
    File claimFile = File.createTempFile("tempClaimFile", "");
    OutputStream claimStream = new FileOutputStream(claimFile);
    headerBean.set("claims_count", visitIdMap.size());
    Map headerMap = headerBean.getMap();
    helper.addClaimHeader(claimStream, headerMap);
    try {
      Files.write(Paths.get(claimFile.getPath()), data, StandardOpenOption.APPEND);
    } catch (IOException exception) {
      logger.error("ERROR while merging Claim Body and Header" + exception);
    } finally {
      claimBodyFile.delete();
    }

    // update fileName in db
    String fileName = selfpayBatchId + "_" + DataBaseUtil.getCurrentDate() + ".xml";
    // Trimming white spaces
    String trimmedFileName = fileName.replaceAll("\\s+", "");
    submissionbean.set("file_name", trimmedFileName);
    filterMap.clear();
    filterMap.put(SELFPAY_BATCH_ID, selfpayBatchId);
    selfPayRepository.update(submissionbean, filterMap);

    if (errorsMap.isEmpty()) {
      // generate a filename
      logger.info("XML processing ends");
      File claimXmlFile = new File("/var/log/insta/insta-ia-sync/" + trimmedFileName);

      claimXmlFile.getParentFile().mkdirs();
      claimXmlFile.createNewFile();

      try (FileOutputStream claimXmlStream = new FileOutputStream(claimXmlFile)) {
        FileInputStream claimXmlInStream = new FileInputStream(claimFile);
        claimXmlStream.write(DataBaseUtil.readInputStream(claimXmlInStream));
        helper.addClaimFooter(claimXmlStream, new HashMap());
        claimXmlStream.flush();
        claimXmlInStream.close();
      }
      return true;
    } else {
      /*
       * set the status as failed in selfpay_submission_batch and set errors as json in db
       */
      String json = new ObjectMapper().writeValueAsString(errorsMap);

      submissionbean.set("errors", json);
      filterMap.clear();
      filterMap.put(SELFPAY_BATCH_ID, selfpayBatchId);
      selfPayRepository.update(submissionbean, filterMap);
      return false;
    }
  }
  
  /**
   * Find all diagnosis for a given claim for a specific accountGroup.
   * For pharmacy claims, diagnoses will be brought from mrd_diagnosis.
   * For Hospital claims, diagnoses will be brought from hospital_claim_diagnosis.
   *
   * @param claimPatientId the claim patient id
   * @param accountGroupId the account group id (pharmacy = 3, hospital = 1)
   * @return the list of diagnoses, if accountGroupId is null returns empty list
   * @throws SQLException the SQL exception
   */
  private List<BasicDynaBean> findAllDiagnosis(String claimPatientId, Integer accountGroupId)
      throws SQLException {
    // if pharmacy claim, get the diagnosis from mrd_diagnosis table
    if (accountGroupId == 3) {
      return mrdDiagnosisService.findAllDiagnosis(claimPatientId);
    } else {
      // bring the diagnoses from hospital_claim_diagnosis table (Coder edited diagnosis)
      return mrdDiagnosisService.findAllCoderDiagnosis(claimPatientId);
    }
  }

  /**
   * Takes the list of bodyMaps generated for each bill and combines ones from the same visit. The
   * charges and observations are combined into a single eclaim object.
   * 
   * @param allBillMap
   *          the list of bodyMaps for each bill
   * @return the map
   */
  private Map<String, Map> combineCommonVisitBills(List<Map> allBillMap) {
    Map<String, Map> visitMap = new HashMap<>();
    for (Map bodyMap : allBillMap) {
      String visitId = (String) bodyMap.get("visit_id");
      if (visitMap.containsKey(visitId)) {
        // merge charges, observations and diagnoses
        Map visitBodyMap1 = visitMap.get(visitId); //map we're merging into
        Eclaim visitClaim1 = (Eclaim) visitBodyMap1.get("eclaim"); //existing claim
        Eclaim visitClaim2 = (Eclaim) bodyMap.get("eclaim");//new claim to be merged
        // combine charges
        List billCharges = visitClaim2.getCharges();
        billCharges.addAll(visitClaim1.getCharges());
        // combine observations
        Map claimObservationsMap = visitClaim2.getObservationsMap();
        claimObservationsMap.putAll(visitClaim1.getObservationsMap());
        visitClaim2.setCharges(billCharges);
        visitClaim2.setObservationsMap(claimObservationsMap);
        
        /* In case we have a corporate visit with cash bills (disconnected from insurer)
         * We set the payer ID as ProFormaPayer*/
        BasicDynaBean selfpayBean2 = visitClaim2.getClaim();
        BasicDynaBean selfpayBean1 = visitClaim1.getClaim();
        if (((String) selfpayBean2.get("payer_id")).equals("ProFormaPayer") 
            && !((String)selfpayBean1.get("payer_id")).equals("ProFormaPayer")) {
          selfpayBean1.set("payer_id", "ProFormaPayer");
          visitClaim1.setClaim(selfpayBean1);
        }
        visitBodyMap1.put("eclaim", visitClaim2);
        
      } else {
        visitMap.put(visitId, bodyMap);
      }
    }
    return visitMap;
  }

  
  /**
   * Sets the filter used to bring selfpay records from the DB.
   *
   * @param parameters the parameters
   * @param centerId the center id
   * @param accGrpId the acc grp id
   * @param registrationDateArray the registration date array
   * @param dischargeDateArray the discharge date array
   * @return the map
   */
  private Map setFilter(Map<String, String[]> parameters, Integer centerId, Integer accGrpId,
      Date[] registrationDateArray, Date[] dischargeDateArray) {
    String[] ignoreOpenBills = parameters.get("ignore_open_bills");
    Map filter = new HashMap<>();

    if (ignoreOpenBills != null) {
      filter.put("ignore_open_bills", true);
    }

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
   * Sets the payer id to Selfpay, ProformaPayer or whatever classification type is specified while
   * registering.
   *
   * @param bill           the bill
   * @param healthAuthPref the health authority preferences.
   */
  private void setPayerId(BasicDynaBean bill, BasicDynaBean healthAuthPref) {
    if (bill.get("is_selfpay_sponsor") != null && (Boolean) bill.get("is_selfpay_sponsor")
        && bill.get("is_tpa") != null && (boolean) bill.get("is_tpa")) {
      // its a corporate or sponsor
      ConversionUtils.setDynaProperty(bill, "payer_id", "ProFormaPayer");
    } else if ((Boolean) healthAuthPref.get("is_visit_classification_mandatory")
        && VisitClassificationType.getXmlTag((String) bill.get("classification")) != null) {
      /*
       * if visit classification is mandatory, set the correct payerID, refer
       * https://practo.atlassian.net/wiki/spaces/HIMS/pages/711394094/Reporting+of+medical+
       * tourism+in+E-Claims
       */
      bill.set("payer_id", VisitClassificationType.getXmlTag((String) bill.get("classification")));
    } else {
      ConversionUtils.setDynaProperty(bill, "payer_id", "SelfPay");
    }
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
    if (type.equals(DIAGNOSIS) || type.equals("drg")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("update_mrd").equals("A")) {
        url = (String) actionUrlMap.get("update_mrd");
        url = newTabHref + path + url + "?_method=getMRDUpdateScreen&patient_id=" + id + "'>" + id
            + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("bill")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("credit_bill_collection").equals("A")) {
        url = (String) actionUrlMap.get("credit_bill_collection");
        url = newTabHref + path + url + "?_method=getCreditBillingCollectScreen&billNo=" + id + "'>"
            + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("claim")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("insurance_claim_reconciliation").equals("A")) {
        url = (String) actionUrlMap.get("insurance_claim_reconciliation");
        url = newTabHref + path + url + "?_method=getClaimBillsActivities&claim_id=" + id + "'>"
            + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("attachment")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("insurance_claim_reconciliation").equals("A")) {
        url = (String) actionUrlMap.get("insurance_claim_reconciliation");
        url = newTabHref + path + url + "?_method=addOrEditAttachment&claim_id=" + id + "'>" + id
            + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("doctor")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("mas_doctors_detail").equals("A")) {
        url = (String) actionUrlMap.get("mas_doctors_detail");
        url = newTabHref + path + url + "?_method=getDoctorDetailsScreen&mode=update&doctor_id="
            + id + "'>" + name + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
      }

    } else if (type.equals("referral")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("mas_ref_doctors").equals("A")) {
        url = (String) actionUrlMap.get("mas_ref_doctors");
        url = newTabHref + path + url + "?_method=show&referal_no=" + id + "'>" + name + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
      }

    } else if (type.equals("patient")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("edit_visit_details").equals("A")) {
        url = (String) actionUrlMap.get("edit_visit_details");
        url = newTabHref + path + url + "?_method=getPatientVisitDetails&ps_status=all&patient_id="
            + id + "'>" + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("account-group")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("accounting_group_master").equals("A")) {
        url = UrlUtil.buildURL("accounting_group_master", UrlUtil.SHOW_URL_VALUE,
            "account_group_id=" + id, null, id);
        url = newTabHref + url + "'>" + name + " Group</a></b>";
      } else {
        url = "<b>" + name + " Group</b>";
      }

    } else if (type.equals("center-name")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("mas_centers").equals("A")) {
        url = UrlUtil.buildURL("mas_centers", UrlUtil.SHOW_URL_VALUE, "center_id=" + id, null, id);
        url = newTabHref + url + "'>" + name + " Center</a></b>";
      } else {
        url = "<b>" + name + " Center</b>";
      }

    } else if (type.equals("submission")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("insurance_claim_reconciliation").equals("A")) {
        url = (String) actionUrlMap.get("insurance_claim_reconciliation");
        url = newTabHref + path + url + "?_method=list&status=&submission_batch_id=" + id + "'>"
            + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("pre-registration")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("reg_general").equals("A")) {
        url = (String) actionUrlMap.get("reg_general");
        url = newTabHref + path + url + "?_method=show&regType=regd&mr_no=" + id + "&mrno=" + id
            + "'>" + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("drug")) {
      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("pharma_sale_edit_bill").equals("A")) {
        url = (String) actionUrlMap.get("pharma_sale_edit_bill");
        url = newTabHref + path + url + "?_method=getSaleDetails&sale_item_id=" + id + "'>" + name
            + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
      }

    } else if (type.equals("adt")) {

      if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("adt").equals("A")) {
        url = (String) actionUrlMap.get("adt");
        url = newTabHref + path + url
            + "?_method=getADTScreen&_searchMethod=getADTScreen&mr_no%40op=ilike"
            + "&_actionId=adt&mr_no=" + id + "'>" + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }

    } else if (type.equals("bill-remittance")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("bill_remittance").equals("A")) {
        url = (String) actionUrlMap.get("bill_remittance");
        url = newTabHref + path + url + "?_method=getBillRemittance&billNo=" + id + "'>" + name
            + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
      }

    } else if (type.equals("ins-remittance")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("ins_remittance_xl").equals("A")) {
        url = (String) actionUrlMap.get("ins_remittance_xl");
        url = newTabHref + path + url + "?_method=show&remittance_id=" + id + "'>" + name + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
      }
    } else if (type.equals("insurance")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("change_visit_tpa").equals("A")) {
        url = (String) actionUrlMap.get("change_visit_tpa");
        url = newTabHref + path + url + "?_method=changeTpa&visitId=" + id + "'>" + id + hrefEnd;
      } else {
        url = "<b>" + id + "</b>";
      }
    } else if (type.equals("sponsor")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("mas_ins_tpas").equals("A")) {
        url = (String) actionUrlMap.get("mas_ins_tpas");
        url = newTabHref + path + url + "?_method=show&tpa_id=" + id + "'>" + name + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
      }

    } else if (type.equals("company")) {

      if (null != urlRightsMap && null != actionUrlMap
          && urlRightsMap.get("mas_insurance_comp").equals("A")) {
        url = (String) actionUrlMap.get("mas_insurance_comp");
        url = newTabHref + path + url + "?_method=show&insurance_co_id=" + id + "'>" + name
            + hrefEnd;
      } else {
        url = "<b>" + name + "</b>";
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
       * brings records from all centers if center id 0 is chosen. ie default center/corporate
       * center
       */
      parameters.remove("center_id");
    }
    return selfPayRepository.listSubmissionDetails(parameters,
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
    Integer selfpayBatchId = Integer.parseInt(parameters.get("batch_id")[0]);
    Map filterMap = new HashMap<>();
    keys.add(selfpayBatchId);
    String msg = "Failed to delete submission batch";
    BasicDynaBean billBean = billService.getBean();
    billBean.set(SELFPAY_BATCH_ID, 0);
    filterMap.put(SELFPAY_BATCH_ID, selfpayBatchId);
    billService.update(billBean, filterMap);
    int[] result = selfPayRepository.batchDelete(SELFPAY_BATCH_ID, keys);
    if (result.length > 0) {
      msg = "Sucessfully deleted selfpay submission batch with id: " + selfpayBatchId;
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
    Integer selfpayBatchId = 0;
    String msg = "Invalid/No submission batch with id: ";
    if (null != parameters.get("batch_id")) {
      selfpayBatchId = Integer.parseInt(parameters.get("batch_id")[0]);
    }
    if (selfpayBatchId == 0) {
      return null;
    }
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(SELFPAY_BATCH_ID, selfpayBatchId);
    BasicDynaBean submissionBean = selfPayRepository.getBean();
    submissionBean.set("selfpay_status", "S");
    submissionBean.set("submission_date", new java.sql.Timestamp((new java.util.Date()).getTime()));
    if (selfPayRepository.update(submissionBean, filterMap) > 0) {
      msg = "Batch successfully marked as Sent with id: ";
    }
    return msg + selfpayBatchId.toString();
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return selfPayRepository.getBean();
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   */
  public void update(BasicDynaBean bean, Map<String, Object> keys) {
    selfPayRepository.update(bean, keys);
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
   * @param selfpayBatchId the selfpay batch id
   * @return the file name
   */
  public BasicDynaBean getFileName(Integer selfpayBatchId) {
    return selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
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
    Integer selfpayBatchId = Integer.parseInt(parameters.get("batch_id")[0]);

    BasicDynaBean submissionBean = selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
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
    return "Requested Resource No longer Available For Submission Batch : " + selfpayBatchId;
  }

  /**
   * Gets the errors.
   *
   * @param selfpayBatchId the selfpay batch id
   * @return the errors
   * @throws JsonParseException   the json parse exception
   * @throws JsonMappingException the json mapping exception
   * @throws IOException          Signals that an I/O exception has occurred.
   */
  public Map getErrors(Integer selfpayBatchId) throws IOException {
    BasicDynaBean submissionBean = selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
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
   * @param selfpayBatchId the selfpay batch id
   * @return the file to string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getFileToString(Integer selfpayBatchId) throws IOException {
    BasicDynaBean submissionBean = selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
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
    Integer selfpayBatchId = Integer.parseInt(parameters.get("batch_id")[0]);
    String xmlString = getFileToString(selfpayBatchId);
    if (xmlString == null) {
      return "Requested Resource No longer Available For Submission Batch : " + selfpayBatchId;
    }

    /*
     * This value is hardcoded as selfpay is being released for HAAD only Health authority should
     * actually come from the center.
     */
    String healthAuthority = "HAAD";

    InsurancePluginManager manager = new InsurancePluginManager();
    InsuranceCaseDetails icd = new InsuranceCaseDetails();
    icd.setHealthAuthority(healthAuthority);
    InsurancePlugin plugin = manager.getPlugin(icd);
    if (plugin == null) {
      return "Unknown Health authority.";
    }

    BasicDynaBean submissionbean = selfPayRepository.findByKey(SELFPAY_BATCH_ID, selfpayBatchId);
    Integer centerId = (Integer) submissionbean.get("center_id");
    BasicDynaBean centerbean = centerService.findByKey(centerId);

    logger.info("Starting Selfpay Claim Upload as file exists");
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
        return "Selfpay Batch Successfully Uploaded And Marked As Sent";
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
          // Read the zip file as write to output stream. The file zipped content has an excel
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
