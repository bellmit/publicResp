package com.insta.hms.scheduledreport;

import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Map;

public interface ScheduledEmailableReportsInterface {

  /**
   * returns the pdf bytes between requested dates(from and to dates).
   *
   * @param con the con
   * @param reportName the report name
   * @param from get pdf bytes from this date
   * @param to get pdf bytes till this date
   * @param params the params
   * @return the pdf bytes
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JRException the JR exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws Exception the exception
   */
  public byte[] getPdfBytes(Connection con, String reportName, Date from, Date to, Map params)
      throws SQLException, IOException, JRException, IllegalArgumentException, TemplateException,
      DocumentException, org.xml.sax.SAXException, Exception;

}
