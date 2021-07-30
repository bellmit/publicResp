package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RefundDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "mr_no", "recipient_name", "recipient_phone",
        "recipient_email", "center_name", "center_address", "center_contact_phone", "doctor_name",
        "referal_doctor", "bill_no", "total_amount", "patient_due",
        "message_footer", "currency_symbol", "lang_code",
        "refund_amount","refund_date", "next_of_kin_contact"};

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
