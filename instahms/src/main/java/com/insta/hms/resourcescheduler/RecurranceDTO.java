package com.insta.hms.resourcescheduler;

import java.sql.Timestamp;

public class RecurranceDTO {

	private int recurrNo;
	private String recurranceOption;
	private String[] week;

	private Timestamp recurrDate;
	private String repeatOption;
	private int occurrNo;
	private java.util.Date untilDate;

	public int getOccurrNo() {
		return occurrNo;
	}
	public void setOccurrNo(int occurrNo) {
		this.occurrNo = occurrNo;
	}
	public String getRecurranceOption() {
		return recurranceOption;
	}
	public void setRecurranceOption(String recurranceOption) {
		this.recurranceOption = recurranceOption;
	}
	public java.util.Date getRecurrDate() {
		return recurrDate;
	}
	public void setRecurrDate(Timestamp recurrDate) {
		this.recurrDate = recurrDate;
	}
	public int getRecurrNo() {
		return recurrNo;
	}
	public void setRecurrNo(int recurrNo) {
		this.recurrNo = recurrNo;
	}
	public String getRepeatOption() {
		return repeatOption;
	}
	public void setRepeatOption(String repeatOption) {
		this.repeatOption = repeatOption;
	}
	public java.util.Date getUntilDate() {
		return untilDate;
	}
	public void setUntilDate(java.sql.Date untilDate) {
		this.untilDate = untilDate;
	}
	public String[] getWeek() {
		return week;
	}
	public void setWeek(String[] week) {
		this.week = week;
	}




}
