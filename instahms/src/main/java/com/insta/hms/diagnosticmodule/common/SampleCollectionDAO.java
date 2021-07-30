package com.insta.hms.diagnosticmodule.common;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SampleCollectionDAO extends GenericDAO{

	static Logger logger = LoggerFactory.getLogger(SampleCollectionDAO.class);
	public SampleCollectionDAO(){
		super("sample_collection");
	}

	private static final String SAMPLE_DETAILS =
		" SELECT sc.user_name, COALESCE(csc.sample_date, sc.sample_date) AS sample_date, COALESCE(csc.sample_sno, sc.sample_sno) AS sample_sno, sc.sample_qty, " +
				"st.sample_type, st.sample_container, sc.mr_no  " +
		" FROM sample_Collection sc" +
		" LEFT JOIN sample_collection csc ON (csc.sample_sno = sc.coll_sample_no)" +
		" JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) ";

	public List<BasicDynaBean> getSampleCollectionDetails(String commaSeperatedSampleNos, String visitId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		
		String[] sampleNos = commaSeperatedSampleNos.replace("\'", "").split(",");
		
		if (sampleNos.length < 1) {
		  return Collections.emptyList();
		}

		try{
			con = DataBaseUtil.getConnection();
			StringBuilder query = new StringBuilder(SAMPLE_DETAILS);
			query.append("WHERE sc.sample_sno IN (");
			for (int count = 0;count<sampleNos.length;count++) {
			  if (count == 0) {
			    query.append("?");
			  } else {
			    query.append(",?");
			  }
			}
			query.append(") AND sc.patient_id = ? ");
			ps = con.prepareStatement(query.toString());
			
			int count = 1;
			for (String sNo : sampleNos) {
			  ps.setString(count, sNo);
			  count++;
			}
			ps.setString(count, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	/**
	 * Returns comma seperated labnos of given samplid as string.
	 * @param sampleId
	 * @return
	 * @throws SQLException
	 */
	private static final String SAMPLE_DETAILS_PAPER_PRINT =
		" select tp.mr_no,sc.patient_id,d.test_name,st.sample_type,tp.sflag, " +
		" COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_sno," +
		" ih.hospital_name,tp.prescribed_id, sc.sample_date " +
		" from sample_collection  sc " +
		" join tests_prescribed tp on tp.sample_collection_id=sc.sample_collection_id " +
		" join diagnostics d on d.test_id=tp.test_id " +
		" left join  sample_type st ON (st.sample_type_id = sc.sample_type_id) " +
		" left join  incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
		" left join  incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name) " +
		" where sc.patient_id= ?  and sc.sample_sno in (#) " +
		" group by sc.sample_sno,tp.mr_no,sc.patient_id,d.test_name,st.sample_type,ih.hospital_name," +
		" tp.sflag,tp.prescribed_id, sc.sample_date, sc.coll_sample_no " +
		" order by tp.prescribed_id";
	public List<BasicDynaBean> getSampleCollectionPaperPrintDetails(String visitId,String commaSeperatedSampleNos )
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> sampleDetails = null;

		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SAMPLE_DETAILS_PAPER_PRINT.replace("#", commaSeperatedSampleNos));
			ps.setString(1, visitId);
			sampleDetails =  DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return sampleDetails;
	}


	public static String getConcatenatedLabIds(String sampleId)
	throws SQLException{
		return DataBaseUtil.getStringValueFromDb(
				"select textcat_commacat(DISTINCT labno) from tests_prescribed where sample_no =?",sampleId);
	}

	/**
	 * Returns comma seperated order date of given samplid as string.
	 * @param sampleId
	 * @return
	 * @throws SQLException
	 */
	public static String getConcatenatedOrderDate(String sampleId)
	throws SQLException{
		return DataBaseUtil.getStringValueFromDb(
				"select textcat_commacat(DISTINCT  to_char(pres_date,'dd-mm-yyyy HH:mi') ) from tests_prescribed where sample_no =?",sampleId);
	}

	public static Map testDetailsOfSample(String sampleNo) throws SQLException {
		Connection con = null;
		PreparedStatement  ps = null;
		BasicDynaBean detBean = null;

		try{
				con = DataBaseUtil.getConnection();
				ps = con.prepareStatement(
						"  SELECT textcat_commacat(test_name) as test_name," +
						"    textcat_commacat(conduction_instructions) as conduction_instructions," +
						"    textcat_commacat(diag_code) as alias " +
						"   FROM diagnostics " +
						"  JOIN tests_prescribed USING(test_id) " +
						"  JOIN sample_collection ON(sample_sno = sample_no) " +
						"WHERE sample_no = ?");
				ps.setString(1, sampleNo);
				detBean = DataBaseUtil.queryToDynaBean(ps);
				return ( detBean != null ? detBean.getMap() : null );
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_SAMPLE_AT_CENTER = "SELECT sc.sample_collection_id, sc.sample_receive_status, "
			+ "sc.coll_sample_no, sc.sample_split_status "
			+ "FROM sample_collection sc "
			+ "JOIN tests_prescribed tp ON (tp.sample_collection_id = sc.sample_collection_id) "
			+ "LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) "
			+ "LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) "
			+ "WHERE COALESCE(isr.center_id, pr.center_id) = ? AND COALESCE(sc.coll_sample_no, sc.sample_sno) = ?";
		
	public static BasicDynaBean getSampleAtCenter(String collSampleNo, int centerID) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		BasicDynaBean sample = null;
		try {
			ps = con.prepareStatement(GET_SAMPLE_AT_CENTER);
			ps.setInt(1, centerID);
			ps.setString(2, collSampleNo);
			sample = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		
		return sample;
	}
}
