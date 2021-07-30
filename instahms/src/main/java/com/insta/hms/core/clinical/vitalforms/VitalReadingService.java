package com.insta.hms.core.clinical.vitalforms;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.FormTemplateDataService;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.diagnosticmodule.laboratory.ResultExpressionProcessor;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.vitalparameter.referenceranges.ReferenceRangesService;
import com.insta.hms.mdm.vitalparameters.VitalParameterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class VitalReadingService.
 *
 * @author anup vishwas
 */

@Service
public class VitalReadingService extends SystemSectionService {
  
  private static Logger logger = LoggerFactory.getLogger(VitalReadingService.class); 
  /** The reading repo. */
  @LazyAutowired
  private VitalReadingRepository readingRepo;

  /** The visit vitals repo. */
  @LazyAutowired
  VisitVitalsRepository visitVitalsRepo;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The vital param service. */
  @LazyAutowired
  VitalParameterService vitalParamService;

  /** The validator. */
  @LazyAutowired
  VitalsValidator validator;

  /** The reg service. */
  @LazyAutowired
  RegistrationService regService;

  /** The reference ranges service. */
  @LazyAutowired
  ReferenceRangesService referenceRangesService;

  /** The patient details service. */
  @LazyAutowired
  PatientDetailsService patientDetailsService;

  /** The form template data service. */
  @LazyAutowired
  private FormTemplateDataService formTemplateDataService;

  /**
   * Instantiates a new vital reading service.
   */
  public VitalReadingService() {
    this.sectionId = -4;
  }

  /**
   * Gets the vital readings.
   *
   * @param patientId the patient id
   * @param paramContainer the param container
   * @return the vital readings
   */
  public List<BasicDynaBean> getVitalReadings(String patientId, String paramContainer) {
    return readingRepo.getVitalReadings(patientId, paramContainer);
  }

  /**
   * Save section.
   *
   * @param dataMap the data map
   * @param errorMap the error map
   * @return the map
   */
  public Map<String, Object> saveVitals(Map dataMap, Map<String, Object> errorMap) {
    FormParameter parameter = new FormParameter(null, null, null,
        (String) dataMap.get("patient_id"), null, null);
    logger.info("VITALS VISIT_ID  :" + dataMap.get("patient_id"));
    saveSection(dataMap, null, parameter, errorMap);
    return errorMap;
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
    List<BasicDynaBean> list = vitalParamService.getAllParams("V", "O");

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = true;

    // get all the labels which are used to process any ftl expression labels in vital parameters.
    // List<String> labels = new ArrayList<String>();
    Map<Integer, String> exprForParam = new HashMap<Integer, String>();
    for (BasicDynaBean vparam : list) {
      // labels.add((String) vparam.get("param_label"));
      exprForParam.put((Integer) vparam.get("param_id"),
          (String) vparam.get("expr_for_calc_result"));
    }

    List<Map<String, Object>> insertRecords = (List<Map<String, Object>>) requestBody.get("insert");
    List<BasicDynaBean> vinserts = new ArrayList<>();
    List<BasicDynaBean> rinserts = new ArrayList<>();
    Map<String, Object> responseData = new HashMap<>();
    if (insertRecords != null && !insertRecords.isEmpty()) {
      responseData.put("insert", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> entry : insertRecords) {

        BasicDynaBean visitVitalBean = visitVitalsRepo.getBean();
        List<String> errors = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(entry, visitVitalBean, errors, false);

        int readingId = visitVitalsRepo.getNextSequence();
        visitVitalBean.set("vital_reading_id", readingId);
        visitVitalBean.set("user_name", sessionService.getSessionAttributes().get("userId"));
        visitVitalBean.set("patient_id", parameter.getPatientId());

        isValid = validator.validateVisitVitalsInsert(visitVitalBean, errMap) && isValid;

        if (!errMap.getErrorMap().isEmpty() || !errors.isEmpty()) {
          if (!errorMap.containsKey("insert")) {
            errorMap.put("insert", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("insert")).put((recordIndex).toString(),
              (new ValidationException(errMap)).getErrors());
        }

        Map<String, Object> record = new HashMap<>();
        record.put("vital_reading_id", visitVitalBean.get("vital_reading_id"));
        record.put("user_name", visitVitalBean.get("user_name"));
        ((Map<String, Object>) responseData.get("insert")).put(recordIndex.toString(), record);
        recordIndex++;
        vinserts.add(visitVitalBean);

        List<Map<String, Object>> values = (List<Map<String, Object>>) entry.get("records");
        List<String> paramValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Map<String, Object> val : values) {
          labels.add((String) val.get("param_label"));
          paramValues.add((String) val.get("param_value"));
        }
        Integer paramIndex = 0;
        for (Map<String, Object> val : values) {
          BasicDynaBean readingBean = readingRepo.getBean();
          ConversionUtils.copyJsonToDynaBean(val, readingBean, errors, false);
          readingBean.set("username", sessionService.getSessionAttributes().get("userId"));
          readingBean.set("mod_time", DateUtil.getCurrentTimestamp());
          readingBean.set("vital_reading_id", readingId);

          isValid = validator.validateVitalReadingInsert(readingBean, errMap) && isValid;

          if (!errMap.getErrorMap().isEmpty() || !errors.isEmpty()) {
            if (!errorMap.containsKey("insert")) {
              errorMap.put("insert", new HashMap<String, Object>());
            }
            Map<String, Object> map = (Map<String, Object>) errorMap.get("insert");
            if (!map.containsKey("records")) {
              map.put("records", new HashMap<String, Object>());
            }
            ((Map<String, Object>) ((Map<String, Object>) errorMap.get("insert")).get("records"))
                .put((paramIndex++).toString(), (new ValidationException(errMap)).getErrors());
          }

          String expr = exprForParam.get((Integer) readingBean.get("param_id"));
          if (expr != null && !expr.equals("")) {
            try {
              String result = ResultExpressionProcessor.processResultExpressionForLAB(labels,
                  paramValues, expr);
              readingBean.set("param_value", result);
            } catch (Exception ce) {
              throw new ConversionException(ce);
            }
          }
          rinserts.add(readingBean);
        }

      }
    }
    List<Map<String, Object>> updateRecords = (List<Map<String, Object>>) requestBody.get("update");

    List<BasicDynaBean> vupdates = new ArrayList<>();
    Map<String, Object> vupdateKeysMap = new HashMap<>();
    List<Object> vupdateKeys = new ArrayList<>();

    Map<String, Object> rupdateKeysMap = new HashMap<>();
    List<Object> readingIdKeys = new ArrayList<>();
    List<Object> paramIdKeys = new ArrayList<>();

    if (updateRecords != null && !updateRecords.isEmpty()) {
      responseData.put("update", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> entry : updateRecords) {
        BasicDynaBean visitVitalBean = visitVitalsRepo.getBean();

        List errors = new ArrayList();
        ConversionUtils.copyJsonToDynaBean(entry, visitVitalBean, errors, false);
        visitVitalBean.set("user_name", sessionService.getSessionAttributes().get("userId"));

        isValid = validator.validateVisitVitalsUpdate(visitVitalBean, errMap) && isValid;

        if (!errMap.getErrorMap().isEmpty() || !errors.isEmpty()) {
          if (!errorMap.containsKey("update")) {
            errorMap.put("update", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("update")).put((recordIndex++).toString(),
              (new ValidationException(errMap)).getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("vital_reading_id", visitVitalBean.get("vital_reading_id"));
        record.put("user_name", visitVitalBean.get("user_name"));
        ((Map<String, Object>) responseData.get("update")).put(recordIndex.toString(), record);
        recordIndex++;
        Integer readingId = (Integer) entry.get("vital_reading_id");
        vupdates.add(visitVitalBean);
        vupdateKeys.add(readingId);

        List<Map<String, Object>> values = (List<Map<String, Object>>) entry.get("records");
        // get all the param values to execute the ftl expression if exists.
        List<String> paramValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (Map<String, Object> val : values) {
          labels.add((String) val.get("param_label"));
          paramValues.add((String) val.get("param_value"));
        }
        Integer paramIndex = 0;
        for (Map<String, Object> val : values) {
          BasicDynaBean readingBean = readingRepo.getBean();
          Map<String, Object> readingUpdateKeys = new HashMap<>();
          ConversionUtils.copyJsonToDynaBean(val, readingBean, errors, false);
          readingBean.set("username", sessionService.getSessionAttributes().get("userId"));
          readingBean.set("mod_time", DateUtil.getCurrentTimestamp());
          readingBean.set("vital_reading_id", readingId);

          String expr = exprForParam.get((Integer) readingBean.get("param_id"));
          if (expr != null && !expr.equals("")) {
            try {
              String result = ResultExpressionProcessor.processResultExpressionForLAB(labels,
                  paramValues, expr);
              readingBean.set("param_value", result);
            } catch (Exception ce) {
              throw new ConversionException(ce);
            }
          }

          // params can be added and deleted form edit vital reading form
          if (val.get("changeType") != null && val.get("changeType").equals("new")) {
            isValid = validator.validateVitalReadingInsert(readingBean, errMap) && isValid;
            if (!errMap.getErrorMap().isEmpty() || !errors.isEmpty()) {
              if (!errorMap.containsKey("insert")) {
                errorMap.put("insert", new HashMap<String, Object>());
              }
              Map<String, Object> map = (Map<String, Object>) errorMap.get("insert");
              if (!map.containsKey("records")) {
                map.put("records", new HashMap<String, Object>());
              }
              ((Map<String, Object>) ((Map<String, Object>) errorMap.get("insert")).get("records"))
                  .put((paramIndex++).toString(), (new ValidationException(errMap)).getErrors());
            }

            isValid = readingRepo.insert(readingBean) > 0 && isValid;

          } else if (val.get("changeType") != null && val.get("changeType").equals("deleted")) {
            isValid = validator.validateDelete(entry, errMap) && isValid;

            if (!errMap.getErrorMap().isEmpty()) {
              if (!errorMap.containsKey("delete")) {
                errorMap.put("delete", new HashMap<String, Object>());
              }
              ((Map<String, Object>) errorMap.get("delete")).put((recordIndex).toString(),
                  (new ValidationException(errMap)).getErrors());
            }

            isValid = readingRepo.delete("param_id", (Integer) readingBean.get("param_id")) > 0
                && isValid;

          } else {
            isValid = validator.validateVitalReadingUpdate(readingBean, errMap) && isValid;

            if (!errMap.getErrorMap().isEmpty() || !errors.isEmpty()) {
              if (!errorMap.containsKey("update")) {
                errorMap.put("update", new HashMap<String, Object>());
              }
              Map<String, Object> map = (Map<String, Object>) errorMap.get("update");
              if (!map.containsKey("records")) {
                map.put("records", new HashMap<String, Object>());
              }
              ((Map<String, Object>) ((Map<String, Object>) errorMap.get("update")).get("records"))
                  .put((paramIndex++).toString(), (new ValidationException(errMap)).getErrors());
            }
            readingUpdateKeys.put("vital_reading_id", readingId);
            readingUpdateKeys.put("param_id", val.get("param_id"));
            isValid = readingRepo.update(readingBean, readingUpdateKeys) > 0 && isValid;
          }
        }
      }
      vupdateKeysMap.put("vital_reading_id", vupdateKeys);
    }

    List<Map<String, Object>> deleteRecords = (List<Map<String, Object>>) requestBody.get("delete");
    List<Object> delete = new ArrayList<>();
    List<BasicDynaBean> vvdelete = new ArrayList<>();
    List<BasicDynaBean> vrdelete = new ArrayList<>();
    Map<String, Object> vdeleteKeysMap = new HashMap<>();
    List<Object> vdeleteKeys = new ArrayList<>();
    if (deleteRecords != null && !deleteRecords.isEmpty()) {
      responseData.put("delete", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> entry : deleteRecords) {
        isValid = validator.validateDelete(entry, errMap) && isValid;

        if (!errMap.getErrorMap().isEmpty()) {
          if (!errorMap.containsKey("delete")) {
            errorMap.put("delete", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("delete")).put((recordIndex).toString(),
              (new ValidationException(errMap)).getErrors());
        }
        vdeleteKeys.add(entry.get("vital_reading_id"));
        BasicDynaBean vrBean = readingRepo.findByKey("vital_reading_id",
                entry.get("vital_reading_id"));
        BasicDynaBean vvBean = visitVitalsRepo.findByKey("vital_reading_id",
                entry.get("vital_reading_id"));
        vrBean.set("status","I");
        vrdelete.add(vrBean);
        vvBean.set("status","I");
        vvdelete.add(vvBean);
      }
      vdeleteKeysMap.put("vital_reading_id", vdeleteKeys);
    }

    if (isValid) {
      if (!vinserts.isEmpty()) {
        visitVitalsRepo.batchInsert(vinserts);
      }
      if (!rinserts.isEmpty()) {
        readingRepo.batchInsert(rinserts);
      }
      if (!vupdates.isEmpty()) {
        visitVitalsRepo.batchUpdate(vupdates, vupdateKeysMap);
      }
      if (!vrdelete.isEmpty()) {
        readingRepo.batchUpdate(vrdelete,vdeleteKeysMap);
      }
      if (!vvdelete.isEmpty()) {
        visitVitalsRepo.batchUpdate(vvdelete,vdeleteKeysMap);
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
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromCurrentForm(com.insta.hms
   * .core.clinical.forms.FormParameter)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    List<BasicDynaBean> vitals = readingRepo.getVitals(parameter);
    if (vitals.size() == 1
        && (null == vitals.get(0).get("param_id") || vitals.get(0).get("param_id").equals(""))) {
      Map<String, Object> responce = new HashMap<>();
      responce.put("section_id", -4);
      responce.put("section_detail_id", vitals.get(0).get("section_detail_id"));
      responce.put("finalized", "N");
      responce.put("records", new ArrayList<>());

      return responce;
    } else {
      // set applicable reference range to vital params
      vitals = referenceRangesService.setApplicableReferenceRange(vitals, parameter.getMrNo());
    }
    List<Object> mapStructure = new ArrayList<>();
    String[] sd = new String[] {"section_id", "finalized", "section_detail_id"};
    mapStructure.addAll(Arrays.asList(sd));

    String[] reading = new String[] {"patient_id", "vital_reading_id", "date_time", "user_name",
        "vital_status", "finalized_by", "finalized_date_time"};
    List rl = new ArrayList(Arrays.asList(reading));
    String[] values = new String[] {"param_id", "param_value", "param_label", "param_uom",
        "param_remarks", "param_order", "reference_range_color_code"};
    rl.add(Arrays.asList(values));

    mapStructure.add(rl);
    return ConversionUtils.convertToStructeredMap(vitals, mapStructure, "vital_reading_id");
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
    List<BasicDynaBean> vitals = readingRepo.getActiveVitals(parameter);
    if (vitals == null || vitals.isEmpty()) {
      Map<String, Object> responce = new HashMap<>();
      responce.put("section_id", -4);
      responce.put("section_detail_id", 0);
      responce.put("finalized", "N");
      responce.put("records", new ArrayList<>());

      // get latest vitals which is captured within 24 hours, if not get only height and weight
      List<BasicDynaBean> latestvitals =
          readingRepo.getLatestVitals(parameter.getMrNo(), parameter.getPatientId());
      if (latestvitals.isEmpty()) {
        latestvitals =
            readingRepo.getHeightAndWeight(parameter.getMrNo(), parameter.getPatientId());
      }
      responce.put("latest_vitals", ConversionUtils.listBeanToListMap(latestvitals));

      return responce;
    } else {
      // set applicable reference range to vital params
      vitals = referenceRangesService.setApplicableReferenceRange(vitals, parameter.getMrNo());
    }

    List<Object> mapStructure = new ArrayList<Object>();
    String[] sd = new String[] {"section_id", "finalized", "section_detail_id"};
    mapStructure.addAll(Arrays.asList(sd));

    String[] reading = new String[] {"patient_id", "vital_reading_id", "date_time", "user_name",
        "vital_status", "finalized_by", "finalized_date_time"};
    List rl = new ArrayList(Arrays.asList(reading));
    String[] values = new String[] {"param_id", "param_value", "param_label", "param_uom",
        "param_remarks", "param_order", "reference_range_color_code"};
    rl.add(Arrays.asList(values));

    mapStructure.add(rl);

    return ConversionUtils.convertToStructeredMap(vitals, mapStructure, "vital_reading_id");
  }

  /**
   * Gets the visit vital weight bean.
   *
   * @param patientId the patient id
   * @return the visit vital weight bean
   */
  public BasicDynaBean getVisitVitalWeightBean(String patientId) {
    return visitVitalsRepo.getVisitVitalWeightBean(patientId);
  }

  /**
   * Gets the default applicable vitals.
   *
   * @param centerId the center id
   * @param deptId the dept id
   * @param visitType the visit type
   * @return the default applicable vitals
   */
  public List<BasicDynaBean> getDefaultApplicableVitals(int centerId, String deptId,
      String visitType) {
    return readingRepo.getDefaultApplicableVitals(centerId, deptId, visitType);
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

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#processTemplateData(com.insta.hms.core.
   * clinical.forms.FormParameter, java.util.Map, java.util.Map, java.lang.Integer)
   */
  @Override
  public void processTemplateData(FormParameter parameters, Map<String, Object> templateData,
      Map<String, Object> responseData, Integer formId) {
    responseData.put("template_vitals", formTemplateDataService.getTemplateVitals(formId));
  }

  /**
   * Gets the vital expr data.
   *
   * @param requestBody the request body
   * @return the vital expr data
   */
  public Map<String, List<Map<String, Object>>> getVitalExprData(Map<String, Object> requestBody) {

    List<Map<String, Object>> vitalRecords = (List<Map<String, Object>>) requestBody.get("vitals");
    List<BasicDynaBean> list = vitalParamService.getAllParams("V", "O");
    Map<String, List<Map<String, Object>>> responseData =
        new HashMap<String, List<Map<String, Object>>>();
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    responseData.put("records", dataList);

    // get all the labels which are used to process any ftl expression labels in vital parameters.
    Map<Integer, String> exprForParam = new HashMap<Integer, String>();
    for (BasicDynaBean vparam : list) {
      exprForParam.put((Integer) vparam.get("param_id"),
          (String) vparam.get("expr_for_calc_result"));
    }

    if (vitalRecords != null && vitalRecords.size() > 0) {
      List<String> errors = new ArrayList<String>();
      for (Map<String, Object> entry : vitalRecords) {
        List<Map<String, Object>> values = (List<Map<String, Object>>) entry.get("records");
        List<String> paramValues = new ArrayList<String>();
        List<String> labels = new ArrayList<String>();
        for (Map<String, Object> val : values) {
          labels.add((String) val.get("param_label"));
          paramValues.add((String) val.get("param_value"));
        }
        for (Map<String, Object> val : values) {
          BasicDynaBean readingBean = readingRepo.getBean();
          ConversionUtils.copyJsonToDynaBean(val, readingBean, errors, false);
          String expr = exprForParam.get((Integer) readingBean.get("param_id"));
          if (expr != null && !expr.equals("")) {
            try {
              String result = ResultExpressionProcessor.processResultExpressionForLAB(labels,
                  paramValues, expr);
              readingBean.set("param_value", result);
            } catch (Exception ce) {
              throw new ConversionException(ce);
            }
          }
          dataList.add(readingBean.getMap());
        }

      }
    }

    return responseData;
  }
}
