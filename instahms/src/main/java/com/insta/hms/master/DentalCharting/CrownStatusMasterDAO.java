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

public class CrownStatusMasterDAO extends GenericDAO {

	public CrownStatusMasterDAO() {
		super("crown_status_master");
	}

	private static final String CROWN_STATUS_FIELDS = "SELECT *";
	private static final String CROWN_STATUS_COUNT = "SELECT count(crown_status_id)";
	private static final String CROWN_STATUS_TABLE = "FROM crown_status_master";

	public PagedList getCrownStatusdetails(Map filter, Map listing)throws SQLException,ParseException  {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try{
			qb = new SearchQueryBuilder(con,CROWN_STATUS_FIELDS,CROWN_STATUS_COUNT, CROWN_STATUS_TABLE,listing);
			qb.addFilterFromParamMap(filter);
			qb.build();
			return qb.getMappedPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if(qb!=null)qb.close();
		}
	}

	public boolean exists(String crownStatusDesc, int crownStatusID)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement("select crown_status_desc from crown_status_master "+
			"	where crown_status_id!=? and upper(trim(crown_status_desc))=upper(trim(?))");
			ps.setInt(1, crownStatusID);
			ps.setString(2, crownStatusDesc);
			rs = ps.executeQuery();
			if(rs.next())
				return true;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
			if(rs != null) rs.close();
		}
		return false;
	}
}