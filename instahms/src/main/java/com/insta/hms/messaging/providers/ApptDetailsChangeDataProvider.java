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
 * The Class ApptDetailsChangeDataProvider.
 *
 * @author Anil N
 */
public class ApptDetailsChangeDataProvider extends QueryDataProvider {
  
  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(ApptDetailsChangeDataProvider.class);
  
  /** The this name. */
  private static String THIS_NAME = "Appointment Details Change";
  
  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";
  
  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";
  
  /** The Constant fromTables. */
  private static final String fromTables = "from (SELECT sa.appointment_id as key, "
      + " sm.salutation as salutation_name, sa.patient_name as recipient_name,"
      + " coalesce(pd.email_id, contdet.patient_email_id) as recipient_email, "
      + " sa.mr_no as receipient_id__ , '' as receipient_type__,"
      + " sa.patient_contact as recipient_mobile, sa.waitlist::text, "
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date , "
      + " to_char(sa.appointment_time, 'HH24:MI:SS') as appointment_time ,"
      + " to_char(sa.appointment_time, 'HH12:MI AM') as appointment_time_12hr, "
      + " sa.appointment_status as status, " + " dr.doctor_name as appointment_doctor,"
      + " dep.dept_name AS doctor_department,"
      + " hcm.center_name, hcm.center_id, hcm.center_contact_phone as center_phone, "
      + " hcm.center_address, hcm.center_code, sa.appt_token , "
      + " (select message_footer from message_types"
      + " where message_type_id='sms_appointment_details_change') as message_footer, "
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
   * Instantiates a new appt details change data provider.
   */
  public ApptDetailsChangeDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "recipient_name", "recipient_email", "recipient_mobile",
        "appointment_date", "appointment_doctor", "appointment_time", "appointment_time_12hr",
        "appt_token", "center_address", "center_code", "center_id", "center_name", "center_phone",
        "doctor_department", "doctor_specialization", "lang_code", "salutation_name", "status", 
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
    }
    return super.getMessageDataList(ctx);
  }

}
