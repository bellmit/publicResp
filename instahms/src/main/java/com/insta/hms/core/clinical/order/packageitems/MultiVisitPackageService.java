package com.insta.hms.core.clinical.order.packageitems;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.packages.PatientPackageContentConsumedRepository;
import com.insta.hms.mdm.packages.PatientPackagesRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Based on Item Type , it returns the multiVisit item charges. Charges Returned if for ONE
 * quantity.
 * 
 * @author ritolia
 *
 */
@Service
public class MultiVisitPackageService {

  private static final String PATIENT_PACKAGE_ID = "pat_package_id";
  private static final String MR_NO = "mr_no";
  private static final String PACKAGE_ID = "package_id";
  private static final String MULTI_VISIT_PACKAGE = "multi_visit_package";

  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  @LazyAutowired
  private PatientPackagesRepository patientPackagesRepository;

  @LazyAutowired
  private PackageOrderItemService packageOrderItemService;

  @LazyAutowired
  private PackageOrderItemRepository packageOrderItemRepository;

  @LazyAutowired
  private PackagesService packagesService;

  @LazyAutowired
  private PatientPackageContentConsumedRepository patientPackageContentConsumedRepository;

  /**
   * Gets the multi visit package item charge based on item type. returns the unit charge for only
   * ONE quantity.
   *
   * @param packageId the package id
   * @param itemBean  the item bean
   * @param bedType   the bed type
   * @param orgId     the ratePlan id
   * @param itemType  the item type
   * @return the multi visit package item charge
   */
  public BigDecimal getMultiVisitPackageItemCharge(Integer packageId, BasicDynaBean itemBean,
      String bedType, String orgId, String itemType) {

    BigDecimal quantity = BigDecimal.ONE;
    BigDecimal charge = BigDecimal.ZERO;

    if (("tests").equals(itemType)) {
      charge = multiVisitRepository.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("test_id"), bedType, orgId, itemType, quantity);
    } else if (("services").equals(itemType)) {
      charge = multiVisitRepository.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("service_id"), bedType, orgId, itemType, quantity);
    } else if (("others").equals(itemType)) {
      charge = multiVisitRepository.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("service_name"), bedType, orgId, itemType, quantity);
    } else if (("doctors").equals(itemType)) {
      charge = multiVisitRepository.getMultiVisitPackageItemCharge(packageId, itemBean,
          (String) itemBean.get("head"), bedType, orgId, itemType, quantity);
    }

    return charge;
  }

  /**
   * Get the patientPackageBean by either fetching an existing record or by constructing a new one.
   *
   * @param patientPackageId the patientPackageId
   * @param mrNo the mrNo
   * @param packageId the packageId
   * @return BasicDynaBean
   */
  public BasicDynaBean getPatientPackageBean(Integer patientPackageId,
                                              Object mrNo, Object packageId) {
    if (patientPackageId == null) {
      BasicDynaBean patientPackageBean = patientPackagesRepository.getBean();
      patientPackageBean.set(PATIENT_PACKAGE_ID, DatabaseHelper.getNextSequence("patient_package"));
      patientPackageBean.set(MR_NO, mrNo);
      patientPackageBean.set(PACKAGE_ID, packageId);
      patientPackageBean.set("status", "P");
      return patientPackageBean;
    }
    Map<String, Integer> filterMap = new HashMap<>();
    filterMap.put(PATIENT_PACKAGE_ID, patientPackageId);
    return patientPackagesRepository.findByPk(filterMap);
  }

  /**
   * Get the patientPackageBean by either fetching an existing record or by constructing a new one.
   *
   * @param mrNo the mrNo
   * @param packageId the packageId
   * @return BasicDynaBean
   */
  public BasicDynaBean insertPatientPackageBean(Object mrNo, Object packageId) {
    BasicDynaBean patientPackageBean = patientPackagesRepository.getBean();
    patientPackageBean.set(PATIENT_PACKAGE_ID, DatabaseHelper.getNextSequence("patient_package"));
    patientPackageBean.set(MR_NO, mrNo);
    patientPackageBean.set(PACKAGE_ID, packageId);
    patientPackageBean.set("status", "P");
    patientPackagesRepository.insert(patientPackageBean);
    return patientPackageBean;
  }

  /**
   * Insert package order item bean, in case of ordering mvp from registration screen.
   *
   * @param patPackBean the pat pack bean
   * @param headerInformation the header information
   * @param userName the user name
   * @return the patient package id
   */
  public Integer insertPackageOrderItemBean(BasicDynaBean patPackBean,
      BasicDynaBean headerInformation, String userName) {
    BasicDynaBean prescribedMultiVisitPackageBean = packageOrderItemRepository.getBean();
    prescribedMultiVisitPackageBean.set("mr_no", headerInformation.get(MR_NO));
    prescribedMultiVisitPackageBean.set("patient_id", headerInformation.get("patient_id"));
    prescribedMultiVisitPackageBean.set("prescription_id", headerInformation.get("packageref"));
    prescribedMultiVisitPackageBean.set("common_order_id", headerInformation.get("commonorderid"));
    prescribedMultiVisitPackageBean.set("presc_date", new java.sql.Timestamp(new Date().getTime()));
    prescribedMultiVisitPackageBean.set("user_name", userName);
    prescribedMultiVisitPackageBean.set(PACKAGE_ID, patPackBean.get(PACKAGE_ID));
    prescribedMultiVisitPackageBean.set(PATIENT_PACKAGE_ID, patPackBean.get(PATIENT_PACKAGE_ID));
    packageOrderItemRepository.insert(prescribedMultiVisitPackageBean);
    return (Integer) patPackBean.get(PATIENT_PACKAGE_ID);
  }
  
  /**
   * Insert the MultiVisit Packages to package Prescribed Table.
   * Insert into patient_packages if the item isn't consumed as part of an existing patient package
   * 
   * @param prescribedMultiVisitPackageBean the prescribedMultiVisitPackageBean
   * @param headerInformation               the headerInformation
   * @param userName                        the userName
   * @return string
   */
  public Integer orderMultiVisitPackage(BasicDynaBean prescribedMultiVisitPackageBean,
      BasicDynaBean headerInformation, String userName) {

    if (prescribedMultiVisitPackageBean != null) {

      Integer patientPackageId = (Integer) prescribedMultiVisitPackageBean.get(PATIENT_PACKAGE_ID);
      BasicDynaBean patPackBean = this.getPatientPackageBean(patientPackageId,
              headerInformation.get(MR_NO), prescribedMultiVisitPackageBean.get(PACKAGE_ID));
      if (patientPackageId == null) {
        patientPackagesRepository.insert(patPackBean);
      }

      prescribedMultiVisitPackageBean.set("mr_no", headerInformation.get(MR_NO));
      prescribedMultiVisitPackageBean.set("patient_id", headerInformation.get("patient_id"));
      prescribedMultiVisitPackageBean.set("prescription_id", headerInformation.get("packageref"));
      prescribedMultiVisitPackageBean.set("common_order_id",
          headerInformation.get("commonorderid"));
      prescribedMultiVisitPackageBean.set("presc_date",
          new java.sql.Timestamp(new Date().getTime()));
      prescribedMultiVisitPackageBean.set("user_name", userName);
      prescribedMultiVisitPackageBean.set(PATIENT_PACKAGE_ID, patPackBean.get(PATIENT_PACKAGE_ID));
      packageOrderItemRepository.insert(prescribedMultiVisitPackageBean);
      return (Integer) patPackBean.get(PATIENT_PACKAGE_ID);
    }

    return null;
  }

  /**
   * Find the Patient Package Content and update its details.
   *
   * @param patientPackageContentId the patient package content id
   * @param qty the qty
   * @param chargeId the charge id
   * @param prescId the presc id
   * @param itemType the item type
   */
  public void insertPatientPackageConsumed(Object patientPackageContentId, Object packageContentId,
      Object patientPackageId, Object qty, Object chargeId, Object prescId, Object itemType) {
    packageOrderItemService.insertPatientPackageConsumed(patientPackageContentId, packageContentId, 
        patientPackageId,qty, chargeId, prescId, itemType);
  }
  
  public List<BasicDynaBean> getPatientPackageContentsForOperation(Object patientPackageId,
      Object opId) {
    return packageOrderItemService.getPatientPackageContentsForOperation(patientPackageId, opId);
  }
  
  /**
   * Returns the MultiVisitPackage Bean.
   * 
   * @param requestParams the requestParams
   * @return basic dyna bean
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean getNewMultiVisitPackageOrders(Map<String, List<Object>> requestParams) {

    BasicDynaBean packagePrescibedBean = null;
    Object[] testOrderItemList = requestParams.get("tests").toArray();
    Object[] servicesOrderItemList = requestParams.get("services").toArray();
    Object[] othersOrderItemList = requestParams.get("others").toArray();
    Object[] doctorsOrderItemList = requestParams.get("doctors").toArray();
    if (testOrderItemList.length > 0) {
      Map<String, Object> testOrderItemDeatils = (Map<String, Object>) testOrderItemList[0];
      if ((Boolean) testOrderItemDeatils.get("tests_multi_visit_package")
          && (testOrderItemDeatils.get("tests_package_id")) != null) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID, testOrderItemDeatils.get("tests_package_id"));
      }
    } else if (servicesOrderItemList.length > 0) {
      Map<String, Object> servicesOrderItemDetails = (Map<String, Object>) servicesOrderItemList[0];
      if ((Boolean) servicesOrderItemDetails.get("services_multi_visit_package")
          && (servicesOrderItemDetails.get("services_package_id")) != null) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID, servicesOrderItemDetails.get("services_package_id"));
      }
    } else if (othersOrderItemList.length > 0) {
      Map<String, Object> othersOrderItemDetails = (Map<String, Object>) othersOrderItemList[0];
      if ((Boolean) othersOrderItemDetails.get("others_multi_visit_package")
          && (othersOrderItemDetails.get("others_package_id")) != null) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID, othersOrderItemDetails.get("others_package_id"));
      }
    } else if (doctorsOrderItemList.length > 0) {
      Map<String, Object> doctorsOrderItemDetails = (Map<String, Object>) doctorsOrderItemList[0];
      if ((Boolean) doctorsOrderItemDetails.get("doctors_multi_visit_package")
          && doctorsOrderItemDetails.get("doctors_package_id") != null) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID, doctorsOrderItemDetails.get("doctors_package_id"));
      }
    }
    return packagePrescibedBean;
  }

  /**
   * Gets the multi visit package bean.
   *
   * @param requestParams the request params
   * @return the multi visit package bean
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean getMultiVisitPackageBean(Map<String, List<Object>> requestParams) {
    BasicDynaBean packagePrescibedBean = null;

    List<Object> testOrderItemList = requestParams.get("tests");
    List<Object> servicesOrderItemList = requestParams.get("services");
    List<Object> othersOrderItemList = requestParams.get("others");
    List<Object> doctorsOrderItemList = requestParams.get("doctors");
    List<Object> operationsOrderItemList = requestParams.get("operations");

    if (testOrderItemList != null && !testOrderItemList.isEmpty()) {
      Map<String, Object> testOrderItemDetails = (Map<String, Object>) testOrderItemList.get(0);
      if ((Boolean) testOrderItemDetails.get(MULTI_VISIT_PACKAGE)
          && !("").equals(testOrderItemDetails.get(PACKAGE_ID).toString())) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID,
            Integer.parseInt(testOrderItemDetails.get(PACKAGE_ID).toString()));
        packagePrescibedBean.set(PATIENT_PACKAGE_ID, testOrderItemDetails.get(PATIENT_PACKAGE_ID));
      }
    } else if (servicesOrderItemList != null && !servicesOrderItemList.isEmpty()) {
      Map<String, Object> servicesOrderItemDetails = (Map<String, Object>) servicesOrderItemList
          .get(0);
      if ((Boolean) servicesOrderItemDetails.get(MULTI_VISIT_PACKAGE)
          && !("").equals(servicesOrderItemDetails.get(PACKAGE_ID).toString())) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID,
            Integer.parseInt(servicesOrderItemDetails.get(PACKAGE_ID).toString()));
        packagePrescibedBean.set(PATIENT_PACKAGE_ID,
                servicesOrderItemDetails.get(PATIENT_PACKAGE_ID));
      }
    } else if (othersOrderItemList != null && !othersOrderItemList.isEmpty()) {
      Map<String, Object> othersOrderItemDetails = (Map<String, Object>) othersOrderItemList.get(0);
      if ((Boolean) othersOrderItemDetails.get(MULTI_VISIT_PACKAGE)
          && !("").equals(othersOrderItemDetails.get(PACKAGE_ID).toString())) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID,
            Integer.parseInt(othersOrderItemDetails.get(PACKAGE_ID).toString()));
        packagePrescibedBean.set(PATIENT_PACKAGE_ID,
                othersOrderItemDetails.get(PATIENT_PACKAGE_ID));
      }
    } else if (doctorsOrderItemList != null && !doctorsOrderItemList.isEmpty()) {
      Map<String, Object> doctorsOrderItemDetails = (Map<String, Object>) doctorsOrderItemList
          .get(0);
      if ((Boolean) doctorsOrderItemDetails.get(MULTI_VISIT_PACKAGE)
          && !("").equals(doctorsOrderItemDetails.get(PACKAGE_ID).toString())) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID,
            Integer.parseInt(doctorsOrderItemDetails.get(PACKAGE_ID).toString()));
        packagePrescibedBean.set(PATIENT_PACKAGE_ID,
                doctorsOrderItemDetails.get(PATIENT_PACKAGE_ID));
      }
    } else if (operationsOrderItemList != null && !operationsOrderItemList.isEmpty()) {
      Map<String, Object> operationsOrderItemDetails = (Map<String, Object>) operationsOrderItemList
          .get(0);
      if (null != operationsOrderItemDetails.get(MULTI_VISIT_PACKAGE)
          && (Boolean) operationsOrderItemDetails.get(MULTI_VISIT_PACKAGE)
          && !("").equals(operationsOrderItemDetails.get(PACKAGE_ID).toString())) {
        packagePrescibedBean = packageOrderItemRepository.getBean();
        packagePrescibedBean.set(PACKAGE_ID,
            Integer.parseInt(operationsOrderItemDetails.get(PACKAGE_ID).toString()));
        packagePrescibedBean.set(PATIENT_PACKAGE_ID,
            operationsOrderItemDetails.get(PATIENT_PACKAGE_ID));
      }
    }
    return packagePrescibedBean;
  }

  /**
   * Update the multi visit package items ordered in the current visit.
   * 
   * @param packageId the packageId
   * @param mrNo      the mrNo
   * @return boolean
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public boolean updateMultivisitPackageStatus(int packageId, Integer patientPackageId,
                                               String mrNo) {

    List<BasicDynaBean> orderedItems = multiVisitRepository
            .getOrderedPatientPackageItems(patientPackageId);
    List<BasicDynaBean> packageItems = packagesService.getPackageComponents(packageId);
    Map orderMap = mapDynaRowSet(orderedItems, "item_id");
    boolean itemsPending = false;
    boolean updated = true;
    for (BasicDynaBean packageItem : packageItems) {
      Integer consultationTypeId = (Integer) packageItem.get("consultation_type_id");
      String itemId = (String) packageItem.get("activity_id");
      String itemType = (String) packageItem.get("item_type");
      itemId = "Doctor".equals(itemType) ? consultationTypeId + "" : itemId;

      // we should not hit this, really.
      if (null == itemId) {
        continue;
      }

      // item is not ordered, so the package is pending
      BasicDynaBean item = (BasicDynaBean) orderMap.get(itemId);
      if (item == null) {
        itemsPending = true;
        break;
      }
      Integer itemQty = (Integer) packageItem.get("activity_qty");
      Integer consumedQty = (null != item.get("consumed_qty"))
          ? ((BigDecimal) item.get("consumed_qty")).intValue()
          : 0;
      if (consumedQty < itemQty) {
        itemsPending = true;
        break;
      }
    }

    if (!itemsPending) {
      updated = false;
      Map<String, Object> keyMap = new HashMap<String, Object>();
      keyMap.put("pat_package_id", patientPackageId);

      BasicDynaBean patientPackageBean = patientPackagesRepository.findByPk(keyMap);

      if (null != patientPackageBean) {
        String currentStatus = (String) patientPackageBean.get("status");
        if (!"X".equalsIgnoreCase(currentStatus)) {
          patientPackageBean.set("status", "C");
        }

        Map params = new HashMap();
        params.put("pat_package_id", patientPackageBean.get("pat_package_id"));

        int updateCount = patientPackagesRepository.update(patientPackageBean, params);
        updated = updateCount >= 0;
      }
    }
    return updated;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private HashMap mapDynaRowSet(List rows, String columnName) {
    HashMap rowSetMap = new HashMap();
    Iterator it = rows.iterator();
    while (it.hasNext()) {
      DynaBean row = (DynaBean) it.next();
      rowSetMap.put(row.get(columnName), row);
    }
    return rowSetMap;
  }

  public List<BasicDynaBean> getOrderedPackageItems(String mrNo) {
    return multiVisitRepository.getOrderedPackageItems(mrNo);
  }

  public List<BasicDynaBean> getMultiVisitPackageIds(String mrNo) {
    return patientPackagesRepository.getMultiVisitPackageIds(mrNo);
  }

  public List<BasicDynaBean> getMultiVisitPackages(String mrNo) {
    return patientPackagesRepository.getMultiVisitPackages(mrNo);
  }

  /**
   * Gets the multi visit package.
   *
   * @param mrNo the mr no
   * @param pkgId the pkg id
   * @return the multi visit package
   */
  public BasicDynaBean getMultiVisitPackage(String mrNo, Integer pkgId) {
    Map<String, Object> filters = new HashMap<>();
    filters.put("mr_no", mrNo);
    filters.put("package_id", pkgId);
    return patientPackagesRepository.findByKey(filters);
  }

  /**
   * Get patient package content charges.
   * 
   * @param packCont the Package Content
   * @param userName the user name
   * @param bedType the  bedType
   * @param orgId the  Org Id
   * @param packageContents Package Contents
   * @param patPackContentCharges Package Content charges
   * @param visitId the  visitId
   * @param packageId the  packageId
   * @param patPackageId the  patPackageId
   * @param submissionType Submission Type
   * 
   * @return Integer
   */
  public Integer getPatientPackageContentCharges(Map<String, Object> packCont,
      String userName, String bedType, String orgId,
      List<BasicDynaBean> packageContents, List<BasicDynaBean> patPackContentCharges,
      Object visitId, Object packageId, Object patPackageId, String submissionType) {
    return packageOrderItemService.getPatientPackageContentCharges(packCont, userName,
        bedType, orgId, packageContents, patPackContentCharges,
        visitId, packageId, patPackageId, submissionType);
  }

  /**
   * Insert patient package content charges.
   * 
   * @param packageContents Package Contents
   * @param patPackContentCharges Package Content charges
   */
  public void insertPatientPackageContentCharges(List<BasicDynaBean> packageContents,
      List<BasicDynaBean> patPackContentCharges) {
    packageOrderItemService.insertPatientPackageContentCharges(packageContents,
        patPackContentCharges);
  }

  /**
   * Insert patient package customized.
   *
   * @param patientPackageId the pat pack id
   * @param packDetails packageDetails
   * @param userName User Name
   */
  public void insertPatientPackageCustomized(Object patientPackageId,
      Map<String, Object> packDetails, String userName) {
    packageOrderItemService.insertPatientPackageCustomized(patientPackageId,
        packDetails, userName);
  }

  /**
   * Update patient package status.
   *
   * @param patientPackageId the pat pack id
   */
  public void updatePatientPackageStatus(Integer patientPackageId) {
    packageOrderItemService.updatePatientPackageStatus(patientPackageId);
  }
}