package com.insta.hms.opthalmology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class OptometristScreenDAO.
 */
public class OptometristScreenDAO {

  /**
   * Gets the eye test list.
   *
   * @return the eye test list
   * @throws SQLException
   *           the SQL exception
   */
  public static List<DynaBean> getEyeTestList() throws SQLException {

    return DataBaseUtil.queryToDynaList("select distinct em.* from eye_test_master em"
        + " JOIN opthal_test_attributes ota on (em.test_id = ota.test_id)");

  }

  /** The Constant PARAMETERS_FIELDS. */
  private static final String PARAMETERS_FIELDS = "SELECT ota.*, am.display_name, am.field_type,"
      + " am.attribute_name, am.field_type, am.default_values " + " FROM opthal_test_attributes ota"
      + " JOIN eye_test_attrib_master am ON (am.attribute_id = ota.attribute_id) "
      + " order by display_order, display_name";

  /**
   * Parameters for eye test.
   *
   * @return the list
   * @throws SQLException
   *           the SQL exception
   */
  public static List parametersForEyeTest() throws SQLException {
    List parameterMap = new ArrayList();
    parameterMap = ConversionUtils
        .copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(PARAMETERS_FIELDS));

    return parameterMap;
  }

  /** The Constant PATIENT_DETAILS_FIELDS. */
  private static final String PATIENT_DETAILS_FIELDS = " SELECT * ";

  /** The Constant PATIENT_DETAILS_QUERY_COUNT. */
  private static final String PATIENT_DETAILS_QUERY_COUNT = " SELECT count(*) ";

  /** The Constant PATIENT_DETAILS_TABLES. */
  private static final String PATIENT_DETAILS_TABLES = "FROM (SELECT otm.opthal_id,"
      + "  dc.consultation_id, dc.mr_no, dc.patient_id,"
      + " get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, "
      + " pd.last_name) as patient_name,d.doctor_name,d.doctor_id, pr.complaint, "
      + " otm.status, case when otm.status='D' then 'Doctor Eye Test Pending' "
      + " when  otm.status='S' then 'Counsellor Session Pending'  when otm.status "
      + " = 'F' then 'Opthalmology Tests Completed' "
      + " when otm.status='C' then 'Closed' ELSE 'Optometrist Test Pending' END as "
      + " status_name,otm.test_notes, dc.presc_date " + " from doctor_consultation dc "
      + " LEFT OUTER JOIN opthal_test_main otm on dc.consultation_id = otm.consult_id "
      + " JOIN doctors d on dc.doctor_name = d.doctor_id "
      + " JOIN patient_details pd on pd.visit_id = dc.patient_id "
      + " JOIN patient_registration pr on pd.mr_no = pr.mr_no AND pr.status = 'A'"
      + " JOIN department dept on d.dept_id = dept.dept_id"
      + "  and UPPER(dept.dept_name) = 'OPHTHALMOLOGY') AS foo ";

  /**
   * Pending patients list.
   *
   * @param filter
   *          the filter
   * @param listing
   *          the listing
   * @return the paged list
   * @throws Exception
   *           the exception
   */
  public static PagedList pendingPatientsList(Map filter, Map listing) throws Exception {
    Connection con = DataBaseUtil.getReadOnlyConnection();

    SearchQueryBuilder qb = new SearchQueryBuilder(con, PATIENT_DETAILS_FIELDS,
        PATIENT_DETAILS_QUERY_COUNT, PATIENT_DETAILS_TABLES, listing);

    if (filter.get("status").equals("")) {
      qb.addFilter(qb.STRING, "status_name", "=", "Optometrist Test Pending");
    } else {
      qb.addFilter(qb.STRING, "status", "=", filter.get("status"));
    }
    qb.addFilter(qb.STRING, "doctor_id", "=", filter.get("doctor_id"));
    qb.addFilter(qb.STRING, "mr_no", "=", filter.get("mr_no"));
    qb.build();
    PagedList list = qb.getMappedPagedList();

    qb.close();
    con.close();

    return list;
  }

  /** The Constant OPTHALMOLOGY_DOCTORS. */
  private static final String OPTHALMOLOGY_DOCTORS = "select doctor_id, doctor_name "
      + " from doctors doc join department dept using (dept_id) "
      + " where UPPER(dept.dept_name) = 'OPHTHALMOLOGY'";

  /**
   * Gets the optha doctors.
   *
   * @return the optha doctors
   * @throws Exception
   *           the exception
   */
  public static ArrayList getOpthaDoctors() throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(OPTHALMOLOGY_DOCTORS);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant DOCTOR_ID_TO_NAME. */
  public static final String DOCTOR_ID_TO_NAME = "select doctor_name from doctors"
      + " where doctor_id = ?";

  /**
   * Gets the doctor from doctor id.
   *
   * @param doctorId
   *          the doctor id
   * @return the doctor from doctor id
   * @throws SQLException
   *           the SQL exception
   */
  public static String getDoctorFromDoctorId(String doctorId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      String doctorIdLocal = doctorId;
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(DOCTOR_ID_TO_NAME);
      ps.setString(1, doctorIdLocal);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant VALUES_TO_PICK. */
  public static final String VALUES_TO_PICK = "select default_values from eye_test_attrib_master"
      + " where display_name in ('Cyl','Spl') "
      + "group by default_values order by max(default_values) desc";

  /**
   * Values to pick.
   *
   * @return the string
   * @throws SQLException
   *           the SQL exception
   */
  public static String valuesToPick() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(VALUES_TO_PICK);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DOCTOR_ID_TO_CONSULTATION_ID. */
  public static final String DOCTOR_ID_TO_CONSULTATION_ID = "SELECT consultation_id "
      + " FROM doctor_consultation WHERE doctor_name = ? AND mr_no = ?";

  /**
   * Gets the consultation id.
   *
   * @param doctorId
   *          the doctor id
   * @param mrNo
   *          the mr no
   * @return the consultation id
   * @throws SQLException
   *           the SQL exception
   */
  public static int getConsultationId(String doctorId, String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(DOCTOR_ID_TO_CONSULTATION_ID);
      pstmt.setString(1, doctorId);
      pstmt.setString(2, mrNo);
      return DataBaseUtil.getIntValueFromDb(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /** The Constant PATIENT_ID_TO_OPTHAL_ID. */
  public static final String PATIENT_ID_TO_OPTHAL_ID = "SELECT opthal_id FROM opthal_test_main "
      + " WHERE patient_id = ?";

  /**
   * Gets the opthal id.
   *
   * @param patientId
   *          the patient id
   * @return the opthal id
   * @throws SQLException
   *           the SQL exception
   */
  public static int getOpthalId(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(PATIENT_ID_TO_OPTHAL_ID);
      pstmt.setString(1, patientId);
      return DataBaseUtil.getIntValueFromDb(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /** The Constant GET_TEST_VALUES. */
  public static final String GET_TEST_VALUES = "SELECT otd.* FROM opthal_test_details otd"
      + " JOIN opthal_test_main otm ON (otd.opthal_id = otm.opthal_id)"
      + " WHERE otm.patient_id = ?";

  /**
   * Gets the test values.
   *
   * @param patientId
   *          the patient id
   * @return the test values
   * @throws SQLException
   *           the SQL exception
   */
  public static List getTestValues(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_TEST_VALUES);
      pstmt.setString(1, patientId);
      return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /** The Constant GET_FIELDS_FOR_EMR. */
  public static final String GET_FIELDS_FOR_EMR = "SELECT d.doctor_name, pr.reg_date, "
      + "pr.patient_id, dc.consultation_id, pd.mr_no, pd.user_name from doctor_consultation dc "
      + " LEFT OUTER JOIN opthal_test_main otm on dc.consultation_id = otm.consult_id "
      + " JOIN doctors d on dc.doctor_name = d.doctor_id "
      + " JOIN patient_details pd on pd.visit_id = dc.patient_id "
      + " JOIN patient_registration pr on pd.mr_no = pr.mr_no AND pr.status = 'A'"
      + " JOIN department dept on d.dept_id = dept.dept_id "
      + " and UPPER(dept.dept_name) = 'OPHTHALMOLOGY' ";

  /**
   * Gets the records.
   *
   * @param visitId
   *          the visit id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocs
   *          the all visits docs
   * @return the records
   * @throws SQLException
   *           the SQL exception
   */
  public static List getRecords(String visitId, String mrNo, boolean allVisitsDocs)
      throws SQLException {
    List<BasicDynaBean> list = null;
    if (allVisitsDocs) {
      list = DataBaseUtil.queryToDynaList(GET_FIELDS_FOR_EMR + " WHERE pd.mr_no = ? ", mrNo);
    } else {
      list = DataBaseUtil.queryToDynaList(GET_FIELDS_FOR_EMR + " WHERE pd.visit_id = ? ", visitId);
    }
    return list;
  }

  /** The Constant GET_FIELDS_FOR_REPORT. */
  public static final String GET_FIELDS_FOR_REPORT = "select om.consult_id, om.test_notes, "
      + " om.status, om.completion_status, em.test_name, eam.attribute_name, otd.test_values, "
      + " em.test_id FROM opthal_test_details otd JOIN eye_test_master em USING(test_id)"
      + " JOIN eye_test_attrib_master eam USING(attribute_id)"
      + " JOIN opthal_test_main om USING(opthal_id) WHERE om.patient_id = ?";

  /**
   * Gets the records for report.
   *
   * @param patientId
   *          the patient id
   * @return the records for report
   * @throws SQLException
   *           the SQL exception
   */
  public static List getRecordsForReport(String patientId) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_FIELDS_FOR_REPORT);
      pstmt.setString(1, patientId);

      return DataBaseUtil.queryToDynaList(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

}