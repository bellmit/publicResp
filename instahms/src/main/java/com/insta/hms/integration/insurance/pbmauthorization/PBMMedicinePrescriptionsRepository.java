package com.insta.hms.integration.insurance.pbmauthorization;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class PBMMedicinePrescriptionsRepository.
 */
@Repository
public class PBMMedicinePrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new PBM medicine prescriptions repository.
   */
  public PBMMedicinePrescriptionsRepository() {
    super("pbm_medicine_prescriptions");
  }

  /** The update pbm prescription id. */
  private String updatePBMPrescriptionId = "UPDATE pbm_medicine_prescriptions " //
      + " SET pbm_presc_id = ? WHERE #filter# = ?";

  /**
   * Update PBM prescription id.
   *
   * @param consId the cons id
   * @param pbmPrescId the pbm presc id
   * @return true, if successful
   */
  public boolean updatePBMPrescriptionId(Object consId, Integer pbmPrescId) {
    String query = updatePBMPrescriptionId.replace("#filter#",
        (consId instanceof String) ? "visit_id" : "consultation_id");
    return DatabaseHelper.update(query, new Object[] {pbmPrescId, consId}) > 0;
  }
}
