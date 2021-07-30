package com.insta.hms.core.clinical.order.ordersets;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.mdm.ordersets.OrderSetsService;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedRepository;
import com.insta.hms.mdm.packages.PatientPackageContentsRepository;
import com.insta.hms.mdm.packages.PatientPackageRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Order(key = "Order Sets", value = { "Order Sets" })
public class OrderSetsOrderItemService extends OrderItemService {

  private OrderSetsOrderItemRepository orderSetsOrderItemRepository;

  public OrderSetsOrderItemService(OrderSetsOrderItemRepository repository) {
    super(repository, "", "");
    orderSetsOrderItemRepository = repository;
  }

  @LazyAutowired
  private PatientPackageRepository patientPackageRepo;

  @LazyAutowired
  private PatientPackageContentConsumedRepository patPackContentConsumedRepo;

  @LazyAutowired
  private PatientPackageContentsRepository patPackContentsRepo;

  @LazyAutowired
  private OrderSetsService orderSetsService;

  @Override
  public BasicDynaBean getMasterChargesBean(Object prefixId, String bedType, String ratePlanId,
      Integer centerId) {
    return null;
  }
  
  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object prefixId, String ratePlanId) {
    return null;
  }

  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    String schedulable = null;
    if (paramMap.get("orderset_schedulable") != null
        && !paramMap.get("orderset_schedulable").equals("")) {
      schedulable = (String) paramMap.get("orderset_schedulable");
    }
    return orderSetsOrderItemRepository.getItemDetails(entityIdList,
        (String) paramMap.get("dept_id"), (Integer) paramMap.get("center_id"),
        (String) paramMap.get("gender_applicability"), (String) paramMap.get("date"),schedulable);
  }

  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams)
      throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicDynaBean toItemBean(Map<String, Object> item, BasicDynaBean headerInformation,
      String username, Map<String, List<Object>> orderedItemAuths, String[] preAuthIds,
      Integer[] preAuthModeIds, BasicDynaBean operationBean) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getOrderItemPrimaryKey() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getOrderItemActivityCode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getPrescriptionDocKey() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<BasicDynaBean> insertOrders(List<Object> itemsMapsList, Boolean chargeable,
      Map<String, List<Object>> billItemAuthMap, Map<String, Object> billInfoMap,
      List<Object> patientPackageidsList) throws ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Insert order sets into patient packages.
   *
   * @param billWiseOrders
   *          the bill wise orders
   * @param billWisePatientPackageIdsMap
   *          the bill wise patient package ids map
   * @param billsInfoMap
   *          the bills info map
   */
  public void insertOrderSetsIntoPatientPackages(
      Map<String, Map<String, List<Object>>> billWiseOrders,
      Map<String, Map<String, List<Object>>> billWisePatientPackageIdsMap,
      Map<String, Map<String, Object>> billsInfoMap) {
    Map<Integer, Integer> allPatientPackageIdsMap = new HashMap<>();
    for (Map.Entry<String, Map<String, List<Object>>> orderItemsEntry : billWiseOrders.entrySet()) {
      Map<String, Object> billInfoMap = billsInfoMap.get(orderItemsEntry.getKey());
      if (billInfoMap == null) {
        continue;
      }
      BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
      Map<String, List<Object>> billPatientPackageIdsMap = new HashMap<>();
      insertOrderSetsIntoPatientPackages(orderItemsEntry.getValue(), billPatientPackageIdsMap,
          allPatientPackageIdsMap, (String) headerInformation.get("mr_no"));
      billWisePatientPackageIdsMap.put(orderItemsEntry.getKey(), billPatientPackageIdsMap);
    }

  }

  /**
   * Insert order sets into patient packages.
   *
   * @param orderItems
   *          the order items
   * @param patientPackageIdsMap
   *          the patient package ids map
   * @param allPatientPackageIdsMap
   *          the all patient package ids map. This map is needed if the function is called when
   *          contents of order sets are present in multiple bills.
   * @param mrNo
   *          the mr no
   */
  public void insertOrderSetsIntoPatientPackages(Map<String, List<Object>> orderItems,
      Map<String, List<Object>> patientPackageIdsMap, Map<Integer, Integer> allPatientPackageIdsMap,
      String mrNo) {
    if (allPatientPackageIdsMap == null) {
      allPatientPackageIdsMap = new HashMap<>();
    }
    for (Map.Entry<String, List<Object>> itemsEntry : orderItems.entrySet()) {
      String key = itemsEntry.getKey();
      for (Object item : itemsEntry.getValue()) {
        Map<String, Object> itemMap = (Map<String, Object>) item;
        Object orderId = itemMap.get("order_id");
        if (orderId != null && !"".equals(orderId)) {
          continue;
        }
        String packageIdString = itemMap.get("package_id") != null
            ? itemMap.get("package_id").toString() : null;
        Integer packageId = packageIdString != null && !"".equals(packageIdString)
            ? Integer.parseInt(packageIdString) : null;
        String packageObIdString = itemMap.get("package_ob_id") != null
            ? itemMap.get("package_ob_id").toString() : null;
        Integer packageObId = packageObIdString != null && !"".equals(packageObIdString)
            ? Integer.parseInt(packageObIdString) : null;
        Boolean isMultiVisitPackage = (Boolean) itemMap.get("multi_visit_package");
        Integer patPackageId = null;
        if (packageId != null && packageObId != null && !isMultiVisitPackage) {
          patPackageId = allPatientPackageIdsMap.get(packageId);
          if (patPackageId == null) {
            BasicDynaBean bean = patientPackageRepo.getBean();
            patPackageId = patientPackageRepo.getNextSequence();
            bean.set("patient_package_id", patPackageId);
            bean.set("mr_no", mrNo);
            bean.set("package_id", packageId);
            bean.set("status", "C");
            patientPackageRepo.insert(bean);
            allPatientPackageIdsMap.put(packageId, patPackageId);
          }
        }
        List<Object> patientPackagesList = patientPackageIdsMap.get(key);
        if (patientPackagesList == null) {
          patientPackagesList = new ArrayList<>();
          patientPackageIdsMap.put(key, patientPackagesList);
        }
        patientPackagesList.add(patPackageId);
      }
    }
  }

  /**
   * Insert into patient package content and consumed.
   *
   * @param patientPackageId
   *          the patient package id
   * @param packageContentId
   *          the package content id
   * @param packageId
   *          the package id
   * @param activityId
   *          the activity id
   * @param consumedQuantity
   *          the consumed quantity
   * @param prescriptionId
   *          the prescription id
   * @param chargeId
   *          the charge id
   * @param username
   *          the username
   * @param itemType
   *          the item type
   */
  public void insertIntoPatientPackageContentAndConsumed(Integer patientPackageId,
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

}
