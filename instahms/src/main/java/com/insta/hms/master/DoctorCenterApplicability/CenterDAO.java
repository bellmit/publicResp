package com.insta.hms.master.DoctorCenterApplicability;

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
public class CenterDAO extends GenericDAO  {

	public CenterDAO() {
		super("doctor_center_master");
	}



	public static final String GET_CENTERS =
		" SELECT dcm.center_id, dcm.status, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, " +
		"	s.state_name, dcm.doc_center_id" +
		" FROM doctor_center_master  dcm " +
		"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=dcm.center_id) " +
		"	LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
		"	LEFT JOIN state_master s ON (s.state_id=c.state_id) " +
		" WHERE dcm.doctor_id=?" +
		" ORDER BY s.state_name, c.city_name, hcm.center_name" ;

	public List getCenters(String docID) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CENTERS);
			ps.setString(1, docID);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean delete(Connection con, String docID) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM doctor_center_master where doctor_id=? and center_id != 0");
			ps.setString(1, docID);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM doctor_center_master where doctor_id=? and center_id != 0");
			ps.setString(1, docID);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer centerId, String docID) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM doctor_center_master where doctor_id=? and center_id = ?");
			ps.setString(1, docID);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM doctor_center_master where doctor_id=? and center_id = ?");
			ps.setString(1, docID);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	public boolean insert(Connection con, int copyFromDoctor, int copyToDoctor) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO doctor_center_master(doc_center_id, doctor_id, center_id, status) " +
					"	(SELECT nextval('doctor_center_master_seq'), ?, center_id, status FROM doctor_center_master WHERE doctor_id=?)");
			ps.setInt(1, copyToDoctor);
			ps.setInt(2, copyFromDoctor);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

}
