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
public class PersonRegisterXmlJob extends GenericJob {

  @Autowired
  PersonRegisterSubmissionService personRegisterSubmissionService;

  static Logger logger = LoggerFactory.getLogger(SelfPayXmlJob.class);

  private int personRegisterBatchId;
  private HashMap urlRightsMap;
  private HashMap actionUrlMap;
  private String path;

  public int getPersonRegisterBatchId() {
    return personRegisterBatchId;
  }

  public void setPersonRegisterBatchId(int personRegisterBatchId) {
    this.personRegisterBatchId = personRegisterBatchId;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    // set status as processing
    String personRegisterBatchId =  (String)jobContext.getJobDetail().getJobDataMap()
        .get("personregister_batch_id");
    BasicDynaBean bean = personRegisterSubmissionService.getBean();
    bean.set("processing_status", "P");
    bean.set("personregister_batch_id", personRegisterBatchId);
    Map<String, Object> keys = new HashMap<>();
    keys.put("personregister_batch_id", personRegisterBatchId);
    personRegisterSubmissionService.update(bean, keys);

    // generate a claim XML
    try {
      boolean success = personRegisterSubmissionService.generate(personRegisterBatchId,
          urlRightsMap, actionUrlMap, path);
      if (success) {
        // set status as completed successfully
        bean.set("processing_status", "C");
        logger.debug(
            "Finished processing person Register batch successfully : " + personRegisterBatchId);
      } else {
        // set status as failed
        bean.set("processing_status", "F");
        logger.debug("Failed to process person register batch : " + personRegisterBatchId);
      }
      keys.clear();
      keys.put("personregister_batch_id", personRegisterBatchId);
      personRegisterSubmissionService.update(bean, keys);
    } catch (Exception exception) {
      exception.printStackTrace();
      logger.error("Person Regsiter XML Job exception", exception);
      bean.set("processing_status", "F");
      keys.clear();
      keys.put("personregister_batch_id", personRegisterBatchId);
      personRegisterSubmissionService.update(bean, keys);
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
