package com.insta.hms.core.patient.registration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class RegistrationPreferencesRepository.
 */
@Repository
public class RegistrationPreferencesRepository extends GenericRepository {

  /**
   * Instantiates a new registration preferences repository.
   */
  public RegistrationPreferencesRepository() {
    super("registration_preferences");
  }

  /** The Constant GET_PREFS. */
  private static final String GET_PREFS = 
      " SELECT custom_field1_label, custom_field2_label, custom_field3_label, "
      + " custom_field4_label,custom_field5_label, custom_field6_label, custom_field7_label,"
      + " custom_field8_label,custom_field9_label,"
      + " custom_field10_label,custom_field11_label,"
      + " custom_field12_label,"
      + " custom_field13_label,"
      + " custom_field14_label,custom_field15_label,custom_field16_label,"
      + " custom_field17_label,custom_field18_label,custom_field19_label,"
      + " custom_field1_validate,custom_field2_validate,"
      + " custom_field3_validate,custom_field4_validate,"
      + " custom_field5_validate,custom_field6_validate,"
      + " custom_field7_validate,custom_field8_validate,"
      + " custom_field9_validate,custom_field10_validate,"
      + " custom_field11_validate,custom_field12_validate,"
      + " custom_field13_validate,custom_field14_validate,"
      + " custom_field15_validate,custom_field16_validate,"
      + " custom_field17_validate,custom_field18_validate,custom_field19_validate,"
      + " custom_field1_show, custom_field2_show, custom_field3_show,"
      + " custom_field4_show, custom_field5_show, "
      + " custom_field6_show, custom_field7_show, custom_field8_show,"
      + " custom_field9_show, custom_field10_show, "
      + " custom_field11_show, custom_field12_show,"
      + " custom_field13_show,custom_field14_show,custom_field15_show,custom_field16_show,"
      + " custom_field17_show,custom_field18_show,custom_field19_show,"
      + " visit_custom_field1_name, visit_custom_field2_name, visit_custom_field3_name,"
      + " visit_custom_field4_name,visit_custom_field5_name,visit_custom_field6_name,"
      + " visit_custom_field7_name,visit_custom_field8_name,visit_custom_field9_name, "
      + " visit_custom_field1_show, visit_custom_field2_show, visit_custom_field3_show,"
      + " visit_custom_field4_show,visit_custom_field5_show,visit_custom_field6_show,"
      + " visit_custom_field7_show,visit_custom_field8_show,visit_custom_field9_show,"
      + " visit_custom_field1_validate, visit_custom_field2_validate,"
      + " visit_custom_field3_validate,"
      + " visit_custom_field4_validate,visit_custom_field5_validate,visit_custom_field6_validate,"
      + " visit_custom_field7_validate,visit_custom_field8_validate,visit_custom_field9_validate,"
      + " patient_category_field_label, category_expiry_field_label,"
      + " area_field_validate, nextofkin_field_validate,"
      + " address_field_validate, complaint_field_validate, old_reg_field_label, "
      + " case_file_settings,hosp_uses_units,"
      + " dept_units_settings,referredby_field_validate,"
      + " admitting_doctor_mandatory, reg_validity_period,"
      + " op_default_selection,mobile_phone_pattern,"
      + " ip_default_selection,"
      + " custom_list1_name, custom_list2_name, custom_list3_name,"
      + " custom_list4_name,custom_list5_name,custom_list6_name,"
      + " custom_list7_name,custom_list8_name,custom_list9_name, "
      + " custom_list1_validate, custom_list2_validate,"
      + " custom_list3_validate,custom_list4_validate,"
      + " custom_list5_validate,custom_list6_validate,custom_list7_validate,"
      + " custom_list8_validate,custom_list9_validate,"
      + " custom_list1_show, custom_list2_show, custom_list3_show,"
      + " custom_list3_show,custom_list4_show,custom_list5_show,custom_list6_show,"
      + " custom_list7_show,custom_list8_show,custom_list9_show,"
      + " visit_custom_list1_name, visit_custom_list2_name,"
      + " visit_custom_list1_show, visit_custom_list2_show,"
      + " visit_custom_list1_validate, visit_custom_list2_validate,"
      + " government_identifier_label, government_identifier_type_label,"
      + " outside_default_selection, encntr_start_and_end_reqd,"
      + " encntr_type_reqd, patientphone_field_validate,allow_age_entry,"
      + " passport_no, passport_validity, passport_issue_country, visa_validity, nationality,"
      + " passport_no_validate, passport_validity_validate,"
      + " passport_issue_country_validate, visa_validity_validate,"
      + " nationality_validate, nationality_show,"
      + " passport_no_show, passport_validity_show,"
      + " passport_issue_country_show, visa_validity_show,"
      + " family_id, family_id_validate, family_id_show, no_reg_charge_sources, "
      + " doc_eandm_codification_required,"
      + " visit_type_dependence,allow_multiple_active_visits,"
      + " prior_auth_required, member_id_label,"
      + " member_id_valid_from_label, member_id_valid_to_label,"
      + " copy_paste_option, default_followup_eandm_code,"
      + " default_op_encounter_start_type, default_visit_details_across_center,"
      + " default_op_encounter_end_type, default_ip_encounter_start_type,"
      + " default_ip_encounter_end_type,name_parts,"
      + " name_local_lang_required, referal_for_life, validate_email_id,"
      + " patient_reg_basis, allow_drg_perdiem, issue_to_mrd_on_registration,"
      + " followup_across_centers, unidentified_patient_first_name,"
      + " unidentified_patient_last_name, emergency_patient_department_id,"
      + " enable_district, show_referrral_doctor_filter, allow_auto_entry_of_area,"
      + " patient_photo_mandatory, patient_outstanding_control, plan_code_search,"
      + " last_name_required, marital_status_required,"
      + " religion_required, close_previous_active_visit"
      + " FROM registration_preferences ";

  /**
   * Gets the registration preferences from DB.
   *
   * @return the registration preferences from DB
   */
  public BasicDynaBean getRegistrationPreferencesFromDB() {
    return DatabaseHelper.queryToDynaBean(GET_PREFS);
  }

}
