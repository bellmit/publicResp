package com.insta.hms.ipservices;

import com.bob.hms.adminmasters.wardandbed.WardAndBedMasterDao;
import com.bob.hms.common.Constants;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmasters.bedmaster.BedMasterDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SplitSearchQueryBuilder;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DashBoardDAO.
 */
public class DashBoardDAO {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DashBoardDAO.class);

  /**
   * Gets the in patient details.
   *
   * @return the in patient details
   * @throws SQLException the SQL exception
   */
  public List getInPatientDetails() throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement pstmt = con
        .prepareStatement("SELECT PR.MR_NO,b.bill_no,b.bill_type, "
            + " b.status,(SM.SALUTATION||' '||PD.PATIENT_NAME|| PD.LAST_NAME) AS NAME, "
            + " PR.ORG_ID AS PATIENT_ORG_ID,  "
            + " PD.PATIENT_PHONE ,PR.BED_TYPE  "
            + " FROM PATIENT_REGISTRATION PR, bill b, PATIENT_DETAILS PD, SALUTATION_MASTER SM  "
            + " WHERE PR.STATUS ='A' AND PR.CFLAG=0 AND PR.MR_NO = PD.MR_NO "
            + " AND PD.SALUTATION = SM.SALUTATION_ID  "
            + " AND PR.MR_NO "
            + " IN (SELECT PR.MR_NO AS  MR_NO FROM  PATIENT_REGISTRATION PR "
            + " WHERE PR.VISIT_TYPE = 'i' AND PR.STATUS='A' AND PR.CFLAG=0 )  "
            + " AND PR.MR_NO NOT IN (SELECT MR_NO FROM ADMISSION) "
            + " AND b.visit_id=PR.patient_id ORDER BY PR.MR_NO  ");
    
    List result = DataBaseUtil.queryToArrayList(pstmt);
    DataBaseUtil.closeConnections(con, pstmt);
    return result;

  }

  /** The Constant PAT_DETAILS_QUERY. */
  public static final String PAT_DETAILS_QUERY = "SELECT (S.SALUTATION || ' ' "
      + " || PD.PATIENT_NAME || ' ' ||PD.LAST_NAME) AS PATIENT_NAME ,"
      + " D.DOCTOR_NAME,pr.doctor,PR.PATIENT_ID,pd.mr_no,pr.org_id as patient_org_id "
      + " FROM PATIENT_DETAILS PD,DOCTORS D,SALUTATION_MASTER S,PATIENT_REGISTRATION PR  "
      + " WHERE Pd.MR_NO=? and  PD.SALUTATION=S.SALUTATION_ID "
      + " and pd.mr_no = pr.mr_no and pr.status='A' AND D.DOCTOR_ID=pr.doctor";

  /**
   * Gets the patient xml details.
   *
   * @param mrNo the mr no
   * @return the patient xml details
   * @throws SQLException the SQL exception
   */
  public String getpatientXmlDetails(String mrNo) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String strPatientXmlDetails = null;
    try {
      con = DataBaseUtil.getConnection();
      if (!"".equals(mrNo)) {
        ps = con.prepareStatement(PAT_DETAILS_QUERY);
        ps.setString(1, mrNo);
      }
      strPatientXmlDetails = DataBaseUtil.getXmlContentWithNoChild(ps, "PATIENTDETAIL");
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return strPatientXmlDetails;
  }

  /** The Constant insertIntoAdmission. */
  public static final String insertIntoAdmission = "INSERT "
      + " INTO Admission(MR_NO,Patient_ID,Bed_id,"
      + " daycare_status,estimated_days,isbaby,parent_id,"
      + " admit_date,admit_time,daysorhrs,last_updated)"
      + " values(?,?,?,?,?,?,?,?,?,?,?)";

  /**
   * Admit patient.
   *
   * @param con the con
   * @param bdto the bdto
   * @param admitDate the admit date
   * @param admitTime the admit time
   * @param isBaby the is baby
   * @param daycare the daycare
   * @param unit the unit
   * @param endDate the end date
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean admitPatient(Connection con, BedAdmissionDTO bdto, String admitDate,
      Timestamp admitTime, String isBaby, boolean daycare, String unit, Timestamp endDate)
      throws Exception {
    SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy");
    // avoid if baby record is already exists
    if (DataBaseUtil.getStringValueFromDb("SELECT MR_NO FROM Admission WHERE MR_NO=?"
        + " AND PATIENT_ID=?", new Object[] {bdto.getMrNo(), bdto.getPatientid()}) != null) {
      return true;
    }
    try (PreparedStatement ps = con.prepareStatement(insertIntoAdmission)) {
      ps.setString(1, bdto.getMrNo());
      ps.setString(2, bdto.getPatientid());
      ps.setInt(3, bdto.getAdmitbedid());
      ps.setString(4, daycare ? "Y" : "N");
      ps.setFloat(5, bdto.getEstimateddays());
      ps.setString(6, isBaby);
      ps.setString(7, bdto.getParentId());
      ps.setDate(8, new java.sql.Date(formater.parse(admitDate).getTime()));
      ps.setTimestamp(9, admitTime);
      ps.setString(10, unit);
      ps.setTimestamp(11, endDate);
      int result = ps.executeUpdate();
      return result != 0;
    }
  }

  // New Query - removed joins to bill, bed_names, ward_names and ord_id field ....if something goes
  /** The getadmittedpatients. */
  // wrong, need to revisit this
  private StringBuilder getadmittedpatients = new StringBuilder(
      "SELECT pvd.mr_no,pvd.patient_full_name as patient_name, pvd.patient_id, "
          + " COALESCE( pvd.bed_name , 'Allocate Bed' ) AS bed_name,"
          + " pvd.bed_type,pvd.ward_name ,pvd.bed_id, "
          + " (CASE WHEN pvd.patient_gender='F' THEN 'FEMALE' "
          + " WHEN pvd.patient_gender='M'THEN 'MALE' ELSE 'NONE' END ) AS patient_gender"
          + " TO_CHAR(pvd.reg_date,'DD-MM-YYYY') AS admitdate, "
          + " TO_CHAR(pvd.reg_time,'HH:MM:SS AM') AS ADMISSION_TIME, "
          + " pvd.doctor_name, pvd.patient_age, pvd.patient_age_in, pvd.ready_to_discharge,"
          + " a.estimated_days, a.daycare_status,a.isbaby  "
          + " FROM patient_visit_details_ext_view pvd  "
          + " left join admission a using (patient_id) "
          + " WHERE pvd.visit_type_name = 'IP'and  pvd.status='A' ");

  /** The Constant PATIENT_ADVANCES_QUERY. */
  private static final String PATIENT_ADVANCES_QUERY = " SELECT "
      + " b.visit_id, b.approval_amount, b.total_amount AS amount, b.total_receipts  AS receipt "
      + " FROM patient_registration pr "
      + " JOIN bill b ON (b.visit_id = pr.patient_id AND b.bill_type = 'C' "
      + " AND b.restriction_type = 'N' AND b.status != 'X') "
      + " WHERE pr.status = 'A' AND pr.visit_type = 'i'";

  /**
   * Gets the advances.
   *
   * @return the advances
   * @throws SQLException the SQL exception
   */
  public static List getAdvances() throws SQLException {
    ArrayList list = new ArrayList();
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(PATIENT_ADVANCES_QUERY);
        ResultSet rs = ps.executeQuery();) {
      while (rs.next()) {
        Hashtable hashtable = new Hashtable();
        hashtable.put("VISIT_ID", rs.getString("VISIT_ID"));
        hashtable.put("AMOUNT", rs.getInt("AMOUNT"));
        hashtable.put("RECEIPT", rs.getInt("RECEIPT"));
        if (rs.getBigDecimal("APPROVAL_AMOUNT") != null) {
          hashtable.put("APPROVAL_AMOUNT", rs.getBigDecimal("APPROVAL_AMOUNT"));
        }
        list.add(hashtable);
      }
    }
    return list;
  }

  /**
   * Gets the advances.
   *
   * @param patientId the patient id
   * @return the advances
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAdvances(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(PATIENT_ADVANCES_QUERY + " and b.visit_id = ?");
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

  /** The Constant GET_BILL_ADVANCE. */
  private static final String GET_BILL_ADVANCE = "select  "
      + " (sum(b.total_receipts)-sum(b.total_amount)) as amt from bill b "
      + " JOIN patient_registration pr on (pr.patient_id = b.visit_id ) "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND "
      + " patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
      + " where pr.status='A' and b.visit_id=?";

  /**
   * Gets the advance balance.
   *
   * @param patientId the patient id
   * @return the advance balance
   * @throws Exception the exception
   */
  public int getAdvanceBalance(String patientId) throws Exception {
    int advancebalance = 0;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(GET_BILL_ADVANCE);) {
      ps.setString(1, patientId);
      try (ResultSet set = ps.executeQuery()) {
        while (set.next()) {
          advancebalance = set.getInt("AMT");
        }
      }
      return advancebalance;
    }
  }

  /**
   * Gets the bed details.
   *
   * @param patientId the patient id
   * @return the bed details
   * @throws Exception the exception
   */
  public int getBedDetails(String patientId) throws Exception {
    BasicDynaBean details = VisitDetailsDAO.getPatientVisitDetailsBean(patientId);
    String icubed = DataBaseUtil
        .getStringValueFromDb("SELECT DISTINCT intensive_bed_type FROM icu_bed_charges "
            + " WHERE intensive_bed_type=?", details.get("bill_bed_type").toString());
    List bedDetails = null;
    int initialPayment = 0;
    String bedType = "BED_TYPE";
    if (icubed != null) {
      bedDetails = new BedMasterDAO().getNormalBedCharge(details.get("bill_bed_type").toString(),
          details.get("org_id").toString());
    } else {
      bedDetails = new BedMasterDAO().getIcuWardCharges(details.get("bill_bed_type").toString(),
          details.get("org_id").toString());
      bedType = "INTENSIVE_BED_TYPE";
    }
    for (int i = 0; i < bedDetails.size(); i++) {
      Hashtable table = (Hashtable) bedDetails.get(i);
      if (details.get("bill_bed_type").toString()
          .equalsIgnoreCase(table.get(bedType).toString())) {
        initialPayment = Integer.parseInt(table.get("INITIAL_PAYMENT").toString());
      }
    }
    return initialPayment;
  }

  /** The Constant ADT_SEARCH. */
  // add all the searchable, orderable, and sortable columns here.
  private static final String ADT_SEARCH = " SELECT pr.mr_no, bn.ward_no, "
      + " vct.care_doctor_id as doctor_id, pr.reg_date + pr.reg_time AS admit_date, "
      + "  get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + "   AS patient_name, COALESCE(bn.bed_name, 'Allocate Bed') AS bed_name, "
      + "  pr.patient_id, pr.visit_type, pr.status, pr.center_id, "
      + "  CASE WHEN ibn.bed_name IS NOT NULL THEN 'Y' ELSE 'N' END as is_icu_type,"
      + "  pr.patient_discharge_status"
      + " FROM patient_registration pr "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND visit_type = 'i' "
      + " AND pr.status='A' AND (patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation) "
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id) "
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "
      + " LEFT JOIN bed_types bt ON(bn.bed_type = bt.bed_type_name) "
      + " LEFT JOIN (SELECT * FROM bed_names  WHERE bed_type "
      + " IN (SELECT intensive_bed_type FROM icu_bed_charges )) "
      + " AS ibn ON (ibn.bed_id = bn.bed_id AND  ibn.occupancy = 'Y' AND ibn.status = 'A' )"
      + " LEFT JOIN visit_care_team vct ON(vct.patient_id = pr.patient_id  #)";

  /** The Constant IN_PATIENTS_SEARCH. */
  private static final String IN_PATIENTS_SEARCH = " SELECT pr.mr_no, bn.ward_no, "
      + " vct.care_doctor_id as doctor_id, pr.reg_date + pr.reg_time AS admit_date, "
      + " get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + " AS patient_name, COALESCE(bn.bed_name, 'Allocate Bed') AS bed_name, "
      + " pr.patient_id, pr.visit_type, pr.status, pr.center_id, "
      + " pr.patient_discharge_status"
      + " FROM patient_registration pr "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND visit_type = 'i'AND "
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation) "
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id) "
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "
      + " LEFT JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id  #) ";

  /** The Constant ADT_FIELDS. */
  private static final String ADT_FIELDS = " SELECT pr.mr_no, bn.ward_no, "
      + " vct.care_doctor_id as doctor_id , pr.reg_date + pr.reg_time AS admit_date, "
      + " get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + " AS patient_name, "
      + " pr.patient_id, pd.vip_status, COALESCE(bn.bed_name, 'Allocate Bed') AS bed_name, "
      + " bn.bed_type, COALESCE(wn.ward_name,'') as ward_name, COALESCE(bn.ward_no,'') as ward_no, "
      + " COALESCE(adm.bed_id,0) AS bed_id, "
      + " (CASE WHEN pd.patient_gender='F' THEN 'FEMALE' WHEN pd.patient_gender='M' THEN 'MALE' "
      + "  ELSE 'NONE' END ) AS patient_gender, "
      + " pr.dept_name as dept_id, dep.dept_name,dep.dept_type_id, pr.org_id, doc.doctor_name, "
      + " get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age, "
      + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_in, "
      + " pr.ready_to_discharge, adm.estimated_days, wn.ward_name as ward, "
      + " adm.daycare_status, COALESCE(adm.isbaby,'') AS isbaby, adm.parent_id, "
      + " pr.discharge_doc_id, pr.discharge_format, pr.status, vtn.visit_type_name, "
      + " adtv.bill_status_ok, adtv.payment_ok, adtv.credit_bill_exists, adtv.total_amount, "
      + " adtv.total_receipts, adtv.approval_amount, "
      + " bt.is_icu, ibd.bed_state, pr.center_id,CASE WHEN ibn.bed_name IS NOT NULL THEN 'Y' "
      + " ELSE 'N' END as is_icu_type, "
      + " pr.patient_discharge_status "
      + " FROM patient_registration pr "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND "
      + " ( patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation) "
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id) "
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "
      + " LEFT JOIN bed_types bt ON (bn.bed_type = bt.bed_type_name) "
      + " LEFT JOIN (SELECT * FROM bed_names  WHERE bed_type "
      + " IN (SELECT intensive_bed_type FROM icu_bed_charges )) "
      + " AS ibn ON (ibn.bed_id = bn.bed_id AND  ibn.occupancy = 'Y' AND ibn.status = 'A' )"
      + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) "
      + " LEFT JOIN department dep ON (dep.dept_id = pr.dept_name) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) "
      + " JOIN visit_type_names vtn ON (vtn.visit_type = pr.visit_type) "
      + " LEFT JOIN adt_bill_and_discharge_status_view adtv ON (adtv.visit_id = pr.patient_id) "
      + " LEFT JOIN ip_bed_details ibd ON(ibd.patient_id = pr.patient_id "
      + " AND ibd.status IN ('C','A')) "
      + " LEFT JOIN visit_care_team vct ON(vct.patient_id = pr.patient_id  #)";

  /** The Constant IN_PATIENTS_DETAILS_FIELDS. */
  private static final String IN_PATIENTS_DETAILS_FIELDS = " SELECT pr.mr_no, "
      + " adtv.credit_bill_exists, adtv.bill_status_ok, adtv.payment_ok, "
      + " dep.dept_type_id, pr.patient_id, vtn.visit_type_name, pr.org_id, "
      + " (CASE WHEN pd.patient_gender='F' THEN 'FEMALE' "
      + " WHEN pd.patient_gender='M' THEN 'MALE' ELSE 'NONE' END ) AS patient_gender, "
      + " pr.dept_name as dept_id, "
      + " get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age, "
      + " COALESCE(adm.isbaby,'') AS isbaby, "
      + " get_patient_full_name(s.salutation, pd.patient_name, pd.middle_name, pd.last_name) "
      + " AS patient_name, COALESCE(wn.ward_name,'') as ward_name, wn.ward_name as ward, "
      + " COALESCE(bn.bed_name, 'Allocate Bed') AS bed_name, "
      + " pr.reg_date + pr.reg_time AS admit_date, doc.doctor_name, "
      + " pr.patient_discharge_status, pr.status "
      + " FROM patient_registration pr "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND "
      + " (patient_confidentiality_check(pd.patient_group,pd.mr_no) )) "
      + " LEFT JOIN salutation_master s ON (s.salutation_id = pd.salutation) "
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id) "
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) "
      + " LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) "
      + " LEFT JOIN department dep ON (dep.dept_id = pr.dept_name) "
      + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor) "
      + " JOIN visit_type_names vtn ON (vtn.visit_type = pr.visit_type) "
      + " LEFT JOIN adt_bill_and_discharge_status_view adtv ON (adtv.visit_id = pr.patient_id) "
      + " LEFT JOIN visit_care_team vct ON (vct.patient_id = pr.patient_id  #) ";

  /**
   * Gets the inpatient list.
   *
   * @param filter the filter
   * @param listing the listing
   * @param centerId the center id
   * @param multicentered the multicentered
   * @param isDoctorLogin the is doctor login
   * @param doctorId the doctor id
   * @param applyNurseRules the apply nurse rules
   * @param userName the user name
   * @return the inpatient list
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getInpatientList(Map filter, Map listing, int centerId, boolean multicentered,
      boolean isDoctorLogin, String doctorId, boolean applyNurseRules, String userName)
      throws SQLException, ParseException {
    Connection con = null;
    SplitSearchQueryBuilder qb = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String inPatientsFieldsReplaced = null;
      
      String inPatientsSearchReplaced = null;
      
      if (isDoctorLogin) {
        inPatientsSearchReplaced = IN_PATIENTS_SEARCH
            .replace("#", " AND vct.care_doctor_id = '"
            + doctorId + "'");
        inPatientsFieldsReplaced = IN_PATIENTS_DETAILS_FIELDS.replace("#",
            " AND vct.care_doctor_id = '" + doctorId + "'");

      } else {
        inPatientsSearchReplaced = IN_PATIENTS_SEARCH
            + (applyNurseRules ? " JOIN  nurse_ward_assignments nwa "
                + " ON(COALESCE(bn.ward_no,pr.ward_id) = nwa.ward_id AND nwa.emp_username = '"
                + userName + "' )"
                : "");
        inPatientsSearchReplaced = inPatientsSearchReplaced.replace("#",
            " AND vct.care_doctor_id = pr.doctor");

        inPatientsFieldsReplaced = IN_PATIENTS_DETAILS_FIELDS
            + (applyNurseRules ? " JOIN  nurse_ward_assignments nwa "
                + " ON(COALESCE(bn.ward_no,pr.ward_id) = nwa.ward_id AND nwa.emp_username = '"
                + userName + "' )"
                : "");
        inPatientsFieldsReplaced = inPatientsFieldsReplaced.replace("#",
            " AND vct.care_doctor_id = pr.doctor");
      }
      qb = new SplitSearchQueryBuilder(con, inPatientsSearchReplaced.toString(),
          inPatientsFieldsReplaced.toString(), null, "pr.patient_id", listing);

      qb.addFilterFromParamMap(filter);
      if (multicentered && centerId != 0) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      qb.addSecondarySort("mr_no", false);
      qb.addSecondarySort("status", false);

      qb.build();
      return qb.getMappedPagedList();

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the admitted patient details 1.
   *
   * @param filter the filter
   * @param listing the listing
   * @param centerId the center id
   * @param multicentered the multicentered
   * @return the admitted patient details 1
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getAdmittedPatientDetails1(Map filter, Map listing, int centerId,
      boolean multicentered) throws SQLException, ParseException {
    return getAdmittedPatientDetails1(filter, listing, centerId, multicentered, false, null, false,
        null);
  }

  /**
   * Gets the admitted patient details 1.
   *
   * @param filter the filter
   * @param listing the listing
   * @param centerId the center id
   * @param multicentered the multicentered
   * @param isDoctorLogin the is doctor login
   * @param doctorId the doctor id
   * @param applyNurserules the apply nurserules
   * @param userName the user name
   * @return the admitted patient details 1
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public PagedList getAdmittedPatientDetails1(Map filter, Map listing, int centerId,
      boolean multicentered, boolean isDoctorLogin, String doctorId, boolean applyNurserules,
      String userName) throws SQLException, ParseException {
    Connection con = null;
    SplitSearchQueryBuilder qb = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      String inPatientsFieldsReplaced = null;
      String inPatientsSearchReplaced = null;
      
      if (isDoctorLogin) {
        inPatientsSearchReplaced = ADT_SEARCH.replace("#", " AND vct.care_doctor_id = '"
            + doctorId + "'");
        inPatientsFieldsReplaced = ADT_FIELDS.replace("#", " AND vct.care_doctor_id = '"
            + doctorId + "'");

      } else {
        inPatientsSearchReplaced = ADT_SEARCH
            + (applyNurserules ? " JOIN  nurse_ward_assignments nwa "
                + " ON(COALESCE(bn.ward_no,pr.ward_id) = nwa.ward_id AND nwa.emp_username = '"
                + userName + "' )"
                : "");
        inPatientsSearchReplaced = inPatientsSearchReplaced.replace("#",
            " AND vct.care_doctor_id = pr.doctor");

        inPatientsFieldsReplaced = ADT_FIELDS
            + (applyNurserules ? " JOIN  nurse_ward_assignments nwa "
                + " ON(COALESCE(bn.ward_no,pr.ward_id) = nwa.ward_id AND nwa.emp_username = '"
                + userName + "' )"
                : "");
        inPatientsFieldsReplaced = inPatientsFieldsReplaced.replace("#",
            " AND vct.care_doctor_id = pr.doctor");

      }
      qb = new SplitSearchQueryBuilder(con, inPatientsSearchReplaced.toString(),
          inPatientsFieldsReplaced.toString(), null, "pr.patient_id", listing);

      qb.addFilterFromParamMap(filter);
      if (multicentered && centerId != 0) {
        qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
      }
      qb.addSecondarySort("mr_no", false);
      qb.addSecondarySort("status", false);

      qb.build();
      return qb.getMappedPagedList();

    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the admitted patient details.
   *
   * @param offSetVal the off set val
   * @return the admitted patient details
   * @throws SQLException the SQL exception
   */
  public List getAdmittedPatientDetails(int offSetVal) throws SQLException {
    Connection con = null;
    PreparedStatement pst = null;
    try {
      con = DataBaseUtil.getConnection();
      getadmittedpatients.append("  ORDER BY PD.MR_NO LIMIT 15 OFFSET ? ");

      pst = con.prepareStatement(getadmittedpatients.toString());
      pst.setInt(1, offSetVal);
      List patientlist = DataBaseUtil.queryToArrayList(pst);
      return patientlist;
    } finally {
      DataBaseUtil.closeConnections(con, pst);
    }

  }

  /** The getcount. */
  private StringBuilder getcount = new StringBuilder(
      "SELECT COUNT(*) FROM PATIENT_DETAILS PD,PATIENT_REGISTRATION PR   "
      + " WHERE  PR.STATUS='A' AND PD.MR_NO=PR.MR_NO and pr.visit_type = 'i' ");

  /**
   * Fetch no of records.
   *
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int fetchNoOfRecords() throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    int count = 0;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(getcount.toString());
      rs = ps.executeQuery();
      while (rs.next()) {
        count = Integer.parseInt(rs.getString(1));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return count;
  }

  /**
   * Fetch no of records based on search.
   *
   * @param sdto the sdto
   * @param wardName the ward name
   * @return the int
   * @throws SQLException the SQL exception
   */
  public int fetchNoOfRecordsBasedOnSearch(SearchDTO sdto, String wardName) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    ResultSet rs = null;
    int count = 0;
    try {
      con = DataBaseUtil.getConnection();
      if (!(wardName.equals(""))) {
        getcount.append(" and pr.ward_name=?");
        ps = con.prepareStatement(getcount.toString());
        ps.setString(1, wardName);
      } else if (sdto.getFirstName() != null && !"".equals(sdto.getFirstName())) {
        getcount.append(" and  pd.patient_name like ?");
        ps = con.prepareStatement(getcount.toString());
        String firstName = sdto.getFirstName() + "%";
        ps.setString(1, firstName);
      } else if (sdto.getLastName() != null && !"".equals(sdto.getLastName())) {
        getcount.append(" and  pd.last_name like ?");
        ps = con.prepareStatement(getcount.toString());
        String lastName = sdto.getLastName() + "%";
        ps.setString(1, lastName);
      } else if (sdto.getSearchdoctor() != null && !"".equals(sdto.getSearchdoctor())) {
        getcount.append(" and pr.doctor=?");
        ps = con.prepareStatement(getcount.toString());
        ps.setString(1, sdto.getSearchdoctor());
      } else if (sdto.getSearchmrno() != null && !"".equals(sdto.getSearchmrno())) {
        getcount.append(" and pr.mr_no=?");
        ps = con.prepareStatement(getcount.toString());
        ps.setString(1, sdto.getSearchmrno());
      } else {
        ps = con.prepareStatement(getcount.toString());
      }

      rs = ps.executeQuery();
      while (rs.next()) {
        count = Integer.parseInt(rs.getString(1));
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
    return count;
  }

  /**
   * Gets the doctors.
   *
   * @return the doctors
   * @throws SQLException the SQL exception
   */
  public List getDoctors() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int centerID = RequestContext.getCenterId();
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(new DoctorMasterDAO().getDoctorNames());
      ps.setInt(1, centerID);
      List doctorList = DataBaseUtil.queryToArrayList(ps);

      return doctorList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the bed charge.
   *
   * @param bed the bed
   * @param org the org
   * @return the bed charge
   * @throws SQLException the SQL exception
   */
  public String getBedCharge(String bed, String org) throws SQLException {
    Connection con = null;
    String bedCharge = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("select "
              + " (coalesce(bd.bed_charge,0) + coalesce(bd.nursing_charge,0) "
              + " + coalesce(bd.duty_charge,0)"
              + " + coalesce(bd.maintainance_charge,0)) as bed_charge "
              + " from bed_details  bd where bd.bed_type=? and  bd.organization=?"
              + " UNION "
              + " select (coalesce(ibc.bed_charge,0) + coalesce(ibc.duty_charge,0) "
              + " + coalesce(ibc.maintainance_charge,0) "
              + " + coalesce(ibc.nursing_charge,0)) as bed_charge "
              + " from  icu_bed_charges ibc "
              + " where ibc.intensive_bed_type=? and "
              + " ibc.organization=?");
      ps.setString(1, bed);
      ps.setString(2, org);
      ps.setString(3, bed);
      ps.setString(4, org);
      rs = ps.executeQuery();
      while (rs.next()) {
        bedCharge = rs.getString(1);
      }
      return bedCharge;
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }

  }

  /**
   * Gets the patient details for prescription.
   *
   * @param mrno the mrno
   * @return the patient details for prescription
   * @throws SQLException the SQL exception
   */
  public List getPatientDetailsForPrescription(String mrno) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String patdetails = "SELECT (S.SALUTATION || ' ' || PD.PATIENT_NAME || ' ' "
          + " ||PD.LAST_NAME) AS PATIENT_NAME ,"
          + " D.DOCTOR_NAME,pr.doctor,pr.DEPT_NAME,PR.PATIENT_ID,pd.mr_no,pr.org_id "
          + " as patient_org_id,get_patient_age(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE, "
          + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE_IN,"
          + " (case when PD.PATIENT_GENDER='F' then 'Female' "
          + " when PD.PATIENT_GENDER='M'then 'Male' else 'none' end ) as PATIENT_GENDER,"
          + " PD.PATIENT_PHONE FROM PATIENT_DETAILS PD,DOCTORS D,SALUTATION_MASTER S,"
          + " PATIENT_REGISTRATION PR  WHERE Pd.MR_NO=? AND PD.SALUTATION=S.SALUTATION_ID and "
          + " pd.mr_no = pr.mr_no and pr.status='A' AND D.DOCTOR_ID=pr.doctor";
      ps = con.prepareStatement(patdetails);
      ps.setString(1, mrno);
      List patientDetailsList = DataBaseUtil.queryToArrayList(ps);
      return patientDetailsList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the patient details for prescription OP.
   *
   * @param mrno the mrno
   * @return the patient details for prescription OP
   * @throws SQLException the SQL exception
   */
  public List getPatientDetailsForPrescriptionOP(String mrno) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String patdetails = "SELECT (S.SALUTATION || ' ' "
          + " || PD.PATIENT_NAME || ' ' ||PD.LAST_NAME) AS PATIENT_NAME "
          + ",pr.DEPT_NAME,PR.PATIENT_ID,pd.mr_no,pr.org_id as patient_org_id,"
          + " get_patient_age(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE"
          + ", get_patient_age_in(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE_IN,"
          + "(case when PD.PATIENT_GENDER='F' then 'Female' "
          + " when PD.PATIENT_GENDER='M'then 'Male' else 'none' end ) as PATIENT_GENDER"
          + ",PD.PATIENT_PHONE FROM PATIENT_DETAILS PD,SALUTATION_MASTER S,"
          + " PATIENT_REGISTRATION PR  WHERE "
          + " Pd.MR_NO=? AND PD.SALUTATION=S.SALUTATION_ID "
          + " and pd.mr_no = pr.mr_no and pr.status='A' ";
      ps = con.prepareStatement(patdetails);
      ps.setString(1, mrno);
      List patientDetailsList = DataBaseUtil.queryToArrayList(ps);
      return patientDetailsList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant getDeptWiseTests. */
  public static final String getDeptWiseTests = "SELECT  DIAG.TEST_ID, "
      + " DIAG.TEST_NAME, DIAG.DDEPT_ID, D.DDEPT_NAME,D.CATEGORY,"
      + " dc.charge,dc.org_name,dc.bed_type FROM  DIAGNOSTICS DIAG "
      + " JOIN DIAGNOSTICS_DEPARTMENTS D  ON(D.DDEPT_ID = DIAG.DDEPT_ID)"
      + " JOIN diagnostic_charges dc on(dc.test_id=Diag.test_id and dc.priority='R') "
      + " WHERE  dc.org_name=? and dc.bed_type  =? ORDER BY TEST_NAME";

  /**
   * Gets the dept wise tests.
   *
   * @param org the org
   * @param bedtype the bedtype
   * @return the dept wise tests
   * @throws SQLException the SQL exception
   */
  public List getDeptWiseTests(String org, String bedtype) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(getDeptWiseTests);
      ps.setString(1, org);
      ps.setString(2, bedtype);
      List getDeptWiseTestsList = DataBaseUtil.queryToArrayList(ps);
      DataBaseUtil.closeConnections(con, ps);
      return getDeptWiseTestsList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the ward wise patients.
   *
   * @param wardname the wardname
   * @param offSetVal the off set val
   * @return the ward wise patients
   * @throws SQLException the SQL exception
   */
  public List getWardWisePatients(String wardname, int offSetVal) throws SQLException {
    List wardwiselist = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      if (!"".equals(wardname)) {
        con = DataBaseUtil.getConnection();

        String wardWise = getadmittedpatients + " and PR.WARD_NAME=? LIMIT 15 OFFSET ?";
        ps = con.prepareStatement(wardWise);
        ps.setString(1, wardname);
        ps.setInt(2, offSetVal);
        wardwiselist = DataBaseUtil.queryToArrayList(ps);
      } else {
        wardwiselist = getAdmittedPatientDetails(offSetVal);
      }
      return wardwiselist;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Gets the search data.
   *
   * @param sdto the sdto
   * @param offSetVal the off set val
   * @return the search data
   * @throws SQLException the SQL exception
   */
  public List getSearchData(SearchDTO sdto, int offSetVal) throws SQLException {

    if (sdto.getLastName() != null && !"".equals(sdto.getLastName())) {
      getadmittedpatients.append(" AND (PD.LAST_NAME like '" + "%" + sdto.getLastName() + "%"
          + "')");
    } else if (sdto.getFirstName() != null && !"".equals(sdto.getFirstName())) {
      getadmittedpatients.append(" AND (PD.PATIENT_NAME like '" + "%" + sdto.getFirstName() + "%"
          + "')");
    } else if (sdto.getSearchdoctor() != null && !"".equals(sdto.getSearchdoctor())) {
      getadmittedpatients.append(" AND (PR.DOCTOR ='" + sdto.getSearchdoctor() + "')");
    } else if (sdto.getSearchmrno() != null && !"".equals(sdto.getSearchmrno())) {
      getadmittedpatients.append(" AND (PR.MR_NO ='" + sdto.getSearchmrno() + "')");
    }
    return getAdmittedPatientDetails(offSetVal);
  }

  /**
   * Gets the medicine names.
   *
   * @return the medicine names
   * @throws SQLException the SQL exception
   */
  public List getMedicineNames() throws SQLException {
    List medicineList = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement("select * from common_medicine_charge_master where status = 'A'");
      medicineList = DataBaseUtil.queryToArrayList1(ps);
      return medicineList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);

    }
  }

  /** 
   * This method to get wardname.
   *
   * @return ArrayList
   */

  private static final String GET_WARD_NAME = "SELECT * "
      + " FROM WARD_NAMES where status='A' ORDER BY WARD_NAME";

  /**
   * Gets the ward name.
   *
   * @return the ward name
   */
  public ArrayList getWardName() {

    return DataBaseUtil.queryToArrayList(GET_WARD_NAME);
  }

  /**
   * Shift bed.
   *
   * @param con the con
   * @param bdto the bdto
   * @param dualoccupancy the dualoccupancy
   * @param datetime the datetime
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean shiftBed(Connection con, IpBedDetailsDTO bdto, boolean dualoccupancy,
      Timestamp datetime) throws Exception {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE ADMISSION SET bed_id=? ,"
              + " estimated_days=?,last_updated=? WHERE mr_no=? and patient_id=?");
      ps.setInt(1, bdto.getShiftbedid());
      ps.setInt(2, bdto.getShiftexpecteddays());
      ps.setTimestamp(3, bdto.getLastUpdated());
      ps.setString(4, bdto.getMrno());
      ps.setString(5, bdto.getPatientid());
      int result = ps.executeUpdate();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }

  /**
   * Insert in shift.
   *
   * @param con the con
   * @param bdto the bdto
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertInShift(Connection con, BedAdmissionDTO bdto) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("INSERT INTO bed_shifting(mr_no, patient_id, prev_bed_name, "
              + " prev_ward_name, admission_date, admission_time,  new_bed_name, new_ward_name, "
              + " shift_date, shift_time,  rstatus) VALUES (?, ?, ?, ?, "
              + " CURRENT_DATE,LOCALTIMESTAMP(0),  ?, ?, CURRENT_DATE,LOCALTIMESTAMP(0),  ?)");
      ps.setString(1, bdto.getMrNo());
      ps.setString(2, bdto.getPatientid());
      ps.setString(3, bdto.getBednumber());
      ps.setString(4, bdto.getWard());
      ps.setString(5, bdto.getBednumber());
      ps.setString(6, bdto.getWard());
      ps.setString(7, "N");
      int result = ps.executeUpdate();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /**
   * Insert in ip bed details.
   *
   * @param con the con
   * @param bdto the bdto
   * @param status the status
   * @param admitid the admitid
   * @param mode the mode
   * @param startdate the startdate
   * @param state the state
   * @param username the username
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean insertInIpBedDetails(Connection con, BedAdmissionDTO bdto, String status,
      int admitid, String mode, Timestamp startdate, String state, String username)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("INSERT INTO ip_bed_details  "
              + " (mrno, patient_id, bed_id, start_date, status, "
              + " admit_id,charge_group,bed_state,updated_date,"
              + " username,duty_doctor_id,is_bystander,admitted_by,remarks) "
              + " values(?,?,?,?,?,?,?,?,LOCALTIMESTAMP(0),?,?,?,?,?)");
      ps.setString(1, bdto.getMrNo());
      ps.setString(2, bdto.getPatientid());
      ps.setInt(3, bdto.getAdmitbedid());
      ps.setString(4, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
          .format(new java.util.Date(startdate.getTime())));
      ps.setString(5, status);
      ps.setInt(6, admitid);
      ps.setString(7, bdto.getGroup());
      ps.setString(8, state);
      ps.setString(9, username);
      ps.setString(10, bdto.getDuty_doctor_id());
      ps.setBoolean(11, bdto.isBystanderBed());
      ps.setString(12, username);
      ps.setString(13, bdto.getRemarks());
      int result = ps.executeUpdate();
      ps.close();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /**
   * Gets the previous beds.
   *
   * @param mrno the mrno
   * @param patientid the patientid
   * @return the previous beds
   * @throws SQLException the SQL exception
   */
  public List getPreviousBeds(String mrno, String patientid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("SELECT W.WARD_NAME,B.BED_NAME,B.BED_ID,ip.bed_state,"
              + "ip.START_DATE,ip.END_DATE,"
              + "(CASE WHEN ip.STATUS='X' THEN 'disabled' ELSE '' END)AS cancled,"
              + "(CASE  WHEN ip.STATUS='A' THEN 'Admitted Bed' "
              + " WHEN ip.STATUS='P' THEN "
              + "(CASE WHEN ip.is_bystander  THEN 'ByStander Bed' ELSE 'Previous Bed' END) "
              + " WHEN ip.STATUS='C' THEN 'Current Bed' WHEN ip.STATUS='R' THEN  "
              + " (CASE WHEN ip.is_bystander  THEN 'ByStander Bed' ELSE 'Retained Bed' END)  "
              + " WHEN ip.status='X' THEN 'Cancelled Bed' END) AS STATUS,"
              + " ip.admit_id,ip.duty_doctor_id,"
              + " username,admitted_by,ip.remarks,ip.updated_date,ip.is_bystander,"
              + " ip.charged_bed_type,"
              + " ip.status as bed_status,ip.estimated_days,d.doctor_name,"
              + " ip.charged_bed_type, date_part('days',( case when end_date is null "
              + " then ( CASE when LOCALTIMESTAMP(0) < start_date then start_date "
              + " else LOCALTIMESTAMP(0) end) else end_date end) "
              + " - start_date):: integer AS days, date_part('hour', "
              + " ( case when end_date is null then ( "
              + " CASE when LOCALTIMESTAMP(0) < start_date then start_date "
              + " else LOCALTIMESTAMP(0) end) else end_date end) - start_date):: integer "
              + " AS hours, date_part('minute', "
              + " ( case when end_date is null then ( "
              + " CASE when LOCALTIMESTAMP(0) < start_date then start_date "
              + " else LOCALTIMESTAMP(0) end) else end_date end) "
              + "   - start_date):: integer AS mins, bt.is_icu   "
              + " FROM IP_BED_DETAILS ip "
              + " JOIN BED_NAMES B ON b.bed_id=ip.bed_id "
              + " JOIN bed_types bt on (b.bed_type = bt.bed_type_name) "
              + " JOIN ward_names w ON w.ward_no = b.ward_no "
              + " LEFT JOIN doctors d on (d.doctor_id = ip.duty_doctor_id)  WHERE mrno=? "
              + " AND ip.PATIENT_ID=? ORDER BY start_date,end_date");
      ps.setString(1, mrno);
      ps.setString(2, patientid);
      List bedslist = DataBaseUtil.queryToDynaList(ps);
      return bedslist;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Extend bed.
   *
   * @param con the con
   * @param dto the dto
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean extendBed(Connection con, IpBedDetailsDTO dto) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE ADMISSION SET ESTIMATED_DAYS=ESTIMATED_DAYS+?,"
              + " LAST_UPDATED=? WHERE MR_NO=? AND PATIENT_ID=?");
      ps.setFloat(1, Float.parseFloat(dto.getExtendeddays()));
      ps.setTimestamp(2, dto.getLastUpdated());
      ps.setString(3, dto.getMrno());
      ps.setString(4, dto.getPatientid());
      int result = ps.executeUpdate();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /**
   * Update enddate.
   *
   * @param con the con
   * @param bdto the bdto
   * @param bedId the bed id
   * @param endDate the end date
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateEnddate(Connection con, BedAdmissionDTO bdto, int bedId,
      Timestamp endDate, String userName) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE ip_bed_details SET STATUS=? , "
              + " END_DATE=?,updated_date=LOCALTIMESTAMP(0), username=?,bed_state=?"
              + " WHERE MRNO=? AND PATIENT_ID=? AND BED_ID=?");
      ps.setString(1, "P");
      ps.setTimestamp(2, endDate);
      ps.setString(3, userName);
      ps.setString(4, "F");
      ps.setString(5, bdto.getMrNo());
      ps.setString(6, bdto.getPatientid());
      ps.setInt(7, bedId);
      int result = ps.executeUpdate();

      ps.close();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }
  
  /**
   * Update enddate.
   *
   * @param con the con
   * @param mrno the mrno
   * @param patid the patid
   * @param bedno the bedno
   * @param enddate the enddate
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateEnddate(Connection con, String mrno, String patid, String bedno,
      Timestamp enddate, String userName) throws SQLException {
    PreparedStatement ps = null;
    boolean result = false;
    try {
      ps = con
          .prepareStatement("UPDATE IP_BED_DETAILS SET END_DATE=?,"
              + " UPDATED_DATE = LOCALTIMESTAMP(0),USERNAME = ? "
              + " WHERE MRNO=? AND PATIENT_ID=? AND BED_NO=?");
      result = true;
      ps.setTimestamp(1, enddate);
      ps.setString(2, userName);
      ps.setString(3, mrno);
      ps.setString(4, patid);
      ps.setString(5, bedno);
      if (ps.executeUpdate() <= 0) {
        result = false;
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return result;
  }

  /**
   * Update the end date of the bed only if it is retained bed.
   *
   * @param con the con
   * @param bdto the bdto
   * @param status the status
   * @param bedId the bed id
   * @param endDate the end date
   * @param retained the retained
   * @param state the state
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateEnddate(Connection con, BedAdmissionDTO bdto, String status, int bedId,
      Timestamp endDate, String retained, String state, String userName) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE ip_bed_details SET STATUS=? , END_DATE=?,"
              + " bed_state=?,UPDATED_DATE=LOCALTIMESTAMP(0) , USERNAME=? "
              + " WHERE MRNO=? AND PATIENT_ID=? AND BED_ID=? AND STATUS=?");
      ps.setString(1, status);
      ps.setTimestamp(2, endDate);
      ps.setString(3, state);
      ps.setString(4, userName);
      ps.setString(5, bdto.getMrNo());
      ps.setString(6, bdto.getPatientid());
      ps.setInt(7, bedId);
      ps.setString(8, retained);
      int result = ps.executeUpdate();
      ps.close();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /**
   * Gets the expected days.
   *
   * @param mrno the mrno
   * @param patientid the patientid
   * @return the expected days
   * @throws SQLException the SQL exception
   */
  public int getExpectedDays(String mrno, String patientid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT ESTIMATED_DAYS FROM ADMISSION WHERE MR_NO=? AND PATIENT_ID=?");
      ps.setString(1, mrno);
      ps.setString(2, patientid);
      int days = DataBaseUtil.getIntValueFromDb(ps);
      return days;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the bed id.
   *
   * @param mrno the mrno
   * @param patientId the patient id
   * @param bed the bed
   * @return the bed id
   * @throws SQLException the SQL exception
   */
  public int getBedId(String mrno, String patientId, int bed) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int id = 0;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("SELECT admit_id FROM ip_bed_details "
              + " where mrno=? AND patient_id=? AND BED_ID=?");
      ps.setString(1, mrno);
      ps.setString(2, patientId);
      ps.setInt(3, bed);
      id = DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return id;

  }

  /**
   * Gets the bed id.
   *
   * @param mrno the mrno
   * @param patientId the patient id
   * @return the bed id
   * @throws SQLException the SQL exception
   */
  public int getBedId(String mrno, String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    int id = 0;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("SELECT admit_id FROM ip_bed_details where mrno=? "
              + " AND patient_id=? AND STATUS IN('C','A')");
      ps.setString(1, mrno);
      ps.setString(2, patientId);
      id = DataBaseUtil.getIntValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return id;

  }

  /**
   * Gets the charge id.
   *
   * @param bill the bill
   * @param chargegroup the chargegroup
   * @param chargehead the chargehead
   * @param activityid the activityid
   * @return the charge id
   * @throws SQLException the SQL exception
   */
  public String getChargeId(String bill, String chargegroup, String chargehead, int activityid)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("SELECT CHARGE_ID FROM BILL_ACTIVITY_CHARGE "
              + " WHERE  ACTIVITY_ID=? AND activity_code=?");
      ps.setInt(1, activityid);
      ps.setString(2, chargegroup);
      String id = DataBaseUtil.getStringValueFromDb(ps);
      return id;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the chargeid for subgroups.
   *
   * @param bill the bill
   * @param chargeHead the charge head
   * @param chargeid the chargeid
   * @return the chargeid for subgroups
   * @throws SQLException the SQL exception
   */
  public String getChargeidForSubgroups(String bill, String chargeHead, String chargeid)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("SELECT CHARGE_ID FROM BILL_CHARGE "
              + " WHERE BILL_NO=? AND CHARGE_HEAD=? AND CHARGE_REF=?");
      ps.setString(1, bill);
      ps.setString(2, chargeHead);
      ps.setString(3, chargeid);
      String id = DataBaseUtil.getStringValueFromDb(ps);
      return id;
    } finally {
      if (ps != null) {
        ps.close();
      }
      if (con != null) {
        con.close();
      }
    }
  }

  /**
   * Gets the chargeid for subgroups.
   *
   * @param chargeHead the charge head
   * @param chargeid the chargeid
   * @return the chargeid for subgroups
   * @throws SQLException the SQL exception
   */
  public String getChargeidForSubgroups(String chargeHead, String chargeid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con
          .prepareStatement("SELECT CHARGE_ID FROM BILL_CHARGE "
              + " WHERE CHARGE_HEAD=? AND CHARGE_REF=?");
      ps.setString(1, chargeHead);
      ps.setString(2, chargeid);
      String id = DataBaseUtil.getStringValueFromDb(ps);
      return id;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant QUERY8TO20. */
  public static final String QUERY8TO20 = "SELECT TO_CHAR('now'::time , 'HH24') AS HOURS";

  /** The Constant MOTHER_CATEGORY. */
  public static final String MOTHER_CATEGORY = "SELECT patient_category_id "
      + " FROM patient_details WHERE mr_no = ?";

  /** The Constant MOTHER_REGISTRATION_CATEGORY. */
  public static final String MOTHER_REGISTRATION_CATEGORY = "SELECT patient_category_id "
      + " FROM patient_registration WHERE patient_id = ?";

  /**
   * Register.
   *
   * @param con the con
   * @param dto the dto
   * @param userid the userid
   * @param centerId the center id
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean register(Connection con, NewBornDTO dto, String userid, int centerId)
      throws SQLException {

    boolean status = true;
    String dob = dto.getDateOfBirth();
    String wardName = "";
    String wardNo = "";

    // check if baby ward exists
    if (dto.getWard() != null && !dto.getWard().equals("")) {
      dto.setPatientWard(dto.getWard());
    }

    List warddetailsList = new WardAndBedMasterDao().getWarDetails(dto.getPatientWard());
    BasicDynaBean warddetails = (warddetailsList != null 
        && warddetailsList.size() > 0) ? (BasicDynaBean) warddetailsList
        .get(0) : null;
    if (warddetails != null) {
      wardName = (String) warddetails.get("ward_name");
      wardNo = (String) warddetails.get("ward_no");
    }

    String autogeneratedmrno = null;
    int motherCategory;

    try (PreparedStatement ps = con.prepareStatement(MOTHER_CATEGORY)) {
      ps.setString(1, dto.getMrNo());
      motherCategory = DataBaseUtil.getIntValueFromDb(ps);
    }

    int defaultCatId = 1;

    if (motherCategory != defaultCatId) {
      String categoryCode = null;
      String isSeperateSeqRequire = null;
      BasicDynaBean categoryBean = new GenericDAO("patient_category_master").findByKey(
          "category_id", motherCategory);
      if (categoryBean != null) {
        if (categoryBean.get("code") != null) {
          categoryCode = categoryBean.get("code").toString();
        }
        isSeperateSeqRequire = categoryBean.get("seperate_num_seq").toString();
      }
      if ("Y".equals(isSeperateSeqRequire) && !"".equals(categoryCode)) {
        autogeneratedmrno = DataBaseUtil.getNextPatternId(categoryCode);

      } else {
        autogeneratedmrno = DataBaseUtil.getNextPatternId("MRNO");
      }
    } else {
      autogeneratedmrno = DataBaseUtil.getNextPatternId("MRNO");
    }

    String autogeneratedpatient = VisitDetailsDAO.getNextVisitId("i", centerId);
    String vistType = "i";

    // details of baby's mother
    String patDetailsQuery = " select pr.bed_type ,pr.org_id as patient_org_id ,* "
        + " from patient_details pd ,"
        + " patient_registration pr "
        + " where pd.mr_no=pr.mr_no and  pr.mr_no = pd.mr_no and pr.patient_id = ? ";
    ArrayList patDetails;
    try (PreparedStatement ps = con.prepareStatement(patDetailsQuery)) {
      ps.setString(1, dto.getPatientid());
      patDetails = DataBaseUtil.queryToArrayList(ps);
    }
    Hashtable patDetailsMap = (Hashtable) patDetails.get(0);

    String patientdetails = "INSERT INTO patient_details(mr_no, patient_name, "
        + " middle_name, patient_gender,"
        + " patient_address, patient_city, patient_state, patient_phone, "
        + " salutation, last_name, dateofbirth,"
        + " custom_list5_value, custom_list6_value, user_name, "
        + " oldmrno,custom_list4_value,emr_access,remarks,PATIENT_CARE_OFTEXT,"
        + " PATIENT_CAREOF_ADDRESS,timeofbirth,patient_area,first_visit_reg_date,country,"
        + " patient_category_id,category_expiry_date,email_id,nationality_id,"
        + " patient_phone_country_code, patient_care_oftext_country_code, delivery_type"
        + (dto.getCaesareanIndicationId() != null ? ",caesarean_indication_id) " : ") ")
        + " VALUES (?,?,?,?,?,?,?,?,?,?,to_date(?,'yyyy-mm-dd'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
        + " to_date(?,'yyyy-mm-dd'),?,?,?,?,?"
        + (dto.getCaesareanIndicationId() != null ? ",?) " : ") ");

    try (PreparedStatement ps = con.prepareStatement(patientdetails)) { 
      
      ps.setString(1, autogeneratedmrno);
      ps.setString(2, dto.getFirstName());
      ps.setString(3, "");
      ps.setString(4, dto.getGender());
      ps.setString(5, (String) patDetailsMap.get("PATIENT_ADDRESS"));
      ps.setString(6, (String) patDetailsMap.get("PATIENT_CITY"));
      ps.setString(7, (String) patDetailsMap.get("PATIENT_STATE"));
      ps.setString(8, (String) patDetailsMap.get("PATIENT_PHONE"));
      ps.setString(9, dto.getSalutation());
      ps.setString(10, dto.getLastName());
      ps.setString(11, dob);
      ps.setString(12, (String) patDetailsMap.get("CUSTOM_LIST5_VALUE"));
      ps.setString(13, "");
      ps.setString(14, userid);
      ps.setString(15, "");
      ps.setString(16, "");
      ps.setString(17, (String) patDetailsMap.get("EMR_ACCESS"));
      ps.setString(18, "");
      /* ps.setString(20, dto.getPatientorg()); */ps.setString(19,
          (String) patDetailsMap.get("PATIENT_CARE_OFTEXT"));
      ps.setString(20, (String) patDetailsMap.get("PATIENT_CAREOF_ADDRESS"));
      ps.setString(21, dto.getTimeOfBirth());
      if (!((String) patDetailsMap.get("PATIENT_AREA")).equals("")) {
        ps.setString(22, (String) patDetailsMap.get("PATIENT_AREA"));
      } else {
        ps.setString(22, null);
      }
      ps.setDate(23, dto.getRegDate());
      ps.setString(24, (String) patDetailsMap.get("COUNTRY"));
      ps.setInt(25, new Integer((String) patDetailsMap.get("PATIENT_CATEGORY_ID")));
      if (patDetailsMap.get("CATEGORY_EXPIRY_DATE") != null
          && !((String) patDetailsMap.get("CATEGORY_EXPIRY_DATE")).equals("")) {
        ps.setString(26, (String) patDetailsMap.get("CATEGORY_EXPIRY_DATE"));
      } else {
        ps.setString(26, null);
      }
      ps.setString(27, (String) patDetailsMap.get("EMAIL_ID"));
      ps.setString(28, dto.getNationalityId());
      ps.setString(29, (String)patDetailsMap.get("PATIENT_PHONE_COUNTRY_CODE"));
      ps.setString(30, (String)patDetailsMap.get("PATIENT_CARE_OFTEXT_COUNTRY_CODE"));
      ps.setString(31, dto.getDeliveryType());
      if (dto.getCaesareanIndicationId() != null) {
        ps.setInt(32, dto.getCaesareanIndicationId());
      }
      
      int count = ps.executeUpdate();
      if (count > 0) {
        status = true;
      }
    }

    String babyDept = null;
    String babyDoctor = null;
    if ((dto.getBabyAttendingDeptIP() == null) 
        || dto.getBabyAttendingDeptIP().equalsIgnoreCase("")) {
      babyDept = (String) patDetailsMap.get("DEPT_NAME");
      babyDoctor = (String) patDetailsMap.get("DOCTOR");
      dto.setBabyAttendingDeptIP(babyDept);

    } else {
      babyDept = dto.getBabyAttendingDeptIP();
      babyDoctor = dto.getBabyAttendingDocIP();
    }

    String bedType = null;
    // check if baby bedtype exists
    if (dto.getBedtype() != null && !dto.getBedtype().equals("")) {
      bedType = dto.getBedtype();
    } else if (dto.getPatientbed().isEmpty() || dto.getPatientbed() == null) {
      bedType = (String) patDetailsMap.get("BED_TYPE");
    } else {
      bedType = dto.getPatientbed();
    }

    int parentcategory;
    try (PreparedStatement ps = con.prepareStatement(MOTHER_REGISTRATION_CATEGORY)) {
      ps.setString(1, dto.getPatientid());
      parentcategory = DataBaseUtil.getIntValueFromDb(ps);
    }
    String patientregistration = ""
        + "INSERT INTO patient_registration(mr_no, patient_id, "
        + " reg_date, reg_time, status, cflag,"
        + " revisit,visit_type,complaint,org_id,dept_name,doctor,"
        + " bed_type,ward_id,ward_name,"
        + " patient_category_id,user_name,mlc_status,admitted_dept,"
        + " child_birth_remarks,main_visit_id"
        + " ,op_type,reg_charge_accepted,center_id)"
        + " VALUES (?, ?, to_date(?,'dd-mm-yyyy'), ?::time without time zone, "
        + "        ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'Y',?);";
    try (PreparedStatement ps = con.prepareStatement(patientregistration)) {
      
      ps.setString(1, autogeneratedmrno);
      ps.setString(2, autogeneratedpatient);
      ps.setString(3, dto.getDate());
      ps.setString(4, dto.getTime());
      ps.setString(5, "A");
      ps.setInt(6, 0);
      ps.setString(7, "N");
      ps.setString(8, vistType);
      ps.setString(9, null);
      ps.setString(10, dto.getPatientorg());
      ps.setString(11, babyDept);
      ps.setString(12, babyDoctor);
      ps.setString(13, bedType);
      ps.setString(14, wardNo);
      ps.setString(15, wardName);
      ps.setInt(16, parentcategory);
      ps.setString(17, userid);
      ps.setString(18, "N");
      ps.setString(19, babyDept);
      ps.setString(20, dto.getChildBirthRemarks());
      ps.setString(21, autogeneratedpatient);
      ps.setString(22, "M");
      ps.setInt(23, centerId);
      int count = ps.executeUpdate();
      if (count > 0) {
        status = true;
      }
    }
    dto.setAutogeneratedmrno(autogeneratedmrno);
    dto.setAutogeneratedpatient(autogeneratedpatient);

    String doctorConsulCharge = "0";

    Statement stm = null;
    String hoursStr = DataBaseUtil.getStringValueFromDb(QUERY8TO20);
    int presenthour = Integer.parseInt(hoursStr);
    BasicDynaBean regPrefs = new RegistrationPreferencesDAO().getRecord();
    int am = ((BigDecimal) regPrefs.get("night_am")).intValue();
    int pm = ((BigDecimal) regPrefs.get("night_pm")).intValue();

    String orgquery = "select org_id from organization_details where org_name=?";
    String generalorgid = DataBaseUtil.getStringValueFromDb(orgquery, 
        Constants.getConstantValue("ORG"));

    if (presenthour >= am && presenthour < pm) {
      try (PreparedStatement ps = con
          .prepareStatement("select doctor_ip_charge from doctor_consultation_charge  "
              + " where doctor_name=? and bed_type=? and organization=?")) {
        ps.setString(1, babyDoctor);
        ps.setString(2, bedType);
        ps.setString(3, (String) patDetailsMap.get("PATIENT_ORG_ID"));
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            doctorConsulCharge = rs.getString(1);
          } else {
            try (PreparedStatement ps1 = con
                .prepareStatement("select doctor_ip_charge from doctor_consultation_charge  "
                    + " where doctor_name=? and bed_type=? and organization=?")) {
              ps1.setString(1, babyDoctor);
              ps1.setString(2, Constants.getConstantValue("BEDTYPE"));
              ps1.setString(3, generalorgid);
              try (ResultSet rs1 = ps.executeQuery()) {
                if (rs1.next()) {
                  doctorConsulCharge = rs1.getString(1);
                }
              }
            }
          }
          dto.setAmOrPm("am");
        }
      }

    } else {
      try (PreparedStatement ps = con
          .prepareStatement("select night_ip_charge from doctor_consultation_charge  "
              + " where doctor_name=? and bed_type=? and organization=?")) {
        ps.setString(1, babyDoctor);
        ps.setString(2, bedType);
        ps.setString(3, (String) patDetailsMap.get("PATIENT_ORG_ID"));
        try (ResultSet rs = ps.executeQuery()) {
          if (rs.next()) {
            doctorConsulCharge = rs.getString(1);
          } else {
            try (PreparedStatement ps1 = con
                .prepareStatement("select night_ip_charge from doctor_consultation_charge  "
                    + " where doctor_name=? and bed_type=? and organization=?")) {
              ps1.setString(1, babyDoctor);
              ps1.setString(2, Constants.getConstantValue("BEDTYPE"));
              ps1.setString(3, generalorgid);
              try (ResultSet rs1 = ps1.executeQuery()) {
                if (rs1.next()) {
                  doctorConsulCharge = rs1.getString(1);
                }
              }
            }
          }
          dto.setAmOrPm("pm");
        }
      }
    }

    dto.setDocConsCharge(doctorConsulCharge);

    return status;
  }

  /**
   * Gets the bed types.
   *
   * @return the bed types
   */
  public ArrayList getBedTypes() {

    return DataBaseUtil
        .queryToArrayList("SELECT distinct BD.BED_TYPE as BED_TYPE "
            + " FROM BED_DETAILS BD WHERE BD.BED_STATUS = 'A' "
            + " UNION  "
            + " select distinct IBC.INTENSIVE_BED_TYPE as BED_TYPE "
            + " FROM  ICU_BED_CHARGES IBC WHERE ibc.bed_status = 'A'");
  }

  /** The Constant AWAITINGPATIENTS. */
  public static final String AWAITINGPATIENTS = "SELECT "
      + " PD.MR_NO,(S.SALUTATION || ' ' || PD.PATIENT_NAME || ' ' ||PD.LAST_NAME) "
      + " AS PATIENT_NAME,PR.BED_TYPE,PR.WARD_NAME,PR.PATIENT_ID,PR.ORG_ID "
      + " AS PATIENT_ORG_ID,D.DOCTOR_NAME,PR.DOCTOR,PR.DEPT_NAME "
      + " FROM PATIENT_DETAILS PD,PATIENT_REGISTRATION PR,DOCTORS D,SALUTATION_MASTER s "
      + " WHERE  PR.VISIT_TYPE = 'i' AND pd.mr_no=pr.mr_no AND D.DOCTOR_ID=PR.DOCTOR "
      + " AND PD.SALUTATION=S.SALUTATION_ID AND PR.STATUS='A' "
      + " AND PR.PATIENT_ID NOT IN(SELECT PATIENT_ID FROM ADMISSION) ";

  /**
   * Gets the list of awaiting IP patients.
   *
   * @return the list of awaiting IP patients
   */
  public List getListOfAwaitingIPPatients() {
    return DataBaseUtil.queryToArrayList(AWAITINGPATIENTS);

  }

  /**
   * This gets the patients daycare status.
   *
   * @param mrno the mrno
   * @param patientid the patientid
   * @return boolean
   * @throws SQLException the SQL exception
   */
  public boolean getDaycareStatus(String mrno, String patientid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT DAYCARE_STATUS FROM ADMISSION WHERE MR_NO=? AND PATIENT_ID=?");
      ps.setString(1, mrno);
      ps.setString(2, patientid);
      String status = DataBaseUtil.getStringValueFromDb(ps);
      if (status == null) {
        status = "N";
      }
      return status.equalsIgnoreCase("Y");
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the patient details for bill.
   *
   * @param patientId the patient id
   * @return the patient details for bill
   * @throws SQLException the SQL exception
   */
  public List getPatientDetailsForBill(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList list = null;

    String patientDetails = "SELECT pvd.mr_no,pvd.patient_full_name as patient_name,"
        + " pvd.org_id AS patient_org_id,pvd.bed_name,pvd.ward_name,"
        + " TO_CHAR(A.ADMIT_DATE,'DD-MM-YYYY') as ADMIT_DATE, "
        + " (case  when pvd.patient_gender='F' then 'Female' when pvd.patient_gender='M'"
        + " then 'Male' else 'none' end )as patient_gender, "
        + " pvd.patient_id,"
        + " pvd.doctor_id FROM patient_visit_details_ext_view pvd  "
        + " LEFT OUTER JOIN  admission a on pvd.patient_id=a.patient_id  "
        + " WHERE pr.status='A' AND pr.patient_id = ?";
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(patientDetails);
      ps.setString(1, patientId);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the open credit bills.
   *
   * @return the open credit bills
   * @throws SQLException the SQL exception
   */
  public List getOpenCreditBills() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT * FROM BILL WHERE STATUS='A' "
              + " and visit_type='i' and bill_type='C' and restriction_type='N' ");
      List billlist = DataBaseUtil.queryToArrayList(ps);
      return billlist;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the patient details for admit.
   *
   * @param patientId the patient id
   * @return the patient details for admit
   * @throws SQLException the SQL exception
   */
  public List getPatientDetailsForAdmit(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList list = null;
    String patientDetails = "SELECT PD.MR_NO,PD.PATIENT_NAME,PR.PATIENT_ID,"
        + " PR.ORG_ID AS PATIENT_ORG_ID,D.DOCTOR_NAME,PR.DOCTOR ,PR.DEPT_NAME FROM "
        + " PATIENT_DETAILS PD,PATIENT_REGISTRATION PR,DOCTORS D  "
        + " WHERE PR.PATIENT_ID NOT IN(SELECT PATIENT_ID FROM ADMISSION) AND "
        + " PR.VISIT_TYPE='i' AND PR.MR_NO=PD.MR_NO  AND D.DOCTOR_ID=PR.DOCTOR AND "
        + " PR.PATIENT_ID = ?";
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(patientDetails);
      ps.setString(1, patientId);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;

  }

  /**
   * Gets the baby or mother details.
   *
   * @param patientId the patient id
   * @return the baby or mother details
   * @throws Exception the exception
   */
  public List getBabyOrMotherDetails(String patientId) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    ArrayList list = null;
    String patientDetails = "select a2.isBaby,a1.mr_no as mrno1,a1.patient_id as pat1 ,"
        + " a2.mr_no as mrno2, a2.patient_id as pat2,pd.patient_name,pd.middle_name "
        + " from patient_details pd,admission a1 join admission a2 "
        + " on a1.patient_id = a2.parent_id "
        + " where pd.mr_no=a1.mr_no and a2.patient_id=? UNION "
        + " select a1.isBaby ,a1.mr_no as mrno1,a1.patient_id as pat1 ,"
        + " a2.mr_no as mrno2,a2.patient_id as pat2,pd.patient_name,pd.middle_name "
        + " from patient_details pd,admission a1 join admission a2 on "
        + " a1.patient_id = a2.parent_id where pd.mr_no=a2.mr_no and a1.patient_id= ?";
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(patientDetails);
      ps.setString(1, patientId);
      ps.setString(2, patientId);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the ward name for beds.
   *
   * @return the ward name for beds
   * @throws SQLException the SQL exception
   */
  public List getWardNameForBeds() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List wards = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT DISTINCT(B.WARD_NO),W.WARD_NAME,"
              + " B.BED_TYPE FROM BED_NAMES B,WARD_NAMES W "
              + " WHERE B.WARD_NO=W.WARD_NO and w.status='A'");
      wards = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return wards;
  }

  /**
   * Gets the free beds.
   *
   * @return the free beds
   * @throws Exception the exception
   */
  public List getFreeBeds() throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List beds = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT bd.ward_no,bd.bed_type,bd.bed_name,bd.occupancy,"
              + " bd.status,bd.bed_id,WN.WARD_NAME "
              + " FROM BED_NAMES BD,WARD_NAMES WN "
              + " where OCCUPANCY IN('N') AND BD.STATUS='A' "
              + " AND WN.STATUS='A' AND BD.WARD_NO=WN.WARD_NO AND BD.bed_status = 'A' ");
      beds = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return beds;
  }

  /**
   * Gets the discharge status of A visit.
   *
   * @param patid the patid
   * @return the discharge status of A visit
   * @throws SQLException the SQL exception
   */
  public String getDischargeStatusOfAVisit(String patid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String dischargestatus = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("SELECT DISCHARGE_STATUS  FROM BILL WHERE VISIT_ID=?");
      ps.setString(1, patid);
      dischargestatus = DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return dischargestatus;

  }

  /**
   * Update hosp details.
   *
   * @param con the con
   * @param bdto the bdto
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateHospDetails(Connection con, BedAdmissionDTO bdto) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE PATIENT_REGISTRATION SET BED_TYPE=?,"
              + " WARD_NAME=?,WARD_ID=? WHERE MR_NO=? AND PATIENT_ID=?");
      ps.setString(1, bdto.getAdmitbedtype());
      ps.setString(2, bdto.getAdmitward());
      ps.setString(3, bdto.getAdmitwardid());
      ps.setString(4, bdto.getMrNo());
      ps.setString(5, bdto.getPatientid());
      int result = ps.executeUpdate();
      return result != 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /**
   * Mark ready to discharge.
   *
   * @param patid the patid
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean markReadyToDischarge(String patid) throws Exception {
    Connection con = null;
    try {
      con = DataBaseUtil.getConnection();
      Map<String, String> columns = new HashMap<String, String>();
      columns.put("ready_to_discharge", "Y");
      return (new GenericDAO("patient_registration").update(con, columns, "patient_id", patid) > 0);
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }

  /**
   * Gets the admission details.
   *
   * @return the admission details
   * @throws SQLException the SQL exception
   */
  public List getAdmissionDetails() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List al = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement("SELECT MR_NO,PATIENT_ID,DAYCARE_STATUS , "
          + " ISBABY FROM ADMISSION");
      al = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return al;
  }

  /**
   * Check baby.
   *
   * @param patientId the patient id
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List checkBaby(String patientId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List bl = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT MR_NO,PATIENT_ID "
              + " FROM ADMISSION WHERE PARENT_ID=? AND ISBABY='Y'");
      ps.setString(1, patientId);
      bl = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bl;

  }

  /** The Constant TEST_CANCEL_QUERY. */
  public static final String TEST_CANCEL_QUERY = "UPDATE "
      + " Tests_Prescribed SET Conducted='Cancel' "
      + " WHERE prescribed_id=?";
  
  /** The Constant SERVICE_CANCEL_QUERY. */
  public static final String SERVICE_CANCEL_QUERY = "UPDATE "
      + " services_prescribed set conducted='C' "
      + " WHERE prescription_id=?";

  /**
   * Cancle prescriptions.
   *
   * @param con the con
   * @param chargehead the chargehead
   * @param activityid the activityid
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean canclePrescriptions(Connection con, String chargehead, int activityid)
      throws SQLException {
    boolean teststatus = true;
    boolean servicestatus = true;
    boolean status = false;
    if (chargehead.equalsIgnoreCase("RTDIA") || chargehead.equalsIgnoreCase("LTDIA")) {
      try (PreparedStatement ps = con.prepareStatement(TEST_CANCEL_QUERY)) {
        ps.setInt(1, activityid);
        if (ps.executeUpdate() <= 0) {
          teststatus = false;
        }
      }
    }
    if (chargehead.equalsIgnoreCase("SERSNP")) {
      try (PreparedStatement ps = con.prepareStatement(SERVICE_CANCEL_QUERY)) {
        ps.setInt(1, activityid);
        if (ps.executeUpdate() <= 0) {
          servicestatus = false;
        }
      }
    }
    if (servicestatus && teststatus) {
      status = true;
    }
    return status;
  }

  /**
   * Gets the exist baby details.
   *
   * @param mrno the mrno
   * @param patientid the patientid
   * @return the exist baby details
   * @throws Exception the exception
   */
  public List getExistBabyDetails(String mrno, String patientid) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("select a.mr_no,a.patient_id,patient_name,middle_name, "
              + " get_patient_age(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE, "
              + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) as PATIENT_AGE_IN, "
              + " case when patient_gender='M' then 'Male' else 'Female' end as patient_gender,"
              + " dateofbirth,pd.timeofbirth "
              + " from admission a,patient_details pd where pd.mr_no=a.mr_no and parent_id=?");
      ps.setString(1, patientid);
      return DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }

  /**
   * Update baby bed.
   *
   * @param con the con
   * @param mrno the mrno
   * @param patid the patid
   * @param bedno the bedno
   * @param ward the ward
   * @param days the days
   * @param daycare the daycare
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean updateBabyBed(Connection con, String mrno, String patid, int bedno, String ward,
      int days, String daycare) throws SQLException {
    if (daycare == null) {
      daycare = "N";
    } else {
      daycare = "Y";
    }
    PreparedStatement ps = null;
    int result = 0;
    try {
      ps = con
          .prepareStatement("UPDATE ADMISSION SET BED_ID=?,ISBABY='N',"
              + " ESTIMATED_DAYS=?,daycare_status=? WHERE MR_NO=? AND PATIENT_ID=?");
      ps.setInt(1, bedno);
      ps.setInt(2, days);
      ps.setString(3, daycare);
      ps.setString(4, mrno);
      ps.setString(5, patid);
      result = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return result != 0;
  }

  /**
   * Gets the occupied beds bya patient.
   *
   * @param mrno the mrno
   * @param ipno the ipno
   * @return the occupied beds bya patient
   * @throws SQLException the SQL exception
   */
  public List getOccupiedBedsByaPatient(String mrno, String ipno) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List bl = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT BED_ID FROM IP_BED_DETAILS WHERE MRNO=? "
              + " AND PATIENT_ID=? AND STATUS IN('A','C','R')");
      ps.setString(1, mrno);
      ps.setString(2, ipno);
      bl = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bl;
  }

  /**
   * Gets the current bed start date.
   *
   * @param mrno the mrno
   * @param patid the patid
   * @return the current bed start date
   * @throws SQLException the SQL exception
   */
  public Timestamp getCurrentBedStartDate(String mrno, String patid) throws SQLException {
    Timestamp startdate = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con
            .prepareStatement("SELECT START_DATE FROM IP_BED_DETAILS "
                + " WHERE MRNO=? AND PATIENT_ID=? AND STATUS='C' "
                + " UNION "
                + " SELECT START_DATE FROM IP_BED_DETAILS "
                + " WHERE MRNO=? AND PATIENT_ID=? AND STATUS='A'");) {
      ps.setString(1, mrno);
      ps.setString(2, patid);
      ps.setString(3, mrno);
      ps.setString(4, patid);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          startdate = rs.getTimestamp(1);
        }
      }
    }
    return startdate;
  }

  /**
   * Gets the initial date for finalise.
   *
   * @param mrno the mrno
   * @param patid the patid
   * @return the initial date for finalise
   * @throws SQLException the SQL exception
   */
  public String getInitialDateForFinalise(String mrno, String patid) throws SQLException {
    String startdate = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con
            .prepareStatement("SELECT to_char(START_DATE, 'dd-mm-yyyy hh:mi') "
                + " FROM IP_BED_DETAILS WHERE MRNO=? AND PATIENT_ID=? AND STATUS='C' "
                + " UNION "
                + " SELECT to_char(START_DATE, 'dd-mm-yyyy hh:mi') "
                + " FROM IP_BED_DETAILS WHERE MRNO=? AND PATIENT_ID=? AND STATUS='A'");) {
      ps.setString(1, mrno);
      ps.setString(2, patid);
      ps.setString(3, mrno);
      ps.setString(4, patid);
      try (ResultSet rs = ps.executeQuery();) {
        while (rs.next()) {
          startdate = rs.getString(1);
        }
      }
      SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy hh:mm");
      if (startdate == null) {
        startdate = formater.format(new java.util.Date());
      }
    }
    return startdate;
  }

  /**
   * Gets the current bed end date.
   *
   * @param mrno the mrno
   * @param patid the patid
   * @return the current bed end date
   * @throws SQLException the SQL exception
   */
  public Timestamp getCurrentBedEndDate(String mrno, String patid) throws SQLException {
    Timestamp startdate = null;
    try (Connection con = DataBaseUtil.getReadOnlyConnection();
        PreparedStatement ps = con
            .prepareStatement("SELECT END_DATE FROM IP_BED_DETAILS "
                + " WHERE MRNO=? AND PATIENT_ID=? AND STATUS='C' "
                + " UNION "
                + " SELECT END_DATE FROM IP_BED_DETAILS "
                + " WHERE MRNO=? AND PATIENT_ID=? AND STATUS='A'");) {
      ps.setString(1, mrno);
      ps.setString(2, patid);
      ps.setString(3, mrno);
      ps.setString(4, patid);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          startdate = rs.getTimestamp(1);
        }
      }
    }
    return startdate;
  }

  /**
   * Gets the startdate and days list.
   *
   * @return the startdate and days list
   * @throws SQLException the SQL exception
   */
  public List getStartdateAndDaysList() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List sl = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con
          .prepareStatement("SELECT MRNO,PATIENT_ID,START_DATE,END_DATE "
              + " FROM IP_BED_DETAILS WHERE  STATUS='C' OR STATUS='A' ");
      sl = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return sl;
  }

  /**
   * This method updates anaesthetist column in bed_operation_schedule table.
   *
   * @param con the con
   * @param opePrescribedId the ope prescribed id
   * @param doctor the doctor
   * @return boolean
   * @throws SQLException the SQL exception
   */
  public boolean updateOperationScheduled(Connection con, String opePrescribedId, String doctor)
      throws SQLException {
    PreparedStatement ps = null;
    int result = 0;
    try {
      ps = con
          .prepareStatement("UPDATE BED_OPERATION_SCHEDULE "
              + " SET ANAESTHETIST=? WHERE PRESCRIBED_ID=?");
      ps.setString(1, doctor);
      ps.setInt(2, Integer.parseInt(opePrescribedId));
      result = ps.executeUpdate();
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return result != 0;

  }

  /**
   * Gets the patient details for EMR prescription.
   *
   * @param mrno the mrno
   * @param patId the pat id
   * @return the patient details for EMR prescription
   * @throws SQLException the SQL exception
   */
  public List getPatientDetailsForEMRPrescription(String mrno, String patId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      String patdetails = "SELECT (S.SALUTATION || ' ' || "
          + " PD.PATIENT_NAME || ' ' ||PD.LAST_NAME) AS PATIENT_NAME ,"
          + " D.DOCTOR_NAME,pr.doctor,pr.DEPT_NAME,PR.PATIENT_ID,pd.mr_no,"
          + " pr.org_id as patient_org_id,get_patient_age(pd.dateofbirth, pd.expected_dob) "
          + " as PATIENT_AGE, get_patient_age_in(pd.dateofbirth, pd.expected_dob) "
          + " as PATIENT_AGE_IN,(case when PD.PATIENT_GENDER='F' then 'Female' "
          + " when PD.PATIENT_GENDER='M'then 'Male' else 'none' end ) "
          + " as PATIENT_GENDER,PD.PATIENT_PHONE FROM PATIENT_DETAILS PD,DOCTORS D,"
          + " SALUTATION_MASTER S,"
          + " PATIENT_REGISTRATION PR WHERE Pd.MR_NO=? and PD.SALUTATION=S.SALUTATION_ID and "
          + " pd.mr_no = pr.mr_no and pr.patient_id=? AND D.DOCTOR_ID=pr.doctor  ";
      ps = con.prepareStatement(patdetails);
      ps.setString(1, mrno);
      ps.setString(2, patId);
      List patientDetailsList = DataBaseUtil.queryToArrayList(ps);
      DataBaseUtil.closeConnections(con, ps);
      return patientDetailsList;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Releases or occupies the retained bed in the active visit.
   *
   * @param con the con
   * @param mrno the mrno
   * @param patientid the patientid
   * @param ward the ward
   * @param bed the bed
   * @param status the status
   * @param userName the user name
   * @return the int
   * @throws SQLException the SQL exception
   */
  public static int releaseOrOccupyRetainedBed(Connection con, String mrno, String patientid,
      String ward, String bed, String status, String userName) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE IP_BED_DETAILS SET STATUS=?,UPDATED_DATE=LOCALTIMESTAMP(0),"
              + " USERNAME=? WHERE MRNO=? AND PATIENT_ID=? AND WARD=? AND BED_NO=?");
      ps.setString(1, status);
      ps.setString(2, userName);
      ps.setString(3, mrno);
      ps.setString(4, patientid);
      ps.setString(5, ward);
      ps.setString(6, bed);
      int result = ps.executeUpdate();
      if (result > 0) {
        DataBaseUtil.getStringValueFromDb("SELECT ");
      }
      return result;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /**
   * Update bed for package.
   *
   * @param con the con
   * @param mrno the mrno
   * @param patientid the patientid
   * @param ward the ward
   * @param bedtype the bedtype
   * @param bed the bed
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateBedForPackage(Connection con, String mrno, String patientid,
      String ward, String bedtype, int bed, String userName) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con
          .prepareStatement("UPDATE IP_BED_DETAILS SET BED_ID=? , "
              + " START_DATE=LOCALTIMESTAMP(0) ,updated_date=LOCALTIMESTAMP(0),username=? "
              + " WHERE MRNO=? AND PATIENT_ID=? AND STATUS='A'");
      ps.setInt(1, bed);
      ps.setString(2, userName);
      ps.setString(3, mrno);
      ps.setString(4, patientid);
      int result = ps.executeUpdate();
      return (result >= 0);
    } finally {
      if (ps != null) {
        ps.close();
      }
    }

  }

  /** The Constant GET_TEST_DEPT_CHARGE_QUERY. */
  private static final String GET_TEST_DEPT_CHARGE_QUERY = " SELECT dc.test_id, d.test_name,"
      + " d.test_name||' - '||dd.ddept_name as test, dc.charge, d.ddept_id,dd.ddept_name,"
      + " (CASE WHEN dd.category = 'DEP_RAD' THEN 'Radiology' ELSE 'Laboratory' END) as category  "
      + " FROM diagnostic_charges dc "
      + " JOIN diagnostics d ON (dc.test_id = d.test_id and d.status = 'A')  "
      + " JOIN diagnostics_departments dd on (d.ddept_id = dd.ddept_id) WHERE priority='R' ";

  /** The Constant GET_TEST_DEPT_CHARGES. */
  private static final String GET_TEST_DEPT_CHARGES = GET_TEST_DEPT_CHARGE_QUERY
      + " AND bed_type=? AND org_name=? " + "UNION " + GET_TEST_DEPT_CHARGE_QUERY
      + " AND bed_type='GENERAL' AND org_name='ORG0001' "
      + " AND NOT EXISTS (SELECT test_id FROM diagnostic_charges WHERE test_id = d.test_id "
      + "                 AND bed_type=? AND org_name=?)";

  /**
   * Gets the test dept charges.
   *
   * @param bedType the bed type
   * @param orgid the orgid
   * @return the test dept charges
   * @throws SQLException the SQL exception
   */
  public static List getTestDeptCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_TEST_DEPT_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      ps.setString(3, bedType);
      ps.setString(4, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return list;
  }

  /** The Constant TESTS_FOR_ORDER. */
  private static final String TESTS_FOR_ORDER = " SELECT dc.test_id as id, d.test_name "
      + " as name,d.test_name||' - '||dd.ddept_name as dis_name, dc.charge, "
      + " d.ddept_id,dd.ddept_name,"
      + " (CASE WHEN dd.category = 'DEP_RAD' THEN 'Radiology' ELSE 'Laboratory' END) "
      + " as category,bed_type as bed_type,org_name as org_id"
      + "  ,'DIA' as type FROM diagnostic_charges dc "
      + " JOIN diagnostics d ON (dc.test_id = d.test_id and d.status = 'A')  "
      + " JOIN diagnostics_departments dd on (d.ddept_id = dd.ddept_id) WHERE priority='R' ";

  /** The Constant PACKAGES_FOR_ORDER. */
  private static final String PACKAGES_FOR_ORDER = "SELECT pm.package_id::text AS id,"
      + " pm.package_name as name,pm.package_name AS dis_name,pc.charge,null as ddept_id,"
      + " null as ddept_name, null as category,pc.bed_type AS bed_type,pc.org_id AS org_id,"
      + " 'PKG' as type FROM PACK_MASTER pm,"
      + " PACKAGE_CHARGES pc "
      + " join pack_org_details pod using(package_id) WHERE  pc.package_id = pm.package_id"
      + " AND package_active='A' AND package_active='A' "
      + " AND pod.applicable='t' and pc.org_id = pod.org_id";

  /** The Constant TESTS_AND_PACKAGES. */
  private static final String TESTS_AND_PACKAGES = "SELECT * FROM (" + TESTS_FOR_ORDER + "UNION "
      + PACKAGES_FOR_ORDER + ") AS tests  WHERE tests.bed_type=? AND tests.org_id = ? ";

  /**
   * Gets the tests for order.
   *
   * @param bedType the bed type
   * @param orgid the orgid
   * @return the tests for order
   * @throws Exception the exception
   */
  public List<BasicDynaBean> getTestsForOrder(String bedType, String orgid) throws Exception {
    Connection con = null;
    PreparedStatement ps = null;
    List<BasicDynaBean> list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(TESTS_AND_PACKAGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToDynaList(ps);
      if (list.size() == 0) {
        ps = con.prepareStatement(TESTS_AND_PACKAGES);
        ps.setString(1, "GENERAL");
        ps.setString(2, "ORG0001");
        list = DataBaseUtil.queryToDynaList(ps);
      }
      return list;
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_EQUPMENT_DEPT. */
  private static final String GET_EQUPMENT_DEPT = " SELECT e.eq_id, "
      + " e.equipment_name, e.dept_id,d.dept_name,"
      + " ec.daily_charge as charge,ec.min_charge,"
      + " ec.incr_charge, em.min_duration,em.incr_duration,"
      + " ec.daily_charge_discount,ec.min_charge_discount,ec.incr_charge_discount "
      + " FROM equipment_master e "
      + " JOIN equipement_charges ec on (e.eq_id = ec.equip_id)"
      + " JOIN equipment_master em on (e.eq_id = em.eq_id) "
      + " JOIN department d on (d.dept_id = e.dept_id) WHERE e.status='A' ";

  /** The Constant GET_EQUIPMENT_DEPT_CHARGES. */
  private static final String GET_EQUIPMENT_DEPT_CHARGES = GET_EQUPMENT_DEPT
      + " AND ec.bed_type=? AND ec.org_id=? " + " UNION " + GET_EQUPMENT_DEPT
      + " AND ec.bed_type='GENERAL' AND ec.org_id='ORG0001' "
      + " AND NOT EXISTS (SELECT equip_id from equipement_charges WHERE equip_id=e.eq_id "
      + "                 AND bed_type=? AND org_id=?) " + " ORDER BY equipment_name ";

  /**
   * Gets the equipment dept charges.
   *
   * @param bedType the bed type
   * @param orgid the orgid
   * @return the equipment dept charges
   * @throws SQLException the SQL exception
   */
  public static List getEquipmentDeptCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_EQUIPMENT_DEPT_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      ps.setString(3, bedType);
      ps.setString(4, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant GET_OPERATION_DEPT. */
  private static final String GET_OPERATION_DEPT = " SELECT o.op_id, o.operation_name, "
      + " o.dept_id,d.dept_name,o.operation_name||' - '||d.dept_name as OPERATION ,"
      + " oc.surg_asstance_charge as charge, oc.surgeon_charge, oc.anesthetist_charge,"
      + " ood.item_code, "
      + " oc.surg_asst_discount,oc.surg_discount,oc.anest_discount "
      + " FROM operation_master o JOIN operation_charges oc USING (op_id) "
      + " JOIN department d USING (dept_id) "
      + " JOIN operation_org_details ood "
      + " ON (ood.operation_id = oc.op_id and ood.org_id = oc.org_id) AND  ood.applicable "
      + " WHERE o.status='A' ";

  /** The Constant GET_OPERATION_DEPT_CHARGES. */
  private static final String GET_OPERATION_DEPT_CHARGES = GET_OPERATION_DEPT
      + " AND oc.bed_type=? AND oc.org_id=? ORDER BY o.operation_name";

  /**
   * Gets the operation dept charges.
   *
   * @param bedType
   *          the bed type
   * @param orgid
   *          the orgid
   * @return the operation dept charges
   * @throws SQLException
   *           the SQL exception
   */
  public static List getOperationDeptCharges(String bedType, String orgid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_OPERATION_DEPT_CHARGES);
      ps.setString(1, bedType);
      ps.setString(2, orgid);
      list = DataBaseUtil.queryToArrayList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /**
   * Gets the bed ward name.
   *
   * @param bedid the bedid
   * @return the bed ward name
   * @throws SQLException the SQL exception
   */
  public List getBedWardName(String bedid) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    List list = null;
    try {
      con = DataBaseUtil.getConnection();
      if (bedid != null && !bedid.equals("")) {
        ps = con.prepareStatement("SELECT bed_name ,ward_name  FROM bed_names b,ward_names w "
            + "WHERE b.ward_no = w.ward_no and b.bed_id = ?");
        ps.setInt(1, Integer.parseInt(bedid));

        list = DataBaseUtil.queryToArrayList(ps);
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }

  /** The Constant BILL_CHARGES_WITHOUT_BED_CHARGES. */
  public static final String BILL_CHARGES_WITHOUT_BED_CHARGES = "SELECT b.visit_id,b.bill_no,"
      + " sum(amount) as total, b.approval_amount  "
      + " FROM bill_charge  join bill b using(bill_no) "
      + " LEFT OUTER JOIN department ON (bill_charge.act_department_id = department.dept_id)  "
      + " JOIN chargehead_constants "
      + " ON (bill_charge.charge_head = chargehead_constants.chargehead_id)  "
      + " JOIN chargegroup_constants "
      + " ON (bill_charge.charge_group = chargegroup_constants.chargegroup_id) "
      + " join patient_registration pr on(pr.patient_id = b.visit_id) "
      + " where b.visit_type='i' and b.status='A' and b.bill_type='C' "
      + " and b.restriction_type='N' and pr.status='A' and charge_group not in('BED','ICU')  "
      + " group BY b.visit_id,b.bill_no,b.approval_amount  ";

  /** The Constant BILL_CHARGES_WITH_BED_CHARGES. */
  public static final String BILL_CHARGES_WITH_BED_CHARGES = "SELECT b.visit_id,"
      + " b.bill_no,sum(amount) as total  "
      + " FROM bill_charge  "
      + " join bill b using(bill_no) "
      + " LEFT OUTER JOIN department ON (bill_charge.act_department_id = department.dept_id)  "
      + " JOIN chargehead_constants "
      + " ON (bill_charge.charge_head = chargehead_constants.chargehead_id)  "
      + " JOIN chargegroup_constants "
      + " ON (bill_charge.charge_group = chargegroup_constants.chargegroup_id) "
      + " join patient_registration pr on(pr.patient_id = b.visit_id) "
      + " where b.visit_type='i' and b.status='A' and pr.status='A'  "
      + " group BY b.visit_id,b.bill_no  ";

  /** The Constant CHARGES. */
  public static final String CHARGES = " select "
      + " charge_id,b.visit_id "
      + " from bill_activity_charge ba "
      + " join bill_charge BC using(charge_id) "
      + " join bill b using(bill_no)  "
      + " join ip_bed_details ip on(ip.patient_id = b.visit_id) "
      + " where ba.activity_code = 'BED' "
      + " and ip.status in('A','C','R') and ip.admit_id::varchar = ba.activity_id";

  /** The Constant RECEIPTS. */
  public static final String RECEIPTS = "select sum(total_receipts) as received "
      + "from bill where bill_no=?";

  /** The Constant AMOUNTS. */
  public static final String AMOUNTS = " select sum(total_amount) as amount "
      + "from bill where bill_no=?";

  /**
   * Retuens the received data for the bill.
   *
   * @param billNo the bill no
   * @return received_amt
   * @throws SQLException the SQL exception
   */
  public float getReceivedAmt(String billNo) throws SQLException {
    float receivedAmt = 0;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(RECEIPTS);) {
      ps.setString(1, billNo);
      try (ResultSet set = ps.executeQuery()) {
        while (set.next()) {
          receivedAmt = set.getFloat("RECEIVED");
        }
      }
      return receivedAmt;
    }
  }

  /**
   * Retuens the received data for the bill.
   *
   * @param billNo the bill no
   * @return received_amt
   * @throws SQLException the SQL exception
   */
  public float getAmount(String billNo) throws SQLException {
    float billedAmt = 0;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(AMOUNTS);) {
      ps.setString(1, billNo);
      try (ResultSet set = ps.executeQuery()) {
        while (set.next()) {
          billedAmt = set.getFloat("AMOUNT");
        }
      }
      return billedAmt;
    }
  }

}
