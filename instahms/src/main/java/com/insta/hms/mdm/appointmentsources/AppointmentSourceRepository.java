package com.insta.hms.mdm.appointmentsources;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class AppointmentSourceRepository.
 */
@Repository
public class AppointmentSourceRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new appointment source repository.
   */
  public AppointmentSourceRepository() {
    super("appointment_source_master", "appointment_source_id", "appointment_source_name");
  }
}
