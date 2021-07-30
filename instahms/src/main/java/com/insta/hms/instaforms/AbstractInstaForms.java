/**
 *
 */

package com.insta.hms.instaforms;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.clinical.forms.ClinicalFormHl7Adapter;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.SectionFields.SectionFieldsDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.AllergiesDAO;
import com.insta.hms.outpatient.AntenatalDAO;
import com.insta.hms.outpatient.HealthMaintenanceDAO;
import com.insta.hms.outpatient.ObstetricRecordDAO;
import com.insta.hms.outpatient.PreAnaesthestheticDAO;
import com.insta.hms.outpatient.PregnancyHistoryDAO;
import com.insta.hms.outpatient.PrescriptionBO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.insta.hms.vitalForm.VitalsBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractInstaForms.
 *
 * @author insta
 */
public abstract class AbstractInstaForms {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(AbstractInstaForms.class);

  /** The psd DAO. */
  static PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();

  /** The psf DAO. */
  static PatientSectionFormsDAO psfDAO = new PatientSectionFormsDAO();

  /** The sm DAO. */
  static SectionsDAO smDAO = new SectionsDAO();

  private static final ClinicalFormHl7Adapter clinicalFormHl7Adapter =
      ApplicationContextProvider.getBean(ClinicalFormHl7Adapter.class);

  /**
   * Gets the allergies.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @return the allergies
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List getAllergies(String mrNo, String patientId, int itemId, int genericFormId, int formId)
      throws SQLException, IOException {
    String itemType = (String) getKeys().get("item_type");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      BasicDynaBean record = PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId,
          genericFormId, -2, formId, itemType);
      if (record == null) {
        return AllergiesDAO.getActiveAllergiesForPatient(mrNo);
      } else {
        return AllergiesDAO.getAllergies(mrNo, patientId, itemId, genericFormId, formId, itemType);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the health maintenance records.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @return the health maintenance records
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List getHealthMaintenanceRecords(String mrNo, String patientId, int itemId,
      int genericFormId, int formId) throws SQLException, IOException {
    String itemType = (String) getKeys().get("item_type");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      BasicDynaBean record =
          PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId, -15,
              formId, itemType);
      if (record == null) {
        return HealthMaintenanceDAO.getAllActiveHealthMaintenance(mrNo);
      } else {
        return HealthMaintenanceDAO.getAllHealthMaintenance(mrNo, patientId, itemId, genericFormId,
            formId, itemType);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pregnancy histories.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @return the pregnancy histories
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List getPregnancyHistories(String mrNo, String patientId, int itemId, int genericFormId,
      int formId) throws SQLException, IOException {
    String itemType = (String) getKeys().get("item_type");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      BasicDynaBean record =
          PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId, -13,
              formId, itemType);
      if (record == null) {
        return PregnancyHistoryDAO.getAllActivePregnancyDetails(mrNo);
      } else {
        return PregnancyHistoryDAO.getAllPregnancyDetails(mrNo, patientId, itemId, genericFormId,
            formId, itemType);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the obstetricrecords.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @return the obstetricrecords
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List getObstetricrecords(String mrNo, String patientId, int itemId, int genericFormId,
      int formId) throws SQLException, IOException {
    String itemType = (String) getKeys().get("item_type");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      BasicDynaBean record =
          PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId, -13,
              formId, itemType);
      if (record == null) {
        return ObstetricRecordDAO.getAllActiveObstetricDetails(mrNo);
      } else {
        return ObstetricRecordDAO.getAllObstetricHeadDetails(mrNo, patientId, itemId,
            genericFormId, formId, itemType);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }


  /**
   * Gets the antenatal records.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @return the antenatal records
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List getAntenatalRecords(String mrNo, String patientId, int itemId, int genericFormId,
      int formId) throws SQLException, IOException {
    String itemType = (String) getKeys().get("item_type");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      BasicDynaBean record =
          PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId, -14,
              formId, itemType);
      if (record == null) {
        return AntenatalDAO.getAllActiveAntenatalDetails(mrNo);
      } else {
        return AntenatalDAO.getAllAntenatalDetails(mrNo, patientId, itemId, genericFormId, formId,
            itemType);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the pre anaestesthetic records.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @return the pre anaestesthetic records
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public List getPreAnaestestheticRecords(String mrNo, String patientId, int itemId,
      int genericFormId, int formId) throws SQLException, IOException {
    String itemType = (String) getKeys().get("item_type");
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      BasicDynaBean record =
          PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId, -16,
              formId, itemType);
      if (record == null) {
        return PreAnaesthestheticDAO.getActionPACRecords(mrNo);
      } else {
        return PreAnaesthestheticDAO.getAllPACRecords(mrNo, patientId, itemId, genericFormId,
            formId, itemType);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the section details.
   *
   * @param formId the form id
   * @param formType the form type
   * @param itemType the item type
   * @param sectionId the section id
   * @param linkedTo the linked to
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @return the section details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  /*
   * gets the section data.
   * 
   * patient or visit linked forms ------------------------------ if the section is not saved in the
   * perticular screen, then gets the section data from very recently saved screen. otherwise get's
   * saved data of that perticualr screen.
   * 
   * order item linked forms ------------------------ will be having different set for each
   * perticular screen. prefill condition doesn't apply here.
   */
  public abstract List getSectionDetails(int formId, String formType, String itemType,
      int sectionId, String linkedTo, String mrNo, String patientId, int itemId, int genericFormId)
      throws SQLException, IOException;

  /**
   * Gets the section details.
   *
   * @param formId the form id
   * @param formType the form type
   * @param itemType the item type
   * @param sectionId the section id
   * @param linkedTo the linked to
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param record the record
   * @return the section details
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public abstract List getSectionDetails(int formId, String formType, String itemType,
      int sectionId, String linkedTo, String mrNo, String patientId, int itemId, int genericFormId,
      BasicDynaBean record) throws SQLException, IOException;

  /**
   * Gets the single instance of AbstractInstaForms.
   *
   * @param formType the form type
   * @return single instance of AbstractInstaForms
   */
  public static AbstractInstaForms getInstance(String formType) {
    AbstractInstaForms formBO = null;
    if (formType.equals("Form_CONS") || formType.equals("Form_OP_FOLLOW_UP_CONS")) {
      formBO = new ConsultationForms();
    } else if (formType.equals("Form_TRI")) {
      formBO = new TriageForms();
    } else if (formType.equals("Form_IA")) {
      formBO = new IAForms();
    } else if (formType.equals("Form_IP")) {
      formBO = new IPForms();
    } else if (formType.equals("Form_OT")) {
      formBO = new OTForms();
    } else if (formType.equals("Form_Serv")) {
      formBO = new ServiceForms();
    } else if (formType.equals("Form_Gen")) {
      formBO = new GenericForms();
    }
    return formBO;
  }

  /**
   * Gets the section item id.
   *
   * @param params the params
   * @return the section item id
   */
  /*
   * get's the order item id ex: consultation_id for consultation and prescription_id for services.
   */
  public abstract int getSectionItemId(Map params);

  /**
   * Gets the keys.
   *
   * @return the keys
   */
  public abstract Map getKeys();

  /**
   * Gets the components.
   *
   * @param params the params
   * @return the components
   * @throws SQLException the SQL exception
   */
  /*
   * returns sections to be shown per screen. if the screen is already saved, then details will be
   * returned from the transaction tables, else from master.
   */
  public abstract BasicDynaBean getComponents(Map params) throws SQLException;


  /**
   * Save complaints.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String saveComplaints(Connection con, Map params, String userName, int sectionDetailId,
      boolean insert) throws SQLException, IOException, Exception {
    VisitDetailsDAO visitDAO = new VisitDetailsDAO();
    SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();

    String error = null;
    String complaint = ConversionUtils.getParamValue(params, "complaint", "");
    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    if (!visitDAO.updateComplaint(con, complaint, patientId, userName)) {
      error = "Failed to update Chief Complaint..";
    }
    String[] rowIds = (String[]) params.get("s_complaint_row_id");
    String[] complaintNames = (String[]) params.get("s_complaint");
    Map<String, Object> resultMap =
        scomplaintDao.insert(con, rowIds, complaintNames, patientId, userName);
    if (!(Boolean) resultMap.get("isSuccess")) {
      error = (String) resultMap.get("msg");
    }

    return error;
  }


  /**
   * Save vitals.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String saveVitals(Connection con, Map params, String userName, int sectionDetailId,
      boolean insert) throws SQLException, IOException, Exception {
    return new VitalsBO().updateVitals(con, params, userName) ? null
        : "Failed to update vital details..";
  }

  /**
   * Save diagnosis details.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String saveDiagnosisDetails(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {
    PrescriptionBO pbo = new PrescriptionBO();

    String[] diagnosisIds = (String[]) params.get("diagnosis_id");
    String[] diagnosisCode = (String[]) params.get("diagnosis_code");
    String[] diagnosisDescription = (String[]) params.get("diagnosis_description");
    String[] diagnosisYearofOnset = (String[]) params.get("diagnosis_year_of_onset");
    String[] diagnosisStatusId = (String[]) params.get("diagnosis_status_id");
    String[] diagnosisRemarks = (String[]) params.get("diagnosis_remarks");
    String[] diagType = (String[]) params.get("diagnosis_type");
    String[] diagDelete = (String[]) params.get("diagnosis_deleted");
    String[] diagEdited = (String[]) params.get("diagnosis_edited");
    String[] diagDoctor = (String[]) params.get("diagnosis_doctor_id");
    String[] diagDatetime = (String[]) params.get("diagnosis_datetime");
    String[] diagFavourite = (String[]) params.get("diagnosis_favourite");
    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    String error = null;

    BasicDynaBean regBean =
        new GenericDAO("patient_registration").findByKey(con, "patient_id", patientId);
    String healthAuthority =
        CenterMasterDAO.getHealthAuthorityForCenter((Integer) regBean.get("center_id"));
    String codeType =
        HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority)
            .getDiagnosis_code_type();

    if (diagnosisIds != null) {
      if (diagFavourite == null) {
        for (int i = 0; i < diagnosisIds.length - 1; i++) {
          if (!pbo.updateDiangoisDetails(con, diagnosisIds[i], diagnosisCode[i],
              diagnosisDescription[i], diagnosisYearofOnset[i], diagnosisStatusId[i],
              diagnosisRemarks[i], diagDoctor[i], diagDatetime[i], new Boolean(diagDelete[i]),
              new Boolean(diagEdited[i]), patientId, diagType[i], userName, null, "N", regBean,
              null, codeType)) {
            error = "Failed to save diagnosis details..";
            break;
          }
        }
      } else {
        for (int i = 0; i < diagnosisIds.length - 1; i++) {
          if (!pbo.updateDiangoisDetails(con, diagnosisIds[i], diagnosisCode[i],
              diagnosisDescription[i], diagnosisYearofOnset[i], diagnosisStatusId[i],
              diagnosisRemarks[i], diagDoctor[i], diagDatetime[i], new Boolean(diagDelete[i]),
              new Boolean(diagEdited[i]), patientId, diagType[i], userName, null,
              diagFavourite[i], regBean, null, codeType)) {
            error = "Failed to save diagnosis details..";
            break;
          }
        }
      }
      List primarydiagnosisList = MRDDiagnosisDAO.getPrimaryDiagnosisList(con, patientId);
      if (primarydiagnosisList.size() > 1) {
        error = "Duplicate Primary Diagnosis";
      }
    }
    return error;
  }

  /**
   * Save allergies.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String saveAllergies(Connection con, Map params, String userName, int sectionDetailId,
      boolean insert) throws SQLException, IOException, Exception {
    AllergiesDAO allergyDAO = new AllergiesDAO();

    // insert/update/delete allergies.
    String[] allergyIds = (String[]) params.get("allergy_id");
    String[] allergies = (String[]) params.get("allergy");
    String[] reactions = (String[]) params.get("reaction");
    String[] onsetDates = (String[]) params.get("onset_date");
    String[] severity = (String[]) params.get("severity");
    String[] statuses = (String[]) params.get("status");
    String[] allergyDelete = (String[]) params.get("delAllergy");
    String[] allergyEdited = (String[]) params.get("Allergy_edited");
    String[] allergyTypeIds = (String[]) params.get("allergy_type");
    String[] allergenCodeIds = (String[]) params.get("allergen_code_id");
    String[] genericCodeIds = (String[]) params.get("generic_code");

    String error = null;
    if (allergyIds != null) {
      for (int i = 0; i < allergyIds.length; i++) {

        if (!allergyDAO.updateAllergies(con, allergyIds[i], sectionDetailId, allergyTypeIds[i],
            allergenCodeIds[i], genericCodeIds[i], allergies[i], reactions[i], onsetDates[i],
            severity[i], statuses[i], Boolean.parseBoolean(allergyDelete[i]), Boolean.parseBoolean(
                allergyEdited[i]), userName, insert)) {
          error = "Failed to insert/update the allergies..";
          break;
        }
      }
    }
    return error;
  }

  /**
   * Save consultation notes.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public abstract String saveConsultationNotes(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception;


  /**
   * Save prescription.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public abstract String savePrescription(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception;

  /**
   * Save health maintenance.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String saveHealthMaintenance(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {
    // insert/update.delete patient health maintenance.
    String[] healthMaintIds = (String[]) params.get("health_maint_id");
    String[] doctorsIds = (String[]) params.get("healthMain_doctor_id");
    String[] activities = (String[]) params.get("activity");
    String[] remarks = (String[]) params.get("healthMain_remarks");
    String[] status = (String[]) params.get("healthMain_status");
    String[] recoededDates = (String[]) params.get("recorded_date");
    String[] dueBys = (String[]) params.get("due_by");
    String[] healthMaintDelete = (String[]) params.get("delHealthMaint");
    String[] healthMaintEdited = (String[]) params.get("healthMaint_edited");
    String error = null;

    HealthMaintenanceDAO healMaintDao = new HealthMaintenanceDAO();
    if (healthMaintIds != null) {
      for (int i = 0; i < healthMaintIds.length - 1; i++) {
        if (!healMaintDao.updateHealthMaintenanceDetails(con, healthMaintIds[i], sectionDetailId,
            doctorsIds[i], activities[i], recoededDates[i], dueBys[i], remarks[i], status[i],
            new Boolean(healthMaintDelete[i]), new Boolean(healthMaintEdited[i]), userName,
            insert)) {
          error = "Failed to save the Health Maintenance section details..";
          break;
        }
      }
    }
    return error;
  }

  /**
   * Save pregnancy details.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String savePregnancyDetails(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {
    // insert/update/delete pregnancy histories.
    String[] pregnancyHistoryIds = (String[]) params.get("pregnancy_history_id");
    String[] pregnancyDates = (String[]) params.get("pregnancy_date");
    String[] pregnancyWeeks = (String[]) params.get("pregnancy_weeks");
    String[] pregnancyPalces = (String[]) params.get("pregnancy_place");
    String[] pregnancyMethods = (String[]) params.get("pregnancy_method");
    String[] pregnancyWeights = (String[]) params.get("pregnancy_weight");
    String[] pregnancyGenders = (String[]) params.get("pregnancy_sex");
    String[] pregnancyComplications = (String[]) params.get("pregnancy_complications");
    String[] pregnancyFeedings = (String[]) params.get("pregnancy_feeding");
    String[] pregnancyOutcomes = (String[]) params.get("pregnancy_outcome");
    String[] pregnancyDeleted = (String[]) params.get("pregnancy_deleted");
    String[] pregnancyEdited = (String[]) params.get("pregnancy_edited");
    String[] obstetricRecordIds = (String[]) params.get("obstetric_record_id");

    String[] fieldG = (String[]) params.get("field_g");
    String[] fieldP = (String[]) params.get("field_p");
    String[] fieldL = (String[]) params.get("field_l");
    String[] fieldA = (String[]) params.get("field_a");
    String error = null;

    ObstetricRecordDAO obstetricDao = new ObstetricRecordDAO();
    if (obstetricRecordIds != null) {
      txn: {
        if (!obstetricDao.updateObstetricDetails(con, obstetricRecordIds[0], sectionDetailId,
            fieldG[0], fieldP[0], fieldL[0], fieldA[0], userName, insert)) {
          error = "Failed to save the Obstetric Head section  ..";
          break txn;
        }
      }
    }


    PregnancyHistoryDAO pregnancyDao = new PregnancyHistoryDAO();
    if (pregnancyHistoryIds != null) {
      for (int i = 0; i < pregnancyHistoryIds.length - 1; i++) {
        if (!pregnancyDao.updatePregnancyDetails(con, pregnancyHistoryIds[i], sectionDetailId,
            pregnancyDates[i], pregnancyWeeks[i], pregnancyPalces[i], pregnancyMethods[i],
            pregnancyWeights[i], pregnancyGenders[i], pregnancyComplications[i],
            pregnancyFeedings[i], pregnancyOutcomes[i], new Boolean(pregnancyDeleted[i]),
            new Boolean(pregnancyEdited[i]), userName, insert)) {
          error = "Failed to save the Obstetric History section details..";
          break;
        }
      }
    }
    return error;

  }

  /**
   * Save antenatal records.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String saveAntenatalRecords(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {
    // insert/update/delete pregnancy histories.
    String[] antenatalIds = (String[]) params.get("antenatal_id");
    String[] visitDates = (String[]) params.get("antenatal_visit_date");
    String[] gestationAges = (String[]) params.get("antenatal_gestation_age");
    String[] heightFundus = (String[]) params.get("antenatal_height_fundus");
    String[] presentation = (String[]) params.get("antenatal_presentation");
    String[] weight = (String[]) params.get("antenatal_weight");
    String[] relppBrim = (String[]) params.get("antenatal_rel_pp_brim");
    String[] foetalHeart = (String[]) params.get("antenatal_foetal_heart");
    String[] urine = (String[]) params.get("antenatal_urine");
    String[] sbp = (String[]) params.get("antenatal_systolic_bp");
    String[] dbp = (String[]) params.get("antenatal_diastolic_bp");
    String[] prescriptionSummary = (String[]) params.get("antenatal_prescription_summary");
    String[] doctorIds = (String[]) params.get("antenatal_doctor_id");
    String[] nextVisitDates = (String[]) params.get("antenatal_next_visit_date");
    String[] antenatalDeleted = (String[]) params.get("antenatal_deleted");
    String[] antenatalEdited = (String[]) params.get("antenatal_edited");
    String error = null;

    AntenatalDAO antenatalDao = new AntenatalDAO();
    if (antenatalIds != null) {
      for (int i = 0; i < antenatalIds.length - 1; i++) {

        if (!antenatalDao.updateAntenatalDetails(con, antenatalIds[i], sectionDetailId,
            visitDates[i], gestationAges[i], heightFundus[i], presentation[i], relppBrim[i],
            foetalHeart[i], urine[i], sbp[i], dbp[i], weight[i], prescriptionSummary[i],
            doctorIds[i], nextVisitDates[i], new Boolean(antenatalDeleted[i]), new Boolean(
                antenatalEdited[i]), userName, insert)) {
          error = "Failed to save the Antenatal section details..";
          break;
        }
      }
    }
    return error;

  }

  /**
   * Save pre anaesthesthetic.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public String savePreAnaesthesthetic(Connection con, Map params, String userName,
      int sectionDetailId, boolean insert) throws SQLException, IOException, Exception {
    PreAnaesthestheticDAO preAnaesDao = new PreAnaesthestheticDAO();

    String[] patientPacIds = (String[]) params.get("patient_pac_id");
    String[] statuses = (String[]) params.get("pac_status");
    String[] doctorIds = (String[]) params.get("pac_doctor_id");
    String[] remarks = (String[]) params.get("pac_remarks");
    String[] checkupDates = (String[]) params.get("conducted_date");
    String[] validDates = (String[]) params.get("validity_date");
    String[] deleted = (String[]) params.get("delPreAnaes");
    String[] edited = (String[]) params.get("preAnaes_edited");

    String error = null;
    if (patientPacIds != null) {
      for (int i = 0; i < patientPacIds.length - 1; i++) {
        if (!preAnaesDao.updatePreAnaesthestheticCheckup(con, patientPacIds[i], sectionDetailId,
            doctorIds[i], statuses[i], remarks[i], checkupDates[i], validDates[i], userName,
            new Boolean(deleted[i]), new Boolean(edited[i]), insert)) {
          error = "Failed to save PreAnaestestetic details..";
        }
      }
    }

    return error;
  }

  /**
   * Save.
   *
   * @param con the con
   * @param paramsMap the params map
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  /**
   * This is the main method to be called per screen.
   * which will take care of saving the each and every individual section that is there in screen.
   * ex: allergies, diagnosis details etc.,
   * user can use the above mentioned methods separatly if they want for storing the data.
   */
  public String save(Connection con, Map paramsMap) throws Exception {
    Map params = new HashMap(paramsMap);
    GenericDAO pfdDao = new GenericDAO("patient_form_details");
    BasicDynaBean pfdBean = pfdDao.getBean();
    String error = null;

    String itemType = (String) getKeys().get("item_type");
    String userName = (String) RequestContext.getSession().getAttribute("userId");
    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    String mrNo = ConversionUtils.getParamValue(params, "mr_no", "");
    String formType = (String) getKeys().get("form_type");
    int formId = Integer.parseInt(ConversionUtils.getParamValue(params, "insta_form_id", "0"));
    boolean isFinalizeAll = false;
    if (itemType.equals("GEN")) {
      int genericFormId =
          Integer.parseInt(ConversionUtils.getParamValue(params, "generic_form_id", "0"));
      if (genericFormId == 0) {
        // generate the sequence no for adding the generic insta form for the patient
        genericFormId = DataBaseUtil.getNextSequence(con, "generic_insta_form_seq");
        try {
          paramsMap.put("generic_form_id", new String[] {genericFormId + ""});
        } catch (IllegalStateException ise) {
          log.info("Parameter Map is readonly Map: " + ise.getMessage());
        }
        params.put("generic_form_id", new String[] {genericFormId + ""});
      }
      pfdBean.set("form_detail_id", genericFormId);
      // this sectionstatus field gets data only when closing the consultaton from op list
      pfdBean.set("patient_id", patientId);
      pfdBean.set("mr_no", mrNo);
      pfdBean.set("form_type", formType);
      pfdBean.set("form_master_id", formId);
      pfdBean.set("form_status", "P");
      pfdBean.set("mod_time", DateUtil.getCurrentTimestamp());
      pfdBean.set("user_name", userName);
      Map<String, Object> identifiers = new HashMap<>();
      identifiers.put("form_detail_id", genericFormId);
      identifiers.put("form_type", formType);
      if (pfdDao.findByKey(identifiers) != null) {
        pfdDao.update(con, pfdBean.getMap(), identifiers);
      } else {
        pfdBean.set("created_by", userName);
        if (!pfdDao.insert(con, pfdBean)) {
          return "Failed to insert form in Patient Form Details.";
        }
      }
    } else if (itemType.equals("SUR")) {
      isFinalizeAll = Boolean.parseBoolean(((String[]) params.get("is_finalizeAll"))[0]);
      int operProcedureId =
          Integer.parseInt(ConversionUtils.getParamValue(params, "operation_proc_id", "0"));
      pfdBean.set("form_detail_id", operProcedureId);
      pfdBean.set("patient_id", patientId);
      pfdBean.set("mr_no", mrNo);
      pfdBean.set("form_type", formType);
      pfdBean.set("form_master_id", formId);
      String formStatus = isFinalizeAll ? "F" : "P";
      pfdBean.set("form_status", formStatus);
      pfdBean.set("mod_time", DateUtil.getCurrentTimestamp());
      pfdBean.set("user_name", userName);
      Map<String, Object> identifiers = new HashMap<>();
      identifiers.put("form_detail_id", operProcedureId);
      identifiers.put("form_type", formType);
      BasicDynaBean existingBean = pfdDao.findByKey(identifiers);
      if (existingBean != null) {
        /*
          while updating pfd bean, if new form status changes: 'F'->'P'
          Mark the form as reopened.
        * */
        if ("F".equals(existingBean.get("form_status")) && formStatus.equals("P")) {
          pfdBean.set("is_reopened", true);
        }
        pfdDao.update(con, pfdBean.getMap(), identifiers);
      } else {
        pfdBean.set("created_by", userName);
        if (!pfdDao.insert(con, pfdBean)) {
          return "Failed to insert form in Patient Form Details.";
        }
      }
    }
    Integer itemId = getSectionItemId(params);
    String closeConsWithoutEdit =
        ConversionUtils.getParamValue(params, "close_cons_without_edit", "N");
    int displayOrder = 1;
    BasicDynaBean formbean = getComponents(params);
    String forms = (String) formbean.get("sections");
    for (String sectionIdStr : forms.split(",")) {

      int sectionId = Integer.parseInt(sectionIdStr);
      // for system sections
      if (sectionId < 0) {
        String isFinalized = "N";
        if (sectionId == -2 || sectionId == -14 || sectionId == -5 || sectionId == -15
            || sectionId == -13 || sectionId == -16) {
          String[] isFinalizedParam = (String[]) params.get(sectionIdStr + "_finalized");
          isFinalized = "N";
          if (isFinalizedParam != null && isFinalizedParam[0].equals("Y")) {
            isFinalized = "Y";
          }
        }

        Map resultMap =
            insertOrUpdateSection(con, params, userName, sectionId, isFinalized, displayOrder++);

        if (resultMap.get("error") != null) {
          return (String) resultMap.get("error");
        }
        // when closing the consultation directly from oplist, no other details found anyways, so
        // just continue.
        if (closeConsWithoutEdit.equals("Y")) {
          continue;
        }
        int sectionDetailId = (Integer) resultMap.get("section_detail_id");
        boolean insert = (Boolean) resultMap.get("insert");

        if (sectionId == -1) {
          error = saveComplaints(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -2) {
          error = saveAllergies(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -4) {
          error = saveVitals(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -5) {
          error = saveConsultationNotes(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -6) {
          error = saveDiagnosisDetails(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -7) {
          error = savePrescription(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -13) {
          error = savePregnancyDetails(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -14) {
          error = saveAntenatalRecords(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -15) {
          error = saveHealthMaintenance(con, params, userName, sectionDetailId, insert);
        } else if (sectionId == -16) {
          error = savePreAnaesthesthetic(con, params, userName, sectionDetailId, insert);
        }
        if (error != null) {
          return error;
        }
      } else { // for custom sections
        String[] isFinalizedParam = (String[]) params.get(sectionIdStr + "_finalized");
        String isFinalized = "N";
        if (isFinalizedParam != null && isFinalizedParam[0].equals("Y")) {
          isFinalized = "Y";
        }
        List<String> sectionDetailIds = null;
        if (!closeConsWithoutEdit.equals("Y")) {
          sectionDetailIds =
              new LinkedList<String>(Arrays.asList((String[]) params.get("section_detail_ids_"
                  + sectionIdStr)));
          sectionDetailIds.removeAll(Arrays.asList(""));
        } else {
          sectionDetailIds = new ArrayList<String>();
          sectionDetailIds.add("new");
        }


        List<Integer> updatedSectionDetailIds = new ArrayList<Integer>();
        List<Integer> insertedSectionDetailIds = new ArrayList<Integer>();

        if (sectionDetailIds != null) {
          for (String sectionDetailIdStr : sectionDetailIds) {

            Map resultMap =
                insertOrUpdateInstaSections(con, params, userName, sectionId, sectionDetailIdStr,
                    isFinalized, displayOrder);

            if (resultMap.get("error") != null) {
              return (String) resultMap.get("error");
            }
            int sectionDetailId = (Integer) resultMap.get("section_detail_id");
            boolean insert = (Boolean) resultMap.get("insert");

            if (insert == true) {
              insertedSectionDetailIds.add(sectionDetailId);
            } else {
              updatedSectionDetailIds.add(sectionDetailId);
            }

            // when closing the consultation directly from oplist, no other details found anyways,
            // so just continue.
            if (closeConsWithoutEdit.equals("Y")) {
              continue;
            }
            String oldSectionDetailId = sectionDetailIdStr;
            error =
                saveDynaSections(con, sectionId, params, userName, oldSectionDetailId,
                    sectionDetailId, insert);
            if (error != null) {

              BasicDynaBean smbean = smDAO.findByKey(con, "section_id", sectionId);

              error += " " + smbean.get("section_title");
            }

            if (error != null) {
              return error;
            }
          }
        }
        displayOrder++;


        BasicDynaBean smbean = smDAO.findByKey(con, "section_id", sectionId);
        if (smbean.get("linked_to").equals("patient")) {
          if (sectionDetailIds != null
              && insertedSectionDetailIds.size() == sectionDetailIds.size()) {
            Map updateKeys = new HashMap();
            updateKeys.put("mr_no", mrNo);
            updateKeys.put("section_id", sectionId);
            updateKeys.put("section_status", "A");

            BasicDynaBean statusBean = psdDAO.getBean();
            statusBean.set("section_status", "I");
            if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
              psdDAO.update(con, statusBean.getMap(), updateKeys);
            }
            for (Integer sectionDetailId : insertedSectionDetailIds) {
              updateKeys = new HashMap();
              updateKeys.put("mr_no", mrNo);
              updateKeys.put("section_detail_id", sectionDetailId);
              statusBean = psdDAO.getBean();
              statusBean.set("section_status", "A");
              if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
                psdDAO.update(con, statusBean.getMap(), updateKeys);
              }
            }

          } else if (sectionDetailIds != null
              && updatedSectionDetailIds.size() < sectionDetailIds.size()) {
            Map identifiers = new HashMap();
            identifiers.put("section_detail_id", updatedSectionDetailIds.get(0));
            BasicDynaBean bean = psdDAO.findByKey(identifiers);
            Map updateKeys = null;
            BasicDynaBean statusBean = null;
            for (Integer sectionDetailId : insertedSectionDetailIds) {
              updateKeys = new HashMap();
              statusBean = psdDAO.getBean();
              updateKeys = new HashMap();
              updateKeys.put("mr_no", mrNo);
              updateKeys.put("section_detail_id", sectionDetailId);
              statusBean = psdDAO.getBean();
              statusBean.set("section_status", bean.get("section_status"));
              if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
                psdDAO.update(con, statusBean.getMap(), updateKeys);
              }
            }
          }
        } else if (smbean.get("linked_to").equals("visit")) {
          if (sectionDetailIds != null
              && insertedSectionDetailIds.size() == sectionDetailIds.size()) {
            Map updateKeys = new HashMap();
            updateKeys.put("patient_id", patientId);
            updateKeys.put("section_id", sectionId);
            updateKeys.put("section_status", "A");

            BasicDynaBean statusBean = psdDAO.getBean();
            statusBean.set("section_status", "I");
            if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
              psdDAO.update(con, statusBean.getMap(), updateKeys);
            }
            for (Integer sectionDetailId : insertedSectionDetailIds) {
              updateKeys = new HashMap();
              updateKeys.put("patient_id", patientId);
              updateKeys.put("section_detail_id", sectionDetailId);
              statusBean = psdDAO.getBean();
              statusBean.set("section_status", "A");
              if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
                psdDAO.update(con, statusBean.getMap(), updateKeys);
              }
            }

          } else if (sectionDetailIds != null
              && updatedSectionDetailIds.size() < sectionDetailIds.size()) {
            Map identifiers = new HashMap();
            identifiers.put("section_detail_id", updatedSectionDetailIds.get(0));
            BasicDynaBean bean = psdDAO.findByKey(identifiers);
            Map updateKeys = null;
            BasicDynaBean statusBean = null;
            for (Integer sectionDetailId : insertedSectionDetailIds) {
              updateKeys = new HashMap();
              statusBean = psdDAO.getBean();
              updateKeys = new HashMap();
              updateKeys.put("patient_id", patientId);
              updateKeys.put("section_detail_id", sectionDetailId);
              statusBean = psdDAO.getBean();
              statusBean.set("section_status", bean.get("section_status"));
              if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
                psdDAO.update(con, statusBean.getMap(), updateKeys);
              }
            }
          }
        } else if (smbean.get("linked_to").equals("order item")) {
          if (sectionDetailIds != null
              && insertedSectionDetailIds.size() == sectionDetailIds.size()) {
            Map updateKeys = new HashMap();
            updateKeys.put("section_item_id", itemId);
            updateKeys.put("item_type", itemType);
            updateKeys.put("section_id", sectionId);
            updateKeys.put("section_status", "A");

            BasicDynaBean statusBean = psdDAO.getBean();
            statusBean.set("section_status", "I");
            if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
              psdDAO.update(con, statusBean.getMap(), updateKeys);
            }
            for (Integer sectionDetailId : insertedSectionDetailIds) {
              updateKeys = new HashMap();
              updateKeys.put("section_item_id", itemId);
              updateKeys.put("item_type", itemType);
              updateKeys.put("section_detail_id", sectionDetailId);
              statusBean = psdDAO.getBean();
              statusBean.set("section_status", "A");
              if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
                psdDAO.update(con, statusBean.getMap(), updateKeys);
              }
            }

          } else if (sectionDetailIds != null
              && updatedSectionDetailIds.size() < sectionDetailIds.size()) {
            Map identifiers = new HashMap();
            identifiers.put("section_detail_id", updatedSectionDetailIds.get(0));
            BasicDynaBean bean = psdDAO.findByKey(identifiers);
            Map updateKeys = null;
            BasicDynaBean statusBean = null;
            for (Integer sectionDetailId : insertedSectionDetailIds) {
              updateKeys = new HashMap();
              statusBean = psdDAO.getBean();
              updateKeys = new HashMap();
              updateKeys.put("section_item_id", itemId);
              updateKeys.put("item_type", itemType);
              updateKeys.put("section_detail_id", sectionDetailId);
              statusBean = psdDAO.getBean();
              statusBean.set("section_status", bean.get("section_status"));
              if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null) {
                psdDAO.update(con, statusBean.getMap(), updateKeys);
              }
            }
          }
        }
      }

    }

    if (isFinalizeAll) {
      String operationProcedureId = null;
      if (paramsMap.containsKey("operation_proc_id")) {
        operationProcedureId = ((String[])paramsMap.get("operation_proc_id"))[0];
      }
      String printerIdStr = null;
      if (paramsMap.containsKey("printerId")) {
        printerIdStr = ((String[]) paramsMap.get("printerId"))[0];
      }
      String printTemplateName = null;
      if (paramsMap.containsKey("printTemplate")) {
        printTemplateName = ((String[]) paramsMap.get("printTemplate"))[0];
      }
      clinicalFormHl7Adapter
          .operationFormSaveAndFinaliseEvent(operationProcedureId, patientId, mrNo, printerIdStr,
              printTemplateName);
    }

    return error;
  }

  /**
   * Insert or update insta sections.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionId the section id
   * @param sectionDetailIdStr the section detail id str
   * @param isFinalized the is finalized
   * @param displayOrder the display order
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  private Map insertOrUpdateInstaSections(Connection con, Map params, String userName,
      int sectionId, String sectionDetailIdStr, String isFinalized, int displayOrder)
      throws SQLException, IOException, Exception {
    String formType = (String) getKeys().get("form_type");
    String itemType = (String) getKeys().get("item_type");
    int formId = Integer.parseInt(ConversionUtils.getParamValue(params, "insta_form_id", "0"));
    int genericFormId =
        Integer.parseInt(ConversionUtils.getParamValue(params, "generic_form_id", "0"));
    // this sectionstatus field gets data only when closing the consultaton from op list
    String closeConsWithoutEdit =
        ConversionUtils.getParamValue(params, "close_cons_without_edit", "");

    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    String mrNo = ConversionUtils.getParamValue(params, "mr_no", "");
    Integer itemId = getSectionItemId(params);

    int sectionDetailIdInt = 0;
    boolean flag = false;
    boolean insert = true;
    PreparedStatement ps = null;
    try {
      txn: {
        BasicDynaBean record = null;
        if (!sectionDetailIdStr.contains("new")) {
          int sectionDetailId = Integer.parseInt(sectionDetailIdStr);
          record =
              PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId,
                  sectionId, sectionDetailId, formId, itemType);
        }
        BasicDynaBean sectionbean = psdDAO.getBean();
        sectionbean.set("section_item_id", itemId);
        sectionbean.set("patient_id", patientId);
        sectionbean.set("mr_no", mrNo);
        sectionbean.set("section_id", sectionId);
        sectionbean.set("item_type", itemType);
        sectionbean.set("user_name", userName);
        sectionbean.set("mod_time", DateUtil.getCurrentTimestamp());
        sectionbean.set("generic_form_id", genericFormId);
        sectionbean.set("finalized", isFinalized);
        if (isFinalized != null && isFinalized.equals("Y")) {
          if (record == null || record.get("finalized").equals("N")) {
            sectionbean.set("finalized_user", userName);
          }
        }

        if (record != null) {
          insert = false;
          sectionDetailIdInt = (Integer) record.get("section_detail_id");
          if (psdDAO.update(con, sectionbean.getMap(), "section_detail_id",
              sectionDetailIdInt) == 0) {
            break txn;
          }
        } else {
          sectionDetailIdInt = psdDAO.getNextSequence();
          sectionbean.set("section_detail_id", sectionDetailIdInt);
          sectionbean.set("section_status", closeConsWithoutEdit.equals("Y") ? "I" : "A");
          if (!psdDAO.insert(con, sectionbean)) {
            break txn;
          }
        }

        ps =
            con.prepareStatement(" SELECT section_detail_id FROM patient_section_details psd "
                + " JOIN patient_section_forms psf USING (section_detail_id) "
                + " WHERE psd.mr_no=? AND psd.patient_id=? "
                + " AND coalesce(psd.section_item_id, 0)=? AND coalesce(psd.generic_form_id, 0)=? "
                + " AND psd.item_type=?  AND psd.section_id=? AND psd.section_detail_id = ?"
                + " AND psf.form_id=?");
        ps.setString(1, mrNo);
        ps.setString(2, patientId);
        ps.setInt(3, itemId);
        ps.setInt(4, genericFormId);
        ps.setString(5, itemType);
        ps.setInt(6, sectionId);
        ps.setInt(7, sectionDetailIdInt);
        ps.setInt(8, formId);
        record = DataBaseUtil.queryToDynaBean(ps);

        if (record == null) {
          BasicDynaBean sectionformbean = psfDAO.getBean();
          sectionformbean.set("section_detail_id", sectionDetailIdInt);
          sectionformbean.set("form_id", formId);
          sectionformbean.set("form_type", formType);
          sectionformbean.set("display_order", displayOrder);

          if (!psfDAO.insert(con, sectionformbean)) {
            break txn;
          }
        }
        flag = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    Map map = new HashMap();
    map.put("insert", insert);
    map.put("section_detail_id", sectionDetailIdInt);
    map.put("error", flag ? null : "Failed to insert/update the Patient Section Details..");
    return map;
  }

  /**
   * Insert or update section.
   *
   * @param con the con
   * @param params the params
   * @param userName the user name
   * @param sectionId the section id
   * @param isFinalized the is finalized
   * @param displayOrder the display order
   * @return the map
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  private Map insertOrUpdateSection(Connection con, Map params, String userName, int sectionId,
      String isFinalized, int displayOrder) throws SQLException, IOException, Exception {
    String formType = (String) getKeys().get("form_type");
    String itemType = (String) getKeys().get("item_type");
    int formId = Integer.parseInt(ConversionUtils.getParamValue(params, "insta_form_id", "0"));
    int genericFormId =
        Integer.parseInt(ConversionUtils.getParamValue(params, "generic_form_id", "0"));
    // this sectionstatus field gets data only when closing the consultaton from op list
    String closeConsWithoutEdit =
        ConversionUtils.getParamValue(params, "close_cons_without_edit", "");

    String patientId = ConversionUtils.getParamValue(params, "patient_id", "");
    String mrNo = ConversionUtils.getParamValue(params, "mr_no", "");
    Integer itemId = getSectionItemId(params);

    int sectionDetailIdInt = 0;
    boolean flag = false;
    boolean insert = true;
    PreparedStatement ps = null;
    try {
      txn: {

        BasicDynaBean record =
            PatientSectionDetailsDAO.getRecord(con, mrNo, patientId, itemId, genericFormId,
                sectionId, formId, itemType);

        BasicDynaBean sectionbean = psdDAO.getBean();
        sectionbean.set("section_item_id", itemId);
        sectionbean.set("patient_id", patientId);
        sectionbean.set("mr_no", mrNo);
        sectionbean.set("section_id", sectionId);
        sectionbean.set("item_type", itemType);
        sectionbean.set("user_name", userName);
        sectionbean.set("mod_time", DateUtil.getCurrentTimestamp());
        sectionbean.set("generic_form_id", genericFormId);
        sectionbean.set("finalized", isFinalized);
        if (isFinalized != null && isFinalized.equals("Y")) {
          if (record == null || record.get("finalized").equals("N")) {
            sectionbean.set("finalized_user", userName);
          }
        }

        if (record != null) {
          insert = false;
          sectionDetailIdInt = (Integer) record.get("section_detail_id");
          if (psdDAO.update(con, sectionbean.getMap(), "section_detail_id",
              sectionDetailIdInt) == 0) {
            break txn;
          }
        } else {
          BasicDynaBean smbean = smDAO.findByKey(con, "section_id", sectionId);
          // when closing the consultation without editing do not inactivate the previously entered
          // section data.
          // just inactivate the current section. This way after closing consultation if we go to
          // other screens
          // where the same section exists then it will bring up the data from previous screen where
          // it is actually edited.

          if (sectionId < 0 || smbean.get("linked_to").equals("patient")) {
            Map updateKeys = new HashMap();
            updateKeys.put("mr_no", mrNo);
            updateKeys.put("section_id", sectionId);
            updateKeys.put("section_status", "A");

            BasicDynaBean statusBean = psdDAO.getBean();
            statusBean.set("section_status", "I");

            if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null
                && psdDAO.update(con, statusBean.getMap(), updateKeys) == 0) {
              break txn;
            }

          } else if (sectionId < 0 || smbean.get("linked_to").equals("visit")) {

            Map updateKeys = new HashMap();
            updateKeys.put("patient_id", patientId);
            updateKeys.put("section_id", sectionId);
            updateKeys.put("section_status", "A");

            BasicDynaBean statusBean = psdDAO.getBean();
            statusBean.set("section_status", "I");

            if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null
                && psdDAO.update(con, statusBean.getMap(), updateKeys) == 0) {
              break txn;
            }
          } else if (sectionId < 0 || smbean.get("linked_to").equals("order item")) {

            Map updateKeys = new HashMap();
            updateKeys.put("section_item_id", itemId);
            updateKeys.put("item_type", itemType);
            updateKeys.put("section_id", sectionId);
            updateKeys.put("section_status", "A");

            BasicDynaBean statusBean = psdDAO.getBean();
            statusBean.set("section_status", "I");

            if (!closeConsWithoutEdit.equals("Y") && psdDAO.findByKey(con, updateKeys) != null
                && psdDAO.update(con, statusBean.getMap(), updateKeys) == 0) {
              break txn;
            }
          }

          sectionDetailIdInt = psdDAO.getNextSequence();
          sectionbean.set("section_detail_id", sectionDetailIdInt);
          sectionbean.set("section_status", closeConsWithoutEdit.equals("Y") ? "I" : "A");
          if (!psdDAO.insert(con, sectionbean)) {
            break txn;
          }
        }

        ps =
            con.prepareStatement(" SELECT section_detail_id FROM patient_section_details psd "
                + " JOIN patient_section_forms psf USING (section_detail_id) "
                + " WHERE psd.mr_no=? AND psd.patient_id=? "
                + " AND coalesce(psd.section_item_id, 0)=? "
                + " AND coalesce(psd.generic_form_id, 0)=? "
                + " AND psd.item_type=?  AND psd.section_id=? AND psf.form_id=?");
        ps.setString(1, mrNo);
        ps.setString(2, patientId);
        ps.setInt(3, itemId);
        ps.setInt(4, genericFormId);
        ps.setString(5, itemType);
        ps.setInt(6, sectionId);
        ps.setInt(7, formId);
        record = DataBaseUtil.queryToDynaBean(ps);

        if (record == null) {
          BasicDynaBean sectionformbean = psfDAO.getBean();
          sectionformbean.set("section_detail_id", sectionDetailIdInt);
          sectionformbean.set("form_id", formId);
          sectionformbean.set("form_type", formType);
          sectionformbean.set("display_order", displayOrder);

          if (!psfDAO.insert(con, sectionformbean)) {
            break txn;
          }
        }
        flag = true;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    Map map = new HashMap();
    map.put("insert", insert);
    map.put("section_detail_id", sectionDetailIdInt);
    map.put("error", flag ? null : "Failed to insert/update the Patient Section Details..");
    return map;
  }

  /**
   * Save dyna sections.
   *
   * @param con the con
   * @param sectionId the section id
   * @param params the params
   * @param userName the user name
   * @param oldSectionDetailId the old section detail id
   * @param sectionDetailId the section detail id
   * @param insert the insert
   * @return the string
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  @SuppressWarnings("unchecked")
  public String saveDynaSections(Connection con, int sectionId, Map params, String userName,
      String oldSectionDetailId, int sectionDetailId, boolean insert) throws SQLException,
      IOException, ParseException {
    GenericDAO fieldsDAO = new GenericDAO("patient_section_fields");
    GenericDAO optionsDAO = new GenericDAO("patient_section_options");
    GenericDAO markersDao = new GenericDAO("patient_section_image_details");
    PatientSectionDetailsDAO secDetDAO = new PatientSectionDetailsDAO();

    boolean success = false;

    // delete existing form values
    secDetDAO.deleteMarkers(sectionDetailId);

    // add new form values: all parameters starting with field_
    txn: {
      // Code for updating column available='N' instead of deleting the data from the table
      List<BasicDynaBean> valuesBean = secDetDAO.getSectionValues(con, sectionDetailId);
      ArrayList<String> paramOptionlist = new ArrayList<String>();
      if (null != valuesBean) {

        for (Object paramObj : params.keySet()) {
          String paramField = paramObj.toString();
          if (!paramField.startsWith(oldSectionDetailId + "_field_")) {
            continue;
          }

          if (paramField.startsWith(oldSectionDetailId + "_field_detail_id_")) {
            continue;
          }
          for (String value : (String[]) params.get(paramField)) {
            if (value.equals("")) {
              continue;
            }
            paramOptionlist.add(value);
          }

        }
      }
      String strFieldId = "";
      String strOptionId = "";
      if (null != valuesBean && !valuesBean.isEmpty()) {

        String updateOptionValuesQuery =
            " UPDATE patient_section_options pso SET available ='N' , user_name = ?"
                + " FROM patient_section_fields psf "
                + " WHERE psf.field_detail_id=pso.field_detail_id AND available ='Y'"
                + " AND section_detail_id=?";
        PreparedStatement psSectionValues = null;
        psSectionValues = con.prepareStatement(updateOptionValuesQuery);
        psSectionValues.setString(1, userName);
        psSectionValues.setInt(2, sectionDetailId);
        psSectionValues.executeUpdate();
        psSectionValues.close();

      }

      for (Object paramObj : params.keySet()) {
        String param = paramObj.toString();
        if (!param.startsWith(oldSectionDetailId + "_field_")) {
          continue;
        }
        if (param.startsWith(oldSectionDetailId + "_field_detail_id_")) {
          continue;
        }

        String fieldIdStr = param.substring(param.lastIndexOf("_") + 1);
        int fieldId = Integer.parseInt(fieldIdStr);
        String fieldDetailIdStr =
            ((String[]) params.get(oldSectionDetailId + "_field_detail_id_" + fieldIdStr))[0];
        int fieldDetailId =
            fieldDetailIdStr == null || fieldDetailIdStr.equals("") ? 0 : Integer
                .parseInt(fieldDetailIdStr);

        for (String value : (String[]) params.get(param)) {
          String fieldType = ((String[]) params.get("field_type_" + fieldId))[0];
          if (value.equals("")) {
            if (fieldType.equals("dropdown") && fieldDetailId != 0) {
              String updatedropdownQuery =
                  " UPDATE patient_section_options SET available ='N' , user_name = ?"
                      + " WHERE available ='Y' AND field_detail_id = ?";
              PreparedStatement psdropdown = null;
              psdropdown = con.prepareStatement(updatedropdownQuery);
              psdropdown.setString(1, userName);
              psdropdown.setInt(2, fieldDetailId);
              psdropdown.executeUpdate();
              psdropdown.close();
            }
            continue;
          }
          int optionId = Integer.parseInt(value);
          String optionRemarks = "";
          String fieldRemarks = "";
          String time = "";
          String date = "";
          String available = "Y";
          if (fieldType.equals("checkbox")) {
            optionRemarks =
                ConversionUtils.getParamValue(params, oldSectionDetailId + "_option_remarks_"
                    + fieldId + "_" + optionId, null);
          } else if (fieldType.equals("dropdown")) {
            optionRemarks =
                ConversionUtils.getParamValue(params, oldSectionDetailId + "_option_remarks_"
                    + fieldId, null);
          } else if (fieldType.equals("text") || fieldType.equals("wide text")) {
            fieldRemarks =
                ConversionUtils.getParamValue(params, oldSectionDetailId + "_option_remarks_"
                    + fieldId, null);

          } else if (fieldType.equals("datetime")) {
            date =
                (ConversionUtils.getParamValue(params, oldSectionDetailId + "_date_" + fieldId,
                    null));
            time =
                ConversionUtils
                    .getParamValue(params, oldSectionDetailId + "_time_" + fieldId, null);

          } else if (fieldType.equals("date")) {
            date =
                (ConversionUtils.getParamValue(params, oldSectionDetailId + "_date_" + fieldId,
                    null));
          }
          if (optionId != 0) {
            String updateOptionValuesQuery =
                " UPDATE patient_section_options pso SET available ='N' , user_name = ?"
                    + "  WHERE available ='Y' AND field_detail_id=? AND option_id=0";
            PreparedStatement psSectionValues = null;
            psSectionValues = con.prepareStatement(updateOptionValuesQuery);
            psSectionValues.setString(1, userName);
            psSectionValues.setInt(2, fieldDetailId);
            psSectionValues.executeUpdate();
            psSectionValues.close();
          }
          BasicDynaBean fieldBean = fieldsDAO.getBean();
          fieldBean.set("section_detail_id", sectionDetailId);
          fieldBean.set("field_id", fieldId);
          if (fieldType.equals("date")) {
            fieldBean.set("date", DateUtil.parseDate(date));
          }
          if (fieldType.equals("datetime")) {
            fieldBean.set("date_time", DateUtil.parseTimestamp(date, time));
          }
          fieldBean.set("user_name", userName);
          fieldBean.set("field_remarks", fieldRemarks);
          fieldBean.set("mod_time", DateUtil.getCurrentTimestamp());

          if (fieldDetailId == 0) {
            fieldDetailId = fieldsDAO.getNextSequence();
            fieldBean.set("field_detail_id", fieldDetailId);
            String imageId =
                ConversionUtils.getParamValue(params, oldSectionDetailId + "_image_id_" + fieldId,
                    null);
            imageId = (imageId == null || imageId.equals("")) ? "0" : imageId.trim();
            fieldBean.set("image_id", Integer.parseInt(imageId));

            if (!fieldsDAO.insert(con, fieldBean)) {
              break txn;
            }
          } else {
            if (fieldsDAO.update(con, fieldBean.getMap(), "field_detail_id", fieldDetailId) != 1) {
              break txn;
            }
          }
          if (fieldType.equals("image")) {

            String[] markerTypes =
                (String[]) params.get(oldSectionDetailId + "_marker_id_" + fieldId);
            String[] coordinateX =
                (String[]) params.get(oldSectionDetailId + "_coordinate_x_" + fieldId);
            String[] coordinateY =
                (String[]) params.get(oldSectionDetailId + "_coordinate_y_" + fieldId);
            String[] notes = (String[]) params.get(oldSectionDetailId + "_notes_" + fieldId);
            if (markerTypes != null) {
              for (int j = 0; j < markerTypes.length - 1; j++) {
                BigDecimal coordX = new BigDecimal(coordinateX[j]);
                BigDecimal coordY = new BigDecimal(coordinateY[j]);
                if (coordX.compareTo(BigDecimal.ZERO) != 0
                    || coordY.compareTo(BigDecimal.ZERO) != 0) {
                  BasicDynaBean imageBean = markersDao.getBean();

                  imageBean.set("field_detail_id", fieldDetailId);
                  imageBean.set("marker_detail_id", markersDao.getNextSequence());
                  imageBean.set("coordinate_x", coordX);
                  imageBean.set("coordinate_y", coordY);
                  imageBean.set("marker_id", Integer.parseInt(markerTypes[j]));
                  imageBean.set("notes", notes[j].trim());
                  imageBean.set("user_name", userName);
                  imageBean.set("mod_time", DateUtil.getCurrentTimestamp());
                  // insert the value
                  if (!markersDao.insert(con, imageBean)) {
                    break txn;
                  }
                }

              }
            }
          } else if (fieldType.equals("dropdown") || fieldType.equals("checkbox")) {
            Map keyMap = new HashMap();
            keyMap.put("field_detail_id", fieldDetailId);
            keyMap.put("option_id", optionId);
            BasicDynaBean exists = null;
            for (BasicDynaBean v : valuesBean) {
              if (v.get("section_detail_id").equals(sectionDetailId)
                  && v.get("field_id").equals(fieldId) && v.get("option_id").equals(optionId)
                  && v.get("field_detail_id").equals(fieldDetailId)) {
                exists = v;
                break;
              }
            }

            if (exists != null) {
              Map optionsMap = new HashMap();
              optionsMap.put("field_detail_id", fieldDetailId);
              optionsMap.put("option_id", optionId);
              optionsMap.put("option_remarks", optionRemarks);
              optionsMap.put("user_name", userName);
              optionsMap.put("mod_time", DateUtil.getCurrentTimestamp());
              optionsMap.put("available", available);
              if (optionsDAO.update(con, optionsMap, keyMap) == 0) {
                break txn;
              }
            } else {
              BasicDynaBean optionsBean = optionsDAO.getBean();
              optionsBean.set("option_detail_id", optionsDAO.getNextSequence());
              optionsBean.set("field_detail_id", fieldDetailId);
              optionsBean.set("option_id", optionId);
              optionsBean.set("option_remarks", optionRemarks);
              optionsBean.set("user_name", userName);
              optionsBean.set("mod_time", DateUtil.getCurrentTimestamp());
              optionsBean.set("available", available);
              if (!optionsDAO.insert(con, optionsBean)) {
                break txn;
              }
            }
          }
        }
      }
      String itemType = (String) getKeys().get("item_type");
      if (itemType.equals("CONS")) {
        SectionFieldsDAO fieldDescDAO = new SectionFieldsDAO();
        java.util.List<BasicDynaBean> formOptions = smDAO.getSectionFieldOptions(sectionId);
        Map<String, List<Map>> options =
            ConversionUtils.listBeanToMapListMap(formOptions, "field_id");

        String insertObservations =
            "INSERT INTO mrd_observations (charge_id, observation_type, code,"
                + " value, value_type, value_editable) "
                + " SELECT bac.charge_id, ?, ?, ?, 'Observation', 'Y'"
                + " FROM bill_activity_charge bac "
                + " WHERE bac.activity_id=?::text AND bac.activity_code='DOC'";

        int consultationId =
            Integer.parseInt(ConversionUtils.getParamValue(params, "consultation_id", "0"));
        /*
         * iterate through all the fields of a section and filter the fields which are having
         * observation code and type insert those fields/options values as a observations into mrd
         * observations. these observations will be a copy of a particular section data from very
         * recently modified screens among consultation, triage and initial assessment screens.
         */
        List<BasicDynaBean> fieldDescs = fieldDescDAO.getFields(sectionId, "A");
        for (BasicDynaBean bean : fieldDescs) {
          String obsCode = (String) bean.get("observation_code");
          String obsType = (String) bean.get("observation_type");
          int fieldId = (Integer) bean.get("field_id");

          if (obsCode != null && !obsCode.equals("") && obsType != null && !obsType.equals("")) {

            int obsId = observationIdExist(con, consultationId, obsCode, obsType);
            /*
             * insert the text fields observations here.
             */
            String fieldType = (String) bean.get("field_type");
            String optionRemarks =
                ConversionUtils.getParamValue(params, oldSectionDetailId + "_option_remarks_"
                    + bean.get("field_id"), null);
            if (obsId <= 0) {
              if ((fieldType.equals("text") || fieldType.equals("wide text"))
                  && optionRemarks != null) {
                log.debug("inserting observations for field.." + bean.get("field_name"));
                try (PreparedStatement ps = con.prepareStatement(insertObservations)) {
                  ps.setString(1, obsType);
                  ps.setString(2, obsCode);
                  ps.setString(3, optionRemarks);
                  ps.setInt(4, consultationId);
  
                  if (ps.executeUpdate() != 1) {
                    break txn;
                  }
                }
              }
              // insert the checkbox/dropdown field observations.
              // insert the observations only for the fields which are edited.
              if ((fieldType.equals("checkbox") || fieldType.equals("dropdown"))
                  && (String[]) params.get(oldSectionDetailId + "_field_" + fieldId) != null) {
                for (String value : (String[]) params
                    .get(oldSectionDetailId + "_field_" + fieldId)) {
                  if (value.equals("")) {
                    continue;
                  }
                  log.debug("inserting observations for field options.." + bean.get("field_name"));
                  try (PreparedStatement ps = con.prepareStatement(insertObservations)) {
                    ps.setString(1, obsType);
                    ps.setString(2, obsCode);
                    String code = null;
                    int optionId = Integer.parseInt(value);
                    if (optionId == 0) {
                      code = "Normal";
                    } else if (optionId == -1) {
                      code = "Others";
                    } else {
                      // get the observation code value from master.
                      for (Map map : options.get(fieldId)) {
                        if (((Integer) map.get("option_id")) == optionId) {
                          code = (String) map.get("value_code");
                        }
                      }
                    }
  
                    ps.setString(3, code);
                    ps.setInt(4, consultationId);
  
                    if (ps.executeUpdate() != 1) {
                      break txn;
                    }
                  }
                }
              }
            } else {
              String updateObservations =
                  "UPDATE mrd_observations mo "
                      + " SET (charge_id, observation_type, code, value,"
                      + " value_type, value_editable) "
                      + " = (bac.charge_id, ?, ?, ?, 'Observation', 'Y') "
                      + " FROM  bill_activity_charge bac "
                      + " WHERE  bac.activity_id=?::text AND bac.activity_code='DOC' "
                      + " AND  mo.observation_id = ? ";

              if ((fieldType.equals("text") || fieldType.equals("wide text"))
                  && optionRemarks != null) {
                try (PreparedStatement ps = con.prepareStatement(updateObservations)) {
                  ps.setString(1, obsType);
                  ps.setString(2, obsCode);
                  ps.setString(3, optionRemarks);
                  ps.setInt(4, consultationId);
                  ps.setInt(5, obsId);
                  if (ps.executeUpdate() != 1) {
                    break txn;
                  }
                }
              }

              // delete and insert
              if ((fieldType.equals("checkbox") || fieldType.equals("dropdown"))
                  && (String[]) params.get(oldSectionDetailId + "_field_" + fieldId) != null) {
                // delete
                String deleteObsvervations =
                    "DELETE FROM mrd_observations mo WHERE mo.observation_type = ?"
                        + " AND mo.code = ? "
                        + " AND mo.charge_id IN (SELECT charge_id FROM bill_activity_charge bac "
                        + " WHERE bac.activity_id = ?::text) ";
                try (PreparedStatement ps = con.prepareStatement(deleteObsvervations)) {
                  ps.setString(1, obsType);
                  ps.setString(2, obsCode);
                  ps.setInt(3, consultationId);
                  if (ps.execute()) {
                    break txn;
                  }
                }
                for (String value : (String[]) params
                    .get(oldSectionDetailId + "_field_" + fieldId)) {
                  if (value.equals("")) {
                    continue;
                  }
                  log.debug("inserting observations for field options.." + bean.get("field_name"));
                  try (PreparedStatement ps = con.prepareStatement(insertObservations)) {
                    ps.setString(1, obsType);
                    ps.setString(2, obsCode);
                    String code = null;
                    int optionId = Integer.parseInt(value);
                    if (optionId == 0) {
                      code = "Normal";
                    } else if (optionId == -1) {
                      code = "Others";
                    } else {
                      // get the observation code value from master.
                      for (Map map : options.get(fieldId)) {
                        if (((Integer) map.get("option_id")) == optionId) {
                          code = (String) map.get("value_code");
                        }
                      }
                    }

                    ps.setString(3, code);
                    ps.setInt(4, consultationId);

                    if (ps.executeUpdate() != 1) {
                      break txn;
                    }
                  }
                }

              }
            }
          }
        }
      }
      success = true;
    }
    return success ? null : "Failed to save the Section Details..";
  }

  /**
   * Sort markers of image.
   *
   * @param map the map
   */
  public static void sortMarkersOfImage(Map<Object, List<List>> map) {
    for (Map.Entry<Object, List<List>> entry : map.entrySet()) {
      for (List<BasicDynaBean> list : entry.getValue()) {
        BasicDynaBean bean = (BasicDynaBean) list.get(0);
        if (bean.get("field_type").equals("image")) {
          Collections.sort(list, new Comparator<BasicDynaBean>() {
            public int compare(BasicDynaBean bean, BasicDynaBean ibean) {
              if (bean == null && ibean == null) {
                return 0;
              }

              if (bean != null
                  && ibean != null
                  && ((bean.get("coordinate_x") == null 
                  && ibean.get("coordinate_x") == null) || (bean
                      .get("coordinate_y") == null && ibean.get("coordinate_y") == null))) {
                return 0;
              }

              if (bean == null || bean.get("coordinate_x") == null
                  || bean.get("coordinate_y") == null) {
                return -1;
              }

              if (ibean == null || ibean.get("coordinate_x") == null
                  || ibean.get("coordinate_y") == null) {
                return 1;
              }

              BigDecimal posX = (BigDecimal) bean.get("coordinate_x");
              BigDecimal posY = (BigDecimal) bean.get("coordinate_y");
              BigDecimal iposX = (BigDecimal) ibean.get("coordinate_x");
              BigDecimal iposY = (BigDecimal) ibean.get("coordinate_y");

              if (posX.compareTo(iposX) > 0 && posY.compareTo(iposY) > 0) {
                return 1;
              } else if (posX.compareTo(iposX) > 0 && posY.compareTo(iposY) == 0) {
                return 0;
              } else if (posX.compareTo(iposX) > 0 && posY.compareTo(iposY) < 0) {
                return -1;
              } else if (posX.compareTo(iposX) == 0 && posY.compareTo(iposY) > 0) {
                return 1;
              } else if (posX.compareTo(iposX) == 0 && posY.compareTo(iposY) == 0) {
                return 0;
              } else if (posX.compareTo(iposX) < 0 && posY.compareTo(iposY) < 0) {
                return -1;
              } else if (posX.compareTo(iposX) < 0 && posY.compareTo(iposY) == 0) {
                return 0;
              } else if (posX.compareTo(iposX) < 0 && posY.compareTo(iposY) > 0) {
                return 1;
              }

              return 0;
            }
          });
        }
      }
    }
  }

  /**
   * Observation id exist.
   *
   * @param con the con
   * @param consultationId the consultation id
   * @param obsCode the obs code
   * @param obsType the obs type
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int observationIdExist(Connection con, int consultationId, String obsCode, String obsType)
      throws SQLException {
    String selectObservations =
        "SELECT observation_id FROM mrd_observations ob "
            + " WHERE ob.charge_id=(select charge_id "
            + " from bill_activity_charge bac where bac.activity_id=?::text "
            + " AND bac.activity_code='DOC') AND ob.observation_type=? AND ob.code=?";
    PreparedStatement ps = null;
    ResultSet rs = null;
    int observationId = 0;
    try {
      ps = con.prepareStatement(selectObservations);
      ps.setInt(1, consultationId);
      ps.setString(2, obsType);
      ps.setString(3, obsCode);
      rs = ps.executeQuery();
      if (rs.next()) {
        observationId = rs.getInt("observation_id");
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return observationId;
  }

}
