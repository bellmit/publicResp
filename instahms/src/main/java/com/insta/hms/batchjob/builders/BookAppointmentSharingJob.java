package com.insta.hms.batchjob.builders;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.scheduler.Appointment;
import com.insta.hms.core.scheduler.AppointmentResource;
import com.insta.hms.core.scheduler.ResourceDTO;
import com.insta.hms.integration.book.BookIntegrationService;
import com.insta.hms.jobs.GenericJob;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class BookAppointmentSharingJob extends GenericJob {

  @LazyAutowired
  private BookIntegrationService bookIntegrationService;

  private Appointment appointment;
  private List<AppointmentResource> appointmentResourceList;
  private List<Map<String, Timestamp>> recurrenceList;
  private Boolean isAppointmentCreated;
  private List<ResourceDTO> resourceDeleteList;

  public Appointment getAppointment() {
    return appointment;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  public List<AppointmentResource> getAppointmentResourceList() {
    return appointmentResourceList;
  }

  public void setAppointmentResourceList(List<AppointmentResource> appointmentResourceList) {
    this.appointmentResourceList = appointmentResourceList;
  }

  public List<Map<String, Timestamp>> getRecurrenceList() {
    return recurrenceList;
  }

  public void setRecurrenceList(List<Map<String, Timestamp>> recurrenceList) {
    this.recurrenceList = recurrenceList;
  }

  public Boolean getIsAppointmentCreated() {
    return isAppointmentCreated;
  }

  public void setIsAppointmentCreated(Boolean isAppointmentCreated) {
    this.isAppointmentCreated = isAppointmentCreated;
  }

  public List<ResourceDTO> getResourceDeleteList() {
    return resourceDeleteList;
  }

  public void setResourceDeleteList(List<ResourceDTO> resourceDeleteList) {
    this.resourceDeleteList = resourceDeleteList;
  }

  @Override
  protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {

    bookIntegrationService.addDoctorAppointmentsToPracto(getAppointment(),
        getAppointmentResourceList(), getRecurrenceList(), getIsAppointmentCreated(), null);
  }

}
