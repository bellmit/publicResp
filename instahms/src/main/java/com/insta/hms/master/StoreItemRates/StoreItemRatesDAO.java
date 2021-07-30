package com.insta.hms.master.StoreItemRates;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.HsSfWorkbookUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StoreItemRatesDAO extends GenericDAO {

	public StoreItemRatesDAO(){
		super("store_item_rates");
	}

	public static Map<String, String> aliasNamesToDbCOlNames = new LinkedHashMap<String, String>();
	static {
		aliasNamesToDbCOlNames.put("item id", "medicine_id");
		aliasNamesToDbCOlNames.put("store rate plan id", "store_rate_plan_id");
		aliasNamesToDbCOlNames.put("item name", "medicine_name");
		aliasNamesToDbCOlNames.put("category name", "category_name");
		aliasNamesToDbCOlNames.put("tax basis", "tax_type");
		aliasNamesToDbCOlNames.put("tax %", "tax_rate");
		aliasNamesToDbCOlNames.put("drug code", "item_code");
		aliasNamesToDbCOlNames.put("code type", "code_type");
		aliasNamesToDbCOlNames.put("selling price expression", "selling_price_expr");

	}

	private static final String SEARCH_FIELDS =
		"SELECT * FROM ";

	private static final String SEARCH_COUNT =  " SELECT count(*) FROM ";

	private static final String SEARCH_TABLES =
		" ( SELECT m.manf_name,category,sid.medicine_name,sid.medicine_id," +
		"       selling_price,selling_price_expr,store_rate_plan_id,store_rate_plan_name,sir.tax_type,sir.tax_rate "+
		" FROM store_item_rates sir " +
		" JOIN store_rate_plans USING(store_rate_plan_id) " +
		" JOIN store_item_details sid USING(medicine_id) " +
		" JOIN store_category_master ON category_id = med_category_id " +
		" JOIN manf_master m ON m.manf_code = sid.manf_name ) as foo " ;

	public PagedList list(Map requestParams, Map pagingParams)
	throws ParseException,SQLException{
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("store_rate_plan_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public BasicDynaBean getItemRates(int medicineId, int storetariffId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SEARCH_FIELDS+SEARCH_TABLES+" WHERE medicine_id = ? and store_rate_plan_id = ? ");
			ps.setInt(1, medicineId);
			ps.setInt(2, storetariffId);

			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static void exportRates(XSSFSheet workSheet, String deptFilter, String statusFilter, int storeRatePlanId,String codeType)
	throws SQLException, ParseException {

		String fields = "SELECT medicine_id,store_rate_plan_id, medicine_name, category_name,tax_type,tax_rate,item_code,code_type,selling_price_expr ";
		String fromTales = " FROM (SELECT pmd.medicine_id, pmd.medicine_name, mc.category AS category_name, selling_price_expr, " +
				"mm.manf_name, gn.generic_name,srp.store_rate_plan_id,srp.store_rate_plan_name,iir.tax_rate,iir.tax_type,sit.item_code,sit.code_type " +
				"FROM store_item_rates iir " +
				" JOIN store_rate_plans srp USING(store_rate_plan_id) " +
				" JOIN store_item_details pmd USING (medicine_id) " +
				" LEFT JOIN store_item_ha_code_types_view sit ON(sit.medicine_id = pmd.medicine_id)" +
				" LEFT JOIN manf_master mm ON pmd.manf_name=mm.manf_code " +
				" LEFT JOIN generic_name gn ON pmd.generic_name=gn.generic_code " +
				" LEFT JOIN store_category_master mc ON pmd.med_category_id=mc.category_id WHERE pmd.status= 'A' " +
				" ORDER BY srp.store_rate_plan_name) AS foo ";

		Connection con = null;
		PreparedStatement pstmt = null;
		Map<String, List<String>> columnNamesMap = new HashMap<String, List<String>>();
		List<String> itemList = new ArrayList<String>();
		itemList.addAll(aliasNamesToDbCOlNames.keySet());
		columnNamesMap.put("mainItems", itemList);

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder sqb = new SearchQueryBuilder(con, fields, null, fromTales);
			sqb.addFilter(SearchQueryBuilder.INTEGER,"store_rate_plan_id", "=",storeRatePlanId);
			sqb.appendToQuery(" (code_type ='"+codeType+"' OR (code_type is null OR code_type = ''))");
			sqb.addSecondarySort("medicine_name");
			sqb.build();

			pstmt = sqb.getDataStatement();
			List list = DataBaseUtil.queryToDynaList(pstmt);
			HsSfWorkbookUtils.createPhysicalCellsWithValues(list, columnNamesMap, workSheet, true,new int[]{0,1});

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);

		}

	}

	private static final String missing_store_item_rates =
			"INSERT INTO  store_item_rates " +
			"	SELECT ?,s.medicine_id,0 " +
			"		FROM store_item_details s " +
			"		WHERE NOT EXISTS ( " +
			"			SELECT medicine_id from store_item_rates smc " +
			"		WHERE smc.medicine_id=s.medicine_id AND smc.store_rate_plan_id=? ) " ;

	public boolean saveMissingItemRates(int storeRatePlanId)throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		boolean success = true;
		 try{
			 con = DataBaseUtil.getConnection();
			 con.setAutoCommit(false);

			 ps = con.prepareStatement(missing_store_item_rates);
			 ps.setInt(1, storeRatePlanId);
			 ps.setInt(2, storeRatePlanId);

			 success = (ps.executeUpdate() > 0);
		 }finally{
			 DataBaseUtil.commitClose(con, success);
		 }

		 return success;
	}
	
  private static final String GET_STORE_ITEM_SUBGROUP_DETAILS = "select stisg.item_subgroup_id, "
      + "isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name "
      + " from store_tariff_item_sub_groups stisg "
      + " left join item_sub_groups isg on (isg.item_subgroup_id = stisg.item_subgroup_id) "
      + " left join store_item_details sdt on (sdt.medicine_id = stisg.item_id) "
      + " left join item_groups ig on (ig.item_group_id = isg.item_group_id)"
      + " left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"
      + " where stisg.item_id = ? and stisg.store_rate_plan_id = ?";

  public static List<BasicDynaBean> getStoreItemSubGroupDetails(Integer itemId, Integer storeRatePlanId)
      throws SQLException {
    List list = null;
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_STORE_ITEM_SUBGROUP_DETAILS);
      ps.setInt(1, itemId);
      ps.setInt(2, storeRatePlanId);
      list = DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return list;
  }
  
  private static final String GET_DEFAULT_STORE_ID = "SELECT store_rate_plan_id "
  + " FROM store_rate_plans ORDER BY store_rate_plan_name LIMIT 1;";
  
  public Integer getDefaultStoreRatePlanid() throws SQLException {
    return DataBaseUtil.getIntValueFromDb(GET_DEFAULT_STORE_ID);
  }

}
