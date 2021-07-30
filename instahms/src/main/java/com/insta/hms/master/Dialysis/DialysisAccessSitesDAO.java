package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DialysisAccessSitesDAO extends GenericDAO {

	public DialysisAccessSitesDAO() {

		super("dialysis_access_sites");

	}

	private static final String GET_ALL_ACCSITES = "SELECT access_site_id, access_site FROM dialysis_access_sites";

	public List getAvalDialAccSites () {

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList dialAccSites = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_ACCSITES);
			dialAccSites = DataBaseUtil.queryToArrayList(ps);
		}catch(SQLException e) {
			Logger.log(e);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return dialAccSites;
	}
}