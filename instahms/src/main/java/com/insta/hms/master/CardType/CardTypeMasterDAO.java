package com.insta.hms.master.CardType;

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

public class CardTypeMasterDAO extends GenericDAO {
	static Connection con = null;

	public CardTypeMasterDAO()
	{
		super("card_type_master");
	}

	private static String CARD_TYPE_FIELDS = " select *";

	private static String CARD_TYPE_COUNT = " SELECT count(card_type_master) ";

	private static String CARD_TYPE_TABLES = " from card_type_master";

	public PagedList getCardTypeDetailPages(Map map, Map pagingParams)throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, CARD_TYPE_FIELDS, CARD_TYPE_COUNT, CARD_TYPE_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("card_type_id");
			qb.build();

			return qb.getMappedPagedList();
		}finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String CARD_TYPE_DETAILS = "Select card_type_id, card_type from card_type_master";

	public static List getCardTypeNamesAndIds() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(CARD_TYPE_DETAILS));
	}
}