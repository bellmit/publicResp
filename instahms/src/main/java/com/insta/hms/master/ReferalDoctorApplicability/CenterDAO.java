package com.insta.hms.master.ReferalDoctorApplicability;

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
public class CenterDAO extends GenericDAO {
	public CenterDAO() {
		super("referral_center_master");
	}

	public static final String GET_CENTERS =
		" SELECT rcm.center_id, rcm.status, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, " +
		"	s.state_name, rcm.referal_center_id" +
		" FROM referral_center_master  rcm " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=rcm.center_id) " +
		"	LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
		"	LEFT JOIN state_master s ON (s.state_id=c.state_id) " +
		" WHERE rcm.referal_no=?" +
		" ORDER BY s.state_name, c.city_name, hcm.center_name" ;

	public List getCenters(String referalNo) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CENTERS);
			ps.setString(1, referalNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean delete(Connection con, String referalno) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM referral_center_master where referal_no=? and center_id != 0");
			ps.setString(1, referalno);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM referral_center_master where referal_no=? and center_id != 0");
			ps.setString(1, referalno);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer centerId, String referalno) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM referral_center_master where referal_no=? and center_id = ?");
			ps.setString(1, referalno);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM referral_center_master where referal_no=? and center_id = ?");
			ps.setString(1, referalno);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	public boolean insert(Connection con, int copyFromReferalDoctor, int copyToReferalDoctor) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO referral_center_master(referal_center_id, referal_no, center_id, status) " +
					"	(SELECT nextval('referral_center_master_seq'), ?, center_id, status FROM referral_center_master WHERE referal_no=?)");
			ps.setInt(1, copyToReferalDoctor);
			ps.setInt(2, copyFromReferalDoctor);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

}
