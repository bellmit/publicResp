package com.insta.hms.mdm.itemtaxuploaddownloads;

import au.com.bytecode.opencsv.CSVReader;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DrgCodeItemSubGroupService extends BulkDataService {

  static Logger logger = LoggerFactory.getLogger(DrgCodeItemSubGroupService.class);
  @LazyAutowired
  MessageUtil messageUtil;
  @Autowired
  private TaxGroupService taxGroupService;
  @Autowired
  private TaxSubGroupService taxSubGroupService;
  @Autowired
  private DrgCodeItemSubGroupRepository drgCodeItemSubGroupRepository;

  public DrgCodeItemSubGroupService(DrgCodeItemSubGroupRepository repository,
      DrgCodeItemSubGroupValidator validator,
      DiagnosticsItemSubGroupCsvBulkDataEntity csvBulkDataEntity) {
    super(repository, validator, csvBulkDataEntity);

  }

  @Transactional
  @Override
  public String parseAndImportCsV(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {

    Map<String, Integer> itemGrpMap = taxGroupService.getItemGrpMap();
    Map<String, Integer> itemSubGrpMap = taxSubGroupService.getItemSubGrpMap();
    Map<String, String> drgCodeMap = drgCodeItemSubGroupRepository.getDrgCodeMap();

    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<Object, Object>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<Object, Object>();
    HashMap<String, String> headersMap = new HashMap<String, String>();

    headersMap.put("drg_code", "drg_code");
    headersMap.put("drg_description", "drg_description");
    headersMap.put("item_group_name", "item_group_name");
    headersMap.put("item_subgroup_name", "item_subgroup_name");

    try {

      // CSVBulkDataEntity csvEntity = getCSVDataEntity();
      InputStreamReader streamReader = new InputStreamReader(file.getInputStream());
      CSVReader csvReader = new CSVReader(streamReader);
      String[] headers = csvReader.readNext();

      if (!headers[0].matches("drg_code")) {
        return "Incompatible file for drg code upload";
      }

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
      String drgCode = null;

      for (Integer index = 0; index < headers.length; index++) {
        String fieldName = headersMap.get(headers[index].trim());
        if (fieldName == null) {
          addWarning(warnings, lineNumber, "exception.csv.unknown.header", headers[index]);
          ignoreColumn[index] = true;
        } else {
          ignoreColumn[index] = false;
        }

        headers[index] = fieldName;
      }
      lineNumber++;

      String[] row = null;
      List<BasicDynaBean> beans = new ArrayList<BasicDynaBean>();
      List<String> duplicateValue = new ArrayList<String>();
      String fieldName = null;
      String fieldValue = null;
      while (null != (row = csvReader.readNext())) {
        boolean hasWarnings = false;
        lineNumber++;
        String itemId = null;
        Integer itemgrpId = null;
        Integer itemsubgrpId = null;
        BasicDynaBean bean = getRepository().getBean();
        for (Integer columnIndex = 0; columnIndex < headers.length
            && columnIndex < row.length; columnIndex++) {

          if (ignoreColumn[columnIndex]) {
            continue;
          }

          fieldName = headers[columnIndex];
          fieldValue = row[columnIndex].trim();

          if ((null != fieldValue) && !(fieldValue.isEmpty())) {

            if (fieldName.equals("drg_code")) {
              drgCode = drgCodeMap.get(fieldValue);
              if (drgCode == null || drgCode.equals("")) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
                hasWarnings = true;
              } else {
                // itemId = fieldValue;
                bean.set("drg_code", drgCode);

              }

            }

            if (fieldName.equals("item_group_name")) {
              if (null != fieldValue && !fieldValue.equals("")) {
                itemgrpId = itemGrpMap.get(fieldValue);
                if (itemgrpId == null || itemgrpId.equals("")) {
                  addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                      fieldName);
                  hasWarnings = true;
                }
              }
            }

            if (fieldName.equals("item_subgroup_name") && itemgrpId != null) {
              if (null != fieldValue && !fieldValue.equals("")) {
                itemsubgrpId = itemSubGrpMap.get(fieldValue);
                if (itemsubgrpId == null || itemsubgrpId.equals("")) {
                  addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                      fieldName);
                  hasWarnings = true;
                }
                // Here check Item group name mapping with item sub group name
                BasicDynaBean records = taxSubGroupService.getSubGroupName(itemgrpId, itemsubgrpId);
                if (records != null) {
                  fieldName = "item_subgroup_id";
                  bean.set(fieldName, itemsubgrpId);
                } else {
                  addWarning(warnings, lineNumber, "exception.csv.item.does.not.match.in.the.sheet",
                      fieldValue, fieldName);
                  hasWarnings = true;
                }
              }
            }
          }
        }
        if (bean.get("drg_code") != null && bean.get("item_subgroup_id") != null) {
          // Here check duplicate values in the csv file
          if (!duplicateValue
              .contains(itemgrpId.toString() + "_" + bean.get("drg_code").toString())) {
            beans.add(bean);
            duplicateValue.add(itemgrpId.toString() + "_" + bean.get("drg_code").toString());
          } else {
            addWarning(warnings, lineNumber, "exception.csv.duplicate.record", fieldValue,
                fieldName);
            hasWarnings = true;
          }
        }

        // if (bean.get("drg_code") != null && bean.get("item_subgroup_id") != null) {
        // // Here check duplicate values in the csv file
        // if (!duplicateValue.contains(bean.get("item_subgroup_id").toString() + "_" +
        // bean.get("drg_code").toString())) {
        // beans.add(bean);
        // duplicateValue.add(bean.get("item_subgroup_id").toString() + "_" +
        // bean.get("drg_code").toString());
        // }else{
        // addWarning(warnings, lineNumber, "exception.csv.duplicate.record",
        // fieldValue, fieldName);
        // hasWarnings = true;
        // }
        // }
      }
      try {
        drgCodeItemSubGroupRepository.deleteAllRecords();
        drgCodeItemSubGroupRepository.batchInsert(beans);

      } catch (DataAccessException exception) {
        addWarning(warnings, lineNumber, "exception.csv.unknown.error",
            exception.getMostSpecificCause().getMessage());
        logger.error("Error uploading csv line", exception.getCause());
        lineWarningsCount++;
      }
      meta.add("Drg code processed Successfully", lineNumber - 1);
      feedback.put("warnings", warnings);
      feedback.put("result", meta);

    } catch (IOException exception) {
      throw new InvalidFileFormatException(exception);
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

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    // TODO Auto-generated method stub
    return null;
  }

}
