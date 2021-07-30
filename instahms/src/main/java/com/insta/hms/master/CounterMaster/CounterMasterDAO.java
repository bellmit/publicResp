package com.insta.hms.master.CounterMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CounterMasterDAO extends GenericDAO{
	Connection con = null;

	public CounterMasterDAO() {
		super("counters");
	}

	public static final String SELECT_COUNTERS = "SELECT c.*, hcm.center_name ";
	public static final String SELECT_COUNT = " SELECT count(*) ";
	public static final String SELECT_TABLES = " FROM counters c JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) ";
	public PagedList searchCounters(Map params, Map<LISTING, Object> listingParams) throws SQLException,
		ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, SELECT_COUNTERS, SELECT_COUNT, SELECT_TABLES, listingParams);
			qb.addFilterFromParamMap(params);
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "c.center_id", "=", centerId);
			qb.addSecondarySort("counter_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}
	public String getNextCounterId() throws SQLException {
		String counterId = null;
		counterId =	AutoIncrementId.getNewIncrUniqueId("COUNTER_ID", "COUNTERS", "COUNTER");
		return counterId;
	}

	private static final String GET_COUNTER = "SELECT c.*, hcm.center_name, hcm.center_id FROM counters c " +
			" JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) where c.counter_id=?";
	public BasicDynaBean getRecord(String counterId) throws SQLException {
		return DataBaseUtil.queryToDynaBean(GET_COUNTER, new String[]{counterId});
	}


	/*
	 * New model code: connection is passed in.
	 */
	private static final String GET_ALL_COUNTER =
		"SELECT counter_no, counter_id FROM counters where center_id=? or  0=?";

	public List getAllCounters(Connection con) throws SQLException {
		int centerId = RequestContext.getCenterId();
		return DataBaseUtil.queryToDynaList(GET_ALL_COUNTER, new Object[]{centerId, centerId});
	}

	private static final String GET_ALL_HOSP_USERS =
		" SELECT emp_username " +
		" FROM u_user " +
		"  JOIN u_role USING(role_id) " +
		" WHERE portal_id='N' ORDER BY emp_username ";

	public ArrayList getAllHospitalUsers(Connection con) throws SQLException{
		PreparedStatement ps = con.prepareStatement(GET_ALL_HOSP_USERS);
		ArrayList list = DataBaseUtil.queryToArrayList(ps);
		return list;
	}

	public final static String GET_COUNTERS_LIST_FOR_BILL  =
			" SELECT counter_no, counter_id, c.center_id, hcm.center_name FROM counters c " +
			"	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) WHERE counter_type='B'";

	public final static String GET_COUNTERS_LIST_FOR_PHARAMACY  =
			" SELECT counter_no, counter_id, c.center_id, hcm.center_name FROM counters c " +
			" JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) WHERE counter_type='P'";

	public List getCountersList() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_COUNTERS_LIST_FOR_BILL);
	}

	public List getCountersPharmacyList() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_COUNTERS_LIST_FOR_PHARAMACY);
	}


	private static final String PHARMACY_ACTIVE_COUNTERS =
		" SELECT counter_id, counter_no, counter_type, c.center_id, hcm.center_name from counters c " +
		"	JOIN hospital_center_master hcm ON (c.center_id=hcm.center_id) where counter_type='P' "+
		"	and c.status='A'" ;
	public List getPharmacyActiveCounters() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PHARMACY_ACTIVE_COUNTERS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String COUNTERS_NAMESAND_iDS="select counter_id,counter_no from counters";

	   public static List getCountersNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(COUNTERS_NAMESAND_iDS));
	}
}
