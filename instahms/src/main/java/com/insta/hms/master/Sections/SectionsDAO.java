package com.insta.hms.master.Sections;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class SectionsDAO extends GenericDAO {

	public SectionsDAO() {
		super("section_master");
	}
	private static final String selectFields = "SELECT * ";
	private static final String fromTables = " FROM section_master";
	private static final String selectCount = "SELECT count(section_id) ";

	public PagedList searchSectionsList(Map params, Map<LISTING, Object> listingParams) throws SQLException,
		ParseException {
		SearchQueryBuilder qb = null;
		Connection con = DataBaseUtil.getConnection();
		try {
			qb = new SearchQueryBuilder(con, selectFields, selectCount, fromTables, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}


	private static final String FORM_NAME_EXISTS = "SELECT section_title FROM section_master " +
		" WHERE upper(section_title)=upper(?) and section_id!=?";
	public boolean exists(Connection con, String sectionName, Integer sectionId) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(FORM_NAME_EXISTS);
			ps.setString(1, sectionName);
			ps.setInt(2, sectionId);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
		} finally {
			DataBaseUtil.closeConnections(null, ps, rs);
		}
		return false;
	}

	public List getSectionsList(String formType) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			String where = "";
			if (formType.equals("Form_OT")) {
				where = " AND linked_to IN ('order item') ";
			} else if (formType.equals("Form_IP") || formType.equals("Form_Gen")) {
				where = " AND linked_to IN ('patient', 'visit', 'form')";
			} else if (formType.equals("Form_Serv")) {
				where = " AND linked_to IN ('patient', 'visit', 'order item') ";
			}
			ps = con.prepareStatement("select * from section_master sm where status='A' "+where);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_FORM_FIELD_OPTIONS =
		" SELECT o.field_id, o.option_id, o.pattern_id, o.display_order, o.option_value, " +
		"	o.phrase_category_id as option_phrase_category_id, o.value_code " +
		" FROM section_field_options o " +
		"   JOIN section_field_desc field USING (field_id) " +
		"   JOIN section_master section USING (section_id) " +
		" WHERE section_id=? and section.status='A' and field.status='A' and o.status='A' " +
		" ORDER BY o.display_order ";

	public List<BasicDynaBean> getSectionFieldOptions(int sectionId) throws java.sql.SQLException {
		return DataBaseUtil.queryToDynaList(GET_FORM_FIELD_OPTIONS, sectionId);
	}

	public static List getAddedSectionMasterDetails(String mrNo, String patientId, int itemId,
			int genericFormId, int formId, String itemType) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
					" SELECT sm.section_title, psd.section_detail_id, psf.form_id, display_order, psd.section_id, " +
					"	psd.finalized, psd.finalized_user, usr.temp_username " +
					" FROM section_master sm " +
					"	JOIN patient_section_details psd ON (psd.section_id=sm.section_id)" +
					"	JOIN patient_section_forms psf ON (psd.section_detail_id=psf.section_detail_id) " +
					"	LEFT JOIN u_user usr ON (psd.finalized_user=usr.emp_username)" +
					" WHERE psd.mr_no=? AND psd.patient_id=? AND coalesce(psd.section_item_id, 0)=?" +
					"	AND coalesce(psd.generic_form_id, 0)=? AND psf.form_id=? AND psd.item_type=?" +
					" ORDER BY display_order, psd.section_detail_id");
			ps.setString(1, mrNo);
			ps.setString(2, patientId);
			ps.setInt(3, itemId);
			ps.setInt(4, genericFormId);
			ps.setInt(5, formId);
			ps.setString(6, itemType);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/*
	 * is this generate_series is used to get the forms in the order it is saved.
	 * ex: if the forms column contains id's like 20,15,25,13 then we should retrieve the form details in the
	 * same order. to get this we use the generate_series method.
	 */
	private static final String SECTION_FOR_ID =
		" SELECT CASE WHEN section.section_id = '-1' THEN 'Complaint (Sys)' WHEN section.section_id = '-2' THEN " +
		"	'Allergies (Sys)' WHEN section.section_id = '-3' THEN 'Triage Summary (Sys)' WHEN " +
		"	section.section_id = '-4' THEN 'Vitals (Sys)' WHEN section.section_id='-5' THEN 'Consultation Notes (Sys)' " +
		"	WHEN section.section_id = '-6' THEN 'Diagnosis Details (Sys)' WHEN section.section_id = '-7' THEN 'Prescriptions (Sys)' " +
		"	WHEN section.section_id = '-8' THEN 'Standing Prescriptions (Sys)' WHEN section.section_id = '-15' THEN 'Health Maintenance (Sys)' " +
		"	WHEN section.section_id = '-16' THEN 'Pre Anaesthesthetic Checkup (Sys)' " +
		"	WHEN section.section_id = '-13' THEN 'Obstetric History (Sys)' " +
		"	WHEN section.section_id = '-14' THEN 'Antenatal (Sys)' " +
		"	ELSE sm.section_title END as section_title, " +
		"	section.section_id, sm.status, sm.linked_to, display_order FROM " +
		"	(SELECT trim(regexp_split_to_table(sections, E',')) as section_id, id, " +
		"			generate_series(1, array_upper(regexp_split_to_array(sections, E','), 1)) as display_order " +
		"		  FROM form_components) as section " +
		"	LEFT JOIN section_master sm ON (section.section_id=sm.section_id::text) " +
		" WHERE section.id=? " +
		" ORDER BY display_order";

	public static List getSections(int sectionId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(SECTION_FOR_ID);
			ps.setInt(1, sectionId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public List<BasicDynaBean> getSections(String sections) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT * FROM section_master where section_id in (" + sections + ")");
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}

