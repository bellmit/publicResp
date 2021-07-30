package com.insta.hms.master.StoresRatePlanMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class StoresRatePlanDAO extends GenericDAO {

	public StoresRatePlanDAO(){
		super("store_rate_plans");
	}

	private static final String SEARCH_FIELDS = "select *";

	private static final String SEARCH_COUNT =  " SELECT count(*) ";

	private static final String SEARCH_TABLES =
		" FROM store_rate_plans " ;

	public PagedList list(Map requestParams, Map pagingParams)
	throws ParseException,SQLException{
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("store_rate_plan_name");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}
	
	public boolean addSPForRatePlan(Connection con,int ratePlanId,int cpRatePlanId)
	throws SQLException{
		String insert_sp_for_rate_plan =
				"INSERT INTO store_item_rates(store_rate_plan_id, medicine_id, selling_price,#) " +
				"( SELECT ?,medicine_id,0,* FROM store_item_details sid )";
		
		PreparedStatement ps = null;
		boolean success = true;
		if(cpRatePlanId == 0)
			insert_sp_for_rate_plan = insert_sp_for_rate_plan.replace(",#", "");
		else
			insert_sp_for_rate_plan = insert_sp_for_rate_plan.replace("#", "selling_price_expr");
		try{
			ps = con.prepareStatement(cpRatePlanId == 0 ? insert_sp_for_rate_plan.replace(",*", "") :
				insert_sp_for_rate_plan.replace("*", "(SELECT selling_price_expr FROM store_item_rates WHERE store_rate_plan_id = ? AND medicine_id = sid.medicine_id)"));
			ps.setInt(1, ratePlanId);
			if(cpRatePlanId != 0)
				ps.setInt(2, cpRatePlanId);
			success = ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
		return success;
	}
	
  private static final String SUB_GROUP_QUERY = "INSERT INTO store_tariff_item_sub_groups "
      + " (item_id,item_subgroup_id,store_rate_plan_id) "
      + " (select item_id, item_subgroup_id, ? from store_tariff_item_sub_groups "
      + " WHERE store_rate_plan_id = ?)";

  public boolean addTaxSubgroupForStoreTariff(Connection con, int ratePlanId, int cpRatePlanId)
      throws SQLException {
    PreparedStatement ps = null;
    boolean success = true;
    try {
      ps = con.prepareStatement(SUB_GROUP_QUERY);
      ps.setInt(1, ratePlanId);
      ps.setInt(2, cpRatePlanId);
      success = ps.executeUpdate() > 0;
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
    return success;
  }

}
