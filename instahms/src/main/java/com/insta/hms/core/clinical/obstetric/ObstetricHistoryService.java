package com.insta.hms.core.clinical.obstetric;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ObstetricHistoryService.
 *
 * @author anupvishwas
 */

@Service
public class ObstetricHistoryService extends SystemSectionService {

  /** The obstetric head repo. */
  @LazyAutowired
  private ObstetricHeadRepository obstetricHeadRepo;

  /** The obstetric history repo. */
  @LazyAutowired
  private ObstetricHistoryRepository obstetricHistoryRepo;

  /** The obs head records validator. */
  @LazyAutowired
  private ObstetricHeadRecordsValidator obsHeadRecordsValidator;

  /** The obs history validator. */
  @LazyAutowired
  private ObstetricHistoryValidator obsHistoryValidator;

  /** The Constant SECTION_INSERT. */
  private static final String SECTION_INSERT = "insert";

  /** The Constant SECTION_UPDATE. */
  private static final String SECTION_UPDATE = "update";

  /** The Constant SECTION_DELETE. */
  private static final String SECTION_DELETE = "delete";

  /** The Constant SECTION_DETAIL_ID. */
  private static final String SECTION_DETAIL_ID = "section_detail_id";

  /** The Constant OBSTETRIC_RECORD_ID. */
  private static final String OBSTETRIC_RECORD_ID = "obstetric_record_id";

  /** The Constant PREGNANCY_HISTORY_ID. */
  private static final String PREGNANCY_HISTORY_ID = "pregnancy_history_id";

  /** The Constant MOD_TIME. */
  private static final String MOD_TIME = "mod_time";

  /** The Constant HEAD. */
  private static final String HEAD = "head";

  /** The Constant USER_NAME. */
  private static final String USER_NAME = "username";

  /** The Constant RECORDS. */
  private static final String RECORDS = "records";

  /**
   * Instantiates a new obstetric history service.
   */
  public ObstetricHistoryService() {
    this.sectionId = -13;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#saveSection(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean, com.insta.hms.core.clinical.forms.FormParameter,
   * java.util.Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = true;
    List<BasicDynaBean> insertObsHeadRecordsList = new ArrayList<>();
    List<BasicDynaBean> updateObsHeadRecordsList = new ArrayList<>();
    Map<String, Object> updateObsHeadRecordKeysMap = new HashMap<>();
    List<Object> updateHeadRecordKeys = new ArrayList<>();

    List<BasicDynaBean> insertObsHistoryList = new ArrayList<>();
    List<BasicDynaBean> updateObsHistoryList = new ArrayList<>();
    Map<String, Object> updateObsHistoryKeysMap = new HashMap<>();
    List<Object> updateObsHistoryKeys = new ArrayList<>();
    List<Object> deleteObsHistoryKeys = new ArrayList<>();

    String userName = (String) RequestContext.getSession().getAttribute("userId");
    Map<String, Object> responseData = new HashMap<>();

    if (requestBody.get(HEAD) != null
        && !((List<Map<String, Object>>) requestBody.get(HEAD)).isEmpty()) {
      responseData.put("head", new HashMap<String, Object>());
      for (Map<String, Object> headRow : (List<Map<String, Object>>) requestBody.get(HEAD)) {
        if (headRow.get(SECTION_INSERT) != null
            && !((List<Map<String, Object>>) headRow.get(SECTION_INSERT)).isEmpty()) {
          ((Map<String, Object>) responseData.get(HEAD)).put(SECTION_INSERT,
              new HashMap<String, Object>());
          Integer recordIndex = 0;
          for (Map<String, Object> row : (List<Map<String, Object>>) headRow.get(SECTION_INSERT)) {
            BasicDynaBean obstetricHeadBean = obstetricHeadRepo.getBean();
            List<String> conversionErrorList = new ArrayList<>();
            errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
            ConversionUtils.copyJsonToDynaBean(row, obstetricHeadBean, conversionErrorList, false);
            obstetricHeadBean.set(OBSTETRIC_RECORD_ID, obstetricHeadRepo.getNextSequence());
            obstetricHeadBean.set(SECTION_DETAIL_ID, sdbean.get(SECTION_DETAIL_ID));
            obstetricHeadBean.set(MOD_TIME, new java.sql.Timestamp(new java.util.Date().getTime()));
            obstetricHeadBean.set(USER_NAME, userName);
            insertObsHeadRecordsList.add(obstetricHeadBean);
            if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
              if (!errorMap.containsKey(SECTION_INSERT)) {
                errorMap.put(SECTION_INSERT, new HashMap<String, Object>());
              }
              ((Map<String, Object>) errorMap.get(SECTION_INSERT)).put(
                  (recordIndex).toString(),
                  (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                      conversionErrorList))).getErrors());
            }
            Map<String, Object> record = new HashMap<>();
            record.put(OBSTETRIC_RECORD_ID, obstetricHeadBean.get(OBSTETRIC_RECORD_ID));
            ((Map<String, Object>) ((Map<String, Object>) responseData.get("head"))
                .get(SECTION_INSERT)).put(recordIndex.toString(), record);
            recordIndex++;
          }
        }
        if (headRow.get(SECTION_UPDATE) != null
            && !((List<Map<String, Object>>) headRow.get(SECTION_UPDATE)).isEmpty()) {
          ((Map<String, Object>) responseData.get(HEAD)).put(SECTION_UPDATE,
              new HashMap<String, Object>());
          Integer recordIndex = 0;
          for (Map<String, Object> row : (List<Map<String, Object>>) headRow.get(SECTION_UPDATE)) {
            BasicDynaBean obstetricHeadBean = obstetricHeadRepo.getBean();
            List<String> conversionErrorList = new ArrayList<>();
            errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
            ConversionUtils.copyJsonToDynaBean(row, obstetricHeadBean, conversionErrorList, false);
            obstetricHeadBean.set(MOD_TIME, new java.sql.Timestamp(new java.util.Date().getTime()));
            obstetricHeadBean.set(USER_NAME, userName);
            isValid = obsHeadRecordsValidator.validateObsHeadRecordsUpdate(obstetricHeadBean,
                errMap) && isValid;
            updateObsHeadRecordsList.add(obstetricHeadBean);
            updateHeadRecordKeys.add(row.get(OBSTETRIC_RECORD_ID));
            if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
              if (!errorMap.containsKey(SECTION_UPDATE)) {
                errorMap.put(SECTION_UPDATE, new HashMap<String, Object>());
              }
              ((Map<String, Object>) errorMap.get(SECTION_UPDATE)).put(
                  (recordIndex).toString(),
                  (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                      conversionErrorList))).getErrors());
            }
            Map<String, Object> record = new HashMap<>();
            record.put(OBSTETRIC_RECORD_ID, obstetricHeadBean.get(OBSTETRIC_RECORD_ID));
            ((Map<String, Object>) ((Map<String, Object>) responseData.get(HEAD))
                .get(SECTION_UPDATE)).put(recordIndex.toString(), record);
            recordIndex++;
          }
          updateObsHeadRecordKeysMap.put(OBSTETRIC_RECORD_ID, updateHeadRecordKeys);
        }
      }
    }

    if (requestBody.get(RECORDS) != null
        && !((List<Map<String, Object>>) requestBody.get(RECORDS)).isEmpty()) {
      responseData.put(RECORDS, new HashMap<String, Object>());
      for (Map<String, Object> historyRow : (List<Map<String, Object>>) requestBody.get(RECORDS)) {
        if (historyRow.get(SECTION_INSERT) != null
            && !((List<Map<String, Object>>) historyRow.get(SECTION_INSERT)).isEmpty()) {
          ((Map<String, Object>) responseData.get(RECORDS)).put(SECTION_INSERT,
              new HashMap<String, Object>());
          Integer recordIndex = 0;
          for (Map<String, Object> row : (List<Map<String, Object>>) historyRow
              .get(SECTION_INSERT)) {
            BasicDynaBean obstetricHistoryBean = obstetricHistoryRepo.getBean();
            List<String> conversionErrorList = new ArrayList<>();
            errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
            ConversionUtils.copyJsonToDynaBean(row, obstetricHistoryBean, conversionErrorList,
                false);
            obstetricHistoryBean.set(PREGNANCY_HISTORY_ID, obstetricHistoryRepo.getNextSequence());
            obstetricHistoryBean.set(MOD_TIME,
                new java.sql.Timestamp(new java.util.Date().getTime()));
            obstetricHistoryBean.set(USER_NAME, userName);
            obstetricHistoryBean.set(SECTION_DETAIL_ID, sdbean.get(SECTION_DETAIL_ID));
            isValid = obsHistoryValidator.validateObsHistoryInsert(obstetricHistoryBean, errMap)
                && isValid;
            insertObsHistoryList.add(obstetricHistoryBean);
            if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
              if (!errorMap.containsKey(SECTION_INSERT)) {
                errorMap.put(SECTION_INSERT, new HashMap<String, Object>());
              }
              ((Map<String, Object>) errorMap.get(SECTION_INSERT)).put(
                  (recordIndex).toString(),
                  (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                      conversionErrorList))).getErrors());
            }
            Map<String, Object> record = new HashMap<>();
            record.put(PREGNANCY_HISTORY_ID, obstetricHistoryBean.get(PREGNANCY_HISTORY_ID));
            ((Map<String, Object>) ((Map<String, Object>) responseData.get(RECORDS))
                .get(SECTION_INSERT)).put(recordIndex.toString(), record);
            recordIndex++;
          }
        }
        if (historyRow.get(SECTION_UPDATE) != null
            && !((List<Map<String, Object>>) historyRow.get(SECTION_UPDATE)).isEmpty()) {
          ((Map<String, Object>) responseData.get(RECORDS)).put(SECTION_UPDATE,
              new HashMap<String, Object>());
          Integer recordIndex = 0;
          for (Map<String, Object> row : (List<Map<String, Object>>) historyRow
              .get(SECTION_UPDATE)) {
            BasicDynaBean obstetricHistoryBean = obstetricHistoryRepo.getBean();
            List<String> conversionErrorList = new ArrayList<>();
            errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
            ConversionUtils.copyJsonToDynaBean(row, obstetricHistoryBean, conversionErrorList,
                false);
            obstetricHistoryBean.set(MOD_TIME,
                new java.sql.Timestamp(new java.util.Date().getTime()));
            obstetricHistoryBean.set(USER_NAME, userName);
            isValid = obsHistoryValidator.validateObsHistoryUpdate(obstetricHistoryBean, errMap)
                && isValid;
            updateObsHistoryKeys.add(row.get(PREGNANCY_HISTORY_ID));
            updateObsHistoryList.add(obstetricHistoryBean);
            if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
              if (!errorMap.containsKey(SECTION_UPDATE)) {
                errorMap.put(SECTION_UPDATE, new HashMap<String, Object>());
              }
              ((Map<String, Object>) errorMap.get(SECTION_UPDATE)).put(
                  (recordIndex).toString(),
                  (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                      conversionErrorList))).getErrors());
            }
            Map<String, Object> record = new HashMap<>();
            record.put(PREGNANCY_HISTORY_ID, obstetricHistoryBean.get(PREGNANCY_HISTORY_ID));
            ((Map<String, Object>) ((Map<String, Object>) responseData.get(RECORDS))
                .get(SECTION_UPDATE)).put(recordIndex.toString(), record);
            recordIndex++;
          }
          updateObsHistoryKeysMap.put(PREGNANCY_HISTORY_ID, updateObsHistoryKeys);
        }
        if (historyRow.get(SECTION_DELETE) != null
            && !((List<Map<String, Object>>) historyRow.get(SECTION_DELETE)).isEmpty()) {
          ((Map<String, Object>) responseData.get(RECORDS)).put(SECTION_DELETE,
              new HashMap<String, Object>());
          Integer recordIndex = 0;
          for (Map<String, Object> row : (List<Map<String, Object>>) historyRow
              .get(SECTION_DELETE)) {
            BasicDynaBean obstetricHistoryBean = obstetricHistoryRepo.getBean();
            List<String> conversionErrorList = new ArrayList<>();
            errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
            ConversionUtils.copyToDynaBean(row, obstetricHistoryBean, conversionErrorList);
            isValid = obsHistoryValidator.validateObsHistoryDelete(obstetricHistoryBean, errMap)
                && isValid;
            deleteObsHistoryKeys.add(row.get(PREGNANCY_HISTORY_ID));
            if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
              if (!errorMap.containsKey(SECTION_DELETE)) {
                errorMap.put(SECTION_DELETE, new HashMap<String, Object>());
              }
              ((Map<String, Object>) errorMap.get(SECTION_DELETE)).put(
                  (recordIndex).toString(),
                  (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                      conversionErrorList))).getErrors());
            }
            Map<String, Object> record = new HashMap<>();
            record.put(PREGNANCY_HISTORY_ID, obstetricHistoryBean.get(PREGNANCY_HISTORY_ID));
            ((Map<String, Object>) ((Map<String, Object>) responseData.get(RECORDS))
                .get(SECTION_DELETE)).put(recordIndex.toString(), record);
            recordIndex++;
          }
        }
      }
    }

    if (isValid) {
      if (!insertObsHeadRecordsList.isEmpty()) {
        obstetricHeadRepo.batchInsert(insertObsHeadRecordsList);
      }
      if (!updateObsHeadRecordsList.isEmpty()) {
        obstetricHeadRepo.batchUpdate(updateObsHeadRecordsList, updateObsHeadRecordKeysMap);
      }
      if (!insertObsHistoryList.isEmpty()) {
        obstetricHistoryRepo.batchInsert(insertObsHistoryList);
      }
      if (!updateObsHistoryList.isEmpty()) {
        obstetricHistoryRepo.batchUpdate(updateObsHistoryList, updateObsHistoryKeysMap);
      }
      if (!deleteObsHistoryKeys.isEmpty()) {
        obstetricHistoryRepo.batchDelete(PREGNANCY_HISTORY_ID, deleteObsHistoryKeys);
      }
    } else {
      return null;
    }

    return responseData;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromCurrentForm(com.insta
   * .hms.core.clinical.forms.FormParameter) The method returns list of record from current saved
   * form
   */
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    Map<String, Object> convertedData = new HashMap<>();
    data.put("head", ConversionUtils.listBeanToListMap(Arrays.asList(obstetricHeadRepo
        .getAllObsHead(parameter))));
    List<Object> mapStructure = new ArrayList<Object>(Arrays.asList(SECTION_DETAIL_ID, "finalized",
        "section_id"));
    List<Object> recordStructure = new ArrayList<Object>(Arrays.asList(PREGNANCY_HISTORY_ID,
        "date", "weeks", "place", "method", "weight", "sex", "complications", "feeding", "outcome",
        USER_NAME));
    mapStructure.add(recordStructure);

    convertedData = ConversionUtils.convertToStructeredMap(
        obstetricHistoryRepo.getObsHistory(parameter), mapStructure, null);
    data.put(RECORDS, convertedData.get(RECORDS));
    data.put(SECTION_DETAIL_ID, convertedData.get(SECTION_DETAIL_ID));
    data.put("finalized", convertedData.get("finalized"));
    data.put("section_id", convertedData.get("section_id"));

    return data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromLastSavedForm(com.insta
   * .hms.core.clinical.forms.FormParameter) The method returns list of record from last saved form
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    List headList = new ArrayList();
    BasicDynaBean headBean = obstetricHeadRepo.getAllActiveObsHead(parameter.getMrNo());
    if (headBean != null) {
      headList = Arrays.asList(headBean);
    }
    data.put(SECTION_DETAIL_ID, 0);
    data.put("finalized", "N");
    data.put("section_id", -13);
    data.put("head", ConversionUtils.listBeanToListMap(headList));
    data.put(RECORDS, ConversionUtils.listBeanToListMap(obstetricHistoryRepo
        .getAllActiveObsHistory(parameter.getMrNo())));

    return data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#deleteSection(java.lang.Integer,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map) Implementation of delete
   * section data at transaction level
   */
  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  /**
   * Gets the all pregnancy details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param itemType
   *          the item type
   * @param formId
   *          the form id
   * @return the all pregnancy details
   */
  public List<BasicDynaBean> getAllPregnancyDetails(String mrNo, String patientId, int itemId,
      int genericFormId, String itemType, int formId) {

    return obstetricHistoryRepo.getAllPregnancyDetails(mrNo, patientId, itemId, genericFormId,
        itemType, formId);
  }

  /**
   * Gets the all obstetric head details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param itemType
   *          the item type
   * @param formId
   *          the form id
   * @return the all obstetric head details
   */
  public List<BasicDynaBean> getAllObstetricHeadDetails(String mrNo, String patientId, int itemId,
      int genericFormId, String itemType, int formId) {

    return obstetricHeadRepo.getAllObstetricHeadDetails(mrNo, patientId, itemId, genericFormId,
        itemType, formId);
  }

}
