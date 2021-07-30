package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.exception.ConversionException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class SectionFormService.
 *
 * @author krishnat
 */
@Service
public class SectionFormService {

  /** doctor consultation service bean. */
  @LazyAutowired
  DoctorConsultationService doctorConsultationService;

  /** The repo. */
  @LazyAutowired
  SectionFormRepository repo;

  /**
   * Gets the repository.
   *
   * @return the repository
   */
  public SectionFormRepository getRepository() {
    return repo;
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return repo.getBean();
  }

  /**
   * Save.
   *
   * @param requestBody the request body
   * @param parameter the parameter
   * @param sdBean the sd bean
   * @param errorMap the error map
   * @param isNew the is new
   * @return the basic dyna bean
   */
  public BasicDynaBean save(Map<String, Object> requestBody, FormParameter parameter,
      BasicDynaBean sdBean, Map<String, Object> errorMap, boolean isNew) {
    int sectionDetailId = (Integer) sdBean.get("section_detail_id");

    BasicDynaBean bean = getBean();
    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyJsonToDynaBean(requestBody, bean, errors, true);
    bean.set("form_type", parameter.getFormType());

    bean.set("section_detail_id", sectionDetailId);

    if (!errors.isEmpty()) {
      throw new ConversionException(errors);
    }

    if (isNew) {
      repo.insert(bean);
    } else {
      Map<String, Object> keys = new HashMap<String, Object>();
      keys.put("section_detail_id", sectionDetailId);
      repo.update(bean, keys);
    }

    return bean;
  }

  /**
   * Delete.
   *
   * @param sectionDetailId the section detail id
   * @return the boolean
   */
  public Boolean delete(Integer sectionDetailId) {
    return repo.delete("section_detail_id", sectionDetailId) != 0;
  }

  /**
   * Update section form type.
   *
   * @param updateBeans the update beans
   * @param updateKeysMap the update keys map
   */
  @SuppressWarnings("unchecked")
  public void updateSectionFormType(List<BasicDynaBean> updateBeans,
      Map<String, Object> updateKeysMap) {
    repo.batchUpdate(updateBeans, updateKeysMap);
  }

  /**
   * Used to update form type when consultation type is changed from Registration screen.
   *
   * @param patientId the patient id.
   * @param newFormType new form type.
   * @param existingFormType existing form type.
   */
  public void updateFormTypeOnConsultationTypeChange(String patientId, String newFormType,
      String existingFormType) {
    List<BasicDynaBean> visitConsultations =
        doctorConsultationService.listVisitConsultations(patientId);
    for (BasicDynaBean cons : visitConsultations) {
      repo.updateFormTypeOnConsultationTypeChange((int) cons.get("consultation_id"), newFormType,
          existingFormType);
    }
  }
}
