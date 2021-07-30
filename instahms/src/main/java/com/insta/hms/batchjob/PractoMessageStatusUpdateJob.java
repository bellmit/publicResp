package com.insta.hms.batchjob;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Calendar;

public class PractoMessageStatusUpdateJob extends SQLUpdateJob {

  private static Logger logger = LoggerFactory.getLogger(PractoMessageStatusUpdateJob.class);

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    String schema = getSchema();
    RequestContext.setConnectionDetails(new String[] { null, null, schema });
    BasicDynaBean modBean;
    try {
      modBean = new GenericDAO("modules_activated").findByKey("module_id", "mod_practo_sms");

      if (modBean != null && modBean.get("activation_status") != null
          && modBean.get("activation_status").equals("Y")) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String startDate = DateUtil.formatIso8601Timestamp(cal.getTime());
        PractoMessageStatusUpdateJobUtil.updateMessageStatus(schema, startDate);
      }
    } catch (SQLException exception) {
      logger.error("Exception in PractoMessageStatusUpdateJob:  ", exception);
    }
  }
}