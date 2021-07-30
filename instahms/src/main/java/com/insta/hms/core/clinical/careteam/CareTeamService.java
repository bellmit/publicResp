package com.insta.hms.core.clinical.careteam;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class CareTeamService.
 *
 * @author anup vishwas
 */

@Service
public class CareTeamService {

  /** The care team repository. */
  @LazyAutowired
  private CareTeamRepository careTeamRepository;

  /** The patient registration service. */
  @LazyAutowired
  private PatientRegistrationService patientRegistrationService;

  /** The care team validator. */
  @LazyAutowired
  private CareTeamValidator careTeamValidator;

  /** The Constant DOCTOR_ID. */
  private static final String DOCTOR_ID = "doctor_id";

  /** The Constant PATIENT_ID. */
  private static final String PATIENT_ID = "patient_id";

  /** The Constant CARE_DOCTOR_ID. */
  private static final String CARE_DOCTOR_ID = "care_doctor_id";

  /** The Constant DELETED. */
  private static final String DELETED = "deleted";

  /** The Constant NEW. */
  private static final String NEW = "new";

  /** The Constant INSERT. */
  private static final String INSERT = "insert";

  /**
   * Gets the all visit details.
   *
   * @param doctorId the doctor id
   * @return the all visit details
   */
  public List<BasicDynaBean> getAllVisitDetails(String doctorId) {

    return careTeamRepository.getAllCareTeamList(doctorId);
  }

  /**
   * Care team visit list.
   *
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @return the list
   */
  public List<BasicDynaBean> careTeamVisitList(String mrNo, String doctorId) {

    return careTeamRepository.careTeamVisitList(mrNo, doctorId);
  }

  /**
   * Care team visit list.
   *
   * @param patientId the patient id
   * @return the list
   */
  public List<BasicDynaBean> careTeamVisitList(String patientId) {
    return careTeamRepository.careTeamVisitList(patientId);
  }

  /**
   * Save care team data.
   *
   * @param params the params
   * @param parameter the parameter
   * @param errorMap the error map
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> saveCareTeamData(List<Map<String, Object>> params,
      FormParameter parameter, Map<String, Object> errorMap) {
    boolean isValid = true;
    Map<String, Object> responseData = new HashMap<>();
    ValidationErrorMap insertErrMap = new ValidationErrorMap();
    ValidationErrorMap deleteErrMap = new ValidationErrorMap();
    List<BasicDynaBean> insertBeans = new ArrayList<>();
    Integer recordIndexInsert = 0;
    Integer recordIndexDelete = 0;
    List<String> columns = new ArrayList<>();
    columns.add(CARE_DOCTOR_ID);
    List<BasicDynaBean> existingTeamList =  careTeamRepository
        .listAll(columns, PATIENT_ID, parameter.getPatientId());
    BasicDynaBean regBean = patientRegistrationService
        .findByKey(PATIENT_ID, parameter.getPatientId());
    String addmitingDoctor =  (String) (regBean != null ? regBean.get("doctor") : "");
    for (Map<String, Object> row : params) {
      if (row.get("changeType") != null && row.get("changeType").equals(NEW)) {
        BasicDynaBean bean = careTeamRepository.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, bean, conversionErrorList, false);
        bean.set(PATIENT_ID, parameter.getPatientId());
        bean.set(CARE_DOCTOR_ID, row.get(DOCTOR_ID));
        isValid = careTeamValidator.commonValidation(bean, insertErrMap) && isValid;
        // Handle concurrent user issue.
        if (isValid && careTeamValidator.isDuplicateCareTeamDoctor(bean, existingTeamList)) {
          Map<String, Object> keys = new HashMap<>();
          keys.put(PATIENT_ID, parameter.getPatientId());
          keys.put(CARE_DOCTOR_ID, row.get(DOCTOR_ID));
          careTeamRepository.update(bean, keys);
        } else {
          insertBeans.add(bean);
          if (!insertErrMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
            if (!errorMap.containsKey(INSERT)) {
              errorMap.put(INSERT, new HashMap<String, Object>());
            }
            ((Map<String, Object>) errorMap.get(INSERT))
                .put((recordIndexInsert).toString(),
                    (new ValidationException(
                        ValidationUtils.copyCoversionErrors(insertErrMap, conversionErrorList)))
                            .getErrors());
          }
          recordIndexInsert++;
        }

      }

      if (row.get("changeType") != null && row.get("changeType").equals(DELETED)) {
        BasicDynaBean bean = careTeamRepository.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, bean, conversionErrorList, false);
        bean.set(PATIENT_ID, parameter.getPatientId());
        bean.set(CARE_DOCTOR_ID, row.get(DOCTOR_ID));
        Map<String, Object> keys = new HashMap<>();
        isValid = careTeamValidator
            .validateCareTeamOnDelete(bean, addmitingDoctor, deleteErrMap) && isValid;
        keys.put(PATIENT_ID, parameter.getPatientId());
        keys.put(CARE_DOCTOR_ID, row.get(DOCTOR_ID));

        if (!deleteErrMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey(DELETED)) {
            errorMap.put(DELETED, new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get(DELETED))
              .put((recordIndexDelete).toString(),
                  (new ValidationException(
                      ValidationUtils.copyCoversionErrors(deleteErrMap, conversionErrorList)))
                          .getErrors());
        }
        recordIndexDelete++;
        if (isValid) {
          careTeamRepository.delete(keys);
        }
      }
    }

    if (isValid) {
      if (!insertBeans.isEmpty()) {
        careTeamRepository.batchInsert(insertBeans);
      }
      List<BasicDynaBean> careTeamList = careTeamVisitList(parameter.getPatientId());
      responseData.put("careTeamList", ConversionUtils.listBeanToListMap(careTeamList));
    }

    return responseData;
  }
}
