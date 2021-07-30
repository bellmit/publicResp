package com.insta.hms.master.HospitalRolesMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HospitalRolesMasterDAO extends GenericDAO{

	public HospitalRolesMasterDAO() {
		super("hospital_roles_master");
	}

	private static final String HOSP_ROLES_FILEDS = "SELECT * ";
	private static final String HOSP_ROLES_FROM = " FROM hospital_roles_master ";
	private static final String COUNT = " SELECT count(hosp_role_id) ";

	public PagedList getHospitalRolesDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
	Connection con = DataBaseUtil.getConnection();
	SearchQueryBuilder qb = null;
	try {
		qb = new SearchQueryBuilder(con, HOSP_ROLES_FILEDS, COUNT, HOSP_ROLES_FROM, listingParams);
		qb.addFilterFromParamMap(params);
		qb.build();

		return qb.getDynaPagedList();
	} finally {
		DataBaseUtil.closeConnections(con, null);
		if (qb != null) qb.close();
		}
	}

	public static final String GET_ALL_HOSPITAL_ROLES = " SELECT hosp_role_id,hosp_role_name,* FROM hospital_roles_master ";

	public static List getAllHospitalRoles() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList suppliersList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_HOSPITAL_ROLES);
			suppliersList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return suppliersList;
	}
	
	private static final String GET_ALL_RULES_FOR_ROLE = " select * from "
      + " hospital_roles_order_controls ";
  
  public List<BasicDynaBean> getOrderControlRules(List userId) throws SQLException {
    String[] placeholdersArr = new String[userId.size()];
    Arrays.fill(placeholdersArr, "?");
    StringBuilder query = new StringBuilder();
    query.append(GET_ALL_RULES_FOR_ROLE);
    query.append("WHERE role_id in (").
             append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
    return DataBaseUtil.queryToDynaList(query.toString(), userId.toArray());
  }
}
