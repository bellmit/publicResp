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

public class DynamicAppointmentReminderDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory.getLogger(DynamicAppointmentReminderDataProvider.class);
  private static String THIS_NAME = "DynamicAppointmentReminder";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  final String fromTables = "from (SELECT sa.appointment_id as key, sa.mr_no as receipient_id__,"
      + " 'PATIENT' as receipient_type__, sa.waitlist::text, "
      + " sm.salutation as salutation_name, sa.patient_name as recipient_name,"
      + " coalesce(pd.email_id, contdet.patient_email_id) as recipient_email, "
      + " sa.patient_contact as recipient_mobile, "
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date,"
      + " date(sa.appointment_time)::text as appointment_date_yyyy_mm_dd, "
      + " to_char(sa.appointment_time, 'HH24:MI:SS') as appointment_time, "
      + " to_char(sa.appointment_time, 'HH12:MI AM') as appointment_time_12hr, "
      + " to_char(sa.appointment_time, 'yyyy-MM-dd') as appointment_time_search__, "
      + " sa.appointment_status as status, LOWER(sa.appointment_status) AS status_lower, "
      + " dr.doctor_name as appointment_doctor, "
      + " dep.dept_name AS doctor_department,dep.dept_id as department_id, "
      + " hcm.center_name,hcm.center_code,hcm.center_contact_phone AS center_phone,"
      + "hcm.center_address, sa.mr_no , "
      + " (select message_footer from message_types"
      + " where message_type_id='sms_appointment_confirmation') as message_footer, "
      + " case when cf.lang_code is not null then cf.lang_code else"
      + " (select contact_pref_lang_code from generic_preferences) end as lang_code "
      + " FROM scheduler_appointments sa "
      + " LEFT JOIN patient_details pd ON (sa.mr_no = pd.mr_no ) "
      + " LEFT JOIN contact_details contdet ON (contdet.contact_id = sa.contact_id) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " LEFT JOIN doctors dr ON (sa.prim_res_id = dr.doctor_id) "
      + " LEFT JOIN services s ON (s.service_id = sa.res_sch_name) "
      + " LEFT JOIN diagnostics d ON (d.test_id = sa.res_sch_name) "
      + " LEFT JOIN operation_master om ON (om.op_id = sa.res_sch_name) "
      + " LEFT JOIN department dep ON (dep.dept_id = dr.dept_id) "
      + " LEFT JOIN services_departments sdep ON (s.serv_dept_id = sdep.serv_dept_id) "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id = sa.center_id) "
      + " LEFT JOIN contact_preferences cf ON(sa.mr_no = cf.mr_no)  "
      + "  WHERE LOWER(sa.appointment_status) IN ('confirmed','booked') "
      + " ORDER BY appointment_date_yyyy_mm_dd,"
      + " appointment_time) as foo";

  public DynamicAppointmentReminderDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "recipient_name", "recipient_email", "recipient_mobile",
        "appointment_date", "appointment_date_yyyy_mm_dd", "appointment_doctor", "appointment_time",
        "appointment_time_12hr", "center_address", "center_code", "center_name", "center_phone",
        "department_id", "doctor_department", "lang_code", "mr_no", "salutation_name", "status",
        "waitlist"};
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
    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("appointment_no")) {
        String[] appointmentNo = new String[] { (String) eventData.get("appointment_no") };
        filter.put("key", appointmentNo);
        filter.put("key@type", new String[] { "text" });
        filter.put("key@cast", new String[] { "y" });
      }

      Map configParams = ctx.getConfigParams();
      String appointmentStatus = configParams.containsKey("status")
          ? configParams.get("status").toString() : "both";

      if (!appointmentStatus.equalsIgnoreCase("both")) {
        filter.put("status_lower", new String[] { appointmentStatus.toLowerCase() });
        filter.put("status_lower@type", new String[] { "text" });
      }
      addCriteriaFilter(filter);
    }
    return super.getMessageDataList(ctx);
  }
}