package com.insta.hms.forms;


import com.bob.hms.common.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.allergies.AllergiesService;
import com.insta.hms.core.clinical.forms.*;
import com.insta.hms.core.clinical.patientproblems.PatientProblemListDetailsRepository;
import com.insta.hms.core.clinical.prescriptions.PatientMedicinePrescriptionsService;
import com.insta.hms.core.medicalrecords.MRDDiagnosisService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.diagnosiscodefavourites.DiagnosisCodeFavouritesService;
import com.insta.hms.mdm.formcomponents.FormComponentsService;
import com.insta.hms.mdm.icdcodes.IcdCodesService;
import com.insta.hms.mdm.icdsupportedcodes.IcdSupportedCodesService;
import com.insta.hms.mdm.imagemarkers.ImageMarkersService;
import com.insta.hms.mdm.sections.SectionsService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class FormService.
 *
 * @author krishnat
 */
public abstract class FormService {

  /** The form key field the primary identifier in transaction. */
  protected String formKeyField;

  /** The form type. */
  protected String formType;

  /** The item type. */
  protected String itemType;

  /** encoded form data. */
  protected String encodedBase64String;

  /**
   * The Possible itemTypes for the forms.
   */
  protected enum ItemType {

    /** The cons. */
    CONS,
    /** The ser. */
    SER,
    /** The sur. */
    SUR,
    /** Generic Form ItemType. */
    GEN
  }

  /** The Constant SECTION_ID_KEY. */
  protected static final String SECTION_ID_KEY = "section_id";

  /** The Constant SECTION_DETAIL_ID_KEY. */
  protected static final String SECTION_DETAIL_ID_KEY = "section_detail_id";

  /** The Constant IS_DELETE_KEY. */
  protected static final String IS_DELETE_KEY = "isDelete";

  /** The Constant FINALIZED_KEY. */
  protected static final String FINALIZED_KEY = "finalized";

  /** The Constant SECTION_ITEM_ID. */
  protected static final String SECTION_ITEM_ID = "section_item_id";

  /** The Constant PATIENT_ID. */
  protected static final String PATIENT_ID = "patient_id";

  /** The Constant GENERIC_FORM_ID. */
  protected static final String GENERIC_FORM_ID = "generic_form_id";

  /** The Constant SAVED_USER_NAME. */
  protected static final String SAVED_USER_NAME = "user_name";

  /** The Constant REVISION_NUMBER. */
  protected static final String REVISION_NUMBER = "revision_number";
  
  /** The Constant VISIT_ID. */
  protected static final String VISIT_ID = "visit_id";
  
  /** The section details repo. */
  @LazyAutowired
  protected SectionDetailsRepository sectionDetailsRepo;

  /** The section form repo. */
  @LazyAutowired
  protected SectionFormRepository sectionFormRepo;

  /** The form components service. */
  @LazyAutowired
  protected FormComponentsService formComponentsService;

  /** The user service. */
  @LazyAutowired
  protected UserService userService;

  /** The session service. */
  @LazyAutowired
  protected SessionService sessionService;

  /** The section factory. */
  @LazyAutowired
  protected SectionFactory sectionFactory;

  /** The form template data service. */
  @LazyAutowired
  protected FormTemplateDataService formTemplateDataService;

  /** The form validator. */
  @LazyAutowired
  protected FormValidator formValidator;

  /** The temp service. */
  @LazyAutowired
  protected TempService tempService;

  /** The sec mas service. */
  @LazyAutowired
  protected SectionsService secMasService;

  /** The form data service. */
  @LazyAutowired
  protected FormTemplateDataService formDataService;

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
  protected ModulesActivatedService modulesActivatedService;
  
  @LazyAutowired
  protected IcdCodesService icdCodesService;
  
  @LazyAutowired
  private PatientMedicinePrescriptionsService patientMedicinePrescriptionsService;

  /**
   * Instantiates a new form service. Sets the specific item type for form based on formType and
   * form key field
   *
   * @param formType the form type
   */
  public FormService(FormComponentsService.FormType formType) {
    if (formType.equals(FormComponentsService.FormType.Form_CONS)
        || formType.equals(FormComponentsService.FormType.Form_OP_FOLLOW_UP_CONS)
        || formType.equals(FormComponentsService.FormType.Form_TRI)
        || formType.equals(FormComponentsService.FormType.Form_IA)) {
      this.formKeyField = SECTION_ITEM_ID;
      this.itemType = ItemType.CONS.toString();
    } else if (formType.equals(FormComponentsService.FormType.Form_Serv)) {
      this.itemType = ItemType.SER.toString();
    } else if (formType.equals(FormComponentsService.FormType.Form_OT)) {
      this.itemType = ItemType.SUR.toString();
    } else if (formType.equals(FormComponentsService.FormType.Form_Gen)) {
      this.itemType = ItemType.GEN.toString();
      this.formKeyField = GENERIC_FORM_ID;
    } else if (formType.equals(FormComponentsService.FormType.Form_IP)) {
      this.itemType = "";
      this.formKeyField = PATIENT_ID;
    }
    this.formType = formType.toString();
  }

  /**
   * Return the record specifically related to form.
   *
   * @param formKeyValue the form key value
   * @return the record
   */
  public abstract BasicDynaBean getRecord(Object formKeyValue);

  /**
   * Returns the summary of the form.
   *
   * @param parameter the parameter
   * @return the summary
   */
  public abstract Map<String, Object> getSummary(FormParameter parameter);

  /**
   * Returns the specific(extra) details required in the form.
   *
   * @param parameter the parameter
   * @param formSpecificBean the form specific bean
   * @return the extra details
   */
  public Map<String, Object> getActivitySpecificDetails(FormParameter parameter,
      BasicDynaBean formSpecificBean) {
    return Collections.emptyMap();
  }

  /**
   * Returns the sections include in form from master.
   *
   * @param parameters the parameters
   * @param formSpecificBean the form specific bean
   * @return the sections from master
   */
  public abstract List<BasicDynaBean> getSectionsFromMaster(FormParameter parameters,
      BasicDynaBean formSpecificBean);

  /**
   * Saves required form details before to sections save.
   *
   * @param requestBody the request body
   * @param parameter the parameter
   * @param errorMap the error map
   * @return the map
   */
  public abstract Map<String, Object> preFormSave(Map<String, Object> requestBody,
      FormParameter parameter, Map<String, Object> errorMap);

  /**
   * Saves form details after to sections save.
   *
   * @param requestBody the request body
   * @param parameters the parameters
   * @param response the response
   * @param errorMap the error map
   */
  public abstract void postFormSave(Map<String, Object> requestBody, FormParameter parameters,
      Map<String, Object> response, Map<String, Object> errorMap);

  /**
   * Returns the templates.
   *
   * @param parameter the parameter
   * @param formSpecificBean the form specific bean
   * @return the template forms
   */
  public abstract List<BasicDynaBean> getTemplateForms(FormParameter parameter,
      BasicDynaBean formSpecificBean);

  /**
   * Returns FormParameter details of the form.
   *
   * @param id the id
   * @param formSpecificBean the form specific bean
   * @return the form parameter
   */
  public abstract FormParameter getFormParameter(Object id, BasicDynaBean formSpecificBean);

  /**
   * Returns the saved section in the form.
   *
   * @param parameters the parameters
   * @return the sections
   */
  public List<BasicDynaBean> getSections(FormParameter parameters) {
    List<BasicDynaBean> forms = sectionDetailsRepo.getSections(parameters, getRoleId());
    if (this.formType.equals(FormComponentsService.FormType.Form_IP.toString())) {
      for (int i = 0; i < forms.size(); i++) {
        if (forms.get(i).get("section_id").equals(-7)) {
          forms.get(i).set("section_title", "Physician Order");
          break;
        }
      }
    }
    return forms;
  }

  /**
   * Returns the TOC(list of sections) of the form.
   *
   * @param parameters the parameters
   * @param formSpecificBean the form specific bean
   * @return the TOC
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getTOC(FormParameter parameters,
      BasicDynaBean formSpecificBean) {
    List<BasicDynaBean> trxSections = getSections(parameters);
    if (trxSections == null || trxSections.isEmpty()) {
      List<Map<String, Object>> masterSections =
          ConversionUtils.listBeanToListMap(getSectionsFromMaster(parameters, formSpecificBean));
      // this modification is done for reflecting the carry forward section in TOC.
      List<Map<String, Object>> sections = new ArrayList<>();
      List<Integer> sectionIds = new ArrayList<>();
      for (Map<String, Object> section : masterSections) {
        sectionIds.add((Integer) section.get("section_id"));
      }
      List<BasicDynaBean> carryFrowardSections =
          sectionDetailsRepo.getCarryForwardSectionsBySectionIds(parameters, sectionIds);
      Integer displayOrder = 1;
      Integer masterSectionsL = masterSections.size();
      for (int i = 0; i < masterSectionsL; i++) {
        Map<String, Object> temp = new HashMap<>();
        temp.putAll(masterSections.get(i));
        temp.put("display_order", displayOrder++);
        sections.add(temp);
        for (BasicDynaBean section : carryFrowardSections) {
          BasicDynaBean secBean = secMasService.getRecord((int) section.get("section_id"));
          if (section.get("section_id").equals(masterSections.get(i).get("section_id"))
              && (Long) section.get("count") > 1 && (Boolean) secBean.get("allow_duplicate")) {
            for (int j = 1; j < (Long) section.get("count"); j++) {
              temp = new HashMap<>();
              temp.putAll(masterSections.get(i));
              temp.put("display_order", displayOrder++);
              sections.add(temp);
            }
          }
        }
      }
      return sections;
    }
    return ConversionUtils.listBeanToListMap(trxSections);
  }

  /**
   * Returns the basic details required to load the form.
   *
   * @param formKeyValue the form key value
   * @return the map
   */
  public Map<String, Object> loadForm(Object formKeyValue) {
    BasicDynaBean formSpecificBean = getRecord(formKeyValue);
    return loadForm(getFormParameter(formKeyValue, formSpecificBean), formSpecificBean);
  }

  /**
   * Load form.
   *
   * @param parameters the parameters
   * @param formSpecificBean the form specific bean
   * @return the map
   */
  private Map<String, Object> loadForm(FormParameter parameters, BasicDynaBean formSpecificBean) {
    Map<String, Object> response = new HashMap<>();
    response.put("sections", getTOC(parameters, formSpecificBean));
    response.put("summary", getSummary(parameters));
    response.put("forms",
        ConversionUtils.listBeanToListMap(getTemplateForms(parameters, formSpecificBean)));
    response.putAll(getActivitySpecificDetails(parameters, formSpecificBean));

    // Delete all the Draft(autosaved) data for current (user, patient)
    tempService.deleteSections(parameters);
    return response;
  }

  /**
   * Reopen form.
   *
   * @param formKeyValue the form key value
   * @param requestBody the request body
   * @return the map
   */
  public abstract Map<String, Object> reopenForm(Object formKeyValue, ModelMap requestBody);

  /**
   * Return the record if section is saved.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @return record
   */
  public BasicDynaBean isSectionSaved(FormParameter parameter, int sectionId) {
    return sectionDetailsRepo.getRecord(parameter, sectionId);
  }

  /**
   * Returns the section details.
   *
   * @param formKeyValue the form key value
   * @param sectionId the section id
   * @return section details
   */
  public Map<String, Object> getSectionDetails(Object formKeyValue, int sectionId) {
    return getSectionDetails(formKeyValue, sectionId, 0, false);
  }

  /**
   * Returns the section details.
   *
   * @param formKeyValue the form key value
   * @param sectionId the section id
   * @param formId the form id
   * @param changeForm the change form
   * @return section details
   */
  public Map<String, Object> getSectionDetails(Object formKeyValue, int sectionId, int formId,
      Boolean changeForm) {
    FormParameter parameter = getFormParameter(formKeyValue, null);
    return getSectionDetails(parameter, sectionId, formId, changeForm);
  }

  /**
   * Gets the section details.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @param formId the form id
   * @param changeForm the change form
   * @return the section details
   */
  private Map<String, Object> getSectionDetails(FormParameter parameter, int sectionId, int formId,
      Boolean changeForm) {
    BasicDynaBean bean = isSectionSaved(parameter, sectionId);
    Boolean isSectionSaved = bean != null;
    Map<String, Object> data = new HashMap<>();
    data.put(SECTION_ID_KEY, sectionId);
    if (sectionId < 0) {
      data.put(SECTION_DETAIL_ID_KEY, bean == null ? 0 : bean.get(SECTION_DETAIL_ID_KEY));
      data.put(FINALIZED_KEY, bean == null ? "N" : bean.get(FINALIZED_KEY));
      data.put(REVISION_NUMBER, bean == null ? null : bean.get(REVISION_NUMBER));
      data.put(SAVED_USER_NAME, bean == null ? null : bean.get(SAVED_USER_NAME));
      data.put("finalized_user", bean == null ? null : bean.get("finalized_user"));
      data.put("mod_time", bean == null ? null : bean.get("mod_time"));
    }
    SectionService section = sectionFactory.getSectionService(sectionId);
    if (isSectionSaved) {
      data.putAll(section.getSectionDetailsFromCurrentForm(parameter));
    } else {
      data.putAll(section.getSectionDetailsFromLastSavedForm(parameter));
    }
    // if template changed, template data will be populated only when saved, prepopulate
    // data does exist for the section.
    if (changeForm) {
      ValidationErrorMap errMap = new ValidationErrorMap();
      if (formComponentsService.getSectionsbyId(formId, getRoleId()) == null) {
        errMap.addError("formId", "exception.notvalid.form.id");
        throw new ValidationException(errMap);
      }
      Map<String, Object> templateData = formTemplateDataService.getdata(formId, sectionId);
      section.processTemplateData(parameter, templateData, data, formId);
    }
    return data;
  }

  /**
   * Save section.
   *
   * @param formKeyValue the form key value
   * @param sectionId the section id
   * @param requestBody the request body
   * @return the string
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(rollbackFor = Exception.class)
  public String saveSection(Object formKeyValue, int sectionId, Map<String, Object> requestBody)
      throws JsonProcessingException {
    formValidator.validateSectionId(sectionId);
    if (requestBody.get(SECTION_ID_KEY) == null) {
      requestBody.put(SECTION_ID_KEY, sectionId);
    }
    return tempService.saveSection(requestBody, getFormParameter(formKeyValue, null));
  }
  
  /**
   * Save section.
   *
   * @param section the section
   * @param parameter the parameter
   * @param sectionId the section id
   * @param stnErrorMap the stn error map
   * @return the map
   */
  protected Map<String, Object> saveSection(Map<String, Object> section, FormParameter parameter,
      Integer sectionId, Map<String, Object> stnErrorMap) {
    SectionService secService = sectionFactory.getSectionService(sectionId);

    if ((Boolean) section.get(IS_DELETE_KEY)) {
      Integer sectionDetailId = (Integer) section.get(SECTION_DETAIL_ID_KEY);
      boolean deleted = deleteSectionMain(sectionDetailId, stnErrorMap);
      deleted = deleted && secService.deleteSection(sectionDetailId, parameter, stnErrorMap);
      if (deleted) {
        // TODO
      }
      return null;
    } else {
      Map<String, Object> responseData = null;
      BasicDynaBean sdbean = saveSectionMain(section, parameter, stnErrorMap);
      responseData = secService.saveSection(section, sdbean, parameter, stnErrorMap);

      if (responseData == null) {
        responseData = new HashMap<>();
      }
      responseData.put(SECTION_ID_KEY, section.get(SECTION_ID_KEY));
      responseData.put(SECTION_DETAIL_ID_KEY, sdbean.get(SECTION_DETAIL_ID_KEY));
      responseData.put("display_order", section.get("display_order"));
      responseData.put("form_id", section.get("form_id"));
      responseData.put(REVISION_NUMBER, sdbean.get(REVISION_NUMBER));
      responseData.put(FINALIZED_KEY, sdbean.get(FINALIZED_KEY));
      responseData.put(SAVED_USER_NAME, sdbean.get(SAVED_USER_NAME));
      responseData.put("mod_time", sdbean.get("mod_time"));
      return responseData;
    }

  }

  /**
   * Only finalise Action for forms.
   * @param formKeyValue form key val.
   * @param params req params.
   * @return finalise payload.
   */
  public Map<String, Object> finalizeForm(Object formKeyValue, Map<String, Object> params) {
    return Collections.emptyMap();
  }

  /**
   * Save.
   *
   * @param formKeyValue the form key value
   * @param params the params
   * @return the map
   * @throws ParseException the parse exception
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> save(Object formKeyValue, Map<String, Object> params)
      throws ParseException {
    FormParameter parameter = getFormParameter(formKeyValue, null);
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> validationErr = new HashMap<>();
    Map<String, Object> nestedException = new HashMap<>();

    // Pre form save
    Map<String, Object> preFormErrMap = new HashMap<>();
    
    // To maintain same date time for transaction
    Timestamp transactionStartDateTime = new Timestamp((new Date()).getTime());
    params.put("transaction_start_date", transactionStartDateTime);

    response.putAll(preFormSave(params, parameter, preFormErrMap));
    if (!preFormErrMap.isEmpty()) {
      nestedException.putAll(preFormErrMap);
      throw new NestableValidationException(nestedException);
    }

    // save all the sections.
    response.put("sections", saveAllSections(params, parameter, nestedException));

    if (!nestedException.isEmpty()) {
      validationErr.put("sections", nestedException);
      throw new NestableValidationException(validationErr);
    }

    // Post form save
    Map<String, Object> postFormErrMap = new HashMap<>();
    postFormSave(params, parameter, response, postFormErrMap);
    if (!postFormErrMap.isEmpty()) {
      nestedException.putAll(postFormErrMap);
      throw new NestableValidationException(nestedException);
    }

    tempService.deleteSections(parameter);
    return response;
  }

  /**
   * Save all sections.
   *
   * @param allSections the all sections
   * @param formparams the formparams
   * @param nestedException the nested exception
   * @return the list
   */
  @SuppressWarnings("unchecked")
  protected List<Map<String, Object>> saveAllSections(Map<String, Object> allSections,
      FormParameter formparams, Map<String, Object> nestedException) {

    // Saving the form
    List tempData = tempService.getSections(formparams);
    Map<String, Map<String, Map<String, Object>>> autoSavedSections =
        ConversionUtils.listMapToMapMap(tempData, "auto_save_section_id");
    Map<Integer, Map<String, Map<String, Object>>> autoSavedSectionsBySection =
        ConversionUtils.listMapToMapMap(tempData, "section_id");

    List<Map<String, Object>> sections = (List<Map<String, Object>>) allSections.get("sections");
    Map<String, Object> stnErrorMap;
    List<Map<String, Object>> sectionsResponseList = new ArrayList<>();
    
    for (Map<String, Object> sec : sections) {
      Integer sectionId = (Integer) sec.get(SECTION_ID_KEY);
      sec.put("transaction_start_date", allSections.get("transaction_start_date"));
      stnErrorMap = new HashMap<>();
      String autoSaveSectionId = (String) sec.get("auto_save_section_id");
      Map<String, Object> sectionResponseMap = new HashMap<>();

      if (autoSavedSections.get(autoSaveSectionId) != null) {
        Map<String, Object> tempSavedata = autoSavedSections.get(autoSaveSectionId).get("data");
        tempSavedata.putAll(sec);
        sectionResponseMap = saveSection(tempSavedata, formparams, sectionId, stnErrorMap);
      } else if (autoSavedSectionsBySection.get(sectionId) != null && sectionId < 0 
          && !sectionHasUpdateData(sec)) {
        Map<String, Object> tempSavedata = autoSavedSectionsBySection.get(sectionId).get("data");
        tempSavedata.putAll(sec);
        sectionResponseMap = saveSection(tempSavedata, formparams, sectionId, stnErrorMap);
      } else {
        sectionResponseMap = saveSection(sec, formparams, sectionId, stnErrorMap);
      }
      if (sectionResponseMap != null) {
        sectionsResponseList.add(sectionResponseMap);
      }
      if (!stnErrorMap.isEmpty()) {
        nestedException.put(((Integer) sections.indexOf(sec)).toString(), stnErrorMap);
      }
    }
    return sectionsResponseList;
  }

  private boolean sectionHasUpdateData(Map<String, Object> sec) {
    return (sec.containsKey("records") && ((List)sec.get("records")).size() > 0) 
        || (sec.containsKey("fields") && ((List)sec.get("fields")).size() > 0)
        || (sec.containsKey("insert") && ((List)sec.get("insert")).size() > 0)
        || (sec.containsKey("update") && ((List)sec.get("update")).size() > 0)
        || (sec.containsKey("delete") && ((List)sec.get("delete")).size() > 0);
  }

  /**
   * Save section main.
   *
   * @param requestBody the request body
   * @param parameter the parameter
   * @param errorMap the error map
   * @return the basic dyna bean
   */
  // saves the form details of section.
  @SuppressWarnings("rawtypes")
  protected BasicDynaBean saveSectionMain(Map<String, Object> requestBody, FormParameter parameter,
      Map errorMap) {

    Map<String, Object> filterMap = new HashMap<>();
    ValidationErrorMap errMap = new ValidationErrorMap();
    Integer sectionDetailId = (Integer) requestBody.get(SECTION_DETAIL_ID_KEY);
    sectionDetailId = sectionDetailId == null ? 0 : sectionDetailId;
    String finalized = (String) requestBody.get(FINALIZED_KEY);
    finalized = finalized == null ? "N" : finalized;

    //Finalize section undo validation only for IP Flow.
    if (sectionDetailId != 0) {
      filterMap.put(SECTION_DETAIL_ID_KEY, sectionDetailId);
      BasicDynaBean secBean = sectionDetailsRepo.findByKey(filterMap);
      Boolean sectionAlreadyFinalized = ((String) secBean.get(FINALIZED_KEY)).equals("Y");
      if (!finalized.equals("Y") && sectionAlreadyFinalized && this.formType.equals("Form_IP")) {
        boolean allEdit =
            formValidator
                .allowSectionUndo(getRoleId(), (String) secBean.get(FINALIZED_KEY), errMap);
        if (!errMap.getErrorMap().isEmpty()) {
          ValidationException ex = new ValidationException(errMap);
          errorMap.putAll(ex.getErrors());
        }
        if (!allEdit) {
          return secBean;
        }
      }
    }
    int sectionId = (Integer) requestBody.get(SECTION_ID_KEY);
    BasicDynaBean sdbean = getSdBean();
    sdbean.set(SECTION_ID_KEY, sectionId);
    sdbean.set(REVISION_NUMBER, new BigDecimal(DateUtil.getCurrentTimestamp().getTime()));
    sdbean.set("mod_time", DateUtil.getCurrentTimestamp());
    sdbean.set("user_name", getUserId());
    if (finalized.equals("Y")) {
      sdbean.set(FINALIZED_KEY, "Y");
      sdbean.set("finalized_user", getUserId());
    } else {
      sdbean.set(FINALIZED_KEY, "N");
    }
    if (sectionDetailId == 0) {
      sectionDetailId = sectionDetailsRepo.getNextSequence();
      sdbean.set(SECTION_DETAIL_ID_KEY, sectionDetailId);
      sdbean.set("section_item_id", formKeyField == "section_item_id" ? parameter.getId() : 0);
      sdbean.set("item_type", parameter.getItemType());
      sdbean.set("mr_no", parameter.getMrNo());
      sdbean.set("patient_id", parameter.getPatientId());
      sdbean.set("generic_form_id", formKeyField == "generic_form_id" ? parameter.getId() : 0);

      boolean allowDuplicate = false;
      String linkedTo = "patient";
      String sectionStatus = "A";
      BasicDynaBean savedSection = isSectionSaved(parameter, sectionId);
      if (sectionId > 0) {
        BasicDynaBean secBean = secMasService.getRecord(sectionId);
        allowDuplicate = (Boolean) secBean.get("allow_duplicate");
        linkedTo = (String) secBean.get("linked_to");
      }
      if (savedSection != null) {
        if (allowDuplicate) {
          sectionStatus = (String) savedSection.get("section_status");
        } else {
          // section duplicate not allowed.
          // please send correct section detail id.
          errMap.addError(SECTION_ID_KEY, "exception.form.section.duplicate");

          ValidationException ex = new ValidationException(errMap);
          errorMap.putAll(ex.getErrors());
          return savedSection;
        }
      }
      sdbean.set("section_status", sectionStatus);
      inActivateSection(parameter, sectionId, linkedTo);
      sectionDetailsRepo.insert(sdbean);

      // save into patient section forms
      BasicDynaBean sfbean = getSfBean();
      sfbean.set(SECTION_DETAIL_ID_KEY, sectionDetailId);
      sfbean.set("form_id", requestBody.get("form_id"));
      sfbean.set("form_type", this.formType);
      sfbean.set("display_order", requestBody.get("display_order"));
      sectionFormRepo.insert(sfbean);
    } else {
      Map<String, Object> keys = new HashMap<>();
      keys.put(SECTION_DETAIL_ID_KEY, sectionDetailId);
      sdbean.set(SECTION_DETAIL_ID_KEY, sectionDetailId);
      BasicDynaBean sfbean = getSfBean();
      sfbean.set("display_order", requestBody.get("display_order"));
      //Form Id need to update in-case of Template load
      sfbean.set("form_id", requestBody.get("form_id"));
      sectionFormRepo.update(sfbean, keys);
      // Handle Multi user case
      keys.put(REVISION_NUMBER, requestBody.get(REVISION_NUMBER));
      if (sectionDetailsRepo.update(sdbean, keys) == 0) {
        errMap.addError(SECTION_ID_KEY, "exception.section.saved");
        ValidationException ex = new ValidationException(errMap);
        errorMap.putAll(ex.getErrors());
        return sdbean;

      }
    }

    return sdbean;
  }

  /**
   * In activate section.
   *
   * @param parameter the parameter
   * @param sectionId the section id
   * @param linkedTo the linked to
   * @return true, if successful
   */
  private boolean inActivateSection(FormParameter parameter, Integer sectionId, String linkedTo) {
    Map<String, Object> updateKeys = new HashMap<>();
    if (linkedTo != null && !linkedTo.equals("form")) {
      if (linkedTo.equals("patient")) {
        updateKeys.put("mr_no", parameter.getMrNo());
      } else if (linkedTo.equals("visit")) {
        updateKeys.put("patient_id", parameter.getPatientId());
      } else if (linkedTo.equals("order item")) {
        updateKeys.put(formKeyField, parameter.getId());
      }
      updateKeys.put(SECTION_ID_KEY, sectionId);
      updateKeys.put("section_status", "A");

      BasicDynaBean statusBean = getSdBean();
      statusBean.set("section_status", "I");
      if (sectionDetailsRepo.findByKey(updateKeys) != null) {
        sectionDetailsRepo.update(statusBean, updateKeys);
      }
    }
    return true;
  }

  /**
   * Delete section main.
   *
   * @param sectionDetailId the section detail id
   * @param errorMap the error map
   * @return the boolean
   */
  public Boolean deleteSectionMain(Integer sectionDetailId, Map<String, Object> errorMap) {
    boolean deleted = sectionDetailsRepo.delete(SECTION_DETAIL_ID_KEY, sectionDetailId) != null;
    deleted = deleted && sectionFormRepo.delete(SECTION_DETAIL_ID_KEY, sectionDetailId) != null;
    return deleted;
  }

  /**
   * Change form.
   *
   * @param formFieldValue the form field value
   * @param formId the form id
   * @return the map
   */
  public Map<String, Object> changeForm(Object formFieldValue, Integer formId) {
    return changeForm(getFormParameter(formFieldValue, null), formId);
  }

  /**
   * Change form.
   *
   * @param parameter the parameter
   * @param formId the form id
   * @return the map
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> changeForm(FormParameter parameter, Integer formId) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    List<BasicDynaBean> masterSections = formComponentsService.getSectionsbyId(formId, getRoleId());
    if (masterSections.isEmpty()) {
      errMap.addError("formId", "exception.notvalid.form.id");
      throw new ValidationException(errMap);
    }

    List<BasicDynaBean> txSections = getSectionsFromTransactionWithSavedStatus(parameter);
    Map<String, Object> txSectionMap =
        ConversionUtils.listBeanToMapListBean(txSections, SECTION_ID_KEY);
    List<Map<String, Object>> sections = new ArrayList<>();
    for (BasicDynaBean section : masterSections) {
      Integer displayOrder = sections.size();
      String finalized = null;
      Map<String, Object> sectionMap = new HashMap<>();
      if (txSectionMap.get(section.get(SECTION_ID_KEY)) != null) {
        List<BasicDynaBean> txSectionsList =
            (List<BasicDynaBean>) txSectionMap.get(section.get(SECTION_ID_KEY));
        for (BasicDynaBean txnSectionBean : txSectionsList) {
          finalized = (String) txnSectionBean.get(FINALIZED_KEY);
          sectionMap.put(SECTION_DETAIL_ID_KEY, txnSectionBean.get(SECTION_DETAIL_ID_KEY));
          sectionMap.putAll(section.getMap());
          sectionMap.put(IS_DELETE_KEY, false);
          sectionMap.put("force_save", false);
          sectionMap.put("form_id", formId);
          sectionMap.put("display_order", ++displayOrder);
          if (finalized != null) {
            sectionMap.put("FINALIZED_KEY", finalized);
          }
          sections.add(sectionMap);

        }
        txSectionMap.remove(section.get(SECTION_ID_KEY));
      } else {
        sectionMap.putAll(section.getMap());
        sectionMap.put(IS_DELETE_KEY, false);
        sectionMap.put("force_save", false);
        sectionMap.put("form_id", formId);
        sectionMap.put("display_order", ++displayOrder);
        if (finalized != null) {
          sectionMap.put("FINALIZED_KEY", finalized);
        }
        sections.add(sectionMap);
      }

    }
    Integer displayOrder = sections.size();
    for (BasicDynaBean section : txSections) {
      if (txSectionMap.get(section.get(SECTION_ID_KEY)) != null) {
        Map<String, Object> sectionMap = new HashMap<>();
        if ((Boolean) section.get("saved")) {
          sectionMap.putAll(section.getMap());
          sectionMap.remove("saved");
          sectionMap.put(IS_DELETE_KEY, false);
          sectionMap.put("force_save", false);
          sectionMap.put("form_id", formId);
          sectionMap.put("display_order", ++displayOrder);
        } else {
          sectionMap.putAll(section.getMap());
          sectionMap.remove("saved");
          sectionMap.put(IS_DELETE_KEY, true);
          sectionMap.put("force_save", false);
          sectionMap.put("display_order", 0);
          sectionMap.put("form_id", formId);
        }
        sections.add(sectionMap);
      }
    }
    Map<String, Object> response = new HashMap<>();
    if (this.formType.equals(FormComponentsService.FormType.Form_IP.toString())) {
      List<Map<String, Object>> sectionsModified = overrideSectionTitle(sections);
      response.put("sections", sectionsModified);
    } else {
      response.put("sections", sections);
    }

    return response;
  }

  /**
   * Override section title.
   *
   * @param sections the sections
   * @return the list
   */
  private List<Map<String, Object>> overrideSectionTitle(List<Map<String, Object>> sections) {
    for (int i = 0; i < sections.size(); i++) {
      if (sections.get(i).get("section_id").equals(-7)) {
        sections.get(i).put("section_title", "Physician Order");
        break;
      }
    }
    return sections;
  }

  /**
   * Gets the sections from transaction with saved status.
   *
   * @param parameter the parameter
   * @return the sections from transaction with saved status
   */
  public List<BasicDynaBean> getSectionsFromTransactionWithSavedStatus(FormParameter parameter) {
    return sectionDetailsRepo.getSectionsWithSavedStatus(parameter, getRoleId());
  }

  /**
   * Save custom form.
   *
   * @param reqBody the req body
   * @return the integer
   * @throws JsonProcessingException the json processing exception
   */
  @SuppressWarnings("unchecked")
  @Transactional(rollbackFor = Exception.class)
  public Integer saveCustomForm(Map<String, Object> reqBody) throws JsonProcessingException {
    formValidator.validateCustomForm(reqBody);

    Map<String, Object> formdata = new HashMap<>();
    formdata.putAll(reqBody);
    formdata.put("form_type", formType);
    if (formType.equals("Form_IP")) {
      formdata.put("operation_id", "-1");
    } else {
      formdata.put("doctor_id", reqBody.get("doctor_id"));
    }
    List<Integer> sectionIds = (List<Integer>) reqBody.get("section_ids");
    formdata.put("sections", sectionIds);
    BasicDynaBean formBean = formComponentsService.insert(formdata);

    boolean success = true;
    Integer formId = (Integer) formBean.get("id");
    List<Integer> vitalParamIds = (List<Integer>) reqBody.get("vital_param_ids");
    if (sectionIds.contains(-4) && vitalParamIds != null && !vitalParamIds.isEmpty()) {
      success &= formDataService.saveTemplateVitals(vitalParamIds, formId);
    }
    List<Map<String, Object>> sections = (List<Map<String, Object>>) reqBody.get("form_data");
    if (success && sections != null) {
      for (Map<String, Object> section : sections) {
        success &= formDataService.insert(section, formId);
      }
    }
    if (!success) {
      throw new HMSException("exception.unable.to.save.custom.form");
    }
    return formId;
  }

  /**
   * Return metadata based on the id.
   *
   * @param id the id
   * @return the dependent meta details
   */
  public abstract Map<String, Object> metadata(Object id);

  /**
   * Returns metadata specific to form and independent of the user(/patient).
   *
   * @return the map
   */
  public abstract Map<String, Object> metadata();

  /**
   * Gets the sd bean.
   *
   * @return the sd bean
   */
  private BasicDynaBean getSdBean() {
    return sectionDetailsRepo.getBean();
  }

  /**
   * Gets the sf bean.
   *
   * @return the sf bean
   */
  private BasicDynaBean getSfBean() {
    return sectionFormRepo.getBean();
  }

  /**
   * Gets the center id.
   *
   * @return the center id
   */
  public Integer getCenterId() {
    return (Integer) sessionService.getSessionAttributes().get("centerId");
  }

  /**
   * Gets the role id.
   *
   * @return the role id
   */
  public Integer getRoleId() {
    return (Integer) sessionService.getSessionAttributes().get("roleId");
  }

  /**
   * Gets the user id.
   *
   * @return the user id
   */
  public String getUserId() {
    return (String) sessionService.getSessionAttributes().get("userId");
  }
  
  /**
   * Gets the image marker by key.
   *
   * @param map the map
   * @return the image marker by key
   */
  public BasicDynaBean getImageMarkerByKey(Map<String, Object> map) {
    return imageMarkersService.findByPk(map);
  }

  
  /**
   * Send HL7 Message.
   * 
   * @param visitId the visit id
   * @param response the response data
   */
  public void triggerEvents(String visitId, Map<String, Object> response) {}
  
  /**
   * Gets the content type.
   *
   * @param parameterMap the parameter map
   * @param bean the bean
   * @return the content type
   */
  public String getContentType(Map<String, Object> parameterMap, BasicDynaBean bean) {
    if (parameterMap.containsKey("content_type")) {
      return (String) parameterMap.get("content_type");
    } else if (null != bean.get("content_type")) {
      return (String) bean.get("content_type");
    } else {
      return MediaType.IMAGE_JPEG_VALUE;
    }
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
   * @param keyColumn the column key.
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
   *  Gets the Clinical Form data in Base64 encoded Byte array (similar to PDF created for Print).
   *  @param id form identifier.
   *  @return byteArray
   */
  public String getFormDataEncodedByteArray(Object id) {
    return encodedBase64String;
  }

  public Map getFormSegmentInformation(Object id) {
    return Collections.emptyMap();
  }

  public boolean isFormReopened(Object id) { return false; }
}
