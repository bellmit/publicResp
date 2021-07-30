/**
 *
 */
package com.insta.hms.master.dentalsupplies;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.dentalsupplies.DentalSuppliesRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author nikunj
 *
 */
@Deprecated
@MigratedTo(value=DentalSuppliesRepository.class)
public class DentalSuppliesMasterDAO extends GenericDAO {

	public DentalSuppliesMasterDAO() {
		super("dental_supplies_master");
	}

	private static final String ITEMS_FILEDS = "SELECT * ";
	private static final String ITEMS_FROM = " FROM dental_supplies_master ";
	private static final String COUNT = " SELECT count(item_id) ";

	@MigratedTo(value=DentalSuppliesRepository.class, method="search")
	public PagedList getDentalSuppliesDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
	Connection con = DataBaseUtil.getConnection();
	SearchQueryBuilder qb = null;
	try {
		qb = new SearchQueryBuilder(con, ITEMS_FILEDS, COUNT, ITEMS_FROM, listingParams);
		qb.addFilterFromParamMap(params);
		qb.build();

		return qb.getDynaPagedList();
	} finally {
		DataBaseUtil.closeConnections(con, null);
		if (qb != null) qb.close();
		}
	}



	public static final String GET_ALL_DENTAL_SUPPLIES_ITEM = " SELECT item_id,item_name FROM dental_supplies_master ";
	
	@MigratedTo(value=DentalSuppliesRepository.class, method="lookup")
	public static List getAllSupplies() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList suppliersList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_DENTAL_SUPPLIES_ITEM);
			suppliersList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return suppliersList;
	}
}
