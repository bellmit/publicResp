package com.insta.hms.OTServices.OtRecord;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.SplitSearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
/**
 * @author nikunj.s
 *
 */
public class OtRecordDAO {

	public OtRecordDAO() {
	}

	public static final String FIND_COMPLAINT =
		" SELECT pr.complaint,* FROM patient_registration pr " +
		" JOIN patient_details pd ON (pd.mr_no = pr.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
		" WHERE patient_id = ? ";

	public BasicDynaBean findComplaint(String patientId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(FIND_COMPLAINT);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OT_RECORD_FORMS_FIELDS =
		" SELECT operation_proc_id, patient_id, operation_details_id FROM patient_section_mini_window_view " +
		"	group by patient_id, operation_details_id, operation_proc_id" ;
	private static final String SECTION_DETAILS = "SELECT * FROM patient_section_mini_window_view ";

	public static PagedList getOtRecordForms(String patientId, String opDetailsId, Map pagingParams) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		SearchQueryBuilder qb = null;
		try {
			pagingParams.put(LISTING.PAGESIZE, 5);
			qb = new SplitSearchQueryBuilder(con, GET_OT_RECORD_FORMS_FIELDS, SECTION_DETAILS, null,
					"operation_proc_id", pagingParams);
			//qb = new SearchQueryBuilder(con, GET_OT_RECORD_FORMS_FIELDS, COUNT, TABLES, pagingParams);
			qb.addFilter(SearchQueryBuilder.STRING, "patient_id", "=", patientId);
			qb.addFilter(SearchQueryBuilder.INTEGER, "operation_details_id", "=", Integer.parseInt(opDetailsId));
			qb.addSecondarySort("operation_proc_id");
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_OPERATIONS_IDS =
		" SELECT ops.operation_id, ops.operation_proc_id, om.dept_id, om.operation_name " +
		" FROM operation_details ods " +
		" JOIN patient_details pd ON (pd.mr_no = ods.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
		" JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id) " +
		" JOIN operation_master om ON (om.op_id=ops.operation_id) " +
		" WHERE ods.patient_id = ? AND ods.operation_details_id = ? order by ops.operation_proc_id";

	public List getOperations(String patientId , int opDetailsId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_OPERATIONS_IDS);
			ps.setString(1, patientId);
			ps.setInt(2, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_OPERATION =
		" SELECT ops.operation_id, ops.operation_proc_id, om.dept_id, ops.operation_details_id, om.operation_name " +
		" FROM operation_details ods " +
		" JOIN patient_details pd ON (pd.mr_no = ods.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
		" 	JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id) " +
		"	JOIN operation_master om ON (om.op_id=ops.operation_id) " +
		" WHERE ops.operation_proc_id = ? ";

	public static BasicDynaBean getOperation(int operation_proc_id) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_OPERATION);
			ps.setInt(1, operation_proc_id);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String EMR_OT_RECORD =
		" SELECT ods.*, om.operation_name, d.doctor_name,bos.prescribed_date,pr.reg_date " +
		" FROM operation_details ods " +
		" JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id) " +
		" LEFT JOIN bed_operation_schedule bos ON(bos.prescribed_id=ops.prescribed_id AND ops.oper_priority='P')" +
		" JOIN operation_master om ON (om.op_id=ops.operation_id) " +
		" LEFT JOIN doctors d ON (ods.prescribing_doctor=d.doctor_id) " +
		" JOIN patient_registration pr ON(ods.patient_id = pr.patient_id)"+
		" WHERE operation_status = 'C' AND ops.oper_priority='P' ";

	public static List getAllCompletedOperationsOtRecord(String visitId, String mrNo, boolean allVisitsDocs) throws SQLException , IOException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			if (allVisitsDocs) {
				ps = con.prepareStatement(EMR_OT_RECORD + " AND pr.mr_no = ? ");
				ps.setString(1, mrNo);
			} else {
				ps = con.prepareStatement(EMR_OT_RECORD + " AND pr.patient_id=?  ");
				ps.setString(1, visitId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
