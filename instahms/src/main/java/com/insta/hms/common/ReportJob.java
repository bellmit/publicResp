package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.jobs.GenericJob;

import org.apache.commons.io.FileUtils;
import org.apache.struts.action.ActionMapping;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReportJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(ReportJob.class);

  @LazyAutowired
  public RedisTemplate<String, Object> redisTemplate;

  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();
    String schema = jobDataMap.get("schema").toString();
    String userName = jobDataMap.get("userName").toString();
    RequestContext
        .setConnectionDetails(new String[] { null, null, jobDataMap.get("schema").toString(),
            jobDataMap.get("userName").toString(), jobDataMap.get("centerId").toString() });

    String outputMode = jobDataMap.getString("outputMode");

    String redisKey = jobDataMap.getString("redisKey");
    String redisValueTemplate = "status:%s;fileName:%s;folder:%s;generatedAt:%s";
    String redisValue = null;

    Date date = new Date();
    String currentDate = new SimpleDateFormat("yyyyMMdd").format(date);
    String generatedAt = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss").format(date);

    StdReportEngine eng = null;
    String printTitle = null;
    try {
      StdReportDesc desc = null;
      if (jobDataMap.getString("reportAction").equals("standard")) {
        ActionMapping mapping = new ActionMapping();
        if (null != jobDataMap.getString("desc_provider")) {
          mapping.setProperty("desc_provider", jobDataMap.getString("desc_provider"));
        }
        if (null != jobDataMap.getString("report_desc")) {
          mapping.setProperty("report_desc", jobDataMap.getString("report_desc"));
        }
        desc = new StdReportAction().getReportDesc(mapping);
      } else if (jobDataMap.getString("reportAction").equals("favourite")) {
        desc = new FavouriteReportAction().getFavReportDesc(
            jobDataMap.getString("custmReportSrxmlName"),
            jobDataMap.getString("favReportActionId"));
      } else if (jobDataMap.getString("reportAction").equals("DaybookSummary")) {
        printTitle = "HospitalDayBook";
        String fileName = printTitle + generatedAt;
        if (outputMode.equals("text")) {
          printTitle = printTitle + ".txt";
          fileName = fileName + ".txt";
          File file = new File(EnvironmentUtil.getTempDirectory() + "/" + currentDate + "/" + schema
              + "/" + userName + "/" + fileName);
          ReportPrinter.processTextReport(file, "HospitalDayBook", (Map) jobDataMap.get("hm"),
              (PrintPageOptions) jobDataMap.get("opts"), new String[] { "DaybookSummary" },
              (String) jobDataMap.get("rootRealPath"));
        } else {
          printTitle = printTitle + ".pdf";
          fileName = fileName + ".pdf";
          File file = new File(EnvironmentUtil.getTempDirectory() + "/" + currentDate + "/" + schema
              + "/" + userName + "/" + fileName);
          ReportPrinter.processPdfStream(file, "HospitalDayBook", (Map) jobDataMap.get("hm"), null,
              new String[] { "DaybookSummary" }, (String) jobDataMap.get("rootRealPath"));
        }
        redisValue = String.format(redisValueTemplate, "completed", printTitle,
            currentDate + "/" + schema + "/" + userName, generatedAt);
        redisTemplate.opsForValue().set(redisKey, redisValue);
        redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // setting expiry time to 1 day
        return;
      } else if ("custom".equals(jobDataMap.getString("reportAction"))) {
        StdReportDescJsonProvider newp = new StdReportDescJsonProvider();
        desc = newp.getReportDescForString(jobDataMap.getString("reportMetadata"));
      }

      StdReportParams params = (StdReportParams) jobDataMap.get("params");

      if (jobDataMap.containsKey("reportFileName")) {
        printTitle = jobDataMap.getString("reportFileName");
      } else {
        printTitle = params.getPrintTitle();
      }
      String fileName = printTitle + generatedAt;
      if (outputMode.equals("pdf")) {
        printTitle = printTitle + ".pdf";
        fileName = fileName + ".pdf";
        File file = new File(EnvironmentUtil.getTempDirectory() + "/" + currentDate + "/" + schema
            + "/" + userName + "/" + fileName);
        FileOutputStream fileOutStream = FileUtils.openOutputStream(file);
        eng = new StdPdfReportEngine(desc, params, fileOutStream);
      } else if (outputMode.equals("csv")) {
        printTitle = printTitle + ".csv";
        fileName = fileName + ".csv";
        File file = new File(EnvironmentUtil.getTempDirectory() + "/" + currentDate + "/" + schema
            + "/" + userName + "/" + fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter filewriter = new FileWriter(file);
        eng = new StdCsvReportEngine(desc, params, filewriter);
      } else if (outputMode.equals("text")) {
        printTitle = printTitle + ".txt";
        fileName = fileName + ".txt";
        File file = new File(EnvironmentUtil.getTempDirectory() + "/" + currentDate + "/" + schema
            + "/" + userName + "/" + fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter filewriter = new FileWriter(file);
        eng = new StdTextReportEngine(desc, params, filewriter);
      } else {
        logger.error("Invalid output mode: " + outputMode);
        return;
      }
      eng.writeReport();
      redisValue = String.format(redisValueTemplate, "completed", printTitle,
          currentDate + "/" + schema + "/" + userName, generatedAt);
      redisTemplate.opsForValue().set(redisKey, redisValue);
      redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // setting expiry time to 1 day

    } catch (Exception exception) {
      logger.error("Report Generation failed with exception: ", exception);
      if (!StringUtil.isNullOrEmpty(printTitle)) {
        logger.error(printTitle + " failed: ", exception);
        String fileName = printTitle.split("\\.")[0] + generatedAt + "-errors.txt";
        File file = new File(EnvironmentUtil.getTempDirectory() + "/" + currentDate + "/" + schema
            + "/" + userName + "/" + fileName);
        file.getParentFile().mkdirs();
        try {
          file.createNewFile();
          PrintStream ps = new PrintStream(file);
          exception.printStackTrace(ps);
        } catch (IOException e1) {
          logger.error("Cannot create log file " + fileName, e1);
        }
      }
      redisValue = String.format(redisValueTemplate, "failed", printTitle,
          currentDate + "/" + schema + "/" + userName, generatedAt);
      redisTemplate.opsForValue().set(redisKey, redisValue);
      redisTemplate.expire(redisKey, 24, TimeUnit.HOURS); // setting expiry time to 1 day
    }
  }
}