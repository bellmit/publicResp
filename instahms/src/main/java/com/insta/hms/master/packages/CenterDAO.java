/**
 *
 */
package com.insta.hms.master.packages;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna
 *
 */
public class CenterDAO extends GenericDAO {
	public CenterDAO() {
		super("center_package_applicability");
	}
	
	@Override
	public int getNextSequence() throws SQLException {
		String query = "SELECT nextval('center_package_applicability_seq')";
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		try {
			ps = con.prepareStatement(query);
			return DataBaseUtil.getIntValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_CENTERS =
		" SELECT pcm.center_id, pcm.status, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, " +
		"	s.state_name, pcm.package_center_id " +
		" FROM center_package_applicability pcm " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=pcm.center_id) " +
		"	LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
		"	LEFT JOIN state_master s ON (s.state_id=c.state_id) " +
		" WHERE pcm.package_id=?" +
		" ORDER BY s.state_name, c.city_name, hcm.center_name" ;
	public List getCenters(int packageId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CENTERS);
			ps.setInt(1, packageId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean delete(Connection con, Integer packId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM center_package_applicability where package_id=? and center_id != -1");
			ps.setInt(1, packId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM center_package_applicability where package_id=? and center_id != -1");
			ps.setInt(1, packId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer packId, Integer centerId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM center_package_applicability where package_id=? and center_id = ?");
			ps.setInt(1, packId);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM center_package_applicability where package_id=? and center_id = ?");
			ps.setInt(1, packId);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean insert(Connection con, int copyFromPackage, int copyToPackage) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO center_package_applicability(package_center_id, package_id, center_id, status) " +
					"	(SELECT nextval('center_package_applicability_seq'), ?, center_id, status FROM center_package_applicability WHERE package_id=?)");
			ps.setInt(1, copyToPackage);
			ps.setInt(2, copyFromPackage);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
}
