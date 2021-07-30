package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DialyzerRatingsDAO extends GenericDAO {


	public DialyzerRatingsDAO() {

		super("dialyzer_ratings");
	}

	private static final String GET_ALL_DIALRATINGS = "SELECT dialyzer_rating_id, dialyzer_rating FROM dialyzer_ratings";

	public List getAvalDialRatings() {

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList dialRatings = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_DIALRATINGS);
			dialRatings = DataBaseUtil.queryToArrayList(ps);

		}catch(SQLException e) {
			Logger.log(e);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return dialRatings;
	}


}