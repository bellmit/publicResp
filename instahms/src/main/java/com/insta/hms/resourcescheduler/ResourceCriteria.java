/**
 *
 */
package com.insta.hms.resourcescheduler;

import java.sql.Date;
import java.util.ArrayList;

/**
 * @author lakshmi.p
 *
 */
public class ResourceCriteria {

	public String category;
	public String department;
	public java.sql.Date choosendate;
	public java.util.List<String> scheduleName;
	public java.util.List<String> schName;
	public java.util.List<Date> datesArray;
	public java.util.List<Date> tempDatesArray;
	public java.util.List<Integer> dayOfweek;

	public ResourceCriteria(){
		this.scheduleName = new ArrayList();
		this.datesArray = new ArrayList();
		this.schName = new ArrayList();
		this.tempDatesArray = new ArrayList<Date>();
		this.dayOfweek = new ArrayList<Integer>();
	}
}
