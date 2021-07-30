package com.insta.hms.resourcescheduler;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.otmasters.theamaster.TheatreMasterDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.QueryBuilder;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.resourcescheduler.ResourceBO.AppointMentResource;
import com.insta.hms.resourcescheduler.ResourceBO.Appointments;
import com.insta.hms.resourcescheduler.ResourceBO.ChannellingAppt;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ResourceDAO {
	static Logger logger = LoggerFactory.getLogger(ResourceDAO.class);
    
    private static final GenericDAO schedulerAppointmentsDAO =
        new GenericDAO("scheduler_appointments");

	Connection con = null;

	public ResourceDAO(Connection con){
		this.con = con;
	}
	public ResourceDAO()
	{}


  public static final String GET_CALNDER_OT_DETATILS = "SELECT sm.res_sch_id,sm.default_duration,sm.height_in_px," +
  		" sm.description,sm.res_sch_name " +
  		" FROM  scheduler_master sm  ";

 public static BasicDynaBean getTimeDurations(ResourceCriteria rc) throws SQLException{
	  BasicDynaBean l = null;
	  Connection con = DataBaseUtil.getReadOnlyConnection();
	  PreparedStatement ps = null;
      try{
 		   StringBuilder where = new StringBuilder();
		   DataBaseUtil.addWhereFieldOpValue(where, "sm.res_sch_type","=" , " ");
		   DataBaseUtil.addWhereFieldOpValue(where, "sm.res_sch_name","=" , " " );
		   StringBuilder query = new StringBuilder(GET_CALNDER_OT_DETATILS);
		   query.append(where);
		   query.append("order by sm.res_sch_id");
		   logger.debug("{}", query);

		   ps = con.prepareStatement(query.toString());
		   if(rc.category.equals("DOC"))
			   ps.setString(1, rc.category);
		   else if (rc.category.equals("DIA"))
			   ps.setString(1, "EQID");
		   else if(rc.category.equals("SNP"))
			   ps.setString(1, "SRID");
		   else if(rc.category.equals("OPE"))
			   ps.setString(1, "THID");

		   ps.setString(2, "*");

		   l =   (BasicDynaBean)(DataBaseUtil.queryToDynaList(ps)).get(0);

      }finally{
    	  DataBaseUtil.closeConnections(con, ps);
      }
	  return l;
  }

	 private static final String GET_DOCTORS_AS_SCHEDULENAMES = "SELECT d.doctor_id as id,d.doctor_name as scheduleName,dcm.center_id" +
	 		" FROM  doctors d "+
	 		" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)";

	 private static final String GET_THEATERS_AS_SCHEDULENAMES = "SELECT tm.theatre_id as id , tm.theatre_name as scheduleName" +
	 		" FROM theatre_master tm ";


	 private static final String GET_EQUIPMENTS_AS_SCHEDULENAMES = "SELECT eq.eq_id::text as id,eq.equipment_name as scheduleName " +
	 		" FROM test_equipment_master eq  " ;

	 private static final String GET_SERVICE_RESOURCES_AS_SCHEDULENAMES = "SELECT sre.serv_res_id::text as id,sre.serv_resource_name as scheduleName " +
			" FROM service_resource_master sre  ";

	 public static List<BasicDynaBean> getScheduleNames(ResourceCriteria rc)throws SQLException {
		 List<BasicDynaBean> l = null;
		 Connection con = DataBaseUtil.getReadOnlyConnection();
		 PreparedStatement ps = null;
		 try{
			 if(rc.category.equals("DOC")){
				StringBuilder query = new StringBuilder(GET_DOCTORS_AS_SCHEDULENAMES);
				StringBuilder where = new StringBuilder();
				DataBaseUtil.addWhereFieldInList(where,"d.doctor_id",rc.scheduleName);
				query.append(where);
				query.append(" order by d.doctor_name ");
				ps = con.prepareStatement(query.toString());
				int index = 1;
				for(int i=0;i<rc.scheduleName.size();i++,index++){
					  ps.setString(index, rc.scheduleName.get(i));
				}

			 }else if(rc.category.equals("OPE")){
				 StringBuilder query = new StringBuilder(GET_THEATERS_AS_SCHEDULENAMES);
				 StringBuilder where = new StringBuilder();
				 DataBaseUtil.addWhereFieldInList(where,"tm.theatre_id",rc.scheduleName);
				 query.append(where);
				 query.append(" order by tm.theatre_name ");
				 ps = con.prepareStatement(query.toString());
				 int index = 1;
				 for(int i=0;i<rc.scheduleName.size();i++,index++){
					  ps.setString(index, rc.scheduleName.get(i));
				 }
			 }else if(rc.category.equals("DIA")){

				 StringBuilder query = new StringBuilder(GET_EQUIPMENTS_AS_SCHEDULENAMES);
				 StringBuilder where = new StringBuilder();
				 DataBaseUtil.addWhereFieldInList(where,"eq_id::text",rc.scheduleName);
				 query.append(where);
				 query.append(" order by eq.equipment_name ");
				 ps = con.prepareStatement(query.toString());
				 int index = 1;
				 for(int i=0;i<rc.scheduleName.size();i++,index++){
					  ps.setString(index, rc.scheduleName.get(i));
				 }
			 } else if (rc.category.equals("SNP")) {
				 StringBuilder query = new StringBuilder(GET_SERVICE_RESOURCES_AS_SCHEDULENAMES);
				 StringBuilder where = new StringBuilder();
				 DataBaseUtil.addWhereFieldInList(where,"serv_res_id::text",rc.scheduleName);
				 query.append(where);
				 query.append(" order by sre.serv_resource_name ");
				 ps = con.prepareStatement(query.toString());
				 int index = 1;
				 for(int i=0;i<rc.scheduleName.size();i++,index++){
					  ps.setString(index, rc.scheduleName.get(i));
				 }
			 }

			 l = DataBaseUtil.queryToDynaList(ps);

		 }finally{
			 DataBaseUtil.closeConnections(con, ps);
		 }
		 return l;
	 }


	private static final String GET_APPOINTS =
		    " SELECT sp.res_sch_id,sp.res_sch_name,r.scheduleitem,sp.contact_id, "+
			" CASE WHEN (uca.confidentiality_grp_id IS NULL AND pd.patient_group !=0) THEN 'Xxxxxx' ELSE sp.mr_no END AS mr_no, "+
			" CASE WHEN (uca.confidentiality_grp_id IS NULL AND pd.patient_group !=0) THEN 'Xxxxxx' ELSE sp.visit_id END AS visit_id, "+
			" CASE WHEN (uca.confidentiality_grp_id IS NULL AND pd.patient_group !=0) THEN 'Xxxxxx' ELSE sp.patient_name END AS patient_name, "+
			" CASE WHEN (uca.confidentiality_grp_id IS NULL AND pd.patient_group !=0) THEN 'Xxxxxx' ELSE sp.patient_contact END AS patient_contact, "+
			" CASE WHEN (uca.confidentiality_grp_id IS NULL AND pd.patient_group !=0) THEN 'N' ELSE 'Y' END AS is_patient_group_accessible, "+
			" sp.appointment_id,to_char( sp.appointment_time, 'hh24:mi:ss')::time as appointment_time,sp.center_id,hcm.center_code,hcm.center_name, " +
			" sp.duration as appointment_duration, sp.appointment_status, sp.booked_by, sp.booked_time, " +
			" sp.changed_by,sp.changed_time,api.resource_type,api.resource_id,sm.res_sch_category,sm.res_sch_type," +
			" sp.complaint AS complaint_name, sp.remarks,r.dept_name,sp.package_id, "+
			" sp.presc_doc_id, (select doctor_name from doctors where doctor_id=sp.presc_doc_id) as presc_doctor, b.payment_status, " +
			" b.bill_type, cas.paid_at_source, cgm.abbreviation " +
			" FROM scheduler_appointments sp  join scheduler_master sm using(res_sch_id) " +
			" left join scheduler_appointment_items api using(appointment_id) " +
			" left join hospital_center_master hcm ON(hcm.center_id=sp.center_id)"+
			" left join bill b on (sp.bill_no = b.bill_no) " +
			" left join appointment_source_master cas ON (cas.appointment_source_id = sp.app_source_id) " +
			" LEFT JOIN patient_details pd ON (pd.mr_no = sp.mr_no)" +
			" LEFT JOIN confidentiality_grp_master cgm ON (cgm.confidentiality_grp_id = pd.patient_group AND cgm.confidentiality_grp_id != 0) " +
			" LEFT JOIN user_confidentiality_association uca ON (uca.confidentiality_grp_id = pd.patient_group AND emp_username = ?) "+
			" LEFT join( select sp.res_sch_id,"+
			" coalesce(doc.doctor_name,dig.test_name,op.operation_name,s.service_name,t.theatre_name,eq.equipment_name,srm.serv_resource_name) as scheduleitem,"+
			" coalesce(doc.doctor_id,dig.test_id,op.op_id,s.service_id,eq.eq_id::text,t.theatre_id,srm.serv_res_id::text) as id," +
			" doc.doctor_id, "+
			" '(' ||sd.department || ')' AS dept_name "+
			" from scheduler_appointments sp left join doctors doc  on sp.prim_res_id=doc.doctor_id "+
			" left join diagnostics dig on dig.test_id=sp.res_sch_name "+
			" left join operation_master op on ( op.op_id = sp.res_sch_name)"+
			" left join services s on s.service_id =  sp.res_sch_name "+
			" left join test_equipment_master eq on eq.eq_id::text  = sp.res_sch_name " +
			" left join service_resource_master srm on srm.serv_res_id::text  = sp.res_sch_name " +
			" left join services_departments sd ON(sd.serv_dept_id = s.serv_dept_id)" +
			" left join theatre_master t on t.theatre_id = sp.res_sch_name " +
			" where " +
			" date(sp.appointment_time)  = ? " +
			" group by sp.res_sch_id," +
			" coalesce(doc.doctor_name,dig.test_name,op.operation_name, "+
			" s.service_name,t.theatre_name,eq.equipment_name,srm.serv_resource_name), "+
			" coalesce(doc.doctor_id,dig.test_id,op.op_id,s.service_id, "+
	        " eq.eq_id::text,t.theatre_id,srm.serv_res_id::text),sd.department,doc.doctor_id) as r on r.res_sch_id = sp.res_sch_id AND " +
			" (CASE WHEN r.doctor_id IS NULL THEN r.id = sp.res_sch_name ELSE r.id = sp.prim_res_id END) ";

	public static List<BasicDynaBean> getAppointMentList(ResourceCriteria rc,int centerId, String userName)throws Exception{

		List<BasicDynaBean> list = null;
		Connection con  = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		List<String> resourceTypeList= new ArrayList<String>();
		List<String> genericResourceTypeList = ResourceDAO.getGenricResourceTypes();

		for(String resourceType : genericResourceTypeList) {
			resourceTypeList.add(resourceType);
		}

		if (rc.category.equals("DIA")) {
			resourceTypeList.add("SRID");
		} else if (rc.category.equals("SNP")) {
			resourceTypeList.add("EQID");
		}
		try{
			List<String> valueList = new ArrayList<String>();
			valueList.add("Noshow");
			valueList.add("Cancel");
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldOpValue(where, "date(sp.appointment_time)", "=", " ");
			if (rc.category.equals("DIA")) {
				DataBaseUtil.addNotInWhereFieldInList(where, "api.resource_type", resourceTypeList, true);
			} else if(rc.category.equals("SNP")) {
				DataBaseUtil.addNotInWhereFieldInList(where, "api.resource_type", resourceTypeList, true);
			}
			DataBaseUtil.addWhereFieldInList(where, "api.resource_id", rc.scheduleName);
			if (rc.choosendate.equals(DateUtil.getCurrentDate()) || rc.choosendate.after(DateUtil.getCurrentDate()))
				QueryBuilder.addWhereFieldOpValue(true, where, "sp.appointment_status", "NOT IN", valueList);

			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1 && centerId != 0) {
				QueryBuilder.addWhereFieldOpValue(true, where, "sp.center_id", "= ?");
			}
			StringBuilder query = new StringBuilder(GET_APPOINTS);
			query.append(where);
			query.append("order by sp.res_sch_name,sp.appointment_id");
			logger.debug("{}", query);

			ps = con.prepareStatement(query.toString());
			ps.setString(1, userName);
			ps.setDate(2,rc.choosendate);
			ps.setDate(3, rc.choosendate);
			int index = 4;
			if (rc.category.equals("DIA")) {
				for (int i=0;i<resourceTypeList.size();i++,index++) {
					ps.setString(index, resourceTypeList.get(i));
				}
			} else if(rc.category.equals("SNP")){
				for (int i=0;i<resourceTypeList.size();i++,index++) {
					ps.setString(index, resourceTypeList.get(i));
				}
			}

			for(int i=0;i<rc.scheduleName.size();i++,index++){
				  ps.setString(index, rc.scheduleName.get(i));
			}

			if (rc.choosendate.equals(DateUtil.getCurrentDate()) || rc.choosendate.after(DateUtil.getCurrentDate())) {
				for(int i=0;i<valueList.size();i++,index++){
					  ps.setString(index, valueList.get(i));
				}
			}
			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1 && centerId != 0) {
				ps.setInt(index, centerId);
			}
			list = DataBaseUtil.queryToDynaList(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

		return list;
	}



   public static final String GET_OT_SCHEDULES = "SELECT ot.theatre_id,ot.theatre_name FROM  theatre_master ot " +
   		" WHERE ot.schedule = true AND ot.status = 'A' order by ot.theatre_name";

	public static final String GET_ALL_TESTS = "SELECT test_name,test_id FROM  diagnostics where status = 'A' order by test_name";

	public static final String GET_ALL_SERVICES = "SELECT service_id,service_name ||'(' || department || ')' as service_name FROM  services s " +
			"	join services_departments sd ON(sd.serv_dept_id = s.serv_dept_id) order by service_name " ;

	public static final String GET_ALL_OPERATIONS = "SELECT op_id,operation_name FROM  operation_master where status='A' order by operation_name " ;

	public static final String GET_ALL_GENERIC_RESOURCES = "SELECT * FROM generic_resource_master grm " +
			" JOIN generic_resource_type grt ON(grt.generic_resource_type_id = grm.generic_resource_type_id)" +
			" where grm.status='A' AND grt.status = 'A' order by generic_resource_name ";

	public static final String GET_ALL_RESOURCES = " SELECT doctor_name as resource_name," +
			"	d.doctor_id as resource_id,'DOC' as resource_type,'' as scheduler_resource_type,dept_id " +
			"	From doctors d " +
			" 	LEFT JOIN doctor_center_master dcm on (d.doctor_id = dcm.doctor_id)"+
			"	WHERE schedule = true AND d.status='A' and dcm.status='A' " +
			"UNION ALL " +
			"  	SELECT theatre_name as resource_name,theatre_id as resource_id," +
			"	'THID' as resource_type,'' as scheduler_resource_type,'' as dept_id " +
			"	FROM theatre_master" +
			"	WHERE schedule = true AND status='A' " +
			"UNION ALL " +
			"	SELECT serv_resource_name as resource_name,serv_res_id::text as resource_id," +
			"	'SRID' as resource_type,'' as scheduler_resource_type,'' as dept_id " +
			"	FROM service_resource_master " +
			"	WHERE schedule = true AND status='A' " +
			"UNION ALL " +
			"	SELECT equipment_name as resource_name,eq_id::text as resource_id," +
			"	'EQID' as resource_type,'' as scheduler_resource_type,'' as dept_id " +
			"	FROM test_equipment_master " +
			"	WHERE schedule=true AND status='A' " +
			"UNION ALL " +
			"	SELECT test_name  as resource_name,test_id as resource_id," +
			"	'TST' as resource_type,'' as scheduler_resource_type,ddept_id as dept_id " +
			"	FROM diagnostics " +
			"	WHERE status = 'A'" +
			"UNION ALL " +
			"	SELECT operation_name  as resource_name,op_id as resource_id," +
			"	'SUR' as resource_type,'' as scheduler_resource_type,dept_id " +
			"	FROM operation_master " +
			"	WHERE status = 'A' " +
			"UNION ALL " +
			"	SELECT service_name||'('||department||')'  as resource_name,service_id as resource_id," +
			"	'SER' as resource_type,'' as scheduler_resource_type,s.serv_dept_id::text as dept_id " +
			"	FROM services s" +
			"	JOIN services_departments sd ON(sd.serv_dept_id=s.serv_dept_id) " +
			"	WHERE s.status = 'A' " +
			"UNION ALL " +
			"	SELECT generic_resource_name as resource_name,generic_resource_id::text as resource_id," +
			"	'GEN' as resource_type,scheduler_resource_type,'' as dept_id " +
			"	FROM generic_resource_master grm" +
			" 	JOIN generic_resource_type grt ON(grt.generic_resource_type_id = grm.generic_resource_type_id)" +
			" 	WHERE grm.status='A' AND grt.status = 'A' AND schedule=true ";

	public static final String GET_ALL_RESOURCES_CenterWise = " SELECT doctor_name as resource_name," +
		"	d.doctor_id as resource_id,'DOC' as resource_type,'' as scheduler_resource_type,dept_id " +
		"	From doctors d " +
		" 	LEFT JOIN doctor_center_master dcm on (d.doctor_id = dcm.doctor_id)"+
		"	WHERE schedule = true AND d.status='A' AND (dcm.center_id = 0 OR dcm.center_id = ? ) and dcm.status='A' " +
		"UNION ALL " +
		"  	SELECT theatre_name as resource_name,theatre_id as resource_id," +
		"	'THID' as resource_type,'' as scheduler_resource_type,'' as dept_id " +
		"	FROM theatre_master" +
		"	WHERE schedule = true AND status='A' " +
		"UNION ALL " +
		"	SELECT serv_resource_name as resource_name,serv_res_id::text as resource_id," +
		"	'SRID' as resource_type,'' as scheduler_resource_type,'' as dept_id " +
		"	FROM service_resource_master " +
		"	WHERE schedule = true AND status='A' " +
		"UNION ALL " +
		"	SELECT equipment_name as resource_name,eq_id::text as resource_id," +
		"	'EQID' as resource_type,'' as scheduler_resource_type,'' as dept_id " +
		"	FROM test_equipment_master " +
		"	WHERE schedule=true AND status='A' " +
		"UNION ALL " +
		"	SELECT test_name  as resource_name,test_id as resource_id," +
		"	'TST' as resource_type,'' as scheduler_resource_type,ddept_id as dept_id " +
		"	FROM diagnostics " +
		"	WHERE status = 'A'" +
		"UNION ALL " +
		"	SELECT operation_name  as resource_name,op_id as resource_id," +
		"	'SUR' as resource_type,'' as scheduler_resource_type,dept_id " +
		"	FROM operation_master " +
		"	WHERE status = 'A' " +
		"UNION ALL " +
		"	SELECT service_name||'('||department||')'  as resource_name,service_id as resource_id," +
		"	'SER' as resource_type,'' as scheduler_resource_type,s.serv_dept_id::text as dept_id " +
		"	FROM services s" +
		"	JOIN services_departments sd ON(sd.serv_dept_id=s.serv_dept_id) " +
		"	WHERE s.status = 'A' " +
		"UNION ALL " +
		"	SELECT generic_resource_name as resource_name,generic_resource_id::text as resource_id," +
		"	'GEN' as resource_type,scheduler_resource_type,'' as dept_id " +
		"	FROM generic_resource_master grm" +
		" 	JOIN generic_resource_type grt ON(grt.generic_resource_type_id = grm.generic_resource_type_id)" +
		" 	WHERE grm.status='A' AND grt.status = 'A' AND schedule=true ";

	public static List<BasicDynaBean> getAllResources()throws SQLException {
		  Connection con = DataBaseUtil.getReadOnlyConnection();
		  PreparedStatement ps = null;
		  int centerID = RequestContext.getCenterId();
		  try{
			  if(centerID == 0){
				  ps = con.prepareStatement(GET_ALL_RESOURCES);
			  }else{
				  ps = con.prepareStatement(GET_ALL_RESOURCES_CenterWise);
				  ps.setInt(1, centerID);
			  }

			  return DataBaseUtil.queryToDynaList(ps);
		  }finally{
			  DataBaseUtil.closeConnections(con, ps);
		  }
	}

	public static final String GET_DOCTOR_SCHEDULES = "select doc.doctor_id,doc.doctor_name,doc.dept_id "+
						" From doctors doc where doc.schedule = true AND doc.status = 'A' order by doc.doctor_name";

	private   String GET_SERVICE_RESOURCE_SCHEDULES = "select srm.serv_res_id::text,srm.serv_resource_name,'' AS dept_id,overbook_limit,center_id FROM  service_resource_master srm " +
			" WHERE srm.schedule = true AND srm.status = 'A' # order by srm.serv_resource_name";

	public List<BasicDynaBean> getServResourceSchedules(String category,int centerId)throws SQLException{
		  Connection con = DataBaseUtil.getReadOnlyConnection();
		  PreparedStatement ps = null;
		  String centerClause = "And center_id = ?";
		  try{

			  if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) == 1) {
				  GET_SERVICE_RESOURCE_SCHEDULES = GET_SERVICE_RESOURCE_SCHEDULES.replace("#", "");
				  ps = con.prepareStatement(GET_SERVICE_RESOURCE_SCHEDULES);
			  } else {
				if(centerId != 0) {
					GET_SERVICE_RESOURCE_SCHEDULES = GET_SERVICE_RESOURCE_SCHEDULES.replace("#", centerClause);
					ps = con.prepareStatement(GET_SERVICE_RESOURCE_SCHEDULES);
					ps.setInt(1, centerId);
				} else {
					GET_SERVICE_RESOURCE_SCHEDULES = GET_SERVICE_RESOURCE_SCHEDULES.replace("#", "");
					ps = con.prepareStatement(GET_SERVICE_RESOURCE_SCHEDULES);
				}
			  }
			  logger.debug(GET_SERVICE_RESOURCE_SCHEDULES);

			  return DataBaseUtil.queryToDynaList(ps);
		  }finally{
			  DataBaseUtil.closeConnections(con, ps);
		  }
	}

	private String GET_ALL_MAPPED_SERVICE_RESOURCE = "SELECT srm.serv_res_id::text,srm.serv_resource_name,'' AS dept_id,overbook_limit,"
			+ " center_id, ssrm.service_id "
			+ " FROM service_resource_master srm"
			+ " JOIN service_service_resources_mapping ssrm ON (srm.serv_res_id = ssrm.serv_res_id) "
			+ " WHERE srm.schedule = true AND srm.status = 'A' # order by srm.serv_resource_name";
	
	private String GET_ALL_MAPPED_EQUIPMENT_RESOURCE = "SELECT tem.eq_id::text, tem.equipment_name,'' AS dept_id, tem.overbook_limit,"
			+ " tem.center_id, dtem.test_id "
			+ " from test_equipment_master tem"
			+ " JOIN diagnostics_test_equipment_master_mapping dtem ON (dtem.eq_id = tem.eq_id) "
			+ " WHERE tem.status='A' AND tem.schedule = true # order by equipment_name";

  private String GET_ALL_MAPPED_THEATRES = "SELECT thm.theatre_id::text, thm.theatre_name, thm.overbook_limit, center_id, otm.theatre_id, otm.operation_id "
      + " FROM theatre_master thm "
      + " JOIN operation_theatre_mapping otm ON (otm.theatre_id = thm.theatre_id) ##userTheatreFilter##"
      + " WHERE thm.status='A' AND thm.schedule = 't'  #centerFilter# order by theatre_name";

  public List<BasicDynaBean> getAllMappedResources(String category, int centerId,String userName,int roleId)
      throws SQLException {
    Connection con = DataBaseUtil.getReadOnlyConnection();
    PreparedStatement ps = null;
    String centerClause = "And center_id = ?";
    String JoinQuery = "join user_theatres ut ON( thm.theatre_id = ut.theatre_id and ut.emp_username = '##userName##')";
    if ((roleId != 1) && (roleId != 2)) {
      GET_ALL_MAPPED_THEATRES = GET_ALL_MAPPED_THEATRES.replace("##userTheatreFilter##", JoinQuery);
      GET_ALL_MAPPED_THEATRES = GET_ALL_MAPPED_THEATRES.replace("##userName##", userName);
    } else {
      GET_ALL_MAPPED_THEATRES = GET_ALL_MAPPED_THEATRES.replace("##userTheatreFilter##", "");
      }
		  try {
			  
			  if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) == 1) {
				  if (category.equals("SNP")) {
					  GET_ALL_MAPPED_SERVICE_RESOURCE = GET_ALL_MAPPED_SERVICE_RESOURCE.replace("#", "");
					  ps = con.prepareStatement(GET_ALL_MAPPED_SERVICE_RESOURCE);
				  } else if (category.equals("EQID")) {
					  GET_ALL_MAPPED_EQUIPMENT_RESOURCE = GET_ALL_MAPPED_EQUIPMENT_RESOURCE.replace("#", "");
					  ps = con.prepareStatement(GET_ALL_MAPPED_EQUIPMENT_RESOURCE);					  
				  } else if (category.equals("OPE")) {
            GET_ALL_MAPPED_THEATRES = GET_ALL_MAPPED_THEATRES.replace("#centerFilter#", " ");
            ps = con.prepareStatement(GET_ALL_MAPPED_THEATRES);
          }
			  } else {
				  if (category.equals("SNP")) {
						if(centerId != 0) {
							GET_ALL_MAPPED_SERVICE_RESOURCE = GET_ALL_MAPPED_SERVICE_RESOURCE.replace("#", centerClause);
							ps = con.prepareStatement(GET_ALL_MAPPED_SERVICE_RESOURCE);
							ps.setInt(1, centerId);
						} else {
							GET_ALL_MAPPED_SERVICE_RESOURCE = GET_ALL_MAPPED_SERVICE_RESOURCE.replace("#", "");
							ps = con.prepareStatement(GET_ALL_MAPPED_SERVICE_RESOURCE);
						}					  
				  } else if (category.equals("EQID")) {
						if(centerId != 0) {
							GET_ALL_MAPPED_EQUIPMENT_RESOURCE = GET_ALL_MAPPED_EQUIPMENT_RESOURCE.replace("#", centerClause);
							ps = con.prepareStatement(GET_ALL_MAPPED_EQUIPMENT_RESOURCE);
							ps.setInt(1, centerId);
						} else {
							GET_ALL_MAPPED_EQUIPMENT_RESOURCE = GET_ALL_MAPPED_EQUIPMENT_RESOURCE.replace("#", "");
							ps = con.prepareStatement(GET_ALL_MAPPED_EQUIPMENT_RESOURCE);
						}					  					  
        } else if (category.equals("OPE")) {
          if (centerId != 0) {
            GET_ALL_MAPPED_THEATRES = GET_ALL_MAPPED_THEATRES.replace("#centerFilter#",
                centerClause);
            ps = con.prepareStatement(GET_ALL_MAPPED_THEATRES);
            ps.setInt(1, centerId);
          } else {
            GET_ALL_MAPPED_THEATRES = GET_ALL_MAPPED_THEATRES.replace("#centerFilter#", " ");
            ps = con.prepareStatement(GET_ALL_MAPPED_THEATRES);
          }
        }
      }

			  return DataBaseUtil.queryToDynaList(ps);
		  } finally {
			  DataBaseUtil.closeConnections(con, ps);
		  }
		
	}
	
	private static final String GET_RESOURCE_LIST =
		" select sm.res_sch_id,sm.res_sch_category,sm.res_sch_type,sm.dept,sm.res_sch_name," +
		" sim.resource_type,sim.resource_id,sm.default_duration,sm.height_in_px,sm.description"+
		" from scheduler_master sm left join scheduler_item_master sim using(res_sch_id)"+
		" where sm.res_sch_name = ? AND sm.res_sch_type = ? AND status = 'A'";

	public static List<BasicDynaBean> getResourceList(String category,String resourceScheduleName)throws SQLException{
		List<BasicDynaBean> list = null;
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		String resourceType =null;
		try{
			logger.debug(GET_RESOURCE_LIST);
			ps = con.prepareStatement(GET_RESOURCE_LIST);
			ps.setString(1, resourceScheduleName);
			if (category.equals("DOC"))
				resourceType = category;
			else if(category.equals("DIA"))
				resourceType = "EQID";
			else if(category.equals("SNP"))
				resourceType = "SRID";
			else if(category.equals("OPE"))
				resourceType = "THID";
			ps.setString(2, resourceType);
			list= DataBaseUtil.queryToDynaList(ps);
			if(list!=null && list.size()>0) {}
			else {
				ps = con.prepareStatement(GET_RESOURCE_LIST);
				ps.setString(1, "*");
				ps.setString(2, resourceType);
				list= DataBaseUtil.queryToDynaList(ps);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	public static List<BasicDynaBean> getResourceListByResourceType(String category,String resourceScheduleName)throws SQLException{
		List<BasicDynaBean> list = null;
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		try{
			logger.debug(GET_RESOURCE_LIST);
			ps = con.prepareStatement(GET_RESOURCE_LIST);
			ps.setString(1, resourceScheduleName);
			ps.setString(2, category);
			list= DataBaseUtil.queryToDynaList(ps);
			if(list!=null && list.size()>0) {}
			else {
				ps = con.prepareStatement(GET_RESOURCE_LIST);
				ps.setString(1, "*");
				ps.setString(2, category);
				list= DataBaseUtil.queryToDynaList(ps);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	private static final String GET_ALL_MAPPED_RESOURCES = "SELECT srm.serv_res_id, srm.serv_resource_name "
      + " FROM service_service_resources_mapping ssrm "
      + " JOIN service_resource_master srm ON (srm.serv_res_id = ssrm.serv_res_id) "
      + " JOIN hospital_center_master hcm ON (hcm.center_id = srm.center_id) "
      + " WHERE ssrm.service_id = ? ##centerFilter## AND hcm.status = 'A' AND srm.status='A'";

	private static final String GET_ALL_MAPPED_TEST_RESOURCES = "SELECT tm.eq_id,equipment_name FROM test_equipment_master tm " +
        " JOIN diagnostics_test_equipment_master_mapping dtem ON (dtem.eq_id = tm.eq_id) " +
        " JOIN diagnostics d ON (d.test_id = dtem.test_id)" +
        " JOIN hospital_center_master hcm ON (hcm.center_id = tm.center_id) " +
        " WHERE tm.status ='A' AND tm.schedule = 't' AND hcm.status ='A' and dtem.test_id = ? ##centerFilter## ";
	
  private static String GET_MAPPED_THEATRES = "SELECT thm.theatre_id,thm.theatre_name "
      + " FROM operation_theatre_mapping otm "
      + " JOIN theatre_master thm ON (thm.theatre_id = otm.theatre_id) "
      + " JOIN hospital_center_master hcm ON (hcm.center_id = thm.center_id) ##userTheatreFilter##"
      + " WHERE otm.operation_id = ? AND  hcm.status = 'A' AND thm.status='A' AND thm.schedule= 't' ##centerFilter## ";

  public static List<BasicDynaBean> getMappedResources(String schName, String category,
      int centerId,String userName,int roleId) throws SQLException {
    String GET_MAPPED_THEATRES_REPLACED = GET_MAPPED_THEATRES;
    String JoinQuery = "join user_theatres ut ON( thm.theatre_id = ut.theatre_id and ut.emp_username = '##userName##')";
    if ((roleId != 1) && (roleId != 2)) {
      GET_MAPPED_THEATRES_REPLACED = GET_MAPPED_THEATRES_REPLACED.replace("##userTheatreFilter##", JoinQuery);
      GET_MAPPED_THEATRES_REPLACED = GET_MAPPED_THEATRES_REPLACED.replace("##userName##", userName);
    } else {
      GET_MAPPED_THEATRES_REPLACED = GET_MAPPED_THEATRES_REPLACED.replace("##userTheatreFilter##", "");
    }
    if (category.equals("SNP")) {
      String temp= GET_ALL_MAPPED_RESOURCES;
      if (centerId != 0 )
        temp = temp.replace("##centerFilter##",
              " AND  hcm.center_id= " + centerId );
        else {
          temp= temp.replace("##centerFilter##", " ");
        }
      try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(temp)) {
        ps.setString(1, schName);
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    if (category.equals("DIA")) {
 String temp= GET_ALL_MAPPED_TEST_RESOURCES;
          if (centerId != 0 )
          temp = temp.replace("##centerFilter##",
                " AND  hcm.center_id=" + centerId);
          else {
            temp= temp.replace("##centerFilter##", " ");
          }
          try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(temp)) {
        ps.setString(1, schName);
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    if (category.equals("OPE")) {
      if (centerId != 0) {
        GET_MAPPED_THEATRES_REPLACED = GET_MAPPED_THEATRES_REPLACED.replace("##centerFilter##",
            " AND  hcm.center_id= " + "?");
        
      } else {
        GET_MAPPED_THEATRES_REPLACED = GET_MAPPED_THEATRES_REPLACED.replace("##centerFilter##", " ");
      }
      try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(GET_MAPPED_THEATRES_REPLACED)) {
        ps.setString(1, schName);
        if(centerId !=0) {
          ps.setInt(2,centerId);
        }
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    return null;
  }

  private static final String GET_SECONDORY_RESOURCE_TYPES = "SELECT resource_type,resource_description FROM"
      + "  scheduler_resource_types  where category=? and primary_resource =false AND resource_type NOT IN('SUR','SER','THID','TST') AND resource_group is null "
      + "  UNION ALL"
      + "  SELECT scheduler_resource_type AS resource_type,resource_type_desc AS resource_description FROM generic_resource_type grt "
      + "  JOIN scheduler_resource_types srt ON(srt.resource_type = grt.scheduler_resource_type) WHERE category = ? AND resource_group = 'GEN'";
  private static final String GET_ALL_MAPPED_SERVICES = " SELECT s.service_id as resource_id, "
      +" s.service_name as resource_name, ssrm.serv_res_id as prim_res_id,"
      +" CASE WHEN service_code IS NULL OR service_code = '' THEN service_name || "
      +" '(' ||department || ')'  ELSE (service_name ||' - '||COALESCE(service_code,'') "
      +" || '(' ||department || ')') END AS resource_name_dept_code, s.service_code AS resource_code, "
      +" sd.department as resource_dept_name FROM service_service_resources_mapping ssrm "
      +" JOIN services s ON (s.service_id = ssrm.service_id) "
      +" JOIN services_departments sd ON(sd.serv_dept_id = s.serv_dept_id) WHERE s.status='A' ";
  
  private static final String GET_ALL_MAPPED_TEST = " SELECT d.test_id as resource_id, "
      +" d.test_name as resource_name, dtem.eq_id as prim_res_id,"
      +" CASE WHEN diag_code IS NULL OR diag_code = '' THEN test_name || "
      +" '(' ||ddept_name || ')'  ELSE (test_name ||' - '||COALESCE(diag_code,'') "
      +" || '(' ||ddept_name || ')') END AS resource_name_dept_code, d.diag_code AS resource_code, "
      +" dd.ddept_name as resource_dept_name FROM diagnostics_test_equipment_master_mapping dtem "
      +" JOIN diagnostics d ON (d.test_id = dtem.test_id) "
      +" JOIN diagnostics_departments dd ON(dd.ddept_id = d.ddept_id) WHERE d.status='A' "; 
  
  private static final String GET_ALL_MAPPED_SURGERY = "SELECT om.op_id as resource_id,  om.operation_name as resource_name,"
      + " otm.theatre_id as prim_res_id, CASE WHEN operation_code  IS NULL OR operation_code  = '' THEN "
      + "operation_name ||  '(' ||operation_code || ')'  ELSE (operation_code ||' - '||COALESCE(operation_code,'') "
      + " || '(' ||operation_name || ')') END AS resource_name_dept_code, om.operation_code AS resource_code,  dp.dept_name as resource_dept_name "
      + "FROM operation_theatre_mapping otm "
      + "JOIN operation_master om ON (om.op_id  = otm.operation_id)  "
      + "JOIN department dp ON(dp.dept_id = om.dept_id) WHERE dp.status='A' ";
  
  public static List<BasicDynaBean> getMappedSecondayResource(String category) throws SQLException {
    if (category.equals("SNP")) {
      try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(GET_ALL_MAPPED_SERVICES)) {
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    if (category.equals("DIA")) {
      try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(GET_ALL_MAPPED_TEST)) {
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    if (category.equals("OPE")) {
      try (Connection con = DataBaseUtil.getConnection();
          PreparedStatement ps = con.prepareStatement(GET_ALL_MAPPED_SURGERY)) {
        return DataBaseUtil.queryToDynaList(ps);
      }
    }
    return null;
  }

	public static List<BasicDynaBean> getSecondoryResourceTypes(ResourceCriteria rc)throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_SECONDORY_RESOURCE_TYPES);
		try{
			ps.setString(1,rc.category);
			ps.setString(2, rc.category);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private String GET_SECONDORY_RESOURCES_LIST =
		          " select * from (select d.doctor_id as resourceid, d.doctor_name as resourcename, 'SUDOC' as resourcetype,dcm.center_id AS center_id "+
				  "	from doctors d" +
				  " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
				  " where d.ot_doctor_flag = 'Y' AND d.dept_id !='DEP0002' AND d.status = 'A' AND dcm.status='A' AND d.schedule=true"+
				  " union all "+
				  "	select d.doctor_id as resourceid ,d.doctor_name as resourcename, 'ANEDOC' as resourcetype,dcm.center_id AS center_id " +
				  " from doctors d "+
				  " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
				  " where d.ot_doctor_flag = 'Y' AND d.dept_id ='DEP0002' AND d.status = 'A' AND dcm.status='A' AND d.schedule=true"+
				  "	union all "+
				  "	select d.doctor_id as resourceid ,d.doctor_name as resourcename, 'LABTECH' as resourcetype,dcm.center_id AS center_id "+
				  "	from doctors d" +
				  " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
				  " where  d.dept_id IN ('DEP_LAB','DEP_RAD') AND d.status = 'A' AND dcm.status='A' AND d.schedule=true" +
				  " union all " +
				  " select d.doctor_id as resourceid,d.doctor_name as resourcename,'ASUDOC' as resourcetype,dcm.center_id as center_id" +
				  " from doctors d" +
				  " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
				  " where d.ot_doctor_flag = 'Y' AND d.dept_id !='DEP0002' AND d.status = 'A' AND dcm.status='A' AND d.schedule=true" +
				  " union all " +
				  " select d.doctor_id as resourceid,d.doctor_name as resourcename,'PAEDDOC' as resourcetype,dcm.center_id as center_id" +
				  " from doctors d" +
				  " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
				  " where d.ot_doctor_flag = 'Y' AND d.status = 'A' AND dcm.status='A' AND d.schedule=true"+
				  "	union all "+
				  "	select eq_id::text as resourceid , equipment_name as resourcename, 'EQID' as resourcetype,center_id "+
				  "	from test_equipment_master where status= 'A' and schedule = true "+
				  "	union all "+
				  "	select d.doctor_id as resourceid, d.doctor_name as resourcename, 'DOC' as resourcetype,dcm.center_id AS center_id "+
				  "	from doctors d " +
				  " JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
				  " WHERE d.status = 'A' AND dcm.status='A' AND d.schedule=true" +
				  " union all " +
				  " select serv_res_id::text as resourceid,serv_resource_name as resourcename,'SRID' as resourcetype,center_id " +
				  " from service_resource_master WHERE status = 'A' and schedule = true "+
				  " union all "+
				  " select theatre_id as resourceid, theatre_name as resourcename, 'THID' as resourcetype,center_id "+
				  " from theatre_master where status ='A' and schedule = true  " +
				  " union all "+
				  " select generic_resource_id::text as resource_id,generic_resource_name as resourcename,grt.scheduler_resource_type as resource_type,center_id" +
				  " from generic_resource_master grm " +
				  " join generic_resource_type grt ON(grt.generic_resource_type_id = grm.generic_resource_type_id) " +
				  " where grm.status = 'A' AND grt.status = 'A' AND schedule = true) as r   where "+
				  " r.resourcetype IN ( SELECT  resource_type from scheduler_resource_types WHERE category = ?) # " +
				  " order by r.resourcename" ;

	public  List<BasicDynaBean> getResources(String category,int centerId)throws SQLException{
		List<BasicDynaBean> list = null;
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		int max_center_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		String dynamicQueryForCenters = "AND center_id IN (0,'"+centerId+"')";
		if(max_center_inc_default > 1) {
			if(centerId != 0) {
				GET_SECONDORY_RESOURCES_LIST = GET_SECONDORY_RESOURCES_LIST.replaceAll("#", dynamicQueryForCenters);
			} else {
				GET_SECONDORY_RESOURCES_LIST = GET_SECONDORY_RESOURCES_LIST.replaceAll("#", " ");
			}
		} else {
			GET_SECONDORY_RESOURCES_LIST = GET_SECONDORY_RESOURCES_LIST.replaceAll("#", " ");
		}
		try{
			ps = con.prepareStatement(GET_SECONDORY_RESOURCES_LIST);
			ps.setString(1, category);
			list = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	public static final String GET_PRIMARY_RESOURCE_TYPE = "SELECT resource_type from scheduler_resource_types where category = ? and primary_resource = true;";

	public static String getPrimaryResourceType(String category)throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_PRIMARY_RESOURCE_TYPE);

		try{
			ps.setString(1,category);

			return DataBaseUtil.getStringValueFromDb(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_RESOURCE_TYPE= "SELECT resource_type FROM scheduler_item_master " +
		"WHERE resource_id=? AND res_sch_id=?";

	public static final String GET_NEXT_APPOINT_ID = "SELECT nextval('scheduler_appointments_seq')";

	public static String getNextAppointMentId()throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_NEXT_APPOINT_ID);
		try{
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static final String GET_NEXT_UNIQUE_APPT_IND = "SELECT nextval('unique_appt_ind_seq')";

	public static int getNextUniqueAppointMentInd()throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_NEXT_UNIQUE_APPT_IND);
		try{
			return DataBaseUtil.getIntValueFromDb(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static int getNextUniqueAppointMentInd(Connection con)throws SQLException{

		PreparedStatement ps = con.prepareStatement(GET_NEXT_UNIQUE_APPT_IND);
		try{
			return DataBaseUtil.getIntValueFromDb(ps);
		}finally{
			ps.close();
		}

	}

	private static final String GET_APPOINTMENT_DETAILS = " SELECT ap.mr_no,ap.contact_id,ap.visit_id,ap.patient_name,ap.patient_contact, "+
				 " ap.complaint,srt.category,ap.remarks,ap.scheduler_visit_type,ap.prim_res_id, ap.waitlist, " +
				 " ap.presc_doc_id, (select doctor_name from doctors where doctor_id=ap.presc_doc_id) as presc_doctor, " +
				 " ap.cond_doc_id, (select doctor_name from doctors where doctor_id=ap.cond_doc_id) as cond_doctor, " +
				 " case when srt.category = 'DOC' then dc.dept_id end as dept_id,ap.center_id, " +
				 " ap.scheduler_prior_auth_no,ap.scheduler_prior_auth_mode_id," +
				 " case when srt.category='DIA' AND dd.category = 'DEP_LAB' then 'Laboratory' " +
				 " when srt.category='DIA' AND dd.category='DEP_RAD' then 'Radiology' " +
				 " when srt.category = 'SNP' then 'Service' end as item_type," +
				 " ap.appointment_id,ap.res_sch_id,ap.res_sch_name," +
				 " to_char(ap.appointment_time,'dd-MM-yyyy') AS text_appointment_date," +
				 " to_char(ap.appointment_time,'dd-MM-yyyy hh24:mi:ss') AS text_appointment_date_time," +
				 " to_char(ap.appointment_time,'hh24:mi')::time AS appointment_time," +
				 " to_char(ap.appointment_time,'hh24:mi') AS text_appointment_time,appointment_time as appointment_date_time," +
				 " to_char(appointment_time+(duration||' mins')::interval,'dd-MM-yyyy hh24:mi:ss') AS text_end_appointment_time,"+
				 " to_char(appointment_time+(duration||' mins')::interval,'hh24:mi') AS text_end_appointment_only_time," +
				 " ap.duration,ap.appointment_status,ap.arrival_time,ap.completed_time,ap.pat_package_id, "+
				 " ap.booked_by,ap.booked_time, b.payment_status, cas.paid_at_source, ap.changed_by,ap.changed_time,apit.resource_type,"+
				 " apit.resource_id,apit.appointment_item_id,srt.primary_resource," +
				 " CASE WHEN apit.resource_type ='SUDOC' THEN (select doctor_name from doctors where doctor_id=apit.resource_id ) " +
			     " WHEN apit.resource_type ='ANEDOC' THEN (select doctor_name from doctors where doctor_id=apit.resource_id ) "+
			     " WHEN apit.resource_type ='EQID' THEN (select equipment_name from test_equipment_master where eq_id::text=apit.resource_id ) "+
			     " WHEN apit.resource_type ='SRID' THEN (select serv_resource_name from service_resource_master where serv_res_id::text=apit.resource_id )"+
			     " WHEN apit.resource_type ='THID' THEN (select theatre_name from theatre_master where theatre_id=apit.resource_id )"+
			     " WHEN apit.resource_type ='OPDOC' THEN (select doctor_name from doctors where doctor_id=apit.resource_id )"+
			     " WHEN apit.resource_type ='DOC' THEN (select doctor_name from doctors where doctor_id=apit.resource_id )"+
			     " WHEN apit.resource_type ='LABTECH' THEN (select doctor_name from doctors where doctor_id=apit.resource_id ) " +
			     " ELSE (SELECT generic_resource_name FROM generic_resource_type grt " +
			     "          JOIN generic_resource_master grm ON(grm.generic_resource_type_id = grt.generic_resource_type_id)" +
			     "          AND grm.generic_resource_id::text = apit.resource_id " +
			     "			WHERE ap.appointment_id = apit.appointment_id AND grt.scheduler_resource_type = apit.resource_type)" +
			     " END AS resourcename, dcd.dept_name as doctor_department,"+
				 " COALESCE(dc.doctor_name,dg.test_name,op.operation_name,s.service_name) AS central_resource_name,consultation_type_id, " +
				 " COALESCE(dg.mandate_additional_info, 'N') as mandate_additional_info,  COALESCE(dg.additional_info_reqts, '') as additional_info_reqts" +
				 " FROM scheduler_appointments ap "+
				 " LEFT JOIN scheduler_appointment_items apit USING(appointment_id) "+
				 " LEFT JOIN doctors dc  ON dc.doctor_id = ap.prim_res_id "+
				 " LEFT JOIN department dcd ON(dc.dept_id=dcd.dept_id)"+
				 " LEFT JOIN diagnostics dg ON dg.test_id = ap.res_sch_name " +
				 " LEFT JOIN services s ON s.service_id = ap.res_sch_name " +
				 " LEFT JOIN operation_master op ON op.op_id = ap.res_sch_name " +
				 " LEFT JOIN diagnostics_departments dd ON dg.ddept_id = dd.ddept_id" +
				 " LEFT JOIN bill b ON (ap.bill_no = b.bill_no) " +
				 " LEFT JOIN appointment_source_master cas ON (cas.appointment_source_id = ap.app_source_id) " +
				 " LEFT JOIN  scheduler_master  sm ON sm.res_sch_id = ap.res_sch_id " +
				 " LEFT JOIN scheduler_resource_types srt ON (srt.category = sm.res_sch_category AND apit.resource_type = srt.resource_type)";

	private static final String GET_APPOINTMENT_DETAILS_WHERE_CLAUSE = " WHERE appointment_id  = ?";

	public static List<BasicDynaBean> getAppointmentDetails(int appointmentId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l =null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			logger.debug(GET_APPOINTMENT_DETAILS+GET_APPOINTMENT_DETAILS_WHERE_CLAUSE);
			ps = con.prepareStatement(GET_APPOINTMENT_DETAILS+GET_APPOINTMENT_DETAILS_WHERE_CLAUSE);
			ps.setInt(1,appointmentId);
			l = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	public static List<BasicDynaBean> getAppointmentDetails(Connection con,int appointmentId)throws SQLException{
		PreparedStatement ps = null;
		List<BasicDynaBean> l =null;
		try{
			logger.debug(GET_APPOINTMENT_DETAILS+GET_APPOINTMENT_DETAILS_WHERE_CLAUSE);
			ps = con.prepareStatement(GET_APPOINTMENT_DETAILS+GET_APPOINTMENT_DETAILS_WHERE_CLAUSE);
			ps.setInt(1,appointmentId);
			l = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
		return l;
	}

	private static final String GET_APPOINTMENT_DETAILS_BY_MR_NO = "SELECT " +
			" (SELECT CASE WHEN appointment_status='Noshow' " +
			" THEN count(appointment_status) else 0 end " +
			" FROM scheduler_appointments WHERE mr_no = ? AND appointment_status = 'Noshow'"+
			" group by appointment_status) AS count, " +
			" (SELECT CASE WHEN appointment_status='Noshow' " +
			" THEN count(appointment_status) else 0 end " +
			" FROM scheduler_appointments WHERE mr_no = ? AND appointment_status = 'Noshow'"
			+ " AND appointment_time::date > (CURRENT_DATE -INTERVAL '1 year')::date" +
			" group by appointment_status) AS lastyearnoshowcount, " +
			" (SELECT count(appointment_status) " +
			" FROM scheduler_appointments WHERE mr_no = ? "
			+ " AND appointment_time::date > (CURRENT_DATE -INTERVAL '1 year')::date " +
			" ) AS lastyeartotalcount, " +
			" appointment_status, visit_mode," +
			" TO_CHAR(date(appointment_time),'dd-MM-yyyy') AS appointment_date," +
			" appointment_time::time::text AS appointment_time,appointment_status, " +
			" sm.res_sch_type AS category, " +
			" CASE WHEN sm.res_sch_category = 'DOC' THEN doc.doctor_name " +
			" WHEN sm.res_sch_category = 'SNP' THEN ser.service_name " +
			" WHEN sm.res_sch_category = 'DIA' THEN diag.test_name " +
			" WHEN sm.res_sch_category = 'OPE' THEN ope.operation_name " +
			" END AS resouce_name, " +
			" CASE WHEN sm.res_sch_category = 'DOC' THEN 'Consultation' " +
			" WHEN sm.res_sch_category = 'SNP' THEN 'Service' " +
			" WHEN sm.res_sch_category = 'DIA' THEN 'Test' " +
			" WHEN sm.res_sch_category = 'OPE' THEN 'Surgery' " +
			" END AS schedule_type " +
			" FROM scheduler_appointments sa " +
			" LEFT JOIN scheduler_master sm ON (sm.res_sch_id = sa.res_sch_id) " +
			" LEFT JOIN doctors doc ON(sa.prim_res_id = doc.doctor_id) " +
			" LEFT JOIN operation_master ope ON(sa.res_sch_name = ope.op_id) " +
			" LEFT JOIN diagnostics diag ON(sa.res_sch_name = diag.test_id) " +
			" LEFT JOIN services ser ON(sa.res_sch_name = ser.service_id) " +
			" WHERE mr_no = ? and appointment_id != ? order by booked_time desc limit 1 ";

	public static Map getAppointmentDetailsByMrno(String mrNo, int appointmentId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> l =null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			logger.debug(GET_APPOINTMENT_DETAILS_BY_MR_NO);
			ps = con.prepareStatement(GET_APPOINTMENT_DETAILS_BY_MR_NO);
			ps.setString(1, mrNo);
			ps.setString(2, mrNo);
			ps.setString(3, mrNo);
			ps.setString(4, mrNo);
			ps.setInt(5, appointmentId);
			l = DataBaseUtil.queryToDynaList(ps);

			if (l != null && l.size() > 0) {
				BasicDynaBean b = (BasicDynaBean) l.get(0);
				return b.getMap();
			}
			return null;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_TIME_LIST = "SELECT  * " +
  		" FROM  scheduler_master sm WHERE  sm.res_sch_category = ? AND sm.res_sch_name =? AND res_sch_type=?" ;
	public static BasicDynaBean getScheduleDetails(String scheduleName, String category)
	   throws SQLException {
		  BasicDynaBean bean = null;
		  Connection con = DataBaseUtil.getReadOnlyConnection();
		  PreparedStatement ps = null;
		  String resource_type = null;
		  if (category.equals("DOC"))
			  resource_type = "DOC";
		  else if(category.equals("SNP"))
			  resource_type = "SER";
		  else if(category.equals("OPE"))
			  resource_type = "SUR";
		  else if(category.equals("DIA"))
			  resource_type = "TST";
	      try{
	    	  ps = con.prepareStatement(GET_TIME_LIST);
	    	  ps.setString(1, category);
	    	  ps.setString(2, "*");
	    	  ps.setString(3, resource_type);

	    	  bean = (BasicDynaBean)(DataBaseUtil.queryToDynaList(ps)).get(0);

	      }finally{
	    	  DataBaseUtil.closeConnections(con, ps);
	      }
		return bean;
	}

	public static final String APPOINTMENT_RESOURCES = "SELECT sc.resource_type, sc.resource_id,sa.res_sch_name," +
			" sc.appointment_item_id," +
			" sa.res_sch_id,srh.category,srh.resource_description " +
			" FROM scheduler_appointments  sa   join scheduler_master sm on  sm.res_sch_id = sa.res_sch_id" +
			" left join   scheduler_appointment_items sc on sc.appointment_id = sa.appointment_id" +
			" join scheduler_resource_types srh on srh.category = sm.res_sch_category" +
			" and sc.resource_type = srh.resource_type" +
			" where  sa.appointment_id=?" +
			" union" +
			" SELECT srh.resource_type, '' as resource_id,'' as res_sch_name, 0 as appointment_item_id,sa.res_sch_id,srh.category," +
			" srh.resource_description" +
			" FROM scheduler_appointments  sa   join scheduler_master sm on  sm.res_sch_id = sa.res_sch_id" +
			" left join   scheduler_appointment_items sc on sc.appointment_id = sa.appointment_id" +
			" join scheduler_resource_types srh on srh.category = sm.res_sch_category" +
			" and srh.resource_type not in (select resource_type from scheduler_appointment_items where appointment_id=?)" +
			" where  sa.appointment_id=? order by appointment_item_id desc";

	public static List getAppointmentResources(int appt_id)throws SQLException {
		List applist = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(APPOINTMENT_RESOURCES);
			ps.setInt(1,appt_id);
			ps.setInt(2,appt_id);
			ps.setInt(3,appt_id);
			//applist = DataBaseUtil.queryToDynaList(ps);
			applist = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return applist;
	}

	public static final String STATUS_LIST = "SELECT * from scheduler_status";

	public static List getStatusList()throws SQLException {
		List statuslist = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(STATUS_LIST);
			statuslist = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return statuslist;
	}

	public static final String RESOURCE_TYPES = "select * from scheduler_resource_types"
	    + " where resource_type NOT IN('TST','SUR','SER') @ ";
	public  List getResourceTypes(boolean needPrimaryResource) throws SQLException {
		List resourceTypelist = null;
		Connection con = null;
		PreparedStatement ps = null;
		String query = RESOURCE_TYPES;
		try{
			con = DataBaseUtil.getConnection();
			if(!needPrimaryResource) {
			  query = query.replace("@", "AND primary_resource =false");
			} else {
			  query = query.replace("@", "");
			}
			ps = con.prepareStatement(query);
			resourceTypelist = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return resourceTypelist;
	}



	public static final String UPDATE_STATUS = "update scheduler_appointments set appointment_status = ?,changed_by = ? where appointment_id=?";

	public static final String UPDATE_STATUS_COMPLETED = "update scheduler_appointments set appointment_status = ?, completed_time = ?,changed_by = ? where appointment_id=?";

	public static final String UPDATE_STATUS_ARRIVED = "update scheduler_appointments set appointment_status = ? , arrival_time=?,changed_by = ?, changed_time = ? where appointment_id=?";

  public boolean updateStatus(String status, int appointment_id, String userName)
      throws SQLException {
    boolean success = true;
    int result = 0;
    // For all categories the arrival time is updated
    if (status.equals("Arrived")) {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS_ARRIVED)) {
        ps.setString(1, status);
        ps.setTimestamp(2, DataBaseUtil.getDateandTime());
        ps.setString(3, userName);
        ps.setTimestamp(4, DataBaseUtil.getDateandTime());
        ps.setInt(5, appointment_id);
        result = ps.executeUpdate();
      }
    } else if (status.equals("Completed")) {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS_COMPLETED)) {
        ps.setString(1, status);
        ps.setTimestamp(2, DataBaseUtil.getDateandTime());
        ps.setString(3, userName);
        ps.setInt(4, appointment_id);
        result = ps.executeUpdate();
      }
    } else {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
        ps.setString(1, status);
        ps.setString(2, userName);
        ps.setInt(3, appointment_id);
        result = ps.executeUpdate();
      }
    }
    if (result > 0)
      success = true;
    else
      success = false;
    return success;
  }
	//using same connection.
  public boolean updateStatus(Connection con, String status, int appointment_id, String userName)
      throws SQLException {
    boolean success = true;
    int result = 0;
    // For all categories the arrival time is updated
    if (status.equals("Arrived")) {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS_ARRIVED)) {
        ps.setString(1, status);
        ps.setTimestamp(2, DataBaseUtil.getDateandTime());
        ps.setString(3, userName);
        ps.setTimestamp(4, DataBaseUtil.getDateandTime());
        ps.setInt(5, appointment_id);
        result = ps.executeUpdate();
      }
    } else if (status.equals("Completed")) {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS_COMPLETED)) {
        ps.setString(1, status);
        ps.setTimestamp(2, DataBaseUtil.getDateandTime());
        ps.setString(3, userName);
        ps.setInt(4, appointment_id);
        result = ps.executeUpdate();
      }
    } else {
      try (PreparedStatement ps = con.prepareStatement(UPDATE_STATUS)) {
        ps.setString(1, status);
        ps.setString(2, userName);
        ps.setInt(3, appointment_id);
        result = ps.executeUpdate();
      }
    }
    if (result > 0)
      success = true;
    else
      success = false;
    return success;
  }

	private static String APPOINTMENTS_FIELDS = " SELECT *  ";

	private static String APPOINTMENTS_COUNT = " SELECT count(*) ";

	private  String APPOINTMENTS_TABLES = "  FROM " +
			" (SELECT sch.appointment_id,sch.package_id, sch.mr_no, sch.res_sch_id, sch.visit_id, sch.waitlist::text, sch.appt_token, sch.visit_mode, "
			+ "sch.app_source_id, " +
			"	sch.remarks, coalesce((SELECT credit_bill_exists FROM op_bill_and_discharge_status_view WHERE visit_id = sch.visit_id)," +
			"			 (SELECT credit_bill_exists FROM adt_bill_and_discharge_status_view WHERE visit_id = sch.visit_id)) AS credit_bill_exists," +
			"	coalesce((SELECT bill_status_ok FROM op_bill_and_discharge_status_view WHERE visit_id = sch.visit_id)," +
			"			 (SELECT bill_status_ok FROM adt_bill_and_discharge_status_view WHERE visit_id = sch.visit_id)) AS bill_status_ok," +
			"	coalesce((SELECT payment_ok FROM op_bill_and_discharge_status_view WHERE visit_id = sch.visit_id)," +
			"			 (SELECT payment_ok FROM adt_bill_and_discharge_status_view WHERE visit_id = sch.visit_id)) AS payment_ok," +
			"	coalesce(sal.salutation,'') || '' || coalesce(pd.patient_name, sch.patient_name) " +
			"	|| ' ' || coalesce(pd.middle_name, '') || ' ' || coalesce(pd.last_name, '') AS patient_name," +
			"	patient_contact,patient_contact_country_code,  sch.complaint, cas.appointment_source_name as app_source_name, " +
			"   sch.presc_doc_id, (select doctor_name from doctors where doctor_id=sch.presc_doc_id) as presc_doctor, " +
			"	sm.res_sch_category,  sm.res_sch_category AS resFilter,  "
			+ " (CASE WHEN sch.res_sch_id = 1 THEN sch.prim_res_id ELSE sch.res_sch_name END) AS resource," +
			"	appointment_time::date AS appoint_date, appointment_time::time AS appoint_time," +
			" to_char(discharge_date, 'dd-MM-yyyy') ||' '|| to_char(discharge_time, 'HH24:MI') AS visit_closing_date, " +
			"	sch.duration,  appointment_status AS appoint_status,  arrival_time,  completed_time,  " +
			"	booked_by, booked_time, changed_by, changed_time, st.status_description,  " +
			"   sai.resource_id as booked_resource_id,  " +
			"   COALESCE(doc.doctor_name, eqp.equipment_name, th.theatre_name, res.serv_resource_name) " +
			"    as booked_resource, " +
			" case when sch.appointment_status='Arrived' then cst.consultation_type else '' end as ordered_consultation, " +
			"	cst.consultation_type, bn.bed_name,wn.ward_name, " +
			" case when sm.res_sch_category='DOC' then (select consultation_type from consultation_types where " +
			" consultation_type_id=sch.res_sch_name::Integer) ELSE '' END as appointment_consultation, " +
			"	CASE WHEN sch.res_sch_name LIKE 'OPID%' THEN (SELECT dept_name FROM operation_master om " +
			" 	JOIN department d ON (d.dept_id = om.dept_id) where op_id = sch.res_sch_name) " +
			"   WHEN sch.res_sch_name LIKE 'SERV%' THEN (SELECT department FROM services s " +
			" 	JOIN services_departments sd ON(sd.serv_dept_id = s.serv_dept_id)  where service_id = sch.res_sch_name)" +
			"   WHEN sch.res_sch_name LIKE 'DGC%' THEN  (SELECT ddept_name FROM diagnostics d " +
			" 	JOIN diagnostics_departments dd on (dd.ddept_id = d.ddept_id) where test_id = sch.res_sch_name) " +
			" 	ELSE  (SELECT dept_name FROM doctors doc JOIN department d ON (d.dept_id=doc.dept_id) where doctor_id = sch.prim_res_id)" +
			"   END AS appointment_department, " +
			"	CASE WHEN sch.res_sch_name LIKE 'OPID%' THEN (SELECT dept_type_id FROM operation_master om " +
			" 	JOIN department d ON (d.dept_id = om.dept_id) where op_id = sch.res_sch_name) " +
			"   WHEN sch.res_sch_name LIKE 'SERV%' THEN (SELECT dept_type_id FROM services s " +
			" 	JOIN services_departments sd ON(sd.serv_dept_id = s.serv_dept_id)  where service_id = sch.res_sch_name)" +
			" 	ELSE  (SELECT dept_type_id FROM doctors doc JOIN department d ON (d.dept_id=doc.dept_id) where doctor_id = sch.prim_res_id)" +
			"   END AS department_type, " +
			"	CASE WHEN sch.res_sch_name LIKE 'OPID%' THEN (SELECT operation_name FROM operation_master where op_id = sch.res_sch_name)" +
			"	     WHEN sch.res_sch_name LIKE 'SERV%' THEN (SELECT service_name|| '(' ||department || ')' FROM services s " +
			"													JOIN services_departments sd ON(sd.serv_dept_id = s.serv_dept_id) " +
			"													 where service_id = sch.res_sch_name)" +
			"        WHEN sch.res_sch_name LIKE 'DGC%' THEN  (SELECT test_name FROM diagnostics where test_id = sch.res_sch_name) " +
			"	 END AS res_sch_name,sch.center_id,dc.consultation_token,doc.chanelling @" +
			" FROM scheduler_appointments sch  " +
			" LEFT JOIN doctor_consultation dc ON(sch.appointment_id=dc.appointment_id) "+
			" JOIN scheduler_master sm ON (sch.res_sch_id = sm.res_sch_id)" +
			" JOIN scheduler_resource_types srt ON (srt.primary_resource=true " +
			"   AND srt.category = sm.res_sch_category) " +
			" LEFT JOIN scheduler_status st ON (st.status_name = sch.appointment_status AND st.category = sm.res_sch_category)" +
			" LEFT JOIN patient_details pd ON (pd.mr_no = sch.mr_no) " +
			" LEFT JOIN patient_registration pr ON (pr.patient_id = sch.visit_id) " +
			" LEFT JOIN salutation_master sal ON (sal.salutation_id = pd.salutation) " +
			" LEFT JOIN admission adm ON (sch.visit_id = adm.patient_id)" +
			" LEFT JOIN bed_names bn ON (bn.bed_id = adm.bed_id) " +
			" LEFT JOIN ward_names wn ON (bn.ward_no = wn.ward_no) " +
			" LEFT JOIN consultation_types cst ON (sch.consultation_type_id = cst.consultation_type_id) #" +
			" LEFT JOIN scheduler_appointment_items sai ON ( " +
			"  sai.appointment_id = sch.appointment_id AND sai.resource_type = srt.resource_type) " +
			" LEFT JOIN doctors doc ON (doc.doctor_id = sai.resource_id)  " +
			" LEFT JOIN theatre_master th ON (th.theatre_id = sai.resource_id) " +
			" LEFT JOIN test_equipment_master eqp ON (eqp.eq_id::text = sai.resource_id AND sai.resource_type = 'EQID')  " +
			" LEFT JOIN service_resource_master res ON (res.serv_res_id::text = sai.resource_id AND sai.resource_type = 'SRID') " +
			" LEFT JOIN appointment_source_master cas ON (cas.appointment_source_id = sch.app_source_id)  " +
			" WHERE ( ##userTheatre## CASE  WHEN (sch.mr_no IS NOT NULL AND sch.mr_no != '') THEN patient_confidentiality_check(pd.patient_group,pd.mr_no)  ELSE true END))  as foo ";

	public  PagedList getTodayAppointments(Map map, Map pagingParams, int centerId, String[] appointStatus, boolean paid, boolean unpaid,String userName,int roleId)
			throws Exception, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		List<Integer> centerList = new ArrayList<Integer>();
		centerList.add(centerId);
    	String JoinQuery = " CASE WHEN (sch.res_sch_id=2) THEN (sch.prim_res_id in (select theatre_id from user_theatres ut where ut.emp_username='##userName##')) ELSE TRUE END  AND";
    
		try {
		  if ((roleId != 1) && (roleId != 2)) {
		    APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("##userTheatre##", JoinQuery);
		    APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("##userName##", userName);
		  } else{
		    APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("##userTheatre##", "");
		  }
			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) == 1) {
				APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("@", "");
				APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("#", "");
			} else {
				APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("@", ",hcm.center_name");
				APPOINTMENTS_TABLES = APPOINTMENTS_TABLES.replace("#", "JOIN hospital_center_master hcm ON(sch.center_id = hcm.center_id)");
			}

			SearchQueryBuilder qb = new SearchQueryBuilder(con, APPOINTMENTS_FIELDS,
					APPOINTMENTS_COUNT, APPOINTMENTS_TABLES, pagingParams);

			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) == 1) {
			} else {
				if(centerId != 0) {
					qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "IN", centerList);
				}
				qb.addSecondarySort("center_name",false);
			}
			String[] patientContact = (String[])map.get("patient_contact");
			map.remove("patient_contact"); // We need custom filter for patient contact	, So removing this from default filter				
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("appoint_date", false);
			qb.addSecondarySort("appoint_time", false);
			qb.addSecondarySort("res_sch_category", false);
			qb.addSecondarySort("appt_token");
			qb.addSecondarySort("consultation_token");
			qb.addSecondarySort("patient_name", false);
			//Filter patient contact , We do exact match and also  match without country code of mobile number
			if(patientContact != null && patientContact.length > 0 && !patientContact[0].trim().isEmpty()){
				ArrayList<Integer> mobileNumTypeList = new ArrayList<Integer>();
				mobileNumTypeList.add(SearchQueryBuilder.STRING);
				mobileNumTypeList.add(SearchQueryBuilder.STRING);
				ArrayList<String> mobileNumValuesList = new ArrayList<String>();
				mobileNumValuesList.add(patientContact[0]);
				mobileNumValuesList.add(patientContact[0]);
				String whereClause = qb.getWhereClause();
				if(whereClause == null || whereClause.equals("")) {
					qb.appendExpression(" WHERE ( patient_contact = ? OR regexp_replace(patient_contact,'^\\' || patient_contact_country_code ,'') = ? ) ", mobileNumTypeList, mobileNumValuesList);
				} else {
					qb.appendExpression(" AND ( patient_contact = ? OR regexp_replace(patient_contact,'^\\' || patient_contact_country_code ,'') = ? ) ", mobileNumTypeList, mobileNumValuesList);
				}
				
			}
			if(appointStatus != null && appointStatus.length > 0 && appointStatus[0] != null && !appointStatus[0].equals("") ) {
				ArrayList types = new ArrayList();
				ArrayList values = new ArrayList();
                String whereClause = qb.getWhereClause();
                String exp = "";
                if(whereClause == null || whereClause.equals("")) {
                	exp = exp + " WHERE ( (appoint_status in (?";
                } else {
				    exp = " AND ( (appoint_status in (?"; //?)) ";
                }
				types.add(qb.STRING);
				values.add(appointStatus[0]);
				for(int i=1; i<appointStatus.length; i++) {
					exp = exp + ",?";
					types.add(qb.STRING);
					values.add(appointStatus[i]);
				}
				  exp = exp + "))";
			    exp = exp + ")";
			    qb.appendExpression(exp, types, values);
			}
			qb.build();

			PagedList l = qb.getMappedPagedList();

			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static String APPOINTMENTS_DATES_FIELDS = " SELECT to_char(app_date::date, 'dd-MM-yyyy') as app_date  ";

	private static String APPOINTMENTS_DATES_COUNT = " SELECT count(app_date::date) ";

	private  String APPOINTMENTS_DATES_TABLES = "  from generate_series(?,    ?, '1 day'::interval) app_date ";

	public PagedList getDocAppointmentDates(Date startDate, Date endDate, Map pagingParams) throws Exception, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con, APPOINTMENTS_DATES_FIELDS,
				APPOINTMENTS_DATES_COUNT, APPOINTMENTS_DATES_TABLES, pagingParams);

		try {
			qb.addInitValue(SearchQueryBuilder.DATE, startDate);
			qb.addInitValue(SearchQueryBuilder.DATE, endDate);
			qb.build();
			PagedList l = qb.getMappedPagedList();

			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}


	private static String DOC_APPOINTMENTS_FIELDS = " SELECT * ";

	private  String DOC_APPOINTMENTS_TABLES = "  FROM (SELECT ap.mr_no,ap.visit_id,ap.patient_name, ap.visit_mode,ap.patient_contact, ap.complaint, ap.appointment_id,  " +
	"ap.appointment_time::date AS appoint_date, to_char(ap.appointment_time::date, 'dd-MM-yyyy') AS appoint_date_txt, ap.appointment_time, pdc.doctor_name as presc_doctor, pdc.doctor_id as presc_doc_id, ap.appt_token, ap.bill_no," +
	"to_char(ap.appointment_time,'hh24:mi') AS text_appointment_time, ap.app_source_id, ap.remarks, b.bill_type," +
	"to_char(appointment_time+(ap.duration||' mins')::interval,'hh24:mi') AS text_end_appointment_time, "+
	"ap.duration, dc.doctor_name, coalesce(dc.doctor_id, sai.resource_id) as doctor_id, dc.dept_id, dcd.dept_name as appointment_department, ap.appointment_status, ct.consultation_type, " +
	"ap.center_id , coalesce(b.payment_status,' ') AS payment_status, casm.paid_at_source, ap.res_sch_id, casm.appointment_source_name as appt_source_name @ " +
	"FROM scheduler_appointments ap " +
	" LEFT JOIN patient_details pd ON (pd.mr_no = ap.mr_no) " +
	"LEFT JOIN  scheduler_master  sm ON sm.res_sch_id = ap.res_sch_id " +
	"LEFT JOIN (select distinct resource_id, appointment_id from scheduler_appointment_items where resource_type IN ('SUDOC', 'PAEDDOC', 'DOC', 'LABTECH', 'ANEDOC', 'ASUDOC')) " +
	" as sai on sai.appointment_id = ap.appointment_id " +
	"LEFT JOIN doctors dc  ON dc.doctor_id = ap.prim_res_id " +
	"LEFT JOIN doctors pdc ON pdc.doctor_id = ap.presc_doc_id " +
	"LEFT JOIN department dcd ON(dc.dept_id=dcd.dept_id) " +
	"LEFT JOIN bill b ON (ap.bill_no = b.bill_no) " +
	"LEFT JOIN appointment_source_master casm on (ap.app_source_id = casm.appointment_source_id) " +
	"LEFT JOIN consultation_types ct using(consultation_type_id) #"+
	" WHERE (CASE WHEN (ap.mr_no IS NOT NULL AND ap.mr_no != '') THEN patient_confidentiality_check(pd.patient_group,pd.mr_no)  ELSE true END)) as foo ";

	public List getDocAppointments(Map map, int centerId, String[] appointStatus, boolean paid, boolean unpaid)//String[] channelStatus)
	throws Exception, ParseException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		List<Integer> centerList = new ArrayList<Integer>();
		centerList.add(centerId);
		try {
			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) == 1) {
				DOC_APPOINTMENTS_TABLES = DOC_APPOINTMENTS_TABLES.replace("@", "");
				DOC_APPOINTMENTS_TABLES = DOC_APPOINTMENTS_TABLES.replace("#", "");
			} else {
				DOC_APPOINTMENTS_TABLES = DOC_APPOINTMENTS_TABLES.replace("@", ",hcm.center_name");
				DOC_APPOINTMENTS_TABLES = DOC_APPOINTMENTS_TABLES.replace("#", "JOIN hospital_center_master hcm ON(ap.center_id = hcm.center_id)");
			}

			SearchQueryBuilder qb = new SearchQueryBuilder(con, DOC_APPOINTMENTS_FIELDS,
					null, DOC_APPOINTMENTS_TABLES);

			if ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) == 1) {
			} else {
				if(centerId != 0) {
					qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "IN", centerList);
				}
				qb.addSecondarySort("center_name",false);
			}

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("appoint_date", false);
			qb.addSecondarySort("appointment_time", false);
			qb.addSecondarySort("appt_token");
			qb.addSecondarySort("text_appointment_time", false);
			qb.addSecondarySort("patient_name", false);
			if(appointStatus != null && appointStatus.length > 0 && appointStatus[0] != null && !appointStatus[0].equals("") ) {
				ArrayList types = new ArrayList();
				ArrayList values = new ArrayList();
				String exp = " AND ( (appointment_status in (?"; //?)) ";
				types.add(qb.STRING);
				values.add(appointStatus[0]);
				for(int i=1; i<appointStatus.length; i++) {
					exp = exp + ",?";
					types.add(qb.STRING);
					values.add(appointStatus[i]);
				}
				exp = exp + "))";
			    exp = exp + ")";
			    qb.appendExpression(exp, types, values);
			}
			qb.build();
			List l = DataBaseUtil.queryToDynaList(qb.getDataStatement());

			qb.close();

			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	/**
	*  INSERT,UPDATE and DELETE of Resources
	*/

	public static final String INSERT_RESOURCE = "INSERT INTO scheduler_appointment_items(appointment_id,resource_type,resource_id, user_name, mod_time) " +
			" values (?,?,?,?,?)";

	/*
	* Insert a list of Resources
	*/
	public boolean insertResources(List list) throws SQLException {
	  boolean success = true;
	try(PreparedStatement ps = con.prepareStatement(INSERT_RESOURCE)){
	  Iterator iterator = list.iterator();
	  while (iterator.hasNext()) {
	    ResourceDTO rdto =  (ResourceDTO) iterator.next();
	    ps.setInt(1, rdto.getAppointmentId());
	    ps.setString(2, rdto.getResourceType());
	    ps.setString(3, rdto.getResourceId());
	    ps.setString(4, rdto.getUser_name());
	    ps.setTimestamp(5, rdto.getMod_time());
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


	public static final String UPDATE_RESOURCE = "UPDATE scheduler_appointment_items SET resource_id=?,user_name=?, mod_time = ? WHERE " +
			" appointment_id=? and resource_type=? and appointment_item_id=?";

	/*
	* Update a list of Resources
	*/
  public boolean updateResources(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_RESOURCE)) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        ResourceDTO rdto = (ResourceDTO) iterator.next();
        ps.setString(1, rdto.getResourceId());
        ps.setString(2, rdto.getUser_name());
        ps.setTimestamp(3, rdto.getMod_time());
        ps.setInt(4, rdto.getAppointmentId());
        ps.setString(5, rdto.getResourceType());
        ps.setInt(6, rdto.getAppointment_item_id());
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

	private static final String DELETE_RESOURCE = "DELETE FROM scheduler_appointment_items WHERE appointment_id=? " +
			"and resource_id=? and resource_type=?";

	/*
	* Delete a list of Resources
	*/
  public boolean deleteResources(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(DELETE_RESOURCE)) {

      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        ResourceDTO rdto = (ResourceDTO) iterator.next();
        ps.setInt(1, rdto.getAppointmentId());
        ps.setString(2, rdto.getResourceId());
        ps.setString(3, rdto.getResourceType());
        ps.addBatch();
      }
      ps.executeBatch();
    }
    return success;
  }

	private static final String THEATRES = "SELECT theatre_id,theatre_name,overbook_limit,center_id FROM theatre_master WHERE status='A' and schedule = true ";

	private static final String EQUIPMENTS = "SELECT eq_id::text,equipment_name,'' AS dept_id,overbook_limit,center_id from test_equipment_master where status='A' and schedule = true ";

	private static final String BEDS = "SELECT bed_type,bed_name,bed_id FROM bed_names WHERE status='A'";

	private static final String SERVICES = "SELECT service_id, service_name, serv_dept_id AS dept_name FROM services ser" +
			" LEFT JOIN services_departments sd USING(serv_dept_id) "+
			" WHERE ser.status='A' ORDER BY service_name";

	private static final String TESTS = "SELECT test_id,test_name,d.ddept_id,category FROM diagnostics d " +
			" LEFT JOIN diagnostics_departments dd ON(d.ddept_id = dd.ddept_id and category in ('DEP_LAB','DEP_RAD'))" +
			" WHERE d.status='A' and dd.status='A' ORDER BY test_name";

	private static final String OPERATIONS = "SELECT op_id,operation_name,dept_id FROM operation_master WHERE status='A'";

	private static final String SCHEDULERDOCTORS = "SELECT d.doctor_id, d.doctor_name, d.dept_id, d.ot_doctor_flag," +
			" (case when schedule=true then 'T' else 'F' end ) as schedule, overbook_limit, dcm.center_id  " +
			" FROM doctors d" +
			" JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
			" WHERE d.status = 'A' and dcm.status='A' order by doctor_name";

	private static final String LABTECHNICIANS = "SELECT d.doctor_id, d.doctor_name, d.dept_id, d.ot_doctor_flag,dcm.center_id FROM doctors d" +
			" JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
			" WHERE d.status = 'A' and d.dept_id in ('DEP_LAB','DEP_RAD') AND d.schedule = true order by doctor_name";

	private static final String GENERIC_RESOURCES = "SELECT *,'' as dept_id,(case when schedule=true then 'T' else 'F' end ) as scheduleable,center_id" +
			" FROM generic_resource_master grm" +
			" JOIN generic_resource_type grt ON(grt.generic_resource_type_id = grm.generic_resource_type_id) " +
			" WHERE grm.status = 'A' AND grt.status ='A' order by generic_resource_name";


	public static List getResourceMasterList(String category) throws SQLException {
		List list = null;
		if(category.equals("DOC")) {
			list = DataBaseUtil.queryToArrayList(SCHEDULERDOCTORS);
		}else if(category.equals("THID")) {
			list = DataBaseUtil.queryToArrayList(THEATRES);
		}else if(category.equals("EQID")){
			list = DataBaseUtil.queryToArrayList(EQUIPMENTS);
		}else if(category.equals("BED")){
			list = DataBaseUtil.queryToArrayList(BEDS);
		}else if(category.equals("OPID")){
			list = DataBaseUtil.queryToArrayList(OPERATIONS);
		}else if(category.equals("DGC")){
			list = DataBaseUtil.queryToArrayList(TESTS);
		}else if(category.equals("SERV")){
			list = DataBaseUtil.queryToArrayList(SERVICES);
		}else if(category.equals("LABTECH")){
			list = DataBaseUtil.queryToArrayList(LABTECHNICIANS);
		} else if(category.equals("GEN")) {
			list = DataBaseUtil.queryToArrayList(GENERIC_RESOURCES);
		}
		return list;
	}

	private static final String CENTER_GENERIC_RESOURCES = "SELECT *,'' as dept_id,(case when schedule=true then 'T' else 'F' end ) as scheduleable" +
			" FROM generic_resource_master grm" +
			" JOIN generic_resource_type grt ON(grt.generic_resource_type_id = grm.generic_resource_type_id) " +
			" WHERE grm.status = 'A' AND grt.status ='A' # order by generic_resource_name";

	private static final String CENTER_THEATRES = "SELECT thm.theatre_id,thm.theatre_name,thm.overbook_limit,thm.center_id FROM theatre_master thm ##userTheatreFilter## " +
			"	WHERE status='A' and schedule = true # order by theatre_name";

	private static final String CENTER_EQUIPMENTS = "SELECT eq_id::text,equipment_name,'' AS dept_id,overbook_limit,center_id from test_equipment_master " +
			"	where status='A' and schedule = true # order by equipment_name";

	public List getCenterResourceMasterList(String category,int centerId,String userName,int roleId) throws Exception{
		List list = null;
		Connection con = null;
		PreparedStatement ps = null;
		String centerClause = "AND center_id = ?";
	  String centerResources = "";
	  String CENTER_THEATRES_REPLACED =CENTER_THEATRES;
    String JoinQuery = "join user_theatres ut ON( thm.theatre_id = ut.theatre_id and ut.emp_username = '##userName##')";
    if ((roleId != 1) && (roleId != 2)) {
      CENTER_THEATRES_REPLACED = CENTER_THEATRES_REPLACED.replace("##userTheatreFilter##",
          JoinQuery);
      CENTER_THEATRES_REPLACED = CENTER_THEATRES_REPLACED.replace("##userName##", userName);
    } else {
      CENTER_THEATRES_REPLACED = CENTER_THEATRES_REPLACED.replace("##userTheatreFilter##", "");
    }
		if(category.equals("THID")) {
		  centerResources = CENTER_THEATRES_REPLACED;
		} else if(category.equals("EQID")){
		  centerResources = CENTER_EQUIPMENTS;
		} else if(category.equals("GEN")) {
		  centerResources = CENTER_GENERIC_RESOURCES;
		}

		try {
			con = DataBaseUtil.getConnection();
			if (((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"))) == 1) {
			  centerResources = centerResources.replace("#", "");
				ps = con.prepareStatement(centerResources);
			} else {
				if(centerId != 0) {
				  centerResources = centerResources.replace("#", centerClause);
					ps = con.prepareStatement(centerResources);
					ps.setInt(1, centerId);
				} else {
				  centerResources = centerResources.replace("#", "");
					ps = con.prepareStatement(centerResources);
				}
			}
			list = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	private String CONSULTATION_DEFAULT_HEADERS = "SELECT count(res_sch_category) AS cnt,ct.resource_id,ct.res_sch_name FROM " +
			" (SELECT res_sch_category, date(sa.appointment_time) AS aptime,d.doctor_id AS resource_id,d.doctor_name AS res_sch_name" +
			"  FROM doctors d" +
			"  JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
			"  LEFT JOIN scheduler_appointments sa ON sa.prim_res_id = d.doctor_id AND  date(sa.appointment_time) = current_date " +
			"  LEFT JOIN scheduler_master sm ON sm.res_sch_id = sa.res_sch_id  AND res_sch_category = 'DOC' " +
			"  WHERE d.status='A' AND dcm.status='A' AND d.schedule = true @ AND d.dept_id IN (#) ) AS ct" +
			"  GROUP BY ct.resource_id,ct.res_sch_name ORDER BY ct.res_sch_name,cnt LIMIT 5";

	private static String SURGERY_DEFAULT_HEADERS = " SELECT count(res_sch_category) as cnt,ct.resource_id,ct.res_sch_name FROM"
      + " (SELECT distinct res_sch_category, sa.appointment_id, date(sa.appointment_time) AS aptime,e.theatre_id AS resource_id,e.theatre_name AS res_sch_name "
      + " FROM theatre_master e"
      + " LEFT JOIN scheduler_appointments sa ON sa.prim_res_id = e.theatre_id AND date(sa.appointment_time) = current_date "
      + " LEFT JOIN scheduler_master sm ON sm.res_sch_id = sa.res_sch_id  AND res_sch_category = 'OPE' "
      + " LEFT JOIN user_theatres ut on ut.theatre_id =e.theatre_id "
      + " WHERE e.status='A' AND e.schedule = true & #) AS ct"
      + " GROUP BY ct.resource_id,ct.res_sch_name ORDER BY cnt DESC LIMIT 5";

	private String SERVICE_DEFAULT_HEADERS = "SELECT count(res_sch_category) as cnt,ct.resource_id,ct.res_sch_name from " +
		" (SELECT res_sch_category, date(sa.appointment_time) as aptime,e.serv_res_id::text AS resource_id,e.serv_resource_name as res_sch_name" +
		" FROM service_resource_master e " +
		" LEFT JOIN scheduler_appointments sa on sa.prim_res_id = e.serv_res_id::text AND date(sa.appointment_time)  = current_date " +
		" LEFT JOIN scheduler_master sm ON sm.res_sch_id = sa.res_sch_id   AND res_sch_category = 'SNP' " +
		" WHERE e.status='A' and e.schedule = true #) AS ct" +
		" GROUP BY ct.resource_id,ct.res_sch_name ORDER BY cnt DESC LIMIT 5";

	private String TESTS_DEFAULT_HEADERS = "SELECT count(res_sch_category) as cnt,ct.resource_id,ct.res_sch_name from " +
		" (SELECT res_sch_category, date(sa.appointment_time) as aptime,e.eq_id::text AS resource_id,e.equipment_name as res_sch_name" +
		" FROM test_equipment_master e " +
		" LEFT JOIN scheduler_appointments sa on prim_res_id = e.eq_id::text AND date(sa.appointment_time)  = current_date " +
		" LEFT JOIN scheduler_master sm ON sm.res_sch_id = sa.res_sch_id AND res_sch_category = 'DIA' " +
		" WHERE e.status='A' and e.schedule = true #) as ct " +
		" GROUP BY ct.resource_id,ct.res_sch_name ORDER BY cnt DESC LIMIT 5;";

	public List getDefaultHeaders(String category, String department, int centerId, String userName, int roleId) throws SQLException {
		List defaultlist = null;
		Connection con = null;
		PreparedStatement ps = null;
		String centerClause = "AND e.center_id = ?";
		String SURGERY_DEFAULT_HEADERS_REPLACED = SURGERY_DEFAULT_HEADERS;
		int index = 1;
    if ((roleId != 1) && (roleId != 2)) {
      SURGERY_DEFAULT_HEADERS_REPLACED = SURGERY_DEFAULT_HEADERS_REPLACED.replace("&",
          "and ut.emp_username =  ?");
    } else {
      SURGERY_DEFAULT_HEADERS_REPLACED = SURGERY_DEFAULT_HEADERS_REPLACED.replace("&", "");
    }
		try{
			con = DataBaseUtil.getConnection();
			if(category.equals("DOC")){

				if((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
					if(centerId != 0)
						CONSULTATION_DEFAULT_HEADERS = CONSULTATION_DEFAULT_HEADERS.replaceAll("@", " AND dcm.center_id IN (0,"+centerId+")");
					else
						CONSULTATION_DEFAULT_HEADERS = CONSULTATION_DEFAULT_HEADERS.replaceAll("@", " ");
				} else {
					CONSULTATION_DEFAULT_HEADERS = CONSULTATION_DEFAULT_HEADERS.replaceAll("@", " ");
				}

				if(department != null && !department.equals("")) {
					CONSULTATION_DEFAULT_HEADERS = CONSULTATION_DEFAULT_HEADERS.replaceAll("#", "?");
					ps = con.prepareStatement(CONSULTATION_DEFAULT_HEADERS);
					ps.setString(1, department);
				}
				else {
					// All department doctors and appointments are selected.
					department = " select dept_id from department ";
					CONSULTATION_DEFAULT_HEADERS = CONSULTATION_DEFAULT_HEADERS.replaceAll("#", department);
					ps = con.prepareStatement(CONSULTATION_DEFAULT_HEADERS);
				}
			}else if(category.equals("OPE")){

        if (centerId == 0
            && (Integer) (GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
          SURGERY_DEFAULT_HEADERS_REPLACED = SURGERY_DEFAULT_HEADERS_REPLACED.replace("#", "");
          ps = con.prepareStatement(SURGERY_DEFAULT_HEADERS_REPLACED);
          if ((roleId != 1) && (roleId != 2)) {
            ps.setString(index, userName);
            index = index + 1;
          }
        } else {
          SURGERY_DEFAULT_HEADERS_REPLACED = SURGERY_DEFAULT_HEADERS_REPLACED.replace("#",
              centerClause);
          ps = con.prepareStatement(SURGERY_DEFAULT_HEADERS_REPLACED);
          if ((roleId != 1) && (roleId != 2)) {
            ps.setString(index, userName);
            index = index + 1;
          }
          ps.setInt(index, centerId);
        }


			}else if(category.equals("SNP")){
					if (centerId == 0 && (Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
						SERVICE_DEFAULT_HEADERS = SERVICE_DEFAULT_HEADERS.replace("#", "");
						ps = con.prepareStatement(SERVICE_DEFAULT_HEADERS);
					} else {
						SERVICE_DEFAULT_HEADERS = SERVICE_DEFAULT_HEADERS.replace("#", centerClause);
						ps = con.prepareStatement(SERVICE_DEFAULT_HEADERS);
						ps.setInt(1, centerId);
					}
		
			}else if(category.equals("DIA")){
					if (centerId == 0 && (Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")) > 1) {
						TESTS_DEFAULT_HEADERS = TESTS_DEFAULT_HEADERS.replace("#", "");
						ps = con.prepareStatement(TESTS_DEFAULT_HEADERS);
					} else {
						TESTS_DEFAULT_HEADERS = TESTS_DEFAULT_HEADERS.replace("#", centerClause);
						ps = con.prepareStatement(TESTS_DEFAULT_HEADERS);
						ps.setInt(1, centerId);
					}
			}
			defaultlist = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return defaultlist;
	}

	private  String GET_RESOURCE_NONAVAILABILITY = " select from_time,to_time,availability_status,srad.center_id" +
			" from sch_resource_availability sra " +
			" JOIN sch_resource_availability_details srad ON(srad.res_avail_id = sra.res_avail_id)" +
			" where res_sch_name = ? and availability_date  = to_date(?,'DD-MM-YYYY')  AND from_time != to_time AND res_sch_type = ? #";

	public  List<BasicDynaBean> getResourceNonAvailablity(String scheduleName, String scheduleDate,String status,String res_sch_type, Integer centerId) throws SQLException, Exception {
		int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
		int userCenter = RequestContext.getCenterId();
		List<BasicDynaBean> nonavllist = null;
		List<BasicDynaBean> filteredNonAvailabilityList = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			if (status != null)
				GET_RESOURCE_NONAVAILABILITY = GET_RESOURCE_NONAVAILABILITY.replace("#", " and availability_status = ?");
			else
				GET_RESOURCE_NONAVAILABILITY = GET_RESOURCE_NONAVAILABILITY.replace("#", "");
			ps = con.prepareStatement(GET_RESOURCE_NONAVAILABILITY);
			ps.setString(1, scheduleName);
			ps.setString(2, scheduleDate);
			ps.setString(3, res_sch_type);
			if (status != null)
				ps.setString(4, status);
			nonavllist = DataBaseUtil.queryToDynaList(ps);
			
			if (max_center > 1 && res_sch_type.equals("DOC") && centerId != null) {
				filteredNonAvailabilityList = ResourceBO.filterAllResourcesAvailability(userCenter, nonavllist,centerId);
			} else {
				filteredNonAvailabilityList = nonavllist;
			}
			return filteredNonAvailabilityList;

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String SCHEDULER_DOCTORS_QUERY = "select doctor_name,doctor_id,dept_id,dept_name from scheduler_doctor_availability " +
			" LEFT JOIN doctors using(doctor_id) LEFT JOIN department using (dept_id) " ;

	public static List<BasicDynaBean> getSchedulerTimingAvailableDoctors() throws SQLException {
		List<BasicDynaBean> list = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SCHEDULER_DOCTORS_QUERY);
			list = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	private static final String SCHEDULER_DOCTORS_NOTIN_DASHBOARD_QUERY = " select doctor_id,doctor_name,schedule,dept_id,dept_name from " +
			" (select doctor_id,doctor_name,schedule,dept_id,dept_name from doctors  " +
			" JOIN department using (dept_id) " +
			" union select '*' as doctor_id, 'All Doctors' as doctor_name,true as schedule,'*' as dept_id,'All Departments' as dept_name ) as d " +
			" where doctor_id NOT IN (select doctor_id from scheduler_doctor_availability) and schedule = true " ;

	public static List<BasicDynaBean> getSchedulerNotinDashboardDoctors() throws SQLException {
		List<BasicDynaBean> list = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SCHEDULER_DOCTORS_NOTIN_DASHBOARD_QUERY);
			list = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	private static final String DOCTOR_NON_AVAILABILITY_TIMINGS_FIELDS = "SELECT non_available_date, doctor_name, " +
			" to_char(non_available_date,'Dy') as week_day, to_char(non_available_date,'D') as week_no, firsthalf_from, " +
			" firsthalf_to, secondhalf_from, secondhalf_to, dept_id, dept_name " ;

	private static final String DOCTOR_NON_AVAILABILITY_TIMINGS_TABLES = " FROM scheduler_doctor_nonavailabilitys " +
			" JOIN doctors using(doctor_id) JOIN department using(dept_id) WHERE doctor_id = ? " ;

	private static final String DOCTOR_NON_AVAILABILITY_TIMINGS_CONDITION = " AND EXTRACT('w' from non_available_date) = ? " +
			" AND non_available_date >= current_date order by non_available_date";

	public static List<BasicDynaBean> getDoctorNonAvailabilityTiming(String doctorId, int weekNumber) throws SQLException {
		List<BasicDynaBean> list = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			if(weekNumber > 52) {
				int weeks = DataBaseUtil.getIntValueFromDb("SELECT CASE extract(week from to_date(extract(year from current_date)||'-12-31','YYYY-MM-DD')) WHEN 1 THEN 52 WHEN 52 THEN 52 ELSE 53 END ");
				if(weeks == weekNumber) {}
				else weekNumber = weekNumber - weeks;
			}
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DOCTOR_NON_AVAILABILITY_TIMINGS_FIELDS + DOCTOR_NON_AVAILABILITY_TIMINGS_TABLES + DOCTOR_NON_AVAILABILITY_TIMINGS_CONDITION);
			ps.setString(1, doctorId);
			ps.setInt(2, weekNumber);
			list = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	private static final String DOCTOR_NON_AVAILABILITY_TIMINGS_BETWEEN_DATES = " AND non_available_date between ? AND ? " +
	" AND non_available_date >= current_date order by non_available_date";

	public static List getTimingBetweenDates(String docid, Date fdate, Date tdate)throws SQLException {
		List<BasicDynaBean> list = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(DOCTOR_NON_AVAILABILITY_TIMINGS_FIELDS + DOCTOR_NON_AVAILABILITY_TIMINGS_TABLES + DOCTOR_NON_AVAILABILITY_TIMINGS_BETWEEN_DATES);
			ps.setString(1, docid);
			ps.setDate(2, fdate);
			ps.setDate(3, tdate);
			list = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return list;
	}

	private static final String GENERATED_DAYS_QUERY = "SELECT dates,to_char(dates,'D') as week_no from " +
			" (SELECT DATE (?) + s.t as dates FROM generate_series(0,(SELECT DATE (?) - DATE (?))) as s(t)) as gendates";

	/**
	*  INSERT,UPDATE and DELETE of Doctor Non Available Timing
	*/

	public static final String INSERT_NONAVAIL_TIMING = "INSERT INTO scheduler_doctor_nonavailabilitys(doctor_id, appt_type, non_available_date," +
			" week_day, firsthalf_from, firsthalf_to, secondhalf_from, secondhalf_to, status) values (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public boolean insertTiming(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(INSERT_NONAVAIL_TIMING)) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        int index = 0;
        TimingDTO tdto = (TimingDTO) iterator.next();
        ps.setString(++index, tdto.getDoctor());
        ps.setString(++index, "DOC");
        ps.setDate(++index, tdto.getNonAvailDate());
        ps.setInt(++index, tdto.getWeekDay());
        index = setTimingValues(ps, tdto, index);
        ps.setString(++index, "A");
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

	public static final String UPDATE_NONAVAIL_TIMING = "UPDATE scheduler_doctor_nonavailabilitys SET firsthalf_from=?, firsthalf_to=?," +
			" secondhalf_from=?, secondhalf_to=? WHERE doctor_id=? and non_available_date=? ";

  public boolean updateTiming(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(UPDATE_NONAVAIL_TIMING)) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        int index = 0;
        TimingDTO tdto = (TimingDTO) iterator.next();
        index = setTimingValues(ps, tdto, index);
        ps.setString(++index, tdto.getDoctor());
        ps.setDate(++index, tdto.getNonAvailDate());
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

	private static final String SELECT_NONAVAIL_TIMING = "SELECT * FROM scheduler_doctor_nonavailabilitys WHERE " +
			" doctor_id=? and non_available_date=? ";

	private static final String DELETE_NONAVAIL_TIMING = "DELETE FROM scheduler_doctor_nonavailabilitys WHERE " +
			" doctor_id=? and non_available_date=? ";

  public boolean deleteTiming(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps1 = con.prepareStatement(SELECT_NONAVAIL_TIMING);
        PreparedStatement ps = con.prepareStatement(DELETE_NONAVAIL_TIMING);) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        int index = 0;
        TimingDTO tdto = (TimingDTO) iterator.next();
        ps.setString(++index, tdto.getDoctor());
        ps.setDate(++index, tdto.getNonAvailDate());

        ps1.setString(1, tdto.getDoctor());
        ps1.setDate(2, tdto.getNonAvailDate());
        try (ResultSet rs = ps1.executeQuery()) {
          if (rs.next())
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
    }
    return success;
  }

	private int setTimingValues(PreparedStatement ps, TimingDTO tdto,int index) throws SQLException {
		if(tdto.getWeekDay() == 1) {
			ps.setTime(++index, tdto.getSun1());
			ps.setTime(++index, tdto.getSun2());
			ps.setTime(++index, tdto.getSun3());
			ps.setTime(++index, tdto.getSun4());
		}else if(tdto.getWeekDay() == 2) {
			ps.setTime(++index, tdto.getMon1());
			ps.setTime(++index, tdto.getMon2());
			ps.setTime(++index, tdto.getMon3());
			ps.setTime(++index, tdto.getMon4());
		}else if(tdto.getWeekDay() == 3) {
			ps.setTime(++index, tdto.getTue1());
			ps.setTime(++index, tdto.getTue2());
			ps.setTime(++index, tdto.getTue3());
			ps.setTime(++index, tdto.getTue4());
		}else if(tdto.getWeekDay() == 4) {
			ps.setTime(++index, tdto.getWed1());
			ps.setTime(++index, tdto.getWed2());
			ps.setTime(++index, tdto.getWed3());
			ps.setTime(++index, tdto.getWed4());
		}else if(tdto.getWeekDay() == 5) {
			ps.setTime(++index, tdto.getThu1());
			ps.setTime(++index, tdto.getThu2());
			ps.setTime(++index, tdto.getThu3());
			ps.setTime(++index, tdto.getThu4());
		}else if(tdto.getWeekDay() == 6) {
			ps.setTime(++index, tdto.getFri1());
			ps.setTime(++index, tdto.getFri2());
			ps.setTime(++index, tdto.getFri3());
			ps.setTime(++index, tdto.getFri4());
		}else if(tdto.getWeekDay() == 7) {
			ps.setTime(++index, tdto.getSat1());
			ps.setTime(++index, tdto.getSat2());
			ps.setTime(++index, tdto.getSat3());
			ps.setTime(++index, tdto.getSat4());
		}
		return index;
	}

	private static final String GET_RESCHEDULE_APPOINTMENT_COUNT = "select count(*) as count from scheduler_appointments sa " +
			" join  sch_resource_availability sra on (date_trunc('day', sa.appointment_time::timestamp) = sra.availability_date)" +
			" and (sa.res_sch_name = sra.res_sch_name) AND sra.res_sch_type = ? " +
			" join sch_resource_availability_details srad on(sra.res_avail_id = srad.res_avail_id) " +
			" and (((date_trunc('minutes', sa.appointment_time::time) >= srad.from_time " +
			" and  date_trunc('minutes', sa.appointment_time::time) <= srad.to_time))" +
			" and sra.availability_date between ? and ?  and sa.appointment_status in ('Booked','Confirmed'))";

	public static int getReschedulableAppCount(List resourceList,Date fdate,Date tdate,String category) throws SQLException{

		Connection con  = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		int count = 0;
		try{

			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where, "sra.res_sch_name", resourceList);

			StringBuilder query = new StringBuilder(GET_RESCHEDULE_APPOINTMENT_COUNT);
			query.append(where);

			ps = con.prepareStatement(query.toString());
			if (category.equals("DOC")) {
				ps.setString(1, "DOC");
			} else if(category.equals("SNP")) {
				ps.setString(1, "SRID");
			} else if(category.equals("OPE")) {
				ps.setString(1, "THID");
			} else if(category.equals("DIA")) {
				ps.setString(1, "EQID");
			}

			int index = 2;
			ps.setDate(index++, fdate);
			ps.setDate(index++, tdate);
			for(int i=0;i<resourceList.size();i++,index++){
				  ps.setString(index, (String)resourceList.get(i));
			}
			count =  DataBaseUtil.getIntValueFromDb(ps);

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return count;
	}


	private static final String APPOINTMENT_COMPLETED =
		"UPDATE scheduler_appointments SET appointment_status=? , completed_time=? WHERE appointment_id=?";

	private static final String GET_APPOINTMENTID_FOR_CONSULTATION ="SELECT appointment_id FROM doctor_consultation WHERE consultation_id = ";

	private static final String GET_APPOINTMENTID_FOR_OPERATION ="SELECT appointment_id FROM bed_operation_schedule WHERE prescribed_id = ";

	private static final String GET_APPOINTMENTID_FOR_TESTS ="SELECT appointment_id FROM tests_prescribed WHERE prescribed_id = ";

	private static final String GET_APPOINTMENTID_FOR_SERVICES ="SELECT appointment_id FROM services_prescribed WHERE prescription_id = ";


	public static boolean updateAppointments(Connection con, int[] consultationIds) throws SQLException {
		try(PreparedStatement ps = con.prepareStatement(APPOINTMENT_COMPLETED)) {
			for (int consultId : consultationIds) {
				int appointment_id =  DataBaseUtil.getIntValueFromDb(GET_APPOINTMENTID_FOR_CONSULTATION + consultId);
				if (appointment_id != 0) {
					ps.setString(1, "Completed");
					ps.setTimestamp(2, DataBaseUtil.getDateandTime());
					ps.setInt(3, appointment_id);
					ps.addBatch();
				}
			}
			int[] results = ps.executeBatch();
			return DataBaseUtil.checkBatchUpdates(results);
		}
	}

	public static boolean updateAppointments(Connection con, int prescriptionId, String Category) throws SQLException {
		boolean flag = true;
		int appointment_id = 0;
		try(PreparedStatement ps = con.prepareStatement(APPOINTMENT_COMPLETED)) {
				if (Category.equals("DOC")) {
					 appointment_id =  DataBaseUtil.getIntValueFromDb(GET_APPOINTMENTID_FOR_CONSULTATION + prescriptionId);
				} else if(Category.equals("SNP")) {
					appointment_id =  DataBaseUtil.getIntValueFromDb(GET_APPOINTMENTID_FOR_SERVICES + prescriptionId);
				} else if(Category.equals("DIA")) {
					appointment_id =  DataBaseUtil.getIntValueFromDb(GET_APPOINTMENTID_FOR_TESTS + prescriptionId);
				} else if(Category.equals("OPE")) {
					appointment_id =  DataBaseUtil.getIntValueFromDb(GET_APPOINTMENTID_FOR_OPERATION + prescriptionId);
				}
				if (appointment_id != 0) {
					ps.setString(1, "Completed");
					ps.setTimestamp(2, DataBaseUtil.getDateandTime());
					ps.setInt(3, appointment_id);
					flag = ps.executeUpdate() > 0;
				}
			return flag;
		}
	}

	public static final String INSERT_CHANNELING_APPOINTMENT = "INSERT INTO scheduler_appointments (mr_no, patient_name, " +
			" patient_contact, complaint, appointment_id, res_sch_id, res_sch_name, appointment_time, " +
			" duration, appointment_status, booked_by, booked_time, cancel_reason, visit_id, consultation_type_id, remarks,changed_by,scheduler_visit_type,scheduler_prior_auth_no,scheduler_prior_auth_mode_id,center_id,presc_doc_id, " +
			" app_source_id, pat_package_id, bill_no, appt_token, salutation_name, unique_appt_ind, prim_res_id) " +
			" 	values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?, ?,?,?,?,?,?,?,?,?)";
    public boolean insertChannellingAppointment(ChannellingAppt appt) throws SQLException {
    	int records = 0;
		PreparedStatement ps = null;

		try {
		    ps = con.prepareStatement(INSERT_CHANNELING_APPOINTMENT);
		    ps.setString(1,appt.getMrNo()) ;
		    ps.setString(2, appt.getPatientName());
		    ps.setString(3,appt.getPhoneNo()) ;
		    ps.setString(4,appt.getComplaint()) ;
		    ps.setInt(5,appt.getAppointmentId()) ;
		    ps.setInt(6,appt.getScheduleId()) ;
		    ps.setString(7,appt.getScheduleName()) ;
		    ps.setTimestamp(8,appt.getAppointmentTime()) ;
		    ps.setInt(9,appt.getAppointmentDuration()) ;
		    ps.setString(10,appt.getAppointStatus()) ;
		    ps.setString(11,appt.getBookedBy()) ;
		    ps.setTimestamp(12,appt.getBookedTime()) ;
		    ps.setString(13,appt.getCancelReason());
		    ps.setString(14,appt.getVisitId());
		    ps.setInt(15, appt.getConsultationTypeId());
		    ps.setString(16,appt.getRemarks());
		    ps.setString(17,appt.getChangedBy());
		    ps.setString(18, appt.getSchedulerVisitType());
		    ps.setString(19, appt.getSchPriorAuthId());
		    ps.setInt(20, appt.getSchPriorAuthModeId());
		    ps.setInt(21, appt.getCenterId());
		    ps.setString(22, appt.getPrescDocId());
		    ps.setInt(23, appt.getApp_source_id());
		    ps.setInt(24, appt.getPat_package_id());
		    ps.setString(25, appt.getBill_no());
		    ps.setInt(26, appt.getApptToken());
		    ps.setString(27, appt.getSalutationName());
		    ps.setInt(28, appt.getUnique_appt_ind());
		    ps.setString(29, appt.getPrim_res_id());
		    records = ps.executeUpdate();
		} catch (Exception exp) {
			logger.error("exception",exp);
		}
		if (ps != null) {
		  ps.close();
		}
		return records > 0;
    }

	public static final String INSERT_APPOINTMENT = "INSERT INTO scheduler_appointments (mr_no,patient_name, " +
			" patient_contact, complaint, appointment_id, res_sch_id, res_sch_name, appointment_time, " +
			" duration, appointment_status, booked_by, booked_time, cancel_reason, visit_id, consultation_type_id, "
			+ "remarks,changed_by,scheduler_visit_type,scheduler_prior_auth_no,scheduler_prior_auth_mode_id,center_id,presc_doc_id, " +
			" salutation_name, unique_appt_ind, prim_res_id,patient_contact_country_code,cond_doc_id, app_source_id,contact_id,waitlist  ) " +
			" 	values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?, ?,?,?,?,?,?,?,?,?,?)";
	//mobile related changes
	public boolean insertAppointments(List list) throws SQLException {
		int results[] = null;
		boolean success = true;
		try(PreparedStatement ps = con.prepareStatement(INSERT_APPOINTMENT)) {
  		Iterator iterator = list.iterator();
  		while (iterator.hasNext()) {
  			Appointments appt =  (Appointments) iterator.next();
  			ps.setString(1,appt.getMrNo()) ;
  			ps.setString(2, appt.getPatientName());
  			ps.setString(3,appt.getPhoneNo()) ;
  			ps.setString(4,appt.getComplaint()) ;
  			ps.setInt(5,appt.getAppointmentId()) ;
  			ps.setInt(6,appt.getScheduleId()) ;
  			ps.setString(7,appt.getScheduleName()) ;
  			ps.setTimestamp(8,appt.getAppointmentTime()) ;
  			ps.setInt(9,appt.getAppointmentDuration()) ;
  			ps.setString(10,appt.getAppointStatus()) ;
  			ps.setString(11,appt.getBookedBy()) ;
  			ps.setTimestamp(12,appt.getBookedTime()) ;
  			ps.setString(13,appt.getCancelReason());
  			ps.setString(14,appt.getVisitId());
  			ps.setInt(15, appt.getConsultationTypeId());
  			ps.setString(16,appt.getRemarks());
  			ps.setString(17,appt.getChangedBy());
  			ps.setString(18, appt.getSchedulerVisitType());
  			ps.setString(19, appt.getSchPriorAuthId());
  			ps.setInt(20, appt.getSchPriorAuthModeId());
  			ps.setInt(21, appt.getCenterId());
  			ps.setString(22, appt.getPrescDocId());
  			ps.setString(23, appt.getSalutationName());
  			ps.setInt(24, appt.getUnique_appt_ind());
  			ps.setString(25, appt.getPrim_res_id());
  			ps.setString(26,appt.getPhoneCountryCode()) ;
  			ps.setString(27,appt.getCondDocId());
  			ps.setInt(28,appt.getApp_source_id()) ;
  			if (appt.getContactId() != null) {
  			  ps.setInt(29,appt.getContactId());
  			} else {
  			  ps.setNull(29,java.sql.Types.INTEGER);
  			}
  			ps.setInt(30, appt.getWaitlist());
  			ps.addBatch();
  			results = ps.executeBatch();
  			
  		}
		} catch (SQLException e) {
			logger.debug("SQLException occured");
			throw e;
		}
		if(results != null) {
		  for (int p = 0; p < results.length; p++) {
		    if (results[p] <= 0) {
		      success = false;
		      break;
		      }
		    }
		  }
		return success;
	}

	public static final String INSERT_APPOINTMENT_ITEMS = "INSERT INTO scheduler_appointment_items (appointment_id, resource_type," +
			" resource_id, appointment_item_id,user_name,mod_time) values (?, ?, ?, ?, ?, ?)";

  public boolean insertAppointmentItems(List list) throws SQLException {
    boolean success = true;
    try (PreparedStatement ps = con.prepareStatement(INSERT_APPOINTMENT_ITEMS)) {
      Iterator iterator = list.iterator();
      while (iterator.hasNext()) {
        AppointMentResource res = (AppointMentResource) iterator.next();
        ps.setInt(1, res.getAppointmentId());
        ps.setString(2, res.getResourceType());
        ps.setString(3, res.getResourceId());
        ps.setInt(4, res.getAppointment_item_id());
        ps.setString(5, res.getUser_name());
        ps.setTimestamp(6, res.getMod_time());
        ps.addBatch();
      }
      int results[] = ps.executeBatch();
      if (results != null) {
        for (int p = 0; p < results.length; p++) {
          if (results[p] <= 0) {
            success = false;
            break;
          }
        }
      }
    }
    return success;
  }

	private String GET_DOCTORS_SCHEDULENAMES = "SELECT distinct d.doctor_id as resource_id, d.doctor_name as resource_name,d.practition_type," +
			" d.op_consultation_validity,d.allowed_revisit_count,d.dept_id,d.overbook_limit " +
			" FROM doctors d" +
			" JOIN doctor_center_master dcm ON (d.doctor_id=dcm.doctor_id)"+
			" WHERE d.status = 'A' AND dcm.status='A' AND d.schedule = true @ AND d.dept_id IN (#) order by d.doctor_name";

	private String GET_THEATERS_SCHEDULENAMES = "SELECT distinct tm.theatre_id as resource_id , tm.theatre_name as resource_name, overbook_limit " +
		" FROM theatre_master tm  LEFT JOIN user_theatres as ut ON(tm.theatre_id = ut.theatre_id)"+
	      " WHERE status = 'A' AND schedule = true & # order by tm.theatre_name " ;

	private String GET_EQUIPMENTS_SCHEDULENAMES = "SELECT eq.eq_id::text as resource_id,eq.equipment_name as resource_name,eq.center_id, overbook_limit " +
			" FROM test_equipment_master eq WHERE status = 'A' AND schedule = true # order by equipment_name " ;

	private String GET_SERVICE_RESOURCE_SCHEDULENAMES = "SELECT sre.serv_res_id::text as resource_id,sre.serv_resource_name as resource_name,sre.center_id, overbook_limit " +
			" FROM service_resource_master sre WHERE status = 'A' AND schedule = true # order by sre.serv_resource_name " ;

	public List getScheduleResourceDoctorsList(String department,Integer centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		int max_center_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		List<Object> args = new ArrayList<>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if(max_center_inc_default > 1) {
				if(centerId != 0) {
					GET_DOCTORS_SCHEDULENAMES = GET_DOCTORS_SCHEDULENAMES.replaceAll("@", " AND dcm.center_id IN (0,?)");
					args.add(centerId);
				} else {
					GET_DOCTORS_SCHEDULENAMES = GET_DOCTORS_SCHEDULENAMES.replaceAll("@", " ");
				}
			} else {
				GET_DOCTORS_SCHEDULENAMES = GET_DOCTORS_SCHEDULENAMES.replaceAll("@", " ");
			}

			if(department != null && !department.equals("")) {
				GET_DOCTORS_SCHEDULENAMES = GET_DOCTORS_SCHEDULENAMES.replaceAll("#", "?");
				ps = con.prepareStatement(GET_DOCTORS_SCHEDULENAMES);
				args.add(department);
				ListIterator<Object> argsIterator = args.listIterator();
				while (argsIterator.hasNext()) {
					ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
				}			
				
			}
			else {
				GET_DOCTORS_SCHEDULENAMES = GET_DOCTORS_SCHEDULENAMES.replaceAll("#", "select dept_id from department");
				ps = con.prepareStatement(GET_DOCTORS_SCHEDULENAMES);
				ListIterator<Object> argsIterator = args.listIterator();
				while (argsIterator.hasNext()) {
					ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
				}			
			}

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private String GET_CHANELLING_DOCTORS_SCHEDULENAMES = "SELECT distinct d.doctor_id as resource_id, d.doctor_name as resource_name,d.overbook_limit,d.practition_type," +
			" d.op_consultation_validity,d.allowed_revisit_count,d.dept_id " +
			" FROM doctors d" +
			" JOIN doctor_center_master dcm ON (d.doctor_id=dcm.doctor_id)"+
			" WHERE d.status = 'A' AND dcm.status='A' AND d.schedule = true AND d.chanelling='Y' @ AND d.dept_id IN (#) order by d.doctor_name";

	public List getScheduleChanellingDoctorsList(String department,Integer centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		List<Object> args = new ArrayList<>();
		int max_center_inc_default = (Integer)GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if(max_center_inc_default > 1) {
				if(centerId != 0) {
					GET_CHANELLING_DOCTORS_SCHEDULENAMES = GET_CHANELLING_DOCTORS_SCHEDULENAMES.replaceAll("@", " AND dcm.center_id IN (0,?)");
					args.add(centerId);
				} else {
					GET_CHANELLING_DOCTORS_SCHEDULENAMES = GET_CHANELLING_DOCTORS_SCHEDULENAMES.replaceAll("@", " ");
				}
			} else {
				GET_CHANELLING_DOCTORS_SCHEDULENAMES = GET_CHANELLING_DOCTORS_SCHEDULENAMES.replaceAll("@", " ");
			}

			if(department != null && !department.equals("")) {
				GET_CHANELLING_DOCTORS_SCHEDULENAMES = GET_CHANELLING_DOCTORS_SCHEDULENAMES.replaceAll("#", "?");
				ps = con.prepareStatement(GET_CHANELLING_DOCTORS_SCHEDULENAMES);
				args.add(department);
				ListIterator<Object> argsIterator = args.listIterator();
				while (argsIterator.hasNext()) {
					ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
				}			
			}
			else {
				GET_CHANELLING_DOCTORS_SCHEDULENAMES = GET_CHANELLING_DOCTORS_SCHEDULENAMES.replaceAll("#", " select dept_id from department ");
				ps = con.prepareStatement(GET_CHANELLING_DOCTORS_SCHEDULENAMES);
				ListIterator<Object> argsIterator = args.listIterator();
				while (argsIterator.hasNext()) {
					ps.setObject(argsIterator.nextIndex() + 1, argsIterator.next());
				}			
			}

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List getScheduleResourceTheatresList( int centerId,String userName,int roleId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String GET_THEATERS_SCHEDULENAMES_REPLACED = GET_THEATERS_SCHEDULENAMES;
		String centerClause = " AND center_id = ?";
		int index = 1;
    if ((roleId != 1) && (roleId != 2)) {
      GET_THEATERS_SCHEDULENAMES_REPLACED = GET_THEATERS_SCHEDULENAMES_REPLACED.replace("&",
          "and ut.emp_username = ? ");
    } else {
      GET_THEATERS_SCHEDULENAMES_REPLACED = GET_THEATERS_SCHEDULENAMES_REPLACED.replace("&", "");
    }
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			if (centerId == 0 && ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"))) > 1) {
				GET_THEATERS_SCHEDULENAMES_REPLACED = GET_THEATERS_SCHEDULENAMES_REPLACED.replace("#", "");
				ps = con.prepareStatement(GET_THEATERS_SCHEDULENAMES_REPLACED);
				if ((roleId != 1) && (roleId != 2)) {
          ps.setString(index, userName);
        }
			} else {
				GET_THEATERS_SCHEDULENAMES_REPLACED = GET_THEATERS_SCHEDULENAMES_REPLACED.replace("#", centerClause);
				ps = con.prepareStatement(GET_THEATERS_SCHEDULENAMES_REPLACED);
				if ((roleId != 1) && (roleId != 2)) {
          ps.setString(index, userName);
          index = index + 1;
        }
        ps.setInt(index, centerId);
			}

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List getScheduleResourceEquipmentsList(String department,int centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String centerClause = "AND center_id = ?";
		try {
			con = DataBaseUtil.getReadOnlyConnection();
				if (centerId == 0 && ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"))) > 1) {
					GET_EQUIPMENTS_SCHEDULENAMES = GET_EQUIPMENTS_SCHEDULENAMES.replace("#", "");
					ps = con.prepareStatement(GET_EQUIPMENTS_SCHEDULENAMES);
				} else {
					GET_EQUIPMENTS_SCHEDULENAMES = GET_EQUIPMENTS_SCHEDULENAMES.replace("#", centerClause);
					ps = con.prepareStatement(GET_EQUIPMENTS_SCHEDULENAMES);
					ps.setInt(1, centerId);
				}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List getScheduleServiceResourceList(String department,int centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String centerClause = "AND center_id = ?";
		try {
			con = DataBaseUtil.getReadOnlyConnection();
				if (centerId == 0 && ((Integer)(GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default"))) > 1) {
					GET_SERVICE_RESOURCE_SCHEDULENAMES = GET_SERVICE_RESOURCE_SCHEDULENAMES.replace("#", "");
					ps = con.prepareStatement(GET_SERVICE_RESOURCE_SCHEDULENAMES);
				} else {
					GET_SERVICE_RESOURCE_SCHEDULENAMES = GET_SERVICE_RESOURCE_SCHEDULENAMES.replace("#", centerClause);
					ps = con.prepareStatement(GET_SERVICE_RESOURCE_SCHEDULENAMES);
					ps.setInt(1, centerId);
				}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private final String GET_SERVICE_NAMES="SELECT service_id as resource_id , service_name as resource_name," +
			" CASE WHEN service_code IS NULL OR service_code = '' THEN " +
			" service_name || '(' ||department || ')'  ELSE (service_name ||' - '||COALESCE(service_code,'') || '(' ||department || ')') END AS resource_name_dept_code, " +
			" service_code AS resource_code,sd.department as resource_dept_name" +
			" FROM services  s " +
			" JOIN services_departments sd ON(sd.serv_dept_id = s.serv_dept_id)" +
			" WHERE s.status = 'A' order by service_name";

	private final String  GET_DIAGNOSTICS_NAMES="SELECT test_id as resource_id , test_name as resource_name, conducting_doc_mandatory,dd.category, " +
			" CASE WHEN diag_code IS NULL OR diag_code = '' THEN " +
			" test_name|| '(' ||ddept_name || ')' ELSE (test_name ||' - '||COALESCE(diag_code,'')|| '(' ||ddept_name || ')') END AS resource_name_dept_code, " +
			" diag_code AS resource_code,dd.ddept_name as resource_dept_name" +
			" FROM diagnostics  diag " +
			" JOIN diagnostics_departments dd ON(dd.ddept_id = diag.ddept_id)" +
			" WHERE diag.is_prescribable AND diag.status = 'A'";

	private final String  GET_OPERATION_NAMES="SELECT op_id as resource_id , operation_name as resource_name," +
			" CASE WHEN operation_code IS NULL OR operation_code = '' THEN " +
			" operation_name|| '(' ||dept_name || ')' ELSE (operation_name ||' - '||COALESCE(operation_code,'')|| '(' ||dept_name || ')')END AS resource_name_dept_code, " +
			" operation_code AS resource_code,d.dept_name as resource_dept_name" +
			" FROM operation_master  om " +
			" JOIN department d ON(d.dept_id = om.dept_id)" +
			" WHERE om.status = 'A'";

	public List getScheduleNamesList(String cat) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String query=null;
		if(cat.equals("SNP"))
			query=GET_SERVICE_NAMES;
		else if(cat.equals("DIA"))
			query=GET_DIAGNOSTICS_NAMES;
		else if(cat.equals("OPE"))
			query=GET_OPERATION_NAMES;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(query);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private final static  String RES_FIELDS = " SELECT * ";

	private static final String RES_COUNT = " SELECT COUNT(0) ";

	private static final String RES_TABLE = " FROM (SELECT srd.resource_id,resource_name,resource_type, " +
			" downtime_start, downtime_end,reason, status FROM scheduler_resource_downtime srd join ( SELECT eq_id::text as resource_id, " +
			" equipment_name as resource_name FROM test_equipment_master em UNION SELECT theatre_id AS resource_id, theatre_name as resource_name " +
			" FROM theatre_master tm) AS res_names on (srd.resource_id = res_names.resource_id)) As foo ";



	public PagedList getDownTimeResourceList(java.util.Map filters, java.util.Map pagingParams)
		throws SQLException, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, RES_FIELDS,
					RES_COUNT, RES_TABLE, pagingParams);

			qb.addFilterFromParamMap(filters);
			qb.addSecondarySort("resource_id", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();

			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public static final String QUERY=" SELECT * FROM (SELECT * FROM scheduler_resource_downtime  JOIN( "+
 								" SELECT eq_id::text as resid, equipment_name as resource_name from test_equipment_master UNION SELECT theatre_id as resid , " +
 								" theatre_name as resource_name from theatre_master) AS rs ON (resource_id= resid))AS foo WHERE resource_id= ?"+
 								" AND downtime_start= (?::timestamp) AND downtime_end= (?::timestamp)";

	public static List getDownTimeDetails(String resource_id, String downtime_start, String downtime_end) throws SQLException, ParseException
	{
		ResultSet rs=null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		List list=null;
		downtime_start=downtime_start.substring(0, 10);
		downtime_end=downtime_end.substring(0, 10);
		try
		{
			ps=con.prepareStatement(QUERY);
			ps.setString(1, resource_id);
			ps.setString(2,downtime_start);
			ps.setString(3,downtime_end);
			list = DataBaseUtil.queryToDynaList(ps);
		}
		finally
		{
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return list;
	}

	public static final String UPDATEDOWNTIME =" UPDATE scheduler_resource_downtime SET status = ? WHERE resource_id = ?"+
		" AND downtime_start =(?::timestamp) AND downtime_end=(?::timestamp)";

	public static boolean updateDownTimeDetails(String status, String resource_id, String downtime_start,String downtime_end)throws SQLException
	{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		downtime_start=downtime_start.substring(0, 10);
		downtime_end=downtime_end.substring(0, 10);
		try
		{
			ps=con.prepareStatement(UPDATEDOWNTIME );
			ps.setString(1, status);
			ps.setString(2,resource_id);
			ps.setString(3, downtime_start);
			ps.setString(4,downtime_end);
			int i=ps.executeUpdate();

			return i !=0;
		}
		finally
		{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static String DOCTOR =" SELECT d.doctor_id, d.doctor_name,dcm.center_id from doctors d" +
									" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
									" where d.status='A' and dcm.status='A' and (dcm.center_id = ? OR dcm.center_id = 0)";
	private static String DIAGNOSTICS="SELECT test_id, test_name from diagnostics where status='A'";
	private static String SERV="SELECT service_id,service_name ||'(' || department || ')' as service_name,department as dept_name FROM  services s " +
			"	join services_departments sd ON(sd.serv_dept_id = s.serv_dept_id) WHERE s.status = 'A' order by service_name " ;
	private static String OPERATION="SELECT op_id, operation_name,prior_auth_required from operation_master where status='A'";
	//private static String PATIENTS="SELECT patient_name  from scheduler_appointments";




public static List getJsonList(String type) throws SQLException
	{
		Connection con=DataBaseUtil.getConnection();
		PreparedStatement ps=null;
		List l=null;
		int centerID = RequestContext.getCenterId();
		try{
			if(type.equals("DOC"))
			{
				ps=con.prepareStatement(DOCTOR);
				ps.setInt(1, centerID);
			}
			else if(type.equals("TES"))
			{
				ps=con.prepareStatement(DIAGNOSTICS);
			}
			else if(type.equals("SER"))
			{
				ps=con.prepareStatement(SERV);
			}
			else if(type.equals("SUR"))
			{
				ps=con.prepareStatement(OPERATION);
			}
			//else
			//{
				//ps=con.prepareStatement(PATIENTS);
			//}
			l=DataBaseUtil.queryToArrayList(ps);
		}
		finally
		{
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;

	}

//Below method filters all the doctor which belongs to super center and specific center.  
private static String ALL_DOCTOR =" SELECT doctor_id, doctor_name from doctors where status='A' ";

public static List getDoctorJsonList(String type) throws SQLException
{
	Connection con=DataBaseUtil.getConnection();
	PreparedStatement ps=null;
	List l=null;
	int centerID = RequestContext.getCenterId();
	try{
		if(type.equals("DOC") && centerID == 0 )
		{
			ps=con.prepareStatement(ALL_DOCTOR);
		}else{
			ps=con.prepareStatement(DOCTOR);
			ps.setInt(1, centerID);
		}
			
		l=DataBaseUtil.queryToArrayList(ps);
	}
	finally
	{
		DataBaseUtil.closeConnections(con, ps);
	}
	return l;
}
/*	private static String DOCTOR_PRIMARY_RESOURCES_CENTERS=" SELECT doc.doctor_id as resource_id,doc.doctor_name as resource_name,doc.center_id,hcm.center_name from doctors doc  " +
		" JOIN hospital_center_master hcm ON(hcm.center_id = doc.center_id) AND hcm.status = 'A' AND doc.status='A'";
*/
	private static String DOCTOR_PRIMARY_RESOURCES_CENTERS=" SELECT doc.doctor_id as resource_id,doc.doctor_name as resource_name,dcm.center_id,hcm.center_name " +
			" from doctors doc  " +
			" JOIN doctor_center_master dcm ON(doc.doctor_id = dcm.doctor_id)"+
			" JOIN hospital_center_master hcm ON(hcm.center_id = dcm.center_id) AND hcm.status = 'A' AND doc.status='A' AND dcm.status='A'";
	private static String TEST_PRIMARY_RESOURCES_CENTERS=" SELECT tem.eq_id::text as resource_id,tem.equipment_name as resource_name,tem.center_id,hcm.center_name from test_equipment_master tem  " +
			" JOIN hospital_center_master hcm ON(hcm.center_id = tem.center_id) AND hcm.status = 'A' AND tem.status='A'";
	private static String SERVICE_PRIMARY_RESOURCES_CENTERS=" SELECT srm.serv_res_id::text as resource_id,srm.serv_resource_name as resource_name,srm.center_id,hcm.center_name from service_resource_master srm  " +
			" JOIN hospital_center_master hcm ON(hcm.center_id = srm.center_id) AND hcm.status = 'A' AND srm.status='A'";
	private static String OPERATION_RESOURCES_CENTERS=" SELECT th.theatre_id as resource_id,th.theatre_name as resource_name,th.center_id,hcm.center_name from theatre_master th  " +
			" JOIN hospital_center_master hcm ON(hcm.center_id = th.center_id) AND hcm.status = 'A' AND th.status='A'";

	public static List getResourceCentersJsonList(String type) throws SQLException
	{
		Connection con=DataBaseUtil.getConnection();
		PreparedStatement ps=null;
		List l=null;
		try{
			if(type.equals("DOC")){
				ps=con.prepareStatement(DOCTOR_PRIMARY_RESOURCES_CENTERS);
			} else if(type.equals("TES")){
				ps=con.prepareStatement(TEST_PRIMARY_RESOURCES_CENTERS);
			} else if(type.equals("SER")) {
				ps=con.prepareStatement(SERVICE_PRIMARY_RESOURCES_CENTERS);
			} else if(type.equals("SUR")) {
				ps=con.prepareStatement(OPERATION_RESOURCES_CENTERS);
			}
			l=DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}


	/*
	 *  Week View Related Methods
	 */

	public static List<BasicDynaBean> getAvailability(String docId,Date fromDate,Date toDate) throws SQLException {
		Connection con=DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps=null;
		PreparedStatement ps1=null;
		List<BasicDynaBean> recordsList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> resultedList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean>resAvailIdList = null;

		String resAvailIdQuery = " SELECT DISTINCT(res_avail_id) FROM sch_resource_availability " +
								 " WHERE res_sch_type = ? AND res_sch_name= ? AND availability_date between ? AND ? "+
						         " ORDER by res_avail_id";

		String query = "select * from sch_resource_availability sra "+
					   "LEFT JOIN "+
					   " (select * from sch_resource_availability_details) as foo ON(foo.res_avail_id=sra.res_avail_id) "+
					   " WHERE res_sch_type = ? AND res_sch_name= ? AND availability_date between ? AND ?  order by sra.res_avail_id";
		 try {
			 ps1 = con.prepareStatement(resAvailIdQuery);
			 ps1.setString(1, "DOC");
			 ps1.setString(2,docId);
			 ps1.setDate(3, new java.sql.Date(fromDate.getTime()));
			 ps1.setDate(4, new java.sql.Date(toDate.getTime()));
			 resAvailIdList =  DataBaseUtil.queryToDynaList(ps1);
			 ps = con.prepareStatement(query.toString());
			 ps.setString(1, "DOC");
			 ps.setString(2,docId);
			 ps.setDate(3, new java.sql.Date(fromDate.getTime()));
			 ps.setDate(4, new java.sql.Date(toDate.getTime()));
			 recordsList =  DataBaseUtil.queryToDynaList(ps);
			 int res_avail_id = 0;
			 if (resAvailIdList != null && resAvailIdList.size() >0 && recordsList != null && recordsList.size() > 0) {
				 for(int i=0;i<resAvailIdList.size();i++) {
					 res_avail_id =  (Integer)resAvailIdList.get(i).get("res_avail_id");
					 for(int j=0;j<recordsList.size();j++) {
						 if (res_avail_id == (Integer)recordsList.get(j).get("res_avail_id")) {
							 resultedList.add(recordsList.get(i));
							 break;
						 }
					 }
				 }
			 }
			 return resultedList;
		 }
		 finally {
			 DataBaseUtil.closeConnections(con, ps);
			 DataBaseUtil.closeConnections(null, ps1);
		 }
	}

	public static ArrayList<Integer> getNoOfDaysAvail(Date fromDate, Date toDate,List<BasicDynaBean> docAvailabilityList,String resource) throws SQLException{
		ArrayList<Integer> l=new ArrayList<Integer>();
		int count = 1;
		if (docAvailabilityList != null && docAvailabilityList.size() > 0) {
			for (BasicDynaBean bean : docAvailabilityList) {
				if (bean.get("from_time") != null && bean.get("to_time") != null) {
					l.add(count);
					count++;
				}
			}
		}
		if(l.size() != 7) {
			l = new ArrayList<Integer>();
			for(int i=1;i<=7;i++) {
				l.add(i);
			}
		}
		return l;
	}

	private static final String NON_AVAILABLE_DATE = "SELECT * FROM scheduler_doctor_nonavailabilitys " +
			"	WHERE doctor_id = ? AND non_available_date = ? AND appt_type = ? AND status = ?";

	private static final String GET_PRIMARY_RESOURCE_ITEM = "SELECT * from scheduler_appointment_items WHERE appointment_id = ? AND" +
			" resource_type = ? ";
	public static BasicDynaBean findPrimaryResource(int appId, String resourceType) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_PRIMARY_RESOURCE_ITEM, new Object[] {appId, resourceType});
	}


	public static final String GET_RESOURCE_NAMES_IDS = "Select doctor_name,doctor_id from doctors " ;

	public static LinkedList<Map> sort(LinkedList<Map> headerMapList) throws SQLException{
		LinkedList<Map> sortedHeader = new LinkedList<Map>();
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		List<String> doctorIdList = new ArrayList<String>();

		try {
			for (Map header : headerMapList) {
				doctorIdList.add((String)header.get("id"));
			}

			StringBuilder query = new StringBuilder(GET_RESOURCE_NAMES_IDS);
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where, "doctor_id", doctorIdList);
			query.append(where);
			query.append(" order by doctor_name ");
			ps = con.prepareStatement(query.toString());
			int index = 1;
			for (String doctor_id : doctorIdList) {
				ps.setString(index++, doctor_id);
			}

			List<BasicDynaBean> docBeans = DataBaseUtil.queryToDynaList(ps);

			for (BasicDynaBean bean : docBeans) {
				for (Map header : headerMapList) {
					if (((String)bean.get("doctor_id")).equals((String)header.get("id")))
						sortedHeader.add(header);
				}
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return sortedHeader;
	}

	private static final String APPOINTMENT_ATTRIBUTES = "SELECT patient_name,mr_no," +
			" date(appointment_time) AS appointment_date,appointment_time::time AS appointment_time," +
			" duration, waitlist::text, " +
			" CASE WHEN sm.res_sch_category = 'DOC' THEN doc.doctor_name " +
			" WHEN sm.res_sch_category = 'OPE' THEN ope.operation_name " +
			" WHEN sm.res_sch_category = 'DIA' THEN dia.test_name " +
			" WHEN sm.res_sch_category = 'SNP' THEN ser.service_name " +
			" END AS booked_resource " +
			" FROM scheduler_appointments sa " +
			" LEFT JOIN scheduler_master sm ON(sa.res_sch_id = sm.res_sch_id)" +
			" LEFT JOIN doctors doc ON (sa.prim_res_id = doc.doctor_id) " +
			" LEFT JOIN operation_master ope ON (sa.res_sch_name = ope.op_id) " +
			" LEFT JOIN diagnostics dia ON (sa.res_sch_name = dia.test_id) " +
			" LEFT JOIN services ser ON (sa.res_sch_name = ser.service_id)" +
			" WHERE appointment_id = ?";

  public static List<BasicDynaBean> getAppointmentDetailsForPrint(int apptId, String category)
      throws SQLException {
    List<BasicDynaBean> apptList = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(APPOINTMENT_ATTRIBUTES)) {
      ps.setInt(1, apptId);
      apptList = DataBaseUtil.queryToDynaList(ps);
    }
    return apptList;
  }

	private static final String RESOURCE_ATTRIBUTES = "SELECT sa.*, " +
			" CASE WHEN sai.resource_type ='OPDOC' THEN " +
			"     (SELECT doctor_name FROM doctors  WHERE doctor_id=sai.resource_id) " +
			" WHEN sai.resource_type = 'THID'  THEN " +
			"     (SELECT theatre_name FROM theatre_master  WHERE theatre_id=sai.resource_id) " +
			" WHEN sai.resource_type = 'SRID' THEN " +
			"     (SELECT serv_resource_name  FROM service_resource_master  WHERE serv_res_id::text=sai.resource_id ) " +
			" WHEN sai.resource_type = 'EQID' THEN " +
			"     (SELECT equipment_name FROM test_equipment_master  WHERE eq_id::text=sai.resource_id) " +
			" END AS booked_resource, " +
			" CASE WHEN sm.res_sch_category = 'DOC' THEN (SELECT doctor_name FROM DOCTORS WHERE doctor_id=sa.prim_res_id) " +
		    " WHEN sm.res_sch_category = 'SNP' THEN (SELECT service_name FROM services WHERE service_id=sa.res_sch_name) "+
		    " WHEN sm.res_sch_category = 'DIA' THEN (SELECT test_name FROM diagnostics WHERE test_id=sa.res_sch_name) "+
		    " WHEN sm.res_sch_category = 'OPE' THEN (SELECT operation_name FROM operation_master WHERE op_id = sa.res_sch_name) "+
		    " END AS secondary_resource, " +
		    " CASE WHEN sm.res_sch_id = 1 THEN 'Consultation'"+
		    " WHEN sm.res_sch_id = 2 THEN 'Surgery'" +
		    " WHEN sm.res_sch_id = 3 THEN 'Service' " +
		    " WHEN sm.res_sch_id = 4 THEN 'Test' " +
		    " END AS appointment_type, "+
		    " CASE WHEN srt.resource_type = 'SUDOC' THEN 'Surgeon' " +
		    " WHEN srt.resource_type = 'ANEDOC' THEN 'Anesthetist' " +
		    " WHEN srt.resource_type = 'THID' THEN 'Operation Theatre' " +
		    " WHEN srt.resource_type = 'EQID' THEN 'Equipment' " +
		    " WHEN srt.resource_type = 'LABTECH' THEN 'Technician/Radiologist' " +
		    " WHEN srt.resource_type = 'DOC' THEN 'Doctor' " +
		    " WHEN srt.resource_type = 'OPDOC' THEN 'Doctor' " +
		    " WHEN srt.resource_type = 'SRID' THEN 'Service Resource' " +
		    " ELSE 'Generic Resource'" +
		    " END AS resource_type " +
		    "FROM scheduler_appointments sa " +
				" LEFT JOIN admission adm ON(adm.mr_no = sa.mr_no) " +
				" LEFT JOIN bed_names bn ON(bn.bed_id = adm.bed_id) " +
				" LEFT JOIN ward_names wn ON(wn.ward_no = bn.ward_no) " +
				" LEFT JOIN scheduler_master sm ON (sm.res_sch_id=sa.res_sch_id) " +
				" LEFT JOIN scheduler_resource_types srt ON ( srt.category=sm.res_sch_category ) " +
				" JOIN scheduler_appointment_items sai ON (sa.appointment_id=sai.appointment_id " +
				" AND sai.resource_type=srt.resource_type) " +
				" WHERE sa.appointment_id = ?";

	public static List<BasicDynaBean>getResourceDetailsForPrint(int apptId) throws SQLException{
    List<BasicDynaBean> resourceList = null;
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(RESOURCE_ATTRIBUTES)) {
      ps.setInt(1, apptId);
      resourceList = DataBaseUtil.queryToDynaList(ps);
    }
    return resourceList;
  }

	public static final String GET_APPOINTMENT_STATUS_FROM_CONSULTATION = "SELECT status FROM doctor_consultation " +
			" WHERE appointment_id = ?";

	public static final String GET_APPOINTMENT_STATUS_FROM_DIAGNOSTIC = "SELECT conducted FROM tests_prescribed  " +
			" WHERE appointment_id = ?";

	public static final String GET_APPOINTMENT_STATUS_FROM_OPERATION = "SELECT status " +
			" FROM bed_operation_schedule " +
			" WHERE appointment_id = ? ";

	public static final String GET_APPOINTMENT_STATUS_FROM_SERVICES = "SELECT conducted FROM services_prescribed sp " +
			" WHERE appointment_id = ?";

	public static boolean getAppointmentStatus(Connection con,String category,int appId) throws SQLException {
    boolean flag = false;
    String appointStatus = null;
    PreparedStatement ps = null;
    try {
      if (category.equals("DOC")) {
        ps = con.prepareStatement(GET_APPOINTMENT_STATUS_FROM_CONSULTATION);
        ps.setInt(1, appId);
      } else if (category.equals("SNP")) {
        ps = con.prepareStatement(GET_APPOINTMENT_STATUS_FROM_SERVICES);
        ps.setInt(1, appId);
      } else if (category.equals("DIA")) {
        ps = con.prepareStatement(GET_APPOINTMENT_STATUS_FROM_DIAGNOSTIC);
        ps.setInt(1, appId);
      } else if (category.equals("OPE")) {
        ps = con.prepareStatement(GET_APPOINTMENT_STATUS_FROM_OPERATION);
        ps.setInt(1, appId);
      }
      if(ps != null) {
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            appointStatus = rs.getString(1);
            if (appointStatus.equals("C")) {
              flag = true;
            }
          }
        }
      }
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
    return flag;
  }

	public static final String GET_APPOINTMENT_STATUS= "SELECT appointment_status FROM scheduler_appointments " +
			" WHERE appointment_id = ?";

  public static String getAppointmentStatus(Connection con, int appId) throws SQLException {
    String appointStatus = null;
    try (PreparedStatement ps = con.prepareStatement(GET_APPOINTMENT_STATUS)) {
      ps.setInt(1, appId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          appointStatus = rs.getString(1);
        }
      }
    }
    return appointStatus;
  }

	private static final String GET_CONDUCTION_FOR_TEST = "SELECT conduction_applicable FROM diagnostics " +
			" WHERE test_id = ?	";

	private static final String GET_CONDUCTION_FOR_SERVICE = "SELECT conduction_applicable FROM services " +
			" WHERE service_id = ?	";

	private static final String GET_CONDUCTION_FOR_SURGERY = "SELECT conduction_applicable FROM operation_master " +
			" WHERE op_id = ?	";

	public static boolean getConductionForTestOrServiceOrOperation(String category,String scheduleId) throws SQLException {
		boolean conduction = false;
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			if(category.equals("SNP")) {
				ps = con.prepareStatement(GET_CONDUCTION_FOR_SERVICE);
				ps.setString(1,scheduleId);
			}else if(category.equals("DIA")) {
				ps = con.prepareStatement(GET_CONDUCTION_FOR_TEST);
				ps.setString(1,scheduleId);
			} else if (category.equals("OPE")) {
				ps = con.prepareStatement(GET_CONDUCTION_FOR_SURGERY);
				ps.setString(1,scheduleId);
			}
			if (ps != null) {
  			rs = ps.executeQuery();
  			if (rs.next()) {
  				conduction = rs.getBoolean(1);
  			}
			}
			return conduction;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	public static final String GET_OPERATION_DETAILS = "SELECT sa.mr_no,sa.visit_id,sa.patient_name,sa.patient_contact,sa.appointment_id,sa.res_sch_id, " +
		" sa.res_sch_name, sa.appointment_status,sa.booked_by,sa.complaint,sa.rescheduled,sa.cancel_reason,sa.appointment_time, " +
		" sa.presc_doc_id, (select doctor_name from doctors where doctor_id=sa.presc_doc_id) as presc_doctor, " +
	 	" date(appointment_time)::text AS appointment_date,date(arrival_time) AS arrival_date,(appointment_time::time::text) AS appoint_text_time, " +
	 	" date(booked_time) AS booked_date,date(orig_appt_time) AS orig_appt_date,sa.appointment_time,sa.booked_time,sa.duration, " +
	 	" date(completed_time) AS completed_date,srt.primary_resource,bn.bed_name AS bed_name,wn.ward_name AS ward_name, " +
	 	" CASE WHEN sai.resource_id LIKE 'DOC%' THEN " +
 		" (SELECT doctor_name FROM doctors  WHERE doctor_id=sai.resource_id) " +
 	  	" WHEN sai.resource_id LIKE 'THID%'  THEN " +
 	  	" (SELECT theatre_name FROM theatre_master  WHERE theatre_id=sai.resource_id) " +
 	  	" WHEN sai.resource_type LIKE 'EQID' THEN " +
		" (SELECT equipment_name  FROM test_equipment_master  WHERE eq_id::text=sai.resource_id ) " +
		" WHEN sai.resource_type LIKE 'SRID' THEN " +
		" (SELECT serv_resource_name  FROM service_resource_master  WHERE serv_res_id::text=sai.resource_id ) " +
		" WHEN sai.resource_id LIKE 'DGC%' THEN " +
 	  	" (SELECT equipment_name FROM test_equipment_master  WHERE eq_id::text=sai.resource_id) " +
 	  	" END AS booked_resource, " +
 	  	" CASE WHEN sai.resource_id LIKE 'DOC%' THEN " +
 		" (SELECT doctor_id FROM doctors  WHERE doctor_id=sai.resource_id) " +
 	  	" WHEN sai.resource_id LIKE 'THID%'  THEN " +
 	  	" (SELECT theatre_id FROM theatre_master  WHERE theatre_id=sai.resource_id) " +
 	  	" WHEN sai.resource_type LIKE 'EQID' THEN " +
		" (SELECT eq_id :: text  FROM test_equipment_master  WHERE eq_id::text=sai.resource_id ) " +
		" WHEN sai.resource_type LIKE 'SRID' THEN " +
		" (SELECT serv_res_id::text  FROM service_resource_master  WHERE serv_res_id::text=sai.resource_id ) " +
		" WHEN sai.resource_id LIKE 'DGC%' THEN " +
 	  	" (SELECT eq_id ::text FROM test_equipment_master  WHERE eq_id::text=sai.resource_id) " +
 	  	" END AS booked_resource_id, "  +
	    " CASE WHEN sm.res_sch_category = 'DOC' THEN (SELECT doctor_name FROM DOCTORS WHERE doctor_id=sa.prim_res_id) " +
	    " WHEN sm.res_sch_category = 'SNP' THEN (SELECT service_name FROM services WHERE service_id=sa.res_sch_name) " +
	    " WHEN sm.res_sch_category = 'DIA' THEN (SELECT test_name FROM diagnostics WHERE test_id=sa.res_sch_name) " +
	    " WHEN sm.res_sch_category = 'OPE' THEN (SELECT operation_name FROM operation_master WHERE op_id = sa.res_sch_name) " +
	    " END AS secondary_resource, " +
	    " CASE WHEN sm.res_sch_id = 1 THEN 'Consultation' " +
	    " WHEN sm.res_sch_id = 2 THEN 'Surgery' " +
	    " WHEN sm.res_sch_id = 3 THEN 'Service' " +
	    " WHEN sm.res_sch_id = 4 THEN 'Test' " +
	    " END AS appointment_type, " +
	    " CASE WHEN srt.resource_type = 'SUDOC' THEN 'Surgeon' " +
	    " WHEN srt.resource_type = 'ANEDOC' THEN 'Anesthetist' " +
	    " WHEN srt.resource_type = 'THID' THEN 'Operation Theatre' " +
	    " WHEN srt.resource_type = 'EQID' THEN 'Equipment' " +
	    " WHEN srt.resource_type = 'LABTECH' THEN 'Technician/Radiologist' " +
	    " WHEN srt.resource_type = 'DOC' THEN 'Doctor' " +
	    " WHEN srt.resource_type = 'OPDOC' THEN 'Doctor' " +
	    " WHEN srt.resource_type = 'SRID' THEN 'Service Resource' " +
	    " ELSE 'Generic Resource'" +
	    " END AS resource_type " +
	    " FROM scheduler_appointments sa " +
	    " LEFT JOIN admission adm ON(adm.mr_no = sa.mr_no) " +
	    " LEFT JOIN bed_names bn ON(bn.bed_id = adm.bed_id) " +
	    " LEFT JOIN ward_names wn ON(wn.ward_no = bn.ward_no) " +
	    " LEFT JOIN scheduler_master sm ON (sm.res_sch_id=sa.res_sch_id) " +
	    " LEFT JOIN scheduler_resource_types srt ON (srt.category=sm.res_sch_category ) " +
	    " JOIN scheduler_appointment_items sai ON (sa.appointment_id=sai.appointment_id AND sai.resource_type=srt.resource_type) " +
	    " WHERE  sa.appointment_id = ?";
	public static List<BasicDynaBean>getOperationDetails(Connection con,int appointId) throws SQLException{
		try(PreparedStatement ps = con.prepareStatement(GET_OPERATION_DETAILS)) {
			ps.setInt(1, appointId);
			return DataBaseUtil.queryToDynaList(ps);
		}
	}

	public static final String UPDATE_VISIT_ID = "UPDATE scheduler_appointments SET visit_id = ? " +
			" WHERE appointment_id = ?";
	public boolean updateVisitId(String visitId, int appointId) throws SQLException{
		int i = 0;
		try(PreparedStatement ps = con.prepareStatement(UPDATE_VISIT_ID)) {
		ps.setString(1, visitId);
		ps.setInt(2, appointId);
		i = ps.executeUpdate();
		}
		return (i > 0);
	}

	private static final String UPDATE_TEST_STATUS = "UPDATE tests_prescribed SET conducted = 'C' where appointment_id = ?";

	private static final String UPDATE_SERVICE_STATUS = "UPDATE services_prescribed SET conducted = ? where appointment_id = ?";
	private static final String UPDATE_OPE_STATUS = "UPDATE bed_operation_schedule SET status = 'C'  where appointment_id = ?";

	public boolean updateTestOrServiceOrOperationStatus(int appId, String Category) throws Exception {
		return updateTestOrServiceOrOperationStatus(appId, Category, null);
	}

	public boolean updateTestOrServiceOrOperationStatus(int appId, String category, String conducted)
		throws Exception {
		PreparedStatement ps = null;
		try {
			if (category.equals("DIA")) {
				ps = con.prepareStatement(UPDATE_TEST_STATUS);
				ps.setInt(1, appId);
			} else if (category.equals("SNP")) {
				ps = con.prepareStatement(UPDATE_SERVICE_STATUS);
				ps.setString(1, conducted != null ? conducted : "C");
				ps.setInt(2, appId);
			} else if (category.equals("OPE")) {
				ps = con.prepareStatement(UPDATE_OPE_STATUS);
				ps.setInt(1, appId);
			}
			return ps.executeUpdate() >= 0;
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}


	private static final String GET_CONSULTATION_TYPES_APPLICABLE_FLAG = "SELECT applicable FROM consultation_org_details WHERE org_id=? AND consultation_type_id=?";
	private static final String GET_OPERATION_APPLICABLE_FLAG = "SELECT applicable FROM operation_org_details WHERE org_id=? AND operation_id=? ";
	private static final String GET_TEST_APPLICABLE_FLAG = "SELECT applicable FROM test_org_details WHERE org_id=? AND test_id=?";
	private static final String GET_SERVICE_APPLICABLE_FLAG = "SELECT applicable FROM service_org_details WHERE org_id=? AND service_id=?";
	public static String isRatePlanApplicable(String category ,String orgId, String scheduleId) throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection();
			if (category.equals("OPE"))
				ps = con.prepareStatement(GET_OPERATION_APPLICABLE_FLAG);
			else if(category.equals("DIA"))
				ps = con.prepareStatement(GET_TEST_APPLICABLE_FLAG);
			else if(category.equals("SNP"))
				ps = con.prepareStatement(GET_SERVICE_APPLICABLE_FLAG);
			else if(category.equals("DOC"))
				ps = con.prepareStatement(GET_CONSULTATION_TYPES_APPLICABLE_FLAG);
			ps.setString(1, orgId);
			if (category.equals("DOC")) {
				ps.setInt(2, Integer.parseInt(scheduleId));
			} else {
				ps.setString(2, scheduleId);
			}
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static  String CHECK_IS_SLOT_IS_BOOKED = "SELECT * FROM " +
			" (SELECT *, appointment_time +(duration||' mins')::interval AS end_appointment_time " +
			" FROM scheduler_appointments sa " +
			" JOIN scheduler_appointment_items sai using(appointment_id)) as foo" +
			" WHERE (" +
			"			(appointment_time <= ? AND end_appointment_time > ?) " +
			"				OR (appointment_time >= ? AND appointment_time < ?) " +
			"       )"+
			" AND appointment_status NOT IN('Cancel','Noshow')" +
			" AND appointment_id != ? AND foo.resource_type = ? AND foo.resource_id = ? " +
			"  #waitlist ORDER BY end_appointment_time ";

  public static List<BasicDynaBean> isSlotBooked(Timestamp statrApptTime, Timestamp endApptTime,
      String resName, String appointmentId, String primaryResource, String primaryResourceType)
      throws SQLException {
    return isSlotBooked(statrApptTime, endApptTime, appointmentId, primaryResource,
        primaryResourceType, -1);
  }

  /**
   * Checks if is slot booked.
   *
   * @param startApptTime the start appt time
   * @param endApptTime the end appt time
   * @param appointmentId the appointment id
   * @param primaryResource the primary resource
   * @param primaryResourceType the primary resource type
   * @param waitlistNumber the waitlist number
   * @return the list
   * @throws SQLException
   */
  public static List<BasicDynaBean> isSlotBooked(Timestamp startApptTime, Timestamp endApptTime,
      String appointmentId, String primaryResource, String primaryResourceType,
      Integer waitlistNumber) throws SQLException {
    PreparedStatement ps = null;
    Connection con = null;
    int index = 1;
    try {
      con = DataBaseUtil.getConnection();
      String query = CHECK_IS_SLOT_IS_BOOKED;
      if (waitlistNumber != null && waitlistNumber != -1) {
        query = query.replace("#waitlist", "AND foo.waitlist > ?");

      } else {
        query = query.replace("#waitlist", "");
      }
      ps = con.prepareStatement(query);
      ps.setTimestamp(index++, startApptTime);
      ps.setTimestamp(index++, startApptTime);
      ps.setTimestamp(index++, startApptTime);
      ps.setTimestamp(index++, endApptTime);
      if (appointmentId != null && !appointmentId.equals("") && !appointmentId.equals("No")) {
        ps.setInt(index++, Integer.parseInt(appointmentId));
      } else {
        ps.setInt(index++, -1);
      }
      ps.setString(index++, primaryResourceType);
      ps.setString(index++, primaryResource);
      if (waitlistNumber != null && waitlistNumber != -1) {
        ps.setInt(index++, waitlistNumber);
      }
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

	public static final String CHECK_IS_RESOURCE_BOOKED =" SELECT * FROM " +
				" (select sa.appointment_id,res_sch_name,appointment_time,sai.resource_type,sai.resource_id,res.overbook_limit," +
				" 	appointment_time+(duration||' mins')::interval AS end_appointment_time,sa.appointment_status " +
				" 	FROM scheduler_appointments sa " +
				" 	JOIN scheduler_appointment_items sai ON(sa.appointment_id=sai.appointment_id) #) as foo" +
				" WHERE (" +
				"			(appointment_time <= ? AND end_appointment_time > ?) " +
				"				OR (appointment_time >= ? AND appointment_time < ?) " +
				"       )"+
				" AND appointment_status NOT IN('Cancel','Noshow')" +
				" AND appointment_id != ? AND foo.resource_id::text = ? AND appointment_time::date = ?" +
				" @ " +
				" ORDER BY end_appointment_time ";

	public static final String DOCTOR_QUERY = " JOIN doctors res ON(res.doctor_id = sai.resource_id)";

	public static final String EQUIPMENT_QUERY = " JOIN test_equipment_master res ON(res.eq_id::text = sai.resource_id)";

	public static final String SERVICE_RESOURCES_QUERY = " JOIN service_resource_master res ON(res.serv_res_id::text = sai.resource_id)";

	public static final String THEATRE_QUERY = " JOIN theatre_master res ON(res.theatre_id = sai.resource_id)";

	public static final String GENERIC_RESOURCE_QUERY = " JOIN generic_resource_master res ON(res.generic_resource_id::text = sai.resource_id) " +
										   " JOIN generic_resource_type grt ON(res.generic_resource_type_id = grt.generic_resource_type_id)	";
	
  public BasicDynaBean isResourceBooked(Timestamp statrApptTime, Timestamp endApptTime,
      String resourceId, String resourceType, int appointmentId, String category)
      throws SQLException {
    return isResourceBooked(statrApptTime, endApptTime,
        resourceId, resourceType, appointmentId, category, true);
  }
	public BasicDynaBean isResourceBooked(Timestamp statrApptTime, Timestamp endApptTime,String resourceId,String resourceType, int appointmentId, String category, boolean isSharingAlreadyBookedSlot) throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		int index = 1;
		
		try {
			con = DataBaseUtil.getConnection();
			String checkResourceBookedQuery = CHECK_IS_RESOURCE_BOOKED;
			if (resourceType.equals("SUDOC") || resourceType.equals("ANEDOC") || resourceType.equals("OPDOC") || resourceType.equals("DOC") || resourceType.equals("LABTECH")) {
			  checkResourceBookedQuery = checkResourceBookedQuery.replace("#", DOCTOR_QUERY);
			  checkResourceBookedQuery = checkResourceBookedQuery.replace("@", "");
			} else if (resourceType.equals("EQID")) {
				checkResourceBookedQuery = checkResourceBookedQuery.replace("#", EQUIPMENT_QUERY);
				checkResourceBookedQuery = checkResourceBookedQuery.replace("@", "AND resource_type = ?");
			} else if (resourceType.equals("THID")) {
				checkResourceBookedQuery = checkResourceBookedQuery.replace("#", THEATRE_QUERY);
				checkResourceBookedQuery = checkResourceBookedQuery.replace("@", "AND resource_type = ?");
			} else if (resourceType.equals("SRID")) {
				checkResourceBookedQuery = checkResourceBookedQuery.replace("#", SERVICE_RESOURCES_QUERY);
				checkResourceBookedQuery = checkResourceBookedQuery.replace("@", "AND resource_type = ?");
			} else {
				checkResourceBookedQuery = checkResourceBookedQuery.replace("#", GENERIC_RESOURCE_QUERY);
				checkResourceBookedQuery = checkResourceBookedQuery.replace("@", "AND resource_type = ?");
			}

			ps = con.prepareStatement(checkResourceBookedQuery);
			ps.setTimestamp(index++, statrApptTime);
			ps.setTimestamp(index++, statrApptTime);
			ps.setTimestamp(index++, statrApptTime);
			ps.setTimestamp(index++, endApptTime);
			ps.setInt(index++, appointmentId);
			ps.setString(index++, resourceId);
			ps.setDate(index++,new java.sql.Date(statrApptTime.getTime()));
			
			if(!resourceType.equals("SUDOC") && !resourceType.equals("OPDOC") && !resourceType.equals("DOC") &&
					!resourceType.equals("ANEDOC") && !resourceType.equals("LABTECH"))
			ps.setString(index++, resourceType);
			List<BasicDynaBean> isResourceAllowed = DataBaseUtil.queryToDynaList(ps);
			
			if (isResourceAllowed != null && !isResourceAllowed.isEmpty()) {
				int overbookCount = isResourceAllowed.size()-1;
				BasicDynaBean bean = isResourceAllowed.get(0);
				Integer overbook_limit = (Integer) bean.get("overbook_limit");
				if(!isSharingAlreadyBookedSlot){
				  overbook_limit=0;
				}
				logger.debug("overbook_allowed=" + overbook_limit);
				if (overbook_limit != null && (overbook_limit == 0 || overbookCount >= overbook_limit)) {
					return bean;
				} 
			}
		
			
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return  null;
	}

	public boolean isOverBookingAllowedForTheUser(String resourceId, String resourceType,
			Timestamp statrApptTime) {
		int roleId = RequestContext.getRoleId();
    Map actionRightsMap = (Map) RequestContext.getSession().getAttribute("actionRightsMap");
		boolean allowApptOverbooking = "A".equals(actionRightsMap.get("allow_appt_overbooking"));
		int numberOfAppts = 0;
		int overbookLimit = 0;
		Connection con = null;
		try  {
			con = DataBaseUtil.getConnection();
			overbookLimit = isResourceOverbooked(con, resourceId, resourceType);
			numberOfAppts = getOverbookCount(resourceId, statrApptTime);
	  } catch (Exception e) {
      logger.error("error getting data...."+ e.getCause());
		}
    if ((roleId != 1 && roleId != 2) && !allowApptOverbooking && numberOfAppts >= 1 && overbookLimit != 0) {
      return false;
		}
		return true;
	}



	public static final String GET_DOCTOR = "SELECT doctor_name FROM doctors where doctor_id = ?";
	public static final String GET_EQUIPMENT = "SELECT equipment_name FROM test_equipment_master where eq_id::text = ?";
	public static final String GET_SERVICE_RESOURCE = "SELECT serv_resource_name FROM service_resource_master where serv_res_id::text = ?";
	public static final String GET_SURGERY = "SELECT operation_name FROM operation_master where op_id = ?";
	public static final String GET_TEST = "SELECT test_name FROM diagnostics where test_id = ?";
	public static final String GET_SERVICE = "SELECT service_name FROM services where service_id= ?";
	public static final String GET_THEATRE = "SELECT theatre_name FROM theatre_master where theatre_id= ?";
	public static final String GET_GENERIC_RESOURCES = " SELECT generic_resource_name FROM generic_resource_master grm " +
			" JOIN generic_resource_type grt ON(grm.generic_resource_type_id = grt.generic_resource_type_id) " +
			" where generic_resource_id::text = ?";

	public static String getResourceName(String resType,String resourceId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			if (resType.equals("OPDOC") ||resType.equals("ANEDOC")||resType.equals("SUDOC") || resType.equals("DOC") || resType.equals("LABTECH"))
				ps = con.prepareStatement(GET_DOCTOR);
			else if(resType.equals("EQID"))
				ps = con.prepareStatement(GET_EQUIPMENT);
			else if(resType.equals("SRID"))
				ps = con.prepareStatement(GET_SERVICE_RESOURCE);
			else if(resType.equals("SUR"))
				ps = con.prepareStatement(GET_SURGERY);
			else if(resType.equals("TST"))
				ps = con.prepareStatement(GET_TEST);
			else if(resType.equals("SER"))
				ps = con.prepareStatement(GET_SERVICE);
			else if(resType.equals("THID"))
				ps = con.prepareStatement(GET_THEATRE);
			else
				ps = con.prepareStatement(GET_GENERIC_RESOURCES);
			ps.setString(1, resourceId);
			return DataBaseUtil.getStringValueFromDb(ps);

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	/*
	 * To get resource name based on res_sch_id of scheduler_appointments.
	 * It is used in avoid multiple appointments scenario.
	 */
	public static String getResourceName(int resourceId,String resoureName) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			if (resourceId == 1)
				ps = con.prepareStatement(GET_DOCTOR);
			else if(resourceId == 2)
				ps = con.prepareStatement(GET_SURGERY);
			else if(resourceId == 3)
				ps = con.prepareStatement(GET_SERVICE);
			else if(resourceId == 4)
				ps = con.prepareStatement(GET_TEST);
			if(ps!=null){
			  ps.setString(1, resoureName);
			  }
			return DataBaseUtil.getStringValueFromDb(ps);

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static final String GET_RESOURCE_AVAILABILITIES = "SELECT *,day_of_week::text AS day_of_week_text,hc.center_name "+
			 " FROM sch_default_res_availability_details sc "+ 
			 " LEFT JOIN hospital_center_master hc ON (sc.center_id=hc.center_id) "+
			 " where res_sch_id = ? AND from_time != to_time order by from_time ";
	
	public static List getResourceAvailabilities(int resourceId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESOURCE_AVAILABILITIES);
			ps.setInt(1, resourceId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_DEFAULT_RESOURCE_AVAILABILITIES = "SELECT * FROM scheduler_master sm " +
			" JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) " +
			" WHERE day_of_week = ? AND sm.res_sch_name = ? AND res_sch_type = ? AND from_time != to_time AND sm.status = 'A' # order by sdra.from_time";

	public  List getResourceDefaultAvailabilities(String resource,int dayOfWeek,String category,String status,Integer centerId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
		int userCenter = RequestContext.getCenterId();
		List<BasicDynaBean> availabilityList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> filteredAvailabilityList = new ArrayList<BasicDynaBean>();
		try {
			con = DataBaseUtil.getConnection();
			String query = null;
			if(status != null)
				query = GET_DEFAULT_RESOURCE_AVAILABILITIES.replace("#", "AND availability_status = ?");
			else
				query = GET_DEFAULT_RESOURCE_AVAILABILITIES.replace("#", "");
			ps = con.prepareStatement(query);
			ps.setInt(1, dayOfWeek);
			ps.setString(2, resource);
			ps.setString(3, category);
			if(status != null){
				ps.setString(4, status);
			}
			availabilityList = DataBaseUtil.queryToDynaList(ps);
			if (max_center > 1 && category.equals("DOC") && centerId != null) {
				filteredAvailabilityList = ResourceBO.filterAllResourcesAvailability(userCenter, availabilityList,centerId);
			} else {
				filteredAvailabilityList = availabilityList;
			}
			return filteredAvailabilityList;

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static final String GET_RESOURCE_AVAILABILITIES_LIST = "SELECT * FROM sch_resource_availability sra " +
			" JOIN sch_resource_availability_details srad ON (srad.res_avail_id = sra.res_avail_id)   " +
			" WHERE  res_sch_type = ? AND res_sch_name = ? AND availability_date=? AND from_time != to_time # order by srad.from_time";

	public static final String WHERE_CLAUSE = "AND availability_status = ? ";

	public  List getResourceAvailabilities(String category,Date availDate,String resourceName,String status, Integer centerId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int max_center = (Integer)new GenericPreferencesDAO().getAllPrefs().get("max_centers_inc_default");
		int userCenter = RequestContext.getCenterId();
		int index = 1;
		List<BasicDynaBean> availabilityList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> filteredAvailabilityList = new ArrayList<BasicDynaBean>();
		try {
			con = DataBaseUtil.getConnection();
			String query = null;
			if (status != null) {
				query = GET_RESOURCE_AVAILABILITIES_LIST.replace("#", WHERE_CLAUSE);
			} else {
				query = GET_RESOURCE_AVAILABILITIES_LIST.replace("#", "");
			}
			ps = con.prepareStatement(query);
			ps.setString(index++, category);
			ps.setString(index++, resourceName);
			ps.setDate(index++, new java.sql.Date(availDate.getTime()));
			if (status != null && !status.equals("")) {
				ps.setString(index++, status);
			}
			availabilityList = DataBaseUtil.queryToDynaList(ps);
			if (max_center > 1 && category.equals("DOC") && centerId !=null) {
				filteredAvailabilityList = ResourceBO.filterAllResourcesAvailability(userCenter,availabilityList,centerId);
			} else {
				filteredAvailabilityList = availabilityList;
			}
			return filteredAvailabilityList;
		} finally{
			DataBaseUtil.closeConnections(con, ps);
			}
	}

	public static final String GET_RESCHEDULE_APP_DETAILS = " SELECT *,appointment_time::date appointment_date,appointment_time::time app_time,sa.res_sch_id FROM scheduler_appointments sa " +
			" JOIN scheduler_appointment_items sai USING(appointment_id) " +
			" WHERE  sa.appointment_id = ?";


	public static List getRescheduleAppDetails(int appointmentId) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESCHEDULE_APP_DETAILS);
			ps.setInt(1, appointmentId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String GET_DOCTORS_SCHEDULENAMES_FOR_TODAYS_SCREEN = "SELECT doctor_id as resource_id, doctor_name as resource_name,overbook_limit,practition_type," +
			" op_consultation_validity,allowed_revisit_count,dept_id " +
			" FROM doctors WHERE status = 'A' AND schedule = true order by doctor_name";

	public List getScheduledDoctorsList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DOCTORS_SCHEDULENAMES_FOR_TODAYS_SCREEN);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static int updateSchedulerPriorInfo(Connection con,int appointmentId,String schPriorAuthId, int schPriorAuthModeId) throws SQLException,IOException{
		List columns = new ArrayList();
		Map columndata = new HashMap();
		columns.add("scheduler_prior_auth_no");
		columns.add("scheduler_prior_auth_mode_id");
		Map keys = new HashMap();
		keys.put("appointment_id", appointmentId);
		columndata.put("scheduler_prior_auth_no",schPriorAuthId);
		columndata.put("scheduler_prior_auth_mode_id",schPriorAuthModeId);
		int i = schedulerAppointmentsDAO.update(con, columns, columndata, keys);
		return i;
	}

	private static final String GET_RESOURCE_MIN_FROM_AND_MAX_TO_TIME = " SELECT min(from_time)as from_time,max(to_time)as to_time " +
			" FROM sch_resource_availability_details srad " +
			" JOIN sch_resource_availability sra ON(srad.res_avail_id=sra.res_avail_id) " +
			" WHERE availability_status ='A' and availability_date = ? AND res_sch_type = ?";

	public static BasicDynaBean getResourceMinFromAndMaxToTime(ResourceCriteria rc,String res_sch_type) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		int index =1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query = new StringBuilder(GET_RESOURCE_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where,"res_sch_name",rc.scheduleName,true);
			query.append(where);
			ps = con.prepareStatement(query.toString());
			ps.setDate(index++, new java.sql.Date(rc.choosendate.getTime()));
			ps.setString(index++, res_sch_type);
			for(int i=0;i<rc.scheduleName.size();i++) {
				ps.setString(index++, rc.scheduleName.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_RESOURCE_DEFAULT_MIN_FROM_AND_MAX_TO_TIME = "SELECT from_time,to_time " +
			"   FROM scheduler_master sm " +
			"   JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) " +
			"   WHERE day_of_week = ? AND res_sch_type = ? AND from_time != to_time AND status = 'A' AND availability_status ='A' ";

	public static BasicDynaBean getResourceDefaultMinFromAndMaxToTime(ResourceCriteria rc,int dayofweek,String resourceType) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query1 = new StringBuilder(GET_RESOURCE_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder query2 = new StringBuilder(GET_RESOURCE_DEFAULT_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder where1 = new StringBuilder();
			StringBuilder where2 = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where1,"res_sch_name",rc.scheduleName,true);
			DataBaseUtil.addWhereFieldInList(where2,"res_sch_name",rc.schName,true);
			query1.append(where1);
			query2.append(where2);
			StringBuilder query = new StringBuilder("SELECT min(from_time) as from_time ,max(to_time) as to_time FROM(");
			query.append(query1);
			query.append(" UNION ALL");
			query.append(query2);
			query.append(" ) AS foo");
			ps = con.prepareStatement(query.toString());
			ps.setDate(index++, new java.sql.Date(rc.choosendate.getTime()));
			ps.setString(index++, resourceType);
			for(int i=0;i<rc.scheduleName.size();i++) {
				ps.setString(index++, rc.scheduleName.get(i));
			}
			ps.setInt(index++, dayofweek);
			ps.setString(index++, resourceType);
			for(int i=0;i<rc.schName.size();i++) {
				ps.setString(index++, rc.schName.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_OVERRIDEN_TIMINGS_FOR_ALL_RESOURCES = " SELECT * " +
			" FROM sch_resource_availability sra " +
			" WHERE availability_date = ? AND res_sch_name = ? AND res_sch_type = ?";

	public static boolean isOverridenTimingPresentForAllResources(ResourceCriteria rc,String resourceType) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		boolean flag = false;
		int index = 0;
		if(rc.schName.size() != rc.scheduleName.size()) {
			rc.schName = new ArrayList<String>();
			rc.schName.addAll(rc.scheduleName);
		}
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for(int i=0;i<rc.scheduleName.size();i++) {
				ps = con.prepareStatement(GET_OVERRIDEN_TIMINGS_FOR_ALL_RESOURCES);
				ps.setDate(1, new java.sql.Date(rc.choosendate.getTime()));
				ps.setString(2, rc.scheduleName.get(i));
				ps.setString(3, resourceType);
				bean = DataBaseUtil.queryToDynaBean(ps);
				if(bean != null) {
					rc.schName.set(i, null);
					index++;
				}
			}
			if(index == rc.scheduleName.size())
				flag = true;
			return flag;

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_DEFAULT_TIMINGS_FOR_ALL_RESOURCES = " SELECT * FROM scheduler_master sm " +
			" WHERE sm.res_sch_name = ? AND res_sch_type = ? AND status = 'A'";

	public static boolean isDefaultTimingPresentForAllResources(ResourceCriteria rc,int weekday,String resourceType) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		boolean flag = false;
		int index = 0;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for(int i=0;i<rc.schName.size();i++) {
				ps = con.prepareStatement(GET_DEFAULT_TIMINGS_FOR_ALL_RESOURCES);
				ps.setString(1, rc.schName.get(i));
				ps.setString(2, resourceType);
				bean = DataBaseUtil.queryToDynaBean(ps);
				if(bean != null) {
					index++;
				}
			}
			if(index == rc.schName.size())
				flag = true;
			return flag;

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_CATEGORY_DEFAULT_MIN_FROM_AND_MAX_TO_TIME = "SELECT from_time,to_time " +
			"   FROM scheduler_master sm " +
			"   JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) " +
			"   WHERE day_of_week = ? AND res_sch_type = ? AND from_time != to_time AND status = 'A' AND availability_status ='A' " +
			"   AND res_sch_name = '*'";

	public static BasicDynaBean schedulerCategoryDefaultMinFromAndMaxToTime(ResourceCriteria rc,int dayofweek,String resourceType) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query1 = new StringBuilder(GET_RESOURCE_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder query2 = new StringBuilder(GET_RESOURCE_DEFAULT_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder query3 = new StringBuilder(GET_CATEGORY_DEFAULT_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder where1 = new StringBuilder();
			StringBuilder where2 = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where1,"res_sch_name",rc.scheduleName,true);
			DataBaseUtil.addWhereFieldInList(where2,"res_sch_name",rc.schName,true);
			query1.append(where1);
			query2.append(where2);
			StringBuilder query = new StringBuilder("SELECT min(from_time) AS from_time,max(to_time) AS to_time FROM(");
			query.append(query1);
			query.append(" UNION ALL ");
			query.append(query2);
			query.append(" UNION ALL ");
			query.append(query3);
			query.append(" ) AS foo");
			ps = con.prepareStatement(query.toString());
			ps.setDate(index++, new java.sql.Date(rc.choosendate.getTime()));
			ps.setString(index++, resourceType);
			for(int i=0;i<rc.scheduleName.size();i++) {
				ps.setString(index++, rc.scheduleName.get(i));
			}
			ps.setInt(index++, dayofweek);
			ps.setString(index++, resourceType);
			for(int i=0;i<rc.schName.size();i++) {
				ps.setString(index++, rc.schName.get(i));
			}
			ps.setInt(index++,dayofweek);
			ps.setString(index++, resourceType);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_RESOURCE_MIN_AND_MAX_APPOINTMENT_TIME = " SELECT (min(appointment_time))::time as from_time," +
			"   (max(appointment_time+(duration||' mins')::interval))::time as to_time " +
			"   from scheduler_appointments sa " +
			"   JOIN scheduler_appointment_items sai ON(sa.appointment_id=sai.appointment_id) " +
			"   JOIN scheduler_resource_types srt ON(srt.resource_type=sai.resource_type) AND srt.primary_resource = true" +
			"  	where date(appointment_time) = ? AND category = ? " +
			"   AND appointment_status NOT IN('Cancel','No show') ";

	public static BasicDynaBean getResourceMaxAndMinAppointmentTime(ResourceCriteria rc,String resourceType) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query = new StringBuilder(GET_RESOURCE_MIN_AND_MAX_APPOINTMENT_TIME);
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where,"sai.resource_id",rc.scheduleName,true);
			query.append(where);
			ps = con.prepareStatement(query.toString());
			ps.setDate(index++, new java.sql.Date(rc.choosendate.getTime()));
			ps.setString(index++, resourceType);
			for(int i=0;i<rc.scheduleName.size();i++) {
				ps.setString(index++, rc.scheduleName.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WEEKLY_OVERRIDEN_TIMINGS_FOR_ALL_RESOURCES = " SELECT * " +
			" FROM sch_resource_availability sra " +
			" WHERE availability_date = ? AND res_sch_name = ? AND res_sch_type ='DOC'";

	public static boolean isWeeklyOverridenTimingPresentForAllResources(ResourceCriteria rc) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		boolean flag = false;
		if(rc.tempDatesArray.size() != rc.datesArray.size()) {
			rc.tempDatesArray = new ArrayList<Date>();
			rc.tempDatesArray.addAll(rc.datesArray);
		}
		int index = 0;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			for(int i=0;i<rc.datesArray.size();i++) {
				ps = con.prepareStatement(GET_WEEKLY_OVERRIDEN_TIMINGS_FOR_ALL_RESOURCES);
				ps.setDate(1, rc.datesArray.get(i));
				ps.setString(2, rc.scheduleName.get(0));
				bean = DataBaseUtil.queryToDynaBean(ps);
				if(bean != null) {
					rc.tempDatesArray.set(i, null);
					index++;
				}
			}
			if(index == rc.datesArray.size())
				flag = true;
			return flag;

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WEEKLY_RESOURCE_MIN_FROM_AND_MAX_TO_TIME = " SELECT min(from_time)as from_time,max(to_time)as to_time " +
			" FROM sch_resource_availability_details srad " +
			" JOIN sch_resource_availability sra ON(srad.res_avail_id=sra.res_avail_id) " +
			" WHERE availability_status ='A' AND res_sch_type = 'DOC' AND res_sch_name = ?";

	public static BasicDynaBean getWeeklyResourceMinFromAndMaxToTime(ResourceCriteria rc) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		int index =1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query = new StringBuilder(GET_WEEKLY_RESOURCE_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where,"availability_date",rc.datesArray,true);
			query.append(where);
			ps = con.prepareStatement(query.toString());
			ps.setString(index++, rc.scheduleName.get(0));
			for(int i=0;i<rc.datesArray.size();i++) {
				ps.setDate(index++, rc.datesArray.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WEEKLY_DEFAULT_TIMINGS_FOR_ALL_RESOURCES = "SELECT * FROM scheduler_master sm " +
			" WHERE sm.res_sch_name = ? AND res_sch_type = 'DOC' AND status = 'A'";

	public static boolean isWeeklyDefaultTimingPresentForAllResources(ResourceCriteria rc) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		boolean flag = false;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_WEEKLY_DEFAULT_TIMINGS_FOR_ALL_RESOURCES);
			ps.setString(1, rc.scheduleName.get(0));
			bean = DataBaseUtil.queryToDynaBean(ps);
			if(bean != null)
				flag = true;
			return flag;

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WEEKLY_RESOURCE_DEFAULT_MIN_FROM_AND_MAX_TO_TIME = "SELECT from_time,to_time " +
			"   FROM scheduler_master sm " +
			"   JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) " +
			"   WHERE res_sch_type = 'DOC' AND from_time != to_time AND status = 'A' AND availability_status ='A' AND res_sch_name = ?";

	public static BasicDynaBean getWeeklyResourceDefaultMinFromAndMaxToTime(ResourceCriteria rc) throws SQLException,IOException{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query1 = new StringBuilder(GET_WEEKLY_RESOURCE_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder query2 = new StringBuilder(GET_WEEKLY_RESOURCE_DEFAULT_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder where1 = new StringBuilder();
			StringBuilder where2 = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where1,"availability_date",rc.datesArray,true);
			DataBaseUtil.addWhereFieldInList(where2,"day_of_week",rc.dayOfweek,true);
			query1.append(where1);
			query2.append(where2);
			StringBuilder query = new StringBuilder("SELECT min(from_time) as from_time ,max(to_time) as to_time FROM(");
			query.append(query1);
			query.append(" UNION ");
			query.append(query2);
			query.append(" ) AS foo");
			ps = con.prepareStatement(query.toString());
			ps.setString(index++,rc.scheduleName.get(0));
			for(int i=0;i<rc.datesArray.size();i++) {
				ps.setDate(index++, rc.datesArray.get(i));
			}
			ps.setString(index++,rc.scheduleName.get(0));
			for(int i=0;i<rc.dayOfweek.size();i++) {
				ps.setInt(index++, rc.dayOfweek.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_WEEKLY_CATEGORY_DEFAULT_MIN_FROM_AND_MAX_TO_TIME = "SELECT from_time,to_time " +
			"   FROM scheduler_master sm " +
			"   JOIN sch_default_res_availability_details sdra ON (sdra.res_sch_id = sm.res_sch_id) " +
			"   WHERE res_sch_type = 'DOC' AND from_time != to_time AND status = 'A' AND availability_status ='A' " +
			"   AND res_sch_name = '*'";

	public static BasicDynaBean schedulerWeeklyCategoryDefaultMinFromAndMaxToTime(ResourceCriteria rc) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query1 = new StringBuilder(GET_WEEKLY_RESOURCE_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder query2 = new StringBuilder(GET_WEEKLY_RESOURCE_DEFAULT_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder query3 = new StringBuilder(GET_WEEKLY_CATEGORY_DEFAULT_MIN_FROM_AND_MAX_TO_TIME);
			StringBuilder where1 = new StringBuilder();
			StringBuilder where2 = new StringBuilder();
			StringBuilder where3 = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where1,"availability_date",rc.datesArray,true);
			DataBaseUtil.addWhereFieldInList(where2,"day_of_week",rc.dayOfweek,true);
			DataBaseUtil.addWhereFieldInList(where3,"day_of_week",rc.dayOfweek,true);
			query1.append(where1);
			query2.append(where2);
			query3.append(where3);
			StringBuilder query = new StringBuilder("SELECT min(from_time) AS from_time,max(to_time) AS to_time FROM(");
			query.append(query1);
			query.append(" UNION ALL ");
			query.append(query2);
			query.append(" UNION ALL ");
			query.append(query3);
			query.append(" ) AS foo");
			ps = con.prepareStatement(query.toString());
			ps.setString(index++,rc.scheduleName.get(0));
			for(int i=0;i<rc.datesArray.size();i++) {
				ps.setDate(index++, rc.datesArray.get(i));
			}
			ps.setString(index++,rc.scheduleName.get(0));
			for(int i=0;i<rc.dayOfweek.size();i++) {
				ps.setInt(index++, rc.dayOfweek.get(i));
			}
			for(int i=0;i<rc.dayOfweek.size();i++) {
				ps.setInt(index++, rc.dayOfweek.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_WEEKLY_RESOURCE_MIN_AND_MAX_APPOINTMENT_TIME = " SELECT (min(appointment_time))::time as from_time," +
			"   (max(appointment_time+(duration||' mins')::interval))::time as to_time " +
			"   from scheduler_appointments sa " +
			"   JOIN scheduler_appointment_items sai ON(sa.appointment_id=sai.appointment_id) " +
			"   JOIN scheduler_resource_types srt ON(srt.resource_type=sai.resource_type) AND srt.primary_resource = true" +
			"  	where category = 'DOC' AND sai.resource_id = ?" +
			"   AND appointment_status NOT IN('Cancel','No show') ";

	public static BasicDynaBean getWeeklyResourceMaxAndMinAppointmentTime(ResourceCriteria rc) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			StringBuilder query = new StringBuilder(GET_WEEKLY_RESOURCE_MIN_AND_MAX_APPOINTMENT_TIME);
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where,"date(appointment_time)",rc.datesArray,true);
			query.append(where);
			ps = con.prepareStatement(query.toString());
			ps.setString(index++,rc.scheduleName.get(0));
			for(int i=0;i<rc.datesArray.size();i++) {
				ps.setDate(index++, rc.datesArray.get(i));
			}
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_GENERIC_RESOURCE_TYPES =
			"SELECT  distinct(resource_type) as category FROM scheduler_resource_types WHERE resource_group = 'GEN'";

	public static List<String> getGenricResourceTypes() throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_GENERIC_RESOURCE_TYPES);
			return DataBaseUtil.queryToStringList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_APPOINTMENTS_WITHIN_GIVEN_TIME = "SELECT appointment_id,appointment_status FROM " +
			" (SELECT sa.appointment_id,appointment_time,sa.appointment_status,sai.resource_id::text, " +
			"	appointment_time+(duration||' mins')::interval AS end_appointment_time " +
			" 	FROM scheduler_appointments sa " +
			"	JOIN scheduler_appointment_items sai ON(sa.appointment_id = sai.appointment_id) # ) as foo " +
			"WHERE resource_id::text = ? AND ((appointment_time <= ? AND end_appointment_time > ?) OR (appointment_time >= ? AND appointment_time < ?))";

	public List<BasicDynaBean> getAppointments(Timestamp fromTime,Timestamp toTime,String resourceId,String category) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_APPOINTMENTS_WITHIN_GIVEN_TIME;
		if(category.equals("DOC")) {
		  query = query.replace("#", DOCTOR_QUERY);
		} else if(category.equals("OPE")) {
			query = query.replace("#", THEATRE_QUERY);
		} else if(category.equals("DIA")) {
			query = query.replace("#", EQUIPMENT_QUERY);
		} else if(category.equals("SNP")) {
			query = query.replace("#", SERVICE_RESOURCES_QUERY);
		}

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(query);
			ps.setString(1, resourceId);
			ps.setTimestamp(2,fromTime);
			ps.setTimestamp(3,fromTime);
			ps.setTimestamp(4,fromTime);
			ps.setTimestamp(5,toTime);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE = " SELECT * FROM scheduler_master WHERE res_sch_category = ? AND res_sch_name = ?";

	public static BasicDynaBean getDefaultAttributesOfResource(String category,String resourceId) throws SQLException{
		return DataBaseUtil.queryToDynaBean(GET_DEFAULT_ATTRIBUTES_FOR_RESOURCE, new Object[]{category,resourceId});
	}
 // method for scheduler
	public static BasicDynaBean getDefaultSchedulerBean(String incResources, String category) throws SQLException{

		BasicDynaBean bean=null;

		if(category == "DOC" || category.equals("DOC"))

			 bean = DoctorMasterDAO.getDefaultDoctorDeptBean(incResources);
		else
			bean = TheatreMasterDAO.getDefaultTheatreBean(incResources);
		return bean ;
	}
	// mobile related changes starts
	private static final String GET_MOBILE_APPOINTMENT_DETAILS = "select * from scheduler_appointments where res_sch_id = 1 AND prim_res_id = ? # ";

	public static List<BasicDynaBean> getAppointmentDetails(String resourceId,Date appointmentDate) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		String query = new String(GET_MOBILE_APPOINTMENT_DETAILS);
		try {
			con = DataBaseUtil.getConnection();
			query = query.replace("#", " AND date(appointment_time) = ? ");
			ps = con.prepareStatement(query);
			ps.setString(1, resourceId);
			ps.setDate(2, new java.sql.Date(appointmentDate.getTime()));
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static final String GET_SLOT_FOR_PATIENT ="SELECT * FROM"+
						"(SELECT *,appointment_time+(duration||' mins')::interval AS end_appointment_time " +
						" FROM scheduler_appointments sa )as foo" +
						" WHERE (" +
						"(appointment_time <= ? AND end_appointment_time > ?) " +
						"OR (appointment_time >= ? AND appointment_time < ?) " +
						"   )"+
						" AND appointment_status NOT IN('Cancel','Noshow') "+
						" AND appointment_id != ?" +
						" # " +
						" ORDER BY end_appointment_time ";
	public static List<BasicDynaBean> IsExitsAppointment(Timestamp statrApptTime, Timestamp endApptTime,int appointmentId, String mrno, String patientName, String mobileNo, Integer contactId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		String query = new String(GET_SLOT_FOR_PATIENT);
		if (mrno == null || mrno.equals("")) {
		  if (contactId == null) {
	      query = query.replace("#","AND patient_name = ? AND patient_contact = ?");
		    
		  } else {
		    query = query.replace("#","AND contact_id = ? ");
		  }
		} else {
			query = query.replace("#","AND mr_no = ? ");
		}
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setTimestamp(index++, statrApptTime);
			ps.setTimestamp(index++, statrApptTime);
			ps.setTimestamp(index++, statrApptTime);
			ps.setTimestamp(index++, endApptTime);
			if(mrno != null && !mrno.equals("")) {
				ps.setInt(index++, appointmentId);
				ps.setString(index++,mrno);
			} else if (contactId != null) {
        ps.setInt(index++, appointmentId);
			  ps.setInt(index++, contactId);
			} else {
				ps.setInt(index++, appointmentId);
				ps.setString(index++,patientName);
				ps.setString(index++,mobileNo);
			}

			return  DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE =
			"   WHERE ap.mr_no = ? AND ap.res_sch_id = ? AND appointment_status IN(?,?) " +
			"	AND date(appointment_time) >= ? AND date(appointment_time) <= ?";

	public static List<BasicDynaBean> getPatientTodaysAppointments(String mrNo) throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		int index = 1;
		int centerId = RequestContext.getCenterId();
		String query = GET_APPOINTMENT_DETAILS+GET_PATIENTS_ALL_TODAYS_APPOINTMENTS_WHERE_CLAUSE;
		if (centerId != 0)
			query = query + " AND ap.center_id = ?";
		query = query + " order by appointment_time";
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(index++, mrNo);
			ps.setInt(index++, 1);
			ps.setString(index++, "Booked");
			ps.setString(index++, "Confirmed");
			//ps.setString(index++, "Channel");
			ps.setDate(index++, DateUtil.getCurrentDate());
			ps.setDate(index++, DateUtil.getCurrentDate());
			if (centerId != 0)
				ps.setInt(index++, centerId);

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_APPOINTMENTS_FOR_A_RESOURCE ="SELECT * FROM "+
			" (SELECT sa.*,sai.resource_id,appointment_time+(duration||' mins')::interval AS end_appointment_time " +
			" FROM scheduler_appointments sa " +
			" JOIN scheduler_appointment_items sai ON(sa.appointment_id = sai.appointment_id) # )as foo" +
			" WHERE resource_id::text = ? " +
			" AND appointment_status NOT IN('Cancel','Noshow') "+
			" AND ((appointment_time <= ? AND end_appointment_time > ?) " +
			" OR  (appointment_time >= ? AND appointment_time < ?)) " +
			" ORDER BY end_appointment_time ";

	public List<BasicDynaBean> getResourceAppointments (String resourceId, String resourceType, Timestamp startTime, Timestamp endTime) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		int index = 1;
		String query = new String(GET_APPOINTMENTS_FOR_A_RESOURCE);
		if(resourceType.equals("DOC")) {
			query = query.replace("#", DOCTOR_QUERY);
		} else if(resourceType.equals("OPE")) {
			query = query.replace("#", THEATRE_QUERY);
		} else if(resourceType.equals("DIA")) {
			query = query.replace("#", EQUIPMENT_QUERY);
		} else if(resourceType.equals("SNP")) {
			query = query.replace("#", SERVICE_RESOURCES_QUERY);
		}
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(index++, resourceId);
			ps.setTimestamp(index++, startTime);
			ps.setTimestamp(index++, startTime);
			ps.setTimestamp(index++, startTime);
			ps.setTimestamp(index++, endTime);

			return  DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public List<BasicDynaBean> getResourceAppointmentDetails(Connection con,String resourceType,String resourceId) throws SQLException{
		Date currentDate = DateUtil.getCurrentDate();
		PreparedStatement ps = null;
		String query =  " SELECT *,appointment_time as appointment_start_time, " +
						" appointment_time+(duration||' mins')::interval AS end_appointment_time" +
						" FROM scheduler_appointments sa" +
						" JOIN scheduler_appointment_items sai ON(sa.appointment_id = sai.appointment_id) #" +
						" WHERE appointment_status IN('Booked','Confirmed') AND appointment_time::date >= ? @";
		if(resourceType.equals("DOC")) {
			query = query.replace("#", DOCTOR_QUERY);
		} else if(resourceType.equals("THID")) {
			query = query.replace("#", THEATRE_QUERY);
		} else if(resourceType.equals("EQID")) {
			query = query.replace("#", EQUIPMENT_QUERY);
		} else if(resourceType.equals("SRID")) {
			query = query.replace("#", SERVICE_RESOURCES_QUERY);
		}

		if (!resourceId.equals("*"))
			query = query.replace("@", " AND resource_id::text = ?");
		else
			query = query.replace("@", " ");
		try {
			ps = con.prepareStatement(query);
			ps.setDate(1, currentDate);
			if (!resourceId.equals("*")) {
				ps.setString(2, resourceId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_DOCTORS_CENTER_LIST = "SELECT distinct d.doctor_id ,d.doctor_name,dcm.center_id,hcm.center_name" +
		" FROM  doctors d "+
		" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
		" JOIN hospital_center_master hcm ON (hcm.center_id=dcm.center_id)"+
		" where d.doctor_id=?";

	public static List<Map> getPrescDoctorListForCenter(String doctorID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_DOCTORS_CENTER_LIST);
			pstmt.setString(1, doctorID);
			return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	private static final String GET_CHANNELING_ITEMS = "SELECT * "
			+ " FROM  scheduler_appointments sa"
			+ " JOIN bill b ON (b.bill_no = sa.bill_no)"
			+ " JOIN bill_charge bc ON (bc.bill_no = b.bill_no)"
			+ " JOIN bill_activity_charge bac ON (bac.charge_id=bc.charge_id)"
			+ " JOIN patient_packages pp ON (pp.pat_package_id = sa.pat_package_id)"
			+ " WHERE sa.appointment_id = ? ORDER BY activity_id";

	public static List<Map> getChannelingItems(Integer appointmentID)throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_CHANNELING_ITEMS);
			pstmt.setInt(1, appointmentID);
			return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	/* It gives the sequential number and it is useful to generate token numbers.
	 * */

	//TODO if the method found useful for someother tables also then we can move this function to
	//		GenericDAO by removing the hardcoded table name and placing the table varible in genericDAO.

	public Integer getToken(Connection con, String tokenGenColName,
			Map<String, Object> whereFilter, Integer incrementTo, boolean convertTodate)throws SQLException {
		PreparedStatement pstmt = null;
		StringBuilder query = new StringBuilder();
		Integer token = 1;

		try {
			query.append("SELECT MAX("+tokenGenColName+") FROM "+ "scheduler_appointments");
			if (whereFilter != null && !whereFilter.isEmpty()) {
				query.append(" WHERE ");
				Iterator it = whereFilter.entrySet().iterator();
				Map.Entry<String, Object> entry = null;
				while (it.hasNext()) {
					entry = (Map.Entry<String, Object>)it.next();
					query.append(entry.getKey());
					if (getColumnType(entry.getKey()).equals(java.sql.Timestamp.class) && convertTodate)
							query.append("::date");
					query.append(" = ?");

					if (it.hasNext()) query.append(" AND ");

				}
				pstmt = con.prepareStatement(query.toString());
				int i=1;
				it = whereFilter.entrySet().iterator();
				while (it.hasNext()) {
					entry = (Map.Entry<String, Object>)it.next();
					if (getColumnType(entry.getKey()).equals(java.sql.Timestamp.class) && convertTodate)
						pstmt.setDate(i, (java.sql.Date)entry.getValue());
					else
						pstmt.setObject(i, entry.getValue());
					i++;
				}

				try(ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					token = rs.getInt(1) + incrementTo;
				}
			}

		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}

		return token;
	}

	public Class getColumnType(String colName)throws SQLException {
		BasicDynaBean bean = schedulerAppointmentsDAO.getBean();
		return bean.getDynaClass().getDynaProperty(colName).getType();
	}

	private static final String GET_ACTIVITIES =" SELECT * "
			+ " FROM bill_charge bc"
			+ " JOIN bill_activity_charge bac ON(bac.charge_id = bc.charge_id) "
			+ " WHERE bc.bill_no = ?";

	public static List<BasicDynaBean> getActivities(Connection con, String billNum)throws SQLException {

		return DataBaseUtil.queryToDynaList(con, GET_ACTIVITIES, billNum);
	}
	private static final String DOCTORS_OVERBOOK = "SELECT overbook_limit from doctors where doctor_id=?";
	private static final String TESTS_OVERBOOK = "SELECT overbook_limit from test_equipment_master where eq_id::text=?";
	private static final String SERVICES_OVERBOOK = "SELECT overbook_limit from service_resource_master where serv_res_id::text=?";
	private static final String THEATRES_OVERBOOK = "SELECT overbook_limit from theatre_master where theatre_id=?";

	public static Integer isResourceOverbooked(Connection con, String resourceId, String category) throws SQLException {
		StringBuilder query = new StringBuilder();
		if(category.equals("DOC")) {
			query.append(DOCTORS_OVERBOOK);
		} else if (category.equals("DIA")) {
			query.append(TESTS_OVERBOOK);
		} else if(category.equals("SNP")) {
			query.append(SERVICES_OVERBOOK);
		} else if(category.equals("OPE")) {
			query.append(THEATRES_OVERBOOK);
		}
		try(PreparedStatement pstmt = con.prepareStatement(query.toString())){
		pstmt.setString(1, resourceId);
		
		
		BasicDynaBean bean = DataBaseUtil.queryToDynaBean(pstmt);
		if (bean != null)
			return (Integer) bean.get("overbook_limit");
		}
		return 0;
	}
	
	//Get doctor belonging center name and id 
	private static final String GET_RESOURCE_CENTER_LIST = "SELECT d.doctor_id ,dcm.center_id,hcm.center_name" +
			" FROM  doctors d "+
			" JOIN doctor_center_master dcm ON(d.doctor_id=dcm.doctor_id)"+
			" JOIN hospital_center_master hcm ON (hcm.center_id=dcm.center_id)"+
			" where d.doctor_id=? and dcm.status='A' ORDER BY center_name ";
			
	public static List<BasicDynaBean> getResourceBelongingCenter(String doctorID) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		List<BasicDynaBean> resCenters = null;
		try {
			ps = con.prepareStatement(GET_RESOURCE_CENTER_LIST);
			ps.setString(1, doctorID);
			resCenters = DataBaseUtil.queryToDynaList(ps);
			if (resCenters != null && resCenters.size() == 1) {
				Integer resCenterId = (Integer)resCenters.get(0).get("center_id");
				if (resCenterId.equals(0))
					resCenters = CenterMasterDAO.getAllCentersAndSuperCenterAsFirst();
			}
					
		} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return resCenters;
	}	
	
	private static final String GET_OVERBOOK_COUNT = "select count(*) from scheduler_appointments where prim_res_id=? and appointment_time=? and appointment_status NOT IN ('Noshow','Cancel')";
	
	public static int getOverbookCount(String scheduleName, Timestamp apptTime) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		int overbookCount ;
		try {
			ps = con.prepareStatement(GET_OVERBOOK_COUNT);
			ps.setString(1, scheduleName);
			ps.setTimestamp(2, apptTime);
			overbookCount = DataBaseUtil.getIntValueFromDb(ps);
			
					
		} finally {
				DataBaseUtil.closeConnections(con, ps);
		}
		return overbookCount;
	}	
	private static final String GetAppointmentSourceName = " select casm.appointment_source_name "
			+ "from appointment_source_master casm "
			+ "join scheduler_appointments sa on(casm.appointment_source_id=sa.app_source_id) "
			+ "where appointment_id=? ";
			
	public static String getAppointmentSource(Integer appointmentId)
			throws SQLException {
		PreparedStatement ps = null;
		Connection  con = DataBaseUtil.getConnection();
		try{
		ps = con.prepareStatement(GetAppointmentSourceName);
		ps.setInt(1, appointmentId);
		return DataBaseUtil.getStringValueFromDb(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String APPOINTMENTS_FOR_PATIENT =
			
			"SELECT sp.visit_id, sp.mr_no, sp.appointment_id, sp.res_sch_id, sp.prim_res_id as res_sch_name , sp.center_id, sp.remarks, sp.patient_name, "
			+ " to_char(sp.appointment_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as appointment_time,"
			+ " to_char(sp.booked_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as booked_time,"
			+ " to_char(sp.changed_time AT TIME ZONE (SELECT  current_setting('TIMEZONE')) AT TIME ZONE 'UTC', 'YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"') as changed_time,"
			+ " sp.duration, sp.appointment_status, doc.doctor_name, doc.dept_id, d.dept_name as department_name, hcm.center_name, sp.complaint, "
			+ " sp.booked_by, sp.changed_by, sp.rescheduled, sp.cancel_reason, sp.scheduler_visit_type, sp.patient_contact, sp.presc_doc_id, p.package_name, sp.appointment_pack_group_id"	
			+ " FROM scheduler_appointments sp  "
			+ " LEFT JOIN doctors doc ON (sp.prim_res_id= doc.doctor_id) " 
			+ " LEFT JOIN packages p ON (p.package_id = sp.package_id) "
			+ " LEFT JOIN department d ON (doc.dept_id = d.dept_id) "
			+ " JOIN hospital_center_master hcm ON (hcm.center_id = sp.center_id) " 
			+ " WHERE $$FILTER_FIELD$$ BETWEEN ? AND ? @ # ^ ORDER BY sp.appointment_time ";
			
	public static List getAppointmentsQueryResult(Connection con,String section, Map sessionParameters, Object fromTime, 
			Object toTime, String patientPhone,String phoneNumberNationalPart, String mrno, String centerId, 
			Boolean useAppointmentDate, Boolean searchByPatient) throws SQLException {
		PreparedStatement pstmt = null;
		String phoneClause = "AND sp.patient_contact IN ( ? , ?) ";
		String mrnoClause = "AND sp.mr_no = ?";
		String centerClause= "AND sp.center_id = ?";
		String phoneWithoutCountryClause="AND REPLACE(sp.patient_contact,sp.patient_contact_country_code,'') IN ( ?) ";
		String APPOINTMENTS =APPOINTMENTS_FOR_PATIENT;
		APPOINTMENTS = APPOINTMENTS.replace("$$FILTER_FIELD$$", useAppointmentDate ? "sp.appointment_time" : "sp.booked_time");
		try {
			if (sessionParameters.get("user_doctor_id") != null 
				  && !((String)sessionParameters.get("user_doctor_id")).isEmpty()) {
				APPOINTMENTS = APPOINTMENTS.replace("^", "AND sp.prim_res_id = ?");
			} else {
				APPOINTMENTS = APPOINTMENTS.replace("^", "");
			}
			if (!searchByPatient) {
				APPOINTMENTS = APPOINTMENTS.replace("#", "");
			} else if (patientPhone == null || patientPhone.equals("")) {
				APPOINTMENTS = APPOINTMENTS.replace("#", mrnoClause);
			} else if (phoneNumberNationalPart == null) {
				APPOINTMENTS = APPOINTMENTS.replace("#", phoneWithoutCountryClause);
			} else {
				APPOINTMENTS = APPOINTMENTS.replace("#", phoneClause);
			}

			if (centerId != null && !centerId.equals(""))
				APPOINTMENTS = APPOINTMENTS.replace("@", centerClause);
			else
				APPOINTMENTS = APPOINTMENTS.replace("@", "");

			int index = 1;
			pstmt = con.prepareStatement(APPOINTMENTS);
			pstmt.setObject(index++, fromTime);
			pstmt.setObject(index++, toTime);
			if (centerId != null && !centerId.equals("")) {
				int centerIdInt = Integer.parseInt(centerId);
				pstmt.setInt(index++, centerIdInt);
			}
			if (searchByPatient) {
				if (patientPhone == null || patientPhone.equals("")) {
					pstmt.setString(index++, mrno);
				} else if (phoneNumberNationalPart == null) {
					pstmt.setString(index++, patientPhone);
				} else {
					pstmt.setString(index++, patientPhone);
					pstmt.setString(index++, phoneNumberNationalPart);
				}				
			}
			if (sessionParameters.get("user_doctor_id") != null 
				  && !((String)sessionParameters.get("user_doctor_id")).isEmpty()) {
				pstmt.setObject(index++, sessionParameters.get("user_doctor_id"));
			}

			return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
		} finally {
			if(pstmt!=null){
				pstmt.close();
			}
		}
	}
		
	private static final String GET_APPOINTMENT_WITH_SECONDARY_RESOURCE = "select sp.appointment_id, sp.center_id, sp.appointment_time,sp.duration,sp.appointment_status, sci.resource_id  "
			+ "from scheduler_appointments sp JOIN scheduler_appointment_items sci ON(sci.appointment_id=sp.appointment_id) "
			+ " where resource_id = ? AND date(appointment_time) = ? AND appointment_status not in ('Cancel','Noshow' ) ";

	public static List<BasicDynaBean> getAppointmentWithSecondaryResource(
			String resourceId, Date appointmentDate) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_APPOINTMENT_WITH_SECONDARY_RESOURCE;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, resourceId);
			ps.setDate(2, new java.sql.Date(appointmentDate.getTime()));
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String GET_APPOINTMENT_COUNT = "select count(*)  "
      + "from scheduler_appointments sp JOIN scheduler_appointment_items sci ON(sci.appointment_id=sp.appointment_id) "
      + " where date(appointment_time) = ? AND appointment_status not in ('Cancel','Noshow' ) # ";
	
	public static boolean isAppointmentLimitReached(Timestamp appointmentTime, String mrNo, String phoneNo, int apptLimit) throws SQLException{
	  //limit as -1 means there is no limit
	  if(apptLimit==-1){
	    return false;
	  }
	  Date appointmentDate=DateUtil.getDatePart(appointmentTime);
	  String phoneClause = "AND sp.patient_contact = ? ";
    String mrnoClause = "AND sp.mr_no = ?";
    String appointmentCountQuery =GET_APPOINTMENT_COUNT;
    Object[] params;
      if(mrNo==null || mrNo.equals("")){
        appointmentCountQuery =appointmentCountQuery.replace("#", phoneClause);
        params=new Object[]{appointmentDate,phoneNo};
      }else{
        appointmentCountQuery =appointmentCountQuery.replace("#", mrnoClause);
        params=new Object[]{appointmentDate,mrNo};
      }
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(appointmentCountQuery, params);
      
      return (bean!=null && bean.get("count")!=null && ((Long) bean.get("count")>=apptLimit));
	}
	
	private static final String GET_SERVICE_DEFAULT_DURATION = "select default_duration from scheduler_master where res_sch_type = 'SER' and res_sch_name = '*'";
	public static int getDefaultDurationService() throws SQLException{
	  Connection con = null;
    PreparedStatement ps = null;
    String query = GET_SERVICE_DEFAULT_DURATION;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      BasicDynaBean bean =  DataBaseUtil.queryToDynaBean(ps);
      return (int) bean.get("default_duration");
    }finally {
      DataBaseUtil.closeConnections(con, ps);
    }
	}
	
  private static final String GET_SURGERY_DEFAULT_DURATION = "select default_duration from scheduler_master sm where res_sch_type = 'SUR' AND sm.res_sch_name = '*'";

  public static int getDefaultDurationSurgery() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String query = GET_SURGERY_DEFAULT_DURATION;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      return (int) bean.get("default_duration");
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
	
  private static final String GET_SERVICE_DURATION = "select service_duration from services where service_id = ?";
  private static final String GET_TEST_DURATION = "select test_duration from diagnostics where test_id =? ";
  private static final String GET_OPERATION_DURATION = "select operation_duration from operation_master where op_id =? ";

  public static int getSecondaryDefaultDuration(String resourceType, String ResourceId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    String query = null;
    String resource = null;
    if (resourceType.toString().equals("SER")) {
      query = GET_SERVICE_DURATION;
       resource = "service_duration";
    } else if (resourceType.toString().equals("TST")) {
      query = GET_TEST_DURATION;
       resource = "test_duration";
    } else if (resourceType.toString().equals("SUR")) {
      query = GET_OPERATION_DURATION;
      resource = "operation_duration";
    }
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      ps.setString(1, ResourceId);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
      return (int) bean.get(resource);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

  }
	
  private static final String FLUSH_CONTACT_FROM_SCHEDULER = 
      "Update scheduler_appointments set mr_no = ?, contact_id = null where contact_id = ? ";

  /** The Constant DELETE_CONTACT_RECORD. */
  private static final String DELETE_CONTACT_RECORD = 
      "delete from contact_details where contact_id = ?";
  

  private static final String GET_CONTACT_ID_FOR_APPOINTMENT = 
      "Select contact_id from scheduler_appointments where appointment_id = ?";

  /**
   * Flush contact.
   *
   * @param mrNo the mr no
   * @param appointmentId the appointment id
   * @throws SQLException 
   */
  public static void flushContact(String mrNo, Integer appointmentId) throws SQLException {
    
    Connection con = null;
    PreparedStatement ps = null;
    String query = GET_CONTACT_ID_FOR_APPOINTMENT;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      BasicDynaBean bean =  DataBaseUtil.queryToDynaBean(query, new Object[]{appointmentId});
      int contactId = (int) bean.get("contact_id");
      query = FLUSH_CONTACT_FROM_SCHEDULER;
      DataBaseUtil.executeQuery(con, query, mrNo, contactId);
      query = DELETE_CONTACT_RECORD;
      DataBaseUtil.executeQuery(con, query, contactId);
      
    }finally{
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant UPDATE_WAITLIST. */
  private static final String UPDATE_WAITLIST = "UPDATE scheduler_appointments "
      + "SET waitlist =? where appointment_id = ?";

  /**
   * Batch upgrade waitlist.
   *
   * @param updateParamsList the update params list
   */
  public static void batchUpgradeWaitlist(List<Object[]> updateParamsList) {
    DatabaseHelper.batchUpdate(UPDATE_WAITLIST, updateParamsList);
  }

}

