/**
 *
 */
package com.insta.hms.master.MedicineRoute;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class MedicineRouteDAO extends GenericDAO {

	public MedicineRouteDAO() {
		super("medicine_route");
	}

	public static final String MEDICINE_ROUTE_FIELDS = "SELECT route_id, route_name, status ";
	public static final String FROM_TABLES = " FROM medicine_route ";
	public static final String COUNT = "SELECT count(route_id) ";
	public PagedList getRoutesList(Map map) throws SQLException, ParseException {
		SearchQueryBuilder qb = null;
		Connection con = DataBaseUtil.getConnection();
		try {
			Map listing = ConversionUtils.getListingParameter(map);
			qb = new SearchQueryBuilder(con, MEDICINE_ROUTE_FIELDS, COUNT, FROM_TABLES, listing);
			qb.addFilterFromParamMap(map);
			qb.build();
			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	private static final String ROUTE_NAME_EXISTS = "SELECT route_name FROM medicine_route " +
			" WHERE upper(route_name)=upper(?) and route_id!=?";
	public boolean exists(Connection con, String routeName, Integer routeId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(ROUTE_NAME_EXISTS);
			ps.setString(1, routeName);
			ps.setInt(2, routeId);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return false;
	}


}
