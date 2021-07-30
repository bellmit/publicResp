/**
 * @author - Sumathi K
 * Modified By :Raju
 * Modified Date : 20/04/08
 * */
package com.insta.hms.master.StoreMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreMasterDAO extends GenericDAO {

	public StoreMasterDAO(){
		super("stores");
	}

  private static Logger logger = LoggerFactory.getLogger(StoreMasterDAO.class);
  
	private static final String STORE_DETAILS =
		"SELECT s.*, hcm.center_name, hcm.status FROM stores s JOIN hospital_center_master hcm ON (hcm.center_id=s.center_id)" +
		"WHERE s.dept_id=?";
	public BasicDynaBean getStoreDetails(int storeId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(STORE_DETAILS);
			ps.setInt(1, storeId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String ALL_STORES = "SELECT * FROM stores";
	public List getStrores(int centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if (centerId != 0) {
				ps = con.prepareStatement(ALL_STORES + " WHERE center_id=? ORDER BY dept_name");
				ps.setInt(1, centerId);
			} else {
				ps = con.prepareStatement(ALL_STORES + " ORDER BY dept_name ");
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	private static final String LIST = "SELECT * FROM stores";
	public List getList(Connection con) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(LIST);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static String STORE_FIELDS = " select *";

	private static String STORE_COUNT = " SELECT count(dept_name) ";

	private static String STORE_TABLES = " from ( select gd.dept_id, gd.dept_name, gd.counter_id, gd.status, " +
			"	gd.purchases_store_vat_account_prefix, gd.purchases_store_cst_account_prefix, " +
			"	gd.sales_store_vat_account_prefix, c.counter_no, gd.center_id, hcm.center_name,gd.allow_auto_po_generation " +
			" 	FROM stores gd " +
			"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id = gd.center_id) "+
			"	LEFT OUTER JOIN counters c ON (gd.counter_id = c.counter_id AND gd.center_id = c.center_id))AS dept ";

	public PagedList getStoreDetailPages(Map map, Map pagingParams, int centerId)throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, STORE_FIELDS, STORE_COUNT, STORE_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			if (centerId != 0 ) {
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", new Integer(centerId));
			}
			qb.addSecondarySort("dept_id");
			qb.build();

			return qb.getMappedPagedList();
		}finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static String AG_COUNT_FOR_COUNTER =
			" SELECT COUNT(account_group), account_group, account_group_name FROM stores " +
			" 	JOIN account_group_master gm ON gm.account_group_id=account_group " +
			" WHERE counter_id=? AND account_group!=? and dept_id!=? GROUP BY account_group, account_group_name ";
	/*
	 * returns true
	 * 	1) if counter is not associated account group.
	 *  2) if counter is not associated the other account group(except the one selected.)
	 */
	public String validateCounter(String counterId, Connection con, Integer accountGroup, Integer deptId)
			throws SQLException {
		if (counterId.equals("")|| deptId == null) return null;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps= con.prepareStatement(AG_COUNT_FOR_COUNTER);
			ps.setString(1, counterId);
			ps.setInt(2, accountGroup);
			ps.setInt(3, deptId);
			rs = ps.executeQuery();
			if (rs.next()) {
				// counter is already associated with another account group.
				return rs.getString("account_group_name");
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return null;
	}



	public String getNextStoreId() throws SQLException {
		String deptId = null;
		deptId =	AutoIncrementId.getNewIncrUniqueId("DEPT_ID","stores", "GENERALDEPARTMENTS");

		return deptId;
	}

	/**
	 *
	 * @param strDeptId
	 * @return
	 */
	public String getDeptName(String strDeptId) {
		try {
      return DataBaseUtil.getStringValueFromDb("SELECT DEPT_NAME FROM stores WHERE DEPT_ID=?",
          strDeptId);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      logger.info("Query execution failed to fetch dept name for dept id : " + strDeptId);
      return null;
    }
	}

	private static final String GET_COUNTER_ID_AND_NOS = "SELECT COUNTER_ID,COUNTER_NO FROM COUNTERS"
	    + " WHERE STATUS='A' ORDER BY COUNTER_NO";
	/**
	 * This metod get the Counters from database
	 *
	 * @return ArrayList
	 */
	public ArrayList getCounters() {
		ArrayList counters = null;
		try (Connection con = DataBaseUtil.getConnection();
		  PreparedStatement ps = con.prepareStatement(GET_COUNTER_ID_AND_NOS,
		      ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
			counters = DataBaseUtil.queryToArrayList(ps);
		} catch(Exception e) {
			logger.info("Failed to fetch active counters from DB");
		}
		return counters;
	}

	public static  String GET_PHARMACY_STORES="SELECT gd.dept_id,gd.dept_name FROM stores gd " +
							" JOIN counters c ON c.counter_id=gd.counter_id AND c.counter_type='P' AND c.status='A'" +
							" WHERE  gd.status='A'";

	public static ArrayList getPharmacyStores() throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con=DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PHARMACY_STORES);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);

		}

	}
	public static  String GET_ALL_PHARMACY_STORES="SELECT dept_id,dept_name as store_name FROM stores ";


	public static List getAllPharmacyStores() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ALL_PHARMACY_STORES);
	}

	private static final String GET_TEMPLATES =
	    " SELECT TEMPLATE_NAME FROM PHARMACY_PRINT_TEMPLATE ";

	public static String getTemplates() throws SQLException {
		PreparedStatement ps = null;Connection con = null;
		try {
			JSONSerializer js = new JSONSerializer().exclude("class");
			con = DataBaseUtil.getReadOnlyConnection();
		    ps = con.prepareStatement(GET_TEMPLATES);
		    return js.serialize(DataBaseUtil.queryToArrayList1(ps));
	    }finally {
		  DataBaseUtil.closeConnections(con, ps);
	    }
    }


	/*
	 * gets the all counters and associated account groups.
	 */
	private static final String GET_COUNTERS_AND_ACCOUNT_GROUPS =
		" SELECT c.counter_id, account_group_name, account_group_id FROM counters c " +
		" JOIN stores gd ON (c.counter_id=gd.counter_id AND c.status='A') " +
		" JOIN account_group_master gm ON (gm.account_group_id=gd.account_group AND gm.status='A')" +
		" GROUP BY c.counter_id, account_group_id, account_group_name";
	public static List getCountersAndAccountGroups() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_COUNTERS_AND_ACCOUNT_GROUPS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List getStores() throws SQLException {
		GenericDAO dao = new GenericDAO("stores");
		return dao.listAll();

	}


	private static final String STORES_NAMESAND_iDS="SELECT dept_name,dept_id FROM  stores";

	  public static List getStoresNamesAndIds() throws SQLException{

		  return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(STORES_NAMESAND_iDS));
	  }

	public static  String GET_STORES_IN_MASTER="SELECT * FROM stores WHERE status='A'";

	public static ArrayList getStoresInMaster() throws SQLException {
		PreparedStatement ps = null;
		Connection con=null;
		try {
			con=DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_STORES_IN_MASTER);
			return DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);

		}

	}
	public static HashMap getStoreDetails() throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		HashMap <String,String>storeHasMap = new HashMap <String, String>();
		ResultSet rs=null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(STORES_NAMESAND_iDS);
		rs=ps.executeQuery();
		while(rs.next()){
			storeHasMap.put(rs.getString("dept_name"), rs.getString("dept_id"));
		}
		DataBaseUtil.closeConnections(con, ps,rs);
		return storeHasMap;
	}

	private static final String ACCOUNT_ID = "SELECT account_group FROM stores WHERE counter_id = ?";

	public static Integer getAccountId(String counterId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(ACCOUNT_ID);
			pstmt.setString(1, counterId);
			rs = pstmt.executeQuery();
			if (rs != null && rs.next())
				return rs.getInt(1);
			return 0;
		} finally {
			DataBaseUtil.closeConnections(con, pstmt, rs);
		}
	}

	private static final String STERILE_STORES = "SELECT * FROM stores WHERE is_sterile_store = 'Y'";

	public static List<BasicDynaBean> getCenterSterileStores(int centerId) throws SQLException {
		if (centerId != 0) {
			return DataBaseUtil.queryToDynaList(STERILE_STORES + " AND center_id=?", centerId);
		} else {
			return DataBaseUtil.queryToDynaList(STERILE_STORES);
		}
	}
	
	public static String STORES_FOR_AUTO_PO_GENERATION=
			  "	SELECT	 * 	FROM stores  "
			+ "	WHERE allow_auto_po_generation = 'Y' " 
	        + " 	AND  ( last_auto_po_date IS null "
	        + "					OR (current_date - to_char(last_auto_po_date, 'MM-DD-YYYY')::date "
	        + "				 >= auto_po_generation_frequency_in_days) ) AND center_id = ? ";
		
		public List<BasicDynaBean> getStoresForAutoPO(int centerId) throws SQLException {
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(STORES_FOR_AUTO_PO_GENERATION);
				ps.setInt(1, centerId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
			
		}
		
		
		public static String STORES_FOR_AUTO_PO_CANCEL =
			"SELECT foo.po_no,foo.dept_name,foo.dept_id from ( " +
			 " SELECT POM.po_no, s.dept_id, s.dept_name,s.center_id, " +
			 " (SELECT COUNT(*) FROM store_grn_main as sgm WHERE sgm.po_no = POM.po_no) AS grn_count "+ 
				 " FROM  store_po_main POM JOIN stores s ON(pom.store_id = s.dept_id) " +
				 " WHERE s.allow_auto_cancel_po = 'Y' AND " +
				  "((to_char(current_date, 'MM-DD-YYYY')::date - to_char(POM.po_date, 'MM-DD-YYYY')::date) >= s.auto_cancel_po_frequency_in_days) AND POM.status NOT IN ('X','FC','C') "+ 
				 " ORDER BY POM.store_id ) as foo where  foo.grn_count = ? AND foo.center_id = ? ";
		
		public List<BasicDynaBean> getStoresForAutoPOCancel(int centerId) throws SQLException {
			Connection con = null;
			PreparedStatement ps = null;
			try {
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(STORES_FOR_AUTO_PO_CANCEL);
				ps.setInt(1, 0);
				ps.setInt(2, centerId);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
			
		}
}


