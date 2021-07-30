package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AssetComplaintsDAO extends GenericDAO{

	public AssetComplaintsDAO() {
		super("assert_complaint_master");
	}

	public int getNextComplaintId() throws SQLException {
		int complaintId = getNextSequence();
		return complaintId;
	}

	private static String FIXED_ASSET_FIELDS = " SELECT complaint_id, fam.asset_id,sid.medicine_name as asset_name,emp_name, acm.batch_no," +
			                                   " sid.medicine_name||'-'||acm.batch_no as asset_str, complaint_type, complaint_desc, complaint_status, complaint_closure_note, "+
			                                   " raised_date, assigned_date, resolved_date, closed_date, created_by, s.dept_name, s.center_id ";

	private static String FIXED_ASSET_COUNT = " SELECT count(*) ";

	private static String FIXED_ASSET_TABLES = " FROM assert_complaint_master acm " +
											   " join fixed_asset_master fam on acm.asset_id = fam.asset_id and acm.batch_no = fam.asset_serial_no " +
											   " join store_item_details sid on fam.asset_id = sid.medicine_id "+
											   " left join stores s on s.dept_id = fam.asset_dept ";

	public PagedList getComplaintMasterDetails(Map filter, Map<LISTING, Object> pagingParams)  throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = (String) pagingParams.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)pagingParams.get(LISTING.SORTASC);
		int pageSize = (Integer)pagingParams.get(LISTING.PAGESIZE);
		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, FIXED_ASSET_FIELDS, FIXED_ASSET_COUNT, FIXED_ASSET_TABLES,
					null, sortField,sortReverse, pageSize, pageNum);

		qb.addFilterFromParamMap(filter);
		/*qb.addFilter(SearchQueryBuilder.NUMERIC, "complaint_status", "IN", statusList);

		if(asset_id != null && !asset_id.equals("")){
			qb.addFilter(SearchQueryBuilder.NUMERIC, "asset_id", "=", new BigDecimal(Integer.parseInt(asset_id)));
		}


		qb.addFilter(SearchQueryBuilder.STRING, "emp_name", "ILIKE", emp_name);

		qb.addFilter(SearchQueryBuilder.STRING, "complaint_type", "ILIKE", type);
		qb.addFilter(SearchQueryBuilder.STRING, "complaint_desc", "ILIKE", complaintDesc);
		if (raisedDate != null && !raisedDate.equals("")){
			qb.addFilter(SearchQueryBuilder.DATE, "raised_date","=", raisedDate);
		}*/
		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List fixedAssetList = DataBaseUtil.queryToDynaList(psData);

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		psData.close();psCount.close();con.close();

		return new PagedList(fixedAssetList,count,25,pageNum);
	}


	public static final String GET_ALL_COMPLAINT_DETAILS = "SELECT complaint_id, asset_id,asset_name ,emp_name,  " +
			"complaint_type, complaint_desc, complaint_status, complaint_closure_note, created_by  " +
			"FROM assert_complaint_master  join fixed_asset_master using(asset_id)" ;

	public static List getAllComplaintMasters(){

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList assetList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_COMPLAINT_DETAILS);
		assetList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return assetList;
	}

	public static final String GET_COMPLAINT_DETAILS = "SELECT acm.complaint_id, acm.asset_id,sid.medicine_name as asset_name ," +
			"acm.emp_name,  acm.complaint_type, acm.complaint_desc, acm.complaint_status, acm.batch_no," +
			"acm.complaint_closure_note, to_char(acm.raised_date,'dd-MM-yyyy') as raised_date, to_char(acm.assigned_date,'dd-MM-yyyy') as assigned_date, "+
			"to_char(acm.resolved_date,'dd-MM-yyyy') as resolved_date, to_char(acm.closed_date,'dd-MM-yyyy') as closed_date, created_by, lm.location_name  "+
			"FROM assert_complaint_master acm  " +
			"join fixed_asset_master fam on acm.asset_id = fam.asset_id and acm.batch_no = fam.asset_serial_no "+
			"join store_item_details sid on fam.asset_id = sid.medicine_id " +
			"left join location_master lm on lm.location_id = fam.asset_location_id "+
			"where acm.complaint_id= ? ";

	public static List getComplaintMasters(BigDecimal complaint_id){

	Connection con = null;
	PreparedStatement ps = null;
	ArrayList assetList = null;

	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_COMPLAINT_DETAILS);
		ps.setBigDecimal(1, complaint_id);
		assetList = DataBaseUtil.queryToArrayList(ps);

	} catch (SQLException e) {
		Logger.log(e);
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return assetList;
	}


	public static final String GET_ASSET_COMPLAINT_DETAILS = " ";

	public static List<BasicDynaBean> getAssetCommplaintDetails(){

	Connection con = null;
	PreparedStatement ps = null;
	 List<BasicDynaBean> beanList = null;

	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_ASSET_COMPLAINT_DETAILS);
		beanList = DataBaseUtil.queryToDynaList(ps);

	} catch (SQLException e) {
		Logger.log(e);
	}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return beanList;
	}

	public static final String GET_ALL_DEPTS = "SELECT dept_id,dept_name FROM stores " +
	" ORDER BY dept_name";
	public static List getAvalDeptnames() {
		PreparedStatement ps = null;
		ArrayList deptNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_DEPTS);
			deptNames = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return deptNames;
	}

	private static String ASSET_COMPLAINT_FIELDS = " select fam.asset_id,sitd.medicine_name as asset_name, d.dept_id, d.dept_name, " +
			" acm.complaint_type, acm.complaint_desc,(CASE WHEN acm.complaint_status =0 THEN 'Recorded' " +
			" WHEN acm.complaint_status =1 THEN 'Assigned' WHEN acm.complaint_status =2 THEN 'Resolved' " +
			" WHEN acm.complaint_status =3 THEN 'Closed'  ELSE 'NONE' END)  as comp_status  ";

	private static String ASSET_COMPLAINT_COUNT = " SELECT count(*) ";

	private static String ASSET_COMPLAINT_TABLES = " from assert_complaint_master acm " +
			" join fixed_asset_master fam using (asset_id) " +
			" join store_item_details sitd on (sitd.medicine_id = fam.asset_id) "+
			" join stores d on (d.dept_id = fam.asset_dept) ";

	public PagedList getAssetComplaintDetails(Map<LISTING, Object> pagingParams, List selDeptList, List selAssetList) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = (String) pagingParams.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)pagingParams.get(LISTING.SORTASC);
		int pageSize = (Integer)pagingParams.get(LISTING.PAGESIZE);
		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, ASSET_COMPLAINT_FIELDS, ASSET_COMPLAINT_COUNT, ASSET_COMPLAINT_TABLES,
					null, sortField,sortReverse, pageSize, pageNum);

		if(!(selDeptList != null && selAssetList != null)) {
			qb.addFilter(SearchQueryBuilder.STRING, "d.dept_id", "IN", selDeptList);
			qb.addFilter(SearchQueryBuilder.NUMERIC, "fam.asset_id", "IN", selAssetList);
		}


		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List fixedAssetList = DataBaseUtil.queryToDynaList(psData);

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		psData.close();psCount.close();con.close();

		return new PagedList(fixedAssetList,count,25,pageNum);
	}
}

