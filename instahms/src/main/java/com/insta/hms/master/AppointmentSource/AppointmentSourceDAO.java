package com.insta.hms.master.AppointmentSource;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentSourceDAO extends GenericDAO{

	public AppointmentSourceDAO() {
		super("appointment_source_master");
	}


	public boolean insertAppointmentSourceDetails(Connection con,Map fields) throws SQLException {
		return DataBaseUtil.dynaInsert(con, "appointment_source_master", fields);
	}

	private static final String GET_APPOINTMENT_SOURCE_DETAILS = " SELECT * "
			+ " FROM appointment_source_master where appointment_source_id=?";

	public static BasicDynaBean getAppointmentSourceDetails(int appointmentSourceId) throws SQLException {
		List l = DataBaseUtil.queryToDynaList(GET_APPOINTMENT_SOURCE_DETAILS, appointmentSourceId);
		return (BasicDynaBean) l.get(0);
	}

	public boolean updateFields(Connection con,int appointment_sourceId, Map fields) throws SQLException {
		HashMap key = new HashMap();
		key.put("appointment_source_id", appointment_sourceId);
		int rows = DataBaseUtil.dynaUpdate(con, "appointment_source_master", fields, key);
		return (rows == 1);
	}

	private static String GET_ALL = "select * from appointment_source_master";

	public static List getAllAppointmentSourceDetails()throws SQLException {
		PreparedStatement ps = null;
		ArrayList channelingSource = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL);
			channelingSource = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return channelingSource;
	}
	
	private static String GET_ACTIVE = "select * from appointment_source_master where status='A'";

	public static List getActiveAppointmentSourceDetails()throws SQLException {
		PreparedStatement ps = null;
		ArrayList channelingSource = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ACTIVE);
			channelingSource = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return channelingSource;
	}
	private static String GET_SOURCE_ID = "select appointment_source_id from appointment_source_master where appointment_source_name ilike ?";

	public static Integer getAppointmentSourceId(String appointmentSource) throws SQLException {
	BasicDynaBean bean = DataBaseUtil.queryToDynaBean(GET_SOURCE_ID, new Object[] {appointmentSource});
	if(bean!=null &&  bean.get("appointment_source_id")!=null)
		return (Integer) bean.get("appointment_source_id");
	else
		return 0;
}
	private static String GET_SOURCE_DETAILS_FROM_NAME = "select appointment_source_id, patient_day_appt_limit from appointment_source_master where appointment_source_name ilike ?";

  public static BasicDynaBean getAppointmentSourceDetailsByName(String appointmentSource) throws SQLException {
  return DataBaseUtil.queryToDynaBean(GET_SOURCE_DETAILS_FROM_NAME, new Object[] {appointmentSource});
}

}
