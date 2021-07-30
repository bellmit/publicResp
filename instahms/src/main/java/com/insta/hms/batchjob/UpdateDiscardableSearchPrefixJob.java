package com.insta.hms.batchjob;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.jobs.GenericJob;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

public class UpdateDiscardableSearchPrefixJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(UpdateDiscardableSearchPrefixJob.class);

  @LazyAutowired
  public RedisTemplate<String, Object> redisTemplate;

  private String params;

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  private static final String GET_PATTERNS = "SELECT LEFT(patient_phone,5) as pat"
      + "   FROM patient_details"
      + "   GROUP BY LEFT(patient_phone,5)"
      + "   HAVING COUNT(LEFT(patient_phone,5)) >= 25000"
      + " UNION ALL"
      + "   SELECT left(government_identifier,5) as pat"
      + "   FROM patient_details GROUP BY"
      + "   LEFT(government_identifier,5)"
      + "   HAVING COUNT(LEFT(government_identifier,5)) >= 25000"
      + " UNION ALL"
      + "   SELECT LEFT(patient_phone,4) as pat"
      + "   FROM patient_details"
      + "   GROUP BY LEFT(patient_phone,4)"
      + "   HAVING COUNT(LEFT(patient_phone,4)) >= 25000"
      + " UNION ALL"
      + "   SELECT left(government_identifier,4) as pat"
      + "   FROM patient_details GROUP BY"
      + "   LEFT(government_identifier,4)"
      + "   HAVING COUNT(LEFT(government_identifier,4)) >= 25000";

  /**
   * Execute Job to compute 5 character discardable prefix patterns that match more than 50k 
   * results.
   * 
   * @param jobContext Context for job execution
   * @throws JobExecutionException  job execution exception
   */
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    String schema = getSchema();
    logger.info("Updating discardable search prefix patterns for " + schema);

    RequestContext.setConnectionDetails(new String[] { null, null, schema, "_system", "0", "" });
    
    List<BasicDynaBean> patterns = DatabaseHelper.queryToDynaList(GET_PATTERNS);
    List<String> discardablePatterns = new ArrayList<>();
    if (patterns != null) {
      for (BasicDynaBean pattern : patterns) {
        String value = ((String)pattern.get("pat")).trim();
        if (value.isEmpty()) {
          continue;
        }
        discardablePatterns.add(value.startsWith("+") ? value.substring(1) : value);
      }
    }
    redisTemplate.opsForValue().set("schema:" + schema + ";discardablesearchpatterns", 
        JsonUtility.toJson(discardablePatterns));
    logger.info("{} discardable search prefix patterns added for {}", discardablePatterns.size(),
        schema);
  }

}
