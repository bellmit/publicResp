package com.insta.hms.master.DietaryMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PatientDietPrescriptionsDAO extends GenericDAO{

	public PatientDietPrescriptionsDAO() {
		super("patient_diet_prescriptions");
	}


	private String GET_PRESCRIBED_MEALS = " SELECT *,dp.status,dp.status_updated_time,doc.doctor_name " +
										  " FROM patient_diet_prescriptions pdp " +
										  " LEFT JOIN diet_prescribed dp ON pdp.visit_id = dp.visit_id AND pdp.diet_pres_id = dp.diet_pres_id " +
										  " LEFT JOIN doctors doc ON (doc.doctor_id = pdp.prescribed_by )" +
										  " WHERE pdp.visit_id = ?  " +
										  " ORDER BY pdp.meal_date ";


	public List<BasicDynaBean> getPrescribedMealsForPatient(String visitId) throws SQLException{
			Connection con = DataBaseUtil.getReadOnlyConnection();
			PreparedStatement ps = con.prepareStatement(GET_PRESCRIBED_MEALS);
			ps.setString(1, visitId);

			List<BasicDynaBean> prescribedMealList = DataBaseUtil.queryToDynaList(ps);

			DataBaseUtil.closeConnections(con, ps);
			return prescribedMealList;
	}

}
