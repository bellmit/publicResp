package com.insta.hms.messaging.providers;

import com.insta.hms.messaging.MessageContext;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileAccessDataProvider extends QueryDataProvider {

  private static String THIS_NAME = "Mobile Access";
  private static final String selectFields = "SELECT * ";
  private static final String selectCount = "SELECT COUNT(*) ";
  private static final String fromTables = "from (SELECT pd.mr_no, pd.mr_no as username,"
      + "pd.mobile_password as password, pd.patient_name as receipient_name,"
      + " pd.email_id as recipient_email, "
      + "pd.patient_phone as recipient_mobile, 'PATIENT' as receipient_type__, "
      + "coalesce(hcm.center_name,(select hospital_name from generic_preferences)) as center_name,"
      + " (select hospital_name from generic_preferences), "
      + " hcm.center_code, hcm.center_contact_phone AS center_phone, hcm.center_address, "
      + "pd.mr_no as receipient_id__, pr.patient_id from patient_details pd "
      + " LEFT JOIN patient_registration pr ON(pr.mr_no = pd.mr_no)"
      + " LEFT JOIN hospital_center_master hcm  ON(hcm.center_id = pr.center_id)) as foo";

  public MobileAccessDataProvider() {
    super(THIS_NAME);
    setQueryParams(selectFields, selectCount, fromTables, null);
  }

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "receipient_name", "recipient_email", "recipient_mobile",
        "center_address", "center_code", "center_name", "center_phone", "hospital_name", "mr_no",
        "password", "patient_id", "username" };
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
    if (null != eventData) {
      Map filter = new HashMap();
      if (null != eventData.get("mr_no")) {
        String[] mrNo = new String[] { (String) eventData.get("mr_no") };
        filter.put("mr_no", mrNo);
        filter.put("mr_no@type", new String[] { "text" });
        filter.put("mr_no@cast", new String[] { "y" });
      }
      if (null != eventData.get("patient_id")) {
        String[] patientId = new String[] { (String) eventData.get("patient_id") };
        filter.put("patient_id", patientId);
        filter.put("patient_id@type", new String[] { "text" });
        filter.put("patient_id@cast", new String[] { "y" });
      }

      addCriteriaFilter(filter);
    }
    return super.getMessageDataList(ctx);
  }
}