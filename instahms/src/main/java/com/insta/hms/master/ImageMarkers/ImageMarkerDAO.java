/**
 *
 */
package com.insta.hms.master.ImageMarkers;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class ImageMarkerDAO extends GenericDAO {

	public ImageMarkerDAO() {
		super("image_markers");
	}

	private static final String SELECT_FIELDS = "SELECT label, status, image_id ";
	private static final String SELECT_COUNT = "SELECT count(*)";
	private static final String TABLE = " FROM image_markers ";
	public PagedList searchMarkers(Map params, Map listingParams) throws ParseException, SQLException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, SELECT_FIELDS, SELECT_COUNT, TABLE, listingParams);
			qb.addFilterFromParamMap(params);
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private static final String LABEL_EXISTS =
		"SELECT label FROM image_markers WHERE UPPER(label)=UPPER(?) AND image_id!=?";
	public boolean labelExists(Integer image_id, String label) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(LABEL_EXISTS);
			ps.setString(1, label);
			ps.setInt(2, image_id);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		return false;
	}

	private static final String GET_MARKERS = "SELECT label, status, image_id FROM image_markers ORDER BY label";
	public List getMarkers() throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_MARKERS);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_MARKERS_FOR_FIELD_ID =
		"SELECT im.label, im.status, im.image_id, marker_display_order FROM image_markers im " +
		"	JOIN (SELECT generate_series(1, array_upper(regexp_split_to_array(markers, E','), 1)) as marker_display_order, " +
		"				regexp_split_to_table(markers, E',')::integer as image_id FROM section_field_desc pffd " +
		"			WHERE pffd.field_id=?) as m ON (m.image_id=im.image_id) " +
		"ORDER BY marker_display_order";
	public List getMarkers(int fieldId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_MARKERS_FOR_FIELD_ID);
			ps.setInt(1, fieldId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_MARKERS_FOR_SECTION =
		" SELECT field_id, section_id, marker_id, field_type, label, field_display_order, marker_display_order FROM " +
		"	(SELECT sfd.display_order as field_display_order, sfd.section_id, sfd.field_type, sfd.field_id, " +
		"		regexp_split_to_table(markers, E'\\,')::integer as marker_id, " +
		"		generate_series(1, array_upper(regexp_split_to_array(markers, E','), 1)) as marker_display_order " +
		"		FROM section_field_desc sfd where section_id=? and field_type='image') as field_markers " +
		"	JOIN image_markers im ON (im.image_id=field_markers.marker_id) " +
		" ORDER BY field_display_order, marker_display_order";
	public List getMarkersForSection(int sectionId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_MARKERS_FOR_SECTION);
			ps.setInt(1, sectionId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	
	public static final String GET_MARKERS_FOR_FORM = 
			" SELECT field_id, section_id, marker_id, field_type, label, field_display_order, marker_display_order FROM " +
					"	(SELECT sfd.display_order as field_display_order, sfd.section_id, sfd.field_type, sfd.field_id, " +
					"		regexp_split_to_table(markers, E'\\,')::integer as marker_id, " +
					"		generate_series(1, array_upper(regexp_split_to_array(markers, E','), 1)) as marker_display_order " +
					"		FROM section_field_desc sfd where section_id in (#) and field_type='image') as field_markers " +
					"	JOIN image_markers im ON (im.image_id=field_markers.marker_id) " +
					" ORDER BY section_id, field_display_order, marker_display_order";
			
	public List getMarkers(String sections) throws SQLException {
		if (sections == null || sections.equals("")) return Collections.EMPTY_LIST;
		
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_MARKERS_FOR_FORM.replace("#", sections));
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

}
