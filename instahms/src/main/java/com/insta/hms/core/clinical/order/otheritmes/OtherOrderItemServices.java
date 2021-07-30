package com.insta.hms.core.clinical.order.otheritmes;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.ordersets.OrderSetsOrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.commoncharges.CommonChargesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class basically does all function related to other charges.
 *
 * @author ritolia
 */
@Service
@Order(key = "Other Charge", value = { "Other Charge", "Implant", "Consumable" }, prefix = "others")
public class OtherOrderItemServices extends OrderItemService {

  private static final String MULTI_VISIT_PACKAGE = "_multi_visit_package";
  private static final String BILL_RATE_PLAN_ID = "bill_rate_plan_id";
  private static final String BED_TYPE = "bed_type";
  private static final String SERVICE_NAME = "service_name";
  private static final String PRESCRIBED_ID = "prescribed_id";
  private static final String PRESCRIBED_TIME = "pres_time";
  private static final String OTHERS_TYPE = "others_type";
  private static final String SERVICE_GROUP = "service_group";
  private static final String ACTIVITY_CODE = "OTC";
  private static final String PRESCRIPTION_DOCTOR_KEY = "doctor_id";
  private static String prefix = "others";
  private OtherOrderItemRepository otherOrderItemRepository;

  /**
   * Instantiates a new other order item services.
   *
   * @param repository
   *          the OtherOrderItemRepository
   */
  public OtherOrderItemServices(OtherOrderItemRepository repository) {
    super(repository, prefix, SERVICE_NAME);
    otherOrderItemRepository = repository;
  }

  @LazyAutowired
  private CommonChargesService commonChargesService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private OrderSetsOrderItemService orderSetsOrderItemService;

  /**
   * Set the header properties for the other charge Bean. Normalizing the data as per parameter
   * getting from front-end.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    setBeanValue(orderBean, PRESCRIBED_ID, otherOrderItemRepository.getNextSequence());
    setBeanValue(orderBean, "patient_id", headerInformation.get("patient_id"));
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));
    setBeanValue(orderBean, "doctor_id", orderedItemList.get("others_prescribed_doctor_id"));
    setBeanValue(orderBean, SERVICE_NAME, orderedItemList.get("others_item_id"));

    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_TIME);
    setBeanValue(orderBean, "pres_time", ConvertUtils
        .convert(orderedItemList.get("others_prescribed_date"), dynaProperties.getType()));

    String serviceGroup = null;
    if (orderedItemList.get(OTHERS_TYPE).equals("Other Charge")) {
      serviceGroup = "OCOTC";
    } else if (orderedItemList.get(OTHERS_TYPE).equals("Implant")) {
      serviceGroup = "IMPOTC";
    } else if (orderedItemList.get(OTHERS_TYPE).equals("Consumable")) {
      serviceGroup = "CONOTC";
    }
    setBeanValue(orderBean, SERVICE_GROUP, serviceGroup);
  }

  @Override
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) {
    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    setBeanValue(orderBean, PRESCRIBED_ID, otherOrderItemRepository.getNextSequence());
    setBeanValue(orderBean, "patient_id", headerInformation.get("patient_id"));
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));
    setBeanValue(orderBean, PRESCRIPTION_DOCTOR_KEY, orderedItemList.get("prescribed_doctor_id"));
    setBeanValue(orderBean, SERVICE_NAME, orderedItemList.get("item_id"));

    DynaProperty dynaProperties = orderBean.getDynaClass().getDynaProperty(PRESCRIBED_TIME);
    setBeanValue(orderBean, "pres_time",
        ConvertUtils.convert(orderedItemList.get("prescribed_date"), dynaProperties.getType()));

    String serviceGroup = null;
    if (orderedItemList.get("type").equals("Other Charge")) {
      serviceGroup = "OCOTC";
    } else if (orderedItemList.get("type").equals("Implant")) {
      serviceGroup = "IMPOTC";
    } else if (orderedItemList.get("type").equals("Consumable")) {
      serviceGroup = "CONOTC";
    }
    setBeanValue(orderBean, SERVICE_GROUP, serviceGroup);
    return orderBean;
  }

  /**
   * return the master charges for the other charge item.
   */
  @Override
  public BasicDynaBean getMasterChargesBean(Object prefixId, String bedType, String ratePlanId,
      Integer centerId) {
    return commonChargesService.getCommonCharge((String) prefixId);
  }

  /**
   * return the master charges for the other charge item list.
   */
  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object prefixId, String ratePlanId) {
    List<BasicDynaBean> chargeList = new ArrayList<BasicDynaBean>();
    chargeList.add(commonChargesService.getCommonCharge((String) prefixId));
    return chargeList;
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");

    BasicDynaBean masterCharge = getMasterChargesBean(paramMap.get("id"), bedType, ratePlanId,
        null);

    return getChargesList(masterCharge, (BigDecimal) paramMap.get("quantity"), isInsurance, null,
        null);
  }

  /**
   * return the charge List to be inserted for bill charge.
   */
  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {

    int insuranceCategoryId;
    if (itemType == null || itemType.get("insurance_category_id") == null) {
      insuranceCategoryId = 0;
    } else {
      insuranceCategoryId = (Integer) itemType.get("insurance_category_id");
    }
    BasicDynaBean billChargeBean = billChargeService.setBillChargeBean(
        (String) itemType.get("charge_group"), (String) itemType.get("charge_type"),
        (BigDecimal) itemType.get("charge"), quantity, BigDecimal.ZERO,
        (String) itemType.get("charge_name"), (String) itemType.get("charge_name"), null,
        (Integer) itemType.get("service_sub_group_id"), insuranceCategoryId, isInsurance);

    billChargeBean.set("allow_rate_increase", (Boolean) itemType.get("allow_rate_increase"));
    billChargeBean.set("allow_rate_decrease", (Boolean) itemType.get("allow_rate_decrease"));
    if (itemType != null || itemType.get("billing_group_id") != null) {
      billChargeBean.set("billing_group_id", (Integer) itemType.get("billing_group_id"));
    }
    if (otherParams != null && otherParams.get("package_id") != null) {
      billChargeBean.set("package_id", otherParams.get("package_id"));
    }
    if (otherParams != null && otherParams.get("item_excluded_from_doctor") != null) {
      if (otherParams.get("item_excluded_from_doctor").equals("Y") || otherParams.get(
          "item_excluded_from_doctor").equals(true)) {
        billChargeBean.set("item_excluded_from_doctor", true);
        billChargeBean.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      } else if (otherParams.get("item_excluded_from_doctor").equals("N") || otherParams.get(
          "item_excluded_from_doctor").equals(false)) {
        billChargeBean.set("item_excluded_from_doctor", false);
        billChargeBean.set("item_excluded_from_doctor_remarks",
            otherParams.get("item_excluded_from_doctor_remarks"));
      }
    }
    
    if (otherParams != null && otherParams.get("preauth_act_id") != null) {
      billChargeBean.set("preauth_act_id", otherParams.get("preauth_act_id"));
    }
    
    List<BasicDynaBean> billChargeList = new ArrayList<BasicDynaBean>();
    billChargeList.add(billChargeBean);
    return billChargeList;
  }

  /**
   * Prepare the other order Bean to be ordered.
   */
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
        BasicDynaBean orderedItemBean = otherOrderItemRepository.getBean();
        ConversionUtils.copyJsonToDynaBeanPrefixed((Map) orderedItemList[i], orderedItemBean,
            errorList, prefix + "_");
        Boolean isMultivisitPackage = false;
        if (((Map<String, Object>) orderedItemList[i]) != null) {
          isMultivisitPackage = new Boolean(
              (Boolean) ((Map<String, Object>) orderedItemList[i]).get(multiVisitKey));
          orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);
        }
        setHeaderProperties(orderedItemBean, headerInformation, username, isMultivisitPackage,
            (Map) orderedItemList[i]);
        orderBeanList.add(orderedItemBean);
      }
    }
    return orderBeanList;
  }

  /**
   * Inserting the charges for other order item.
   * 
   * @throws ParseException the ParseException
   */
  @Override
  public List<BasicDynaBean> insertOrderCharges(boolean chargeable, BasicDynaBean headerInformation,
      BasicDynaBean orderBean, BasicDynaBean bill, String[] preAuthIds, Integer[] preAuthModeIds,
      int[] planIds, String condDoctorId, String activityCode, Integer centerId,
      Boolean isMultivisitPackage, Map<String, Object> orderItemDetails) throws ParseException {

    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    BigDecimal quantity = (BigDecimal) orderBean.get("quantity");
    String serviceGroup = (String) orderBean.get(SERVICE_GROUP);
    String bedType = (String) headerInformation.get(BED_TYPE);
    String ratePlanId = (String) headerInformation.get(BILL_RATE_PLAN_ID);
    List<BasicDynaBean> chargesList = null;

    if (chargeable) {
      if ("OCOTC".equals(serviceGroup)) {
        BasicDynaBean masterCharge = getMasterChargesBean((String) orderBean.get(SERVICE_NAME), "",
            "", centerId);
        BigDecimal charge = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        Map<String,Boolean> properties = null;
        if (isMultivisitPackage) {
          charge = new BigDecimal(String.valueOf(orderItemDetails.get(prefix + "_act_rate")));
          discount = new BigDecimal(String.valueOf(orderItemDetails.get(prefix + "_discount")));
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
              (Integer)orderItemDetails.get("others_package_id"));
          masterCharge.set("allow_rate_increase",  
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          masterCharge.set("allow_rate_decrease",  
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
          masterCharge.set("charge", charge);
        }
        Map<String, Object> otherParams = new HashMap<>();
        otherParams.put("item_excluded_from_doctor",
            orderItemDetails.get("item_excluded_from_doctor"));
        otherParams.put("item_excluded_from_doctor_remarks",
            orderItemDetails.get("item_excluded_from_doctor_remarks"));
        if (orderItemDetails.get("package_id") != null) {
          otherParams.put("package_id", orderItemDetails.get("package_id"));
        }
        chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId,
            otherParams);
        if (null != isMultivisitPackage && isMultivisitPackage && !chargesList.isEmpty()) {
          BasicDynaBean otherCharge = chargesList.get(0);
          otherCharge.set("discount", discount);
          otherCharge.set("amount", (charge.multiply(quantity)).subtract(discount));
          if (discount.compareTo(BigDecimal.ZERO) != 0) {
            otherCharge.set("overall_discount_amt", discount);
            otherCharge.set("overall_discount_auth", -1);
          } else {
            otherCharge.set("overall_discount_auth", null);
            otherCharge.set("overall_discount_amt", null);
          }
        }
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, orderBean,
            planIds, (Integer) orderBean.get(PRESCRIBED_ID), (String) orderBean.get("doctor_id"),
            new Timestamp(postedDate.getTime()));
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, orderBean,
            planIds, (Integer) orderBean.get(PRESCRIBED_ID), (String) orderBean.get("doctor_id"),
            (Timestamp) orderBean.get(PRESCRIBED_TIME));
      }
    }

    return chargesList;
  }

  /**
   * Used in Package (Type : Package Package).
   *
   * @param otherBean
   *          the other bean
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
  public void insertPackageBillActivityCharge(BasicDynaBean otherBean,
      BasicDynaBean headerInformation, Integer centerId, String packageChargeID,
      String conductingDoctorId) {

    BasicDynaBean billActicityChargeBean = billActivityChargeService.getBillActivityChargeBean(
        packageChargeID, "OTC", (String) otherBean.get("service_group"),
        otherBean.get(PRESCRIBED_ID).toString(), (String) otherBean.get(SERVICE_NAME), null, "Y",
        null);
    billActivityChargeService.insert(billActicityChargeBean);
  }

  /**
   * Returns the list of other item details by passing there charge_id.
   * 
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return otherOrderItemRepository.getItemDetails(entityIdList);
  }

  /**
   * Retuns the list of other items ordered for a given visit.
   */
  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    return otherOrderItemRepository.getOrderedItems((String) parameters.get("visit_id"),
        (Integer) parameters.get("operation_id"), (Boolean) parameters.get("package_ref"));
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, item.get("order_id"));
    bean.set("remarks", item.get("remarks"));
    bean.set("doctor_id", item.get("prescribed_doctor_id"));
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    return getCancelBean(item.get("order_id"));
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, orderId);
    bean.set("cancel_status", "C");
    bean.set("user_name", sessionService.getSessionAttributes().get("userId"));
    return bean;
  }

  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) {
    orderedItemAuths.get("newPreAuths").add(null);
    orderedItemAuths.get("newPreAuthModesList").add(1);
    orderedItemAuths.get("secNewPreAuths").add(null);
    orderedItemAuths.get("secNewPreAuthModesList").add(1);
    orderedItemAuths.get("conductingDoctorList").add(null);

    orderedItemAuths.get("conductingDoctorList").add((String) item.get("payee_doctor_id"));

    List errorList = new ArrayList();
    BasicDynaBean orderedItemBean = otherOrderItemRepository.getBean();
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

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    Map<String, Object> keys = new HashMap<>();
    List<Object> prescriptionIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescriptionIdsList.add(item.get(PRESCRIBED_ID));
    }
    keys.put(PRESCRIBED_ID, prescriptionIdsList);
    return otherOrderItemRepository.batchUpdate(items, keys);
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    // For other services there is no update.
  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // For other services there is no update.
    return true;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    return PRESCRIBED_ID;
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
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageIdsList) throws ParseException {
    BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
    String username = (String) billInfoMap.get("user_name");
    String[] preAuthIds = (String[]) billInfoMap.get("pre_auth_ids");
    Integer[] preAuthModeIds = (Integer[]) billInfoMap.get("pre_auth_mode_ids");
    BasicDynaBean bill = (BasicDynaBean) billInfoMap.get("bill");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int[] planIds = (int[]) billInfoMap.get("plan_ids");

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, null);

    otherOrderItemRepository.batchInsert(itemList);

    List<Object> ismvpList = billItemAuthMap.get("isMultivisitPackageList");
    List<Object> condDocList = billItemAuthMap.get("conductingDoctorList");
    for (int index = 0; index < itemList.size(); index++) {
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> otherItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean otherOrderBean = itemList.get(index);

      Boolean isMultivisitPackage = (Boolean) ismvpList.get(index);
      if (!condDocList.isEmpty() && index < condDocList.size()) {
        condDoctorId = (String) condDocList.get(index);
      }
      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      BasicDynaBean mainChargeBean = null;
      if (null != bill) {
        List<BasicDynaBean> chargesList = insertOrderItemCharges(chargeable, headerInformation,
            otherOrderBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, condDoctorId,
            ACTIVITY_CODE, centerId, isMultivisitPackage, otherItemDetails);
        mainChargeBean = chargesList == null ? null : chargesList.get(0);
      }

      String packageIdString = otherItemDetails.get("package_id") != null
          ? otherItemDetails.get("package_id").toString() : null;
      Integer packageId = packageIdString != null && !"".equals(packageIdString)
          ? Integer.parseInt(packageIdString) : null;
      String packageObIdString = otherItemDetails.get("package_ob_id") != null
          ? otherItemDetails.get("package_ob_id").toString() : null;
      Integer packObId = packageObIdString != null && !"".equals(packageObIdString)
          ? Integer.parseInt(packageObIdString) : null;
      Boolean isMultiVisitPackItem = (Boolean) otherItemDetails.get("multi_visit_package");
      Integer patientPackageId = null;
      if (patientPackageIdsList != null && patientPackageIdsList.size() >= index) {
        patientPackageId = (Integer) patientPackageIdsList.get(index);
      }
      String chargeId = mainChargeBean != null ? (String) mainChargeBean.get("charge_id") : null;
      if ((packageId != null) && (packObId != null) && (patientPackageId != null)
          && !isMultiVisitPackItem) {
        orderSetsOrderItemService.insertIntoPatientPackageContentAndConsumed(patientPackageId,
            packObId, packageId, (String) otherOrderBean.get(SERVICE_NAME),
            Integer.valueOf(((BigDecimal) otherOrderBean.get("quantity")).intValue()),
            (Integer) otherOrderBean.get(PRESCRIBED_ID), chargeId, username, "others");
      }
    }
    return itemList;
  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    BigDecimal quantity = (BigDecimal) orderBean.get("quantity");
    String serviceGroup = (String) orderBean.get(SERVICE_GROUP);
    List<BasicDynaBean> chargesList = null;

    if (chargeable) {
      if ("OCOTC".equals(serviceGroup)) {
        BasicDynaBean masterCharge = getMasterChargesBean((String) orderBean.get(SERVICE_NAME), "",
            "", centerId);
        BigDecimal charge = BigDecimal.ZERO;
        BigDecimal discount  = BigDecimal.ZERO;
        if (isMultivisitPackage) {
          charge = new BigDecimal(String.valueOf(orderItemDetails.get("act_rate")));
          discount = BigDecimal.valueOf(Double.valueOf((String) orderItemDetails.get("discount")));
          masterCharge.set("charge", charge);
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id",
              (Integer) orderItemDetails.get("package_id"));
          masterCharge.set("allow_rate_increase", 
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          masterCharge.set("allow_rate_decrease", 
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
        }
        Map<String, Object> otherParams = new HashMap<>();
        if (orderItemDetails.get("package_id") != null) {
          otherParams.put("package_id", orderItemDetails.get("package_id"));
        }
        chargesList = getChargesList(masterCharge, quantity,
        isInsurance, condDoctorId, otherParams);
        if (null != isMultivisitPackage && isMultivisitPackage && !chargesList.isEmpty()) {
          BasicDynaBean otherCharge = chargesList.get(0);
          otherCharge.set("discount", discount);
          otherCharge.set("amount", (charge.multiply(quantity)).subtract(discount));
          if (discount.compareTo(BigDecimal.ZERO) != 0) {
            otherCharge.set("overall_discount_amt", discount);
            otherCharge.set("overall_discount_auth", -1);
          } else {
            otherCharge.set("overall_discount_auth", null);
            otherCharge.set("overall_discount_amt", null);
          }
        }
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, orderBean,
            planIds, (Integer) orderBean.get(PRESCRIBED_ID),
            (String) orderBean.get(PRESCRIPTION_DOCTOR_KEY), new Timestamp(postedDate.getTime()));
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, orderBean,
            planIds, (Integer) orderBean.get(PRESCRIBED_ID),
            (String) orderBean.get(PRESCRIPTION_DOCTOR_KEY),
            (Timestamp) orderBean.get(PRESCRIBED_TIME));
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
              orderItemDetails.get("quantity"),
              chargeId, orderBean.get(PRESCRIBED_ID),
              orderItemDetails.get("type"));
        }
      }
    }

    return chargesList;
  }

  @Override
  public void insertPackageContent(BasicDynaBean packageBean, BasicDynaBean packageItem,
      BasicDynaBean headerInformation, String username, Integer centerId,
      BasicDynaBean mainChargeBean, Map<String, Object> packageItemDetails, Integer index) {
    BasicDynaBean otherOrderItemBean = getBean();
    setBeanValue(otherOrderItemBean, SERVICE_NAME, packageItem.get("activity_id"));
    setBeanValue(otherOrderItemBean, "pres_time", packageBean.get("presc_date"));
    setBeanValue(otherOrderItemBean, "doctor_id", packageBean.get("doctor_id"));
    setBeanValue(otherOrderItemBean, "remarks", packageBean.get("remarks"));
    setBeanValue(otherOrderItemBean, "service_group", packageItem.get("charge_head"));
    setBeanValue(otherOrderItemBean, "package_ref", packageBean.get("prescription_id"));
    setBeanValue(otherOrderItemBean, "mr_no", (String) headerInformation.get("mr_no"));
    setBeanValue(otherOrderItemBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(otherOrderItemBean, "user_name", username);
    String patientId = (String) headerInformation.get("patient_id");
    setBeanValue(otherOrderItemBean, "patient_id", patientId);
    setBeanValue(otherOrderItemBean, PRESCRIBED_ID, otherOrderItemRepository.getNextSequence());

    otherOrderItemRepository.insert(otherOrderItemBean);
    String packageChargeID = mainChargeBean != null ? (String) mainChargeBean.get("charge_id")
        : null;
    if (packageChargeID != null) {
      insertPackageBillActivityCharge(otherOrderItemBean, headerInformation, centerId,
          packageChargeID, null);
    }
  }

}
