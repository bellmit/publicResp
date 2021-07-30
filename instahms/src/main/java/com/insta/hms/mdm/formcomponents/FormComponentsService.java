package com.insta.hms.mdm.formcomponents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.cities.CityService;
import com.insta.hms.mdm.commonprinttemplates.CommonPrintTemplateService;
import com.insta.hms.mdm.departments.DepartmentService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.documenttypes.DocumentTypeService;
import com.insta.hms.mdm.operations.OperationsService;
import com.insta.hms.mdm.sections.SectionsService;
import com.insta.hms.mdm.services.ServicesService;
import com.insta.hms.mdm.systemgeneratedsections.SystemGeneratedSectionsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FormComponentsService.
 *
 * @author anup vishwas
 */

@Service
public class FormComponentsService {

  /** The dept service. */
  @LazyAutowired
  private DepartmentService deptService;

  /** The operations service. */
  @LazyAutowired
  private OperationsService operationsService;

  /** The services service. */
  @LazyAutowired
  private ServicesService servicesService;

  /** The sys section service. */
  @LazyAutowired
  private SystemGeneratedSectionsService sysSectionService;

  /** The section service. */
  @LazyAutowired
  private SectionsService sectionService;

  /** The common print template service. */
  @LazyAutowired
  private CommonPrintTemplateService commonPrintTemplateService;

  /** The document type service. */
  @LazyAutowired
  private DocumentTypeService documentTypeService;

  /** The doctor service. */
  @LazyAutowired
  private DoctorService doctorService;

  /** The form components repository. */
  @LazyAutowired
  private FormComponentsRepository formComponentsRepository;

  /** The generic preferences service. */
  @LazyAutowired
  private GenericPreferencesService genericPreferencesService;

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The city service. */
  @LazyAutowired
  private CityService cityService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The Constant STATUS. */
  private static final String STATUS = "status";

  /** The Constant SECTION_TITLE. */
  private static final String SECTION_TITLE = "section_title";

  /** The Constant CENTER_ID. */
  private static final String CENTER_ID = "center_id";

  /** The Constant DEPT_ID. */
  private static final String DEPT_ID = "dept_id";

  /** The Constant FORM_COMPONENT_CENTER_ID. */
  private static final String FORM_COMPONENT_CENTER_ID = "form_components_center_id";

  /** The Constant FORM_COMPONENT_ID. */
  private static final String FORM_COMPONENT_ID = "form_components_id";

  public enum FormType {
    Form_CONS, Form_TRI, Form_IA, Form_OP_FOLLOW_UP_CONS, Form_Serv, Form_OT, Form_Gen, Form_IP
  }

  public enum OPFormColumns {
    form_type, dept_id, doctor_id, center_id, role_id
  }

  public enum FollowupFormColumns {
    form_type, dept_id, doctor_id, center_id, role_id
  }

  public enum TriageFormColumns {
    dept_id, doctor_id, center_id, role_id
  }

  public enum IAFormColumns {
    dept_id, doctor_id, center_id, role_id
  }

  public enum IPFormColumns {
    dept_id, center_id, role_id
  }

  public enum ServFormColumns {
    dept_id, service_id, center_id, role_id
  }

  public enum GENFormColumns {
    dept_id, center_id, role_id, id
  }

  public enum OTFormColumns {
    dept_id, operation_id, center_id, role_id
  }

  /**
   * Gets the OP form.
   *
   * @param columns the {@link OPFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getOPForm(Map<OPFormColumns, Object> columns) {
    String deptId = (String) columns.get(OPFormColumns.dept_id);
    String doctorId = (String) columns.get(OPFormColumns.doctor_id);
    Integer centerId = (Integer) columns.get(OPFormColumns.center_id);
    Integer roleId = (Integer) columns.get(OPFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getOPForm(deptId, doctorId, roleId,
        centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    forms = formComponentsRepository.getOPForm(deptId, "-1", roleId, centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    return formComponentsRepository.getOPForm("-1", "-1", roleId, centerId);
  }

  /**
   * Gets the triage form.
   *
   * @param columns the {@link TriageFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getTriageForm(Map<TriageFormColumns, Object> columns) {
    String deptId = (String) columns.get(TriageFormColumns.dept_id);
    String doctorId = (String) columns.get(TriageFormColumns.doctor_id);
    Integer centerId = (Integer) columns.get(TriageFormColumns.center_id);
    Integer roleId = (Integer) columns.get(TriageFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getTriageForm(deptId, doctorId, roleId,
        centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    forms = formComponentsRepository.getTriageForm(deptId, "-1", roleId, centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    return formComponentsRepository.getTriageForm("-1", "-1", roleId, centerId);
  }


  /**
   * Gets the IA form.
   *
   * @param columns the {@link IAFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getIAForm(Map<IAFormColumns, Object> columns) {
    String deptId = (String) columns.get(IAFormColumns.dept_id);
    Integer centerId = (Integer) columns.get(IAFormColumns.center_id);
    Integer roleId = (Integer) columns.get(IAFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getIAForm(deptId, roleId,
        centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    forms = formComponentsRepository.getIAForm(deptId, roleId, centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    return formComponentsRepository.getIAForm("-1", roleId, centerId);
  }

  /**
   * Gets the IP form.
   *
   * @param columns the {@link IPFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getIPForm(Map<IPFormColumns, Object> columns) {
    String deptId = (String) columns.get(IPFormColumns.dept_id);
    Integer centerId = (Integer) columns.get(IPFormColumns.center_id);
    Integer roleId = (Integer) columns.get(IPFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getIPForm(deptId, roleId, centerId);
    if (forms == null || forms.isEmpty()) {
      forms = formComponentsRepository.getIPForm("-1", roleId, centerId); 
    }
    for (int i = 0; i < forms.size(); i++) {
      if (forms.get(i).get("section_id").equals(-7)) {
        forms.get(i).set("section_title", "Physician Order");
        break;
      }
    }
    return forms;
  }

  /**
   * Gets the service form.
   *
   * @param columns the {@link ServFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getServiceForm(Map<ServFormColumns, Object> columns) {
    String deptId = (String) columns.get(ServFormColumns.dept_id);
    String serviceId = (String) columns.get(ServFormColumns.service_id);
    Integer centerId = (Integer) columns.get(ServFormColumns.center_id);
    Integer roleId = (Integer) columns.get(ServFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getServiceForm(deptId, serviceId, roleId,
        centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    forms = formComponentsRepository.getServiceForm(deptId, "-1", roleId, centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    return formComponentsRepository.getServiceForm("-1", "-1", roleId, centerId);
  }

  /**
   * Gets the generic form.
   *
   * @param columns the {@link GENFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getGenericForm(Map<GENFormColumns, Object> columns) {
    Integer formId = (Integer) columns.get(GENFormColumns.id);
    Integer centerId = (Integer) columns.get(GENFormColumns.center_id);
    Integer roleId = (Integer) columns.get(GENFormColumns.role_id);
    return formComponentsRepository.getGenericForm(formId, roleId, centerId);
  }

  /**
   * Gets the OT form.
   *
   * @param columns the {@link OTFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getOTForm(Map<OTFormColumns, Object> columns) {
    String deptId = (String) columns.get(OTFormColumns.dept_id);
    String operationId = (String) columns.get(OTFormColumns.operation_id);
    Integer centerId = (Integer) columns.get(OTFormColumns.center_id);
    Integer roleId = (Integer) columns.get(OTFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getOTForm(deptId, operationId, roleId,
        centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    forms = formComponentsRepository.getOTForm(deptId, operationId, roleId, centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    return formComponentsRepository.getOTForm("-1", "-1", roleId, centerId);
  }

  /**
   * Gets the follow up form.
   *
   * @param columns the {@link FollowupFormColumns}
   * @return sections of form
   */
  public List<BasicDynaBean> getFollowUpForm(Map<FollowupFormColumns, Object> columns) {
    String deptId = (String) columns.get(FollowupFormColumns.dept_id);
    String doctorId = (String) columns.get(FollowupFormColumns.doctor_id);
    Integer centerId = (Integer) columns.get(FollowupFormColumns.center_id);
    Integer roleId = (Integer) columns.get(FollowupFormColumns.role_id);
    List<BasicDynaBean> forms = formComponentsRepository.getFollowUpForm(deptId, doctorId, roleId,
        centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    forms = formComponentsRepository.getFollowUpForm(deptId, "-1", roleId, centerId);
    if (forms != null && !forms.isEmpty()) {
      return forms;
    }
    return formComponentsRepository.getFollowUpForm("-1", "-1", roleId, centerId);
  }

  /**
   * Form component details.
   *
   * @param deptId
   *          the dept id
   * @param formType
   *          the form type
   * @param operationId
   *          the operation id
   * @return the list
   */
  public List<BasicDynaBean> formComponentDetails(String deptId, String formType,
      String operationId) {
    return formComponentsRepository.formComponentDetails(deptId, formType, operationId);

  }

  /**
   * Gets the components.
   *
   * @return the components
   */
  public List<BasicDynaBean> getComponents() {
    Integer maxCenter = (Integer) getAllPrefs().get("max_centers_inc_default");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    List<BasicDynaBean> componentsList = null;
    if ((maxCenter > 1) && (centerId != 0)) {
      componentsList = formComponentsRepository.getComponentsCenterWise(centerId);
    } else {
      componentsList = formComponentsRepository.getComponents();
    }
    return componentsList;
  }

  /**
   * Gets the form map.
   *
   * @param params
   *          the params
   * @return the form map
   */
  public Map getFormMap(Map params) {
    Map<String, Object> map = new HashMap<String, Object>();
    return formComponentsRepository
        .getFormBean((Integer.parseInt(((String[]) params.get("id"))[0]))).getMap();
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return formComponentsRepository.getBean();
  }

  /**
   * Gets the reference data.
   *
   * @param params
   *          the params
   * @return the reference data
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map<String, List<Map>> getReferenceData(Map params) {

    Map<String, List<Map>> addshowRefData = new HashMap<String, List<Map>>();
    addshowRefData.put("depts",
        ConversionUtils.listBeanToListMap(deptService.listAll(null, STATUS, "A", "dept_name")));
    addshowRefData.put("ops", ConversionUtils
        .listBeanToListMap(operationsService.listAll(null, STATUS, "A", "operation_name")));
    addshowRefData.put("serv", ConversionUtils
        .listBeanToListMap(servicesService.listAll(null, STATUS, "A", "service_name")));
    if (((String[]) params.get("id")) != null) {
      addshowRefData.put("selectedSections",
          ConversionUtils.listBeanToListMap(formComponentsRepository
              .getSections(Integer.parseInt(((String[]) params.get("id"))[0]))));
    }
    String[] formType = (String[]) params.get("form_type");
    addshowRefData.put("selectedSectionsJSON",
        ConversionUtils.copyListDynaBeansToLinkedMap(sectionService.listAll()));
    addshowRefData.put("availableSections", getAvailableSections(formType[0]));
    addshowRefData.put("templateList", ConversionUtils
        .copyListDynaBeansToLinkedMap(commonPrintTemplateService.getGenericFormTemplateList()));
    addshowRefData.put("doctypelist",
        ConversionUtils.listBeanToListMap(documentTypeService.listAll("doc_type_name")));
    addshowRefData.put("doctors",
        ConversionUtils.listBeanToListMap(doctorService.listAll(null, STATUS, "A", "doctor_name")));
    return addshowRefData;

  }

  /**
   * Method is invoked to get section list for specific form type.
   *
   * @param formType
   *          the form type
   * @return section list
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Map> getAvailableSections(String formType) {

    List<Map> sectionsList = ConversionUtils
        .copyListDynaBeansToLinkedMap(sectionService.getSectionsList(formType));
    List<BasicDynaBean> sysSectionsList = sysSectionService.listAll();
    int index = 0;
    for (BasicDynaBean section : sysSectionsList) {
      String active = "N";
      if (formType.equals("Form_CONS")) {
        active = (String) section.get("op");
      } else if (formType.equals("Form_IP")) {
        active = (String) section.get("ip");
      } else if (formType.equals("Form_OT")) {
        active = (String) section.get("surgery");
      } else if (formType.equals("Form_Serv")) {
        active = (String) section.get("service");
      } else if (formType.equals("Form_TRI")) {
        active = (String) section.get("triage");
      } else if (formType.equals("Form_IA")) {
        active = (String) section.get("initial_assessment");
      } else if (formType.equals("Form_Gen")) {
        active = (String) section.get("generic_form");
      } else if (formType.equals("Form_OP_FOLLOW_UP_CONS")) {
        active = (String) section.get("op_follow_up_consult_form");
      }
      if (active != null && active.equals("Y")) {
        Map<String, Object> tmpSection = new HashMap<>();
        tmpSection.put(SECTION_TITLE, section.get("section_name"));
        tmpSection.put("section_id", section.get("section_id"));
        sectionsList.add(index++, tmpSection);
      }
    }

    Comparator<Map> comp = new Comparator<Map>() {
      @Override
      public int compare(Map o1, Map o2) {
        if (o1 == null && o2 == null) {
          return 0;
        }

        if (o1 != null && o2 != null && o1.get(SECTION_TITLE) == null
            && o2.get(SECTION_TITLE) == null) {
          return 0;
        }
        if (o1 == null || o1.get(SECTION_TITLE) == null) {
          return -1;
        }

        if (o2 == null || o2.get(SECTION_TITLE) == null) {
          return 1;
        }

        return ((String) o1.get(SECTION_TITLE)).compareTo((String) o2.get(SECTION_TITLE));
      }
    };

    Collections.sort(sectionsList, comp);
    return sectionsList;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.MasterService#toBean(java.util.Map, java.util.Map)
   *
   * Adding the sections data in required format
   */

  /**
   * To bean.
   *
   * @param requestParams
   *          the request params
   * @param bean
   *          the bean
   * @return the basic dyna bean
   */
  public BasicDynaBean toBean(Map<String, String[]> requestParams, BasicDynaBean bean) {
    String sections = "";
    String[] selectedSections = requestParams.get("selected_sections");
    if (selectedSections != null) {
      boolean first = true;
      for (String sectionId : selectedSections) {
        if (!first) {
          sections += ",";
        }
        sections += sectionId;
        first = false;
      }
    }
    bean.set("sections", sections);
    if (bean.get("group_patient_sections") == null) {
      bean.set("group_patient_sections", "N");
    }
    if (bean.get("immunization") == null) {
      bean.set("immunization", "N");
    }
    return bean;
  }

  /**
   * Insert form.
   *
   * @param reqParams
   *          the req params
   * @return the basic dyna bean
   */
  @SuppressWarnings({ "rawtypes" })
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean insertForm(Map<String, String[]> reqParams) {
    BasicDynaBean formBean = getBean();
    BasicDynaBean formDeptBean = formComponentsRepository.getDeptBean();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(reqParams, formBean, errors);
    ConversionUtils.copyToDynaBean(reqParams, formDeptBean, errors);
    formBean = toBean(reqParams, formBean);
    formBean.set("id", formComponentsRepository.getNextSequence());
    return insert(formBean, formDeptBean);
  }

  /**
   * Update form.
   *
   * @param reqParams
   *          the req params
   * @return the basic dyna bean
   */
  @SuppressWarnings({ "rawtypes" })
  @Transactional(rollbackFor = Exception.class)
  public BasicDynaBean updateForm(Map<String, String[]> reqParams) {
    BasicDynaBean formBean = getBean();
    BasicDynaBean formDeptBean = formComponentsRepository.getDeptBean();
    List errors = new ArrayList();
    ConversionUtils.copyToDynaBean(reqParams, formBean, errors);
    ConversionUtils.copyToDynaBean(reqParams, formDeptBean, errors);
    formBean = toBean(reqParams, formBean);
    return update(formBean, formDeptBean);
  }

  /**
   * Insert. Method save the form as Template
   *
   * @param data
   *          the data
   * @return the basic dyna bean
   */
  @SuppressWarnings("unchecked")
  public BasicDynaBean insert(Map<String, Object> data) {
    List<Integer> sections = (List<Integer>) data.get("sections");
    BasicDynaBean formBean = getBean();
    ConversionUtils.copyToDynaBean(data, formBean);
    formBean.set("id", formComponentsRepository.getNextSequence());
    String sectionsStr = Arrays.toString(sections.toArray());
    formBean.set("sections", sectionsStr.substring(1, sectionsStr.length() - 1).replace(" ", ""));
    formBean.set("istemplate", true);
    BasicDynaBean formDeptBean = formComponentsRepository.getDeptBean();
    formDeptBean.set(DEPT_ID, data.get(DEPT_ID));
    return this.insert(formBean, formDeptBean);
  }

  /**
   * Insert.
   *
   * @param formBean
   *          the form bean
   * @param formDeptBean
   *          the form dept bean
   * @return the basic dyna bean
   */
  public BasicDynaBean insert(BasicDynaBean formBean, BasicDynaBean formDeptBean) {
    boolean success = false;
    BasicDynaBean formCenterBean = formComponentsRepository.getFormCenterBean();
    formCenterBean.set(FORM_COMPONENT_CENTER_ID,
        formComponentsRepository.getFormComponentCenterId());
    formCenterBean.set(FORM_COMPONENT_ID, formBean.get("id"));
    formCenterBean.set(STATUS, "A");
    formCenterBean.set(CENTER_ID, 0);
    boolean newFormRule = formBean.get("istemplate") != null ? (Boolean) formBean.get("istemplate")
        : false;
    if (formComponentsRepository.isDuplicateForm(formBean, (String) formDeptBean.get(DEPT_ID),
        newFormRule)) {
      formComponentsRepository.throwDuplicateEntityException(formBean,
          (String) formDeptBean.get(DEPT_ID), newFormRule);
    } else {
      if (formComponentsRepository.insert(formBean) == 1) {
        formDeptBean.set("id", formBean.get("id"));
        success = formComponentsRepository.insertFormDept(formDeptBean) == 1
            && formComponentsRepository.insertFormCenter(formCenterBean) == 1;
      }
    }
    if (!success) {
      throw new DuplicateEntityException("flash.create.failed.form",
          new String[] { (String) formBean.get("form_name") });
    }
    return formBean;

  }

  /**
   * Update.
   *
   * @param formBean
   *          the form bean
   * @param formDeptBean
   *          the form dept bean
   * @return the basic dyna bean
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public BasicDynaBean update(BasicDynaBean formBean, BasicDynaBean formDeptBean) {
    boolean success = false;
    Map<String, Object> keys = new HashMap();
    boolean newFormRule = formBean.get("istemplate") != null ? (Boolean) formBean.get("istemplate")
        : false;
    if (formBean.get("id") != null && !formBean.get("id").equals("")) {
      keys.put("id", formBean.get("id"));
      if (formComponentsRepository.isDuplicateForm(formBean, (String) formDeptBean.get(DEPT_ID),
          newFormRule)) {
        formComponentsRepository.throwDuplicateEntityException(formBean,
            (String) formDeptBean.get(DEPT_ID), newFormRule);
      } else {
        if (formComponentsRepository.update(formBean, keys) == 1) {
          Map<String, Object> keyMap = new HashMap();
          keyMap.put("form_department_id", formDeptBean.get("form_department_id"));
          success = formComponentsRepository.updateFormDept(formDeptBean, keyMap) == 1;
        }
      }
    }
    if (!success) {
      throw new DuplicateEntityException("flash.update.failed.form",
          new String[] { (String) formBean.get("form_name") });
    }

    return formBean;
  }

  /**
   * Gets the forms for consultation.
   *
   * @param doctorId
   *          the doctor id
   * @param deptId
   *          the dept id
   * @param centerId
   *          the center id
   * @param formType
   *          the form type
   * @return the forms for consultation
   */
  public List<BasicDynaBean> getFromTemplatesForConsultation(String doctorId, String deptId,
      Integer centerId, String formType) {
    return formComponentsRepository.getFormTemplatesForConsultation(doctorId, deptId, centerId,
        formType);
  }

  public List<BasicDynaBean> getConsTemplateForms(String doctorId, String deptId,
      Integer centerId) {
    return formComponentsRepository.getConsTemplateForms(doctorId, deptId, centerId);
  }

  public List<BasicDynaBean> getTriageTemplateForms(String deptId, Integer centerId) {
    return formComponentsRepository.getTriageTemplateForms(deptId, centerId);
  }

  public List<BasicDynaBean> getAssessmentTemplateForms(String deptId, Integer centerId) {
    return formComponentsRepository.getAssessmentTemplateForms(deptId, centerId);
  }

  public List<BasicDynaBean> getFollowUpTemplateForms(String doctorId, String deptId,
      Integer centerId) {
    return formComponentsRepository.getFollowUpTemplateForms(doctorId, deptId, centerId);
  }

  public List<BasicDynaBean> getSurgeryTemplateForms(String deptId, String operationId,
      Integer centerId) {
    return formComponentsRepository.getSurgeryTemplateForms(deptId, operationId, centerId);
  }

  public List<BasicDynaBean> getGenericTemplateForms(String deptId, Integer centerId) {
    return formComponentsRepository.getGenericTemplateForms(deptId, centerId);
  }

  public List<BasicDynaBean> getInPatientTemplateForms(String deptId, Integer centerId) {
    return formComponentsRepository.getInPatientTemplateForms(deptId, centerId);
  }

  /**
   * Gets the forms for consultation.
   *
   * @param doctorId
   *          the doctor id
   * @param deptId
   *          the dept id
   * @param centerId
   *          the center id
   * @param formType
   *          the form type
   * @return the forms for consultation
   */
  public List<BasicDynaBean> getFormsForConsultation(String doctorId, String deptId,
      Integer centerId, String formType) {
    return formComponentsRepository.getFormsForConsultation(doctorId, deptId, centerId, formType);
  }

  /**
   * Gets the sectionsby id.
   *
   * @param formId
   *          the form id
   * @param roleId
   *          the role id
   * @return the sectionsby id
   */
  public List<BasicDynaBean> getSectionsbyId(Integer formId, Integer roleId) {
    return formComponentsRepository.getSectionsbyId(formId, roleId);
  }

  /**
   * Gets the form details.
   *
   * @param formId
   *          the form id
   * @return the form details
   */
  public BasicDynaBean getFormDetails(int formId) {
    return formComponentsRepository.findByKey("id", formId);
  }

  /**
   * Show list.
   *
   * @param formId
   *          the form id
   * @return the map
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Map showList(int formId) {
    Map<String, Object> showRefData = new HashMap<String, Object>();
    showRefData.put("applicable_centers",
        ConversionUtils.listBeanToListMap(getAllApplicableCenters(formId)));
    showRefData.put("bean", getFormAndDeptDetails(formId).getMap());
    showRefData.put("cities_json", ConversionUtils.listBeanToListMap(getCities()));
    showRefData.put("centers_json", ConversionUtils.listBeanToListMap(getAllCenters()));
    showRefData.put("prefs", getAllPrefs().get("max_centers_inc_default"));
    return showRefData;
  }

  /**
   * Gets the all applicable centers.
   *
   * @param formId
   *          the form id
   * @return the all applicable centers
   */
  public List<BasicDynaBean> getAllApplicableCenters(int formId) {
    return formComponentsRepository.getAllApplicableCenters(formId);
  }

  /**
   * Gets the all centers.
   *
   * @return the all centers
   */
  public List<BasicDynaBean> getAllCenters() {
    return centerService.getAllCentres();
  }

  /**
   * Gets the form and dept details.
   *
   * @param formId
   *          the form id
   * @return the form and dept details
   */
  public BasicDynaBean getFormAndDeptDetails(int formId) {
    return formComponentsRepository.getFormBean(formId);
  }

  /**
   * Gets the cities.
   *
   * @return the cities
   */
  public List<BasicDynaBean> getCities() {
    return cityService.listAllCenters();
  }

  /**
   * Gets the all prefs.
   *
   * @return the all prefs
   */
  public BasicDynaBean getAllPrefs() {
    return genericPreferencesService.getAllPreferences();
  }

  /**
   * Update form center.
   *
   * @param request
   *          the request
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updateFormCenter(Map<String, String[]> request) {
    List<BasicDynaBean> insertBeans = new ArrayList<>();
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    List<Object> deleteKeys = new ArrayList<>();
    Map<String, Object> updateKeysMap = new HashMap<>();
    List<Object> updateKeys = new ArrayList<>();
    int formId = Integer.parseInt(request.get("form_component_id")[0]);
    String appForCenters = request.get("applicable_for_centers")[0];

    if (appForCenters != null) {
      if (appForCenters.equalsIgnoreCase("all")) {
        if (!deleteAllCenters(formId)) {
          throw new DuplicateEntityException("flash.delete.few.centers.failed");
        }
        if (!addCenter(formId, 0, "A")) {
          throw new DuplicateEntityException("flash.update.all.centers.failed");
        }
      } else {
        if (!deleteDefaultCenter(formId)) {
          throw new DuplicateEntityException("flash.delete.default.center.failed");
        }
        String[] centerIds = request.get(CENTER_ID);
        String[] formComponentsCenterId = request.get(FORM_COMPONENT_CENTER_ID);
        String[] formComponentsCenterDelete = request.get("cntr_delete");
        String[] formComponentsCenterEdited = request.get("cntr_edited");
        String[] formComponentsCenterAdded = request.get("cntr_added");
        String[] centerStatuses = request.get("center_status");

        for (int i = 0; i < centerIds.length - 1; i++) {
          if (new Boolean(formComponentsCenterAdded[i])) {
            BasicDynaBean bean = formComponentsRepository.getFormCenterBean();
            bean.set(FORM_COMPONENT_CENTER_ID, formComponentsRepository.getFormComponentCenterId());
            bean.set(FORM_COMPONENT_ID, formId);
            bean.set(CENTER_ID, Integer.parseInt(centerIds[i]));
            bean.set(STATUS, centerStatuses[i]);
            insertBeans.add(bean);
          } else if (new Boolean(formComponentsCenterDelete[i])) {
            deleteKeys.add(Integer.parseInt(formComponentsCenterId[i]));
          } else if (new Boolean(formComponentsCenterEdited[i])) {
            BasicDynaBean bean = formComponentsRepository.getFormCenterBean();
            bean.set(STATUS, centerStatuses[i]);
            bean.set(FORM_COMPONENT_CENTER_ID, Integer.parseInt(formComponentsCenterId[i]));
            updateBeans.add(bean);
            updateKeys.add(bean.get(FORM_COMPONENT_CENTER_ID));
          }
        }

        updateKeysMap.put(FORM_COMPONENT_CENTER_ID, updateKeys);
      }
    }

    if (!insertBeans.isEmpty()
        && (formComponentsRepository.formCenterBatchInsert(insertBeans).length <= 0)) {
      throw new DuplicateEntityException("flash.insert.form.applicability.failed");

    }
    if (!updateBeans.isEmpty() && (formComponentsRepository.formCenterBatchUpdate(updateBeans,
        updateKeysMap).length <= 0)) {
      throw new DuplicateEntityException("flash.update.form.applicability.failed");
    }
    if (!deleteKeys.isEmpty() && (formComponentsRepository
        .formCenterBatchDelete(FORM_COMPONENT_CENTER_ID, deleteKeys).length <= 0)) {
      throw new DuplicateEntityException("flash.delete.form.applicability.failed");
    }
    return true;

  }

  /**
   * Delete default center.
   *
   * @param formId
   *          the form id
   * @return the boolean
   */
  public Boolean deleteDefaultCenter(int formId) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(FORM_COMPONENT_ID, formId);
    filterMap.put(CENTER_ID, 0);
    if (formComponentsRepository.findCenterByKey(filterMap) == null) {
      return true;
    } else {
      return formComponentsRepository.formCenterDelete(filterMap) == 1;
    }
  }

  /**
   * Delete all centers.
   *
   * @param formId
   *          the form id
   * @return the boolean
   */
  public Boolean deleteAllCenters(int formId) {
    if (!formComponentsRepository.getAllNonDefaultCenters(formId).isEmpty()) {
      Map<String, Object> filterMap = new HashMap<>();
      filterMap.put(FORM_COMPONENT_ID, formId);
      return formComponentsRepository.deleteAllNonDefaultCenters(formId) > 0;
    }
    return true;
  }

  /**
   * Adds the center.
   *
   * @param formComponentId
   *          the form component id
   * @param centerId
   *          the center id
   * @param status
   *          the status
   * @return true, if successful
   */
  public boolean addCenter(Integer formComponentId, Integer centerId, String status) {
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(FORM_COMPONENT_ID, formComponentId);
    filterMap.put(CENTER_ID, centerId);
    BasicDynaBean getFormCenter = formComponentsRepository.findCenterByKey(filterMap);
    if (getFormCenter == null) {
      BasicDynaBean bean = formComponentsRepository.getFormCenterBean();
      bean.set(FORM_COMPONENT_CENTER_ID, formComponentsRepository.getFormComponentCenterId());
      bean.set(FORM_COMPONENT_ID, formComponentId);
      bean.set(CENTER_ID, centerId);
      bean.set(STATUS, status);
      return formComponentsRepository.formCenterInsert(bean) == 1;
    }
    return true;
  }

}