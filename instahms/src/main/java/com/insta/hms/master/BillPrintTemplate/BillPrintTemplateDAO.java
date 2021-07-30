package com.insta.hms.master.BillPrintTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class BillPrintTemplateDAO extends GenericDAO{

	public static final String table = "bill_print_template";

	public BillPrintTemplateDAO(){
		super(table);
	}

	public static  String BILL_TEMPLATE = " SELECT template_name, template_mode, " +
		" download_content_type, user_name, reason, download_extn FROM " + table +
		" ORDER BY template_name";

	public static List getBillTemplateList()throws SQLException{
		return DataBaseUtil.queryToDynaList(BILL_TEMPLATE);
	}

	public static String AVAILABLE_TEMPLATES_LIST =
		" SELECT * FROM all_bill_available_templates_view " +
		" WHERE status = 'A' AND " +
		"  (bill_type='*' OR bill_type=?) AND (insurance_type='*' OR insurance_type=?) " +
		"	GROUP BY template_id, template_name, bill_type, insurance_type, display_order, status";

	public static List getAvailableTemplatesList(String billType, String insuranceType) throws SQLException {
		return DataBaseUtil.queryToDynaList(AVAILABLE_TEMPLATES_LIST, new String[]{billType, insuranceType});
	}
	
	public static List getAvailableTemplatesList(Connection con,String billType, String insuranceType) throws SQLException {
    return DataBaseUtil.queryToDynaList(con, AVAILABLE_TEMPLATES_LIST, new String[]{billType, insuranceType});
  }

	public static final String BILL_CONTENT = "SELECT bill_template_content, template_mode, " +
			" download_content_type, download_extn  "+
			" FROM bill_print_template WHERE template_name= ? OR ('CUSTOM-' || template_name) = ?";

	public static BasicDynaBean getTemplateContent(String templateName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String templateContent = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(BILL_CONTENT);
			ps.setString(1, templateName);
			ps.setString(2, templateName);
			List<BasicDynaBean> l  =  DataBaseUtil.queryToDynaList(ps);
			if (!l.isEmpty()){
				BasicDynaBean bean = l.get(0);
				return bean;
			} else {
				return null;
			}

		}finally{
				DataBaseUtil.closeConnections(con, ps);
		}
	}

	public final String DELETE_BILL_TEMPLATE = "DELETE FROM "+table+" WHERE template_name= ? ";


	public boolean deleteTemplateContent(Connection con, String templateName) throws SQLException{
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DELETE_BILL_TEMPLATE);
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
