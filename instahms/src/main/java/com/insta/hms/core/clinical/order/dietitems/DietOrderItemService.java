package com.insta.hms.core.clinical.order.dietitems;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.core.clinical.prescriptions.PatientDietPrescriptionsService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;

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
@Order(key = "Meal", value = { "Meal" }, prefix = "meals")
public class DietOrderItemService extends OrderItemService {

  private static final String ACTIVITY_CODE = "DIE";
  private static final String PRESCRIPTION_TABLE_PRIMARY_KEY = "ordered_id";
  private static final String PRESCRIPTION_DOCTOR_KEY = "ordered_by";

  @Autowired
  private DietOrderItemRepository dietOrderItemRepository;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private PatientDietPrescriptionsService patientDietPrescriptionsService;
  
  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  public DietOrderItemService(DietOrderItemRepository repository) {
    super(repository, "diet", "diet_id");
  }

  @Override
  public void setHeaderProperties(BasicDynaBean orderBean, BasicDynaBean headerInformation,
      String username, boolean isMultiVisitPackItem, Map orderedItemList) {

    super.setHeaderProperties(orderBean, headerInformation, username, isMultiVisitPackItem,
        orderedItemList);
    orderBean.set("visit_id", headerInformation.get("patient_id"));
    orderBean.set("common_order_id", headerInformation.get("commonOrderId"));
    // orderBean.set("ordered_id", DataBaseUtil.getNextSequence("diet_prescribed_seq"));
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

  @Override
  public BasicDynaBean getMasterChargesBean(Object dietId, String bedType, String ratePlanId,
      Integer centerId) {
    return dietOrderItemRepository.getDietChargesBean(Integer.parseInt(dietId.toString()), bedType,
        ratePlanId);
  }

  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object dietId, String ratePlanId) {
    return dietOrderItemRepository.getAllDietChargesBean(Integer.parseInt(dietId.toString()),
        ratePlanId);
  }

  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean diet, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {
    Integer insuranceCategoryId = 0;
    if (diet.get("insurance_category_id") != null) {
      insuranceCategoryId = (Integer) diet.get("insurance_category_id");
    }
    Map<String, Object> chargeHeadMap = new HashMap<String, Object>();
    chargeHeadMap.put("chargehead_id", "MDIE");
    BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(chargeHeadMap);
    BasicDynaBean billChargeBean = billChargeService.setBillChargeBean("DIE", "MDIE",
        (BigDecimal) diet.get("charge"), quantity,
        ((BigDecimal) diet.get("discount")).multiply(quantity), diet.get("diet_id").toString(),
        (String) diet.get("meal_name"), null, (Integer) diet.get("service_sub_group_id"),
        insuranceCategoryId, isInsurance);
    billChargeBean.set("allow_rate_increase", (Boolean) chargeHeadBean.get("allow_rate_increase"));
    billChargeBean.set("allow_rate_decrease", (Boolean) chargeHeadBean.get("allow_rate_decrease"));
    List<BasicDynaBean> chargesList = new ArrayList<BasicDynaBean>();
    if (diet != null || diet.get("billing_group_id") != null) {
      billChargeBean.set("billing_group_id", (Integer) diet.get("billing_group_id"));
    }
    chargesList.add(billChargeBean);
    
    /*
     * HMS-22250 - According to this bug, Now we do not support service Tax. So instead of 
     * migrating the item master, we are not considering in code.
     * 
    BigDecimal taxPer = (BigDecimal) diet.get("service_tax");
    if (taxPer != null && taxPer.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal mealCharge = (BigDecimal) diet.get("charge");
      BigDecimal taxAmount = taxPer.multiply(mealCharge).multiply(quantity)
          .divide(new BigDecimal("100"), 2);
      BasicDynaBean taxCharge = billChargeService.setBillChargeBean("TAX", "STAX", taxAmount,
          BigDecimal.ONE, BigDecimal.ZERO, null,
          "Service Tax (" + (String) diet.get("meal_name") + ")", null,
          (Integer) diet.get("service_sub_group_id"), insuranceCategoryId, isInsurance);
      taxCharge.set("allow_rate_increase", false);
      taxCharge.set("allow_rate_decrease", false);
      chargesList.add(taxCharge);
    }
    */
    
    return chargesList;
  }

  /**
   * Returns the list of diet item details by passing there id.
   * @param entityIdList the entityIdList
   * @param paramMap the paramMap
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return dietOrderItemRepository.getItemDetails(entityIdList);
  }

  /**
   * Returns the list of diet order items ordered for a given visit.
   * @param parameters the parameters
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {

    if (parameters.get("operation_id") != null) {
      return Collections.emptyList();
    }
    return dietOrderItemRepository.getOrderedItems((String) parameters.get("visit_id"),
        (Boolean) parameters.get("package_ref"));
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    BasicDynaBean bean = getBean();
    bean.set(PRESCRIPTION_TABLE_PRIMARY_KEY, item.get("order_id"));
    bean.set("special_instructions", item.get("remarks"));
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
    bean.set(PRESCRIPTION_TABLE_PRIMARY_KEY, orderId);
    bean.set("status", "X");
    bean.set("status_updated_by", sessionService.getSessionAttributes().get("userId"));
    bean.set("status_updated_time", DateUtil.getCurrentTimestamp());
    return bean;
  }

  @Override
  public BasicDynaBean setItemBeanProperties(BasicDynaBean orderBean,
      BasicDynaBean headerInformation, String username, boolean isMultiVisitPackItem,
      Map<String, Object> orderedItemList, BasicDynaBean operationBean) throws ParseException {

    setBeanValue(orderBean, "common_order_id", headerInformation.get("commonorderid"));
    setBeanValue(orderBean, "user_name", username);
    setBeanValue(orderBean, "visit_id", headerInformation.get("patient_id"));
    setBeanValue(orderBean, PRESCRIPTION_TABLE_PRIMARY_KEY,
        dietOrderItemRepository.getNextSequence());
    setBeanValue(orderBean, "diet_id", new BigDecimal(orderedItemList.get("item_id").toString()));
    setBeanValue(orderBean, PRESCRIPTION_DOCTOR_KEY, orderedItemList.get("prescribed_doctor_id"));
    setBeanValue(orderBean, "special_instructions", orderedItemList.get("remarks"));

    setBeanValue(orderBean, "ordered_time",
        DateUtil.parseTimestamp((String) orderedItemList.get("prescribed_date_date"),
            (String) orderedItemList.get("prescribed_date_time")));

    setBeanValue(orderBean, "meal_time",
        DateUtil.parseTime((String) orderedItemList.get("start_date_time")));

    setBeanValue(orderBean, "meal_date",
        DateUtil.parseDate((String) orderedItemList.get("start_date_date")));
    setBeanValue(orderBean, "diet_pres_id", !"".equals(orderedItemList.get("doc_presc_id"))
        ? orderedItemList.get("doc_presc_id") : null);
    setBeanValue(orderBean, "status", "N");

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

    orderedItemAuths.get("conductingDoctorList").add((String) item.get("payee_doctor_id"));

    List errorList = new ArrayList();
    BasicDynaBean orderedItemBean = dietOrderItemRepository.getBean();

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
      prescriptionIdsList.add(item.get(PRESCRIPTION_TABLE_PRIMARY_KEY));
    }
    keys.put(PRESCRIPTION_TABLE_PRIMARY_KEY, prescriptionIdsList);
    return dietOrderItemRepository.batchUpdate(items, keys);
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    // For diet items there is no update.

  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // For diet items there is no update.
    return true;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    return PRESCRIPTION_TABLE_PRIMARY_KEY;
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
  public String getOrderItemRemarks(BasicDynaBean item) {
    return (String) item.get("special_instructions");
  }

  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageidsList) throws ParseException {
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

    dietOrderItemRepository.batchInsert(itemList);

    List<Object> ismvpList = billItemAuthMap.get("isMultivisitPackageList");
    List<Object> condDocList = billItemAuthMap.get("conductingDoctorList");
    for (int index = 0; index < itemList.size(); index++) {
      String condDoctorId = null;
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      Map<String, Object> dietItemDetails = (Map<String, Object>) itemsMapsList.get(index);
      BasicDynaBean dietBean = itemList.get(index);

      Boolean isMultivisitPackage = (Boolean) ismvpList.get(index);
      if (!condDocList.isEmpty() && index < condDocList.size()) {
        condDoctorId = (String) condDocList.get(index);
      }
      if (dietItemDetails.get("doc_presc_id") != null
          && !"".equals(dietItemDetails.get("doc_presc_id"))) {
        updatePrescription((Integer) dietItemDetails.get("doc_presc_id"));
      }

      setPreAuthIdAndMode(finalPreAuthIds, finalPreAuthModeIds, billItemAuthMap, index);
      if (null != bill) {
        insertOrderItemCharges(chargeable, headerInformation, dietBean, bill, finalPreAuthIds,
            finalPreAuthModeIds, planIds, condDoctorId, ACTIVITY_CODE, centerId,
            isMultivisitPackage, dietItemDetails);
      }
    }
    return itemList;
  }

  /**
   * update Prescription.
   * @param presId the presId
   */
  public void updatePrescription(Integer presId) {
    BasicDynaBean dietPrescriptionsBean = patientDietPrescriptionsService.getBean();
    dietPrescriptionsBean.set("diet_pres_id", presId);
    dietPrescriptionsBean.set("added_to_bill", true);
    Map<String, Object> patientPrescKey = new HashMap<>();
    patientPrescKey.put("diet_pres_id", presId);
    patientDietPrescriptionsService.update(dietPrescriptionsBean, patientPrescKey);
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

    List<BasicDynaBean> chargesList = null;
    if (chargeable) {
      BasicDynaBean masterCharge = getMasterChargesBean((Object) orderBean.get("diet_id"), bedType,
          ratePlanId, centerId);
      chargesList = getChargesList(masterCharge, BigDecimal.ONE, isInsurance, condDoctorId, null);
      if (isMultivisitPackage) {
        for (BasicDynaBean charge : chargesList) {
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id",
              (Integer) charge.get("package_id"));
          charge.set("allow_rate_increase", 
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          charge.set("allow_rate_decrease", 
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
        }
      }
      if (orderItemDetails != null &&  orderItemDetails.get("posted_date") != null 
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, "Y", null, preAuthIds, preAuthModeIds,
            bill, orderBean, planIds, 
            (Integer) orderBean.get(PRESCRIPTION_TABLE_PRIMARY_KEY),
            (String) orderBean.get(PRESCRIPTION_DOCTOR_KEY), 
            new Timestamp(postedDate.getTime()),bedType);
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y", null, preAuthIds, preAuthModeIds,
            bill, orderBean, planIds, (Integer) orderBean.get(PRESCRIPTION_TABLE_PRIMARY_KEY),
            (String) orderBean.get(PRESCRIPTION_DOCTOR_KEY),
            DateUtil.parseTimestamp((String) orderItemDetails.get("prescribed_date_date"),
                (String) orderItemDetails.get("prescribed_date_time")),
            (String) orderBean.get("special_instructions"));
      }
    }

    return chargesList;
  }

}
