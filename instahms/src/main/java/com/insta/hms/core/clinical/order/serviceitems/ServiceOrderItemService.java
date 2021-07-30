package com.insta.hms.core.clinical.order.serviceitems;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.master.OrgDetailsRepository;
import com.insta.hms.core.clinical.order.ordersets.OrderSetsOrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.patientactivities.PatientActivitiesService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.prescriptions.PatientServicePrescriptionsService;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsRepository;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.extension.clinical.ivf.IVFCycleService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.services.ServiceOrgDetailsRepository;
import com.insta.hms.mdm.services.ServicesRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class does all service order items task.
 * 
 * @author ritolia
 */
@Service
@Order(key = "Service", value = { "Service" }, prefix = "services")
public class ServiceOrderItemService extends OrderItemService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceOrderItemService.class);

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The services repository. */
  @LazyAutowired
  private ServicesRepository servicesRepository;

  /** The bill activity charge service. */
  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  /** The service order item repository. */
  private ServiceOrderItemRepository serviceOrderItemRepository;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  @LazyAutowired
  PatientServicePrescriptionsService servicePrescService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The ivf cycle service. */
  @LazyAutowired
  private IVFCycleService ivfCycleService;

  /** The patient activities service. */
  @LazyAutowired
  private PatientActivitiesService patientActivitiesService;

  /** The order sets order item service. */
  @LazyAutowired
  private OrderSetsOrderItemService orderSetsOrderItemService;
  
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;
  
  /** The pending prescriptions service. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;
  
  /** Module activated service. */
  @LazyAutowired
  private ModulesActivatedService modulesActivatedService;

  @LazyAutowired
  private MRDObservationsRepository mrdObservationsRepository;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  /** The Constant PATIENT_ID. */
  private static final String PATIENT_ID = "patient_id";

  /** The Constant SERVICE_ID. */
  private static final String SERVICE_ID = "service_id";

  /** The Constant ITEM_CODE. */
  private static final String ITEM_CODE = "item_code";

  /** The Constant CONDUCTION_DOCTOR_MANDATORY. */
  private static final String CONDUCTION_DOCTOR_MANDATORY = "conducting_doc_mandatory";

  /** The Constant ALLOW_RATE_INCREASE. */
  private static final String ALLOW_RATE_INCREASE = "allow_rate_increase";

  /** The Constant ALLOW_RATE_DECREASE. */
  private static final String ALLOW_RATE_DECREASE = "allow_rate_decrease";

  /** The Constant ACTIVITY_CODE. */
  private static final String ACTIVITY_CODE = "SER";

  /** The Constant PRESCRIPTION_ID. */
  private static final String PRESCRIPTION_ID = "prescription_id";

  /** The Constant DOCTOR_ID. */
  private static final String DOCTOR_ID = "doctor_id";

  /** The Constant DOC_PRESC_ID. */
  private static final String DOC_PRESC_ID = "doc_presc_id";

  /** The prefix. */
  private static String prefix = "services";

  /**
   * Instantiates a new service order item service.
   *
   * @param repo the repo
   */
  public ServiceOrderItemService(ServiceOrderItemRepository repo,
      ServiceOrgDetailsRepository orgDetailsRepository) {
    super(repo, "services", SERVICE_ID, orgDetailsRepository);
    serviceOrderItemRepository = repo;
  }

  /**
   * Set the data required to be inserted along with each order.
   *
   * @param orderBean            the order bean
   * @param headerInformation    the header information
   * @param username             the username
   * @param isMultiVisitPackItem the is multi visit pack item
   * @param orderedItemList      the ordered item list
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(orderBean, PATIENT_ID, patientId);
    setBeanValue(orderBean, PRESCRIPTION_ID, serviceOrderItemRepository.getNextSequence());
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));

    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty("presc_date");
    setBeanValue(orderBean, "presc_date", ConvertUtils
        .convert(orderedItemList.get("services_prescribed_date"), dynaProperties.getType()));

    setBeanValue(orderBean, DOCTOR_ID, orderedItemList.get("services_prescribed_doctor_id"));
    setBeanValue(orderBean, SERVICE_ID, orderedItemList.get("services_item_id"));
    setAppointmentId(orderBean, (Integer) headerInformation.get("appointmentid"));
    setOperationRef(orderBean, null);
    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    BasicDynaBean serviceChargeBean = getMasterChargesBean((String) orderBean.get(SERVICE_ID),
        bedType, ratePlanId, null);
    setSpecilization(serviceChargeBean, orderBean);
    setConduction(serviceChargeBean, orderBean);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * setItemBeanProperties(org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String, boolean, java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) throws ParseException {
    super.setItemBeanProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList, operationBean);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(orderBean, PATIENT_ID, patientId);
    setBeanValue(orderBean, PRESCRIPTION_ID, serviceOrderItemRepository.getNextSequence());
    setBeanValue(orderBean, DOC_PRESC_ID,
        !"".equals(orderedItemList.get(DOC_PRESC_ID)) ? orderedItemList.get(DOC_PRESC_ID) : null);
    setBeanValue(orderBean, "tooth_fdi_number", orderedItemList.get("tooth_fdi_number"));
    setBeanValue(orderBean, "tooth_unv_number", orderedItemList.get("tooth_unv_number"));

    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));

    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty("presc_date");
    setBeanValue(orderBean, "presc_date",
        ConvertUtils.convert(orderedItemList.get("prescribed_date"), dynaProperties.getType()));

    setBeanValue(orderBean, DOCTOR_ID, orderedItemList.get("prescribed_doctor_id"));
    setBeanValue(orderBean, SERVICE_ID, orderedItemList.get("item_id"));
    setAppointmentId(orderBean, (Integer) headerInformation.get("appointmentid"));
    setOperationRef(orderBean, operationBean);
    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    BasicDynaBean serviceChargeBean = getMasterChargesBean((String) orderBean.get(SERVICE_ID),
        bedType, ratePlanId, null);
    setSpecilization(serviceChargeBean, orderBean);
    setConduction(serviceChargeBean, orderBean);
    return orderBean;
  }

  /**
   * Sets the specilization.
   *
   * @param service   the service
   * @param orderBean the order bean
   */
  public void setSpecilization(BasicDynaBean service, BasicDynaBean orderBean) {
    setBeanValue(orderBean, "specialization", service.get("specialization"));
  }

  /**
   * Sets the conduction.
   *
   * @param service   the service
   * @param orderBean the order bean
   */
  public void setConduction(BasicDynaBean service, BasicDynaBean orderBean) {
    boolean condApplicable = (Boolean) service.get("conduction_applicable");
    setBeanValue(orderBean, "conducted", condApplicable ? "N" : "U");
  }

  /**
   * Gets the prescribed service list.
   *
   * @param patientId the patient id
   * @return the prescribed service list
   */
  public List<BasicDynaBean> getPrescribedServiceList(String patientId) {

    return serviceOrderItemRepository.getPrescribedServiceList(patientId);
  }

  /**
   * Gets the conducted service details.
   *
   * @param patientId the patient id
   * @return the conducted service details
   */
  public List<BasicDynaBean> getConductedServiceDetails(String patientId) {

    return serviceOrderItemRepository.getConductedServiceDetails(patientId);
  }

  /**
   * Get the Master Charges Data.
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
    BasicDynaBean service = servicesRepository.getServiceChargesBean((String) serviceId, bedType,
        orgId);
    if (service == null) {
      service = servicesRepository.getServiceChargesBean((String) serviceId, "GENERAL", "ORG0001");
    }
    return service;
  }

  /**
   * Get the Master Charges Data.
   *
   * @param serviceId the service id
   * @param orgId     the org id
   * @return the master charges bean
   */
  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object serviceId, String orgId) {
    return servicesRepository.getAllServiceChargesBean((String) serviceId, orgId);
  }

  @Override
  public BigDecimal getCashCharge(Map<String, Object> paramMap) {
    String id = (String) paramMap.get("id");
    Integer centerId = (Integer) paramMap.get("center_id");
    String defaultRatePlan = (String) centerPreferencesService.getCenterPreferences(centerId)
        .get("pref_rate_plan_for_non_insured_bill");
    String bedType = (String) paramMap.get("bed_type");
    
    if (StringUtils.isEmpty(defaultRatePlan)) {
      return null;
    }

    BasicDynaBean cashChargeBean = getMasterChargesBean(id, bedType, defaultRatePlan,
        centerId);
    return (BigDecimal)cashChargeBean.get("unit_charge");
  }  

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#getCharges( java.util.Map)
   */
  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");

    Map<String, Object> otherParamsMap = new HashMap<String, Object>();
    otherParamsMap.put("visit_type", (String) paramMap.get("visit_type"));
    otherParamsMap.put("item_excluded_from_doctor", paramMap.get("item_excluded_from_doctor"));
    otherParamsMap.put("item_excluded_from_doctor_remarks",
        paramMap.get("item_excluded_from_doctor_remarks"));
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    BasicDynaBean masterCharge = getMasterChargesBean(paramMap.get("id"), bedType, ratePlanId,
        null);
    return getChargesList(masterCharge, (BigDecimal) paramMap.get("quantity"), isInsurance, null,
        otherParamsMap);
  }

  /**
   * Returns the listBillCharge bean that contains service orderCharges and tax charges.
   *
   * @param service      the service
   * @param quantity     the quantity
   * @param isInsurance  the is insurance
   * @param condDoctorId the cond doctor id
   * @return the charges list
   */
  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean service, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {

    BigDecimal unitCharge = (BigDecimal) service.get("unit_charge");
    Integer insuranceCategoryId;
    if (service.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) service.get("insurance_category_id");
    } else {
      insuranceCategoryId = 0;
    }

    BigDecimal discount = ((BigDecimal) service.get("discount")).multiply(quantity);
    BasicDynaBean billChargeBean = billChargeService.setBillChargeBean("SNP", "SERSNP", unitCharge,
        quantity, discount, (String) service.get(SERVICE_ID), (String) service.get("service_name"),
        service.get("serv_dept_id").toString(), (Integer) service.get("service_sub_group_id"),
        insuranceCategoryId, isInsurance);

    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      if (otherParams.get("item_excluded_from_doctor").equals("Y")
          || otherParams.get("item_excluded_from_doctor").equals(true)) {
        billChargeBean.set("item_excluded_from_doctor", true);
        billChargeBean.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      } else if (otherParams.get("item_excluded_from_doctor").equals("N")
          || otherParams.get("item_excluded_from_doctor").equals(false)) {
        billChargeBean.set("item_excluded_from_doctor", false);
        billChargeBean.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      }
    }

    if (condDoctorId != null) {
      billChargeBean.set("activity_conducted", "Y");
      billChargeBean.set("payee_doctor_id", condDoctorId);
    }

    billChargeBean.set("act_item_code", (String) service.get("service_code"));
    if (service.get(ITEM_CODE) != null && !service.get(ITEM_CODE).equals("")) {
      billChargeBean.set("act_rate_plan_item_code", (String) service.get(ITEM_CODE));
    }

    if (service.get("code_type") != null && !service.get("code_type").equals("")) {
      billChargeBean.set("code_type", (String) service.get("code_type"));
    }
    
    if (otherParams != null && otherParams.get("preauth_act_id") != null) {
      billChargeBean.set("preauth_act_id", otherParams.get("preauth_act_id"));
    }

    billChargeBean.set(CONDUCTION_DOCTOR_MANDATORY, service.get(CONDUCTION_DOCTOR_MANDATORY));
    billChargeBean.set(ALLOW_RATE_INCREASE, (Boolean) service.get(ALLOW_RATE_INCREASE));
    billChargeBean.set(ALLOW_RATE_DECREASE, (Boolean) service.get(ALLOW_RATE_DECREASE));
    String visitType = (String) otherParams.get("visit_type");

    String allowZeroClaimfor = (String) service.get("allow_zero_claim_amount");
    if (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor)) {
      billChargeBean.set("allow_zero_claim", true);
    }
    if (service != null || service.get("billing_group_id") != null) {
      billChargeBean.set("billing_group_id", (Integer) service.get("billing_group_id"));
    }
    if (otherParams.get("package_id") != null) {
      billChargeBean.set("package_id", otherParams.get("package_id"));
    }
    List<BasicDynaBean> serviceChargesList = new ArrayList<BasicDynaBean>();
    serviceChargesList.add(billChargeBean);

    /*
     * HMS-22250 - According to this bug, Now we do not support service Tax. So instead of migrating
     * the item master, we are not considering in code.
     * 
     * BigDecimal taxPer = (BigDecimal) service.get("service_tax");
     * 
     * if (taxPer != null && taxPer.compareTo(BigDecimal.ZERO) != 0) { BigDecimal taxAmount =
     * taxPer.multiply(unitCharge).multiply(quantity).divide(new BigDecimal("100"), 2); String
     * serviceName = (String) service.get("service_name"); BasicDynaBean taxCharge =
     * billChargeService.setBillChargeBean("TAX", "STAX", taxAmount, BigDecimal.ONE,
     * BigDecimal.ZERO, null, "Service Tax (" + serviceName + ")",
     * service.get("serv_dept_id").toString(), (Integer) service.get("service_sub_group_id"),
     * insuranceCategoryId, isInsurance);
     * 
     * taxCharge.set(ALLOW_RATE_INCREASE, (Boolean) service.get(ALLOW_RATE_INCREASE));
     * taxCharge.set(ALLOW_RATE_DECREASE, (Boolean) service.get(ALLOW_RATE_DECREASE));
     * taxCharge.set(CONDUCTION_DOCTOR_MANDATORY, "N"); taxCharge.set("amount", taxAmount);
     * taxCharge.set("act_item_code", (String) service.get("service_code"));
     * 
     * if (service.get(ITEM_CODE) != null && !service.get(ITEM_CODE).equals("")) {
     * taxCharge.set("act_rate_plan_item_code", (String) service.get(ITEM_CODE)); } if
     * (service.get("code_type") != null && !service.get("code_type").equals("")) {
     * taxCharge.set("code_type", (String) service.get("code_type")); } if (condDoctorId != null) {
     * taxCharge.set("payee_doctor_id", ""); billChargeBean.set("activity_conducted", "Y"); }
     * serviceChargesList.add(taxCharge); }
     */

    return serviceChargesList;
  }

  /**
   * Update.
   *
   * @param bean the bean
   * @param keys the keys
   * @return the int
   */
  public int update(BasicDynaBean bean, Map<String, Object> keys) {
    return serviceOrderItemRepository.update(bean, keys);
  }

  /**
   * Used in Case of Packages (Package Package).
   *
   * @param serviceBean        the service bean
   * @param headerInformation  the header information
   * @param centerId           the center id
   * @param packageChargeID    the package charge ID
   * @param conductingDoctorId the conducting doctor id
   */
  public void insertPackageBillActivityCharge(BasicDynaBean serviceBean,
      BasicDynaBean headerInformation, Integer centerId, String packageChargeID,
      String conductingDoctorId) {

    BasicDynaBean billActicityChargeBean = billActivityChargeService.getBillActivityChargeBean(
        packageChargeID, "SER", "SERSNP", serviceBean.get(PRESCRIPTION_ID).toString(),
        (String) serviceBean.get(SERVICE_ID), conductingDoctorId,
        "N".equals(serviceBean.get("conducted")) ? "N" : "Y", null);
    billActivityChargeService.insert(billActicityChargeBean);
  }

  /**
   * Returns the list of service item details by passing there id.
   *
   * @param entityIdList the entity id list
   * @param paramMap     the param map
   * @return the item details
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    List schedulable = null;
    if (paramMap.get("service_schedulable") != null
        && !paramMap.get("service_schedulable").equals("")) {
      schedulable = (java.util.List<String>) paramMap.get("service_schedulable");
    }
    return serviceOrderItemRepository.getItemDetails(entityIdList, schedulable);
  }

  /**
   * Returns the list of service items ordered for a given visit Id.
   *
   * @param parameters the parameters
   * @return the ordered items
   */
  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    return serviceOrderItemRepository.getOrderedItems((String) parameters.get("visit_id"),
        (Integer) parameters.get("operation_id"), (Boolean) parameters.get("package_ref"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#insertOrders (java.util.List,
   * java.lang.Boolean, java.util.Map, java.util.Map, java.util.List)
   */
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
    Long patientPendingPrescId = null;

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, null);

    serviceOrderItemRepository.batchInsert(itemList);

    List<Object> ismvpList = billItemAuthMap.get("isMultivisitPackageList");
    List<Object> condDocList = billItemAuthMap.get("conductingDoctorList");
    for (int index = 0; index < itemList.size(); index++) {
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> serviceItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean serviceBean = itemList.get(index);

      Boolean isMultivisitPackage = (Boolean) ismvpList.get(index);
      if (!condDocList.isEmpty() && index < condDocList.size()) {
        condDoctorId = (String) condDocList.get(index);
      }
      Integer doctorPrescriptionId = (Integer) serviceBean.get(DOC_PRESC_ID);
      if (doctorPrescriptionId != null) {
        BasicDynaBean prescriptionBean = servicePrescService
            .findByPrescriptionId(doctorPrescriptionId);
        if (prescriptionBean != null) {
          Integer orderedQty = Integer.valueOf(
              getOrderedQuantity(doctorPrescriptionId).intValue());
          if (orderedQty
              .compareTo((Integer) prescriptionBean.get("qty")) == 0) {
            updatePrescription("O", doctorPrescriptionId);
          } else {
            updatePrescription("PA", doctorPrescriptionId);
          }
          if (serviceItemDetails.get("pat_pending_presc_id") != null
              && !serviceItemDetails.get("pat_pending_presc_id").equals("null")) {
            patientPendingPrescId = Long
                .parseLong((String) serviceItemDetails.get("pat_pending_presc_id"));
          }
          if (null != patientPendingPrescId) {
            pendingPrescriptionsService.updatePendingPrescriptionStatus(
                patientPendingPrescId.toString(), "O");
          } else {
            Integer servicesQuantity = (Integer) serviceItemDetails.get("quantity");
            String priorActStatus = (String) serviceItemDetails.get("preauth_act_status");
            pendingPrescriptionsService.updatePendingPrescriptionStatus(
                doctorPrescriptionId, priorActStatus, servicesQuantity, "O");
          }
                    
        }
      }      

      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      BasicDynaBean mainChargeBean = null;
      List<BasicDynaBean> chargesList = null;
      if (null != bill) {
        chargesList = insertOrderItemCharges(chargeable, headerInformation,
            serviceBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, condDoctorId,
            ACTIVITY_CODE, centerId, isMultivisitPackage, serviceItemDetails);
        mainChargeBean = chargesList == null ? null : chargesList.get(0);
      }

      String packageIdString = serviceItemDetails.get("package_id") != null
          ? serviceItemDetails.get("package_id").toString()
          : null;
      Integer packageId = packageIdString != null && !"".equals(packageIdString)
          ? Integer.parseInt(packageIdString)
          : null;
      String packageObIdString = serviceItemDetails.get("package_ob_id") != null
          ? serviceItemDetails.get("package_ob_id").toString()
          : null;
      Integer packObId = packageObIdString != null && !"".equals(packageObIdString)
          ? Integer.parseInt(packageObIdString)
          : null;
      Boolean isMultiVisitPackItem = (Boolean) serviceItemDetails.get("multi_visit_package");
      Integer patientPackageId = null;
      if (patientPackageIdsList != null && patientPackageIdsList.size() >= index) {
        patientPackageId = (Integer) patientPackageIdsList.get(index);
      }
      String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
      if ((packageId != null) && (packObId != null) && (patientPackageId != null)
          && !isMultiVisitPackItem) {
        orderSetsOrderItemService.insertIntoPatientPackageContentAndConsumed(patientPackageId,
            packObId, packageId, (String) serviceBean.get(SERVICE_ID),
            Integer.valueOf(((BigDecimal) serviceBean.get("quantity")).intValue()),
            (Integer) serviceBean.get(PRESCRIPTION_ID), chargeId, username, "services");
      }

      String healthAuthority = null;
      try {
        healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      } catch (SQLException sqle) {
        LOGGER.error("SQL exception occured while finding health authority: {} ",
            sqle.getStackTrace());
      }
      if ("DHA".equals(healthAuthority)) {
        insertSpecialServiceObservation(chargesList, (String) bill.get("bill_rate_plan_id"));
      }
    }
    return itemList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#getEditBean (java.util.Map)
   */
  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIPTION_ID, item.get("order_id"));
    bean.set("remarks", item.get("remarks"));
    bean.set(DOCTOR_ID, item.get("prescribed_doctor_id"));
    bean.set("quantity", new BigDecimal(item.get("quantity").toString()));
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#getCancelBean (java.util.Map)
   */
  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    return getCancelBean(item.get("order_id"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#getCancelBean (java.lang.Object)
   */
  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIPTION_ID, orderId);
    bean.set("conducted", "X");
    bean.set("cancelled_by", sessionService.getSessionAttributes().get("userId"));
    bean.set("cancel_date", DateUtil.getCurrentDate());
    bean.set(DOC_PRESC_ID, null);
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#toItemBean( java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String, java.util.Map,
   * java.lang.String[], java.lang.Integer[], org.apache.commons.beanutils.BasicDynaBean)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) throws ParseException {

    List<Object> newPreAuths = orderedItemAuths.get("newPreAuths");
    List<Object> newPreAuthModesList = orderedItemAuths.get("newPreAuthModesList");
    List<Object> secNewPreAuths = orderedItemAuths.get("secNewPreAuths");
    List<Object> secNewPreAuthModesList = orderedItemAuths.get("secNewPreAuthModesList");

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

    BasicDynaBean orderedItemBean = serviceOrderItemRepository.getBean();
    ConversionUtils.copyJsonToDynaBean(item, orderedItemBean, errorList, true);

    if (!errorList.isEmpty()) {
      throw new ConversionException(errorList);
    }

    Object mvp = item.get("multi_visit_package");
    Boolean isMultivisitPackage = mvp != null && !"".equals(mvp) ? Boolean.valueOf(mvp.toString())
        : false;
    orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);

    setItemBeanProperties(orderedItemBean, headerInformation, username, isMultivisitPackage, item,
        null);

    return orderedItemBean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updateCancelStatusAndRefOrders(java.util.List, boolean, boolean, java.util.List, java.util.Map)
   */
  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    if (cancel) {
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      serviceOrderItemRepository.updateCancelStatusToPatient(items, userName, itemInfoMap);
    }
  }

  /**
   * Gets the charge prior auth details.
   *
   * @param itemInfoMap the item info map
   * @return the charge prior auth details
   */
  public Map<Integer, Map<String, Object>> getChargePriorAuthDetails(Map<String, 
      Object> itemInfoMap) {
    List<Map<String, Object>> itemsList = (List<Map<String, Object>>) itemInfoMap.get(ITEMS_MAP);
    if (itemsList == null) { 
      return Collections.emptyMap();
    }
    Map<Integer, Map<String, Object>> chargePriorAuthDetails = 
        new HashMap<Integer, Map<String, Object>>();
    for (Map<String, Object> item : itemsList) {
      Map<String, Object> insuranceMap = new HashMap<>();
      insuranceMap.put("prior_auth_id", (item.containsKey("prior_auth_id")) 
          ? item.get("prior_auth_id") : null);
      insuranceMap.put("preauth_act_status", (item.containsKey("preauth_act_status")) 
          ? item.get("preauth_act_status") : null);
      insuranceMap.put("preauth_act_id", (item.containsKey("preauth_act_id")) 
          ? item.get("preauth_act_id") : null);
      insuranceMap.put("send_for_prior_auth", (item.containsKey("send_for_prior_auth")) 
          ? item.get("send_for_prior_auth") : null);
      insuranceMap.put("insurance_claim_amount", (item.containsKey("insurance_claim_amount")) 
          ? item.get("insurance_claim_amount") : null);
      chargePriorAuthDetails.put((Integer) item.get("id"), insuranceMap);
    }
    return chargePriorAuthDetails;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#updateItemBeans (java.util.List)
   */
  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    Map<String, Object> keys = new HashMap<>();
    List<Object> prescriptionIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescriptionIdsList.add(item.get(PRESCRIPTION_ID));
    }
    keys.put(PRESCRIPTION_ID, prescriptionIdsList);
    return serviceOrderItemRepository.batchUpdate(items, keys);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updateFinalizableItemCharges(java.util.List, java.util.Map)
   */
  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // For services there is no update.
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService# getOrderItemPrimaryKey()
   */
  @Override
  public String getOrderItemPrimaryKey() {
    return PRESCRIPTION_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService# getOrderItemActivityCode()
   */
  @Override
  public String getOrderItemActivityCode() {
    return ACTIVITY_CODE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService# getPrescriptionDocKey()
   */
  @Override
  public String getPrescriptionDocKey() {
    return DOCTOR_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * setChargeQuantityAndAmount(org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean)
   */
  @Override
  public void setChargeQuantityAndAmount(BasicDynaBean item, BasicDynaBean charge) {
    charge.set("act_quantity", (BigDecimal) item.get("quantity"));
    billChargeService.recalcAmount(charge);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService# insertOrderItemCharges(boolean,
   * org.apache.commons.beanutils.BasicDynaBean, org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, java.lang.String[], java.lang.Integer[], int[],
   * java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.util.Map)
   */
  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    Integer priorAuthItemId = null;
    Long patPendingPrescId = null;
    String preAuthActStatus = null;

    if (orderItemDetails != null) {
      priorAuthItemId = (Integer) orderItemDetails.get("prior_auth_item_id");
      if (BooleanUtils.isTrue((Boolean) orderItemDetails.get("prescriptionOrder"))
          && priorAuthItemId == null
          && MapUtils.getInteger(orderItemDetails, "doc_presc_id") != null) {
        priorAuthItemId = preAuthItemsService
            .getPriorAuthItemIdFromPrescId((Integer) orderItemDetails.get("doc_presc_id"));
      }
      if (orderItemDetails.get("pat_pending_presc_id") != null
          && !orderItemDetails.get("pat_pending_presc_id").toString().equals("null")) {
        patPendingPrescId = (Long) orderItemDetails.get("pat_pending_presc_id");
      }
      preAuthActStatus = (String) orderItemDetails.get("preauth_act_status");
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
            (int) priorAuthItemId, preAuthActStatus, "O");
      }
    }

    BasicDynaBean masterCharge = getMasterChargesBean((Object) orderBean.get(SERVICE_ID), bedType,
        ratePlanId, centerId);

    boolean condApplicable;
    if (masterCharge.getDynaClass().getDynaProperty("conduction_applicable") != null) {
      condApplicable = (Boolean) masterCharge.get("conduction_applicable");
    } else {
      condApplicable = false;
    }

    if (masterCharge.get("specialization") != null
        && masterCharge.get("specialization").equals("I")) {
      BasicDynaBean bean = ivfCycleService.getBean();
      bean.set("mr_no", headerInformation.get("mr_no"));
      bean.set("ivf_cycle_id", ivfCycleService.getNextSequence());
      bean.set(PATIENT_ID, headerInformation.get(PATIENT_ID));
      bean.set("start_date", DateUtil.getCurrentDate());
      bean.set("cycle_status", "O");
      ivfCycleService.insert(bean);
    }

    List<BasicDynaBean> chargesList = null;
    if (chargeable) {
      BigDecimal quantity = BigDecimal.ONE;
      if (null != orderBean.getDynaClass().getDynaProperty("quantity")
          && !((orderBean.get("quantity")).equals(""))) {
        quantity = (BigDecimal) orderBean.get("quantity");
      }
      BigDecimal charge;
      BigDecimal discount  = BigDecimal.ZERO;
      if (isMultivisitPackage) {
        charge = new BigDecimal(String.valueOf(orderItemDetails.get("act_rate")));
        discount = BigDecimal.valueOf(Double.valueOf((String) orderItemDetails.get("discount")));
        masterCharge.set("unit_charge", charge);
        masterCharge.set("discount", discount.divide(quantity));
        BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
            (Integer) orderItemDetails.get("package_id"));
        masterCharge.set("allow_rate_increase", 
            (Boolean)componentDeatilBean.get("allow_rate_increase"));
        masterCharge.set("allow_rate_decrease", 
            (Boolean)componentDeatilBean.get("allow_rate_decrease"));
      }

      Map<String, Object> otherParamsMap = new HashMap<>();
      otherParamsMap.put("visit_type", (String) bill.get("visit_type"));
      otherParamsMap.put("item_excluded_from_doctor",
          orderItemDetails.get("item_excluded_from_doctor"));
      otherParamsMap.put("item_excluded_from_doctor_remarks",
          orderItemDetails.get("item_excluded_from_doctor_remarks"));
      if (orderItemDetails.get("package_id") != null) {
        otherParamsMap.put("package_id", orderItemDetails.get("package_id"));
      }
      chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId,
          otherParamsMap);
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds,
            (Integer) orderBean.get(PRESCRIPTION_ID), (String) orderBean.get(DOCTOR_ID),
            new Timestamp(postedDate.getTime()), (String) orderBean.get("remarks"),
            orderItemDetails,bedType);
      } else {
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds,
            (Integer) orderBean.get(PRESCRIPTION_ID), (String) orderBean.get(DOCTOR_ID),
            (Timestamp) orderBean.get("presc_date"), (String) orderBean.get("remarks"),
            orderItemDetails,bedType);
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
              orderItemDetails.get("pack_ob_id"), orderItemDetails.get("pat_package_id"),
              Integer.valueOf((String) orderItemDetails.get("quantity")),
              chargeId, orderBean.get(PRESCRIPTION_ID),
              orderItemDetails.get("type"));
        }
      }
    }

    return chargesList;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * insertPackageContent(org.apache.commons.beanutils.BasicDynaBean,
   * org.apache.commons.beanutils.BasicDynaBean, org.apache.commons.beanutils.BasicDynaBean,
   * java.lang.String, java.lang.Integer, org.apache.commons.beanutils.BasicDynaBean, java.util.Map,
   * java.lang.Integer)
   */
  @Override
  public void insertPackageContent(BasicDynaBean packageBean, BasicDynaBean packageItem,
      BasicDynaBean headerInformation, String username, Integer centerId,
      BasicDynaBean mainChargeBean, Map<String, Object> packageItemDetails, Integer index) {

    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");

    BasicDynaBean masterServiceBean = getMasterChargesBean(packageItem.get("activity_id"), bedType,
        ratePlanId, centerId);

    if (!(Boolean) masterServiceBean.get("applicable")) {
      return;
    }

    BasicDynaBean serviceBean = getBean();
    setBeanValue(serviceBean, SERVICE_ID, packageItem.get("activity_id"));
    setBeanValue(serviceBean, "quantity", BigDecimal.ONE);
    setBeanValue(serviceBean, "presc_date", packageBean.get("presc_date"));
    setBeanValue(serviceBean, DOCTOR_ID, packageBean.get(DOCTOR_ID));
    setBeanValue(serviceBean, "conducted", "N");
    setBeanValue(serviceBean, "remarks", packageBean.get("remarks"));
    setBeanValue(serviceBean, "package_ref", packageBean.get("prescription_id"));

    setBeanValue(serviceBean, "specialization", masterServiceBean.get("specialization"));

    setBeanValue(serviceBean, "mr_no", (String) headerInformation.get("mr_no"));
    setBeanValue(serviceBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(serviceBean, "user_name", username);
    String patientId = (String) headerInformation.get(PATIENT_ID);
    setBeanValue(serviceBean, PATIENT_ID, patientId);
    setBeanValue(serviceBean, PRESCRIPTION_ID, serviceOrderItemRepository.getNextSequence());
    setConduction(masterServiceBean, serviceBean);
    serviceOrderItemRepository.insert(serviceBean);

    String conductingDoctorId = null;
    ArrayList<Object> packageConductingDoctorList = (ArrayList<Object>) packageItemDetails
        .get("package_conducting_doctor");
    for (Object packageConductingDoctor : packageConductingDoctorList) {
      Map<String, Object> packageConductingDoctorMap =
          (Map<String, Object>) packageConductingDoctor;
      int actIndex = (Integer) packageConductingDoctorMap.get("package_activity_index");
      if (actIndex == index) {
        conductingDoctorId = (String) packageConductingDoctorMap.get("package_doctor_id");
        break;
      }
    }
    String packageChargeID = mainChargeBean != null ? (String) mainChargeBean.get("charge_id")
        : null;
    if (packageChargeID != null) {
      insertPackageBillActivityCharge(serviceBean, headerInformation, centerId, packageChargeID,
          conductingDoctorId);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.order.master.OrderItemService#
   * updatePatientActivities(java.util.List)
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

      prescriptionType.add("S");
      orderNo.add(orderBean.get(PRESCRIPTION_ID));
    }

    Map<String, Object> keys = new HashMap<>();
    keys.put("order_no", orderNo);
    keys.put("prescription_type", prescriptionType);

    List<BasicDynaBean> updateBeans = new ArrayList<>();
    patientActivitiesService.batchUpdate(updateBeans, keys);
  }
  
  public BigDecimal getOrderedQuantity(Integer doctorPrescriptionId) {
    return serviceOrderItemRepository.getOrderedQuantity(doctorPrescriptionId);
  }

  /**
   * Get the Special Service Code .
   *
   * @param serviceId the service id
   * @param orgId     the org id
   * @return the master charges bean
   */
  public BasicDynaBean getMasterSpecialCodeBean(Object serviceId, String orgId) {
    return servicesRepository.getSpecialServiceCodeBean((String) serviceId,orgId);
  }

  private void insertSpecialServiceObservation(List<BasicDynaBean> chargesList, String orgId) {
    if (StringUtils.isBlank(orgId)) {
      return;
    }
    for (BasicDynaBean charge : chargesList) {
      if ("SNP".equals(charge.get("charge_group"))) {
        String serviceId = (String) charge.get("act_description_id");
        String chargeId = (String) charge.get("charge_id");
        BasicDynaBean specialCodeBean = getMasterSpecialCodeBean(serviceId, orgId);
        if (specialCodeBean != null && StringUtils.isNotBlank((String) specialCodeBean.get(
            "special_service_code"))) {
          BasicDynaBean obsBean = mrdObservationsRepository.getBean();
          obsBean.set("charge_id", chargeId);
          obsBean.set("observation_type", "Grouping");
          obsBean.set("code", "PackageID");
          obsBean.set("value", specialCodeBean.get("special_service_code"));
          obsBean.set("value_type", "Other");
          obsBean.set("value_editable", "N");
          mrdObservationsRepository.insert(obsBean);
        }
      }
    }
  }

}

