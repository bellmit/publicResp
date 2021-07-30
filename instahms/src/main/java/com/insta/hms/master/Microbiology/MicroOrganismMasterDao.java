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

public class MicroOrganismMasterDao{


	private static String ORGANISM_FIELDS = " SELECT *  ";

	private static String ORGANISM_COUNT = " SELECT count(*) ";

	private static String ORGANISM_TABLES = " FROM (SELECT organism_id, organism_name, mom.status, org_group_name FROM micro_organism_master mom "
			+ " JOIN micro_org_group_master USING (org_group_id) ) as foo ";

	public static PagedList getOrganismDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, ORGANISM_FIELDS,
					ORGANISM_COUNT, ORGANISM_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("organism_id", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_ORGANISM_NAMES = "SELECT organism_id, organism_name FROM micro_organism_master";

	public static List getOrganismNamesAndIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_ORGANISM_NAMES));
	 }

	private static final String ORGANISM_DETAILS = "SELECT organism_id, organism_name, mo.status, org_group_id FROM micro_organism_master mo "
			+ " JOIN micro_org_group_master USING (org_group_id)"
			+ " WHERE organism_id = ?";

	public static BasicDynaBean getBean(Integer organismID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(ORGANISM_DETAILS);
			pstmt.setInt(1, organismID);
			return DataBaseUtil.queryToDynaBean(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}


}