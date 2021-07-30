package com.insta.hms.master.RegionMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.regions.RegionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author nikunj.s
 *
 */
@Deprecated
@MigratedTo(RegionRepository.class)
public class RegionMasterDAO extends GenericDAO {

	public RegionMasterDAO() {
		super("region_master");
	}

	private static final String REGION_FILEDS = "SELECT * ";
	private static final String REGION_FROM = " FROM region_master ";
	private static final String COUNT = " SELECT count(region_id) ";

	public PagedList getRegionMasterDetails(Map params, Map<LISTING, Object> listingParams)
			throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, REGION_FILEDS, COUNT, REGION_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null)
				qb.close();
		}
	}

	public static final String GET_ALL_REGION_MASTER = " SELECT region_id,region_name,* FROM region_master ";

	public static List getAllRegions() {
		Connection con = null;
		PreparedStatement ps = null;
		List suppliersList = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_REGION_MASTER);
			suppliersList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return suppliersList;
	}


}
