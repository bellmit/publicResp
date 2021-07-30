package com.insta.hms.integration.insurance.submission;

import com.insta.hms.jobs.GenericJob;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SelfPayXmlJob extends GenericJob {

  @Autowired
  SelfPaySubmissionService selfpayService;

  static Logger logger = LoggerFactory.getLogger(SelfPayXmlJob.class);

  private int selfpayBatchId;
  private HashMap urlRightsMap;
  private HashMap actionUrlMap;
  private String path;

  public int getSelfpayBatchId() {
    return selfpayBatchId;
  }

  public void setSelfpayBatchId(int selfpayBatchId) {
    this.selfpayBatchId = selfpayBatchId;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    // set status as processing
    Integer selfpayBatchId = (Integer) jobContext.getJobDetail().getJobDataMap()
        .get("selfpay_batch_id");
    BasicDynaBean bean = selfpayService.getBean();
    bean.set("processing_status", "P");
    bean.set("selfpay_batch_id", selfpayBatchId);
    Map<String, Object> keys = new HashMap<>();
    keys.put("selfpay_batch_id", selfpayBatchId);
    selfpayService.update(bean, keys);

    // generate a claim XML
    try {
      boolean success = selfpayService.generate(selfpayBatchId, urlRightsMap, actionUrlMap, path);
      if (success) {
        // set status as completed successfully
        bean.set("processing_status", "C");
        logger.debug("Finished processing selfpay batch successfully : " + selfpayBatchId);
      } else {
        // set status as failed
        bean.set("processing_status", "F");
        logger.debug("Failed to process selfpay batch : " + selfpayBatchId);
      }
      keys.clear();
      keys.put("selfpay_batch_id", selfpayBatchId);
      selfpayService.update(bean, keys);
    } catch (Exception exception) {
      exception.printStackTrace();
      logger.error("SelfpayXML Job exception", exception);
      bean.set("processing_status", "F");
      keys.clear();
      keys.put("selfpay_batch_id", selfpayBatchId);
      selfpayService.update(bean, keys);
      throw new JobExecutionException(exception);
    }
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public HashMap getUrlRightsMap() {
    return urlRightsMap;
  }

  public void setUrlRightsMap(HashMap urlRightsMap) {
    this.urlRightsMap = urlRightsMap;
  }

  public HashMap getActionUrlMap() {
    return actionUrlMap;
  }

  public void setActionUrlMap(HashMap actionUrlMap) {
    this.actionUrlMap = actionUrlMap;
  }
}
