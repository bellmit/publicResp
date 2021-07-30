package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionDoctorOtherFavouriteRepository extends GenericRepository {

  /**
   * constructor.
   */
  public PrescriptionDoctorOtherFavouriteRepository() {
    super("doctor_other_favourites");
  }

  private static final String INSERT_PRESC_DOC_OTHER_FAVOURITE =
      " INSERT INTO doctor_other_favourites(favourite_id, doctor_id, display_order, "
          + "item_name, item_remarks, frequency, duration, duration_units, medicine_quantity, "
          + "strength, item_form_id, item_strength, item_strength_units, non_hosp_medicine, "
          + " consumption_uom, admin_strength, special_instr) "
          + " (select nextval('doctor_other_favourites_seq'), ?, ?, item_name, item_remarks, "
          + " frequency, duration, duration_units, "
          + "medicine_quantity, strength, item_form_id, item_strength, item_strength_units, "
          + "non_hosp_medicine, consumption_uom, admin_strength, "
          + " ? from patient_other_prescriptions "
          + "where prescription_id=?)";

  /**
   * insert prescription doctor other favourites.
   * @param doctorId doctor ID
   * @param specialInstruction instruction string
   * @param patientPrescId patinet presc ID
   * @return bean
   */
  public boolean insertPresDocOtherFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRESC_DOC_OTHER_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
