/**
 * 
 */

package com.insta.hms.core.patient.inpatientlist;

import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.mdm.savedsearches.Parameter;
import com.insta.hms.mdm.savedsearches.SearchParameters;

/**
 * The Class InPatientSearchFields.
 *
 * @author anup vishwas
 */
@SavedSearch(value = "IP Flow")
public class InPatientSearchFields extends SearchParameters {

  /**
   * Instantiates a new in patient search fields.
   */
  public InPatientSearchFields() {

    fields.put("visit_date", new Parameter("visit_date", "ge,le", "date", null, true));
    fields.put("discharge_status", new Parameter("discharge_status", "in", null, null, true));
    fields.put("bill_status", new Parameter("bill_status", "in", null, null, true));
    fields.put("visit_status", new Parameter("visit_status", "in", null, null, true));
    fields.put("ward", new Parameter("ward", "in", null, null, true));
    fields.put("doctor", new Parameter("doctor", "in", null, null, true));
    fields.put("department", new Parameter("department", "in", null, null, true));
    fields.put("selected_field", new Parameter("selected_field"));
    fields.put("selected_date", new Parameter("selected_date"));
    fields.put("selected_status", new Parameter("selected_status"));
  }
}
