package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class CriticalLabTestsDataProvider.
 */
public class CriticalLabTestsDataProvider extends MapDataProvider {

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.messaging.providers.MapDataProvider#getTokens()
   */
  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "patient_name", "visit_date", "receipient_id__", "sample_date",
        "ward", "bed", "age", "gender", "test_name", "result_name", "report_value", "severity",
        "units", "reference_range", "doctor_name", "patient_phone" };
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
