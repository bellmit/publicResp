package com.insta.hms.batchjob;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;

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

public class CEODashboardJob extends SQLUpdateJob {

  private static final Logger logger = LoggerFactory.getLogger(CEODashboardJob.class);

  private static final String CEO_DASHBOARD_QUERY = "SELECT * FROM ceo_dashbaord_data_view "
      + "WHERE date::date >= ?";

  private static final String UPDATE_CRON_DETAILS = "UPDATE cron_details SET last_exec_time= ? "
      + "WHERE cron_name = ?";

  private static final String GET_CRON_DETAILS = "SELECT * FROM cron_details WHERE cron_name = ?";

  private static final String FILE_NAME = "/tmp/ceodashboard/%s-%s.csv";

  private static final String CRON_NAME = "CEODashboardJob";

  private static final String MODULE_ID = "mod_ceo_dashboard";

  @LazyAutowired
  private SecurityService securityService;

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    setJobConnectionDetails();
    if (!securityService.getActivatedModules().contains(MODULE_ID)) {
      logger.debug("Job skipped as module is not enabled");
      return;
    }
    Timestamp timestamp = (Timestamp) DatabaseHelper
        .queryToDynaBean(GET_CRON_DETAILS, new Object[] { CRON_NAME }).get("last_exec_time");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String fileName = null;
    Calendar cal = Calendar.getInstance();
    if (timestamp != null) {
      // last cron Execution day MINUS 1 day
      cal.setTime(timestamp);
      cal.add(Calendar.DAY_OF_MONTH, -1);

      fileName = String.format(FILE_NAME, RequestContext.getSchema(),
          simpleDateFormat.format(DateUtil.getCurrentDate()));

    } else {
      // last 2 years
      cal.setTime(DateUtil.getCurrentTimestamp());
      cal.add(Calendar.YEAR, -2);
      fileName = String.format(FILE_NAME, RequestContext.getSchema(), "initialdump");

    }
    DatabaseHelper.queryWithCustomMapper(CEO_DASHBOARD_QUERY,
        new Object[] { DateUtil.getDatePart(new Timestamp(cal.getTimeInMillis())) },
        new CsvMapper(fileName));
    // Update the last cron execution time to current time
    DatabaseHelper.update(UPDATE_CRON_DETAILS,
        new Object[] { DateUtil.getCurrentTimestamp(), CRON_NAME });

    logger.info("Job has successfuly executed at :" + DateUtil.getCurrentTimestamp());

  }

  private class CsvMapper implements ResultSetExtractor<ResultSet> {
    private final String fileName;

    public CsvMapper(String fileName) {
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
