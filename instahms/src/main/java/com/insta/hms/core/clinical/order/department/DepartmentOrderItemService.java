package com.insta.hms.core.clinical.order.department;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.mdm.doctors.DoctorService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Order(key = "Department", value = { "Department" })
public class DepartmentOrderItemService extends OrderItemService {

  private DepartmentOrderItemRepository departmentOrderItemRepository;
  @LazyAutowired
  private DoctorOrderItemService doctorOrderItemService;
  @LazyAutowired
  private DoctorService doctorService;

  public DepartmentOrderItemService(DepartmentOrderItemRepository repository) {
    super(repository, "", "");
    departmentOrderItemRepository = repository;
  }

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
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {
    return null;
  }

  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    List<BasicDynaBean> orderItemDetails = new ArrayList<>();
    List<Object> doctorIds = new ArrayList<>();
    if ("true".equals((String) paramMap.get("department_doctor"))) {
      List<BasicDynaBean> allDoctorIdBeans = new ArrayList<>();
      for (Object departmentId : entityIdList) {
        List<BasicDynaBean> doctorIdBeans = doctorService
            .getDepartmentDoctors((String) departmentId);
        allDoctorIdBeans.addAll(doctorIdBeans);
      }
      if (!allDoctorIdBeans.isEmpty()) {
        for (BasicDynaBean bean : allDoctorIdBeans) {
          doctorIds.add(bean.get("entity_id"));
        }
        orderItemDetails.addAll(doctorOrderItemService.getItemDetails(doctorIds,paramMap));
      }
    } else {
      orderItemDetails.addAll(departmentOrderItemRepository.getDepartmentDetails(entityIdList));
    }
    return orderItemDetails;
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) throws ParseException {
    return null;
  }

  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
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

}
