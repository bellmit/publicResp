package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionMedicineFavouriteRepository extends GenericRepository {

  public PrescriptionMedicineFavouriteRepository() {
    super("doctor_medicine_favourites");
  }

  private static final String INSERT_PRESC_MEDICINE_FAVOURITE =
      " INSERT INTO doctor_medicine_favourites(favourite_id, doctor_id, display_order, "
          + " frequency, duration, duration_units, medicine_quantity, medicine_remarks,"
          + " medicine_id, route_of_admin, "
          + " strength, generic_code, item_form_id, item_strength, item_strength_units, "
          + "consumption_uom, admin_strength, special_instr) "
          + " (select nextval('doctor_medicine_favourites_seq'), ?, ?, frequency, duration, "
          + "duration_units, "
          + " medicine_quantity, medicine_remarks, medicine_id, route_of_admin, strength, "
          + "generic_code, "
          + " item_form_id, item_strength, item_strength_units, consumption_uom, "
          + " admin_strength, ? "
          + "  from patient_medicine_prescriptions where op_medicine_pres_id=?)";

  /**
   * insert presc medicine favourite.
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param patientPrescId patient presc ID
   * @return bean
   */
  public boolean insertPresMedicineFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRESC_MEDICINE_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
