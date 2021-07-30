package com.insta.hms.core.clinical.multiuser;

import com.insta.hms.common.PushService;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.careteam.CareTeamService;
import com.insta.hms.core.clinical.forms.ClinicalFormService;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SectionService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.formcomponents.FormComponentsService.FormType;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MultiUserFormService.
 *
 * @author sonam
 */
@Service
public abstract class MultiUserFormService extends ClinicalFormService {

  /** The push service. */
  @LazyAutowired
  private PushService pushService;

  /** The care team service. */
  @LazyAutowired
  private CareTeamService careTeamService;

  /** The web socket channel. */
  protected String webSocketChannel;

  /** The redis repository. */
  @LazyAutowired
  private MultiUserRedisRepository redisRepository;

  /**
   * Instantiates a new multi user form service.
   *
   * @param formType the form type
   * @param webSocketChannel the channel
   */
  public MultiUserFormService(FormType formType, String webSocketChannel) {
    super(formType);
    this.webSocketChannel = webSocketChannel;

  }


  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#save(java.lang.Object, java.util.Map)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
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
    List<Map<String, Object>> sections = (List<Map<String, Object>>) params.get("sections");
    if (sections != null) {
      response.put("sections", saveAllSections(params, parameter, nestedException));

      if (!nestedException.isEmpty()) {
        validationErr.put("sections", nestedException);
        throw new NestableValidationException(validationErr);
      }

    } else {
      validationErr.put("sections", "exception.form.section.required");
      nestedException.putAll(validationErr);
      throw new NestableValidationException(validationErr);
    }
    // Post form save
    Map<String, Object> postFormErrMap = new HashMap<>();
    postFormSave(params, parameter, response, postFormErrMap);
    Map<String, Object> webSocketResponse = new HashMap<>();
    Map<String, Object> webResponseMap = new HashMap<>();
    if (!postFormErrMap.isEmpty()) {
      nestedException.putAll(postFormErrMap);
      throw new NestableValidationException(nestedException);
    } else {
      String key = getSectionLockRedisKey(parameter);
      redisRepository.deleteKey(key);
      webResponseMap.put("formUpdate", true);
      webResponseMap.put("patientId", formKeyValue);
      webResponseMap.put("userName", sessionService.getSessionAttributes().get("userId"));
      response.put("mod_time", new java.sql.Timestamp((new java.util.Date()).getTime()));
      webSocketResponse.put("formUpdate", webResponseMap);
      pushSectionResponseToWebSocket(webSocketResponse, (String) formKeyValue);
    }

    return response;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.core.clinical.forms.FormService#saveAllSections(java.util.Map,
   * com.insta.hms.core.clinical.forms.FormParameter, java.util.Map)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected List<Map<String, Object>> saveAllSections(Map<String, Object> allSections,
      FormParameter formparams, Map<String, Object> nestedException) {
    // Saving the form
    List<Map<String, Object>> sections = (List<Map<String, Object>>) allSections.get("sections");
    Map<String, Object> stnErrorMap;
    List<Map<String, Object>> sectionsResponseList = new ArrayList<>();

    for (Map<String, Object> sec : sections) {
      Integer sectionId = (Integer) sec.get(SECTION_ID_KEY);
      sec.put("transaction_start_date", allSections.get("transaction_start_date"));
      stnErrorMap = new HashMap<>();
      Map<String, Object> sectionResponseMap = new HashMap<>();
      sectionResponseMap = saveSection(sec, formparams, sectionId, stnErrorMap);
      if (sectionResponseMap != null) {
        sectionsResponseList.add(sectionResponseMap);
      }
      if (!stnErrorMap.isEmpty()) {
        nestedException.put(((Integer) sections.indexOf(sec)).toString(), stnErrorMap);
      }
    }

    return sectionsResponseList;
  }

  /**
   * Auto save section.
   *
   * @param section the section
   * @param formparams the formparams
   * @param nestedException the nested exception
   * @return the map
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> autoSaveSection(Map<String, Object> section, FormParameter formparams,
      Map<String, Object> nestedException) {

    Map<String, Object> stnErrorMap = new HashMap<>();
    Integer sectionId = (Integer) section.get(SECTION_ID_KEY);
    SectionService secService = sectionFactory.getSectionService(sectionId);
    // save section
    BasicDynaBean sdbean = saveSectionMain(section, formparams, stnErrorMap);
    Map<String, Object> responseData =
        secService.saveSection(section, sdbean, formparams, stnErrorMap);

    if (responseData == null) {
      responseData = new HashMap<>();
    }
    responseData.put(SECTION_ID_KEY, section.get(SECTION_ID_KEY));
    responseData.put(SECTION_DETAIL_ID_KEY, sdbean.get(SECTION_DETAIL_ID_KEY));
    responseData.put("display_order", section.get("display_order"));
    responseData.put("form_id", section.get("form_id"));
    responseData.put("user_name", sdbean.get("user_name"));
    responseData.put("mod_time", sdbean.get("mod_time"));
    responseData.put(REVISION_NUMBER, sdbean.get(REVISION_NUMBER));
    responseData.put(FINALIZED_KEY, sdbean.get(FINALIZED_KEY));


    if (!stnErrorMap.isEmpty()) {
      nestedException.put(sectionId.toString(), stnErrorMap);
    }
    return responseData;

  }

  /**
   * Save section.
   *
   * @param formKeyValue the form key value
   * @param sectionId the section id
   * @param requestBody the request body
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> saveSection(Object formKeyValue, Integer sectionId,
      Map<String, Object> requestBody) {

    FormParameter parameter = getFormParameter(formKeyValue, null);
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> validationErr = new HashMap<>();
    Map<String, Object> nestedException = new HashMap<>();
    formValidator.validateSectionDetails(sectionId, requestBody);
    if (requestBody.get(SECTION_ID_KEY) == null) {
      requestBody.put(SECTION_ID_KEY, sectionId);
    }
    String key = getSectionLockRedisKey(parameter);
    Map<String, Object> sectionLock = redisRepository.getHashKeyData(key, sectionId);
    String loggedInUser = (String) sessionService.getSessionAttributes().get("userId");
    // Check for valid user who locked the section
    if (sectionId == -18 || (sectionLock != null && !sectionLock.isEmpty()
        && loggedInUser.equals(sectionLock.get("user_name")))) {
      // save individual section.
      response.put("section", autoSaveSection(requestBody, parameter, nestedException));
    } else if (sectionId != -22) {
      // TODO:-Need to check for section lock incase of only sending erx
      ValidationErrorMap errMap = new ValidationErrorMap();
      errMap.addError("user_name", "exception.notvalid.user.for.autosave");
      throw new ValidationException(errMap);
    }
    Map<String, Object> webSocketResMap = new HashMap<>();
    if (!nestedException.isEmpty()) {
      validationErr.put("section", nestedException);
      throw new NestableValidationException(validationErr);
    }
    // Post section save
    if (sectionId.equals(-22) && formType.toString().equals("Form_IP")) {
      Map<String, Object> postFormErrMap = new HashMap<>();
      postFormSave(requestBody, parameter, response, postFormErrMap);
      if (!postFormErrMap.isEmpty()) {
        nestedException.putAll(postFormErrMap);
        throw new NestableValidationException(nestedException);
      }
    }
    webSocketResMap.putAll(removeSectionLock(key, sectionId));
    webSocketResMap.put("sectionUpdate", true);
    webSocketResMap.put("patientId", formKeyValue);
    webSocketResMap.put("section_id", sectionId);
    webSocketResMap.put("userName", sessionService.getSessionAttributes().get("userId"));
    Map<String, Object> webSocketResponse = new HashMap<>();
    webSocketResponse.put("sectionUpdate", webSocketResMap);
    pushSectionResponseToWebSocket(webSocketResponse, (String) formKeyValue);


    return response;
  }

  /**
   * Removes the section lock.
   *
   * @param key the key
   * @param sectionId the section id
   * @return the map
   */
  public Map<String, Object> removeSectionLock(String key, Integer sectionId) {
    redisRepository.deleteData(key, sectionId);
    Map<String, Object> keyMap = new HashMap<>();
    keyMap.put("sectionsLock", redisRepository.getData(key));
    return keyMap;
  }

  /**
   * Save care team.
   *
   * @param formKeyValue the form key value
   * @param requestBody the request body
   * @return the map
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> saveCareTeam(Object formKeyValue, Map<String, Object> requestBody) {
    FormParameter parameter = getFormParameter(formKeyValue, null);
    Map<String, Object> response = new HashMap<>();
    Map<String, Object> errMap = new HashMap<>();
    Map<String, Object> nestedException = new HashMap<>();
    Map<String, Object> careTeamErrMap = new HashMap<>();
    Map<String, Object> webSocketResponse = new HashMap<>();
    List<Map<String, Object>> params = (List<Map<String, Object>>) requestBody.get("careTeam");
    if (params != null) {
      response = careTeamService.saveCareTeamData(params, parameter, careTeamErrMap);
      if (!careTeamErrMap.isEmpty()) {
        errMap.put("careTeamError", careTeamErrMap);
        nestedException.putAll(errMap);
        throw new NestableValidationException(nestedException);
      }
      response.put("careTeamUpdate", true);
      response.put("patientId", formKeyValue);
      response.put("userName", sessionService.getSessionAttributes().get("userId"));
      webSocketResponse.put("careTeamUpdate", response);
      pushSectionResponseToWebSocket(webSocketResponse, (String) formKeyValue);
    }

    return response;
  }

  /**
   * Push section response to web socket.
   *
   * @param sectionResponse the section response
   * @param patientId the patient id
   */
  public void pushSectionResponseToWebSocket(Map<String, Object> sectionResponse,
      String patientId) {
    String channel = patientId.concat(webSocketChannel);
    this.pushService.push(channel, sectionResponse);
  }

  /**
   * Gets the sections lock.
   *
   * @param patientId the patient id
   * @param sectionId the section id
   * @return the sections lock
   */
  public abstract boolean getSectionsLock(String patientId, Integer sectionId);

  /**
   * Gets the section lock redis key.
   *
   * @param parameter the parameter
   * @return the section lock redis key
   */
  public abstract String getSectionLockRedisKey(FormParameter parameter);

  /**
   * Delete section lock.
   *
   * @param userName the user name
   */
  public abstract void deleteSectionLock(String userName);

  /**
   * Delete section lock.
   *
   * @param sectionId the section id
   * @param visitId the visit id
   */
  public abstract void deleteSectionLock(Integer sectionId, String visitId);

}
