package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdvancePaymentDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "patient_name", "patient_phone", "center_name",
        "center_address", "center_contact_phone", "doctor_name", "referal_doctor", "bill_no",
        "total_amount_received", "total_amount", "patient_due", "currency_symbol",
        "advance_amount_paid", "admission_date", "admission_time", "department", "lang_code" };

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
