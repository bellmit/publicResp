package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AppointmentPlannerDataProvider extends QueryDataProvider {

  private static final String THIS_NAME = "Appointment Planner";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  String fromTables = "from (SELECT COALESCE(pd.patient_name,'') as patient_name, " + "case "
      + "when cp.lang_code is not null then cp.lang_code else (select contact_pref_lang_code "
      + "from generic_preferences) end as lang_code, " + "COALESCE(pd.mr_no,'') as "
      + "receipient_id__ ,'PATIENT' as receipient_type__, pd.patient_phone as "
      + "recipient_mobile ," + "string_agg(to_char(papd.plan_visit_date, 'DD-MM-YYYY'),',') "
      + "as appointment_date_list, " + "string_agg(COALESCE(to_char(sa.appointment_time, "
      + "'HH24:MI:SS'), 'N/A'),',') as appointment_time_list, " + "string_agg(COALESCE"
      + "(to_char(sa.appointment_time, 'HH12:MI AM'), 'N/A'),',') as "
      + "appointment_time_12hr_list, " + "string_agg(COALESCE(sa.appointment_status, 'N/A'),"
      + "',') as appointment_status_list, " + "string_agg(CASE  " + "WHEN sm.res_sch_category"
      + " = 'DOC' THEN 'Doctor'  " + "WHEN sm.res_sch_category = 'SNP' THEN 'Service'  "
      + "WHEN sm.res_sch_category = 'DIA' THEN 'Test'   " + "WHEN sm.res_sch_category = 'OPE'"
      + " THEN 'Operation' END,',') AS resource_type_list , " + "string_agg(CASE  " + "WHEN "
      + "sm.res_sch_category = 'DOC' THEN (SELECT doctor_name FROM DOCTORS WHERE "
      + "doctor_id=papd.secondary_resource_id)  " + "WHEN sm.res_sch_category = 'SNP' THEN "
      + "(SELECT service_name FROM services WHERE service_id=papd.secondary_resource_id)  "
      + "WHEN sm.res_sch_category = 'DIA' THEN (SELECT test_name FROM diagnostics WHERE "
      + "test_id=papd.secondary_resource_id)  " + "WHEN sm.res_sch_category = 'OPE' THEN "
      + "(SELECT operation_name FROM operation_master WHERE op_id = papd"
      + ".secondary_resource_id)  END,',') AS secondary_resource_list, " + "string_agg"
      + "(COALESCE(ctm.complaint_type,'N/A'),',') as complaint_type_list, " + "pap.plan_name,"
      + " hcm.center_name, hcm.center_address, hcm.center_contact_phone, pap.created_by, "
      + "(SELECT hospital_name from generic_preferences) as hospital_name " + "FROM "
      + "patient_appointment_plan pap  " + "LEFT JOIN patient_details pd ON pd.mr_no = pap"
      + ".mr_no " + "LEFT JOIN patient_appointment_plan_details papd ON papd.plan_id = pap"
      + ".plan_id " + "LEFT JOIN scheduler_appointments sa ON sa.appointment_id = papd"
      + ".appointment_id " + "LEFT JOIN complaint_type_master ctm ON ctm.complaint_type_id = "
      + "papd.consultation_reason_id " + "LEFT JOIN hospital_center_master hcm ON hcm"
      + ".center_id = pap.center_id " + "LEFT JOIN scheduler_master sm ON (sm.res_sch_id = sa"
      + ".res_sch_id) " + "LEFT JOIN contact_preferences cp ON (cp.mr_no = pap.mr_no) "
      + "#FILTER# ";

  String[] tokens = new String[] { "patient_name", "lang_code", "appointment_date_list",
      "appointment_time_list", "appointment_time_12hr_list", "appointment_status_list",
      "resource_type_list", "secondary_resource_list", "complaint_type_list", "plan_name",
      "center_name", "center_address", "center_contact_phone", "created_by", "hospital_name" };

  public AppointmentPlannerDataProvider() {
    super(THIS_NAME);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = new ArrayList<String>(Arrays.asList(tokens));
    Collections.sort(tokenList);
    return tokenList;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {

    String planId = null;
    Map eventData = ctx.getEventData();
    if (eventData != null) {
      planId = ((Integer) eventData.get("plan_id")).toString();
    }
    fromTables = fromTables.replace("#FILTER#",
        "where pap.plan_id = " + planId + " AND (sa"
            + ".appointment_status isnull OR lower(sa.appointment_status) IN ('booked',"
            + "'confirmed','')) " + " group by pap.plan_id,pap.plan_name,pap.created_by,pd"
            + ".patient_name,cp.lang_code,hcm.center_name," + " hcm.center_address,hcm"
            + ".center_contact_phone,pd.mr_no,pd.patient_phone) as foo");
    setQueryParams(selectFields, selectCount, fromTables, null);
    return super.getMessageDataList(ctx);
  }

}