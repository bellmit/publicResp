package com.insta.hms.master.PrescriptionsLabelPrintTemplates;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PrescriptionsLabelPrintTemplateDAO extends GenericDAO{
	public static final String table = "prescription_label_print_template";
	public PrescriptionsLabelPrintTemplateDAO() {
		super(table);
	}
	private static final String GET_TEMPLATE_NAMES = "Select template_name from "+table;

	public static List getTemplateNames() throws SQLException{
		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(GET_TEMPLATE_NAMES));
	}
	private static final String TEMPLATE_CONTENT = "Select prescription_lbl_template_content,template_mode From " + table + " WHERE template_name = ?";
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
}
