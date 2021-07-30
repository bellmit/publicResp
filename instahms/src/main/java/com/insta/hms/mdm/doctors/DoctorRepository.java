package com.insta.hms.mdm.doctors;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.integration.book.BookSDKUtil;
import com.insta.hms.mdm.MasterRepository;
import com.insta.hms.mdm.centers.CenterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class DoctorRepository.
 */
@Repository
public class DoctorRepository extends MasterRepository<String> {

  /** The center service. */
  @LazyAutowired
  private CenterService centerService;

  /** The Constant GET_DOCTORS. */
  private static final String GET_DOCTORS = "SELECT  d.*, dep.dept_name, dcm.status_on_practo "
      + " FROM doctors d " + " JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id)"
      + " JOIN department dep using (dept_id) WHERE d.status = 'A' AND dcm.status='A' @ ";

  /** The Constant SCHEUDULABLE_WHERE_CLAUSE. */
  private static final String SCHEUDULABLE_WHERE_CLAUSE = "AND d.schedule = ?";

  /** The Constant GET_DOCTOR_CENTER. */
  private static final String GET_DOCTOR_CENTER =
      " SELECT * FROM doctor_center_master WHERE doctor_id = ? AND center_id = ?";

  /** The Constant GET_DOCTOR_CENTERS_LIST. */
  private static final String GET_DOCTOR_CENTERS_LIST =
      "SELECT center_id FROM doctor_center_master WHERE doctor_id = ?";

  /** The Constant UPDATE_DOCTOR_STATUS_ON_PRACTO. */
  private static final String UPDATE_DOCTOR_STATUS_ON_PRACTO =
      "UPDATE doctor_center_master SET status_on_practo = ? "
          + " WHERE doctor_id = ? AND center_id = ? ";

  /**
   * Instantiates a new doctor repository.
   */
  public DoctorRepository() {
    super("doctors", "doctor_id", "doctor_name");
  }

  /** The Constant DOCTOR_LOOKUP_QUERY. */
  private static final String DOCTOR_LOOKUP_QUERY = "SELECT * "
      + " FROM ( SELECT d.doctor_id, d.doctor_name, d.dept_id, d.doctor_license_number, "
      + " d.practition_type, dcm.center_id, d.practitioner_id, d.ot_doctor_flag "
      + " FROM doctors d " + " JOIN doctor_center_master dcm USING (doctor_id) "
      + " WHERE d.status = 'A'" + " #deptFilter# "
      + " AND dcm.status = 'A' ORDER BY d.doctor_name) as foo ";

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.mdm.MasterRepository#getLookupQuery()
   */
  public String getLookupQuery() {
    return DOCTOR_LOOKUP_QUERY;
  }

  public String getMasterLookupQuery() {
    return super.getLookupQuery();
  }

  /**
   * Get the list of doctors for a given center.
   *
   * @param centerId - The center for which doctors are required
   * @param centersIncDefault - Total centers including default center
   * @param isSchedulable - Whether the doctors are to be scheduled
   * @return - list of doctors along with department and center details
   */
  protected List<Map<String, Object>> getDoctors(Integer centerId, int centersIncDefault,
      Boolean isSchedulable) {

    List<Object> queryParams = new ArrayList<Object>();
    String query = GET_DOCTORS;
    if (isSchedulable != null) {
      query = query.replace("@", SCHEUDULABLE_WHERE_CLAUSE);
      queryParams.add(isSchedulable);
    } else {
      query = query.replace("@", "");
    }

    if (centersIncDefault > 1 && centerId != null && centerId != 0) {
      query += " AND center_id = ?  ORDER BY doctor_name";
      queryParams.add(centerId);
    } else {
      query += " ORDER BY doctor_name";
    }

    List<BasicDynaBean> doctorsBeanList =
        DatabaseHelper.queryToDynaList(query, queryParams.toArray());
    return ConversionUtils.listBeanToListMap(doctorsBeanList);
  }

  /** The Constant GET_DOCTORS_QUERY. */
  private static final String GET_DOCTORS_QUERY =
      "SELECT doctor_id, dept_id " + " FROM doctors WHERE doctor_id IN (:doctorId) ";

  /**
   * Gets the doctors.
   *
   * @param doctorIds the doctor ids
   * @return the doctors
   */
  public List<BasicDynaBean> getDoctors(List<String> doctorIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("doctorId", doctorIds);
    return DatabaseHelper.queryToDynaList(GET_DOCTORS_QUERY, parameters);
  }

  /**
   * Gets the doctor center.
   *
   * @param doctorId the doctor id
   * @param centerId the center id
   * @return the doctor center
   */
  protected BasicDynaBean getDoctorCenter(String doctorId, int centerId) {
    return DatabaseHelper.queryToDynaBean(GET_DOCTOR_CENTER, new Object[] {doctorId, centerId});
  }

  /**
   * Gets the doctor center list.
   *
   * @param doctorId the doctor id
   * @return the doctor center list
   */
  protected List<BasicDynaBean> getDoctorCenterList(String doctorId) {
    return DatabaseHelper.queryToDynaList(GET_DOCTOR_CENTERS_LIST, new Object[] {doctorId});
  }

  /**
   * Update doctor status on Practo.
   *
   * @param doctorId String
   * @param centerId int
   * @param status String
   * @return int
   */
  public int updateDoctorStatusOnPracto(String doctorId, int centerId, String status) {
    BasicDynaBean doctorCenter = getDoctorCenter(doctorId, centerId);
    String existingStatusOnPracto = (String) doctorCenter.get("status_on_practo");
    if (existingStatusOnPracto != null
        && existingStatusOnPracto.equalsIgnoreCase(BookSDKUtil.PRACTO_BOOK_ENABLED)
        && status.equalsIgnoreCase(BookSDKUtil.PRACTO_DOCTOR_LISTED)) {
      // skip the update of doctor status
      return 0;
    }
    return DatabaseHelper.update(UPDATE_DOCTOR_STATUS_ON_PRACTO,
        new Object[] {status, doctorId, centerId});
  }

  /** The Constant GET_DOCTOR_PAYMENT_BEAN. */
  private static final String GET_DOCTOR_PAYMENT_BEAN =
      " SELECT * FROM (SELECT doctor_id, doctor_name, specialization, doctor_type, dept_id,"
          + " ot_doctor_flag, consulting_doctor_flag, "
          + "  qualification, payment_category::varchar, custom_field1_value, "
          + " custom_field2_value, custom_field3_value, custom_field4_value, "
          + " custom_field5_value, payment_eligible " + " FROM " + " doctors " + " UNION "
          + " SELECT referal_no as doctor_id, referal_name as doctor_name,"
          + " null as specialization, null as doctor_type,  "
          + " null as dept_id,  null as ot_doctor_flag, null as consulting_doctor_flag,"
          + " null as qualification, payment_category::varchar, null AS custom_field1_value,"
          + " null AS custom_field2_value, null AS custom_field3_value,  "
          + " null AS custom_field4_value,null AS  custom_field5_value, payment_eligible "
          + " FROM referral) AS foo where doctor_id = ? ";

  /**
   * Gets the doctor payment bean.
   *
   * @param doctorId the doctor id
   * @return the doctor payment bean
   */
  public BasicDynaBean getDoctorPaymentBean(String doctorId) {
    return DatabaseHelper.queryToDynaBean(GET_DOCTOR_PAYMENT_BEAN, new Object[] {doctorId});
  }

  /** The Constant GET_DOCTOR_DEPT_QUERY. */
  private static final String GET_DOCTOR_DEPT_QUERY =
      " SELECT d.doctor_id, d.doctor_name, d.dept_id, dept.dept_name, d.service_sub_group_id, "
          + "  dopc.op_charge, dopc.op_charge_discount, "
          + "  dopc.op_revisit_charge, dopc.op_revisit_discount as op_revisit_charge_discount, "
          + "  dopc.op_oddhr_charge, dopc.op_oddhr_charge_discount, dopc.private_cons_charge, "
          + "  dopc.private_cons_discount as private_cons_charge_discount, "
          + "  dopc.private_cons_revisit_charge, "
          + "  dopc.private_revisit_discount as private_cons_revisit_charge_discount, "
          + "  dc.doctor_ip_charge, dc.doctor_ip_charge_discount, "
          + "  dc.night_ip_charge, dc.night_ip_charge_discount, "
          + "  dc.ward_ip_charge, dc.ward_ip_charge_discount, "
          + "  dc.ot_charge, dc.ot_charge_discount, "
          + "  dc.co_surgeon_charge, dc.co_surgeon_charge_discount, "
          + "  dc.assnt_surgeon_charge, dc.assnt_surgeon_charge_discount, " + "  d.ot_doctor_flag "
          + "  FROM doctors d" + "  JOIN department dept ON (dept.dept_id = d.dept_id)"
          + "  JOIN doctor_consultation_charge dc ON (dc.doctor_name = d.doctor_id)"
          + "  JOIN doctor_op_consultation_charge dopc ON (dopc.doctor_id = d.doctor_id) ";

  /**
   * get doctor charges.
   *
   * @param doctorId String
   * @param orgId String
   * @param bedType String
   * @return BasicDynaBean
   */
  public BasicDynaBean getDoctorCharges(String doctorId, String orgId, String bedType) {
    BasicDynaBean doctorCharges = null;
    List dynalist = null;
    String getDoctorDeptQueryWithBedOrg =
        GET_DOCTOR_DEPT_QUERY + "  WHERE dc.bed_type=? AND dc.organization=? "
            + " AND dopc.org_id=? AND d.doctor_id=? ORDER BY doctor_name";

    dynalist = DatabaseHelper.queryToDynaList(getDoctorDeptQueryWithBedOrg, bedType, orgId, orgId,
        doctorId);
    if (dynalist.size() > 0) {
      doctorCharges = (BasicDynaBean) dynalist.get(0);
    } else {

      String getDoctorDeptQueryWithDefaultBedOrg =
          GET_DOCTOR_DEPT_QUERY + " WHERE dc.bed_type='GENERAL' AND dc.organization='ORG0001' "
              + " AND dopc.org_id='ORG0001' AND d.doctor_id=? ORDER BY doctor_name";

      dynalist = DatabaseHelper.queryToDynaList(getDoctorDeptQueryWithDefaultBedOrg, doctorId);
      if (dynalist.size() > 0) {
        doctorCharges = (BasicDynaBean) dynalist.get(0);
      }
    }
    return doctorCharges;
  }

  /**
   * The Constant GET_DOCTOR_DEPT_QUERY.
   */
  private static final String ALL_BED_TYPE_GET_DOCTOR_DEPT_QUERY =
      " SELECT dopc.op_charge, dopc.op_charge_discount, "
          + "  dopc.op_revisit_charge, dopc.op_revisit_discount as op_revisit_charge_discount, "
          + "  dopc.op_oddhr_charge, dopc.op_oddhr_charge_discount, dopc.private_cons_charge, "
          + "  dopc.private_cons_discount as private_cons_charge_discount, "
          + "  dopc.private_cons_revisit_charge, "
          + "  dopc.private_revisit_discount as private_cons_revisit_charge_discount, "
          + "  dc.doctor_ip_charge, dc.doctor_ip_charge_discount, "
          + "  dc.night_ip_charge, dc.night_ip_charge_discount, "
          + "  dc.ward_ip_charge, dc.ward_ip_charge_discount, "
          + "  dc.ot_charge, dc.ot_charge_discount, dc.bed_type, "
          + "  dc.co_surgeon_charge, dc.co_surgeon_charge_discount, "
          + "  dc.assnt_surgeon_charge, dc.assnt_surgeon_charge_discount "
          + "  FROM doctor_consultation_charge dc "
          + "  JOIN doctor_op_consultation_charge dopc ON (dopc.doctor_id = dc.doctor_name "
          + "  And dc.organization = dopc.org_id) ";

  /**
   * get doctor charges of all bed types.
   *
   * @param doctorId String
   * @param orgId String
   * @return BasicDynaBean
   */
  public List<BasicDynaBean> getAllDoctorCharges(String doctorId, String orgId) {
    List<BasicDynaBean> dynalist = null;
    String getDoctorDeptQueryWithBedOrg = ALL_BED_TYPE_GET_DOCTOR_DEPT_QUERY
        + "  WHERE dc.organization=? " + " AND dopc.org_id=? AND dc.doctor_name=? ";

    dynalist = DatabaseHelper.queryToDynaList(getDoctorDeptQueryWithBedOrg, orgId, orgId, doctorId);
    return dynalist;
  }

  /** The Constant CONSULTATION_CHARGES. */
  public static final String CONSULTATION_CHARGES = " SELECT cc.*, cod.item_code, cod.code_type "
      + " FROM consultation_charges cc " + " JOIN consultation_org_details cod "
      + " ON (cod.consultation_type_id = cc.consultation_type_id " + " AND cod.org_id = cc.org_id) "
      + " WHERE cc.consultation_type_id = ? AND cc.bed_type = ? AND cc.org_id = ? ";

  /**
   * Gets the consultation charges.
   *
   * @param consultationId the consultation id
   * @param bedType the bed type
   * @param ratePlan the rate plan
   * @return the consultation charges
   */
  public BasicDynaBean getConsultationCharges(int consultationId, String bedType, String ratePlan) {
    Object[] params = {consultationId, bedType, ratePlan};
    return DatabaseHelper.queryToDynaBean(CONSULTATION_CHARGES, params);
  }

  /** The Constant GET_CODE_DESC. */
  public static final String GET_CODE_DESC =
      "SELECT code_desc FROM mrd_codes_master " + " WHERE code = ? AND code_type = ? ";

  /**
   * Gets the code desc.
   *
   * @param code the code
   * @param codeType the code type
   * @return the code desc
   */
  public String getCodeDesc(String code, String codeType) {
    return DatabaseHelper.getString(GET_CODE_DESC, code, codeType);
  }

  /** The Constant PRES_DOCTORS. */
  private static final String PRES_DOCTORS =
      "SELECT doctor_name AS item_name, d.doctor_id AS item_id, 'Doctor' AS item_type, d.dept_id,"
          + " dept.dept_name, 'DOC' as presc_activity_type,"
          + " COALESCE(cat.insurance_category_id,0) AS insurance_category_id, "
          + " COALESCE(cat.insurance_category_name,'') AS insurance_category_name, "
          + " COALESCE(cat.category_payable,'N') AS category_payable, "
          + " cc.charge + docc.op_charge AS charge, "
          + " cc.discount + docc.op_charge_discount AS discount, cod.applicable "
          + " FROM doctors d " + " JOIN department dept ON (dept.dept_id=d.dept_id) "
          + " JOIN consultation_org_details cod ON (cod.consultation_type_id =-1 AND cod.org_id=?)"
          + " JOIN consultation_charges cc "
          + " ON (cc.org_id=? AND cc.bed_type = ? AND cc.consultation_type_id = -1) "
          + " JOIN doctor_op_consultation_charge docc "
          + " ON (docc.doctor_id=d.doctor_id AND docc.org_id=cc.org_id) "
          + " JOIN consultation_types ct ON (ct.consultation_type_id=cc.consultation_type_id) "
          + " LEFT JOIN LATERAL(SELECT cic.consultation_type_id AS consultation_type_id,"
          + " cic.insurance_category_id, iic.insurance_category_name, ipd.category_payable "
          + " FROM consultation_types_insurance_category_mapping cic "
          + " JOIN item_insurance_categories iic "
          + " ON (cic.insurance_category_id = iic.insurance_category_id) "
          + " JOIN insurance_plan_details ipd "
          + " ON (ipd.insurance_category_id = iic.insurance_category_id "
          + " AND ipd.patient_type = ? AND ipd.plan_id=?) "
          + " WHERE ct.consultation_type_id = cic.consultation_type_id "
          + " ORDER BY iic.priority LIMIT 1) as cat "
          + " ON (cat.consultation_type_id = ct.consultation_type_id) "
          + " WHERE d.status='A' AND (doctor_name ilike ? OR doctor_name ilike ?) "
          + " ORDER BY doctor_name " + " LIMIT ?";

  /**
   * get doctors prescription.
   *
   * @param bedType String
   * @param orgId String
   * @param patientType String
   * @param insPlanId Integer
   * @param searchQuery String
   * @param itemLimit Integer
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getDoctorsForPrescription(String bedType, String orgId,
      String patientType, Integer insPlanId, String searchQuery, Integer itemLimit) {
    return DatabaseHelper.queryToDynaList(PRES_DOCTORS, new Object[] {orgId, orgId, bedType,
        patientType, insPlanId, searchQuery + "%", "% " + searchQuery + "%", itemLimit});
  }

  /** The Constant DOCTORS_OVERBOOK. */
  private static final String DOCTORS_OVERBOOK =
      "SELECT overbook_limit from doctors where doctor_id=?";

  /**
   * Gets the doctor overbook limit.
   *
   * @param doctorId the doctor id
   * @return the doctor overbook limit
   */
  public Integer getDoctorOverbookLimit(String doctorId) {
    return DatabaseHelper.getInteger(DOCTORS_OVERBOOK, doctorId);
  }

  /** The Constant GET_RESOURCE_CENTER_LIST. */
  private static final String GET_RESOURCE_CENTER_LIST =
      "SELECT d.doctor_id ,dcm.center_id,hcm.center_name" + " FROM  doctors d "
          + " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"
          + " JOIN hospital_center_master hcm ON (hcm.center_id=dcm.center_id)"
          + " where d.doctor_id=? and dcm.status='A' ORDER BY center_name ";

  /**
   * get resources for center.
   *
   * @param doctorId String
   * @return List of BasicDynaBean
   */
  public List<BasicDynaBean> getResourceBelongingCenter(String doctorId) {
    List<BasicDynaBean> resCenters = null;
    resCenters = DatabaseHelper.queryToDynaList(GET_RESOURCE_CENTER_LIST, new Object[] {doctorId});
    if (resCenters != null && resCenters.size() == 1) {
      Integer resCenterId = (Integer) resCenters.get(0).get("center_id");
      if (resCenterId.equals(0)) {
        resCenters = centerService.getAllCentersAndSuperCenterAsFirst();
      }
    }

    return resCenters;
  }

  /** The Constant GET_DOCTOR_FROM_CONS_ID. */
  private static final String GET_DOCTOR_FROM_CONS_ID = "SELECT d.*  FROM doctor_consultation dc "
      + " JOIN doctors d ON (d.doctor_id=dc.doctor_name) WHERE dc.consultation_id=?";

  private static final String GET_DOCTOR_FROM_VISIT_ID = "SELECT d.*  FROM patient_registration pr "
      + " JOIN doctors d ON (d.doctor_id=pr.doctor) WHERE patient_id=?";

  /**
   * Gets the doctor by cons id.
   *
   * @param consId the cons id
   * @return the doctor by cons id
   */
  public BasicDynaBean getDoctorByConsId(Object consId) {
    BasicDynaBean doctorBean = (consId instanceof String)
        ? DatabaseHelper.queryToDynaBean(GET_DOCTOR_FROM_VISIT_ID, new Object[] {consId})
        : DatabaseHelper.queryToDynaBean(GET_DOCTOR_FROM_CONS_ID, new Object[] {consId});
    return doctorBean;
  }

  /** The Constant GET_OT_DOCTOR_CHARGES. */
  private static final String GET_OT_DOCTOR_CHARGES =
      "SELECT ot_charge as charge, " + " ot_charge_discount as discount, "
          + " co_surgeon_charge as cosurgeoncharge, co_surgeon_charge_discount, "
          + " assnt_surgeon_charge as asst_charge, assnt_surgeon_charge_discount, "
          + " d.doctor_id, d.doctor_name, d.dept_id " + " FROM doctor_consultation_charge dcc "
          + " JOIN doctors d ON (d.doctor_id = dcc.doctor_name) "
          + " WHERE dcc.doctor_name=? and bed_type=? and organization=?";

  /**
   * Gets the OT doctor charges bean.
   *
   * @param doctorId the doctor id
   * @param bedType the bed type
   * @param orgId the org id
   * @return the OT doctor charges bean
   */
  public BasicDynaBean getOtDoctorChargesBean(String doctorId, String bedType, String orgId) {
    BasicDynaBean docchargebean =
        DatabaseHelper.queryToDynaBean(GET_OT_DOCTOR_CHARGES, doctorId, bedType, orgId);
    if (docchargebean == null) {
      docchargebean =
          DatabaseHelper.queryToDynaBean(GET_OT_DOCTOR_CHARGES, doctorId, "GENERAL", "ORG0001");
    }
    return docchargebean;
  }

  /** The Constant GET_ALL_DOCTORS. */
  private static final String GET_ALL_DOCTORS = " select d.doctor_id , d.doctor_name,"
      + " d.specialization,  d.op_consultation_validity, d.registration_no, "
      + " d.doctor_type, d.doctor_address, d.doctor_mobile, d.doctor_mail_id, "
      + " d.dept_id, d.ot_doctor_flag, d.consulting_doctor_flag, d.schedule, d.qualification, "
      + " d.res_phone, d.clinic_phone, d.payment_category, d.payment_eligible,"
      + " d.doctor_license_number,  d.allowed_revisit_count, "
      + " d.custom_field1_value, d.custom_field2_value, d.custom_field3_value,"
      + " d.custom_field4_value, d.custom_field5_value, d.ip_discharge_consultation_validity, "
      + " hcm.center_name, d.ip_discharge_consultation_count, d.ip_template_id, d.overbook_limit,"
      + " to_char(d.created_timestamp AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT "
      + " TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS created_timestamp,"
      + " to_char(d.updated_timestamp AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT "
      + " TIME ZONE 'UTC','YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS updated_timestamp,"
      + " d.practition_type, d.status as doctor_status, "
      + " d.available_for_online_consults, dcm.status as doctor_center_status, "
      + " dcm.doc_center_id, dcm.center_id ,dept.dept_name from "
      + " doctors d LEFT JOIN doctor_center_master dcm ON(dcm.doctor_id=d.doctor_id)"
      + " LEFT JOIN department dept ON(d.dept_id=dept.dept_id)"
      + " join hospital_center_master hcm on(dcm.center_id=hcm.center_id) "
      + " WHERE @ d.scheduleable_by = ? order by doctor_name";

  /**
   * Gets the all doctors.
   *
   * @param sendOnlyActiveData the send only active data
   * @return the all doctors
   */
  public List<BasicDynaBean> getAllDoctors(boolean sendOnlyActiveData, boolean scheduleableAll) {
    String query = GET_ALL_DOCTORS;
    if (sendOnlyActiveData) {
      query = query.replace("@", "d.status = 'A' AND dcm.status = 'A' AND hcm.status='A' AND");
    } else {
      query = query.replace("@", "");
    }
    String param = scheduleableAll ? "A" : "S";
    return DatabaseHelper.queryToDynaList(query, param);
  }

  private static final String GET_DOCTOR_USER_NAMES = "SELECT d.doctor_id as user_id,"
      + " d.doctor_name as user_name " + " FROM doctors d"
      + " JOIN doctor_center_master  dcm ON(d.doctor_id=dcm.doctor_id) " + " WHERE d.status = 'A'";

  /**
   * Gets the doctor user names.
   *
   * @param centerId the center id
   * @param centersIncDefault the centers inc default
   * @return the doctor user names
   */
  public List<BasicDynaBean> getDoctorUserNames(Integer centerId, int centersIncDefault) {
    List<Object> queryParams = new ArrayList<Object>();
    String query = GET_DOCTOR_USER_NAMES;
    if (centersIncDefault > 1 && centerId != null && centerId != 0) {
      query += " AND (center_id = ? OR center_id=0) ORDER BY doctor_name";
      queryParams.add(centerId);
    } else {
      query += " ORDER BY doctor_name";
    }
    return DatabaseHelper.queryToDynaList(query, queryParams.toArray());
  }

  private static final String GET_DEPARTMENT_DOCTORS =
      "select doctor_id as entity_id" + " from doctors where dept_id= ? AND status='A' limit 20 ";

  /**
   * Gets the department doctors.
   *
   * @param departmentId the department id
   * @return the department doctors
   */
  public List<BasicDynaBean> getDepartmentDoctors(String departmentId) {
    return DatabaseHelper.queryToDynaList(GET_DEPARTMENT_DOCTORS, departmentId);
  }

  private static final String DOC_REFDOC_LIST =
      "SELECT" + " doctor_id,doctor_name FROM doctors WHERE doctor_name ILIKE :docName"
          + " UNION SELECT referal_no AS doctor_id,referal_name AS doctor_name"
          + " FROM referral WHERE  referal_name ILIKE :docName";

  /**
   * Get Doctor and ReferalDoctor list.
   * 
   * @param searchString the search string
   * @return list of beans
   */
  public List<BasicDynaBean> getDoctorAndReferalDoctor(String searchString) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("docName", "%" + searchString + "%");
    return DatabaseHelper.queryToDynaList(DOC_REFDOC_LIST, parameter);
  }
  
  private static final String GET_DOCTOR_DEPARTMENT_INFO =
      "SELECT doctor_id, doctor_name, dept_id, dept_name " + "FROM doctors doc "
          + "LEFT JOIN department dept USING(dept_id) WHERE doctor_id IN (:doctorId)";

  /**
   * Get doctor and department info.
   * 
   * @param doctorIds list of all doctorIds
   * @return list of beans
   */
  public List<BasicDynaBean> getDoctorDepartmentInfo(List<String> doctorIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("doctorId", doctorIds);
    return DatabaseHelper.queryToDynaList(GET_DOCTOR_DEPARTMENT_INFO, parameters);
  }
}
