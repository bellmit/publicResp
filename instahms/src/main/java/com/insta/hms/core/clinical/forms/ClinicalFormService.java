package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.patientproblems.PatientProblemListDetailsRepository;
import com.insta.hms.core.clinical.prescriptions.PatientMedicinePrescriptionsService;
import com.insta.hms.core.clinical.vitalforms.VitalReadingService;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.prints.PrintService;
import com.insta.hms.forms.FormService;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.mdm.allergy.AllergenMasterService;
import com.insta.hms.mdm.diagnosiscodefavourites.DiagnosisCodeFavouritesService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.icdcodes.IcdCodesService;
import com.insta.hms.mdm.icdsupportedcodes.IcdSupportedCodesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.notetypes.NoteTypesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FormService.
 *
 * @author krishnat
 */
@Service
public abstract class ClinicalFormService extends FormService {

  /** The vital reading service. */
  @LazyAutowired
  private VitalReadingService vitalReadingService;

  /** The image markers service. */
  @LazyAutowired
  private ImageMarkersService imageMarkersService;

  /** The allergies service. */
  @LazyAutowired
  private AllergiesService allergiesService;

  /** The mrd diag service. */
  @LazyAutowired
  private MRDDiagnosisService mrdDiagService;

  /** The diag code fav service. */
  @LazyAutowired
  private DiagnosisCodeFavouritesService diagCodeFavService;

  /** The icd supported codes service. */
  @LazyAutowired
  private IcdSupportedCodesService icdSupportedCodesService;

  /** The mrd diagnosis service. */
  @LazyAutowired
  private MRDDiagnosisService mrdDiagnosisService;

  /** The patient insurance plans service. */
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;
  
  @LazyAutowired
  private PatientProblemListDetailsRepository patientProblemListDetailsRepository;
  
  @LazyAutowired
  protected IcdCodesService icdCodesService;

  @LazyAutowired
  protected PrintService printService;

  @LazyAutowired
  protected ClinicalFormHl7Adapter clinicalFormHl7Adapter;

  /** The note types service. */
  @LazyAutowired
  private NoteTypesService noteTypesService;

  @LazyAutowired
  private PatientMedicinePrescriptionsService patientMedicinePrescriptionsService;

  @LazyAutowired
  private AllergenMasterService allergenMasterService;

  @LazyAutowired
  private InterfaceEventMappingService interfaceEventMappingService;
  
  /**
   * Instantiates a new form service. Sets the specific item type for form based on formType and
   * form key field
   *
   * @param formType the form type
   */
  public ClinicalFormService(FormComponentsService.FormType formType) {
    super(formType);
  }

  /**
   * Gets the vital expr data.
   *
   * @param requestBody the request body
   * @return the vital expr data
   */
  public Map<String, List<Map<String, Object>>> getVitalExprData(Map<String, Object> requestBody) {
    return vitalReadingService.getVitalExprData(requestBody);
  }

  /**
   * Gets the patient recent allergies.
   *
   * @param mrNo the mr no
   * @return the patient recent allergies
   */
  public Map<String, Object> getPatientRecentAllergies(String mrNo) {
    Map<String, Object> mapData = new HashMap<>();
    mapData.put("patient_recent_allergies",
        ConversionUtils.copyListDynaBeansToMap(allergiesService.getPatientRecentAllergies(mrNo)));
    return mapData;
  }

  /**
   * Returns latest year of onset for a diag code.
   *
   * @param mrNo the mr no
   * @param diagCode the diag code
   * @return the diag year of onset
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getDiagYearOfOnset(String mrNo, String diagCode) {
    BasicDynaBean bean = mrdDiagService.getOnsetYear(mrNo, diagCode);
    Map<String, Object> map = new HashMap<>();
    if (bean != null) {
      map = bean.getMap();
      return map;
    }
    map.put("year_of_onset", "");
    return map;
  }

  /**
   * Gets the favrourites diagnosis code.
   *
   * @param doctorId the doctor id
   * @param searchInput the search input
   * @param codeType the code type
   * @return the favrourites diagnosis code
   */
  public Map<String, Object> getFavrouritesDiagnosisCode(String doctorId, String searchInput,
      String codeType) {
    Map<String, Object> favDiagCodeMap = new HashMap<>();
    favDiagCodeMap.put("diagnosis_codes", ConversionUtils.listBeanToListMap(diagCodeFavService
        .getDiagCodeFavOfCodeTypeList(searchInput, doctorId, codeType)));
    return favDiagCodeMap;
  }

  /**
   * Gets the diagnosis codes.
   *
   * @param searchInput the search input
   * @param codeType the code type
   * @return the diagnosis codes
   */
  public Map<String, Object> getDiagnosisCodes(String searchInput, String codeType) {
    Map<String, Object> diagCodeMap = new HashMap<>();
    diagCodeMap.put("diagnosis_codes", ConversionUtils.listBeanToListMap(icdSupportedCodesService
        .getDiagCodeOfCodeTypeList(searchInput, codeType)));
    return diagCodeMap;
  }

  /**
   * Gets the previous diagnosis details.
   *
   * @param patientId the patient id
   * @return the previous diagnosis details
   */
  public Map<String, Object> getPreviousDiagnosisDetails(String patientId, Integer pageNo) {
    Map<String, Object> prevDiagDetailMap = new HashMap<>();
    pageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
    prevDiagDetailMap.put("diagnosis_details",
        ConversionUtils.listBeanToListMap(mrdDiagnosisService
            .getPrevDiagnosisDetails(patientId, pageNo)));
    return prevDiagDetailMap;
  }

  /**
   * Gets the pat insurance info.
   *
   * @param visitId the visit id
   * @return the pat insurance info
   */
  public Map<String, Object> getPatInsuranceInfo(String visitId) {
    Map<String, Object> insuranceInfo = new HashMap<>();
    BasicDynaBean insurancedetails = patientInsurancePlansService.getPatInsuranceInfo(visitId);
    insuranceInfo.put("insurancedetails", insurancedetails.getMap());
    return insuranceInfo;
  }
    
  /**
   * Get Patient Problem History.
   * 
   * @return parameters the map
   */
  public Map<String, Object> getPatientProblemHistory(Map<String, String[]> parameters) {
    PagedList pagedList = patientProblemListDetailsRepository.getPatientProblemHistory(parameters);
    Map<String, Object> returnMap = new HashMap<>();
    returnMap.put("history", pagedList.getDtoList());
    returnMap.put("record_count", pagedList.getTotalRecords());
    returnMap.put("num_pages", pagedList.getNumPages());
    return returnMap;
  }
  
  /**
   * This method is used to get id's of section data which are inserted, updated and deleted, for
   * the purpose of triggering the hl7 message.
   * 
   * @param responseSectionData the form saved response data
   * @param SectionId required section id
   * @return the map which contains insert,update and delete keys
   */
  @SuppressWarnings("unchecked")
  protected Map<String, Object> getIdsFromSectionMap(Map<String, Object> responseSectionData,
      String keyColumn) {
    Map<String, Object> sectionDataMap = responseSectionData;
    String[] mapKeys = {"insert", "update", "delete"};
    Map<String, Object> requiredIdsListMap = new HashMap<>();
    for (String key : mapKeys) {
      if (sectionDataMap.get(key) != null) {
        Map<String, Object> dataMap = (Map<String, Object>) sectionDataMap.get(key);
        List<Integer> idsList = null;
        if (dataMap != null && !dataMap.isEmpty()) {
          idsList = new ArrayList<>();
          Map<String, Object> idMap = null;
          for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            if (dataMap.get(entry.getKey()) != null) {
              idMap = (Map<String, Object>) dataMap.get(entry.getKey());
              idsList.add(Integer.parseInt(idMap.get(keyColumn).toString()));
            }
          }
          requiredIdsListMap.put(key, idsList);
        }
      }
    }
    return requiredIdsListMap;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getSectionMap(Map<String, Object> response, int sectionId) {
    Map<String, Object> sectionMap = null;
    if (response.get("sections") != null) {
      for (Map<String, Object> map : (List<Map<String, Object>>) response.get("sections")) {
        if (map.get("section_id") != null && map.get("section_id").equals(sectionId)) {
          sectionMap = map;
          break;
        }
      }
    } else if ((response.get("section") != null)
        && ((Map<String, Object>) response.get("section")).get("section_id") != null
        && ((Map<String, Object>) response.get("section")).get("section_id").equals(sectionId)) {
      sectionMap = (Map<String, Object>) response.get("section");
    }
    return sectionMap;
  }

  /**
   * Sends Diagnosis related Hl7 message only if diagnosis is added / edited.
   * 
   * @param hl7Event the Hl7Event
   * @param visitId visitId the visitId
   * @param response the form saved response data
   */
  protected void triggerDiagnosisEvent(String visitId, Map<String, Object> response) {
    Map<String, Object> sectionMap = getSectionMap(response, -6);
    /* if (sectionMap != null && !getIdsFromSectionMap(sectionMap,"id").isEmpty()) {
      interfaceEventMappingService.diagnosisEvent(visitId);
    }*/
    interfaceEventMappingService.diagnosisEvent(visitId);
  }

  /**
   * Sends allergies related Hl7 message only if diagnosis is added / edited.
   * 
   * @param hl7Event the Hl7Event
   * @param visitId visitId the visitId
   * @param response the form saved response data
   */
  protected void triggerAllergiesEvent(String visitId, Map<String, Object> response) {
    Map<String, Object> sectionMap = getSectionMap(response, -2);
    /* if (sectionMap != null && !getIdsFromSectionMap(sectionMap, "allergy_id").isEmpty()) {
      interfaceEventMappingService.allergiesEvent(visitId);
    }*/
    interfaceEventMappingService.allergiesEvent(visitId);
  }

  /**
   * Sends allergies related Hl7 message only if diagnosis is added / edited.
   * 
   * @param hl7Event the Hl7Event
   * @param visitId visitId the visitId
   * @param response the form saved response data
   */
  protected void triggerVitalEvent(String visitd, Map<String, Object> response) {
    Map<String, Object> sectionMap = getSectionMap(response, -4);
    if (sectionMap != null && !getIdsFromSectionMap(sectionMap, "vital_reading_id").isEmpty()) {
      interfaceEventMappingService.vitalReadingEvent(visitd);
    }
  }

  /**
   * Sends medicines related Hl7 message only if prescription is added / edited.
   * 
   * @param visitId visitId the visitId
   * @param response the form saved response data
   */
  @SuppressWarnings("unchecked")
  protected void triggerMedicinePrescEvent(String visitId, Map<String, Object> response,
      boolean isDischargeMedication) {
    Map<String, Object> sectionMap = getSectionMap(response, isDischargeMedication ? -22 : -7);
    if (sectionMap != null) {
      Map<String, Object> prescIdsMap = getIdsFromSectionMap(sectionMap, "item_prescribed_id");
      if (!prescIdsMap.isEmpty()) {
        interfaceEventMappingService.medicinePrescriptionEvent(visitId, getCenterId(), "insert",
            patientMedicinePrescriptionsService
                 .filterMedicinePrescIds((List<Integer>) prescIdsMap.get("insert"), false));
        interfaceEventMappingService.medicinePrescriptionEvent(visitId, getCenterId(), "update",
            patientMedicinePrescriptionsService
                  .filterMedicinePrescIds((List<Integer>) prescIdsMap.get("update"), false));
        interfaceEventMappingService.medicinePrescriptionEvent(visitId, getCenterId(), "delete",
            patientMedicinePrescriptionsService
                  .filterMedicinePrescIds((List<Integer>) prescIdsMap.get("delete"), true));
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void triggerPatientProblemEvent(String visitId, Map<String, Object> response) {
    Map<String, Object> sectionMap = getSectionMap(response, -21);
    if (sectionMap != null) {
      Map<String, Object> pplIdsMap = getIdsFromSectionMap(sectionMap, "ppl_id");
      if (!pplIdsMap.isEmpty()) {
        interfaceEventMappingService.chronicProblemsEvent(visitId, getCenterId(), "insert",
            (List<Integer>) pplIdsMap.get("insert"));
        interfaceEventMappingService.chronicProblemsEvent(visitId, getCenterId(), "update",
            (List<Integer>) pplIdsMap.get("update"));
        interfaceEventMappingService.chronicProblemsEvent(visitId, getCenterId(), "delete",
            (List<Integer>) pplIdsMap.get("delete"));
      }
    }
  }
  
  /**
   * Send HL7 Message.
   * 
   * @param visitId the visit id
   * @param response the response data
   */
  public void triggerEvents(String visitId, Map<String, Object> response) {}

  /**
   * Get Patient Problem List.
   * 
   * @param searchInput the search text
   * @return map
   */
  public Map<String, Object> getPatientProblemList(String searchInput, String codeType) {
    return icdCodesService.getPatientProblemList(searchInput, codeType);
  }

  /**
   * Get Allergies list.
   * 
   * @param parameters the parameters
   * @return map
   */
  public Map<String, Object> getAllergies(Map<String, String[]> parameters) {
    Map<String, Object> responseMap = new HashMap<String, Object>();
    String filterText = (null != parameters && parameters.containsKey("filterText")) ? parameters
        .get("filterText")[0] : null;
    List<BasicDynaBean> searchSet = (null != filterText) ? allergenMasterService.autocomplete(
        "allergen_description", filterText, true, parameters) : allergenMasterService.lookup(true);
    responseMap.put("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    responseMap.put("listSize", searchSet.size());
    return responseMap;
  }

  /**
   * Get Note Types template details.
   * 
   * @param parameters filter map
   * @return map
   */
  public Map<String, Object> getNoteTypesTemplateDetails(Map<String, String[]> parameters) {
    return noteTypesService.getTemplateDetails(parameters);
  }

  public void updateAndSendPrescriptionEmail(Object id, Map<String,Object> requestBody) {
  }
}
