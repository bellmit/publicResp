package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientPrescriptionsRepository.
 */
@Repository
public class PatientPrescriptionsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new patient prescriptions repository.
   */
  public PatientPrescriptionsRepository() {
    super("patient_prescription", "patient_presc_id");
  }


  /**
   * Update presc integer.
   *
   * @param patientPrescIds the patient presc ids
   * @param status          the status
   * @return the integer
   */
  public Integer updatePresc(List<Integer> patientPrescIds, String status) {
    if (CollectionUtils.isEmpty(patientPrescIds)) {
      return 0;
    }
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("prescIds", patientPrescIds);
    params.addValue("status", status);
    return DatabaseHelper.update(
        "UPDATE patient_prescription SET status = :status  WHERE patient_presc_id in (:prescIds)",
        params);
  }

}
