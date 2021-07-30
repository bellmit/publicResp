package com.insta.hms.master.TpaMasterCenterAssociation;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
/**
 * @author nikunj.s
 *
 */
public class CenterDAO extends GenericDAO {
	public CenterDAO() {
		super("tpa_center_master");
	}

	public static final String GET_CENTERS =
		" SELECT tcm.center_id, tcm.status, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, " +
		"	s.state_name, tcm.tpa_center_id,tcm.claim_format " +
		" FROM tpa_center_master tcm " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=tcm.center_id) " +
		"	LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
		"	LEFT JOIN state_master s ON (s.state_id=c.state_id) " +
		" WHERE tcm.tpa_id=?" +
		" ORDER BY s.state_name, c.city_name, hcm.center_name" ;
	public List getCenters(String tpaId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CENTERS);
			ps.setString(1, tpaId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean delete(Connection con, String tpaId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM tpa_center_master where tpa_id=? and center_id != -1");
			ps.setString(1, tpaId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM tpa_center_master where tpa_id=? and center_id != -1");
			ps.setString(1, tpaId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer centerId, String tpaId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM tpa_center_master where tpa_id=? and center_id = ?");
			ps.setString(1, tpaId);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM tpa_center_master where tpa_id=? and center_id = ?");
			ps.setString(1, tpaId);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean insert(Connection con, int copyFromTpa, int copyToTpa) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO tpa_center_master(tpa_center_id, tpa_id, center_id, status) " +
					"	(SELECT nextval('tpa_center_master_seq'), ?, center_id, status FROM tpa_center_master WHERE tpa_id=?)");
			ps.setInt(1, copyToTpa);
			ps.setInt(2, copyFromTpa);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
}
