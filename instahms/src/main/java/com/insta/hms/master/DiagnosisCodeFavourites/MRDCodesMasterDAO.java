package com.insta.hms.master.DiagnosisCodeFavourites;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Anil N
 *
 */
public class MRDCodesMasterDAO extends GenericDAO{

	public MRDCodesMasterDAO() {
		super("mrd_codes_doctor_master");
	}

	static Logger logger = LoggerFactory.getLogger(MRDCodesMasterDAO.class);
	private static String GET_SELECTED_CODES_LIST = "SELECT code,code_type FROM mrd_codes_doctor_master" +
			" WHERE doctor_id = ? AND code_type = ?";
	public static List<BasicDynaBean> getSelectedCodesList(String doctorId, String codeType)
			throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_SELECTED_CODES_LIST);
			ps.setString(1, doctorId);
			ps.setString(2, codeType);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static boolean exists(String doctorId, String codeType) throws SQLException{

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_SELECTED_CODES_LIST);
			ps.setString(1, doctorId);
			ps.setString(2, codeType);
			rs = ps.executeQuery();
			while(rs.next()){
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

	private static String GET_MRD_CODES_LIST1 = "FROM mrd_codes_master m JOIN mrd_supported_codes s ON m.code_type = s.code_type";
	private static String SEARCH_MRD_CODES = "SELECT *";
	private static String MRD_CODES_COUNT = "SELECT count(*)";
	public PagedList getMRDCodesMasterList(Map requestParams, Map<LISTING, Object> pagingparam, String codeType, String code) throws Exception{

		String where = " WHERE s.code_category = "+"'Diagnosis'"+" AND s.code_type = "+"'"+codeType+"'"+" AND m.status="+"'A'";
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, SEARCH_MRD_CODES,MRD_CODES_COUNT, GET_MRD_CODES_LIST1, where, pagingparam);
			if(code != null && !"".equals(code)){
				qb.appendToQuery("m.code ilike "+"'"+code+"%' OR m.code_desc ilike "+"'"+code+"%' " +
						"OR m.code_desc ilike "+"'%"+code+"%' OR m.code_desc ilike "+"'%"+code+"'");
			}
			qb.addSecondarySort("code");
			qb.build();
			return qb.getDynaPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}

	}

	private static String GET_MRD_CODES_FAV_LIST = "FROM mrd_codes_doctor_master d JOIN mrd_codes_master m ON d.code = m.code AND " +
			                          "d.code_type = m.code_type";
	private static String SEARCH_MRD_CODE = "SELECT * ";
	private static String MRD_CODES_COUNTS = "SELECT count(*)";
    public PagedList getSelectedFavourites(Map requestParams, Map<LISTING, Object> pagingparam,String doctorId, String codeType, String code)
	throws SQLException {

    	String where = " WHERE d.code_type = "+"'"+codeType+"'"+" AND d.doctor_id= "+"'"+doctorId+"'"+" AND m.status="+"'A'";
		Connection con = null;
		SearchQueryBuilder qb = null;
		try {
			con = DataBaseUtil.getConnection();
			qb = new SearchQueryBuilder(con, SEARCH_MRD_CODE,MRD_CODES_COUNTS, GET_MRD_CODES_FAV_LIST, where, pagingparam);
			if(code != null && !"".equals(code)){
				qb.appendToQuery("m.code ilike "+"'"+code+"%' OR m.code_desc ilike "+"'"+code+"%'" +
						"OR m.code_desc ilike "+"'%"+code+"%' OR m.code_desc ilike "+"'%"+code+"'");
			}
			qb.addSecondarySort("d.code");
			qb.build();
			return qb.getDynaPagedList();
		} finally {
			qb.close();
			DataBaseUtil.closeConnections(con, null);
		}
	}

    private static String GET_CODE_FAV_LIST = "SELECT m.code_doc_id, m.code_type, m.code, d.doctor_name FROM mrd_codes_doctor_master m JOIN doctors d ON m.doctor_id=d.doctor_id " +
    	                                    	" WHERE m.doctor_id =? ORDER BY code_type,code";
    public List<BasicDynaBean> getICDFavourites(String doctorId) throws SQLException {

    	Connection con = null;
    	PreparedStatement ps = null;
    	try {
    		con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_CODE_FAV_LIST);
			ps.setString(1, doctorId);
			return DataBaseUtil.queryToDynaList(ps);
    	}
    	finally {
    		DataBaseUtil.closeConnections(con, ps);
    		}
    }

    private static String  GET_ALL_AVAILABLE_CODE = "SELECT * FROM mrd_codes_master m JOIN mrd_supported_codes s ON m.code_type = s.code_type WHERE s.code_category='Diagnosis'" +
    		                                    " and m.status='A'";
    public static List<BasicDynaBean> getAllAvlbeCodes() throws SQLException {

    	Connection con = null;
    	PreparedStatement ps = null;
    	try {
    		con = DataBaseUtil.getConnection();
    		con.setAutoCommit(false);
    		ps = con.prepareStatement(GET_ALL_AVAILABLE_CODE);
    		return DataBaseUtil.queryToDynaList(ps);
    		}
    	finally {
    		DataBaseUtil.closeConnections(con, ps);
    	}
    }

    public static String DIGNOSIS_CODE_TYPES = "SELECT code_type from mrd_supported_codes WHERE code_category = ?";
    public List<String> getCodeTypes()throws SQLException {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DataBaseUtil.getReadOnlyConnection();
            con.setAutoCommit(false);
            pstmt = con.prepareStatement(DIGNOSIS_CODE_TYPES);
            pstmt.setString(1, "Diagnosis");
            return DataBaseUtil.queryToStringList(pstmt);
        } finally {
            DataBaseUtil.closeConnections(con, pstmt);
        }
     }

    private static String INSERT_ICD_FAV = "INSERT INTO mrd_codes_doctor_master(code_type, code, doctor_id ) values (? ,? ,?) ";
    public boolean insertICDFavourites(List<BasicDynaBean> icdfavList) throws SQLException {

    	boolean success = false;
    	Connection con = null;
    	PreparedStatement stmt = null;
    	for(int i = 0; i<icdfavList.size(); i++ ){
    		try {
    			con = DataBaseUtil.getConnection();
    			stmt = con.prepareStatement(INSERT_ICD_FAV);
    			String codeType = (String) icdfavList.get(i).get("code_type");
    			String code = (String) icdfavList.get(i).get("code");
    			String doctorId = (String) icdfavList.get(i).get("doctor_id");
    			stmt.setString(1, codeType);
    			stmt.setString(2, code);
    			stmt.setString(3, doctorId);
    			int result = stmt.executeUpdate();
    			if(result > 0) {
    				success = true;
    			}
    		}
    		catch(Exception e){
    			logger.error("Not saved code successfully");
    		}
    		finally {
    			DataBaseUtil.closeConnections(con, stmt);
    		}
    	}
    	return success;
    }


    private static String GET_DOC_FAV_CODE = "SELECT code FROM mrd_codes_doctor_master" +
    " WHERE doctor_id = ? AND code_type = ?";

    public static List getDoctorFavouriteCodesList(String doctorId, String codeType)
    throws SQLException {

    	Connection con = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	List<String> codeList = new ArrayList<String>();
    	try {
    		con = DataBaseUtil.getConnection();
    		con.setAutoCommit(false);
    		ps = con.prepareStatement(GET_DOC_FAV_CODE);
    		ps.setString(1, doctorId);
    		ps.setString(2, codeType);
    		rs = ps.executeQuery();
    		while(rs.next()){
    			codeList.add((String)rs.getString("code"));
    			}
    		}
    	catch(Exception e){
    		logger.error("", e);
    		}
    	finally {
    		DataBaseUtil.closeConnections(con, ps, rs);
    		}
    	return codeList;
    }

    private static String GET_EXISTS_CODE = "SELECT code,code_type FROM mrd_codes_doctor_master" +
	" WHERE doctor_id = ? AND code_type = ? AND code = ?";
    public static boolean existsCode(String codeType, String code, String doctorId) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(GET_EXISTS_CODE);
			ps.setString(1, doctorId);
			ps.setString(2, codeType);
			ps.setString(3, code);
			rs = ps.executeQuery();
			while(rs.next()){
				return true;
			}
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return false;
	}

    private static final String CODE_SEARCH = "SELECT * FROM mrd_codes_master m JOIN mrd_supported_codes s ON m.code_type = s.code_type " +
    	                                      "WHERE s.code_category = 'Diagnosis' AND s.code_type = ? AND m.status= 'A' order by m.code";
    public static List getAllCodeTypesCodes(String codeType) throws SQLException {
    	Connection con = null;
		PreparedStatement ps = null;
		List codesList = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CODE_SEARCH);
			ps.setString(1, codeType);
			codesList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			logger.debug(e.toString());
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return codesList;
	}

    private static String GET_DOC_FAV_CODE_SEARCH = "SELECT * FROM mrd_codes_doctor_master d JOIN mrd_codes_master m ON d.code = m.code AND " +
                                                    "d.code_type = m.code_type WHERE d.code_type =? AND d.doctor_id= ? AND m.status= 'A' order by m.code";
    public static List getAllFavDocSearchCodes(String doctorId, String codeType) throws SQLException {
    	Connection con = null;
		PreparedStatement ps = null;
		List favcodesList = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DOC_FAV_CODE_SEARCH);
			ps.setString(1, codeType);
			ps.setString(2, doctorId);
			favcodesList = DataBaseUtil.queryToDynaList(ps);

		} catch (SQLException e) {
			logger.debug(e.toString());
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return favcodesList;
	}
}