
package com.insta.hms.discharge;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/** The Class DischargeDAO. */
public class DischargeDAO extends GenericDAO {

  /** Instantiates a new discharge DAO. */
  public DischargeDAO() {
    super("patient_discharge");
  }

  /**
   * Checks if is dischargeable.
   *
   * @param patientId the patient id
   * @return true, if is dischargeable
   * @throws SQLException the SQL exception
   */
  public boolean isDischargeable(String patientId) throws SQLException {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      return new BillDAO(con).getOkToDischarge(patientId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
  
  private static final String GET_INITIATE_DISCHARGE_DETAILS =
      "SELECT pd.initiate_discharge_status, pd.initiate_discharging_doctor,"
          + " pd.expected_discharge_date, pd.expected_discharge_time, "
          + " pd.initiate_discharging_date,"
          + " pd.initiate_discharging_time, pd.initiate_discharge_comments, "
          + " doc.doctor_name, uu.temp_username, "
          + " pds.patient_name, pds.patient_care_oftext, pds.patient_phone, "
          + " pds.relation, pds.email_id"
          + " FROM patient_discharge pd"
          + " JOIN u_user uu ON pd.initiate_entered_by=uu.emp_username"
          + " JOIN patient_details pds ON pds.mr_no= ?"
          + " JOIN doctors doc ON pd.initiate_discharging_doctor=doc.doctor_id "
          + " WHERE pd.patient_id = ? AND "
          + " (patient_confidentiality_check(pds.patient_group,pds.mr_no) )";

  /**
   * Gets the initiate discharge details.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @return the initiate discharge details
   * @throws SQLException the SQL exception
   */
  
  public BasicDynaBean getInitiateDischargeDetails(String mrNo, String patientId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      
      StringBuilder query = new StringBuilder(GET_INITIATE_DISCHARGE_DETAILS);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      return bean;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String GET_FINANCIAL_DISCHARGE_DETAILS =
      "SELECT pd.financial_discharge_status,"
          + " pd.financial_entered_by, uu.temp_username, "
          + " pd.financial_discharge_date,"
          + " pd.financial_discharge_time"
          + " FROM patient_discharge pd"
          + " JOIN patient_registration pr USING (patient_id)"
          + " JOIN patient_details pdd ON (pdd.mr_no = pr.mr_no)"
          + " JOIN u_user uu ON uu.emp_username=pd.financial_entered_by"
          + " WHERE pd.patient_id = ? AND "
          + " (patient_confidentiality_check(pdd.patient_group,pdd.mr_no) )";

  /**
   * Gets the financial discharge details.
   *
   * @param patientId the patient id
   * @return the financial discharge details
   * @throws SQLException the SQL exception
   */
  
  public BasicDynaBean getFinancialDischargeDetails(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      
      StringBuilder query = new StringBuilder(GET_FINANCIAL_DISCHARGE_DETAILS);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String GET_REPORT_FINALIZED_DETAILS =
      "SELECT pr.discharge_finalized_user,"
          + " pr.discharge_finalized_date, "
          + " pr.discharge_finalized_time,"
          + " uu.temp_username "
          + " FROM patient_registration pr "
          + " JOIN patient_details pd USING (mr_no)"
          + " LEFT JOIN u_user uu ON pr.discharge_finalized_user=uu.emp_username"
          + " WHERE pr.patient_id = ? AND "
          + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Gets the report finalized details.
   *
   * @param patientId the patient id
   * @return the report finalized details
   * @throws SQLException the SQL exception
   */
  
  public BasicDynaBean getReportFinalizedDetails(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_REPORT_FINALIZED_DETAILS);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String GET_REPORT_FINALIZED_DETAILS_FOR_PHY_DISC =
      "SELECT pr.discharged_by as user_name, pr.discharge_flag,"
          + " pr.discharge_date, pr.discharge_time,"
          + " uu.temp_username "
          + " FROM patient_registration pr "
          + " JOIN patient_details pd USING (mr_no)"
          + " LEFT JOIN u_user uu ON pr.discharged_by=uu.emp_username"
          + " WHERE pr.patient_id = ? AND "
          + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )";

  /**
   * Gets the physical discharge details.
   *
   * @param patientId the patient id
   * @return the physical discharge details
   * @throws SQLException the SQL exception
   */
  
  public BasicDynaBean getPhysicalDischargeDetails(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_REPORT_FINALIZED_DETAILS_FOR_PHY_DISC);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String GET_CLINICAL_DISCHARGE_DETAILS =
      "SELECT pd.clinical_discharge_flag,"
          + " pd.clinical_entered_by, pd.clinical_discharging_date,"
          + " pd.clinical_discharging_time, "
          + " pd.clinical_discharge_comments, uu.temp_username"
          + " FROM patient_discharge pd"
          + " JOIN patient_registration pr USING (patient_id)"
          + " JOIN patient_details pdd ON (pdd.mr_no = pr.mr_no)"
          + " JOIN u_user uu ON pd.clinical_entered_by = uu.emp_username"
          + " WHERE pd.patient_id = ? AND "
          + " (patient_confidentiality_check(pdd.patient_group,pdd.mr_no) )";

  /**
   * Gets the clinical discharge details.
   *
   * @param patientId the patient id
   * @return the clinical discharge details
   * @throws SQLException the SQL exception
   */
 
  public BasicDynaBean getClinicalDischargeDetails(String patientId) 
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_CLINICAL_DISCHARGE_DETAILS);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  private static final String GET_PATIENT_DISCHARGE_ENTRY =
      "SELECT * FROM patient_discharge" + " WHERE patient_id = ?";

  /**
   * Check if patient discharge entry exists.
   *
   * @param patientId the patient id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  
  public BasicDynaBean checkIfPatientDischargeEntryExists(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder query = new StringBuilder(GET_PATIENT_DISCHARGE_ENTRY);
      ps = con.prepareStatement(query.toString());
      ps.setString(1, patientId);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      return bean;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Insert clinical discharge details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param clinicalDischarge the clinical discharge
   * @param clinicalDischargeUser the clinical discharge user
   * @param clinicalDischargeComments the clinical discharge comments
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  
  public Boolean insertClinicalDischargeDetails(
      Connection con,
      String patientId,
      Boolean clinicalDischarge,
      String clinicalDischargeUser,
      String clinicalDischargeComments)
      throws SQLException, IOException {
    
    BasicDynaBean bean = getBean();
    bean.set("patient_id", patientId);
    bean.set("clinical_discharge_flag", clinicalDischarge);
    bean.set("clinical_entered_by", clinicalDischargeUser);
    bean.set("clinical_discharge_comments", clinicalDischargeComments);
    bean.set("clinical_discharging_date", DateUtil.getCurrentDate());
    bean.set("clinical_discharging_time", DateUtil.getCurrentTime());

    return insert(con, bean);
  }

  /**
   * Update clinical discharge details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param clinicalDischarge the clinical discharge
   * @param clinicalDischargeUser the clinical discharge user
   * @param clinicalDischargeComments the clinical discharge comments
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  
  public Boolean updateClinicalDischargeDetails(
      Connection con,
      String patientId,
      Boolean clinicalDischarge,
      String clinicalDischargeUser,
      String clinicalDischargeComments)
      throws SQLException, IOException {
    Map keys = new HashMap();
    keys.put("patient_id", patientId);
    Map values = new HashMap();
    values.put("clinical_discharge_flag", clinicalDischarge);
    values.put("clinical_entered_by", clinicalDischargeUser);
    values.put("clinical_discharging_date", clinicalDischarge ? DateUtil.getCurrentDate() : null);
    values.put("clinical_discharging_time", clinicalDischarge ? DateUtil.getCurrentTime() : null);
    values.put("clinical_discharge_comments", clinicalDischargeComments);
    return update(con, values, keys) > 0;
  }

  /**
   * Insert initiate discharge details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param initiateDischargeStatus the initiate discharge status
   * @param initiateDischargeDoctor the initiate discharge doctor
   * @param expectedDischargeDate the expected discharge date
   * @param expectedDischargeTime the expected discharge time
   * @param initiateDischargeRemarks the initiate discharge remarks
   * @param initiateDischargeUser the initiate discharge user
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  
  public Boolean insertInitiateDischargeDetails(
      Connection con,
      String patientId,
      Boolean initiateDischargeStatus,
      String initiateDischargeDoctor,
      String expectedDischargeDate,
      String expectedDischargeTime,
      String initiateDischargeRemarks,
      String initiateDischargeUser)
      throws SQLException, IOException, ParseException {

    BasicDynaBean bean = getBean();
    bean.set("patient_id", patientId);
    bean.set("initiate_discharge_status", initiateDischargeStatus);
    bean.set("initiate_discharging_doctor", initiateDischargeDoctor);
    bean.set("initiate_discharging_date", DateUtil.getCurrentDate());
    bean.set("initiate_discharging_time", DateUtil.getCurrentTime());
    bean.set("expected_discharge_date", DataBaseUtil.parseDate(expectedDischargeDate));
    bean.set("expected_discharge_time", DataBaseUtil.parseTime(expectedDischargeTime));
    bean.set("initiate_discharge_comments", initiateDischargeRemarks);
    bean.set("initiate_entered_by", initiateDischargeUser);

    return insert(con, bean);
  }

  /**
   * Update initiate discharge details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param initiateDischargeStatus the initiate discharge status
   * @param initiateDischargeDoctor the initiate discharge doctor
   * @param expectedDischargeDate the expected discharge date
   * @param expectedDischargeTime the expected discharge time
   * @param initiateDischargeRemarks the initiate discharge remarks
   * @param initiateDischargeUser the initiate discharge user
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  
  public Boolean updateInitiateDischargeDetails(
      Connection con,
      String patientId,
      Boolean initiateDischargeStatus,
      String initiateDischargeDoctor,
      String expectedDischargeDate,
      String expectedDischargeTime,
      String initiateDischargeRemarks,
      String initiateDischargeUser)
      throws SQLException, IOException, ParseException {
    Map keys = new HashMap();
    keys.put("patient_id", patientId);
    Map values = new HashMap();
    values.put("initiate_discharge_status", initiateDischargeStatus);
    values.put("initiate_discharging_doctor", initiateDischargeDoctor);
    values.put(
        "initiate_discharging_date", initiateDischargeStatus ? DateUtil.getCurrentDate() : null);
    values.put(
        "initiate_discharging_time", initiateDischargeStatus ? DateUtil.getCurrentTime() : null);
    values.put("expected_discharge_date", DataBaseUtil.parseDate(expectedDischargeDate));
    values.put("expected_discharge_time", DataBaseUtil.parseTime(expectedDischargeTime));
    values.put("initiate_discharge_comments", initiateDischargeRemarks);
    values.put("initiate_entered_by", initiateDischargeUser);
    return update(con, values, keys) > 0;
  }

  /**
   * Insert financial discharge details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param financialDischargeStatus the financial discharge status
   * @param financialDischargeUser the financial discharge user
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  
  public Boolean insertFinancialDischargeDetails(
      Connection con,
      String patientId,
      Boolean financialDischargeStatus,
      String financialDischargeUser)
      throws SQLException, IOException {
    BasicDynaBean bean = getBean();
    bean.set("patient_id", patientId);
    bean.set("financial_discharge_status", financialDischargeStatus);
    bean.set("financial_discharge_date", DateUtil.getCurrentDate());
    bean.set("financial_discharge_time", DateUtil.getCurrentTime());
    bean.set("financial_entered_by", financialDischargeUser);
    return insert(con, bean);
  }

  /**
   * Update financial discharge details.
   *
   * @param con the con
   * @param patientId the patient id
   * @param financialDischargeStatus the financial discharge status
   * @param financialDischargeUser the financial discharge user
   * @return the boolean
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  
  public Boolean updateFinancialDischargeDetails(
      Connection con,
      String patientId,
      Boolean financialDischargeStatus,
      String financialDischargeUser)
      throws SQLException, IOException {
    Map keys = new HashMap();
    keys.put("patient_id", patientId);
    Map values = new HashMap();
    values.put("financial_discharge_status", financialDischargeStatus);
    values.put(
        "financial_discharge_date", financialDischargeStatus ? DateUtil.getCurrentDate() : null);
    values.put(
        "financial_discharge_time", financialDischargeStatus ? DateUtil.getCurrentTime() : null);
    values.put("financial_entered_by", financialDischargeStatus ? financialDischargeUser : null);
    return update(con, values, keys) > 0;
  }
}
