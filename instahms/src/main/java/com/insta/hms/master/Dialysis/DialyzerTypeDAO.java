package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DialyzerTypeDAO extends GenericDAO {


	public DialyzerTypeDAO() {

		super("dialyzer_types");
	}

	private static final String GET_ALL_TYPES = "SELECT dialyzer_type_id, dialyzer_type_name FROM dialyzer_types";

	public List getAvalDialyzerTypes() {

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList dialTypes = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_TYPES);
			dialTypes = DataBaseUtil.queryToArrayList(ps);

		}catch(SQLException e) {
			Logger.log(e);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return dialTypes;
	}


}