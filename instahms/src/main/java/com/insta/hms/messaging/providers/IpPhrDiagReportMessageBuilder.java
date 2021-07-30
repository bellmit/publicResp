package com.insta.hms.messaging.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpPhrDiagReportMessageBuilder extends OpIpDiagReportMessageBuilder {
  static Logger logger = LoggerFactory
      .getLogger(IpPhrDiagReportMessageBuilder.class);

  public IpPhrDiagReportMessageBuilder() {
    this.addDataProvider(new IpPhrDiagReportDataProvider());
  }
}
