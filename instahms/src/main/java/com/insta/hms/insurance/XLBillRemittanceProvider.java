package com.insta.hms.insurance;

import com.insta.hms.billing.BillClaimDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class XLBillRemittanceProvider.
 */
public class XLBillRemittanceProvider extends XLRemittanceProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(XLBillRemittanceProvider.class);

  /**
   * Process Row.
   *
   * @param remAdvice the rem advice
   * @param row       the row
   * @param errorMap  the error map
   * @param claimIds  the claim ids
   * @param billNos   the bill nos
   * @return the remittance advice
   * @throws Exception the exception
   */
  @Override
  public RemittanceAdvice processRow(RemittanceAdvice remAdvice, HSSFRow row, Map errorMap,
      List<String> claimIds, List<String> billNos) throws Exception {

    String idPayer = getIdPayer(row, errorMap);
    if (null == idPayer) {
      return null;
    }

    String itemPaymentReference = getPaymentReference(row);
    if (!validatePaymentReference(row, itemPaymentReference, errorMap)) {
      return null;
    }

    String billNo = getRowBillNo(row);
    if (billNo != null && !billNo.equals("") && !billNos.contains(billNo)) {

      if (!validateRemittanceParameters(row, billNo, errorMap)) {
        return null;
      }

      BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(billNo);
      String claimID = billClaimBean.get("claim_id") != null
          ? (String) billClaimBean.get("claim_id")
          : null;

      if (claimID != null && !claimID.equals("") && !claimIds.contains(claimID)) {

        if (!validateClaimParameters(row, claimID, errorMap)) {
          return null;
        }

        RemittanceAdviceClaim claim = new RemittanceAdviceClaim();
        claim.setClaimID(claimID);
        claim.setIdPayer(idPayer);
        claim.setPaymentReference(itemPaymentReference);
        ArrayList<RemittanceAdviceBill> remBills = getClaimBills(claimID, errorMap);

        if (null != remBills) {
          // .get("error") != null && !claimMap.get("error").equals(""))
          // {
          // ArrayList<RemittanceAdviceBill> remBills =
          // (ArrayList<RemittanceAdviceBill>)claimMap.get("RemAdviceBills");
          claim.setBills(remBills);
          remAdvice.addClaim(claim);
        } else {
          return null;
        }

        claimIds.add(claimID);
      }
    }
    return remAdvice;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#skipRow(org.apache.poi.hssf.usermodel.
   * HSSFRow, java.util.List, java.util.List)
   */
  @Override
  public boolean skipRow(HSSFRow row, List<String> claimIds, List<String> billNos) {
    if (row == null) {
      return true;
    }

    if (row.getRowNum() <= headerRowIndex) {
      return true;
    }

    String billNo = getRowBillNo(row);

    if (billNo == null || billNo.equals("") || billNos.contains(billNo)) {
      return true;
    }
    return false;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#getRequiredColumns()
   */
  @Override
  public String[] getRequiredColumns() {
    return new String[] { "bill_no_heading", "amount_heading", "denial_remarks_heading" };
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.insurance.XLRemittanceProvider#initializeColumnIndices(java.util.Map)
   */
  @Override
  public void initializeColumnIndices(Map<String, Integer> requiredColumnIndexMap) {
    super.initializeColumnIndices(requiredColumnIndexMap);
    this.billNoCellIndex = requiredColumnIndexMap.get("bill_no_heading");
  }

  /**
   * Gets the claim bills.
   *
   * @param claimID  the claim ID
   * @param errorMap the error map
   * @return the claim bills
   * @throws SQLException the SQL exception
   */
  private ArrayList<RemittanceAdviceBill> getClaimBills(String claimID, Map errorMap)
      throws SQLException {

    ArrayList<String> bills = new ArrayList<String>();
    ArrayList<RemittanceAdviceBill> remBills = new ArrayList<RemittanceAdviceBill>();

    Iterator rowIterator = sheet.rowIterator();
    HSSFRow row = null;
    while (rowIterator.hasNext()) {
      row = (HSSFRow) rowIterator.next();

      logger.debug("Reading bill from line number............" + getLineNumber(row));

      if (row.getRowNum() <= headerRowIndex) {
        continue;
      }

      if (row == null) {
        continue;
      }

      String rowBillNo = getRowBillNo(row);

      if (rowBillNo == null || rowBillNo.equals("")) {
        continue;
      }

      String rowClaimID = null;
      BigDecimal billInsuranceClaimAmount = BigDecimal.ZERO;
      BasicDynaBean bill = null;
      if (rowBillNo != null && !rowBillNo.equals("")) {

        bill = billdao.findByKey("bill_no", rowBillNo);
        BasicDynaBean billClaimBean = new BillClaimDAO().getPrimaryBillClaim(rowBillNo);
        rowClaimID = billClaimBean.get("claim_id") != null ? (String) billClaimBean.get("claim_id")
            : null;

        if (rowClaimID == null || rowClaimID.equals("")) {
          errorMap.put("error", "Invalid Bill. Bill Claim ID is Invalid :" + rowBillNo
              + " found at Line number...." + getLineNumber(row));
          return null;
        }

        if (!rowClaimID.equals(claimID)) {
          continue;
        }
      }

      String itemPaymentReference = getPaymentReference(row);
      if (!validatePaymentReference(row, itemPaymentReference, errorMap)) {
        return null;
      }

      BigDecimal paymentAmount = getPaymentAmount(row, errorMap);
      if (null == paymentAmount) {
        return null;
      }

      String denialRemarks = getRowDenialRemarks(row);
      if (rowBillNo != null && !rowBillNo.equals("") && !bills.contains(rowBillNo)) {
        if (bill == null) {
          throw new RuntimeException("Claim bill is null");
        } else {
          billInsuranceClaimAmount = (BigDecimal) bill.get("total_claim");
          RemittanceAdviceBill remBill = new RemittanceAdviceBill();

          remBill.setBillNo(rowBillNo);
          remBill.setPaymentAmount(paymentAmount);
          remBill.setPaymentReference(itemPaymentReference);

          if (billInsuranceClaimAmount.compareTo(paymentAmount) != 0) {
            remBill.setDenialRemarks((denialRemarks == null)
                ? "Denied as the claim total amount and payment amount is not equal."
                : denialRemarks);
          } else {
            remBill.setDenialRemarks(null);
          }

          remBills.add(remBill);
        }
      }
    } // while

    return remBills;
  }
}
