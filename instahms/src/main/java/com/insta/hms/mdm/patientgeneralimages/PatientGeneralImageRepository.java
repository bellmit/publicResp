package com.insta.hms.mdm.patientgeneralimages;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Component;

/**
 * The Class PatientGeneralImageRepository.
 */
@Component
public class PatientGeneralImageRepository extends GenericRepository {

  /**
   * Instantiates a new patient general image repository.
   */
  public PatientGeneralImageRepository() {
    super("patient_general_images");
  }

}