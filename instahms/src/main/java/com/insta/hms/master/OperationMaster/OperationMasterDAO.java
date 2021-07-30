package com.insta.hms.master.OperationMaster;

import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OperationMasterDAO extends GenericDAO {

	static Logger logger = LoggerFactory.getLogger(OperationMasterDAO.class);

	public OperationMasterDAO() {
		super("operation_master");
	}

	public String getNextId() throws SQLException {
		return AutoIncrementId.getNewIncrUniqueId("OP_ID", "OPERATION_MASTER","OPERATIONID");
	}

	/*
	 * Search: returns a PagedList suitable for a dashboard type list
	 */
	private static final String SEARCH_FIELDS = " SELECT *";

	private static final String SEARCH_COUNT =  " SELECT count(*)";

	private static final String SEARCH_TABLES =
		" FROM (SELECT ood.operation_id as op_id, ood.org_id, ood.applicable, ood.item_code, " +
		"  ood.code_type, op.operation_name, op.status, op.dept_id, dept.dept_name, op.service_sub_group_id, " +
		"  'operations'::text as chargeCategory,od.org_name, ood.is_override "+
		"	FROM operation_org_details ood " +
		"  JOIN operation_master op ON (op.op_id = ood.operation_id) " +
		"  JOIN organization_details od on(od.org_id=ood.org_id) "+
		"  JOIN department dept ON (dept.dept_id = op.dept_id)) AS foo ";


	public PagedList search(Map requestParams, Map pagingParams) throws ParseException, SQLException {
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, SEARCH_FIELDS, SEARCH_COUNT, SEARCH_TABLES, pagingParams);
			qb.addFilterFromParamMap(requestParams);
			qb.addSecondarySort("op_id");
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	public boolean insert(Connection con, BasicDynaBean b) throws SQLException, java.io.IOException {
		try {
			super.insert(con, b);
			return true;
		} catch (SQLException e) {
			if (!DataBaseUtil.isDuplicateViolation(e))
				throw (e);
		}
		return false;
	}

	public int update(Connection con, Map columnData, String keyName, Object keyValue)
		throws SQLException, IOException {
		try {
			int rows = super.update(con, columnData, keyName, keyValue);
			return rows;
		} catch (SQLException e) {
			if (!DataBaseUtil.isDuplicateViolation(e))
				throw (e);
		}
		return 0;
	}

	public List<String> getAllNames() throws SQLException {
		return getColumnList("operation_name");
	}

	private static final String OPERATION_DETAILS =
		" SELECT op.*, ood.org_id, ood.applicable, ood.item_code, o.org_name, ood.code_type " +
		" FROM operation_master op " +
		"  JOIN operation_org_details ood ON (ood.operation_id = op.op_id) " +
		"  JOIN organization_details o ON (o.org_id = ood.org_id) " +
		" WHERE op.op_id=? AND ood.org_id=? ";

	public BasicDynaBean getOperationDetails(String operationId, String orgId) throws SQLException {
		List<BasicDynaBean> l = DataBaseUtil.queryToDynaList(OPERATION_DETAILS, operationId, orgId);
		if (l.size() > 0)
			return l.get(0);
		else
			return null;
	}

  public List<BasicDynaBean> getActiveInsuranceCategories(String id){
    return DatabaseHelper.queryToDynaList(SELECT_INSURANCE_CATEGORY_IDS, id);
  }

  private static final String SELECT_INSURANCE_CATEGORY_IDS = "SELECT insurance_category_id "
      + "FROM operation_insurance_category_mapping "
      + "WHERE operation_id =?";

	private static String GET_OPERATION_DEPARTMENT="SELECT dept_name, dept_id FROM department";

	public static HashMap getOperationDepartmentHashMap() throws SQLException{

		HashMap<String,String> operationDeptHashMap =new HashMap <String,String>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs=null;
		List list = null;
		con=DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_OPERATION_DEPARTMENT);
		rs=ps.executeQuery();
		while(rs.next()){
			operationDeptHashMap.put(rs.getString("dept_name"), rs.getString("dept_id"));
		}
		DataBaseUtil.closeConnections(con, ps,rs);

		return operationDeptHashMap;
	}

	private static final String GET_OPERATION_NAMES = "SELECT op_id, operation_name FROM operation_master" +
			" WHERE dept_id = ?";

	public static HashMap<String, List<Map<String, String>>> getOperationNames() throws SQLException {

		Connection con = null;
		PreparedStatement pstmt = null;
		HashMap deptMap = getOperationDepartmentHashMap();
		HashMap<String, List<Map<String, String>>> deptNamesMap = new HashMap<String, List<Map<String, String>>>();
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			Iterator it = deptMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> mapEntry = (Map.Entry<String, String>)it.next();
				pstmt = con.prepareStatement(GET_OPERATION_NAMES);
				pstmt.setString(1, (String)mapEntry.getValue());
				List<Map<String, String>> mapList = ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(pstmt));
				deptNamesMap.put(mapEntry.getValue(), mapList);
			}
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

		return deptNamesMap;
	}
	
	private static final String GET_OP_ITEM_SUBGROUP_DETAILS = "select oisg.item_subgroup_id,isg.item_subgroup_name,ig.item_group_id,item_group_name,igt.item_group_type_id,igt.item_group_type_name " +
			" from operation_item_sub_groups oisg "+
			" left join item_sub_groups isg on (isg.item_subgroup_id = oisg.item_subgroup_id) "+
			" left join operation_master om on (om.op_id = oisg.op_id) "+
			" left join item_groups ig on (ig.item_group_id = isg.item_group_id)"+
			" left join item_group_type igt on (igt.item_group_type_id = ig.item_group_type_id)"+
			" where oisg.op_id = ? "; 

		public static List<BasicDynaBean> getOpItemSubGroupDetails(String opId) throws SQLException {
			List list = null;
			Connection con = null;
		    PreparedStatement ps = null;
		 try{
			 con=DataBaseUtil.getReadOnlyConnection();
			 ps=con.prepareStatement(GET_OP_ITEM_SUBGROUP_DETAILS);
			 ps.setString(1, opId);
			 list = DataBaseUtil.queryToDynaList(ps);
		 }finally {
			 DataBaseUtil.closeConnections(con, ps);
		 }
		return list;
		}

}

