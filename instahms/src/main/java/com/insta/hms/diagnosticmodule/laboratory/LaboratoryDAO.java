package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.documentpersitence.TestDocumentAbstractImpl;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.BillActivityCharge;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.DynamicSearchQueryBuilder;
import com.insta.hms.common.DynamicSearchQueryBuilder.JoinTypes;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.OutHouseSampleDetails;
import com.insta.hms.diagnosticmodule.common.SampleCollection;
import com.insta.hms.diagnosticmodule.common.TestReportImages;
import com.insta.hms.diagnosticmodule.common.TestVisitReports;
import com.insta.hms.diagnosticsmasters.Result;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.emr.EMRInterface.Provider;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LaboratoryDAO {

	static Logger logger = LoggerFactory.getLogger(LaboratoryDAO.class);
	
	private static GenericDAO tpDAO = new GenericDAO("tests_prescribed");
	private static GenericDAO tvrDAO = new GenericDAO("test_visit_reports");
	
	private static final String GET_TEST_DETAILS =
			" SELECT d.test_name,doc.doctor_name,tp.prescribed_id, tp.re_conduction, " +
			"   tp.sflag, tp.common_order_id,tp.conducted, " +
			"	tp.outsource_dest_prescribed_id, tp.report_id " +
			" FROM tests_prescribed tp " +
			" 	JOIN diagnostics d on tp.test_id = d.test_id" +
			" 	JOIN diagnostics_departments dd on dd.ddept_id = d.ddept_id  and dd.category = ?" +
			"   LEFT join doctors doc on (tp.pres_doctor = doc.doctor_id )" +
			" WHERE pat_id=? and conducted in ('N','NRN') ";

	public static List<BasicDynaBean> getTestDetailsList(String visitId, String category) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TEST_DETAILS);
			ps.setString(1, category);
			ps.setString(2, visitId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			if (ps!=null) ps.close();
			if (con!=null) con.close();
		}
	}
	
	public static final String TEST_DETAILS_LIST =
			"SELECT mr_no ,patient_id,test_id,prescribed_id,conducted_in_reportformat,"
            + " resultlabel,report_value,units,reference_range,withinnormal,test_details_id,"
            + " user_name,resultlabel_id,test_detail_status"
            + " FROM test_details WHERE prescribed_id=?";

	public static List getTestDetailsList(int prescribedId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(TEST_DETAILS_LIST);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_TEST_DETAILS_FOR_CANCELLATION =
			" SELECT d.test_name,doc.doctor_name,tp.prescribed_id, tp.re_conduction, " +
			"   tp.sflag, tp.common_order_id,tp.conducted, " +
			"	tp.outsource_dest_prescribed_id, tp.report_id, d.test_id, tpc.report_id AS cond_center_report_id," +
			"	tpc.re_conduction AS cond_center_re_conduction, tp.curr_location_presc_id  " +
			" FROM tests_prescribed tp " +
			" 	JOIN diagnostics d on tp.test_id = d.test_id" +
			" 	JOIN diagnostics_departments dd on dd.ddept_id = d.ddept_id  and dd.category = ?" +
			"   LEFT join doctors doc on (tp.pres_doctor = doc.doctor_id )" +
			"	LEFT JOIN tests_prescribed tpc ON(tpc.prescribed_id = tp.curr_location_presc_id)" +
			" WHERE tp.pat_id=? ";

	public static List<Hashtable<String,String>> getTestDetails(String visitId, String category, String allowCancelAtAnyTime) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_TEST_DETAILS_FOR_CANCELLATION +
					(allowCancelAtAnyTime.equals("A") ? "AND tp.conducted NOT IN ('S', 'X', 'RAS', 'RBS')"
							: "AND tp.conducted IN ('N', 'NRN') AND (tpc.conducted IS NULL OR tpc.conducted IN ('N', 'NRN'))"));
			ps.setString(1, category);
			ps.setString(2, visitId);
			return DataBaseUtil.queryToArrayList(ps);
		}finally{
			if (ps!=null) ps.close();
			if (con!=null) con.close();
		}
	}

	private static final String TEST_DETAILS =
		" SELECT tp.mr_no,tp.pat_id,tp.test_id,tp.prescribed_id," +
		"  tp.pres_doctor, tp.conducted, COALESCE(sc.coll_sample_no, sc.sample_sno) AS sample_sno, sc.sample_status, d.test_name, dd.ddept_name," +
		"  dd.ddept_id, d.sample_needed, d.conduction_format, " +
		"  CASE WHEN is_outhouse_test(tp.test_id,?) THEN 'O' ELSE 'I' END AS house_status ," +
		"  tc.remarks, COALESCE(tc.conducted_by, bac.doctor_id, (case when bc.charge_group='PKG' then null else bc.payee_doctor_id end)) AS conducted_by, " +
		"  to_char(tc.conducted_date,'dd-mm-yyyy') as conducted_date," +
		"  tp.report_id, tvr.report_name, to_char(COALESCE(sccol.sample_date, sc.sample_date), 'dd-mm-yyyy') as sample_date, " +
		"  COALESCE(to_char(sccol.sample_date, 'HH24:mi'), to_char(sc.sample_date, 'HH24:mi')) as sample_time,"+
		"  COALESCE(sccol.orig_sample_no, sc.orig_sample_no) AS orig_sample_no, tp.prescription_type, tp.labno,b.payment_status,tp.re_conduction, " +
		"  COALESCE(isrreg.incoming_source_type, isr.incoming_source_type) AS incoming_source_type, " +
		"  b.status as bill_status, b.bill_type, d.conducting_doc_mandatory,d.conduction_instructions, " +
		"  to_char(tc.conducted_date,'HH24:mi') as conducted_time,ss.source_name " +
		"  ,sc.specimen_condition,tp.remarks as order_remarks,st.sample_type, " +
		"	(select count(prescribed_id) from test_documents td where td.prescribed_id=tp.prescribed_id) as doc_count," +
		"   CASE " +
		"		WHEN EXISTS(SELECT title FROM test_images tm WHERE tm.prescribed_id = tp.prescribed_id) " +
		"			THEN 'Y' " +
		"		ELSE 'N' " +
		"   END as imageuploaded, tc.validated_by, tc.validated_date, tc.technician, d.conducting_role_id, tp.revision_number, " +
		" 	d.mandate_additional_info, tp.outsource_dest_prescribed_id, d.isconfidential " +
		"  FROM tests_prescribed tp " +
		"  LEFT JOIN sample_collection sc USING (sample_collection_id) " +
		" LEFT JOIN sample_sources ss on(sample_source_id = source_id) " +
		" LEFT JOIN sample_type st on(st.sample_type_id = sc.sample_type_id) " +
		" JOIN diagnostics d on d.test_id = tp.test_id " +
		" JOIN diagnostics_departments dd on d.ddept_id = dd.ddept_id " +
		" LEFT join tests_conducted tc on tc.prescribed_id = tp.prescribed_id " +
		" LEFT join test_visit_reports tvr on tp.pat_id = tvr.patient_id and tp.report_id = tvr.report_id " +
		" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
		" LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) " +
		" LEFT JOIN sample_collection sccol ON (sccol.sample_collection_id = tpcol.sample_collection_id) " +
		" LEFT JOIN incoming_sample_registration isrreg ON (isrreg.incoming_visit_id = tpcol.pat_id) " +
		" LEFT JOIN bill_activity_charge bac on (bac.activity_id=tp.prescribed_id::varchar) " +
		"  AND bac.activity_code = 'DIA'" +
		" LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id" +
		" LEFT JOIN bill b on b.bill_no=bc.bill_no" ;

	public List<Hashtable<String, String>> getVisitTestDetails(String visitId, String category,
			String prescriptionList)throws SQLException  {

		PreparedStatement ps = null;
		Connection con = null;
		List<Hashtable<String, String>>l = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
	
			int centerId = VisitDetailsDAO.getCenterId(con,visitId);
	
			ArrayList presList = new ArrayList();
			String temp[] = prescriptionList.split(",");
			for(int i=0;i<temp.length;i++){
				if(!temp[i].equals(",")){
					presList.add(Integer.parseInt(temp[i]));
				}
			}
			StringBuilder query  = new StringBuilder(TEST_DETAILS);
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldOpValue(where, "tp.pat_id", "=", visitId);
			DataBaseUtil.addWhereFieldOpValue(where, "tp.conducted", "!=", "X");
			DataBaseUtil.addWhereFieldOpValue(where, "tp.conducted", "!=", "RBS");
			DataBaseUtil.addWhereFieldOpValue(where, "tp.conducted", "!=", "RAS");
			DataBaseUtil.addWhereFieldOpValue(where, "dd.category", "=", " ");
			DataBaseUtil.addWhereFieldInList(where,"tp.prescribed_id",presList);
			query.append(where);
			query.append("order by dd.display_order,tp.prescribed_id");
	
			ps = con.prepareStatement(query.toString());
	
			int index = 1;
			ps.setInt(index++, centerId);
			ps.setString(index++, visitId);
			ps.setString(index++, "X");
			ps.setString(index++, "RAS");
			ps.setString(index++, "RBS");
			ps.setString(index++, category);
			for(int i=0;i<presList.size();i++){
				ps.setInt(index++, (Integer)presList.get(i) );
			}
	
			logger.debug("{}", ps);
			l = DataBaseUtil.queryToArrayList(ps);
	
			return l;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		
	}

	private static String VISITED_CENTER_TEST_RESULTS =
		  " SELECT tm.test_id,tm.resultlabel,tm.resultlabel_id,td.report_value as value,      	"
	   +  "   td.comments as remarks,td.result_disclaimer, td.withinnormal, td.reference_range                    	"
	   +  "  ,tm.expr_4_calc_result,tm.code_type,tm.result_code,tm.units,td.test_details_id   	"
	   +  "  ,td.revised_test_details_id,td.original_test_details_id,td.amendment_reason      	"
	   +  "  ,td.test_detail_status,td.test_detail_status,tm.display_order,td.calculated,"
	   +  "  tm.data_allowed,tm.source_if_list, dmm.method_name, dmm.method_id, tm.default_value"
	   +  " FROM test_results_master tm                                                         "
	   +  " LEFT JOIN test_details td ON td.test_id = tm.test_id "
	   +  "  AND td.resultlabel_id =  tm.resultlabel_id  AND td.prescribed_id = ?               "
	   +  " LEFT JOIN diag_methodology_master dmm ON (tm.method_id = dmm.method_id)"
	   +  " LEFT JOIN test_results_center trc ON (tm.resultlabel_id = trc.resultlabel_id) "
	   +  " WHERE tm.test_id=? AND (trc.center_id = 0 OR trc.center_id = ?) AND trc.status='A' ORDER BY display_order , resultlabel_id, test_details_id desc  ";

	private static String TEST_RESULTS =
		  " SELECT tm.test_id, COALESCE(tm.resultlabel, td.resultlabel) AS resultlabel, tm.resultlabel_id, td.report_value as value, "
	   +  "   td.comments as remarks, td.result_disclaimer, td.withinnormal, td.reference_range "
	   +  "  ,tm.expr_4_calc_result,tm.code_type, tm.result_code, COALESCE(tm.units, td.units) as units, td.test_details_id   	"
	   +  "  ,td.revised_test_details_id, td.original_test_details_id, td.amendment_reason      	"
	   +  "  ,td.test_detail_status, td.test_detail_status, tm.display_order, td.calculated,"
	   +  "  tm.data_allowed,tm.source_if_list, dmm.method_name, dmm.method_id, tm.default_value"
	   +  " FROM test_details td                                                         "
	   +  " LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id AND td.resultlabel_id =  tm.resultlabel_id)"
	   +  " LEFT JOIN diag_methodology_master dmm ON (tm.method_id = dmm.method_id)"
	   +  " WHERE td.prescribed_id = ? AND td.test_id = ?  ORDER BY display_order , resultlabel_id, test_details_id desc  ";
	public static List<BasicDynaBean> getResultsfrommaster(String testId, String prescribedId, String visitId)throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		List<BasicDynaBean> l = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			int centerId = VisitDetailsDAO.getCenterId(con,visitId);
			GenericDAO testDetailsDao = new GenericDAO("test_details");
			BasicDynaBean bean = testDetailsDao.getBean();
			testDetailsDao.loadByteaRecords(bean, "prescribed_id", Integer.parseInt(prescribedId));

			if(null != bean && bean.get("test_id")!=null){
				List<BasicDynaBean> n = null;
				ps = con.prepareStatement(TEST_RESULTS);
				ps.setInt(1, Integer.parseInt(prescribedId));
				ps.setString(2, testId);
				l = DataBaseUtil.queryToDynaList(ps);

				ps = con.prepareStatement(VISITED_CENTER_TEST_RESULTS);
				ps.setInt(1, Integer.parseInt(prescribedId));
				ps.setString(2, testId);
				ps.setInt(3, centerId);
				n = DataBaseUtil.queryToDynaList(ps);

				Iterator it = n.iterator();
				 while (it.hasNext()) {
					 boolean isExists = false;
					 BasicDynaBean resultbean = (BasicDynaBean) it.next();
					 Iterator itr = l.iterator();
						while (itr.hasNext()) {
							BasicDynaBean savedresult = (BasicDynaBean) itr.next();
								if(resultbean.get("resultlabel_id").toString().
										equals(null != savedresult.get("resultlabel_id") ? savedresult.get("resultlabel_id").toString() : null)) {
									isExists = true;
									break;
								}
						}
						if (!isExists)
							l.add(resultbean);
				 }

			} else {
				ps = con.prepareStatement(VISITED_CENTER_TEST_RESULTS);
				ps.setInt(1, Integer.parseInt(prescribedId));
				ps.setString(2, testId);
				ps.setInt(3, centerId);
				logger.debug(VISITED_CENTER_TEST_RESULTS);
				l = DataBaseUtil.queryToDynaList(ps);
			}

		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	public List getLabtechnicions(String category, int center_id)throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		List l;

		try{
			con = DataBaseUtil.getConnection();
		    String doctorlist = "select d.Doctor_name, d.doctor_id " +
		    					" from doctors d " +
			                    "join department dept on d.dept_id = dept.dept_id and dept.dept_id = ? " +
			                     "and d.status = 'A' " +
			                     "JOIN doctor_center_master dcm ON (d.doctor_id = dcm.doctor_id)"+
			                    // "JOIN hospital_center_master hcm ON (hcm.center_id = dcm.center_id) " +
			                     "where dcm.center_id in('0',?) and dcm.status='A' ";
			ps = con.prepareStatement(doctorlist);
			ps.setString(1,category);
			ps.setInt(2, center_id);
			l = DataBaseUtil.queryToArrayList(ps);
			logger.debug("lab technician list===>"+l);
		}finally{
			if(ps!=null)ps.close();
			if(con!=null)con.close();
		}

		return l;
	}

	private static final String GET_TEMPLAT = "SELECT report_file FROM test_format WHERE testformat_id = ? ";
	private static final String CHECK_PRESCRIPTION ="SELECT COUNT(*) FROM test_details WHERE prescribed_id = ? AND format_name=? ";
	private static final String GET_PATRTIAL_TEMPLATE = "SELECT patient_report_file FROM test_details WHERE prescribed_id = ?";

	public String getTemplateContent(Connection con ,String templateid,String prescribedid,String testDetailsId)throws SQLException{
	  String template = null;
      PreparedStatement ps = null;
      try {
        if ( testDetailsId == null )
            testDetailsId = "";
        ps = con.prepareStatement(CHECK_PRESCRIPTION + ( testDetailsId.isEmpty() ? "" : " AND test_details_id = ? "));
        int index = 1;
        ps.setInt(index++, Integer.parseInt(prescribedid));
        if ( !testDetailsId.isEmpty() ) {
            ps.setString(index++, templateid);
            ps.setInt(index++, Integer.parseInt(testDetailsId));
        } else {
            ps.setString(index++, templateid);
        }
  
        String count = DataBaseUtil.getStringValueFromDb(ps);
        if(count.equals("0")){
          int indexPar = 1;
            ps = con.prepareStatement(GET_TEMPLAT);
            ps.setString(indexPar++,templateid);
            template = DataBaseUtil.getStringValueFromDb(ps);
        }else{
            ps = con.prepareStatement(GET_PATRTIAL_TEMPLATE + ( testDetailsId.isEmpty() ? "" : " AND test_details_id = ? " ));
            int indexPar = 1;
            ps.setInt(indexPar++,Integer.parseInt(prescribedid));
            if ( !testDetailsId.isEmpty() )
                ps.setInt(indexPar++, Integer.parseInt(testDetailsId));
            template = DataBaseUtil.getStringValueFromDb(ps);
        }
      } finally {
        DataBaseUtil.closeConnections(null, ps);
      }
      return template;
	}

	/*
	 * Returns a list of patient Ids according to the filter given for the tests.
	 */
	private static String SCHEDULES_VISIT_FIELDS =
		" SELECT mr_no, patient_id, max(prescribed_id) as max_pres_id ";
	
	private static String SCHEDULES_VISIT_COUNT =
		" SELECT count(distinct patient_id) ";

	private static String SCHEDULES_VISIT_FROM_TABLES = " FROM tests_prescribed tp ";	

	private static String SCHEDULES_VISIT_GROUP =
		" mr_no, patient_id ";

    public static PagedList getDiagSchedulesVisits(String category, Map filterParams, Map listing,
      String[] origsampleSnosArray)
		throws Exception {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection(true);

			String initWhere = "";
			DynamicSearchQueryBuilder qb = null;
			Map modifiedFilterParamMap = null;
			List<Integer> prescribedIds = getDiagSchedulesPrescribedIds(category, filterParams);
			initWhere += 
			    " WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0), mr_no)) "
			    + " AND prescribed_id = ANY (? :: int[]) ";
			String prescribedIdsFilter = "'{" + StringUtils.join(prescribedIds, ",") + "}'";
			initWhere = initWhere.replace("?", prescribedIdsFilter);
			Map<String, String> visitSubFilterMap = getParamReplacementMapForFilteringVisits(category);
			qb = new DynamicSearchQueryBuilder(con, SCHEDULES_VISIT_FIELDS, SCHEDULES_VISIT_COUNT,
			    SCHEDULES_VISIT_FROM_TABLES, initWhere, SCHEDULES_VISIT_GROUP, listing,
			    visitSubFilterMap, aliasTableMap());
            modifiedFilterParamMap = modifyFilterParamsMap(filterParams, visitSubFilterMap);
            qb.addFilterFromParamMap(modifiedFilterParamMap);
            addJoinsToTestsPrescribed(modifiedFilterParamMap, qb, origsampleSnosArray, category);
            qb.addSecondarySort("max_pres_id");
			qb.build();
			PagedList l = qb.getMappedPagedList();
			qb.close();
			return l;
    } finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}
	   
    private static String SCHEDULES_PRESC_ID_FIELD = 
        " SELECT prescribed_id ";
    
	private static String LAB_SCHEDULES_PRES_ID_TABLES =
	      "FROM tests_prescribed tp  "
	      + " JOIN diagnostics d ON (d.test_id = tp.test_id "
	      + "  AND tp.conducted IN('N','P','C','V','RC','RV','RP','MA','TS','CC','CR'))"
	      + " JOIN diagnostics_departments dd on(d.ddept_id=dd.ddept_id AND dd.category='DEP_LAB')"
	      + " LEFT JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id)"
	      + " LEFT JOIN sample_collection sc "
	      + "  ON (sc.sample_collection_id = tp.sample_collection_id) ";
	
	private static String RAD_SCHEDULES_PRES_ID_TABLES =
        "FROM tests_prescribed tp  "
        + " JOIN diagnostics d ON (d.test_id = tp.test_id "
        + "  AND tp.conducted IN('N','P','C','V','RC','RV','NRN','CRN','RP','MA','TS','CC','CR'))"
        + " JOIN diagnostics_departments dd on(d.ddept_id=dd.ddept_id AND dd.category='DEP_RAD')"
        + " LEFT JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id)"
        + " LEFT JOIN sample_collection sc "
        + "  ON (sc.sample_collection_id = tp.sample_collection_id) ";

  private static List<Integer> getDiagSchedulesPrescribedIds(String category, Map filterParams)
      throws ParseException, SQLException {
    Connection con = null;
    List<Integer> prescribedIds = null;
    try {
      con = DataBaseUtil.getConnection(true);
      String initWhere = "WHERE conducted NOT IN ('NRN','CRN') "
          + " AND (report_id = 0 OR report_id IS NULL OR signed_off = 'N') "
          + " AND (sample_receive_status IS NULL OR sample_receive_status = 'R') ";
      DynamicSearchQueryBuilder qb = null;
      Map<String, String> testsPrescFilterMap = getParamReplacementMapForFilteringPrescIds(category);
      if (category.equals("DEP_LAB")) {
        qb = new DynamicSearchQueryBuilder(con, SCHEDULES_PRESC_ID_FIELD, null,
            LAB_SCHEDULES_PRES_ID_TABLES, initWhere, null, null, false, 0, 0, testsPrescFilterMap,
            aliasTableMap());
        if (testsPrescFilterMap.containsKey("sample_no")) {
          qb.addJoin(JoinTypes.LEFT_JOIN.toString(), "tp", "itp",
              "tp.coll_prescribed_id = itp.prescribed_id");
        }
      } else {
        qb = new DynamicSearchQueryBuilder(con, SCHEDULES_PRESC_ID_FIELD, null,
            RAD_SCHEDULES_PRES_ID_TABLES, initWhere, null, null, false, 0, 0, testsPrescFilterMap,
            aliasTableMap());
      }
      Map modifiedFilterParams =
          modifyFilterParamsMap(filterParams, testsPrescFilterMap);
      qb.addFilterFromParamMap(modifiedFilterParams);
      qb.build();
      PreparedStatement ps = qb.getDataStatement();
      prescribedIds = DataBaseUtil.queryToIntegerArrayList(ps);
      return prescribedIds;
    } finally {
      DataBaseUtil.closeConnections(con, null);
    }
  }
  
  private static void addJoinsToTestsPrescribed(Map filterParams, DynamicSearchQueryBuilder qb,
      String[] origsampleSnosArray, String category) {
    String leftJoin = JoinTypes.LEFT_JOIN.toString();
    String patientDetailsJoinClause = "pd.mr_no = tp.mr_no";
    if (filterParams.containsKey("visit_type") || filterParams.containsKey("reg_date")
        || filterParams.containsKey("center_id")
        || filterParams.containsKey("reference_docto_id")) {
      qb.addJoin(leftJoin, "tp", "pr", "tp.pat_id = pr.patient_id");
      qb.addJoin(leftJoin, "tp", "isr", "isr.incoming_visit_id = tp.pat_id");
    }
    if (filterParams.containsKey("oh_name")) {
      qb.addJoin(leftJoin, "tp", "oh", "oh.prescribed_id = tp.prescribed_id");
      qb.addJoin(leftJoin, "tp", "dom", "dom.outsource_dest_id = oh.outsource_dest_id");
      qb.addJoin(leftJoin, "tp", "om", "om.oh_id = dom.outsource_dest");
    }
    if (filterParams.containsKey("patient_sponsor_type")) {
      qb.addJoin(leftJoin, "tp", "pr", "tp.pat_id = pr.patient_id");
      qb.addJoin(leftJoin, "tp", "tpa", "pr.primary_sponsor_id=tpa.tpa_id");
      if (category.equals("DEP_LAB")) {
        qb.addJoin(leftJoin, "tp", "itp", "itp.prescribed_id=tp.coll_prescribed_id");
      } 
      if (category.equals("DEP_RAD")) {
        qb.addJoin(leftJoin, "tp", "isr", "tp.pat_id = isr.incoming_visit_id");
        qb.addJoin(leftJoin, "tp", "isrd", "isrd.prescribed_id = tp.prescribed_id "
            + "AND isr.incoming_visit_id = isrd.incoming_visit_id");
        qb.addJoin(leftJoin, "tp", "itp", "itp.prescribed_id=isrd.source_test_prescribed");
      }
      qb.addJoin(leftJoin, "tp", "ipr", "ipr.patient_id=itp.pat_id");
      qb.addJoin(leftJoin, "tp", "itpa", "itpa.tpa_id=ipr.primary_sponsor_id");
      patientDetailsJoinClause = "pd.mr_no = COALESCE(itp.mr_no, tp.mr_no)";
    }
    if(origsampleSnosArray != null && !origsampleSnosArray.equals("")) {
      qb.addFilter(SearchQueryBuilder.STRING, "orig_sample_no", "in", 
          Arrays.asList(origsampleSnosArray));
      qb.addJoin(leftJoin, "tp", "isr", "tp.pat_id = isr.incoming_visit_id");
      qb.addJoin(leftJoin, "tp", "isrd", "isrd.prescribed_id = tp.prescribed_id "
          + "AND isr.incoming_visit_id = isrd.incoming_visit_id");
    }
    
    if (filterParams.containsKey("collection_center_id") || filterParams.containsKey("hospital")) {
      qb.addJoin(leftJoin, "tp", "pr", "pr.patient_id = tp.pat_id");
    }
    
    if (filterParams.containsKey("inc_patient_name") || filterParams.containsKey("patient_other_info")) {
      qb.addJoin(leftJoin, "tp", "isr", "isr.incoming_visit_id = tp.pat_id");
    }
    
    if (filterParams.containsKey("ih_name")) {
      qb.addJoin(leftJoin, "tp", "isr", "isr.incoming_visit_id = tp.pat_id");
      qb.addJoin(leftJoin, "tp", "ih", "ih.hospital_id = isr.orig_lab_name");
    }
    qb.addJoin(leftJoin, "tp", "pd", patientDetailsJoinClause);
  }
  
  private static Map<String, String> aliasTableMap() {
    Map<String, String> map = new HashMap<>();
    map.put("tp", "tests_prescribed");
    map.put("pr", "patient_registration");
    map.put("isr", "incoming_sample_registration");
    map.put("isrd", "incoming_sample_registration_details");
    map.put("oh", "outsource_sample_details");
    map.put("dom", "diag_outsource_master");
    map.put("om", "outhouse_master");
    map.put("tpa", "tpa_master");
    map.put("itp", "tests_prescribed");
    map.put("ipr", "patient_registration");
    map.put("itpa", "tpa_master");
    map.put("ih", "incoming_hospitals");
    map.put("pd", "patient_details");
    return map;
  }
   
  
  private static Map<String, String> getParamReplacementMapForFilteringPrescIds(String category) {
    Map<String, String> map = new HashMap<>();
    if (category.equals("DEP_LAB")) {
      map.put("sample_no", "coalesce(itp.sample_no, tp.sample_no)");
      map.put("sample_status",
          " CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END ");
    }
    map.put("common_order_id", "tp.common_order_id");
    map.put("mr_no", "tp.mr_no"); 
    map.put("patient_id", "tp.pat_id");
    map.put("prescribed_id", "tp.prescribed_id");
    map.put("labno", "tp.labno"); 
    map.put("pres_date", "CAST(tp.pres_date as date)"); 
    map.put("conducted", "tp.conducted"); 
    map.put("pres_doctor", "tp.pres_doctor");
    map.put("ddept_id", "d.ddept_id"); 
    map.put("test_name", "d.test_name"); 
    map.put("category", "dd.category");
    map.put("report_date", "tvr.report_date"); 
    map.put("report_results_severity_status",
        "tvr.report_results_severity_status"); 
    map.put("exp_rep_ready_time", "tp.exp_rep_ready_time"); 
    map.put("priority", "tp.priority"); 
    map.put("report_id", "tp.report_id");
    map.put("signed_off", "tvr.signed_off");
    return map;
  }
  
  private static Map<String, String> getParamReplacementMapForFilteringVisits(String category) {
    Map<String, String> map = new HashMap<>();
    if (category.equals("DEP_LAB")) {
      map.put("orig_sample_no", "isrd.orig_sample_no");
      map.put("reference_docto_id",
          "COALESCE(pr.reference_docto_id, isr.referring_doctor)");
    }
    map.put("mr_no", "tp.mr_no"); 
    map.put("patient_id", "tp.pat_id");
    map.put("prescribed_id", "tp.prescribed_id");
    map.put("oh_name", "om.oh_name");
    map.put("report_id", "tp.report_id");
    map.put("signed_off", "tvr.signed_off");
    map.put("inc_patient_name", "isr.patient_name");
    map.put("ih_name", "ih.hospital_name");
    map.put("visit_type",
        "CASE WHEN isr.incoming_visit_id IS NOT NULL THEN 't' ELSE pr.visit_type END ");
    map.put("reg_date", "coalesce(pr.reg_date, isr.date");
    map.put("patient_other_info", "isr.patient_other_info");
    map.put("hospital",
        "CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END");
    map.put("sample_receive_status", "sc.sample_receive_status");
    map.put("patient_sponsor_type",
        "CASE WHEN coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R') = 'R' THEN 'R' ELSE 'S' END");
    map.put("collection_center_id", "pr.collection_center_id");
    map.put("center_id", "coalesce(pr.center_id, isr.center_id)");
    return map;
  
  }
	
  private static Map modifyFilterParamsMap(Map filterParamMap, Map subFilterMap) {
    Map modifiedFilterParamMap = new HashMap();
    Iterator fIt = filterParamMap.entrySet().iterator();
    while (fIt.hasNext()) {
      Map.Entry fEntry = (Map.Entry) fIt.next();
      if (subFilterMap.containsKey(fEntry.getKey())) {
        String fKey = (String) fEntry.getKey();
        modifiedFilterParamMap.put(fEntry.getKey(), fEntry.getValue());
        String fKeyOp = fKey + "@op";
        if (filterParamMap.containsKey(fKeyOp)) {
          modifiedFilterParamMap.put(fKeyOp, filterParamMap.get(fKeyOp));
        }
        String fKeyType = fKey + "@type";
        if (filterParamMap.containsKey(fKeyType)) {
          modifiedFilterParamMap.put(fKeyType, filterParamMap.get(fKeyType));
        }
        String fKeyCast = fKey + "@cast";
        if (filterParamMap.containsKey(fKeyCast)) {
          modifiedFilterParamMap.put(fKeyCast, filterParamMap.get(fKeyCast));
        }
      }
    }
    return modifiedFilterParamMap;
  }

	private static String SCHEDULES_PRESC_ID = " SELECT prescribed_id ";
	private static String SCHEDULES_PRESC_COUNT = " SELECT count(prescribed_id) ";
	private static String SCHEDULES_PRESC_TABLE = " FROM diag_schedules_summary_view ";
	public static PagedList getDiagSchedulesPresIds(Map filterParams, Map listingParams, String[] origsampleSnosArray) throws SQLException,
		ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
		    List<String> validColumns = Arrays.asList("pres_date", "token_number", "mr_no",
		        "patient_id");
			qb = new SearchQueryBuilder(con, SCHEDULES_PRESC_ID, SCHEDULES_PRESC_COUNT,
	              SCHEDULES_PRESC_TABLE, null, null, listingParams, validColumns);

			qb.addFilterFromParamMap(filterParams);

			String sortOrder = (String) listingParams.get(LISTING.SORTCOL);
			sortOrder = sortOrder == null || sortOrder.equals("") ? "pres_date" : sortOrder;

			if (sortOrder.equals("mr_no")) {
				qb.addSecondarySort("reg_date", true);
			}
			if (!sortOrder.equals("pres_date")) {
				qb.addSecondarySort("pres_date", true);
			}
			if(origsampleSnosArray != null && origsampleSnosArray.length > 0)
				qb.addFilter(SearchQueryBuilder.STRING, "orig_sample_no", "in", Arrays.asList(origsampleSnosArray));
			qb.build();

			return qb.getDynaPagedList();

		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	/*
	 * Returns the details of reports/tests for the given list of patients. Used in conjunction with
	 * the previous query for the Lab Schedules dashboard.
	 */

	public static List<BasicDynaBean> getDiagSchedulesDetails(String category, Map filterParams,
			List<String> visitIds) throws SQLException, ParseException {
		Connection con = null;
		try {
			con = DataBaseUtil.getConnection(true);
			String initWhere = "WHERE  (sample_receive_status IS NULL OR sample_receive_status = 'R')";
			SearchQueryBuilder qb = null;
			if(category.equals("DEP_LAB")) {
				qb = new SearchQueryBuilder(con,
					"SELECT * ", null, "FROM diag_lab_schedules_view ", initWhere, null, false, 0, 0);
			} else {
				qb = new SearchQueryBuilder(con,
						"SELECT * ", null, "FROM diag_rad_schedules_view ", null, null, false, 0, 0);
			}
			qb.addFilterFromParamMap(filterParams);
			qb.addFilter(SearchQueryBuilder.STRING, "patient_id", "IN", visitIds);
			qb.addSecondarySort("prescribed_id");
			qb.build();

			List l = DataBaseUtil.queryToDynaList(qb.getDataStatement());
			qb.close();
			return l;
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}

	}

	public static String TESTS_FIELDS  =
		" SELECT mr_no, pat_id, test_id, labno,exp_rep_ready_time, test_name, conducted, sflag, prescribed_id, common_order_id, " +
		" 	is_outhouse_selected, remarks, patient_full_name, patient_name, inc_patient_name, incoming_visit_id, " +
		" 	last_name, salutation, sample_needed, ddept_id, house_status, priority, bill_type, visit_type, " +
		"	payment_status, report_id, report_name, signed_off, report_data, report_date, re_conduction, " +
		"	charge_group, pres_date, ih_name, oh_name, charge_head, reg_date,results_entry_applicable, " +
		"   category, sample_status,sample_no,doctor_name as pres_doctor_name, doc_count, token_number, hospital," +
		"	outsource_dest, outsource_dest_type, incoming_source_type, sample_collection_status, " +
		"	conducting_doctor_name, doctor_id, sample_receive_status, sample_transfer_status, conducting_doc_mandatory, patient_sponsor_type ";
	public static String TESTS_TABLES = " FROM all_tests_ordered_view ";
	public static String TESTS_COUNT = "SELECT count(*) ";

	public static PagedList unfinishedTestsList(Map filterParams, Map listing, List prescIds, String[] origsampleSnosArray)
		throws SQLException, IOException, ParseException {
		Connection con = DataBaseUtil.getConnection(true);
		SearchQueryBuilder qb = null;
		String initWhere = null;
		if (((String[])filterParams.get("category"))[0].equals("DEP_LAB"))
			initWhere = "WHERE (sample_receive_status IS NULL OR sample_receive_status = 'R')";
		try {
			qb = new SearchQueryBuilder(con, TESTS_FIELDS, TESTS_COUNT, TESTS_TABLES, initWhere, null, false, 0, 0);
			qb.addFilter(SearchQueryBuilder.INTEGER, "prescribed_id", "IN", prescIds);
			if(origsampleSnosArray != null && !origsampleSnosArray.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "orig_sample_no", "in", Arrays.asList(origsampleSnosArray));
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null)
				qb.close();
		}
	}

	public List getTestConfirmList(String mrno ,String department, String testid, String sortFeild, String sortOrder)throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		List testList = null;
		try{
		con =  DataBaseUtil.getConnection();
		String prescribedMrnos = "SELECT TP.MR_NO,TP.PAT_ID,TP.TEST_ID,TP.TEST_NAME,TP.CONDUCTED,TP.SFLAG,TP.PRESCRIBED_ID," +
				"(PD.PATIENT_NAME||(CASE PD.MIDDLE_NAME WHEN '' THEN ' ' ELSE (' '||PD.MIDDLE_NAME||' ') END)||PD.LAST_NAME) as NAME," +
				"TC.CONDUCTED_STATUS,TC.CONFIRM_STATUS,D.SAMPLE_NEEDED,TP.DEPARTMENT,TP.PRIORITY AS PRIORITY , SC.SAMPLE_SNO, " +
				"B.BILL_TYPE,B.STATUS  as BILL_STATUS FROM PATIENT_DETAILS PD " +
				"JOIN PATIENT_REGISTRATION PR ON PR.MR_NO = PD.MR_NO AND  PR.CFLAG='0' JOIN TESTS_PRESCRIBED TP ON TP.pat_id=PR.PATIENT_ID " +
				"AND TP.BILL_RAISED = 'Y' AND TP.CONFIRM_STATUS='N' AND TP.CONDUCTED='Y' JOIN diagnostics_departments DD on dd.ddept_id = tp.department " +
				"and dd.category='DEP_LAB' LEFT OUTER JOIN TESTS_CONDUCTED TC ON TC.MR_NO = TP.MR_NO " +
				"AND TP.PRESCRIBED_ID = TC.PRESCRIBED_ID AND TC.CONFIRM_STATUS = 'N' LEFT OUTER JOIN SAMPLE_COLLECTION SC ON " +
				"SC.sample_sno=TP.sample_no and SC.SAMPLE_STATUS = 'A'  JOIN DIAGNOSTICS D ON D.TEST_ID = TP.TEST_ID " +
				" and ((D.SAMPLE_NEEDED='y' and TP.SFLAG='1') or(D.SAMPLE_NEEDED='n')) " +
				"JOIN BILL_ACTIVITY_CHARGE BAC ON BAC.ACTIVITY_ID=TP.PRESCRIBED_ID::varchar AND BAC.activity_code='DIA'" +
				"JOIN BILL_CHARGE BC ON  BC.CHARGE_ID=BAC.CHARGE_ID  " +
				"JOIN BILL B ON B.BILL_NO=BC.BILL_NO  ";

		if(mrno!=null && !mrno.equalsIgnoreCase("")) {
			prescribedMrnos = prescribedMrnos +" AND TP.MR_NO=?";
		}
		if(department!=null && !department.equalsIgnoreCase("")) {
			prescribedMrnos = prescribedMrnos +" AND TP.DEPARTMENT = ?";
		}
		if(testid!=null && !testid.equalsIgnoreCase("")) {
			prescribedMrnos = prescribedMrnos +" AND TP.TEST_ID=?";
		}

		if(sortFeild == null){
			prescribedMrnos = prescribedMrnos + "ORDER BY  TP.PRES_DATE DESC";
		}
		else if(sortFeild.equalsIgnoreCase("mrno")){
				if(sortOrder!=null && sortOrder.equalsIgnoreCase("true"))
					prescribedMrnos = prescribedMrnos + "order by TP.MR_NO asc";
				else
					prescribedMrnos = prescribedMrnos + "order by TP.MR_NO desc";
		}else if(sortFeild.equalsIgnoreCase("testname")){
				if(sortOrder!=null && sortOrder.equalsIgnoreCase("true"))
					prescribedMrnos = prescribedMrnos + "order by TP.TEST_NAME asc";
				else
					prescribedMrnos = prescribedMrnos + "order by TP.TEST_NAME desc";
		}
		int i = 1;

		ps = con.prepareStatement(prescribedMrnos);
		if(mrno!=null && !mrno.equalsIgnoreCase("")) {
			ps.setString(i++, mrno);
		}
		if(department!=null && !department.equalsIgnoreCase("")) {
			ps.setString(i++, department);
		}
		if(testid!=null && !testid.equalsIgnoreCase("")) {
			ps.setString(i++, testid);
		}

		testList = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return testList;
	}

	private static final String GETALL_PRESCRIPEDTESTS = "SELECT  DISTINCT TP.TEST_ID AS TESTID, " +
		" D.TEST_NAME AS TESTNAME, dd.ddept_id as DEPARTMENT " +
		" FROM TESTS_PRESCRIBED TP  JOIN DIAGNOSTICS D ON D.test_id = TP.test_id" +
		" JOIN diagnostics_departments dd on dd.ddept_id = D.ddept_id " +
		" WHERE  dd.category = ? order by D.TEST_NAME";

	public List getTestNames(String category)throws SQLException{
      List testList = null;
	  try (Connection con = DataBaseUtil.getConnection();
	      PreparedStatement ps = con.prepareStatement(GETALL_PRESCRIPEDTESTS)) {
  		ps.setString(1, category);
  		testList = DataBaseUtil.queryToArrayList(ps);
	  }
	  return testList;
	}

	private static final String UPDATE_OUT_HOUSE_SELECTED_FLAG =
			"UPDATE tests_prescribed SET is_outhouse_selected = 'Y' WHERE prescribed_id=?";


	public static boolean insertSample(Connection con, SampleCollection sc, int centerId)
			throws SQLException,IOException {

		logger.debug("Inserting sample for presid " + sc.getPrescribedId()
				+ " " + sc.getSampleDate());
		boolean status = false;
		PreparedStatement ps = null;
		BasicDynaBean sampleBean = null;
		GenericDAO sampleDAO = new GenericDAO("sample_collection");
		GenericDAO diagnosticsDAO = new GenericDAO("diagnostics");
		BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
		boolean autogenerate = "Y".equals((String)diagGenericPref.get("autogenerate_sampleid"));
		String noGeneration = (String)diagGenericPref.get("sample_no_generation");
		boolean isBatchBased = autogenerate && noGeneration.equals("B");

		try {
			//	check whether sample is already collected OR not
			sampleBean = sampleDAO.findByKey("sample_collection_id", sc.getSampleSequence());

			BasicDynaBean diagMasterBean  = diagnosticsDAO.findByKey("test_id", sc.getTestId());
			String houseStatus = getHouseStatus(con,sc.getTestId(),centerId);
			houseStatus = houseStatus.equals("t")?"O":"I";

			if (sampleBean == null && diagMasterBean.get("sample_needed").equals("y") && sc.getSampleSequence() != 0) {
				logger.debug("New Sample required");
				do {
					status = DiagnosticsDAO.insertSamples(con, sc, isBatchBased);
					if (!status)
						break;
				} while (false);

				/*
				 * Tests which does't have sample and conducted out side for
				 * those tests we are inserting in the sample collection table
				 * and in outhose sample details table and updating
				 * is_outhouse_selected column to 'Y' for our internal reference
				 */

			} else if (sampleBean == null && diagMasterBean.get("sample_needed").equals("n")
					&& (houseStatus.equals("O"))) {

				do {
					ps = con.prepareStatement(UPDATE_OUT_HOUSE_SELECTED_FLAG);
					ps.setInt(1, sc.getPrescribedId());

					status = ps.executeUpdate() > 0;
				} while (false);

			} else if(sampleBean != null && diagMasterBean.get("sample_needed").equals("y")){
				do {
					Map<String,Object> values = new HashMap<String, Object>();
					values.put("sample_status", sc.getSampleStatus());
					values.put("sample_type_id", sc.getSampleTypeId());
					values.put("sample_source_id", sc.getSampleSourceId());
					values.put("sample_qty", sc.getSampleQty());
					if (sc.getSampleStatus().equals("C"))
						values.put("sample_date", sc.getSampleDate());

					status = ( sampleDAO.update(con, values, "sample_collection_id", sc.getSampleSequence()) > 0 );

					if (!status)
						break;

				} while (false);

			} else {
				// for the test those does not require sample and test conducted
				// inside the hospital

				status = true;
			}
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
		return status;
	}

	public static String getHouseStatus(Connection con,String testId,int centerId)throws SQLException{
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("SELECT is_outhouse_test(?,?)");
			ps.setString(1, testId);
			ps.setInt(2, centerId);
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally{
			if(ps!=null) ps.close();
		}
	}


	private static final String GET_TEMPLATES = "SELECT tf.format_name ,tf.testformat_id FROM  " +
			" test_format tf  join test_template_master ttm on( tf.testformat_id = ttm.format_name" +
			" AND ttm.test_id = ? ) " ;

	private static final String CHECK_TEMPLATES_IN_PRESCRIPTION = "SELECT format_name FROM test_details " +
			" WHERE prescribed_id = ? AND conducted_in_reportformat='Y'";

	private static final String GET_TEMPLATES_IN_PRESCRIPTION =
		"SELECT distinct tf.format_name,tf.testformat_id,td.amendment_reason FROM" +
		" test_format tf join test_details td on (tf.testformat_id = td.format_name " +
		" AND prescribed_id = ?) ";

	public static List<BasicDynaBean> getTemplatesForTest(String testId,String prescribedId) throws SQLException{
		List<BasicDynaBean> l = null;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
    		ps = con.prepareStatement(CHECK_TEMPLATES_IN_PRESCRIPTION);
    		ps.setInt(1,Integer.parseInt(prescribedId));
    		String format = DataBaseUtil.getStringValueFromDb(ps);
    		if(format == null ){
    			ps = con.prepareStatement(GET_TEMPLATES);
    			ps.setString(1, testId);
    			l = DataBaseUtil.queryToDynaList(ps);
    		}else{
    			ps = con.prepareStatement(GET_TEMPLATES_IN_PRESCRIPTION);
    			ps.setInt(1, Integer.parseInt(prescribedId));
    			l = DataBaseUtil.queryToDynaList(ps);
    		}
		} finally {
		  DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}


    private static final String GET_NEW_TEMPLATES = "SELECT tf.format_name ,tf.testformat_id FROM  " +
	" test_format tf  join test_template_master ttm on( tf.testformat_id = ttm.format_name" +
	" AND ttm.test_id = ? ) WHERE " +
	" tf.testformat_id NOT IN(SELECT td.format_name FROM test_details td where prescribed_id=?  AND test_detail_status != 'A' ) " ;

	public static List<BasicDynaBean> getTemplatesAtMasterLevel(String testId,String prescribedId)
	  throws SQLException{
			List<BasicDynaBean> l = null;
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_NEW_TEMPLATES);
				ps.setString(1, testId);
				ps.setInt(2, Integer.parseInt(prescribedId));
				l = DataBaseUtil.queryToDynaList(ps);
			}finally{
				DataBaseUtil.closeConnections(con, ps);
			}
			return l;
	}

	public static List<BasicDynaBean> getTemplatesAtPrescriptionLevel(String prescribedId)
		throws SQLException{
			List<BasicDynaBean> l = null;
			Connection con = null;
			PreparedStatement ps = null;
			try{
				con = DataBaseUtil.getReadOnlyConnection();
				ps = con.prepareStatement(GET_TEMPLATES_IN_PRESCRIPTION);
				ps.setInt(1, Integer.parseInt(prescribedId));
				l = DataBaseUtil.queryToDynaList(ps);
			}finally{
				DataBaseUtil.closeConnections(con, ps);
			}
		return l;
	}


	private static String GET_TESTS_LISTS="select tvp.report_id,tvp.report_name,d.test_name,"+
    " case when tvp.report_date IS NOT NULL then 'Y' else 'N' end as report_data ,tp.prescribed_id,tp.test_id," +
    " tp.conducted,tvp.signed_off,tvp.handed_over,tvp.report_addendum,tvp.addendum_signed_off, " +
    " tp.labno, tp.pres_date, tp.common_order_id,tvp.report_state, dom.outsource_dest_type, dom.outsource_dest, " +
    " sc.sample_status, tp.sflag, tp.mr_no, isr.incoming_visit_id, isr.incoming_source_type, tp.revision_number, " +
    " (SELECT 'O' FROM diag_outsource_detail dod " +
    " WHERE dod.test_id = d.test_id AND dod.source_center_id = COALESCE(pr.center_id,isr.center_id) AND dod.status = 'A' LIMIT 1) AS outhouse" +
	" from diagnostics d " +
	" join tests_prescribed tp on tp.test_id=d.test_id" +
    " join diagnostics_departments dd on dd.ddept_id = d.ddept_id  and dd.category = ? " +
	" left join test_visit_reports tvp on tvp.report_state != 'D' " +
	" and tvp.report_id=tp.report_id " +
	" LEFT JOIN outsource_sample_details osd ON(osd.prescribed_id = tp.prescribed_id)" +
	" LEFT JOIN diag_outsource_master dom ON(dom.outsource_dest_id = osd.outsource_dest_id)" +
	" LEFT JOIN sample_collection sc ON(sc.sample_collection_id = tp.sample_collection_id)" +
	" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id)" +
	" LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)" +
	" WHERE tp.pat_id=?  and tp.conducted NOT IN ('U','X','NRN','CRN') " ;
	private static String WHERE_GET_TESTS_LISTS=" and (sample_needed='n' or (sample_needed='y' and sflag='1')) "+
	" order by tvp.report_id ";
	private static String WHERE_ASSERT_GET_TESTS_LISTS=" and (sample_needed='n' or (sample_needed='y' and sflag='1' and  sc.sample_status ='A') or signed_off ='Y') "+
	" order by tvp.report_id ";

	public static List<Hashtable<String, String>> getTestsLists(String visitId,String category)
		throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		ArrayList<Hashtable<String, String>> testsLists = null;
		try {
    		con = DataBaseUtil.getReadOnlyConnection();
    		int index = 1;
    		boolean sampleAssertion = "Y".equals((String)GenericPreferencesDAO.getdiagGenericPref().get("sample_assertion"));
    			String gTestsList = GET_TESTS_LISTS;
    			if(!sampleAssertion)
    				gTestsList = gTestsList+WHERE_GET_TESTS_LISTS;
    			else
    				gTestsList = gTestsList+WHERE_ASSERT_GET_TESTS_LISTS;
    
    		ps = con.prepareStatement(gTestsList);
    		ps.setString(index++, category);
    		ps.setString(index++, visitId);
    		testsLists = DataBaseUtil.queryToArrayList(ps);
		} finally {
		  DataBaseUtil.closeConnections(con, ps);
		}
		return testsLists;
	}


	public static final String GET_PRES_LIST = "SELECT tp.prescribed_id FROM tests_prescribed tp " +
						" left join bill b on (b.visit_id=tp.pat_id) and b.bill_type='P' and b.status='C' " +
						" left join bill_activity_charge  bac on (bac.activity_id=tp.prescribed_id::varchar) and bac.activity_code=?" +
						" left join bill_charge bc on bc.charge_id=bac.charge_id and b.bill_no=bc.bill_no" +
						" join test_visit_reports tvr on (tvr.report_id = tp.report_id) where tvr.report_id=?"+

						" union" +

						" SELECT tp.prescribed_id FROM tests_prescribed tp " +
						" left join bill b on (b.visit_id=tp.pat_id) and b.bill_type !='P' and b.restriction_type='N' " +
						" left join bill_activity_charge  bac on  (bac.activity_id=tp.prescribed_id::varchar) and bac.activity_code=?" +
						" left join bill_charge bc on (bc.charge_id=bac.charge_id) and (b.bill_no=bc.bill_no)" +
						" join test_visit_reports tvr on (tvr.report_id = tp.report_id) where tvr.report_id=?" ;

	public static ArrayList<String> getTestsListsForReportId(String reportId,String category)throws SQLException{
		ArrayList<String> al = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			logger.debug(GET_PRES_LIST);
			ps = con.prepareStatement(GET_PRES_LIST);
			String chargeHead = null;

			chargeHead = "DIA";
			ps.setString(1, chargeHead);
			ps.setInt(2,Integer.parseInt(reportId));
			ps.setString(3, chargeHead);
			ps.setInt(4,Integer.parseInt(reportId));
			al = DataBaseUtil.queryToOnlyArrayList(ps);
			return al;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}
	public static final String PRESCRIPTIONS_LIST =
		"SELECT tp.*, tc.conducted_by, tc.technician FROM tests_prescribed tp JOIN tests_conducted tc USING (prescribed_id) WHERE report_id=?";
	public static List getTestList(int reportId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(PRESCRIPTIONS_LIST);
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	public static boolean updateTechnician(String technician, int prescribedId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("UPDATE tests_conducted set technician=? WHERE prescribed_id=?");
			ps.setString(1, technician);
			ps.setInt(2, prescribedId);
			return ps.executeUpdate() > 0;
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String UPDATE_REPORT_WITH_NAME = "UPDATE test_visit_reports SET report_data = ? , " +
		"report_name = ? , report_date = current_timestamp WHERE report_id = ?";

	public static boolean  editReport(Connection con,TestVisitReports tvr)throws SQLException{
		PreparedStatement ps = null;
		int result = 0;
		boolean status = false;
		try{

			ps = con.prepareStatement(UPDATE_REPORT_WITH_NAME );
			ps.setString(1, tvr.getReportData());
			ps.setString(2, tvr.getReportName());
			ps.setInt(3, tvr.getReportId());
			result = ps.executeUpdate();
			if(result>0) status = true;
		}finally{
			if(ps != null) ps.close();
		}
		return status;
		}


	private static final String GET_IMAGE_NAMES = "SELECT title,image_name FROM test_images where prescribed_id = ? ";
	public static final List getImageDetails(int prescribedId)throws SQLException{
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
    	  con = DataBaseUtil.getReadOnlyConnection();
    	  ps = con.prepareStatement(GET_IMAGE_NAMES);
    	  ps.setInt(1, prescribedId);
    	  l = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	private static final String GET_REPORT_IMAGE_NAMES =
		"SELECT title,image_name " +
		"	FROM test_images " +
		"JOIN tests_prescribed USING(prescribed_id)" +
		"WHERE report_id = ? ";
	public static final List getReportImageDetails(int reportId)throws SQLException{
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
		  con = DataBaseUtil.getReadOnlyConnection();
		  ps = con.prepareStatement(GET_REPORT_IMAGE_NAMES);
		  ps.setInt(1, reportId);
		  l = DataBaseUtil.queryToDynaList(ps);
		} finally {
		  DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}


	private static final String GET_UPLOADED_FILES = "SELECT id,title,file_name,file_type FROM test_report_files Where report_id =?";
	public static final List getUploadedFiles(int reportId)throws SQLException{
		List l = new ArrayList();
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_UPLOADED_FILES);
			ps.setInt(1, reportId);
			l = DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return l;
	}

	private static final String UPLOAD_IMAGE = "INSERT INTO test_images(prescribed_id,image,title,image_name,doc_type) " +
			" VALUES(?,?,?,?,?)  ";

	public static boolean uploadImage(Connection con, TestReportImages tri)throws SQLException, IOException{
		boolean status = false;
		ByteArrayInputStream bio = null;
		try (PreparedStatement ps = con.prepareStatement(UPLOAD_IMAGE)) {
  		  FormFile myFile = tri.getImage();
          String fileName    = myFile.getFileName();
          byte[] fileData    = myFile.getFileData();
  
          bio = new ByteArrayInputStream(fileData);
          ps.setInt(1, tri.getPrescribedId());
          ps.setBinaryStream(2, bio,bio.available());
          ps.setString(3, tri.getTitle());
          ps.setString(4, fileName);
          ps.setString(5, tri.getDocType());
  
          int i = ps.executeUpdate();
  
          if(i>0)status = true;
          bio.close();
		}
		return status;
	}


	private static final String DELETE_IMAGE = "DELETE FROM  test_images WHERE prescribed_id = ? and " +
			" title =?";
	public static boolean deleteImage(Connection con, TestReportImages trm)throws SQLException{
		boolean status = false;
		try (PreparedStatement ps = con.prepareStatement(DELETE_IMAGE);) {
		  int index = 1;
    	  ps.setInt(index++, trm.getPrescribedId());
    	  ps.setString(index++, trm.getTitle());
    	  int i = ps.executeUpdate();
    	  if(i>0) status = true;
		}
		return status;
	}


	private static final String GET_IMAGE_BYTES = "SELECT image FROM test_images WHERE prescribed_id = ? AND " +
			"  title =? ";
	public static byte[] getImageBytes( TestReportImages trm)throws SQLException{
		byte[] image = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
		  int index = 1;
    	  con = DataBaseUtil.getReadOnlyConnection();
    	  ps = con.prepareStatement(GET_IMAGE_BYTES);
    	  ps.setInt(index++, trm.getPrescribedId());
    	  ps.setString(index++, trm.getTitle());
    
    	  rs = ps.executeQuery();
    	  if(rs!=null && rs.next()) {
    	    image = rs.getBytes(1);
    	  }
		} finally {
		  DataBaseUtil.closeConnections(con, ps, rs);
		}

		return image;
	}

	public static InputStream getImageStream(int prescribedId, String title) throws SQLException {
		InputStream is = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
		  con = DataBaseUtil.getReadOnlyConnection();
		  int index = 1;
		  ps = con.prepareStatement(GET_IMAGE_BYTES);
		  ps.setInt(index++, prescribedId);
		  ps.setString(index++, title);
		  rs = ps.executeQuery();
		  if (rs!=null && rs.next()) {
			is = rs.getBinaryStream(1);
		  }
		} finally {
		  DataBaseUtil.closeConnections(con, ps, rs);
		}

		return is;
	}

	private static final String GET_IMAGE_BYTES_FOR_REPORT =
		"SELECT image FROM test_images WHERE ( prescribed_id = ? OR prescribed_id = (" +
		" (SELECT prescribed_id FROM tests_prescribed WHERE report_id = ? LIMIT 1) ) )AND " +
		"  title =? ";

	public static InputStream getImageStream(int prescribedId,int reportId, String title) throws SQLException {
		InputStream is = null;
		ResultSet rs = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
		  con = DataBaseUtil.getReadOnlyConnection();
		  int index = 1;
		  ps = con.prepareStatement(GET_IMAGE_BYTES_FOR_REPORT);
		  ps.setInt(index++, prescribedId);
		  ps.setInt(index++, reportId);
		  ps.setString(3, title);

		  rs = ps.executeQuery();
		  if (rs!=null && rs.next()) {
			is = rs.getBinaryStream(1);
		  }
		} finally {
		  DataBaseUtil.closeConnections(con, ps, rs);
		}
		return is;
	}



	private static final String GET_SAMPLE_COLLECTIONLIST =
		"(SELECT tp.mr_no,tp.pat_id,tp.prescribed_id,tp.sflag, tp.conducted,sc.sample_sno,sc.sample_date, "+
		" d.type_of_specimen,stm.sample_type_id,tp.test_id,tp.labno,  d.test_name,dd.ddept_name,dd.ddept_id, "+
		" d.test_id, CASE WHEN sc.outsource_dest_id is not null THEN 'O' ELSE 'I' END AS house_status," +
		" dom.outsource_dest_id,osm.sample_no as outhouse_sampleno, "+
		" sc.sample_status,osm.test_id as oh_test_id,pr.collection_center_id,  stp.sample_prefix,stp.start_number as start_number," +
		" d.sample_collection_instructions,COALESCE(st.sample_container, stm.sample_container) AS sample_container,"+
		" tp.pres_date,source_name, sample_source_id, sc.sample_qty, d.dependent_test_id, tp.common_order_id," +
		" depd.test_name as dependent_test_name, st.sample_type, sc.sample_collection_id,stm.sample_type as master_sample_type," +
		" st.sample_type_id as sc_sample_type_id, COALESCE(om.oh_name, hcm.center_name) AS out_source_name, tp.priority, pr.center_id "+
		" FROM tests_prescribed tp "+
		" LEFT JOIN sample_collection sc on (tp.sample_collection_id = sc.sample_collection_id) "+
		" LEFT JOIN sample_sources on(source_id = sample_source_id) "+
		" LEFT JOIN outsource_sample_details osm using(prescribed_id) "+
		" LEFT JOIN diag_outsource_master dom on(dom.outsource_dest_id = sc.outsource_dest_id) "+
		" LEFT JOIN outhouse_master om on(om.oh_id = dom.outsource_dest) " +
		" LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) "+
		" JOIN diagnostics d on (d.test_id = tp.test_id) and d.sample_needed = 'y' "+
		" LEFT JOIN sample_type st on (sc.sample_type_id=st.sample_type_id)  "+
		" LEFT JOIN sample_type stm on (d.sample_type_id=stm.sample_type_id) AND stm.status = 'A' " +
		" LEFT JOIN sample_type_number_prefs stp ON (stp.sample_type_id = st.sample_type_id) AND stp.center_id = 0"+
		" JOIN diagnostics_departments dd on d.ddept_id = dd.ddept_id   "+
		" LEFT JOIN bill_activity_charge bac on (bac.activity_id=tp.prescribed_id::varchar) "+
		"	AND bac.activity_code = 'DIA' "+
		" LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id "+
		" LEFT JOIN bill bp ON(bp.bill_no=bc.bill_no AND bp.visit_id = tp.pat_id AND bp.bill_type = 'P' ) "+
		" LEFT JOIN bill b ON( b.bill_no=bc.bill_no AND b.visit_id = tp.pat_id AND b.bill_type = 'C' )  "+
		" LEFT JOIN patient_registration pr on(pr.patient_id = tp.pat_id) "+
		" LEFT JOIN diagnostics depd ON(depd.test_id = d.dependent_test_id) "+
		" WHERE ";


	private static final String  WHERE_CONSTRAINT_WITH_ALL_TESTS=
			" tp.pat_id = ? " +
			" and tp.conducted  NOT IN ('X','U') AND dd.category='DEP_LAB'  " +
			" and COALESCE(bp.payment_status,'P') = 'P'  and " +
			" (b.payment_status in ('P','U') or b.payment_status is null)  " +
			" order by tp.prescribed_id,type_of_specimen)";


	public static List getSampleCollectionList(String visitId, boolean sampleFlow,String testId)throws SQLException{
		List l = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			if(sampleFlow){
				String samplesList = GET_SAMPLE_COLLECTIONLIST;
				if(testId != null)
					samplesList = samplesList+" tp.test_id = ? AND "+WHERE_CONSTRAINT_WITH_ALL_TESTS;
				else
					samplesList = samplesList+WHERE_CONSTRAINT_WITH_ALL_TESTS;

				ps = con.prepareStatement(samplesList);
				logger.debug(samplesList);
			  int index = 1;
			  if(testId != null){
				ps.setString(index++, testId);
				ps.setString(index++, visitId);
			  }else{
				ps.setString(index++, visitId);
			  }
			  l = DataBaseUtil.queryToDynaList(ps);
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}

		return l;
	}


	private static final String SEND_SAMPLES = "INSERT INTO  OUTSOURCE_SAMPLE_DETAILS(visit_id,outsource_dest_id, "+
		                  " sample_no,test_id,prescribed_id)" +
			                "VALUES(?,?,?,?,?)";

	public static boolean setSamplesToOuthouse(OutHouseSampleDetails osd,Connection con)throws SQLException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			
			ps = con.prepareStatement(SEND_SAMPLES);
			ps.setString(1, osd.getVisitId());
			ps.setInt(2, Integer.parseInt(osd.getoutSourceId()));
			ps.setString(3, osd.getSampleNo());
			ps.setString(4, osd.getTestId());
			ps.setInt(5, osd.getPrescribedId());
			int i = ps.executeUpdate();
	
			if(i>0){
				status = true;
			}
	
			return status;
		} finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String UPDATE_OUTHOUSE_SAMPLES =
		" UPDATE outsource_sample_details SET visit_id= ?, " +
		" 	outsource_dest_id = ?,sample_no = ?,test_id = ? " +
		" WHERE prescribed_id = ? ";

	public static boolean updateSamplesToOuthouse(OutHouseSampleDetails osd,Connection con)throws SQLException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(UPDATE_OUTHOUSE_SAMPLES);
			ps.setString(1, osd.getVisitId());
			ps.setInt(2, Integer.parseInt(osd.getoutSourceId()));
			ps.setString(3, osd.getSampleNo());
			ps.setString(4, osd.getTestId());
			ps.setInt(5, osd.getPrescribedId());
			int i = ps.executeUpdate();
	
			if(i>0){
				status = true;
			}
	
			return status;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	/*
	 * Get a list of reports for the given patient ID. Note that we guess the doctor name as the first
	 * non-null doctor for all the prescribed tests that are there in this report.
	 */
	private static String EMR_CONDUCT_TESTS =
		"SELECT DISTINCT tvr.report_id, tvr.report_name, tvr.category, tvr.report_date, tvr.report_mode,tvr.user_name, " +
		"  (SELECT doc.doctor_name FROM tests_prescribed tp " +
		"  left join doctors doc on doc.doctor_id = tp.pres_doctor" +
		"  WHERE tp.report_id=tvr.report_id AND " +
		"  tp.pres_doctor IS NOT NULL AND tp.pres_doctor!= '' LIMIT 1) AS pres_doctor, " +
		"	(SELECT textcat_commacat(d.test_name) as test_names from diagnostics d " +
		"		JOIN tests_prescribed tp ON (d.test_id=tp.test_id)" +
		"	 WHERE tp.report_id=tvr.report_id) as test_names, tp.pat_id AS patient_id,pr.reg_date" +
		" FROM test_visit_reports tvr " +
		"	JOIN tests_prescribed tp ON (tp.report_id = tvr.report_id)" +
		"	JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) @ " +
		" WHERE tvr.report_state != 'D' ";

	private static final String TEST_DOCUMENTS_EMR = "SELECT d.test_name, doc_name, td.doc_id, tp.prescribed_id, td.result_status," +
			"		td.username, doc_date, tp.pat_id, dd.category, doc.doctor_name, pd.doc_format, pd.doc_location,pr.reg_date " +
			"	FROM test_documents td " +
			"		JOIN patient_documents pd ON (td.doc_id=pd.doc_id) " +
			"		JOIN tests_prescribed tp ON (td.prescribed_id=tp.prescribed_id) " +
			"       JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
			"		JOIN diagnostics d ON (tp.test_id=d.test_id) " +
			"		JOIN diagnostics_departments dd ON (d.ddept_id=dd.ddept_id) " +
			"		LEFT JOIN doctors doc ON (tp.pres_doctor=doc.doctor_id) " ;
			
	
	public static final String GET_TEST_EXTERNALREPORT = "SELECT textcat_commacat(test_name) as test_names,patient_id from " +
								" (select d.test_name,tp.pat_id as patient_id " +
								" FROM tests_prescribed tp " +
								" JOIN diagnostics d ON (d.test_id=tp.test_id)" +
								" JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)" ;

	public static List<EMRDoc> getConductedTestsListForEMR(String patientId, String mrNo, Boolean allVisitsDocs)
			throws SQLException {
		return getConductedTestsListForEMR(patientId, mrNo, allVisitsDocs, false);
	}
	public static List<EMRDoc> getConductedTestsListForEMR(String patientId, String mrNo, Boolean allVisitsDocs, boolean isPatientView)
		throws SQLException {

		List<EMRDoc> docs = new ArrayList<EMRDoc>();
		List<BasicDynaBean> l = null;

		BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
		String showSignedOff = ((String) genPrefs.get("show_tests_in_emr")).equals("S") || isPatientView ? " AND signed_off='Y' " : "";
		String EMRConductQuery=EMR_CONDUCT_TESTS;
		String diagJoin=" JOIN diagnostics d ON (tp.test_id=d.test_id) ";
		if(isPatientView){
			EMRConductQuery=EMRConductQuery.replace("@", diagJoin);
			showSignedOff+= " AND d.isconfidential=false ";
		}else{
			EMRConductQuery=EMRConductQuery.replace("@", "");
		}
		if (allVisitsDocs) {
			l = DataBaseUtil.queryToDynaList(EMRConductQuery + showSignedOff +" AND pr.mr_no=? ORDER BY report_date DESC", mrNo);
		} else {
			l = DataBaseUtil.queryToDynaList(EMRConductQuery + showSignedOff +" AND tp.pat_id=? ORDER BY report_date DESC", patientId);
		}
		
		BasicDynaBean radPrintPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD);
		BasicDynaBean labPrintPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG);
		if(isPatientView){
			getEMRTests(docs,l,radPrintPref,labPrintPref,true);
			return docs;
		}
		getEMRTests(docs,l,radPrintPref,labPrintPref,false);
		// documents which are added to test.
		List<BasicDynaBean> documentsList = null;
		if (allVisitsDocs) {
			documentsList = DataBaseUtil.queryToDynaList(TEST_DOCUMENTS_EMR + " WHERE tp.mr_no=? AND tp.conducted not in ('RAS','RBS', 'X') ORDER BY doc_date desc ", mrNo);
		} else {
			documentsList = DataBaseUtil.queryToDynaList(TEST_DOCUMENTS_EMR + " WHERE tp.pat_id=? AND tp.conducted not in ('RAS','RBS', 'X') ORDER BY doc_date desc ", patientId);
		}
		for (BasicDynaBean bean: documentsList) {
			EMRDoc sdoc = new EMRDoc();
			sdoc.setDocid((Integer) bean.get("doc_id")+"");
			sdoc.setVisitid((String) bean.get("pat_id"));
			if (bean.get("result_status") != null && bean.get("result_status").equals("P")) {
			  sdoc.setTitle("("+bean.get("result_status")+") - "+"supporting document for " +bean.get("test_name")+" : "+bean.get("doc_name").toString());
			} else {
			  sdoc.setTitle("supporting document for " +bean.get("test_name")+" : "+bean.get("doc_name").toString());
			}
			String displayUrl = "";
			if (bean.get("category").equals("DEP_LAB")) {
				sdoc.setType("SYS_LR");
				displayUrl = "/Laboratory/TestDocumentsPrint.do?_method=print&doc_id="+bean.get("doc_id");
			} else {
				sdoc.setType("SYS_RR");
				displayUrl = "/Radiology/TestDocumentsPrint.do?_method=print&doc_id="+bean.get("doc_id");
			}
			if (bean.get("doc_format").equals("doc_link")) {
				sdoc.setDisplayUrl((String) bean.get("doc_location"));
				sdoc.setExternalLink(true);
			} else {
				sdoc.setDisplayUrl(displayUrl);
			}
			sdoc.setDoctor((String) bean.get("doctor_name"));
			sdoc.setAuthorized(true);
			sdoc.setDate((java.sql.Date) bean.get("doc_date"));
			sdoc.setUpdatedBy((String) bean.get("username"));
			sdoc.setVisitDate((java.sql.Date) bean.get("reg_date"));
			sdoc.setProvider(EMRInterface.Provider.DIAGProvider);
			docs.add(sdoc);
		}
		// To get External report link at visit level for lab tests.
		List<BasicDynaBean> externalReportsList = null;
		if (allVisitsDocs) {
			externalReportsList = DataBaseUtil.queryToDynaList(GET_TEST_EXTERNALREPORT + "  WHERE tp.external_report_ready = 'Y' AND tp.mr_no=? order by tp.prescribed_id) as foo GROUP BY patient_id", mrNo);
		} else {
			externalReportsList = DataBaseUtil.queryToDynaList(GET_TEST_EXTERNALREPORT + "  WHERE tp.external_report_ready = 'Y' AND tp.pat_id=? order by tp.prescribed_id) as foo GROUP BY patient_id", patientId);
		}
		for (BasicDynaBean testbean : externalReportsList){
				EMRDoc edoc = new EMRDoc();
				edoc.setDocid("-1");
				edoc.setVisitid((String) testbean.get("patient_id"));
				edoc.setDescription((String) testbean.get("test_names"));
				edoc.setProvider(EMRInterface.Provider.DIAGProvider);
				edoc.setTitle("External report - ("+(String) testbean.get("patient_id")+")");
				edoc.setType("SYS_LR");
				String displayUrl = "/doctor/EMRMainDisplay.do?_method=getexternalreport&forcePdf=true&_external_visit_id="+patientId;
				edoc.setAuthorized(true);
				edoc.setDisplayUrl(displayUrl);
				edoc.setPdfSupported(true);
				docs.add(edoc);
			}		
		return docs;
	}

	public static final String GET_OHTEST_CHARGEID = "SELECT bc.charge_id FROM bill_charge bc JOIN "+
		"  bill USING (bill_no) JOIN bill_activity_charge bac ON  bac.charge_id=bc.charge_id "+
		" WHERE activity_id= ? and bac.activity_code=? ";

	public static String getOhTestChargeId(Connection con, int prescribedId,
			String charge_head) throws SQLException {
		PreparedStatement ps = null;
		try {
			String presId=new Integer(prescribedId).toString();
			ps = con.prepareStatement(GET_OHTEST_CHARGEID);
			ps.setString(1, presId);
			ps.setString(2, "DIA");
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			ps.close();
		}
	}

	private static String GET_ALL_TESTS_PRESCRIBED_FIELDS = "SELECT tp.mr_no,tp.pat_id,tp.test_id,d.test_name,tp.prescribed_id," +
			" tp.conducted,pd.patient_name  as NAME, isr.patient_name as incomingpatientname, isr.incoming_visit_id," +
			" pd.last_name as lastName, s.salutation, CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital";
	private static String GET_ALL_TESTS_PRESCRIBED_COUNT = "SELECT COUNT(tp.prescribed_id)";

	private static String GET_ALL_TESTS_PRESCRIBED = " FROM tests_prescribed tp "
			+ " JOIN diagnostics d USING (test_id)"
			+ " JOIN diagnostics_departments dd USING (ddept_id)"
			+ " LEFT JOIN patient_registration pr  on (tp.pat_id=pr.patient_id)"
			+ " LEFT JOIN patient_details pd  on (pr.mr_no=pd.mr_no)"
			+ " LEFT JOIN incoming_sample_registration isr on tp.pat_id = isr.incoming_visit_id"
			+ " LEFT JOIN salutation_master s on (s.salutation_id = pd.salutation)"
			+ " LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name)"
			+ " LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)"
			+ " LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id) "
			+ " LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)";

	 private static final String ALL_TESTS_INIT_WHERE = "WHERE ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) ) ";
	 
	public PagedList getAllTestDetails(String mrno, String department, String testName,
			int pageNum, List patient, Date fromDate, Date toDate, String sortFeild,
			boolean sortReverse,String category,boolean showOnlyHouseTests,
			boolean showOnlyInhouseTests, List<String> testStatus) throws SQLException{
			/*The following things should be consider whenever we change this function
			 *
			 * 1)Order of display should be the order of prescription.
			 * 2)visit should be appear in dashboard until all reports are signed off corresponding to the category.
			 * 3)zero chage activities are will not posted to bill, should be use left join.
			 * 4)laboratory dashboard should not depend on radiology dashboard vice versa.
			 *
			 * */

			PreparedStatement ps = null;
			Connection con = null;
			List<BasicDynaBean> testList = null;
			List<String> visitList = new ArrayList<String>();
			int totalCount = 0;

			try{
				con =  DataBaseUtil.getReadOnlyConnection();
				SearchQueryBuilder qb = null;

				qb = new SearchQueryBuilder(con,GET_ALL_TESTS_PRESCRIBED_FIELDS, GET_ALL_TESTS_PRESCRIBED_COUNT,
						GET_ALL_TESTS_PRESCRIBED, ALL_TESTS_INIT_WHERE, sortFeild, sortReverse, 20, pageNum);

				qb.addFilter(SearchQueryBuilder.STRING, "tp.mr_no", "=", mrno);
				qb.addFilter(SearchQueryBuilder.STRING, "d.ddept_id", "=", department);
				qb.addFilter(SearchQueryBuilder.STRING, "d.test_name", "ilike", testName);
				qb.addFilter(SearchQueryBuilder.STRING, "pr.visit_type", "IN", patient);
				qb.addFilter(SearchQueryBuilder.DATE,   "tp.pres_date::date", ">=", fromDate);
				qb.addFilter(SearchQueryBuilder.DATE,   "tp.pres_date::date", "<=", toDate);
				qb.addFilter(SearchQueryBuilder.DATE,   "tp.pres_date::date", ">=", fromDate);
				qb.addFilter(SearchQueryBuilder.DATE,   "tp.pres_date::date", "<=", toDate);
				qb.addFilter(SearchQueryBuilder.STRING, "tp.conducted", "IN", testStatus);
				qb.addFilter(SearchQueryBuilder.STRING, "dd.category", "=", category);

				if(showOnlyHouseTests)
					qb.addFilter(SearchQueryBuilder.STRING, "d.house_status", "=", "O");
				if(showOnlyInhouseTests)
					qb.addFilter(SearchQueryBuilder.STRING, "tp.prescription_type", "=", "i");

				qb.build();

				PreparedStatement psData = qb.getDataStatement();
				PreparedStatement psCount = qb.getCountStatement();

				testList = DataBaseUtil.queryToDynaList(psData);
				totalCount =  Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));

				psData.close(); psCount.close();

			}finally{
				if(con != null) con.close();
				if(ps != null) ps.close();
			}

		return new PagedList(testList, totalCount, 20, pageNum);
	}

	public static String TEST_AUDIT_LOG_FIELDS = "select al.log_id, al.datetime, al.username, al.db_table, al.operation, " +
			" al.db_record_id,al.db_field_name, al.db_field_old_val, al.db_field_new_val,doc.doctor_name " ;

	private static String TEST_AUDIT_LOG_COUNT = "SELECT count(*)";
	public static PagedList getTestAuditLog(int pageNum, String prescribedId)throws SQLException {

	Connection con = DataBaseUtil.getReadOnlyConnection();

	String TEST_AUDIT_LOG_TABLES = "from audit_logs al "+
			" LEFT JOIN doctors doc on doc.doctor_id = al.db_field_new_val " +
			" WHERE (db_table = 'tests_prescribed' and db_record_id ='"+prescribedId+"') " +
			" OR (db_table = 'sample_collection' and db_record_id IN " +
				" (SELECT pprescribed_id::character varying FROM sample_collection t1 WHERE t1.pprescribed_id = '"+prescribedId+"')) " +
			" OR (db_table = 'tests_conducted' and db_record_id IN " +
				" (SELECT prescribed_id::character varying FROM tests_conducted t1 WHERE t1.prescribed_id = '"+prescribedId+"')) " +
			" OR (db_table = 'test_details' and db_record_id IN " +
				" (SELECT id::character varying FROM test_details t1 WHERE t1.prescribed_id = '"+prescribedId+"')) " +
			" OR (db_table = 'test_visit_reports' and db_record_id IN " +
				" (SELECT report_id::character varying FROM test_visit_reports t1 WHERE t1.report_id = " +
					" (SELECT report_id FROM tests_prescribed WHERE prescribed_id = '"+prescribedId+"'))) " ;


		SearchQueryBuilder qb =
			new SearchQueryBuilder(con,TEST_AUDIT_LOG_FIELDS,TEST_AUDIT_LOG_COUNT,
					TEST_AUDIT_LOG_TABLES,null,null,"al.datetime",false,20,pageNum);//here 10 number records per page

		qb.build();
		PreparedStatement psData = qb.getDataStatement();
		PreparedStatement psCount = qb.getCountStatement();

		ArrayList auditLogList = (ArrayList) DataBaseUtil.queryToDynaList(psData);
		int count = Integer.parseInt((DataBaseUtil.getStringValueFromDb(psCount)));

		psData.close();
		psCount.close();
		con.close();

		return new PagedList(auditLogList,count,20,pageNum);
	}

	public static boolean updateStock(Connection con,int itemId,String identifier,BigDecimal qty)throws SQLException{
		   PreparedStatement ps = null;
		   boolean status = false;
		   try{
			   ps = con.prepareStatement("UPDATE STORE_STOCK_DETAILS SET QTY=QTY-?,CHANGE_SOURCE='Diag Reagent usage' WHERE DEPT_ID='-1' AND MEDICINE_ID=? AND BATCH_NO=?");
			   ps.setBigDecimal(1,qty);
			   ps.setInt(2, itemId);
			   ps.setString(3, identifier);
			   if(ps.executeUpdate() > 0)status = true;
		   }finally{
			   if(ps != null)ps.close();
		   }
		   return status;
	   }

	public static List<BasicDynaBean> getReagentDetails(String testId)throws SQLException{

		PreparedStatement ps = null;
		Connection con = null;
		List<BasicDynaBean> l;
		try{
			con = DataBaseUtil.getConnection();
			String reagentlist = "select sum(qty) as qty,medicine_id ,medicine_name,quantity_needed  from store_stock_details "
                               +" join store_item_details using(medicine_id)"
                               +" join diagnostics_reagents dr on reagent_id=medicine_id"
                               +" where dept_id= -1 and test_id=? and dr.status='A'"
                               +" group by medicine_name,medicine_id,quantity_needed";
			ps = con.prepareStatement(reagentlist);
			ps.setString(1,testId);
			l = DataBaseUtil.queryToDynaList(ps);
		}finally{
			if(ps!=null)ps.close();
			if(con!=null)con.close();
		}

		return l;
	}

	private static final String INSERT_TEST_PRESCRIPTIONS_FOR_RECONDUCTION_WITHXISTINGSAMPLE = "INSERT INTO tests_prescribed" +
			"( mr_no,pat_id,test_id,pres_date,pres_doctor,conducted,sflag," +
			"  prescribed_id,user_name,package_name,prescription_type," +
			"  labno,re_conduction,reference_pres,common_order_id,remarks,sample_no,sample_collection_id, token_number,package_ref," +
			"  outsource_dest_id, outsource_dest_prescribed_id, curr_location_presc_id, coll_prescribed_id, " +
			"  source_test_prescribed_id,exp_rep_ready_time, clinical_notes, his_prescribed_id)" +
			"  SELECT mr_no,pat_id,test_id,?,pres_doctor,'N',?," +
			"  ?,user_name,package_name,prescription_type, labno,true,?,?,remarks,?,sample_collection_id,?,package_ref," +
			"  outsource_dest_id, outsource_dest_prescribed_id, ?, coll_prescribed_id, source_test_prescribed_id, " +
			"  exp_rep_ready_time, clinical_notes, his_prescribed_id " +
			"FROM tests_prescribed" +
			" WHERE prescribed_id=?";

	private static final String INSERT_TEST_PRESCRIPTIONS_FOR_RECONDUCTION_WITHNEWSAMPLE = "INSERT INTO tests_prescribed" +
			"( mr_no,pat_id,test_id,pres_date,pres_doctor,conducted,sflag," +
			"  prescribed_id,user_name,package_name,prescription_type," +
			"  labno,re_conduction,reference_pres,common_order_id,remarks,sample_no, token_number,package_ref," +
			"  outsource_dest_id, curr_location_presc_id, source_test_prescribed_id,exp_rep_ready_time, clinical_notes, his_prescribed_id )" +
			"  SELECT mr_no,pat_id,test_id,?,pres_doctor,'N',?," +
			"  ?,user_name,package_name,prescription_type, labno,true,?,?,remarks,?,?,package_ref," +
			"  outsource_dest_id, ?, source_test_prescribed_id,exp_rep_ready_time, clinical_notes, his_prescribed_id " +
			" FROM tests_prescribed" +
			" WHERE prescribed_id=?";


	private static final String UPDATE_TEST_CONDUCTION = "UPDATE tests_conducted SET satisfactory_status = ? " +
			" WHERE prescribed_id = ?";

	public static boolean saveReconductionDetails(Connection con,Map requestMap,int commonOrderId)
	throws SQLException,IOException{
		boolean status = true;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;

		try{
			String prescribedId[] = (String[])requestMap.get("prescribedId");
			String[] recodncutionReason = (String[])requestMap.get("reconducted_reason");
			String[] markedForReconduction = (String[])requestMap.get("marked_for_reconduction");
			String[] sampleState = (String[])requestMap.get("sampleState");
			String conductedBY = null;
			Map<String, Object> columndata = new HashMap<String, Object>();
			columndata.put("is_outhouse_selected", "Y");

			java.sql.Timestamp date = DateUtil.getCurrentTimestamp();
			ps1 = con.prepareStatement(UPDATE_TEST_CONDUCTION);
			BasicDynaBean testPresBean = null,tvrBean = null,outSourceBean = null, testCondBean = null, incomingSampleRegBean;
			GenericDAO outDAO = new GenericDAO("outsource_sample_details");
			GenericDAO testCondDAO = new GenericDAO("tests_conducted");
			GenericDAO incomingSampleRegDAO = new GenericDAO("incoming_sample_registration");

			for(int i=0;i<prescribedId.length;i++){
				if(markedForReconduction[i].equals("Y")){

					if(anyUnconductedRefTests(con,Integer.parseInt(prescribedId[i]) ))
						return false;
						if(sampleState[i].equals("N"))
							ps = con.prepareStatement(INSERT_TEST_PRESCRIPTIONS_FOR_RECONDUCTION_WITHNEWSAMPLE);
						else
							ps = con.prepareStatement(INSERT_TEST_PRESCRIPTIONS_FOR_RECONDUCTION_WITHXISTINGSAMPLE);

						testPresBean = tpDAO.findByKey("prescribed_id", Integer.parseInt(prescribedId[i]) );
						incomingSampleRegBean = incomingSampleRegDAO.findByKey("incoming_visit_id", testPresBean.get("pat_id"));
						testCondBean = testCondDAO.findByKey("prescribed_id", Integer.parseInt(prescribedId[i]));
						if (testCondBean != null && testCondBean.get("conducted_by") != null && !testCondBean.get("conducted_by").equals(""))
							conductedBY = (String)testCondBean.get("conducted_by");
						Object outSourceDestPrescID = testPresBean.get("outsource_dest_prescribed_id");
						tvrBean = tvrDAO.findByKey("report_id", testPresBean.get("report_id"));
						int newPresId = DataBaseUtil.getIntValueFromDb("select nextval('test_prescribed')");
						ps.setTimestamp(1, date);
						if( sampleState[i].equals("N") ||  sampleState[i].equals("NA") )
							ps.setString(2, "0");
						else
							ps.setString(2, "1");
						ps.setBigDecimal(3, new BigDecimal(newPresId));
						ps.setBigDecimal(4, new BigDecimal(Integer.parseInt(prescribedId[i])));
						ps.setInt(5, commonOrderId);
						if(sampleState[i].equals("N"))
							ps.setString(6, null);
						else
							ps.setString(6, (String)testPresBean.get("sample_no"));
						BasicDynaBean refbean = getPrescribedDetails(Integer.parseInt(prescribedId[i]));
						Integer token = DeptTokenGeneratorDAO.getToken((String) refbean.get("ddept_id"), (Integer) refbean.get("center_id"));

						if ( token == null )
							ps.setNull(7,Types.INTEGER);
						else
							ps.setInt(7, token);
						ps.setInt(8, newPresId);
						ps.setBigDecimal(9, new BigDecimal(Integer.parseInt(prescribedId[i])));
						
						status &= ps.executeUpdate() > 0;

						 ps1.setString(1, "N");
						 ps1.setInt(2,  (Integer)testPresBean.get("curr_location_presc_id"));

						 status &= ps1.executeUpdate() > 0;

						 //update conducted status to  RAS==>Reconduction After Signoff,RBS==>Reconduction Before Signoff
						 testPresBean.set("conducted",tvrBean == null || tvrBean.get("signed_off").equals("N") ? "RBS":"RAS");
						 testPresBean.set("new_test_prescribed_id", newPresId);
						 testPresBean.set("reconducted_reason", recodncutionReason[i]);
						 tpDAO.update(con, testPresBean.getMap(), "prescribed_id",testPresBean.get("prescribed_id"));

						 outSourceBean = outDAO.findByKey(con, "prescribed_id", testPresBean.get("prescribed_id"));

						 if ( outSourceBean != null && sampleState[i].equals("E") ) {
							 outSourceBean.set("prescribed_id", newPresId);
							 status &= outDAO.insert(con, outSourceBean);
							 //Need to set flag as Y for the column is_outhouse_selected in tests_prescribed table
							 status &= tpDAO.update(con, columndata, "prescribed_id", newPresId)>0;
						 }
						 //update bill activity to new activity id
						 status &= new BillActivityChargeDAO(con).updateActivityId(testPresBean.get("prescribed_id").toString(),
								 BillActivityCharge.DIAG_ACTIVITY_CODE, String.valueOf(newPresId));

						 String userName = RequestContext.getUserName();
						 //update activity_conducted to N in bill_charge,bill_activity_charge
						 BillActivityChargeDAO.updateActivityDetails(
								 con, BillActivityCharge.DIAG_ACTIVITY_CODE, String.valueOf(newPresId), conductedBY,"N",null, userName);

						 if (outSourceDestPrescID != null && !outSourceDestPrescID.equals(""))
						 	status &= updateReconductStatusForConductionCenter(con, recodncutionReason[i], outSourceDestPrescID, tvrBean);

						 if (null != incomingSampleRegBean) {
							 status &= updateReconductedPrescIdToCollectionCenter(con, testPresBean, newPresId);
							 status &= updateNewprescidForIncomingTest(con, (Integer)testPresBean.get("prescribed_id"), newPresId);
						 }
						 
						 // copy the test documents to the new prescription
						 status = status & new TestDocumentAbstractImpl().copyTestDocuments(con, 
								 (Integer) testPresBean.get("prescribed_id"), newPresId);

					}
			}


		}catch(Exception e){
			logger.error("", e);
			return false;
		}
		return status;
	}

	private static boolean updateReconductStatusForConductionCenter(Connection con,
			String recodncutionReason, Object outSourceDestPresID, BasicDynaBean tvrBean) throws SQLException,IOException {

		List<BasicDynaBean> lists = new ArrayList<BasicDynaBean>();
		lists.add(tpDAO.findByKey(con, "outsource_dest_prescribed_id", outSourceDestPresID));
		Map<String, Object> columndata = new HashMap<String, Object>();
		columndata.put("conducted", (tvrBean == null || tvrBean.get("signed_off").equals("N")) ? "RBS" : "RAS");
		columndata.put("new_test_prescribed_id", null);
		columndata.put("reconducted_reason", recodncutionReason);
		return LaboratoryBO.copyDataToMultipleChains(con, lists, columndata, "outsource_dest_prescribed_id", null);
		
	}

	private static final String GET_COLLECTED_SAMPLE_LIST=
		"  SELECT  sc.sample_sno,d.test_name,sc.patient_id, d.test_name AS d_test_name        " +
		"    ,sc.sample_date,st.sample_type, COALESCE(sc.coll_sample_no, sc.sample_sno) as coll_sample_no " +
		"    FROM sample_collection sc            " +
        "    JOIN tests_prescribed tp on(sc.sample_collection_id = tp.sample_collection_id)  " +
        "    JOIN diagnostics d using(test_id)                    " +
        "    LEFT JOIN sample_type st on (st.sample_type_id=d.sample_type_id)     " +
        "    WHERE tp.sflag='1' and tp.conducted not in('NRN','CRN') and sc.patient_id=?               " +
        "    GROUP BY sc.sample_sno, sc.coll_sample_no, d.test_name,sc.patient_id,sc.sample_date,st.sample_type     " +
        "    ORDER BY sc.sample_sno";

	public static List getCollectedSampleList(String visitId) throws SQLException {

     PreparedStatement ps=null;
     Connection con=DataBaseUtil.getReadOnlyConnection();
     List<Hashtable> newList = new ArrayList<Hashtable>();
     ps=con.prepareStatement(GET_COLLECTED_SAMPLE_LIST);
     ps.setString(1, visitId);
     List collectedSampleList=DataBaseUtil.queryToArrayList(ps);
     Iterator<Hashtable> it = collectedSampleList.iterator();

     String sampleNo="";
     String prevSampleNo=null;
     String grouptestNames="";
     String innerSampleNo="";
    while (it.hasNext()) {
		Hashtable groupIt = it.next();
		sampleNo =(String)groupIt.get("SAMPLE_SNO");
		if(sampleNo.equals(prevSampleNo))
		continue;
		prevSampleNo=(String)groupIt.get("SAMPLE_SNO");
		grouptestNames="";
		groupIt.put("SAMPLE_SNO",sampleNo );
		groupIt.put("SAMPLE_DATE",(String)groupIt.get("SAMPLE_DATE") );
		groupIt.put("SAMPLE_TYPE", (String)groupIt.get("SAMPLE_TYPE"));
		 Iterator<Hashtable> it1 = collectedSampleList.iterator();
		  while (it1.hasNext()) {
			  Hashtable testGroupIt = it1.next();
			  innerSampleNo=(String)testGroupIt.get("SAMPLE_SNO");
			  if(sampleNo.equals(innerSampleNo)){
			  grouptestNames=grouptestNames+(String)testGroupIt.get("TEST_NAME")+',';
			  }
		  }
		  grouptestNames=grouptestNames.substring(0, grouptestNames.length()-1);
		  groupIt.put("TEST_NAME",grouptestNames);
		newList.add(groupIt);
    }
      DataBaseUtil.closeConnections(con, ps);
		return newList;
	}

	private static final String MRNO_VISITID = "SELECT pr.mr_no, tvr.patient_id FROM patient_registration pr "
		+ " LEFT OUTER JOIN test_visit_reports tvr ON pr.patient_id=tvr.patient_id "
		+ " WHERE report_id=?";
	public static BasicDynaBean getMrNoVisitId(int reportId) throws SQLException{
		List list = DataBaseUtil.queryToDynaList(MRNO_VISITID, reportId);
		if (list != null && !list.isEmpty())
			return (BasicDynaBean) list.get(0);
		return null;
	}

	public static final String GET_OUTSOURCE_TESTS = "SELECT d.test_id, d.test_name, dod.outsource_dest_id, "+
		" case when outsource_dest_type='O' then om.oh_name else hcm.center_name end as outsource_name, "+
		" dod.source_center_id as outsource_center, pr.center_id as patient_center, tp.prescribed_id, dom.outsource_dest "+
		" FROM diagnostics d "+
		" JOIN tests_prescribed tp ON (tp.test_id=d.test_id) "+
		" LEFT JOIN patient_registration pr ON (tp.pat_id=pr.patient_id) "+
		" JOIN diagnostics_departments dd USING(ddept_id) "+
		" LEFT JOIN diag_outsource_detail dod ON (dod.test_id = d.test_id) "+
		" LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = dod.outsource_dest_id) "+
		" LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest) "+
		" LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) "+
		" LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=tp.pat_id)"+
		" LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)" +
		" WHERE (d.sample_needed='n' OR (d.sample_needed ='y' AND pr.patient_id IS NULL)) "+
		" AND dod.outsource_dest_id is not null AND d.conduction_applicable "+
		" AND (sc.sample_receive_status IS NULL OR sc.sample_receive_status = 'R')"+
		" AND tp.is_outhouse_selected='N' AND dd.category=? AND tp.conducted!='X' AND "+
		" tp.pat_id=?  ";

	public static List getOutsourceTestList(String visitId, String category)
			throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		con = DataBaseUtil.getReadOnlyConnection();
		int max_centers_inc_default = (Integer) GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default");
		if (max_centers_inc_default > 1)
			ps = con.prepareStatement(GET_OUTSOURCE_TESTS + "  AND dod.source_center_id=coalesce(pr.center_id, isr.center_id) ORDER BY tp.prescribed_id ");
		else
			ps = con.prepareStatement(GET_OUTSOURCE_TESTS + " ORDER BY tp.prescribed_id ");

		ps.setString(1, category);
		ps.setString(2, visitId);
		List outSourceTestLsit = DataBaseUtil.queryToArrayList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return outSourceTestLsit;
	}

	public static final String CHECK_IS_TEMPLATE_THERE="SELECT td.mr_no FROM test_details td " +
	                                    "JOIN tests_prescribed tp USING(prescribed_id) " +
	                                    "WHERE tp.conducted='N' AND tp.prescribed_id=?";

	public boolean getIstemplatethere(String prescribedId)
			throws NumberFormatException, SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		ResultSet rs = null;
		boolean target = false;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(CHECK_IS_TEMPLATE_THERE);
			ps.setInt(1, Integer.parseInt(prescribedId));
			rs = ps.executeQuery();
			if (rs.next()) {
				target = true;
			}
			return target;
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}
		
	}

	private static final String GET_DIAG_PROGRESSIVE_DETAILS=
		 "(SELECT  CASE WHEN  dd.category='DEP_LAB' THEN 'Laboratory' else ''  END AS ddept_name, "
	   +   " to_char(tp.pres_date,'yyyy-MM-dd') AS period,COUNT(tp.test_id) as test_count "
	   + 	"FROM tests_prescribed tp "
	   +   		"JOIN diagnostics using(test_id) "
	   +   		"JOIN diagnostics_departments dd using(ddept_id) "
	   + 	"WHERE date(tp.pres_date) BETWEEN ? AND ? "
	   + 	"AND tp.conducted !='X' and category='DEP_LAB' "
	   + 	"GROUP BY dd.category,to_char(tp.pres_date,'yyyy-MM-dd') "
	   + 	"ORDER BY to_char(tp.pres_date,'yyyy-MM-dd'),dd.category) "
	   + 	"UNION "
	   + "(SELECT  dd.ddept_name,to_char(tp.pres_date,'yyyy-MM-dd') AS period,COUNT(tp.test_id) as test_count "
	   +  	"FROM tests_prescribed tp "
	   + 		 "JOIN diagnostics using(test_id) "
	   +	 	 "JOIN diagnostics_departments dd using(ddept_id) "
	   +   "WHERE date(tp.pres_date) BETWEEN ? AND ? "
	   +   "AND tp.conducted !='X' and category='DEP_RAD' "
	   +   "GROUP BY dd.ddept_name,to_char(tp.pres_date,'yyyy-MM-dd') "
	   +   "ORDER BY to_char(tp.pres_date,'yyyy-MM-dd'),dd.ddept_name) ";

	public static List<BasicDynaBean> getDiagProgressiveDetails(Date fromDate, Date toDate) throws SQLException {

		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		ps = con.prepareStatement(GET_DIAG_PROGRESSIVE_DETAILS);
		ps.setDate(1, fromDate);
		ps.setDate(2, toDate);
		ps.setDate(3, fromDate);
		ps.setDate(4, toDate);
		List diagProgressiveList = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return diagProgressiveList;
	}
	private static final String diag_reagents_used =
		"SELECT reagent_id,prescription_id,test_id,usage_no,qty,i.medicine_name as item_name,'true' as status FROM diagnostic_reagent_usage  dru " +
			"JOIN store_item_details i ON(i.medicine_id = dru.reagent_id) WHERE prescription_id = ? ";
	public static List<BasicDynaBean> getDiagReagentsUsed(int prescriptionId)throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(diag_reagents_used);
			ps.setInt(1, prescriptionId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_OUT_SOURCE_SAMPLE_DETAILS=" SELECT  sc.mr_no,sc.patient_id,d.test_name,d.test_id, COALESCE(sc.coll_sample_no,sc.sample_sno)AS sample_sno, "+
		 " d.conduction_format, CASE WHEN sc.outsource_dest_id is not null THEN 'O' ELSE 'I' END AS house_status, "+
		 " COALESCE (get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name), "+
		 " isr.patient_name)  AS patient_full_name, "+
		 " case when pd.patient_gender='M' then 'Male' WHEN  pd.patient_gender='F' then 'Female' when isr.patient_gender='M' then 'Male' "+
		 " WHEN  isr.patient_gender='F' then 'Female' end as patient_gender, "+
		 " get_patient_age(pd.dateofbirth, pd.expected_dob,isr.isr_dateofbirth,isr.patient_age) as age, "+
		 " get_patient_age_in(pd.dateofbirth, pd.expected_dob,isr_dateofbirth,isr.age_unit) AS agein, "+
		 " case when dom.outsource_dest_type IN ('O', 'IO') THEN om.oh_name ELSE hcm.center_name END AS outsource_name, "+
		 " om.clia_no,om.oh_address,doc.doctor_name,osd.outsource_dest_id, tp.prescribed_id, ppd.member_id as member_id,sppd.member_id as sec_member_id, bc.act_rate_plan_item_code," +
		 " (select mrd.icd_code  from mrd_diagnosis mrd where (mrd.visit_id=sc.patient_id and mrd.diag_type='P')) as primary_diag_icd_code, "+
		 " (select textcat_linecat( md.icd_code ) from mrd_diagnosis md where (md.visit_id=sc.patient_id and md.diag_type='S') "+
		 "	GROUP BY md.visit_id) as secondary_diag_icd_code  "+
		 " FROM sample_collection  sc "+
		 " JOIN tests_prescribed tp on tp.sample_collection_id=sc.sample_collection_id "+
		 " JOIN outsource_sample_details osd on(osd.test_id=tp.test_id "+
		 "  	and osd.prescribed_id=tp.prescribed_id) "+
		 " JOIN diagnostics d on d.test_id=tp.test_id "+
		 " JOIN diag_outsource_master dom on (dom.outsource_dest_id = osd.outsource_dest_id) "+
		 " LEFT JOIN outhouse_master om on(om.oh_id = dom.outsource_dest) "+
		 " LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) "+
		 " LEFT join patient_details pd on (pd.mr_no=sc.mr_no) "+
		 " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = sc.patient_id) "+
		 " LEFT JOIN doctors doc on(doc.doctor_id=tp.pres_doctor) "+
		 " LEFT JOIN patient_insurance_plans ppip ON( ppip.patient_id = sc.patient_id AND ppip.priority = 1) "+
		 " LEFT JOIN patient_insurance_plans spip ON( spip.patient_id = sc.patient_id AND spip.priority = 2) "+
		 " LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A' AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id) "+
		 " LEFT JOIN patient_policy_details sppd ON (sppd.mr_no = ppip.mr_no and sppd.status = 'A' AND sppd.patient_policy_id = spip.patient_policy_id and spip.plan_id = sppd.plan_id) "+
		 " LEFT JOIN bill_activity_charge bac on (bac.activity_id = tp.prescribed_id::varchar AND bac.activity_code='DIA') "+
		 " LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id"+
		 " WHERE  tp.sflag='1'  and d.sample_needed='y' "+
		 " and sc.patient_id= ? and osd.outsource_dest_id= ? and  ";


	public static List<BasicDynaBean> getOutSourceSampleDetails(String visitId, String prescribedIds, String outSourceDestId, String sampleNo) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> outHouseSampleList = null;
		String whereClause=" sc.sample_sno=? order by tp.prescribed_id ";
		con = DataBaseUtil.getConnection();

		ps = con.prepareStatement(GET_OUT_SOURCE_SAMPLE_DETAILS+whereClause);
		ps.setString(1, visitId);
		ps.setInt(2, Integer.parseInt(outSourceDestId));
		ps.setString(3, sampleNo);
		outHouseSampleList=DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);
		return outHouseSampleList;
	}

	private static String GET_TEST_RESULT_LABELS="SELECT resultlabel FROM test_results_master WHERE test_id=? order by display_order ";

	public static HashMap getTestResultLables(List<BasicDynaBean> outSourceSampleDetails, String outSourceNames) throws SQLException {

		String outSources[] = outSourceNames.split(",");
		String outSourceId = null;
		HashMap testWiseResultLabels = new HashMap();
		PreparedStatement ps = null;
		Connection con = null;
		con = DataBaseUtil.getReadOnlyConnection();

		ps = con.prepareStatement(GET_TEST_RESULT_LABELS);
		for (int i = 0; i < outSources.length; i++) {
			outSourceId = outSources[i];
			for (int k = 0; k < outSourceSampleDetails.size(); k++) {
				BasicDynaBean outHSampleDetails = (BasicDynaBean) outSourceSampleDetails
						.get(k);
				if (outHSampleDetails.get("outsource_dest_id").equals(outSourceId)) {
					ps.setString(1, (String) outHSampleDetails.get("test_id"));
					testWiseResultLabels.put(outHSampleDetails.get("prescribed_id").toString(), DataBaseUtil.queryToArrayList1(ps));

				}

			}

		}
		DataBaseUtil.closeConnections(con, ps);

		return testWiseResultLabels;
	}

	public static Map getOutHouseSamplePatientDetailsDetails(String visitId, String prescribedIds, String outSourceDestId, String sampleNo) throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		Map outHousePatientSampleMap = new HashMap();
		BasicDynaBean outHousePatientSample = null;
		String whereClause=" sc.sample_sno=? order by tp.prescribed_id ";
		con = DataBaseUtil.getConnection();
		outSourceDestId.trim();
		ps = con.prepareStatement(GET_OUT_SOURCE_SAMPLE_DETAILS+whereClause);
		ps.setString(1, visitId);
		ps.setInt(2, Integer.parseInt(outSourceDestId));
		ps.setString(3, sampleNo);
		outHousePatientSample = DataBaseUtil.queryToDynaBean(ps);
		DataBaseUtil.closeConnections(con, ps);
		if (outHousePatientSample != null)
			outHousePatientSampleMap = outHousePatientSample.getMap();
		return outHousePatientSampleMap;
	}

	public static final String GET_OUTSOURCE_SAMPLE_DETAILS=
		" SELECT tp.mr_no,tp.pat_id,dom.outsource_dest_id, "+
		" CASE WHEN dom.outsource_dest_type='O' THEN om.oh_name ELSE hcm.center_name END AS outsource_name, "+
		" COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_no,osm.outsource_dest_id, "+
		" textcat_commacat(osm.prescribed_id::varchar) as prescribed_id, "+
		" textcat_commacat(d.test_name) as test_name, sc.sample_sno AS current_center_sample_no "+
		" FROM tests_prescribed tp "+
		" JOIN diagnostics d using (test_id) "+
		" JOIN outsource_sample_details osm USING(prescribed_id) "+
		" JOIN diag_outsource_master dom ON(dom.outsource_dest_id = osm.outsource_dest_id) "+
		" LEFT JOIN outhouse_master om on (om.oh_id=dom.outsource_dest) "+
		" LEFT JOIN hospital_center_master hcm ON (hcm.center_id::text = dom.outsource_dest) "+
		" LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id) " +
		" WHERE osm.visit_id= ? AND tp.sample_no != ? AND tp.conducted not in ('NRN','CRN') "+
		" GROUP BY tp.sample_no,tp.mr_no,tp.pat_id,dom.outsource_dest_id,om.oh_name, "+
		" osm.outsource_dest_id,outsource_dest_type,hcm.center_name, sc.coll_sample_no, sc.sample_sno ";


	public static List<BasicDynaBean> getOutHouseSourceList(String visitId)
			throws SQLException {

		Connection con = null;
		PreparedStatement ps = null;
		List<BasicDynaBean> outSourceSamplelist = null;
		con = DataBaseUtil.getReadOnlyConnection();
		ps = con.prepareStatement(GET_OUTSOURCE_SAMPLE_DETAILS);
		ps.setString(1, visitId);
		ps.setString(2, visitId);
		outSourceSamplelist = DataBaseUtil.queryToDynaList(ps);
		DataBaseUtil.closeConnections(con, ps);

		return outSourceSamplelist;
	}

	/*
	 * Update the visit ID of tests, given a bill number and the old/new visit IDs.
	 * This is used for OP to IP conversion, when some tests need to be delinked from the old
	 * visit and associated with the new visit.
	 *
	 * Since a report contains multiple tests, it is ambiguous as to whether the report is also
	 * to be associated with the new visit, if there are multiple tests, each with a different
	 * action (ie, one test is to be moved, but another is not). for this purpose, the caller
	 * uses excludeReports as a boolean, whereby, reports are retained with the old visit.
	 * All new tests will have to be re-associated with reports and signed off again if they
	 * had originally been signed off.
	 */
	public static final String UPDATE_VISIT_ID =
		"FROM bill_activity_charge bac, bill_charge bc " +
		"WHERE (bac.activity_id::integer = t.prescribed_id AND activity_code = 'DIA') " +
		"  AND (bac.charge_id = bc.charge_id) " +
		"  AND bill_no=?";

	public static final String UPDATE_TP_VISIT_ID =
		"UPDATE tests_prescribed t SET pat_id=? " + UPDATE_VISIT_ID;

	public static final String UPDATE_TC_VISIT_ID =
		"UPDATE tests_conducted t SET patient_id=? " + UPDATE_VISIT_ID;

	public static final String UPDATE_TD_VISIT_ID =
		"UPDATE test_details t SET patient_id=? " + UPDATE_VISIT_ID;

	public static final String UPDATE_SC_VISIT_ID =
		" UPDATE sample_collection s SET patient_id=? "+
		" FROM bill_activity_charge bac  "+
		" JOIN bill_charge bc USING(charge_id)  "+
		" JOIN tests_prescribed t ON (t.prescribed_id = bac.activity_id::integer AND activity_code = 'DIA')  "+
		" WHERE  (bac.charge_id = bc.charge_id AND s.sample_collection_id = t.sample_collection_id)   AND bill_no=?  ";

	public static void updateVisitId(Connection con, String billNo, String oldVisit, String newVisit)
		throws SQLException {

		DataBaseUtil.executeQuery(con, UPDATE_TP_VISIT_ID, newVisit, billNo);
		DataBaseUtil.executeQuery(con, UPDATE_TC_VISIT_ID, newVisit, billNo);
		DataBaseUtil.executeQuery(con, UPDATE_TD_VISIT_ID, newVisit, billNo);
		DataBaseUtil.executeQuery(con, UPDATE_SC_VISIT_ID, newVisit, billNo);
	}

	private static final String GET_VISIT_REPORT_COUNTS =
		"SELECT report_id, " +
		" (SELECT count(*) FROM tests_prescribed tp " +
		"   WHERE tp.report_id = tvr.report_id AND tp.pat_id = tvr.patient_id) as same_visit_count, " +
		" (SELECT count(*) FROM tests_prescribed tp " +
		"   WHERE tp.report_id = tvr.report_id AND tp.pat_id != tvr.patient_id) as other_visit_count " +
		"FROM test_visit_reports tvr " +
		"WHERE patient_id=?";

	public static final String COPY_TEST_REPORT_FIELDS =
		" report_name, category, report_date, signed_off, report_mode, user_name, pheader_template_id, " +
		" report_addendum, addendum_signed_off, handed_over, handed_over_to, hand_over_time, " +
		" num_prints, report_state, revised_report_id, report_results_severity_status ";

	public static final String COPY_TEST_REPORT =
		" INSERT INTO test_visit_reports (report_id, patient_id, " + COPY_TEST_REPORT_FIELDS + ") " +
		" SELECT ?, ?, " + COPY_TEST_REPORT_FIELDS +
		" FROM test_visit_reports WHERE report_id=?";

	public static void splitReportIds(Connection con, String oldVisit, String newVisit)
		throws SQLException {

		List<BasicDynaBean> reports = DataBaseUtil.queryToDynaList(con, GET_VISIT_REPORT_COUNTS, oldVisit);
		for (BasicDynaBean report : reports) {
			int oldReportId = (Integer) report.get("report_id");
			long oldVisitCount = (Long) report.get("same_visit_count");
			long newVisitCount = (Long) report.get("other_visit_count");

			if (newVisitCount != 0 && oldVisitCount == 0) {
				// all tests have moved to the new visit. Move the report also to the new one.
				DataBaseUtil.executeQuery(con,
					"UPDATE test_visit_reports SET patient_id=? WHERE report_id=?",
					newVisit, oldReportId);

			} else if (newVisitCount == 0 && oldVisitCount != 0) {
				// all tests have stayed with the old visit. Nothing to do.

			} else {
				/*
				 * split required: create a new report as a copy for the new visit,
				 * then move all the tests from the old report to the new one wherever the
				 * patient ID is different
				 */
				int newReportId = DiagnosticsDAO.getNextReportId();
				DataBaseUtil.executeQuery(con, COPY_TEST_REPORT, new Object[]{newReportId, newVisit, oldReportId});
				DataBaseUtil.executeQuery(con,
						"UPDATE tests_prescribed SET report_id=? WHERE report_id=? AND pat_id=?",
						new Object[]{newReportId, oldReportId, newVisit});
			}
		}
	}

	public static final String collected_samples =
		" SELECT CASE WHEN dom.outsource_dest_type='O' THEN om.oh_name ELSE hcm.center_name "+
		" END AS outsource_name,st.sample_type,d.test_name,sc.sample_sno,sc.sample_status "+
		" FROM sample_collection sc "+
		" JOIN tests_prescribed tp on (tp.sample_collection_id = sc.sample_collection_id) "+
		" LEFT JOIN outsource_sample_details osm ON(sc.sample_sno = osm.sample_no) "+
		" LEFT JOIN diag_outsource_master  dom  ON(dom.outsource_dest_id = osm.outsource_dest_id) "+
		" LEFT JOIN outhouse_master om ON(om.oh_id = dom.outsource_dest) "+
		" LEFT JOIN hospital_center_master hcm ON(hcm.center_id::text = dom.outsource_dest) "+
		" JOIN diagnostics d ON (tp.test_id = d.test_id) "+
		" JOIN sample_type st ON (st.sample_type_id=d.sample_type_id) "+
		" WHERE patient_id =? ";

	public List getCollectedSamples(String patient_id)throws Exception{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(collected_samples);
			ps.setString(1, patient_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public String getAddendumTemplateContent(String reportId) throws Exception{
		String templateContent = null;
		BasicDynaBean testVisitDetails = tvrDAO.findByKey("report_id", Integer.parseInt(reportId));
		if(testVisitDetails.get("report_addendum") == null)
			templateContent = new GenericDAO("test_format").
								findByKey("testformat_id", "ADDENDUM_FORMAT_DEF").get("report_file").toString();
		else
			templateContent = testVisitDetails.get("report_addendum").toString();
		return templateContent;
	}
	
	private static final String INCREAMENT_NUMBER_OF_PRINTS = "UPDATE test_visit_reports SET num_prints = num_prints + 1 " +
			" WHERE report_id = ? ";
	
	public void increaseNumPrints(int reportId)throws Exception{
		Connection con = null;
		PreparedStatement pstmt = null;
		try{
			con = DataBaseUtil.getConnection();
			pstmt = con.prepareStatement(INCREAMENT_NUMBER_OF_PRINTS);
			pstmt.setInt(1, reportId);
			pstmt.executeUpdate();
		}finally{
			DataBaseUtil.closeConnections(con, pstmt);
		}
	}

	public String checkSeviarity(BasicDynaBean refernceResultRangeBean,BigDecimal value)throws SQLException{
		String seviarity = "";

		if(refernceResultRangeBean.get("max_improbable_value") != null && 
				value.doubleValue() > ((BigDecimal)refernceResultRangeBean.get("max_improbable_value")).doubleValue() ){
			seviarity =  Result.IMPROPABLE_HIGH;
		}else if(refernceResultRangeBean.get("max_critical_value") != null &&
				value.doubleValue() > ((BigDecimal)refernceResultRangeBean.get("max_critical_value")).doubleValue() ){
			seviarity =  Result.CRITICAL_HIGH;
		}else if(refernceResultRangeBean.get("max_normal_value") != null &&
				value.doubleValue() > ((BigDecimal)refernceResultRangeBean.get("max_normal_value")).doubleValue() ){
			seviarity =  Result.ABNORMAL_HIGH;
		}else if(refernceResultRangeBean.get("min_improbable_value") != null &&
				value.doubleValue() < ((BigDecimal)refernceResultRangeBean.get("min_improbable_value")).doubleValue() ){
			seviarity =  Result.IMPROBABLE_LOW;
		}else if(refernceResultRangeBean.get("min_critical_value") != null &&
				value.doubleValue() < ((BigDecimal)refernceResultRangeBean.get("min_critical_value")).doubleValue() ){
			seviarity =  Result.CRITICAL_LOW;
		}else if(refernceResultRangeBean.get("min_normal_value") != null &&
				value.doubleValue() < ((BigDecimal)refernceResultRangeBean.get("min_normal_value")).doubleValue() ){
			seviarity =  Result.ABNORMAL_LOW;
		}else if ( refernceResultRangeBean.get("min_normal_value") != null || refernceResultRangeBean.get("max_normal_value") != null){
			seviarity = Result.NORMAL;
		}

		return seviarity;
	}

	public static boolean anyUnconductedRefTests(Connection con,int refPresId)throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement("SELECT * FROM tests_prescribed WHERE reference_pres = ? AND conducted = 'N' ");
			ps.setInt(1, refPresId);
			return DataBaseUtil.queryToDynaBean(ps) != null;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}

	}

	public final String IMPRESSION_DETAILS =
		" SELECT *,thr.impression_details AS histo_impression_details from test_histopathology_results thr " +
		" LEFT JOIN histo_impression_master him  using(impression_id) " +
		" WHERE prescribed_id = ? ";

	public BasicDynaBean getTestImpressionDetails(int prescribedId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con= DataBaseUtil.getConnection();
			ps = con.prepareStatement(IMPRESSION_DETAILS);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public final String MICRO_TEST_DETAILS =
		" SELECT  * FROM test_microbiology_results "+
		" LEFT JOIN test_micro_org_group_details megd USING(test_micro_id)"+
		" LEFT JOIN test_micro_antibiotic_details tmad USING(test_org_group_id) "+
		" LEFT JOIN micro_org_group_master USING(org_group_id) "+
		" LEFT JOIN micro_organism_master mom USING(organism_id) "+
		" LEFT JOIN micro_abst_antibiotic_master maam ON(maam.antibiotic_id = tmad.antibiotic_id "+
		"   AND maam.abst_panel_id=megd.abst_panel_id) "+
		" LEFT JOIN micro_antibiotic_master  mam ON(mam.antibiotic_id = maam.antibiotic_id)"+
		" LEFT JOIN micro_abst_panel_master mapm ON(mapm.abst_panel_id = maam.abst_panel_id) "+
		" LEFT JOIN micro_nogrowth_template_master USING(nogrowth_template_id) "+
		" LEFT JOIN micro_growth_template_master USING(growth_template_id) "+
		" WHERE prescribed_id= ?";

	public List<BasicDynaBean> getTestMicroBiologyDetails(int prescribedId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con= DataBaseUtil.getConnection();
			ps = con.prepareStatement(MICRO_TEST_DETAILS);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public final String CYTO_IMPRESSION_DETAILS =
		" SELECT *,tcr.impression_details AS cyto_impression_details FROM test_cytology_results tcr" +
		" LEFT JOIN histo_impression_master him ON (him.impression_id = tcr.impression_id) " +
		" WHERE tcr.prescribed_id= ? ";

	public List<BasicDynaBean> getTestCytoDetails(int prescribedId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con= DataBaseUtil.getConnection();
			ps = con.prepareStatement(CYTO_IMPRESSION_DETAILS);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String PREVIOUS_RESULTS_FIELDS =
		" SELECT td.resultlabel, td.report_value, td.units, tc.conducted_date, sc.sample_date, tp.report_id, " +
		"	tvr.report_name, tp.prescribed_id, tp.pat_id, tp.mr_no, d.test_name ";
	private static final String PREVIOUS_RESULTS_COUNT = "SELECT count(tp.mr_no) ";
	private static final String PREVIOUS_RESULTS_TABLES =
		" FROM tests_prescribed tp " +
		"	JOIN test_details td ON (tp.prescribed_id=td.prescribed_id) " +
		"	JOIN tests_conducted tc ON (tp.prescribed_id=tc.prescribed_id) " +
		"	JOIN diagnostics d ON (tp.test_id=d.test_id) " +
		"	LEFT JOIN test_visit_reports tvr ON (tp.report_id=tvr.report_id)" +
		"	LEFT JOIN test_format tf ON (td.format_name=tf.testformat_id) " +
		"	LEFT JOIN sample_collection sc ON (tp.pat_id=sc.patient_id and tp.sample_collection_id=sc.sample_collection_id) "+
		"	LEFT JOIN incoming_sample_registration isr ON(isr.incoming_visit_id = tp.pat_id)"
		+" LEFT JOIN patient_details pd ON (pd.mr_no = tp.mr_no)";

	private static final String WHERE_COND = "WHERE coalesce(td.report_value) != '' AND td.test_detail_status != 'A' " +
			" AND tp.conducted NOT IN('RAS','RBS', 'X') AND patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no)";

	// dont show the amended resutls in previous results dialog.
	public PagedList searchPreviousResults(String mrNo, String testDateTime, String resultlabel, String methodId, String resultLblId, Map listingParams)
		throws SQLException, ParseException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			String sortOrder = " tc.conducted_date ";
			Integer pageSize = (Integer) listingParams.get(LISTING.PAGESIZE);
			Integer pageNum = (Integer) listingParams.get(LISTING.PAGENUM);

			if (mrNo == null || mrNo.equals(""))
				return new PagedList(new ArrayList(), 0, pageSize, pageNum);

			qb = new SearchQueryBuilder(con, PREVIOUS_RESULTS_FIELDS, PREVIOUS_RESULTS_COUNT,
					PREVIOUS_RESULTS_TABLES, WHERE_COND, sortOrder, true, pageSize, pageNum);
			qb.addFilter(SearchQueryBuilder.STRING, "tp.mr_no", "=", mrNo);
			qb.addFilter(SearchQueryBuilder.TIMESTAMP, "tc.conducted_date", "<", DateUtil.parseTimestamp(testDateTime));
			qb.addFilter(SearchQueryBuilder.INTEGER, "td.resultlabel_id", "=", Integer.parseInt(resultLblId));

			qb.addSecondarySort("tp.pat_id");
			qb.addSecondarySort("tp.prescribed_id");
			
			qb.build();

			return qb.getMappedPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}

	}

	/**
	 * Useful in case conduction format is not values
	 */
	public static final String TEST_RESULTS_DETAILS=
		" SELECT prescribed_id,test_details_id,test_detail_status," +
		" original_test_details_id,revised_test_details_id,amendment_reason" +
		",report_value,withinnormal " +
		" FROM test_details where ";
	public static BasicDynaBean  getTestDetails(int prescribedId,boolean amended)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(TEST_RESULTS_DETAILS+"prescribed_id = ?  AND test_detail_status != 'A'");
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean  getTestDetails(int testDetailsId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(TEST_RESULTS_DETAILS+" test_details_id =? ");
			ps.setInt(1, testDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static List<BasicDynaBean>  getTemplatesAtPrescriptionLevel(int prescribedId,boolean amended)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_TEMPLATES_IN_PRESCRIPTION+" " +
					"AND ( test_detail_status "+ (amended ? " ='A' )" : " != 'A' OR test_detail_status IS NULl)"));
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String amended_template_query =
		" SELECT (SELECT format_name FROM test_format tf WHERE tf.testformat_id = td.format_name) as format_name,td.amendment_reason " +
		" FROM test_details td   " +
		" WHERE prescribed_id = ? AND test_detail_status = 'A' ";

	public static List<BasicDynaBean > getAmendedTemplates(int prescribedId)throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(amended_template_query);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static BasicDynaBean getPrescribedDetails(int prescribedId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
					" SELECT tp.*, d.test_name, ddept_id, coalesce(pr.center_id, isr.center_id) as center_id ," +
					" CASE WHEN d.sample_needed = 'n' THEN 'U' ELSE tp.sflag END AS sample_status ," +
					" sc.sample_status as sample_collection_status, " +
					" CASE WHEN is_outhouse_test(d.test_id,coalesce(pr.center_id, isr.center_id)::integer) THEN 'O' ELSE 'I' " +
					" END AS house_status, dom.outsource_dest_type, isr.incoming_source_type," +
					" CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital," +
					" sc.sample_transfer_status " +
					" FROM tests_prescribed tp " +
					" 	JOIN diagnostics d ON (d.test_id=tp.test_id) " +
					" 	LEFT JOIN patient_registration pr ON (tp.pat_id=pr.patient_id) " +
					" 	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=tp.pat_id) " +
					"   LEFT JOIN sample_collection sc ON (sc.sample_collection_id = tp.sample_collection_id)" +
					"	LEFT JOIN outsource_sample_details oh ON (oh.prescribed_id = tp.prescribed_id)" +
					"	LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = oh.outsource_dest_id)" +
					" WHERE tp.prescribed_id=? ");
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String TEST_DOCUMENT_FIELDS = "SELECT tp.mr_no, tp.pat_id, td.doc_id, td.doc_name, " +
			" td.doc_date, td.username, td.prescribed_id, pd.doc_format,pd.doc_status, pd.template_id, dv.status, dv.doc_type, " +
			" dv.template_name, dv.specialized, dv.access_rights, pd.doc_location ";

	private static String TEST_DOCUMENT_TABLES =
		" FROM test_documents td " +
		"	JOIN tests_prescribed tp ON (tp.prescribed_id=td.prescribed_id) " +
		"	JOIN patient_documents pd using (doc_id) " +
		"	LEFT JOIN doc_all_templates_view dv USING (doc_format, template_id) ";

	private static String TEST_DOCUMENT_COUNT = "SELECT count(tp.mr_no) ";
	public static PagedList searchTestDocuments(Map listingParams, Map extraParams, Boolean specialized) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		SearchQueryBuilder qb = null;
		try {
			qb = new SearchQueryBuilder(con, TEST_DOCUMENT_FIELDS, TEST_DOCUMENT_COUNT, TEST_DOCUMENT_TABLES, listingParams);
			qb.addFilter(SearchQueryBuilder.INTEGER, "td.prescribed_id", "=", Integer.parseInt((String) extraParams.get("prescribed_id")));
			qb.addSecondarySort("td.doc_id");
			qb.build();

			return qb.getDynaPagedList();
		} finally {
			DataBaseUtil.closeConnections(con, null);
			if (qb != null) qb.close();
		}
	}

	public static List getTestDocuments(String patientId, String category) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			String query = "SELECT td.doc_id, td.doc_name, pm.package_name, d.test_name, tp.common_order_id, " +
					"	d.test_id, td.doc_date, td.username, tp.prescribed_id, tp.clinical_notes, dept.category, d.additional_info_reqts " +
					"   FROM tests_prescribed tp " +
					"	JOIN diagnostics d ON (d.test_id=tp.test_id AND d.mandate_additional_info='O') " +
					"	JOIN diagnostics_departments dept ON (dept.ddept_id=d.ddept_id) " +
					"	LEFT JOIN test_documents td ON (tp.prescribed_id=td.prescribed_id) " +
					"	LEFT JOIN package_prescribed pp ON (pp.prescription_id=tp.package_ref) " +
					"	LEFT JOIN packages pm ON (pm.package_id=pp.package_id) " +
					"	LEFT JOIN patient_documents pd ON (td.doc_id=pd.doc_id AND pd.doc_format='doc_fileupload') " +
					" WHERE tp.pat_id=?	#dept_cat# AND tp.conducted IN ('N', 'P', 'U', 'NRN') " +
					" ORDER BY tp.prescribed_id, td.doc_id ";
			category = category == null ? "" : category;
			query = query.replace("#dept_cat#", (category.isEmpty() ? "" : " AND dept.category=?"));
			ps = con.prepareStatement(query);
			ps.setString(1, patientId);
			if (!category.isEmpty()) {
			  ps.setString(2, category);
			}
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static List<BasicDynaBean> getTestDocuments(int prescId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(
					" SELECT td.doc_id, td.doc_name, pm.package_name, d.test_name, tp.pat_id, tp.common_order_id, " +
					"	d.test_id, td.doc_date, td.username, tp.prescribed_id, tp.clinical_notes, dept.category, " +
					"	hcm.center_name, coalesce(pr.reg_date, isr.date::date) as reg_date, doc.doctor_name, " +
					"	pd.doc_format, pd.doc_location " +
					" 	FROM tests_prescribed tp " +
					"	JOIN diagnostics d ON (d.test_id=tp.test_id AND d.mandate_additional_info='O') " +
					"	JOIN diagnostics_departments dept ON (dept.ddept_id=d.ddept_id) " +
					" 	LEFT JOIN patient_registration pr ON (tp.pat_id=pr.patient_id) " +
					" 	LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id=tp.pat_id) " +
					"	JOIN hospital_center_master hcm ON (hcm.center_id=coalesce(pr.center_id, isr.center_id)) " + 
					"	JOIN test_documents td ON (tp.prescribed_id=td.prescribed_id) " +
					"	LEFT JOIN package_prescribed pp ON (pp.prescription_id=tp.package_ref) " +
					"	LEFT JOIN packages pm ON (pm.package_id=pp.package_id) " +
					"	LEFT JOIN doctors doc ON (tp.pres_doctor=doc.doctor_id) " +
					"	JOIN patient_documents pd ON (td.doc_id=pd.doc_id AND pd.doc_format='doc_fileupload') " +
					" WHERE tp.prescribed_id=? " +
					" UNION ALL " +
					" SELECT td.doc_id, td.doc_name, pm.package_name, d.test_name, tp.pat_id, tp.common_order_id, " +
					"	d.test_id, td.doc_date, td.username, tp.prescribed_id, tp.clinical_notes, dept.category, " +
					"	hcm.center_name, isr.date::date as reg_date, doc.doctor_name,  " +
					"	pd.doc_format, pd.doc_location " +
					" 	FROM tests_prescribed tp " +
					"	JOIN diagnostics d ON (d.test_id=tp.test_id AND d.mandate_additional_info='O') " +
					"	JOIN diagnostics_departments dept ON (dept.ddept_id=d.ddept_id) " +
					"	LEFT JOIN patient_registration pr ON (pr.patient_id=tp.pat_id) " +
					"	LEFT JOIN incoming_sample_registration isr ON (tp.pat_id=isr.incoming_visit_id) " +
					"	JOIN hospital_center_master hcm ON (hcm.center_id=coalesce(pr.center_id, isr.center_id)) " + 
					"	JOIN test_documents td ON (tp.prescribed_id=td.prescribed_id) " +
					"	LEFT JOIN package_prescribed pp ON (pp.prescription_id=tp.package_ref) " +
					"	LEFT JOIN packages pm ON (pm.package_id=pp.package_id) " +
					"	LEFT JOIN doctors doc ON (tp.pres_doctor=doc.doctor_id) " +
					"	JOIN patient_documents pd ON (td.doc_id=pd.doc_id AND pd.doc_format='doc_fileupload') " +
					" WHERE tp.prescribed_id=(select itp.coll_prescribed_id from tests_prescribed itp where itp.prescribed_id=?) " +
					" ORDER BY center_name, prescribed_id, doc_id "
					);
			ps.setInt(1, prescId);
			ps.setInt(2, prescId);
			return DataBaseUtil.queryToDynaList(ps);

		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}



	public static BasicDynaBean getReport(int reportId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT * FROM test_visit_reports WHERE report_id = ? AND signed_off = 'N' AND report_state != 'D'");
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getOriginalReport(int reportId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT report_id FROM test_visit_reports WHERE revised_report_id = ?");
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_ACTIVE_SHORT_IMPRESSION=
		" SELECT * FROM histo_impression_master where status = ? AND short_impression ILIKE ? LIMIT ?";

	public static List findImpressionsActive(String findString, int limit, String status) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        if(limit<=0) limit = 100;
        try {
        	con = DataBaseUtil.getConnection();
        	ps = con.prepareStatement(GET_ACTIVE_SHORT_IMPRESSION);
        	ps.setString(1, "A");
        	ps.setString(2, "%"+findString+"%");
        	ps.setInt(3, limit);
        	return DataBaseUtil.queryToDynaList(ps);
        } finally {
        	DataBaseUtil.closeConnections(con, ps);
        }
    }

	public static final String GET_IMPRESSION_NAME=
		" SELECT short_impression FROM histo_impression_master where short_impression LIKE ?";

	public static String findImpressionsDeatils(String shImpression) throws SQLException {
        Connection con = null;
        PreparedStatement ps = null;
        try {
        	con = DataBaseUtil.getConnection();
        	ps = con.prepareStatement(GET_IMPRESSION_NAME);
        	ps.setString(1, shImpression);
        	return DataBaseUtil.getStringValueFromDb(ps);
        } finally {
        	DataBaseUtil.closeConnections(con, ps);
        }
    }

	private static final String MICRO_ORG_DETAILS =
		"SELECT * FROM test_micro_org_group_details" +
		" JOIN micro_org_group_master USING(org_group_id)" +
		" LEFT JOIN micro_organism_master USING(organism_id)" +
		" WHERE test_micro_id = ? ";

	public static List<BasicDynaBean> getMicroOrgGrpDetails(int microId)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(MICRO_ORG_DETAILS);
			ps.setInt(1, microId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_SAMPLE_BEAN = "SELECT * FROM sample_collection " +
			"WHERE sample_sno = ? AND sample_type_id = ?";

	public static BasicDynaBean isBeanExists(Connection con, String sampleSno, int sampleTypeID)throws SQLException {
		PreparedStatement pstmt = null;

		try {
			pstmt = con.prepareStatement(GET_SAMPLE_BEAN);
			pstmt.setString(1, sampleSno);
			pstmt.setInt(2, sampleTypeID);

			return DataBaseUtil.queryToDynaBean(pstmt);
		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}

	}

	private static final String GET_CONDUCTING_DOCTOR = "SELECT payee_doctor_id FROM tests_prescribed  tp "+
		" LEFT JOIN bill_activity_charge bac on (bac.activity_id = tp.prescribed_id::varchar "+
		" AND bac.activity_code='DIA') "+
		" LEFT JOIN bill_charge bc on  bc.charge_id=bac.charge_id "+
		" LEFT JOIN bill b on b.bill_no=bc.bill_no "+
		" where tp.pat_id=? and bc.charge_group = 'DIA' and prescribed_id=?  ";

	public static String getConductingDoctor(Connection con, int prescID,String visitID) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_CONDUCTING_DOCTOR);
			ps.setString(1, visitID);
			ps.setInt(2, prescID);
			return DataBaseUtil.getStringValueFromDb(ps);
		}finally {
			if(ps!=null) ps.close();
		}
	}

	private static final String GET_DOCTOR_DEPARTMENTS =
		" SELECT d.doctor_id, d.doctor_name, dep.dept_id " +
		" FROM doctors d " +
		"  JOIN department dep ON (d.dept_id = dep.dept_id AND dep.dept_id = ?) " +
		"  JOIN doctor_center_master dcm ON(d.doctor_id = dcm.doctor_id)"+
		" WHERE d.status = 'A' AND (dcm.center_id=? OR dcm.center_id = 0) and dcm.status='A' order by doctor_name";

	public static final List getDoctorDepartmentsDynaList(String category, int centerId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_DOCTOR_DEPARTMENTS);
			ps.setString(1, category);
			ps.setInt(2, centerId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String UPDATE_ACTIVITY_DETAILS =
		" UPDATE bill_activity_charge SET doctor_id=? " +
		" WHERE activity_code=? AND activity_id = ? ";

	public static final String UPDATE_CHARGE_ACTIVITY_DETAILS =
		" UPDATE bill_charge bc SET payee_doctor_id=? " +
		" FROM bill_activity_charge bac " +
		" WHERE bac.activity_code=? AND bac.activity_id = ? " +
		"  AND bc.charge_id = bac.charge_id AND bc.charge_head != 'PKGPKG' ";

	public static final String UPDATE_CONDUCTED_BY =
		"UPDATE tests_conducted SET conducted_by = ? WHERE prescribed_id = ?";

	public static boolean updateActivityDetails(Connection con, String activityCode, String activityId,
			String doctorId) throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_ACTIVITY_DETAILS);
		int i=1;
		ps.setString(i++, doctorId);
		ps.setString(i++, activityCode);
		ps.setString(i++, activityId);
		ps.executeUpdate();
		ps.close();

		i=1;
		ps = con.prepareStatement(UPDATE_CHARGE_ACTIVITY_DETAILS);
		ps.setString(i++, doctorId);
		ps.setString(i++, activityCode);
		ps.setString(i++, activityId);
		ps.executeUpdate();
		ps.close();

		i=1;

		ps = con.prepareStatement(UPDATE_CONDUCTED_BY);
		ps.setString(i++, doctorId);
		ps.setInt(i++, Integer.parseInt(activityId));
		ps.executeUpdate();
		ps.close();

		return true;
	}


	public static final String GET_TEST_DETAILS_LIST = " SELECT COALESCE(sc.coll_sample_no, sc.sample_sno) as sample_no, tp.test_id, d.test_name, sc.assertion_time, " +
							" COALESCE (sc.transfer_user, sc1.transfer_user) AS transfer_user, COALESCE (sc.transfer_time, sc1.transfer_time) AS transfer_time, " +
							" COALESCE (sc.transfer_other_details, sc1.transfer_other_details) AS transfer_other_details, COALESCE(scol.user_name, sc.user_name) AS user_name, " +
							" case when sc.sample_transfer_status = 'T' then nsc.receipt_user else sc.receipt_user end as receipt_user, " + 
							" case when sc.sample_transfer_status = 'T' then nsc.receipt_time else sc.receipt_time end as receipt_time, " +
							" case when sc.sample_transfer_status = 'T' then nsc.receipt_other_details else sc.receipt_other_details end as receipt_other_details, " + 
							" COALESCE(scol.sample_date, sc.sample_date) as sample_date, dd.ddept_name, st.sample_type, " +
							" pr.visit_type, COALESCE (pd.mr_no , isr.mr_no) As mr_no, COALESCE (pd.patient_gender , isr.patient_gender) AS patient_gender, " +
							" get_patient_age(pd.dateofbirth,pd.expected_dob,isr.isr_dateofbirth,isr.patient_age) AS age, " +
							" get_patient_age_in(pd.dateofbirth,pd.expected_dob,isr.isr_dateofbirth,isr.age_unit) AS agein, " +
							" COALESCE (get_patient_full_name(sm.salutation, pd.patient_name, pd.middle_name, pd.last_name), isr.patient_name) AS patient_full_name, " +
							" bn.bed_name AS alloc_bed_name, wn.ward_name AS alloc_ward_name, wnr.ward_name AS reg_ward_name, pr.bed_type AS bill_bed_type, " +
							" COALESCE (pd.patient_phone, isr.phone_no) AS patient_phone, COALESCE (drs.doctor_name, rd.referal_name) AS referal_doc_name, "+
							" pdoc.doctor_name AS prescribing_doctor_name, condoc.doctor_name AS conducting_doctor_name, " +
							" COALESCE (tpc.prescribed_id, tp.prescribed_id) AS prescribed_id, COALESCE(sc.transfer_batch_id, sc1.transfer_batch_id) transfer_batch_id, " +
							" CASE WHEN tp.outsource_dest_prescribed_id IS NOT NULL THEN 'Y' ELSE 'N' END AS cflag, tp.labno, tp.conducted " +
							" FROM sample_collection sc " +
							" JOIN tests_prescribed tp ON (sc.sample_collection_id = tp.sample_collection_id) " +
							" JOIN diagnostics d USING (test_id) " +
							" JOIN diagnostics_departments dd ON (dd.ddept_id = d.ddept_id) " +
							" JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id) " +
							" LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id) " +
							" LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
							" LEFT JOIN ward_names wnr ON (wnr.ward_no = pr.ward_id) " +
							" LEFT JOIN admission ad ON (ad.patient_id = pr.patient_id) " +
							" LEFT JOIN bed_names bn ON (bn.bed_id = ad.bed_id) " +
							" LEFT JOIN ward_names wn ON (wn.ward_no = bn.ward_no) " +
							" LEFT JOIN patient_details pd ON (pd.mr_no = pr.mr_no) " +
							" LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id) " +
							" LEFT JOIN incoming_sample_Registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
							" LEFT JOIN doctors drs ON (drs.doctor_id = COALESCE(pr.reference_docto_id, isr.referring_doctor)) " +
							" LEFT JOIN referral rd ON (rd.referal_no = COALESCE(pr.reference_docto_id, isr.referring_doctor)) " +
							// previous center sample details
							" LEFT JOIN tests_prescribed tp1 ON (tp1.prescribed_id = tp.source_test_prescribed_id) " +
							" LEFT JOIN sample_collection sc1 ON (sc1.sample_collection_id = tp1.sample_collection_id) " +
							// next center sample details
							" LEFT JOIN tests_prescribed tpc ON (tpc.prescribed_id = tp.outsource_dest_prescribed_id) " +
							" LEFT JOIN sample_collection nsc ON (nsc.sample_collection_id=tpc.sample_collection_id) " +
							//prescribing and conducting doctor
							" LEFT JOIN doctors pdoc ON (pdoc.doctor_id = tp.pres_doctor) " +
							" LEFT JOIN LATERAL (" +
							"   select bc.charge_group, bac.doctor_id, bc.payee_doctor_id" +
							"   from bill_charge bc" +
							"   JOIN bill b ON (b.bill_no = bc.bill_no)" +
							"   JOIN bill_activity_charge bac ON ((bc.charge_id = bac.charge_id)  AND (bac.activity_id::int = tp.prescribed_id)" +
							"   AND bac.activity_code = 'DIA')" +
							"    where b.visit_id = tp.pat_id" +
							"  ) b ON true" +
							" LEFT JOIN doctors condoc ON (condoc.doctor_id = COALESCE(b.doctor_id, (case WHEN b" +
							" .charge_group = 'PKG' THEN null ELSE b.payee_doctor_id end)))"+
							// used to get collection center details through any hops
							" LEFT JOIN tests_prescribed tpcol ON (tpcol.prescribed_id = tp.coll_prescribed_id) " +
							" LEFT JOIN sample_collection scol ON (scol.sample_collection_id = tpcol.sample_collection_id) " +
							" WHERE tp.conducted NOT IN ('X','RAS') "+
							" AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) ) "+
							" AND # " ;

	public static List getWorkSheetDetailsList(String patientId, String actionId, String sampleCollectionIds, String bulkWorkSheetPrint) throws SQLException, ParseException {
		Connection con = null;
		PreparedStatement ps = null;
		String[] sampleCollectionIdsArray = null;
		if(sampleCollectionIds != null)
			sampleCollectionIdsArray = sampleCollectionIds.split(",");
		String query = GET_TEST_DETAILS_LIST;
		if(bulkWorkSheetPrint != null && bulkWorkSheetPrint.equals("N")) {
			if(actionId != null && actionId.equalsIgnoreCase("lab_receive_sample"))
				query = query.replaceAll("#", " sc1.sample_transfer_status = 'T' AND isr.incoming_source_type  ='C' AND ");
			else if(actionId != null && actionId.equalsIgnoreCase("lab_transfer_sample"))
				query = query.replaceAll("#", " dom.outsource_dest_type  ='C' AND ");
			else
				query = query.replaceAll("#", " ");
		} else {
			query = query.replaceAll("#", " ");
		}
		StringBuilder sb  = new StringBuilder(query);
		if(bulkWorkSheetPrint != null && bulkWorkSheetPrint.equals("N"))
			sb.append(" tp.pat_id = ? ");
		else
			sb.append(" sc.sample_collection_id IN (");
		if(sampleCollectionIdsArray != null) {
			for (int i=0; i<sampleCollectionIdsArray.length; i++) {
				sb.append(sampleCollectionIdsArray[i]);
				if (i != sampleCollectionIdsArray.length-1)
					sb.append(",");
			}
			sb.append(")");
		}
		sb.append(" ORDER BY sc.sample_date, sc1.sample_date ");
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(sb.toString());
			if(bulkWorkSheetPrint != null && bulkWorkSheetPrint.equals("N"))
				ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_TEST_RESULTS_LIST = " SELECT tp.test_id, trm.resultlabel, trm.resultlabel_id " +
							   " FROM tests_prescribed tp " +
							   " LEFT JOIN test_results_master trm ON (trm.test_id = tp.test_id) " +
							   " LEFT JOIN test_results_center trc ON (trm.resultlabel_id = trc.resultlabel_id) ";
	public static List<BasicDynaBean> getTestResults(String patientId, String sampleCollectionIds, String bulkWorkSheetPrint, String testId, int centerId)
			throws SQLException, ParseException {
		Connection con = null;
		PreparedStatement ps = null;
		String[] sampleCollectionIdsArray = null;
		StringBuilder sb  = new StringBuilder(GET_TEST_RESULTS_LIST);
		if(sampleCollectionIds != null)
			sampleCollectionIdsArray = sampleCollectionIds.split(",");
		if(bulkWorkSheetPrint != null && bulkWorkSheetPrint.equals("N"))
			sb.append(" WHERE tp.pat_id = ? AND tp.test_id = ? AND (trc.center_id = 0 OR trc.center_id = ?) ");
		else
			sb.append(" WHERE tp.sample_collection_id IN (");
		if(sampleCollectionIdsArray != null) {
			for (int i=0; i<sampleCollectionIdsArray.length; i++) {
				sb.append(sampleCollectionIdsArray[i]);
				if (i != sampleCollectionIdsArray.length-1)
					sb.append(",");
			}
			sb.append(") AND tp.test_id = ? AND (trc.center_id = 0 OR trc.center_id = ?) ");
		}
		sb.append(" GROUP BY tp.test_id, trm.resultlabel, trm.resultlabel_id, trm.display_order ORDER BY test_id, display_order, trm.resultlabel_id ");
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(sb.toString());
			if(bulkWorkSheetPrint != null && bulkWorkSheetPrint.equals("N")) {
				ps.setString(1, patientId);
				ps.setString(2, testId);
				ps.setInt(3, centerId);
			} else {
				ps.setString(1, testId);
				ps.setInt(2, centerId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_STATE_ORDER = "select max(state_order) from diag_states_master where level=?";
	public static int getMaxStateOrder(int level) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_STATE_ORDER);
			ps.setInt(1, level);
			return DataBaseUtil.getIntValueFromDb(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static final String GET_STATES_LIST_NEXT = "select * from diag_states_master where level IN (?,?) and status = 'A' and category = ? order by state_order";
	public static List<BasicDynaBean> getconductionStatesNext(int level, int nextlevel, String state_category) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_STATES_LIST_NEXT);
			ps.setInt(1, level);
			ps.setInt(2, nextlevel);
			ps.setString(3, state_category);
			return DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	public static final String GET_STATES_LIST = "select * from diag_states_master where level = ? and status = 'A' and category = ? order by state_order";
	public static List<BasicDynaBean> getconductionStates(int level, String state_category) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_STATES_LIST);
			ps.setInt(1, level);
			ps.setString(2, state_category);
			return DataBaseUtil.queryToDynaList(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	private static final String GET_MAX_LEVEL = "select max(level) from diag_states_master";
	public static int getMaxLevel()throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_MAX_LEVEL);
			return DataBaseUtil.getIntValueFromDb(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String CONDUCTION_CENTER_TEST_DETAILS = "SELECT isrd.source_test_prescribed " +
			" FROM incoming_sample_registration isr" +
			" JOIN incoming_sample_registration_details isrd ON(isr.incoming_visit_id = isrd.incoming_visit_id)" +
			" WHERE isrd.test_id = ? AND isrd.prescribed_id = ? AND isr.incoming_source_type = 'C'";

	private static final String UPDATE_OUTSOURCE_DESTPRESC_ID = "UPDATE tests_prescribed SET outsource_dest_prescribed_id = ?, conducted = 'N'" +
			" WHERE prescribed_id = ?";

	private static boolean updateReconductedPrescIdToCollectionCenter(Connection con,
			BasicDynaBean testPresBean, int newPrescID)throws SQLException, IOException {
		boolean status = true;
		PreparedStatement pstmt = null;
		try {

			if (testPresBean.get("source_test_prescribed_id") != null) {
				pstmt = con.prepareStatement(UPDATE_OUTSOURCE_DESTPRESC_ID);
				pstmt.setInt(1, newPrescID);
				pstmt.setInt(2, (Integer)testPresBean.get("source_test_prescribed_id"));
				status &= pstmt.executeUpdate() >0;
			}
			
			List<BasicDynaBean> lists = new ArrayList<BasicDynaBean>();
			lists.add(tpDAO.findByKey(con, "prescribed_id", newPrescID));
			Map<String, Object> columndata = new HashMap<String, Object>();
			columndata.put("curr_location_presc_id", newPrescID);
			columndata.put("conducted", "N");
			columndata.put("report_id", null);
			status &= LaboratoryBO.copyDataToMultipleChains(con, lists, columndata, "source_test_prescribed_id", null);
			
			return status;
		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}
	}

	private static final String GET_CONDUCTION_CENTER_ID = "SELECT isr.center_id "
			+ " FROM incoming_sample_registration isr "
			+ " JOIN incoming_sample_registration_details isrd ON (isr.incoming_visit_id = isrd.incoming_visit_id) "
			+ " WHERE isrd.test_id = ?  AND isrd.prescribed_id = ? " ;

	public static int getConductionCenterId(String testId, int prescribedId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_CONDUCTION_CENTER_ID);
			ps.setString(1, testId);
			ps.setInt(2, prescribedId);
			return DataBaseUtil.getIntValueFromDb(ps);
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static boolean updateOuthouseFlag(Connection con, int prescribedID)throws SQLException {

		PreparedStatement ps = con.prepareStatement(UPDATE_OUT_HOUSE_SELECTED_FLAG);
		ps.setInt(1, prescribedID);

		return ps.executeUpdate() > 0;
	}

	private static final String UPDATE_NEWPRESCID = "UPDATE incoming_sample_registration_details SET prescribed_id = ?"
			+ " WHERE prescribed_id = ?";

	public static boolean updateNewprescidForIncomingTest(Connection con, Object oldPrescID, Object newPrescID)throws SQLException {
		if (OhSampleRegistrationDAO.isIncomingTest(con, oldPrescID)) {
			PreparedStatement ps = con.prepareStatement(UPDATE_NEWPRESCID);
			ps.setObject(1, newPrescID);
			ps.setObject(2, oldPrescID);

			return ps.executeUpdate() > 0;
		}
		return true;
	}

	public static boolean revertReconductionForOutsource(Object outSourceDestPresID) throws SQLException,IOException {

		Connection con = null;
		boolean status = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean testPrescribedBean = tpDAO.findByKey(con, "prescribed_id", outSourceDestPresID);
			BasicDynaBean tvrBean = tvrDAO.findByKey(con, "report_id", testPrescribedBean.get("report_id"));

			testPrescribedBean.set("conducted", tvrBean != null && tvrBean.get("signed_off").equals("Y") ? "S" :
				( tvrBean == null ) ? "P" : "C");//reverting re-conduction

			status = tpDAO.update(con, testPrescribedBean.getMap(), "prescribed_id",
					outSourceDestPresID) > 0;
		} finally {
			DataBaseUtil.commitClose(con, status);
		}

		return status;
	}

	private static final String GET_HANDOVER =
			"select m.handover_to from tests_prescribed t inner join "+
			"package_prescribed p on t.package_ref=p.prescription_id "+
			"join packages m on p.package_id=m.package_id "+
			" WHERE t.report_id=? and m.handover_to='S'";

	public static boolean isHandoverToSponsor(int reportId) throws SQLException,IOException {
		boolean returnValue=false;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_HANDOVER);
			ps.setInt(1,reportId);
			List<BasicDynaBean>data= DataBaseUtil.queryToDynaList(ps);
			if(null!=data&&data.size()>0)
				returnValue=true;
		}finally{
			if (ps!=null) ps.close();
			if (con!=null) con.close();
		}
	return returnValue;
	}
	
	public Object[] addToArray(Object[] array1, Object[] array2) {
		Object[] newArray = null;
		if (array1 != null && array2 != null) {
			newArray = new String[(array1.length + array2.length)];
			int index = 0;
				for (int i=0; i<array1.length; i++) {
					newArray[i] = array1[i];
					index = i;
				}				
				for (int j=0; j<array2.length; j++) {
					index++;
					newArray[index] = array2[j];
				}
		} else if (array1 != null && array2 == null) {
			return array1;
		}			
		return newArray;
	}
	
	public void removeSpaceFromArray(String[] array1) {
		if (array1 != null) {
			for (int i=0; i<array1.length; i++) {
				array1[i] = array1[i].trim();
			}				
		} 		
	}	
	
	private static final String GET_REPORT_COLLECTION_CENTER =
			" SELECT COALESCE(isr.center_id, pr.center_id) AS center_id " +
	        " FROM tests_prescribed tp " +
	        " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
	        " LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
	        " WHERE tp.report_id = ? AND tp.coll_prescribed_id IS NULL LIMIT 1 ";
	
	private static final String GET_NONSIGNEDOFF_REPORT_COLLECTION_CENTER = 
			"select COALESCE(pr.center_id, isr.center_id) as center_id " +
            " FROM tests_prescribed tp " +
            " JOIN tests_prescribed tpc ON tpc.prescribed_id = tp.coll_prescribed_id " +
            " LEFT JOIN patient_registration pr ON pr.patient_id = tpc.pat_id " +
            " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = tpc.pat_id " +
            " WHERE tp.report_id = ? LIMIT 1;";

	public static BasicDynaBean getReportCollectionCenter(int reportId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_COLLECTION_CENTER);
			ps.setInt(1, reportId);
			BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
			if (bean == null) {
				ps = con.prepareStatement(GET_NONSIGNEDOFF_REPORT_COLLECTION_CENTER);
				ps.setInt(1, reportId);
				bean = DataBaseUtil.queryToDynaBean(ps);				
			}
			return bean;
		}finally{
			if (ps!=null) ps.close();
			if (con!=null) con.close();
		}
	}
	
	private static final String GET_REPORT_CONDUCTION_CENTER =
			" SELECT COALESCE(isr.center_id, pr.center_id) AS center_id " +
	        " FROM tests_prescribed tp " +
	        " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
	        " LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +  
	        " WHERE tp.report_id = ? AND tp.outsource_dest_prescribed_id IS NULL LIMIT 1 ";

	public static BasicDynaBean getReportConductionCenter(int reportId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_CONDUCTION_CENTER);
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			if (ps!=null) ps.close();
			if (con!=null) con.close();
		}
	}
	
	private static final String GET_TEST_SEVERITIES = 
			" select td.*,report_name, d.test_name " +
			" FROM test_details td " +
			" JOIN diagnostics d USING (test_id) " +
			" JOIN tests_prescribed tp USING (prescribed_id) " +
			" JOIN test_visit_reports tvr USING (report_id) " +
			" where (report_id = ?) and (withinnormal = '***' or withinnormal = '###') and (withinnormal != '') " ;
	
	private static final String GET_TEST_SEVERITIES_LIST = 
			" select report_name " +
			" FROM test_details td " +
			" JOIN tests_prescribed tp USING (prescribed_id) " +
			" JOIN test_visit_reports tvr USING (report_id) " +
			" where (report_id = ?) and (withinnormal = '***' or withinnormal = '###') and (withinnormal != '') LIMIT 1" ;
	
	private static final String GET_TEST_IMPROBABLE_VALUES = 
			" select report_value, withinnormal, resultlabel, test_name" +
			" FROM test_details join diagnostics using(test_id) " +
			" where test_details_id = ?" ;
	
	public static String getSeverityResultByReportId(Connection con,  int reportId) throws SQLException {
			return DataBaseUtil.getStringValueFromDb(GET_TEST_SEVERITIES_LIST, reportId);
	}

	public static List<BasicDynaBean> getSeveritiesByReportId(Connection con, int reportId) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_TEST_SEVERITIES);
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String GET_TEST_RESULT =
			" select td.*,report_name, d.test_name ," +
					" (CASE WHEN report_results_severity_status = 'A' THEN 'Normal' "+
					" WHEN report_results_severity_status = 'H' THEN 'Abnormal'"+
					" WHEN report_results_severity_status = 'C' THEN 'Critical' END) as severity_status"+
					" FROM test_details td " +
					" JOIN diagnostics d USING (test_id) " +
					" JOIN tests_prescribed tp USING (prescribed_id) " +
					" JOIN test_visit_reports tvr USING (report_id) " +
					" where (report_id = ?) AND tvr.signed_off = 'Y'" +
					" AND tvr.report_results_severity_status !='T' order by tvr.report_date" ;
	public static List<BasicDynaBean> getTestResultByReportId(int reportId) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_TEST_RESULT);
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	public static BasicDynaBean getExistingRecordsByTestdetailId(int testDetailsId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(GET_TEST_IMPROBABLE_VALUES);
			ps.setInt(1, testDetailsId);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_RESULT_LABEL = 
			" select resultlabel " +
			" FROM test_results_master trm where resultlabel_id = ? ";
	
	public static String getResultLabel(Connection con,  int resultLabelId) throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_RESULT_LABEL);
			ps.setInt(1, resultLabelId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	
	private static final String GET_CRITICAL_RESULT_LABELS = " select tp.prescribed_id, tp.mr_no, tp.pat_id, tp.pres_doctor, tp.coll_prescribed_id, td.*, sc.sample_date::timestamp, d.test_name, "
																 + "(case when withinnormal = '##' then 'critical high'  when withinnormal = '**' then 'critical low' end ) as severity, "
																 + "pr.reg_date, pr.reference_docto_id, pr.doctor, pr.bed_type, bn.bed_name, wnr.ward_name, pd.patient_name, pd.patient_gender, pd.dateofbirth, pd.expected_dob,"
																 + "CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN (floor(current_date - COALESCE(pd.dateofbirth, pd.expected_dob)))::integer "
																 + "WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730 THEN (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/30.43))::integer "
																 + "ELSE (floor((current_date - COALESCE(pd.dateofbirth, pd.expected_dob))/365.25))::integer END AS age, "
																 + "CASE WHEN current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 31 THEN 'D' WHEN (current_date - COALESCE(pd.dateofbirth, pd.expected_dob) < 730) THEN 'M' "
																 + "ELSE 'Y' END AS agein, pd.patient_phone "
																 + "FROM tests_prescribed tp "
																 + "JOIN diagnostics d using(test_id) "
																 +" JOIN test_details td on(td.prescribed_id=tp.curr_location_presc_id)  "
																 + "JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) "
																 + "JOIN patient_details pd ON(pd.mr_no = tp.mr_no) "
																 + "LEFT JOIN sample_collection sc using(sample_collection_id) "
																 + "LEFT JOIN ward_names wnr ON(pr.ward_id = wnr.ward_no) "
																 + "LEFT JOIN admission ad ON ad.patient_id = pr.patient_id "
																 + "LEFT JOIN bed_names bn ON bn.bed_id = ad.bed_id "
																 + " where tp.coll_prescribed_id IS NULL and withinnormal in('**', '##') and td.test_detail_status != 'A' and tp.report_id =? "
																 +" order by td.test_id, td.prescribed_id, td.resultlabel_id";
	
	public static List<BasicDynaBean> getCriticalTestDetails(int reportId) throws SQLException {
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pStmt = con.prepareStatement(GET_CRITICAL_RESULT_LABELS);
			pStmt.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(pStmt);
		} finally {
			DataBaseUtil.closeConnections(con, pStmt);
		}
	}
	
    private static final String IS_BILL_PENDING = "SELECT COUNT(*) FROM diag_report_sharing_on_bill_payment WHERE report_id::text = ?";
    
    public static boolean isBillPending(String reportId) throws SQLException {
    	Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(IS_BILL_PENDING);
			ps.setString(1, reportId);
			return DataBaseUtil.getIntValueFromDb(ps) > 0;
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
    }
    
    private static final String SET_NOTIFICATION_STATUS = "UPDATE test_visit_reports SET notification_sent = ? where report_id = ? ";
    
    public static void setNotificationStatus(int reportId, String status) throws SQLException {
    	Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(SET_NOTIFICATION_STATUS);
			ps.setString(1, status);
			ps.setInt(2, reportId);
			ps.execute();
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
    }
    
    private static final String GET_REPORT_LIST = " select distinct(tp.report_id) from tests_prescribed tp where pat_id= ?"; 
	
	public static List<BasicDynaBean> getAllReportsForPatientId(String patientId) throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_LIST);
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String CHECK_CONFIDENTIAL_REPORT = "select distinct(report_id) from tests_prescribed where not exists(select 1 from tests_prescribed join diagnostics using(test_id) "+
															"where isconfidential = 'true' and report_id::integer = ?) and report_id::integer = ?";
    
    public static String isConfidentialReport(Integer reportId) throws SQLException {
    	Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(CHECK_CONFIDENTIAL_REPORT);
			ps.setInt(1, reportId);
			ps.setInt(2, reportId);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally{
			DataBaseUtil.closeConnections(con, ps);
		}
    }

	private static void getEMRTests(List<EMRDoc> docs, List<BasicDynaBean> l,BasicDynaBean PrintPref1,BasicDynaBean PrintPref2,boolean isPatientView){
		for (BasicDynaBean b : l) {
			EMRDoc doc = new EMRDoc();
			String docId = b.get("report_id").toString();
			doc.setProvider(Provider.DIAGProvider);
			doc.setDocid(docId);
			doc.setVisitid((String) b.get("patient_id"));
			doc.setDescription((String) b.get("test_names"));
			doc.setTitle((String)b.get("report_name"));
			doc.setDoctor((b.get("pres_doctor") == null) ? "" : (String)b.get("pres_doctor"));
			doc.setVisitDate((java.util.Date)b.get("reg_date"));
			BasicDynaBean printpref = null;
			String category = (String) b.get("category");
			if (category.equals("DEP_RAD")) {
				doc.setType("SYS_RR");
				printpref = PrintPref1;
			} else {
				doc.setType("SYS_LR");
				printpref = PrintPref2;
			}
			
			int printerId = (Integer) printpref.get("printer_id");
			doc.setPrinterId(printerId);
			String displayUrl = "/pages/DiagnosticModule/DiagReportPrint.do?_method=printReport&reportId="+docId+"&forcePdf=true&printerId="+printerId;
			if(isPatientView){
				displayUrl = displayUrl+"&patient_view=true";
			}
			doc.setUpdatedBy((String)b.get("user_name"));
			doc.setAuthorized(true);
			doc.setDisplayUrl(displayUrl);

			String reportMode = (String) b.get("report_mode");
			if (reportMode.equals("H")) {
				doc.setPdfSupported(false);
			} else {
				doc.setPdfSupported(true);
			}

			doc.setDate((java.sql.Timestamp)b.get("report_date"));
			docs.add(doc);
		}
	}

  private static final String GET_DIAG_SIGNEDOFF_NOTIFICATION_USER = "SELECT emp_username "
      + " FROM tests_prescribed tp "
      + " JOIN u_user u ON ( u.doctor_id = tp.pres_doctor)"
      + " WHERE (tp.pres_doctor != '' AND tp.pres_doctor is not null) AND #filter = ? "
      + " GROUP BY emp_username";

  public List<BasicDynaBean> getNotificationUser(int reportId, boolean isTestDoc)
      throws SQLException {
    // if notification is to be sent for test supporting doc then filtering of user is done based on presc_id
    String query = GET_DIAG_SIGNEDOFF_NOTIFICATION_USER.replace("#filter",
        isTestDoc ? "test_doc_id" : "report_id");
    try (Connection con = DataBaseUtil.getConnection();
        PreparedStatement ps = con.prepareStatement(query)) {
      ps.setInt(1, reportId);
      return DataBaseUtil.queryToDynaList(ps);
    }
  }
  
}
