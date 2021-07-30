package com.insta.hms.core.clinical.notes;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.MasterValidator;
import com.insta.hms.mdm.notetypes.NoteTypesRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NotesValidator extends MasterValidator {

  @LazyAutowired
  private NotesRepository notesRepository;
  @LazyAutowired
  private NoteTypesRepository noteTypesRepository;

  protected static final String PATIENT_ID = "patient_id";
  protected static final String ON_BEHALF_DOCTOR_ID = "on_behalf_doctor_id";
  protected static final String NOTE_CONTENT = "note_content";
  protected static final String SAVE_STATUS = "save_status";
  protected static final String NOTE_TYPE_ID = "note_type_id";
  protected static final String NOTE_ID = "note_id";
  protected static final String ORIGINAL_NOTE_ID = "original_note_id";
  protected static final String TRANSCRIBING_ROLE_ID = "transcribing_role_id";
  protected static final String BILLABLE_CONSULTATION = "billable_consultation";
  protected static final String CONSULTATION_TYPE_ID = "consultation_type_id";


  /**
   * validates save notes.
   * 
   * @param params the param
   * @param errors the errors
   * @return boolean value
   */
  public boolean validateSaveNoteParams(Map<String, Object> params, ValidationErrorMap errors) {
    boolean status = true;
    if (params == null || params.isEmpty() || errors == null) {
      return false;
    }
    if (params.get(NOTE_TYPE_ID) == null || params.get(NOTE_TYPE_ID).equals("")) {
      errors.addError(NOTE_TYPE_ID, "exception.notes.notnull.notetypeid");
      status = false;
    }
    if (params.get(PATIENT_ID) == null || params.get(PATIENT_ID).equals("")) {
      errors.addError(PATIENT_ID, "exception.notes.notnull.paientid");
      status = false;
    }
    if (params.get(NOTE_CONTENT) == null || params.get(NOTE_CONTENT).equals("")) {
      errors.addError(NOTE_CONTENT, "exception.notes.notnull.notecontent");
      status = false;
    }
    if (params.get(SAVE_STATUS) == null || params.get(SAVE_STATUS).equals("")) {
      errors.addError(SAVE_STATUS, "exception.notes.notnull.savestatus");
      status = false;
    }
    status = checkBillableConsultation(params, errors) && status;
    return status;
  }

  /**
   * Validates duplicate draft note.
   * 
   * @param patientId the visit id
   * @param errMap the error map
   * @param userName the username
   * @return boolean value
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public boolean validateDuplicateDraftNoteType(String patientId, ValidationErrorMap errMap,
      String userName) {
    List<BasicDynaBean> drafnotes = notesRepository.getSameNoteTypeDrafts(patientId, userName);
    for (BasicDynaBean countBean : drafnotes) {
      long count = (long) countBean.get("count");
      if (count > 1) {
        errMap.addError(NOTE_TYPE_ID, "exception.notes.duplicatedraft.notetype");
        return false;
      }
    }

    return true;
  }

  /**
   * Validates edit note params.
   * 
   * @param params the param
   * @param errors the errors
   * @return boolean value
   */
  public boolean validateEditNoteParams(Map<String, Object> params, ValidationErrorMap errors) {
    boolean status = true;
    if (params == null || params.isEmpty() || errors == null) {
      return false;
    }
    if (params.get(NOTE_ID) == null || params.get(NOTE_ID).equals("")) {
      errors.addError(NOTE_ID, "exception.notes.notnull.originalnoteid");
      status = false;
    }
    if (params.get(NOTE_TYPE_ID) == null || params.get(NOTE_TYPE_ID).equals("")) {
      errors.addError(NOTE_TYPE_ID, "exception.notes.notnull.notetypeid");
      status = false;
    }
    if (params.get(PATIENT_ID) == null || params.get(PATIENT_ID).equals("")) {
      errors.addError(PATIENT_ID, "exception.notes.notnull.paientid");
      status = false;
    }
    if (params.get(NOTE_CONTENT) == null || params.get(NOTE_CONTENT).equals("")) {
      errors.addError(NOTE_CONTENT, "exception.notes.notnull.notecontent");
      status = false;
    }
    if (params.get(SAVE_STATUS) == null || params.get(SAVE_STATUS).equals("")) {
      errors.addError(SAVE_STATUS, "exception.notes.notnull.savestatus");
      status = false;
    }
    status = checkBillableConsultation(params, errors) && status;
    return status;
  }

  private boolean checkBillableConsultation(Map<String, Object> params, ValidationErrorMap errors) {
    boolean status = true;
    if (params.get(BILLABLE_CONSULTATION) != null
        && params.get(BILLABLE_CONSULTATION).equals("Y")) {
      if (params.get(ON_BEHALF_DOCTOR_ID) == null || params.get(ON_BEHALF_DOCTOR_ID).equals("")) {
        errors.addError(ON_BEHALF_DOCTOR_ID, "exception.notes.notnull.behalf.doctor");
        status = false;
      }
      if (params.get(CONSULTATION_TYPE_ID) == null) {
        errors.addError(CONSULTATION_TYPE_ID, "exception.notes.notnull.consultation.typeid");
        status = false;
      }
    }
    return status;
  }


  /**
   * Is editable note.
   * 
   * @param noteTypeMap the map
   * @param errMap the error map
   * @return boolean value
   */
  @SuppressWarnings({"unchecked"})
  public boolean isEditableNoteType(Map<String, Object> noteTypeMap, ValidationErrorMap errMap) {
    int roleId = RequestContext.getRoleId();
    String userName = (String) RequestContext.getSession().getAttribute("userId");
    boolean status = true;
    if (noteTypeMap.get("new_note_id") != null && !noteTypeMap.get("new_note_id").equals("")) {
      return false;
    }
    if (roleId != 1 && roleId != 2) {
      int noteTypeId = (int) noteTypeMap.get(NOTE_TYPE_ID);
      BasicDynaBean noteType = noteTypesRepository.findByKey(NOTE_TYPE_ID, noteTypeId);
      if (noteType.get("editable_by").equals("A")
          && !userName.equals(noteTypeMap.get("created_by"))) {
        status = false;
      } else if (noteType.get("editable_by").equals("O")) {
        status = checkUserCanEditThisNote(noteType, userName);
      }
    }
    if (!status && errMap != null) {
      errMap.addError(NOTE_TYPE_ID, "exception.notes.editable.note");
    }
    return status;
  }

  private boolean checkUserCanEditThisNote(BasicDynaBean noteType, String userName) {
    List<Integer> hospRoleIds = new ArrayList<>();
    hospRoleIds.add((Integer) noteType.get("assoc_hosp_role_id"));
    if (noteType.get(TRANSCRIBING_ROLE_ID) != null
        && !noteType.get(TRANSCRIBING_ROLE_ID).equals("")) {
      hospRoleIds.add((Integer) noteType.get(TRANSCRIBING_ROLE_ID));
    }
    List<BasicDynaBean> hospUserList = notesRepository.getEditiableNoteUsers(hospRoleIds, userName);
    return !hospUserList.isEmpty();
  }

}
