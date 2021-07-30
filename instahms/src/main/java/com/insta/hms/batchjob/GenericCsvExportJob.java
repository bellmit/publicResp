package com.insta.hms.batchjob;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import au.com.bytecode.opencsv.CSVWriter;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.integration.scm.CsvContext;
import com.insta.hms.jobs.GenericJob;
import com.insta.hms.jobs.JobService;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.http.conn.UnsupportedSchemeException;
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


//TODO rename to CsvExportJob
@Component
public class GenericCsvExportJob extends GenericJob {
  protected static final String URI_PATH_SEPERATOR = "/";
  protected static final String SCHEME_SFTP = "sftp";
  protected static final String SCHEME_FILE = "file";
  protected static final String[] SUPPORTED_SCHEMES = new String[] {SCHEME_SFTP, SCHEME_FILE};

  private String eventId;
  private int ttl;
  private List<Map<String, Object>> eventData;
  private CsvContext context;

  @LazyAutowired
  JobService jobService;

  private static final Logger logger = LoggerFactory.getLogger(GenericCsvExportJob.class);

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public int getTtl() {
    return ttl;
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  public List<Map<String, Object>> getEventData() {
    return eventData;
  }

  public void setEventData(List<Map<String, Object>> eventData) {
    this.eventData = eventData;
  }

  public CsvContext getContext() {
    return context;
  }

  public void setContext(CsvContext context) {
    this.context = context;
  }


  private FileLock tryLock(RandomAccessFile randomAccessFile) throws IOException {
    FileChannel fileChannel = randomAccessFile.getChannel();
    return fileChannel.tryLock();
  }

  @Override
  //TODO Check synchronized behavior for inv transactions and implement likewise
  //Maybe we can use synchronized block
  protected synchronized void executeInternal(JobExecutionContext jobExecutionContext)
      throws JobExecutionException {
    {
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
        URI csvExportUri =
            new URI(EnvironmentUtil.getCsvExportUri() + URI_PATH_SEPERATOR + getSchema().trim()
                +
                URI_PATH_SEPERATOR + context.getEntityName() + "/out");
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
          if (file.isFile() && file.getName().getBaseName().matches(context.getEntityName()
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
              context.getEntityName() + "_" + dateFormat.format(date) + ".csv");
          outWriter = new BufferedWriter(new OutputStreamWriter(fileSystemManager
              .resolveFile(newFileUri)
              .getContent().getOutputStream()));
          if (uriScheme.equals(SCHEME_FILE)) {
            randomAccessFile = new RandomAccessFile(new File(newFileUri), "rw");
            lock = tryLock(randomAccessFile);

            if (lock == null) {

              logger.error(
                  "Acquiring lock on new file failed. event " + getEventId() + " TTL = "
                      + getTtl());
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
          csvOutWriter.writeNext(context.getColumns());
        }

        for (Map<String, Object> data : eventData) {
          List<String> values = new ArrayList<>();

          for (String column : context.getColumns()) {
            if (data.get(column) != null) {
              values.add(data.get(column).toString());
            } else {
              values.add("");
            }
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
  }


  private URI makePath(URI uri, String... pathSegments) throws URISyntaxException {
    StringBuilder uriStringBuilder = new StringBuilder(uri.toString());
    for (String pathSegment : pathSegments) {
      uriStringBuilder.append("/").append(pathSegment);
    }
    return new URI(uriStringBuilder.toString());
  }


  private Map<String, Object> buildJobData() {
    Map<String, Object> jobData = new HashMap<String, Object>();
    jobData.put("schema", getSchema());
    jobData.put("eventData", eventData);
    jobData.put("eventId", getEventId());
    jobData.put("ttl", getTtl() - 1);
    return jobData;
  }
}
