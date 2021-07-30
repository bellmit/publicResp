package com.insta.hms.master.Bank;

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

public class BankMasterDAO extends GenericDAO {
	static Connection con = null;

	public BankMasterDAO()
	{
		super("bank_master");
	}

	private static String BANK_MASTER_FIELDS = " select *";

	private static String BANK_MASTER_COUNT = " SELECT count(bank_master) ";

	private static String BANK_MASTER_TABLES = " from bank_master";

	public PagedList getBankDetailPages(Map map, Map pagingParams)throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, BANK_MASTER_FIELDS, BANK_MASTER_COUNT, BANK_MASTER_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("bank_id");
			qb.build();

			return qb.getMappedPagedList();
		}finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String BANK_MASTER_DETAILS = "Select bank_id, bank_name from bank_master";

	public static List getBankNamesAndIds() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(BANK_MASTER_DETAILS));
	}

}