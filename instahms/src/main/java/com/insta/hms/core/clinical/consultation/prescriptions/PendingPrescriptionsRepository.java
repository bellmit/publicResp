package com.insta.hms.core.clinical.consultation.prescriptions;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryAssembler;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.SearchQuery;
import com.insta.hms.mdm.doctors.DoctorRepository;
import com.insta.hms.security.usermanager.RoleService;
import com.insta.hms.security.usermanager.UserService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PendingPrescriptionsRepository.
 *
 * @author allabakash
 */
@Repository
public class PendingPrescriptionsRepository extends GenericRepository {

  /** Doctor repo. */
  @LazyAutowired
  private DoctorRepository doctorRepository;

  @LazyAutowired
  private UserService userService;

  @LazyAutowired
  private RoleService roleService;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /** The Constant USER_ID_SESSION. */
  private static final String USER_ID_SESSION = "userId";

  /** ppp service. */
  @LazyAutowired
  private PendingPrescriptionsService pendingPrescriptionService;

  /**
   * Instantiates pending prescription repository.
   */
  public PendingPrescriptionsRepository() {
    super("patient_pending_prescriptions");
  }

  private static final String GET_PENDING_PRESC = "SELECT pd.mr_no, "
      + " COALESCE (sm.salutation || ' ' || pd.patient_name "
      + "  || CASE WHEN coalesce(pd.middle_name, '') = '' "
      + "  THEN '' ELSE (' ' || pd.middle_name) END "
      + "  || CASE WHEN coalesce(pd.last_name, '') = '' THEN '' "
      + " ELSE (' ' || pd.last_name) END)::character varying AS patient_name, "
      + " pd.patient_phone AS phone_no, ppp.patient_presc_id AS patient_presc_id, "
      + " ppp.pat_pending_presc_id AS pending_prescription_id, ppp.presc_item_id AS presc_item_id,"
      + " ppp.presc_item_type AS prescription_type, ppp.presc_item_qty AS item_qty, "
      + " ppp.prescribed_by AS prescribed_doctor_id, "
      + " ppp.prescribed_date::DATE AS prescription_date, "
      + " ppp.start_datetime::DATE AS prescription_start_date, "
      + " pr.primary_sponsor_id AS pri_sponsor_id, pr.secondary_sponsor_id AS sec_sponsor_id, "
      + " preauth.preauth_status AS prior_auth_status, "
      + " ppa.preauth_act_status AS prior_auth_act_status, "
      + " ppp.preauth_activity_status AS prior_auth_item_status, "
      + " ppa.mod_time::DATE AS prior_auth_date, " + " ppa.preauth_required AS preauth_required, "
      + " CASE WHEN (pp.status IS NULL OR pp.status = 'PA') "
      + " THEN ppp.status ELSE pp.status END AS status, "
      + " pr.patient_id AS visit_id, pr.primary_insurance_co AS insurance_co_id, "
      + " ppa.preauth_presc_id AS preauth_id, dc.consultation_id AS consultation_id, "
      + " ppp.declined_by, ppp.declined_at::DATE, ppp.declined_reason_id, ppp.assigned_to_user_id,"
      + " ppp.assigned_to_role_id, u.temp_username AS assigned_to_user, "
      + " ur.hosp_role_name assigned_role_name, "
      + " ppp.modified_at::DATE AS assigned_date, ppp.followup_id, "
      + " ppp.presc_item_dept_id AS presc_item_dept_id, ppp.prescribed_date, "
      + " CASE WHEN pr.reference_docto_id IS NOT NULL AND pr.reference_docto_id != '0' THEN "
      + " COALESCE(refdoc.doctor_name,ref.referal_name)||'(REF)' "
      + " ELSE pr.reference_docto_id END  AS referral_doctor_name "
      + " FROM patient_pending_prescriptions ppp "
      + " LEFT JOIN patient_prescription pp ON pp.patient_presc_id = ppp.patient_presc_id "
      + " LEFT JOIN preauth_prescription_activities ppa "
      + " ON ppa.preauth_act_id = ppp.preauth_activity_id "
      + " LEFT JOIN preauth_prescription preauth "
      + " ON preauth.preauth_presc_id = ppa.preauth_presc_id "
      + " LEFT JOIN doctor_consultation dc ON dc.consultation_id = pp.consultation_id "
      + " INNER JOIN patient_registration pr ON pr.patient_id = ppp.visit_id "
      + " INNER JOIN patient_details pd ON pd.mr_no = pr.mr_no "
      + " INNER JOIN salutation_master sm ON sm.salutation_id = pd.salutation "
      + " LEFT JOIN u_user u ON u.emp_username = ppp.assigned_to_user_id "
      + " LEFT JOIN hospital_roles_master ur ON ur.hosp_role_id = ppp.assigned_to_role_id "
      + " LEFT JOIN referral ref ON ref.referal_no = pr.reference_docto_id"
      + " LEFT JOIN doctors refdoc ON refdoc.doctor_id = pr.reference_docto_id ";

  private static final String whereClause =
      " WHERE patient_confidentiality_check(pd.patient_group,pd.mr_no) AND "
          + " pr.visit_type='o' AND pr.center_id = ? ";

  /**
   * Get pending presc.
   * 
   * @param parameters the param
   * @return paged list
   * @throws ParseException the exception
   */
  @SuppressWarnings("unchecked")
  public PagedList getPendingPrescriptions(Map<String, String[]> parameters) throws ParseException {
    Map<String, Object> whereFilters = applyFilters(getRequestMap(parameters));
    ArrayList<Object> values = (ArrayList<Object>) whereFilters.get("params");
    SearchQuery query = new SearchQuery(
        "FROM (" + GET_PENDING_PRESC + whereFilters.get("whereClause") + ") AS foo");
    SearchQueryAssembler qb = new SearchQueryAssembler(query.getFieldList(), query.getCountQuery(),
        query.getSelectTables(), null, ConversionUtils.getListingParameter(parameters));
    qb.setfieldValues(values);
    qb.addSecondarySort("prescribed_date", true);
    qb.addSecondarySort("pending_prescription_id");
    qb.build();
    return qb.getMappedPagedList();
  }

  private static final String GET_CURRENT_SEQUENCE =
      " SELECT " + " CURRVAL('patient_pending_prescriptions_seq') ";

  public Integer getCurrentSequence() {
    return DatabaseHelper.getInteger(GET_CURRENT_SEQUENCE);
  }

  private Map<String, Object> getRequestMap(Map<String, String[]> requestMap) {
    Map<String, Object> filterMap = new HashMap<>();
    Map<String, Object> flattenRequestMap = ConversionUtils.flatten(requestMap);
    filterMap.put("centerId", Integer.parseInt(flattenRequestMap.get("center_id").toString()));
    if (flattenRequestMap.get("mr_no") != null) {
      filterMap.put("mrNo",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("mr_no").toString()));
    } else {
      filterMap.put("mrNo", "");
    }

    if (flattenRequestMap.get("tpa_id") != null) {
      filterMap.put("priTpaId",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("tpa_id").toString()));
    } else {
      filterMap.put("priTpaId", "");
    }

    if (flattenRequestMap.get("sec_tpa_id") != null) {
      filterMap.put("secTpaId",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("sec_tpa_id").toString()));
    } else {
      filterMap.put("secTpaId", "");
    }

    if (flattenRequestMap.get("prescribed_from_date") != null) {
      filterMap.put("prescribedFromDate",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("prescribed_from_date").toString()));
    } else {
      filterMap.put("prescribedFromDate", "");
    }

    if (flattenRequestMap.get("prescribed_to_date") != null) {
      filterMap.put("prescribedToDate",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("prescribed_to_date").toString()));
    } else {
      filterMap.put("prescribedToDate", "");
    }

    if (flattenRequestMap.get("prescription_type") != null) {
      filterMap.put("prescriptionType",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("prescription_type").toString()));
    } else {
      filterMap.put("prescriptionType", "");
    }

    if (flattenRequestMap.get("prescription_by") != null) {
      filterMap.put("prescribedBy",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("prescription_by").toString()));
    } else {
      filterMap.put("prescribedBy", "");
    }

    if (flattenRequestMap.get("prior_auth_status") != null) {
      filterMap.put("priorAuthStatus",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("prior_auth_status").toString()));
    } else {
      filterMap.put("priorAuthStatus", "");
    }

    if (flattenRequestMap.get("appointment_status") != null) {
      filterMap.put("appointmentStatus",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("appointment_status").toString()));
    } else {
      filterMap.put("appointmentStatus", "");
    }

    if (flattenRequestMap.get("assigned_to") != null) {
      filterMap.put("assignedTo",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("assigned_to").toString()));
    } else {
      filterMap.put("assignedTo", "");
    }

    if (flattenRequestMap.get("assigned_type") != null) {
      filterMap.put("assignedType",
          StringUtils.trimAllWhitespace(flattenRequestMap.get("assigned_type").toString()));
    } else {
      filterMap.put("assignedType", "");
    }

    String loggedInUserIdStr =
        sessionService.getSessionAttributes().get(USER_ID_SESSION).toString();
    Integer loggedInRoleId = (Integer) sessionService.getSessionAttributes().get("roleId");
    BasicDynaBean userBean = userService.findByKey("emp_username", loggedInUserIdStr);
    String doctorId = (String) userBean.get("doctor_id");
    filterMap.put("doctorId", doctorId);
    filterMap.put("loggedInUserIdStr", loggedInUserIdStr);
    filterMap.put("loggedInRoleId", loggedInRoleId);
    filterMap.put("page_num", Integer.parseInt(flattenRequestMap.get("page_num").toString()));
    filterMap.put("page_size", Integer.parseInt(flattenRequestMap.get("page_size").toString()));
    return filterMap;
  }

  private static final String CASE_WHEN_FOR_STATUS = "AND CASE WHEN "
      + "(pp.status IS NULL OR pp.status = 'PA') THEN ppp.status ELSE pp.status END ";

  private Map<String, Object> applyFilters(Map<String, Object> filtermap) throws ParseException {
    List<Object> paramList = new ArrayList<Object>();

    paramList.add(filtermap.get("centerId"));

    // prescribed date from and to filter
    Calendar calPrescFrom = Calendar.getInstance();
    Calendar calPrescTo = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    StringBuilder where = new StringBuilder(whereClause);
    if (!filtermap.get("prescribedFromDate").equals("")
        && !filtermap.get("prescribedToDate").equals("")) {
      calPrescFrom.setTime(sdf.parse(filtermap.get("prescribedFromDate").toString()));
      calPrescFrom.set(Calendar.HOUR_OF_DAY, 0);
      calPrescFrom.set(Calendar.MINUTE, 0);
      calPrescFrom.set(Calendar.SECOND, 0);
      where.append(" AND ppp.prescribed_date BETWEEN ? AND ? ");
      paramList.add(calPrescFrom.getTime());
      calPrescTo.setTime(sdf.parse(filtermap.get("prescribedToDate").toString()));
      calPrescTo.add(Calendar.DATE, 1);
      paramList.add(calPrescTo.getTime());
    }

    // mrno filter
    if (!filtermap.get("mrNo").equals("")) {
      where.append(" AND pd.mr_no = ? ");
      paramList.add(filtermap.get("mrNo").toString());
    }

    // primary tpa filter
    if (!filtermap.get("priTpaId").equals("")) {
      where.append(" AND pr.primary_sponsor_id = ? ");
      paramList.add(filtermap.get("priTpaId"));
    }

    // secondary tpa filter
    if (!filtermap.get("secTpaId").equals("")) {
      where.append(" AND pr.secondary_sponsor_id = ? ");
      paramList.add(filtermap.get("secTpaId"));
    }

    // prescription type filter
    if (!filtermap.get("prescriptionType").equals("")) {
      String[] prescTypesArray = filtermap.get("prescriptionType").toString().split(",");
      List<String> prescriptionTypes = Arrays.asList(prescTypesArray);
      if (!prescriptionTypes.isEmpty()) {
        where.append(" AND ppp.presc_item_type IN (");
        List<String> whereParams = new ArrayList<>();
        for (String prescriptionTp : prescriptionTypes) {
          whereParams.add("?");
          paramList.add(prescriptionTp);
          if ("Ref".equals(prescriptionTp)) {
            whereParams.add("?");
            paramList.add("Dep");
          }
        }
        where.append(StringUtils.arrayToCommaDelimitedString(whereParams.toArray()));
        where.append(")");
      }
    }

    // prescribed by doctor filter
    if (!filtermap.get("prescribedBy").equals("")) {
      where.append(" AND (ppp.prescribed_by = ? ");
      paramList.add(filtermap.get("prescribedBy"));
      where.append(" OR (ppp.visit_id IN (SELECT patient_id FROM patient_registration WHERE "
          + " reference_docto_id = ? AND op_type = 'O') AND ppp.patient_presc_id IS NULL)) ");
      paramList.add(filtermap.get("prescribedBy"));
    }

    // prior auth status filter
    if (!filtermap.get("priorAuthStatus").equals("")) {
      String[] priorAuthStatusArray = filtermap.get("priorAuthStatus").toString().split(",");
      List<String> priorAuthList = Arrays.asList(priorAuthStatusArray);
      if (!priorAuthList.isEmpty()) {
        if (priorAuthList.contains("NA")) {
          where.append(" AND (ppa.preauth_required IS NULL "
              + "OR ppa.preauth_required  = 'N') ");
        } else {
          where.append(" AND ppa.preauth_required = 'Y' ");
          if (priorAuthList.contains("O")) {
            where.append(" AND (preauth.preauth_status = 'O' ");
          }
          if (priorAuthList.contains("S")) {
            where.append(priorAuthList.contains("O") ? " OR " : " AND (");
            where.append(" preauth.preauth_status = 'S' "
                + "AND (ppp.preauth_activity_status = 'O' "
                + " OR ppp.preauth_activity_status = 'S') ");
          }
          
          String stringConcatinate = "";
          if ((priorAuthList.contains("S") && !priorAuthList.contains("O")) 
              || ((priorAuthList.contains("S") && priorAuthList.contains("O")))) {
            stringConcatinate = " OR ";
          }
          
          if (!priorAuthList.contains("S") && priorAuthList.contains("O")) {
            stringConcatinate = " AND ";
          }
          
          if (!priorAuthList.contains("S") && !priorAuthList.contains("O")) {
            stringConcatinate = " AND (";
          }
          
          where.append(stringConcatinate);
          where.append(" ppp.preauth_activity_status IN (");
          List<String> whereParams = new ArrayList<>();
          for (String priorAuthStatus : priorAuthList) {
            whereParams.add("?");
            paramList.add(priorAuthStatus);
          }
          where.append(StringUtils.arrayToCommaDelimitedString(whereParams.toArray()));
          where.append("))");
        }
      }
    }

    // Loggedin user is doctor
    if (filtermap.get("doctorId") != null && !filtermap.get("doctorId").equals("")) {
      where.append(
          " AND (ppp.prescribed_by = ? " + " OR dc.doctor_name = ? OR ppp.assigned_to_user_id = ? "
              + " OR ppp.presc_item_id = ? ) ");
      paramList.add(filtermap.get("doctorId"));
      paramList.add(filtermap.get("doctorId"));
      paramList.add(filtermap.get("loggedInUserIdStr"));
      paramList.add(filtermap.get("doctorId"));
    } else if ((filtermap.get("prescriptionType") == null
        || filtermap.get("prescriptionType").equals(""))) {
      List<String> userPrescriptionTypes = pendingPrescriptionService.getUserPrescriptionTypes();
      if (!userPrescriptionTypes.isEmpty()) {
        where.append(" AND ppp.presc_item_type IN (");
        List<String> whereParams = new ArrayList<>();
        for (String prescriptionType : userPrescriptionTypes) {
          whereParams.add("?");
          paramList.add(prescriptionType);
          if ("Ref".equals(prescriptionType)) {
            whereParams.add("?");
            paramList.add("Dep");
          }
        }
        where.append(StringUtils.arrayToCommaDelimitedString(whereParams.toArray()));
        where.append(")");
      }
    }

    // Appointment Status
    String appointmentStatus = (String) filtermap.get("appointmentStatus");
    if (!appointmentStatus.equals("")) {
      switch (appointmentStatus) {
        case "O":
          where.append(CASE_WHEN_FOR_STATUS + " = ? ");
          paramList.add(appointmentStatus);
          break;
        case "P":
          where.append(CASE_WHEN_FOR_STATUS + " = ? AND NOT EXISTS "
              + "(SELECT patient_presc_id FROM scheduler_appointments sa "
              + "WHERE sa.patient_presc_id = ppp.pat_pending_presc_id) ");
          paramList.add(appointmentStatus);
          break;
        case "Cancel":
          where.append(" AND EXISTS " + "(SELECT usa.patient_presc_id "
              + "FROM (SELECT patient_presc_id, appointment_status "
              + "FROM scheduler_appointments sa "
              + "WHERE sa.patient_presc_id = ppp.pat_pending_presc_id "
              + "ORDER BY appointment_id DESC LIMIT 1) "
              + "AS usa WHERE LOWER(usa.appointment_status) = ?)");
          paramList.add(appointmentStatus.toLowerCase());
          break;
        default:
          where.append(CASE_WHEN_FOR_STATUS + " = 'P' AND EXISTS "
              + "(SELECT patient_presc_id FROM scheduler_appointments sa "
              + "WHERE sa.patient_presc_id = ppp.pat_pending_presc_id  "
              + "AND LOWER(appointment_status) = ?)");
          paramList.add(appointmentStatus.toLowerCase());
      }
    }

    // Assigned to
    if (!filtermap.get("assignedTo").equals("")) {
      if (filtermap.get("assignedType").equals("role")) {
        where.append(" AND ppp.assigned_to_role_id = ? ");
        paramList.add(Integer.parseInt(filtermap.get("assignedTo").toString()));
      }
      if (filtermap.get("assignedType").equals("user")) {
        where.append(" AND ppp.assigned_to_user_id = ? ");
        paramList.add(filtermap.get("assignedTo"));
      }
    }

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("whereClause", where.toString());
    map.put("params", paramList);
    return map;
  }

  private static final String PENDING_SERVICE_PRESCRIPTION =
      "SELECT pat_pending_presc_id, preauth_activity_id " + "FROM patient_pending_prescriptions "
          + "WHERE %column% = :column_value AND status = 'P' "
          + "ORDER BY pat_pending_presc_id DESC LIMIT :limit";

  /**
   * Get pending presc ids.
   * 
   * @param map the map
   * @return list of basicdynabean
   */
  public List<BasicDynaBean> getPendingServicePrescriptionId(Map<String, Object> map) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("column_value", map.get("columnValue"));
    parameter.addValue("limit", map.get("limit"));
    String query =
        PENDING_SERVICE_PRESCRIPTION.replace("%column%", map.get("columnName").toString());
    return DatabaseHelper.queryToDynaList(query, parameter);
  }

  private static final String PRE_AUTH_END_DATE_QUERY = "SELECT prad.end_date::DATE "
      + "    FROM preauth_prescription pp INNER JOIN preauth_request_approval_details prad"
      + "    ON prad.preauth_request_id=pp.preauth_request_id "
      + "    WHERE pp.preauth_presc_id = ? ";

  public BasicDynaBean getPreauthEndDate(Object id) {
    return DatabaseHelper.queryToDynaBean(PRE_AUTH_END_DATE_QUERY, id);
  }

  /**
   * Get pending presc id.
   * 
   * @param patPrescId the presc id
   * @return the list of Object
   */
  public List<Object> getPendingPrescIds(int patPrescId) {
    Map<String, Object> filtermap = new HashMap<>();
    filtermap.put("patient_presc_id", patPrescId);
    List<Object> id = new ArrayList<>();
    List<BasicDynaBean> beanList = findByCriteria(filtermap);
    for (BasicDynaBean bean : beanList) {
      id.add(bean.get("pat_pending_presc_id"));
    }
    return id;
  }

  private static final String UPDATE_PRESC_ID_IN_APPOINTMENT_TABLE = "UPDATE "
      + " scheduler_appointments " + " SET patient_presc_id = NULL WHERE patient_presc_id = ? ";

  private static final String UPDATE_VISIT_ID = "UPDATE patient_pending_prescriptions ppp "
      + "    SET visit_id = ? " + "    FROM doctor_consultation d, patient_prescription pp, "
      + "    bill_activity_charge bac, bill_charge bc "
      + "    WHERE (bac.activity_id = d.consultation_id::text AND activity_code = 'DOC') "
      + "    AND (bac.charge_id = bc.charge_id)  AND bill_no = ? "
      + "    AND (d.consultation_id = pp.consultation_id) "
      + "    AND (pp.patient_presc_id = ppp.patient_presc_id)";

  public void updateVisitId(String billNo, String newVisit) {
    DatabaseHelper.update(UPDATE_VISIT_ID, newVisit, billNo);
  }

  private static final String GET_HOSPITAL_PRESC_TYPES = "SELECT DISTINCT prescription_type FROM "
      + "hospital_role_prescription_types hrpt "
      + "JOIN hospital_roles_master hrm ON hrm.hosp_role_id=hrpt.hosp_role_id and hrm.status='A' "
      + "WHERE hrpt.hosp_role_id IN (:roleIds)";

  /**
   * gets the scheduler details for pending prescription dashboard.
   * 
   * @param userHospitalRoles the list of prescIds
   * @return list of map
   */
  public List<Map<String, String>> getUserPrescrptionTypes(List<Integer> userHospitalRoles) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("roleIds", userHospitalRoles);
    List<BasicDynaBean> beanList =
        DatabaseHelper.queryToDynaList(GET_HOSPITAL_PRESC_TYPES, parameters);
    return ConversionUtils.copyListDynaBeansToMap(beanList);

  }

  private static final String GET_DISTINCT_PRESCRIBED_USERS =
      "SELECT " + " DISTINCT assigned_to_user_id AS distinct_row FROM "
          + " patient_pending_prescriptions WHERE assigned_to_user_id IS NOT NULL";

  private static final String GET_DISTINCT_PRESCRIBED_ROLES =
      "SELECT " + " DISTINCT assigned_to_role_id AS distinct_row FROM "
          + " patient_pending_prescriptions WHERE assigned_to_role_id IS NOT NULL";

  /**
   * Get unique users and roles.
   * 
   * @param selectType the type role or users
   * @return the list of map
   */
  public List<Map<String, Object>> getDistinctUsersOrRoles(String selectType) {
    List<BasicDynaBean> beanList = null;
    if ("users".equals(selectType)) {
      beanList = DatabaseHelper.queryToDynaList(GET_DISTINCT_PRESCRIBED_USERS);
    } else if ("roles".equals(selectType)) {
      beanList = DatabaseHelper.queryToDynaList(GET_DISTINCT_PRESCRIBED_ROLES);
    }
    return ConversionUtils.copyListDynaBeansToMap(beanList);
  }

  private static final String GET_APPOINTMENT_ID =
      "SELECT appointment_id, appointment_time " + " FROM scheduler_appointments "
          + " WHERE patient_presc_id = ? AND appointment_status != 'Cancel' "
          + " ORDER BY booked_time DESC LIMIT 1";

  public BasicDynaBean getAppointmentId(Object ppdId) {
    return DatabaseHelper.queryToDynaBean(GET_APPOINTMENT_ID, ppdId);
  }

  private static final String GET_PRESC_ID_OF_APPOINTMENT = "SELECT "
      + " ppp.pat_pending_presc_id, ppp.patient_presc_id, ppp.preauth_activity_id,"
      + " ppp.preauth_activity_status" + " FROM scheduler_appointments sa "
      + " JOIN patient_pending_prescriptions ppp "
      + " ON (ppp.pat_pending_presc_id = sa.patient_presc_id) " + " WHERE sa.appointment_id = ? ";

  public BasicDynaBean getPrescIdOfAppointment(String appointmentId) {
    return DatabaseHelper.queryToDynaBean(GET_PRESC_ID_OF_APPOINTMENT, Long.valueOf(appointmentId));

  }

  private static final String UPDATE_PENDING_PRESC_STATUS = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = ? WHERE pat_pending_presc_id = ?";

  /**
   * Update pending presc status.
   * 
   * @param patientPendingPrescId the pending presc id
   * @param status the status
   */
  public void updatePendingPrescriptionStatus(String patientPendingPrescId, String status) {
    if ("P".equals(status)) {
      DatabaseHelper.update(UPDATE_PRESC_ID_IN_APPOINTMENT_TABLE,
          Long.valueOf(patientPendingPrescId));
    }
    DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS, status, Long.valueOf(patientPendingPrescId));
  }

  private static final String SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC =
      " SELECT " + "pat_pending_presc_id FROM patient_pending_prescriptions  ";

  private static final String UPDATE_PENDING_PRESC_STATUS_TO_ORDERED_FIFO = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = ? WHERE pat_pending_presc_id IN ( "
      + SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
      + " WHERE patient_presc_id = ? AND status = 'P' "
      + " ORDER BY pat_pending_presc_id LIMIT ? )";

  private static final String UPDATE_PENDING_PRESC_STATUS_AND_PA_STATU_TO_ORDERED_FIFO = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = ? WHERE pat_pending_presc_id IN ( "
      + SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
      + " WHERE patient_presc_id = ? AND status = 'P' "
      + "  AND COALESCE(preauth_activity_status,'O') = ? "
      + " ORDER BY pat_pending_presc_id LIMIT ? )";

  private static final String UPDATE_PENDING_PRESC_STATUS_TO_PRESCRIBED_FIFO = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = 'P' WHERE pat_pending_presc_id IN ( ? )";

  private static final String GET_LATEST_ORDERED_PRESC_ID_FROM_PRESCRIBED_ID =
      SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
          + "WHERE patient_presc_id = :paramid AND status = 'O' "
          + " ORDER BY pat_pending_presc_id DESC LIMIT :limit";


  private static final String GET_LATEST_ORDERED_PRESC_ID_FROM_PRESCRIBED_ID_AND_PA_STATUS =
      SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
          + "WHERE patient_presc_id = :paramid AND status = 'O' "
          + " AND preauth_activity_status = :priorActStatus "
          + " ORDER BY pat_pending_presc_id DESC LIMIT :limit";

  /**
   * Update pending presc status.
   * 
   * @param prescriptionId the presc id
   * @param qty the quantity
   * @param status the status
   */
  public void updatePendingPrescriptionStatus(Integer prescriptionId, Integer qty, String status) {
    if ("O".equals(status)) {
      DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS_TO_ORDERED_FIFO, status, prescriptionId,
          qty);
    } else {
      MapSqlParameterSource param = new MapSqlParameterSource();
      param.addValue("paramid", prescriptionId);
      param.addValue("limit", qty);
      List<BasicDynaBean> beansList =
          DatabaseHelper.queryToDynaList(GET_LATEST_ORDERED_PRESC_ID_FROM_PRESCRIBED_ID, param);
      for (BasicDynaBean bean : beansList) {
        Object patPendingPrescId = bean.get("pat_pending_presc_id");
        DatabaseHelper.update(UPDATE_PRESC_ID_IN_APPOINTMENT_TABLE, patPendingPrescId);
        DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS_TO_PRESCRIBED_FIFO,
            (Object) patPendingPrescId);
      }
    }
  }

  /**
   * Update pending presc status.
   * 
   * @param prescriptionId the presc id
   * @param qty the quantity
   * @param priorAuthStatus the prior auth status
   * @param status the status
   */
  public void updatePendingPrescriptionStatus(Integer prescriptionId, Integer qty,
      String priorAuthStatus, String status) {
    if ("O".equals(status)) {
      DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS_AND_PA_STATU_TO_ORDERED_FIFO, status,
          prescriptionId, priorAuthStatus, qty);
    } else {
      MapSqlParameterSource param = new MapSqlParameterSource();
      param.addValue("paramid", prescriptionId);
      param.addValue("priorActStatus", priorAuthStatus);
      param.addValue("limit", qty);
      List<BasicDynaBean> beansList = DatabaseHelper
          .queryToDynaList(GET_LATEST_ORDERED_PRESC_ID_FROM_PRESCRIBED_ID_AND_PA_STATUS, param);
      for (BasicDynaBean bean : beansList) {
        Object patPendingPrescId = bean.get("pat_pending_presc_id");
        DatabaseHelper.update(UPDATE_PRESC_ID_IN_APPOINTMENT_TABLE, patPendingPrescId);
        DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS_TO_PRESCRIBED_FIFO,
            (Object) patPendingPrescId);
      }
    }
  }

  private static final String UPDATE_PENDING_PRESC_STATUS_TO_ORDERED_FOR_PRIOAUTH_ITEM = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = 'O' WHERE pat_pending_presc_id IN ( "
      + SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
      + " WHERE preauth_activity_id = ? AND status = 'P' "
      + " ORDER BY pat_pending_presc_id LIMIT 1 ) ";

  private static final String UPDATE_PENDING_PRESC_STATUS_TO_PRESC_FOR_PRIOAUTH_ITEM = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = 'P' WHERE pat_pending_presc_id IN ( "
      + SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
      + " WHERE preauth_activity_id = ? AND status = 'O' "
      + " ORDER BY pat_pending_presc_id DESC LIMIT 1 ) ";

  private static final String UPDATE_PENDING_PRESC_STATUS_WITH_PRIOAUTH_ID = "UPDATE "
      + " patient_pending_prescriptions " + " SET status = 'P' WHERE pat_pending_presc_id IN (?) ";

  private static final String GET_LATEST_ORDERED_PRESC_ID_FROM_PREAUTH_ID =
      SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
          + "WHERE preauth_activity_id = :paramid AND status = 'O' "
          + " ORDER BY pat_pending_presc_id DESC LIMIT 1";


  private static final String GET_LATEST_ORDERED_PRESC_ID_FROM_PREAUTH_ID_WITH_PREAUTH_STATUS =
      SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
          + "WHERE preauth_activity_id = :paramid AND status = 'O' "
          + " AND COALESCE(preauth_activity_status,'O') = :status"
          + " ORDER BY pat_pending_presc_id DESC LIMIT 1";


  /**
   * Update prior auth item status.
   *
   * @param priorAuthArg the prior auth arg
   * @param status the status
   */
  public void updatePriorAuthItemStatus(int priorAuthArg, String status) {
    if ("O".equals(status)) {
      DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS_TO_ORDERED_FOR_PRIOAUTH_ITEM,
          (Object) priorAuthArg);
    } else {
      MapSqlParameterSource param = new MapSqlParameterSource();
      param.addValue("paramid", priorAuthArg);
      BasicDynaBean bean =
          DatabaseHelper.queryToDynaBean(GET_LATEST_ORDERED_PRESC_ID_FROM_PREAUTH_ID, param);
      if (null != bean) {
        Object patPendingPrescId = bean.get("pat_pending_presc_id");

        DatabaseHelper.update(UPDATE_PRESC_ID_IN_APPOINTMENT_TABLE, (Object) patPendingPrescId);

        DatabaseHelper.update(UPDATE_PENDING_PRESC_STATUS_WITH_PRIOAUTH_ID,
            (Object) patPendingPrescId);

      }

    }
  }


  private static final String UPDATE_PENDING_PRESC_STATUS_TO_ORDERED_FOR_PRIOAUTH_ITEM_FOR_SERV =
      "  UPDATE " + " patient_pending_prescriptions "
          + " SET status = 'O' WHERE pat_pending_presc_id IN ( "
          + SELECT_PATIENT_PRESC_ID_FROM_PATIENT_PENDING_PRESC
          + " WHERE preauth_activity_id = ? AND status = 'P' "
          // preauth_activity_status can be null for Open item as well.
          + " AND COALESCE(preauth_activity_status,'O') = ? "
          + " ORDER BY pat_pending_presc_id LIMIT 1 ) ";

  private static final String UPDATE_PENDING_PRESC_STATUS_TO_PRESC_FOR_PRIOAUTH_ITEM_FOR_SERV =
      "UPDATE " + " patient_pending_prescriptions "
          + " SET status = 'P' WHERE pat_pending_presc_id IN (?) ";


  /**
   * Update prior auth item status.
   *
   * @param priorAuthArg the prior auth arg
   * @param preAuthActStatus the selected preauth status of item to be ordered
   * @param status the status
   */
  public void updatePriorAuthItemStatus(int priorAuthArg, String preAuthActStatus, String status) {
    if ("O".equals(status)) {
      DatabaseHelper.update(
          UPDATE_PENDING_PRESC_STATUS_TO_ORDERED_FOR_PRIOAUTH_ITEM_FOR_SERV
              + " AND (patient_presc_id IS NULL OR patient_presc_id = 0)",
          (Object) priorAuthArg, (Object) preAuthActStatus);
    } else {
      MapSqlParameterSource param = new MapSqlParameterSource();
      param.addValue("paramid", priorAuthArg);
      param.addValue("status", preAuthActStatus);
      BasicDynaBean bean = DatabaseHelper
          .queryToDynaBean(GET_LATEST_ORDERED_PRESC_ID_FROM_PREAUTH_ID_WITH_PREAUTH_STATUS, param);
      if (null != bean) {
        Object patPendingPrescId = bean.get("pat_pending_presc_id");

        DatabaseHelper.update(UPDATE_PRESC_ID_IN_APPOINTMENT_TABLE, (Object) patPendingPrescId);
        DatabaseHelper.update(
            UPDATE_PENDING_PRESC_STATUS_TO_PRESC_FOR_PRIOAUTH_ITEM_FOR_SERV
                + " AND (patient_presc_id IS NULL OR patient_presc_id = 0)",
            (Object) patPendingPrescId);
      }


    }
  }


  private static final String UPDATE_PRIOR_AUTH_STATUS_IN_PPD =
      "UPDATE " + " patient_pending_prescriptions "
          + " SET preauth_activity_status = :preAuthStatus " + " WHERE pat_pending_presc_id IN ("
          + "  SELECT pat_pending_presc_id " + "  FROM patient_pending_prescriptions "
          + "  WHERE preauth_activity_id = :preauthActId " + "  ORDER BY pat_pending_presc_id ";
  private static final String LIMIT = " LIMIT :quantityToUpdate " + " )";

  /**
   * Update prior auth status.
   * 
   * @param preauthActId the presc activity id
   * @param preAuthStatus the pre auth status
   * @param quantityToUpdate the qty
   * @return the integer
   */
  public int updatePriorAuthStatus(int preauthActId, String preAuthStatus, int quantityToUpdate) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("preAuthStatus", preAuthStatus);
    parameters.addValue("preauthActId", preauthActId);
    parameters.addValue("quantityToUpdate", quantityToUpdate);
    String orderBy = "";
    if ("D".equals(preAuthStatus)) {
      orderBy = "DESC";
    }
    parameters.addValue("orderBy", orderBy);
    return DatabaseHelper.update(UPDATE_PRIOR_AUTH_STATUS_IN_PPD + orderBy + LIMIT, parameters);
  }
}
