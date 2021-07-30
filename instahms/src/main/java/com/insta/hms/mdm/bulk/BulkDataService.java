package com.insta.hms.mdm.bulk;

import au.com.bytecode.opencsv.CSVReader;

import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.MasterValidator;


import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class BulkDataService which serves as a service facade for bulk data operations. The CSV
 * entity is injected here.
 * 
 * @author tanmay.k
 */
public abstract class BulkDataService extends MasterService {

  /** The CSV data entity. */
  private CsVBulkDataEntity csvDataEntity;

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(BulkDataService.class);

  /** The missing headers key. */
  private static final String MISSINGHEADERSKEY = "exception.csv.missing.headers";

  /** The non printable headers key. */
  private static final String NONPRINTABLEHEADERSKEY = "exception.csv.non.printable.characters";

  /** The non comma delimiter key. */
  private static final String NONCOMMADELIMITERKEY = "exception.csv.non.comma.seperators";

  /** The unknown header key. */
  private static final String UNKNOWNHEADERKEY = "exception.csv.unknown.header";

  /** The unknown master value key. */
  private static final String UNKNOWNMASTERVALUEKEY = "exception.csv.unknown.master.value";

  /** The filter check failure key. */
  private static final String FILTERCHECKFAILUREKEY = "exception.csv.filter.check.failure";

  /** The conversion error key. */
  private static final String CONVERSIONERRORKEY = "exception.csv.conversion.error";

  /** The duplicate record key. */
  private static final String DUPLICATERECORDKEY = "exception.csv.duplicate.record";

  /** The unknown error key. */
  private static final String UNKNOWNERRORKEY = "exception.csv.unknown.error";

  /** The message util. */
  MessageUtil messageUtil = null;

  /**
   * Instantiates a new bulk data service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param csvEntity
   *          the Data Access Object for CSV bulk operations
   */
  public BulkDataService(MasterRepository<?> repository, MasterValidator validator,
      CsVBulkDataEntity csvEntity) {
    super(repository, validator);
    this.csvDataEntity = csvEntity;
  }

  /**
   * Gets the CSV data entity.
   *
   * @return the CSV data entity
   */
  public CsVBulkDataEntity getCsVDataEntity() {
    return this.csvDataEntity;
  }

  /**
   * Gets the message util.
   *
   * @return the message util
   */
  public MessageUtil getMessageUtil() {
    if (null == this.messageUtil) {
      this.messageUtil = ApplicationContextProvider.getBean(MessageUtil.class);
    }

    return this.messageUtil;
  }

  /**
   * Exports data.
   *
   * @return the list
   */
  public Map<String, List<String[]>> exportData() {
    BulkDataRepository<?> repository = (BulkDataRepository<?>) super.getRepository();
    setDynamicAliases();
    return repository.exportData(getCsVDataEntity());
  }
  
  /**
   * Exports data with some filter.
   *
   * @param values
   *          the values
   * @return the list
   */
  public Map<String, List<String[]>> exportData(Object[] values) {
    BulkDataRepository<?> repository = (BulkDataRepository<?>) super.getRepository();
    setDynamicAliases();
    return repository.exportData(getCsVDataEntity(), values);
  }

  /**
   * Import data.
   *
   * @param file
   *          the file
   * @param feedback
   *          the feedback
   * @return the string
   */
  public String importData(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {
    setDynamicAliases();
    return parseAndImportCsV(file, feedback);
  }

  
  public String importData(InputStreamReader streamReader,
      Map<String, MultiValueMap<Object, Object>> feedback) throws IOException {
    setDynamicAliases();
    return parseAndImportCsV(streamReader, feedback);
  }

  /**
   * Parses the master data.
   *
   * @param masterData
   *          the master data
   * @return the map
   */
  private Map<String, Map<String, Object>> parseMasterData(
      Map<String, List<BasicDynaBean>> masterData) {
    CsVBulkDataEntity csvEntity = getCsVDataEntity();
    Map<String, Map<String, Object>> parsedMap = new HashMap<String, Map<String, Object>>();
    for (Entry<String, List<BasicDynaBean>> entry : masterData.entrySet()) {
      Map<String, Object> nameIdMap = new HashMap<String, Object>();
      BulkDataMasterEntity master = csvEntity.getReferencedMastersWithFieldAsKeyMap()
          .get(entry.getKey());
      for (BasicDynaBean bean : entry.getValue()) {
        nameIdMap.put((String) bean.get(master.getReferencedTableNameField()),
            bean.get(master.getReferencedTablePrimaryKeyField()));
      }
      parsedMap.put(entry.getKey(), nameIdMap);
    }
    return parsedMap;
  }

  /**
   * Parses the and import CSV.
   *
   * @param file
   *          the file
   * @param feedback
   *          the feedback
   * @return the string
   */
  @SuppressWarnings("unchecked")
  public String parseAndImportCsV(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {

    try {
      InputStreamReader streamReader = new InputStreamReader(file.getInputStream());
      parseAndImportCsV(streamReader, feedback);
    } catch (IOException exception) {
      throw new InvalidFileFormatException(exception);
    }

    return null;
  }

  /**
   * Parses the and import CSV.
   *
   * @param streamReader
   *          the streamReader
   * @param feedback
   *          the feedback
   * @return the string
   */
  public String parseAndImportCsV(InputStreamReader streamReader,
      Map<String, MultiValueMap<Object, Object>> feedback) throws IOException {
    List<BasicDynaBean> rows = new ArrayList<BasicDynaBean>();
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();

    CSVReader csvReader = new CSVReader(streamReader);
    String[] headers = csvReader.readNext();

    if (headers.length < 1) {
      return MISSINGHEADERSKEY;
    }

    if (!headers[0].matches("\\p{Print}*")) {
      return NONPRINTABLEHEADERSKEY;
    }

    if (headers.length == 1) {
      return NONCOMMADELIMITERKEY;
    }

    boolean[] ignoreColumn = new boolean[headers.length];
    Integer lineNumber = 0;
    Integer insertionCount = 0;
    Integer updationCount = 0;
    CsVBulkDataEntity csvEntity = getCsVDataEntity();
    Map<String, Class<?>> typeMap = csvEntity.getTypeMap();

    for (Integer index = 0; index < headers.length; index++) {
      String fieldName = csvEntity.getRealFieldName(headers[index].trim());
      if (!csvEntity.getAllFields().contains(fieldName)) {
        addWarning(warnings, lineNumber, UNKNOWNHEADERKEY, headers[index]);
        ignoreColumn[index] = true;
      } else {
        ignoreColumn[index] = false;
      }

      headers[index] = fieldName;
    }
    lineNumber++;

    String[] row = null;
    Map<String, Map<String, Object>> masterData = parseMasterData(getMasterData());

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
          Map<String, Object> masterDataForField = masterData.get(fieldName);
          if (null != masterDataForField) {
            String primaryKeyForField = (null == masterDataForField.get(fieldValue)) ? null
                : String.valueOf(masterDataForField.get(fieldValue));
            if (null == primaryKeyForField) {
              addWarning(warnings, lineNumber, UNKNOWNMASTERVALUEKEY, fieldValue, fieldName);
              hasWarnings = true;
            }

            String filterCheckName = (String) csvEntity.getMasterFilterCheck().get(fieldName);
            if ((null != filterCheckName) && !(filterCheckName.equals(fieldValue))) {
              addWarning(warnings, lineNumber, FILTERCHECKFAILUREKEY, fieldValue, fieldName);
              hasWarnings = true;
              continue;
            }

            fieldValue = primaryKeyForField;
          }

          property = bean.getDynaClass().getDynaProperty(fieldName);
          Class<?> enforcedType = typeMap.get(fieldName);

          if (null != enforcedType) {
            Object convertedFieldValue = null;
            try {
              convertedFieldValue = ConvertUtils.convert(fieldValue, enforcedType);
            } finally {
              if (null == convertedFieldValue) {
                addWarning(warnings, lineNumber, CONVERSIONERRORKEY, fieldValue,
                    (enforcedType == BigDecimal.class ? " Number " : enforcedType.getSimpleName()),
                    fieldName);
                hasWarnings = true;
              }
            }
          }
          bean.set(fieldName, ConvertUtils.convert(fieldValue, property.getType()));
          nonEmptyColumnsCount++;
        } else {
          bean.set(fieldName, null);
        }
      }

      Map<String, BulkDataLookupEntity> masterLookup = csvEntity.getMasterLookupMap();
      if ((null != masterLookup) && !(masterLookup.isEmpty())) {
        for (DynaProperty property : bean.getDynaClass().getDynaProperties()) {
          BulkDataLookupEntity lookup = masterLookup.get(property.getName());
          Map lookupMap = lookup.getMap();
          List list = (List) lookupMap.get(bean.get(lookup.getField()));
          if ((null == list) || !(list.contains(bean.get(lookup.getField())))) {
            addWarning(warnings, lineNumber, lookup.getErrorMessage());
          }
        }
      }

      if (hasWarnings || nonEmptyColumnsCount == 0) {
        continue;
      }

      Map<String, Object> keyValueMap = new HashMap<String, Object>();
      boolean allKeysGiven = true;
      List<String> keys = csvEntity.getKeys();

      for (String key : keys) {
        Object keyValue = bean.get(key);
        if (null == keyValue) {
          allKeysGiven = false;
        }
        keyValueMap.put(key, keyValue);
      }

      try {
        if (keys.size() == 1) {
          // pre-update hook for the derived classes to override for any special pre-processing on
          // the bean
          // the default implementation does nothing, returns true
          
          CsvRowContext rowContext = new CsvRowContext(lineNumber, headers, row, bean, warnings);
          
          boolean okToContinue = preUpdate(rowContext);
          if (!okToContinue) {
            continue; // skip the update and go to the next record
          }
          if (allKeysGiven) {
            Integer rowsAffected = update(bean);
            if (rowsAffected != 0) {
              updationCount++;
            } else {
              super.insert(bean);
              insertionCount++;
            }
          } else {
            super.insert(bean);
            insertionCount++;
          }

          // post-update hook for the derived classes to override for any additional processing
          // after
          // the main record has been updated / inserted. The default implementation does nothing.
          postUpdate(rowContext);

        }
      } catch (DuplicateEntityException exception) {
        addWarning(warnings, lineNumber, DUPLICATERECORDKEY, exception.getMessage());
        logger.error("Duplicate record found : " + exception.getMessage());
      } catch (DataAccessException exception) {
        addWarning(warnings, lineNumber, UNKNOWNERRORKEY,
            exception.getMostSpecificCause().getMessage());
        logger.error("Error uploading csv line", exception.getCause());
      } catch (ValidationException exception) {
        for (Map.Entry<String, List<String>> entry : (exception.getErrors()).entrySet()) {
          warnings.add(lineNumber, entry.getValue().get(0));
        }

        logger.error(exception.getMessage());
      }
    }
    feedback.put("warnings", warnings);

    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<Object, Object>();
    meta.add("processed_count", lineNumber - 1);
    meta.add("insertion_count", insertionCount);
    meta.add("updation_count", updationCount);
    feedback.put("result", meta);
    return null;

  }

  /**
   * Pre-Processing Hook - called just before the row is saved into the database.
   *
   * @param rowContext the row context
   * @return true, if successful
   */

  protected boolean preUpdate(CsvRowContext rowContext) {
    boolean okToContinue = true;
    return okToContinue;
  }

  /**
   * Post Processing Hook - called just after a row has been saved to the database.
   *
   * @param headers
   *          the headers
   * @param row
   *          the row
   * @param bean
   *          the bean
   */

  protected void postUpdate(CsvRowContext rowContext) {
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
  protected void addWarning(MultiValueMap<Object, Object> warnings, Integer lineNumber,
      String message, Object... parameters) {
    StringBuilder warning = new StringBuilder();
    warning.append(getMessageUtil().getMessage(message, parameters));
    warnings.add(lineNumber, warning.toString());
  }

  /**
   * Gets the dynamic header aliases. The default implementation returns an empty map. Classes that
   * require dynamic aliases i.e. where the field name changes on a certain situation must override
   * this method and return the required map.
   *
   * @return the dynamic header aliases
   */
  public Map<String, String> getDynamicHeaderAliases() {
    return Collections.emptyMap();
  }

  /**
   * Sets the dynamic aliases in CSVBulkDataEntity.
   */
  private void setDynamicAliases() {
    Map<String, String> aliases = getDynamicHeaderAliases();
    if (null != aliases && !(aliases.isEmpty())) {
      getCsVDataEntity().setAliases(aliases);
    }
  }

  /**
   * Gets the master data.
   *
   * @return the master data
   */
  public abstract Map<String, List<BasicDynaBean>> getMasterData();

}
