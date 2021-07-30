package com.insta.hms.master.CenterPreferences;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author mithun.saha
 *
 */
public class CenterPreferencesDAO extends GenericDAO{
	static Logger logger = LoggerFactory.getLogger(CenterPreferencesDAO.class);

	public CenterPreferencesDAO() {
		super("center_preferences");
	}


	private static final String UPDATE_CENTER_PREFS = "UPDATE center_preferences SET " +
			"	pref_rate_plan_for_non_insured_bill = ? WHERE center_id = ?";

	private static final String INSERT_CENTER_PREFS = "INSERT INTO center_preferences(pref_default_op_bill_type, pref_default_ip_bill_type, pref_rate_plan_for_non_insured_bill,center_id) VALUES( " +
			" ?, ?, ?, ? )";
	
	private static final Integer DEFAULT_CENTER = 0;

	/*public static boolean saveCenterPreferences(CenterPreferencesDTO dto,Integer centerId)
	throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		boolean flag = false;

		try {
			con = DataBaseUtil.getConnection();
			int i=1;
			boolean exists = new CenterPreferencesDAO().exist("center_id", centerId);
			CenterPreferencesDTO defaultCenterPrefs = CenterPreferencesDAO.getCenterPreferences();
			if(exists) {
				ps = con.prepareStatement(UPDATE_CENTER_PREFS);
			} else {
				ps = con.prepareStatement(INSERT_CENTER_PREFS);
				ps.setString(i++, defaultCenterPrefs.getPrefDefaultOpBillType());
				ps.setString(i++, defaultCenterPrefs.getPrefDefaultIpBillType());
			}
			ps.setString(i++, dto.getPrefRatePlanForNonInsuredBill());
			ps.setInt(i++, centerId);
			if (ps.executeUpdate() == 1) {
				flag = true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return flag;
	}*/

	public BasicDynaBean getCenterPreferences(Connection con, Integer centerId) throws SQLException{
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		boolean localCon = con == null;
		try {
			con = con == null ? DataBaseUtil.getConnection() : con;
			ps = con.prepareStatement(GET_CENTER_PREFS);
			ps.setInt(1, centerId);
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean == null){
				ps.setInt(1, 0);
				bean = DataBaseUtil.queryToDynaBean(ps);
			}
		} finally {
			// close the connection only if it is created in this method locally.
			DataBaseUtil.closeConnections(localCon ? con : null, ps);
		}
		return bean;
	}

	public BasicDynaBean getCenterPreferences(Integer centerId) throws SQLException{
		return getCenterPreferences(null, centerId);
	}

	public BasicDynaBean getCenterPreferences() throws SQLException {
		return getCenterPreferences(0);
	}

	private static final String GET_CENTER_PREFS = "SELECT * " +
			"	FROM center_preferences WHERE center_id = ?";

	public static BasicDynaBean getAllCenterPrefs(Integer centerId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CENTER_PREFS);
			ps.setInt(1, centerId);
			bean = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}if (bean == null) {
			bean = getAllCenterPrefs(DEFAULT_CENTER);
		}
		return bean;
	}

	public static BasicDynaBean getAllCenterPrefs() throws SQLException {
		return getAllCenterPrefs(0);
	}

	public static String getPreferenceBillRatePlanForNonInsuredBill(Integer centerId) throws SQLException {
		String ratePlan = null;
		BasicDynaBean prefbean = getAllCenterPrefs(centerId);
		if (prefbean != null && prefbean.get("pref_rate_plan_for_non_insured_bill") != null
					&& !((String)prefbean.get("pref_rate_plan_for_non_insured_bill")).equals(""))
			return (String)prefbean.get("pref_rate_plan_for_non_insured_bill");
		return ratePlan;
	}

	public static String getPreferenceBillRatePlanForNonInsuredBill() throws SQLException {
		return getPreferenceBillRatePlanForNonInsuredBill(0);
	}

	public static String getRatePlanForNonInsuredBills(Integer centerId) throws SQLException {
		String ratePlan = null;
		BasicDynaBean centerPrefs = CenterPreferencesDAO.getAllCenterPrefs(centerId);
		if(centerPrefs == null)
			centerPrefs = CenterPreferencesDAO.getAllCenterPrefs();

		ratePlan = (centerPrefs != null && centerPrefs.get("pref_rate_plan_for_non_insured_bill") != null
								&& !centerPrefs.get("pref_rate_plan_for_non_insured_bill").equals("")) ?
									(String)centerPrefs.get("pref_rate_plan_for_non_insured_bill") : null;
		return ratePlan;
	}

	private static final String GET_ALL_CENTER_PREFS = "SELECT pref_default_ip_bill_type,pref_default_op_bill_type," +
			"	pref_rate_plan_for_non_insured_bill,center_id, emergency_patient_category_id " +
			"	FROM center_preferences";

	public static List<BasicDynaBean> getAllCentersPreferences() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_CENTER_PREFS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getCentersPreferencesUrls(int centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT pacs_mrno_url,pacs_order_url FROM center_preferences WHERE center_id = ? ");
			ps.setInt(1, centerId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			if(bean == null) {
				ps.setInt(1, 0);
				return DataBaseUtil.queryToDynaBean(ps);
			}
			return bean;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
