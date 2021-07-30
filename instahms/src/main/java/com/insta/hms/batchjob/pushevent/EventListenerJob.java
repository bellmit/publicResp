package com.insta.hms.batchjob.pushevent;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.JobService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

@Component
public class EventListenerJob extends GenericJob {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  EventClassMapInitializer eventClassMapInit;

  @Autowired
  private JobService jobService;

  @LazyAutowired
  private BeanFactory beanFactory;

  private String eventId;
  private String schema;
  private HashMap<String, Object> eventData;
 
  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public HashMap<String, Object> getEventData() {
    return eventData;
  }

  public void setEventData(HashMap<String, Object> eventData) {
    this.eventData = eventData;
  }

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    RequestContext
        .setConnectionDetails(new String[] { null, null, getSchema(), "InstaAdmin", "0" });
    HashMap<String, ArrayList<Class>> map = eventClassMapInit.getEventClassMap();
    ArrayList<Class> classList = map.get(getEventId());
    for (Class cl : classList) {
      try {
        Method clientDetails = cl.getDeclaredMethod("clientDetails",String.class);
        List<BasicDynaBean> clients = (List) clientDetails.invoke(beanFactory.getBean(cl),
            getEventId());
        if (!clients.isEmpty()) {
          for (BasicDynaBean bean : clients) {
            HashMap<String, Object> dataMap = getEventData();
            dataMap.putAll(bean.getMap());
            dataMap.put("firstEventTime", System.currentTimeMillis());
            dataMap.put("retryCount", 0);
            jobService.scheduleImmediate(buildJob(
                "EventJob_" + System.currentTimeMillis() + new Random().nextInt(100), cl, dataMap));
          }
        }
      } catch (IllegalAccessException | NoSuchMethodException | SecurityException
          | IllegalArgumentException | InvocationTargetException exp) {
        logger.error(exp.getMessage() + "" + exp.getStackTrace());
      }
    }
  }

  protected void reTrigger(HashMap dataMap, Class cl) {
    int count = (int) dataMap.get("retryCount");
    if ((!(dataMap.get("retry_duration_min") == null)
        && !dataMap.get("retry_duration_min").equals("")
        && System.currentTimeMillis() < (Long.valueOf((Integer) dataMap.get("retry_duration_min"))
            * 60000) + (long) dataMap.get("firstEventTime"))
        || (!(dataMap.get("retry_count") == null) && !dataMap.get("retry_count").equals("")
            && (int) dataMap.get("retry_count") > count)) {
      count++;
      dataMap.put("retryCount", count);
      jobService.scheduleImmediate(buildJob(
          "EventJob_" + System.currentTimeMillis() + new Random().nextInt(100), cl, dataMap));
    }
  }
}
