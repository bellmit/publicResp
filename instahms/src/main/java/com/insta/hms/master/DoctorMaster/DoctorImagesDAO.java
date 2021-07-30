/**
 *
 */
package com.insta.hms.master.DoctorMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author krishna
 *
 */
public class DoctorImagesDAO extends GenericDAO {

	public DoctorImagesDAO() {
		super("doctor_images");
	}

	public boolean imageExist(String doctorId) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		try {
			return imageExist(con, doctorId);
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	public boolean imageExist(Connection con, String doctorId) throws SQLException, IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT doctor_id FROM doctor_images WHERE doctor_id=?");
			ps.setString(1, doctorId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return false;
	}

	public boolean insertOrupdatePhoto(Connection con, String doctorId, BasicDynaBean bean)
		throws SQLException, IOException {

		if (imageExist(con, doctorId)) {
			return update(con, bean.getMap(), "doctor_id", doctorId) > 0;
		} else {
			return insert(con, bean);
		}

	}
	
	private static final String DOCTORS_PHOTO = " SELECT photo  FROM doctor_images WHERE doctor_id=?";

	public static InputStream getDoctorsPhoto(String doctor_id) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(DOCTORS_PHOTO);
			ps.setString(1, doctor_id);
			rs = ps.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

}
