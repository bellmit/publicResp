package com.insta.hms.messaging.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpPhrDiagReportMessageBuilder extends OpIpDiagReportMessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(OpPhrDiagReportMessageBuilder.class);

  public OpPhrDiagReportMessageBuilder() {
    this.addDataProvider(new PhrDiagReportDataProvider());
  }
}
