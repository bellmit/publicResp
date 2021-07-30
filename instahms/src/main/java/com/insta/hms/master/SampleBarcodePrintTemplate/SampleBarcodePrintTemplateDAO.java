package com.insta.hms.master.SampleBarcodePrintTemplate;

import com.insta.hms.common.GenericDAO;

import java.sql.SQLException;

public class SampleBarcodePrintTemplateDAO  extends GenericDAO{
	public SampleBarcodePrintTemplateDAO(){
		super("sample_bar_code_print_templates");
	}
	public String getCustomizedTemplate(String template_name) throws SQLException {
		return (String) findByKey("template_name", template_name).get("print_template_content");
	}
}
