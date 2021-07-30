/**
 *
 */
package com.insta.hms.master.ForeignCurrency;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi.p
 *
 */
public class ForeignCurrencyDAO  extends GenericDAO {
	public ForeignCurrencyDAO() {
		super("foreign_currency");
	}

	public int getNextCurrencyId() throws SQLException {
		return getNextSequence();
	}

	private static String CURRENCY_EXIST =
					"SELECT currency FROM foreign_currency where currency_id=? and upper(currency)=upper(?)";
	public boolean exist(int currencyId, String currency) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(CURRENCY_EXIST);
			ps.setInt(1, currencyId);
			ps.setString(2, currency);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	public static final String CURRENCY_DETAILS = "SELECT currency_id, currency, conversion_rate, status " +
			" FROM foreign_currency " ;

	public static List<BasicDynaBean> getCurrencyDetails() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = con.prepareStatement(CURRENCY_DETAILS);
		List l = DataBaseUtil.queryToDynaList(ps);
		if (ps != null) ps.close();
		if (con != null) con.close();
		return l;
	}

}
