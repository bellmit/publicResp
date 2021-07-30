package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OhsrsdohgovphReportStatusRepository extends GenericRepository {

  private static final String COLUMN_ID = "id";
  public static final String COLUMN_CENTERID = "center_id";
  public static final String COLUMN_REPORTING_YEAR = "reporting_year";
  public static final String COLUMN_OHSRS_FUNCTION = "ohsrs_function";
  private static final String COLUMN_CREATED_AT = "created_at";
  private static final String COLUMN_CREATED_BY = "created_by";
  private static final String COLUMN_MODIFIED_BY = "modified_by";
  private static final String COLUMN_MODIFIED_AT = "modified_at";
  public static final String COLUMN_STATUS = "status";
  public static final String COLUMN_DETAILS = "details";

  public static final String STATUS_UNPROCESSED = "unprocessed";
  public static final String STATUS_GENERATION_FAILED = "generation_failed";

  public static final List<String> DISALLOWED_STATUS_FOR_SUBMIT = Arrays.asList(STATUS_UNPROCESSED, 
      STATUS_GENERATION_FAILED);
  
  public static final String STATUS_GENERATED = "generated";
  public static final String STATUS_SUBMISSION_FAILED = "submission_failed";
  public static final String STATUS_SIGNOFF_COMPLETED = "signoff_completed";

  public static final List<String> DISALLOWED_STATUS_FOR_SIGNOFF = Arrays.asList(
      STATUS_GENERATED, STATUS_SUBMISSION_FAILED, STATUS_SIGNOFF_COMPLETED);

  public static final String STATUS_SUBMITTED = "submitted";
  public static final String STATUS_SIGNOFF_FAILED = "signoff_failed";
  
  public static final List<String> SUBMIT_PENDING = Arrays.asList(STATUS_GENERATED, 
      STATUS_SUBMISSION_FAILED);


  public OhsrsdohgovphReportStatusRepository() {
    super("ohsrsdohgovph_report_status");
  }

  /**
   * Get report data for given ohsrs function.
   * @param centerId      Id of Center for which report status is required
   * @param year          Reporting year
   * @param ohsrsFunction Ohsrs Function for which status is to be fetched
   * @return Basicdynabean
   */
  public BasicDynaBean getReportStatus(int centerId, int year, String ohsrsFunction) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    BasicDynaBean bean = findByKey(filters);
    if (bean == null) {
      bean = getBean();
      bean.set(COLUMN_CENTERID, centerId);
      bean.set(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
      bean.set(COLUMN_REPORTING_YEAR, year);
      bean.set(COLUMN_STATUS, STATUS_UNPROCESSED);
      bean.set(COLUMN_DETAILS, "");
    }
    return bean;
  }
  
  /**
   * Create new bean for given data.
   * @param centerId      Center Id
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @param status        Status of report section
   * @param details       Additional error or response details
   * @param user          User creating this data
   * @return New bean for report data
   */
  public BasicDynaBean newBean(int centerId, int year, String ohsrsFunction, 
      String status, String details,String user) {
    
    Timestamp now = DateUtil.getCurrentTimestamp();
    BasicDynaBean bean = getBean();
    bean.set(COLUMN_CREATED_AT, now);
    bean.set(COLUMN_CREATED_BY, user);
    bean.set(COLUMN_MODIFIED_AT, now);
    bean.set(COLUMN_MODIFIED_BY, user);
    bean.set(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    bean.set(COLUMN_REPORTING_YEAR, year);
    bean.set(COLUMN_CENTERID, centerId);
    bean.set(COLUMN_STATUS, status);
    bean.set(COLUMN_DETAILS, details);
    return bean;
  }

  /**
   * Update and existing bean with new value and modified logging fields.
   * @param bean Bean containing existing dataset 
   * @param status        Status of report section
   * @param details       Additional error or response details
   * @param user user performing update
   */
  public void updateBean(BasicDynaBean bean, String status, String details, String user) {
    Timestamp now = DateUtil.getCurrentTimestamp();
    bean.set(COLUMN_STATUS, status);
    bean.set(COLUMN_DETAILS, details);
    bean.set(COLUMN_MODIFIED_AT, now);
    bean.set(COLUMN_MODIFIED_BY, user);
  }
  
  /**
   * Update an existing or create a new row for uploaded CSV file in DB based on center id, year, 
   * ohsrs function combination.
   * @param centerId      Center ID
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @param status        Status of report section
   * @param details       Additional error or response details
   * @param user          User performing upload
   * @return count of rows affected by db update/insert operation
   */
  public boolean upsertStatus(int centerId, int year, String ohsrsFunction, 
      String status, String details, String user) {
    
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    BasicDynaBean bean = findByKey(filters);
    Timestamp now = DateUtil.getCurrentTimestamp();
    if (bean == null) {
      bean = newBean(centerId, year, ohsrsFunction, status, details, user);
    } else {
      updateBean(bean, status, details, user);
    }    
    boolean opStatus = false;
    if (bean.get(COLUMN_ID) == null) {
      opStatus = insert(bean) > 0;
    } else {
      Map<String,Object> keys = new HashMap<>();
      keys.put(COLUMN_ID, bean.get(COLUMN_ID));
      opStatus = update(bean, keys) > 0;
    }
    return opStatus;
  }

}
