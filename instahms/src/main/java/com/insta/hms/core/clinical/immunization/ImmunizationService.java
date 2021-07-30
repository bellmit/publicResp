package com.insta.hms.core.clinical.immunization;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ImmunizationService.
 */
@Service
public class ImmunizationService extends SectionService {

  /** The doctor consultation service. */
  @LazyAutowired
  private DoctorConsultationService doctorConsultationService;

  /** The Constant SECTION_UPDATE. */
  private static final String SECTION_UPDATE = "update";

  /**
   * Instantiates a new immunization service.
   */
  public ImmunizationService() {
    this.sectionId = -17;
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
    List<Map<String, Object>> records = (List<Map<String, Object>>) requestBody.get(SECTION_UPDATE);
    boolean success = false;
    if (records != null && !records.isEmpty()) {
      success = doctorConsultationService.saveImmunizationDetails(records.get(0),
          (Integer) parameter.getId());
    }
    if (success) {
      Map<String, Object> response = new HashMap<>();
      response.put(SECTION_UPDATE, new HashMap<String, Object>());
      ((Map<String, Object>) response.get(SECTION_UPDATE)).put("0", records.get(0));
      return response;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.SectionService#deleteSection(java.lang.Integer,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map) This method represent own
   * implementation of delete section data at transaction level
   */
  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromCurrentForm(com.insta
   * .hms.core.clinical.forms.FormParameter) The method returns list of record from current saved
   * form
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    List<Map<String, Object>> records = new ArrayList<>();
    records.add(doctorConsultationService.getImmunizationDetails((Integer) parameter.getId())
        .getMap());
    data.put("records", records);
    return data;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.core.clinical.forms.SectionService#getSectionDetailsFromLastSavedForm(com.insta
   * .hms.core.clinical.forms.FormParameter) The method returns list of record from last saved form
   */
  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    return getSectionDetailsFromCurrentForm(parameter);
  }

}
