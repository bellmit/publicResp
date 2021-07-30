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

public class GenericResourceMasterDAO extends GenericDAO{
	public GenericResourceMasterDAO() {
		super("generic_resource_master");
	}

	public static BasicDynaBean getRecord(int resourceId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(
					" SELECT grm.*,grt.*, hcm.center_name FROM generic_resource_master grm " +
					" JOIN generic_resource_type grt ON(grt.generic_resource_type_id=grm.generic_resource_type_id)" +
					" JOIN hospital_center_master hcm ON (hcm.center_id=grm.center_id) where generic_resource_id=?");
			ps.setInt(1, resourceId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private  String GENERIC_RESOURCES_FIELDS = "SELECT grm.generic_resource_id,grm.generic_resource_name,grm.status," +
			" grm.generic_resource_type_id,grm.status,grm.schedule,grm.overbook_limit," +
			" grt.resource_type_desc,grm.center_id,grt.scheduler_resource_type, hcm.center_name ";

	private static final String GENERIC_RESOURCES_COUNT = " SELECT COUNT(*)";

	private  String GENERIC_RESOURCES_TABLES = " FROM generic_resource_master grm " +
			" JOIN generic_resource_type grt ON(grt.generic_resource_type_id=grm.generic_resource_type_id)" +
			" JOIN hospital_center_master hcm ON(grm.center_id = hcm.center_id)";

	public  PagedList getGenericResources(Map params,Map paginParam ,int centerId) throws Exception {

		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				GENERIC_RESOURCES_FIELDS, GENERIC_RESOURCES_COUNT, GENERIC_RESOURCES_TABLES, paginParam);
		qb.addFilterFromParamMap(params);
		if (centerId != 0 ) {
			qb.addFilter(SearchQueryBuilder.INTEGER, "hcm.center_id", "=", new Integer(centerId));
		}
		qb.build();

		PagedList l = qb.getMappedPagedList();
		con.close();

		return l;
	}

	public static final String GET_ALL_GENERIC_RESOURCES = " SELECT * FROM generic_resource_master ";

	public static List getAllResourceNames() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList resourcesList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_GENERIC_RESOURCES);
			resourcesList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return resourcesList;
	}
}
