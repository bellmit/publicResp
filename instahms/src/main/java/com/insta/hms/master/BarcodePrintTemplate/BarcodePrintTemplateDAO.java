package com.insta.hms.master.BarcodePrintTemplate;

import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;

import java.sql.SQLException;

public class BarcodePrintTemplateDAO extends GenericDAO {

	public BarcodePrintTemplateDAO() {
		super("bar_code_print_templates");
	}

	public String getCustomizedTemplate(PrintTemplate template) throws SQLException {
		return (String) findByKey("template_type", template.getType()).get("print_template_content");
	}

}
