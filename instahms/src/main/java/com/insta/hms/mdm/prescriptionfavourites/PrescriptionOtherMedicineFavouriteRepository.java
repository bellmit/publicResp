package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionOtherMedicineFavouriteRepository extends GenericRepository {

  /**
   * constructor.
   */
  public PrescriptionOtherMedicineFavouriteRepository() {
    super("doctor_other_medicine_favourites");
  }

  private static final String INSERT_PRESC_OTHER_MEDICINE_FAVOURITE =
      " INSERT INTO doctor_other_medicine_favourites(favourite_id, doctor_id, display_order, "
          + " medicine_name, frequency, duration, duration_units, medicine_quantity, "
          + " medicine_remarks, route_of_admin, "
          + " strength, item_form_id, item_strength, item_strength_units, consumption_uom, "
          + " admin_strength, special_instr) "
          + " (select nextval('doctor_other_medicine_favourites_seq'), ?, ?, medicine_name, "
          + " frequency, duration, duration_units, "
          + " medicine_quantity, medicine_remarks, route_of_admin, strength, item_form_id, "
          + " item_strength, item_strength_units, "
          + " consumption_uom, admin_strength, ? "
          + " from patient_other_medicine_prescriptions where prescription_id=?)";

  /**
   * insert presc other medicine favourites.
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param patientPrescId patient presc ID
   * @return bean result
   */
  public boolean insertPresOtherMedicineFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRESC_OTHER_MEDICINE_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
