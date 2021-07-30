package com.insta.hms.core.clinical.antenatal;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AntenatalService.
 *
 * @author anupvishwas
 */
@Service
public class AntenatalService extends SystemSectionService {

  public AntenatalService() {
    this.sectionId = -14;
  }

  /** The antenatal repo. */
  @LazyAutowired
  private AntenatalRepository antenatalRepo;

  /** The antenatal main repository. */
  @LazyAutowired
  private AntenatalMainRepository antenatalMainRepository;

  /** The antenatal validator. */
  @LazyAutowired
  private AntenatalValidator antenatalValidator;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  private static final String RECORDS = "records";

  private static final String UPDATE = "update";

  private static final String INSERT = "insert";

  private static final String DELETE = "delete";

  private static final String USERID = "userId";

  private static final String USERNAME = "username";

  /**
   * To bean.
   *
   * @param requestBody
   *          the request body
   * @param bean
   *          the bean
   */
  private void toBean(Map<String, Object> requestBody, BasicDynaBean bean) {
    List<String> errorFields = new ArrayList<>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errorFields, true);
  }

  /**
   * Insert antenatal main.
   *
   * @param mainBean
   *          the main bean
   */
  private void insertAntenatalMain(BasicDynaBean mainBean) {
    antenatalMainRepository.insert(mainBean);
  }

  /**
   * Update antenatal main.
   *
   * @param mainBean
   *          the main bean
   * @param keys
   *          the keys
   */
  private void updateAntenatalMain(BasicDynaBean mainBean, Map<String, Object> keys) {
    antenatalMainRepository.update(mainBean, keys);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#saveSection(java.util.Map,
   * org.apache.commons.beanutils.BasicDynaBean, com.insta.hms.core.clinical.forms.FormParameter,
   * java.util.Map)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {
    Map<String, Object> responseData = new HashMap<>();
    List recordResList = new ArrayList();
    if (requestBody.get(RECORDS) != null
        && !((List<Map<String, Object>>) requestBody.get(RECORDS)).isEmpty()) {
      for (Map<String, Object> main : (List<Map<String, Object>>) requestBody.get(RECORDS)) {

        List<BasicDynaBean> insertAntenatalBeanList = new ArrayList<>();
        if ( main.get(AntenatalMainRepository.ANTENATAL_MAIN_ID) == null 
            && ((List<Map<String, Object>>) main.get(UPDATE)).isEmpty()
            && ((List<Map<String, Object>>) main.get(INSERT)).isEmpty()
            && ((List<Map<String, Object>>) main.get(DELETE)).isEmpty()) {
          continue;
        }
        List<BasicDynaBean> updateAntenatalBeanList = new ArrayList<>();
        BasicDynaBean antenatalMainBean = insertOrUpdateAntenatalMain(main, sdbean);
        Map<String, Object> mainRes = new HashMap<>();
        mainRes.put(AntenatalMainRepository.ANTENATAL_MAIN_ID,
            antenatalMainBean.get(AntenatalMainRepository.ANTENATAL_MAIN_ID));
        List insertAntenatalResponseData = insertAntenatalDetails(main, antenatalMainBean, errorMap,
            insertAntenatalBeanList);
        mainRes.put(INSERT, insertAntenatalResponseData);
        List updateAntenatalResponseData = updateAntenatalDetails(main, errorMap,
            updateAntenatalBeanList);
        mainRes.put(UPDATE, updateAntenatalResponseData);
        List<Object> deleteKeys = new ArrayList<>();
        deleteAntenatalDetails(main, antenatalMainBean, errorMap, deleteKeys, mainRes);
        recordResList.add(mainRes);
      }
    }
    responseData.put(RECORDS, recordResList);
    return responseData;
  }

  private BasicDynaBean insertOrUpdateAntenatalMain(Map<String, Object> main,
      BasicDynaBean psdBean) {

    BasicDynaBean mainBean = antenatalMainRepository.getBean();
    toBean(main, mainBean);
    if (mainBean.get(AntenatalMainRepository.ANTENATAL_MAIN_ID) == null) {
      Object nextId = antenatalMainRepository.getNextSequence();
      mainBean.set(AntenatalMainRepository.ANTENATAL_MAIN_ID, nextId);
      mainBean.set(AntenatalMainRepository.SECTION_DETAIL_ID,
          psdBean.get(AntenatalMainRepository.SECTION_DETAIL_ID));
      String userId = (String) sessionService.getSessionAttributes().get(USERID);
      mainBean.set("created_by", userId);
      mainBean.set("modified_by", userId);
      mainBean.set("modified_at", DateUtil.getCurrentTimestamp());
      if (null != mainBean.get(AntenatalMainRepository.CLOSE_PREGNANCY)
          && mainBean.get(AntenatalMainRepository.CLOSE_PREGNANCY).equals("Y")) {
        mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY, "Y");
        mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY_USER,
            sessionService.getSessionAttributes().get(USERID));
        mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY_DATE_TIME,
            DateUtil.getCurrentTimestamp());
      }
      insertAntenatalMain(mainBean);
    } else {
      Map<String, Object> keys = new HashMap<>();
      keys.put(AntenatalMainRepository.ANTENATAL_MAIN_ID,
          mainBean.get(AntenatalMainRepository.ANTENATAL_MAIN_ID));
      mainBean.set("modified_by", sessionService.getSessionAttributes().get(USERID));
      mainBean.set("modified_at", DateUtil.getCurrentTimestamp());
      // added close pregnancy details
      if (null != mainBean.get(AntenatalMainRepository.CLOSE_PREGNANCY)) {
        if (mainBean.get(AntenatalMainRepository.CLOSE_PREGNANCY).equals("Y")) {
          mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY, "Y");
          mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY_USER,
              sessionService.getSessionAttributes().get(USERID));
          mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY_DATE_TIME,
              DateUtil.getCurrentTimestamp());
        } else if (mainBean.get(AntenatalMainRepository.CLOSE_PREGNANCY).equals("N")) {
          mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY, "N");
          mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY_USER, null);
          mainBean.set(AntenatalMainRepository.CLOSE_PREGNANCY_DATE_TIME, null);
        }
      }
      updateAntenatalMain(mainBean, keys);
    }
    return mainBean;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> insertAntenatalDetails(Map<String, Object> main,
      BasicDynaBean antenatalMainBean, Map<String, Object> errorMap,
      List<BasicDynaBean> insertAntenatalBeanList) {
    boolean isValid = true;
    ValidationErrorMap errMap = new ValidationErrorMap();
    List<Map<String, Object>> insertDetails = new ArrayList<>();
    int inserSize = ((List<Map<String, Object>>) main.get(INSERT)).size();
    if (main.get(INSERT) != null && (inserSize > 0)) {
      Integer recordIndex = 0;
      for (Map<String, Object> details : (List<Map<String, Object>>) main.get(INSERT)) {
        BasicDynaBean antenatalBean = antenatalRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        ConversionUtils.copyJsonToDynaBean(details, antenatalBean, conversionErrorList, false);
        antenatalBean.set(AntenatalRepository.ANTENATAL_ID, antenatalRepo.getNextSequence());
        antenatalBean.set(AntenatalMainRepository.ANTENATAL_MAIN_ID,
            antenatalMainBean.get(AntenatalMainRepository.ANTENATAL_MAIN_ID));
        antenatalBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        antenatalBean.set(USERNAME, (String) RequestContext.getSession().getAttribute(USERID));
        insertAntenatalBeanList.add(antenatalBean);
        isValid = antenatalValidator.validateAntenatalInsert(antenatalBean, errMap) && isValid;
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey(INSERT)) {
            errorMap.put(INSERT, new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get(INSERT))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put(AntenatalRepository.ANTENATAL_ID,
            antenatalBean.get(AntenatalRepository.ANTENATAL_ID));
        insertDetails.add(detailMap);
        recordIndex++;
      }
    }
    if (isValid && !insertAntenatalBeanList.isEmpty()) {
      antenatalRepo.batchInsert(insertAntenatalBeanList);
    }
    return insertDetails;
  }

  /**
   * Update antenatal details.
   *
   * @param main
   *          the main
   * @param errorMap
   *          the error map
   * @param updateAntenatalBeanList
   *          the update antenatal bean list
   * @return the list
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> updateAntenatalDetails(Map<String, Object> main,
      Map<String, Object> errorMap, List<BasicDynaBean> updateAntenatalBeanList) {
    boolean isValid = true;
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    Integer recordIndex = 0;
    List<Map<String, Object>> updateDetails = new ArrayList<>();
    if (main.get(UPDATE) != null && !((List<Map<String, Object>>) main.get(UPDATE)).isEmpty()) {
      for (Map<String, Object> details : (List<Map<String, Object>>) main.get(UPDATE)) {
        BasicDynaBean antenatalBean = antenatalRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        ConversionUtils.copyJsonToDynaBean(details, antenatalBean, conversionErrorList, false);
        antenatalBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        antenatalBean.set(USERNAME, (String) RequestContext.getSession().getAttribute(USERID));
        isValid = antenatalValidator.validateAntenatalUpdate(antenatalBean, errMap) && isValid;
        updateKeys.add(antenatalBean.get(AntenatalRepository.ANTENATAL_ID));
        updateAntenatalBeanList.add(antenatalBean);
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey(UPDATE)) {
            errorMap.put(UPDATE, new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get(UPDATE))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put(AntenatalRepository.ANTENATAL_ID,
            antenatalBean.get(AntenatalRepository.ANTENATAL_ID));
        updateDetails.add(detailMap);
        recordIndex++;
      }
      updateKeysMap.put(AntenatalRepository.ANTENATAL_ID, updateKeys);
    }

    if (isValid && !updateAntenatalBeanList.isEmpty()) {
      antenatalRepo.batchUpdate(updateAntenatalBeanList, updateKeysMap);
    }
    return updateDetails;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> deleteAntenatalDetails(Map<String, Object> main,
      BasicDynaBean antenatalMainBean, Map<String, Object> errorMap, List<Object> deleteKeys,
      Map<String, Object> mainRes) {
    boolean isValid = true;
    ValidationErrorMap errMap = new ValidationErrorMap();
    List<Map<String, Object>> deleteDetails = new ArrayList<>();
    if (null != main.get(DELETE) && !((List<Map<String, Object>>) main.get(DELETE)).isEmpty()) {
      Integer recordIndex = 0;
      for (Map<String, Object> details : (List<Map<String, Object>>) main.get(DELETE)) {
        BasicDynaBean antenatalBean = antenatalRepo.getBean();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyToDynaBean(details, antenatalBean, conversionErrorList);
        deleteKeys.add(details.get(AntenatalRepository.ANTENATAL_ID));
        isValid = antenatalValidator.validateAntenatalDelete(antenatalBean, errMap) && isValid;
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey(DELETE)) {
            errorMap.put(DELETE, new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get(DELETE))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put(AntenatalRepository.ANTENATAL_ID,
            antenatalBean.get(AntenatalRepository.ANTENATAL_ID));
        deleteDetails.add(detailMap);
        recordIndex++;
      }
      mainRes.put(DELETE, deleteDetails);
    }

    if (!deleteKeys.isEmpty()) {
      antenatalRepo.batchDelete(AntenatalRepository.ANTENATAL_ID, deleteKeys);
      BasicDynaBean isMoreAntenatalBean = antenatalRepo.findByKey(
          AntenatalMainRepository.ANTENATAL_MAIN_ID,
          antenatalMainBean.get(AntenatalMainRepository.ANTENATAL_MAIN_ID));
      if (isMoreAntenatalBean == null) {
        antenatalMainRepository.delete(AntenatalMainRepository.ANTENATAL_MAIN_ID,
            antenatalMainBean.get(AntenatalMainRepository.ANTENATAL_MAIN_ID));
        mainRes.clear();
      }
    }
    return deleteDetails;
  }

  /**
   * Gets the section data.
   *
   * @param parameter the parameter
   * @return the section data
   */
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    List<Object> mapStructure = getAntenatalResponseStructure();
    List<BasicDynaBean> antenatalList = antenatalRepo.getAllAntenatal(parameter);
    if (null == antenatalList || antenatalList.isEmpty()) {
      Map<String, Object> records = new HashMap<>();
      records.put(RECORDS, new ArrayList<>());
      records.put("lmp", null);
      records.put("edd", null);
      records.put("final_edd", null);
      records.put("pregnancy_result", "Delivery");
      records.put("pregnancy_result_date", null);
      records.put("number_of_birth", null);
      records.put("pregnancy_count", 1);
      records.put("close_pregnancy", "N");
      records.put("remarks", null);

      Map<String, Object> structeredRecords = new HashMap<>();
      structeredRecords.put(RECORDS, records);
      return structeredRecords;

    } else {
      return ConversionUtils.convertToStructuredMap(antenatalList, mapStructure,
          AntenatalMainRepository.ANTENATAL_MAIN_ID, AntenatalRepository.ANTENATAL_ID);
    }
  }

  /**
   * Gets the antenatal response structure.
   *
   * @return the antenatal response structure
   */
  private List<Object> getAntenatalResponseStructure() {

    List<Object> mapStructure = new ArrayList<>();
    String[] sd = new String[] { "section_id", "finalized", "section_detail_id" };
    mapStructure.addAll(Arrays.asList(sd));

    List<Object> antenatalMainFieldList = new ArrayList<>();
    antenatalMainFieldList.add("antenatal_main_id");
    antenatalMainFieldList.add("lmp");
    antenatalMainFieldList.add("edd");
    antenatalMainFieldList.add("final_edd");
    antenatalMainFieldList.add("pregnancy_result");
    antenatalMainFieldList.add("pregnancy_result_date");
    antenatalMainFieldList.add("number_of_birth");
    antenatalMainFieldList.add("pregnancy_count");
    antenatalMainFieldList.add("close_pregnancy");
    antenatalMainFieldList.add("remarks");
    Map<String, List<Object>> antenatalMainMap = new LinkedHashMap<>();
    antenatalMainMap.put(RECORDS, antenatalMainFieldList);

    List<Object> antenatalDetailsFieldList = new ArrayList<>();
    antenatalDetailsFieldList.add("antenatal_id");
    antenatalDetailsFieldList.add("visit_date");
    antenatalDetailsFieldList.add("gestation_age");
    antenatalDetailsFieldList.add("height_fundus");
    antenatalDetailsFieldList.add("presentation");
    antenatalDetailsFieldList.add("rel_pp_brim");
    antenatalDetailsFieldList.add("foetal_heart");
    antenatalDetailsFieldList.add("urine");
    antenatalDetailsFieldList.add("weight");
    antenatalDetailsFieldList.add("prescription_summary");
    antenatalDetailsFieldList.add("next_visit_date");
    antenatalDetailsFieldList.add(USERNAME);
    antenatalDetailsFieldList.add("systolic_bp");
    antenatalDetailsFieldList.add("diastolic_bp");
    antenatalDetailsFieldList.add("movement");
    antenatalDetailsFieldList.add("position");
    antenatalDetailsFieldList.add("doctor_id");
    antenatalDetailsFieldList.add("doctor_name");
    Map<String, List<Object>> antenatalDetailsMap = new LinkedHashMap<>();
    antenatalDetailsMap.put(RECORDS, antenatalDetailsFieldList);
    antenatalMainFieldList.add(antenatalDetailsMap);
    mapStructure.add(antenatalMainMap);
    return mapStructure;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromLastSavedForm(com.insta.
   * hms.core.clinical.forms.FormParameter)
   */
  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    List<Object> mapStructure = getAntenatalResponseStructure();
    List<BasicDynaBean> antenatalList = antenatalRepo.getAllActiveAntenatal(parameter.getMrNo());
    if (null == antenatalList || antenatalList.isEmpty()) {
      Map<String, Object> records = new HashMap<>();
      records.put(RECORDS, new ArrayList<>());
      records.put("lmp", null);
      records.put("edd", null);
      records.put("final_edd", null);
      records.put("pregnancy_result", "Delivery");
      records.put("pregnancy_result_date", null);
      records.put("number_of_birth", null);
      records.put("pregnancy_count", 1);
      records.put("close_pregnancy", "N");
      records.put("remarks", null);

      Map<String, Object> structeredRecords = new HashMap<>();
      structeredRecords.put(RECORDS, records);
      return structeredRecords;
    } else {
      return ConversionUtils.convertToStructuredMap(antenatalList, mapStructure,
          "antenatal_main_id", "antenatal_id");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#deleteSection(java.lang.Integer,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map)
   */
  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  /**
   * Gets the all antenatal details.
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
   * @return the all antenatal details
   */
  public List<BasicDynaBean> getAllAntenatalDetails(String mrNo, String patientId, int itemId,
      int genericFormId, String itemType, int formId) {

    return antenatalRepo.getAllAntenatalDetails(mrNo, patientId, itemId, genericFormId, itemType,
        formId);
  }

}
