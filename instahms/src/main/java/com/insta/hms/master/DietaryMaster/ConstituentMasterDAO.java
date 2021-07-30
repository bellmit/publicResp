package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConstituentMasterDAO extends GenericDAO{

	public ConstituentMasterDAO() {
		super("diet_constituents");
	}

	private String GET_ALL_CONSTIUTENT_FOR_DIET = " SELECT * from  diet_constituents where diet_id = ? ";

	public ArrayList getConstiuentForDiet(int dietID){

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ArrayList constiutentList = null;
		try {
				ps = con.prepareStatement(GET_ALL_CONSTIUTENT_FOR_DIET);
				ps.setInt(1, dietID);

				constiutentList = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return constiutentList;
	}

	private String GET_TOTAL_CALORY = " SELECT sum(calorific_value) as total_calory FROM diet_constituents WHERE diet_id = ? ";
	public String  getTotalCalorificValue(int dietId) throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_TOTAL_CALORY);
		ps.setInt(1, dietId);

		String totalCalValue = DataBaseUtil.getStringValueFromDb(ps);
		DataBaseUtil.closeConnections(con, ps);
		return totalCalValue;
	}
}
