package com.insta.hms.core.clinical.complaints;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.patient.registration.RegistrationService;
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
 * Complaints Service class.
 * 
 * @author anupvishwas
 *
 */

@Service
public class ComplaintsService extends SystemSectionService {

  /** The complaints repository. */
  @LazyAutowired
  private ComplaintsRepository complaintsRepository;
  
  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;
  
  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;
  
  /** The comp validator. */
  @LazyAutowired
  private ComplaintsValidator compValidator;

  /**
   * Instantiates a new complaints service.
   */
  public ComplaintsService() {
    this.sectionId = -1;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = true;
    List<BasicDynaBean> insertSecComplaintBeanList = new ArrayList<>();
    List<BasicDynaBean> updateSecComplaintBeanList = new ArrayList<>();
    BasicDynaBean regBean = null;

    Map<String, Object> updateKeysMap = new HashMap<>();
    Map<String, Object> updateRegKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    List<Object> deleteKeys = new ArrayList<>();

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    String patientId = parameter.getPatientId();
    Map<String, Object> responseData = new HashMap<>();

    if (requestBody.get("insert") != null
        && !((List<Map<String, Object>>) requestBody.get("insert")).isEmpty()) {
      responseData.put("insert", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("insert")) {
        BasicDynaBean secComplaintsBean = complaintsRepository.getBean();
        boolean isPrimaryComplaintRowId = false;
        if (null != row.get("row_id") && (Integer) row.get("row_id") == 0) {
          isPrimaryComplaintRowId = true;
        }
        if (isPrimaryComplaintRowId) {
          regBean = registrationService.getBean();
          regBean.set("complaint", row.get("complaint"));
          regBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          regBean.set("user_name", userName);
          updateRegKeysMap.put("patient_id", patientId);
        } else {
          secComplaintsBean.set("row_id", complaintsRepository.getNextSequence());
          secComplaintsBean.set("complaint", row.get("complaint"));
          secComplaintsBean.set("username", userName);
          secComplaintsBean.set("visit_id", patientId);
          secComplaintsBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          insertSecComplaintBeanList.add(secComplaintsBean);
          Map<String, Object> record = new HashMap<>();
          record.put("row_id", secComplaintsBean.get("row_id"));
          ((Map<String, Object>) responseData.get("insert")).put(recordIndex.toString(), record);

        }
        if (!errMap.getErrorMap().isEmpty()) {
          if (!errorMap.containsKey("insert")) {
            errorMap.put("insert", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("insert")).put((recordIndex).toString(),
              (new ValidationException(
                  ValidationUtils.copyCoversionErrors(errMap, new ArrayList<String>())))
                      .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("row_id", isPrimaryComplaintRowId ? 0 : secComplaintsBean.get("row_id"));
        ((Map<String, Object>) responseData.get("insert")).put(recordIndex.toString(), record);

        recordIndex++;
      }
    }

    if (requestBody.get("update") != null
        && !((List<Map<String, Object>>) requestBody.get("update")).isEmpty()) {
      responseData.put("update", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("update")) {
        BasicDynaBean secComplaintsBean = complaintsRepository.getBean();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        boolean isPrimaryComplaintRowId = false;
        if (null != row.get("row_id") && (Integer) row.get("row_id") == 0) {
          isPrimaryComplaintRowId = true;
        }
        if (isPrimaryComplaintRowId) {
          regBean = registrationService.getBean();
          regBean.set("complaint", row.get("complaint"));
          regBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          regBean.set("user_name", userName);
          updateRegKeysMap.put("patient_id", patientId);
        } else {
          secComplaintsBean.set("complaint", row.get("complaint"));
          secComplaintsBean.set("username", userName);
          secComplaintsBean.set("visit_id", patientId);
          secComplaintsBean.set("row_id", row.get("row_id"));
          secComplaintsBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          isValid = compValidator.validateSecComplaintsUpdate(secComplaintsBean, errMap) && isValid;
          updateSecComplaintBeanList.add(secComplaintsBean);
          updateKeys.add(row.get("row_id"));
        }
        if (!errMap.getErrorMap().isEmpty()) {
          if (!errorMap.containsKey("update")) {
            errorMap.put("update", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("update")).put((recordIndex).toString(),
              (new ValidationException(
                  ValidationUtils.copyCoversionErrors(errMap, new ArrayList<String>())))
                      .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("row_id", isPrimaryComplaintRowId ? 0 : secComplaintsBean.get("row_id"));
        ((Map<String, Object>) responseData.get("update")).put(recordIndex.toString(), record);

        recordIndex++;
      }
      updateKeysMap.put("row_id", updateKeys);
    }

    if (requestBody.get("delete") != null
        && !((List<Map<String, Object>>) requestBody.get("delete")).isEmpty()) {
      responseData.put("delete", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("delete")) {
        BasicDynaBean secComplaintsBean = complaintsRepository.getBean();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        if (null != row.get("row_id") && (Integer) row.get("row_id") == 0) {
          regBean = registrationService.getBean();
          regBean.set("complaint", row.get("complaint"));
          regBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          regBean.set("user_name", userName);
          updateRegKeysMap.put("patient_id", patientId);
          if (!errMap.getErrorMap().isEmpty()) {
            if (!errorMap.containsKey("update")) {
              errorMap.put("update", new HashMap<String, Object>());
            }
            ((Map<String, Object>) errorMap.get("update")).put((recordIndex).toString(),
                (new ValidationException(
                    ValidationUtils.copyCoversionErrors(errMap, new ArrayList<String>())))
                        .getErrors());
          }
          Map<String, Object> record = new HashMap<>();
          record.put("row_id", 0);
          ((Map<String, Object>) responseData.get("update")).put(recordIndex.toString(), record);
        } else {
          secComplaintsBean.set("row_id", row.get("row_id"));
          secComplaintsBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
          deleteKeys.add(row.get("row_id"));
          isValid = compValidator.validateSecComplaintsDelete(secComplaintsBean, errMap) && isValid;
          if (!errMap.getErrorMap().isEmpty()) {
            if (!errorMap.containsKey("delete")) {
              errorMap.put("delete", new HashMap<String, Object>());
            }
            ((Map<String, Object>) errorMap.get("delete")).put((recordIndex).toString(),
                (new ValidationException(
                    ValidationUtils.copyCoversionErrors(errMap, new ArrayList<String>())))
                        .getErrors());
          }
          Map<String, Object> record = new HashMap<>();
          record.put("row_id", secComplaintsBean.get("row_id"));
          ((Map<String, Object>) responseData.get("delete")).put(recordIndex.toString(), record);
        }
        recordIndex++;
      }
    }
    if (isValid) {
      if (!insertSecComplaintBeanList.isEmpty()) {
        complaintsRepository.batchInsert(insertSecComplaintBeanList);
      }
      if (!updateSecComplaintBeanList.isEmpty()) {
        complaintsRepository.batchUpdate(updateSecComplaintBeanList, updateKeysMap);
      }
      if (!deleteKeys.isEmpty()) {
        complaintsRepository.batchDelete("row_id", deleteKeys);
      }
      if (null != regBean) {
        registrationService.update(regBean, updateRegKeysMap);
      }
    } else {
      return null;
    }
    return responseData;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    List<Object> mapStructure =
        new ArrayList<Object>(Arrays.asList(new String[] {"section_detail_id", "section_id"}));
    List<Object> recordStructure =
        new ArrayList<Object>(Arrays.asList(new String[] {"row_id", "complaint", "type"}));
    mapStructure.add(recordStructure);
    return ConversionUtils.convertToStructeredMap(complaintsRepository.getComplaintList(parameter),
        mapStructure, null);
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    List<Object> mapStructure =
        new ArrayList<Object>(Arrays.asList("section_detail_id", "section_id", "revision_number"));
    List<Object> recordStructure =
        new ArrayList<Object>(Arrays.asList("row_id", "complaint", "type"));
    mapStructure.add(recordStructure);
    List<BasicDynaBean> allComplaintsList =
        complaintsRepository.getAllActiveComplaintList(parameter.getPatientId());
    if (allComplaintsList.isEmpty()) {
      Map<String, Object> data = new HashMap<>();
      data.put("section_id", -1);
      data.put("section_detail_id", 0);
      data.put("records", new ArrayList<Map>());
      return data;
    }
    return ConversionUtils.convertToStructeredMap(allComplaintsList, mapStructure, null);
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processTemplateData(FormParameter parameters, Map<String, Object> templateData,
      Map<String, Object> responseData, Integer formId) {
    if (((List<Map<String, Object>>) responseData.get("records")).size() == 1) {
      String complaint = (String) ((List<Map<String, Object>>) responseData.get("records")).get(0)
          .get("complaint");
      if ((complaint == null || "".equals(complaint)) && templateData != null) {
        responseData.put("records", templateData.get("records"));
        responseData.put("isTemplateRecords", true);
      }
    }
  }

  /**
   * Gets the chief complaint.
   *
   * @param patientId the patient id
   * @return the chief complaint
   */
  public String getChiefComplaint(String patientId) {
    return complaintsRepository.getChiefComplaint(patientId);
  }

  /**
   * Gets the secondary complaints.
   *
   * @param patientId the patient id
   * @return the secondary complaints
   */
  public String getSecondaryComplaints(String patientId) {
    return complaintsRepository.getSecondaryComplaints(patientId);
  }

}
