package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */


public class DialysisPrepeMasterDAO extends GenericDAO{
	public DialysisPrepeMasterDAO() {
		super("dialysis_prep_master");
	}
	static Logger logger = LoggerFactory.getLogger(DialysisPrepeMasterDAO.class);

	private static String DIALYSIS_PREP_FIELDS = " SELECT *  ";

	private static String DIALYSIS_PREP_COUNT = " SELECT count(*) ";

	private static String DIALYSIS_PREP_TABLES = " FROM (SELECT * FROM dialysis_prep_master ) as foo ";

	public PagedList getDialysisPrepDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, DIALYSIS_PREP_FIELDS,
					DIALYSIS_PREP_COUNT, DIALYSIS_PREP_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("prep_param");
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String GET_PREP_DETAILS = "SELECT * FROM dialysis_prep_master " +
			" WHERE prep_param_id = ? ";

	public BasicDynaBean getPrepBean(int prepParamId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PREP_DETAILS);
			ps.setInt(1, prepParamId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PREP_BEAN = "SELECT * FROM dialysis_prep_master " +
			" WHERE prep_param= ? AND prep_state = ?";

	public BasicDynaBean getPrepBean(String paramName, String prepState) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PREP_BEAN);
			ps.setString(1, paramName);
			ps.setString(2, prepState);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_DIALYSIS_PREP_NAME_AND_IDS = "Select prep_param_id,prep_param,prep_state FROM dialysis_prep_master";

	public List getPrepParamDetails() throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DIALYSIS_PREP_NAME_AND_IDS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}



}
