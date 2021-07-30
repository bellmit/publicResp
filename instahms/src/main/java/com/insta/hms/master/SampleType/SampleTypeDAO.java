package com.insta.hms.master.SampleType;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.ConversionUtils;
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
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleTypeDAO extends GenericDAO{

	 static Logger logger = LoggerFactory.getLogger(SampleTypeDAO.class);

	public SampleTypeDAO() {
		super("sample_type");

	}

	private static String GET_NEW_SAMPLE_NUMBER = "SELECT generate_next_sample_id(?, ?) ";
	public static String getNextSampleNumber(Integer sampleTypeId, Integer centerID) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try{
			String val = null;
			ps = con.prepareStatement(GET_NEW_SAMPLE_NUMBER);
			ps.setInt(1,sampleTypeId);
			ps.setInt(2, centerID);
			val = DataBaseUtil.getStringValueFromDb(ps);
			return val;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


  private static String GET_SAMPLE_TYPE_DETAILS="SELECT * FROM  sample_type";

  public static HashMap getSampleTypeDetails()	throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs=null;
		HashMap <String,Integer> specimanTypeHashMap= new HashMap <String,Integer>();
		con=DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_SAMPLE_TYPE_DETAILS);
		rs=ps.executeQuery();
		while(rs.next()){
			specimanTypeHashMap.put(rs.getString("sample_type"), rs.getInt("sample_type_id"));
		}
		DataBaseUtil.closeConnections(con, ps,rs);
		return specimanTypeHashMap;
	}

	private static final String SAMPLES_NAMESAND_iDS="select sample_type_id,sample_type from sample_type";

	   public static List getSamplesNamesAndIds() throws SQLException{
		   return ConversionUtils.copyListDynaBeansToMap(DataBaseUtil.queryToDynaList(SAMPLES_NAMESAND_iDS));
	}

	private static final String GET_BATCH_BASED_SAMPLE_NO = "SELECT generate_id(?)";

	public String getBatchBasedSampleNo(Connection con)throws SQLException {
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(GET_BATCH_BASED_SAMPLE_NO);
			pstmt.setString(1, "SAMPLE_ID");
			return DataBaseUtil.getStringValueFromDb(pstmt);
		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}
	}

	private static final String MAX_COUNTER = "SELECT MAX(sample_no_counter) FROM sample_collection WHERE patient_id = ?";

	public static int getMaxCounter(Connection con, String visitID)throws SQLException {
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(MAX_COUNTER);
			pstmt.setString(1, visitID);
			return DataBaseUtil.getIntValueFromDb(pstmt)+1;
		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}
	}

	private static final String GET_CENTERS = "SELECT * " +
			"FROM hospital_center_master hcm " +
			"WHERE hcm.center_id != 0 AND hcm.status = 'A' ORDER BY center_name";

	public static List<BasicDynaBean> getListOfCentersExcludingDefault()throws SQLException{

		return DataBaseUtil.queryToDynaList(GET_CENTERS);

	}

	private static String SAMPLE_FIELDS = "SELECT * " ;

	private static String SAMPLE_COUNT = "SELECT count(*)";

	private static String SAMPLE_TABLES = " FROM (SELECT s.sample_type_id, s.sample_type, s.status," +
			" sn.sample_prefix " +
			" FROM sample_type s" +
			" LEFT JOIN sample_type_number_prefs sn ON(s.sample_type_id = sn.sample_type_id) AND center_id = 0) AS foo";

	public PagedList search(Map map,Map pagingParams)throws SQLException, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			SearchQueryBuilder qb = new SearchQueryBuilder(con, SAMPLE_FIELDS, SAMPLE_COUNT ,
					SAMPLE_TABLES,pagingParams);

			qb.addFilterFromParamMap(map);
			qb.addSecondarySort("sample_type_id", false);
			qb.build();

			PagedList l = qb.getMappedPagedList();
			return l;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

}