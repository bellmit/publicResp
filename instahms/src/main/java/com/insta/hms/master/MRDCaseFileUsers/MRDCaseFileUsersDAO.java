/**
 *
 */
package com.insta.hms.master.MRDCaseFileUsers;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lakshmi.p
 *
 */
public class MRDCaseFileUsersDAO extends GenericDAO {
	Connection con = null;


	public MRDCaseFileUsersDAO(){
		super("mrd_casefile_users");
	}

	public static final String GET_ALL_MRD_CASEFILE_USERS = "SELECT file_user_id,file_user_name FROM mrd_casefile_users " ;

	public static List getMRDCaseFileUsers() {
		PreparedStatement ps = null;
		List userNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_MRD_CASEFILE_USERS);
			userNames = DataBaseUtil.queryToDynaList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return userNames;
	}


	private static String GET_ALL_USERS = " select * from mrd_casefile_users where status = 'A' order by file_user_name " ;

	public static List getAllUserNames() {
	PreparedStatement ps = null;
	ArrayList userNameList = null;
	Connection con = null;

	try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_USERS);
			userNameList = DataBaseUtil.queryToArrayList1(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
	}

		return userNameList;
	}
	
}
