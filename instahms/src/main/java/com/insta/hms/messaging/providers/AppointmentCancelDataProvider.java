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
 * The Class AppointmentCancelDataProvider.
 */
public class AppointmentCancelDataProvider extends QueryDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(AppointmentCancelDataProvider.class);

  /** The this name. */
  private static String THIS_NAME = "Appointment Cancelled";

  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";

  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";

  /** The from tables. */
  final String fromTables = "from (SELECT sa.appointment_id as key,"
      + " sa.appointment_status as status, "
      + " sa.mr_no as mr_no,sm.salutation as salutation_name, sa.patient_name,"
      + " pd.name_local_language as patient_local_name, "
      + " sa.patient_contact, sa.patient_contact as recipient_mobile,"
      + " coalesce(pd.email_id, contdet.patient_email_id) as recipient_email, "
      + " dr.doctor_name as appointment_doctor," + " dep.dept_name AS doctor_department,"
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date,"
      + " to_char(sa.appointment_time, 'HH12:MI AM') as appointment_time, "
      + " service_name,hcm.center_name, hcm.center_code, sa.cancel_reason, "
      + " dr.custom_field1_value as doctor_custom_field_1,"
      + " dr.custom_field2_value as doctor_custom_field_2 ,"
      + " dr.custom_field3_value as doctor_custom_field_3,"
      + " dr.custom_field4_value as doctor_custom_field_4,"
      + " dr.custom_field5_value as doctor_custom_field_5,"
      + " coalesce(hcm.center_contact_phone,'') AS center_phone,"
      + " coalesce(hcm.center_address,'') as center_address, "
      + " sa.mr_no as receipient_id__, '' as receipient_type__,"
      + " to_char(sa.appointment_time, 'yyyy-MM-dd') as appointment_time_search__, sa.appt_token , "
      + " (select message_footer from message_types where"
      + " message_type_id='sms_appointment_cancellation') as message_footer, "
      + " case when cf.lang_code is not null then cf.lang_code else"
      + " (select contact_pref_lang_code from generic_preferences) end as lang_code, "
      + " dr.specialization as doctor_specialization " + " FROM scheduler_appointments sa "
      + " LEFT JOIN patient_details pd ON (sa.mr_no = pd.mr_no ) "
      + " LEFT JOIN contact_details contdet ON (contdet.contact_id = sa.contact_id) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " LEFT JOIN doctors dr ON (sa.prim_res_id = dr.doctor_id) "
      + " LEFT JOIN services s ON (s.service_id = sa.res_sch_name) "
      + " LEFT JOIN diagnostics d ON (d.test_id = sa.res_sch_name) "
      + " LEFT JOIN operation_master om ON (om.op_id = sa.res_sch_name) "
      + " LEFT JOIN department dep ON (dep.dept_id = dr.dept_id) "
      + " LEFT JOIN services_departments sdep ON (s.serv_dept_id = sdep.serv_dept_id) "
      + " LEFT JOIN diagnostics_departments ddep ON (d.ddept_id = ddep.ddept_id) "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id = sa.center_id) "
      + " LEFT JOIN contact_preferences cf ON(sa.mr_no = cf.mr_no)  "
      + " ORDER BY appointment_date, appointment_time) as foo";

  /**
   * Instantiates a new appointment cancel data provider.
   */
  public AppointmentCancelDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "recipient_email", "recipient_mobile", "appointment_date",
        "appointment_doctor", "appointment_time", "appt_token", "cancel_reason", "center_address",
        "center_code", "center_name", "center_phone", "doctor_custom_field_1",
        "doctor_custom_field_2", "doctor_custom_field_3", "doctor_custom_field_4",
        "doctor_custom_field_5", "doctor_department", "doctor_specialization", "lang_code", "mr_no",
        "patient_contact", "patient_local_name", "patient_name", "salutation_name",
        "status","service_name" };
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
    }
    return super.getMessageDataList(ctx);
  }
}
