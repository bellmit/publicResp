package com.insta.hms.core.clinical.patientactivities;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class PatientActivitiesRepository.
 */
@Repository
public class PatientActivitiesRepository extends GenericRepository {

  /**
   * Instantiates a new patient activities repository.
   */
  public PatientActivitiesRepository() {
    super("patient_activities");
  }

  /**
   * Cancel activity.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   */
  public boolean cancelActivity(int prescriptionId, String prescriptionType) {
    StringBuilder query = new StringBuilder("UPDATE patient_activities SET activity_status='X' "
            + " WHERE prescription_id=? AND ((prescription_type!='M' AND activity_status='P') "
            + " or (activity_status='S' AND prescription_type='M' ");
    if (prescriptionType != null) {
      query.append("AND due_date > now ()");
    }
    query.append(")) ");
    return DatabaseHelper.update(query.toString(),
        new Object[] {prescriptionId}) > 0;
  }

  /**
   * Delete incomplete activity.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   */
  public boolean deleteIncompleteActivity(int prescriptionId, String prescriptionType) {
    return DatabaseHelper.delete(
        "DELETE FROM patient_activities "
            + " WHERE activity_status='P' AND prescription_id=? AND prescription_type=?",
        new Object[] {prescriptionId, prescriptionType}) > 0;
  }

  /**
   * Completed activities exists.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   */
  public boolean completedActivitiesExists(int prescriptionId, String prescriptionType) {
    return !DatabaseHelper.queryToDynaList(
        "SELECT * FROM patient_activities "
        + "WHERE activity_status in ('C', 'O') AND prescription_id=? AND prescription_type=? ",
        new Object[] {prescriptionId, prescriptionType}).isEmpty();
  }

  /**
   * Gets the pending activity.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return the pending activity
   */
  public BasicDynaBean getPendingActivity(int prescriptionId, String prescriptionType) {
    return DatabaseHelper.queryToDynaBean(
        "SELECT * FROM patient_activities "
            + " WHERE activity_status='P' AND prescription_id=? AND prescription_type=?",
        new Object[] {prescriptionId, prescriptionType});
  }

  /** The Constant GET_MEDICATIONS_ACTIVITIES. */
  private static final String GET_MEDICATIONS_ACTIVITIES =
      "SELECT pa.activity_id, pa.infusion_site, pa.activity_remarks,"
      + " pa.activity_num, pa.setup_id, pa.completed_date, pa.due_date, pa.stock, "
      + " pa.med_exp_date, pa.completed_by, pa.iv_status, pa.med_batch, pa.activity_status, "
      + " pa.prescription_id, pa.serving_remarks_id, pa.mod_time, pa.username "
      + " FROM patient_activities pa "
      + " join patient_prescription pp on (pp.patient_presc_id=pa.prescription_id) "
      + " join patient_medicine_prescriptions pmp on (pp.patient_presc_id=pmp.op_medicine_pres_id)"
      + " where pa.patient_id=? and pa.prescription_type='M' and " + " #PRISCRIPTIONIDFILTER# "
      + "   ((pa.due_date >= ? AND pa.due_date <= ?) or "
      + "     (pp.recurrence_daily_id = -2 AND pa.due_date <= now() "
      + "       AND pa.activity_status in ('S', 'P')) or "
      + "     (pp.recurrence_daily_id = -2 AND pa.completed_date >= ? "
      + "       AND pa.completed_date <= ? ) or "
      + "     (pmp.medication_type = 'IV' AND Exists (Select 1 from "
      + "       patient_iv_administered_details where "
      + "       activity_id=pa.activity_id)))"
      + " order by due_date";

  /**
   * Gets the medcations activities.
   *
   * @param patientId the patient id
   * @param from the from
   * @param to the to
   * @param prescriptionId prescription id
   * @return the medcations activities
   */
  public List<BasicDynaBean> getMedicationsActivities(String patientId, Timestamp from,
      Timestamp to, Integer prescriptionId) {

    String query = GET_MEDICATIONS_ACTIVITIES;
    if (prescriptionId != null) {
      query = query.replace("#PRISCRIPTIONIDFILTER#", "pa.prescription_id = ? AND ");
      return DatabaseHelper.queryToDynaList(query, patientId,prescriptionId, from, to, from,
          to);
    } else {
      query = query.replace("#PRISCRIPTIONIDFILTER#", "");
      return DatabaseHelper.queryToDynaList(query, patientId, from, to, from,
          to);
    }

  }

  /** The Constant GET_MEDICATION_ACTIVITIES_INFO. */
  private static final String GET_MEDICATION_ACTIVITIES_INFO =
      "SELECT count(1) as activitiy_count, max(pa.due_date) as due_date, "
      + " (select count(1) from patient_activities pa1 where pa1.completed_date >= CURRENT_DATE "
      + "   AND pa1.prescription_id = ? AND pa1.prescription_type='M' "
      + "   AND pa1.activity_status in ('S', 'P', 'D')) as "
      + "   current_day_activity_count from patient_activities pa "
      + "WHERE pa.prescription_id=? AND pa.prescription_type='M' "
      + " AND pa.activity_status in ('S', 'P', 'D')";

  /**
   * Gets the medication activitiys info.
   *
   * @param prescriptionId the prescription id
   * @return the medication activitiys info
   */
  public BasicDynaBean getMedicationActivitiysInfo(Integer prescriptionId) {
    Object[] queryParams = new Object[] {prescriptionId, prescriptionId};
    return DatabaseHelper.queryToDynaBean(GET_MEDICATION_ACTIVITIES_INFO, queryParams);
  }

  /**
   * Gets the iv administer details.
   *
   * @param activityIds the activity ids
   * @return the iv administer details
   */
  public List<BasicDynaBean> getIvAdministerDetails(List<Integer> activityIds) {
    MapSqlParameterSource parameters = new MapSqlParameterSource();
    parameters.addValue("activityIds", activityIds);
    return DatabaseHelper.queryToDynaList(
        "Select * from patient_iv_administered_details where activity_id in (:activityIds)",
        parameters);
  }

  private static final String GET_MAX_ACTIVITY_NUM = "select coalesce(max(activity_num),0)"
      + " from patient_activities where prescription_id = ? ";

  public Integer getMaxActivityNum(Integer precribedId) {
    return DatabaseHelper.getInteger(GET_MAX_ACTIVITY_NUM, precribedId);
  }
  
  /**
   * Check the count of existing activities beyond last setup date.
   *
   * @param prescriptionId the prescription id
   * @param lastSetupDateTime timestamp for last setup due date
   * @return the count of existing activities beyond last setup date.
   */
  public Integer checkIfExisitingActivitiesBeyondSetupDueDate(Integer prescriptionId,
      Timestamp lastSetupDateTime) {
    return DatabaseHelper.getInteger(
        "select count(*) from patient_activities where prescription_id = ? AND due_date > ?",
        prescriptionId, lastSetupDateTime);
  }

  private static final String DELETE_ACTIVITIES_BEYOND_SETUP_DUE_DATE =
      "DELETE from " + "patient_activities where prescription_id = ? AND due_date > ?";

  public boolean deleteActivitiesBeyondSetupDueDate(Integer prescriptionId,
      Timestamp lastSetupDateTime) {
    return DatabaseHelper.delete(DELETE_ACTIVITIES_BEYOND_SETUP_DUE_DATE,
        new Object[] {prescriptionId, lastSetupDateTime}) > 0;
  }

  private static final String GET_LAST_SAVED_SETUP_DUE_DATE =
      "select max(due_date) as last_setup_due_date"
          + " FROM patient_activities WHERE prescription_id=?";

  public BasicDynaBean getLastSavedSetupDueDate(Integer prescriptionId) {
    return DatabaseHelper.queryToDynaBean(GET_LAST_SAVED_SETUP_DUE_DATE,
        new Object[] {prescriptionId});
  }
}
