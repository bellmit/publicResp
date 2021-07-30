/**
 *
 */
package com.insta.hms.resourcescheduler;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class CategoryMasterDAO  extends GenericDAO{
	Connection con = null;


	public CategoryMasterDAO(){
		super("scheduler_master");
	}

	private static String Category_FIELDS = "SELECT * " ;

	private static String Category_COUNT = "SELECT count(*)";

	private static String Category_TABLES = " FROM (SELECT sm.res_sch_id,sm.res_sch_category,sm.res_sch_type,sm.dept,sm.res_sch_name,sm.description,sm.default_duration," +
			"coalesce((case when sm.res_sch_category='DOC' then d.doctor_name " +
			" when sm.res_sch_category='OPE' THEN "+
			"  (CASE WHEN res_sch_type = 'SUR'  THEN ope.operation_name " +
			"		WHEN res_sch_type = 'THID' THEN tm.theatre_name"+
			"    ELSE (CASE WHEN sm.res_sch_name != '*' THEN grm.generic_resource_name end) end)" +
			" WHEN sm.res_sch_category='DIA' THEN "+
			"  (CASE WHEN res_sch_type='TST' THEN dia.test_name "+
			"	WHEN res_sch_type = 'EQID' THEN tem.equipment_name "+
			"    ELSE (CASE WHEN sm.res_sch_name != '*' THEN grm.generic_resource_name end) end)"+
			" WHEN sm.res_sch_category='SNP' THEN "+
			"  (CASE WHEN res_sch_type = 'SER' THEN serv.service_name "+
			"     WHEN res_sch_type ='SRID' THEN srm.serv_resource_name " +
			"		ELSE (CASE WHEN sm.res_sch_name != '*' THEN grm.generic_resource_name end) end) end),'Any') as resource_name," +
			" CASE when sm.res_sch_type = 'DOC' AND res_sch_name != '*' THEN (SELECT dept_name from department where dept_id = sm.dept) "+
			" 	when sm.res_sch_type = 'SER' AND res_sch_name != '*' THEN (SELECT department from services_departments where serv_dept_id::text = sm.dept) "+
			" 	when sm.res_sch_type = 'TST' AND res_sch_name != '*' THEN (SELECT ddept_name from diagnostics_departments where ddept_id = sm.dept) " +
			"   when sm.res_sch_type = 'SUR' AND res_sch_name != '*' THEN (SELECT dept_name from department where dept_id = sm.dept)" +
			"   else 'Any' END AS dept_name, sm.status, " +
			" CASE when sm.res_sch_type = 'DOC' AND res_sch_name != '*' THEN (SELECT status from doctors where doctor_id = sm.res_sch_name) "+
			" 	when sm.res_sch_type = 'SER' AND res_sch_name != '*' THEN (SELECT status from services where service_id = sm.res_sch_name) "+
			" 	when sm.res_sch_type = 'THID' AND res_sch_name != '*' THEN (SELECT status from theatre_master where theatre_id = sm.res_sch_name) "+
			" 	when sm.res_sch_type = 'SRID' AND res_sch_name != '*' THEN (SELECT status from service_resource_master where serv_res_id::text = sm.res_sch_name) "+
			" 	when sm.res_sch_type = 'EQID' AND res_sch_name != '*' THEN (SELECT status from test_equipment_master where eq_id::text = sm.res_sch_name) "+
			" 	when sm.res_sch_type = 'SUR' AND res_sch_name != '*' THEN (SELECT status from operation_master where op_id = sm.res_sch_name) "+
			" 	when sm.res_sch_type = 'TST' AND res_sch_name != '*' THEN (SELECT status from diagnostics where test_id = sm.res_sch_name) " +
			"   else " +
			"		(case when sm.res_sch_name != '*' then grm.status else 'A' end) END AS resource_status," +
			" case when sm.res_sch_type NOT IN('SER','THID','SRID','EQID','SUR','TST','DOC') " +
			" THEN grt.status ELSE 'A' end AS generic_resource_type_status"+
			" FROM scheduler_master sm left join doctors d on (d.doctor_id = sm.res_sch_name)" +
			" left join theatre_master tm on(tm.theatre_id = sm.res_sch_name) " +
			" left join department dept on(dept.dept_id = sm.dept) " +
			" left join services_departments sd ON(sd.serv_dept_id::text = sm.dept AND sm.res_sch_type = 'SER')"+
			" left join diagnostics_departments dd ON(dd.ddept_id = sm.dept)"+
			" left join service_resource_master srm ON(srm.serv_res_id::text = sm.res_sch_name)" +
			" left join test_equipment_master tem on(tem.eq_id::text = sm.res_sch_name)" +
			" left join operation_master ope ON(ope.op_id=sm.res_sch_name) "+
			" left join diagnostics dia ON(dia.test_id=sm.res_sch_name) " +
			" left join generic_resource_type grt ON(sm.res_sch_type = grt.scheduler_resource_type)" +
			" left join generic_resource_master grm ON(grm.generic_resource_id::text = sm.res_sch_name)"+
			" left join services serv ON(serv.service_id=sm.res_sch_name)) AS foo";

	public PagedList getCategoryDetailPages(Map map, Map<LISTING, Object> pagingParams)throws SQLException, ParseException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		List l = new ArrayList();
		l.add("*");
		List statusList = new ArrayList();
		statusList.add("A");
		SearchQueryBuilder qb =
			new SearchQueryBuilder(con,Category_FIELDS, Category_COUNT, Category_TABLES, pagingParams);
		qb.addFilter(qb.STRING, "foo.resource_status", "IN", statusList);
		qb.addFilter(qb.STRING, "foo.generic_resource_type_status", "IN", statusList);
		qb.addFilterFromParamMap(map);
		qb.addFilter(qb.STRING, "res_sch_type", "NOT IN", l);
		qb.addSecondarySort("res_sch_type");
		qb.build();

		PagedList pagedList = qb.getMappedPagedList();
		con.close();

		return pagedList;
	}

	public static final String GET_CATEGORY_DETAILS = " SELECT res_sch_id,res_sch_type,res_sch_category,dept,res_sch_name,description,status," +
			" default_duration," +
			" height_in_px FROM scheduler_master WHERE res_sch_id=? " ;

	public static BasicDynaBean getCategoryDetails(int res_sch_id) {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CATEGORY_DETAILS);
			ps.setInt(1,res_sch_id);
			List categorydetails =  DataBaseUtil.queryToDynaList(ps);
			return (BasicDynaBean)categorydetails.get(0);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}

	public static final String GET_ALL_ResourceS_ADD = " select srt.resource_type,'' as resource_id,srt.primary_resource" +
			" from  scheduler_resource_types srt   WHERE srt.category = ? AND srt.primary_resource='false'" ;

	public static final String GET_ALL_ResourceS_EDIT = " select sim.resource_type,sim.resource_id,srt.primary_resource  from " +
			" scheduler_item_master sim " +
			" left join  scheduler_master  sm on sm.res_sch_id = sim.res_sch_id " +
			" left join scheduler_resource_types srt on (srt.category = sm.res_sch_category and sim.resource_type = srt.resource_type)" +
			" WHERE sim.res_sch_id=? AND srt.primary_resource='false'" ;

	public static List getResourceDetails(String category,int res_sch_id) {
		PreparedStatement ps = null;
		List<BasicDynaBean> CategoryNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			if(res_sch_id == 0) {
				ps = con.prepareStatement(GET_ALL_ResourceS_ADD);
				ps.setString(1,category);
			}else {
				ps = con.prepareStatement(GET_ALL_ResourceS_EDIT);
				ps.setInt(1,res_sch_id);
			}
			CategoryNames = DataBaseUtil.queryToDynaList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return CategoryNames;
	}


	/**
	*  INSERT,UPDATE and DELETE of Resources
	*/

	public static final String INSERT_RESOURCE = "INSERT INTO scheduler_item_master(res_sch_id,resource_type,resource_id) values (?,?,?)";

	/*
	* Insert a list of Resources
	*/
	public static boolean insertResources(Connection con,List list) throws SQLException {
	PreparedStatement ps = con.prepareStatement(INSERT_RESOURCE);
	boolean success = true;
	Iterator iterator = list.iterator();
	while (iterator.hasNext()) {
		ResourceDTO rdto =  (ResourceDTO) iterator.next();
		ps.setInt(1, rdto.getRes_sch_id());
		ps.setString(2, rdto.getResourceType());
		ps.setString(3, rdto.getResourceId());
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
	return success;
	}


	public static final String UPDATE_RESOURCE = "UPDATE scheduler_item_master SET resource_id=? WHERE res_sch_id=? and resource_type=? and resource_id=?";

	/*
	* Update a list of Resources
	*/
	public static boolean updateResources(Connection con, List list) throws SQLException {
		PreparedStatement ps = con.prepareStatement(UPDATE_RESOURCE);
		boolean success = true;
		Iterator iterator = list.iterator();
		while (iterator.hasNext()) {
			ResourceDTO rdto =  (ResourceDTO) iterator.next();
			ps.setString(1, rdto.getResourceId());
			ps.setInt(2, rdto.getRes_sch_id());
			ps.setString(3, rdto.getResourceType());
			ps.setString(4, rdto.getItem_id());
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
		return success;
	}

	private static final String DELETE_RESOURCE = "DELETE FROM scheduler_item_master WHERE res_sch_id=? and resource_id=? and resource_type=?";

	/*
	* Delete a list of Resources
	*/
	public static boolean deleteResources(Connection con, List list) throws SQLException {
	PreparedStatement ps = con.prepareStatement(DELETE_RESOURCE);
	boolean success = true;

	Iterator iterator = list.iterator();
	while (iterator.hasNext()) {
		ResourceDTO rdto = (ResourceDTO) iterator.next();
		ps.setInt(1, rdto.getRes_sch_id());
		ps.setString(2,rdto.getResourceId());
		ps.setString(3, rdto.getResourceType());
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
		return success;
	}

	public static final String GET_CATEGORY_DETAILS_BY_RESOURCE_TYPE = "SELECT * FROM scheduler_master WHERE res_sch_type = ? AND res_sch_name = ? AND status = 'A'" ;

	public static BasicDynaBean getCategoryDetailsByResourceType(String res_sch_name,String resourceType) throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CATEGORY_DETAILS_BY_RESOURCE_TYPE);
			ps.setString(1,resourceType);
			ps.setString(2,res_sch_name);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_CATEGORY_DETAILS_BY_RESOURCE_ID= "SELECT * FROM scheduler_master WHERE res_sch_type = ? AND res_sch_name = ? AND res_sch_id != ? AND status = 'A'" ;

	public static BasicDynaBean getCategoryDetailsByResourceId(String res_sch_name,String resourceType,int resourceId) throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CATEGORY_DETAILS_BY_RESOURCE_ID);
			ps.setString(1,resourceType);
			ps.setString(2,res_sch_name);
			ps.setInt(3, resourceId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_CATEGORY_DESCRIPTION= "SELECT res_sch_type,resource_description from scheduler_resource_types srt"+
			" 	JOIN scheduler_master sm ON(sm.res_sch_type = srt.resource_type) AND (srt.category = sm.res_sch_category) " +
			" 	AND res_sch_name = '*'" +
			" 	WHERE srt.resource_group is null " +
			" UNION " +
			" 	SELECT scheduler_resource_type AS res_sch_type,resource_type_desc AS resource_description " +
			" 	FROM generic_resource_type grt " +
			"	JOIN scheduler_resource_types srt ON(srt.resource_type = grt.scheduler_resource_type) AND resource_group = 'GEN' ";

	public static List<BasicDynaBean> getCategoryDescription(boolean primaryCategory) throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_CATEGORY_DESCRIPTION);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
		public static final String GET_RESOURCE_AVAILABLETIMING_LIST =" SELECT srad.*, hcm.center_name "
				+ " from  sch_resource_availability_details srad "
				+ " LEFT JOIN hospital_center_master hcm ON (srad.center_id = hcm.center_id) " 
				+ " WHERE res_avail_id = ? ORDER BY from_time ";
		
		public static List<BasicDynaBean> getResourceAvailtimingList(int res_avail_id) throws Exception{
			PreparedStatement ps = null;
			Connection con = null;
			try{
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(GET_RESOURCE_AVAILABLETIMING_LIST);
				ps.setInt(1,res_avail_id);
				return DataBaseUtil.queryToDynaList(ps);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
}
