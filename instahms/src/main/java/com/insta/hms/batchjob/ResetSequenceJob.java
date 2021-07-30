package com.insta.hms.batchjob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * # # Script to reset the sequences back to 1 periodically. Note that this only # works on
 * sequences defined in hosp_id_patterns. #
 * 
 * @author insta
 *
 */
public class ResetSequenceJob extends SQLUpdateJob {

  private static Logger logger = LoggerFactory.getLogger(ResetSequenceJob.class);

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.SQLUpdateJob#executeInternal(org.quartz.JobExecutionContext)
   */
  @Override
  @Transactional
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    setJobConnectionDetails();
    Date date = new Date(); /*-----DateFormate----------*/
    String dow = new SimpleDateFormat("E").format(date); /* Day : Mon,Tue,Fri ... */
    String dom = new SimpleDateFormat("dd").format(date); /* Date : 01,02,03,04,... */
    String mon = new SimpleDateFormat("MM").format(date); /* Month : 01,02, ....... */
    /*--------------------------*/
    String types = "'D'";

    if (dow.equals("Mon")) {
      types += ",'W'";
    }

    if (dom.equals("01")) {
      types += ",'M'";
    }

    if (dom.equals("01") && mon.equals("01")) {
      types += ",'Y'";
    }

    if (dom.equals("01") && mon.equals("04")) {
      types += ",'F'";
    }

    String query = "SELECT setval(sequence_name, 1, false) FROM hosp_id_patterns "
        + " WHERE sequence_reset_freq IN (" + types + ") AND is_transactional_sequence = false";
    

    String query1 = " UPDATE hosp_id_patterns SET date_prefix = "
        + " coalesce(to_char(current_timestamp, date_prefix_pattern),'')  "
        + " WHERE sequence_reset_freq IN (" + types + ")";

    logger.debug(
        " Will update sample type pattern in schema ***" + RequestContext.getSchema() + "***");
    DatabaseHelper.queryToDynaBean(query);
    DatabaseHelper.update(query1);

    String resetTransactionalSequence = "UPDATE transactional_sequence set value = 1"
        + " where sequence_name in (SELECT DISTINCT sequence_name "
        + " FROM hosp_id_patterns WHERE is_transactional_sequence = true "
        + " AND sequence_reset_freq IN (" + types + "))";
    DatabaseHelper.update(resetTransactionalSequence);
    
    String sampleUpdateQuery = "UPDATE sample_type_number_prefs SET start_number = 1 "
        + " WHERE reset_freq IN ("
        + types + "); " + "UPDATE sample_type_number_prefs SET date_prefix = "
        + " coalesce(to_char(current_timestamp, date_prefix_pattern),'') " + "WHERE reset_freq IN ("
        + types + ")";
    DatabaseHelper.update(sampleUpdateQuery);

  }
}
