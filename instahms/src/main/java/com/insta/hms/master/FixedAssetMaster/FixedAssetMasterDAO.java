package com.insta.hms.master.FixedAssetMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FixedAssetMasterDAO extends GenericDAO{

	public FixedAssetMasterDAO() {
		super("fixed_asset_master");
	}

	public int getNextFixedAssetId() throws SQLException {
		int assetId = getNextSequence();
		return assetId;
	}

	private static String FIXED_ASSET_FIELDS = " select *  ";

	private static String FIXED_ASSET_COUNT = " SELECT count(*) ";


	private static String FIXED_ASSET_TABLES = " FROM (select medicine_name, asset_make,asset_model,category,asset_status,asset_serial_no,"+
		" asset_id, s.dept_name, asset_seq,cat.category_id, s.center_id,stk.dept_id "+
		" from fixed_asset_master fam "+
		" join store_item_details sid on medicine_id = asset_id  "+
		" join store_category_master cat on sid.med_category_id = cat.category_id  "+
		" join  (select ssd.dept_id, medicine_id,batch_no,sum(qty) as qty "+
				" from store_stock_details ssd group by medicine_id,batch_no,ssd.dept_id "+
				" ) as stk  on (sid.medicine_id = stk.medicine_id and stk.batch_no = fam.asset_serial_no and stk.qty > 0 ) "+
		" join stores s on stk.dept_id = s.dept_id) as foo ";


	public static PagedList getFixedAssetDetails (Map filter, Map listing, int deptId, int centerId) throws SQLException, ParseException{
		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				FIXED_ASSET_FIELDS, FIXED_ASSET_COUNT, FIXED_ASSET_TABLES, listing);
		if(deptId != -99999)
			qb.addFilter(qb.INTEGER, "dept_id", "=", deptId);
		if(centerId != 0)
			qb.addFilter(qb.INTEGER, "center_id", "=", centerId);
		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("asset_id");
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}




	/*private static String FIXED_ASSET_HISTORY_FIELDS = " select ama.maint_activity_id, ama.asset_id,ama.maint_date,amai.description, " +
													   " ama.maint_by,amai.item_id, amai.component, coalesce(amai.labor_cost,0)+coalesce(amai.part_cost,0) as cost," +
													   " coalesce(amai.labor_cost,0) as labor_cost, coalesce(amai.part_cost,0) as part_cost " +
													   " , ama.description as maint_description";
*/
	private static String FIXED_ASSET_HISTORY_FIELDS = "select fam.asset_id, fam.asset_status, sid.medicine_name as asset_name, " +
						" fam.asset_make,fam.asset_model,fam.asset_serial_no, cat.category, sid.medicine_name||'-'||fam.asset_serial_no as asset_str, " +
						" ama.maint_activity_id, ama.scheduled_date, ama.maint_date,amai.description,  ama.maint_by,amai.item_id, " +
						" amai.component, coalesce(amai.labor_cost,0)+coalesce(amai.part_cost,0) as cost, " +
	                    " coalesce(amai.labor_cost,0) as labor_cost, coalesce(amai.part_cost,0) as part_cost , " +
	                    " ama.description as maint_description ";

	private static String FIXED_ASSET_GROUP_FIELDS = "select * ";

	private static String FIXED_ASSET_GROUP_COUNT = " SELECT count(asset_id) ";

	private static String FIXED_ASSET_GROUP_TABLES = " from ( select fam.asset_id, fam.asset_dept, sid.medicine_name as asset_name, asset_serial_no, ama.maint_activity_id, "+
	"ama.scheduled_date, sid.medicine_name||'-'||asset_serial_no as asset_str, "+
	"ama.maint_date, ama.maint_by, ama.description, CASE WHEN scheduled_date < CURRENT_DATE THEN 'Yes' ELSE 'No' END as overdue, "+
						" CASE WHEN scheduled_date < CURRENT_DATE AND maint_date IS NULl THEN 'overdue' "+
						" WHEN scheduled_date < CURRENT_DATE AND maint_date IS NOT NULl THEN 'completed' "+
						" ELSE 'pending' END AS status, "+
						" coalesce(sum(amai.labor_cost),0)+coalesce(sum(amai.part_cost),0) as cost, s.dept_name, s.center_id " +
						 " from store_item_details sid "+
						" join fixed_asset_master fam on sid.medicine_id = fam.asset_id join stores s on fam.asset_dept = s.dept_id "+
						" join asset_maintenance_activity ama on fam.asset_id = ama.asset_id and fam.asset_serial_no = ama.batch_no "+
						" left outer join asset_maintenance_activity_item amai on ama.maint_activity_id = amai.maint_activity_id "+
						" group by fam.asset_id, fam.asset_dept, asset_name, asset_serial_no, ama.maint_activity_id,ama.scheduled_date,ama.maint_date, ama.maint_by, ama.description, s.dept_name, s.center_id) as foo ";


	private static String FIXED_ASSET_HISTORY_COUNT = " SELECT count(*) ";

	/*private static String FIXED_ASSET_HISTORY_TABLES = " from asset_maintenance_activity ama " +
			" join asset_maintenance_activity_item amai on ama.maint_activity_id = amai.maint_activity_id ";*/

	private static String FIXED_ASSET_HISTORY_TABLES = " from store_item_details sid "+
				" join fixed_asset_master fam on sid.medicine_id = fam.asset_id "+
				" join store_category_master cat on cat.category_id = sid.med_category_id "+
				" left join asset_maintenance_activity ama on fam.asset_id = ama.asset_id and fam.asset_serial_no = ama.batch_no "+
				" left outer join asset_maintenance_activity_item amai on ama.maint_activity_id = amai.maint_activity_id ";




	public PagedList getMaintActivityDetails(Map filter, Map<LISTING, Object> pagingParams) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = (String) pagingParams.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)pagingParams.get(LISTING.SORTASC);
		int pageSize = (Integer)pagingParams.get(LISTING.PAGESIZE);
		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, FIXED_ASSET_GROUP_FIELDS, FIXED_ASSET_GROUP_COUNT, FIXED_ASSET_GROUP_TABLES,
					null, sortField,sortReverse, pageSize, pageNum);
		qb.addFilterFromParamMap(filter);
		qb.addSecondarySort("asset_name");

		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List fixedAssetList = DataBaseUtil.queryToDynaList(psData);

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		psData.close();psCount.close();con.close();

		return new PagedList(fixedAssetList,count,25,pageNum);
	}


	public PagedList getFixedAssetHistory(BigDecimal asset_id) throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, FIXED_ASSET_HISTORY_FIELDS, FIXED_ASSET_HISTORY_COUNT, FIXED_ASSET_HISTORY_TABLES,
					null, null,false, 0, 0);

		qb.addFilter(qb.NUMERIC, "ama.asset_id", "=", asset_id);
		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List fixedAssetList = DataBaseUtil.queryToDynaList(psData);

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		psData.close();psCount.close();con.close();

		return new PagedList(fixedAssetList,count,0,0);
	}




	public static List getNewMaintAssetHistory(BigDecimal maint_activity_id) throws SQLException  {
		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, FIXED_ASSET_HISTORY_FIELDS, FIXED_ASSET_HISTORY_COUNT, FIXED_ASSET_HISTORY_TABLES,
					null, null,false, 0, 0);
		qb.addFilter(qb.NUMERIC, "ama.maint_activity_id", "=", maint_activity_id);
		qb.build();
		qb.addSecondarySort("component");
		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List mintActivityDetails = DataBaseUtil.queryToDynaList(psData);


		psData.close();psCount.close();con.close();

		return mintActivityDetails;
	}

	public static final String GET_MAINT_DETAILS = "SELECT maint_activity_id,asset_id,maint_date,maint_by, description, " +
			" scheduled_date, batch_no FROM asset_maintenance_activity" ;

	public static final String GET_ALL_ASSETS_DETAILS =" SELECT fam.asset_id, fam.asset_status, sitd.medicine_name as asset_name, fam.asset_make, "+
		 " fam.asset_model,  fam.asset_serial_no, fam.asset_dept,d.dept_name, cat.category as asset_category,fam.installation_date, fam.asset_purchase_val, "+
		 " (sum(coalesce(amai.labor_cost,0))+sum(coalesce(amai.part_cost,0))) as maint_cost FROM fixed_asset_master fam "+
		 " left JOIN  asset_maintenance_activity ama USING (asset_id) "+
		 " left JOIN  asset_maintenance_activity_item amai USING (maint_activity_id) "+
		 " left join stores d on (fam.asset_dept = d.dept_id) "+
		 " left join store_item_details sitd on (fam.asset_id = sitd.medicine_id) "+
		 " left join store_category_master cat on (cat.category_id = sitd.med_category_id) "+
		 " group by  fam.asset_id, fam.asset_status, sitd.medicine_name, fam.asset_make, fam.asset_model, "+
		 " fam.asset_serial_no,  fam.asset_dept,d.dept_name, fam.installation_date, cat.category, "+
		 " fam.asset_purchase_val "+
		 " ORDER BY sitd.medicine_name ";

	public static List<BasicDynaBean> getAllAssetDetails(){

		Connection con = null;
		PreparedStatement ps = null;
		 List<BasicDynaBean> beanList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_ASSETS_DETAILS);
			beanList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return beanList;
	}

	public static final String GET_ASSETS_DETAILS =" select fam.asset_id,asset_make,asset_model,asset_purchase_val,asset_seq,"
		+" asset_status,sid.medicine_name,category,category_id,asset_generic_name,asset_serial_no,installation_date,asset_bill_no,parent_asset_id,"
		+" sd.medicine_name as parent_asset,location_name,dept_name,asset_dept,asset_remarks"
        +" from fixed_asset_master fam"
        +" join store_item_details sid on medicine_id=asset_id "
        +" left join store_item_details sd on sd.medicine_id=parent_asset_id "
        +" join store_category_master on sid.med_category_id=category_id"
        +" left join location_master lm on lm.location_id=asset_location_id"
        +" join stores on dept_id=asset_dept"
        +" where asset_seq=?";

	public static BasicDynaBean getAssetDetails(int id) throws Exception{

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ASSETS_DETAILS);
			ps.setInt(1,id);
			List list = DataBaseUtil.queryToDynaList(ps);
			return (BasicDynaBean)list.get(0);

		} finally{
			DataBaseUtil.closeConnections(con, ps);
	    }

	}
	public static final String GET_FILE_SIZES = "SELECT count(*) " +
		" FROM fixed_asset_uploads where asset_seq=? ";

	public static int getFileSizes(int id) throws SQLException {
		int count = 0;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_FILE_SIZES);
			ps.setInt(1,id);
			rs = ps.executeQuery();
			while (rs.next()){
				count = rs.getInt(1);
			}
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return count;
	}

	public static final String GET_FILE_UPLODAS = "SELECT asset_file_name,asset_file_seq " +
	" FROM fixed_asset_uploads where asset_seq=? ";

	public static ArrayList getFilecount(int id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_FILE_UPLODAS);
			ps.setInt(1,id);
			return DataBaseUtil.queryToArrayList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private static final String GET_FORM = " SELECT asset_upload_file FROM fixed_asset_uploads "
			+" where asset_file_seq=?";

	public static InputStream getUplodedForm(int id) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_FORM);
			ps.setInt(1,id);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private static final String GET_FILENAME = " SELECT asset_file_name FROM fixed_asset_uploads "
		+" where asset_file_seq=?";

public static String getUplodedFile(int id) throws SQLException {

	Connection con = null;
	PreparedStatement ps = null;
	ResultSet rs = null;

	try {
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_FILENAME);
		ps.setInt(1,id);
		rs = ps.executeQuery();
		if (rs.next())
			return rs.getString(1);
		else
			return null;
	} finally {
		DataBaseUtil.closeConnections(con, ps, rs);
	}
}
	private static final String GET_UPLOAD_DATA = " SELECT asset_file_name, asset_upload_file, content_type FROM fixed_asset_uploads "
	+" where asset_file_seq=?";

    public static Map getUploadedData(int id) throws SQLException {
    	Map<String,Object> upload = new HashMap<String,Object>();
    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		ps = con.prepareStatement(GET_UPLOAD_DATA);
    		ps.setInt(1,id);
    		rs = ps.executeQuery();
    		if (rs.next()){
    			String filename = (String)rs.getString(1);
    			upload.put("filename", filename);
    			InputStream uploadfile = (InputStream)rs.getBinaryStream(2);
    			upload.put("uploadfile", uploadfile);
    			String contenttype = (String)rs.getString(3);
    			upload.put("contenttype", contenttype);

    		}
    	} finally {
    		DataBaseUtil.closeConnections(con, ps, rs);
    	}
    	return upload;

    }

	private static final String DELETE_FORM = " delete from fixed_asset_uploads where asset_file_seq=? ";

	public static boolean DeleteUplodedForm(int id) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(DELETE_FORM);
			ps.setInt(1,id);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}



	public static final String GET_ACTIVE_ASSETS =" SELECT distinct asset_id, ssd.batch_no, sid.medicine_id, asset_status, " +
	   " sid.medicine_name, asset_make, asset_model, " +
	   " asset_serial_no, cat.category, asset_dept, lm.location_name, asset_location_id, " +
	   " s.center_id,  s.dept_name  " +
	   "FROM fixed_asset_master join store_stock_details ssd on asset_id = medicine_id "+
	   "and asset_serial_no = batch_no and asset_dept = ssd.dept_id "+
	   "JOIN store_item_details sid using (medicine_id) "+
	   "join store_category_master cat on cat.category_id = sid.med_category_id  "+
	   "left join location_master lm on (lm.location_id = asset_location_id)  "+
	   "left join stores s on (s.dept_id = ssd.dept_id) "+
	   "WHERE cat.asset_tracking = 'Y' and asset_status = 'A' and ssd.asset_approved='Y'  " ;

	public static final String WHERE_ASSET_DEPT_IN = "and asset_dept in (SELECT regexp_split_to_table(multi_store, E'\\,')::integer as dept_id FROM u_user WHERE emp_username=?) ";

	public static final String ORDER_BY = "ORDER BY sid.medicine_name";

	/** This is the General method which return JSON data from any table
	 * @param query
	 * @return
	 * @throws SQLException
	 */

	public static String getTableDataInJSON(String query, String userName, int roleId, String multiStoreAccess, String pharmacyStore) throws SQLException {
		Connection con = null; PreparedStatement ps = null;ArrayList data = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if(roleId != 1 && roleId != 2 && multiStoreAccess.equals("A")) {
				if(query != null){
					ps = con.prepareStatement(query + WHERE_ASSET_DEPT_IN  + ORDER_BY);
				}else{
					ps = con.prepareStatement(GET_ACTIVE_ASSETS + WHERE_ASSET_DEPT_IN + ORDER_BY);
				}
				ps.setString(1, userName);
			} else {
				if(roleId != 1 && roleId != 2 && multiStoreAccess.equals("N") && pharmacyStore != null && !"".equals(pharmacyStore))
				{
					ps = con.prepareStatement(GET_ACTIVE_ASSETS + WHERE_ASSET_DEPT_IN + ORDER_BY);
					ps.setString(1, userName);
				} else {
					ps = con.prepareStatement(GET_ACTIVE_ASSETS  + ORDER_BY);
				}
			}
			JSONSerializer js = new JSONSerializer().exclude("class");
			data = DataBaseUtil.queryToArrayList(ps);
			return js.serialize(data);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String UPDATE_NEXT_MAINT_DATE = "UPDATE asset_maintenance_master  SET next_maint_date = ( "+
		" SELECT MAX(maint_date) FROM asset_maintenance_activity WHERE asset_id = ? AND batch_no =?) + ? "+
		" WHERE asset_id = ? AND batch_no = ?  ";

	public static int UpdateNextMaintDate(Connection con, int asset_id, String batch_no, int noOfDays) throws SQLException {
		PreparedStatement ps = null;
		int success = 0;
		try {
			ps = con.prepareStatement(UPDATE_NEXT_MAINT_DATE);
			ps.setInt(1, asset_id);
			ps.setString(2, batch_no);
			ps.setInt(3, noOfDays);
			ps.setInt(4, asset_id);
			ps.setString(5, batch_no);
			success = ps.executeUpdate();
		}catch (Exception e){
			e.printStackTrace();
		} finally {
			if (ps != null)
				ps.close();
		}
		return success;
	}

}