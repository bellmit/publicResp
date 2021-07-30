package com.insta.hms.batchjob;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class ClearInterfaceMessageQueue extends SQLUpdateJob {

  private static Logger logger = LoggerFactory.getLogger(ClearInterfaceMessageQueue.class);

  private String params;

  @Override
  public String getParams() {
    return params;
  }

  @Override
  public void setParams(String params) {
    this.params = params;
  }

  public String[] getStatus() {
    return StringUtils.isEmpty(getParams()) ? new String[] {"SENT"} : getParams().split(",");
  }

  private static final String CLEAR_INTERFACE_QUEUE_QUERY = "DELETE FROM export_message_queue";

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    setJobConnectionDetails();

    Object[] queryParams = new Object[getStatus().length + 1];
    StringBuilder query = new StringBuilder(CLEAR_INTERFACE_QUEUE_QUERY);
    query.append(" WHERE status IN (");
    int count;
    for (count = 0; count < getStatus().length; count++) {
      if (count == 0) {
        query.append("?");
      } else {
        query.append(",?");
      }
      queryParams[count] = getStatus()[count];
    }
    query.append(")");
    query.append(" AND created_at < ?");

    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DAY_OF_MONTH, -60);

    Timestamp date = new Timestamp(cal.getTimeInMillis());
    queryParams[count] = date;

    int clearedqueueCount = DatabaseHelper.delete(query.toString(), queryParams);
    logger.info("Cleared {} entries from interface queue having status {} and older than {}.",
        clearedqueueCount, getStatus(), date);
  }
}
