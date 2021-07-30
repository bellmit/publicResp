package com.insta.hms.master.DentalCharting;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

public class ToothRootStatusMasterDAO extends GenericDAO {

	public ToothRootStatusMasterDAO() {
		super("tooth_root_status");
	}

	private static final String TOOTH_ROOT_STATUS_FIELDS = "SELECT *";
	private static final String TOOTH_ROOT_STATUS_COUNT = "SELECT count(root_status_id)";
	private static final String TOOTH_ROOT_STATUS_TABLES = "FROM tooth_root_status";


	public PagedList getToothRootStatusDetails(Map filter, Map listing)throws SQLException,ParseException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con,TOOTH_ROOT_STATUS_FIELDS,TOOTH_ROOT_STATUS_COUNT,TOOTH_ROOT_STATUS_TABLES,listing);
			qb.addFilterFromParamMap(filter);
			qb.build();
			return qb.getMappedPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if(qb!=null) qb.close();
		}
	}

	public Boolean exists(int rootStatusID, String rootStatusDesc)throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("select root_status_desc from tooth_root_status "+
					" where root_status_id!=? and upper(trim(root_status_desc))=upper(trim(?))");
			ps.setInt(1, rootStatusID);
			ps.setString(2, rootStatusDesc);
			rs = ps.executeQuery();
			if(rs.next()) return true;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
			if(rs!=null) rs.close();
		}
		return false;
	}
}