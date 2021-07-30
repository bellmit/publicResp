package com.insta.hms.batchjob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.integration.book.BookSDKCallback;
import com.insta.hms.integration.book.BookSDKUtil;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.JobService;
import com.practo.integration.sdk.SDKException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PractoSDKInitializationJob extends GenericJob {
  private Logger logger = LoggerFactory.getLogger(PractoSDKInitializationJob.class);
  @LazyAutowired
  private InstaIntegrationService instaIntegrationService;
  @LazyAutowired
  private ApplicationContext applicationContext;
  @LazyAutowired
  private SecurityService securityService;
  @LazyAutowired
  private JobService jobService;

  @Override
  public void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
    List<BasicDynaBean> schemaList = DatabaseHelper.getAllSchemas();
    for (BasicDynaBean schemaBean : schemaList) {
      String schema = (String) schemaBean.get("schema");
      RequestContext.setConnectionDetails(new String[] { null, null, schema });
      if (securityService.getActivatedModules().contains(BookSDKUtil.MODULE_ID)) {
        RequestContext.setConnectionDetails(new String[] { null, "", schema, "", "", "" });
        initToPracto();
      }
    }
  }

  /**
   * Establish the connection with each center of schema.
   */
  private void initToPracto() {
    BasicDynaBean integrationRecord = instaIntegrationService
        .getActiveRecord(BookSDKUtil.PRACTO_BOOK_INTEGRATION);
    Integer integrationId = (Integer) integrationRecord.get("integration_id");
    String applicationID = (String) integrationRecord.get("application_id");
    String applicationSecret = (String) integrationRecord.get("application_secret");
    String agentHost = (String) integrationRecord.get("agent_host");
    Integer agentPort = (Integer) integrationRecord.get("agent_port");
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("integration_id", integrationId);
    List<BasicDynaBean> centerIntegrationPrefs = instaIntegrationService
        .centerDetailsLookup(filterMap);
    for (BasicDynaBean centerBean : centerIntegrationPrefs) {
      String establishmentKey = (String) centerBean.get("establishment_key");
      if (establishmentKey == null || establishmentKey.trim().isEmpty()) {
        continue;
      }
      Integer centerId = (Integer) centerBean.get("center_id");
      BookSDKCallback bookSDKCallback = applicationContext.getBean(BookSDKCallback.class,
          RequestContext.getSchema(), centerId);
      try {
        BookSDKUtil.init(applicationID, applicationSecret, establishmentKey, bookSDKCallback,
            agentHost, agentPort);
        logger.info(String.format("Connect to Practo for center: [%s] is successfull", centerId));
      } catch (SDKException ex) {
        logger.error(String.format("Failed to connect to Practo for center:[%s]", centerId), ex);
      }
    }
  }
}
