package com.insta.hms.core.clinical.notes;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.core.clinical.forms.FormParameter;
import com.insta.hms.core.clinical.forms.SystemSectionService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.NestableValidationException;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.doctors.DoctorService;
import com.insta.hms.mdm.hospitalroles.HospitalRoleService;
import com.insta.hms.mdm.notetypes.NoteTypeTemplateRepository;
import com.insta.hms.mdm.notetypes.NoteTypesRepository;
import com.insta.hms.mdm.notetypes.NoteTypesService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotesService extends SystemSectionService {

  protected static final String NOTE_TYPE_ID = "note_type_id";
  protected static final String ASSOC_HOSP_ROLE_ID = "assoc_hosp_role_id";
  protected static final String NOTE_ID = "note_id";
  protected static final String PATIENT_ID = "patient_id";
  protected static final String RECORDS = "records";
  protected static final String INSERT = "insert";
  protected static final String CONSULTATION_TYPE_ID = "consultation_type_id";
  protected static final String END_DATE = "end_date";
  protected static final String START_DATE = "start_date";
  protected static final String ORIGINAL_NOTE_ID = "original_note_id";
  protected static final String UPDATE = "update";
  protected static final String TRANSCRIBING_ROLE_ID = "transcribing_role_id";
  protected static final String BILLABLE_CONSULTATION = "billable_consultation";
  protected static final String SAVE_STATUS = "save_status";
  protected static final String MOD_TIME = "mod_time";
  protected static final String DOCUMENTED_DATE = "documented_date";
  protected static final String DOCUMENTED_TIME = "documented_time";
  protected static final String ON_BEHALF_DOCTOR_ID = "on_behalf_doctor_id";
  protected static final String CHARGE_ID = "charge_id";
  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @LazyAutowired
  private NoteTypesService noteTypesService;
  @LazyAutowired
  private NoteTypesRepository noteTypesRepository;
  @LazyAutowired
  private NoteTypeTemplateRepository noteTypeTemplateRepository;
  @LazyAutowired
  private DoctorService doctorService;
  @LazyAutowired
  private HospitalRoleService hospitalRoleService;
  @LazyAutowired
  private CenterService centerService;
  @LazyAutowired
  private ConsultationTypesService consultationTypesService;
  @LazyAutowired
  private NotesRepository notesRepository;
  @LazyAutowired
  private SessionService sessionService;
  @LazyAutowired
  private BillService billService;
  @LazyAutowired
  private RegistrationService registrationService;
  @LazyAutowired
  private PatientInsurancePlansService patientInsurancePlansService;
  @LazyAutowired
  private DoctorOrderItemService doctorOrderService;
  @LazyAutowired
  private NotesValidator notesValidator;
  @LazyAutowired
  private IpPreferencesService ipPrefService;
  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> saveSection(Map<String, Object> requestBody, BasicDynaBean sdbean,
      FormParameter parameter, Map<String, Object> errorMap) {

    ValidationErrorMap errMap = new ValidationErrorMap();
    boolean isValid = true;
    List<BasicDynaBean> insertBeans = new ArrayList<>();
    List<BasicDynaBean> updateBeans = new ArrayList<>();
    Map<String, Object> responseData = new HashMap<>();
    Map<String, Integer> billableConsDocs = new HashMap<>();

    BasicDynaBean ipPrefs = ipPrefService.getPreferences();
    Integer maxBillableConsDay = (Integer) ipPrefs.get("max_billable_cons_day");

    Map<String, Object> updatedReqBody = null;

    if (requestBody.get(INSERT) != null) {
      isValid = insertPatientNotes(insertBeans, requestBody, responseData, errMap, errorMap,
          billableConsDocs, maxBillableConsDay);
      requestBody = distinguishUpdateRequestOnAdmReq(requestBody);
    }

    Map<String, Object> updateKeysMap = new HashMap<>();
    if (requestBody.get(UPDATE) != null) {
      isValid = isValid && updatePatientNotes(insertBeans, updateBeans, requestBody, updateKeysMap,
          responseData, errMap, errorMap, billableConsDocs, maxBillableConsDay);
    }
    if (isValid) {
      if (!insertBeans.isEmpty()) {
        notesRepository.batchInsert(insertBeans);
      }
      if (!updateBeans.isEmpty()) {
        notesRepository.batchUpdate(updateBeans, updateKeysMap);
      }
      String userName = (String) sessionService.getSessionAttributes().get("userId");
      /*logger.info("parameter patientId : "
              + parameter.getPatientId());*/
      boolean duplicateexists =
          notesValidator.validateDuplicateDraftNoteType(parameter.getPatientId(), errMap, userName);
      if (!duplicateexists) {
        Map<String, Object> nestedException = new HashMap<String, Object>();
        nestedException.put(NOTE_TYPE_ID, new ValidationException(errMap).getErrors());
        throw new NestableValidationException(nestedException);
      }
    } else {
      return null;
    }

    return responseData;
  }

  private Map<String, Object> distinguishUpdateRequestOnAdmReq(Map<String, Object> requestBody) {
    List<Map<String, Object>> mapList = new ArrayList<>();
    Map<String,Object> updatedRequestBody = new HashMap<>();
    Integer noteTypeId = noteTypesRepository.getNotesTypeIdByName("Admission Request Notes");
    for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(INSERT)) {
      if (row.get("note_type_id") != null && noteTypeId != null
              && Integer.parseInt(row.get("note_type_id").toString()) == noteTypeId
              && row.get("original_note_id") != null) {
        mapList.add(row);
      }
    }
    if (!mapList.isEmpty()) {
      updatedRequestBody.putAll(requestBody);
      updatedRequestBody.remove("insert");
      updatedRequestBody.put("update", mapList);
      return updatedRequestBody;
    }
    return requestBody;
  }

  @SuppressWarnings("unchecked")
  private boolean updatePatientNotes(List<BasicDynaBean> insertBeans,
      List<BasicDynaBean> updateBeans, Map<String, Object> requestBody,
      Map<String, Object> updateKeysMap, Map<String, Object> responseData,
      ValidationErrorMap errMap, Map<String, Object> errorMap,
      Map<String, Integer> billableConsDocs, Integer maxBillableConsDay) {
    boolean isValid = true;
    List<Object> updateKeys = new ArrayList<>();
    responseData.put(UPDATE, new HashMap<String, Object>());
    Integer recordIndex = 0;
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(UPDATE)) {
      errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
      isValid = notesValidator.validateEditNoteParams(row, errMap) && isValid;
      List<String> conversionErrorList = new ArrayList<>();
      BasicDynaBean patientNoteBean = notesRepository.getBean();
      ConversionUtils.copyJsonToDynaBean(row, patientNoteBean, conversionErrorList, false);
      patientNoteBean.set("mod_user", userName);
      patientNoteBean.set(MOD_TIME, new java.sql.Timestamp(new java.util.Date().getTime()));
      patientNoteBean.set(CHARGE_ID, "");
      int originalNoteId = (int) patientNoteBean.get(NOTE_ID);
      BasicDynaBean originalNote = notesRepository.findByKey(NOTE_ID, originalNoteId);
      isValid = notesValidator.isEditableNoteType(originalNote.getMap(), errMap) && isValid;
      if (originalNote.get(SAVE_STATUS).equals("D")) {
        updateDraftNote(row, updateBeans, patientNoteBean, updateKeys, billableConsDocs,
            responseData, maxBillableConsDay);
      } else {
        int newNoteId = (int) notesRepository.getNextSequence();
        // creating new note in edit mode
        patientNoteBean.set("created_by", userName);
        patientNoteBean.set(NOTE_ID, newNoteId);
        patientNoteBean.set("created_time", new java.sql.Timestamp(new java.util.Date().getTime()));
        setDocumentedDateTime(row,patientNoteBean);
        patientNoteBean.set(ORIGINAL_NOTE_ID, originalNoteId);
        insertBeans.add(patientNoteBean);
        // updating to original note
        originalNote.set("mod_user", userName);
        originalNote.set(MOD_TIME, new java.sql.Timestamp(new java.util.Date().getTime()));
        originalNote.set("new_note_id", newNoteId);
        updateBeans.add(originalNote);
        updateKeys.add(originalNote.get(NOTE_ID));
      }
      if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
        if (!errorMap.containsKey(UPDATE)) {
          errorMap.put(UPDATE, new HashMap<String, Object>());
        }
        ((Map<String, Object>) errorMap.get(UPDATE)).put((recordIndex).toString(),
            (new ValidationException(
                ValidationUtils.copyCoversionErrors(errMap, conversionErrorList))).getErrors());
      }
      Map<String, Object> record = new HashMap<>();
      if (originalNote.get(SAVE_STATUS).equals("D")) {
        record.put(NOTE_ID, patientNoteBean.get(NOTE_ID));
      } else {
        record.put("new_note_id", patientNoteBean.get(NOTE_ID));
      }
      ((Map<String, Object>) responseData.get(UPDATE)).put(recordIndex.toString(), record);
      recordIndex++;
    }
    updateKeysMap.put(NOTE_ID, updateKeys);
    return isValid;
  }

  private void setDocumentedDateTime(Map<String, Object> row,BasicDynaBean patientNoteBean) {
    if (row.containsKey(DOCUMENTED_DATE) && row.get(DOCUMENTED_DATE) != null) {
      Date parsedDate = getParsedSqlDate((String) row.get(DOCUMENTED_DATE));
      patientNoteBean.set(DOCUMENTED_DATE,parsedDate);
    } else {
      patientNoteBean.set(DOCUMENTED_DATE, new java.sql.Date(new java.util.Date().getTime()));
    }
    if (row.containsKey(DOCUMENTED_TIME) && row.get(DOCUMENTED_TIME) != null) {
      patientNoteBean.set(DOCUMENTED_TIME,getParsedTime((String) row.get(DOCUMENTED_TIME)));
    } else {
      patientNoteBean.set(DOCUMENTED_TIME, new java.sql.Time((new java.util.Date()).getTime()));
    }
  }

  private void updateDraftNote(Map<String, Object> row, List<BasicDynaBean> updateBeans,
      BasicDynaBean patientNoteBean, List<Object> updateKeys, Map<String, Integer> billableConsDocs,
      Map<String, Object> responseData, Integer maxBillableConsDay) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    String userName = (String) sessionAttributes.get("userId");
    if (row.get(BILLABLE_CONSULTATION) != null && row.get(BILLABLE_CONSULTATION).equals("Y")
        && patientNoteBean.get(SAVE_STATUS).equals("F")) {
      setDoctorCharges(row, userName, patientNoteBean, centerId, billableConsDocs, responseData,
          maxBillableConsDay);
    }
    updateBeans.add(patientNoteBean);
    updateKeys.add(patientNoteBean.get(NOTE_ID));

  }

  /**
   * Insert patient notes.
   * @param insertBeans the list of beans
   * @param requestBody the map
   * @param responseData the map
   * @param errMap the map
   * @param errorMap the map
   * @param billableConsDocs the map
   * @param maxBillableConsDay integer
   * @return boolean value
   */
  @SuppressWarnings("unchecked")
  public boolean insertPatientNotes(List<BasicDynaBean> insertBeans,
      Map<String, Object> requestBody, Map<String, Object> responseData, ValidationErrorMap errMap,
      Map<String, Object> errorMap, Map<String, Integer> billableConsDocs,
      Integer maxBillableConsDay) {
    boolean isValid = true;
    responseData.put(INSERT, new HashMap<String, Object>());
    Integer recordIndex = 0;
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    String userName = (String) sessionAttributes.get("userId");
    Integer admissionReqNoteTypeId =
        noteTypesRepository.getNotesTypeIdByName("Admission Request Notes");
    for (Map<String, Object> row : (List<Map<String, Object>>) requestBody.get(INSERT)) {
      if (row != null && row.get("note_type_id") != null
              && Integer.parseInt(row.get("note_type_id").toString()) != admissionReqNoteTypeId) {
        errMap.setErrorMap(new HashMap<String, Map<String, List<String>>>());
        isValid = notesValidator.validateSaveNoteParams(row, errMap) && isValid;
        BasicDynaBean patientNoteBean = notesRepository.getBean();
        List<String> conversionErrorList = new ArrayList<>();
        ConversionUtils.copyJsonToDynaBean(row, patientNoteBean, conversionErrorList, false);
        patientNoteBean.set(NOTE_ID, notesRepository.getNextSequence());
        patientNoteBean.set(MOD_TIME, new java.sql.Timestamp(new java.util.Date().getTime()));
        setDocumentedDateTime(row,patientNoteBean);
        patientNoteBean.set(CHARGE_ID, "");
        if (row.get(BILLABLE_CONSULTATION) != null && row.get(BILLABLE_CONSULTATION).equals("Y")
                && patientNoteBean.get(SAVE_STATUS).equals("F")) {
          setDoctorCharges(row, userName, patientNoteBean, centerId, billableConsDocs, responseData,
                  maxBillableConsDay);
        }
        patientNoteBean.set("created_by", userName);
        insertBeans.add(patientNoteBean);
        if (!errMap.getErrorMap().isEmpty() || !conversionErrorList.isEmpty()) {
          if (!errorMap.containsKey(INSERT)) {
            errorMap.put(INSERT, new HashMap<String, Object>());
          }
          ((Map<String, Object>) errorMap.get(INSERT)).put((recordIndex).toString(),
                  (new ValidationException(
                          ValidationUtils.copyCoversionErrors(errMap, conversionErrorList)))
                          .getErrors());
        }
        Map<String, Object> record = new HashMap<>();
        record.put(NOTE_ID, patientNoteBean.get(NOTE_ID));
        ((Map<String, Object>) responseData.get(INSERT)).put(recordIndex.toString(), record);
        recordIndex++;
      }
    }
    return isValid;
  }

  private Date getParsedSqlDate(String date) {
    Date finalParsedDate = null;
    try {
      SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
      java.util.Date parsedDate = sdf1.parse(date);
      finalParsedDate = new java.sql.Date(parsedDate.getTime());
    } catch (ParseException ex) {
      logger.error("Issue while parsing date",ex);
    }
    return finalParsedDate;
  }

  private Time getParsedTime(String time) {
    Time finalTime = null;
    try {
      finalTime = DateUtil.parseTime(time);
    } catch (ParseException ex) {
      logger.error("Issue while parsing time",ex);
    }
    return finalTime;
  }

  /**
   * Sets doctor charges.
   * @param row the map
   * @param userName the username
   * @param patientNoteBean the bean
   * @param centerId the center id
   * @param billableConsDocs the map
   * @param responseData the map
   * @param maxBillableConsDay integer
   */
  public void setDoctorCharges(Map<String, Object> row, String userName,
      BasicDynaBean patientNoteBean, Integer centerId, Map<String, Integer> billableConsDocs,
      Map<String, Object> responseData, Integer maxBillableConsDay) {
    String patientId = (String) row.get(PATIENT_ID);
    int consultationTypeId =
        (row.get(CONSULTATION_TYPE_ID) != null && !row.get(CONSULTATION_TYPE_ID).equals(""))
            ? (int) row.get(CONSULTATION_TYPE_ID)
            : 0;
    String doctorId =
        row.get(ON_BEHALF_DOCTOR_ID) != null ? (String) row.get(ON_BEHALF_DOCTOR_ID) : "";
    boolean valid = checkMaxBillableConsultations(doctorId, patientId, patientNoteBean,
        billableConsDocs, maxBillableConsDay);
    if (!valid) {
      responseData.put("message", "maxBillable");
      return;
    }
    BasicDynaBean billBean = billService.getLatestOpenBillLaterElseBillNow(patientId);
    if (billBean == null) {
      patientNoteBean.set(BILLABLE_CONSULTATION, "N");
      patientNoteBean.set(CONSULTATION_TYPE_ID, null);
      responseData.put("message", "noOpenBills");
      return;
    }
    try {
      BasicDynaBean headerInformation =
          registrationService.getBillPatientInfo(patientId, (String) billBean.get("bill_no"));
      BasicDynaBean orderBean = registrationService.regBaseDoctor(doctorId,
          String.valueOf(consultationTypeId), null, null, "", 0);
      if (orderBean != null) {
        orderBean.set("common_order_id", headerInformation.get("commonorderid"));
        orderBean.set("username", headerInformation.get("user_name"));
        orderBean.set(PATIENT_ID, headerInformation.get(PATIENT_ID));
        orderBean.set("consultation_id", 0);
        orderBean.set("presc_doctor_id", doctorId);
        int[] planIds = patientInsurancePlansService.getPlanIds(patientId);
        List<BasicDynaBean> chargesList = null;
        chargesList = doctorOrderService.insertOrderCharges(true, headerInformation, orderBean,
            billBean, null, null, planIds, "", "DOC", centerId, false, null);
        if (chargesList.size() > 0) {
          BasicDynaBean chargeBean = chargesList.get(0);
          if (chargeBean != null) {
            patientNoteBean.set(CHARGE_ID, chargeBean.get(CHARGE_ID));
            patientNoteBean.set(CONSULTATION_TYPE_ID, consultationTypeId);
          }
        }
      }
    } catch (Exception exe) {
      logger.error("Exception occured while setting up doctor charges " + exe);
    }

  }

  /**
   * Check max billable cons.
   * @param doctorId the doc id
   * @param patientId the visit id
   * @param patientNoteBean the bean
   * @param billableConsDocs the map
   * @param maxBillableConsDay integer
   * @return boolean value
   */
  public boolean checkMaxBillableConsultations(String doctorId, String patientId,
      BasicDynaBean patientNoteBean, Map<String, Integer> billableConsDocs,
      Integer maxBillableConsDay) {
    if (!doctorId.equals("")) {
      int newBillableCons = 0;
      if (billableConsDocs.containsKey(doctorId)) {
        newBillableCons = billableConsDocs.get(doctorId);
      } else {
        newBillableCons = notesRepository.getBillableNotesForDay(patientId, doctorId);
      }
      int totalBillableCons = newBillableCons + 1;
      if (totalBillableCons <= maxBillableConsDay) {
        billableConsDocs.put(doctorId, totalBillableCons);
        return true;
      } else {
        patientNoteBean.set(BILLABLE_CONSULTATION, "N");
        patientNoteBean.set(CONSULTATION_TYPE_ID, null);
      }
    }
    return false;
  }

  @Override
  public Boolean deleteSection(Integer sectiondetailId, FormParameter parameter,
      Map<String, Object> errorMap) {
    return false;
  }

  @Override
  public Map<String, Object> getSectionDetailsFromLastSavedForm(FormParameter parameter) {
    return getSectionDetailsFromCurrentForm(parameter);
  }

  @Override
  public Map<String, Object> getSectionDetailsFromCurrentForm(FormParameter parameter) {
    Map<String, Object> data = new HashMap<>();
    Map<String, String[]> paramMap = new HashMap<>();
    String patientId = parameter.getPatientId();
    PagedList pagedList = getPatientNotes(patientId, paramMap);
    data.put(RECORDS, pagedList.getDtoList());
    Map<String, Object> pageInfo = new HashMap<>();
    pageInfo.put("total_records", pagedList.getTotalRecords());
    pageInfo.put("page_size", pagedList.getPageSize());
    pageInfo.put("page_number", pagedList.getPageNumber());
    pageInfo.put("num_pages", pagedList.getNumPages());
    pageInfo.put("total_notes", getTotalNotesCoun(patientId));
    data.put("page_info", pageInfo);
    return data;
  }

  /**
   * Gets user note types.
   * @return list of map
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<Map<String, Object>> getUserNoteTypes() { 
    List<Map<String, Object>> noteTypelist = new ArrayList<>();
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    String userName = (String) sessionAttributes.get("userId");
    Integer roleId = (Integer) sessionAttributes.get("roleId");
    Integer centerId = (Integer) sessionAttributes.get("centerId");
    List<BasicDynaBean> notetypes = null;
    if (roleId == 1 || roleId == 2) {
      List<String> noteTypeCols = new ArrayList<>();
      noteTypeCols.add(NOTE_TYPE_ID);
      noteTypeCols.add("note_type_name");
      noteTypeCols.add(ASSOC_HOSP_ROLE_ID);
      noteTypeCols.add(TRANSCRIBING_ROLE_ID);
      noteTypeCols.add("billing_option");
      notetypes = noteTypesRepository.listAll(noteTypeCols, "status", "A");
    } else {
      notetypes = noteTypesService.getUserNoteTypes(userName);
    }
    for (BasicDynaBean noteBean : notetypes) {
      Map<String, Object> noteTypeMap = new HashMap();
      noteTypeMap.putAll(noteBean.getMap());
      List<String> templateCols = new ArrayList<>();
      templateCols.add("template_id");
      templateCols.add("template_name");
      noteTypeMap.put("template", ConversionUtils.listBeanToListMap(noteTypeTemplateRepository
          .listAll(templateCols, NOTE_TYPE_ID, noteTypeMap.get(NOTE_TYPE_ID))));
      List<Map<String, Object>> behalfOfUsers = new ArrayList<>();
      if (noteTypeMap.get(ASSOC_HOSP_ROLE_ID) != null) {
        if ((Integer) noteTypeMap.get(ASSOC_HOSP_ROLE_ID) == -1) {
          behalfOfUsers = doctorService.getDoctorUsers(centerId);
        } else {
          behalfOfUsers = hospitalRoleService
              .getHospitalUsers((Integer) noteTypeMap.get(ASSOC_HOSP_ROLE_ID), centerId);
        }
      }
      noteTypeMap.put("on_behalf_of_users", behalfOfUsers);
      noteTypelist.add(noteTypeMap);
    }

    return noteTypelist;
  }

  /**
   * Gets dropdown values for date fields.
   * @return list of map with values
   */
  public List<Map<String,String>> getSortByDateFields() {
    Map<String,String> entryDateMap = new HashMap<>();
    Map<String,String> documentDateMap = new HashMap<>();
    entryDateMap.put("sort_date", "created_time");
    entryDateMap.put("sort_date_name", "Entry Date");
    documentDateMap.put("sort_date", "documented_date");
    documentDateMap.put("sort_date_name", "Document Date");
    List<Map<String,String>> fieldMapList = new ArrayList<>();
    fieldMapList.add(entryDateMap);
    fieldMapList.add(documentDateMap);
    return fieldMapList;
  }

  /**
   * Gets cons types for rate plan.
   * @param ordId the rate plan id
   * @param centerId the center id
   * @return list of beans
   */
  public List<BasicDynaBean> getConsultationTypesForRateplan(String ordId, Integer centerId) {
    Map<String, Object> params = new HashMap<>();
    params.put("center_id", centerId);
    BasicDynaBean centerBean = centerService.findByPk(params);
    String healthAuthority = (String) centerBean.get("health_authority");
    healthAuthority = healthAuthority == null ? "" : healthAuthority;
    return consultationTypesService.getConsultationTypes("i", ordId, healthAuthority);
  }

  /**
   * Gets patient notes.
   * @param patientId the visit id
   * @param paramMap the param map
   * @return paged list
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public PagedList getPatientNotes(String patientId, Map<String, String[]> paramMap) {
    Date startDate = null;
    Date endDate = null;
    try {
      if (paramMap.get(START_DATE) != null && paramMap.get(START_DATE)[0] != null
          && !paramMap.get(START_DATE)[0].equals("")) {
        startDate = DateUtil.parseDate(paramMap.get(START_DATE)[0]);
      } else {
        startDate = DateUtil.getCurrentDate();
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
    paramMap.put(PATIENT_ID, new String[] {patientId});
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer roleId = (Integer) sessionAttributes.get("roleId");
    String userName = (String) sessionAttributes.get("userId");
    PagedList notetypes =
        notesRepository.getPatientNotesDetails(paramMap, startDate, endDate, roleId, userName);
    List<Object> noteTypelist = new ArrayList<>();
    for (Map<String, Object> map : (List<Map<String, Object>>) notetypes.getDtoList()) {
      Map<String, Object> noteTypeMap = new HashMap();
      noteTypeMap.putAll(map);
      noteTypeMap.put("isEditable", notesValidator.isEditableNoteType(noteTypeMap, null));
      if (noteTypeMap.get(ORIGINAL_NOTE_ID) != null
          && !noteTypeMap.get(ORIGINAL_NOTE_ID).equals("")) {
        Map keys = new HashMap<>();
        keys.put(NOTE_ID, noteTypeMap.get(ORIGINAL_NOTE_ID));
        keys.put(PATIENT_ID, patientId);
        BasicDynaBean originalNote = notesRepository.findByKey(keys);
        noteTypeMap.put("original_note", originalNote.getMap());
      }
      noteTypelist.add(noteTypeMap);
    }
    notetypes.setDtoList(noteTypelist);
    return notetypes;
  }

  /**
   * Gets patient final notes.
   * 
   * @param patientId the visit id
   * @return list of beans
   */
  public List<BasicDynaBean> getPatientFinalNotes(String patientId, List<Integer> noteTypeIds) {
    List<BasicDynaBean> notesForPrint = notesRepository.getPatientNotesForPrint(
        patientId, noteTypeIds);
    return cleanUpNotes(notesForPrint);
  }

  private List<BasicDynaBean> cleanUpNotes(List<BasicDynaBean> notesForPrint) {
    String cleanUpRegex = "(/*?)/>";
    String noteContentLiteral = "note_content";
    for (BasicDynaBean note: notesForPrint) {
      String noteContent = (String) note.get(noteContentLiteral);
      int noteTypeId = (int) note.get("note_type_id");
      if (noteTypeId > 0 && noteTypeId < 5) {
        note.set(noteContentLiteral, noteContent.replace("\n", "<br/>"));
      } else {
        note.set(noteContentLiteral, noteContent.replaceAll(cleanUpRegex, "/>"));
      }
    }
    return notesForPrint;
  }

  /**
   * Gets billed cons count.
   * 
   * @param patientId the visit id
   * @param centerId the center id
   * @return list of map
   */
  public List<Map<String, Object>> getBilledConsCountPerDay(String patientId, Integer centerId) {

    return ConversionUtils
        .listBeanToListMap(notesRepository.getBilledConsCountPerDay(patientId, centerId));
  }

  /**
   * Gets total notes.
   * 
   * @param patientId the visit id
   * @return integer
   */
  public Integer getTotalNotesCoun(String patientId) {
    Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
    Integer roleId = (Integer) sessionAttributes.get("roleId");
    String userName = (String) sessionAttributes.get("userId");
    return notesRepository.getTotalNotesCount(patientId, roleId, userName);
  }

}
