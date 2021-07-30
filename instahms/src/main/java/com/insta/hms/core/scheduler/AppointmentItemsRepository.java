package com.insta.hms.core.scheduler;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class AppointmentItemsRepository extends GenericRepository {

  public AppointmentItemsRepository() {
    super("scheduler_appointment_items");
  }
}