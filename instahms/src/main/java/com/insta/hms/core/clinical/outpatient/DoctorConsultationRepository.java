package com.insta.hms.core.clinical.outpatient;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.consultation.prescriptions.PendingPrescriptionsRepository;
import com.insta.hms.core.clinical.order.master.OrderItemRepository;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class DoctorConsultationRepository.
 */
@Repository
public class DoctorConsultationRepository extends OrderItemRepository {
  
  /** The session service. */
  @LazyAutowired
  SessionService sessionService;
  
  /** The user service. */
  @LazyAutowired
  private UserService userService;


  /** The pending prescriptions repository. */
  @LazyAutowired
  private PendingPrescriptionsRepository pendingPrescriptionsRepository;
  
  /**
   * Instantiates a new doctor consultation repository.
   */
  public DoctorConsultationRepository() {
    super("doctor_consultation");
  }

  /** The Constant CONSULTATION_ID. */
  private static final String CONSULTATION_ID = "consultation_id";

  /** The Constant GET_VISIT_CONSULTATIONS. */
  private static final String GET_VISIT_CONSULTATIONS = "SELECT "
      + " mr_no, patient_id, doctor_id, d.doctor_name,consultation_id, head, consultation_type,"
      + " date(presc_date) AS presc_date, dc.cancel_status, d.dept_id, dep.dept_name, "
      + " consultation_token FROM doctor_consultation dc "
      + " JOIN doctors d on(dc.doctor_name = doctor_id) "
      + " JOIN department dep ON (dep.dept_id = d.dept_id) "
      + " JOIN consultation_types ct on(ct.consultation_type_id::text = dc.head)"
      + " WHERE patient_id = ? ";

  /**
   * List visit consultations.
   *
   * @param visitId the visit id
   * @return the list
   */
  public List<BasicDynaBean> listVisitConsultations(String visitId) {
    return DatabaseHelper.queryToDynaList(GET_VISIT_CONSULTATIONS, visitId);
  }

  /** The Constant CONSULT_FIELD_VALUES. */
  private static final String CONSULT_FIELD_VALUES =
      " SELECT dc.doc_id, dc.template_id, dc.patient_id, pcfv.value_id, pcfv.field_id, "
          + " pcfv.field_value, 'Consultation Notes' as field_name, 'Y' as print_column"
          + " FROM doctor_consultation dc JOIN patient_consultation_field_values pcfv "
          + "   ON (dc.doc_id=pcfv.doc_id and pcfv.field_id = -1) WHERE consultation_id=?";
  
  /** The Constant PRINTABLE_COLUMNS. */
  private static final String PRINTABLE_COLUMNS = " AND print_column='Y' ";
  
  /** The Constant VALUE_NOT_EMPTY_COLUMNS. */
  private static final String VALUE_NOT_EMPTY_COLUMNS = " AND coalesce(field_value, '')!=''";

  /**
   * Gets the consultation fields values.
   *
   * @param consultationId the consultation id
   * @param allFields : false : returns only the columns(fields and values) which are set to
   *        printable in master. true : returs all the fields.
   * @param notEmptyFieldValues the not empty field values
   * @return the consultation fields values
   */
  public List getConsultationFieldsValues(int consultationId, boolean allFields,
      boolean notEmptyFieldValues) {
    String fieldsValues = CONSULT_FIELD_VALUES;
    if (notEmptyFieldValues) {
      fieldsValues += VALUE_NOT_EMPTY_COLUMNS;
    }
    return DatabaseHelper.queryToDynaList(fieldsValues, new Object[] {consultationId});
  }

  /** The Constant CONSULTING_DOCTOR_DETAIL. */
  private static final String CONSULTING_DOCTOR_DETAIL =
      "SELECT dc.consultation_id, dc.mr_no, d.doctor_name, d.doctor_id, dc.patient_id, "
          + " '' as description, '' as doctor_notes, '' as remarks, '' as diagnosis,"
          + " prescription_notes, dc.status, '' as nursing_assessment,"
          + " dc.immunization_status_upto_date, dc.emergency_category,"
          + " dc.consultation_complete_time, dc.immunization_remarks, d.registration_no,"
          + " start_datetime, end_datetime, dept.dept_id, dc.doc_id, dc.template_id "
          + " FROM doctor_consultation dc JOIN doctors d ON (dc.doctor_name=d.doctor_id) "
          + " JOIN department dept ON (dept.dept_id = d.dept_id)"
          + " WHERE dc.consultation_id = ?";

  /**
   * Gets the doctor consult details.
   *
   * @param consultId the consult id
   * @return the doctor consult details
   */
  public BasicDynaBean getDoctorConsultDetails(int consultId) {
    List<BasicDynaBean> list =
        DatabaseHelper.queryToDynaList(CONSULTING_DOCTOR_DETAIL, new Object[] {consultId});
    if (list != null && !list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  /** The Constant GENERATE_NEXT_SEQUENCE_QUERY. */
  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  /* (non-Javadoc)
   * @see com.insta.hms.common.GenericRepository#getNextSequence()
   */
  public Integer getNextSequence() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY, "doctor_consultation_sequence");
  }

  /** The Constant GET_CONSULTATION_SUMMARY_DATA. */
  private static final String GET_CONSULTATION_SUMMARY_DATA = " SELECT pd.mr_no,"
      + " pr.patient_id as visit_id, pr.org_id, pr.op_type as visit_type, pr.bed_type,"
      + " pr.plan_id, pr.use_perdiem, pr.per_diem_code, d.doctor_id as admitting_doctor_id,"
      + " pr.reference_docto_id, doc.dept_id AS dept_name, pr.center_id, pr.bed_type, "
      + " pip.sponsor_id as tpa_id, ipm.require_pbm_authorization,"
      + " org.pharmacy_discount_type, org.pharmacy_discount_percentage,"
      + " dc.doctor_name as consulting_doctor_id,dc.start_datetime as consultation_start_datetime,"
      + " dc.end_datetime as consultation_end_datetime,"
      + " dc.consultation_id, dc.remarks, dc.head as consultation_type_id,"
      + " dc.status as consultation_status, dc.prescription_notes, "
      + " dc.discharge_prescription_notes, "
      + " COALESCE(refA.doctor_name, refB.referal_name) as referal_doctor,"
      + " doc.doctor_name as consulting_doctor, doc.practitioner_id,"
      + " d.doctor_name as admitting_doctor, '' as prescription_note_taker,"
      + " '' as logged_in_doctor, '' as logged_in_doctor_id, pr.established_type,"
      + " dc.immunization_status_upto_date, dc.immunization_remarks, dc.emergency_category, "
      + " CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 "
      + " THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer "
      + " WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730"
      + " THEN (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer"
      + " ELSE (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer"
      + " END AS age, "
      + " CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D'"
      + " WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M'"
      + " ELSE 'Y'" + " END AS agein, ''::text as age_text, dc.cons_revision_number, org.org_name "
      + " FROM doctor_consultation dc"
      + " LEFT JOIN patient_registration pr ON(dc.patient_id=pr.patient_id)"
      + " LEFT JOIN doctors refA ON (refA.doctor_id=pr.reference_docto_id)"
      + " LEFT JOIN referral refB ON (refB.referal_no=pr.reference_docto_id)"
      + " JOIN doctors doc ON(doc.doctor_id = dc.doctor_name)"
      + " LEFT JOIN doctors d ON(d.doctor_id = dc.doctor_name)"
      + " JOIN patient_details pd ON pr.mr_no = pd.mr_no"
      + " LEFT JOIN patient_insurance_plans pip ON(pip.patient_id = pr.patient_id)"
      + " LEFT JOIN insurance_plan_main ipm ON(ipm.plan_id = pip.plan_id)"
      + " JOIN organization_details org ON (org.org_id = pr.org_id)"
      + " WHERE dc.consultation_id =?";

  /**
   * Consultation summary info.
   *
   * @param consultId the consult id
   * @return the basic dyna bean
   */
  public BasicDynaBean consultationSummaryInfo(int consultId) {
    return DatabaseHelper.queryToDynaBean(GET_CONSULTATION_SUMMARY_DATA, new Object[] {consultId});

  }

  /** The Constant GET_TRIAGE_SUMMARY. */
  private static final String GET_TRIAGE_SUMMARY = "SELECT "
      + "dc.triage_start_datetime, dc.triage_end_datetime, dc.triage_done as triage_status,"
      + " dc.emergency_category, dc.remarks, COALESCE(d.doctor_id, ref.referal_no, '')"
      + "  AS reference_docto_id, COALESCE(d.doctor_name, ref.referal_name, '') AS referal_doctor,"
      + " dc.doctor_name as consulting_doctor_id, doc.doctor_name as consulting_doctor,"
      + " '' as logged_in_doctor, '' as logged_in_doctor_id, doc.dept_id AS dept_name,"
      + " dc.triage_revision_number "
      + "FROM doctor_consultation dc "
      + "JOIN patient_registration pr ON(dc.patient_id=pr.patient_id) "
      + "JOIN doctors doc ON (doc.doctor_id=dc.doctor_name) "
      + "LEFT JOIN doctors d ON (d.doctor_id=pr.reference_docto_id) "
      + "LEFT JOIN referral ref ON (ref.referal_no=pr.reference_docto_id) "
      + "WHERE dc.consultation_id=?";

  /**
   * Gets the triage summary.
   *
   * @param consId the cons id
   * @return the triage summary
   */
  public BasicDynaBean getTriageSummary(Integer consId) {
    return DatabaseHelper.queryToDynaBean(GET_TRIAGE_SUMMARY, new Object[] {consId});
  }

  /** The Constant GET_INITIAL_ASSESSMENT_SUMMARY. */
  private static final String GET_INITIAL_ASSESSMENT_SUMMARY =
      "SELECT " + "pr.op_type as visit_type, dc.head as consultation_type_id, "
          + "dc.initial_assessment_status, dc.emergency_category, "
          + "dc.remarks, COALESCE(d.doctor_id, ref.referal_no, '') AS reference_docto_id, "
          + "COALESCE(d.doctor_name, ref.referal_name, '') AS referal_doctor,"
          + " dc.doctor_name as consulting_doctor_id, doc.doctor_name as consulting_doctor,"
          + " '' as logged_in_doctor, '' as logged_in_doctor_id, doc.dept_id AS dept_name,"
          + " dc.ia_end_datetime, dc.ia_mod_time::text, dc.ia_start_datetime, "
          + " CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 "
          + " THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer "
          + " WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730"
          + " THEN (floor((current_date - COALESCE(pd.dateofbirth,"
          + "   pd.expected_dob))/30.43))::integer"
          + " ELSE (floor((current_date - COALESCE(pd.dateofbirth, "
          + "   pd.expected_dob))/365.25))::integer END AS age, "
          + " CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D'"
          + " WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M'"
          + " ELSE 'Y'" + " END AS agein, ''::text as age_text " + "FROM doctor_consultation dc "
          + "JOIN patient_registration pr ON(dc.patient_id=pr.patient_id) "
          + "JOIN doctors doc ON (doc.doctor_id=dc.doctor_name) "
          + "LEFT JOIN doctors d ON (d.doctor_id=pr.reference_docto_id) "
          + "JOIN patient_details pd ON pr.mr_no = pd.mr_no "
          + "LEFT JOIN referral ref ON (ref.referal_no=pr.reference_docto_id) "
          + "WHERE dc.consultation_id=?";

  /**
   * Gets the initial assessment summary.
   *
   * @param consId the cons id
   * @return the initial assessment summary
   */
  public BasicDynaBean getInitialAssessmentSummary(Integer consId) {
    return DatabaseHelper.queryToDynaBean(GET_INITIAL_ASSESSMENT_SUMMARY, new Object[] {consId});
  }

  /** The Constant GET_PATIENT_CONSULTATION_LIST. */
  private static final String GET_PATIENT_CONSULTATION_LIST = "SELECT dc.consultation_id, "
      + " dc.doctor_name as doctor_id, doc.doctor_name, pr.reg_date, pr.reg_time, "
      + " pr.patient_id, pr.status as visit_status, pr.op_type as visit_type, "
      + " dc.presc_date as consultation_datetime, dc.status as consultation_status, "
      + " b.bill_type,b.payment_status, d.dept_name, "
      + " opn.op_type_name as visit_type_name FROM doctor_consultation dc"
      + " JOIN doctors doc ON (doc.doctor_id = dc.doctor_name)"
      + " JOIN department d ON (doc.dept_id = d.dept_id)"
      + " JOIN (SELECT reg_date, reg_time, patient_id, status, op_type "
      + "   FROM patient_registration WHERE mr_no IN"
      + "   (SELECT mr_no FROM patient_details WHERE COALESCE(CASE WHEN original_mr_no = ''"
      + "   THEN NULL ELSE original_mr_no END, mr_no) = ?) AND visit_type='o' #) pr"
      + "   ON (dc.patient_id = pr.patient_id) "
      + " LEFT JOIN (select activity_id, charge_id from bill_activity_charge "
      + " WHERE activity_code='DOC') bac ON (bac.activity_id = CAST(dc.consultation_id as text))"
      + " LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id)"
      + " LEFT JOIN bill b ON (b.bill_no = bc.bill_no)"
      + " JOIN op_type_names opn ON ( pr.op_type = opn.op_type )"
      + " WHERE (cancel_status is null OR cancel_status != 'C') " + " AND dc.status !='U' ";


  /**
   * Gets the patient consultation details list.
   *
   * @param mrNo the mr no
   * @param isDoctorLogin the is doctor login
   * @return the patient consultation details list
   */
  // returns the consultations of logged in center, if the schema is a multi center.
  public List<BasicDynaBean> getPatientConsultationDetailsList(String mrNo, boolean isDoctorLogin) {
    String query = GET_PATIENT_CONSULTATION_LIST;
    List<Object> queryfilterList = new ArrayList<>();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    String loggedInDoctorId =
        (String) userService.findByKey("emp_username", userName).get("doctor_id");
    queryfilterList.add(mrNo);
    if (centerId != 0) {
      query = query.replace("#", " AND center_id = ? ");
      queryfilterList.add(centerId);
    } else {
      query = query.replace("#", "");
    }
    if (isDoctorLogin) {
      query += (" AND dc.doctor_name= ? ");
      queryfilterList.add(loggedInDoctorId);
    }
    query += (" ORDER BY dc.visited_date DESC, doc.doctor_name ");
    return DatabaseHelper.queryToDynaList(query, queryfilterList.toArray());
  }

  /** The Constant GET_PATIENT_TRIAGE_LIST. */
  private static final String GET_PATIENT_TRIAGE_LIST = "SELECT dc.consultation_id, "
      + " dc.doctor_name as doctor_id, doc.doctor_name, pr.reg_date, pr.reg_time, "
      + " pr.patient_id, pr.status as visit_status, pr.op_type as visit_type, "
      + " dc.presc_date as consultation_datetime, dc.triage_done as triage_status, "
      + " b.bill_type, b.payment_status, d.dept_name, opn.op_type_name as visit_type_name "
      + " FROM doctor_consultation dc JOIN doctors doc ON (doc.doctor_id = dc.doctor_name)"
      + " JOIN department d ON (doc.dept_id = d.dept_id)"
      + " JOIN (SELECT * FROM patient_registration WHERE mr_no IN (SELECT mr_no"
      + " FROM patient_details WHERE COALESCE(CASE WHEN original_mr_no = '' "
      + " THEN NULL ELSE original_mr_no END, mr_no) = ?) "
      + " AND visit_type='o' #) pr ON(dc.patient_id = pr.patient_id) "
      + " LEFT JOIN (select * from bill_activity_charge where activity_code='DOC') "
      + " bac ON (bac.activity_id = CAST(dc.consultation_id as text))"
      + " LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id)"
      + " LEFT JOIN bill b ON (b.bill_no = bc.bill_no)"
      + " JOIN op_type_names opn ON ( pr.op_type = opn.op_type )"
      + " WHERE (cancel_status is null OR cancel_status != 'C') AND dc.status !='U' ";

  /**
   * Gets the patient triage details list.
   * 
   *
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param centerId the center id
   * @return returns the triages of logged in center, if the schema is a multi center.
   */
  public List<BasicDynaBean> getPatientTriageDetailsList(String mrNo, String doctorId,
      Integer centerId) {
    String query = GET_PATIENT_TRIAGE_LIST;
    List<Object> queryParamneters = new ArrayList<>();
    queryParamneters.add(mrNo);
    if (centerId != 0) {
      query = query.replace("#", " AND center_id = ? ");
      queryParamneters.add(centerId);
    } else {
      query = query.replace("#", "");
    }
    if (doctorId != null && !doctorId.equals("")) {
      query += (" AND dc.doctor_name= ? ");
      queryParamneters.add(doctorId);
    }
    query += (" ORDER BY dc.visited_date DESC, doc.doctor_name ");
    return DatabaseHelper.queryToDynaList(query, queryParamneters.toArray());
  }

  /** The Constant GET_PATIENT_INITIAL_ASSESSMENT_LIST. */
  private static final String GET_PATIENT_INITIAL_ASSESSMENT_LIST = "SELECT dc.consultation_id, "
      + " dc.doctor_name as doctor_id, doc.doctor_name, pr.reg_date, pr.reg_time, "
      + " pr.patient_id, pr.status as visit_status, pr.op_type as visit_type, "
      + " dc.presc_date as consultation_datetime, dc.initial_assessment_status as ia_status, "
      + " dc.initial_assessment_status, b.bill_type, b.payment_status, d.dept_name, "
      + " opn.op_type_name as visit_type_name " + " FROM doctor_consultation dc"
      + " JOIN doctors doc ON (doc.doctor_id = dc.doctor_name)"
      + " JOIN department d ON (doc.dept_id = d.dept_id)"
      + " JOIN patient_registration pr ON(dc.patient_id = pr.patient_id)"
      + " LEFT JOIN bill_activity_charge bac ON (bac.activity_id = dc.consultation_id::text "
      + "   and bac.activity_code='DOC')"
      + " LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id)"
      + " LEFT JOIN bill b ON (b.bill_no = bc.bill_no)"
      + " JOIN op_type_names opn ON ( pr.op_type = opn.op_type )"
      + " WHERE pr.mr_no IN (?,(select mr_no from patient_details where original_mr_no = ?))"
      + " AND (cancel_status is null OR cancel_status != 'C') AND pr.visit_type = 'o' "
      + " AND dc.status !='U' ";

  /**
   * Gets the patient initial assessment details list.
   *
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @param centerId the center id
   * @return returns the initial assessment of logged in center, if the schema is a multi center.
   */
  public List<BasicDynaBean> getPatientInitialAssessmentDetailsList(String mrNo, String doctorId,
      Integer centerId) {
    StringBuilder query = new StringBuilder(GET_PATIENT_INITIAL_ASSESSMENT_LIST);
    List<Object> queryParamneters = new ArrayList<>();
    queryParamneters.add(mrNo);
    queryParamneters.add(mrNo);
    if (doctorId != null && !doctorId.equals("")) {
      query.append(" AND dc.doctor_name= ? ");
      queryParamneters.add(doctorId);
    }
    if (centerId != 0) {
      query.append(" AND pr.center_id = ? ");
      queryParamneters.add(centerId);
    }
    query.append(" ORDER BY dc.visited_date DESC, doc.doctor_name ");
    return DatabaseHelper.queryToDynaList(query.toString(), queryParamneters.toArray());
  }

  /** The get dept from consultation. */
  private static final String GET_DEPT_FROM_CONSULTATION =
      "SELECT doc.dept_id, doc.doctor_id From doctor_consultation dc"
      + " JOIN doctors doc ON (doc.doctor_id=dc.doctor_name) WHERE dc.consultation_id= ? ";

  /**
   * Gets the consultation.
   *
   * @param consultationId the consultation id
   * @return the consultation
   */
  public BasicDynaBean getConsultation(Integer consultationId) {
    return DatabaseHelper.queryToDynaBean(GET_DEPT_FROM_CONSULTATION,
        new Object[] {consultationId});
  }

  private static final String GET_PREVIOUS_CONSULTATIONS = "SELECT "
      + " dc.patient_id, dc.visited_date, d.doctor_name, dc.consultation_id, "
      + " dep.dept_name, (SELECT count(*) FROM patient_prescription "
      + " WHERE consultation_id = dc.consultation_id) AS number_of_prescriptions "
      + "FROM doctor_consultation dc  JOIN doctors d ON (d.doctor_id=dc.doctor_name)"
      + " JOIN department dep ON (d.dept_id = dep.dept_id) WHERE"
      + " #fliter# dc.mr_no=? ";

  /**
   * Gets the previous consultations.
   *
   * @param currentConsBean Consultation Bean
   * @param opDataAccessPrefs OP Data Access Preference
   * @return the previous consultations
   */
  public List<BasicDynaBean> getPreviousConsultations(BasicDynaBean currentConsBean,
      String opDataAccessPrefs, Boolean isCurrentConsRequired) {
    List<Object> queryParamneters = new ArrayList<>();
    String query = GET_PREVIOUS_CONSULTATIONS;
    if (!isCurrentConsRequired) {
      query = query.replace("#fliter#", "dc.consultation_id < ? AND");
      queryParamneters.add(currentConsBean.get("consultation_id"));
      queryParamneters.add(currentConsBean.get("mr_no"));
      if (opDataAccessPrefs.equals("D")) {
        query += (" AND d.dept_id= ? ");
        queryParamneters.add(currentConsBean.get("dept_id"));
      } else if (opDataAccessPrefs.equals("S")) {
        query += (" AND  dc.doctor_name=? ");
        queryParamneters.add(currentConsBean.get("doctor_id"));
      }
    } else {
      query = query.replace("#fliter#", "");
      queryParamneters.add(currentConsBean.get("mr_no"));
    }
    query += " ORDER BY dc.consultation_id DESC ";
    return DatabaseHelper.queryToDynaList(query, queryParamneters.toArray());
  }

  /** The get previous consultations by doctor. */
  private static final String GET_PREVIOUS_CONSULTATIONS_BY_DOCTOR =
      "Select dc.patient_id, dc.visited_date, d.doctor_name, dc.consultation_id"
      + " FROM doctor_consultation dc JOIN doctors d ON (d.doctor_id=dc.doctor_name)"
      + " WHERE dc.consultation_id < ? AND dc.mr_no=? AND dc.doctor_name=?"
      + " ORDER BY dc.consultation_id DESC";

  /**
   * Gets the previous consultations by doctor.
   *
   * @param consId the cons id
   * @param mrNo the mr no
   * @param doctorId the doctor id
   * @return the previous consultations by doctor
   */
  public List<BasicDynaBean> getPreviousConsultationsByDoctor(Integer consId, String mrNo,
      String doctorId) {
    return DatabaseHelper.queryToDynaList(GET_PREVIOUS_CONSULTATIONS_BY_DOCTOR,
        new Object[] {consId, mrNo, doctorId});
  }

  /** The Constant CONSULT_TEMPLATE. */
  private static final String CONSULT_TEMPLATE =
      " SELECT field_id, field_name, num_lines, default_value ,"
          + " default_value as field_value, display_order, null as value_id "
          + " FROM doc_hvf_templates dht"
          + " JOIN doc_hvf_template_fields dhtf USING (template_id) "
          + " WHERE dht.template_id=? and dhtf.field_status='A'";

  /**
   * Gets the consultation notes template.
   *
   * @param templateId the template id
   * @return the consultation notes template
   */
  public List<BasicDynaBean> getConsultationNotesTemplate(int templateId) {
    return DatabaseHelper.queryToDynaList(CONSULT_TEMPLATE, new Object[] {templateId});

  }

  /** The Constant GET_DOCTOR_DETAIL. */
  private static final String GET_DOCTOR_DETAIL =
      " SELECT distinct 'Doctor' as type,d.doctor_id AS id, d.doctor_name AS name, "
          + " null as code, d.available_for_online_consults, "
          + " dept.dept_name AS department, dept.dept_id AS department_id,"
          + " s.service_sub_group_id as subGrpId,"
          + " s.service_group_id as groupid,'N' as prior_auth_required,0 AS insurance_category_id,"
          + " false as conduction_applicable,false as conducting_doc_mandatory,"
          + " false as results_entry_applicable,'N' as tooth_num_required,"
          + " false as multi_visit_package, dcm.center_id, '-1' as tpa_id,"
          + " 'N' as mandate_additional_info,'' as additional_info_reqts"
          + " FROM doctors d JOIN department dept USING (dept_id)"
          + " JOIN service_sub_groups s using(service_sub_group_id) "
          + " JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id"
          + "   AND (dcm.center_id = 0 OR dcm.center_id=?)) # ";

  /** The Constant DOCTOR_SCHEDULABLE. */
  private static final String DOCTOR_SCHEDULABLE =
      " where ( scheduleable_by = ? OR scheduleable_by IS NULL) ";

  /**
   * Gets the doctor details.
   *
   * @param entityIdList the entity id list
   * @param centerId the center id
   * @param schedulable the schedulable
   * @param schedule the schedule
   * @return the doctor details
   */
  public List<BasicDynaBean> getDoctorDetails(List<Object> entityIdList, Integer centerId,
      String schedulable, boolean schedule) {
    List<Object> otherParams = new ArrayList<>();
    otherParams.add(centerId);
    String query = GET_DOCTOR_DETAIL;
    if (schedulable != null) {
      query = query.replace("#", DOCTOR_SCHEDULABLE);
      otherParams.add(schedulable);
    } else {
      query = query.replace("#", "");
    }
    StringBuilder sb = new StringBuilder(query);
    if (schedule && schedulable != null) {
      sb.append(" AND schedule = true ");
    } else if (schedule) {
      sb.append(" WHERE schedule = true ");
    }
    return super.getItemDetails(sb.toString(), entityIdList, "d.doctor_id", otherParams, false);
  }

  /** The Constant DOCTOR_FOR_PACKAGE_QUERY. */
  private static final String DOCTOR_FOR_PACKAGE_QUERY =
      "SELECT 'Doctor' AS type,'Doctor' AS id, 'Doctor' AS name, "
          + " null AS code, 'Package' AS department, "
          + "-1  AS subGrpId, -1 AS groupid, 'N' as prior_auth_required, "
          + " 0 AS insurance_category_id, false as conduction_applicable, "
          + " false as conducting_doc_mandatory, false as results_entry_applicable,"
          + " 'N' as tooth_num_required, false as multi_visit_package, -1 as center_id,"
          + " '-1' as tpa_id, 'N' as mandate_additional_info,'' as additional_info_reqts ";

  /**
   * Gets the doctor package.
   *
   * @return the doctor package
   */
  public BasicDynaBean getDoctorPackage() {
    return DatabaseHelper.queryToDynaBean(DOCTOR_FOR_PACKAGE_QUERY);
  }

  /** The Constant GET_CONSULTATION_LIST. */
  public static final String GET_CONSULTATION_LIST =
      "SELECT dc.consultation_id,dc.mr_no, dc.patient_id,u.role_id doctor_role_id , "
          + "u.emp_username doctor_user_id, u.doctor_id FROM doctor_consultation dc "
          + "INNER JOIN u_user u ON dc.doctor_name = u.doctor_id WHERE dc.patient_id = ? ";

  /**
   * Gets the consultation details.
   *
   * @param patientId the patient id
   * @return the consultation details
   */
  public List<BasicDynaBean> getConsultationDetails(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_CONSULTATION_LIST, new Object[] {patientId});
  }

  /** The get consultation status. */
  public static String GET_CONSULTATION_STATUS =
      "SELECT status FROM doctor_consultation WHERE consultation_id = ? ";
  
  /** The reopen consultation status. */
  public static String REOPEN_CONSULTATION_STATUS = "UPDATE doctor_consultation SET status='P', "
      + " consultation_complete_time=null WHERE consultation_id=?";
  
  /** The get ticket status. */
  public static String GET_TICKET_STATUS = "SELECT status " + " FROM reviews WHERE id = ? ";
  
  /** The get visit details. */
  public static String GET_VISIT_DETAILS =
      "SELECT codification_status " + " FROM patient_registration WHERE patient_id = ? ";

  /**
   * Open reopen consultation.
   *
   * @param consultaionId the consultaion id
   * @param ticketId the ticket id
   * @param patientId the patient id
   * @return the string
   */
  public String openReopenConsultation(Integer consultaionId, Integer ticketId, String patientId) {
    BasicDynaBean consultationQuery =
        DatabaseHelper.queryToDynaBean(GET_CONSULTATION_STATUS, new Object[] {consultaionId});
    BasicDynaBean ticketQuery =
        DatabaseHelper.queryToDynaBean(GET_TICKET_STATUS, new Object[] {ticketId});
    BasicDynaBean patientRegistrationQuery =
        DatabaseHelper.queryToDynaBean(GET_VISIT_DETAILS, new Object[] {patientId});
    // consultation status can be changed, only when consultation status is closed
    // consultation status can be changed, only if review is open or inprogress
    if (consultationQuery.get("status").equals("C") && ((ticketQuery.get("status").equals("open")
        || ticketQuery.get("status").equals("inprogress"))
        && (patientRegistrationQuery.get("codification_status").equals("P")
            || patientRegistrationQuery.get("codification_status").equals("C")))) {
      DatabaseHelper.update(REOPEN_CONSULTATION_STATUS, new Object[] {consultaionId});
      return "reopen";
    }
    return "open";
  }

  /** The Constant ORDERED_ITEMS. */
  private static final String ORDERED_ITEMS =
      "SELECT dc.patient_id, dc.consultation_id as order_id, dc.common_order_id,"
          + " 'Doctor' as type, dc.head as sub_type, 'DOC' as activity_code, "
          + " CASE WHEN ot_doc_role IS NULL OR ot_doc_role = '' THEN ct.consultation_type"
          + "   ELSE chc.chargehead_name END AS sub_type_name,"
          + " dc.doctor_name as item_id, dcd.doctor_name as item_name, null as item_code,"
          + " pd.doctor_id as pres_doctor_id, pd.doctor_name as pres_doctor_name,"
          + " dc.presc_date as pres_timestamp,"
          + " COALESCE(bc.posted_date, dc.presc_date) as posted_date, "
          + " dc.remarks as remarks,TO_CHAR(dc.presc_date,'dd-mm-yyyy') as pres_date, bc.discount,"
          + " 1 as qty, visited_date as from_timestamp, null as to_timestamp,"
          + " to_char(visited_date , 'DD-MM-YYYY HH24:MI') AS details, bc.consultation_type_id, "
          + " operation_ref, package_ref, b.is_primary_bill, b.bill_no, b.is_tpa, b.bill_type,"
          + " b.status as bill_status,bc.amount, bc.tax_amt, bc.insurance_claim_amount,"
          + " bc.sponsor_tax_amt, (CASE WHEN cancel_status='C' THEN 'X' WHEN dc.status='A'"
          + "   THEN 'N' ELSE dc.status END) as status, 'N' as sample_collected,"
          + " 'U' as finalization_status,bc.prior_auth_id, bc.prior_auth_mode_id,"
          + " bc.first_of_category, '' as cond_doctor_name,null as cond_doctor_id,null as labno,"
          + " true as canclebill, '' AS isdialysis, '' AS dialysis_status,"
          + " '' AS completion_status, '' as urgent,null as tooth_number,bc.insurance_category_id,"
          + " bc.charge_id, null AS outsource_dest_prescribed_id, 'N' as mandate_additional_info,"
          + " '' as additional_info_reqts, COALESCE(pm.multi_visit_package, false) "
          + " AS multi_visit_package, pm.package_id, pp.pat_package_id, "
          + " bc.charge_head,bc.charge_group, bc.item_excluded_from_doctor, "
          + " bc.item_excluded_from_doctor_remarks, bc.preauth_act_id, "
          + " ppa.preauth_required AS send_for_prior_auth, ppa.preauth_act_status, "
          + " ppc.content_id_ref, ppc.patient_package_content_id "
          + " FROM doctor_consultation dc JOIN doctors dcd on dcd.doctor_id = dc.doctor_name"
          + " LEFT JOIN doctors pd on pd.doctor_id = dc.presc_doctor_id"
          + " LEFT JOIN consultation_types ct ON (ct.consultation_type_id::text = dc.head)"
          + " LEFT JOIN chargehead_constants chc ON (chc.chargehead_id = dc.ot_doc_role)"
          + " LEFT JOIN bill_activity_charge bac ON bac.activity_id=dc.consultation_id::text"
          + "   AND bac.activity_code='DOC'"
          + " LEFT JOIN package_prescribed pp ON(pp.prescription_id = dc.package_ref) "
          + " LEFT JOIN patient_package_content_consumed ppcc "
          + " ON (ppcc.prescription_id = dc.consultation_id AND "
          + "     ppcc.item_type IN ('Doctor')) "
          + " LEFT JOIN patient_package_contents ppc "
          + " ON (ppcc.patient_package_content_id = ppc.patient_package_content_id) "
          + " LEFT JOIN packages pm ON CASE WHEN package_ref IS NOT NULL "
          + "   THEN (pm.package_id=pp.package_id) ELSE (pm.package_id::text=dc.doctor_name) END "
          + " LEFT JOIN bill_charge bc USING (charge_id) LEFT JOIN bill b USING (bill_no)"
          + " LEFT JOIN preauth_prescription_activities ppa "
          + "  ON bc.preauth_act_id = ppa.preauth_act_id"    
          + " WHERE dc.patient_id = ? "; 

  /**
   * Gets the ordered items.
   *
   * @param visitId the visit id
   * @param operationRef the operation ref
   * @param packageRef the package ref
   * @return the ordered items
   */
  public List<BasicDynaBean> getOrderedItems(String visitId, Integer operationRef,
      Boolean packageRef) {
    Object[] values;

    String packageRefCondition = IGNORE_MVP_ITEM_CONDITION;
    if (packageRef != null && packageRef) {
      packageRefCondition = GET_MVP_ITEM_CONDITION;
    }

    String operationRefCondition;
    if (operationRef == null) {
      operationRefCondition = IGNORE_OPERATION_ITEM_CONDITION;
      values = new Object[] {visitId};
    } else {
      operationRefCondition = GET_OPERATION_ITEM_CONDITION;
      values = new Object[] {visitId, operationRef};
    }
    return DatabaseHelper
        .queryToDynaList(ORDERED_ITEMS + operationRefCondition + packageRefCondition, values);
  }

  /** The Constant GET_OT_DOCTOR_CHARGES. */
  private static final String GET_OT_DOCTOR_CHARGES =
      "SELECT ot_charge as charge, ot_charge_discount as discount, "
          + "  co_surgeon_charge as cosurgeoncharge, co_surgeon_charge_discount, "
          + "  assnt_surgeon_charge as asst_charge, assnt_surgeon_charge_discount, "
          + "  d.doctor_id, d.doctor_name, d.dept_id FROM doctor_consultation_charge dcc "
          + "  JOIN doctors d ON (d.doctor_id = dcc.doctor_name) "
          + "  WHERE dcc.doctor_name=? and bed_type=? and organization=?";

  /**
   * Gets the OT doctor charges bean.
   *
   * @param doctorId the doctor id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the OT doctor charges bean
   */
  public BasicDynaBean getOTDoctorChargesBean(String doctorId, String bedType, String orgId) {
    BasicDynaBean docchargebean = DatabaseHelper.queryToDynaBean(GET_OT_DOCTOR_CHARGES,
        new Object[] {doctorId, bedType, orgId});
    if (docchargebean == null) {
      docchargebean = DatabaseHelper.queryToDynaBean(GET_OT_DOCTOR_CHARGES,
          new Object[] {doctorId, "GENERAL", "ORG0001"});
    }
    return docchargebean;
  }

  /** The Constant FOR_PATIENT_CONSULTATION. */
  private static final String FOR_PATIENT_CONSULTATION =
      "UPDATE patient_prescription SET status = 'P', username=? WHERE patient_presc_id = ?";

  /**
   * Update cancel status to patient.
   *
   * @param items the items
   * @param userName the user name
   * @return the int[]
   */
  public int[] updateCancelStatusToPatient(List<BasicDynaBean> items, String userName) {
    List<Object[]> paramsList = new ArrayList<>();
    for (BasicDynaBean item : items) {
      BasicDynaBean bean = findByKey("consultation_id", item.get("consultation_id"));
      Object docPrescId = bean == null ? null : bean.get("doc_presc_id");
      if (docPrescId != null && !"".equals(docPrescId.toString())) {
        paramsList.add(new Object[] {userName, docPrescId});
        pendingPrescriptionsRepository.updatePendingPrescriptionStatus((Integer)docPrescId, 1, "P");
      }
    }
    return DatabaseHelper.batchUpdate(FOR_PATIENT_CONSULTATION, paramsList);
  }

  /** The Constant GET_PACKAGE_REF. */
  private static final String GET_PACKAGE_REF = "SELECT consultation_id as order_id, "
      + " 'Doctor' as type FROM doctor_consultation WHERE patient_id = ? ";

  /* (non-Javadoc)
   * @see com.insta.hms.core.clinical.order.master.OrderItemRepository#getPackageRefQuery()
   */
  @Override
  public String getPackageRefQuery() {
    return GET_PACKAGE_REF;
  }

  /* (non-Javadoc)
   * @see com.insta.hms.core.clinical.order.master.OrderItemRepository#getOperationRefQuery()
   */
  @Override
  public String getOperationRefQuery() {
    return GET_PACKAGE_REF;
  }

  /**
   * Gets the doc id.
   *
   * @return the doc id
   */
  public int getDocId() {
    return DatabaseHelper.getInteger("select nextval('patient_consultation_template_seq')");
  }

  /** The Constant GET_DOCTOR_CONSULTATION_CHARGE. */
  private static final String GET_DOCTOR_CONSULTATION_CHARGE =
      " SELECT  dc.*, bc.charge_id, b.bill_no " + " FROM doctor_consultation dc  "
          + " JOIN patient_registration pr USING (patient_id) "
          + " JOIN bill b ON b.visit_id = pr.patient_id  "
          + " JOIN bill_charge bc ON bc.bill_no = b.bill_no "
          + " JOIN bill_activity_charge bac ON bac.charge_id= bc.charge_id "
          + " AND bac.activity_id = dc.consultation_id::varchar where bc.charge_id = ? ";

  /**
   * Gets the doctor consultation charge.
   *
   * @param chargeId String
   * @return the doctor consultation charge
   */
  public DynaBean getDoctorConsultationCharge(String chargeId) {
    return DatabaseHelper.queryToDynaBean(GET_DOCTOR_CONSULTATION_CHARGE, chargeId);
  }

  /** The Constant GET_IMMUNIZATION_DETAILS. */
  private static final String GET_IMMUNIZATION_DETAILS = "SELECT "
      + " dc.consultation_id, COALESCE(dc.immunization_status_upto_date, '') "
      + "   AS immunization_status_upto_date, dc.immunization_remarks "
      + "FROM doctor_consultation dc WHERE consultation_id=?";

  /**
   * Gets the immunization details.
   *
   * @param consId the cons id
   * @return the immunization details
   */
  public BasicDynaBean getImmunizationDetails(Integer consId) {
    return DatabaseHelper.queryToDynaBean(GET_IMMUNIZATION_DETAILS, new Object[] {consId});
  }

  /** The get consultation field values. */
  private static final String GET_CONSULTATION_FIELD_VALUES =
      " SELECT textcat_linecat(' ' || field_name || ' :- ' ||field_value)"
          + " FROM (SELECT dhtf.field_name,pcfv.field_value "
          + "   FROM patient_consultation_field_values pcfv "
          + "   JOIN doc_hvf_template_fields dhtf ON (dhtf.field_id=pcfv.field_id) "
          + "   JOIN doctor_consultation dc ON (dc.doc_id=pcfv.doc_id "
          + "       AND dhtf.template_id = dc.template_id) "
          + "   WHERE dc.consultation_id = ? AND trim(COALESCE(pcfv.field_value,'')) != '' "
          + "   ORDER BY dhtf.display_order) AS foo";

  /**
   * Gets the consultation field values.
   *
   * @param consId the cons id
   * @return the consultation field values
   */
  public String getConsultationFieldValues(Integer consId) {
    return DatabaseHelper.getString(GET_CONSULTATION_FIELD_VALUES, new Object[] {consId});
  }

  /** The Constant GET_PRINT_CONSULTATION_LIST. */
  private static final String GET_PRINT_CONSULTATION_LIST = "SELECT dc.consultation_id, "
      + " dc.doctor_name as doctor_id, doc.doctor_name, pr.reg_date, pr.reg_time, "
      + " pr.patient_id, pr.status as visit_status, pr.op_type as visit_type, "
      + " dc.presc_date as consultation_datetime, dc.status as consultation_status, "
      + " dc.triage_done as triage_status, dc.initial_assessment_status, "
      + " b.bill_type, b.payment_status, " + " d.dept_name, "
      + " opn.op_type_name as visit_type_name " + " FROM doctor_consultation dc"
      + " JOIN doctors doc ON (doc.doctor_id = dc.doctor_name)"
      + " JOIN department d ON (doc.dept_id = d.dept_id)"
      + " JOIN (SELECT * FROM patient_registration WHERE mr_no IN"
      + "   (SELECT mr_no FROM patient_details WHERE COALESCE(CASE WHEN original_mr_no = ''"
      + "   THEN NULL ELSE original_mr_no END, mr_no) = ?) AND visit_type='o' #) pr "
      + "   ON (dc.patient_id = pr.patient_id) "
      + " LEFT JOIN (select * from bill_activity_charge where activity_code='DOC') "
      + " bac ON (bac.activity_id = CAST(dc.consultation_id as text))"
      + " LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id)"
      + " LEFT JOIN bill b ON (b.bill_no = bc.bill_no)"
      + " JOIN op_type_names opn ON ( pr.op_type = opn.op_type )"
      + " WHERE (cancel_status is null OR cancel_status != 'C') AND dc.status !='U' ";

  /**
   * Gets the consultation list for print.
   *
   * @param mrNo the mr no
   * @param isDoctorLogin the is doctor login
   * @return the consultation list for print
   */
  // returns the consultations of logged in center, if the schema is a multi center.
  public List<BasicDynaBean> getConsultationListForPrint(String mrNo, boolean isDoctorLogin) {
    String query = GET_PRINT_CONSULTATION_LIST;
    List<Object> queryFilterParams = new ArrayList<>();
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    Integer centerId = (Integer) sessionService.getSessionAttributes().get("centerId");
    String loggedInDoctorId =
        (String) userService.findByKey("emp_username", userName).get("doctor_id");
    queryFilterParams.add(mrNo);
    if (centerId != 0) {
      query = query.replace("#", " AND center_id = ? ");
      queryFilterParams.add(centerId);
    } else {
      query = query.replace("#", "");
    }
    if (isDoctorLogin) {
      query += (" AND dc.doctor_name= ? ");
      queryFilterParams.add(loggedInDoctorId);
    }
    query += (" ORDER BY dc.visited_date DESC, doc.doctor_name ");
    return DatabaseHelper.queryToDynaList(query, queryFilterParams.toArray());
  }

  /** The Constant GET_MR_NO_FOR_CONSULTATION. */
  private static final String GET_MR_NO_FOR_CONSULTATION =
      "Select mr_no from doctor_consultation where consultation_id = ?";

  /**
   * Gets the mr no for consulatation.
   *
   * @param consId the cons id
   * @return the mr no for consulatation
   */
  public String getMrNoForConsulatation(Integer consId) {
    return DatabaseHelper.getString(GET_MR_NO_FOR_CONSULTATION, consId);
  }

  private static final String GET_VISIT_ID =
      "SELECT patient_id FROM doctor_consultation WHERE consultation_id=?";

  public String getVisitId(int consultationId) {
    return DatabaseHelper.getString(GET_VISIT_ID, consultationId);
  }

  /** query const. */
  private static final String GET_CONSULTATION_SAVE_EVENT_SEGMENT_DATA =
      "SELECT dc.consultation_complete_time, d.doctor_name, dc.consultation_id, dc.patient_id, "
          + "d.doc_first_name,d.doc_middle_name,d.doc_last_name,d.doctor_license_number,"
          + "dc.mr_no, dc.cons_reopened FROM doctor_consultation dc "
          + "JOIN doctors d ON (d.doctor_id=dc.doctor_name) where dc.status='C' and "
          + "dc.consultation_id=?";

  /**
   * get consultation event data when saved.
   * @param consultationId consultation id.
   * @return data bean
   */
  public BasicDynaBean getConsultationSaveEventSegmentData(int consultationId) {
    return DatabaseHelper
        .queryToDynaBean(GET_CONSULTATION_SAVE_EVENT_SEGMENT_DATA, new Object[] {consultationId});
  }
}
