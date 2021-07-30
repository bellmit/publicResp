package com.insta.hms.core.clinical.order.doctoritems;

import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.insta.hms.common.ConsulationTokenGenerator;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTransactionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.order.ordersets.OrderSetsOrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.prescriptions.PatientPrescriptionsRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.organization.OrganizationService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeMappingsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This Class does all doctor order item entries.
 * 
 * @author ritolia
 *
 */
@Service
@Order(key = "Doctor", value = { "Doctor", "Doctor Package" }, prefix = "doctors")
public class DoctorOrderItemService extends OrderItemService {

  private static final SimpleDateFormat START_DATE_FORMAT 
      = new SimpleDateFormat("dd-MM-yyyy HH:mm");
  private static final String OPERATION_REF = "operation_ref";
  private static final String MULTI_VISIT_PACKAGE = "_multi_visit_package";
  private static final String DOCTOR_NAME = "doctor_name";
  private static final String CONSULTATION_ID = "consultation_id";
  private static final String VISITED_DATE = "visited_date";
  private static final String PRESCRIBED_DATE = "presc_date";
  private static final String CONSULTATION_TYPE_ID = "consultation_type_id";
  private static final String ACTIVITY_CODE = "DOC";
  private static final String PRESCRIPTION_DOCTOR_KEY = "presc_doctor_id";
  private static String prefix = "doctors";
  private DoctorConsultationRepository doctorOrderItemRepository;

  @LazyAutowired
  private DoctorService doctorService;

  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  @LazyAutowired
  private OrganizationService organizationService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private BillChargeTransactionService billChargeTransactionService;

  @LazyAutowired
  private RegistrationPreferencesService regPrefService;

  @LazyAutowired
  private ConsulationTokenGenerator consulationTokenGenerator;

  @LazyAutowired
  private PatientPrescriptionsRepository patientPrescriptionsRepository;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private RegistrationService regService;

  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  @LazyAutowired
  private OperationsService operationsService;

  @LazyAutowired
  private OperationOrderItemService operationOrderItemService;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private OrderSetsOrderItemService orderSetsOrderItemService;

  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  @LazyAutowired
  private PractitionerTypeMappingsService practitionerTypeMappingsService;

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
  
  private static final Logger log = LoggerFactory.getLogger(DoctorOrderItemService.class);

  /**
   * Instantiates a new doctor order item service.
   *
   * @param repository
   *          the DoctorOrderItemRepository
   */
  public DoctorOrderItemService(DoctorConsultationRepository repository) {
    super(repository, prefix, DOCTOR_NAME);
    doctorOrderItemRepository = repository;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    setBeanValue(orderBean, "mr_no", (String) headerInformation.get("mr_no"));
    setBeanValue(orderBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(orderBean, "username", username);
    setBeanValue(orderBean, "patient_id", headerInformation.get("patient_id"));
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));
    setBeanValue(orderBean, CONSULTATION_ID, doctorOrderItemRepository.getNextSequence());
    setBeanValue(orderBean, DOCTOR_NAME, orderedItemList.get("doctors_item_id"));
    setConsultationToken(orderBean, headerInformation);
    setStatus(orderBean);
    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_DATE);
    DynaProperty visitedDateDynaProperties = orderBean.getDynaClass().getDynaProperty(VISITED_DATE);
    setBeanValue(orderBean, PRESCRIBED_DATE, ConvertUtils
        .convert(orderedItemList.get("doctors_prescribed_date"), dynaProperties.getType()));
    setBeanValue(orderBean, VISITED_DATE, ConvertUtils
        .convert(orderedItemList.get("doctors_start_date"), visitedDateDynaProperties.getType()));
    setBeanValue(orderBean, "presc_doctor_id", orderedItemList.get("doctors_prescribed_doctor_id"));
  }

  /**
   * Returns the Operation Master Bean.
   *
   * @param orderBean
   *          the order bean
   * @param headerInformation
   *          the header information
   * @return the op master bean
   * @throws SQLException
   *           the SQL exception
   */
  @SuppressWarnings("unused")
  private BasicDynaBean getOpMasterBean(BasicDynaBean orderBean, BasicDynaBean headerInformation)
      throws SQLException {
    BasicDynaBean opMasterBean = null;
    HashMap<Integer, BasicDynaBean> operationBeanMap = new HashMap<Integer, BasicDynaBean>();
    if (null != orderBean.get(OPERATION_REF)) {
      opMasterBean = operationBeanMap.get(orderBean.get(OPERATION_REF));
      if (opMasterBean == null) {
        GenericDAO opDao = new GenericDAO("bed_operation_schedule");
        BasicDynaBean opBean = opDao.findByKey("prescribed_id", orderBean.get(OPERATION_REF));
        String operId = (String) opBean.get("operation_name");
        opMasterBean = new OperationMasterDAO().getOperationChargeBean(operId,
            (String) headerInformation.get("bed_type"),
            (String) headerInformation.get("bill_rate_plan_id"));
      }
    }
    return opMasterBean;
  }

  /**
   * Set the Consultation Token.
   *
   * @param orderBean
   *          the order bean
   * @param headerInformation
   *          the header information
   */
  public void setConsultationToken(BasicDynaBean orderBean, BasicDynaBean headerInformation) {
    if (headerInformation.get("visit_type").equals("o")
        && consulationTokenGenerator.isTokenGenerationEnabled()) {
      int token = consulationTokenGenerator.generateToken((String) orderBean.get(DOCTOR_NAME));
      orderBean.set("consultation_token", new BigDecimal(token));
    }
  }

  /**
   * returns the Consultation Type for specific consultationId.
   *
   * @param consultationId
   *          the consultation id
   * @return the consultation type bean
   */
  private BasicDynaBean getConsultationTypeBean(int consultationId) {
    Map<String, Integer> params = new HashMap<String, Integer>();
    params.put(CONSULTATION_TYPE_ID, consultationId);
    return consultationTypesService.findByPk(params);
  }

  /**
   * Sets the status.
   *
   * @param orderBean
   *          the new status
   */
  public void setStatus(BasicDynaBean orderBean) {
    String otDocRole = (String) orderBean.get("ot_doc_role");
    if (otDocRole != null && !("").equals(otDocRole)) {
      setBeanValue(orderBean, "status", "U");
    } else if (null != orderBean.get("status") && orderBean.get("status").equals("")) {
      setBeanValue(orderBean, "status", "A");
    }
  }

  @Override
  public BasicDynaBean getMasterChargesBean(Object prefixId, String bedType, String ratePlanId,
      Integer centerId) {
    return doctorService.getDoctorCharges((String) prefixId, ratePlanId, bedType);
  }

  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object prefixId, String ratePlanId) {
    return doctorService.getAllDoctorCharges((String) prefixId, ratePlanId);
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    BigDecimal quantity = (BigDecimal) paramMap.get("quantity");

    return getChargesList(null, quantity, isInsurance, null, paramMap);
  }

  /**
   * Returns ot doctor charges list.
   * 
   * @param doctor the doctor
   * @param otDocRole the otDocRole
   * @param operationRates the operationRates
   * @param quantity the quantity
   * @param isInsurance the isInsurance
   * @return chargesList.
   */
  public List<BasicDynaBean> getOtDoctorCharges(BasicDynaBean doctor, String otDocRole,
      BasicDynaBean operationRates, BigDecimal quantity, boolean isInsurance,
      Boolean multiVisitPackage, Map<String, Object> orderItemDetails) {
    String desc = (String) doctor.get("doctor_name");
    int insuranceCategoryId = 0;

    BigDecimal doctorCharge = BigDecimal.ZERO;
    BigDecimal discount = BigDecimal.ZERO;

    // base charge is based on the operation
    if (operationRates != null) {
      // Removed the addition for assistant surgeon and co (Bug : HMS-27556)
      if (otDocRole.equals("SUOPE")) {
        doctorCharge = (BigDecimal) operationRates.get("surgeon_charge");
        discount = (BigDecimal) operationRates.get("surg_discount");
      } else if (otDocRole.equals("ANAOPE")) {
        doctorCharge = (BigDecimal) operationRates.get("anesthetist_charge");
        discount = (BigDecimal) operationRates.get("anest_discount");
      }
      desc = operationRates.get("operation_name") + "/" + desc;
      insuranceCategoryId = (Integer) operationRates.get("insurance_category_id");
    }

    // the doctor premium is based on the ot doc role, hardcoded to doctor
    // fields
    if (otDocRole.equals("COSOPE")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("co_surgeon_charge"));
      discount = discount.add((BigDecimal) doctor.get("co_surgeon_charge_discount"));
    } else if (otDocRole.equals("ASUOPE") || otDocRole.equals("AANOPE")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("assnt_surgeon_charge"));
      discount = discount.add((BigDecimal) doctor.get("assnt_surgeon_charge_discount"));
    } else if (otDocRole.equals("IPDOC")) {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("doctor_ip_charge"));
      discount = discount.add((BigDecimal) doctor.get("doctor_ip_charge_discount"));
    } else {
      doctorCharge = doctorCharge.add((BigDecimal) doctor.get("ot_charge"));
      discount = discount.add((BigDecimal) doctor.get("ot_charge_discount"));
    }
    if ("ANAOPE".equals(otDocRole) && Boolean.TRUE.equals(multiVisitPackage)) {
      doctorCharge = new BigDecimal(String.valueOf(orderItemDetails.get("act_rate")));
      discount = new BigDecimal(String.valueOf(orderItemDetails.get("discount")));
    }

    boolean allowRateIncrease = false;
    boolean allowRateDecrease = false;

    if (operationRates != null) {
      allowRateIncrease = (Boolean) operationRates.get("allow_rate_increase");
      allowRateDecrease = (Boolean) operationRates.get("allow_rate_decrease");
    }

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("chargehead_id", otDocRole);
    BasicDynaBean chargeheadBean = chargeHeadsService.findByPk(keys);
    if (chargeheadBean != null) {
      allowRateIncrease &= (Boolean) chargeheadBean.get("allow_rate_increase");
      allowRateDecrease &= (Boolean) chargeheadBean.get("allow_rate_decrease");
    }

    String chargeGroup = "OPE";
    int subGroupId = (Integer) doctor.get("service_sub_group_id");
    BasicDynaBean chargeBean = billChargeService.setBillChargeBean(chargeGroup, otDocRole,
        doctorCharge, quantity, discount.multiply(quantity), (String) doctor.get("doctor_id"), desc,
        (String) doctor.get("dept_id"), subGroupId, insuranceCategoryId, isInsurance);
    
    chargeBean.set("allow_rate_increase", allowRateIncrease);
    chargeBean.set("allow_rate_decrease", allowRateDecrease);

    chargeBean.set("act_rate_plan_item_code", operationRates.get("item_code"));
    chargeBean.set("code_type", operationRates.get("code_type"));

    // set the conducting doctor ID same as the consulting doctor id.
    chargeBean.set("payee_doctor_id", doctor.get("doctor_id"));

    if (null != operationRates.get("op_id")) {
      chargeBean.set("op_id", operationRates.get("op_id"));
    }

    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    if (operationRates != null || operationRates.get("billing_group_id") != null) {
      chargeBean.set("billing_group_id", (Integer) operationRates.get("billing_group_id"));
    }

    if (null != orderItemDetails && orderItemDetails.get("package_id") != null) {
      chargeBean.set("package_id", orderItemDetails.get("package_id"));
    }
    chargesList.add(chargeBean);
    return chargesList;
  }

  /**
   * get Package Doctor Charges.
   * @param chargeType the chargeType
   * @param isInsurance the isInsurance
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getPackageDoctorCharges(String chargeType, boolean isInsurance) {
    BasicDynaBean consultationType = getConsultationTypeBean(Integer.parseInt(chargeType));
    Integer insuranceCategoryId = 0;
    if (consultationType.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) consultationType.get("insurance_category_id");
    }
    BasicDynaBean chrgdto = billChargeService.setBillChargeBean("DOC",
        (String) consultationType.get("charge_head"), BigDecimal.ZERO, BigDecimal.ONE,
        BigDecimal.ZERO, "Doctor", "Doctor", "Doctor", 0, insuranceCategoryId, isInsurance);

    List<BasicDynaBean> list = new ArrayList<BasicDynaBean>();
    if (consultationType != null || consultationType.get("billing_group_id") != null) {
      chrgdto.set("billing_group_id", (Integer) consultationType.get("billing_group_id"));
    }
    list.add(chrgdto);
    return list;
  }

  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean doctorBean, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {
    String bedType = (String) otherParams.get("bed_type");
    String orgId = (String) otherParams.get("org_id");
    String id = (String) otherParams.get("id");
    String chargeType = (String) otherParams.get("charge_type");

    String operationId = (String) otherParams.get("operation_id");
    List<BasicDynaBean> charges;
    if (id.equals("Doctor")) {
      charges = getPackageDoctorCharges(chargeType, isInsurance);
    } else {
      if (doctorBean == null) {
        doctorBean = getMasterChargesBean(id, bedType, orgId, null);
      }
      BasicDynaBean opMasterBean = null;

      if (operationId != null && !operationId.equals("")) {
        opMasterBean = operationsService.getOperationCharge(operationId, bedType, orgId);
        charges = getOtDoctorCharges(doctorBean, chargeType, opMasterBean, quantity, isInsurance,
            false, otherParams);
      } else {
        BasicDynaBean consTypeBean = getConsultationTypeBean(Integer.parseInt(chargeType));
        charges = getDoctorConsCharges(doctorBean, consTypeBean,
            organizationService.getOrgdetailsDynaBean(orgId), quantity, isInsurance, bedType,
            BigDecimal.ZERO, false, false, false, (String) otherParams.get("visit_type"), 
            BigDecimal.ZERO);
      }
    }

    setDoctorExclusionFlag(otherParams, charges);
    return charges;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List<BasicDynaBean> getOrderBean(Map<String, List<Object>> requestParams,
      BasicDynaBean headerInformation, String username, Map<String, List<Object>> orderedItemAuths,
      String[] preAuthIds, Integer[] preAuthModeIds) {

    List<BasicDynaBean> orderBeanList = new ArrayList<BasicDynaBean>();
    Object[] orderedItemList = requestParams.get(prefix).toArray();
    String multiVisitKey = prefix + MULTI_VISIT_PACKAGE;
    List errorList = new ArrayList();
    if (orderedItemList != null) {
      for (int i = 0; i < orderedItemList.length; i++) {
        orderedItemAuths.get("newPreAuths").add(null);
        orderedItemAuths.get("newPreAuthModesList").add(1);
        orderedItemAuths.get("secNewPreAuths").add(null);
        orderedItemAuths.get("secNewPreAuthModesList").add(1);
        orderedItemAuths.get("conductingDoctorList").add(null);
        BasicDynaBean orderedItemBean = doctorOrderItemRepository.getBean();
        ConversionUtils.copyJsonToDynaBeanPrefixed((Map) orderedItemList[i], orderedItemBean,
            errorList, prefix + "_");
        Boolean isMultivisitPackage = false;
        if (((Map<String, Object>) orderedItemList[i]) != null) {
          isMultivisitPackage = new Boolean(
              (Boolean) ((Map<String, Object>) orderedItemList[i]).get(multiVisitKey));
          orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);
        }
        
        Object docPrescId = ((Map<String, Object>) orderedItemList[i])
            .get(prefix + "_doc_presc_id");
        if (null != docPrescId && !StringUtils.EMPTY.equals(docPrescId)) {
          orderedItemBean.set("doc_presc_id", Integer.valueOf(docPrescId.toString()));
        }
        orderedItemBean.set("visit_mode", "I");
        String appId =  String.valueOf(((Map)orderedItemList[i]).get("appointment_id"));
        if (!StringUtils.isEmpty(appId)) {
          orderedItemBean.set("visit_mode", (String) ((Map)orderedItemList[i]).get("visit_mode"));
        }

        setHeaderProperties(orderedItemBean, headerInformation, username, isMultivisitPackage,
            (Map) orderedItemList[i]);
        orderBeanList.add(orderedItemBean);
      }
    }
    return orderBeanList;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public List<BasicDynaBean> insertOrderCharges(boolean chargeable, BasicDynaBean headerInformation,
      BasicDynaBean orderBean, BasicDynaBean bill, String[] preAuthIds, Integer[] preAuthModeIds,
      int[] planIds, String condDoctorId, String activityCode, Integer centerId,
      Boolean isMultivisitPackage, Map<String, Object> orderItemDetails) {

    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    
    String cashRateRatePlanId = (String) centerPreferencesService.getCenterPreferences(centerId)
        .get("pref_rate_plan_for_non_insured_bill");

    
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    String visitId = (String) bill.get("visit_id");
    Integer priorAuthItemId = null;
    Long patPendingPrescId = null;
    if (orderItemDetails != null) {
      priorAuthItemId = (Integer) orderItemDetails.get(prefix + "_prior_auth_item_id");
      patPendingPrescId = (orderItemDetails.get(prefix + "_pat_pending_presc_id") != null)
          ? Long.valueOf(orderItemDetails.get(prefix + "_pat_pending_presc_id").toString()) : null;
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
    List<BasicDynaBean> chargesList = null;

    if (chargeable) {
      BasicDynaBean masterCashRateCharge = null;
      if (!StringUtils.isEmpty(cashRateRatePlanId)) {
        masterCashRateCharge = getMasterChargesBean((String) orderBean.get(DOCTOR_NAME),
            bedType, cashRateRatePlanId, centerId);        
      }
      BasicDynaBean masterCharge = getMasterChargesBean((String) orderBean.get(DOCTOR_NAME),
          bedType, ratePlanId, centerId);

      BigDecimal charge = BigDecimal.ZERO;
      BigDecimal discount = BigDecimal.ZERO;
      Boolean allowRateIncrease = false;
      Boolean allowRateDecrease = false;
      if (isMultivisitPackage) {
        charge =
            new BigDecimal(String.valueOf(orderItemDetails.get(prefix + "_act_rate")));
        discount =
            new BigDecimal(String.valueOf(orderItemDetails.get(prefix + "_discount")));
        BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
            (Integer) orderItemDetails.get(prefix + "_package_id"));
        allowRateIncrease = (Boolean)componentDeatilBean.get("allow_rate_increase");
        allowRateDecrease = (Boolean)componentDeatilBean.get("allow_rate_decrease");
      }

      String consType = (String) orderBean.get("head");
      BasicDynaBean consTypeBean = getConsultationTypeBean(Integer.parseInt(consType));
      BasicDynaBean orgDetails = organizationService.getOrgdetailsDynaBean(ratePlanId);
      BasicDynaBean cashRateOrgDetails = null;
      if (!StringUtils.isEmpty(cashRateRatePlanId)) {
        cashRateOrgDetails = organizationService.getOrgdetailsDynaBean(cashRateRatePlanId);
      }
      chargesList = getDoctorConsCharges(masterCharge, consTypeBean, orgDetails, BigDecimal.ONE,
          isInsurance, bedType, charge, allowRateIncrease, allowRateDecrease, isMultivisitPackage,
          (String) bill.get("visit_type"), discount);
      List<BasicDynaBean> cashChargeList = null;
      if (masterCashRateCharge != null) {
        cashChargeList = getDoctorConsCharges(masterCashRateCharge, consTypeBean,
            cashRateOrgDetails, BigDecimal.ONE, false, bedType, charge, allowRateIncrease,
            allowRateDecrease, isMultivisitPackage, (String) bill.get("visit_type"), 
            discount);        
      }

      boolean modEclaim = modulesActivatedService
          .isModuleActivated("mod_eclaim");
      BasicDynaBean regPrefenence = regPrefService.getRegistrationPreferences();
      if (modEclaim && regPrefenence.get("doc_eandm_codification_required") != null
          && regPrefenence.get("doc_eandm_codification_required").equals("Y")) {
        BasicDynaBean visitbean = regService.getPatientVisitDetailsBean(visitId);
        String opType = (String) visitbean.get("op_type");
        if (opType != null && opType.equals("F")) {
          String followUpCode = (String) regPrefenence.get("default_followup_eandm_code");
          if (followUpCode != null && !followUpCode.equals("")) {
            Map fields = new HashMap();
            fields.put(CONSULTATION_ID, orderBean.get(CONSULTATION_ID));
            BasicDynaBean bean = doctorOrderItemRepository.getBean();
            bean.set("description", doctorService.getCodeDesc(followUpCode, "E&M"));
            doctorOrderItemRepository.update(bean, fields);
          }
        }
      }
      if (orderItemDetails != null) {
        Integer packageId = (Integer) orderItemDetails.get(prefix + "_package_id");
        if (packageId != null) {
          chargesList.get(0).set("package_id", packageId);
        }
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate;
        try {
          postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        } catch (ParseException exception) {
          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("posted_date", "exception.invalid.value",
              Arrays.asList(StringUtil.prettyName("posted_date")));
          throw new ValidationException(errorMap);
        }

        insertOrderBillCharges(chargesList, activityCode, "Y",
            (Timestamp) orderBean.get(VISITED_DATE), null, null, bill, orderBean, planIds,
            (Integer) orderBean.get(CONSULTATION_ID), (String) orderBean.get("presc_doctor_id"),
            new Timestamp(postedDate.getTime()), (String) orderBean.get("remarks"),
            orderItemDetails,cashChargesToTransactionCharges(cashChargeList));
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y",
            (Timestamp) orderBean.get(VISITED_DATE), null, null, bill, orderBean, planIds,
            (Integer) orderBean.get(CONSULTATION_ID), (String) orderBean.get("presc_doctor_id"),
            (Timestamp) orderBean.get(PRESCRIBED_DATE), (String) orderBean.get("remarks"),
            orderItemDetails,cashChargesToTransactionCharges(cashChargeList));
      }
    }

    return chargesList;
  }

  /**
   * Gets the doctor cons charges.
   * 
   * @param doctor the doctor
   * @param consTypeBean the consTypeBean
   * @param orgDetails the orgDetails
   * @param quantity the quantity
   * @param isInsurance the isInsurance
   * @param bedType the bedType
   * @param doctorCharge the doctorCharge
   * @param isMultivisitPackage the isMultivisitPackage
   * @param visitType the visitType
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getDoctorConsCharges(BasicDynaBean doctor, BasicDynaBean consTypeBean,
      BasicDynaBean orgDetails, BigDecimal quantity, boolean isInsurance, String bedType,
      BigDecimal doctorCharge, Boolean allowRateIncrease, Boolean allowRateDecrease,  
      Boolean isMultivisitPackage, String visitType, BigDecimal discount) {

    String rpItemCode;
    String codeType;
    int consTypeId = (Integer) consTypeBean.get(CONSULTATION_TYPE_ID);

    BasicDynaBean consultationTypeCharge = getConsultationCharge(consTypeId, bedType,
        (String) orgDetails.get("org_id"));

    String docChargeType = (String) consTypeBean.get("doctor_charge_type");

    rpItemCode = consultationTypeCharge.get("item_code") != null
        ? (String) consultationTypeCharge.get("item_code") : null;
    codeType = consultationTypeCharge.get("code_type") != null
        ? (String) consultationTypeCharge.get("code_type") : null;

    if (!isMultivisitPackage) {
      doctorCharge = (BigDecimal) doctor.get(docChargeType);
      discount = (BigDecimal) doctor.get(docChargeType + "_discount");
      doctorCharge = doctorCharge.add((BigDecimal) consultationTypeCharge.get("charge"));
      discount = (consultationTypeCharge.get("discount") != null
          ? discount.add((BigDecimal) consultationTypeCharge.get("discount")) : discount);
    }
    String desc = (String) doctor.get(DOCTOR_NAME);
    String chargeGroup = "DOC";
    String consultationChargeHead = (String) consTypeBean.get("charge_head");
    BasicDynaBean chrgdto = billChargeService.setBillChargeBean(chargeGroup, consultationChargeHead,
        doctorCharge, quantity, discount.multiply(quantity), (String) doctor.get("doctor_id"), desc,
        (String) doctor.get("dept_id"), (Integer) consTypeBean.get("service_sub_group_id"),
        (Integer) consTypeBean.get("insurance_category_id"), isInsurance);
    chrgdto.set("act_rate_plan_item_code", rpItemCode);
    chrgdto.set("code_type", codeType);
    if (isMultivisitPackage) {
      chrgdto.set("allow_rate_increase", allowRateIncrease);
      chrgdto.set("allow_rate_decrease", allowRateDecrease);
    } else {
      chrgdto.set("allow_rate_increase", (Boolean) consTypeBean.get("allow_rate_increase"));
      chrgdto.set("allow_rate_decrease", (Boolean) consTypeBean.get("allow_rate_decrease"));
    }
    chrgdto.set(CONSULTATION_TYPE_ID, consTypeId);
    chrgdto.set("payee_doctor_id", (String) doctor.get("doctor_id"));
    String allowZeroClaimfor = (String) consTypeBean.get("allow_zero_claim_amount");
    if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
      chrgdto.set("allow_zero_claim", true);
    }
    if (consTypeBean != null || consTypeBean.get("billing_group_id") != null) {
      chrgdto.set("billing_group_id", (Integer) consTypeBean.get("billing_group_id"));
    }
    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    chargesList.add(chrgdto);
    return chargesList;
  }

  /**
   * Gets the consultation charge.
   *
   * @param consultationId
   *          the consultation id
   * @param bedType
   *          the bed type
   * @param ratePlan
   *          the rate plan
   * @return the consultation charge
   */
  public BasicDynaBean getConsultationCharge(int consultationId, String bedType, String ratePlan) {
    BasicDynaBean consChargeBean = doctorService.getConsultationCharges(consultationId, bedType,
        ratePlan);
    if (consChargeBean == null) {
      consChargeBean = doctorService.getConsultationCharges(consultationId, "GENERAL", "ORG0001");
    }
    return consChargeBean;
  }

  @Override
  public void updatePrescription(String status, Integer presId) {
    BasicDynaBean patientPrescriptionsBean = patientPrescriptionsRepository.getBean();
    patientPrescriptionsBean.set("patient_presc_id", presId);
    patientPrescriptionsBean.set("status", status);
    patientPrescriptionsBean.set("username", sessionService.getSessionAttributes().get("userId"));
    Map<String, Integer> patientPrescKey = new HashMap<String, Integer>();
    patientPrescKey.put("patient_presc_id", presId);
    patientPrescriptionsRepository.update(patientPrescriptionsBean, patientPrescKey);
  }

  /**
   * Insert package bill activity charge.
   *
   * @param doctorBean
   *          the doctor bean
   * @param headerInformation
   *          the header information
   * @param centerId
   *          the center id
   * @param packageChargeID
   *          the package charge ID
   * @param conductingDoctorId
   *          the conducting doctor id
   * @throws SQLException
   *           the SQL exception
   */
  public void insertPackageBillActivityCharge(BasicDynaBean doctorBean,
      BasicDynaBean headerInformation, Integer centerId, String packageChargeID,
      String conductingDoctorId) {

    String chargeHead;

    if (null != doctorBean.get("operation_ref")) {
      chargeHead = (String) doctorBean.get("ot_doc_role");
    } else {
      String consType = (String) doctorBean.get("head");
      BasicDynaBean consTypeBean = getConsultationTypeBean(Integer.parseInt(consType));
      chargeHead = (String) consTypeBean.get("charge_head");
    }

    BasicDynaBean billActicityChargeBean = billActivityChargeService.getBillActivityChargeBean(
        packageChargeID, "DOC", chargeHead, doctorBean.get(CONSULTATION_ID).toString(),
        (String) doctorBean.get(DOCTOR_NAME), (String) doctorBean.get(DOCTOR_NAME), "Y",
        (Timestamp) doctorBean.get(VISITED_DATE));

    billActivityChargeService.insert(billActicityChargeBean);
  }

  /**
   * It returns the doctor package. This is hack to support ordering doctor in case of packages.
   * 
   * @return basic dyna bean
   */
  public BasicDynaBean getDoctorPackageItemDetail() {
    return doctorOrderItemRepository.getDoctorPackage();
  }

  /**
   * Returns the list of doctors details by passing there doctor_id.
   * @param entityIdList the entityIdList
   * @param paramMap the paramMap
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    // if other resources will also start having the schedulable_by property then this column can be
    // moved to orderable_items table
    String schedulable = null;
    if (paramMap.get("doctor_schedulable") != null
        && !paramMap.get("doctor_schedulable").equals("")) {
      schedulable = (String) paramMap.get("doctor_schedulable");
    }
    return doctorOrderItemRepository.getDoctorDetails(entityIdList,
        (Integer) paramMap.get("center_id"), schedulable, 
        paramMap.get("schedule") != null 
        ? Boolean.valueOf((paramMap.get("schedule")).toString()) : false);
  }

  /**
   * Returns the list of doctor consultation ordered for given visit.
   */
  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    return doctorOrderItemRepository.getOrderedItems((String) parameters.get("visit_id"),
        (Integer) parameters.get("operation_id"), (Boolean) parameters.get("package_ref"));
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    BasicDynaBean bean = getBean();
    bean.set(CONSULTATION_ID, item.get("order_id"));
    bean.set("remarks", item.get("remarks"));
    bean.set(PRESCRIPTION_DOCTOR_KEY, item.get("prescribed_doctor_id"));
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    return getCancelBean(item.get("order_id"));
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    BasicDynaBean bean = getBean();
    bean.set(CONSULTATION_ID, orderId);
    bean.set("cancel_status", "C");
    bean.set("username", sessionService.getSessionAttributes().get("userId"));
    bean.set("doc_presc_id", null);
    return bean;
  }

  @Override
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemMap, BasicDynaBean operationBean) throws ParseException {

    setBeanValue(orderBean, "mr_no", (String) headerInformation.get("mr_no"));
    setBeanValue(orderBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(orderBean, "username", username);
    setBeanValue(orderBean, "patient_id", headerInformation.get("patient_id"));
    setBeanValue(orderBean, CONSULTATION_ID, doctorOrderItemRepository.getNextSequence());
    setBeanValue(orderBean, "visit_mode", "I");
    setBeanValue(orderBean, "doc_presc_id",
        !"".equals(orderedItemMap.get("doc_presc_id")) ? orderedItemMap.get("doc_presc_id") : null);
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));

    setDoctorId(orderBean, orderedItemMap);

    boolean isOperation = false;
    if (operationBean == null) {
      setConsultationToken(orderBean, headerInformation);
    } else {
      isOperation = setOperationRef(orderBean, operationBean);
      setBeanValue(orderBean, "ot_doc_role", orderedItemMap.get("charge_head"));
    }

    setRemarks(orderBean, orderedItemMap);

    setChargeHead(orderBean, orderedItemMap);

    setStatus(orderBean);

    setPrescribedDate(orderBean, orderedItemMap);

    setVisitedDate(orderBean, orderedItemMap);

    setBeanValue(orderBean, PRESCRIPTION_DOCTOR_KEY, orderedItemMap.get("prescribed_doctor_id"));

    return orderBean;
  }

  private void setRemarks(BasicDynaBean orderBean, Map<String, Object> orderedItemMap) {
    if (orderBean.get("remarks") == null) {
      Object remarks = orderedItemMap.get("surgery_remarks");
      setBeanValue(orderBean, "remarks", remarks);
    }
  }

  private void setDoctorId(BasicDynaBean orderBean, Map<String, Object> orderedItemMap) {
    Object doctorId = orderedItemMap.get("item_id");
    doctorId = doctorId == null || "".equals(doctorId) ? orderedItemMap.get("anaesthetist_id")
        : doctorId;
    doctorId = doctorId == null || "".equals(doctorId) ? orderedItemMap.get("surgery_ot_doctor")
        : doctorId;
    setBeanValue(orderBean, DOCTOR_NAME, doctorId);
  }

  private void setChargeHead(BasicDynaBean orderBean, Map orderedItemMap) {
    if (orderBean.get("head") == null) {
      setBeanValue(orderBean, "head", orderedItemMap.get("charge_head"));
    }
  }

  private void setVisitedDate(BasicDynaBean orderBean, Map<String, Object> orderedItemMap) {
    Object date = orderedItemMap.get("from_date");
    if (date == null || "".equals(date)) {
      date = new Date();
    } else {
      try {
        date = START_DATE_FORMAT.parse((String) date);
        // Adding seconds to visited date, as start date does not have seconds, but we need to have
        // them to maintain consistency with registration flow
        date = DateUtils.addSeconds((Date) date,
            (int) DateUtils.getFragmentInSeconds(new Date(), Calendar.MINUTE));
      } catch (ParseException ex) {
        log.warn("Parse failed for start date", ex);
      }
    }
    DynaProperty visitedDateDynaProperties = orderBean.getDynaClass().getDynaProperty(VISITED_DATE);
    setBeanValue(orderBean, VISITED_DATE,
        ConvertUtils.convert(date, visitedDateDynaProperties.getType()));
  }

  private void setPrescribedDate(BasicDynaBean orderBean, Map<String, Object> orderedItemMap) {
    Object date = orderedItemMap.get("prescribed_date");
    if (date == null || "".equals(date)) {
      date = new Date();
    }

    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_DATE);
    setBeanValue(orderBean, PRESCRIBED_DATE, ConvertUtils.convert(date, dynaProperties.getType()));
  }

  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) throws ParseException {

    orderedItemAuths.get("newPreAuths").add(null);
    orderedItemAuths.get("newPreAuthModesList").add(1);
    orderedItemAuths.get("secNewPreAuths").add(null);
    orderedItemAuths.get("secNewPreAuthModesList").add(1);
    orderedItemAuths.get("conductingDoctorList").add(null);

    List errorList = new ArrayList();
    BasicDynaBean orderedItemBean = doctorOrderItemRepository.getBean();
    ConversionUtils.copyJsonToDynaBean(item, orderedItemBean, errorList, false);

    if (!errorList.isEmpty()) {
      throw new ConversionException(errorList);
    }
    Object mvp = item.get("multi_visit_package");
    Boolean isMultivisitPackage = mvp != null && !"".equals(mvp) ? Boolean.valueOf(mvp.toString())
        : false;
    orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);
    BasicDynaBean setOperationBean = null;
    String chargeGroup = (String) item.get("charge_group");
    if (chargeGroup != null && chargeGroup.equals("OPE")) {
      setOperationBean = operationBean;
    }
    setItemBeanProperties(orderedItemBean, headerInformation, username, isMultivisitPackage, item,
        setOperationBean);

    return orderedItemBean;
  }

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    Map<String, Object> keys = new HashMap<>();
    List<Object> consultationIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      consultationIdsList.add(item.get(CONSULTATION_ID));
    }
    keys.put(CONSULTATION_ID, consultationIdsList);
    return doctorOrderItemRepository.batchUpdate(items, keys);
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    if (cancel) {
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      doctorOrderItemRepository.updateCancelStatusToPatient(items, userName);
    }
  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // For doctors there is no update.
    return true;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    return CONSULTATION_ID;
  }

  @Override
  public String getOrderItemActivityCode() {
    return ACTIVITY_CODE;
  }

  @Override
  public String getPrescriptionDocKey() {
    return PRESCRIPTION_DOCTOR_KEY;
  }

  @Override
  public boolean isDoctorPartOfPackage(BasicDynaBean item) {
    return item != null && item.get("package_ref") != null && !"".equals(item.get("package_ref"));
  }

  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageIdsList) throws ParseException {
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
    BasicDynaBean operationBean = (BasicDynaBean) billInfoMap.get("operationBean");
    Map<String, Object> operationIdsMap = (Map<String, Object>) billInfoMap.get("operationIdsMap");

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, operationBean);

    if (operationBean != null && operationIdsMap != null) {
      List<String> modulesActivated = securityService.getActivatedModules();
      if ((modulesActivated != null) && (modulesActivated.contains("mod_advanced_ot"))) {
        copySurgeonAndAnaestiatistDetails(itemList, operationIdsMap, chargeable);
      }
    }

    doctorOrderItemRepository.batchInsert(itemList);

    List<Object> ismvpList = billItemAuthMap.get("isMultivisitPackageList");
    List<Object> condDocList = billItemAuthMap.get("conductingDoctorList");
    for (int index = 0; index < itemList.size(); index++) {
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> doctorItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean doctorBean = itemList.get(index);

      Boolean isMultivisitPackage = (Boolean) ismvpList.get(index);
      if (!condDocList.isEmpty() && index < condDocList.size()) {
        condDoctorId = (String) condDocList.get(index);
      }
      if (doctorBean.get("doc_presc_id") != null) {
        updatePrescription("O", (Integer) doctorBean.get("doc_presc_id"));
      }

      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      BasicDynaBean mainChargeBean = null;
      List<BasicDynaBean> chargesList = null;
      if (null != bill) {
        chargesList = insertOrderItemCharges(chargeable, headerInformation,
            doctorBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, condDoctorId,
            ACTIVITY_CODE, centerId, isMultivisitPackage, doctorItemDetails, operationBean);
        mainChargeBean = chargesList == null ? null : chargesList.get(0);
      }

      Map<String, Object> mvpDetails = (Map<String, Object>) billInfoMap.get("mvp_details");
      if (null != mvpDetails && (Boolean) mvpDetails.get("multi_visit_package")
          && null != chargesList) {
        List<BasicDynaBean> contentbeans = multiVisitPackageService
            .getPatientPackageContentsForOperation(mvpDetails.get("pat_package_id"),
                mvpDetails.get("item_id"));
        for (BasicDynaBean charge : chargesList) {
          Object chargeId = charge.get("charge_id");
          Object chargeHead = charge.get("charge_head");
          Iterator<BasicDynaBean> itr = contentbeans.iterator();
          while (itr.hasNext()) {
            BasicDynaBean bean = itr.next();
            if (((String) bean.get("charge_head")).equals(chargeHead.toString())) {
              itr.remove();
              multiVisitPackageService
                  .insertPatientPackageConsumed(bean.get("patient_package_content_id"),
                  bean.get("package_content_id"),
                  mvpDetails.get("pat_package_id"), mvpDetails.get("quantity"), chargeId,
                  (Integer) mvpDetails.get("prescribed_id"), mvpDetails.get("type"));
              break;
            }
          }

        }
      }
      String packageIdString = doctorItemDetails.get("package_id") != null
          ? doctorItemDetails.get("package_id").toString() : null;
      Integer packageId = packageIdString != null && !"".equals(packageIdString)
          ? Integer.parseInt(packageIdString) : null;
      String packObIdString = doctorItemDetails.get("package_ob_id") != null
          ? doctorItemDetails.get("package_ob_id").toString() : null;
      Integer packObId = packObIdString != null && !"".equals(packObIdString)
          ? Integer.parseInt(packObIdString) : null;
      Boolean isMultiVisitPackItem = (Boolean) doctorItemDetails.get("multi_visit_package");
      Integer patientPackageId = null;
      if (patientPackageIdsList != null && patientPackageIdsList.size() >= index) {
        patientPackageId = (Integer) patientPackageIdsList.get(index);
      }
      String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
      if ((packageId != null) && (packObId != null) && (patientPackageId != null)
          && !isMultiVisitPackItem) {
        orderSetsOrderItemService.insertIntoPatientPackageContentAndConsumed(patientPackageId,
            packObId, packageId, (String) doctorBean.get("doctor_name"), 1,
            (Integer) doctorBean.get(CONSULTATION_ID), chargeId, username, "doctor");
      }
    }
    return itemList;
  }

  /**
   * Copy surgeon and anaestiatist details.
   *
   * @param itemList
   *          the item list
   * @param operationBean
   *          the operation bean
   * @param bedType
   *          the bed type
   * @param orgId
   *          the org id
   */
  private void copySurgeonAndAnaestiatistDetails(List<BasicDynaBean> itemList,
      Map<String, Object> operationIdsMap, boolean chargeable) {
    if (operationIdsMap == null) {
      return;
    }

    for (BasicDynaBean itemBean : itemList) {
      operationOrderItemService.copySurgeonAndAnaestiatistDetails(itemBean, chargeable,
          operationIdsMap);
    }

  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    return insertOrderItemCharges(chargeable, headerInformation, orderBean, bill, preAuthIds,
        preAuthModeIds, planIds, condDoctorId, activityCode, centerId, isMultivisitPackage,
        orderItemDetails, null);
  }

  /**
   * Insert order item charges.
   *
   * @param chargeable
   *          the chargeable
   * @param headerInformation
   *          the header information
   * @param orderBean
   *          the order bean
   * @param bill
   *          the bill
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param planIds
   *          the plan ids
   * @param condDoctorId
   *          the cond doctor id
   * @param activityCode
   *          the activity code
   * @param centerId
   *          the center id
   * @param isMultivisitPackage
   *          the is multivisit package
   * @param orderItemDetails
   *          the order item details
   * @param operationBean
   *          the operation bean
   * @return the list
   * @throws ParseException
   *           the parse exception
   */
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails, BasicDynaBean operationBean) throws ParseException {
    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    String cashRateRatePlanId = (String) centerPreferencesService.getCenterPreferences(centerId)
        .get("pref_rate_plan_for_non_insured_bill");
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    String visitId = (String) bill.get("visit_id");
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
      patPendingPrescId = (Long) orderItemDetails.get("pat_pending_presc_id");
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
    String chargeGroup = (String) orderItemDetails.get("charge_group");
    List<BasicDynaBean> chargesList = null;
    List<BasicDynaBean> cashRateChargesList = null;
    if (chargeable) {
      BasicDynaBean masterCharge = getMasterChargesBean((String) orderBean.get(DOCTOR_NAME),
          bedType, ratePlanId, centerId);
      BasicDynaBean cashRateMasterCharge = null;
      if (!StringUtils.isEmpty(cashRateRatePlanId)) {
        cashRateMasterCharge = getMasterChargesBean((String) orderBean.get(DOCTOR_NAME), bedType,
            cashRateRatePlanId, centerId);
      }
      BasicDynaBean opMasterBean = null;
      if (operationBean != null && chargeGroup.equals("OPE")) {
        opMasterBean = operationsService
            .getOperationCharge((String) operationBean.get("operation_name"), bedType, ratePlanId);
      }

      if (opMasterBean != null) {
        // This is an operation related doctor order.
        chargesList = getOtDoctorCharges(masterCharge, (String) orderBean.get("ot_doc_role"),
            opMasterBean, BigDecimal.ONE, isInsurance, isMultivisitPackage, orderItemDetails);
      } else {
        BigDecimal charge = BigDecimal.ZERO;
        BigDecimal discount  = BigDecimal.ZERO;
        Boolean allowRateIncrease = false;
        Boolean allowRateDecrease = false;
        if (Boolean.TRUE.equals(isMultivisitPackage) 
            && orderItemDetails.get("package_id") != null) {
          charge = new BigDecimal(String.valueOf(orderItemDetails.get("act_rate")));
          discount = BigDecimal.valueOf(Double.valueOf((String) orderItemDetails.get("discount")));
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
              Integer.parseInt(orderItemDetails.get("package_id").toString()));
          allowRateIncrease = (Boolean)componentDeatilBean.get("allow_rate_increase");
          allowRateDecrease = (Boolean)componentDeatilBean.get("allow_rate_decrease");
        }

        String consType = (String) orderBean.get("head");
        BasicDynaBean consTypeBean = getConsultationTypeBean(Integer.parseInt(consType));
        BasicDynaBean orgDetails = organizationService.getOrgdetailsDynaBean(ratePlanId);
        if (!StringUtils.isEmpty(cashRateRatePlanId)) {
          BasicDynaBean cashRateOrgDetails = organizationService
              .getOrgdetailsDynaBean(cashRateRatePlanId);          
          cashRateChargesList = getDoctorConsCharges(cashRateMasterCharge,
              consTypeBean, cashRateOrgDetails, BigDecimal.ONE, false, bedType, charge, 
              allowRateIncrease, allowRateDecrease,isMultivisitPackage, 
              (String) bill.get("visit_type"),discount);          
        }
        chargesList = getDoctorConsCharges(masterCharge, consTypeBean, orgDetails, BigDecimal.ONE,
            isInsurance, bedType, charge, allowRateIncrease, allowRateDecrease, 
            isMultivisitPackage, (String) bill.get("visit_type"),discount);
        if (Boolean.TRUE.equals(isMultivisitPackage) && !chargesList.isEmpty()
            && orderItemDetails.get("package_id") != null) {
          (chargesList.get(0)).set("package_id", orderItemDetails.get("package_id"));
        }
        setDoctorExclusionFlag(orderItemDetails, chargesList);

        List<String> activatedModules = securityService.getActivatedModules();
        Boolean modAccumed = activatedModules.contains("mod_accumed");
        Boolean modEclaim = activatedModules.contains("mod_eclaim");
        BasicDynaBean regPrefenence = regPrefService.getRegistrationPreferences();
        if ((modAccumed || modEclaim)
            && regPrefenence.get("doc_eandm_codification_required") != null
            && regPrefenence.get("doc_eandm_codification_required").equals("Y")) {
          BasicDynaBean visitbean = regService.getPatientVisitDetailsBean(visitId);
          String opType = (String) visitbean.get("op_type");
          if (opType != null && opType.equals("F")) {
            String followUpCode = (String) regPrefenence.get("default_followup_eandm_code");
            if (followUpCode != null && !followUpCode.equals("")) {
              Map<String, Object> fields = new HashMap<>();
              fields.put(CONSULTATION_ID, orderBean.get(CONSULTATION_ID));
              BasicDynaBean bean = doctorOrderItemRepository.getBean();
              bean.set("description", doctorService.getCodeDesc(followUpCode, "E&M"));
              doctorOrderItemRepository.update(bean, fields);
            }
          }
        }
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, "Y",
            (Timestamp) orderBean.get(VISITED_DATE), null, null, bill, orderBean, planIds,
            (Integer) orderBean.get(CONSULTATION_ID), (String) orderBean.get("presc_doctor_id"),
            new Timestamp(postedDate.getTime()), (String) orderBean.get("remarks"),
            orderItemDetails,cashChargesToTransactionCharges(cashRateChargesList));
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y",
            (Timestamp) orderBean.get(VISITED_DATE), null, null, bill, orderBean, planIds,
            (Integer) orderBean.get(CONSULTATION_ID), (String) orderBean.get("presc_doctor_id"),
            (Timestamp) orderBean.get(PRESCRIBED_DATE), (String) orderBean.get("remarks"),
            orderItemDetails,cashChargesToTransactionCharges(cashRateChargesList));
      }
      if (Boolean.TRUE.equals(isMultivisitPackage)) {
        Object chargeId = chargesList.get(0).get("charge_id");
        Map<String, Object> mvpItem = (Map<String, Object>) orderItemDetails.get("mvp_item");
        Boolean isOldMvp = false;
        if (null != mvpItem) {
          isOldMvp = mvpItem.get("is_old_mvp") != null 
              ? (Boolean) mvpItem.get("is_old_mvp") : false;
        }
        //MVP which is partially consumed in 12.3 and upgraded to 12.4
        //Not inserting the data into patient package consumed table
        if (!isOldMvp) {
          multiVisitPackageService.insertPatientPackageConsumed(
              orderItemDetails.get("patient_package_content_id"),
              orderItemDetails.get("pack_ob_id"), orderItemDetails.get("pat_package_id"),
              orderItemDetails.get("quantity"),
              chargeId, orderBean.get(CONSULTATION_ID),
              orderItemDetails.get("type"));
        }
      }
    }

    return chargesList;
  }

  private List<BasicDynaBean> cashChargesToTransactionCharges(List<BasicDynaBean> cashCharges) {
    
    if (cashCharges == null) {
      return null;
    }
    
    List<BasicDynaBean> transactionCharges = new ArrayList<>();
    for (BasicDynaBean cashCharge : cashCharges) {
      BasicDynaBean transactionBean = billChargeTransactionService.getBean();
      transactionBean.set("bill_charge_id", cashCharge.get("charge_id"));
      transactionBean.set("cash_rate", cashCharge.get("act_rate"));
      transactionCharges.add(transactionBean);
    }
    return transactionCharges;
  }
  
  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    return doctorOrderItemRepository.getPackageRefOrders(visitId, prescriptionIdList);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insertPackageContent(BasicDynaBean packageBean, BasicDynaBean packageItem,
      BasicDynaBean headerInformation, String username, Integer centerId,
      BasicDynaBean mainChargeBean, Map<String, Object> packageItemDetails, Integer index) {

    ArrayList<Object> packageIdDoctorList = (ArrayList<Object>) packageItemDetails
        .get("package_id_for_doc");
    for (Object packageIdDoctormap : packageIdDoctorList) {
      Map<String, Object> packageIdDoctorMap = 
          (Map<String, Object>) packageIdDoctormap;
      Integer packageDoctorHead = Integer.parseInt((String) packageIdDoctorMap
                .get("package_doctor_head"));
      if (packageIdDoctorMap.get("package_visited_doctor") != null && packageIdDoctorMap
          .get("package_visited_doctor")
          .equals(packageItem.get("activity_id")) && packageDoctorHead
                  .equals(packageItem.get("consultation_type_id"))) {
        BasicDynaBean itemBean = doctorOrderItemRepository.getBean();
        setBeanValue(itemBean, "doctor_name",  packageIdDoctorMap
            .get("package_visited_doctor"));

        DynaProperty dynaProperties = itemBean.getDynaClass().getDynaProperty(VISITED_DATE);
        setBeanValue(itemBean, VISITED_DATE, ConvertUtils
            .convert( packageIdDoctorMap.get("package_doc_visit_date_time"), 
                dynaProperties.getType()));

        setBeanValue(itemBean, PRESCRIBED_DATE, packageBean.get(PRESCRIBED_DATE));
        setBeanValue(itemBean, "remarks", packageBean.get("remarks"));
        setBeanValue(itemBean, "head", packageIdDoctorMap
            .get("package_doctor_head"));
        setBeanValue(itemBean, "package_ref", packageBean.get("prescription_id"));
        setBeanValue(itemBean, "mr_no", (String) headerInformation.get("mr_no"));
        setBeanValue(itemBean, "common_order_id", headerInformation.get("commonorderid"));
        setBeanValue(itemBean, "username", username);
        setBeanValue(itemBean, "patient_id", headerInformation.get("patient_id"));
        setBeanValue(itemBean, CONSULTATION_ID, doctorOrderItemRepository.getNextSequence());

        setConsultationToken(itemBean, headerInformation);
        setStatus(itemBean);

        doctorOrderItemRepository.insert(itemBean);
        String packageChargeID = mainChargeBean != null ? (String) mainChargeBean.get("charge_id")
            : null;
        if (packageChargeID != null) {
          insertPackageBillActivityCharge(itemBean, headerInformation, centerId, 
              packageChargeID, null);
        }
      }
    }
  }

  public Integer getNextSequence() {
    return doctorOrderItemRepository.getNextSequence();
  }

  public int insert(BasicDynaBean bean) {
    return doctorOrderItemRepository.insert(bean);
  }

  private void setDoctorExclusionFlag(Map<String, Object> detailsMap,
      List<BasicDynaBean> chargesList) {
    if (("Y").equals(detailsMap.get("item_excluded_from_doctor"))
        || Boolean.TRUE.equals(detailsMap.get(
        "item_excluded_from_doctor"))) {
      chargesList.get(0).set("item_excluded_from_doctor", true);
      chargesList.get(0).set("item_excluded_from_doctor_remarks", detailsMap.get(
          "item_excluded_from_doctor_remarks"));
    } else if (("N").equals(detailsMap.get("item_excluded_from_doctor"))
        || Boolean.FALSE.equals(detailsMap.get(
        "item_excluded_from_doctor"))) {
      chargesList.get(0).set("item_excluded_from_doctor", false);
      chargesList.get(0).set("item_excluded_from_doctor_remarks", detailsMap.get(
          "item_excluded_from_doctor_remarks"));
    }
  }

  @Override
  public Map<String, Object> getCodeDetails(String itemId, String orgId) {
    BasicDynaBean orgDetails = consultationTypesService.getOrgDetails(itemId, orgId);
    Map<String, Object> codeDetails = new HashMap<>();
    codeDetails.put("ct_code", orgDetails.get("item_code"));
    codeDetails.put("code_type", orgDetails.get("code_type"));
    return codeDetails;
  }
}
