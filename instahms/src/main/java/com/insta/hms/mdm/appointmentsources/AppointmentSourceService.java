package com.insta.hms.mdm.appointmentsources;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The appointment source service.
 * 
 * @author preeti
 */
@Service
public class AppointmentSourceService extends MasterService {

  /** The appointment source repository. */
  @LazyAutowired
  AppointmentSourceRepository appointmentSourceRepository;

  /**
   * Instantiates a new appointment source service.
   *
   * @param repository
   *          the r
   * @param validator
   *          the v
   */
  public AppointmentSourceService(AppointmentSourceRepository repository,
      AppointmentSourceValidator validator) {
    super(repository, validator);
  }

  /**
   * Gets the active appointment sources.
   *
   * @return the active appointment sources
   */
  public List<BasicDynaBean> getActiveAppointmentSources() {
    List<String> columns = new ArrayList<String>();
    columns.add("appointment_source_id");
    columns.add("appointment_source_name");
    columns.add("editable");
    return appointmentSourceRepository.listAll(columns, "status", "A");
  }

  /**
   * Gets the appointment source id for source name.
   *
   * @param apptSourceName
   *          the appt source name
   * @return the appointment source id for source name
   */
  public List<BasicDynaBean> getAppointmentSourceIdForSourceName(String apptSourceName) {
    List<String> columns = new ArrayList<String>();
    columns.add("appointment_source_id");
    return appointmentSourceRepository.listAll(columns, "appointment_source_name", apptSourceName);
  }
}
