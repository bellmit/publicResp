package com.insta.hms.master.InsCatCenter;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author prasanna.kumar
 *
 */

public class InsCatCenterDAO extends GenericDAO {
	public InsCatCenterDAO() {
		super("insurance_category_center_master");
	}


	public static final String GET_CENTERS =
		" SELECT icm.center_id, icm.status, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, " +
		"	s.state_name, icm.inscat_center_id" +
		" FROM insurance_category_center_master  icm " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=icm.center_id) " +
		"	LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
		"	LEFT JOIN state_master s ON (s.state_id=c.state_id) " +
		" WHERE icm.category_id=?" +
		" ORDER BY s.state_name, c.city_name, hcm.center_name" ;

	public List getCenters(int categoryID) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CENTERS);
			ps.setInt(1, categoryID);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public boolean delete(Connection con, int categoryID) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM insurance_category_center_master where category_id=? and center_id != 0");
			ps.setInt(1, categoryID);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM insurance_category_center_master where category_id=? and center_id != 0");
			ps.setInt(1, categoryID);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer centerId, int categoryID) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM insurance_category_center_master where category_id=? and center_id = ?");
			ps.setInt(1, categoryID);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM insurance_category_center_master where category_id=? and center_id = ?");
			ps.setInt(1, categoryID);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	public boolean insert(Connection con, int copyFromplantype, int copyToplantype) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO insurance_category_center_master(inscat_center_id, category_id, center_id, status) " +
					"	(SELECT nextval('insurance_category_center_master_seq'), ?, center_id, status FROM insurance_category_center_master WHERE category_id=?)");
			ps.setInt(1, copyToplantype);
			ps.setInt(2, copyFromplantype);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
}
