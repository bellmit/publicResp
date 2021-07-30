package com.insta.hms.common;

import com.lowagie.text.DocumentException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Map;

/**
 * The Class FtlHelper.
 */
public class FtlHelper {

  /**
   * Gets the ftl report.
   *
   * @param template the template
   * @param params   the params
   * @param format   the format
   * @param out      the out
   * @return the ftl report
   * @throws IOException       Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws SQLException      the SQL exception
   */
  public byte[] getFtlReport(Template template, Map params, String format, Object out)
      throws IOException, TemplateException, DocumentException, SQLException {

    byte[] bytes = null;

    if (format.equals("pdf")) {
      OutputStream os = (OutputStream) out;

      StringWriter writer = new StringWriter();
      template.process(params, writer);
      String htmlContent = writer.toString();

      HtmlConverter hc = new HtmlConverter();
      hc.writePdf(os, htmlContent);
      os.close();

    } else if (format.equals("pdfbytes")) {
      StringWriter writer = new StringWriter();
      template.process(params, writer);
      String htmlContent = writer.toString();

      HtmlConverter hc = new HtmlConverter();
      bytes = hc.getPdfBytes(htmlContent);

    } else if (format.equals("screen")) {
      template.process(params, (Writer) out);
    }

    return bytes;
  }

}
