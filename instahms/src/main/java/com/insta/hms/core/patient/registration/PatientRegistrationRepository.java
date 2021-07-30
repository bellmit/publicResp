package com.insta.hms.core.patient.registration;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientRegistrationRepository.
 */
@Repository
public class PatientRegistrationRepository extends GenericRepository {

  /**
   * Instantiates a new patient registration repository.
   */
  public PatientRegistrationRepository() {
    super("patient_registration");
  }

  private static final List<String> DEFAULT_VISIT_LIST = Arrays.asList("M", "R");

  /** The Constant GET_PATIENT_VISITS. */
  private static final String GET_PATIENT_VISITS = 
      "select pra.patient_id as visit_id,pra.reg_date,to_char(pra.reg_time,'hh24:mi') as reg_time"
      + " ,pra.status, pra.center_id, dept.dept_name, doc.doctor_name,"
      + " dept.dept_id, doc.doctor_id, pra.op_type, pd.patient_gender,"
      + " pd.salutation, pra.visit_type, pra.bed_type, pra.org_id, pra.ip_credit_limit_amount"
      + " FROM patient_registration pra" + " JOIN patient_details pd on pd.mr_no = pra.mr_no"
      + " LEFT JOIN department dept on pra.dept_name = dept.dept_id"
      + " LEFT JOIN doctors doc on pra.doctor = doc.doctor_id WHERE pra.mr_no IN"
      + " (select mr_no from patient_details where original_mr_no = ? or mr_no = ?)"
      + " AND pra.visit_type = ? ";

  /**
   * Gets the patient visits.
   *
   * @param mrNo
   *          the mr no
   * @param visitType
   *          the visit type
   * @param centerId
   *          the center id
   * @param activeOnly
   *          the active only
   * @param allowOspPatient
   *          the allow osp patient
   * @return the patient visits
   */
  public List<BasicDynaBean> getPatientVisits(String mrNo, String visitType, Integer centerId,
      boolean activeOnly, boolean allowOspPatient) {
    String query = GET_PATIENT_VISITS;
    if (centerId != null && centerId != 0) {
      query = query + " AND pra.center_id = '" + centerId + "' ";
    }
    if (activeOnly) {
      query = query + " AND pra.status = 'A' ";
    }
    if (!allowOspPatient) {
      query = query + " AND pra.op_type != 'O' ";
    }
    query = query + " ORDER BY pra.reg_date DESC, pra.reg_time DESC";
    return DatabaseHelper.queryToDynaList(query, mrNo, mrNo, visitType);
  }

  /** The Constant GET_PATIENT_ALL_ACTIVE_VISITS. */
  private static final String GET_PATIENT_ALL_ACTIVE_VISITS = "select pra.patient_id as visit_id,"
      + " pra.reg_date,to_char(pra.reg_time,'hh24:mi') as reg_time"
      + " ,pra.status, pra.ipemr_status, pra.center_id, pra.discharge_doc_id,"
      + " pra.discharge_format, pra.visit_type, dept.dept_name,"
      + " doc.doctor_name, dept.dept_id, doc.doctor_id" + " FROM patient_registration pra"
      + " LEFT JOIN department dept on pra.dept_name = dept.dept_id"
      + " LEFT JOIN doctors doc on pra.doctor = doc.doctor_id" + " WHERE pra.mr_no IN (:mrList) ";

  private static final String GET_MR_NO = 
      "select mr_no from patient_details where original_mr_no = ?";

  /**
   * Gets the patient all active visits.
   *
   * @param mrNo
   *          the mr no
   * @param centerId
   *          the center id
   * @param activeOnly
   *          the active only
   * @return the patient all active visits
   */
  public List<BasicDynaBean> getPatientAllActiveVisits(String mrNo, Integer centerId,
      boolean activeOnly) {
    String query = GET_MR_NO;
    List<String> mrList = new ArrayList<String>();
    List<BasicDynaBean> beanList = DatabaseHelper.queryToDynaList(query, new Object[] { mrNo });
    for (BasicDynaBean bean : beanList) {
      mrList.add((String) bean.get("mr_no"));
    }
    mrList.add(mrNo);

    query = GET_PATIENT_ALL_ACTIVE_VISITS;
    if (centerId != null && centerId != 0) {
      query = query + " AND pra.center_id = '" + centerId + "' ";
    }
    if (activeOnly) {
      query = query + " AND pra.status = 'A' ";
    }
    query = query + " ORDER BY pra.reg_date DESC, pra.reg_time DESC";
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("mrList", mrList);
    return DatabaseHelper.queryToDynaList(query, parameters);
  }

  /** The Constant GET_PATIENT_VISIT_DETAILS. */
  private static final String GET_PATIENT_VISIT_DETAILS = 
      " SELECT pdev.*, coalesce(to_char(dc.visited_date::Date, 'DD-MM-YYYY'),'')"
      + " as cons_date, coalesce(to_char(dc.visited_date::Time,'HH24:MI'), '') as cons_time"
      + " ,dc.consultation_token as token_no FROM patient_details_ext_view as pdev"
      + " LEFT JOIN doctor_consultation dc ON (pdev.patient_id = dc.patient_id)"
      + " WHERE pdev.patient_id= ? ";

  /**
   * Gets the patient visit details bean.
   *
   * @param patientId
   *          the patient id
   * @return the patient visit details bean
   */
  public BasicDynaBean getPatientVisitDetailsBean(String patientId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_VISIT_DETAILS, patientId);
  }

  /** The get patient mlc status. */
  private static String GET_PATIENT_MLC_STATUS = "SELECT patient_id FROM "
      + " patient_registration WHERE COALESCE(mlc_status,'N') = 'Y'"
      + " AND mr_no = ? ORDER BY reg_date DESC LIMIT 1 ";

  /**
   * Gets the mlc status visit id.
   *
   * @param mrNo
   *          the mr no
   * @return the mlc status visit id
   */
  public BasicDynaBean getMlcStatusVisitId(String mrNo) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_MLC_STATUS, mrNo);
  }

  /** The Constant GET_LATEST_ACTIVE_VISIT. */
  private static final String GET_LATEST_ACTIVE_VISIT = "SELECT * FROM"
      + " patient_registration WHERE mr_no=? AND (center_id=? or 0=?)"
      + " AND status='A' ORDER BY reg_date DESC limit 1 ";

  /**
   * Gets the latest active visit.
   *
   * @param mrNo
   *          the mr no
   * @return the latest active visit
   */
  public BasicDynaBean getLatestActiveVisit(String mrNo) {
    return DatabaseHelper.queryToDynaBean(GET_LATEST_ACTIVE_VISIT, mrNo,
        RequestContext.getCenterId(), RequestContext.getCenterId());
  }

  /** The Constant CENTER_VISITS_AND_DOCTORS. */
  private static final String CENTER_VISITS_AND_DOCTORS = 
      " SELECT pr.*, d.doctor_name FROM patient_registration pr "
      + " LEFT JOIN doctors d ON (pr.doctor=d.doctor_id) " + " WHERE pr.mr_no=? ";

  /**
   * Gets the all center visits and doctors.
   *
   * @param mrNo
   *          the mr no
   * @return the all center visits and doctors
   */
  public static List<BasicDynaBean> getAllCenterVisitsAndDoctors(String mrNo) {
    StringBuilder query = new StringBuilder();
    query.append(CENTER_VISITS_AND_DOCTORS);
    int centerId = RequestContext.getCenterId();
    if (centerId != 0) {
      query.append(" AND center_id in (?) ");
      return DatabaseHelper.queryToDynaList(query.toString(), mrNo, centerId);
    } else {
      return DatabaseHelper.queryToDynaList(query.toString(), mrNo);
    }
  }

  /** The Constant VISIT_DETAILS. */
  private static final String VISIT_DETAILS = 
      "SELECT pr.*, tm.tpa_name, stm.tpa_name AS sec_tpa_name FROM patient_registration pr"
      + " LEFT OUTER JOIN tpa_master tm ON pr.primary_sponsor_id=tm.tpa_id"
      + " LEFT OUTER JOIN tpa_master stm ON pr.secondary_sponsor_id=stm.tpa_id"
      + " WHERE patient_id=? ";

  /**
   * Gets the visit details.
   *
   * @param visitId
   *          the visit id
   * @return the visit details
   */
  public BasicDynaBean getVisitDetails(String visitId) {
    return DatabaseHelper.queryToDynaBean(VISIT_DETAILS, visitId);
  }

  /** The Constant VISIT_DETAILS_WITH_REFERRAL_DOCTOR. */
  private static final String VISIT_DETAILS_WITH_REFERRAL_DOCTOR = 
      "SELECT pr.*, tm.tpa_name, stm.tpa_name AS sec_tpa_name, "
      + " COALESCE(drs.doctor_name, rd.referal_name) as prescribed_doctor_name "
      + " FROM patient_registration pr"
      + " LEFT OUTER JOIN tpa_master tm ON pr.primary_sponsor_id=tm.tpa_id"
      + " LEFT OUTER JOIN tpa_master stm ON pr.secondary_sponsor_id=stm.tpa_id"
      + " LEFT JOIN doctors drs ON pr.reference_docto_id::text = drs.doctor_id::text"
      + " LEFT JOIN referral rd ON pr.reference_docto_id::text = rd.referal_no::text"
      + " WHERE pr.patient_id=? ";
  
  /**
   * Gets the visit details.
   *
   * @param visitId
   *          the visit id
   * @return the visit details
   */
  public BasicDynaBean getVisitDetailsWithReferralDoctor(String visitId) {
    return DatabaseHelper.queryToDynaBean(VISIT_DETAILS_WITH_REFERRAL_DOCTOR, visitId);
  }
  
  /** The get episode all followup visits only. */
  private static String GET_EPISODE_ALL_FOLLOWUP_VISITS_ONLY = "SELECT *"
      + " FROM patient_registration WHERE main_visit_id ="
      + " (SELECT main_visit_id FROM patient_registration WHERE patient_id = ?)"
      + " AND op_type IN ('F','D') ";

  /**
   * Gets the episode all follow up visits only.
   *
   * @param visitId
   *          the visit id
   * @return the episode all follow up visits only
   */
  public List<BasicDynaBean> getEpisodeAllFollowUpVisitsOnly(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_EPISODE_ALL_FOLLOWUP_VISITS_ONLY, visitId);
  }

  /** The Constant GET_CENTER_ID. */
  private static final String GET_CENTER_ID = 
      " SELECT pr.center_id FROM patient_registration pr where pr.patient_id=?"
      + " UNION ALL" + " SELECT isr.center_id"
      + " FROM incoming_sample_registration isr where isr.incoming_visit_id=?" + " UNION ALL"
      + " SELECT src.center_id" + " FROM store_retail_customers src where src.customer_id=? ";

  /**
   * Gets the center id.
   *
   * @param obj
   *          the obj
   * @return the center id
   */
  public Integer getCenterId(Object[] obj) {
    return DatabaseHelper.getInteger(GET_CENTER_ID, obj);
  }

  /** The Constant GET_PATIENT_PREVIOUS_VISITS_DOCTOR. */
  private static final String GET_PATIENT_PREVIOUS_VISITS_DOCTOR = 
      " SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name" + " FROM patient_registration"
      + " WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time)"
      + " FROM patient_registration"
      + " WHERE mr_no=? AND doctor=? AND (op_type ='M' OR op_type = 'R')"
      + " AND visit_type = 'o' ORDER BY (reg_date + reg_time) DESC LIMIT 1)"
      + " AND mr_no=? AND doctor=? AND visit_type = 'o' AND op_type != 'D'" + " AND use_drg = 'N'"
      + " ORDER BY (reg_date + reg_time) ";

  /**
   * Gets the patient previous visits doctor.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @return the patient previous visits doctor
   */
  public List<BasicDynaBean> getPatientPreviousVisitsDoctor(String mrNo, String doctor) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_PREVIOUS_VISITS_DOCTOR,
        new Object[] { mrNo, doctor, mrNo, doctor });
  }

  /** The Constant GET_PATIENT_PREVIOUS_VISITS_BY_DOCTOR_AND_CENTER. */
  private static final String GET_PATIENT_PREVIOUS_VISITS_BY_DOCTOR_AND_CENTER = 
      " SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name" + " FROM patient_registration"
      + " WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time)"
      + " FROM patient_registration"
      + " WHERE mr_no=? AND doctor=? AND center_id=? AND (op_type ='M' OR op_type = 'R')"
      + " AND visit_type = 'o' ORDER BY (reg_date + reg_time) DESC LIMIT 1)"
      + " AND mr_no=? AND doctor=? AND center_id=? AND visit_type = 'o' AND op_type != 'D'"
      + " AND use_drg = 'N'" + " ORDER BY (reg_date + reg_time) ";

  /**
   * Gets the patient previous visits by doctor and center.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @param centerId
   *          the center id
   * @return the patient previous visits by doctor and center
   */
  public List<BasicDynaBean> getPatientPreviousVisitsByDoctorAndCenter(String mrNo, String doctor,
      int centerId) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_PREVIOUS_VISITS_BY_DOCTOR_AND_CENTER,
        new Object[] { mrNo, doctor, centerId, mrNo, doctor, centerId });
  }

  /** The Constant GET_PATIENT_PREVIOUS_VISIT_ID_IN_SAME_CENTER. */
  private static final String GET_PATIENT_PREVIOUS_VISIT_ID_IN_SAME_CENTER = 
      "SELECT patient_id from patient_registration where mr_no=?"
      + " AND center_id=? ORDER BY (reg_date + reg_time) desc limit 1 ";

  /**
   * Gets the patient previous visit id in same center.
   *
   * @param centerId
   *          the center id
   * @param mrNo
   *          the mr no
   * @return the patient previous visit id in same center
   */
  public String getPatientPreviousVisitIdInSameCenter(Integer centerId, String mrNo) {
    return DatabaseHelper.getString(GET_PATIENT_PREVIOUS_VISIT_ID_IN_SAME_CENTER,
        new Object[] { mrNo, centerId });
  }

  /** The Constant GET_PATIENT_PREVIOUS_VISIT_ID_ACROSS_CENTER. */
  private static final String GET_PATIENT_PREVIOUS_VISIT_ID_ACROSS_CENTER = 
      "SELECT patient_id from patient_registration"
      + " where mr_no=? ORDER BY (reg_date + reg_time) desc limit 1 ";

  /**
   * Gets the patient previous visit id across center.
   *
   * @param mrNo
   *          the mr no
   * @return the patient previous visit id across center
   */
  public String getPatientPreviousVisitIdAcrossCenter(String mrNo) {
    return DatabaseHelper.getString(GET_PATIENT_PREVIOUS_VISIT_ID_ACROSS_CENTER,
        new Object[] { mrNo });
  }

  /** The Constant GET_PATIENT_PREVIOUS_VISITS_DEPARTMENT. */
  private static final String GET_PATIENT_PREVIOUS_VISITS_DEPARTMENT = 
      "SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name, dept_name" + " FROM patient_registration"
      + " WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time) FROM patient_registration"
      + " WHERE mr_no=? AND doctor is not null AND trim(doctor) != ''"
      + " AND dept_name=(SELECT dept_id FROM doctors WHERE doctor_id =? )"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC LIMIT 1)"
      + " AND mr_no=? AND doctor is not null AND trim(doctor) != ''"
      + " AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?) AND visit_type = 'o'"
      + " AND op_type != 'D' AND use_drg = 'N'" + " ORDER BY (reg_date + reg_time) ";

  /**
   * Gets the patient previous visits dept.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @return the patient previous visits dept
   */
  public List<BasicDynaBean> getPatientPreviousVisitsDept(String mrNo, String doctor) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_PREVIOUS_VISITS_DEPARTMENT,
        new Object[] { mrNo, doctor, mrNo, doctor });
  }

  /** The Constant GET_PATIENT_PREVIOUS_VISITS_BY_DEPARTMENT_CENTER. */
  private static final String GET_PATIENT_PREVIOUS_VISITS_BY_DEPARTMENT_CENTER = 
      " SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name, dept_name" + " FROM patient_registration"
      + " WHERE (reg_date + reg_time) >= (SELECT (reg_date + reg_time) FROM patient_registration"
      + " WHERE mr_no=? AND doctor is not null AND trim(doctor) != ''" + " AND center_id=?"
      + " AND dept_name=(SELECT dept_id FROM doctors WHERE doctor_id =? )"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC LIMIT 1)"
      + " AND mr_no=? AND doctor is not null AND trim(doctor) != '' AND center_id=?"
      + " AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?)"
      + " AND visit_type = 'o' AND op_type != 'D' AND use_drg = 'N' "
      + " ORDER BY (reg_date + reg_time) ";

  /**
   * Gets the patient previous visits by dept and center.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @param centerId
   *          the center id
   * @return the patient previous visits by dept and center
   */
  public List<BasicDynaBean> getPatientPreviousVisitsByDeptAndCenter(String mrNo, String doctor,
      int centerId) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_PREVIOUS_VISITS_BY_DEPARTMENT_CENTER,
        new Object[] { mrNo, centerId, doctor, mrNo, centerId, doctor });
  }

  /** The Constant GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT_DETAILS. */
  private static final String GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT_DETAILS = 
      " SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name" + " FROM patient_registration "
      + " WHERE mr_no=? AND doctor=? "
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o' "
      + " ORDER BY (reg_date + reg_time) DESC LIMIT 1 ";

  /**
   * Gets the patient previous main visits doctor.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @return the patient previous main visits doctor
   */
  public List<BasicDynaBean> getPatientPreviousMainVisitsDoctor(String mrNo, String doctor) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT_DETAILS,
        new Object[] { mrNo, doctor });
  }

  /** The Constant GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT_DETAILS. */
  private static final String GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT_DETAILS = 
      "SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name, dept_name" + " FROM patient_registration"
      + " WHERE mr_no=? AND doctor is not null AND trim(doctor) != ''"
      + " AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?)"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC ";

  /**
   * Gets the patient previous main visits dept.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @return the patient previous main visits dept
   */
  public List<BasicDynaBean> getPatientPreviousMainVisitsDept(String mrNo, String doctor) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT_DETAILS,
        new Object[] { mrNo, doctor });
  }

  /** The Constant BILL_PATIENT_INFO. */
  /*
   * Returns the information combined for both patient and bill(required for order).
   */
  private static final String BILL_PATIENT_INFO = 
      "SELECT pr.mr_no, pr.patient_id, b.bill_no, b.is_tpa,"
      + " b.visit_type, pr.center_id, pr.user_name,"
      + " pr.bed_type, b.status, b.payment_status, b.bill_rate_plan_id,"
      + " ppip.plan_id as pri_plan_id, spip.plan_id as sec_plan_id,"
      + " 0 as commonorderid, 0 as appointmentId, 0 as packageref,"
      + " null as labno, null as radno FROM patient_registration pr"
      + " LEFT JOIN patient_insurance_plans ppip"
      + " on(pr.patient_id = ppip.patient_id and ppip.priority=1)"
      + " LEFT JOIN patient_insurance_plans spip"
      + " on(pr.patient_id = spip.patient_id and spip.priority=2)"
      + " JOIN bill b on(pr.patient_id = b.visit_id)"
      + " WHERE pr.patient_id= ? and b.bill_no = ? ";

  /**
   * Gets the patient bill info.
   *
   * @param patientID
   *          the patient ID
   * @param billNo
   *          the bill no
   * @return the patient bill info
   */
  public BasicDynaBean getPatientBillInfo(String patientID, String billNo) {
    return DatabaseHelper.queryToDynaBean(BILL_PATIENT_INFO, patientID, billNo);
  }

  /** The Constant PATIENT_INFO. */
  /*
   * Returns the information of patient
   */
  private static final String PATIENT_INFO = 
      "SELECT pr.mr_no, pr.patient_id, null, false, pr.visit_type, pr.center_id, pr.user_name,"
      + " pr.bed_type, null, null, pr.org_id AS bill_rate_plan_id,"
      + " ppip.plan_id as pri_plan_id, spip.plan_id as sec_plan_id,"
      + " 0 as commonorderid, 0 as appointmentId, 0 as packageref,"
      + " ppip.sponsor_id as pri_sponsor_id ,null as labno, null as radno"
      + " FROM patient_registration pr" + " LEFT JOIN patient_insurance_plans ppip"
      + " on(pr.patient_id = ppip.patient_id and ppip.priority=1)"
      + " LEFT JOIN patient_insurance_plans spip"
      + " on(pr.patient_id = spip.patient_id and spip.priority=2)" + " WHERE pr.patient_id= ? ";

  /**
   * Gets the patient info.
   *
   * @param patientID
   *          the patient ID
   * @return the patient info
   */
  public BasicDynaBean getPatientInfo(String patientID) {
    return DatabaseHelper.queryToDynaBean(PATIENT_INFO, patientID);
  }

  /** The Constant GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT. */
  private static final String GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT = 
      " SELECT patient_id, op_type, main_visit_id, (reg_date + reg_time)"
      + " AS visited_date, doctor AS doctor_name" + " FROM patient_registration"
      + " WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time)"
      + " FROM patient_registration" + " WHERE mr_no=? AND doctor=?"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC OFFSET 1 LIMIT 1)" + " AND mr_no=? AND doctor=?"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC ";

  /**
   * Gets the previous main visits doctor.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @return the previous main visits doctor
   */
  public List<BasicDynaBean> getPreviousMainVisitsDoctor(String mrNo, String doctor) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_DOCTOR_PREVIOUS_MAIN_VISIT,
        new Object[] { mrNo, doctor, mrNo, doctor });
  }

  /** The Constant GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT. */
  private static final String GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT = 
      "SELECT patient_id, op_type, main_visit_id,"
      + " (reg_date + reg_time) AS visited_date, doctor AS doctor_name, dept_name"
      + " FROM patient_registration"
      + " WHERE  (reg_date + reg_time) >= (SELECT (reg_date + reg_time)"
      + " FROM patient_registration"
      + " WHERE mr_no=? AND doctor is not null AND trim(doctor) != ''"
      + " AND dept_name=(SELECT dept_id FROM doctors WHERE doctor_id =? )"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC OFFSET 1 LIMIT 1)"
      + " AND mr_no=? AND doctor is not null AND trim(doctor) != ''"
      + " AND dept_name =(SELECT dept_id FROM doctors WHERE doctor_id = ?)"
      + " AND (op_type ='M' OR op_type = 'R') AND visit_type = 'o'"
      + " ORDER BY (reg_date + reg_time) DESC ";

  /**
   * Gets the previous main visits dept.
   *
   * @param mrNo
   *          the mr no
   * @param doctor
   *          the doctor
   * @return the previous main visits dept
   */
  public List<BasicDynaBean> getPreviousMainVisitsDept(String mrNo, String doctor) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_DEPARTMENT_PREVIOUS_MAIN_VISIT,
        new Object[] { mrNo, doctor, mrNo, doctor });
  }

  /** The Constant GET_MAIN_VISIT_ID. */
  private static final String GET_MAIN_VISIT_ID = 
      "SELECT main_visit_id FROM patient_registration WHERE patient_id = ? ";

  /**
   * Gets the main visit id.
   *
   * @param object
   *          the object
   * @return the main visit id
   */
  public String getMainVisitId(Object[] object) {
    return DatabaseHelper.getString(GET_MAIN_VISIT_ID, object);
  }

  /** The Constant GET_VISIT_TYPE. */
  private static final String GET_VISIT_TYPE = 
      "SELECT visit_type FROM patient_registration WHERE patient_id = ? ";

  /**
   * Gets the visit type.
   *
   * @param object
   *          the object
   * @return the visit type
   */
  public String getVisitType(Object[] object) {
    return DatabaseHelper.getString(GET_VISIT_TYPE, object);
  }

  /** The Constant LATEST_VISIT_FOR_MRNO. */
  private static final String LATEST_VISIT_FOR_MRNO = 
      " SELECT patient_id, visit_type FROM patient_registration pr WHERE mr_no=?  ";

  /**
   * Gets the patient latest visit.
   *
   * @param mrNo
   *          the mr no
   * @param active
   *          the active
   * @param visitType
   *          the visit type
   * @param centerId
   *          the center id
   * @return the patient latest visit
   */
  public BasicDynaBean getPatientLatestVisit(String mrNo, Boolean active, String visitType,
      Integer centerId) {
    StringBuilder query = new StringBuilder(LATEST_VISIT_FOR_MRNO);
    if (active != null) {
      if (active) {
        query.append(" AND status='A' ");
      } else {
        query.append(" AND status='I' ");
      }
    }

    if (visitType != null && !visitType.equals("")) {
      query.append(" AND visit_type = '" + DatabaseHelper.quoteIdent(visitType) + "' ");
    }
    if (centerId != null) {
      query.append(" AND center_id = '" + centerId + "' ");
    }
    query.append(" ORDER BY (reg_date+reg_time) DESC LIMIT 1 ");
    return DatabaseHelper.queryToDynaBean(query.toString(), mrNo);
  }

  /** The Constant GET_PREVIOUS_VISITS_FOR_MAIN_VISIT. */
  private static final String GET_PREVIOUS_VISITS_FOR_MAIN_VISIT = 
      "SELECT pr.mr_no, pr.patient_id,pr.op_type,main_visit_id, d.doctor_name,"
      + " t1.tpa_name as primary_sponsor_name, t2.tpa_name as secondary_sponsor_name, "
      + " date(dc.presc_date) as prescription_date" + " FROM patient_registration pr "
      + " LEFT JOIN doctors d on (pr.doctor = d.doctor_id) " + " LEFT JOIN doctor_consultation dc"
      + " on (dc.patient_id = pr.patient_id and dc.doctor_name = pr.doctor)"
      + " LEFT JOIN tpa_master t1 ON (pr.primary_sponsor_id = t1.tpa_id) "
      + " LEFT JOIN tpa_master t2 ON (pr.secondary_sponsor_id = t2.tpa_id) "
      + " WHERE main_visit_id = ? ORDER BY (reg_date + reg_time) DESC ";

  /**
   * List previous visits for main visit.
   *
   * @param mainVisit
   *          the main visit
   * @return the list
   */
  public List<BasicDynaBean> listPreviousVisitsForMainVisit(String mainVisit) {
    return DatabaseHelper.queryToDynaList(GET_PREVIOUS_VISITS_FOR_MAIN_VISIT, mainVisit);
  }

  /** The Constant GET_PREVIOUS_CONSULTATIONS_FOR_MR_NO. */
  private static final String GET_PREVIOUS_CONSULTATIONS_FOR_MR_NO = 
      "SELECT pr.mr_no, pr.patient_id,pr.op_type,main_visit_id, d.doctor_name,"
      + " t1.tpa_name as primary_sponsor_name, t2.tpa_name as secondary_sponsor_name, "
      + " dc.consultation_id, extract(epoch from dc.visited_date) as visited_date, "
      + " date(dc.presc_date) as prescription_date" + " FROM patient_registration pr "
      + " JOIN doctor_consultation dc" + " on (dc.patient_id = pr.patient_id)"
      + " LEFT JOIN doctors d on (dc.doctor_name = d.doctor_id) "
      + " LEFT JOIN tpa_master t1 ON (pr.primary_sponsor_id = t1.tpa_id) "
      + " LEFT JOIN tpa_master t2 ON (pr.secondary_sponsor_id = t2.tpa_id) "
      + " WHERE pr.mr_no = ? ";
  
  private static final String ONE_YEAR_CHECK = " AND date(dc.presc_date) >="
      + " (select current_timestamp - (coalesce(prescription_validity::Integer,365)"
      + " || ' days ')::interval   from generic_preferences)";

  /**
   * List previous visits for main visit.
   *
   * @param mrNo
   *          the mr no
   * @return the list
   */
  public List<BasicDynaBean> listPreviousConsultations(String mrNo) {
    return listPreviousConsultations(mrNo, false);
  }
  
  /**
   * List previous visits for main visit.
   *
   * @param mrNo
   *          the mr no
   * @param durationCheck
   *          the duration check
   * @return the list
   */
  public List<BasicDynaBean> listPreviousConsultations(String mrNo, boolean durationCheck) {
    String query = GET_PREVIOUS_CONSULTATIONS_FOR_MR_NO;
    if (durationCheck) {
      query = query + ONE_YEAR_CHECK;
    }
    return DatabaseHelper.queryToDynaList(query, mrNo);
  }

  /** The get prev visit bill wise dues. */
  private static String GET_PREV_VISIT_BILL_WISE_DUES = "SELECT bill_no, "
      + " (total_amount + insurance_deduction + total_tax"
      + " - total_receipts - total_claim - deposit_set_off" + " - total_claim_tax ) as due_amount"
      + " FROM patient_registration pr" + " JOIN bill b  ON (pr.patient_id = b.visit_id)"
      + " WHERE b.status !='X' AND mr_no = ? and center_id = ? and"
      + " (total_amount + insurance_deduction + total_tax"
      + " - total_receipts - total_claim - deposit_set_off - total_claim_tax ) != 0 ";

  /**
   * Gets the all prev visit bill wise dues.
   *
   * @param mrno
   *          the mrno
   * @param centerId
   *          the center id
   * @return the all prev visit bill wise dues
   */
  public List<BasicDynaBean> getAllPrevVisitBillWiseDues(String mrno, int centerId) {
    return DatabaseHelper.queryToDynaList(GET_PREV_VISIT_BILL_WISE_DUES,
        new Object[] { mrno, centerId });
  }

  /** The get patient family bills total. */
  private static String GET_PATIENT_FAMILY_BILLS_TOTAL = " SELECT sum(total_amount) as total_amount"
      + " FROM patient_registration pr" + " JOIN bill b  ON (pr.patient_id = b.visit_id)"
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no)"
      + " WHERE b.status !='X' AND pd.family_id = ?"
      + " AND extract('year' from pr.reg_date) = extract('year' from current_date) ";

  /**
   * Gets the patient family bills total.
   *
   * @param familyId
   *          the family id
   * @return the patient family bills total
   */
  public BasicDynaBean getPatientFamilyBillsTotal(String familyId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_FAMILY_BILLS_TOTAL, familyId);
  }

  /**
   * Gets the patient visit details map.
   *
   * @param patientId
   *          the patient id
   * @return the patient visit details map
   */
  public Map getPatientVisitDetailsMap(String patientId) {
    List list = DatabaseHelper.queryToDynaList(GET_PATIENT_VISIT_DETAILS, patientId);
    if (list != null && list.size() > 0) {
      BasicDynaBean bean = (BasicDynaBean) list.get(0);
      boolean precise = (bean.get("dateofbirth") != null);
      if (bean.get("expected_dob") != null) {
        bean.set("age_text",
            DateUtil.getAgeText((java.sql.Date) bean.get("expected_dob"), precise));
      }
      return bean.getMap();
    }
    return null;
  }

  /** The Constant GET_PATIENT_ICD_CODES. */
  private static final String GET_PATIENT_ICD_CODES = 
      " SELECT textcat_commacat(icd_code_value) as icd_codes"
      + " FROM (select icd_code||'('|| (case when diag_type = 'P'"
      + " then 'Primary' else 'Secondary' end)||')' as icd_code_value"
      + " FROM mrd_diagnosis where visit_id = ?) as foo ";

  /**
   * Gets the patient icd codes.
   *
   * @param patientId
   *          the patient id
   * @return the patient icd codes
   */
  public String getPatientIcdCodes(String patientId) {
    return DatabaseHelper.getString(GET_PATIENT_ICD_CODES, patientId);
  }

  /** The Constant GET_PATIENT_CPT_CODES. */
  private static final String GET_PATIENT_CPT_CODES = 
      " SELECT textcat_commacat(cpt_code_value) as cpt_codes"
      + " FROM (select act_rate_plan_item_code as cpt_code_value" + " FROM bill_charge bc"
      + " JOIN bill b ON(bc.bill_no = b.bill_no)"
      + " WHERE b.visit_id = ? AND bc.code_type = 'CPT') as foo ";

  /**
   * Gets the patient cpt codes.
   *
   * @param patientId
   *          the patient id
   * @return the patient cpt codes
   */
  public String getPatientCptCodes(String patientId) {
    return DatabaseHelper.getString(GET_PATIENT_CPT_CODES, patientId);
  }

  /** The Constant GET_OP_PATIENT_PRESCRIBED_CPT_CODES. */
  private static final String GET_OP_PATIENT_PRESCRIBED_CPT_CODES =
      "SELECT textcat_commacat(item_code) as cpt_item_code"
      + " FROM (SELECT item_code" + " FROM patient_prescription pp" + " JOIN doctor_consultation dc"
      + " on (pp.consultation_id = dc.consultation_id)"
      + " JOIN patient_test_prescriptions ptp ON ( pp.patient_presc_id = ptp.op_test_pres_id)"
      + " JOIN test_org_details tod ON(tod.test_id = ptp.test_id AND tod.org_id = ? )"
      + " WHERE patient_id = ?" + " UNION ALL" + " SELECT item_code"
      + " FROM patient_prescription pp"
      + " JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id)"
      + " JOIN patient_service_prescriptions psp"
      + " ON ( pp.patient_presc_id = psp.op_service_pres_id)"
      + " JOIN service_org_details sod ON(sod.service_id = psp.service_id AND sod.org_id = ? )"
      + " WHERE patient_id = ?" + " UNION ALL" + " SELECT item_code"
      + " FROM patient_prescription pp"
      + " JOIN doctor_consultation dc on (pp.consultation_id = dc.consultation_id)"
      + " JOIN patient_operation_prescriptions pop"
      + " ON ( pp.patient_presc_id = pop.prescription_id)" + " JOIN operation_org_details ood"
      + " ON(ood.operation_id = pop.operation_id AND ood.org_id = ? )"
      + " WHERE patient_id = ? ) AS foo ";

  /**
   * Gets the op patient prescribed cpt codes.
   *
   * @param patientId
   *          the patient id
   * @return the op patient prescribed cpt codes
   */
  public String getOpPatientPrescribedCptCodes(String patientId) {
    BasicDynaBean patientDetailBean = getPatientVisitDetailsBean(patientId);
    String orgId = (String) patientDetailBean.get("org_id");
    return DatabaseHelper.getString(GET_OP_PATIENT_PRESCRIBED_CPT_CODES, orgId, patientId, orgId,
        patientId, orgId, patientId);
  }

  /** The Constant VISITID_SEQUENCE_PATTERN. */
  private static final String VISITID_SEQUENCE_PATTERN = 
      " SELECT pattern_id FROM hosp_op_ip_seq_prefs"
      + " WHERE priority = (" + " SELECT min(priority) FROM hosp_op_ip_seq_prefs"
      + " WHERE (visit_type = ?) AND " + " (center_id = ? OR center_id = 0) " + " ) ";

  /**
   * Gets the visit id pattern.
   *
   * @param visitType
   *          the visit type
   * @param centerId
   *          the center id
   * @return the visit id pattern
   */
  public String getVisitIdPattern(String visitType, int centerId) {
    BasicDynaBean patternBean = DatabaseHelper.queryToDynaBean(VISITID_SEQUENCE_PATTERN,
        new Object[] { visitType, centerId });
    return (String) patternBean.get("pattern_id");
  }

  /**
   * Gets the next visit id.
   *
   * @param visitType
   *          the visit type
   * @param centerId
   *          the center id
   * @return the next visit id
   */
  public String getNextVisitId(String visitType, int centerId) {
    String patternId = getVisitIdPattern(visitType, centerId);
    return DatabaseHelper.getNextPatternId(patternId);
  }

  /** The Constant GET_PATIENT_PLANS_DETAILS. */
  public static final String GET_PATIENT_PLANS_DETAILS = 
      "SELECT pip.insurance_co, pip.sponsor_id, pip.plan_id, "
      + "pip.plan_type_id, ppd.member_id, ppd.policy_number,ppd.policy_validity_start, "
      + "ppd.policy_validity_end, "
      + "ppd.policy_holder_name, ppd.patient_relationship, pip.patient_policy_id, "
      + "ppd.mr_no, pip.priority, icm.insurance_co_id, icm.insurance_co_name,"
      + "pip.use_drg, pip.use_perdiem, pip.per_diem_code, pip.insurance_approval,ppd.status, "
      + "pip.plan_limit,pip.visit_limit,pip.visit_deductible,pip.visit_copay_percentage,"
      + "pip.visit_max_copay_percentage, "
      + "pip.visit_per_day_limit,pip.episode_limit,pip.episode_deductible,"
      + "pip.episode_copay_percentage,"
      + "pip.episode_max_copay_percentage,pip.mr_no,pip.patient_id,pip.codification_status, "
      + "pip.codified_by,pip.codification_remarks,ppd.status,"
      + "pip.drg_code,pip.per_diem_code,ppd.visit_id,pr.visit_type,"
      + "pip.patient_insurance_plans_id, icm.insurance_co_name,ipm.limits_include_followup,"
      + "pip.utilization_amount,(select discount_plan_name from discount_plan_main where "
      + "discount_plan_id = ipm.discount_plan_id) as discount_plan_name, pdd.doc_id "
      + "FROM patient_insurance_plans pip " + "LEFT JOIN insurance_plan_main ipm USING (plan_id) "
      + "JOIN patient_registration pr ON pr.patient_id=pip.patient_id "
      + "LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + "LEFT JOIN plan_docs_details pdd on pdd.patient_policy_id= ppd.patient_policy_id "
      + "LEFT JOIN insurance_company_master icm USING (insurance_co_id) "
      + "WHERE pip.mr_no= ? AND pip.patient_id = ? "
      + "AND pip.use_drg = 'N' ORDER BY pip.priority ";

  /**
   * Gets the previous visit patient plan details.
   *
   * @param mrNo
   *          the mr no
   * @param visitId
   *          the visit id
   * @return the previous visit patient plan details
   */
  public List<BasicDynaBean> getPreviousVisitPatientPlanDetails(String mrNo, String visitId) {
    return DatabaseHelper.queryToDynaList(GET_PATIENT_PLANS_DETAILS,
        new Object[] { mrNo, visitId });
  }

  /** The Constant GET_VISIT_RATE_PLAN. */
  private static final String GET_VISIT_RATE_PLAN = " SELECT od.org_name, od.org_id"
      + " FROM patient_registration pr" + " JOIN organization_details od ON od.org_id = pr.org_id"
      + " WHERE pr.patient_id = ? AND pr.visit_type = ? ;";

  /**
   * Gets the visit rate plan.
   *
   * @param visitId
   *          the visit id
   * @param visitType
   *          the visit type
   * @return the visit rate plan
   */
  public BasicDynaBean getVisitRatePlan(String visitId, String visitType) {
    return DatabaseHelper.queryToDynaBean(GET_VISIT_RATE_PLAN, new Object[] { visitId, visitType });
  }

  /** The Constant GET_VISIT_RATE_PLAN_WITH_OUT_TYPE. */
  private static final String GET_VISIT_RATE_PLAN_WITH_OUT_TYPE = " SELECT od.*"
      + " FROM patient_registration pr" + " JOIN organization_details od ON od.org_id = pr.org_id"
      + " WHERE pr.patient_id = ? ";

  /**
   * Gets the visit rate plan.
   *
   * @param visitId
   *          the visit id
   * @return the visit rate plan
   */
  public BasicDynaBean getVisitRatePlan(String visitId) {
    return DatabaseHelper.queryToDynaBean(GET_VISIT_RATE_PLAN_WITH_OUT_TYPE,
        new Object[] { visitId });
  }

  /** The Constant GET_VISITS_WITH_ORDER. */
  public static final String GET_VISITS_WITH_ORDER = "SELECT distinct pr.patient_id, pr.status"
      + " FROM patient_registration pr " + " JOIN bill b on pr.patient_id = b.visit_id"
      + " JOIN bill_charge bc on (bc.bill_no = b.bill_no AND bc.order_number IS NOT NULL )"
      + " WHERE pr.mr_no= ? AND pr.visit_type = ? AND pr.center_id = ? ";

  /**
   * To get all visits having Order.
   *
   * @param mrNo
   *          the mr no
   * @param visitType
   *          the visit type
   * @param centerId
   *          the center id
   * @return the patient visits having order
   */
  public List<BasicDynaBean> getPatientVisitsHavingOrder(String mrNo, String visitType,
      Integer centerId) {
    return DatabaseHelper.queryToDynaList(GET_VISITS_WITH_ORDER,
        new Object[] { mrNo, visitType, centerId });
  }

  /** The Constant GET_PATIENT_DETAILS. */
  private static final String GET_PATIENT_DETAILS = "SELECT pra.patient_id as visit_id,"
      + " pra.reg_date, to_char(pra.reg_time,'hh24:mi') as reg_time,"
      + " pra.status, pra.visit_type, pra.center_id, dept.dept_name,"
      + " dept.dept_id, doc.doctor_name, doc.doctor_id, pra.op_type,"
      + " pd.patient_gender, pra.bed_type, pra.org_id" + " FROM patient_registration pra"
      + " JOIN patient_details pd on pd.mr_no = pra.mr_no"
      + " LEFT JOIN department dept on pra.dept_name = dept.dept_id"
      + " LEFT JOIN doctors doc on pra.doctor = doc.doctor_id ";

  /**
   * Returns Patient Information Used for Visit DropDown Card.
   *
   * @param visitIdList
   *          the visit id list
   * @param centerId
   *          the center id
   * @return the patient details
   */
  public List<BasicDynaBean> getPatientDetails(List<String> visitIdList, Integer centerId) {

    StringBuffer query = new StringBuffer();
    query = query.append(GET_PATIENT_DETAILS);

    String[] placeholdersArr = new String[visitIdList.size()];
    Arrays.fill(placeholdersArr, "?");
    query.append(" WHERE pra.patient_id IN ( ")
        .append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");

    List<Object> params = new ArrayList<Object>();
    params.addAll(visitIdList);

    if (centerId != null && centerId != 0) {
      query.append("AND pra.center_id = ? ");
      params.add(centerId);
    }

    return DatabaseHelper.queryToDynaList(query.toString(), params.toArray());
  }

  /** The Constant GET_VISIT_CODIFICATION_STATUS. */
  private static final String GET_VISIT_CODIFICATION_STATUS = " SELECT codification_status"
      + " FROM patient_registration" + " WHERE patient_id = ? ";

  /**
   * Gets the visit codification status.
   *
   * @param patientId
   *          the patient id
   * @return the visit codification status
   */
  public String getVisitCodificationStatus(String patientId) {
    return DatabaseHelper.getString(GET_VISIT_CODIFICATION_STATUS, new Object[] { patientId });
  }

  /** The Constant GET_VISIT_REFERRAL_DOCTOR. */
  private static final String GET_VISIT_REFERRAL_DOCTOR = 
      "select COALESCE(drs.doctor_name, rd.referal_name)"
      + " AS refdoctorname FROM patient_registration pra"
      + " LEFT JOIN doctors drs ON pra.reference_docto_id::text = drs.doctor_id::text"
      + " LEFT JOIN referral rd ON pra.reference_docto_id::text = rd.referal_no::text"
      + " where pra.patient_id = ?";

  /**
   * Gets the visit referral doctor name.
   *
   * @param patientId
   *          the patient id
   * @return the visit referral doctor name
   */
  public String getVisitReferralDoctorName(String patientId) {
    return DatabaseHelper.getString(GET_VISIT_REFERRAL_DOCTOR, new Object[] { patientId });
  }

  /** The Constant PATIENT_VISIT_WITH_BED_TYPE_DETAILS. */
  private static final String PATIENT_VISIT_WITH_BED_TYPE_DETAILS = 
      "SELECT coalesce(bn.bed_type, pr.bed_type) as bed_type,"
      + " * FROM patient_registration  pr"
      + " LEFT JOIN admission ad ON ad.patient_id = pr.patient_id"
      + " LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id" + " WHERE pr.patient_id = ?";

  /**
   * Gets the patient bean with bed type.
   *
   * @param patientId
   *          the patient id
   * @return the patient bean with bed type
   */
  public BasicDynaBean getPatientBeanWithBedType(String patientId) {

    return DatabaseHelper.queryToDynaBean(PATIENT_VISIT_WITH_BED_TYPE_DETAILS, patientId);
  }

  /** The Constant GET_IP_CREDITLIMIT. */
  public static final String GET_IP_CREDITLIMIT = " SELECT ip_credit_limit_amount "
      + " FROM patient_registration WHERE patient_id = ? ";

  /**
   * Gets the ip credit limit amount.
   *
   * @param visitId
   *          the visit id
   * @return the ip credit limit amount
   */
  public BigDecimal getIpCreditLimitAmount(String visitId) {
    if (visitId == null || (visitId.trim()).equals("")) {
      return null;
    }
    return DatabaseHelper.getBigDecimal(GET_IP_CREDITLIMIT, new Object[] { visitId });
  }

  /** The Constant GET_LAST_VISIT_DATE. */
  private static final String GET_LAST_VISIT_DATE = "SELECT reg_date "
      + "FROM patient_registration WHERE mr_no=? AND center_id=? AND dept_name=? "
      + "ORDER BY (reg_date+reg_time) DESC LIMIT 1";

  /**
   * Gets the last vist date.
   *
   * @param centerId
   *          the center id
   * @param mrno
   *          the mr no
   * @param deptname
   *          the dept name
   * @return the last vist date
   */
  public Date getLastVistDate(Integer centerId, String mrno, String deptname) {
    BasicDynaBean regDateBean = DatabaseHelper.queryToDynaBean(GET_LAST_VISIT_DATE,
        new Object[] { mrno, centerId, deptname });
    return regDateBean != null ? (Date) regDateBean.get("reg_date") : null;
  }

  
  public boolean getPatientHasVisitsExistByDoctor(String mrNo, String doctorId) {
    return getPatientHasVisitsExistByDoctor(mrNo, doctorId, null, false);
  }
  
  /** The Constant GET_PATIENT_HAS_VISITS_EXIST_BY_DOCTOR. */
  private static final String GET_PATIENT_HAS_VISITS_EXIST_BY_DOCTOR = " SELECT"
      + " count(*)::INTEGER AS total_consultation_count FROM doctor_consultation dc "
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (dc.cancel_status IS NULL OR dc.cancel_status != 'C')"
      + " AND dc.mr_no = ? and dc.doctor_name=? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient has visits exist by doctor.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @return the patient has visits exist by doctor
   */
  public boolean getPatientHasVisitsExistByDoctor(String mrNo, String doctorId, String sponsorId,
      Boolean considerSponsor) {

    StringBuilder query = new StringBuilder(GET_PATIENT_HAS_VISITS_EXIST_BY_DOCTOR);
    List<Object> queryParams = new ArrayList<>();
    queryParams.add(mrNo);
    queryParams.add(doctorId);

    if (considerSponsor) {
      if (StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id IS NULL ");
      } else {
        query.append(" AND pr.primary_sponsor_id = ? ");
        queryParams.add(sponsorId);
      }
    }

    return DatabaseHelper.getInteger(query.toString(),
        queryParams.toArray()) > 0;
  }

  /** The Constant GET_PATIENT_HAS_VISITS_EXIST_BY_DOCTOR_IN_CENTER. */
  private static final String GET_PATIENT_HAS_VISITS_EXIST_BY_DOCTOR_IN_CENTER = " SELECT"
      + " count(*)::INTEGER AS total_consultation_count FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (cancel_status IS NULL OR cancel_status != 'C')"
      + " AND dc.mr_no =? AND doctor_name=? AND pr.center_id=? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient has visits exist by doctor in center.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param centerId
   *          the center id
   * @return the patient has visits exist by doctor in center
   */
  public boolean getPatientHasVisitsExistByDoctorInCenter(String mrNo, String doctorId,
      int centerId) {
    return getPatientHasVisitsExistByDoctorInCenter(mrNo, doctorId, centerId, null, false);
  }
  
  
  /**
   * Gets the patient has visits exist by doctor in center.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param centerId
   *          the center id
   * @param sponsorId
   *          the sponsor id
   * @param considerSponsor
   *          consider or ignore sponsor         
   * @return the patient has visits exist by doctor in center
   */
  public boolean getPatientHasVisitsExistByDoctorInCenter(String mrNo, String doctorId,
      int centerId, String sponsorId, Boolean considerSponsor) {

    StringBuilder query = new StringBuilder(GET_PATIENT_HAS_VISITS_EXIST_BY_DOCTOR_IN_CENTER);
    List<Object> queryParams = new ArrayList<>();

    queryParams.add(mrNo);
    queryParams.add(doctorId);
    queryParams.add(centerId);

    if (considerSponsor) {
      if (StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id IS NULL ");
      } else {
        query.append(" AND pr.primary_sponsor_id  = ? ");
        queryParams.add(sponsorId);
      }
    }

    return DatabaseHelper.getInteger(query.toString(), queryParams.toArray()) > 0;
  }

  /** The Constant GET_PATIENT_HAS_VISITS_EXIST_BY_DEPARTMENT. */
  private static final String GET_PATIENT_HAS_VISITS_EXIST_BY_DEPARTMENT = " SELECT"
      + " count(*)::INTEGER AS total_consultation_count FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (cancel_status IS NULL OR cancel_status != 'C') AND dc.mr_no = ?"
      + " AND pr.dept_name=? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient has visits exist by department.
   *
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @return the patient has visits exist by department
   */
  public boolean getPatientHasVisitsExistByDepartment(String mrNo, String departmentId) {
    return getPatientHasVisitsExistByDepartment(mrNo, departmentId, null, false);
  }

  /**
   * Gets the patient has visits exist by department.
   *
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param sponsorId
   *          the sponsor id
   * @param considerSponsor
   *          consider or ignore sponsor         
   * @return the patient has visits exist by department
   */
  public boolean getPatientHasVisitsExistByDepartment(String mrNo, String departmentId,
      String sponsorId, Boolean considerSponsor) {

    StringBuilder query = new StringBuilder(GET_PATIENT_HAS_VISITS_EXIST_BY_DEPARTMENT);
    List<Object> queryParams = new ArrayList<>();

    queryParams.add(mrNo);
    queryParams.add(departmentId);

    if (considerSponsor) {
      if (StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id IS NULL ");
      } else {
        query.append(" AND pr.primary_sponsor_id  = ? ");
        queryParams.add(sponsorId);
      }
    }

    return DatabaseHelper.getInteger(query.toString(), queryParams.toArray()) > 0;
  }

  /** The Constant GET_PATIENT_HAS_VISITS_EXIST_BY_DEPARTMENT_IN_CENTER. */
  private static final String GET_PATIENT_HAS_VISITS_EXIST_BY_DEPARTMENT_IN_CENTER = " SELECT"
      + " count(*)::INTEGER AS total_consultation_count FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (cancel_status IS NULL OR cancel_status != 'C')"
      + " AND dc.mr_no =? AND pr.dept_name=? AND pr.center_id=? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient has visits exist by department in center.
   *
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param centerId
   *          the center id
   * @return the patient has visits exist by department in center
   */
  public boolean getPatientHasVisitsExistByDepartmentInCenter(String mrNo, String departmentId,
      int centerId) {
    return getPatientHasVisitsExistByDepartmentInCenter(mrNo, departmentId, centerId, null, false);
  }

  /**
   * Gets the patient has visits exist by department in center.
   *
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param centerId
   *          the center id
   * @param sponsorId
   *          the sponsor id
   * @param considerSponsor
   *          consider or ignore sponsor                  
   * @return the patient has visits exist by department in center
   */
  public boolean getPatientHasVisitsExistByDepartmentInCenter(String mrNo, String departmentId,
      int centerId, String sponsorId, Boolean considerSponsor) {
    
    StringBuilder query = new StringBuilder(GET_PATIENT_HAS_VISITS_EXIST_BY_DEPARTMENT_IN_CENTER);
    List<Object> queryParams = new ArrayList<>();
    queryParams.add(mrNo);
    queryParams.add(departmentId);
    queryParams.add(centerId);

    if (considerSponsor) {
      if (StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id IS NULL ");
      } else {
        query.append(" AND pr.primary_sponsor_id  = ? ");
        queryParams.add(sponsorId);
      }
    }
    return DatabaseHelper.getInteger(query.toString(),
        queryParams.toArray()) > 0;
  }

  /** The Constant GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS. */
  private static final String GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS = " SELECT dc.patient_id,"
      + " dc.visited_date,pr.main_visit_id" + " FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (dc.cancel_status IS NULL OR dc.cancel_status != 'C')"
      + " AND pr.op_type IN (:main_visit_type_list)"
      + " AND dc.mr_no =:mr_no AND dc.doctor_name=:doctor_id "
      + " AND ct.skip_for_followup_count = 'N' "
      + "  # order by pr.reg_date desc, pr.reg_time desc limit 1 ";

  /**
   * Gets the patient main visit within validity days.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param validityDays
   *          the validity days
   * @return the patient main visit within validity days
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDays(String consultationValidityUnits,
      String mrNo, String doctorId, int validityDays) {

    return getPatientMainVisitWithinValidityDays(consultationValidityUnits, mrNo, doctorId,
        validityDays, DEFAULT_VISIT_LIST, null, false);
  }

  /**
   * Gets the patient main visit within validity days.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param validityDays
   *          the validity days
   * @return the patient main visit within validity days
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDays(String consultationValidityUnits,
      String mrNo, String doctorId, int validityDays, List<String> mainVisitTypeList,
      String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND dc.visited_date::DATE > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText.append(" AND dc.visited_date > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id = :primary_sponsor_id");
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL");

      }
      
    }


    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("mr_no", mrNo);
    parameterSource.addValue("doctor_id", doctorId);
    parameterSource.addValue("main_visit_type_list", mainVisitTypeList);
    parameterSource.addValue("primary_sponsor_id", sponsorId);
    StringBuilder query = new StringBuilder(
        GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS.replaceAll("#", replaceText.toString()));
    return DatabaseHelper.queryToDynaBean(query.toString(), parameterSource);
  }

  /** The Constant GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_BY_CENTER. */
  private static final String GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_BY_CENTER = " SELECT"
      + " dc.patient_id, dc.visited_date, pr.main_visit_id" + " FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (dc.cancel_status IS NULL OR dc.cancel_status != 'C')"
      + " AND pr.op_type IN (:main_visit_type_list) AND"
      + " dc.mr_no =:mr_no AND dc.doctor_name=:doctor_id "
      + " AND ct.skip_for_followup_count = 'N'"
      + " # AND pr.center_id=:center_id order by pr.reg_date desc, pr.reg_time desc limit 1 ";

  /**
   * Gets the patient main visit within validity days by center.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param validityDays
   *          the validity days
   * @param centerId
   *          the center id
   * @return the patient main visit within validity days by center
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDaysByCenter(
      String consultationValidityUnits, String mrNo, String doctorId, int validityDays,
      int centerId) {
    return getPatientMainVisitWithinValidityDaysByCenter(consultationValidityUnits, mrNo, doctorId,
        validityDays, centerId, DEFAULT_VISIT_LIST, null, false);
  }

  /**
   * Gets the patient main visit within validity days by center.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param validityDays
   *          the validity days
   * @param centerId
   *          the center id
   * @return the patient main visit within validity days by center
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDaysByCenter(
      String consultationValidityUnits, String mrNo, String doctorId, int validityDays,
      int centerId, List<String> mainVistTypeList, String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND dc.visited_date::DATE > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText.append(" AND dc.visited_date > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id=:primary_sponsor_id ");
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }
    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("main_visit_type_list", mainVistTypeList);
    parameterSource.addValue("mr_no", mrNo);
    parameterSource.addValue("doctor_id", doctorId);
    parameterSource.addValue("center_id", centerId);
    parameterSource.addValue("primary_sponsor_id", sponsorId);

    StringBuilder query = new StringBuilder(GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_BY_CENTER
        .replaceAll("#", replaceText.toString()));
    return DatabaseHelper.queryToDynaBean(query.toString(), parameterSource);
  }

  /** The Constant GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_IN_DEPARTMENT. */
  private static final String GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_IN_DEPARTMENT = " SELECT"
      + " dc.patient_id, dc.visited_date,pr.main_visit_id" + " FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (dc.cancel_status IS NULL OR dc.cancel_status != 'C')"
      + " AND pr.op_type IN (:main_visit_type_list)"
      + " AND dc.mr_no =:mr_no AND pr.dept_name=:dept_id "
      + " AND ct.skip_for_followup_count = 'N'"
      + " # order by pr.reg_date desc, pr.reg_time desc limit 1 ";

  /**
   * Gets the patient main visit within validity days in department.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param validityDays
   *          the validity days
   * @return the patient main visit within validity days in department
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDaysInDepartment(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays) {
    return getPatientMainVisitWithinValidityDaysInDepartment(consultationValidityUnits, mrNo,
        departmentId, validityDays, DEFAULT_VISIT_LIST, null, false);
  }

  /**
   * Gets the patient main visit within validity days in department.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param validityDays
   *          the validity days
   * @return the patient main visit within validity days in department
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDaysInDepartment(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays,
      List<String> visitTypeList, String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND dc.visited_date::DATE > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText.append(" AND dc.visited_date > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id=:primary_sponsor_id ");
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL ");        
      }
    }

    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("main_visit_type_list", visitTypeList);
    parameterSource.addValue("mr_no", mrNo);
    parameterSource.addValue("dept_id", departmentId);
    parameterSource.addValue("primary_sponsor_id", sponsorId);
    StringBuilder query = new StringBuilder(
        GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_IN_DEPARTMENT.replaceAll("#",
            replaceText.toString()));
    return DatabaseHelper.queryToDynaBean(query.toString(), parameterSource);
  }

  /** The Constant GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_IN_DEPARTMENT_BY_CENTER. */
  private static final String GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_IN_DEPARTMENT_BY_CENTER =
      " SELECT dc.patient_id, dc.visited_date, pr.main_visit_id"
      + " FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (dc.cancel_status IS NULL OR dc.cancel_status != 'C')"
      + " AND pr.op_type IN (:main_visit_type_list) AND dc.mr_no =:mr_no AND pr.dept_name=:dept_id"
      + " AND ct.skip_for_followup_count = 'N'"
      + " # AND pr.center_id=:center_id order by pr.reg_date desc, pr.reg_time desc limit 1 ";

  /**
   * Gets the patient main visit within validity days in department by center.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param validityDays
   *          the validity days
   * @param centerId
   *          the center id
   * @return the patient main visit within validity days in department by center
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDaysInDepartmentByCenter(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays,
      int centerId) {
    return getPatientMainVisitWithinValidityDaysInDepartmentByCenter(consultationValidityUnits,
        mrNo, departmentId, validityDays, centerId, DEFAULT_VISIT_LIST, null, false);
  }

  /**
   * Gets the patient main visit within validity days in department by center.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param validityDays
   *          the validity days
   * @param centerId
   *          the center id
   * @return the patient main visit within validity days in department by center
   */
  public BasicDynaBean getPatientMainVisitWithinValidityDaysInDepartmentByCenter(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays,
      int centerId, List<String> visitTypeList, String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND dc.visited_date::DATE > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText.append(" AND dc.visited_date > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id=:primary_sponsor_id ");
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    parameterSource.addValue("mr_no", mrNo);
    parameterSource.addValue("dept_id", departmentId);
    parameterSource.addValue("center_id", centerId);
    parameterSource.addValue("main_visit_type_list", visitTypeList);
    parameterSource.addValue("primary_sponsor_id", sponsorId);
    String query = 
        GET_PATIENT_MAIN_VISIT_WITHIN_VALIDITY_DAYS_IN_DEPARTMENT_BY_CENTER.replaceAll("#",
        replaceText.toString());
    return DatabaseHelper.queryToDynaBean(query, parameterSource);
  }

  /** The Constant GET_PATIENT_LATEST_MAIN_VISIT. */
  private static final String GET_PATIENT_LATEST_MAIN_VISIT = " SELECT dc.patient_id,"
      + " dc.visited_date,pr.main_visit_id FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " WHERE pr.op_type IN ('M','R') AND dc.mr_no =? AND dc.doctor_name=? "
      + " order by dc.visited_date desc limit 1 ";

  /**
   * Gets the patient latest main visit bean.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @return the patient latest main visit bean
   */
  public BasicDynaBean getPatientLatestMainVisitBean(String mrNo, String doctorId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_LATEST_MAIN_VISIT,
        new Object[] { mrNo, doctorId });
  }

  /** The Constant GET_PATIENT_LATEST_MAIN_VISIT_BY_CENTER. */
  private static final String GET_PATIENT_LATEST_MAIN_VISIT_BY_CENTER = " SELECT"
      + " dc.patient_id, dc.visited_date, pr.main_visit_id FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " WHERE pr.op_type IN ('M','R') AND dc.mr_no =? AND dc.doctor_name=? AND pr.center_id=?"
      + " order by dc.visited_date desc limit 1 ";

  /** The Constant GET_NEXT_UNIDENTIFIED_PATIENT. */
  private static final String GET_NEXT_UNIDENTIFIED_PATIENT = 
      "SELECT nextval('unidentified_patient_seq')";

  /**
   * Gets the next unidentified patient number.
   *
   * @return the next unidentified patient number
   */
  public Integer getNextUnidentifiedPatientNumber() {
    return DatabaseHelper.getInteger(GET_NEXT_UNIDENTIFIED_PATIENT);
  }

  /**
   * Gets the patient latest main visit bean by center.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param centerId
   *          the center id
   * @return the patient latest main visit bean by center
   */
  public BasicDynaBean getPatientLatestMainVisitBeanByCenter(String mrNo, String doctorId,
      int centerId) {
    return DatabaseHelper.queryToDynaBean(GET_PATIENT_LATEST_MAIN_VISIT_BY_CENTER,
        new Object[] { mrNo, doctorId, centerId });
  }

  /** The Constant GET_PATIENT_FOLLOWUP_COUNT_BY_DOCTOR. */
  private static final String GET_PATIENT_FOLLOWUP_COUNT_BY_DOCTOR = " SELECT count(*)::INTEGER"
      + " AS followup_count FROM doctor_consultation dc"
      + " LEFT JOIN patient_registration pr ON (dc.patient_id = pr.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (dc.cancel_status IS NULL OR dc.cancel_status != 'C') AND"
      + " dc.mr_no =? and dc.doctor_name=? AND dc.visited_date > ? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient followup count by doctor.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param visitedDate
   *          the visited date
   * @return the patient followup count by doctor
   */
  public Integer getPatientFollowupCountByDoctor(String mrNo, String doctorId,
      Timestamp visitedDate) {
    return getPatientFollowupCountByDoctor(mrNo, doctorId, visitedDate, null, false);
  }

  /**
   * Gets the patient followup count by doctor.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param visitedDate
   *          the visited date
   * @param sponsorId
   *          the sponsor id, null to not consider sponsor, '$' to get cash patients 
   * @return the patient followup count by doctor
   */
  public Integer getPatientFollowupCountByDoctor(String mrNo, String doctorId,
      Timestamp visitedDate, String sponsorId, Boolean considerSponsor) {

    StringBuilder query = new StringBuilder(GET_PATIENT_FOLLOWUP_COUNT_BY_DOCTOR);
    List<Object> queryParams = new ArrayList<>();

    queryParams.add(mrNo);
    queryParams.add(doctorId);
    queryParams.add(visitedDate);

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id = ? ");
        queryParams.add(sponsorId);
      } else {
        query.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    return DatabaseHelper.getInteger(query.toString(), queryParams.toArray());
  }

  /** The Constant GET_PATIENT_FOLLOWUP_COUNT_BY_DOCTOR_IN_CENTER. */
  private static final String GET_PATIENT_FOLLOWUP_COUNT_BY_DOCTOR_IN_CENTER = " SELECT"
      + " count(*)::INTEGER AS followup_count FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (cancel_status IS NULL OR cancel_status != 'C') AND"
      + " dc.mr_no=? and doctor_name=? AND visited_date > ? AND pr.center_id=? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient followup count by doctor in center.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param visitedDate
   *          the visited date
   * @param centerId
   *          the center id
   * @return the patient followup count by doctor in center
   */
  public Integer getPatientFollowupCountByDoctorInCenter(String mrNo, String doctorId,
      Timestamp visitedDate, int centerId, String sponsorId, Boolean considerSponsor) {

    StringBuilder query = new StringBuilder(GET_PATIENT_FOLLOWUP_COUNT_BY_DOCTOR_IN_CENTER);
    List<Object> queryParams = new ArrayList<Object>();
    queryParams.add(mrNo);
    queryParams.add(doctorId);
    queryParams.add(visitedDate);
    queryParams.add(centerId);

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id=? ");
        queryParams.add(sponsorId);
      } else {
        query.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    return DatabaseHelper.getInteger(query.toString(), queryParams.toArray());
  }

  public Integer getPatientFollowupCountByDoctorInCenter(String mrNo, String doctorId,
      Timestamp visitedDate, int centerId) {
    return getPatientFollowupCountByDoctorInCenter(mrNo, doctorId, visitedDate,
      centerId, null, false);
  }

  /** The Constant GET_PATIENT_FOLLOWUP_COUNT_BY_DEPARTMENT. */
  private static final String GET_PATIENT_FOLLOWUP_COUNT_BY_DEPARTMENT = " SELECT"
      + " count(*)::INTEGER AS followup_count FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref"
      + " IS NULL AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (cancel_status IS NULL OR cancel_status != 'C') AND dc.mr_no = ?"
      + " AND pr.dept_name = ? AND visited_date > ? "
      + " AND ct.skip_for_followup_count = 'N'";

  /**
   * Gets the patient followup count by department.
   *
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param visitedDate
   *          the visited date
   * @return the patient followup count by department
   */
  public Integer getPatientFollowupCountByDepartment(String mrNo, String departmentId,
      Timestamp visitedDate, String sponsorId, Boolean considerSponsor) {
    StringBuilder query = new StringBuilder(GET_PATIENT_FOLLOWUP_COUNT_BY_DEPARTMENT);
    List<Object> queryParams = new ArrayList<Object>();

    queryParams.add(mrNo);
    queryParams.add(departmentId);
    queryParams.add(visitedDate);
    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id=? ");
        queryParams.add(sponsorId);
      } else {
        query.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    return DatabaseHelper.getInteger(query.toString(), queryParams.toArray());
  }

  public Integer getPatientFollowupCountByDepartment(String mrNo, String departmentId,
      Timestamp visitedDate) {
    return getPatientFollowupCountByDepartment(mrNo, departmentId, visitedDate, null, false);
  }

  /** The Constant GET_PATIENT_FOLLOWUP_COUNT_BY_DEPARTMENT_IN_CENTER. */
  private static final String GET_PATIENT_FOLLOWUP_COUNT_BY_DEPARTMENT_IN_CENTER = " SELECT"
      + " count(*)::INTEGER AS followup_count FROM doctor_consultation dc"
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id)"
      + " LEFT JOIN consultation_types ct ON (dc.operation_ref IS NULL"
      + " AND dc.head = ct.consultation_type_id::text) "
      + " WHERE (cancel_status IS NULL OR cancel_status != 'C') AND dc.mr_no = ?"
      + " AND pr.dept_name = ? AND visited_date > ? AND pr.center_id =? "
      + " AND ct.skip_for_followup_count = 'N'";


  /**
   * Gets the patient followup count by department in center.
   *
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param visitedDate
   *          the visited date
   * @param centerId
   *          the center id
   * @return the patient followup count by department in center
   */
  public Integer getPatientFollowupCountByDepartmentInCenter(String mrNo, String departmentId,
      Timestamp visitedDate, int centerId, String sponsorId, Boolean considerSponsor) {

    StringBuilder query = new StringBuilder(GET_PATIENT_FOLLOWUP_COUNT_BY_DEPARTMENT_IN_CENTER);
    List<Object> queryParams = new ArrayList<Object>();

    queryParams.add(mrNo);
    queryParams.add(departmentId);
    queryParams.add(visitedDate);
    queryParams.add(centerId);

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        query.append(" AND pr.primary_sponsor_id = ? ");
        queryParams.add(sponsorId);
      } else {
        query.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    return DatabaseHelper.getInteger(query.toString(), queryParams.toArray());
  }

  public Integer getPatientFollowupCountByDepartmentInCenter(String mrNo, String departmentId,
      Timestamp visitedDate, int centerId) {
    return getPatientFollowupCountByDepartmentInCenter(mrNo, departmentId, visitedDate, centerId,
        null, false);
  }

  /** The Constant GET_ALL_IP_VISITS. */
  private static final String GET_ALL_IP_VISITS = "SELECT pra.patient_id as visit_id,"
      + " pra.reg_date, pra.reg_time, pra.status as visit_status, pra.ipemr_status, "
      + " dept.dept_name, doc.doctor_name as admitting_doctor, 'C' as bill_type, "
      + " pra.doctor as admitting_doctor_id, dept.dept_id" + " FROM patient_registration pra"
      + " LEFT JOIN visit_care_team vct ON (vct.patient_id = pra.patient_id)"
      + " LEFT JOIN department dept ON (pra.admitted_dept = dept.dept_id)"
      + " JOIN doctors doc ON (pra.doctor = doc.doctor_id)"
      + " WHERE pra.mr_no IN (SELECT mr_no from patient_details"
      + " WHERE original_mr_no = ? or mr_no = ?)" + " AND pra.visit_type = 'i' AND pra.mr_no=? ";

  /**
   * Gets the ip patient visit list.
   *
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param centerId
   *          the center id
   * @return the ip patient visit list
   */
  public List<BasicDynaBean> getIpPatientVisitList(String mrNo, String doctorId, Integer centerId) {
    StringBuilder query = new StringBuilder(GET_ALL_IP_VISITS);
    List<Object> queryParamneters = new ArrayList<>();
    queryParamneters.add(mrNo);
    queryParamneters.add(mrNo);
    queryParamneters.add(mrNo);
    if (doctorId != null && !doctorId.equals("")) {
      query.append(" AND vct.care_doctor_id= ? ");
      queryParamneters.add(doctorId);
    }
    if (centerId != 0) {
      query.append(" AND pra.center_id = ? ");
      queryParamneters.add(centerId);
    }
    query.append(" ORDER BY pra.reg_date DESC, pra.reg_time DESC ");
    return DatabaseHelper.queryToDynaList(query.toString(), queryParamneters.toArray());

  }

  /** The Constant GET_IP_PATIENT_VISIT_INFO. */
  private static final String GET_IP_PATIENT_VISIT_INFO = "select pr.patient_id, dep.dept_id,"
      + " dep.dept_name, dept_type_id,"
      + " doc.doctor_id as admitting_doctor_id, doc.doctor_name as admitting_doctor,"
      + " pr.plan_id, pr.center_id, pr.bed_type, pr.discharge_date,"
      + " pr.discharge_prescription_notes,"
      + " pr.org_id, wn.ward_name, pr.ward_id, pr.bed_type, bn.bed_name,"
      + " pr.plan_id, pr.center_id, patient_discharge_status as discharge_status, pr.ipemr_status,"
      + " reg_date, reg_time," + " '' as logged_in_doctor," + " '' as logged_in_doctor_id,"
      + " '' as prescription_note_taker,"
      + " COALESCE(refA.doctor_name, refB.referal_name) as referal_doctor,"
      + " CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31"
      + " THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer"
      + " WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730"
      + " THEN (floor((current_date -"
      + " COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer"
      + " ELSE (floor((current_date -"
      + " COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer" + " END AS age,"
      + " (CASE WHEN pd.patient_gender='F' THEN 'FEMALE'"
      + " WHEN pd.patient_gender='M' THEN 'MALE'     ELSE 'NONE' END )"
      + " AS patient_gender, COALESCE(adm.isbaby,'')" + " AS isbaby, pr.ipemr_revision_number,"
      + " pip.sponsor_id as tpa_id, org.org_name, org.pharmacy_discount_type,"
      + " org.pharmacy_discount_percentage " + " from patient_registration pr"
      + " JOIN patient_details pd ON pr.mr_no = pd.mr_no"
      + " LEFT JOIN admission adm ON (adm.patient_id = pr.patient_id)"
      + " LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id)"
      + " LEFT JOIN ward_names wn ON (wn.ward_no = pr.ward_id)"
      + " LEFT JOIN department dep ON (dep.dept_id = pr.dept_name)"
      + " LEFT JOIN doctors refA ON (refA.doctor_id=pr.reference_docto_id)"
      + " LEFT JOIN referral refB ON (refB.referal_no=pr.reference_docto_id)"
      + " LEFT JOIN doctors doc ON (doc.doctor_id = pr.doctor)"
      + " LEFT JOIN patient_insurance_plans pip ON(pip.patient_id = pr.patient_id)"
      + " JOIN organization_details org ON (org.org_id = pr.org_id) " + " where pr.patient_id= ? ";

  /**
   * Gets the patient visit summary info.
   *
   * @param patientId
   *          the patient id
   * @return the patient visit summary info
   */
  public BasicDynaBean getPatientVisitSummaryInfo(String patientId) {
    StringBuilder query = new StringBuilder(GET_IP_PATIENT_VISIT_INFO);
    List<Object> queryParamneters = new ArrayList<>();
    queryParamneters.add(patientId);
    return DatabaseHelper.queryToDynaBean(query.toString(), queryParamneters.toArray());
  }

  /** The Constant GET_ALL_VISIT_DETAILS. */
  private static final String GET_ALL_VISIT_DETAILS = "SELECT pr.patient_id, pr.status, "
      + " pr.reg_date, pr.reg_time, doc.doctor_name, dep.dept_name, tpa.tpa_name "
      + " FROM patient_registration pr " + " LEFT JOIN patient_insurance_plans pip ON "
      + " (pip.patient_id = pr.patient_id and pip.priority = 1) "
      + " LEFT JOIN store_retail_customers phc ON (pr.patient_id = phc.customer_id) "
      + " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = pr.patient_id) "
      + " LEFT JOIN tpa_master tpa ON tpa.tpa_id = pip.sponsor_id "
      + " LEFT JOIN department dep  ON dep.dept_id = pr.dept_name "
      + " LEFT JOIN doctors doc ON doc.doctor_id = pr.doctor "
      + " WHERE pr.mr_no = ? and pr.visit_type = ? ";

  /** The Constant CENTER_ID_CONDITION. */
  private static final String CENTER_ID_CONDITION = 
      " and coalesce(pr.center_id, isr.center_id, phc.center_id) = ? ";

  /** The Constant ORDER_BY. */
  private static final String ORDER_BY = " ORDER BY pr.patient_id DESC, "
      + " pr.reg_date DESC, pr.reg_time DESC ";

  /**
   * get all visit Details for a patient on basis of visit type.
   * 
   * @param mrNo
   *          the mrNo
   * @param visitType
   *          the visitType
   * @param centerID
   *          the centerID
   * @return list of basic dyna bean
   */
  public List<BasicDynaBean> getAllVisitDetails(String mrNo, String visitType, int centerID) {
    if (centerID != 0) {
      return DatabaseHelper.queryToDynaList(GET_ALL_VISIT_DETAILS + CENTER_ID_CONDITION + ORDER_BY,
          mrNo, visitType, centerID);
    } else {
      return DatabaseHelper.queryToDynaList(GET_ALL_VISIT_DETAILS + ORDER_BY, mrNo, visitType);
    }
  }

  /** The Constant GET_PATIENT_LAST_IP_VISIT. */
  private static final String GET_PATIENT_LAST_IP_VISIT = "SELECT patient_id, "
      + " discharge_date+discharge_time as discharge_date_time, "
      + " doctor AS admitting_doctor, null as visited_date, pr.main_visit_id "
      + " FROM patient_registration pr "
      + " WHERE mr_no = ? AND doctor = ? AND visit_type='i' AND status='I'  # "
      + " ORDER BY discharge_date DESC,discharge_time DESC LIMIT 1";

  public BasicDynaBean getPatientIpVisitWithinValidityDays(String consultationValidityUnits,
      String mrNo, String doctorId, int validityDays) {
    return getPatientIpVisitWithinValidityDays(consultationValidityUnits, mrNo, doctorId,
        validityDays, null, false);
  }

  /**
   * Gets the patient ip visit within validity days.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param validityDays
   *          the validity days
   * @return the patient ip visit within validity days
   */
  public BasicDynaBean getPatientIpVisitWithinValidityDays(String consultationValidityUnits,
      String mrNo, String doctorId, int validityDays, String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND discharge_date > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText.append(" AND (discharge_date+discharge_time) > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    List<Object> queryParams = new ArrayList<>();
    queryParams.add(mrNo);
    queryParams.add(doctorId);

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id=? ");
        queryParams.add(sponsorId);
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    String query = GET_PATIENT_LAST_IP_VISIT;
    String replaceString = query.replaceAll("#", replaceText.toString());
    return DatabaseHelper.queryToDynaBean(replaceString, queryParams.toArray());
  }

  public BasicDynaBean getPatientIpVisitWithinValidityDaysByCenter(String consultationValidityUnits,
      String mrNo, String doctorId, int validityDays, int centerId) {
    return getPatientIpVisitWithinValidityDaysByCenter(consultationValidityUnits, mrNo, doctorId,
        validityDays, centerId, null, false);
  }

  /**
   * Gets the patient ip visit within validity days by center.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param doctorId
   *          the doctor id
   * @param validityDays
   *          the validity days
   * @param centerId
   *          the center id
   * @return the patient ip visit within validity days by center
   */
  public BasicDynaBean getPatientIpVisitWithinValidityDaysByCenter(String consultationValidityUnits,
      String mrNo, String doctorId, int validityDays, int centerId, String sponsorId,
      Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND center_id = ? ")
          .append("AND discharge_date > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText
          .append(
              " AND center_id = ? AND (discharge_date+discharge_time) > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    List<Object> queryParams = new ArrayList<>();
    queryParams.add(mrNo);
    queryParams.add(doctorId);
    queryParams.add(centerId);

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id=? ");
        queryParams.add(sponsorId);
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    String query = GET_PATIENT_LAST_IP_VISIT;
    String replaceString = query.replaceAll("#", replaceText.toString());
    return DatabaseHelper.queryToDynaBean(replaceString, queryParams.toArray());
  }

  /** The Constant GET_PATIENT_LAST_IP_VISIT_FOR_DEPARTMENT. */
  private static final String GET_PATIENT_LAST_IP_VISIT_FOR_DEPARTMENT = "SELECT patient_id, "
      + " discharge_date+discharge_time as discharge_date_time, "
      + " doctor AS admitting_doctor, null as visited_date, pr.main_visit_id "
      + " FROM patient_registration pr"
      + " WHERE mr_no =? AND dept_name = ? AND visit_type='i' AND status='I' # "
      + " ORDER BY discharge_date DESC,discharge_time DESC LIMIT 1";

  public BasicDynaBean getPatientIpVisitWithinValidityDaysInDepartment(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays) {
    return getPatientIpVisitWithinValidityDaysInDepartment(consultationValidityUnits, mrNo,
        departmentId, validityDays, null, false);
  }

  /**
   * Gets the patient ip visit within validity days in department.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param validityDays
   *          the validity days
   * @return the patient ip visit within validity days in department
   */
  public BasicDynaBean getPatientIpVisitWithinValidityDaysInDepartment(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays,
      String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND discharge_date > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText.append(" AND (discharge_date+discharge_time) > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    List<Object> queryParams = new ArrayList<>();
    queryParams.add(mrNo);
    queryParams.add(departmentId);
    
    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id=? ");
        queryParams.add(sponsorId);
      } else {
        replaceText.append(" AND pr.primary_sponsor_id is NULL ");
      }
    }

    String query = GET_PATIENT_LAST_IP_VISIT_FOR_DEPARTMENT;
    String replaceString = query.replaceAll("#", replaceText.toString());
    return DatabaseHelper.queryToDynaBean(replaceString, queryParams.toArray());
  }

  public BasicDynaBean getPatientIpVisitWithinValidityDaysInDepartmentByCenter(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays,
      int centerId) {
    return getPatientIpVisitWithinValidityDaysInDepartmentByCenter(consultationValidityUnits, mrNo,
        departmentId, validityDays, centerId, null, false);
  }

  /**
   * Gets the patient ip visit within validity days in department by center.
   *
   * @param consultationValidityUnits
   *          the consultation validity units
   * @param mrNo
   *          the mr no
   * @param departmentId
   *          the department id
   * @param validityDays
   *          the validity days
   * @param centerId
   *          the center id
   * @return the patient ip visit within validity days in department by center
   */

  public BasicDynaBean getPatientIpVisitWithinValidityDaysInDepartmentByCenter(
      String consultationValidityUnits, String mrNo, String departmentId, int validityDays,
      int centerId, String sponsorId, Boolean considerSponsor) {
    StringBuilder replaceText = new StringBuilder();
    if (consultationValidityUnits.equals("D")) {
      replaceText.append(" AND center_id = ? ")
          .append("AND discharge_date > (now()::DATE - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    } else {
      replaceText
          .append(
              " AND center_id = ? AND " + "(discharge_date+discharge_time) > (now() - interval '")
          .append(String.valueOf(validityDays)).append(" days')");
    }

    List<Object> queryParams = new ArrayList<>();

    queryParams.add(mrNo);
    queryParams.add(departmentId);
    queryParams.add(centerId);

    if (considerSponsor) {
      if (!StringUtils.isEmpty(sponsorId)) {
        replaceText.append(" AND pr.primary_sponsor_id = ? ");
        queryParams.add(sponsorId);
      }
    } else {
      replaceText.append(" AND pr.primary_sponsor_id is NULL ");
    }

    String query = GET_PATIENT_LAST_IP_VISIT_FOR_DEPARTMENT;
    String replaceString = query.replaceAll("#", replaceText.toString());
    return DatabaseHelper.queryToDynaBean(replaceString, queryParams.toArray());
  }

  /** The Constant GET_LATEST_IP_VISIT_BY_DOCTOR_OR_DEPT. */
  private static final String GET_LATEST_IP_VISIT_BY_DOCTOR_OR_DEPT = " select"
      + " dept_name, doctor"
      + " from patient_registration pr WHERE visit_type = 'i' and mr_no = ? ";

  /**
   * Gets the latest ip visit BY doctor or dept.
   *
   * @param mrNo
   *          the mr no
   * @param centerId
   *          the center id
   * @param doctorId
   *          the doctor id
   * @param dept
   *          the dept
   * @return the latest ip visit BY doctor or dept
   */
  public BasicDynaBean getLatestIpVisitBYDoctorOrDept(String mrNo, Integer centerId,
      String doctorId, String dept) {
    StringBuilder sb = new StringBuilder(GET_LATEST_IP_VISIT_BY_DOCTOR_OR_DEPT);
    List<Object> argValues = new ArrayList<Object>();
    argValues.add(mrNo);
    if (doctorId != null) {
      sb.append(" AND doctor = ? ");
      argValues.add(doctorId);
    } else {
      sb.append(" AND dept_name = ? ");
      argValues.add(dept);
    }
    if (centerId != null) {
      sb.append(" AND center_id = ? ");
      argValues.add(centerId);
    }
    sb.append(" order by reg_date desc, reg_time desc limit 1");
    return DatabaseHelper.queryToDynaBean(sb.toString(), argValues.toArray());
  }

  private static final String GET_OP_ADMISSION_DATA = "SELECT pd.mr_no,pra.patient_id,"
      + "pra.reg_date, pra.reg_time, sm.salutation || ' ' || patient_name || "
      + "case when coalesce(middle_name, '') = '' then '' else (' ' || middle_name) end || "
      + "case when coalesce(last_name, '') = '' then '' else (' ' || last_name) end as full_name,"
      + " hcm.center_name, hcm.center_code, pra.complaint, pra.user_name as admitted_by,"
      + "  dep.dept_name, pra.dept_name AS dept_id, pd.patient_phone, pd.patient_care_oftext,"
      + " pd.relation,dr.doctor_name, COALESCE(drs.doctor_name, rd.referal_name) AS refdoctorname,"
      + "dr.doctor_mobile,pra.doctor, pra.reference_docto_id,  sm.salutation, dr.specialization,"
      + " pd.visit_id, pd.email_id  FROM patient_registration pra "
      + "JOIN patient_details pd ON pra.mr_no = pd.mr_no "
      + "LEFT JOIN salutation_master sm ON pd.salutation = sm.salutation_id "
      + "LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pra.center_id) "
      + "LEFT JOIN doctors dr ON dr.doctor_id = pra.doctor "
      + "LEFT JOIN department dep ON pra.dept_name = dep.dept_id "
      + "LEFT JOIN doctors drs ON pra.reference_docto_id = drs.doctor_id "
      + "LEFT JOIN referral rd ON pra.reference_docto_id = rd.referal_no "
      + " WHERE ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ) AND patient_id= ? ";

  /**
   * Gets OP admission data for a visit.
   *
   * @param patientId
   *          visit id
   * @return OP adminssion data as a Basicdynabean
   */
  public BasicDynaBean getOPAdmissionData(String patientId) {
    return DatabaseHelper.queryToDynaBean(GET_OP_ADMISSION_DATA, new Object[] { patientId });
  }
  
  /** The Constant GET_PATIENT_VISIT_INSURANCE_DETAILS. */
  private static final String GET_PATIENT_VISIT_INSURANCE_DETAILS = " SELECT "
      + " pra.patient_id, pcd.sponsor_id AS corporate_sponsor_id, pra.patient_id AS visit_id, "
      + " pnd.sponsor_id AS national_sponsor_id, "
      + " spcd.sponsor_id AS sec_corporate_sponsor_id, spnd.sponsor_id AS sec_national_sponsor_id, "
      + " pra.primary_sponsor_id, pra.secondary_sponsor_id, pra.primary_insurance_approval,"
      + " pra.secondary_insurance_approval, pd.dateofbirth, pra.reg_date, "
      + " pra.patient_category_id AS  patient_category, pra.org_id, pra.visit_type, "
      + " pra.primary_insurance_co, pra.plan_id, pra.use_perdiem, "
      + " tpa.sponsor_type AS sponsor_type , stpa.sponsor_type AS sec_sponsor_type, "
      + " pra.secondary_insurance_co,pcd.patient_relationship AS patient_corporate_relation, "
      + " spcd.patient_relationship AS sec_patient_corporate_relation, "
      + " pcd.employee_id, pcd.employee_name, spcd.employee_id AS sec_employee_id, "
      + " spcd.employee_name AS sec_employee_name, pnd.national_id, pnd.citizen_name,"
      + " pnd.patient_relationship AS patient_national_relation, "
      + " spnd.national_id AS sec_national_id,spnd.citizen_name AS sec_citizen_name, "
      + " spnd.patient_relationship AS sec_patient_national_relation,pd.category_expiry_date, "
      + " pra.op_type, pra.mr_no, pra.use_drg "
      + " FROM patient_registration pra "
      + " JOIN patient_details pd ON (pra.mr_no = pd.mr_no) "
      + " LEFT JOIN patient_corporate_details pcd "
      + "   ON (pcd.patient_corporate_id = pra.patient_corporate_id)"
      + " LEFT JOIN patient_national_sponsor_details pnd "
      + "   ON (pnd.patient_national_sponsor_id = pra.patient_national_sponsor_id)"
      + " LEFT JOIN patient_corporate_details spcd "
      + "   ON (spcd.patient_corporate_id = pra.secondary_patient_corporate_id) "
      + " LEFT JOIN patient_national_sponsor_details spnd "
      + "   ON (spnd.patient_national_sponsor_id = pra.secondary_patient_national_sponsor_id) "
      + " LEFT JOIN tpa_master tpa ON (tpa.tpa_id = pra.primary_sponsor_id) "
      + " LEFT JOIN tpa_master stpa ON (stpa.tpa_id = pra.secondary_sponsor_id) "
      + " WHERE pra.patient_id = ? ";

  /**
   * Gets the patient visit insurance details map.
   *
   * @param visitId
   *          the patient id
   * @return the patient visit insurance details map
   */
  public Map getPatientVisitInsuranceDetailsMap(String visitId) {
    List list = DatabaseHelper.queryToDynaList(GET_PATIENT_VISIT_INSURANCE_DETAILS, visitId);
    if (list != null && !list.isEmpty()) {
      BasicDynaBean bean = (BasicDynaBean) list.get(0);
      return bean.getMap();
    }
    return null;
  }

  private static final String GET_IPEMR_FORM_SAVE_EVENT_DATA =
      "SELECT pr.ipemr_complete_time, d.doctor_name, pr.patient_id, pr.mr_no, pr.ipemr_reopened,"
          + "d.doc_first_name,d.doc_middle_name,d.doc_last_name,d.doctor_license_number"
          + "FROM patient_registration pr JOIN doctors d ON (d.doctor_id=pr.doctor)"
          + "WHERE pr.ipemr_status='C' AND visit_type='i' AND patient_id=?";

  /**
   * Get IpEmr Form Save Event Segment Data
   *
   * @param patientId the visit id.
   * @return bean
   */
  public BasicDynaBean getIpEmrFormSaveEventSegmentData(String patientId) {
    return DatabaseHelper.queryToDynaBean(GET_IPEMR_FORM_SAVE_EVENT_DATA, patientId);
  }
}
