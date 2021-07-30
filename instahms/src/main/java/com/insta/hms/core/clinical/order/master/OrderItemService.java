package com.insta.hms.core.clinical.order.master;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeClaimTaxService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillChargeTransactionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.DiscountService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.eauthorization.EAuthPrescriptionActivitiesRepository;
import com.insta.hms.core.clinical.eauthorization.EAuthorizationService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.preauth.PreAuthItemsService;
import com.insta.hms.core.clinical.prescriptions.PatientMedicinePrescriptionsRepository;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.extension.clinical.ivf.IVFCycleService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class OrderItemService.
 */
public abstract class OrderItemService {

  /** The repository. */
  OrderItemRepository repository;

  OrgDetailsRepository orgDetailsRepository;
  
  /** The prefix. */
  private String prefix;

  /** The prefix id. */
  private String prefixId;

  /** The Constant MR_NO. */
  private static final String MR_NO = "mr_no";

  /** The Constant OPERATION_REF. */
  private static final String OPERATION_REF = "operation_ref";

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

  /** The Constant MILLISEC_IN_SEC. */
  private static final Integer MILLISEC_IN_SEC = 1000;

  /** The Constant SEC_IN_MIN. */
  private static final Integer SEC_IN_MIN = 60;

  /** The Constant MIN_IN_HOUR. */
  private static final Integer MIN_IN_HOUR = 60;

  /** The Constant HOUR_IN_DAY. */
  private static final Integer HOUR_IN_DAY = 24;

  /** The Constant ITEMS_MAP. */
  protected static final String ITEMS_MAP = "items_map";

  /** The Constant ORDER_BASE_PATH. */
  protected static final String ORDER_BASE_PATH = "com.insta.hms.core.clinical.order";

  /** The discount plans service. */
  @LazyAutowired
  private DiscountService discountPlansService;

  /** The bill charge service. */
  @LazyAutowired
  private BillChargeService billChargeService;

  /** The bill charge tax service. */
  @LazyAutowired
  private BillChargeTaxService billChargeTaxService;

  /** The bill activity charge service. */
  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  /** The bill charge claim service. */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /** The bill charge claim tax service. */
  @LazyAutowired
  private BillChargeClaimTaxService billChargeClaimTaxService;

  /** The bill service. */
  @LazyAutowired
  private BillService billService;

  /** The patient medicine prescriptions repository. */
  @LazyAutowired
  private PatientMedicinePrescriptionsRepository patientMedicinePrescriptionsRepository;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  /** The security service. */
  @LazyAutowired
  private SecurityService securityService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The ivf cycle service. */
  @LazyAutowired
  private IVFCycleService ivfCycleService;

  /** The pre auth items service. */
  @LazyAutowired
  private PreAuthItemsService preAuthItemsService;
  
  /** The order service. */
  @LazyAutowired
  private OrderService orderService;
  
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;
  
  /** Module activated service. */
  @LazyAutowired
  private ModulesActivatedService modulesActivatedService;
  
  /** Pending prescription service. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;
  
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The bill charge transaction. */
  @LazyAutowired
  private BillChargeTransactionService billChargeTransactionService;
  
  @LazyAutowired
  private CenterPreferencesService centerPreferencesService;

  /** The e auth prescription activities repository. */
  @LazyAutowired
  private EAuthPrescriptionActivitiesRepository eauthPrescActivitiesRepo;
  
  /** The pat pack content consumed repo. */
  @LazyAutowired
  private PatientPackageContentConsumedService patPackContentConsumedSer;

  @LazyAutowired
  private EAuthorizationService eauthService;

  
  /** The formatter. */
  public static DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  /**
   * Instantiates a new order item service.
   *
   * @param rep
   *          the repository
   * @param prefix
   *          the prefix
   * @param prefixId
   *          the prefix id
   */
  public OrderItemService(OrderItemRepository rep, String prefix, String prefixId) {
    this.repository = rep;
    this.prefix = prefix;
    this.prefixId = prefixId;
  }

  /**
   * Instantiates a new Order item service.
   *
   * @param rep                  the rep
   * @param prefix               the prefix
   * @param prefixId             the prefix id
   * @param orgDetailsRepository the org details repository
   */
  public OrderItemService(OrderItemRepository rep, String prefix, String prefixId,
      OrgDetailsRepository orgDetailsRepository) {
    this.repository = rep;
    this.prefix = prefix;
    this.prefixId = prefixId;
    this.orgDetailsRepository = orgDetailsRepository;
  }

  /**
   * Gets the main visit id.
   *
   * @param visitDetails
   *          the visit details
   * @return the main visit id
   */
  protected String getMainVisitId(BasicDynaBean visitDetails) {
    if (visitDetails != null) {
      return (String) visitDetails.get("main_visit_id");
    } else {
      return null;
    }
  }

  /**
   * Set the appointmentId.
   *
   * @param orderBean
   *          the order bean
   * @param appointmentId
   *          the appointment id
   */
  protected void setAppointmentId(BasicDynaBean orderBean, Integer appointmentId) {
    if (appointmentId > 0) {
      orderBean.set("appointment_id", appointmentId);
    } else {
      orderBean.set("appointment_id", null);
    }
  }

  /**
   * Set the Package Reference if the multiPackageItem exist.
   *
   * @param orderBean
   *          the order bean
   * @param isMultiVisitPackItem
   *          the is multi visit pack item
   * @param packageRef
   *          the package ref
   */
  protected void setPackageRef(BasicDynaBean orderBean, boolean isMultiVisitPackItem,
      Integer packageRef) {
    if (isMultiVisitPackItem) {
      orderBean.set("package_ref", packageRef);
    }
  }

  /**
   * Set the bean Value, pass the bean, columnName & the value.
   *
   * @param orderBean
   *          the order bean
   * @param columnName
   *          the column name
   * @param value
   *          the value
   */
  protected void setBeanValue(BasicDynaBean orderBean, String columnName, Object value) {
    orderBean.set(columnName, value);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return repository.getBean();
  }

  /**
   * Setting up the common Properties of Ordering Services.
   *
   * @param orderBean
   *          the order bean
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param isMultiVisitPackItem
   *          the is multi visit pack item
   * @param orderedItemList
   *          the ordered item list
   */
  @SuppressWarnings("rawtypes")
  protected void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {
    setBeanValue(orderBean, MR_NO, (String) headerInformation.get(MR_NO));
    setBeanValue(orderBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(orderBean, "user_name", username);
  }

  /**
   * Setting up the common Properties of Ordering Services.
   *
   * @param orderBean
   *          the order bean
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param isMultiVisitPackItem
   *          the is multi visit pack item
   * @param orderedItemList
   *          the ordered item list
   * @param operationBean
   *          the operation bean
   * @return the basic dyna bean
   * @throws ParseException
   *           the parse exception
   */
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) throws ParseException {
    setBeanValue(orderBean, MR_NO, (String) headerInformation.get(MR_NO));
    setBeanValue(orderBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(orderBean, "user_name", username);
    return orderBean;
  }

  /**
   * Sets the operation ref.
   *
   * @param orderBean
   *          the order bean
   * @param operationBean
   *          the operation bean
   * @return true, if successful
   */
  protected boolean setOperationRef(BasicDynaBean orderBean, BasicDynaBean operationBean) {
    if (operationBean != null) {
      setBeanValue(orderBean, OPERATION_REF, (Integer) operationBean.get("prescribed_id"));
      return true;
    }

    setBeanValue(orderBean, OPERATION_REF, null);
    return false;
  }

  /**
   * Set the orderAttributes into billChargeBean.
   *
   * @param billChargeBean
   *          the bill charge bean
   * @param chargeId
   *          the charge id
   * @param billNo
   *          the bill no
   * @param userName
   *          the user name
   * @param remarks
   *          the remarks
   * @param presDrId
   *          the pres dr id
   * @param postedDate
   *          the posted date
   */
  protected void setOrderAttributes(BasicDynaBean billChargeBean, String chargeId, String billNo,
      String userName, String remarks, String presDrId, Timestamp postedDate) {
    billChargeBean.set("charge_id", chargeId);
    billChargeBean.set("bill_no", billNo);
    billChargeBean.set("username", userName);
    billChargeBean.set("user_remarks", remarks);
    billChargeBean.set("prescribing_dr_id", presDrId);
    billChargeBean.set("posted_date", postedDate);
    billChargeBean.set("mod_time", DateUtil.getCurrentTimestamp());
  }

  /**
   * Get code details (code type and code).
   *
   * @param itemId the item id
   * @param orgId  the org id
   * @return the map
   */
  public Map<String, Object> getCodeDetails(String itemId, String orgId) {
    if (orgDetailsRepository == null) {
      return Collections.emptyMap();
    }
    Map<String, Object> resultMap = new HashMap<>();
    BasicDynaBean codeDetails =  orgDetailsRepository.getCodeDetails( itemId,  orgId);
    resultMap.put("code_type", codeDetails.get("code_type"));
    resultMap.put("ct_code", codeDetails.get("item_code"));
    return resultMap;
  }

  /**
   * Save the list of BasicDynaBean Orders called by the respective Order.
   *
   * @param orderBean
   *          the order bean
   */
  public void insertOrderBeanList(List<BasicDynaBean> orderBean) {
    for (int i = 0; i < orderBean.size(); i++) {
      repository.insert(orderBean.get(i));
    }
  }

  /**
   * To convert requestParams to respective orderItem List BasicDynaBean  Returns,
   * all auth information, conducting doctor Information though serviceAuths Map.
   *
   * @param requestParams
   *          the request params
   * @param prefix
   *          the prefix
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param orderedItemAuths
   *          the ordered item auths
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @return the list
   */
  protected List<BasicDynaBean> toBean(Map<String, List<Object>> requestParams, String prefix,
      BasicDynaBean headerInformation, String username, Map<String, List<Object>> orderedItemAuths,
      String[] preAuthIds, Integer[] preAuthModeIds) {

    List<BasicDynaBean> orderBeanList = new ArrayList<BasicDynaBean>();
    String priAuthIdKey = prefix + PRIOR_AUTH_ID;
    String priAuthModeIdKey = prefix + PRIOR_AUTH_MODE_ID;
    String secAuthIdkey = prefix + SEC_PRIOR_AUTH_ID;
    String secAuthModeIdKey = prefix + SEC_PRIOR_AUTH_MODE_ID;
    String payeeDoctorIdKey = prefix + PAYEE_DOCTOR_ID;
    String multiVisitIdKey = prefix + MULTI_VISIT_PACKAGE;

    Object[] orderedItemList = requestParams.get(prefix).toArray();

    List errorList = new ArrayList();
    if (orderedItemList != null) {
      for (int i = 0; i < orderedItemList.length; i++) {

        Object priAuthId = ((Map<String, Object>) orderedItemList[i]).get(priAuthIdKey);
        if (priAuthId == null || ((String) (priAuthId)).equals("")) {
          if (preAuthIds != null && preAuthIds.length != 0 && preAuthIds[0] != null) {
            orderedItemAuths.get("newPreAuths").add(preAuthIds[0]);
          } else {
            orderedItemAuths.get("newPreAuths").add(null);
          }
        } else {
          orderedItemAuths.get("newPreAuths").add((String) (priAuthId));
        }

        Object priAuthModeId = ((Map<String, Object>) orderedItemList[i]).get(priAuthModeIdKey);
        if (priAuthModeId == null || ((Integer) priAuthModeId) == 0) {
          if (preAuthModeIds != null && preAuthModeIds.length != 0 && preAuthModeIds[0] != null
              && preAuthModeIds[0] != 0) {
            orderedItemAuths.get("newPreAuthModesList").add(preAuthModeIds[0]);
          } else {
            orderedItemAuths.get("newPreAuthModesList").add(1);
          }
        } else {
          orderedItemAuths.get("newPreAuthModesList").add(priAuthModeId);
        }

        Object secAuthId = ((Map<String, Object>) orderedItemList[i]).get(secAuthIdkey);
        if (secAuthId == null || ((String) secAuthId).equals("")) {
          if (preAuthIds != null && preAuthIds.length > 1 && preAuthIds[1] != null) {
            orderedItemAuths.get("secNewPreAuths").add(preAuthIds[1]);
          } else {
            orderedItemAuths.get("secNewPreAuths").add(null);
          }
        } else {
          orderedItemAuths.get("secNewPreAuths").add((String) secAuthId);
        }

        Object secAuthModeId = ((Map<String, Object>) orderedItemList[i]).get(secAuthModeIdKey);
        if (secAuthModeId == null || (Integer) secAuthModeId == 0) {
          if (preAuthModeIds != null && preAuthModeIds.length > 1 && preAuthModeIds[1] != null
              && preAuthModeIds[1] != 0) {
            orderedItemAuths.get("secNewPreAuthModesList").add(preAuthModeIds[1]);
          } else {
            orderedItemAuths.get("secNewPreAuthModesList").add(1);
          }
        } else {
          orderedItemAuths.get("secNewPreAuthModesList").add(secAuthModeId);
        }

        BasicDynaBean orderedItemBean = repository.getBean();
        orderedItemAuths.get("conductingDoctorList")
            .add((String) ((Map<String, Object>) orderedItemList[i]).get(payeeDoctorIdKey));

        ConversionUtils.copyJsonToDynaBeanPrefixed((Map) orderedItemList[i], orderedItemBean,
            errorList, prefix + "_");

        Boolean isMultivisitPackage = false;
        if (((Map<String, Object>) orderedItemList[i]) != null) {
          isMultivisitPackage = new Boolean(
              (Boolean) ((Map<String, Object>) orderedItemList[i]).get(multiVisitIdKey));
          orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);
        }
        
        Object docPrescIdObject = ((Map<String, Object>) orderedItemList[i])
            .get(prefix + "_doc_presc_id");
        if (null != docPrescIdObject && !StringUtils.EMPTY.equals(docPrescIdObject)) {
          Integer docPrescId = Integer.valueOf(docPrescIdObject.toString());
          orderedItemBean.set("doc_presc_id", docPrescId);
        }
        
        setHeaderProperties(orderedItemBean, headerInformation, username, isMultivisitPackage,
            (Map) orderedItemList[i]);
        orderBeanList.add(orderedItemBean);
      }
    }

    return orderBeanList;
  }

  /**
   * Gets the order bean.
   *
   * @param requestParams
   *          the request params
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param orderedItemAuths
   *          the ordered item auths
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @return the order bean
   */
  public List<BasicDynaBean> getOrderBean(Map<String, List<Object>> requestParams,
      BasicDynaBean headerInformation, String username, Map<String, List<Object>> orderedItemAuths,
      String[] preAuthIds, Integer[] preAuthModeIds) {

    return toBean(requestParams, prefix, headerInformation, username, orderedItemAuths, preAuthIds,
        preAuthModeIds);
  }

  /**
   * To convert requestParams to respective orderItem List BasicDynaBean Returns
   * all Auth information, conducting doctor Information though Map String,
   * List Object serviceAuths.
   *
   * @param item
   *          the item
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param orderedItemAuths
   *          the ordered item auths
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param operationBean
   *          the operation bean
   * @return the basic dyna bean
   * @throws ParseException
   *           the parse exception
   */
  public abstract BasicDynaBean toItemBean(Map<String, Object> item,
      BasicDynaBean headerInformation, String username, Map<String, List<Object>> orderedItemAuths,
      String[] preAuthIds, Integer[] preAuthModeIds, BasicDynaBean operationBean)
      throws ParseException;

  /**
   * Gets the order item bean.
   *
   * @param item
   *          the item
   * @param authMap
   *          the auth map
   * @param headerInformation
   *          the header information
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param username
   *          the username
   * @param operationBean
   *          the operation bean
   * @return the order item bean
   * @throws ParseException
   *           the parse exception
   */
  public BasicDynaBean getOrderItemBean(Map<String, Object> item, Map<String, List<Object>> authMap,
      BasicDynaBean headerInformation, String[] preAuthIds, Integer[] preAuthModeIds,
      String username, BasicDynaBean operationBean) throws ParseException {
    return toItemBean(item, headerInformation, username, authMap, preAuthIds, preAuthModeIds,
        operationBean);
  }

  /**
   * Create dummy object to set all parameter from request Params.
   *
   * @return the order auth object
   */
  public Map<String, List<Object>> getOrderAuthObject() {
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
   * Abstract function : Every Item Type will have its own Master Charges. Return
   * the Master Charge Bean Data.
   *
   * @param prefixId
   *          the prefix id
   * @param bedType
   *          the bed type
   * @param ratePlanId
   *          the rate plan id
   * @param centerId
   *          the center id
   * @return the master charges bean
   */
  public abstract BasicDynaBean getMasterChargesBean(Object prefixId, String bedType,
      String ratePlanId, Integer centerId);

  /**
   * Abstract Function Returns the ChargesList (Ordered Item Charges plus its tax
   * Charges).
   *
   * @param itemType
   *          the item type
   * @param quantity
   *          the quantity
   * @param isInsurance
   *          the is insurance
   * @param condDoctorId
   *          the cond doctor id
   * @param otherParams
   *          the other params
   * @return the charges list
   * @throws ParseException
   *           the parse exception
   */
  protected abstract List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams)
      throws ParseException;

  /**
   * Gets the charges.
   *
   * @param paramMap
   *          the param map
   * @return the charges
   * @throws ParseException
   *           the parse exception
   */
  public abstract List<BasicDynaBean> getCharges(Map<String, Object> paramMap)
      throws ParseException;
 
  
  /**
   * Gets the cash charges.
   *
   * @param paramMap
   *          the param map
   * @return the charges
   * @throws ParseException
   *           the parse exception
   */
  public BigDecimal getCashCharge(Map<String, Object> paramMap) {
    String id = (String) paramMap.get("id");
    Integer centerId = (Integer) paramMap.get("center_id");
    String defaultRatePlan = (String) centerPreferencesService.getCenterPreferences(centerId)
        .get("pref_rate_plan_for_non_insured_bill");
    String bedType = (String) paramMap.get("bed_type");

    if (StringUtils.isEmpty(defaultRatePlan ) || StringUtils.isEmpty(bedType)) {
      return null;
    }
    
    BasicDynaBean cashChargeBean = getMasterChargesBean(id, bedType, defaultRatePlan, centerId);
    return (BigDecimal) cashChargeBean.get("charge");
  }

  /**
   * Insert order bill charges.
   *
   * @param chargesList
   *          the charges list
   * @param activityCode
   *          the activity code
   * @param activityConducted
   *          the activity conducted
   * @param conductedOn
   *          the conducted on
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param bill
   *          the bill
   * @param orderBean
   *          the order bean
   * @param planIds
   *          the plan ids
   * @param prescId
   *          the presc id
   * @param presDrId
   *          the pres dr id
   * @param postedDate
   *          the posted date
   */
  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, String bedType) {
    String remarks = ( activityCode == "DIE" )
        ? (String) orderBean.get("special_instructions") 
            : (String) orderBean.get("remarks");
    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate,
        remarks , bedType);
  }
  
  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate) {
    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate,
        orderBean != null ? (String) orderBean.get("remarks") : null, (String)null);
  }

  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, List<BasicDynaBean> transactionCharges) {
    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate,
        (String) orderBean.get("remarks"), transactionCharges);
  }
  
  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, String remarks,
      Map<String, Object> orderItemDetails) {
    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate, remarks,
        orderItemDetails, (String)null);
  }

  
  /**
   * Insert order bill charges.
   *
   * @param chargesList
   *          the charges list
   * @param activityCode
   *          the activity code
   * @param activityConducted
   *          the activity conducted
   * @param conductedOn
   *          the conducted on
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param bill
   *          the bill
   * @param orderBean
   *          the order bean
   * @param planIds
   *          the plan ids
   * @param prescId
   *          the presc id
   * @param presDrId
   *          the pres dr id
   * @param postedDate
   *          the posted date
   * @param remarks
   *          the remarks
   */
  
  

  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, String remarks,
      Map<String, Object> orderItemDetails, String bedType) {

    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    List<BasicDynaBean> cashCharges = new ArrayList<BasicDynaBean>();

    for (BasicDynaBean charge : chargesList) {
      Map paramMap = new HashMap<String, Object>();
      paramMap.put("id", charge.get("act_description_id"));
      paramMap.put("bed_type", bedType);
      paramMap.put("center_id", centerId);
      String actDescriptionId = (String) charge.get("act_description_id");
      String ratePlanId = (String) bill.get("bill_rate_plan_id");
      BasicDynaBean chargeTransactionBean = billChargeTransactionService.getBean();
      chargeTransactionBean.set("cash_rate", getCashCharge(paramMap));
      cashCharges.add(chargeTransactionBean);
    }

    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate, remarks,
        orderItemDetails, cashCharges);
  }
  
  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, String remarks,
      Map<String, Object> orderItemDetails,
      List<BasicDynaBean> chargeTransactions) {

    String billNo = (String) bill.get("bill_no");
    // String username = (String) bill.get("username");
    String username = (String) sessionService.getSessionAttributes().get("userId");
    Integer commonOrderId = orderBean != null ? (Integer) orderBean.get("common_order_id")
        : (chargesList.get(0) != null && (chargesList.get(0)).get("order_number") != null
        ? (Integer) (chargesList.get(0)).get("order_number") : null);
    Map<String, String> preAuthStatusMap = new HashMap<>();
    String preAuthActStatus = null;

    Integer billDiscountPlanId = 0;
    if (bill.get("discount_category_id") != null) {
      billDiscountPlanId = (Integer) bill.get("discount_category_id");
    }

    int opIndex = 0;
    int panelIndex = 0;
    int packIndex = 0;
    int bedIndex = 0;
    int mainPackIndex = 0;
    int chargeListIndex = 0;
    int panelId = 0;
    Map<Integer, String> packageNameMap = new HashMap<Integer, String>();
    Map<String, String> panelNameMap = new HashMap<String, String>();
    Map<String, String> operationMap = new HashMap<String, String>();
    for (BasicDynaBean charge : chargesList) {
      if (postedDate.getTime() < ((java.sql.Timestamp) bill.get("open_date")).getTime()) {
        postedDate = (java.sql.Timestamp) bill.get("open_date");
      }
      
      String mainChargeId = charge.get("charge_id") != null
          ? (String) charge.get("charge_id") : null;
      String nextPreId = null;
      if (mainChargeId != null
          && !mainChargeId.startsWith("_")
          && "PKG".equals(charge.get("charge_group"))) {
        nextPreId = mainChargeId;
      } else {
        nextPreId = billChargeService.getNextPrefixedId();
      }
      setOrderAttributes(charge, nextPreId, billNo, username, remarks,
          presDrId, postedDate);
      if ((Boolean) charge.get("allow_discount")) {
        discountPlansService.applyDiscountRule(charge, billDiscountPlanId,
            (String) bill.get("visit_type"));
      }
      if (null != charge.get("op_id") && "TCOPE".equals(charge.get("charge_head"))) {
        operationMap.put(String.valueOf(charge.get("op_id"))
            + Integer.toString(opIndex), nextPreId);
        opIndex++;
      }
      if (null != charge.get("act_description_id") && "BBED".equals(charge.get("charge_head"))) {
        operationMap.put(String.valueOf(charge.get("act_description_id"))
            + Integer.toString(bedIndex), nextPreId);
        bedIndex++;
      }
      if ("PKGPKG".equals(charge.get("charge_head"))) {
        packageNameMap.put(packIndex, (String) charge.get("act_description"));
        packIndex++;
        mainPackIndex = chargeListIndex;
      }
      chargeListIndex++;
      if (null != charge.get("panel_id") && !operationMap.containsKey(
          String.valueOf(charge.get("panel_id")) + Integer.toString(panelIndex))) {
        if (panelId != (int) charge.get("panel_id")) {
          panelIndex  = 0; 
        }
        operationMap.put(String.valueOf(charge.get("panel_id"))
            + Integer.toString(panelIndex), nextPreId);
        panelNameMap.put(String.valueOf(charge.get("panel_id"))
            + Integer.toString(panelIndex), (String) charge.get("act_description"));
        panelIndex++;
        panelId = (int) charge.get("panel_id");
      }
      charge.set("bill_no", billNo);
      charge.set("order_number", commonOrderId);
      charge.set("conducted_datetime", conductedOn);
      if (preAuthIds != null) {
        charge.set("prior_auth_id", preAuthIds[0]);
      } else {
        charge.set("prior_auth_id", null);
      }
      
      Integer preAuthItemId = null;
      Integer preAuthModeId = null;
      if (preAuthModeIds != null) {
        preAuthModeId = preAuthModeIds[0];
      }
      if (charge.get("act_remarks") == null) {
        charge.set("act_remarks", null);
      }
      if (orderItemDetails != null) {
            
        if (orderItemDetails.containsKey("prior_auth_item_id")) {
          preAuthItemId = (Integer) orderItemDetails.get("prior_auth_item_id");
        } else if (orderItemDetails.containsKey("tests_prior_auth_item_id")) {
          preAuthItemId = (Integer) orderItemDetails.get("tests_prior_auth_item_id");          
        } else if (orderItemDetails.containsKey("services_prior_auth_item_id")) {
          preAuthItemId = (Integer) orderItemDetails.get("services_prior_auth_item_id");
        } else if (orderItemDetails.containsKey("doctors_prior_auth_item_id")) {          
          preAuthItemId = (Integer) orderItemDetails.get("doctors_prior_auth_item_id");
        }    
        if (null == preAuthModeId) {
          if (orderItemDetails.containsKey("prior_auth_mode_id")) {
            preAuthModeId = (Integer) orderItemDetails.get("prior_auth_mode_id");
          } else if (orderItemDetails.containsKey("tests_prior_auth_mode_id")) {
            preAuthModeId = (Integer) orderItemDetails.get("tests_prior_auth_mode_id");          
          } else if (orderItemDetails.containsKey("services_prior_auth_mode_id")) {
            preAuthModeId = (Integer) orderItemDetails.get("services_prior_auth_mode_id");
          } else if (orderItemDetails.containsKey("doctors_prior_auth_mode_id")) {
            preAuthModeId = (Integer) orderItemDetails.get("doctors_prior_auth_mode_id");
          }    
        }
        
        if (BooleanUtils.isTrue((Boolean) orderItemDetails.get("prescriptionOrder"))
              && preAuthItemId == null
              && MapUtils.getInteger(orderItemDetails, "doc_presc_id") != null) {
          preAuthItemId = preAuthItemsService
                .getPriorAuthItemIdFromPrescId((Integer) orderItemDetails.get("doc_presc_id"));
        }
        preAuthActStatus = orderItemDetails.containsKey("services_preauth_act_status")
            ? (String) orderItemDetails.get("services_preauth_act_status")
            : (String) orderItemDetails.get("preauth_act_status");

      }
      charge.set("prior_auth_mode_id", preAuthModeId);
      if (preAuthItemId != null) {
        charge.set("preauth_act_id", preAuthItemId);
        boolean shouldLockClaim = "Y".equals(genericPreferencesService.getAllPreferences()
            .get("set_preauth_approved_amt_as_claim_amt")) || "D".equals(preAuthActStatus);
        charge.set("is_claim_locked", shouldLockClaim);
        preAuthStatusMap.put((String)charge.get("charge_id"), preAuthActStatus);
      }
      if (charge.get("op_id") == null) {
        charge.set("op_id", null);
      }
      // Explicitly set because the charges are inserted through batch
      // insert.
      if (charge.get("surgery_anesthesia_details_id") == null) {
        charge.set("surgery_anesthesia_details_id", null);
      }
      // Explicitly set because the charges are inserted through batch insert.
      charge.set("payee_doctor_id", charge.get("payee_doctor_id"));
      charge.set("activity_conducted", activityConducted);
    }
    Boolean firstPanel = true;
    int operIndex = 0;
    int panIndex = 0;
    int pacIndex = 0;
    int beddIndex = 0;
    int runningIndex = 0;
    int panelPackId = 0;
    int chargeListSize = chargesList.size();
    for (BasicDynaBean charge : chargesList) {
      if ("OPE".equals(charge.get("charge_group"))) {
        charge.set("charge_ref", chargesList.get(0).get("charge_id"));
        charge.set("hasactivity", true);
      }
      if (runningIndex == mainPackIndex) {
        if (charge.get("charge_ref") == null) {
          charge.set("charge_ref", null);
        }
        if (charge.get("act_remarks") == null) {
          charge.set("act_remarks", "");
        }
        continue;
      }
      charge.set("charge_ref", chargesList.get(mainPackIndex).get("charge_id"));
      String chargeHead = (String) charge.get("charge_head");
      if (null != charge.get("op_id")
          && !"TCOPE".equals(charge.get("charge_head"))
          && null != operationMap.get(String.valueOf(charge.get("op_id"))
              + Integer.toString(operIndex))) {
        charge.set("charge_ref", operationMap.get(String.valueOf(charge.get("op_id"))
            + Integer.toString(operIndex)));
        if (runningIndex + 1 < chargeListSize
            && null == chargesList.get(runningIndex + 1).get("op_id")) {
          operIndex++;
        }
      }
      String bedHead = "";
      if (StringUtils.isNotBlank(chargeHead)) {
        bedHead = chargeHead.substring(chargeHead.length() - 3);
      }

      if (null != charge.get("act_description_id")
          && bedHead.equals("BED")
          && !"BBED".equals(charge.get("charge_head"))
          && null != operationMap.get(String.valueOf(charge.get("act_description_id"))
              + Integer.toString(beddIndex))) {
        charge.set("charge_ref", operationMap.get(String.valueOf(charge.get("act_description_id"))
            + Integer.toString(beddIndex)));
        if (runningIndex + 1 < chargeListSize
            && null == chargesList.get(runningIndex + 1).get("act_description_id")) {
          beddIndex++;
        }
      }
      if ("PKG".equals(charge.get("charge_group"))) {
        charge.set("act_remarks", packageNameMap.get(pacIndex));
      } else {
        charge.set("act_remarks", "");
      }
      if (runningIndex + 1 < chargeListSize
          && !"PKG".equals(chargesList.get(runningIndex + 1).get("charge_group"))) {
        pacIndex++;
      }
      //Storing first panel id with main charge ref
      if (null != charge.get("panel_id")) {
        if (panelPackId != (int) charge.get("panel_id")) {
          panIndex  = 0;
          firstPanel = true;
        }
        if (!firstPanel) {
          charge.set("charge_ref", operationMap.get(String.valueOf(charge.get("panel_id"))
              + Integer.toString(panIndex)));
        }
        panelPackId = (int) charge.get("panel_id");
        firstPanel = false;
        if (runningIndex + 1 < chargeListSize
            && null == chargesList.get(runningIndex + 1).get("panel_id")) {
          panIndex++;
        }
      } else {
        firstPanel = true;
        charge.set("panel_id", null);
      }
      charge.set("hasactivity", true);
      if (remarks != null && !charge.get("charge_head").equals("ANATOPE")) {
        charge.set("act_remarks", chargesList.get(mainPackIndex).get("act_remarks"));
      }
      runningIndex++;
    }

    BasicDynaBean mainCharge = chargesList.get(mainPackIndex);
    if (prescId != 0) {
      mainCharge.set("hasactivity", true);
    } else {
      mainCharge.set("hasactivity", false);
    }
    mainCharge.set("charge_ref", null);
    List<BasicDynaBean> activityChargeList = billActivityChargeService
        .getActivityChargeList(chargesList, prescId, activityCode, activityConducted);

    billChargeService.batchInsert(chargesList);
    billChargeTaxService.batchInsert(chargesList);
    billActivityChargeService.batchInsert(activityChargeList);
    preAuthItemsService.updatePreAuthItemQuantity(chargesList, false,preAuthActStatus);
    if ((Boolean) bill.get("is_tpa")) {
      String patientId = (String) bill.get("visit_id");
      billChargeClaimService.insertBillChargeClaims(chargesList, planIds, patientId, bill,
          preAuthIds, preAuthModeIds, preAuthStatusMap);
    }

    if (chargeTransactions == null) {
      chargeTransactions = new ArrayList<>();
      for (int i = 0;i < chargesList.size();i++) {
        chargeTransactions.add(billChargeTransactionService.getBean());
      }
    }
    
    for (int i = 0; i < chargesList.size(); i++) {
      BasicDynaBean chargeBean = chargesList.get(i);
      chargeTransactions.get(i).set("bill_charge_id", chargeBean.get("charge_id"));
      if ("SNP".equals(chargeBean.get("charge_group"))) {
        String actDescriptionId = (String) chargeBean.get("act_description_id");
        String ratePlanId = (String) bill.get("bill_rate_plan_id");
        
        billChargeTransactionService.populateServiceCode(chargeTransactions.get(i),
            actDescriptionId, ratePlanId);
      }
    
    }
    billChargeTransactionService.insert(chargeTransactions);      
  }

  /**
   * Insert order bill charges.
   *
   * @param chargesList
   *          the charges list
   * @param activityCode
   *          the activity code
   * @param activityConducted
   *          the activity conducted
   * @param conductedOn
   *          the conducted on
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param bill
   *          the bill
   * @param orderBean
   *          the order bean
   * @param planIds
   *          the plan ids
   * @param prescId
   *          the presc id
   * @param presDrId
   *          the pres dr id
   * @param postedDate
   *          the posted date
   * @param remarks
   *          the remarks
   */
  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, String remarks, String bedType) {
    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate, remarks, null,
        bedType);
  }

  protected void insertOrderBillCharges(List<BasicDynaBean> chargesList, String activityCode,
      String activityConducted, Timestamp conductedOn, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean bill, BasicDynaBean orderBean, int[] planIds,
      int prescId, String presDrId, Timestamp postedDate, String remarks,
      List<BasicDynaBean> transactionCharges) {
    insertOrderBillCharges(chargesList, activityCode, activityConducted, conductedOn, preAuthIds,
        preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate, remarks, null,
        transactionCharges);
  }

  /**
   * This Function performs the data collection and insertion into
   * table(bill_charge, bill_activity_charge, bill_chare_claim).
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
   * @return the list
   * @throws ParseException
   *           the parse exception
   * @throws SecurityException
   *           the security exception
   */
  @Transactional(rollbackFor = Exception.class)
  public List<BasicDynaBean> insertOrderCharges(boolean chargeable, BasicDynaBean headerInformation,
      BasicDynaBean orderBean, BasicDynaBean bill, String[] preAuthIds, Integer[] preAuthModeIds,
      int[] planIds, String condDoctorId, String activityCode, Integer centerId,
      Boolean isMultivisitPackage, Map<String, Object> orderItemDetails) throws ParseException {

    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    Integer priorAuthItemId = null;
    Long patPendingPrescId = null;
    String preAuthActStatus = null;
    if (orderItemDetails != null) {
      priorAuthItemId = (Integer) orderItemDetails.get(prefix + "_prior_auth_item_id");
      patPendingPrescId = (orderItemDetails.get(prefix + "_pat_pending_presc_id") != null
        && !"null".equals(orderItemDetails.get(prefix + "_pat_pending_presc_id")))
          ? Long.valueOf(orderItemDetails.get(prefix + "_pat_pending_presc_id").toString()) : null;
      preAuthActStatus = (String) orderItemDetails.get(prefix + "_preauth_act_status");
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
    
    BasicDynaBean masterCharge = getMasterChargesBean((Object) orderBean.get(prefixId), bedType,
        ratePlanId, centerId);

    boolean condApplicable;
    if (masterCharge.getDynaClass().getDynaProperty("conduction_applicable") != null) {
      condApplicable = (Boolean) masterCharge.get("conduction_applicable");
    } else {
      condApplicable = false;
    }

    if (masterCharge.getDynaClass().getDynaProperty("specialization") != null
        && masterCharge.get("specialization") != null
        && masterCharge.get("specialization").equals("I") && ("services").equals(prefix)) {
      BasicDynaBean bean = ivfCycleService.getBean();
      bean.set("ivf_cycle_id", ivfCycleService.getNextSequence());
      bean.set(MR_NO, headerInformation.get(MR_NO));
      bean.set("patient_id", headerInformation.get("patient_id"));
      bean.set("start_date", DateUtil.getCurrentDate());
      bean.set("cycle_status", "O");
      ivfCycleService.insert(bean);
    }

    List<BasicDynaBean> chargesList = null;
    if (chargeable) {
      BigDecimal charge;
      BigDecimal discount;
      if (isMultivisitPackage) {
        charge =
            new BigDecimal(String.valueOf(orderItemDetails.get(prefix + "_act_rate")));
        discount =
            new BigDecimal(String.valueOf(orderItemDetails.get(prefix + "_discount")));
        BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
            (Integer) orderItemDetails.get(prefix + "_package_id"));
        if (("services").equals(prefix)) {
          masterCharge.set("unit_charge", charge);
          masterCharge.set("discount", discount);
          masterCharge.set("allow_rate_increase", 
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          masterCharge.set("allow_rate_decrease", 
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
        }
      }
      if (("packages").equals(prefix)) {
        masterCharge.set("package_name", orderItemDetails.get(prefix + "_package_name"));
      }

      BigDecimal quantity = BigDecimal.ONE;
      if (null != orderBean.getDynaClass().getDynaProperty("quantity")
          && !((orderBean.get("quantity")).equals(""))) {
        quantity = (BigDecimal) orderBean.get("quantity");
      }
      HashMap<String, Object> otherParams = new HashMap<>();
      otherParams.put("package_id", orderItemDetails.get(prefix + "_package_id"));
      otherParams.put("visit_type", bill.get("visit_type"));
      otherParams.put("exclude_invite", true);
      otherParams.put("insert_charges", true);
      otherParams.put("org_id", orderItemDetails.get("packages_org_id"));
      if (orderItemDetails.get(prefix + "_item_excluded_from_doctor") != null) {
        otherParams.put("item_excluded_from_doctor",
            orderItemDetails.get(prefix + "_item_excluded_from_doctor"));
      }
      if (orderItemDetails.get(prefix + "_item_excluded_from_doctor_remarks") != null) {
        otherParams.put("item_excluded_from_doctor_remarks",
            orderItemDetails.get(prefix + "_item_excluded_from_doctor_remarks"));
      }
      if (orderItemDetails.get(prefix + "_item_excluded_from_doctor") != null) {
        if (orderItemDetails.get(prefix + "_item_excluded_from_doctor").equals("Y")) {
          otherParams.put("item_excluded_from_doctor", true);
          otherParams.put("item_excluded_from_doctor_remarks",
              orderItemDetails.get(prefix + "_item_excluded_from_doctor_remarks"));

        } else if (orderItemDetails.get(prefix + "_item_excluded_from_doctor").equals("N")) {

          otherParams.put("item_excluded_from_doctor", false);
          otherParams.put("item_excluded_from_doctor_remarks",
              orderItemDetails.get(prefix + "_item_excluded_from_doctor_remarks"));
        }
      }
      if (orderItemDetails.get(prefix + "_package_contents") != null) {
        otherParams.put("package_contents",
            orderItemDetails.get(prefix + "_package_contents"));
      }
      if (orderItemDetails.get(prefix + "_is_customized") != null) {
        otherParams.put("is_customized",
            orderItemDetails.get(prefix + "_is_customized"));
      }
      if (orderItemDetails.get(prefix + "_submission_batch_type") != null) {
        otherParams.put("submission_batch_type", orderItemDetails
            .get(prefix + "_submission_batch_type"));
      }
      if (orderItemDetails.get(prefix + "_code_type") != null) {
        otherParams.put("code_type", orderItemDetails
            .get(prefix + "_code_type"));
      }
      if (orderItemDetails.get(prefix + "_act_rate_plan_item_code") != null) {
        otherParams.put("act_rate_plan_item_code", orderItemDetails
            .get(prefix + "_act_rate_plan_item_code"));
      }
      chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId, otherParams);
      int prescId = (Integer) orderBean.get("prescription_id");
      String presDrId = (String) orderBean.get("doctor_id");
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId,
            new Timestamp(postedDate.getTime()), (String) orderBean.get("remarks"),
            orderItemDetails,bedType);
      } else {
        Timestamp postedDate = (Timestamp) orderBean.get("presc_date");
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds, prescId, presDrId, postedDate,
            (String) orderBean.get("remarks"),orderItemDetails,bedType);
      }
    }
    return chargesList;
  }

  /**
   * This Function performs the data collection and insertion into
   * table(bill_charge, bill_activity_charge, bill_chare_claim).
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
   * @return the list
   * @throws ParseException
   *           the parse exception
   */
  @Transactional(rollbackFor = Exception.class)
  public abstract List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException;

  /**
   * Gets the duration charge.
   *
   * @param duration
   *          the duration
   * @param minDuration
   *          the min duration
   * @param slab1Duration
   *          the slab 1 duration
   * @param incrDuration
   *          the incr duration
   * @param minCharge
   *          the min charge
   * @param slab1Rate
   *          the slab 1 rate
   * @param incrRate
   *          the incr rate
   * @param splitCharge
   *          the split charge
   * @return the duration charge
   */
  public BigDecimal getDurationCharge(int duration, int minDuration, int slab1Duration,
      int incrDuration, BigDecimal minCharge, BigDecimal slab1Rate, BigDecimal incrRate,
      boolean splitCharge) {

    return getDurationCharge(duration, minDuration, slab1Duration, slab1Duration, incrDuration,
        minCharge, slab1Rate, slab1Rate, incrRate, splitCharge);
  }

  /**
   * Gets the duration charge, 3-slab.
   *
   * @param duration
   *          the duration
   * @param minDuration
   *          the min duration
   * @param slab1Duration
   *          the slab 1 duration
   * @param slab2Duration
   *          the slab 2 duration
   * @param incrDuration
   *          the incr duration
   * @param minCharge
   *          the min charge
   * @param slab1Rate
   *          the slab 1 rate
   * @param slab2Rate
   *          the slab 2 rate
   * @param incrRate
   *          the incr rate
   * @param splitCharge
   *          the split charge
   * @return the duration charge
   */
  public BigDecimal getDurationCharge(int duration, int minDuration, int slab1Duration,
      int slab2Duration, int incrDuration, BigDecimal minCharge, BigDecimal slab1Rate,
      BigDecimal slab2Rate, BigDecimal incrRate, boolean splitCharge) {

    if (duration <= minDuration) {
      return minCharge;

    } else if (duration <= slab1Duration) {
      return slab1Rate;

    } else if (duration <= slab2Duration) {
      return slab2Rate;

    } else {
      if (incrDuration != 0) {
        int addnlUnits = duration - slab2Duration;
        int incrUnits = addnlUnits / incrDuration + (addnlUnits % incrDuration > 0 ? 1 : 0);
        if (splitCharge) {
          return incrRate.multiply(new BigDecimal(incrUnits));
        } else {
          return slab2Rate.add(incrRate.multiply(new BigDecimal(incrUnits)));
        }

      } else {
        if (minDuration == 0) {
          return minCharge.add(incrRate.multiply(new BigDecimal(duration)));
        } else {
          return minCharge;
        }
      }
    }
  }

  /**
   * Gets the duration.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @param units
   *          the units
   * @return the duration
   */
  public static int getDuration(Timestamp from, Timestamp to, String units) {
    return getDuration(from, to, units, 60);
  }

  /**
   * Returns the Duration between two timeStamp. Any part of hour is considered as,
   * full hour. Example: 61min == 2hr,
   * We use Ceiling. Example: if unitSize is 15, then the following are the,
   * conversions: 0-15: 1, 16-30: 2, 31-45: 3, etc.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @param type
   *          the type
   * @param unitSize
   *          the unit size
   * @return the duration
   */
  public static int getDuration(Timestamp from, Timestamp to, String type, int unitSize) {
    long timeDiff = to.getTime() - from.getTime();
    int minutes = (int) (timeDiff / SEC_IN_MIN / MILLISEC_IN_SEC);

    int duration;
    if (("D").equals(type)) {
      int hours;
      if ((minutes % MIN_IN_HOUR) > 0) {
        hours = (minutes / MIN_IN_HOUR) + 1;
      } else {
        hours = minutes / MIN_IN_HOUR;
      }

      if ((hours % HOUR_IN_DAY) > 0) {
        duration = (hours / HOUR_IN_DAY) + 1;
      } else {
        duration = hours / HOUR_IN_DAY;
      }
    } else {
      if ((minutes % unitSize) > 0) {
        duration = (minutes / unitSize) + 1;
      } else {
        duration = minutes / unitSize;
      }
    }
    return duration;
  }

  /**
   * Check if the Annotation contains Order as meta-annotation.
   *
   * @param annotations
   *          the annotations
   * @return the annotation
   */
  public Annotation checkOrderAnnotionExist(Annotation[] annotations) {
    for (Annotation annotation : annotations) {
      if (annotation instanceof Order) {
        return annotation;
      }
    }
    return null;
  }

  /**
   * Update Prescription Status - Services / Tests.
   *
   * @param status
   *          the status
   * @param presId
   *          the pres id
   */
  public void updatePrescription(String status, Integer presId) {
    patientMedicinePrescriptionsRepository.updatePrescription(status,
        (String) sessionService.getSessionAttributes().get("userId"), presId);
  }

  /**
   * This will return the item details based on parameters passed.
   *
   * @param entityIdList
   *          the entity id list
   * @param paramMap
   *          the param map
   * @return the item details
   */
  public abstract List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap);

  /**
   * This will return items ordered for the specific visitId.
   *
   * @param parameters
   *          the parameters
   * @return the ordered items
   */
  public abstract List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters);

  /**
   * returns the corresponding cancelled bean of order item.
   *
   * @param item
   *          the item
   * @return the cancel bean
   * @throws ParseException
   *           the parse exception
   */
  public abstract BasicDynaBean getCancelBean(Map<String, Object> item) throws ParseException;

  /**
   * Gets the cancel bean. this is called with reflection in package order item
   * service. if u change the method signature u need to change take care it does
   * not break cancelPackageRefOrders.
   *
   * @param orderId
   *          the order id
   * @return the cancel bean
   * @throws ParseException
   *           the parse exception
   */
  public abstract BasicDynaBean getCancelBean(Object orderId) throws ParseException;

  /**
   * returns the corresponding editted bean of order item.
   *
   * @param item
   *          the item
   * @return the edits the bean
   * @throws ParseException
   *           the parse exception
   */
  public abstract BasicDynaBean getEditBean(Map<String, Object> item) throws ParseException;

  /**
   * update the edited, cancelled or cancel item charge beans in a bulk.
   *
   * @param items
   *          the items
   * @return the int[]
   */
  public abstract int[] updateItemBeans(List<BasicDynaBean> items);

  /**
   * update patient prescription tables to cancel status and cancels ref orders,
   * for operation, packages, beds,
   * In equipement the order bean's finalization status is queried from database,
   * to use in updateFinalizableItemCharges function.
   *
   * @param items
   *          the items
   * @param cancel
   *          the cancel
   * @param cancelCharges
   *          the cancel charges
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
   *           the parse exception
   */
  public abstract void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException,
      ParseException;

  /**
   * update finalizable items charges, ex - equipment, bed, operations.
   *
   * @param item
   *          the item
   * @param itemInfoMap
   *          the item info map
   * @return true, if successful
   * @throws ParseException
   *           the parse exception
   */
  public abstract boolean updateFinalizableItemCharges(List<BasicDynaBean> item,
      Map<String, Object> itemInfoMap) throws ParseException;

  /**
   * Returns primary key for the order item prescription table.
   *
   * @return the order item primary key
   */
  public abstract String getOrderItemPrimaryKey();

  /**
   * Returns activity code for order item prescription table.
   *
   * @return the order item activity code
   */
  public abstract String getOrderItemActivityCode();

  /**
   * Returns prescription doctor key for order item prescription table.
   *
   * @return the prescription doc key
   */
  public abstract String getPrescriptionDocKey();

  /**
   * Return true only in case of doctor order item service, where package_ref is
   * not null.
   *
   * @param item
   *          the item
   * @return true, if is doctor part of package
   */
  public boolean isDoctorPartOfPackage(BasicDynaBean item) {
    return false;
  }

  /**
   * Return remarks. only in case of diet order item service, it is overridden.
   *
   * @param item
   *          the item
   * @return the order item remarks
   */
  public String getOrderItemRemarks(BasicDynaBean item) {
    return (String) item.get("remarks");
  }

  /**
   * Overridden in Service order item service.
   *
   * @param item
   *          the item
   * @param charge
   *          the charge
   */
  public void setChargeQuantityAndAmount(BasicDynaBean item, BasicDynaBean charge) {
    // This function sets charge act_quantity and recalculates amount for
    // Service item.
  }

  /**
   * Order items.
   *
   * @param allOrderItems
   *          the all order items
   * @param visitParams
   *          the visit params
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @param billsInfoMap
   *          the bills info map
   * @param billWisePatPackIdsMap
   *          the bill wise pat pack ids map
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
  public void orderItems(List<Object> allOrderItems, Map<String, Object> visitParams,
      List<String> editOrCancelOrderBills, Map<String, Map<String, Object>> billsInfoMap,
      Map<String, List<Object>> billWisePatPackIdsMap) throws ParseException, IOException,
      NoSuchMethodException, IllegalAccessException, InvocationTargetException {

    List<Object> cancelItemsMaps = new ArrayList<>();
    List<Object> cancelItemChargesMaps = new ArrayList<>();
    List<Object> editedItemsMaps = new ArrayList<>();
    Map<String, List<Object>> billsItemsMap = new HashMap<>();

    Map<String, Object> itemInfoMap = new HashMap<>();
    itemInfoMap.put("visit_id", visitParams.get("visit_id"));

    groupingOrderItems(allOrderItems, billsItemsMap, editedItemsMaps, cancelItemsMaps,
        cancelItemChargesMaps);

    if (!cancelItemsMaps.isEmpty()) {
      List<BasicDynaBean> cancelItems = getCancelledBeans(cancelItemsMaps);
      itemInfoMap.put(ITEMS_MAP, cancelItemsMaps);
      updateOrders(cancelItems, true, false, true, editOrCancelOrderBills, itemInfoMap);
      
    }

    if (!cancelItemChargesMaps.isEmpty()) {
      List<BasicDynaBean> cancelItemCharges = getCancelledBeans(cancelItemChargesMaps);
      itemInfoMap.put(ITEMS_MAP, cancelItemChargesMaps);
      updateOrders(cancelItemCharges, true, true, false, editOrCancelOrderBills, itemInfoMap);
    }

    if (!editedItemsMaps.isEmpty()) {
      List<BasicDynaBean> editedItems = getEditedBeans(editedItemsMaps);
      itemInfoMap.put(ITEMS_MAP, editedItemsMaps);
      itemInfoMap.put("billsInfoMap", billsInfoMap);
      updateOrders(editedItems, false, false, false, editOrCancelOrderBills, itemInfoMap);
      
    }

    // Seperate Insert for all different bill Items.
    for (Map.Entry<String, List<Object>> billsItemsEntry : billsItemsMap.entrySet()) {
      List<Object> itemsList = billsItemsEntry.getValue();
      String billNo = billsItemsEntry.getKey();
      List<Object> patientPackageidsList = null;
      if (billWisePatPackIdsMap != null) {
        patientPackageidsList = billWisePatPackIdsMap.get(billNo);
      }
      if (!itemsList.isEmpty()) {
        Map<String, List<Object>> billItemAuthMap = getOrderAuthObject();
        insertOrders(itemsList, true, billItemAuthMap, billsInfoMap.get(billNo),
            patientPackageidsList);
        if (!"new".equals(billNo) && !"newInsurance".equals(billNo)
            && !editOrCancelOrderBills.contains(billNo)) {
          editOrCancelOrderBills.add(billNo);
        }
      }
    }
  }

  /**
   * Gets the cancelled beans.
   *
   * @param cancelledItems
   *          the cancelled items
   * @return the cancelled beans
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getCancelledBeans(List<Object> cancelledItems) throws ParseException {
    List<BasicDynaBean> cancelledBeanList = new ArrayList<>();
    for (Object itemObject : cancelledItems) {
      Map<String, Object> item = (Map<String, Object>) itemObject;
      cancelledBeanList.add(getCancelBean(item));
    }
    return cancelledBeanList;
  }

  /**
   * Gets the edited beans.
   *
   * @param editedItems
   *          the edited items
   * @return the edited beans
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getEditedBeans(List<Object> editedItems) throws ParseException {
    List<BasicDynaBean> editedBeanList = new ArrayList<>();
    for (Object itemObject : editedItems) {
      Map<String, Object> item = (Map<String, Object>) itemObject;
      editedBeanList.add(getEditBean(item));
    }
    return editedBeanList;
  }

  /**
   * Gets the order item bean list.
   *
   * @param newItems
   *          the new items
   * @param authMap
   *          the auth map
   * @param headerInformation
   *          the header information
   * @param preAuthIds
   *          the pre auth ids
   * @param preAuthModeIds
   *          the pre auth mode ids
   * @param username
   *          the username
   * @param operationBean
   *          the operation bean
   * @return the order item bean list
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> getOrderItemBeanList(List<Object> newItems,
      Map<String, List<Object>> authMap, BasicDynaBean headerInformation, String[] preAuthIds,
      Integer[] preAuthModeIds, String username, BasicDynaBean operationBean)
      throws ParseException {
    List<BasicDynaBean> newItemsBeanList = new ArrayList<>();
    for (Object itemObject : newItems) {
      Map<String, Object> item = (Map<String, Object>) itemObject;
      if (item.get("item_id") != null && item.get("item_id") instanceof Integer) {
        item.put("item_id", String.valueOf((int) item.get("item_id")));
      }
      BasicDynaBean bean = getOrderItemBean(item, authMap, headerInformation, preAuthIds,
          preAuthModeIds, username, operationBean);
      newItemsBeanList.add(bean);
    }
    return newItemsBeanList;
  }

  /**
   * Insert orders.
   *
   * @param itemsMapsList
   *          the items maps list
   * @param chargeable
   *          the chargeable
   * @param billItemAuthMap
   *          the bill item auth map
   * @param billInfoMap
   *          the bill info map
   * @param patientPackageidsList
   *          the patient packageids list. To Support order sets package
   *          Customization.
   * @return the list
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public abstract List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageidsList) throws ParseException, IOException;

  /**
   * Set the preAuthId, PreAuthMode, conductingDoctorId, multiVisitPackage value.
   *
   * @param finalPreAuthIds
   *          the final pre auth ids
   * @param finalPreAuthModeIds
   *          the final pre auth mode ids
   * @param orderAuth
   *          the order auth
   * @param index
   *          the index
   */
  public void setPreAuthIdAndMode(String[] finalPreAuthIds, Integer[] finalPreAuthModeIds,
      Map<String, List<Object>> orderAuth, int index) {

    if (!orderAuth.get("newPreAuths").isEmpty() && index < orderAuth.get("newPreAuths").size()) {
      finalPreAuthIds[0] = (String) orderAuth.get("newPreAuths").get(index);
    }

    if (!orderAuth.get("secNewPreAuths").isEmpty()
        && index < orderAuth.get("secNewPreAuths").size()) {
      finalPreAuthIds[1] = (String) orderAuth.get("secNewPreAuths").get(index);
    }

    if (!orderAuth.get("newPreAuthModesList").isEmpty()
        && index < orderAuth.get("newPreAuthModesList").size()) {
      finalPreAuthModeIds[0] = (Integer) orderAuth.get("newPreAuthModesList").get(index);
    }

    if (!orderAuth.get("secNewPreAuthModesList").isEmpty()
        && index < orderAuth.get("secNewPreAuthModesList").size()) {
      finalPreAuthModeIds[1] = (Integer) orderAuth.get("secNewPreAuthModesList").get(index);
    }

  }

  /**
   * Grouping the order items into separate List BasicDynaBean  Edited items,
   * Cancelled items, Cancelled Item Charges, New Bill items, New Insurance Bill
   * Items.
   *
   * @param allOrderItems
   *          the all order items
   * @param billsItemsMap
   *          the bills items map
   * @param editedItemsMaps
   *          the edited items maps
   * @param cancelItemsMaps
   *          the cancel items maps
   * @param cancelItemChargesMaps
   *          the cancel item charges maps
   */
  public void groupingOrderItems(List<Object> allOrderItems,
      Map<String, List<Object>> billsItemsMap, List<Object> editedItemsMaps,
      List<Object> cancelItemsMaps, List<Object> cancelItemChargesMaps) {

    for (Object itemObject : allOrderItems) {
      Map<String, Object> item = (Map<String, Object>) itemObject;
      String cancelled = (String) item.get("cancelled");
      String edited = (String) item.get("edited");
      Object orderId = item.get("order_id");
      Boolean opEdited = checkOperationEdited(item);
      if (orderId != null && !"".equals(orderId)
          && ("I".equals(cancelled) || "IC".equals(cancelled) || "Y".equals(edited) || opEdited)) {
        if (!"".equals(cancelled)) {
          if ("IC".equals(cancelled)) {
            cancelItemChargesMaps.add(item);
          } else if ("I".equals(cancelled)) {
            cancelItemsMaps.add(item);
          }
        } else {
          editedItemsMaps.add(item);
        }
      } else if (orderId == null || "".equals(orderId)) {
        List<Object> itemList = billsItemsMap.get(item.get("bill_no"));
        if (itemList == null) {
          itemList = new ArrayList<>();
          itemList.add(item);
          billsItemsMap.put((String) item.get("bill_no"), itemList);
        } else {
          itemList.add(item);
        }
      }
    }
  }

  /**
   * Check operation edited.
   *
   * @param item
   *          the item
   * @return the boolean
   */
  public Boolean checkOperationEdited(Map<String, Object> item) {
    // Overridden in operation.
    return false;
  }

  /**
   * update the prescribing doctor ID in the bill_charge.
   *
   * @param item
   *          the item
   * @param chargeId
   *          the charge id
   * @return the basic dyna bean
   */
  private BasicDynaBean updatePrescribingDoctor(BasicDynaBean item, String chargeId) {

    String prescDrId = (String) item.get(getPrescriptionDocKey());
    billChargeService.updatePrescribingDoctor(chargeId, prescDrId, true);

    BasicDynaBean curCharge = billChargeService.getCharge(chargeId);
    curCharge.set("user_remarks", getOrderItemRemarks(item));
    setChargeQuantityAndAmount(item, curCharge);

    billChargeService.updateChargeAmounts(curCharge);

    return curCharge;
  }

  /**
   * Cancel charges and associated charges.
   *
   * @param item
   *          the item
   * @param cancelCharges
   *          the cancel charges
   * @param unlinkActivity
   *          the unlink activity
   * @param chargeId
   *          the charge id
   */
  public void cancelChargesAndAssociatedCharges(BasicDynaBean item, boolean cancelCharges,
      boolean unlinkActivity, String chargeId) {

    String userName = (String) sessionService.getSessionAttributes().get("userId");
    String activityCode = getOrderItemActivityCode();
    String activityId = String.valueOf(item.get(getOrderItemPrimaryKey()));
    if (cancelCharges) {
      billChargeService.cancelChargeUpdate(chargeId, true, userName);

      List<BasicDynaBean> associatedChargeList = billChargeService.getAssociatedCharges(chargeId);
      List<String> chargeIdsList = new ArrayList<>();
      chargeIdsList.add(chargeId);
      for (BasicDynaBean bean : associatedChargeList) {
        chargeIdsList.add(String.valueOf(bean.get("charge_id")));
      }

      billChargeTaxService.cancelBillChargeTax(chargeIdsList);
      billChargeClaimService.cancelBillChargeClaim(chargeIdsList);
      billChargeClaimTaxService.cancelBillChargeClaimTax(chargeIdsList);
    } else if (unlinkActivity) {
      // update the charge as hasActivity = false, when not canceling the
      // charge.
      billChargeService.updateHasActivityStatus(chargeId, false, true);
      billActivityChargeService.deleteActivity(activityCode, activityId);
    } else if (isDoctorPartOfPackage(item)) {
      // doctor is part of package, just delete the activity. this can be
      // executed when
      // user changes the consulting doctor from op list.
      billActivityChargeService.deleteActivity(activityCode, activityId);
    }

  }

  /**
   * Update orders.
   *
   * @param orders
   *          the orders
   * @param cancel
   *          the cancel
   * @param cancelCharges
   *          the cancel charges
   * @param unlinkActivity
   *          the unlink activity
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @param itemInfoMap
   *          the item info map - contains visit_id , items_map keys. For
   *          operation update you need the billInfoMap which contains
   *          header_information, bill, user_name, plan ids, because it needs the
   *          bill in which operation was ordered.
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   * @throws ParseException
   *           the parse exception
   */
  public void updateOrders(List<BasicDynaBean> orders, boolean cancel, boolean cancelCharges,
      boolean unlinkActivity, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      ParseException {
    updateOrders(orders, cancel, cancelCharges, unlinkActivity, false, editOrCancelOrderBills,
        itemInfoMap);
  }

  /**
   * Update orders.
   *
   * @param orders
   *          the orders
   * @param cancel
   *          the cancel
   * @param cancelCharges
   *          the cancel charges
   * @param unlinkActivity
   *          the unlink activity
   * @param ignoreBillStatus
   *          the ignore bill status
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @param itemInfoMap
   *          the item info map - contains visit_id , items_map keys. For
   *          operation update you need the billInfoMap which contains
   *          header_information, bill, user_name, plan ids, because it needs the
   *          bill in which operation was ordered.
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   * @throws ParseException
   *           the parse exception
   */
  public void updateOrders(List<BasicDynaBean> orders, boolean cancel, boolean cancelCharges,
      boolean unlinkActivity, boolean ignoreBillStatus, List<String> editOrCancelOrderBills,
      Map<String, Object> itemInfoMap) throws IOException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException, ParseException {

    if (orders.isEmpty()) {
      return;
    }

    List<BasicDynaBean> chargeListToUpdateTax = new ArrayList<>();

    updateCancelStatusAndRefOrders(orders, cancel, cancelCharges, editOrCancelOrderBills,
        itemInfoMap);

    updateItemBeans(orders);

    updatePreAuthItemQuantity(itemInfoMap, cancel);

    updateAdditionalInfo(orders, cancel, cancelCharges, editOrCancelOrderBills, itemInfoMap);

    if (!cancel) {
      updateFinalizableItemCharges(orders, itemInfoMap);
    }

    if (cancel || cancelCharges) {
      updatePatientActivities(orders);
      //update patient_pending_prescriptions status to prescribed.
      //Update the patient_package_content_consumed if find patient_package_content_id.
      updatePendingPrescriptionStatusAndPackageComptionOnCancel(itemInfoMap);
      cancelAutogeneratedPriorAuthPrescriptions(itemInfoMap);
    }

    String primaryKey = getOrderItemPrimaryKey();
    String activityCode = getOrderItemActivityCode();

    for (BasicDynaBean item : orders) {

      String activityId = item.get(primaryKey).toString();
      BasicDynaBean chargeBean = billActivityChargeService.getChargeAndBillDetails(activityCode,
          activityId);
      String chargeId = chargeBean != null ? (String) chargeBean.get("charge_id") : null;

      if (!ignoreBillStatus && chargeId != null && (cancelCharges)) {
        String billStatus = billChargeService.getBillStatus(chargeId);
        if (billStatus != null && !billStatus.equals("A")) {
          throw new ValidationException(
              "exception.bill.status.is.not.open.cannot.update.cancel.charge");
        }
      }

      if (cancel) {
        cancelChargesAndAssociatedCharges(item, cancelCharges, unlinkActivity, chargeId);
      } else {
        chargeListToUpdateTax.add(updatePrescribingDoctor(item, chargeId));
      }

      if (editOrCancelOrderBills != null && chargeId != null) {
        String billNo = (chargeBean != null) ? (String) chargeBean.get("bill_no") : null;
        if (billNo != null && !editOrCancelOrderBills.contains(billNo)) {
          editOrCancelOrderBills.add(billNo);
        }
      }
    }
    if (!chargeListToUpdateTax.isEmpty()) {

      billChargeTaxService.calculateAndUpdateBillChargeTaxes(chargeListToUpdateTax,
          (String) itemInfoMap.get("visit_id"));
    }
    
    // Send for pre auth if marked.
    String visitId = (String) itemInfoMap.get("visit_id");
    BasicDynaBean insurancePlanDetails = patientInsurancePlansService.getVisitPrimaryPlan(visitId);
    if (null == insurancePlanDetails) {
      return;
    }


    List<String> chargesToSendForPriorAuth = getChargesToSendForPriorAuth(itemInfoMap);
    if (chargesToSendForPriorAuth.isEmpty()) {
      return;
    }
    List<BasicDynaBean> chargesForPriorAuth = billChargeService
        .getBillChargesForPriorAuth(chargesToSendForPriorAuth);

    orderService.createPreAuthPrescriptionForOrderedItems(chargesForPriorAuth, visitId,
        (String) insurancePlanDetails.get("insurance_co_id"), 0);

  }

  private void cancelAutogeneratedPriorAuthPrescriptions(Map<String, Object> itemInfoMap) {
    List<Map<String, Object>> itemsList = (List<Map<String, Object>>) itemInfoMap.get(ITEMS_MAP);

    if (CollectionUtils.isEmpty(itemsList)) {
      return;
    }

    List<Integer> preauthActIds = new ArrayList<>();

    for (Map<String, Object> item : itemsList) {

      if (item.get("preauth_act_id") == null) {
        continue;
      }
      preauthActIds.add((Integer) item.get("preauth_act_id"));
    }

    List<BasicDynaBean> filteredPreAuthPrescriptionActivities =
        eauthPrescActivitiesRepo.getAutogeneratedPreAuthActIds(preauthActIds);

    if (CollectionUtils.isEmpty(filteredPreAuthPrescriptionActivities)) {
      return;
    }

    preauthActIds = new ArrayList<>();
    for (BasicDynaBean preauthActBean : filteredPreAuthPrescriptionActivities) {
      preauthActIds.add((Integer) preauthActBean.get("preauth_act_id"));
    }

    eauthService.deleteEAuthOfOrderedItem(preauthActIds,
        (String) sessionService.getSessionAttributes().get("username"));


  }

  /**
   * Gets the charges to send for prior auth.
   *
   * @param itemInfoMap the item info map
   * @return the charges to send for prior auth
   */
  private List<String> getChargesToSendForPriorAuth(Map<String, Object> itemInfoMap) {
    List<Map<String, Object>> itemsList = (List<Map<String, Object>>) itemInfoMap.get(ITEMS_MAP);
    if (itemsList == null) { 
      return Collections.emptyList();
    }
    List<String> chargesToSendForPriorAuth = new ArrayList<>();
    for (Map<String, Object> item : itemsList) {
      boolean sendForPriorAuth = "Y".equals((String)item.get("send_for_prior_auth"));
      Integer preAuthActId = (Integer) item.get("preauth_act_id");
      if (null == preAuthActId && sendForPriorAuth) {
        chargesToSendForPriorAuth.add((String)item.get("charge_id"));
      }
    }
    return chargesToSendForPriorAuth;
  }

  /**
   * Update pending prescription status on cancel.
   *
   * @param itemInfoMap the item info map
   */
  private void updatePendingPrescriptionStatusAndPackageComptionOnCancel(
      Map<String, Object> itemInfoMap) {
    List<Map<String, Object>> itemsList = (List<Map<String, Object>>) itemInfoMap.get(ITEMS_MAP);
    if (itemsList == null) { 
      return;
    }
    for (Map<String, Object> item : itemsList) {
      Integer preAuthActId = (Integer) item.get("preauth_act_id");
      if (null != preAuthActId) {
        if ("Service".equals(item.get("type"))) {
          String preauthStatusOfCancelledItem = preAuthItemsService
              .determinePreauthStatusOfChargeForCancellation((String) item.get("charge_id"),
                  preAuthActId);
          pendingPrescriptionsService.updatePendingPrescriptionStatusWithPreauthId(preAuthActId,
              preauthStatusOfCancelledItem, "P");
        } else {
          pendingPrescriptionsService.updatePendingPrescriptionStatusWithPreauthId(preAuthActId,
              "P");
        }
      }
      
      //Update the patient_package_content_consumed with patient_package_content_id for cancel item
      Integer patientPackageContentId = (item.get("patient_package_content_id") != null) 
          ? Integer.parseInt(item.get("patient_package_content_id").toString()) : 0;
      //if the package type is Operation, Get the content_ref_id, update all content_ref_id items 
      //to consumed.
      String itemType = (item.get("type") != null) ? item.get("type").toString() : "";
      Integer contentId = (item.get("content_id_ref") != null) 
          ? Integer.parseInt(item.get("content_id_ref").toString()) : 0;
      Integer orderID = (item.get("order_id") != null) 
          ? Integer.parseInt(item.get("order_id").toString()) : 0;
      if (patientPackageContentId > 0 && orderID > 0) {
        patPackContentConsumedSer.updatePkgContentConsumedQuantity(
            patientPackageContentId, orderID, itemType, contentId);
      }
    }   
  }

  /**
   * Update pre auth item quantity.
   *
   * @param itemInfoMap
   *          the item info map
   * @param isCancel
   *          the is cancel
   */
  private void updatePreAuthItemQuantity(Map<String, Object> itemInfoMap, boolean isCancel) {
    if (MapUtils.isNotEmpty(itemInfoMap)) {
      List<Map<String, Object>> itemMapList = (List<Map<String, Object>>) itemInfoMap
          .get(ITEMS_MAP);
      String chargeId;
      String preAuthActStatus;
      int itemQuantity = 0;
      if (CollectionUtils.isNotEmpty(itemMapList)) {
        for (Map<String, Object> itemMap : itemMapList) {
          if (itemMap.get("charge_id") != null
              && MapUtils.getBooleanValue(itemMap, "isRefundAllowed", true)) {
            Integer preAuthActId = (Integer) itemMap.get("preauth_act_id");
            chargeId = (String) itemMap.get("charge_id");
            itemQuantity = MapUtils.getInteger(itemMap, "quantity", 0);
            if (null != preAuthActId && preAuthActId > 0) {
              preAuthActStatus = preAuthItemsService
                 .determinePreauthStatusOfChargeForCancellation((String) itemMap.get("charge_id"),
                              preAuthActId);
              preAuthItemsService.updatePreAuthItemQuantity(chargeId, itemQuantity,
                  isCancel, false, preAuthActStatus);
            }
          }
        }
      }
    }
  }

  /**
   * Update patient activities.
   *
   * @param orders
   *          the orders
   */
  public void updatePatientActivities(List<BasicDynaBean> orders) {
    // update patient_activities for tests and services.
  }

  /**
   * Update additional info.
   *
   * @param orders
   *          the orders
   * @param cancel
   *          the cancel
   * @param cancelCharges
   *          the cancel charges
   * @param editOrCancelOrderBills
   *          the edit or cancel order bills
   * @param itemInfoMap
   *          the item info map
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws NoSuchMethodException
   *           the no such method exception
   * @throws IllegalAccessException
   *           the illegal access exception
   * @throws InvocationTargetException
   *           the invocation target exception
   */
  public void updateAdditionalInfo(List<BasicDynaBean> orders, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws IOException, ParseException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException {
    // this function is overridden only in test order item and Operation
    // order item.
  }

  /**
   * Gets the package ref orders.
   *
   * @param visitId
   *          the visit id
   * @param prescriptionId
   *          the prescription id
   * @return the package ref orders
   */
  public List<BasicDynaBean> getPackageRefOrders(String visitId, Integer prescriptionId) {
    List<Integer> prescriptionIdList = new ArrayList<>();
    prescriptionIdList.add(prescriptionId);
    return getPackageRefOrders(visitId, prescriptionIdList);
  }

  /**
   * Gets the package ref orders. This function is called through reflection. Do
   * not change the function name or parameters. If modifying them , modify at
   * cancelPackageRefOrders function in PackageOrderItemService
   *
   * @param visitId
   *          the visit id
   * @param prescriptionIdList
   *          the prescription id list
   * @return the package ref orders
   */
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionIdList) {
    return repository.getPackageRefOrders(visitId, prescriptionIdList);
  }

  /**
   * Gets the operation ref orders.
   *
   * @param visitId
   *          the visit id
   * @param prescribedId
   *          the prescribed id
   * @return the operation ref orders
   */
  public List<BasicDynaBean> getOperationRefOrders(String visitId, Integer prescribedId) {
    List<Integer> prescribedIdList = new ArrayList<>();
    prescribedIdList.add(prescribedId);
    return getOperationRefOrders(visitId, prescribedIdList);
  }

  /**
   * Gets the operation ref orders. This function is called through reflection. Do
   * not change the function name or parameters. If modifying them , modify at
   * cancelPackageRefOrders function in PackageOrderItemService
   *
   * @param visitId
   *          the visit id
   * @param prescribedIdList
   *          the prescribed id list
   * @return the operation ref orders
   */
  public List<BasicDynaBean> getOperationRefOrders(String visitId, List<Integer> prescribedIdList) {
    return repository.getOperationRefOrders(visitId, prescribedIdList);
  }

  /**
   * Insert package content.
   *
   * @param packageBean
   *          the package bean
   * @param packageItem
   *          the package item
   * @param headerInformation
   *          the header information
   * @param username
   *          the username
   * @param centerId
   *          the center id
   * @param mainChargeBean
   *          the main charge bean
   * @param packageItemDetails
   *          the package item details
   * @param index
   *          the index
   * @throws ParseException
   *           the parse exception
   */
  public void insertPackageContent(BasicDynaBean packageBean, BasicDynaBean packageItem,
      BasicDynaBean headerInformation, String username, Integer centerId,
      BasicDynaBean mainChargeBean, Map<String, Object> packageItemDetails, Integer index)
      throws ParseException {
    // Overridden for only items which can be ordered in packages
    // Service, otherServices, tests, Operation and doctor.
  }

  /**
   * Abstract function : Every Item Type will have its own Master Charges. Return
   * the Master charge details of all bed type.
   *
   * @param prefixId   the prefix id
   * @param ratePlanId the ratePlan id
   * @return list of charge
   */
  public abstract List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object prefixId,
      String ratePlanId);

}
