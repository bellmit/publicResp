package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.common.SplitSearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sirisha.rachkonda
 *
 */
public class OTServicesDAO {

	static Logger logger = LoggerFactory.getLogger(OTServicesDAO.class);

	// search query includes only the filterable fields.
	private static final String OPERATION_SEARCH_QUERY =
		" SELECT op.mr_no,op.patient_id, opm.operation_name as operation, op.start_datetime, op.prescribed_date, " +
		"  op.department AS dept_id, pr.visit_type, op.status, op.prescribed_id, pr.center_id " +
		" FROM bed_operation_schedule op " +
		"  JOIN operation_master opm on (opm.op_id = op.operation_name) " +
		"  JOIN patient_registration pr on op.patient_id = pr.patient_id " +
		"  JOIN patient_details pd on (op.mr_no = pd.mr_no AND " +
        "  patient_confidentiality_check(pd.patient_group,pd.mr_no))";
		 

	// fields query includes all display fields.
	private static final String OPERATION_FIELDS_QUERY =
		" SELECT op.mr_no, op.patient_id, op.consultant_doctor, op.theatre_name, op.status, " +
		"  op.start_datetime, op.package_name, op.finalization_status, op.po_schedule_status, op.remarks, " +
		"  op.prescribed_id, op.prescribed_id as prescription_id, op.end_datetime, op.hrly, " +
		"  op.surgeon, op.anaesthetist, opm.operation_name as operation, " +
		"  op.frompackage, op.common_order_id, op.prescribed_date, op.stock_reduced, " +
		"  op.package_ref, op.department AS dept_id, op.operation_name AS operation_id, " +
		"  b.bill_type, b.status AS bill_status, b.payment_status, " +
		"  get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) " +
		"    AS patient_name, b.visit_type, " +
		"  (SELECT coalesce(count(doc_id), 0) FROM operation_documents od " +
		"    WHERE od.prescription_id=op.prescribed_id) as doc_count " +
		" FROM bed_operation_schedule op " +
		"  JOIN operation_master opm on (opm.op_id = op.operation_name) " +
		"  JOIN department d USING(dept_id) " +
		"  JOIN patient_details pd on op.mr_no = pd.mr_no " +
		"  LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) " +
		"  LEFT JOIN bill_activity_charge bac ON " +
		"    (bac.activity_id=op.prescribed_id::varchar) AND bac.activity_code = 'OPE' " +
		"  LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id " +
		"  LEFT JOIN bill b on b.bill_no=bc.bill_no ";

	/*
	 * pendingList : true retrieves the pending operations
	 * 				 false retrieves the conduction completed and partially completed operations.
	 */
	public static PagedList searchPendingOperations(Map reqParams, Map listingParams, boolean pendingList)
		throws SQLException, ParseException {

		Connection con = DataBaseUtil.getConnection();
		SplitSearchQueryBuilder sb = null;
		try {
			String CONDUCTION_STATUS = "";
			if (pendingList) {
				CONDUCTION_STATUS = "  WHERE status in ('N', 'P') ";
			} else {
				CONDUCTION_STATUS = " WHERE status in ('P', 'C')";
			}
			sb = new SplitSearchQueryBuilder(con, OPERATION_SEARCH_QUERY, OPERATION_FIELDS_QUERY,
					CONDUCTION_STATUS, "prescribed_id", listingParams);
			sb.addFilterFromParamMap(reqParams);
			if (RequestContext.getCenterId() != 0 ) {
				sb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", RequestContext.getCenterId());
			}
			sb.addSecondarySort("prescribed_id");
			sb.build();

			return sb.getMappedPagedList();
		} finally {
			if (sb != null) sb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final BasicDynaBean getOPDetails(int prescribedId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(OPERATIONS_PRESCRIBED_QUERY + " WHERE prescribed_id=? ");
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String OPERATIONS_PRESCRIBED_QUERY = "SELECT bos.mr_no, bos.patient_id, (doc.doctor_name) AS surgeon, " +
			"(doctor.doctor_name) AS anaesthetist,d.dept_name,om.operation_name,om.op_id,tm.theatre_name, " +
			"TO_CHAR(bos.start_datetime,'hh24:mi') AS st_time, TO_CHAR(bos.end_datetime,'HH24:MI') AS end_time," +
			"TO_CHAR(bos.start_datetime,'DD-MM-YYYY') AS startdate,TO_CHAR(bos.end_datetime,'DD-MM-YYYY') AS enddate, " +
			"bos.remarks,bos.prescribed_id,bos.hrly,bos.status,bos.surgeon AS surgeon_id ," +
			"bos.anaesthetist AS anaesthetist_id,bos.theatre_name AS theatre_id, bos.frompackage  " +
			"FROM bed_operation_schedule bos " +
			"JOIN patient_details pd on (pd.mr_no = bos.mr_no AND " +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			"LEFT OUTER JOIN doctors doc ON doc.doctor_id=bos.surgeon " +
			"LEFT OUTER JOIN doctors doctor ON doctor.doctor_id=bos.anaesthetist  " +
			"LEFT OUTER JOIN department d ON d.dept_id=bos.department " +
			"LEFT OUTER JOIN theatre_master tm ON bos.theatre_name=tm.theatre_id " +
			"LEFT OUTER JOIN operation_master om ON bos.operation_name=om.op_id " ;

	/**
	 * Gives a list of scheduled operation
	 * @param patid
	 * @param prescribedIds
	 * @return
	 * @throws SQLException
	 */
	public BasicDynaBean getPrescribedOperation(int prescribedId)throws SQLException{
		Connection con = null;
		PreparedStatement ps =null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(OPERATIONS_PRESCRIBED_QUERY + " WHERE bos.prescribed_id=?");
			ps.setInt(1, prescribedId);

			return DataBaseUtil.queryToDynaBean(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
    }


	public static final String OPERATION_LIST =
			" SELECT op.patient_id, op.mr_no, opm.operation_name, opm.op_id, op.prescribed_id, op.status as conducted_status " +
			" FROM bed_operation_schedule op" +
			" JOIN patient_details pd on (op.mr_no = pd.mr_no AND " +
			"  patient_confidentiality_check(pd.patient_group,pd.mr_no))" +
			" JOIN operation_master opm on (opm.op_id = op.operation_name)" ;
	/**
	 * Gives all active operations list of the visit
	 * @param patient_id
	 * @return
	 * @throws Exception
	 */
	public List<BasicDynaBean> operationsList(String patient_id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(OPERATION_LIST+" where patient_id = ? and op.status != 'X' ");
			ps.setString(1, patient_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/*
	 * conducted : null gets all operations.
	 * 			 : false gets not completed operations
	 * 			 : true gets completed operations.
	 */
	public List<BasicDynaBean> operationsList(String mrNo, Boolean conducted) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			String conductedStatus = (conducted == null ? "" : (conducted ? " AND op.status = 'C'" : " AND op.status != 'C'"));
			ps = con.prepareStatement(OPERATION_LIST+" where op.mr_no = ? and op.status != 'X' " + conductedStatus);
			ps.setString(1, mrNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getOperation(int prescribedId)throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(OPERATION_LIST + " where prescribed_id = ?");
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

 	public static final String ORDERS_CONNECTED_TO_AN_OPERATION =
 		"select *  from (select 'SERSNP' as charge_head ,prescription_id as  prescribed_id,operation_ref,charge_id from services_prescribed  " +
 		"join bill_activity_charge bac on(activity_id = prescription_id::text and activity_code = 'SER')" +
 		" UNION ALL" +
 		" select 'EQOPE' as charge_head,BED_PRESCRIBED_EQUIP_ID as prescribed_id ,operation_ref,charge_id from patient_bed_eqipmentcharges" +
 		" join bill_activity_charge bac on(activity_id = BED_PRESCRIBED_EQUIP_ID::text and activity_code = 'EQO')" +
 		" UNION ALL" +
 		" select service_group as charge_head,prescribed_id,operation_ref,charge_id from other_services_prescribed" +
 		" join bill_activity_charge bac on(activity_id = prescribed_id::text and activity_code ='OTC') " +
 		" UNION ALL " +
 		" select head as charge_head ,consultation_id as prescribed_id,operation_ref,charge_id from DOCTOR_CONSULTATION" +
 		" join bill_activity_charge bac on(activity_id = consultation_id::text and activity_code ='DOC')" +
		" ) " +
 		" as operationOrders where operation_ref=?";

 	/**
 	 * Gives a list of all orders made for a perticuler operation
 	 * @param operation_ref
 	 * @return
 	 * @throws Exception
 	 */
 	public List<BasicDynaBean> getOredersConnectedToOperation(int operation_ref)throws Exception{
 		Connection con = null;
 		PreparedStatement ps = null;
 		try{
 			con = DataBaseUtil.getConnection();
 			ps = con.prepareStatement(ORDERS_CONNECTED_TO_AN_OPERATION);
 			ps.setInt(1, operation_ref);
 			return DataBaseUtil.queryToDynaList(ps);
 		}finally{
 			DataBaseUtil.closeConnections(con, ps);
 		}
 	}
 	public static final String OT_CONSUMABLES = "SELECT opm.operation_name as consumer_name,otc.operation_id as consumer_id," +
		"otc.consumable_id as item_id," +
		" (case when otc.status='A' then 'true' else 'false' end ) as status,i.medicine_name AS item_name,otc.qty_needed as qty," +
		" '' as usage_no, 0 as ref_no FROM  ot_consumables otc" +
		" join operation_master opm on(opm.op_id = otc.operation_id)" +
		" join store_item_details i on(otc.consumable_id = i.medicine_id) WHERE  otc.operation_id = ?";
 	/**
 	 * Gives a list of all consumables and operation mapped
 	 * @param operation_id
 	 * @return
 	 * @throws SQLException
 	 */
	public List getOTConsumables(String operation_id)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(OT_CONSUMABLES);
			ps.setString(1, operation_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String OT_CONSUMABLES_USED =
		"SELECT consumable_id as item_id, prescription_id as ref_no, operation_id, usage_no, qty, " +
		"i.medicine_name AS item_name,'true' as status, op.operation_name " +
			" FROM ot_consumable_usage  ocu " +
			" JOIN store_item_details i ON(i.medicine_id = ocu.consumable_id) " +
			" JOIN operation_master op ON ocu.operation_id = op.op_id" +
			" WHERE prescription_id = ? AND operation_id = ? AND operation_type = ?";

	public static List<BasicDynaBean> getOTConsumablesUsed(int prescriptionId,String operationId, String operationType) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(OT_CONSUMABLES_USED);
			ps.setInt(1, prescriptionId);
			ps.setString(2, operationId);
			ps.setString(3, operationType);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getOperationsWithConsumables(String opId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT operation_id FROM ot_consumables  WHERE operation_id=?");
			ps.setString(1, opId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String GET_ALL_OPERATIONS ="SELECT operation_name,op_id FROM operation_master" ;
	public static List getOperations() throws SQLException,IOException {
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(GET_ALL_OPERATIONS));
	}

	private static final String GET_PATIENT_ISSUE_FIELDS = " SELECT * ";

	private static final String COUNT = "SELECT count(*) ";

	private static final String TABLES = " FROM (SELECT sid.medicine_id,medicine_name,round(sum(qty-return_qty),2) as qty," +
		" sim.issued_to "+
		" FROM stock_issue_main sim "+
		" JOIN stock_issue_details sid ON(sim.user_issue_no = sid.user_issue_no) "+
		" JOIN store_item_details sitd ON(sitd.medicine_id = sid.medicine_id) "+
		" LEFT JOIN patient_registration pr ON (pr.patient_id = sim.issued_to) " +
		" LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no)" +
		" WHERE (qty-return_qty) > 0 AND patient_confidentiality_check(pd.patient_group,pd.mr_no)"+
		" GROUP BY sid.medicine_id,medicine_name,sim.issued_to) AS foo ";

	public static PagedList getPatientIssueDetails(String patientId, String pageNumParam, String pageSizeParam) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		SearchQueryBuilder qb = null;
		try {
			int pageNum = 0,noOfRecord=0;
			int pageSize = 20;
			if (pageSizeParam != null && !pageSizeParam.equals(""))
				pageSize = Integer.parseInt(pageSizeParam);
			if (pageNumParam != null && !pageNumParam.equals("")) {
				pageNum = Integer.parseInt(pageNumParam);
			} else {
				ps = con.prepareStatement(COUNT + TABLES + " WHERE (foo.issued_to  = ?)");
				ps.setString(1, patientId);
				try (ResultSet rs = ps.executeQuery();) {
  				if(rs.next())
  					noOfRecord = rs.getInt(1);
				}

				int mod = noOfRecord % pageSize;
				if (mod == 0) {
					pageNum =  noOfRecord/pageSize;
				} else {
					pageNum =  noOfRecord/pageSize + 1;
				}
			}
			qb = new SearchQueryBuilder(con, GET_PATIENT_ISSUE_FIELDS, COUNT, TABLES, null, "foo.medicine_name", false, pageSize, pageNum);
			qb.addFilter(SearchQueryBuilder.STRING, "foo.issued_to", "=", patientId);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String PLANNED_OPERATION_SEARCH_QUERY =
		" SELECT od.mr_no,od.patient_id, opm.operation_name as operation, sa.appointment_time AS operation_scheduled_time, " +
		"  pr.visit_type, op.prescribed_id,tm.theatre_name,od.operation_details_id,tm.theatre_id,od.operation_status,od.added_to_bill,pr.center_id " +
		" FROM operation_details od " +
		"  LEFT JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND op.oper_priority = 'P') &" +
		"  LEFT JOIN theatre_master tm ON(od.theatre_id = tm.theatre_id )" +
		"  LEFT JOIN scheduler_appointments sa ON(sa.appointment_id = od.appointment_id) " +
		"  LEFT JOIN operation_master opm on (opm.op_id = op.operation_id) " +
		"  JOIN patient_registration pr on od.patient_id = pr.patient_id ";

	// fields query includes all display fields.
	private static final String PLANNED_OPERATION_FIELDS_QUERY =
		" SELECT od.mr_no, od.patient_id, " +
		"  op.prescribed_id, op.prescribed_id as prescription_id, od.surgery_start," +
		"  opm.operation_name as operation,sa.appointment_time AS operation_scheduled_time, " +
		"  get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name) " +
		"    AS patient_name,tm.theatre_name,od.operation_details_id,tm.theatre_id,od.operation_status,foo.primary_surgeon_name," +
		"  od.surgery_start,od.surgery_end,od.conduction_remarks,od.cancel_reason,od.added_to_bill"+
		" FROM operation_details od " +
		"  LEFT JOIN scheduler_appointments sa USING(appointment_id) &" +
		"  LEFT JOIN theatre_master tm ON(od.theatre_id = tm.theatre_id )" +
		"  LEFT JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND op.oper_priority = 'P')" +
		"  LEFT JOIN operation_master opm on (opm.op_id = op.operation_id) " +
		"  LEFT JOIN " +
		"		(select textcat_commacat(doctor_name) as primary_surgeon_name,ot.operation_details_id " +
		"			FROM operation_team ot " +
		"			JOIN doctors d ON(d.doctor_id = ot.resource_id) " +
		"			WHERE ot.operation_speciality = 'SU' group by operation_details_id" +
		"		) AS foo ON(od.operation_details_id = foo.operation_details_id)" +
		"  JOIN patient_details pd on (od.mr_no = pd.mr_no AND " +
		"   ( patient_confidentiality_check(pd.patient_group,pd.mr_no) ))" +
		"  LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) ";


	public static PagedList searchPatientPlannedOperations(Map reqParams, Map listingParams,String userName, int roleId)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SplitSearchQueryBuilder sb = null;
		String PLANNED_OPERATION_FIELDS_QUERY_REPLACED = PLANNED_OPERATION_FIELDS_QUERY;
		String PLANNED_OPERATION_SEARCH_QUERY_REPLACED = PLANNED_OPERATION_SEARCH_QUERY;
    String JoinQuery = "Join user_theatres ut ON((od.theatre_id = ut.theatre_id or od.theatre_id is NULL) and ut.emp_username = '##userName##')";
		try {
			String CONDUCTION_STATUS = null;
			String[] billingStatus = (String[])reqParams.get("_billing_status");
			if ((roleId != 1) && (roleId != 2)) {
			  PLANNED_OPERATION_FIELDS_QUERY_REPLACED = PLANNED_OPERATION_FIELDS_QUERY_REPLACED.replace("&", JoinQuery);
			  PLANNED_OPERATION_FIELDS_QUERY_REPLACED = PLANNED_OPERATION_FIELDS_QUERY_REPLACED.replace("##userName##", userName);
			  PLANNED_OPERATION_SEARCH_QUERY_REPLACED = PLANNED_OPERATION_SEARCH_QUERY_REPLACED.replace("&", JoinQuery);
			  PLANNED_OPERATION_SEARCH_QUERY_REPLACED = PLANNED_OPERATION_SEARCH_QUERY_REPLACED.replace("##userName##", userName);
      } else{
        PLANNED_OPERATION_FIELDS_QUERY_REPLACED = PLANNED_OPERATION_FIELDS_QUERY_REPLACED.replace("&", "");
        PLANNED_OPERATION_SEARCH_QUERY_REPLACED = PLANNED_OPERATION_SEARCH_QUERY_REPLACED.replace("&", "");
      }
			if(billingStatus != null && billingStatus.length > 0) {
				boolean whereClauseAdded = false;
				for(int i=0;i<billingStatus.length;i++) {
					if(billingStatus[i].equals("B")) {
						if(!whereClauseAdded) {
							CONDUCTION_STATUS = "WHERE ( added_to_bill = 'Y'";
							whereClauseAdded = true;
						} else {
							CONDUCTION_STATUS = CONDUCTION_STATUS+" OR added_to_bill = 'Y' ";
						}
					} else if (billingStatus[i].equals("NB")) {
						if(!whereClauseAdded) {
							CONDUCTION_STATUS = "WHERE ( added_to_bill = 'N'";
							whereClauseAdded = true;
						} else {
							CONDUCTION_STATUS = CONDUCTION_STATUS+ " OR added_to_bill = 'N' ";
						}
					}
				}
				CONDUCTION_STATUS = CONDUCTION_STATUS != null ? CONDUCTION_STATUS+" )" : CONDUCTION_STATUS;
			}
			sb = new SplitSearchQueryBuilder(con, PLANNED_OPERATION_SEARCH_QUERY_REPLACED, PLANNED_OPERATION_FIELDS_QUERY_REPLACED,
					CONDUCTION_STATUS, "od.operation_details_id", listingParams);

			sb.addFilterFromParamMap(reqParams);
			if (RequestContext.getCenterId() != 0 ) {
				sb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", RequestContext.getCenterId());
			}
			sb.addSecondarySort("operation_details_id");
			sb.build();

			return sb.getMappedPagedList();
		} finally {
			if (sb != null) sb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String EQUIP_COUNT = "SELECT COUNT(*)";
	private static final String EQUIP_FIELDS = "SELECT * ";
	private static final String EQUIP_TABLES = "FROM ( "+
		" SELECT ep.eq_id,em.equipment_name,patient_id, to_char(used_from,'dd-MM-YYYY hh:mi AM') as used_from ," +
		" to_char(used_till,'dd-MM-YYYY hh:mi AM') as used_till, ep.mr_no " +
		" FROM equipment_prescribed ep " +
		" JOIN patient_details pd ON (pd.mr_no = ep.mr_no AND " +
		" patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
		" JOIN equipment_master em ON(ep.eq_id = em.eq_id) " +
		" WHERE ep.cancel_status != 'C') AS foo";

	public static PagedList getEquipDetails(String patientId, String pageNumParam, String pageSizeParam) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		SearchQueryBuilder qb = null;
		try {
			int pageNum = 0,noOfRecord=0;
			int pageSize = 20;
			if (pageSizeParam != null && !pageSizeParam.equals(""))
				pageSize = Integer.parseInt(pageSizeParam);
			if (pageNumParam != null && !pageNumParam.equals("")) {
				pageNum = Integer.parseInt(pageNumParam);
			} else {
				ps = con.prepareStatement(EQUIP_COUNT + EQUIP_TABLES + " WHERE (foo.patient_id  = ?)");
				ps.setString(1, patientId);
				try (ResultSet rs = ps.executeQuery();) {
  				if(rs.next())
  					noOfRecord = rs.getInt(1);
				}

				int mod = noOfRecord % pageSize;
				if (mod == 0) {
					pageNum =  noOfRecord/pageSize;
				} else {
					pageNum =  noOfRecord/pageSize + 1;
				}
			}
			qb = new SearchQueryBuilder(con, EQUIP_FIELDS, EQUIP_COUNT, EQUIP_TABLES, null, "foo.equipment_name", false, pageSize, pageNum);
			qb.addFilter(SearchQueryBuilder.STRING, "foo.patient_id", "=", patientId);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
