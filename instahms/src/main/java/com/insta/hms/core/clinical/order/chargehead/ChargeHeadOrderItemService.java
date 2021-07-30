package com.insta.hms.core.clinical.order.chargehead;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.Order;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.order.master.OrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitRepository;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;

import org.apache.commons.beanutils.BasicDynaBean;
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

/**
 * Need to check prefix and prefix_id passed. Currently not in used. Once start using this in order
 * post, then it is required
 * 
 * @author ritolia
 *
 */
@Service
@Order(key = "Direct Charge", value = { "Direct Charge" }, prefix = "charges")
public class ChargeHeadOrderItemService extends OrderItemService {

  @LazyAutowired
  private ChargeHeadOrderItemRepository chargeHeadOrderItemRepository;

  @LazyAutowired
  private BillChargeService billChargeService;

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private ChargeHeadsService chargeHeadsService;

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private DiscountPlanService discountPlanService;

  /** The multi visit package service. */
  @LazyAutowired
  private MultiVisitPackageService multiVisitPackageService;

  /** The multi visit package repository. */
  @LazyAutowired
  private MultiVisitRepository multiVisitRepository;

  private static final String ALLOW_RATE_INCREASE = "allow_rate_increase";
  private static final String ALLOW_RATE_DECREASE = "allow_rate_decrease";

  public ChargeHeadOrderItemService(ChargeHeadOrderItemRepository repository) {
    super(repository, "", "");
    // TODO Auto-generated constructor stub
  }

  @Override
  public List<BasicDynaBean> getCharges(Map<String, Object> paramMap) {
    String bedType = (String) paramMap.get("bed_type");
    String ratePlanId = (String) paramMap.get("org_id");
    Boolean isInsurance = (Boolean) paramMap.get("is_insurance");
    String id = (String) paramMap.get("id");
    String chargeType = (String) paramMap.get("renewal");
    String visitType = (String) paramMap.get("visit_type");

    if (id.contains("REG")) {
      boolean isRenewal = (chargeType != null) && chargeType.equalsIgnoreCase("renewal");
      return getRegistrationCharges(bedType, ratePlanId, id, isRenewal, isInsurance, false,
          visitType);
    } else {
      return getChargesList(null, (BigDecimal) paramMap.get("quantity"), isInsurance, null,
          paramMap);
    }
  }

  /**
   * get Registration Charges.
   * @param bedType the bedType
   * @param orgId the orgId
   * @param chargeHead the chargeHead
   * @param isRenewal the isRenewal
   * @param isInsurance the isInsurance
   * @param excludeZero the excludeZero
   * @param visitType the visitType
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getRegistrationCharges(String bedType, String orgId, String chargeHead,
      boolean isRenewal, Boolean isInsurance, boolean excludeZero, String visitType) {
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("chargehead_id", chargeHead);

    Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
    map = billService.getChargeandDiscount(chargeHead, isRenewal, orgId, bedType, visitType);
    BigDecimal charge = null;
    BigDecimal discount = null;
    charge = map.get("charge");
    discount = map.get("discount");
    BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(keys);
    BasicDynaBean chargeBean = billChargeService.setBillChargeBean("REG", chargeHead,
        (BigDecimal) charge, BigDecimal.ONE, (BigDecimal) discount, chargeHead, "", null,
        (Integer) chargeHeadBean.get("service_sub_group_id"),
        (Integer) chargeHeadBean.get("insurance_category_id"), isInsurance);
    chargeBean.set("prior_auth_mode_id", 1);
    chargeBean.set(ALLOW_RATE_INCREASE, chargeHeadBean.get(ALLOW_RATE_INCREASE));
    chargeBean.set(ALLOW_RATE_DECREASE, chargeHeadBean.get(ALLOW_RATE_DECREASE));
    List<BasicDynaBean> chargeBeanList = new ArrayList<BasicDynaBean>();
    if ((charge.compareTo(BigDecimal.ZERO) != 0) || !excludeZero) {
      chargeBeanList.add(chargeBean);
    }
    return chargeBeanList;
  }

  @Override
  public BasicDynaBean getMasterChargesBean(Object prefixId, String bedType, String ratePlanId,
      Integer centerId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<BasicDynaBean> getAllBedTypeMasterChargesBean(Object prefixId, String ratePlanId) {
    return null;
  }

  @Override
  protected List<BasicDynaBean> getChargesList(BasicDynaBean itemType, BigDecimal quantity,
      Boolean isInsurance, String condDoctorId, Map<String, Object> otherParams) {
    String chargeHead = (String) otherParams.get("id");
    BasicDynaBean charge = billChargeService.getBean();
    charge.set("charge_head", chargeHead);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("chargehead_id", chargeHead);
    BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(keys);
    charge.set("service_sub_group_id", chargeHeadBean.get("service_sub_group_id"));
    charge.set(ALLOW_RATE_INCREASE, chargeHeadBean.get(ALLOW_RATE_INCREASE));
    charge.set(ALLOW_RATE_DECREASE, chargeHeadBean.get(ALLOW_RATE_DECREASE));
    charge.set("amount_included", BigDecimal.ZERO);
    charge.set("qty_included", BigDecimal.ZERO);
    List<BasicDynaBean> charges = new ArrayList<BasicDynaBean>();
    charges.add(charge);
    return charges;
  }

  /**
   * Returns the list of direct charges item details by passing there id.
   * @param entityIdList the entityIdList
   * @param paramMap the paramMap
   * @return list of basic dyna bean
   */
  @Override
  public List<BasicDynaBean> getItemDetails(List<Object> entityIdList,
      Map<String, Object> paramMap) {
    return chargeHeadOrderItemRepository.getItemDetails(entityIdList);
  }

  @Override
  public List<BasicDynaBean> getOrderedItems(Map<String, Object> parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BasicDynaBean getEditBean(Map<String, Object> item) {
    // TODO Auto-generated method stub
    BasicDynaBean bean = getBean();
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Map<String, Object> item) {
    // TODO Auto-generated method stub
    BasicDynaBean bean = getBean();
    return bean;
  }

  @Override
  public BasicDynaBean getCancelBean(Object orderId) {
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
  public int[] updateItemBeans(List<BasicDynaBean> items) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateCancelStatusAndRefOrders(List<BasicDynaBean> items, boolean cancel,
      boolean cancelCharges, List<String> editOrCancelOrderBills, Map<String, Object> itemInfoMap) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean updateFinalizableItemCharges(List<BasicDynaBean> orders,
      Map<String, Object> itemInfoMap) {
    // TODO Auto-generated method stub
    return true;
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
    if (itemsMapsList.isEmpty()) {
      return Collections.emptyList();
    }
    BasicDynaBean headerInformation = (BasicDynaBean) billInfoMap.get("header_information");
    BasicDynaBean bill = (BasicDynaBean) billInfoMap.get("bill");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    int[] planIds = (int[]) billInfoMap.get("plan_ids");

    List<BasicDynaBean> chargesList = new ArrayList<>();
    for (int index = 0; index < itemsMapsList.size(); index++) {
      String[] finalPreAuthIds = new String[2];
      Integer[] finalPreAuthModeIds = new Integer[2];
      BasicDynaBean chargeBean = null;

      Map<String, Object> chargeHeadDetails = (Map<String, Object>) itemsMapsList.get(index);
      Boolean isMultiVisitPackage =
          chargeHeadDetails.get("multi_visit_package") != null
          ? (Boolean) chargeHeadDetails.get("multi_visit_package") : false;
      if (null != bill && isMultiVisitPackage) {
        chargesList = insertOrderItemCharges(chargeable, headerInformation,
            chargeBean, bill, finalPreAuthIds, finalPreAuthModeIds, planIds, "",
            (String) chargeHeadDetails.get("activity_code"),
            centerId, isMultiVisitPackage, chargeHeadDetails);
      }
    }

    return chargesList;
  }

  @Override
  public List<BasicDynaBean> insertOrderItemCharges(boolean chargeable,
      BasicDynaBean headerInformation, BasicDynaBean orderBean, BasicDynaBean bill,
      String[] preAuthIds, Integer[] preAuthModeIds, int[] planIds, String condDoctorId,
      String activityCode, Integer centerId, Boolean isMultivisitPackage,
      Map<String, Object> orderItemDetails) throws ParseException {
    Boolean isInsurance = (Boolean) bill.get("is_tpa");
    BigDecimal quantity = BigDecimal.valueOf((int) orderItemDetails.get("quantity"));
    List<BasicDynaBean> chargesList = new ArrayList<>();

    if (chargeable) {
      BasicDynaBean charge = billChargeService.setBillChargeBean(
          (String) orderItemDetails.get("charge_group"),
          (String) orderItemDetails.get("charge_head"),
          BigDecimal.valueOf(Double.valueOf((String) orderItemDetails.get("amount"))),quantity,
          (BigDecimal.valueOf(Double.valueOf((String)
              orderItemDetails.get("discount")))).multiply(quantity), 
          (String) orderItemDetails.get("charge_name"),
          (String) orderItemDetails.get("charge_name"),
          null, (int) orderItemDetails.get("service_sub_group_id"),
          Integer.valueOf((String) orderItemDetails.get("insurance_category_id")), isInsurance);

      if (orderItemDetails.get("billing_group_id") != null) {
        charge.set("billing_group_id", (Integer) orderItemDetails.get("billing_group_id"));
      }
      Integer discCatId = (Integer) orderItemDetails.get("disc_cat_id");
      discCatId = discCatId != null ? (Integer) discCatId : 0;
      List<BasicDynaBean> discountPlanDetails = discountPlanService.listAllDiscountPlanDetails(null,
          "discount_plan_id", discCatId, "priority");
      String visitType = (String) orderItemDetails.get("visit_type");
      discountPlanService.applyDiscountRule(charge, discCatId, discountPlanDetails, visitType);
      if (orderItemDetails.get("package_id") != null) {
        charge.set("package_id", orderItemDetails.get("package_id"));
        if (isMultivisitPackage) {
          BasicDynaBean componentDeatilBean = multiVisitRepository.findByKey("package_id", 
              (Integer) charge.get("package_id"));
          charge.set("allow_rate_increase", 
              (Boolean)componentDeatilBean.get("allow_rate_increase"));
          charge.set("allow_rate_decrease", 
              (Boolean)componentDeatilBean.get("allow_rate_decrease"));
        }
      }
      chargesList.add(charge);
      if (chargesList.size() > 0) {
        (chargesList.get(0)).set("order_number",
            headerInformation.get("commonorderid"));
      }
      if (orderItemDetails != null && orderItemDetails.get("posted_date") != null
          && !orderItemDetails.get("posted_date").equals("")) {
        Date postedDate = formatter.parse((String) orderItemDetails.get("posted_date"));
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, null,
            planIds, 0, null, new Timestamp(postedDate.getTime()));
      } else {
        insertOrderBillCharges(chargesList, activityCode, "Y", null, null, null, bill, null,
            planIds, 0,
            null, null);
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
              orderItemDetails.get("quantity"), chargeId, null,
              orderItemDetails.get("type"));
        }
      }
    }

    return chargesList;
  }

  @Override
  public List<BasicDynaBean> getPackageRefOrders(String visitId, List<Integer> prescriptionId) {
    // TODO Auto-generated method stub
    return Collections.emptyList();
  }

}
