package com.insta.hms.core.clinical.diagnosisdetails;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.diagnosiscodefavourites.DiagnosisCodeFavouritesService;
import com.insta.hms.mdm.healthauthoritypreferences.HealthAuthorityPreferencesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishnat.
 *
 */

@Service
public class DiagnosisDetailsService extends SystemSectionService {

  @LazyAutowired
  DiagnosisDetailsRepository mrdDiagnosisRepo;
  @LazyAutowired
  HospitalClaimDiagnosisRepository coderDiagnosisRepo;
  @LazyAutowired
  DiagnosisDetailsValidator validator;
  @LazyAutowired
  private RegistrationService registrationService;
  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private DiagnosisCodeFavouritesService diagCodeFavService;
  @LazyAutowired
  private HealthAuthorityPreferencesService healthAuthorityPrefService;
  
  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(DiagnosisDetailsService.class);

  public DiagnosisDetailsService() {
    this.sectionId = -6;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = true;
    List<BasicDynaBean> insertMrdDiagBeanList = new ArrayList<>();
    List<BasicDynaBean> updateMrdDiagBeanList = new ArrayList<>();
    List<BasicDynaBean> insertDiagCodeFavBeanList = new ArrayList<>();

    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    List<Object> deleteKeys = new ArrayList<>();

    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    String patientId = parameter.getPatientId();
    int centerId = (Integer) registrationService.findByKey(patientId).get("center_id");
    String healthPrefDiagCodeType = (String) healthAuthorityPrefService.listBycenterId(centerId)
        .get("diagnosis_code_type");
    Map<String, Object> responseData = new HashMap<>();

    if (requestBody.get("delete") != null
        && !((List<Map<String, Object>>) requestBody.get("delete")).isEmpty()) {
      responseData.put("delete", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("delete")) {
        BasicDynaBean diagDetailsBean = mrdDiagnosisRepo.getBean();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyToDynaBean(row, diagDetailsBean, conversionErrorList);
        deleteKeys.add(row.get("id"));
        isValid = validator.validateDiagDetailsDelete(insertMrdDiagBeanList, updateMrdDiagBeanList,
            diagDetailsBean, errMap) && isValid;
        if (!errMap.getErrorMap().isEmpty()) {
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
        record.put("id", diagDetailsBean.get("id"));
        ((Map<String, Object>) responseData.get("delete")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    if (requestBody.get("insert") != null
        && !((List<Map<String, Object>>) requestBody.get("insert")).isEmpty()) {
      responseData.put("insert", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("insert")) {
        BasicDynaBean diagDetailsBean = mrdDiagnosisRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        ConversionUtils.copyJsonToDynaBean(row, diagDetailsBean, conversionErrorList, false);
        String codeType = (String) row.get("code_type");

        diagDetailsBean.set("username", userName);
        diagDetailsBean.set("visit_id", patientId);
        diagDetailsBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        String diagCodeFavourite = (String) row.get("diag_code_favourite");
        if (null != diagCodeFavourite && diagCodeFavourite.equals("Y")) {
          BasicDynaBean diagCodeFavBean = diagCodeFavService.getBean();
          diagCodeFavBean.set("code_doc_id", diagCodeFavService.getNextSequence());
          diagCodeFavBean.set("code_type", healthPrefDiagCodeType);
          diagCodeFavBean.set("code", diagDetailsBean.get("icd_code"));
          diagCodeFavBean.set("doctor_id", diagDetailsBean.get("doctor_id"));
          isValid = diagCodeFavService.isDuplicateFavDiagCode(diagCodeFavBean, errMap) && isValid;
          insertDiagCodeFavBeanList.add(diagCodeFavBean);
        }
        if (diagDetailsBean.get("id") == null) {
          diagDetailsBean.set("id", new BigDecimal(mrdDiagnosisRepo.getNextSequence()));
          diagDetailsBean.set("code_type", healthPrefDiagCodeType);
          isValid = validator.validateDiagDetailsInsert(diagDetailsBean, errMap) && isValid;
          insertMrdDiagBeanList.add(diagDetailsBean);
        } else { // in case of follow up
          BasicDynaBean mrdDiagBean = mrdDiagnosisRepo.findByKey("id", diagDetailsBean.get("id"));
          if (mrdDiagBean != null && mrdDiagBean.get("visit_id").equals(patientId)) {
            diagDetailsBean.set("code_type", codeType);
            updateKeys.add(diagDetailsBean.get("id"));
            isValid = validator.validateDiagDetailsUpdate(diagDetailsBean, errMap) && isValid;
            updateMrdDiagBeanList.add(diagDetailsBean);
          } else {
            diagDetailsBean.set("id", new BigDecimal(mrdDiagnosisRepo.getNextSequence()));
            diagDetailsBean.set("code_type", codeType);
            isValid = validator.validateDiagDetailsInsert(diagDetailsBean, errMap) && isValid;
            insertMrdDiagBeanList.add(diagDetailsBean);
          }
        }

        if (!errMap.getErrorMap().isEmpty()) {
          if (!errorMap.containsKey("insert") || !conversionErrorList.isEmpty()) {
            errorMap.put("insert", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("insert"))
              .put((recordIndex).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("id", diagDetailsBean.get("id"));
        ((Map<String, Object>) responseData.get("insert")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }
    if (requestBody.get("update") != null
        && !((List<Map<String, Object>>) requestBody.get("update")).isEmpty()) {
      responseData.put("update", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("update")) {
        BasicDynaBean diagDetailsBean = mrdDiagnosisRepo.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        ConversionUtils.copyJsonToDynaBean(row, diagDetailsBean, conversionErrorList, false);
        diagDetailsBean.set("username", userName);
        diagDetailsBean.set("visit_id", patientId);
        diagDetailsBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        updateKeys.add(diagDetailsBean.get("id"));
        isValid = validator.validateDiagDetailsUpdate(diagDetailsBean, errMap) && isValid;
        updateMrdDiagBeanList.add(diagDetailsBean);

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
        record.put("id", diagDetailsBean.get("id"));
        ((Map<String, Object>) responseData.get("update")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    if (!updateKeys.isEmpty()) {
      updateKeysMap.put("id", updateKeys);
    }

    if (isValid) {
      if (!insertMrdDiagBeanList.isEmpty()) {
        mrdDiagnosisRepo.batchInsert(insertMrdDiagBeanList);
        if (!insertDiagCodeFavBeanList.isEmpty()) {
          diagCodeFavService.batchInsertDiagCodeFavList(insertDiagCodeFavBeanList);
        }
      }
      if (!updateMrdDiagBeanList.isEmpty()) {
        mrdDiagnosisRepo.batchUpdate(updateMrdDiagBeanList, updateKeysMap);
      }
      if (!deleteKeys.isEmpty()) {
        mrdDiagnosisRepo.batchDelete("id", deleteKeys);
      }

      errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      if (isDuplicateDiagnosisCode(patientId)) {
        errMap.addError("others", "exception.diagdetail.duplicate.icdcode");
      }
      String msg = checkPrincipleDiagnosis(patientId);
      if (msg != null) {
        errMap.addError("others", msg);
      }
      if (!errMap.getErrorMap().isEmpty()) {
        errorMap.putAll(new ValidationException(errMap).getErrors());
      }
    } else {
      //Additional check to ensure diagnosis is not missing
      String msg = checkPrincipleDiagnosis(patientId);
      if (msg != null) {
        if (errMap.getErrorMap() == null) {
          errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());          
        }
        errMap.addError("others", msg);
        errorMap.putAll(new ValidationException(errMap).getErrors());
      }
      return null;
    }
    return responseData;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    List<Object> mapStructure = new ArrayList<Object>(
        Arrays.asList("section_detail_id", "patient_id", "section_id"));
    List<Object> recordStructure = new ArrayList<Object>(
        Arrays.asList("id", "description", "icd_code", "code_type", "diag_type", "username",
            "diagnosis_status_id", "remarks", "doctor_id", "diagnosis_datetime", "doctor_name",
            "diagnosis_status_name", "year_of_onset", "is_year_of_onset_mandatory"));
    mapStructure.add(recordStructure);
    return ConversionUtils.convertToStructeredMap(
        mrdDiagnosisRepo.getAllDiagnosisDetails(parameter), mapStructure, null);
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    List<BasicDynaBean> allDiagList = mrdDiagnosisRepo
        .getAllActiveDiagnosisDetails(parameter.getPatientId());
    Map<String, Object> data = new HashMap<>();
    data.put("section_id", -6);
    data.put("section_detail_id", 0);
    data.put("records", ConversionUtils.listBeanToListMap(allDiagList));
    return data;
  }

  /**
   * checks for duplicate diagnosis codes saved for the patient.
   */
  public boolean isDuplicateDiagnosisCode(String patientId) {
    boolean isDuplicate = false;
    List<BasicDynaBean> list = mrdDiagnosisRepo.isDuplicateDiagnosisCode(patientId);
    List<BasicDynaBean> filterlist = new ArrayList<>();
    if (!list.isEmpty()) {
      for (BasicDynaBean bean : list) {
        if (!filterlist.isEmpty()) {
          for (BasicDynaBean filter : filterlist) {
            if (filter.get("icd_code").equals(bean.get("icd_code"))) {
              if (filter.get("diag_type").equals("V") || bean.get("diag_type").equals("V")) {
                isDuplicate = filter.get("diag_type").equals(bean.get("diag_type"));
              } else {
                isDuplicate = true;
              }
            }
            if (isDuplicate) {
              break;
            }
          }
        }
        filterlist.add(bean);
      }

    }
    return isDuplicate;
  }

  /**
   * checks for principle diagnosis.
   */
  public String checkPrincipleDiagnosis(String patientId) {
    List<BasicDynaBean> list = mrdDiagnosisRepo.diagnosisCountList(patientId);
    String message = null;
    if (!list.isEmpty()) {
      if (list.size() == 1) {
        String diagType = (String) list.get(0).get("diag_type");
        if (!diagType.equals("P")) {
          message = "exception.diagdetail.requireprimary.icdcode";
          return message;
        }
      }
      int primaryDiagTypeCount = 0;
      for (BasicDynaBean bean : list) {
        long count = (long) bean.get("count");
        String diagType = (String) bean.get("diag_type");
        if (diagType.equals("P")) {
          if (count == 0) {
            message = "exception.diagdetail.requireprimary.icdcode";
            return message;
          } else if (count == 1) {
            logger.info("Executing Empty block");
          } else {
            message = "exception.diagdetail.mupltipleprimary.icdcode";
            return message;
          }
          primaryDiagTypeCount++;
        }
      }
      if (primaryDiagTypeCount == 0) {
        message = "exception.diagdetail.requireprimary.icdcode";
        return message;
      }
    }
    return null;
  }

  public List<BasicDynaBean> findAllDiagnosis(String mainVisitId) {
    return mrdDiagnosisRepo.findAllDiagnosis(mainVisitId);
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  /**
   * Gets the all doctor diagnosis details.
   *
   * @param patientId the patient id
   * @return the all diagnosis details
   */
  public List<BasicDynaBean> getAllDiagnosisDetails(String patientId) {
    return mrdDiagnosisRepo.getAllDiagnosisDetails(patientId);
  }

  /**
   * Gets the all coder diagnosis details.
   *
   * @param patientId the patient id
   * @return the all diagnosis details
   */
  public List<BasicDynaBean> getAllCoderDiagnosisDetails(String patientId) {
    if (patientId == null || patientId.isEmpty()) {
      return new ArrayList<>();
    }
    List<String> columns = new ArrayList<>();
    columns.add("visit_id");
    columns.add("description");
    columns.add("icd_code");
    columns.add("code_type");
    columns.add("diag_type");
    columns.add("mod_time");
    columns.add("year_of_onset");
    columns.add("remarks");
    columns.add("present_on_admission");
    return coderDiagnosisRepo.listAll(columns, "visit_id", patientId, "diag_type");
  }
}
