package com.bob.hms.report;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.CommonReportAction;
import com.insta.hms.master.DietaryMaster.DietaryFtlHealper;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * The Class DietaryReportAction.
 */
public class DietaryReportAction extends DispatchAction {

  /**
   * Gets the report screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the report screen
   * @throws SQLException the SQL exception
   */
  public ActionForward getReportScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {
    String reportType = mapping.getProperty("report-name");
    int centerId = (Integer) request.getSession().getAttribute("centerId");
    boolean multiCentered = GenericPreferencesDAO.getGenericPreferences()
        .getMax_centers_inc_default() > 1;

    request.setAttribute("wards", BedMasterDAO.getAllWardNames(centerId, multiCentered));
    String forwardName = null;
    if (reportType.equals("ToBeDeliveredMealsReport")) {
      forwardName = "getDailyReport";
    } else if (reportType.equals("TrendReport")) {
      forwardName = "getTrendScreen";
    } else if (reportType.equals("RevenueReport")) {
      forwardName = "getRevenueReport";
    }

    return mapping.findForward(forwardName);
  }

  /**
   * Gets the trend report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the trend report
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws ParseException the parse exception
   */
  public ActionForward getTrendReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, TemplateException, DocumentException, ParseException {

    Map params = new HashMap();
    String format = request.getParameter("format");
    if ((format == null) || ("".equals(format))) {
      format = "pdf";
    }
    Object out;
    if (format.equals("pdf")) {
      out = response.getOutputStream();
      response.setContentType("application/pdf");
    } else {
      out = response.getWriter();
    }
    params.put("format", format);
    params.put("fromDate", DataBaseUtil.parseDate(request.getParameter("fromDate")));
    params.put("toDate", DataBaseUtil.parseDate(request.getParameter("toDate")));
    
    Connection con = DataBaseUtil.getReadOnlyConnection();
    new DietaryFtlHealper(AppInit.getFmConfig()).getDietaryTrendReport(con, params, out);
    return null;
  }

  /**
   * To be delivered meals report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JRException the JR exception
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public ActionForward toBeDeliveredMealsReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, JRException, ParseException, SQLException {
    CommonReportAction reportaction = new CommonReportAction();
    reportaction.getReport(mapping, form, request, response);
    return null;
  }
}
