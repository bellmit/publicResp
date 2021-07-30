package com.insta.hms.core.clinical.outpatient;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

/**
 * The Class DoctorConsultationImageRepository.
 *
 * @author anup vishwas
 */

@Repository
public class DoctorConsultationImageRepository extends GenericRepository {

  /**
   * Instantiates a new doctor consultation image repository.
   */
  public DoctorConsultationImageRepository() {
    super("doctor_consult_images");
  }

}
