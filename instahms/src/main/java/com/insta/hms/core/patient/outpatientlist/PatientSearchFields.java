/**
 * 
 */
package com.insta.hms.core.patient.outpatientlist;

import com.insta.hms.common.annotations.SavedSearch;
import com.insta.hms.mdm.savedsearches.Parameter;
import com.insta.hms.mdm.savedsearches.SearchParameters;

/**
 * @author krishnat
 *
 */
@SavedSearch(value="OP Flow")
public class PatientSearchFields extends SearchParameters {
	
	public PatientSearchFields() {
		fields.put("appointment_date", new Parameter("appointment_date", "ge,le", "date", null, true));
		fields.put("visit_date", new Parameter("visit_date", "ge,le", "date", null, true));
		fields.put("visit_status", new Parameter("visit_status", "in", null, null, true));
		fields.put("bill_status", new Parameter("bill_status", "in", null, null, true));
		fields.put("patient_status", new Parameter("patient_status", "in", null, null, true));
		fields.put("doctor", new Parameter("doctor", "in", null, null, true));
		fields.put("department", new Parameter("department", "in", null, null, true));
		fields.put("_department_type", new Parameter("_department_type"));
		fields.put("primary_insurance", new Parameter("primary_insurance", "in", null, null, true));
		fields.put("secondary_insurance", new Parameter("secondary_insurance", "in", null, null, true));
		fields.put("selected_field", new Parameter("selected_field"));
		fields.put("selected_date", new Parameter("selected_date"));
		fields.put("selected_status", new Parameter("selected_status"));
	}
	
}
