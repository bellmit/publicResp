/**
 *
 */
package com.insta.hms.vitalForm;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class VisitVitalsDAO extends GenericDAO {

	public VisitVitalsDAO() {
		super("visit_vitals");
	}

	public List getVisitVitals(Connection con, String patientId) throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM visit_vitals WHERE patient_id=? and vv.status='A'");
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	public static final String GET_LATEST_READING_FOR_PATINT = 
		" SELECT  vpm.param_label, vpm.param_uom, vv.vital_reading_id, vr.param_id, mandatory_in_tx, " +
		"	vr.param_value, date_time, vv.user_name " +
		" FROM vital_reading vr " +
		"		JOIN visit_vitals vv USING(vital_reading_id) " +  
		"		JOIN vital_parameter_master vpm USING (param_id) " + 
		"	WHERE vpm.param_status='A' and vv.status='A' and vr.status='A' and " +
		"		vital_reading_id=(SELECT vital_reading_id FROM visit_vitals vv where vv.patient_id in " +
		"			(SELECT patient_id FROM patient_registration pr " + 
		"				where pr.mr_no=? and pr.patient_id!=? and pr.visit_type='o' and reg_date+reg_time >= (localtimestamp(0) - interval '24 hours') " +
		"			ORDER BY reg_date+reg_time desc limit 1) and vv.status='A' " +
		" 		ORDER BY vv.date_time desc limit 1) " +
		" ORDER BY vpm.param_order ASC";
	
	public List<BasicDynaBean> getLatestVitals(String mrNo, String patientId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_LATEST_READING_FOR_PATINT);
			ps.setString(1, mrNo);
			ps.setString(2, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	// fetch vitals params for the particular, used while OP to IP conversion
	public static final String GET_LATEST_READING_FOR_PATINT_FOR_VISIT = 
			" SELECT  vpm.param_label, vpm.param_uom, vv.vital_reading_id, vr.param_id, mandatory_in_tx, " +
			"	vr.param_value, date_time, vv.user_name " +
			" FROM vital_reading vr " +
			"		JOIN visit_vitals vv USING(vital_reading_id) " +  
			"		JOIN vital_parameter_master vpm USING (param_id) " + 
			"	WHERE vpm.param_status='A' and vv.status='A' and vr.status='A' and (visit_type = ? OR visit_type is null) and " +
			"		vital_reading_id=(SELECT vital_reading_id FROM visit_vitals vv where vv.patient_id in " +
			"			(SELECT patient_id FROM patient_registration pr " + 
			"				where pr.mr_no=? and pr.patient_id!=? and pr.visit_type=? and reg_date+reg_time >= (localtimestamp(0) - interval '24 hours') " +
			"			ORDER BY reg_date+reg_time desc limit 1) and vv.status='A' " +
			" 		ORDER BY vv.date_time desc limit 1) " +
			" ORDER BY vpm.param_order ASC";
		
		public List<BasicDynaBean> getLatestVitalsForVisit(String mrNo, String patientId, String visitType) throws SQLException{
			Connection con = DataBaseUtil.getConnection();
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(GET_LATEST_READING_FOR_PATINT_FOR_VISIT);
				ps.setString(1, visitType);
				ps.setString(2, mrNo);
				ps.setString(3, patientId);
				if (visitType.equals("I"))
					ps.setString(4, "i");
				else
					ps.setString(4, "o");
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
	
	public static final String GET_PATIENT_VITAL_DETAILS =
		" SELECT  vpm.param_label, vpm.param_uom, vv.vital_reading_id, vr.param_id, vr.param_value, date_time, vr.param_remarks, " +
		"	vv.user_name, vv.vital_status " +
		" FROM vital_reading vr " +
		" 	JOIN visit_vitals vv USING(vital_reading_id) " +
		" 	JOIN vital_parameter_master vpm USING(param_id) " +
		" WHERE vpm.param_status='A' and vv.patient_id=? and vv.status='A' and vr.status='A'";
	
	public boolean readingsExist(String patientId, String paramContainer) throws SQLException {
		paramContainer = paramContainer ==  null ? "" : paramContainer;
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = GET_PATIENT_VITAL_DETAILS;

		if (!paramContainer.equals("") && !paramContainer.equals("V"))
			query += " AND param_container IN ('I','O') ";
		else
			query += " AND param_container = 'V' ";

		try {
			int i=1;
			ps = con.prepareStatement(query + " ORDER BY vv.date_time,vv.vital_reading_id, vpm.param_container DESC, vpm.param_order ASC");
			ps.setString(i++, patientId);

			rs = ps.executeQuery();
			
			while (rs.next()) {
				return true;
			}
			return false;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}
	
	public List getVitals(String patientId, String fromDate, String toDate, String paramContainer) throws SQLException, ParseException {
		Map pd = VisitDetailsDAO.getPatientVisitDetailsMap(patientId);
		return getVitals(patientId, fromDate, toDate, paramContainer, pd);
	}
	
	public List getVitals(String patientId, String fromDate, String toDate, String paramContainer, Map pd) throws SQLException,
		ParseException {
		fromDate = fromDate == null ? "" : fromDate;
		toDate = toDate == null ? "" : toDate;
		paramContainer = paramContainer ==  null ? "" : paramContainer;
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;

		String query = GET_PATIENT_VITAL_DETAILS;

		if (!paramContainer.equals("") && !paramContainer.equals("V"))
			query += " AND param_container IN ('I','O') ";
		else
			query += " AND param_container = 'V' ";
		if (!fromDate.equals(""))
			query += " AND date(vv.date_time) >= ?";

		if (!toDate.equals(""))
			query += " AND date(vv.date_time) <= ?";

		ResultSet rs = null;
		try {
			int i=1;
			ps = con.prepareStatement(query + " ORDER BY vv.date_time,vv.vital_reading_id, vpm.param_container DESC, vpm.param_order ASC");
			ps.setString(i++, patientId);
			if (!fromDate.equals(""))
				ps.setDate(i++, DateUtil.parseDate(fromDate));
			if (!toDate.equals(""))
				ps.setDate(i++, DateUtil.parseDate(toDate));

			rs = ps.executeQuery();
			int readingId = 0;
			VisitVitals vvDto = null;
			List<VisitVitals> readingsList = new ArrayList<VisitVitals>();
			while (rs.next()) {
				int currentVitalReadingId = rs.getInt("vital_reading_id");
				if (readingId != currentVitalReadingId) {
	        vvDto = new VisitVitals();
					vvDto.setDateTime(rs.getTimestamp("date_time"));
					vvDto.setVitalReadingId(currentVitalReadingId);
					vvDto.setUserName(rs.getString("user_name"));
					vvDto.setVitalStatus(rs.getString("vital_status"));
					readingsList.add(vvDto);
				}

				VitalsReadings details = new VitalsReadings();
				details.setParamValue(rs.getString("param_value"));
				details.setParamId(rs.getInt("param_id"));
				details.setParamLabel(rs.getString("param_label"));
				details.setParamUOM(rs.getString("param_uom"));
				details.setColorCode(getColorCode(patientId, rs.getInt("param_id"), rs.getString("param_value"), pd));
				details.setParamRemarks(rs.getString("param_remarks"));
				if (vvDto != null ) {
				  vvDto.addReading(details);
				}
				readingId = currentVitalReadingId;
			}
			return readingsList;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	
	public boolean insertVitals(Connection con, String patientId, String userName, Timestamp dateTime,
			int vitalReadingId, String[] paramIds, String[] paramValues, String[] paramLabels,
			boolean edited, String[] paramRemarks, boolean deleted) throws
			SQLException, IOException, Exception {
		// if the vital reading is edited return true;
		if (vitalReadingId != 0 && !edited && !deleted) return true;

		boolean flag = true;
		boolean insertedOrUpdated = false;
		BasicDynaBean vitalbean = getBean();
		vitalbean.set("user_name", userName);
		vitalbean.set("date_time", dateTime);
		vitalbean.set("patient_id", patientId);
		if (vitalReadingId == 0) {
			vitalReadingId = getNextSequence();
			vitalbean.set("vital_reading_id", vitalReadingId);
			flag = flag && insert(con, vitalbean);
			insertedOrUpdated = true;
		} else {
			if (deleted) {
				vitalbean.set("status","I");
				flag = flag && update(con, vitalbean.getMap(), "vital_reading_id", vitalReadingId) > 0;
				flag = flag && new VitalReadingsDAO().inactiveVitalsFromVitalReading(con,vitalReadingId);
				insertedOrUpdated = true;
			}
			else if (edited) {
				flag = flag && update(con, vitalbean.getMap(), "vital_reading_id", vitalReadingId) > 0;
				insertedOrUpdated = true;
			}
		}
		if (flag && insertedOrUpdated) {
			flag = flag && new VitalReadingsDAO().insert(con, vitalReadingId, paramIds, paramValues, paramLabels, paramRemarks, userName);
		}
		return flag;
	}

	public boolean deleteVitals(Connection con, int vitalReadingId) throws SQLException {
		boolean flag = new VitalReadingsDAO().delete(con, "vital_reading_id", vitalReadingId);
		flag = flag && delete(con, "vital_reading_id", vitalReadingId);
		return flag;
	}

	public String getVisitType(String patientId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT visit_type FROM patient_registration WHERE patient_id=?");
			ps.setString(1, patientId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String exactresultrange =
		" SELECT * FROM vital_reference_range_master where param_id = ? AND  " +
		" ( range_for_all = 'N'  AND " +
		" ( (min_patient_age IS NULL OR min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) <= " +
		"		( SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer " +
		"			FROM patient_details WHERE mr_no = ? ) )" +
		"  AND" +
		"   (max_patient_age IS NULL OR (  SELECT (current_date - COALESCE(dateofbirth, expected_dob))::integer " +
		"			FROM patient_details WHERE mr_no = ? ) <= " +
		"			max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )) " +
		" AND (patient_gender = ? OR patient_gender = 'N')) ORDER BY priority LIMIT 1 ";

	private static String result_all = " SELECT * FROM vital_reference_range_master where param_id = ? AND  " +
		" range_for_all = 'Y'  ";


	public String getColorCode(String patientId, Integer param_id, String value, Map pd)throws SQLException {


		Connection con = null;
		PreparedStatement ps = null;
		String placeHolderValue =  (String)pd.get("mr_no") ;
		BasicDynaBean resultRangeBean = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(exactresultrange);
			ps.setInt(1, param_id);
			ps.setString(2,placeHolderValue);
			ps.setString(3, placeHolderValue);
			ps.setString(4, (String)pd.get("patient_gender"));
			resultRangeBean = DataBaseUtil.queryToDynaBean(ps);

			if( resultRangeBean == null ){
				ps = con.prepareStatement(result_all);
				ps.setInt(1, param_id);

				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			}

			BasicDynaBean prefColorBean = GenericPreferencesDAO.getAllPrefs();
			BigDecimal paramValue = null;
			if (null != value && !value.equals("")) {
				try{
					paramValue = new BigDecimal(value);
				}catch(java.lang.NumberFormatException ex) {
					return (String)prefColorBean.get("normal_color_code");
				}
			}


			if (paramValue == null || resultRangeBean == null) {

				return (String)prefColorBean.get("normal_color_code");
			} else if (null != resultRangeBean.get("max_improbable_value") && !resultRangeBean.get("max_improbable_value").equals("")
					&& paramValue.floatValue() > ((BigDecimal)resultRangeBean.get("max_improbable_value")).floatValue()) {
					return (String)prefColorBean.get("improbable_color_code");

			} else if (null != resultRangeBean.get("max_critical_value") && !resultRangeBean.get("max_critical_value").equals("")
					&& paramValue.floatValue() > ((BigDecimal)resultRangeBean.get("max_critical_value")).floatValue()) {
				return (String)prefColorBean.get("critical_color_code");

			} else if (null != resultRangeBean.get("max_normal_value") && !resultRangeBean.get("max_normal_value").equals("")
					&& paramValue.floatValue() > ((BigDecimal)resultRangeBean.get("max_normal_value")).floatValue()) {
				return (String)prefColorBean.get("abnormal_color_code");
			} else if (null != resultRangeBean.get("min_improbable_value") && !resultRangeBean.get("min_improbable_value").equals("")
					&& paramValue.floatValue() < ((BigDecimal)resultRangeBean.get("min_improbable_value")).floatValue()) {
					return (String)prefColorBean.get("improbable_color_code");

			} else if (null != resultRangeBean.get("min_critical_value") && !resultRangeBean.get("min_critical_value").equals("")
					&& paramValue.floatValue() < ((BigDecimal)resultRangeBean.get("min_critical_value")).floatValue()) {
				return (String)prefColorBean.get("critical_color_code");

			} else if (null != resultRangeBean.get("min_normal_value") && !resultRangeBean.get("min_normal_value").equals("")
					&& paramValue.floatValue() < ((BigDecimal)resultRangeBean.get("min_normal_value")).floatValue()) {
				return (String)prefColorBean.get("abnormal_color_code");

			} else if ((null != resultRangeBean.get("min_normal_value") && !resultRangeBean.get("min_normal_value").equals(""))
					|| (null != resultRangeBean.get("max_normal_value") && !resultRangeBean.get("max_normal_value").equals(""))) {

				return (String)prefColorBean.get("normal_color_code");

			} else{
				return (String)prefColorBean.get("normal_color_code");
			}

		} finally {

			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_VISIT_VITAL_WEIGHT_DETAILS =
		" SELECT param_label, param_uom, vpm.param_id, vr.param_value, " +
		" localtimestamp(0) as date_time, vr.vital_reading_id ,vpm.visit_type," +
		" vpm.expr_for_calc_result" +
		" FROM visit_vitals vls " +
		" JOIN vital_reading vr ON (vr.vital_reading_id=vls.vital_reading_id)" +
		" JOIN vital_parameter_master vpm ON (vpm.param_id=vr.param_id)" +
		" JOIN patient_registration pr ON (pr.patient_id = vls.patient_id)" +
		" WHERE param_container='V' " +
		" AND (vpm.visit_type = CASE WHEN pr.visit_type :: text = 'i' THEN 'I' else 'O'" +
		" END OR vpm.visit_type is null) " +
		" AND param_status='A' AND param_label='Weight' " +
		" AND vls.patient_id= ? and vls.status='A' ORDER BY vls.date_time DESC limit 1";

	public static BasicDynaBean getVisitVitalWeightBean(String patientId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_VISIT_VITAL_WEIGHT_DETAILS);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static final String GET_HEIGHT_AND_WEIGHT = 
		  " SELECT vpm.param_label AS paramlabel, "
		+ "	vr.param_id AS paramid, vr.param_value AS paramvalue, "
		+ " vv.vital_reading_id AS vital_reading_id, vv.date_time, vv.user_name AS user_name, pr.patient_id "
		+ "	FROM visit_vitals vv "
		+ "		JOIN patient_registration pr using (patient_id) "
		+ "		JOIN vital_reading vr using (vital_reading_id) "
		+ "		JOIN vital_parameter_master vpm on (vr.param_id=vpm.param_id) "
		+ " WHERE pr.mr_no=? and vv.status='A' and vr.status='A' and "
		+ " 	reg_date+reg_time <= (select reg_date+reg_time from patient_registration where patient_id=?) "
		+ "		and vpm.param_id in (5, 6) order by pr.reg_date+pr.reg_time desc, vv.date_time desc limit 2 ";
	public List getHeightAndWeight(String mrNo, String patientId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_HEIGHT_AND_WEIGHT);
			ps.setString(1, mrNo);
			ps.setString(2, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
