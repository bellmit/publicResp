package com.insta.hms.core.clinical.patientactivities;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.clinical.consultation.prescriptions.PrescriptionsService;
import com.insta.hms.mdm.dailyrecurrences.RecurrenceDailyService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientActivitiesService.
 */
@Service
public class PatientActivitiesService {

  /** The patient activities repository. */
  @LazyAutowired
  private PatientActivitiesRepository patientActivitiesRepository;
  
  /** The recurrence daily service. */
  @LazyAutowired
  private RecurrenceDailyService recurrenceDailyService;
  
  /** The prescriptions service. */
  @LazyAutowired
  private PrescriptionsService prescriptionsService;

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return patientActivitiesRepository.getBean();
  }

  /**
   * Batch update.
   *
   * @param beans the beans
   * @param keys the keys
   * @return the int[]
   */
  public int[] batchUpdate(List<BasicDynaBean> beans, Map<String, Object> keys) {
    return patientActivitiesRepository.batchUpdate(beans, keys);
  }

  /**
   * Insert.
   *
   * @param bean the bean
   * @param patientId the patient id
   * @param userName the user name
   * @return the basic dyna bean
   * @throws ParseException the parse exception
   */
  public BasicDynaBean insert(BasicDynaBean bean, String patientId, String userName)
      throws ParseException {
    String prescType =
        prescriptionsService.getItemTypesShortForm().get((String) bean.get("presc_type"));
    java.sql.Timestamp startDateTime = (java.sql.Timestamp) bean.get("start_datetime");
    java.sql.Timestamp duedate = null;
    String freqType = (String) bean.get("freq_type");
    if (prescType.equals("C") || freqType.equals("R")) {
      duedate = startDateTime;
    } else {
      duedate = getNextDueDateTime((String) bean.get("freq_type"),
          (Integer) bean.get("recurrence_daily_id"), (Integer) bean.get("repeat_interval"),
          (String) bean.get("repeat_interval_units"), startDateTime);
    }
    BasicDynaBean activityBean = getBean();
    activityBean.set("activity_id", patientActivitiesRepository.getNextSequence());
    activityBean.set("patient_id", patientId);
    activityBean.set("activity_type", "P");
    activityBean.set("prescription_type", prescType);
    activityBean.set("prescription_id", bean.get("patient_presc_id"));
    activityBean.set("presc_doctor_id", bean.get("doctor_id"));
    activityBean.set("due_date", duedate);
    activityBean.set("activity_status", "Y".equals(bean.get("discontinued")) ? "X" : "P");
    activityBean.set("added_by", userName);
    activityBean.set("username", userName);
    activityBean.set("activity_num", 1);
    return patientActivitiesRepository.insert(activityBean) == 1 ? activityBean : null;
  }

  /**
   * Gets the next due date time.
   * checkDate is the date from which next due date is calculated.
   *
   * @param freqType the freq type
   * @param recurrenceDailyId the recurrence daily id
   * @param interval the interval
   * @param intervalUnits the interval units
   * @param checkDate the check date
   * @return the next due date time
   * @throws ParseException the parse exception
   */
  public Timestamp getNextDueDateTime(String freqType, Integer recurrenceDailyId, Integer interval,
      String intervalUnits, java.sql.Timestamp checkDate) throws ParseException {
    if (freqType.equals("F")) {
      Map<String, Object> params = new HashMap<>();
      params.put("recurrence_daily_id", recurrenceDailyId);
      BasicDynaBean recurrenceBean = recurrenceDailyService.findByPk(params);
      String timingStr = ((String) recurrenceBean.get("timings"));
      if (timingStr == null || timingStr.equals("")) {
        return checkDate;
      }

      List<String> timings = Arrays.asList(((String) recurrenceBean.get("timings")).split(","));
      for (int i = 0; i < timings.size(); i++) {
        timings.set(i, timings.get(i).trim());
      }
      Collections.sort(timings);
      SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
      String checkDateTime = timeFormat.format(checkDate).toString();
      Integer index = -1;
      for (int i = 0; i < timings.size(); i++) {
        if (checkDateTime.compareTo(timings.get(i)) < 0) {
          index = i;
          break;
        }
      }
      Date nextDueDate = null;
      SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yyyy");
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
      if (index == -1) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(checkDate);
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        nextDueDate = sdf.parse(sd.format(cal.getTime()) + " " + timings.get(0));
      } else {
        nextDueDate = sdf.parse(sd.format(checkDate) + " " + timings.get(index));
      }
      return new java.sql.Timestamp(nextDueDate.getTime());
    } else {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(checkDate);
      if (interval != null && interval != 0) {
        if (intervalUnits.equals("M")) {
          calendar.add(Calendar.MINUTE, interval);
        }
        if (intervalUnits.equals("H")) {
          calendar.add(Calendar.HOUR, interval);
        }
        if (intervalUnits.equals("D")) {
          calendar.add(Calendar.DATE, interval);
        }
      }

      return new java.sql.Timestamp(calendar.getTimeInMillis());
    }
  }

  /**
   * Cancel activity.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   */
  public boolean cancelActivity(int prescriptionId, String prescriptionType) {
    return patientActivitiesRepository.cancelActivity(prescriptionId, prescriptionType);
  }


  /**
   * Cancel activity without due date check.
   *
   * @param prescriptionId the prescription id
   * @return true, if successful
   */
  public boolean cancelActivity(int prescriptionId) {
    return cancelActivity(prescriptionId,null);
  }

  /**
   * Delete incomplete activity.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   */
  public boolean deleteIncompleteActivity(int prescriptionId, String prescriptionType) {
    return patientActivitiesRepository.deleteIncompleteActivity(prescriptionId, prescriptionType);
  }

  /**
   * Completed activities exists.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return true, if successful
   */
  public boolean completedActivitiesExists(int prescriptionId, String prescriptionType) {
    return patientActivitiesRepository.completedActivitiesExists(prescriptionId, prescriptionType);
  }

  /**
   * Gets the pending activity.
   *
   * @param prescriptionId the prescription id
   * @param prescriptionType the prescription type
   * @return the pending activity
   */
  public BasicDynaBean getPendingActivity(int prescriptionId, String prescriptionType) {
    return patientActivitiesRepository.getPendingActivity(prescriptionId, prescriptionType);
  }

  /**
   * Gets the medications activities.
   *
   * @param patientId the patient id
   * @param from the from
   * @param to the to
   * @return the mar activities
   */
  public List<BasicDynaBean> getMedicationsActivities(String patientId, Timestamp from,
      Timestamp to) {
    return patientActivitiesRepository.getMedicationsActivities(patientId, from, to, null);
  }

}
