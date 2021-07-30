package com.insta.hms.billing.paymentdetails;

import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.Receipt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PharmacyPaymentDetailsImpl.
 *
 * @author lakshmi.p
 */
public class PharmacyPaymentDetailsImpl extends AbstractPaymentDetails {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(PharmacyPaymentDetailsImpl.class);

  /**
   * The Enum CreditPaymentType.
   */
  public static enum CreditPaymentType {
    /** The hospital. */
    HOSPITAL,
    /** The retail. */
    RETAIL
  }

  /**
   * The Enum Transaction.
   */
  public static enum Transaction {
    /** The estimate. */
    estimate,
    /** The sales. */
    sales,
    /** The payment. */
    payment
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.billing.paymentdetails.AbstractPaymentDetails#
   * generatePrintReceiptUrls(java.util.List, java.util.Map)
   */
  @Override
  public List<String> generatePrintReceiptUrls(List<Receipt> receiptList, Map printParamMap)
      throws SQLException {

    List<HashMap> issuedMedicineList = (List) printParamMap.get("issuedMedicineList");

    String[] consultationIds = printParamMap.get("consultationIds") != null
        ? (String[]) printParamMap.get("consultationIds")
        : null;
    String[] medicationIds = printParamMap.get("medicationIds") != null
        ? (String[]) printParamMap.get("medicationIds")
        : null;
    String salePrintItems = printParamMap.get("salePrintItems") != null
        ? (String) printParamMap.get("salePrintItems")
        : "";
    String templateName = printParamMap.get("templateName") != null
        ? (String) printParamMap.get("templateName")
        : "";
    String lblTemplateName = printParamMap.get("lblTemplateName") != null
        ? (String) printParamMap.get("lblTemplateName")
        : "";

    // Default tranSaction is sales for a sales print. Allowed tranSaction values
    // are estimate, sales, payment
    String transaction = printParamMap.get("transaction") != null
        ? (String) printParamMap.get("transaction")
        : Transaction.sales.toString();
    String visitType = printParamMap.get("visitType") != null
        ? (String) printParamMap.get("visitType")
        : "";
    String returnStr = printParamMap.get("sale_return") != null
        ? (String) printParamMap.get("sale_return")
        : "";
    String saleUnit = printParamMap.get("saleUnit") != null ? (String) printParamMap.get("saleUnit")
        : "";
    String visitId = printParamMap.get("visitId") != null ? (String) printParamMap.get("visitId")
        : "";

    // Default credit_print is hospital for the sales payment. Allowed credit_print
    // values are hospital, retail
    String creditPatientType = printParamMap.get("creditPatientType") != null
        ? (String) printParamMap.get("creditPatientType")
        : CreditPaymentType.HOSPITAL.toString();

    String billNo = printParamMap.get("billNo") != null ? (String) printParamMap.get("billNo")
        : null;
    String customerId = printParamMap.get("customerId") != null
        ? (String) printParamMap.get("customerId")
        : null;
    String doctor = printParamMap.get("doctor") != null ? (String) printParamMap.get("doctor")
        : null;

    String saleId = printParamMap.get("saleId") != null ? (String) printParamMap.get("saleId")
        : null;

    String printerTypeStr = (String) printParamMap.get("printerTypeStr");
    String labelPrintTypeStr = (String) printParamMap.get("labelPrinterType");

    int labelPrinterId = 0;
    int printerId = 0;
    if ((printerTypeStr != null) && !printerTypeStr.equals("")) {
      printerId = Integer.parseInt(printerTypeStr);
    }
    if ((labelPrintTypeStr != null) && !labelPrintTypeStr.equals("")) {
      labelPrinterId = Integer.parseInt(labelPrintTypeStr);
    }

    // create a list of print URLs and set it for printing when the next page loads
    List<String> printUrls = new ArrayList<String>();

    String cxtPath = RequestContext.getHttpRequest().getContextPath();

    if (transaction.equals(Transaction.payment.toString())) {

      String creditPaymentUrl = "";

      if (creditPatientType.equals(CreditPaymentType.HOSPITAL.toString())) {

        creditPaymentUrl = "/pages/stores/PendingSalesBillPrint.do?_method=getCreditReceiptPrint";
        creditPaymentUrl += "&creditPatientType=" + CreditPaymentType.HOSPITAL.toString();

      } else if (creditPatientType.equals(CreditPaymentType.RETAIL.toString())) {

        creditPaymentUrl = "/pages/stores/RetailPendingSalesBillPrint.do?"
            + "_method=getCreditReceiptPrint";
        creditPaymentUrl += "&creditPatientType=" + CreditPaymentType.RETAIL.toString();
      }

      if (receiptList != null && receiptList.size() > 0) {

        for (int i = 0; i < receiptList.size(); i++) {
          Receipt receipt = (Receipt) receiptList.get(i);
          String receiptNo = receipt.getReceiptNo();
          String paymentType = receipt.getPaymentType();

          String payType = "receipt";
          if (paymentType.equals("F")) {
            payType = "refund";
          }

          String popUpUrl = cxtPath;

          popUpUrl += creditPaymentUrl;
          popUpUrl += "&receiptNo=" + receiptNo;
          popUpUrl += "&payType=" + payType;
          popUpUrl += "&billNo=" + billNo;
          popUpUrl += "&customerId=" + customerId;
          popUpUrl += "&printerType=" + printerTypeStr;
          popUpUrl += "&doctor=" + doctor;

          printUrls.add(popUpUrl);
        }
      }

    } else if (transaction.equals(Transaction.estimate.toString())) {
      String popupUrl = cxtPath;
      popupUrl += "/pages/stores/MedicineSalesPrint.do?method=getEstimatePrint";
      popupUrl += "&estimateId=" + saleId;
      popupUrl += "&visitType=" + visitType;
      popupUrl += "&printerId=" + printerId;

      if (saleId != null && !saleId.equals("")) {
        printUrls.add(popupUrl);
      }

    } else if (transaction.equals(Transaction.sales.toString())) {
      String popupUrl = cxtPath;
      popupUrl += "/pages/stores/MedicineSalesPrint.do?method=getSalesPrint";
      popupUrl += "&saleId=" + saleId;
      popupUrl += "&return=" + returnStr;
      popupUrl += "&printerId=" + printerId;
      popupUrl += "&visitType=" + visitType;
      if (consultationIds != null
          && (salePrintItems.equals("BILLPRESC") || salePrintItems.equals("BILLPRESCLABEL"))) {

        for (int i = 0; i < consultationIds.length; i++) {
          String popupPrescriptionUrl = cxtPath;

          if (!consultationIds[i].equals("")) {

            popupPrescriptionUrl += "/print/printPresConsultation.json?consultation_id="
                + consultationIds[i] + "&templateName=" + templateName;
            popupPrescriptionUrl += "&printerId=" + printerId;
            printUrls.add(popupPrescriptionUrl);
          }
        }
      }
      if (medicationIds != null
          && (salePrintItems.equals("BILLPRESC") || salePrintItems.equals("BILLPRESCLABEL"))) {

        for (int i = 0; i < medicationIds.length; i++) {
          String popupDischargeMedicationUrl = cxtPath;

          if (!medicationIds[i].equals("")) {
            popupDischargeMedicationUrl += "/pages/dischargeMedicationPrint.do?"
                + "_method=dischargeMedicationPrint&patient_id="
                + visitId;
            popupDischargeMedicationUrl += "&printerId=" + printerId;
            printUrls.add(popupDischargeMedicationUrl);
          }
        }
      }

      if (returnStr != null && returnStr.equals("false")) {
        if (issuedMedicineList != null && issuedMedicineList.size() > 0
            && (salePrintItems.equals("BILLLABEL") || salePrintItems.equals("BILLPRESCLABEL"))) {
          String popupLabelUrl = cxtPath;

          popupLabelUrl += "/pages/stores/MedicineSalesPrint.do?method=printPrescLabel&saleId="
              + saleId + "&templateName=" + lblTemplateName;
          if (salePrintItems.equals("BILLPRESCLABEL")) {
            popupLabelUrl += "&printerId=" + labelPrinterId;
          } else {
            popupLabelUrl += "&printerId=" + labelPrinterId;
          }

          printUrls.add(popupLabelUrl);
        }
      }

      if (saleId != null && !saleId.equals("")) {
        printUrls.add(popupUrl);
      }
    }

    return printUrls;
  }
}
