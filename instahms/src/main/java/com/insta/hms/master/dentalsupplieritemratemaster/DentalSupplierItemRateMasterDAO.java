package com.insta.hms.master.dentalsupplieritemratemaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DentalSupplierItemRateMasterDAO extends GenericDAO{

	public DentalSupplierItemRateMasterDAO() {
		super("dental_supplier_item_rate_master");
	}

	private static String DENTAL_SUPPLIER_ITEM_RATE_FIELDS = " SELECT *  ";

	private static String DENTAL_SUPPLIER_ITEM_RATE_COUNT = " SELECT count(*) ";

	private static String DENTAL_SUPPLIER_ITEM_RATE_TABLES = " FROM (SELECT *,dsrm.status as item_rate_status FROM dental_supplier_item_rate_master dsrm " +
			"		JOIN dental_supplies_master dsm ON(dsm.item_id = dsrm.item_id)" +
			"       JOIN dental_supplier_master dsma ON(dsma.supplier_id = dsrm.supplier_id) " +
			"    ) as foo";

	public PagedList getDentalItemRateDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, DENTAL_SUPPLIER_ITEM_RATE_FIELDS,
					DENTAL_SUPPLIER_ITEM_RATE_COUNT, DENTAL_SUPPLIER_ITEM_RATE_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("item_name", false);
			qb.addSecondarySort("supplier_name", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static List<BasicDynaBean> getItems() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT item.item_name, item.item_id, item.status as item_status, " +
					"	sup.supplier_name, sup.supplier_id, sup.status as supplier_status, " +
					"	rate.status as rate_status, unit_rate, vat_perc" +
					" FROM dental_supplier_item_rate_master rate" +
					" 	JOIN dental_supplies_master item ON (rate.item_id = item.item_id) " +
					" 	JOIN dental_supplier_master sup ON (rate.supplier_id = sup.supplier_id)");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static final String GET_ALL_DENTAL_SUPPLIERS = " SELECT supplier_id,supplier_name FROM dental_supplier_master ";

	public static List getAllSuppliers() {
		Connection con = null;
		PreparedStatement ps = null;
		ArrayList suppliersList = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_DENTAL_SUPPLIERS);
			suppliersList = DataBaseUtil.queryToArrayList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return suppliersList;
	}

	public static List getItemSupplierList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(" SELECT  supplier_id,supplier_name,item_id,dsmr.status FROM dental_supplier_item_rate_master dsmr " +
					" JOIN dental_supplier_master dsm USING (supplier_id)");

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
