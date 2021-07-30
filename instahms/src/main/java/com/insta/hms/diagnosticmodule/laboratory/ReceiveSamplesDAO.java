package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
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

public class ReceiveSamplesDAO extends GenericDAO {

	public ReceiveSamplesDAO(){
		super("sample_collection");
	}

	private static Logger logger = LoggerFactory.getLogger(ReceiveSamplesDAO.class);
	private static final String RECEIVE_SAMPLES_FILEDS =
		" SELECT mr_no, patient_name, center_id, pat_id, sample_no, current_sample_no, sample_collection_id, "+ 
		" sample_type, sample_type_id, transfer_time, aliquot_parent_sample_no, " +
		" sample_date, sample_receive_status, collection_center, sample_transfer_status, receipt_other_details, sample_split_status, " +
		" receipt_time, transfer_batch_id, source_center_name, curr_sample_transfer_status, patient_age, age_unit, patient_gender, " +
		" string_agg(COALESCE(test_name,''), '|') as test_name, " +
		" string_agg(COALESCE(test_id,''), ', ' ) as test_id, " +
		" string_agg(COALESCE(ddept_id,''), ', ' ) as ddept_id, " +
		" string_agg(COALESCE(ddept_name,''), ', ' ) as ddept_name, patient_sponsor_type, " +
		" string_agg(COALESCE(prescribed_id::text, ''), ', ' ) as prescribed_id ";
	private static final String RECEIVE_SAMPLES_FROM = " FROM (SELECT tp.mr_no as mr_no, "
			+ "COALESCE(isr.patient_name, get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)) as patient_name, "
			+ "get_patient_age(pd.dateofbirth,pd.expected_dob,isr.isr_dateofbirth, isr.patient_age) as patient_age, "
			+" get_patient_age_in(pd.dateofbirth,pd.expected_dob,isr.isr_dateofbirth, isr.age_unit) as age_unit,"
			+ "COALESCE(isr.patient_gender, pd.patient_gender) as patient_gender, "
			+ "COALESCE(pr.center_id,isr.center_id) as center_id, "
			+ "dd.ddept_id, dd.ddept_name, tp.pat_id, COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_no, "
			+ "sc.sample_split_status as sample_split_status, sc.sample_sno as current_sample_no, sc.aliquot_parent_sample_no, "
			+ "tp.sample_collection_id, st.sample_type, st.sample_type_id,sc.sample_date as sample_date, "
			+ "COALESCE(sc.receipt_time, sc.sample_date) as receive_filter_time, " // when filter is marked as received, this field is used for sorting samples.
			+ "sc.sample_receive_status, d.test_name , d.test_id, hcmcol.center_name AS collection_center, "
			+ "scsrc.sample_transfer_status, sc.sample_transfer_status as curr_sample_transfer_status, "
			+ "scsrc.transfer_time, sc.receipt_other_details, sc.receipt_time, scsrc.transfer_batch_id, "
			+ "case when coalesce(itpa.sponsor_type, 'R')='R' then 'R' else 'S' end as patient_sponsor_type, "
			+ "tp.prescribed_id, COALESCE(hcm.center_name, hcmsrc.center_name) as source_center_name, "
			+ "CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type "
			+ "FROM tests_prescribed tp "
			+ "LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " // details related to incoming patient
			+ "LEFT JOIN patient_details pd ON (pd.mr_no = tp.mr_no) " // details related to in-chain patient
			+ "LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) "
			+ "LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) "
			+ "LEFT JOIN tests_prescribed tpsrc ON (tpsrc.prescribed_id = tp.source_test_prescribed_id) "
			+ "JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id AND "
			+ "((sc.outsource_dest_id IS NULL) OR (sc.sample_split_status = 'D'))) "
			+ "LEFT JOIN sample_collection scsrc ON (scsrc.sample_collection_id =  tpsrc.sample_collection_id) "
			+ "JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) "
			+ "JOIN diagnostics d ON (d.test_id = tp.test_id) "
			+ "LEFT JOIN tests_prescribed tpcol on (tpcol.prescribed_id = tp.coll_prescribed_id) "
			+ "LEFT JOIN patient_registration prcol ON (tpcol.pat_id = prcol.patient_id) "
			+ "LEFT JOIN tpa_master itpa ON (itpa.tpa_id = pr.primary_sponsor_id) "
			+ "LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id) "
			+ "LEFT JOIN hospital_center_master hcmsrc ON (hcmsrc.center_id = isr.source_center_id) "
			+ "LEFT JOIN hospital_center_master hcmcol ON (hcmcol.center_id = prcol.center_id)"
			+ "JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id) "
			+ "WHERE sc.aliquot_parent_sample_no IS NULL AND tp.conducted NOT IN ('X','RAS') "
			+ "AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )) as foo";

	private static final String COUNT = " SELECT count(distinct(sample_no)) ";

	private static final String GROUP_BY =
		" mr_no, patient_name, center_id, pat_id, sample_no, sample_collection_id, sample_type, sample_type_id, transfer_time, receipt_time, " +
		" sample_date, sample_receive_status, collection_center, sample_transfer_status, " +
		" receipt_other_details, transfer_batch_id, patient_sponsor_type, source_center_name, aliquot_parent_sample_no, current_sample_no "
		+ ", sample_split_status, curr_sample_transfer_status, receive_filter_time, patient_age, age_unit, patient_gender ";

	public List getReceiveSamplesBySample(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		String sampleNos = null;
		String[] sampleNosArray = null;

		if (params.get("_sample_no") != null && !((String[]) params.get("_sample_no"))[0].equals("")) {
			sampleNos = ((String[])params.get("_sample_no"))[0];
			sampleNosArray = sampleNos.split(",");
		}
		String transferBatchId = ConversionUtils.getParamValue(params, "transfer_batch_id", "");
		listingParams.put(LISTING.PAGESIZE, 0);
		try {
			if ( (sampleNosArray == null || sampleNosArray.length == 0) && transferBatchId.equals("")) return null;

			qb = new SearchQueryBuilder(con, RECEIVE_SAMPLES_FILEDS, COUNT, RECEIVE_SAMPLES_FROM, null, GROUP_BY, listingParams);
			if (sampleNos != null && !sampleNos.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "sample_no", "in", Arrays.asList(sampleNosArray));

			qb.addFilter(SearchQueryBuilder.STRING, "sample_receive_status", "=", "P");
			qb.addFilter(SearchQueryBuilder.STRING, "transfer_batch_id", "=", transferBatchId);
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

	public PagedList getReceiveSamplesList(Map params, Map<LISTING, Object> listingParams) throws SQLException,
	ParseException {
		Connection con = DataBaseUtil.getConnection(true);
		SearchQueryBuilder qb = null;
		String sampleNos = null;
		String[] sampleNosArray = null;
		if(params.get("_sample_no") != null) {
			sampleNos = ((String[])params.get("_sample_no"))[0];
			sampleNosArray = sampleNos.split(",");
			for (int i = 0; i < sampleNosArray.length; i++) {
				sampleNosArray[i] = sampleNosArray[i].trim();
			}
		}

		try {
			qb = new SearchQueryBuilder(con, RECEIVE_SAMPLES_FILEDS, COUNT, RECEIVE_SAMPLES_FROM, null, GROUP_BY, listingParams);
			qb.addFilterFromParamMap(params);
			if(sampleNos != null && !sampleNos.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "sample_no", "in", Arrays.asList(sampleNosArray));
			qb.addFilter(SearchQueryBuilder.INTEGER,"sample_type_id", "IN",
					ConversionUtils.getParamAsListInteger(params, "_sample_type_id"));
			// Sorting by receive_time desc when filtered for received samples, else sort by sample_date.
			String[] sampleReceiveStatusFilter = (String[]) params.get("sample_receive_status");
			if (sampleReceiveStatusFilter != null && sampleReceiveStatusFilter.length > 0 
					&& sampleReceiveStatusFilter[0].equals("R")) {
				qb.addSecondarySort("receive_filter_time", true);
			} else {
				qb.addSecondarySort("sample_date");
			}
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

//	 limit is applied, to get the single row, when single sample no is generated for the multiple tests.
	private static final String GET_RECEIVE_SAMPLE_DETAILS = " SELECT COALESCE(sccol.sample_date, sc.sample_date) as sample_date, "+
								" sc.coll_sample_no as sample_sno, " +
								" sc.sample_qty, st.sample_type, hcm.center_name, sccol.transfer_batch_id, " +
								" hcmsrc.center_name as source_center_name " +
								" FROM sample_collection sc " +
								" JOIN tests_prescribed tp on (sc.sample_collection_id=tp.sample_collection_id) " +
								" JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) " +
								" LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) " +
								" LEFT JOIN sample_collection sccol ON (sccol.sample_collection_id=tpcol.sample_collection_id) " +
								" LEFT JOIN patient_registration pr ON (pr.patient_id = tpcol.pat_id) " +
								" LEFT JOIN hospital_center_master hcm ON (hcm.center_id = pr.center_id) " +
								" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
								" LEFT JOIN hospital_center_master hcmsrc ON (hcmsrc.center_id = COALESCE(isr.source_center_id, pr.center_id)) " +															
								" WHERE sc.sample_collection_id = ? limit 1";

	public BasicDynaBean getReceiveSampleDetails(int sampleCollectionID)throws SQLException{

		BasicDynaBean receiveSampleDetails = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_RECEIVE_SAMPLE_DETAILS);
			ps.setInt(1, sampleCollectionID);
			receiveSampleDetails = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return receiveSampleDetails;
	}

	private static final String GET_INCOMING_SAMPLE_REGISTRATION_DETAILS = " SELECT isr.patient_name, get_patient_age(null,null,isr.isr_dateofbirth,isr.patient_age) as patient_age, isr.patient_gender, " +
			"isr.mr_no, isr.incoming_visit_id, get_patient_age_in(null,null,isr.isr_dateofbirth, isr.age_unit) as age_unit " +
			" FROM incoming_sample_registration isr " +
			" WHERE isr.incoming_visit_id = ? ";

	public BasicDynaBean getIncomingSampleRegistrationDetails(String incomingVisitId)throws SQLException{
		BasicDynaBean incomingSampleRegistrationDetails = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_INCOMING_SAMPLE_REGISTRATION_DETAILS);
			ps.setString(1, incomingVisitId);
			incomingSampleRegistrationDetails = DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return incomingSampleRegistrationDetails;
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
	
	private static final String GET_TEST_CENTER_ASSOCIATION_SELECT_FIELDS = "SELECT dod.test_id as test_id, count(1)::text || ', ' || "
			+ "TEXTCAT_COMMACAT(COALESCE(hcm.center_name, om.oh_name)) as out_centers, "
			+ "TEXTCAT_COMMACAT(dod.outsource_dest_id::text) as outsource_dest_ids ";
	private static final String GET_TEST_CENTER_ASSOCIATION_FROM_FIELDS = " FROM diag_outsource_detail dod "
			+ "JOIN diag_outsource_master dom ON (dod.outsource_dest_id = dom.outsource_dest_id) "
			+ "LEFT JOIN hospital_center_master AS hcm ON (dom.outsource_dest = hcm.center_id::text) "
			+ "LEFT JOIN outhouse_master om ON (dom.outsource_dest = om.oh_id) ";
	private static final String GET_TEST_CENTER_ASSOCIATION_WHERE_FIELDS = " WHERE dod.source_center_id = ? AND dom.status = 'A'"
			+ " AND dod.status = 'A' ";
	private static final String GET_TEST_CENTER_ASSOCIATION_GROUP_BY = " GROUP BY dod.test_id";
	
	public static List getTestCenterAssociation(int centerID, List<String> testIDs) throws SQLException {
		if (testIDs == null || testIDs.size() == 0) {
			return null;
		}
		StringBuilder query = new StringBuilder();
		query.append(GET_TEST_CENTER_ASSOCIATION_SELECT_FIELDS);
		query.append(GET_TEST_CENTER_ASSOCIATION_FROM_FIELDS);
		query.append(GET_TEST_CENTER_ASSOCIATION_WHERE_FIELDS);
		DataBaseUtil.addWhereFieldInList(query, "dod.test_id", testIDs, true);
		query.append(GET_TEST_CENTER_ASSOCIATION_GROUP_BY);
		String testCenterAssociationQuery = query.toString();
		Connection con = DataBaseUtil.getConnection();
		List testCenterAssociation = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(testCenterAssociationQuery);
			ps.setInt(1, centerID);
			for (int i = 0; i < testIDs.size(); i++) {
				ps.setString(i + 2, testIDs.get(i));
			}
			testCenterAssociation = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		return testCenterAssociation;
	}
	
	private static final String GET_ALL_CHILD_SAMPLES_SELECT_FIELDS = "SELECT sc.sample_sno as child_sample_no, sc.sample_collection_id, "
			+ " aliquot_parent_sample_no as parent_sample_no, "
			+ " TEXTCAT_COMMACAT(COALESCE(tp.test_id,'') ) as test_id, "
			+ " string_agg(COALESCE(d.test_name,''), '|') as test_name, "
			+ " TEXTCAT_COMMACAT(COALESCE(dd.ddept_name,'') ) as dept_name ";
	
	private static final String GET_ALL_CHILD_SAMPLES_FROM_FIELDS = "FROM sample_collection sc"
			+ " LEFT JOIN tests_prescribed tp ON (tp.sample_no = sc.sample_sno) "
			+ " JOIN diagnostics d ON (d.test_id = tp.test_id) "
			+ " JOIN diagnostics_departments dd ON (d.ddept_id = dd.ddept_id) ";
	
	private static final String GET_ALL_CHILD_SAMPLES_GROUP_BY = " GROUP BY child_sample_no, sc.sample_collection_id, aliquot_parent_sample_no ";
	 
	public static List getChildSamples(List<String> sampleNo) throws SQLException {
		if (sampleNo == null || sampleNo.size() == 0) {
			return null;
		}
		StringBuilder query = new StringBuilder();
		query.append(GET_ALL_CHILD_SAMPLES_SELECT_FIELDS);
		query.append(GET_ALL_CHILD_SAMPLES_FROM_FIELDS);
		DataBaseUtil.addWhereFieldInList(query, "aliquot_parent_sample_no", sampleNo, false);
		query.append(GET_ALL_CHILD_SAMPLES_GROUP_BY);
		String getChildSamplesQuery = query.toString();
		Connection con = DataBaseUtil.getConnection();
		List childSamples = null;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(getChildSamplesQuery);
			for (int i = 0; i < sampleNo.size(); i++) {
				ps.setString(i + 1, sampleNo.get(i));
			}
			childSamples = DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		
		return childSamples;
	}
}
