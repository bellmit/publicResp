package com.insta.hms.batchjob;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import au.com.bytecode.opencsv.CSVWriter;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.integration.scm.inventory.ScmOutBoundInvService;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.JobService;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.http.conn.UnsupportedSchemeException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//TODO: This is for inventory purpose only. Merge with GenericCsvExportJob as per new framework
@DisallowConcurrentExecution
@Component
public class CsvExportJob extends GenericJob {

  public static final String PKG_SIZE = "PKG_SIZE";
  private static final Logger logger = LoggerFactory.getLogger(CsvExportJob.class);
  private static final String SCHEME_SFTP = "sftp";
  private static final String SCHEME_FILE = "file";
  private static final String[] SUPPORTED_SCHEMES = new String[] {SCHEME_SFTP, SCHEME_FILE};

  private static final String ENTITY_NAME = "inventory";
  public static final String TRANSACTION_ID = "TRANSACTION_ID";
  public static final String TRANSACTION_TYPE = "TRANSACTION_TYPE";
  public static final String TRANSACTION_ITEM_ID = "TRANSACTION_ITEM_ID";
  public static final String TRANSACTION_DATE = "TRANSACTION_DATE";
  public static final String TRANSACTION_DATE_TIME = "TRANSACTION_DATE_TIME";
  public static final String USER_NAME = "USER_NAME";
  public static final String SOURCE_APPLICATION = "SOURCE_APPLICATION";
  public static final String SOURCE_STORE_NAME = "SOURCE_STORE_NAME";
  public static final String SOURCE_STORE_CODE = "SOURCE_STORE_CODE";
  public static final String SOURCE_CENTER_NAME = "SOURCE_CENTER_NAME";
  public static final String SOURCE_CENTER_CODE = "SOURCE_CENTER_CODE";
  public static final String DEPARTMENT_NAME = "DEPARTMENT_NAME";
  public static final String MRN = "MRN";
  public static final String VISIT_ID = "VISIT_ID";
  public static final String ITEM_INTERFACE_CODE = "ITEM_INTERFACE_CODE";
  public static final String ITEM_NAME = "ITEM_NAME";
  public static final String BATCH_NUMBER = "BATCH_NUMBER";
  public static final String BATCH_EXPIRY_DATE = "BATCH_EXPIRY_DATE";
  public static final String ITEM_COST = "ITEM_COST";
  public static final String TRANSACTION_QUANTITY = "TRANSACTION_QUANTITY";
  public static final String TRANSACTION_UOM = "TRANSACTION_UOM";
  public static final String DISCOUNT = "DISCOUNT";
  public static final String DISCOUNT_PERCENT = "DISCOUNT_PERCENT";
  public static final String TAX_CODE = "TAX_CODE";
  public static final String TAX_AMOUNT = "TAX_AMOUNT";
  public static final String MRP = "MRP";
  public static final String AMOUNT = "AMOUNT";
  public static final String ADJUSTMENT_TYPE = "ADJUSTMENT_TYPE";
  public static final String DESTINATION_STORE_NAME = "DESTINATION_STORE_NAME";
  public static final String DESTINATION_STORE_CODE = "DESTINATION_STORE_CODE";
  public static final String DESTINATION_CENTER_NAME = "DESTINATION_CENTER_NAME";
  public static final String DESTINATION_CENTER_CODE = "DESTINATION_CENTER_CODE";
  public static final String BILL_DISCOUNT = "BILL_DISCOUNT";
  public static final String ROUND_OFF = "ROUND_OFF";
  public static final String REMARKS = "REMARKS";

  private String eventId;
  private int ttl;
  private List<Map<String, Object>> eventData;

  @LazyAutowired
  private ScmOutBoundInvService invTransaction;

  @LazyAutowired
  JobService jobService;

  private FileLock tryLock(RandomAccessFile randomAccessFile) throws IOException {
    FileChannel fileChannel = randomAccessFile.getChannel();
    return fileChannel.tryLock();
  }

  private Map<String, Object> buildJobData() {
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("schema", getSchema());
    jobData.put("eventData", eventData);
    jobData.put("eventId", getEventId());
    jobData.put("ttl", getTtl() - 1);
    return jobData;
  }

  @Override
  protected synchronized void executeInternal(JobExecutionContext arg0)
      throws JobExecutionException {
    StandardFileSystemManager fileSystemManager = null;
    RandomAccessFile randomAccessFile = null;
    FileLock lock = null;


    try {
      if (getSchema() == null) {
        throw new IllegalStateException("schema can't be null");
      }
      if (EnvironmentUtil.getCsvExportUri() == null
          || EnvironmentUtil.getCsvExportUri().trim().isEmpty()) {
        logger.error(EnvironmentUtil.URI_EXPORT_CSVS + " not defined. Exiting job");
        return;
      }
      URI csvExportUri = new URI(EnvironmentUtil.getCsvExportUri() + "/" + getSchema().trim()
          + "/" + ENTITY_NAME + "/out");
      String uriScheme = csvExportUri.getScheme();

      if (!ArrayUtils.contains(SUPPORTED_SCHEMES, uriScheme)) {
        throw new UnsupportedSchemeException("Scheme " + uriScheme + "not supported.");
      }

      fileSystemManager = new StandardFileSystemManager();
      fileSystemManager.init();
      FileObject csvExportDirectory = fileSystemManager.resolveFile(csvExportUri);

      if (!csvExportDirectory.exists() || !csvExportDirectory.isFolder()) {
        logger.info("Path not found " + csvExportDirectory.getPublicURIString());
        csvExportDirectory.createFolder();
        logger.info("Created " + csvExportDirectory.getPublicURIString());
      }

      Boolean fileExists = false;
      String fileName = null;
      for (FileObject file : csvExportDirectory.getChildren()) {
        if (file.isFile() && file.getName().getBaseName().matches(ENTITY_NAME
            + "_\\d{4}-\\d{2}-\\d{2}T\\d{6}\\.csv")) {
          fileExists = true;
          fileName = file.getName().getBaseName();
        }
      }

      if (uriScheme.equals(SCHEME_FILE) && fileExists) {
        File file = new File(makePath(csvExportUri, fileName));
        randomAccessFile = new RandomAccessFile(file, "rw");
        lock = tryLock(randomAccessFile);
        if (lock == null) {
          fileExists = false;
          fileName = null;
          randomAccessFile.close();
        }

      }

      BufferedWriter outWriter = null;
      if (fileExists) {
        outWriter = new BufferedWriter(new OutputStreamWriter(fileSystemManager
            .resolveFile(makePath(csvExportUri, fileName)).getContent().getOutputStream(true)));
      } else {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
        Date date = new Date();
        URI newFileUri = makePath(csvExportUri,
            ENTITY_NAME + "_" + dateFormat.format(date) + ".csv");
        outWriter = new BufferedWriter(new OutputStreamWriter(fileSystemManager
            .resolveFile(newFileUri)
            .getContent().getOutputStream()));
        if (uriScheme.equals(SCHEME_FILE)) {
          randomAccessFile = new RandomAccessFile(new File(newFileUri), "rw");
          lock = tryLock(randomAccessFile);

          if (lock == null) {

            logger.error(
                "Acquiring lock on new file failed. event " + getEventId() + " TTL = " + getTtl());
            if (getTtl() <= 0) {
              return;
            }
            jobService
                .scheduleRandom(buildJob(getEventId(), CsvExportJob.class, buildJobData()), 5);
            return;
          }

        }
      }

      CSVWriter csvOutWriter = new CSVWriter(outWriter);

      if (!fileExists) {
        List<String> fields = new ArrayList<>();
        fields.add(TRANSACTION_ID);
        fields.add(TRANSACTION_TYPE);
        fields.add(TRANSACTION_ITEM_ID);
        fields.add(TRANSACTION_DATE);
        fields.add(TRANSACTION_DATE_TIME);
        fields.add(USER_NAME);
        fields.add(SOURCE_APPLICATION);
        fields.add(SOURCE_STORE_NAME);
        fields.add(SOURCE_STORE_CODE);
        fields.add(SOURCE_CENTER_NAME);
        fields.add(SOURCE_CENTER_CODE);
        fields.add(DEPARTMENT_NAME);
        fields.add(MRN);
        fields.add(VISIT_ID);
        fields.add(ITEM_INTERFACE_CODE);
        fields.add(ITEM_NAME);
        fields.add(BATCH_NUMBER);
        fields.add(BATCH_EXPIRY_DATE);
        fields.add(ITEM_COST);
        fields.add(TRANSACTION_QUANTITY);
        fields.add(TRANSACTION_UOM);
        fields.add(PKG_SIZE);
        fields.add(DISCOUNT);
        fields.add(DISCOUNT_PERCENT);
        fields.add(TAX_CODE);
        fields.add(TAX_AMOUNT);
        fields.add(MRP);
        fields.add(AMOUNT);
        fields.add(ADJUSTMENT_TYPE);
        fields.add(DESTINATION_STORE_NAME);
        fields.add(DESTINATION_STORE_CODE);
        fields.add(DESTINATION_CENTER_NAME);
        fields.add(DESTINATION_CENTER_CODE);
        fields.add(BILL_DISCOUNT);
        fields.add(ROUND_OFF);
        fields.add(REMARKS);

        csvOutWriter.writeNext(fields.toArray(new String[0]));
      }

      for (Map<String, Object> data : eventData) {
        List<String> values = new ArrayList<>();
        if (data.get(TRANSACTION_ID) != null) {
          values.add(data.get(TRANSACTION_ID).toString());
        } else {
          values.add("");
        }
        if (data.get(TRANSACTION_TYPE) != null) {
          values.add(data.get(TRANSACTION_TYPE).toString());
        } else {
          values.add("");
        }
        if (data.get(TRANSACTION_ITEM_ID) != null) {
          values.add(data.get(TRANSACTION_ITEM_ID).toString());
        } else {
          values.add("");
        }
        if (data.get(TRANSACTION_DATE) != null) {
          values.add(data.get(TRANSACTION_DATE).toString());
        } else {
          values.add("");
        }
        if (data.get(TRANSACTION_DATE_TIME) != null) {
          values.add(data.get(TRANSACTION_DATE_TIME).toString());
        } else {
          values.add("");
        }
        if (data.get(USER_NAME) != null) {
          values.add(data.get(USER_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get(SOURCE_APPLICATION) != null) {
          values.add(data.get(SOURCE_APPLICATION).toString());
        } else {
          values.add("");
        }
        if (data.get(SOURCE_STORE_NAME) != null) {
          values.add(data.get(SOURCE_STORE_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get(SOURCE_STORE_CODE) != null) {
          values.add(data.get(SOURCE_STORE_CODE).toString());
        } else {
          values.add("");
        }
        if (data.get(SOURCE_CENTER_NAME) != null) {
          values.add(data.get(SOURCE_CENTER_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get(SOURCE_CENTER_CODE) != null) {
          values.add(data.get(SOURCE_CENTER_CODE).toString());
        } else {
          values.add("");
        }
        if (data.get(DEPARTMENT_NAME) != null) {
          values.add(data.get(DEPARTMENT_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get("MRN") != null) {
          values.add(data.get("MRN").toString());
        } else {
          values.add("");
        }
        if (data.get(VISIT_ID) != null) {
          values.add(data.get(VISIT_ID).toString());
        } else {
          values.add("");
        }
        if (data.get(ITEM_INTERFACE_CODE) != null) {
          values.add(data.get(ITEM_INTERFACE_CODE).toString());
        } else {
          values.add("");
        }
        if (data.get(ITEM_NAME) != null) {
          values.add(data.get(ITEM_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get(BATCH_NUMBER) != null) {
          values.add(data.get(BATCH_NUMBER).toString());
        } else {
          values.add("");
        }
        if (data.get(BATCH_EXPIRY_DATE) != null) {
          values.add(data.get(BATCH_EXPIRY_DATE).toString());
        } else {
          values.add("");
        }
        if (data.get(ITEM_COST) != null) {
          values.add(data.get(ITEM_COST).toString());
        } else {
          values.add("");
        }
        if (data.get(TRANSACTION_QUANTITY) != null) {
          values.add(data.get(TRANSACTION_QUANTITY).toString());
        } else {
          values.add("");
        }
        if (data.get(TRANSACTION_UOM) != null) {
          values.add(data.get(TRANSACTION_UOM).toString());
        } else {
          values.add("");
        }
        if (data.get(PKG_SIZE) != null) {
          values.add(data.get(PKG_SIZE).toString());
        } else {
          values.add("");
        }
        if (data.get(DISCOUNT) != null) {
          values.add(data.get(DISCOUNT).toString());
        } else {
          values.add("");
        }
        if (data.get(DISCOUNT_PERCENT) != null) {
          values.add(data.get(DISCOUNT_PERCENT).toString());
        } else {
          values.add("");
        }
        if (data.get(TAX_CODE) != null) {
          values.add(data.get(TAX_CODE).toString());
        } else {
          values.add("");
        }
        if (data.get(TAX_AMOUNT) != null) {
          values.add(data.get(TAX_AMOUNT).toString());
        } else {
          values.add("");
        }
        if (data.get(MRP) != null) {
          values.add(data.get(MRP).toString());
        } else {
          values.add("");
        }
        if (data.get(AMOUNT) != null) {
          values.add(data.get(AMOUNT).toString());
        } else {
          values.add("");
        }
        if (data.get(ADJUSTMENT_TYPE) != null) {
          values.add(data.get(ADJUSTMENT_TYPE).toString());
        } else {
          values.add("");
        }
        if (data.get(DESTINATION_STORE_NAME) != null) {
          values.add(data.get(DESTINATION_STORE_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get(DESTINATION_STORE_CODE) != null) {
          values.add(data.get(DESTINATION_STORE_CODE).toString());
        } else {
          values.add("");
        }
        if (data.get(DESTINATION_CENTER_NAME) != null) {
          values.add(data.get(DESTINATION_CENTER_NAME).toString());
        } else {
          values.add("");
        }
        if (data.get(DESTINATION_CENTER_CODE) != null) {
          values.add(data.get(DESTINATION_CENTER_CODE).toString());
        } else {
          values.add("");
        }
        if (data.get(BILL_DISCOUNT) != null) {
          values.add(data.get(BILL_DISCOUNT).toString());
        } else {
          values.add("");
        }
        if (data.get(ROUND_OFF) != null) {
          values.add(data.get(ROUND_OFF).toString());
        } else {
          values.add("");
        }
        if (data.get(REMARKS) != null) {
          values.add(data.get(REMARKS).toString());
        } else {
          values.add("");
        }

        csvOutWriter.writeNext(values.toArray(new String[0]));
      }

      try {
        csvOutWriter.flush();
        csvOutWriter.close();
        outWriter.close();
      } catch (IOException ex) {
        logger.error("Error closing csvWriter: ", ex);
      } finally {
        logger.debug("method executeInternal completed");
      }

    } catch (URISyntaxException | IOException ex) {
      logger.error("Something went wrong", ex);
    } finally {
      if (fileSystemManager != null) {
        fileSystemManager.close();
      }

      if (lock != null) {
        try {
          lock.release();
        } catch (IOException ex) {
          logger.error("Can't release lock", ex);
        }
      }

      if (randomAccessFile != null) {
        try {
          randomAccessFile.close();
        } catch (IOException ex) {
          logger.error("Can't close raf", ex);
        }
      }
    }

  }

  private URI makePath(URI uri, String... pathSegments) throws URISyntaxException {
    StringBuilder uriStringBuilder = new StringBuilder(uri.toString());
    for (String pathSegment : pathSegments) {
      uriStringBuilder.append("/").append(pathSegment);
    }
    return new URI(uriStringBuilder.toString());
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public List<Map<String, Object>> getEventData() {
    return eventData;
  }

  public void setEventData(List<Map<String, Object>> eventData) {
    this.eventData = eventData;
  }

  public int getTtl() {
    return ttl;
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }


}
