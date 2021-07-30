package com.bob.hms.report;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class IpOpStatsReportAction.
 */
public class IpOpStatsReportAction extends DispatchAction {
  
  /**
   * Gets the screen.
   *
   * @param mapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the screen
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) {
    return mapping.findForward("getScreen");
  }

  /**
   * Gets the report.
   *
   * @param mapping the m
   * @param form the f
   * @param req the req
   * @param res the res
   * @return the report
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException the transformer exception
   */
  public ActionForward getReport(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, TemplateException, DocumentException,
      SQLException, ParseException, XPathExpressionException, TransformerException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      String fromDateStr = req.getParameter("fromDate");
      String toDateStr = req.getParameter("toDate");
      String report = req.getParameter("report");
      String centerFilter = req.getParameter("center") == null
          || req.getParameter("center").equals("")
              ? req.getAttribute("center") == null ? "0" : (String) req.getAttribute("center")
              : req.getParameter("center");
      int centerId = (Integer) req.getSession(false).getAttribute("centerId");
      int viewCenter = Integer.parseInt(centerFilter);
      int center = centerId != 0 ? centerId : viewCenter;
      java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
      java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);
      String format = getParamDefault(req, "printerType", "pdf");
      Map params = new HashMap();
      params.put("fromDate", fromDate);
      params.put("toDate", toDate);
      params.put("report", report);
      params.put("format", format);
      params.put("center", center);
      params.put("hospital", GenericPreferencesDAO.getPrefsBean().get("hospital_name"));
      OutputStream out;

      BasicDynaBean printprefs = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
      String textReport = null;

      if (format.equals("pdf")) {
        out = res.getOutputStream();
        res.setContentType("application/pdf");
        if (report.equals("ops") || report.equals("opsd")) {
          textReport = new IpOpReportFtlHelper().getOpStatsFtlReport(con, params, out);
        } else {
          textReport = new IpOpReportFtlHelper().getIpStatsDeptWiseFtlReport(con, params, out);
        }

        out.close();
        return null;
      }

      if (report.equals("ops")) {
        textReport = new IpOpReportFtlHelper().getOpStatsFtlReport(con, params, null);
      } else {
        textReport = new IpOpReportFtlHelper().getIpStatsDeptWiseFtlReport(con, params, null);
      }
      if (format.equals("text")) {
        req.setAttribute("textReport", textReport);
        req.setAttribute("textColumns", 90);
        req.setAttribute("printerType", "DMP");
        return mapping.findForward("textPrintApplet");
      }
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
    return null;
  }

  /**
   * Gets the param default.
   *
   * @param req the req
   * @param paramName the param name
   * @param defaultValue the default value
   * @return the param default
   */
  private String getParamDefault(HttpServletRequest req, String paramName, String defaultValue) {
    String value = req.getParameter(paramName);
    if ((value == null) || value.equals("")) {
      value = defaultValue;
    }
    return value;
  }
}
