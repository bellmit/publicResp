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

/**
 * The Class FollowupDataProvider.
 */
public class FollowupDataProvider extends QueryDataProvider {
  
  /** The logger. */
  static Logger logger = LoggerFactory
      .getLogger(FollowupDataProvider.class);
  
  /** The this name. */
  private static String THIS_NAME = "Followup Patients ";
  
  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";
  
  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";
  
  /** The Constant fromTables. */
  private String fromTables = "from (SELECT f.followup_id as key, "
      + "sm.salutation as salutation_name, p.patient_name as receipient_name,"
      + " p.email_id as recipient_email, "
      + "p.mr_no as receipient_id__, 'PATIENT' as receipient_type__,"
      + "p.patient_phone as recipient_mobile, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cp.receive_communication in ('S','B') OR cp.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cp.receive_communication in ('E','B')  OR cp.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + "d.doctor_name as followup_doctor, dd.dept_name as followup_dept, "
      + "to_char(f.followup_date, 'DD-MM-YYYY') as followup_date, "
      + "date(f.followup_date)::text as followup_date_yyyy_mm_dd, "
      + "to_char(f.followup_date, 'yyyy-MM-dd') as followup_date_search__, "
      + "f.followup_remarks, hcm.center_name,hcm.center_code,"
      + "hcm.center_contact_phone AS center_phone,hcm.center_address , "
      + " (select message_footer from message_types where message_type_id='sms_followup_reminder')"
      + " as message_footer, " + " case when cp.lang_code is not null then cp.lang_code else"
      + " (select contact_pref_lang_code from generic_preferences) end as lang_code, "
      + " d.specialization as doctor_specialization "
      + " FROM follow_up_details f, patient_details p "
      + " left join patient_communication_preferences pcpref on (p.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + "left join contact_preferences cp on (p.mr_no = cp.mr_no), doctors d, "
      + "department dd, hospital_center_master hcm,"
      + "patient_registration pr, salutation_master sm " + "WHERE hcm.center_id = pr.center_id and "
      + "f.patient_id = pr.patient_id and pr.mr_no = p.mr_no and "
      + "f.followup_doctor_id = d.doctor_id and d.dept_id = dd.dept_id "
      + " and sm.salutation_id = p.salutation order by followup_date_yyyy_mm_dd) as foo";

  /**
   * Instantiates a new followup data provider.
   */
  public FollowupDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null, "followup_date_yyyy_mm_dd", false);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
        "center_address", "center_code", "center_name", "center_phone", "doctor_specialization",
        "followup_date", "followup_date_yyyy_mm_dd", "followup_dept", "followup_doctor",
        "followup_remarks", "lang_code", "message_group_name", "salutation_name", "send_email",
        "send_sms" };
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
    setQueryParams(selectFields, selectCount, fromTables, null, "followup_date_yyyy_mm_dd", false);
    
    Map configParams = ctx.getConfigParams();
    if (null != configParams) {
      java.util.Date startDate = null;
      java.util.Date endDate = null;

      String overduePeriod = (String) configParams.get("overdue_period");
      if (null != overduePeriod && !overduePeriod.isEmpty()) {
        Integer overdueDays = Integer.parseInt(overduePeriod);
        if (overdueDays == 0) {
          startDate = DateUtil.getCurrentDate();
        } else if (overdueDays > 0) {
          java.util.Date refDate = (java.util.Date) DateUtil.getExpectedDate(overdueDays, "D", true,
              true);
          startDate = new java.sql.Date(refDate.getTime());
        }
      }
      String duePeriod = (String) configParams.get("due_period");
      if (null != duePeriod && !duePeriod.isEmpty()) {
        Integer dueDays = Integer.parseInt(duePeriod);
        if (dueDays == 0) {
          endDate = DateUtil.getCurrentDate();
        } else if (dueDays > 0) {
          java.util.Date refDate = (java.util.Date) DateUtil.getExpectedDate(dueDays, "D", false,
              true);
          endDate = new java.sql.Date(refDate.getTime());
        }
      }

      Map dateFilter = new HashMap();
      DateUtil dateUtil = new DateUtil();
      String strStartDate = dateUtil.getSqlDateFormatter().format(startDate);
      String strEndDate = dateUtil.getSqlDateFormatter().format(endDate);
      if (null == startDate) {
        dateFilter.put("followup_date_search__", new String[] { strEndDate });
        dateFilter.put("followup_date_search__@op", new String[] { "le" });
      } else if (null == endDate) {
        dateFilter.put("followup_date_search__", new String[] { strStartDate });
        dateFilter.put("followup_date_search__@op", new String[] { "ge" });
      } else {
        dateFilter.put("followup_date_search__", new String[] { strStartDate, strEndDate });
        dateFilter.put("followup_date_search__@op", new String[] { "ge,le" });
      }
      dateFilter.put("followup_date_search__@type", new String[] { "text" });
      dateFilter.put("followup_date_search__@cast", new String[] { "y" });
      logger.debug("Date Filter :" + strStartDate + "::" + strEndDate);
      
      String messageMode = (String) messageTypeBean.get("message_mode");
      String mode = messageMode.equalsIgnoreCase("sms") ? "sms" : "email";
      dateFilter.put("send_" + mode, new String[] { "Y" });
      dateFilter.put("send_" + mode + "@type", new String[] { "text" });
      addCriteriaFilter(dateFilter);
    }
    return super.getMessageDataList(ctx);
  }
}
