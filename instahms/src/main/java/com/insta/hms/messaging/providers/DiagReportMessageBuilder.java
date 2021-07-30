package com.insta.hms.messaging.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagReportMessageBuilder extends OpIpDiagReportMessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(DiagReportMessageBuilder.class);

  public DiagReportMessageBuilder() {
    this.addDataProvider(new DiagReportDataProvider());
  }

}
