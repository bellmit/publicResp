package com.insta.hms.billing.paymentdetails;

import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.BillConstants;
import com.insta.hms.billing.Receipt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class BillPaymentDetailsImpl.
 *
 * @author lakshmi.p
 */
public class BillPaymentDetailsImpl extends AbstractPaymentDetails {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.billing.paymentdetails.AbstractPaymentDetails#
   * generatePrintReceiptUrls(java.util.List, java.util.Map)
   */
  @Override
  public List generatePrintReceiptUrls(List<Receipt> receiptList, Map printParamMap)
      throws SQLException {

    String customTemplate = (String) printParamMap.get("customTemplate");
    String printerTypeStr = (String) printParamMap.get("printerTypeStr");
    boolean isPaymentByDeposit = printParamMap.get("isPaymentByDeposit") != null
        ? (Boolean) printParamMap.get("isPaymentByDeposit")
        : false;
    boolean isPatientCreditNote = printParamMap.get("patient_credit_note") != null
        ? (Boolean) printParamMap.get("patient_credit_note")
        : false;
    boolean isPaymentByRewardPoints = printParamMap.get("isPaymentByRewardPoints") != null
        ? (Boolean) printParamMap.get("isPaymentByRewardPoints")
        : false;
    String billNoStr = printParamMap.get("billNo") != null ? (String) printParamMap.get("billNo")
        : null;
    String patientId = printParamMap.get("patient_id") != null
        ? (String) printParamMap.get("patient_id") : null;
    String visitType = printParamMap.get("visit_type") != null
        ? (String) printParamMap.get("visit_type") : null;

    int printerType = 0;
    if ((printerTypeStr != null) && !printerTypeStr.equals("")) {
      printerType = Integer.parseInt(printerTypeStr);
    }

    boolean billCumReceipt = false;
    List<String> printUrls = new ArrayList<String>();
    if (receiptList != null && receiptList.size() > 0) {

      // create a list of print URLs and set it for printing when the next page loads

      for (int i = 0; i < receiptList.size(); i++) {
        Receipt receipt = (Receipt) receiptList.get(i);
        String receiptPrintFormat = receipt.getReceiptPrintFormat();
        if (receiptPrintFormat.equals(Receipt.BILL_CUM_RECEIPT_PRINT)) {
          billCumReceipt = true;
          break;
        }
      }

      if (isPatientCreditNote) {
        billCumReceipt = true;
      }
      if (billCumReceipt) {
        Receipt receipt = (Receipt) receiptList.get(0);
        String billNo;
        if (isPatientCreditNote) {
          billNo = billNoStr;
        } else {
          billNo = receipt.getBillNo();
        }
        String url = RequestContext.getHttpRequest().getContextPath();

        url += "/pages/Enquiry/billprint.do";
        String[] parts = customTemplate.split("-", 2);
        if (parts[0].equals("CUSTOM")) {
          url += "?_method=billPrintTemplate&billType=" + parts[1];
        } else {
          url += "?_method=billPrint";
          String[] options = parts[1].split("-", 2);
          url += "&detailed=" + options[0];
          url += "&option=" + options[1];
        }
        url += "&printerType=" + printerType;
        url += "&billNo=" + billNo;

        printUrls.add(url);

      } else {

        for (int i = 0; i < receiptList.size(); i++) {

          Receipt receipt = (Receipt) receiptList.get(i);
          int paymentModeId = receipt.getPaymentModeId();
          if (BillConstants.depositSetoffPaymentModes.contains(paymentModeId)) {
            continue;
          }
          String url = RequestContext.getHttpRequest().getContextPath();
          String receiptNo = receipt.getReceiptNo();

          url += "/pages/Enquiry/billprint.do?_method=receiptRefundPrintTemplate";
          url += "&printerType=" + printerType;
          url += "&receiptNo=" + receiptNo;
          url += "&patientId=" + patientId;
          url += "&visitType=" + visitType;
          String receiptPrintFormat = receipt.getReceiptPrintFormat();
          if (receiptPrintFormat.equals(Receipt.RECEIPT_PRINT)) {
            url += "&type=R";
          } else if (receiptPrintFormat.equals(Receipt.REFUND_PRINT)) {
            url += "&type=F";
          } else if (receiptPrintFormat.equals(Receipt.SPONSOR_RECEIPT_PRINT)) {
            url += "&type=S";
          }
          printUrls.add(url);
        }
      }
    }

    // Print bill cum receipt if bill payment is through deposits or reward points
    if (!billCumReceipt && (isPaymentByDeposit || isPaymentByRewardPoints)) {
      String url = RequestContext.getHttpRequest().getContextPath();

      url += "/pages/Enquiry/billprint.do";
      String[] parts = customTemplate.split("-", 2);
      if (parts[0].equals("CUSTOM")) {
        url += "?_method=billPrintTemplate&billType=" + parts[1];
      } else {
        url += "?_method=billPrint";
        String[] options = parts[1].split("-", 2);
        url += "&detailed=" + options[0];
        url += "&option=" + options[1];
      }
      url += "&printerType=" + printerType;
      url += "&billNo=" + billNoStr;

      printUrls.add(url);
    }

    return printUrls;
  }

  /**
   * Generate print deposit receipt urls.
   *
   * @param receiptList
   *          the receipt list
   * @param printParamMap
   *          the print param map
   * @return the list
   */
  public static List<String> generatePrintDepositReceiptUrls(List<Receipt> receiptList,
      Map printParamMap) {

    List<String> printUrls = new ArrayList<String>();

    String printTemplate = (String) printParamMap.get("printTemplate");
    String printerTypeStr = (String) printParamMap.get("printer");
    String mrNo = (String) printParamMap.get("mrNo");

    int printerType = 0;
    if ((printerTypeStr != null) && !printerTypeStr.equals("")) {
      printerType = Integer.parseInt(printerTypeStr);
    }

    if (receiptList != null && receiptList.size() > 0) {

      String ctxtPath = RequestContext.getHttpRequest().getContextPath();
      String url = "/pages/BillDischarge/DepositPrint.do?_method=depositPrint";

      for (int i = 0; i < receiptList.size(); i++) {
        Receipt receipt = (Receipt) receiptList.get(i);
        String depositType = receipt.getPaymentType();
        String receiptNo = receipt.getReceiptNo();

        String popupUrl = ctxtPath + url;
        popupUrl += "&deposit_no=" + receiptNo;
        popupUrl += "&printDepositType=" + depositType;
        popupUrl += "&mrNo=" + mrNo;
        popupUrl += "&printTemplate=" + printTemplate;
        popupUrl += "&printerType=" + printerType;

        printUrls.add(popupUrl);
      }
    }
    return printUrls;
  }
}
