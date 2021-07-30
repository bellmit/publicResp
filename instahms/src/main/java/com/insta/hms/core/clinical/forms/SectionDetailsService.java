package com.insta.hms.core.clinical.forms;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.outpatient.DoctorConsultationService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.sections.SectionsService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The Class SectionDetailsService.
 *
 * @author krishnat
 */
@Service
public class SectionDetailsService {

  /** The repo. */
  @LazyAutowired
  SectionDetailsRepository repo;

  /** The session service. */
  @LazyAutowired
  SessionService sessionService;

  /** The sec mas service. */
  @LazyAutowired
  SectionsService secMasService;

  /** The doctor cons service. */
  @LazyAutowired
  DoctorConsultationService doctorConsService;

  /** The sd validator. */
  @LazyAutowired
  SectionDetailsValidator sdValidator;

  /** The sec val repo. */
  @LazyAutowired
  SectionValuesRepository secValRepo;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return repo.getBean();
  }

  /**
   * Gets the all records.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @return the all records
   */
  public List<BasicDynaBean> getAllRecords(FormParameter parameter, Integer sectionId) {
    return repo.getAllRecords(parameter, sectionId);
  }

  /**
   * Checks if is image used.
   *
   * @param fieldDetailId the field detail id
   * @param imageId       the image id
   * @return true, if is image used
   */
  public boolean isImageUsed(int fieldDetailId, int imageId) {
    return secValRepo.isImageUsed(fieldDetailId, imageId);
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
   * Gets the section data status.
   *
   * @param parameter       the parameter
   * @param sectionDetailId the section detail id
   * @param sectionId       the section id
   * @return the section data status
   */
  public Boolean getSectionDataStatus(FormParameter parameter, Integer sectionDetailId,
      Integer sectionId) {
    return repo.getSectionDataStatus(parameter, sectionDetailId, sectionId);
  }

  /**
   * Gets the all section details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the all section details
   */
  public List<BasicDynaBean> getAllSectionDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {

    return repo.getAllSectionDetails(mrNo, patientId, itemId, genericFormId, formId, itemType);
  }

  /**
   * Gets the sections.
   *
   * @param parameter the parameter
   * @param roleId    the role id
   * @return the sections
   */
  public List<BasicDynaBean> getSections(FormParameter parameter, Integer roleId) {
    return repo.getSections(parameter, roleId);
  }

  /**
   * Gets the component details.
   *
   * @param formType  the form type
   * @param consultId the consult id
   * @param consBean  the cons bean
   * @return the component details
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean getComponentDetails(String formType, Integer consultId,
      BasicDynaBean consBean) {
    FormParameter parameter = new FormParameter(formType, "CONS", (String) consBean.get("mr_no"),
        (String) consBean.get("patient_id"), consultId, "section_item_id");
    List<Map<String, Object>> allSections = ConversionUtils.listBeanToListMap(
        repo.getSections(parameter, RequestContext.getRoleId()));
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("sections");
    builder.add("form_id", Integer.class);
    BasicDynaBean bean = builder.build();
    int formId = 0;
    List<Integer> tmp = new ArrayList<>();
    for (Map<String, Object> section : allSections) {
      if (!tmp.contains((Integer) section.get("section_id"))) {
        tmp.add((Integer) section.get("section_id"));
        formId = (Integer) section.get("form_id");
      }
    }
    String sectionsStr = Arrays.toString(tmp.toArray());
    bean.set("sections", sectionsStr.substring(1, sectionsStr.length() - 1).replace(" ", ""));
    bean.set("form_id", formId);
    return bean;

  }

  /**
   * Gets the triage component details.
   *
   * @param deptId    the dept id
   * @param formType  the form type
   * @param consultId the consult id
   * @return the triage component details
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean getTriageComponentDetails(String deptId, String formType,
      Integer consultId) {
    BasicDynaBean consBean = doctorConsService.findByKey(consultId);
    FormParameter parameter = new FormParameter(formType, "CONS", (String) consBean.get("mr_no"),
        (String) consBean.get("patient_id"), consultId, "section_item_id");
    List<Map<String, Object>> allSections = ConversionUtils.listBeanToListMap(
        repo.getSections(parameter, (Integer) sessionService.getSessionAttributes().get("roleId")));
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("sections");
    builder.add("form_id", Integer.class);
    BasicDynaBean bean = builder.build();
    int formId = 0;
    List<Integer> tmp = new ArrayList<>();
    for (Map<String, Object> section : allSections) {
      if (!tmp.contains((Integer) section.get("section_id"))) {
        tmp.add((Integer) section.get("section_id"));
        formId = (Integer) section.get("form_id");
      }
    }
    String sectionsStr = Arrays.toString(tmp.toArray());
    bean.set("sections", sectionsStr.substring(1, sectionsStr.length() - 1).replace(" ", ""));
    bean.set("form_id", formId);
    return bean;

  }

  /**
   * Gets the all master sections.
   *
   * @param roleId   the role id
   * @param formType the form type
   * @return the all master sections
   */
  public List<BasicDynaBean> getAllMasterSections(Integer roleId, String formType) {
    List<BasicDynaBean> sections = repo.getAllMasterSections(roleId, formType);
    if (formType.equals(FormComponentsService.FormType.Form_IP.toString())) {
      for (int i = 0; i < sections.size(); i++) {
        if (sections.get(i).get("section_id").equals(-7)) {
          sections.get(i).set("section_title", "Physician Order");
          break;
        }
      }
    }
    return sections;
  }

  /**
   * Gets the added section master details.
   *
   * @param mrNo          the mr no
   * @param patientId     the patient id
   * @param itemId        the item id
   * @param genericFormId the generic form id
   * @param formId        the form id
   * @param itemType      the item type
   * @return the added section master details
   */
  public List<BasicDynaBean> getAddedSectionMasterDetails(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) {
    return repo.getAddedSectionMasterDetails(mrNo, patientId, itemId, genericFormId, formId,
        itemType);

  }

  /**
   * Gets the carry forward sections by section ids.
   *
   * @param paramter   the paramter
   * @param sectionIds the section ids
   * @return the carry forward sections by section ids
   */
  public List<BasicDynaBean> getCarryForwardSectionsBySectionIds(FormParameter paramter,
      List<Integer> sectionIds) {
    return repo.getCarryForwardSectionsBySectionIds(paramter, sectionIds);
  }

  /**
   * Gets the ip emr component details.
   *
   * @param mrNo          the mr no
   * @param ipEmrFormType the ip emr form type
   * @param patientId     the patient id
   * @return the ip emr component details
   */
  public BasicDynaBean getIpEmrComponentDetails(String mrNo, String ipEmrFormType,
      String patientId) {
    FormParameter parameter = new FormParameter(ipEmrFormType, "", mrNo, patientId, patientId,
        "patient_id");
    List<Map<String, Object>> allSections = ConversionUtils.listBeanToListMap(
        repo.getSections(parameter, (Integer) sessionService.getSessionAttributes().get("roleId")));
    DynaBeanBuilder builder = new DynaBeanBuilder();
    builder.add("sections");
    builder.add("form_id", Integer.class);
    BasicDynaBean bean = builder.build();
    int formId = 0;
    List<Integer> tmp = new ArrayList<>();
    for (Map<String, Object> section : allSections) {
      if (!tmp.contains((Integer) section.get("section_id"))) {
        tmp.add((Integer) section.get("section_id"));
        formId = (Integer) section.get("form_id");
      }
    }
    String sectionsStr = Arrays.toString(tmp.toArray());
    bean.set("sections", sectionsStr.substring(1, sectionsStr.length() - 1).replace(" ", ""));
    bean.set("form_id", formId);
    return bean;
  }

  /**
   * Update unfinalize status for multiple sections.
   * @param sectionIds section identifiers
   * @param param      parameters
   * @return true or false inidcating status of update
   */
  public boolean updateSectionsUnFinalizeStatus(List<Integer> sectionIds, FormParameter param) {
    if (param != null) {
      return repo.updateSectionsUnFinalizeStatus(param, sectionIds);
    }
    return false;
  }

}
