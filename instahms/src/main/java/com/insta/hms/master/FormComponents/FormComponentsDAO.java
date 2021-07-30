/**
 *
 */
package com.insta.hms.master.FormComponents;

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
public class FormComponentsDAO extends GenericDAO {

	public FormComponentsDAO() {
		super("form_components");
	}

	private static final String GET_COMPONENTS =
		" SELECT cc.form_name, cc.id, case when form_type = 'Form_Serv' then sd.department else d.dept_name end as dept_name, fdd.dept_id, " +
		"	cc.form_type, cc.sections, cc.group_patient_sections, cc.operation_id, om.operation_name, cc.service_id, s.service_name, " +
		"	cc.immunization, cc.print_template_id, cc.status, cc.doc_type " +
		" FROM	form_components cc " +
		" JOIN form_department_details fdd ON (fdd.id=cc.id) " +
		"	LEFT JOIN department d ON (fdd.dept_id=d.dept_id) "+
		"	LEFT JOIN services_departments sd ON (fdd.dept_id=sd.serv_dept_id::text) " +
		"	LEFT JOIN services s ON (s.service_id=cc.service_id)" +
		"  LEFT JOIN operation_master om ON (cc.operation_id=om.op_id)"; // get the dept id -1 also
	public List getComponents() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_COMPONENTS +  "ORDER BY cc.form_name");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public BasicDynaBean getComponentsForDeptAndVisitType(int componentId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_COMPONENTS + " WHERE cc.id=? " + " ORDER BY cc.form_name");
			ps.setInt(1, componentId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean exists(String formName, String operationId, String deptId, String formType, String serviceId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder query = new StringBuilder("SELECT dept_id FROM form_components fc, form_department_details fdd where fc.id=fdd.id and ");
		try {
			if (formType.equals("Form_Serv")) {
				query.append("(service_id=? AND dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?)");
			} else if (formType.equals("Form_OT")) {
				query.append("(operation_id=? AND dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?)");
			} else if (formType.equals("Form_CONS")) {						//for Op patient
				query.append("(dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?)");
			} else if (formType.equals("Form_IP")) {												//for Ip patient
				query.append("(dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?)");
			} else if (formType.equals("Form_TRI")) {
				query.append("(dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?)");
			} else if (formType.equals("Form_IA")) {
				query.append("(dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?)");
			} else if (formType.equals("Form_Gen")) {
				// we can add more than one form for the same department, so only check form name existance.
				query.append("LOWER(form_name)=LOWER(?)");
			}

			ps = con.prepareStatement(query.toString());
			int index = 1;
			if (formType.equals("Form_Serv")) {
				ps.setString(index++, serviceId);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if(formType.equals("Form_OT")) {
				ps.setString(index++, operationId);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_CONS")) {
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_IP")) {
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_TRI")) {
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_IA")) {
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_Gen")) {
				ps.setString(index++, formName);
			}

			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	public boolean exists(int id, String formName, String operationId, String deptId, String formType, String serviceId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder query = new StringBuilder("SELECT dept_id FROM form_components fc, form_department_details fdd where fdd.id=fc.id and ");
		try {
			if (formType.equals("Form_Serv")) {
				query.append("id!=? AND ((service_id=? AND dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?))");
			} else if (formType.equals("Form_OT")) {
				query.append("id!=? AND ((operation_id=? AND dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?))");
			} else if (formType.equals("Form_CONS")) {						//for Op patient
				query.append("id!=? AND ((dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?))");
			} else if (formType.equals("Form_IP")) {												//for Ip patient
				query.append("id!=? AND ((dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?))");
			} else if (formType.equals("Form_TRI")) {
				query.append("id!=? AND ((dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?))");
			} else if (formType.equals("Form_IA")) {
				query.append("id!=? AND ((dept_id=? AND form_type=?) OR LOWER(form_name)=LOWER(?))");
			} else if (formType.equals("Form_Gen")) {
				// we can add more than one form for the same department, so only check form name existance.
				query.append("id!=? AND LOWER(form_name)=LOWER(?)");
			}

			ps = con.prepareStatement(query.toString());
			int index = 1;
			if (formType.equals("Form_Serv")) {
				ps.setInt(index++, id);
				ps.setString(index++, serviceId);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if(formType.equals("Form_OT")) {
				ps.setInt(index++, id);
				ps.setString(index++, operationId);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_CONS")) {
				ps.setInt(index++, id);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_IP")) {
				ps.setInt(index++, id);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_TRI")) {
				ps.setInt(index++, id);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_IA")) {
				ps.setInt(index++, id);
				ps.setString(index++, deptId);
				ps.setString(index++, formType);
				ps.setString(index++, formName);
			} else if (formType.equals("Form_Gen")) {
				ps.setInt(index++, id);
				ps.setString(index++, formName);
			}

			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	public boolean exists(String operationId, String deptId, String visitType, String serviceId, int exceptCompId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringBuilder query = new StringBuilder("SELECT dept_id FROM form_components fc, form_department_details fdd where fdd.id=fc.id and ");
		try {
			if (visitType.equals("sc")) {
				query.append("service_id=? and dept_id=? and form_type=lower(?) and id!=?");
			} else if (visitType.equals("s")) {
				query.append("operation_id=? and dept_id=? and form_type=lower(?) and id!=?");

			} else if (visitType.equals("o")) {						//for Op patient
				query.append("dept_id=? and form_type=lower(?) and id!=?");
			} else {												//for Ip patient
				query.append("dept_id=? and form_type=lower(?) and id!=?");
			}
			ps = con.prepareStatement(query.toString());
			if (visitType.equals("sc")) {
				ps.setString(1, serviceId);
				ps.setString(2, deptId);
				ps.setString(3, visitType);
				ps.setInt(4, exceptCompId);
			} else if(visitType.equals("s")) {
				ps.setString(1, operationId);
				ps.setString(2, deptId);
				ps.setString(3, visitType);
				ps.setInt(4, exceptCompId);
			} else {
				ps.setString(1, deptId);
				ps.setString(2, visitType);
				ps.setInt(3, exceptCompId);
			}
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	/*
	 * get the ip record components
	 */
	public BasicDynaBean getComponents(String deptId, String visit_type, String visitId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			ps = con.prepareStatement("SELECT array_to_string ( array (SELECT form_id FROM patient_forms_details " +
					"	WHERE patient_id=? AND consultation_id is null AND operation_proc_id is null AND serv_prescribed_id is null " +
					"	order by ip_record_display_order), ',') as forms ");
			ps.setString(1, visitId);
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null && !bean.get("forms").equals("")) return bean;

			String GET_ACTIVE_FORMS =
				" SELECT array_to_string (array " +
				"	(SELECT foo.form_id FROM (SELECT regexp_split_to_table(forms, ',') as form_id, " +
				"			generate_series(1, array_upper(regexp_split_to_array(forms, E','), 1)) as display_order " +
				"	 FROM form_components where dept_id=? and form_type=?) as foo " +
				" 		LEFT JOIN physician_form_desc pfd ON (pfd.form_id::text=foo.form_id) " +
				" 	 WHERE coalesce(pfd.status, 'A')='A' order by display_order), ',') as forms ";
			ps = con.prepareStatement(GET_ACTIVE_FORMS);
			ps.setString(1, deptId);
			ps.setString(2, visit_type);
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null && !bean.get("forms").equals("")) return bean;

			ps.setString(1, "-1");
			ps.setString(2, visit_type);
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null && !bean.get("forms").equals("")) return bean;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;

	}

	/*
	 *  get the ot record components.
	 */
	public BasicDynaBean getComponents(String deptId, String visit_type, String visitId, String operationId, int operationProcId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			if (operationProcId != 0) {
				ps = con.prepareStatement(
						" SELECT array_to_string ( array (SELECT form_id FROM patient_forms_details pfd " +
						"	WHERE operation_proc_id=? order by surgery_display_order), ',') as forms, om.operation_name, ops.operation_proc_id " +
						" FROM operation_details ods " +
						" 	LEFT JOIN operation_procedures ops ON (ods.operation_details_id=ops.operation_details_id) " +
						" 	LEFT JOIN operation_master om ON (om.op_id=ops.operation_id) " +
						" WHERE ods.patient_id=? AND ops.operation_proc_id=?" );
				ps.setInt(1, operationProcId);
				ps.setString(2, visitId);
				ps.setInt(3, operationProcId);
				bean = DataBaseUtil.queryToDynaBean(ps);
				if (bean != null && !bean.get("forms").equals("")) return bean;
			}

			String GET_ACTIVE_FORMS =
				" SELECT array_to_string (array " +
				"	(SELECT foo.form_id FROM (SELECT regexp_split_to_table(forms, ',') as form_id, " +
				"			generate_series(1, array_upper(regexp_split_to_array(forms, E','), 1)) as display_order " +
				"	 FROM form_components cfc #sub_query_where# ) as foo " +
				" 		LEFT JOIN physician_form_desc pfd ON (pfd.form_id::text=foo.form_id) " +
				" 	 WHERE coalesce(pfd.status, 'A')='A' order by display_order), ',') as forms, om.operation_name, "+operationProcId+" as operation_proc_id " +
				" FROM consultation_form_components cfc " +
                " 	JOIN operation_master om ON (om.op_id = ?) ";
			// here om.op_id=cfc.operation_id can't be used, because for a perticular operation if forms are not available we will bring
			// the default operation(-1) forms, in this it will fail to bring the operation.

			BasicDynaBean opbean = new GenericDAO("operation_master").findByKey("op_id"	, operationId);

			String query = "";
			String whereCond = " WHERE cfc.form_type = ? and cfc.operation_id = ? ";
			query = GET_ACTIVE_FORMS.replace("#sub_query_where#", whereCond); // this for getting formids,
			query = query + whereCond; // this where for getting operation name.
			ps = con.prepareStatement(query);

			ps.setString(1, visit_type);
			ps.setString(2, operationId);

			ps.setString(3, operationId);
			ps.setString(4, visit_type);
			ps.setString(5, operationId);

			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) return bean;
			ps.close();

			//finding physician_forms in particular department and all operations.
			whereCond = " WHERE cfc.form_type = ? and cfc.dept_id = ? and cfc.operation_id = ? ";
			query = GET_ACTIVE_FORMS.replace("#sub_query_where#", whereCond); // this for getting formids,
			query = query + whereCond; // this where for getting operation name.

			ps = con.prepareStatement(query);
			ps.setString(1, visit_type);
			ps.setString(2, (String)opbean.get("dept_id"));
			ps.setString(3, "-1");

			ps.setString(4, operationId);
			ps.setString(5, visit_type);
			ps.setString(6, (String)opbean.get("dept_id"));
			ps.setString(7, "-1");

			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) return bean;
			ps.close();

			//finding physician_forms in all departments and all operations.
			whereCond = " WHERE cfc.form_type = ? and cfc.dept_id = ? and cfc.operation_id = ? ";
			query = GET_ACTIVE_FORMS.replace("#sub_query_where#", whereCond); // this for getting formids,
			query = query + whereCond; // this where for getting operation name.

			ps = con.prepareStatement(query);
			ps.setString(1, visit_type);
			ps.setString(2, "-1");
			ps.setString(3, "-1");

			ps.setString(4, operationId);
			ps.setString(5, visit_type);
			ps.setString(6, "-1");
			ps.setString(7, "-1");

			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) return bean;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;

	}
	/* to get the services
	 *
	 */
	public BasicDynaBean getComponents(String deptId, String visit_type, String serviceId, int service_presc_id) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			if (service_presc_id != 0) {
				ps = con.prepareStatement(
						" SELECT array_to_string ( array (SELECT form_id FROM patient_forms_details " +
						"	WHERE serv_prescribed_id=? AND consultation_id is null AND operation_proc_id is null order by service_display_order), ',') as forms ");
				ps.setInt(1,service_presc_id);
								bean = DataBaseUtil.queryToDynaBean(ps);
				if (bean != null && !bean.get("forms").equals("")) return bean;
			}

			String GET_ACTIVE_FORMS =
				" SELECT array_to_string (array " +
				"	(SELECT foo.form_id FROM (SELECT regexp_split_to_table(forms, ',') as form_id, " +
				"			generate_series(1, array_upper(regexp_split_to_array(forms, E','), 1)) as display_order " +
				"	 FROM form_components cfc #sub_query_where# ) as foo " +
				" 		LEFT JOIN physician_form_desc pfd ON (pfd.form_id::text=foo.form_id) " +
				" 	 WHERE coalesce(pfd.status, 'A')='A' order by display_order), ',') as forms " +
				" FROM consultation_form_components cfc " +
                " 	LEFT JOIN services s ON (s.service_id = ?) ";


			String query = "";
			String whereCond = " WHERE cfc.form_type = ? and cfc.service_id = ? ";
			query = GET_ACTIVE_FORMS.replace("#sub_query_where#", whereCond); // this for getting formids,
			query = query + whereCond; // this where for getting service name.
			ps = con.prepareStatement(query);

			ps.setString(1, visit_type);
			ps.setString(2, serviceId);

			ps.setString(3, serviceId);
			ps.setString(4, visit_type);
			ps.setString(5, serviceId);

			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) return bean;
			ps.close();

			//finding physician_forms in particular department and all services.
			whereCond = " WHERE cfc.form_type = ? and cfc.dept_id = ? and cfc.service_id = ? ";
			query = GET_ACTIVE_FORMS.replace("#sub_query_where#", whereCond); // this for getting formids,
			query = query + whereCond; // this where for getting service name.

			ps = con.prepareStatement(query);
			ps.setString(1, visit_type);
			ps.setString(2, deptId);
			ps.setString(3, "-1");

			ps.setString(4, "-1");
			ps.setString(5, visit_type);
			ps.setString(6, deptId);
			ps.setString(7, "-1");

			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) return bean;
			ps.close();

			//finding physician_forms in all departments and all services.
			whereCond = " WHERE cfc.form_type = ? and cfc.dept_id = ? AND cfc.service_id=?";
			query = GET_ACTIVE_FORMS.replace("#sub_query_where#", whereCond); // this for getting formids,
			query = query + whereCond; // this where for getting service name.

			ps = con.prepareStatement(query);
			ps.setString(1, visit_type);
			ps.setString(2, "-1");
			ps.setString(3, "-1");
			ps.setString(4, "-1");

			ps.setString(5, visit_type);
			ps.setString(6,"-1");
			ps.setString(7, "-1");
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null) return bean;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;

	}


	/*
	 * get the consultation form components
	 */
	public BasicDynaBean getComponents(String deptId, String formType, Integer consultationId) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			if (consultationId != null && consultationId != 0) {
				ps = con.prepareStatement(
						" SELECT sections FROM patient_forms WHERE item_id=? AND form_type=?");
				ps.setInt(1, consultationId);
				ps.setString(2, formType);
				bean = DataBaseUtil.queryToDynaBean(ps);
			}
			if (bean != null && !bean.get("forms").equals("")) return bean;

			String GET_ACTIVE_FORMS =
				" SELECT array_to_string (array " +
				"	(SELECT foo.form_id FROM (SELECT regexp_split_to_table(forms, ',') as form_id, " +
				"			generate_series(1, array_upper(regexp_split_to_array(forms, E','), 1)) as display_order " +
				"	 FROM form_components where dept_id=? and form_type=?) as foo " +
				" 		LEFT JOIN physician_form_desc pfd ON (pfd.form_id::text=foo.form_id) " +
				" 	 WHERE coalesce(pfd.status, 'A')='A' order by display_order), ',') as forms ";
			ps = con.prepareStatement(GET_ACTIVE_FORMS);
			ps.setString(1, deptId);
			ps.setString(2, formType);
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null && !bean.get("forms").equals("")) return bean;

			ps.setString(1, "-1");
			ps.setString(2, formType);
			bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean != null && !bean.get("forms").equals("")) return bean;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return null;
	}

	public BasicDynaBean getPatientSectionsGroup(String depId, String formType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			ps = con.prepareStatement("select group_patient_sections from form_components fc, form_department_details fdd where dept_id=? "
					+ " and form_type=? and fdd.id=fc.id");
			ps.setString(1, depId);
			ps.setString(2, formType);
			List l = DataBaseUtil.queryToDynaList(ps);
			if (l == null || l.isEmpty()) {
				ps.setString(1, "-1");
				ps.setString(2, formType);
				l = DataBaseUtil.queryToDynaList(ps);
				if (l != null && !l.isEmpty()) {
					bean = (BasicDynaBean)l.get(0);
				}
			} else {
				bean = (BasicDynaBean)l.get(0);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}

	public BasicDynaBean getPatientSectionsGroupForServices(String deptId, String serviceId, String formType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean bean = null;
		try {
			ps = con.prepareStatement("select group_patient_sections from form_components fc, form_department_details fdd where service_id=? "
					+ "	AND dept_id=? and form_type=? and fdd.id=fc.id");
			ps.setString(1, serviceId);
			ps.setString(2, deptId);
			ps.setString(3, formType);

			List l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty()) {
				return (BasicDynaBean)l.get(0);
			}
			ps.setString(1, "-1");
			ps.setString(2, deptId);
			ps.setString(3, formType);

			l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty()) {
				return (BasicDynaBean)l.get(0);
			}

			ps.setString(1, "-1");
			ps.setString(2, "-1");
			ps.setString(3, formType);

			l = DataBaseUtil.queryToDynaList(ps);
			if (l != null && !l.isEmpty()) {
				return  (BasicDynaBean)l.get(0);
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return bean;
	}
}
