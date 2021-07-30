package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManualBillEmailDataProvider extends MapDataProvider {
  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "recipient_name", "center_name",
        "center_contact_phone", "recipient_phone", "doctor_name", "referal_doctor", "bill_no",
        "bill_date", "bill_amount", "recipient_email" };
    List<String> tokenList = super.getTokens();

    if (null == tokenList) {
      tokenList = new ArrayList<String>();
    }
    for (String token : tokens) {
      if (!tokenList.contains(token)) {
        tokenList.add(token);
      }
    }
    return tokenList;
  }
}
