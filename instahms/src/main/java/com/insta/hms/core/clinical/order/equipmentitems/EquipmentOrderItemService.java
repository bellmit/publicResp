package com.insta.hms.core.clinical.order.equipmentitems;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillActivityChargeService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.ordersets.OrderSetsOrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.equipment.EquipmentRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Order(key = "Equipment", value = { "Equipment" }, prefix = "equipments")
public class EquipmentOrderItemService extends OrderItemService {

  private static final String ACTIVITY_CODE = "EQU";
  private static final String PRESCRIBED_ID = "prescribed_id";
  private static final String DOCTOR_ID = "doctor_id";
  private static String prefix = "equipments";
  private static final String CHARGE_ID = "charge_id";

  @Autowired
  private EquipmentOrderItemRepository equipmentOrderItemRepository;

  @LazyAutowired
  private EquipmentRepository equipmentRepository;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private OrderSetsOrderItemService orderSetsOrderItemService;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  public EquipmentOrderItemService(EquipmentOrderItemRepository repository) {
    super(repository, "equipment", "eq_id");
  }

  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    Timestamp from = (Timestamp) orderBean.get("used_from");
    Timestamp to = (Timestamp) orderBean.get("used_till");
    String units = (String) orderBean.get("units");
    setBeanValue(orderBean, "duration", getDuration(from, to, units));
    setBeanValue(orderBean, "patient_id", headerInformation.get("patient_id"));
    setOperationRef(orderBean, null);
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");

    BasicDynaBean masterCharge = getMasterChargesBean(paramMap.get("id"), bedType, ratePlanId,
        null);

    return getChargesList(masterCharge, (BigDecimal) paramMap.get("quantity"), isInsurance, null,
        paramMap);
  }

  @Override
  public BasicDynaBean getMasterChargesBean(Object equipmentId, String bedType, String ratePlanId,
      Integer centerId) {
    BasicDynaBean equipment = equipmentRepository.getEquipmentChargesBean((String) equipmentId,
        bedType, ratePlanId);
    if (equipment == null) {
      equipment = equipmentRepository.getEquipmentChargesBean((String) equipmentId, "GENERAL",
          "ORG0001");
    }
    return equipment;
  }

  /**
   * Returns the list of equipments item details by passing there id.
   * @param entityIdList the entityIdList
   * @param paramMap the paramMap
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return equipmentOrderItemRepository.getItemDetails(entityIdList);
  }

  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object equipmentId, String ratePlanId) {
    return equipmentRepository.getAllEquipmentChargesBean((String) equipmentId,
        ratePlanId);
  }

  /**
   * Returns the list of equipment ordered for a given visit.
   * @param parameters the parameters
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    return equipmentOrderItemRepository.getOrderedItems((String) parameters.get("visit_id"),
        (Integer) parameters.get("operation_id"), (Boolean) parameters.get("package_ref"));
  }

  private Map<String, Object> calculateRateAndDiscount(BasicDynaBean equipment, BigDecimal quantity,
      String units, Timestamp fromDate, Timestamp toDate) {
    int duration = 0;
    BigDecimal rate = null;
    BigDecimal discount = null;
    int qty = 1;
    String unitsStr = "";
    Map<String, Object> valuesMap = new HashMap<String, Object>();
    if (units == null || units.equals("")) {
      units = "H";
    }
    if ((units.equals("D") || units.equals("Days"))) {
      /*
       * For Daily Charge, we put num days as qty, so that rate*qty-discount = amt is maintained.
       * This cannot be done for hourly charge.
       */
      rate = (BigDecimal) equipment.get("charge");
      discount = (BigDecimal) equipment.get("daily_charge_discount");
      if (fromDate == null) {
        qty = quantity.intValue(); // equipment supports only integer
        // quantities
      } else {
        qty = getDuration(fromDate, toDate, "D");
      }
      unitsStr = "Days";

    } else {
      /*
       * rate*qty-discount = amt must be maintained. So, we calculate the total charge as per
       * min/incr and put the rate as the total charge, set the qty=1. Note that we should not
       * display units as Hrs because it is not 1 hrs. The trade off is between maintaining
       * rate*qty-discount=amt vs. showing the correct amount of Hrs in the display. We choose the
       * former.
       */
      qty = 1;
      BigDecimal minRate = (BigDecimal) equipment.get("min_charge");
      BigDecimal minDiscount = (BigDecimal) equipment.get("min_charge_discount");
      BigDecimal slab1Rate = (BigDecimal) equipment.get("slab_1_charge");
      BigDecimal slab1Discount = (BigDecimal) equipment.get("slab_1_charge_discount");
      BigDecimal incrRate = (BigDecimal) equipment.get("incr_charge");
      BigDecimal incrDiscount = (BigDecimal) equipment.get("incr_charge_discount");

      int minDuration = ((BigDecimal) equipment.get("min_duration")).intValue();
      int slab1Duration = ((BigDecimal) equipment.get("slab_1_threshold")).intValue();
      int incrDuration = ((BigDecimal) equipment.get("incr_duration")).intValue();

      if (fromDate == null) {
        duration = quantity.intValue();
        unitsStr = "Hrs";
      } else {
        duration = getDuration(fromDate, toDate, "H",
            (Integer) equipment.get("duration_unit_minutes"));
        unitsStr = "";
      }

      rate = getDurationCharge(duration, minDuration, slab1Duration, incrDuration, minRate,
          slab1Rate, incrRate, false);
      discount = getDurationCharge(duration, minDuration, slab1Duration, incrDuration, minDiscount,
          slab1Discount, incrDiscount, false);
    }
    valuesMap.put("rate", rate);
    valuesMap.put("discount", discount);
    valuesMap.put("qty", qty);
    valuesMap.put("unitsStr", unitsStr);
    return valuesMap;
  }

  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean equipment, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {
    Timestamp fromDate = (Timestamp) otherParams.get("from_date");
    Timestamp toDate = (Timestamp) otherParams.get("to_date");
    String units = (String) otherParams.get("units");
    BigDecimal rate = BigDecimal.ZERO;
    Map<String, Object> valuesMap = calculateRateAndDiscount(equipment, quantity, units, fromDate,
        toDate);
    if ((Boolean.TRUE.equals(otherParams.get("multi_visit_package")))) {
      valuesMap.put("discount", BigDecimal.valueOf(Double.parseDouble((String)
          otherParams.get("discount"))));
    }
    if (otherParams.get("package_id") != null) {
      rate = (BigDecimal) equipment.get("charge");
    } else {
      rate = (BigDecimal) valuesMap.get("rate");
    }
    boolean isOperation = (Boolean) otherParams.get("is_operation");
    int serviceSubGroupId = (Integer) equipment.get("service_sub_group_id");
    BigDecimal discount = (BigDecimal) valuesMap.get("discount");
    int qty = (int) valuesMap.get("qty");

    Integer insuranceCategoryId;
    if (equipment.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) equipment.get("insurance_category_id");
    } else {
      insuranceCategoryId = 0;
    }
    discount = discount.multiply(new BigDecimal(qty));
    BasicDynaBean billChargeBean = billChargeService.setBillChargeBean(isOperation ? "OPE" : "OTC",
        isOperation ? "EQOPE" : "EQUOTC", rate, new BigDecimal(qty), discount,
        (String) equipment.get("equip_id"), (String) equipment.get("equipment_name"),
        (String) equipment.get("dept_id"), serviceSubGroupId, insuranceCategoryId, isInsurance);

    billChargeBean.set("act_rate_plan_item_code", equipment.get("equipment_code"));
    billChargeBean.set("allow_rate_increase", equipment.get("allow_rate_increase"));
    billChargeBean.set("allow_rate_decrease", equipment.get("allow_rate_decrease"));
    String unitsStr = (String) valuesMap.get("unitsStr");
    billChargeBean.set("act_unit", unitsStr);
    billChargeBean.set("from_date", fromDate);
    billChargeBean.set("to_date", toDate);
    if (equipment != null || equipment.get("billing_group_id") != null) {
      billChargeBean.set("billing_group_id", (Integer) equipment.get("billing_group_id"));
    }
    if (otherParams.get("package_id") != null) {
      billChargeBean.set("package_id", otherParams.get("package_id"));
    }
    List<BasicDynaBean> billChargeBeanList = new ArrayList<BasicDynaBean>();
    billChargeBeanList.add(billChargeBean);

    /*
     * HMS-22250 - According to this bug, Now we do not support service Tax. So instead of migrating
     * the item master, we are not considering in code.
     * 
     * BigDecimal taxPer = (BigDecimal) equipment.get("tax");
     * 
     * if (taxPer.compareTo(BigDecimal.ZERO) > 0) { BigDecimal taxAmount = rate.multiply(new
     * BigDecimal(qty)).subtract(discount).multiply(taxPer) .divide(new BigDecimal(100), 2);
     * 
     * BasicDynaBean taxCharge = billChargeService.setBillChargeBean("TAX", "STAX", taxAmount,
     * BigDecimal.ONE, BigDecimal.ZERO, null, "Service Tax (" + (String)
     * equipment.get("equipment_name") + ")", (String) equipment.get("dept_id"), serviceSubGroupId,
     * insuranceCategoryId, isInsurance); taxCharge.set("from_date", fromDate);
     * taxCharge.set("to_date", toDate); taxCharge.set("allow_rate_increase",
     * equipment.get("allow_rate_increase")); taxCharge.set("allow_rate_decrease",
     * equipment.get("allow_rate_decrease")); taxCharge.set("act_rate_plan_item_code", null);
     * l.add(taxCharge); }
     */

    return billChargeBeanList;
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) throws ParseException {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIBED_ID, item.get("order_id"));
    bean.set("remarks", item.get("remarks"));
    bean.set(DOCTOR_ID, item.get("prescribed_doctor_id"));
    bean.set("used_from", DateUtil.parseTimestamp((String) item.get("start_date_date"),
        (String) item.get("start_date_time")));
    bean.set("used_till", DateUtil.parseTimestamp((String) item.get("end_date_date"),
        (String) item.get("end_date_time")));
    bean.set("finalization_status", "F".equals(item.get("finalized")) ? "F" : "N");
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
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) throws ParseException {
    super.setItemBeanProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList, operationBean);

    setBeanValue(orderBean, "patient_id", headerInformation.get("patient_id"));
    setBeanValue(orderBean, "eq_id", orderedItemList.get("item_id"));
    setBeanValue(orderBean, "date",
        DateUtil.parseTimestamp((String) orderedItemList.get("posted_date")));
    setBeanValue(orderBean, PRESCRIBED_ID, equipmentOrderItemRepository.getNextSequence());
    Timestamp from = DateUtil.parseTimestamp((String) orderedItemList.get("start_date_date"),
        (String) orderedItemList.get("start_date_time"));
    Timestamp to = DateUtil.parseTimestamp((String) orderedItemList.get("end_date_date"),
        (String) orderedItemList.get("end_date_time"));
    setBeanValue(orderBean, "used_from", from);
    setBeanValue(orderBean, "used_till", to);
    String units = (String) orderBean.get("units");
    setBeanValue(orderBean, "duration", getDuration(from, to, units));
    setBeanValue(orderBean, "finalization_status",
        "N".equals(orderedItemList.get("finalized")) ? "N" : "F");
    setBeanValue(orderBean, DOCTOR_ID, orderedItemList.get("prescribed_doctor_id"));
    setPackageRef(orderBean, isMultiVisitPackItem, (Integer) headerInformation.get("packageref"));
    setOperationRef(orderBean, operationBean);
    return orderBean;
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
    BasicDynaBean orderedItemBean = equipmentOrderItemRepository.getBean();

    ConversionUtils.copyJsonToDynaBean(item, orderedItemBean, errorList, true);

    if (!errorList.isEmpty()) {
      throw new ConversionException(errorList);
    }
    Object mvp = item.get("multi_visit_package");
    Boolean isMultivisitPackage = mvp != null && !"".equals(mvp) ? Boolean.valueOf(mvp.toString())
        : false;
    if (isMultivisitPackage) {
      orderedItemBean.set("units", null);
    }
    orderedItemAuths.get("isMultivisitPackageList").add(isMultivisitPackage);

    setItemBeanProperties(orderedItemBean, headerInformation, username, isMultivisitPackage, item,
        null);

    return orderedItemBean;
  }

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    Map<String, Object> keys = new HashMap<>();
    List<Object> prescribedIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescribedIdsList.add(item.get(PRESCRIBED_ID));
    }
    keys.put(PRESCRIBED_ID, prescribedIdsList);
    return equipmentOrderItemRepository.batchUpdate(items, keys);
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    // For equiments there is no update.

    // Get all equipments orders Finalization status.
    List<Integer> prescribedIdsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      prescribedIdsList.add((Integer) item.get(PRESCRIBED_ID));
    }

    List<BasicDynaBean> beans = equipmentOrderItemRepository
        .getFinalizationStatus(prescribedIdsList);
    itemInfoMap.put("before_update_beans", beans);
  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {

    for (BasicDynaBean orderBean : orders) {
      updateFinalizableItemCharges(orderBean, itemInfoMap);
    }

    return true;
  }

  /**
   * update finalizable item charges.
   * @param orderBean the orderBean
   * @param itemInfoMap the itemInfoMap
   * @return boolean
   */
  public boolean updateFinalizableItemCharges(BasicDynaBean orderBean,
      Map<String, Object> itemInfoMap) {
    /*
     * Get the current charge and bill details of the existing order
     */
    Integer prescId = (Integer) orderBean.get(PRESCRIBED_ID);

    // These are set in updateCancelStatusAndRefOrders.
    List<BasicDynaBean> oldBeans = (List<BasicDynaBean>) itemInfoMap.get("before_update_beans");
    String finalizationStatus = "";

    BasicDynaBean equipmentBean = null;
    String billNo = null;
    for (BasicDynaBean item : oldBeans) {
      if (prescId.equals(item.get(PRESCRIBED_ID))) {
        equipmentBean = item;
        finalizationStatus = (String) item.get("finalization_status");
        billNo = (String) item.get("bill_no");
        break;
      }
    }

    BasicDynaBean headerInformation = null;

    Map<String, Object> billsInfoMap = (Map<String, Object>) itemInfoMap.get("billsInfoMap");
    if ("F".equals(finalizationStatus) || equipmentBean == null || billsInfoMap == null
        || billsInfoMap.get(billNo) == null) {
      return false;
    } else {
      headerInformation = (BasicDynaBean) ((Map<String, Object>) billsInfoMap.get(billNo))
          .get("header_information");
      if (headerInformation == null || headerInformation.get("bill_no") == null) {
        return false;
      }
    }

    BasicDynaBean curCharge = billActivityChargeService.getCharge(ACTIVITY_CODE,
        String.valueOf(prescId));
    BasicDynaBean bill = billService.getBillRatePlanAndBedType((String) curCharge.get("bill_no"));

    Timestamp from = (Timestamp) orderBean.get("used_from");
    Timestamp to = (Timestamp) orderBean.get("used_till");

    BasicDynaBean equipDetails = getMasterChargesBean(equipmentBean.get("eq_id"),
        (String) bill.get("bed_type"), (String) bill.get("bill_rate_plan_id"), null);

    Map<String, Object> params = new HashMap<>();
    params.put("from_date", from);
    params.put("to_date", to);
    params.put("units", equipmentBean.get("units"));
    params.put("is_operation", false);
    List<BasicDynaBean> newCharges = getChargesList(equipDetails, BigDecimal.ONE,
        (Boolean) headerInformation.get("is_tpa"), null, params);

    /*
     * The main charge must already exist, just update it.
     */
    BasicDynaBean newMainCharge = newCharges.get(0);
    billChargeService.copyChargeAmounts(newMainCharge, curCharge, true);
    curCharge.set("act_remarks",
        DateUtil.formatTimestamp(from) + " to " + DateUtil.formatTimestamp(to));
    Map<String, Object> keys = new HashMap<>();
    keys.put(CHARGE_ID, curCharge.get(CHARGE_ID));
    billChargeService.update(curCharge, keys);

    /*
     * The referenced charge (tax) may or may not exist. If it exists, update, else insert a new
     * one. Other way round, cancel the existing charge.
     */
    String chargeId = (String) curCharge.get(CHARGE_ID);
    List<BasicDynaBean> curChargeRefs = billChargeService.getChargeReferences(chargeId);
    BasicDynaBean newTaxCharge = newCharges.size() > 1 ? newCharges.get(1) : null;
    BasicDynaBean curTaxCharge = !curChargeRefs.isEmpty() ? curChargeRefs.get(0) : null;

    if (newTaxCharge != null && curTaxCharge != null) {
      billChargeService.copyChargeAmounts(newTaxCharge, curTaxCharge, true);
      keys.put(CHARGE_ID, curTaxCharge.get(CHARGE_ID));
      billChargeService.update(curTaxCharge, keys);
    } else if (newTaxCharge != null) {
      setOrderAttributes(newTaxCharge, billChargeService.getNextPrefixedId(),
          (String) curCharge.get("bill_no"), (String) itemInfoMap.get("user_name"), "", "",
          (Timestamp) curCharge.get("posted_date"));

      newTaxCharge.set("charge_ref", curCharge.get(CHARGE_ID));
      newTaxCharge.set("has_activity", true);
      billChargeService.insert(newTaxCharge);
    } else if (curTaxCharge != null) {
      billChargeService.cancelBillCharge(chargeId, false, (String) itemInfoMap.get("user_name"));
    }

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
    return DOCTOR_ID;
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

    List<BasicDynaBean> itemList = getOrderItemBeanList(itemsMapsList, billItemAuthMap,
        headerInformation, preAuthIds, preAuthModeIds, username, null);

    equipmentOrderItemRepository.batchInsert(itemList);

    List<Object> ismvpList = billItemAuthMap.get("isMultivisitPackageList");
    List<Object> condDocList = billItemAuthMap.get("conductingDoctorList");
    for (int index = 0; index < itemList.size(); index++) {
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> equipmentItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean equipmentBean = itemList.get(index);

      Boolean isMultivisitPackage = (Boolean) ismvpList.get(index);
      if (!condDocList.isEmpty() && index < condDocList.size()) {
        condDoctorId = (String) condDocList.get(index);
      }
      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      BasicDynaBean mainChargeBean = null;
      if (null != bill) {
        List<BasicDynaBean> chargesList = insertOrderItemCharges(chargeable, headerInformation,
            equipmentBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, condDoctorId,
            ACTIVITY_CODE, centerId, isMultivisitPackage, equipmentItemDetails);
        mainChargeBean = chargesList == null ? null : chargesList.get(0);
      }

      String packageIdString = equipmentItemDetails.get("package_id") != null
          ? equipmentItemDetails.get("package_id").toString() : null;
      Integer packageId = packageIdString != null && !"".equals(packageIdString)
          ? Integer.parseInt(packageIdString) : null;
      String packageObIdString = equipmentItemDetails.get("package_ob_id") != null
          ? equipmentItemDetails.get("package_ob_id").toString() : null;
      Integer packObId = packageObIdString != null && !"".equals(packageObIdString)
          ? Integer.parseInt(packageObIdString) : null;
      Boolean isMultiVisitPackItem = (Boolean) equipmentItemDetails.get("multi_visit_package");
      Integer patientPackageId = null;
      if (patientPackageIdsList != null && patientPackageIdsList.size() >= index) {
        patientPackageId = (Integer) patientPackageIdsList.get(index);
      }
      String chargeId = mainChargeBean != null ? (String) mainChargeBean.get(CHARGE_ID) : null;
      if ((packageId != null) && (packObId != null) && (patientPackageId != null)
          && !isMultiVisitPackItem) {
        orderSetsOrderItemService.insertIntoPatientPackageContentAndConsumed(patientPackageId,
            packObId, packageId, (String) equipmentBean.get("eq_id"), 1,
            (Integer) equipmentBean.get(PRESCRIBED_ID), chargeId, username, "equipments");
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
    String bedType = (String) headerInformation.get("bed_type");
    String ratePlanId = (String) headerInformation.get("bill_rate_plan_id");
    Boolean isInsurance = (Boolean) bill.get("is_tpa");

    BasicDynaBean masterCharge = getMasterChargesBean((Object) orderBean.get("eq_id"), bedType,
        ratePlanId, centerId);

    boolean condApplicable;
    if (masterCharge.getDynaClass().getDynaProperty("conduction_applicable") != null) {
      condApplicable = (Boolean) masterCharge.get("conduction_applicable");
    } else {
      condApplicable = false;
    }

    List<BasicDynaBean> chargesList = null;
    if (chargeable) {
      BigDecimal quantity = BigDecimal.ONE;
      if (null != orderBean.getDynaClass().getDynaProperty("quantity")
          && !((orderBean.get("quantity")).equals(""))) {
        quantity = (BigDecimal) orderBean.get("quantity");
      }
      BigDecimal charge = BigDecimal.ZERO;
      BigDecimal discount  = BigDecimal.ZERO;
      if (null != isMultivisitPackage && isMultivisitPackage) {
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
      otherParams.put("is_operation", orderBean.get("operation_ref") != null);
      otherParams.put("to_date", orderBean.get("used_till"));
      otherParams.put("from_date", orderBean.get("used_from"));
      otherParams.put("multi_visit_package", orderItemDetails.get("multi_visit_package"));
      otherParams.put("discount", orderItemDetails.get("discount"));
      if (orderItemDetails.get("package_id") != null) {
        otherParams.put("package_id", orderItemDetails.get("package_id"));
      }
      otherParams.put("units", orderBean.get("units"));
      chargesList = getChargesList(masterCharge, quantity, isInsurance, condDoctorId, otherParams);
      if (null != isMultivisitPackage && isMultivisitPackage && !chargesList.isEmpty()) {
        BasicDynaBean equiCharge = chargesList.get(0);
        equiCharge.set("discount", discount);
        equiCharge.set("amount", (charge.multiply(quantity)).subtract(discount));
        if (discount.compareTo(BigDecimal.ZERO) != 0) {
          equiCharge.set("overall_discount_amt", discount);
          equiCharge.set("overall_discount_auth", -1);
        } else {
          equiCharge.set("overall_discount_auth", null);
          equiCharge.set("overall_discount_amt", null);
        }
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds,
            (Integer) orderBean.get(PRESCRIBED_ID), (String) orderBean.get(DOCTOR_ID),
            new Timestamp(postedDate.getTime()));
      } else {
        insertOrderBillCharges(chargesList, activityCode, condApplicable ? "N" : "Y", null,
            preAuthIds, preAuthModeIds, bill, orderBean, planIds,
            (Integer) orderBean.get(PRESCRIBED_ID), (String) orderBean.get(DOCTOR_ID),
            (Timestamp) orderBean.get("date"));
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
              orderItemDetails.get("quantity"), chargeId,
              (Integer) orderBean.get(PRESCRIBED_ID),
              orderItemDetails.get("type"));
        }
      }
    }

    return chargesList;
  }

  /**
   * Find by key.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String key, Object value) {
    return equipmentOrderItemRepository.findByKey(key, value);
  }

}
