package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.DataBaseUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DietaryMasterBO {


	DietChargesDAO dietChargeDAO = new DietChargesDAO();

	public boolean groupUpdateCharges(String strOrgid, List<String> bedTypes, List<Integer> selectDiets,
			BigDecimal amount, boolean isPercentage, BigDecimal roundOff, String updateTable) throws SQLException {
		Connection con = null;
		boolean success = true;
		con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		dietChargeDAO.groupUpdateDiets(con, strOrgid, bedTypes, selectDiets, amount, isPercentage, roundOff, updateTable);
		DataBaseUtil.commitClose(con, success);
		return success;
	}
}
