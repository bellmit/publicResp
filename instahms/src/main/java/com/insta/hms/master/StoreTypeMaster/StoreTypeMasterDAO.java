package com.insta.hms.master.StoreTypeMaster;

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

public class StoreTypeMasterDAO extends GenericDAO {
	static Connection con = null;

	public StoreTypeMasterDAO()
	{
		super("store_type_master");
	}

	private static String STORE_TYPE_FIELDS = " select *";

	private static String STORE__TYPE_COUNT = " SELECT count(store_type_name) ";

	private static String STORE_TYPE_TABLES = " from store_type_master";

	public PagedList getStoreTypeDetailPages(Map map, Map pagingParams)throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, STORE_TYPE_FIELDS, STORE__TYPE_COUNT, STORE_TYPE_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("store_type_id");
			qb.build();

			return qb.getMappedPagedList();
		}finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String STORE_TYPE_DETAILS = "Select store_type_id, store_type_name from store_type_master";

		public static List getStoreTypeNamesAndIds() throws SQLException {
			return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(STORE_TYPE_DETAILS));
		}

}
