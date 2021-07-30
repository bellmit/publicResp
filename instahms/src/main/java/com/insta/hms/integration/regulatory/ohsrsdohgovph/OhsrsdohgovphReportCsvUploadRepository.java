package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OhsrsdohgovphReportCsvUploadRepository extends GenericRepository {

  public static final String COLUMN_CENTERID = "center_id";
  public static final String COLUMN_REPORTING_YEAR = "reporting_year";
  public static final String COLUMN_OHSRS_FUNCTION = "ohsrs_function";
  public static final String COLUMN_CONTENT = "content";
  private static final String COLUMN_REUPLOADED_BY = "reuploaded_by";
  private static final String COLUMN_UPLOADED_BY = "uploaded_by";
  private static final String COLUMN_REUPLOADED_AT = "reuploaded_at";
  private static final String COLUMN_UPLOADED_AT = "uploaded_at";
  public static final String COLUMN_PROCESSED = "processed";
  public static final String COLUMN_ID = "id";
  private static final String COLUMN_STATUS = "status";
  
  public OhsrsdohgovphReportCsvUploadRepository() {
    super("ohsrsdohgovph_report_csv_upload");
  }

  /**
   * Update an existing or create a new row for uploaded CSV file in DB based on center id, year, 
   * ohsrs function combination.
   * @param centerId      Center ID
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @param content       Uploaded CSV File Content
   * @param user          User performing upload
   * @return count of rows affected by db update/insert operation
   */
  public Integer upsertCsvFile(int centerId, int year, String ohsrsFunction, 
      InputStream content, String user) {
    
    BasicDynaBean bean = getCsvUpload(centerId, year, ohsrsFunction);
    Timestamp now = DateUtil.getCurrentTimestamp();
    if (bean == null) {
      bean = getBean();
      bean.set(COLUMN_REPORTING_YEAR, year);
      bean.set(COLUMN_CENTERID, centerId);
      bean.set(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
      bean.set(COLUMN_UPLOADED_BY, user);
      bean.set(COLUMN_UPLOADED_AT, now);
    }
    bean.set(COLUMN_CONTENT, content);
    bean.set(COLUMN_REUPLOADED_BY, user);
    bean.set(COLUMN_REUPLOADED_AT, now);
    bean.set(COLUMN_PROCESSED, false);
    bean.set(COLUMN_STATUS, "A");
    boolean status;
    if (bean.get(COLUMN_ID) == null) {
      status = insert(bean) > 0;
    } else {
      Map<String,Object> keys = new HashMap<>();
      keys.put(COLUMN_ID, bean.get(COLUMN_ID));
      status = update(bean, keys) > 0;
    }
    return status ? ((Integer) (getCsvUpload(centerId, year, ohsrsFunction).get(COLUMN_ID))) : null;
  }
  
  /**
   * Get an uploaded csv file row from DB.
   * @param centerId      Center ID
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @return BasicDynaBean containing information from DB
   */
  private BasicDynaBean getCsvUpload(int centerId, int year, String ohsrsFunction) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    filters.put(COLUMN_STATUS, "A");
    List<BasicDynaBean> csvUpload = findByCriteria(filters);
    if (csvUpload != null && !csvUpload.isEmpty()) {
      return csvUpload.get(0);
    }
    return null;
  }

  /**
   * Get all unprocessed uploaded csv file row from DB.
   * @param centerId      Center ID
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @return List of BasicDynaBean containing information from DB
   */
  public List<BasicDynaBean> getUnprocessedCsvUpload(int centerId, int year, String ohsrsFunction) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    filters.put(COLUMN_STATUS, "A");
    filters.put(COLUMN_PROCESSED, false);
    return findByCriteria(filters);
  }

  /**
   * Mark a unprocessed CSV row as processed.
   * @param bean Bean containing unprocessed CSV row information
   */
  public void markAsProcessed(BasicDynaBean bean) {
    BasicDynaBean updateBean = getBean();
    updateBean.set(COLUMN_PROCESSED, true);
    Map<String,Object> keys = new HashMap<>();
    keys.put(COLUMN_ID, bean.get(COLUMN_ID));
    update(updateBean, keys);    
  }
}
