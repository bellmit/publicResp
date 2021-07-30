package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MaintenanceScheduleDAO extends GenericDAO{

	public MaintenanceScheduleDAO() {
		super("asset_maintenance_master");
	}

	public int getNextMaintScheduleId() throws SQLException {
		int maintId = getNextSequence();
		return maintId;
	}

	private static String FIXED_ASSET_FIELDS = " SELECT maint_id, fam.asset_id,batch_no,medicine_name as asset_name, medicine_name||'-'||batch_no as asset_str, "+
												"maint_frequency, next_maint_date, s.dept_name, " +
												" department, department_contact, ams.contractor_id, contractor_name, cm.status, grade, address, " +
												" phone, fax, email, website, contact_person_name, contact_person_phone ,center_id ";


	private static String FIXED_ASSET_COUNT = " SELECT count(*) ";

	private static String FIXED_ASSET_TABLES = " FROM asset_maintenance_master ams  " +
												" LEFT JOIN fixed_asset_master fam ON ams.asset_id = fam.asset_id and ams.batch_no = fam.asset_serial_no " +
												" LEFT JOIN store_item_details sitd ON (sitd.medicine_id = fam.asset_id) "+
												" LEFT JOIN stores s ON (fam.asset_dept = s.dept_id) "+
												" LEFT JOIN contractor_master cm USING(contractor_id)";


	public PagedList getMaintScheduleDetails(Map filter, Map<LISTING, Object> pagingParams) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		String sortField = (String) pagingParams.get(LISTING.SORTCOL);
		boolean sortReverse = (Boolean)pagingParams.get(LISTING.SORTASC);
		int pageSize = (Integer)pagingParams.get(LISTING.PAGESIZE);
		int pageNum = (Integer)pagingParams.get(LISTING.PAGENUM);

		SearchQueryBuilder qb =
			new SearchQueryBuilder(con, FIXED_ASSET_FIELDS, FIXED_ASSET_COUNT, FIXED_ASSET_TABLES,
					null, sortField,sortReverse, pageSize, pageNum);
		qb.addFilterFromParamMap(filter);
		qb.build();

		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		List fixedAssetList = DataBaseUtil.queryToDynaList(psData);

		int count = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));

		psData.close();psCount.close();con.close();

		return new PagedList(fixedAssetList,count,25,pageNum);
	}


	public static final String GET_ALL_MAINT_SCHEDULES = "SELECT maint_id, fam.asset_id,medicine_name as asset_name, maint_frequency, next_maint_date," +
			" contractor_id, contractor_name, phone, address, department, department_contact, " +
			" fax, email, website, contact_person_name, contact_person_phone, ams.batch_no, medicine_name||'-'||ams.batch_no as asset_str " +
			" FROM asset_maintenance_master ams " +
			" LEFT JOIN fixed_asset_master fam ON ams.asset_id = fam.asset_id and ams.batch_no = fam.asset_serial_no " +
			" LEFT JOIN store_item_details sitd ON (sitd.medicine_id = fam.asset_id) " +
			" LEFT JOIN stores s ON (s.dept_id = fam.asset_dept) "+
			" LEFT JOIN contractor_master cm USING(contractor_id)";

	public static List getAllMaintSchedules(){

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList assetList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_MAINT_SCHEDULES);
			assetList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return assetList;
	}

	public static final String GET_MAINT_DETAILS = " select fam.asset_id, medicine_name as asset_name, ams.next_maint_date, " +
			" ama.maint_date, 'overdue' as status " +
	" from  fixed_asset_master fam " +
	" join  asset_maintenance_master ams using (asset_id)  " +
	" join store_item_details sitd on fam.asset_id = sitd.medicine_id " +
	" join stores s on fam.asset_dept = s.dept_id  "+
	" left join  asset_maintenance_activity ama on (ama.asset_id = ams.asset_id) " +
	" where ama.maint_date is null and (ams.next_maint_date < CURRENT_DATE)  " +

	" union all " +

	" (select fam.asset_id, medicine_name as asset_name, ams.next_maint_date,  " +
	"  ama.maint_date,'upcoming' as status from  fixed_asset_master fam " +
	"  join  asset_maintenance_master ams using (asset_id)  " +
	"  join store_item_details sitd on fam.asset_id = sitd.medicine_id "+
	"  join stores s on fam.asset_dept = s.dept_id "+
	"  left join  asset_maintenance_activity ama on (ama.asset_id = ams.asset_id)  " +
	"  where  ama.maint_date is null and (ams.next_maint_date > CURRENT_DATE) order by next_maint_date)";

	public static List<BasicDynaBean> getMaintScheduleDetails(){

		Connection con = null;
		PreparedStatement ps = null;
		 List<BasicDynaBean> beanList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MAINT_DETAILS);
			beanList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		}finally{
		DataBaseUtil.closeConnections(con, ps);
	}
	return beanList;
	}

	private static final String GET_LAST_UPDATED_MAINT_DATE = "SELECT to_date(to_char(max(maint_date), 'dd-mm-yyyy'), 'dd-mm-yyyy') " +
			" FROM asset_maintenance_activity " +
			" WHERE  asset_id  = ? AND batch_no = ? ";

	public static Date getLastUpdatedMaintDate(int asset_id, String batch_no) throws SQLException {
		Date lastMaintDate=null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_LAST_UPDATED_MAINT_DATE );
			ps.setInt(1, asset_id);
			ps.setString(2, batch_no);
			lastMaintDate = DataBaseUtil.getDateValueFromDb(ps);
		}catch (SQLException e) {
		  Logger.log(e);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return lastMaintDate;
	}

	private static final String GET_MAINT_FREQUENCY = "SELECT maint_frequency FROM asset_maintenance_master WHERE asset_id = ? AND batch_no = ?";

	public static String getMaintFrequency(Connection con, int asset_id, String batch_no) throws SQLException {
		PreparedStatement ps = null;
		String freq = "";
		try{
			ps = con.prepareStatement(GET_MAINT_FREQUENCY);
			ps.setInt(1, asset_id);
			ps.setString(2, batch_no);
			freq = DataBaseUtil.getStringValueFromDb(ps);
		}catch (SQLException e) {
		  Logger.log(e);
		}finally {
			if(ps != null)
				ps.close();
		}
		return freq;
	}
}
