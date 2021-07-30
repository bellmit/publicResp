package com.insta.hms.core.clinical.patientproblems;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionDetailsRepository;
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
 * Patient Problem List Service.
 * 
 * @author VinayKumarJavalkar
 */
@Service
public class PatientProblemListService extends SystemSectionService {

  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private PatientProblemListRepository repository;

  @LazyAutowired
  private PatientProblemListDetailsRepository patientProblemListDetailsRepository;

  @LazyAutowired
  private PatientProblemListValidator patientProblemListValidator;

  @LazyAutowired
  private PatientProblemListDetailsValidator patientProblemListDetailsValidator;
  
  @LazyAutowired
  private SectionDetailsRepository sectionDetailsRepository;
  
  @LazyAutowired
  private RegistrationService registrationService;

  private static final String INSERT = "insert";
  private static final String UPDATE = "update";
  private static final String DELETE = "delete";

  public PatientProblemListService() {
    this.sectionId = -21;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    Map<String, Object> responseData = new HashMap<>();

    List<BasicDynaBean> insertPatProbBeansList = null;
    List<BasicDynaBean> insertPatProbDetailsBeansList = null;
    if (requestBody.get(INSERT) != null
        && !((List<Map<String, Object>>) requestBody.get(INSERT)).isEmpty()) {
      insertPatProbBeansList = new ArrayList<>();
      insertPatProbDetailsBeansList = new ArrayList<>();
      insertPatientProblem(requestBody, sdbean, parameter, errorMap, errMap, responseData,
          insertPatProbBeansList, insertPatProbDetailsBeansList);
    }

    Map<String, Object> updateKeysMap = null;
    List<BasicDynaBean> updatePatProbBeansList = null;
    List<BasicDynaBean> updatePatProbDetailsBeansList = null;
    if (requestBody.get(UPDATE) != null
        && !((List<Map<String, Object>>) requestBody.get(UPDATE)).isEmpty()) {
      updateKeysMap = new HashMap<>();
      updatePatProbBeansList = new ArrayList<>();
      updatePatProbDetailsBeansList = new ArrayList<>();
      updatePatientProblem(requestBody, sdbean, parameter, errorMap, errMap, responseData,
          updatePatProbBeansList, updatePatProbDetailsBeansList, updateKeysMap);
    }

    Map<String, Object> deleteKeysMap = null;
    List<BasicDynaBean> deletePatProbBeansList = null;
    if (requestBody.get(DELETE) != null
        && !((List<Map<String, Object>>) requestBody.get(DELETE)).isEmpty()) {
      deleteKeysMap = new HashMap<>();
      deletePatProbBeansList = new ArrayList<>();
      deletePatientProblem(requestBody, sdbean, errorMap, errMap, responseData,
          deletePatProbBeansList, deleteKeysMap);
    }

    if (errMap.getErrorMap().isEmpty()) {
      if ((insertPatProbBeansList != null) && (!insertPatProbBeansList.isEmpty())) {
        repository.batchInsert(insertPatProbBeansList);
        if (!insertPatProbDetailsBeansList.isEmpty()) {
          patientProblemListDetailsRepository.batchInsert(insertPatProbDetailsBeansList);
        }
      }
      if ((updatePatProbBeansList != null) && (!updatePatProbBeansList.isEmpty())) {
        repository.batchUpdate(updatePatProbBeansList, updateKeysMap);
        if (!updatePatProbDetailsBeansList.isEmpty()) {
          patientProblemListDetailsRepository.batchInsert(updatePatProbDetailsBeansList);
        }
      }
      if ((deletePatProbBeansList != null) && (!deletePatProbBeansList.isEmpty())) {
        repository.batchUpdate(deletePatProbBeansList, deleteKeysMap);
      }
    } else {
      return null;
    }
    return responseData;
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map<String, Object> data = getPatientProblems(parameter);
    data.put("section_detail_id", 0);
    data.put("section_id", this.sectionId);
    return data;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    Map<String, Object> data = getPatientProblems(parameter);
    data.put("section_detail_id", getSectionDetailId(parameter));
    data.put("section_id", this.sectionId);
    return data;
  }
  
  private int getSectionDetailId(FormParameter parameter) {
    BasicDynaBean sectionDetailsBean =
        sectionDetailsRepository.getRecord(parameter, this.sectionId);
    return sectionDetailsBean != null ? (int) sectionDetailsBean.get("section_detail_id") : 0;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getPatientProblems(FormParameter parameter) {
    List<Object> mapStructure =
        new ArrayList<Object>(Arrays.asList("mr_no"));
    List<Object> recordStructure = new ArrayList<Object>(
        Arrays.asList("ppl_id", "patient_problem_id", "ext_identified", "identified_by",
            "identified_by_name", "onset", "problem_note", "patient_problem_code", "recorded_by",
            "recorded_by_name", "patient_problem_desc", "code_type", "recorded_date"));
    mapStructure.add(recordStructure);
    int sectionDetailId = getSectionDetailId(parameter);
    Map<String, Object> patientProblemMap = ConversionUtils.convertToStructeredMap(
        repository.getPatientProblems(parameter, sectionDetailId), mapStructure, null);
    String startDateTime = repository.getStartTimeOfVisitOrConsultation(parameter);
    if (patientProblemMap.get("records") != null) {
      List<Map<String, Object>> patProbRecordsList =
          (List<Map<String, Object>>) patientProblemMap.get("records");
      BasicDynaBean patientProblemListDetailsBean = null;
      for (Map<String, Object> map : patProbRecordsList) {
        patientProblemListDetailsBean = patientProblemListDetailsRepository
            .getLastUpdatedProblemDetails((int) map.get("ppl_id"), sectionDetailId, startDateTime);
        map.putAll(patientProblemListDetailsBean.getMap());
      }
    } else {
      patientProblemMap.put("records", new ArrayList<>());
    }
    return patientProblemMap;
  }

  @SuppressWarnings("unchecked")
  private void insertPatientProblem(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap, ValidationErrorMap errMap,
      Map<String, Object> responseData, List<BasicDynaBean> insertPatProbBeansList,
      List<BasicDynaBean> insertPatProbDetailsBeansList) {
    BasicDynaBean patientProblemBean = null;
    BasicDynaBean patientProblemDetailsBean = null;

    responseData.put(INSERT, new HashMap<String, Object>());
    Integer recordIndex = 0;
    int pplId;
    for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(INSERT)) {
      // Insert in patient problem list table
      patientProblemBean = repository.getBean();
      List<String> conversionErrorList = new ArrayList<>();
      errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      ConversionUtils.copyJsonToDynaBean(row, patientProblemBean, conversionErrorList, false);
      patientProblemBean.set("mr_no", parameter.getMrNo());
      pplId = repository.getNextSequence();
      patientProblemBean.set("ppl_id", pplId);
      patientProblemBean.set("status", "A");
      patientProblemBean.set("section_detail_id", sdbean.get("section_detail_id"));
      patientProblemBean.set("created_in_sec_detail_id", sdbean.get("section_detail_id"));
      String usrId = (String) sessionService.getSessionAttributes().get("userId");
      patientProblemBean.set("created_by", usrId);
      patientProblemBean.set("created_at", requestBody.get("transaction_start_date"));
      patientProblemBean.set("modified_by", usrId);
      patientProblemBean.set("modified_at", requestBody.get("transaction_start_date"));
      patientProblemListValidator.validatePatientProblemListInsert(patientProblemBean, errMap);
      insertPatProbBeansList.add(patientProblemBean);

      // Insert in patient problem list details table
      patientProblemDetailsBean = patientProblemListDetailsRepository.getBean();
      ConversionUtils.copyJsonToDynaBean(row, patientProblemDetailsBean, conversionErrorList,
          false);
      patientProblemDetailsBean.set("ppld_id",
          patientProblemListDetailsRepository.getNextSequence());
      patientProblemDetailsBean.set("visit_id", parameter.getPatientId());
      patientProblemDetailsBean.set("ppl_id", pplId);
      patientProblemDetailsBean.set("created_by", usrId);
      patientProblemDetailsBean.set("created_at", requestBody.get("transaction_start_date"));
      patientProblemDetailsBean.set("section_detail_id", sdbean.get("section_detail_id"));
      patientProblemListDetailsValidator.validatePatientProblemListInsert(patientProblemDetailsBean,
          errMap);
      insertPatProbDetailsBeansList.add(patientProblemDetailsBean);

      if (!errMap.getErrorMap().isEmpty()) {
        if (!errorMap.containsKey(INSERT) || !conversionErrorList.isEmpty()) {
          errorMap.put(INSERT, new HashMap<String, Object>());
        }
        ((Map<String, Object>) errorMap.get(INSERT)).put((recordIndex).toString(),
            (new ValidationException(
                ValidationUtils.copyCoversionErrors(errMap, conversionErrorList))).getErrors());
      }
      Map<String, Object> record = new HashMap<>();
      record.put("ppl_id", patientProblemBean.get("ppl_id"));
      record.put("visit_id", patientProblemDetailsBean.get("visit_id"));
      record.put("visit_date",
          registrationService.getVisitDate(patientProblemDetailsBean.get("visit_id").toString()));
      ((Map<String, Object>) responseData.get(INSERT)).put(recordIndex.toString(), record);
      recordIndex++;
    }
  }

  @SuppressWarnings("unchecked")
  private void updatePatientProblem(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap, ValidationErrorMap errMap,
      Map<String, Object> responseData, List<BasicDynaBean> updatePatProbBeansList,
      List<BasicDynaBean> insertPatProbDetailsBeansList, Map<String, Object> updateKeysMap) {

    List<Object> updateKeys = new ArrayList<>();
    BasicDynaBean patientProblemBean = null;
    BasicDynaBean patientProblemDetailsBean = null;

    responseData.put(UPDATE, new HashMap<String, Object>());
    Integer recordIndex = 0;
    for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(UPDATE)) {
      patientProblemBean = repository.getBean();
      List<String> conversionErrorList = new ArrayList<>();
      errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      ConversionUtils.copyJsonToDynaBean(row, patientProblemBean, conversionErrorList, false);
      String usrId = (String) sessionService.getSessionAttributes().get("userId");
      patientProblemBean.set("modified_by", usrId);
      patientProblemBean.set("modified_at", requestBody.get("transaction_start_date"));
      patientProblemBean.set("section_detail_id", sdbean.get("section_detail_id"));
      patientProblemListValidator.validatePatientProblemListUpdate(patientProblemBean, errMap);
      updateKeys.add(patientProblemBean.get("ppl_id"));
      updatePatProbBeansList.add(patientProblemBean);

      // if status is modified, then insert a new record in patient_problem_list_details table
      String currentStatus = (String) row.get("problem_status");
      String startDateTime = repository.getStartTimeOfVisitOrConsultation(parameter);
      int sectionDetailId = getSectionDetailId(parameter);
      String previousStatus = patientProblemListDetailsRepository
          .getLastUpdatedProblemDetails((int) patientProblemBean.get("ppl_id"), sectionDetailId,
              startDateTime)
          .get("problem_status").toString();
      if (!currentStatus.equals(previousStatus)) {
        patientProblemDetailsBean = patientProblemListDetailsRepository.getBean();
        ConversionUtils.copyJsonToDynaBean(row, patientProblemDetailsBean, conversionErrorList,
            false);
        patientProblemDetailsBean.set("ppld_id",
            patientProblemListDetailsRepository.getNextSequence());
        patientProblemDetailsBean.set("ppl_id", patientProblemBean.get("ppl_id"));
        patientProblemDetailsBean.set("created_by", usrId);
        patientProblemDetailsBean.set("created_at", requestBody.get("transaction_start_date"));
        patientProblemDetailsBean.set("visit_id", parameter.getPatientId());
        patientProblemDetailsBean.set("section_detail_id", sdbean.get("section_detail_id"));
        patientProblemListDetailsValidator
            .validatePatientProblemListInsert(patientProblemDetailsBean, errMap);
        insertPatProbDetailsBeansList.add(patientProblemDetailsBean);
      }

      if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
        if (!errorMap.containsKey(UPDATE)) {
          errorMap.put(UPDATE, new HashMap<String, Object>());
        }
        ((Map<String, Object>) errorMap.get(UPDATE)).put((recordIndex).toString(),
            (new ValidationException(
                ValidationUtils.copyCoversionErrors(errMap, conversionErrorList))).getErrors());
      }
      Map<String, Object> record = new HashMap<>();
      record.put("ppl_id", patientProblemBean.get("ppl_id"));
      ((Map<String, Object>) responseData.get(UPDATE)).put(recordIndex.toString(), record);
      recordIndex++;
    }
    if (!updateKeys.isEmpty()) {
      updateKeysMap.put("ppl_id", updateKeys);
    }
  }

  @SuppressWarnings("unchecked")
  private void deletePatientProblem(Map<String, Object> requestBody, BasicDynaBean sdbean,
      Map<String, Object> errorMap, ValidationErrorMap errMap, Map<String, Object> responseData,
      List<BasicDynaBean> deletePatProbBeansList, Map<String, Object> deleteKeysMap) {
    List<Object> deleteKeys = new ArrayList<>();
    BasicDynaBean patientProblemBean = null;

    responseData.put(DELETE, new HashMap<String, Object>());
    Integer recordIndex = 0;
    for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(DELETE)) {
      patientProblemBean = repository.getBean();
      List<String> conversionErrorList = new ArrayList<>();
      errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      ConversionUtils.copyJsonToDynaBean(row, patientProblemBean, conversionErrorList, false);
      String usrId = (String) sessionService.getSessionAttributes().get("userId");
      patientProblemBean.set("status", "I");
      patientProblemBean.set("modified_by", usrId);
      patientProblemBean.set("modified_at", requestBody.get("transaction_start_date"));
      patientProblemBean.set("deleted_by", usrId);
      patientProblemBean.set("deleted_at", requestBody.get("transaction_start_date"));
      patientProblemBean.set("section_detail_id", sdbean.get("section_detail_id"));
      deleteKeys.add(patientProblemBean.get("ppl_id"));
      deletePatProbBeansList.add(patientProblemBean);

      if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
        if (!errorMap.containsKey(DELETE)) {
          errorMap.put(DELETE, new HashMap<String, Object>());
        }
        ((Map<String, Object>) errorMap.get(DELETE)).put((recordIndex).toString(),
            (new ValidationException(
                ValidationUtils.copyCoversionErrors(errMap, conversionErrorList))).getErrors());
      }
      Map<String, Object> record = new HashMap<>();
      record.put("ppl_id", patientProblemBean.get("ppl_id"));
      ((Map<String, Object>) responseData.get(DELETE)).put(recordIndex.toString(), record);
      recordIndex++;
    }
    if (!deleteKeys.isEmpty()) {
      deleteKeysMap.put("ppl_id", deleteKeys);
    }
  }
}
