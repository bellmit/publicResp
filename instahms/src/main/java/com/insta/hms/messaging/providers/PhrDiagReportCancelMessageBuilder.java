package com.insta.hms.messaging.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhrDiagReportCancelMessageBuilder extends OpIpDiagReportMessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(PhrDiagReportCancelMessageBuilder.class);

  public PhrDiagReportCancelMessageBuilder() {
    this.addDataProvider(new PhrDiagReportCancelDataProvider());
  }
}
