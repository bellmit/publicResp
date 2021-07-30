/**
 *
 */
package com.insta.hms.master.ReceiptRefundPrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi.p
 *
 */
public class ReceiptRefundPrintTemplateDAO extends GenericDAO {

	public static final String table = "receipt_refund_print_template";

	public ReceiptRefundPrintTemplateDAO() {
		super(table);
	}

	public static  String ALL_TEMPLATES = " SELECT * FROM "+table;

	public List getTemplateList()throws SQLException{
		return DataBaseUtil.queryToDynaList(ALL_TEMPLATES);
	}

	public static final String TEMPLATE_CONTENT = "SELECT template_content, template_mode  "+
	" FROM "+table+" WHERE template_name= ? ";

	public static BasicDynaBean getTemplateContent(String templateName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(TEMPLATE_CONTENT);
			ps.setString(1, templateName);
			List<BasicDynaBean> l  =  DataBaseUtil.queryToDynaList(ps);
			if (!l.isEmpty()){
				BasicDynaBean bean = l.get(0);
				return bean;
			}else{
				return null;
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public final String DELETE_TEMPLATE = "DELETE FROM "+table+" WHERE template_name= ? ";

	public boolean deleteTemplateContent(Connection con, String templateName) throws SQLException{
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_TEMPLATE);
			ps.setString(1, templateName);
			return ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static final String templateNames = "SELECT template_name FROM "+table;

	public List getTemplateNames()throws SQLException {

		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(templateNames));
	}
}
