package com.insta.hms.master.SalutationMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BasicCachingDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class SalutationMasterDAO extends BasicCachingDAO {

	public SalutationMasterDAO() {
		super("salutation_master");
	}

	public String getNextSalutationId() throws SQLException {
		String salutationId = null;
		salutationId =	AutoIncrementId.getNewIncrId("SALUTATION_ID", "SALUTATION_MASTER", "salutation");
		return salutationId;
	}

	public static final String getSalutationIdName =
		"SELECT salutation_id, salutation, gender FROM salutation_master WHERE status='A'";

	public static List getSalutationIdName() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List saluationIdName = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(getSalutationIdName);
			saluationIdName = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return saluationIdName ;
	}
}
