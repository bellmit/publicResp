package com.insta.hms.core.clinical.order.testitems;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.ordersets.OrderSetsOrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.order.testvisitreports.TestVisitReportsService;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationDetailsRepository;
import com.insta.hms.core.diagnostics.incomingsampleregistration.IncomingSampleRegistrationService;
import com.insta.hms.documents.LabTestDocumentService;
import com.insta.hms.documents.TestDocumentsService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.testorganization.TestOrganizationRepository;
import com.insta.hms.mdm.tests.TestsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This Class does all Test Order Item related Task. Creation of test order
 * Bean, insertion of charges to bill charge , bill charge claim. Creation of
 * Test Bill charge Bean.
 * 
 * @author ritolia
 */
@Service
@Order(key = "Diagnostics", value = { "Laboratory", "Radiology" }, prefix = "tests")
public class TestOrderItemService extends OrderItemService {

  /** The Constant PATIENT_ID. */
  private static final String PATIENT_ID = "patient_id";

  /** The Constant TEST_ID. */
  private static final String TEST_ID = "test_id";

  /** The Constant MULTI_VISIT_PACKAGE. */
  private static final String MULTI_VISIT_PACKAGE = "_multi_visit_package";

  /** The Constant PRIOR_AUTH_ID. */
  private static final String PRIOR_AUTH_ID = "_prior_auth_id";

  /** The Constant PRIOR_AUTH_MODE_ID. */
  private static final String PRIOR_AUTH_MODE_ID = "_prior_auth_mode_id";

  /** The Constant SEC_PRIOR_AUTH_ID. */
  private static final String SEC_PRIOR_AUTH_ID = "_sec_prior_auth_id";

  /** The Constant SEC_PRIOR_AUTH_MODE_ID. */
  private static final String SEC_PRIOR_AUTH_MODE_ID = "_sec_prior_auth_mode_id";

  /** The Constant PAYEE_DOCTOR_ID. */
  private static final String PAYEE_DOCTOR_ID = "_payee_doctor_id";

  /** The Constant BILL_RATE_PLAN_ID. */
  private static final String BILL_RATE_PLAN_ID = "bill_rate_plan_id";

  /** The Constant BED_TYPE. */
  private static final String BED_TYPE = "bed_type";

  /** The Constant PRESCRIBED_DATE. */
  private static final String PRESCRIBED_DATE = "pres_date";

  /** The Constant PRESCRIBED_ID. */
  private static final String PRESCRIBED_ID = "prescribed_id";

  /** The Constant CATEGORY. */
  private static final String CATEGORY = "category";

  /** The Constant DEP_LAB. */
  private static final String DEP_LAB = "DEP_LAB";

  /** The Constant CONDUCTION_APPLICABLE. */
  private static final String CONDUCTION_APPLICABLE = "conduction_applicable";

  /** The Constant NEW_PRE_AUTHS. */
  private static final String NEW_PRE_AUTHS = "newPreAuths";

  /** The Constant SEC_NEW_PRE_AUTHS. */
  private static final String SEC_NEW_PRE_AUTHS = "secNewPreAuths";

  /** The Constant NEW_PRE_AUTH_MODE. */
  private static final String NEW_PRE_AUTH_MODE = "newPreAuthModesList";

  /** The Constant SEC_NEW_PRE_AUTH_MODE. */
  private static final String SEC_NEW_PRE_AUTH_MODE = "secNewPreAuthModesList";

  /** The Constant ACTIVITY_CODE. */
  private static final String ACTIVITY_CODE = "DIA";

  /** The Constant PRES_DOCTOR. */
  private static final String PRES_DOCTOR = "pres_doctor";

  /** The Constant OUTSOURCE_DEST_PRESCRIBED_ID. */
  private static final String OUTSOURCE_DEST_PRESCRIBED_ID = "outsource_dest_prescribed_id";

  /** The Constant DOC_PRESC_ID. */
  private static final String DOC_PRESC_ID = "doc_presc_id";

  /** The Constant CLINICAL_NOTES. */
  private static final String CLINICAL_NOTES = "clinical_notes";

  /** The prefix. */
  private static String prefix = "tests";

  /** The test order item repository. */
  @LazyAutowired
  private TestOrderItemRepository testOrderItemRepository;

  /** The test service. */
  @LazyAutowired
  private TestsService testService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The bill activity charge service. */
  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The test visit reports service. */
  @LazyAutowired
  private TestVisitReportsService testVisitReportsService;

  /** The incoming sample reg service. */
  @LazyAutowired
  private IncomingSampleRegistrationService incomingSampleRegService;

  @LazyAutowired
  private IncomingSampleRegistrationDetailsRepository incomingSampleRegDetailsrepo;

  /** The lab test document service. */
  @LazyAutowired
  private LabTestDocumentService labTestDocumentService;

  /** The test documents service. */
  @LazyAutowired
  private TestDocumentsService testDocumentsService;

  /** The patient activities service. */
  @LazyAutowired
  private PatientActivitiesService patientActivitiesService;

  /** The order sets order item service. */
  @LazyAutowired
  private OrderSetsOrderItemService orderSetsOrderItemService;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;
  
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;

   
  /** Module activated service. */
  @LazyAutowired
  private ModulesActivatedService modulesActivatedService;
  
  /** Pending prescription service. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;
  
  /**
   * Instantiates a new test order item service.
   *
   * @param repo the repo
   */
  public TestOrderItemService(TestOrderItemRepository repo,
      TestOrganizationRepository orgDetailsRepository) {
    super(repo, prefix, TEST_ID, orgDetailsRepository);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#setHeaderProperties
   * (org.apache.commons .beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String, boolean,
   * java.util.Map)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_DATE);
    setBeanValue(orderBean, PRESCRIBED_DATE, ConvertUtils
        .convert(orderedItemList.get("tests_prescribed_date"), dynaProperties.getType()));
    setBeanValue(orderBean, PRES_DOCTOR, orderedItemList.get("tests_prescribed_doctor_id"));
    setBeanValue(orderBean, TEST_ID, orderedItemList.get("tests_item_id"));
    BasicDynaBean prefs = genericPreferencesService.getAllPreferences();
    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    Integer centerId = (Integer) headerInformation.get("center_id");
    BasicDynaBean test = getMasterChargesBean((String) orderBean.get(TEST_ID), bedType, ratePlanId,
        centerId);
    if ((Boolean) test.get("applicable")) {
      setLabNo(prefs, test, orderBean, headerInformation);
      setTokenNo(test, prefs, orderBean, centerId);
      setBeanValue(orderBean, "pat_id", patientId);
      int prescId = testOrderItemRepository.getNextSequence();
      setBeanValue(orderBean, PRESCRIBED_ID, prescId);
      setBeanValue(orderBean, "curr_location_presc_id", prescId);

      if ("I".equals((String) test.get("house_status"))) {
        setBeanValue(orderBean, "prescription_type", "h");
      } else {
        setBeanValue(orderBean, "prescription_type", "o");
      }

      setReadyTime(orderBean, centerId);
      setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));
      setAppointmentId(orderBean, (Integer) headerInformation.get("appointmentid"));
      setConducted(test, orderBean);
      setConductionType(orderBean, centerId);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * setItemBeanProperties(org.apache. commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String, boolean,
   * java.util.Map, org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) {
    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    Integer centerId = (Integer) headerInformation.get("center_id");
    BasicDynaBean test = getMasterChargesBean((String) orderedItemList.get("item_id"), bedType,
        ratePlanId, centerId);
    // If the test is not applicable then orderBean is set to null
    if ((test.get("applicable") == null
          || !((Boolean) test.get("applicable"))) && !isMultiVisitPackItem) {
      return null;
    }
    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_DATE);
    setBeanValue(orderBean, PRESCRIBED_DATE,
        ConvertUtils.convert(orderedItemList.get("prescribed_date"), dynaProperties.getType()));
    setBeanValue(orderBean, PRES_DOCTOR, orderedItemList.get("prescribed_doctor_id"));
    setBeanValue(orderBean, TEST_ID, orderedItemList.get("item_id"));
    BasicDynaBean prefs = genericPreferencesService.getAllPreferences();
    setLabNo(prefs, test, orderBean, headerInformation);
    setTokenNo(test, prefs, orderBean, centerId);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(orderBean, "pat_id", patientId);
    int prescId = testOrderItemRepository.getNextSequence();
    setBeanValue(orderBean, PRESCRIBED_ID, prescId);
    setBeanValue(orderBean, "curr_location_presc_id", prescId);
    setBeanValue(orderBean, "clinical_notes", orderedItemList.get("clinical_notes"));

    setBeanValue(orderBean, DOC_PRESC_ID,
        !"".equals(orderedItemList.get(DOC_PRESC_ID)) ? orderedItemList.get(DOC_PRESC_ID) : null);

    if ("I".equals((String) test.get("house_status"))) {
      setBeanValue(orderBean, "prescription_type", "h");
    } else {
      setBeanValue(orderBean, "prescription_type", "o");
    }
    setReadyTime(orderBean, centerId);
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));
    setAppointmentId(orderBean, (Integer) headerInformation.get("appointmentid"));
    setConducted(test, orderBean);
    setConductionType(orderBean, centerId);
    return orderBean;
  }

  /**
   * Sets the lab no.
   *
   * @param prefs             the prefs
   * @param test              the test
   * @param orderBean         the order bean
   * @param headerInformation the header information
   */
  public void setLabNo(BasicDynaBean prefs, BasicDynaBean test, BasicDynaBean orderBean,
      BasicDynaBean headerInformation) {
    String testCategory = (String) test.get(CATEGORY);
    String labNo = null;
    if ("Y".equals(prefs.get("autogenerate_labno"))) {
      if (DEP_LAB.equals(testCategory)) {
        if (headerInformation.get("labno") != null) {
          labNo = (String) headerInformation.get("labno");
        } else {
          labNo = testOrderItemRepository.getSequenceId("LABNO");
          headerInformation.set("labno", labNo);
        }
      } else {
        if (headerInformation.get("radno") != null) {
          labNo = (String) headerInformation.get("radno");
        } else {
          labNo = testOrderItemRepository.getSequenceId("RADNO");
          headerInformation.set("radno", labNo);
        }
      }
    }
    orderBean.set("labno", labNo);
  }

  /**
   * Sets the token no.
   *
   * @param test      the test
   * @param prefs     the prefs
   * @param orderBean the order bean
   * @param centerId  the center id
   */
  public void setTokenNo(BasicDynaBean test, BasicDynaBean prefs, BasicDynaBean orderBean,
      Integer centerId) {

    String testCategory = (String) test.get(CATEGORY);
    String testDeptId = (String) testService
        .findByUniqueName((String) orderBean.get(TEST_ID), TEST_ID).get("ddept_id");
    Integer tokenNumber = null;
    if ((DEP_LAB.equals(testCategory) && "Y".equals(prefs.get("gen_token_for_lab")))
        || "Y".equals(prefs.get("gen_token_for_rad"))) {
      tokenNumber = testService.getToken(testDeptId, centerId);
    }
    orderBean.set("token_number", tokenNumber);

  }

  /**
   * Sets the item is conducted.
   *
   * @param test      the test
   * @param orderBean the order bean
   */
  public void setConducted(BasicDynaBean test, BasicDynaBean orderBean) {
    boolean condApplicable = (Boolean) test.get(CONDUCTION_APPLICABLE);
    boolean resultEntryApplicable = (Boolean) test.get("results_entry_applicable");

    String conducted = "N";
    String sflag = "0";
    if (!condApplicable) {
      conducted = "U";
      sflag = "1";
    }
    if (condApplicable && !resultEntryApplicable) {
      conducted = "NRN";
    }

    setBeanValue(orderBean, "conducted", conducted);
    setBeanValue(orderBean, "sflag", sflag);
  }

  /**
   * Sets the conduction type.
   *
   * @param orderBean the order bean
   * @param centerId  the center id
   */
  public void setConductionType(BasicDynaBean orderBean, Integer centerId) {
    if (testService.isOutsourceTest((String) orderBean.get(TEST_ID), centerId)) {
      setBeanValue(orderBean, "conduction_type", "o");
    } else {
      setBeanValue(orderBean, "conduction_type", "i");
    }
  }

  /**
   * Sets the ready time.
   *
   * @param orderBean the order bean
   * @param centerId  the center id
   */
  public void setReadyTime(BasicDynaBean orderBean, Integer centerId) {
    Timestamp readyTime = testService.calculateExptRptReadyTime(
        (Timestamp) orderBean.get(PRESCRIBED_DATE), (String) orderBean.get(TEST_ID), centerId);
    setBeanValue(orderBean, "exp_rep_ready_time", readyTime);
  }

  /**
   * Gets the Master Charges for the test order Item.
   *
   * @param serviceId the service id
   * @param bedType   the bed type
   * @param orgId     the org id
   * @param centerId  the center id
   * @return the master charges bean
   */
  @Override
  public BasicDynaBean getMasterChargesBean(Object serviceId, String bedType, String orgId,
      Integer centerId) {
    return testService.getTestDetails((String) serviceId, bedType, orgId, centerId);
  }

  /**
   * Gets the Master Charges for the test order Item.
   *
   * @param serviceId the service id
   * @param orgId     the org id
   * @return the master charges bean
   */
  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object serviceId, String orgId) {
    return testService.getAllBedTypeTestDetails((String) serviceId, orgId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#getCharges(java.
   * util.Map)
   */
  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    Integer centerId = (Integer) paramMap.get("center_id");

    BasicDynaBean masterCharge = getMasterChargesBean(paramMap.get("id"), bedType, ratePlanId,
        centerId);

    return getChargesList(masterCharge, (BigDecimal) paramMap.get("quantity"), isInsurance, null,
        paramMap);
  }

  /**
   * Returns the listBillCharge bean that contains service orderCharges and tax
   * charges.
   *
   * @param itemType     the item type
   * @param quantity     the quantity
   * @param isInsurance  the is insurance
   * @param condDoctorId the cond doctor id
   * @param otherParams  the other params
   * @return the charges list
   */
  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {

    String testCategory = (String) itemType.get(CATEGORY);
    String testName = (String) itemType.get("test_name");

    String head = testCategory.equals(DEP_LAB) ? "LTDIA" : "RTDIA";

    int insuranceCategoryId;
    if (itemType == null || itemType.get("insurance_category_id") == null) {
      insuranceCategoryId = 0;
    } else {
      insuranceCategoryId = (Integer) itemType.get("insurance_category_id");
    }

    BasicDynaBean chrgdto = billChargeService.setBillChargeBean("DIA", head,
        (BigDecimal) itemType.get("charge"), quantity,
        ((BigDecimal) itemType.get("discount")).multiply(quantity), (String) itemType.get(TEST_ID),
        testName, (String) itemType.get("ddept_id"), (Integer) itemType.get("service_sub_group_id"),
        insuranceCategoryId, isInsurance);

    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      if (otherParams.get("item_excluded_from_doctor").equals("Y")
          || otherParams.get("item_excluded_from_doctor").equals(true)) {
        chrgdto.set("item_excluded_from_doctor", true);
        chrgdto.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      } else if (otherParams.get("item_excluded_from_doctor").equals("N")
          || otherParams.get("item_excluded_from_doctor").equals(false)) {
        chrgdto.set("item_excluded_from_doctor", false);
        chrgdto.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      }
    }

    if (condDoctorId != null) {
      chrgdto.set("activity_conducted", "Y");
      chrgdto.set("payee_doctor_id", condDoctorId);
    }

    chrgdto.set("act_item_code", (String) itemType.get("diag_code"));
    chrgdto.set("act_rate_plan_item_code", (String) itemType.get("rate_plan_code"));
    chrgdto.set("code_type", (String) itemType.get("code_type"));
    chrgdto.set("conducting_doc_mandatory", itemType.get("conducting_doc_mandatory"));
    chrgdto.set("allow_rate_increase", (Boolean) itemType.get("allow_rate_increase"));
    chrgdto.set("allow_rate_decrease", (Boolean) itemType.get("allow_rate_decrease"));
    if (itemType != null || itemType.get("billing_group_id") != null) {
      chrgdto.set("billing_group_id", (Integer) itemType.get("billing_group_id"));
    }
    if (null != otherParams.get("package_id")) {
      chrgdto.set("package_id", Integer.parseInt(otherParams.get("package_id").toString()));
    }
    
    if (otherParams != null && otherParams.get("preauth_act_id") != null) {
      chrgdto.set("preauth_act_id", otherParams.get("preauth_act_id"));
    }

    String allowZeroClaimfor = (String) itemType.get("allow_zero_claim_amount");
    String visitType = (String) otherParams.get("visit_type");
    if (null != visitType
        && (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor))) {
      chrgdto.set("allow_zero_claim", true);
    }
    List<BasicDynaBean> testChargesList = new ArrayList<BasicDynaBean>();
    testChargesList.add(chrgdto);
    return testChargesList;
  }

  /**
   * Gets the prescribed test list.
   *
   * @param patientId the patient id
   * @return the prescribed test list
   */
  public List<BasicDynaBean> getPrescribedTestList(String patientId) {
    return testOrderItemRepository.getPrescribedTestList(patientId);
  }

  /**
   * Gets the selected test details.
   *
   * @param prescId the presc id
   * @return the selected test details
   */
  public List<BasicDynaBean> getSelectedTestDetails(String[] prescId) {
    return testOrderItemRepository.getSelectedTestDetails(prescId);
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return testOrderItemRepository.update(bean, keys);
  }

  /**
   * Since there is priority setup we need to override the base class function.
   *
   * @param requestParams     the request params
   * @param headerInformation the header information
   * @param username          the username
   * @param orderedItemAuths  the ordered item auths
   * @param preAuthIds        the pre auth ids
   * @param preAuthModeIds    the pre auth mode ids
   * @return the order bean
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List<BasicDynaBean> getOrderBean(Map<String, List<Object>> requestParams,
      BasicDynaBean headerInformation, String username, Map<String, List<Object>> orderedItemAuths,
      String[] preAuthIds, Integer[] preAuthModeIds) {

    List<BasicDynaBean> orderBeanList = new ArrayList<BasicDynaBean>();
    Object[] orderedItemList = requestParams.get(prefix).toArray();

    String multiVisitKey = prefix + MULTI_VISIT_PACKAGE;
    String priAuthIdKey = prefix + PRIOR_AUTH_ID;
    String priAuthModeIdKey = prefix + PRIOR_AUTH_MODE_ID;
    String secAuthIdkey = prefix + SEC_PRIOR_AUTH_ID;
    String secAuthModeIdKey = prefix + SEC_PRIOR_AUTH_MODE_ID;
    String payeeDoctorIdKey = prefix + PAYEE_DOCTOR_ID;
    String urgentKey = prefix + "_urgent";
    String docPrescIdKey = prefix + "_" + DOC_PRESC_ID;

    List errorList = new ArrayList();
    if (orderedItemList != null) {
      for (int i = 0; i < orderedItemList.length; i++) {

        Object priAuthId = ((Map<String, Object>) orderedItemList[i]).get(priAuthIdKey);
        if (priAuthId == null || ((String) (priAuthId)).equals("")) {
          if (preAuthIds != null && preAuthIds.length != 0 && preAuthIds[0] != null) {
            orderedItemAuths.get(NEW_PRE_AUTHS).add(preAuthIds[0]);
          } else {
            orderedItemAuths.get(NEW_PRE_AUTHS).add(null);
          }
        } else {
          orderedItemAuths.get(NEW_PRE_AUTHS).add((String) (priAuthId));
        }

        Object priAuthModeId = ((Map<String, Object>) orderedItemList[i]).get(priAuthModeIdKey);
        if (priAuthModeId == null || ((Integer) priAuthModeId) == 0) {
          if (preAuthModeIds != null && preAuthModeIds.length != 0 && preAuthModeIds[0] != null
              && preAuthModeIds[0] != 0) {
            orderedItemAuths.get(NEW_PRE_AUTH_MODE).add(preAuthModeIds[0]);
          } else {
            orderedItemAuths.get(NEW_PRE_AUTH_MODE).add(1);
          }
        } else {
          orderedItemAuths.get(NEW_PRE_AUTH_MODE).add(priAuthModeId);
        }

        Object secAuthId = ((Map<String, Object>) orderedItemList[i]).get(secAuthIdkey);
        if (secAuthId == null || ((String) secAuthId).equals("")) {
          if (preAuthIds != null && preAuthIds.length > 1 && preAuthIds[1] != null) {
            orderedItemAuths.get(SEC_NEW_PRE_AUTHS).add(preAuthIds[1]);
          } else {
            orderedItemAuths.get(SEC_NEW_PRE_AUTHS).add(null);
          }
        } else {
          orderedItemAuths.get(SEC_NEW_PRE_AUTHS).add((String) secAuthId);
        }

        Object secAuthModeId = ((Map<String, Object>) orderedItemList[i]).get(secAuthModeIdKey);
        if (secAuthModeId == null || (Integer) secAuthModeId == 0) {
          if (preAuthModeIds != null && preAuthModeIds.length > 1 && preAuthModeIds[1] != null
              && preAuthModeIds[1] != 0) {
            orderedItemAuths.get(SEC_NEW_PRE_AUTH_MODE).add(preAuthModeIds[1]);
          } else {
            orderedItemAuths.get(SEC_NEW_PRE_AUTH_MODE).add(1);
          }
        } else {
          orderedItemAuths.get(SEC_NEW_PRE_AUTH_MODE).add(secAuthModeId);
        }

        orderedItemAuths.get("conductingDoctorList")
            .add((String) ((Map<String, Object>) orderedItemList[i]).get(payeeDoctorIdKey));

        BasicDynaBean orderedItemBean = testOrderItemRepository.getBean();
        ConversionUtils.copyJsonToDynaBeanPrefixed((Map) orderedItemList[i], orderedItemBean,
            errorList, prefix + "_");

        Boolean isMultivisitPackage = false;
        if (((Map<String, Object>) orderedItemList[i]) != null) {
          isMultivisitPackage = new Boolean(
              (Boolean) ((Map<String, Object>) orderedItemList[i]).get(multiVisitKey));
          orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);
        }
        if (null != orderedItemBean && null != orderedItemBean.get("conducted")
            && orderedItemBean.get("conducted").toString().isEmpty()) {
          orderedItemBean.set("conducted", "N");
        }

        Object testUrgentId = ((Map<String, Object>) orderedItemList[i]).get(urgentKey);
        orderedItemBean.set("priority", (String) testUrgentId);

        Object docPrescIdObject = ((Map<String, Object>) orderedItemList[i]).get(docPrescIdKey);
        if (null != docPrescIdObject && !StringUtils.EMPTY.equals(docPrescIdObject)) {
          orderedItemBean.set(DOC_PRESC_ID, Integer.valueOf(docPrescIdObject.toString()));
        }

        String bedType = (String) headerInformation.get(BED_TYPE);
        String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
        Integer centerId = (Integer) headerInformation.get("center_id");
        String testId = (String) ((Map<String, Object>) orderedItemList[i]).get("tests_item_id");
        BasicDynaBean test = getMasterChargesBean(testId, bedType, ratePlanId, centerId);
        if ((Boolean) test.get("applicable")) {
          setHeaderProperties(orderedItemBean, headerInformation, username, isMultivisitPackage,
              (Map) orderedItemList[i]);
          orderBeanList.add(orderedItemBean);
        }
      }
    }
    return orderBeanList;
  }

  /**
   * This Function performs the data collection and insertion into
   * table(bill_charge, bill_activity_charge, bill_chare_claim).
   *
   * @param chargeable          the chargeable
   * @param headerInformation   the header information
   * @param orderBean           the order bean
   * @param bill                the bill
   * @param preAuthIds          the pre auth ids
   * @param preAuthModeIds      the pre auth mode ids
   * @param planIds             the plan ids
   * @param condDoctorId        the cond doctor id
   * @param activityCode        the activity code
   * @param centerId            the center id
   * @param isMultivisitPackage the is multivisit package
   * @param testItemDetails     the test item details
   * @return the basic dyna bean
   * @throws ParseException the parse exception
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public List<BasicDynaBean> insertOrderCharges(boolean chargeable, BasicDynaBean headerInformation,
      BasicDynaBean orderBean, BasicDynaBean bill, String[] preAuthIds, Integer[] preAuthModeIds,
      int[] planIds, String condDoctorId, String activityCode, Integer centerId,
      Boolean isMultivisitPackage, Map<String, Object> testItemDetails) throws ParseException {

    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    Integer priorAuthItemId = null;
    Long patPendingPrescId = null;
    if (testItemDetails != null) {
      priorAuthItemId = (Integer) testItemDetails.get(prefix + "_prior_auth_item_id");
      patPendingPrescId = (testItemDetails.get(prefix + "_pat_pending_presc_id") != null
          && !"null".equals(testItemDetails.get(prefix + "_pat_pending_presc_id")))
          ? Long.valueOf(testItemDetails.get(prefix + "_pat_pending_presc_id").toString()) : null;
    }
    // Update pending prescriptions status for prior_auth items if
    // mod_pat_pending_prescription
    // module is enabled
    boolean modPatPendingPres = modulesActivatedService
        .isModuleActivated("mod_pat_pending_prescription");
    if (modPatPendingPres) {
      if (null != patPendingPrescId) {
        pendingPrescriptionsService.updatePendingPrescriptionStatus(patPendingPrescId.toString(),
            "O");
      } else if (priorAuthItemId != null) {
        pendingPrescriptionsService.updatePendingPrescriptionStatusWithPreauthId(
            (int) priorAuthItemId, "O");
      }
    }
    BasicDynaBean masterCharge = getMasterChargesBean((String) orderBean.get(TEST_ID), bedType,
        ratePlanId, centerId);

    boolean condApplicable = (Boolean) masterCharge.get(CONDUCTION_APPLICABLE);
    List<BasicDynaBean> chargesList = null;

    if (chargeable) {
      BigDecimal charge;
      BigDecimal discount;
      Map<String,Boolean> properties = null;
      if (isMultivisitPackage) {
        charge = new BigDecimal(String.valueOf(testItemDetails.get(prefix + "_act_rate")));
        discount = new BigDecimal(String.valueOf(testItemDetails.get(prefix + "_discount")));
        BasicDynaBean packageBean = multiVisitRepository.findByKey("package_id",
            (Integer) testItemDetails.get("tests_package_id"));
        masterCharge.set("allow_rate_increase",
            (Boolean) packageBean.get("allow_rate_increase"));
        masterCharge.set("allow_rate_decrease",
            (Boolean) packageBean.get("allow_rate_decrease"));
        masterCharge.set("charge", charge);
        masterCharge.set("discount", discount);
      }

      BigDecimal quantity = BigDecimal.ONE;
      HashMap<String, Object> otherParams = new HashMap<>();
      otherParams.put("visit_type", bill.get("visit_type"));
      if (null != testItemDetails && testItemDetails.get(prefix + "_package_id") != null) {
        otherParams.put("package_id", testItemDetails.get(prefix + "_package_id"));
      }
      chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId, otherParams);
      int prescId = (Integer) orderBean.get(PRESCRIBED_ID);
      String presDrId = (String) orderBean.get(PRES_DOCTOR);
      if (testItemDetails != null && testItemDetails.get("posted_date") != null
          && !testItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) testItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId,
            new Timestamp(postedDate.getTime()), (String) orderBean.get("remarks"),
            testItemDetails);
      } else {
        Timestamp postedDate = (Timestamp) orderBean.get(PRESCRIBED_DATE);
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate,
            (String) orderBean.get("remarks"), testItemDetails);
      }
    }
    return chargesList;
  }

  /**
   * Used in Case of Packages (Package Package). It inserts package bill activity
   * charges.
   *
   * @param testBean           the test bean
   * @param headerInformation  the header information
   * @param centerId           the center id
   * @param packageChargeID    the package charge ID
   * @param conductingDoctorId the conducting doctor id
   */
  public void insertPackageBillActivityCharge(BasicDynaBean testBean,
      BasicDynaBean headerInformation, Integer centerId, String packageChargeID,
      String conductingDoctorId) {

    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    BasicDynaBean masterCharge = getMasterChargesBean(testBean.get(TEST_ID), bedType, ratePlanId,
        centerId);
    String testCategory = (String) masterCharge.get(CATEGORY);
    String pmtChargeHead = testCategory.equals(DEP_LAB) ? "LTDIA" : "RTDIA";
    boolean condApplicable = (Boolean) masterCharge.get(CONDUCTION_APPLICABLE);

    BasicDynaBean billActicityChargeBean = billActivityChargeService.getBillActivityChargeBean(
        packageChargeID, "DIA", pmtChargeHead, testBean.get(PRESCRIBED_ID).toString(),
        (String) testBean.get(TEST_ID), conductingDoctorId, condApplicable ? "N" : "Y", null);
    billActivityChargeService.insert(billActicityChargeBean);
  }

  /**
   * Returns the list of diagnostics item details by passing there id.
   *
   * @param entityIdList the entity id list
   * @param paramMap     the param map
   * @return the item details
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return testOrderItemRepository.getItemDetails(entityIdList);
  }

  /**
   * Returns the list of all test ordered for a given visit id.
   *
   * @param parameters the parameters
   * @return the ordered items
   */
  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    if (parameters.get("operation_id") != null) {
      return Collections.emptyList();
    }
    return testOrderItemRepository.getOrderedItems((String) parameters.get("visit_id"),
        (Boolean) parameters.get("package_ref"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#getEditBean(java.
   * util.Map)
   */
  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, item.get("order_id"));
    bean.set("remarks", item.get("remarks"));
    bean.set(PRES_DOCTOR, item.get("prescribed_doctor_id"));
    bean.set("priority", item.get("urgent"));
    bean.set(CLINICAL_NOTES, item.get(CLINICAL_NOTES));
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#getCancelBean(java.
   * util.Map)
   */
  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    BasicDynaBean bean = getCancelBean(item.get("order_id"));
    bean.set(CLINICAL_NOTES, item.get(CLINICAL_NOTES));
    bean.set("cancel_reason", item.get("cancelReason"));
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#getCancelBean(java.
   * lang.Object)
   */
  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, orderId);
    bean.set("conducted", "X");
    bean.set("cancelled_by", sessionService.getSessionAttributes().get("userId"));
    bean.set("cancel_date", DateUtil.getCurrentDate());
    bean.set(DOC_PRESC_ID, null);
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#toItemBean(java.
   * util.Map, org.apache.commons.beanutils.BasicDynaBean, java.lang.String,
   * java.util.Map, java.lang.String[], java.lang.Integer[],
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) {

    List<Object> newPreAuths = orderedItemAuths.get(NEW_PRE_AUTHS);
    List<Object> newPreAuthModesList = orderedItemAuths.get(NEW_PRE_AUTH_MODE);
    List<Object> secNewPreAuths = orderedItemAuths.get(SEC_NEW_PRE_AUTHS);
    List<Object> secNewPreAuthModesList = orderedItemAuths.get(SEC_NEW_PRE_AUTH_MODE);

    Object priAuthId = item.get("prior_auth_id");
    if (priAuthId == null || ((String) (priAuthId)).equals("")) {
      if (preAuthIds != null && preAuthIds.length != 0 && preAuthIds[0] != null) {
        newPreAuths.add(preAuthIds[0]);
      } else {
        newPreAuths.add(null);
      }
    } else {
      newPreAuths.add((String) (priAuthId));
    }

    Object priAuthModeId = item.get("prior_auth_mode_id");
    if (priAuthModeId == null || ((Integer) priAuthModeId) == 0) {
      if (preAuthModeIds != null && preAuthModeIds.length != 0 && preAuthModeIds[0] != null
          && preAuthModeIds[0] != 0) {
        newPreAuthModesList.add(preAuthModeIds[0]);
      } else {
        newPreAuthModesList.add(1);
      }
    } else {
      newPreAuthModesList.add(priAuthModeId);
    }

    Object secAuthId = item.get("sec_prior_auth_id");
    if (secAuthId == null || ((String) secAuthId).equals("")) {
      if (preAuthIds != null && preAuthIds.length > 1 && preAuthIds[1] != null) {
        secNewPreAuths.add(preAuthIds[1]);
      } else {
        secNewPreAuths.add(null);
      }
    } else {
      secNewPreAuths.add((String) secAuthId);
    }

    Object secAuthModeId = item.get("sec_prior_auth_mode_id");
    if (secAuthModeId == null || (Integer) secAuthModeId == 0) {
      if (preAuthModeIds != null && preAuthModeIds.length > 1 && preAuthModeIds[1] != null
          && preAuthModeIds[1] != 0) {
        secNewPreAuthModesList.add(preAuthModeIds[1]);
      } else {
        secNewPreAuthModesList.add(1);
      }
    } else {
      secNewPreAuthModesList.add(secAuthModeId);
    }

    orderedItemAuths.get("conductingDoctorList").add((String) item.get("payee_doctor_id"));

    List errorList = new ArrayList();
    BasicDynaBean orderedItemBean = testOrderItemRepository.getBean();

    ConversionUtils.copyJsonToDynaBean(item, orderedItemBean, errorList, true);

    if (!errorList.isEmpty()) {
      throw new ConversionException(errorList);
    }

    Object mvp = item.get("multi_visit_package");
    Boolean isMultivisitPackage = mvp != null && !"".equals(mvp) ? Boolean.valueOf(mvp.toString())
        : false;
    orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);

    Object testUrgentId = item.get("urgent");
    orderedItemBean.set("priority", (String) testUrgentId);

    orderedItemBean = setItemBeanProperties(orderedItemBean, headerInformation, username,
        isMultivisitPackage, item, null);
    return orderedItemBean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#updateItemBeans(
   * java.util.List)
   */
  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    Map<String, Object> keys = new HashMap<>();
    List<Object> prescribedIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescribedIdsList.add(item.get(PRESCRIBED_ID));
    }
    keys.put(PRESCRIBED_ID, prescribedIdsList);
    return testOrderItemRepository.batchUpdate(items, keys);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updateCancelStatusAndRefOrders(java .util.List, boolean, boolean,
   * java.util.List, java.util.Map)
   */
  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    if (cancel) {
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      testOrderItemRepository.updateCancelStatusToPatient(items, userName);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updateFinalizableItemCharges(java .util.List, java.util.Map)
   */
  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // For tests there is no update.
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * getOrderItemPrimaryKey()
   */
  @Override
  public String getOrderItemPrimaryKey() {
    return PRESCRIBED_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * getOrderItemActivityCode()
   */
  @Override
  public String getOrderItemActivityCode() {
    return ACTIVITY_CODE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * getPrescriptionDocKey()
   */
  @Override
  public String getPrescriptionDocKey() {
    return PRES_DOCTOR;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.order.master.OrderItemService#insertOrders(java.
   * util.List, java.lang.Boolean, java.util.Map, java.util.Map, java.util.List)
   */
  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageIdsList) throws ParseException, IOException {
    if (itemsMapsList.isEmpty()) {
      return Collections.emptyList();
    }
    BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
    String username = (String) billInfoMap.get("user_name");
    String[] preAuthIds = (String[]) billInfoMap.get("pre_auth_ids");
    Integer[] preAuthModeIds = (Integer[]) billInfoMap.get("pre_auth_mode_ids");
    BasicDynaBean bill = (BasicDynaBean) billInfoMap.get("bill");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int[] planIds = (int[]) billInfoMap.get("plan_ids");

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, null);

    List<BasicDynaBean> insertItemsList = new ArrayList<>();
    for (BasicDynaBean itemBean : itemList) {
      // Item can be null because test may not be applicable.
      if (itemBean != null) {
        insertItemsList.add(itemBean);
      }
    }
    testOrderItemRepository.batchInsert(insertItemsList);

    updateTestDocuments(itemsMapsList, itemList);

    List<Object> ismvpList = billItemAuthMap.get("isMultivisitPackageList");
    List<Object> condDocList = billItemAuthMap.get("conductingDoctorList");
    for (int index = 0; index < itemList.size(); index++) {
      BasicDynaBean testBean = itemList.get(index);
      if (testBean == null) {
        continue;
      }
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> testItemDetails = (Map<String, Object>) itemsMapsList.get(index);

      Boolean isMultivisitPackage = (Boolean) ismvpList.get(index);
      if (!condDocList.isEmpty() && index < condDocList.size()) {
        condDoctorId = (String) condDocList.get(index);
      }

      if (testBean.get(DOC_PRESC_ID) != null) {
        updatePrescription("O", (Integer) testBean.get(DOC_PRESC_ID));
      }

      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      BasicDynaBean mainChargeBean = null;
      if (null != bill) {
        List<BasicDynaBean> chargesList = insertOrderItemCharges(chargeable, headerInformation,
            testBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, condDoctorId,
            ACTIVITY_CODE, centerId, isMultivisitPackage, testItemDetails);
        mainChargeBean = chargesList == null ? null : chargesList.get(0);
      }

      String packageIdString = testItemDetails.get("package_id") != null
          ? testItemDetails.get("package_id").toString()
          : null;
      Integer packageId = packageIdString != null && !"".equals(packageIdString)
          ? Integer.parseInt(packageIdString)
          : null;
      String packageObIdString = testItemDetails.get("package_ob_id") != null
          ? testItemDetails.get("package_ob_id").toString()
          : null;
      Integer packObId = packageObIdString != null && !"".equals(packageObIdString)
          ? Integer.parseInt(packageObIdString)
          : null;
      Boolean isMultiVisitPackItem = (Boolean) testItemDetails.get("multi_visit_package");
      Integer patientPackageId = null;
      if (patientPackageIdsList != null && patientPackageIdsList.size() >= index) {
        patientPackageId = (Integer) patientPackageIdsList.get(index);
      }
      String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
      if ((packageId != null) && (packObId != null) && (patientPackageId != null)
          && !isMultiVisitPackItem) {
        orderSetsOrderItemService.insertIntoPatientPackageContentAndConsumed(patientPackageId,
            packObId, packageId, (String) testBean.get(TEST_ID), 1,
            (Integer) testBean.get(PRESCRIBED_ID), chargeId, username, "tests");
      }
    }
    return itemList;
  }

  /**
   * Update test documents.
   *
   * @param itemsMap the items map
   * @param itemList the item list
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @SuppressWarnings("unchecked")
  public void updateTestDocuments(List<Object> itemsMap, List<BasicDynaBean> itemList)
      throws IOException {

    Map<String, String[]> params = new HashMap<>();
    List<String> deleteDocuments = new ArrayList<>();
    List<BasicDynaBean> insertedDocuments = new ArrayList<>();

    for (int index = 0; index < itemsMap.size(); index++) {
      BasicDynaBean bean = (itemList.size() <= index + 1) ? itemList.get(index) : null;
      if (bean == null) {
        continue;
      }
      Map<String, Object> item = (Map<String, Object>) itemsMap.get(index);

      List<Map<String, Object>> documentList = (List<Map<String, Object>>) item.get("documents");
      if (documentList == null) {
        documentList = (List<Map<String, Object>>) item.get("tests_documents");
      }

      if (documentList != null) {
        Integer prescribedId = (Integer) bean.get(PRESCRIBED_ID);
        for (Map<String, Object> document : documentList) {
          Integer docId = Integer.parseInt(document.get("doc_id").toString());
          if (document.get("deleted") != null && (Boolean) document.get("deleted")) {
            deleteDocuments.add(docId + "," + "doc_fileupload");
          } else {
            if (document.get(PRESCRIBED_ID) == null || "".equals(document.get(PRESCRIBED_ID))) {
              BasicDynaBean insertBean = testDocumentsService.getBean();
              insertBean.set(PRESCRIBED_ID, prescribedId);
              insertBean.set("doc_id", docId);
              insertBean.set("doc_name", document.get("doc_name"));
              insertBean.set("doc_date", DateUtil.getCurrentDate());
              insertBean.set("username", sessionService.getSessionAttributes().get("userId"));
              insertedDocuments.add(insertBean);
            }
          }
        }
      }
    }

    // Insert the doc Ids prescribed ids. for newly added documents.
    if (!insertedDocuments.isEmpty()) {
      testDocumentsService.insertDocuments(insertedDocuments);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * insertOrderItemCharges(boolean, org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String[],
   * java.lang.Integer[], int[], java.lang.String, java.lang.String,
   * java.lang.Integer, java.lang.Boolean, java.util.Map)
   */
  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    Integer priorAuthItemId = null;
    Long patPendingPrescId = null;

    if (orderItemDetails != null) {
      priorAuthItemId = (Integer) orderItemDetails.get("prior_auth_item_id");
      if (BooleanUtils.isTrue((Boolean) orderItemDetails.get("prescriptionOrder"))
          && priorAuthItemId == null
          && MapUtils.getInteger(orderItemDetails, "doc_presc_id") != null) {
        priorAuthItemId = preAuthItemsService
            .getPriorAuthItemIdFromPrescId((Integer) orderItemDetails.get("doc_presc_id"));
      }
      if (orderItemDetails.get("pat_pending_presc_id") != null
          && !orderItemDetails.get("pat_pending_presc_id").equals("null")) {
        patPendingPrescId = Long.parseLong((String) orderItemDetails.get("pat_pending_presc_id"));
      }
    }
    // Update pending prescriptions status for prior_auth items if
    // mod_pat_pending_prescription
    // module is enabled
    boolean modPatPendingPres = modulesActivatedService
        .isModuleActivated("mod_pat_pending_prescription");
    if (modPatPendingPres) {
      if (null != patPendingPrescId) {
        pendingPrescriptionsService.updatePendingPrescriptionStatus(patPendingPrescId.toString(),
            "O");
      } else if (priorAuthItemId != null) {
        pendingPrescriptionsService.updatePendingPrescriptionStatusWithPreauthId(
            (int) priorAuthItemId, "O");
      }
    }

    BasicDynaBean masterCharge = getMasterChargesBean((String) orderBean.get(TEST_ID), bedType,
        ratePlanId, centerId);

    boolean condApplicable = (Boolean) masterCharge.get(CONDUCTION_APPLICABLE);

    List<BasicDynaBean> chargesList = null;
    if (chargeable) {
      BigDecimal charge = BigDecimal.ZERO;
      BigDecimal discount  = BigDecimal.ZERO;
      if (isMultivisitPackage) {
        charge = new BigDecimal(String.valueOf(orderItemDetails.get("act_rate")));
        discount = BigDecimal.valueOf(Double.valueOf((String) orderItemDetails.get("discount")));
        masterCharge.set("charge", charge);
        masterCharge.set("discount", discount);
        BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
            (Integer) orderItemDetails.get("package_id"));
        masterCharge.set("allow_rate_increase", 
            (Boolean)componentDeatilBean.get("allow_rate_increase"));
        masterCharge.set("allow_rate_decrease", 
            (Boolean)componentDeatilBean.get("allow_rate_decrease"));
      }

      HashMap<String, Object> otherParams = new HashMap<>();
      otherParams.put("visit_type", bill.get("visit_type"));
      otherParams.put("item_excluded_from_doctor",
          orderItemDetails.get("item_excluded_from_doctor"));
      otherParams.put("item_excluded_from_doctor_remarks",
          orderItemDetails.get("item_excluded_from_doctor_remarks"));
      BigDecimal quantity = BigDecimal.ONE;
      if (orderItemDetails.get("package_id") != null) {
        otherParams.put("package_id", orderItemDetails.get("package_id"));
      }
      chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId, otherParams);
      int prescId = (Integer) orderBean.get(PRESCRIBED_ID);
      String presDrId = (String) orderBean.get(PRES_DOCTOR);
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId,
            new Timestamp(postedDate.getTime()), (String) orderBean.get("remarks"),
            orderItemDetails,bedType);
      } else {
        Timestamp postedDate = (Timestamp) orderBean.get(PRESCRIBED_DATE);
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate,
            (String) orderBean.get("remarks"), orderItemDetails,bedType);
      }
      if (isMultivisitPackage) {
        Object chargeId = chargesList.get(0).get("charge_id");
        Map<String, Object> mvpItem = (Map<String, Object>) orderItemDetails.get("mvp_item");
        boolean isOldMvp = mvpItem.get("is_old_mvp") != null 
            ? (boolean) mvpItem.get("is_old_mvp") : false;
        //MVP which is partially consumed in 12.3 and upgraded to 12.4
        //Not inserting the data into patient package consumed table
        if (!isOldMvp) {
          multiVisitPackageService.insertPatientPackageConsumed(
              orderItemDetails.get("patient_package_content_id"), 
              orderItemDetails.get("pack_ob_id"), 
              orderItemDetails.get("pat_package_id"),
              orderItemDetails.get("quantity"), chargeId, prescId,
              orderItemDetails.get("type"));
        }
      }
    }
    return chargesList;
  }

  /**
   * Cancel test.
   *
   * @param prescribedId the prescribed id
   * @return the int
   */
  public int cancelTest(Integer prescribedId) {
    BasicDynaBean bean = getBean();
    bean.set("cancelled_by", sessionService.getSessionAttributes().get("userId"));
    bean.set("conducted", "X");
    bean.set("cancel_date", DateUtil.getCurrentDate());
    bean.set(PRESCRIBED_ID, prescribedId);

    Map<String, Object> keys = new HashMap<>();
    keys.put(PRESCRIBED_ID, prescribedId);

    return testOrderItemRepository.update(bean, keys);
  }

  /**
   * Two things can happen in this method 1.Reverting reconduction of old test
   * which was reconducted 2.Reverting activity_id of bill_activity_table to old
   * activity_id which was reconducted
   *
   * @param recTestBean the rec test bean
   */
  public void onCancleReconductTest(BasicDynaBean recTestBean) {

    Object refPrescribedId = recTestBean.get("reference_pres");
    BasicDynaBean refTpBean = testOrderItemRepository.findByKey(PRESCRIBED_ID, refPrescribedId);
    // reference prescription if this is reconducting test
    if (refTpBean != null) {
      // this is the old test which is reconducted
      BasicDynaBean tvrBean = testVisitReportsService.findByKey("report_id",
          refTpBean.get("report_id"));
      String conducted = tvrBean != null && tvrBean.get("signed_off").equals("Y") ? "S"
          : (tvrBean == null) ? "P" : "C";
      refTpBean.set("conducted", conducted); // reverting reconduction
      Map<String, Object> keyMap = new HashMap<>();
      keyMap.put(PRESCRIBED_ID, refTpBean.get(PRESCRIBED_ID));
      testOrderItemRepository.update(refTpBean, keyMap);
    }

    // update bill activity to new activity id
    if (billActivityChargeService.getChargeId(ACTIVITY_CODE,
        String.valueOf(recTestBean.get(PRESCRIBED_ID))) != null) {
      BasicDynaBean activityBean = billActivityChargeService.getBean();
      activityBean.set("activity_id", String.valueOf(refPrescribedId));
      Map<String, Object> keysMap = new HashMap<>();
      keysMap.put("activity_id", String.valueOf(recTestBean.get(PRESCRIBED_ID)));
      keysMap.put("activity_code", ACTIVITY_CODE);
      billActivityChargeService.update(activityBean, keysMap);
    }
    // update activity_conducted to C in bill_charge,bill_activity_charge
    billActivityChargeService.updateActivityDetails(ACTIVITY_CODE, String.valueOf(refPrescribedId),
        null, "C", null);

    // update newPresc id to old prescid
    BasicDynaBean incomgSampleRegDetails = incomingSampleRegDetailsrepo.findByKey(PRESCRIBED_ID,
        refPrescribedId);
    if (incomgSampleRegDetails != null && !incomgSampleRegDetails.getMap().isEmpty()) {
      BasicDynaBean bean = incomingSampleRegDetailsrepo.getBean();
      bean.set(PRESCRIBED_ID, refPrescribedId);
      Map<String, Object> keyMap = new HashMap<>();
      keyMap.put(PRESCRIBED_ID, recTestBean.get(PRESCRIBED_ID));
      incomingSampleRegDetailsrepo.update(bean, keyMap);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * cancelChargesAndAssociatedCharges
   * (org.apache.commons.beanutils.BasicDynaBean, boolean, boolean,
   * java.lang.String)
   */
  @Override
  public void cancelChargesAndAssociatedCharges(BasicDynaBean item, boolean cancelCharges,
      boolean unlinkActivity, String chargeId) {

    BasicDynaBean cancleTestBean = testOrderItemRepository.findByKey(PRESCRIBED_ID,
        item.get(PRESCRIBED_ID));
    BasicDynaBean cancelChildTestBean = null;

    String childTestChargeId = null;
    String activityId = String.valueOf(item.get(getOrderItemPrimaryKey()));
    String userName = (String) sessionService.getSessionAttributes().get("userId");

    // handling to cancel the test from child center also for Internal Lab
    if (cancleTestBean.get(OUTSOURCE_DEST_PRESCRIBED_ID) != null
        && !cancleTestBean.get(OUTSOURCE_DEST_PRESCRIBED_ID).equals("")) {

      cancelChildTestBean = testOrderItemRepository.findByKey(PRESCRIBED_ID,
          cancleTestBean.get(OUTSOURCE_DEST_PRESCRIBED_ID));
      childTestChargeId = billActivityChargeService.getChargeId(ACTIVITY_CODE, activityId);
      cancelTest((Integer) cancelChildTestBean.get(PRESCRIBED_ID));
    }

    if (cancleTestBean.get("re_conduction") != null
        && (Boolean) cancleTestBean.get("re_conduction")) {
      onCancleReconductTest(cancleTestBean);
      if (cancelChildTestBean != null) {
        onCancleReconductTest(cancelChildTestBean);
      }
    } else {
      super.cancelChargesAndAssociatedCharges(item, cancelCharges, unlinkActivity, chargeId);
      if (cancelCharges && cancelChildTestBean != null) {
        billChargeService.cancelChargeUpdate(childTestChargeId, true, userName);
      } else if (unlinkActivity && cancelChildTestBean != null) {
        billChargeService.updateHasActivityStatus(childTestChargeId, false, true);
        billActivityChargeService.deleteActivity(ACTIVITY_CODE,
            String.valueOf(cancelChildTestBean.get(PRESCRIBED_ID)));
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updateAdditionalInfo(java.util.List, boolean, boolean, java.util.List,
   * java.util.Map)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void updateAdditionalInfo(List<BasicDynaBean> orders, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws IOException {

    List<Object> itemsMap = (List<Object>) itemInfoMap.get("items_map");

    if (cancel || cancelCharges || itemsMap == null || itemsMap.isEmpty()) {
      return;
    }

    updateTestDocuments(itemsMap, orders);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * insertPackageContent (org.apache.commons .beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String,
   * java.lang.Integer, org.apache.commons.beanutils.BasicDynaBean, java.util.Map,
   * java.lang.Integer)
   */
  @Override
  public void insertPackageContent(BasicDynaBean packageBean, BasicDynaBean packageItem,
      BasicDynaBean headerInformation, String username, Integer centerId,
      BasicDynaBean mainChargeBean, Map<String, Object> packageItemDetails, Integer index) {

    String packageChargeID = mainChargeBean != null ? (String) mainChargeBean.get("charge_id")
        : null;
    ArrayList<Object> packageConductingDoctorList = (ArrayList<Object>) packageItemDetails
        .get("package_conducting_doctor");

    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    BasicDynaBean genericPrefBean = genericPreferencesService.getAllPreferences();
    int prescId = testOrderItemRepository.getNextSequence();
    BasicDynaBean testBean = getBean();
    setBeanValue(testBean, TEST_ID, packageItem.get("activity_id"));
    BasicDynaBean masterTestCharges = getMasterChargesBean(testBean.get("test_id"), bedType,
        ratePlanId, centerId);
    if ((Boolean) masterTestCharges.get("applicable")) {

      setBeanValue(testBean, PRESCRIBED_DATE, packageBean.get("presc_date"));
      setBeanValue(testBean, PRES_DOCTOR, packageBean.get("doctor_id"));
      setBeanValue(testBean, "conducted", "N");
      setBeanValue(testBean, "priority", "R");
      setBeanValue(testBean, "remarks", packageBean.get("remarks"));
      setBeanValue(testBean, "package_ref", packageBean.get("prescription_id"));
      setBeanValue(testBean, "prescription_type",
          ((String) masterTestCharges.get("house_status")).equals("I") ? "h" : "o");
      setBeanValue(testBean, "mr_no", (String) headerInformation.get("mr_no"));
      setBeanValue(testBean, "common_order_id", headerInformation.get("commonorderid"));
      setBeanValue(testBean, "user_name", username);
      setBeanValue(testBean, "pat_id", patientId);
      setBeanValue(testBean, PRESCRIBED_ID, prescId);
      setBeanValue(testBean, "curr_location_presc_id", prescId);

      setLabNo(genericPrefBean, masterTestCharges, testBean, headerInformation);
      setTokenNo(masterTestCharges, genericPrefBean, testBean, centerId);
      setReadyTime(testBean, centerId);
      setConducted(masterTestCharges, testBean);
      setConductionType(testBean, centerId);

      testOrderItemRepository.insert(testBean);

      String conductingDoctorId = null;
      for (Object packageConductingDoctor : packageConductingDoctorList) {
        Map<String, Object> packageConductingDoctorMap = 
            (Map<String, Object>) packageConductingDoctor;
        int actIndex = (Integer) packageConductingDoctorMap.get("package_activity_index");
        if (actIndex == index) {
          conductingDoctorId = (String) packageConductingDoctorMap.get("package_doctor_id");
          break;
        }
      }

      if (packageChargeID != null) {
        insertPackageBillActivityCharge(testBean, headerInformation, centerId, packageChargeID,
            conductingDoctorId);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updatePatientActivities(java.util .List)
   */
  @Override
  public void updatePatientActivities(List<BasicDynaBean> orders) {

    List<Object> orderNo = new ArrayList<>();
    List<Object> prescriptionType = new ArrayList<>();
    for (BasicDynaBean orderBean : orders) {
      BasicDynaBean patActivityBean = patientActivitiesService.getBean();
      patActivityBean.set("activity_status", "P");
      patActivityBean.set("completed_date", null);
      patActivityBean.set("completed_by", null);
      patActivityBean.set("order_no", null);

      prescriptionType.add("I");
      orderNo.add(orderBean.get(PRESCRIBED_ID));
    }

    Map<String, Object> keys = new HashMap<>();
    keys.put("order_no", orderNo);
    keys.put("prescription_type", prescriptionType);

    List<BasicDynaBean> updateBeans = new ArrayList<>();
    patientActivitiesService.batchUpdate(updateBeans, keys);
  }

}
