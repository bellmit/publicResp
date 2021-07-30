package com.insta.hms.master.ServiceMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceDepartmentDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(ServiceDepartmentDAO.class);

	public ServiceDepartmentDAO() {
		super("services_departments");
	}

	/*
	 * Old method: deprecated
	 */
	private static final String DEPARTMENTLIST =
		"SELECT department from services_departments ORDER BY department";

	public ArrayList getDepartment()throws SQLException {
		Connection con =DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps =con.prepareStatement(DEPARTMENTLIST);
		ArrayList al = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		logger.debug("{}", al);
		return al;
	}

	public List<BasicDynaBean> getServiceDepartments() throws SQLException {
		return super.listAll("department");		// sort by department
	}

	private static String GET_SERVICE_DEPARTMENT="SELECT department,serv_dept_id FROM services_departments";

	public static HashMap getServiceDepartmentHashMap() throws SQLException{

		HashMap<String,String> servDeptHashMap =new HashMap <String,String>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs=null;
		List list = null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_SERVICE_DEPARTMENT);
		rs=ps.executeQuery();
		while(rs.next()){
			servDeptHashMap.put(rs.getString("department"), rs.getString("serv_dept_id"));
		}
		DataBaseUtil.closeConnections(con, ps,rs);

		return servDeptHashMap;
	}

	public static final String GET_ALL_USER_DEPTS_IDS = "SELECT * FROM user_services_depts WHERE emp_username = ?";
	public static List<BasicDynaBean> getUserServiceDepts(String user) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(GET_ALL_USER_DEPTS_IDS, user);
		return l;
		}

}

