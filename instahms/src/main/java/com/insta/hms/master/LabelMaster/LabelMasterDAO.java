package com.insta.hms.master.LabelMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.BasicCachingDAO;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LabelMasterDAO extends BasicCachingDAO {

	public LabelMasterDAO() {
		super("label_master");
	}

	private static final String LABEL_FILEDS = "SELECT * ";
	private static final String LABEL_FROM = " FROM label_master ";
	private static final String COUNT = " SELECT count(label_id) ";

	public PagedList getLabelMasterDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
	Connection con = null;
	SearchQueryBuilder qb = null;
	try {
		con = DataBaseUtil.getConnection();
		qb = new SearchQueryBuilder(con, LABEL_FILEDS, COUNT, LABEL_FROM, listingParams);
		qb.addFilterFromParamMap(params);
		qb.build();

		return qb.getDynaPagedList();
	} finally {
		DataBaseUtil.closeConnections(con, null);
		if (qb != null) qb.close();
	}
}

	public static final String GET_ALL_LABEL_MASTER_VALUES = " SELECT * FROM label_master ";

	public static List getAllLabels() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList labelsList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_LABEL_MASTER_VALUES);
			labelsList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return labelsList;
	}

}
