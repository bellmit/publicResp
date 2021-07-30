package com.insta.hms.accountinglog;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.accounting.AccountingExporter;
import com.insta.hms.billing.accounting.AccountingHelper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.scheduledexportprefs.ScheduledExportPrefsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class XmlImportExportLogAction.
 *
 * @author krishna
 */
public class XmlImportExportLogAction extends DispatchAction {

  /** The log. */
  static Logger log = LoggerFactory
      .getLogger(XmlImportExportLogAction.class);

  /** The dao. */
  XmlImportExportLogDAO dao = new XmlImportExportLogDAO();

  /** The voucher details dao. */
  GenericDAO voucherDetailsDao = new GenericDAO("accounting_voucher_details");

  /** The acc group DAO. */
  AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();

  /** The sched prefs dao. */
  ScheduledExportPrefsDAO schedPrefsDao = new ScheduledExportPrefsDAO();

  /** The center dao. */
  CenterMasterDAO centerDao = new CenterMasterDAO();

  /**
   * List.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  @IgnoreConfidentialFilters
  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException {
    Map listing = ConversionUtils.getListingParameter(request.getParameterMap());

    request.setAttribute("pagedList", dao.searchLogs(request.getParameterMap(), listing));
    return mapping.findForward("list");
  }

  /**
   * Show.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward show(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ParseException, IOException,
      ServletException {
    Integer exportNo = Integer.parseInt(request.getParameter("export_no"));
    BasicDynaBean bean = dao.findByKey("export_no", exportNo);

    BasicDynaBean prefs = schedPrefsDao.getRecord();
    if (prefs != null) {
      String exportDir = (String) prefs.get("directory");
      String folder = null;
      if (bean.get("status").equals("Pending")) {
        folder = File.separator + "input";
      } else if (bean.get("status").equals("Success")) {
        folder = File.separator + "done";
      } else if (bean.get("status").equals("Error")) {
        folder = File.separator + "error";
      }
      File dir = new File(exportDir + folder);
      String fileName = (String) bean.get("file_name");
      File[] files = dir.listFiles(new FileNameStartsWithFilter(fileName + "_"));
      if (files != null && files.length > 0 && files[0] != null) {
        FileInputStream fis = new FileInputStream(files[0]);
        request.setAttribute("file_data", new String(DataBaseUtil.readInputStream(fis)));
      } else {
        request.setAttribute("file_data", "Details file not found. Please update the status.");
      }
    }

    return mapping.findForward("show");
  }

  /**
   * Update.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, ServletException, IOException {
    BasicDynaBean prefs = schedPrefsDao.getRecord();
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    FlashScope flash = FlashScope.getScope(request);

    if (prefs != null) {
      Connection con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      Boolean commit = false;
      try {
        commit = dao.updateStatus(con);
      } finally {
        DataBaseUtil.commitClose(con, commit);
        if (!commit) {
          flash.error("Failed to update the status.");
        }
      }
    }
    return redirect;
  }

  /**
   * Regenerate.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws ServletException
   *           the servlet exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws Exception
   *           the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward regenerate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException,
      ServletException, IOException, Exception {
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    String[] exportNos = request.getParameterValues("_exportNo");

    if (exportNos != null) {
      Connection con = DataBaseUtil.getConnection();
      try {
        for (int i = 0; i < exportNos.length; i++) {
          int fetchAgnstExportNo = Integer.parseInt(exportNos[i]);
          BasicDynaBean bean = dao.findByKey("export_no", fetchAgnstExportNo);
          String existingFile = (String) bean.get("file_name");

          String accountingCompanyName = null;
          if ((Integer) bean.get("center_id") > 0) {
            accountingCompanyName = (String) centerDao.findByKey(con, "center_id",
                (Integer) bean.get("center_id")).get("accounting_company_name");
          } else {
            accountingCompanyName = (String) accGroupDAO.findByKey(con, "account_group_id",
                bean.get("account_group")).get("accounting_company_name");
          }

          // regenerating new file
          int saveAgnstExportNo = dao.getNextSequence();
          String newFileName = saveAgnstExportNo + ".xml";

          // insert one entry in import_export_log table for one export
          BasicDynaBean importlogBean = dao.getBean(con);
          importlogBean.set("file_name", newFileName);
          importlogBean.set("status", "Pending");
          importlogBean.set("account_group", bean.get("account_group"));
          importlogBean.set("export_no", saveAgnstExportNo);
          importlogBean.set("export_dir", bean.get("export_dir"));
          importlogBean.set("export_type", bean.get("export_type"));
          importlogBean.set("center_id", bean.get("center_id"));
          importlogBean.set("schedule_id", bean.get("schedule_id"));
          importlogBean.set("voucher_date", bean.get("voucher_date"));
          if (!dao.insert(con, importlogBean)) {
            log.error("Failed to insert the file details for export No: " + saveAgnstExportNo
                + ". Hence not regenerating the file, "
                + " because we can't update status of the file later");
          }

          FileOutputStream fos = new FileOutputStream(bean.get("export_dir") + File.separator
              + "input" + File.separator + newFileName);
          boolean atleastOneVoucherExported = false;

          try {
            AccountingExporter exporter = new AccountingExporter(
                (Integer) bean.get("account_group"), saveAgnstExportNo, fetchAgnstExportNo,
                "tallyxml", (java.sql.Date) bean.get("voucher_date"));
            AccountingHelper.addHeader(fos, "tallyxml", accountingCompanyName);
            String[] exportItems = { "" };
            atleastOneVoucherExported = exporter.exportDetails(exportItems, fos,
                (Integer) bean.get("center_id"));
            AccountingHelper.addFooter(fos, "", "", "", "", "tallyxml", "", exportItems, "", "",
                "", "");
            fos.flush();
          } catch (Exception exception) {
            /*
             * file is not exported hence delete the inserted file and voucher details from
             * database.
             */
            if (!dao.delete(con, "export_no", saveAgnstExportNo)) {
              log.error("failed to delete the file details for export No: " + saveAgnstExportNo);
            }
            if (!voucherDetailsDao.delete(con, "export_no", saveAgnstExportNo)) {
              log.error("failed to delete the voucher details for export No: " + saveAgnstExportNo);
            }
            throw exception;
          } finally {
            if (fos != null) {
              fos.close();
            }
          }

          // vouchers exported successfully for regeneration. hence delete the older file and it's
          // error file.
          String folder = null;
          if (bean.get("status").equals("Pending")) {
            folder = File.separator + "input";
          } else if (bean.get("status").equals("Success")) {
            folder = File.separator + "done";
          } else if (bean.get("status").equals("Error")) {
            folder = File.separator + "error";
          }

          // delete the entries from database for the file which is used for regeneration.
          if (!dao.delete(con, "export_no", fetchAgnstExportNo)) {
            log.error("failed to delete the existing(old) file details for export No: "
                + fetchAgnstExportNo);
          }
          if (!voucherDetailsDao.delete(con, "export_no", fetchAgnstExportNo)) {
            log.error("failed to delete the existing(old) voucher details for export No: "
                + fetchAgnstExportNo);
          }

          List<BasicDynaBean> exportDirs = schedPrefsDao.getList(con);
          for (BasicDynaBean dirBean : exportDirs) {
            File dir = new File(dirBean.get("directory") + folder);
            File[] files = dir.listFiles(new FileNameStartsWithFilter(existingFile));
            if (files != null) {
              for (File file : files) {
                file.delete(); // deleting the main file and error files.
              }
            }
          }
        }

        dao.updateStatus(con);
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    return redirect;
  }

  /**
   * Delete.
   *
   * @param mapping
   *          the mapping
   * @param form
   *          the form
   * @param request
   *          the request
   * @param response
   *          the response
   * @return the action forward
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws ServletException
   *           the servlet exception
   */
  @IgnoreConfidentialFilters
  public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws SQLException, IOException, ServletException {
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("listRedirect"));
    String[] exportNos = request.getParameterValues("_exportNo");
    BasicDynaBean prefs = schedPrefsDao.getRecord();
    String exportDir = (String) prefs.get("directory");

    if (exportNos != null) {
      Connection con = DataBaseUtil.getConnection();
      // rollback doesn't make sense here, because it is deleting the files in the directory as
      // well,
      // which we can't rollback once deleted.
      try {
        List<BasicDynaBean> exportDirs = schedPrefsDao.getList(con);
        for (int i = 0; i < exportNos.length; i++) {
          int exportNo = Integer.parseInt(exportNos[i]);
          BasicDynaBean bean = dao.findByKey("export_no", exportNo);

          boolean status = dao.deleteDetails(con, exportNo);
          if (status) {
            String folder = null;
            if (bean.get("status").equals("Pending")) {
              folder = File.separator + "input";
            } else if (bean.get("status").equals("Success")) {
              folder = File.separator + "done";
            } else if (bean.get("status").equals("Error")) {
              folder = File.separator + "error";
            }

            for (BasicDynaBean dirBean : exportDirs) {
              File dir = new File(dirBean.get("directory") + folder);
              File[] files = dir.listFiles(new FileNameStartsWithFilter((String) bean
                  .get("file_name")));
              if (files != null) {
                for (File file : files) {
                  file.delete(); // deleting the main file and error files.
                }
              }
            }
          }

        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    return redirect;
  }

}
