package com.insta.hms.common.ftl;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FTL report generator. This can be used for generating reports/prints that make their
 * own queries instead of the caller having to pass in the query results.
 * Some basic parameters (eg, dates for reports, object ID for prints) need to be passed in
 */
public class FtlReportGenerator {

  private Map reportParams = new HashMap();
  private Template templ;

  public FtlReportGenerator(String reportName) throws IOException {
    templ = AppInit.getFmConfig().getTemplate(reportName + ".ftl");
    setFtlMethods();
  }

  public FtlReportGenerator(String name, Reader reader) throws IOException {
    templ = new Template("custom_" + name, reader, AppInit.getFmConfig());
    setFtlMethods();
  }

  private void setFtlMethods() {
    // initialize methods for the FTL
    reportParams.put("queryToDynaList", new FtlMethods.FtlMethodQueryToDynaList());
    reportParams.put("queryToDynaBean", new FtlMethods.FtlMethodQueryToDynaBean());
    reportParams.put("listBeanToMapBean", new FtlMethods.FtlMethodListBeanToMapBean());
    reportParams.put("listBeanToMapNumeric", new FtlMethods.FtlMethodListBeanToMapNumeric());
    reportParams.put("listBeanToMapMapNumeric", new FtlMethods.FtlMethodListBeanToMapMapNumeric());
    reportParams.put("listBeanToMapMapMapNumeric",
        new FtlMethods.FtlMethodListBeanToMapMapMapNumeric());
    reportParams.put("getDatesInRange", new FtlMethods.FtlMethodGetDatesInRange());
    int centerId = RequestContext.getCenterId();
    reportParams.put("getScreenLogoPath", new FtlMethods.FtlMethodGetScreenLogoPath(centerId));
    reportParams.put("getNumberConversion", new FtlMethods.FtlMethodNumberConversion());
    reportParams.put("getNumberToWordConversion", new FtlMethods.FtlMethodNumberToWordConversion());
    // todo: more methods
  }

  public void setReportParams(Map reportParams) {
    this.reportParams = reportParams;
    setFtlMethods();
  }

  /**
   * Sets the parameters for the report from a HTTP request parameter map like map, ie, a map of
   * name => String[] pairs.
   */
  public void setParamsFromParamMap(Map paramMap) throws java.text.ParseException {

    for (Map.Entry e : (Set<Map.Entry>) paramMap.entrySet()) {
      String name = (String) e.getKey();
      String[] values = (String[]) e.getValue();

      if (name.startsWith("_")) {
        continue;
      }

      if ("fromDate".equals(name)) {
        reportParams.put("fromDate", DateUtil.parseDate(values[0]));
      } else if ("toDate".equals(name)) {
        reportParams.put("toDate", DateUtil.parseDate(values[0]));
      } else if ("toDateTime".equals(name)) {
        reportParams.put("toDateTime", DateUtil.parseTimestamp(values[0]));
      } else if ("fromDateTime".equals(name)) {
        reportParams.put("fromDateTime", DateUtil.parseTimestamp(values[0]));

      } else if (name.endsWith("Array")) {
        reportParams.put(name, values);
      } else {
        reportParams.put(name, values[0]);
      }
    }
  }

  public void setParam(String name, Object value) throws java.text.ParseException {
    reportParams.put(name, value);
  }

  /**
   * Run pdf report.
   *
   * @param os the os
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws SQLException the SQL exception
   */
  public void runPdfReport(OutputStream os)
      throws IOException, TemplateException, DocumentException, SQLException {

    StringWriter writer = new StringWriter();
    templ.process(reportParams, writer);
    String htmlContent = writer.toString();
    HtmlConverter hc = new HtmlConverter();
    hc.writePdf(os, htmlContent);
    os.close();
  }

  public void runScreenReport(Writer out) throws IOException, TemplateException {
    templ.process(reportParams, out);
  }

  public void process(Writer out) throws IOException, TemplateException {
    templ.process(reportParams, out);
  }

  /**
   * Gets the pdf bytes.
   *
   * @return the pdf bytes
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   */
  public byte[] getPdfBytes() throws IOException, TemplateException, DocumentException {

    byte[] bytes = null;

    StringWriter writer = new StringWriter();
    templ.process(reportParams, writer);
    String htmlContent = writer.toString();

    HtmlConverter hc = new HtmlConverter();
    return hc.getPdfBytes(htmlContent);
  }

  /**
   * Gets the plain text.
   *
   * @param paramMap the param map
   * @return the plain text
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   */
  public String getPlainText(Map paramMap)
      throws IOException, TemplateException, DocumentException {
    StringWriter writer = new StringWriter();
    setReportParams(paramMap);
    process(writer);
    return writer.toString();
  }
  // todo: text mode output
}
