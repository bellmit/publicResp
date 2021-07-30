package com.insta.hms.batchjob;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.jobs.GenericJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SupplierReturnsWithDebitNoteCsvExporterJob extends GenericJob {

  private static final Logger logger = LoggerFactory
      .getLogger(SupplierReturnsWithDebitNoteCsvExporterJob.class);

  private static final String STORE_DEBIT_SELECT_QUERY = "SELECT s.dept_name as storename, "
      + " sgd.item_order as transactionid, sm.supplier_name as supplier,"
      + " sdn.debit_note_no as invoiceno, sdn.debit_note_date::date as invoicedate,"
      + " sid.item_barcode_id as itemcode, sgd.batch_no as batchno, sgd.exp_dt as expiry,"
      + " sgd.cost_price as cost, sgd.mrp, sgd.billed_qty as qty, '101'::text as movement_type";

  private static final String STORE_DEBIT_COUNT_QUERY = "select count(*) as cn";

  private static final String STORE_DEBIT_FILTER_QUERY = " FROM store_debit_note sdn "
      + " JOIN supplier_master sm on sm.supplier_code=sdn.supplier_id"
      + " JOIN store_grn_main sgm on sgm.debit_note_no=sdn.debit_note_no"
      + " JOIN store_grn_details sgd on sgd.grn_no=sgm.grn_no"
      + " JOIN store_item_details sid on sid.medicine_id=sgd.medicine_id"
      + " JOIN stores s on s.dept_id=sdn.store_id"
      + " WHERE sdn.status='C' AND sdn.date_time > ? and sdn.date_time <= ?";

  private static final String GET_LAST_CRON_DETAILS = "SELECT "
      + " max(job_start_time) as job_start_time"
      + " FROM job_log WHERE job_status='SUCCESS' and job_group = ?";

  private static final String FILE_NAME = EnvironmentUtil
      .getSupplierReturnsWithDebitNoteCsvExportPath() + "/StoreDebitNote_%s.csv";

  private static final String CRON_NAME = SupplierReturnsWithDebitNoteCsvExporterJob.class
      .getSimpleName();

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    setJobConnectionDetails();
    Timestamp thisExecutionCutOff = DateUtil.getCurrentTimestamp();

    String schema = getSchema();
    Timestamp lastExecutedCutOff = (Timestamp) DatabaseHelper
        .queryToDynaBean(GET_LAST_CRON_DETAILS, new Object[] { schema + "_" + CRON_NAME })
        .get("job_start_time");

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
    Calendar cal = Calendar.getInstance();
    cal.setTime(thisExecutionCutOff);

    int recordCount = DatabaseHelper.getInteger(STORE_DEBIT_COUNT_QUERY + STORE_DEBIT_FILTER_QUERY,
        new Object[] { lastExecutedCutOff, thisExecutionCutOff });
    if (recordCount < 1) {
      logger.debug("No closed debit notes to export");
      return;
    }
    String fileName = String.format(FILE_NAME, simpleDateFormat.format(DateUtil.getCurrentTime()));

    DatabaseHelper.queryWithCustomMapper(STORE_DEBIT_SELECT_QUERY + STORE_DEBIT_FILTER_QUERY,
        new Object[] { lastExecutedCutOff, thisExecutionCutOff }, new CSVMapper(fileName));

    logger.debug("Exported " + String.valueOf(recordCount) + " closed debit notes line items");

  }

  private class CSVMapper implements ResultSetExtractor<ResultSet> {
    private final String fileName;

    public CSVMapper(String fileName) {
      this.fileName = fileName;
    }

    @Override
    public ResultSet extractData(ResultSet resultSet) throws SQLException {
      try {
        File file = new File(fileName);
        if (!file.exists()) {
          if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
          }
          file.createNewFile();
        }
        // creating file writer object
        FileWriter fw = new FileWriter(file);

        CSVWriter csvWriter = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR,
            CSVWriter.NO_QUOTE_CHARACTER);
        // Dump the resultSet to csv
        csvWriter.writeAll(resultSet, true);
        csvWriter.flush();
        fw.close();
      } catch (IOException ex) {
        logger.error("", ex);
      }
      return null;
    }
  }
}
