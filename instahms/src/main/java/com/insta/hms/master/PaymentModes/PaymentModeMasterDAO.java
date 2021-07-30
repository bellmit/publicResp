package com.insta.hms.master.PaymentModes;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class PaymentModeMasterDAO extends GenericDAO {
	static Connection con = null;

	public PaymentModeMasterDAO()
	{
		super("payment_mode_master");
	}

	private static String PAYMENT_MODE_FIELDS = " select *";

	private static String PAYMENT_MODE_COUNT = " SELECT count(payment_mode_master) ";

	private static String PAYMENT_MODE_TABLES = " from payment_mode_master";

	public PagedList getPaymentModeDetailPages(Map map, Map pagingParams)throws ParseException, SQLException {

		Connection con = null;
		SearchQueryBuilder qb = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, PAYMENT_MODE_FIELDS, PAYMENT_MODE_COUNT, PAYMENT_MODE_TABLES, pagingParams);
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("displayorder");
			qb.build();

			return qb.getMappedPagedList();
		}finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String PAYMENT_MODE_DETAILS = "Select mode_id, payment_mode from payment_mode_master";

	public static List getPaymentModeNamesAndIds() throws SQLException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(PAYMENT_MODE_DETAILS));
	}

	private static final String PAYMENT_MODE_TABLE_DETAILS =
		" SELECT mode_id, payment_mode, payment_mode AS mode_name from payment_mode_master " +
		" UNION " +
		" SELECT -2 AS mode_id, '_total' as payment_mode, 'Total' AS mode_name from payment_mode_master  " +
		" ORDER BY mode_id desc";

	public List getPaymentModeFieldsWithTotal() throws SQLException {
		return DataBaseUtil.queryToDynaList(PAYMENT_MODE_TABLE_DETAILS);
	}
	
	
	private static final String SPECIFIC_PAYMENT_MODE_DEATILS = 
		"SELECT * from payment_mode_master where status = 'A' ORDER BY displayorder ASC";
	
	public List getSpecificPaymentModeDetails() throws SQLException {
		return DataBaseUtil.queryToDynaList(SPECIFIC_PAYMENT_MODE_DEATILS);
	}
	
	private static final String GET_PAYMENT_MODE_NAME_BY_ID = "SELECT payment_mode FROM payment_mode_master WHERE mode_id = ?";
	public BasicDynaBean getPaymentMode(int paymentModeId) throws SQLException{
		Connection connection = null;
		PreparedStatement statement = null;
		try{
			connection = DataBaseUtil.getConnection();
			statement = connection.prepareStatement(GET_PAYMENT_MODE_NAME_BY_ID);
			statement.setInt(1, paymentModeId);
			return DataBaseUtil.queryToDynaBean(statement);
		}finally{
			DataBaseUtil.closeConnections(connection, statement);
		}
		
	}
	private static final String CASH_MODE_TRANSACTION_LIMIT =
			"SELECT transaction_limit from payment_mode_master where mode_id = -1";

	public BigDecimal getCashLimit() throws SQLException {
		Connection connection = null;
		PreparedStatement statement = null;
		BigDecimal cashLimit = BigDecimal.ZERO;
		try {
			connection = DataBaseUtil.getConnection();
			statement = connection.prepareStatement(CASH_MODE_TRANSACTION_LIMIT);
			cashLimit= DataBaseUtil.getBigDecimalValueFromDb(statement);
		} finally {
			DataBaseUtil.closeConnections(connection, statement);
		}
		return cashLimit;
	}
}
