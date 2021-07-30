package com.insta.hms.master.ItemKitMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ItemKitMasterDAO extends GenericDAO {

	public ItemKitMasterDAO() {
		super("store_kit_main");
	}

	private static final String kit_item_details =
		" SELECT medicine_name,issue_base_unit,kit_item_id,qty,kit_id,kit_item_detail_id,issue_type " +
		"	FROM store_kit_details " +
		"	JOIN store_item_details ON ( kit_item_id = medicine_id ) " +
		"   JOIN store_category_master ON(category_id = med_category_id) WHERE kit_id = ? ";

	public List<BasicDynaBean> getKitItemDetails(int kit_id)
	throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(kit_item_details);
			ps.setInt(1, kit_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ACTIVE_KIT_ITEMS = 
		" SELECT skd.kit_id, skd.kit_item_id, sid.medicine_name, skd.qty " +
		" FROM store_kit_details skd " +
		"  JOIN store_kit_main skm ON (skm.kit_id = skd.kit_id) " +
		"  JOIN store_item_details sid ON (sid.medicine_id = skd.kit_item_id) " +
		" WHERE skm.status = 'A' ";

	public List<BasicDynaBean> getActiveKitItems() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ACTIVE_KIT_ITEMS);
	}

}

