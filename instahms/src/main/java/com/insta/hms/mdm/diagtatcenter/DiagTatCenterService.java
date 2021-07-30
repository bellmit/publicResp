package com.insta.hms.mdm.diagtatcenter;

import au.com.bytecode.opencsv.CSVReader;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.diagtesttemplates.DiagTestTemplateService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class DiagTatCenterService.
 *
 * @author anil.n
 */
@Service
public class DiagTatCenterService extends BulkDataService {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(DiagTestTemplateService.class);

  /** The message util. */
  @LazyAutowired
  MessageUtil messageUtil;

  /** The diag tat center repository. */
  @LazyAutowired
  private DiagTatCenterRepository diagTatCenterRepository;

  /** The generic preference service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferenceService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /**
   * Instantiates a new diag tat center service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   * @param entity
   *          the entity
   */
  public DiagTatCenterService(DiagTatCenterRepository repo, DiagTatCenterValidator validator,
      DiagTatCenterCsvBulkDataEntity entity) {
    super(repo, validator, entity);
  }

  /**
   * Adds the test TAT centers.
   *
   * @param testId
   *          the test id
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean addTestTatCenters(String testId) {

    boolean success = true;
    List<BasicDynaBean> centerIdList = Collections.EMPTY_LIST;
    BasicDynaBean bean = genericPreferenceService.getPreferences();
    int maxCentersIncDefault = (Integer) bean.get("max_centers_inc_default");
    if (maxCentersIncDefault > 1) {
      centerIdList = centerService.getCentersList();
    } else {
      centerIdList = centerService.listAll(true);
    }
    BasicDynaBean diagTatCenterBean = diagTatCenterRepository.getBean();

    if (centerIdList != null && !centerIdList.isEmpty()) {
      for (Iterator iterator = centerIdList.iterator(); iterator.hasNext();) {
        Integer centerId = (Integer) ((BasicDynaBean) iterator.next()).get("center_id");
        diagTatCenterBean.set("tat_center_id",
            Integer.toString(diagTatCenterRepository.getNextSequence()));
        diagTatCenterBean.set("test_id", testId);
        diagTatCenterBean.set("center_id", centerId);
        diagTatCenterBean.set("processing_days", "XXXXXXX");
        success &= diagTatCenterRepository.insert(diagTatCenterBean) > 0;
      }
    }
    return success;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.bulk.BulkDataService#getMasterData()
   */
  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    return null;
  }

  /**
   * Gets the max centers default.
   *
   * @return the max centers default
   */
  public int getMaxCentersDefault() {
    return (Integer) genericPreferenceService.getPreferences().get("max_centers_inc_default");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.mdm.bulk.BulkDataService#parseAndImportCSV(org.springframework.web.multipart.
   * MultipartFile, java.util.Map)
   */
  @Override
  public String parseAndImportCsV(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {

    boolean hasErrors = false;
    List<BasicDynaBean> rows = new ArrayList<BasicDynaBean>();
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<Object, Object>();
    Map<String, String> aliasUnmsToDBnmsMap = new HashMap<String, String>();
    aliasUnmsToDBnmsMap.put("tat center id", "tat_center_id");
    aliasUnmsToDBnmsMap.put("Center Id", "center_id");
    aliasUnmsToDBnmsMap.put("Test Name", "test_name");
    aliasUnmsToDBnmsMap.put("Center Name", "center_name");
    aliasUnmsToDBnmsMap.put("Logistics TAT", "logistics_tat_hours");
    aliasUnmsToDBnmsMap.put("Processing Days", "processing_days");
    aliasUnmsToDBnmsMap.put("Conduction Start Time", "conduction_start_time");
    aliasUnmsToDBnmsMap.put("Conduction TAT", "conduction_tat_hours");
    try {
      InputStreamReader streamReader = new InputStreamReader(file.getInputStream());
      CSVReader csvReader = new CSVReader(streamReader);
      String[] headers = csvReader.readNext();

      if (headers.length < 1) {
        return "exception.csv.missing.headers";
      }
      if (!headers[0].matches("\\p{Print}*")) {
        return "exception.csv.non.printable.characters";
      }
      if (headers.length == 1) {
        return "exception.csv.non.comma.seperators";
      }
      boolean[] ignoreColumn = new boolean[headers.length];
      Integer lineNumber = 0;
      Integer lineWarningsCount = 0;
      Integer updationCount = 0;
      String testName = null;
      BigDecimal logTathours = null;
      String tatCenterId = null;
      BigDecimal conductionTathours = null;
      Time condStartTime = null;
      String processingDays = new String();
      CsVBulkDataEntity csvEntity = getCsVDataEntity();
      Map<String, Class<?>> typeMap = csvEntity.getTypeMap();

      for (Integer index = 0; index < headers.length; index++) {
        String fieldName = aliasUnmsToDBnmsMap.get(headers[index].trim());
        if (fieldName == null || fieldName.equals("")) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.header", headers[index]);
          ignoreColumn[index] = true;
        } else {
          ignoreColumn[index] = false;
        }

        headers[index] = fieldName;
      }
      List<String> manadatoryField = Arrays.asList(new String[] { "test_name", "center_name" });
      for (String mfield : manadatoryField) {
        if (!Arrays.asList(headers).contains(mfield)) {
          addWarning(warnings, lineNumber,
              "Mandatory field " + mfield + " is missing cannot process further in the sheet ",
              mfield);
          hasErrors = true;
        }
      }
      if (hasErrors) {
        feedback.put("result", meta);
        feedback.put("warnings", warnings);
        return null;
      }
      lineNumber++;

      String[] row = null;
      while (null != (row = csvReader.readNext())) {
        Integer nonEmptyColumnsCount = 0;
        boolean hasWarnings = false;
        BasicDynaBean bean = getRepository().getBean();
        lineNumber++;

        for (Integer columnIndex = 0; columnIndex < headers.length
            && columnIndex < row.length; columnIndex++) {
          if (ignoreColumn[columnIndex]) {
            continue;
          }

          String fieldName = headers[columnIndex];
          String fieldValue = row[columnIndex].trim();
          DynaProperty property;

          if ((null != fieldValue) && !(fieldValue.isEmpty())) {
            if (fieldName.equals("tat_center_id")) {
              tatCenterId = fieldValue;
              bean.set(fieldName, fieldValue);
            }
            if (fieldName.equals("test_name")) {
              testName = fieldValue;
            }

            if (fieldName.equals("logistics_tat_hours")) {
              if (fieldValue != null && !fieldValue.equals("")) {
                logTathours = new BigDecimal(Double.parseDouble(fieldValue));
                bean.set(fieldName, logTathours);
              }
            }
            if (fieldName.equals("processing_days")) {
              processingDays = fieldValue;
              bean.set(fieldName, fieldValue);
            }
            if (fieldName.equals("conduction_start_time")) {
              if (fieldValue != null) {
                condStartTime = java.sql.Time.valueOf(fieldValue);
                bean.set(fieldName, condStartTime);
              }
            }
            if (fieldName.equals("conduction_tat_hours")) {
              if (fieldValue != null && !fieldValue.equals("")) {
                conductionTathours = new BigDecimal(Double.parseDouble(fieldValue));
                bean.set(fieldName, conductionTathours);
              }
            }
            property = bean.getDynaClass().getDynaProperty(fieldName);
            Class<?> enforcedType = typeMap.get(fieldName);
            if (null != enforcedType) {
              if (null == ConvertUtils.convert(fieldValue, enforcedType)) {
                addWarning(warnings, lineNumber, "exception.csv.conversion.error", fieldValue,
                    (enforcedType == BigDecimal.class ? " Number " : enforcedType.getSimpleName()),
                    fieldName);
                hasWarnings = true;
                continue;
              }
            }
            nonEmptyColumnsCount++;
          }
        }

        if (hasWarnings || nonEmptyColumnsCount == 0) {
          continue;
        }

        try {
          Integer rowsAffected = super.update(bean);
          if (rowsAffected != 0) {
            updationCount++;
          }
        } catch (DuplicateEntityException ex) {
          addWarning(warnings, lineNumber, "exception.csv.duplicate.record");
          logger.error("Duplicate record found : " + bean.get("dept_name"));
          lineWarningsCount++;
        } catch (DataAccessException ex) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.error",
              ex.getMostSpecificCause().getMessage());
          logger.error("Error uploading csv line", ex.getCause());
          lineWarningsCount++;
        } catch (ValidationException ex) {
          for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) (Object) ex
              .getErrors()).entrySet()) {
            warnings.add(lineNumber, entry.getValue().get(0));
          }

          logger.error(ex.getMessage());
        }
      }
      feedback.put("warnings", warnings);

      meta.add("processed_count", lineNumber - 1);
      meta.add("insertion_count", 0);
      meta.add("updation_count", updationCount);
      feedback.put("result", meta);

    } catch (IOException ex) {
      throw new InvalidFileFormatException(ex);
    }

    return null;
  }

  /**
   * Adds the warning.
   *
   * @param warnings
   *          the warnings
   * @param lineNumber
   *          the line number
   * @param message
   *          the message
   * @param parameters
   *          the parameters
   */
  @Override
  protected void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message, Object... parameters) {
    StringBuilder warning = new StringBuilder();
    warning.append(messageUtil.getMessage(message, parameters));
    warnings.add(lineNumber, warning.toString());
  }
}
