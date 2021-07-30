package com.insta.hms.Registration;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.OTServices.OperationDetailsBO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.batchjob.pushevent.EventListenerJob;
import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.RandomGeneration;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.core.clinical.ipemr.IpEmrFormService;
import com.insta.hms.diagnosticmodule.prescribetest.DiagnoDAOImpl;
import com.insta.hms.documentpersitence.MLCDocumentAbstractImpl;
import com.insta.hms.documentpersitence.RegDocumentAbstractImpl;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.integration.hl7.message.v23.ADTService;
import com.insta.hms.integration.insurance.VisitClassificationType;
import com.insta.hms.integration.insurance.eligbilityauthorization.EligibilityAuthorizationService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.AreaMaster.AreaMasterDAO;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DepartmentMaster.DepartmentMasterDAO;
import com.insta.hms.master.DepartmentUnitMaster.DepartmentUnitMasterDAO;
import com.insta.hms.master.DiscountPlanMaster.DiscountPlanMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericDocumentTemplate.GenericDocumentTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GovtIdentifierMaster.GovtIdentifierMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import com.insta.hms.master.OtherIdentifierMaster.OtherIdentificationDocumentTypesDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.SalutationMaster.SalutationMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.mdm.confidentialitygrpmaster.ConfidentialityGroupService;
import com.insta.hms.mdm.formcomponents.FormComponentsRepository;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.insuranceplantypes.InsurancePlanTypeService;
import com.insta.hms.mdm.tpas.TpaService;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.orders.OrderAction;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.PackageOrderDTO;
import com.insta.hms.orders.TestDocumentDTO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.PatientPrescriptionDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.resourcescheduler.PractoBookHelper;
import com.insta.hms.resourcescheduler.ResourceBO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.resourcescheduler.ResourceDTO;
import com.insta.hms.wardactivities.defineipcareteam.IPCareDAO;
import flexjson.JSONSerializer;
import java.util.Collections;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

public class RegistrationAction extends BaseAction {
  
  private static Logger log = LoggerFactory.getLogger(RegistrationAction.class);
  private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();
  private static JSONSerializer js = new JSONSerializer().exclude("class");
  private static InsuCompMasterDAO insuCompMasterDao = new InsuCompMasterDAO();
  private static PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
  private static InsuranceCompanyTPAMasterDAO insuranceCompanyTpaMasterDao = new InsuranceCompanyTPAMasterDAO();
  private static ConfidentialityGroupService confidentialityService = ApplicationContextProvider
      .getBean(ConfidentialityGroupService.class);
  private static TpaService tpaService = ApplicationContextProvider.getBean(TpaService.class);
  private static InsurancePlanService insurancePlanService = ApplicationContextProvider
      .getBean(InsurancePlanService.class);
  private static InsuranceCompanyService insuranceCompanyService = ApplicationContextProvider
      .getBean(InsuranceCompanyService.class);
  private static InsurancePlanTypeService insurancePlanTypeService = ApplicationContextProvider
      .getBean(InsurancePlanTypeService.class);
  private static EligibilityAuthorizationService eligibilityAuthService = ApplicationContextProvider
      .getBean(EligibilityAuthorizationService.class);
  private static SimpleDateFormat sqlDateFormatter = new DateUtil().getSqlDateFormatter();
  private static PatientInsurancePlanDAO planDAO = new PatientInsurancePlanDAO();
  private static GenericDAO patPlanDetailsDAO = new GenericDAO("patient_insurance_plan_details");
  private static GenericDAO tpaMasterDao = new GenericDAO("tpa_master");
  private static GenericDAO sponsorTypeDao = new GenericDAO("sponsor_type");
  private static PatientDetailsDAO patientDetailsDao = new PatientDetailsDAO();
  private static VisitDetailsDAO rdao = new VisitDetailsDAO();
  private static RegistrationBO regBO = new RegistrationBO();
  private static MRDDiagnosisDAO mrdDiagDao = new MRDDiagnosisDAO();
  private static SecondaryComplaintDAO secComplaintDAO = new SecondaryComplaintDAO();
  private static GenericDAO schedulerAppointmentsDao = new GenericDAO("scheduler_appointments");
  private static RegDocumentAbstractImpl regDocumentImpl = new RegDocumentAbstractImpl();
  private static PlanMasterDAO planMasterDao = new PlanMasterDAO();
  private static IPCareDAO ipcareDAO = new IPCareDAO();
  private static GenericDAO ipBedDetailsDao = new GenericDAO("ip_bed_details");
  private static GenericDAO admissionDao = new GenericDAO("admission");
  private static GenericDAO salutationMasterDao = new GenericDAO("salutation_master");
  private static DoctorConsultationDAO doctorConsultationDao = new DoctorConsultationDAO();
  private static MRDUpdateScreenDAO mrdUpdateScreenDao = new MRDUpdateScreenDAO();
  private static CenterPreferencesDAO centerPreferencesDao = new CenterPreferencesDAO();
  private static DepartmentMasterDAO departmentMasterDao = new DepartmentMasterDAO();
  private static GenericDAO centerMasterDao = new GenericDAO("hospital_center_master");
  private static GenericDAO wardNamesDao = new GenericDAO("ward_names");
  private static GenericDAO ipPreferencesDao = new GenericDAO("ip_preferences");
  private static IPPreferencesDAO ipPerfsDao = new IPPreferencesDAO();
  private static GenericDAO masterTimestampDao = new GenericDAO("master_timestamp");
  private static PatientCategoryDAO patientCategoryDao = new PatientCategoryDAO();
  private static GenericDAO areaMasterGenDao = new GenericDAO("area_master");
  private static GenericDAO cityMaster = new GenericDAO("city");
  private static GenericDAO countryMaster = new GenericDAO("country_master");
  private static GenericDAO stateMaster = new GenericDAO("state_master");
  private static GenericDAO referralDao = new GenericDAO("referral");
  private static GenericDAO doctorsDao = new GenericDAO("doctors");
  private static GovtIdentifierMasterDAO govtIdMasterDao = new GovtIdentifierMasterDAO();
  private static OtherIdentificationDocumentTypesDAO otherIdDocTypesDAO = new OtherIdentificationDocumentTypesDAO();
  private static BedMasterDAO bedMasterDao = new BedMasterDAO();
  private static AreaMasterDAO areaMasterDao = new AreaMasterDAO();
  private static MessageManager messageManager = new MessageManager();
  private static DiagnoDAOImpl diagDao = new DiagnoDAOImpl();
  private static OrderBO orderBo = new OrderBO();
  private static MLCDocumentAbstractImpl mlcDocImpl = new MLCDocumentAbstractImpl();
  private static OperationDetailsBO operationDetailsBo = new OperationDetailsBO();
  private static AdmissionRequestBO admissionRequestBo = new AdmissionRequestBO();
  private static SponsorBO sponsorBo = new SponsorBO();
  private static PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
  JobService jobService = JobSchedulingService.getJobService();
  private static PatientPrescriptionDAO prescDAO = new PatientPrescriptionDAO();
  private static IpEmrFormService ipEmrFormService = ApplicationContextProvider.getBean(IpEmrFormService.class);
  private static FormComponentsRepository formComponentsRepository = ApplicationContextProvider.getBean(FormComponentsRepository.class);
  private static InterfaceEventMappingService interfaceEventMappingService = ApplicationContextProvider.getBean(InterfaceEventMappingService.class);
  /** The Constant INSURANCE_CO_ID. */
  private static final String INSURANCE_CO_ID = "insurance_co_id";

  /* Message msg ; */

  @IgnoreConfidentialFilters
  public ActionForward getdetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException, ParseException, IOException {

    HttpSession session = request.getSession();
    String billingCounterId = (String) session.getAttribute("billingcounterId");
    request.setAttribute("billingCounterId", billingCounterId);

    BasicDynaBean mst = (BasicDynaBean) masterTimestampDao.getRecord();
    request.setAttribute("masterTimeStamp", mst.get("master_count"));

    request.setAttribute("allowBillNowInsurance",
        BillDAO.isBillInsuranceAllowed(Bill.BILL_TYPE_PREPAID, true));
    request.setAttribute("hasMadatoryAddlnFields",
        RegistrationPreferencesDAO.getMadatoryAddlnFieldsCount() > 0);

    int[] tempVisitFields = RegistrationPreferencesDAO.getMadatoryVisitAddlnFieldsCount();
    request.setAttribute("hasMadatoryVisitAddlnFields", tempVisitFields[0] > 0);
    request.setAttribute("hasVisitAddlnFields", tempVisitFields[1] > 0);

    String countryCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
    if (StringUtil.isNullOrEmpty(countryCode)) {
      countryCode = centerMasterDAO.getCountryCode(0);
    }
    request.setAttribute("defaultCountryCode", countryCode);
    request.setAttribute("patientConfidentialityCategories",
        js.serialize(ConversionUtils.listBeanToListMap(confidentialityService
            .getUserConfidentialityGroups((String) session.getAttribute("userid")))));
    request.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
    request.setAttribute("visitClassifications", VisitClassificationType.enumMapToList());
    request.setAttribute("mobilePatAccess", "N");
    Preferences pref = (Preferences) session.getAttribute("preferences");
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      String modMobile = (String) pref.getModulesActivatedMap().get("mod_mobile");
      if (modMobile != null && modMobile.equals("Y")) {
        request.setAttribute("mobilePatAccess", "Y");
      }
    }
    setRegistrationAttributes(request, mapping.getProperty("action_id"));

    return mapping.findForward("onloadreg");
  }

  /*
   * Gets the details via AJAX. This method was created to reduce the load time of
   * registration page. Objects that are heavy can be brought after initial page load via
   * this method. Other attributes from getDetails() method may also be moved here as and
   * when needed.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the details AJAX
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */

  @IgnoreConfidentialFilters
  public ActionForward getdetailsAJAX(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

    HttpSession session = (HttpSession) request.getSession(false);
    Map<String, Object> responseMap = new HashMap<>();
    Integer centerId = (Integer) session.getAttribute("centerId");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    LinkedHashMap<Integer, List<String>> networkTypeSponsorIdListMap = new LinkedHashMap<>();
    responseMap.put("companyTpaList", ConversionUtils
        .listBeanToListMap(insuranceCompanyTpaMasterDao.getCompanyTpaList(centerId)));
    responseMap.put("discountPlansJSON", ConversionUtils
        .listBeanToMapMap(DiscountPlanMasterDAO.getInsuranceDiscountPlans(), "insurance_plan_id"));
    responseMap.put("tpanames", ConversionUtils.listBeanToListMap(TpaMasterDAO.gettpanames()));
    List<DynaBeanMapDecorator> insuCategoryNames = ConversionUtils.listBeanToListMap(
        InsuranceCategoryMasterDAO.getInsCatCenter(RequestContext.getCenterId()));
    for (DynaBeanMapDecorator insuranceCategory : insuCategoryNames) {
      Map<String, Object> filterMap = new HashMap<>();
      Integer categoryId = (Integer) insuranceCategory.get("category_id");
      filterMap.put("category_id", categoryId);
      filterMap.put("status", "A");
      List<BasicDynaBean> planBeans = insurancePlanService.getSponserByCategory(categoryId);
      List<String> sponsorIdList = new ArrayList<>();
      for (BasicDynaBean planBean : planBeans) {
        sponsorIdList.add((String) planBean.get("sponsor_id"));
      }
      networkTypeSponsorIdListMap.put(categoryId, sponsorIdList);
    }
    Map<String, Object> filterMap = new HashMap<>();
	filterMap.put("status", "A");
	responseMap.put("policyNames", ConversionUtils.listBeanToListMap(insurancePlanService.listAll(filterMap)));
    responseMap.put("insuCategoryNames", insuCategoryNames);
    responseMap.put("networkTypeSponsorIdListMap", networkTypeSponsorIdListMap);
    List<String> insuCompanyDetailsColumns = Arrays.asList("insurance_co_id", "status",
        "insurance_co_name");
    responseMap.put("insuCompanyDetails", ConversionUtils.listBeanToListMap(
        insuCompMasterDao.listAll(insuCompanyDetailsColumns, "status", "A", null)));
    responseMap.put("categoryWiseRateplans", ConversionUtils.listBeanToListMap(
        PatientCategoryDAO.getAllCategoriesIncSuperCenter(RequestContext.getCenterId())));
    responseMap.put("orgNameJSON",
        ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations()));
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }


  @IgnoreConfidentialFilters
  public ActionForward getRatePlan(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("categoryWiseRateplans", ConversionUtils.listBeanToListMap(
        PatientCategoryDAO.getAllCategoriesIncSuperCenter(RequestContext.getCenterId())));
    responseMap.put("orgNameJSON",
        ConversionUtils.listBeanToListMap(OrgMasterDao.getOrganizations()));
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getSponsorDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String tpaId = request.getParameter("tpa_id");
    String categoryString = request.getParameter("category_id");
    Map<String, Object> responseMap = new HashMap<>();
    Integer categoryId = null;
    if (categoryString != null && !categoryString.isEmpty()) {
      categoryId = Integer.parseInt(categoryString);
    }
    BasicDynaBean sponsorBean = tpaService.getDetails(tpaId);
    if (sponsorBean == null) {
      throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
    }
    List<BasicDynaBean> insuranceCompaniesList = insuranceCompanyService
        .getMappedInsuranceCompanies(tpaId, categoryString);
    List<BasicDynaBean> networkPlanTypesList = insurancePlanTypeService
        .getPlanTypesForSponsor(tpaId, categoryId, centerId);
    List<BasicDynaBean> planNamesList = insurancePlanService.getPlanNamesForSponsor(tpaId,
        categoryId, centerId);
    Map<Object, Object> networkPlansMap = new HashMap<>();
    Map<Object, Object> insuranceCompaniesMap = new HashMap<>();
    for (BasicDynaBean planBean : planNamesList) {
      Integer networkPlanId = (Integer) planBean.get("category_id");
      List<Map> planList = (List<Map>) networkPlansMap.get(networkPlanId);
      if (planList == null) {
        List<Map> list = new ArrayList<>();
        list.add(planBean.getMap());
        networkPlansMap.put(networkPlanId, list);
      } else {
        planList.add(planBean.getMap());
        networkPlansMap.put(networkPlanId, planList);
      }
    }
    for (BasicDynaBean networkPlanBean : networkPlanTypesList) {
      String insuCompId = (String) networkPlanBean.get(INSURANCE_CO_ID);
      List<Map> planTypeList = (List<Map>) insuranceCompaniesMap.get(insuCompId);
      List<Map> planNames = (List<Map>) networkPlansMap.get(networkPlanBean.get("category_id"));
      Map map = new HashMap(networkPlanBean.getMap());
      if (planNames == null) {
        continue;
      }
      map.put("plan_names", planNames);
      if (planTypeList == null) {
        List<Map> list = new ArrayList<>();
        list.add(map);
        insuranceCompaniesMap.put(insuCompId, list);
      } else {
        planTypeList.add(map);
        insuranceCompaniesMap.put(insuCompId, planTypeList);
      }
    }
    List<Map> finalInsuCompList = new ArrayList<>();
    for (BasicDynaBean insuCompBean : insuranceCompaniesList) {
      String insuCompId = (String) insuCompBean.get(INSURANCE_CO_ID);
      List<Map> planTypeList = (List<Map>) insuranceCompaniesMap.get(insuCompId);
      Map map = new HashMap(insuCompBean.getMap());
      map.put("network_plan_types", planTypeList != null ? planTypeList : Collections.EMPTY_LIST);
      finalInsuCompList.add(map);
    }
    Map<String, Object> sponsorMap = new HashMap<>(sponsorBean.getMap());
    sponsorMap.put("insurance_companies", finalInsuCompList);
    BasicDynaBean healthAuthTpaBean = tpaService.getTpaHealthAuthorityDetails(tpaId, centerId);
    if (healthAuthTpaBean != null) {
      sponsorMap.put("enable_eligibility_authorization",
          healthAuthTpaBean.get("enable_eligibility_authorization"));
    }
    sponsorMap.put("eligibility_authorization_status",
        ConversionUtils.listBeanToListMap(eligibilityAuthService.listAll()));

    responseMap.put("insurance_sponsor_details", sponsorMap);
    response.getWriter().write(js.deepSerialize(responseMap));
    response.flushBuffer();
    return null;
  }

  private void setRegistrationAttributes(HttpServletRequest request, String actionId)
      throws SQLException, ParseException, IOException {
    HttpSession session = (HttpSession) request.getSession(false);
    Integer centerId = (Integer) session.getAttribute("centerId");
    BasicDynaBean centerBean = centerMasterDao.findByKey("center_id", centerId);
    String healthAuthorityForCenter = (String) centerBean.get("health_authority");

    request.setAttribute("preferredLanguages", RegistrationPreferencesDAO
        .getPreferredLanguages((String) request.getAttribute("language")));
    request.setAttribute("defaultPreferredLanguage",
        GenericPreferencesDAO.getAllPrefs().get("contact_pref_lang_code"));
    request.setAttribute("salutationQueryJson",
        js.serialize(ConversionUtils.listBeanToListMap(SalutationMasterDAO.getSalutationIdName())));
    RegistrationPreferencesDTO regPrefTemp = RegistrationPreferencesDAO
        .getRegistrationPreferences();
    request.setAttribute("regPref", regPrefTemp);
    request.setAttribute("regPrefJSON", js.serialize(regPrefTemp));
    BasicDynaBean centerPrefs = centerPreferencesDao.getCenterPreferences(centerId);
    request.setAttribute("centerPrefs", centerPrefs);
    request.setAttribute("centerPrefsJson", js.serialize(centerPrefs.getMap()));
    request.setAttribute("healthAuthoPrefJSON", js.serialize(
        HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter)));
    request.setAttribute("orgConstant", Constants.getConstantValue("ORG"));
    // should be cached
    request.setAttribute("referalDetails", js.serialize(ReferalDoctorDAO.getReferencedoctors()));
    request.setAttribute("departmentsList", DepartmentMasterDAO.getDepartmentsNamesAndIds());

    // only for IP
    if (actionId.equals("ip_registration")) {
      if (ipPerfsDao.getPreferences().get("allocate_bed_at_reg").equals("Y"))
        request.setAttribute("bedTypesJson",
            js.serialize(ConversionUtils.listBeanToListMap(BedMasterDAO.getAllActiveBedTypes())));
      else
        request.setAttribute("bedTypesJson", js.serialize(
            ConversionUtils.listBeanToListMap(BedMasterDAO.getAllActiveBillableBedTypes())));
      request.setAttribute("ip_prefs", ipPreferencesDao.getRecord());
      // in multi center schema wards must belong to user center.
      Map<String, Object> filterMap = new HashMap<String, Object>();
      filterMap = new HashMap<String, Object>();
      if (centerId != 0
          && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1)
        filterMap.put("center_id", (Integer) request.getSession().getAttribute("centerId"));
      filterMap.put("status", "A");
      List wardsList = wardNamesDao.listAll(null, filterMap, "ward_name");
      request.setAttribute("wards", wardsList);
      request.setAttribute("wardsJson", js.serialize(ConversionUtils.listBeanToListMap(wardsList)));
      request.setAttribute("paymentModesJSON",
          js.serialize(ConversionUtils.listBeanToListMap(paymentModeMasterDao.listAll())));
      request.setAttribute("doctorConsultationTypes", null);
      request.setAttribute("allDoctorConsultationTypes", null);
      request.setAttribute("doctorCharges", null);
      request.setAttribute("defaultDiagnosisCodeType", null);
      request.setAttribute("anaeTypesJSON", null);
      List doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList();
      request.setAttribute("doctorsList",
          js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));
      request.setAttribute("dutydoclist", doctorsList);
      List departmentList = ConversionUtils.listBeanToListMap(departmentMasterDao.listAll());
      request.setAttribute("deptsList", js.serialize(departmentList));
      request.setAttribute("arrdeptDetails", DepartmentMasterDAO.getDeapartmentlist());
      request.setAttribute("unitDetails", js.serialize(
          ConversionUtils.listBeanToListMap(DepartmentUnitMasterDAO.getAllDepartmentUnits())));
      List docDeptNameList = EditVisitDetailsDAO.getDoctorDeptList("%");
      request.setAttribute("docDeptNameList",
          js.serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));
    } else {
      request.setAttribute("doctorConsultationTypes", null);
      request.setAttribute("allDoctorConsultationTypes", null);
      request.setAttribute("doctorCharges", null);
      request.setAttribute("defaultDiagnosisCodeType", HealthAuthorityPreferencesDAO
          .getHealthAuthorityPreferences(healthAuthorityForCenter).getDiagnosis_code_type());
      request.setAttribute("anaeTypesJSON", null);
      request.setAttribute("paymentModesJSON", js.serialize(null));
      request.setAttribute("unitDetails", js.serialize(null));
      request.setAttribute("arrdeptDetails", null);
      request.setAttribute("deptsList", js.serialize(null));
      request.setAttribute("dutydoclist", null);
      request.setAttribute("doctorsList", js.serialize(null));
      request.setAttribute("docDeptNameList", js.serialize(null));
    }
    // IP ends

    request.setAttribute("mlc_templates", GenericDocumentTemplateDAO.getTemplates(true, "4", "A"));

    BasicDynaBean genPrefBean = GenericPreferencesDAO.getAllPrefs();
    request.setAttribute("genPrefs", js.serialize(genPrefBean.getMap()));
    request.setAttribute("clinicalPrefs", ApplicationContextProvider.getApplicationContext()
        .getBean(ClinicalPreferencesService.class).getClinicalPreferences().getMap());
    request.setAttribute("pref",
        PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL));
    request.setAttribute("templateList", BillPrintTemplateDAO.getBillTemplateList());

    request.setAttribute("gen_category",
        js.serialize(((BasicDynaBean) patientCategoryDao.findByKey("category_id", 1)).getMap()));
    request.setAttribute("docs", VisitDetailsDAO.getDocsUpload());

    BasicDynaBean areaBean = AreaMasterDAO
        .getAreaCityStateCountryList(request.getParameter("area_id"));
    String areaId = null;
    String cityId = null;
    String stateId = null;
    String countryId = null;
    String areaName = null;
    String cityName = null;
    String stateName = null;
    String countryName = null;
    String districtId = null;
    String districtName = null;
    if (!((String) centerBean.get("city_id")).equals("")) {
      if (areaBean != null) {
        areaId = (String) areaBean.get("area_id");
        areaName = (String) (areaMasterGenDao.findByKey("area_id", areaId)).get("area_name");
      }
      cityId = (String) centerBean.get("city_id");
      stateId = (String) centerBean.get("state_id");
      countryId = (String) centerBean.get("country_id");
      cityName = (String) (cityMaster.findByKey("city_id", cityId)).get("city_name");
      stateName = (String) (stateMaster.findByKey("state_id", stateId)).get("state_name");
      countryName = (String) (countryMaster.findByKey("country_id", countryId)).get("country_name");
      BasicDynaBean districtBean = patientDetailsDao.getDistrictDetails(cityId, stateId);
      if (districtBean != null) {
        districtId = (String) districtBean.get("district_id");
        districtName = (String) districtBean.get("district_name");
      }
    }
    request.setAttribute("defaultCity", cityId);
    request.setAttribute("defaultDistrict", districtId);
    request.setAttribute("defaultState", stateId);
    request.setAttribute("defaultCountry", countryId);
    request.setAttribute("defaultAreaName", areaName);
    request.setAttribute("defaultCityName", cityName);
    request.setAttribute("defaultDistrictName", districtName);
    request.setAttribute("defaultStateName", stateName);
    request.setAttribute("defaultCountryName", countryName);

    /* Get the appointment details if the request is from scheduler */
    String appointmentId = request.getParameter("appointment_id");
    if (appointmentId != null && !appointmentId.equals("")) {
      String category = request.getParameter("category");
      String registrationType = request.getParameter("registrationType");
      String schedulerGenerateOrder = (String) genPrefBean.get("scheduler_generate_order");
      BasicDynaBean apptBean = schedulerAppointmentsDao.findByKey("appointment_id",
          Integer.parseInt(appointmentId));
      if (schedulerGenerateOrder.equals("Y")) {
        List appointmentDetails = ResourceDAO
            .getAppointmentDetails(Integer.parseInt(appointmentId));
        if (appointmentDetails != null && appointmentDetails.size() > 0) {
          BasicDynaBean appointBean = (BasicDynaBean) appointmentDetails.get(0);
          Integer pat_package_id = (Integer) appointBean.get("pat_package_id");
          int packageId = -1;
          List<BasicDynaBean> channellingMultiVisitPacakgeUnorderedDetails = null;
          if (pat_package_id != null) {
            // get the MVP for a doctor
            List packId = PackageDAO
                .getMvpForChannelingRes((String) appointBean.get("res_sch_name"));
            if (packId != null && packId.size() > 0) {
              BasicDynaBean packIdBean = (BasicDynaBean) packId.get(0);
              packageId = ((java.math.BigDecimal) packIdBean.get("package_id")).intValue();
            }
          }
          if (packageId != -1) {
            channellingMultiVisitPacakgeUnorderedDetails = new ArrayList<BasicDynaBean>();
            String mrNo = (String) appointBean.get("mr_no");
            String dept_id = (String) appointBean.get("dept_id");
            request.setAttribute("dept_id", dept_id);
            channellingMultiVisitPacakgeUnorderedDetails.addAll(
                PackageDAO.getChannellingMultiVisitPackageUnorderedComponentDetails(packageId, mrNo,
                    pat_package_id));
            request.setAttribute("channellingOrdersList", js.serialize(
                ConversionUtils.listBeanToListMap(channellingMultiVisitPacakgeUnorderedDetails)));
            request.setAttribute("pat_package_id", pat_package_id);
          }
          request.setAttribute("appointmentDetailsList",
              js.serialize(ConversionUtils.listBeanToListMap(appointmentDetails)));
        } else {
          request.setAttribute("appointmentDetailsList", js.serialize(null));
        }
      } else {
        request.setAttribute("appointmentDetailsList", js.serialize(null));
        request.setAttribute("apptDetailsExcludingOrders", js.serialize(apptBean.getMap()));
      }
      request.setAttribute("appointmentId", appointmentId);
      request.setAttribute("category", category);
      request.setAttribute("registrationType", registrationType);
      request.setAttribute("referer", request.getHeader("Referer"));
      request.setAttribute("scheduleName", apptBean.get("res_sch_name"));
      request.setAttribute("presDocId", apptBean.get("presc_doc_id"));
    }

    String doctorId = request.getParameter("doctor_id");
    if (doctorId != null && !doctorId.equals("")) {
      BasicDynaBean docDeptBean = DoctorMasterDAO.getDoctorDept(doctorId);
      request.setAttribute("docDeptName", docDeptBean.get("dept_name"));
      request.setAttribute("docName", docDeptBean.get("doctor_name"));
      request.setAttribute("docDeptId", docDeptBean.get("dept_id"));
    }

    String admissionRequestId = request.getParameter("adm_request_id");
    if (admissionRequestId != null && !admissionRequestId.isEmpty()) {
      BasicDynaBean admissionReqBean = AdmissionRequestDAO
          .getPatientAdmissionRequestDetails(Integer.parseInt(admissionRequestId));
      if (admissionReqBean != null) {
        request.setAttribute("admissionRequestDetails", js.serialize(admissionReqBean.getMap()));
      } else {
        request.setAttribute("admissionRequestDetails", js.serialize(null));
      }
      request.setAttribute("adm_request_id", admissionRequestId);
    }

    List<BasicDynaBean> govtIdentifier = govtIdMasterDao.listAll(null, "status", "A");
    request.setAttribute("govtIdentifierTypes", govtIdentifier);
    request.setAttribute("govtIdentifierTypesJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(govtIdentifier)));
    List<BasicDynaBean> otherIdentifier = otherIdDocTypesDAO.listAll(null, "status", "A");
    request.setAttribute("otherIdentifierTypes", otherIdentifier);
    request.setAttribute("otherIdentifierTypesJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(otherIdentifier)));
    request.setAttribute("sponsorTypelist",
        js.serialize(ConversionUtils.listBeanToListMap(sponsorTypeDao.listAll())));
    request.setAttribute("uhidPatient", request.getParameter("uhidPatient"));
    if (null != request.getParameter("uhidPatient")
        && request.getParameter("uhidPatient").equals("yes")) {
      request.setAttribute("uhidPatientFirstName", request.getParameter("uhidPatientFirstName"));
      request.setAttribute("uhidPatientMiddleName", request.getParameter("uhidPatientMiddleName"));
      request.setAttribute("uhidPatientLastName", request.getParameter("uhidPatientLastName"));
      request.setAttribute("uhidPatientPhone", request.getParameter("uhidPatientPhone"));
      request.setAttribute("uhidPatientGender", request.getParameter("uhidPatientGender"));
      request.setAttribute("uhidPatientUHID", request.getParameter("uhidPatientUHID"));
      request.setAttribute("uhidPatientAge", request.getParameter("uhidPatientAge"));
    }
  }

  @IgnoreConfidentialFilters
  public ActionForward getChannelingOrders(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, IOException, Exception {

    HttpSession session = req.getSession();
    String appointmentId = req.getParameter("appointmentId");

    Map<String, Object> map = new HashMap<String, Object>();
    if (appointmentId != null && !appointmentId.equals("")) {
      BasicDynaBean apptBean = schedulerAppointmentsDao.findByKey("appointment_id",
          Integer.parseInt(appointmentId));
      List appointmentDetails = ResourceDAO.getAppointmentDetails(Integer.parseInt(appointmentId));
      if (appointmentDetails != null && appointmentDetails.size() > 0) {
        BasicDynaBean appointBean = (BasicDynaBean) appointmentDetails.get(0);
        String status = (String) appointBean.get("appointment_status");
        map.put("status", false);
        // req.setAttribute("registrationType", registrationType);
        // req.setAttribute("referer", req.getHeader("Referer"));
        // req.setAttribute("scheduleName", apptBean.get("res_sch_name"));
      } else {
        map.put("status", false);
        map.put("appointmentDetailsList", null);
      }
    }
    res.setContentType("application/x-json");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(js.deepSerialize(map));
    res.flushBuffer();
    return null;
    // return redirect;
  }

  /**
   * Ignore Confidential Filter used to overcome display of uncaught exceptions, which ends up
   * redirecting to GET call causing 403 to be displayed instead of opening the 500 error page.
   * PCG is handled for regd patient as part of validateMrno function.
   */
  @IgnoreConfidentialFilters
  public ActionForward doRegister(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception, FileNotFoundException, IOException {

    String admRequestId = request.getParameter("adm_request_id");
    HttpSession session = request.getSession();
    String userName = (String) session.getAttribute("userid");
    String originalAdmUserName = userName;
    if(!StringUtils.isEmpty(admRequestId)){
      BasicDynaBean admDbBean = new GenericDAO("patient_admission_request").findByKey("adm_request_id", Integer.parseInt(admRequestId));
      if(admDbBean!=null && admDbBean.get("user_name")!=null){
        originalAdmUserName = (String) admDbBean.get("user_name");
      }
    }
    Integer centerId = (Integer) session.getAttribute("centerId");
    String registrationChargesApplicable = request.getParameter("apply_registration_charges");
    registrationChargesApplicable = (registrationChargesApplicable == null
        || registrationChargesApplicable.equals("")) ? "Y" : registrationChargesApplicable;
    Integer sampleCollectionCenterId = (Integer) session.getAttribute("sampleCollectionCenterId");
    Map requestParams = new HashMap(request.getParameterMap());
    String regAndBill = getParamDefault(request, "regAndBill", "N");
    Preferences pref = (Preferences) session.getAttribute("preferences");

    List<BasicDynaBean> patPrimaryInsuranceDetailsBeanList = null;
    List<BasicDynaBean> patSecondaryInsuranceDetailsBeanList = null;

    String modAdvanceOTActive = "Y";
    String errorMsg = null;
    if ((pref != null) && (pref.getModulesActivatedMap() != null)) {
      modAdvanceOTActive = (String) pref.getModulesActivatedMap().get("mod_advanced_ot");
      if (modAdvanceOTActive == null || modAdvanceOTActive.equals("")) {
        modAdvanceOTActive = "N";
      }
    }

    List errors = new ArrayList();

    String primarySponsor = request.getParameter("primary_sponsor");
    String secondarySponsor = request.getParameter("secondary_sponsor");

    String primarySponsorId = null;
    String primaryInsCompId = null;

    String secondarySponsorId = null;
    String secondaryInsCompId = null;

    String priorAuthId = null;
    Integer priorAuthModeId = 0;

    String estimateAmtStr = request.getParameter("estimateAmount");
    BigDecimal estimatedAmt = (estimateAmtStr != null && !estimateAmtStr.equals(""))
        ? new BigDecimal(estimateAmtStr)
        : BigDecimal.ZERO;
    int planId = 0;
    Integer categoryId = 0;
    String useDRG = "N";

    int secPlanId = 0;

    String primaryInsuranceApprovalStr = null;
    String secondaryInsuranceApprovalStr = null;

    BigDecimal primaryInsuranceApproval = null;
    BigDecimal secondaryInsuranceApproval = null;

    BasicDynaBean primaryInsuranceBean = planDAO.getBean();
    BasicDynaBean secondaryInsBean = planDAO.getBean();

    int noOfPlans = 0;

    if (null != primarySponsor && primarySponsor.equals("I"))
      noOfPlans++;

    if (null != secondarySponsor && secondarySponsor.equals("I"))
      noOfPlans++;

    int[] planIds = noOfPlans > 0 ? new int[noOfPlans] : null;
    String[] preAuthIds = new String[2];
    Integer[] preAuthModeIds = new Integer[2];
    int planIdIdx = 0;

    if (primarySponsor != null) {
      if (primarySponsor.equals("I")) {
        ConversionUtils.copyToDynaBean(requestParams, primaryInsuranceBean);
        primaryInsuranceBean = getPrimarySponsorDetails(request, primaryInsuranceBean);
        planId = (primaryInsuranceBean.get("plan_id") == null
            || primaryInsuranceBean.get("plan_id").equals("")) ? 0
                : (Integer) primaryInsuranceBean.get("plan_id");
        primarySponsorId = (String) primaryInsuranceBean.get("sponsor_id");
        primaryInsCompId = (String) primaryInsuranceBean.get("insurance_co");
        primaryInsuranceApprovalStr = getParamDefault(request, "primary_insurance_approval", null);
        categoryId = (Integer) primaryInsuranceBean.get("plan_type_id");
        useDRG = (String) primaryInsuranceBean.get("use_drg");
        priorAuthId = (String) primaryInsuranceBean.get("prior_auth_id");
        priorAuthModeId = (Integer) primaryInsuranceBean.get("prior_auth_mode_id");

        if (null != planIds) {
          planIds[planIdIdx] = planId;
          preAuthIds[planIdIdx] = priorAuthId;
          preAuthModeIds[planIdIdx] = priorAuthModeId;
          planIdIdx++;
        }

      } else if (primarySponsor.equals("C")) {
        primarySponsorId = request.getParameter("primary_corporate");
        primaryInsuranceApprovalStr = getParamDefault(request, "primary_corporate_approval", null);

      } else if (primarySponsor.equals("N")) {
        primarySponsorId = request.getParameter("primary_national_sponsor");
        primaryInsuranceApprovalStr = getParamDefault(request, "primary_national_approval", null);
      }

      primaryInsuranceApproval = primaryInsuranceApprovalStr != null
          ? new BigDecimal(primaryInsuranceApprovalStr)
          : null;
    }

    if (secondarySponsor != null) {
      if (secondarySponsor.equals("I")) {
        ConversionUtils.copyToDynaBean(requestParams, secondaryInsBean);
        secondaryInsBean = getSecondarySponsorDetails(request, secondaryInsBean);
        secPlanId = (secondaryInsBean.get("plan_id") == null
            || secondaryInsBean.get("plan_id").equals("")) ? 0
                : (Integer) secondaryInsBean.get("plan_id");
        secondarySponsorId = (String) secondaryInsBean.get("sponsor_id");
        secondaryInsCompId = (String) secondaryInsBean.get("insurance_co");
        secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_insurance_approval",
            null);

        if (null != planIds) {
          planIds[planIdIdx] = secPlanId;
          preAuthIds[planIdIdx] = (String) secondaryInsBean.get("prior_auth_id");
          preAuthModeIds[planIdIdx] = (Integer) secondaryInsBean.get("prior_auth_mode_id");
          planIdIdx++;
        }

      } else if (secondarySponsor.equals("C")) {
        secondarySponsorId = request.getParameter("secondary_corporate");
        secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_corporate_approval",
            null);

      } else if (secondarySponsor.equals("N")) {
        secondarySponsorId = request.getParameter("secondary_national_sponsor");
        secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_national_approval",
            null);
      }

      secondaryInsuranceApproval = secondaryInsuranceApprovalStr != null
          ? new BigDecimal(secondaryInsuranceApprovalStr)
          : null;
    }

    String category = request.getParameter("category");
    String scheduleName = request.getParameter("scheduleName");
    String schedulerCategory = request.getParameter("schedulerCategory");
    scheduleName = (scheduleName == null || scheduleName.equals("")) ? null : scheduleName;

    String last_active_visit = request.getParameter("last_active_visit");

    Connection con = null;
    boolean success = true;
    boolean allSuccess = false;
    ActionRedirect redirect = null;
    FlashScope flash = FlashScope.getScope(request);
    RegistrationForm regForm = (RegistrationForm) form;

    String regType = request.getParameter("regType");
    String visitType = request.getParameter("group");
    String patientId = null;
    String patientType = null;

    BasicDynaBean patientDetailsBean = patientDetailsDao.getBean();
    BasicDynaBean visitDetailsBean = rdao.getBean();

    boolean is_tpa = primarySponsorId != null && !primarySponsorId.isEmpty();

    Preferences prefs = (Preferences) session.getAttribute("preferences");
    Map groups = prefs.getModulesActivatedMap();

    RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
    boolean doc_eandm_codification_required = regPrefs.getDoc_eandm_codification_required() != null
        && regPrefs.getDoc_eandm_codification_required().equals("Y");

    // Default allow multiple active visits is No.
    String allowMultipleActiveVisits = (regPrefs.getAllow_multiple_active_visits() != null
        && !regPrefs.getAllow_multiple_active_visits().equals(""))
            ? regPrefs.getAllow_multiple_active_visits()
            : "N";

    String billNo = null;
    String mrNo = null;
    // when reg_name contains four parts we r appending the 3rd part to 2nd part and saving in
    // DB
    String middle_name = "";
    String middle_name1 = getValue("middle_name", requestParams);
    String middle_name2 = getValue("middle_name2", requestParams);
    if (middle_name2 != null && !middle_name2.equals("..MiddleName2..")) {
      if (middle_name1 != null && !middle_name1.equals("..MiddleName.."))
        middle_name = middle_name1.trim() + "" + middle_name2.trim();
    } else if (middle_name1 != null && middle_name1.equals("..MiddleName.."))
      middle_name = "";
    else
      middle_name = middle_name1.trim();
    requestParams.put("middle_name", middle_name.trim());
    String emailCommPref = getValue("modeOfCommEmail", requestParams);
    String smsCommPref = getValue("modeOfCommSms", requestParams);
    String communication = "B";
    if (emailCommPref.equals("on") || smsCommPref.equals("on")) {
      if (!emailCommPref.equals("on")) {
        communication = "S";
      }
      if (!smsCommPref.equals("on")) {
        communication = "E";
      }
    } else {
      communication = "N";
    }
    int appointmentId = 0;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      ConversionUtils.copyToDynaBean(requestParams, patientDetailsBean, errors, true);

      if (patientDetailsBean.get("middle_name") == null)
        patientDetailsBean.set("middle_name", "");

      if (patientDetailsBean.get("last_name") != null
          && patientDetailsBean.get("last_name").equals("..LastName.."))
        patientDetailsBean.set("last_name", "");

      if (!errors.isEmpty()) {
        String msg = getResources(request)
            .getMessage("registration.patient.action.message.error.in.copying.visit.details");
        redirect = new ActionRedirect(mapping.findForwardConfig("failureRedirect"));
        flash.error(msg);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
      ConversionUtils.copyToDynaBean(requestParams, visitDetailsBean, errors, true);
      if (!errors.isEmpty()) {
        String msg = getResources(request)
            .getMessage("registration.patient.action.message.error.in.copying.visit.details");
        redirect = new ActionRedirect(mapping.findForwardConfig("failureRedirect"));
        flash.error(msg);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }

      if (request.getParameter("appointmentId") != null
          && !request.getParameter("appointmentId").equals("")) {
        appointmentId = new Integer(request.getParameter("appointmentId"));
      }

      String dateOfBirth = request.getParameter("dateOfBirth");
      String mlcSelected = request.getParameter("patientMlcStatus");
      visitDetailsBean.set("reg_date", new java.sql.Date((new java.util.Date()).getTime()));
      visitDetailsBean.set("reg_time", new java.sql.Time((new java.util.Date()).getTime()));
      visitDetailsBean.set("status", "A");

      visitDetailsBean.set("user_name", userName);
      visitDetailsBean.set("created_by", userName);
      visitDetailsBean.set("center_id", centerId);
      visitDetailsBean.set("collection_center_id", sampleCollectionCenterId);

      patientDetailsBean.set("user_name", userName);
      String patientCategoryId = getParamDefault(request, "patient_category_id", "1");
      patientDetailsBean.set("patient_category_id", Integer.parseInt(patientCategoryId));
      visitDetailsBean.set("patient_category_id", Integer.parseInt(patientCategoryId));
      String orgId = getParamDefault(request, "organization", "ORG0001");
      visitDetailsBean.set("org_id", orgId);

      /*
       * In case bed allocation is at registration time,Non Billing bedtype are also available
       * for Registration. Check if the selected bedtype is non-billing bedtype if yes set
       * 'GENERAL' bed type as visit bedtype which can never be non-billing bedtype
       */
      if (visitType.equals("ipreg")
          && ipPerfsDao.getPreferences().get("allocate_bed_at_reg").equals("Y")) {
        BasicDynaBean bedTypeBean = bedMasterDao
            .getBedType((String) visitDetailsBean.get("bed_type"));
        visitDetailsBean.set("bed_type",
            bedTypeBean.get("billing_bed_type").equals("Y") ? visitDetailsBean.get("bed_type")
                : "GENERAL");
      }

      visitDetailsBean.set("primary_sponsor_id", primarySponsorId);
      visitDetailsBean.set("primary_insurance_co", primaryInsCompId);

      visitDetailsBean.set("secondary_sponsor_id", secondarySponsorId);
      visitDetailsBean.set("secondary_insurance_co", secondaryInsCompId);

      visitDetailsBean.set("primary_insurance_approval", primaryInsuranceApproval);
      visitDetailsBean.set("secondary_insurance_approval", secondaryInsuranceApproval);

      visitDetailsBean.set("use_drg", useDRG);

      /* Bug 20326, 20398 */
      String caseFileRequired = "N";
      String caseFileNo = getParamDefault(request, "casefileNo", null);
      String raiseIndent = getParamDefault(request, "raiseCaseFileIndent", null);
      String oldMrnoAutoEnabled = getParamDefault(request, "oldRegAutoGenerate", null);
      String oldmrno = getParamDefault(request, "oldmrno", null);
      String uhid = getParamDefault(request, "uhidPatientUHID", null);

      BasicDynaBean categoryDetBean = patientCategoryDao.findByKey("category_id",
          Integer.parseInt(patientCategoryId));
      if (categoryDetBean != null && categoryDetBean.get("case_file_required") != null
          && !categoryDetBean.get("case_file_required").equals(""))
        caseFileRequired = categoryDetBean.get("case_file_required").toString();

      /* Generate Case File if case file required for patient category */
      if (caseFileRequired.equals("Y")) {
        if (oldMrnoAutoEnabled != null && "Y".equals(oldMrnoAutoEnabled)) {
          caseFileNo = DataBaseUtil.getNextPatternId("CASENO");
          patientDetailsBean.set("casefile_no", caseFileNo);
        } else if (caseFileNo != null && !caseFileNo.equals("")) {
          patientDetailsBean.set("casefile_no", caseFileNo);
        }
      }

      if (oldmrno != null) {
        patientDetailsBean.set("oldmrno", oldmrno);
      }

      /* Bug 19841 */
      visitDetailsBean.set("admitted_dept", visitDetailsBean.get("dept_name"));

      if (visitDetailsBean.get("ward_id") == null)
        visitDetailsBean.set("ward_id", "");
      if (mlcSelected.equals("Y")) {
        visitDetailsBean.set("mlc_status", "Y");
        String mlcNumber = DataBaseUtil.getNextPatternId("MLCNO");
        visitDetailsBean.set("mlc_no", mlcNumber);
      } else {
        visitDetailsBean.set("mlc_status", "N");
        visitDetailsBean.set("mlc_type", null);
        visitDetailsBean.set("accident_place", null);
        visitDetailsBean.set("police_stn", null);
        visitDetailsBean.set("mlc_remarks", null);
        visitDetailsBean.set("certificate_status", null);
      }

      String cardImage = getValue("cardImage", requestParams);
      if (null != cardImage && !cardImage.equals("")) {
        patientDetailsBean.set("patient_photo", new ByteArrayInputStream(
            Base64.decodeBase64(request.getParameter("cardImage").getBytes())));
      }
      /* Patient photo */
      if (regForm.getPatPhoto() != null && regForm.getPatPhoto().getFileSize() > 0) {
        FormFile ff = regForm.getPatPhoto();
        patientDetailsBean.set("patient_photo", ff.getInputStream());
      } else if (null != regForm.getPastedPhoto() && regForm.getPastedPhoto().getFileSize() > 0) {
        patientDetailsBean.set("patient_photo", regForm.getPastedPhoto().getInputStream());
      }

      /* Generate a new Mrno. */
      if (regType.equals("new") && uhid != null && !uhid.isEmpty()) {
        mrNo = uhid;
        patientDetailsBean.set("resource_captured_from", "uhid");
      } else {
        mrNo = regBO.generateMrno(patientDetailsBean, regType, request.getParameter("mrno"));
      }

      BasicDynaBean apptBean = null;
      apptBean = schedulerAppointmentsDao.findByKey("appointment_id", appointmentId);
      if (apptBean != null && apptBean.get("contact_id") != null) {
        PatientDetailsDAO.flushContact(mrNo, (Integer) apptBean.get("contact_id"));
      }

      if (allowMultipleActiveVisits.equals("N")
          && !request.getParameter("screenId").equals("out_pat_reg")) {
        String visitId = VisitDetailsDAO.getPatientLatestVisitId(mrNo, true, "i");
        if (visitId != null && !visitId.equals("")) {
          String msg = getResources(request)
              .getMessage("registration.patient.action.message.error.active.visit.exists");
          redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
          flash.error(msg);
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          return redirect;
        }
      }

      BasicDynaBean patientBean = patientDetailsDao.findByKey(con, "mr_no", mrNo);
      if (request.getParameter("mobilePatAccess") != null
          && request.getParameter("mobilePatAccess").equals("Y")) {
        patientDetailsBean.set("mobile_access", true);
        if ((patientBean == null || (patientBean.get("mobile_password") == null
            || "".equals((String) patientBean.get("mobile_password"))))) {
          patientDetailsBean.set("mobile_password", RandomGeneration.randomGeneratedPassword(6));
        }
      }
      patientDetailsBean.set("mr_no", mrNo);
      visitDetailsBean.set("mr_no", mrNo);

      if (patientDetailsDao.isMrnoVisitExists(con, mrNo)) {
        visitDetailsBean.set("revisit", "Y");
      } else {
        visitDetailsBean.set("revisit", "N");
        if (patientBean == null
            || (patientBean != null && (patientBean.get("first_visit_reg_date") == null
                || patientBean.get("first_visit_reg_date").equals("")))) {
          patientDetailsBean.set("first_visit_reg_date",
              new java.sql.Date((new java.util.Date()).getTime()));
        }
      }

      /* update the sanctioned IP Credit Limit(ip_credit_limit_amount) */
      String ipCreditLimit = getParamDefault(request, "ipcreditlimit", null);
      BigDecimal availableIpCreditLimit = BigDecimal.ZERO;
      if (ipCreditLimit != null && !ipCreditLimit.equals("")) {
        availableIpCreditLimit = new BigDecimal(ipCreditLimit.trim());
      }

      BigDecimal depositBal = RegistrationBO.getAvailableGeneralAndIpDeposit(mrNo);
      BigDecimal sanctionedCreditLimit = (availableIpCreditLimit.subtract(depositBal))
          .compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO
              : availableIpCreditLimit.subtract(depositBal);
      visitDetailsBean.set("ip_credit_limit_amount", sanctionedCreditLimit);

      /* Checking registration validity is expired or not. */
      boolean isRenewal = regBO.isRegValidityExpired(visitDetailsBean, mrNo, regType);

      /* Generate a new visitId. */
      if (visitType.equals("opreg")) {
        patientId = VisitDetailsDAO.getNextVisitId("o", centerId);
        visitDetailsBean.set("patient_id", patientId);
        visitDetailsBean.set("visit_type", "o");

        String billingBedTypeForOP = GenericPreferencesDAO.getGenericPreferences()
            .getBillingBedTypeForOP();
        visitDetailsBean.set("bed_type",
            (billingBedTypeForOP != null && !billingBedTypeForOP.equals("")) ? billingBedTypeForOP
                : "GENERAL");
        patientType = "o";
      } else if (visitType.equals("ipreg")) {
        patientId = VisitDetailsDAO.getNextVisitId("i", centerId);
        visitDetailsBean.set("patient_id", patientId);
        visitDetailsBean.set("visit_type", "i");
        patientType = "i";
      }

      /* Patient age */
      String ageIn = request.getParameter("ageIn");
      if (dateOfBirth.trim().equals("") && !ageIn.equals("D")) {
        int age = Integer.parseInt(request.getParameter("age"));
        java.util.Date date = (java.util.Date) DateUtil.getExpectedDate(age, ageIn, true, true);
        java.sql.Date expectedDob = new java.sql.Date(date.getTime());

        if (regType.equalsIgnoreCase("new")) {
          patientDetailsBean.set("expected_dob", expectedDob);

        } else if (regType.equalsIgnoreCase("regd")) {
          BasicDynaBean patDetBean = PatientDetailsDAO
              .getPatientGeneralDetailsBean((String) patientDetailsBean.get("mr_no"));
          int existingAge = (Integer) patDetBean.get("age");
          String existingAgeIn = (String) patDetBean.get("agein");
          if (existingAge != age || !existingAgeIn.equals(ageIn))
            patientDetailsBean.set("expected_dob", expectedDob);
        }

      } else {
        // If ageIn is days, we can calculate the dob accurately. No need for expected_dob.
        java.sql.Date dob;
        if (dateOfBirth.trim().equals("")) {
          int age = Integer.parseInt(request.getParameter("age"));
          java.util.Date date = (java.util.Date) DateUtil.getExpectedDate(age, ageIn, true, true);
          dob = new java.sql.Date(date.getTime());
        } else {
          dob = Date.valueOf(dateOfBirth);
        }
        patientDetailsBean.set("dateofbirth", dob);
      }

      /* Encounter type */
      int encounterType = 0;
      BasicDynaBean visitEncBean = null;

      if (visitType.equals("opreg")) {
        visitEncBean = rdao.getVisitDefaultEncounter("o", false);

      } else if (visitType.equals("ipreg")) {
        boolean dayCare = request.getParameter("daycare_status") != null;
        visitEncBean = rdao.getVisitDefaultEncounter("i", dayCare);
      }
      if (visitEncBean != null) {
        encounterType = (Integer) visitEncBean.get("encounter_type_id");
      }
      visitDetailsBean.set("encounter_type", encounterType);

      /* Default Encounter Start type and Encounter End type */
      if (visitType.equals("opreg")) {
        visitDetailsBean.set("encounter_start_type",
            (regPrefs.getDefault_op_encounter_start_type() != null
                && !regPrefs.getDefault_op_encounter_start_type().equals(""))
                    ? new Integer(regPrefs.getDefault_op_encounter_start_type())
                    : null);
        visitDetailsBean.set("encounter_end_type",
            (regPrefs.getDefault_op_encounter_end_type() != null
                && !regPrefs.getDefault_op_encounter_end_type().equals(""))
                    ? new Integer(regPrefs.getDefault_op_encounter_end_type())
                    : null);

      } else if (visitType.equals("ipreg")) {
        visitDetailsBean.set("encounter_start_type",
            (regPrefs.getDefault_ip_encounter_start_type() != null
                && !regPrefs.getDefault_ip_encounter_start_type().equals(""))
                    ? new Integer(regPrefs.getDefault_ip_encounter_start_type())
                    : null);
        visitDetailsBean.set("encounter_end_type",
            (regPrefs.getDefault_ip_encounter_end_type() != null
                && !regPrefs.getDefault_ip_encounter_end_type().equals(""))
                    ? new Integer(regPrefs.getDefault_ip_encounter_end_type())
                    : null);
      }

      /*
       * Copy chief complaint & encounter types from previous visit(same episode) to episode
       * follow-up
       */
      regBO.copyComplaintAndEncounterTypes(visitDetailsBean);

      if (mlcSelected.equals("Y")) {
        patientDetailsBean.set("first_mlc_visitid", visitDetailsBean.get("patient_id").toString());
      }

      HashMap<String, Object> policyDetailsMap = new HashMap<String, Object>();
      HashMap<String, Object> corporateDetailsMap = new HashMap<String, Object>();
      HashMap<String, Object> nationalDetailsMap = new HashMap<String, Object>();

      HashMap<String, Object> secPolicyDetailsMap = new HashMap<String, Object>();
      HashMap<String, Object> secCorporateDetailsMap = new HashMap<String, Object>();
      HashMap<String, Object> secNationalDetailsMap = new HashMap<String, Object>();

      regBO.createInsuranceDetailsMap(request, policyDetailsMap, secPolicyDetailsMap);

      validate: {

        /* Validate Mrno */
        if (regType.equals("regd")) {
          success = regBO.validateMrno(request.getParameter("mrno"),
              request.getParameter("main_visit_id"));
          if (!success) {
            String msg = getResources(request).getMessage(
                "registration.patient.action.message.invalid.visit.and.mrno.details.failed.to.register.patient");
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            flash.error(msg);
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
        }

        String opType = request.getParameter("op_type");
        String mainVisitId = request.getParameter("main_visit_id");
        mainVisitId = (mainVisitId == null || mainVisitId.equals("")) ? patientId : mainVisitId;

        /* Op type. */
        if (!regType.equals("new") && opType != null) {
          if (opType.equals("M")) {
            visitDetailsBean.set("op_type", "M");
            visitDetailsBean.set("main_visit_id", patientId);

          } else if (opType.equals("F")) {
            visitDetailsBean.set("op_type", "F");
            visitDetailsBean.set("main_visit_id", mainVisitId);

          } else if (opType.equals("D")) {
            visitDetailsBean.set("op_type", "D");
            visitDetailsBean.set("main_visit_id", mainVisitId);

          } else if (opType.equals("R")) {
            visitDetailsBean.set("op_type", "R");
            visitDetailsBean.set("main_visit_id", patientId);
          }
        } else {
          visitDetailsBean.set("op_type", "M");
          visitDetailsBean.set("main_visit_id", patientId);
        }

        if (request.getParameter("screenId").equals("out_pat_reg")) {
          visitDetailsBean.set("visit_type", "o");
          visitDetailsBean.set("bed_type", "GENERAL");
          visitDetailsBean.set("op_type", "O");
          visitDetailsBean.set("main_visit_id", patientId);
        }

        /* Set the plan & plan type */
        visitDetailsBean.set("plan_id", planId);
        visitDetailsBean.set("category_id", categoryId);
        visitDetailsBean.set("prior_auth_id", priorAuthId);
        visitDetailsBean.set("prior_auth_mode_id", priorAuthModeId);

        /* Set established type */
        if (regType.equals("regd")) {
          Boolean mrkAsEstblshd = VisitDetailsDAO.checkMarkAsEstablished(con, visitDetailsBean);
          if (mrkAsEstblshd)
            visitDetailsBean.set("established_type", "E");
        }

        List<BasicDynaBean> episodePreviousVisitDetails = VisitDetailsDAO
            .getPreviousEpisodeVisitDetails(mainVisitId);
        String latestEpisodeVisitId = null;

        if (episodePreviousVisitDetails != null && episodePreviousVisitDetails.size() > 0) {
          for (BasicDynaBean eb : episodePreviousVisitDetails) {
            latestEpisodeVisitId = (String) eb.get("patient_id");
            break;
          }
        }

        /* Selecting the base doctor. */
        String doctorId = request.getParameter("doctor");
        String doctorCharge = request.getParameter("doctorCharge");
        String consDate = request.getParameter("consDate");
        String consTime = request.getParameter("consTime");
        String schDocAppointRemarks = "Scheduler Consultation";
        String consRemarks = ((appointmentId > 0) ? schDocAppointRemarks : "")
            + request.getParameter("consRemarks");

        BasicDynaBean baseDocBean = null;
        if (visitType.equals("opreg")) {
          baseDocBean = regBO.regBaseDoctor(visitDetailsBean, doctorId, doctorCharge, consDate,
              consTime, consRemarks, appointmentId);
        }

        /* Copy diagnosis codes from previous visit(same episode) to episode follow-up */
        success = mrdDiagDao.copyDiagCodes(con, visitDetailsBean, latestEpisodeVisitId, userName);
        if (!success)
          break validate;

        /* Referal Doctor. */
        String referredBy = request.getParameter("referred_by");
        if (referredBy != null && !referredBy.equals("")) {
          visitDetailsBean.set("reference_docto_id", referredBy);
        }
        String visitClassification = request.getParameter("visitClassification");
        if (StringUtils.isNotEmpty(visitClassification)) {
          visitDetailsBean.set("classification", visitClassification);
        }

        if (patientDetailsBean.get("patient_address") == null) {
          patientDetailsBean.set("patient_address", "");
        }

        /* Area changes. */
        if (patientDetailsBean.get("patient_area") != null
            && !patientDetailsBean.get("patient_area").toString().equals("")) {
          success = areaMasterDao.checkAreaAndInsert(
              patientDetailsBean.get("patient_city").toString(),
              patientDetailsBean.get("patient_area").toString());
        } else
          patientDetailsBean.set("patient_area", null);

        if (!success)
          break validate;

        /* Patient policy details. */

        if (secondarySponsor != null && !secondarySponsor.equals("")
            && secondarySponsor.equals("I")) {
          secondaryInsBean.set("mr_no", mrNo);
          secondaryInsBean.set("patient_id", patientId);

          if (secPlanId > 0) {
            // success = regBO.regPolicyDetails(con, secondaryInsBean, regType,
            // (Integer)secondaryInsBean.get("plan_id"), secPolicyDetailsMap,secSponsorTypeBean);
            success = regBO.regPolicyDetailsN(con, secondaryInsBean, regType,
                (Integer) secondaryInsBean.get("plan_id"), secPolicyDetailsMap);
            if (!success)
              break validate;

          }
        }

        if (primarySponsor != null && !primarySponsor.equals("") && primarySponsor.equals("I")) {
          primaryInsuranceBean.set("mr_no", mrNo);
          primaryInsuranceBean.set("patient_id", patientId);

          if (planId > 0) {
            // success = regBO.regPolicyDetails(con, primaryInsuranceBean, regType,
            // (Integer)primaryInsuranceBean.get("plan_id"), policyDetailsMap,priSponsorTypeBean);
            success = regBO.regPolicyDetailsN(con, primaryInsuranceBean, regType,
                (Integer) primaryInsuranceBean.get("plan_id"), policyDetailsMap);
            if (!success)
              break validate;
          }
        }

        /* Corporate details. */
        if (corporateDetailsMap.get("sponsor_id") != null
            && !((String) corporateDetailsMap.get("sponsor_id")).equals("")) {
          success = regBO.regCorporateDetails(con, visitDetailsBean, corporateDetailsMap, true);
          if (!success)
            break validate;
        }

        if (secCorporateDetailsMap.get("sponsor_id") != null
            && !((String) secCorporateDetailsMap.get("sponsor_id")).equals("")) {
          success = regBO.regCorporateDetails(con, visitDetailsBean, secCorporateDetailsMap, false);
          if (!success)
            break validate;
        }

        /* National details. */
        if (nationalDetailsMap.get("sponsor_id") != null
            && !((String) nationalDetailsMap.get("sponsor_id")).equals("")) {
          success = regBO.regNationalDetails(con, visitDetailsBean, nationalDetailsMap, true);
          if (!success)
            break validate;
        }

        if (secNationalDetailsMap.get("sponsor_id") != null
            && !((String) secNationalDetailsMap.get("sponsor_id")).equals("")) {
          success = regBO.regNationalDetails(con, visitDetailsBean, secNationalDetailsMap, false);
          if (!success)
            break validate;
        }

        /* Close previous visit and then create new visit */
        if (last_active_visit != null && !last_active_visit.equals("")) {
          HashMap mapDetails = new HashMap();
          mapDetails.put("mr_no", patientDetailsBean.get("mr_no").toString());
          mapDetails.put("visitId", last_active_visit);
          mapDetails.put("userName", userName);
          String err = EditVisitDetailsDAO.dischargePatientOrCloseVisit(con, mapDetails);
          if (err != null)
            success = false;
        }

        if (!success)
          break validate;

        if (registrationChargesApplicable.equals("N"))// marking reg_charge_accepted as "NO" when
                                                      // user unchecks Registration Chrges checkbox
                                                      // from Op Registration screen.
          visitDetailsBean.set("reg_charge_accepted", "N");

        /* Complaint */
        String complaintName = request.getParameter("ailment");
        int complaintIdInt = 0;
        if (!request.getParameter("screenId").equals("out_pat_reg")) {
          visitDetailsBean.set("complaint", complaintName);
        }

        // Get the country code for the center and populate in Patient Details object
        String patientContact = (String) patientDetailsBean.get("patient_phone");
        if (patientContact != null && !patientContact.isEmpty()) {
          String defaultCode = null;
          try {
            defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
          } catch (NullPointerException e) {
            ValidationErrorMap errorMap = new ValidationErrorMap();
            errorMap.addError("center_id", "exception.scheduler.override.invalid.center_id");
            ValidationException ex = new ValidationException(errorMap);
            Map<String, Object> nestedException = new HashMap<String, Object>();
            nestedException.put("appointment", ex.getErrors());
            throw new NestableValidationException(nestedException);
          }
          if (defaultCode == null) {
            defaultCode = centerMasterDAO.getCountryCode(0);
          }
          String patientCareOfText = String.valueOf(patientDetailsBean.get("patient_care_oftext")
              == null ? "" : patientDetailsBean.get("patient_care_oftext"));
          List<String> countryCodeAndNumber = PhoneNumberUtil.getCountryCodeAndNationalPart(
              patientCareOfText, null);
          if (countryCodeAndNumber != null && !countryCodeAndNumber.isEmpty() && !countryCodeAndNumber.get(0).isEmpty()) {
            patientDetailsBean.set("patient_care_oftext", "+"
                + countryCodeAndNumber.get(0)
                + countryCodeAndNumber.get(1));
            visitDetailsBean.set("patient_care_oftext", "+"
                + countryCodeAndNumber.get(0)
                + countryCodeAndNumber.get(1));
          } else if (defaultCode != null && patientCareOfText != null && !patientCareOfText.equals("")) {
            patientDetailsBean.set("patient_care_oftext", "+"
                + defaultCode
                + patientCareOfText);
            visitDetailsBean.set("patient_care_oftext", "+"
                + defaultCode
                + patientCareOfText);

          }
          List<String> parts = PhoneNumberUtil.getCountryCodeAndNationalPart(patientContact, null);
          if (parts != null && !parts.isEmpty() && !parts.get(0).isEmpty()) {
            // appt.setPhoneCountryCode("+" + parts.get(0));
            patientDetailsBean.set("patient_phone_country_code", "+"
                + parts.get(0));
            patientDetailsBean.set("patient_phone", "+"
                + parts.get(0)
                + parts.get(1));
          } else if (defaultCode != null) {
            // appt.setPhoneCountryCode("+" + defaultCode);
            patientDetailsBean.set("patient_phone_country_code", "+"
                + defaultCode);
            if (patientContact != null && !patientContact.equals("") && !patientContact.startsWith("+")) {
              // appt.setPhoneNo("+" + defaultCode + patientContact);
              patientDetailsBean.set("patient_phone", "+"
                  + defaultCode
                  + patientContact);
            }
          }
          if (((String) patientDetailsBean.get("patient_phone")).length() > 16) {
            ValidationErrorMap errorMap = new ValidationErrorMap();
            errorMap.addError("patient_contact",
                "exception.scheduler.patient.invalid.phoneno.long");
            ValidationException ex = new ValidationException(errorMap);
            Map<String, Object> nestedException = new HashMap<String, Object>();
            nestedException.put("patient", ex.getErrors());
            throw new NestableValidationException(nestedException);
          }
        }

        /* Insert or update the patientdetails and visitdetails. */
        if (regType.equals("new")) {
          try {
          success = patientDetailsDao.insert(con, patientDetailsBean)
              && rdao.insert(con, visitDetailsBean);
          } catch (Exception ex) {
        	  log.error("{}", ex);
        	  success = false;
          }
          if (primarySponsor != null && !primarySponsor.equals("") && primarySponsor.equals("I")) {
        	try {
        	  success &= planDAO.insert(con, primaryInsuranceBean);
        	} catch (Exception ex) {
        	  log.error("{}", ex);
        	  success = false;
        	}
            patPrimaryInsuranceDetailsBeanList = getPrimaryInsuranceDetails(request, patientId,
                (String) visitDetailsBean.get("visit_type"), primaryInsuranceBean);
            if (null != patPrimaryInsuranceDetailsBeanList
                && patPrimaryInsuranceDetailsBeanList.size() > 0) {
              try {
            	success &= patPlanDetailsDAO.insertAll(con, patPrimaryInsuranceDetailsBeanList);
  	      	  } catch (Exception ex) {
	      	    log.error("{}", ex);
	      	    success = false;
	      	  }
            }
          }
          if (secondarySponsor != null && !secondarySponsor.equals("")
              && secondarySponsor.equals("I")) {
          	try {
              success &= planDAO.insert(con, secondaryInsBean);
        	} catch (Exception ex) {
          	  log.error("{}", ex);
          	  success = false;
          	}
            patSecondaryInsuranceDetailsBeanList = getSecondaryInsuranceDetails(request, patientId,
                (String) visitDetailsBean.get("visit_type"), secondaryInsBean);
            if (null != patSecondaryInsuranceDetailsBeanList
                && patSecondaryInsuranceDetailsBeanList.size() > 0) {
              try {
            	success &= patPlanDetailsDAO.insertAll(con, patSecondaryInsuranceDetailsBeanList);
              } catch (Exception ex) {
          	    log.error("{}", ex);
          	    success = false;
          	  }
            }
          }
          if (!success) {
            String msg = getResources(request)
                .getMessage("registration.patient.action.message.error.while.creating.visit");
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            flash.error(msg);
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }

        } else if (regType.equals("regd")) {
          Map<String, String> keys = new HashMap<String, String>();
          keys.put("mr_no", patientDetailsBean.get("mr_no").toString());
          try {
        	  success = (patientDetailsDao.update(con, patientDetailsBean.getMap(), keys)) > 0;

	          if (success) {
	            success = rdao.insert(con, visitDetailsBean);
	          }
	      } catch (Exception ex) {
      	      log.error("{}", ex);
      	      success = false;
	      }
          if (primarySponsor != null && !primarySponsor.isEmpty() && primarySponsor.equals("I")) {
            try {
        	  success &= planDAO.insert(con, primaryInsuranceBean);
  	      	} catch (Exception ex) {
      	      log.error("{}", ex);
      	      success = false;
      	    }
            patPrimaryInsuranceDetailsBeanList = getPrimaryInsuranceDetails(request, patientId,
                (String) visitDetailsBean.get("visit_type"), primaryInsuranceBean);
            if (null != patPrimaryInsuranceDetailsBeanList
                && patPrimaryInsuranceDetailsBeanList.size() > 0) {
                try {
            	  success &= patPlanDetailsDAO.insertAll(con, patPrimaryInsuranceDetailsBeanList);
      	      	} catch (Exception ex) {
            	  log.error("{}", ex);
            	  success = false;
            	}
            }
          }
          if (secondarySponsor != null && !secondarySponsor.isEmpty()
              && secondarySponsor.equals("I")) {
        	  try {
        		  success &= planDAO.insert(con, secondaryInsBean);
        	  } catch (Exception ex) {
            	  log.error("{}", ex);
            	  success = false;
              }
            patSecondaryInsuranceDetailsBeanList = getSecondaryInsuranceDetails(request, patientId,
                (String) visitDetailsBean.get("visit_type"), secondaryInsBean);
            if (null != patSecondaryInsuranceDetailsBeanList
                && patSecondaryInsuranceDetailsBeanList.size() > 0) {
                try {
                	success &= patPlanDetailsDAO.insertAll(con, patSecondaryInsuranceDetailsBeanList);
                } catch (Exception ex) {
              	  log.error("{}", ex);
              	  success = false;
                }
            }
          }

          if (!success) {
            String msg = getResources(request)
                .getMessage("registration.patient.action.message.error.while.creating.visit");
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            flash.error(msg);
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
        }

        if (!success) {
          break validate;
        }

        /* Copy other complaints from previous visit(same episode) to episode follow-up */
        success = secComplaintDAO.copyComplaints(con, visitDetailsBean, latestEpisodeVisitId,
            userName);
        if (!success) {
          break validate;
        }

        /* Insert or update Case file Department based on Raise indent */
        if (caseFileNo != null && !caseFileNo.equals("")) {
          success = regBO.regCaseFile(con, patientDetailsBean,
              (String) visitDetailsBean.get("dept_name"), userName, raiseIndent);
          if (!success) {
             break validate;
          }
        }

        // Bug # 22408: Depending on bill now tpa allowed pref., need to make bill now bill
        // insured.
        boolean allowBillInsurance = BillDAO
            .isBillInsuranceAllowed(request.getParameter("bill_type"), is_tpa);

        String billType = request.getParameter("bill_type");
        Bill bill = new Bill();

        /* Generate bill */
        if (!request.getParameter("screenId").equals("out_pat_reg")) {

          if (regBO.checkToCreatePrepaidBill(regAndBill, is_tpa, estimatedAmt, billType)) {

            BigDecimal billDeduction = (request.getParameter("billDeduction") != null
                && !request.getParameter("billDeduction").equals("")) ? new BigDecimal("")
                    : BigDecimal.ZERO;

            try {
            	success = regBO.regBill(con, patientId, patientType, userName, bill, billType,
                    allowBillInsurance, primaryInsuranceApproval, secondaryInsuranceApproval,
                    billDeduction, orgId, request.getParameter("insurance_discount_plan"));
	      	} catch (Exception ex) {
      	      log.error("{}", ex);
      	      success = false;
	      	}
            if (!success) {
                break validate;
             }

            billNo = bill.getBillNo();
            if (uhid != null && !uhid.isEmpty()) {
              visitDetailsBean.set("reg_charge_accepted", "N");
            }
            Boolean noGenRegCharge = Arrays.asList(regPrefs.getNo_reg_charge_sources().split(","))
                .contains(patientDetailsBean.get("resource_captured_from"));
            
            try {
	            success = regBO.insertRegistrationCharges(con, bill, visitDetailsBean, planIds,
	                isRenewal, registrationChargesApplicable, preAuthIds, preAuthModeIds,
	                allowBillInsurance, noGenRegCharge);
  	      	} catch (Exception ex) {
        	      log.error("{}", ex);
        	      success = false;
    	    }
            if (!success) {
                break validate;
             }
          }

        }
        /* Diagnosis changes */
        if (groups.containsKey("mod_mrd_icd") && "Y".equals(groups.get("mod_mrd_icd"))
            && request.getParameter("screenId").equals("out_pat_reg")) {
          success = regBO.regOutSidePatientDiagnosis(con, requestParams, userName, patientId);
          if (!success) {
              break validate;
           }
        }

        /*
         * Order tests/service/consultations (include packages), update the prescription saying it
         * has been fulfilled.
         */

        if (request.getParameter("screenId").equals("new_op_registration")) {
          success = regBO.regOrders(con, requestParams);
          if (!success)
            break validate;
        }

        /* Update the appointment */
        int consultationTypeId = 0;
        if (apptBean != null && apptBean.get("consultation_type_id") != null) {
          consultationTypeId = (Integer) apptBean.get("consultation_type_id");
        }
        String presDocId = request.getParameter("presDocId");
        if (appointmentId > 0) {
          success = ResourceBO.updateScheduler(con, appointmentId, mrNo, patientId,
              patientDetailsBean, complaintName, userName, consultationTypeId, null, presDocId,
              "Reg");
          schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_ARRIVED);
          if (!success)
            break validate;
        }

        /* Generate registration documents */
        success = regDocumentImpl.autoGenerateRegDocuments(con, patientId, mrNo, visitType,
            userName);

        if (!success)
          break validate;

        /* Scheduler Appointment */
        ResourceDAO resdao = new ResourceDAO(con);
        if (appointmentId > 0) {

          if (apptBean != null && apptBean.get("appointment_status").equals("Arrived")) {
            String msg = getResources(request)
                .getMessage("registration.patient.action.message.error.patinet.already.arrived");
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            flash.error(msg);
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
        }

        BasicDynaBean OperationBean = null;
        boolean conduction = false;
        if (category.equals("OPE") && appointmentId > 0) {
          if (modAdvanceOTActive.equals("Y")) {
          } else
            OperationBean = regBO.regOperationOrder(con, visitDetailsBean, apptBean, visitType,
                category, appointmentId);
        }

        BasicDynaBean planBean = null;
        if (planId > 0) {
          planBean = planMasterDao.findByKey("plan_id", planId);
        }

        /* Orders */
        if (!request.getParameter("screenId").equals("out_pat_reg")) {
          boolean chargeable = true;
          if (billNo != null) {
            orderBo.setBillInfo(con, patientId, billNo, is_tpa, userName);
          } else {
            // no bill created, set patient info and make it non-chargeable.
            orderBo.setPatientInfo(patientId, mrNo, userName);
            chargeable = false;
          }

          List orders = new ArrayList();
          LinkedList newPreAuths = new LinkedList();
          LinkedList newSecPreAuths = new LinkedList();
          LinkedList<Integer> newPreAuthModes = new LinkedList<Integer>();
          LinkedList<Integer> newSecPreAuthModes = new LinkedList<Integer>();
          List firstOfCategoryList = new ArrayList();
          List<String> condDoctrsList = new ArrayList<String>();
          List<Map<String, Object>> operAnaesTypeList = new ArrayList<Map<String, Object>>();
          List<Boolean> multiVisitPackageList = new ArrayList<Boolean>();
          List<List<TestDocumentDTO>> testAdditionalDocList = new ArrayList<List<TestDocumentDTO>>();

          BasicDynaBean newMultiVisitPackageOrdersBean = OrderAction
              .getNewMultiVisitPackageOrders(request);

          if (appointmentId > 0 && null != category && !category.equals("")
              && !category.equals("DOC"))
            conduction = ResourceDAO.getConductionForTestOrServiceOrOperation(category,
                (String) apptBean.get("res_sch_name"));

          OrderAction.getNewOrderBeans(requestParams, orders, newPreAuths, newPreAuthModes,
              firstOfCategoryList, condDoctrsList, multiVisitPackageList, errors, newSecPreAuths,
              newSecPreAuthModes, operAnaesTypeList, testAdditionalDocList, form);

          if (baseDocBean != null) {
            orders.add(baseDocBean);
            newPreAuths.add("");
            newSecPreAuths.add("");
            newPreAuthModes.add(1);
            newSecPreAuthModes.add(1);
            firstOfCategoryList.add(request.getParameter("regDocFirstOfCategory"));
            condDoctrsList.add("");
            multiVisitPackageList.add(false);
          }

          if (planBean != null) {
            orders.add(planBean);
            newPreAuths.add("");
            newSecPreAuths.add("");
            newPreAuthModes.add(1);
            newSecPreAuthModes.add(1);
            condDoctrsList.add("");
            multiVisitPackageList.add(false);
          }

          if (OperationBean != null && scheduleName != null) {
            orders.add(OperationBean);
            newPreAuths
                .add((preAuthIds[0] != null && !preAuthIds[0].equals("")) ? preAuthIds[0] : "");
            newSecPreAuths
                .add((preAuthIds[1] != null && !preAuthIds[1].equals("")) ? preAuthIds[1] : "");
            newPreAuthModes
                .add(null != preAuthModeIds[0] && preAuthModeIds[0] != 0 ? preAuthModeIds[0] : 1);
            newSecPreAuthModes
                .add(null != preAuthModeIds[1] && preAuthModeIds[1] != 0 ? preAuthModeIds[1] : 1);
            condDoctrsList.add("");
            multiVisitPackageList.add(false);
          }

          priorAuthModeId = (priorAuthModeId == 0) ? 1 : priorAuthModeId;
          if (null != preAuthModeIds && null != preAuthModeIds[0])
            preAuthModeIds[0] = (preAuthModeIds[0] == 0) ? 1 : preAuthModeIds[0];

          if (null != preAuthIds && null != preAuthModeIds) {
            for (int i = 0; i < orders.size(); i++) {
              BasicDynaBean b = (BasicDynaBean) orders.get(i);
              DynaClass c = b.getDynaClass();
              if (c.getName().equals("tests_prescribed")) {
                if (newPreAuths.get(i) == null || newPreAuths.get(i).equals("")) {
                  newPreAuths.remove(i);
                  newPreAuths.add(i, preAuthIds[0]);
                }
                if (newSecPreAuths.get(i) == null || newSecPreAuths.get(i).equals("")) {
                  newSecPreAuths.remove(i);
                  newSecPreAuths.add(i, preAuthIds[1]);
                }

                if (newPreAuthModes.get(i) == null || newPreAuthModes.get(i).equals("")) {
                  newPreAuthModes.remove(i);
                  newPreAuthModes.add(i, preAuthModeIds[0]);
                }

                if (newSecPreAuthModes.get(i) == null || newSecPreAuthModes.get(i).equals("")) {
                  newSecPreAuthModes.remove(i);
                  newSecPreAuthModes.add(i, preAuthModeIds[1]);
                }

              } else if (c.getName().equals("services_prescribed")) {
                if (newPreAuths.get(i) == null || newPreAuths.get(i).equals("")) {
                  newPreAuths.remove(i);
                  newPreAuths.add(i, preAuthIds[0]);
                }
                if (newSecPreAuths.get(i) == null || newSecPreAuths.get(i).equals("")) {
                  newSecPreAuths.remove(i);
                  newSecPreAuths.add(i, preAuthIds[1]);
                }

                if (newPreAuthModes.get(i) == null || newPreAuthModes.get(i).equals("")) {
                  newPreAuthModes.remove(i);
                  newPreAuthModes.add(i, preAuthModeIds[0]);
                }

                if (newSecPreAuthModes.get(i) == null || newSecPreAuthModes.get(i).equals("")) {
                  newSecPreAuthModes.remove(i);
                  newSecPreAuthModes.add(i, preAuthModeIds[1]);
                }
              }
            }
          }
          Integer pat_package_id = -1;
          if (newMultiVisitPackageOrdersBean != null) {
            String patPackIdStr = request.getParameter("pat_package_id");
            if (patPackIdStr == null || (patPackIdStr != null && !patPackIdStr.equals(""))) {
              try {
                pat_package_id = Integer.parseInt(request.getParameter("pat_package_id"));
              } catch (NumberFormatException e) {
                pat_package_id = -1;
              }
            }
            if (pat_package_id != -1) {
              success = orderBo.orderMultiVisitPackageForChannelling(con,
                  newMultiVisitPackageOrdersBean, pat_package_id) == null;
            } else {
              success = orderBo.orderMultiVisitPackage(con, newMultiVisitPackageOrdersBean) == null;
            }
          }

          if (!success)
            break validate;
          if (pat_package_id != -1) {
            success = (orderBo.orderItems(con, orders, newPreAuths, newPreAuthModes,
                firstOfCategoryList, condDoctrsList, multiVisitPackageList, null, null, null,
                appointmentId, chargeable, true, true, newSecPreAuths, newSecPreAuthModes,
                operAnaesTypeList, testAdditionalDocList) == null);
          } else {
            success = (orderBo.orderItems(con, orders, newPreAuths, newPreAuthModes,
                firstOfCategoryList, condDoctrsList, multiVisitPackageList, null, null, null,
                appointmentId, chargeable, true, newSecPreAuths, newSecPreAuthModes,
                operAnaesTypeList, testAdditionalDocList) == null);
          }

          if (!success)
            break validate;

          if (null != newMultiVisitPackageOrdersBean) {
            if (pat_package_id != -1) {
              success = orderBo.updateChannellingMultivisitPackageStatus(con,
                  newMultiVisitPackageOrdersBean, pat_package_id) == null;
            } else {
              success = orderBo.updateMultivisitPackageStatus(con,
                  newMultiVisitPackageOrdersBean) == null;
            }
          }

          if (!success)
            break validate;

          if (appointmentId > 0) {
            Map columndata = new HashMap();
            columndata.put("scheduler_prior_auth_no", priorAuthId);
            columndata.put("scheduler_prior_auth_mode_id", priorAuthModeId);
            Map keys = new HashMap();
            keys.put("appointment_id", appointmentId);
            success = schedulerAppointmentsDao.update(con, columndata, keys) > 0;

            // Incaseof scheduler_generate_order is set to N, Then the category value is not
            // available, Since we block the
            // most of the tokens from scheduler.
            if ((category.equals("OPE") || (GenericPreferencesDAO.getGenericPreferences()
                .getSchedulerGenerateOrder().equals("N") && schedulerCategory.equals("OPE")))
                && modAdvanceOTActive.equals("Y")) {
              errorMsg = operationDetailsBo.saveSurgeryAppointmnetToOpertionDetails(con,
                  appointmentId, orgId);
              if (errorMsg != null) {
                errorMsg = getResources(request).getMessage(errorMsg);
                flash.error(errorMsg);
                redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
                redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
                return redirect;
              } else {
                success = true;
              }
            }
            if (!success)
              break validate;
          }

          // Need to do null check for the category, if the generate_scheduler_order preference is
          // set to NO

          if (modAdvanceOTActive.equals("Y") && null != category && !category.equals("")
              && category.equals("OPE") && appointmentId > 0) {
            success = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId, userName);
            schedulePushEvent(String.valueOf(appointmentId),Events.APPOINTMENT_ARRIVED);
            if (!success)
              break validate;
          } else if (!conduction && appointmentId > 0 && null != category && !category.equals("")
              && !category.equals("DOC")) {
            if (scheduleName == null) {
              success = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,
                  userName);
              schedulePushEvent(String.valueOf(appointmentId),Events.APPOINTMENT_ARRIVED);
              if (!success)
                break validate;
            } else {
              success = ResourceBO.updateTestOrServiceOrOperationStatus(appointmentId, category);
              if (!success)
                break validate;

              success = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,
                  userName);
              schedulePushEvent(String.valueOf(appointmentId),Events.APPOINTMENT_COMPLETED);
              if (!success)
                break validate;
            }
          }
          if (GenericPreferencesDAO.getGenericPreferences().getSchedulerGenerateOrder().equals("N")
              && category == null && category.equals("")) {
            success = resdao.updateStatus(ResourceDTO.APPT_COMPLETED_STATUS, appointmentId,
                userName);
            schedulePushEvent(String.valueOf(appointmentId),Events.APPOINTMENT_COMPLETED);
            if (!success)
              break validate;
          }

          List<PackageOrderDTO> newPackageOrders = OrderAction.getNewPackageOrders(requestParams,
              form);
          success = orderBo.orderPackages(con, newPackageOrders, chargeable) == null;

          if (!success)
            break validate;

        }
        /* Update visit care for ip patient------------------------------ */
        if (request.getParameter("screenId").equals("ip_registration")
            && !visitDetailsBean.get("doctor").equals("")) {
          success = ipcareDAO.insertVisitCareDeatils(con, visitDetailsBean);
        }

        /* Allocating bed from registration */
        if (request.getParameter("screenId").equals("ip_registration")
            && request.getParameter("bill_type").equals("C")) {

          BasicDynaBean ipBedBean = ipBedDetailsDao.getBean();
          ActionForward bedErrors = copyToDynaBean(request, response, ipBedBean);
          if (bedErrors != null) {
            success = false;
            break validate;
          }

          BasicDynaBean admBean = admissionDao.getBean();
          bedErrors = copyToDynaBean(request, response, admBean);
          if (bedErrors != null) {
            success = false;
            break validate;
          }

          String bedType = request.getParameter("bed_type");
          String dayCare = request.getParameter("daycare_status");

          success = regBO.regIPBedAllocation(con, ipBedBean, admBean, userName, dayCare, bedType,
              mrNo, patientId, billNo, is_tpa);

          if (!success)
            break validate;
        }

        if (appointmentId > 0 && null != category && !category.equals("")) {
          if (!category.equals("SNP") && !category.equals("OPE")) {
            success = ResourceBO.updateAppointmentStatus(con, category, appointmentId, userName);
          } else {
            if (scheduleName != null) {
              success = ResourceBO.updateAppointmentStatus(con, category, appointmentId, userName);
            } else {
              success = resdao.updateStatus(ResourceDTO.APPT_ARRIVED_STATUS, appointmentId,
                  userName);
              schedulePushEvent(String.valueOf(appointmentId), Events.APPOINTMENT_ARRIVED);
              if (!success)
                break validate;
            }
          }
          if (!success) {
            log.error("Appointment updation failed...");
            break validate;
          }
        }

        /* MLC template. */
        if (mlcSelected.equals("Y")) {
          Map map = new HashMap();
          map.put("mr_no", mrNo);
          map.put("patient_id", patientId);
          map.put("doc_name", request.getParameter("mlc_template_name"));
          map.put("username", request.getSession(false).getAttribute("userid"));
          map.put("mlc_template_id", request.getParameter("mlc_template_id"));
          success = mlcDocImpl.create(map, con);
          if (!success)
            break validate;
        }

        String specializedDocType = mapping.getProperty("documentType");
        Boolean specialized = new Boolean(mapping.getProperty("specialized"));

        FormFile priInsDocBytea = regForm.getPrimary_insurance_doc_content_bytea1();
        FormFile priCorpDocBytea = regForm.getPrimary_corporate_doc_content_bytea1();
        FormFile priNatDocBytea = regForm.getPrimary_national_doc_content_bytea1();

        FormFile secInsDocBytea = regForm.getSecondary_insurance_doc_content_bytea1();
        FormFile secCorpDocBytea = regForm.getSecondary_corporate_doc_content_bytea1();
        FormFile secNatDocBytea = regForm.getSecondary_national_doc_content_bytea1();

        FormFile primarySponsorPastedPhoto = regForm.getPrimary_sponsor_pastedPhoto();

        // Upload scanned primary document
        success = regBO.regPrimarySponsorDocs(con, visitDetailsBean, requestParams, priInsDocBytea,
            priCorpDocBytea, priNatDocBytea, primarySponsorPastedPhoto, specializedDocType,
            specialized, userName);
        if (!success)
          break validate;

        // Upload scanned secondary document
        success = regBO.regSecondarySponsorDocs(con, visitDetailsBean, requestParams,
            secInsDocBytea, secCorpDocBytea, secNatDocBytea, primarySponsorPastedPhoto,
            specializedDocType, specialized, userName);
        if (!success)
          break validate;

        // Upload other documents
        if (requestParams.get("doc_name") != null
            && ((String[]) requestParams.get("doc_name")).length > 0) {

          success = regBO.regUploadDocs(con, visitDetailsBean, requestParams, regForm,
              specializedDocType, specialized, userName);
          if (!success)
            break validate;
        }

        /**
         * Last Action to be done while registration (when bill is to be closed for reward points
         * earning)
         */
        // Update bill status and payment status after all the bill charges are inserted.
        success = regBO.updateBillStatus(con, regAndBill, bill, doc_eandm_codification_required);
        if (!success)
          break validate;

        // Insert presenting complaint for consultation if DHA E-claim
        if (baseDocBean != null && complaintIdInt != 0) {
          int consultation_id = doctorConsultationDao.getAdmittingDoctorConsultationId(con,
              patientId);
          ChargeDTO chargeDTO = new BillActivityChargeDAO(con).getCharge("DOC", consultation_id);
          if (chargeDTO != null) {
            String chargeId = chargeDTO.getChargeId();

            String chiefComplaint = mrdUpdateScreenDao.getChiefComplaint(con, patientId);
            chiefComplaint = (chiefComplaint != null && !chiefComplaint.trim().equals("")
                ? "Chief Complaint :- "
                    + chiefComplaint
                : "");
            success = mrdUpdateScreenDao.insertPresentingComplaint(con, chargeId, chiefComplaint);
            if (!success)
              break validate;
          }
        }

        if (allowMultipleActiveVisits.equals("N")
            && !request.getParameter("screenId").equals("out_pat_reg")) {
          String visitId = VisitDetailsDAO.getPatientLatestVisitId(mrNo, true, "i");
          if (visitId != null && !visitId.equals("")) {
            String msg = getResources(request)
                .getMessage("registration.patient.action.message.error.active.visit.exists");
            redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
            flash.error(msg);
            redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
            return redirect;
          }
        }

        // Inserting Admission Request Related Stuff....

        String admissionRequestId = request.getParameter("adm_request_id");
        if (request.getParameter("screenId").equals("ip_registration") && admissionRequestId != null
            && !admissionRequestId.isEmpty()) {
          allSuccess = admissionRequestBo.insertAdmissionRequestDetails(con,
              Integer.parseInt(admissionRequestId), patientId, originalAdmUserName, mrNo);
        }

        allSuccess = true;

      } // validate

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);

      if (allSuccess) {
    	interfaceEventMappingService.visitRegistrationEvent(patientId, regType.equals("new"));

        /* Update contact preferences for ip patient */
        PatientDetailsDAO.updateContactPreference(mrNo,
            getValue("preferredLanguage", requestParams), communication);

        // New bill's deduction and claim amount should be reset newly.
        if (billNo != null && !billNo.equals("")) {
          BillDAO.setDeductionAndSponsorClaimTotals(billNo);
          BillDAO.updateIsSystemDiscountToYes(billNo);
        }
        if (appointmentId != 0 && PractoBookHelper.isPractoAdvantageEnabled()) {
          PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
        }
      }

      if (regAndBill.equals("Y")
          && MessageUtil.allowMessageNotification(request, "general_message_send")) {
        String referredBy = request.getParameter("referred_by");
        String doctorId = request.getParameter("doctor");
        Map<String, String> smsBillData = new HashMap<String, String>();
        try {
          smsBillData = populateTokenMap(patientDetailsBean, centerId, referredBy, doctorId);

          String messageFooterToken = "SELECT message_footer from message_types WHERE message_type_id = 'sms_bill_payment_received'";
          String messageFooterTokenvalue = DataBaseUtil.getStringValueFromDb(messageFooterToken);
          smsBillData.put("message_footer", messageFooterTokenvalue);
          smsBillData.put("bill_no", billNo);
          if (primarySponsor != null) {
            if (primarySponsor.equals("")) {
              smsBillData.put("amount_paid", estimateAmtStr);
            } else {
              smsBillData.put("amount_paid", request.getParameter("patientAmt"));
            }
          } else {
            smsBillData.put("amount_paid", estimateAmtStr);
          }
          smsBillData.put("total_amount", estimateAmtStr);
          smsBillData.put("patient_due", "0.00");
          smsBillData.put("lang_code",
              PatientDetailsDAO.getContactPreference(smsBillData.get("mr_no")));
          String patientMobileNo = smsBillData.get("recipient_phone");
          if (estimatedAmt.compareTo(BigDecimal.ZERO) > 0) {
            smsBillData.put("recipient_mobile", patientMobileNo.trim());
            messageManager.processEvent("bill_payment_message", smsBillData);
          } else {
            log.info("Patient mobile # not available, skipping SMS on admission");
          }
        } catch (Exception e) {
          log.error("Error while trying to send Payment Received SMS : "
              + e.getMessage());
        }
      }

    }

    sponsorBo.recalculateSponsorAmount(patientId);

    try {

      // New Registration from IP Screen or Register using existing mr_no
      Map<String, Object> adtData = new HashMap<>();
      adtData.put("patient_id", (String) visitDetailsBean.get(Constants.PATIENT_ID));
      adtData.put("mr_no", (String) visitDetailsBean.get(Constants.MR_NO));
      ADTService adtService = (ADTService) ApplicationContextProvider.getApplicationContext()
          .getBean("adtService");
      adtService.createAndSendADTMessage("ADT_04", adtData);

      String mrNoChk = request.getParameter("mrno");
      // registration completed, send SMS to admiitting and referal doctors
      if (success && null != patientType) {
        if (MessageUtil.allowMessageNotification(request, "scheduler_message_send")) {
          if (patientType.equalsIgnoreCase("i")) { // IP registration
            log.debug("Admission successful : Sending SMS to admitting doctor and referal doctor");
            sendIPAdmissionSMS(patientId, mrNoChk);
          } else if (patientType.equalsIgnoreCase("o")) { // OP registration
            log.debug("Admission successful : Sending SMS to patient and patient party");
            sendOPAdmissionSMS(patientId, mrNoChk);
          }
        }
        if (request.getParameter("mobilePatAccess") != null
            && request.getParameter("mobilePatAccess").equals("Y")
            && MessageUtil.allowMessageNotification(request, "scheduler_message_send")) {
          Map patientData = new HashMap();
          patientData.put("mr_no", mrNoChk);
          patientData.put("patient_id", patientId);
          messageManager.processEvent("enable_mobile_access", patientData);
        }
      }
    } catch (Exception e) {
      log.error("Error while trying to send SMS : "
          + e.getMessage());
      // let the other things continue normally
    }
    /* Receipt generation while register and pay */
    String billRemarks = request.getParameter("billRemarks");
    String billType = request.getParameter("bill_type");
    String printType = request.getParameter("printType");
    String customTemplate = request.getParameter("printBill");
    BigDecimal patientAmt = (request.getParameter("patientAmt") != null
        && !request.getParameter("patientAmt").equals("0"))
            ? new BigDecimal(request.getParameter("patientAmt"))
            : BigDecimal.ZERO;

    if (regAndBill.equals("Y")
        && regBO.checkToCreatePrepaidBill(regAndBill, is_tpa, estimatedAmt, billType)) {
      String error = null;
      if (MessageUtil.allowMessageNotification(request, "general_message_send")) {
        Map<String, String> tokenMap = populateTokenMap(patientDetailsBean, centerId,
            request.getParameter("referred_by"), request.getParameter("doctor"));
        tokenMap.put("userId", (String) session.getAttribute("userid"));
        error = regBO.regReceipt(billNo, patientAmt, is_tpa, billRemarks, printType, customTemplate,
            doc_eandm_codification_required, tokenMap);
      } else {
        error = regBO.regReceipt(billNo, patientAmt, is_tpa, billRemarks, printType, customTemplate,
            doc_eandm_codification_required, null);
      }
      flash.error(error);
      redirect = new ActionRedirect(mapping.findForward("registBillRedirect"));
      redirect.addParameter("billNo", billNo);
      redirect.addParameter("patient_id", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    /* Registration success */
    if (success) {
      redirect = new ActionRedirect(mapping.findForward("registrationSuccessRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      redirect.addParameter("patient_id", patientId);
      redirect.addParameter("billNo", billNo);
      if (regAndBill.equals("E"))
        redirect.addParameter("patientBillPopUp", "Y");
      redirect.addParameter("referer", request.getParameter("referer"));

      Map<String, Object> requestParam = new HashMap<String, Object>();
      if(!StringUtils.isEmpty(admRequestId) && !StringUtils.isEmpty(mrNo)) {
        try {
          requestParam.put("mr_no", mrNo);
          requestParam.put("adm_request_id", Integer.parseInt(admRequestId));

              txn:
              con = DataBaseUtil.getConnection();
              con.setAutoCommit(false);
              {
                prescDAO.update(con, requestParam, "visit_id", patientId);
              }
        }catch (Exception e){
          log.error("Error occurred while saving admission request data ",e);
        } finally {
          DataBaseUtil.commitClose(con, success);
        }
      }

      return redirect;
    } else {
      String msg = getResources(request)
          .getMessage("registration.patient.action.message.failed.to.register.patient");
      flash.error(msg);
      redirect = new ActionRedirect(mapping.findForward("failureRedirect"));
      redirect.addParameter("mrno", mrNo);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
  }

  public ActionForward registrationSuccessForward(ActionMapping m, ActionForm f,
      HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException, Exception {

    String patId = req.getParameter("patient_id");
    BasicDynaBean patientDetails = VisitDetailsDAO
        .getPatientVisitDetailsBean(req.getParameter("patient_id"));
    req.setAttribute("patientvisitdetails", patientDetails);
    req.setAttribute("billNo", req.getParameter("billNo"));
    req.setAttribute("billBean", BillDAO.getBillBean(req.getParameter("billNo")));

    String tokenDBQuery = "SELECT consultation_token FROM doctor_consultation WHERE patient_id=?";
    String tokenAuthorizationDBQuery = "SELECT op_generate_token FROM registration_preferences";
    req.setAttribute("tokenNo", DataBaseUtil.getStringValueFromDb(tokenDBQuery, patId));
    req.setAttribute("tokenRights", DataBaseUtil.getStringValueFromDb(tokenAuthorizationDBQuery));
    req.setAttribute("regTemplates", GenericDocumentTemplateDAO.getTemplates(true, "SYS_RG", "A"));
    req.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    req.setAttribute("autoRegDocs", RegistrationBO.getAutoTemplates(patId));
    req.setAttribute("docs", VisitDetailsDAO.getDocsUpload());
    req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    req.setAttribute("referer", req.getParameter("referer"));

    return m.findForward("registrationsucces");
  }

  @IgnoreConfidentialFilters
  public ActionForward getBedChargeForOrganization(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {
    String bedtype = request.getParameter("bedtype");
    String orgid = request.getParameter("orgid");
    List bedcharges = null;
    int charge = 0;
    String isicubedtype = DataBaseUtil.getStringValueFromDb(
        "SELECT DISTINCT intensive_bed_type FROM icu_bed_charges WHERE intensive_bed_type=?",
        bedtype);
    if (isicubedtype == null) {
      bedcharges = bedMasterDao.getNormalBedCharge(bedtype, orgid);
    } else {
      bedcharges = bedMasterDao.getIcuBedcharges("GENERAL", bedtype, orgid);
    }
    if (bedcharges.size() > 0) {
      for (int i = 0; i < bedcharges.size(); i++) {
        Hashtable ht = (Hashtable) bedcharges.get(i);
        charge = charge + Integer.parseInt(ht.get("BED_CHARGE").toString());
        charge = charge + Integer.parseInt(ht.get("NURSING_CHARGE").toString());
        charge = charge + Integer.parseInt(ht.get("DUTY_CHARGE").toString());
        charge = charge + Integer.parseInt(ht.get("MAINTAINANCE_CHARGE").toString());
      }
    }
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(String.valueOf(charge));
    response.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getTimeStamp(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(diagDao.getCountFromDiagTimeStamp()));
    response.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward customScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
      HttpServletResponse res) throws Exception, IOException, ServletException {

    Map fields = new HashMap();
    Enumeration e = req.getParameterNames();
    while (e.hasMoreElements()) {
      String name = (String) e.nextElement();
      String value = req.getParameter(name);
      fields.put(name, value);
    }
    req.setAttribute("regFieldNamesAndValues", fields);
    req.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());

    return m.findForward("customScreen");
  }

  @IgnoreConfidentialFilters
  public ActionForward getPlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    String planId = request.getParameter("plan_id");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter()
        .write(js.serialize(PlanMasterDAO.getPlanDetails(Integer.parseInt(planId)).getMap()));
    response.flushBuffer();
    return null;
  }

  private void sendIPAdmissionSMS(String patientId, String mrNo)
      throws SQLException, ParseException, IOException {
    Map<String, String> admissionData = getIPAdmissionData(patientId);

    String refDocMobileNo = admissionData.get("referal_doctor_mobile");
    String admitDocMobileNo = admissionData.get("doctor_mobile");
    String patientMobileNo = admissionData.get("patient_phone");
    String patientPartyMobileNo = admissionData.get("next_of_kin_contact");
    String admAndRefDocMobileno = null;

    if (null != admitDocMobileNo && !admitDocMobileNo.trim().equals("")) {
      admAndRefDocMobileno = admitDocMobileNo.trim();
    }

    if (null != refDocMobileNo && !refDocMobileNo.trim().equals("")) {
      if (admAndRefDocMobileno == null) {// no mobile number for admitting doctor
        admAndRefDocMobileno = refDocMobileNo.trim();
      } else {
        admAndRefDocMobileno = admAndRefDocMobileno.concat(",").concat(refDocMobileNo.trim());
      }
    }
    // Send SMS to admitting doctor
    if (null != admAndRefDocMobileno && !admAndRefDocMobileno.trim().equals("")) {
      Map<String, String> admissionDataForDoctor = new HashMap<String, String>();
      admissionDataForDoctor.putAll(admissionData);
      admissionDataForDoctor.put("receipient_id__",
          admissionDataForDoctor.get("admitting_doctor_id__"));
      admissionDataForDoctor.put("receipient_type__", "DOCTOR");
      admissionDataForDoctor.put("recipient_mobile", admAndRefDocMobileno);
      messageManager.processEvent("patient_admitted", admissionDataForDoctor);
    } else {
      log.info("Admitting and Referral Doctor mobile # not available, skipping SMS on admission");
    }

    /*
     * // Send SMS to referal doctor if (null != refDocMobileNo &&
     * !refDocMobileNo.trim().equals("")) { admissionData.put("recipient_mobile",
     * refDocMobileNo.trim()); mgr.processEvent("patient_admitted", admissionData); } else {
     * log.info ("Referal Doctor mobile # not available, skipping SMS on admission"); }
     */

    sendPatientAddmissionSMS(mrNo, patientMobileNo, patientPartyMobileNo, admissionData,
        messageManager, "ip");
  }

  private void sendOPAdmissionSMS(String patientId, String mrNo)
      throws SQLException, ParseException, IOException {
    Map<String, String> admissionData = getOPAdmissionData(patientId);

    String patientMobileNo = admissionData.get("patient_phone");
    String patientPartyMobileNo = admissionData.get("next_of_kin_contact");

    sendPatientAddmissionSMS(mrNo, patientMobileNo, patientPartyMobileNo, admissionData,
        messageManager, "op");
  }

  private void sendPatientAddmissionSMS(String mrNo, String patientMobileNo,
      String patientPartyMobileNo, Map<String, String> admissionData, MessageManager mgr,
      String patientType) throws SQLException, ParseException, IOException {

    admissionData.put("lang_code",
        PatientDetailsDAO.getContactPreference((String) admissionData.get("mr_no")));
    if (null != mrNo && !mrNo.isEmpty()) { // patient already available, revise visit
      // Send Revise SMS to Patient
      admissionData.put("recipient_mobile", patientMobileNo);
      mgr.processEvent("patient_on_"
          + patientType
          + "_patient_revisit", admissionData);

      // Send Revise SMS to Patient Party
      if (null != patientPartyMobileNo && !patientPartyMobileNo.trim().equals("")) {
        admissionData.put("recipient_mobile", patientPartyMobileNo.trim());
        mgr.processEvent("family_on_"
            + patientType
            + "_patient_revisit", admissionData);
      } else {
        log.info("Patient Party mobile # not available, skipping SMS on admission");
      }
    } else { // new Patient Registration
      // Send SMS to Patient
      admissionData.put("recipient_mobile", patientMobileNo);
      mgr.processEvent("patient_on_"
          + patientType
          + "_patient_admission", admissionData);
      // Send SMS to Patient Party
      if (null != patientPartyMobileNo && !patientPartyMobileNo.trim().equals("")) {
        admissionData.put("recipient_mobile", patientPartyMobileNo.trim());
        mgr.processEvent("family_on_"
            + patientType
            + "_patient_admission", admissionData);
      } else {
        log.info("Patient Party mobile # not available, skipping SMS on admission");
      }
    }
  }

  private Map<String, String> getIPAdmissionData(String patientId) throws SQLException {
    BasicDynaBean bean = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    Map<String, String> admissionData = new HashMap<String, String>();

    String referalDoctorId = (String) bean.get("reference_docto_id");
    String referalDocMobile = "";
    // referal doctor mobile no is not available as part of patient_details_ext_view
    if (null != referalDoctorId && !referalDoctorId.trim().equals("")) {
      String referalDoctorQuery = "SELECT referrer_phone from all_referrers_view WHERE id = ?";
      referalDocMobile = DataBaseUtil.getStringValueFromDb(referalDoctorQuery, referalDoctorId);
    }
    String messageFooterToken = "SELECT message_footer from message_types WHERE message_type_id = 'sms_patient_on_op_admission'";
    String messageFooterTokenvalue = DataBaseUtil.getStringValueFromDb(messageFooterToken);
    admissionData.put("message_footer", messageFooterTokenvalue);
    admissionData.put("mr_no", (String) bean.get("mr_no"));
    admissionData.put("ward_name", (String) bean.get("reg_ward_name"));
    admissionData.put("patient_name", (String) bean.get("full_name"));
    admissionData.put("bed_name", (String) bean.get("alloc_bed_name"));
    admissionData.put("admission_date", DateUtil.formatDate((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_date_yyyy_mm_dd",
        sqlDateFormatter.format((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_time", (String) bean.get("reg_time").toString());
    admissionData.put("admission_time_12hr",
        DateUtil.formatTimeMeridiem((java.sql.Time) bean.get("reg_time")));
    admissionData.put("center_name", (String) bean.get("center_name"));
    admissionData.put("center_code", (String) bean.get("center_code"));
    admissionData.put("complaint", (String) bean.get("complaint"));
    admissionData.put("admitted_by", (String) bean.get("admitted_by"));
    admissionData.put("department", (String) bean.get("dept_name"));
    admissionData.put("receipient_id__", (String) bean.get("mr_no"));
    admissionData.put("receipient_type__", "PATIENT");

    admissionData.put("department_id", (String) bean.get("dept_id"));
    admissionData.put("patient_phone", (String) bean.get("patient_phone"));
    admissionData.put("recipient_email", (String) bean.get("email_id"));
    admissionData.put("next_of_kin_contact", (String) bean.get("patient_care_oftext"));
    admissionData.put("next_of_kin_name", (String) bean.get("relation"));

    admissionData.put("doctor_name", (String) bean.get("doctor_name"));
    admissionData.put("referal_doctor", (String) bean.get("refdoctorname"));
    admissionData.put("doctor_mobile", (String) bean.get("doctor_mobile"));
    admissionData.put("referal_doctor_mobile", referalDocMobile);
    admissionData.put("admitting_doctor_id__", (String) bean.get("doctor"));
    admissionData.put("referal_doctor_id__", (String) bean.get("reference_docto_id"));
    admissionData.put("salutation_name", (String) bean.get("salutation"));
    admissionData.put("doctor_specialization", (String) bean.get("specialization"));
    return admissionData;
  }

  private Map<String, String> getOPAdmissionData(String patientId) throws SQLException {
    BasicDynaBean bean = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    Map<String, String> admissionData = new HashMap<String, String>();

    String referalDoctorId = (String) bean.get("reference_docto_id");
    String referalDocMobile = "";
    // referal doctor mobile no is not available as part of patient_details_ext_view
    if (null != referalDoctorId && !referalDoctorId.trim().equals("")) {
      String referalDoctorQuery = "SELECT referrer_phone from all_referrers_view WHERE id = ?";
      referalDocMobile = DataBaseUtil.getStringValueFromDb(referalDoctorQuery, referalDoctorId);
    }
    String messageFooterToken = "SELECT message_footer from message_types WHERE message_type_id = 'sms_patient_on_op_admission'";
    String messageFooterTokenvalue = DataBaseUtil.getStringValueFromDb(messageFooterToken);
    admissionData.put("message_footer", messageFooterTokenvalue);
    admissionData.put("mr_no", (String) bean.get("mr_no"));
    admissionData.put("patient_name", (String) bean.get("full_name"));
    admissionData.put("admission_date", DateUtil.formatDate((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_date_yyyy_mm_dd",
        sqlDateFormatter.format((java.util.Date) bean.get("reg_date")));
    admissionData.put("admission_time", (String) bean.get("reg_time").toString());
    admissionData.put("admission_time_12hr",
        DateUtil.formatTimeMeridiem((java.sql.Time) bean.get("reg_time")));
    admissionData.put("center_name", (String) bean.get("center_name"));
    admissionData.put("center_code", (String) bean.get("center_code"));
    admissionData.put("complaint", (String) bean.get("complaint"));
    admissionData.put("admitted_by", (String) bean.get("admitted_by"));
    admissionData.put("department", (String) bean.get("dept_name"));
    admissionData.put("department_id", (String) bean.get("dept_id"));
    admissionData.put("patient_phone", (String) bean.get("patient_phone"));
    admissionData.put("next_of_kin_contact", (String) bean.get("patient_care_oftext"));
    admissionData.put("next_of_kin_name", (String) bean.get("relation"));
    admissionData.put("receipient_id__", (String) bean.get("mr_no"));
    admissionData.put("receipient_type__", "PATIENT");

    admissionData.put("doctor_name", (String) bean.get("doctor_name"));
    admissionData.put("referal_doctor", (String) bean.get("refdoctorname"));
    admissionData.put("doctor_mobile", (String) bean.get("doctor_mobile"));
    admissionData.put("referal_doctor_mobile", referalDocMobile);
    admissionData.put("consulting_doctor_id__", (String) bean.get("doctor"));
    admissionData.put("referal_doctor_id__", (String) bean.get("reference_docto_id"));
    admissionData.put("salutation_name", (String) bean.get("salutation"));
    admissionData.put("doctor_specialization", (String) bean.get("specialization"));
    return admissionData;
  }

  private BasicDynaBean getPrimarySponsorDetails(HttpServletRequest request,
      BasicDynaBean primarySponsorBean) throws SQLException {

    String primarySponsorId = null;
    String primaryInsCompId = null;
    BigDecimal primaryInsuranceApproval = null;
    String priorAuthId = null;
    int priorAuthModeId = 0;
    String primaryInsuranceApprovalStr = null;
    Integer planId = null;
    Integer categoryId = null;
    String useDRG = "N";
    String visitType = request.getParameter("group");

    primarySponsorBean.set("patient_insurance_plans_id", planDAO.getNextSequence());

    primarySponsorId = request.getParameter("primary_sponsor_id");
    primaryInsCompId = getParamDefault(request, "primary_insurance_co", null);

    primaryInsuranceApprovalStr = getParamDefault(request, "primary_insurance_approval", null);

    planId = request.getParameter("primary_plan_id") == null
        || request.getParameter("primary_plan_id").equals("") ? null
            : Integer.parseInt(request.getParameter("primary_plan_id"));
    categoryId = request.getParameter("primary_plan_type") == null
        || request.getParameter("primary_plan_type").equals("") ? null
            : Integer.parseInt(request.getParameter("primary_plan_type"));

    useDRG = (request.getParameter("primary_use_drg") != null
        && !request.getParameter("primary_use_drg").equals(""))
            ? request.getParameter("primary_use_drg")
            : useDRG;

    priorAuthId = request.getParameter("primary_prior_auth_id");
    priorAuthModeId = request.getParameter("primary_prior_auth_mode_id") == null
        || request.getParameter("primary_prior_auth_mode_id").equals("") ? 0
            : Integer.parseInt(request.getParameter("primary_prior_auth_mode_id"));
    primaryInsuranceApproval = primaryInsuranceApprovalStr != null
        ? new BigDecimal(primaryInsuranceApprovalStr)
        : null;

    String primaryPlanLimitStr = getParamDefault(request, "primary_plan_limit", null);
    BigDecimal primaryPlanLimit = primaryPlanLimitStr != null ? new BigDecimal(primaryPlanLimitStr)
        : null;

    String primaryVisitLimitStr = getParamDefault(request, "primary_visit_limit", null);
    BigDecimal primaryVisitLimit = primaryVisitLimitStr != null
        ? new BigDecimal(primaryVisitLimitStr)
        : null;

    String primaryVisitDeductibleStr = getParamDefault(request, "primary_visit_deductible", null);
    BigDecimal primaryVisitDeductible = primaryVisitDeductibleStr != null
        ? new BigDecimal(primaryVisitDeductibleStr)
        : null;

    String primaryVisitCopayStr = getParamDefault(request, "primary_visit_copay", null);
    BigDecimal primaryVisitCopay = primaryVisitCopayStr != null
        ? new BigDecimal(primaryVisitCopayStr)
        : null;

    String primaryMaxCopayStr = getParamDefault(request, "primary_max_copay", null);
    BigDecimal primaryMaxCopay = primaryMaxCopayStr != null ? new BigDecimal(primaryMaxCopayStr)
        : null;

    String primaryPerDayLimitStr = getParamDefault(request, "primary_perday_limit", null);
    BigDecimal primaryPerDayLimit = primaryPerDayLimitStr != null
        ? new BigDecimal(primaryPerDayLimitStr)
        : null;

    String limit_include_followUp = request.getParameter("primary_limits_include_followUps");

    String primaryUtilizationLimitStr = getParamDefault(request, "primary_plan_utilization", null);
    BigDecimal primaryUtilizationAmount = primaryUtilizationLimitStr != null
        ? new BigDecimal(primaryUtilizationLimitStr)
        : null;

    primarySponsorBean.set("insurance_co", primaryInsCompId);
    primarySponsorBean.set("sponsor_id", primarySponsorId);
    primarySponsorBean.set("plan_id", planId);
    primarySponsorBean.set("plan_type_id", categoryId);
    primarySponsorBean.set("insurance_approval", primaryInsuranceApproval);
    primarySponsorBean.set("prior_auth_id", priorAuthId);
    primarySponsorBean.set("prior_auth_mode_id", priorAuthModeId);
    primarySponsorBean.set("use_drg", useDRG);
    primarySponsorBean.set("priority", 1);
    primarySponsorBean.set("plan_limit", primaryPlanLimit);
    primarySponsorBean.set("visit_per_day_limit", primaryPerDayLimit);
    primarySponsorBean.set("utilization_amount", primaryUtilizationAmount);
    if (null != limit_include_followUp && !limit_include_followUp.equals("")
        && limit_include_followUp.equals("Y") && visitType.equalsIgnoreCase("opreg")) {
      primarySponsorBean.set("episode_limit", primaryVisitLimit);
      primarySponsorBean.set("episode_deductible", primaryVisitDeductible);
      primarySponsorBean.set("episode_copay_percentage", primaryVisitCopay);
      primarySponsorBean.set("episode_max_copay_percentage", primaryMaxCopay);
    } else {
      primarySponsorBean.set("visit_limit", primaryVisitLimit);
      primarySponsorBean.set("visit_deductible", primaryVisitDeductible);
      primarySponsorBean.set("visit_copay_percentage", primaryVisitCopay);
      primarySponsorBean.set("visit_max_copay_percentage", primaryMaxCopay);
    }

    return primarySponsorBean;
  }

  private BasicDynaBean getSecondarySponsorDetails(HttpServletRequest request,
      BasicDynaBean secondarySponsorBean) throws SQLException {

    String secondarySponsorId = null;
    String secondaryInsCompId = null;
    BigDecimal secondaryInsuranceApproval = null;
    String priorAuthId = null;
    int priorAuthModeId = 0;
    String secondaryInsuranceApprovalStr = null;
    Integer planId = 0;
    Integer categoryId = 0;
    String useDRG = "N";
    String visitType = request.getParameter("group");

    secondarySponsorBean.set("patient_insurance_plans_id", planDAO.getNextSequence());
    secondarySponsorId = request.getParameter("secondary_sponsor_id");
    secondaryInsCompId = getParamDefault(request, "secondary_insurance_co", null);

    secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_insurance_approval", null);

    planId = request.getParameter("secondary_plan_id") == null
        || request.getParameter("secondary_plan_id").equals("") ? null
            : Integer.parseInt(request.getParameter("secondary_plan_id"));

    categoryId = request.getParameter("secondary_plan_type") == null
        || request.getParameter("secondary_plan_type").equals("") ? null
            : Integer.parseInt(request.getParameter("secondary_plan_type"));

    useDRG = (request.getParameter("secondary_use_drg") != null
        && !request.getParameter("secondary_use_drg").equals(""))
            ? request.getParameter("secondary_use_drg")
            : useDRG;

    priorAuthId = request.getParameter("secondary_prior_auth_id");
    priorAuthModeId = request.getParameter("secondary_prior_auth_mode_id") == null
        || request.getParameter("secondary_prior_auth_mode_id").equals("") ? 0
            : Integer.parseInt(request.getParameter("secondary_prior_auth_mode_id"));
    secondaryInsuranceApproval = secondaryInsuranceApprovalStr != null
        ? new BigDecimal(secondaryInsuranceApprovalStr)
        : null;

    String secondaryPlanLimitStr = getParamDefault(request, "secondary_plan_limit", null);
    BigDecimal secondaryPlanLimit = secondaryPlanLimitStr != null
        ? new BigDecimal(secondaryPlanLimitStr)
        : null;

    String secondaryVisitLimitStr = getParamDefault(request, "secondary_visit_limit", null);
    BigDecimal secondaryVisitLimit = secondaryVisitLimitStr != null
        ? new BigDecimal(secondaryVisitLimitStr)
        : null;

    String secondaryVisitDeductibleStr = getParamDefault(request, "secondary_visit_deductible",
        null);
    BigDecimal secondaryVisitDeductible = secondaryVisitDeductibleStr != null
        ? new BigDecimal(secondaryVisitDeductibleStr)
        : null;

    String secondaryVisitCopayStr = getParamDefault(request, "secondary_visit_copay", null);
    BigDecimal secondaryVisitCopay = secondaryVisitCopayStr != null
        ? new BigDecimal(secondaryVisitCopayStr)
        : null;

    String secondaryMaxCopayStr = getParamDefault(request, "secondary_max_copay", null);
    BigDecimal secondaryMaxCopay = secondaryMaxCopayStr != null
        ? new BigDecimal(secondaryMaxCopayStr)
        : null;

    String secondaryPerDayLimitStr = getParamDefault(request, "secondary_perday_limit", null);
    BigDecimal secondaryPerDayLimit = secondaryPerDayLimitStr != null
        ? new BigDecimal(secondaryPerDayLimitStr)
        : null;

    String limit_include_followUp = request.getParameter("secondary_limits_include_followUps");

    String secondaryUtilizationLimitStr = getParamDefault(request, "secondary_plan_utilization",
        null);
    BigDecimal secondaryUtilizationAmount = secondaryUtilizationLimitStr != null
        ? new BigDecimal(secondaryUtilizationLimitStr)
        : null;

    secondarySponsorBean.set("insurance_co", secondaryInsCompId);
    secondarySponsorBean.set("sponsor_id", secondarySponsorId);
    secondarySponsorBean.set("plan_id", planId);
    secondarySponsorBean.set("plan_type_id", categoryId);
    secondarySponsorBean.set("insurance_approval", secondaryInsuranceApproval);
    secondarySponsorBean.set("prior_auth_id", priorAuthId);
    secondarySponsorBean.set("prior_auth_mode_id", priorAuthModeId);
    secondarySponsorBean.set("use_drg", useDRG);
    secondarySponsorBean.set("priority", 2);
    secondarySponsorBean.set("plan_limit", secondaryPlanLimit);
    secondarySponsorBean.set("visit_per_day_limit", secondaryPerDayLimit);
    secondarySponsorBean.set("utilization_amount", secondaryUtilizationAmount);
    if (null != limit_include_followUp && !limit_include_followUp.equals("")
        && limit_include_followUp.equals("Y") && visitType.equalsIgnoreCase("opreg")) {
      secondarySponsorBean.set("episode_limit", secondaryVisitLimit);
      secondarySponsorBean.set("episode_deductible", secondaryVisitDeductible);
      secondarySponsorBean.set("episode_copay_percentage", secondaryVisitCopay);
      secondarySponsorBean.set("episode_max_copay_percentage", secondaryMaxCopay);
    } else {
      secondarySponsorBean.set("visit_limit", secondaryVisitLimit);
      secondarySponsorBean.set("visit_deductible", secondaryVisitDeductible);
      secondarySponsorBean.set("visit_copay_percentage", secondaryVisitCopay);
      secondarySponsorBean.set("visit_max_copay_percentage", secondaryMaxCopay);
    }

    return secondarySponsorBean;
  }

  private static String getValue(String key, Map params) {
    Object[] obj = (Object[]) params.get(key);
    if (obj != null && obj[0] != null) {
      return obj[0].toString();
    }
    return "";
  }

  private List<BasicDynaBean> getPrimaryInsuranceDetails(HttpServletRequest request,
      String patientID, String visitType, BasicDynaBean patientInsPlanBean) throws SQLException {

    ArrayList<BasicDynaBean> priInsDeatils = new ArrayList<BasicDynaBean>();
    String[] categoryNames = request.getParameterValues("P_cat_name");
    String[] categoryIds = request.getParameterValues("P_cat_id");
    String[] sponserLimits = request.getParameterValues("P_sponser_limit");
    String[] catDeducts = request.getParameterValues("P_cat_deductible");
    String[] itemDeducts = request.getParameterValues("P_item_deductible");
    String[] copayPercent = request.getParameterValues("P_copay_percent");
    String[] maxCopayPercent = request.getParameterValues("P_max_copay");

    BasicDynaBean primaryInsSponsorBean;

    Integer primarySponsorId = null;
    Integer insurance_category_id = null;
    BigDecimal patient_amount = null;
    BigDecimal patient_percent = null;
    BigDecimal patient_amount_cap = null;
    BigDecimal per_treatment_limit = null;
    BigDecimal patient_amount_per_category = null;
    String patient_type = visitType;

    if (categoryNames != null && categoryNames.length > 0) {
      for (int j = 0; j < categoryNames.length; j++) {
        primaryInsSponsorBean = patPlanDetailsDAO.getBean();

        if (null != patientInsPlanBean
            && null != patientInsPlanBean.get("patient_insurance_plans_id")) {
          Integer patientInsPlanId = (Integer) patientInsPlanBean.get("patient_insurance_plans_id");
          primaryInsSponsorBean.set("patient_insurance_plans_id", patientInsPlanId);
        }

        primarySponsorId = request.getParameter("primary_plan_id") == null
            || request.getParameter("primary_plan_id").equals("") ? null
                : Integer.parseInt(request.getParameter("primary_plan_id"));

        insurance_category_id = categoryIds[j] == null || categoryIds[j].equals("") ? new Integer(0)
            : Integer.parseInt(categoryIds[j]);

        patient_amount = sponserLimits[j] == null || sponserLimits[j].equals("") ? null
            : new BigDecimal(sponserLimits[j]);

        patient_percent = catDeducts[j] == null || catDeducts[j].equals("") ? new BigDecimal(0)
            : new BigDecimal(catDeducts[j]);

        patient_amount_cap = itemDeducts[j] == null || itemDeducts[j].equals("") ? new BigDecimal(0)
            : new BigDecimal(itemDeducts[j]);

        per_treatment_limit = copayPercent[j] == null || copayPercent[j].equals("")
            ? new BigDecimal(0)
            : new BigDecimal(copayPercent[j]);

        patient_amount_per_category = maxCopayPercent[j] == null || maxCopayPercent[j].equals("")
            ? null
            : new BigDecimal(maxCopayPercent[j]);

        primaryInsSponsorBean.set("visit_id", patientID);
        primaryInsSponsorBean.set("plan_id", primarySponsorId);
        primaryInsSponsorBean.set("insurance_category_id", insurance_category_id);
        primaryInsSponsorBean.set("patient_amount", patient_amount_cap);
        primaryInsSponsorBean.set("patient_percent", per_treatment_limit);
        primaryInsSponsorBean.set("patient_amount_cap", patient_amount_per_category);
        primaryInsSponsorBean.set("per_treatment_limit", patient_amount);
        primaryInsSponsorBean.set("patient_amount_per_category", patient_percent);
        primaryInsSponsorBean.set("patient_type", patient_type);

        priInsDeatils.add(primaryInsSponsorBean);

      }
    }

    return priInsDeatils;
  }

  private List<BasicDynaBean> getSecondaryInsuranceDetails(HttpServletRequest request,
      String patientID, String visitType, BasicDynaBean secondaryInsPlanBean) throws SQLException {

    ArrayList<BasicDynaBean> secInsDeatils = new ArrayList<BasicDynaBean>();
    String[] categoryNames = request.getParameterValues("S_cat_name");
    String[] categoryIds = request.getParameterValues("S_cat_id");
    String[] sponserLimits = request.getParameterValues("S_sponser_limit");
    String[] catDeducts = request.getParameterValues("S_cat_deductible");
    String[] itemDeducts = request.getParameterValues("S_item_deductible");
    String[] copayPercent = request.getParameterValues("S_copay_percent");
    String[] maxCopayPercent = request.getParameterValues("S_max_copay");

    BasicDynaBean secondaryInsSponsorBean;

    Integer secondarySponsorId = null;
    Integer insurance_category_id = null;
    BigDecimal patient_amount = null;
    BigDecimal patient_percent = null;
    BigDecimal patient_amount_cap = null;
    BigDecimal per_treatment_limit = null;
    BigDecimal patient_amount_per_category = null;
    String patient_type = visitType;

    if (categoryNames != null && categoryNames.length > 0) {
      for (int j = 0; j < categoryNames.length; j++) {
        secondaryInsSponsorBean = patPlanDetailsDAO.getBean();

        if (null != secondaryInsPlanBean
            && null != secondaryInsPlanBean.get("patient_insurance_plans_id")) {
          Integer patientInsPlanId = (Integer) secondaryInsPlanBean
              .get("patient_insurance_plans_id");
          secondaryInsSponsorBean.set("patient_insurance_plans_id", patientInsPlanId);
        }

        secondarySponsorId = request.getParameter("secondary_plan_id") == null
            || request.getParameter("secondary_plan_id").equals("") ? null
                : Integer.parseInt(request.getParameter("secondary_plan_id"));

        insurance_category_id = request.getParameter("S_cat_id") == null
            || request.getParameter("S_cat_id").equals("") ? new Integer(0)
                : Integer.parseInt(request.getParameter("S_cat_id"));

        insurance_category_id = categoryIds[j] == null || categoryIds[j].equals("") ? new Integer(0)
            : Integer.parseInt(categoryIds[j]);

        patient_amount = sponserLimits[j] == null || sponserLimits[j].equals("") ? null
            : new BigDecimal(sponserLimits[j]);

        patient_percent = catDeducts[j] == null || catDeducts[j].equals("") ? new BigDecimal(0)
            : new BigDecimal(catDeducts[j]);

        patient_amount_cap = itemDeducts[j] == null || itemDeducts[j].equals("") ? new BigDecimal(0)
            : new BigDecimal(itemDeducts[j]);

        per_treatment_limit = copayPercent[j] == null || copayPercent[j].equals("")
            ? new BigDecimal(0)
            : new BigDecimal(copayPercent[j]);

        patient_amount_per_category = maxCopayPercent[j] == null || maxCopayPercent[j].equals("")
            ? null
            : new BigDecimal(maxCopayPercent[j]);

        secondaryInsSponsorBean.set("visit_id", patientID);
        secondaryInsSponsorBean.set("plan_id", secondarySponsorId);
        secondaryInsSponsorBean.set("insurance_category_id", insurance_category_id);
        secondaryInsSponsorBean.set("patient_amount", patient_amount_cap);
        secondaryInsSponsorBean.set("patient_percent", per_treatment_limit);
        secondaryInsSponsorBean.set("patient_amount_cap", patient_amount_per_category);
        secondaryInsSponsorBean.set("per_treatment_limit", patient_amount);
        secondaryInsSponsorBean.set("patient_amount_per_category", patient_percent);
        secondaryInsSponsorBean.set("patient_type", patient_type);

        secInsDeatils.add(secondaryInsSponsorBean);

      }
    }

    return secInsDeatils;
  }

  @IgnoreConfidentialFilters
  public ActionForward getInsurancePlanDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    String planId = request.getParameter("plan_id");
    String visitType = request.getParameter("visitType");
    String visitId = request.getParameter("visitId");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(planDetailsDao
        .getAllPlanChargesBasedonPatientType(Integer.parseInt(planId), visitType, visitId))));
    response.flushBuffer();
    return null;
  }

  @IgnoreConfidentialFilters
  public ActionForward getInsurancePlanDetailsForFollowUpVisit(ActionMapping mapping,
      ActionForm form, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, SQLException {
    String planId = request.getParameter("plan_id");
    String mrno = request.getParameter("mrno");
    String visitType = request.getParameter("visitType");
    String mainvisitId = request.getParameter("mainvisitId");
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter()
        .write(js.serialize(ConversionUtils.listBeanToListMap(
            planDetailsDao.getInsurancePlanDetailsForFollowUpVisit(Integer.parseInt(planId), mrno,
                visitType, mainvisitId))));
    response.flushBuffer();
    return null;
  }

  private Map<String, String> populateTokenMap(BasicDynaBean patientDetailsBean, Integer centerId,
      String referredBy, String doctorId) throws SQLException {
    Map<String, String> tokenMap = new HashMap<String, String>();
    Calendar currDate = Calendar.getInstance();
    String currentDate = sqlDateFormatter.format(currDate.getTime());
    tokenMap.put("payment_date", currentDate);
    String mrNo = (String) patientDetailsBean.get("mr_no");
    tokenMap.put("mr_no", mrNo);
    tokenMap.put("receipient_id__", mrNo);
    String patientMobileNo = (String) patientDetailsBean.get("patient_phone");
    tokenMap.put("recipient_phone", patientMobileNo);
    tokenMap.put("recipient_mobile", patientMobileNo);
    String salId = (String) patientDetailsBean.get("salutation");
    String patientSalutation = "";
    String fullName = "";
    if (salId != null && !salId.equals("")) {
      BasicDynaBean salBean = salutationMasterDao.findByKey("salutation_id", salId);
      patientSalutation = (String) salBean.get("salutation");
    }
    if (patientSalutation != null && !patientSalutation.equals(""))
      fullName = patientSalutation
          + " ";
    if (patientDetailsBean.get("patient_name") != null)
      fullName = fullName
          + patientDetailsBean.get("patient_name")
          + " ";
    if (patientDetailsBean.get("middle_name") != null)
      fullName = fullName
          + patientDetailsBean.get("middle_name")
          + " ";
    if (patientDetailsBean.get("last_name") != null)
      fullName = fullName + patientDetailsBean.get("last_name");
    tokenMap.put("recipient_name", fullName);
    tokenMap.put("recipient_email", (String) patientDetailsBean.get("email_id"));
    String currency = GenericPreferencesDAO.getGenericPreferences().getCurrencySymbol();
    tokenMap.put("currency_symbol", currency);
    BasicDynaBean centerData = centerMasterDAO.findByKey("center_id", centerId);
    String centerName = (String) centerData.get("center_name");
    tokenMap.put("center_name", centerName);
    String centerPhone = (String) centerData.get("center_contact_phone");
    tokenMap.put("center_contact_phone", centerPhone);
    String centerAddress = (String) centerData.get("center_address");
    tokenMap.put("center_address", centerAddress);
    String referralDoctor = "";
    if (referredBy != null && !referredBy.equals("")) {
      BasicDynaBean refBeanFromReferral = referralDao.findByKey("referal_no", referredBy);
      if (refBeanFromReferral != null) {
        referralDoctor = (String) refBeanFromReferral.get("referal_name");
      } else {
        BasicDynaBean refBeanFromDoc = doctorsDao.findByKey("doctor_id", referredBy);
        if (refBeanFromDoc != null) {
          referralDoctor = (String) refBeanFromDoc.get("doctor_name");
        }
      }
    }
    tokenMap.put("referal_doctor", referralDoctor);
    String concDoctor = "";
    if (doctorId != null && !doctorId.equals("")) {
      concDoctor = (String) doctorsDao.findByKey("doctor_id", doctorId).get("doctor_name");
    }
    tokenMap.put("doctor_name", concDoctor);
    tokenMap.put("receipient_type__", "PATIENT");
    return tokenMap;
  }
  
  public void schedulePushEvent( String appointmentId, String eventId) {
    String schema =  RequestContext.getSchema();
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("appointment_id", appointmentId);
    eventData.put("schema", schema);
    eventData.put("eventId", eventId);
    
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", schema);
    jobData.put("eventId", eventId);
    jobData.put("eventData", eventData);
    jobService
        .scheduleImmediate(buildJob("PushEventJob_" + appointmentId,
            EventListenerJob.class, jobData));
  }
}
