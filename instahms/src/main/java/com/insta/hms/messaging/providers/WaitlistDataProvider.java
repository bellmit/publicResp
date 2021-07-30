package com.insta.hms.messaging.providers;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.MessageContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaitlistDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory.getLogger(PatientDueDataProvider.class);
  private static String THIS_NAME = "Waitlist";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  /** The from tables. */
  String fromTables = "from (SELECT sa.appointment_id as key, sa.mr_no as "
      + "receipient_id__, 'PATIENT' as receipient_type__," + " sm.salutation as "
      + "salutation_name, sa.patient_name as receipient_name,"
      + " coalesce(pd.email_id, contdet.patient_email_id) as recipient_email,"
      + " pd.name_local_language as patient_local_name, sa.patient_contact as "
      + "recipient_mobile,sa.waitlist::text, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as "
      + "appointment_date, date(sa.appointment_time)::text as appointment_date_yyyy_mm_dd, "
      + " to_char(sa.appointment_time, 'HH24:MI:SS') as appointment_time, " + " to_char(sa"
      + ".appointment_time, 'HH12:MI AM') as appointment_time_12hr, " + " to_char(sa"
      + ".appointment_time, 'yyyy-MM-dd') as appointment_time_search__, " + " sa"
      + ".appointment_status as status, dr.doctor_name as appointment_doctor," + " dep"
      + ".dept_name AS doctor_department,dep.dept_id as department_id, service_name, sdep"
      + ".department AS service_department , " + " dr.custom_field1_value as "
      + "doctor_custom_field_1, dr.custom_field2_value as doctor_custom_field_2 , dr"
      + ".custom_field3_value as doctor_custom_field_3," + " dr.custom_field4_value as "
      + "doctor_custom_field_4, dr.custom_field5_value as doctor_custom_field_5," + " "
      + "test_name , ddep.ddept_name AS diagnostics_department, "
      + "operation_name as surgery_procedure_name," + " hcm"
      + ".center_name,hcm.center_code,hcm.center_contact_phone AS center_phone,hcm"
      + ".center_address, sa.mr_no , " + " s.serv_dept_id as services_department_id,d"
      + ".ddept_id as diagnostics_department_id, sa.appt_token , " + " (select message_footer"
      + " from message_types where message_type_id='sms_appointment_confirmation') as "
      + "message_footer, " + " case when cf.lang_code is not null then cf.lang_code else "
      + "(select contact_pref_lang_code from generic_preferences) end as lang_code, " + " dr"
      + ".specialization as doctor_specialization " + " FROM scheduler_appointments sa " + " "
      + "LEFT JOIN patient_details pd ON (sa.mr_no = pd.mr_no ) " + " LEFT JOIN "
      + "salutation_master sm ON (pd.salutation = sm.salutation_id) " + " LEFT JOIN doctors "
      + "dr ON (sa.prim_res_id = dr.doctor_id) " + " LEFT JOIN services s ON (s.service_id ="
      + " sa.res_sch_name) " + " LEFT JOIN diagnostics d ON (d.test_id = sa.res_sch_name) "
      + " LEFT JOIN operation_master om ON (om.op_id = sa.res_sch_name) " + " LEFT JOIN "
      + "department dep ON (dep.dept_id = dr.dept_id) " + " LEFT JOIN services_departments "
      + "sdep ON (s.serv_dept_id = sdep.serv_dept_id) " + " LEFT JOIN diagnostics_departments"
      + " ddep ON (d.ddept_id = ddep.ddept_id) " + " LEFT JOIN hospital_center_master hcm ON"
      + "(hcm.center_id = sa.center_id) " + " LEFT JOIN contact_preferences cf ON(sa.mr_no = "
      + "cf.mr_no) " + " LEFT JOIN contact_details contdet ON (contdet.contact_id = sa.contact_id) "
      + "left join patient_communication_preferences pcpref on (sa.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + " ORDER BY appointment_date_yyyy_mm_dd, appointment_time) as foo";

  public WaitlistDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  /** The tokens. */
  String[] tokens = new String[] { "salutation_name", "receipient_name", "recipient_email",
      "recipient_mobile", "appointment_date", "appointment_date_yyyy_mm_dd", "appointment_time",
      "appointment_time_12hr", "status", "appointment_doctor", "doctor_department", "department_id",
      "service_name", "service_department", "test_name", "diagnostics_department",
      "surgery_procedure_name", "center_name", "center_code", "center_phone", "center_address",
      "doctor_custom_field_1", "doctor_custom_field_2", "doctor_custom_field_3",
      "doctor_custom_field_4", "doctor_custom_field_5", "mr_no", "services_department_id",
      "diagnostics_department_id", "appt_token", "message_footer", "lang_code",
      "patient_local_name", "doctor_specialization", "waitlist" };

  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = null;
    if (null == tokenList) {
      tokenList = new ArrayList<String>();
    }
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
      if (null != eventData.get("appointment_ids")) {
        String[] appointmentId = (String[]) eventData.get("appointment_ids");
        filter.put("key", appointmentId);
        filter.put("key@type", new String[] { "text" });
        filter.put("key@cast", new String[] { "y" });
      }

      addCriteriaFilter(filter);
    }
    GenericDAO messageTypesDao = new GenericDAO("message_types");
    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id",
        (String) ctx.getMessageType().get("message_type_id"));
    String messageGroupName = (String) messageTypeBean.get("message_group_name");
    fromTables = fromTables.replace("#", messageGroupName);
    setQueryParams(selectFields, selectCount, fromTables, null);

    String messageMode = (String) messageTypeBean.get("message_mode");
    String mode = messageMode.equalsIgnoreCase("sms") ? "sms" : "email";
    Map modeFilter = new HashMap();
    modeFilter.put("send_" + mode, new String[] { "Y" });
    modeFilter.put("send_" + mode + "@type", new String[] { "text" });
    addCriteriaFilter(modeFilter);

    return super.getMessageDataList(ctx);
  }
}