/**
 *
 */
package com.insta.hms.master.DiagnosisStatus;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author krishna
 *
 */
public class DiagnosisStatusDAO extends GenericDAO {

	public DiagnosisStatusDAO() {
		super("diagnosis_statuses");
	}

	public boolean statusExists(Connection con, Integer statusId, String statusName) throws SQLException{
		PreparedStatement ps = con.prepareStatement("SELECT diagnosis_status_name FROM diagnosis_statuses " +
				"	WHERE diagnosis_status_id!=? and diagnosis_status_name=?");
		ps.setInt(1, statusId);
		ps.setString(2, statusName);
		return DataBaseUtil.queryToDynaBean(ps) != null;
	}

}
