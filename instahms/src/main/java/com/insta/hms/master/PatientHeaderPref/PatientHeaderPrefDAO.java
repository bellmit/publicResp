/**
 *
 */
package com.insta.hms.master.PatientHeaderPref;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Logger;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Anil N
 *
 */
public class PatientHeaderPrefDAO extends GenericDAO {

    public PatientHeaderPrefDAO(){
        super("patient_header_preferences");
    }

    private static final String PATIENT_FILEDS = "SELECT * ";
    private static final String PATIENT_FROM = " FROM patient_header_preferences";
    private static final String COUNT = " SELECT count(field_name) ";

    public PagedList getPatientHeaderDetails(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	    ParseException {
	    Connection con = null;
	    SearchQueryBuilder qb = null;
	    try {
	        con = DataBaseUtil.getConnection();
	        qb = new SearchQueryBuilder(con, PATIENT_FILEDS, COUNT, PATIENT_FROM, listingParams);
	        qb.addFilterFromParamMap(params);
	        qb.addSecondarySort("field_name");
	        qb.build();

	        return qb.getDynaPagedList();
	    } finally {
	        DataBaseUtil.closeConnections(con, null);
	        if (qb != null) qb.close();
	    }
    }

    public List getFields(String dataLevel, Boolean showClinical, String visitType) throws SQLException {
    	Connection con = DataBaseUtil.getConnection();
    	PreparedStatement ps = null;
    	try {
    		String query = "SELECT * FROM patient_header_preferences WHERE data_level=? AND data_category IN (?, 'Both') AND visit_type IN (?, 'b') " +
    				"	AND display='Y' ORDER BY data_level, display_order ";

    		ps = con.prepareStatement(query);
    		ps.setString(1, dataLevel);
    		ps.setString(2, showClinical ? "C" : "O");
    		ps.setString(3, visitType);
    		return DataBaseUtil.queryToDynaList(ps);
    	} finally {
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }

    public static final String GET_ALL_PATIENT_HEADER_PREF_VALUES = " SELECT * FROM patient_header_preferences ";

    public static List getAllPatientHeaderPrefFields() throws SQLException {
    	Connection con = null;
		PreparedStatement ps = null;
		List fieldsList = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_ALL_PATIENT_HEADER_PREF_VALUES);
			fieldsList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			Logger.log(e);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return fieldsList;
	}


}
