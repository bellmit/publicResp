package com.insta.hms.mdm.ordersets;

import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.mdm.savedsearches.Parameter;
import com.insta.hms.mdm.savedsearches.SearchParameters;

@SavedSearch(value = "Order Sets")
public class OrderSetsSearchFields extends SearchParameters {

  /**
   * Instantiates a new order sets search fields.
   */
  public OrderSetsSearchFields() {
    fields.put("valid_from", new Parameter("valid_from", "ge", "date", null, false));
    fields.put("valid_till", new Parameter("valid_till", "le", "date", null, false));
    fields.put("status", new Parameter("status", "eq", null, null, false));
    fields.put("gender_applicability",
        new Parameter("gender_applicability", "eq", null, null, false));
    fields.put("visit_applicability",
        new Parameter("visit_applicability", "eq", null, null, false));
    fields.put("center_applicability",
        new Parameter("center_applicabilty", "eq", null, null, false));
    fields.put("dept_applicability", new Parameter("dept_applicabilty", "eq", null, null, false));
  }

}