package com.insta.hms.billing.paymentdetails;

import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.Receipt;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class IncomingPaymentDetailsImpl.
 *
 * @author lakshmi.p
 */
public class IncomingPaymentDetailsImpl extends AbstractPaymentDetails {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.billing.paymentdetails.AbstractPaymentDetails#
   * generatePrintReceiptUrls(java.util.List, java.util.Map)
   */
  @Override
  public List generatePrintReceiptUrls(List<Receipt> receiptList, Map printParamMap)
      throws SQLException {

    List<String> printUrls = new ArrayList<String>();

    String incomingVisitId = (String) printParamMap.get("incomingVisitId");
    String printerTypeStr = (String) printParamMap.get("printerTypeStr");
    String category = (String) printParamMap.get("category");
    String billNo = (String) printParamMap.get("billNo");
    String billType = printParamMap.get("billType") != null ? (String) printParamMap.get("billType")
        : "C";

    String printType = GenericPreferencesDAO.getGenericPreferences().getSampleCollectionPrintType();

    String billPrint = printParamMap.get("BILLPRINT") != null
        ? (String) printParamMap.get("BILLPRINT")
        : "N";
    String sampleNo = printParamMap.get("sampleNo") != null ? (String) printParamMap.get("sampleNo")
        : "";
    String samplDates = printParamMap.get("sampleDates") != null
        ? (String) printParamMap.get("sampleDates")
        : "";
    String sampleTypes = printParamMap.get("sampleTypes") != null
        ? (String) printParamMap.get("sampleTypes")
        : "";
    String sampleBardCodeTemplate = printParamMap.get("sampleBardCodeTemplate") != null
        ? (String) printParamMap.get("sampleBardCodeTemplate")
        : "";
    String cxtPath = RequestContext.getHttpRequest().getContextPath();
    String receiptCategoryUrl = "";

    int printerId = 0;
    if ((printerTypeStr != null) && !printerTypeStr.equals("")) {
      printerId = Integer.parseInt(printerTypeStr);
    }

    if (category.equals("DEP_LAB")) {
      receiptCategoryUrl =
          "/pages/DiagnosticModule/IncomingRegistraionLaboratory.do?"
          + "_method=getIncomingSampleBillPrint";

    } else {
      receiptCategoryUrl =
          "/pages/DiagnosticModule/IncomingRegistraionLaboratory.do?"
          + "_method=getIncomingSampleBillPrint";
    }

    // Bill Cum receipt print
    if (billPrint.equals("Y") || billType.equals("P")) {

      String popUpUrl = cxtPath;

      popUpUrl += receiptCategoryUrl;
      popUpUrl += "&billNo=" + billNo;
      popUpUrl += "&incomingVisitId=" + incomingVisitId;
      popUpUrl += "&printerId=" + printerId;
      popUpUrl += "&category=" + category;
      popUpUrl += "&BILLPRINT=Y";

      printUrls.add(popUpUrl);

    } else if (receiptList != null && receiptList.size() > 0) {

      // create a list of print URLs and set it for printing when the next page loads

      for (int i = 0; i < receiptList.size(); i++) {
        Receipt receipt = (Receipt) receiptList.get(i);
        String receiptNo = receipt.getReceiptNo();
        String paymentType = receipt.getPaymentType();

        String popUpUrl = cxtPath;

        popUpUrl += receiptCategoryUrl;
        popUpUrl += "&receiptNo=" + receiptNo;
        popUpUrl += "&paymentType=" + paymentType;
        popUpUrl += "&billNo=" + billNo;
        popUpUrl += "&incomingVisitId=" + incomingVisitId;
        popUpUrl += "&printerId=" + printerId;
        popUpUrl += "&category=" + category;
        popUpUrl += "&BILLPRINT=" + billPrint;

        printUrls.add(popUpUrl);
      }
    }

    String popUpSampleUrl = cxtPath;
    String diagCategoryUrl = "";

    if (category.equals("DEP_LAB")) {
      diagCategoryUrl = "/pages/DiagnosticModule/IncomingRegistraionLaboratory.do";

    } else {
      diagCategoryUrl = "/pages/DiagnosticModule/IncomingRegistraionRadiology.do";
    }
    String needPrint = printParamMap.get("needPrint") != null
            ? (String) printParamMap.get("needPrint") : "";
    if (needPrint.equals("Y")) {

      if (printType != null && printType.equals("SL")) {
        popUpSampleUrl += diagCategoryUrl + "?_method=generateSampleCollectionReport";
        popUpSampleUrl += "&visitid=" + incomingVisitId;
        popUpSampleUrl += "&sampleNo=" + sampleNo;
        popUpSampleUrl += "&sampleDates=" + samplDates;
        popUpSampleUrl += "&sampleTypes=" + sampleTypes;
      } else {
        popUpSampleUrl += "/Laboratory/GenerateSamplesBarCodePrint.do?method=execute";
        popUpSampleUrl += "&visitId=" + incomingVisitId;
        popUpSampleUrl += "&sampleNo=" + sampleNo;
        popUpSampleUrl += "&sampleDates=" + samplDates;
        popUpSampleUrl += "&sampleTypes=" + sampleTypes;
        popUpSampleUrl += "&barcodeType=sample";
        popUpSampleUrl += "&template_name=" + sampleBardCodeTemplate;
      }

      printUrls.add(popUpSampleUrl);
    }

    return printUrls;
  }
}
