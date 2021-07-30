package com.insta.hms.master.PackageUOM;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class PackageUOMDAO extends GenericDAO {
	public PackageUOMDAO() {
		super("package_issue_uom");
	}

	private static final String PKG_UOM_QUERY_FIELDS = "SELECT * ";
	private static final String PKG_UOM_COUNT_QUERY = "SELECT COUNT(*) ";
	private static final String PKG_UOM_TABLE_QUERY = "FROM package_issue_uom";
	public static PagedList searchPackageUOMs(Map filter, Map listing) throws SQLException, Exception {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			qb = new SearchQueryBuilder(con,
					PKG_UOM_QUERY_FIELDS, PKG_UOM_COUNT_QUERY, PKG_UOM_TABLE_QUERY, listing);

			qb.addFilterFromParamMap(filter);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		}finally {
			DataBaseUtil.closeConnections(con, null);
			qb.close();
		}
	}


	private static final String GET_PACK_ISSUES_UOM = 
		"SELECT distinct issue_uom,package_uom,package_size,integration_uom_id FROM package_issue_uom";

	public static List getPackIssueUOMs() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List l = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PACK_ISSUES_UOM);
			l = DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	private static final String ALL_PACKAGE_UOMS = " SELECT * FROM package_uoms_view";
	public static List getAllPackageUOMs() throws SQLException {
		return DataBaseUtil.queryToDynaList(ALL_PACKAGE_UOMS);
	}

	private static final String ALL_ISSUE_UOMS = " SELECT * FROM issue_uoms_view";
	public static List getAllIssueUOMs() throws SQLException {
		return DataBaseUtil.queryToDynaList(ALL_ISSUE_UOMS);
	}
}
