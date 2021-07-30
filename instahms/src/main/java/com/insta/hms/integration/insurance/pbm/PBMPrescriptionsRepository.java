package com.insta.hms.integration.insurance.pbm;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PBMPrescriptionsRepository.
 */
@Repository
public class PBMPrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new PBM prescriptions repository.
   */
  public PBMPrescriptionsRepository() {
    super("pbm_prescription");
  }

  /** The Constant GET_CONS_ERX_DETAILS. */
  public static final String GET_CONS_ERX_DETAILS =

      "SELECT pr.mr_no, pr.patient_id, pp.pbm_presc_id, pp.erx_consultation_id, pp.erx_presc_id,  "
          + " pp.erx_request_type, pp.erx_request_date, pp.erx_center_id, pp.erx_reference_no,  "
          + "   st.is_selfpay_sponsor, "
          + "   dc.doctor_name, coalesce(pip.plan_id, pr.plan_id, 0) AS plan_id, "
          + " pr.primary_sponsor_id, " //
          + "   pr.primary_insurance_co, " + "       CASE "
          + "     WHEN pd.government_identifier IS NULL "
          + "     OR pd.government_identifier = '' THEN COALESCE(gim.identifier_type, '') "
          + "     ELSE pd.government_identifier " + "       END AS emirates_id_number, "
          + "       ppd.member_id, " + "       ppd.policy_number, " + "       pr.encounter_type, "
          + "       etc.encounter_type_desc, "
          + "       to_char(coalesce(pd.dateofbirth, expected_dob), 'dd/MM/yyyy') AS dob, "
          + "       pd.email_id " + "FROM pbm_prescription pp "
          + "JOIN doctor_consultation dc ON (dc.consultation_id = pp.erx_consultation_id) "
          + "JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
          + "JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
          + "LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
          + "LEFT JOIN patient_insurance_plans pip ON  "
          + " (pr.patient_id = pip.patient_id AND pip.priority=1) "
          + "LEFT JOIN patient_policy_details ppd ON "
          + " (ppd.patient_policy_id = pip.patient_policy_id) "
          + "LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
          + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
          + "LEFT JOIN sponsor_type st ON (st.sponsor_type_id = tm.sponsor_type_id) "
          + "WHERE pp.pbm_presc_id = ? ";


  public static final String GET_ERX_DETAILS_FOR_VISIT_ID =
      "SELECT pr.mr_no, pr.patient_id, pp.pbm_presc_id, pp.erx_visit_id, pp.erx_presc_id,  "
          + " pp.erx_request_type, pp.erx_request_date, pp.erx_center_id, pp.erx_reference_no,  "
          + "   st.is_selfpay_sponsor, "
          + "   pr.doctor as doctor_name, coalesce(pip.plan_id, pr.plan_id, 0) AS plan_id, "
          + " pr.primary_sponsor_id, " //
          + "   pr.primary_insurance_co, " + "       CASE "
          + "     WHEN pd.government_identifier IS NULL "
          + "     OR pd.government_identifier = '' THEN COALESCE(gim.identifier_type, '') "
          + "     ELSE pd.government_identifier " + "       END AS emirates_id_number, "
          + "       ppd.member_id, " + "       ppd.policy_number, " + "       pr.encounter_type, "
          + "       etc.encounter_type_desc, "
          + "       to_char(coalesce(pd.dateofbirth, expected_dob), 'dd/MM/yyyy') AS dob, "
          + "pd.email_id FROM pbm_prescription pp "
          + "JOIN patient_registration pr ON (pr.patient_id = pp.erx_visit_id) "
          + "JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
          + "LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
          + "LEFT JOIN patient_insurance_plans pip ON  "
          + " (pr.patient_id = pip.patient_id AND pip.priority=1) "
          + "LEFT JOIN patient_policy_details ppd ON "
          + " (ppd.patient_policy_id = pip.patient_policy_id) "
          + "LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type) "
          + "LEFT JOIN tpa_master tm ON (tm.tpa_id = pr.primary_sponsor_id) "
          + "LEFT JOIN sponsor_type st ON (st.sponsor_type_id = tm.sponsor_type_id) "
          + "WHERE pp.pbm_presc_id = ? ";

  /**
   * Gets the cons erx details.
   *
   * @param pbmPrescId the pbm presc id
   * @return the cons erx details
   */
  public BasicDynaBean getConsErxDetails(int pbmPrescId) {

    String erxVisitId = (String) findByKey("pbm_presc_id", pbmPrescId).get("erx_visit_id");
    if (erxVisitId != null) {
      return DatabaseHelper.queryToDynaBean(GET_ERX_DETAILS_FOR_VISIT_ID,
          new Object[] {pbmPrescId});
    }
    return DatabaseHelper.queryToDynaBean(GET_CONS_ERX_DETAILS, new Object[] {pbmPrescId});
  }

  /** The Constant GET_LATEST_CONS_ERX_DETAILS. */
  private static final String GET_LATEST_CONS_ERX_DETAILS =
      " SELECT pp.erx_consultation_id, pp.erx_visit_id, pp.erx_presc_id,"
          + " pp.erx_request_type, pp.erx_request_date " + " FROM pbm_prescription pp "
          + " JOIN patient_medicine_prescriptions pmp ON (pmp.pbm_presc_id = pp.pbm_presc_id) "
          + " WHERE #filter# = ? "
          + " AND pp.erx_presc_id IS NOT NULL AND pp.erx_reference_no IS NOT NULL LIMIT 1 ";

  /**
   * Gets the latest cons erx bean.
   *
   * @param consId the cons id
   * @return the latest cons erx bean
   */
  public BasicDynaBean getLatestConsErxBean(Object consId) {
    String query = GET_LATEST_CONS_ERX_DETAILS.replace("#filter#",
        (consId instanceof String) ? "is_discharge_medication=true AND erx_visit_id"
            : "erx_consultation_id");
    return DatabaseHelper.queryToDynaBean(query, new Object[] {consId});
  }

  /** The Constant GET_MRNOS_BY_PRESC_ID. */
  public static final String GET_MRNOS_BY_PRESC_ID = "SELECT DISTINCT patient.mr_no FROM "
      + " (( SELECT pr.mr_no, pmp.pbm_presc_id FROM patient_registration pr"
      + " JOIN pbm_medicine_prescriptions pmp ON (pr.patient_id = pmp.visit_id)"
      + " JOIN pbm_prescription pp USING (pbm_presc_id))" + " UNION ALL"
      + " (SELECT pre.mr_no,pmpa.pbm_presc_id FROM patient_registration pre"
      + " JOIN doctor_consultation dc ON (dc.mr_no = pre.mr_no)"
      + " JOIN patient_prescription pap ON (pap.consultation_id = dc.consultation_id)"
      + " JOIN patient_medicine_prescriptions pmpa ON "
      + " (pap.patient_presc_id = pmpa.op_medicine_pres_id))) as patient"
      + "  WHERE patient.pbm_presc_id IN (:pbmPrescId)";

  /**
   * Gets the mr nos by PBM prescription id.
   *
   * @param pbmPrescIds the pbm presc ids
   * @return the mr nos by PBM prescription id
   */
  public List<BasicDynaBean> getMrNosByPBMPrescriptionId(List<Integer> pbmPrescIds) {

    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("pbmPrescId", pbmPrescIds);

    return DatabaseHelper.queryToDynaList(GET_MRNOS_BY_PRESC_ID, parameters);
  }

  /** The Constant GET_PBM_PRESCRIPTION_WITH_OPEN_STATUS. */
  private static final String GET_PBM_PRESCRIPTION_WITH_OPEN_STATUS = "SELECT * "
      + "FROM pbm_prescription WHERE status = 'O' "
      + "AND (pbm_request_id IS NULL OR pbm_request_id = '') AND pbm_presc_id = ?";

  /**
   * gets the pbm presc bean with open status and null requestid.
   *
   * @param pbmPrescId the pbm prscription id
   * @return pbm presc bean
   */
  public BasicDynaBean getPbmPrescriptionWithOpenStatus(int pbmPrescId) {
    return DatabaseHelper
        .queryToDynaBean(GET_PBM_PRESCRIPTION_WITH_OPEN_STATUS, new Object[] {pbmPrescId});
  }
}
