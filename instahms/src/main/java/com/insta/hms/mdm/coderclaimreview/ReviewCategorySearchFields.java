package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.mdm.savedsearches.Parameter;
import com.insta.hms.mdm.savedsearches.SearchParameters;

@SavedSearch(value = "Review Category")
public class ReviewCategorySearchFields extends SearchParameters {

  /**
   * Instantiates a new review category search fields.
   */
  public ReviewCategorySearchFields() {
    fields.put("status", new Parameter("status", "eq", null, null, false));
  }

}