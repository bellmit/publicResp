package com.insta.hms.batchjob;

import au.com.bytecode.opencsv.CSVWriter;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.utils.EnvironmentUtil;
import com.insta.hms.integration.scm.inventory.ScmItemMasterOutBoundService;
import com.insta.hms.jobs.GenericJob;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.http.conn.UnsupportedSchemeException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//TODO Merge this with CsvExportJob
@Component
public class CsvItemsExportJob extends GenericJob {

  private static final Logger logger = LoggerFactory.getLogger(CsvItemsExportJob.class);
  private static final String[] SUPPORTED_SCHEMES = new String[] {"sftp", "file"};
  private static final String ENTITY_NAME = "item_master";

  public static final String TRANSACTION_ID = "TRANSACTION_ID";
  public static final String TRANSACTION_TYPE = "TRANSACTION_TYPE";
  public static final String MEDICINE_NAME = "MEDICINE_NAME";
  public static final String MEDICINE_SHORT_NAME = "MEDICINE_SHORT_NAME";
  public static final String CUST_ITEM_CODE = "CUST_ITEM_CODE";
  public static final String MED_CATEGORY_ID = "MED_CATEGORY_ID";
  public static final String MED_CATEGORY_INTEGRATION_ID = "MED_CATEGORY_INTEGRATION_ID";
  public static final String ITEM_CATEGORY_NAME = "ITEM_CATEGORY_NAME";
  public static final String SERVICE_SUB_GROUP_ID = "SERVICE_SUB_GROUP_ID";
  public static final String SERVICE_SUB_GROUP_INTEGRATION_ID = "SERVICE_SUB_GROUP_INTEGRATION_ID";
  public static final String SERVICE_SUB_GROUP_NAME = "SERVICE_SUB_GROUP_NAME";
  public static final String SERVICE_GROUP_NAME = "SERVICE_GROUP_NAME";
  public static final String SERVICE_GROUP_CODE = "SERVICE_GROUP_CODE";
  public static final String SERVICE_GROUP_INTEGRATION_ID = "SERVICE_GROUP_INTEGRATION_ID";
  public static final String PACKAGE_UOM = "PACKAGE_UOM";
  public static final String ISSUE_UOM = "ISSUE_UOM";
  public static final String PACKAGE_SIZE = "PACKAGE_SIZE";
  public static final String INTEGRATION_UOM_ID = "INTEGRATION_UOM_ID";
  public static final String BATCH_NO_APPLICABLE = "BATCH_NO_APPLICABLE";
  public static final String ITEM_BARCODE_ID = "ITEM_BARCODE_ID";
  public static final String MAX_COST_PRICE = "MAX_COST_PRICE";
  public static final String ITEM_SELLING_PRICE = "ITEM_SELLING_PRICE";
  public static final String STATUS = "STATUS";
  public static final String VALUE = "VALUE";
  public static final String HIGH_COST_CONSUMABLE = "HIGH_COST_CONSUMABLE";
  public static final String TAX_TYPE = "TAX_TYPE";
  public static final String TAX_SUB_GROUPS = "TAX_SUB_GROUPS";
  public static final String TAX_RATE = "TAX_RATE";
  public static final String BIN = "BIN";
  public static final String CREATED_DATE_TIME = "CREATED_DATE_TIME";
  public static final String LAST_UPDATED_DATE_TIME = "LAST_UPDATED_DATE_TIME";
  public static final String HEALTH_AUTORITY_CODE = "HEALTH_AUTORITY_CODE";
  public static final String HEALTH_AUTORITY_CODE_TYPE = "HEALTH_AUTORITY_CODE_TYPE";
  public static final String HEALTH_AUTORITY_DRUG_CODE = "HEALTH_AUTORITY_DRUG_CODE";
  public static final String ITEM_FORM_ID = "ITEM_FORM_ID";
  public static final String UOM_ID = "UOM_ID";
  public static final String ITEM_FORM_NAME = "ITEM_FORM_NAME";
  public static final String INTEGRATION_FORM_ID = "INTEGRATION_FORM_ID";
  public static final String MANF_CODE = "MANF_CODE";
  public static final String MANF_NAME = "MANF_NAME";
  public static final String MANF_MNEMONIC = "MANF_MNEMONIC";
  public static final String INTEGRATION_MANF_ID = "INTEGRATION_MANF_ID";
  public static final String GENERIC_CODE = "GENERIC_CODE";
  public static final String GENERIC_NAME = "GENERIC_NAME";
  public static final String INTEGRATION_GENERIC_ID = "INTEGRATION_GENERIC_ID";
  public static final String FIELD_STRENGTH_UNIT_ID = "STRENGTH_UNIT_ID";
  public static final String FIELD_INTEGRATION_STRENGTH_UNIT_ID = "INTEGRATION_STRENGTH_UNIT_ID";
  public static final String FIELD_UNIT_NAME = "UNIT_NAME";
  public static final String FIELD_ITEM_STRENGTH = "ITEM_STRENGTH";

  private String eventId;
  private List<Map<String, Object>> eventData;

  private static final String[] COLUMN_SEQUENCE = new String[] {
      TRANSACTION_ID,
      TRANSACTION_TYPE,
      MEDICINE_NAME,
      MEDICINE_SHORT_NAME,
      CUST_ITEM_CODE,
      MED_CATEGORY_ID,
      MED_CATEGORY_INTEGRATION_ID,
      ITEM_CATEGORY_NAME,
      SERVICE_SUB_GROUP_ID,
      SERVICE_SUB_GROUP_INTEGRATION_ID,
      SERVICE_SUB_GROUP_NAME,
      SERVICE_GROUP_NAME,
      SERVICE_GROUP_CODE,
      SERVICE_GROUP_INTEGRATION_ID,
      PACKAGE_UOM,
      ISSUE_UOM,
      PACKAGE_SIZE,
      INTEGRATION_UOM_ID,
      BATCH_NO_APPLICABLE,
      ITEM_BARCODE_ID,
      MAX_COST_PRICE,
      ITEM_SELLING_PRICE,
      STATUS,
      VALUE,
      HIGH_COST_CONSUMABLE,
      TAX_TYPE,
      TAX_SUB_GROUPS,
      TAX_RATE,
      BIN,
      CREATED_DATE_TIME,
      LAST_UPDATED_DATE_TIME,
      HEALTH_AUTORITY_CODE,
      HEALTH_AUTORITY_CODE_TYPE,
      HEALTH_AUTORITY_DRUG_CODE,
      UOM_ID,
      ITEM_FORM_ID,
      ITEM_FORM_NAME,
      INTEGRATION_FORM_ID,
      MANF_CODE,
      MANF_NAME,
      MANF_MNEMONIC,
      INTEGRATION_MANF_ID,
      GENERIC_CODE,
      GENERIC_NAME,
      INTEGRATION_GENERIC_ID,
      FIELD_INTEGRATION_STRENGTH_UNIT_ID,
      FIELD_STRENGTH_UNIT_ID,
      FIELD_UNIT_NAME,
      FIELD_ITEM_STRENGTH
  };

  @LazyAutowired
  private ScmItemMasterOutBoundService itemTransaction;

  @Override
  protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
    StandardFileSystemManager fileSystemManager = null;
    try {
      if (EnvironmentUtil.getCsvExportUri() == null
          || EnvironmentUtil.getCsvExportUri().trim().isEmpty()) {
        logger.error(EnvironmentUtil.URI_EXPORT_CSVS + " not defined. Exiting job");
        return;
      }
      URI csvExportUri = new URI(
          EnvironmentUtil.getCsvExportUri() + "/" + getSchema() + "/" + ENTITY_NAME + "/out");
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
        if (file.isFile() && file.getName().getBaseName()
            .matches(ENTITY_NAME + "_\\d{4}-\\d{2}-\\d{2}T\\d{6}\\.csv")) {
          fileExists = true;
          fileName = file.getName().getBaseName();
        }
      }

      BufferedWriter outWriter = null;
      if (fileExists) {
        outWriter = new BufferedWriter(new OutputStreamWriter(fileSystemManager
            .resolveFile(makePath(csvExportUri, fileName)).getContent().getOutputStream(true)));
      } else {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HHmmss");
        Date date = new Date();
        outWriter = new BufferedWriter(
            new OutputStreamWriter(
                fileSystemManager
                    .resolveFile(makePath(csvExportUri,
                        ENTITY_NAME + "_" + dateFormat.format(date) + ".csv"))
                    .getContent().getOutputStream()));
      }

      CSVWriter csvOutWriter = new CSVWriter(outWriter);

      if (!fileExists) {
        csvOutWriter.writeNext(COLUMN_SEQUENCE);
      }

      for (Map<String, Object> data : eventData) {
        List<String> values = new ArrayList<>();
        for (String field : COLUMN_SEQUENCE) {
          if (data.get(field) != null) {
            values.add(data.get(field).toString());
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

    } catch (URISyntaxException | UnsupportedSchemeException | FileSystemException ex) {
      logger.error("Something went wrong", ex);
    } finally {
      if (fileSystemManager != null) {
        fileSystemManager.close();
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

}
