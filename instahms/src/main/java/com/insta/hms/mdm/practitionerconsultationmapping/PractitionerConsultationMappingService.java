package com.insta.hms.mdm.practitionerconsultationmapping;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.practitionertypes.PractitionerTypeService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PractitionerConsultationMappingService.
 */
@Service
public class PractitionerConsultationMappingService extends MasterService {

  /** The repository. */
  @LazyAutowired
  private PractitionerConsultationMappingRepository repository;
  
  @LazyAutowired
  private PractitionerTypeService practitionerTypeService;

  /** The consultation types service. */
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;

  /**
   * Instantiates a new practitioner consultation mapping service.
   *
   * @param repository the repository
   * @param validator the validator
   */
  public PractitionerConsultationMappingService(
      PractitionerConsultationMappingRepository repository,
      PractitionerConsultationMappingValidator validator) {
    super(repository, validator);
  }

  
  /**
   * Insert.
   *
   * @param requestBody the request body
   * @return int
   */
  public int insert(ModelMap requestBody) {

    String practitionerTypeName = (String) requestBody.get("practitioner_type_name");
    List<Integer> consultationTypeIds = (List<Integer>) requestBody.get("consultation_type_ids");
    String status = (String) requestBody.get("status");
    int practitionerTypeId = practitionerTypeService.insert(practitionerTypeName,status);
    List<BasicDynaBean> beans = new ArrayList<>();

    for (Integer consultationTypeId : consultationTypeIds) {
      BasicDynaBean bean = repository.getBean();
      bean.set("consultation_type_id", consultationTypeId);
      bean.set("practitioner_type_id", practitionerTypeId);
      beans.add(bean);
    }
    repository.batchInsert(beans);
    return practitionerTypeId;

  }


  /**
   * Gets the consultation types.
   *
   * @param practitionerTypeId the practitioner type id
   * @return the consultation types
   */
  public List<BasicDynaBean> getConsultationTypes(Integer practitionerTypeId, String apptCat) {
    
    return repository.getConsultationTypes(practitionerTypeId, apptCat);
  }

  /**
  * Gets doctor consultation types mapped to practitioner types.
  * @param practitionerTypeId the practitioner type id
  * @param apptCat appointmentCatgeory Doctor, Service
  * @return Map
  */
  public Map getDoctorConsultationTypes(Integer practitionerTypeId, String apptCat) {
    List<BasicDynaBean> consultationTypes = null;
    Map<String, Object> responseMap = new HashMap<>();
    //TODO : move this check to masters, while making practitioner
    //TODO type inactive check if that practitioner type is mapped to any doctor.
    if (practitionerTypeId != null &&  !practitionerTypeId.equals("")
        && practitionerTypeService.checkIfPractitionerTypeIsActive(practitionerTypeId)) {
      consultationTypes = repository.getConsultationTypes(practitionerTypeId, apptCat);
      responseMap.put("consultation_types", ConversionUtils.listBeanToListMap(consultationTypes));
    }
    if (consultationTypes == null || consultationTypes.isEmpty()) {
      List<String> consultationTypesColumns = new ArrayList<>();
      consultationTypesColumns.add("consultation_type");
      consultationTypesColumns.add("consultation_type_id");
      consultationTypesColumns.add("duration");
      if (apptCat != null && apptCat != "") {
        Map filterMap = new HashMap();
        filterMap.put("patient_type", "o");
        filterMap.put("status", "A");
        responseMap.put("consultation_types", ConversionUtils.listBeanToListMap(
            consultationTypesService.getAllOpConsultationTypes(
                consultationTypesColumns, filterMap)));
      } else {
        responseMap.put("consultation_types", ConversionUtils.listBeanToListMap(
            consultationTypesService.getAllConsultationTypes(consultationTypesColumns)));
      }
    }
    return responseMap;
  }

  /**
   * Update.
   *
   * @param requestBody the request body
   */
  @Transactional(rollbackFor = Exception.class)
  public void update(ModelMap requestBody) {
    
    int practitionerTypeId = (int) requestBody.get("practitioner_type_id");
    List<Integer> consultationTypeIds = (List<Integer>) requestBody.get("consultation_type_ids");
    String status = (String) requestBody.get("status");
    List<BasicDynaBean> beans = new ArrayList<>();

    for (Integer consultationTypeId : consultationTypeIds) {
      BasicDynaBean bean = repository.getBean();
      bean.set("consultation_type_id", consultationTypeId);
      bean.set("practitioner_type_id", practitionerTypeId);
      beans.add(bean);
    }

    ModelMap practitionerTypeMap = new ModelMap();
    practitionerTypeMap.put("practitioner_id", practitionerTypeId);
    practitionerTypeMap.put("status", status);
    practitionerTypeService.update(practitionerTypeService.toBean(practitionerTypeMap));

    repository.delete("practitioner_type_id", practitionerTypeId);
    repository.batchInsert(beans);
    
  }

}
