package com.insta.hms.ipservices;

import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * This class is only for struts classes not for spring classes 
 * TODO : This will be removed once ADT
 * will move in spring.
 *
 * @author yashwant
 */
public class BedChargeJobDetails {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(BedChargeJobDetails.class);

  /**
   * Gets the job time summary.
   *
   * @return SS:MM:HH Ex:If job is scheduled at 12 O'clock night,then time will be (00 : 00 : 12)
   */
  public static Map<String, Object> getJobTimeSummery() {

    try {
      /**
       * Adding try catch because in case of any abnormal conditions. Bed shift screen should not
       * interrupt Bed charge post timing
       **/
      Map<String, Object> bedChargeTimeDetails = new HashMap<String, Object>();
      GenericDAO cronDao = new GenericDAO("cron_job");
      BasicDynaBean cronJobBean = cronDao.findByKey("job_name", "BedChargeUpdateJobScheduler");
      if (cronJobBean == null) {
        return null;
      }
      String jobTimeExpression = (String) cronJobBean.get("job_time");
      CronExpression cronExpr = new CronExpression(jobTimeExpression);
      String jobExprSummery = cronExpr.getExpressionSummary();
      String[] arrJobExpSummery = jobExprSummery.split("\n");
      if (null != arrJobExpSummery && arrJobExpSummery.length >= 3) {
        String sec = ((arrJobExpSummery[0].split(":") != null) 
            && (arrJobExpSummery[0].split(":").length == 2)) ? arrJobExpSummery[0]
            .split(":")[1] : "-";
        String min = ((arrJobExpSummery[0].split(":") != null) 
            && (arrJobExpSummery[1].split(":").length == 2)) ? arrJobExpSummery[1]
            .split(":")[1] : "-";
        String hr = ((arrJobExpSummery[0].split(":") != null) 
            && (arrJobExpSummery[2].split(":").length == 2)) ? arrJobExpSummery[2]
            .split(":")[1] : "-";
        bedChargeTimeDetails.put(
            "job_time",
            String.format("%02d", Integer.parseInt(hr.trim())) + ":"
                + String.format("%02d", Integer.parseInt(min.trim())) + ":"
                + String.format("%02d", Integer.parseInt(sec.trim())));
      }

      bedChargeTimeDetails.put("job_status", (String) cronJobBean.get("job_status"));
      /* It returns SS : MM : HH */
      return bedChargeTimeDetails;
    } catch (Exception exp) {
      logger.error("Error finding information about BedChargeDetails, " + exp);
      return null;
    }
  }
}
