package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class ReportPrinter. New recommended way of using JaspreReports. Compiles on-the-fly during
 * report development, and also uses existing compiled files during deployment. Handles JRXMLs
 * stored within the WEB-INF/jrxml directory.
 */
public class ReportPrinter extends JRTextExporter {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(ReportPrinter.class);

  /** The Constant JRXML_DIR. */
  public static final String JRXML_DIR = "/WEB-INF/jrxml";

  /** The Constant JASPER_DIR. */
  public static final String JASPER_DIR = "/WEB-INF/jasper";

  /**
   * Prints the pdf stream.
   *
   * @param req            the req
   * @param res            the res
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void printPdfStream(HttpServletRequest req, HttpServletResponse res,
      String reportBaseName, Map parameterMap) throws JRException, IOException, SQLException {
    printPdfStream(req, res, reportBaseName, parameterMap, null, null);
  }

  /**
   * Prints the pdf stream. Write directly to output stream: use when you are writing to a file or
   * any other output stream (eg, ZipOutputStream).
   *
   * @param os             the os
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void printPdfStream(OutputStream os, String reportBaseName, Map parameterMap,
      PrintPageOptions opts) throws JRException, IOException, SQLException {

    String jasperPath = compileReport(reportBaseName, opts);

    parameterMap.put("SUBREPORT_DIR", AppInit.getRootRealPath() + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);
    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    try {
      InputStream is = JRLoader.getFileInputStream(jasperPath);
      JasperRunManager.runReportToPdfStream(is, os, parameterMap, con);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Prints the pdf stream. Write the report to the HttpServletResponse
   *
   * @param req            the req
   * @param res            the res
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void printPdfStream(HttpServletRequest req, HttpServletResponse res,
      String reportBaseName, Map parameterMap, PrintPageOptions opts, String[] subReportsUsed)
      throws JRException, IOException, SQLException {

    String jasperPath = compileReport(reportBaseName, opts);
    if (subReportsUsed != null) {
      for (int i = 0; i < subReportsUsed.length; i++) {
        // sub-reports are not expected to use page options
        compileReport(subReportsUsed[i], null);
      }
    }

    parameterMap.put("SUBREPORT_DIR", AppInit.getRootRealPath() + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);

    JRSwapFile swap = new JRSwapFile("/tmp", 2048, 1024);
    JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(50, swap, true);
    parameterMap.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);

    try {
      logger.debug("Filling report");
      JasperPrint print = JasperFillManager.fillReport(jasperPath, parameterMap, con);

      logger.debug("Exporting PDF report");
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      JasperExportManager.exportReportToPdfStream(print, os);

      logger.debug("Done printing report.");
      os.flush();
      os.close();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (virtualizer != null) {
        virtualizer.cleanup();
      }
    }
  }

  /**
   * Adds the decimal params.
   *
   * @param parameterMap the parameter map
   * @throws SQLException the SQL exception
   */
  public static void addDecimalParams(Map parameterMap) throws SQLException {
    int decimaldigits = GenericPreferencesDAO.getGenericPreferences().getDecimalDigits();
    parameterMap.put("decimalplaces", decimaldigits);
  }

  /**
   * Compile report.
   *
   * @param reportBaseName the report base name
   * @param opts           the opts
   * @return the string
   * @throws JRException the JR exception
   */
  private static String compileReport(String reportBaseName, PrintPageOptions opts)
      throws JRException {

    String jrxmlPath = AppInit.getRootRealPath() + JRXML_DIR + "/" + reportBaseName + ".jrxml";
    /*
     * We need different jaspers for each set of options, so include the opts' string representation
     * in the compiled output file name. This way, the jaspers can be cached across hospitals.
     */
    String optsSuffix = (opts != null) ? "_" + opts.toString() : "";
    String jasperPath = AppInit.getRootRealPath() + JASPER_DIR + "/" + reportBaseName + optsSuffix
        + ".jasper";

    File jrxmlFile = new File(jrxmlPath);
    File jasperFile = new File(jasperPath);

    if ((jasperFile.lastModified() == 0)
        || (jrxmlFile.lastModified() > jasperFile.lastModified())) {

      logger.debug("Compiling " + jrxmlFile + " to: " + jasperFile);
      if (opts == null) {
        JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);
      } else {
        JasperDesign jrd = JRXmlLoader.load(jrxmlFile);

        if (opts.leftMargin != -1) {
          jrd.setLeftMargin(opts.leftMargin);
        }

        if (opts.rightMargin != -1) {
          jrd.setRightMargin(opts.rightMargin);
        }

        if (opts.pageWidth != -1) {
          jrd.setPageWidth(opts.pageWidth);
        }

        if (opts.continuousFeed.equals("Y")) {
          jrd.setIgnorePagination(true);
        } else {
          jrd.setTopMargin(opts.topMargin);
          jrd.setBottomMargin(opts.bottomMargin);
          jrd.setPageHeight(opts.pageHeight);
        }

        if (opts.orientation.equals("L")) {
          jrd.setOrientation(JRReport.ORIENTATION_LANDSCAPE);
        } else {
          jrd.setOrientation(JRReport.ORIENTATION_PORTRAIT);
        }

        if (opts.repeatPatientInfo) {
          JRGroup patientHeaderGroup = null;
          JRGroup[] groups = (JRGroup[]) jrd.getGroups();
          if (groups != null) {
            for (int i = 0; i < groups.length; i++) {
              if (groups[i].getName().equals("patientHeader")) {
                patientHeaderGroup = groups[i];
              }
            }
          }
          if (patientHeaderGroup != null) {
            patientHeaderGroup.setReprintHeaderOnEachPage(true);
          }
        }

        JasperCompileManager.compileReportToFile(jrd, jasperPath);
      }
      logger.debug("Finished compilation.");
    } else {
      logger.debug("Using pre-compiled report: " + jasperPath);
    }

    return jasperPath;
  }

  /**
   * Gets the pdf bytes.
   *
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @return the pdf bytes
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static byte[] getPdfBytes(String reportBaseName, Map parameterMap)
      throws JRException, IOException, SQLException {
    return getPdfBytes(reportBaseName, parameterMap, null, null);
  }

  /**
   * Gets the pdf bytes. Returns a byte array instead of printing to the output stream
   *
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @return the pdf bytes
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static byte[] getPdfBytes(String reportBaseName, Map parameterMap, PrintPageOptions opts,
      String[] subReportsUsed) throws JRException, IOException, SQLException {
    byte[] bytes = null;

    String jasperPath = compileReport(reportBaseName, null);
    if (subReportsUsed != null) {
      for (int i = 0; i < subReportsUsed.length; i++) {
        // sub-reports are not expected to use page options
        compileReport(subReportsUsed[i], null);
      }
    }

    parameterMap.put("SUBREPORT_DIR", AppInit.getRootRealPath() + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);
    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    try {
      logger.debug("Loading report");
      InputStream is = JRLoader.getFileInputStream(jasperPath);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JasperRunManager.runReportToPdfStream(is, baos, parameterMap, con);
      bytes = baos.toByteArray();
      logger.debug("Done printing report.");
      baos.close();

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return bytes;
  }

  /**
   * Prints the text report.
   *
   * @param req            the req
   * @param res            the res
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void printTextReport(HttpServletRequest req, HttpServletResponse res,
      String reportBaseName, Map parameterMap) throws JRException, IOException, SQLException {
    printTextReport(req, res, reportBaseName, parameterMap, null, null);
  }

  /**
   * Prints the text report.
   *
   * @param req            the req
   * @param res            the res
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param subReportsUsed the sub reports used
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void printTextReport(HttpServletRequest req, HttpServletResponse res,
      String reportBaseName, Map parameterMap, String[] subReportsUsed)
      throws JRException, IOException, SQLException {
    printTextReport(req, res, reportBaseName, parameterMap, null, subReportsUsed);
  }

  /**
   * Prints the text report.
   *
   * @param req            the req
   * @param res            the res
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void printTextReport(HttpServletRequest req, HttpServletResponse res,
      String reportBaseName, Map parameterMap, PrintPageOptions opts, String[] subReportsUsed)
      throws JRException, IOException, SQLException {

    String jasperPath = compileReport(reportBaseName, opts);
    if (subReportsUsed != null) {
      for (int i = 0; i < subReportsUsed.length; i++) {
        // sub-reports are not expected to use page options
        compileReport(subReportsUsed[i], null);
      }
    }

    parameterMap.put("SUBREPORT_DIR", AppInit.getRootRealPath() + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);

    JRSwapFile swap = new JRSwapFile("/tmp", 2048, 1024);
    JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(50, swap, true);
    parameterMap.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);

    try {
      logger.debug("Filling report");

      logger.debug("Exporting text report");
      res.setContentType("text/plain");
      res.setHeader("Content-disposition", "attachment; filename=" + reportBaseName + ".txt");

      JRTextExporter textExporter = new JRTextExporter();

      JasperPrint print = JasperFillManager.fillReport(jasperPath, parameterMap, con);
      textExporter.setParameter(JRTextExporterParameter.JASPER_PRINT, print);
      OutputStream os = res.getOutputStream();
      textExporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, os);
      textExporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, opts.pageHeight);
      textExporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, opts.charWidth);
      textExporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Integer(12));
      textExporter.setParameter(JRTextExporterParameter.LINE_SEPARATOR,
          JRTextExporter.systemLineSeparator);

      textExporter.exportReport();

      logger.debug("Done Exporting  report.");
      os.flush();
      os.close();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (virtualizer != null) {
        virtualizer.cleanup();
      }
    }
  }

  /**
   * Prints the text report.
   *
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @return the string
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static String printTextReport(String reportBaseName, Map parameterMap,
      PrintPageOptions opts, String[] subReportsUsed)
      throws JRException, IOException, SQLException {

    String jasperPath = compileReport(reportBaseName, opts);
    if (subReportsUsed != null) {
      for (int i = 0; i < subReportsUsed.length; i++) {
        compileReport(subReportsUsed[i], null);
      }
    }

    parameterMap.put("SUBREPORT_DIR", AppInit.getRootRealPath() + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);
    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    ByteArrayOutputStream writer = new ByteArrayOutputStream();

    try {
      logger.debug("Loading report");

      JasperPrint print = JasperFillManager.fillReport(jasperPath, parameterMap, con);
      JRTextExporter textExporter = new JRTextExporter();
      textExporter.setParameter(JRTextExporterParameter.JASPER_PRINT, print);
      textExporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, writer);

      // options
      textExporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, opts.charWidth);
      textExporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, opts.pageHeight);
      textExporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Integer(12));
      textExporter.setParameter(JRTextExporterParameter.LINE_SEPARATOR,
          JRTextExporter.systemLineSeparator);
      textExporter.exportReport();

      logger.debug("Done Exporting  report.");
      for (int i = 0; i < opts.textModeTrailingLines; i++) {
        writer.write(JRTextExporter.systemLineSeparator.getBytes());
      }
      String reportString = writer.toString();
      return reportString;

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Process text report.
   *
   * @param file           the file
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void processTextReport(File file, String reportBaseName, Map parameterMap,
      PrintPageOptions opts, String[] subReportsUsed)
      throws JRException, IOException, SQLException {
    processTextReport(file, reportBaseName, parameterMap, opts, subReportsUsed,
        AppInit.getRootRealPath());
  }

  /**
   * Process text report.
   *
   * @param file           the file
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @param rootRealPath   absolute path of application root
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void processTextReport(File file, String reportBaseName, Map parameterMap,
      PrintPageOptions opts, String[] subReportsUsed, String rootRealPath)
      throws JRException, IOException, SQLException {
    String jasperPath = compileReport(reportBaseName, opts);
    if (subReportsUsed != null) {
      for (int i = 0; i < subReportsUsed.length; i++) {
        // sub-reports are not expected to use page options
        compileReport(subReportsUsed[i], null);
      }
    }

    parameterMap.put("SUBREPORT_DIR", rootRealPath + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);

    JRSwapFile swap = new JRSwapFile("/tmp", 2048, 1024);
    JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(50, swap, true);
    parameterMap.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);
    FileOutputStream os = null;

    try {
      logger.debug("Filling report");
      JasperPrint print = JasperFillManager.fillReport(jasperPath, parameterMap, con);

      logger.debug("Exporting text report");
      os = FileUtils.openOutputStream(file);

      JRTextExporter textExporter = new JRTextExporter();

      textExporter.setParameter(JRTextExporterParameter.JASPER_PRINT, print);
      textExporter.setParameter(JRTextExporterParameter.OUTPUT_STREAM, os);
      textExporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, opts.pageHeight);
      textExporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, opts.charWidth);
      textExporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Integer(12));
      textExporter.setParameter(JRTextExporterParameter.LINE_SEPARATOR,
          JRTextExporter.systemLineSeparator);

      textExporter.exportReport();

      logger.debug("Done Exporting  report.");
      os.flush();
      os.close();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (null != os) {
        os.close();
      }
      virtualizer.cleanup();
    }
  }

  /**
   * Process pdf stream.
   *
   * @param file           the file
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void processPdfStream(File file, String reportBaseName, Map parameterMap,
      PrintPageOptions opts, String[] subReportsUsed)
      throws JRException, IOException, SQLException {
    processPdfStream(file, reportBaseName, parameterMap, opts, subReportsUsed,
        AppInit.getRootRealPath());
  }

  /**
   * Process pdf stream.
   *
   * @param file           the file
   * @param reportBaseName the report base name
   * @param parameterMap   the parameter map
   * @param opts           the opts
   * @param subReportsUsed the sub reports used
   * @param rootRealPath   absolute path of application root
   * @throws JRException  the JR exception
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public static void processPdfStream(File file, String reportBaseName, Map parameterMap,
      PrintPageOptions opts, String[] subReportsUsed, String rootRealPath)
      throws JRException, IOException, SQLException {

    String jasperPath = compileReport(reportBaseName, opts);
    if (subReportsUsed != null) {
      for (int i = 0; i < subReportsUsed.length; i++) {
        // sub-reports are not expected to use page options
        compileReport(subReportsUsed[i], null);
      }
    }

    parameterMap.put("SUBREPORT_DIR", rootRealPath + JASPER_DIR);
    parameterMap.put("center_id", RequestContext.getCenterId());
    addDecimalParams(parameterMap);

    JRSwapFile swap = new JRSwapFile("/tmp", 2048, 1024);
    JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(50, swap, true);
    parameterMap.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

    Connection con = DataBaseUtil.getConnection(60);
    con.setAutoCommit(false);

    try {
      logger.debug("Filling report");
      JasperPrint print = JasperFillManager.fillReport(jasperPath, parameterMap, con);

      logger.debug("Exporting PDF report");
      FileOutputStream os = FileUtils.openOutputStream(file);
      JasperExportManager.exportReportToPdfStream(print, os);

      logger.debug("Done printing report.");
      os.flush();
      os.close();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (virtualizer != null) {
        virtualizer.cleanup();
      }
    }
  }
}