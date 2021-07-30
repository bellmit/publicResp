package com.insta.hms.core.clinical.prescriptions;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class PatientOtherMedicinePrescriptionsRepository.
 *
 * @author anup vishwas
 */

@Repository
public class PatientOtherMedicinePrescriptionsRepository extends GenericRepository {

  /**
   * Instantiates a new patient other medicine prescriptions repository.
   */
  public PatientOtherMedicinePrescriptionsRepository() {
    super("patient_other_medicine_prescriptions");
  }

  /** The Constant GET_OTHER_PRESCRIBED_MEDICINES. */
  private static final String GET_OTHER_PRESCRIBED_MEDICINES =
      " SELECT pp.consultation_id, pomp.medicine_name, frequency as medicine_dosage, strength,"
      + " medicine_remarks, medicine_quantity, prescribed_date, mod_time, activity_due_date,"
      + " route_id, route_name, 'P' as issued, g.generic_name, g.generic_code,"
      + " pomp.consumption_uom, duration, duration_units "
      + " FROM patient_prescription pp "
      + " JOIN patient_other_medicine_prescriptions pomp "
      + "   ON (pp.patient_presc_id=pomp.prescription_id) "
      + " JOIN prescribed_medicines_master pms ON (pomp.medicine_name=pms.medicine_name)"
      + " LEFT JOIN generic_name g ON (pms.generic_name=g.generic_code)"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pomp.route_of_admin) "
      + " WHERE consultation_id=? ORDER BY prescription_id ";

  /**
   * Gets the other prescribed medicines.
   *
   * @param consultationId the consultation id
   * @return the other prescribed medicines
   */
  public List<BasicDynaBean> getOtherPrescribedMedicines(int consultationId) {
    return DatabaseHelper.queryToDynaList(GET_OTHER_PRESCRIBED_MEDICINES,
        new Object[] {consultationId});
  }

}
