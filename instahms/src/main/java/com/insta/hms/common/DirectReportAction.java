package com.insta.hms.common;

import com.bob.hms.common.DataBaseUtil;

import net.sf.jasperreports.engine.JRException;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * A generic report printer action class. This class saves you from writing an action class/method
 * for each report that you need to invoke. Instead, it generically invokes the report based on
 * the "report" request parameter and other parameters passed in the request.
 *
 * The following known parameters are specially treated:
 *   fromDate: is converted to a java.sql.Date object, and added to the report parameters
 *   toDate: is converted to a java.sql.Date object, and added to the report parameters
 *   report: name of the jrxml to run (eg, PharmacySalesReport), not added to the report parameters
 * All other request parameters are directly passed on to the report transparently as strings.
 *
 * Example:
 *   /DirectReport.do?fromDate=13-02-2009&toDate=14-02-2009&report=PharmacySalesReport
 *
 * TODO: special handling such as today, thisMonth, thisWeek, thisYear. This will enable
 * bookmarks to be made for reports, so that the user does not have to go through a
 * date selection.
 */

/**
 * The Class DirectReportAction.
 */
public class DirectReportAction extends Action {

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping,
   * org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
   * javax.servlet.http.HttpServletResponse)
   */
  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res)
      throws IOException, JRException, java.text.ParseException, SQLException {

    HashMap reportParams = new HashMap();
    Enumeration reqParamNames = req.getParameterNames();
    String reportName = null;
    BigDecimal defalutTax = new BigDecimal(0).setScale(2);
    BigDecimal[] taxRate = new BigDecimal[3];
    taxRate[0] = defalutTax;
    String vatORcstOne = null;
    String vatORcstTwo = null;
    boolean istaxRateSelected = false;

    int index = 1;

    while (reqParamNames.hasMoreElements()) {
      String name = (String) reqParamNames.nextElement();
      String value = req.getParameter(name);

      if (name.equals("fromDate")) {
        reportParams.put("fromDate", DataBaseUtil.parseDate(value));
      } else if (name.equals("toDate")) {
        reportParams.put("toDate", DataBaseUtil.parseDate(value));
      } else if (name.equals("report")) {
        reportName = value;
      } else if (name.equals("toDateTime")) {
        reportParams.put("toDateTime", DataBaseUtil.parseTimestamp(value));
      } else if (name.equals("fromDateTime")) {
        reportParams.put("fromDateTime", DataBaseUtil.parseTimestamp(value));
      } else if (name.equals("groupBy")) {
        reportParams.put("groupBy", value);
      } else if (name.equals("taxrate" + value)) {
        taxRate[index] = new BigDecimal(value);
        istaxRateSelected = true;
        if (index == 1) {
          reportParams.put("taxRateOne", new BigDecimal(value));
          vatORcstOne = "VAT";
        }
        if (index == 2) {
          reportParams.put("taxRateTwo", new BigDecimal(value));
          vatORcstTwo = "VAT";
        }
        index++;
      } else if (name.equals("cessrate" + value)) {
        taxRate[index] = new BigDecimal(value);
        istaxRateSelected = true;
        if (index == 1) {
          reportParams.put("taxRateOne", new BigDecimal(value));
          vatORcstOne = "CST";
        }
        if (index == 2) {
          reportParams.put("taxRateTwo", new BigDecimal(value));
          vatORcstTwo = "CST";
        }
        index++;
      } else {
        reportParams.put(name, req.getParameter(name));
      }
    }
    if (istaxRateSelected) {
      reportParams.put("taxRate", taxRate);
      reportParams.put("vatORcstOne", vatORcstOne);
      reportParams.put("vatORcstTwo", vatORcstTwo);
    }

    if (reportName != null) {
      ReportPrinter.printPdfStream(req, res, reportName, reportParams);
      return null;
    } else {
      return mapping.findForward("error");
    }
  }
}
