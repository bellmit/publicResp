package com.insta.hms.mdm.notetypes;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.exception.ConversionException;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.MasterService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class NoteTypesService.
 */
@Service
public class NoteTypesService extends MasterService {

  protected static final String CONTAINS = "contains";
  protected static final String TEMPLATE_ID = "template_id";
  protected static final String NOTE_TYPE_ID = "note_type_id";
  protected static final String NOTE_TYPE_NAME = "note_type_name";
  protected static final String TEMPLATE = "template";
  protected static final String END_DATE = "end_date";
  protected static final String START_DATE = "start_date";
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(NoteTypesService.class);
  /** The hospital role service. */
  @LazyAutowired
  private HospitalRoleService hospitalRoleService;

  /** The note types repository. */
  @LazyAutowired
  private NoteTypesRepository noteTypesRepository;

  /** The note type template repository. */
  @LazyAutowired
  private NoteTypeTemplateRepository noteTypeTemplateRepository;

  /** The note types validator. */
  @LazyAutowired
  private NoteTypesValidator noteTypesValidator;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Instantiates a new note types service.
   *
   * @param noteTypesRepository
   *          the note types repository
   * @param noteTypesValidator
   *          the note types validator
   */
  public NoteTypesService(NoteTypesRepository noteTypesRepository,
      NoteTypesValidator noteTypesValidator) {
    super(noteTypesRepository, noteTypesValidator);
  }

  /**
   * Gets the hospital roles list.
   *
   * @return the hospital roles list
   */
  public Map<String, Object> getHospitalRolesList() {
    Map<String, Object> hospitalroles = new HashMap<>();
    hospitalroles.put("hospitalroles",
        ConversionUtils.listBeanToListMap(hospitalRoleService.lookup(true)));
    return hospitalroles;
  }

  /**
   * Insert note type.
   *
   * @param params
   *          the params
   * @return the map
   */
  public Map<String, Object> saveNoteType(Map<String, Object> params) {
    Map<String, Object> map = null;
    if (params.get(NOTE_TYPE_ID) != null && !params.get(NOTE_TYPE_ID).equals("")) {
      map = updateNoteType(params);
    } else {
      map = insertNoteType(params);
    }
    return map;
  }

  /**
   * Update note type.
   *
   * @param params
   *          the params
   * @return the map
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, Object> updateNoteType(Map<String, Object> params) {
    ArrayList errors = new ArrayList();
    String strNoteTypeId = String.valueOf(params.get(NOTE_TYPE_ID));
    int noteTypeId = 0;
    try {
      noteTypeId = Integer.parseInt(strNoteTypeId);
    } catch (NumberFormatException exception) {
      errors.add("note_type_id is not valid");
    }
    BasicDynaBean noteTypeBean = noteTypesRepository.getBean();
    ConversionUtils.copyToDynaBean(params, noteTypeBean, errors);
    if (errors.isEmpty()) {
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      noteTypeBean.set("mod_user", userName);
      noteTypeBean.set("mod_time", DateUtil.getCurrentTimestamp());
      String noteTypeName = (String) noteTypeBean.get(NOTE_TYPE_NAME);
      DynaBean exists = noteTypesRepository.findByKey(NOTE_TYPE_NAME, noteTypeName);
      if (exists != null && noteTypeId != (Integer) exists.get(NOTE_TYPE_ID)) {
        throw new DuplicateEntityException(new String[] { "Note Type", noteTypeName });
      } else {
        Map keys = new HashMap();
        keys.put(NOTE_TYPE_ID, noteTypeId);
        noteTypesRepository.update(noteTypeBean, keys);
      }
      Map<String, Object> templateParams = (Map<String, Object>) params.get(TEMPLATE);
      if (templateParams != null && !templateParams.isEmpty()) {
        BasicDynaBean templateBean = noteTypeTemplateRepository.getBean();
        ConversionUtils.copyToDynaBean(templateParams, templateBean, errors);
        if (templateBean.get(TEMPLATE_ID) != null && !templateBean.get(TEMPLATE_ID)
            .equals("")) {
          Map tkeys = new HashMap();
          tkeys.put(TEMPLATE_ID, templateBean.get(TEMPLATE_ID));
          tkeys.put(NOTE_TYPE_ID, noteTypeId);
          noteTypeTemplateRepository.update(templateBean, tkeys);
        } else if (templateBean.get("template_name") != null 
            && !templateBean.get("template_name").equals("")) {
          templateBean.set(NOTE_TYPE_ID, noteTypeId);
          noteTypeTemplateRepository.insert(templateBean);
        }
      }
    } else {
      throw new ConversionException(errors);
    }
    return params;
  }

  /**
   * Insert note type.
   *
   * @param params
   *          the params
   * @return the map
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Map<String, Object> insertNoteType(Map<String, Object> params) {
    ArrayList errors = new ArrayList();
    BasicDynaBean noteTypeBean = noteTypesRepository.getBean();
    ConversionUtils.copyToDynaBean(params, noteTypeBean, errors);
    if (errors.isEmpty()) {
      noteTypesValidator.validateInsert(noteTypeBean);
      String noteTypeName = (String) noteTypeBean.get(NOTE_TYPE_NAME);
      boolean exists = noteTypesRepository.exist(NOTE_TYPE_NAME, noteTypeName);
      if (exists) {
        logger.error("Note Type already exists with name :" + noteTypeName);
        throw new DuplicateEntityException(new String[] { "Note Type", noteTypeName });
      } else {
        int noteTypeId = noteTypesRepository.getNextSequence();
        String userName = (String) sessionService.getSessionAttributes().get("userId");
        noteTypeBean.set(NOTE_TYPE_ID, noteTypeId);
        noteTypeBean.set("created_by", userName);
        noteTypeBean.set("mod_user", userName);
        noteTypesRepository.insert(noteTypeBean);
        Map<String, Object> templateParams = (Map<String, Object>) params.get(TEMPLATE);
        if (templateParams != null && !templateParams.isEmpty()) {
          BasicDynaBean templateBean = noteTypeTemplateRepository.getBean();
          ConversionUtils.copyToDynaBean(templateParams, templateBean, errors);
          templateBean.set(NOTE_TYPE_ID, noteTypeId);
          noteTypeTemplateRepository.insert(templateBean);
        }
      }
    } else {
      throw new ConversionException(errors);
    }
    return params;

  }

  /**
   * Gets the note types details.
   *
   * @param paramMap
   *          the param map
   * @return the note types details
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public PagedList getNoteTypesDetails(Map<String, String[]> paramMap) {

    Date startDate = null;
    Date endDate = null;
    try {
      if (paramMap.get(START_DATE) != null && paramMap.get(START_DATE)[0] != null
          && !paramMap.get(START_DATE)[0].equals("")) {
        startDate = DateUtil.parseDate(paramMap.get(START_DATE)[0]);
      }
      if (paramMap.get(END_DATE) != null && paramMap.get(END_DATE)[0] != null 
          && !paramMap.get(END_DATE)[0].equals("")) {
        endDate = DateUtil.parseDate(paramMap.get(END_DATE)[0]);
      }
    } catch (ParseException exe) {
      logger.error("Date parse exception occured " + exe);
    }
    paramMap.remove(START_DATE);
    paramMap.remove(END_DATE);

    PagedList notetypes = noteTypesRepository.getNoteTypesDetails(paramMap, startDate,
        endDate);
    List<Object> noteTypelist = new ArrayList<>();
    for (Map<String, Object> map : 
        (List<Map<String, Object>>) notetypes.getDtoList()) {
      Map<String, Object> noteTypeMap = new HashMap();
      noteTypeMap.putAll(map);
      List<String> templateCols = new ArrayList<>();
      templateCols.add(TEMPLATE_ID);
      templateCols.add("template_name");
      noteTypeMap.put(TEMPLATE, ConversionUtils.listBeanToListMap(noteTypeTemplateRepository
          .listAll(templateCols, NOTE_TYPE_ID, noteTypeMap.get(NOTE_TYPE_ID))));
      noteTypelist.add(noteTypeMap);
    }
    notetypes.setDtoList(noteTypelist);
    return notetypes;
  }

  /**
   * Gets the template details.
   *
   * @param parameterMap
   *          the parameter map
   * @return the template details
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getTemplateDetails(Map<String, String[]> parameterMap) {
    ValidationErrorMap errorMap = new ValidationErrorMap();
    if (!noteTypesValidator.validateTemplateId(parameterMap, errorMap)) {
      Map<String, Object> nestedException = new HashMap<>();
      ValidationException ex = new ValidationException(errorMap);
      nestedException.put(TEMPLATE_ID, ex.getErrors());
      throw new NestableValidationException(nestedException);
    }
    BasicDynaBean templateBean = noteTypeTemplateRepository.findByKey(TEMPLATE_ID,
        Integer.parseInt(parameterMap.get(TEMPLATE_ID)[0]));
    return templateBean.getMap();
  }

  /**
   * Delete template.
   *
   * @param params
   *          the params
   */
  @SuppressWarnings("rawtypes")
  public void deleteTemplate(Map params) {
    int templateId = (Integer) (params.get(TEMPLATE_ID) != null ? params.get(TEMPLATE_ID) : 0);
    noteTypeTemplateRepository.delete(TEMPLATE_ID, templateId);
  }

  /**
   * Gets the template auto complete.
   *
   * @param parameters the parameters
   * @return the template auto complete
   */
  public Map<String, Object> gettemplateAutoComplete(Map<String, String[]> parameters) {
    Map<String, Object> responseMap = new HashMap<>();
    String filterText = (null != parameters && parameters.containsKey("filterText")) 
        ? parameters.get("filterText")[0] : null;
    List<BasicDynaBean> searchSet = null;
    if (null != filterText) {
      searchSet = templateAutocomplete(filterText,parameters);
    } else {
      searchSet = noteTypeTemplateRepository.lookup(false);
    }
    responseMap.put("dtoList", ConversionUtils.listBeanToListMap(searchSet));
    responseMap.put("listSize", searchSet.size());
    return responseMap;
  }

  /**
   * Template autocomplete.
   *
   * @param match the match
   * @param parameters the parameters
   * @return the list
   */
  @SuppressWarnings("unchecked")
  public List<BasicDynaBean> templateAutocomplete(String match,
      Map<String, String[]> parameters) {
    // check for contains
    boolean contains = false;
    String paramContains = null;
    if (null != parameters.get(CONTAINS) && parameters.get(CONTAINS)[0] != null) {
      paramContains = parameters.get(CONTAINS)[0].trim();
    }
    if (null != paramContains && paramContains.equalsIgnoreCase("true")) {
      contains = true;
    }
    final String lookupQuery = noteTypeTemplateRepository.getLookupQuery();
    SearchQueryAssembler qb = null;
    qb = getLookupQueryAssembler(lookupQuery, parameters);
    addFilterForLookUp(qb, match, "template_name", contains, parameters);
    qb.build();
    PagedList pagedList = qb.getDynaPagedList();
    return pagedList.getDtoList();
  }

  public List<BasicDynaBean> getUserNoteTypes(String userName) {
    return noteTypesRepository.getUserNotes(userName);
  }

}

