package com.insta.hms.master.GovtIdentifierMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class GovtIdentifierMasterDAO extends GenericDAO {

	static Connection con = null;

	public GovtIdentifierMasterDAO() {
		super("govt_identifier_master");
	}

	public static String GOVT_IDENTIFIER_FIELDS = "SELECT * ";

	public static String GOVT_IDENTIFIER_COUNT = "SELECT COUNT(govt_identifier_master)";

	public static String GOVT_IDENTIFIER_TABLES ="FROM govt_identifier_master";


	public PagedList getGovtIdentifierList(Map map, Map pagingParams) throws SQLException, ParseException {
		Connection con = null;
		SearchQueryBuilder qb = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, GOVT_IDENTIFIER_FIELDS, GOVT_IDENTIFIER_COUNT, GOVT_IDENTIFIER_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("identifier_id");
			qb.build();
			return qb.getMappedPagedList();
		}finally{
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}

	}

	private static final String GOVT_IDENTIFIER_DETAILS = "Select identifier_id, identifier_type from govt_identifier_master";

	public static List getIdentifierNamesAndIds() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GOVT_IDENTIFIER_DETAILS));
	}
}