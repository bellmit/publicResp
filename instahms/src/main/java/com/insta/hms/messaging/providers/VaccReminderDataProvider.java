package com.insta.hms.messaging.providers;

import com.bob.hms.common.DateUtil;
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

public class VaccReminderDataProvider extends QueryDataProvider {

  static Logger logger = LoggerFactory
      .getLogger(VaccReminderDataProvider.class);
  private static String THIS_NAME = "Vaccination Due Patients";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*) ";

  private String fromTables = "FROM (SELECT (mr_no||vaccine_dose_id||vaccine_id) as"
      + " key, (recommended_age_days::date)::text AS vaccine_due_date, " + "receipient_name, "
      + "mr_no, recipient_email, recipient_mobile, " + "vaccine_name, sms_due_date ,mr_no as "
      + "receipient_id__, 'PATIENT' as receipient_type__, salutation_name , "
      + " send_sms, send_email,message_group_name, (select "
      + "message_footer from message_types where "
      + "message_type_id='sms_next_day_appointment_reminder') as message_footer, lang_code"
      + " FROM (select *,((recommended_age_days::date - notification_lead_time_days)::date)"
      + "::text as sms_due_date " + "FROM " + "(SELECT receipient_name, recipient_email, "
      + "send_sms, send_email,message_group_name,"
      + "recipient_mobile, vdm.vaccine_dose_id,vdm.notification_lead_time_days, " + "(vm"
      + ".vaccine_name ||' '|| coalesce(vdm.dose_num::text, '')) AS vaccine_name, mr_no, vm"
      + ".vaccine_id, lang_code, " + "vm.status as vacc_status, vdm.status as dose_status,"
      + "CASE WHEN vdm.age_units = 'W' THEN date_trunc('day', coalesce(dateofbirth, "
      + "expected_dob)) + cast(recommended_age || ' weeks' as interval) " + "WHEN vdm"
      + ".age_units = 'M' THEN date_trunc('day', coalesce(dateofbirth, expected_dob)) + cast"
      + "(recommended_age || ' months' as interval) " + "ELSE " + "date_trunc('day', coalesce"
      + "(dateofbirth, expected_dob)) + cast(recommended_age || ' years' as interval) " + "END"
      + " AS recommended_age_days, " + "coalesce(dateofbirth, expected_dob) as patient_dob, "
      + "salutation_name " + " FROM vaccine_dose_master vdm  " + "JOIN vaccine_master vm ON"
      + "(vdm.vaccine_id = vm.vaccine_id), " + "(SELECT * FROM (select dateofbirth, "
      + "expected_dob, pd.mr_no, " + "get_patient_full_name(null, pd.patient_name, pd"
      + ".middle_name, pd.last_name) as receipient_name, " + "pd.email_id as recipient_email, " + ""
      + "pd.patient_phone as recipient_mobile," + "(extract(year from current_date) - "
      + "extract(year from " + " coalesce(dateofbirth, expected_dob) ) ) as patient_age, sm"
      + ".salutation  as salutation_name, " + " case when cf.lang_code is not null then cf"
      + ".lang_code else (select contact_pref_lang_code from generic_preferences) end as "
      + "lang_code,"
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name "
      + " FROM patient_details pd  "
      + " left join patient_communication_preferences pcpref on (pd.mr_no = pcpref.mr_no and " 
      + " pcpref.message_group_name='#') "
      + " LEFT JOIN salutation_master sm ON (pd"
      + ".salutation = sm.salutation_id) " + "  LEFT JOIN contact_preferences cf ON(pd.mr_no ="
      + " cf.mr_no) " + " WHERE pd.sms_for_vaccination='Y')as foo1 " + " WHERE "
      + "foo1.patient_age <19) as foo2 " + ") as foo3 )as foo4 " + "WHERE vaccine_dose_id NOT"
      + " IN (select vaccine_dose_id from patient_vaccination pv where pv.mr_no = foo4.mr_no)" + " "
      + " AND vacc_status='A' AND dose_status='A' " + "order by sms_due_date )as foo5 ";

  public VaccReminderDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
        "lang_code", "message_group_name", "mr_no", "salutation_name", "send_email", "send_sms",
        "sms_due_date", "vaccine_due_date", "vaccine_name" };
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
    
    
    Map configParams = ctx.getConfigParams();
    if (null != configParams) {
      java.util.Date startDate = DateUtil.getCurrentDate();
      java.util.Date endDate = DateUtil.getCurrentDate();

      String duePeriod = (String) configParams.get("due_period");
      if (null != duePeriod && !duePeriod.isEmpty()) {
        Integer dueDays = Integer.parseInt(duePeriod);
        if (dueDays > 0) {
          java.util.Date refDate = (java.util.Date) DateUtil.getExpectedDate(dueDays, "D", true,
              true);
          startDate = new java.sql.Date(refDate.getTime());
        }
      }

      Map dateFilter = new HashMap();
      DateUtil dateUtil = new DateUtil();
      String strStartDate = dateUtil.getSqlDateFormatter().format(startDate);
      String strEndDate = dateUtil.getSqlDateFormatter().format(endDate);
      dateFilter.put("sms_due_date", new String[] { strStartDate, strEndDate });
      dateFilter.put("sms_due_date@op", new String[] { "ge,le" });
      dateFilter.put("sms_due_date@type", new String[] { "text" });

      logger.debug("dates :" + strStartDate + "::" + strEndDate);
      addCriteriaFilter(dateFilter);
    }
    return super.getMessageDataList(ctx);
  }

}