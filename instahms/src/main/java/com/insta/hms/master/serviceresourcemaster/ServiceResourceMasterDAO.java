package com.insta.hms.master.serviceresourcemaster;

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

public class ServiceResourceMasterDAO extends GenericDAO{
	public ServiceResourceMasterDAO() {
		super("service_resource_master");

	}

	public static BasicDynaBean getRecord(int resourceId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(
					"SELECT srm.*, hcm.center_name FROM service_resource_master srm " +
					"	JOIN hospital_center_master hcm ON (hcm.center_id=srm.center_id) where serv_res_id=?");
			ps.setInt(1, resourceId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private  String SERVICE_RESOURCES_FIELDS = "SELECT serv_res_id,serv_resource_name,srm.status,schedule,overbook_limit, " +
			" srm.center_id, hcm.center_name ";

	private static final String SERVICE_RESOURCES_COUNT = " SELECT COUNT(*)";

	private  String SERVICE_RESOURCES = "FROM service_resource_master srm " +
			"	JOIN hospital_center_master hcm ON(srm.center_id = hcm.center_id)";

	public  PagedList getResourcesList(Map params,Map paginParam ,int centerId) throws Exception {

		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = new SearchQueryBuilder(con,
				SERVICE_RESOURCES_FIELDS, SERVICE_RESOURCES_COUNT, SERVICE_RESOURCES, paginParam);
		qb.addFilterFromParamMap(params);
		if (centerId != 0 ) {
			qb.addFilter(SearchQueryBuilder.INTEGER, "hcm.center_id", "=", new Integer(centerId));
		}
		
		String[] overbk_lmt = (String[])params.get("_overbook_limit");
		if(overbk_lmt != null && !overbk_lmt.equals("") && overbk_lmt.length > 0) {			
			if(!overbk_lmt[0].equals("")) {
				boolean overbook_limit = new Boolean(overbk_lmt[0]);
				if(overbook_limit) {
					qb.addFilter(SearchQueryBuilder.BOOLEAN, "COALESCE(srm.overbook_limit, 1) > 0 ", "=",true );				
				} else {
					qb.addFilter(SearchQueryBuilder.INTEGER, "srm.overbook_limit", "=", new Integer(0));
				}
			}
		}
		
		qb.build();

		PagedList l = qb.getMappedPagedList();
		con.close();

		return l;
	}

	public static final String GET_ALL_SERVICE_RESOURCES = " SELECT serv_res_id,serv_resource_name FROM service_resource_master ";

	public static List getAllResourceNames() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList resourcesList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_SERVICE_RESOURCES);
			resourcesList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return resourcesList;
	}

}