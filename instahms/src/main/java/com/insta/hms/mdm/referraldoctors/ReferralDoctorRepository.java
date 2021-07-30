package com.insta.hms.mdm.referraldoctors;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

@Repository
public class ReferralDoctorRepository extends MasterRepository<String> {

  public ReferralDoctorRepository() {
    super("referral", "referal_no");
  }

  private static final String REFERRAL_DOCTOR_LOOKUP_QUERY =
      "SELECT * "
          + " FROM (SELECT d.doctor_id AS referal_no, d.doctor_name AS referal_name, "
          + " d.doctor_mobile AS referal_mobileno, 'D' as ref_type,"
          + " d.doctor_license_number  AS clinician_id, dcm.center_id, "
          + " null as area_id, ctm.city_id, dm.district_id, sm.state_id, "
          + " null as area_name, ctm.city_name, dm.district_name, sm.state_name " 
          + " FROM doctors d "
          + " JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id) "
          + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id = dcm.center_id) "
          + " LEFT JOIN city ctm ON (ctm.city_id = hcm.city_id) "
          + " LEFT JOIN state_master sm ON (sm.state_id = ctm.state_id "
          + "                                AND sm.state_id = hcm.state_id) "
          + " LEFT JOIN district_master dm ON (dm.district_id = ctm.district_id "
          + "                                AND dm.state_id = sm.state_id ) "
          + " WHERE d.status = 'A' and dcm.status = 'A' "
          + " UNION "
          + " SELECT r.referal_no, r.referal_name, r.referal_mobileno, 'O' AS ref_type,"
          + " r.clinician_id, rcm.center_id, am.area_id, ctm.city_id, dm.district_id, sm.state_id,"
          + " am.area_name, ctm.city_name, dm.district_name, sm.state_name "
          + " FROM referral r "
          + " JOIN referral_center_master rcm on (r.referal_no = rcm.referal_no) "
          + " LEFT JOIN area_master am ON (am.area_id = r.referal_doctor_area_id) "
          + " LEFT JOIN city ctm ON (ctm.city_id = r.referal_doctor_city_id "
          + "                         AND ctm.city_id = am.city_id) "
          + " LEFT JOIN state_master sm ON (sm.state_id = ctm.state_id) "
          + " LEFT JOIN district_master dm ON (dm.district_id = ctm.district_id "
          + "                         AND dm.state_id = sm.state_id ) "
          + " WHERE r.status ='A' and rcm.status = 'A' "
          + " ORDER BY referal_name) as foo";

  @Override
  public String getLookupQuery() {
    return REFERRAL_DOCTOR_LOOKUP_QUERY;
  }

  private static final String referalDoctorQuery =
      "SELECT referrer_phone from all_referrers_view WHERE id = ?";

  public BasicDynaBean getReferralDocMobile(String referalDoctorId) {
    return DatabaseHelper.queryToDynaBean(referalDoctorQuery, referalDoctorId);
  }

  public BasicDynaBean getReferralForVisit(String referalDoctorId) {
    return DatabaseHelper.queryToDynaBean(
      REFERRAL_DOCTOR_LOOKUP_QUERY + " WHERE referal_no = ?", referalDoctorId);
  }
}
