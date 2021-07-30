package com.insta.hms.growthcharts;

import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.vitalForm.VisitVitalsDAO;
import com.insta.hms.vitalForm.genericVitalFormAction;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc

/**
 * The Class GrowthChartsAction.
 *
 * @author nikunj.s
 */
public class GrowthChartsAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(genericVitalFormAction.class);

  /** The vm DAO. */
  VitalMasterDAO vmDAO = new VitalMasterDAO();

  /** The vv DAO. */
  VisitVitalsDAO vvDAO = new VisitVitalsDAO();

  /** The user dao. */
  UserDAO userDao = new UserDAO();

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ServletException the servlet exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ServletException, IOException,
      ParseException {
    return mapping.findForward("addshow");
  }

  /**
   * Gets the screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the screen
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException,
      ParseException {

    String mrNo = request.getParameter("mr_no");
    String patietnId = request.getParameter("patient_id");
    String chartType = request.getParameter("chart_type");

    request.setAttribute("chart_type", chartType);
    request.setAttribute("patient_id", patietnId);
    request.setAttribute("mr_no", mrNo);
    return mapping.findForward("showGrowthChartRedirect");
  }

  /**
   * Creates the charts.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward createCharts(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException,
      ParseException {

    String mrNo = request.getParameter("mr_no");
    String chartType = request.getParameter("chart_type");

    AbstractGrowthCharts agcf = AbstractGrowthCharts.getInstance(chartType);
    JFreeChart chart = agcf.generateChart(mrNo);
    /*
     * OutputStream out = response.getOutputStream(); response.setContentType("image/png");
     * ChartUtilities.writeChartAsPNG(out, chart, 1200, 900); out.flush(); out.close();
     */
    float width = PageSize.A4.getWidth();
    float height = PageSize.A4.getHeight();
    response.setContentType("application/pdf");
    OutputStream out = response.getOutputStream();
    writeChartAsPDF(out, chart, width, height, new DefaultFontMapper());
    out.close();
    return null;

  }

  /**
   * Write chart as PDF.
   *
   * @param out the out
   * @param chart the chart
   * @param width the width
   * @param height the height
   * @param mapper the mapper
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void writeChartAsPDF(OutputStream out, JFreeChart chart, float width, float height,
      FontMapper mapper) throws IOException {

    Document document = new Document();
    try {
      PdfWriter writer = PdfWriter.getInstance(document, out);
      document.addAuthor("JFreeChart");
      document.addSubject("Demonstration");
      document.open();
      PdfContentByte cb = writer.getDirectContent();
      PdfTemplate tp = cb.createTemplate(width, height);
      Graphics2D g2 = tp.createGraphics(width, height, mapper);
      Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
      chart.draw(g2, r2D);
      g2.dispose();
      cb.addTemplate(tp, 0, 0);
    } catch (DocumentException de) {
      System.err.println(de.getMessage());
    }
    document.close();
  }
}
