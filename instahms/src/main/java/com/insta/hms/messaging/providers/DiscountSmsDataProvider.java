package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DiscountSmsDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "patient_name", "patient_phone", "center_name",
        "center_address", "center_contact_phone", "doctor_name", "referal_doctor", "bill_no",
        "total_amount_received", "total_amount", "currency_symbol", "admission_date",
        "admission_time", "department", "total_discount", "authorizer_name", "date_time" };

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