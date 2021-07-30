package com.insta.hms.master.Microbiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


public class MicroAbstAntibioticMasterDao{

	private static String ANTIBIOTIC_FIELDS = " SELECT *  ";

	private static String ANTIBIOTIC_COUNT = " SELECT count(*) ";

	private static String ANTIBIOTIC_TABLES = " FROM (SELECT antibiotic_id, antibiotic_name, status FROM micro_antibiotic_master mam ) as foo ";

	public static PagedList getAnticbioticDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, ANTIBIOTIC_FIELDS,
					ANTIBIOTIC_COUNT, ANTIBIOTIC_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("antibiotic_id", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_ANTIBIOTIC_NAMES = "SELECT antibiotic_id, antibiotic_name,status FROM micro_antibiotic_master";

	public static List getAntibioticNamesAndIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_ANTIBIOTIC_NAMES));
	 }


	private static final String ANTIBIOTIC_DETAILS = "SELECT antibiotic_id, antibiotic_name, status FROM micro_antibiotic_master mam "
			+ " WHERE antibiotic_id = ?";

	public static BasicDynaBean getBean(Integer antibioticID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(ANTIBIOTIC_DETAILS);
			pstmt.setInt(1, antibioticID);
			return DataBaseUtil.queryToDynaBean(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String GET_ANTIBIOTIC_DETAILS = "SELECT * FROM micro_abst_antibiotic_master"
		+ " WHERE antibiotic_name = ? AND abst_panel_id = ?";

	public static boolean findByKeys(String antibioticName,int abstPanelId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean flag = false;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_ANTIBIOTIC_DETAILS);
			pstmt.setString(1, antibioticName);
			pstmt.setInt(2, abstPanelId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				flag = true;
			}
			return flag;
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}

	private static final String GET_ANTIBIOTIC_DET = "SELECT * FROM micro_abst_antibiotic_master"
		+ " WHERE antibiotic_id = ? AND abst_panel_id = ?";

	public static boolean findByKeys(int antibioticName,int abstPanelId)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean flag = false;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_ANTIBIOTIC_DET);
			pstmt.setInt(1, antibioticName);
			pstmt.setInt(2, abstPanelId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				flag = true;
			}
			return flag;
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}

}