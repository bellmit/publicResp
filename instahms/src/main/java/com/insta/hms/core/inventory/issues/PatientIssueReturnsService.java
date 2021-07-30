package com.insta.hms.core.inventory.issues;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillChargeService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientIssueReturnsService.
 */
@Service
public class PatientIssueReturnsService {

  /** The patient issue returns repository. */
  @LazyAutowired
  PatientIssueReturnsRepository patientIssueReturnsRepository;

  /** The bill charge service. */
  @LazyAutowired
  BillChargeService billChargeService;

  /**
   * Sets the issue items for returns.
   *
   * @param totReturnQty
   *          the tot return qty
   * @param totReturnAmt
   *          the tot return amt
   * @param totReturnNet
   *          the tot return net
   * @param updateChargeList
   *          the update charge list
   * @param issuebean
   *          the issuebean
   */
  public void setIssueItemsForReturns(BigDecimal totReturnQty, BigDecimal totReturnAmt,
      BigDecimal totReturnNet, List<BasicDynaBean> updateChargeList, BasicDynaBean issuebean) {

    if (issuebean != null) {

      if (totReturnQty.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal salenet = (BigDecimal) issuebean.get("insurance_claim_amount");
        BigDecimal saleamt = (BigDecimal) issuebean.get("amount");
        BigDecimal saleqty = (BigDecimal) issuebean.get("act_quantity");

        BigDecimal saleReturnQty = ((BigDecimal) issuebean.get("return_qty")).negate();
        saleqty = saleqty.add(saleReturnQty);

        String chargeId = (String) issuebean.get("charge_id");

        /*
         * If the remaining sale qty for the sold item exists then calculate the sale item related
         * return qty, net & amount to be updated.
         */

        if (saleqty.compareTo(BigDecimal.ZERO) > 0) {
          if ((totReturnQty.subtract(saleqty)).compareTo(BigDecimal.ZERO) > 0) {

            BasicDynaBean chargeBean = billChargeService.getBean();

            BigDecimal returnNet = totReturnNet.subtract(salenet);
            BigDecimal returnAmt = totReturnAmt.subtract(saleamt);
            BigDecimal returnQty = totReturnQty.subtract(saleqty);
            BigDecimal discount = ((BigDecimal) issuebean.get("discount"))
                .multiply(totReturnQty.subtract(saleqty));

            chargeBean.set("act_description_id", ((String) issuebean.get("act_description_id")));
            chargeBean.set("return_insurance_claim_amt", (returnNet.negate()));
            chargeBean.set("return_amt", returnAmt.negate());
            chargeBean.set("return_qty", returnQty.negate());
            chargeBean.set("charge_id", chargeId);
            chargeBean.set("discount", discount);

            updateChargeList.add(chargeBean);

            totReturnQty = totReturnQty.subtract(saleqty);
            totReturnNet = totReturnNet.subtract(salenet);
            totReturnAmt = totReturnAmt.subtract(salenet);

          } else if ((totReturnQty.subtract(saleqty)).compareTo(BigDecimal.ZERO) <= 0) {

            BasicDynaBean chargeBean = billChargeService.getBean();

            BigDecimal returnNet = totReturnNet;
            BigDecimal returnAmt = totReturnAmt;
            BigDecimal returnQty = totReturnQty;
            BigDecimal discount = ((BigDecimal) issuebean.get("discount")).multiply(totReturnQty);

            chargeBean.set("act_description_id", (String) issuebean.get("act_description_id"));
            chargeBean.set("return_insurance_claim_amt", returnNet.negate());
            chargeBean.set("return_amt", returnAmt.negate());
            chargeBean.set("return_qty", returnQty.negate());
            chargeBean.set("charge_id", chargeId);
            chargeBean.set("discount", discount);

            updateChargeList.add(chargeBean);

            totReturnQty = totReturnQty.subtract(saleqty);
            totReturnNet = totReturnNet.subtract(salenet);
            totReturnAmt = totReturnAmt.subtract(saleamt);
          }
        }
      }
    }
  }

  /**
   * Gets the issue return charges.
   *
   * @param visitId
   *          the visit id
   * @return the issue return charges
   */
  public List<BasicDynaBean> getIssueReturnCharges(String visitId) {
    return patientIssueReturnsRepository.getIssueReturnCharges(visitId);
  }

  /**
   * Gets the visit item issues.
   *
   * @param visitId
   *          the visit id
   * @param medicineId
   *          the medicine id
   * @param itemBatchId
   *          the item batch id
   * @param storeId
   *          the store id
   * @param returnChargeId
   *          the return charge id
   * @return the visit item issues
   */
  public List<BasicDynaBean> getVisitItemIssues(String visitId, Integer medicineId, int itemBatchId,
      int storeId, String returnChargeId) {
    return patientIssueReturnsRepository.getVisitItemIssues(visitId, medicineId, itemBatchId,
        storeId, returnChargeId);
  }

  /**
   * Update charges for returns.
   *
   * @param saleIdChargesToUpdate
   *          the sale id charges to update
   * @return true, if successful
   */
  public boolean updateChargesForReturns(List<BasicDynaBean> saleIdChargesToUpdate) {
    return patientIssueReturnsRepository.updateChargesForReturns(saleIdChargesToUpdate);
  }

}
