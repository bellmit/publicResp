package com.insta.hms.core.clinical.eauthorization;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class EAuthPrescriptionRepository.
 *
 * @author teja
 */
@Repository
public class EAuthPrescriptionRepository extends GenericRepository {

  /**
   * Instantiates a new e auth prescription repository.
   */
  public EAuthPrescriptionRepository() {
    super("preauth_prescription");
  }

  /** The Constant GET_LATEST_EAUTH_PRESC_ID. */
  private static final String GET_LATEST_EAUTH_PRESC_ID = "SELECT "
      + " prp.preauth_presc_id "
      + " FROM preauth_prescription prp "
      + " JOIN preauth_prescription_activities ppa USING(preauth_presc_id) "
      + " WHERE prp.preauth_status = 'O' "
      + " AND (preauth_request_id IS NULL OR preauth_request_id = '') "
      + " AND visit_id= ? AND ppa.consultation_id = ? AND prp.preauth_payer_id = ?  "
      + " ORDER BY preauth_presc_id DESC " + "LIMIT 1";

  /**
   * Gets the latest E auth presc id.
   *
   * @param visitId
   *          the visit id
   * @param consId
   *          the cons id
   * @param insuranceCoId
   *          the insurance co id
   * @return the latest E auth presc id
   */
  public Integer getLatestEAuthPrescId(String visitId,Integer consId, String insuranceCoId) {
    return DatabaseHelper.getInteger(GET_LATEST_EAUTH_PRESC_ID, new Object[] { visitId, consId,
        insuranceCoId });
  }
  
  /** The Constant GET_MRNOS_BY_EAUTH_PRESC_ID. */
  private static final String GET_MRNOS_BY_EAUTH_PRESC_ID =  " SELECT distinct pr.mr_no "
      + " FROM preauth_prescription pp "
      + " JOIN preauth_prescription_activities ppa ON(pp.preauth_presc_id = ppa.preauth_presc_id) "
      + " JOIN patient_registration pr ON(pr.patient_id = ppa.visit_id)  "
      + " WHERE pp.preauth_presc_id IN (:preAuthPrescId) ";

  /**
   * Gets the mr nos by E auth presc id.
   *
   * @param preAuthPrescIds
   *          the pre auth presc ids
   * @return the mr nos by E auth presc id
   */
  public List<BasicDynaBean> getMrNosByEAuthPrescId(List<Integer> preAuthPrescIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("preAuthPrescId", preAuthPrescIds);
    return DatabaseHelper.queryToDynaList(GET_MRNOS_BY_EAUTH_PRESC_ID, parameters);
  }
}
