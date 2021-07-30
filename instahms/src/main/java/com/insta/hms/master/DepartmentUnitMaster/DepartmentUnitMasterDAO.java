/**
 *
 */
package com.insta.hms.master.DepartmentUnitMaster;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author lakshmi.p
 *
 */
public class DepartmentUnitMasterDAO extends GenericDAO{

	public DepartmentUnitMasterDAO() {
		super("dept_unit_master");
	}

	private static final String DEPT_UNIT_FIELDS = "SELECT *";
	private static final String DEPT_UNIT_COUNT = "SELECT count(unit_id)";
	private static final String DEPT_UNIT_TABLES = " FROM (SELECT dept.dept_id, dept_name, unit_id," +
			" unit_name, um.status FROM dept_unit_master um JOIN department " +
			" dept ON (dept.dept_id = um.dept_id)) AS dept" ;

	public static PagedList searchDeptUnitList(Map map, Map listing) throws SQLException, ParseException {

		Connection con = null;
		SearchQueryBuilder qb = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			qb = new SearchQueryBuilder(con, DEPT_UNIT_FIELDS,
					DEPT_UNIT_COUNT, DEPT_UNIT_TABLES, listing);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("unit_id", true);
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String SEARCH_DEPT_UNIT = "SELECT * from dept_unit_master WHERE dept_id=? AND unit_name=? ";

	public static boolean isDeptUnitExists(String dept_id, String unit_name) throws SQLException {
		Connection con = null;
		PreparedStatement ps =null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SEARCH_DEPT_UNIT);
			ps.setString(1, dept_id);
			ps.setString(2, unit_name);
			rs = ps.executeQuery();
			if(rs.next()) return true;
			else return false;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
	}

	private static final String ALL_DEPT_UNITS = "SELECT * from dept_unit_master WHERE status = 'A' ";

	public static List<BasicDynaBean> getAllDepartmentUnits() throws SQLException {
		return DataBaseUtil.queryToDynaList(ALL_DEPT_UNITS);
	}

	private static final String ALL_DEPT_APPENDED_UNITS = "SELECT dum.dept_id,unit_id" +
			",dept_name||'-' ||unit_name AS unit_name,dum.status  " +
			" FROM dept_unit_master dum JOIN department dp USING(dept_id) " +
			" ORDER BY unit_name ";

	public static List<BasicDynaBean> getAllDepartmentAppendedUnits() throws SQLException {
		return DataBaseUtil.queryToDynaList(ALL_DEPT_APPENDED_UNITS);
	}

	private static final String UNIT_NAMESAND_iDS="select unit_name,unit_id from dept_unit_master";

	public static List getUnitNamesAndIds() throws SQLException{
		return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(UNIT_NAMESAND_iDS));
	}

}
