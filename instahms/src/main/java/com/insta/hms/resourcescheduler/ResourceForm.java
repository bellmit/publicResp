/**
 *
 */
package com.insta.hms.resourcescheduler;

import org.apache.struts.action.ActionForm;

/**
 * @author lakshmi.p
 *
 */
public class ResourceForm extends ActionForm {

	private String date;
	private String[] scheduleId;

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String[] getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(String[] scheduleId) {
		this.scheduleId = scheduleId;
	}
}
