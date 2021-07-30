/**
 *
 */
package com.insta.hms.master.PatientReport;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author krishna.t
 *
 */
public class FormHeaderDAO extends GenericDAO {

	private static final String table = "form_header";
	public FormHeaderDAO() {
		super("form_header");
	}

	public boolean exist(String keycolumn, Object identifier, String keycolumn1, Object identifier1) throws SQLException{
		StringBuilder query = new StringBuilder();
		query.append("SELECT "+keycolumn+" FROM ").append(table).append(" WHERE ")
				.append("upper("+keycolumn+")").append("=upper(?) and ")
				.append(keycolumn1).append("!=?;");


		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(query.toString());
			ps.setObject(1, identifier);
			ps.setObject(2, identifier1);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

}
