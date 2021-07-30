package com.insta.hms.extension.insurance.drg;

import com.insta.hms.common.BusinessService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class DrgCalculatorService.
 */
@Service
public class DrgCalculatorService extends BusinessService {

  /**
   * The Constant DRG_CHARGE_ID.
   */
  private static final String DRG_CHARGE_ID = "drg_charge_id";

  /**
   * The Constant CHARGE_ID.
   */
  private static final String CHARGE_ID = "charge_id";

  /**
   * The Constant BILL_NO.
   */
  private static final String BILL_NO = "bill_no";

  /**
   * The Constant CODE_TYPE.
   */
  private static final String CODE_TYPE = "code_type";

  /**
   * The Constant ACT_RATE_PLAN_ITEM_CODE.
   */
  private static final String ACT_RATE_PLAN_ITEM_CODE = "act_rate_plan_item_code";

  /**
   * The Constant ACT_REMARKS.
   */
  private static final String ACT_REMARKS = "act_remarks";

  /**
   * The bill service.
   */
  @LazyAutowired
  private BillService billService;

  /**
   * The bill charge service.
   */
  @LazyAutowired
  private BillChargeService billChargeService;

  /**
   * The bill charge claim service.
   */
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;

  /**
   * The bill charge tax service.
   */
  @LazyAutowired
  BillChargeTaxService billChargeTaxService;

  /**
   * The registration service.
   */
  @LazyAutowired
  private RegistrationService registrationService;

  /**
   * The drg update service.
   */
  @LazyAutowired
  private DrgUpdateService drgUpdateService;

  /**
   * The patient insurance plans service.
   */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;

  /**
   * The sales service.
   */
  @LazyAutowired
  SalesService salesService;

  /**
   * Adds the DRG.
   *
   * @param billNo  the bill no
   * @param drgCode the drg code
   * @return the boolean
   */
  public Boolean addDRG(String billNo, String drgCode) {
    String visitId = billService.getVisitId(billNo);
    drgUpdateService.updateDRGCode(visitId, drgCode);
    Map drgCodeMap = billService.getDRGCode(visitId);
    if (null != drgCodeMap && null != drgCodeMap.get(DRG_CHARGE_ID)) {
      drgUpdateService.updateDRGBasePayment(visitId, billNo, drgCode,
          (String) drgCodeMap.get(DRG_CHARGE_ID));
      drgUpdateService.updateAdjustmentEntry(billNo, visitId);
      BasicDynaBean chgBean = drgUpdateService
          .getChargeBeanForAddOnPayMent(billNo);
      if (null != chgBean) {
        drgUpdateService.updateAddOnPayment(visitId, billNo, drgCode);
      } else {
        postAddOnPaymentEntry(billNo, visitId, drgCode);
      }
    } else {
      // DRG base payment should be posted
      postDRGBasePayment(visitId, billNo, drgCode);
      // Post an adjustment for bill totals
      postAdjustmentEntry(billNo, visitId);
      // Post add-on payment applicable
      postAddOnPaymentEntry(billNo, visitId, drgCode);
      // post outlier amount
      postDRGOutlierEntry(billNo, visitId, drgCode);
    }

    // Item sponsor amount set to zero
    billChargeClaimService.setItemsSponsorAmount(billNo);
    salesService.setItemsSponsorAmount(billNo);
    billChargeService.lockItemsInDRGBill(billNo);

    return false;
  }

  /**
   * Post adjustmen entry.
   *
   * @param billNo  the bill no
   * @param visitId the visit id
   */
  private void postAdjustmentEntry(String billNo, String visitId) {
    BasicDynaBean bean = drgUpdateService.getAdjustmentAmt(visitId);
    BigDecimal adjAmt = BigDecimal.ZERO;
    BigDecimal taxAmt = BigDecimal.ZERO;
    if (null != bean && null != bean.get("amount")) {
      adjAmt = (BigDecimal) bean.get("amount");
    }
    if (null != bean && null != bean.get("tax")) {
      taxAmt = (BigDecimal) bean.get("tax");
    }

    BasicDynaBean adjChargeBean = billChargeService.setBillChargeBean("DRG",
        "ADJDRG", adjAmt.negate(), BigDecimal.ONE, BigDecimal.ZERO,
        null, "DRG Adjustment", null, 0, -1, true);
    adjChargeBean.set(CHARGE_ID, billChargeService.getNextPrefixedId());
    adjChargeBean.set(BILL_NO, billNo);
    adjChargeBean.set("tax_amt", taxAmt.negate());
    drgUpdateService.postBillCharge(adjChargeBean);
    drgUpdateService.postBillChargeClaims(adjChargeBean, visitId, billNo);

  }

  /**
   * Post DRG base payment.
   *
   * @param visitId the visit id
   * @param billNo  the bill no
   * @param drgCode the drg code
   */
  private void postDRGBasePayment(String visitId, String billNo,
                                  String drgCode) {
    BigDecimal baseRate = drgUpdateService.getBaseRate(visitId);
    BasicDynaBean drgCodeBean = drgUpdateService.getDrgCodeBean(drgCode);
    BigDecimal relativeWeight = null != drgCodeBean.get("relative_weight")
        ? (BigDecimal) drgCodeBean.get("relative_weight")
        : BigDecimal.ZERO;

    String codeType = null != drgCodeBean.get(CODE_TYPE)
        ? (String) drgCodeBean.get(CODE_TYPE)
        : "";
    BigDecimal drgbasePaymentAmt = baseRate.multiply(relativeWeight);
    String drgRemarks = "Base Payment.     BaseRate : " + baseRate
        + "  RelativeWeight : " + relativeWeight;

    BasicDynaBean basePaymentBean = billChargeService.setBillChargeBean(
        "DRG", "BPDRG", drgbasePaymentAmt, BigDecimal.ONE,
        BigDecimal.ZERO, drgCode, "DRG Base Payment", null, 0, -2,
        true);
    basePaymentBean.set(CHARGE_ID, billChargeService.getNextPrefixedId());
    basePaymentBean.set(BILL_NO, billNo);
    basePaymentBean.set(ACT_RATE_PLAN_ITEM_CODE, drgCode);
    basePaymentBean.set(CODE_TYPE, codeType);
    basePaymentBean.set(ACT_REMARKS, drgRemarks);

    drgUpdateService.postBillCharge(basePaymentBean);
    drgUpdateService.postBillChargeClaims(basePaymentBean, visitId, billNo);
    calculateDrgCodeTaxAmt(basePaymentBean);
  }

  /**
   * Calculate drg code tax amt.
   *
   * @param charge the charge
   */
  private void calculateDrgCodeTaxAmt(BasicDynaBean charge) {
    List<BasicDynaBean> chargesList = new ArrayList<>();
    chargesList.add(charge);
    billChargeTaxService.batchInsert(chargesList);
  }

  /**
   * Post DRG outlier entry.
   *
   * @param billNo  the bill no
   * @param visitId the visit id
   * @param drgCode the drg code
   */
  public void postDRGOutlierEntry(String billNo, String visitId,
                                  String drgCode) {
    BigDecimal outlierRate = drgUpdateService
        .getDrgOutlierTotalAmount(billNo, visitId);

    // post the outlier charge
    if (outlierRate.compareTo(BigDecimal.ZERO) > 0) {
      BasicDynaBean outlierChargeBean = billChargeService
          .setBillChargeBean("DRG", "OUTDRG", outlierRate,
              BigDecimal.ONE, BigDecimal.ZERO, drgCode,
              "DRG Outlier Amount", null, 0, -2, true);
      String drgRemarks = "DRG Outlier Amount : " + outlierRate;
      outlierChargeBean.set(CHARGE_ID,
          billChargeService.getNextPrefixedId());
      outlierChargeBean.set(BILL_NO, billNo);
      outlierChargeBean.set(ACT_RATE_PLAN_ITEM_CODE, "99");
      outlierChargeBean.set(CODE_TYPE, "Service Code");
      outlierChargeBean.set(ACT_REMARKS, drgRemarks);

      drgUpdateService.postBillCharge(outlierChargeBean);
      drgUpdateService.postBillChargeClaims(outlierChargeBean, visitId,
          billNo);
      calculateDrgCodeTaxAmt(outlierChargeBean);
    }

  }

  /**
   * Post add on payment entry.
   *
   * @param billNo  the bill no
   * @param visitId the visit id
   * @param drgCode the drg code
   */
  private void postAddOnPaymentEntry(String billNo, String visitId,
                                     String drgCode) {
    BigDecimal hcpcsPayment = drgUpdateService.getHCPCSPaymentAmt(billNo,
        visitId, drgCode);
    String addOnPaymentRemarks = "HCPCs Add On Payment.   HCPCs Portion Per : "
        + drgUpdateService.getHCPCSFactor(drgCode)
        + "  Add On Payment Factor Per : "
        + drgUpdateService.getAddOnPyamentFactor(visitId);
    if (hcpcsPayment.compareTo(BigDecimal.ZERO) > 0) {
      BasicDynaBean addOnPaymentBean = billChargeService
          .setBillChargeBean("DRG", "APDRG", hcpcsPayment,
              BigDecimal.ONE, BigDecimal.ZERO, drgCode,
              "DRG Add On Payment", null, 0, -2, true);
      addOnPaymentBean.set(CHARGE_ID,
          billChargeService.getNextPrefixedId());
      addOnPaymentBean.set(BILL_NO, billNo);
      addOnPaymentBean.set(ACT_RATE_PLAN_ITEM_CODE, "98");
      addOnPaymentBean.set(CODE_TYPE, "Service Code");
      addOnPaymentBean.set(ACT_REMARKS, addOnPaymentRemarks);

      drgUpdateService.postBillCharge(addOnPaymentBean);
      drgUpdateService.postBillChargeClaims(addOnPaymentBean, visitId,
          billNo);
      calculateDrgCodeTaxAmt(addOnPaymentBean);

    }
  }

  /**
   * Process DRG.
   *
   * @param billNo  String
   * @param drgCode String
   * @return the boolean
   */
  public Boolean processDRG(String billNo, String drgCode) {
    Boolean success = false;
    // If bill status is not open, then don't process DRG calculation.
    BasicDynaBean billBean = billService.findByKey(billNo);
    if (!((String) billBean.get("status")).equals("A")) {
      return true;
    }
    // Item sponsor amount set to zero

    success = billChargeClaimService.setItemsSponsorAmount(billNo);
    success &= salesService.setItemsSponsorAmount(billNo);
    success &= billChargeService.lockItemsInDRGBill(billNo);

    // DRG Base Payment changes after changing Insurance Details

    String visitId = billService.getVisitId(billNo);
    Map drgCodeMap = billService.getDRGCode(visitId);
    drgUpdateService.updateDRGBasePayment(visitId, billNo, drgCode,
        (String) drgCodeMap.get(DRG_CHARGE_ID));

    // Post an adjustment for bill totals
    success &= drgUpdateService.updateAdjustmentEntry(billNo, visitId);
    BasicDynaBean chgBean = drgUpdateService
        .getChargeBeanForAddOnPayMent(billNo);
    if (null != chgBean) {
      drgUpdateService.updateAddOnPayment(visitId, billNo, drgCode);
    } else {
      postAddOnPaymentEntry(billNo, visitId, drgCode);
    }
    BasicDynaBean outlierChgBean = drgUpdateService
        .getChargeBeanForOutlierAmount(billNo);
    if (null != outlierChgBean) {
      drgUpdateService.updateDRGOutlierEntry(visitId, billNo, drgCode);
    } else {
      postDRGOutlierEntry(billNo, visitId, drgCode);
    }
    return success;
  }

  /**
   * Remove DRG happens from edit insurance screen, codification screen.
   *
   * @param billNo the bill no
   */
  public void removeDRG(String billNo) {
    String visitId = billService.getVisitId(billNo);
    drgUpdateService.updateDRGCode(visitId, "");
    billChargeClaimService.cancelDRGItemsClaims(billNo);
    billChargeService.cancelDRGItems(billNo);
    billChargeClaimService.includeItemsInInsCalc(billNo);
    billChargeService.unLockItemsInDrgBill(billNo);
  }

}
