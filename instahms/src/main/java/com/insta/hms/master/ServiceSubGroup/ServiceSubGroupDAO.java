package com.insta.hms.master.ServiceSubGroup;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AbstractCachingDAO;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import net.sf.ehcache.Cache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceSubGroupDAO extends AbstractCachingDAO {

	public ServiceSubGroupDAO() {
		super("service_sub_groups");
	}

	protected Cache newCache(String region) {
		return new Cache(region, 100, MemoryStoreEvictionPolicy.LRU, false, "/tmp", true, 0, 0, false, 0,
				null);
	}

	private static final String SERVICE_SUBGROUP_FIELDS = " SELECT ssg.*,sg.service_group_name ";

	private static final String SERVICE_SUBGROUP_QUERY_COUNT =
		" SELECT count(*) ";

	private static final String SERVICE_SUBGROUP_TABLES =
		" FROM service_sub_groups  ssg " +
		" LEFT JOIN service_groups sg ON (ssg.service_group_id=sg.service_group_id) " ;

	public static PagedList searchList(Map filter, Map listing) throws Exception {

		Connection con = DataBaseUtil.getReadOnlyConnection();

		SearchQueryBuilder qb = new SearchQueryBuilder(con,SERVICE_SUBGROUP_FIELDS, SERVICE_SUBGROUP_QUERY_COUNT,
				SERVICE_SUBGROUP_TABLES, listing);

		qb.addFilterFromParamMap(filter);
		qb.build();

		PagedList l = qb.getMappedPagedList();

		qb.close();
		con.close();

		return l;
	}

	public static final String GET_ALL_SERVICE_SUB_GROUPS = " SELECT ssg.*,to_char(ssg.mod_time,'dd-mm-yyyy hh:mm'), " +
			" sg.service_group_name " +
			" FROM service_sub_groups  ssg " +
			" LEFT JOIN service_groups sg ON (ssg.service_group_id=sg.service_group_id) " ;

	public static List getAllServiceSubGroups() throws SQLException {
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_SERVICE_SUB_GROUPS);
			l = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return l;
	}

	public static final String sub_group_details =
		" SELECT *,service_group_name FROM service_sub_groups " +
		" join service_groups using(service_group_id) " +
		" where lower(service_group_name) =? and lower(service_sub_group_name) = ?";

	public BasicDynaBean getServiceSubGroupBean(String subGroupName,String groupName)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(sub_group_details);
			ps.setString(1, groupName.toLowerCase());
			ps.setString(2, subGroupName.toLowerCase());
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_ALL_ACTIVE_SERVICE_SUB_GROUPS = " SELECT service_sub_group_id," +
			" service_group_id,service_sub_group_name,service_sub_group_code from service_sub_groups where status='A' "+
			"ORDER BY service_sub_group_name ";

	public static List getAllActiveServiceSubGroups() throws SQLException {
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_ACTIVE_SERVICE_SUB_GROUPS);
			l = DataBaseUtil.queryToArrayList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return l;
	}
	public static final String GET_SERVICE_SUB_GROUP = " SELECT service_sub_group_id," +
	" service_group_id,service_sub_group_name from service_sub_groups " ;

	public BasicDynaBean getServiceSubGroup(String serviceSubGroupName,int serviceGroupId)throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_SERVICE_SUB_GROUP+" WHERE service_sub_group_name= ? AND service_group_id = ?");
			ps.setString(1, serviceSubGroupName);
			ps.setInt(2, serviceGroupId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_SERVICE_SUBGROUPS_DETAILS = "SELECT ssg.service_sub_group_id, ssg.service_sub_group_name," +
		" sg.service_group_name, ssg.status, ssg.display_order,	ssg.service_sub_group_code" +
		" FROM service_sub_groups ssg" +
		" JOIN service_groups sg USING(service_group_id) WHERE ssg.status = 'A' ORDER BY ssg.service_sub_group_id";

	public static List<BasicDynaBean> getServiceSubGroupsDetails()throws SQLException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pstmt = con.prepareStatement(GET_SERVICE_SUBGROUPS_DETAILS);
			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}
	
	public Map<Integer , Integer> getServSubGrpAndServGrpsMap() throws SQLException {
		Map<Integer , Integer> servGrpsMap = new HashMap<Integer,Integer>();
		List<BasicDynaBean> servSubGrpBeanList= this.listAll();
		for(BasicDynaBean servSubGrpBean : servSubGrpBeanList) {
			servGrpsMap.put((Integer)servSubGrpBean.get("service_sub_group_id"), (Integer)servSubGrpBean.get("service_group_id"));
		}
		return servGrpsMap;
	}
	
	private static final String GET_ALL_SERVICE_SUB_GRPS = "select service_sub_group_id from "
      + " service_sub_groups ";
  
  public List getAllServiceSubGrps(List grpIdList) {
    String[] placeholdersArr = new String[grpIdList.size()];
    Arrays.fill(placeholdersArr, "?");
    StringBuilder query = new StringBuilder();
    query.append(GET_ALL_SERVICE_SUB_GRPS);
    query.append("WHERE service_group_id in (").
             append(StringUtils.arrayToCommaDelimitedString(placeholdersArr)).append(")");
    List<BasicDynaBean> list = DatabaseHelper.queryToDynaList(query.toString(), grpIdList.toArray());
    List values = new ArrayList();
    for(BasicDynaBean bean : list){
        values.add(bean.get("service_sub_group_id"));
    }
    return values;
   }
 
}
