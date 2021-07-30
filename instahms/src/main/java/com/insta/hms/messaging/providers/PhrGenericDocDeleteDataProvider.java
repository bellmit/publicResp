package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhrGenericDocDeleteDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory
      .getLogger(PhrGenericDocDeleteDataProvider.class);
  private static String THIS_NAME = "PHR Document Upload Deletion ";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = "FROM (select sm.salutation||' '||pd.patient_name||' '||"
      + "coalesce(pd.middle_name,'')||' '||coalesce(pd.last_name,'')  as patient_full_name, "
      + " to_char(pdm.mod_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) "
      + "AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') AS mod_time, "
      + " pd.patient_phone as patient_phone, "
      + " pd.patient_gender, to_char(pd.dateofbirth,'YYYY-MM-DD') AS patient_dateofbirth,"
      + " pd.email_id as patient_email, "
      + " get_patient_age(pd.dateofbirth, pd.expected_dob) AS patient_age,"
      + " get_patient_age_in(pd.dateofbirth, pd.expected_dob) AS patient_age_unit, "
      + " pd.mr_no,(select current_schema()) as group_id, "
      + " hcmd.center_name as default_center_name,hcmd.center_code as default_center_code, "
      + " hcmd.center_contact_phone AS default_center_phone,"
      + " hcmd.center_address as default_center_address,dc.city_name as default_center_city,"
      + " ds.state_name as default_center_state, "
      + " pd.mr_no as receipient_id__, 'PATIENT' as receipient_type__ ,"
      + " (select hospital_name from generic_preferences) as hospital_name  "
      + " from patient_details pd "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " JOIN patient_demographics_mod pdm ON(pd.mr_no = pdm.mr_no) "
      + " LEFT JOIN hospital_center_master hcmd ON(hcmd.center_id = 0) "
      + " LEFT JOIN city dc ON (dc.city_id=hcmd.city_id) "
      + " LEFT JOIN state_master ds ON (dc.state_id=ds.state_id) ) as foo ";

  public PhrGenericDocDeleteDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<Map> getMessageDataList(MessageContext ctx) throws SQLException, ParseException {
    Map eventData = ctx.getEventData();

    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("mr_no")) {
        String[] mrNo = (String[]) eventData.get("mr_no");
        filter.put("mr_no", mrNo);
        filter.put("mr_no@type", new String[] { "String" });
        filter.put("mr_no@cast", new String[] { "y" });
        filter.put("mr_no@op", new String[] { "in" });
      }
      addCriteriaFilter(filter);
    }

    List<Map> messageDataList = super.getMessageDataList(ctx);
    List<Map> messageDataListWithReportContent = new ArrayList<Map>();
    for (int i = 0; i < messageDataList.size(); i++) {
      Map item = new HashMap<Object, Object>();
      item.putAll(messageDataList.get(i));
      if (eventData.get("record_type") != null) {
        item.put("record_type", eventData.get("record_type"));
        item.put("document_id", eventData.get("document_id"));
      }
      messageDataListWithReportContent.add(item);
    }

    return messageDataListWithReportContent;
  }
}
