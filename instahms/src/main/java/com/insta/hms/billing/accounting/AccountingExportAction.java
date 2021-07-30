package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;

import freemarker.template.TemplateException;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class AccountingExportAction.
 */
/*
 * TODO: - pharmacy returns - inventory returns - Separate entity for pharmacy: - exclude pharma
 * bills from hospital bills - separate pharma bills in summary - option for pharma/hospital export
 * - Add prvs month/week, no need for year. - Automate creation of guid_prefix per hospital (use
 * cmdline utility uuidgen)
 */
public class AccountingExportAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(AccountingExportAction.class);

  /** The ac prefs DAO. */
  private static AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();

  /** The acc group DAO. */
  private static AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();

  /** The center DAO. */
  CenterMasterDAO centerDAO = new CenterMasterDAO();

  /** The exporter. */
  AccountingExporter exporter = null;

  /**
   * Gets the screen.
   *
   * @param actionMapping
   *          the m
   * @param actionForm
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the screen
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getScreen(ActionMapping actionMapping, ActionForm actionForm,
      HttpServletRequest req, HttpServletResponse res) throws Exception {
    req.setAttribute("acc_prefs", acPrefsDAO.getRecord());
    req.setAttribute("accGroups", accGroupDAO.getAssociatedAccountGroups());
    req.setAttribute("centers", centerDAO.getAllCentersExceptSuper());
    return actionMapping.findForward("TallyExportDates");
  }

  /*
   * Gets all the vouchers to be shown. Based on Summary/Details/XML/CSV, we return different kinds
   * of data or pages that need to be shown to the user.
   */

  /**
   * Gets the vouchers.
   *
   * @param mapping
   *          the m
   * @param form
   *          the f
   * @param req
   *          the req
   * @param res
   *          the res
   * @return the vouchers
   * @throws SQLException
   *           the SQL exception
   * @throws TemplateException
   *           the template exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ParseException
   *           the parse exception
   * @throws ClassNotFoundException
   *           the class not found exception
   * @throws AccountingException
   *           the accounting exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getVouchers(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, TemplateException, IOException, ParseException,
      ClassNotFoundException, AccountingException {

    Map debitSummary = null;
    Map creditSummary = null;
    java.sql.Date voucherDate = null;
    java.sql.Date voucherFromDate = null;
    java.sql.Date voucherToDate = null;

    String fromTimeStr = req.getParameter("fromTime");
    String toTimeStr = req.getParameter("toTime");
    if (fromTimeStr == null || fromTimeStr.equals("")) {
      fromTimeStr = "00:00";
    }
    if (toTimeStr == null || toTimeStr.equals("")) {
      toTimeStr = "23:59";
    }

    String voucherFromDateStr = req.getParameter("voucherFromDate");
    String voucherToDateStr = req.getParameter("voucherToDate");

    if (voucherFromDateStr != null && !voucherFromDateStr.equals("")) {
      voucherFromDate = DateUtil.parseDate(voucherFromDateStr);
    }
    if (voucherToDateStr != null && !voucherToDateStr.equals("")) {
      voucherToDate = DateUtil.parseDate(voucherToDateStr);
    }

    String useVoucherDate = req.getParameter("useVoucherDate");
    String voucherDateStr = req.getParameter("voucherDate");
    if ((useVoucherDate != null) && (voucherDateStr != null)) {
      voucherDate = DataBaseUtil.parseDate(voucherDateStr);
    } else {
      voucherDate = null;
    }

    String format = req.getParameter("format");
    if (format.equals("summary")) {
      debitSummary = new LinkedHashMap();
      creditSummary = new LinkedHashMap();
    } else {
      debitSummary = null;
      creditSummary = null;
    }

    String exportFor = req.getParameter("exportFor");
    String[] exportItems = req.getParameterValues("exportItems");
    int accountGroup = 1;
    int centerId = 0;
    if (exportFor.charAt(0) == 'C') {
      centerId = Integer.parseInt(exportFor.substring(1));
    } else if (exportFor.charAt(0) == 'A') {
      accountGroup = Integer.parseInt(exportFor.substring(1));
    }

    String fromDateStr = req.getParameter("fromDate");
    String toDateStr = req.getParameter("toDate");

    java.sql.Timestamp fromDate = DateUtil.parseTimestamp(fromDateStr + " " + fromTimeStr + ":00");
    java.sql.Timestamp toDate = DateUtil.parseTimestamp(toDateStr + " " + toTimeStr + ":59");

    exporter = new AccountingExporter(fromDate, toDate, accountGroup, voucherDate, debitSummary,
        creditSummary, voucherFromDate, voucherToDate, format, 0, 0);

    if (format.equals("summary")) {
      exporter.exportDetails(exportItems, null, centerId);
      req.setAttribute("debitSummary", debitSummary);
      req.setAttribute("creditSummary", creditSummary);

      return mapping.findForward("TallyExportSummary");

    } else if (format.equals("details")) {
      res.setContentType("text/html");

      OutputStream stream = res.getOutputStream();
      AccountingHelper.addHeader(stream, format, null);
      exporter.exportDetails(exportItems, stream, centerId);
      AccountingHelper.addFooter(stream, fromDateStr, toDateStr, useVoucherDate, voucherDateStr,
          format, exportFor, exportItems, voucherFromDateStr, voucherToDateStr, fromTimeStr,
          toTimeStr);

      stream.flush();
      return null;

    } else if (format.equals("tallyxml")) {
      File file = File.createTempFile("tally", "import");
      FileOutputStream fos = new FileOutputStream(file);
      String accountingCompanyName = null;
      if (exportFor.charAt(0) == 'C') {
        accountingCompanyName = (String) centerDAO.findByKey("center_id", centerId).get(
            "accounting_company_name");
      } else {
        accountingCompanyName = (String) accGroupDAO.findByKey("account_group_id", accountGroup)
            .get("accounting_company_name");
      }

      AccountingHelper.addHeader(fos, format, accountingCompanyName);
      exporter.exportDetails(exportItems, fos, centerId);
      AccountingHelper.addFooter(fos, fromDateStr, toDateStr, useVoucherDate, voucherDateStr,
          format, exportFor, exportItems, voucherFromDateStr, voucherToDateStr, fromTimeStr,
          toTimeStr);

      res.setContentType("text/xml");
      String fileName = accountingCompanyName;
      fileName = fileName == null || fileName.equals("") ? "import" : fileName;
      res.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xml");
      FileInputStream fis = new FileInputStream(file);
      OutputStream stream = res.getOutputStream();
      HtmlConverter.writeFiletoStream(fis, stream);
      stream.flush();
      file.delete();
      return null;

    } else if (format.equals("csv")) {
      return null;
    }

    return null;
  }

}
