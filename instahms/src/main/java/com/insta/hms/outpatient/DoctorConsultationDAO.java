
package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.SplitSearchQueryBuilder;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.ConsultationForms;
import com.insta.hms.instaforms.IAForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.instaforms.TriageForms;
import com.insta.hms.master.ConsultationFavourites.ConsultationFavouritesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/*
 * DAO representing the table doctor_consultation: there is one row in this table for
 * every consultation prescribed/conducted for a patient. Multiple consultations can
 * be done for every visit, each with a possibly different doctor.
 */

/**
 * The Class DoctorConsultationDAO.
 */
public class DoctorConsultationDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(DoctorConsultationDAO.class);

  /**
   * Instantiates a new doctor consultation DAO.
   */
  public DoctorConsultationDAO() {
    super("doctor_consultation");
  }

  /**
   * Gets the latest consultation.
   *
   * @param patientId the patient id
   * @return the latest consultation
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getLatestConsultation(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement("SELECT * FROM doctor_consultation"
          + " WHERE patient_id=? AND status NOT IN ('C', 'X') AND"
          + " coalesce(cancel_status,'')='' order by presc_date desc limit 1");
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CONSULTATION_DETAILS. */
  private static final String CONSULTATION_DETAILS = "SELECT dc.*, consultation_type, "
      + "   COALESCE(b.status, 'C') as bill_status FROM doctor_consultation dc "
      + "   JOIN consultation_types ct ON (dc.head=ct.consultation_type_id::text) "
      + "  LEFT JOIN bill_activity_charge bac"
      + " ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') "
      + "  LEFT JOIN bill_charge bc ON (bac.charge_id=bc.charge_id) "
      + "  LEFT JOIN bill b ON (b.bill_no = bc.bill_no) " + " WHERE consultation_id=?";

  /**
   * Gets the consultation details.
   *
   * @param consultationId the consultation id
   * @return the consultation details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getConsultationDetails(int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONSULTATION_DETAILS);
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  // To fetch the no of signed off lab & rad reports for the patient.
  /** The get signed off lab reports count. */
  // (for op consultation search optimization)
  private static String GET_SIGNED_OFF_LAB_REPORTS_COUNT = " SELECT "
      + " COALESCE(count(tvr.report_id),0)"
      + " AS lab_rad_signed_off_reports, patient_id FROM test_visit_reports tvr ";

  /**
   * Gets the signed off lab reports count.
   *
   * @param visitIds the visit ids
   * @return the signed off lab reports count
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getSignedOffLabReportsCount(List visitIds) throws SQLException {
    if (visitIds == null || visitIds.isEmpty()) {
      return Collections.emptyList();
    }

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();
      Boolean signedOff = ((String) prefs.get("show_tests_in_emr")).equals("S");
      StringBuilder where = new StringBuilder("");
      DataBaseUtil.addWhereFieldInList(where, "tvr.patient_id", visitIds);

      ps = con.prepareStatement(GET_SIGNED_OFF_LAB_REPORTS_COUNT + where.toString()
          + (signedOff ? " AND signed_off='Y' " : "") + " GROUP BY tvr.patient_id ");
      for (int i = 0; i < visitIds.size(); i++) {
        ps.setString(i + 1, (String) visitIds.get(i));
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  // To fetch the no of signed off service reports for the patient.
  /** The Constant GET_SIGNED_OFF_SERVICE_REPORTS_COUNT. */
  // (for op consultation search optimization)
  private static final String GET_SIGNED_OFF_SERVICE_REPORTS_COUNT = "SELECT"
      + " COALESCE(count(doc_id),0)"
      + " AS service_signed_off_reports, sp.patient_id FROM service_documents sr"
      + " JOIN services_prescribed sp "
      + "       ON (sp.prescription_id=sr.prescription_id and signed_off) ";

  /**
   * Gets the signed off service reports count.
   *
   * @param visitIds the visit ids
   * @return the signed off service reports count
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getSignedOffServiceReportsCount(List visitIds)
      throws SQLException {
    if (visitIds == null || visitIds.isEmpty()) {
      return Collections.emptyList();
    }

    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      StringBuilder where = new StringBuilder("");
      DataBaseUtil.addWhereFieldInList(where, "sp.patient_id", visitIds);
      ps = con.prepareStatement(
          GET_SIGNED_OFF_SERVICE_REPORTS_COUNT + where.toString() + " GROUP BY sp.patient_id ");
      for (int i = 0; i < visitIds.size(); i++) {
        ps.setString(i + 1, (String) visitIds.get(i));
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant FROM_DATE_WHERE_CLAUSE. */
  private static final String FROM_DATE_WHERE_CLAUSE = " WHERE visited_date >= ? ";

  /** The Constant SEARCH_CONSULTATIONS. */
  // add all the searchable, orderable, and sortable columns here.
  private static final String SEARCH_CONSULTATIONS = " SELECT dcpr.mr_no, dcpr.visited_date,"
      + "   emergency_category, dcpr.status, triage_done, dept.dept_id,"
      + "   dcpr.doctor_name as doctor_id, dcpr.visit_type, dcpr.cancel_status,  "
      + "    dcpr.consultation_token, doc.doctor_name, dcpr.visit_status, dcpr.consultation_id, "
      + "   TO_TIMESTAMP(TO_CHAR(dcpr.reg_date+dcpr.reg_time,'dd-MM-yyyy HH24:MI:SS'),"
      + " 'dd-MM-yyyy HH24:MI:SS') as arrival_time, dcpr.visit_mode, "
      + "   dcpr.center_id, presc_doc.doctor_name as presc_doctor_name,"
      + " doc.doctor_name as doctor_full_name, sa.appointment_time, sa.teleconsult_url "
      + "FROM ("
      + "SELECT dc.mr_no, dc.presc_doctor_id, dc.appointment_id, dc.visited_date,"
      + " dc.status, dc.doctor_name, "
      + "pr.visit_type, dc.cancel_status, dc.consultation_token, dc.consultation_id,"
      + " pr.reg_date, pr.reg_time, pr.center_id, "
      + "dc.emergency_category, dc.triage_done, pr.status AS visit_status, dc.visit_mode "
      + "FROM doctor_consultation dc "
      + "   JOIN patient_registration pr ON (dc.patient_id=pr.patient_id) " + FROM_DATE_WHERE_CLAUSE
      + ") AS dcpr" + "   JOIN doctors doc ON (doc.doctor_id=dcpr.doctor_name) "
      + "   LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = dcpr.presc_doctor_id) "
      + "   LEFT JOIN scheduler_appointments sa ON(sa.appointment_id=dcpr.appointment_id) "
      + "   JOIN department dept ON (dept.dept_id=doc.dept_id) ";

  /** The Constant CONSULTATION_FIELDS. */
  private static final String CONSULTATION_FIELDS = " SELECT pd.mr_no, pr.patient_id,"
      + " dc.consultation_id, dc.doctor_name, dc.consultation_token, dc.start_datetime,"
      + " dc.visited_date, dc.remarks, dc.emergency_category, dc.triage_done, dc.status,"
      + " dc.appointment_id, dc.visit_mode, pr.complaint,  "
      + "   s.salutation, cm.country_name AS nationality,"
      + "   pd.patient_name, pd.last_name, pd.patient_phone, "
      + "   get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name,"
      + " pd.last_name) AS patient_full_name,"
      + "   get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age, "
      + "   get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_in, "
      + "   dept.dept_id, dept.dept_name as dept_name, doc.doctor_name as doctor_full_name, "
      + "   pd.patient_consultation_info, b.payment_status, b.bill_type,"
      + " bc.act_rate_plan_item_code, pr.analysis_of_complaint,"
      + " presc_doc.doctor_name as presc_doctor_name, doc.schedule, sa.teleconsult_url,"
      + "   0 as lab_rad_signed_off_reports,sa.appointment_time,"
      + " TO_TIMESTAMP(TO_CHAR(pr.reg_date+pr.reg_time,'dd-MM-yyyy HH24:MI:SS'),"
      + "'dd-MM-yyyy HH24:MI:SS') as arrival_time,"
      + "   0 as service_signed_off_reports, dept.dept_type_id " + " FROM doctor_consultation dc "
      + "  JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      // left join are required, when patient is registered with zero amount and having the
      // consultation doctor (ie., bill will be created in this case).
      + "  LEFT JOIN bill_activity_charge bac"
      + " ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') "
      + "  LEFT JOIN bill_charge bc ON (bac.charge_id=bc.charge_id) "
      + "  LEFT JOIN bill b ON (b.bill_no = bc.bill_no) "
      + "  JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND"
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + "  JOIN doctors doc ON (doc.doctor_id = dc.doctor_name) "
      + "  LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = dc.presc_doctor_id) "
      + "  JOIN department dept ON (dept.dept_id = doc.dept_id) "
      + "  JOIN salutation_master s ON (s.salutation_id = pd.salutation) "
      + "  LEFT JOIN country_master  cm ON (cm.country_id = pd.nationality_id)"
      + "  LEFT JOIN scheduler_appointments sa ON(sa.appointment_id=dc.appointment_id)";

  /** The Constant CONSULTATION_EXT_FIELDS. */
  /*
   * Search query for a list of open consultations
   */
  private static final String CONSULTATION_EXT_FIELDS = " SELECT dc.*,"
      + " pr.revisit as patient_revisit, pr.op_type, pr.complaint, pr.visit_type, pr.org_id, "
      + " s.salutation, pd.mr_no as patient_id, pd.patient_name, pd.last_name,"
      + " pd.patient_phone, get_patient_full_name(s.salutation, pd.patient_name,"
      + " pd.middle_name, pd.last_name) AS patient_full_name,"
      + "   get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age, "
      + "   get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_in, "
      + "  dept.dept_id, dept.dept_name as dept_name, doc.doctor_name as doctor_full_name,"
      + "  (case when pr.visit_type = 'o' then doc.op_template_id else"
      + " doc.ip_template_id end) as doctor_template_id,"
      + "   pd.patient_consultation_info, b.payment_status, b.bill_type,"
      + " bc.act_rate_plan_item_code, pr.analysis_of_complaint,"
      + "  presc_doc.doctor_name as presc_doctor_name, doc.schedule, doc.prescribe_by_favourites,"
      + "   0 as lab_rad_signed_off_reports,sa.appointment_time,"
      + " TO_TIMESTAMP(TO_CHAR(pr.reg_date+pr.reg_time,'dd-MM-yyyy HH24:MI:SS'),"
      + " 'dd-MM-yyyy HH24:MI:SS') as arrival_time," + "   reg_date+reg_time as admitted_datetime, "
      + "   0 as service_signed_off_reports, dept.dept_type_id, "
      + "   dc.start_datetime, dc.end_datetime, b.is_tpa, b.restriction_type ";

  /** The Constant CONSULTATION_EXT_TABLES. */
  private static final String CONSULTATION_EXT_TABLES = " FROM doctor_consultation dc "
      + "  JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + "  LEFT JOIN bill_activity_charge bac"
      + " ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC') "
      + "  LEFT JOIN bill_charge bc ON (bac.charge_id=bc.charge_id) "
      + "  LEFT JOIN bill b ON (b.bill_no = bc.bill_no) "
      + "  JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
      + "  JOIN doctors doc ON (doc.doctor_id = dc.doctor_name) "
      + "  LEFT JOIN doctors presc_doc ON (presc_doc.doctor_id = dc.presc_doctor_id) "
      + "  JOIN department dept ON (dept.dept_id = doc.dept_id) "
      + "  JOIN salutation_master s ON (s.salutation_id = pd.salutation) "
      + "  LEFT JOIN scheduler_appointments sa ON(sa.appointment_id=dc.appointment_id)";

  /** The Constant OPEN_CONSULTATION_WHERE. */
  private static final String OPEN_CONSULTATION_WHERE = "WHERE visit_type='o'"
      + " and cancel_status is null ";

  /**
   * Search consultations.
   *
   * @param params        the params
   * @param sortOrder     the sort order
   * @param sortReverse   the sort reverse
   * @param pageSize      the page size
   * @param pageNum       the page num
   * @param secondarySort the secondary sort
   * @param fromDate the from date
   * @return the paged list
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList searchConsultations(Map params, String sortOrder, boolean sortReverse,
      int pageSize, int pageNum, String secondarySort, String fromDate)
      throws SQLException, ParseException {

    Connection con = DataBaseUtil.getReadOnlyConnection();
    SplitSearchQueryBuilder sb = null;
    try {
      Map<LISTING, Object> listingParams = new HashMap<LISTING, Object>();
      listingParams.put(LISTING.SORTCOL, sortOrder);
      listingParams.put(LISTING.SORTASC, sortReverse);
      listingParams.put(LISTING.PAGENUM, pageNum);
      listingParams.put(LISTING.PAGESIZE, pageSize);
      String searchConsLastWeek = "";
      StringBuilder sbuild = new StringBuilder("'");
      if (fromDate != null) {
        sbuild.append(fromDate).append("'");
        searchConsLastWeek = FROM_DATE_WHERE_CLAUSE.replace("?", sbuild.toString());
      }
      String searchCons = SEARCH_CONSULTATIONS.replace(FROM_DATE_WHERE_CLAUSE, searchConsLastWeek);
      sb = new SplitSearchQueryBuilder(con, searchCons, CONSULTATION_FIELDS,
          OPEN_CONSULTATION_WHERE, "consultation_id", listingParams);
      params.put("consultation_id@type", new String[] { "integer" });
      sb.addFilterFromParamMap(params);
      int centerId = RequestContext.getCenterId();
      if (centerId != 0) {
        sb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
      }

      if (secondarySort != null && !secondarySort.equals("")) {
        sb.addSecondarySort(secondarySort);        
      }
      sb.build();
      return sb.getDynaPagedList();
    } finally {
      sb.close();
      con.close();
    }
  }

  /** The Constant NEW_CONSULTATIONS. */
  private static final String NEW_CONSULTATIONS = " SELECT dc.mr_no,"
      + " dc.consultation_token as token_no, sm.salutation, pd.patient_name, pd.last_name, "
      + "   pd.middle_name, dc.visited_date ";

  /** The Constant FROM_TABLES. */
  private static final String FROM_TABLES = "FROM doctor_consultation dc "
      + "   JOIN patient_registration pr ON (dc.patient_id=pr.patient_id) "
      + "   JOIN patient_details pd ON (dc.mr_no=pd.mr_no "
      + " AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
      + "   JOIN salutation_master sm ON (sm.salutation_id=pd.salutation) ";

  /** The Constant COUNT. */
  private static final String COUNT = "SELECT count(dc.consultation_id) ";

  /** The Constant WHERE_COND. */
  private static final String WHERE_COND = " WHERE dc.status='A' and pr.visit_type='o'"
      + " and coalesce(dc.cancel_status,'')=''";

  /**
   * Gets the new consultations.
   *
   * @param doctorId       the doctor id
   * @param pageNumber     the page number
   * @param recordsPerPage the records per page
   * @return the new consultations
   * @throws SQLException the SQL exception
   */
  public static PagedList getNewConsultations(String doctorId, int pageNumber, int recordsPerPage)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    SearchQueryBuilder qb = null;
    try {
      qb = new SearchQueryBuilder(con, NEW_CONSULTATIONS, COUNT, FROM_TABLES, WHERE_COND,
          "dc.consultation_token", false, recordsPerPage, pageNumber);
      qb.addFilter(SearchQueryBuilder.STRING, "dc.doctor_name", "=", doctorId);
      qb.build();
      return qb.getDynaPagedList();
    } finally {
      DataBaseUtil.closeConnections(con, null);
      if (qb != null) {
        qb.close();
      }
    }
  }

  /** The Constant CONSULTATIONS. */
  private static final String CONSULTATIONS = " SELECT dc.*,"
      + " d.doctor_name as doctor_full_name " + " FROM doctor_consultation dc "
      + "   JOIN doctors d ON (dc.doctor_name=d.doctor_id) " + " WHERE patient_id=?";

  /**
   * Gets the consultations.
   *
   * @param patientId the patient id
   * @return the consultations
   * @throws SQLException the SQL exception
   */
  public static List getConsultations(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONSULTATIONS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant MAIN_VISIT_CONSULTATIONS. */
  private static final String MAIN_VISIT_CONSULTATIONS = " SELECT dc.*,"
      + " d.doctor_name as doctor_full_name " + " FROM doctor_consultation dc "
      + "   JOIN doctors d ON (dc.doctor_name=d.doctor_id) " + " WHERE patient_id = ? "
      + " ORDER BY consultation_id ";

  /**
   * Gets the MRD visit consultations.
   *
   * @param patientId the patient id
   * @return the MRD visit consultations
   * @throws SQLException the SQL exception
   */
  public static List getMRDVisitConsultations(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(MAIN_VISIT_CONSULTATIONS);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Find consultation.
   *
   * @param consultationId the consultation id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  /*
   * Returns the basic consultation object: just the table columns
   */
  public BasicDynaBean findConsultation(int consultationId) throws SQLException {
    return findByKey("consultation_id", consultationId);
  }

  /** The Constant FIND_CONSULTATION_EXT. */
  /*
   * Returns the extended patient consultation object (includes patient info, doctor name etc.) from
   * doctor_consultation
   */
  private static final String FIND_CONSULTATION_EXT = CONSULTATION_EXT_FIELDS
      + CONSULTATION_EXT_TABLES + " WHERE consultation_id=? ";

  /**
   * Find consultation ext.
   *
   * @param consultationId the consultation id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean findConsultationExt(int consultationId) throws SQLException {

    List list = DataBaseUtil.queryToDynaList(FIND_CONSULTATION_EXT, consultationId);
    if (list.size() > 0) {
      return (BasicDynaBean) list.get(0);
    }
    return null;
  }

  /** The Constant CONS_PACKAGE_NAME. */
  /*
   * To get package name method
   */
  private static final String CONS_PACKAGE_NAME = "SELECT pm.package_name "
      + " FROM doctor_consultation dc "
      + " JOIN package_prescribed pp ON(dc.package_ref=pp.prescription_id) "
      + " JOIN packages pm ON(pm.package_id=pp.package_id)" + " WHERE consultation_id=?";

  /**
   * Gets the package name.
   *
   * @param consultationId the consultation id
   * @return the package name
   * @throws SQLException the SQL exception
   */
  public BasicDynaBean getPackageName(int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONS_PACKAGE_NAME);
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  // ---------------------------------------------------------------------------

  /** The Constant COUNT_OF_CONSULTATIONS. */
  private static final String COUNT_OF_CONSULTATIONS = "SELECT MIN(consultation_id)"
      + " FROM doctor_consultation dc " + "       JOIN patient_registration pr using (patient_id) "
      + "   WHERE pr.mr_no=? " + "       AND pr.visit_type=? and doctor_name = ? "
      + "       AND consultation_id < ? " + "   GROUP BY consultation_id "
      + "   ORDER BY consultation_id desc limit 1 ";

  /**
   * Gets the previous consultation id.
   *
   * @param mrNo           the mr no
   * @param consultationId the consultation id
   * @param doctorId       the doctor id
   * @param visitType      the visit type
   * @return the previous consultation id
   * @throws SQLException the SQL exception
   */
  public static int getPreviousConsultationId(String mrNo, int consultationId, String doctorId,
      String visitType) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(COUNT_OF_CONSULTATIONS);
      ps.setString(1, mrNo);
      ps.setString(2, visitType);
      ps.setString(3, doctorId);
      ps.setInt(4, consultationId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return 0;
  }

  /**
   * Insert consultation token.
   *
   * @param con           the con
   * @param presriptionid the presriptionid
   * @param token         the token
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertConsultationToken(Connection con, int presriptionid, int token)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement("INSERT INTO consultation_token "
        + " (consultation_id,consultation_token) " + " VALUES(?,?)");
    ps.setInt(1, presriptionid);
    ps.setInt(2, token);
    int result = ps.executeUpdate();
    return result != 0;
  }

  /** The Constant REOPEN_CONSULTATION. */
  private static final String REOPEN_CONSULTATION = "UPDATE doctor_consultation"
      + " set status='P', " + " consultation_complete_time=null WHERE consultation_id=?";

  /**
   * Reopen consultation.
   *
   * @param consultationId the consultation id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean reopenConsultation(int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(REOPEN_CONSULTATION);
      ps.setInt(1, consultationId);
      return ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CLOSE_CONSULTATIONS. */
  /*
   * Closes the consultation status to indicate it is done (retains patient_reg status as active,
   * till it gets closed in Edit Visit or at midnight automatically)
   */
  private static final String CLOSE_CONSULTATIONS = "UPDATE doctor_consultation"
      + " SET status='C',consultation_complete_time=?,"
      + "start_datetime=COALESCE(start_datetime, ?),end_datetime=COALESCE(end_datetime, ?) "
      + "WHERE consultation_id=?";

  /** The Constant CLOSE_TRIAGE. */
  private static final String CLOSE_TRIAGE = "UPDATE doctor_consultation"
      + " SET triage_done='Y',triage_complete_time=?,"
      + "triage_start_datetime=COALESCE(triage_start_datetime, ?),"
      + "triage_end_datetime=COALESCE(triage_end_datetime, ?) "
      + "WHERE consultation_id=?";


  /** The Constant CLOSE_INITIAL_ASSESSMENT. */
  private static final String CLOSE_INITIAL_ASSESSMENT = "UPDATE doctor_consultation"
      + " SET initial_assessment_status='Y',ia_complete_time=?,"
      + "ia_start_datetime=COALESCE(ia_start_datetime, ?),"
      + "ia_end_datetime=COALESCE(ia_end_datetime, ?) "
      + "WHERE consultation_id=?";


  /**
   * Close consultations.
   *
   * @param con        the con
   * @param consultIds the consult ids
   * @param closeTime  the close time
   * @param userName   the user name
   * @return true, if successful
   * @throws Exception    the exception
   */
  public static boolean closeConsultations(Connection con, int[] consultIds, Timestamp closeTime,
      String userName) throws Exception {
    PreparedStatement ps = null;
    DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
    AbstractInstaForms formdao = new ConsultationForms();
    try {
      ps = con.prepareStatement(CLOSE_CONSULTATIONS);
      for (int consultId : consultIds) {
        BasicDynaBean consRecord = consultDao.findConsultationExt(consultId);
        Map params = new HashMap();
        //Form type required for main/follow up form
        String formType = consRecord.get("op_type").equals("F") 
            ? "Form_OP_FOLLOW_UP_CONS" : "Form_CONS";
        params.put("form_type", formType);
        params.put("consultation_id", new String[] { consultId + "" });
        params.put("mr_no", new String[] { consRecord.get("mr_no").toString() });
        params.put("patient_id", new String[] { consRecord.get("patient_id").toString() });
        BasicDynaBean form = formdao.getComponents(params);
        params.put("insta_form_id", new String[] { form.get("form_id").toString() });
        params.put("close_cons_without_edit", new String[] { "Y" });
        // if it is a new consultation, and trying to close it without editing it, then copy master
        // forms and insert into transaction table.
        if (consRecord.get("status").equals("A") && formdao.save(con, params) != null) {
          return false;
        }

        ps.setTimestamp(1, closeTime);
        ps.setTimestamp(2, closeTime);
        ps.setTimestamp(3, closeTime);
        ps.setInt(4, consultId);

        ps.addBatch();
      }
      
      int[] results = ps.executeBatch();
      return DataBaseUtil.checkBatchUpdates(results);
    } finally {
      ps.close();
    }
  }

  /**
   * Close triage.
   *
   * @param con the con
   * @param consultIds the consult ids
   * @param closeTime the close time
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean closeTriage(Connection con, int[] consultIds, Timestamp closeTime,
      String userName) throws Exception {
    PreparedStatement ps = null;
    DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
    AbstractInstaForms formdao = new TriageForms();
    try {
      ps = con.prepareStatement(CLOSE_TRIAGE);
      for (int consultId : consultIds) {
        BasicDynaBean consRecord = consultDao.findConsultationExt(consultId);
        Map params = new HashMap();
        params.put("consultation_id", new String[] { consultId + "" });
        params.put("mr_no", new String[] { consRecord.get("mr_no").toString() });
        params.put("patient_id", new String[] { consRecord.get("patient_id").toString() });
        BasicDynaBean form = formdao.getComponents(params);
        params.put("insta_form_id", new String[] { form.get("form_id").toString() });
        params.put("close_cons_without_edit", new String[] { "Y" });
        // if it is a new consultation, and trying to close it without editing it, then copy master
        // forms and insert into transaction table.
        if (consRecord.get("triage_done").equals("N") && formdao.save(con, params) != null) {
          return false;
        }

        ps.setTimestamp(1, closeTime);
        ps.setTimestamp(2, closeTime);
        ps.setTimestamp(3, closeTime);
        ps.setInt(4, consultId);

        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      return DataBaseUtil.checkBatchUpdates(results);
    } finally {
      ps.close();
    }
  }

  /**
   * Close initial assessment.
   *
   * @param con the con
   * @param consultIds the consult ids
   * @param closeTime the close time
   * @param userName the user name
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean closeInitialAssessment(Connection con, int[] consultIds,
      Timestamp closeTime, String userName) throws Exception {
    PreparedStatement ps = null;
    DoctorConsultationDAO consultDao = new DoctorConsultationDAO();
    AbstractInstaForms formdao = new IAForms();
    try {
      ps = con.prepareStatement(CLOSE_INITIAL_ASSESSMENT);
      for (int consultId : consultIds) {
        BasicDynaBean consRecord = consultDao.findConsultationExt(consultId);
        Map params = new HashMap();
        params.put("consultation_id", new String[] { consultId + "" });
        params.put("mr_no", new String[] { consRecord.get("mr_no").toString() });
        params.put("patient_id", new String[] { consRecord.get("patient_id").toString() });
        BasicDynaBean form = formdao.getComponents(params);
        params.put("insta_form_id", new String[] { form.get("form_id").toString() });
        params.put("close_cons_without_edit", new String[] { "Y" });
        // if it is a new consultation, and trying to close it without editing it, then copy master
        // forms and insert into transaction table.
        if (consRecord.get("initial_assessment_status").equals("N") 
            && formdao.save(con, params) != null) {
          return false;
        }

        ps.setTimestamp(1, closeTime);
        ps.setTimestamp(2, closeTime);
        ps.setTimestamp(3, closeTime);
        ps.setInt(4, consultId);

        ps.addBatch();
      }
      int[] results = ps.executeBatch();
      return DataBaseUtil.checkBatchUpdates(results);
    } finally {
      ps.close();
    }
  }

  /** The Constant GET_DEPARTMENT_ID. */
  private static final String GET_DEPARTMENT_ID = "SELECT d.dept_id, dc.mr_no,"
      + " dc.patient_id FROM doctor_consultation dc"
      + " JOIN doctors d on (d.doctor_id = dc.doctor_name)" + " WHERE consultation_id= ?";

  /**
   * Find department id.
   *
   * @param consId the cons id
   * @return the basic dyna bean
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean findDepartmentId(int consId) throws SQLException {

    PreparedStatement ps = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_DEPARTMENT_ID);
      ps.setInt(1, consId);
      List list = DataBaseUtil.queryToDynaList(ps);
      return (BasicDynaBean) list.get(0);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant GET_CONSULTATION_DOCS. */
  private static final String GET_CONSULTATION_DOCS = "SELECT consultation_id,"
      + " visited_date, d.doctor_name, username, dc.patient_id,pr.reg_date"
      + " FROM doctor_consultation dc " 
      + " JOIN doctors d ON (d.doctor_id = dc.doctor_name) "
      + " JOIN patient_registration pr ON (dc.patient_id=pr.patient_id) "
      + " WHERE EXISTS (SELECT section_item_id from patient_section_details "
      + " where dc.consultation_id=section_item_id and #filter#=?) AND "
      + " dc.status in ('P', 'C') AND coalesce(dc.cancel_status, '')!='C' AND "
      + " operation_ref is null AND ";

  /**
   * Gets the case sheet EMR docs.
   *
   * @param patientId          the patient id
   * @param mrNo               the mr no
   * @param allVisitsDocuments the all visits documents
   * @return the case sheet EMR docs
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   */
  public static List<EMRDoc> getCaseSheetEMRDocs(String patientId, String mrNo,
      Boolean allVisitsDocuments) throws SQLException, ParseException {

    List<BasicDynaBean> list = null;
    if (allVisitsDocuments) {
      String query = GET_CONSULTATION_DOCS.replace("#filter#", "mr_no");
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(query + " dc.mr_no=? ", mrNo, mrNo);
    } else {
      String query = GET_CONSULTATION_DOCS.replace("#filter#", "patient_id");
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(query + " dc.patient_id=? ", patientId, patientId);
    }

    List<EMRDoc> opForms = new ArrayList<EMRDoc>();
    BasicDynaBean printpref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    int printerId = (Integer) printpref.get("printer_id");

    for (BasicDynaBean b : list) {
      EMRDoc emrDoc = new EMRDoc();

      emrDoc.setPrinterId(printerId);
      String doctor = (String) b.get("doctor_name");
      int consultId = (Integer) b.get("consultation_id");
      emrDoc.setDocid("" + consultId);
      emrDoc.setDate((Timestamp) b.get("visited_date"));
      emrDoc.setType("SYS_CONSULT");
      emrDoc.setPdfSupported(true);
      emrDoc.setAuthorized(true);
      emrDoc.setContentType("application/pdf");
      emrDoc.setUpdatedBy((String) b.get("username"));
      emrDoc.setDoctor(doctor);
      emrDoc.setTitle("Case Sheet - " + doctor);
      emrDoc.setDisplayUrl("/print/printEmrConsultation.json?consultation_id=" + consultId
          + "&printerId=" + printerId);
      emrDoc.setProvider(EMRInterface.Provider.CaseSheetsProvider);
      emrDoc.setVisitid((String) b.get("patient_id"));
      emrDoc.setVisitDate((java.util.Date) b.get("reg_date"));

      opForms.add(emrDoc);
    }
    return opForms;
  }

  /** The Constant GET_PATIENT_CONSULTATION_INFO. */
  private static final String GET_PATIENT_CONSULTATION_INFO = "SELECT DISTINCT d.doctor_name,"
      + "d.doctor_id,dc.consultation_id "
      + " FROM doctor_consultation dc JOIN doctors d ON dc.doctor_name=d.doctor_id "
      + "   JOIN patient_prescription pp ON (pp.consultation_id=dc.consultation_id) "
      + "   JOIN patient_medicine_prescriptions omp"
      + " on (omp.op_medicine_pres_id = pp.patient_presc_id) "
      + " WHERE dc.patient_id=? and pp.status IN ('P', 'PA')";

  /**
   * Gets the OP conslt details.
   *
   * @param patientId the patient id
   * @return the OP conslt details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getOPConsltDetails(String patientId) throws SQLException {

    Connection con = null;
    List<BasicDynaBean> list = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_PATIENT_CONSULTATION_INFO);
      ps.setString(1, patientId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      ps.close();
      con.close();
    }
    return list;
  }

  /** The Constant GET_PBM_PATIENT_DOC_CONSULTATION_INFO. */
  private static final String GET_PBM_PATIENT_DOC_CONSULTATION_INFO = "SELECT"
      + " DISTINCT d.doctor_name," + "d.doctor_id,dc.consultation_id "
      + " FROM doctor_consultation dc JOIN doctors d ON dc.doctor_name=d.doctor_id "
      + "   JOIN pbm_medicine_prescriptions omp on omp.consultation_id = dc.consultation_id "
      + " WHERE dc.patient_id=? and omp.issued != 'Y'";

  /** The Constant GET_PBM_PATIENT_REF_CONSULTATION_INFO. */
  private static final String GET_PBM_PATIENT_REF_CONSULTATION_INFO = "SELECT DISTINCT doctor_name,"
      + " doctor_id, consultation_id FROM ( " + " SELECT  "
      + "  CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name"
      + " END AS doctor_name, "
      + "  CASE WHEN (pr.op_type != 'O') THEN d.doctor_name ELSE ref.referal_name"
      + " END AS doctor_id, " + " 0 as consultation_id " + " FROM patient_registration pr "
      + " LEFT JOIN doctors d ON (d.doctor_id = pr.doctor) " + " LEFT JOIN ("
      + "   SELECT referal_name,referal_no FROM referral" + "   UNION"
      + "   SELECT doctor_name,doctor_id FROM doctors"
      + " ) AS ref ON (ref.referal_no = pr.reference_docto_id)"
      + " JOIN pbm_medicine_prescriptions pbmp on pbmp.visit_id = pr.patient_id "
      + " WHERE pr.patient_id=? and pbmp.issued != 'Y' ) AS foo";

  /**
   * Gets the PBM conslt details.
   *
   * @param patientId the patient id
   * @param opType    the op type
   * @return the PBM conslt details
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPBMConsltDetails(String patientId, String opType)
      throws SQLException {

    Connection con = null;
    List<BasicDynaBean> list = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      if (opType != null && opType.equals("O")) {
        ps = con.prepareStatement(GET_PBM_PATIENT_REF_CONSULTATION_INFO);
      } else {
        ps = con.prepareStatement(GET_PBM_PATIENT_DOC_CONSULTATION_INFO);
      }
      ps.setString(1, patientId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      ps.close();
      con.close();
    }
    return list;
  }

  /** The Constant GET_DOCTOR_VISITS. */
  private static final String GET_DOCTOR_VISITS = " SELECT dc.*, d.*," + " d.doctor_name as doctor "
      + " FROM doctor_consultation dc "
      + " JOIN consultation_types ON(head = consultation_type_id::text ) "
      + " JOIN doctors d on d.doctor_id=dc.doctor_name"
      + " WHERE dc.patient_id=? and d.status='A' and dc.status!='U'";

  /**
   * Gets the doctor visits.
   *
   * @param visitId the visit id
   * @return the doctor visits
   * @throws SQLException the SQL exception
   */
  /*
   * returns all the doctor visits except the co-surgeons added for the operations.
   */
  public static List<BasicDynaBean> getDoctorVisits(String visitId) throws SQLException {
    return getDoctorVisits(visitId, null);
  }

  /**
   * Gets the doctor visits.
   *
   * @param visitId   the visit id
   * @param visitType the visit type
   * @return the doctor visits
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getDoctorVisits(String visitId, String visitType)
      throws SQLException {
    String query = "";
    if (visitType == null) {
      query = GET_DOCTOR_VISITS;
    } else if (visitType.equals("i")) {
      query = GET_DOCTOR_VISITS + " AND patient_type = 'i'";
    } else if (visitType.equals("o")) {
      query = GET_DOCTOR_VISITS + " AND patient_type = 'o'";
    }
    query += " ORDER BY dc.visited_date desc";
    return DataBaseUtil.queryToDynaList(query, visitId);
  }

  /** The Constant GET_PATIENT_DOC_VISITS. */
  /*
   * Returns all the patient's previous consultation dates across visits. Used for checking revisit
   * validity.
   */
  private static final String GET_PATIENT_DOC_VISITS = " SELECT dc.*,"
      + "d.doctor_name as doctor,visit_consultation_type FROM doctor_consultation dc "
      + " JOIN doctors d on(dc.doctor_name = doctor_id) "
      + " JOIN consultation_types ct on(ct.consultation_type_id::text = dc.head)"
      + " WHERE visit_consultation_type IN ('-1', '-2') AND mr_no=? "
      + " ORDER BY visited_date DESC LIMIT 50 ";

  /**
   * Gets the patient doctor visits.
   *
   * @param mrNo the mr no
   * @return the patient doctor visits
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPatientDoctorVisits(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PATIENT_DOC_VISITS, mrNo);
  }

  /** The Constant GET_PATIENT_FOLLOWUP_DOC_VISITS. */
  /*
   * Returns all the patient's previous follow up consultation dates across visits. Used for
   * checking Op revisit validity after IP Discharge.
   */
  private static final String GET_PATIENT_FOLLOWUP_DOC_VISITS = "SELECT dc.doctor_name,"
      + "d.doctor_name as doctor,visit_consultation_type " + " FROM doctor_consultation dc "
      + " JOIN doctors d on(dc.doctor_name = doctor_id) "
      + " JOIN consultation_types ct on(ct.consultation_type_id::text = dc.head)"
      + " WHERE visit_consultation_type IN ('-4') AND mr_no=? " + " ORDER BY visited_date DESC ";

  /**
   * Gets the patient follow up doctor visits.
   *
   * @param mrNo the mr no
   * @return the patient follow up doctor visits
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getPatientFollowUpDoctorVisits(String mrNo)
      throws SQLException {
    return DataBaseUtil.queryToDynaList(GET_PATIENT_FOLLOWUP_DOC_VISITS, mrNo);
  }

  /** The Constant GET_VISIT_CONSULTATIONS. */
  /*
   * Returns all the doctor consultations for a visit.
   */
  private static final String GET_VISIT_CONSULTATIONS = "SELECT "
      + "  mr_no, patient_id, doctor_id, d.doctor_name,consultation_id,"
      + " head, consultation_type, date(presc_date) AS presc_date,"
      + " dc.cancel_status, d.dept_id, dep.dept_name," + "  consultation_token "
      + " FROM doctor_consultation dc " + "   JOIN doctors d on(dc.doctor_name = doctor_id) "
      + "   JOIN department dep ON (dep.dept_id = d.dept_id) "
      + "   JOIN consultation_types ct on(ct.consultation_type_id::text = dc.head)"
      + " WHERE patient_id = ? ";

  /**
   * Gets the visit consultations.
   *
   * @param visitId the visit id
   * @return the visit consultations
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getVisitConsultations(String visitId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_VISIT_CONSULTATIONS);
      ps.setString(1, visitId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant CONSULTING_DOCTOR. */
  /*
   * query to get doctor_consultation details including doctor name for the consultation id.
   */
  private static final String CONSULTING_DOCTOR = "SELECT dc.consultation_id,"
      + " dc.mr_no, d.doctor_name, d.doctor_id, dc.patient_id, "
      + " '' as description, '' as doctor_notes, '' as remarks, '' as diagnosis,"
      + " prescription_notes, dc.status, "
      + " '' as nursing_assessment, dc.immunization_status_upto_date,"
      + " dc.emergency_category, dc.consultation_complete_time, "
      + "   dc.immunization_remarks, d.registration_no, start_datetime, end_datetime "
      + " FROM doctors d " + " JOIN doctor_consultation dc ON dc.doctor_name=d.doctor_id "
      + " WHERE dc.consultation_id=?";

  /**
   * Gets the consult details.
   *
   * @param consultId the consult id
   * @return the consult details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getConsultDetails(int consultId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(CONSULTING_DOCTOR);
      ps.setInt(1, consultId);
      List list = DataBaseUtil.queryToDynaList(ps);
      if (list != null && !list.isEmpty()) {
        return (BasicDynaBean) list.get(0);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return null;
  }

  /** The Constant CONSULT_TEMPLATE. */
  private static final String CONSULT_TEMPLATE = " SELECT dht.template_id,"
      + " template_name, field_id, field_name, num_lines, default_value as field_value"
      + " FROM doc_hvf_templates dht " + "   JOIN doc_hvf_template_fields dhtf USING (template_id) "
      + " WHERE dht.template_id=? and dhtf.field_status='A'";

  /**
   * Gets the consultation template.
   *
   * @param templateId the template id
   * @return the consultation template
   * @throws SQLException the SQL exception
   */
  public static List getConsultationTemplate(int templateId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONSULT_TEMPLATE);
      ps.setInt(1, templateId == 0 ? -1 : templateId);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UPDATE_CONSULTATION_VISIT_ID. */
  /*
   * Update the visit ID of a doctor consultation, restricted to the bill number which is being
   * moved from one visit to another. Used in OP IP conversion.
   */
  public static final String UPDATE_CONSULTATION_VISIT_ID = "UPDATE doctor_consultation"
      + " d SET patient_id=? " + "FROM bill_activity_charge bac, bill_charge bc "
      + " WHERE (bac.activity_id = d.consultation_id::text AND activity_code = 'DOC')"
      + "  AND (bac.charge_id = bc.charge_id) " + "  AND bill_no=?";

  /**
   * Update visit id.
   *
   * @param con      the con
   * @param billNo   the bill no
   * @param newVisit the new visit
   * @return true or false indicating success of operation
   * @throws SQLException the SQL exception
   */
  public boolean updateVisitId(Connection con, String billNo, String newVisit) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_CONSULTATION_VISIT_ID);
    ps.setString(1, newVisit);
    ps.setString(2, billNo);
    boolean success = ps.executeUpdate() > 0;
    ps.close();
    return success;
  }

  /** The get doctor consultation charge. */
  private static String GET_DOCTOR_CONSULTATION_CHARGE = " SELECT  dc.*,"
      + " bc.charge_id, b.bill_no, op.oper_priority " + " FROM doctor_consultation dc  "
      + " JOIN patient_registration pr USING (patient_id) "
      + " JOIN bill b ON b.visit_id = pr.patient_id  "
      + " JOIN bill_charge bc ON bc.bill_no = b.bill_no "
      + " JOIN bill_activity_charge bac ON bac.charge_id= bc.charge_id "
      + " AND bac.activity_id = dc.consultation_id::varchar "
      + " LEFT JOIN operation_procedures op ON dc.operation_ref = op.prescribed_id "
      + " AND bc.op_id=op.operation_id " 
      + " LEFT JOIN bed_operation_schedule boss ON "
      + "    (dc.operation_ref = boss.prescribed_id AND op.oper_priority='P') "
      + " LEFT JOIN bed_operation_secondary bos ON "
      + "    (dc.operation_ref = bos.sec_prescribed_id AND op.oper_priority='S') "
      + " where bc.charge_id = ? ";

  /**
   * Gets the doctor consultation charge.
   *
   * @param con      the con
   * @param chargeId the charge id
   * @return the doctor consultation charge
   * @throws SQLException the SQL exception
   */
  public DynaBean getDoctorConsultationCharge(Connection con, String chargeId) throws SQLException {
    PreparedStatement ps = con.prepareStatement(GET_DOCTOR_CONSULTATION_CHARGE);
    ps.setString(1, chargeId);
    DynaBean drBean = DataBaseUtil.queryToDynaBean(ps);
    ps.close();
    return drBean;
  }

  /**
   * Gets the doctor consultation charge.
   *
   * @param chargeId the charge id
   * @return the doctor consultation charge
   * @throws SQLException the SQL exception
   */
  public DynaBean getDoctorConsultationCharge(String chargeId) throws SQLException {
    DynaBean drBean = null;
    try (Connection con = DataBaseUtil.getConnection(); 
        PreparedStatement ps = con.prepareStatement(GET_DOCTOR_CONSULTATION_CHARGE)) {
      ps.setString(1, chargeId);
      drBean = DataBaseUtil.queryToDynaBean(ps);
    }
    return drBean;
  }

  /** The Constant GET_TRIAGE_DOCS. */
  private static final String GET_TRIAGE_DOCS = " SELECT consultation_id,"
      + " visited_date, d.doctor_name, username, dc.patient_id,pr.reg_date "
      + " FROM doctor_consultation dc "
      + " JOIN patient_registration pr ON (pr.patient_id=dc.patient_id) "
      + " JOIN doctors d ON (d.doctor_id = dc.doctor_name) "
      + " WHERE coalesce(dc.cancel_status, '')!='C' AND dc.triage_done!='N'";

  /**
   * Gets the triage docs.
   *
   * @param visitId       the visit id
   * @param mrNo          the mr no
   * @param allVisitsDocs the all visits docs
   * @return the triage docs
   * @throws SQLException the SQL exception
   */
  public static List getTriageDocs(String visitId, String mrNo, boolean allVisitsDocs)
      throws SQLException {
    List<BasicDynaBean> list = null;
    List<EMRDoc> opForms = new ArrayList<EMRDoc>();

    if (allVisitsDocs) {
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(GET_TRIAGE_DOCS + " AND dc.mr_no=? ", mrNo);
    } else {
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(GET_TRIAGE_DOCS + " AND dc.patient_id = ? ", visitId);
    }
    BasicDynaBean printpref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    int printerId = (Integer) printpref.get("printer_id");
    List<Integer> consultations = new ArrayList<>();
    for (BasicDynaBean b : list) {
      int consultId = (Integer) b.get("consultation_id");
      if (consultations.contains(consultId)) {
        continue;
      }

      consultations.add(consultId);
      EMRDoc emrDoc = new EMRDoc();

      emrDoc.setPrinterId(printerId);
      String doctor = (String) b.get("doctor_name");
      emrDoc.setDocid("" + consultId);
      emrDoc.setDate((Timestamp) b.get("visited_date"));
      emrDoc.setType("SYS_TRIAGE");
      emrDoc.setPdfSupported(true);
      emrDoc.setAuthorized(true);
      emrDoc.setContentType("application/pdf");
      emrDoc.setUpdatedBy((String) b.get("username"));
      emrDoc.setDoctor(doctor);
      emrDoc.setTitle("Triage/Nurse Assessment - " + doctor);
      emrDoc.setDisplayUrl(
          "/print/printTriage.json?consultationId=" + consultId + "&printerId=" + printerId);
      emrDoc.setProvider(EMRInterface.Provider.TriageSummaryProvider);
      emrDoc.setVisitid((String) b.get("patient_id"));
      emrDoc.setVisitDate((java.util.Date) b.get("reg_date"));

      opForms.add(emrDoc);
    }

    return opForms;
  }

  /** The Constant INITIAL_ASSESSMENT_DOCS. */
  private static final String INITIAL_ASSESSMENT_DOCS = "SELECT consultation_id,"
      + " visited_date, d.doctor_name, username, dc.patient_id,pr.reg_date"
      + " FROM doctor_consultation dc " + "   JOIN patient_section_details psd"
      + " ON (psd.section_item_id=dc.consultation_id and psd.item_type='CONS')"
      + "   JOIN patient_section_forms psf"
      + " ON (psd.section_detail_id=psf.section_detail_id and psf.form_type='Form_IA') "
      + "   JOIN patient_registration pr ON (pr.patient_id=dc.patient_id) "
      + "   JOIN doctors d ON (d.doctor_id = dc.doctor_name) "
      + " WHERE coalesce(dc.cancel_status, '')!='C' ";

  /**
   * Gets the assessment docs.
   *
   * @param visitId       the visit id
   * @param mrNo          the mr no
   * @param allVisitsDocs the all visits docs
   * @return the assessment docs
   * @throws SQLException the SQL exception
   */
  public static List getAssessmentDocs(String visitId, String mrNo, boolean allVisitsDocs)
      throws SQLException {
    List<BasicDynaBean> list = null;
    List<EMRDoc> opForms = new ArrayList<EMRDoc>();

    if (allVisitsDocs) {
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(INITIAL_ASSESSMENT_DOCS + " AND dc.mr_no=?  ", mrNo);
    } else {
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(INITIAL_ASSESSMENT_DOCS + " AND dc.patient_id = ? ", visitId);
    }
    BasicDynaBean printpref = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    int printerId = (Integer) printpref.get("printer_id");
    List<Integer> consultations = new ArrayList<>();
    for (BasicDynaBean b : list) {
      int consultId = (Integer) b.get("consultation_id");
      if (consultations.contains(consultId)) {
        continue;
      }
      consultations.add(consultId);
      EMRDoc emrDoc = new EMRDoc();

      emrDoc.setPrinterId(printerId);
      String doctor = (String) b.get("doctor_name");
      emrDoc.setDocid("" + consultId);
      emrDoc.setDate((Timestamp) b.get("visited_date"));
      emrDoc.setType("SYS_ASSESSMENT");
      emrDoc.setPdfSupported(true);
      emrDoc.setAuthorized(true);
      emrDoc.setContentType("application/pdf");
      emrDoc.setUpdatedBy((String) b.get("username"));
      emrDoc.setDoctor(doctor);
      emrDoc.setTitle("Initial Assessment - " + doctor);
      emrDoc.setDisplayUrl("/InitialAssessment/InitialAssessmentPrint.do?_method="
          + "printInitialAssessment&consultation_id=" + consultId + "&printerId=" + printerId);
      emrDoc.setProvider(EMRInterface.Provider.AssessmentProvider);
      emrDoc.setVisitid((String) b.get("patient_id"));
      emrDoc.setVisitDate((java.util.Date) b.get("reg_date"));

      opForms.add(emrDoc);
    }

    return opForms;
  }

  /**
   * replaces oldConsultationId with the new Consultation Id for all the transactions.
   *
   * @param con          the con
   * @param oldConsultId the old consult id
   * @param newConsultId the new consult id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public static boolean replaceOldWithNewConsultation(Connection con, int oldConsultId,
      int newConsultId) throws SQLException, IOException {
    GenericDAO images = new GenericDAO("doctor_consult_images");
    PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
    PatientPrescriptionDAO ppDAO = new PatientPrescriptionDAO();

    boolean flag = true;
    txn: {
      // update the transaction tables with latest consultation id in place cancelled one.
      if (ppDAO.findByKey(con, "consultation_id", oldConsultId) != null) {
        flag = false;
        BasicDynaBean bean = ppDAO.getBean();
        bean.set("consultation_id", newConsultId);
        if (ppDAO.update(con, bean.getMap(), "consultation_id", oldConsultId) == 0) {
          break txn;
        }
      }

      BasicDynaBean consultImageBean = images.getBean();
      if (images.loadByteaRecords(con, consultImageBean, "consultation_id", oldConsultId)) {
        flag = false;
        BasicDynaBean bean = images.getBean();
        bean.set("consultation_id", newConsultId);
        if (images.update(con, bean.getMap(), "consultation_id", oldConsultId) == 0) {
          break txn;
        }
      }
      Map filterKeys = new HashMap();
      filterKeys.put("section_item_id", oldConsultId);
      filterKeys.put("item_type", "CONS");
      if (psdDAO.findByKey(con, filterKeys) != null) {
        flag = false;
        BasicDynaBean bean = psdDAO.getBean();
        bean.set("section_item_id", newConsultId);

        if (psdDAO.update(con, bean.getMap(), filterKeys) == 0) {
          break txn;
        }
      }

      DoctorConsultationDAO consultDAO = new DoctorConsultationDAO();
      BasicDynaBean consultBeanOld = consultDAO.findByKey(con, "consultation_id", oldConsultId);
      BasicDynaBean consultBeanNew = consultDAO.getBean();

      consultBeanNew.set("triage_done", consultBeanOld.get("triage_done"));
      consultBeanNew.set("emergency_category", consultBeanOld.get("emergency_category"));
      consultBeanNew.set("immunization_status_upto_date",
          consultBeanOld.get("immunization_status_upto_date"));
      consultBeanNew.set("prescription_notes", consultBeanOld.get("prescription_notes"));
      consultBeanNew.set("doc_id", consultBeanOld.get("doc_id"));
      consultBeanNew.set("template_id", consultBeanOld.get("template_id"));
      consultBeanNew.set("status", consultBeanOld.get("status"));
      if (consultDAO.update(con, consultBeanNew.getMap(), "consultation_id", newConsultId) == 0) {
        flag = false;
        break txn;
      }
      // updating with empty values for old consultation(cancelled consultation).
      consultBeanOld = consultDAO.getBean();
      consultBeanOld.set("doc_id", 0);
      consultBeanOld.set("template_id", 0);
      consultBeanOld.set("triage_done", "N");
      consultBeanOld.set("emergency_category", "N");
      consultBeanOld.set("immunization_status_upto_date", "");
      consultBeanOld.set("prescription_notes", "");
      if (consultDAO.update(con, consultBeanOld.getMap(), "consultation_id", oldConsultId) == 0) {
        flag = false;
        break txn;
      }

      flag = true;
    }
    return flag;
  }

  /**
   * Strings equal.
   *
   * @param str  the str
   * @param str1 the str 1
   * @return true, if successful
   */
  public static boolean stringsEqual(String str, String str1) {
    str = str == null ? "" : str;
    str1 = str1 == null ? "" : str1;
    return str.equals(str1);
  }

  /**
   * Int equal.
   *
   * @param int1 the int 1
   * @param int2 the int 2
   * @return true, if successful
   */
  public static boolean intEqual(Integer int1, Integer int2) {
    int1 = int1 == null ? -100 : int1;
    int2 = int2 == null ? -100 : int2;
    return int1.intValue() == int2.intValue();
  }

  /**
   * Checks if is duplicate.
   *
   * @param con                the con
   * @param prescBean          the presc bean
   * @param itemType           the item type
   * @param doctorId           the doctor id
   * @param useStoreItems      the use store items
   * @param favouriteId        the favourite id
   * @param againtPrescription the againt prescription
   * @param nonHospMedicine    the non hosp medicine
   * @return true, if is duplicate
   * @throws SQLException the SQL exception
   */
  public static boolean isDuplicate(Connection con, BasicDynaBean prescBean, String itemType,
      String doctorId, String useStoreItems, int favouriteId, boolean againtPrescription,
      Boolean nonHospMedicine) throws SQLException {
    ConsultationFavouritesDAO favDAO = null;
    boolean isduplicate = false;

    if (itemType.equals("Medicine") && !nonHospMedicine) {
      if (useStoreItems.equals("Y")) {
        favDAO = new ConsultationFavouritesDAO("doctor_medicine_favourites");
        List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
        for (BasicDynaBean bean : favList) {
          if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("frequency"), (String) bean.get("frequency"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("medicine_remarks"),
              (String) bean.get("medicine_remarks"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("strength"), (String) bean.get("strength"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("generic_code"),
              (String) bean.get("generic_code"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("cons_uom_id"),
              (Integer) bean.get("cons_uom_id"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("item_strength"),
              (String) bean.get("item_strength"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("item_strength_units"),
              (Integer) bean.get("item_strength_units"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("duration"), (Integer) bean.get("duration"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("duration_units"),
              (String) bean.get("duration_units"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("medicine_quantity"),
              (Integer) bean.get("medicine_quantity"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("medicine_id"),
              (Integer) bean.get("medicine_id"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("route_of_admin"),
              (Integer) bean.get("route_of_admin"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("item_form_id"),
              (Integer) bean.get("item_form_id"))) {
            continue;
          }
          // all the values are same
          isduplicate = true;
          break;

        }
      } else {
        favDAO = new ConsultationFavouritesDAO("doctor_other_medicine_favourites");
        List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
        for (BasicDynaBean bean : favList) {
          if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("frequency"), (String) bean.get("frequency"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("medicine_remarks"),
              (String) bean.get("medicine_remarks"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("strength"), (String) bean.get("strength"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("medicine_name"),
              (String) bean.get("medicine_name"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("cons_uom_id"),
                  (Integer) bean.get("cons_uom_id"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("item_strength"),
              (String) bean.get("item_strength"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("item_strength_units"),
              (Integer) bean.get("item_strength_units"))) {
            continue;
          }

          if (!intEqual((Integer) prescBean.get("duration"), (Integer) bean.get("duration"))) {
            continue;
          }
          if (!stringsEqual((String) prescBean.get("duration_units"),
              (String) bean.get("duration_units"))) {
            continue;
          }

          if (!intEqual((Integer) prescBean.get("medicine_quantity"),
              (Integer) bean.get("medicine_quantity"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("route_of_admin"),
              (Integer) bean.get("route_of_admin"))) {
            continue;
          }
          if (!intEqual((Integer) prescBean.get("item_form_id"),
              (Integer) bean.get("item_form_id"))) {
            continue;
          }

          // all the values are same
          isduplicate = true;
          break;

        }
      }
    } else if (itemType.equals("Inv.")) {
      favDAO = new ConsultationFavouritesDAO("doctor_test_favourites");
      List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
      for (BasicDynaBean bean : favList) {
        if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("test_id"), (String) bean.get("test_id"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("test_remarks"),
            (String) bean.get("test_remarks"))) {
          continue;
        }

        if (!((Boolean) prescBean.get("ispackage")).equals((Boolean) bean.get("ispackage"))) {
          continue;
        }

        // all the values are same
        isduplicate = true;
        break;

      }

    } else if (itemType.equals("Service")) {
      favDAO = new ConsultationFavouritesDAO("doctor_service_favourites");
      List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
      for (BasicDynaBean bean : favList) {
        if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("service_id"), (String) bean.get("service_id"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("service_remarks"),
            (String) bean.get("service_remarks"))) {
          continue;
        }

        // all the values are same
        isduplicate = true;
        break;

      }

    } else if (itemType.equals("Doctor")) {
      favDAO = new ConsultationFavouritesDAO("doctor_consultation_favourites");
      List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
      for (BasicDynaBean bean : favList) {
        if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
          continue;
        }
        if (!stringsEqual(
            (String) prescBean.get(againtPrescription ? "doctor_id" : "cons_doctor_id"),
            (String) bean.get("cons_doctor_id"))) {
          continue;
        }
        if (!stringsEqual(
            (String) prescBean.get(againtPrescription ? "cons_remarks" : "consultation_remarks"),
            (String) bean.get("consultation_remarks"))) {
          continue;
        }

        // all the values are same
        isduplicate = true;
        break;

      }

    } else if (itemType.equals("NonHospital") || (itemType.equals("Medicine") && nonHospMedicine)) {
      favDAO = new ConsultationFavouritesDAO("doctor_other_favourites");

      List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
      for (BasicDynaBean bean : favList) {
        if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("frequency"), (String) bean.get("frequency"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("item_remarks"),
            (String) bean.get("item_remarks"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("strength"), (String) bean.get("strength"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("item_name"), (String) bean.get("item_name"))) {
          continue;
        }
        if (!intEqual((Integer) prescBean.get("cons_uom_id"),
                (Integer) bean.get("cons_uom_id"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("item_strength"),
            (String) bean.get("item_strength"))) {
          continue;
        }
        if (!intEqual((Integer) prescBean.get("item_strength_units"),
            (Integer) bean.get("item_strength_units"))) {
          continue;
        }
        if (!intEqual((Integer) prescBean.get("duration"), (Integer) bean.get("duration"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("duration_units"),
            (String) bean.get("duration_units"))) {
          continue;
        }

        if (!intEqual((Integer) prescBean.get("medicine_quantity"),
            (Integer) bean.get("medicine_quantity"))) {
          continue;
        }
        if (!intEqual((Integer) prescBean.get("item_form_id"),
            (Integer) bean.get("item_form_id"))) {
          continue;
        }
        if ((Boolean) prescBean.get("non_hosp_medicine") != (Boolean) bean
            .get("non_hosp_medicine")) {
          continue;
        }

        // all the values are same
        isduplicate = true;
        break;

      }

    } else if (itemType.equals("Operation")) {
      favDAO = new ConsultationFavouritesDAO("doctor_operation_favourites");
      List<BasicDynaBean> favList = favDAO.findAllByKey(con, "doctor_id", doctorId);
      for (BasicDynaBean bean : favList) {
        if (favouriteId == ((Integer) bean.get("favourite_id")).intValue()) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("operation_id"),
            (String) bean.get("operation_id"))) {
          continue;
        }
        if (!stringsEqual((String) prescBean.get("remarks"), (String) bean.get("remarks"))) {
          continue;
        }
        // all the values are same
        isduplicate = true;
        break;

      }

    }
    return isduplicate;

  }

  /**
   * Insert favourites.
   *
   * @param con                the con
   * @param itemPrescId        the item presc id
   * @param prescBean          the presc bean
   * @param itemType           the item type
   * @param doctorId           the doctor id
   * @param useStoreItems      the use store items
   * @param nonHospMedicine    the non hosp medicine
   * @param specialInstruction the special instruction
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean insertFavourites(Connection con, int itemPrescId, BasicDynaBean prescBean,
      String itemType, String doctorId, String useStoreItems, Boolean nonHospMedicine,
      String specialInstruction) throws SQLException {
    boolean flag = true;
    // set 1 as default for display order when inserting the favourite from consultation screen.
    // Refer Bug : 32059. which gets the items in alphabetical order, if the user wants it in
    // display order,
    // he will give the correct display order by entering in the master.
    int displayOrder = 1;
    // dont insert the duplicate rows, dont even throw error, just ignore it.
    if (isDuplicate(con, prescBean, itemType, doctorId, useStoreItems, 0, true, nonHospMedicine)) {
      return true;
    }

    if (itemType.equals("Medicine") && !nonHospMedicine) {
      if (useStoreItems.equals("Y")) {
        PreparedStatement ps = null;
        try {
          ps = con.prepareStatement(" INSERT INTO doctor_medicine_favourites(favourite_id,"
              + " doctor_id, display_order, frequency, duration, duration_units,"
              + " medicine_quantity, medicine_remarks, medicine_id, route_of_admin, "
              + "   strength, generic_code, item_form_id, item_strength,"
              + " item_strength_units, consumption_uom, admin_strength, special_instr)"
              + " (select nextval('doctor_medicine_favourites_seq'), ?, ?, frequency,"
              + " duration, duration_units, medicine_quantity, medicine_remarks,"
              + " medicine_id, route_of_admin, strength, generic_code, "
              + "   item_form_id, item_strength, item_strength_units,"
              + " consumption_uom, admin_strength, ? "
              + "  from patient_medicine_prescriptions where op_medicine_pres_id=?)");
          ps.setString(1, doctorId);
          ps.setInt(2, displayOrder);
          ps.setString(3, specialInstruction);
          ps.setInt(4, itemPrescId);
          flag = ps.executeUpdate() == 1;

        } catch (SQLException exe) {
          if (!DataBaseUtil.isDuplicateViolation(exe)) {
            throw exe;
          }
        } finally {
          DataBaseUtil.closeConnections(null, ps);
        }
      } else {
        PreparedStatement ps = null;
        try {
          ps = con.prepareStatement(" INSERT INTO doctor_other_medicine_favourites(favourite_id,"
              + " doctor_id, display_order, "
              + "   medicine_name, frequency, duration, duration_units,"
              + " medicine_quantity, medicine_remarks, route_of_admin, "
              + "   strength, item_form_id, item_strength, item_strength_units,"
              + " consumption_uom, admin_strength, special_instr) "
              + " (select nextval('doctor_other_medicine_favourites_seq'),"
              + " ?, ?, medicine_name, frequency, duration, duration_units, "
              + "   medicine_quantity, medicine_remarks, route_of_admin, strength,"
              + " item_form_id, item_strength, item_strength_units, "
              + "   consumption_uom, admin_strength, ? "
              + "   from patient_other_medicine_prescriptions where prescription_id=?)");
          ps.setString(1, doctorId);
          ps.setInt(2, displayOrder);
          ps.setString(3, specialInstruction);
          ps.setInt(4, itemPrescId);
          flag = ps.executeUpdate() == 1;

        } catch (SQLException se) {
          if (!DataBaseUtil.isDuplicateViolation(se)) {
            throw se;
          }
        } finally {
          DataBaseUtil.closeConnections(null, ps);
        }
      }
    } else if (itemType.equals("Inv.")) {
      PreparedStatement ps = null;
      try {
        ps = con.prepareStatement(" INSERT INTO doctor_test_favourites(favourite_id,"
            + " doctor_id, display_order, " + "   test_id, test_remarks, ispackage, special_instr) "
            + " (select nextval('doctor_test_favourites_seq'), ?, ?, test_id,"
            + " test_remarks, ispackage, ? "
            + "   from patient_test_prescriptions where op_test_pres_id=?)");
        ps.setString(1, doctorId);
        ps.setInt(2, displayOrder);
        ps.setString(3, specialInstruction);
        ps.setInt(4, itemPrescId);
        flag = ps.executeUpdate() == 1;

      } catch (SQLException se) {
        if (!DataBaseUtil.isDuplicateViolation(se)) {
          throw se;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }

    } else if (itemType.equals("Service")) {
      PreparedStatement ps = null;
      try {
        ps = con.prepareStatement(" INSERT INTO doctor_service_favourites(favourite_id,"
            + " doctor_id, display_order, " + "   service_id, service_remarks, special_instr) "
            + " (select nextval('doctor_service_favourites_seq'), ?, ?,"
            + " service_id, service_remarks, ? "
            + "   from patient_service_prescriptions where op_service_pres_id=?)");
        ps.setString(1, doctorId);
        ps.setInt(2, displayOrder);
        ps.setString(3, specialInstruction);
        ps.setInt(4, itemPrescId);
        flag = ps.executeUpdate() == 1;

      } catch (SQLException exe) {
        if (!DataBaseUtil.isDuplicateViolation(exe)) {
          throw exe;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }

    } else if (itemType.equals("Doctor")) {
      PreparedStatement ps = null;
      try {
        ps = con.prepareStatement(" INSERT INTO doctor_consultation_favourites(favourite_id,"
            + " doctor_id, display_order, "
            + "   cons_doctor_id, consultation_remarks, special_instr) "
            + " (select nextval('doctor_consultation_favourites_seq'), ?, ?,"
            + " doctor_id, cons_remarks, ? "
            + "   from patient_consultation_prescriptions where prescription_id=?)");
        ps.setString(1, doctorId);
        ps.setInt(2, displayOrder);
        ps.setString(3, specialInstruction);
        ps.setInt(4, itemPrescId);
        flag = ps.executeUpdate() == 1;

      } catch (SQLException exe) {
        if (!DataBaseUtil.isDuplicateViolation(exe)) {
          throw exe;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }

    } else if (itemType.equals("NonHospital") || (itemType.equals("Medicine") && nonHospMedicine)) {
      PreparedStatement ps = null;
      try {
        ps = con.prepareStatement(" INSERT INTO doctor_other_favourites(favourite_id,"
            + " doctor_id, display_order, "
            + "   item_name, item_remarks, frequency, duration, duration_units,"
            + " medicine_quantity, "
            + "   strength, item_form_id, item_strength, item_strength_units,"
            + " non_hosp_medicine, consumption_uom, admin_strength, special_instr) "
            + " (select nextval('doctor_other_favourites_seq'), ?, ?, item_name,"
            + " item_remarks, frequency, duration, duration_units, "
            + "   medicine_quantity, strength, item_form_id, item_strength,"
            + " item_strength_units, " + "   non_hosp_medicine, consumption_uom, admin_strength, ?"
            + " from patient_other_prescriptions " + "   where prescription_id=?)");
        ps.setString(1, doctorId);
        ps.setInt(2, displayOrder);
        ps.setString(3, specialInstruction);
        ps.setInt(4, itemPrescId);
        flag = ps.executeUpdate() == 1;

      } catch (SQLException exe) {
        if (!DataBaseUtil.isDuplicateViolation(exe)) {
          throw exe;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }

    } else if (itemType.equals("Operation")) {
      PreparedStatement ps = null;
      try {
        ps = con.prepareStatement(" INSERT INTO doctor_operation_favourites(favourite_id,"
            + " doctor_id, display_order, " + "   operation_id, remarks, special_instr) "
            + " (select nextval('doctor_operation_favourites_seq'), ?,"
            + " ?, operation_id, remarks, ? " + "   from patient_operation_prescriptions"
            + " where prescription_id=?)");
        ps.setString(1, doctorId);
        ps.setInt(2, displayOrder);
        ps.setString(3, specialInstruction);
        ps.setInt(4, itemPrescId);
        flag = ps.executeUpdate() == 1;

      } catch (SQLException exe) {
        if (!DataBaseUtil.isDuplicateViolation(exe)) {
          throw exe;
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }
    }
    return flag;
  }

  /** The Constant ADMITTING_DOCTOR_CONSULTATION. */
  private static final String ADMITTING_DOCTOR_CONSULTATION = "SELECT consultation_id"
      + " FROM  doctor_consultation dc " + "   JOIN patient_registration pr"
      + " ON (pr.patient_id=dc.patient_id and dc.doctor_name=pr.doctor)"
      + " WHERE dc.patient_id=? AND"
      + " COALESCE(dc.cancel_status,'') != 'C' order by visited_date limit 1 ";

  /**
   * Gets the admitting doctor consultation id.
   *
   * @param patientId the patient id
   * @return the admitting doctor consultation id
   * @throws SQLException the SQL exception
   */
  public int getAdmittingDoctorConsultationId(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    try {
      return getAdmittingDoctorConsultationId(con, patientId);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the admitting doctor consultation id.
   *
   * @param con       the con
   * @param patientId the patient id
   * @return the admitting doctor consultation id
   * @throws SQLException the SQL exception
   */
  public int getAdmittingDoctorConsultationId(Connection con, String patientId)
      throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ps = con.prepareStatement(ADMITTING_DOCTOR_CONSULTATION);
      ps.setString(1, patientId);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("consultation_id");
      }
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return 0;
  }

  /** The Constant GET_LATEST_PROGRESS_NOTE_DETAILS. */
  public static final String GET_LATEST_PROGRESS_NOTE_DETAILS = "SELECT *"
      + " FROM progress_notes WHERE doctor= ? AND mr_no = ?" + " ORDER BY mod_time DESC";

  /**
   * Gets the latest progress note details.
   *
   * @param doctorName the doctor name
   * @param mrNo       the mr no
   * @return the latest progress note details
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getLatestProgressNoteDetails(String doctorName, String mrNo)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_LATEST_PROGRESS_NOTE_DETAILS);
      ps.setString(1, doctorName);
      ps.setString(2, mrNo);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /** The Constant CONS_CHARGE_DETAILS. */
  private static final String CONS_CHARGE_DETAILS = "SELECT bc.charge_id"
      + " FROM doctor_consultation dc " + " LEFT JOIN bill_activity_charge bac"
      + " ON (bac.activity_id=dc.consultation_id::text and bac.activity_code='DOC')"
      + " LEFT JOIN bill_charge bc ON (bac.charge_id=bc.charge_id) " + " WHERE consultation_id=?";

  /**
   * Gets the consultation charge.
   *
   * @param consultationId the consultation id
   * @return the consultation charge
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getConsultationCharge(int consultationId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(CONS_CHARGE_DETAILS);
      ps.setInt(1, consultationId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant SINGLE_VISIT_CONSULTATION. */
  private static final String SINGLE_VISIT_CONSULTATION = " SELECT dc.* "
      + " FROM doctor_consultation dc " + "   JOIN doctors d ON (dc.doctor_name=d.doctor_id) "
      + " WHERE patient_id=?";

  /**
   * Gets the visit wise single consultation.
   *
   * @param patientId the patient id
   * @return the visit wise single consultation
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getVisitWiseSingleConsultation(String patientId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(SINGLE_VISIT_CONSULTATION);
      ps.setString(1, patientId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

}
