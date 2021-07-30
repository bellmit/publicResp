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

public class ToothSurfaceOptionMasterDAO extends GenericDAO {

	public ToothSurfaceOptionMasterDAO() {
		super("tooth_surface_option_master");
	}

	private static final String TOOTH_SURFACE_OPTION_FIELDS = "SELECT *";
	private static final String TOOTH_SURFACE_OPTION_COUNT ="SELECT COUNT(option_id)";
	private static final String TOOTH_SURFACE_OPTION_TABLES = "FROM tooth_surface_option_master";

	public PagedList getToothSurfaceOptionlist(Map filter,Map listing) throws SQLException,ParseException 	{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con,TOOTH_SURFACE_OPTION_FIELDS,TOOTH_SURFACE_OPTION_COUNT,
					TOOTH_SURFACE_OPTION_TABLES,listing);
			qb.addFilterFromParamMap(filter);
			qb.build();
			return qb.getMappedPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if(qb!=null) qb.close();
		}
	}

	public boolean exists(int optionID,String optionName)throws SQLException,ParseException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = con.prepareStatement("select option_name from tooth_surface_option_master " +
					"where option_id!=? and upper(trim(option_name))=upper(trim(?))");
			ps.setInt(1, optionID);
			ps.setString(2, optionName);
			rs = ps.executeQuery();
			if(rs.next()) return true;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
			if(rs!=null) rs.close();
		}
		return false;
	}

}

