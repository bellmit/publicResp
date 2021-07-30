package com.insta.hms.common;

public class SentryUiConfig {

  private String dsn;

  private String environment;

  private String release;

  private String servername;

  public String getDsn() {
    return dsn;
  }

  public void setDsn(String dsn) {
    this.dsn = dsn;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public String getServername() {
    return servername;
  }

  public void setServername(String servername) {
    this.servername = servername;
  }

  public boolean hasDsn() {
    return dsn != null && !dsn.equals("");
  }

}
