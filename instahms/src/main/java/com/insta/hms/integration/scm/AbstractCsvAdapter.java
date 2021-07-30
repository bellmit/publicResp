package com.insta.hms.integration.scm;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.batchjob.GenericCsvExportJob;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.JobService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public abstract class AbstractCsvAdapter {

  @LazyAutowired
  private JobService jobService;

  protected CsvContext context;

  public AbstractCsvAdapter(CsvContext context) {
    this.context = context;
  }

  /**
   * Schedule txn export.
   *
   * @param transactions  the transactions
   * @param transactionId the transaction id
   */
  public void scheduleTxnExport(List<Map<String, Object>> transactions, String transactionId) {
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("eventId", getTransactionType() + "_" + transactionId);
    jobData.put("eventData", transactions);
    jobData.put("context", this.context);

    jobService.scheduleImmediate(
        buildJob((String) jobData.get("eventId"), GenericCsvExportJob.class, jobData));
  }

  /**
   * Schedule txn export.
   *
   * @param transactions the transactions
   */
  public void scheduleTxnExport(List<Map<String, Object>> transactions) {
    Date date = new Date();
    scheduleTxnExport(transactions, String.valueOf(date.getTime()));
  }


  /**
   * Gets transaction type.
   *
   * @return the transaction type
   */
  protected String getTransactionType() {
    return context.getEntityName();
  }

  /**
   * Map to job data map.
   *
   * @param map the map
   * @return the map
   */
  public abstract Map<String, Object> mapToJobData(Map<String, Object> map);

  /**
   * Bean to job data map.
   *
   * @param bean the bean
   * @return the map
   */
  public Map<String, Object> beanToJobData(BasicDynaBean bean) {
    if (bean != null) {
      return mapToJobData(bean.getMap());
    } else {
      return Collections.emptyMap();
    }
  }

  /**
   * Bean to job data list.
   *
   * @param beans the beans
   * @return the list
   */
  public List<Map<String, Object>> beanToJobData(List<BasicDynaBean> beans) {
    List<Map<String, Object>> jobData = new ArrayList<>();

    if (beans == null) {
      return Collections.emptyList();
    }

    for (BasicDynaBean bean : beans) {
      jobData.add(beanToJobData(bean));
    }
    return jobData;
  }
}
