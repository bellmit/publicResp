package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageBuilder;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Map;

public class ReportReadyMessageBuilder extends MessageBuilder {
  static Logger logger = LoggerFactory.getLogger(ReportReadyMessageBuilder.class);

  public ReportReadyMessageBuilder() {
    this.addDataProvider(new DiagReportDataProvider());
  }

  @Override
  public boolean preProcess(MessageContext context) throws SQLException {
    if (!super.preProcess(context)) {
      return false;
    }
    Map eventData = context.getEventData();
    if (eventData != null) {
      String[] reportIds = (String[]) eventData.get("report_id");
      eventData.put("handoverreadyreports", reportIds);
    }
    return true;
  }

}
