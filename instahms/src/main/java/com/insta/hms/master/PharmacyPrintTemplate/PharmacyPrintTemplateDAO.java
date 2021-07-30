package com.insta.hms.master.PharmacyPrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PharmacyPrintTemplateDAO extends GenericDAO{

	public static final String table = "store_print_template";

	public PharmacyPrintTemplateDAO(){
		super(table);
	}

	public static  String PHARMACY_TEMPLATE = " select ppt.*,"
			+" (select  distinct case when ppt.template_name=gd.template_name then 'Y' else 'N' end"
            +" from stores gd where ppt.template_name=gd.template_name) as avbltemplate"
            +" from "+table+" ppt";

	public static List getPharmacyTemplateList()throws SQLException{
		return DataBaseUtil.queryToDynaList(PHARMACY_TEMPLATE);
	}

	public static final String BILL_CONTENT = "SELECT store_template_content FROM store_print_template "+
		"WHERE template_name= ? ";

	public static String getTemplateContent(String templateName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String templateContent = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(BILL_CONTENT);
			ps.setString(1, templateName);
			templateContent = DataBaseUtil.getStringValueFromDb(ps);

		}finally{
				DataBaseUtil.closeConnections(con, ps);
		}
		return templateContent;
	}

	public final String DELETE_BILL_TEMPLATE = "DELETE FROM "+table+" WHERE template_name= ? ";


	public boolean deleteTemplateContent(Connection con, String templateName) throws SQLException{
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_BILL_TEMPLATE);
			ps.setString(1, templateName);
			return ps.execute();
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

}
