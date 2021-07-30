package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StockUploadDAO {


	static Logger log = LoggerFactory.getLogger(StockUploadDAO.class);

	public static boolean resetSequence( String sequenceName,int startNumber) throws SQLException {

		boolean status = true;
		Statement s = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			s = con.createStatement();
			s.executeUpdate("ALTER SEQUENCE " + sequenceName + " RESTART "+startNumber);
		} finally {
			if (s != null) {
				s.close();
				con.close();
			}
		}
		return status;
	}
}