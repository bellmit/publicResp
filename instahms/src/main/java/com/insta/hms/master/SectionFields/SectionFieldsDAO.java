/**
 *
 */
package com.insta.hms.master.SectionFields;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class SectionFieldsDAO extends GenericDAO {
	public SectionFieldsDAO() {
		super("section_field_desc");
	}

	private static final String SELECT_FORM_FIELDS =
		"SELECT sfd.field_id, sfd.section_id, sfd.display_order, sfd.field_name, sfd.field_type, " +
		"	sfd.allow_others, sfd.allow_normal, sfd.normal_text, sfd.no_of_lines, " +
		"	sfd.status,  sm.section_title, sm.allow_all_normal, sm.linked_to, sfd.is_mandatory " ;
	private static final String SELECT_COUNT = "SELECT count(field_id)";
	private static final String FROM_TABLES = " FROM section_field_desc sfd JOIN section_master sm using (section_id) ";
	public PagedList searchSectionFields(Map params, Map<LISTING, Object> listingParams) throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			params.put("section_id@type", new String[]{"integer"});
			qb = new SearchQueryBuilder(con, SELECT_FORM_FIELDS, SELECT_COUNT, FROM_TABLES, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();
			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	/*
	 * returns true : if found duplicate field name within the same form.
	 */
	public boolean fieldNameExists(String fieldName, Integer sectionId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement("SELECT field_name FROM section_field_desc WHERE section_id=? and " +
					" upper(field_name)=upper(?)");
			ps.setInt(1, sectionId);
			ps.setString(2, fieldName);
			rs = ps.executeQuery();
			if (rs.next()) return true;
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	private static final String FIELD_NAME_EXISTS = "SELECT field_name FROM section_field_desc " +
		" WHERE upper(field_name)=upper(?) and section_id=? and field_id!=?";
	public boolean exists(Connection con, String fieldName, Integer sectionId, Integer fieldId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(FIELD_NAME_EXISTS);
			ps.setString(1, fieldName);
			ps.setInt(2, sectionId);
			ps.setInt(3, fieldId);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return false;
	}

	private static final String FIELD_DETAILS =
		"SELECT sfd.field_id, sfd.pattern_id, sfd.section_id, sfd.display_order, sfd.field_name, sfd.field_type, " +
		"	sfd.allow_others, sfd.allow_normal, sfd.normal_text, sfd.no_of_lines, " +
		"	sfd.status, sm.section_title, sfd.observation_type, sfd.observation_code, sfd.markers, " +
		"	sfd.is_mandatory, sfd.use_in_presenting_complaint, sfd.phrase_category_id as field_phrase_category_id, sfd.default_to_current_datetime " +
		" FROM section_field_desc sfd JOIN section_master sm USING (section_id) " +
		" WHERE field_id=?";
	public BasicDynaBean getFieldDetails(int field_id) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(FIELD_DETAILS);
			ps.setInt(1, field_id);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String OPTION_DETAILS =
		" SELECT sfd.section_id, sfd.field_id, sfd.field_name, sfd.allow_others, sfd.allow_normal, " +
		"	sfd.field_type, sfd.normal_text, sfd.status as field_status, " +
		"	sfd.display_order as field_display_order, sfd.no_of_lines, " +
		"	option.option_id, option.display_order as option_display_order, option.option_value, " +
		"	option.status as option_status, option.value_code, option.phrase_category_id as option_phrase_category_id, " +
		"	cat.phrase_suggestions_category as phrase_category_name, option.pattern_id " +
		" FROM section_field_desc sfd " +
		"	JOIN section_field_options option using (field_id) " +
		"	LEFT JOIN phrase_suggestions_category_master cat ON (cat.phrase_suggestions_category_id=option.phrase_category_id)" +
		" WHERE sfd.field_id=?";
	public List getOptionDetails(int field_id) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(OPTION_DETAILS);
			ps.setInt(1, field_id);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public Map getImageDetails(int fieldId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Map imgDetails = null;
		try {
			ps = con.prepareStatement("SELECT field_id, field_name, filename, content_type, file_content FROM " +
					" section_field_desc WHERE field_id=?");
			ps.setInt(1, fieldId);
			rs = ps.executeQuery();
			if (rs.next()) {
				imgDetails = new HashMap();
				int i = 0;
				imgDetails.put("field_id", rs.getString("field_id"));
				imgDetails.put("field_name", rs.getString("field_name"));
				imgDetails.put("filename", rs.getString("filename"));
				imgDetails.put("content_type", rs.getString("content_type"));
				imgDetails.put("file_content", rs.getBinaryStream("file_content"));
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return imgDetails;
	}

	public static final String ALL_FILEDS =
			" SELECT field_id, pattern_id, section_id, display_order, field_name, field_type, " +
			" 	allow_others, allow_normal, normal_text, no_of_lines, status, is_mandatory, " +
			"	phrase_category_id as field_phrase_category_id, observation_code, observation_type, default_to_current_datetime " +
			" FROM section_field_desc where section_id=? and status=? " +
			" ORDER BY display_order ";
	public List getFields(int sectionId, String status) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(ALL_FILEDS);
			ps.setInt(1, sectionId);
			ps.setString(2, status);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
