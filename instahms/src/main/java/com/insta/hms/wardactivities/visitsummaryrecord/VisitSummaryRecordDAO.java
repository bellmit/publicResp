package com.insta.hms.wardactivities.visitsummaryrecord;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.core.clinical.notes.NotesRepository;
import com.insta.hms.core.emr.Constants;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.mdm.hospitalroles.HospitalRoleRepository;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Class VisitSummaryRecordDAO.
 */
public class VisitSummaryRecordDAO {

  /** The Constant FIND_COMPLAINT. */
  public static final String FIND_COMPLAINT = " SELECT pr.complaint,* FROM patient_registration pr "
      + " WHERE patient_id = ? ";
  
  private static final NotesRepository notesRepository = ApplicationContextProvider
      .getApplicationContext().getBean(NotesRepository.class);
  
  private static final HospitalRoleRepository hospitalRoleRepository = ApplicationContextProvider
      .getApplicationContext().getBean(HospitalRoleRepository.class);

  /**
   * Find complaint.
   *
   * @param patientId the patient id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findComplaint(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(FIND_COMPLAINT);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The emr visit summary record. */
  private static final String EMR_VISIT_SUMMARY_RECORD = " SELECT * FROM patient_registration pr "
      + " JOIN patient_section_details psd ON (psd.patient_id=pr.patient_id AND"
      + " coalesce(psd.section_item_id, 0)=0 AND coalesce(psd.generic_form_id, 0)=0) "
      + " JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id"
      + " AND psf.form_type='Form_IP') "
      + " WHERE pr.visit_type='i'";

  /**
   * Gets the all EMR visit summary record.
   *
   * @param visitId the visit id
   * @param mrNo the mr no
   * @param allVisitsDocs the all visits docs
   * @return the all EMR visit summary record
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List<EMRDoc> getAllEMRVisitSummaryRecord(String visitId, String mrNo,
      boolean allVisitsDocs)
      throws SQLException, IOException {
    List<EMRDoc> emrVisitSummaryRecords = new ArrayList<EMRDoc>();
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getConnection();
      if (allVisitsDocs) {
        ps = con.prepareStatement(EMR_VISIT_SUMMARY_RECORD + " AND pr.mr_no = ? ");
        ps.setString(1, mrNo);
      } else {
        ps = con.prepareStatement(EMR_VISIT_SUMMARY_RECORD + " AND pr.patient_id=?  ");
        ps.setString(1, visitId);
      }
      rs = ps.executeQuery();
      Map recordsMap = new HashMap();
      BasicDynaBean printpref = PrintConfigurationsDAO
          .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
      while (rs.next()) {
        if (!recordsMap.containsValue(rs.getString("patient_id"))) {
          populateEMRDoc(emrVisitSummaryRecords, rs, printpref);
          recordsMap.put("patient_id", rs.getString("patient_id"));
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return emrVisitSummaryRecords;
  }

  /**
   * Populate EMR doc.
   *
   * @param emrVisitSummaryRecords the emr visit summary records
   * @param rs the rs
   * @param printpref the printpref
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void populateEMRDoc(List<EMRDoc> emrVisitSummaryRecords, ResultSet rs,
      BasicDynaBean printpref) throws SQLException, IOException {
    int printerId = (Integer) printpref.get("printer_id");
    String patientId = rs.getString("patient_id");
    EMRDoc dtoObj = new EMRDoc();
    dtoObj.setDocid("");
    dtoObj.setTitle("IP EMR");
    dtoObj.setType("SYS_IP");
    // TODO: this should be Hospital Services, not Rx.
    dtoObj.setDate(rs.getDate("reg_date"));
    dtoObj.setVisitDate(rs.getDate("reg_date"));
    dtoObj.setDoctor("");
    dtoObj.setUpdatedBy("");
    dtoObj.setVisitid(patientId);

    dtoObj.setAuthorized(true);
    dtoObj.setPrinterId(printerId);
    String displayUrl = "";
    displayUrl = "/print/printIpEmr.json?patientId=" + patientId + "&printerId="
        + printerId;

    dtoObj.setPdfSupported(true);
    dtoObj.setDocid(rs.getString("patient_id"));
    dtoObj.setDisplayUrl(displayUrl);
    emrVisitSummaryRecords.add(dtoObj);
    
    setNotesDocuments(emrVisitSummaryRecords, rs, patientId, printerId);

    EMRDoc vitals = new EMRDoc();
    vitals.setDocid("");
    vitals.setTitle("Vitals Chart");
    vitals.setType("SYS_IP");
    vitals.setDate(rs.getDate("reg_date"));
    vitals.setVisitDate(rs.getDate("reg_date"));
    vitals.setDoctor("");
    vitals.setUpdatedBy("");
    vitals.setVisitid(rs.getString("patient_id"));

    vitals.setAuthorized(true);
    vitals.setPrinterId(printerId);
    displayUrl = "/print/printVitalsChart.json?patientId=" + rs.getString("patient_id")
        + "&printerId=" + printerId;

    vitals.setPdfSupported(true);
    vitals.setDocid(rs.getString("patient_id"));
    vitals.setDisplayUrl(displayUrl);
    emrVisitSummaryRecords.add(vitals);

  }
  
  private void setNotesDocuments(List<EMRDoc> emrVisitSummaryRecords,
      ResultSet rs, String patientId, int printerId) throws SQLException {

    Map clinicalPreferencesMap = ApplicationContextProvider.getApplicationContext()
        .getBean(ClinicalPreferencesService.class).getClinicalPreferences().getMap();

    String displayUrl;
    Date regDate = rs.getDate("reg_date");
    if (clinicalPreferencesMap.containsKey("notes_grouping_preference")) {
      String notesGroupingPreference = (String) clinicalPreferencesMap
          .get("notes_grouping_preference");
      switch (notesGroupingPreference) {
        case Constants.NOTE_GROUPING_PREFERENCE_NO_GROUPING:
          EMRDoc notes = new EMRDoc();
          setNotesInfoForEMR(notes, patientId, regDate, printerId);
          notes.setTitle("Notes");
          displayUrl = "/print/printPatientNotes.json?patientId=" + patientId
            + "&printerId=" + printerId;
          notes.setDisplayUrl(displayUrl);
          emrVisitSummaryRecords.add(notes);
          break;
        case Constants.NOTE_GROUPING_PREFERENCE_NOTE_TYPE:
          List<BasicDynaBean> notesByHospitalRolesOrNoteType = notesRepository
              .getNotesByHospitalRolesOrNoteType(patientId);
          if (!notesByHospitalRolesOrNoteType.isEmpty()) {
            for (BasicDynaBean noteTypeMasterBean : notesByHospitalRolesOrNoteType) {
              EMRDoc noteTypeMasterDOC = new EMRDoc();
              setNotesInfoForEMR(noteTypeMasterDOC, patientId, regDate, printerId);
              noteTypeMasterDOC.setTitle((String) noteTypeMasterBean.get("note_type_name"));
              displayUrl = "/print/printPatientNotes.json?patientId=" + patientId
                + "&printerId=" + printerId
                + "&noteTypeId=" + noteTypeMasterBean.get("note_type_id");
              noteTypeMasterDOC.setDisplayUrl(displayUrl);
              emrVisitSummaryRecords.add(noteTypeMasterDOC);
            }
          }
          break;
        case Constants.NOTE_GROUPING_PREFERENCE_HOSPITAL_ROLE:
          List<BasicDynaBean> notesByHospitalRolesOrNoteTypeMaster = notesRepository
              .getNotesByHospitalRolesOrNoteType(patientId);
          if (!notesByHospitalRolesOrNoteTypeMaster.isEmpty()) {
            //To avoid duplicates
            Set<Integer> hospitalRoleIds = new HashSet<>();
            for (BasicDynaBean hospitalRoleBean : notesByHospitalRolesOrNoteTypeMaster) {
              Integer hospitalRoleId = (Integer) hospitalRoleBean.get("hosp_role_id");
              if (!hospitalRoleIds.contains(hospitalRoleId)) {
                hospitalRoleIds.add(hospitalRoleId);
                String noteTitle = hospitalRoleBean.get("hosp_role_name") + " Notes";
                EMRDoc hospitalRoleDoc = new EMRDoc();
                setNotesInfoForEMR(hospitalRoleDoc, patientId, regDate, printerId);
                hospitalRoleDoc.setTitle(noteTitle);
                displayUrl = "/print/printPatientNotes.json?patientId=" + patientId
                  + "&printerId=" + printerId
                  + "&hospitalRoleId=" + hospitalRoleId;
                hospitalRoleDoc.setDisplayUrl(displayUrl);
                emrVisitSummaryRecords.add(hospitalRoleDoc);
              }
            }
          }
          break;
        default:
          break;
      }
    }
  }
  
  private void setNotesInfoForEMR(EMRDoc emrDoc, String patientId, Date regDate, int printerId) {
    emrDoc.setDocid("");
    emrDoc.setType("SYS_IP");
    emrDoc.setDate(regDate);
    emrDoc.setVisitDate(regDate);
    emrDoc.setDoctor("");
    emrDoc.setUpdatedBy("");
    emrDoc.setVisitid(patientId);
    emrDoc.setAuthorized(true);
    emrDoc.setPrinterId(printerId);
    emrDoc.setPdfSupported(true);
    emrDoc.setDocid(patientId);
  }

}
