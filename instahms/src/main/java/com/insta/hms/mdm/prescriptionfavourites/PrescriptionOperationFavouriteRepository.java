package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionOperationFavouriteRepository extends GenericRepository {

  /**
   * constructor.
   */
  public PrescriptionOperationFavouriteRepository() {
    super("doctor_operation_favourites");
  }

  private static final String INSERT_PRES_OPERATION_FAVOURITE =
      " INSERT INTO doctor_operation_favourites(favourite_id, doctor_id, display_order, "
          + " operation_id, remarks, special_instr) "
          + " (select nextval('doctor_operation_favourites_seq'), ?, ?, operation_id, remarks, ? "
          + " from patient_operation_prescriptions where prescription_id=?)";

  /**
   * insert presc operation favourite.
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param patientPrescId patient presc ID
   * @return result
   */
  public boolean insertPresOperationFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRES_OPERATION_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
