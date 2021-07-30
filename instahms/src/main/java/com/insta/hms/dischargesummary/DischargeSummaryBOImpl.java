package com.insta.hms.dischargesummary;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.ipservices.IPBedDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.IPPreferences.IPPreferencesDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.medicalrecorddepartment.MRDCaseFileIssueDAO;
import com.insta.hms.orders.OrderBO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DischargeSummaryBOImpl.
 */
public class DischargeSummaryBOImpl {

  /** The logger. */
  static Logger logger =
      LoggerFactory.getLogger(DischargeSummaryBOImpl.class);
  
  /** The dao sum. */
  DischargeSummaryDAOImpl daoSum = new DischargeSummaryDAOImpl();
  
  private static final GenericDAO ipBedDetailsDAO = new GenericDAO("ip_bed_details");

  /**
   * Medical record details.
   *
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String medicalRecordDetails() throws SQLException {

    String bedName = daoSum.medicalRecordDetails();
    return bedName;
  }

  /**
   * Gets the discharge patient details.
   *
   * @param patientId the patient Id
   * @return the discharge patient details
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public ArrayList getDischargePatientDetails(String patientId) 
      throws SQLException, ParseException {

    ArrayList arrDischargePatientDetails = daoSum.getDischargePatientDetails(patientId);

    return arrDischargePatientDetails;
  }

  /**
   * Gets the doc type.
   *
   * @return the doc type
   * @throws SQLException the SQL exception
   */
  public String getDocType() throws SQLException {

    String docType = daoSum.getDocType();
    return docType;
  }

  /**
   * Fetch doc id.
   *
   * @param mrNo the mr no
   * @param patientID the patient ID
   * @param formId the form id
   * @return the string
   * @throws SQLException the SQL exception
   */
  public String fetchDocId(String mrNo, String patientID, String formId) throws SQLException {
    String docId = daoSum.fetchDocId(mrNo, patientID, formId);
    return docId;
  }

  /**
   * Gets the form fields from database.
   *
   * @param formId the form Id
   * @return the form fields from database
   * @throws SQLException the SQL exception
   */
  public ArrayList getFormFieldsFromDatabase(String formId) throws SQLException {
    ArrayList arrFormFields = daoSum.getFormFieldsFromDatabase(formId);
    return arrFormFields;
  }

  /**
   * Gets the form caption.
   *
   * @param templateType the template type
   * @return the form caption
   * @throws SQLException the SQL exception
   */
  public ArrayList getFormCaption(String templateType) throws SQLException {
    ArrayList arrFormCaption = daoSum.getFormCaption(templateType);
    return arrFormCaption;
  }

  /**
   * Gets the form fields values from database.
   *
   * @param docid the docid
   * @return the form fields values from database
   * @throws SQLException the SQL exception
   */
  public List getFormFieldsValuesFromDatabase(int docid) throws SQLException {
    return daoSum.getFormFieldsValuesFromDatabase(docid);
  }

  /**
   * Gets the status closed status.
   *
   * @param mrNo the mr no
   * @param patId the pat id
   * @return the status closed status
   * @throws SQLException the SQL exception
   */
  public String getStatusClosedStatus(String mrNo, String patId) throws SQLException {
    String closeStatus = daoSum.getStatusClosedStatus(mrNo, patId);
    return closeStatus;
  }

  /**
   * Gets the discharge status.
   *
   * @param patId the pat id
   * @return the discharge status
   * @throws SQLException the SQL exception
   */
  public String getDischargeStatus(String patId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String status = "";
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps =
          con.prepareStatement(
              "select discharge_flag from patient_registration where patient_id=?");
      ps.setString(1, patId);
      rs = ps.executeQuery();
      while (rs.next()) {
        status = rs.getString("discharge_flag");
      }
    } catch (SQLException se) {
      throw se;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return status;
  }

  /**
   * will be part of a transaction updates patient_registration table status--->I and
   * discharge_flag---->D updates case file status ----->P finalizes occupied beds releases occupied
   * beds.
   *
   * @param con the con
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param userName the user name
   * @param disDate the dis date
   * @param disTime the dis time
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean dischargePatient(
      Connection con,
      String mrNo,
      String patientId,
      String userName,
      String disDate,
      String disTime)
      throws Exception {
    return dischargePatient(con, mrNo, patientId, userName, disDate, disTime, null, null);
  }

  /**
   * Discharge patient.
   *
   * @param con the con
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param userName the user name
   * @param disDate the dis date
   * @param disTime the dis time
   * @param patientRegistration the patient registration
   * @param patientDetailsBean the patient details bean
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean dischargePatient(
      Connection con,
      String mrNo,
      String patientId,
      String userName,
      String disDate,
      String disTime,
      BasicDynaBean patientRegistration,
      BasicDynaBean patientDetailsBean)
      throws Exception {
    boolean result = true;
    if (patientRegistration == null) {
      patientRegistration =
          new GenericDAO("patient_registration").findByKey(con, "patient_id", patientId);
    }

    patientRegistration.set("user_name", userName);
    patientRegistration.set("status", "I");
    patientRegistration.set("discharge_flag", "D");
    patientRegistration.set("patient_discharge_status", "D");
    patientRegistration.set("ready_to_discharge", "Y");
    if (disDate != null && disTime != null && !disDate.equals("") && !disTime.equals("")) {
      patientRegistration.set("discharge_date", DataBaseUtil.parseDate(disDate));
      patientRegistration.set("discharge_time", DataBaseUtil.parseTime(disTime));
      patientRegistration.set("discharged_by", userName);
    }
    //Finalize occupied beds.
    BasicDynaBean prefs = new IPPreferencesDAO().getPreferences();
    List prevBedDetails = ipBedDetailsDAO.listAll(null, "patient_id", patientId);
    BasicDynaBean bedBean = null;
    if (prevBedDetails != null && prevBedDetails.size() > 0) {
      Bill bill = BillDAO.getVisitCreditBill(patientId, true);
      for (int i = 0; i < prevBedDetails.size(); i++) {
        bedBean = (BasicDynaBean) prevBedDetails.get(i);
        if (!bedBean.get("bed_state").equals("F")) {
          if (bill == null) {
            logger.error("No credit bill, and active beds exist, cannot close visit");
            return false; // no credit bill and bill is not finalized. error.
          }
          OrderBO orderBO = new OrderBO();
          String err = orderBO.setBillInfo(con, patientId, bill.getBillNo(), false, userName);
          if (err != null) {
            logger.error(err);
            return false;
          }
          result &=
              (orderBO.finalizeBed(
                      con,
                      bedBean,
                      DateUtil.getCurrentTimestamp(),
                      true,
                      bedBean.get("status").equals("R"))
                  == null);
          break;
        }
      }
      for (int g = 0; g < prevBedDetails.size(); g++) {
        bedBean = (BasicDynaBean) prevBedDetails.get(g);
        if (!bedBean.get("status").equals("X")) {
          Map values = new HashMap();
          values.put("bed_state", "F");
          values.put("status", "P");

          if (!bedBean
              .get("bed_state")
              .equals("F")) { //update end_date only for not finalized beds.
            values.put("end_date", DateUtil.getCurrentTimestamp());
            values.put("UPDATED_DATE", DateUtil.getCurrentTimestamp());
          }

          ipBedDetailsDAO.update(con, values, "admit_id", (Integer) bedBean.get("admit_id"));
        }
      }

      for (int i = 0; i < prevBedDetails.size(); i++) {
        bedBean = (BasicDynaBean) prevBedDetails.get(i);
        BasicDynaBean bedTypeBean =
            BedMasterDAO.getBedDetailsBean(con, (Integer) bedBean.get("bed_id"));
        if (!bedBean.get("bed_state").equals("F")) {
          // bug#16698
          if (bedTypeBean.get("bed_ref_id") == null) {
            result &=
                new IPBedDAO()
                    .updateChildBeds(
                        con,
                        (String) bedBean.get("patient_id"),
                        (Integer) bedBean.get("bed_id"),
                        "N");
          } else {
            result &=
                new IPBedDAO().updateParentBed(con, (Integer) bedTypeBean.get("bed_ref_id"), "N");
          }
        }
      }
    }

    //updating mrd case files
    result &=
        MRDCaseFileIssueDAO.setMRDCaseFileStatus(
            con, mrNo, MRDCaseFileIssueDAO.MRD_CASE_FILE_STATUS_ON_DISCHARGE);

    //possible when when discharged from EditVisitDetails screen i.e,Force Discharge
    if (patientRegistration.get("discharge_type_id") == null) {
      patientRegistration.set("discharge_type_id", 1);
    }
      

    //updating the patient_registration table
    result = new VisitDetailsDAO().updatePatientRegistration(con, patientRegistration);

    if (patientDetailsBean
        != null) { //update death reason and death date ,can do only from discharge screen
      patientDetailsBean.set("visit_id", null);
      patientDetailsBean.set("previous_visit_id", patientId);
      result = new PatientDetailsDAO().updatePatientDetails(con, patientDetailsBean);
    }
    return result;
  }

  /**
   * Gets the doctor details.
   *
   * @param doctorId the doctor id
   * @return the doctor details
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getDoctorDetails(String doctorId) throws SQLException {
    Map<String, String> doctorFilterMap = new HashMap<String, String>();
    doctorFilterMap.put("doctor_id", doctorId);
    List<String> doctorDetailsColumns = new ArrayList<String>();
    doctorDetailsColumns.add("doctor_mail_id");
    doctorDetailsColumns.add("doctor_mobile");
    doctorDetailsColumns.add("doctor_name");
    List<BasicDynaBean> doctorDetailsList =
        new DoctorMasterDAO().listAll(doctorDetailsColumns, doctorFilterMap, null);
    BasicDynaBean doctorDetails = null;
    if (doctorDetailsList.size() != 0) {
      doctorDetails = doctorDetailsList.get(0);
    }
    return doctorDetails;
  }

  /**
   * Gets the physical discharge tokens.
   *
   * @param patientDetailsMap the patient details map
   * @return the physical discharge tokens
   * @throws SQLException the SQL exception
   */
  public HashMap getPhysicalDischargeTokens(Map<String, Object> patientDetailsMap)
      throws SQLException {

    Map<String, String> referralFilterMap = new HashMap<String, String>();
    HashMap<String, Object> physicalDischargeData = new HashMap<String, Object>();
    String doctor = (String) patientDetailsMap.get("discharge_doctor_id");
    BasicDynaBean doctorDetails = getDoctorDetails(doctor);
    String doctorMobile = null;
    String doctorMail = null;
    String referalDoctor = (String) patientDetailsMap.get("referral_doctor_id");
    if (referalDoctor != null) {
      referralFilterMap.put("referal_no", referalDoctor);
      List<String> referralDetailsColumns = new ArrayList<String>();
      referralDetailsColumns.add("referal_doctor_email");
      referralDetailsColumns.add("referal_doctor_phone");
      referralDetailsColumns.add("referal_name");
      List<BasicDynaBean> referralList =
          new ReferalDoctorDAO().listAll(referralDetailsColumns, referralFilterMap, null);
      if (referralList.size() != 0) {
        BasicDynaBean referralDetails = referralList.get(0);
        physicalDischargeData.put(
            "referral_mobile", (String) referralDetails.get("referal_doctor_phone"));
        physicalDischargeData.put(
            "referral_email", (String) referralDetails.get("referal_doctor_email"));
      } else {
        BasicDynaBean referalDoctorDetails = getDoctorDetails(referalDoctor);
        if (referalDoctorDetails != null) {
          physicalDischargeData.put(
              "referral_mobile", (String) referalDoctorDetails.get("doctor_mobile"));
          physicalDischargeData.put(
              "referral_email", (String) referalDoctorDetails.get("doctor_mail_id"));
        }
      }
    }
    if (doctorDetails != null) {
      doctorMobile = (String) doctorDetails.get("doctor_mobile");
      doctorMail = (String) doctorDetails.get("doctor_mail_id");
      physicalDischargeData.put("doctor_name", (String) doctorDetails.get("doctor_name"));
    }
    String referralMobile = (String) physicalDischargeData.get("referral_mobile");
    String referralMail = (String) physicalDischargeData.get("referral_email");

    if (doctorMobile == null) {
      if (referralMobile != null) {
        doctorMobile = referralMobile;
      }
    } else {
      if (referralMobile != null) {
        doctorMobile = doctorMobile + "," + referralMobile;
      }
    }
    if (doctorMail == null) {
      if (referralMail != null) {
        doctorMail = referralMail;
      }
    } else {
      if (referralMail != null) {
        doctorMail = doctorMail + "," + referralMail;
      }
    }
    physicalDischargeData.putAll(patientDetailsMap);
    physicalDischargeData.put("doctor_mobile", doctorMobile);
    physicalDischargeData.put("doctor_mail", doctorMail);
    physicalDischargeData.put("discharge_status", "P");

    return physicalDischargeData;
  }
}
