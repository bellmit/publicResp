package com.insta.hms.scheduledreport;

import com.insta.hms.customreports.CustomReportsDAO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.beanutils.DynaBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CustomReportProviderImpl.
 *
 * @author krishna.t
 */
public class CustomReportProviderImpl implements ScheduledEmailableReportsInterface {

  private String workDir = null;
  private String event = null;
  private String host = null;
  private String database = null;
  private String schema = null;
  private String debugMode = "no";

  /**
   * Instantiates a new custom report provider impl.
   *
   * @param args
   *          the args
   */
  public CustomReportProviderImpl(String[] args) {
    workDir = args[0];
    event = args[1];
    host = args[2];
    database = args[3];
    schema = args[4];
    if (args.length > 5) {
      debugMode = args[5];
    }
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.ScheduledEmailableReportsInterface#getPdfBytes(java.sql.
   * Connection, java.lang.String, java.sql.Date, java.sql.Date, java.util.Map)
   */
  public byte[] getPdfBytes(Connection con, String reportName, Date from, Date to, Map paramMap)
      throws SQLException, IOException, JRException {

    paramMap.put("fromdate", from);
    paramMap.put("todate", to);

    paramMap.put("fromDate", from);
    paramMap.put("toDate", to);

    paramMap.put("reportDate", to);

    Timestamp fromDateTime = new Timestamp(from.getTime());
    Timestamp toDateTime = new Timestamp(to.getTime() + (24 * 60 * 60 - 1) * 1000); // 23:59

    paramMap.put("fromDateTime", fromDateTime);
    paramMap.put("toDateTime", toDateTime);

    String jasperDir = workDir + "/reports/" + schema;

    System.out.println("Generating Report using custom-jrxml provider for: " + reportName);

    CustomReportsDAO reportsdao = new CustomReportsDAO();
    DynaBean report = reportsdao.findByKey("report_name", reportName);

    String metadata = report.get("report_metadata").toString();
    long timestamp = ((java.sql.Timestamp) report.get("timestamp")).getTime();
    String reportname = report.get("report_name").toString();
    String jasperPath = compileReport(jasperDir, reportname, metadata, timestamp);

    List<DynaBean> subReports = reportsdao
        .getSubReports(Integer.parseInt(report.get("report_id").toString()));
    for (DynaBean bean : subReports) {
      String subreportMetadata = bean.get("report_metadata").toString();
      String subreportName = bean.get("report_name").toString();
      compileReport(jasperDir, subreportName, subreportMetadata, timestamp);
    }

    HashMap reportParams = new HashMap();
    reportParams.put("SUBREPORT_DIR", jasperDir);

    byte[] bytes = null;

    InputStream is = JRLoader.getFileInputStream(jasperPath);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JasperRunManager.runReportToPdfStream(is, baos, paramMap, con);
    bytes = baos.toByteArray();
    baos.close();
    return bytes;
  }

  /**
   * Compile report.
   *
   * @param baseDir
   *          the base dir
   * @param reportname
   *          the reportname
   * @param reportMetadata
   *          the report metadata
   * @param timestamp
   *          the timestamp
   * @return the string
   * @throws JRException
   *           the JR exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private synchronized String compileReport(String baseDir, String reportname,
      String reportMetadata, long timestamp) throws JRException, IOException {

    File basedir = new File(baseDir);
    basedir.mkdirs();

    String jasperPath = basedir + "/" + reportname + ".jasper";
    File jasperFile = new File(jasperPath);

    if ((jasperFile.lastModified() == 0) || (timestamp > jasperFile.lastModified())) {

      try (FileOutputStream out = new FileOutputStream(jasperFile)) {
        InputStream is = new ByteArrayInputStream(reportMetadata.getBytes());
        try {
          JasperCompileManager.compileReportToStream(is, out);
        } catch (JRException jex) {
          jasperFile.delete();// cleanup the file as it is invalid
          throw jex;// rethrow exception
        }
      }
    }

    return jasperPath;
  }
}
