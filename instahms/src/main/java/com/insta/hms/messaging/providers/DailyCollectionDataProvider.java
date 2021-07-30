package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DailyCollectionDataProvider extends MapDataProvider {

  @Override
  public List<String> getTokens() throws SQLException {
    String[] tokens = new String[] { "total_collection", "hospital_name", "total_collection",
        "total_collection_summary", "detailed_summary", "yesteday_date", "currency_symbol" };

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
