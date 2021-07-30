/**
 *
 */
package com.insta.hms.master.dentalsupplier;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.annotations.MigratedTo;
import com.insta.hms.mdm.dentalsuppliers.DentalSupplierRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mithun
 *
 */
@Deprecated
@MigratedTo(value=DentalSupplierRepository.class)
public class DentalSupplierMasterDAO extends GenericDAO{
	public DentalSupplierMasterDAO() {
		super("dental_supplier_master");
	}

	private static String DENTAL_SUPPLIER_FIELDS = " SELECT *  ";

	private static String DENTAL_SUPPLIER_COUNT = " SELECT count(*) ";

	private static String DENTAL_SUPPLIER_TABLES = " FROM dental_supplier_master";
	
	@MigratedTo(value=DentalSupplierRepository.class, method="search")
	public PagedList getDentalSupplierDetails(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, DENTAL_SUPPLIER_FIELDS,
					DENTAL_SUPPLIER_COUNT, DENTAL_SUPPLIER_TABLES, pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("supplier_name", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String GET_ALL_DENTAL_SUPPLIERS = " SELECT supplier_id,supplier_name FROM dental_supplier_master ";
	
	@MigratedTo(value=DentalSupplierRepository.class, method="lookup")
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
}
