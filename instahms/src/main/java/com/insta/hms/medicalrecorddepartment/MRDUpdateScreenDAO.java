package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * The Class MRDUpdateScreenDAO.
 */
public class MRDUpdateScreenDAO {
  
  private static final GenericDAO mrdObservationsDAO = new GenericDAO("mrd_observations");

  /** The Constant UPDATE_PATIENTDETAILS. */
  private static final String UPDATE_PATIENTDETAILS = "UPDATE patient_details set"
      + " bloodgroup=? where mr_no=?";

  /**
   * Save patient details.
   *
   * @param con
   *          the con
   * @param bloodgrp
   *          the bloodgrp
   * @param mrno
   *          the mrno
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean savePatientDetails(Connection con, String bloodgrp, String mrno)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PATIENTDETAILS)) {
      ps.setString(1, bloodgrp);
      ps.setString(2, mrno);
      return ps.executeUpdate() > 0;
    }
  }

  /** The Constant UPDATE_PATIENTDISCHARGE. */
  private static final String UPDATE_PATIENTDISCHARGE = "UPDATE patient_registration"
      + " set discharge_type=? where patient_id =?";

  /**
   * Save patient discharge.
   *
   * @param con
   *          the con
   * @param referredTo
   *          the referred to
   * @param disType
   *          the dis type
   * @param patientId
   *          the patient id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean savePatientDischarge(Connection con, String referredTo, String disType,
      String patientId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_PATIENTDISCHARGE)) {
      ps.setString(1, disType);
      ps.setString(2, patientId);
      return ps.executeUpdate() > 0;
    }
  }

  /** The Constant FIND_CODES. */
  private static final String FIND_CODES = "SELECT code_type, code, code_desc, "
      + " status FROM getItemCodesForCodeType('*') "
      + " WHERE code_type=? AND code ilike ? AND status='A'";

  /**
   * Find diagnosis codes.
   *
   * @param findString
   *          the find string
   * @param defaultDiagnosisCodeType
   *          the default diagnosis code type
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public static List findDiagnosisCodes(String findString, String defaultDiagnosisCodeType)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(FIND_CODES);
      ps.setString(1, defaultDiagnosisCodeType);
      ps.setString(2, findString + "%");
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant FIELDS_FOR_EMRDOC. */
  private static final String FIELDS_FOR_EMRDOC = "SELECT reg_date, user_name,"
      + " doctor_name, patient_id " + " FROM patient_registration "
      + " LEFT JOIN doctors ON ( doctor = doctor_id)" + " WHERE codification_status = 'V' ";

  /**
   * Fields for emrdoc.
   *
   * @param visitId
   *          the visit id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocs
   *          the all visits docs
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<BasicDynaBean> fieldsForEmrdoc(String visitId, String mrNo,
      boolean allVisitsDocs) throws SQLException {
    if (allVisitsDocs) {
      return DataBaseUtil.queryToDynaList(FIELDS_FOR_EMRDOC + " AND mr_no=? ", mrNo);
    } else {
      return DataBaseUtil.queryToDynaList(FIELDS_FOR_EMRDOC + " AND patient_id=? ", visitId);
    }
  }

  /** The get code desc. */
  public static final String GET_CODE_DESC = "SELECT code_desc FROM mrd_codes_master "
      + " WHERE code = ? AND code_type = ? ";

  /**
   * Gets the code desc.
   *
   * @param code
   *          the code
   * @param codeType
   *          the code type
   * @return the code desc
   * @throws SQLException
   *           the SQL exception
   */
  public static String getCodeDesc(String code, String codeType) throws SQLException {
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(GET_CODE_DESC);
      ps.setString(1, code);
      ps.setString(2, codeType);
      rs = ps.executeQuery();
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /** The get chief complaint. */
  public static final String GET_CHIEF_COMPLAINT = " SELECT pr.complaint "
      + " FROM patient_registration pr WHERE pr.patient_id = ? ";

  /**
   * Gets the chief complaint.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @return the chief complaint
   * @throws SQLException
   *           the SQL exception
   */
  public String getChiefComplaint(Connection con, String patientId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_CHIEF_COMPLAINT)) {
      ps.setString(1, patientId);
      return DataBaseUtil.getStringValueFromDb(ps);
    }
  }

  /** The get secondary complaints. */
  public static final String GET_SECONDARY_COMPLAINTS = " SELECT textcat_linecat(sc.complaint)"
      + " AS sec_complaint FROM secondary_complaints sc " + " WHERE sc.visit_id = ? ";

  /**
   * Gets the secondary complaints.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @return the secondary complaints
   * @throws SQLException
   *           the SQL exception
   */
  public String getSecondaryComplaints(Connection con, String patientId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_SECONDARY_COMPLAINTS)) {
      ps.setString(1, patientId);
      return DataBaseUtil.getStringValueFromDb(ps);
    }
  }

  /** The get consultation field values. */
  public static final String GET_CONSULTATION_FIELD_VALUES = " SELECT textcat_linecat(' '"
      + " || field_name || ' :- ' ||field_value) FROM (SELECT dhtf.field_name,pcfv.field_value "
      + "   FROM patient_consultation_field_values pcfv "
      + "   JOIN doc_hvf_template_fields dhtf ON (dhtf.field_id=pcfv.field_id) "
      + "   JOIN doctor_consultation dc ON (dc.doc_id=pcfv.doc_id "
      + "   AND dhtf.template_id = dc.template_id) "
      + "   WHERE dc.consultation_id = ? AND trim(COALESCE(pcfv.field_value,'')) != '' "
      + "   ORDER BY dhtf.display_order) AS foo";

  /**
   * Gets the consultation field values.
   *
   * @param con
   *          the con
   * @param consultationId
   *          the consultation id
   * @return the consultation field values
   * @throws SQLException
   *           the SQL exception
   */
  public String getConsultationFieldValues(Connection con, int consultationId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_CONSULTATION_FIELD_VALUES)) {
      ps.setInt(1, consultationId);
      return DataBaseUtil.getStringValueFromDb(ps);
    }
  }

  /** The get presenting complaint. */
  public static final String GET_PRESENTING_COMPLAINT = " SELECT ob.* "
      + " FROM mrd_observations ob WHERE ob.charge_id = ? "
      + " AND ob.observation_type='Text' AND ob.code='Presenting-Complaint' "
      + " ORDER BY observation_id LIMIT 1 ";

  /**
   * Update presenting complaint.
   *
   * @param con
   *          the con
   * @param consultationId
   *          the consultation id
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean updatePresentingComplaint(Connection con, int consultationId, String mrNo,
      String patientId) throws SQLException, IOException {

    boolean success = true;

    ChargeDTO chargeDTO = new BillActivityChargeDAO(con).getCharge("DOC", consultationId);

    if (chargeDTO == null) {
      return success;
    }

    // Consultation charge Id.
    String chargeId = chargeDTO.getChargeId();

    if (chargeId == null) {
      return success;
    }

    StringBuilder complaintVal = new StringBuilder();

    String chiefComplaint = getChiefComplaint(con, patientId);
    complaintVal
        .append(chiefComplaint != null && !chiefComplaint.trim().equals("") ? "Chief Complaint :- "
            + chiefComplaint : "");

    String secComplaints = getSecondaryComplaints(con, patientId);
    complaintVal
        .append(secComplaints != null && !secComplaints.trim().equals("") ? " Other Complaints :- "
            + secComplaints : "");

    String consFieldValues = getConsultationFieldValues(con, consultationId);
    complaintVal
        .append(consFieldValues != null && !consFieldValues.trim().equals("") ? consFieldValues
            : "");

    PatientSectionDetailsDAO pfdDAO = new PatientSectionDetailsDAO();
    String consSectionFieldValues = pfdDAO.getConsInstaSectionFieldValues(con, consultationId);
    complaintVal.append(consSectionFieldValues != null 
        && !consSectionFieldValues.trim().equals("") ? consSectionFieldValues
            : "");

    BasicDynaBean existingbean = DataBaseUtil.queryToDynaBean(GET_PRESENTING_COMPLAINT, chargeId);

    if (existingbean != null) {

      existingbean.set("value", complaintVal.toString());
      success =
          (mrdObservationsDAO.updateWithName(con, existingbean.getMap(), "observation_id") > 0);
    } else {
      success = insertPresentingComplaint(con, chargeId, complaintVal.toString());
    }
    return success;
  }

  /**
   * Insert presenting complaint.
   *
   * @param con
   *          the con
   * @param chargeId
   *          the charge id
   * @param complaintVal
   *          the complaint val
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean insertPresentingComplaint(Connection con, String chargeId, String complaintVal)
      throws SQLException, IOException {
    BasicDynaBean mrdObsbean = mrdObservationsDAO.getBean();

    mrdObsbean.set("observation_id",
        DataBaseUtil.getNextSequence(con, "mrd_observations_observation_id_seq"));
    mrdObsbean.set("charge_id", chargeId);
    mrdObsbean.set("observation_type", "Text");
    mrdObsbean.set("code", "Presenting-Complaint");
    mrdObsbean.set("value", complaintVal);
    mrdObsbean.set("value_type", "Presenting-Complaint");
    mrdObsbean.set("value_editable", "Y");
    return mrdObservationsDAO.insert(con, mrdObsbean);
  }

  /** The get open inprogress reviews. */
  public static final String GET_OPEN_INPROGRESS_REVIEWS = "SELECT count(id) ticket_count"
      + " FROM reviews t"
      + " WHERE t.patient_id = ? AND status IN ('open','inprogress') ";

  /**
   * Gets the open inprogress reviews count.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @return the open inprogress reviews count
   * @throws SQLException
   *           the SQL exception
   */
  public Integer getOpenInprogressReviewsCount(Connection con, String patientId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_OPEN_INPROGRESS_REVIEWS)) {
      ps.setString(1, patientId);
      return Integer.parseInt(String.valueOf(DataBaseUtil.queryToDynaBean(ps).get("ticket_count")));
    }
  }

}
