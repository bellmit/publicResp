package com.insta.hms.common;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.common.DataBaseUtil;

import net.sf.jasperreports.engine.JRException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * A generic report action class. This class saves you from writing an action class/method
 * for each report that you need to invoke. It is also different from DirectReportAction because
 * it allows proper user rights to be checked for each report.
 *
 * A report is considered to have the following requests:
 *  - A page for selecting the dates and maybe some other parameters
 *  - An action for getting the Jasper report (optional, but usually there)
 *  - An action for getting the CSV report (optionally)
 *
 * To create a new report, in struts-config, you need the following:
 *
 * <action path="/pharmacy/SalesReport" type="com.insta.hms.common.CommonReportAction"
 *   parameter="method">
 *    <set-property report-name="SalesReport" csv-view-name="pharmacy_sales_report_view"/>
 *    <forward name="getScreen" path="/pages/pharmacy/SalesReport.jsp"/>
 * </action>
 *
 * Now, the following will be automatically available for you:
 *   /pharmacy/SalesReport.do?method=getScreen => gets /pharmacy/SalesReport.jsp
 *   /pharmacy/SalesReport.do?method=getReport => gets the jasper PDF for SalesReport.jrxml
 *   /pharmacy/SalesReport.do?method=getCsv    => gets the CSV output of pharmacy_sales_report_view
 *
 * For the report itself, create one view as the main query for the report. Use this view directly
 * for the CSV generation, and also use the same view in the jrxml for getting the rows.
 *
 * Additional request parameters can be passed, which will be used for passing on to the jrxml and
 * the query for the view. fromDate and toDate are converted to dates, all other parameters are
 * passed on as strings, except those that start with _. This allows us to have parameters that
 * will fail if we pass to a CSV report, which is essentially a view.
 *
 * TODO: special handling such as today, thisMonth, thisWeek, thisYear. This will enable
 * bookmarks to be made for reports, so that the user does not have to go through a
 * date selection.
 */

/**
 * The Class CommonReportAction.
 */
public class CommonReportAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(CommonReportAction.class);

  /**
   * Gets the screen.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the screen
   */
  public ActionForward getScreen(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) {
    return mapping.findForward("getScreen");
  }

  /**
   * Gets the report.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the report
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws JRException    the JR exception
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public ActionForward getReport(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, JRException, ParseException, SQLException {
    try {
      HashMap reportParams = new HashMap();
      String reportName = null;

      reportName = mapping.getProperty("report-name");
      // handling mulitiple report names
      if (reportName == null) {
        reportName = req.getParameter("report_name");
      }

      if (reportName != null) {

        String printerMode = req.getParameter("printerType");
        if (printerMode != null && printerMode.equals("text")) {
          StringBuffer url = req.getRequestURL();
          String queryString = req.getQueryString();
          url = url.append("?")
              .append(queryString.replaceAll("method=getReport", "method=getText"));
          req.setAttribute("url", url.toString());
          req.setAttribute("printerType", "DMP");
          return mapping.findForward("textModePrintApplet");
        } else {
          setReportParams(req, reportParams);
          ReportPrinter.printPdfStream(req, res, reportName, reportParams);
          return null;
        }

      } else {
        return mapping.findForward("error");
      }
    } catch (SQLException exception) {
      if (exception.getSQLState().equals("57014")) { // i.e. SQL query interrupted state
        res.setContentType("text/html");
        res.setHeader("Content-disposition", "inline; filename=timeOut.html");
        return mapping.findForward("sqlTimeout");
      } else {
        throw exception;
      }
    } catch (JRException jrexception) {
      if (jrexception.getCause() != null && (jrexception.getCause().toString()).equalsIgnoreCase(
          "org.postgresql.util.PSQLException: ERROR: canceling statement due to statement "
              + "timeout")) {
        // SQL query interrupted state
        res.setContentType("text/html");
        res.setHeader("Content-disposition", "inline; filename=timeOut.html");
        return mapping.findForward("sqlTimeout");
      } else {
        throw jrexception;
      }
    }
  }

  /**
   * Gets the csv.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the csv
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public ActionForward getCsv(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, ParseException {

    String viewName = mapping.getProperty("csv-view-name");
    if (viewName == null) {
      return null;
    }

    String filename = mapping.getPath();
    filename = filename.substring(filename.lastIndexOf("/") + 1);

    res.setHeader("Content-type", "application/csv");
    res.setHeader("Content-disposition", "attachment; filename=" + filename + ".csv");

    String dateFieldName = req.getParameter("_dateFieldName");
    if (dateFieldName == null) {
      dateFieldName = "date";
    }

    // construct the query
    StringBuilder where = new StringBuilder();
    for (Enumeration enu = req.getParameterNames(); enu.hasMoreElements();) {
      String name = (String) enu.nextElement();
      String value = req.getParameter(name);
      if (value.equals("")) {
        value = null;
      }
      if (name.equals("fromDate")) {
        DataBaseUtil.addWhereFieldOpValue(where, "date(" + dateFieldName + ")", ">=", value);
      } else if (name.equals("toDate")) {
        DataBaseUtil.addWhereFieldOpValue(where, "date(" + dateFieldName + ")", "<=", value);
      } else if (name.equals("fromDateTime")) {
        DataBaseUtil.addWhereFieldOpValue(where, "date_time", ">=", value);
      } else if (name.equals("toDateTime")) {
        DataBaseUtil.addWhereFieldOpValue(where, "date_time", "<=", value);
      } else if (!name.equals("method") && !name.startsWith("_") && !name.equals("printerType")) {
        DataBaseUtil.addWhereFieldOpValue(where, DataBaseUtil.quoteIdent(name), "=", value);
      }
    }

    // prepare the statement
    Connection con = null;
    PreparedStatement ps = null;
    String query = "SELECT * from " + viewName + where.toString();

    try {
      con = DataBaseUtil.getConnection(60);
      ps = con.prepareStatement(query);
      int index = 1;

      for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
        String name = (String) e.nextElement();
        String value = req.getParameter(name);

        if (value.equals("")) {
          continue;
        }

        if (name.equals("fromDate")) {
          ps.setDate(index++, DataBaseUtil.parseDate(value));
        } else if (name.equals("toDate")) {
          ps.setDate(index++, DataBaseUtil.parseDate(value));
        } else if (name.equals("fromDateTime")) {
          ps.setTimestamp(index++, DataBaseUtil.parseTimestamp(value));
        } else if (name.equals("toDateTime")) {
          ps.setTimestamp(index++, DataBaseUtil.parseTimestamp(value));
        } else if (!name.equals("method") && !name.startsWith("_") && !name.equals("printerType")) {
          ps.setString(index++, value);
        }
      }
      ResultSet rs = ps.executeQuery();

      CSVWriter writer = new CSVWriter(res.getWriter(), CSVWriter.DEFAULT_SEPARATOR);
      writer.writeAll(rs, true);
      writer.flush();
    } finally {
      ps.close();
      con.close();
    }
    return null;
  }

  /**
   * Gets the text.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the text
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws JRException    the JR exception
   * @throws ParseException the parse exception
   * @throws SQLException   the SQL exception
   */
  public ActionForward getText(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, JRException, ParseException, SQLException {

    HashMap reportParams = new HashMap();
    String reportName = null;

    reportName = mapping.getProperty("report-name");
    if (reportName == null) {
      reportName = req.getParameter("report_name");
    }

    setReportParams(req, reportParams);

    if (reportName != null) {
      PrintPageOptions opts = new PrintPageOptions(10, 10, 842, "N");
      opts.pageWidth = 842;
      opts.charWidth = 6;
      ReportPrinter.printTextReport(req, res, reportName, reportParams, opts, null);
    }
    return null;
  }

  /**
   * Sets the report params.
   *
   * @param req          the req
   * @param reportParams the report params
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws JRException    the JR exception
   * @throws ParseException the parse exception
   */
  private void setReportParams(HttpServletRequest req, HashMap reportParams)
      throws IOException, JRException, java.text.ParseException {

    Enumeration paramNames = req.getParameterNames();
    while (paramNames.hasMoreElements()) {
      String name = (String) paramNames.nextElement();
      String[] stringValues = req.getParameterValues(name);
      int len = stringValues.length;

      // special case: Arrays with one empty value need to be skipped
      if ((name.endsWith("Array") || name.contains("Array@"))
          && (len == 1 && stringValues[0].equals(""))) {
        continue;
      }

      // find/guess the datatype
      String dataType = "string";
      if (name.endsWith("@int")) {
        dataType = "int";
      } else if (name.equals("toDate") || name.equals("fromDate") || name.endsWith("@date")) {
        dataType = "date";
      } else if (name.equals("toDateTime") || name.equals("fromDateTime")
          || name.endsWith("@datetime")) {
        dataType = "datetime";
      }

      name = name.split("@")[0];

      // convert data type if required
      Object[] values;
      if (dataType.equals("int")) {
        values = new Integer[len];
        for (int i = 0; i < len; i++) {
          values[i] = new Integer(Integer.parseInt(stringValues[i]));
        }

      } else if (dataType.equals("date")) {
        values = new java.sql.Date[len];
        for (int i = 0; i < len; i++) {
          values[i] = DataBaseUtil.parseDate(stringValues[i]);
        }

      } else if (dataType.equals("datetime")) {
        values = new java.sql.Timestamp[len];
        for (int i = 0; i < len; i++) {
          values[i] = DataBaseUtil.parseTimestamp(stringValues[i]);
        }

      } else {
        values = stringValues;
      }

      // put single value or array
      if (name.endsWith("Array")) {
        reportParams.put(name, values);
      } else {
        reportParams.put(name, values[0]);
      }
    }
  }

}
