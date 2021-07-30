package com.insta.hms.messaging.providers;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.MessageUtil;
import com.insta.hms.messaging.MessageContext;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackSmsDataProvider extends QueryDataProvider {

  private static final MessageUtil messageUtil = ApplicationContextProvider
      .getBean(MessageUtil.class);

  static Logger logger = LoggerFactory
      .getLogger(FeedbackSmsDataProvider.class);
  private static String thisName = "Feedback SMS";
  private static final String SELECT_FIELDS = "SELECT * ";
  private static final String SELECT_COUNT = "SELECT COUNT(*) ";

  private String fromTables = "FROM (select distinct pr.mr_no as receipient_id__ , "
      + "'PATIENT' as receipient_type__, pr.visit_type, pd.patient_name, "
      + "(pr.reg_date + pr.reg_time)::text as registration_time,pr.op_type, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + "pd.patient_phone as recipient_mobile , pd.email_id as recipient_email,"
      + " d.send_feedback_sms, hcm.center_name, "
      + "hcm.center_contact_phone, cid.param as url, cid.integration_id "
      + "from patient_registration pr JOIN patient_details pd ON (pd.mr_no=pr.mr_no) "
      + "JOIN doctors d ON (pr.doctor=d.doctor_id) "
      + "JOIN doctor_consultation dc ON (dc.patient_id = pr.patient_id) "
      + "JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id) "
      + "LEFT JOIN contact_preferences cf ON(pd.mr_no = cf.mr_no) "
      + "left join patient_communication_preferences pcpref on (pd.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + "JOIN center_integration_details cid ON (cid.center_id=pr.center_id) where "
      + "visit_type='o' AND send_feedback_sms=true AND "
      + "param IS NOT NULL AND op_type in ('M','R') AND "
      + "integration_id=(select integration_id from insta_integration where "
      + "integration_name='center_practo_profile' and status='A'))as foo5 ";

  public FeedbackSmsDataProvider() {
    super(thisName);
    setQueryParams(SELECT_FIELDS, SELECT_COUNT, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "day", "center_contact_phone", "patient_name",
        "center_name", "url", "recipient_mobile" };
    List<String> tokenList = new ArrayList<String>();
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    Collections.sort(tokenList);
    return tokenList;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    GenericDAO messageTypesDao = new GenericDAO("message_types");
    BasicDynaBean messageTypeBean = messageTypesDao.findByKey("message_type_id",
        (String) ctx.getMessageType().get("message_type_id"));
    String messageGroupName = (String) messageTypeBean.get("message_group_name");
    fromTables = fromTables.replace("#", messageGroupName);
    setQueryParams(SELECT_FIELDS, SELECT_COUNT, fromTables, null);
    
    String messageMode = (String) messageTypeBean.get("message_mode");
    String mode = messageMode.equalsIgnoreCase("sms") ? "sms" : "email";
    Map modeFilter = new HashMap();
    modeFilter.put("send_" + mode, new String[] { "Y" });
    modeFilter.put("send_" + mode + "@type", new String[] { "text" });
    addCriteriaFilter(modeFilter);
    
    
    Map eventData = ctx.getEventData();
    String configNumber = eventData.get("config_number").toString();
    Map configParams = ctx.getConfigParams();
    String startTime = (String) configParams.get("start_time_" + configNumber);
    String endTime = (String) configParams.get("end_time_" + configNumber);
    String day = (String) configParams.get("day_" + configNumber);

    Date date = DateUtil.getCurrentDate();
    String dayStr = messageUtil.getMessage("js.common.calendar.today");
    if (!day.equals("0")) {
      java.util.Date backDate = (java.util.Date) DateUtil.getExpectedDate(Integer.parseInt(day),
          "D", true, true);
      date = new java.sql.Date(backDate.getTime());
      if (day.equals("1")) {
        dayStr = messageUtil.getMessage("js.common.calendar.yesterday");
      } else {
        dayStr = day + " " + messageUtil.getMessage("js.common.calendar.days.back");
      }
    }
    Map dateFilter = new HashMap();

    dateFilter.put("registration_time",
        new String[] { date + " " + startTime, date + " " + endTime });
    dateFilter.put("registration_time@op", new String[] { "ge,le" });
    dateFilter.put("registration_time@type", new String[] { "text" });

    logger.debug("Time :" + startTime + "::" + endTime);
    addCriteriaFilter(dateFilter);
    List<Map> messageDataList = super.getMessageDataList(ctx);
    List<Map> messageDataListWithReportContent = new ArrayList<Map>();
    try {
      for (int i = 0; i < messageDataList.size(); i++) {
        Map item = new HashMap<Object, Object>();
        item.putAll(messageDataList.get(i));
        item.put("day", dayStr);
        messageDataListWithReportContent.add(item);
      }
    } catch (Exception exception) {
      logger.error("", exception);
    }
    return messageDataListWithReportContent;
  }
}