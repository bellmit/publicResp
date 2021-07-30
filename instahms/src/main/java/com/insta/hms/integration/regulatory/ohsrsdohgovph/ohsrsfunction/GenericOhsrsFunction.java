package com.insta.hms.integration.regulatory.ohsrsdohgovph.ohsrsfunction;

import au.com.bytecode.opencsv.CSVReader;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.annotations.OhsrsFunctionProcessor;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsFunctionFieldMeta;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsFunctionMeta;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphIcdLookupDataRepository;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphMetaDataRepository;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphReportCsvUploadRepository;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphReportDataRepository;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphReportStatusRepository;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSettings;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphSurgeryLookupDataRepository;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphUtility;
import com.insta.hms.integration.regulatory.ohsrsdohgovph.OhsrsdohgovphWebserviceClient;
import com.insta.hms.mdm.bulk.CsVModelAndView;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;


public abstract class GenericOhsrsFunction {

  @LazyAutowired
  OhsrsdohgovphReportCsvUploadRepository csvUploadRepo;

  @LazyAutowired
  OhsrsdohgovphIcdLookupDataRepository icdLookupRepo;

  @LazyAutowired
  OhsrsdohgovphSurgeryLookupDataRepository surgeryLookupRepo;

  @LazyAutowired
  OhsrsdohgovphReportDataRepository reportRepo;

  @LazyAutowired
  OhsrsdohgovphReportStatusRepository reportStatusRepo;

  @LazyAutowired
  OhsrsdohgovphMetaDataRepository metaRepo;

  @LazyAutowired
  OhsrsdohgovphUtility utility;

  @LazyAutowired
  CenterService centerService;
  
  @LazyAutowired
  OhsrsdohgovphWebserviceClient client;
  
  private static final String OPERATION_INSERT = "insert";
  private static final String OPERATION_UPDATE = "update";
  private static final String SOURCE_CSV = "csv";
  private static final String SOURCE_DB = "db";
  private static final String SETTINGS_DEPT_MAPPING = "dept_mapping_ohsrsdohgovph";
  private static final String SETTINGS_DIAG_DEPT_MAPPING = "diag_dept_mapping_ohsrsdohgovph";
  private static final String SETTINGS_ICD_MAPPING = "icd_mapping_ohsrsdohgovph";
  private static final String SETTINGS_WARD_MAPPING = "ward_mapping_ohsrsdohgovph";
  
  private static final SimpleDateFormat DATE_PARSER = new DateUtil().getSqlDateFormatter();
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(GenericOhsrsFunction.class);

  public Map<String, Object> getReportMap(int year) {
    return getReportMap(year, utility.getLoggedInCenterId(), true);
  }

  /**
   * get report data as map from DB.
   * @param year Reporting Year
   * @param centerId Center Id
   * @param humanReadable set to true to get Description instead of code for OHSRS predefined 
   *                      datasets
   * @return Map of stored report data.
   */
  public Map<String, Object> getReportMap(int year, int centerId, boolean humanReadable) {
    OhsrsFunctionMeta meta = getMeta();
    BasicDynaBean reportStatus = reportStatusRepo.getReportStatus(centerId, year, meta.getKey());
    Map<String,Object> reportMap = new HashMap<>();
    reportMap.put(SOURCE_CSV, new HashMap<String, Object>());
    reportMap.put(SOURCE_DB, new HashMap<String, Object>());
    reportMap.put(OhsrsdohgovphReportStatusRepository.COLUMN_STATUS,
        reportStatus.get(OhsrsdohgovphReportStatusRepository.COLUMN_STATUS));
    reportMap.put(OhsrsdohgovphReportStatusRepository.COLUMN_DETAILS,
        reportStatus.get(OhsrsdohgovphReportStatusRepository.COLUMN_DETAILS));
    Map<String, OhsrsFunctionFieldMeta> fieldMetaMap = meta.getFieldsAsMap();
    List<BasicDynaBean> reportData = reportRepo.getReport(centerId, year, meta.getKey());
    boolean simpleRepresentation = meta.getRepresentation()
        .equals(OhsrsFunctionMeta.REPRESENTATION_SIMPLE);
    for (BasicDynaBean reportField : reportData) {
      String field = (String) reportField.get(OhsrsdohgovphReportDataRepository.COLUMN_FIELD);
      OhsrsFunctionFieldMeta fieldMeta = fieldMetaMap.get(field);
      Map<String,Object> map;
      boolean uploaded = (boolean) reportField.get(OhsrsdohgovphReportDataRepository.COLUMN_UPLOAD);
      Map<String,Object> sourceSubMap = (Map<String,Object>) reportMap.get(
          uploaded ? SOURCE_CSV : SOURCE_DB);
      if (simpleRepresentation) {
        map = sourceSubMap;
      } else {
        String tableIndex = String.valueOf(
            reportField.get(OhsrsdohgovphReportDataRepository.COLUMN_TABLE_INDEX));
        if (!sourceSubMap.containsKey(tableIndex)) {
          sourceSubMap.put(tableIndex, new HashMap<String, Object>());
        }
        map = (Map<String,Object>) sourceSubMap.get(tableIndex);
      }
      String fieldDataType = fieldMeta.getDataType();
      try {
        String value = (String) reportField.get(OhsrsdohgovphReportDataRepository.COLUMN_VALUE);
        String desc = (String) reportField.get(
            OhsrsdohgovphReportDataRepository.COLUMN_DESCRIPTION);
        if (value == null || value.isEmpty()) {
          if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_AMOUNT)
              && fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_INTEGER)) {
            value = "0";
          } else {
            value = "";
          }
        }
        if (desc == null) {
          desc = "";
        }
        if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_AMOUNT)) {
          map.put(field, Double.parseDouble(value));
        } else if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_INTEGER)) {
          map.put(field, Integer.parseInt(value));
        } else if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_LOOKUP) && humanReadable) {
          map.put(field, desc);
        } else {
          map.put(field, value);
        }
      } catch (NumberFormatException ex) {
        logger.debug(ex.getMessage());
      }
    }
    return reportMap;
  }
  
  /**
   * Extract data from DB and unprocessed CSVs.
   * @param year Reporting Year
   * @return true or false indicating status of operation
   */
  public boolean process(int year) {
    OhsrsFunctionMeta meta = getMeta();
    int centerId = utility.getLoggedInCenterId();
    String user = utility.getLoggedInUser(); 
    Map<String, Long> tableIndexMap = new HashMap<>();
    Map<String, List<BasicDynaBean>> processedData = new HashMap<>();
    List<BasicDynaBean> beansToUpdate = new ArrayList<>();
    List<BasicDynaBean> beansToInsert = new ArrayList<>();
    processedData.put(OPERATION_UPDATE, beansToUpdate);
    processedData.put(OPERATION_INSERT, beansToInsert);
    List<Map<String, Object>> dbData = getData(year);
    reportRepo.flushRecords(year, centerId, meta.getKey(), false);
    Map<String, Map<String,String>> lookupMap = metaRepo.getLookupMetaMap(meta.getKey());
    Map<String, OhsrsFunctionFieldMeta> fieldMap = meta.getFieldsAsMap();
    String groupByField = null;
    String groupByValue = null;
    if (meta.getRepresentation().equals(OhsrsFunctionMeta.REPRESENTATION_TABLE)
        && meta.getGroupBy() != null && !meta.getGroupBy().isEmpty()) {
      groupByField = meta.getGroupBy();
    }
    for (Map<String, Object> rowData : dbData) {
      if (groupByField != null) {
        groupByValue = (String) rowData.get(groupByField);
      }
      for (Entry<String, Object> fieldEntry : rowData.entrySet()) {
        OhsrsFunctionFieldMeta fieldMeta = fieldMap.get(fieldEntry.getKey());
        String fieldDataType = fieldMeta.getDataType();
        String value = String.valueOf(fieldEntry.getValue());
        if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_LOOKUP)) {
          Map<String, String> fieldLookup = lookupMap.get(fieldEntry.getKey());
          value = fieldLookup.get(value.toLowerCase()); 
        }
        createUpdateBean(value, centerId, year, meta.getKey(), 
            String.valueOf(fieldEntry.getKey()), user, false, beansToUpdate, beansToInsert, 
            groupByField, groupByValue, tableIndexMap);
      }
    }
    if (!uploadSupported(year)) {
      return updateDb(year, processedData);
    }
    //Process upload and re-uploads which are not yet processed
    List<BasicDynaBean> unprocessedCsvs = 
        csvUploadRepo.getUnprocessedCsvUpload(centerId, year, meta.getKey());
    if (unprocessedCsvs.size() > 0) {
      reportRepo.flushRecords(year, centerId, meta.getKey(), true);      
    }
    for (BasicDynaBean bean : unprocessedCsvs) {
      CSVReader csvReader = new CSVReader(new InputStreamReader((InputStream) bean.get("content")));
      List<String[]> csvRows;
      try {
        csvRows = csvReader.readAll();
      } catch (IOException ex) {
        csvRows = new ArrayList<>();
      }
      Map<String, List<BasicDynaBean>> csvData = validateAndParseCsv(year, csvRows, tableIndexMap);
      processedData.get(OPERATION_INSERT).addAll(csvData.get(OPERATION_INSERT));
      processedData.get(OPERATION_UPDATE).addAll(csvData.get(OPERATION_UPDATE));
      csvUploadRepo.markAsProcessed(bean);
    }
    return updateDb(year, processedData);
  }
  
  private boolean updateDb(int year, Map<String, List<BasicDynaBean>> beanMap) {
    reportRepo.batchInsert(beanMap.get(OPERATION_INSERT));
    for (BasicDynaBean bean : beanMap.get(OPERATION_UPDATE)) {
      Map<String,Object> keys = new HashMap<>();
      keys.put(OhsrsdohgovphReportDataRepository.COLUMN_ID, 
          bean.get(OhsrsdohgovphReportDataRepository.COLUMN_ID));
      reportRepo.update(bean, keys);
    }
    return true;
  }

  private Map<String, List<BasicDynaBean>> validateAndParseCsv(int year, List<String[]> csvRows,
      Map<String, Long> tableIndexMap) {
    OhsrsFunctionMeta meta = getMeta();
    String ohsrsFunction = meta.getKey();
    int centerId = utility.getLoggedInCenterId();
    String user = utility.getLoggedInUser();
    Map<String, List<BasicDynaBean>> map = new HashMap<>();
    List<BasicDynaBean> beansToUpdate = new ArrayList<>();
    List<BasicDynaBean> beansToInsert = new ArrayList<>();
    map.put(OPERATION_UPDATE, beansToUpdate);
    map.put(OPERATION_INSERT, beansToInsert);
    Map<String, Map<String,String>> lookupMap = metaRepo.getLookupMetaMap(ohsrsFunction);
    Map<String, OhsrsFunctionFieldMeta> fieldMap = meta.getFieldsAsMap();
    if (csvRows.size() < 3) {
      logger.info("Skipping CSV Parsing for " + ohsrsFunction + ". Total Rows: "
          + String.valueOf(csvRows.size()));
      return map;
    }
    List<String> headers = Arrays.asList(csvRows.get(1));
    ListIterator<String[]> iterator = csvRows.listIterator();
    String groupByField = null;
    String groupByValue = null;
    int groupByFieldIdx = -1;
    if (meta.getRepresentation().equals(OhsrsFunctionMeta.REPRESENTATION_TABLE)
        && meta.getGroupBy() != null && !meta.getGroupBy().isEmpty()) {
      groupByField = meta.getGroupBy();
      groupByFieldIdx = headers.indexOf(groupByField);
    }
    while (iterator.hasNext()) {
      if (iterator.nextIndex() < 2) {
        iterator.next();
        continue;
      }
      List<String> row = Arrays.asList(iterator.next()); 
      if (groupByField != null && groupByFieldIdx != -1) {
        groupByValue = row.get(groupByFieldIdx);
      } else {
        groupByValue = null;
      }
      
      for (int idx = 0; idx < headers.size(); idx++) {
        String field = headers.get(idx);
        String value = row.get(idx).trim();
        if (!fieldMap.containsKey(field)) {
          throw new ValidationException(
              "ui.error.csv.validation.failed.for.unknown.field.double.placeholder",
              new String[] {meta.getLabel(), field});
        }
        Object interpretedValue = null;
        OhsrsFunctionFieldMeta fieldMeta = fieldMap.get(field);
        String fieldDataType = fieldMeta.getDataType();
        if (value.isEmpty() && (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_AMOUNT)
            || fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_INTEGER))) {
          value = "0";
        }
        if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_AMOUNT)) {
          try {
            interpretedValue = Double.parseDouble(value);
          } catch (NumberFormatException ex) {
            throw new ValidationException(
                "ui.error.csv.validation.failed.on.row.value.is.not.an.amount.triple.placeholder",
                new String[] {meta.getLabel(), String.valueOf(idx + 1), value});
          }
        } else if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_INTEGER)) {
          try {
            interpretedValue = Integer.parseInt(value);
          } catch (NumberFormatException ex) {
            throw new ValidationException(
                "ui.error.csv.validation.failed.on.row.value.is.not.an.integer.triple.placeholder",
                new String[] {meta.getLabel(), String.valueOf(idx + 1), value});
          }
        } else if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_DATE) && !value.isEmpty()) {
          try {
            interpretedValue = DATE_PARSER.format(DATE_PARSER.parse(value));
          } catch (ParseException ex) {
            throw new ValidationException(
                "ui.error.csv.validation.failed.on.row.value.is.not.a.date.triple.placeholder",
                new String[] {meta.getLabel(), String.valueOf(idx + 1), value});
          }
        } else if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_LOOKUP)) {
          Map<String, String> fieldLookup = lookupMap.get(field);
          if (!fieldLookup.containsKey(value.toLowerCase())) {
            throw new ValidationException(
                "ui.error.csv.validation.failed.on.row.is.not.a.valid."
                + "option.for.quadruple.placeholder",
                new String[] {meta.getKey(), String.valueOf(idx + 1), value, field});
          }
          interpretedValue = fieldLookup.get(value.toLowerCase());
        } else {
          interpretedValue = value;
        }
        
        createUpdateBean(String.valueOf(interpretedValue), centerId, year, ohsrsFunction, field, 
            user, true, beansToUpdate, beansToInsert, groupByField, groupByValue, tableIndexMap);
      }
    }
    return map;
  }

  private void createUpdateBean(String value, int centerId, int year, String ohsrsFunction, 
      String field, String user, boolean upload, List<BasicDynaBean> beansToUpdate, 
      List<BasicDynaBean> beansToInsert, String groupByField, String groupByValue,
      Map<String, Long> tableIndexMap) {
    BasicDynaBean bean = reportRepo.getReportFieldData(centerId, year, ohsrsFunction, field, 
        upload);
    if (bean == null) {
      bean = reportRepo.newBean(value, centerId, year, ohsrsFunction, field, upload, user, 
          groupByField, groupByValue, tableIndexMap);
      beansToInsert.add(bean);
    } else {
      reportRepo.updateBean(bean, user, value);
      beansToUpdate.add(bean);
    }    
  }

  private boolean uploadSupported(int year) {
    OhsrsFunctionMeta meta = getMeta();
    int centerId = utility.getLoggedInCenterId();
    BasicDynaBean center = centerService.findByKey(centerId);
    Date goliveDate = (Date) center.get("golive_date");
    String uploadable = meta.getUploadable();
    int goliveYear = DateUtil.getYear(goliveDate);
    java.util.Date goLiveYearStart = DateUtil.getFirstDayOfYear(goliveDate);
    return uploadable.equals(OhsrsFunctionMeta.UPLOADABLE_ALWAYS)
        || (uploadable.equals(OhsrsFunctionMeta.UPLOADABLE_FIRST_YEAR) 
            && (goliveDate != null && year == goliveYear && goliveDate.after(goLiveYearStart)));
    
  }
  
  /**
   * Get CSV template.
   * @param year Reporting Year
   * @return Model and View for CSV Template
   */
  public CsVModelAndView getCsvTemplate(int year) {
    OhsrsFunctionMeta meta = getMeta();
    int centerId = utility.getLoggedInCenterId();
    BasicDynaBean center = centerService.findByKey(centerId);
    Date goliveDate = (Date) center.get("golive_date");
    String uploadable = meta.getUploadable();
    int goliveYear = DateUtil.getYear(goliveDate);
    java.util.Date goLiveYearStart = DateUtil.getFirstDayOfYear(goliveDate);
    if (!uploadSupported(year)) {
      throw new ValidationException(
          "ui.error.csv.upload.download.not.supported.for.single.placeholder", 
          new String[] {meta.getKey()});
    }
    List<String> labels = new ArrayList<>();
    List<String> fields = new ArrayList<>();
    for (OhsrsFunctionFieldMeta field : meta.getFields()) {
      labels.add(field.getLabel());
      fields.add(field.getKey());
    }
    CsVModelAndView mav = new CsVModelAndView(
        "ohsrs_" + meta.getKey() + "_" + String.valueOf(year));
    List<String[]> dataList = new ArrayList<>();
    dataList.add(fields.toArray(new String[0]));
    mav.addHeader(labels.toArray(new String[0]));
    mav.addData(dataList);
    return mav;
  }
  
  /**
   * Get computed data points for given OHSRS function from Insta's dataset.
   * @param year Reporting Year
   * @return Computed data points from Insta's dataset
   */
  public List<Map<String, Object>> getData(int year) {
    List<Map<String, Object>> data = getData(year, utility.getLoggedInCenterId());
    return data == null ? new ArrayList<Map<String, Object>>() : data;
  }
  
  protected abstract List<Map<String, Object>> getData(int year, int centerId); 

  protected OhsrsFunctionMeta getMeta() {
    String ohsrsFunction = getClass().getAnnotation(OhsrsFunctionProcessor.class).supports();
    return utility.getOhsrsFunctionMeta(ohsrsFunction);
  }

  private List<Map<String, String>> getUploadData(int year) {
    OhsrsFunctionMeta meta = getMeta();
    List<Map<String, String>> uploadData;
    Map<String, Object> reportMap = getReportMap(year, utility.getLoggedInCenterId(), false);
    Map<String, Object> dbReportMap = (Map<String, Object>) reportMap.get(SOURCE_DB);
    Map<String, Object> csvReportMap = (Map<String, Object>) reportMap.get(SOURCE_CSV);
    boolean uploadSupported = uploadSupported(year);
    boolean simpleRepresentation = meta.getRepresentation()
        .equals(OhsrsFunctionMeta.REPRESENTATION_SIMPLE);
    boolean uploadAlways = meta.getUploadable()
        .equals(OhsrsFunctionMeta.UPLOADABLE_ALWAYS);
    List<OhsrsFunctionFieldMeta> fields = meta.getFields();
    if (!uploadSupported) {
      uploadData = simpleRepresentation 
          ? Arrays.asList(reportMapToUploadData(dbReportMap, fields))
              : reportMapMapToUploadData(dbReportMap, fields);
    } else if (uploadAlways) {
      uploadData = simpleRepresentation 
          ? Arrays.asList(reportMapToUploadData(csvReportMap, fields))
              : reportMapMapToUploadData(csvReportMap, fields);
    } else {
      uploadData = simpleRepresentation 
          ? Arrays.asList(reportMapToUploadData(dbReportMap, csvReportMap, fields))
              : reportMapMapToUploadData(dbReportMap, csvReportMap, fields);
    }
    return uploadData;
  }
  
  private Map<String, String> reportMapToUploadData(Map<String, Object> map, 
      List<OhsrsFunctionFieldMeta> fields) {
    Map<String, String> data = new HashMap<>();
    for (OhsrsFunctionFieldMeta field : fields) {
      String key = field.getKey();
      String fieldDataType = field.getDataType();
      String value;
      Object originalValue = map.get(key);
      if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_AMOUNT)
          || fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_INTEGER)) {
        value = String.valueOf(originalValue);
      } else {
        value = (String) originalValue;
      }
      data.put(key, value != null ? value : "");
    }
    return data;
  }

  private Map<String, String> reportMapToUploadData(Map<String, Object> mapA, 
      Map<String, Object> mapB, List<OhsrsFunctionFieldMeta> fields) {
    Map<String, String> data = new HashMap<>();
    for (OhsrsFunctionFieldMeta field : fields) {
      String key = field.getKey();
      String fieldDataType = field.getDataType();
      String value;
      Object originalValueA = mapA.get(key);
      Object originalValueB = mapB.get(key);
      if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_AMOUNT)) {
        value = String.valueOf((originalValueA != null ? ((double) originalValueA) : 0)
            + (originalValueB != null ? ((double) originalValueB) : 0));
      } else if (fieldDataType.equals(OhsrsFunctionFieldMeta.DATATYPE_INTEGER)) {
        value = String.valueOf((originalValueA != null ? ((int) originalValueA) : 0)
            + (originalValueB != null ? ((int) originalValueB) : 0));
      } else {
        value = originalValueA != null ? ((String) originalValueA) : ((String) originalValueB);
      }
      data.put(key, value != null ? value : "");
    }
    return data;
  }
  
  private List<Map<String, String>> reportMapMapToUploadData(Map<String, Object> map, 
      List<OhsrsFunctionFieldMeta> fields) {
    List<Map<String,String>> list = new ArrayList<>();
    for (String key : map.keySet()) {
      list.add(reportMapToUploadData((Map<String, Object>) map.get(key), fields));
    }
    return list;
  }

  private List<Map<String, String>> reportMapMapToUploadData(Map<String, Object> mapA, 
      Map<String, Object> mapB, List<OhsrsFunctionFieldMeta> fields) {
    List<Map<String,String>> list = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    keys.addAll(mapA.keySet());
    for (String key : mapB.keySet()) {
      if (!keys.contains(key)) {
        keys.add(key);
      }
    }
    for (String key : keys) {
      list.add(reportMapToUploadData(
          (mapA.containsKey(key) ? ((Map<String, Object>) mapA.get(key)) 
              : (new HashMap<String, Object>())),
          (mapB.containsKey(key) ? ((Map<String, Object>) mapB.get(key))
              : (new HashMap<String, Object>())),
          fields));
    }
    return list;    
  }
  
  protected void updateIcdFields(Map<String, String> data, String codeKey, 
      String descKey, String catKey) {
    BasicDynaBean icdMeta = icdLookupRepo.findByKey(
        OhsrsdohgovphIcdLookupDataRepository.COLUMN_ICD10CODE, data.get(codeKey));
    String desc = "";
    String cat = "";
    if (icdMeta != null) {
      cat = (String) icdMeta.get(OhsrsdohgovphIcdLookupDataRepository.COLUMN_ICD10CAT);
      desc = (String) icdMeta.get(OhsrsdohgovphIcdLookupDataRepository.COLUMN_ICD10DESC);
    }
    data.put(catKey, cat);
    data.put(descKey, desc);
  }
  
  protected void updateSurgeryFields(Map<String, String> data, String codeKey, 
      String descKey) {
    BasicDynaBean surgeryMeta = surgeryLookupRepo.findByKey(
        OhsrsdohgovphSurgeryLookupDataRepository.COLUMN_OPERATIONCODE, data.get(codeKey));
    String desc = "";
    if (surgeryMeta != null) {
      desc = (String) surgeryMeta.get(OhsrsdohgovphSurgeryLookupDataRepository.COLUMN_DESCRIPTION);
    }
    data.put(descKey, desc);
  }

  public boolean submit(int year, OhsrsdohgovphSettings settings) {
    return submit(year, settings, getUploadData(year));
  }

  protected abstract boolean submit(int year, OhsrsdohgovphSettings settings, 
      List<Map<String, String>> uploadData);
  
  protected Map<String, List<String>> getDeptMapping() {
    Map<String, Object> reportingMeta = centerService
        .getReportingMeta(utility.getLoggedInCenterId());
    return (Map<String, List<String>>) reportingMeta.get(SETTINGS_DEPT_MAPPING);  
  }

  protected String getDeptMappingKey(String deptName) {
    return deptName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
  }
  
  protected Map<String, List<String>> getDiagDeptMapping() {
    Map<String, Object> reportingMeta = centerService
        .getReportingMeta(utility.getLoggedInCenterId());
    return (Map<String, List<String>>) reportingMeta.get(SETTINGS_DIAG_DEPT_MAPPING);  
  }
  
  protected Map<String, List<String>> getIcdMapping() {
    Map<String, Object> reportingMeta = centerService
        .getReportingMeta(utility.getLoggedInCenterId());
    return (Map<String, List<String>>) reportingMeta.get(SETTINGS_ICD_MAPPING);  
  }

  protected Map<String, List<String>> getWardMapping() {
    Map<String, Object> reportingMeta = centerService
        .getReportingMeta(utility.getLoggedInCenterId());
    return (Map<String, List<String>>) reportingMeta.get(SETTINGS_WARD_MAPPING);  
  }

  protected Object getReportMetaField(String key) {
    Map<String, Object> reportingMeta = centerService
        .getReportingMeta(utility.getLoggedInCenterId());
    return reportingMeta.get(key);  
  }

  protected List<String> getListMapping(String key) {
    Map<String, Object> reportingMeta = centerService
        .getReportingMeta(utility.getLoggedInCenterId());
    return (List<String>) reportingMeta.get(key);  
  }
}
