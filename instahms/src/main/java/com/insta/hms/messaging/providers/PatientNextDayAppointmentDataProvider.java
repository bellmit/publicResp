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

/**
 *
 * @author mohammed.r
 *
 */
public class PatientNextDayAppointmentDataProvider extends QueryDataProvider {

  static Logger logger = LoggerFactory
      .getLogger(PatientNextDayAppointmentDataProvider.class);
  private static String THIS_NAME = "Patient Appointment Reminder";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private String fromTables = "from (SELECT sa.appointment_id as key, "
      + "sa.mr_no as receipient_id__, 'PATIENT' as receipient_type__,"
      + " sa.patient_name as receipient_name, sa.waitlist::text, "
      + " coalesce(pd.email_id, contdet.patient_email_id) as recipient_email, "
      + " sa.patient_contact as recipient_mobile, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + " to_char(sa.appointment_time, 'DD-MM-YYYY') as appointment_date, "
      + "date(sa.appointment_time)::text as appointment_date_yyyy_mm_dd, "
      + " to_char(sa.appointment_time, 'HH24:MI:SS') as appointment_time, "
      + " to_char(sa.appointment_time, 'HH12:MI AM') as appointment_time_12hr, "
      + " to_char(sa.appointment_time, 'yyyy-MM-dd') as appointment_time_search__, "
      + " sa.appointment_status as status, LOWER(sa.appointment_status) AS status_lower,"
      + " dr.doctor_name as appointment_doctor, "
      + " dep.dept_name AS doctor_department, "
      + " dep.dept_id as department_id, service_name, "
      + " sdep.department AS service_department , "
      + " test_name , ddep.ddept_name AS diagnostics_department, "
      + " operation_name as surgery_procedure_name , "
      + " odep.dept_name AS surgery_procedure_department, "
      + " hcm.center_name,hcm.center_code,hcm.center_contact_phone AS center_contact_phone,"
      + " hcm.center_address, sa.mr_no , "
      + " s.serv_dept_id as services_department_id,d.ddept_id as diagnostics_department_id , "
      + " (select message_footer from message_types "
      + "where message_type_id='sms_next_day_appointment_reminder') as message_footer, "
      + " case when cf.lang_code is not null then " + "cf.lang_code else ("
      + " select contact_pref_lang_code from generic_preferences) end as lang_code, "
      + " dr.specialization as doctor_specialization " + " FROM scheduler_appointments sa "
      + " LEFT JOIN patient_details pd ON (sa.mr_no = pd.mr_no ) "
      + " LEFT JOIN contact_details contdet ON (contdet.contact_id = sa.contact_id) "
      + " LEFT JOIN doctors dr ON (sa.prim_res_id = dr.doctor_id) "
      + " LEFT JOIN services s ON (s.service_id = sa.res_sch_name) "
      + " LEFT JOIN diagnostics d ON (d.test_id = sa.res_sch_name) "
      + " LEFT JOIN operation_master om ON (om.op_id = sa.res_sch_name) "
      + " LEFT JOIN department dep ON (dep.dept_id = dr.dept_id) "
      + " LEFT JOIN department odep ON (odep.dept_id = om.dept_id) "
      + " LEFT JOIN services_departments sdep ON (s.serv_dept_id = sdep.serv_dept_id) "
      + " LEFT JOIN diagnostics_departments ddep ON (d.ddept_id = ddep.ddept_id) "
      + " LEFT JOIN hospital_center_master hcm ON(hcm.center_id = sa.center_id) "
      + " LEFT JOIN appointment_source_master casm on(sa.app_source_id=casm.appointment_source_id) "
      + " LEFT JOIN contact_preferences cf ON(sa.mr_no = cf.mr_no)  "
      + "left join patient_communication_preferences pcpref on (sa.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + " WHERE to_char(sa.appointment_time, 'YYYY-MM-DD') = date(current_date+1)::text "
      + " AND LOWER(appointment_status) IN ('confirmed','booked') "
      + " AND (casm.appointment_source_name not ilike 'Practo' "
      + " OR casm.appointment_source_name IS NULL) "
      + " ORDER BY appointment_date_yyyy_mm_dd, appointment_time) as foo";

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
        "appointment_date", "appointment_date_yyyy_mm_dd", "appointment_time",
        "appointment_time_12hr", "status", "appointment_doctor", "doctor_department",
        "service_name", "service_department", "test_name", "diagnostics_department",
        "surgery_procedure_name", "center_name", "center_code", "center_contact_phone",
        "center_address", "mr_no", "doctor_specialization", "waitlist",
        "surgery/procedure_department" };
    List<String> tokenList = new ArrayList<>();
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    Collections.sort(tokenList);
    return tokenList;
  }
  
  public PatientNextDayAppointmentDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    GenericDAO messageTypesDao = new GenericDAO("message_types");
    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id",
        (String) ctx.getMessageType().get("message_type_id"));
    String messageGroupName = (String) messageTypeBean.get("message_group_name");
    fromTables = fromTables.replace("#", messageGroupName);
    setQueryParams(selectFields, selectCount, fromTables, null);
    
    String messageMode = (String) messageTypeBean.get("message_mode");
    String mode = messageMode.equalsIgnoreCase("sms") ? "sms" : "email";
    Map configParams = ctx.getConfigParams();
    String appointmentStatus = configParams.containsKey("status") 
        ? configParams.get("status").toString() : "both";
    Map<String,Object> modeFilter = new HashMap();
    modeFilter.put("send_" + mode, new String[] { "Y" });
    modeFilter.put("send_" + mode + "@type", new String[] { "text" });
    if (!appointmentStatus.equalsIgnoreCase("both")) {
      modeFilter.put("status_lower", new String[] { appointmentStatus.toLowerCase() });
      modeFilter.put("status_lower@type", new String[] { "text" });
    }
    addCriteriaFilter(modeFilter);
    return super.getMessageDataList(ctx);
  }

}
