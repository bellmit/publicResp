package com.insta.hms.core.clinical.pac;

import com.bob.hms.common.DateUtil;
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
import java.util.List;
import java.util.Map;

/**
 * The Class PreAnaesthestheticService.
 *
 * @author teja
 */
@Service
public class PreAnaesthestheticService extends SystemSectionService {

  /** The pac repo. */
  @LazyAutowired
  private PreAnaesthestheticRepository pacRepo;

  /** The pac validator. */
  @LazyAutowired
  private PreAnaesthestheticValidator pacValidator;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Instantiates a new pre anaesthesthetic service.
   */
  public PreAnaesthestheticService() {
    this.sectionId = -16;
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
    Map<String, Object> response = new HashMap<>();
    boolean isValid = true;

    List<BasicDynaBean> insertBeans = new ArrayList<>();
    if (requestBody.get("insert") != null) {
      response.put("insert", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("insert")) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean pacBean = pacRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, pacBean, conversionErrorList, false);
        pacBean.set("patient_pac_id", pacRepo.getNextSequence());
        pacBean.set("section_detail_id", sdbean.get("section_detail_id"));
        pacBean.set("username", sessionService.getSessionAttributes().get("userId"));
        pacBean.set("mod_time", DateUtil.getCurrentTimestamp());
        isValid = pacValidator.validateInsert(pacBean, errMap) && isValid;
        insertBeans.add(pacBean);
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("insert")) {
            errorMap.put("insert", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("insert"))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("patient_pac_id", pacBean.get("patient_pac_id"));
        ((Map<String, Object>) response.get("insert")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    List<BasicDynaBean> updateBeans = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    if (requestBody.get("update") != null) {
      response.put("update", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("update")) {
        // reset errMap
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean pacBean = pacRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, pacBean, conversionErrorList, false);
        pacBean.set("section_detail_id", sdbean.get("section_detail_id"));
        pacBean.set("username", sessionService.getSessionAttributes().get("userId"));
        pacBean.set("mod_time", DateUtil.getCurrentTimestamp());
        isValid = pacValidator.validateUpdate(pacBean, errMap) && isValid;
        updateBeans.add(pacBean);
        updateKeys.add(pacBean.get("patient_pac_id"));
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("update")) {
            errorMap.put("update", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("update"))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("patient_pac_id", pacBean.get("patient_pac_id"));
        ((Map<String, Object>) response.get("update")).put(recordIndex.toString(), record);
        recordIndex++;
      }
      updateKeysMap.put("patient_pac_id", updateKeys);
    }

    List<Object> deleteKeys = new ArrayList<>();
    if (requestBody.get("delete") != null) {
      response.put("delete", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("delete")) {
        // reset errMap
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean pacBean = pacRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyToDynaBean(row, pacBean, conversionErrorList);
        deleteKeys.add(row.get("patient_pac_id"));
        isValid = pacValidator.validateDelete(pacBean, errMap) && isValid;
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("delete")) {
            errorMap.put("delete", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("delete"))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("patient_pac_id", pacBean.get("patient_pac_id"));
        ((Map<String, Object>) response.get("delete")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    if (isValid) {
      if (!insertBeans.isEmpty()) {
        pacRepo.batchInsert(insertBeans);
      }
      if (!updateBeans.isEmpty()) {
        pacRepo.batchUpdate(updateBeans, updateKeysMap);
      }
      if (!deleteKeys.isEmpty()) {
        pacRepo.batchDelete("patient_pac_id", deleteKeys);
      }
    } else {
      return null;
    }
    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromCurrentForm(com.insta.hms
   * .core.clinical.forms.FormParameter)
   */
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameters) {
    List<Object> mapStructure =
        new ArrayList<Object>(Arrays.asList("section_id", "section_detail_id", "finalized"));
    List<Object> recordStructure =
        new ArrayList<Object>(Arrays.asList("patient_pac_id", "doctor_id", "doctor_name", "status",
            "patient_pac_remarks", "pac_date", "pac_validity", "username"));
    mapStructure.add(recordStructure);
    return ConversionUtils.convertToStructeredMap(
        pacRepo.getPreAnaesthestheticdetails(parameters.getMrNo(), parameters.getPatientId(),
            parameters.getId(), parameters.getItemType(), parameters.getFormFieldName()),
        mapStructure, null);
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
    Map<String, Object> data = new HashMap<>();
    data.put("section_id", -16);
    data.put("section_detail_id", 0);
    data.put("finalized", "N");
    data.put("records", ConversionUtils
        .listBeanToListMap(pacRepo.getActivePreAnaesthestheticdetails(parameter.getMrNo())));
    return data;
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
   * Gets the all PAC records.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the all PAC records
   */
  // below method used for prints
  public List<BasicDynaBean> getAllPACRecords(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {
    return pacRepo.getAllPACRecords(mrNo, patientId, itemId, genericFormId, formId, itemType);
  }

}
