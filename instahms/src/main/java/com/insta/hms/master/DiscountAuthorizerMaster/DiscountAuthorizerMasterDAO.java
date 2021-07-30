/**
 *
 */
package com.insta.hms.master.DiscountAuthorizerMaster;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author lakshmi.p
 *
 */
public class DiscountAuthorizerMasterDAO extends GenericDAO {
	Connection con = null;


	public DiscountAuthorizerMasterDAO(){
		super("discount_authorizer");
	}

	public static final String GET_ALL_DISCOUNT_AUTHORIZERS = "SELECT disc_auth_id,disc_auth_name FROM discount_authorizer " ;

	public static List getDiscountAuthorizerNames() {
		PreparedStatement ps = null;
		List authorizerNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_DISCOUNT_AUTHORIZERS);
			authorizerNames = DataBaseUtil.queryToDynaList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return authorizerNames;
	}

	private static final String DISCOUNTS_NAMESAND_iDS="select disc_auth_id,disc_auth_name from discount_authorizer";

	   public static List getDiscountsNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(DISCOUNTS_NAMESAND_iDS));
	}

    public static final String GET_ACTIVE_DISCOUNT_AUTHORIZERS = "SELECT disc_auth_id,disc_auth_name,center_id FROM discount_authorizer where status='A' " ;

	public static List getActiveDiscountAuthorizerNames() throws SQLException {
		return DataBaseUtil.queryToDynaList(GET_ACTIVE_DISCOUNT_AUTHORIZERS);
	}

}
