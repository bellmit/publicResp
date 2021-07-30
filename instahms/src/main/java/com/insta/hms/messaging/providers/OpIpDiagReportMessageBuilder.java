package com.insta.hms.messaging.providers;

import com.insta.hms.billing.BillDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.messaging.MessageBuilder;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OpIpDiagReportMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory.getLogger(OpIpDiagReportMessageBuilder.class);

  @Override
  public boolean preProcess(MessageContext context) throws SQLException {
    if (!super.preProcess(context)) {
      return false;
    }
    Map eventData = context.getEventData();
    Map configParams = context.getConfigParams();
    Map messageType = context.getMessageType();
    if (eventData != null) {
      // Auto triggered message
      List<String> handoverreadyreports = new ArrayList<String>();
      String[] reportIds = null;
      String billNo = (String) eventData.get("billNo");
      String forceResend = (String) eventData.get("forceResend");
      if (billNo != null && !billNo.equals("")) {
        if (null != configParams && configParams.get("check_patient_due") != null
            && configParams.get("check_patient_due").equals("Y")) {
          List<String> reportIdsList = BillDAO.getSignoffReportIdsforBill(billNo);
          if (reportIdsList.size() > 0) {
            reportIds = reportIdsList.toArray(new String[reportIdsList.size()]);
            handoverreadyreports.addAll(Arrays.asList(reportIds));
          }
        }
      } else {
        reportIds = (String[]) eventData.get("report_id");
        boolean checkPatientDue = null != configParams
            && configParams.get("check_patient_due") != null
            && configParams.get("check_patient_due").equals("Y");
        for (String reportId : reportIds) {
          if (checkPatientDue && LaboratoryDAO.isBillPending(reportId)) {
            logger.info("Skipping the message for ReportId: " + reportId + ", as bill pending");
          } else {
            if (LaboratoryDAO.isConfidentialReport(Integer.parseInt(reportId)) != null) {
              handoverreadyreports.add(reportId);
            }
          }
        }
      }
      List<String> messageSentreports = new ArrayList<String>();
      if (forceResend != null && forceResend.equals("Block")) {
        for (String reportId : handoverreadyreports) {
          boolean reportsent = false;
          reportsent = MessageBuilder.isMessageSent(reportId,
              (String) messageType.get("message_type_id"));
          if (reportsent) {
            messageSentreports.add(reportId);
          }
        }
      }
      for (String sentReportId : messageSentreports) {
        handoverreadyreports.remove(sentReportId);
      }
      if (handoverreadyreports.isEmpty()) {
        return false;
      }

      context.getEventData().put("handoverreadyreports",
          handoverreadyreports.toArray(new String[handoverreadyreports.size()]));
    }
    return true;
  }

}