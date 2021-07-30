package com.insta.hms.master.sectionrolerights;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SectionRoleRightsDAO extends GenericDAO{

	public SectionRoleRightsDAO() {
		super("insta_section_rights");
	}
	private static final String selectFields = "SELECT * ";
	private static final String fromTables = " FROM insta_section_rights";
	private static final String selectCount = "SELECT count(section_role_id) ";

	String GET_ALL_SECTIONS_RIGHTS = "Select section_id from insta_section_rights Where role_id=?";
	public List<String> getAllSectionsRights(Integer roleId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			StringBuilder query = new StringBuilder(GET_ALL_SECTIONS_RIGHTS); 
			ps = con.prepareStatement(query.toString());
			ps.setInt(1, roleId);
			List<String> section_ids = DataBaseUtil.queryToArrayList1(ps);
			return section_ids;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	String GET_ALL_ROLES = " SELECT ur.role_id, ur.role_name"
	        + " FROM u_role ur"
	        + " JOIN insta_section_rights isr ON (isr.section_id=? AND isr.role_id=ur.role_id)"
	        + " ORDER BY ur.role_name";
	public List<BasicDynaBean> getallroles(Integer sectionId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			StringBuilder query = new StringBuilder(GET_ALL_ROLES);
			ps = con.prepareStatement(query.toString());
			ps.setInt(1, sectionId);
			List<BasicDynaBean> roles = DataBaseUtil.queryToDynaList(ps);
			return roles;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean save(String[] roles, Integer sectionId) throws SQLException, IOException {
		Connection con = null;
		Boolean flag = false;
		Integer roles_count = roles == null ? 0 : roles.length;
		List<BasicDynaBean> beans = new ArrayList<BasicDynaBean>();
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			this.delete(con, "section_id", sectionId);
			for (int i = 0; i < roles_count; i++) {
				BasicDynaBean temp = this.getBean();
				temp.set("role_id", Integer.parseInt(roles[i]));
				temp.set("section_id", sectionId);
				beans.add(temp);
			}
			if (roles_count > 0) {
				this.insertAll(con, beans);
			}
			flag = true;
		} finally {
			DataBaseUtil.commitClose(con, flag);
		}
		return true;
	}
}
