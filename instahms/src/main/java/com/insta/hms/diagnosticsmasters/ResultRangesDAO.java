package com.insta.hms.diagnosticsmasters;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultRangesDAO extends GenericDAO {
	public ResultRangesDAO(){
		super("test_result_ranges");
	}
	private static final String exactresultrange =
		" SELECT * FROM test_result_ranges where resultlabel_id = ? AND  " +
		" ( range_for_all = 'N'  AND " +
		" ( (min_patient_age IS NULL OR (min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )::integer <= " +
		"		( SELECT (?::date - ?::date)::integer  ) )" +
		"  AND" +
		"   (max_patient_age IS NULL OR (  SELECT (?::date - ?::date)::integer " +
		"			) <= " +
		"			(max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )::integer )) " +
		" AND (patient_gender = ? OR patient_gender = 'N')) ORDER BY priority LIMIT 1 ";

	private static final String inpatient_exactresultrange =
		" SELECT * FROM test_result_ranges where resultlabel_id = ? AND  " +
		" ( range_for_all = 'N'  AND " +
		" ((min_patient_age IS NULL OR (min_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end))::integer <= " +
		"		 ? ::integer) " +
		"  AND  " +
		" (max_patient_age IS NULL OR  " +
		"		 ? ::integer " +
		"		<= ( max_patient_age*(CASE WHEN age_unit = 'Y' THEN 365.25 ELSE 1 end) )::integer  )) " +
		"  AND (patient_gender = ? OR patient_gender = 'N') ) ORDER BY priority LIMIT 1";

	private static String result_all = " SELECT * FROM test_result_ranges where resultlabel_id = ? AND  " +
		" range_for_all = 'Y' ORDER BY priority limit 1";


	public static BasicDynaBean getResultRange(HashMap<String, Object> map,Map<String, Object> pd)throws SQLException, ParseException{
		Connection con = null;
		PreparedStatement ps = null;
		Date placeDateHolder = (Date) pd.get("expected_dob");
		
		BasicDynaBean resultRangeBean = null;
		try{
			con = DataBaseUtil.getConnection();
			if (placeDateHolder == null) {
				BigDecimal incomingDateHolder =  (BigDecimal) pd.get("patient_age_days");
				ps = con.prepareStatement(inpatient_exactresultrange);
				ps.setInt(1, Integer.parseInt(map.get("resultlabel_id").toString()));
				ps.setBigDecimal(2, incomingDateHolder);
				ps.setBigDecimal(3, incomingDateHolder);
				ps.setString(4, (String)pd.get("patient_gender"));
				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			} else {
				ps = con.prepareStatement(exactresultrange );
				java.sql.Date sampleDate = ((map.get("sample_date") != null 
						&& !map.get("sample_date").equals("")) ? DataBaseUtil.parseDate(map.get("sample_date").toString()) : DateUtil.getCurrentDate());
				ps.setInt(1, Integer.parseInt(map.get("resultlabel_id").toString()));
				ps.setDate(2, sampleDate);
				ps.setDate(3, placeDateHolder);
				ps.setDate(4, sampleDate);
				ps.setDate(5, placeDateHolder);
				ps.setString(6, (String)pd.get("patient_gender"));
				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			}

			if( resultRangeBean == null ){
				ps = con.prepareStatement(result_all);
				ps.setInt(1, Integer.parseInt(map.get("resultlabel_id").toString()));

				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			}

			return resultRangeBean;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static BasicDynaBean getResultRange(Connection con, HashMap<String, Object> map,Map<String, Object> pd)throws SQLException, ParseException{
		PreparedStatement ps = null;
		Date placeDateHolder = (Date) pd.get("expected_dob");
		
		BasicDynaBean resultRangeBean = null;
		try{
			con = DataBaseUtil.getConnection();
			if (placeDateHolder == null) {
				BigDecimal incomingDateHolder =  (BigDecimal) pd.get("patient_age_days");
				ps = con.prepareStatement(inpatient_exactresultrange);
				ps.setInt(1, Integer.parseInt(map.get("resultlabel_id").toString()));
				ps.setBigDecimal(2, incomingDateHolder);
				ps.setBigDecimal(3, incomingDateHolder);
				ps.setString(4, (String)pd.get("patient_gender"));
				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			} else {
				ps = con.prepareStatement(exactresultrange );
				java.sql.Date sampleDate = ((map.get("sample_date") != null 
						&& !map.get("sample_date").equals("")) ? DataBaseUtil.parseDate(map.get("sample_date").toString()) : DateUtil.getCurrentDate());
				ps.setInt(1, Integer.parseInt(map.get("resultlabel_id").toString()));
				ps.setDate(2, sampleDate);
				ps.setDate(3, placeDateHolder);
				ps.setDate(4, sampleDate);
				ps.setDate(5, placeDateHolder);
				ps.setString(6, (String)pd.get("patient_gender"));
				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			}

			if( resultRangeBean == null ){
				ps = con.prepareStatement(result_all);
				ps.setInt(1, Integer.parseInt(map.get("resultlabel_id").toString()));

				resultRangeBean = DataBaseUtil.queryToDynaBean(ps);
			}

			return resultRangeBean;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String all_results_with_ranges =
		"   SELECT DISTINCT(tr.resultlabel_id) FROM test_result_ranges tr    " +
		"   JOIN test_results_master using(resultlabel_id)WHERE test_id = ?  ";

	public static List<BasicDynaBean> listAllTestResultReferences(String test_id)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(all_results_with_ranges);
			ps.setString(1, test_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static final String RESULT_LABELS_FOR_A_TEST = "SELECT resultlabel, resultlabel_id, method_name " +
			" FROM test_results_master trm " +
			" LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id)" +
			" WHERE trm.test_id = ? ORDER BY display_order";


	public static final String CENTER_WISE_RESULT_LABELS_FOR_A_TEST = "SELECT resultlabel, trm.resultlabel_id, method_name " +
			" FROM test_results_master trm " +
			" JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) " +
			" LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id) " +
			" WHERE trm.test_id = ? AND (trc.center_id = 0 OR trc.center_id = ?) AND trc.status='A' ORDER BY display_order";

	public static final String DEFAULT_CENTER_RESULT_LABELS_FOR_A_TEST = "SELECT resultlabel, trm.resultlabel_id, method_name " +
			" FROM test_results_master trm " +
			" JOIN test_results_center trc ON (trc.resultlabel_id = trm.resultlabel_id) " +
			" LEFT JOIN diag_methodology_master dmm ON(dmm.method_id = trm.method_id)" +
			" WHERE trm.test_id = ? AND trc.status='A' " +
			" GROUP BY trm.resultlabel, trm.resultlabel_id, dmm.method_name, trm.display_order ORDER BY display_order";

	public static List<BasicDynaBean> listAllresultlblsForATest(String testId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		int centerId = RequestContext.getCenterId();
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			if (centerId != 0 && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1) {
				pstmt = con.prepareStatement(CENTER_WISE_RESULT_LABELS_FOR_A_TEST);
				pstmt.setString(1, testId);
				pstmt.setInt(2, centerId);
			} else {
				pstmt = con.prepareStatement(DEFAULT_CENTER_RESULT_LABELS_FOR_A_TEST);
				pstmt.setString(1, testId);
			}
			return DataBaseUtil.queryToDynaList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	public static List<BasicDynaBean> resultsforTest(String testId)throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(RESULT_LABELS_FOR_A_TEST);
			pstmt.setString(1, testId);
			return DataBaseUtil.queryToDynaList(pstmt);

		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	public static final String CENTERS_FOR_RESULT_LABELS =
			"	SELECT center_id from test_results_master trm " +
			"	LEFT JOIN test_results_center trc on trc.resultlabel_id = trm.resultlabel_id " +
			"	WHERE trm.test_id =? and trm.resultlabel_id=? ";

		public static List CentersForResults(Connection con, String testId,int resultLabelId)throws SQLException {
			PreparedStatement pstmt = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				pstmt = con.prepareStatement(CENTERS_FOR_RESULT_LABELS);
				pstmt.setString(1, testId);
				pstmt.setInt(2, resultLabelId);
				return DataBaseUtil.queryToStringList(pstmt);

			} finally {
				DataBaseUtil.closeConnections(con, pstmt);
			}
		}

}
