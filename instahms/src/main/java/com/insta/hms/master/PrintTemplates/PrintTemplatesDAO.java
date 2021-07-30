/**
 *
 */
package com.insta.hms.master.PrintTemplates;

import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;

/**
 * @author krishna.t
 *
 */
public class PrintTemplatesDAO extends GenericDAO {

	public PrintTemplatesDAO() {
		super("print_templates");
	}

	public String getCustomizedTemplate(PrintTemplate template) throws SQLException {
		return (String) findByKey("template_type", template.getType()).get("print_template_content");
	}

	public Integer getPatientHeaderTemplateId(PrintTemplate template) throws SQLException {
		Integer pheader_template_id = null;
		if (template.type.equals(PrintTemplate.Lab.type)
				|| template.type.equals(PrintTemplate.Rad.type)
				|| template.type.equals(PrintTemplate.Ser.type)
				|| template.type.equals(PrintTemplate.Patient_Ward_Activities.type)
				|| template.type.equals(PrintTemplate.TreatmentSheet.type)
				|| template.type.equals(PrintTemplate.ClinicalInfo.type)
				|| template.type.equals(PrintTemplate.Triage.type)
				|| template.type.equals(PrintTemplate.Initial_Assessment.type)
				|| template.type.equals(PrintTemplate.WebLab.type)
				|| template.type.equals(PrintTemplate.WebRad.type)
				|| template.type.equals(PrintTemplate.Medication_Chart.type)
				|| template.type.equals(PrintTemplate.APILAB.type)
				|| template.type.equals(PrintTemplate.APIRAD.type)
				|| template.type.equals(PrintTemplate.Vital_Measurements.type)
				|| template.type.equals(PrintTemplate.Discharge_Medication.type)){
			pheader_template_id =
				(Integer) findByKey("template_type", template.getType()).get("pheader_template_id");
		}
		return pheader_template_id;
	}
}
