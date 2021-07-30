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

public class ToothSurfaceStatusMasterDAO extends GenericDAO {

	public ToothSurfaceStatusMasterDAO() {
		super("tooth_surface_status_master");
	}

	private static final String TOOTH_SURFACE_STATUS_FIELDS = "SELECT *";
	private static final String TOOTH_SURFACE_STATUS_COUNT ="SELECT COUNT(surface_status_id)";
	private static final String TOOTH_SURFACE_STATUS_TABLES = "FROM tooth_surface_status_master";

	public PagedList getToothSurfaceStatuslist(Map filter,Map listing) throws SQLException,ParseException 	{
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con,TOOTH_SURFACE_STATUS_FIELDS,TOOTH_SURFACE_STATUS_COUNT,
					TOOTH_SURFACE_STATUS_TABLES,listing);
			qb.addFilterFromParamMap(filter);
			qb.build();
			return qb.getMappedPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if(qb!=null) qb.close();
		}
	}

	public boolean exists(int surfaceStatusID,String surfaceStatusName)throws SQLException,ParseException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = con.prepareStatement("select surface_status_name from tooth_surface_status_master " +
					"where surface_status_id!=? and upper(trim(surface_status_name))=upper(trim(?))");
			ps.setInt(1, surfaceStatusID);
			ps.setString(2, surfaceStatusName);
			rs = ps.executeQuery();
			if(rs.next()) return true;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
			if(rs!=null) rs.close();
		}
		return false;
	}
}