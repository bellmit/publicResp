package com.insta.hms.master.GenericSchedulerMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericResourceTypeDAO extends GenericDAO{
	public GenericResourceTypeDAO() {
		super("generic_resource_type");
	}

	private  String GENERIC_RESOURCE__TYPES_FIELDS = "SELECT grt.* ";

	private static final String GENERIC_RESOURCE_TYPES_COUNT = " SELECT COUNT(*)";

	private  String GENERIC_RESOURCE_TYPES_TABLES = " FROM generic_resource_type grt " ;

	public  PagedList getGenericResourceTypes(Map params,Map paginParam) throws Exception {

		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				GENERIC_RESOURCE__TYPES_FIELDS, GENERIC_RESOURCE_TYPES_COUNT, GENERIC_RESOURCE_TYPES_TABLES, paginParam);
		qb.addFilterFromParamMap(params);
		qb.build();

		PagedList l = qb.getMappedPagedList();
		con.close();

		return l;
	}

	public static BasicDynaBean getRecord(int resourceId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(
					" SELECT grt.* FROM generic_resource_type grt " +
					" WHERE generic_resource_type_id = ?");
			ps.setInt(1, resourceId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_ALL_GENERIC_RESOURCE_TYPES = " SELECT * FROM generic_resource_type ";

	public static List getAllResourceNames() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList resourcesList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_GENERIC_RESOURCE_TYPES);
			resourcesList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return resourcesList;
	}
}
