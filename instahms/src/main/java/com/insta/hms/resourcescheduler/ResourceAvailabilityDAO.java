package com.insta.hms.resourcescheduler;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ResourceAvailabilityDAO extends GenericDAO{
	static Logger logger = LoggerFactory.getLogger(ResourceAvailabilityDAO.class);
	public ResourceAvailabilityDAO() {
		super("sch_resource_availability");
	}

	private static String RES_AVAIL_FIELDS = " SELECT *  ";

	private static String RES_AVAIL_COUNT = " SELECT count(*) ";

	private static String RES_AVAIL_TABLES = "FROM sch_resource_availability sra ";
	
    private static final GenericDAO schResourceAvailabilityDetailsDAO =
        new GenericDAO("sch_resource_availability_details");

	public PagedList getResourceAvailabilityList(Map map, Map pagingParams)
		throws Exception, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();

			SearchQueryBuilder qb = new SearchQueryBuilder(con, RES_AVAIL_FIELDS,
					RES_AVAIL_COUNT, RES_AVAIL_TABLES, pagingParams);
			map.remove("login_center_id");
			map.remove("login_center_name");
			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("availability_date",true);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public String GET_RES_DETAILS = " SELECT sra.*,# FROM sch_resource_availability sra ";
	private static String DYNAMIC_QUERY_DOCTORS = "d.doctor_name AS resource_name ";
	private static String DYNAMIC_QUERY_TEST_EQUIPMENT = "te.equipment_name AS resource_name ";
	private static String DYNAMIC_QUERY_SERVICE_RESOURCES = "srm.serv_resource_name AS resource_name ";
	private static String DYNAMIC_QUERY_OT = "tm.theatre_name AS resource_name ";
	private static String DYNAMIC_QUERY_TEST = "d.test_name AS resource_name ";
	private static String DYNAMIC_QUERY_SERVICE = "sv.service_name ||'('|| sd.department ||')' AS resource_name ";
	private static String DYNAMIC_QUERY_SURGERY = "op.operation_name AS resource_name ";
	private static String DYNAMIC_QUERY_GENERIC_RESOURCE = "grm.generic_resource_name AS resource_name ";
	private static String GET_DOCTOR_DETAILS = "JOIN doctors d ON(d.doctor_id = sra.res_sch_name) ";
	private static String GET_EQUIPMENT_DETAILS = " JOIN test_equipment_master te ON(te.eq_id::text = sra.res_sch_name) ";
	private static String GET_OT_DETAILS = " JOIN theatre_master tm ON(tm.theatre_id = sra.res_sch_name)";
	private static String GET_SERVICE_RESOURCE_DETAILS = " JOIN service_resource_master srm ON(srm.serv_res_id::text = sra.res_sch_name)";
	private static String GET_TEST_DETAILS = " JOIN diagnostics d on(res_sch_name = test_id)";
	private static String GET_SERVICE_DETAILS = " JOIN services  sv ON(sv.service_id= sra.res_sch_name) " +
			"	JOIN services_departments sd ON(sd.serv_dept_id = sv.serv_dept_id)";
	private static String GET_OPERATION_DETAILS = " JOIN operation_master op ON(op.op_id = sra.res_sch_name)";
	private static String GET_GENERIC_RESOURCE_DETAILS = " JOIN generic_resource_master grm ON(grm.generic_resource_id::text = sra.res_sch_name)";

	public BasicDynaBean getResourceDetails(int resAvailId,String resourceType) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_RES_DETAILS;
		if (resourceType.equals("DOC")) {
			query = query.replace("#", DYNAMIC_QUERY_DOCTORS) + GET_DOCTOR_DETAILS;
		} else if (resourceType.equals("EQID")) {
			query = query.replace("#", DYNAMIC_QUERY_TEST_EQUIPMENT) + GET_EQUIPMENT_DETAILS;
		} else if (resourceType.equals("SRID")){
			query = query.replace("#", DYNAMIC_QUERY_SERVICE_RESOURCES) + GET_SERVICE_RESOURCE_DETAILS;
		} else if (resourceType.equals("THID")){
			query = query.replace("#", DYNAMIC_QUERY_OT) + GET_OT_DETAILS;
		} else if (resourceType.equals("SER")) {
			query = query.replace("#", DYNAMIC_QUERY_SERVICE) + GET_SERVICE_DETAILS;
		} else if (resourceType.equals("SUR")) {
			query = query.replace("#", DYNAMIC_QUERY_SURGERY) + GET_OPERATION_DETAILS;
		} else if (resourceType.equals("TST")) {
			query = query.replace("#", DYNAMIC_QUERY_TEST) + GET_TEST_DETAILS;
		} else {
			query = query.replace("#", DYNAMIC_QUERY_GENERIC_RESOURCE) + GET_GENERIC_RESOURCE_DETAILS;
		}
		query = query + " WHERE res_sch_type= ? AND res_avail_id = ?" ;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, resourceType);
			ps.setInt(2, resAvailId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public String GET_RESOURCES = " SELECT * FROM sch_resource_availability sra where  res_sch_type = ? AND res_sch_name = ?  order by availability_date ";
	public List<BasicDynaBean> getResourcesList(String resourceType,String resourceName) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESOURCES);
			ps.setString(1, resourceType);
			ps.setString(2, resourceName);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static final String GET_DEFAULT_RESOURCE_DETAILS = " SELECT * FROM scheduler_master sm where  res_sch_type = ? AND res_sch_name='*'";
	public BasicDynaBean getDefaultCategory(String resourceType) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DEFAULT_RESOURCE_DETAILS);
			ps.setString(1, resourceType);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE= " SELECT * FROM sch_resource_availability sm where  res_sch_type = ? AND res_sch_name= ? AND availability_date = ?";
	public BasicDynaBean getResourceByAvailDate(String resourceType,String resourceName,java.sql.Date date) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESOURCE_DETAILS_BY_AVAILABILTY_DATE);
			ps.setString(1, resourceType);
			ps.setString(2, resourceName);
			ps.setDate(3, date);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public  StringBuilder DELETE_RESOURCE_TIMINGS = new StringBuilder("delete from sch_resource_availability_details " +
	   "where res_avail_id = ?");

	public  boolean deleteResourceTimings(Connection con,int resAvailId,List resAvailDetId) throws Exception{
		PreparedStatement ps = null;
		try {
			DataBaseUtil.addNotInWhereFieldInList(DELETE_RESOURCE_TIMINGS, "res_avail_details_id", resAvailDetId,true);
			ps = con.prepareStatement(DELETE_RESOURCE_TIMINGS.toString());
			ps.setInt(1, resAvailId);
			int index = 1;
			for(int i=0;i<resAvailDetId.size();i++) {
				ps.setInt(++index, (Integer)resAvailDetId.get(i));
			}
			return ps.executeUpdate() >= 0;

		} finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public static String GET_RESOURCE_DEATILS = "SELECT * FROM scheduler_master where res_sch_type = ? and res_sch_name = ? AND status = 'A'";

	public static BasicDynaBean getResourceDetils(String res_sch_type,String res_sch_name) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESOURCE_DEATILS);
			ps.setString(1, res_sch_type);
			ps.setString(2, res_sch_name);
			return DataBaseUtil.queryToDynaBean(ps);

		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public  BasicDynaBean getResourceDetilsByDate(String res_sch_type,String res_sch_name) throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		String query = GET_RES_DETAILS;
		if (res_sch_type.equals("DOC")) {
			query = query.replace("#", DYNAMIC_QUERY_DOCTORS) + GET_DOCTOR_DETAILS;
		} else if (res_sch_type.equals("EQID")) {
			query = query.replace("#", DYNAMIC_QUERY_TEST_EQUIPMENT) + GET_EQUIPMENT_DETAILS;
		} else if (res_sch_type.equals("SRID")){
			query = query.replace("#", DYNAMIC_QUERY_SERVICE_RESOURCES) + GET_SERVICE_RESOURCE_DETAILS;
		} else if (res_sch_type.equals("THID")){
			query = query.replace("#", DYNAMIC_QUERY_OT) + GET_OT_DETAILS;
		} else if (res_sch_type.equals("SER")) {
			query = query.replace("#", DYNAMIC_QUERY_SERVICE) + GET_SERVICE_DETAILS;
		} else if (res_sch_type.equals("SUR")) {
			query = query.replace("#", DYNAMIC_QUERY_SURGERY) + GET_OPERATION_DETAILS;
		} else if (res_sch_type.equals("TST")) {
			query = query.replace("#", DYNAMIC_QUERY_TEST) + GET_TEST_DETAILS;
		} else {
			query = query.replace("#", " ") ;
		}
		query = query + " WHERE res_sch_type= ? AND res_sch_name = ? ORDER BY availability_date desc limit 1 " ;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, res_sch_type);
			ps.setString(2, res_sch_name);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	public static String SELECT_RECORDS_FOR_SCH_RESOURCE_AVAILABILITY = "SELECT * FROM sch_resource_availability where availability_date not between ? and ? AND res_sch_type = ? AND res_sch_name = ?";

	public static String DELETE_RECORDS_FOR_SCH_RESOURCE_AVAILABILITY = "DELETE FROM sch_resource_availability where availability_date not between ? and ? AND res_sch_type = ? AND res_sch_name = ?";

	public static boolean deleteExistingRecords(Connection con,Date fromDate,Date toDate, String resourceType,String res_sch_name) throws Exception{
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		int rowsDeleted = 0;
		List<BasicDynaBean> records = new ArrayList<BasicDynaBean>();
		ps = con.prepareStatement(SELECT_RECORDS_FOR_SCH_RESOURCE_AVAILABILITY);
		ps.setDate(1, new java.sql.Date(fromDate.getTime()));
		ps.setDate(2, new java.sql.Date(toDate.getTime()));
		ps.setString(3, resourceType);
		ps.setString(4, res_sch_name);
		records = DataBaseUtil.queryToDynaList(ps);
		if (records != null && records.size() > 0) {
			for (int i=0; i<records.size();i++) {
				int res_avail_id = (Integer)records.get(i).get("res_avail_id");
				schResourceAvailabilityDetailsDAO.delete(con, "res_avail_id", res_avail_id);
			}
			ps1 = con.prepareStatement(DELETE_RECORDS_FOR_SCH_RESOURCE_AVAILABILITY);
			ps1.setDate(1, new java.sql.Date(fromDate.getTime()));
			ps1.setDate(2, new java.sql.Date(toDate.getTime()));
			ps1.setString(3, resourceType);
			ps1.setString(4, res_sch_name);
			rowsDeleted = ps1.executeUpdate();

			if (ps != null)
				ps.close();
			if (ps1 != null)
				ps1.close();
		}
		return rowsDeleted >= 0;
	}

	public static final String GET_RESOURCES_TIMINGS_BY_AVAILABILTY_DATE= " SELECT * FROM sch_resource_availability sm where  res_sch_type = ? AND res_sch_name= ? ";
	public List<BasicDynaBean> getResourceByAvailDate(String resourceType,String resourceName) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_RESOURCES_TIMINGS_BY_AVAILABILTY_DATE);
			ps.setString(1, resourceType);
			ps.setString(2, resourceName);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
}
