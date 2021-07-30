package com.insta.hms.api.repositories;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class ApiReferralDoctorRepository extends MasterRepository<String> {

  public ApiReferralDoctorRepository() {
    super("referral", "referal_no");
  }

  private static final String REFERRAL_DOCTOR_LOOKUP_QUERY =
      "SELECT *  "
          + "FROM (SELECT d.doctor_id AS id,d.doctor_name AS name,d.doctor_mobile AS mobile, "
          + "d.doctor_address AS address , null AS area_id, ctm.city_id,d.res_phone AS phone, "
          + "d.doctor_mail_id AS email , d.doctor_license_number AS license_no,"
          + "d.created_timestamp AS created_at, d.updated_timestamp AS modified_at,"
          + "STRING_AGG (dcm.center_id::varchar, ',' ORDER BY dcm.center_id ASC) center_ids,"
          + "d.status AS status, 'Doctor' AS referral_source "
          + "FROM doctors d LEFT JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "
          + "LEFT JOIN hospital_center_master hcm ON(hcm.center_id = dcm.center_id) "
          + "LEFT JOIN city ctm ON (ctm.city_id = hcm.city_id)  "
          + "LEFT JOIN state_master sm ON "
          + "(sm.state_id = ctm.state_id AND sm.state_id = hcm.state_id) "
          + "LEFT JOIN district_master dm ON "
          + "(dm.district_id = ctm.district_id AND dm.state_id = sm.state_id ) "
          + "GROUP BY d.doctor_id,"
          + "ctm.city_id,area_id UNION "
          + "SELECT r.referal_no AS id, r.referal_name AS name , r.referal_mobileno AS mobile, "
          + "r.referal_doctor_address AS address, am.area_id, ctm.city_id, "
          + "r.referal_doctor_phone AS phone,r.referal_doctor_email As email,"
          + "r.clinician_id AS license_no, r.created_timestamp AS created_at,"
          + "r.updated_timestamp AS modified_at,"
          + "STRING_AGG (rcm.center_id::varchar, ',' ORDER BY rcm.center_id ASC) center_ids,"
          + "r.status AS status, 'Referral' AS referral_source "
          + "FROM referral r LEFT JOIN referral_center_master rcm "
          + "on (r.referal_no = rcm.referal_no) "
          + "LEFT JOIN area_master am ON (am.area_id = r.referal_doctor_area_id) "
          + "LEFT JOIN city ctm ON "
          + "(ctm.city_id = r.referal_doctor_city_id AND ctm.city_id = am.city_id) "
          + "LEFT JOIN state_master sm ON (sm.state_id = ctm.state_id) "
          + "LEFT JOIN district_master dm "
          + "ON (dm.district_id = ctm.district_id AND dm.state_id = sm.state_id ) "
          + "GROUP BY r.referal_no, ctm.city_id,am.area_id ORDER BY created_at) as foo";

  @Override
  public String getLookupQuery() {
    return REFERRAL_DOCTOR_LOOKUP_QUERY;
  }

  public BasicDynaBean getReferralById(String referalDoctorId) {
    return DatabaseHelper.queryToDynaBean(
        REFERRAL_DOCTOR_LOOKUP_QUERY + " WHERE id = ?", referalDoctorId);
  }
}
