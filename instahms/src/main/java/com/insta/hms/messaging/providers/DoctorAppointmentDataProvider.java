package com.insta.hms.messaging.providers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class DoctorAppointmentDataProvider.
 */
public class DoctorAppointmentDataProvider extends QueryDataProvider {
  
  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(DoctorAppointmentDataProvider.class);
  
  /** The this name. */
  private static String THIS_NAME = "Doctors with Appointments ";
  
  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";
  
  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";
  
  /** The Constant fromTables. */
  private static final String fromTables = "from (SELECT d.doctor_id as key, "
      + "'DOCTOR' as receipient_type__, d.doctor_id as receipient_id__,"
      + "d.doctor_name as receipient_name, d.doctor_mail_id as recipient_email, "
      + "d.doctor_mobile as recipient_mobile, "
      + "to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date, "
      + "date(sa.appointment_time)::text as appointment_date_yyyy_mm_dd, "
      + "to_char(sa.appointment_time, 'yyyy-MM-dd') as appointment_time_search__,  "
      + "count(sa.appointment_id)::text as total_appointments, " + "sa.salutation_name, "
      + "textcat_commacat(pg_catalog.time(sa.appointment_time)::text || ' - ' || "
      + "sa.patient_name) as _appointment_details, "
      + "hcm.center_name,hcm.center_code,hcm.center_contact_phone AS center_phone,"
      + "hcm.center_address, dep.dept_name AS doctor_department, sa.appt_token , "
      + " (select message_footer from message_types "
      + "where message_type_id='sms_doctor_appointments') as message_footer"
      + " FROM scheduler_appointments sa, scheduler_appointment_items sai, doctors d,"
      + " hospital_center_master hcm, department dep " + "WHERE hcm.center_id = sa.center_id  and "
      + "sa.appointment_id = sai.appointment_id and dep.dept_id = d.dept_id "
      + "and sai.resource_type = 'OPDOC' and "
      + "sai.resource_id = d.doctor_id and sa.appointment_status IN ('Booked', 'Confirmed') "
      + "GROUP BY d.doctor_id, sa.mr_no, d.doctor_name, d.doctor_mail_id, d.doctor_mobile,"
      + " appointment_date_yyyy_mm_dd, "
      + "appointment_date, appointment_time_search__, hcm.center_name, hcm.center_code, "
      + "hcm.center_contact_phone , hcm.center_address, dep.dept_name, "
      + "sa.salutation_name, sa.appt_token "
      + "order by appointment_date_yyyy_mm_dd, receipient_name) as foo";

  /**
   * Instantiates a new doctor appointment data provider.
   */
  public DoctorAppointmentDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null, "appointment_date_yyyy_mm_dd",
        false);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
        "_appointment_details", "appointment_date", "appointment_date_yyyy_mm_dd", "appt_token",
        "center_address", "center_code", "center_name", "center_phone", "doctor_department",
        "salutation_name", "total_appointments" };
    List<String> tokenList = new ArrayList<>();
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    Collections.sort(tokenList);
    return tokenList;
  }
  
  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map configParams = ctx.getConfigParams();
    if (null != configParams) {
      java.util.Date startDate = DateUtil.getCurrentDate();
      java.util.Date endDate = DateUtil.getCurrentDate();

      String duePeriod = (String) configParams.get("due_period");
      if (null != duePeriod && !duePeriod.isEmpty()) {
        Integer dueDays = Integer.parseInt(duePeriod);
        if (dueDays > 0) {
          java.util.Date refDate = (java.util.Date) DateUtil.getExpectedDate(dueDays, "D", false,
              true);
          endDate = new java.sql.Date(refDate.getTime());
        }
      }

      Map dateFilter = new HashMap();
      DateUtil dateUtil = new DateUtil();
      String strStartDate = dateUtil.getSqlDateFormatter().format(startDate);
      String strEndDate = dateUtil.getSqlDateFormatter().format(endDate);
      dateFilter.put("appointment_time_search__", new String[] { strStartDate, strEndDate });
      dateFilter.put("appointment_time_search__@op", new String[] { "ge,le" });
      dateFilter.put("appointment_time_search__@type", new String[] { "text" });
      dateFilter.put("appointment_time_search__@cast", new String[] { "y" });

      logger.debug("dates :" + strStartDate + "::" + strEndDate);
      addCriteriaFilter(dateFilter);
    }
    return super.getMessageDataList(ctx);
  }
}
