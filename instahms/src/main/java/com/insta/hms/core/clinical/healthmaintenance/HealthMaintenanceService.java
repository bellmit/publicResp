package com.insta.hms.core.clinical.healthmaintenance;

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

/**
 * The Class HealthMaintenanceService.
 *
 * @author sonam
 */
@Service
public class HealthMaintenanceService extends SystemSectionService {

  /** The health maint repo. */
  @LazyAutowired
  private HealthMaintenanceRepository healthMaintRepo;

  /** The health maint validation. */
  @LazyAutowired
  private HealthMaintenanceValidation healthMaintValidation;

  /** The Constant SECTION_INSERT. */
  private static final String SECTION_INSERT = "insert";

  /** The Constant HEALTH_MAINT_ID. */
  private static final String HEALTH_MAINT_ID = "health_maint_id";

  /** The Constant SECTION_DETAIL_ID. */
  private static final String SECTION_DETAIL_ID = "section_detail_id";

  /** The Constant USERNAME. */
  private static final String USERNAME = "username";

  /** The Constant SECTION_UPDATE. */
  private static final String SECTION_UPDATE = "update";

  /** The Constant SECTION_DELETE. */
  private static final String SECTION_DELETE = "delete";

  /**
   * Instantiates a new health maintenance service.
   */
  public HealthMaintenanceService() {
    this.sectionId = -15;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.forms.SectionService#saveSection(java.util .Map,
   * org.apache.commons.beanutils.BasicDynaBean, com.insta.hms.core.clinical.forms.FormParameter,
   * java.util.Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = true;
    List<BasicDynaBean> insertBeans = new ArrayList<>();
    Map<String, Object> responseData = new HashMap<>();
    if (requestBody.get(SECTION_INSERT) != null) {
      responseData.put(SECTION_INSERT, new HashMap<String, Object>());
      Integer recordIndex = 0;
      String userName = (String) RequestContext.getSession().getAttribute("userId");
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(SECTION_INSERT)) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean healthBean = healthMaintRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, healthBean, conversionErrorList, false);
        healthBean.set(HEALTH_MAINT_ID, healthMaintRepo.getNextSequence());
        healthBean.set(SECTION_DETAIL_ID, sdbean.get(SECTION_DETAIL_ID));
        healthBean.set(USERNAME, userName);
        healthBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        isValid = healthMaintValidation.validateHealthInsert(healthBean, errMap) && isValid;
        insertBeans.add(healthBean);

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
        record.put(HEALTH_MAINT_ID, healthBean.get(HEALTH_MAINT_ID));
        ((Map<String, Object>) responseData.get(SECTION_INSERT))
            .put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    Map<String, Object> updateKeysMap = new HashMap<>();
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    List<Object> updateKeys = new ArrayList<>();

    if (requestBody.get(SECTION_UPDATE) != null) {
      responseData.put(SECTION_UPDATE, new HashMap<String, Object>());
      Integer recordIndex = 0;
      String userName = (String) RequestContext.getSession().getAttribute("userId");
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(SECTION_UPDATE)) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean healthBean = healthMaintRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, healthBean, conversionErrorList, false);
        healthBean.set(SECTION_DETAIL_ID, sdbean.get(SECTION_DETAIL_ID));
        healthBean.set(USERNAME, userName);
        healthBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        isValid = healthMaintValidation.validateHealthUpdate(healthBean, errMap) && isValid;
        updateBeans.add(healthBean);
        updateKeys.add(healthBean.get(HEALTH_MAINT_ID));
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
        record.put(HEALTH_MAINT_ID, healthBean.get(HEALTH_MAINT_ID));
        ((Map<String, Object>) responseData.get(SECTION_UPDATE))
            .put(recordIndex.toString(), record);
        recordIndex++;
      }
      updateKeysMap.put(HEALTH_MAINT_ID, updateKeys);
    }

    List<Object> deleteKeys = new ArrayList<>();

    if (requestBody.get(SECTION_DELETE) != null) {
      responseData.put(SECTION_DELETE, new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(SECTION_DELETE)) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean healthBean = healthMaintRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyToDynaBean(row, healthBean, conversionErrorList);
        deleteKeys.add(row.get(HEALTH_MAINT_ID));
        isValid = healthMaintValidation.validateHealthDelete(healthBean, errMap) && isValid;

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
        record.put(HEALTH_MAINT_ID, healthBean.get(HEALTH_MAINT_ID));
        ((Map<String, Object>) responseData.get(SECTION_DELETE))
            .put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    if (isValid) {
      if (!insertBeans.isEmpty()) {
        healthMaintRepo.batchInsert(insertBeans);
      }
      if (!updateBeans.isEmpty()) {
        healthMaintRepo.batchUpdate(updateBeans, updateKeysMap);
      }
      if (!deleteKeys.isEmpty()) {
        healthMaintRepo.batchDelete(HEALTH_MAINT_ID, deleteKeys);
      }
    } else {
      return null;
    }
    return responseData;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.forms.SectionService# getSectionDetailsFromCurrentForm
   * (com.insta.hms.core.clinical.forms.FormParameter) The method returns list of record from
   * current saved form
   */
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    List<Object> mapStructure = new ArrayList<Object>(Arrays.asList(SECTION_DETAIL_ID, "finalized",
        "section_id"));
    List<Object> recordStructure = new ArrayList<Object>(Arrays.asList(HEALTH_MAINT_ID,
        "doctor_id", "doctor_name", "activity", "remarks", "status", "recorded_date", "due_by",
        USERNAME));
    mapStructure.add(recordStructure);
    return ConversionUtils.convertToStructeredMap(healthMaintRepo.getHealthMaintRecords(
        parameter.getMrNo(), parameter.getPatientId(), (int) parameter.getId(),
        parameter.getItemType(), parameter.getFormType(), parameter.getFormFieldName()),
        mapStructure, null);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.forms.SectionService# getSectionDetailsFromLastSavedForm
   * (com.insta.hms.core.clinical.forms.FormParameter)
   * The method returns list of record from last
   * saved form
   */
  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    data.put(SECTION_DETAIL_ID, 0);
    data.put("finalized", "N");
    data.put("section_id", -15);
    data.put("records", ConversionUtils.listBeanToListMap(healthMaintRepo
        .getAllActiveHealthMaint(parameter.getMrNo())));
    return data;
  }

  /**
   * Gets the all health maintenance.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param itemId
   *          the item id
   * @param genericFormId
   *          the generic form id
   * @param formId
   *          the form id
   * @param itemType
   *          the item type
   * @return the all health maintenance
   */
  // Used in discharge summary and prints
  public List<BasicDynaBean> getAllHealthMaintenance(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return healthMaintRepo.getAllHealthMaintenance(mrNo, patientId, itemId, genericFormId, formId,
        itemType);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.core.clinical.forms.SectionService#deleteSection(java.lang .Integer,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map) This method represent own
   * Implementation of delete section data at transaction level
   */
  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

}