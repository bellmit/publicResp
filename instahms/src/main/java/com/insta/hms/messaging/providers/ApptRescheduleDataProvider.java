package com.insta.hms.messaging.providers;

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

// TODO: Auto-generated Javadoc
/**
 * The Class ApptRescheduleDataProvider.
 */
public class ApptRescheduleDataProvider extends QueryDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(ApptRescheduleDataProvider.class);

  /** The this name. */
  private static String THIS_NAME = "Appointment Booked Patient ";

  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";

  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";

  /** The Constant fromTables. */
  private static final String fromTables = "FROM (SELECT sa.appointment_id as key, "
      + " sa.salutation_name, sa.patient_name as receipient_name,"
      + " coalesce(pd.email_id, contdet.patient_email_id) as recipient_email, "
      + " sa.mr_no as receipient_id__ , 'PATIENT' as receipient_type__,"
      + " sa.patient_contact as recipient_mobile, sa.waitlist::text, "
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date , "
      + " to_char(sa.appointment_time, 'HH24:MI:SS') as appointment_time ,"
      + " to_char(sa.appointment_time, 'HH12:MI AM') as appointment_time_12hr, "
      + " date(sa.appointment_time)::text as appointment_date_yyyy_mm_dd, "
      + " sa.appointment_status as status, d.doctor_name as appointment_doctor, "
      + " hcm.center_name, hcm.center_code, hcm.center_contact_phone AS  center_phone,"
      + " hcm.center_address, sa.appt_token , " + " (select message_footer from message_types"
      + " where message_type_id='sms_appointment_reschedule') as message_footer, "
      + " case when cp.lang_code is not null then cp.lang_code"
      + " else (select contact_pref_lang_code from generic_preferences) end as lang_code, "
      + " d.specialization as doctor_specialization "
      + " FROM scheduler_appointments sa left join contact_preferences cp on (sa.mr_no = cp.mr_no)"
      + " LEFT JOIN contact_details contdet ON (contdet.contact_id = sa.contact_id) "
      + " LEFT JOIN patient_details pd ON (sa.mr_no = pd.mr_no ) "
      + ","
      + " doctors d, hospital_center_master hcm " + " WHERE hcm.center_id = sa.center_id "
      + " AND res_sch_id = 1 " + " AND sa.prim_res_id = d.doctor_id "
      + " AND sa.appointment_status IN ('Booked', 'Confirmed')"
      + " order by appointment_date_yyyy_mm_dd ) as foo";

  /**
   * Instantiates a new appt reschedule data provider.
   */
  public ApptRescheduleDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null, "appointment_date_yyyy_mm_dd",
        false);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
        "appointment_date", "appointment_date_yyyy_mm_dd", "appointment_doctor", "appointment_time",
        "appointment_time_12hr", "appt_token", "center_address", "center_code", "center_name",
        "center_phone", "doctor_specialization", "lang_code", "salutation_name", "status",
        "waitlist" };
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
    Map eventData = ctx.getEventData();
    Map filter = new HashMap();
    if (null != eventData) {

      if (null != eventData.get("appointment_id")) {
        String[] appointmentId = new String[] { eventData.get("appointment_id").toString() };
        filter.put("key", appointmentId);
        filter.put("key@type", new String[] { "integer" });
      }

      // TODO: need to add a filter based on the status of the appointment

      addCriteriaFilter(filter);
      addSecondarySort("appointment_time");
    }
    return super.getMessageDataList(ctx);
  }
}
