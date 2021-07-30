package com.insta.hms.integration.insurance.remittance;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.batchjob.SQLUpdateJob;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesCache;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RemittanceAutoProcessJob extends SQLUpdateJob {

  /**
   * The logger.
   */
  static Logger logger = LoggerFactory.getLogger(RemittanceAutoProcessJob.class);

  /**
   * The remittance service.
   */
  @Autowired
  private RemittanceService remittanceService;

  /**
   * Implementation to auto process RA files.
   *
   * @param jobContext jobContext
   * @throws JobExecutionException JobExecutionException
   */
  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    logger.info("RemittanceAutoProcessJob.autoProcess method invoked.");
    try {
      String schema = (String) jobContext.getJobDetail().getJobDataMap().get("schema");
      List<BasicDynaBean> centers = CenterMasterDAO.getAllCenters();
      for (BasicDynaBean dynaBean : centers) {
        Integer centerId = (Integer) dynaBean.get("center_id");
        Connection con = DataBaseUtil.getConnection();
        GenericPreferencesDTO dto = GenericPreferencesCache.CACHEDPREFERENCESDTO.get(schema);
        if (dto == null || GenericPreferencesCache.CACHEDPREFERENCESDTO.isEmpty()) {
          dto = GenericPreferencesDAO.getGenericPreferencesFromDB(con);
          GenericPreferencesCache.CACHEDPREFERENCESDTO.put(schema, dto);
        }
        Map<String, String[]> listingParameters = getListingParameters(
            dto.getRaAutoProcessLastNumberOfDays(), 1, "N", "new",
            Boolean.TRUE);
        PagedList raDownloadList;
        logger.info("Started auto processing for center id :" + centerId);
        try {
          raDownloadList = remittanceService.radownloadlist(listingParameters, centerId);
          if (raDownloadList != null && !raDownloadList.getDtoList().isEmpty()) {
            List unprocessedItems = raDownloadList.getDtoList();
            for (Object item : unprocessedItems) {
              Remittance remittance = (Remittance) item;
              Map<String, String[]> parameters = new HashMap<>();
              parameters.put("fileId", new String[]{remittance.getFileId()});
              parameters.put("file_name", new String[]{remittance.getFileName()});
              parameters.put("account_group_id", new String[]{
                  String.valueOf(remittance.getAccountGroupId())});
              parameters.put("tpa_id", new String[]{remittance.getTpaId()});
              parameters.put("status", new String[]{"new"});
              parameters.put("processing_statuses", new String[]{"N"});
              String msg = null;
              try {
                msg = remittanceService.raDownloadProcessFile(parameters, centerId, schema);
              } catch (IOException | ParseException | SQLException | SAXException exception) {
                logger.error("Error while downloading and processing RA files for file :"
                    + remittance.getFileName() + " :  " + msg, exception);
              }
            }
          } else {
            logger.info("No RA files found found.");
          }
        } catch (IOException | SAXException exception) {
          logger.error("Error while getting the RA files list.", exception);
        }
        logger.info("Completed auto processing for center id :" + centerId);
      }
    } catch (SQLException exception) {
      logger.error("Error while getting the active center list.", exception);
      return;
    }
    logger.info("RemittanceAutoProcessJob.autoProcess method exited.");
  }

  /**
   * Method to get the parameters to fetch RA list.
   *
   * @param lastNumberOfDays Last number of days to be taken it account to get the data. <b>Default
   * last 7 days.</b>
   * @param accountGroupId the account group id. <b>Default 1(Hospital)</b>
   * @param processingStatus Can be one of N,S,P,F,C or I. <b>Default N</b>
   * @param status Can be one downloaded or new. <b>Default new</b>
   * @param sortReverse reverse sort flag. <b>Default true</b>
   */
  private Map<String, String[]> getListingParameters(Integer lastNumberOfDays,
      Integer accountGroupId, String processingStatus, String status, Boolean sortReverse) {
    // setting default values
    if (lastNumberOfDays == null) {
      lastNumberOfDays = 7;
    }
    if (accountGroupId == null) {
      accountGroupId = 1;
    }
    if (processingStatus == null) {
      processingStatus = "N";
    }
    if (status == null) {
      status = "new";
    }
    if (sortReverse == null) {
      sortReverse = Boolean.TRUE;
    }
    Map<String, String[]> listingParameters = new HashMap<>();
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -lastNumberOfDays);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    listingParameters.put("received_start_date", new String[]{dateFormat.format(cal.getTime())});
    listingParameters.put("received_end_date", new String[]{dateFormat.format(new Date())});
    listingParameters.put("account_group_id", new String[]{String.valueOf(accountGroupId)});
    listingParameters.put("processing_status", new String[]{processingStatus});
    listingParameters.put("status", new String[]{status});
    listingParameters.put("sortReverse", new String[]{String.valueOf(sortReverse)});
    return listingParameters;
  }
}
