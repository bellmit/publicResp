package com.insta.hms.eservice;

/**
 * The Class EService.
 *
 * @param <T> the generic type
 */
public abstract class EService<T> {

  /**
   * The service user.
   */
  protected String serviceUser = null;

  /**
   * The service password.
   */
  protected String servicePassword = null;

  /**
   * The clinician user.
   */
  protected String clinicianUser = null;

  /**
   * The clinician password.
   */
  protected String clinicianPassword = null;

  /**
   * Instantiates a new e service.
   */
  public EService() {
  }

  /**
   * Instantiates a new e service.
   *
   * @param serviceUser     the service user
   * @param servicePassword the service password
   */
  public EService(String serviceUser, String servicePassword) {
    super();
    this.serviceUser = serviceUser;
    this.servicePassword = servicePassword;
  }

  /**
   * Instantiates a new e service.
   *
   * @param serviceUser the service user
   * @param servicePassword the service password
   * @param clinicianUser the clinician user
   * @param clinicianPassword the clinician password
   */
  public EService(String serviceUser, String servicePassword, String clinicianUser,
          String clinicianPassword) {
    super();
    this.serviceUser = serviceUser;
    this.servicePassword = servicePassword;
    this.clinicianUser = clinicianUser;
    this.clinicianPassword = clinicianPassword;
  }

  /**
   * Gets the service user.
   *
   * @return the service user
   */
  public String getServiceUser() {
    return serviceUser;
  }

  /**
   * Gets the service password.
   *
   * @return the service password
   */
  public String getServicePassword() {
    return servicePassword;
  }

  /**
   * Gets the clinician user.
   *
   * @return the clinician user
   */
  public String getClinicianUser() {
    return clinicianUser;
  }

  /**
   * Gets the clinician password.
   *
   * @return the clinician password
   */
  public String getClinicianPassword() {
    return clinicianPassword;
  }

  /**
   * Gets the remote service.
   *
   * @return the remote service
   */
  public abstract T getRemoteService();

}
