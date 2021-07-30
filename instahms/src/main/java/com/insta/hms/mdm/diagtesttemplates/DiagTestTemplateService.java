package com.insta.hms.mdm.diagtesttemplates;

import au.com.bytecode.opencsv.CSVReader;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.bulk.BulkDataMasterEntity;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestValidator;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Class DiagTestTemplateService.
 *
 * @author anil.n
 */
@Service
public class DiagTestTemplateService extends BulkDataService {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(DiagTestTemplateService.class);

  /** The message util. */
  @LazyAutowired
  MessageUtil msgUtil;

  /** The diagnostic test service. */
  @Autowired
  private DiagnosticTestService diagnosticTestService;

  /** The diag department service. */
  @Autowired
  private DiagDepartmentService diagDepartmentService;

  /** The diag test template repository. */
  @Autowired
  private DiagTestTemplateRepository diagTestTemplateRepository;

  /**
   * Instantiates a new diag test template service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param csvDataEntity
   *          the csv data entity
   */
  public DiagTestTemplateService(DiagTestTemplateRepository repository,
      DiagnosticTestValidator validator, TestTemplateCsvBulkDataEntity csvDataEntity) {
    super(repository, validator, csvDataEntity);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.bulk.BulkDataService#getMasterData()
   */
  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    Map<String, List<BasicDynaBean>> masterMap = new HashMap<>();
    masterMap.put("test_id", diagnosticTestService.lookup(true));
    masterMap.put("ddept_name", diagDepartmentService.lookup(true));
    masterMap.put("format_name", diagnosticTestService.getReportFormats());
    return masterMap;
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
    Map<String, String> testIdsMap = diagnosticTestService.getTestNamesAndIds();
    Map<String, String> deptsMap = diagDepartmentService.getDiagDepartmentsMap();
    Map<String, String> reportFormatMap = diagnosticTestService.getReportFormatsMap();
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<>();
    Map<String, String> headersMap = new HashMap<>();
    Map<String, String> deletedIds = new HashMap<>();
    headersMap.put("Test Name", "test_name");
    headersMap.put("Dept Name", "ddept_name");
    headersMap.put("Format Name", "format_name");

    List<String> mandatoryList = Arrays.asList("test_name", "ddept_name", "format_name");
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

      Integer lineNumber = 0;
      Integer lineWarningsCount = 0;
      Integer updationCount = 0;
      CsVBulkDataEntity csvEntity = getCsVDataEntity();
      Map<String, Class<?>> typeMap = csvEntity.getTypeMap();
      boolean[] ignoreColumn = new boolean[headers.length];
      for (Integer index = 0; index < headers.length; index++) {
        String fieldName = headersMap.get(headers[index].trim());
        if (fieldName == null || fieldName.equals("")) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.header", headers[index]);
          ignoreColumn[index] = true;
        } else {
          ignoreColumn[index] = false;
        }

        headers[index] = fieldName;
      }
      for (String mfield : mandatoryList) {
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
        String testId = null;
        String formatId = null;
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
            String masterValue = null;
            if (fieldName.equals("test_name")) {
              masterValue = testIdsMap.get(fieldValue.trim());
              if (null == masterValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
              fieldName = "test_id";
              testId = masterValue;
              fieldValue = masterValue;
            }
            if (fieldName.equals("ddept_name")) {
              masterValue = deptsMap.get(fieldValue.trim());
              if (null == masterValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
            }
            if (fieldName.equals("format_name")) {
              masterValue = reportFormatMap.get(fieldValue.trim());
              if (null == masterValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
              fieldValue = masterValue;
              formatId = masterValue;
            }
            if (!fieldName.endsWith("ddept_name")) {
              property = bean.getDynaClass().getDynaProperty(fieldName);
              Class<?> enforcedType = typeMap.get(fieldName);
              if (null != enforcedType) {
                if (null == ConvertUtils.convert(fieldValue, enforcedType)) {
                  addWarning(warnings, lineNumber, "exception.csv.conversion.error", fieldValue,
                      (enforcedType == BigDecimal.class ? " Number "
                          : enforcedType.getSimpleName()),
                      fieldName);
                  hasWarnings = true;
                  continue;
                }
              }
              bean.set(fieldName, ConvertUtils.convert(fieldValue, property.getType()));
            }
            nonEmptyColumnsCount++;
          } else {
            if (mandatoryList.contains(fieldName)) {
              addWarning(warnings, lineNumber, fieldName + " can not be null in the sheet",
                  fieldValue, fieldName);
              hasWarnings = true;
            } else {
              bean.set(fieldName, null);
            }
          }
        }

        if (hasWarnings || nonEmptyColumnsCount == 0) {
          continue;
        }

        try {
          if (deletedIds.get(testId) == null) {
            diagTestTemplateRepository.deleteTemplate(testId);
            deletedIds.put(testId, "");
          }
          diagTestTemplateRepository.insertTemplate(testId, formatId);
          updationCount++;
        } catch (DuplicateEntityException duplicateEntityException) {
          addWarning(warnings, lineNumber, "exception.csv.duplicate.record");
          logger.error("Duplicate record found : " + bean.get("dept_name"));
          lineWarningsCount++;
        } catch (DataAccessException dataAccessException) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.error",
              dataAccessException.getMostSpecificCause().getMessage());
          logger.error("Error uploading csv line", dataAccessException.getCause());
          lineWarningsCount++;
        } catch (ValidationException validationException) {
          for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) (Object) 
              validationException.getErrors()).entrySet()) {
            warnings.add(lineNumber, entry.getValue().get(0));
          }

          logger.error(validationException.getMessage());
        }
      }
      Integer insertionCount = 0;
      feedback.put("warnings", warnings);

      meta.add("processed_count", lineNumber - 1);
      meta.add("insertion_count", insertionCount);
      meta.add("updation_count", updationCount);
      feedback.put("result", meta);

    } catch (IOException ioException) {
      throw new InvalidFileFormatException(ioException);
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
    warning.append(msgUtil.getMessage(message, parameters));
    warnings.add(lineNumber, warning.toString());
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
    Map<String, Map<String, Object>> parsedMap = new HashMap<>();
    for (Entry<String, List<BasicDynaBean>> entry : masterData.entrySet()) {
      Map<String, Object> nameIdMap = new HashMap<>();
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

}
