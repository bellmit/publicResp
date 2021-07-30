/**
 * mithun.saha
 */
package com.insta.hms.OTServices;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.instaforms.OTForms;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author mithun.saha
 *
 */
public class OperationDetailsDAO extends GenericDAO{
	public OperationDetailsDAO() {
		super("operation_details");
	}


	private static final String GET_SURGEONS = "SELECT * FROM doctors d "+
			" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
			" WHERE d.status='A' AND d.ot_doctor_flag = 'Y' AND d.dept_id !='DEP0002' #";

	public List<BasicDynaBean> getSurgeons(Integer centerId) throws Exception{
		String query = GET_SURGEONS;
		Boolean isMultiCenter = centerId != 0 
		    && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;
		if (isMultiCenter) {
			query = query.replaceAll("#", " AND dcm.center_id IN( ?,0)");
		} else {
			query = query.replaceAll("#", " ");
		}
		try (Connection con = DataBaseUtil.getConnection();
		    PreparedStatement ps = con.prepareStatement(query)) {
			if (isMultiCenter) {
			  ps.setInt(1, centerId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	private  static final String GET_ANAESTHETISTS = "SELECT * FROM doctors d "+
			" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
			" WHERE d.status='A' AND dcm.status='A' AND d.ot_doctor_flag = 'Y' AND d.dept_id ='DEP0002' #";

	public List<BasicDynaBean> getAnaesthetists(Integer centerId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_ANAESTHETISTS;
		if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1 && centerId != 0) {
			query = query.replaceAll("#", " AND dcm.center_id In( '"+centerId+"'"+",0)");
		} else {
			query = query.replaceAll("#", " ");
		}
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_APPOINTMENT_PROCEDURE_DETAILS = "SELECT res_sch_name as operation_id,sai.resource_id as theatre_id,om.operation_name,appointment_time," +
			"   appointment_time+(duration||' mins')::interval AS end_appointment_time,sa.presc_doc_id as presc_doc_id,sa.* " +
			"	FROM scheduler_appointments sa" +
			
			"   LEFT JOIN scheduler_appointment_items sai ON(sa.appointment_id=sai.appointment_id) " +
			"   LEFT JOIN operation_master om ON(om.op_id = sa.res_sch_name) " +
			"	WHERE sai.appointment_id = ? AND sai.resource_type = 'THID'";

	public static BasicDynaBean getAppointmentProcedureDetails(int appointmentId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_APPOINTMENT_PROCEDURE_DETAILS);
			ps.setInt(1, appointmentId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getAppointmentProcedureDetails(Connection con,int appointmentId) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_APPOINTMENT_PROCEDURE_DETAILS);
			ps.setInt(1, appointmentId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_APPOINTMENT_SURGEON_DETAILS = "SELECT sai.resource_id as resource_id,d.doctor_name as surgeon_name,sai.resource_type " +
			"	FROM scheduler_appointment_items sai " +
			"	LEFT JOIN doctors d ON(d.doctor_id=sai.resource_id)" +
			"	WHERE appointment_id = ? ";

	public static List<BasicDynaBean> getAppointmentResourceDetails(int appointmentId,String[] resourceTypes) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			StringBuilder query = new StringBuilder(GET_APPOINTMENT_SURGEON_DETAILS);
			DataBaseUtil.addWhereFieldInList(query, "sai.resource_type", Arrays.asList(resourceTypes));
			ps = con.prepareStatement(query.toString());
			ps.setInt(index++, appointmentId);
			for(String resourceType : resourceTypes) {
				ps.setString(index++, resourceType);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getAppointmentResourceDetails(Connection con,int appointmentId,String[] resourceTypes) throws Exception{
		PreparedStatement ps = null;
		int index =1;
		try {
			StringBuilder query = new StringBuilder(GET_APPOINTMENT_SURGEON_DETAILS);
			DataBaseUtil.addWhereFieldInList(query, "sai.resource_type", Arrays.asList(resourceTypes));
			ps = con.prepareStatement(query.toString());
			ps.setInt(index++, appointmentId);
			for(String resourceType : resourceTypes) {
				ps.setString(index++, resourceType);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

/*	private static final String GET_APPOINTMENT_ANAESTIATIST_DETAILS = "SELECT sai.resource_id as anestiatist_id,d.doctor_name as  anestiatist_name" +
			"	FROM scheduler_appointment_items sai" +
			"	LEFT JOIN doctors d ON(d.doctor_id=sai.resource_id)" +
			"	WHERE appointment_id = ? AND sai.resource_type = 'ANEDOC'";


	public static List<BasicDynaBean> getAppointmentAnaestiatistDetails(int appointmentId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_APPOINTMENT_ANAESTIATIST_DETAILS);
			ps.setInt(1, appointmentId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getAppointmentAnaestiatistDetails(Connection con,int appointmentId) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_APPOINTMENT_ANAESTIATIST_DETAILS);
			ps.setInt(1, appointmentId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}*/


	private static final String GET_SURGERY_DETAILS = "SELECT * FROM operation_details WHERE patinet_id = ?";
	public static BasicDynaBean getOperationDetails(String patinetId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SURGERY_DETAILS);
			ps.setString(1, patinetId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PROCEDURES = "SELECT od.*,op.*,om.*,coalesce(boss.stock_reduced,bos.stock_reduced) as stock_reduced, " +
			" op.prescribed_id as main_prescribed_id " +
			" FROM operation_details od " +
			" JOIN patient_details pd ON (pd.mr_no = od.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id)" +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id) " +
			" LEFT JOIN bed_operation_schedule boss ON(op.prescribed_id = boss.prescribed_id AND boss.operation_name = op.operation_id AND op.oper_priority='P')" +
			" LEFT JOIN bed_operation_secondary bos ON(op.prescribed_id = bos.sec_prescribed_id AND bos.operation_id = op.operation_id AND op.oper_priority='S')" +
			" WHERE od.operation_details_id = ? ORDER BY op.oper_priority,op.operation_proc_id";

	public static List<BasicDynaBean> getProcedures(Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PROCEDURES);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean> getProcedures(Connection con, Integer opDetailsId) throws Exception{
	  try(PreparedStatement ps = con.prepareStatement(GET_PROCEDURES)) {
	    ps.setInt(1, opDetailsId);
	    return DataBaseUtil.queryToDynaList(ps);
	  }
	}

	private static final  String GET_SURGERY_APPOINTMENTS = "SELECT appointment_id," +
			"	TO_CHAR(appointment_time,'DD-MM-YYYY HH24:MI')||' '||sa.patient_name||' '||coalesce(om.operation_name,'') AS patient_app_date_time_name_text" +
			"	FROM scheduler_appointments sa" +
			"   LEFT JOIN patient_details pd ON (pd.mr_no = sa.mr_no AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no)) " +
			"   LEFT JOIN operation_master om ON(om.op_id=sa.res_sch_name)" +
			"	WHERE res_sch_id = 2 AND appointment_status IN('Booked','Confirmed')" +
			"	AND appointment_id NOT IN(select appointment_id FROM operation_details WHERE mr_no = ? AND appointment_id is not null)" +
			"	AND sa.mr_no = ? #" +
			"   order by appointment_id,appointment_time";

	public List<BasicDynaBean> getSurgeryAppointments(Integer centerId,String mrNo) throws Exception{
		String query = GET_SURGERY_APPOINTMENTS;
		Boolean isMultiCenter = centerId != 0
		    && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;
		if (isMultiCenter) {
			query = query.replaceAll("#", " AND center_id = ?");
		} else {
			query = query.replaceAll("#", " ");
		}
		try (Connection con = DataBaseUtil.getConnection();
		    PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, mrNo);
			ps.setString(2, mrNo);
			if (isMultiCenter) {
			  ps.setInt(3, centerId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	public static final String GET_OPERATION_DETAILS = "SELECT opd.*, op.*, d.*, sa,*, om.*, sai.*, sai1.*, tm.*, doc.*," +
			"	TO_CHAR(appointment_time,'DD-MM-YYY HH24:MI')||' '||sa.patient_name AS patient_app_date_time_name_text," +
			"   TO_CHAR(appointment_time,'DD-MM-YYYY HH24:MI')||' /Theatre:'||coalesce(tm.theatre_name,'None')||' /Surgery:'||coalesce(om.operation_name,'None')||' /Prim Surgeon:'||coalesce(doc.doctor_name,'None') " +
			"	AS patient_app_details_showing_text," +
			"   d.doctor_name" +
			" FROM operation_details opd " +
			" JOIN patient_details pd ON (pd.mr_no = opd.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" LEFT JOIN operation_procedures op ON(op.operation_details_id = opd.operation_details_id AND oper_priority = 'P') " +
			" LEFT JOIN doctors d ON(d.doctor_id = opd.prescribing_doctor)" +
			" LEFT JOIN scheduler_appointments sa ON(sa.appointment_id = opd.appointment_id) " +
			" LEFT JOIN operation_master om ON(om.op_id=sa.res_sch_name)"+
			" LEFT JOIN scheduler_appointment_items sai ON(sa.appointment_id = sai.appointment_id AND sai.resource_type = 'SUDOC') " +
			" LEFT JOIN scheduler_appointment_items sai1 ON(sa.appointment_id = sai1.appointment_id AND sai1.resource_type='THID') " +
			" LEFT JOIN theatre_master tm ON(tm.theatre_id = sai1.resource_id)" +
			" LEFT JOIN doctors doc ON(doc.doctor_id=sai.resource_id)" +
			" WHERE opd.operation_details_id = ? ";

	public static BasicDynaBean getSurgeryDetails(Integer opeDetailsId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_OPERATION_DETAILS);
			ps.setInt(1,opeDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_PATIENT_PLANNED_OPERATIONS =
		" SELECT od.*, op.prescribed_id FROM operation_details od" +
		" JOIN patient_details pd on (pd.mr_no = od.mr_no AND " +
        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
		"	JOIN operation_procedures op on (od.operation_details_id=op.operation_details_id AND op.oper_priority = 'P') where patient_id = ? ";

	public static List<BasicDynaBean> getPatinetOperations(String patientId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PATIENT_PLANNED_OPERATIONS);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PROCEDURE_RESOURCES = "SELECT * FROM operation_team ot " +
			" LEFT JOIN doctors d ON(d.doctor_id=ot.resource_id)" +
			" WHERE operation_details_id = ? ";

	public static List<BasicDynaBean> getProcedureResources(String resourceType,Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			int index = 1;
			String[] resourcetypeArr = new String[]{};
			StringBuilder query =  new StringBuilder(GET_PROCEDURE_RESOURCES);
			if(resourceType.equals("surgeon")) {
				resourcetypeArr = new String[]{"SU","ASU","COSOPE"};
			} else if (resourceType.equals("anestiatist")) {
				resourcetypeArr = new String[]{"AN","ASAN"};
			} else if (resourceType.equals("paediatrician")) {
				resourcetypeArr = new String[]{"PAED"};
			}
			DataBaseUtil.addWhereFieldInList(query, "operation_speciality", Arrays.asList(resourcetypeArr));
			ps = con.prepareStatement(query.toString());
			ps.setInt(index++, opDetailsId);
			for(String spec : resourcetypeArr) {
				ps.setString(index++, spec);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PROCEDURE_ANAESTHESIA_TYPES = "SELECT * FROM operation_anaesthesia_details asad " +
			" LEFT JOIN anesthesia_type_master atm ON(atm.anesthesia_type_id = asad.anesthesia_type)" +
			" WHERE operation_details_id = ? ";

	public static List<BasicDynaBean> getProcedureAnaesthesiaTypes(Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			int index = 1;
			StringBuilder query =  new StringBuilder(GET_PROCEDURE_ANAESTHESIA_TYPES);
			ps = con.prepareStatement(query.toString());
			ps.setInt(index++, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String INSERT_PROCEDURES = "INSERT INTO operation_procedures(operation_proc_id,operation_details_id,oper_priority,operation_id,modifier,prescribed_id,"
	    + "prior_auth_id, prior_auth_mode_id) " +
						" values (?,?,?,?,?,?,?,?)";


	public boolean insertProcedures(List<ProcedureDetails> list,Connection con) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(INSERT_PROCEDURES);) {
  		boolean success = true;
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			ProcedureDetails pdt =  (ProcedureDetails) iterator.next();
  			ps.setInt(1, pdt.getProcedureId());
  			ps.setInt(2, pdt.getOpDetailsId());
  			ps.setString(3, pdt.getOperationPriority());
  			ps.setString(4, pdt.getOperationId());
  			ps.setString(5, pdt.getModifier());
  			ps.setInt(6, pdt.getPrescribedId());
  			ps.setString(7, pdt.getPriorAuthId());
  			ps.setObject(8, pdt.getPriorAuthModeId());
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();

  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
  		return success;
		}
	}


	private static final String DELETE_PROCEDURE = "DELETE FROM operation_procedures WHERE operation_proc_id = ?";

	private static final String DELETE_OT_CONSUMABLES = "DELETE FROM ot_consumable_usage WHERE prescription_id = ? AND operation_id = ?";

	public boolean deleteProcedures(List list,Connection con) throws SQLException {
		boolean success = true;
		try {

			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				ProcedureDetails pdt = (ProcedureDetails) iterator.next();
				deleteProcedureAndOTConsumableDetails(con,pdt.getProcedureId(),pdt.getPrescribedId(),pdt.getOperationId());

				// delete the physician forms data for the proc id.
				if (!OTForms.deleteFormDetails(con, pdt.getProcedureId())) return false;
			}
		} finally {
			DataBaseUtil.closeConnections(null, null);
		}
		return success;
	}

	public static boolean deleteProcedureAndOTConsumableDetails(Connection con, int operationProcId,int prescriptionId,String operationId) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(DELETE_PROCEDURE);
			ps.setInt(1, operationProcId);
			ps.executeUpdate();
			ps.close();

			ps = con.prepareStatement(DELETE_OT_CONSUMABLES);
			ps.setInt(1, prescriptionId);
			ps.setString(2, operationId);
			ps.executeUpdate();
			ps.close();

		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return true;
	}

	public static final String UPDATE_PROCEDURE = "UPDATE operation_procedures SET operation_details_id=?,oper_priority=?, operation_id = ?," +
			"  modifier = ? " +
			" WHERE operation_proc_id = ? ";

	public boolean updateProcedures(List<ProcedureDetails> list,Connection con) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(UPDATE_PROCEDURE);) {
  		boolean success = true;
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			ProcedureDetails pdt =  (ProcedureDetails) iterator.next();
  			ps.setInt(1,pdt.getOpDetailsId());
  			ps.setString(2, pdt.getOperationPriority());
  			ps.setString(3, pdt.getOperationId());
  			ps.setString(4, pdt.getModifier());
  			ps.setInt(5, pdt.getProcedureId());
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();
  
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
  		return success;
		}
	}

	private static final String DELETE_PROCEDURE_RESOURCES = "DELETE FROM operation_team WHERE operation_team_id = ?";

	public boolean deleteProceduerResources(List<OperationResources> list,Connection con) throws SQLException {
		boolean success = true;
		try (PreparedStatement ps = con.prepareStatement(DELETE_PROCEDURE_RESOURCES);) {
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			OperationResources or = (OperationResources) iterator.next();
  			ps.setInt(1, or.getOpTeamId());
  			ps.addBatch();
  		}
  		ps.executeBatch();
		}
		return success;
	}


	public static final String UPDATE_PROCEDURE_RESOURES = "UPDATE operation_team SET operation_details_id=?,resource_id=?, operation_speciality = ?" +
			"  WHERE operation_team_id = ? " ;

	public boolean updateProcedureResources(List<OperationResources> list,Connection con) throws SQLException {
	  boolean success = true;
	  try (PreparedStatement ps = con.prepareStatement(UPDATE_PROCEDURE_RESOURES);) {	
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			OperationResources or =  (OperationResources) iterator.next();
  			ps.setInt(1,or.getOpDetailsId());
  			ps.setString(2, or.getResourceId());
  			ps.setString(3, or.getOperationSpeciality());
  			ps.setInt(4, or.getOpTeamId());
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();
  
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
	  }
		return success;
	}

	public static final String INSERT_PROCEDURE_RESOURCES = "INSERT INTO operation_team(operation_team_id,operation_details_id,resource_id,operation_speciality) " +
			" values (?,?,?,?)";


	public boolean insertProcedureResources(List<OperationResources> list,Connection con) throws SQLException {
	  boolean success = true;
	  try (PreparedStatement ps = con.prepareStatement(INSERT_PROCEDURE_RESOURCES);) {
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			OperationResources or =  (OperationResources) iterator.next();
  			ps.setInt(1, or.getOpTeamId());
  			ps.setInt(2, or.getOpDetailsId());
  			ps.setString(3, or.getResourceId());
  			ps.setString(4, or.getOperationSpeciality());
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();
  
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
	  }
		return success;
	}

	private static final String DELETE_PROCEDURE_ANAETHESIA_TYPES = "DELETE FROM operation_anaesthesia_details WHERE operation_anae_detail_id = ?";

	public static boolean deleteProcedureAnaesthesiaTypes(List<BasicDynaBean> list,Connection con) throws SQLException{
		boolean success = true;
		try (PreparedStatement ps = con.prepareStatement(DELETE_PROCEDURE_ANAETHESIA_TYPES);) {
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			BasicDynaBean bean = (BasicDynaBean)iterator.next();
  			ps.setInt(1,(Integer)bean.get("operation_anae_detail_id"));
  			ps.addBatch();
  		}
  		ps.executeBatch();
		}
		return success;
	}

	private static final String UPDATE_PROCEDURES_ANAESTHESIA_DETAILS = "UPDATE operation_anaesthesia_details SET anesthesia_type = ?," +
			"	anaes_start_datetime = ?,anaes_end_datetime = ? WHERE operation_anae_detail_id = ?";

	public boolean updateProcedureAnaesthesiaTypes(List<BasicDynaBean> list,Connection con) throws SQLException{
	  boolean success = true;
	  try (PreparedStatement ps = con.prepareStatement(UPDATE_PROCEDURES_ANAESTHESIA_DETAILS);) {
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			BasicDynaBean bean =  (BasicDynaBean) iterator.next();
  			ps.setString(1,(String)bean.get("anesthesia_type"));
  			ps.setTimestamp(2, (Timestamp)bean.get("anaes_start_datetime"));
  			ps.setTimestamp(3, (Timestamp)bean.get("anaes_end_datetime"));
  			ps.setInt(4, (Integer)bean.get("operation_anae_detail_id"));
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();
  
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
	  }
		return success;
	}

	private static final String INSERT_PROCEDURES_ANAESTHESIA_DETAILS = "INSERT INTO operation_anaesthesia_details(operation_anae_detail_id,operation_details_id," +
			" anesthesia_type,anaes_start_datetime,anaes_end_datetime)" +
			" VALUES(?, ?, ?, ?, ?) " ;

	public boolean insertProcedureAnaesthesiaTypes(List<BasicDynaBean> list,Connection con) throws SQLException{
	  boolean success = true;
	  try (PreparedStatement ps = con.prepareStatement(INSERT_PROCEDURES_ANAESTHESIA_DETAILS);) {
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			BasicDynaBean bean =  (BasicDynaBean) iterator.next();
  			ps.setInt(1,(Integer)bean.get("operation_anae_detail_id"));
  			ps.setInt(2,(Integer)bean.get("operation_details_id"));
  			ps.setString(3,(String)bean.get("anesthesia_type"));
  			ps.setTimestamp(4, (Timestamp)bean.get("anaes_start_datetime"));
  			ps.setTimestamp(5, (Timestamp)bean.get("anaes_end_datetime"));
  			ps.addBatch();
  		}
  
  		int results[] = ps.executeBatch();
  		ps.close();
  
  		for (int p = 0; p < results.length; p++) {
  			if (results[p] <= 0) {
  				success = false;
  				break;
  			}
  		}
	  }
		return success;
	}

	private static final String GET_OTMANGEMENT_MINI_WINDOW_SURGERY_DETAILS = "SELECT * " +
			" FROM operation_details od " +
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND" +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no))" +
			" LEFT JOIN  operation_procedures op ON(op.operation_details_id = od.operation_details_id AND op.oper_priority = 'P')" +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
			" LEFT JOIN theatre_master tm ON(tm.theatre_id=od.theatre_id)" +
			" WHERE od.operation_details_id = ?";

	public static BasicDynaBean getOTMiniWindowSurgeryDetails(Integer opDetailsid) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_OTMANGEMENT_MINI_WINDOW_SURGERY_DETAILS);
			ps.setInt(1, opDetailsid);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OTMANGEMENT_MINI_WINDOW_SURGEON_DETAILS = "SELECT textcat_commacat(d.doctor_name) as surgeon " +
			" FROM operation_details od " +
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND" +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no))" +
			" JOIN operation_team ot ON(ot.operation_details_id = od.operation_details_id AND operation_speciality = 'SU')" +
			" JOIN doctors d ON(d.doctor_id=ot.resource_id)" +
			" WHERE od.operation_details_id = ?";

	public static BasicDynaBean getOtMiniWindowSurgeonDetails(Integer OpDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_OTMANGEMENT_MINI_WINDOW_SURGEON_DETAILS);
			ps.setInt(1, OpDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OTMANGEMENT_MINI_WINDOW_ANESTEATIST_DETAILS = "SELECT textcat_commacat(d.doctor_name) as anaesthetist " +
			" FROM operation_details od " +
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND" +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no))" +
			" JOIN operation_team ot ON(ot.operation_details_id = od.operation_details_id AND operation_speciality = 'AN')" +
			" JOIN doctors d ON(d.doctor_id=ot.resource_id)" +
			" WHERE od.operation_details_id = ?";

	public static BasicDynaBean getOtMiniWindowAnesteatistDetails(Integer OpDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_OTMANGEMENT_MINI_WINDOW_ANESTEATIST_DETAILS);
			ps.setInt(1, OpDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_OPERATION_BILL_ITEMS = "SELECT * FROM ( "+
			" SELECT op.operation_details_id, op.operation_proc_id," +
			" op.oper_priority,om.operation_name,d.doctor_name as resource_name,ot.operation_team_id::text as resource_id,'TEAM' " +
			" as resource_type, operation_speciality,billable "+
			" FROM operation_details od "+
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND " +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" LEFT JOIN operation_procedures op using(operation_details_id) "+
			" LEFT JOIN operation_team ot on (op.operation_details_id = ot.operation_details_id) "+
			" LEFT JOIN operation_master om on (om.op_id = op.operation_id) "+
			" LEFT JOIN doctors d on (d.doctor_id = ot.resource_id) "+
			" LEFT JOIN operation_billable_resources obr on (obr.resource_id = ot.operation_team_id::text AND "+
			"	obr.operation_proc_id = op.operation_proc_id AND obr.resource_type='TEAM') "+

			" UNION ALL "+

			" SELECT op.operation_details_id, op.operation_proc_id,op.oper_priority, "+
			" om.operation_name, tm.theatre_name as resource_name, od.theatre_id as resource_id, "+
			" 'THEAT' as resource_type,''::text AS operation_speciality,billable "+
			" FROM operation_details od "+
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND " +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" LEFT JOIN operation_procedures op using(operation_details_id) "+
			" LEFT JOIN operation_master om on (om.op_id = op.operation_id) "+
			" LEFT JOIN theatre_master tm on (tm.theatre_id = od.theatre_id) "+
			" LEFT JOIN operation_billable_resources obr on (obr.resource_id = od.theatre_id AND "+
			"	obr.operation_proc_id = op.operation_proc_id AND obr.resource_type='THEAT') "+

			" UNION ALL "+

			" SELECT op.operation_details_id, op.operation_proc_id,op.oper_priority, "+
			" om.operation_name,atm.anesthesia_type_name as resource_name, asad.operation_anae_detail_id::text as resource_id, "+
			" 'ANAE' as resource_type, ''::text AS operation_speciality,billable "+
			" FROM operation_details od "+
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND " +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" LEFT JOIN operation_procedures op using(operation_details_id) "+
			" LEFT JOIN operation_master om on (om.op_id = op.operation_id) "+
			" LEFT JOIN operation_anaesthesia_details asad ON(asad.operation_details_id=od.operation_details_id)" +
			" LEFT JOIN anesthesia_type_master atm on(atm.anesthesia_type_id = asad.anesthesia_type) " +
			" LEFT JOIN operation_billable_resources obr on (obr.resource_id = asad.operation_anae_detail_id::text AND "+
			"	obr.operation_proc_id = op.operation_proc_id AND obr.resource_type='ANAE') "+
			" ) AS foo WHERE operation_details_id=? and (resource_id is not null and resource_id!='') ";

	public List<BasicDynaBean> getOperationBillItems(int operationDetailsId)throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_OPERATION_BILL_ITEMS);
			ps.setInt(1, operationDetailsId);
			return DataBaseUtil.queryToDynaList(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}


	private static final String GET_PRIMARY_PROCEDURES = "SELECT * FROM operation_details od " +
			" LEFT JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND oper_priority = 'P')" +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
			" WHERE od.operation_details_id = ?";
	public static BasicDynaBean getPrimaryOperationDetails(Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PRIMARY_PROCEDURES);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PRIMARY_PROCEDURES_BY_PRESCRIBED_ID = "SELECT * FROM operation_details od " +
			" LEFT JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND oper_priority = 'P')" +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
			" WHERE op.prescribed_id = ?";

	public static BasicDynaBean getPrimaryOperationDetailsByPrescribedId(Integer prescribedId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_PRIMARY_PROCEDURES_BY_PRESCRIBED_ID);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_SECONDARY_PROCEDURES_BY_PRESCRIBED_ID = "SELECT * FROM operation_details od " +
      " LEFT JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND oper_priority = 'S')" +
      " LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
      " WHERE op.prescribed_id = ?";

  public static BasicDynaBean getSecondaryOperationDetailsByPrescribedId(Integer prescribedId) throws Exception{
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_SECONDARY_PROCEDURES_BY_PRESCRIBED_ID);
      ps.setInt(1, prescribedId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
	
	
	private static final String GET_PROCEDURES_BY_PRESCRIBED_ID = "SELECT * FROM operation_details od " +
      " LEFT JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id)" +
      " LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
      " WHERE op.prescribed_id = ? and operation_id = ?  ";

  public static BasicDynaBean getOperationDetailsByPrescribedId(Integer prescribedId,String operation_id) throws Exception{
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_PROCEDURES_BY_PRESCRIBED_ID);
      ps.setInt(1, prescribedId);
      ps.setString(2, operation_id);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

	private static final String GET_SECONDARY_PROCEDURES = "SELECT * FROM operation_details od " +
			" JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND oper_priority = 'S') " +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
			" WHERE od.operation_details_id = ?";
	public static List<BasicDynaBean> getSecondaryOperationDetails(Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SECONDARY_PROCEDURES);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_OPERATION_BILLABLE_RESOURCES = "SELECT * FROM operation_billable_resources obr" +
			"	LEFT JOIN operation_anaesthesia_details asad ON(asad.operation_anae_detail_id::text = obr.resource_id AND obr.resource_type = 'ANAE')" +
			"	WHERE operation_proc_id = ? " +
			" 	ORDER BY resource_type DESC";

	public static List<BasicDynaBean> getOperationBillableResources(Connection con,Integer opProcId) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_OPERATION_BILLABLE_RESOURCES);
			ps.setInt(1, opProcId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	private static final String GET_OPERATION_TEAM = 
	    " SELECT *,opt.resource_id as team_doc_id " + 
	    " FROM operation_billable_resources obr " + 
      " LEFT JOIN operation_team opt ON(opt.operation_team_id::text = obr.resource_id " + 
      " AND obr.resource_type = 'TEAM' AND opt.operation_speciality = 'SU') " + 
      " WHERE operation_proc_id = ? and obr.resource_type = 'TEAM' " + 
      "   ORDER BY resource_type DESC ";

  public static BasicDynaBean getOperationTeam(Connection con,Integer opProcId) throws Exception{
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_OPERATION_TEAM);
      ps.setInt(1, opProcId);
      return DataBaseUtil.queryToDynaBean(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }


	private static final String GET_RATE_PLAN_APPLICABLE_OPERATIONS = "SELECT * FROM operation_master om " +
			" JOIN operation_org_details ood ON (ood.operation_id = om.op_id) " +
			" WHERE ood.org_id = ? AND ood.applicable AND om.status = 'A'";

	public static List<BasicDynaBean> getRatePlanApplicableOperations(String orgId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RATE_PLAN_APPLICABLE_OPERATIONS);
			ps.setString(1, orgId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_RATE_PLAN_APPLICABLE_THEATRES = "SELECT * FROM theatre_master tm " +
			" JOIN theatre_org_details tod ON (tod.theatre_id = tm.theatre_id) " +
			" WHERE tod.org_id = ? AND tod.applicable AND tm.status = 'A' #";

	public List<BasicDynaBean> getRatePlanApplicableTheatres(String orgId,Integer centerId) throws Exception{
		String query = GET_RATE_PLAN_APPLICABLE_THEATRES;
		Boolean isMultiCenter = centerId != 0
		    && GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1;
		if (isMultiCenter) {
		  query = query.replaceAll("#", " AND center_id = ?");
		} else {
		  query = query.replaceAll("#", " ");
		}
		try (Connection con = DataBaseUtil.getConnection();
		    PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, orgId);
			if (isMultiCenter) {
			  ps.setInt(2, centerId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	private static final String GET_RATE_PLAN_APPLICABLE_ANESTHESIA_TYPES = "SELECT * FROM anesthesia_type_master atm " +
			" JOIN anesthesia_type_org_details atod ON (atod.anesthesia_type_id = atm.anesthesia_type_id) " +
			" WHERE atod.org_id = ? AND atod.applicable AND atm.status = 'A'";

	public static List<BasicDynaBean> getRatePlanApplicableAnesthesiaTypes(String orgId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RATE_PLAN_APPLICABLE_ANESTHESIA_TYPES);
			ps.setString(1, orgId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_APPLICABLE_OPERATIONS = "SELECT * FROM operation_master om " +
			" JOIN operation_org_details ood ON (ood.operation_id = om.op_id) " +
			" WHERE ood.org_id = ?  AND ood.operation_id = ? AND ood.applicable";

	public static BasicDynaBean getRatePlanApplicableOperations(String orgId,String operationId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_APPLICABLE_OPERATIONS);
			ps.setString(1, orgId);
			ps.setString(2, operationId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_APPLICABLE_THEATRES = "SELECT * FROM theatre_master tm " +
			" JOIN theatre_org_details tod ON (tod.theatre_id = tm.theatre_id) " +
			" WHERE tod.org_id = ? AND tod.applicable AND tod.theatre_id = ?";

	public static BasicDynaBean getRatePlanApplicableTheatres(String orgId,String theatreId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_APPLICABLE_THEATRES);
			ps.setString(1, orgId);
			ps.setString(2, theatreId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_SECONDARY_PROCEDURE_DETAILS = "SELECT * from bed_operation_secondary WHERE prescribed_id = ?";

	public static List<BasicDynaBean> getSecondaryOperationDetails(Connection con,Integer prescribedId) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_SECONDARY_PROCEDURE_DETAILS);
			ps.setInt(1,prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_THETARE_AS_BILLABLE_RESOURCE = "SELECT * FROM operation_billable_resources " +
			" WHERE operation_proc_id = ? AND resource_type = 'THEAT'";

	public static BasicDynaBean getTheatreAsBillableResource(Connection con,Integer opeProcId) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_THETARE_AS_BILLABLE_RESOURCE);
			ps.setInt(1,opeProcId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_COMMA_SEPARTED_SECONDARY_OPEARTIONS = "SELECT textcat_commacat(om.operation_name) AS secondary_opeartions" +
			" FROM operation_details od " +
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND " +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND oper_priority = 'S') " +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id)" +
			" WHERE od.operation_details_id = ? group by od.operation_details_id";
	public static String getCommaSepartedSecondaryOpeartions(Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_COMMA_SEPARTED_SECONDARY_OPEARTIONS);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_COMMA_SEPARATED_ANAETHESIA_TYPE_DETAILS = "SELECT textcat_commacat(atm.anesthesia_type_name) AS anaesthesia_types," +
			"	textcat_commacat(to_char(oad.anaes_start_datetime,'DD-MM-YYYY HH24:MI:SS')) as anaesthesia_start_datetime," +
			"	textcat_commacat(to_char(oad.anaes_end_datetime,'DD-MM-YYYY HH24:MI:SS')) as anaesthesia_end_datetime" +
			" FROM operation_details od " +
			" JOIN patient_details pd on (pd.mr_no = od.mr_no AND " +
	        " patient_confidentiality_check(pd.patient_group,pd.mr_no)) " +
			" JOIN operation_anaesthesia_details oad using(operation_details_id)" +
			" LEFT JOIN anesthesia_type_master atm ON(oad.anesthesia_type=atm.anesthesia_type_id)" +
			" WHERE od.operation_details_id = ? group by od.operation_details_id";

	public static BasicDynaBean getCommaSepartedAnaethesiaTypeDetails(Integer opDetailsId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_COMMA_SEPARATED_ANAETHESIA_TYPE_DETAILS);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_SURGERY_DETAILS_FOR_FTL = " SELECT od.*,tm.theatre_name, d.doctor_name as prescribed_by " +
			" FROM operation_details od " +
			" LEFT JOIN  operation_procedures op ON(op.operation_details_id = od.operation_details_id AND op.oper_priority = 'P') " +
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id) " +
			" LEFT JOIN theatre_master tm ON(tm.theatre_id=od.theatre_id) " +
			" LEFT JOIN doctors d ON(d.doctor_id=od.prescribing_doctor) "+
			" WHERE od.operation_details_id = ? ";

	public static BasicDynaBean getSurgeryDetailsForFTL(Integer opDetailsId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SURGERY_DETAILS_FOR_FTL);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_SURGERY_LIST_FOR_FTL = " SELECT om.operation_name ,op.modifier, od.operation_status, op.oper_priority" +
			" FROM operation_details od " +
			" JOIN operation_procedures op ON(op.operation_details_id=od.operation_details_id AND oper_priority IN ('P','S')) "+
			" LEFT JOIN operation_master om ON(om.op_id=op.operation_id) " +
			" WHERE od.operation_details_id = ? ORDER BY CASE op.oper_priority WHEN 'P' THEN 1 ELSE 2 END ";

	public static List getSurgeryListForFTL(Integer opDetailsId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_SURGERY_LIST_FOR_FTL);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OPERATION_TEAM_FOR_FTL = "SELECT d.doctor_name,ot.operation_speciality "+
			 " FROM operation_details od  " +
			 " JOIN operation_team ot ON(ot.operation_details_id = od.operation_details_id) " +
			 " JOIN doctors d ON(d.doctor_id=ot.resource_id) " +
			 " WHERE od.operation_details_id = ? ORDER BY operation_team_id ";

	public static List getOperationTeam(Integer opDetailsId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_OPERATION_TEAM_FOR_FTL);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OPERATION_ANAESTHESIA_DETAILS_FOR_FTL = "SELECT *,anaes_start_datetime as anaesthesia_start,  " +
			 "	anaes_end_datetime as anaesthesia_end"+
			 "  FROM operation_anaesthesia_details oad "+
			 "  JOIN anesthesia_type_master atm ON (atm.anesthesia_type_id = oad.anesthesia_type)  " +
			 "  WHERE oad.operation_details_id = ? ORDER BY anesthesia_type_name ";

	public static List getOperationAnaesthesiaDetails(Integer opDetailsId) throws Exception {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_OPERATION_ANAESTHESIA_DETAILS_FOR_FTL);
			ps.setInt(1, opDetailsId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static String GET_OT_CONSUMABLES_REQUIRED_QUANTITY = "SELECT  consumable_id as medicine_id,qty_needed FROM operation_details od  "
			+" LEFT JOIN operation_procedures op using(operation_details_id)  "
            +" LEFT JOIN  ot_consumables oc on(oc.operation_id=op.operation_id) "
            +" LEFT JOIN operation_master om on (om.op_id = op.operation_id)  "
            +" where op.operation_details_id=? ";

	private static final String CHECK__MEDICINE_AVAILABLE_OR_NOT =" select medicine_id from store_stock_details where medicine_id  in ( # ) and dept_id=? and asset_approved='Y' group by medicine_id";
	
	private static final String GET_STROE_STOCK_QUANTITY ="select medicine_id ,sum(qty) as total_qty from( SELECT  distinct sibd.item_batch_id,sibd.batch_no,sum(ssd.qty) as qty,"
			+" sibd.medicine_id FROM store_stock_details ssd  JOIN store_item_batch_details sibd ON(sibd.item_batch_id = ssd.item_batch_id) "
			+" WHERE ssd.dept_id=? and asset_approved='Y' AND exp_dt >= current_date and ssd.qty > 0 GROUP BY sibd.item_batch_id,sibd.batch_no ORDER BY item_batch_id ) as foo " 
			+" where  medicine_id in( # )GROUP BY  medicine_id ";
	
	public static Map checkAvailableQuantity(Connection con,Integer operation_details_id, Integer deptId) throws Exception {
		
		PreparedStatement ps = null;
		Map quantityMap = new HashMap();
		GenericPreferencesDTO genericPref = GenericPreferencesDAO.getGenericPreferences();
		try {
			String medicine_id ="";
			ps = con.prepareStatement(GET_OT_CONSUMABLES_REQUIRED_QUANTITY);
			ps.setInt(1, operation_details_id);
			List requiredQuantityDetails = DataBaseUtil.queryToDynaList(ps);
			quantityMap.put("required_qty", requiredQuantityDetails);
			
			for(int i=0;i<requiredQuantityDetails.size();i++){
				BasicDynaBean bean = (BasicDynaBean) requiredQuantityDetails.get(i);
				if(bean != null &&  null != bean.get("medicine_id")){
					medicine_id = medicine_id+(bean.get("medicine_id").toString())+",";	
				}
			}
			
			if(!medicine_id.equals("")){
				String query ="";
				if(genericPref.getConsumableStockNegative().equals("N")){
					 query = GET_STROE_STOCK_QUANTITY;
				}else{
					 query = CHECK__MEDICINE_AVAILABLE_OR_NOT;
				}
				ps = con.prepareStatement(query.replace("#", medicine_id.substring(0, medicine_id.length()-1)));
				ps.setInt(1, deptId);
			
				List availableQuantityDetails =  DataBaseUtil.queryToDynaList(ps);
				for(int i=0;i<availableQuantityDetails.size();i++){
					BasicDynaBean bean = (BasicDynaBean) availableQuantityDetails.get(i);
					if(genericPref.getConsumableStockNegative().equals("N")){
						quantityMap.put((Integer)bean.get("medicine_id"),(BigDecimal)bean.get("total_qty"));
					}else{
						quantityMap.put((Integer)bean.get("medicine_id"),0);
					}
				}
				return quantityMap;
			}else{
				return quantityMap;
			}
			
		} finally {
			ps.close();
		}
	}
	
	private static String GET_OT_MODITY_QUANTITY ="select consumable_id as medicine_id,qty as qty_needed from ot_consumable_usage "
	    + "where prescription_id =? AND operation_id = ? AND operation_type = ?";
	
	public static List getModifyQuantity(Connection con,Integer prescription_id, String operation_id, String operation_type) throws Exception {
		
		try (PreparedStatement ps = con.prepareStatement(GET_OT_MODITY_QUANTITY);) {
			ps.setInt(1, prescription_id);
			ps.setString(2, operation_id);
			ps.setString(3, operation_type);
			return DataBaseUtil.queryToDynaList(ps);	
		} 
		
	}
	
	private static String GET_OPERATION_DETAILS_WITH_CONF_CHECK ="SELECT od.* FROM operation_details od "
			+ " JOIN patient_details pd ON (pd.mr_no = od.mr_no AND patient_confidentiality_check(pd.patient_group,pd.mr_no)) "
			+ " WHERE od.operation_details_id = ?";
	
	public static BasicDynaBean getOperationDetailsBean(Integer operation_detail_id) throws Exception {

		return DataBaseUtil.queryToDynaBean(GET_OPERATION_DETAILS_WITH_CONF_CHECK, new Integer[] {operation_detail_id});
	}

}
