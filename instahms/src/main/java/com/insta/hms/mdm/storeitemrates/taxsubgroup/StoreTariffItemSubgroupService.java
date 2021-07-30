package com.insta.hms.mdm.storeitemrates.taxsubgroup;

import au.com.bytecode.opencsv.CSVReader;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.InvalidFileFormatException;
import com.insta.hms.mdm.bulk.BulkDataService;
import com.insta.hms.mdm.storesrateplanmaster.StoresRatePlanService;
import com.insta.hms.mdm.taxgroups.TaxGroupService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



/**
 * The Class StoreTariffItemSubgroupService.
 */

@Service
public class StoreTariffItemSubgroupService extends BulkDataService {
  
  static Logger logger = LoggerFactory.getLogger(StoreTariffItemSubgroupService.class);
  
  private static final String ITEM_ID = "item_id";
  private static final String STORE_RATE_PLAN_ID = "store_rate_plan_id";
  private static final String STORE_RATE_PLAN_NAME = "store_rate_plan_name";
  private static final String MEDICINE_ID = "medicine_id";
  private static final String ITEM_GROUP_NAME = "item_group_name";
  private static final String ITEM_SUBGROUP_NAME = "item_subgroup_name";
  
  @LazyAutowired
  private TaxGroupService taxGroupService;
  @LazyAutowired
  private TaxSubGroupService taxSubGroupService;
  @LazyAutowired 
  private StoresRatePlanService storesRatePlanService;
  @LazyAutowired
  private StoreTariffItemSubgroupRepository storeTariffItemSubgroupRepository;

  /**
   * Instantiates a new store item rates service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   * @param csvBulkdataEntity
   *          the csvBulkdataEntity
   */
  public StoreTariffItemSubgroupService(StoreTariffItemSubgroupRepository repository,
      StoreTariffItemSubgroupValidator validator,
      StoreTariffItemSubgroupCsvBulkDataEntity csvBulkdataEntity) {
    super(repository, validator, csvBulkdataEntity);
  }

  /**find by key.
   * @param itemId the itemId
   * @param storeTariffId the storeTariffId
   * @return basic dyna bean
   */
  public BasicDynaBean findByKey(Integer itemId, Integer storeTariffId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(ITEM_ID, itemId);
    filterMap.put(STORE_RATE_PLAN_ID, storeTariffId);
    return ((StoreTariffItemSubgroupRepository) getRepository()).findByKey(filterMap);
  }

  @Override
  public Map<String, List<BasicDynaBean>> getMasterData() {
    return null;
  }
  
  public BasicDynaBean getBean() {
    return getRepository().getBean();
  }
  
  @Transactional
  @Override
  public String parseAndImportCsV(MultipartFile file,
      Map<String, MultiValueMap<Object, Object>> feedback) {

    Map<String, Integer> itemGrpMap = taxGroupService.getItemGrpMap();
    Map<String, Integer> itemSubGrpMap = taxSubGroupService.getItemSubGrpMap();
    Map<String, String> storeMap = storeTariffItemSubgroupRepository.getStoreItemMap();
    Map<Integer, BasicDynaBean> storeRatePlanMap = storesRatePlanService.getStoreRatePlanMap();
    

    MultiValueMap<Object, Object> warnings = new LinkedMultiValueMap<>();
    MultiValueMap<Object, Object> meta = new LinkedMultiValueMap<>();
    HashMap<String, String> headersMap = new HashMap<>();

    headersMap.put(MEDICINE_ID, MEDICINE_ID);
    headersMap.put("medicine_name", "medicine_name");
    headersMap.put(STORE_RATE_PLAN_ID, STORE_RATE_PLAN_ID);
    headersMap.put(STORE_RATE_PLAN_NAME, STORE_RATE_PLAN_NAME);
    headersMap.put(ITEM_GROUP_NAME, ITEM_GROUP_NAME);
    headersMap.put(ITEM_SUBGROUP_NAME, ITEM_SUBGROUP_NAME);

    try {
      InputStreamReader streamReader = new InputStreamReader(file.getInputStream());
      CSVReader csvReader = new CSVReader(streamReader);
      String[] headers = csvReader.readNext();

      if (!headers[0].matches(MEDICINE_ID)) {
        return "Incompatible file for store item's tariff tax subgroup upload";
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
      List<BasicDynaBean> beans = new ArrayList<>();
      List<String> duplicateValue = new ArrayList<>();
      String fieldName = null;
      String fieldValue = null;
      Set<Integer> storeRatePlans = new HashSet<>();
      while (null != (row = csvReader.readNext())) {
        lineNumber++;
        String itemId = null;
        Integer itemgrpId = null;
        Integer itemsubgrpId = null;
        Integer storeRatePlanId = null;
        BasicDynaBean storeRatePlanBean = null;
        BasicDynaBean bean = getRepository().getBean();
        for (Integer columnIndex = 0; columnIndex < headers.length
            && columnIndex < row.length; columnIndex++) {

          if (ignoreColumn[columnIndex]) {
            continue;
          }

          fieldName = headers[columnIndex];
          fieldValue = row[columnIndex].trim();

          if ((null != fieldValue) && !(fieldValue.isEmpty()) || fieldName.equals(MEDICINE_ID)) {

            if (fieldName.equals(MEDICINE_ID)) {
              itemId = storeMap.get(fieldValue);
              if (itemId == null || itemId.equals("")) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
              } else {
                int medicineId = Integer.parseInt(itemId);
                bean.set(ITEM_ID, medicineId);

              }

            }

            if (fieldName.equals(ITEM_GROUP_NAME) && null != fieldValue
                && !fieldValue.equals("")) {
              itemgrpId = itemGrpMap.get(fieldValue);
              if (itemgrpId == null || itemgrpId.equals("")) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
              }

            }

            if (fieldName.equals(ITEM_SUBGROUP_NAME) && itemgrpId != null && null != fieldValue
                && !fieldValue.equals("")) {
              itemsubgrpId = itemSubGrpMap.get(fieldValue);
              if (itemsubgrpId == null || itemsubgrpId.equals("")) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
              }
              // Here check Item group name mapping with item sub group name
              BasicDynaBean records = taxSubGroupService.getSubGroupName(itemgrpId, itemsubgrpId);
              if (records != null) {
                fieldName = "item_subgroup_id";
                bean.set(fieldName, itemsubgrpId);
              } else {
                addWarning(warnings, lineNumber, "exception.csv.item.does.not.match.in.the.sheet",
                    fieldValue, fieldName);
              }
            }
            
            if (fieldName.equals(STORE_RATE_PLAN_ID) && null != fieldValue
                && !fieldValue.equals("")) {
              storeRatePlanBean = storeRatePlanMap.get(Integer.parseInt(fieldValue));
              if (storeRatePlanBean == null) {
                addWarning(warnings, lineNumber, "exception.csv.unknown.master.value", fieldValue,
                    fieldName);
              }
            }
            
            if (fieldName.equals(STORE_RATE_PLAN_NAME) && null != fieldValue
                && !fieldValue.equals("") && storeRatePlanBean != null) {
              String storeRatePlanName = (String) storeRatePlanBean.get(STORE_RATE_PLAN_NAME);
              if (fieldValue.equalsIgnoreCase(storeRatePlanName)) {
                storeRatePlanId = (Integer) storeRatePlanBean.get(STORE_RATE_PLAN_ID);
                storeRatePlans.add(storeRatePlanId);
                bean.set(STORE_RATE_PLAN_ID, storeRatePlanId);
              } else {
                addWarning(warnings, lineNumber, "exception.csv.store.rate.plan.check.failure",
                    STORE_RATE_PLAN_ID, fieldName);
              }
            }     

          }
        }

        if (bean.get(ITEM_ID) != null && bean.get("item_subgroup_id") != null
            && bean.get(STORE_RATE_PLAN_ID) != null) {
          // Here check duplicate values in the csv file
          if (!duplicateValue.contains(itemgrpId.toString() + "_" + bean.get(ITEM_ID).toString()
              + "_" + storeRatePlanId.toString())) {
            beans.add(bean);
            duplicateValue.add(itemgrpId.toString() + "_" + bean.get(ITEM_ID).toString() + "_"
                + storeRatePlanId.toString());
          } else {
            addWarning(warnings, lineNumber, "exception.csv.duplicate.record", fieldValue,
                fieldName);
          }
        }
      }
      try {
        storeTariffItemSubgroupRepository.batchDelete(STORE_RATE_PLAN_ID,
            new ArrayList<Object>(storeRatePlans));
        storeTariffItemSubgroupRepository.batchInsert(beans);

      } catch (DataAccessException exception) {
        addWarning(warnings, lineNumber, "exception.csv.unknown.error",
            exception.getMostSpecificCause().getMessage());
        logger.error("Error uploading csv line", exception.getCause());
      }
      meta.add("Store items tariff tax subgroup processed Successfully", lineNumber - 1);
      feedback.put("warnings", warnings);
      feedback.put("result", meta);

    } catch (IOException exception) {
      throw new InvalidFileFormatException(exception);
    }

    return null;

  }
  
  public Integer delete(String key, Object value) {
    return getRepository().delete(key, value);
  }
  
  public int[] batchInsert(List<BasicDynaBean> beans) {
    return getRepository().batchInsert(beans);
  }

}
