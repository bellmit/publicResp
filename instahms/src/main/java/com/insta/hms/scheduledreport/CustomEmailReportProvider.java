package com.insta.hms.scheduledreport;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.customreports.CustomReportsDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The Class CustomEmailReportProvider.
 *
 * @author krishna.t
 */
public class CustomEmailReportProvider implements EmailReportProviderInterface {

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#listAvailableReports()
   */
  public List<Map> listAvailableReports() throws SQLException {
    ArrayList reports = new ArrayList();
    List<BasicDynaBean> customReportList = new CustomReportsDAO().listNonSrjsReports();
    return ConversionUtils.listBeanToListMap(customReportList);
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#getReportName(java.lang.String)
   */
  public String getReportName(String reportIdStr) throws SQLException {
    int reportId = Integer.parseInt(reportIdStr);
    BasicDynaBean reportBean = new CustomReportsDAO().findByKey("report_id", reportId);
    return (String) reportBean.get("report_name");
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.scheduledreport.EmailReportProviderInterface#getReportBytes
   * (java.lang.String, java.lang.String, java.util.Map)
   */
  public byte[] getReportBytes(String reportIdStr, String format, Map paramMap)
      throws SQLException, IOException, JRException, TemplateException, DocumentException {

    int reportId = Integer.parseInt(reportIdStr);
    String[] dbSchema = RequestContext.getConnectionDetails();
    String schema = dbSchema[2];
    String jasperDir = AppInit.getRootRealPath() + "/reports/" + schema;

    CustomReportsDAO reportsdao = new CustomReportsDAO();
    DynaBean report = reportsdao.findByKey("report_id", reportId);

    String reportname = (String) report.get("report_name");
    String metadata = (String) report.get("report_metadata");
    String reportType = (String) report.get("report_type");

    if (reportType.equals("jrxml")) {
      long timestamp = ((java.sql.Timestamp) report.get("timestamp")).getTime();
      String jasperPath = compileReport(jasperDir, reportname, metadata, timestamp);

      List<DynaBean> subReports = reportsdao.getSubReports(reportId);

      for (DynaBean bean : subReports) {
        String subreportMetadata = bean.get("report_metadata").toString();
        String subreportName = bean.get("report_name").toString();
        compileReport(jasperDir, subreportName, subreportMetadata, timestamp);
      }

      HashMap reportParams = new HashMap();
      reportParams.put("SUBREPORT_DIR", jasperDir);

      InputStream is = JRLoader.getFileInputStream(jasperPath);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try (Connection con = DataBaseUtil.getConnection(60);) {
        JasperRunManager.runReportToPdfStream(is, baos, paramMap, con);
      }
      byte[] bytes = baos.toByteArray();
      baos.close();
      return bytes;

    } else if (reportType.equals("ftl")) {
      FtlReportGenerator fg = new FtlReportGenerator(reportname, new StringReader(metadata));
      fg.setReportParams(paramMap);
      return fg.getPdfBytes();

    } else {
      return null;
    }
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
