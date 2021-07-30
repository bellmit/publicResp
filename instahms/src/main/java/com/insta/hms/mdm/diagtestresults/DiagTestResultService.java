package com.insta.hms.mdm.diagtestresults;

import au.com.bytecode.opencsv.CSVReader;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.diagnosticmodule.laboratory.ResultExpressionProcessor;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.bulk.CsVBulkDataEntity;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.diagdepartments.DiagDepartmentService;
import com.insta.hms.mdm.diagmethodologies.DiagMethodologyService;
import com.insta.hms.mdm.diagnostics.DiagnosticTestService;
import com.insta.hms.mdm.diagtestresultcenters.DiagTestResultCenterService;
import com.insta.hms.mdm.hl7interfaces.Hl7interfaceService;
import com.insta.hms.mdm.testresultranges.TestResultRangeService;
import freemarker.core.InvalidReferenceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

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
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class DiagTestResultService.
 *
 * @author anil.n
 */

@Service
public class DiagTestResultService extends BulkDataService {

  /** The logger. */
  private Logger logger = LoggerFactory.getLogger(DiagTestResultService.class);

  /** The message util. */
  @LazyAutowired
  MessageUtil messageUtil;

  /** The diag test repository. */
  @LazyAutowired
  private DiagTestResultRepository diagTestRepository;

  /** The hl 7 interface service. */
  @LazyAutowired
  private Hl7interfaceService hl7interfaceService;

  /** The diag test result repository. */
  @LazyAutowired
  private DiagTestResultRepository diagTestResultRepository;

  /** The diag test result center service. */
  @LazyAutowired
  private DiagTestResultCenterService diagTestResultCenterService;

  /** The test result range service. */
  @LazyAutowired
  private TestResultRangeService testResultRangeService;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The diagnostic test service. */
  @LazyAutowired
  private DiagnosticTestService diagnosticTestService;

  /** The diag department service. */
  @LazyAutowired
  private DiagDepartmentService diagDepartmentService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The diag methodology service. */
  @LazyAutowired
  private DiagMethodologyService diagMethodologyService;

  /**
   * Instantiates a new diag test result service.
   *
   * @param repo
   *          the repo
   * @param validator
   *          the validator
   * @param entity
   *          the entity
   */
  public DiagTestResultService(DiagTestResultRepository repo, DiagTestResultValidator validator,
      DiagTestResultCsvBulkDataEntity entity) {
    super(repo, validator, entity);
  }

  /**
   * Insert test results.
   *
   * @param testId
   *          the test id
   * @param req
   *          the req
   * @param success
   *          the success
   * @param msg
   *          the msg
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean insertTestResults(String testId, HttpServletRequest req, boolean success,
      StringBuilder msg) {

    String[] units = req.getParameterValues("units");
    String[] codeType = req.getParameterValues("code_type");
    String[] resultCode = req.getParameterValues("result_code");
    String[] dataAllowed = req.getParameterValues("data_allowed");
    String[] sourceIfList = req.getParameterValues("source_if_list");
    String[] resultLabel = req.getParameterValues("resultLabel");
    String[] hl7Interface = req.getParameterValues("hl7_interface");
    String[] methodIds = req.getParameterValues("methodId");
    String[] order = req.getParameterValues("order");
    String[] resultLabelShort = req.getParameterValues("resultLabelShort");
    String[] expression = req.getParameterValues("expression");
    String[] defaultValues = req.getParameterValues("defaultValue");
    String reportGroup = req.getParameter("reportGroup");
    String resultValidation = req.getParameter("resultsValidation");

    ArrayList<Result> results = new ArrayList<Result>();
    ResultExpressionProcessor processor = new ResultExpressionProcessor();

    List<BasicDynaBean> checkBean = hl7interfaceService.lookup(true);
    Result res = null;
    if (units != null) {
      for (int i = 0; i < units.length - 1; i++) {
        String[] resultlabelIdArr = new String[units.length - 1];
        int resultlabelId = diagTestResultRepository.getNextSequence();
        resultlabelIdArr[i] = new Integer(resultlabelId).toString();
        if (checkBean != null && checkBean.size() > 0) {
          res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
              resultlabelIdArr[i], expression[i], hl7Interface[i]);
          res.setCode_type(codeType[i]);
          res.setResult_code(resultCode[i]);
          res.setDataAllowed(dataAllowed[i]);
          res.setSourceIfList(sourceIfList[i]);
          res.setHl7_interface(hl7Interface[i]);
          res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
              .parseInt(methodIds[i]) : null);
          res.setDefaultValue(defaultValues[i]);
        } else {
          res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
              resultlabelIdArr[i], expression[i]);
          res.setCode_type(codeType[i]);
          res.setResult_code(resultCode[i]);
          res.setDataAllowed(dataAllowed[i]);
          res.setSourceIfList(sourceIfList[i]);
          res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
              .parseInt(methodIds[i]) : null);
          res.setDefaultValue(defaultValues[i]);
        }
        results.add(res);
      }
    }
    Iterator<Result> it = results.iterator();
    if (null != reportGroup && reportGroup.equals("V") && it.hasNext()) {
      while (it.hasNext()) {
        Result rs = it.next();
        res.setTestId(testId);
        Integer resultLabelId = Integer.parseInt(rs.getResultlabel_id());

        if (!res.getExpression().isEmpty()) {
          List<BasicDynaBean> resultsMaster = diagTestResultRepository.getResultsList(testId);
          processor.setValidExpr(processor.istExpressionValid(resultsMaster, res.getTestId(),
              res.getExpression()));
          success = processor.isValidExpr();

          if (!success) {
            msg = msg.append("The expression for result : " + res.getResultLabel() + " is "
                + res.getExpression() + " ," + "which is not a valid expression.");
            return false;
          }
        }
      }
      success &= diagTestResultRepository.insertResults(results);
      if (success) {
        success = diagTestResultCenterService.insertTestResultCenters(results);
      }

      if (null != reportGroup && reportGroup.equals("V")) {
        List<BasicDynaBean> resultsMaster = diagTestResultRepository.getResultsList(testId);

        processor.setValidExpr(processor
            .istExpressionValid(resultsMaster, testId, resultValidation));

        success = processor.isValidExpr();
        if (!success) {
          msg = msg.append("The expression for Results Validation: " + resultValidation
              + " is not a valid expression.");
          return false;
        }
      }
    }
    return success;
  }

  /**
   * Gets the test results.
   *
   * @param testId
   *          the test id
   * @return the test results
   */
  public List<BasicDynaBean> getTestResults(String testId) {
    return diagTestResultRepository.getTestResultsList(testId);
  }

  /**
   * List all test result references.
   *
   * @param testId
   *          the test id
   * @return the list
   */
  public List<BasicDynaBean> listAllTestResultReferences(String testId) {
    return diagTestResultRepository.listAllTestResultReferences(testId);
  }

  /**
   * Gets the results list for json.
   *
   * @param testId
   *          the test id
   * @return the results list for json
   */
  public List<BasicDynaBean> getResultsListForJson(String testId) {
    return diagTestResultRepository.getResultsListForJson(testId);
  }

  /**
   * Update test results.
   *
   * @param testId
   *          the test id
   * @param req
   *          the req
   * @param success
   *          the success
   * @param msg
   *          the msg
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateTestResults(String testId, HttpServletRequest req, boolean success,
      StringBuilder msg) {
    String[] units = req.getParameterValues("units");
    String[] resultLableId = req.getParameterValues("resultlabel_id");
    String[] codeType = req.getParameterValues("code_type");
    String[] resultCode = req.getParameterValues("result_code");
    String[] dataAllowed = req.getParameterValues("data_allowed");
    String[] sourceIfList = req.getParameterValues("source_if_list");
    String[] resultLabel = req.getParameterValues("resultLabel");
    String[] hl7Interface = req.getParameterValues("hl7_interface");
    String[] methodIds = req.getParameterValues("methodId");
    String[] order = req.getParameterValues("order");
    String[] resultLabelShort = req.getParameterValues("resultLabelShort");
    String[] expression = req.getParameterValues("expression");
    String[] defaultValues = req.getParameterValues("defaultValue");
    String[] resultOp = req.getParameterValues("resultOp");
    String reportGroup = req.getParameter("reportGroup");
    String resultValidation = req.getParameter("resultsValidation");

    ArrayList<Result> results = new ArrayList<Result>();
    ArrayList<Result> modifiedList = new ArrayList<Result>();
    ArrayList<Result> deletedResult = new ArrayList<Result>();
    ResultExpressionProcessor processor = new ResultExpressionProcessor();

    List<BasicDynaBean> checkBean = hl7interfaceService.lookup(true);
    Result res = null;
    if (resultOp != null) {
      for (int i = 0; i < resultOp.length; i++) {
        String op = resultOp[i];
        if (op.equals("add")) {
          String[] resultlabelIdArr = new String[resultOp.length];
          int resultlabelId = diagTestResultRepository.getNextSequence();
          resultlabelIdArr[i] = new Integer(resultlabelId).toString();
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
                resultlabelIdArr[i], expression[i], hl7Interface[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceIfList[i]);
            res.setHl7_interface(hl7Interface[i]);
            res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
                .parseInt(methodIds[i]) : null);
            res.setDefaultValue(defaultValues[i]);
          } else {
            res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
                resultlabelIdArr[i], expression[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceIfList[i]);
            res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
                .parseInt(methodIds[i]) : null);
            res.setDefaultValue(defaultValues[i]);
          }
          results.add(res);
        } else if (op.equals("mod")) {
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
                resultLableId[i], expression[i], hl7Interface[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceIfList[i]);
            res.setHl7_interface(hl7Interface[i]);
            res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
                .parseInt(methodIds[i]) : null);
            res.setDefaultValue(defaultValues[i]);
          } else {
            res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
                resultLableId[i], expression[i]);
            res.setCode_type(codeType[i]);
            res.setResult_code(resultCode[i]);
            res.setDataAllowed(dataAllowed[i]);
            res.setSourceIfList(sourceIfList[i]);
            res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
                .parseInt(methodIds[i]) : null);
            res.setDefaultValue(defaultValues[i]);
          }
          modifiedList.add(res);
        } else if (op.equals("del")) {
          if (checkBean != null && checkBean.size() > 0) {
            res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
                resultLableId[i], expression[i], hl7Interface[i]);
            res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
                .parseInt(methodIds[i]) : null);

          } else {
            res = new Result(testId, resultLabel[i], resultLabelShort[i], units[i], order[i],
                resultLableId[i], expression[i]);

            res.setMethodId((methodIds[i] != null && !methodIds[i].equals("")) ? Integer
                .parseInt(methodIds[i]) : null);
          }
          deletedResult.add(res);
        }
      }
    }

    if (null != reportGroup && reportGroup.equals("V")) {
      if (!resultValidation.isEmpty()) {
        List<BasicDynaBean> resultsMaster = diagTestResultRepository.getResultsList(testId);
        processor.setValidExpr(processor.istExpressionValid(resultsMaster, res.getTestId(),
            resultValidation));
        success = processor.isValidExpr();
        if (!success) {
          msg = msg.append("The expression for Results Validation : " + resultValidation
              + " is not valid.");
          return false;
        }
      }
    }

    if (null != reportGroup && reportGroup.equals("V")) {

      success &= diagTestResultRepository.updateResults(modifiedList);

      success &= diagTestResultRepository.deleteResults(deletedResult);
      /* result range with out a result label can not exist */
      success &= testResultRangeService.deleteResultRanges(deletedResult);
      success &= diagTestResultCenterService.deleteResultsCenter(deletedResult, success);

      success &= diagTestRepository.insertResults(results);
      success &= diagTestResultCenterService.insertTestResultCenters(results);

      Iterator<Result> it = modifiedList.iterator();
      Result rs = null;

      while (it.hasNext()) {
        rs = it.next();
        List centersList = new ArrayList();
        Integer resultLabelId = Integer.parseInt(res.getResultlabel_id());
        centersList = diagTestResultRepository.centersForResults(rs.getTestId(), resultLabelId);
        if (!res.getExpression().isEmpty()) {
          List<BasicDynaBean> resultsMaster = diagTestResultRepository.getResultsList(testId);
          processor.setValidExpr(processor.istExpressionValid(resultsMaster, res.getTestId(),
              res.getExpression()));
          success = processor.isValidExpr();
          if (!success) {
            msg = msg.append("The expression for result : " + res.getResultLabel() + " is "
                + res.getExpression() + " ," + "which is not a valid expression.");
            return false;
          }
        }
      }
      it = results.iterator();
      while (it.hasNext()) {
        res = it.next();
        List<Integer> centersList = new ArrayList<Integer>();
        centersList.add(RequestContext.getCenterId());
        if (!res.getExpression().isEmpty()) {
          List<BasicDynaBean> resultsMaster = diagTestResultRepository.getResultsList(testId);
          processor.setValidExpr(processor.istExpressionValid(resultsMaster, res.getTestId(),
              res.getExpression()));
          success = processor.isValidExpr();

          if (!success) {
            msg = msg.append("The expression for the result: " + res.getResultLabel() + " is "
                + res.getExpression() + " ," + " which is not a valid expression.");
            return false;
          }
        }
      }
    }
    return success;
  }

  /*
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
    return (Integer) genericPreferencesService.getPreferences().get("max_centers_inc_default");
  }


  /**
   * Parses the and import csv.
   *
   * @param file the file
   * @param feedback the feedback
   * @return the string
   */
  @SuppressWarnings("unchecked")
  @Override
  public String parseAndImportCsV(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {

    boolean hasErrors = false;
    List<BasicDynaBean> rows = new ArrayList<BasicDynaBean>();
    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<Object, Object>();
    Map centerMap = centerService.getAllCentersMap();
    List<BasicDynaBean> methodList = diagMethodologyService.lookup(true);
    Map<String, Integer> methodlogyMap = new HashMap<String, Integer>();
    for (BasicDynaBean mothodBean : methodList) {
      methodlogyMap.put((String) mothodBean.get("method_name"),
          (Integer) mothodBean.get("method_id"));
    }
    int maxCentersIncDefault = (Integer) genericPreferencesService.getPreferences().get(
        "max_centers_inc_default");
    List<BasicDynaBean> centerIdsList = null;
    List<String> columns = null;
    if (maxCentersIncDefault > 1) {
      columns = Arrays.asList(new String[] { "resultlabel_id", "test_name", "ddept_name",
          "resultlabel", "center_name", "method_name", "units", "display_order" });
    } else {
      columns = Arrays.asList(new String[] { "resultlabel_id", "test_name", "ddept_name",
          "resultlabel", "method_name", "units", "display_order" });
    }
    Map<String, String> headersMap = new HashMap<String, String>();
    headersMap.put("resultlabel_id", "resultlabel_id");
    headersMap.put("Test Name", "test_name");
    headersMap.put("Dept Name", "ddept_name");
    headersMap.put("Result label", "resultlabel");
    if (maxCentersIncDefault > 1) {
      headersMap.put("Center", "center_name");
    }
    headersMap.put("Methodology", "method_name");
    headersMap.put("Units", "units");
    headersMap.put("Display Order", "display_order");
    List<String> mandatoryFields = null;
    if (maxCentersIncDefault > 1) {
      mandatoryFields = Arrays.asList(new String[] { "test_name", "ddept_name", "center_name" });
    } else {
      mandatoryFields = Arrays.asList(new String[] { "test_name", "ddept_name" });
    }
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
      Integer insertionCount = 0;
      Integer updationCount = 0;
      CsVBulkDataEntity csvEntity = getCsVDataEntity();
      Map<String, Class<?>> typeMap = csvEntity.getTypeMap();

      for (Integer index = 0; index < headers.length; index++) {
        String fieldName = headersMap.get(headers[index].trim());
        if (!columns.contains(fieldName)) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.header", headers[index]);
          ignoreColumn[index] = true;
        } else {
          ignoreColumn[index] = false;
        }

        headers[index] = fieldName;
      }
      for (String mfield : mandatoryFields) {
        if (!Arrays.asList(headers).contains(mfield)) {
          addWarning(warnings, lineNumber, "Mandatory field " + mfield
              + " is missing cannot process further in the sheet ", mfield);
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
        String operation = "update";
        List<String> savedCenters = new ArrayList<String>();
        List<String> insertCenterList = new ArrayList<String>();
        Double itemId = null;
        String testId = null;

        for (Integer columnIndex = 0; columnIndex < headers.length
            && columnIndex < row.length; columnIndex++) {
          if (ignoreColumn[columnIndex]) {
            continue;
          }

          String fieldName = headers[columnIndex];
          String fieldValue = row[columnIndex].trim();
          DynaProperty property;

          if ((null != fieldValue && !fieldValue.isEmpty())
              || headers[columnIndex].equals("resultlabel_id")) {
            if (headers[columnIndex].equals("resultlabel_id")) {
              if (fieldValue == null || fieldValue.equals("")) {
                operation = "insert";
              } else {
                itemId = new Double(fieldValue);
              }
            }
            if (headers[columnIndex].equals("test_name")) {
              Map testMaps = diagnosticTestService.getTestNamesAndIds();
              String masterValue = (String) testMaps.get(fieldValue.trim());
              if (null == masterValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
              fieldName = "test_id";
              fieldValue = masterValue;
              testId = masterValue;
            }
            if (headers[columnIndex].equals("ddept_name")) {
              Map deptMap = diagDepartmentService.getDiagDepartmentsMap();
              String masterValue = (String) deptMap.get(fieldValue.trim());
              if (null == masterValue) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              }
            }
            if (headers[columnIndex].equals("center_name")) {
              Map centersMap = centerService.getAllCentersMap();
              String[] centerNames = fieldValue.split(",");
              for (int i = 0; i < centerNames.length; i++) {
                Integer masterValue = (Integer) centersMap.get(centerNames[i].trim());
                if (null == masterValue) {
                  addWarning(warnings, lineNumber, "exception.csv.unknown.master.value",
                      centerNames[i], fieldName);
                  hasWarnings = true;
                }
              }
              centerIdsList = centerService.getCenterDetailsList(centerNames);
              if (operation.equals("update")) {
                savedCenters = centerService.getSavedCenters(itemId.intValue());
                for (int i = 0; i < centerNames.length; i++) {
                  if (centerMap.get(centerNames[i].trim()) != null) {
                    if (savedCenters.contains(centerNames[i].trim())) {
                      savedCenters.remove(centerNames[i].trim());
                    } else {
                      if (!insertCenterList.contains(centerNames[i].trim())) {
                        insertCenterList.add(centerNames[i].trim());
                      }
                    }
                  }
                }
              }
            }
            if (headers[columnIndex].equals("method_name")) {
              Integer masterValue = null;
              if (fieldValue != null && !fieldValue.equals("")) {
                masterValue = methodlogyMap.get(fieldValue);
                if (null == masterValue) {
                  addWarning(warnings, lineNumber, "exception.csv.unknown.master.value",
                      fieldValue, fieldName);
                  hasWarnings = true;
                }
              }
              fieldName = "method_id";
              fieldValue = masterValue != null ? masterValue.toString() : null;
            }
            if (!headers[columnIndex].equals("ddept_name")
                && !headers[columnIndex].equals("center_name")) {
              property = bean.getDynaClass().getDynaProperty(fieldName);
              Class<?> enforcedType = typeMap.get(fieldName);
              if (null != enforcedType) {
                if (null == ConvertUtils.convert(fieldValue, enforcedType)) {
                  addWarning(
                      warnings,
                      lineNumber,
                      "exception.csv.conversion.error",
                      fieldValue,
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
            if (mandatoryFields.contains(fieldName)) {
              addWarning(warnings, lineNumber, fieldName + " can not be null in the sheet",
                  fieldValue, fieldName);
              hasWarnings = true;
            } else if (headers[columnIndex].equals("method_name")) {
              bean.set("method_id", null);
            } else {
              bean.set(fieldName, null);
            }
          }
        }
        List<BasicDynaBean> existResultLabel = diagTestResultRepository.getExistingResultLabel(
            testId, (String) bean.get("resultlabel"), bean.get("method_id"));
        if (operation.equals("insert") && existResultLabel != null
            && existResultLabel.size() != 0) {
          addWarning(warnings, lineNumber, "exception.csv.duplicate.record");
          hasWarnings = true;
        }
        if (hasWarnings || nonEmptyColumnsCount == 0) {
          continue;
        }

        try {
          boolean success = true;
          if (operation.equals("update")) {
            Map keys = new HashMap<String, Object>();
            keys.put("test_id", testId);
            keys.put("resultlabel_id", itemId.intValue());
            success = super.update(bean) > 0;

            if (success) {
              updationCount++;
              for (int i = 0; i < insertCenterList.size(); i++) {
                BasicDynaBean resultCenterBean = diagTestResultCenterService.getBeanRepository();
                resultCenterBean.set("resultlabel_id", itemId.intValue());
                resultCenterBean.set("center_id",
                    Integer.parseInt(centerMap.get(insertCenterList.get(i)).toString()));
                resultCenterBean.set("status", "A");
                success = diagTestResultCenterService.insert(resultCenterBean) > 0;
              }
              if (savedCenters != null) {
                for (int i = 0; i < savedCenters.size(); i++) {
                  BasicDynaBean resultCenterBean = diagTestResultCenterService.getBeanRepository();
                  resultCenterBean.set("resultlabel_id", itemId.intValue());
                  resultCenterBean.set("center_id",
                      Integer.parseInt(centerMap.get(savedCenters.get(i)).toString()));

                  success = diagTestResultCenterService.deleteCenters(resultCenterBean) > 0;
                }
              }
            }

          } else {
            Integer labelId = this.diagTestResultRepository.getNextSequence();
            bean.set("resultlabel_id", labelId.intValue());
            bean.set("test_id", testId);
            success = diagTestResultRepository.insertTestResultsCsv(bean) > 0;

            if (success) {
              if (maxCentersIncDefault > 1) {
                for (int i = 0; i < centerIdsList.size(); i++) {
                  BasicDynaBean resultCenterIdBean = centerIdsList.get(i);
                  int centerId = (Integer) resultCenterIdBean.get("center_id");
                  BasicDynaBean resultCenterBean = diagTestResultCenterService.getBeanRepository();
                  resultCenterBean.set("resultlabel_id", labelId);
                  resultCenterBean.set("center_id", centerId);
                  resultCenterBean.set("status", "A");
                  success = diagTestResultCenterService.insert(resultCenterBean) > 0;
                }
              } else {
                BasicDynaBean resultCenterBean = diagTestResultCenterService.getBeanRepository();
                resultCenterBean.set("resultlabel_id", labelId);
                resultCenterBean.set("center_id", 0);
                resultCenterBean.set("status", "A");
                success = diagTestResultCenterService.insert(resultCenterBean) > 0;
              }
            }
            if (success) {
              insertionCount++;
            }
          }
        } catch (DuplicateEntityException ex) {
          addWarning(warnings, lineNumber, "exception.csv.duplicate.record");
          logger.error("Duplicate record found : " + bean.get("dept_name"));
          lineWarningsCount++;
        } catch (DataAccessException ex) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.error", ex.getMostSpecificCause()
              .getMessage());
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
      meta.add("insertion_count", insertionCount);
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

  /**
   * Results center applicability check.
   *
   * @param testId
   *          the test ID
   * @param msg
   *          the msg
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean resultsCenterApplicabilityCheck(String testId, StringBuilder msg) {
    boolean isValid = true;
    List<BasicDynaBean> resultList = diagTestResultRepository.getResultsList(testId);
    for (int i = 0; i < resultList.size(); i++) {
      BasicDynaBean bean = resultList.get(i);
      if (null != bean.get("expr_4_calc_result") && !bean.get("expr_4_calc_result").equals("")) {
        isValid = istExpressionValid(testId, (String) bean.get("expr_4_calc_result"),
            (Integer) bean.get("center_id"));
        if (!isValid) {
          msg = msg.append("The result lables which is involved in the expression : "
              + (String) bean.get("expr_4_calc_result") + " OR the resullabel: "
              + (String) bean.get("resultlabel") + " are mapped to different centers.");
          break;
        }
      }
    }
    return isValid;
  }

  /**
   * Checks if is t expression valid.
   *
   * @param testId
   *          the test id
   * @param expression
   *          the expression
   * @param centerId
   *          the center ID
   * @return true, if is t expression valid
   */
  public boolean istExpressionValid(String testId, String expression, int centerId) {

    boolean valid = false;
    List<BasicDynaBean> resultsMaster = diagTestResultRepository.getResultsListForExpr(testId,
        centerId);
    BasicDynaBean resultMasterBean = null;
    StringWriter writer = new StringWriter();

    try {
      expression = "<#setting number_format=\"##.##\">\n" + expression;
      HashMap<String, Object> resultParams = new HashMap<String, Object>();
      Map<String, Object> results = new HashMap<String, Object>();
      List values = new ArrayList();
      for (int i = 0; i < resultsMaster.size(); i++) {
        resultMasterBean = resultsMaster.get(i);
        resultParams.put((String) resultMasterBean.get("resultlabel"), 1);
        values.add(1);
      }
      results.put("results", resultParams);
      results.put("values", values);
      Template expressionTemplate = new Template("expression", new StringReader(expression),
          new Configuration());

      expressionTemplate.process(results, writer);
    } catch (InvalidReferenceException ine) {
      logger.error("", ine);
      return false;
    } catch (TemplateException ex) {
      logger.error("", ex);
      return false;
    } catch (freemarker.core.ParseException ex) {
      logger.error("", ex);
      return false;
    } catch (ArithmeticException ex) {
      logger.error("", ex);
      return false;
    } catch (Exception ex) {
      logger.error("", ex);
      return false;
    }
    valid = !writer.toString().contains("[^.\\d]");

    try {
      if (!writer.toString().trim().isEmpty()) {
        BigDecimal validNumber = new BigDecimal(writer.toString());
      }
    } catch (NumberFormatException ne) {
      logger.error("", ne);
      valid = false;
    }
    return valid;
  }

  /**
   * Gets the results for equipment.
   *
   * @return the results for equipment
   */
  public List<BasicDynaBean> getResultsForEquipment() {
    return diagTestResultRepository.getResultsForEquipment();
  }
}
