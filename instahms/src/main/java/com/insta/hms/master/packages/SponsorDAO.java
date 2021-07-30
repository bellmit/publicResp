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
public class SponsorDAO extends GenericDAO {
	public SponsorDAO() {
		super("package_sponsor_master");
	}

	public List getSponsors(int packageId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
					" SELECT tpa_name, psm.tpa_id, psm.status, package_sponsor_id " +
					" FROM package_sponsor_master psm	" +
					"	LEFT JOIN tpa_master tm ON (tm.tpa_id=psm.tpa_id)" +
					" WHERE pack_id=?" +
					" ORDER BY tpa_name");
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
			ps = con.prepareStatement("SELECT * FROM package_sponsor_master where pack_id=? and tpa_id != '-1'");
			ps.setInt(1, packId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM package_sponsor_master where pack_id=? and tpa_id != '-1'");
			ps.setInt(1, packId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer packId, String tpaId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM package_sponsor_master where pack_id=? and tpa_id = ?");
			ps.setInt(1, packId);
			ps.setString(2, tpaId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM package_sponsor_master where pack_id=? and tpa_id = ?");
			ps.setInt(1, packId);
			ps.setString(2, tpaId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean insert(Connection con, int copyFromPackage, int copyToPackage) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO package_sponsor_master(package_sponsor_id, pack_id, tpa_id, status) " +
					"	(SELECT nextval('package_sponsor_master_seq'), ?, tpa_id, status FROM package_sponsor_master WHERE pack_id=?)");
			ps.setInt(1, copyToPackage);
			ps.setInt(2, copyFromPackage);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
}
