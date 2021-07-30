package com.insta.hms.master.dentalshades;

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


/**
 * @author Mithun
 *
 */

public class DentalShadesMasterDAO extends GenericDAO{
	public DentalShadesMasterDAO() {
		super("dental_shades_master");
	}

	private static String DENTAL_SHADES_FIELDS = " SELECT *  ";

	private static String DENTAL_SHADES_COUNT = " SELECT count(*) ";

	private static String DENTAL_SHADES_TABLES = " FROM dental_shades_master";

	public PagedList getDentalShadesDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, DENTAL_SHADES_FIELDS,
					DENTAL_SHADES_COUNT, DENTAL_SHADES_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("shade_name", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_DENTAL_SHADES = " SELECT shade_id,shade_name FROM dental_shades_master ";

	public static List getAllShades() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList shadesList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_DENTAL_SHADES);
			shadesList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return shadesList;
	}
}

