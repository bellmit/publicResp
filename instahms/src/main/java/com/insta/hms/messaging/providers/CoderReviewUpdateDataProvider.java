package com.insta.hms.messaging.providers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Class CoderReviewUpdateDataProvider.
 */
public class CoderReviewUpdateDataProvider extends MapDataProvider {

  /** The tokens. */
  String[] tokens = { "receipient_id__", "temp", "receipient_type__",
      "recipient_name", "activity_data", "recipient_email", "link_to_ticket" };

  @Override
  public List<String> getTokens() throws SQLException {
    List<String> tokenList = Arrays.asList(tokens);
    Collections.sort(tokenList);
    return tokenList;
  }

}
