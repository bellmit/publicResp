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

public class WardShiftingDataProvider extends QueryDataProvider {
  static Logger logger = LoggerFactory.getLogger(WardShiftingDataProvider.class);
  private static String THIS_NAME = "Ward Shift ";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*)";
  private static final String fromTables = "FROM (select pr.patient_id,"
      + " pd.patient_care_oftext as next_of_kin_phone , "
      + " sm.salutation||' '||pd.patient_name||' '||coalesce(pd.middle_name,'')||' '||"
      + "coalesce(pd.last_name,'') as patient_name, pd.email_id as patient_email_id, "
      + " pr.reg_date , pr.reg_time, pd.patient_phone as patient_mobile, "
      + " pd.mr_no,hcm.center_id, hcm.center_name,hcm.center_contact_phone AS center_phone, "
      + "  pd.relation as next_of_kin_name , hcm.center_address, "
      + " d.doctor_name as doctor_name, d.doctor_mobile , "
      + " pd.mr_no as receipient_id__, 'PATIENT' as receipient_type__ , "
      + " case when cf.lang_code is not null then cf.lang_code else"
      + " (select contact_pref_lang_code from generic_preferences) end as lang_code "
      + " from patient_registration pr " + " JOIN patient_details pd ON(pr.mr_no = pd.mr_no) "
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) "
      + " JOIN patient_demographics_mod pdm ON(pd.mr_no = pdm.mr_no) "
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = pr.center_id) "
      + " LEFT JOIN contact_preferences cf ON(pd.mr_no = cf.mr_no)  "
      + " LEFT JOIN doctors d on(d.doctor_id = pr.doctor) ) as foo ";

  public WardShiftingDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  String[] tokens = new String[] { "patient_mobile", "center_address", "center_id", "center_name",
      "center_phone", "doctor_mobile", "mr_no", "next_of_kin_name", "next_of_kin_phone",
      "patient_id", "patient_name", "doctor_name", "reg_date", "reg_time", "new_bed", "old_bed",
      "new_ward", "old_ward", "lang_code" };

  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = new ArrayList<String>();
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
      if (null != eventData.get("patient_id")) {
        String[] patientId = { eventData.get("patient_id").toString() };
        filter.put("patient_id", patientId);
        filter.put("patient_id@type", new String[] { "text" });
        filter.put("patient_id@cast", new String[] { "y" });
        filter.put("patient_id@op", new String[] { "in" });
      }
      addCriteriaFilter(filter);
      List<Map> messageDataList = super.getMessageDataList(ctx);
      List<Map> messageDataListWithReportContent = new ArrayList<Map>();
      try {
        for (int i = 0; i < messageDataList.size(); i++) {
          Map item = new HashMap<Object, Object>();
          item.putAll(messageDataList.get(i));

          item.put("old_bed", eventData.get("old_bed").toString());
          item.put("old_ward", eventData.get("old_ward").toString());
          item.put("new_bed", eventData.get("new_bed").toString());
          item.put("new_ward", eventData.get("new_ward").toString());

          messageDataListWithReportContent.add(item);
        }
      } catch (Exception ex) {
        logger.error("", ex);
      }
      return messageDataListWithReportContent;
    }
    return Collections.EMPTY_LIST;
  }
}