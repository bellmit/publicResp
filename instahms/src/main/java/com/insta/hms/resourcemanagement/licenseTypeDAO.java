package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class licenseTypeDAO extends GenericDAO{

	public licenseTypeDAO() {
		super("license_type_master");
	}

	public boolean insertLicenseTypeDetails(Connection con,Map fields) throws SQLException {
		return DataBaseUtil.dynaInsert(con, "license_type_master", fields);
	}

	private static final String GET_LICENSE_DETAILS = " SELECT license_type,license_type_id,status FROM license_type_master where license_type_id=?";

	public static BasicDynaBean getLicenseDetails(int licenseId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_LICENSE_DETAILS, licenseId);
		return (BasicDynaBean) l.get(0);
	}

	public boolean updateFields(Connection con,int licenseTypeId, Map fields) throws SQLException {
		HashMap key = new HashMap();
		key.put("license_type_id", licenseTypeId);
		int rows = DataBaseUtil.dynaUpdate(con, "license_type_master", fields, key);
		return (rows == 1);
	}

	private static String GET_ALL_LICENSE = " select license_type_id,license_type from license_type_master " ;

	public static List getAlllicense() {
		PreparedStatement ps = null;
		ArrayList licenseType = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_LICENSE);
			licenseType = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return licenseType;
	}
}
