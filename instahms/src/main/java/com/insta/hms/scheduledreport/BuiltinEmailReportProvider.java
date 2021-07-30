package com.insta.hms.scheduledreport;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.common.Screen;
import com.bob.hms.common.ScreenConfig;
import com.insta.hms.billing.RevenueReportFtlHelper;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.common.ftl.FtlReportGenerator;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class BuiltinEmailReportProvider.
 */
public class BuiltinEmailReportProvider implements EmailReportProviderInterface {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(BuiltinEmailReportProvider.class);

  /**
   *  (non-Javadoc).
   *
   * @return the list
   * @throws Exception the exception
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#listAvailableReports()
   */
  public List<Map> listAvailableReports() throws Exception {

    Set<String> builtinReportIds = new HashSet<>();

    // get the list of jrxml and ftl reports from the StrutsDescParser.
    Map jrxmls = StrutsDescParser.getjrXmlReportInfoMap();
    Map ftls = StrutsDescParser.getFtlReportInfoMap();

    builtinReportIds.addAll(jrxmls.keySet());
    builtinReportIds.addAll(ftls.keySet());
    // add special reports which don't fit into any of the above. Use the same
    // ID as in struts config so as to avoid conflicts with the above.
    builtinReportIds.add("billing_daybook");
    builtinReportIds.add("rep_rev_dashboard");

    // fill in the report names from the screen config
    List<Map> availableReports = new ArrayList();
    for (String reportId : builtinReportIds) {
      Map report = new HashMap();
      report.put("report_id", reportId);
      report.put("report_name", getReportName(reportId));
      availableReports.add(report);
    }

    // sort it by report Name
    Collections.sort(availableReports, new Comparator<Map>() {
      public int compare(Map map1, Map map2) {
        if (map1 == null && map2 == null) {
          return 0;
        }

        if (map1 != null && map2 != null && map1.get("report_name") == null
            && map2.get("report_name") == null) {
          return 0;
        }

        if (map1 == null || map1.get("report_name") == null) {
          return -1;
        }

        if (map2 == null || map2.get("report_name") == null) {
          return 1;
        }

        return ((String) map1.get("report_name"))
            .compareToIgnoreCase((String) map2.get("report_name"));
      }
    });

    return availableReports;
  }

  /** (non-Javadoc)
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#getReportBytes
   * (java.lang.String, java.lang.String, java.util.Map)
   */
  public byte[] getReportBytes(String reportId, String format, Map params) throws Exception {

    // check if it is a special report
    if (reportId.equals("rep_rev_dashboard")) {
      try (Connection con = DataBaseUtil.getReadOnlyConnection()) {
        if (params.get("dateField") == null) {
          params.put("dateField", "finalized_date");
        }
        params.put("format", format);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        new RevenueReportFtlHelper(AppInit.getFmConfig()).getDashboardReport(con, params, os);
        return os.toByteArray();
      }
    } else if (reportId.equals("billing_daybook")) {
      // todo: we can support text also, might as well support it?
      return ReportPrinter.getPdfBytes("HospitalDayBook", params);
    }

    // check if it is a builtin jrxml report
    Map jrxmls = StrutsDescParser.getjrXmlReportInfoMap();
    String[] reportDesc = (String[]) jrxmls.get(reportId);
    if (reportDesc != null) {
      String reportName = reportDesc[1];
      return ReportPrinter.getPdfBytes(reportName, params);
    }

    // check whether it is a builtin ftl report
    Map ftls = StrutsDescParser.getFtlReportInfoMap();
    reportDesc = (String[]) ftls.get(reportId);
    if (reportDesc != null) {
      String reportName = reportDesc[1];
      FtlReportGenerator ftlGen = new FtlReportGenerator(reportName);
      ftlGen.setReportParams(params);
      return ftlGen.getPdfBytes();
    }

    return null;
  }

  /** The screen config. */
  private static ScreenConfig screenConfig;

  /** (non-Javadoc)
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#getReportName(java.lang.String)
   */
  public String getReportName(String reportId) throws IOException, org.xml.sax.SAXException {
    if (screenConfig == null) {
      if (RequestContext.getSession() != null) {
        log.debug("Using screen config from request context");
        screenConfig = (ScreenConfig) RequestContext.getSession().getServletContext()
            .getAttribute("screenConfig");
      } else {
        // not called from within struts. We need to construct our own screenconfig
        log.debug("Parsing screens config again");
        String rootRealPath = AppInit.getRootRealPath();
        Digester screensDigester = DigesterLoader.createDigester(new org.xml.sax.InputSource(
            new FileInputStream(new File(rootRealPath + "/WEB-INF/screens-digester.xml"))));
        screenConfig = (ScreenConfig) screensDigester
            .parse(new File(rootRealPath + "/WEB-INF/screens.xml"));
      }
    }

    Screen scr = screenConfig.getScreen(reportId);
    if (scr == null) {
      log.error("No screen config found for: " + reportId);
      return null;
    }
    return scr.getName();
  }
}
