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
 * The Class AppointmentConfirmationDoctorDataProvider.
 */
public class AppointmentConfirmationDoctorDataProvider extends QueryDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(AppointmentConfirmationDoctorDataProvider.class);

  /** The this name. */
  private static String THIS_NAME = "Appointment Confirmation to Doctors";

  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";

  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";

  /** The from tables. */
  final String fromTables = "from (SELECT sa.appointment_id as key, sa.res_sch_id,"
      + " dr.doctor_id as receipient_id__, 'DOCTOR' as receipient_type__,"
      + " sa.patient_name , pd.name_local_language as patient_local_name, "
      + " dr.doctor_mobile as recipient_mobile, "
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date, "
      + "date(sa.appointment_time)::text as appointment_date_yyyy_mm_dd, "
      + " to_char(sa.appointment_time, 'HH24:MI:SS') as appointment_time, "
      + " to_char(sa.appointment_time, 'HH12:MI AM') as appointment_time_12hr, "
      + " sa.appointment_status as status, " + " dr.doctor_name as appointment_doctor, "
      + " dr.custom_field1_value as doctor_custom_field_1,"
      + " dr.custom_field2_value as doctor_custom_field_2 ,"
      + " dr.custom_field3_value as doctor_custom_field_3,"
      + " dr.custom_field4_value as doctor_custom_field_4,"
      + " dr.custom_field5_value as doctor_custom_field_5,"
      + " hcm.center_name, hcm.center_contact_phone AS center_phone,hcm.center_address, "
      + " (select message_footer from message_types"
      + " where message_type_id='sms_appointment_confirmation_for_doctor') as message_footer"
      + " FROM scheduler_appointments sa "
      + " LEFT JOIN patient_details pd ON (sa.mr_no = pd.mr_no ) "
      + " LEFT JOIN doctors dr ON (sa.prim_res_id = dr.doctor_id) "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id = sa.center_id) "
      + " ORDER BY appointment_date_yyyy_mm_dd, appointment_time) as foo";

  /**
   * Instantiates a new appointment confirmation doctor data provider.
   */
  public AppointmentConfirmationDoctorDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null, "appointment_doctor", false);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "recipient_mobile", "appointment_date",
        "appointment_date_yyyy_mm_dd", "appointment_doctor", "appointment_time",
        "appointment_time_12hr", "center_address", "center_name", "center_phone",
        "doctor_custom_field_1", "doctor_custom_field_2", "doctor_custom_field_3",
        "doctor_custom_field_4", "doctor_custom_field_5", "patient_local_name", "patient_name",
        "res_sch_id", "status" };
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
    // This will be available only if the message type is sms_appointment_confirmation
    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("appointment_id")) {
        String[] appointmentId = new String[] { eventData.get("appointment_id").toString() };
        filter.put("key", appointmentId);
        filter.put("key@type", new String[] { "text" });
        filter.put("key@cast", new String[] { "y" });
      }

      filter.put("res_sch_id", new String[] { "1" });
      filter.put("res_sch_id@type", new String[] { "text" });
      filter.put("res_sch_id@cast", new String[] { "y" });

      filter.put("status", new String[] { eventData.get("status").toString() });
      filter.put("status@type", new String[] { "text" });
      filter.put("status@cast", new String[] { "y" });
      addCriteriaFilter(filter);

    }
    return super.getMessageDataList(ctx);
  }

}
