package com.insta.hms.core.clinical.order.master;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.RelevantSorting;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillHelper;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionEAuthorization;
import com.insta.hms.core.clinical.eauthorization.EAuthorizationService;
import com.insta.hms.core.clinical.order.beditems.BedOrderItemService;
import com.insta.hms.core.clinical.order.chargehead.ChargeHeadOrderItemService;
import com.insta.hms.core.clinical.order.dietitems.DietOrderItemService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.clinical.order.equipmentitems.EquipmentOrderItemService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.order.ordersets.OrderSetsOrderItemService;
import com.insta.hms.core.clinical.order.otheritmes.OtherOrderItemServices;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.PackageOrderItemService;
import com.insta.hms.core.clinical.order.serviceitems.ServiceOrderItemService;
import com.insta.hms.core.clinical.order.testitems.TestOrderItemService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationRepository;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService.PreAuthItemType;
import com.insta.hms.core.clinical.prescriptions.PatientConsultationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientDietPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientOperationPrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientServicePrescriptionsService;
import com.insta.hms.core.clinical.prescriptions.PatientTestPrescriptionsService;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.medicalrecords.codification.MRDObservationsRepository;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.documents.TestDocumentsService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.priorauth.PriorAuthorizationService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.mdm.anaesthesia.AnaesthesiaTypeService;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.commoncharges.CommonChargesRepository;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesRepository;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.ordersets.PackageContentCharges;
import com.insta.hms.mdm.ordersets.PackageContentChargesService;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedRepository;
import com.insta.hms.mdm.packages.PatientPackageContentsRepository;
import com.insta.hms.mdm.packages.PatientPackageRepository;
import com.insta.hms.mdm.registrationcharges.RegistrationChargesService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.services.ServicesService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;
import com.insta.hms.mdm.theatre.TheatreService;
import com.insta.hms.mdm.tpas.TpaService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class OrderService {

  private static Logger log = LoggerFactory.getLogger(OrderService.class);

  private static final String CONSULTATION_ID = "consultation_id";
  private static final String ORDER_BASE_PATH = "com.insta.hms.core.clinical.order";
  private static final String CHARGE_TYPE = "charge_type";
  private static final String NEWLY_ADDED_ORDERED_ITEM = "newly_added_ordered_item";
  private static final String INSURANCE_CATEGORY_ID = "insurance_category_id";
  private static final String ESTIMATE_AMOUNT = "estimate_amount";
  private static final String CANCELLED = "cancelled";
  private static final String CONSULTATION_TYPE_ID = "consultation_type_id";
  private static final String TAX_AMT = "tax_amt";
  private static final String ORG_ID = "org_id";
  private static final String BILL_NO = "bill_no";
  private static final String OPERATION_ID = "operation_id";
  private static final String EDITED_ITEM_INDEX = "editedItemIndex";
  private static final String UPDATED_EDITED_ORDER_ITEM_INDEX = "updatedEditedOrderItemIndex";
  private static final String EDITED = "edited";

  @LazyAutowired
  private DietOrderItemService dietOrderService;

  @LazyAutowired
  private ServiceOrderItemService serviceOrderService;

  @LazyAutowired
  private EquipmentOrderItemService equipmentOrderService;

  /** The user service. */
  @LazyAutowired 
  private UserService userService;
  
  @LazyAutowired
  private DoctorOrderItemService doctorOrderService;
  
  @LazyAutowired
  private BedTypeService bedTypeService;

  @LazyAutowired
  private DoctorConsultationRepository doctorOrderItemRepository;

  @LazyAutowired
  private TestOrderItemService testOrderService;

  @LazyAutowired
  private OtherOrderItemServices otherOrderServices;

  @LazyAutowired
  private OperationOrderItemService operationOrderService;

  @LazyAutowired
  private PackageOrderItemService packageOrderItemService;

  @LazyAutowired
  private RegistrationService registrationService;

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;

  @LazyAutowired
  private PackagesService packagesService;

  @LazyAutowired
  private CenterPreferencesService centerPrefService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private BillHelper billHelper;

  @LazyAutowired
  private SponsorService sponsorService;

  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  @LazyAutowired
  private TpaService tpaService;

  @Autowired
  private OrderRepository orderRepository;

  @LazyAutowired
  private BedOrderItemService bedOrderItemService;

  @LazyAutowired
  private ChargeHeadOrderItemService chargeHeadOrderItemService;

  @LazyAutowired
  private CenterService centerService;

  @LazyAutowired
  private ServiceGroupService serviceGroupService;

  @LazyAutowired
  private ServiceSubGroupService serviceSubGroupService;

  @LazyAutowired
  private PatientTestPrescriptionsService testPrescService;

  @LazyAutowired
  private PatientServicePrescriptionsService servicePrescService;

  @LazyAutowired
  private PatientConsultationPrescriptionsService consultationPrescService;

  @LazyAutowired
  private PatientDietPrescriptionsService dietPrescService;

  @LazyAutowired
  private PatientOperationPrescriptionsService operationPrescService;

  @LazyAutowired
  private AllergiesService allergiesService;

  @LazyAutowired
  private DiscountPlanService discountPlanService;

  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  @LazyAutowired
  private TheatreService theatreService;

  @LazyAutowired
  private AnaesthesiaTypeService anesthesiaTypeService;

  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  @LazyAutowired
  private PriorAuthorizationService priorAuthService;

  @LazyAutowired
  private BeanFactory beanFactory;
  @LazyAutowired
  private PatientDetailsService patientdetailsService;

  @LazyAutowired
  private HospitalCenterService hospitalCenterService;

  @LazyAutowired
  private PatientPackageRepository patientPackageRepo;

  @LazyAutowired
  private PatientPackageContentsRepository patPackContentsRepo;

  @LazyAutowired
  private OrderSetsService orderSetsService;

  @LazyAutowired
  private PatientPackageContentConsumedRepository patPackContentConsumedRepo;

  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  @LazyAutowired
  private OrderSetsOrderItemService orderSetsOrderItemService;

  @LazyAutowired
  private TestDocumentsService testDocumentsService;

  @LazyAutowired
  private OrderBillSelection orderBillSelection;

  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;

  @LazyAutowired
  private OrderValidator orderValidator;

  /** The registration charges service. */
  @LazyAutowired
  RegistrationChargesService registrationChargesService;

  /** The doctor service. */
  @LazyAutowired
  DoctorService doctorService;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  @LazyAutowired
  private InsurancePlanService insurancePlanService;

  @LazyAutowired
  private HospitalRoleService hospitalRoleService;

  @LazyAutowired
  private DiagnosticTestService diagnosticTestService;

  @LazyAutowired
  private ServicesService servicesService;

  private Set<Class<? extends OrderItemService>> orderItemsTypes;

  @LazyAutowired
  private AllocationService allocationService;
  
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;

  @LazyAutowired
  private MRDObservationsRepository mrdObservationsRepository;
  
  @LazyAutowired
  private PrescriptionEAuthorization prescriptionEAuthorization;
  
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;
  
  @LazyAutowired
  private EAuthorizationService eauthService;

  @LazyAutowired
  private ConsultationTypesRepository consultationTypesRepository;
  
  /** The common charges repository. */
  @LazyAutowired
  private CommonChargesRepository commonChargesRepository;

  @LazyAutowired
  private PackageContentChargesService packageContentChargesService;

  /**
   * OrderService.
   * 
   */
  public OrderService() {
    // Doing this in constructor so that it happens only once.
    Reflections reflections = new Reflections(ORDER_BASE_PATH);
    this.orderItemsTypes = reflections.getSubTypesOf(OrderItemService.class);
  }

  public Set<Class<? extends OrderItemService>> getOrderItemsTypes() {
    return orderItemsTypes;
  }

  /**
   * set common order id.
   * 
   * @param headerInformation
   *          the headerInformation
   */
  public void setCommonOrderId(BasicDynaBean headerInformation) {
    if (headerInformation != null && headerInformation.get("commonorderid").equals(0)) {
      headerInformation.set("commonorderid", DatabaseHelper.getNextSequence("common_order"));
    }
  }

  private void setAppointmentId(BasicDynaBean headerInformation, Integer appointmentId) {
    if (appointmentId != null) {
      headerInformation.set("appointmentid", appointmentId);
    }
  }

  private void setPackageRef(BasicDynaBean headerInformation) {
    if (headerInformation != null) {
      headerInformation.set("packageref", packageOrderItemService.getNextSequence());
    }
  }

  /**
   * Returns map of order related details needed for particular visit Id.
   * 
   * @param visitId
   *          the visitId
   * @param visitType
   *          the visitType
   * @param pharmacy
   *          the pharmacy
   * @return map
   */
  public Map<String, Object> getDetails(String visitId, String visitType, String pharmacy) {
    Map<String, Object> map = new HashMap<>();
    map.put("bills", ConversionUtils
        .listBeanToListMap(getUnpaidBillsForVisit(visitId, visitType, pharmacy, "N", null)));
    map.put("theatres",
        ConversionUtils.listBeanToListMap(theatreService.getTheatreListForPatientId(visitId)));
    map.put("insurance",
        ConversionUtils.listBeanToListMap(getInsuranceDetails(visitId, visitType)));
    map.put("rate_plan", getVisitRatePlan(visitId, visitType).getMap());
    map.put("multi_visit_package_bills",
        ConversionUtils.listBeanToListMap(getMvpBillsForVisit(visitId)));
    map.put("server_time", new Date());
    map.put("visit_patient_due", getVisitPatientDue(visitId));
    map.put("creditLimitDetailsMap", getCreditLimitDetails(visitId));
    return map;
  }

  /**
   * Returns operations, anesthesia and related details in a visit.
   * 
   * @param visitId
   *          the visitId
   * @return map
   */
  @SuppressWarnings("rawtypes")
  private Map<String, Object> getOperationDetails(List<Integer> operationIds, String visitId) {
    Map<String, Object> returnMap = new HashMap<String, Object>();
    Map<Integer, List> operOrdersMap = new HashMap<Integer, List>();

    Map<Integer, List> operAnaesthesiaTypeOrdersMap = new HashMap<Integer, List>();
    Map<Integer, List> addTheaters = new HashMap<Integer, List>();
    Map<Integer, List> advanceOTAnaesthesiaTypeOrdersMap = new HashMap<Integer, List>();
    for (Integer opId : operationIds) {
      List<BasicDynaBean> operOrders = operationOrderService.getOperationReferenceOrders(visitId,
          opId);
      List<BasicDynaBean> operAnesthesiaTypes = operationOrderService
          .getOperationAnesthesiaDetails(opId);
      List<BasicDynaBean> addTheaterTypes = operationOrderService
          .getOperationAdditionalTheaterDetails(opId);
      operOrdersMap.put(opId, ConversionUtils.listBeanToListMap(operOrders));
      operAnaesthesiaTypeOrdersMap.put(opId,
          ConversionUtils.listBeanToListMap(operAnesthesiaTypes));
      addTheaters.put(opId, ConversionUtils.listBeanToListMap(addTheaterTypes));
      List<BasicDynaBean> advOTAnesthesiaTypes = operationOrderService
          .getAdvanceOperationAnesthesiaDetails(opId);
      advanceOTAnaesthesiaTypeOrdersMap.put(opId,
          ConversionUtils.listBeanToListMap(advOTAnesthesiaTypes));
    }

    returnMap.put("operation_orders_map", operOrdersMap);
    returnMap.put("additional_theaters_map", addTheaters);
    returnMap.put("operation_anaesthesia_type_orders_map", operAnaesthesiaTypeOrdersMap);
    returnMap.put("adv_ot_anaesthesia_type_orders_map", advanceOTAnaesthesiaTypeOrdersMap);
    return returnMap;
  }

  Map<String, Map<String, List<Object>>> updateBillNoInItemsMap(
          Map<String, Map<String, List<Object>>> itemsMap,
          String billNo) {
    Map<String, Map<String, List<Object>>> newItemsMap = new HashMap<>();
    for (Entry<String, Map<String, List<Object>>> billEntry: itemsMap.entrySet()) {
      Map<String, List<Object>> billItemsMap = billEntry.getValue();
      for (Entry<String, List<Object>> itemsEntry: billItemsMap.entrySet()) {
        List<Object> itemsList = itemsEntry.getValue();
        for (Object item: itemsList) {
          ((Map<String, Object>)item).put("bill_no", billNo);
        }
      }
      newItemsMap.put(billNo, billItemsMap);
    }
    return newItemsMap;
  }

  /**
   * Starting Point where other services calls to insert orders. Bill is already Created.
   * AllowClosedBills is always true for registration.
   * 
   * @param requestParams
   *          the requestParams
   * @param allowClosedBills
   *          the allowClosedBills
   * @param billNo
   *          the billNo
   * @param centerId
   *          the centerId
   * @param plansList
   *          the plansList
   * @param params
   *          the params
   * @param visitDetailsBean
   *          the visitDetailsBean
   * @throws ParseException
   *           the ParseException
   * @throws SQLException
   *           the SQLException
   * @throws IOException
   *           the IOException
   * @throws NoSuchMethodException
   *           the NoSuchMethodException
   * @throws IllegalAccessException
   *           the IllegalAccessException
   * @throws InvocationTargetException
   *           the InvocationTargetException
   */
  public void orderItems(Map<String, List<Object>> requestParams, boolean allowClosedBills,
      String billNo, Integer centerId, List<BasicDynaBean> plansList, Map<String, Object> params,
      BasicDynaBean visitDetailsBean, Boolean isReg,
      Map<String, Map<String, List<Object>>> regOrderedItems)
          throws ParseException, SQLException, IOException, NoSuchMethodException,
          IllegalAccessException, InvocationTargetException {
    BasicDynaBean headerInformation = null;
    BasicDynaBean bill = null;
    String patientId = (String) visitDetailsBean.get("patient_id");
    String visitType = (String) visitDetailsBean.get("visit_type");

    if (null == billNo) {
      headerInformation = registrationService.getPatientInfo(patientId);
    } else {
      headerInformation = registrationService.getBillPatientInfo(patientId, billNo);
      bill = billService.findByKey(billNo);
    }

    Integer appointmentId = null;
    Map<String, Object> visitRequestParams = (Map<String, Object>) params.get("visit");
    if (visitRequestParams.get("appointment_id") != null
        && !"".equals(visitRequestParams.get("appointment_id"))) {
      appointmentId = Integer.parseInt(visitRequestParams.get("appointment_id").toString());
    }
    if (visitType == null || centerId == null || billNo == null) {
      log.debug("Cannot order Items, either visitType, centerId or Bill is not defined");
    }

    // BasicDynaBean headerInformation = registrationService.getBillPatientInfo(patientId, billNo);

    if (!allowClosedBills && headerInformation != null) {
      if (!("A").equals(headerInformation.get("status"))) {
        log.debug("Bill is not open, cannot add new items to the bill");
      }

      if (("P").equals(headerInformation.get("payment_status"))) {
        log.debug("Bill is paid, cannot add new items to the bill");
      }
    }

    setAppointmentId(headerInformation, appointmentId);
    setCommonOrderId(headerInformation);
    setPackageRef(headerInformation);

    int[] planIds = new int[plansList.size()];
    String[] preAuthIds = new String[plansList.size()];
    Integer[] preAuthModeIds = new Integer[plansList.size()];
    int planIdIndex = 0;

    for (BasicDynaBean bean : plansList) {
      planIds[planIdIndex] = (Integer) bean.get("plan_id");
      preAuthIds[planIdIndex] = (String) bean.get("prior_auth_id");
      preAuthModeIds[planIdIndex] = (Integer) bean.get("prior_auth_mode_id");
      planIdIndex++;
    }

    orderConsulationDoctor(params, headerInformation, bill, centerId, planIds);
    List<Map<String, Object>> newMVPDetails = (List<Map<String, Object>>) params
            .get("new_mvp_details");
    Boolean isNewMVP = CollectionUtils.isNotEmpty(newMVPDetails);
    if (isReg) {
      ModelMap map = new ModelMap();
      map.put("new_mvp_details", newMVPDetails);
      Map<String, Object> chargesDetailsParams = (Map<String, Object>) params.get("charges");
      Integer regCharge = (Integer)chargesDetailsParams.get("registrationCharge");
      String consultationTypeId = (String)chargesDetailsParams.get("consultation_type_id");
      boolean newbill = (regCharge <= 0 && StringUtil.isNullOrEmpty(consultationTypeId)) ? false
          : true;
      
      if ((isNewMVP || isHasMvpOrder(regOrderedItems.get("new"))) && newbill) {
        map.put("ordered_items", updateBillNoInItemsMap(regOrderedItems, "new"));
      } else {
        map.put("ordered_items", updateBillNoInItemsMap(regOrderedItems, billNo));
      }
      map.put("mr_no", headerInformation.get("mr_no"));

      Map<String, Object> visitParams = new HashMap<>();
      visitParams.put("visit_id", patientId);
      visitParams.put("bed_type", visitDetailsBean.get("bed_type"));
      visitParams.put("org_id", visitDetailsBean.get("org_id"));
      visitParams.put("status", visitDetailsBean.get("status"));
      visitParams.put("visit_type", visitDetailsBean.get("visit_type"));
      map.put("visit", visitParams);
      orderItems(map, headerInformation);
    } else {
      Integer patientPackId = insertMultiVistPackageContents(params, headerInformation, patientId);
      callRespectiveOrderServices(requestParams, headerInformation, preAuthIds, preAuthModeIds,
              planIds, bill, centerId, patientPackId, isNewMVP);
      if (patientPackId != null && patientPackId > 0) {
        multiVisitPackageService.updatePatientPackageStatus(patientPackId);
      }
    }
    initiatePreAuthLimitCheck(patientId);

  }

  /**
   * Updating Order for a visit.
   * <p>
   * params object template - { "ordered_items": { "bill_no_1" : { "tests": [ List of updated,
   * cancelled, inserted items], "doctors" : [] ...etc } ...etc. },
   * 
   * "visit" : { "visit_id" : "", ...etc. } }
   * </p>
   * 
   * @param params
   *          the params
   * @throws ParseException
   *           the ParseException
   * @throws IOException
   *           the IOException
   * @throws InvocationTargetException
   *           the InvocationTargetException
   * @throws IllegalAccessException
   *           the IllegalAccessException
   * @throws NoSuchMethodException
   *           the NoSuchMethodException
   * @throws SQLException
   *           the SQLException
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public void orderItems(ModelMap params, BasicDynaBean regHeaderInformation) throws ParseException,
      IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      SQLException {
    Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
    visitParams.put("mr_no", params.get("mr_no"));
    String userName = RequestContext.getUserName();
    visitParams.put("user_name", userName);
    List<Map<String, Object>> newMVPDetails =
        (List<Map<String, Object>>) params
        .get("new_mvp_details");
    Map<String, Map<String, List<Object>>> orderedItems =
        (Map<String, Map<String, List<Object>>>) params
        .get("ordered_items");
    Map<Integer, Object> newMvpMap = new HashMap<Integer, Object>();
    List<String> editOrCancelOrderBills = new ArrayList<>();
    Map<Integer, String> contentRefMap = new HashMap<>();
    Map<String, Integer> contentBaseMap = new HashMap<>();
    List<Map<String, Object>>  patPackContIds =  new ArrayList<>();
    if (newMVPDetails != null && !newMVPDetails.isEmpty()) {
      String bedType = (String) visitParams.get("bed_type");
      String mrNo = (String) visitParams.get("mr_no");
      String visitId = (String) visitParams.get("visit_id");
      String orgId = (String) visitParams.get("org_id");
      List<BasicDynaBean> patPackCont = new ArrayList<>();
      List<BasicDynaBean> patPackContCharges = new ArrayList<>();
      int packContIndex = 0;
      for (Map<String, Object> newMvpDetail : newMVPDetails) {
        Map<String, Object> packDetails =
            (Map<String, Object>) newMvpDetail.get("package_details");
        String submissionType = String.valueOf('P');
        if (packDetails != null) {
          submissionType = (String) packDetails.get("submission_batch_type");
        }
        if (newMvpDetail.get("is_customized") != null) {
          packDetails.put("is_customized", newMvpDetail.get("is_customized"));
        }
        List<Map<String, Object>> packContents =
                (List<Map<String, Object>>) newMvpDetail.get("package_contents");
        Integer mvpPackageId = (Integer) newMvpDetail.get("package_id");
        BasicDynaBean patPackBean = multiVisitPackageService.insertPatientPackageBean(
            mrNo, (Object) mvpPackageId);
        Integer patPackId = (Integer) patPackBean.get("pat_package_id");
        newMvpMap.put((Integer) newMvpDetail.get("mvp_index"), patPackId);
        for (Map<String, Object> packCont : packContents) {
          Map<String, Object> patPackContId = new HashMap<>();
          Integer patPackContentId =
              multiVisitPackageService.getPatientPackageContentCharges(packCont,
              userName, bedType, orgId, patPackCont, patPackContCharges, visitId, mvpPackageId,
              patPackId, submissionType);
          if (!"Operation".equals(packCont.get("activity_type"))) {
            patPackContId.put("patient_package_content_id", patPackContentId);
            patPackContId.put("packContIndex", packContIndex);
            patPackContIds.add(patPackContId);
          }
          packContIndex ++;
          if (packCont.get("content_id_ref") != null) {

            contentRefMap.put(patPackContentId, (String) packCont.get("content_id_ref"));
            if (!contentBaseMap.containsKey(packCont.get("content_id_ref"))) {
              contentBaseMap.put((String) packCont.get("content_id_ref"),
                  patPackContentId);
            }
          }
        }
        if (packDetails != null) {
          multiVisitPackageService.insertPatientPackageCustomized(patPackId, packDetails, userName);
        }
      }
      if (patPackCont.size() > 0) {
        List<BasicDynaBean> patPackContents = new ArrayList<>();
        for (BasicDynaBean patPackContent : patPackCont) {
          if (contentRefMap.containsKey(patPackContent.get("patient_package_content_id"))) {
            patPackContent.set("content_id_ref",
                contentBaseMap.get(
                    contentRefMap.get(patPackContent.get("patient_package_content_id"))));
          } else {
            patPackContent.set("content_id_ref", null);
          }
          patPackContents.add(patPackContent);
        }
        multiVisitPackageService.insertPatientPackageContentCharges(patPackContents,
            patPackContCharges);
      }
    }
    visitParams.put("new_mvp_map", newMvpMap);
    Map<String, Map<String, Object>> billsInfoMap = getBillsInfoMap(orderedItems, 
        visitParams ,patPackContIds, regHeaderInformation);

    Map<String, List<Object>> newInsuranceItems = orderedItems.get("newInsurance");
    Boolean isNewInsurance = newInsuranceItems != null && !newInsuranceItems.isEmpty();

    updateOrderedItems(orderedItems, visitParams, editOrCancelOrderBills,
        billsInfoMap, newMVPDetails);

    // update payment_status to 'P' if patient due is 0 in new insurance bill.
    if (isNewInsurance) {
      BigDecimal patientAmtDue = getPatientAmountDue(newInsuranceItems);
      BasicDynaBean bill = (BasicDynaBean) ((Map<String, Object>) billsInfoMap.get("newInsurance"))
          .get("bill");

      if (patientAmtDue.compareTo(BigDecimal.ZERO) == 0) {
        billService.setBillPaidStatus((String) bill.get("bill_no"));
      }
    }

    Integer centerId = RequestContext.getCenterId();
    for (String billNo : editOrCancelOrderBills) {
      if (billNo != null && !billNo.equals("")) {
        billService.resetTotalsOrReProcess(billNo);
        // Call the allocation job and update the patient payments for the created bill.
        allocationService.updateBillTotal(billNo);
        // Call the Allocation method.
        allocationService.allocate(billNo, centerId);
      }
    }

  }


  private boolean isHasMvpOrder(
       Map<String, List<Object>> newBillItems) {

    if (MapUtils.isEmpty(newBillItems)) {
      return false;
    }

    for (String itemType : newBillItems.keySet()) {
      for (Object item : newBillItems.get(itemType)) {
        if (Boolean.TRUE.equals(((Map)item).get("multi_visit_package"))) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Post the Consultation Doctor Bean and its charges.
   * 
   * @param params
   *          the params
   * @param headerInformation
   *          the headerInformation
   * @param bill
   *          the bill
   * @param centerId
   *          the centerId
   * @param planIds
   *          the planIds
   * @throws ParseException
   *           the ParseException
   * @throws SQLException
   *           the SQLException
   */
  @SuppressWarnings("unchecked")
  public void orderConsulationDoctor(Map<String, Object> params, BasicDynaBean headerInformation,
      BasicDynaBean bill, Integer centerId, int[] planIds) {

    Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
    Integer appointmentId = 0;

    if (!StringUtils.isEmpty(visitParams.get("appointment_id"))) {
      appointmentId = Integer.parseInt(visitParams.get("appointment_id").toString());
    }
    String doctorId = (String) visitParams.get("doctor");
    if (doctorId != null && !doctorId.equals("")) {
      String doctorCharge = "";
      if (visitParams.get("doctorCharge") != null) {
        doctorCharge = visitParams.get("doctorCharge").toString();
      }
      String consDate = (String) visitParams.get("cons_date");
      String consTime = (String) visitParams.get("cons_time");
      String schDocAppointRemarks = "Scheduler Consultation";
      String remarks = visitParams.get("consRemarks") != null
          ? (String) visitParams.get("consRemarks") : "";
      String consRemarks = ((appointmentId > 0) ? schDocAppointRemarks : "") + remarks;
      BasicDynaBean baseDocBean = registrationService.regBaseDoctor(doctorId, doctorCharge,
          consDate, consTime, consRemarks, appointmentId);
      if (baseDocBean != null) {

        baseDocBean.set("mr_no", (String) headerInformation.get("mr_no"));
        baseDocBean.set("common_order_id", headerInformation.get("commonorderid"));
        baseDocBean.set("username", headerInformation.get("user_name"));
        baseDocBean.set("patient_id", headerInformation.get("patient_id"));
        baseDocBean.set(CONSULTATION_ID, doctorOrderItemRepository.getNextSequence());
        doctorOrderService.setConsultationToken(baseDocBean, headerInformation);
        doctorOrderService.setStatus(baseDocBean);
        doctorOrderItemRepository.insert(baseDocBean);
        Map<String, List<Object>> doctorAuths = getOrderAuthObject();
        doctorAuths.get("newPreAuths").add("");
        doctorAuths.get("newPreAuthModesList").add(1);
        doctorAuths.get("secNewPreAuths").add("");
        doctorAuths.get("secNewPreAuthModesList").add(1);
        doctorAuths.get("conductingDoctorList").add("");
        doctorAuths.get("isMultivisitPackageList").add(false);

        String[] finalPreAuthIds = new String[2];
        Integer[] finalPreAuthModeIds = new Integer[2];

        setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, doctorAuths, 0, finalPreAuthIds,
            finalPreAuthModeIds);
        if (null != bill) {
          doctorOrderService.insertOrderCharges(true, headerInformation, baseDocBean, bill,
              finalPreAuthIds, finalPreAuthModeIds, planIds, "", "DOC", centerId, false, null);
        }
      }
    }
  }

  /**
   * Set the preAuthId, PreAuthMode, conductingDoctorId, multiVisitPackage value.
   * 
   * @param finalPreAuthIds
   *          the finalPreAuthIds
   * @param finalPreAuthModeIds
   *          the finalPreAuthModeIds
   * @param orderAuth
   *          the orderAuth
   * @param index
   *          the index
   * @param preAuthIds
   *          the preAuthIds
   * @param preAuthModeIds
   *          the preAuthModeIds
   */
  private void setPreAuthIdAndMode(String[] finalPreAuthIds, Integer[] finalPreAuthModeIds,
      Map<String, List<Object>> orderAuth, int index, String[] preAuthIds,
      Integer[] preAuthModeIds) {

    if (orderAuth.get("newPreAuths").size() > 0 && index < orderAuth.get("newPreAuths").size()) {
      finalPreAuthIds[0] = (String) orderAuth.get("newPreAuths").get(index);
    }
    if (orderAuth.get("secNewPreAuths").size() > 0
        && index < orderAuth.get("secNewPreAuths").size()) {
      finalPreAuthIds[1] = (String) orderAuth.get("secNewPreAuths").get(index);
    }
    if (orderAuth.get("newPreAuthModesList").size() > 0
        && index < orderAuth.get("newPreAuthModesList").size() && null != preAuthModeIds) {
      finalPreAuthModeIds[0] = (Integer) orderAuth.get("newPreAuthModesList").get(index);
    }
    if (orderAuth.get("secNewPreAuthModesList").size() > 0
        && index < orderAuth.get("secNewPreAuthModesList").size() && null != preAuthModeIds) {
      finalPreAuthModeIds[1] = (Integer) orderAuth.get("secNewPreAuthModesList").get(index);
    }
  }

  /**
   * Create dummy object to set all parameter from request Params.
   * 
   * @return map
   */
  private Map<String, List<Object>> getOrderAuthObject() {
    Map<String, List<Object>> orderAuths = new HashMap<String, List<Object>>();
    orderAuths.put("newPreAuths", new ArrayList<Object>());
    orderAuths.put("newPreAuthModesList", new ArrayList<Object>());
    orderAuths.put("secNewPreAuths", new ArrayList<Object>());
    orderAuths.put("secNewPreAuthModesList", new ArrayList<Object>());
    orderAuths.put("conductingDoctorList", new ArrayList<Object>());
    orderAuths.put("isMultivisitPackageList", new ArrayList<Object>());
    return orderAuths;
  }

  /**
   * Calls the respective Item Type OrderService.
   * 
   * @param requestParams
   *          the requestParams
   * @param headerInformation
   *          the headerInformation
   * @param preAuthModeIds
   *          the preAuthModeIds
   * @param preAuthIds
   *          the preAuthIds
   * @param planIds
   *          the planIds
   * @param centerId
   *          the centerId
   * @throws SQLException
   *           the SQLException
   * @throws ParseException
   *           the ParseException
   * @throws IOException
   *           the IOException
   */
  @SuppressWarnings("unchecked")
  private void callRespectiveOrderServices(Map<String, List<Object>> requestParams,
      BasicDynaBean headerInformation, String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds,
      BasicDynaBean bill, Integer centerId, Integer patientPackId, Boolean isNewMVP)
      throws SQLException, ParseException, IOException {

    String username = (String) headerInformation.get("user_name");
    String bedType = (String) headerInformation.get("bed_type");
    String orgId = (String) headerInformation.get("bill_rate_plan_id");

    Map<String, List<Integer>> patientPackageIdsMap = new HashMap<String, List<Integer>>();
    insertOrderSetsIntoPatientPackages(requestParams, patientPackageIdsMap,
        (String) headerInformation.get("mr_no"));

    Map<String, List<Object>> doctorAuths = getOrderAuthObject();
    List<BasicDynaBean> doctorBean = doctorOrderService.getOrderBean(requestParams,
        headerInformation, username, doctorAuths, preAuthIds, preAuthModeIds);
    if (null != doctorBean && doctorBean.size() != 0) {
      doctorOrderService.insertOrderBeanList(doctorBean);
      Object[] orderedItemList = requestParams.get("doctors").toArray();
      List<Integer> patientPackageIdsList = patientPackageIdsMap.get("doctors");
      for (int i = 0; i < doctorBean.size(); i++) {
        String condDoctorId = null;
        String[] finalPreAuthIds = new String[2];
        Integer[] finalPreAuthModeIds = new Integer[2];
        Map<String, Object> doctorsItemDetails = (Map<String, Object>) orderedItemList[i];
        BasicDynaBean mainChargeBean = null;

        Boolean isMultivisitPackage = (Boolean) doctorAuths.get("isMultivisitPackageList").get(i);
        if (doctorAuths.get("conductingDoctorList").size() > 0
            && i < doctorAuths.get("conductingDoctorList").size()) {
          condDoctorId = (String) doctorAuths.get("conductingDoctorList").get(i);
        }
        Integer doctorPrescriptionId = (Integer) doctorBean.get(i).get("doc_presc_id");
        if (doctorPrescriptionId != null) {
          doctorOrderService.updatePrescription("O",
              doctorPrescriptionId);
        } else {
          // For followups the doc_prescId does not exist
          // so we will have to update with ppd id.          
          Long patientPendingPrescId = (doctorsItemDetails.get("pat_pending_presc_id") != null 
              && !"null".equals(doctorsItemDetails.get("pat_pending_presc_id").toString()))
              ? Long.valueOf(doctorsItemDetails.get("pat_pending_presc_id")
                  .toString()) : null;
          if (null == patientPendingPrescId
              && null != doctorsItemDetails.get("doctors_pat_pending_presc_id")) {
            Long docPatPendingPrescId = Long.valueOf(
                doctorsItemDetails.get("doctors_pat_pending_presc_id").toString());
            if (null != docPatPendingPrescId) {
              patientPendingPrescId = docPatPendingPrescId;
            }
          }
          if (null != patientPendingPrescId) {
            pendingPrescriptionsService.updatePendingPrescriptionStatus(
                patientPendingPrescId.toString(), "O");
          }        
        }

        setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, doctorAuths, i, finalPreAuthIds,
            finalPreAuthModeIds);
        if (null != bill) {
          List<BasicDynaBean> chargesList = doctorOrderService.insertOrderCharges(true,
              headerInformation, doctorBean.get(i), bill, finalPreAuthIds, finalPreAuthModeIds,
              planIds, condDoctorId, "DOC", centerId, isMultivisitPackage, doctorsItemDetails);
          mainChargeBean = chargesList == null ? null : chargesList.get(0);
        }

        Integer packageId = !"".equals(doctorsItemDetails.get("doctors_package_id"))
            && doctorsItemDetails.get("doctors_package_id") != null
                ? Integer.valueOf(doctorsItemDetails.get("doctors_package_id").toString()) : null;
        Integer packObId = !"".equals(doctorsItemDetails.get("doctors_package_ob_id"))
            && doctorsItemDetails.get("doctors_package_ob_id") != null
                ? Integer.valueOf(doctorsItemDetails.get("doctors_package_ob_id").toString())
                : null;
        if (null == packObId) {
          packObId = !"".equals(doctorsItemDetails.get("doctors_pack_ob_id"))
              && doctorsItemDetails.get("doctors_pack_ob_id") != null
                  ? Integer.valueOf(doctorsItemDetails.get("doctors_pack_ob_id").toString())
                  : null;
        }
        boolean isMultiVisitPackItem = (Boolean) doctorsItemDetails
            .get("doctors_multi_visit_package");
        Integer mvpPatientPackageId = null;
        if (isMultiVisitPackItem) {
          mvpPatientPackageId = !"".equals(doctorsItemDetails.get("doctors_pat_package_id"))
                  && doctorsItemDetails.get("doctors_pat_package_id") != null
                  ? ((Integer) doctorsItemDetails.get("doctors_pat_package_id"))
                  : null;
        }
        Integer patientPackageId = patientPackageIdsList.get(i);
        String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
        if ((packageId != null) && (packObId != null) && (patientPackageId != null)
            && !isMultiVisitPackItem) {
          insertIntoPatientPackageContentAndConsumed(patientPackageId, packObId, packageId,
              (String) doctorBean.get(i).get("doctor_name"), 1,
              (Integer) doctorBean.get(i).get("consultation_id"), chargeId, username, "doctor");
        }
        if (null != packageId && isMultiVisitPackItem) {
          multiVisitPackageService.insertPatientPackageConsumed(null, packObId, mvpPatientPackageId,
                1, chargeId, doctorBean.get(i).get("consultation_id"), "doctor");
        }
      }
    }

    Map<String, List<Object>> otherAuths = getOrderAuthObject();
    List<BasicDynaBean> otherOrderBean = otherOrderServices.getOrderBean(requestParams,
        headerInformation, username, otherAuths, preAuthIds, preAuthModeIds);
    if (null != otherOrderBean && otherOrderBean.size() != 0) {
      otherOrderServices.insertOrderBeanList(otherOrderBean);

      Object[] orderedItemList = requestParams.get("others").toArray();
      List<Integer> patientPackageIdsList = patientPackageIdsMap.get("others");
      for (int i = 0; i < otherOrderBean.size(); i++) {
        String condDoctorId = null;
        String[] finalPreAuthIds = new String[2];
        Integer[] finalPreAuthModeIds = new Integer[2];
        Map<String, Object> otherItemDetails = (Map<String, Object>) orderedItemList[i];
        BasicDynaBean mainChargeBean = null;

        Boolean isMultivisitPackage = (Boolean) otherAuths.get("isMultivisitPackageList").get(i);
        if (otherAuths.get("conductingDoctorList").size() > 0
            && i < otherAuths.get("conductingDoctorList").size()) {
          condDoctorId = (String) otherAuths.get("conductingDoctorList").get(i);
        }
        setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, otherAuths, i, finalPreAuthIds,
            finalPreAuthModeIds);
        if (null != bill) {
          List<BasicDynaBean> chargesList = otherOrderServices.insertOrderCharges(true,
              headerInformation, otherOrderBean.get(i), bill, finalPreAuthIds, finalPreAuthModeIds,
              planIds, condDoctorId, "OTC", centerId, isMultivisitPackage, otherItemDetails);
          mainChargeBean = chargesList == null ? null : chargesList.get(0);

        }

        Integer packageId = !"".equals(otherItemDetails.get("others_package_id"))
            && otherItemDetails.get("others_package_id") != null
                ? Integer.valueOf((otherItemDetails.get("others_package_id").toString())) : null;
        Integer packObId = !"".equals(otherItemDetails.get("others_package_ob_id"))
            && otherItemDetails.get("others_package_ob_id") != null
                ? Integer.valueOf(otherItemDetails.get("others_package_ob_id").toString()) : null;
        if (null == packObId) {
          packObId = !"".equals(otherItemDetails.get("others_pack_ob_id"))
              && otherItemDetails.get("others_pack_ob_id") != null
                  ? Integer.valueOf(otherItemDetails.get("others_pack_ob_id").toString())
                  : null;
        }
        boolean isMultiVisitPackItem = (Boolean) otherItemDetails.get("others_multi_visit_package");
        Integer mvpPatientPackageId = null;
        if (isMultiVisitPackItem) {
          mvpPatientPackageId = !"".equals(otherItemDetails.get("others_pat_package_id"))
              && otherItemDetails.get("others_pat_package_id") != null
              ? ((Integer) otherItemDetails.get("others_pat_package_id"))
              : null;
        }
        Integer patientPackageId = patientPackageIdsList.get(i);
        String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
        if ((packageId != null) && (packObId != null) && (patientPackageId != null)
            && !isMultiVisitPackItem) {
          insertIntoPatientPackageContentAndConsumed(patientPackageId, packObId, packageId,
              (String) otherOrderBean.get(i).get("service_name"),
              Integer.valueOf(((BigDecimal) otherOrderBean.get(i).get("quantity")).intValue()),
              (Integer) otherOrderBean.get(i).get("prescribed_id"), chargeId, username, "others");
        }
        if (isMultiVisitPackItem && null != packageId) {
          multiVisitPackageService.insertPatientPackageConsumed(null, packObId, mvpPatientPackageId,
              Integer.valueOf(((BigDecimal) otherOrderBean.get(i).get("quantity")).intValue()),
              chargeId, otherOrderBean.get(i).get("prescribed_id"), "others");
        }
      }
    }

    Map<String, List<Object>> serviceAuths = getOrderAuthObject();
    List<BasicDynaBean> serviceBean = serviceOrderService.getOrderBean(requestParams,
        headerInformation, username, serviceAuths, preAuthIds, preAuthModeIds);
    if (null != serviceBean && serviceBean.size() != 0) {
      serviceOrderService.insertOrderBeanList(serviceBean);

      Object[] orderedItemList = requestParams.get("services").toArray();
      List<Integer> patientPackageIdsList = patientPackageIdsMap.get("services");
      for (int i = 0; i < serviceBean.size(); i++) {
        String condDoctorId = null;
        String[] finalPreAuthIds = new String[2];
        Integer[] finalPreAuthModeIds = new Integer[2];
        Map<String, Object> serviceItemDetails = (Map<String, Object>) orderedItemList[i];
        BasicDynaBean mainChargeBean = null;

        Boolean isMultivisitPackage = (Boolean) serviceAuths.get("isMultivisitPackageList").get(i);
        if (serviceAuths.get("conductingDoctorList").size() > 0
            && i < serviceAuths.get("conductingDoctorList").size()) {
          condDoctorId = (String) serviceAuths.get("conductingDoctorList").get(i);
        }
        if ((serviceBean.get(i)).get("doc_presc_id") != null) {
          Integer doctorPrescriptionId = (Integer) serviceBean.get(i)
              .get("doc_presc_id");
          BasicDynaBean prescriptionBean = servicePrescService
              .findByPrescriptionId(doctorPrescriptionId);
          if (null != prescriptionBean
              && serviceOrderService.getOrderedQuantity(doctorPrescriptionId)
              .compareTo(new BigDecimal((Integer)prescriptionBean.get("qty"))) == 0) {
            serviceOrderService.updatePrescription("O", doctorPrescriptionId);
          } else {
            serviceOrderService.updatePrescription("PA", doctorPrescriptionId);
            Long patientPendingPrescId = (serviceItemDetails.get("pat_pending_presc_id") != null) 
                ? Long.valueOf(serviceItemDetails.get("pat_pending_presc_id").toString()) : null;
            if (null == patientPendingPrescId
                && null != serviceItemDetails.get("services_pat_pending_presc_id")) {
              // Only for services it can come as services_pat_pending_presc_id due to the prefix.
              Long servicePatPendingPrescId = Long.valueOf(
                  serviceItemDetails.get("services_pat_pending_presc_id").toString());
              if (null != servicePatPendingPrescId) {
                patientPendingPrescId = servicePatPendingPrescId;
              }
            }
            if (null != patientPendingPrescId) {
              pendingPrescriptionsService.updatePendingPrescriptionStatus(
                  patientPendingPrescId.toString(), "O");
            } else {
              Integer servicesQuantity = (Integer)serviceItemDetails.get("services_quantity");
              if (servicesQuantity > 0) {
                pendingPrescriptionsService.updatePendingPrescriptionStatus(
                    doctorPrescriptionId,1, "O");
              }
            }
          } 
        } else {
          Long patientPendingPrescId = (serviceItemDetails
              .get("services_pat_pending_presc_id") != null && !"null".equals(serviceItemDetails
              .get("services_pat_pending_presc_id").toString())) ? Long.valueOf(serviceItemDetails
              .get("services_pat_pending_presc_id").toString()) : null;
          if (null == patientPendingPrescId 
              && null != serviceItemDetails.get("pat_pending_presc_id")) {
            patientPendingPrescId = (Long) serviceItemDetails.get("pat_pending_presc_id");
          }
          if (null != patientPendingPrescId) {
            updatePriorAuthForPendingPrescription(patientPendingPrescId);
          }
        }

        setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, serviceAuths, i, finalPreAuthIds,
            finalPreAuthModeIds);
        if (null != bill) {
          List<BasicDynaBean> chargesList = serviceOrderService.insertOrderCharges(true,
              headerInformation, serviceBean.get(i), bill, finalPreAuthIds, finalPreAuthModeIds,
              planIds, condDoctorId, "SER", centerId, isMultivisitPackage, serviceItemDetails);
          mainChargeBean = chargesList == null ? null : chargesList.get(0);
        }

        Integer packageId = !"".equals(serviceItemDetails.get("services_package_id"))
            && serviceItemDetails.get("services_package_id") != null
                ? Integer.valueOf(serviceItemDetails.get("services_package_id").toString()) : null;
        Integer packObId = !"".equals(serviceItemDetails.get("services_package_ob_id"))
            && serviceItemDetails.get("services_package_ob_id") != null
                ? Integer.valueOf(serviceItemDetails.get("services_package_ob_id").toString())
                : null;
        if (null == packObId) {
          packObId = !"".equals(serviceItemDetails.get("services_pack_ob_id"))
              && serviceItemDetails.get("services_pack_ob_id") != null
                  ? Integer.valueOf(serviceItemDetails.get("services_pack_ob_id").toString())
                  : null;
        }
        boolean isMultiVisitPackItem = (Boolean) serviceItemDetails
            .get("services_multi_visit_package");
        Integer patientPackageId = (Integer) patientPackageIdsList.get(i);
        Integer mvpPatientPackageId = null;
        if (isMultiVisitPackItem) {
          mvpPatientPackageId = !"".equals(serviceItemDetails.get("services_pat_package_id"))
                  && serviceItemDetails.get("services_pat_package_id") != null
                  ? ((Integer) serviceItemDetails.get("services_pat_package_id"))
                  : null;
        }
        String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
        if ((packageId != null) && (packObId != null) && (patientPackageId != null)
            && !isMultiVisitPackItem) {
          insertIntoPatientPackageContentAndConsumed(patientPackageId, packObId, packageId,
              (String) serviceBean.get(i).get("service_id"),
              Integer.valueOf(((BigDecimal) serviceBean.get(i).get("quantity")).intValue()),
              (Integer) serviceBean.get(i).get("prescription_id"), chargeId, username, "services");
        }
        if (isMultiVisitPackItem && null != packageId) {
          multiVisitPackageService.insertPatientPackageConsumed(null, packObId, mvpPatientPackageId,
              Integer.valueOf(((BigDecimal) serviceBean.get(i).get("quantity")).intValue()),
              chargeId, serviceBean.get(i).get("prescription_id"), "services");
        }

        String healthAuthority =
                CenterMasterDAO.getHealthAuthorityForCenter(RequestContext.getCenterId());
        if ("DHA".equals(healthAuthority)) {
          String serviceId = (String) serviceItemDetails.get("services_item_id");
          String splServiceCode = null;
          BasicDynaBean specialCodeBean = serviceOrderService.getMasterSpecialCodeBean(serviceId,
              (String) bill.get("bill_rate_plan_id"));
          if (specialCodeBean != null) {
            splServiceCode = (String) specialCodeBean.get("special_service_code");
          }
          if (splServiceCode != null && !splServiceCode.isEmpty()) {
            BasicDynaBean obsBean = mrdObservationsRepository.getBean();
            obsBean.set("charge_id", chargeId);
            obsBean.set("observation_type", "Grouping");
            obsBean.set("code", "PackageID");
            obsBean.set("value", splServiceCode);
            obsBean.set("value_type", "Other");
            obsBean.set("value_editable", "N");
            mrdObservationsRepository.insert(obsBean);
          }
        }
      }
    }

    Map<String, List<Object>> testAuths = getOrderAuthObject();
    List<BasicDynaBean> testBean = testOrderService.getOrderBean(requestParams, headerInformation,
        username, testAuths, preAuthIds, preAuthModeIds);
    if (null != testBean && testBean.size() != 0) {

      Object[] orderedItemList = requestParams.get("tests").toArray();
      testOrderService.updateTestDocuments(Arrays.asList(orderedItemList), testBean);

      testOrderService.insertOrderBeanList(testBean);

      List<Integer> patientPackageIdsList = patientPackageIdsMap.get("tests");
      for (int i = 0; i < testBean.size(); i++) {
        String condDoctorId = null;
        String[] finalPreAuthIds = new String[2];
        Integer[] finalPreAuthModeIds = new Integer[2];
        Map<String, Object> testItemDetails = (Map<String, Object>) orderedItemList[i];
        BasicDynaBean mainChargeBean = null;

        Boolean isMultivisitPackage = (Boolean) testAuths.get("isMultivisitPackageList").get(i);
        if (testAuths.get("conductingDoctorList").size() > 0
            && i < testAuths.get("conductingDoctorList").size()) {
          condDoctorId = (String) testAuths.get("conductingDoctorList").get(i);
        }
        Integer doctorPrescriptionId = (Integer) testBean.get(i).get("doc_presc_id");
        if (doctorPrescriptionId != null) {
          testOrderService.updatePrescription("O",doctorPrescriptionId);
          updatePriorAuthForPrescription(doctorPrescriptionId, 1);
        } else {
          Long patientPendingPrescId = (testItemDetails
              .get("tests_pat_pending_presc_id") != null && !"null".equals(testItemDetails
              .get("tests_pat_pending_presc_id").toString())) ? Long.valueOf(testItemDetails
              .get("tests_pat_pending_presc_id").toString()) : null;
          if (null == patientPendingPrescId 
              && null != testItemDetails.get("pat_pending_presc_id")) {
            patientPendingPrescId = Long.valueOf(
                testItemDetails.get("pat_pending_presc_id").toString());
          }
          if (null != patientPendingPrescId) {
            updatePriorAuthForPendingPrescription(patientPendingPrescId);
          }
        }

        setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, testAuths, i, finalPreAuthIds,
            finalPreAuthModeIds);
        if (null != bill) {
          List<BasicDynaBean> chargesList = testOrderService.insertOrderCharges(true,
              headerInformation, testBean.get(i), bill, finalPreAuthIds, finalPreAuthModeIds,
              planIds, condDoctorId, "DIA", centerId, isMultivisitPackage, testItemDetails);
          mainChargeBean = chargesList == null ? null : chargesList.get(0);
        }

        Integer packageId = !"".equals(testItemDetails.get("tests_package_id"))
            && testItemDetails.get("tests_package_id") != null
                ? Integer.valueOf(testItemDetails.get("tests_package_id").toString()) : null;
        Integer packObId = !"".equals(testItemDetails.get("tests_package_ob_id"))
            && testItemDetails.get("tests_package_ob_id") != null
                ? Integer.valueOf(testItemDetails.get("tests_package_ob_id").toString()) : null;
        if (null == packObId) {
          packObId = !"".equals(testItemDetails.get("tests_pack_ob_id"))
              && testItemDetails.get("tests_pack_ob_id") != null
                  ? Integer.valueOf(testItemDetails.get("tests_pack_ob_id").toString())
                  : null;
        }
        boolean isMultiVisitPackItem = (Boolean) testItemDetails.get("tests_multi_visit_package");
        Integer mvpPatientPackageId = null;
        if (isMultiVisitPackItem) {
          mvpPatientPackageId = !"".equals(testItemDetails.get("tests_pat_package_id"))
                  && testItemDetails.get("tests_pat_package_id") != null
                  ? ((Integer) testItemDetails.get("tests_pat_package_id"))
                  : null;
        }
        Integer patientPackageId = (Integer) patientPackageIdsList.get(i);
        String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
        if ((packageId != null) && (packObId != null) && (patientPackageId != null)
            && !isMultiVisitPackItem) {
          insertIntoPatientPackageContentAndConsumed(patientPackageId, packObId, packageId,
              (String) testBean.get(i).get("test_id"), 1,
              (Integer) testBean.get(i).get("prescribed_id"), chargeId, username, "tests");
        }
        if (isMultiVisitPackItem && null != packageId) {
          multiVisitPackageService.insertPatientPackageConsumed(null, packObId, mvpPatientPackageId,
              1, chargeId, testBean.get(i).get("prescribed_id"), "tests");
        }
      }
    }

    if (!isNewMVP) {
      BasicDynaBean prescribedMultiVisitPackageBean = multiVisitPackageService
              .getNewMultiVisitPackageOrders(requestParams);
      if (prescribedMultiVisitPackageBean != null) {
        multiVisitPackageService.insertPackageOrderItemBean(prescribedMultiVisitPackageBean,
                headerInformation, username);

      }
    }

    Map<String, List<Object>> packageAuths = getOrderAuthObject();
    List<BasicDynaBean> packageBean = packageOrderItemService.getOrderBean(requestParams,
        headerInformation, username, packageAuths, preAuthIds, preAuthModeIds);
    if (null != packageBean && packageBean.size() != 0) {
      packageOrderItemService.insertOrderBeanList(packageBean);

      Object[] orderedItemList = requestParams.get("packages").toArray();
      for (int i = 0; i < packageBean.size(); i++) {
        String[] finalPreAuthIds = new String[2];
        Integer[] finalPreAuthModeIds = new Integer[2];
        Map<String, Object> packageItemDetails = (Map<String, Object>) orderedItemList[i];

        if (packageAuths.get("newPreAuths").size() > 0
            && i < packageAuths.get("newPreAuths").size()) {
          finalPreAuthIds[0] = (String) packageAuths.get("newPreAuths").get(i);
        }
        if (((BasicDynaBean) packageBean.get(i)).get("doc_presc_id") != null) {
          packageOrderItemService.updatePrescription("O",
              (Integer) ((BasicDynaBean) packageBean.get(i)).get("doc_presc_id"));
        } else {
          String patientPendingPrescId = (String)packageItemDetails
              .get("packages_pat_pending_presc_id");
          if (null == patientPendingPrescId 
              && packageItemDetails.containsKey("pat_pending_presc_id")) {
            patientPendingPrescId = packageItemDetails
                .get("pat_pending_presc_id").toString();
          }
          if (null != patientPendingPrescId && !"null".equals(patientPendingPrescId)) {
            updatePriorAuthForPendingPrescription(Long.parseLong(patientPendingPrescId));
          }
        }

        packageOrderItemService.insertPatientPackages(packageBean.get(i));
        BasicDynaBean mainChargeBean = null;
        List<BasicDynaBean> chargesList = null;
        if (null != bill) {
          chargesList = packageOrderItemService.insertOrderCharges(true,
              headerInformation, packageBean.get(i), bill, finalPreAuthIds, finalPreAuthModeIds,
              planIds, "", "PKG", centerId, false, packageItemDetails);
        }
        packageOrderItemService.insertPackageComponent(packageBean.get(i), headerInformation,
            username, centerId, chargesList, packageItemDetails);

        packageOrderItemService.insertPatientPackageContents(packageItemDetails,packageBean.get(i),
            chargesList, username, bedType, orgId);
      }
    }
  }

  /**
   * This method is responsible for inserting MultiVistPackageContents from registration screen
   * order panel.
   * @param params the request parameter
   * @param headerInformation the headerInformation
   * @param visitId the visitId
   */
  public Integer insertMultiVistPackageContents(Map<String, Object> params,
      BasicDynaBean headerInformation, String visitId) {
    List<Map<String, Object>> newMVPDetails = (List<Map<String, Object>>) params
        .get("new_mvp_details");
    Map<Integer, Object> newMvpMap = new HashMap<Integer, Object>();
    Map<Integer, String> contentRefMap = new HashMap<>();
    Map<String, Integer> contentBaseMap = new HashMap<>();
    Integer patientPackageId = 0;
    String userName = RequestContext.getUserName();
    if (newMVPDetails != null && !newMVPDetails.isEmpty()) {
      String bedType = (String) headerInformation.get("bed_type");
      String orgId = (String) headerInformation.get("bill_rate_plan_id");
      String mrNo = (String) headerInformation.get("mr_no");
      List<BasicDynaBean> patPackCont = new ArrayList<>();
      List<BasicDynaBean> patPackContCharges = new ArrayList<>();
      for (Map<String, Object> newMvpDetail : newMVPDetails) {
        Map<String, Object> packDetails = (Map<String, Object>) newMvpDetail.get("package_details");
        String submissionType = String.valueOf('P');
        if (packDetails != null) {
          submissionType = (String) packDetails.get("submission_batch_type");
        }
        if (newMvpDetail.get("is_customized") != null) {
          packDetails.put("is_customized", newMvpDetail.get("is_customized"));
        }
        List<Map<String, Object>> packContents = (List<Map<String, Object>>) newMvpDetail
                .get("package_contents");
        Integer mvpPackageId = (Integer) newMvpDetail.get("package_id");
        BasicDynaBean patPackBean = multiVisitPackageService.insertPatientPackageBean(mrNo,
            (Object) mvpPackageId);
        patientPackageId = multiVisitPackageService.insertPackageOrderItemBean(patPackBean,
            headerInformation, userName);
        Integer patPackId = (Integer) patPackBean.get("pat_package_id");
        newMvpMap.put((Integer) newMvpDetail.get("mvp_index"), patPackId);
        for (Map<String, Object> packCont : packContents) {
          Integer patPackContentId = multiVisitPackageService.getPatientPackageContentCharges(
              packCont, userName, bedType, orgId, patPackCont, patPackContCharges, visitId,
              mvpPackageId, patPackId, submissionType);
          if (packCont.get("content_id_ref") != null) {

            contentRefMap.put(patPackContentId, (String) packCont.get("content_id_ref"));
            if (!contentBaseMap.containsKey(packCont.get("content_id_ref"))) {
              contentBaseMap.put((String) packCont.get("content_id_ref"), patPackContentId);
            }
          }
        }
        if (packDetails != null) {
          multiVisitPackageService.insertPatientPackageCustomized(patPackId, packDetails, userName);
        }
      }
      if (!patPackCont.isEmpty()) {
        List<BasicDynaBean> patPackContents = new ArrayList<>();
        for (BasicDynaBean patPackContent : patPackCont) {
          if (contentRefMap.containsKey(patPackContent.get("patient_package_content_id"))) {
            patPackContent.set("content_id_ref", contentBaseMap
                .get(contentRefMap.get(patPackContent.get("patient_package_content_id"))));
          } else {
            patPackContent.set("content_id_ref", null);
          }
          patPackContents.add(patPackContent);
        }
        multiVisitPackageService.insertPatientPackageContentCharges(patPackContents,
            patPackContCharges);
      }
    }
    return patientPackageId;
  }
  
  private void updatePriorAuthForPrescription(Integer prescriptionId, int quantity) {
    //get preauth_act_id from prescId
    Integer preauthActId = preAuthItemsService.getPriorAuthItemIdFromPrescId(prescriptionId);
    //update quantity.
    if (null != preauthActId) {
      preAuthItemsService.updatePreAuthItemQuantity(preauthActId, 1, false, true, null);
    }
  }
  
  private void updatePriorAuthForPendingPrescription(Long pendingPrescriptionId) {
    //get preauth_act_id from pendingPrescId
    Integer preauthActId = pendingPrescriptionsService
        .getPriorAuthActIdFromPendingPrescId(pendingPrescriptionId);
    if (null == preauthActId) {
      //Update pending prescriptions status to Ordered
      pendingPrescriptionsService.updatePendingPrescriptionStatus(pendingPrescriptionId.toString(),
          "O");
    }
  }

  private void insertIntoPatientPackageContentAndConsumed(Integer patientPackageId,
      Integer packageContentId, Integer packageId, String activityId, Integer consumedQuantity,
      Integer prescriptionId, String chargeId, String username, String itemType) {
    // Inserting into new patient_package_contents table.
    BasicDynaBean ppcBean = patPackContentsRepo.getBean();
    BasicDynaBean packageContent = orderSetsService.getPackageContentDetail(packageId,
        packageContentId);
    Integer patientPackageContentId = patPackContentsRepo.getNextSequence();
    ppcBean.set("patient_package_content_id", patientPackageContentId);
    ppcBean.set("patient_package_id", patientPackageId);
    ppcBean.set("package_content_id", packageContentId);
    ppcBean.set("package_id", packageId);
    ppcBean.set("activity_id", activityId);
    ppcBean.set("consultation_type_id", packageContent.get("consultation_type_id"));
    ppcBean.set("activity_type", packageContent.get("activity_type"));
    ppcBean.set("activity_qty", packageContent.get("activity_qty"));
    ppcBean.set("charge_head", packageContent.get("charge_head"));
    ppcBean.set("created_by", username);
    patPackContentsRepo.insert(ppcBean);

    // inserting into patient_package_consumed
    BasicDynaBean ppccBean = patPackContentConsumedRepo.getBean();
    ppccBean.set("patient_package_content_id", patientPackageContentId);
    ppccBean.set("quantity",
        consumedQuantity != null ? consumedQuantity : packageContent.get("activity_qty"));
    ppccBean.set("prescription_id", prescriptionId);
    ppccBean.set("bill_charge_id", chargeId);
    ppccBean.set("item_type", itemType);
    patPackContentConsumedRepo.insert(ppccBean);
  }

  private void insertOrderSetsIntoPatientPackages(Map<String, List<Object>> requestParams,
      Map<String, List<Integer>> patientPackageIdsMap, String mrNo) {
    Map<Integer, Integer> packagesMap = new HashMap<Integer, Integer>();
    for (Map.Entry<String, List<Object>> itemsEntry : requestParams.entrySet()) {
      String key = itemsEntry.getKey();
      for (Object item : itemsEntry.getValue()) {
        Map<String, Object> itemMap = (Map<String, Object>) item;
        Integer packageId = !"".equals(itemMap.get(key + "_package_id"))
            && itemMap.get(key + "_package_id") != null
                ? Integer.valueOf(itemMap.get(key + "_package_id").toString()) : null;

        Integer packageObId = !"".equals(itemMap.get(key + "_pack_ob_id"))
            && itemMap.get(key + "_pack_ob_id") != null
                ? Integer.valueOf(itemMap.get(key + "_pack_ob_id").toString()) : null;
        Boolean isMultiVisitPackage = (Boolean) itemMap.get(key + "_multi_visit_package");
        Integer patPackageId = null;
        if (packageId != null && packageObId != null && !isMultiVisitPackage) {
          patPackageId = packagesMap.get(packageId);
          if (patPackageId == null) {
            BasicDynaBean bean = patientPackageRepo.getBean();
            patPackageId = patientPackageRepo.getNextSequence();
            bean.set("patient_package_id", patPackageId);
            bean.set("mr_no", mrNo);
            bean.set("package_id", packageId);
            bean.set("status", "C");
            patientPackageRepo.insert(bean);
            packagesMap.put(packageId, patPackageId);
          }
        }
        List<Integer> patientPackagesList = patientPackageIdsMap.get(key);
        if (patientPackagesList == null) {
          patientPackagesList = new ArrayList<Integer>();
          patientPackageIdsMap.put(key, patientPackagesList);
        }
        patientPackagesList.add(patPackageId);
      }
    }

  }

  /**
   * Return the List of newly added ordered item, and delete/edited ordered item.
   * 
   * @param newCharges
   *          the newCharges
   * @param editedCharges
   *          the editedCharges
   * @param orderList
   *          the orderList
   * @param insuranceList
   *          the insuranceList
   */
  @SuppressWarnings("unchecked")
  public void addOrderCharges(ArrayList<Map<String, Object>> newCharges,
      List<Map<String, Object>> editedCharges, List orderList, ArrayList insuranceList) {

    String isClaimAmtIncludesTax = "N";
    String isLimitIncludesTax = "N";

    if (null != insuranceList && !insuranceList.isEmpty()) {
      Map insParams = (Map) insuranceList.get(0);
      String priSponsorId = (String) insParams.get("sponsor_id");
      Map<String, Object> keys = new HashMap<>();
      keys.put("tpa_id", priSponsorId);
      BasicDynaBean tpaBean = tpaService.findByPk(keys);
      if (null != tpaBean) {
        isClaimAmtIncludesTax = (String) tpaBean.get("claim_amount_includes_tax");
        isLimitIncludesTax = (String) tpaBean.get("limit_includes_tax");
      }
    }

    for (int k = 0; k < orderList.size(); k++) {
      Map<String, Object> obj = (Map<String, Object>) orderList.get(k);
      if (obj.get("package_contents") != null) {
        ArrayList packContents = (ArrayList) obj.get("package_contents");
        addOrderCharges(newCharges, editedCharges, packContents, insuranceList);
      }
      if ((null != obj.get("type") && !obj.get("type").equals(""))
          && ((null != obj.get("new") && obj.get("new").equals("Y"))
              || (null != obj.get(CANCELLED) && !obj.get(CANCELLED).equals(""))
              || (null != obj.get(EDITED) && !obj.get(EDITED).equals("")))) {

        Map<String, Object> chargeMap = createOrderItemMap(obj, isClaimAmtIncludesTax,
            isLimitIncludesTax, k);

        if ((null != obj.get(CANCELLED) && obj.get(CANCELLED).equals("IC"))) {
          chargeMap.put("amount", BigDecimal.ZERO);
          chargeMap.put("discount", BigDecimal.ZERO);
          editedCharges.add(chargeMap);
        } else if ((null != obj.get(CANCELLED) && obj.get(CANCELLED).equals("I"))
            || (null != obj.get(EDITED) && obj.get(EDITED).equals("Y"))) {
          if (null != obj.get("is_tpa") && !((Boolean) obj.get("is_tpa"))) {
            chargeMap.put("amount", BigDecimal.ZERO);
            chargeMap.put("discount", BigDecimal.ZERO);
          }
          if ((null != obj.get("new") && obj.get("new").equals("Y"))) {
            newCharges.add(chargeMap);
          } else {
            editedCharges.add(chargeMap);
          }
        } else {
          if ((null != obj.get("bill_no") && obj.get("bill_no").equals("new"))
              || (null != obj.get("is_tpa") && !((Boolean) obj.get("is_tpa")))) {
            chargeMap.put("amount", BigDecimal.ZERO);
            chargeMap.put("discount", BigDecimal.ZERO);
          }
          newCharges.add(chargeMap);
        }
      }
    }
  }

  /**
   * add order charges.
   * 
   * @param charges
   *          the charges
   * @param orderedItemList
   *          the orderedItemList
   */
  public void addOrderCharges(List<String> charges, List<Map<String, Object>> orderedItemList) {
    for (Map<String, Object> itemMap : orderedItemList) {
      if (null != itemMap.get("charge_id") && !itemMap.get("charge_id").equals("")
          && null != itemMap.get(CANCELLED) && !itemMap.get(CANCELLED).equals("")
          && itemMap.get(CANCELLED).equals("Y")) {
        charges.add("charge_id");
      }
    }
  }

  /**
   * Create ChargeMap for ordered Item.
   * 
   * @param orderItem
   *          the orderItem
   * @param isClaimAmtIncludesTax
   *          the isClaimAmtIncludesTax
   * @param isLimitIncludesTax
   *          the isLimitIncludesTax
   * @param index
   *          the index
   * @return map
   */
  private Map<String, Object> createOrderItemMap(Map<String, Object> orderItem,
      String isClaimAmtIncludesTax, String isLimitIncludesTax, int index) {

    Map<String, Object> chargeMap = new HashMap<String, Object>();

    if (orderItem.get("charge_id") != null) {
      chargeMap.put("charge_id", orderItem.get("charge_id"));
    } else {
      chargeMap.put("charge_id", "_" + (index + 1));
    }

    BigDecimal amt = BigDecimal.ZERO;
    if (null != orderItem.get("amount") && !StringUtils.isEmpty(orderItem.get("amount"))) {
      amt = new BigDecimal((String) orderItem.get("amount"));
    }

    BigDecimal taxAmt = BigDecimal.ZERO;
    if (null != orderItem.get(TAX_AMT) && !StringUtils.isEmpty(orderItem.get(TAX_AMT))) {
      taxAmt = new BigDecimal((String) orderItem.get(TAX_AMT));
    }

    chargeMap.put(INSURANCE_CATEGORY_ID, orderItem.get(INSURANCE_CATEGORY_ID));
    String chargeGroup = (String) orderItem.get("charge_group");
    int catId = -1;
    if ("DOC".equals(chargeGroup)) {
      int consId = (Integer) orderItem.get("consultation_type_id");
      Map<String, Object> params = new HashMap<>();
      params.put("consultation_type_id", consId);
      BasicDynaBean consultationBean = consultationTypesService.findByPk(params);
      if (null != consultationBean) {
        catId = (Integer) consultationBean.get("insurance_category_id");
      }
      chargeMap.put("insurance_category_id", catId);
    }

    if (isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
      chargeMap.put("amount", amt);
    } else {
      chargeMap.put("amount", amt.subtract(taxAmt));
    }

    BigDecimal discount = BigDecimal.ZERO;
    if (null != orderItem.get("discount") && !orderItem.get("discount").toString().isEmpty()) {
      discount = new BigDecimal(orderItem.get("discount").toString());
    }

    chargeMap.put("discount", discount);
    chargeMap.put(CONSULTATION_TYPE_ID, orderItem.get(CONSULTATION_TYPE_ID));
    Boolean chargeHeadPayable = billService
        .isChargeHeadPayable((String) orderItem.get("charge_head"),
            (String) orderItem.get("charge_group"));
    chargeMap.put("is_insurance_payable", chargeHeadPayable);
    
    // Lock the claim if the preauth_act_status is Denied.
    Boolean isClaimLocked = "D".equals(orderItem.get("preauth_act_status"));

    chargeMap.put("charge_head_id", orderItem.get("charge_head"));
    chargeMap.put("charge_group", orderItem.get("charge_group"));
    chargeMap.put("billing_group_id", orderItem.get("billing_group_id"));
    chargeMap.put("is_claim_locked", isClaimLocked);
    chargeMap.put("primclaimAmt", "0");
    chargeMap.put("secclaimAmt", "0");
    chargeMap.put("store_item_category_payable", true);
    chargeMap.put("pri_include_in_claim", "Y");
    chargeMap.put("sec_include_in_claim", "Y");
    chargeMap.put("act_description_id", orderItem.get("act_description_id"));
    chargeMap.put("package_id", orderItem.get("package_id"));
    chargeMap.put("consultationTypeId", orderItem.get(CONSULTATION_TYPE_ID)); // TODO: why twice?
    if (orderItem.get("package_id") != null && "OPE".equals(orderItem.get("charge_group")) 
        && orderItem.get("op_id") == null) {
      chargeMap.put("op_id",orderItem.get("act_description_id"));
    } else {
      chargeMap.put("op_id", orderItem.get("op_id"));
    }
    chargeMap.put("item_excluded_from_doctor", orderItem.get("item_excluded_from_doctor"));
    chargeMap.put("item_excluded_from_doctor_remarks",
        orderItem.get("item_excluded_from_doctor_remarks"));
    Integer preauthActId = (Integer) orderItem.get("preauth_act_id");
    if (null == preauthActId && orderItem.containsKey("prior_auth_item_id")) {
      preauthActId = (Integer) orderItem.get("prior_auth_item_id");
    }
    chargeMap.put("preauth_act_id", preauthActId);
    billHelper.saveBillChargeBillingGroup(chargeMap);
    return chargeMap;
  }

  /**
   * Call this method for new orders where a new Bill with/Without Insurance needs to be Created.
   * 
   * @param isInsurance
   *          the isInsurance
   * @param params
   *          the params
   * @return map
   */
  public Map<String, Object> createBill(boolean isInsurance, Map<String, Object> params) {
    Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
    String visitId = (String) visitParams.get("visit_id");
    /*
     * Here hard coded third parameter, because method signature changed now using this method we
     * can create both bill now and bill later bills.
     */
    return billService.createBill(isInsurance, visitId, "P");
  }

  /**
   * Cancel charges.
   * 
   * @param params
   *          the params
   * @return map
   * @throws SQLException
   *           the SQLException
   * @throws ParseException
   *           the ParseException
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> cancel(Map<String, Object> params)
      throws SQLException, ParseException {
    Map<String, List<Object>> orderItems = (Map<String, List<Object>>) params.get("ordered_items");
    List<Map<String, Object>> orderList = addOrderItemCharges(orderItems);
    Map<String, Object> map = billService.cancel(orderList);
    if (null != map || !map.isEmpty()) {
      return map;
    }
    List<String> billNos = new ArrayList<String>();
    billService.getCancelledEditedItemsBills(billNos, orderItems);
    for (String billNo : billNos) {
      // resetReprocessBill(billNo,map);
    }
    Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
    String visitId = (String) visitParams.get("visit_id");
    callInsuranceCalculator(visitId);
    if (map != null && map.isEmpty()) {
      map.put("success", "Bill Charges Updated successfully");
    }
    return map;
  }

  private void resetReprocessBill(String billNo) throws ParseException {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userId = (String) sessionAttributes.get("userId");
    String centerId = (String) sessionAttributes.get("centerId");

    billService.resetServiceTaxClaimCharge(billNo, userId);

    /**
     * Except from bill screen and edit pharmacy item amounts screen, automatic process is done from
     * other screens if a charge is added/cancelled.
     */
    billService.dynaPackageProcessor(billNo);

  }

  /**
   * get activity code.
   * 
   * @param type
   *          the type.
   * @return ActivityCode
   */
  public String getActivityCode(String type) {
    String activityCode = "";
    if (null != type && !type.equals("")) {
      if (type.equals("Laboratory") || type.equals("Radiology")) {
        activityCode = "DIA";
      } else if (type.equals("Doctor")) {
        activityCode = "DOC";
      } else if (type.equals("Other Charge")) {
        activityCode = "OTC";
      } else if (type.equals("Meal")) {
        activityCode = "DIE";
      } else if (type.equals("Operation")) {
        activityCode = "OPE";
      } else if (type.equals("Package")) {
        activityCode = "PKG";
      } else if (type.equals("Equipment")) {
        activityCode = "EQU";
      } else if (type.equals("Service")) {
        activityCode = "SER";
      }
    }
    return activityCode;
  }

  /**
   * add order item charges.
   * 
   * @param orderItems
   *          the orderItems
   * @return list of map
   */
  public List<Map<String, Object>> addOrderItemCharges(Map<String, List<Object>> orderItems) {
    List<Map<String, Object>> obj = new ArrayList<Map<String, Object>>();
    Object[] orderedItemList = orderItems.get("services").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("tests").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("doctors").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("equipments").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("others").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("packages").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("operation").toArray();
    addToList(obj, orderedItemList);
    orderedItemList = orderItems.get("diet").toArray();
    addToList(obj, orderedItemList);
    return obj;
  }

  private void addToList(List<Map<String, Object>> obj, Object[] orderedItemList) {
    for (int z = 0; z < orderedItemList.length; z++) {
      obj.add((Map<String, Object>) orderedItemList[z]);
    }
  }

  /*
   * Co-Pay calculations based on Insurance-Rules.
   */
  private void callInsuranceCalculator(String patientId) throws SQLException {
    sponsorService.recalculateSponsorAmount(patientId);
  }

  public List<BasicDynaBean> getUnpaidBillsForVisit(String visitId, String visitType,
      String pharmacy, String includeMultiVisitPackageBills, Date orderDateTime) {
    return billService.getUnpaidBillsForVisit(visitId, visitType, pharmacy,
        includeMultiVisitPackageBills, orderDateTime);
  }

  protected List<BasicDynaBean> getMvpUnpaidBillsForVisit(String visitId, Integer patPackageId,
      Date orderDateTime) {
    return billService.getMvpUnpaidBillsForVisit(visitId, patPackageId, orderDateTime);
  }

  protected List<BasicDynaBean> getMvpBillsForVisit(String visitId) {
    return billService.getMvpBillsForVisit(visitId);
  }

  /**
   * validation for visit.
   * 
   * @param map
   *          the map
   * @param patientDetails
   *          the patientDetails
   */
  public void validate(Map<String, String> map, BasicDynaBean patientDetails) {
    if (null == patientDetails || (null != patientDetails && patientDetails.getMap().isEmpty())) {
      map.put("error", "Invalid visit ID");
    } else if (patientDetails.get("visit_status").equals("I")) {
      map.put("error", "Patient visit is not active");
    }
  }

  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getInsuranceDetails(String visitId, String visitType) {
    return patientInsurancePlansService.getInsuranceDetails(visitId, visitType);
  }

  public BasicDynaBean getVisitRatePlan(String visitId, String visitType) {
    return registrationService.getVisitRatePlan(visitId, visitType);
  }

  /**
   * Returns All Visits that have Order.
   * 
   * @param mrNo
   *          the mrNo
   * @param visitType
   *          the visitType
   * @return list of basic dyna bean
   */
  private List<BasicDynaBean> getPatientVisitsWithOrder(String mrNo, String visitType) {
    return registrationService.getPatientVisitsHavingOrder(mrNo, visitType);
  }

  /**
   * Returns all visits, active visits having order, inactive visit having order.
   * 
   * @param mrNo
   *          nthe mrNo
   * @return map
   */
  public Map<String, Object> getVisitDetails(String mrNo, String visitType) {

    Map<String, Object> map = new HashMap<>();
    List<BasicDynaBean> patientVisitsWithOrder = getPatientVisitsWithOrder(mrNo, visitType);

    List<Map> inActiveVisits = new ArrayList<>();
    List<Map> activeVisits = new ArrayList<>();
    List<Map> allVisits = registrationService.getPatientVisits(mrNo, visitType, false, true);
    for (Map visit : allVisits) {
      if (visit.get("status").equals("A")) {
        activeVisits.add(visit);
      } else {
        inActiveVisits.add(visit);
      }
    }

    map.put("visits_with_order", ConversionUtils.listBeanToListMap(patientVisitsWithOrder));
    map.put("active_visits", activeVisits);
    map.put("inactive_visits", inActiveVisits);
    map.put("patient_recent_allergies",
        ConversionUtils.copyListDynaBeansToMap(allergiesService.getPatientRecentAllergies(mrNo)));
    return map;
  }

  /**
   * Set parameters to default value for orderable Items or parameter getting from paramterMap.
   * <p>
   * org_id defaults to ORG0001 unless specified. multiVisitPackage defaults to N PageLimit defaults
   * to 25 unless specified. tpaId defaults to 0 unless specified. centerId defaults to -1 (which
   * means all center) Taken care for centerId as 0 too (which also means all center)
   * </p>
   * 
   * @param params
   *          the params
   * @return list of basic dyna bean
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<BasicDynaBean> getOrderableItem(InstaLinkedMultiValueMap<String, String> params) {

    List<String> ratePlanList = new ArrayList<>();
    List<String> tpaList = new ArrayList<>();
    List<String> planList = new ArrayList<>();
    if (null != params.get(ORG_ID) && !params.get(ORG_ID).isEmpty()
        && !params.get(ORG_ID).get(0).equals("")) {
      ratePlanList = params.get(ORG_ID);
    } else {
      ratePlanList.add("ORG0001");
    }

    if (null != params.get("tpa_id") && !params.get("tpa_id").isEmpty()
        && !params.get("tpa_id").get(0).equals("")) {
      tpaList = params.get("tpa_id");
    } else {
      tpaList.add("0");
    }
    if (null != params.get("plan_id") && !params.get("plan_id").isEmpty()
        && !params.get("plan_id").get(0).equals("")) {
      planList = params.get("plan_id");
    } else {
      planList.add("0");
    }
    Map<String, Object> paramsMap = new HashMap<>();

    List userId = (List) sessionService.getSessionAttributes().get("hospital_role_ids");
    List<BasicDynaBean> ordersRoleControlsList = new ArrayList();
    List itemList = new ArrayList();
    List<Integer> subGrpIdList = new ArrayList();
    String orderControlApplicability = params.getFirst("order_control_applicability");

    if (orderControlApplicability != null && orderControlApplicability.equals("true")) {
      if (userId != null && userId.size() > 0) {
        ordersRoleControlsList = hospitalRoleService.getOrderControlRules(userId);
      }
      List<Integer> grpIdList = new ArrayList();
      for (BasicDynaBean bean : ordersRoleControlsList) {
        if (bean.get("item_id").equals("*")) {
          if (!bean.get("service_sub_group_id").equals(-9)) {
            subGrpIdList.add((Integer) bean.get("service_sub_group_id"));
          } else {
            grpIdList.add((Integer) bean.get("service_group_id"));
          }
        } else {
          itemList.add(bean.get("item_id"));
        }
      }
      List<Integer> subGrps = new ArrayList();
      if (!grpIdList.isEmpty()) {
        subGrps = serviceSubGroupService.getAllServiceSubGrps(grpIdList);
        subGrpIdList.addAll(subGrps);
      }
    }
    paramsMap.put("order_control_items", itemList);
    paramsMap.put("order_controls_sub_groups", subGrpIdList);

    paramsMap.put("is_multi_visit_package",
        (String) params.getFirstOrDefault("is_multi_visit_package", "N"));
    paramsMap.put("direct_billing", (String) params.getFirst("direct_billing"));
    paramsMap.put("orderable", (String) params.getFirst("orderable"));
    paramsMap.put("operation_applicable", (String) params.getFirst("operation_applicable"));
    paramsMap.put("module_id", (String) params.getFirst("module_id"));
    paramsMap.put("tpa_id", tpaList);
    paramsMap.put("plan_id", planList);
    paramsMap.put("dept_id", (String) params.getFirstOrDefault("dept_id", "*"));
    paramsMap.put("package_applicable", (String) params.getFirst("package_applicable"));
    paramsMap.put("visit_type", (String) params.getFirst("visit_type"));
    paramsMap.put("page_limit",
        Integer.parseInt((String) params.getFirstOrDefault("page_limit", "25")));
    paramsMap.put("search_id", (String) params.getFirst("search_id"));
    paramsMap.put("gender_applicability", params.getFirstOrDefault("gender_applicability", null));
    paramsMap.put("service_group_id", (String) params.getFirst("service_group_id"));
    paramsMap.put("date", (String) params.getFirstOrDefault("date", ""));
    paramsMap.put("doctor_schedulable", (String) params.getFirst("doctor_schedulable"));
    paramsMap.put("orderset_schedulable", (String) params.getFirst("orderset_schedulable"));
    paramsMap.put("department_doctor", (String) params.getFirst("department_doctor"));

    if (!StringUtils.isEmpty((String)params.getFirst("age_text"))) {
      paramsMap.put("age_text", "P" + params.getFirst("age_text"));
    }
    paramsMap.put("center_id", (String) params.getFirst("center_id") != null
        ? Integer.parseInt((String) params.getFirst("center_id")) : null);
    if (paramsMap.get("service_group_id") != null
        && !paramsMap.get("service_group_id").equals("")) {
      paramsMap.put("service_group_id",
          Integer.parseInt((String) paramsMap.get("service_group_id")));
    }
    paramsMap.put("service_sub_group_id", (String) params.getFirst("service_sub_group_id"));
    if (paramsMap.get("service_sub_group_id") != null
        && !paramsMap.get("service_sub_group_id").equals("")) {
      paramsMap.put("service_sub_group_id",
          Integer.parseInt((String) paramsMap.get("service_sub_group_id")));
    }
    paramsMap.put(INSURANCE_CATEGORY_ID, (String) params.getFirst(INSURANCE_CATEGORY_ID));
    if (paramsMap.get(INSURANCE_CATEGORY_ID) != null
        && !paramsMap.get(INSURANCE_CATEGORY_ID).equals("")) {
      paramsMap.put(INSURANCE_CATEGORY_ID,
          Integer.parseInt((String) paramsMap.get(INSURANCE_CATEGORY_ID)));
    }
    String filter = (String) params.getFirst("filter");
    String[] filterList = null;
    if (filter != null && !filter.equals("")) {
      filterList = filter.split(",");
    }
    if (paramsMap.get("center_id") == null) {
      BasicDynaBean genericPrefs = genericPreferencesService.getPreferences();
      if (Boolean.valueOf(params.getFirstOrDefault("any_center", "false"))) {
        paramsMap.put("center_id", null);
      } else if ((Integer) genericPrefs.get("max_centers_inc_default") > 1) {
        paramsMap.put("center_id", (Integer) sessionService.getSessionAttributes().get("centerId"));
      }
    }

    paramsMap.put("item_id", (String) params.getFirst("item_id"));
    paramsMap.put("schedule", params.getFirstOrDefault("schedule", null));
    paramsMap.put("doctor_package_applicability",
        params.getFirstOrDefault("doctor_package_applicability", null));
    paramsMap.put("service_schedulable", params.getOrDefault("service_schedulable",
        Arrays.asList("A", "S")));

    if (userService != null && userService.getLoggedUser() != null) {
      BasicDynaBean loggedInUser = userService.getLoggedUser();
      String doctor = (String) loggedInUser.get("doctor_id");
      String loginControlsApplicable = (String) loggedInUser.get("login_controls_applicable");
      if (doctor != null && !doctor.isEmpty()) {
        paramsMap.put("doctor", new String(doctor));
        paramsMap.put("user_is_doctor", new String("Y"));
      }
      if (loginControlsApplicable.equalsIgnoreCase("Y")) {
        paramsMap.put("login_controls_applicable", new String("Y"));
      }
    }

    List<BasicDynaBean> orderableItems = orderRepository.getOrderableItems(paramsMap, filterList,
        ratePlanList);
    return getItemDetailsOrderableItems(orderableItems, paramsMap);
  }

  /**
   * Calculates the item charges for the item. If there is no orgId and billNo, then we decide what
   * it will be.
   * <p>
   * If orgId is null or empty and bill is there, then pick orgId from bill.
   * 
   * If bill is null or empty, then select orgId as GENERAL rate plan.
   * </p>
   * 
   * @param params
   *          the params
   * @return map
   * @throws ParseException
   *           the ParseException
   */
  @SuppressWarnings({ "unchecked" })
  public Map<String, Object> getItemCharges(Map<String, Object> params) throws ParseException {

    Map<String, Object> paramsMap = new HashMap<>();
    String patientId = (String) params.get("patient_id");
    String orgId = (String) params.get(ORG_ID);
    String billNo = (String) params.get(BILL_NO);
    BasicDynaBean patBean = registrationService.findByKey(patientId);
    Map<String, Object> billRatePlanMap = new HashMap<>();
    if (null != patientId && !patientId.equals("") && (null == orgId || orgId.equals(""))
        && (null == billNo || billNo.equals(""))) {
      billRatePlanMap = orderBillSelection.billSelectionRatePlan(params);
      if (null != billRatePlanMap) {
        orgId = (String) billRatePlanMap.get(ORG_ID);
        billNo = (String) billRatePlanMap.get(BILL_NO);

        Integer roleId = (Integer) sessionService.getSessionAttributes().get("roleId");
        boolean newBillActionRight = false;
        if (roleId == 1 || roleId == 2) {
          newBillActionRight = true;
        } else {
          Map<String, String> actionRightsMap = (Map<String, String>) securityService
              .getSecurityAttributes().get("actionRightsMap");
          newBillActionRight = "A".equals(actionRightsMap.get("new_bill_for_order_screen"));
        }
        String prescribedDate = (String) params.get("prescribed_date_date");
        String prescribedTime = (String) params.get("prescribed_date_time");
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        // The bill creation time is in format dd-MM-yyyy HH:mm:ss
        // While order time is in dd-MM-yyyy HH:mm Hence adding :59 to order time
        Date orderDateTime = formatter.parse(prescribedDate + " " + prescribedTime + ":59");
        Long backTimeAllowed = (long) (1800 * 1000); // 30mins
        Date clientTime = new Date((Long) params.get("serverSeconds") - backTimeAllowed);

        String admissionDate = DateUtil.formatDate((java.util.Date) patBean.get("reg_date"));
        String admissionTime = (String) patBean.get("reg_time").toString();
        Date admissionDateTime = formatter.parse(admissionDate + " " + admissionTime);

        if (billNo.equals("new") || billNo.equals("newInsurance")) {
          Map exisistingBillDetails = (Map) billRatePlanMap.get("exsisting_bill_details");
          if (!newBillActionRight && exisistingBillDetails.isEmpty()) {
            throw new ValidationException("exception.not.authorized.create.bill");
          } else if (!newBillActionRight && !exisistingBillDetails.isEmpty()) {
            billNo = (String) exisistingBillDetails.get("bill_no");
            billRatePlanMap.put("bill_no", billNo);
            billRatePlanMap.put("is_tpa", exisistingBillDetails.get("is_tpa"));
            billRatePlanMap.put("org_id", exisistingBillDetails.get("org_id"));
            String type = (String) params.get("type");
            boolean isMultiVisitPackage = (boolean) params.get("multi_visit_package");
            if ((boolean)exisistingBillDetails.get("is_tpa")
                && (type.equals("Package") || isMultiVisitPackage)) {
              String itemId = (String) params.get("id");
              String packageId = (String) params.get("package_id");
              Map keys = new HashMap();
              keys.put("patient_id", patientId);
              keys.put("priority", 1);
              BasicDynaBean patInsBean = patientInsurancePlansService.findByKeys(keys);
              if (patInsBean != null) {
                String tpaId = (String) patInsBean.get("sponsor_id");
                Integer planId = (Integer) patInsBean.get("plan_id");
                boolean packApplicability = getPackageApplicability(
                    !(isMultiVisitPackage) ? Integer.parseInt(itemId) :
                      Integer.parseInt(packageId), tpaId, planId);

                if (!packApplicability) {
                  ValidationErrorMap errMap = new ValidationErrorMap();
                  errMap.addError(billNo, "exception.not.authorized.create.bill");
                  throw new ValidationException(errMap);
                }
              }
            }
          }
          if (orderDateTime.before(admissionDateTime)) {
            throw new ValidationException("exception.backdated.bill.creation.denied");
          }
        }
      }
    } else if ((null != billNo && !billNo.equals(""))) {
      if (null == orgId || orgId.equals("")) {
        orgId = "ORG0001";
      }
      List<Integer> planIdsStrList = (List<Integer>) params.get("plan_ids");
      boolean isInsurance = null != planIdsStrList && !planIdsStrList.isEmpty();
      Integer planId = isInsurance ? planIdsStrList.get(0) : 0;
      String tpaId = isInsurance ? (String) params.get("tpa_id") : "0";
      String type = (String) params.get("type");
      boolean isMultiVisitPackage = (boolean) params.get("multi_visit_package");
      boolean isTpa;
      String packageId = params.get("package_id") != null
          && params.get("package_id") instanceof Integer
          ? String.valueOf(params.get("package_id"))
              : (String) params.get("package_id");
      if (null == packageId && isMultiVisitPackage && null != params.get("mvp_package_content")) {
        Map<String, Object> contentMap = (Map<String, Object>) params.get("mvp_package_content");
        packageId = contentMap.get("package_id") != null
            && contentMap.get("package_id") instanceof Integer
                ? String.valueOf(contentMap.get("package_id"))
                : (String) contentMap.get("package_id");
      }
      if (type.equals("Package") || isMultiVisitPackage) {
        if (billNo.equals("new") || billNo.equals("newInsurance")) {
          isTpa = billNo.equals("new") ? false : billNo.equals("newInsurance") ? true : false;
        } else {
          BasicDynaBean billBean = billService.findByKey(BILL_NO, billNo);
          isTpa = (Boolean) billBean.get("is_tpa");
        }
        String itemId = params.get("id") instanceof Integer
            ? String.valueOf((int) params.get("id")) : (String) params.get("id");
        boolean packApplicability = getPackageApplicability(
            !(isMultiVisitPackage) ? Integer.parseInt(itemId) : Integer.parseInt(packageId), tpaId,
            planId);
        isInsurance = (isInsurance && packApplicability && isTpa) ? true : false;
        if (!isInsurance && (billNo.equals("newInsurance") || isTpa)) {
          ValidationErrorMap errMap = new ValidationErrorMap();
          errMap.addError(billNo, "exception.package.not.applicable.for.insurance.bill");
          throw new ValidationException(errMap);
        }
      }
      billRatePlanMap.put(BILL_NO, billNo);
      billRatePlanMap.put(ORG_ID, orgId);
      if (billNo.equals("new")) {
        billRatePlanMap.put("is_tpa", false);
      } else if (billNo.equals("newInsurance")) {
        billRatePlanMap.put("is_tpa", true);
      } else {
        BasicDynaBean billBean = billService.findByKey(BILL_NO, billNo);
        billRatePlanMap.put("is_tpa", (Boolean) billBean.get("is_tpa"));
      }

    }

    if ((billNo == null || billNo.equals("")) && (null == orgId || orgId.equals(""))) {
      orgId = "ORG0001";
    }
    paramsMap.put("patient_id", patientId);
    paramsMap.put("type", (String) params.get("type"));
    paramsMap.put("charge_id", (String) params.get("charge_id"));
    String idVal = params.get("id") instanceof Integer
        ? String.valueOf((int) params.get("id")) : (String) params.get("id");
    paramsMap.put("id", idVal);
    if (params.get(CHARGE_TYPE) != null) {
      paramsMap.put(CHARGE_TYPE, params.get(CHARGE_TYPE).toString());
    }
    String visitType = (String) params.get("visit_type");
    paramsMap.put("visit_type", visitType);
    paramsMap.put("finalized", (String) params.get("finalized"));
    paramsMap.put("ot", (String) params.get("ot"));
    paramsMap.put("center_id", getParamDefault(params, "center_id", 0));
    paramsMap.put("surgeon", (String) params.get("surgeon"));
    paramsMap.put("anaesthetist", (String) params.get("anaesthetist"));
    paramsMap.put(OPERATION_ID, (String) params.get(OPERATION_ID));
    paramsMap.put("pack_ob_id", (Integer) params.get("pack_ob_id"));
    if (params.get("package_id") != null
            && !StringUtils.isEmpty(params.get("package_id").toString())) {
      paramsMap.put("package_id", Integer.parseInt(params.get("package_id").toString()));
    }
    paramsMap.put(ORG_ID, orgId);
    paramsMap.put("bed_type", (String) getParamDefault(params, "bed_type", "GENERAL"));
    paramsMap.put("units", (String) getParamDefault(params, "units", "D"));
    paramsMap.put("anesthesia_type", params.get("anesthesia_type"));
    paramsMap.put("anesthesia_type_from_date", params.get("anesthesia_type_from_date"));
    paramsMap.put("anesthesia_type_to_date", params.get("anesthesia_type_to_date"));
    paramsMap.put("anesthesia_type_from_time", params.get("anesthesia_type_from_time"));
    paramsMap.put("anesthesia_type_to_time", params.get("anesthesia_type_to_time"));
    paramsMap.put("surgeon_ot_doctor_id", params.get("surgeon_ot_doctor_id"));
    paramsMap.put("surgeon_ot_doctor_type", params.get("surgeon_ot_doctor_type"));

    if (params.get("item_excluded_from_doctor") != null) {
      if (params.get("item_excluded_from_doctor").equals("Y")) {
        paramsMap.put("item_excluded_from_doctor", true);
        paramsMap.put("item_excluded_from_doctor_remarks",
            params.get("item_excluded_from_doctor_remarks"));

      } else if (params.get("item_excluded_from_doctor").equals("N")) {

        paramsMap.put("item_excluded_from_doctor", false);
        paramsMap.put("item_excluded_from_doctor_remarks",
            params.get("item_excluded_from_doctor_remarks"));
      }
    }
    paramsMap.put("is_operation", getParamDefault(params, "is_operation", false));
    paramsMap.put("quantity", new BigDecimal(getParamDefault(params, "quantity", "1").toString()));

    paramsMap.put(BILL_NO, billNo);
    paramsMap.put("first_of_category", getParamDefault(params, "first_of_category", false));
    paramsMap.put("multi_visit_package", getParamDefault(params, "multi_visit_package", false));
    paramsMap.put("preauth_act_id", params.get("preauth_act_id"));

    String toDate = (String) params.get("to_date");
    DateUtil dateUtil = new DateUtil();
    if (toDate != null && !toDate.isEmpty()) {
      paramsMap.put("to_date", dateUtil.parseTheTimestamp(toDate));
    }

    String fromDate = (String) params.get("from_date");
    if (fromDate != null && !fromDate.isEmpty()) {
      paramsMap.put("from_date", dateUtil.parseTheTimestamp(fromDate));
    }

    List<Integer> planIdsStrList = (List<Integer>) params.get("plan_ids");
    int[] planIds = null;
    if (planIdsStrList != null && !planIdsStrList.isEmpty()) {
      planIds = new int[planIdsStrList.size()];
      int planIdx = 0;
      for (int i = 0; i < planIdsStrList.size(); i++) {
        if ((planIdsStrList.get(i) != null)) {
          planIds[planIdx++] = planIdsStrList.get(i);
        }
      }
    }
    paramsMap.put("planIds", planIds);

    boolean isInsurance = null != planIds && planIds.length > 0;
    if (!isInsurance) {
      isInsurance = (Boolean) getParamDefault(params, "insurance", false);
    }
    paramsMap.put("is_insurance", isInsurance);

    // use "new" for non insurance bill
    boolean isNonInsuBill = billNo != null && !billNo.equals("newInsurance");
    paramsMap.put("is_non_insu_bill", isNonInsuBill);
    if (params.get("package_contents") != null) {
      paramsMap.put("package_contents", params.get("package_contents"));
    }

    if (params.get("mvp_package_content") != null) {
      paramsMap.put("mvp_package_content", params.get("mvp_package_content"));
    }

    Map<String, Object> packDetails =
        (Map<String, Object>) params.get("package_details");
    String submissionType = (String) params.get("submission_batch_type");
    paramsMap.put("submission_batch_type", submissionType);
    paramsMap.put("code_type", params.get("code_type"));
    paramsMap.put("act_rate_plan_item_code", params.get("ct_code"));
    boolean isCustomizedPackage = params.get("is_customized") != null
        ? (Boolean) params.get("is_customized") : false ;
    paramsMap.put("is_customized", isCustomizedPackage);
    List<BasicDynaBean> chargesList = getCharges(paramsMap);
    String nationalityId = (String) params.get("nationality_id");
    String sponsorId = (String) params.get("sponsor_id");
    String insuranceDiscountPlan = (String) params.get("insurance_discount_plan");
    processChargesList(chargesList, billNo, visitType, patientId, planIds, insuranceDiscountPlan,
        nationalityId, sponsorId);
    Map<String, Object> itemChargesMap = new HashMap<>();
    if ("Package".equals(paramsMap.get("type"))
        && !((Boolean)getParamDefault(paramsMap, "multi_visit_package", false))) {
      List<BasicDynaBean> newChargesList = new ArrayList<>();
      List<BasicDynaBean> mainChargesList = new ArrayList<>();
      BigDecimal totalCharge = BigDecimal.ZERO;
      BigDecimal totalDiscount = BigDecimal.ZERO;
      BigDecimal totalTax = BigDecimal.ZERO;
      BasicDynaBean newMainCharge = null;
      for (BasicDynaBean charge: chargesList) {
        if ("PKGPKG".equals(charge.get("charge_head"))) {
          newMainCharge = charge;
          mainChargesList.add(newMainCharge);
        } else {
          totalCharge = totalCharge.add((BigDecimal)charge.get("amount"));
          totalDiscount = totalDiscount.add((BigDecimal)charge.get("discount"));
          totalTax = totalTax.add((BigDecimal)charge.get("tax_amt"));
          newChargesList.add(charge);
        }
      }
      if (newMainCharge != null) {
        chargesList = mainChargesList;
        itemChargesMap.put("package_contents",
            ConversionUtils.listBeanToListMap(newChargesList));
      }

      newMainCharge.set("act_rate", totalCharge);
      newMainCharge.set("orig_rate", totalCharge);
      newMainCharge.set("tax_amt", totalTax);
      newMainCharge.set("discount", totalDiscount);
      if (totalDiscount.compareTo(BigDecimal.ZERO) != 0) {
        newMainCharge.set("overall_discount_amt", totalDiscount);
      }
      newMainCharge.set("amount", totalCharge);
    }
    itemChargesMap.put("charge_list", chargesList);
    itemChargesMap.put("selected_bill", billRatePlanMap);
    return itemChargesMap;
  }

  /**
   * Returns the list of charges based on orderableItems types. It calls the corresponding items
   * getCharges function in each orderItemService.
   * 
   * @param paramsMap
   *          the paramsMap
   * @return list of basic dyna bean
   * @throws ParseException
   *           the ParseException
   */
  private List<BasicDynaBean> getCharges(Map<String, Object> paramsMap) throws ParseException {
    String type = (String) paramsMap.get("type");

    Boolean mvp = (Boolean) paramsMap.get("multi_visit_package");
    if (mvp) {
      // As it is mvp item need to get mvp charges.
      return packageOrderItemService.getCharges(paramsMap);
    }

    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null && Arrays.asList(orderAnnotation.value()).contains(type)) {
        OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
        return orderItem.getCharges(paramsMap);
      }
    }
    return Collections.emptyList();
  }

  /**
   * Returns the map of charges for each bed type. It calls the corresponding items
   * getCharges function in each orderItemService.
   *
   * @param paramsMap the paramsMap
   * @param orderItem the object of respective class to get charge.
   * @return chargeMap the charge map with bed as the key.
   * @throws ParseException the ParseException
   * @throws SQLException   SQL Exception
   */
  public Map<String, Object> getAllBedTypeChargeMap(Map<String, Object> paramsMap,
      OrderItemService orderItem)
      throws ParseException, SQLException {

    String type = (String) paramsMap.get("type");
    //forWarding to getDoctorOrOperationChargeMap to get the Doctor & Operation
    if (type.equals("Doctor") || type.equals("Operation")) {
      return getDoctorOrOperationChargeMap(paramsMap, orderItem);
    }
    Map<String, Object> chargeMap = new HashMap<>();
    List<BasicDynaBean> charge = new ArrayList<BasicDynaBean>();
    //if type is Equipment then setting default value as zero
    if (type.equals("Equipment")) {
      return getDefaultChargeMap();
    } else if (type.equals("Bed") || type.equals("ICU")) {
      return getBedChargeChargeMap(paramsMap);
    }
    if (orderItem != null) {
      charge = orderItem.getAllBedTypeMasterChargesBean(paramsMap.get("id"),
          paramsMap.get("org_id").toString());
      chargeMap = getChargeMap(charge, (Integer) paramsMap.get("quantity"), type);
    } else {
      //inserting 0 if orderItem is null
      chargeMap = getDefaultChargeMap();
    }
    return chargeMap;
  }

  /**
   * @param chargeBean bean containing charge details.
   * @param type       activity type.
   * @return chargeMap with charge map.
   */
  private Map<String, Object> getChargeMap(List<BasicDynaBean> chargeBean, Integer quantity,
      String type) {
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    Map<String, Object> chargeMap = new HashMap<>();
    Map<String, Object> chargeMasterMap = new HashMap<>();
    if (chargeBean == null || chargeBean.isEmpty()) {
      return getDefaultChargeMap();
    }
    for (BasicDynaBean charge : chargeBean) {
      if (!type.equals("Other Charge")) {
        chargeMasterMap.put(charge.get("bed_type").toString(),
            ((BigDecimal) charge.get("charge")).multiply(new BigDecimal(quantity)));
      }
    }
    for (String bedType : bedNames) {
      if (type.equals("Other Charge")) {
        //as there will be only one row in the list.
        chargeMap.put(bedType,
            ((BigDecimal) chargeBean.get(0).get("charge")).multiply(new BigDecimal(quantity)));
      } else {
        if (chargeMasterMap.containsKey(bedType)) {
          chargeMap.put(bedType, chargeMasterMap.get(bedType));
        } else {
          chargeMap.put(bedType, BigDecimal.ZERO);
        }
      }
    }
    return chargeMap;
  }

  /**
   * Returns the map of charges for each bed type. It calls the corresponding items
   * getCharges function in each orderItemService.
   *
   * @param paramsMap the paramsMap
   * @return chargeMap the charge map with bed as the key.
   * @throws ParseException the ParseException
   * @throws SQLException   SQL Exception
   */
  public Map<String, Object> getBedChargeChargeMap(Map<String, Object> paramsMap)
      throws ParseException, SQLException {
    Map<String, Object> chargeMap = new HashMap<>();
    List<BasicDynaBean> icuBedCharge = new ArrayList<BasicDynaBean>();

    BasicDynaBean normalBedCharge =
        bedOrderItemService.getNormalBedChargesBean(paramsMap.get("id").toString(),
            paramsMap.get("org_id").toString());
    icuBedCharge = bedOrderItemService.getAllIcuBedChargesBean(paramsMap.get("id").toString(),
        paramsMap.get("org_id").toString());
    chargeMap = getBedChargeMap(icuBedCharge, normalBedCharge, (Integer) paramsMap.get("quantity"),
        (String) paramsMap.get("charge_head"));

    if (chargeMap == null || chargeMap.isEmpty()) {
      //inserting 0 if orderItem is null
      chargeMap = getDefaultChargeMap();
    }
    return chargeMap;
  }

  /**
   * Returns a charg map for bed related activites like Nurcing charge,Bed charge
   * Professional Charge,Duty Doctor Charge..
   *
   * @param icuBedCharge bean containing charge details of icu beds.
   * @param normalBedCharge bean containing charge details of normal beds.
   * @param quantity       number of units ordered.
   * @param chargeHead       charge head of the iteam.
   * @return chargeMap with charge map.
   */
  private Map<String, Object> getBedChargeMap(List<BasicDynaBean> icuBedCharge,
      BasicDynaBean normalBedCharge,
      Integer quantity, String chargeHead) {
    Map<String, Object> chargeMap = new HashMap<>();
    Map<String, Object> chargeMasterMap = new HashMap<>();
    if (icuBedCharge != null) {
      for (BasicDynaBean charge : icuBedCharge) {
        if (chargeHead.equals("BICU")) {
          chargeMasterMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("bed_charge")).multiply(new BigDecimal(quantity)));
        } else if (chargeHead.equals("NCICU")) {
          chargeMasterMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("nursing_charge")).multiply(new BigDecimal(quantity)));
        } else if (chargeHead.equals("PCICU")) {
          chargeMasterMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("maintainance_charge")).multiply(new BigDecimal(quantity)));
        } else if (chargeHead.equals("DDICU")) {
          chargeMasterMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("duty_charge")).multiply(new BigDecimal(quantity)));
        } else {
          chargeMasterMap.put(charge.get("bed_type").toString(), BigDecimal.ZERO);
        }
      }
    }
    BigDecimal normalCharge = BigDecimal.ZERO;
    if (icuBedCharge != null) {
      if (chargeHead.equals("BBED")) {
        normalCharge =
            ((BigDecimal) normalBedCharge.get("bed_charge")).multiply(new BigDecimal(quantity));
      } else if (chargeHead.equals("NCBED")) {
        normalCharge =
            ((BigDecimal) normalBedCharge.get("nursing_charge")).multiply(new BigDecimal(quantity));
      } else if (chargeHead.equals("PCBED")) {
        normalCharge =
            ((BigDecimal) normalBedCharge.get("maintainance_charge"))
                .multiply(new BigDecimal(quantity));
      } else if (chargeHead.equals("DDBED")) {
        normalCharge =
            ((BigDecimal) normalBedCharge.get("duty_charge")).multiply(new BigDecimal(quantity));
      }
    }
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    for (String bedType : bedNames) {
      if (chargeMasterMap.containsKey(bedType)) {
        chargeMap.put(bedType, chargeMasterMap.get(bedType));
      } else {
        chargeMap.put(bedType, normalCharge);
      }
    }
    return chargeMap;
  }

  /**
   * @return chargeMap with zero values.
   */
  private Map<String, Object> getDefaultChargeMap() {
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    Map<String, Object> chargeMap = new HashMap<>();
    for (String bedType : bedNames) {
      chargeMap.put(bedType, BigDecimal.ZERO);
    }
    return chargeMap;
  }

  /**
   * Returns the map of charges for each bed type for Doctor & Operation.
   * It calls the corresponding items
   * getCharges function in each orderItemService.
   *
   * @param paramsMap parameters Map
   * @return chargeMap the charge map with bed as the key.
   */
  private Map<String, Object> getDoctorOrOperationChargeMap(Map<String, Object> paramsMap,
      OrderItemService orderItem)
      throws SQLException {
    Map<String, Object> chargeMap = new HashMap<String, Object>();
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    String type = (String) paramsMap.get("type");
    List<BasicDynaBean> charge = new ArrayList<BasicDynaBean>();
    if (orderItem != null) {
      charge = orderItem.getAllBedTypeMasterChargesBean(paramsMap.get("id"),
          paramsMap.get("org_id").toString());
      chargeMap = getDocterOrOperationCharge(charge, paramsMap);
    }
    if (chargeMap.isEmpty() || chargeMap == null) {
      return getDefaultChargeMap();
    }
    return chargeMap;
  }

  /**
   * Returns the map of objects where Activity type as
   * the key.
   *
   * @return objectMap the object map with Activity type as the key.
   * @author EashwarChandraVidyaSagarg
   */
  public Map<String, Object> getTypeObjectMap() {
    Map<String, Object> objectMap = new HashMap<String, Object>();
    String type = null;
    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      type = Arrays.asList(orderAnnotation.value()).get(0);
      OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
      objectMap.put(type, orderItem);
    }
    return objectMap;
  }

  /**
   * @param chargeBean bean containing charge details.
   * @param paramsMap  containing parameters.
   * @return chargeMap with charge map.
   * @throws SQLException SQL Exception
   */
  private Map<String, Object> getDocterOrOperationCharge(List<BasicDynaBean> chargeBean, Map<String,
      Object> paramsMap) throws SQLException {
    List<String> bedNames = bedTypeService.getAllBedTypeNames();
    Map<String, Object> chargeMap = new HashMap<>();
    Map<String, Object> chargeMasterMap = new HashMap<>();
    String type = (String) paramsMap.get("type");
    if (chargeBean == null || chargeBean.isEmpty()) {
      return getDefaultChargeMap();
    }
    BigDecimal quantity = new BigDecimal((Integer) paramsMap.get("quantity"));
    for (BasicDynaBean charge : chargeBean) {
      if (type.equals("Operation")) {
        String chargeHead = (String) paramsMap.get("charge_head");
        if (chargeHead.equals("SACOPE")) {
          chargeMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("surg_asstance_charge")).multiply(quantity));

        } else if (chargeHead.equals("SUOPE")) {
          chargeMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("surgeon_charge")).multiply(quantity));

        } else if (chargeHead.equals("ANAOPE")) {
          chargeMap.put(charge.get("bed_type").toString(),
              ((BigDecimal) charge.get("anesthetist_charge")).multiply(quantity));

        } else {
          chargeMap.put(charge.get("bed_type").toString(), BigDecimal.ZERO);

        }
      } else {
        String doctorChargeType =
            consultationTypesRepository
                .getDoctorChargeType((Integer) paramsMap.get("consultation_type_id"));
        chargeMap.put(charge.get("bed_type").toString(),
            ((BigDecimal) charge.get(doctorChargeType)).multiply(quantity));
      }
    }
    for (String bedType : bedNames) {
      if (chargeMap.containsKey(bedType)) {
        chargeMasterMap.put(bedType, chargeMap.get(bedType));
      } else {
        chargeMasterMap.put(bedType, BigDecimal.ZERO);
      }
    }
    return chargeMasterMap;
  }

  /**
   * Check applicability for packages for that tpa and plan.
   *
   * @param packId
   *          package Id
   * @param tpaId
   *          tpaId
   * @param planId
   *          plan Id
   *
   */

  public boolean getPackageApplicability(Integer packId, String tpaId, Integer planId) {
    return packageOrderItemService.getPackageApplicablity(packId, tpaId, planId);
  }

  /**
   * Calculating the discount rules and tax amount for the charge List. the changes are reflected in
   * chargesList Object.
   * 
   * @param chargesList
   *          the chargesList
   * @param billNo
   *          the billNo
   * @param visitType
   *          the visitType
   * @param patientId
   *          the patientId
   * @param planIds
   *          the planIds
   * @param insuranceDiscountPlan
   *          the insuranceDiscountPlan
   * @param nationalityId
   *          the nationalityId
   * @param sponsorId
   *          the sponsorId
   */
  private void processChargesList(List<BasicDynaBean> chargesList, String billNo, String visitType,
      String patientId, int[] planIds, String insuranceDiscountPlan, String nationalityId,
      String sponsorId) {

    Boolean isTpaBill = false;
    BasicDynaBean billBean = billService.findByKey(BILL_NO, billNo);
    if (null != billBean) {
      isTpaBill = (Boolean) billBean.get("is_tpa");
    } else if (billNo != null && billNo.equals("newInsurance")) {
      isTpaBill = true;
    }

    int visitDiscountPlanId = 0;
    boolean isSystemDisc = false;
    if (null != patientId && !patientId.equals("")) {
      BasicDynaBean visitPrimPlanDetails = patientInsurancePlansService
          .getVisitPrimaryPlan(patientId);
      visitDiscountPlanId = (isTpaBill && visitPrimPlanDetails != null
          && visitPrimPlanDetails.get("discount_plan_id") != null
              ? (Integer) visitPrimPlanDetails.get("discount_plan_id") : 0);
      if (null != visitPrimPlanDetails && null != visitPrimPlanDetails.get("discount_plan_id")) {
        isSystemDisc = true;
      }
    } else {
      String discPlanStr = insuranceDiscountPlan;
      visitDiscountPlanId = (null != discPlanStr && !discPlanStr.equals(""))
          ? Integer.parseInt(discPlanStr) : 0;
    }

    if (visitDiscountPlanId == 0 && billNo != null && null != billBean
        && null != billBean.get("discount_category_id")) {
      visitDiscountPlanId = (Integer) billBean.get("discount_category_id");
    }

    List<BasicDynaBean> discountPlanDetails = discountPlanService.listAllDiscountPlanDetails(null,
        "discount_plan_id", visitDiscountPlanId, "priority");
    Map<String, String> commonCharges = commonChargesRepository.getCommonChargeTypeMap();
    for (BasicDynaBean charge : chargesList) {
      if (null == charge.get("charge_head")) {
        charge.set("charge_head", commonCharges.get(charge.get("act_description")));
      }
      boolean isItemCategoryPayable = true;
      Boolean isDoctorExcluded = null;
      if (charge.get("item_excluded_from_doctor") != null) {
        isDoctorExcluded = (Boolean) charge.get("item_excluded_from_doctor");
      }
      if (visitType != null && !visitType.equals("")) {
        isItemCategoryPayable = discountPlanService.isItemCategoryPayable(
            planIds != null && planIds.length >= 1 ? planIds[0] : 0, visitType,
            (int) charge.get(INSURANCE_CATEGORY_ID), planIds != null && planIds.length >= 1);
      }

      Boolean allowDiscount = charge.get("allow_discount") != null
          ? (Boolean) charge.get("allow_discount") : false;
      if (allowDiscount) {
        BasicDynaBean discountRuleBean = discountPlanService.getDiscountRule(charge,
            discountPlanDetails);
        if (null != discountRuleBean
            && isPayableAfterDoctorExcluded(isItemCategoryPayable, isDoctorExcluded)) {
          discountPlanService.setDiscountRule(charge, discountRuleBean);
          if (isSystemDisc) {
            charge.set("is_system_discount", "Y");
          } else {
            charge.set("is_system_discount", "N");
          }
        }
      }
    }

    BasicDynaBean visitBean = null;
    BasicDynaBean patientBean = null;
    BasicDynaBean tpaBean = null;

    // billNo is null for screens other than billing and order.
    if (billNo == null) {
      patientBean = patientdetailsService.getBean();
      patientBean.set("nationality_id", nationalityId);
      billBean = billService.getBean();
      billBean.set("is_tpa", planIds != null && planIds.length > 0);
      tpaBean = tpaService.getDetails(sponsorId);
    } else {
      // bill bean is null if billNo is new or newInsurance.
      if (billBean != null) {
        visitBean = registrationService.findByKey((String) billBean.get("visit_id"));
        patientBean = patientdetailsService.findByKey((String) visitBean.get("mr_no"));
      } else {
        if (!isTpaBill) {
          billBean = billService.getBean();
          billBean.set("is_tpa", false);
          visitBean = registrationService.findByKey(patientId);
          if (visitBean != null) {
            patientBean = patientdetailsService.findByKey((String) visitBean.get("mr_no"));
          }
        }
      }
    }

    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    BasicDynaBean centerBean = hospitalCenterService.findByKey(centerId);
    for (BasicDynaBean charge : chargesList) {
      billChargeTaxService.setTaxAmounts(charge, patientBean, billBean, centerBean, visitBean,
          tpaBean);
      charge.set("amount",
          ((BigDecimal) charge.get("amount")).add((BigDecimal) charge.get(TAX_AMT)));
    }
  }

  /**
   * Add item to Map of string, List of object for specified itemType.
   * 
   * @param orderParams
   *          the orderParams
   * @param itemType
   *          the itemType
   * @param orderItem
   *          the orderItem
   */
  private void addToOrderParams(Map<String, List<Object>> orderParams, String itemType,
      BasicDynaBean orderItem) {
    if (orderParams.get(itemType) != null) {
      orderParams.get(itemType).add(orderItem.get("entity_id"));
    } else {
      List<Object> listOrderParams = new ArrayList<Object>();
      listOrderParams.add(orderItem.get("entity_id"));
      orderParams.put(itemType, listOrderParams);
    }
  }

  /**
   * Check if the Annotation contains Order as meta-annotation.
   * 
   * @param annotations
   *          the annotations
   * @return Annotation
   */
  private Annotation checkOrderAnnotionExist(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Order) {
        return annotation;
      }
    }
    return null;
  }

  /**
   * Returns the list of order items details based on orderableItems returned from orderable_item
   * search list.
   * 
   * @param orderableItems
   *          the orderableItems
   * @param paramsMap
   *          the paramsMap
   * @return list of basic dyna bean
   */
  private List<BasicDynaBean> getItemDetailsOrderableItems(List<BasicDynaBean> orderableItems,
      Map<String, Object> paramsMap) {

    List<BasicDynaBean> relevantSearchResult = RelevantSorting.rankBasedSorting(orderableItems,
        (String) paramsMap.get("search_id"), "item_name");

    int endIndex = relevantSearchResult.size() > (Integer) paramsMap.get("page_limit")
        ? (Integer) paramsMap.get("page_limit") : relevantSearchResult.size();

    List<BasicDynaBean> relevantSearchResults = new ArrayList<BasicDynaBean>(
        relevantSearchResult.subList(0, endIndex));

    Map<String, List<Object>> orderParams = new HashMap<String, List<Object>>();
    for (BasicDynaBean orderItem : relevantSearchResults) {
      String entityName = (String) orderItem.get("entity");
      if (entityName.equals("Doctor") && orderItem.get("entity_id").equals("Doctor")) {
        addToOrderParams(orderParams, "Doctor Package", orderItem);
        continue;
      }
      Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
          .iterator();
      while (orderItemsTypesIterator.hasNext()) {
        Annotation[] annotations = orderItemsTypesIterator.next().getAnnotations();
        Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
        if (orderAnnotation != null) {
          String[] entitySupported = orderAnnotation.value();
          for (String entity : entitySupported) {
            if (entity.equals(entityName)) {
              addToOrderParams(orderParams, orderAnnotation.key(), orderItem);
            }
          }
        }
      }
    }

    List<BasicDynaBean> orderItemDetails = getItemDetails(orderParams, paramsMap);
    if (orderParams.get("Doctor Package") != null) {
      orderItemDetails.add(doctorOrderService.getDoctorPackageItemDetail());
    }
    if ("true".equals((String) paramsMap.get("department_doctor"))) {
      return RelevantSorting.rankBasedSorting(orderItemDetails, (String) paramsMap.get("search_id"),
          "department");
    }

    return RelevantSorting.rankBasedSorting(orderItemDetails, (String) paramsMap.get("search_id"),
        "name");
  }

  /**
   * Returns the list of order item details based on entity and id passed Bed is both normal bed and
   * ICU bed Diagnostics is both Lab and Radiology Package is Used for
   * OP/IP/Diag(Lab,Radiology)/MultiVisit Packages.
   * 
   * @param orderParams
   *          the orderParams
   * @param paramsMap
   *          the paramsMap
   * @return list of basic dyna bean
   */
  private List<BasicDynaBean> getItemDetails(Map<String, List<Object>> orderParams,
      Map<String, Object> paramsMap) {

    List<BasicDynaBean> orderItemDetails = new ArrayList<BasicDynaBean>();

    for (Map.Entry<String, List<Object>> entry : orderParams.entrySet()) {
      String key = entry.getKey();
      List<Object> entityIdList = entry.getValue();
      Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
          .iterator();
      while (orderItemsTypesIterator.hasNext()) {
        Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
        Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
        Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
        if (orderAnnotation != null && orderAnnotation.key().equals(key)) {
          OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
          orderItemDetails.addAll(orderItem.getItemDetails(entityIdList, paramsMap));
        }
      }
    }
    return orderItemDetails;
  }


  /**
   * Gets item details.
   *
   * @param type   the activity type
   * @param itemId the item id or activity id
   * @param orgId  the org id
   * @return the item details
   */
  public Map<String, Object> getItemDetails(String type, String itemId, String orgId,
      String bedType, String chargeHead, Integer packageContentId, Integer panelId) {
    if (StringUtils.isEmpty(bedType)) {
      bedType = "GENERAL";
    }

    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();

    Map<String, Object> itemDetails = new HashMap<>();

    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null && Arrays.asList(orderAnnotation.value()).contains(type)
          && !type.equals("Direct Charge")) {
        OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
        String activityCharge = getCustomizedItemCharge(orderItem, packageContentId, bedType, orgId,
            itemId, chargeHead, type);
        if (null != panelId) {
          PackageContentCharges packageContentCharges = packageContentChargesService
              .findPackageContentActivityChargesByPackageContentId(
                  packageContentId, bedType, orgId);
          itemDetails.put("activity_charge", packageContentCharges.getCharge());
        } else {
          itemDetails.put("activity_charge", activityCharge);
        }

        itemDetails.putAll(orderItem.getCodeDetails(itemId, orgId));

      }
    }

    if ("Operation".equals(type)) {
      itemDetails.put("operation_id", itemId);
    }

    return itemDetails;
  }

  private String getOperationChargeFromChargeHead(String chargeHead, BasicDynaBean bedCharge) {
    if ("SUOPE".equals(chargeHead)) {
      return String.valueOf(bedCharge.get("surgeon_charge"));
    }
    if ("SACOPE".equals(chargeHead)) {
      return String.valueOf(bedCharge.get("surg_asstance_charge"));
    }
    return String.valueOf(bedCharge.get("surgeon_charge"));
  }

  /**
   * Returns the list one time basic information required for ordering an order.
   *
   * @param visitType
   *          the visitType
   * @return map
   */
  public Map<String, Object> getBasicOrderInfo(String visitType) {

    Map<String, Object> sessionDetails = sessionService.getSessionAttributes();
    Map<String, Object> basicInfo = new HashMap<>();

    basicInfo.put("service_groups",
        ConversionUtils.listBeanToListMap(serviceGroupService.lookup(true)));
    basicInfo.put("service_sub_groups",
        ConversionUtils.listBeanToListMap(serviceSubGroupService.listOrderActiveRecord()));
    basicInfo.put("anesthesia_types", ConversionUtils
        .listBeanToListMap(anesthesiaTypeService.listAll(null, "status", "A", null)));
    basicInfo.put("all_doctor_consultation_types", ConversionUtils
        .listBeanToListMap(consultationTypesService.getConsultationTypes(visitType)));
    basicInfo.put("ot_doctor_charge_heads",
        ConversionUtils.listBeanToListMap(chargeHeadsService.getOtDoctorChargeHeads()));
    // TODO: Refactor ot_doctor_charge_heads to be contained in charge_heads itself
    basicInfo.put("charge_heads",
        ConversionUtils.listBeanToListMap(chargeHeadsService.lookup(false)));
    basicInfo.put("prior_auth_mode", ConversionUtils.listBeanToListMap(priorAuthService.listAll()));
    basicInfo.put("center_preferences",
        centerPrefService.getCenterPreferences((Integer) sessionDetails.get("centerId")).getMap());
    basicInfo.put("server_time", new Date());

    return basicInfo;
  }

  /**
   * Returns list of all ordered items for a specific visit id. as well as prescriptions items.
   * 
   * @param visitId
   *          the visitId
   * @param flowType
   *          the flowType
   * @param itemTypes
   *          the itemTypes
   * @return map
   */
  public Map<String, Object> getOrders(String visitId, String flowType, List<String> itemTypes) {
    List<Map> orderedItems = new ArrayList<>();
    List<Integer> operationOrderIds = new ArrayList<Integer>();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("visit_id", visitId);

    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    while (orderItemsTypesIterator.hasNext()) {

      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null) {
        boolean itemTypeExists = false;
        if (itemTypes != null) {
          String[] annotationValues = orderAnnotation.value();
          for (String annotationValue : annotationValues) {
            if (itemTypes.contains(annotationValue)) {
              itemTypeExists = true;
              break;
            }
          }
        } else {
          itemTypeExists = true;
        }
        if (itemTypeExists) {
          OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);

          List<Map> orderedItem = new ArrayList<>();
          
          for (Map item: (List<Map>)ConversionUtils.listBeanToListMap(
              orderItem.getOrderedItems(parameters))) {
            Map<String, Object> orderItemMap = new HashMap<>(item);
            if (orderItemMap.get("type").equals("Package")) {
              orderItemMap.put("packageComponents", 
                  ConversionUtils.listBeanToListMap(getPackageComponentDetails(
                      Integer.parseInt((String) item.get("item_id")))));
            }
            
            orderedItem.add(orderItemMap);
          }

          if (orderedItem != null) {
            orderedItems.addAll(orderedItem);
          }
        }
      }
    }

    orderedItems.addAll(ConversionUtils.listBeanToListMap(
        packageOrderItemService.getMultivisitPackageItems(visitId)));
    
    List<Map> cancelledOrders = new ArrayList<>();
    List<Map> allOrders = new ArrayList<>();
    // Get operation Ids from all items.
    for (Map orderedItem : orderedItems) {
      if (orderedItem.get("type") != null && orderedItem.get("type").equals("Operation")) {
        operationOrderIds.add((Integer) orderedItem.get("order_id"));
      }
      if ("X".equals(orderedItem.get("status"))) {
        cancelledOrders.add(orderedItem);
      } else {
        allOrders.add(orderedItem);
      }
    }

    Map<String, Object> orderedItemsMap = new HashMap<String, Object>();
    orderedItemsMap.put("allOrders", allOrders);
    orderedItemsMap.put("cancelledOrders", cancelledOrders);
    orderedItemsMap.putAll(getOperationDetails(operationOrderIds, visitId));
    orderedItemsMap.put("dietPrescriptions",
        ConversionUtils.listBeanToListMap(dietPrescService.getPrescriptions(visitId)));
    if (flowType.equalsIgnoreCase("opFlow") || flowType.equals("o")) {
      String mrNo = (String) registrationService.findByKey(visitId).get("mr_no");
      Map<String, Object> previousPrescriptionsMap = new HashMap<>();
      previousPrescriptionsMap.putAll(registrationService.getPreviousPrescriptions(mrNo));
      Map<String, Map<String, Object>> listVisitPreAuthPrescriptions = new HashMap<>();
      listVisitPreAuthPrescriptions = registrationService.getVisitWisePriorauthItems(visitId);
      previousPrescriptionsMap.putAll(listVisitPreAuthPrescriptions);
      orderedItemsMap.put("previous_prescriptions",
          previousPrescriptionsMap);
    } else {
      orderedItemsMap.put("previous_prescriptions", Collections.EMPTY_MAP);
    }
    Map<PreAuthItemType, List<BasicDynaBean>> activePreAuthApprovedItems = preAuthItemsService
        .getAllActivePreAuthApprovedItems(visitId);
    for (Map.Entry<PreAuthItemType, List<BasicDynaBean>> itemTypeBeanMap : 
        activePreAuthApprovedItems.entrySet()) {
      orderedItemsMap.put(itemTypeBeanMap.getKey().getResponseMapKey(),
          ConversionUtils.listBeanToListMap(itemTypeBeanMap.getValue()));
    }
    return orderedItemsMap;
  }

  private List<BasicDynaBean> getMultiVisitPackageBeans(String mrNo) {
    return packageOrderItemService.getMultiVisitPackageBeans(mrNo);
  }

  /**
   * Returns the package Components given a package Id.
   * 
   * @param packageId
   *          the packageId
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getPackageComponentDetails(Integer packageId) {
    return getPackageComponentDetails(packageId, "ORG0001", "GENERAL");
  }

  /**
   * Returns the package Components given a package Id.
   *
   * @param packageId the packageId
   * @param orgId the orgId
   * @param bedType the bedType
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getPackageComponentDetails(Integer packageId, String orgId,
      String bedType) {
    if (null == orgId || "".equals(orgId)) {
      orgId = "ORG0001";
    }
    if (null == bedType || "".equals(bedType)) {
      bedType = "GENERAL";
    }
    List<BasicDynaBean> packageComponentDetails = packagesService
        .getPackageComponents(packageId, orgId, bedType);

    //for Cash Bill of Insurance Patient
    if (packageComponentDetails == null || packageComponentDetails.isEmpty()) {
      Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
      BasicDynaBean centerPreferences = centerPrefService.getCenterPreferences(centerId);
      String nonInsuranceRatePlan = (String) (centerPreferences
          .get("pref_rate_plan_for_non_insured_bill") != null
              ? centerPreferences.get("pref_rate_plan_for_non_insured_bill")
              : "ORG0001");
      packageComponentDetails = packagesService
          .getPackageComponents(packageId, nonInsuranceRatePlan, bedType);
    }

    return packageComponentDetails;
  }

  /**
   * Returns the package details.
   *
   * @param packageId the packageId
   * @param orgId the orgId
   * @param bedType the bedType
   * @return list of basic dyna bean
   */
  public BasicDynaBean getPackageDetails(Integer packageId, String orgId,
                                                        String bedType) {
    if (null == orgId || "".equals(orgId)) {
      orgId = "ORG0001";
    }
    if (null == bedType || "".equals(bedType)) {
      bedType = "GENERAL";
    }
    BasicDynaBean packageDetails = packagesService.getPackageDetails(packageId, orgId, bedType);

    //for Cash Bill of Insurance Patient
    if (packageDetails == null) {
      Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
      String nonInsuranceRatePlan = (String) (centerPrefService.getCenterPreferences(centerId))
          .get("pref_rate_plan_for_non_insured_bill") != null
          ? (String) (centerPrefService.getCenterPreferences(centerId))
          .get("pref_rate_plan_for_non_insured_bill") : "ORG0001";
      packageDetails = packagesService.getPackageDetails(packageId, nonInsuranceRatePlan, bedType);
    }
    return packageDetails;
  }

  /**
   * Gets the components across the given packages.
   *
   * @param mrNo the mrNo
   * @return list of package components and patient packages
   */
  public Map<String, List<BasicDynaBean>> getPatientPackageDetails(String mrNo) {
    Map<String, List<BasicDynaBean>> orderedPackageDetails = new HashMap<>();
    List<BasicDynaBean> mvpBeans = this.getMultiVisitPackageBeans(mrNo);
    Iterator mvpBeanIterator = this.getMultiVisitPackageBeans(mrNo).iterator();
    List<Integer> packageIdList = new ArrayList<>();
    while (mvpBeanIterator.hasNext()) {
      packageIdList.add((Integer) ((BasicDynaBean) mvpBeanIterator.next()).get("package_id"));
    }
    List<BasicDynaBean> packageComponentBeans =
            packagesService.getPatientPackagesComponents(mrNo, packageIdList);
    orderedPackageDetails.put("packageComponents", packageComponentBeans);
    orderedPackageDetails.put("patientPackages", mvpBeans);
    return orderedPackageDetails;
  }

  /**
   * get package component details for bulk appointments.
   * 
   * @param packageId
   *          the packageId
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getPackageComponentDetailsForBulkAppts(Integer packageId) {
    List<BasicDynaBean> list = packagesService.getPackageComponents(packageId);
    checkIfValidPackageForBulkAppointments(list);
    return list;
  }

  /**
   * check if valid package for bulk appointments.
   * 
   * @param list
   *          the list
   */
  public void checkIfValidPackageForBulkAppointments(List<BasicDynaBean> list) {
    int countDoc = 0;
    int countSer = 0;
    int counter = 0;
    for (BasicDynaBean bean : list) {
      String itemType = (String) bean.get("item_type");
      if (counter > 0) {
        String dependency = (String) bean.get("parent_pack_ob_id");
        if (dependency == null || dependency.equals("")) {

          ValidationErrorMap errorMap = new ValidationErrorMap();
          errorMap.addError("packageId", "exception.scheduler.invalid.orderset");
          ValidationException ex = new ValidationException(errorMap);
          Map<String, Object> nestedException = new HashMap<String, Object>();
          nestedException.put("orderableItems", ex.getErrors());
          throw new NestableValidationException(nestedException);
        }
      }
      Integer consultationTypeId = bean.get("consultation_type_id") != null
          ? (Integer) bean.get("consultation_type_id") : null;
      countDoc = itemType.equalsIgnoreCase("Department") && consultationTypeId != null
          ? countDoc + 1 : countDoc;
      countSer = itemType.equalsIgnoreCase("Service") ? countSer + 1 : countSer;
      counter++;
    }
    if (countDoc != 1 || countSer != 1 || list.size() != 2) {
      ValidationErrorMap errorMap = new ValidationErrorMap();
      errorMap.addError("packageId", "exception.scheduler.invalid.orderset");
      ValidationException ex = new ValidationException(errorMap);
      Map<String, Object> nestedException = new HashMap<String, Object>();
      nestedException.put("orderableItems", ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
  }

  public List<BasicDynaBean> getOrderedPackageItems(String mrNo) {
    return multiVisitPackageService.getOrderedPackageItems(mrNo);
  }

  private Map<String, Map<String, Object>> getBillsInfoMap(
      Map<String, Map<String, List<Object>>> orderedItems, Map<String, Object> visitParams,
      List<Map<String, Object>> patPackContIds, BasicDynaBean regHeaderInformation) {

    String visitId = (String) visitParams.get("visit_id");
    Map<Integer, Object> newMvpMap =
        (Map<Integer, Object>) visitParams.get("new_mvp_map");
    Integer commonOrderId = null;
    Map<String, Map<String, Object>> billInfoMap = new HashMap<>();

    List<String> billList = getItemsAddedBillList(orderedItems, newMvpMap, patPackContIds);

    if (billList.isEmpty()) {
      return billInfoMap;
    }
    if (null != regHeaderInformation && !isHasMvpOrder(orderedItems.get("new"))) {
      commonOrderId = (Integer) regHeaderInformation.get("commonorderid");
    } else {
      // Same common-order-id for the orders in multiple bills at one save.
      commonOrderId = DatabaseHelper.getNextSequence("common_order");
    }

    for (String billNo : billList) {
      billInfoMap.put(billNo, getBillInfoMap(orderedItems, visitId, billNo, commonOrderId,
          regHeaderInformation));
    }

    return billInfoMap;
  }

  private Map<String, Object> getBillInfoMap(Map<String, Map<String, List<Object>>> orderedItems,
      String visitId, String billNo, Integer commonOrderId, BasicDynaBean regHeaderInformation) {

    Map<String, Object> billInfoMap = new HashMap<>();
    BasicDynaBean bill;

    // Creating the new Bill
    if (billNo != null && billNo.startsWith("new")) {
      /*
       * Here hard coded third parameter, because method signature changed now using this method we
       * can create both bill now and bill later bills.
       */
      billInfoMap = billService.createBill(billNo.equals("newInsurance"), visitId, "P");
      bill = (BasicDynaBean) billInfoMap.get("bill");
    } else {
      bill = billService.findByKey(billNo);
      billInfoMap.put("bill", bill);
    }

    BasicDynaBean headerInformation = registrationService.getBillPatientInfo(visitId,
        (String) bill.get("bill_no"));
    headerInformation.set("commonorderid", commonOrderId);
    if (orderedItems != null && !orderedItems.isEmpty()) {
      BasicDynaBean mvpBean = multiVisitPackageService
          .getMultiVisitPackageBean(orderedItems.get(billNo));
      if (mvpBean != null) {
        Integer packageRef = null;
        if (null != regHeaderInformation) {
          packageRef = (Integer) regHeaderInformation.get("packageref");
        } else {
          packageRef = packageOrderItemService.getNextSequence();
        }
        headerInformation.set("packageref", packageRef);
      }
      billInfoMap.put("mvp_bean", mvpBean);
    }

    billInfoMap.put("header_information", headerInformation);

    billInfoMap.put("user_name", sessionService.getSessionAttributes().get("userId"));

    billInfoMap.put("plan_ids", patientInsurancePlansService.getPlanIds(visitId));

    return billInfoMap;
  }

  /**
   * returns the list of bills into which new items have been added.
   *
   * @param orderedItems
   *          the ordered items
   * @param newMvpMap
   *          the newMvpMap
   * @return the items added bill list
   */
  private List<String> getItemsAddedBillList(Map<String, Map<String, List<Object>>> orderedItems,
      Map<Integer, Object> newMvpMap, List<Map<String, Object>> patPackContIds ) {
    List<String> billList = new ArrayList<>();

    for (Map.Entry<String, Map<String, List<Object>>> billEntry : orderedItems.entrySet()) {
      Map<String, List<Object>> itemsMap = billEntry.getValue();
      String billNo = null;
      int patPackContentId = 0;
      for (Map.Entry<String, List<Object>> itemEntry : itemsMap.entrySet()) {
        List<Object> itemList = itemEntry.getValue();
        for (Object itemObject : itemList) {
          Map<String, Object> itemMap = (Map<String, Object>) itemObject;
          if (itemMap.get("mvp_index") != null
                  && newMvpMap.containsKey(itemMap.get("mvp_index"))) {
            itemMap.put("pat_package_id", newMvpMap.get(itemMap.get("mvp_index")));
            if (patPackContIds != null && !"Operation".equals(itemMap.get("type"))) {
              int packContentIndex = (int) itemMap.get("package_content_index");
              for (Map<String, Object> packContId : patPackContIds) {
                int packContInd = (int) packContId.get("packContIndex");
                if (packContInd == packContentIndex) {
                  itemMap.put("patient_package_content_id", packContId
                      .get("patient_package_content_id"));
                  patPackContIds.remove(packContId);
                  break;
                }
              }
            }
          }
          //getting the package content id from order package link
          if (patPackContIds.isEmpty() && itemMap.get("mvp_item") != null) {
            Map<String, List<Object>> mvpEntry = (Map<String, List<Object>>) itemMap
                .get("mvp_item");
            for (Map.Entry<String, List<Object>> mvpItem : mvpEntry.entrySet()) {
              if (mvpItem.getKey().equals("patient_package_content_id")) {
                itemMap.put("patient_package_content_id", mvpItem.getValue());
                patPackContentId ++;
              }
            }
          }
        }
      }

      for (Map.Entry<String, List<Object>> itemEntry : itemsMap.entrySet()) {
        List<Object> itemList = itemEntry.getValue();
        for (Object itemObject : itemList) {
          Map<String, Object> itemMap = (Map<String, Object>) itemObject;
          if (("operations".equals(itemEntry.getKey()) && checkItemAddedInOperation(itemMap))
              || ("equipments".equals(itemEntry.getKey()) && "Y".equals(itemMap.get("edited")))) {
            billNo = billEntry.getKey();
            break;
          } else {
            Object orderId = itemMap.get("order_id");
            if (orderId == null || "".equals(orderId)) {
              billNo = billEntry.getKey();
              break;
            }
          }
        }
        if (billNo != null) {
          break;
        }
      }
      if (billNo != null) {
        billList.add(billNo);
      }
    }

    return billList;
  }

  @SuppressWarnings("unchecked")
  private boolean checkItemAddedInOperation(Map<String, Object> itemMap) {
    Object orderId = itemMap.get("order_id");
    if (orderId == null || "".equals(orderId) || "Y".equals(itemMap.get("edited"))) {
      return true;
    }

    if (itemMap.get("primaryAnaesthetist") != null
        && !"".equals(itemMap.get("primaryAnaesthetist"))) {
      Map<String, Object> primaryAnaesthetist = (Map<String, Object>) itemMap
          .get("primaryAnaesthetist");
      Object primaryAnaesthetistOrderId = primaryAnaesthetist.get("order_id");
      if (primaryAnaesthetistOrderId == null || "".equals(primaryAnaesthetistOrderId)) {
        return true;
      }
    }

    List<Map<String, Object>> surgeryTeam = (List<Map<String, Object>>) itemMap.get("surgeryTeam");
    for (Map<String, Object> surgeryMember : surgeryTeam) {
      Object surgeryOrderId = surgeryMember.get("order_id");
      if (surgeryOrderId == null || "".equals(surgeryOrderId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the patient amount due.
   *
   * @param newInsuranceItems
   *          the new insurance items
   * @return the patient amount due
   */
  @SuppressWarnings("unchecked")
  private BigDecimal getPatientAmountDue(Map<String, List<Object>> newInsuranceItems) {

    BigDecimal patientAmtDue = BigDecimal.ZERO;
    for (Map.Entry<String, List<Object>> itementry : newInsuranceItems.entrySet()) {
      List<Object> itemList = itementry.getValue();
      if (itemList == null) {
        continue;
      }
      for (Object itemObject : itemList) {
        Map<String, Object> itemMap = (Map<String, Object>) itemObject;
        BigDecimal patientDue = itemMap.get("patient_amt") != null
            ? new BigDecimal(itemMap.get("patient_amt").toString()) : BigDecimal.ZERO;
        patientAmtDue = patientAmtDue.add(patientDue);
      }
    }
    return patientAmtDue;
  }

  /**
   * Update ordered items.
   *
   * @param orderedItems
   *          the ordered items
   * @param visitParams
   *          the visit params
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @param billsInfoMap
   *          the new bill info map
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   */
  @Transactional(rollbackFor = Exception.class)
  public void updateOrderedItems(Map<String, Map<String, List<Object>>> orderedItems,
      Map<String, Object> visitParams, List<String> editOrCancelOrderBills,
      Map<String, Map<String, Object>> billsInfoMap,
      List<Map<String, Object>> newMvpDetails) throws ParseException, IOException,
      NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    // Inserting Order Sets into patient packages, which returns patient package Ids.
    Map<String, Map<String, List<Object>>> billWisePatientPackageIdsMap = new HashMap<>();
    orderSetsOrderItemService.insertOrderSetsIntoPatientPackages(orderedItems,
        billWisePatientPackageIdsMap, billsInfoMap);

    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    // Insert or Update Order items other than multivisit Package.
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      OrderItemService orderItem = beanFactory.getBean(orderItemsTypesClassName);
      List<Object> allOrderItems = new ArrayList<>();
      Map<String, List<Object>> billItemWisePatPackIdsMap = new HashMap<>();

      // Grouping all the item of same type in different bill into one list.
      for (Map.Entry<String, Map<String, List<Object>>> billNoEntry : orderedItems.entrySet()) {
        String orderPrefix = orderAnnotation != null ? orderAnnotation.prefix() : "";
        List<Object> itemOrderList = billNoEntry.getValue().get(orderPrefix);

        if (itemOrderList != null && !itemOrderList.isEmpty()) {
          allOrderItems.addAll(itemOrderList);
        }

        Map<String, List<Object>> itemsPatPackIdsMap = billWisePatientPackageIdsMap
            .get(billNoEntry.getKey());
        if (itemsPatPackIdsMap != null) {
          List<Object> itemPatPackList = itemsPatPackIdsMap.get(orderPrefix);
          if (itemPatPackList != null) {
            billItemWisePatPackIdsMap.put(billNoEntry.getKey(), itemPatPackList);
          }
        }
      }

      if (!allOrderItems.isEmpty()) {
        orderItem.orderItems(allOrderItems, visitParams, editOrCancelOrderBills, billsInfoMap,
            billItemWisePatPackIdsMap);
      }
    }
    // Seperate Insert and Update for Multivisit Package.
    Integer count = 0;
    for (Map.Entry<String, Map<String, Object>> billEntry : billsInfoMap.entrySet()) {
      Map<String, Object> billInfoMap = billEntry.getValue();

      BasicDynaBean prescribedmvpBean = (BasicDynaBean) billInfoMap.get("mvp_bean");
      if (prescribedmvpBean != null) {
        BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
        Integer patientPackageId = multiVisitPackageService
                .orderMultiVisitPackage(prescribedmvpBean, headerInformation,
                        (String) billInfoMap.get("user_name"));
        multiVisitPackageService.updatePatientPackageStatus(patientPackageId);
      }
      count++;
    }

  }

  /**
   * Call order item service method. This method can be used only if the function returns List of
   * BasicDynaBean or BasicDynaBean
   *
   * @param methodName
   *          the method name
   * @param paramTypes
   *          the param types
   * @param params
   *          the params
   * @return the list
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> callOrderItemServiceMethod(String methodName,
      List<Class<?>> paramTypes, List<Object> params)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    List<BasicDynaBean> orderItemDetails = new ArrayList<>();

    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null) {
        OrderItemService orderItem = beanFactory.getBean(orderItemsTypesClassName);
        Method method = orderItemsTypesClassName.getMethod(methodName,
            paramTypes.toArray(new Class[0]));
        if (method.getReturnType().equals(List.class)) {
          orderItemDetails.addAll(
              (Collection<? extends BasicDynaBean>) method.invoke(orderItem, params.toArray()));
        } else {
          BasicDynaBean bean = (BasicDynaBean) method.invoke(orderItem, params.toArray());
          if (bean != null) {
            orderItemDetails.add(bean);
          }
        }

      }
    }
    return orderItemDetails;
  }

  /**
   * Call particular order item service method.
   *
   * @param methodName
   *          the method name
   * @param paramTypes
   *          the param types
   * @param params
   *          the params
   * @param type
   *          the type
   * @return the list
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   */
  public List<BasicDynaBean> callParticularOrderItemServiceMethod(String methodName,
      List<Class<?>> paramTypes, List<Object> params, String type)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    List<BasicDynaBean> orderItemDetails = new ArrayList<>();
    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      if (orderAnnotation != null && Arrays.asList(orderAnnotation.value()).contains(type)) {
        OrderItemService orderItem = beanFactory.getBean(orderItemsTypesClassName);
        Method method = orderItemsTypesClassName.getMethod(methodName,
            paramTypes.toArray(new Class[0]));
        if (method.getReturnType().equals(List.class)) {
          return (List<BasicDynaBean>) method.invoke(orderItem, params.toArray());
        } else {
          BasicDynaBean bean = (BasicDynaBean) method.invoke(orderItem, params.toArray());
          if (bean != null) {
            orderItemDetails.add(bean);
            return orderItemDetails;
          }
        }
      }
    }
    return orderItemDetails;
  }

  /**
   * Update orders.
   *
   * @param cancelItemsTypeMap
   *          the cancel items type map
   * @param cancel
   *          the cancel
   * @param cancelCharges
   *          the cancel charges
   * @param unlinkActivity
   *          the unlink activity
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @param itemInfoMap
   *          the item info map
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the ParseException
   */
  public void updateOrders(Map<String, List<BasicDynaBean>> cancelItemsTypeMap, boolean cancel,
      boolean cancelCharges, boolean unlinkActivity, List<String> editOrCancelOrderBills,
      Map<String, Object> itemInfoMap) throws NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, IOException, ParseException {

    Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
        .iterator();
    while (orderItemsTypesIterator.hasNext()) {
      Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
      Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
      Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
      for (String type : orderAnnotation.value()) {
        if (orderAnnotation != null && cancelItemsTypeMap.get(type) != null) {
          OrderItemService orderItem = ApplicationContextProvider.getBean(orderItemsTypesClassName);
          orderItem.updateOrders(cancelItemsTypeMap.get(type), cancel, cancelCharges,
              unlinkActivity, editOrCancelOrderBills, itemInfoMap);
        }
      }
    }
  }

  /**
   * Insert package contents.
   *
   * @param packageBean
   *          the package bean
   * @param packageContents
   *          the package contents
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param centerId
   *          the center id
   * @param chargeList
   *          the list of charges
   * @param packageItemDetails
   *          the package item details
   * @throws ParseException
   *           the ParseException
   */
  public void insertPackageContents(BasicDynaBean packageBean, List<BasicDynaBean> packageContents,
      BasicDynaBean headerInformation, String username, Integer centerId,
      List<BasicDynaBean> chargeList, Map<String, Object> packageItemDetails)
      throws ParseException {

    int index = 0;
    for (BasicDynaBean packageItem : packageContents) {

      Iterator<Class<? extends OrderItemService>> orderItemsTypesIterator = orderItemsTypes
          .iterator();

      while (orderItemsTypesIterator.hasNext()) {
        Class<? extends OrderItemService> orderItemsTypesClassName = orderItemsTypesIterator.next();
        Annotation[] annotations = orderItemsTypesClassName.getAnnotations();
        Order orderAnnotation = (Order) checkOrderAnnotionExist(annotations);
        OrderItemService orderItem = beanFactory.getBean(orderItemsTypesClassName);
        // Doctor order is handled differently.
        if (orderAnnotation != null
            && Arrays.asList(orderAnnotation.value()).contains(packageItem.get("activity_type"))
            && !"doctors".equals(orderAnnotation.prefix())) {
          orderItem.insertPackageContent(packageBean, packageItem, headerInformation, username,
              centerId, chargeList.get(index), packageItemDetails, index);
          index++;
        }
        if (orderAnnotation != null
                && Arrays.asList(orderAnnotation.value()).contains(packageItem.get("activity_type"))
                && "doctors".equals(orderAnnotation.prefix())) {
          orderItem.insertPackageContent(packageBean, packageItem, headerInformation, username,
                  centerId, chargeList.get(index), packageItemDetails, index);
          index++;
        }
      }
    }
  }

  /**
   * Returns the orderItem patient and insurance amount. Wrapper over item charge and estimate
   * amount Api. Doing all calculation here and get rid of calculation done on front-end.
   * 
   * 
   * @param params
   *          the params
   * @return map
   * @throws ParseException
   *           the ParseException
   * @throws SQLException
   *           the SQLException
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, Object> getItemChargeEstimate(Map<String, Object> params)
      throws ParseException, SQLException {

    List<Map<String, Object>> orderItems = (List<Map<String, Object>>) params
        .get("existing_ordered_items");

    Map<String, Object> processedChargedMap = new HashMap<>();
    if (params.get(NEWLY_ADDED_ORDERED_ITEM) != null) {
      Map<String, Object> newlyAddedItem = (Map<String, Object>) params
          .get(NEWLY_ADDED_ORDERED_ITEM);
      processedChargedMap = getItemCharges(newlyAddedItem);
    }

    List<String> referenceId = new ArrayList<>();
    Map<String, Object> newlyAddedOrderItemMap = (Map<String, Object>) params
        .get(NEWLY_ADDED_ORDERED_ITEM);
    Integer editOrderIndex = null;
    if (null != newlyAddedOrderItemMap && newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX) != null) {
      editOrderIndex = (Integer) newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX);
    }

    Map<String, Object> expandedExisitingOrderMap = expandOrderItems(orderItems, referenceId,
        editOrderIndex);
    List<Map<String, Object>> expandingExistingOrderItems =
        (List<Map<String, Object>>) expandedExisitingOrderMap
        .get("expandedOrderItemList");
    int editedItemIndex = (int) expandedExisitingOrderMap.get(UPDATED_EDITED_ORDER_ITEM_INDEX);
    if (null != newlyAddedOrderItemMap && newlyAddedOrderItemMap.get("type").equals("Operation")) {
      String orderItemId = (String) newlyAddedOrderItemMap.get("id");
      String refrenceStr = "";
      if (newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX) != null
          && !newlyAddedOrderItemMap.get(EDITED).equals("Y")) {
        // editing newly order operation and bill change
        Integer orderIndex = (Integer) newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX);
        refrenceStr = orderItemId.concat("_" + Integer.toString(orderIndex));
        referenceId.add(refrenceStr);
      } else if (newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX) != null
          && newlyAddedOrderItemMap.get(EDITED).equals("Y")) { // editing exisiting operation
        refrenceStr = ((orderItems.get(editOrderIndex)).get("id")).toString();
      } else {
        String chargeId = (String) newlyAddedOrderItemMap.get("charge_id");
        refrenceStr = orderItemId.concat(chargeId);
        referenceId.add(refrenceStr);
      }

      if (newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX) != null) {
        List<Map<String, Object>> expandedOrderItem = getSubItemOrderList(processedChargedMap,
            refrenceStr, (String) newlyAddedOrderItemMap.get("type"), params);
        for (Map<String, Object> expandedItem : expandedOrderItem) {
          expandingExistingOrderItems.set(editedItemIndex, expandedItem);
          editedItemIndex++;
        }
      } else {
        expandingExistingOrderItems.addAll(getSubItemOrderList(processedChargedMap, refrenceStr,
            (String) newlyAddedOrderItemMap.get("type"), params));
      }
    } else if (null != newlyAddedOrderItemMap) {
      if (newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX) != null) {
        expandingExistingOrderItems.set(editedItemIndex,
            createNewlyAddedOrderMap(processedChargedMap, params));
      } else {
        expandingExistingOrderItems.add(createNewlyAddedOrderMap(processedChargedMap, params));
      }
    }
    Map<String, Object> estimateAmtParams = new HashMap<>();
    ArrayList insuranceList = (ArrayList) params.get("insurance_details");
    estimateAmtParams.put("insurance_details", insuranceList);
    Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
    estimateAmtParams.put("visit", visitParams);
    estimateAmtParams.put("ordered_items", expandingExistingOrderItems);
    Map<String, Object> estimateAmtMap = billService.estimateAmount(estimateAmtParams);

    List<Map<String, Object>> finalOrderItemAmount = new ArrayList<>(expandingExistingOrderItems);
    calculatePatientSponsorAmt(estimateAmtMap, finalOrderItemAmount, insuranceList);
    Map<String, Object> itemChargesData = new HashMap<>();
    itemChargesData.put("overall_adjustment", groupSubOrderItem(finalOrderItemAmount, referenceId));
    return itemChargesData;
  }

  private void setPostedDateFromPrescDate(Map<String, Object> orderedItem) {
    if (null != orderedItem.get("pres_timestamp")
        && !"".equals(orderedItem.get("pres_timestamp"))) {
      orderedItem.put("posted_date", (String) orderedItem.get("pres_timestamp"));
    } else if ((null != orderedItem.get("prescribed_date_date")
        && null != orderedItem.get("prescribed_date_time"))
        && (!"".equals(orderedItem.get("prescribed_date_date"))
            && !"".equals(orderedItem.get("prescribed_date_time")))) {
      String prescribedDate = (String) orderedItem.get("prescribed_date_date");
      String prescribedTime = (String) orderedItem.get("prescribed_date_time");
      String orderDateTime = prescribedDate + " " + prescribedTime + ":00";
      orderedItem.put("posted_date", orderDateTime);
    }
  }

  /**
   * Return the Map from itemCharge List for item type apart from Operation or item type that have
   * sub type.
   * 
   * @param processedChargedMap
   *          the processedChargedMap
   * @param params
   *          the params
   * @return map
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> createNewlyAddedOrderMap(Map<String, Object> processedChargedMap,
      Map<String, Object> params) {

    List<BasicDynaBean> itemChargesList = (List<BasicDynaBean>) processedChargedMap
        .get("charge_list");

    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal totalTaxAmount = BigDecimal.ZERO;
    for (BasicDynaBean itemCharges : itemChargesList) {
      totalAmount = totalAmount.add((BigDecimal) itemCharges.get("amount"));
      totalTaxAmount = totalTaxAmount.add((BigDecimal) itemCharges.get(TAX_AMT));
    }

    Map<String, Object> newAddedOrderItemMap = itemChargesList.get(0).getMap();
    newAddedOrderItemMap
        .putAll((Map<? extends String, ? extends Object>) params.get(NEWLY_ADDED_ORDERED_ITEM));
    BigDecimal packCharge = BigDecimal.ZERO;
    BigDecimal packDiscount = BigDecimal.ZERO;
    Map<String, Object> chargeIdMap = new HashMap<String, Object>();
    String mainChargeId = (String) newAddedOrderItemMap.get("charge_id");
    Integer chargeIdInc = 0;
    if (null != newAddedOrderItemMap.get("package_contents")
        && ((List<Map<String, Object>>) newAddedOrderItemMap.get("package_contents")).size() > 0) {
      List<Map<String, Object>> packContents =
          (List<Map<String, Object>>) newAddedOrderItemMap.get("package_contents");
      for (Map<String, Object> packContent : packContents) {
        chargeIdMap.put("_" + mainChargeId + "_" + String.valueOf(chargeIdInc), packContent);
        if (packCharge.compareTo(BigDecimal.ZERO) == 0) {
          packCharge = packCharge.add(
              new BigDecimal(String.valueOf(packContent.get("package_charge"))));
          packDiscount = packDiscount.add(
              new BigDecimal(String.valueOf(packContent.get("package_discount"))));
          params.put("package_charge", packCharge);
          params.put("package_discount", packDiscount);
        }
        chargeIdInc++;
      }
    }
    newAddedOrderItemMap.put("amount", totalAmount.toString());
    newAddedOrderItemMap.put(TAX_AMT, totalTaxAmount.toString());
    setOrderItemMap(newAddedOrderItemMap, processedChargedMap, params);
    if (processedChargedMap.get("package_contents") != null) {
      List<Map<String, Object>> packContents =
          (List<Map<String, Object>>) processedChargedMap.get("package_contents");
      for (Map<String, Object> packCont: packContents) {
        Map<String, Object> existingContent =
            (Map<String, Object>) (chargeIdMap.containsKey(packCont.get("charge_id"))
            ? chargeIdMap.get(packCont.get("charge_id")) : null);
        if (existingContent != null) {
          packCont
          .putAll((Map<? extends String, ? extends Object>) existingContent);
        }
        setOrderItemMap(packCont, processedChargedMap, params);
      }
      newAddedOrderItemMap.put("package_contents",
          packContents);
    }
    return newAddedOrderItemMap;
  }

  private void setOrderItemMap(Map<String, Object> orderItem, Map<String,
      Object> processedChargedMap, Map<String, Object> params) {
    orderItem.put("discount",
        getParamDefault(orderItem, "discount", BigDecimal.ZERO).toString());
    orderItem.put("package_charge",
        getParamDefault(params, "package_charge", BigDecimal.ZERO));
    orderItem.put("package_discount",
        getParamDefault(params, "package_discount", BigDecimal.ZERO));
    orderItem.put("amount",
        getParamDefault(orderItem, "amount", BigDecimal.ZERO).toString());
    orderItem.put("tax_amt",
        getParamDefault(orderItem, "tax_amt", BigDecimal.ZERO).toString());
    orderItem.put(INSURANCE_CATEGORY_ID,
        orderItem.get(INSURANCE_CATEGORY_ID));
    int consultationTypeId = 0;
    if (null != orderItem.get(CONSULTATION_TYPE_ID)) {
      consultationTypeId = (Integer) orderItem.get(CONSULTATION_TYPE_ID);
    }
    orderItem.put(CONSULTATION_TYPE_ID, consultationTypeId);
    if (((Map<String, String>) params.get(NEWLY_ADDED_ORDERED_ITEM)).get("edited") == null
        || !((Map<String, String>) params.get(NEWLY_ADDED_ORDERED_ITEM)).get("edited")
            .equals("Y")) {
      orderItem.put("new", "Y");
    } else {
      orderItem.put(EDITED, "Y");
    }
    if (orderItem.get("id") instanceof Integer) {
      orderItem.put("id", String.valueOf((int)orderItem.get("id")));
    }
    Map<String, Object> selectedBillRatePlanMap = (Map<String, Object>) processedChargedMap
        .get("selected_bill");
    orderItem.put(BILL_NO, selectedBillRatePlanMap.get(BILL_NO));
    orderItem.put(ORG_ID, selectedBillRatePlanMap.get(ORG_ID));
    if (selectedBillRatePlanMap.get("is_tpa") != null
        && !selectedBillRatePlanMap.get("is_tpa").equals("")) {
      orderItem.put("is_tpa", selectedBillRatePlanMap.get("is_tpa"));
    }
    String isDoctorExcluded = ((Map<String, String>) params.get(NEWLY_ADDED_ORDERED_ITEM))
        .get("item_excluded_from_doctor");
    if (("Y").equals(isDoctorExcluded)) {
      orderItem.put("item_excluded_from_doctor", true);
      orderItem.put("item_excluded_from_doctor_remarks",
          ((Map<String, String>) params.get(NEWLY_ADDED_ORDERED_ITEM))
              .get("item_excluded_from_doctor_remarks"));
    } else if (("N").equals(isDoctorExcluded)) {
      orderItem.put("item_excluded_from_doctor", false);
      orderItem.put("item_excluded_from_doctor_remarks",
          ((Map<String, String>) params.get(NEWLY_ADDED_ORDERED_ITEM))
              .get("item_excluded_from_doctor_remarks"));
    }
    setPostedDateFromPrescDate(orderItem);
  }

  /**
   * Expanding the items list that have subItem List like operation. We need to return the
   * updatedEditedOrderItem index as we are expanding the order item, edited item index will be
   * changed.
   * 
   * @param orderItems
   *          the orderItems
   * @param referenceId
   *          the referenceId
   * @param editOrderIndex
   *          the editOrderIndex
   * @return map
   */
  private Map<String, Object> expandOrderItems(List<Map<String, Object>> orderItems,
      List<String> referenceId, Integer editOrderIndex) {
    List<Map<String, Object>> expandedOrderItems = new ArrayList<>();
    Integer expandedEditOrderIndex = 0;
    Integer updatedEditOrderIndex = -1;
    Map<String, Boolean> isInsuranceBillMap = new HashMap<>();
    isInsuranceBillMap.put("newInsurance", true);
    isInsuranceBillMap.put("new", false);
    Map<String, Object> expandedOrderItemMap = new HashMap<>();
    for (int orderItemIterator = 0; orderItemIterator < orderItems.size(); orderItemIterator++) {
      if (editOrderIndex != null && orderItemIterator == editOrderIndex) {
        updatedEditOrderIndex = expandedEditOrderIndex;
      }
      Map<String, Object> orderedItem = orderItems.get(orderItemIterator);
      setPostedDateFromPrescDate(orderedItem);
      if (orderedItem.get("type").equals("Operation")) {
        String refStr = orderedItem.get("id").toString();
        if (orderedItem.get("new").equals("Y")) {
          Integer orderIndex = (Integer) orderedItem.get("index");
          refStr = refStr.concat("_" + Integer.toString(orderIndex - 1));
        }
        referenceId.add(refStr);
        List<Map<String, Object>> exoandedOperationList = operationOrderService
            .expandOperationMap(orderedItem, refStr);
        expandedOrderItems.addAll(exoandedOperationList);
        expandedEditOrderIndex += exoandedOperationList.size();
      } else {
        if (isInsuranceBillMap.containsKey(orderedItem.get("bill_no"))) {
          orderedItem.put("is_tpa", isInsuranceBillMap.get(orderedItem.get("bill_no")));
        } else {
          String billNo = (String) orderedItem.get("bill_no");
          BasicDynaBean billBean = billService.getBill(billNo);
          if (null != billBean) {
            orderedItem.put("is_tpa", (Boolean) billBean.get("is_tpa"));
            isInsuranceBillMap.put(billNo, (Boolean) billBean.get("is_tpa"));
          } else {
            orderedItem.put("is_tpa", false);
            isInsuranceBillMap.put(billNo, false);
          }
        }
        if (orderedItem.get("head") != null && !"".equals(orderedItem.get("head"))) {
          orderedItem.put("charge_type", orderedItem.get("head"));
          orderedItem.put("chargeType", orderedItem.get("head"));
        }
        if (orderedItem.get("insurance_category_id") != null
            && orderedItem.get("insurance_category_id") instanceof String) {
          orderedItem.put("insurance_category_id",
              Integer.valueOf((String) orderedItem.get("insurance_category_id")));
        }
        expandedOrderItems.add(orderedItem);
        expandedEditOrderIndex++;
      }
    }
    expandedOrderItemMap.put("expandedOrderItemList", expandedOrderItems);
    expandedOrderItemMap.put(UPDATED_EDITED_ORDER_ITEM_INDEX, updatedEditOrderIndex);
    return expandedOrderItemMap;
  }

  /**
   * Grouping the subOrder Items. Keeping visitArray to maintain the same order as it was passed.
   * <p>
   * There are 2 possiblity of grouping. a) if order is exisiting order: in this case operation id
   * check will work for grouping. b) if order is new: in this case based on charge id we group.
   * 
   * Special case, suppose we added a new operation and again added new item. Then for grouping
   * exisiting new operation, operation id will be used.
   * </p>
   * 
   * @param finalOrderItemAmount
   *          the finalOrderItemAmount
   * @param referenceId
   *          the referenceId
   * @return list of object
   */
  private List<Object> groupSubOrderItem(List<Map<String, Object>> finalOrderItemAmount,
      List<String> referenceId) {

    List<Object> groupedList = new ArrayList<>();
    Boolean[] visitedOrderItem = new Boolean[finalOrderItemAmount.size()];
    Arrays.fill(visitedOrderItem, Boolean.FALSE);
    int refIndex = 0;
    for (int visitedIterator = 0; visitedIterator < visitedOrderItem.length; visitedIterator++) {
      if (!visitedOrderItem[visitedIterator]) {
        Map<String, Object> orderItem = finalOrderItemAmount.get(visitedIterator);
        String chargeId = (String) orderItem.get("charge_id");
        String type = (String) orderItem.get("type");
        String chargeGroup = (String) orderItem.get("charge_group");
        String operationId = "";
        if (null != orderItem.get(OPERATION_ID)) {
          operationId = (String) orderItem.get(OPERATION_ID);
        }
        boolean isPartOfSubOrder = checkPartSubOrder(chargeId, referenceId, operationId, type,
            chargeGroup);

        if (!isPartOfSubOrder) {
          groupedList.add(finalOrderItemAmount.get(visitedIterator));
          visitedOrderItem[visitedIterator] = Boolean.TRUE;
        } else {
          List<Map<String, Object>> subOrderItem = new ArrayList<>();
          String refId = referenceId.get(refIndex);
          for (int orderItemIterator = 0; orderItemIterator < finalOrderItemAmount
              .size(); orderItemIterator++) {
            Map<String, Object> orderedItem = finalOrderItemAmount.get(orderItemIterator);
            String orderedChargeGroup = (String) orderedItem.get("charge_group");
            if (((((String) orderedItem.get("charge_id")).contains(refId)
                && !orderedItem.get("charge_id").equals(refId) && orderedChargeGroup != null
                && orderedChargeGroup.equals("OPE"))
                || (null != orderedItem.get(OPERATION_ID)
                    && orderedItem.get(OPERATION_ID).equals(refId)
                    && orderedItem.get("type").equals("Operation")))
                && !visitedOrderItem[orderItemIterator]) {
              subOrderItem.add(orderedItem);
              visitedOrderItem[orderItemIterator] = Boolean.TRUE;
            }
          }
          refIndex++;
          groupedList.add(subOrderItem);
        }
      }
    }

    return groupedList;
  }

  /**
   * Check order item is part of group of items like part of operation.
   * 
   * @param chargeId
   *          the chargeId
   * @param referenceId
   *          the referenceId
   * @return boolean
   */
  private boolean checkPartSubOrder(String chargeId, List<String> referenceId, String operationId,
      String type, String chargeGroup) {
    for (String refStr : referenceId) {
      if ((chargeId.contains(refStr) && chargeGroup != null && chargeGroup.equals("OPE"))
          || (operationId.equals(refStr) && type.equals("Operation"))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Processing the order item if it's have subItem inside it.
   * 
   * @param processedChargedMap
   *          the processedChargedMap
   * @param refrenceId
   *          the refrenceId
   * @param type
   *          the type
   * @param params
   *          the params
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getSubItemOrderList(Map<String, Object> processedChargedMap,
      String refrenceId, String type, Map<String, Object> params) {

    List<Map<String, Object>> subOrderItemList = new ArrayList<>();
    int index = 0;

    List<BasicDynaBean> itemChargesList = (List<BasicDynaBean>) processedChargedMap
        .get("charge_list");
    Map<String, Object> selectedBillRatePlanMap = (Map<String, Object>) processedChargedMap
        .get("selected_bill");

    Map<String, Object> newlyAddedOrderItemMap = (Map<String, Object>) params
        .get(NEWLY_ADDED_ORDERED_ITEM);

    List<Map<String, Object>> orderItems = (List<Map<String, Object>>) params
        .get("existing_ordered_items");

    boolean isEdited = newlyAddedOrderItemMap.get(EDITED).equals("Y");
    if (!isEdited) {
      for (BasicDynaBean itemCharge : itemChargesList) {
        Map<String, Object> itemChargeMap = itemCharge.getMap();
        itemChargeMap.put("charge_id", "_".concat(refrenceId + "_" + Integer.toString(index)));
        itemChargeMap.put("new", "Y");
        itemChargeMap.put("amount", itemChargeMap.get("amount").toString());
        itemChargeMap.put(TAX_AMT, itemChargeMap.get(TAX_AMT).toString());
        itemChargeMap.put("discount", itemChargeMap.get("discount").toString());
        itemChargeMap.put("act_quantity", itemChargeMap.get("act_quantity").toString());
        itemChargeMap.put("type", type);
        itemChargeMap.put(INSURANCE_CATEGORY_ID,
            itemChargeMap.get(INSURANCE_CATEGORY_ID).toString());
        int consultationTypeId = 0;
        if (null != itemChargeMap.get(CONSULTATION_TYPE_ID)) {
          consultationTypeId = (Integer) itemChargeMap.get(CONSULTATION_TYPE_ID);
        }
        itemChargeMap.put(CONSULTATION_TYPE_ID, consultationTypeId);
        itemChargeMap.put(BILL_NO, selectedBillRatePlanMap.get(BILL_NO));
        if (selectedBillRatePlanMap.get("is_tpa") != null
            && !selectedBillRatePlanMap.get("is_tpa").equals("")) {
          itemChargeMap.put("is_tpa", selectedBillRatePlanMap.get("is_tpa"));
        }
        setPostedDateFromPrescDate(itemChargeMap);
        subOrderItemList.add(itemChargeMap);
        index++;
      }
    } else {
      Integer orderIndex = (Integer) newlyAddedOrderItemMap.get(EDITED_ITEM_INDEX);
      Map<String, Object> orderedItem = orderItems.get(orderIndex);
      setPostedDateFromPrescDate(orderedItem);
      subOrderItemList = operationOrderService.expandEditOperationMap(orderedItem, itemChargesList,
          refrenceId, selectedBillRatePlanMap);
    }

    return subOrderItemList;
  }

  /**
   * Function give separate insurance amount, tax, patient amount, patient tax for each order item.
   * Taking care of item having subItems like Operation
   * 
   * @param estimateAmtMap
   *          the estimateAmtMap
   * @param finalOrderItemAmount
   *          the finalOrderItemAmount
   * @param insuranceList
   *          the insuranceList
   */
  @SuppressWarnings({ "unchecked", "unlikely-arg-type" })
  private void calculatePatientSponsorAmt(Map<String, Object> estimateAmtMap,
      List<Map<String, Object>> finalOrderItemAmount, List<Map<String, Object>> insuranceList) {

    for (int orderItemIterator = 0; orderItemIterator < finalOrderItemAmount
        .size(); orderItemIterator++) {

      Map<String, Object> orderedItem = finalOrderItemAmount.get(orderItemIterator);
      if (orderedItem.get("package_contents") != null) {
        List<Map<String, Object>> packContentAmount =
            (List<Map<String, Object>>) orderedItem.get("package_contents");
        if (packContentAmount.size() > 0) {
          calculatePatientSponsorAmt(estimateAmtMap, packContentAmount, insuranceList);
        }
      }
     
      BigDecimal totalInsuranceAmount = BigDecimal.ZERO;
      BigDecimal[] insuranceAmount = new BigDecimal[insuranceList.size()];
      BigDecimal[] insuranceTaxAmount = new BigDecimal[insuranceList.size()];
      BigDecimal totalInsuranceTax = BigDecimal.ZERO;
      String chargeId = (String) orderedItem.get("charge_id");
      for (int insuranceIterator = 0; insuranceIterator < insuranceList
          .size(); insuranceIterator++) {
        int planId = (Integer) insuranceList.get(insuranceIterator).get("plan_id");
        Map<String, Object> insuranceEstimate = 
            (Map<String, Object>) ((Map<String, Object>) estimateAmtMap
            .get(ESTIMATE_AMOUNT)).get(planId);

        BigDecimal insuranceAmt = BigDecimal.ZERO;
        BigDecimal insuranceTaxAmt = BigDecimal.ZERO;
        Map<String, Object> insuranceChargeMap = (Map<String, Object>) insuranceEstimate
            .get(chargeId);
        String chargeHead = (String) orderedItem.get("charge_head");
        if (null != insuranceChargeMap && null != chargeHead
            && !"PKGPKG".equalsIgnoreCase(chargeHead)) {
          insuranceAmt = (BigDecimal) insuranceChargeMap.get("insurance_claim_amt");
          insuranceTaxAmt = (BigDecimal) insuranceChargeMap.get(TAX_AMT);
        }

        totalInsuranceAmount = totalInsuranceAmount.add(insuranceAmt);
        insuranceAmount[insuranceIterator] = insuranceAmt;

        insuranceTaxAmount[insuranceIterator] = insuranceTaxAmt;
        totalInsuranceTax = totalInsuranceTax.add(insuranceTaxAmt);
      }
      String chargeHead = (String) orderedItem.get("charge_head");
      if (null == chargeHead && "Package".equals(orderedItem.get("type"))
          && !(Boolean)orderedItem.get("multi_visit_package")) {
        totalInsuranceAmount = new BigDecimal(
            String.valueOf(orderedItem.get("insurance_claim_amount")));
        totalInsuranceTax = new BigDecimal(String.valueOf(orderedItem.get("sponsor_tax_amt")));
      }

      if (orderedItem.get("package_contents") != null) {
        List<Map<String, Object>> packageContents =
            (List<Map<String, Object>>) orderedItem.get("package_contents");
        for (Map<String, Object> content : packageContents) {
          for (int insuranceIterator = 0; insuranceIterator < insuranceList
              .size(); insuranceIterator++) {

            BigDecimal insuranceAmt =
                ((BigDecimal[]) content.get("insurance_amount"))[insuranceIterator];
            BigDecimal insuranceTaxAmt =
                ((BigDecimal[]) content.get("insurance_tax"))[insuranceIterator];


            totalInsuranceAmount = totalInsuranceAmount.add(insuranceAmt);
            insuranceAmount[insuranceIterator] = totalInsuranceAmount;

            totalInsuranceTax = totalInsuranceTax.add(insuranceTaxAmt);
            insuranceTaxAmount[insuranceIterator] = totalInsuranceTax;

          }
        }
      }
      BigDecimal totalItemAmount =
          (BigDecimal) ((orderedItem.get("amount") instanceof BigDecimal)
          ? orderedItem.get("amount") : new BigDecimal((String) orderedItem.get("amount")));
      BigDecimal totalTax = (BigDecimal) ((orderedItem.get(TAX_AMT) instanceof BigDecimal)
          ? orderedItem.get(TAX_AMT) : new BigDecimal((String) orderedItem.get(TAX_AMT)));
      Map<String, Object> ksaTaxAdjMap = 
          (Map<String, Object>) ((Map<Integer, Object>) estimateAmtMap
          .get(ESTIMATE_AMOUNT)).get(-2);
      setPatientInsuranceAmtDetails(orderedItem, totalItemAmount, totalTax, totalInsuranceAmount,
          totalInsuranceTax, ksaTaxAdjMap);

      finalOrderItemAmount.get(orderItemIterator).put("insurance_amount", insuranceAmount);
      finalOrderItemAmount.get(orderItemIterator).put("insurance_tax", insuranceTaxAmount);
    }
  }

  /**
   * Used to calculate Patient Amount, marking it to Zero is cancelled.
   * 
   * @param orderItemMap
   *          the orderItemMap
   * @param totalItemAmount
   *          the totalItemAmount
   * @param totalTax
   *          the totalTax
   * @param totalInsuranceAmount
   *          the totalInsuranceAmount
   * @param totalInsuranceTax
   *          the totalInsuranceTax
   * @param ksaTaxAdjMap
   *          the ksaTaxAdjMap
   */
  private void setPatientInsuranceAmtDetails(Map<String, Object> orderItemMap,
      BigDecimal totalItemAmount, BigDecimal totalTax, BigDecimal totalInsuranceAmount,
      BigDecimal totalInsuranceTax, Map<String, Object> ksaTaxAdjMap) {

    Boolean noTax = "Y".equals(ksaTaxAdjMap.get(orderItemMap.get("charge_id")));
    BigDecimal patientAmount = totalItemAmount.subtract(totalTax).subtract(totalInsuranceAmount);
    BigDecimal patientTax = totalTax.subtract(totalInsuranceTax);
    orderItemMap.put("patient_amt", patientAmount);
    if (noTax) {
      orderItemMap.put("patient_tax", BigDecimal.ZERO);
    } else {
      orderItemMap.put("patient_tax", patientTax);
    }

    if (null != orderItemMap.get(CANCELLED) && orderItemMap.get(CANCELLED).equals("IC")) {
      orderItemMap.put("patient_amt", BigDecimal.ZERO);
      orderItemMap.put("patient_tax", BigDecimal.ZERO);
    }

  }

  /**
   * Common function to get value from map, if empty or null return default value.
   * 
   * @param itemDetailParams
   *          the itemDetailParams
   * @param paramName
   *          the paramName
   * @param defaultValue
   *          the defaultValue
   * @return object
   */
  public Object getParamDefault(Map<String, Object> itemDetailParams, String paramName,
      Object defaultValue) {
    Object value = itemDetailParams.get(paramName);
    if ((value == null) || value.equals("")) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Gets the consultation types.
   *
   * @param orgIds
   *          the org ids
   * @param visitType
   *          the visit type
   * @return the consultation types
   */
  public List<Map> getConsultationTypes(List<String> orgIds, String visitType) {
    return getConsultationTypes(orgIds, visitType, "N");
  }

  /**
   * Gets the consultation types.
   *
   * @param orgIds
   *          the org ids
   * @param visitType
   *          the visit type
   * @return the consultation types
   */
  public List<Map> getConsultationTypes(List<String> orgIds, String visitType,
      Integer practitionerTypeId) {
    return getConsultationTypes(orgIds, visitType, "N", practitionerTypeId);
  }

  /**
   * Return the consultation Type as per ratePlan and visitType.
   * 
   * @param orgIds
   *          the orgIds
   * @param visitType
   *          the visitType
   * @return list of map
   */
  public List<Map> getConsultationTypes(List<String> orgIds, String visitType,
      String includeOtDocCharges) {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");

    Map<String, Object> params = new HashMap<>();
    params.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);

    String healthAuthority = (String) centerBean.get("health_authority");
    if (null == healthAuthority) {
      healthAuthority = "";
    }

    List<BasicDynaBean> consTypesBeans;

    String operationApplicability = (String) genericPreferencesService.getAllPreferences()
        .get("operation_apllicable_for");
    if ("o".equals(visitType)) {
      if (("b".equals(operationApplicability) || "o".equals(operationApplicability))
          && "Y".equals(includeOtDocCharges)) {
        consTypesBeans = consultationTypesService.getConsultationTypes(visitType, "ot", orgIds,
            healthAuthority);
      } else {
        consTypesBeans = consultationTypesService.getConsultationTypes(visitType, orgIds,
            healthAuthority);
      }
    } else if (includeOtDocCharges.equals("Y")) {
      /* combined list of ip and ot types for billing */
      if (operationApplicability.equals("b") || operationApplicability.equals("i")) {
        consTypesBeans = consultationTypesService.getConsultationTypes("i", "ot", orgIds,
            healthAuthority);
      } else {
        consTypesBeans = consultationTypesService.getConsultationTypes("i", orgIds,
            healthAuthority);
      }
    } else if ("i".equals(visitType)) {
      consTypesBeans = consultationTypesService.getConsultationTypes(visitType, orgIds,
          healthAuthority);
    } else {
      consTypesBeans = consultationTypesService.getConsultationTypes("i", "o", healthAuthority);
    }

    List<Map> returnList = new ArrayList<>();
    for (BasicDynaBean bean : consTypesBeans) {
      Map beanMap = new HashMap<>(bean.getMap());
      String orgIdString = (String) beanMap.get("org_ids");
      beanMap.put("org_ids", orgIdString.split(Pattern.quote(", ")));
      returnList.add(beanMap);
    }

    return returnList;
  }

  /**
   * Return the consultation Type as per ratePlan and visitType.
   * 
   * @param orgIds
   *          the orgIds
   * @param visitType
   *          the visitType
   * @param includeOtDocCharges
   *          the includeOtDocCharges
   * @param practitionerTypeId
   *          the practitionerTypeId
   * @return list of map
   */
  public List<Map> getConsultationTypes(List<String> orgIds, String visitType,
      String includeOtDocCharges, Integer practitionerTypeId) {

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");

    Map<String, Object> params = new HashMap<>();
    params.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);

    String healthAuthority = (String) centerBean.get("health_authority");
    if (null == healthAuthority) {
      healthAuthority = "";
    }

    List<BasicDynaBean> consTypesBeans;

    String operationApplicability = (String) genericPreferencesService.getAllPreferences()
        .get("operation_apllicable_for");
    if ("o".equals(visitType)) {
      if (("b".equals(operationApplicability) || "o".equals(operationApplicability))
          && "Y".equals(includeOtDocCharges)) {
        consTypesBeans = consultationTypesService.getConsultationTypes(visitType, "ot", orgIds,
            healthAuthority, practitionerTypeId);
      } else {
        consTypesBeans = consultationTypesService.getConsultationTypes(visitType, orgIds,
            healthAuthority, practitionerTypeId);
      }
    } else if (includeOtDocCharges.equals("Y")) {
      /* combined list of ip and ot types for billing */
      if (operationApplicability.equals("b") || operationApplicability.equals("i")) {
        consTypesBeans = consultationTypesService.getConsultationTypes("i", "ot", orgIds,
            healthAuthority, practitionerTypeId);
      } else {
        consTypesBeans = consultationTypesService.getConsultationTypes("i", orgIds, healthAuthority,
            practitionerTypeId);
      }
    } else if ("i".equals(visitType)) {
      consTypesBeans = consultationTypesService.getConsultationTypes(visitType, orgIds,
          healthAuthority, practitionerTypeId);
    } else {
      consTypesBeans = consultationTypesService.getConsultationTypes("i", "o", healthAuthority,
          practitionerTypeId);
    }

    List<Map> returnList = new ArrayList<>();
    for (BasicDynaBean bean : consTypesBeans) {
      Map beanMap = new HashMap<>(bean.getMap());
      String orgIdString = (String) beanMap.get("org_ids");
      beanMap.put("org_ids", orgIdString.split(Pattern.quote(", ")));
      returnList.add(beanMap);
    }

    return returnList;
  }

  /**
   * Gets the additional docs.
   *
   * @param prescribedId
   *          the prescribed id
   * @return the additional docs
   */
  public List<BasicDynaBean> getAdditionalDocs(Integer prescribedId) {
    return testDocumentsService.getAdditionalDocs(prescribedId);
  }

  /**
   * get common order ids.
   * 
   * @param visitId
   *          the visitId
   * @return list of object
   */
  public List<Object> getCommonOrderIds(String visitId) {
    List<BasicDynaBean> beans = billService.getCommonOrderIds(visitId);
    List<Object> orderList = new ArrayList<>();
    for (BasicDynaBean bean : beans) {
      orderList.add(bean.get("order_number"));
    }
    return orderList;
  }

  /**
   * This method will return patient due across all bills per visit.
   *
   * @param visitId
   *          the visitId id
   * @return will return BigDecimal value
   */

  private BigDecimal getVisitPatientDue(String visitId) {
    return billService.getVisitPatientDue(visitId);
  }

  private Map<String, Object> getCreditLimitDetails(String visitId) {
    return billService.getCreditLimitDetails(visitId);
  }

  protected Map<String, Object> updateOrderResponse(String visitId, String visitType, String mrNo,
      List<String> itemTypes) {
    Map<String, Object> updateOrderedItemsMap = new HashMap<>();
    updateOrderedItemsMap.put("bills", ConversionUtils
        .listBeanToListMap(getUnpaidBillsForVisit(visitId, visitType, null, "N", null)));
    updateOrderedItemsMap.put("multi_visit_package_bills",
        ConversionUtils.listBeanToListMap(getMvpBillsForVisit(visitId)));
    updateOrderedItemsMap.put("server_time", new Date());
    updateOrderedItemsMap.putAll(getOrders(visitId, visitType, itemTypes));
    updateOrderedItemsMap.put("visit_patient_due", getVisitPatientDue(visitId));
    updateOrderedItemsMap.put("creditLimitDetailsMap", getCreditLimitDetails(visitId));
    updateOrderedItemsMap.putAll(getVisitDetails(mrNo, visitType));
    return updateOrderedItemsMap;
  }

  /**
   * Gets the reg chargeand discount.
   *
   * @param chargeHead
   *          the charge head
   * @param isRenewal
   *          the is renewal
   * @param orgId
   *          the org id
   * @param bedType
   *          the bed type
   * @param visitType
   *          the visit type
   * @return the reg chargeand discount
   */
  public Map<String, BigDecimal> getRegChargeandDiscount(String chargeHead, boolean isRenewal,
      String orgId, String bedType, String visitType) {

    BigDecimal charge = null;
    BigDecimal discount = null;
    BasicDynaBean regChargesBean = registrationChargesService.getRegistrationCharges(bedType,
        orgId);
    if (chargeHead.equals("GREG")) {
      if (isRenewal) {
        charge = (BigDecimal) regChargesBean.get("reg_renewal_charge");
        discount = (BigDecimal) regChargesBean.get("reg_renewal_charge_discount");
      } else {
        charge = (BigDecimal) regChargesBean.get("gen_reg_charge");
        discount = (BigDecimal) regChargesBean.get("gen_reg_charge_discount");
      }
    } else if (chargeHead.equals("IPREG")) {
      charge = (BigDecimal) regChargesBean.get("ip_reg_charge");
      discount = (BigDecimal) regChargesBean.get("ip_reg_charge_discount");
    } else if (chargeHead.equals("OPREG")) {
      charge = (BigDecimal) regChargesBean.get("op_reg_charge");
      discount = (BigDecimal) regChargesBean.get("op_reg_charge_discount");
    } else if (chargeHead.equals("MLREG")) {
      if (visitType.equalsIgnoreCase("i")) {
        charge = (BigDecimal) regChargesBean.get("ip_mlccharge");
        discount = (BigDecimal) regChargesBean.get("ip_mlccharge_discount");
      } else {
        charge = (BigDecimal) regChargesBean.get("op_mlccharge");
        discount = (BigDecimal) regChargesBean.get("op_mlccharge_discount");
      }
    } else if (chargeHead.equals("EMREG")) {
      charge = (BigDecimal) regChargesBean.get("mrcharge");
      discount = (BigDecimal) regChargesBean.get("mrcharge_discount");
    } else {
      log.error("Invalid registration charge head: " + chargeHead);
    }

    Map<String, BigDecimal> map = new HashMap<>();
    map.put("charge", charge);
    map.put("discount", discount);
    return map;
  }

  /**
   * Gets the consultation type bean.
   *
   * @param consultationId
   *          the consultation id
   * @return the consultation type bean
   */
  public BasicDynaBean getConsultationTypeBean(int consultationId) {
    return consultationTypesService
        .findByPk(Collections.singletonMap("consultation_type_id", consultationId));
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
    BasicDynaBean conschargebean = doctorService.getConsultationCharges(consultationId, bedType,
        ratePlan);
    if (conschargebean == null) {
      conschargebean = doctorService.getConsultationCharges(consultationId, "GENERAL", "ORG0001");
    }
    return conschargebean;
  }

  /**
   * Recalculates sponsor Amounts.
   *
   * @param visitId
   *          the visit id
   */
  @Transactional(rollbackFor = Exception.class)
  public void recalculateSponsorAmount(String visitId) {
    sponsorService.recalculateSponsorAmount(visitId);
  }

  /**
   * get item types.
   * 
   * @param requestBody
   *          the requestBody
   * @return list of string
   */
  public List<String> getItemTypes(ModelMap requestBody) {
    Map<String, Map<String, List<Object>>> orderedItems =
        (Map<String, Map<String, List<Object>>>) requestBody
        .get("ordered_items");
    List<Object> cancelledItems = (List<Object>) requestBody.get("cancelled_items");
    List<String> itemTypes = new ArrayList<>();
    for (Map.Entry<String, Map<String, List<Object>>> billEntry : orderedItems.entrySet()) {
      for (Map.Entry<String, List<Object>> itemType : billEntry.getValue().entrySet()) {
        for (Object itemObj : itemType.getValue()) {
          Map<String, Object> item = (Map<String, Object>) itemObj;
          String type = (String) item.get("type");
          itemTypes.add(type);
        }
      }
    }
    for (Object cancelItemObj : cancelledItems) {
      Map<String, Object> cancelItem = (Map<String, Object>) cancelItemObj;
      String type = (String) cancelItem.get("type");
      itemTypes.add(type);
    }
    return itemTypes;
  }

  /**
   * get package contents for bulk appointments.
   *
   * @param activityId
   *          the activity id
   * @param activityType
   *          the activity type
   * @param mrNo
   *          the mrNo
   * @param multiVisitPackage
   *          the multiVisitPackage
   * @return map
   */
  public Map<String, Object> getpackagecontentsforbulkappts(String activityId, String activityType,
      String mrNo, boolean multiVisitPackage) {
    List<BasicDynaBean> multiVisitPackageComponentsQuantityDetails = null;
    List list = new ArrayList();
    Map<String, Object> packContentsMap = new HashMap<>();
    if (activityType != null && activityType.equals("Order Sets")) {
      if (multiVisitPackage) {
        multiVisitPackageComponentsQuantityDetails =
            getOrderedPackageItems(mrNo);
      }
      List<BasicDynaBean> packageContentDetails = getPackageComponentDetailsForBulkAppts(
          Integer.parseInt(activityId));
      for (BasicDynaBean bean : packageContentDetails) {
        Map beanMap = new HashMap(bean.getMap());
        String appCat = (String) bean.get("item_type");
        beanMap.put("appt_cat", appCat.equalsIgnoreCase("Service") ? "SNP" : "DOC");
        if (appCat.equalsIgnoreCase("Service")) {
          beanMap.put("dept_id", bean.get("serv_dept_id"));
          beanMap.put("duration", beanMap.get("service_duration"));
          beanMap.put("activity_name", beanMap.get("item_name"));
        }
        if (appCat.equalsIgnoreCase("Department") && (bean.get("consultation_type_id") != null
            || bean.get("consultation_type_id").equals(""))) {
          beanMap.put("activity_id", bean.get("consultation_type_id"));
          beanMap.put("duration", beanMap.get("consultation_type_duration"));
          beanMap.put("activity_name", beanMap.get("consultation_type"));
        }
        list.add(beanMap);
      }
    } else {
      if (activityType != null) {
        Map beanMap = new HashMap();
        if (activityType.equals("Laboratory") || activityType.equals("Radiology")) {
          BasicDynaBean bean = diagnosticTestService.findByKey(activityId);
          if (bean != null) {
            beanMap.put("appt_cat", "DIA");
            beanMap.put("activity_id", bean.get("test_id"));
            beanMap.put("duration", bean.get("test_duration"));
            beanMap.put("activity_name", bean.get("test_name"));
            beanMap.put("pack_ob_id", 1);
            beanMap.put("parent_pack_ob_id", null);
            beanMap.put("conducting_doc_mandatory", bean.get("conducting_doc_mandatory"));
          }
        } else if (activityType.equals("Service")) {
          BasicDynaBean bean = servicesService.findByKey(activityId);
          if (bean != null) {
            beanMap.put("appt_cat", "SNP");
            beanMap.put("activity_id", bean.get("service_id"));
            beanMap.put("duration", bean.get("service_duration"));
            beanMap.put("activity_name", bean.get("service_name"));
            beanMap.put("pack_ob_id", 1);
            beanMap.put("parent_pack_ob_id", null);
          }
        } else if (activityType.equals("Doctor")) {
          BasicDynaBean bean = doctorService.getDoctorById(activityId);
          if (bean != null) {
            beanMap.put("appt_cat", "DOC");
            beanMap.put("activity_id", activityId);
            beanMap.put("duration", null);
            beanMap.put("activity_name", bean.get("doctor_name"));
            beanMap.put("pack_ob_id", 1);
            beanMap.put("parent_pack_ob_id", null);
          }
        }
        list.add(beanMap);
      }
    }
    packContentsMap.put("packComponentDetails", list);
    // packContentsMap.put("multVisitPackComponentQtyDetails"
    // ConversionUtils.listBeanToListMap(multiVisitPackageComponentsQuantityDetails));
    return packContentsMap;
  }

  /**
   * set prescribed date and doctor.
   * 
   * @param orderItems
   *          the orderItems
   * @return map
   */
  public Map<String, List<Object>> setPrescribedDateAndDoctor(
      Map<String, List<Map<String, Object>>> orderItems) {
    Map<String, List<Object>> orderedItemsObject = new HashMap<String, List<Object>>();
    for (Entry<String, List<Map<String, Object>>> billEntry : orderItems.entrySet()) {
      String orderItemType = billEntry.getKey();
      List<Object> orderItemList = new ArrayList<Object>();
      for (Map<String, Object> item : billEntry.getValue()) {
        if (item.get(orderItemType + "_prescribed_date") == null
            || item.get(orderItemType + "_prescribed_date") == "") {
          DateUtil dateUtil = new DateUtil();
          item.put(orderItemType + "_prescribed_date", dateUtil.getCurrentTimestamp());
        }
        if (item.get(orderItemType + "_prescribed_doctor_id") != null
            && item.get(orderItemType + "_prescribed_doctor_id") != "") {
          item.put("prescribed_doctor_id", item.get(orderItemType + "_prescribed_doctor_id"));
        }
        orderItemList.add(item);
      }
      orderedItemsObject.put(orderItemType, orderItemList);
    }
    return orderedItemsObject;
  }

  /**
   * create orders by visit id and items.
   * 
   * @param params
   *          the params
   * @return map
   * @throws ParseException
   *           the ParseException
   * @throws SQLException
   *           the SQLException
   * @throws IOException
   *           the IOException
   */
  public Map<String, Object> createOrdersByVisitIdAndItems(Map<String, Object> params)
          throws ParseException, SQLException, IOException,
          NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    String visitId = (String) params.get("visit_id");
    String mrNo = (String) params.get("mr_no");
    BasicDynaBean visitDetails = null;
    if (visitId != "" && visitId != null) {
      visitDetails = registrationService.getVisitDetails(visitId);
    } else if (mrNo != "" && mrNo != null) {
      visitDetails = registrationService.getLatestActiveVisit(mrNo);
      if (visitDetails != null) {
        visitId = (String) visitDetails.get("patient_id");
      }
    } else {
      throw new ValidationException("exception.parameters.missing");
    }
    if (visitDetails == null) {
      throw new ValidationException("ui.error.fetch.visits.by.patient");
    }
    Map<String, Object> visitDetailsMap = new HashMap<String, Object>(visitDetails.getMap());
    params.put("visit", visitDetailsMap);
    BasicDynaBean bill = billService.getPrimaryOpenBillLaterElseBillNow(visitId);
    if (bill == null) {
      int[] planIds = patientInsurancePlansService.getPlanIds(visitId);
      Boolean allowBillInsurance = null;
      String discountPlanId = null;
      if (planIds != null && planIds.length > 0) {
        allowBillInsurance = true;
        Map searchMap = new HashMap();
        searchMap.put("plan_id", planIds[0]);
        BasicDynaBean planBean = insurancePlanService.findByPk(searchMap);
        if (planBean.get("discount_plan_id") != null) {
          discountPlanId = ((Integer) planBean.get("discount_plan_id")).toString();
        }
      } else {
        allowBillInsurance = false;
      }
      bill = billService.getBean();
      billService.generateBill(visitId, (String) visitDetails.get("visit_type"),
          (String) visitDetails.get("user_name"), bill, "P", allowBillInsurance, BigDecimal.ZERO,
          (String) visitDetails.get("org_id"), discountPlanId);
    }
    String billNo = (String) bill.get("bill_no");
    List<BasicDynaBean> previousPrescDoctorsBean = billChargeService
        .getChargePresDoctorsByVisit(visitId);
    List<String> previousPrescDoctors = new ArrayList<String>();
    for (BasicDynaBean ppd : previousPrescDoctorsBean) {
      String prescribingDocId = (String) ppd.get("prescribing_dr_id");
      if (null != prescribingDocId && !prescribingDocId.isEmpty()) {
        previousPrescDoctors.add(prescribingDocId);
      }
    }
    Map<String, List<Map<String, Object>>> orderItems = 
        (Map<String, List<Map<String, Object>>>) params
        .get("ordered_items");
    Map<String, Object> params1 = new HashMap<String, Object>();
    Map<String, Object> validateParams = new HashMap<String, Object>();
    params1.put(billNo, orderItems);
    validateParams.put("ordered_items", params1);
    validateParams.put("visit", visitDetailsMap);
    validateParams.put("previous_presc_doctors", previousPrescDoctors);
    List<BasicDynaBean> plansList = patientInsurancePlansService
        .getPatientInsuranceDetails(visitId);
    Map<String, List<Object>> orderObjectList = setPrescribedDateAndDoctor(orderItems);
    orderValidator.validatePrescDoctor(validateParams);
    orderItems(orderObjectList, true, billNo, (Integer) visitDetails.get("center_id"), plansList,
        params, visitDetails, false, null);

    return getOrders(visitId, (String) visitDetails.get("visit_type"), null);
  }

  private boolean isPayableAfterDoctorExcluded(boolean isItemCategoryPayable,
      Boolean isDoctorExcluded) {
    return (isItemCategoryPayable && (isDoctorExcluded == null))
        || (!isItemCategoryPayable && (isDoctorExcluded != null) && isDoctorExcluded);
  }

  /**
   * Get item names by entity ids.
   * @param entityIds entity identifiers
   * @return Map of entity id and corresponding name
   */
  public Map<String, String> getItemNamesByEntityIds(List<String> entityIds) {
    List<BasicDynaBean> itemList = orderRepository.getItemNamesByEntityIds(entityIds);
    Map<String, String> nameMap = new HashMap<>();
    if (CollectionUtils.isNotEmpty(itemList)) {
      nameMap = new HashMap<>();
      for (BasicDynaBean bean : itemList) {
        nameMap.put((String) bean.get("entity_id"), (String) bean.get("item_name"));
      }
    }
    return nameMap;
  }
  
  /**
   * Creates the pre auth prescription for ordered items of visit. (This method does not check if
   * the pre authorized amount has been exceeded, this has to be done before invoking this method.)
   *
   * @param visitId
   *          the visit id
   * @return true, if successful
   */
  public boolean createPreAuthPrescriptionForOrderedItemsOfVisit(String visitId,
      Integer consultationId) {
    List<BasicDynaBean> billList = billService.getOpenTpaBills(visitId);
    if (null == billList) {
      return Boolean.TRUE;
    }

    BasicDynaBean insurancePlanDetails = patientInsurancePlansService.getVisitPrimaryPlan(visitId);

    for (BasicDynaBean bill : billList) {
      boolean result = createPreAuthPrescriptionForOrderedItemsOfBill((String) bill.get("bill_no"),
          visitId, insurancePlanDetails, consultationId);
      if (!result) {
        return Boolean.FALSE;
      }
    }
    return Boolean.TRUE;
  }

  private boolean createPreAuthPrescriptionForOrderedItemsOfBill(String billNo, String visitId,
      BasicDynaBean insurancePlanDetails, Integer consutationId) {
    String excludedChargeGroupString = (String) insurancePlanDetails.get("excluded_charge_groups");
    String insuranceCoId = (String) insurancePlanDetails.get("insurance_co_id");
    Set<String> excludedChargeGroups = new HashSet<>();
    if (null != excludedChargeGroupString) {
      excludedChargeGroups = new HashSet<>(
          Arrays.asList(excludedChargeGroupString.split(",")));
    }
    List<BasicDynaBean> billCharges = billChargeService.getBillChargesForPriorAuth(billNo,
        excludedChargeGroups);
    return createPreAuthPrescriptionForOrderedItems(billCharges, visitId, insuranceCoId,
        consutationId);
  }

  protected boolean createPreAuthPrescriptionForOrderedItems(List<BasicDynaBean> billCharges,
      String visitId, String insuranceCoId, Integer consultationId) {
    Map<String, Object> params = new HashMap<>();
    params.put("visit_id", visitId);
    params.put("insurance_co_id", insuranceCoId);
    params.put("consultation_id", consultationId);
    return prescriptionEAuthorization.initiateForOrderedItems(params, billCharges);
  }
  
  /**
   * Delete prior auth for ordered items of visit.
   *
   * @param visitId the visit id
   * @param consultationId the consultation id
   * @return true, if successful
   */
  public boolean deletePriorAuthForOrderedItemsOfVisit(String visitId, Integer consultationId) {
    BasicDynaBean insurancePlanInfo = patientInsurancePlansService.getPatInsuranceInfo(visitId);
    if (insurancePlanInfo == null
        || !"Y".equals(insurancePlanInfo.get("enable_pre_authorized_limit"))) {
      return true;
    }
    List<Integer> preAuthToDelete = eauthService.getPreauthActIdsOfOrderedItems(visitId);
    if (preAuthToDelete == null || preAuthToDelete.isEmpty()) {
      return true;
    }
    String username = (String) sessionService.getSessionAttributes().get("userId");
    boolean success = eauthService.deleteEAuthOfOrderedItem(preAuthToDelete, username);
    success = success && billChargeService.clearPreauthActIdOfBillChargesForVisit(visitId);
    return success;
  }

  /**
   * This method will initiate the preauth check which will consider even the prescriptions of the
   * visit along with the orders. The initiate function will further create the preauth
   * prescriptions of orders placed and prescribed items if the preauth limit is crossed.
   *
   * @param visitId
   *          the visit id
   */
  public void initiatePreAuthLimitCheck(String visitId) {
    List<BasicDynaBean> consultationList = doctorConsultationService
        .listVisitConsultations(visitId);

    for (BasicDynaBean consultation : consultationList) {
      int consultationId = (int) consultation.get("consultation_id");
      Map<String, Object> params = new HashMap<>();
      params.put("patient_id", visitId);
      params.put("consultation_id", consultationId);

      prescriptionEAuthorization.initiate(params, new HashMap<String, Object>());
    }
  }

  /**
   * Marks patient package as discontinued while updating the remark.
   *
   * @param patientPackageId the patient package id
   * @param discontinueRemark the discontinue remark
   */
  public void discontinuePackage(Integer patientPackageId, String discontinueRemark) {
    this.packageOrderItemService.discontinuePackage(patientPackageId, discontinueRemark);
  }

  /**
   * Gets Customized Item charge for pre-filling.
   * @param orderItem The orderItem
   * @param packageContentId The package content id for added itemId is part of
   * @param bedType The bedType for which to fetch the charge
   * @param orgId The orgId for which which to fetch the charge
   * @param itemId The itemId for which to fetch the charge
   * @param chargeHead The chargeHead to get Operation charge
   * @param type The item type
   * @return activityCharge
   */
  private String getCustomizedItemCharge(OrderItemService orderItem, Integer packageContentId,
                                         String bedType, String orgId, String itemId,
                                         String chargeHead, String type) {
    if (null != packageContentId) {
      PackageContentCharges packageChargeBean = this.packageContentChargesService
          .findPackageContentActivityChargesByPackageContentId(packageContentId, bedType, orgId);
      return String.valueOf(packageChargeBean.getCharge());
    }
    List<BasicDynaBean> bedCharges = orderItem.getAllBedTypeMasterChargesBean(itemId, orgId);
    String activityCharge = "";
    for (BasicDynaBean bedCharge : bedCharges) {
      if ("Other Charge".equals(type)) {
        activityCharge = String.valueOf(bedCharge.get("charge"));
      } else if (((String) bedCharge.get("bed_type")).equals(bedType)) {
        if ("Operation".equals(type)) {
          activityCharge = getOperationChargeFromChargeHead(chargeHead, bedCharge);
        } else {
          activityCharge = String.valueOf(bedCharge.get("charge"));
        }
      }
    }
    return activityCharge;
  }

}
