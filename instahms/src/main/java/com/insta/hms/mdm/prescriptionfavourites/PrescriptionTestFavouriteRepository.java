package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionTestFavouriteRepository extends GenericRepository {

  /**
   * constructor.
   */
  public PrescriptionTestFavouriteRepository() {
    super("doctor_test_favourites");
  }

  private static final String INSERT_PRES_TEST_FAVOURITE =
      " INSERT INTO doctor_test_favourites(favourite_id, doctor_id, display_order, "
          + " test_id, test_remarks, ispackage, special_instr) "
          + " (select nextval('doctor_test_favourites_seq'), ?, ?, test_id, test_remarks,"
          + " ispackage, ? "
          + " from patient_test_prescriptions where op_test_pres_id=?)";

  /**
   * insert presc test favourites.
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param patientPrescId patient presc ID
   * @return bean
   */
  public boolean insertPresTestFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRES_TEST_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
