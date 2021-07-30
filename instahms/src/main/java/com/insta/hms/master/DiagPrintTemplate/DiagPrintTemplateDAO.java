package com.insta.hms.master.DiagPrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DiagPrintTemplateDAO extends GenericDAO {

	public static final String table = "diagnostic_outhouse_print_template";

	public DiagPrintTemplateDAO(){
		super(table);
	}
	public static  String DIAGNOSTIC_TEMPLATE = " SELECT * FROM "+table;

	public static List getBillTemplateList()throws SQLException{
		return DataBaseUtil.queryToDynaList(DIAGNOSTIC_TEMPLATE);
	}

	public static final String DIGNOSTIC_TEMPLATE_CONTENT = "SELECT diag_template_content, template_mode  "+
		" FROM diagnostic_print_template WHERE template_name= ? ";

	public static BasicDynaBean getTemplateContent(String templateName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String templateContent = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(DIGNOSTIC_TEMPLATE_CONTENT);
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

	public final String DELETE_DIAGNOSTIC_TEMPLATE = "DELETE FROM "+table+" WHERE template_name= ? ";


	public boolean deleteTemplateContent(Connection con, String templateName) throws SQLException{
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_DIAGNOSTIC_TEMPLATE);
			ps.setString(1, templateName);
			return ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

}
