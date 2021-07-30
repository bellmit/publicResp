package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class PendingSamplesDAO extends GenericDAO {
	public static Logger log = LoggerFactory.getLogger(PendingSamplesDAO.class);
	public PendingSamplesDAO(){
		super("sample_collection");
	}

	public static String TESTS_FIELDS  =
		" SELECT mr_no, pat_id, test_id, labno, test_name, conducted, sflag, prescribed_id, common_order_id," +
		" 	is_outhouse_selected, remarks, patient_full_name, patient_name, inc_patient_name, incoming_visit_id, " +
		" 	last_name, salutation, sample_needed, ddept_id, house_status, priority, bill_type, visit_type, " +
		"	payment_status, report_id, report_name, signed_off, report_data, report_date, re_conduction, " +
		"	charge_group, pres_date, ih_name, oh_name, charge_head, reg_date, results_entry_applicable, " +
		"   category,sample_type,sample_sno,sample_container,sample_status,sample_date, sample_type_status, " +
		"	outsource_dest_id, patient_sponsor_type, hospital, collection_center_visit_id ";

	public static String TESTS_TABLES = " FROM all_tests_ordered_view ";

	public static String TESTS_COUNT = "SELECT count(*) ";

	public static String NOT_CONDUCTED_TESTS =
		" WHERE conducted in ('N','NRN') ";

	public static PagedList pendingSamplesList(Map listing, List prescIds)
		throws SQLException, IOException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, TESTS_FIELDS, TESTS_COUNT, TESTS_TABLES, NOT_CONDUCTED_TESTS, null, false, 0, 0);
			qb.addFilter(SearchQueryBuilder.INTEGER, "prescribed_id", "IN", prescIds);
		//	qb.addFilter(SearchQueryBuilder.STRING, "sample_type_status", "=", "A");
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null)
				qb.close();
		}
	}

	public static List getSampleCollectionList(String visitId)throws Exception{
		return getSampleCollectionList(visitId,null);
	}

	public static List getSampleCollectionList(String visitId, String testId )throws Exception{

		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
        return LaboratoryDAO.getSampleCollectionList(
        		visitId,diagGenericPref.get("sampleflow_required").equals("Y"),testId);

	}

	private static final String SAMPLE_DETAILS = " SELECT sample_sno,sample_date,rejected_by," +
		" rejected_time,rejection_remarks,sample_type_id,sample_status,test_prescribed_id,sample_collection_id "+
		" FROM sample_collection join sample_rejections using(sample_collection_id) WHERE mr_no = ? AND sample_status = 'R'  ";


	public static List<BasicDynaBean> getSampleRejectionDetails(String mr_no)throws Exception{
		List l = new ArrayList();
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(SAMPLE_DETAILS);
			//logger.debug(SAMPLE_DETAILS);
			ps.setString(1, mr_no);
			l = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	private static final String GET_SAMPLE_TYPE="SELECT * FROM sample_type  where status='A' ORDER BY sample_type_id ";

	public static List getSampleType() throws SQLException {
		PreparedStatement ps=null;
		Connection con=DataBaseUtil.getReadOnlyConnection();
		ps=con.prepareStatement(GET_SAMPLE_TYPE);
		List sampleType=DataBaseUtil.queryToArrayList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return sampleType;
	}
	private static final String GET_SAMPLE_NOS="SELECT sample_sno FROM sample_collection WHERE patient_id=?";

	public static List getSampleNos(String visitId) throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_SAMPLE_NOS);
		ps.setString(1, visitId);
		List sampleNoList = DataBaseUtil.queryToArrayList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return sampleNoList;
	}

	private static final String SAMPLES_FIELDS = "SELECT sample_type, sample_sno, sample_date, outsource_dest_id, outsource_name,"
		+ "  collection_center, sample_qty, sample_status, assertion_time, rejected_time, conducted, sample_type_id, outsource_dest_id,orig_sample_no, "
		+ "  transfer_time, transfer_other_details, receipt_time, receipt_other_details, bill_status, ih_name, charge_head, "
		+ "  TEXTCAT_COMMACAT(COALESCE(ddept_id,'') ) as ddept_id, "
		+ "  TEXTCAT_COMMACAT(COALESCE(ddept_name,'') ) as ddept_name, "
		+ "  TEXTCAT_COMMACAT(COALESCE(test_name,'') ) as test_name,  "
		+ "  mr_no, patient_id, collection_center_id, patient_name, visit_type, center_id, sample_collection_id,priority, "
		+ "  coll_sample_no ";

	private static final String SAMPLES_COUNT =
		" SELECT count(distinct(sample_collection_id)) ";
//selecting field from tp, please check condition 'case when sc.sample_status='R' then tpr.field else tp.field end'
//joining table with tp please check is it tp or tpr.
	private static final String SAMPLES_TABLES =
		 "FROM ( SELECT sample_type, COALESCE(sc.coll_sample_no, sc.sample_sno) as coll_sample_no, sc.sample_sno, " 
		+ " COALESCE(sc1.sample_date, sc.sample_date) as sample_date, "		 
		+ " hcmcol.center_name as collection_center, sc.sample_collection_id, sc.sample_receive_status, "
		+ " sc1.transfer_time, sc1.transfer_other_details, sc.receipt_time, sc.receipt_other_details, "
		+ " ddept_id, ddept_name, test_name, sc.sample_qty, sc.sample_status, sc.assertion_time, sc.rejected_time, tp.conducted, st.sample_type_id, "
		+ "  coalesce(pr.mr_no,isr.mr_no) as mr_no,COALESCE(pr.patient_id, isr.incoming_visit_id) AS patient_id, pr.collection_center_id, "
		+ "  COALESCE (isr.patient_name, get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name)) AS patient_name, "
		+ "  CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END as visit_type, "
		+ "  COALESCE(sc1.orig_sample_no, sc.orig_sample_no) AS orig_sample_no, "
		+ "  COALESCE(pr.center_id, isr.center_id) as center_id, ih.hospital_name as ih_name, isr.patient_name as inc_patient_name,isr.patient_other_info, "
		+ "  case when sc.sample_status='R' then tpr.priority else tp.priority end, dom.outsource_dest_id, COALESCE(om.oh_name, hcm.center_name) AS outsource_name, "
		+ "  CASE WHEN is_outhouse_test(d.test_id,pr.center_id) THEN 'O' ELSE 'I' END AS house_status, b.status as bill_status, bc.charge_head as charge_head "
		+ "  FROM (select sc.*, sr.test_prescribed_id, case when sc.sample_status='R' then sr.test_prescribed_id"
		+ "  else sc.sample_collection_id end as key FROM sample_collection sc"
		+ "  LEFT JOIN sample_rejections sr ON (sc.sample_collection_id=sr.sample_collection_id) ) sc"
		+ "  LEFT JOIN tests_prescribed tp on (tp.sample_collection_id=sc.key) "
		+ "  LEFT JOIN tests_prescribed tpr on (tpr.prescribed_id=sc.key) "
		+ "  join diagnostics d on(COALESCE(tp.test_id, tpr.test_id) = d.test_id) "
		+ "  left join diag_outsource_master dom on(sc.outsource_dest_id = dom.outsource_dest_id) "
		+ "  left join outhouse_master om on(om.oh_id = dom.outsource_dest) "
		+ "	 LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest)"
		+ "  join diagnostics_departments using(ddept_id)  "
		+ "  LEFT JOIN patient_registration pr using(patient_id) "
		+ "  LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no) "
		+"   LEFT JOIN salutation_master sm ON (sm.salutation_id = pd.salutation) "
		+ "  LEFT JOIN incoming_sample_registration isr ON (sc.patient_id = isr.incoming_visit_id) "
		//+ "  LEFT JOIN incoming_sample_registration_details isrd ON (isr.incoming_visit_id = isrd.incoming_visit_id AND tp.prescribed_id=isrd.prescribed_id) "
		+ "  LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name) "
		//+ "  LEFT JOIN sample_collection_centers scc on (scc.collection_center_id = pr.collection_center_id) "
		+ "  LEFT JOIN tests_prescribed tp1 ON (tp1.prescribed_id = tp.coll_prescribed_id) "
		+ "  LEFT JOIN sample_collection sc1 ON (sc1.sample_collection_id = tp1.sample_collection_id) "
		+ "  LEFT JOIN patient_registration prcol on (prcol.patient_id = tp1.pat_id) "
		+ "  LEFT JOIN hospital_center_master hcmcol ON (hcmcol.center_id = COALESCE(prcol.center_id, pr.center_id)) "
		+ "  JOIN bill_activity_charge bac ON (COALESCE(tp.prescribed_id::text, tpr.prescribed_id::text)=bac.activity_id::text AND bac.activity_code='DIA')"
		+ "  JOIN bill_charge bc on (bc.charge_id=bac.charge_id)"
		+ "  JOIN bill b on (b.bill_no = bc.bill_no)"
		+ "  JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) "
		+ "  WHERE sc.sample_status IN ('C','A','R')  AND (case when sc.sample_status='R' then tpr.conducted else tp.conducted end) in ('N','NRN') "
		+ "	 AND sc.sample_transfer_status = 'P' "
		+ "  AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )"
		+ "  ) AS foo ";


	private static final String GROUP_BY =
		" sample_sno, sample_type, sample_date, collection_center, mr_no, patient_id, sample_qty, ih_name, " +
		" sample_status, assertion_time, rejected_time, conducted, sample_type_id, collection_center_id, visit_type, " +
		" center_id, sample_collection_id, patient_name, priority, outsource_dest_id, outsource_name, orig_sample_no," +
		" transfer_time, transfer_other_details, receipt_time, receipt_other_details, bill_status, charge_head, coll_sample_no ";

	public PagedList getPendingSampleAssertionList(Map filterMap,Map listingMap, String[] sampleNosArray, String[] origsampleSnosArray)
	throws SQLException,ParseException{
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try{
			qb = new SearchQueryBuilder(con, SAMPLES_FIELDS, SAMPLES_COUNT, SAMPLES_TABLES, null, GROUP_BY,listingMap);
			qb.addFilterFromParamMap(filterMap);
			if(sampleNosArray != null && !sampleNosArray.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "coll_sample_no", "in", Arrays.asList(sampleNosArray));
			if(origsampleSnosArray != null && !origsampleSnosArray.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "orig_sample_no", "in", Arrays.asList(origsampleSnosArray));
			qb.addFilter(SearchQueryBuilder.INTEGER,"sample_type_id", "IN",
					ConversionUtils.getParamAsListInteger(filterMap, "_sample_type_id"));
			int centerId = RequestContext.getCenterId();
			if (centerId != 0)
			    qb.addFilter(SearchQueryBuilder.INTEGER, "center_id", "=", centerId);
			qb.addFilter(SearchQueryBuilder.STRING, "sample_receive_status", "=", "R");
			qb.build();
			return qb.getDynaPagedList();
		}finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null)
				qb.close();
		}
	}


	public static List<BasicDynaBean> getDistinctSampleNos(List<BasicDynaBean> list) {
		List<String> listOfSamples = new ArrayList<String>();
		List<BasicDynaBean> distinctSmplNosList = new ArrayList<BasicDynaBean>();
		String samplePrefix = "";

		for (BasicDynaBean bean : list) {
			samplePrefix = (String)bean.get("sample_prefix");
			if ((null != samplePrefix && !samplePrefix.equals("")) &&
					(bean.get("sample_sno") != null && !((String)bean.get("sample_sno")).equals("")) &&
					!( ((String)bean.get("sample_status")).equals("C") || ((String)bean.get("sample_status")).equals("A") )&&
					(!listOfSamples.contains(samplePrefix+bean.get("sample_sno"))) ) {
				distinctSmplNosList.add(bean);
				listOfSamples.add((String)bean.get("sample_prefix")+bean.get("sample_sno"));
			} else if ((bean.get("sample_sno") == null || ((String)bean.get("sample_sno")).equals("")) &&
					(bean.get("sample_type_id") != null && !bean.get("sample_type_id").equals("")) && !((String)bean.get("house_status")).equals("O") &&
					!listOfSamples.contains("*"+bean.get("sample_type_id"))) {
				distinctSmplNosList.add(bean);
				listOfSamples.add("*"+bean.get("sample_type_id"));
			}
		}
		return distinctSmplNosList;
	}


	private static final String GET_ORDER_NOS = "SELECT distinct common_order_id ";

	private static final String GET_ORDER_DATES = "SELECT distinct pres_date::date ";

	private static final String TABLES =
		 " FROM tests_prescribed tp "+
		 " JOIN diagnostics d on (d.test_id = tp.test_id) and d.sample_needed = 'y' "+
		 " JOIN diagnostics_departments dd on d.ddept_id = dd.ddept_id "+
		 " LEFT JOIN bill_activity_charge bac on (bac.activity_id=tp.prescribed_id::varchar) "+
		 " AND bac.activity_code = 'DIA' "+
		 " LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id "+
		 " LEFT JOIN bill bp ON(bp.bill_no=bc.bill_no AND bp.visit_id = tp.pat_id AND bp.bill_type = 'P' ) "+
		 " LEFT JOIN bill b ON( b.bill_no=bc.bill_no AND b.visit_id = tp.pat_id AND b.bill_type = 'C' ) "+
		 " WHERE ";
	
	private static final String TABLES_FOR_SAMPLE_FLOW_NOT_REQUIRED =
		 " FROM tests_prescribed tp "+
		 " LEFT JOIN outsource_sample_details osm using(prescribed_id) "+
		 " LEFT JOIN diag_outsource_master dom on(dom.outsource_dest_id = osm.outsource_dest_id) "+
		 " JOIN diagnostics d on (d.test_id = tp.test_id) and d.sample_needed = 'y' "+
		 " JOIN diagnostics_departments dd on d.ddept_id = dd.ddept_id "+
		 " LEFT JOIN bill_activity_charge bac on (bac.activity_id=tp.prescribed_id::varchar) "+
		 " AND bac.activity_code = 'DIA' "+
		 " LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id "+
		 " LEFT JOIN bill bp ON(bp.bill_no=bc.bill_no AND bp.visit_id = tp.pat_id AND bp.bill_type = 'P' ) "+
		 " LEFT JOIN bill b ON( b.bill_no=bc.bill_no AND b.visit_id = tp.pat_id AND b.bill_type = 'C' ) "+
		 " WHERE ";

	private static final String  WHERE_CONSTRAINT_WITH_ALL_TESTS=
		" tp.pat_id = ? " +
		" and tp.conducted  NOT IN ('X','U') AND dd.category='DEP_LAB'  " +
		" and COALESCE(bp.payment_status,'P') = 'P'  and " +
		" (b.payment_status in ('P','U') or b.payment_status is null)  " ;


	private static final String  WHERE_CONSTRAINT_WITH_OUT_HOUSE_TESTS=
	 	" tp.pat_id = ? " +
        " and tp.conducted NOT IN ('X','U') and dom.outsource_dest_type='O' AND dd.category='DEP_LAB' " +
        " and COALESCE(bp.payment_status,'P') = 'P'  and " +
		" (b.payment_status in ('P','U') or b.payment_status is null)  " ;


	public static List<BasicDynaBean> getOrderNos(String visitId, String testId, String field, String date)throws SQLException, ParseException {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			java.sql.Date sqlDate = null;
			if (date != null && !date.equals(""))
				sqlDate = DateUtil.parseDate(date);
			BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
			boolean sampleFlow = diagGenericPref.get("sampleflow_required").equals("Y");
			con = DataBaseUtil.getReadOnlyConnection();

			if(sampleFlow){
				String samplesList = (field.equals("orderno") ? GET_ORDER_NOS : GET_ORDER_DATES) + TABLES;

				if(testId != null && !testId.equals("")) {
					samplesList = samplesList+" tp.test_id = ? AND "+WHERE_CONSTRAINT_WITH_ALL_TESTS;
				} else {
					samplesList = samplesList+WHERE_CONSTRAINT_WITH_ALL_TESTS;
				}

				samplesList = samplesList + ((sqlDate != null && !sqlDate.equals("")) ? " AND pres_date::date = ?" : "" );
				samplesList = samplesList + (field.equals("orderno") ? " order by common_order_id DESC" : " order by pres_date DESC");

				pstmt = con.prepareStatement(samplesList);

			}else{
				String samplesList = (field.equals("orderno") ? GET_ORDER_NOS : GET_ORDER_DATES) + TABLES_FOR_SAMPLE_FLOW_NOT_REQUIRED;
				if(testId != null && !testId.equals("")) {
					samplesList = samplesList+" tp.test_id = ? AND "+WHERE_CONSTRAINT_WITH_OUT_HOUSE_TESTS;
				} else {
					samplesList = samplesList+WHERE_CONSTRAINT_WITH_OUT_HOUSE_TESTS;
				}

				samplesList = samplesList + ((sqlDate != null && !sqlDate.equals("")) ? " AND pres_date::date = ?" : "" );
				samplesList = samplesList + (field.equals("orderno") ? " order by common_order_id DESC" : " order by pres_date DESC");

				pstmt = con.prepareStatement(samplesList);

			}

			if(testId != null && !testId.equals("") && sqlDate!= null && !sqlDate.equals("")){
				pstmt.setString(1, testId);
				pstmt.setString(2, visitId);
				pstmt.setDate(3, sqlDate);

			} else if ((testId == null || testId.equals("")) && sqlDate != null && !sqlDate.equals("")) {
				pstmt.setString(1, visitId);
				pstmt.setDate(2, sqlDate);

			} else if (testId != null && !testId.equals("") && (sqlDate == null || sqlDate.equals("")) ) {
				pstmt.setString(1, testId);
				pstmt.setString(2, visitId);

			} else {
				pstmt.setString(1, visitId);
			}
			return DataBaseUtil.queryToDynaList(pstmt);
		} finally {
			DataBaseUtil.closeConnections(con, pstmt);
		}

	}


	private static final String SAMPLE_DEPENDENCY_CHECK =
		" SELECT * FROM tests_prescribed where test_id= ? " +
		" AND common_order_id = ? AND sflag = '0'";

	public boolean isSampleCollectible(String depTestId,int orderId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SAMPLE_DEPENDENCY_CHECK);
			ps.setString(1, depTestId);
			ps.setInt(2, orderId);
			return (DataBaseUtil.queryToDynaBean(ps) == null);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String IS_OUTSOUREC_TEST = "SELECT test_id FROM diag_outsource_detail dod "
			+ "WHERE dod.test_id = ? AND dod.source_center_id = ? AND dod.status = 'A' LIMIT 1;";

	public boolean isOutsourceTest(String testId,int centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(IS_OUTSOUREC_TEST);
			ps.setString(1, testId);
			ps.setInt(2, centerId);
			return DataBaseUtil.getStringValueFromDb(ps) != null;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static Map<String, List<Hashtable<String, String>>> getOuthousesAgainstTestId(int centerId, boolean isFromIncomingReg, String visitId)throws SQLException {
		
		ArrayList<Hashtable<String, String>> list = null;
		if (isFromIncomingReg)
			list = OutHouseMasterDAO.getAllOutSourceNameForIncoming(centerId);
		else
			list = OutHouseMasterDAO.getAllOutSourceName(centerId, visitId);
		
		Map<String, List<Hashtable<String, String>>> outhousemap = new HashMap<String, List<Hashtable<String, String>>>();
		for(int i=0; i<list.size(); i++) {
			if (outhousemap.get(list.get(i).get("TEST_ID")) == null) {
				List l = new ArrayList();
				l.add(list.get(i));
				outhousemap.put(list.get(i).get("TEST_ID"), l);
			} else {
				List l = outhousemap.get(list.get(i).get("TEST_ID"));
				l.add(list.get(i));
				outhousemap.put(list.get(i).get("TEST_ID"), l);
			}
		}
		return outhousemap;
	}

	public static Map getSampleContainers()throws SQLException {
		List<BasicDynaBean> list = new GenericDAO("sample_type").listAll();
		Map map = new HashMap();
		if (list != null) {
			for (BasicDynaBean bean : list) {
				map.put(bean.get("sample_type_id"), bean.get("sample_container"));
			}
		}
		return map;
	}
	
	/* used while automatic sample registration for next hops and 
	further if its associated with other next hops */ 
	private static String IS_NEXT_OUTSOUREC_ASSOCIATED = "SELECT test_id, TEXTCAT_COMMACAT(dod.outsource_dest_id::text) AS outsource_dest_ids,"
			+ " TEXTCAT_COMMACAT(dom.outsource_dest_type::text) AS outsource_dest_types "
			+ " FROM diag_outsource_detail dod "
			+ " JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "
			+ " WHERE dod.test_id = ? AND dod.source_center_id = ? AND dod.status = 'A' "
			+ " GROUP BY dod.test_id;";

	public BasicDynaBean isNextOutsourceAssociated(String testId, int centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(IS_NEXT_OUTSOUREC_ASSOCIATED);
			ps.setString(1, testId);
			ps.setInt(2, centerId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String NULLIFY_SOURCEPRESCRIBED_ID1 = "UPDATE incoming_sample_registration_details SET source_test_prescribed = null"
			+ " WHERE source_test_prescribed = ?";
	
	private static final String NULLIFY_SOURCEPRESCRIBED_ID2 = "UPDATE tests_prescribed SET source_test_prescribed_id = null"
			+ " WHERE source_test_prescribed_id = ?";
	
	public static boolean nullifySourcetestPrescribedID(Connection con, int prescribedID)throws SQLException {
		
		boolean status = true;
		PreparedStatement pstmt = con.prepareStatement(NULLIFY_SOURCEPRESCRIBED_ID1);
		PreparedStatement pstmt1 = con.prepareStatement(NULLIFY_SOURCEPRESCRIBED_ID2);
		pstmt.setInt(1, prescribedID);
		pstmt1.setInt(1, prescribedID);
		
		status &= pstmt.executeUpdate() > 0;
		status &= pstmt1.executeUpdate() > 0;
		
		return status;
	}
	
}
