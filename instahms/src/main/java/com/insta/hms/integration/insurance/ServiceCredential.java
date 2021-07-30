package com.insta.hms.integration.insurance;

/**
 * The Class ServiceCredential.
 */
public class ServiceCredential {

  /** The service user. */
  private String serviceUser;
  
  /** The service password. */
  private String servicePassword;
  
  /** The service regisration number. */
  private String serviceRegisrationNumber;

  /**
   * Instantiates a new service credential.
   *
   * @param user the user
   * @param pwd the pwd
   * @param serviceRegisrationNumber the service regisration number
   */
  public ServiceCredential(String user, String pwd, String serviceRegisrationNumber) {
    this.serviceUser = user;
    this.servicePassword = pwd;
    this.serviceRegisrationNumber = serviceRegisrationNumber;
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
   * Gets the service registration number.
   *
   * @return the service registration number
   */
  public String getServiceRegistrationNumber() {
    return serviceRegisrationNumber;
  }

}
