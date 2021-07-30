package com.insta.hms.core.clinical.allergies;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.allergy.AllergenMasterRepository;
import com.insta.hms.mdm.allergy.AllergyTypeRepository;
import com.insta.hms.mdm.stores.genericnames.GenericNamesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Allergies Service.
 * 
 * @author krishnat
 *
 */
@Service
public class AllergiesService extends SystemSectionService {

  @LazyAutowired
  private AllergiesValidator allergiesValidator;
  @LazyAutowired
  private AllergiesRepository allergiesRepository;
  @LazyAutowired
  private GenericNamesService genericNamesService;
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;
  @LazyAutowired
  private AllergenMasterRepository allergenMasterRepository;
  @LazyAutowired
  private AllergyTypeRepository allergyTypeRepository;

  public AllergiesService() {
    this.sectionId = -2;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();

    boolean isValid = true;

    List<BasicDynaBean> insertBeans = new ArrayList<>();
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    Map<String, Object> responseData = new HashMap<>();

    if (requestBody.get("insert") != null) {
      responseData.put("insert", new HashMap<String, Object>());
      Integer recordIndex = 0;
      String userName = (String) RequestContext.getSession().getAttribute("userId");

      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("insert")) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean allergyBean = allergiesRepository.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, allergyBean, conversionErrorList, false);

        Integer allergyTypeId = (Integer) row.get(AllergiesConstants.ALLERGY_TYPE_ID);

        if (null != allergyTypeId && allergyTypeId != 0) {
          if (allergyTypeId == AllergiesConstants.MEDICINE_ALLERGY_TYPE_ID) {
            // To map the respective allergen code id with the selected generic name
            fetchAllergenCodeIdForGenericMedicine(row.get("generic_code"), allergyBean);
          } else {
            if (allergyBean.get(AllergiesConstants.ALLERGEN_CODE_ID) == null) {
              // To auto create an entry in allergen master
              int allergenCodeId = autoCreateAllergenEntry(userName, row.get("allergy").toString(),
                  allergyTypeId);
              allergyBean.set(AllergiesConstants.ALLERGEN_CODE_ID, allergenCodeId);
            }
          }
        }
        allergyBean.set("allergy_id", allergiesRepository.getNextSequence());
        allergyBean.set("section_detail_id", sdbean.get("section_detail_id"));
        allergyBean.set("username", userName);
        allergyBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));

        isValid = allergiesValidator.validateAllergyInsert(allergyBean, errMap) && isValid;
        insertBeans.add(allergyBean);

        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("insert")) {
            errorMap.put("insert", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("insert")).put((recordIndex).toString(),
              (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                  conversionErrorList))).getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("allergy_id", allergyBean.get("allergy_id"));
        ((Map<String, Object>) responseData.get("insert")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();

    if (requestBody.get("update") != null) {
      responseData.put("update", new HashMap<String, Object>());
      Integer recordIndex = 0;
      String userName = (String) RequestContext.getSession().getAttribute("userId");
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("update")) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean allergyBean = allergiesRepository.getBean();

        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, allergyBean, conversionErrorList, false);

        Integer allergyTypeId = (Integer) row.get(AllergiesConstants.ALLERGY_TYPE_ID);

        if (null != allergyTypeId && allergyTypeId != 0) {
          if (allergyTypeId == AllergiesConstants.MEDICINE_ALLERGY_TYPE_ID) {
            // To map the respective allergen code id with the selected generic name
            fetchAllergenCodeIdForGenericMedicine(row.get("generic_code"), allergyBean);
          } else {
            if (allergyBean.get(AllergiesConstants.ALLERGEN_CODE_ID) == null) {
              // To auto create an entry in allergen master
              int allergenCodeId = autoCreateAllergenEntry(userName, row.get("allergy").toString(),
                  allergyTypeId);
              allergyBean.set(AllergiesConstants.ALLERGEN_CODE_ID, allergenCodeId);
            }
          }
        }
        allergyBean.set("section_detail_id", sdbean.get("section_detail_id"));
        allergyBean.set("username", userName);
        allergyBean.set("mod_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        isValid = allergiesValidator.validateAllergyUpdate(allergyBean, errMap) && isValid;
        updateBeans.add(allergyBean);
        updateKeys.add(allergyBean.get("allergy_id"));
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("update")) {
            errorMap.put("update", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("update")).put((recordIndex).toString(),
              (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                  conversionErrorList))).getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("allergy_id", allergyBean.get("allergy_id"));
        ((Map<String, Object>) responseData.get("update")).put(recordIndex.toString(), record);
        recordIndex++;
      }
      updateKeysMap.put("allergy_id", updateKeys);
    }

    List<Object> deleteKeys = new ArrayList<>();

    if (requestBody.get("delete") != null) {
      responseData.put("delete", new HashMap<String, Object>());
      Integer recordIndex = 0;
      for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get("delete")) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        BasicDynaBean allergyBean = allergiesRepository.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyToDynaBean(row, allergyBean, conversionErrorList);
        deleteKeys.add(row.get("allergy_id"));
        isValid = allergiesValidator.validateAllergyDelete(allergyBean, errMap) && isValid;

        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey("delete")) {
            errorMap.put("delete", new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get("delete")).put((recordIndex).toString(),
              (new ValidationException(ValidationUtils.copyCoversionErrors(errMap,
                  conversionErrorList))).getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put("allergy_id", allergyBean.get("allergy_id"));
        ((Map<String, Object>) responseData.get("delete")).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }

    if (isValid) {
      if (!insertBeans.isEmpty()) {
        allergiesRepository.batchInsert(insertBeans);
      }
      if (!updateBeans.isEmpty()) {
        allergiesRepository.batchUpdate(updateBeans, updateKeysMap);
      }
      if (!deleteKeys.isEmpty()) {
        allergiesRepository.batchDelete("allergy_id", deleteKeys);
      }

      Map<String, Object> keys = new HashMap<>();
      keys.put("section_detail_id", sdbean.get("section_detail_id"));
      List<BasicDynaBean> allergydata = allergiesRepository.listAll(null, keys, null);
      isValid = allergiesValidator.validateAllergyType(allergydata, errMap);
      if (!errMap.getErrorMap().isEmpty()) {
        ValidationException ex = new ValidationException(errMap);
        errorMap.putAll(ex.getErrors());
      }
      if (!isValid) {
        return null;
      }
    } else {
      return null;
    }
    return responseData;
  }

  private int autoCreateAllergenEntry(String userName, String allergyString,
      int allergyBeanTypeId) {
    BasicDynaBean existingAllergenEntryBean = allergenMasterRepository.existingAllergenEntry(
        allergyString, allergyBeanTypeId);
    if (existingAllergenEntryBean != null) {
      return (int) existingAllergenEntryBean.get(AllergiesConstants.ALLERGEN_CODE_ID);
    }
    BasicDynaBean allergenMasterBean = allergenMasterRepository.getBean();
    allergenMasterBean.set(AllergiesConstants.ALLERGEN_CODE_ID, allergenMasterRepository
        .getNextId());
    allergenMasterBean.set(AllergiesConstants.ALLERGY_TYPE_ID, allergyBeanTypeId);
    allergenMasterBean.set("allergen_description", allergyString);
    allergenMasterBean.set("created_by", userName);
    allergenMasterBean.set("mod_user", userName);
    allergenMasterRepository.insert(allergenMasterBean);

    return (int) allergenMasterBean.get(AllergiesConstants.ALLERGEN_CODE_ID);
  }

  private void fetchAllergenCodeIdForGenericMedicine(Object genericCode,
      BasicDynaBean allergyBean) {
    if (genericCode instanceof String) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("generic_code", (String) genericCode);
      BasicDynaBean genericNamesBean = genericNamesService.findByPk(params);
      if (genericNamesBean != null) {
        allergyBean.set(AllergiesConstants.ALLERGEN_CODE_ID, genericNamesBean.get(
            AllergiesConstants.ALLERGEN_CODE_ID));
      }
    }
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    List<Object> mapStructure = new ArrayList<Object>(Arrays.asList("section_detail_id",
        "finalized", "section_id"));
    List<Object> recordStructure = new ArrayList<Object>(Arrays.asList("allergy_id",
        "allergy_type", "allergy", "allergy_type_id", "reaction", "onset_date",
        "severity", "status", "username", "created_at", "allergen_code_id", "allergy_type_name"));
    mapStructure.add(recordStructure);
    return ConversionUtils.convertToStructeredMap(allergiesRepository.getAllergies(parameter
        .getMrNo(), parameter.getPatientId(), parameter.getId(), parameter.getItemType(), parameter
            .getFormType(), parameter.getFormFieldName()), mapStructure, null);
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    data.put("section_detail_id", 0);
    data.put("finalized", "N");
    data.put("section_id", -2);
    data.put("records", ConversionUtils.listBeanToListMap(allergiesRepository.getAllActiveAllergies(
        parameter.getMrNo())));
    return data;
  }


  /**
   * Gets the all active allergies. used in DischargeSummary services need to check later if we can
   * use common method.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the all active allergies
   */
  public List<BasicDynaBean> getAllActiveAllergies(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return allergiesRepository.getAllActiveAllergies(mrNo, patientId, itemId, genericFormId, formId,
        itemType);
  }

  // get patient allergies to show in patient header
  public List<BasicDynaBean> getPatientRecentAllergies(String mrNo) {
    return allergiesRepository.getPatientRecentAllergies(mrNo);
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

}
