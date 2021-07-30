/**
 * 
 */

package com.insta.hms.core.patient.followupdetails;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRepository;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FollowUpService.
 *
 * @author anup vishwas
 */

@Service
public class FollowUpService {

  /** The follow up repository. */
  @LazyAutowired
  private FollowUpRepository followUpRepository;

  /** The follow up validator. */
  @LazyAutowired
  private FollowUpValidator followUpValidator;

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;
  
  @LazyAutowired
  private ModulesActivatedService modulesActivatedService;
  
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionsService;
  
  @LazyAutowired
  private PendingPrescriptionsRepository pendingPrescriptionsRepository;

  /**
   * Find by key.
   *
   * @param patientId
   *          the patient id
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String patientId) {

    return followUpRepository.findByKey("patient_id", patientId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {

    return followUpRepository.getBean();
  }

  /**
   * Gets the follow up details.
   *
   * @param patientId
   *          the patient id
   * @return the follow up details
   */
  public List<BasicDynaBean> getfollowUpDetails(String patientId) {

    return followUpRepository.getfollowUpDetails(patientId);
  }

  /**
   * Insert.
   *
   * @param followUpDetailsBean
   *          the follow up details bean
   * @return the int
   */
  public int insert(BasicDynaBean followUpDetailsBean) {

    return followUpRepository.insert(followUpDetailsBean);
  }

  /**
   * Update.
   *
   * @param followUpDetailsBean
   *          the follow up details bean
   * @param keys
   *          the keys
   * @return the int
   */
  public int update(BasicDynaBean followUpDetailsBean, Map<String, Object> keys) {

    return followUpRepository.update(followUpDetailsBean, keys);
  }

  /**
   * Delete.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the int
   */
  public int delete(String key, Object value) {

    return followUpRepository.delete(key, value);
  }

  /**
   * Gets the next follow up id.
   *
   * @return the next follow up id
   */
  public String getNextFollowUpId() {

    return (String) followUpRepository.getNextId();
  }

  /**
   * Save follow up details.
   *
   * @param reqBoady
   *          the req boady
   * @param parameters
   *          the parameters
   * @param errorMap
   *          the error map
   * @return the map
   * @throws ParseException
   *           the parse exception
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> saveFollowUpDetails(Map<String, Object> reqBoady,
      FormParameter parameters, Map<String, Object> errorMap) throws ParseException {
    boolean isValid = true;
    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> responseData = new HashMap<>();
    Map<String, Object> params = (Map<String, Object>) reqBoady.get("followup_details");
    List<BasicDynaBean> insertBeans = new ArrayList<>();
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    List<Object> deleteKeys = new ArrayList<>();

    if (params != null && params.size() > 0) {
      BasicDynaBean consultationBean = doctorConsultationService
          .findByKey((int) parameters.getId());
      responseData.put("followup_details", new HashMap<String, Object>());
      if (params.get("insert") != null
          && !((List<Map<String, Object>>) params.get("insert")).isEmpty()) {
        Integer recordIndex = 0;
        ((Map<String, Object>) responseData.get("followup_details")).put("insert",
            new HashMap<String, Object>());
        for (Map<String, Object> row : (List<Map<String, Object>>) params.get("insert")) {
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
          BasicDynaBean followupBean = getBean();
          List<String> conversionErrorList = new ArrayList<>();
          ConversionUtils.copyJsonToDynaBean(row, followupBean, conversionErrorList, false);
          followupBean.set("followup_id", getNextFollowUpId());
          followupBean.set("patient_id", parameters.getPatientId());
          isValid = followUpValidator
              .validatefollowUpInsert(followupBean, consultationBean, errMap) && isValid;
          insertBeans.add(followupBean);
          if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
            if (!errorMap.containsKey("insert")) {
              errorMap.put("insert", new HashMap<String, Object>());
            }
            ((Map<String, Object>) errorMap.get("insert")).put(
                (recordIndex).toString(),
                (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                    conversionErrorList))).getErrors());
          }
          Map<String, Object> record = new HashMap<>();
          record.put("followup_id", followupBean.get("followup_id"));
          ((Map<String, Object>) ((Map<String, Object>) responseData.get("followup_details"))
              .get("insert")).put(recordIndex.toString(), record);
          recordIndex++;
        }
      }
      if (params.get("update") != null
          && !((List<Map<String, Object>>) params.get("update")).isEmpty()) {
        Integer recordIndex = 0;
        ((Map<String, Object>) responseData.get("followup_details")).put("update",
            new HashMap<String, Object>());
        for (Map<String, Object> row : (List<Map<String, Object>>) params.get("update")) {
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
          BasicDynaBean followupBean = getBean();
          List<String> conversionErrorList = new ArrayList<>();
          ConversionUtils.copyJsonToDynaBean(row, followupBean, conversionErrorList, false);
          followupBean.set("patient_id", parameters.getPatientId());
          isValid = followUpValidator
              .validatefollowUpUpdate(followupBean, consultationBean, errMap) && isValid;
          updateBeans.add(followupBean);
          updateKeys.add(followupBean.get("followup_id"));
          if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
            if (!errorMap.containsKey("update")) {
              errorMap.put("update", new HashMap<String, Object>());
            }
            ((Map<String, Object>) errorMap.get("update")).put(
                (recordIndex).toString(),
                (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                    conversionErrorList))).getErrors());
          }
          Map<String, Object> record = new HashMap<>();
          record.put("followup_id", followupBean.get("followup_id"));
          ((Map<String, Object>) ((Map<String, Object>) responseData.get("followup_details"))
              .get("update")).put(recordIndex.toString(), record);
          recordIndex++;
        }
        updateKeysMap.put("followup_id", updateKeys);
      }
      if (params.get("delete") != null
          && !((List<Map<String, Object>>) params.get("delete")).isEmpty()) {
        Integer recordIndex = 0;
        ((Map<String, Object>) responseData.get("followup_details")).put("delete",
            new HashMap<String, Object>());
        for (Map<String, Object> row : (List<Map<String, Object>>) params.get("delete")) {
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
          BasicDynaBean followupBean = getBean();
          List<String> conversionErrorList = new ArrayList<>();
          ConversionUtils.copyToDynaBean(row, followupBean, conversionErrorList);
          String followUpId = (String) row.get("followup_id");
          isValid = followUpValidator.validateFollowUpId(followUpId, errMap) && isValid;
          deleteKeys.add(followUpId);
          if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
            if (!errorMap.containsKey("delete")) {
              errorMap.put("delete", new HashMap<String, Object>());
            }
            ((Map<String, Object>) errorMap.get("delete")).put(
                (recordIndex).toString(),
                (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                    conversionErrorList))).getErrors());
          }
          Map<String, Object> record = new HashMap<>();
          record.put("followup_id", followUpId);
          ((Map<String, Object>) ((Map<String, Object>) responseData.get("followup_details"))
              .get("delete")).put(recordIndex.toString(), record);
          recordIndex++;
        }

      }
    }

    if (isValid) {
      /*
       * Pendingprescription table is updated only when mod_pat_pending_prescription
       * module is enabled
       */
      boolean modPatPendingPres = 
          modulesActivatedService.isModuleActivated("mod_pat_pending_prescription");
      if (!insertBeans.isEmpty()) {
        followUpRepository.batchInsert(insertBeans);
        if (modPatPendingPres) {
          pendingPrescriptionsService.insertUpdateFollowUpPrescriptions("insert",insertBeans);
        }
      }
      if (!updateBeans.isEmpty()) {
        followUpRepository.batchUpdate(updateBeans, updateKeysMap);
        if (modPatPendingPres) {
          pendingPrescriptionsService.insertUpdateFollowUpPrescriptions("update",updateBeans);
        }
      }
      if (!deleteKeys.isEmpty()) {
        followUpRepository.batchDelete("followup_id", deleteKeys);
        if (modPatPendingPres) {
          pendingPrescriptionsService.deleteFollowUpPrescription(deleteKeys);
        }
      }
    } else {
      return null;
    }
    return responseData;
  }
}
