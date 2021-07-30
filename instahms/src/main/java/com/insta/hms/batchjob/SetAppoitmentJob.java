package com.insta.hms.batchjob;

import com.insta.hms.batchjob.pushevent.Events;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.security.SecurityService;
import com.insta.hms.core.scheduler.AppointmentService;
import com.insta.hms.jobs.JobService;
import com.insta.hms.resourcescheduler.PractoBookHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;

/**
 * # # This script marks all appointments which are scheduled for a time < (current_time - 2 hours)
 * # as "No Show". This is to be run after midnight to close all such appointments. # #
 *
 *
 * @author insta
 *
 */
public class SetAppoitmentJob extends SQLUpdateJob {

  @LazyAutowired
  private SecurityService securityService;
  
  @LazyAutowired
  private JobService jobService;
  
  @LazyAutowired
  private AppointmentService appointmentService;

  private static Logger logger = LoggerFactory.getLogger(SetAppoitmentJob.class);

  private static final String QUERY_APP_NO_SHOW = "Select * "
      + " from scheduler_appointments WHERE appointment_status"
      + " IN ('Booked','Confirmed') AND "
      + " appointment_time < (current_timestamp - interval '2 Hours')";

  private static final String QUERY_FOR_UNIQUE_APPID = "UPDATE scheduler_appointments "
      + " SET appointment_status='Noshow',unique_appt_ind = "
      + " nextval('unique_appt_ind_seq'), changed_by='auto_update', "
      + "changed_time=current_timestamp WHERE appointment_status IN ('Booked','Confirmed') "
      + " AND appointment_time < (current_timestamp - interval '2 Hours')"
      + "AND unique_appt_ind = 0 ";

  private static final String QUERY_FOR_APPID = "UPDATE scheduler_appointments "
      + " SET appointment_status='Noshow', changed_by='auto_update', "
      + "changed_time=current_timestamp WHERE appointment_status IN ('Booked','Confirmed') "
      + " AND appointment_time < (current_timestamp - interval '2 Hours')"
      + "AND unique_appt_ind != 0 ";

  @Override
  protected List<String> getQueryList() {
    List<String> queryList = new ArrayList<>();
    queryList.add(QUERY_FOR_UNIQUE_APPID);
    queryList.add(QUERY_FOR_APPID);
    return queryList;
  }

  @Override
  public void executeInternal(JobExecutionContext jobContext) throws JobExecutionException {
    try {
      List<BasicDynaBean> appList;
      appList = DatabaseHelper.queryToDynaList(QUERY_APP_NO_SHOW);
      super.executeInternal(jobContext);
      if (appList.size() > 0) {
        // Sending the 'No Show' status to Practo Book
        boolean modPractoAdvantage = (securityService.getActivatedModules())
            .contains("mod_practo_advantage");
        
        String[] apptIds = new String[appList.size()];
        int inc = 0;
        for (BasicDynaBean bean : appList) {
          Integer appointmentId = (Integer) bean.get("appointment_id");
          apptIds[inc++] = appointmentId.toString();
          if (modPractoAdvantage) {
            PractoBookHelper.addDoctorAppointmentsToPracto(appointmentId, false);
          }
        }
        appointmentService.schedulePushEvent(apptIds, Events.APPOINTMENT_NOSHOW);
      }
    } catch (Exception exception) {
      logger.error("error in SetAppoitmentJob : " + exception);
    }
  }
  
}
