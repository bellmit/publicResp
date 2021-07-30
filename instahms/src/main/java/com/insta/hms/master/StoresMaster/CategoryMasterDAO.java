package com.insta.hms.master.StoresMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryMasterDAO  extends GenericDAO{

	public CategoryMasterDAO() {
		super("store_category_master");
	}
	private static String CATEGORY_FIELDS = " SELECT * ";
	private static String CATEGORY_COUNT = " SELECT count(*) ";
	private static String CATEGORY_TABLES = " FROM store_category_master  ";

	public PagedList list1 (Map filter, Map listing) throws Exception{
		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				CATEGORY_FIELDS, CATEGORY_COUNT, CATEGORY_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("category_id");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}



	private static String TEST_CATEGORY = "select count(*),med_category_id from store_stock_details ims," +
			"store_item_details iid where  ims.medicine_id = iid.medicine_id and med_category_id = ? group by med_category_id";

	public String testCategoryUsed(String catId) throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		String list = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(TEST_CATEGORY);
			ps.setInt(1, Integer.parseInt(catId));
			list = DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);

		}

		return list;
	}

	private static String GET_PACKAGE_TYPE="SELECT DISTINCT(package_type) FROM store_item_details";

	public static List getpackageType() throws SQLException {

		return DataBaseUtil.queryToDynaList(GET_PACKAGE_TYPE);
	}

	public static HashMap getIssueTypes() {

		HashMap issueTypes = null;
		issueTypes = new HashMap();
		ArrayList issueTypeNames = null;
		ArrayList issueTypeValues = null;
		issueTypeNames = new ArrayList();
		issueTypeValues = new ArrayList();
		issueTypeNames.add("All");
		issueTypeNames.add("Permanent");
		issueTypeNames.add("Consumable");
		issueTypeNames.add("Reusable");
		issueTypeValues.add("*");
		issueTypeValues.add("P");
		issueTypeValues.add("C");
		issueTypeValues.add("L");
		issueTypes.put("issueType_Names", issueTypeNames);
		issueTypes.put("issueType_Values", issueTypeValues);

		return issueTypes;
	}

	public static HashMap getIdentification() {

		HashMap identification = null;
		identification = new HashMap();
		ArrayList identificationNames=null;
		ArrayList identificationValues=null;
		identificationNames=new ArrayList();
		identificationValues=new ArrayList();
		identificationNames.add("All");
		identificationNames.add("Batch");
		identificationNames.add("Serial");
		identificationValues.add("*");
		identificationValues.add("B");
		identificationValues.add("S");

		identification.put("identification_Names", identificationNames);
		identification.put("identification_Values", identificationValues);

		return identification;
	}


	private static final String CATEGORIES_NAMESAND_iDS="SELECT category,category_id FROM  store_category_master";

	  public static List getCategoriesNamesAndIds() throws SQLException{

		  return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(CATEGORIES_NAMESAND_iDS));
	  }

	  public static HashMap getStoreCategoryDetails() throws SQLException {

			Connection con = null;
			PreparedStatement ps = null;
			HashMap <String,String>storeCategoryHasMap = new HashMap <String, String>();
			ResultSet rs=null;
			con=DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CATEGORIES_NAMESAND_iDS);
			rs=ps.executeQuery();
			while(rs.next()){
				storeCategoryHasMap.put(rs.getString("category"), rs.getString("category_id"));
			}
			DataBaseUtil.closeConnections(con, ps,rs);
			return storeCategoryHasMap;
		}
	  
	  private static final String GET_CATEGORY_NAME_BY_ID = "SELECT category FROM store_category_master WHERE category_id = ?";

		public static String getCategoryNameById(Integer categoryId) throws SQLException {
			PreparedStatement ps = null;
			Connection con = null;

			try {
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_CATEGORY_NAME_BY_ID);
				ps.setInt(1, (null == categoryId ? -1 : categoryId));
				return DataBaseUtil.getStringValueFromDb(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

	private static final String COUNT_DISCOUNT_PLANS = "SELECT COUNT(*) FROM discount_plan_details"
	    + " dpd, discount_plan_main dpm  WHERE (dpd.discount_plan_id= dpm.discount_plan_id) AND "
	    + " dpm.status = 'A' AND applicable_to_id =? AND applicable_type='S'";

	public static boolean isCategoryInDiscPlan(Integer catId) throws SQLException {
		Connection con = null;
		int entries = 0;
		PreparedStatement ps = null;
		try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(COUNT_DISCOUNT_PLANS);
		ps.setString(1, (null == catId ? "-1" : catId.toString()));
		entries = Integer.parseInt(DataBaseUtil.getStringValueFromDb(ps));
		} finally {
		con.close();
		}
		if (entries > 0) return true;
		return false;
	}
}
