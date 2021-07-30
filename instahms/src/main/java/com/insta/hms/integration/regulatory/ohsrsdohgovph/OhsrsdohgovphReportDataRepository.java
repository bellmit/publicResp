package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OhsrsdohgovphReportDataRepository extends GenericRepository {

  public static final String COLUMN_CENTERID = "center_id";
  public static final String COLUMN_REPORTING_YEAR = "reporting_year";
  public static final String COLUMN_OHSRS_FUNCTION = "ohsrs_function";
  private static final String COLUMN_CREATED_AT = "created_at";
  private static final String COLUMN_CREATED_BY = "created_by";
  private static final String COLUMN_MODIFIED_BY = "modified_by";
  private static final String COLUMN_MODIFIED_AT = "modified_at";
  public static final String COLUMN_UPLOAD = "upload";
  public static final String COLUMN_TABLE_INDEX = "table_index";
  public static final String COLUMN_FIELD = "field";
  public static final String COLUMN_VALUE = "value";
  public static final String COLUMN_ID = "id";
  private static final String COLUMN_STATUS = "status";
  public static final String COLUMN_DESCRIPTION = "description";
  private static final String OHSRS_FUNCTION_TABLE_INDEX_SEQ = 
      "ohsrsdohgovph_report_data_table_index";
  
  private static final String GET_REPORT_BY_OHSRS_FUNCTION = "SELECT r.*, rm.description"
      + " FROM ohsrsdohgovph_report_data r"
      + " LEFT JOIN ohsrsdohgovph_meta_data rm"
      + "   ON r.ohsrs_function = rm.ohsrs_function"
      + "   AND r.field = rm.field"
      + "   AND r.value = rm.value"
      + " WHERE"
      + "   r.center_id = ?"
      + "   AND r.reporting_year = ?"
      + "   AND r.ohsrs_function = ?"
      + "   AND r.status = 'A'"
      + " ORDER BY r.table_index;";
  
  public OhsrsdohgovphReportDataRepository() {
    super("ohsrsdohgovph_report_data");
  }

  /**
   * Get report data for given center and year.
   * @param centerId      Id of Center for which report data is required
   * @param year          Reporting year
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getReport(int centerId, int year) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_STATUS, "A");
    return findByCriteria(filters, COLUMN_OHSRS_FUNCTION);
  }

  /**
   * Get report data for given ohsrs function, center and year.
   * @param centerId      Id of Center for which report data is required
   * @param year          Reporting year
   * @param ohsrsFunction Ohsrs Function for which data is to be fetched
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getReport(int centerId, int year, String ohsrsFunction) {
    return DatabaseHelper.queryToDynaList(
        GET_REPORT_BY_OHSRS_FUNCTION,
        new Object[] {centerId, year, ohsrsFunction});
  }

  /**
   * Get report data for given ohsrs function.
   * @param centerId      Id of Center for which report data is required
   * @param year          Reporting year
   * @param ohsrsFunction Ohsrs Function for which data is to be fetched
   * @return List of Basicdynabean
   */
  public BasicDynaBean getReportFieldData(int centerId, int year, String ohsrsFunction, 
      String field, boolean upload) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    filters.put(COLUMN_FIELD, field);
    filters.put(COLUMN_UPLOAD, upload);
    filters.put(COLUMN_STATUS, "A");
    return findByKey(filters);
  }
  
  /**
   * Get table index from DB or allocate a new table index from sequence.
   * @param centerId      Id of Center for which report data is required
   * @param year          Reporting year
   * @param ohsrsFunction Ohsrs Function for which data is to be fetched
   * @param field         Field to be used for determination of table index for multirow data
   * @param value         Value of field to be used for determination of table index
   * @return table index
   */
  private long getTableIndex(int centerId, int year, String ohsrsFunction, String field, 
      String value) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    filters.put(COLUMN_FIELD, field);
    filters.put(COLUMN_VALUE, value);
    filters.put(COLUMN_STATUS, "A");
    BasicDynaBean bean = findByKey(filters);
    if (bean != null) {
      return (Long) bean.get(COLUMN_TABLE_INDEX);
    } else {
      return DatabaseHelper.getNextSequence(OHSRS_FUNCTION_TABLE_INDEX_SEQ).longValue();
    }
  }

  /**
   * Create new bean for given data.
   * @param value         Value for the field.
   * @param centerId      Center Id
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @param field         Field in OHSRSS Function
   * @param upload        Set to true if field is getting updated via CSV Upload
   * @param user          User creating this data
   * @param groupByField  Field to be used for determination of table index for multirow data
   * @param groupByValue  Value of field to be used for determination of table index
   * @param tableIndexMap Existing assigned or extracted table indexes
   * @return New bean for report data
   */
  public BasicDynaBean newBean(String value, int centerId, int year, String ohsrsFunction, 
      String field, boolean upload, String user, String groupByField, String groupByValue,
      Map<String, Long> tableIndexMap) {
    
    Timestamp now = DateUtil.getCurrentTimestamp();
    BasicDynaBean bean = getBean();
    bean.set(COLUMN_CREATED_AT, now);
    bean.set(COLUMN_CREATED_BY, user);
    bean.set(COLUMN_MODIFIED_AT, now);
    bean.set(COLUMN_MODIFIED_BY, user);
    bean.set(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    bean.set(COLUMN_REPORTING_YEAR, year);
    bean.set(COLUMN_CENTERID, centerId);
    bean.set(COLUMN_FIELD, field);
    bean.set(COLUMN_UPLOAD, upload);
    bean.set(COLUMN_VALUE, value);
    if (groupByField != null) {
      if (!tableIndexMap.containsKey(groupByValue)) {
        tableIndexMap.put(groupByValue, 
            getTableIndex(centerId, year, ohsrsFunction, groupByField, groupByValue));
      }
      bean.set(COLUMN_TABLE_INDEX, tableIndexMap.get(groupByValue));
    }
    bean.set(COLUMN_STATUS, "A");
    return bean;
  }

  /**
   * Update and existing bean with new value and modified logging fields.
   * @param bean Bean containing existing dataset 
   * @param user user performing update
   * @param value new value for the field
   */
  public void updateBean(BasicDynaBean bean, String user, String value) {
    Timestamp now = DateUtil.getCurrentTimestamp();
    bean.set(COLUMN_VALUE, value);
    bean.set(COLUMN_MODIFIED_AT, now);
    bean.set(COLUMN_MODIFIED_BY, user);
  }

  /**
   * Delete existing reporting data for given section and upload state.
   * @param centerId      Center Id
   * @param year          Reporting Year
   * @param ohsrsFunction OHSRS Function
   * @param upload        Set to true if field is getting updated via CSV Upload
   */
  public void flushRecords(int year, int centerId, String ohsrsFunction, boolean upload) {
    Map<String,Object> filters = new HashMap<>();
    filters.put(COLUMN_CENTERID, centerId);
    filters.put(COLUMN_REPORTING_YEAR, year);
    filters.put(COLUMN_OHSRS_FUNCTION, ohsrsFunction);
    filters.put(COLUMN_UPLOAD, upload);
    filters.put(COLUMN_STATUS, "A");
    delete(filters);
  }
}
