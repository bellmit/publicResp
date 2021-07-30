/**
 *
 */
package com.insta.hms.master.PatientHeaderTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna.t
 *
 */
public class PatientHeaderTemplateDAO extends GenericDAO {

	private static final String table = "doc_patient_header_templates";
	public PatientHeaderTemplateDAO() {
		super(table);
	}

	public List getTemplates(String type, String status) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;

		try {
			StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + table);
			queryBuilder.append(" WHERE type=?");
			queryBuilder.append(" AND status=?");
			ps = con.prepareStatement(queryBuilder.toString());
			ps.setString(1, type);
			ps.setString(2, status);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * if templateId is
	 * 		1) null		:	no patient header(empty string will be returned)
	 * 		2) 0 		:	default patient header on disk will be returned.
	 * 		3) greater than 0 : customized patient header will be returned.
	 *
	 * @param templateId
	 * @param type
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public String getPatientHeader(Integer templateId, String type) throws SQLException, IOException {
		String templateContent = "";
		if (templateId == null) return templateContent;
		if (templateId == 0) {
			PatientHeaderTemplate phTemplate = null;
			for (PatientHeaderTemplate template : PatientHeaderTemplate.values()) {
				if (template.getType().equalsIgnoreCase(type)) {
					phTemplate = template;
					break;
				}
			}
			String ftlPath = AppInit.getServletContext().getRealPath("/WEB-INF/templates/PatientHeaders");
			FileInputStream stream = new FileInputStream(ftlPath + "/" + phTemplate.getFtlName() + ".ftl");
			templateContent = new String(DataBaseUtil.readInputStream(stream));
		} else {
			BasicDynaBean bean = findByKey("template_id", templateId);
			templateContent = (String) bean.get("template_content");
		}

		return templateContent;
	}

}
