package com.insta.hms.billing.accounting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.accountinglog.XmlImportExportLogDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.scheduledexportprefs.ScheduledExportPrefsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * The Class ScheduledAccountingExporter.
 *
 * @author krishna
 */
public class ScheduledAccountingExporter extends GenericJob {

  /** The Constant log. */
  private static final Logger log = LoggerFactory.getLogger(ScheduledAccountingExporter.class);

  /** The params. */
  private String params;

  /** The dao. */
  ScheduledExportPrefsDAO dao = new ScheduledExportPrefsDAO();

  /** The import log dao. */
  XmlImportExportLogDAO importLogDao = new XmlImportExportLogDAO();

  /** The voucher details dao. */
  GenericDAO voucherDetailsDao = new GenericDAO("accounting_voucher_details");

  /** The acc group DAO. */
  private static AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();

  /** The center dao. */
  CenterMasterDAO centerDao = new CenterMasterDAO();

  /**
   * Required for persisting job data.
   *
   * @param params
   *          the new params
   */
  public void setParams(String params) {
    this.params = params;
  }

  /**
   * Gets the params.
   *
   * @return the params
   */
  public String getParams() {
    return params;
  }

  /**
   * @see org.springframework.scheduling.quartz.
   *      QuartzJobBean#executeInternal(org.quartz.JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext context) throws JobExecutionException {

    Integer scheduleIdVal = -1;

    String scheduleId = getParams();
    if (null != scheduleId) {
      scheduleIdVal = Integer.valueOf(scheduleId);
    }

    Timestamp fromDate = null;
    Timestamp toDate = null;
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.MINUTE, -5);
    toDate = new java.sql.Timestamp(cal.getTimeInMillis());

    /*
     * if (period.equals("hourly")) { cal.add(Calendar.HOUR, -1); fromDate = new
     * java.sql.Timestamp(cal.getTimeInMillis());
     * 
     * } else if (period.equals("daily")) { cal.add(Calendar.DATE, -1); fromDate = new
     * java.sql.Timestamp(cal.getTimeInMillis());
     * 
     * } else if (period.equals("weekly")) { cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
     * cal.add(Calendar.WEEK_OF_YEAR, -1); fromDate = new java.sql.Timestamp(cal.getTimeInMillis());
     * 
     * }
     */

    // String[] dbSchema = new String[] {null, null, schema};
    // RequestContext.setConnectionDetails(dbSchema);

    setJobConnectionDetails();
    Connection con = DataBaseUtil.getConnection();

    try {

      BasicDynaBean bean = dao.findByKey(con, "schedule_id", scheduleIdVal);
      if (null == bean) {
        log.error("Unable to find schedule with ID " + scheduleIdVal + ", exiting");
        return;
      }
      fromDate = getExportFromDate(bean);
      int accountGroup = (Integer) bean.get("account_group");
      String[] exportItems = ((String) bean.get("export_items")).split(",");
      String exportDir = (String) bean.get("directory");
      String accountingCompanyName = null;
      if ((Integer) bean.get("center_id") > 0) {
        accountingCompanyName = (String) centerDao.findByKey(con, "center_id",
            (Integer) bean.get("center_id")).get("accounting_company_name");
      } else {
        accountingCompanyName = (String) accGroupDAO.findByKey(con, "account_group_id",
            accountGroup).get("accounting_company_name");
      }

      boolean dirExists = true;
      File inputDir = new File(exportDir + File.separator + "input");
      if (!inputDir.exists()) {
        dirExists = inputDir.mkdirs();
      }
      int exportNo = importLogDao.getNextSequence();
      String filename = exportNo + ".xml";

      // insert one entry in import_export_log table for one export
      BasicDynaBean importlogBean = importLogDao.getBean(con);
      importlogBean.set("file_name", filename);
      importlogBean.set("status", dirExists ? "Pending" : "Error");
      importlogBean.set("account_group", accountGroup);
      importlogBean.set("export_no", exportNo);
      importlogBean.set("message", dirExists ? ""
          : "File not created because of the directory obsence.");
      importlogBean.set("export_dir", bean.get("directory"));
      importlogBean.set("export_type", bean.get("export_type"));
      importlogBean.set("center_id", bean.get("center_id"));
      importlogBean.set("schedule_id", bean.get("schedule_id"));
      importlogBean.set("voucher_date", bean.get("voucher_date"));
      if (!importLogDao.insert(con, importlogBean)) {
        log.error("Failed to insert the file details for export No: " + exportNo
            + ". Hence not exporting the file, because we can't update status of the file later");
        return;
      }
      if (!dirExists) {
        log.error("File not created because of the directory '" + exportDir + File.separator
            + "input' obsence.");
        return;
      }
      FileOutputStream fos = new FileOutputStream(exportDir + File.separator + "input"
          + File.separator + filename);
      boolean atleastOneVoucherExported = false;

      try {
        AccountingExporter exporter = new AccountingExporter(fromDate, toDate, accountGroup,
            "tallyxml", exportNo, (java.sql.Date) bean.get("voucher_date"));
        AccountingHelper.addHeader(fos, "tallyxml", accountingCompanyName);
        atleastOneVoucherExported = exporter.exportDetails(exportItems, fos,
            (Integer) bean.get("center_id"));
        AccountingHelper
            .addFooter(fos, "", "", "", "", "tallyxml", "", exportItems, "", "", "", "");
        fos.flush();

        // updating file status to empty if the recently generated file contains no vouchers.
        BasicDynaBean logBean = importLogDao.getBean(con);
        logBean.set("status", atleastOneVoucherExported ? "Pending" : "Empty");
        if (!atleastOneVoucherExported) {
          logBean.set("message", "No Vouchers found.");
        }
        if (importLogDao.update(con, logBean.getMap(), "export_no", exportNo) != 1) {
          return;
        }

      } catch (Exception exception) {
        /*
         * file is not exported hence delete the inserted file and voucher details from database.
         */
        if (!importLogDao.delete(con, "export_no", exportNo)) {
          log.error("failed to delete the file details for export No: " + exportNo);
        }
        if (!voucherDetailsDao.delete(con, "export_no", exportNo)) {
          log.error("failed to delete the voucher details for export No: " + exportNo);
        }
        log.error("Exception raised while generating xml: " + filename, exception);
        return;
      } finally {
        if (fos != null) {
          fos.close();
        }
      }

      /*
       * update the status, error message if exist of the file
       */
      importLogDao.updateStatus(con);

    } catch (Exception exception) {
      log.error("failed to export the file", exception);
      throw new JobExecutionException(exception.getMessage());
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }

  }

  /**
   * Gets the export from date.
   *
   * @param bean
   *          the bean
   * @return the export from date
   */
  private Timestamp getExportFromDate(BasicDynaBean bean) {
    String period = null;
    Calendar cal = Calendar.getInstance();
    Timestamp fromDate = null;

    cal.add(Calendar.MINUTE, -5);
    if (null != bean) {
      period = (String) bean.get("period");
    }
    if (null != period) {
      if (period.equals("H")) {
        cal.add(Calendar.HOUR, -1);
        fromDate = new java.sql.Timestamp(cal.getTimeInMillis());

      } else if (period.equals("D")) {
        cal.add(Calendar.DATE, -1);
        fromDate = new java.sql.Timestamp(cal.getTimeInMillis());

      } else if (period.equals("W")) {
        cal.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        fromDate = new java.sql.Timestamp(cal.getTimeInMillis());
      }
    }

    return fromDate;
  }

}
