package com.insta.hms.diagnosticmodule.diagprogressivereport;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class DiagProgressiveReportAction.
 */
public class DiagProgressiveReportAction extends DispatchAction {

  /**
   * Gets the screen.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the screen
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   * @throws Exception
   *           the exception
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException, Exception {

    return mapping.findForward("getScreen");
  }

  /**
   * Show report.
   *
   * @param actionMapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws JRException
   *           the JR exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   */
  public ActionForward showReport(ActionMapping actionMapping, ActionForm form,
      HttpServletRequest req, HttpServletResponse res) throws SQLException, JRException,
      ParseException, IOException, TemplateException, DocumentException {

    String fromDateStr = req.getParameter("fromDate");
    String toDateStr = req.getParameter("toDate");
    java.sql.Date fromDate = DataBaseUtil.parseDate(fromDateStr);
    java.sql.Date toDate = DataBaseUtil.parseDate(toDateStr);
    Map params = new HashMap();
    params.put("fromDate", fromDate);
    params.put("toDate", toDate);
    Object out;
    out = res.getOutputStream();
    res.setContentType("application/pdf");
    DiagProgressiveReportFtlHelper reportFtlHelper = new DiagProgressiveReportFtlHelper(
        AppInit.getFmConfig());
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      reportFtlHelper.getTrendReport(con, params, out);

    } finally {
      con.close();
    }

    return null;
  }

}
