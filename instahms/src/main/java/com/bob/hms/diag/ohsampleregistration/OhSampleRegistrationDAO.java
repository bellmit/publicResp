package com.bob.hms.diag.ohsampleregistration;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.diagnosticmodule.common.DiagnosticsPostgresQueryHandler;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class OhSampleRegistrationDAO.
 */
public class OhSampleRegistrationDAO {

  /**
   * Gets the out house details.
   *
   * @return the out house details
   */
  public List getOutHouseDetails() {
    return DataBaseUtil
        .queryToArrayList("" + "SELECT hospital_id as oh_id,hospital_name as oh_name,"
            + "default_rate_plan_id from incoming_hospitals WHERE status='A'");
  }

  /**
   * Gets the incoming tests.
   *
   * @return the incoming tests
   */
  public String getIncomingTests() {
    return DataBaseUtil.getXmlContentWithNoChild(DiagnosticsPostgresQueryHandler.getIncomingTests,
        "TNAMEID");
  }

  /** The Constant GET_TEST_NAMES. */
  private static final String GET_TEST_NAMES = "SELECT TEST_ID,TEST_NAME,DDEPT_ID FROM DIAGNOSTICS "
      + "WHERE HOUSE_STATUS='I' ORDER BY TEST_NAME ASC";

  /**
   * Gets the test names.
   *
   * @return the test names
   * @throws SQLException the SQL exception
   */
  public ArrayList getTestNames() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList list = null;
    con = DataBaseUtil.getConnection();
    ps = con.prepareStatement(GET_TEST_NAMES);
    list = DataBaseUtil.queryToArrayList(ps);
    ps.close();
    con.close();

    return list;
  }

  /** The Constant GET_SALUTIONS. */
  private static final String GET_SALUTIONS = "SELECT salutation_id,salutation"
      + " FROM salutation_master";

  /**
   * Gets the salutions.
   *
   * @return the salutions
   * @throws SQLException the SQL exception
   */
  public List getSalutions() throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    con = DataBaseUtil.getConnection();
    ps = con.prepareStatement(GET_SALUTIONS);

    list = DataBaseUtil.queryToArrayList(ps);

    ps.close();
    con.close();

    return list;
  }

  /** The Constant GET_OH_TESTS. */
  private static final String GET_OH_TESTS = "SELECT osr.mr_no,osr.visit_id,osr.name,osr.test_id,"
      + " osr.oh_name,osr.sample_id,osr.prescribed_id,d.test_name,om.oh_name as outhouse  FROM "
      + " oh_sample_registration osr join diagnostics d on d.test_id = osr.test_id "
      + " join outhouse_master om on om.oh_id = osr.oh_name"
      + " LEFT JOIN tests_prescribed tp on tp.prescribed_id = osr.prescribed_id "
      + " WHERE tp.conducted != 'Y'  ";

  /**
   * Gets the test details.
   *
   * @return the test details
   * @throws SQLException the SQL exception
   */
  public List getTestDetails() throws SQLException {
    List dynaList = null;
    PreparedStatement ps = null;
    Connection con = DataBaseUtil.getConnection();
    ps = con.prepareStatement(GET_OH_TESTS);
    dynaList = DataBaseUtil.queryToArrayList(ps);
    return dynaList;
  }

  /** The Constant GET_INCOMING_CUSTOMER. */
  /*
   * Fetch a retail customer record
   */
  private static final String GET_INCOMING_CUSTOMER = "SELECT ih.*, isr.incoming_visit_id,"
      + " isr.orig_lab_name, isr.patient_name, "
      + " isr.patient_gender, " + " get_patient_age(pd.dateofbirth,pd.expected_dob, "
      + " isr.isr_dateofbirth,isr.patient_age) as patient_age, "
      + " isr.referring_doctor, isr.address, isr.billno, isr.date, isr.category, "
      + " isr.phone_no, isr.center_id, isr.patient_other_info, isr.source_center_id, "
      + " isr.mr_no, isr.incoming_source_type, "
      + " get_patient_age_in(null,null,isr.isr_dateofbirth, isr.age_unit) as age_unit, "
      + " isr.his_visit_id, isr.phone_no_country_code , "
      + " isr.government_identifier , isr.identifier_id, "
      + " COALESCE(d.doctor_name,rf.referal_name) as referral, "
      + " 'A' as visit_status,'ORG0001' as org_id,'in' as visit_type, "
      + " COALESCE(pd.dateofbirth, pd.expected_dob) AS expected_dob, "
      + " get_patient_age(pd.dateofbirth,pd.expected_dob,"
      + " isr.isr_dateofbirth,isr.patient_age)::text " + " as age_text, pd.dateofbirth, "
      + " CASE WHEN COALESCE(pd.patient_gender, isr.patient_gender) = 'M' THEN 'Male'"
      + " WHEN COALESCE(pd.patient_gender, isr.patient_gender) = 'F' THEN 'Female'"
      + " ELSE 'Couple' END AS gender, hcm.center_name "
      + " FROM incoming_sample_registration as isr "
      + " JOIN incoming_hospitals as ih on orig_lab_name=hospital_id "
      + " LEFT JOIN doctors d ON (d.doctor_id = isr.referring_doctor) "
      + " LEFT JOIN referral rf ON (rf.referal_no = isr.referring_doctor) "
      + " LEFT JOIN patient_details pd ON (pd.mr_no = isr.mr_no) "
      + " JOIN hospital_center_master hcm ON (hcm.center_id=isr.center_id) "
      + " WHERE incoming_visit_id = ?";

  /**
   * Gets the incoming customer.
   *
   * @param customerId the customer id
   * @return the incoming customer
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getIncomingCustomer(String customerId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_INCOMING_CUSTOMER);
      ps.setString(1, customerId);
      List list = DataBaseUtil.queryToDynaList(ps);
      BasicDynaBean bean = null;
      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
        return bean;
      } else {
        return null;
      }

    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }

  /**
   * Gets the incoming customer.
   *
   * @param customerId the customer id
   * @return the incoming customer
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getIncomingCustomer(Connection con, String customerId)
      throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(GET_INCOMING_CUSTOMER);
      ps.setString(1, customerId);
      List list = DataBaseUtil.queryToDynaList(ps);
      BasicDynaBean bean = null;
      if (list.size() > 0) {
        bean = (BasicDynaBean) list.get(0);
        return bean;
      } else {
        return null;
      }

    } finally {
      DataBaseUtil.closeConnections(null, ps, rs);
    }
  }

  /** The Constant GET_TESTS_IN_PACKAGE. */
  private static final String GET_TESTS_IN_PACKAGE = "SELECT"
      + " CASE WHEN d.diag_code IS null OR d.diag_code='' THEN d.test_name||'-'||dd.ddept_name "
      + " ELSE d.test_name||'['|| d.diag_code ||']'||'-'||dd.ddept_name END AS test_name,"
      + " d.test_name as test, d.test_id,d.diag_code,d.ddept_id, dd.ddept_name, "
      + " dd.category,st.sample_type_id, st.sample_type,'DIA' as type,"
      + " d.conduction_applicable, "
      + " d.sample_needed, CASE WHEN is_outhouse_test(d.test_id,?) THEN 'O' ELSE 'I' "
      + " END AS house_status, " + " (case when coalesce(d.conducting_doc_mandatory, 'N') = 'O' "
      + " then true else false end) "
      + " as conducting_doc_mandatory, COALESCE(d.mandate_additional_info, 'N')"
      + " as mandate_additional_info, "
      + " COALESCE(d.additional_info_reqts, '') as additional_info_reqts "
      + " FROM package_contents  pcd "
      + " LEFT JOIN diagnostics d on(d.test_id = pcd.activity_id) "
      + " JOIN diagnostics_departments dd ON(dd.ddept_id=d.ddept_id AND d.STATUS='A') "
      + " LEFT JOIN sample_type st ON d.sample_type_id=st.sample_type_id " + " WHERE package_id = ?"
      + " UNION " + " SELECT CASE WHEN d.diag_code IS null OR d.diag_code='' "
      + " THEN d.test_name||'-'||dd.ddept_name "
      + " ELSE d.test_name||'['|| d.diag_code ||']'||'-'||dd.ddept_name END AS test_name,"
      + " d.test_name as test, d.test_id,d.diag_code,d.ddept_id, "
      + " dd.ddept_name, dd.category, st.sample_type_id,"
      + " st.sample_type,'DIA' as type,d.conduction_applicable,d.sample_needed, "
      + " CASE WHEN is_outhouse_test(d.test_id,?) THEN 'O' ELSE 'I' END AS house_status, "
      + "  (case when coalesce(d.conducting_doc_mandatory, 'N') = 'O' "
      + " then true else false end) "
      + " as conducting_doc_mandatory, COALESCE(d.mandate_additional_info, 'N') "
      + " as mandate_additional_info, "
      + " COALESCE(d.additional_info_reqts, '') as additional_info_reqts "
      + " FROM package_contents  pc " + " LEFT JOIN diagnostics d on(d.test_id = pc.activity_id) "
      + " JOIN diagnostics_departments dd ON(dd.ddept_id=d.ddept_id AND d.STATUS='A') "
      + " LEFT JOIN sample_type st ON d.sample_type_id=st.sample_type_id "
      + " WHERE package_id = ?";

  /**
   * Gets the testsin package.
   *
   * @param packageId the package id
   * @param centerId  the center id
   * @return the testsin package
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getTestsinPackage(int packageId, int centerId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_TESTS_IN_PACKAGE);
      ps.setInt(1, centerId);
      ps.setInt(2, packageId);
      ps.setInt(3, centerId);
      ps.setInt(4, packageId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_SOURCE_PRESCRIBED_IDS. */
  private static final String GET_SOURCE_PRESCRIBED_IDS = "SELECT tp.source_test_prescribed_id,"
      + " tp.report_id FROM tests_prescribed tp WHERE tp.report_id = ? AND tp.pat_id = ?"
      + " AND tp.conducted !='X'";

  // Which gives the source prescribed Id's of the report.

  /**
   * Gets the source prescribed IDS.
   *
   * @param con       the con
   * @param reportID  the report ID
   * @param patientID the patient ID
   * @return the source prescribed IDS
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getSourcePrescribedIDS(Connection con, Integer reportID,
      String patientID) throws SQLException {

    PreparedStatement pstmt = null;

    try {
      pstmt = con.prepareStatement(GET_SOURCE_PRESCRIBED_IDS);
      pstmt.setInt(1, reportID.intValue());
      pstmt.setString(2, patientID);
      return DataBaseUtil.queryToDynaList(pstmt);

    } finally {
      DataBaseUtil.closeConnections(null, pstmt);
    }

  }

  /** The Constant IS_INCOMING_TEST. */
  private static final String IS_INCOMING_TEST = "SELECT *"
      + " FROM incoming_sample_registration_details "
      + " WHERE prescribed_id = ?";

  /**
   * Checks if is incoming test.
   *
   * @param con     the con
   * @param prescID the presc ID
   * @return true, if is incoming test
   * @throws SQLException the SQL exception
   */
  public static boolean isIncomingTest(Connection con, Object prescID) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(IS_INCOMING_TEST);
      ps.setObject(1, prescID);
      return DataBaseUtil.queryToDynaBean(ps) != null;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant IS_CENTER_LAB_PATIENT. */
  private static final String IS_CENTER_LAB_PATIENT = "SELECT * FROM incoming_sample_registration "
      + " WHERE mr_no = ?";

  /**
   * Checks if is center lab patient.
   *
   * @param mrNo the mr no
   * @return true, if is center lab patient
   * @throws SQLException the SQL exception
   */
  public static boolean isCenterLabPatient(String mrNo) throws SQLException {

    return DataBaseUtil.queryToDynaBean(IS_CENTER_LAB_PATIENT, mrNo) != null;
  }

  /**
   * Update modified patient details for incoming patient.
   *
   * @param mrNo the mr no
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static boolean updateModifiedPatientDetailsForIncomingPatient(String mrNo)
      throws SQLException, IOException {
    Connection con = null;
    boolean status = true;
    if (isCenterLabPatient(mrNo)) {
      GenericDAO incomingRegDAO = new GenericDAO("incoming_sample_registration");
      BasicDynaBean incomingPatientBean = incomingRegDAO.getBean();
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        BasicDynaBean patientBean = PatientDetailsDAO.getPatientGeneralDetailsBean(con, mrNo);
        incomingPatientBean.set("patient_name", patientBean.get("full_name"));
        incomingPatientBean.set("patient_age", patientBean.get("age"));
        incomingPatientBean.set("age_unit", patientBean.get("agein"));
        incomingPatientBean.set("patient_gender", patientBean.get("patient_gender"));
        incomingPatientBean.set("phone_no", patientBean.get("patient_phone"));
        incomingPatientBean.set("referring_doctor", patientBean.get("reference_docto_id"));

        status = incomingRegDAO.update(con, incomingPatientBean.getMap(), "mr_no", mrNo) > 0;
      } finally {
        DataBaseUtil.commitClose(con, status);
      }
    }
    return status;
  }

  /** The Constant GET_RATE_PLAN. */
  private static final String GET_RATE_PLAN = " SELECT org_name,default_rate_plan_id as org_id "
      + " From incoming_hospitals ih "
      + " JOIN organization_details od ON(ih.default_rate_plan_id = od.org_id) "
      + " where hospital_name=? ";

  /** The Constant GET_ALL_RATE_PLAN. */
  private static final String GET_ALL_RATE_PLAN = " SELECT org_name,org_id"
      + " FROM organization_details WHERE status ='A' ORDER BY org_name ";

  /** The Constant IN_HOSP_RATE_PLAN. */
  private static final String IN_HOSP_RATE_PLAN = "SELECT ih.default_rate_plan_id "
      + "FROM incoming_hospitals ih "
      + "JOIN organization_details od ON(ih.default_rate_plan_id = od.org_id) "
      + "WHERE od.status ='A' AND ih.default_rate_plan_id !='' AND ih.hospital_name =?";

  /**
   * Gets the rate plan names.
   *
   * @param incomingHospital the inchosp
   * @return the rate plan names
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getRatePlanNames(String incomingHospital) throws SQLException {
    List<BasicDynaBean> orgDetBeanList = null;

    String exists = DataBaseUtil.getStringValueFromDb(IN_HOSP_RATE_PLAN, incomingHospital);
    if (exists != null) {
      orgDetBeanList = DataBaseUtil.queryToDynaList(GET_RATE_PLAN, incomingHospital);
    } else {
      orgDetBeanList = DataBaseUtil.queryToDynaList(GET_ALL_RATE_PLAN);
    }

    return orgDetBeanList;
  }

  /** The Constant GET_TEST_DETAILS_LIST. */
  private static final String GET_TEST_DETAILS_LIST = " SELECT dom.outsource_dest_type,"
      + " dod.outsource_dest_id "
      + " FROM diag_outsource_detail dod "
      + " JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
      + " WHERE dod.test_id = ? AND dod.source_center_id = ? AND dod.status = 'A' LIMIT 2 ";

  /**
   * Gets the test details list.
   *
   * @param testId         the test id
   * @param sourceCenterId the source center id
   * @return the test details list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> getTestDetailsList(String testId, int sourceCenterId)
      throws SQLException {

    List list = null;
    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_TEST_DETAILS_LIST);
      ps.setString(1, testId);
      ps.setInt(2, sourceCenterId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant IS_HIS_PATIENTEXIST. */
  private static final String IS_HIS_PATIENTEXIST = "SELECT isr.incoming_visit_id, b.*, tp.labno "
      + " FROM incoming_sample_registration isr " + " LEFT JOIN bill b ON (b.bill_no = isr.billno)"
      + " LEFT JOIN tests_prescribed tp ON (tp.pat_id = isr.incoming_visit_id)"
      + " WHERE his_visit_id = ? LIMIT 1";

  /**
   * Checks if is his patientid exists.
   *
   * @param hisPatientID the his patient ID
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean isHisPatientidExists(String hisPatientID) throws SQLException {

    return DataBaseUtil.queryToDynaBean(IS_HIS_PATIENTEXIST, hisPatientID);
  }

  /**
   * Checks if is sample num exists for patient.
   *
   * @param con       the con
   * @param patientID the patient ID
   * @param sampleNum the sample num
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean isSampleNumExistsForPatient(Connection con, String patientID,
      String sampleNum) throws SQLException {
    PreparedStatement pstmt = null;
    try {
      String sampleDetailsForPatient = "SELECT sample_collection_id FROM sample_collection "
          + "WHERE patient_id = ? AND sample_sno = ? LIMIT 1";
      pstmt = con.prepareStatement(sampleDetailsForPatient);
      pstmt.setString(1, patientID);
      pstmt.setString(2, sampleNum);
      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
  }

}
