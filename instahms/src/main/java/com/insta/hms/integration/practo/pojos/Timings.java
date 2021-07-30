package com.insta.hms.integration.practo.pojos;

public class Timings {
  private Wednesday wednesday;

  private Thursday thursday;

  private Monday monday;

  private Sunday sunday;

  private Saturday saturday;

  private Friday friday;

  private Tuesday tuesday;

  public Wednesday getWednesday() {
    return wednesday;
  }

  public void setWednesday(Wednesday wednesday) {
    this.wednesday = wednesday;
  }

  public Thursday getThursday() {
    return thursday;
  }

  public void setThursday(Thursday thursday) {
    this.thursday = thursday;
  }

  public Monday getMonday() {
    return monday;
  }

  public void setMonday(Monday monday) {
    this.monday = monday;
  }

  public Sunday getSunday() {
    return sunday;
  }

  public void setSunday(Sunday sunday) {
    this.sunday = sunday;
  }

  public Saturday getSaturday() {
    return saturday;
  }

  public void setSaturday(Saturday saturday) {
    this.saturday = saturday;
  }

  public Friday getFriday() {
    return friday;
  }

  public void setFriday(Friday friday) {
    this.friday = friday;
  }

  public Tuesday getTuesday() {
    return tuesday;
  }

  public void setTuesday(Tuesday tuesday) {
    this.tuesday = tuesday;
  }

  @Override
  public String toString() {
    return "ClassPojo [wednesday = " + wednesday + ", thursday = " + thursday + ", monday = "
        + monday + ", sunday = " + sunday + ", saturday = " + saturday + ", friday = " + friday
        + ", tuesday = " + tuesday + "]";
  }
}