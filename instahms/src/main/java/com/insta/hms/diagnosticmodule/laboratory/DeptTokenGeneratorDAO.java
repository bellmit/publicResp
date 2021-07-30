/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author krishna
 *
 */
public class DeptTokenGeneratorDAO extends GenericDAO {

	public DeptTokenGeneratorDAO() {
		super("test_dept_tokens");
	}

	/*
	 * to get the new token for each call we should use the separate connection.
	 * do not pass on the connection object to the method.
	 */
	public static Integer getToken(String deptId, int centerId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("UPDATE test_dept_tokens SET token_number=coalesce(token_number, 0)+1 " +
					" WHERE dept_id=? and center_id=? RETURNING token_number");
			ps.setString(1, deptId);
			ps.setInt(2, centerId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("token_number");
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return null;
	}


}
