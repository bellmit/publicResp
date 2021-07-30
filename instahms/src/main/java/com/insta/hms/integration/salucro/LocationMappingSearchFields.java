package com.insta.hms.integration.salucro;

import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.mdm.savedsearches.Parameter;
import com.insta.hms.mdm.savedsearches.SearchParameters;

@SavedSearch(value = "Location Mapping")
public class LocationMappingSearchFields extends SearchParameters {

  /**
   * Instantiates a new role mapping search fields.
   */
  public LocationMappingSearchFields() {
    fields.put("status", new Parameter("status", "eq", null, null, false));
  }

}