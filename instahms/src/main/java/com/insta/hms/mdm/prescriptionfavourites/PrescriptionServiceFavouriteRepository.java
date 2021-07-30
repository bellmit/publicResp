package com.insta.hms.mdm.prescriptionfavourites;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PrescriptionServiceFavouriteRepository extends GenericRepository {

  /**
   * constructor.
   */
  public PrescriptionServiceFavouriteRepository() {
    super("doctor_service_favourites");
  }

  private static final String INSERT_PRESC_SERVICE_FAVOURITE =
      " INSERT INTO doctor_service_favourites(favourite_id, doctor_id, display_order, "
          + " service_id, service_remarks, special_instr) "
          + " (select nextval('doctor_service_favourites_seq'), ?, ?, service_id, "
          + "service_remarks, ? "
          + " from patient_service_prescriptions where op_service_pres_id=?)";

  /**
   * insert presc service favourites.
   * @param doctorId doctor ID
   * @param specialInstruction instruction
   * @param patientPrescId patient presc ID
   * @return result
   */
  public boolean insertPresServiceFavourite(
      String doctorId, String specialInstruction, int patientPrescId) {

    return DatabaseHelper.queryToDynaBean(
            INSERT_PRESC_SERVICE_FAVOURITE,
            new Object[] {doctorId, 1, specialInstruction, patientPrescId})
        != null;
  }
}
