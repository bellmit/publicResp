package com.insta.hms.common.ftl;

import com.insta.hms.common.BaseAction;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FtlReportAction extends BaseAction {

  private static CenterMasterDAO centerDao = new CenterMasterDAO();

  /**
   * Gets the report generation ui.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the report generation ui
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) {
    HttpSession session = req.getSession();
    Integer centerId = (Integer) session.getAttribute("centerId");
    req.setAttribute("reportingMeta", centerDao.getReportingMeta(centerId));
    return mapping.findForward("getScreen");
  }

  /**
   * Gets the report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the report
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getReport(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res)
      throws IOException, TemplateException, DocumentException, SQLException, ParseException {

    String reportName = mapping.getProperty("report-name");

    FtlReportGenerator fg = new FtlReportGenerator(reportName);
    fg.setParamsFromParamMap(req.getParameterMap());
    HttpSession session = req.getSession();
    Integer centerId = (Integer) session.getAttribute("centerId");
    fg.setParam("loggedInCenterId", centerId);

    String format = getParamDefault(req, "format", "pdf");
    if (format.equals("pdf")) {
      res.setContentType("application/pdf");
      fg.runPdfReport(res.getOutputStream());
    } else if (format.equals("text")) {
      // todo: need to invoke the text mode print applet
    } else {
      // screen for viewing
      fg.runScreenReport(res.getWriter());
    }

    return null;
  }

}
