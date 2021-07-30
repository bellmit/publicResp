package com.insta.hms.messaging.providers;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.messaging.MessageContext;
import org.apache.commons.beanutils.BasicDynaBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BirthdayMessageDataProvider extends QueryDataProvider {

  /** The logger. */
  static Logger logger = LoggerFactory.getLogger(BirthdayMessageDataProvider.class);

  /** The this name. */
  private static String THIS_NAME = "Birthday Message";

  /** The Constant selectFields. */
  private static final String selectFields = "SELECT * ";

  /** The Constant selectCount. */
  private static final String selectCount = "SELECT COUNT(*)";

  /** The Constant fromTables. */
  private String fromTables = "FROM (SELECT distinct pr.mr_no as key, "
      + "pr.mr_no as receipient_id__ , "
      + " 'PATIENT' as receipient_type__, pd.patient_name, pd.patient_phone as recipient_mobile, "
      + "coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('S','B') then 'Y' else 'N' end , CASE WHEN "
      + "(cf.receive_communication in ('S','B') OR cf.receive_communication is null) "
      + " then 'Y' else 'N' end) as send_sms, "
      + " coalesce( CASE WHEN pcpref.communication_type is null then null "
      + "when pcpref.communication_type in ('E','B') then 'Y' else 'N' end ,CASE WHEN "
      + "(cf.receive_communication in ('E','B')  OR cf.receive_communication is null)"
      + " then 'Y' else 'N' end) as send_email,pcpref.message_group_name,"
      + " case when cf.lang_code is not null then cf.lang_code"
      + " else (select contact_pref_lang_code from generic_preferences) end as lang_code, "
      + "pd.email_id as recipient_email, hcm.center_name, hcm.center_contact_phone "
      + "from patient_details pd left JOIN patient_registration pr ON (pd.mr_no=pr.mr_no) "
      + "JOIN hospital_center_master hcm ON (hcm.center_id=pr.center_id) "
      + "LEFT JOIN contact_preferences cf ON(pd.mr_no = cf.mr_no) "
      + "left join patient_communication_preferences pcpref on (pd.mr_no = pcpref.mr_no and"
      + " pcpref.message_group_name='#')"
      + "where to_char(dateofbirth, 'DD-MM') = to_char(CURRENT_DATE,'DD-MM') ) as foo";

  public BirthdayMessageDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }
  
  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "patient_name", "recipient_mobile", "lang_code",
        "recipient_email", "center_name", "center_contact_phone" };

    return Arrays.asList( tokens );
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
    return super.getMessageDataList(ctx);
  }

}
