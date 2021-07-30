package com.insta.hms.diagnostics.diagnosticreportdocket;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendingTestsReportGenerator {

  static Logger logger = LoggerFactory
      .getLogger(PendingTestsReportGenerator.class);

  /**
   * Gets the pending tests report.
   *
   * @param visitId
   *          the visit id
   * @return the pending tests report
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   */
  public String getPendingTestsReport(String visitId)
      throws SQLException, IOException, TemplateException {

    Map<String, Object> root = new HashMap<>();
    List<BasicDynaBean> pendingTests = DiagnosticsDAO.getPendingTests(visitId);

    for (int i = 0; i < pendingTests.size(); i++) {
      pendingTests.get(i).set("slno", i + 1);// setting serial number
    }

    root.put("pendingTests", pendingTests);
    root.put("todaysDate", DateUtil.getCurrentTimestamp());
    Template template = AppInit.getFmConfig().getTemplate("PendingTestsTemplate.ftl");
    StringWriter writer = new StringWriter();
    template.process(root, writer);
    StringBuilder html = new StringBuilder(writer.toString());
    writer.close();

    String outString = "";

    if (!pendingTests.isEmpty()) {
      outString = outString.concat(html.toString());
    }
    logger.debug(outString);
    return outString;
  }
}
