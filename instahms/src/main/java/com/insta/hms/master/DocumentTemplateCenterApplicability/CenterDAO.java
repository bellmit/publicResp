package com.insta.hms.master.DocumentTemplateCenterApplicability;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CenterDAO extends GenericDAO  {

	public CenterDAO() {
		super("doc_template_center_master");
	}



	public static final String GET_APPLICABLE_CENTERS =
			" SELECT dtcm.center_id, hcm.center_name, hcm.city_id, hcm.state_id, c.city_name, " +
			"	s.state_name, dtcm.doc_template_center_id, dtcm.status " +
			" FROM doc_template_center_master  dtcm " +
			"	LEFT JOIN hospital_center_master hcm ON (hcm.center_id=dtcm.center_id) " +
			"	LEFT JOIN city c ON (c.city_id=hcm.city_id) " +
			"	LEFT JOIN state_master s ON (s.state_id=c.state_id) " +
			" WHERE dtcm.template_id=? AND dtcm.doc_template_type=?" +
			" ORDER BY s.state_name, c.city_name, hcm.center_name" ;

		public List getApplicableCenters(int templId, String format) throws SQLException{
			Connection con = DataBaseUtil.getConnection();
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(GET_APPLICABLE_CENTERS);
				ps.setInt(1, templId);
				String type = "";
				if(format.contains("pdf")) {
					type = "P";
				}
				if(format.contains("rtf")) {
					type = "T";
				}
				if(format.contains("rich")) {
					type = "R";
				}
				if(format.contains("hvf")) {
					type = "H";
				}
				ps.setString(2, type);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}

	public boolean delete(Connection con, int docTemplID) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM doc_template_center_master where template_id=? and center_id != 0");
			ps.setInt(1, docTemplID);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM doc_template_center_master where template_id=? and center_id != 0");
			ps.setInt(1, docTemplID);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean delete(Connection con, Integer centerId, int docTemplID) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM doc_template_center_master where template_id=? and center_id = ?");
			ps.setInt(1, docTemplID);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // no records to delete.

			ps = con.prepareStatement("DELETE FROM doc_template_center_master where template_id=? and center_id = ?");
			ps.setInt(1, docTemplID);
			ps.setInt(2, centerId);
			int rowsDeleted = ps.executeUpdate();
			return (rowsDeleted != 0);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	public boolean insert(Connection con, int copyFromDocTempl, int copyToDocTempl) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO doc_template_center_master(doc_template_center_id, template_id, center_id, status) " +
					"	(SELECT nextval('doc_template_center_master_seq'), ?, center_id, status FROM doc_template_center_master WHERE template_id=?)");
			ps.setInt(1, copyToDocTempl);
			ps.setInt(2, copyFromDocTempl);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}




}

