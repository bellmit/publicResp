package com.insta.hms.master.DiagnosticReagentMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiagnosticReagentMasterDAO extends GenericDAO {
	public String status = "Active";
	public DiagnosticReagentMasterDAO() {
		super("diagnostics_reagents");
	}
	private static String DIAG_FIELDS = " SELECT *";

	private static String DIAG_COUNT = "SELECT count(*)";

	private static String DIAG_TABLES = " FROM (SELECT dm.reagent_id,im.medicine_name as item_name,d.test_name," +
			" dm.quantity_needed, dm.test_id, dm.status" +
			" FROM store_item_details im " +
			" JOIN diagnostics_reagents dm ON dm.reagent_id = im.medicine_id " +
			" JOIN diagnostics d ON dm.test_id = d.test_id ) AS foo  ";

	public PagedList getDiagReagentDetailPages(Map requestParams, Map<LISTING, Object> pagingParams)
				throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, DIAG_FIELDS, DIAG_COUNT, DIAG_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("test_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public String getReagents(String test_id)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuffer reagents = new StringBuffer();
		int count = 0;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("select i.medicine_name as item_name,dr.status from store_item_details i join diagnostics_reagents dr on(i.medicine_id = dr.reagent_id) where dr.test_id=?  ");
			ps.setString(1, test_id);
			rs = ps.executeQuery();
			while(rs.next()){
				if(rs.getString("status").equalsIgnoreCase("I"))status = "Inactive";
				count++;
				if(count == 1)reagents.append(rs.getString("item_name"));
				else reagents.append(",").append(rs.getString("item_name"));
			}
			return reagents.toString();
		}finally{
			if(con!= null)con.close();
			if(ps != null)ps.close();
			if(rs != null)rs.close();
		}
	}
	private static String FIND_RECORD_BY_TEST = "SELECT d.test_name,dr.test_id,dr.reagent_id" +
			" ,(case when dr.status='A' then 'false' else 'true' end ) as status,i.medicine_name as item_name,dr.quantity_needed FROM  diagnostics_reagents dr" +
			" join diagnostics d using(test_id)" +
			" join store_item_details i on(dr.reagent_id = i.medicine_id) WHERE  dr.test_id = ? ";

	public List findTestBykey(String testid) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			 con = DataBaseUtil.getReadOnlyConnection();
			 ps = con.prepareStatement(FIND_RECORD_BY_TEST);
			 ps.setString(1, testid);
			 List list = DataBaseUtil.queryToDynaList(ps);
			 return list;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	private static String FIND_RECORD_BY_BOTH = "SELECT * FROM  diagnostics_reagents WHERE reagent_id = ? AND test_id = ? ";

	public BasicDynaBean findTestBykey(int reagentid,String testid) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		try {
			 con = DataBaseUtil.getReadOnlyConnection();
			 ps = con.prepareStatement(FIND_RECORD_BY_BOTH);

			 ps.setInt(1, reagentid);
			 ps.setString(2, testid);

			 List list = DataBaseUtil.queryToDynaList(ps);

			 if(list.size() > 0){
				 return (BasicDynaBean) list.get(0);
			 }else{
				 return null;
			 }
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	private static String GET_ALL_RECORDS = " SELECT reagent_id,test_id from diagnostics_reagents ";

	public ArrayList getAllRecords() throws SQLException{

		ArrayList reagentList = new ArrayList();
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_RECORDS);

			reagentList = DataBaseUtil.queryToArrayList(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return reagentList;
	}
	private static String TESTS_NOT_IN_REAGENT_MASTER = "SELECT test_id,test_name FROM diagnostics " +
			" WHERE status='A' AND  test_id NOT IN (SELECT test_id FROM diagnostics_reagents where status='I')" +
			" ORDER BY test_name";
	public List getTestToMapReagents()throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(TESTS_NOT_IN_REAGENT_MASTER);
			return DataBaseUtil.queryToArrayList(ps);
		}finally{
			if(ps != null)ps.close();
			if(con != null)con.close();
		}
	}
	public static final String DIAGNOSTIC_REAGENTS = "SELECT d.test_name,dr.test_id, dr.reagent_id," +
			"(case when dr.status='A' then 'true' else 'false' end ) as status,i.medicine_name as item_name,dr.quantity_needed as qty," +
			" 0 as reagent_usage_seq,0 as ref_no FROM  diagnostics_reagents dr" +
			"  join diagnostics d using(test_id) join store_item_details i on(dr.reagent_id = i.medicine_id) WHERE  dr.test_id = ?";
	public List getDiagnosticReagents(String testId)throws SQLException{
	Connection con = null;
	PreparedStatement ps = null;
	try{
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(DIAGNOSTIC_REAGENTS);
		ps.setString(1, testId);
		return DataBaseUtil.queryToDynaList(ps);
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
}

	private static final String TESTS_NAMESAND_iDS="select test_id,test_name from diagnostics";

	   public static List getTestsNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(TESTS_NAMESAND_iDS));
	}


}
