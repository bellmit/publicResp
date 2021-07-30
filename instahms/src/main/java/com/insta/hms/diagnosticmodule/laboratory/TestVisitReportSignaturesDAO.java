/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

/**
 * @author krishna
 *
 */
public class TestVisitReportSignaturesDAO extends GenericDAO {

	DoctorMasterDAO doctorsDAO = new DoctorMasterDAO();

	public TestVisitReportSignaturesDAO() {
		super("test_visit_report_signatures");
	}

	public static final String REPORT_SIGNATURE_PARAMS_DOCTOR = "SELECT * FROM test_visit_report_signatures WHERE " +
			" report_id=? AND prescribed_id=? AND doctor_id=? AND signed_as=?";
	public static final String REPORT_SIGNATURE_PARAMS_TECH = "SELECT * FROM test_visit_report_signatures WHERE " +
			" report_id=? AND prescribed_id=? AND signatory_username=? AND signed_as=?";
	public BasicDynaBean getReportSignatureRecord(int reportId, int prescribedId, String doctorIdOrUsername, String signed_as) throws
		SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(signed_as.equals("T") ? REPORT_SIGNATURE_PARAMS_TECH : REPORT_SIGNATURE_PARAMS_DOCTOR);
			ps.setInt(1, reportId);
			ps.setInt(2, prescribedId);
			ps.setString(3, doctorIdOrUsername);
			ps.setString(4, signed_as);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean insertSignatureRecord(Connection con, int reportId, int prescribedId, String userName,
			String signedAs, String techOrDoctor) throws SQLException, IOException {
		techOrDoctor = techOrDoctor == null ? "" : techOrDoctor;
		if (techOrDoctor.equals("")) return true;

		BasicDynaBean sigBean = getReportSignatureRecord(reportId, prescribedId, techOrDoctor, signedAs);
		boolean status = true;
		if (sigBean == null) {
			GenericDAO userDAO = new GenericDAO("u_user");

			BasicDynaBean bean = getBean();
			bean.set("report_id", reportId);
			bean.set("prescribed_id", prescribedId);
			bean.set("signed_as", signedAs);
			String allow_sig_usage_by_others = "";

			if (signedAs.equals("T")) {
				bean.set("signatory_username", techOrDoctor);
				BasicDynaBean user = userDAO.findByKey("emp_username", techOrDoctor);
				allow_sig_usage_by_others = (String) user.get("allow_sig_usage_by_others");
			} else {
				bean.set("doctor_id", techOrDoctor);
				allow_sig_usage_by_others = (String) ((BasicDynaBean)
						doctorsDAO.findByKey("doctor_id", techOrDoctor)).get("allow_sig_usage_by_others");
			}
			BasicDynaBean loggedInUser = userDAO.findByKey("emp_username", userName);
			if (techOrDoctor.equals(signedAs.equals("T") ? userName : loggedInUser.get("doctor_id"))) {
				status = insert(con, bean);
			} else {
				if (allow_sig_usage_by_others.equals("Y"))
					status &= insert(con, bean);
			}
		} else {
			String colName = signedAs.equals("T") ? "signatory_username" : "doctor_id";
			if (!sigBean.get(colName).equals(techOrDoctor)) {
				LinkedHashMap map = new LinkedHashMap();
				map.put("report_id", reportId);
				map.put("prescribed_id", prescribedId);
				map.put("signed_as", signedAs);
				status &= delete(con, map);

				BasicDynaBean bean = getBean();
				bean.set("report_id", reportId);
				bean.set("prescribed_id", prescribedId);
				bean.set(colName, techOrDoctor);
				bean.set("signed_as", signedAs);
				status &= insert(con, bean);
			}
		}
		return status;

	}

	public static boolean deleteSignatures(Connection con, int reportId, int prescribedId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT * FROM test_visit_report_signatures WHERE report_id=? AND prescribed_id=?");
			ps.setInt(1, reportId);
			ps.setInt(2, prescribedId);
			rs = ps.executeQuery();
			if (!rs.next()) return true; // not found signatures, hence nothing there to delete.

			ps = con.prepareStatement("DELETE FROM test_visit_report_signatures WHERE report_id=? AND prescribed_id=?");
			ps.setInt(1, reportId);
			ps.setInt(2, prescribedId);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
	}
}
