package com.insta.hms.resourcemanagement;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class contractsTypeDAO extends GenericDAO {

	public contractsTypeDAO(){
		super("contract_type_master");
	}

	public boolean insertContractDetails(Connection con,Map fields) throws SQLException {
		return DataBaseUtil.dynaInsert(con, "contract_type_master", fields);
	}

	private static final String GET_LICENSE_DETAILS = " SELECT contract_type,contract_type_id,status FROM contract_type_master where contract_type_id=?";

	public static BasicDynaBean getContractDetails(int contractId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_LICENSE_DETAILS, contractId);
		return (BasicDynaBean) l.get(0);
	}

	public boolean updateFields(Connection con,int contract_typeId, Map fields) throws SQLException {
		HashMap key = new HashMap();
		key.put("contract_type_id", contract_typeId);
		int rows = DataBaseUtil.dynaUpdate(con, "contract_type_master", fields, key);
		return (rows == 1);
	}

	private static String GET_ALL = " select contract_type_id,contract_type from contract_type_master " ;

	public static List getAllcontracts() {
		PreparedStatement ps = null;
		ArrayList contractType = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL);
			contractType = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return contractType;
	}
}