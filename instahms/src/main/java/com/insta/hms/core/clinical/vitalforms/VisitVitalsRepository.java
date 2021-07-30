package com.insta.hms.core.clinical.vitalforms;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

/**
 * The Class VisitVitalsRepository.
 *
 * @author krishnat
 */
@Repository
public class VisitVitalsRepository extends GenericRepository {

  /**
   * Instantiates a new visit vitals repository.
   */
  public VisitVitalsRepository() {
    super("visit_vitals");
  }

  /** The Constant GET_VISIT_VITAL_WEIGHT_DETAILS. */
  public static final String GET_VISIT_VITAL_WEIGHT_DETAILS =
      " SELECT param_label, param_uom, vpm.param_id, vr.param_value, "
          + " localtimestamp(0) as date_time, vr.vital_reading_id ,vpm.visit_type,"
          + " vpm.expr_for_calc_result" + " FROM visit_vitals vls "
          + " JOIN vital_reading vr ON (vr.vital_reading_id=vls.vital_reading_id)"
          + " JOIN vital_parameter_master vpm ON (vpm.param_id=vr.param_id)"
          + " JOIN patient_registration pr ON (pr.patient_id = vls.patient_id)"
          + " WHERE param_container='V' and vr.status = 'A' and vls.status='A' "
          + " AND (vpm.visit_type = CASE WHEN pr.visit_type :: text = 'i' THEN 'I' else 'O'"
          + " END OR vpm.visit_type is null) " + " AND param_status='A' AND param_label='Weight' "
          + " AND vls.patient_id= ? ORDER BY vls.date_time DESC limit 1";

  /**
   * Gets the visit vital weight bean.
   *
   * @param patientId the patient id
   * @return the visit vital weight bean
   */
  public BasicDynaBean getVisitVitalWeightBean(String patientId) {
    return DatabaseHelper.queryToDynaBean(GET_VISIT_VITAL_WEIGHT_DETAILS, new Object[] {patientId});
  }
}
