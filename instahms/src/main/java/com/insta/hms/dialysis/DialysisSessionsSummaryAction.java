package com.insta.hms.dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;


/**
 * The Class DialysisSessionsSummaryAction.
 */
public class DialysisSessionsSummaryAction extends DispatchAction {

  /**
   * Search.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward search(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse response) throws SQLException, ParseException {

    return mapping.findForward("list");
  }

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form the form
   * @param req the req
   * @param res the res
   * @return the action forward
   * @throws ParseException the parse exception
   * @throws SQLException the SQL exception
   */
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws ParseException, SQLException {

    HashMap filter = new HashMap();
    filter.put("start_time", DataBaseUtil.parseDate(req.getParameter("start_date")));
    filter.put("end_time", DataBaseUtil.parseDate(req.getParameter("end_date")));
    filter.put("status", ConversionUtils.getParamAsList(req.getParameterMap(), "status"));
    filter.put("mr_no", req.getParameter("mr_no"));
    filter.put("operator", req.getParameter("operator"));
    filter.put("value",
        (req.getParameter("value") != null && !req.getParameter("value").equals(""))
            ? Integer.parseInt(req.getParameter("value"))
            : null);

    PagedList list = DialysisSessionsDao.getSessionSummary(filter,
        ConversionUtils.getListingParameter(req.getParameterMap()));

    req.setAttribute("pagedList", list);
    req.setAttribute("module", req.getParameter("module"));
    return mapping.findForward("list");
  }

  /**
   * Gets the date range selection screen.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the date range selection screen
   */
  public ActionForward getDateRangeSelectionScreen(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) {
    request.setAttribute("mrNo", request.getParameter("mrNo"));

    return mapping.findForward("dateRangeScreen");
  }

  /**
   * Gets the flow sheet report.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the flow sheet report
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TemplateException the template exception
   * @throws DocumentException the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws ParseException the parse exception
   */
  public ActionForward getFlowSheetReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException,
      TemplateException, DocumentException, XPathExpressionException, ParseException {

    Map filterMap = new HashMap();
    String mrNo = request.getParameter("mr_no");
    String fromDate = request.getParameter("fromDate");
    String toDate = request.getParameter("toDate");

    filterMap.put("mr_no", mrNo);
    filterMap.put("fromDate", fromDate);
    filterMap.put("toDate", toDate);

    BasicDynaBean prevSessionBean = DialysisSessionsDao
        .getPrevSessionFinalRealWt(request.getParameter("mr_no"), request.getParameter("fromDate"));
    BigDecimal finalWt = prevSessionBean != null ? (BigDecimal) prevSessionBean.get("fin_real_wt")
        : new BigDecimal(-1);

    /*
     * StringBuilder medicatonDetails =
     * DialysisSessionsDao.builder(DialysisMedicationsDAO.getPrescriptinDetails(mrNo, fromDate,
     * toDate), "item_name");
     */

    /*
     * StringBuilder drugsAdministered = DialysisSessionsDao.builder(DialysisSessionsDao.
     * getDrugListForFlowsheet(mrNo, fromDate, toDate), "medicine_name");
     */

    Map map = new HashMap();
    map.put("patient",
        PatientDetailsDAO.getPatientGeneralDetailsMap(request.getParameter("mr_no")));
    map.put("hospitalName", request.getSession(false).getAttribute("sesHospitalId"));
    map.put("flowSheetDetails", DialysisSessionsDao.getDialysisFlowSheet(filterMap));
    map.put("finalWt", finalWt);
    map.put("medications", DialysisMedicationsDAO.getPrescriptinDetails(mrNo, fromDate, toDate));
    map.put("drugsAdministered",
        DialysisSessionsDao.getDrugListForFlowsheet(mrNo, fromDate, toDate));

    Template template = AppInit.getFmConfig().getTemplate("DialysisFlowSheet.ftl");
    HtmlConverter htmlConverter = new HtmlConverter();

    StringWriter writer = new StringWriter();
    template.process(map, writer);
    String textContent = writer.toString();
    OutputStream os = response.getOutputStream();
    response.setContentType("application/pdf");

    try {
      htmlConverter.writePdf(os, textContent);
    } finally {
      os.close();
    }
    return null;
  }
}
