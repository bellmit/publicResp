package com.insta.hms.core.scheduler;

import org.apache.commons.beanutils.BasicDynaBean;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface AppointmentCategory.
 */
public interface AppointmentCategory {

  /**
   * Gets the category.
   *
   * @return the category
   */
  public String getCategory();

  /**
   * Gets the secondary resource master query.
   *
   * @return the secondary resource master query
   */
  public String getSecondaryResourceMasterQuery();

  /**
   * Gets the primary resource master query.
   *
   * @return the primary resource master query
   */
  public String getPrimaryResourceMasterQuery();

  /**
   * Gets the dept filter clause.
   *
   * @return the dept filter clause
   */
  public String getDeptFilterClause();

  /**
   * Sets the appointment data.
   *
   * @param appointmentsList the appointments list
   * @param params the params
   */
  public void setAppointmentData(List<Appointment> appointmentsList, Map<String, Object> params);

  /**
   * Sets the appointment additional resource data.
   *
   * @param appointment the appointment
   * @param appointmentItemsList the appointment items list
   * @param params the params
   */
  public void setAppointmentAdditionalResourceData(Appointment appointment,
      List<AppointmentResource> appointmentItemsList, Map<String, Object> params);

  /**
   * Gets the secondary resource type.
   *
   * @return the secondary resource type
   */
  public String getSecondaryResourceType();

  /**
   * Gets the primary resource type.
   *
   * @return the primary resource type
   */
  public String getPrimaryResourceType();

  /**
   * Gets the resource overbook limit.
   *
   * @param resourceId the resource id
   * @param resType the res type
   * @return the resource overbook limit
   */
  public Integer getResourceOverbookLimit(String resourceId, String resType);

  /**
   * Filter visit timings by center.
   *
   * @param visitTimingsList the visit timings list
   * @param loggedInCenterId the logged in center id
   * @return the list
   */
  public List<BasicDynaBean> filterVisitTimingsByCenter(List<BasicDynaBean> visitTimingsList,
      int loggedInCenterId);

  /**
   * Gets the appointment details query.
   *
   * @return the appointment details query
   */
  public String getAppointmentDetailsQuery();

  /**
   * Gets the resource name.
   *
   * @param resourceId the resource id
   * @param resType the res type
   * @return the resource name
   */
  public String getResourceName(String resourceId, String resType);

  /**
   * Validate prim res.
   *
   * @return the string
   */
  public String validatePrimRes();

  /**
   * Validate sec res.
   *
   * @return the string
   */
  public String validateSecRes();

  /**
   * Gets the appointment duration.
   *
   * @param secResId the sec res id
   * @param primResId the prim res id
   * @return the appointment duration
   */
  public int getAppointmentDuration(String secResId, String primResId);

  /**
   * Gets the slot duration of prim res.
   *
   * @param primResId the prim res id
   * @return the slot duration of prim res
   */
  public int getSlotDurationOfPrimRes(String primResId);

  /**
   * Gets the prim res applicable for sec res.
   *
   * @param secResId the sec res id
   * @param centerId the center id
   * @param deptId the dept id
   * @return the prim res applicable for sec res
   */
  public List<Map> getPrimResApplicableForSecRes(String secResId, int centerId, String deptId);

  /**
   * Gets the secondary resources.
   *
   * @return the secondary resources
   */
  public List<Map> getSecondaryResources();
  

  /**
   * Gets the secondary resources.
   *
   * @param primResId the prim res id
   * @return the secondary resources
   */
  public List<Map> getSecondaryResources(String primResId);
}
