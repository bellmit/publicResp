package com.insta.hms.master.RegistrationCards;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrationCardsDAO extends GenericDAO {

	public RegistrationCardsDAO() {
		super("registration_cards");
	}

	private static final String GET_RATE_PLAN_LISTS = "SELECT org_id,org_name FROM organization_details "
			+ "WHERE status='A' ORDER BY org_name";

	public List ratePlansList() throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_RATE_PLAN_LISTS);
		List ratePlanList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
	    return ratePlanList;
	}

	public boolean insertCustomerRegCardDetails(Connection con,Map fields) throws SQLException {
		return DataBaseUtil.dynaInsert(con, "registration_cards", fields);
	}

	private static final String UPDATE_FILE =
		"UPDATE registration_cards SET custom_reg_card_template=? WHERE card_id=?";

	public boolean updateRegistrationCustomerCardTemplate(Connection con,int cardId, InputStream file, int size) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_FILE);
			ps.setBinaryStream(1, file, size);
			ps.setInt(2, cardId);
			int result = ps.executeUpdate();
			return (result > 0 );
		} finally {
			ps.close();
		}
	}

	private static final String UPDATE_ODT_FILE =
		"UPDATE registration_cards SET odt_file=?, user_name=?, mod_time=? where card_id=?";
	public boolean updateOdtFile(Connection con, int cardId, InputStream file, int size, String userName)
		throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_ODT_FILE);
			ps.setBinaryStream(1, file, size);
			ps.setString(2, userName);
			ps.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
			ps.setInt(4, cardId);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static final String GET_REG_CARD_FIELDS = " SELECT * ";

	private static final String GET_REG_CARD_COUNT = "SELECT count(card_id) ";

	private static final String GET_REG_CARD_TABLE = " FROM (SELECT rc.card_id,rc.card_name,"
			+ " rc.visit_type,rc.rate_plan,rc.status,COALESCE(od.org_name,'ALL') AS rate_plan_name FROM registration_cards rc "
			+ " LEFT JOIN organization_details od ON (od.org_id = rc.rate_plan)) as foo ";

	public PagedList getCustomerRegCardList(Map filters, Map pagingParams) throws SQLException, ParseException {

		Connection con = null;
		PagedList l = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = null;
			qb = new SearchQueryBuilder(con, GET_REG_CARD_FIELDS,
					GET_REG_CARD_COUNT, GET_REG_CARD_TABLE, pagingParams);

			// add the value for the initial where clause

			qb.addFilterFromParamMap(filters);
			qb.addSecondarySort("card_id");
			qb.build();

			l = qb.getMappedPagedList();
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}
		return l;
	}

	private static final String GET_CUSTOM_REG_CARD_TEMPLATE = "SELECT custom_reg_card_template " +
															   "FROM registration_cards WHERE card_id=?";

	public static InputStream getCustomRegCardTemplate(int cardId)
			throws SQLException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			stmt = con.prepareStatement(GET_CUSTOM_REG_CARD_TEMPLATE);
			stmt.setInt(1, cardId);
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getBinaryStream(1);
			else
				return null;
		} finally {
			DataBaseUtil.closeConnections(con, stmt, rs);
		}
	}

	public boolean updateCustomerRegCardDetails(Connection con, int cardId,
			Map fields) throws SQLException {
		HashMap key = new HashMap();
		key.put("card_id", cardId);
		int rows = DataBaseUtil.dynaUpdate(con, "registration_cards", fields, key);
		return (rows == 1);
	}


	private static final String UPDATE_CUSTOM_REG_CARD = "UPDATE registration_cards SET custom_reg_card_template=? WHERE card_id=?";

	public boolean updateCoustmerRegCardTemplate(Connection con, int cardId, InputStream file,
			int size) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(UPDATE_CUSTOM_REG_CARD);
			ps.setBinaryStream(1, file, size);
			ps.setInt(2, cardId);
			int result = ps.executeUpdate();
			return (result > 0);
		} finally {
			ps.close();
		}
	}

	private static String GET_ALL_CARDS = " select card_id,visit_type,rate_plan from registration_cards " ;

	public static List getAllCards() {
		PreparedStatement ps = null;
		ArrayList cards = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_CARDS);
			cards = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return cards;
	}
}
