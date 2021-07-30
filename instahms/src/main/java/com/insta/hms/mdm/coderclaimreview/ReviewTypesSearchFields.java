package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.mdm.savedsearches.Parameter;
import com.insta.hms.mdm.savedsearches.SearchParameters;

@SavedSearch(value = "Review Types")
public class ReviewTypesSearchFields extends SearchParameters {

  /**
   * Instantiates a new review types search fields.
   */
  public ReviewTypesSearchFields() {
    fields.put("status", new Parameter("status", "eq", null, null, false));
  }

}