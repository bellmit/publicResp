package com.insta.hms.integration.practo.pojos;

public class Monday {
  private String session2_start_time;

  private String session1_start_time;

  private String session1_end_time;

  private String session2_end_time;

  public String getSession2_start_time() {
    return session2_start_time;
  }

  public void setSession2_start_time(String session2_start_time) {
    this.session2_start_time = session2_start_time;
  }

  public String getSession1_start_time() {
    return session1_start_time;
  }

  public void setSession1_start_time(String session1_start_time) {
    this.session1_start_time = session1_start_time;
  }

  public String getSession1_end_time() {
    return session1_end_time;
  }

  public void setSession1_end_time(String session1_end_time) {
    this.session1_end_time = session1_end_time;
  }

  public String getSession2_end_time() {
    return session2_end_time;
  }

  public void setSession2_end_time(String session2_end_time) {
    this.session2_end_time = session2_end_time;
  }

  @Override
  public String toString() {
    return "ClassPojo [session2_start_time = " + session2_start_time + ", session1_start_time = "
        + session1_start_time + ", session1_end_time = " + session1_end_time
        + ", session2_end_time = " + session2_end_time + "]";
  }
}