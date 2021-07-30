package com.insta.hms.master.Dialysis;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


public class DialLocationMasterDAO extends GenericDAO {


	public DialLocationMasterDAO() {

		super("location_master");
	}




	private static String DIAL_LOCATION_FIELDS = "SELECT location_id, location_name, lm.status, remarks, " +
			"	hcm.center_name, hcm.center_id ";

	private static String DIAL_LOCATION_COUNT = "SELECT count(*) ";

	private static String DIAL_LOCATION_TABLE = " FROM location_master lm JOIN hospital_center_master hcm ON (hcm.center_id=lm.center_id) ";


	public PagedList getDialLocationsList(Map<LISTING, Object> listingParams, Map filter)
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, DIAL_LOCATION_FIELDS, DIAL_LOCATION_COUNT, DIAL_LOCATION_TABLE, listingParams);

			qb.addFilterFromParamMap(filter);
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "lm.center_id", "=", centerId);
			qb.addSecondarySort("location_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	private static final String GET_ALL_LOCATIONS =
		" SELECT location_id, location_name, center_id FROM location_master where (center_id=? or ?=0)";
	public static List getAvalDialLocations() throws SQLException {
		return getAvalDialLocations(RequestContext.getCenterId());
	}

	public static List getAvalDialLocations(int centerId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		List dialLocations = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_LOCATIONS);
			ps.setInt(1, centerId);
			ps.setInt(2, centerId);
			dialLocations = ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(ps));

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return dialLocations;
	}




	public static BasicDynaBean getRecord(int locationId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(
					"SELECT location_name, location_id, lm.status, remarks, lm.center_id, center_name FROM location_master lm " +
					"	JOIN hospital_center_master hcm ON (hcm.center_id=lm.center_id) where location_id=?");
			ps.setInt(1, locationId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}