package com.insta.hms.master.OrderKitMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderKitMasterDAO extends GenericDAO{
	public OrderKitMasterDAO() {
		super("order_kit_main");
	}

	private static final String ORDERKIT_ITEM_DETAILS =
		" SELECT medicine_name,issue_base_unit,medicine_id,qty_needed,order_kit_id,issue_type " +
		"	FROM order_kit_details " +
		"	JOIN store_item_details using (medicine_id) " +
		"   JOIN store_category_master ON(category_id = med_category_id) WHERE order_kit_id = ? ";

	public List<BasicDynaBean> getKitItemDetails(int kit_id)
	throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ORDERKIT_ITEM_DETAILS);
			ps.setInt(1, kit_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ACTIVE_ORDERKIT_ITEMS = 
		" SELECT okd.order_kit_id, okd.medicine_id, sid.medicine_name, okd.qty_needed " +
		" FROM order_kit_details okd " +
		"  JOIN order_kit_main okm ON (okm.order_kit_id = okd.order_kit_id) " +
		"  JOIN store_item_details sid ON (sid.medicine_id = okd.medicine_id) " +
		" WHERE okm.status = 'A' ";

	public List<BasicDynaBean> getActiveKitItems() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ACTIVE_ORDERKIT_ITEMS);
	}
	 
	private String orderKitNames_query ="select order_kit_name from order_kit_main ";
	public List getOrderKitNames(String orderkitId)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			if(orderkitId != null){
				String filterQuery = orderKitNames_query + " where order_kit_id != ?";
				ps = con.prepareStatement(filterQuery);
				ps.setInt(1, Integer.parseInt(orderkitId));
			}else{
				ps = con.prepareStatement(orderKitNames_query);
			}
			
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		
	}
}
