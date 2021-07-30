package com.insta.hms.master.DepartmentMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepartmentMasterDAO extends GenericDAO{

	Connection con = null;


	public DepartmentMasterDAO() {
		super("department");
	}

	private static final String SELECT_FIELDS =	"SELECT dep.*, dt.dept_type_desc ";
	private static final String SELECT_TABLES =
		" FROM department dep " +
		"	LEFT JOIN department_type_master dt ON (dep.dept_type_id=dt.dept_type_id) ";
	private static final String COUNT = "SELECT count(*) ";
	public static PagedList searchDeptList(Map params, Map<LISTING, Object> listingParams)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, SELECT_FIELDS, COUNT, SELECT_TABLES, listingParams);
			qb.addFilterFromParamMap(params);
			qb.addSecondarySort("dept_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}


	public String getNextDepartmentId() throws SQLException {
		String departmentId = null;

		departmentId =	AutoIncrementId.getNewIncrId("DEPT_ID","DEPARTMENT", "Department");

		return departmentId;
	}

	public static final String GET_ALL_DEPTS = "SELECT dept_id,dept_name FROM department " +
		" ORDER BY dept_name";

	public static final String GET_ACTIVE_DEPTS = "SELECT dept_id,dept_name FROM department " +
		" WHERE status='A' ORDER BY dept_name";


	public static List getActiveDepts() throws SQLException {
		PreparedStatement ps = null;
		List deptNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ACTIVE_DEPTS);
			deptNames = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return deptNames;
	}

	/* obsolete */
	public static List getAvalDeptnames() {
		PreparedStatement ps = null;
		ArrayList deptNames = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_ALL_DEPTS);
			deptNames = DataBaseUtil.queryToArrayList(ps);
		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return deptNames;
	}

	public static final String dept="SELECT * FROM department WHERE status='A' AND " +
			" (coalesce(dept_type_id,'') = '' OR dept_type_id != 'NOCL') ORDER BY dept_name ";
	public static ArrayList getDeapartmentlist() {
        Connection con = null;
        PreparedStatement ps = null;
        ArrayList deptId = null;
        try{
            con = DataBaseUtil.getConnection();
            ps = con.prepareStatement(dept);
            deptId = DataBaseUtil.queryToArrayList(ps);
        }catch(Exception e){
            System.out.println("Exception raised in saluation id name");
        }finally{
            try {
                if (ps!=null) ps.close();
                if (con!=null) con.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return deptId ;
    }

	private static final String  ALL_DEPARTMENTS="SELECT DEPT_ID,DEPT_NAME FROM DEPARTMENT";

	public static ArrayList getAllDepartmentsList()throws SQLException{
		return DataBaseUtil.queryToArrayList(ALL_DEPARTMENTS);
	}


	public static final String DOC="UPDATE doctors SET status='I' WHERE dept_id=?";
	public static final String OPE="UPDATE operation_master SET status='I' WHERE dept_id=?";

	public boolean updateStatus(String dept_id)
	{
		Connection con=null;
		PreparedStatement ps=null;
		boolean success=true;
		try
		{
			con=DataBaseUtil.getConnection();
			ps=con.prepareStatement(DOC);
			ps.setObject(1, dept_id);
			ps.executeUpdate();

			ps=con.prepareStatement(OPE);
			ps.setObject(1, dept_id);
			ps.executeUpdate();
		}
		catch(SQLException e)
		{
			success=false;
		}
		finally
		{
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	 private static final String DEPARTMENT_NAMESAND_iDS="select dept_name,dept_id from department";

	 public static List getDepartmentsNamesAndIds() throws SQLException{
		 return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(DEPARTMENT_NAMESAND_iDS));
	 }

		public static HashMap getDeptDetails() throws SQLException{

			Connection con = null;
			PreparedStatement ps = null;
			HashMap <String,String>deptHasMap = new HashMap <String, String>();
			ResultSet rs=null;
			List list = null;
			con=DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(ALL_DEPARTMENTS);
			rs=ps.executeQuery();
			while(rs.next()){
				deptHasMap.put(rs.getString("dept_name"), rs.getString("dept_id"));
			}
			DataBaseUtil.closeConnections(con, ps,rs);
			return deptHasMap;

		}

	private static final String GET_DEPARTMENTS =
		"SELECT dept_id,dept_name from department order by dept_name";
	public static Map getAlldepartmentsMap() throws SQLException {
		Map<String, String> deptMap = new HashMap<String, String>();
		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DEPARTMENTS);
			rs = ps.executeQuery();
			while(rs.next()) {
				deptMap.put(rs.getString(2), rs.getString(1));
			}

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return deptMap;
	}
}
