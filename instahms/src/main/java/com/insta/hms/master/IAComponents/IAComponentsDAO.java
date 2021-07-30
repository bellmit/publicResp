/**
 *
 */
package com.insta.hms.master.IAComponents;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author krishna
 *
 */
public class IAComponentsDAO extends GenericDAO {

	public IAComponentsDAO() {
		super("assessment_components");
	}

	private static final String GET_COMPONENTS =
		" SELECT ac.form_name, d.dept_name, ac.dept_id, ac.vitals, ac.id, forms " +
		" FROM	assessment_components ac " +
		"	LEFT JOIN department d ON (ac.dept_id=d.dept_id) "; // get the dept id -1 also
	public List getComponents() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_COMPONENTS);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public BasicDynaBean getComponents(String deptId, Integer consultationId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			String GET_ACTIVE_FORMS =
				" SELECT array_to_string (array (" +
				"	SELECT foo.form_id FROM (select regexp_split_to_table(forms, ',') as form_id, " +
				"		generate_series(1, array_upper(regexp_split_to_array(forms, E','), 1)) as display_order " +
				"	FROM assessment_components where dept_id=?) as foo " +
				"		LEFT JOIN physician_form_desc pfd ON (pfd.form_id::text=foo.form_id) " +
				"	WHERE coalesce(pfd.status, 'A')='A' order by display_order), ',') as forms, vitals " +
				" FROM assessment_components tc where dept_id=?";

			ps = con.prepareStatement(GET_ACTIVE_FORMS);
			ps.setString(1, deptId);
			ps.setString(2, deptId);
			bean = DataBaseUtil.queryToDynaBean(ps);

			if (bean == null) {
				ps.setString(1, "-1");
				ps.setString(2, "-1");
				bean = DataBaseUtil.queryToDynaBean(ps);
			}
			ps.close();

			if (consultationId != null && consultationId != 0) {
				ps = con.prepareStatement("SELECT array_to_string ( array (SELECT form_id FROM patient_forms_details " +
					"	WHERE consultation_id=? AND assessment_form='Y' order by assessment_display_order), ',') as forms ");
				ps.setInt(1, consultationId);
				BasicDynaBean cbean = DataBaseUtil.queryToDynaBean(ps);
				if (cbean != null && cbean.get("forms") != null && !cbean.get("forms").equals(""))
					bean.set("forms", cbean.get("forms"));

			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	public BasicDynaBean getComponents(int componentId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_COMPONENTS + " WHERE ac.id=? ");
			ps.setInt(1, componentId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean exists(int id, String formName, String deptId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String query = "SELECT dept_id FROM assessment_components WHERE dept_id=? AND lower(form_name)=lower(?)";
			if (id != 0)
				query += " AND id!=? ";
			ps = con.prepareStatement("");
			ps.setString(1, deptId);
			ps.setString(2, formName);
			if (id != 0)
				ps.setInt(3, id);

			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	public boolean exists(String deptId, int exceptCompId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT dept_id FROM assessment_components where dept_id=? " +
					" and id!=? ");
			ps.setString(1, deptId);
			ps.setInt(2, exceptCompId);
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

}
