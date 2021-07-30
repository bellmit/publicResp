package com.insta.hms.master.Microbiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


public class MicroAbstPanelMasterDao{

	private static String ABST_PANEL_FIELDS = " SELECT *  ";

	private static String ABST_PANEL_COUNT = " SELECT count(*) ";

	private static String ABST_QUERY =
		"SELECT abst_panel_id, abst_panel_name, map.status" +
		" FROM micro_abst_panel_master map " ;

	private static String ABST_PANEL_TABLES = " FROM ("+ABST_QUERY+" ) as foo ";

	public static PagedList getOrganismDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, ABST_PANEL_FIELDS,
					ABST_PANEL_COUNT, ABST_PANEL_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("abst_panel_id", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_ABSTPANEL_NAMES = "SELECT abst_panel_id, abst_panel_name FROM micro_abst_panel_master";

	public static List getPanelmNamesAndIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_ABSTPANEL_NAMES));
	 }

	private static final String GET_ABSTPANEL_GRP_DETAILS = "SELECT *, mgm.org_group_name FROM micro_abst_orggr mabst " +
				" JOIN micro_org_group_master mgm USING(org_group_id) " +
				" WHERE abst_panel_id = ?";

	public static List<BasicDynaBean> getOrgGrpList(Integer panelID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_ABSTPANEL_GRP_DETAILS);
			pstmt.setInt(1, panelID);
			return DataBaseUtil.queryToDynaList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}

	private static final String GET_ABSTPANEL_ANTIBIOTIC_DETAILS = "SELECT *, mam.antibiotic_name FROM micro_abst_antibiotic_master maam " +
	" JOIN micro_antibiotic_master mam USING(antibiotic_id) " +
	" WHERE abst_panel_id = ?";

	public static List<BasicDynaBean> getAntibioticList(Integer panelID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_ABSTPANEL_ANTIBIOTIC_DETAILS);
			pstmt.setInt(1, panelID);
			return DataBaseUtil.queryToDynaList(pstmt);

			} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}
	private static final String GET_ABSTPANELS = "SELECT abst_panel_id, abst_panel_name, " +
			"map.status, mgm.org_group_name,mgm.org_group_id" +
			" FROM micro_abst_panel_master map " +
			"	JOIN micro_abst_orggr mao USING(abst_panel_id) " +
			"	JOIN micro_org_group_master mgm ON(mgm.org_group_id = mao.org_group_id)";
	public static List<BasicDynaBean> getAllABSTpanels()
	throws SQLException{
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_ABSTPANELS);
			return DataBaseUtil.queryToDynaList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String GET_ABSTPANEL_ANTIBIOTCS = "SELECT abst_panel_id,mam.antibiotic_id,antibiotic_name " +
					"FROM micro_antibiotic_master mam "+
					"LEFT JOIN micro_abst_antibiotic_master maam ON(maam.antibiotic_id=mam.antibiotic_id)"+
					"WHERE mam.status = 'A'";
	public static List<BasicDynaBean> getAllABSTAndAntibiotics()
	throws SQLException{
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_ABSTPANEL_ANTIBIOTCS);
			return DataBaseUtil.queryToDynaList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

}