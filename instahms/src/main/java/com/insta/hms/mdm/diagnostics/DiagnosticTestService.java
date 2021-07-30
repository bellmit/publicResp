package com.insta.hms.mdm.diagnostics;

import au.com.bytecode.opencsv.CSVReader;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.CommonUtils;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.scheduler.resourcelist.SchedulerResourceRepository;
import com.insta.hms.diagnosticsmasters.addtest.TestCharge;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.jobs.MasterChargesCronSchedulerDetailsRepository;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentService;
import com.insta.hms.mdm.diagexportinterfaces.DiagExportInterfaceService;
import com.insta.hms.mdm.diagmethodologies.DiagMethodologyService;
import com.insta.hms.mdm.diagnosticcharges.DiagnosticChargeService;
import com.insta.hms.mdm.diagtatcenter.DiagTatCenterService;
import com.insta.hms.mdm.diagtestresults.DiagTestResultService;
import com.insta.hms.mdm.histoimpressions.HistoImpressionService;
import com.insta.hms.mdm.hl7interfaces.Hl7interfaceService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryService;
import com.insta.hms.mdm.microabstantibiotics.MicroAbstAntibioticService;
import com.insta.hms.mdm.mrdcodesupport.MrdCodeSupportService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;
import com.insta.hms.mdm.testequipments.TestEquipmentRepository;
import com.insta.hms.mdm.testorganization.TestOrganizationService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class DiagnosticTestService.
 *
 * @author anil.n
 */
@Service
public class DiagnosticTestService extends BulkDataService {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DiagnosticTestService.class);

  /** The message util. */
  @LazyAutowired
  MessageUtil messageUtil;

  /** The bed type service. */
  @LazyAutowired
  private BedTypeService bedTypeService;

  /** The diag test repository. */
  @LazyAutowired
  private DiagnosticTestRepository diagTestRepository;

  /** The validator. */
  @LazyAutowired
  private DiagnosticTestValidator validator;

  /** The diag department service. */
  @LazyAutowired
  private DiagDepartmentService diagDepartmentService;

  /** The service sub group service. */
  @LazyAutowired
  private ServiceSubGroupService serviceSubGroupService;

  /** The hl 7 interface service. */
  @LazyAutowired
  private Hl7interfaceService hl7interfaceService;

  /** The mrd code support service. */
  @LazyAutowired
  private MrdCodeSupportService mrdCodeSupportService;

  /** The histo impression service. */
  @LazyAutowired
  private HistoImpressionService histoImpressionService;

  /** The micro abst antibiotic service. */
  @LazyAutowired
  private MicroAbstAntibioticService microAbstAntibioticService;

  /** The diag export interface service. */
  @LazyAutowired
  private DiagExportInterfaceService diagExportInterfaceService;

  /** The diag test result service. */
  @LazyAutowired
  private DiagTestResultService diagTestResultService;

  /** The diag methodology service. */
  @LazyAutowired
  private DiagMethodologyService diagMethodologyService;

  /** The organization service. */
  @LazyAutowired
  private OrganizationService organizationService;

  /** The test organization service. */
  @LazyAutowired
  private TestOrganizationService testOrganizationService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The diag tat center service. */
  @LazyAutowired
  private DiagTatCenterService diagTatCenterService;

  /** The diagnostic charge service. */
  @LazyAutowired
  private DiagnosticChargeService diagnosticChargeService;

  /** The insurance item category service. */
  @LazyAutowired
  private InsuranceItemCategoryService insuranceItemCategoryService;

  /** The service group service. */
  @LazyAutowired
  private ServiceGroupService serviceGroupService;

  /** The hospital role service. */
  @LazyAutowired
  private HospitalRoleService hospitalRoleService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The tax group service. */
  @LazyAutowired
  private TaxGroupService taxGroupService;

  /** The tax sub group service. */
  @LazyAutowired
  private TaxSubGroupService taxSubGroupService;

  /** The Diagnostics Test Insurance Category Mapping Repository. */
  @LazyAutowired
  private DiagnosticsTestInsuranceCategoryRepository diagnosticsTestInsuranceCategoryRepository;
  /** Test Equipment master. */
  @LazyAutowired
  private TestEquipmentRepository testEquipmentRepository;
  /** Scheduler master. **/
  @LazyAutowired
  private SchedulerResourceRepository schedulerResourceRepository;

  /** The Master Charges Cron Scheduler Details Repository. */
  @LazyAutowired
  private MasterChargesCronSchedulerDetailsRepository masterChargesCronSchedulerDetailsRepository;

  /**
   * Instantiates a new diagnostic test service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   * @param csvDataEntity
   *          the csv data entity
   */
  public DiagnosticTestService(DiagnosticTestRepository repo, DiagnosticTestValidator validator,
      DiagnosticTestCsvBulkDataEntity csvDataEntity) {
    super(repo, validator, csvDataEntity);
  }

  /**
   * Gets the search query assembler.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the search query assembler
   */
  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.MasterService#getSearchQueryAssembler(java.util.Map, java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  protected SearchQueryAssembler getSearchQueryAssembler(Map params,
      Map<LISTING, Object> listingParams) {
    SearchQueryAssembler qa = super.getSearchQueryAssembler(params, listingParams);
    qa.addSecondarySort("test_id");
    return qa;
  }

  /**
   * Search.
   *
   * @param params
   *          the params
   * @param listingParams
   *          the listing params
   * @return the paged list
   */
  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.MasterService#search(java.util.Map, java.util.Map)
   */
  @Override
  public PagedList search(Map params, Map<LISTING, Object> listingParams) {
    Map paramsMap = new HashMap(params);
    Object orgId = paramsMap.get("org_id");
    if (orgId == null || orgId.equals("")) {
      String[] defaultOrgId = { "ORG0001" };
      paramsMap.put("org_id", defaultOrgId);
    }
    return super.search(paramsMap, listingParams, false);
  }

  /**
   * Gets the list page data.
   *
   * @param requestParams
   *          the request params
   * @return the list page data
   */
  public Map<String, List<BasicDynaBean>> getListPageData(Map requestParams) {

    String orgId = null;
    String[] orgid = (String[]) requestParams.get("org_id");
    if (orgid == null || orgid.equals("")) {
      orgId = "ORG0001";
    } else {
      orgId = orgid[0];
    }
    Map<String, List<BasicDynaBean>> beanMap = new HashMap<String, List<BasicDynaBean>>();
    PagedList list = diagTestRepository.getTestDetails(requestParams,
        ConversionUtils.getListingParameter(requestParams));
    List<String> testIds = new ArrayList<String>();
    for (Map obj : (List<Map>) list.getDtoList()) {
      testIds.add((String) obj.get("test_id"));
    }

    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    List<BasicDynaBean> testChargesList = diagTestRepository.getTestChargesForAllBedTypes(orgId,
        bedTypes, testIds);

    List<BasicDynaBean> testnames = ((DiagnosticTestRepository) getRepository()).getAllTestNames();
    beanMap.put("orgMasterData", organizationService.getRateSheetForCharge());
    beanMap.put("serviceSubGroup", serviceSubGroupService.listOrderActiveRecord());
    beanMap.put("diagDepts", diagDepartmentService.getActiveDiagDepartments());
    beanMap.put("testnames", testnames);
    beanMap.put("bedTypes", bedTypes);
    beanMap.put("testCharges", testChargesList);
    Map<String, Object> cronJobKeys = new HashMap<String, Object>();
    cronJobKeys.put("entity", "DIAGNOSTIC");
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    cronJobKeys.put("status", status);
    List<BasicDynaBean> masterCronJobDetails =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(cronJobKeys);
    beanMap.put("masterCronJobDeatils", masterCronJobDetails);
    return beanMap;
  }

  /**
   * Gets the adds the page data.
   *
   * @param requestParams
   *          the request params
   * @return the adds the page data
   */
  public Map<String, List<BasicDynaBean>> getAddPageData(Map requestParams) {

    Map<String, List<BasicDynaBean>> beanMap = new HashMap<>();
    List<BasicDynaBean> diagDepartmentList = diagDepartmentService.getDiagDepartments();
    beanMap.put("diagdepts", diagDepartmentList);
    beanMap.put("testList", diagTestRepository.getTestNames());
    beanMap.put("reportformats", diagTestRepository.getReportFormats());
    List<BasicDynaBean> serviceGroups = serviceGroupService.getAllServiceGroups();
    beanMap.put("serviceGroups", serviceGroups);
    List<BasicDynaBean> serviceSubGroupList = serviceSubGroupService.listActiveRecord();
    beanMap.put("serviceSubGroup", serviceSubGroupList);
    List<BasicDynaBean> sampleTypesList = diagTestRepository.getActiveSampleTypeList();
    beanMap.put("sampleTypes", sampleTypesList);
    List<BasicDynaBean> hl7interfaceList = hl7interfaceService.lookup(true);
    beanMap.put("hl7Interfaces", hl7interfaceList);
    beanMap.put("hl7InterfacesMasterData", hl7interfaceService.getHl7Interfaces());
    List<BasicDynaBean> codeTypesJson = mrdCodeSupportService.getObservationCodeType();
    beanMap.put("codeTypesJSON", codeTypesJson);
    List<BasicDynaBean> impressionList = histoImpressionService.lookup(true);
    beanMap.put("impressions", impressionList);
    List<BasicDynaBean> antibioticsList = microAbstAntibioticService.lookup(false);
    beanMap.put("antibiotics", antibioticsList);
    List<BasicDynaBean> methodologies = diagMethodologyService.lookup(true);
    beanMap.put("methodologies", methodologies);
    beanMap.put("insuranceCategory", diagTestRepository.getInsuranceCategories());
    List<BasicDynaBean> genericPrefenceList = new ArrayList<>();
    genericPrefenceList.add(genericPreferencesService.getPreferences());
    beanMap.put("max_centers", genericPrefenceList);
    beanMap.put("hospRolesMasterData", hospitalRoleService.lookup(true));
    beanMap.put("itemGroupTypeList",
        new GenericRepository("item_group_type").listAll(null, "item_group_type_id", "TAX"));
    List<BasicDynaBean> itemGroupListJson = taxGroupService.getAllItemGroup();
    beanMap.put("itemGroupListJson", itemGroupListJson);
    List<BasicDynaBean> itemSubGroupList = taxSubGroupService
        .getItemSubGroupList(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if (itenSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itenSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    beanMap.put("itemSubGroupListJson", validateItemSubGrouList);
    beanMap.put("testEquipments", testEquipmentRepository.getTestEquipmentsByCenter());
    Map identifiers  = new HashMap();
    identifiers.put("res_sch_category", "DIA");
    identifiers.put("res_sch_type", "TST");
    BasicDynaBean schedulerMasterBean = schedulerResourceRepository.findByKey(identifiers);
    beanMap.put("default_duration", Arrays.asList(schedulerMasterBean));
    return beanMap;
  }

  /**
   * Insert test details.
   *
   * @param req
   *          the req
   * @param testId
   *          the test id
   * @param msg
   *          the msg
   * @return true, if successful
   */
  public boolean insertTestDetails(HttpServletRequest req, String testId, StringBuilder msg) {

    String[] interfaceNames = req.getParameterValues("interface_name");
    String[] itemTypes = req.getParameterValues("item_type");
    String[] hl7LabInterfaceIds = req.getParameterValues("hl7_lab_interface_id");
    String[] formatNames = req.getParameterValues("formatName");
    String serviceSubGroupId = req.getParameter("serviceSubGroupId");
    boolean isConfidential = false;
    if (req.getParameter("isconfidential") != null) {
      isConfidential = true;
    }
    boolean success = true;

    BasicDynaBean testBean = diagTestRepository.getBean();
    Map<String, String[]> parameters = req.getParameterMap();
    ConversionUtils.copyToDynaBean(parameters, testBean);

    testBean.set("service_sub_group_id", Integer.parseInt(serviceSubGroupId));
    String userName = (String) req.getSession(false).getAttribute("userid");
    testBean.set("username", userName);
    testBean.set("test_id", testId);
    String conductionFormat = req.getParameter("reportGroup");
    testBean.set("conduction_format", conductionFormat == null ? "" : conductionFormat);
    String specimenId = req.getParameter("specimen");
    if (specimenId != null && !specimenId.equals("")) {
      testBean.set("sample_type_id", Integer.parseInt(specimenId));
      testBean.set("type_of_specimen",
          diagTestRepository.getSpecimen(Integer.parseInt(specimenId)));
    }
    String[] conductingRoleIds = req.getParameterValues("conductingRoleId");
    testBean.set("conducting_role_id", CommonUtils.getCommaSeparatedString(conductingRoleIds));
    String sampleCollectionInstructions = req.getParameter("sampleCollectionInstructions");
    testBean.set("sample_collection_instructions", sampleCollectionInstructions);
    String conductionInstructions = req.getParameter("conductionInstructions");
    testBean.set("conduction_instructions", conductionInstructions);
    String resultsValidation = req.getParameter("resultsValidation");
    testBean.set("results_validation", resultsValidation);
    String additionalTestInfo = req.getParameter("test_additional_info");
    testBean.set("additional_info_reqts", additionalTestInfo);
    testBean.set("isconfidential", isConfidential);
    String allowZeroClaimAmount = req.getParameter("allow_zero_claim_amount");
    testBean.set("allow_zero_claim_amount", allowZeroClaimAmount);
    Integer billingGroupId = null;
    if (req.getParameter("billing_group_id") != null
        && !req.getParameter("billing_group_id").equals("")) {
      billingGroupId = Integer.parseInt(req.getParameter("billing_group_id"));
    }
    testBean.set("billing_group_id", billingGroupId);

    TransactionStatus insertTxStatus = DatabaseHelper.startTransaction("diagTestInsertTransaction");
    try {
      success = insertTest(testBean);

      if (success) {
        success = saveOrUpdateInsuranceCategory(testId, req);
      }

      if (success) {
        success = saveOrUpdateTestEquipments(testId, req);
      }

      if (success) {
        String testIdFromBean = (String) testBean.get("test_id");
        success = saveOrUpdateItemSubGroup(testIdFromBean, req);
      }

      if (success) {
        success = success && diagExportInterfaceService.insertDiagExprtInterfaces(testId,
            interfaceNames, itemTypes, hl7LabInterfaceIds);
        success = diagTestResultService.insertTestResults(testId, req, success, msg);
        BasicDynaBean diagBean = diagTestRepository.getBean();
        diagBean.set("results_validation", resultsValidation);
        Map keys = new HashMap();
        keys.put("test_id", testId);
        if ((diagTestRepository.update(diagBean, keys)) > 0) {
          if (conductionFormat != null && conductionFormat.equals("T")) {
            success &= diagTestRepository.insertTemplates(testId, formatNames);
          }
        } else {
          success &= false;
        }
        success &= diagTestRepository.initItemCharges(testId, userName);
        if (success && !"testuser".equals(userName)) {
          diagnosticChargeService.diagChargeScheduleJob(testId, userName);
        }
        success &= diagTatCenterService.addTestTatCenters(testId);
        if (success) {
          success &= diagTestRepository.updateDiagnosticTimeStamp();
        }
      } else {
        msg = msg.append("Failed to insert the Test Details");
      }
    } catch (Exception ex) {
      logger.debug("Error while inserting the test details" + ex.getMessage());
      DatabaseHelper.rollback(insertTxStatus);
      msg = msg.append("Failed to insert the Test Details");
      return false;
    }
    if (success) {
      DatabaseHelper.commit(insertTxStatus);
    }
    return success;
  }

  /**
   * Insert test.
   *
   * @param bean
   *          the bean
   * @return true, if successful
   */
  public boolean insertTest(BasicDynaBean bean) {
    int result = diagTestRepository.insert(bean);
    if (result == 0) {
      return false;
    }
    return true;
  }

  /**
   * Gets the test details.
   *
   * @param testId
   *          the test id
   * @return the test details
   */
  public List<BasicDynaBean> getTestDetails(String testId) {
    return diagTestRepository.getTestDetails(testId);
  }

  /**
   * Gets the list edit page data.
   *
   * @param testDetails
   *          the test details
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @return the list edit page data
   */
  @SuppressWarnings("unchecked")
  public Map getListEditPageData(List<BasicDynaBean> testDetails, String testId, String orgId) {

    Map detailsMap = new HashMap();
    BasicDynaBean bean = null;
    for (int i = 0; i < testDetails.size(); i++) {
      bean = testDetails.get(i);
      String testName = (String) (bean).get("test_name");
      testId = (String) (bean).get("test_id");
      Integer typeOfSpeciman = (Integer) (bean).get("sample_type_id");
      detailsMap.put("typeOfSpeciman", typeOfSpeciman);
      detailsMap.put("testName", testName);
      Integer testDuration = (Integer)bean.get("test_duration");
      detailsMap.put("testDuration", testDuration);
      List<BasicDynaBean> centers = DiagnosticTestRepository.getCentersList();
      detailsMap.put("centers_json", ConversionUtils.copyListDynaBeansToMap(centers));
      int serviceSubGroupId = (Integer) (bean).get("service_sub_group_id");
      Map params = new HashMap();
      params.put("service_sub_group_id", serviceSubGroupId);
      String groupId = serviceSubGroupService.findByPk(params).get("service_group_id").toString();
      detailsMap.put("groupId", groupId);
      detailsMap.put("serviceSubGroup", serviceSubGroupId);
      detailsMap.put("diagdepts", diagDepartmentService.getDiagDepartments());
      detailsMap.put("reportformats",
          ConversionUtils.listBeanToListMap(diagTestRepository.getReportFormats()));
      detailsMap.put("testList",
          ConversionUtils.copyListDynaBeansToMap(diagTestRepository.getTestNames()));

      detailsMap.put("testDetails", bean);
      detailsMap.put("conductingRoleIds", (String) bean.get("conducting_role_id") != null
          ? ((String) bean.get("conducting_role_id")).split(",") : "");
      detailsMap.put("loggedInCenter", RequestContext.getCenterId());
      detailsMap.put("testIdforCenter", testId);
      detailsMap.put("testResults",
          ConversionUtils.listBeanToListMap(diagTestResultService.getTestResults(testId)));
      detailsMap.put("testsRanges", ConversionUtils
          .copyListDynaBeansToMap(diagTestResultService.listAllTestResultReferences(testId)));
      detailsMap.put("testId", testId);
      detailsMap.put("orgId", orgId);
      detailsMap.put("serviceSubGroupsList",
          ConversionUtils.listBeanToListMap(serviceSubGroupService.listActiveRecord()));
      detailsMap.put("hl7Interfaces",
          ConversionUtils.listBeanToListMap(hl7interfaceService.lookup(true)));
      detailsMap.put("codeTypesJSON",
          ConversionUtils.copyListDynaBeansToMap(mrdCodeSupportService.getObservationCodeType()));
      detailsMap.put("impressions",
          ConversionUtils.listBeanToListMap(histoImpressionService.lookup(true)));
      detailsMap.put("antibiotics",
          ConversionUtils.listBeanToListMap(microAbstAntibioticService.lookup(false)));
      Integer genericPreference = (Integer) genericPreferencesService.getPreferences()
          .get("max_centers_inc_default");
      detailsMap.put("max_centers", genericPreference);
      detailsMap.put("test_timestamp", diagTestRepository.getCountFromDiagTimeStamp());
      String conductionInTemplate = (String) (bean).get("conduction_format");
      if (null != conductionInTemplate && conductionInTemplate.equals("T")) {
        List<BasicDynaBean> templateList = null;
        templateList = diagTestRepository.getTemplateList(testId);
        String[] templates = null;
        templates = populateListValuesToArray(templates, templateList);
        detailsMap.put("templates", templates);
      }
      List<BasicDynaBean> sampleTypesList = diagTestRepository.getActiveSampleTypeList();
      detailsMap.put("sampleTypes", sampleTypesList);
      detailsMap.put("results_json", ConversionUtils
          .copyListDynaBeansToMap(diagTestResultService.getResultsListForJson(testId)));
      detailsMap.put("hl7mappingRecords",
          ConversionUtils.listBeanToListMap(hl7interfaceService.getHl7MappingDetails(testId)));
      List<BasicDynaBean> activeInsuranceCategories = diagTestRepository
          .getActiveInsuranceCategories(testId);
      List<Integer> activeInsuranceCategory = new ArrayList<>();
      for (BasicDynaBean activeInsurance : activeInsuranceCategories) {
        activeInsuranceCategory.add((Integer) activeInsurance.get("insurance_category_id"));
      }
      detailsMap.put("activeInsuranceCategory", activeInsuranceCategory);
      List<BasicDynaBean> serviceGroups = serviceGroupService.getAllServiceGroups();
      detailsMap.put("serviceGroups", ConversionUtils.listBeanToListMap(serviceGroups));
      detailsMap.put("insuranceCategory",
          ConversionUtils.listBeanToListMap(diagTestRepository.getInsuranceCategories()));
      detailsMap.put("hl7InterfacesMasterData",
          ConversionUtils.listBeanToListMap(hl7interfaceService.lookup(true)));
      List<BasicDynaBean> methodologies = diagMethodologyService.lookup(true);
      detailsMap.put("methodologies", ConversionUtils.listBeanToListMap(methodologies));
      detailsMap.put("max_centers",
          (Integer) genericPreferencesService.getPreferences().get("max_centers_inc_default"));
      detailsMap.put("hospRolesMasterData",
          ConversionUtils.listBeanToListMap(hospitalRoleService.lookup(true)));

      List<BasicDynaBean> taxsubgroup = diagTestRepository.getTestItemSubGroupDetails(testId);
      detailsMap.put("taxsubgroup", ConversionUtils.listBeanToListMap(taxsubgroup));
      boolean isConfidential = (Boolean) (bean).get("isconfidential");
      detailsMap.put("isconfidential", isConfidential);
    }
    detailsMap.put("itemGroupTypeList", ConversionUtils.listBeanToListMap(
        new GenericRepository("item_group_type").listAll(null, "item_group_type_id", "TAX")));
    List<BasicDynaBean> itemGroupList = taxGroupService.getAllItemGroup();
    detailsMap.put("itemGroupListJson", ConversionUtils.listBeanToListMap(itemGroupList));
    List<BasicDynaBean> itemSubGroupList = taxSubGroupService
        .getItemSubGroupList(new java.sql.Date(new java.util.Date().getTime()));
    Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while (itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if (itenSubGroupbean.get("validity_end") != null) {
        Date endDate = (Date) itenSubGroupbean.get("validity_end");

        try {
          if (sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException ex) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    detailsMap.put("itemSubGroupListJson",
        ConversionUtils.listBeanToListMap(validateItemSubGrouList));

    detailsMap.put("testEquipments",
        ConversionUtils.listBeanToListMap(testEquipmentRepository.getTestEquipmentsByCenter()));
    List<BasicDynaBean> mappedEquipments = testEquipmentRepository.getTestEquipments(testId);
    List<Integer> selectedTestEquipments = new ArrayList<>();
    for (BasicDynaBean mappedEquipment : mappedEquipments) {
      selectedTestEquipments.add((Integer) mappedEquipment.get("eq_id"));
    }
    detailsMap.put("selectedTestEquipments", selectedTestEquipments);
    return detailsMap;
  }

  /**
   * Gets the next test id.
   *
   * @return the next test id
   */
  public String getNextTestId() {
    return (String) diagTestRepository.getNextId();
  }

  /**
   * Update test details.
   *
   * @param req
   *          the req
   * @param testId
   *          the test id
   * @param msg
   *          the msg
   * @return true, if successful
   */
  public boolean updateTestDetails(HttpServletRequest req, String testId, StringBuilder msg) {

    String[] interfaceNames = req.getParameterValues("interface_name");
    String[] itemTypes = req.getParameterValues("item_type");
    String[] formatNames = req.getParameterValues("formatName");
    String[] hl7mappdeleted = req.getParameterValues("hl7_mapping_deleted");
    String[] hl7LabInterfaceIds = req.getParameterValues("hl7_lab_interface_id");
    String serviceSubGroupId = req.getParameter("serviceSubGroupId");
    boolean isConfidential = false;
    if (req.getParameter("isconfidential") != null) {
      isConfidential = true;
    }
    boolean success = true;

    BasicDynaBean testBean = diagTestRepository.getBean();
    Map<String, String[]> parameters = req.getParameterMap();
    ConversionUtils.copyToDynaBean(parameters, testBean);

    testBean.set("service_sub_group_id", Integer.parseInt(serviceSubGroupId));
    String dependantTestId = req.getParameter("dependent_test_id");
    testBean.set("dependent_test_id", dependantTestId);
    String userName = (String) req.getSession(false).getAttribute("userid");
    testBean.set("username", userName);
    String conductionFormat = req.getParameter("reportGroup");
    testBean.set("conduction_format", conductionFormat);
    String specimenId = req.getParameter("specimen");
    if (specimenId != null && !specimenId.equals("")) {
      testBean.set("sample_type_id", Integer.parseInt(specimenId));
      testBean.set("type_of_specimen",
          diagTestRepository.getSpecimen(Integer.parseInt(specimenId)));
    }
    String[] conductingRoleIds = req.getParameterValues("conductingRoleId");
    testBean.set("conducting_role_id", CommonUtils.getCommaSeparatedString(conductingRoleIds));
    String sampleCollectionInstructions = req.getParameter("sampleCollectionInstructions");
    testBean.set("sample_collection_instructions", sampleCollectionInstructions);
    String conductionInstructions = req.getParameter("conductionInstructions");
    testBean.set("conduction_instructions", conductionInstructions);
    String resultsValidation = req.getParameter("resultsValidation");
    testBean.set("results_validation", resultsValidation);
    String additionalTestInfo = req.getParameter("test_additional_info");
    testBean.set("additional_info_reqts", additionalTestInfo);
    testBean.set("isconfidential", isConfidential);
    String allowZeroClaimAmount = req.getParameter("allow_zero_claim_amount");
    testBean.set("allow_zero_claim_amount", allowZeroClaimAmount);

    TransactionStatus txStatus = DatabaseHelper.startTransaction("diagTestTransaction");

    try {
      success = updateTest(testBean, testId);

      if (success) {
        success = saveOrUpdateInsuranceCategory(testId, req);
      }

      if (success) {
        success = saveOrUpdateTestEquipments(testId, req);
      }

      if (success) {
        success = saveOrUpdateItemSubGroup(testId, req);
      }

      if (success) {
        success = diagExportInterfaceService.updateDiagExprtInterfaces(testId, hl7LabInterfaceIds,
            interfaceNames, itemTypes, hl7mappdeleted);
        success = success && diagTestResultService.updateTestResults(testId, req, success, msg);
        Map keys = new HashMap();
        keys.put("test_id", testId);
        if (success && diagTestRepository.updateTemplates(testId, formatNames, conductionFormat)) {
          success = true;
        } else {
          success = false;
        }
        if (success) {
          diagTestRepository.updateDiagnosticTimeStamp();
        }
        if (success) {
          success = diagTestResultService.resultsCenterApplicabilityCheck(testId, msg);
        }
      } else {
        msg = msg.append("Test Details not saved");
      }
    } catch (Exception ex) {
      logger.debug("Error while updating the test details" + ex.getMessage());
      DatabaseHelper.rollback(txStatus);
      msg = msg.append("Failed to updated the Test Details");
      return false;
    }

    if (success) {
      DatabaseHelper.commit(txStatus);
    } else {
      DatabaseHelper.rollback(txStatus);
    }
    return success;
  }

  /**
   * Update test.
   *
   * @param testBean
   *          the test bean
   * @param testId
   *          the test id
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateTest(BasicDynaBean testBean, String testId) {
    int result = diagTestRepository.updateTestDetails(testBean, testId);
    if (result == 0) {
      return false;
    }
    return true;
  }

  /**
   * Populate list values TO array.
   *
   * @param arr
   *          the arr
   * @param al
   *          the al
   * @return the string[]
   */
  private static String[] populateListValuesToArray(String[] arr, List<BasicDynaBean> al) {
    Iterator<BasicDynaBean> it = al.iterator();
    arr = new String[al.size()];

    int index = 0;
    while (it.hasNext()) {
      arr[index++] = (String) it.next().get("format_name");
    }

    return arr;
  }

  /**
   * Gets the list edit charge data.
   *
   * @param chargeType
   *          the charge type
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @return the list edit charge data
   */
  public Map getListEditChargeData(String chargeType, String testId, String orgId) {

    Map chargesMap = new HashMap();
    List<BasicDynaBean> testDetails = diagTestRepository.getTestDetails(testId);
    chargesMap.put("testId", testId);
    chargesMap.put("orgId", orgId);
    chargesMap.put("testName", testDetails.iterator().next().get("test_name"));
    Map orgMap = new HashMap();
    orgMap.put("org_id", orgId);
    BasicDynaBean orgBean = organizationService.findByPk(orgMap);
    chargesMap.put("orgName", orgBean.get("org_name"));

    List<BasicDynaBean> list = testOrganizationService.getOrgItemCode(orgId, testId);
    String codeType = "";
    if (!list.isEmpty()) {
      BasicDynaBean bean = list.get(0);
      codeType = (String) bean.get("code_type");
      chargesMap.put("item_code", (String) bean.get("item_code"));
      chargesMap.put("applicable", (Boolean) bean.get("applicable"));
      chargesMap.put("code_type", codeType);
    }
    List<BasicDynaBean> activeRateSheets = organizationService.getActiveOrgIdNamesExcludeOrg(orgId);
    chargesMap.put("activeRateSheets", ConversionUtils.listBeanToListMap(activeRateSheets));

    List<BasicDynaBean> notApplicableRatePlans = testOrganizationService
        .getTestNotApplicableRatePlans(testId, orgId);
    chargesMap.put("ratePlansNotApplicable",
        ConversionUtils.listBeanToListMap(notApplicableRatePlans));
    List<BasicDynaBean> derivedRatePlanDetails = diagTestRepository.getDerivedRatePlanDetails(orgId,
        testId);
    if (!(derivedRatePlanDetails.size() > 0)) {
      chargesMap.put("derivedRatePlanDetails", Collections.EMPTY_LIST);
    } else {
      chargesMap.put("derivedRatePlanDetails", derivedRatePlanDetails);
    }
    List testsList = diagTestRepository.getTestsNamesAndIds();

    chargesMap.put("testsList", testsList);
    chargesMap.put("testid", testId);
    chargesMap.put("codeType", codeType);

    List<BasicDynaBean> bedTypesList = bedTypeService.getAllBedTypes();
    Map editChargesMap = diagTestRepository.editTestCharges(bedTypesList, orgId, testId);
    chargesMap.put("chargeMap", editChargesMap);
    List<BasicDynaBean> rateSheetsList = organizationService.getRateSheetForCharge();
    chargesMap.put("rateSheets", ConversionUtils.listBeanToListMap(rateSheetsList));
    List<BasicDynaBean> treatmentCodes = mrdCodeSupportService.getTreatmentCodeType();
    chargesMap.put("treatmentCodes", ConversionUtils.listBeanToListMap(treatmentCodes));
    Map<String, Object> searchMap = new HashMap<String, Object>();
    searchMap.put("entity", "DIAGNOSTIC");
    searchMap.put("entity_id", testId);
    ArrayList<String> status = new ArrayList<String>();
    status.add("F");
    status.add("P");
    searchMap.put("status", status);
    List<BasicDynaBean> masterJobData =
        masterChargesCronSchedulerDetailsRepository.findByCriteria(searchMap);
    chargesMap.put("masterJobCount", masterJobData.size());
    return chargesMap;
  }

  /**
   * Gets the master data.
   *
   * @return the master data
   */
  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.bulk.BulkDataService#getMasterData()
   */
  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    return null;
  }

  /**
   * Update test charges.
   *
   * @param tclist
   *          the tclist
   * @param testId
   *          the test id
   * @param orgId
   *          the org id
   * @param disabled
   *          the disabled
   * @param orgItemCode
   *          the org item code
   * @param codeType
   *          the code type
   * @param derivedRateplanIds
   *          the derived rateplan ids
   * @param ratePlanApplicable
   *          the rate plan applicable
   * @param bedType
   *          the bed type
   * @param regularcharge
   *          the regularcharge
   * @param discount
   *          the discount
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateTestCharges(ArrayList<TestCharge> tclist, String testId, String orgId,
      boolean disabled, String orgItemCode, String codeType, String[] derivedRateplanIds,
      String[] ratePlanApplicable, String[] bedType, Double[] regularcharge, Double[] discount) {

    boolean status = true;
    status = testOrganizationService.updateTestOrganizationDetails(testId, orgId);
    if (!status) {
      return false;
    }

    status &= diagTestRepository.addOrEditTestCharges(tclist);

    TestCharge itemCode = new TestCharge();
    itemCode.setApplicable(disabled);
    itemCode.setOrgItemCode(orgItemCode);
    itemCode.setOrgId(orgId);
    itemCode.setTestId(testId);
    itemCode.setCodeType(codeType);
    ArrayList<TestCharge> codeList = new ArrayList<>();
    codeList.add(itemCode);
    status = diagTestRepository.addOrEditItemCode(codeList);
    if (status) {
      diagTestRepository.updateDiagnosticTimeStamp();
    }
    if (null != derivedRateplanIds && derivedRateplanIds.length > 0) {
      status = status && testOrganizationService.updateOrgForDerivedRatePlans(derivedRateplanIds,
          ratePlanApplicable, testId);
      status = status && diagnosticChargeService.updateChargesForDerivedRatePlans(orgId,
          derivedRateplanIds, bedType, regularcharge, testId, discount, ratePlanApplicable);
    }
    List<BasicDynaBean> allDerivedRatePlanIds = diagTestRepository.getDerivedRatePlanIds(orgId);
    if (null != allDerivedRatePlanIds) {
      testOrganizationService.updateApplicableflagForDerivedRatePlans(allDerivedRatePlanIds,
          "diagnostics", "test_id", testId, "test_org_details", orgId);
    }

    return status;
  }

  /**
   * Gets the report formats.
   *
   * @return the report formats
   */
  public List<BasicDynaBean> getReportFormats() {
    return diagTestRepository.getReportFormats();
  }

  /**
   * Gets the test names and ids.
   *
   * @return the test names and ids
   */
  public Map<String, String> getTestNamesAndIds() {
    List<BasicDynaBean> list = diagTestRepository.getAllTestNames();
    Map<String, String> testMap = new HashMap<>();
    for (BasicDynaBean bean : list) {
      testMap.put((String) bean.get("test_name"), (String) bean.get("test_id"));
    }
    return testMap;
  }

  /**
   * Gets the report formats map.
   *
   * @return the report formats map
   */
  public Map<String, String> getReportFormatsMap() {
    Map<String, String> formatMap = new HashMap<>();
    List<BasicDynaBean> list = diagTestRepository.getReportFormats();
    for (BasicDynaBean bean : list) {
      formatMap.put(((String) bean.get("format_name")).trim(), (String) bean.get("testformat_id"));
    }
    return formatMap;
  }

  /**
   * Parses the and import cs V.
   *
   * @param file
   *          the file
   * @param feedback
   *          the feedback
   * @return the string
   */
  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.bulk.BulkDataService#parseAndImportCSV
   * (org.springframework.web.multipart.MultipartFile, java.util.Map)
   */
  @Override
  public String parseAndImportCsV(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {

    boolean hasErrors = false;
    Map<String, String> deptsMap = diagDepartmentService.getDiagDepartmentsMap();
    List<BasicDynaBean> orgList = organizationService.lookup(false);
    List<BasicDynaBean> bedTypes = bedTypeService.getAllBedTypes();
    List<BasicDynaBean> inactiveBedList = bedTypeService.getInactiveBeds();
    Map<String, Integer> insCategoryMap = insuranceItemCategoryService.getnsuranceCategoryMap();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String userNameWithHint = userName + ":CSV";

    List<BasicDynaBean> rows = new ArrayList<BasicDynaBean>();
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<Object, Object>();
    HashMap<String, String> headersMap = new HashMap<String, String>();
    headersMap.put("test_id", "test_id");
    headersMap.put("Test Name", "test_name");
    headersMap.put("Sample Needed", "sample_needed");
    headersMap.put("Dept Name", "ddept_name");
    headersMap.put("Type Of Specimen", "type_of_specimen");
    headersMap.put("Conduct In Report Format", "conduction_format");
    headersMap.put("Status", "status");
    headersMap.put("Service Group Name", "service_group_name");
    headersMap.put("Service Sub Group Name", "service_sub_group_name");
    headersMap.put("Conduction Applicable", "conduction_applicable");
    headersMap.put("Conducting Doctor Mandatory", "conducting_doc_mandatory");
    headersMap.put("mandate additional info", "mandate_additional_info");
    headersMap.put("Unit Charge", "charge");
    headersMap.put("Results Entry Applicable", "results_entry_applicable");
    headersMap.put("Alias", "diag_code");
    headersMap.put("Insurance Category", "insurance_category_name");
    headersMap.put("Pre Auth Required", "prior_auth_required");
    headersMap.put("Allow Rate Increase", "allow_rate_increase");
    headersMap.put("Allow Rate Decrease", "allow_rate_decrease");
    headersMap.put("Interface Test Code", "hl7_export_code");
    headersMap.put("Do not auto-share results", "isconfidential");
    headersMap.put("test_duration", "test_duration");
    headersMap.put("Prescribable", "is_prescribable");

    List<String> mandatoryList = Arrays.asList("test_name", "status", "ddept_name",
        "service_sub_group_name", "sample_needed", "service_group_name",
        "mandate_additional_info", "test_duration");

    try {
      InputStreamReader streamReader = new InputStreamReader(file.getInputStream());
      CSVReader csvReader = new CSVReader(streamReader);
      String[] headers = csvReader.readNext();

      if (headers.length < 1) {
        return "exception.csv.missing.headers";
      }
      if (!headers[0].matches("\\p{Print}*")) {
        return "exception.csv.non.printable.characters";
      }
      if (headers.length == 1) {
        return "exception.csv.non.comma.seperators";
      }
      boolean[] ignoreColumn = new boolean[headers.length];
      Integer lineNumber = 0;
      Integer lineWarningsCount = 0;
      Integer insertionCount = 0;
      Integer updationCount = 0;
      String operation = "update";
      CsVBulkDataEntity csvEntity = getCsVDataEntity();
      Map<String, Class<?>> typeMap = csvEntity.getTypeMap();

      for (Integer index = 0; index < headers.length; index++) {
        String fieldName = headersMap.get(headers[index].trim());
        if (fieldName == null) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.header", headers[index]);
          ignoreColumn[index] = true;
        } else {
          ignoreColumn[index] = false;
        }

        headers[index] = fieldName;
      }
      lineNumber++;
      for (String mfield : mandatoryList) {
        if (!Arrays.asList(headers).contains(mfield)) {
          addWarning(warnings, lineNumber,
              "Mandatory field " + mfield + " is missing cannot process further in the sheet ",
              mfield);
          hasErrors = true;
        }
      }
      if (hasErrors) {
        feedback.put("result", meta);
        feedback.put("warnings", warnings);
        return null;
      }

      String[] row = null;
      while (null != (row = csvReader.readNext())) {
        Integer nonEmptyColumnsCount = 0;
        boolean hasWarnings = false;
        BasicDynaBean bean = getRepository().getBean();
        lineNumber++;
        String newId = null;
        String itemId = null;
        String itemName = null;
        Object itemDept = null;
        String beanId = null;
        String grpId = null;
        String subGrpName = null;
        List<BasicDynaBean> existOrNot = null;
        String subGrpBean = null;
        BigDecimal unitCharge = null;

        for (Integer columnIndex = 0; columnIndex < headers.length
            && columnIndex < row.length; columnIndex++) {
          if (ignoreColumn[columnIndex]) {
            continue;
          }

          String fieldName = headers[columnIndex];
          String fieldValue = row[columnIndex].trim();
          DynaProperty property;

          if (((null != fieldValue) && !(fieldValue.isEmpty())) || fieldName.equals("test_id")) {
            if (fieldName.equals("test_id")) {
              itemId = fieldValue;
              if (fieldValue.trim() == null || fieldValue.trim().equals("")) {
                operation = "insert";
              }
            }
            if (fieldName.equals("test_name")) {
              itemName = fieldValue;
              if (null == fieldValue) {
                addWarning(warnings, lineNumber, "can not be null", fieldValue, fieldName);
                hasWarnings = true;
              }
              bean.set(fieldName, fieldValue);
            }
            if (fieldName.equals("ddept_name")) {
              if (null == fieldValue) {
                addWarning(warnings, lineNumber, "Can not be null", fieldValue, fieldName);
                hasWarnings = true;
              }
              String masterValue = deptsMap.get(fieldValue);
              itemDept = masterValue;
              if (null == masterValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
              fieldName = "ddept_id";
              fieldValue = masterValue;
              bean.set(fieldName, fieldValue);
            }
            if (fieldName.equals("status")) {
              if (null == fieldValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
            }
            if (fieldName.equals("charge")) {
              if (null != fieldValue && !fieldValue.equals("")) {
                unitCharge = new BigDecimal(fieldValue);
              }
            }
            if (fieldName.equals("service_group_name")) {
              if (null == fieldValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
              grpId = String.valueOf(serviceGroupService
                  .findByUniqueName(fieldValue, "service_group_name").get("service_group_id"));
              if (null == grpId) {
                addWarning(warnings, lineNumber, "Service Group Id not exist in the sheet",
                    fieldValue, fieldName);
                hasWarnings = true;
              }
            }
            if (fieldName.equals("service_sub_group_name")) {
              if (null == fieldValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
              subGrpName = fieldValue;
            }
            if (fieldName.equals("mandate_additional_info")) {
              if (null == fieldValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
            }
            if (fieldName.equals("insurance_category_name")) {
              if (null != fieldValue && !fieldValue.equals("")) {
                Integer insCatId = insCategoryMap.get(fieldValue);
                if (insCatId == null || insCatId.equals("")) {
                  addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                      fieldName);
                  hasWarnings = true;
                }
                fieldName = "insurance_category_id";
                fieldValue = String.valueOf(insCatId);
              }
            }
            if (fieldName.equals("sample_needed") || fieldName.equals("type_of_specimen")) {
              if (fieldName.equals("sample_needed")) {
                bean.set("sample_needed", fieldValue);
              } else {
                if (fieldValue == null) {
                  continue;
                } else {
                  bean.set("type_of_specimen", fieldValue);
                  Map<String, Object> identifier = new HashMap<>();
                  identifier.put("sample_type", fieldValue);
                  String specimenId = diagTestRepository.getSpecimenId(fieldValue);
                  if (specimenId != null && !specimenId.equals("")) {
                    bean.set("sample_type_id", Integer.parseInt(specimenId));
                  }
                }
              }
            }
            if (!fieldName.endsWith("ddept_name") && !fieldName.equals("service_group_name")
                && !fieldName.equals("service_sub_group_name") && !fieldName.equals("charge")) {
              property = bean.getDynaClass().getDynaProperty(fieldName);
              Class<?> enforcedType = typeMap.get(fieldName);
              if (null != enforcedType) {
                if (null == ConvertUtils.convert(fieldValue, enforcedType)) {
                  addWarning(warnings, lineNumber, "exception.csv.conversion.error", fieldValue,
                      (enforcedType == BigDecimal.class ? " Number "
                          : enforcedType.getSimpleName()),
                      fieldName);
                  hasWarnings = true;
                  continue;
                }
              }
              bean.set(fieldName, ConvertUtils.convert(fieldValue, property.getType()));
            }
            nonEmptyColumnsCount++;
          } else {
            if (mandatoryList.contains(fieldName)) {
              addWarning(warnings, lineNumber, fieldName + " can not be null in the sheet",
                  fieldValue, fieldName);
              hasWarnings = true;
            }
          }
        }
        if (hasWarnings || nonEmptyColumnsCount == 0) {
          continue;
        }

        if (itemName != null && itemDept != null) {
          Map filterMap = new HashMap();
          filterMap.put("status", "A");
          filterMap.put("test_name", itemName);
          filterMap.put("ddept_id", itemDept);
          existOrNot = diagTestRepository.getTestIdAndName(itemName, (String) itemDept);
        }
        if (existOrNot != null && existOrNot.size() > 0) {
          beanId = (String) existOrNot.get(0).get("test_id");
        }
        if (operation.equals("update") && existOrNot != null && existOrNot.size() > 0) {
          if (!itemId.equals(beanId)) {
            addWarning(warnings, lineNumber, "exception.duplicate.entity", "Test Name", itemName);
            hasWarnings = true;
          }
        } else {
          if (beanId != null) {
            addWarning(warnings, lineNumber, "exception.csv.duplicate.record");
            hasWarnings = true;
          }
        }
        if (grpId != null && subGrpName != null) {

          Map filterMap = new HashMap();
          filterMap.put("service_group_id", Integer.parseInt(grpId));
          filterMap.put("service_sub_group_name", subGrpName.trim());
          List<BasicDynaBean> serviceGrpList = serviceSubGroupService
              .getServiceGroupIdWithName(filterMap);
          if (serviceGrpList != null && serviceGrpList.size() > 0) {
            subGrpBean = String.valueOf(serviceGrpList.get(0).get("service_sub_group_id"));
          }
          if (subGrpBean == null) {
            addWarning(warnings, lineNumber, "Group and Subgroup is not matching in the sheet",
                itemName, null);
            hasWarnings = true;
          } else {
            bean.set("service_sub_group_id", Integer.parseInt(subGrpBean));
          }
        }
        if (hasWarnings || nonEmptyColumnsCount == 0) {
          continue;
        }

        try {

          /* get the diag code */
          boolean success = true;
          String orderAlias = getOrderAlias("Diag", itemDept.toString(), grpId.toString(),
              subGrpBean);

          if (operation.equals("update")) {
            bean.set("test_id", itemId);
            bean.set("username", userNameWithHint);
            success = super.update(bean) > 0;
            if (success) {
              updationCount++;
            }
          } else {
            newId = this.getNextTestId();
            bean.set("test_id", newId);
            bean.set("diag_code", orderAlias);
            bean.set("username", userName);
            success = diagTestRepository.insert(bean) > 0;
            if (success) {
              insertionCount++;
            }

            /* insert org details */
            for (BasicDynaBean org : orgList) {
              success &= testOrganizationService.insertDetails(newId, (String) org.get("org_id"),
                  true) > 0;
            }
            if (unitCharge == null || unitCharge.equals("")) {
              unitCharge = BigDecimal.ZERO;
            }
            for (BasicDynaBean org : orgList) {
              for (BasicDynaBean bedName : bedTypes) {
                String bed = (String) bedName.get("bed_type");
                String priority = "R";
                success &= diagnosticChargeService.insertTestCharges(newId,
                    (String) org.get("org_id"), bed, priority, unitCharge, userName) > 0;
              }
            }

            if (!inactiveBedList.isEmpty()) {
              for (BasicDynaBean org : orgList) {
                for (BasicDynaBean bedName : inactiveBedList) {
                  String bed = (String) bedName.get("intensive_bed_type");
                  String priority = "R";
                  success &= diagnosticChargeService.insertTestCharges(newId,
                      (String) org.get("org_id"), bed, priority, unitCharge, userName) > 0;
                }
              }
            }
          }
        } catch (DuplicateEntityException ex) {
          addWarning(warnings, lineNumber, "exception.csv.duplicate.record");
          logger.error("Duplicate record found : " + bean.get("test_name"));
          lineWarningsCount++;
        } catch (DataAccessException ex) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.error",
              ex.getMostSpecificCause().getMessage());
          logger.error("Error uploading csv line", ex.getCause());
          lineWarningsCount++;
        } catch (ValidationException ex) {
          for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) (Object) ex
              .getErrors()).entrySet()) {
            warnings.add(lineNumber, entry.getValue().get(0));
          }

          logger.error(ex.getMessage());
        }
      }
      feedback.put("warnings", warnings);

      meta.add("processed_count", lineNumber - 1);
      meta.add("insertion_count", insertionCount);
      meta.add("updation_count", updationCount);
      feedback.put("result", meta);

    } catch (IOException ex) {
      throw new InvalidFileFormatException(ex);
    }

    return null;
  }

  /**
   * Adds the warning.
   *
   * @param warnings
   *          the warnings
   * @param lineNumber
   *          the line number
   * @param message
   *          the message
   * @param parameters
   *          the parameters
   */
  @Override
  protected void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message, Object... parameters) {
    StringBuilder warning = new StringBuilder();
    warning.append(messageUtil.getMessage(message, parameters));
    warnings.add(lineNumber, warning.toString());
  }

  /**
   * Gets the order alias.
   *
   * @param type
   *          the type
   * @param deptId
   *          the dept id
   * @param groupId
   *          the group id
   * @param subGrpId
   *          the sub grp id
   * @return the order alias
   */
  public String getOrderAlias(String type, String deptId, String groupId, String subGrpId) {
    Map params = new HashMap();
    params.put("service_group_id", new Integer(groupId));
    BasicDynaBean serviceGroup = serviceGroupService.findByPk(params);
    params = new HashMap();
    params.put("service_sub_group_id", new Integer(subGrpId));
    BasicDynaBean serviceSubGroup = serviceSubGroupService.findByPk(params);
    String groupCode = (String) serviceGroup.get("service_group_code") == null ? ""
        : (String) serviceGroup.get("service_group_code");
    String subGrpCode = (String) serviceSubGroup.get("service_sub_group_code") == null ? ""
        : (String) serviceSubGroup.get("service_sub_group_code");
    BasicDynaBean masterCounts = diagTestRepository.getMastersCounts(type, deptId);
    String count = (masterCounts == null) ? "" : masterCounts.get("count").toString();

    return groupCode + subGrpCode + count;
  }

  /**
   * Update test details for charge.
   *
   * @param testId
   *          the test id
   * @param deptId
   *          the dept id
   * @param status
   *          the status
   * @return true, if successful
   */
  public boolean updateTestDetailsForCharge(String testId, String deptId, String status,
      String userId) {
    return diagTestRepository.updateTestDetails(testId, deptId, status, userId) > 0;
  }

  /**
   * Update diagnostic time stamp.
   *
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateDiagnosticTimeStamp() {
    return diagTestRepository.updateDiagnosticTimeStamp();
  }

  /** The Constant SEARCH_CODES_OF_TYPE_QUERY. */
  private static final String SEARCH_CODES_OF_TYPE_QUERY = "SELECT code, "
      + " code ||' '||COALESCE(code_desc,'') AS icd,"
      + " COALESCE(code_desc,'') AS code_desc,code_type" + " FROM getItemCodesForCodeType(?, ?)"
      + " WHERE status = 'A' AND code_type = ? ";

  /**
   * Gets the codes list of code type.
   *
   * @param searchInput
   *          the search input
   * @param codeType
   *          the code type
   * @param patientType
   *          the patient type
   * @param dialogType
   *          the dialog type
   * @return the codes list of code type
   */
  public List getCodesListOfCodeType(String searchInput, String codeType, String patientType,
      String dialogType) {

    String[] searchWord = null;
    if (searchInput == null || searchInput.equals("")) {
      searchWord = new String[] { "" };
    } else {
      searchWord = searchInput.split(" ");
    }
    StringBuilder query = new StringBuilder(SEARCH_CODES_OF_TYPE_QUERY);
    for (int k = 0; k < searchWord.length; k++) {
      query.append(" AND (code ILIKE '" + searchWord[k] + "%' OR code_desc ILIKE '" + searchWord[k]
          + "%' OR code_desc ILIKE '%" + searchWord[k] + "' OR code_desc ILIKE '%" + searchWord[k]
          + "%')");
    }
    query.append(" LIMIT 100");
    return DatabaseHelper.queryToDynaList(query.toString(), codeType, patientType, codeType);
  }

  /**
   * Save or update item sub group.
   *
   * @param testId
   *          the test id
   * @param request
   *          the request
   * @return true, if successful
   */
  private boolean saveOrUpdateItemSubGroup(String testId, HttpServletRequest request) {
    Map params = request.getParameterMap();
    List errors = new ArrayList();

    int result = 1;
    String[] itemSubgroupId = request.getParameterValues("item_subgroup_id");
    String[] delete = request.getParameterValues("deleted");

    if (errors.isEmpty()) {
      if (itemSubgroupId != null && itemSubgroupId.length > 0 && !itemSubgroupId[0].equals("")) {
        GenericRepository itemsubgroupdao = new GenericRepository("diagnostics_item_sub_groups");
        BasicDynaBean itemsubgroupbean = itemsubgroupdao.getBean();
        ConversionUtils.copyToDynaBean(params, itemsubgroupbean, errors);
        BasicDynaBean records = itemsubgroupdao.findByKey("test_id", testId);
        if (records != null) {
          result = itemsubgroupdao.delete("test_id", testId);
        }
        for (int i = 0; i < itemSubgroupId.length; i++) {
          if (itemSubgroupId[i] != null && !itemSubgroupId[i].isEmpty()) {
            if (delete[i].equalsIgnoreCase("false")) {
              itemsubgroupbean.set("test_id", testId);
              itemsubgroupbean.set("item_subgroup_id", Integer.parseInt(itemSubgroupId[i]));
              result = itemsubgroupdao.insert(itemsubgroupbean);
            }
          }
        }
        if (result == 0) {
          return false;
        } else {
          return true;
        }
      }
    }
    return true;
  }

  /**
   * Save or update Insurance Category.
   *
   * @param test_id
   *          the test id
   * @param request
   *          the request
   * @return true, if successful
   */
  private boolean saveOrUpdateInsuranceCategory(String testId, HttpServletRequest request) {
    int result = 1;
    String[] insuranceCategories = request.getParameterValues("insurance_category_id");
    if (insuranceCategories != null && insuranceCategories.length > 0
        && !insuranceCategories[0].equals("")) {
      BasicDynaBean insuranceCategoryBean = diagnosticsTestInsuranceCategoryRepository.getBean();
      Map<String, Object> criteriaParams = new HashMap<>();
      criteriaParams.put("diagnostic_test_id", testId);
      List<BasicDynaBean> records = diagnosticsTestInsuranceCategoryRepository
          .findByCriteria(criteriaParams);
      if (records != null && records.size() > 0) {
        result = diagnosticsTestInsuranceCategoryRepository.delete("diagnostic_test_id", testId);
      }
      for (String insuranceCategory : insuranceCategories) {
        insuranceCategoryBean.set("diagnostic_test_id", testId);
        insuranceCategoryBean.set("insurance_category_id", Integer.parseInt(insuranceCategory));
        result = diagnosticsTestInsuranceCategoryRepository.insert(insuranceCategoryBean);
      }
      if (result == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * save or update mapped equipments to test.
   *
   * @param testId
   *          test id
   * @param request
   *          request paramter
   * @return boolean value
   * @throws SQLException
   *           throws sql exception
   * @throws IOException
   *           throws io exception
   */
  public boolean saveOrUpdateTestEquipments(String testId, HttpServletRequest request)
      throws SQLException, IOException {
    boolean result = true;
    String[] equipments = request.getParameterValues("eq_id");
    GenericDAO testEquipmentDiagnosticsMapping = new GenericDAO(
        "diagnostics_test_equipment_master_mapping");
    BasicDynaBean testEquipmentDiagnosticsMappingBean = testEquipmentDiagnosticsMapping.getBean();
    List<BasicDynaBean> existingMappingRecords = testEquipmentDiagnosticsMapping
        .findAllByKey("test_id", testId);
    Connection con = DataBaseUtil.getConnection();
    try {
      if (existingMappingRecords != null && existingMappingRecords.size() > 0) {
        result = testEquipmentDiagnosticsMapping.delete(con, "test_id", testId);
      }
      if (equipments != null && equipments.length > 0) {
        for (String equipmentId : equipments) {
          testEquipmentDiagnosticsMappingBean.set("test_id", testId);
          testEquipmentDiagnosticsMappingBean.set("eq_id", Integer.valueOf(equipmentId));
          result = testEquipmentDiagnosticsMapping.insert(con, testEquipmentDiagnosticsMappingBean);
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

    return result;
  }

  /**
   * Gets the diagnostics item sub group tax details.
   *
   * @param actDescriptionId
   *          the act description id
   * @return the diagnostics item sub group tax details
   */
  public List<BasicDynaBean> getDiagnosticsItemSubGroupTaxDetails(String actDescriptionId) {
    return diagTestRepository.getDiagnosticsItemSubGroupTaxDetails(actDescriptionId);
  }

  /**
   * Gets the cat id based on plan ids.
   *
   * @param listItemIds
   *          the list item ids
   * @param planIds
   *          the plan ids
   * @param visitType
   *          the visit type
   * @return the cat id based on plan ids
   */
  public List<BasicDynaBean> getCatIdBasedOnPlanIds(List<String> listItemIds, Set<Integer> planIds,
      String visitType) {

    return diagTestRepository.getCatIdBasedOnPlanIds(listItemIds, planIds, visitType);
  }
  
  public BasicDynaBean findByKey(String testId) {
    return diagTestRepository.findByKey("test_id", testId);
  }

}
