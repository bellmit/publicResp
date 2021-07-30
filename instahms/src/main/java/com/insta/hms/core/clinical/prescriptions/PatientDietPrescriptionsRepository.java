package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientDietPrescriptionsRepository.
 *
 * @author ritolia
 */
@Repository
public class PatientDietPrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient diet prescriptions repository.
   */
  public PatientDietPrescriptionsRepository() {
    super("patient_diet_prescriptions");
  }

  /** The Constant DIET_PRESCRIPTIONS. */
  private static final String DIET_PRESCRIPTIONS =
      " SELECT dp.visit_id as patient_id, diet_pres_id as pres_id, d.doctor_id, d.doctor_name, "
          + " dp.meal_name as name, dp.special_instructions as remarks, dm.diet_id, "
          + " dp.meal_date, dp.meal_timing, dp.meal_time " + " FROM patient_diet_prescriptions dp "
          + " JOIN diet_master dm USING (meal_name) "
          + " JOIN doctors d ON (dp.prescribed_by = d.doctor_id) "
          + " WHERE added_to_bill = 'f' AND dp.visit_id=? order by diet_pres_id";

  /**
   * Gets the prescriptions.
   *
   * @param patientId the patient id
   * @return the prescriptions
   */
  public List<BasicDynaBean> getPrescriptions(String patientId) {
    return DatabaseHelper.queryToDynaList(DIET_PRESCRIPTIONS, patientId);
  }

}
