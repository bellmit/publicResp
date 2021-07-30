/**
 *
 */
package com.insta.hms.resourcescheduler;

/**
 * @author lakshmi.p
 *
 */
public class ResourceDTO {

//	 Used for scheduler_appointment_items updations
	private String resourceDelete;
	private String resourceCheck;
	private String resourceId;
	private String resourceType;
	private int appointmentId;
	private int appointment_item_id;
	private String user_name;
	private java.sql.Timestamp mod_time;
	private Integer centerId;

  // Used for Scheduler_Item_Master updations
	private int res_sch_id;
	private String item_id;

	public static final String APPT_BOOKED_STATUS = "Booked";
	public static final String APPT_CONFIRMED_STATUS = "Confirmed";
	public static final String APPT_ARRIVED_STATUS = "Arrived";
	public static final String APPT_COMPLETED_STATUS = "Completed";

	public static final String APPT_NOSHOW_STATUS = "Noshow";
	public static final String APPT_CANCEL_STATUS = "Cancel";

	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getResourceCheck() {
		return resourceCheck;
	}
	public void setResourceCheck(String resourceCheck) {
		this.resourceCheck = resourceCheck;
	}
	public String getResourceDelete() {
		return resourceDelete;
	}
	public void setResourceDelete(String resourceDelete) {
		this.resourceDelete = resourceDelete;
	}
	public int getAppointment_item_id() {
		return appointment_item_id;
	}
	public void setAppointment_item_id(int appointment_item_id) {
		this.appointment_item_id = appointment_item_id;
	}
	public int getAppointmentId() {
		return appointmentId;
	}
	public void setAppointmentId(int appointmentId) {
		this.appointmentId = appointmentId;
	}
	public int getRes_sch_id() {
		return res_sch_id;
	}
	public void setRes_sch_id(int res_sch_id) {
		this.res_sch_id = res_sch_id;
	}
	public String getItem_id() {
		return item_id;
	}
	public void setItem_id(String item_id) {
		this.item_id = item_id;
	}
	public java.sql.Timestamp getMod_time() {
		return mod_time;
	}
	public void setMod_time(java.sql.Timestamp mod_time) {
		this.mod_time = mod_time;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
  public Integer getCenterId() {
    return centerId;
  }
  public void setCenterId(Integer centerId) {
    this.centerId = centerId;
  }


}
