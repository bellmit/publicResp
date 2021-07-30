package com.insta.hms.core.scheduler;

import java.sql.Timestamp;
import java.util.List;

public class RecurrencePattern {
  private Integer recurrNo;
  private String recurranceOption;
  private List week;

  private Timestamp recurrDate;
  private Integer occurrNo;
  private java.util.Date untilDate;
  private String monthlyRecurrType;

  public Integer getRecurrNo() {
    return recurrNo;
  }

  public void setRecurrNo(Integer recurrNo) {
    this.recurrNo = recurrNo;
  }

  public String getRecurranceOption() {
    return recurranceOption;
  }

  public void setRecurranceOption(String recurranceOption) {
    this.recurranceOption = recurranceOption;
  }

  public List getWeek() {
    return week;
  }

  public void setWeek(List week) {
    this.week = week;
  }

  public java.util.Date getRecurrDate() {
    return recurrDate;
  }

  public void setRecurrDate(Timestamp recurrDate) {
    this.recurrDate = recurrDate;
  }

  public Integer getOccurrNo() {
    return occurrNo;
  }

  public void setOccurrNo(Integer occurrNo) {
    this.occurrNo = occurrNo;
  }

  public java.util.Date getUntilDate() {
    return untilDate;
  }

  public void setUntilDate(java.util.Date untilDate) {
    this.untilDate = untilDate;
  }

  public List getAppointmentDateTimes() {
    return null;
  }

  public String getMonthlyRecurrType() {
    return monthlyRecurrType;
  }

  public void setMonthlyRecurrType(String monthlyRecurrType) {
    this.monthlyRecurrType = monthlyRecurrType;
  }

}
