package com.insta.instaapi.customer.patientclinicaldata;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PatientClinicalDataDAO {
  static Logger logger = LoggerFactory
      .getLogger(com.insta.instaapi.customer.patientclinicaldata.PatientClinicalDataDAO.class);

  private static final String LAB = "SELECT * FROM (SELECT pr.patient_id, pd.mr_no,"
      + " trm.resultlabel,"
      + " trm.resultlabel_id, trm.units, clv.test_value, to_char(clv.value_date AT TIME ZONE ("
      + " SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS value_date,"
      + " to_char(clv.mod_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE"
      + " 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time, clv.values_id,"
      + " coalesce((SELECT min_normal_value FROM test_result_ranges trr"
      + " WHERE resultlabel_id = trm.resultlabel_id AND ((range_for_all = 'N'  AND "
      + "  ((min_patient_age IS NULL OR"
      + " (min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer "
      + " <= ( SELECT (CASE WHEN age_unit = 'Y' THEN "
      + " get_patient_age(dateofbirth, expected_dob)*365.25 "
      + " ELSE (current_date - COALESCE(dateofbirth, expected_dob)) END)::integer))"
      + " AND (max_patient_age IS NULL OR (SELECT (CASE WHEN age_unit = 'Y' THEN"
      + " get_patient_age(dateofbirth, expected_dob)*365.25"
      + " ELSE (current_date - COALESCE(dateofbirth, expected_dob)) END)::integer)"
      + " <= (max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer))"
      + " AND (trr.patient_gender = pd.patient_gender OR trr.patient_gender = 'N')))"
      + " ORDER BY priority LIMIT 1 )," + " (select min_normal_value from test_result_ranges trr "
      + " WHERE resultlabel_id=trm.resultlabel_id and range_for_all='Y' order by priority limit 1))"
      + " as min_normal_value, coalesce((SELECT max_normal_value FROM test_result_ranges trr where"
      + " resultlabel_id = trm.resultlabel_id AND ((range_for_all = 'N' AND"
      + " ((min_patient_age IS NULL OR "
      + " (min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer"
      + " <= (SELECT (CASE WHEN age_unit = 'Y' THEN"
      + " get_patient_age(dateofbirth, expected_dob)*365.25 ELSE"
      + " (current_date - COALESCE(dateofbirth, expected_dob)) END)::integer))"
      + " AND (max_patient_age IS NULL OR (SELECT (CASE WHEN age_unit = 'Y' THEN"
      + " get_patient_age(dateofbirth, expected_dob)*365.25 ELSE"
      + " (current_date - COALESCE(dateofbirth, expected_dob)) END)::integer)"
      + " <= (max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer))"
      + " AND (trr.patient_gender = pd.patient_gender OR trr.patient_gender = 'N')))"
      + " ORDER BY priority LIMIT 1 ), (select max_normal_value from test_result_ranges trr"
      + " where resultlabel_id=trm.resultlabel_id and range_for_all='Y' order by priority limit 1))"
      + " as max_normal_value FROM clinical_lab_recorded cle JOIN patient_details pd"
      + " ON (pd.mr_no = cle.mrno) JOIN clinical_lab_values clv"
      + " ON (cle.clinical_lab_recorded_id = clv.clinical_lab_recorded_id)"
      + " JOIN clinical_lab_result clr ON (clr.resultlabel_id = clv.resultlabel_id)"
      + " JOIN test_results_master trm ON (trm.resultlabel_id = clr.resultlabel_id)"
      + " JOIN patient_registration pr ON (pr.patient_id = coalesce(pd.visit_id,previous_visit_id))"
      + " WHERE clr.status = 'A' and clv.mod_time BETWEEN ? AND ? ORDER BY pr.patient_id ) as lab";

  private static final String MEDICATIONS = "SELECT * FROM ( SELECT dc.patient_id, dc.mr_no,"
      + " pmp.op_medicine_pres_id, dc.consultation_id, sid.medicine_name as name,"
      + " pmp.medicine_remarks, pmp.frequency as dosage, pmp.duration_units,pmp.duration,"
      + " mr.route_name,to_char(pp.prescribed_date AT TIME ZONE "
      + "  (SELECT current_setting('TIMEZONE')) AT TIME ZONE 'UTC',"
      + "  'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS prescribed_date,"
      + " to_char(pmp.mod_time AT TIME ZONE (SELECT current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time"
      + " FROM doctor_consultation dc"
      + " JOIN patient_prescription pp USING (consultation_id)"
      + " JOIN patient_medicine_prescriptions pmp ON(pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " JOIN store_item_details sid ON(pmp.medicine_id = sid.medicine_id)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pmp.route_of_admin)"
      + " WHERE pmp.mod_time BETWEEN ? AND ? ORDER BY dc.patient_id ) as MEDICATIONS";

  private static final String ALLERGIES = "SELECT * FROM ( SELECT psd.patient_id, psd.mr_no, "
      + " pa.allergy_id, "
      + " psd.section_detail_id, COALESCE(am.allergen_description,gn.generic_name) as allergy, "
      + " pa.reaction, pa.status, pa.onset_date, "
      + " atm.allergy_type_code as allergy_type, "
      + " to_char(psd.mod_time AT TIME ZONE (SELECT current_setting('TIMEZONE')) "
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time "
      + " FROM patient_allergies pa "
      + " LEFT JOIN allergy_type_master atm "
      + " ON atm.allergy_type_id = pa.allergy_type_id "
      + " LEFT JOIN allergen_master am "
      + " ON am.allergen_code_id = pa.allergen_code_id "
      + " LEFT JOIN generic_name gn ON (gn.allergen_code_id = pa.allergen_code_id) "
      + " JOIN patient_section_details psd ON (pa.section_detail_id=psd.section_detail_id) "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + " WHERE  section_id=-2  AND psd.mod_time BETWEEN ? AND ? ORDER BY patient_id ) "
      + " as ALLERGIES ";

  private static final String APPOINTMENTS = "SELECT * FROM ( SELECT sp.visit_id, sp.mr_no,"
      + " sp.appointment_id,"
      + " sp.res_sch_id, sp.prim_res_id as res_sch_name, sp.center_id, sp.remarks,"
      + " to_char(sp.appointment_time AT TIME ZONE (SELECT  current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as appointment_time,"
      + " to_char(sp.booked_time AT TIME ZONE (SELECT  current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as booked_time, sp.duration,"
      + " sp.appointment_status FROM scheduler_appointments sp"
      + " JOIN scheduler_master sm using(res_sch_id)"
      + " JOIN scheduler_resource_types srt "
      + "  ON (srt.primary_resource=true AND srt.category = sm.res_sch_category)"
      + " JOIN scheduler_appointment_items  sai"
      + "  ON (sai.appointment_id = sp.appointment_id AND sai.resource_type = srt.resource_type)"
      + " WHERE sai.mod_time BETWEEN ? AND ? ORDER BY sp.visit_id ) as APPOINTMENTS";

  private static final String PATIENT_DEMOGRAPHICS = "SELECT * FROM ( SELECT "
      + "pd.mr_no, CONCAT_WS(' ', sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + " as Patient_name, pd.patient_name as first_name, pd.middle_name, pd.last_name,"
      + " to_char(pd.dateofbirth,'YYYY-MM-DD') AS dateofbirth,"
      + " CASE when pd.patient_gender='M' then 'Male' WHEN pd.patient_gender='F' then 'Female'"
      + " END AS gender, pd.patient_phone, pd.email_id, pd.mobile_password,"
      + " to_char(pdm.mod_time AT TIME ZONE (SELECT current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time"
      + " FROM patient_details pd"
      + " join salutation_master sm on(pd.salutation=sm.salutation_id)"
      + " JOIN patient_demographics_mod pdm using(mr_no)"
      + " WHERE pdm.mod_time BETWEEN ? AND ? ORDER BY pd.mr_no ) as patient_demographics";

  private static final String PATIENT_DETAILS = "SELECT * FROM ( SELECT pd.mr_no, "
      + "pd.patient_city,"
      + " CONCAT_WS(' ', sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)"
      + " as patient_full_name, pd.patient_name, pd.last_name, "
      + " to_char(pd.dateofbirth,'YYYY-MM-DD') AS dateofbirth, pd.patient_gender, pd.country,"
      + " pd.patient_phone, pd.patient_address, pd.patient_state, pd.salutation,"
      + " pd.patient_phone, pd.email_id, pd.mobile_password, pd.patient_category_id,"
      + " to_char(pd.expected_dob,'YYYY-MM-DD') AS expected_dob, pd.email_id,"
      + " to_char(pdm.mod_time AT TIME ZONE (SELECT  current_setting('TIMEZONE'))"
      + " AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time"
      + " FROM patient_details pd join salutation_master sm on(pd.salutation=sm.salutation_id)"
      + " JOIN patient_demographics_mod pdm using(mr_no)"
      + " WHERE pdm.mod_time BETWEEN ? AND ? ORDER BY pd.mr_no ) as PATIENT_DETAILS";

  private static final String MEDICAL_PROFESSIONAL_DEMOGRAPHICS = "SELECT"
      + " u.emp_username as username, ur.role_name as application_role, u.temp_username as name,"
      + " u.emp_status as status, u.email_id, u.doctor_id as user_doc_id, u.mobile_no,"
      + " u.center_id, to_char(u.created_timestamp AT TIME ZONE ("
      + " SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', "
      + " 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS created_timestamp,"
      + " to_char(coalesce(u.mod_date, u.created_timestamp) AT TIME ZONE ("
      + " SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC',"
      + " 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS  mod_datetime, "
      + " (select textcat_commacat(hrm.hosp_role_name) FROM u_user uu"
      + " left join user_hosp_role_master uhrm on (uu.emp_username=uhrm.u_user"
      + " and uu.emp_username=u.emp_username)"
      + " left join hospital_roles_master hrm on(uhrm.hosp_role_id=hrm.hosp_role_id) )"
      + " as hospital_roles from u_user u "
      + " JOIN u_role ur on(u.role_id=ur.role_id) "
      + " WHERE (u.mod_date BETWEEN ? AND ? ) OR (u.created_timestamp BETWEEN ? AND ?)"
      + " ORDER BY username";

  /**
   * Get Query Results based on section.
   * @param section           Section
   * @param sessionParameters Session Parameters
   * @param fromTime          From Date
   * @param toTime            To Date
   * @return                  List of BasicDynaBean 
   * @throws SQLException     Query related exception
   */
  public static List getQueryResult(String section, Map sessionParameters, Object fromTime,
      Object toTime) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = com.insta.instaapi.common.DbUtil
          .getConnection((String) sessionParameters.get("hospital_name"));
      final String[] sections = new String[] { "LAB", "MEDICATIONS", "APPOINTMENTS",
          "PATIENT_DEMOGRAPHICS" };
      String queryString = getQueryString(section);
      if (Arrays.asList(sections).contains(section)
          && (Boolean) sessionParameters.get("patient_login")) {
        queryString = queryString + " where mr_no = '"
            + (String) sessionParameters.get("customer_user_id") + "'";
      }
      pstmt = con.prepareStatement(queryString);
      pstmt.setObject(1, fromTime);
      pstmt.setObject(2, toTime);
      if (section.equals("MEDICAL_PROFESSIONAL_DEMOGRAPHICS")) {
        pstmt.setObject(3, fromTime);
        pstmt.setObject(4, toTime);
      }

      return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));

    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /**
   * Get Query String based on Section.
   * @param section    Section
   * @return           Query String
   */
  private static String getQueryString(String section) {
    if (section.equals("LAB")) {
      return LAB;
    } else if (section.equals("MEDICATIONS")) {
      return MEDICATIONS;
    } else if (section.equals("ALLERGIES")) {
      return ALLERGIES;
    } else if (section.equals("APPOINTMENTS")) {
      return APPOINTMENTS;
    } else if (section.equals("PATIENT_DEMOGRAPHICS")) {
      return PATIENT_DEMOGRAPHICS;
    } else if (section.equals("MEDICAL_PROFESSIONAL_DEMOGRAPHICS")) {
      return MEDICAL_PROFESSIONAL_DEMOGRAPHICS;
    } else if (section.equals("PATIENT_DETAILS")) {
      return PATIENT_DETAILS;
    }
    return "";
  }

}
