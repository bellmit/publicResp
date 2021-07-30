package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TransferSamplesDAO extends GenericDAO {

	public TransferSamplesDAO(){
		super("sample_collection");
	}

	static Logger logger = LoggerFactory.getLogger(TransferSamplesDAO.class);

	private static final String TRANSFER_SAMPLES_FILEDS = "SELECT mr_no, pat_id, sample_collection_id, sample_no, outsource_name, " +
								" center_id, sample_type, sample_type_id, sample_transfer_status,  transfer_user, transfer_time, " +
								" transfer_other_details, sample_date, patient_full_name, transfer_batch_id, sample_status, sample_split_status, " +
								" TEXTCAT_COMMACAT(COALESCE(test_id,'') ) as test_id, " +
								" TEXTCAT_COMMACAT(COALESCE(test_name,'') ) as test_name, " +
								" TEXTCAT_COMMACAT(COALESCE(sample_no,'') ) as sg_sample_no, " +
								" TEXTCAT_COMMACAT(COALESCE(sample_type_id::text,'') ) as sg_sample_type_id, " +
								" TEXTCAT_COMMACAT(COALESCE(prescribed_id::text,'') ) as prescribed_id, " +								
								" TEXTCAT_COMMACAT(COALESCE(outsource_dest_prescribed_id::text,'') ) as outsource_dest_prescribed_id, " +
								" TEXTCAT_COMMACAT(COALESCE(ddept_id,'') ) as ddept_id, " +
								" TEXTCAT_COMMACAT(COALESCE(ddept_name,'') ) as ddept_name, outsource_dest_id, source_center_name, visit_type, " +
								" collection_center ";

	private static final String TRANSFER_SAMPLES_FROM = " FROM (SELECT tp.mr_no, tp.pat_id, tp.sample_collection_id, " +
								" coalesce (sc.coll_sample_no, sc.sample_sno) as sample_no, sc.sample_split_status, d.test_name, tp.test_id, tp.prescribed_id, " +
								" tp.outsource_dest_prescribed_id, osn.outsource_name, coalesce (pr.center_id, isr.center_id) as center_id, " +
								" st.sample_type, st.sample_type_id, dd.ddept_id, dd.ddept_name, " +
								" sc.sample_transfer_status, sc.transfer_user, sc.transfer_time, sc.transfer_other_details, sc.sample_date, " +
								" COALESCE(get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name), isr.patient_name) " +
								" as patient_full_name, hcmcol.center_name AS collection_center, " +
								" sc.transfer_batch_id, tp.outsource_dest_id, hcmsrc.center_name as source_center_name, sc.sample_status, " +
								" CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type " +
								" FROM tests_prescribed tp " +
								//" LEFT JOIN tests_prescribed tp1 ON (tp.outsource_dest_prescribed_id = tp1.prescribed_id) " +
								" JOIN diagnostics d ON (d.test_id = tp.test_id) " +
								" LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
								" LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id) " +
								" LEFT JOIN outsource_names osn ON (osn.outsource_dest_id = dom.outsource_dest_id) " +
								" JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id) " +
								//" LEFT JOIN sample_collection sc1 ON (sc1.sample_collection_id = tp1.sample_collection_id) " +
								" JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) " +
								" JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id) " +
								" LEFT JOIN patient_details pd  ON (tp.mr_no=pd.mr_no) " +
								" LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) " +
								" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
								" LEFT JOIN hospital_center_master hcmsrc ON (hcmsrc.center_id = COALESCE(isr.source_center_id, pr.center_id)) " +
								// used to get collection center details from all the hops
								" LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) " +
								" LEFT JOIN patient_registration prcol on (prcol.patient_id = tpcol.pat_id) " +
								" LEFT JOIN hospital_center_master hcmcol ON (hcmcol.center_id = COALESCE(prcol.center_id, pr.center_id)) " +
								" WHERE tp.conducted != 'X' AND sc.sample_conduction_status = 'N' " +
								" AND sc.sample_receive_status = 'R' " +
								" AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no))) as foo";

	private static final String COUNT = " SELECT count(distinct(sample_no)) ";
	
	private static final String TRANSFER_SAMPLES_MANUAL = " FROM (SELECT tp.mr_no, tp.pat_id, tp.sample_collection_id, " +
								" coalesce (sc.coll_sample_no, sc.sample_sno) as sample_no, sc.sample_split_status, d.test_name, tp.test_id, tp.prescribed_id, " +
								" tp.outsource_dest_prescribed_id, osn.outsource_name, coalesce (pr.center_id, isr.center_id) as center_id, " +
								" st.sample_type, st.sample_type_id, dd.ddept_id, dd.ddept_name, " +
								" sc.sample_transfer_status, sc.transfer_user, sc.transfer_time, sc.transfer_other_details, sc.sample_date, " +
								" COALESCE(get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name), isr.patient_name) " +
								" as patient_full_name, hcmcol.center_name AS collection_center, " +
								" sc.transfer_batch_id, tp.outsource_dest_id, hcmsrc.center_name as source_center_name, sc.sample_status, " +
								" CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type " +
								" FROM tests_prescribed tp " +
								" JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id AND tp.conducted != 'X' #) " +
								" JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) " +
								" JOIN diagnostics d ON (d.test_id = tp.test_id) " +
								" JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id) " +
								" LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
								" LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id) " +
								" LEFT JOIN outsource_names osn ON (osn.outsource_dest_id = dom.outsource_dest_id) " +
								" LEFT JOIN patient_details pd  ON (tp.mr_no=pd.mr_no) " +
								" LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) " +
								" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
								" LEFT JOIN hospital_center_master hcmsrc ON (hcmsrc.center_id = COALESCE(isr.source_center_id, pr.center_id)) " +
								" LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) " +
								" LEFT JOIN patient_registration prcol on (prcol.patient_id = tpcol.pat_id) " +
								" LEFT JOIN hospital_center_master hcmcol ON (hcmcol.center_id = COALESCE(prcol.center_id, pr.center_id)) " +
								" WHERE sc.sample_conduction_status = 'N' AND sc.sample_receive_status = 'R' "+
								" AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )) as foo";

	private static final String GROUP_BY =" mr_no, pat_id, sample_collection_id, sample_no, outsource_name, center_id, sample_type, sample_type_id, " +
										  " sample_transfer_status,  transfer_user, transfer_time, transfer_other_details, sample_date, " +
										  "	patient_full_name, transfer_batch_id, outsource_dest_id, source_center_name, sample_status, visit_type, " +
										  " collection_center, sample_split_status ";

	public List getTransferSamplesBySampleNo(Map params, Map<LISTING, Object> listingParams)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		String sampleNos = null;
		String[] sampleNosArray = null;
		if (params.get("_sample_no") != null && !((String[]) params.get("_sample_no"))[0].equals("")) {
			sampleNos = ((String[])params.get("_sample_no"))[0];
			sampleNosArray = sampleNos.split(",");
		}


		listingParams.put(LISTING.PAGESIZE, 0);
		try {
			if (sampleNosArray == null || sampleNosArray.length == 0)
				return null;

			qb = new SearchQueryBuilder(con, TRANSFER_SAMPLES_FILEDS, COUNT, TRANSFER_SAMPLES_FROM, null, GROUP_BY, listingParams);
			qb.addFilter(SearchQueryBuilder.STRING, "sample_transfer_status", "=", "P");
			if (sampleNos != null && !sampleNos.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "sample_no", "in", Arrays.asList(sampleNosArray));

			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
				qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);

			qb.build();

			PreparedStatement psData = qb.getDataStatement();
			ResultSet rsData = psData.executeQuery();

			RowSetDynaClass rsd = new RowSetDynaClass(rsData);
			List dataList = rsd.getRows();
			rsData.close();
			psData.close();

			return dataList;
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	public PagedList getTransferSamplesList(Map params, Map<LISTING, Object> listingParams)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection(true);
		SearchQueryBuilder qb = null;
		String sampleNos = null;
		String[] sampleNosArray = null;
		if(params.get("_sample_no") != null) {
			sampleNos = ((String[])params.get("_sample_no"))[0];
			sampleNosArray = sampleNos.split(",");
		}

		try {
			String LOCAL_SAMPLE_TRANSFER_FROM = TRANSFER_SAMPLES_MANUAL;
			StringBuilder sampleDateFilter = new StringBuilder();
			if (null != params.get("sample_date") && ((String[])params.get("sample_date")).length > 0 && null != ((String[]) params.get("sample_date"))[0] 
					&& !((String[])params.get("sample_date"))[0].equals(""))
				sampleDateFilter.append(" AND sc.sample_date::date >= to_date('"+((String[])params.get("sample_date"))[0]+"', 'DD-MM-YYYY') ");
			if (null != params.get("sample_date") && ((String[])params.get("sample_date")).length > 1 && null != ((String[]) params.get("sample_date"))[1] 
					&& !((String[])params.get("sample_date"))[1].equals("")) {
				sampleDateFilter.append(" AND sc.sample_date::date <= to_date('"+((String[])params.get("sample_date"))[1]+"', 'DD-MM-YYYY') ");
			} else {
				sampleDateFilter.append(" AND sc.sample_date::date <= '"+DateUtil.getCurrentDate()+"'");
			}
			
			LOCAL_SAMPLE_TRANSFER_FROM = LOCAL_SAMPLE_TRANSFER_FROM.replace("#", sampleDateFilter.toString());
			
			qb = new SearchQueryBuilder(con, TRANSFER_SAMPLES_FILEDS, COUNT, LOCAL_SAMPLE_TRANSFER_FROM, null, GROUP_BY, listingParams);
			qb.addFilterFromParamMap(params);
			if(sampleNos != null && !sampleNos.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "sample_no", "in", Arrays.asList(sampleNosArray));
			qb.addFilter(SearchQueryBuilder.INTEGER,"sample_type_id", "IN",
					ConversionUtils.getParamAsListInteger(params, "_sample_type_id"));
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
			}
	}

	private static final String GET_INTERNAL_LAB_SAMPLE_DETAILS = "SELECT sc.sample_receive_status, sc.sample_collection_id, tp.prescribed_id " +
								" FROM tests_prescribed tp " +
								" JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id) " +
								" WHERE tp.prescribed_id = ?";

	public BasicDynaBean getInternalLabSamplesDetails(int outsourceDestPrescribedId)throws SQLException{

		BasicDynaBean internalLabsampleDetails = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_INTERNAL_LAB_SAMPLE_DETAILS);
			ps.setInt(1, outsourceDestPrescribedId);
			internalLabsampleDetails = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

		return internalLabsampleDetails;
	}

	private static final String GET_TRANSFER_SAMPLE_DETAILS = " SELECT sc.sample_date, "+
								" COALESCE(tpcol.sample_no, tp.sample_no) as sample_sno, " +
								" sc.sample_qty, st.sample_type, osn.outsource_name, COALESCE(tpcol.pat_id, tp.pat_id) as patient_id, " +
								" sc.transfer_batch_id, sc.outsource_dest_id, sc.sample_transfer_status, sc.sample_split_status, " +
								" TEXTCAT_COMMACAT(COALESCE(tp.test_id,'') ) as test_id, " +
								" TEXTCAT_COMMACAT(COALESCE(tp.prescribed_id::text,'') ) as prescribed_id, " +
								" TEXTCAT_COMMACAT(COALESCE(sc.sample_sno,'') ) as sg_sample_no, " +
								" TEXTCAT_COMMACAT(COALESCE(st.sample_type_id::text,'') ) as sg_sample_type_id, " +
								" hcmsrc.center_name as source_center_name, sc.sample_status, hcmcol.center_name AS collection_center " +
								" FROM sample_collection sc " +
								" JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) " +
								" LEFT JOIN outsource_names osn ON (osn.outsource_dest_id = sc.outsource_dest_id) " +
								" JOIN tests_prescribed tp ON (tp.sample_collection_id = sc.sample_collection_id) " +
								" LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) " +
								" LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
								" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
								" LEFT JOIN hospital_center_master hcmsrc ON (hcmsrc.center_id = COALESCE(isr.source_center_id, pr.center_id)) " +
								" LEFT JOIN patient_registration prcol on (prcol.patient_id = tpcol.pat_id) " +
								" LEFT JOIN hospital_center_master hcmcol ON (hcmcol.center_id = COALESCE(prcol.center_id, pr.center_id)) " +
								" WHERE sc.sample_collection_id = ? " +
								" GROUP BY sc.sample_date, sc.sample_sno,sc.sample_qty, st.sample_type, osn.outsource_name, " +
								" sc.transfer_batch_id, sc.outsource_dest_id, tpcol.pat_id, tp.pat_id, tpcol.sample_no, tp.sample_no, " +
								" hcmsrc.center_name, sc.sample_transfer_status, sc.sample_status, hcmcol.center_name, sc.sample_split_status ";

	public BasicDynaBean getTransferSampleDetails(int sampleCollectionID)throws SQLException{

		BasicDynaBean transferSampleDetails = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_TRANSFER_SAMPLE_DETAILS);
			ps.setInt(1, sampleCollectionID);
			transferSampleDetails = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return transferSampleDetails;
	}

	private static String GET_ALL_OUTSOURCE_NAMES = " SELECT distinct(osn.outsource_name) AS outsource_name, " +
			  " osn.outsource_dest_id " +
			  " FROM center_outsources co " +
			  " JOIN outsource_names osn ON (osn.outsource_dest = co.outsource_id) " +
			  " WHERE co.center_id = ? ORDER BY outsource_name";
	
	public static ArrayList getAllOutSource() throws SQLException{
		ArrayList outSources = new ArrayList();
		int centerId = RequestContext.getCenterId();
		Connection con =null;
		con = DataBaseUtil.getConnection();
		PreparedStatement ps = con.prepareStatement(GET_ALL_OUTSOURCE_NAMES);
		ps.setInt(1, centerId);
		outSources = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return outSources;
	}

	public static final String GET_DIAG_OUTSOURCE_DETAIL = " SELECT dom.outsource_dest_type, hcm.center_id, om.protocol "
			+ " FROM diag_outsource_master dom "
			+ " LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) "
			+ " LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest) "
			+ " WHERE dom.outsource_dest_id = ? " ;
	
	public static BasicDynaBean getDiagOutSourceDetails(int outSourceDestId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DIAG_OUTSOURCE_DETAIL);
			ps.setInt(1, outSourceDestId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public void removeSpaceFromArray(String[] array1) {
		if (array1 != null) {
			for (int i=0; i<array1.length; i++) {
				array1[i] = array1[i].trim();
			}				
		} 		
	}	

	private static String GET_ALL_COLLECTION_CENTERS = " SELECT distinct(center_name) AS collection_center " +
			  " FROM incoming_sample_registration isr " +
			  " JOIN hospital_center_master hcm ON (hcm.center_id = isr.source_center_id) " +
			  " WHERE isr.incoming_source_type = 'C' ";

	public static ArrayList getAllCollectionCenter() throws SQLException{
		ArrayList collectionCenter = new ArrayList();
		Connection con =null;
		con = DataBaseUtil.getConnection();
		PreparedStatement ps = con.prepareStatement(GET_ALL_COLLECTION_CENTERS);
		collectionCenter = DataBaseUtil.queryToArrayList(ps);
		ps.close();
		con.close();
		return collectionCenter;
	}
	
}
