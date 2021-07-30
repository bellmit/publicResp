package com.insta.hms.common.datauploaddownload;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.mdm.FileOperationService;
import com.insta.hms.mdm.codesets.CodeSetsService;
import com.insta.hms.mdm.codesets.CodeSystemCategoriesRepository;
import com.insta.hms.mdm.codesets.CodeSystemsRepository;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.insuranceplantypes.InsurancePlanTypeService;
import com.insta.hms.mdm.tpas.TpaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BulkUploadDownloadService {

  /** RedisTemplate. */
  @LazyAutowired
  private RedisTemplate<String, Object> template;

  /** Session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** File operation service. */
  @LazyAutowired
  private FileOperationService fileOperationService;

  @LazyAutowired
  private InsuranceCompanyService insuranceCompanyService;

  /** Insurance plan service. */
  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  /** TPA Service. */
  @LazyAutowired
  private TpaService tpaService;

  /** Insurance plan type service. */
  @LazyAutowired
  private InsurancePlanTypeService insurancePlanTypeService;

  /** Registration service. */
  @LazyAutowired
  private RegistrationService registrationService;
  
  @LazyAutowired
  private CodeSetsService codeSetsService;

  /** The message util. */
  @LazyAutowired
  private MessageUtil messageUtil;
  
  @LazyAutowired
  private CodeSystemsRepository codeSystemsRepository;
  
  @LazyAutowired
  private CodeSystemCategoriesRepository codeSystemCategoriesRepository;

  private static Logger logger = LoggerFactory.getLogger(BulkUploadDownloadService.class);

  /**
   * gets all redis keys.
   * 
   * @return keysList rediskeylist
   */
  private List<String> getAllKeys() {
    String keyTemplate = String.format("schema:%s;user:%s;file:%s;uid:%s",
        RequestContext.getSchema(), sessionService.getSessionAttributes().get("userId"), "*", "*");

    Set<String> redisKeys = template.keys(keyTemplate);
    // Store the keys in a List
    List<String> keysList = new ArrayList<>();
    Iterator<String> it = redisKeys.iterator();
    while (it.hasNext()) {
      String data = it.next();
      keysList.add(data);
    }
    return keysList;
  }

  /**
   * Gets status of all job and the download paths if any file generation is
   * completed.
   * 
   * @return a list of list containing status,filename and download link of all
   *         files
   */
  public List<Map<String, String>> getAllBulkUploadDownloadJobs() {
    List<Map<String, String>> listValues = new ArrayList<>();
    Map<String, String> entry = null;
    for (String key : getAllKeys()) {
      entry = new HashMap<>();
      entry.put("id", key);
      String value = (String) template.opsForValue().get(key);

      // Sample template.opsForValue().get(key) value as below,
      // status:%s;action:%s;master:%s;startedAt:%s;completedAt:%s;file:%s
      String[] tokenize = value.split(";");

      String[] status = tokenize[0].split(":");
      entry.put("status", status[1]);

      String[] action = tokenize[1].split(":");
      entry.put("action", action[1]);

      String[] master = tokenize[2].split(":");
      entry.put("master", master[1]);

      entry.put("startedAt", tokenize[3].replace("startedAt:", ""));
      entry.put("completedAt", tokenize[4].replace("completedAt:", ""));

      String[] file = tokenize[5].split(":");
      if (file.length > 1) {
        entry.put("fileName", file[1]);
      } else {
        entry.put("fileName", "");
      }

      listValues.add(entry);
    }

    // sorting values based on creation time (descending)
    java.util.Collections.sort(listValues, new Comparator<Map<String, String>>() {
      @Override
      public int compare(Map<String, String> first, Map<String, String> second) {
        String firstCreationTime = first.get("startedAt");
        String secondCreationTime = second.get("startedAt");
        if (null == firstCreationTime && null == secondCreationTime) {
          return 0;
        }
        if (firstCreationTime == null) {
          return -1;
        }
        if (secondCreationTime == null) {
          return 1;
        }
        return secondCreationTime.compareTo(firstCreationTime);
      }
    });
    return listValues;
  }

  /**
   * Gets file path by Redis Key.
   *
   * @param redisKey report id for which status is to be returned
   * 
   * @return file path for given redis key
   */
  public String getFilePathForRedisKey(String redisKey) {
    String value = (String) template.opsForValue().get(redisKey);
    String[] tokenize = value.split(";");
    String generatedAt = null;
    String downloadPath = null;
    String[] folder = tokenize[2].split(":");
    String[] fileName = tokenize[1].split(":");
    String[] status = tokenize[0].split(":");
    if (tokenize.length > 3) { // check to support old redis keys
      generatedAt = tokenize[3].split(":")[1] + ":" + tokenize[3].split(":")[2] + ":"
          + tokenize[3].split(":")[3];
      String fileExtension = fileName[1].substring(fileName[1].lastIndexOf("."),
          fileName[1].length());
      String fileNamePart = fileName[1].substring(0, fileName[1].lastIndexOf("."));

      if (status[1].equals("completed")) {
        downloadPath = EnvironmentUtil.getTempDirectory() + File.separator + folder[1]
            + File.separator + fileNamePart + generatedAt + fileExtension;
      } else {
        downloadPath = EnvironmentUtil.getTempDirectory() + File.separator + folder[1]
            + File.separator + fileNamePart + generatedAt + "-errors.txt";
      }
    } else {
      downloadPath = EnvironmentUtil.getTempDirectory() + File.separator + folder[1]
          + File.separator + fileName[1];
    }
    return downloadPath;
  }

  /**
   * Get status by Redis Key.
   *
   * @param redisKey report id for which status is to be returned
   *
   * @return file path for given redis key
   */

  public String getStatusForRedisKey(String redisKey) {
    String value = (String) template.opsForValue().get(redisKey);
    if (value == null) {
      return value;
    }
    String[] tokenize = value.split(";");
    String[] status = tokenize[0].split(":");
    return status[1];
  }

  /**
   * uploaddownload method.
   * 
   * @param map the map
   * @throws IOException exception
   */
  public void uploadDownload(Map<String, Object> map) throws IOException {
    fileOperationService.bulkDataOperation(map);
  }

  /**
   * gets the file by using redis key.
   * 
   * @param id redis key
   * @return file the file
   */
  public File getFile(String id) {
    String value = (String) template.opsForValue().get(id);
    String[] tokenize = value.split(";");
    String[] fileName = tokenize[5].split(":");
    if (fileName.length > 1) {
      return new File(fileName[1]);
    } else {
      logger.warn("File not available for redis id : " + id);
      return null;
    }
  }

  /**
   * gets the message of redis key.
   * 
   * @param id redis key
   * @return message from redis value
   */
  public String getMessage(String id) {
    String value = (String) template.opsForValue().get(id);
    String[] tokenize = value.split(";");
    return tokenize[6].replaceFirst("message:", "").replaceFirst("Unresolved key:", "");
  }

  /**
   * import bulk data into insurance company master.
   * 
   * @param map the map
   * @return boolean value
   * @throws IOException  the exception
   * @throws SQLException the exception
   */
  public boolean bulkUploadInsuranceCompany(Map<String, Object> map)
      throws IOException, SQLException {
    File file = (File) map.get("file");
    String[] mandatoryColumnsInsCompany = { "insurance_co_name" };
    String[] mandatoryColumnsHealthAuthority = null;
    String[] mandatoryColumnsInsCategories = null;
    Map<String, String[]> mandateColumnInExcelFile = new HashMap<String, String[]>();
    mandateColumnInExcelFile.put("InsuranceCompanies", mandatoryColumnsInsCompany);
    mandateColumnInExcelFile.put("HealthAuthorityCode", mandatoryColumnsHealthAuthority);
    mandateColumnInExcelFile.put("InsuranceItemCategories", mandatoryColumnsInsCategories);
    Map<String, List<Map<String, Object>>> insuranceCompanyDataMap = fileOperationService
        .validateExcelFileAndConvertToMapList(file, mandateColumnInExcelFile);
    if (!insuranceCompanyDataMap.get("InsuranceCompanies").isEmpty()) {
      insuranceCompanyService.importBulkInsuranceCompanies(insuranceCompanyDataMap);
      return true;
    } else {
      throw new HMSException(messageUtil.getMessage("exception.no.data.found.to.upload"));
    }
  }

  /**
   * import bulk data into insurance plan master.
   * 
   * @param map the map
   * @return boolean value
   * @throws IOException exception
   */
  public boolean bulkUploadInsurancePlan(Map<String, Object> map) throws IOException {
    String[] mandatoryColumnsInsDetails = { "plan_name", "insurance_company_name", "sponsor",
        "plan_type", "limit_type", "copay_percent_applicable_on_post_discounted_amount",
        "ip_applicable", "op_applicable" };
    String[] mandatoryColumnsInsCategories = { "plan_name", "insurance_category_name",
        "insurance_payable" };
    Map<String, String[]> mandateColumnInExcelFile = new HashMap<String, String[]>();
    mandateColumnInExcelFile.put("PlanDetails", mandatoryColumnsInsDetails);
    mandateColumnInExcelFile.put("Copay_Limit_OP", mandatoryColumnsInsCategories);
    mandateColumnInExcelFile.put("Copay_Limit_IP", mandatoryColumnsInsCategories);
    Map<String, List<Map<String, Object>>> insurancePlanDataMap = fileOperationService
        .validateExcelFileAndConvertToMapList((File) map.get("file"), mandateColumnInExcelFile);
    if (!insurancePlanDataMap.get("PlanDetails").isEmpty()) {
      insurancePlanService.importBulkInsurancePlans(insurancePlanDataMap);
      return true;
    } else {
      throw new HMSException(messageUtil.getMessage("exception.no.data.found.to.upload"));
    }
  }

  /**
   * import bulk tpa/sponsors.
   * 
   * @param map the map
   * @return boolean value
   * @throws IOException  exception
   * @throws SQLException exception
   */
  public boolean bulkUploadTpa(Map<String, Object> map) throws IOException, SQLException {
    String[] mandatoryColumnsTpa = { "tpa_name", "country", "state", "city", "claim_format",
        "tpa_type", "scanned_doc_upload", "prior_authorization_mode", "duplicate_membership_id",
        "claim_Amount_includes_tax", "limit_includes_tax" };
    String[] mandatoryColumnsHealthAuthority = null;
    Map<String, String[]> mandateColumnInExcelFile = new HashMap<String, String[]>();
    mandateColumnInExcelFile.put("TPA", mandatoryColumnsTpa);
    mandateColumnInExcelFile.put("HealthAuthorityCode", mandatoryColumnsHealthAuthority);
    Map<String, List<Map<String, Object>>> tpaDataMap = fileOperationService
        .validateExcelFileAndConvertToMapList((File) map.get("file"), mandateColumnInExcelFile);
    if (!tpaDataMap.get("TPA").isEmpty()) {
      tpaService.importBulkTpa(tpaDataMap);
      return true;
    } else {
      throw new HMSException(messageUtil.getMessage("exception.no.data.found.to.upload"));
    }
  }

  /**
   * import bulk insurance category.
   * 
   * @param map the map
   * @return boolean value
   * @throws IOException  exception
   * @throws SQLException exception
   */
  public boolean bulkUploadInsurancePlanType(Map<String, Object> map)
      throws IOException, SQLException {
    String[] mandatoryColumnsTpa = { "category_name", "insurance_company_name" };
    Map<String, String[]> mandateColumnInExcelFile = new HashMap<String, String[]>();
    mandateColumnInExcelFile.put("InsurancePlanType", mandatoryColumnsTpa);
    Map<String, List<Map<String, Object>>> insCatDataMap = fileOperationService
        .validateExcelFileAndConvertToMapList((File) map.get("file"), mandateColumnInExcelFile);
    if (!insCatDataMap.get("InsurancePlanType").isEmpty()) {
      insurancePlanTypeService.importBulkInsuranceCategory(insCatDataMap);
      return true;
    } else {
      throw new HMSException(messageUtil.getMessage("exception.no.data.found.to.upload"));
    }
  }

  /**
   * import bulk patient data.
   * 
   * @param map the map
   * @return boolean value
   * @throws IOException  exception
   * @throws SQLException exception
   */
  public boolean bulkUploadPatientData(Map<String, Object> map) throws IOException, SQLException {
    String[] mandatoryColumns = { "title", "first_name", "gender", "dob", "country", "state" };
    Map<String, String[]> mandateColumnInExcelFile = new HashMap<String, String[]>();
    mandateColumnInExcelFile.put("PatientData", mandatoryColumns);
    Map<String, List<Map<String, Object>>> patientDataMap = fileOperationService
        .validateExcelFileAndConvertToMapList((File) map.get("file"), mandateColumnInExcelFile);
    List<Map<String, Object>> patientDataList = patientDataMap.get("PatientData");
    if (!patientDataList.isEmpty()) {
      registrationService.importBulkPatientData(patientDataList);
      return true;
    } else {
      throw new HMSException(messageUtil.getMessage("exception.no.data.found.to.upload"));
    }
  }

  /**
   * Bulk Download patient data.
   * 
   * @param map the map
   * @return file the file
   * @throws IOException the exception
   */
  public File bulkDownloadPatientData(Map<String, Object> map) throws IOException {
    File file = null;
    Map<String, Object> fileInfo = new HashMap<String, Object>();

    if (!map.get("template").equals("Y")) {
      file = new File(map.get("filePath") + File.separator + "PatientData.xlsx");
      // Get the data yet to implement
    } else {
      file = new File(map.get("filePath") + File.separator + "PatientData-Template.xlsx");
    }
    fileInfo.put("template", map.get("template"));
    fileInfo.put("file", file);
    List<String> sheetNames = new ArrayList<String>();
    sheetNames.add("PatientData");
    fileInfo.put("SheetNames", sheetNames);

    String[] header = { "New_Mr_No", "Old_Mr_No", "Patient_Category", "Title", "First_Name",
        "Middle_Name", "Last_Name", "Gender", "DOB", "Phone_No", "Additional_Phone", "Address",
        "Area", "City", "State", "Country", "FirstVisitDate", "Nationality", "Email_ID",
        "Field1Name", "Field1Value", "Field2Name", "Field2Value", "Field3Name", "Field3Value",
        "Field4Name", "Field4Value", "Field5Name", "Field5Value", "Field6Name", "Field6Value",
        "Field7Name", "Field7Value", "Field8Name", "Field8Value", "Field9Name", "Field9Value",
        "Field10Name", "Field10Value", "Field11Name", "Field11Value", "Field12Name", "Field12Value",
        "Field13Name", "Field13Value" };
    Map<String, Object> patientDataSheet = new HashMap<String, Object>();
    patientDataSheet.put("header", header);

    Map<String, Map<String, Object>> sheetInfo = new HashMap<String, Map<String, Object>>();
    sheetInfo.put("PatientData", patientDataSheet);
    file = fileOperationService.createExcelFile(fileInfo, sheetInfo);
    return file;
  }

  /**
   * Bulk Download insurance company data.
   * 
   * @param map the map
   * @return file the file
   * @throws IOException the exception
   */
  public File bulkDownloadInsuranceCompany(Map<String, Object> map) throws IOException {
    File file = null;
    Map<String, Object> fileInfo = new HashMap<String, Object>();

    if (!map.get("template").equals("Y")) {
      file = new File(map.get("filePath") + File.separator + "InsuranceCompanies.xlsx");
      // Get the data yet to implement
    } else {
      file = new File(map.get("filePath") + File.separator + "InsuranceCompanies-Template.xlsx");
    }
    fileInfo.put("template", map.get("template"));
    fileInfo.put("file", file);
    List<String> sheetNames = new ArrayList<String>();
    sheetNames.add("InsuranceCompanies");
    sheetNames.add("HealthAuthorityCode");
    sheetNames.add("InsuranceItemCategories");
    fileInfo.put("SheetNames", sheetNames);

    String[] insCompHeader = { "Insurance_co_Name", "Default_Rate_Plan", "Status", "Address",
        "City", "State", "Country", "Mobile", "Email", "TIN_Number", "Interface_Code" };
    Map<String, Object> insCompDataSheet = new HashMap<String, Object>();
    insCompDataSheet.put("header", insCompHeader);

    String[] haHeader = { "Insurance_co_Name", "Health_Authority", "Insurance_Co_Code" };
    Map<String, Object> haDataSheet = new HashMap<String, Object>();
    haDataSheet.put("header", haHeader);

    String[] insCatHeader = { "Insurance_co_Name", "Insurance_Item_Category" };
    Map<String, Object> insCatDataSheet = new HashMap<String, Object>();
    insCatDataSheet.put("header", insCatHeader);

    Map<String, Map<String, Object>> sheetInfo = new HashMap<String, Map<String, Object>>();
    sheetInfo.put("InsuranceCompanies", insCompDataSheet);
    sheetInfo.put("HealthAuthorityCode", haDataSheet);
    sheetInfo.put("InsuranceItemCategories", insCatDataSheet);

    file = fileOperationService.createExcelFile(fileInfo, sheetInfo);
    return file;
  }

  /**
   * Bulk Download insurance plan data.
   * 
   * @param map the map
   * @return file the file
   * @throws IOException the exception
   */
  public File bulkDownloadInsurancePlan(Map<String, Object> map) throws IOException {
    File file = null;
    Map<String, Object> fileInfo = new HashMap<String, Object>();

    if (!map.get("template").equals("Y")) {
      file = new File(map.get("filePath") + File.separator + "InsurancePlanDetails.xlsx");
      // Get the data yet to implement
    } else {
      file = new File(map.get("filePath") + File.separator + "InsurancePlanDetails-Template.xlsx");
    }
    fileInfo.put("template", map.get("template"));
    fileInfo.put("file", file);
    List<String> sheetNames = new ArrayList<String>();
    sheetNames.add("PlanDetails");
    sheetNames.add("Copay_Limit_OP");
    sheetNames.add("Copay_Limit_IP");
    fileInfo.put("SheetNames", sheetNames);

    String[] insPlanHeader = { "Plan_Name", "Insurance_Company_Name",
        "Insurance_Validity_Start_Date", "Insurance_Validity_End_Date", "Interface_Code",
        "Plan_Notes", "Plan_Exclusions", "Plan_Code", "Sponsor", "Plan Type", "Default_Rate_Plan",
        "Default_Discount_Plan", "Copay_Percent_Applicable_On_Post_Discounted_Amount",
        "IP_Applicable", "OP_Applicable", "Limit_Type", "Number_of_Case_Rates_Allowed", "Base_Rate",
        "Gap_Amount", "Marginal_Percent", "Add_On_Payment_Factor", "Perdiem_Copay_Per",
        "Perdiem_Copay_Amount", "Limits_Include_Followup", "OP_Plan_Limit", "OP_Visit_Limit",
        "OP_Episode_Limit", "OP_Visit_Deductible", "OP_Copay_Percent", "OP_Visit_Copay_Limit",
        "IP_Plan_Limit", "IP_Visit_Limit", "IP_Per_Day_Limit", "IP_Visit_Deductible",
        "IP_Copay_Percent", "IP_Visit_Copay_Limit" };
    Map<String, Object> insPlanDataSheet = new HashMap<String, Object>();
    insPlanDataSheet.put("header", insPlanHeader);

    String[] copaylimitHeader = { "Plan_Name", "Insurance_Category_Name", "Insurance_Payable",
        "Category_Prior_Auth_Required", "Sponsor_Limit", "Deductible_Category", "Deductible_Item",
        "Copay_Percentage", "Max_Copay" };
    Map<String, Object> copayDataSheet = new HashMap<String, Object>();
    copayDataSheet.put("header", copaylimitHeader);

    Map<String, Map<String, Object>> sheetInfo = new HashMap<String, Map<String, Object>>();
    sheetInfo.put("PlanDetails", insPlanDataSheet);
    sheetInfo.put("Copay_Limit_OP", copayDataSheet);
    sheetInfo.put("Copay_Limit_IP", copayDataSheet);

    file = fileOperationService.createExcelFile(fileInfo, sheetInfo);
    return file;
  }

  /**
   * Bulk Download TPA data.
   * 
   * @param map the map
   * @return file the file
   * @throws IOException the exception
   */
  public File bulkDownloadTpa(Map<String, Object> map) throws IOException {
    File file = null;
    Map<String, Object> fileInfo = new HashMap<String, Object>();

    if (!map.get("template").equals("Y")) {
      file = new File(map.get("filePath") + File.separator + "TPADetails.xlsx");
      // Get the data yet to implement
    } else {
      file = new File(map.get("filePath") + File.separator + "TPADetails-Template.xlsx");
    }
    fileInfo.put("template", map.get("template"));
    fileInfo.put("file", file);
    List<String> sheetNames = new ArrayList<String>();
    sheetNames.add("TPA");
    sheetNames.add("HealthAuthorityCode");
    fileInfo.put("SheetNames", sheetNames);

    String[] tpaHeader = { "TPA_Name", "Email_id", "Status", "Address", "Fax",
        "TPA_Prior_Auth_Form", "Mobile_no", "Phone_no", "Pincode", "Country", "State", "City",
        "Validity_End_Date", "TPA_Claim_Form", "Claim_Format", "TPA_Type", "Scanned_Doc_Upload",
        "Prior_authorization_Mode", "Member_Id_Pattern", "Duplicate_Membership_id",
        "Child_duplicate_mem_id_days", "TIN_number", "Claim_Amount_Includes_Tax",
        "Limit_Includes_Tax", "Max_Resubmission_Count", "Contact_Name", "Contact_Designation",
        "Contact_Phone", "Contact_Mobile", "Contact_Email" };
    Map<String, Object> tpaDataSheet = new HashMap<String, Object>();
    tpaDataSheet.put("header", tpaHeader);

    String[] haHeader = { "TPA_Name", "Health_Authority", "TPA_Code",
        "Enable_Eligibility_Authorization", "Enable_Eligibility_Auth_in_XML" };
    Map<String, Object> haDataSheet = new HashMap<String, Object>();
    haDataSheet.put("header", haHeader);

    Map<String, Map<String, Object>> sheetInfo = new HashMap<String, Map<String, Object>>();
    sheetInfo.put("TPA", tpaDataSheet);
    sheetInfo.put("HealthAuthorityCode", haDataSheet);

    file = fileOperationService.createExcelFile(fileInfo, sheetInfo);
    return file;
  }

  /**
   * Bulk Download Insurance plan type data.
   * 
   * @param map the map
   * @return file the file
   * @throws IOException the exception
   */
  public File bulkDownloadInsurancePlanType(Map<String, Object> map) throws IOException {
    File file = null;
    Map<String, Object> fileInfo = new HashMap<String, Object>();

    if (!map.get("template").equals("Y")) {
      file = new File(map.get("filePath") + File.separator + "InsurancePlanType.xlsx");
      // Get the data yet to implement
    } else {
      file = new File(map.get("filePath") + File.separator + "InsurancePlanType-Template.xlsx");
    }
    fileInfo.put("template", map.get("template"));
    fileInfo.put("file", file);
    List<String> sheetNames = new ArrayList<String>();
    sheetNames.add("InsurancePlanType");
    fileInfo.put("SheetNames", sheetNames);

    String[] insPlanTypeHeader = { "Category_Name", "Insurance_Company_Name", "Status" };
    Map<String, Object> insPlanTypeDataSheet = new HashMap<String, Object>();
    insPlanTypeDataSheet.put("header", insPlanTypeHeader);

    Map<String, Map<String, Object>> sheetInfo = new HashMap<String, Map<String, Object>>();
    sheetInfo.put("InsurancePlanType", insPlanTypeDataSheet);

    file = fileOperationService.createExcelFile(fileInfo, sheetInfo);
    return file;
  }
  
  /**
   * Bulk Upload code sets.
   * 
   * @param map the map
   * @return boolean
   * @throws IOException exception
   */
  public boolean bulkUploadCodeSets(Map<String, Object> map) throws IOException {
    String[] mandatoryColumns =
        {"master_name", "code_system", "entity_name", "code", "code_description"};
    Map<String, String[]> mandateColumnInExcelFile = new HashMap<>();
    mandateColumnInExcelFile.put("CodeSets", mandatoryColumns);
    Map<String, List<Map<String, Object>>> codeSetsMap = fileOperationService
        .validateExcelFileAndConvertToMapList((File) map.get("file"), mandateColumnInExcelFile);
    List<Map<String, Object>> codeSetsMapList = codeSetsMap.get("CodeSets");
    if (!codeSetsMapList.isEmpty()) {
      codeSetsService.importBulkCodeSets(codeSetsMapList);
      return true;
    } else {
      throw new HMSException(messageUtil.getMessage("exception.no.data.found.to.upload"));
    }
  }
  
  /**
   * Bulk Download Code sets data.
   * 
   * @param map the map
   * @return file the file
   * @throws IOException the exception
   */
  public File bulkDownloadCodeSets(Map<String, Object> map) throws IOException {
    String codeSystemsLabel = "";
    if (!StringUtils.isEmpty(map.get("code_systems_id"))) {
      codeSystemsLabel = (String) codeSystemsRepository
          .findByKey("id", Integer.parseInt((String)map.get("code_systems_id"))).get("label");
      codeSystemsLabel = codeSystemsLabel.replaceAll(" ", "_");
    }
    String codeSystemCategoryLabel = "";
    if (!StringUtils.isEmpty(map.get("code_system_category_id"))) {
      codeSystemCategoryLabel = (String) codeSystemCategoriesRepository
          .findByKey("id", Integer.parseInt((String) map.get("code_system_category_id")))
          .get("label");
      codeSystemCategoryLabel = codeSystemCategoryLabel.replaceAll(" ", "_");
    }
    File file = null;
    if (!map.get("template").equals("Y")) {
      file = new File(map.get("filePath") + File.separator + "CodeSets-" + codeSystemCategoryLabel
          + "-" + codeSystemsLabel + ".xlsx");
    } else {
      file = new File(map.get("filePath") + File.separator + "CodeSets-" + codeSystemCategoryLabel
          + "-" + codeSystemsLabel + "-Template.xlsx");
    }
    Map<String, Object> fileInfo = new HashMap<>();
    fileInfo.put("template", map.get("template"));
    fileInfo.put("file", file);
    List<String> sheetNames = new ArrayList<>();
    sheetNames.add("CodeSets");
    fileInfo.put("SheetNames", sheetNames);

    String[] codeSetsHeader =
        {"master_name", "code_system", "entity_name", "code", "code_description"};
    Map<String, Object> codeSetSheet = new HashMap<>();
    codeSetSheet.put("header", codeSetsHeader);

    Map<String, Map<String, Object>> sheetInfo = new HashMap<>();
    sheetInfo.put("CodeSets", codeSetSheet);

    file = fileOperationService.createExcelFile(fileInfo, sheetInfo);

    if (!"Y".equals(map.get("template"))) {
      Map<String, String[]> params = new HashMap<>();
      String[] codeSystemsId = new String[1];
      String[] codeSystemCategoryId = new String[1];
      String[] pageNumber = new String[1];
      codeSystemsId[0] = String.valueOf(map.get("code_systems_id"));
      codeSystemCategoryId[0] = String.valueOf(map.get("code_system_category_id"));
      params.put("code_systems_id", codeSystemsId);
      params.put("code_system_category_id", codeSystemCategoryId);
      fileInfo.put("ExcelFile", file);
      PagedList pagedList = codeSetsService.searchForDownload(params);
      if (pagedList != null) {
        int maxLoopCount = (pagedList.getTotalRecords() / pagedList.getPageSize()) + 1;
        for (int i = 1; i <= maxLoopCount; i++) {
          pageNumber[0] = String.valueOf(i);
          params.put("pageNum", pageNumber);
          pagedList = codeSetsService.searchForDownload(params);
          if (pagedList.getDtoList().isEmpty()) {
            break;
          }
          codeSetSheet.put("data", pagedList.getDtoList());
          file = fileOperationService.appendDataToExcelFile(fileInfo, sheetInfo);
        }
      }
    }
    return file;
  }
}
