package com.insta.mhms.patient.scheduler;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceDAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 */
public class ResourceBO {
  static Logger logger = LoggerFactory.getLogger(ResourceBO.class);

  /**
   * method to save appointment.
   *
   * @param con connection object
   * @param ap appointment object
   * @param resourceId resource id
   * @param isMobileAppReq is mobile app request
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
   */
  public boolean saveAppointment(
      Connection con, Appointments ap, String resourceId, boolean isMobileAppReq)
      throws SQLException {
    List<com.insta.hms.resourcescheduler.ResourceBO.Appointments> appList =
        new ArrayList<com.insta.hms.resourcescheduler.ResourceBO.Appointments>();
    Integer appointmentId = ap.getAppointmentId();
    appList.add(ap);
    AppointMentResource res = null;
    res = new AppointMentResource(appointmentId, "OPDOC", resourceId);
    if (isMobileAppReq) {
      res.setAppointment_item_id(new GenericDAO("scheduler_appointment_items").getNextSequence());
    } else {
      res.setAppointment_item_id(
          com.insta.instaapi.common.DbUtil.getNextSequence(con, "scheduler_appointment_items"));
    }
    java.sql.Timestamp modTime = com.insta.instaapi.common.DbUtil.getDateandTime(con);
    res.setAppointment_item_id(res.getAppointment_item_id());
    res.setUser_name("InstaAPI");
    res.setMod_time(modTime);
    List<AppointMentResource> scheduleAppointItemList = new ArrayList<AppointMentResource>();
    scheduleAppointItemList.add(res);

    ResourceDAO rdao = new ResourceDAO(con);
    boolean success = true;
    if (isMobileAppReq) {
      success = rdao.insertAppointments(appList);
    } else {
      success = com.insta.mhms.patient.scheduler.ResourceDAO.insertAppointments(con, appList);
    }
    if (success) {
      if (!scheduleAppointItemList.isEmpty()) {
        if (isMobileAppReq) {
          success = rdao.insertAppointmentItems(scheduleAppointItemList);
        } else {
          success =
              com.insta.mhms.patient.scheduler.ResourceDAO.insertAppointmentItems(
                  con, scheduleAppointItemList);
        }
      }
    }

    return success;
  }

  /**
   * Update appointment method.
   *
   * @param con connection object
   * @param ap appointment object
   * @param resourceId resouce id
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
   */
  public boolean updateAppointment(Connection con, Appointments ap, String resourceId)
      throws SQLException {
    List<com.insta.hms.resourcescheduler.ResourceBO.Appointments> appList =
        new ArrayList<com.insta.hms.resourcescheduler.ResourceBO.Appointments>();
    Integer appointmentId = ap.getAppointmentId();
    appList.add(ap);
    AppointMentResource res = null;
    res = new AppointMentResource(appointmentId, "OPDOC", resourceId);
    res.setAppointmentId(appointmentId);
    res.setUser_name("InstaAPI");
    java.sql.Timestamp modTime = com.insta.instaapi.common.DbUtil.getDateandTime(con);
    res.setMod_time(modTime);
    res.setResourceId(resourceId);
    List<AppointMentResource> scheduleAppointItemList = new ArrayList<AppointMentResource>();
    scheduleAppointItemList.add(res);
    boolean success = true;
    success = updateAppointment(con, appList, modTime);
    if (success) {
      if (!scheduleAppointItemList.isEmpty()) {
        success = updateAppointmentItem(con, scheduleAppointItemList);
      }
    }

    return success;
  }

  // nedd to add to instahms resourcedao class.
  public static final String UPDATE_APPOINTMENT =
      "UPDATE scheduler_appointments SET "
          + " res_sch_id = ?, res_sch_name = ?, appointment_time = ?, "
          + " duration = ?, appointment_status = ?,changed_by = ?,center_id = ?,changed_time = ?, "
          + " booked_by = ?, unique_appt_ind = ?, prim_res_id = ? WHERE appointment_id = ?";

  /**
   * method to update appointment.
   *
   * @param con connection object
   * @param list list parameter
   * @param changedTime changed time
   * @return returns true or false
   * @throws SQLException may throw Sql Exception
   */
  public boolean updateAppointment(Connection con, List list, Timestamp changedTime)
      throws SQLException {
    int[] results = null;
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(UPDATE_APPOINTMENT);
      Iterator iterator = list.iterator();
      int index = 1;
      while (iterator.hasNext()) {
        Appointments appt = (Appointments) iterator.next();
        ps.setInt(index++, appt.getScheduleId());
        ps.setString(index++, appt.getScheduleName());
        ps.setTimestamp(index++, appt.getAppointmentTime());
        ps.setInt(index++, appt.getAppointmentDuration());
        ps.setString(index++, appt.getAppointStatus());
        ps.setString(index++, appt.getChangedBy());
        ps.setInt(index++, appt.getCenterId());
        ps.setTimestamp(index++, changedTime);
        ps.setString(index++, appt.getBookedBy());
        ps.setInt(index++, appt.getUnique_appt_ind());
        ps.setString(index++, appt.getPrim_res_id());
        ps.setInt(index++, appt.getAppointmentId());
        ps.addBatch();
        results = ps.executeBatch();
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    ps.close();
    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  public static final String UPDATE_APPOINTMENT_ITEMS =
      "UPDATE scheduler_appointment_items SET resource_id = ?, user_name = ?, mod_time = ?"
          + " WHERE appointment_id = ?";

  /**
   * update appointment item method.
   *
   * @param con connection object.
   * @param list list
   * @return returns true or false
   * @throws SQLException may throw Sql exception
   */
  public boolean updateAppointmentItem(Connection con, List list) throws SQLException {
    PreparedStatement ps = con.prepareStatement(UPDATE_APPOINTMENT_ITEMS);
    boolean success = true;
    Iterator iterator = list.iterator();
    while (iterator.hasNext()) {
      AppointMentResource res = (AppointMentResource) iterator.next();
      ps.setString(1, res.getResourceId());
      ps.setString(2, res.getUser_name());
      ps.setTimestamp(3, res.getMod_time());
      ps.setInt(4, res.getAppointmentId());
      ps.addBatch();
    }
    int[] results = ps.executeBatch();

    ps.close();
    for (int p = 0; p < results.length; p++) {
      if (results[p] <= 0) {
        success = false;
        break;
      }
    }
    return success;
  }

  public static final String CANCEL_APPOINTMENT =
      "UPDATE scheduler_appointments SET appointment_status = ?, changed_by = ?, "
          + " changed_time = ?, cancel_reason = ?, unique_appt_ind = ? "
          + " WHERE appointment_id = ?";

  /**
   * Cancel appointment.
   *
   * @param con connection object
   * @param columns appointment columns
   * @return returns true or fals
   * @throws SQLException may throw Sql Exception
   */
  public boolean cancelAppointment(Connection con, Map<String, Object> columns)
      throws SQLException {
    PreparedStatement ps = con.prepareStatement(CANCEL_APPOINTMENT);
    ps.setString(1, (String) columns.get("appointment_status"));
    ps.setString(2, (String) columns.get("changed_by"));
    ps.setTimestamp(3, (Timestamp) columns.get("changed_time"));
    ps.setString(4, (String) columns.get("cancel_reason"));
    ps.setInt(5, (Integer) columns.get("unique_appt_ind"));
    ps.setInt(6, (Integer) columns.get("appointment_id"));
    boolean success = true;
    success = ps.executeUpdate() >= 0;

    if (ps != null) {
      ps.close();
    }

    return success;
  }
}
