package com.insta.hms.master.HVFPrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class HVFPrintTemplateDAO extends GenericDAO{
	public static final String table = "hvf_print_template";

	public HVFPrintTemplateDAO(){
		super(table);
	}
	public static  String HVF_TEMPLATE = " SELECT * FROM "+table;

	public static List gethvfTemplateList()throws SQLException{
		return DataBaseUtil.queryToDynaList(HVF_TEMPLATE);
	}
	public static final String HVF_TEMPLATE_CONTENT = "SELECT hvf_template_content, template_mode  "+
		" FROM hvf_print_template WHERE template_name= ? ";

	public static BasicDynaBean getTemplateContent(String templateName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String templateContent = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(HVF_TEMPLATE_CONTENT);
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
}
