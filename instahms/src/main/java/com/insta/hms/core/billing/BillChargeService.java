package com.insta.hms.core.billing;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BusinessService;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Service
public class BillChargeService extends BusinessService {
  public static final String CH_CLAIM_SERVICE_TAX = "CSTAX";
  public static final String CH_BILL_SERVICE_CHARGE = "BSTAX";
  public static final String CH_ROUND_OFF = "ROF";
  public static final String CH_DYNA_PACKAGE_MARGIN = "MARPKG";
  public static final String CH_BYBED = "BYBED";
  public static final String CH_SERVICE_TAX = "STAX";
  public static final String CH_LUXURY_TAX = "LTAX";
  public static final String CH_PHARMACY_CREDIT_MEDICINE = "PHCMED";
  public static final String CH_PHARMACY_CREDIT_RETURNS = "PHCRET";
  public static final String CH_INVENTORY_RETURNS = "INVRET";
  public static final String CH_INVENTORY_ITEM = "INVITE";
  public static final String CH_DUTY_DOCTOR = "DDBED";
  public static final String CH_DUTY_DOCTOR_ICU = "DDICU";
  public static final String CH_PERDIEM = "MARPDM";

  public static final String CG_ICU = "ICU";
  public static final String CG_BED = "BED";
  public static final String CG_DISCOUNTS = "DIS";
  public static final String CG_INVENTORY = "ITE";

  public static final List<String> skipForChargeHeads = Arrays.asList(CH_BILL_SERVICE_CHARGE,
      CH_CLAIM_SERVICE_TAX, CH_ROUND_OFF);

  @LazyAutowired
  private BillChargeRepository billChargeRepo;

  @LazyAutowired
  private BillActivityChargeService billActivityChargeService;

  @LazyAutowired
  private BillHelper billHelper;
  
  @LazyAutowired
  private SessionService sessionService;

  public String getNextPrefixedId() {
    return (String) billChargeRepo.getNextId();
  }

  public BasicDynaBean getBean() {
    return billChargeRepo.getBean();
  }

  public int[] batchInsert(List<BasicDynaBean> regCharges) {
	  String revenueDepartmentId = "";
	if (!regCharges.isEmpty()) {
		revenueDepartmentId = billHelper.getRevenueDepartmentFromCharge(regCharges.get(0));
	}
	for (BasicDynaBean charge : regCharges) {
		if (null != charge) {
			charge.set("revenue_department_id", revenueDepartmentId);
			billHelper.setBillChargeBillingGroup(charge);
		}
	}
    return billChargeRepo.batchInsert(regCharges);
  }

  public BasicDynaBean getBillFromChargeId(String chargeId) {
    return billChargeRepo.getBillFromChargeId(chargeId);
  }

  public Integer update(BasicDynaBean bean, Map<String, Object> keys) {
    bean.set("username", sessionService.getSessionAttributes().get("userId"));
    return billChargeRepo.update(bean, keys);
  }
  
  /**
   * Update.
   *
   * @param updateMap
   *          the map of values to be updated.
   * @param chargeId
   *          the charge id for which the values are to be updated.
   * @return the integer
   */
  public Integer update(Map<String, Object> updateMap, String chargeId) {
    BasicDynaBean bean = getBean();
    for (Entry<String, Object> parameter : updateMap.entrySet()) {
      bean.set(parameter.getKey(), parameter.getValue());
    }
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("charge_id", chargeId);
    return update(bean, keys);
  }

  public Integer updateActivity(String chargeId, String status) {
    BasicDynaBean bean = getBean();
    bean.set("status", status);
    bean.set("hasactivity", "f");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("charge_id", chargeId);
    return update(bean, keys);
  }

  String a = "INSERT INTO bill_activity_charge " + " (charge_id, activity_id, activity_code)"
      + " values(?,?,?);";

  public Integer insertActivity(String chargeId, String activityId, String chargeHead) {
    BasicDynaBean chargeBean = getBean();
    chargeBean.set("charge_id", chargeId);
    chargeBean.set("activity_id", activityId);
    chargeBean.set("activity_code", chargeHead);
    return billChargeRepo.insert(chargeBean);
  }

  /**
   * Set the bean property
   * 
   * @param bean
   * @param field
   * @param value
   */
  private void setBeanProperty(BasicDynaBean bean, String field, Object value) {
    bean.set(field, value);
  }

  /**
   * Return billChargeBean with value passed by FirstOfCategory is no more required (dataBase not
   * null) TODO: insurance_claim_amount setting to total amount inCase of insurance bill (wrong).
   * 
   * @param unitCharge
   * @param quantity
   * @param isInsurance
   * @return
   */
  public BasicDynaBean setBillChargeBean(String chargeGroup, String chargeHead,
      BigDecimal unitCharge, BigDecimal quantity, BigDecimal discount, String descId, String desc,
      String deptId, int serviceSubGroupId, int insuranceCategoryId, boolean isInsurance) {

    BasicDynaBean bean = getBean();
    setBeanProperty(bean, "charge_group", chargeGroup);
    setBeanProperty(bean, "charge_head", chargeHead);
    setBeanProperty(bean, "act_rate", unitCharge);
    setBeanProperty(bean, "orig_rate", unitCharge);
    setBeanProperty(bean, "act_quantity", quantity);
    setBeanProperty(bean, "discount", discount);
    setBeanProperty(bean, "act_unit", "");
    setBeanProperty(bean, "act_description_id", descId);
    setBeanProperty(bean, "act_description", desc);
    setBeanProperty(bean, "act_department_id", deptId);
    setBeanProperty(bean, "service_sub_group_id", serviceSubGroupId);
    setBeanProperty(bean, "insurance_category_id", insuranceCategoryId);
    setBeanProperty(bean, "status", "A");
    setBeanProperty(bean, "hasactivity", false);
    setBeanProperty(bean, "allow_discount", true);
    if (discount.compareTo(BigDecimal.ZERO) != 0) {
      setBeanProperty(bean, "overall_discount_amt", discount);
      setBeanProperty(bean, "overall_discount_auth", -1);
    } else {
      setBeanProperty(bean, "overall_discount_auth", null);
      setBeanProperty(bean, "overall_discount_amt", null);
    }
    setBeanProperty(bean, "amount", (unitCharge.multiply(quantity)).subtract(discount));
    setBeanProperty(bean, "first_of_category", false);
    setBeanProperty(bean, "insurance_claim_amount", BigDecimal.ZERO);
    Timestamp postedDate = DateUtil.getCurrentTimestamp();
    setBeanProperty(bean, "mod_time", postedDate);
    setBeanProperty(bean, "posted_date", postedDate);
    setBeanProperty(bean, "allow_zero_claim", false);
    String username = (String)sessionService.getSessionAttributes().get("userId");
    setBeanProperty(bean, "username", username);   
    return bean;
  }

  public List<BasicDynaBean> getChargeDetailsBean(String billNo) {
    return billChargeRepo.getBillCharges(new Object[] { billNo });
  }

  public List<BasicDynaBean> getChargePresDoctorsByVisit(String visitId) {
    return billChargeRepo.getChargePresDoctorsByVisit(new Object[] { visitId });
  }

  public BasicDynaBean getChargePaymentDetails(String chargeId) {
    return billChargeRepo.getChargePaymentDetails(chargeId);
  }

  public BasicDynaBean getBillChargeExcludeTotals(String billNo, List<String> chargeHeads) {
    return billChargeRepo.getBillChargeExcludeTotals(billNo, chargeHeads);
  }

  public BasicDynaBean getChargeBean(List<BasicDynaBean> charges, String chargeHead) {
    for (BasicDynaBean charge : charges) {
      if (charge.get("status").equals("A") && charge.get("charge_head").equals(chargeHead)) {
        return charge;
      }
    }
    return null;
  }

  public BigDecimal getChargeApplicableTotal(List<BasicDynaBean> charges, String fieldToEvaluate,
      String fieldToAdd) {
    BigDecimal total = BigDecimal.ZERO;
    for (BasicDynaBean charge : charges) {
      String chargehead = (String) charge.get("charge_head");
      String status = (String) charge.get("status");

      if (status.equals("X") || skipForChargeHeads.contains(chargehead)) {
        continue;
      }

      String servApplicable = (String) charge.get(fieldToEvaluate);
      if (servApplicable.equals("Y")) {
        total = total.add((BigDecimal) charge.get(fieldToAdd));
      }
    }
    return total;
  }

  /**
   * This method will change the status of a charge to 'X' which means cancelled charge. If
   * cancelRefs is true, it will also cancel charges whose charge_ref is this charge. This is mainly
   * called when cancelling an order.
   */
  public int cancelBillCharge(String chargeId, boolean chargeRef, String userName) {
    return billChargeRepo.cancelCharge(chargeId, chargeRef, userName);
  }

  public List<BasicDynaBean> getAssociatedCharges(String chargeId) {
    return billChargeRepo.getAssociatedCharges(chargeId);
  }

  public int updateHasActivityStatus(String chargeId, boolean hasActivity, boolean refs) {
    return billChargeRepo.updateHasActivityStatus(chargeId, hasActivity, refs);
  }

  /*
   * Update the prescribing doctor to the new one. If updateRefs is true, then, also updates the
   * referenced charges with the same prescribing doctor.
   */
  public int updatePrescribingDoctor(String chargeId, String prescDrId, boolean updateRefs) {
    return billChargeRepo.updatePrescribingDoctor(chargeId, prescDrId, updateRefs);
  }

  public BasicDynaBean getCharge(String chargeId) {
    return billChargeRepo.findByKey("charge_id", chargeId);
    // return billChargeRepo.getCharge(chargeId);
  }

  public void updateChargeAmounts(BasicDynaBean curCharge) {
    Map<String, Object> key = new HashMap<String, Object>();
    key.put("charge_id", curCharge.get("charge_id"));
    billChargeRepo.update(curCharge, key);
  }

  public BasicDynaBean findByKey(String key, String value) {
    return billChargeRepo.findByKey(key, value);
  }

  public String getBillStatus(String chargeId) {
    return billChargeRepo.getBillStatus(chargeId);
  }

  public void cancelChargeUpdate(String chargeId, boolean cancelRefs, String userName) {
    billChargeRepo.cancelChargeUpdate(chargeId, cancelRefs, userName);
  }

  public void recalcAmount(BasicDynaBean charge) {
    BigDecimal actRate = (BigDecimal) charge.get("act_rate");
    BigDecimal actQuantity = (BigDecimal) charge.get("act_quantity");
    BigDecimal discount = (BigDecimal) charge.get("discount");
    charge.set("amount",
        ConversionUtils.setScale(actRate.multiply(actQuantity).subtract(discount)));
    if (discount.compareTo(BigDecimal.ZERO) != 0) {
      charge.set("overall_discount_amt", discount);
      charge.set("overall_discount_auth", -1);
    }
  }

  public List<BasicDynaBean> getChargeReferences(String chargeId) {
    return billChargeRepo.getChargeReferences(chargeId);
  }

  public Integer insert(BasicDynaBean chargeBean) {
    chargeBean.set("username", sessionService.getSessionAttributes().get("userId"));
    return billChargeRepo.insert(chargeBean);
  }
  
  public Integer insertCharge(BasicDynaBean chargeBean, Integer activityId, String activityCode,String activityConducted, String activityDescriptionId, Timestamp conductedDateAndTime){
    if(activityId != 0){
      chargeBean.set("hasactivity", true);
    }
    int result = insert(chargeBean);
    if(result > 0 && activityCode !=null && activityId != 0){
      BasicDynaBean billActivityChargeBean = billActivityChargeService.getBillActivityChargeBean((String) chargeBean.get("charge_id"),
          activityCode, (String) chargeBean.get("charge_head"), activityId.toString(), activityDescriptionId,
          null, "N".equals(activityConducted) ? "N" : "Y", conductedDateAndTime);
      billActivityChargeService.insert(billActivityChargeBean);
    }
    return result;
  }

  public void copyChargeAmounts(BasicDynaBean from, BasicDynaBean to, boolean setModTime) {
    to.set("act_rate", from.get("act_rate"));
    to.set("act_quantity", from.get("act_quantity"));
    to.set("act_unit", from.get("act_unit"));
    to.set("discount", from.get("discount"));
    to.set("overall_discount_amt", from.get("discount"));
    to.set("amount", from.get("amount"));
    if (setModTime) {
      to.set("mod_time", DateUtil.getCurrentTimestamp());
    }
    to.set("payee_doctor_id", from.get("payee_doctor_id"));
    to.set("activity_conducted", from.get("activity_conducted"));
    to.set("act_description", from.get("act_description"));
    to.set("act_description_id", from.get("act_description_id"));
    to.set("act_remarks", from.get("act_remarks"));
    to.set("username", from.get("username"));
    String chargeGroup = (String) from.get("charge_group");
    String chargeHead = (String) from.get("charge_head");
    if ((chargeGroup.equals("BED") && (chargeHead.equals("BBED") || chargeHead.equals("BYBED")))
        || (chargeGroup.equals("ICU") && chargeHead.equals("BICU"))) {
      to.set("act_rate_plan_item_code", from.get("act_rate_plan_item_code"));
    }
  }

  public List<BasicDynaBean> getExcludedBedCharges(String billNo) {
    return billChargeRepo.getExcludedBedCharges(billNo);
  }

  public void deleteExcludedBedCharges(List<BasicDynaBean> charges) {
    for (BasicDynaBean chargeBean : charges)
      billChargeRepo.delete("charge_id", chargeBean.get("charge_id"));
  }

  public List<BasicDynaBean> list(Map filterMap, String sortColumn) {
    return billChargeRepo.listAll(null, filterMap, sortColumn);
  }

  public List<BasicDynaBean> getChargeAndRefs(String chargeId) {
    return billChargeRepo.getChargeAndRefs(chargeId);
  }

  public void updateChargeAmounts(List<BasicDynaBean> charges) {
    for (BasicDynaBean bean : charges) {
      Map<String, Object> key = new HashMap<String, Object>();
      key.put("charge_id", (String) bean.get("charge_id"));
      billChargeRepo.update(bean, key);
    }
  }

  public void cancelChargeUpdateAuditLog(String chargeId, boolean cancelRefs, String userName) {
    billChargeRepo.cancelChargeUpdateAuditLog(chargeId, cancelRefs, userName);
  }

  public void updateChargeExcluded(String billNo) {
    billChargeRepo.updateChargeExcluded(billNo);
  }

  /**
   * Find by keys.
   *
   * @param keys
   *          Map
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKeys(Map keys) {
    return billChargeRepo.findByKey(keys);
  }

  /**
   * Gets the adjustment amt.
   *
   * @param billNo
   *          String
   * @return the adjustment amt
   */
  public BasicDynaBean getAdjustmentAmt(String billNo) {
    return billChargeRepo.getAdjustmentAmt(billNo);
  }

  /**
   * Gets the adds the on payment amt.
   *
   * @param billNo
   *          String
   * @return the adds the on payment amt
   */
  public BigDecimal getAddOnPaymentAmt(String billNo) {
    return billChargeRepo.getAddOnPaymentAmt(billNo);
  }

  /**
   * Cancel add on payment DRG items.
   *
   * @param billNo
   *          String
   * @return the boolean
   */
  public Boolean cancelAddOnPaymentDRGItems(String billNo) {
    return billChargeRepo.cancelAddOnPaymentDRGItems(billNo);
  }

  /**
   * Cancel DRG outlier amount entry.
   *
   * @param billNo
   *          String
   * @return the boolean
   */
  public Boolean cancelDRGOutlierAmountEntry(String billNo) {
    return billChargeRepo.cancelDRGOutlierAmountEntry(billNo);
  }

  /**
   * Cancel DRG items.
   *
   * @param billNo
   *          String
   */
  public void cancelDRGItems(String billNo) {
    billChargeRepo.cancelDRGItems(billNo);
  }

  /**
   * Un lock items in drg bill.
   *
   * @param billNo
   *          String
   */
  public void unLockItemsInDrgBill(String billNo) {
    billChargeRepo.unLockItemsInDrgBill(billNo);
  }

  /**
   * Lock items in DRG bill.
   *
   * @param billNo
   *          String
   * @return the boolean
   */
  public Boolean lockItemsInDRGBill(String billNo) {
    return billChargeRepo.lockItemsInDRGBill(billNo);
  }

  /**
   * Gets the all hospital charges for DRG.
   *
   * @param billNo
   *          String
   * @return the all hospital charges for DRG
   */
  public List<BasicDynaBean> getAllHospitalChargesForDRG(String billNo) {
    return billChargeRepo.getAllHospitalChargesForDRG(billNo);
  }

  /**
   * Gets the inv and pharm total amt.
   *
   * @param billNo
   *          String
   * @return the inventory and pharmacy total amt
   */
  public BasicDynaBean getInvAndPharmTotalAmt(String billNo) {
    return billChargeRepo.getInvAndPharmTotalAmt(billNo);
  }

	public Boolean updateCharges(String query, String ratePlanId, String bedType) {
		// TODO Auto-generated method stub
		
		return billChargeRepo.updateCharges(query, ratePlanId, bedType);
	}

	public Boolean updateCharges(String query) {
		// TODO Auto-generated method stub
		return billChargeRepo.updateCharges(query);
	}
	
	public List<BasicDynaBean> getCharges(String query) {
		// TODO Auto-generated method stub
		return billChargeRepo.getCharges(query);
	}

	public boolean unlockBillCharges(String billNo) {
		// TODO Auto-generated method stub
		return billChargeRepo.unlockBillCharges(billNo);
	}

	public List<BasicDynaBean> listAll(String billNo) {
		return billChargeRepo.listAll(null, "bill_no", billNo);
	}

	public Boolean unlockVisitBillsCharges(String visitId, String billStatus) {
		// TODO Auto-generated method stub
		return billChargeRepo.unlockVisitBillsCharges(visitId, billStatus);
	}

	public Boolean setIssueReturnsClaimAmountTOZero(String visitId,
			String billStatus) {
		// TODO Auto-generated method stub
		return billChargeRepo.setIssueReturnsClaimAmountTOZero(visitId, billStatus);
	}

  public List<BasicDynaBean> getAllBillCharges(String billNos) {
    // TODO Auto-generated method stub
    return billChargeRepo.getAllBillCharges(billNos);
  }

  public Boolean batchUpdate(List<BasicDynaBean> billCharges, Map<String,Object> updateKeysMap) {
    // TODO Auto-generated method stub
    return billChargeRepo.batchUpdate(billCharges, updateKeysMap)[0] >= 0;
  }

  public List<BasicDynaBean> getVisitBillCharges(String visitId) {
    // TODO Auto-generated method stub
    return billChargeRepo.getVisitBillCharges(visitId);
  }

  public List<BasicDynaBean> getBillChargesExcludingPharmacy(String billNos) {
    // TODO Auto-generated method stub
    return billChargeRepo.getBillChargesExcludingPharmacy(billNos);
  }

  public Boolean updateDiscountAuthAsRatePlanDiscount(String billNos) {
    // TODO Auto-generated method stub
    return billChargeRepo.updateDiscountAuthAsRatePlanDiscount(billNos);
  }
  
  public boolean getDRGMarginExist(String claimId, Integer priority) {
    return billChargeRepo.getDRGMarginExist(claimId, priority);
  }

  public BigDecimal getDRGAdjustmentAmt(String claimId, Integer priority) {
    return billChargeRepo.getDRGAdjustmentAmt(claimId, priority);
  }
  
  public List<BasicDynaBean> getBillChargesForPriorAuth(String billNo, Set<String> excludedChargeGroups) {
    return billChargeRepo.getChargesForPriorAuth(billNo, excludedChargeGroups);
  }
  
  public List<BasicDynaBean> getBillChargesForPriorAuth(List<String> chargeIds) {
    return billChargeRepo.getChargesForPriorAuth(chargeIds);
  }

  public Integer setClaimAmountAndExclusionBasedOnPreAuth(String visitId) {
    return billChargeRepo.setClaimAmountAndExclusionBasedOnPreAuth(visitId);
  }
  
  public Integer setPriorAuthApprovalAmountAsClaimAmount(int preauthActId, Object approvedAmount) {
    return billChargeRepo.setPriorAuthApprovalAmountAsClaimAmount(preauthActId, approvedAmount);
  }
  
  public Integer setPriorAuthDetailsForCharges(int preAuthActId, String priorAuthId, Integer priorAuthModeId) {
    return billChargeRepo.setPriorAuthDetailsForCharges(preAuthActId, priorAuthId, priorAuthModeId);
  }

  public boolean clearPreauthActIdOfBillChargesForVisit(String visitId) {
    List<BasicDynaBean> billChargesOfVisitWithPreauth = billChargeRepo.getVisitBillChargesWithPreAuth(visitId);
    if(billChargesOfVisitWithPreauth.isEmpty()) {
      return true;
    }
    List<String> chargeIds = new ArrayList<>();
    for(BasicDynaBean bean : billChargesOfVisitWithPreauth) {
      bean.set("preauth_act_id", null);
      chargeIds.add((String)bean.get("charge_id"));
    }
    Map<String, Object> params = new HashMap<>();
    params.put("charge_id", chargeIds);
    return batchUpdate(billChargesOfVisitWithPreauth, params);
  }

  public Integer unlockChargeClaim(List<String> chargeIds){
    if(CollectionUtils.isEmpty(chargeIds)){
      return 0;
    }
    return billChargeRepo.unlockChargeClaim(chargeIds);
  }
  public Integer lockChargeClaim(List<String> chargeIds){
    if(CollectionUtils.isEmpty(chargeIds)){
      return 0;
    }
    return billChargeRepo.lockChargeClaim(chargeIds);
  }
}
