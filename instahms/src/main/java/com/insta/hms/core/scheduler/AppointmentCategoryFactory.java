package com.insta.hms.core.scheduler;

import com.insta.hms.common.annotations.LazyAutowired;

import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating AppointmentCategory objects.
 */
@Component
public class AppointmentCategoryFactory {
  
  /** The doctor appointment category. */
  @LazyAutowired
  private DoctorAppointmentCategory doctorAppointmentCategory;

  /** The service apointment category. */
  @LazyAutowired
  private ServiceAppointmentCategory serviceApointmentCategory;

  /** The test appointment category. */
  @LazyAutowired
  private TestAppointmentCategory testAppointmentCategory;

  /**
   * Gets the single instance of AppointmentCategoryFactory.
   *
   * @param resourceCategory the resource category
   * @return single instance of AppointmentCategoryFactory
   */
  public AppointmentCategory getInstance(String resourceCategory) {
    ResourceCategory category = ResourceCategory.valueOf(resourceCategory);
    switch (category) {
      case DOC:
        return doctorAppointmentCategory;
      case SNP:
        return serviceApointmentCategory;
      case DIA:
        return testAppointmentCategory;
      default:
        return doctorAppointmentCategory;
        /*
         * case OPE: return opeAppointmentCategory; case SNP: 
         * return snpApointmentCategory; case DIA:
         * return diaAppointmentCategory;
         */
    }
  }

}
