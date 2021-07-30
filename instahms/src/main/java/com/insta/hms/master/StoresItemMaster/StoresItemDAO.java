package com.insta.hms.master.StoresItemMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.stores.StoreDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class StoresItemDAO extends GenericDAO {
	
	public StoresItemDAO(){
		super("store_item_details");
	}
	

	private static final String GET_STORE_ITEM_SUBGROUP_DETAILS = "select sisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
		" from store_item_sub_groups sisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = sisg.item_subgroup_id) "+
			" left join store_item_details sdt on (sdt.medicine_id = sisg.medicine_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where sisg.medicine_id = ? ";

	public static List<BasicDynaBean> getStoreItemSubGroupDetails(Integer medicineId) throws SQLException {
		List list = null;
		Connection con = null;
	    PreparedStatement ps = null;
	 try{
		 con=DataBaseUtil.getReadOnlyConnection();
		 ps=con.prepareStatement(GET_STORE_ITEM_SUBGROUP_DETAILS);
		 ps.setInt(1, medicineId);
		 list = DataBaseUtil.queryToDynaList(ps);
	 }finally {
		 DataBaseUtil.closeConnections(con, ps);
	 }
	 return list;
	}

  private static final String GET_STORE_ITEM_SUBGROUP_DETAILS_ENH = " select sisg.item_subgroup_id, "
      + " isg.item_subgroup_name,ig.item_group_id, ig.item_group_name, "
      + " stisg.item_subgroup_id as tariff_subgroup_id, "
      + " isg1.item_subgroup_name as tariff_subgroup_name,ig1.item_group_id as tariff_item_group_id, "
      + " ig1.item_group_name as tariff_group_name, "
      + " sstisg.item_subgroup_id as rate_plan_tariff_subgroup_id, "
      + " isg2.item_subgroup_name as rate_plan_tariff_subgroup_name ,ig2.item_group_id as rate_plan_tariff_item_group_id, "
      + " ig2.item_group_name as rate_plan_tariff_group_name, "
      + " igt.item_group_type_id,igt.item_group_type_name " 
      + " from store_item_details sdt "
      + " left join store_item_sub_groups sisg on (sdt.medicine_id = sisg.medicine_id) "
      + " left join store_tariff_item_sub_groups stisg on (sdt.medicine_id = stisg.item_id AND stisg.store_rate_plan_id = ?) "
      + " left join store_tariff_item_sub_groups sstisg on (sdt.medicine_id = sstisg.item_id AND sstisg.store_rate_plan_id = ?) "
      + " left join item_sub_groups isg on (isg.item_subgroup_id = sisg.item_subgroup_id) "
      + " left join item_sub_groups isg1 on (isg1.item_subgroup_id = stisg.item_subgroup_id) "
      + " left join item_sub_groups isg2 on (isg2.item_subgroup_id = sstisg.item_subgroup_id) "
      + " left join item_groups ig on (ig.item_group_id = isg.item_group_id) "
      + " left join item_groups ig1 on (ig1.item_group_id = isg1.item_group_id) "
      + " left join item_groups ig2 on (ig2.item_group_id = isg2.item_group_id) "
      + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id) "
      + " left join item_group_type igt1 on (igt1.item_group_type_id = ig1.item_group_type_id) "
      + " left join item_group_type igt2 on (igt2.item_group_type_id = ig2.item_group_type_id) "
      + " where sdt.medicine_id = ? ";

  public static List<BasicDynaBean> getStoreItemSubGroupDetails(Integer medicineId,
      int visitStoreRatePlanId, int deptId) throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean storeBean = StoreDAO.findByStore(deptId);
    int storeRatePlanId = (storeBean.get("store_rate_plan_id") == null ? 0
        : (Integer) storeBean.get("store_rate_plan_id"));
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_STORE_ITEM_SUBGROUP_DETAILS_ENH);
      ps.setInt(1, storeRatePlanId);
      ps.setInt(2, visitStoreRatePlanId);
      ps.setInt(3, medicineId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }
  private static final String GET_ITEM_GROUP_CODE = "SELECT group_code FROM item_groups ig "
      + "LEFT JOIN item_sub_groups isg ON (ig.item_group_id = isg.item_group_id) "
      + "WHERE isg.item_subgroup_id = ?";

  public static List<String> getGroupCodes(Integer itemSubgroupId) throws SQLException {
    List<String> codes = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ITEM_GROUP_CODE);
      ps.setInt(1, itemSubgroupId);
      codes = DataBaseUtil.queryToStringList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return codes;
  }
	
	private String items_list = 
			" select * from store_item_details order by updated_timestamp ";
	
	public BasicDynaBean getLatestItemUpdated() throws SQLException{
		Connection con = null;
		PreparedStatement ps =null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(items_list+" desc limit 1 ");
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
  private static final String GET_INVENTORY_ITEM_DETAILS = " SELECT sid.medicine_id, sic.item_code, "
      + " sid.medicine_name, sic.code_type, sid.service_sub_group_id, "
      + " sid.status, sid.insurance_category_id, sid.billing_group_id, "
      + " sid.item_selling_price "
      + " FROM store_item_details sid "
      + " LEFT JOIN ha_item_code_type hict ON (hict.medicine_id = sid.medicine_id "
      + "  AND hict.health_authority = ?) "
      + " LEFT JOIN store_item_codes sic ON(sic.medicine_id = hict.medicine_id "
      + "  AND sic.code_type = hict.code_type) " + " WHERE sid.medicine_id = ? ";

  public BasicDynaBean getInventoryItemBean(Integer itemId, String healthAuthority) {
    return DatabaseHelper.queryToDynaBean(GET_INVENTORY_ITEM_DETAILS, new Object[] {
        healthAuthority, itemId });
  }

  private static final String GET_INVENTORY_ITEM_RATE = " SELECT sir.medicine_id, "
      + " CASE WHEN textregexeq(sir.selling_price_expr, '^[[:digit:]]+(\\.[[:digit:]]+)?$') "
      + " THEN sir.selling_price_expr::decimal ELSE 0 END AS item_rate "
      + " FROM store_item_rates sir " + " WHERE sir.medicine_id = ? AND sir.store_rate_plan_id = ?";

  public BasicDynaBean getInventoryItemRate(Integer itemId, Integer storeRatePlanId) {
    return DatabaseHelper.queryToDynaBean(GET_INVENTORY_ITEM_RATE, new Object[] { itemId,
        storeRatePlanId });
  }

}
