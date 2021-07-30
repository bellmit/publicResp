package com.insta.hms.batchjob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

public abstract class SQLUpdateJob extends QuartzJobBean {

  private static final Logger logger = LoggerFactory.getLogger(SQLUpdateJob.class);

  /* This is required for setting schema to run the job in particular schema */
  private String schema;
  private String params;

  /* params and schema is required because of setting job data */
  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  protected void setJobConnectionDetails() {
    RequestContext.setConnectionDetails(new String[] { null, "", getSchema(), "_system", "", "" });
  }

  @Override
  @Transactional
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    setJobConnectionDetails();
    logger.debug("Job Key : " + jobContext.getJobDetail().getKey() + ", Running in schema : "
        + RequestContext.getSchema());
    List<String> queries = getQueryList();

    for (String query : queries) {
      DatabaseHelper.update(query);
    }
  }

  protected List<String> getQueryList() {
    return Collections.emptyList();
  }

  protected void executeUpdate(String query, Connection con) {
    Statement stmt = null;
    if (con == null) {
      return;
    }
    int rowsStatus = 0;
    try {
      stmt = con.createStatement();
      rowsStatus = stmt.executeUpdate(query);
      logger.debug(this.getClass() + " : sql = \n" + query);
      logger.info(this.getClass() + " : Updating no of rows by CronJob : " + rowsStatus
          + " : schema=" + RequestContext.getSchema());

    } catch (SQLException exception) {
      logger.error(this.getClass() + "    Error  :  " + exception + "     :    In schema="
          + RequestContext.getSchema() + "\n" + "SQL=" + query);
    } finally {
      DataBaseUtil.closeConnections(con, stmt);
    }
  }

  protected void executeUpdate(List<String> queryList, Connection con) {
    Statement stmt = null;
    if (con == null) {
      return;
    }
    int rowsStatus = 0;
    try {
      stmt = con.createStatement();
    } catch (SQLException exception) {
      logger.error(exception.getMessage());
    }

    try {
      int index = 1;
      for (String query : queryList) {
        try {

          rowsStatus = stmt.executeUpdate(query);
          logger.debug("sql-" + index + "=" + query);
          logger.debug(this.getClass() + " : Updating no of rows by CronJob : " + rowsStatus
              + " : schema=" + RequestContext.getSchema());
        } catch (SQLException exception) {
          logger.error(this.getClass() + "    Error  :  " + exception + "     :    In schema="
              + RequestContext.getSchema() + "\n" + "SQL=" + query);
        }
        index++;
      }
    } finally {
      DataBaseUtil.closeConnections(con, stmt);
    }
  }

  protected void execute(String sql, Connection con) {
    Statement stmt = null;
    try {
      stmt = con.createStatement();
      stmt.execute(sql);
      logger.debug(this.getClass() + " :  Job executed successfully for : schema = "
          + RequestContext.getSchema());
    } catch (SQLException exception) {
      logger.error(this.getClass() + "    Error  :  " + exception + "     :    In schema="
          + RequestContext.getSchema() + "\n" + "SQL=" + sql);

    } finally {
      DataBaseUtil.closeConnections(con, stmt);
    }

  }

}
