/**
 *
 */
package com.insta.hms.master.CommonPrintTemplates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna
 *
 */
public class PrintTemplatesDAO extends GenericDAO {

	public PrintTemplatesDAO() {
		super("common_print_templates");
	}

	private static final String GET_TEMPLATE_NAMES = "SELECT template_name FROM common_print_templates WHERE template_type=?";

	public static List getTemplateNames(String templateType) throws SQLException{
		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(GET_TEMPLATE_NAMES, templateType));
	}
	private static final String TEMPLATE_CONTENT = "SELECT template_content, template_mode FROM common_print_templates WHERE template_name = ?";
	public static BasicDynaBean getTemplateContent(String templateName) throws SQLException {
		Connection con = null;
		PreparedStatement ps =null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(TEMPLATE_CONTENT);
			ps.setString(1, templateName);
			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
			if (!l.isEmpty()) {
				BasicDynaBean bean = l.get(0);
				return bean;
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String TEMPLATE_CONTENT_BY_ID = "SELECT template_content, template_mode, pheader_template_id, " + 
			" print_template_id FROM common_print_templates WHERE print_template_id = ?";
	public static BasicDynaBean getTemplateContent(int templateId) throws SQLException {
		Connection con = null;
		PreparedStatement ps =null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(TEMPLATE_CONTENT_BY_ID);
			ps.setInt(1, templateId);
			List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(ps);
			if (!l.isEmpty()) {
				BasicDynaBean bean = l.get(0);
				return bean;
			} else {
				return null;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_TEMPLATE_LIST = "SELECT print_template_id, template_name FROM common_print_templates " + 
			" WHERE template_type='InstaGenericForm' order by print_template_id desc";
	public static List getTemplateList() throws SQLException{
		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(GET_TEMPLATE_LIST));
	}

}
