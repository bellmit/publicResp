package com.insta.hms.master.RegularExpression;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.BasicCachingDAO;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Anil N
 *
 */
public class RegularExpressionDAO extends BasicCachingDAO {

	public RegularExpressionDAO() {
		super("regexp_pattern_master");
	}

	public static final String GET_ALL_REG_EXPS = " SELECT * FROM regexp_pattern_master ";

	public static List getAllRegExp() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList regExpList = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_REG_EXPS);
			regExpList = DataBaseUtil.queryToArrayList(ps);
			}
		catch (SQLException e) {
				Logger.log(e);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		return regExpList;
	}

	public static final String GET_ALL_ACTIVE_REG_EXPS = " SELECT * FROM regexp_pattern_master where status= 'A' ";

	public static List<BasicDynaBean> getAllActiveRegExp() {
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> regExpList = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_ACTIVE_REG_EXPS);
			regExpList = DataBaseUtil.queryToDynaList(ps);
			}
		catch (SQLException e) {
			Logger.log(e);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		return regExpList;
	}

	private static final String REG_EXP_FILEDS = "SELECT * ";
	private static final String REG_EXP_FROM = " FROM regexp_pattern_master ";
	private static final String COUNT = " SELECT count(pattern_id) ";

	public PagedList getRegularExpressionDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
	Connection con = null;
	SearchQueryBuilder qb = null;
	try {
		con = DataBaseUtil.getConnection();
		qb = new SearchQueryBuilder(con, REG_EXP_FILEDS, COUNT, REG_EXP_FROM, listingParams);
		qb.addFilterFromParamMap(params);
		qb.build();

		return qb.getDynaPagedList();
	} finally {
		DataBaseUtil.closeConnections(con, null);
		if (qb != null) qb.close();
	}
   }

	private static final String GET_ALL_REG_PATTERN_EXPS = "SELECT pattern_id, regexp_pattern, pattern_desc FROM regexp_pattern_master WHERE status = 'A'";
	public static HashMap<Integer,String> getRegPatternWithExpression(String patternOrDesc) {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		HashMap<Integer, String> regExpMap = new HashMap<Integer, String>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_REG_PATTERN_EXPS);
			rs = ps.executeQuery();
			while(rs.next()) {
				Integer patternId = (Integer) rs.getInt("pattern_id");
				String patternDescOrExp = null ;
				if(patternOrDesc.equals("E")){
					patternDescOrExp = (String)rs.getString("regexp_pattern");
				}
				else if (patternOrDesc.equals("D")) {
					patternDescOrExp = (String)rs.getString("pattern_desc");
				}
				regExpMap.put(patternId, patternDescOrExp);
				}
			}
		catch (SQLException e) {
			Logger.log(e);
			} finally {
				DataBaseUtil.closeConnections(con, ps, rs);
			}
		return regExpMap;
	}

	private static final String GET_ALL_REG_PATTERN_NAMES = "SELECT pattern_id,pattern_name FROM regexp_pattern_master WHERE status = 'A'";
	public static HashMap<Integer,String> getRegPatternNames() {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		HashMap<Integer, String> regExpMap = new HashMap<Integer, String>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_REG_PATTERN_NAMES);
			rs = ps.executeQuery();
			while(rs.next()) {
				Integer patternId = (Integer) rs.getInt("pattern_id");
				String patternName = (String)rs.getString("pattern_name");
				regExpMap.put(patternId, patternName);
				}
			}
		catch (SQLException e) {
			Logger.log(e);
			} finally {
				DataBaseUtil.closeConnections(con, ps, rs);
			}
		return regExpMap;
	}
}