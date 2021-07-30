package com.insta.hms.diagnosticmodule.radiology;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.TestConducted;
import com.insta.hms.diagnosticmodule.common.TestDetails;
import com.insta.hms.diagnosticmodule.common.TestPrescribed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class RadiologyDAO {

	public static  Logger logger = LoggerFactory.getLogger(RadiologyDAO.class);




	private static final String GET_DEPT_IDS = "SELECT ddept_id FROM diagnostics_departments WHERE category='DEP_RAD'";
	public static ArrayList<String> getAllRadiologyDeptsIds()throws SQLException{
		ArrayList<String> al = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_DEPT_IDS);
		al = DataBaseUtil.queryToOnlyArrayList(ps);
		ps.close();
		con.close();
		return al;
	}


	private static final String TEST_DETAILS = "select distinct tp.mr_no,tp.pat_id,tp.test_id,tp.prescribed_id,tp.pres_doctor," +
			" tp.conducted,tp.confirm_status,d.test_name,dd.ddept_name,dd.ddept_id,d.sample_needed,d.conduction_format," +
			" d.format_name as formatid, tf.format_name, tc.labno,tc.remarks,tc.conducted_by, " +
			" tp.report_id,tvr.report_name" +
			" FROM tests_prescribed tp join diagnostics d on d.test_id = tp.test_id and d.house_status = 'I'" +
			" join diagnostics_departments dd on d.ddept_id = dd.ddept_id and dd.category='DEP_RAD'" +
			" left join test_format tf on d.format_name = tf.testformat_id " +
			" left join tests_conducted tc on tc.prescribed_id = tp.prescribed_id" +
			" left join test_visit_reports tvr on tp.pat_id = tvr.patient_id and tp.report_id = tvr.report_id" +
			" where tp.mr_no=? and tp.pat_id=? and tp.confirm_status!='Y' and tp.conducted !='Cancel' ";

	public static List<Hashtable<String, String>> getTestDetails(String mrno, String visitId)throws SQLException {

			PreparedStatement ps = null;
			Connection con = null;
			List<Hashtable<String, String>>l = null;

			con = DataBaseUtil.getReadOnlyConnection();
			logger.debug(TEST_DETAILS);
			ps = con.prepareStatement(TEST_DETAILS);

			ps.setString(1, mrno);
			ps.setString(2, visitId);
			l = DataBaseUtil.queryToArrayList(ps);

			ps.close();
			con.close();

		return l;
	}


	private static final String RADIOLOGISTS = "select d.Doctor_name, d.doctor_id from doctors d, department dept where " +
	"d.dept_id = dept.dept_id and dept.dept_id like 'DEP_RAD'";


	public static List radiologist()throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		List l;

		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(RADIOLOGISTS);
		l = DataBaseUtil.queryToArrayList(ps);

		ps.close();
		con.close();
		return l;

	}

	private static final String UPDATE_TEST_PRESCRIPTION ="UPDATE tests_prescribed SET conducted=?,report_id=?,  " +
			" labno=?, user_name=?  WHERE prescribed_id=?";
	public static boolean  updateTestPrescription(Connection con, TestPrescribed tp)
	throws SQLException,IOException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(UPDATE_TEST_PRESCRIPTION);
			ps.setString(1, tp.getTestconductedFlag());
			ps.setInt(2, tp.getReportId());
			ps.setString(3, tp.getLabno());
			ps.setString(4, tp.getUserName());
			ps.setInt(5, tp.getPrescribedId());

			status = ps.executeUpdate() > 0 ;

		}finally{
			ps.close();
		}

		return status;
	}



	private static String CHECK_TEST_CONDUCTION = "SELECT count(*) from tests_conducted where prescribed_id = ?";
	private static final String UPDATE_TEST_CONDUCTION = "UPDATE tests_conducted SET " +
			" conducted_date=?,remarks = ?,conducted_by=?, user_name=?, validated_by=?, validated_date=?, " +
			" technician=?,completed_by=?  WHERE prescribed_id=?";

	public static boolean insertOrUpdateTestConduction(Connection con,TestConducted tc)throws SQLException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(CHECK_TEST_CONDUCTION);
			ps.setInt(1, tc.getPrescribedId());
			String count = DataBaseUtil.getStringValueFromDb(ps);
			ps.close();

			ps = con.prepareStatement(UPDATE_TEST_CONDUCTION);
			if(count.equals("0")){
				status = DiagnosticsDAO.insertTestConductionDetatils(con,tc);
			}else{
				ps.setTimestamp(1, tc.getConductedDate());
				ps.setString(2, tc.getRemarks());
				ps.setString(3, tc.getConductedBy());
				ps.setString(4, tc.getUserName());
				ps.setString(5, tc.getValidatedBy());
				ps.setDate(6, tc.getValidatedDate());
				ps.setString(7, tc.getTechnician());
				ps.setString(8, tc.getCompletedBy());

				ps.setInt(9, tc.getPrescribedId());
				int i = ps.executeUpdate();
				if(i>0){
					status = true;
			}
		}
		}finally{
			if(ps!=null)ps.close();
		}
		return status;
	}


	/*
	 * Called only for value tests. If it is a report format, DiagnosticsDAO.updateReportFormat is the
	 * only one that is used.
	 */
	private static final String CHECK_NUM_RESULTS = "SELECT count(*) FROM test_details " +
		" WHERE prescribed_id=? AND resultlabel=?";

	private static final String CHECK_NUM_RESULTS_FORMAT = "SELECT count(*) FROM test_details " +
		" WHERE prescribed_id=?";

	private static final String UPDATE_TEST_DETAILS =
		" UPDATE test_details SET report_value=?,comments=?, " +
		" units=?,reference_range=?,withinnormal=?,user_name=?,result_disclaimer=?  " +
		" ,amendment_reason = ?,test_detail_status =?,"+
		" original_test_details_id = ? ,revised_test_details_id = ?, calculated= ? " +
		" WHERE test_details_id = ? ";

	private static final String UPDATE_TEST_DETAILS_FORMAT =
		" UPDATE test_details SET report_value=?,comments=?, " +
		" units=?,reference_range=?,withinnormal=?,user_name=?," +
		" result_disclaimer=?,amendment_reason = ?,test_detail_status = ?," +
		" original_test_details_id = ? ,revised_test_details_id = ?, calculated= ? " +
		" WHERE test_details_id = ? ";

	public static boolean insertTestResults(Connection con,TestDetails td)throws SQLException{
		boolean status  = false;

		boolean reportFormat = td.getConductedInFormat().equals("Y");

		PreparedStatement rps = reportFormat ?
			con.prepareStatement(UPDATE_TEST_DETAILS_FORMAT) : con.prepareStatement(UPDATE_TEST_DETAILS);

		boolean exists = istestDetailsExists(td.getTestId(),td.getPrescribedId());
		if(td.getTestDetailsId() == 0 && (reportFormat ? !exists : true)){
			status = DiagnosticsDAO.insertTestResults(con,td);
		} else {
			rps.setString(1, td.getResultValue());
			rps.setString(2, td.getRemarks());
			rps.setString(3, td.getUnits());
			rps.setString(4, td.getReferenceRange());
			rps.setString(5, td.getWithInNormal());
			rps.setString(6, td.getUserName());
			rps.setString(7, td.getResultDisclaimer());
			rps.setString(8, td.getAmendReason());
			rps.setString(9, td.getTestDetailStatus());
			rps.setInt(10, td.getOrginalTestDetailsId());
			rps.setInt(11, td.getRevisedTestDetailsId());
			rps.setString(12, td.getResultExpressionCheck());

			if(td.getTestDetailsId() == 0 && exists && reportFormat) {
				int testDetailId = getTestDetailId(td.getTestId(),td.getPrescribedId());
				rps.setInt(13, testDetailId);
			} else {
				rps.setInt(13, td.getTestDetailsId());
			}

			status =  rps.executeUpdate() > 0;
		}
		return status;
	}

	private static final String IS_TEST_DETAILS_EXISITS = "select * from test_details where " +
	" test_detail_status not in ('A','S') and test_id=? and prescribed_id=?";

	public static boolean istestDetailsExists(String testId, int prescId) throws SQLException{
		boolean exists = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(IS_TEST_DETAILS_EXISITS);
			ps.setString(1, testId);
			ps.setInt(2, prescId);
			rs = ps.executeQuery();
			if(rs.next())
				exists = true;
			return exists;
		}finally{
			if(rs!=null) rs.close();
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_TEST_DETAIL_ID = "select test_details_id from test_details where "+
	" test_detail_status not in ('A','S') and test_id=? and prescribed_id=?";

	public static int getTestDetailId(String testId, int prescId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TEST_DETAIL_ID);
			ps.setString(1, testId);
			ps.setInt(2, prescId);
			return DataBaseUtil.getIntValueFromDb(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}

