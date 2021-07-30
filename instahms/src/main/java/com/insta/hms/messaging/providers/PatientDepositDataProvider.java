package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PatientDepositDataProvider extends MapDataProvider {
  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = super.getTokens();
    String[] tokens = new String[] { "recipient_name", "recipient_mobile", "recipient_email",
        "mr_no", "patient_name", "patient_phone", "deposit_payer_name", "deposit_payer_phone",
        "remarks", "counter", "deposit_date", "receipient_id__", "receipient_type__",
        "deposit_amount", "lang_code", "patient_local_name", "currency_symbol", "center_name" };

    if (tokenList == null) {
      tokenList = new ArrayList<String>();
    }

    tokenList.addAll(Arrays.asList(tokens));
    Collections.sort(tokenList);
    return tokenList;
  }
}
