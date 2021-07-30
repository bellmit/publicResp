package com.insta.hms.integration.hl7.v2;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.modulesactivated.ModulesActivatedService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Hl7Repository {

  @LazyAutowired
  private ModulesActivatedService modulesActivatedService;

  @LazyAutowired
  private PatientRegistrationRepository patientRegistrationRepository;

  private static final String MODULE_ADVANCE_OT = "mod_advanced_ot";

  private static final String MODULE_BASIC_OT = "mod_basic_ot";

  /**
   * Get Patient Data To Validate.
   * 
   * @param visitId the visitId
   * @return dyna bean
   */
  public BasicDynaBean getPatientDataToValidate(String visitId) {
    return patientRegistrationRepository.findByKey("patient_id", visitId);
  }

  private static final String SEGMENT_TEMPLATE_QUERY = "SELECT segment_field_number,"
      + " field_template FROM hl7_segment_template WHERE status = 'A'"
      + " AND segment_field_number ILIKE '#segmentName#_%'"
      + " AND version = ? AND interface_id = ?";

  private static final String HOSP_CENTER_QUERY = "SELECT center_name," 
      + " hospital_center_service_reg_no AS center_service_reg_no"
      + " FROM hospital_center_master WHERE center_id = ?";

  public BasicDynaBean getCenterDetails(int centerId) {
    return DatabaseHelper.queryToDynaBean(HOSP_CENTER_QUERY, new Object[] {centerId});
  }

  /**
   * Get segment template.
   * 
   * @param segmentName the name of the segment
   * @param version the message version
   * @param interfaceId the interface id
   * @return list of bean having field wise template
   */
  public List<BasicDynaBean> getSegmentTemplate(String segmentName, String version,
      int interfaceId) {
    return DatabaseHelper.queryToDynaList(
        SEGMENT_TEMPLATE_QUERY.replaceAll("#segmentName#", segmentName), version, interfaceId);
  }

  private static final String PATIENT_QUERY = "SELECT pd.mr_no, pd.passport_no, pd.last_name,"
      + " pd.patient_name AS first_name, pd.dateofbirth AS date_of_birth, pd.email_id,"
      + " pd.expected_dob AS expected_date_of_birth, pd.death_date, pd.death_time,pd.middle_name,"
      + " pd.patient_gender AS gender, pd.patient_address, c.city_name AS patient_city_name,"
      + " sm.id AS patient_state_id,sm.state_name AS patient_state_name,pd.custom_list9_value,"
      + " country.id AS patient_country_id, country.country_name AS patient_country_name,"
      + " pd.patient_phone, pd.patient_phone2 AS alternate_phone, msm.marital_status_id,"
      + " msm.marital_status_name AS marital_status, rm.religion_id, rm.religion_name AS religion,"
      + " pd.government_identifier, cm.id AS nationality_id, cm.country_name AS nationality,"
      + " c.city_id AS patient_city_id, pd.vip_status, pd.mod_time AS last_updated_date_time,"
      + " pd.race_id, pd.patient_group, cp.lang_code FROM patient_details pd"
      + " LEFT JOIN marital_status_master msm ON pd.marital_status_id=msm.marital_status_id"
      + " LEFT JOIN religion_master rm ON pd.religion_id=rm.religion_id"
      + " LEFT JOIN country_master cm ON pd.nationality_id=cm.country_id"
      + " LEFT JOIN country_master country ON country.country_id = pd.country"
      + " LEFT JOIN contact_preferences cp ON cp.mr_no = pd.mr_no"
      + " LEFT JOIN state_master sm ON sm.state_id = pd.patient_state"
      + " LEFT JOIN city c ON c.city_id = pd.patient_city WHERE pd.mr_no = ?";

  /**
   * Gets patient data.
   * 
   * @param mrNo the mr_no
   * @return list of beans
   */
  public List<BasicDynaBean> getPatientData(String mrNo) {
    return DatabaseHelper.queryToDynaList(PATIENT_QUERY, mrNo);
  }

  private static final String VISIT_QUERY = "SELECT pr.patient_id AS visit_id, pr.op_type,"
      + " dept.id AS department_id, UPPER(pr.visit_type) AS visit_type,"
      + " dept.dept_name AS cons_admit_dept, doc.doctor_license_number,"
      + " doc.doctor_name, doc.doc_first_name, doc.doc_middle_name, doc.doc_last_name,"
      + " pr.reg_date, pr.reg_time, pr.discharge_type_id, pr.status AS visit_status,"
      + " pr.discharge_date, pr.discharge_time, pr.encounter_type,"
      + " th.transfer_hospital_name, bn.bed_name, bn.ward_no, dtm.discharge_type,"
      + " pr.complaint AS chief_complaint, md.icd_code AS diagnosis_code,"
      + " md.description AS diagnosis_description, pr.doctor AS doctor_id,"
      + " a.parent_id AS parent_visit_id, a.isbaby, p_pr.mr_no AS parent_mr_no,"
      + " p_pd.government_identifier AS parent_government_identifier,"
      + " wn.ward_no, wn.ward_name_id, wn.ward_name, etv.encounter_types_visit_name"
      + " FROM patient_registration pr LEFT JOIN department dept ON pr.dept_name=dept.dept_id"
      + " LEFT JOIN doctors doc ON pr.doctor=doc.doctor_id"
      + " LEFT JOIN ip_bed_details ibd ON pr.patient_id=ibd.patient_id"
      + " LEFT JOIN bed_names bn ON ibd.bed_id=bn.bed_id"
      + " LEFT JOIN ward_names wn ON wn.ward_no = bn.ward_no"
      + " LEFT JOIN discharge_type_master dtm ON pr.discharge_type_id = dtm.discharge_type_id"
      + " LEFT JOIN transfer_hospitals th ON th.transfer_hospital_id = pr.transfer_destination"
      + " LEFT JOIN mrd_diagnosis md ON (pr.patient_id = md.visit_id AND md.diag_type='P')"
      + " LEFT JOIN admission a ON (a.patient_id = pr.patient_id)"
      + " LEFT JOIN patient_registration p_pr ON (p_pr.patient_id = a.parent_id)"
      + " LEFT JOIN patient_details p_pd ON (p_pd.mr_no = p_pr.mr_no)"
      + " LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = pr.encounter_type)"
      + " LEFT JOIN encounter_types_visits etv"
      + " ON (etv.encounter_types_visit_id = etc.encounter_visit_type)"
      + " WHERE pr.patient_id = ?";

  /**
   * Gets visit data.
   * 
   * @param visitId the visitId
   * @return list of beans
   */
  public List<BasicDynaBean> getVisitData(String visitId) {
    return DatabaseHelper.queryToDynaList(VISIT_QUERY, visitId);
  }

  private static final String ALLERGY_QUERY = "SELECT pa.allergy_id, "
      + " CASE WHEN pa.allergy_type_id is null THEN 'N' ELSE atm.allergy_type_code END "
      + " as allergy_type, pa.onset_date, pa.mod_time, pa.severity,"
      + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
      + " pa.created_at, pa.reaction, atm.allergy_type_id, atm.allergy_type_code, "
      + " atm.allergy_type_name, am.allergen_code_id, am.allergen_description, "
      + " gn.generic_name, gn.generic_name_id "
      + " FROM patient_allergies pa "
      + " JOIN patient_section_details psd ON (psd.section_detail_id = pa.section_detail_id"
      + " AND psd.section_status = 'A' AND psd.patient_id = ?)"
      + " LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)"
      + " LEFT JOIN allergen_master am ON (am.allergen_code_id = pa.allergen_code_id)"
      + " LEFT JOIN generic_name gn ON (gn.allergen_code_id = pa.allergen_code_id)"
      + " WHERE pa.status = 'A' ";

  /**
   * Gets Allergies data.
   * 
   * @param visitId the visitId
   * @return list of beans
   */
  public List<BasicDynaBean> getAllergiesData(String visitId) {
    return DatabaseHelper.queryToDynaList(ALLERGY_QUERY, visitId);
  }

  private static final String INSURANCE_QUERY = "SELECT pip.plan_id AS ins_plan_id,"
      + " pip.insurance_co AS ins_comp_id, icm.insurance_co_name, ppd.member_id AS member_id,"
      + " pip.priority, coalesce(hic.insurance_co_code,'') as ins_co_code,"
      + " ppd.policy_validity_start,ppd.policy_validity_end"
      + " FROM patient_insurance_plans pip"
      + " LEFT JOIN insurance_company_master icm ON (pip.insurance_co = icm.insurance_co_id)"
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = ?)"
      + " LEFT JOIN ha_ins_company_code hic ON (hcm.health_authority = hic.health_authority"
      + " AND hic.insurance_co_id = pip.insurance_co)"
      + " LEFT JOIN patient_policy_details ppd ON (pip.patient_policy_id = ppd.patient_policy_id)"
      + " WHERE pip.patient_id = ? ORDER BY pip.priority";

  /**
   * Gets insurance data.
   * 
   * @param visitId the visitId
   * @return list of beans
   */
  public List<BasicDynaBean> getInsuranceData(String visitId, int centerId) {
    return DatabaseHelper.queryToDynaList(INSURANCE_QUERY, new Object[] {centerId, visitId});
  }

  private static final String DIAGNOSIS_QUERY = "SELECT"
      + " md.icd_code AS diagnosis_code, md.description AS diagnosis_description, md.diag_type,"
      + " md.diagnosis_datetime, md.diagnosis_status_id, ds.diagnosis_status_name"
      + " FROM mrd_diagnosis md"
      + " LEFT JOIN diagnosis_statuses ds ON (ds.diagnosis_status_id = md.diagnosis_status_id)"
      + " WHERE visit_id = ? ORDER BY md.diagnosis_datetime DESC";

  public List<BasicDynaBean> getDiagnosisData(String patientId) {
    return DatabaseHelper.queryToDynaList(DIAGNOSIS_QUERY, patientId);
  }

  public boolean diagnosisExists(String patientId) {
    return DatabaseHelper.getBoolean("SELECT exists (" + DIAGNOSIS_QUERY + ")", patientId);
  }

  private static final String ADVANCED_SURGERY_ORDER_DATA_QUERY = "SELECT"
      + " od.operation_details_id, ood.item_code AS item_code, mcm.code_desc AS item_code_desc,"
      + " od.conduction_remarks, od.surgery_start AS surgery_start_datetime,"
      + " od.surgery_end AS surgery_end_datetime, md.icd_code AS diagnosis_code,"
      + " md.description AS diagnosis_description, op.oper_priority,"
      + " od.operation_status AS operation_conducted_status FROM operation_details od"
      + " LEFT JOIN operation_procedures op ON op.operation_details_id = od.operation_details_id"
      + " LEFT JOIN mrd_diagnosis md ON (md.visit_id = od.patient_id AND md.diag_type = 'P')"
      + " LEFT JOIN patient_registration pr ON pr.patient_id = od.patient_id"
      + " LEFT JOIN operation_org_details ood"
      + " ON (ood.org_id = pr.org_id AND ood.operation_id = op.operation_id)"
      + " LEFT JOIN mrd_codes_master mcm"
      + " ON (ood.item_code = mcm.code AND ood.code_type = mcm.code_type)"
      + " WHERE od.patient_id = :visitId";

  private static final String SURGEON_DATA_QUERY = "SELECT d.doctor_license_number"
      + " AS surgeon_license_number, d.doc_first_name AS surgeon_first_name,"
      + " d.doc_last_name AS surgeon_last_name"
      + " FROM operation_team ot LEFT JOIN doctors d ON (ot.resource_id = d.doctor_id)"
      + " WHERE ot.operation_speciality='SU' AND ot.operation_details_id = :operationDetailsId"
      + " ORDER BY ot.operation_team_id LIMIT 1";

  private static final String ANAESTHETIST_DATA_QUERY = "SELECT"
      + " d.doctor_license_number AS anaesthetist_license_number,"
      + " d.doc_first_name AS anaesthetist_first_name, d.doc_last_name AS anaesthetist_last_name"
      + " FROM operation_team ot LEFT JOIN doctors d ON (ot.resource_id = d.doctor_id)"
      + " WHERE ot.operation_speciality='AN' AND ot.operation_details_id = :operationDetailsId"
      + " ORDER BY ot.operation_team_id LIMIT 1";

  private static final String BASIC_SURGERY_ORDER_DATA_QUERY = "SELECT ood.item_code AS item_code,"
      + " mcm.code_desc AS item_code_desc, bos.remarks AS conduction_remarks,"
      + " bos.start_datetime AS surgery_start_datetime, bos.end_datetime AS surgery_end_datetime,"
      + " md.icd_code AS diagnosis_code, md.description AS diagnosis_description,"
      + " d_surgon.doctor_license_number AS surgeon_license_number,"
      + " d_surgon.doc_first_name AS surgeon_first_name,"
      + " d_surgon.doc_last_name AS surgeon_last_name,"
      + " d_anesthes.doctor_license_number AS anaesthetist_license_number,"
      + " d_anesthes.doc_first_name AS anaesthetist_first_name,"
      + " d_anesthes.doc_last_name AS anaesthetist_last_name,"
      + " bos.status AS operation_conducted_status FROM bed_operation_schedule bos"
      + " LEFT JOIN mrd_diagnosis md ON (md.visit_id = bos.patient_id AND md.diag_type = 'P')"
      + " LEFT JOIN patient_registration pr ON pr.patient_id = bos.patient_id"
      + " LEFT JOIN operation_org_details ood"
      + " ON (ood.org_id = pr.org_id AND ood.operation_id = bos.operation_name)"
      + " LEFT JOIN mrd_codes_master mcm"
      + " ON (ood.item_code = mcm.code AND ood.code_type = mcm.code_type)"
      + " LEFT JOIN doctors d_surgon ON (bos.surgeon = d_surgon.doctor_id)"
      + " LEFT JOIN doctors d_anesthes ON (bos.anaesthetist = d_anesthes.doctor_id)"
      + " WHERE bos.patient_id = :visitId";

  /**
   * Get Surgery Order Data.
   * 
   * @param visitId the visit id
   * @return list of beans
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getSurgeryData(String visitId, Integer operationId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("visitId", visitId);
    List<Map<String, Object>> opDeatilsMapList = null;
    StringBuilder query;
    if (modulesActivatedService.isModuleActivated(MODULE_ADVANCE_OT)) {
      query = new StringBuilder(ADVANCED_SURGERY_ORDER_DATA_QUERY);
      if (!StringUtils.isEmpty(operationId)) {
        query.append(" AND od.operation_details_id = :operationDetailsId");
        parameter.addValue("operationDetailsId", operationId);
      }
      query.append(" ORDER BY od.operation_details_id,oper_priority");
      List<BasicDynaBean> opDeatilsList =
          DatabaseHelper.queryToDynaList(query.toString(), parameter);
      Map<String, Object> opDeatilsMap = null;
      BasicDynaBean surgeonDataBean;
      BasicDynaBean anaesthetistDataBean;
      opDeatilsMapList = new ArrayList<>();
      for (BasicDynaBean bean : opDeatilsList) {
        opDeatilsMap = new HashMap<>();
        opDeatilsMap.putAll(bean.getMap());
        parameter.addValue("operationDetailsId", bean.get("operation_details_id"));
        surgeonDataBean = DatabaseHelper.queryToDynaBean(SURGEON_DATA_QUERY, parameter);
        if (surgeonDataBean != null) {
          opDeatilsMap.putAll(surgeonDataBean.getMap());
        }
        anaesthetistDataBean = DatabaseHelper.queryToDynaBean(ANAESTHETIST_DATA_QUERY, parameter);
        if (anaesthetistDataBean != null) {
          opDeatilsMap.putAll(anaesthetistDataBean.getMap());
        }
        opDeatilsMapList.add(opDeatilsMap);
      }
    } else if (modulesActivatedService.isModuleActivated(MODULE_BASIC_OT)) {
      query = new StringBuilder(BASIC_SURGERY_ORDER_DATA_QUERY);
      if (!StringUtils.isEmpty(operationId)) {
        query.append(" AND bos.prescribed_id = :prescribedId");
        parameter.addValue("prescribedId", operationId);
      }
      query.append(" ORDER BY bos.prescribed_id");
      opDeatilsMapList = ConversionUtils
          .listBeanToListMap(DatabaseHelper.queryToDynaList(query.toString(), parameter));
    } else {
      opDeatilsMapList = Collections.<Map<String, Object>>emptyList();
    }
    return opDeatilsMapList;
  }

  private static final String CHRONIC_PROBLEMS_QUERY = "SELECT ppl.ppl_id, ppl.mr_no,"
      + " ppl.patient_problem_id, ppl.status, ppl.onset, mcm.code_type, ppl.recorded_by,"
      + " ppl.recorded_date, ppl.problem_note, mcm.code AS patient_problem_code,"
      + " mcm.code_desc AS patient_problem_desc, d.doctor_name AS recorded_by_name,"
      + " d.doctor_license_number AS recorded_by_doctor_license_number,"
      + " d.doc_first_name AS recorded_by_doctor_first_name,"
      + " d.doc_last_name AS recorded_by_doctor_last_name FROM patient_problem_list ppl"
      + " LEFT JOIN mrd_codes_master mcm ON mcm.mrd_code_id = ppl.patient_problem_id"
      + " LEFT JOIN doctors d ON ppl.recorded_by = d.doctor_id WHERE ppl.ppl_id IN (:pplIds)";

  private static final String CHRONIC_PROBLEM_DETAILS_QUERY = "SELECT ppld.visit_id,"
      + " ppld.problem_status, ppld.last_status_date, ppld.created_by AS modified_by,"
      + " ppld.created_at AS last_status_modified_at"
      + " FROM patient_problem_list_details ppld WHERE ppld.ppl_id=:pplId"
      + " ORDER BY ppld.created_at DESC LIMIT 1";

  /**
   * Get Last Updated Problem Details.
   * 
   * @param pplId the pplid
   * @param endDateTime the datetime
   * @return bean
   */
  private BasicDynaBean getLastUpdatedProblemDetails(int pplId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("pplId", pplId);
    return DatabaseHelper.queryToDynaBean(CHRONIC_PROBLEM_DETAILS_QUERY, parameter);
  }

  /**
   * Get problem list.
   * 
   * @param pplIdList the patient problem id list
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getChronicProblemsData(List<Integer> pplIdList) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("pplIds", pplIdList);
    List<BasicDynaBean> allPatientProblemList =
        DatabaseHelper.queryToDynaList(CHRONIC_PROBLEMS_QUERY, parameter);

    List<Map<String, Object>> problemMapList = new ArrayList<>();
    Map<String, Object> beanMap = null;
    for (BasicDynaBean bean : allPatientProblemList) {
      beanMap = new HashMap<>();
      beanMap.putAll(bean.getMap());
      beanMap.putAll(getLastUpdatedProblemDetails((int) bean.get("ppl_id")).getMap());
      problemMapList.add(beanMap);
    }
    return problemMapList;
  }

  private static final String INVESTIGATION_QUERY = "SELECT tp.prescribed_id AS patient_presc_id,"
      + " tp.source_test_prescribed_id, tp.pres_date AS prescribed_date, tp.signoff_reverted,"
      + " tc.conducted_date AS conduction_start_date, tc.technician, sc.sample_sno,"
      + " sc.sample_date, st.sample_type, tvr.report_date AS signoff_date, diag.sample_needed,"
      + " diag.conduction_format, diag.test_name, dd.diag_dept_id,"
      + " tod.code_type, tod.item_code AS test_code, stod.item_code AS source_test_code,"
      + " d.doc_first_name AS presc_doc_first_name, d.doc_last_name AS presc_doc_last_name,"
      + " d.doctor_license_number AS presc_doc_license_number,"
      + " CASE WHEN dd.category = 'DEP_LAB' THEN true ELSE false END AS is_lab,"
      + " CASE WHEN dd.category = 'DEP_RAD' THEN true ELSE false END AS is_rad,"
      + " u.doctor_id AS tp_modified_by_doctor_id, u.employee_id AS tp_modified_by_employee_id,"
      + " u.user_first_name AS tp_modified_by_user_first_name,"
      + " u.user_last_name AS tp_modified_by_user_last_name,"
      + " signedoff_doc.doctor_license_number AS tp_modified_by_doc_license_number,"
      + " signedoff_doc.doc_first_name AS tp_modified_by_doc_first_name,"
      + " signedoff_doc.doc_last_name AS tp_modified_by_doc_last_name,"
      + " td.resultlabel_id, trm.result_code, td.report_value AS result_value, td.units,"
      + " td.reference_range, CASE WHEN (td.withinnormal = 'Y') THEN 'Normal'"
      + " WHEN (td.withinnormal = '*') THEN 'Abnormal Low'"
      + " WHEN (td.withinnormal = '#') THEN 'Abnormal High'"
      + " WHEN (td.withinnormal = '**') THEN 'Critical Low'"
      + " WHEN (td.withinnormal = '##') THEN 'Critical High'"
      + " WHEN (td.withinnormal = '***') THEN 'Improbable Low'"
      + " WHEN (td.withinnormal = '###') THEN 'Improbable High' END AS severity,"
      + " td.patient_report_file, td.comments, trm.resultlabel AS result_label_name,"
      + " dmm.method_name, trr.min_normal_value, trr.max_normal_value, trr.min_critical_value,"
      + " trr.max_critical_value, trr.min_improbable_value, trr.max_improbable_value"
      + " FROM tests_prescribed tp"
      + " LEFT JOIN tests_conducted tc ON (tc.prescribed_id = tp.prescribed_id)"
      + " LEFT JOIN sample_collection sc ON (sc.sample_sno = tp.sample_no)"
      + " LEFT JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id)"
      + " LEFT JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id)"
      + " LEFT JOIN diagnostics diag ON (diag.test_id = tp.test_id)"
      + " LEFT JOIN diagnostics_departments dd ON (dd.ddept_id = diag.ddept_id)"
      + " LEFT JOIN patient_registration pr ON (tp.pat_id = pr.patient_id)"
      + " LEFT JOIN test_org_details tod ON (tod.test_id = tp.test_id AND tod.org_id = pr.org_id)"
      + " LEFT JOIN doctors d ON (d.doctor_id = tp.pres_doctor)"
      + " LEFT JOIN u_user u ON (tp.user_name = u.emp_username)"
      + " LEFT JOIN doctors signedoff_doc ON (u.doctor_id = signedoff_doc.doctor_id)"
      + " LEFT JOIN tests_prescribed stp ON (tp.source_test_prescribed_id = stp.prescribed_id)"
      + " LEFT JOIN patient_registration spr ON (stp.pat_id = spr.patient_id)"
      + " LEFT JOIN test_org_details stod ON (stod.test_id = stp.test_id"
      + " AND stod.org_id = spr.org_id)"
      + " LEFT JOIN test_details td ON (td.prescribed_id = tp.prescribed_id)"
      + " LEFT JOIN test_results_master trm ON (trm.resultlabel_id = td.resultlabel_id)"
      + " LEFT JOIN diag_methodology_master dmm ON (td.method_id = dmm.method_id)"
      + " LEFT JOIN test_result_ranges trr ON (trr.resultlabel_id = trm.resultlabel_id)"
      + " WHERE td.amendment_reason = '' AND tp.prescribed_id = :prescId"
      + " ORDER BY td.test_details_id ASC";

  /**
   * Get NTE segment data.
   * 
   * @param prescId the presc id
   * @return list of beans
   */
  public List<BasicDynaBean> getInvestigationData(int prescId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("prescId", prescId);
    return DatabaseHelper.queryToDynaList(INVESTIGATION_QUERY, parameter);
  }

  private static final String MEDICINE_PRESCRIPTION_QUERY = "SELECT pmp.op_medicine_pres_id,"
      + " pmp.frequency, pmp.duration, pmp.strength, pmp.duration_units, pmp.issued_qty,"
      + " pmp.medicine_quantity AS presc_qty, pmp.item_form_id, pmp.cons_uom_id,"
      + " pmp.medicine_remarks,pmp.priority,pmp.refills,"
      + " pmp.item_strength, pp.patient_presc_id, pp.start_datetime AS presc_start_date,"
      + " pp.doc_presc_id, pp.end_datetime AS presc_end_date, ppm.created_at AS prescribed_date,"
      + " d.doc_first_name AS presc_doc_first_name, d.doc_last_name AS presc_doc_last_name,"
      + " d.doctor_license_number AS presc_doc_license_number, sid.medicine_name, sid.issue_units,"
      + " sid.package_uom, cum.consumption_uom, ifm.granular_units, ifm.item_form_name,"
      + " mr.route_name, mr.route_id, sic.item_code AS medicine_ha_code,"
      + " pres_doc.doc_first_name AS ip_presc_doc_first_name,"
      + " pres_doc.doc_last_name AS ip_presc_doc_last_name,"
      + " pres_doc.doctor_license_number AS ip_presc_doc_license_number #DISPENSE_FIELDS#"
      + " FROM patient_medicine_prescriptions pmp"
      + " LEFT JOIN patient_prescription pp ON (pp.patient_presc_id = pmp.op_medicine_pres_id)"
      + " LEFT JOIN patient_prescriptions_main ppm ON (ppm.doc_presc_id = pp.doc_presc_id)"
      + " LEFT JOIN doctor_consultation dc ON (dc.consultation_id = pp.consultation_id)"
      + " LEFT JOIN doctors d ON (dc.doctor_name = d.doctor_id)"
      + " LEFT JOIN store_item_details sid ON (pmp.medicine_id = sid.medicine_id)"
      + " LEFT JOIN generic_name gn ON (pmp.generic_code = gn.generic_code)"
      + " LEFT JOIN consumption_uom_master cum ON (pmp.cons_uom_id = cum.cons_uom_id)"
      + " LEFT JOIN item_form_master ifm ON (pmp.item_form_id = ifm.item_form_id)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id = pmp.route_of_admin)"
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = :centerId)"
      + " LEFT JOIN ha_item_code_type hict ON (hcm.health_authority = hict.health_authority"
      + " AND pmp.medicine_id = hict.medicine_id)"
      + " LEFT JOIN store_item_codes sic ON (sic.code_type = hict.code_type"
      + " AND sic.medicine_id = pmp.medicine_id)"
      + " LEFT JOIN doctors pres_doc ON (pres_doc.doctor_id = pp.doctor_id)";

  private static final String MEDICINE_PRESC_DISPENSE_FIELDS = ",piu.uom_id, ssm.sale_date,"
      + " ssd.sale_unit, u.employee_id AS ph_user_employee_id,"
      + " u.user_first_name AS ph_user_first_name, u.user_last_name AS ph_user_last_name";

  private static final String MEDICINE_PRESC_DISPENSE_TABLES = 
      " LEFT JOIN package_issue_uom piu ON (sid.issue_units = piu.issue_uom"
      + " AND sid.package_uom = piu.package_uom)"
      + " LEFT JOIN store_sales_main ssm ON (ssm.sale_id = pmp.final_sale_id)"
      + " LEFT JOIN store_sales_details ssd ON (ssm.sale_id = ssd.sale_id"
      + " AND pmp.medicine_id = ssd.medicine_id)"
      + " LEFT JOIN u_user u ON (ssm.username = u.emp_username)";

  /**
   * Gets medicine prescriptions.
   * 
   * @param prescIds the presc ids
   * @param isPrescDeleted specifies if the presc is deleted
   * @return list of map
   */
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> getMedicinePrescription(List<Integer> prescIds, int centerId,
      boolean isPrescDeleted, boolean isMedicineDispense) {
    StringBuilder query = new StringBuilder(MEDICINE_PRESCRIPTION_QUERY
        .replace("#DISPENSE_FIELDS#", isMedicineDispense ? MEDICINE_PRESC_DISPENSE_FIELDS : ""));
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("centerId", centerId);
    List<Map<String, Object>> medPrescList = new ArrayList<>();
    if (isPrescDeleted) {
      query = new StringBuilder(MEDICINE_PRESCRIPTION_QUERY
        .replace("#DISPENSE_FIELDS#", isMedicineDispense ? MEDICINE_PRESC_DISPENSE_FIELDS : "")
        .replace("patient_medicine_prescriptions", "patient_medicine_prescriptions_audit")
        .replace("patient_prescription pp", "patient_prescription_audit pp"));
      query.append(" WHERE pmp.op_medicine_pres_id = :prescId");
      query.append(" ORDER BY pmp.revision_id DESC LIMIT 1");
      for (int prescId : prescIds) {
        parameter.addValue("prescId", prescId);
        medPrescList.add(DatabaseHelper.queryToDynaBean(query.toString(), parameter).getMap());
      }
    } else {
      if (isMedicineDispense) {
        query.append(MEDICINE_PRESC_DISPENSE_TABLES);
      }
      query.append(" WHERE pmp.op_medicine_pres_id IN (:prescIds)");
      query.append(" ORDER BY pmp.op_medicine_pres_id");
      parameter.addValue("prescIds", prescIds);
      medPrescList = ConversionUtils
          .copyListDynaBeansToMap(DatabaseHelper.queryToDynaList(query.toString(), parameter));
    }
    return medPrescList;
  }

  private static final String VITAL_READING_QUERY = "SELECT vr.vital_reading_id, vr.param_id,"
      + " vr.param_value, vr.param_remarks, vr.mod_time AS vital_param_mod_time,"
      + " vri.date_time AS vital_date_time, vpm.param_label, vpm.param_container,"
      + " vpm.param_uom, vrrm.min_patient_age, vrrm.max_patient_age, vrrm.age_unit,"
      + " vrrm.min_normal_value, vrrm.max_normal_value, vrrm.min_critical_value,"
      + " vrrm.max_critical_value, vrrm.min_improbable_value, vrrm.max_improbable_value,"
      + " vrrm.reference_range_txt FROM vital_reading vr"
      + " JOIN LATERAL (SELECT vital_reading_id, patient_id, date_time FROM visit_vitals"
      + " WHERE patient_id = :visitId ORDER BY vital_reading_id DESC LIMIT 1) AS vri"
      + " ON (vri.vital_reading_id = vr.vital_reading_id)"
      + " LEFT JOIN patient_details pd ON (pd.visit_id = vri.patient_id)"
      + " LEFT JOIN vital_parameter_master vpm ON (vpm.param_id = vr.param_id)"
      + " LEFT JOIN vital_reference_range_master vrrm"
      + " ON (pd.patient_gender = vrrm.patient_gender AND vrrm.param_id = vr.param_id)"
      + " WHERE (vr.param_value is not null AND vr.param_value != '')";

  /**
   * Gets vital readings.
   * 
   * @param visitId the visit id
   * @return list of map
   */
  public List<BasicDynaBean> getVitalReadingData(String visitId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("visitId", visitId);
    return DatabaseHelper.queryToDynaList(VITAL_READING_QUERY, parameter);
  }

  private static final String VACCINATION_ADMINISTERED_QUERY = "SELECT pv.pat_vacc_id,"
      + " pv.vaccine_dose_id, pv.vaccination_datetime, pv.vacc_by, pv.vacc_doctor_id,"
      + " pv.manufacturer, pv.expiry_date, pv.batch, pv.mod_time AS vacc_last_updated_date,"
      + " pv.medicine_id, pv.vaccine_category_id, pv.medicine_quantity, pv.cons_uom_id,"
      + " pv.route_of_admin, pv.site_id, pv.remarks AS vaccine_remarks, pv.mod_user,"
      + " vdm.vaccine_id, vdm.recommended_age, vdm.age_units, vm.vaccine_name,"
      + " d.doctor_name AS vacc_doctor_name, d.doc_first_name AS vacc_doc_first_name,"
      + " d.doc_middle_name AS vacc_doc_middle_name, d.doc_last_name AS vacc_doc_last_name,"
      + " d.doctor_license_number AS vacc_doc_license_number, ii.site_name, sid.medicine_name,"
      + " sid.item_form_id, cum.consumption_uom, vcm.vaccine_category_name, mr.route_name,"
      + " sic.item_code AS medicine_ha_code, uu.user_first_name, uu.user_last_name,"
      + " uu.user_middle_name, uu.employee_id"
      + " FROM patient_vaccination pv"
      + " LEFT JOIN vaccine_dose_master vdm ON (pv.vaccine_dose_id = vdm.vaccine_dose_id)"
      + " LEFT JOIN vaccine_master vm ON (vdm.vaccine_id = vm.vaccine_id)"
      + " LEFT JOIN doctors d ON (pv.vacc_doctor_id = d.doctor_id)"
      + " LEFT JOIN iv_infusionsites ii ON (pv.site_id = ii.id)"
      + " LEFT JOIN store_item_details sid ON (pv.medicine_id = sid.medicine_id)"
      + " LEFT JOIN consumption_uom_master cum ON (pv.cons_uom_id = cum.cons_uom_id)"
      + " LEFT JOIN vaccine_category_master vcm ON (vcm.vaccine_category_id=pv.vaccine_category_id)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id = pv.route_of_admin)"
      + " LEFT JOIN hospital_center_master hcm ON (hcm.center_id = :centerId)"
      + " LEFT JOIN ha_item_code_type hict ON (hcm.health_authority = hict.health_authority"
      + " AND pv.medicine_id = hict.medicine_id)"
      + " LEFT JOIN store_item_codes sic ON (sic.code_type = hict.code_type"
      + " AND sic.medicine_id = pv.medicine_id)"
      + " LEFT JOIN adverse_reaction_for_vaccination arfv"
      + " ON (arfv.adverse_reaction_id = pv.adverse_reaction_id)"
      + " LEFT JOIN u_user uu ON (uu.emp_username = pv.mod_user)"
      + " WHERE pv.pat_vacc_id IN (:itemIds)";

  /**
   * Get vaccination info.
   * 
   * @param itemIds the list of vaccination administered ids
   * @return list of beans
   */
  public List<BasicDynaBean> getVaccinationAdministeredData(List<Integer> itemIds, int centerId) {
    MapSqlParameterSource parameter = new MapSqlParameterSource();
    parameter.addValue("itemIds", itemIds);
    parameter.addValue("centerId", centerId);
    return DatabaseHelper.queryToDynaList(VACCINATION_ADMINISTERED_QUERY, parameter);
  }
  

  private static final String PCG_UPDATED_IN_PATIENT_DETIALS_QUERY = "select mr_no, "
      + " operation from patient_details_audit_log "
      + " where (operation like 'INSERT' or operation like 'UPDATE') "
      + " and field_name like 'patient_group' and new_value not like '0' and "
      + " mod_time >= now()-interval'3 seconds' and "
      + " mr_no = ? ";

  /**
   * Get PCG update info.
   * 
   * @param mrNo mr_no of the current patient
   * @return list of beans
   */
  public List<BasicDynaBean> isPatientAddedToPCG(String mrNo) {
    return DatabaseHelper.queryToDynaList(PCG_UPDATED_IN_PATIENT_DETIALS_QUERY, mrNo);
  }
}
