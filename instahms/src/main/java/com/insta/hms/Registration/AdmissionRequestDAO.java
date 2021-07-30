/**
 *
 */
package com.insta.hms.Registration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */
public class AdmissionRequestDAO extends GenericDAO{
	static Logger log = LoggerFactory.getLogger(AdmissionRequestDAO.class);

	public AdmissionRequestDAO() {
		super("patient_admission_request");
	}

	private static final String ADMISSION_REQUEST_FIELDS = "SELECT par.*,d.doctor_name,hcm.center_name," +
			"	get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS patient_full_name";
	private static final String ADMISSION_REQUEST_FROM = " FROM patient_admission_request par " +
			"	JOIN patient_details pd ON(pd.mr_no=par.mr_no AND "
			+ " 	( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))" +
			"   JOIN salutation_master sm ON(sm.salutation_id=pd.salutation)" +
			"   LEFT JOIN doctors d ON(d.doctor_id=par.requesting_doc)" +
			"   JOIN hospital_center_master hcm ON(hcm.center_id = par.center_id)";
	private static final String ADMISSION_REQUEST_COUNT = "SELECT count(adm_request_id) ";

	public PagedList getPatientAdmissionRequest(Map params, Map<LISTING, Object> listingParams, Integer centerId)
		throws ParseException,SQLException{

		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, ADMISSION_REQUEST_FIELDS, ADMISSION_REQUEST_COUNT, ADMISSION_REQUEST_FROM, listingParams);
			qb.addFilterFromParamMap(params);
			if(centerId != null)
				qb.addFilter(SearchQueryBuilder.INTEGER, "par.center_id" ,"=", centerId);
			else if ((Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") > 1 && RequestContext.getCenterId() != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "par.center_id" ,"=", RequestContext.getCenterId());
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	private static final String  UPDATE_ADMISSION_CANCEL_ATTRIBUTES = "UPDATE patient_admission_request SET cancelled_by = ?," +
			"	cancellation_remarks=?,cancelled_on=?,status=? WHERE adm_request_id = ?";

	public static boolean updateCancelAdmissionAttributes(Connection con, String userName,String remarks,Timestamp cancellationDate, Integer admRequestId) throws SQLException {
		PreparedStatement ps = null;
		int index = 1;
		try {
			ps = con.prepareStatement(UPDATE_ADMISSION_CANCEL_ATTRIBUTES);
			ps.setString(index++, userName);
			ps.setString(index++, remarks);
			ps.setTimestamp(index++, cancellationDate);
			ps.setString(index++, "X");
			ps.setInt(index++, admRequestId);
			return ps.executeUpdate() >= 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String  GET_CANCELLED_ADMISSION_REQUEST = "SELECT * FROM patient_admission_request" +
			" WHERE adm_request_id = ? AND status = ?";

	public static BasicDynaBean getCancelledAdmissionRequest(Integer admRequestId) throws SQLException {
		PreparedStatement ps = null;
		int index = 1;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CANCELLED_ADMISSION_REQUEST);
			ps.setInt(index++, admRequestId.intValue());
			ps.setString(index++, "X");
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_ADMISSION_REQUEST_DETAILS = " SELECT par.*,doc.doctor_name as requsting_docto_name," +
			"	dep.dept_id as requesting_doctor_dept_id " +
			"   FROM patient_admission_request par " +
			"	JOIN doctors doc ON(doc.doctor_id=par.requesting_doc)" +
			"   JOIN department dep ON(doc.dept_id=dep.dept_id) " +
			"	WHERE adm_request_id = ? ";

	public static BasicDynaBean getPatientAdmissionRequestDetails(Integer admReqId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ADMISSION_REQUEST_DETAILS);
			ps.setInt(1, admReqId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String UPDATE_ADMISSION_STATUS = " UPDATE patient_admission_request SET status = ?,user_name = ?, mod_time = ?" +
			"	WHERE adm_request_id = ? ";

	public static boolean updateAdmissionRequestStatus(Connection con,Integer admReqId,String status, String userName) throws SQLException {
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(UPDATE_ADMISSION_STATUS);
			ps.setString(index++, status);
			ps.setString(index++, userName);
			ps.setTimestamp(index++, DateUtil.getCurrentTimestamp());
			ps.setInt(index++, admReqId);
			return ps.executeUpdate() >= 0;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
