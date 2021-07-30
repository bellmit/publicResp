package com.insta.hms.master.PackageCategoryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
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
 * @author nikunj.s
 *
 */
public class PackageCategoryMasterDAO extends GenericDAO {

	public PackageCategoryMasterDAO() {
		super("package_category_master");
	}

	private static final String PACKAGE_CATEGORY_FILEDS = "SELECT * ";
	private static final String PACKAGE_CATEGORY_FROM = " FROM package_category_master ";
	private static final String COUNT = " SELECT count(package_category_id) ";

	public PagedList getPackageCategotyMasterDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
	Connection con = DataBaseUtil.getConnection();
	SearchQueryBuilder qb = null;
	try {
		qb = new SearchQueryBuilder(con, PACKAGE_CATEGORY_FILEDS, COUNT, PACKAGE_CATEGORY_FROM, listingParams);
		qb.addFilterFromParamMap(params);
		qb.build();

		return qb.getDynaPagedList();
	} finally {
		DataBaseUtil.closeConnections(con, null);
		if (qb != null) qb.close();
		}
	}

	public static final String GET_ALL_PACKAGE_CATEGORY_MASTER = " SELECT package_category_id,package_category,* FROM package_category_master ";

	public static List getAllpackageCategoty() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList suppliersList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_PACKAGE_CATEGORY_MASTER);
			suppliersList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return suppliersList;
	}

}
