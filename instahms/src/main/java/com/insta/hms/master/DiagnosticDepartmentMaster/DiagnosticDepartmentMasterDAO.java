package com.insta.hms.master.DiagnosticDepartmentMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class DiagnosticDepartmentMasterDAO extends GenericDAO {

	public DiagnosticDepartmentMasterDAO() {
		super("diagnostics_departments");
	}

	public String getNextDeptId() throws SQLException{
		String id = null;
		id =AutoIncrementId.getNewIncrId("DDept_ID",
				"Diagnostics_Departments", "Diagnostics_Departments");
		return id;
	}

	public ArrayList getAllDiagnosticDepartmentsInArrayList() {

		String diaglist="SELECT DDEPT_ID, DDEPT_NAME, DESIGNATION, STATUS, CATEGORY " +
				" FROM DIAGNOSTICS_DEPARTMENTS WHERE STATUS = 'A'  ORDER BY DDEPT_NAME";

		return DataBaseUtil.queryToArrayList(diaglist);
	}

	public static final String DIAG_DEPT_LIST="SELECT DDEPT_ID, DDEPT_NAME FROM DIAGNOSTICS_DEPARTMENTS " +
	"WHERE STATUS='A' ORDER BY DDEPT_NAME";

	public static ArrayList getAllDiagnosticDepartments()throws SQLException{

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(DIAG_DEPT_LIST);
		ArrayList al = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return al;
	}

	private static final String GET_USER_DEPT = "SELECT lab_dept_id FROM u_user WHERE emp_username = ?";

	public String getUserDepartment( String userid)throws SQLException{

		String dept = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_USER_DEPT);
		ps.setString(1, userid);
		dept = DataBaseUtil.getStringValueFromDb(ps);
		ps.close();
		con.close();

		return dept;
	}

	public BasicDynaBean getUserDeptAndCategory(String userid)throws SQLException{

		BasicDynaBean dept = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT lab_dept_id, category from u_user u " +
					"	join diagnostics_departments dd ON (lab_dept_id=ddept_id) " +
					" WHERE emp_username=?");
			ps.setString(1, userid);
			dept = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return dept;
	}

	private static final String GET_DEPTS = "SELECT ddept_id,ddept_name,status,designation FROM " +
	" diagnostics_departments ddept WHERE category=? AND STATUS = 'A' ORDER BY ddept_name";

	public static ArrayList<Hashtable<String,String>> getDepts(String category)
		throws SQLException{

		ArrayList<Hashtable<String,String>> al =null;
		PreparedStatement ps = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_DEPTS);
		ps.setString(1, category);

		al = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return al;
	}

	public static final String getdiagdeptlist = "SELECT ddept_id, ddept_name FROM diagnostics_departments where status='A' order by ddept_name";
	public static ArrayList getdiagnosticdepartmentlist() {
        Connection con = null;
        PreparedStatement ps = null;
        ArrayList diagDeptList = null;
        try{
            return DataBaseUtil.queryToArrayList(getdiagdeptlist);
        }catch(Exception e){
            e.printStackTrace();
        }return diagDeptList;
    }

	public static HashMap getDiagDeptList() throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		HashMap <String,String>dDeptHasMap = new HashMap <String, String>();
		ResultSet rs=null;
		List list = null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(getdiagdeptlist);
		rs=ps.executeQuery();
		while(rs.next()){
			dDeptHasMap.put(rs.getString("ddept_name"), rs.getString("ddept_id"));
		}
		DataBaseUtil.closeConnections(con, ps,rs);
		return dDeptHasMap;
	}

	public static final String DIAG="UPDATE diagnostics SET status='I' WHERE ddept_id=?";

	public boolean updateStatus(String ddept_id) throws SQLException {
		Connection con=null;
		PreparedStatement ps=null;
		boolean success=true;
		try {
			con=DataBaseUtil.getConnection();
			ps=con.prepareStatement(DIAG);
			ps.setObject(1, ddept_id);
			ps.executeUpdate();
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return success;
	}

	private static final String GET_DIAG_DEPT_CENTER_STORE = "SELECT store_id from diagnostic_department_stores " +
			" where ddept_id=? and center_id=?";

	public static int getStoreOfDiagnosticDepartment(String testId,int center_id) throws SQLException {

		BasicDynaBean tBean = new GenericDAO("diagnostics").findByKey("test_id", testId);
		String ddeptId = ( tBean != null ) ? (String) tBean.get("ddept_id") : "";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int storeId = -1;
		try {
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_DIAG_DEPT_CENTER_STORE);
			ps.setString(1, ddeptId);
			ps.setInt(2, center_id);
			rs =  ps.executeQuery();
			if(rs.next()){
				storeId = rs.getInt(1);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return storeId;
	}

	 private static final String DDEPT_NAMESAND_iDS="select ddept_name,ddept_id from diagnostics_departments";
     public static List getDdeptsNamesAndIds() throws SQLException{

  	   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(DDEPT_NAMESAND_iDS));
     }

     private static final String IS_DIAG_DEPT_STORE_EXIST = "select * from diagnostic_department_stores " +
		" where ddept_id=? and center_id=?";
	public boolean isDiagDeptStoreExist(String ddept_id, int center_id) throws SQLException {
		 Connection con=null;
		 PreparedStatement ps=null;
		 ResultSet rs = null;
		 try
		 {
			 con=DataBaseUtil.getConnection();
			 ps=con.prepareStatement(IS_DIAG_DEPT_STORE_EXIST);
			 ps.setString(1, ddept_id);
			 ps.setInt(2, center_id);
			 rs = ps.executeQuery();
			 if (rs.next()) {
				return true;
			 }
		 } finally {
			 DataBaseUtil.closeConnections(con, ps, rs);
		 }
		 return false;
	}

	private static final String GET_DIAG_DEPT_STORE_DETAILS = "select ddept_id, dds.center_id, store_id, " +
		" center_name, dept_name, s.dept_id "+
		" from diagnostic_department_stores dds "+
		" left join stores s on (s.dept_id = dds.store_id) "+
		" left join hospital_center_master hcm on (hcm.center_id = dds.center_id) "+
		" where ddept_id = ? ";

	public List<BasicDynaBean> getDiagDeptStoreDetails(String ddeptId) throws SQLException {
	 Connection con = null;
	 PreparedStatement ps = null;
	 try{
		 con=DataBaseUtil.getReadOnlyConnection();
		 ps=con.prepareStatement(GET_DIAG_DEPT_STORE_DETAILS);
		 ps.setString(1, ddeptId);
		 return DataBaseUtil.queryToDynaList(ps);
	 }finally {
		 DataBaseUtil.closeConnections(con, ps);
	 }
	}

}
