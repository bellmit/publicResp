package com.insta.hms.master.DepositReceiptRefundTemplate;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class DepositReceiptRefundPrintTemplateDAO extends GenericDAO {

	public static final String table = "deposit_receipt_refund_template";

	public DepositReceiptRefundPrintTemplateDAO(){
		super(table);
	}

	public static final String templateList = "SELECT * FROM deposit_receipt_refund_template";

	public List getTemplateList()throws SQLException {

		return DataBaseUtil.queryToDynaList(templateList);
	}

	public static final String templateNames = "SELECT template_name FROM deposit_receipt_refund_template";

	public List getTemplateNames()throws SQLException {

		return ConversionUtils.listBeanToListMap(DataBaseUtil.queryToDynaList(templateNames));
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


}