/**
 *
 */
package com.insta.hms.resourcescheduler;

/**
 * @author lakshmi.p
 *
 */
public class TimingDTO {

	private String dept;
	private String doctor;
	private java.sql.Date nonAvailDate;
	private int weekDay;
	private java.sql.Date finalfromdate;
	private java.sql.Date finaltodate;

	private String remarks;

	private java.sql.Time mon1;
	private java.sql.Time mon2;
	private java.sql.Time mon3;
	private java.sql.Time mon4;

	private java.sql.Time tue1;
	private java.sql.Time tue2;
	private java.sql.Time tue3;
	private java.sql.Time tue4;

	private java.sql.Time wed1;
	private java.sql.Time wed2;
	private java.sql.Time wed3;
	private java.sql.Time wed4;

	private java.sql.Time thu1;
	private java.sql.Time thu2;
	private java.sql.Time thu3;
	private java.sql.Time thu4;

	private java.sql.Time fri1;
	private java.sql.Time fri2;
	private java.sql.Time fri3;
	private java.sql.Time fri4;

	private java.sql.Time sat1;
	private java.sql.Time sat2;
	private java.sql.Time sat3;
	private java.sql.Time sat4;

	private java.sql.Time sun1;
	private java.sql.Time sun2;
	private java.sql.Time sun3;
	private java.sql.Time sun4;

	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
	}
	public String getDoctor() {
		return doctor;
	}
	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}
	public java.sql.Date getFinalfromdate() {
		return finalfromdate;
	}
	public void setFinalfromdate(java.sql.Date finalfromdate) {
		this.finalfromdate = finalfromdate;
	}
	public java.sql.Date getFinaltodate() {
		return finaltodate;
	}
	public void setFinaltodate(java.sql.Date finaltodate) {
		this.finaltodate = finaltodate;
	}
	public java.sql.Time getFri1() {
		return fri1;
	}
	public void setFri1(java.sql.Time fri1) {
		this.fri1 = fri1;
	}
	public java.sql.Time getFri2() {
		return fri2;
	}
	public void setFri2(java.sql.Time fri2) {
		this.fri2 = fri2;
	}
	public java.sql.Time getFri3() {
		return fri3;
	}
	public void setFri3(java.sql.Time fri3) {
		this.fri3 = fri3;
	}
	public java.sql.Time getFri4() {
		return fri4;
	}
	public void setFri4(java.sql.Time fri4) {
		this.fri4 = fri4;
	}
	public java.sql.Time getMon1() {
		return mon1;
	}
	public void setMon1(java.sql.Time mon1) {
		this.mon1 = mon1;
	}
	public java.sql.Time getMon2() {
		return mon2;
	}
	public void setMon2(java.sql.Time mon2) {
		this.mon2 = mon2;
	}
	public java.sql.Time getMon3() {
		return mon3;
	}
	public void setMon3(java.sql.Time mon3) {
		this.mon3 = mon3;
	}
	public java.sql.Time getMon4() {
		return mon4;
	}
	public void setMon4(java.sql.Time mon4) {
		this.mon4 = mon4;
	}
	public java.sql.Date getNonAvailDate() {
		return nonAvailDate;
	}
	public void setNonAvailDate(java.sql.Date nonAvailDate) {
		this.nonAvailDate = nonAvailDate;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public java.sql.Time getSat1() {
		return sat1;
	}
	public void setSat1(java.sql.Time sat1) {
		this.sat1 = sat1;
	}
	public java.sql.Time getSat2() {
		return sat2;
	}
	public void setSat2(java.sql.Time sat2) {
		this.sat2 = sat2;
	}
	public java.sql.Time getSat3() {
		return sat3;
	}
	public void setSat3(java.sql.Time sat3) {
		this.sat3 = sat3;
	}
	public java.sql.Time getSat4() {
		return sat4;
	}
	public void setSat4(java.sql.Time sat4) {
		this.sat4 = sat4;
	}
	public java.sql.Time getSun1() {
		return sun1;
	}
	public void setSun1(java.sql.Time sun1) {
		this.sun1 = sun1;
	}
	public java.sql.Time getSun2() {
		return sun2;
	}
	public void setSun2(java.sql.Time sun2) {
		this.sun2 = sun2;
	}
	public java.sql.Time getSun3() {
		return sun3;
	}
	public void setSun3(java.sql.Time sun3) {
		this.sun3 = sun3;
	}
	public java.sql.Time getSun4() {
		return sun4;
	}
	public void setSun4(java.sql.Time sun4) {
		this.sun4 = sun4;
	}
	public java.sql.Time getThu1() {
		return thu1;
	}
	public void setThu1(java.sql.Time thu1) {
		this.thu1 = thu1;
	}
	public java.sql.Time getThu2() {
		return thu2;
	}
	public void setThu2(java.sql.Time thu2) {
		this.thu2 = thu2;
	}
	public java.sql.Time getThu3() {
		return thu3;
	}
	public void setThu3(java.sql.Time thu3) {
		this.thu3 = thu3;
	}
	public java.sql.Time getThu4() {
		return thu4;
	}
	public void setThu4(java.sql.Time thu4) {
		this.thu4 = thu4;
	}
	public java.sql.Time getTue1() {
		return tue1;
	}
	public void setTue1(java.sql.Time tue1) {
		this.tue1 = tue1;
	}
	public java.sql.Time getTue2() {
		return tue2;
	}
	public void setTue2(java.sql.Time tue2) {
		this.tue2 = tue2;
	}
	public java.sql.Time getTue3() {
		return tue3;
	}
	public void setTue3(java.sql.Time tue3) {
		this.tue3 = tue3;
	}
	public java.sql.Time getTue4() {
		return tue4;
	}
	public void setTue4(java.sql.Time tue4) {
		this.tue4 = tue4;
	}
	public java.sql.Time getWed1() {
		return wed1;
	}
	public void setWed1(java.sql.Time wed1) {
		this.wed1 = wed1;
	}
	public java.sql.Time getWed2() {
		return wed2;
	}
	public void setWed2(java.sql.Time wed2) {
		this.wed2 = wed2;
	}
	public java.sql.Time getWed3() {
		return wed3;
	}
	public void setWed3(java.sql.Time wed3) {
		this.wed3 = wed3;
	}
	public java.sql.Time getWed4() {
		return wed4;
	}
	public void setWed4(java.sql.Time wed4) {
		this.wed4 = wed4;
	}
	public int getWeekDay() {
		return weekDay;
	}
	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}





}
