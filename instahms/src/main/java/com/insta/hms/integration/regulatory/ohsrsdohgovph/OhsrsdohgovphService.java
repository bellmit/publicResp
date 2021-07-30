package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction.GenericOhsrsFunction;
import com.insta.hms.jobs.JobService;
import com.insta.hms.mdm.bulk.CsVModelAndView;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
class OhsrsdohgovphService {
  
  @LazyAutowired
  OhsrsdohgovphReportDataRepository reportRepo;
  
  @LazyAutowired
  OhsrsdohgovphReportStatusRepository reportStatusRepo;

  @LazyAutowired
  CenterService centerService;
  
  @LazyAutowired
  RedisTemplate redis;
  
  @LazyAutowired
  OhsrsdohgovphReportCsvUploadRepository csvUploadRepo;
  
  @LazyAutowired
  private JobService jobService;
  
  @LazyAutowired
  private OhsrsdohgovphUtility utility;

  @LazyAutowired
  private OhsrsFunctionProvider ohsrsFunctionProvider;

  @LazyAutowired
  OhsrsdohgovphWebserviceClient client;

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(OhsrsdohgovphService.class);
  
  private static final String SETTINGS = "settings_ohsrsdohgovph"; 
  private static final String IN_PROGRESS = "in-progress";
  private static final String QUEUED = "queued";
  private static final String SUBMITTED_REPORTS = "submittedReports";
  
  private static final List<String> SIGNOFF_FORM_KEYS = Arrays.asList("reportedby", "designation");
  
  private static final SimpleDateFormat DATE_FORMAT = new DateUtil().getSqlTimeStampFormatterSecs();

  /**
   * Store uploaded csv.
   * @param ohsrsFunction OHSRS Function
   * @param year          reporting year
   * @param csvFile       CSV File
   * @return map containing id of file stored to db
   */
  public Map<String, Object> storeCsv(int year, String ohsrsFunction, MultipartFile csvFile) {
    Map<String, Object> response = new HashMap<>();
    try {
      int centerId = utility.getLoggedInCenterId();
      String user = utility.getLoggedInUser();
      Integer id = csvUploadRepo.upsertCsvFile(centerId, year, ohsrsFunction, 
          csvFile.getInputStream(), user);
      reportStatusRepo.upsertStatus(centerId, year, ohsrsFunction, 
          OhsrsdohgovphReportStatusRepository.STATUS_UNPROCESSED, "", user);
      response.put("id", id);      
    } catch (IOException exception) {
      throw new ValidationException("ui.error.failed.to.read.uploaded.file.please.retry.upload");
    }
    return response;
  }

  /**
   * Get OHSRS Report data for given year.
   * @param year reporting year
   * @return Map containing function wise data
   */
  public Map<String, Object> getReport(int year) {
    if (!(reportYears().contains(year))) {
      throw new ValidationException("ui.error.report.generation.not.support.for.year", 
          new String[] { String.valueOf(year)});
    }
    Map<String,Object> response = new HashMap<>();
    Map<String,Object> functionWiseMap = new HashMap<>();
    response.put("report", functionWiseMap);
    String generationStatus = utility.getReportGenerationStatus(year);
    String submitStatus = utility.getReportSubmissionStatus(year);
    response.put("generation_job_status", generationStatus);
    boolean allowSubmit = true;
    boolean allowSignoff = true;
    boolean allowGenerate = true;
    boolean signOffCompleted = false;
    response.put("submission_job_status", submitStatus);
    for (String ohsrsFunction : ohsrsFunctionProvider.getSupportedOhsrsFunctions()) {
      try {
        Map<String,Object> functionReportData = ohsrsFunctionProvider
            .getProcessor(ohsrsFunction).getReportMap(year);
        functionWiseMap.put(ohsrsFunction, functionReportData);
        String dataStatus = (String) functionReportData.get("status");
        if (OhsrsdohgovphReportStatusRepository.DISALLOWED_STATUS_FOR_SUBMIT.contains(dataStatus)) {
          allowSubmit = false;
          allowSignoff = false;
        }
        if (OhsrsdohgovphReportStatusRepository
            .DISALLOWED_STATUS_FOR_SIGNOFF.contains(dataStatus)) {
          allowSignoff = false;
        }
        if (OhsrsdohgovphReportStatusRepository.STATUS_SIGNOFF_COMPLETED.equals(dataStatus)) {
          allowSubmit = false;
          allowGenerate = false;
          signOffCompleted = true;
        }
      } catch (IllegalAccessException ex) {
        logger.error(ex.getMessage());
      } catch (InstantiationException ex) {
        logger.error(ex.getMessage());
      }
    }
    int centerId = utility.getLoggedInCenterId();
    BasicDynaBean reportStatusBean = reportStatusRepo
        .getReportStatus(centerId, year, SUBMITTED_REPORTS);
    if (reportStatusBean != null) {
      response.put("signoff_error", reportStatusBean.get("details"));
    }
    if (signOffCompleted) {
      Map<String, String> signOffMap = new HashMap<>();
      List<BasicDynaBean> beans = reportRepo.getReport(utility.getLoggedInCenterId(), 
          year, "submittedReports");
      if (beans != null) {
        for (BasicDynaBean bean : beans) {
          signOffMap.put((String) bean.get("field"), (String) bean.get("value"));
        }
      }
      response.put("signoff", signOffMap);
    }
    if (year == DateUtil.getYear(DateUtil.getCurrentDate())) {
      allowSignoff = false;
      allowGenerate = false;
    }
    response.put("allow_submit", allowSubmit);    
    response.put("allow_signoff", allowSignoff);    
    response.put("allow_generate", allowGenerate);    
    return response;
  }

  /**
   * Download CSV Template.
   * @param ohsrsFunction OHSRS Function
   * @param year          reporting year
   * @return Model and View for CSV template
   */
  public CsVModelAndView downloadCsv(int year, String ohsrsFunction) {
    if (!ohsrsFunctionProvider.getSupportedOhsrsFunctions().contains(ohsrsFunction)) {
      throw new ValidationException(
          "ui.error.not.a.valid.ohsrs.function.single.placeholder", new String[] {ohsrsFunction});
    }
    try {
      return ohsrsFunctionProvider.getProcessor(ohsrsFunction).getCsvTemplate(year);
    } catch (IllegalAccessException ex) {
      logger.error("", ex);
    } catch (InstantiationException ex) {
      logger.error("", ex);
    }
    throw new ValidationException(
        "ui.error.not.a.valid.ohsrs.function.single.placeholder", new String[] {ohsrsFunction});
  }

  private OhsrsdohgovphSettings getSettings() {
    Map<String, Object> reportingMeta = centerService.getReportingMeta(
        utility.getLoggedInCenterId());
    Map<String, Object> settingsMap = (Map<String, Object>) reportingMeta.get(SETTINGS);
    OhsrsdohgovphSettings settings = new OhsrsdohgovphSettings();
    settings.setHfhudCode((String) settingsMap.get("hfhud_code"));
    settings.setWebserviceKey((String) settingsMap.get("webservice_key"));
    settings.setTrainingMode((boolean) settingsMap.get("training_mode"));
    return settings;
  }
  
  protected boolean submitReport(int year) {
    if (!(reportYears().contains(year)) || year == DateUtil.getYear(DateUtil.getCurrentDate())) {
      throw new ValidationException("ui.error.report.submission.not.support.for.year", 
          new String[] { String.valueOf(year)});
    }
    utility.updateReportSubmissionStatus(year, IN_PROGRESS);
    boolean status = true;
    OhsrsdohgovphSettings settings = getSettings();
    int centerId = utility.getLoggedInCenterId();
    String user = utility.getLoggedInUser();
    for (String ohsrsFunction : ohsrsFunctionProvider.getSupportedOhsrsFunctions()) {
      GenericOhsrsFunction processor = null;
      try {
        processor = ohsrsFunctionProvider.getProcessor(ohsrsFunction);
      } catch (IllegalAccessException ex) {
        logger.error("", ex);
      } catch (InstantiationException ex) {
        logger.error("", ex);
      }
      if (processor == null) {
        status &= false;
        continue;
      } 
      try {
        BasicDynaBean reportStatusBean = reportStatusRepo
            .getReportStatus(centerId, year, ohsrsFunction);
        if (OhsrsdohgovphReportStatusRepository.SUBMIT_PENDING
            .contains(reportStatusBean.get(OhsrsdohgovphReportStatusRepository.COLUMN_STATUS))) {
          status &= processor.submit(year, settings);
          reportStatusRepo.upsertStatus(centerId, year, ohsrsFunction, 
              OhsrsdohgovphReportStatusRepository.STATUS_SUBMITTED, "", user);
        } else {
          status &= true;
        }
             
      } catch (Exception ex) {
        logger.error("", ex);
        status &= false;
        reportStatusRepo.upsertStatus(centerId, year, ohsrsFunction, 
            OhsrsdohgovphReportStatusRepository.STATUS_SUBMISSION_FAILED, ex.getMessage(), user);
      }
      
    }
    utility.removeReportSubmissionStatus(year);
    return status;    
  }

  public Map<String, Object> signoffReport(int year, Map<String, Object> params) {
    if (!(reportYears().contains(year)) || year == DateUtil.getYear(DateUtil.getCurrentDate())) {
      throw new ValidationException("ui.error.report.signoff.not.support.for.year", 
          new String[] { String.valueOf(year)});
    }
    for (String key : SIGNOFF_FORM_KEYS) {
      if (params.get(key) == null || ((String)params.get(key)).trim().isEmpty()) {
        throw new ValidationException("ui.error.required.single.placeholder", new String[] {key});
      }
    }
    Map<String, Object> reportMap = getReport(year);
    if (!((boolean)reportMap.get("allow_signoff"))) {
      throw new ValidationException(
        "ui.error.signoff.not.allowed.either.already.signedoff.or.submission.has.errors");
    }
    int centerId = utility.getLoggedInCenterId();
    String user = utility.getLoggedInUser();
    try {
      String responseXml = "";
      String reportYear = String.valueOf(year);
      OhsrsdohgovphSettings settings = getSettings();
      String reportedBy = (String) params.get("reportedby");
      String designation = (String) params.get("designation");
      String section = params.containsKey("section") ? ((String) params.get("section")) : "";
      String department = params.containsKey("department")
          ? ((String) params.get("department")) : "";
      String dateReported = DATE_FORMAT.format(DateUtil.getCurrentTimestamp());
      if (settings.isTrainingMode()) {
        responseXml = client.getTrainingPort().submittedReports(
            settings.getHfhudCode(),
            reportYear,
            "S",
            reportedBy,
            designation,
            section,
            department,
            dateReported);
      } else {
        responseXml = client.getProductionPort().submittedReports(
            settings.getHfhudCode(),
            reportYear,
            "S",
            reportedBy,
            designation,
            section,
            department,
            dateReported,
            settings.getWebserviceKey());
      }
      boolean success = client.parseWebserviceResponse(responseXml);
      if (success) {
        List<BasicDynaBean> beans = new ArrayList<>();
        beans.add(reportRepo.newBean(reportedBy, centerId, year, SUBMITTED_REPORTS, "reportedby",
            false, user, null, null, null));
        beans.add(reportRepo.newBean(section, centerId, year, SUBMITTED_REPORTS, "section",
            false, user, null, null, null));
        beans.add(reportRepo.newBean(designation, centerId, year, SUBMITTED_REPORTS, "designation",
            false, user, null, null, null));
        beans.add(reportRepo.newBean(department, centerId, year, SUBMITTED_REPORTS, "department",
            false, user, null, null, null));
        beans.add(reportRepo.newBean(dateReported, centerId, year, SUBMITTED_REPORTS, 
            "datereported", false, user, null, null, null));
        reportRepo.batchInsert(beans);
        for (String ohsrsFunctionStatus : ohsrsFunctionProvider.getSupportedOhsrsFunctions()) {
          reportStatusRepo.upsertStatus(centerId, year, ohsrsFunctionStatus, 
              OhsrsdohgovphReportStatusRepository.STATUS_SIGNOFF_COMPLETED, "", user);
        }
        reportStatusRepo.upsertStatus(centerId, year, SUBMITTED_REPORTS, 
            OhsrsdohgovphReportStatusRepository.STATUS_SIGNOFF_COMPLETED,"", user);
      }
      Map<String, Object> response = new HashMap<>();
      response.put("status", "submitted");
      return response;      
    } catch (Exception ex) {
      for (String ohsrsFunctionStatus : ohsrsFunctionProvider.getSupportedOhsrsFunctions()) {
        reportStatusRepo.upsertStatus(centerId, year, ohsrsFunctionStatus, 
            OhsrsdohgovphReportStatusRepository.STATUS_SIGNOFF_FAILED,"", user);
      }     
      reportStatusRepo.upsertStatus(centerId, year, SUBMITTED_REPORTS, 
          OhsrsdohgovphReportStatusRepository.STATUS_SIGNOFF_FAILED, ex.getMessage(), user);
      throw new ValidationException("ui.error.report.signoff.failed");
    }
  }

  protected Map<String,Object> queueReportSubmission(int year) {
    if (!utility.getReportSubmissionStatus(year).isEmpty()) {
      throw new ValidationException("ui.error.report.submission.already.in.progress");
    }
    utility.updateReportSubmissionStatus(year, QUEUED);
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", utility.getLoggedInSchema());
    jobData.put("userName", utility.getLoggedInUser());
    jobData.put("centerId", utility.getLoggedInCenterId());
    jobData.put("reportingYear", year);
    jobService.scheduleImmediate(
        buildJob("OhsrsdohgovphReportSubmissionJob_" 
           + String.valueOf(utility.getLoggedInCenterId()) + "_" + String.valueOf(year), 
           OhsrsdohgovphReportSubmissionJob.class, jobData));
    Map<String, Object> response = new HashMap<>();
    response.put("status", QUEUED);
    return response;
  }

  protected Map<String,Object> queueReportGeneration(int year) {
    if (!utility.getReportGenerationStatus(year).isEmpty()) {
      throw new ValidationException("ui.error.report.generation.already.in.progress");
    }
    utility.updateReportGenerationStatus(year, QUEUED);
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", utility.getLoggedInSchema());
    jobData.put("userName", utility.getLoggedInUser());
    jobData.put("centerId", utility.getLoggedInCenterId());
    jobData.put("reportingYear", year);
    jobService.scheduleImmediate(
        buildJob("OhsrsdohgovphReportGenerationJob_" 
           + String.valueOf(utility.getLoggedInCenterId()) + "_" + String.valueOf(year), 
           OhsrsdohgovphReportGenerationJob.class, jobData));
    Map<String, Object> response = new HashMap<>();
    response.put("status", QUEUED);
    return response;
  }
  
  protected boolean generateReport(int year) {
    utility.updateReportGenerationStatus(year, IN_PROGRESS);
    boolean status = true;
    int centerId = utility.getLoggedInCenterId();
    String user = utility.getLoggedInUser();
    for (String ohsrsFunction : ohsrsFunctionProvider.getSupportedOhsrsFunctions()) {
      GenericOhsrsFunction processor = null;
      try {
        processor = ohsrsFunctionProvider.getProcessor(ohsrsFunction);
      } catch (IllegalAccessException ex) {
        logger.error("", ex);
      } catch (InstantiationException ex) {
        logger.error("", ex);
      }
      if (processor == null) {
        status &= false;
        continue;
      } 
      try {
        status &= processor.process(year);
        reportStatusRepo.upsertStatus(centerId, year, ohsrsFunction, 
            OhsrsdohgovphReportStatusRepository.STATUS_GENERATED, "", user);
      } catch (Exception ex) {
        logger.error("", ex);
        status &= false;
        reportStatusRepo.upsertStatus(centerId, year, ohsrsFunction, 
            OhsrsdohgovphReportStatusRepository.STATUS_GENERATION_FAILED, ex.getMessage(), user);
      }
      
    }
    utility.removeReportGenerationStatus(year);
    return status;
  }
  
  protected List<Integer> reportYears() {
    BasicDynaBean center = centerService.findByKey(utility.getLoggedInCenterId());
    Date goliveDate = (Date) center.get("golive_date");
    int startYear = goliveDate != null ? DateUtil.getYear(goliveDate) : 2018;
    int endYear = DateUtil.getYear(DateUtil.getCurrentDate());
    List<Integer> list = new ArrayList<>();
    for (int year = startYear; year <= endYear; year++) {
      list.add(year);
    }
    return list;
  }
  
  /**
   * Set meta data to view for rendering report UI.
   * @param mav Model and View where meta is to be set
   */
  public void setViewMeta(ModelAndView mav) {
    BasicDynaBean center = centerService.findByKey(utility.getLoggedInCenterId());
    Date goliveDate = (Date) center.get("golive_date");
    boolean midyearGolive = false;
    if (goliveDate != null) {
      java.util.Date goliveYearStart = DateUtil.getFirstDayOfYear(goliveDate);
      midyearGolive = !DateUtil.formatDate(goliveDate)
          .equals(DateUtil.formatDate(goliveYearStart));
    }
    mav.addObject("renderer_meta", utility.getRendererMetaRaw());
    mav.addObject("ohsrs_functions", ohsrsFunctionProvider.getSupportedOhsrsFunctions());
    mav.addObject("reporting_years", reportYears());
    mav.addObject("midyearGolive", midyearGolive);
  }
  
}