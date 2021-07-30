package com.insta.hms.medicalrecorddepartment;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class MRDDiagnosisDAO.
 *
 * @author krishna.t
 */
public class MRDDiagnosisDAO extends GenericDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(MRDDiagnosisDAO.class);

  /**
   * Instantiates a new MRD diagnosis DAO.
   */
  public MRDDiagnosisDAO() {
    super("mrd_diagnosis");
  }

  /** The Constant GET_PRIMARY_DIAGNOSIS_DETAILS. */
  private static final String GET_PRIMARY_DIAGNOSIS_DETAILS = "SELECT visit_id, id, "
      + " description, icd_code, code_type, diag_type, username, mod_time FROM mrd_diagnosis"
      + " WHERE visit_id=? and diag_type='P' ORDER BY id";

  /**
   * Gets the primary diagnosis.
   *
   * @param patientId
   *          the patient id
   * @return the primary diagnosis
   * @throws SQLException
   *           the SQL exception
   */
  // Diag codes are independent for each visit.
  public static BasicDynaBean getPrimaryDiagnosis(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PRIMARY_DIAGNOSIS_DETAILS);
      ps.setString(1, patientId);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && !list.isEmpty()) {
        return (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return null;
  }

  private static final String GET_DIAG_WHERE_VISITS = "SELECT visit_id, id, description, "
      + " icd_code, code_type, year_of_onset, present_on_admission, "
      + " diag_type, username, mod_time, remarks FROM mrd_diagnosis WHERE visit_id IN (:visitIds)";

  /**
   * Returns diagnoses that match a given list of visit Ids.
   *
   * @param visitIds
   *          the list of visit ids
   * @return the list of mrd_diagnosis rows
   */
  public static List<BasicDynaBean> getListWhereVisit(List<String> visitIds) {
    if (visitIds.isEmpty()) {
      return new ArrayList<>();
    }
    MapSqlParameterSource params = new MapSqlParameterSource();
    StringBuilder query = new StringBuilder(GET_DIAG_WHERE_VISITS);
    params.addValue("visitIds", visitIds);
    return DatabaseHelper.queryToDynaList(query.toString(), params);
  }

  /** The Constant GET_DIAGNOSIS_DETAILS. */
  // Diag codes are independent for each visit.
  private static final String GET_DIAGNOSIS_DETAILS = "SELECT visit_id, id, description, icd_code, "
      + " code_type, diag_type, username, mod_time FROM mrd_diagnosis WHERE visit_id=? "
      + " and diag_type='S' ORDER BY id";

  /**
   * Gets the diagnosis details.
   *
   * @param patientId
   *          the patient id
   * @return the diagnosis details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getDiagnosisDetails(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_DIAGNOSIS_DETAILS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  // returns the primary diagnosis details as first record.
  /** The Constant GET_ALL_DIAGNOSIS_DETAILS. */
  // Diag codes are independent for each visit.
  private static final String GET_ALL_DIAGNOSIS_DETAILS = "SELECT md.visit_id, md.id, "
      + " md.description, md.icd_code, "
      + " md.code_type, md.diag_type, md.username, md.mod_time, diagnosis_status_name, md.remarks, "
      + " md.diagnosis_status_id, "
      + " diagnosis_datetime, md.doctor_id, d.doctor_name, sent_for_approval, md.year_of_onset, "
      + " health_authority, is_year_of_onset_mandatory " + " FROM mrd_diagnosis md "
      + " JOIN patient_registration pr ON (pr.patient_id = md.visit_id) "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no "
      + " AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
      + " JOIN mrd_codes_master mcm ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + " LEFT JOIN mrd_codes_details mcd ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + " LEFT JOIN diagnosis_statuses ds USING (diagnosis_status_id) "
      + " LEFT JOIN doctors d ON (md.doctor_id=d.doctor_id) WHERE #"
      + " ORDER BY diag_type asc, diagnosis_datetime asc, id asc";

  /**
   * Gets the all diagnosis details.
   *
   * @param patientId
   *          the patient id
   * @return the all diagnosis details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllDiagnosisDetails(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = GET_ALL_DIAGNOSIS_DETAILS;
    try {
      query = query.replace("#", " md.visit_id=?");
      ps = con.prepareStatement(query);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  
  
  /** The Constant GET_ALL_ADMR_DIAGNOSIS_DETAILS. */
  private static final String GET_ALL_ADMR_DIAGNOSIS_DETAILS = "SELECT md.visit_id, md.id, "
      + " md.description, md.icd_code, "
      + " md.code_type, md.diag_type, md.username, md.mod_time, diagnosis_status_name, md.remarks, "
      + " md.diagnosis_status_id, "
      + " diagnosis_datetime, md.doctor_id, d.doctor_name, sent_for_approval, md.year_of_onset, "
      + " health_authority, is_year_of_onset_mandatory "
      + " FROM mrd_diagnosis md "
      + " JOIN mrd_codes_master mcm ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + " LEFT JOIN mrd_codes_details mcd ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + " LEFT JOIN diagnosis_statuses ds USING (diagnosis_status_id) "
      + " LEFT JOIN doctors d ON (md.doctor_id=d.doctor_id) WHERE #"
      + " ORDER BY diag_type asc, diagnosis_datetime asc, id asc";


  /**
   * Gets the all diagnosis details.
   *
   * @param admissonRequestId
   *          the admisson request id
   * @return the all diagnosis details
   * @throws SQLException
   *           the SQL exception
   */
  public static List getAllDiagnosisDetails(Integer admissonRequestId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    String query = GET_ALL_ADMR_DIAGNOSIS_DETAILS;
    try {
      query = query.replace("#", " adm_request_id = ? AND md.visit_id is null");
      ps = con.prepareStatement(query);
      ps.setInt(1, admissonRequestId != null ? admissonRequestId.intValue() : 0);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_PRIM_SEC_DIAGNOSIS_DETAILS. */
  private static final String GET_PRIM_SEC_DIAGNOSIS_DETAILS = "SELECT visit_id, id,"
      + "  description, icd_code, "
      + " code_type, diag_type, username, mod_time FROM mrd_diagnosis WHERE visit_id=? "
      + " and diag_type IN ('P', 'S', 'V') " + " ORDER BY diag_type asc";

  /**
   * Gets the primary secondary diagnosis.
   *
   * @param patientId
   *          the patient id
   * @return the primary secondary diagnosis
   * @throws SQLException
   *           the SQL exception
   */
  // Diag codes are independent for each visit.
  public static List getPrimarySecondaryDiagnosis(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PRIM_SEC_DIAGNOSIS_DETAILS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PRVS_DIAGNOSIS_FIELDS. */
  public static final String PRVS_DIAGNOSIS_FIELDS = "SELECT visit_id, id,md.doctor_id,"
      + " md.description, icd_code, md.code_type, diag_type, md.remarks, ds.diagnosis_status_name, "
      + " diagnosis_datetime, doc.doctor_name, pr.visit_type, md.username, ds.diagnosis_status_id, "
      + " year_of_onset, health_authority, is_year_of_onset_mandatory ";

  /** The Constant PRVS_DIAGNOSIS_TABLES. */
  public static final String PRVS_DIAGNOSIS_TABLES = " FROM mrd_diagnosis md "
      + " JOIN patient_registration pr ON (md.visit_id = pr.patient_id) "
      + " JOIN mrd_codes_master mcm ON (mcm.code = md.icd_code AND mcm.code_type = md.code_type) "
      + " LEFT JOIN mrd_codes_details mcd ON (mcd.mrd_code_id = mcm.mrd_code_id) "
      + " LEFT JOIN doctors doc ON (md.doctor_id=doc.doctor_id) "
      + " LEFT JOIN diagnosis_statuses ds ON (md.diagnosis_status_id=ds.diagnosis_status_id) ";

  /** The Constant PRVS_DIAGNOSIS_COUNT. */
  public static final String PRVS_DIAGNOSIS_COUNT = "SELECT count(*) ";

  /**
   * Gets the previous diagnosis details.
   *
   * @param mrNo
   *          the mr no
   * @param patientId
   *          the patient id
   * @param pageNum
   *          the page num
   * @return the previous diagnosis details
   * @throws SQLException
   *           the SQL exception
   */
  public static PagedList getPreviousDiagnosisDetails(String mrNo, String patientId,
      Integer pageNum) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    qb = new SearchQueryBuilder(con, PRVS_DIAGNOSIS_FIELDS, PRVS_DIAGNOSIS_COUNT,
        PRVS_DIAGNOSIS_TABLES, null, null, false, 10, pageNum);

    try (PreparedStatement ps = con
        .prepareStatement("SELECT reg_date+reg_time as reg_date_time FROM"
            + " patient_registration WHERE patient_id=?")) {
      ps.setString(1, patientId);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      qb.addFilter(SearchQueryBuilder.STRING, "pr.mr_no", "=", mrNo);
      qb.addFilter(SearchQueryBuilder.TIMESTAMP, "reg_date+reg_time", "<",
          (java.sql.Timestamp) bean.get("reg_date_time"));

      qb.addSecondarySort("reg_date", true);
      qb.addSecondarySort("reg_time", true);
      qb.addSecondarySort("diag_type");
      qb.addSecondarySort("diagnosis_datetime", true);
      qb.build();
      return qb.getMappedPagedList();
    } finally {
      qb.close();
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /** The Constant DIAGNOSIS_HISTORY. */
  private static final String DIAGNOSIS_HISTORY = " SELECT visit_id, id, md.description,"
      + " icd_code, code_type, diag_type, md.username, md.mod_time, pr.reg_date "
      + " FROM mrd_diagnosis md "
      + " JOIN patient_registration pr ON pr.patient_id=md.visit_id WHERE visit_id IN "
      + " (SELECT patient_id FROM doctor_consultation dc "
      + "    JOIN patient_registration pr using (patient_id) "
      + " WHERE dc.mr_no=? AND pr.visit_type=? AND doctor_name = ? AND consultation_id < ? "
      + " ORDER BY consultation_id desc) AND diag_type IN ('P', 'S', 'V') "
      + " order by reg_date + reg_time desc , diag_type";

  /**
   * Gets the diagnosis history.
   *
   * @param mrNo
   *          the mr no
   * @param consultationId
   *          the consultation id
   * @param doctorId
   *          the doctor id
   * @param visitType
   *          the visit type
   * @return the diagnosis history
   * @throws SQLException
   *           the SQL exception
   */
  public static List getDiagnosisHistory(String mrNo, int consultationId, String doctorId,
      String visitType) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DIAGNOSIS_HISTORY);
      ps.setString(1, mrNo);
      ps.setString(2, visitType);
      ps.setString(3, doctorId);
      ps.setInt(4, consultationId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant DIAGNOSIS_VISIT_HISTORY. */
  private static final String DIAGNOSIS_VISIT_HISTORY = " SELECT visit_id, id, md.description, "
      + " icd_code, code_type,"
      + " diag_type, md.username, mod_time, pr.reg_date FROM mrd_diagnosis md "
      + " JOIN patient_registration pr ON pr.patient_id=md.visit_id  WHERE visit_id IN  "
      + " ( SELECT patient_id FROM doctor_consultation dc JOIN ( "
      + "     SELECT * FROM patient_registration WHERE main_visit_id =( "
      + "       SELECT main_visit_id FROM patient_registration "
      + "       WHERE patient_id = ?  ) AND reg_date+reg_time< ( "
      + "       SELECT reg_date+reg_time FROM patient_registration "
      + "       WHERE patient_id = ? ) ) AS pr USING (patient_id)  "
      + "  ORDER BY consultation_id desc ) AND diag_type IN ('P', 'S') "
      + " AND visit_id!= ? ORDER BY  reg_date, visit_id, diag_type";

  /**
   * Gets the diagnosis history.
   *
   * @param visitId
   *          the visit id
   * @return the diagnosis history
   * @throws SQLException
   *           the SQL exception
   */
  public static List getDiagnosisHistory(String visitId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(DIAGNOSIS_VISIT_HISTORY);
      ps.setString(1, visitId);
      ps.setString(2, visitId);
      ps.setString(3, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Copy diag codes.
   *
   * @param con
   *          the con
   * @param visitDetailsBean
   *          the visit details bean
   * @param latestEpisodeVisitId
   *          the latest episode visit id
   * @param username
   *          the username
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean copyDiagCodes(Connection con, BasicDynaBean visitDetailsBean,
      String latestEpisodeVisitId, String username) throws SQLException, IOException {
    boolean success = true;
    String opType = (String) visitDetailsBean.get("op_type");

    if (latestEpisodeVisitId != null && opType != null
        && (opType.equals("F") || opType.equals("D"))) {

      String visitId = (String) visitDetailsBean.get("patient_id");

      List<BasicDynaBean> previousVisitDiagCodes = findAllByKey("visit_id", latestEpisodeVisitId);
      if (previousVisitDiagCodes != null && previousVisitDiagCodes.size() > 0) {
        for (BasicDynaBean diagbean : previousVisitDiagCodes) {
          BigDecimal id = new BigDecimal(DataBaseUtil.getNextSequence("mrd_diagnosis_seq"));
          diagbean.set("id", id);
          diagbean.set("visit_id", visitId);
          diagbean.set("username", username);
          diagbean.set("mod_time", DateUtil.getCurrentTimestamp());
        }

        success = insertAll(con, previousVisitDiagCodes);
      }
    }
    return success;
  }

  /**
   * Sets the reset diag flags.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @param setResetFlag
   *          the set reset flag
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean setResetDiagFlags(Connection con, String patientId, boolean setResetFlag)
      throws SQLException, IOException {
    boolean success = true;

    BasicDynaBean mrdDiagnosisBean = getBean();
    mrdDiagnosisBean.set("visit_id", patientId);
    mrdDiagnosisBean.set("sent_for_approval", setResetFlag);

    success = updateWithName(con, mrdDiagnosisBean.getMap(), "visit_id") > 0;

    if (!success) {
      logger.error("Error while setting/resetting the " + " sent_for_approval flag for visit "
          + patientId);
    }
    return success;
  }

  /** The Constant UPDATE_USERNAME. */
  private static final String UPDATE_USERNAME = "UPDATE mrd_diagnosis SET username = ?"
      + "  WHERE id = ?";

  /**
   * Update user name.
   *
   * @param con
   *          the con
   * @param diagnosisId
   *          the diagnosis id
   * @param userName
   *          the user name
   * @return true, if successful
   * @throws SQLException
   *           the SQL exception
   */
  public static boolean updateUserName(Connection con, int diagnosisId, String userName)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_USERNAME);
      ps.setString(1, userName);
      ps.setInt(2, diagnosisId);
      return ps.executeUpdate() > 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /** The Constant UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID. */
  private static final String UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID = "UPDATE mrd_diagnosis SET "
      + "visit_id=? WHERE visit_id=?";

  /**
   * Update visit id.
   *
   * @param con
   *          the con
   * @param opVisitId
   *          the op visit id
   * @param ipVisitId
   *          the ip visit id
   * @throws SQLException
   *           the SQL exception
   */
  public static void updateVisitId(Connection con, String opVisitId, String ipVisitId)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_OP_VISIT_Id_TO_IP_VISIT_ID)) {
      ps.setString(1, ipVisitId);
      ps.setString(2, opVisitId);
      ps.executeUpdate();
    }
  }

  /**
   * Gets the primary diagnosis list.
   *
   * @param con
   *          the con
   * @param patientId
   *          the patient id
   * @return the primary diagnosis list
   * @throws SQLException
   *           the SQL exception
   */
  public static List getPrimaryDiagnosisList(Connection con, String patientId) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(GET_PRIMARY_DIAGNOSIS_DETAILS)) {
      ps.setString(1, patientId);
      List list = DataBaseUtil.queryToDynaList(ps);
      return list;
    }

  }
}