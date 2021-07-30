package com.insta.hms.integration.insurance.erxprescription;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ERxResponseRepository.
 */
@Repository
public class ERxResponseRepository extends GenericRepository {

  /**
   * Instantiates a new e rx response repository.
   */
  public ERxResponseRepository() {
    super("erx_response");
  }

  /**
   * Gets the int value.
   *
   * @param value the value
   * @return the int value
   */
  @SuppressWarnings("rawtypes")
  private Object getIntValue(Object value) {
    if (value == null || value == "") {
      return 0;
    } else {
      return value;
    }
  }

  /** The Constant GET_ERX_PBM_ID. */
  public static final String GET_ERX_PBM_ID = " SELECT pbm_presc_id "
      + " FROM patient_prescription pp " + "   JOIN patient_medicine_prescriptions pmp ON "
      + "(pp.patient_presc_id=pmp.op_medicine_pres_id) " + " WHERE consultation_id = ? ";

  /**
   * Gets the erx cons pbm id.
   *
   * @param consId the cons id
   * @return the erx cons pbm id
   */
  public Integer getErxConsPbmId(int consId) {
    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(GET_ERX_PBM_ID,
        new Object[] { consId });
    Integer id = 0;
    if (!list.isEmpty()) {
      Object value = list.get(0).get("pbm_presc_id");
      id = (Integer) getIntValue(value);
    }
    return id;
  }

  /** The Constant GET_CONS_ERX_DETAILS. */
  public static final String GET_CONS_ERX_DETAILS = " SELECT pr.mr_no, pr.patient_id,"
      + " pp.pbm_presc_id, pp.erx_consultation_id, pp.erx_presc_id, "
      + " pp.erx_request_type, pp.erx_request_date, pp.erx_center_id, pp.erx_reference_no, "
      + " dc.doctor_name, coalesce(pip.plan_id, pr.plan_id, 0) as plan_id,"
      + " pr.primary_sponsor_id, pr.primary_insurance_co, "
      + " CASE WHEN pd.government_identifier IS NULL OR pd.government_identifier = '' THEN "
      + " COALESCE(gim.identifier_type,'') ELSE"
      + " pd.government_identifier END AS emirates_id_number, "
      + " ppd.member_id, ppd.policy_number, pr.encounter_type, etc.encounter_type_desc,"
      + " to_char(coalesce(pd.dateofbirth, expected_dob), 'dd/MM/yyyy') as dob,"
      + " pd.email_id "
      + " FROM pbm_prescription pp "
      + " JOIN doctor_consultation dc ON (dc.consultation_id = pp.erx_consultation_id) "
      + " JOIN patient_registration pr ON (pr.patient_id = dc.patient_id) "
      + " JOIN patient_details pd ON (pd.mr_no = pr.mr_no)"
      + " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id) "
      + " LEFT JOIN patient_insurance_plans pip ON"
      + " (pr.patient_id = pip.patient_id AND pip.priority=1 ) "
      + " LEFT JOIN patient_policy_details ppd ON (ppd.patient_policy_id = pip.patient_policy_id) "
      + " LEFT JOIN encounter_type_codes etc ON (etc.encounter_type_id = encounter_type)  "
      + " WHERE pp.pbm_presc_id = ? ";

  /**
   * Gets the cons erx details.
   *
   * @param pbmPrescId the pbm presc id
   * @return the cons erx details
   */
  public BasicDynaBean getConsErxDetails(int pbmPrescId) {
    return DatabaseHelper.queryToDynaBean(GET_CONS_ERX_DETAILS, new Object[] { pbmPrescId });
  }

}
