package com.insta.hms.master.PhraseSuggestionsMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nikunj.s
 *
 */
public class PhraseSuggestionsMasterDAO  extends GenericDAO {

	public PhraseSuggestionsMasterDAO(){
		super("phrase_suggestions_master");
	}

	private static final String PHRASE_SUGGESTIONS_FILEDS = "SELECT * ";
	private static final String PHRASE_SUGGESTIONS_FROM =
		" FROM (SELECT psm.phrase_suggestions_id, psm.phrase_suggestions_desc, psm.status as phrasestatus, psm.dept_id,dept.dept_name, " +
		" 			psm.phrase_suggestions_category_id, pscm.phrase_suggestions_category " +
		"		FROM phrase_suggestions_master psm LEFT OUTER JOIN phrase_suggestions_category_master pscm USING (phrase_suggestions_category_id) " +
		"		LEFT JOIN department dept USING(dept_id) "+
		"   ) AS foo " ;
	private static final String COUNT = " SELECT count(phrase_suggestions_id) ";

	public PagedList getPhraseSuggestionsMasterDetails(Map params, Map<LISTING, Object> listingParams)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, PHRASE_SUGGESTIONS_FILEDS, COUNT, PHRASE_SUGGESTIONS_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	public static final String GET_ALL_PHRASE_SUGGESTIONS_MASTER =
		" SELECT * FROM phrase_suggestions_master ";

	public static List getAllPhraseSuggestions() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList suppliersList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_PHRASE_SUGGESTIONS_MASTER);
			suppliersList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return suppliersList;
	}

	public static List getPhraseSuggestionsDynaList() throws SQLException {
		return DataBaseUtil.queryToDynaList("SELECT psm.*, cat.phrase_suggestions_category FROM phrase_suggestions_master psm " +
				"	JOIN phrase_suggestions_category_master cat ON (cat.phrase_suggestions_category_id=psm.phrase_suggestions_category_id) " +
				" WHERE psm.status = 'A' " +
				" ORDER by cat.phrase_suggestions_category, psm.phrase_suggestions_desc");
	}

	public static List getUniquePhraseSuggestionsDynaList() throws SQLException {
		return DataBaseUtil.queryToDynaList("SELECT distinct psm.phrase_suggestions_desc,psm.phrase_suggestions_category_id FROM phrase_suggestions_master psm " +
				"	JOIN phrase_suggestions_category_master cat ON (cat.phrase_suggestions_category_id=psm.phrase_suggestions_category_id) " +
				"	WHERE psm.status = 'A' "+
				" ORDER by psm.phrase_suggestions_desc");
	}

	public static List getPhraseSuggestionsByDeptDynaList(String dept_id) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT psm.*, cat.phrase_suggestions_category FROM phrase_suggestions_master psm " +
					"	JOIN phrase_suggestions_category_master cat ON (cat.phrase_suggestions_category_id=psm.phrase_suggestions_category_id) " +
					"	WHERE psm.status = 'A' and psm.dept_id=? or psm.dept_id is null or psm.dept_id=''"+
					" ORDER by cat.phrase_suggestions_category, psm.phrase_suggestions_desc");
			ps.setString(1, dept_id);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		
	}

	public boolean exists(String phrase, int categoryId, String dept,String status) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT phrase_suggestions_desc FROM phrase_suggestions_master psm " +
					" where psm.phrase_suggestions_category_id=? and trim(upper(phrase_suggestions_desc))=trim(upper(?)) " +
					" AND psm.dept_id=? AND psm.status=? " );
			ps.setInt(1, categoryId);
			ps.setString(2, phrase);
			ps.setString(3, dept);
			ps.setString(4, status);
			List l = DataBaseUtil.queryToDynaList(ps);
			if (!l.isEmpty()) return true;
			else return false;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean exists(String phrase, int phraseId, int categoryId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT phrase_suggestions_desc FROM phrase_suggestions_master psm " +
					" where psm.phrase_suggestions_category_id=? and trim(upper(phrase_suggestions_desc))=trim(upper(?)) " +
					"	and phrase_suggestions_id != ?");
			ps.setInt(1, categoryId);
			ps.setString(2, phrase);
			ps.setInt(3, phraseId);
			List l = DataBaseUtil.queryToDynaList(ps);
			if (!l.isEmpty()) return true;
			else return false;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}



}
