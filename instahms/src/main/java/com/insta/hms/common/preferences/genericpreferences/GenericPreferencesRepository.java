package com.insta.hms.common.preferences.genericpreferences;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.preferences.PreferencesRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class GenericPreferencesRepository.
 */
@Repository("genericPreferencesRepository")
class GenericPreferencesRepository extends PreferencesRepository {

  /**
   * This constructor is not public because it should used from only. GenericPreferencesServices
   * classes
   */
  GenericPreferencesRepository() {
    super("generic_preferences");
  }

  /** The get erx center header details. */
  private static String GET_ERX_CENTER_HEADER_DETAILS = " SELECT pbmp.erx_file_name, "
      + " pbmp.pbm_presc_id, pbmp.erx_consultation_id, pbmp.erx_presc_id, hcm.center_id, "
      + " hcm.center_name, "
      + " COALESCE (hcm.hospital_center_service_reg_no,gp.hospital_service_regn_no) AS provider_id,"
      + " (SELECT COALESCE (htpa.tpa_code, '@'||tpam.tpa_name) " + " FROM tpa_master tpam "
      + " LEFT JOIN ha_tpa_code htpa on (tpam.tpa_id = htpa.tpa_id AND health_authority = ?)"
      + " WHERE tpam.tpa_id = ?) "
      + " AS receiver_id, " + " (SELECT COALESCE (insurance_co_code, '@'||insurance_co_name) "
      + " FROM insurance_company_master icm "
      + "   LEFT JOIN ha_ins_company_code hic ON"
      + "     (hic.insurance_co_id = icm.insurance_co_id AND health_authority = ?) "
      + " WHERE icm.insurance_co_id = ?) "
      + " AS payer_id,"
      + " to_char(erx_request_date::timestamp, 'dd/MM/yyyy hh24:mi') AS transaction_date, "
      + " 1 as erx_record_count, "
      + " (SELECT doctor_license_number FROM doctors WHERE doctor_id = ?) AS doctor_license_number,"
      + " (SELECT doctor_name FROM doctors WHERE doctor_id = ?) AS doctor_name "
      + " FROM generic_preferences gp, pbm_prescription pbmp "
      + " LEFT JOIN hospital_center_master hcm ON "
      + "  (hcm.center_id = pbmp.erx_center_id AND hcm.center_id != 0) "
      + " WHERE pbm_presc_id = ? ";

  /**
   * Gets the e rx header fields.
   *
   * @param pbmPrescId
   *          the pbm presc id
   * @param doctorId
   *          the doctor id
   * @param tpaId
   *          the tpa id
   * @param insuCompId
   *          the insu comp id
   * @param healthAuthority
   *          the health authority
   * @return the e rx header fields
   */
  public BasicDynaBean getERxHeaderFields(int pbmPrescId, String doctorId, String tpaId,
      String insuCompId, String healthAuthority) {
    return DatabaseHelper.queryToDynaBean(GET_ERX_CENTER_HEADER_DETAILS, new Object[] {
        healthAuthority, tpaId, healthAuthority, insuCompId, doctorId, doctorId, pbmPrescId });
  }

}
