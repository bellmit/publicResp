package com.insta.hms.master.DischargeTypeMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
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

public class DischargeTypeMasterDAO extends GenericDAO{
	public DischargeTypeMasterDAO() {
		super("discharge_type_master");
	}

	private static String DISCHARGE_TYPES_FIELDS = " SELECT *  ";

	private static String DISCHARGE_TYPES_COUNT = " SELECT count(*) ";

	private static String DISCHARGE_TYPES_TABLES = " FROM discharge_type_master";

	public PagedList getDischargeTypes(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, DISCHARGE_TYPES_FIELDS,
					DISCHARGE_TYPES_COUNT, DISCHARGE_TYPES_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("discharge_type", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_DISCHARGE_TYPES = " SELECT discharge_type,discharge_type_id FROM discharge_type_master ";

	public static List getAllDischargeTypes() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList dischargeTypeList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_DISCHARGE_TYPES);
			dischargeTypeList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return dischargeTypeList;
	}
}
