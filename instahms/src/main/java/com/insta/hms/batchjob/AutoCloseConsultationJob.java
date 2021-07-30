package com.insta.hms.batchjob;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * @author teja
 *
 *         The Class AutoCloseConsultation.
 *
 */
public class AutoCloseConsultationJob extends SQLUpdateJob {

  /** The clinical preferences service. */
  @LazyAutowired
  private ClinicalPreferencesService clinicalPreferencesService;

  /** The Constant CLOSE_OPEN_CONSULTATION_LIMIT. */
  private static final Integer CLOSE_OPEN_CONSULTATION_LIMIT = 150;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.batchjob.SQLUpdateJob#executeInternal(org.quartz.JobExecutionContext)
   */
  @Override
  public void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
    setJobConnectionDetails();
    closePartialConsultations();
    closeOpenConsultations();
  }

  /** The Constant CLOSE_PARTIAL_CONSULTATIONS. */
  private static final String CLOSE_PARTIAL_CONSULTATIONS = "UPDATE doctor_consultation "
      + " SET status='C', consultation_complete_time=?, consultation_mod_time=?, "
      + " username='_system'  WHERE status='P' "
      + "  AND presc_date::timestamp(0) <= localtimestamp(0) - interval '#hours# hours' ";

  /**
   * Close partial consultations.
   */
  private void closePartialConsultations() {
    @SuppressWarnings("unchecked")
    Map<String, Object> clinicalPreferences = clinicalPreferencesService.getClinicalPreferences()
        .getMap();
    Timestamp now = new Timestamp(new Date().getTime());
    if ("Y".equals(clinicalPreferences.get("op_consultation_auto_closure"))) {
      DatabaseHelper.update(
          CLOSE_PARTIAL_CONSULTATIONS.replace("#hours#", (String) clinicalPreferencesService
              .getClinicalPreferences().get("op_consultation_auto_closure_period").toString()),
          now, now);
    }
  }

  /** The Constant CLOSE_OPEN_CONSULTATIONS. */
  private static final String CLOSE_OPEN_CONSULTATIONS = "SELECT auto_close_consultation(?)"
      + " as count";

  /**
   * Close open consultations.
   */
  private void closeOpenConsultations() {
    int limit = CLOSE_OPEN_CONSULTATION_LIMIT;
    while (limit == CLOSE_OPEN_CONSULTATION_LIMIT) {
      limit = DatabaseHelper.getInteger(CLOSE_OPEN_CONSULTATIONS, CLOSE_OPEN_CONSULTATION_LIMIT);
    }
  }

}
