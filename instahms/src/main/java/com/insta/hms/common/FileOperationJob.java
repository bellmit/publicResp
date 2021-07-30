package com.insta.hms.common;

import com.bob.hms.common.RequestContext;
import com.google.common.base.Joiner;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.datauploaddownload.BulkUploadDownloadService;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.exception.HMSException;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.master.DoctorMaster.DoctorMasterAction;
import com.insta.hms.mdm.FileOperationService;
import com.insta.hms.mdm.FileOperationService.OperationScreenType;

import org.apache.commons.io.FileUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FileOperationJob extends GenericJob {

  private static Logger logger = LoggerFactory.getLogger(FileOperationJob.class);

  private static final String COMPLETED = "completed";

  private static final String FAIL = "fail";

  private static final int REDIS_KEY_ALIVE_TIME = 72;

  /** RedisTemplate. */
  @LazyAutowired
  public RedisTemplate<String, Object> redisTemplate;

  /** File operation service. */
  @LazyAutowired
  FileOperationService fileOperationService;

  /** Bulk Upload Download Service. */
  @LazyAutowired
  BulkUploadDownloadService bulkUploadDownloadService;

  /**
   * This method starts processing a background job based on redis key.
   * 
   * @param the jobContext
   */
  @Override
  protected void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {

    JobDataMap jobDataMap = jobContext.getJobDetail().getJobDataMap();

    String schema = jobDataMap.get("schema").toString();
    String userName = jobDataMap.get("userName").toString();
    RequestContext
        .setConnectionDetails(new String[] { null, null, jobDataMap.get("schema").toString(),
            jobDataMap.get("userName").toString(), jobDataMap.get("centerId").toString() });
    String completedAt = null;
    File file = null;
    Date date = null;
    String currentDate = null;
    File receivedFile = null;
    String status = FAIL;
    String message = null;
    boolean success = false;

    String action = jobDataMap.get("action").toString();
    boolean isUpload = action.equalsIgnoreCase("Upload");
    boolean isDownload = action.equalsIgnoreCase("download");
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    if (isUpload) {
      file = (File) jobDataMap.get("file");
    } else {
      date = new Date();
      currentDate = sdf.format(date);
      Path path = Paths.get(EnvironmentUtil.getTempDirectory() + File.separator + currentDate
          + File.separator + schema + File.separator + userName);
      jobDataMap.put("filePath", path);
      if (!Files.exists(path)) {
        try {
          Files.createDirectories(path);
        } catch (IOException exception) {
          logger.error("Failed to create directories");
          throw new HMSException("Failed to create directories");
        }
      }
    }
    String redisValueTemplate = 
        "status:%s;action:%s;master:%s;startedAt:%s;completedAt:%s;file:%s;message:%s";
    String redisValue = null;
    String startedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    redisValue = String.format(redisValueTemplate, "In-Process", action,
        jobDataMap.get("master").toString(), startedAt, "", file, message);
    String redisKey = jobDataMap.getString("redisKey");
    redisTemplate.opsForValue().set(redisKey, redisValue);
    redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);

    try {
      switch ((OperationScreenType) jobDataMap.get("master")) {
        case DoctorDefinitionDetails:
          DoctorMasterAction doctorMasterAction = new DoctorMasterAction();
          if (isUpload) {
            doctorMasterAction.importDoctorDetailsFromXlsJob(file);
          } else if (isDownload) {
            receivedFile = doctorMasterAction.exportDoctorDetailsToXlsJob();
            file = new File(
                EnvironmentUtil.getTempDirectory() + File.separator + currentDate + File.separator
                    + schema + File.separator + userName + File.separator + receivedFile.getName());
            FileUtils.copyFile(receivedFile, file);
          }
          success = true;
          break;
        case DoctorCharges:
          DoctorMasterAction doctorMasterAct = new DoctorMasterAction();
          if (isUpload) {
            doctorMasterAct.importChargesFromXlsJob(jobDataMap);
          } else if (isDownload) {
            receivedFile = doctorMasterAct.exportChargesToXlsJob(jobDataMap);
            file = new File(
                EnvironmentUtil.getTempDirectory() + File.separator + currentDate + File.separator
                    + schema + File.separator + userName + File.separator + receivedFile.getName());
            FileUtils.copyFile(receivedFile, file);
          }
          success = true;
          break;
        case BulkPatientData:
          if (isUpload) {
            success = bulkUploadDownloadService.bulkUploadPatientData(jobDataMap);
          } else {
            file = bulkUploadDownloadService.bulkDownloadPatientData(jobDataMap);
            success = true;
          }
          break;
        case InsuranceCompany:
          if (isUpload) {
            success = bulkUploadDownloadService.bulkUploadInsuranceCompany(jobDataMap);
          } else {
            file = bulkUploadDownloadService.bulkDownloadInsuranceCompany(jobDataMap);
            success = true;
          }
          break;
        case TpaMaster:
          if (isUpload) {
            success = bulkUploadDownloadService.bulkUploadTpa(jobDataMap);
          } else {
            file = bulkUploadDownloadService.bulkDownloadTpa(jobDataMap);
            success = true;
          }
          break;
        case InsurancePlanType:
          if (isUpload) {
            success = bulkUploadDownloadService.bulkUploadInsurancePlanType(jobDataMap);
          } else {
            file = bulkUploadDownloadService.bulkDownloadInsurancePlanType(jobDataMap);
            success = true;
          }
          break;
        case InsurancePlan:
          if (isUpload) {
            success = bulkUploadDownloadService.bulkUploadInsurancePlan(jobDataMap);
          } else {
            file = bulkUploadDownloadService.bulkDownloadInsurancePlan(jobDataMap);
            success = true;
          }
          break;
        case CodeSets :
          if (isUpload) {
            success = bulkUploadDownloadService.bulkUploadCodeSets(jobDataMap);
          } else {
            file = bulkUploadDownloadService.bulkDownloadCodeSets(jobDataMap);
            success = true;
          }
          break;
        default :
          logger.info("No proper master action for upload/download");
          break;
      }
      if (success) {
        status = COMPLETED;
      }
    } catch (HMSException exception) {
      message = Joiner.on("\n").join(exception.getErrorsList());
      logger.error("File Uppload/Download failed :" + message);
    } catch (IOException | SQLException exception) {
      message = exception.getMessage();
      logger.error(message);
    } finally {
      completedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      redisValue = String.format(redisValueTemplate, status, action,
          jobDataMap.get("master").toString(), startedAt, completedAt, file, message);
      redisTemplate.opsForValue().set(redisKey, redisValue);
      redisTemplate.expire(redisKey, REDIS_KEY_ALIVE_TIME, TimeUnit.HOURS);
    }
  }
}