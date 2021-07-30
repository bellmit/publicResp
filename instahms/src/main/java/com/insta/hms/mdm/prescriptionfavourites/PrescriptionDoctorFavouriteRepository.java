package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionDoctorFavouriteRepository extends GenericRepository {

  public PrescriptionDoctorFavouriteRepository() {
    super("doctor_consultation_favourites");
  }

  private static final String INSERT_PRESC_DOCTOR_FAVOURITE =
      " INSERT INTO doctor_consultation_favourites(favourite_id, doctor_id, display_order, "
          + "cons_doctor_id, consultation_remarks, special_instr) "
          + " (select nextval('doctor_consultation_favourites_seq'), ?, ?,"
          + " doctor_id, cons_remarks, ? "
          + "from patient_consultation_prescriptions where prescription_id=?)";

  /**
   * insert prescription doctor favourites.
   * @param doctorId doctor ID
   * @param specialInstruction instruction string
   * @param patientPrescId patient presc ID
   * @return bean
   */
  public boolean insertPresDoctorFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRESC_DOCTOR_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
