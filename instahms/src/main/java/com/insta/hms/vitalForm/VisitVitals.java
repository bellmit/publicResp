/**
 *
 */
package com.insta.hms.vitalForm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author krishna
 *
 */
public class VisitVitals {
	public Timestamp dateTime;
	public int vitalReadingId;
	public String userName;
	public String vitalStatus;
	public List readings = new ArrayList();
	public void setVitalReadingId(int vitalReadingId) {
		this.vitalReadingId = vitalReadingId;
	}
	public int getVitalReadingId() {
		return vitalReadingId;
	}
	public void setDateTime(Timestamp dateTime) {
		this.dateTime = dateTime;
	}
	public Timestamp getDateTime() {
		return dateTime;
	}
	public void addReading(VitalsReadings reading) {
		readings.add(reading);
	}
	public List getReadings() {
		return readings;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
  public String getVitalStatus() {
    return vitalStatus;
  }
  public void setVitalStatus(String vitalStatus) {
    this.vitalStatus = vitalStatus;
  }
	
}
