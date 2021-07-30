package com.insta.hms.diagnosticmodule.common;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.SearchQueryBuilder;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticmodule.radiology.RadiologyDAO;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.SampleType.SampleTypeDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class DiagnosticsDAO {

	//This class contains all the methods  for inserting into diagnostics corresponding tables;
	static Logger logger = LoggerFactory.getLogger(DiagnosticsDAO.class);
	PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
	
	private static final GenericDAO storeReagentUsageMainDAO = new GenericDAO("store_reagent_usage_main");

   private static final String INSERT_TEST_CONDUCTION_DETAILS = "INSERT INTO tests_conducted(mr_no,patient_id," +
   		"test_id,conducted_date,conducted_by,conducted_time,satisfactory_status," +
   		"prescribed_id,remarks,user_name, validated_by, validated_date, technician, completed_by)VALUES(?,?," +
   		"?,?,?,to_char('now'::time,'hh:mi:ss')::time,?," +
   		"?,?,?,?,?,?,?)";

	public static boolean insertTestConductionDetatils(Connection con,TestConducted tc)throws SQLException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(INSERT_TEST_CONDUCTION_DETAILS);
			int i=1;
			logger.debug(INSERT_TEST_CONDUCTION_DETAILS);
			ps.setString(i++, tc.getMrNo());
			ps.setString(i++, tc.getVisitId());
			ps.setString(i++, tc.getTestId());
			ps.setTimestamp(i++, tc.getConductedDate());
			ps.setString(i++, tc.getConductedBy());
			ps.setString(i++, tc.getStatisFactoryFlag());
			ps.setInt(i++, tc.getPrescribedId());
			ps.setString(i++, tc.getRemarks());
			ps.setString(i++, tc.getUserName());
			ps.setString(i++, tc.getValidatedBy());
			ps.setDate(i++, tc.getValidatedDate());
			ps.setString(i++, tc.getTechnician());
			ps.setString(i++, tc.getCompletedBy());
			int j = ps.executeUpdate();

			if(j>0){
				status = true;
			}
		}finally{
			ps.close();
		}
		return status;
	}


	/*
	 * While inserting, the format is always copied from the template master, instead of
	 * keeping it as null.
	 */
	private static final String INSERT_TEST_DETAILS = "INSERT INTO test_details("+
	    " mr_no, patient_id, test_id, prescribed_id, "+
	    " conducted_in_reportformat, format_name, patient_report_file,"+
	    " resultlabel, report_value, units, reference_range, comments,"+
	    " path,withinnormal,user_name,code_type,result_code,result_disclaimer," +
	    " resultlabel_id,original_test_details_id,test_detail_status,amendment_reason, method_id, calculated )"+
	    " VALUES (?, ?, ?, ?,"+
	            "?, ?, (SELECT report_file FROM test_format WHERE testformat_id=?),"+
	            "?, ?, ?, ?, ?,"+
	            "?,?,?,?,?,?,?,?,?,?,?,? );";

	public static boolean insertTestResults(Connection con,TestDetails td) throws SQLException {
		PreparedStatement ps = con.prepareStatement(INSERT_TEST_DETAILS);
		int i=1;

		ps.setString(i++, td.getMrNo());
		ps.setString(i++, td.getVisitId());
		ps.setString(i++, td.getTestId());
		ps.setInt(i++, td.getPrescribedId());
		ps.setString(i++,td.getConductedInFormat());
		ps.setString(i++,td.getFormatId() );

		if(td.getConductedInFormat().equals("Y")) {
			ps.setString(i++,td.getFormatId());
		} else {
			ps.setString(i++, null);
		}

		ps.setString(i++, td.getResultLabel());
		ps.setString(i++, td.getResultValue());
		ps.setString(i++, td.getUnits());
		ps.setString(i++, td.getReferenceRange());
		ps.setString(i++, td.getRemarks());
		ps.setString(i++, null);
		ps.setString(i++, td.getWithInNormal());
		ps.setString(i++, td.getUserName());
		ps.setString(i++, td.getCodeType());
		ps.setString(i++, td.getResultCode());
		ps.setString(i++, td.getResultDisclaimer());
		ps.setInt(i++, td.getResultlabelId());
		ps.setInt(i++, td.getOrginalTestDetailsId());
		ps.setString(i++, td.getTestDetailStatus());
		ps.setString(i++, td.getAmendReason());
		ps.setObject(i++, td.getMethodId());
		ps.setObject(i++, td.getResultExpressionCheck());
		
		return ps.executeUpdate() > 0;
	}


	private static final String INSERT_SAMPLE =
			" INSERT INTO sample_collection( "+
		    " mr_no, patient_id,sample_sno, "+
		    " sample_status,sample_date,user_name," +
		    " sample_type_id,sample_source_id, sample_qty, sample_no_counter, " +
		    " sample_collection_id, outsource_dest_id, sample_transfer_status,bed_id,ward_id ) " +
		    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,? );";


	public static boolean insertSamples(Connection con,SampleCollection sc, boolean isBatchBased)throws SQLException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			ps =con.prepareStatement(INSERT_SAMPLE);
			ps.setString(1,sc.getMrNo());
			ps.setString(2,sc.getVisitId());
			ps.setString(3,sc.getSampleNo());
			ps.setString(4,sc.getSampleStatus());
			ps.setTimestamp(5,sc.getSampleStatus().equals("C") ? sc.getSampleDate() : null );
			ps.setString(6,sc.getUserName());
			ps.setInt(7, sc.getSampleTypeId());
			ps.setInt(8, sc.getSampleSourceId());
			ps.setInt(9, sc.getSampleQty());
			logger.info(Boolean.toString(isBatchBased));
			ps.setInt(10, isBatchBased ? SampleTypeDAO.getMaxCounter(con, sc.getVisitId()) : 0);
			ps.setInt(11, sc.getSampleSequence());
			if(null != sc.getoutSourceId() && !sc.getoutSourceId().isEmpty())
				ps.setInt(12, Integer.parseInt(sc.getoutSourceId()));
			else
				ps.setObject(12, null);
			ps.setString(13,sc.getSampleTransferStatus());
			ps.setInt(14, sc.getBedId());
			ps.setString(15, sc.getWardId());
			int j = ps.executeUpdate();
			if(j>0){
				status = true;
			}
		}finally{
			ps.close();
		}
		return status;
	}

	private static final String update_samples =
		" UPDATE sample_collection SET sample_status = ? ";

	public static boolean updateSamples(Connection con,SampleCollection sc)throws SQLException{
		PreparedStatement ps = null;
		try{
	        ps = con.prepareStatement(update_samples+" WHERE sample_sno = ?");
			ps.setString(1, sc.getSampleStatus());
			ps.setString(2, sc.getSampleNo());
			return ps.executeUpdate() > 0;
		}finally{
			if(ps != null)ps.close();
		}
	}

	private static final String CLOSE_PRESCRIPTIONS = "UPDATE tests_prescribed " +
			" set user_name= ?,conducted='X',remarks=?,cancelled_by=?,Cancel_Date=? WHERE prescribed_id=?";


	public static boolean closePrescriptions(Connection con,TestPrescribed tp)
	throws SQLException,IOException{
		boolean status = false;
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(CLOSE_PRESCRIPTIONS);
			ps.setString(1, tp.getUserName());
			ps.setString(2, tp.getRemarks());
			ps.setString(3, tp.getCancelledBy());
			ps.setDate(4, tp.getCancelDate());
			ps.setInt(5, tp.getPrescribedId());

			status = ps.executeUpdate() > 0;

		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}

		return status;
	}


	private static final String GET_REPORT_TEST_VALUES_FIELDS =
		"SELECT ih.hospital_name, coalesce(rf.referal_name, refdoc.doctor_name) as referal_name, dd.category, " +
		"  td.prescribed_id, td.conducted_in_reportformat, td.resultlabel, td.report_value, " +
		"  td.units, td.reference_range, td.comments,d.conduction_instructions,d.sample_collection_instructions, " +
		"  coalesce(td.patient_report_file,'') as patient_report_file, td.withinnormal, " +
		"  d.test_name, d.type_of_specimen, dd.ddept_name, doc.doctor_name,tp.pres_date, " +
		"  tc.conducted_date,  tc.remarks, COALESCE(csc.sample_date, sc.sample_date) AS sample_date, " +
		"  tp.labno, COALESCE(csc.sample_sno, sc.sample_sno) AS sample_sno," +
		"  ss.source_name, him.impression_details,him.short_impression,thr.no_of_blocks,thr.block_description," +
		"  thr.no_of_slides,thr.slide_description,thr.clinical_details,thr.micro_gross_details," +
		"  tmr.growth_exists,tmr.colony_count,mntm.nogrowth_template_name,tp.reference_pres," +
		"  tmr.nogrowth_report_comment,tmr.growth_report_comment,tmr.microscopic_details,thr.histo_id,tmr.test_micro_id," +
		"  tcr.test_type,tcr.specimen_adequacy,tcr.smear_received,tcr.microscopic_details AS cyto_microscopic_details,tcr.cyto_id, "+
		"  hic.short_impression AS cyto_short_impression,hic.impression_details AS cyto_impression_details," +
		"  tcr.clinical_details AS cyto_clinical_details,td.test_detail_status,td.original_test_details_id" +
		"  ,tp.re_conduction,td.amendment_reason,tp.reconducted_reason,tp.package_ref,pm.package_name" +
		"  ,pm.description,st.sample_type,td.result_code, tc.validated_by, tc.validated_date, tc.technician, " +
		"  tc.conducted_by, cdoc.doctor_name as cond_doctor_name, sc.assertion_time, tp.pres_date, td.result_disclaimer, " +
		"  d.service_sub_group_id, d.test_id, ssg.service_sub_group_name," +
		" (select display_order from package_contents pc " +// HMS-11962
		" where pc.package_id = pm.package_id AND activity_type IN ('Laboratory','Radiology') AND activity_id = d.test_id limit 1)  as display_order_in_package, " +
		" dmm.method_name, tm.display_order as result_display_order, bn.bed_name,wn.ward_name,tc.completed_by, td.test_details_id "+
		" FROM test_details td " +
		"  JOIN diagnostics d ON (d.test_id = td.test_id) " +
		"  JOIN tests_prescribed tp ON (tp.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN tests_conducted tc ON (tc.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id AND td.resultlabel_id = tm.resultlabel_id) " +
		"  LEFT JOIN diag_methodology_master dmm ON(tm.method_id = dmm.method_id)" +
		"  LEFT JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id) "+
		"  LEFT JOIN sample_collection csc ON (csc.sample_sno = sc.coll_sample_no) " +
		"  LEFT JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id ) " +
		"  LEFT JOIN sample_sources ss on (ss.source_id = sc.sample_source_id ) " +
		"  LEFT JOIN doctors doc ON (doc.doctor_id = tp.pres_doctor) " +
		"  LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by) " +
		"  JOIN  diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)" +
		"  LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = d.service_sub_group_id) "+
		"  LEFT JOIN incoming_sample_registration isr on (tp.pat_id = isr.incoming_visit_id) "+
		"  LEFT JOIN referral rf  on (isr.referring_doctor = rf.referal_no) "+
		"  LEFT JOIN doctors refdoc on (isr.referring_doctor = refdoc.doctor_id) " +
		"  left join incoming_hospitals ih on(isr.orig_lab_name = ih.hospital_id) "+
		"  LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN histo_impression_master him on(him.impression_id = thr.impression_id) " +
		"  LEFT JOIN test_microbiology_results tmr ON(tmr.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN micro_nogrowth_template_master mntm ON(mntm.nogrowth_template_id = tmr.nogrowth_template_id ) " +
		"  LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = td.prescribed_id)" +
		"  LEFT JOIN histo_impression_master hic on(hic.impression_id = tcr.impression_id)"+
		"  LEFT JOIN package_prescribed pp ON( tp.package_ref = pp.prescription_id ) " +
		"  LEFT JOIN packages pm USING(package_id) " +
//		"  LEFT JOIN package_contents pc ON(pc.package_id = pm.package_id AND activity_type IN ('Laboratory','Radiology') AND activity_id = d.test_id)" + // HMS-11962
		"  LEFT JOIN bed_names bn ON(bn.bed_id = sc.bed_id)" +
		"  LEFT JOIN ward_names wn ON(wn.ward_no =sc.ward_id)";

	private static final String GET_REPORT_TEST_VALUES_ORDERBY_FIELDS =
		" dd.display_order,tp.prescribed_id,display_order_in_package,dd.ddept_id, td.conducted_in_reportformat,tm.display_order, td.resultlabel_id ";

	/*
	 *  returns all the tests conducted results that are included in report.
	 */
	public static List getReportTestValues(int reportId,Boolean amendedResults) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_TEST_VALUES_FIELDS +
					( amendedResults == null || amendedResults ? " WHERE tp.prescribed_id = ? " :" WHERE tp.report_id=? " ) +
					( amendedResults != null && amendedResults ? " AND test_detail_status = 'A' " :
						( amendedResults != null)? "AND test_detail_status != 'A'" : "" )+
					" ORDER BY " + GET_REPORT_TEST_VALUES_ORDERBY_FIELDS);
			ps.setInt(1, reportId);


			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * returns all the tests conducted results for the patient.
	 *
	 * @param patientId
	 * @return returns testresults as list of BasicDynaBeans.
	 * @throws SQLException
	 */
	private static final String GET_REPORT_TEST_VALUES_FIELDS_FOR_PATIENT =
		"SELECT ih.hospital_name, coalesce(rf.referal_name, refdoc.doctor_name) as referal_name, dd.category, " +
		"  td.prescribed_id, td.conducted_in_reportformat, td.resultlabel, td.report_value, " +
		"  td.units, td.reference_range, td.comments,d.conduction_instructions,d.sample_collection_instructions, " +
		"  coalesce(td.patient_report_file,'') as patient_report_file, td.withinnormal, " +
		"  d.test_name, d.type_of_specimen, dd.ddept_name, doc.doctor_name,tp.pres_date, " +
		"  tc.conducted_date,  tc.remarks, COALESCE(csc.sample_date, sc.sample_date) AS sample_date, " +
		"  tp.labno, COALESCE(csc.sample_sno, sc.sample_sno) AS sample_sno, ss.source_name, " +
		"  him.impression_details,him.short_impression,thr.no_of_blocks,thr.block_description," +
		"  thr.no_of_slides,thr.slide_description,thr.clinical_details,thr.micro_gross_details," +
		"  tmr.growth_exists,tmr.colony_count,mntm.nogrowth_template_name,tp.reference_pres," +
		"  tmr.nogrowth_report_comment,tmr.growth_report_comment,tmr.microscopic_details,thr.histo_id,tmr.test_micro_id," +
		"  tcr.test_type,tcr.specimen_adequacy,tcr.smear_received,tcr.microscopic_details AS cyto_microscopic_details,tcr.cyto_id, "+
		"  hic.short_impression AS cyto_short_impression,hic.impression_details AS cyto_impression_details," +
		"  tcr.clinical_details AS cyto_clinical_details,td.test_detail_status,td.original_test_details_id" +
		"  ,tp.re_conduction,td.amendment_reason,tp.reconducted_reason,tp.package_ref,pm.package_name" +
		"  ,pm.description,st.sample_type,td.result_code, tc.validated_by, tc.validated_date, tc.technician, " +
		"  tc.conducted_by, cdoc.doctor_name as cond_doctor_name, sc.assertion_time, tp.pres_date, td.result_disclaimer, " +
		"  d.service_sub_group_id, ssg.service_sub_group_name,pc.display_order as display_order_in_package, dmm.method_name "+
		" FROM test_details td " +
		"  JOIN diagnostics d ON (d.test_id = td.test_id) " +
		"  JOIN tests_prescribed tp ON (COALESCE(tp.outsource_dest_prescribed_id, tp.prescribed_id) = td.prescribed_id) " +
		"  LEFT JOIN tests_conducted tc ON (tc.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id AND td.resultlabel_id = tm.resultlabel_id) " +
		"  LEFT JOIN diag_methodology_master dmm ON(tm.method_id = dmm.method_id)" +
		"  LEFT JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id) "+
		"  LEFT JOIN sample_collection csc ON (csc.sample_sno = sc.coll_sample_no) " +
		"  LEFT JOIN sample_type st ON (st.sample_type_id = sc.sample_type_id ) " +
		"  LEFT JOIN sample_sources ss on (ss.source_id = sc.sample_source_id ) " +
		"  LEFT JOIN doctors doc ON (doc.doctor_id = tp.pres_doctor) " +
		"  LEFT JOIN doctors cdoc ON (cdoc.doctor_id=tc.conducted_by) " +
		"  JOIN  diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)" +
		"  LEFT JOIN service_sub_groups ssg ON (ssg.service_sub_group_id = d.service_sub_group_id) "+
		"  LEFT JOIN incoming_sample_registration isr on (tp.pat_id = isr.incoming_visit_id) "+
		"  LEFT JOIN referral rf  on (isr.referring_doctor = rf.referal_no) "+
		"  LEFT JOIN doctors refdoc on (isr.referring_doctor = refdoc.doctor_id) " +
		"  left join incoming_hospitals ih on(isr.orig_lab_name = ih.hospital_id) "+
		"  LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN histo_impression_master him on(him.impression_id = thr.impression_id) " +
		"  LEFT JOIN test_microbiology_results tmr ON(tmr.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN micro_nogrowth_template_master mntm ON(mntm.nogrowth_template_id = tmr.nogrowth_template_id ) " +
		"  LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = td.prescribed_id)" +
		"  LEFT JOIN histo_impression_master hic on(hic.impression_id = tcr.impression_id)"+
		"  LEFT JOIN package_prescribed pp ON( tp.package_ref = pp.prescription_id ) " +
		"  LEFT JOIN packages pm ON(pm.package_id = pp.package_id)  " +
		"  LEFT JOIN package_contents pc ON(pc.package_id = pm.package_id AND activity_type IN ('Laboratory','Radiology') AND activity_id = d.test_id)";


	public static List getTestValuesForPatient(String patientId) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_TEST_VALUES_FIELDS_FOR_PATIENT + " WHERE td.test_detail_status != 'A' " +
					" AND tp.conducted = 'S' AND tp.pat_id=? " +
					" ORDER BY td.conducted_in_reportformat, tp.report_id, tc.conducted_date, td.test_id,"
					+ " tp.prescribed_id, tm.display_order");
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}


	/*
	 * this query copied from GET_REPORT_TEST_VALUES_FIELDS and added extra bill joins.
	 * adding bill joins at the bottom of that query taking long time, hence copied above query and added bill joins after
	 * tests_prescribed which improved query time a lot.
	 *
	 * refer BUG 27809
	 */
	private static final String GET_REPORT_TEST_VALUES_FIELDS_BILL =
		"SELECT ih.hospital_name, coalesce(rf.referal_name, refdoc.doctor_name) as referal_name, " +
		"  td.test_id, td.prescribed_id, td.conducted_in_reportformat, td.resultlabel, td.report_value, " +
		"  td.units, td.reference_range, td.comments, " +
		"  d.conduction_instructions, d.sample_collection_instructions, d.remarks as test_remarks, " +
		"  coalesce(td.patient_report_file,'') as patient_report_file, td.withinnormal, " +
		"  d.test_name, d.type_of_specimen, dd.ddept_name, doc.doctor_name,tp.pres_date, " +
		"  tc.conducted_date,  tc.remarks, COALESCE(csc.sample_date, sc.sample_date) AS sample_date, " +
		"  tp.labno, COALESCE(csc.sample_sno, sc.sample_sno) AS sample_sno, ss.source_name, " +
		"  him.impression_details,him.short_impression,thr.no_of_blocks,thr.block_description," +
		"  thr.no_of_slides,thr.slide_description,thr.clinical_details,thr.micro_gross_details," +
		"  tmr.growth_exists,tmr.colony_count,mntm.nogrowth_template_name,tp.reference_pres," +
		"  tmr.nogrowth_report_comment,tmr.growth_report_comment,tmr.microscopic_details,thr.histo_id,tmr.test_micro_id," +
		"  tcr.test_type,tcr.specimen_adequacy,tcr.smear_received,tcr.microscopic_details AS cyto_microscopic_details,tcr.cyto_id, "+
		"  hic.short_impression AS cyto_short_impression,hic.impression_details AS cyto_impression_details," +
		"  tcr.clinical_details AS cyto_clinical_details,td.test_detail_status,td.original_test_details_id" +
		" ,tp.re_conduction,td.amendment_reason,tp.reconducted_reason,tp.package_ref,pm.package_name,pm.description " +
		" FROM test_details td " +
		"  JOIN diagnostics d ON (d.test_id = td.test_id) " +
		"  JOIN tests_prescribed tp ON (tp.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN bill_activity_charge bac ON(bac.activity_id::integer = tp.prescribed_id) "+
		"  LEFT JOIN bill_charge bc ON (bac.charge_id = bc.charge_id) " +
		"  LEFT JOIN tests_conducted tc ON (tc.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id AND td.resultlabel_id = tm.resultlabel_id) " +
		"  LEFT JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id) "+
		"  LEFT JOIN sample_collection csc ON (csc.sample_sno = sc.coll_sample_no) " +
		"  LEFT JOIN sample_sources ss on (ss.source_id = sc.sample_source_id ) " +
		"  LEFT JOIN doctors doc ON (doc.doctor_id = tp.pres_doctor) " +
		"  JOIN  diagnostics_departments dd ON (dd.ddept_id = d.ddept_id)" +
		"  LEFT JOIN incoming_sample_registration isr on (tp.pat_id = isr.incoming_visit_id) "+
		"  LEFT JOIN referral rf  on (isr.referring_doctor = rf.referal_no) "+
		"  LEFT JOIN doctors refdoc on (isr.referring_doctor = refdoc.doctor_id) " +
		"  left join incoming_hospitals ih on(isr.orig_lab_name = ih.hospital_id) "+
		"  LEFT JOIN test_histopathology_results thr ON(thr.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN histo_impression_master him on(him.impression_id = thr.impression_id) " +
		"  LEFT JOIN test_microbiology_results tmr ON(tmr.prescribed_id = td.prescribed_id) " +
		"  LEFT JOIN micro_nogrowth_template_master mntm ON(mntm.nogrowth_template_id = tmr.nogrowth_template_id ) " +
		"  LEFT JOIN test_cytology_results tcr ON(tcr.prescribed_id = td.prescribed_id)" +
		"  LEFT JOIN histo_impression_master hic on(hic.impression_id = tcr.impression_id)"+
		"  LEFT JOIN package_prescribed pp ON( tp.package_ref = pp.prescription_id ) " +
		"  LEFT JOIN packages pm ON (pp.package_id = pm.package_id) ";

	/**
	 * Tests Results related to given bill.
	 * @param billNo
	 * @return
	 * @throws SQLException
	 */
	public static List getTestValuesForBill(String billNo) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT_TEST_VALUES_FIELDS_BILL +
					" WHERE bc.bill_no=? AND bac.activity_code = 'DIA'" +
					" ORDER BY td.conducted_in_reportformat, tp.report_id, tc.conducted_date, td.test_id,"
					+ " tp.prescribed_id, tm.display_order");
			ps.setString(1, billNo);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}

	}

	private static final String GET_REPORT_DESIGNATIONS = "SELECT DISTINCT designation " +
		" FROM tests_prescribed tp " +
		"  JOIN Diagnostics d on (tp.test_id =d.test_id) " +
		"  JOIN diagnostics_departments dd ON (dd.ddept_id=d.ddept_id) " +
		" WHERE tp.report_id=?";

	public static List getReportDesignations(int reportId) throws SQLException {
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pStmt = con.prepareStatement(GET_REPORT_DESIGNATIONS);
			pStmt.setInt(1, reportId);
			return DataBaseUtil.queryToArrayList(pStmt);
		} finally {
			DataBaseUtil.closeConnections(con, pStmt);
		}
	}

	private static final String GET_CONDUCTING_DOCTORS = "SELECT DISTINCT " +
		" d.doctor_name, d.specialization,d.qualification " +
		" FROM tests_prescribed tp " +
		"  JOIN tests_conducted tc on (tc.prescribed_id = tp.prescribed_id) " +
		"  JOIN doctors d on (d.doctor_id = tc.conducted_by) " +
		" WHERE tp.report_id=?";

	public static List getConductingDoctors(int reportId) throws SQLException {
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pStmt = con.prepareStatement(GET_CONDUCTING_DOCTORS);
			pStmt.setInt(1, reportId);
			return DataBaseUtil.queryToArrayList(pStmt);
		} finally {
			DataBaseUtil.closeConnections(con, pStmt);
		}
	}

	private static final String GET_REPORT_PATIENT_ID =
		"SELECT patient_id from test_visit_reports WHERE report_id=?";

	public static String getReportPatientId(int reportId) throws SQLException {
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = DataBaseUtil.getReadOnlyConnection();
			pStmt = con.prepareStatement(GET_REPORT_PATIENT_ID);
			pStmt.setInt(1, reportId);
			return DataBaseUtil.getStringValueFromDb(pStmt);
		} finally {
			DataBaseUtil.closeConnections(con, pStmt);
		}
	}

	private static String CANCEL_TEST_PRESCRIPTION = "UPDATE tests_prescribed SET user_name=?,conducted='X',cancelled_by=?,remarks=?," +
	"Cancel_Date=? WHERE   pat_id=?  and prescribed_id=? ";

	public boolean cancellPrescriptionDetails(Connection con, TestPrescribed dto,String chargeHead)throws SQLException{
		boolean status = false;
		PreparedStatement ps,ps1 = null;
		ps = con.prepareStatement(CANCEL_TEST_PRESCRIPTION);

		ChargeBO chargeBO = new ChargeBO();

		if(chargeBO.isCancelable("DIA", dto.getPrescribedId())){

			ps.setString(1, dto.getUserName());
			ps.setString(2, dto.getCancelledBy());
			ps.setString(3, dto.getRemarks());
			ps.setDate(4, dto.getCancelDate());
			ps.setString(5, dto.getVisitId());
			ps.setInt(6,dto.getPrescribedId());

			String chargeId = BillActivityChargeDAO.getChargeId("DIA", dto.getPrescribedId());

			do {
				int i = ps.executeUpdate();
				if(i>0){
					status= true;
				}
				if(!status)break;

				ChargeDAO.cancelChargeUpdateAuditLog(con, chargeId, false, RequestContext.getUserName());
				ChargeDAO chargeDao = new ChargeDAO(con);
				chargeDao.cancelBillChargeClaim(con, chargeId);
				List<BasicDynaBean> associatedChargeList = chargeDao.getAssociatedCharges(con,chargeId);
				for(BasicDynaBean bean : associatedChargeList){
					String chargeRef = String.valueOf(bean.get("charge_id"));
					chargeDao.cancelBillChargeClaim(con, chargeRef);
				}
			} while(false);
		}

		ps.close();

		return status;
	}

	private static final String GET_REPORT_LIST =
			"SELECT report_id,report_name,signed_off,handed_over " +
			"  FROM test_visit_reports WHERE " +
			" patient_id=? and category=? ";
	private static final String GET_NEXT_SEQUENCE_ID = "SELECT nextval('test_report_sequence') ";

	public static ArrayList<Hashtable<String, String>> getReportList(String visitId,String category, boolean setFlagForEmptyRcds)throws SQLException{
		ArrayList<Hashtable<String, String>> l =null;
 		Connection con = null;
 		PreparedStatement ps = null;
 		
 		
 		try{
 			con = DataBaseUtil.getReadOnlyConnection();
 	
 			ps = con.prepareStatement(GET_REPORT_LIST);
 			ps.setString(1,visitId);
 			ps.setString(2,category);
 			l = DataBaseUtil.queryToArrayList(ps);
 			if(l.size() == 0 && setFlagForEmptyRcds){
 				Hashtable<String,String> ht = new Hashtable<String, String>();
 				ht.put("REPORT_ID", "NEW1");
 				ht.put("REPORT_NAME", "NEW1");
 				l.add(ht);
 			}
 			return l;
 		} finally{
 			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_PRESCRIPTION_REPORTLIST = "SELECT distinct tvr.report_id,tvr.report_name" +
			" FROM  test_visit_reports tvr join tests_prescribed tp on(tvr.report_id = tp.report_id ) and " +
			" tvr.patient_id = ? and tvr.category = ?  and tvr.patient_id  = tp.pat_id ";

	public static ArrayList<Hashtable<String, String>>reportListInprescription(String visitId,String category)throws SQLException{
		ArrayList<Hashtable<String, String>> l =null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_PRESCRIPTION_REPORTLIST);
			ps.setString(1,visitId);
			ps.setString(2,category);
	
			l = DataBaseUtil.queryToArrayList(ps);
			return l;
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}


	private static final String GET_REPORT_ID = "SELECT report_id,report_name FROM test_visit_reports WHERE " +
	" patient_id=? AND report_name = ? AND  category =?";

	public static int getReportId(TestVisitReports tvr) throws SQLException{
		int id  =0;
		Connection con = DataBaseUtil.getReadOnlyConnection();

		if(!tvr.getReportName().startsWith("NEW")){
			PreparedStatement ps = con.prepareStatement(GET_REPORT_ID);
			ps.setString(1, tvr.getVisitId());
			ps.setString(2, tvr.getReportName());
			ps.setString(3, tvr.getCategory());

			String reportIdStr = DataBaseUtil.getStringValueFromDb(ps);
			if(reportIdStr != null){
			  id =  Integer.parseInt(reportIdStr);
			}
			ps.close();
		}else{
			PreparedStatement ps = con.prepareStatement(GET_NEXT_SEQUENCE_ID);
			id = Integer.parseInt(DataBaseUtil.getStringValueFromDb(ps));
			ps.close();
		}
		con.close();
		return id;
	}


	public static int getNextReportId()throws SQLException{
		int id=0;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_NEXT_SEQUENCE_ID);
		id = Integer.parseInt(DataBaseUtil.getStringValueFromDb(ps));
		ps.close();
		con.close();
		return id;
	}

	private static final String INSERT_REPORT =
		"INSERT INTO test_visit_reports (report_id, patient_id, " +
		" report_name, category, report_mode, user_name, pheader_template_id,report_results_severity_status) " +
		" VALUES(?,?,?,?,?,?,?,?) ";

	public static boolean insertOrUpdateReport(Connection con,TestVisitReports tvr)throws SQLException{
		boolean status = false;
		PreparedStatement ps = null;

		try{
			if(tvr.getReportName().startsWith("NEW") 
					&& DiagnosticsDAO.getReportDynaBean(con,tvr.getReportId()) == null) {
				ps = con.prepareStatement(INSERT_REPORT);
				ps.setInt(1,tvr.getReportId());
				ps.setString(2, tvr.getVisitId());
				ps.setString(3, getNewReportName(con,tvr));
				ps.setString(4, tvr.getCategory());
				ps.setString(5, tvr.getReportMode());
				ps.setString(6, tvr.getUserName());
				ps.setObject(7, tvr.getPheader_template_id());
				ps.setString(8, tvr.getReportResultsSeverityStatus());

				if (ps.executeUpdate()>0) status = true;
			} else {
				ps = con.prepareStatement(" UPDATE test_visit_reports SET report_results_severity_status = ? WHERE report_id = ? ");
				ps.setString(1, tvr.getReportResultsSeverityStatus());
				ps.setInt(2, tvr.getReportId());

				status = ps.executeUpdate() > 0;
			}
		} finally {
			if (ps !=null) ps.close();
		}
		return status;
	}

	private String DISCARD_REPORT =
		"UPDATE test_visit_reports SET report_state = ? ,revised_report_id =? WHERE report_id = ?";

	public boolean discardReport(Connection con,int reportId,int revisedReportId)
	throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(DISCARD_REPORT);
			ps.setString(1, "D");
			ps.setInt(2, revisedReportId);
			ps.setInt(3, reportId);
			return ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	private static final String GET_REPORT_COUNT = "SELECT count(report_name) FROM test_visit_reports " +
		" WHERE patient_id = ? and category=?";

	private static String getNewReportName(Connection con,TestVisitReports tvr)throws SQLException{
		String reportName = null;
		String testCategory=null;
		PreparedStatement ps = con.prepareStatement(GET_REPORT_COUNT);
		ps.setString(1, tvr.getVisitId());
		ps.setString(2, tvr.getCategory());

		String count = DataBaseUtil.getStringValueFromDb(ps);
		String toDate = DataBaseUtil.getStringValueFromDb("select to_char(localtimestamp(0)::date,'ddmmyy')");
		if( tvr.getCategory().equals("DEP_LAB"))
			testCategory="LR";
		else
			testCategory="RR";

		ps = con.prepareStatement(REPORT_TESTS);
		ps.setInt(1, tvr.getReportId());
		List<BasicDynaBean > reportTests = DataBaseUtil.queryToDynaList(ps);
		BasicDynaBean reportTestBean = null;
		if( reportTests.size() > 1 ){//more than one department involved in report so normal way of naming
			if(count!=null){
				reportName = testCategory+"-"+toDate+"-"+(Integer.parseInt(count) + 1);
			}
		}else{
			reportTestBean = reportTests.get(0);
			if( ( (String)reportTestBean.get("test_names") ).split(",").length > 1 ){
				//group of test with same deprtament,First testname with + can be Report name
				reportName = testCategory+"-"+"(+)-"+
								((String)reportTestBean.get("test_names")).split(",")[0]+
								(Integer.parseInt(count) + 1);
			}else{
				//only one test involved Test Name will be Report Name
				reportName = testCategory+"-"+(String)reportTestBean.get("test_names")+"-"+(Integer.parseInt(count) + 1);
			}
		}

		ps.close();

		return reportName;
	}

	public static final String REPORT_TESTS =
		"  select ddept_name,textcat_commacat(test_name) as test_names,count(test_id) from tests_prescribed   " +
		"  JOIN diagnostics using(test_id) " +
		"  JOIN diagnostics_departments using(ddept_id) " +
		"  where report_id = ?  group by ddept_name  ";


	private static final String GET_REPORT_ID_FOR_PRESID = "SELECT tp.report_id FROM tests_prescribed tp " +
			"join test_visit_reports tvr on(tvr.report_id = tp.report_id) where tp.prescribed_id=? ";
	public static final int getReportIdForPrescription(int prescribedId)throws SQLException{
		int reportId=0;
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = con.prepareStatement(GET_REPORT_ID_FOR_PRESID);
		ps.setInt(1, prescribedId);

		String reportIdStr = DataBaseUtil.getStringValueFromDb(ps);
		if(reportIdStr !=null && !reportIdStr.equals(""))
			reportId = Integer.parseInt(reportIdStr);
		DataBaseUtil.closeConnections(con, ps);
		return reportId;
	}



	private static final String UPDATE_REPORT_FIELDS = "UPDATE test_visit_reports SET  " +
			" report_date=current_timestamp, user_name=?";
	private static final String UPDATE_REPORT_WHERE_COND = " WHERE report_id=? AND patient_id=? ";

	public static boolean setReportContent(int reportId,String visitId, String username,
			boolean updatePHTemplate, Integer pheader_template_id) throws SQLException {
		boolean status = false;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			String UPDATE_REPORT = "";
			if (updatePHTemplate)
				UPDATE_REPORT = UPDATE_REPORT_FIELDS + ", pheader_template_id=? " + UPDATE_REPORT_WHERE_COND;
			else
				UPDATE_REPORT = UPDATE_REPORT_FIELDS + UPDATE_REPORT_WHERE_COND;

			ps = con.prepareStatement(UPDATE_REPORT);

			int i=1;

			ps.setString(i++, username);
			if (updatePHTemplate)
				ps.setObject(i++, pheader_template_id);
		    ps.setInt(i++, reportId);
			ps.setString(i++, visitId);


			if (ps.executeUpdate() > 0) status = true;

		}finally{
			ps.close();
			DataBaseUtil.commitClose(con, status);
		}
		return status;
	}

	private static final String REPORT_INIT_WHERE = "WHERE dd.category =? "+
	                                                " AND ( patient_confidentiality_check(COALESCE(pd.patient_group, 0),pd.mr_no) )";

	private static final String REPORT_LIST_QUERY_FIELDS=
		" SELECT  tp.mr_no, tv.report_id, tv.report_date, tp.pat_id,him.impression_id,him.short_impression, " +
		" tv.report_name, isr.patient_name,tv.addendum_signed_off," +
		" (CASE WHEN tv.report_addendum IS NOT NULL THEN 'Y' ELSE 'N' END) as has_addendum , " +
		" get_patient_name(pd.salutation, pd.patient_name, pd.middle_name, pd.last_name) AS name,pd.email_id, " +
		" tv.handed_over,tv.handed_over_to,tv.hand_over_time,tv.num_prints, coalesce(ipr.center_id, pr.center_id) as source_center_id," +
		" isr.incoming_source_type, tv.signoff_center, COALESCE(pr.reference_docto_id,isr.referring_doctor) as reference_docto_id," +
		" isr.center_id AS incoming_test_center, tv.report_results_severity_status,tv.notification_sent, " +
		" CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END AS hospital, " +
		" case when coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' then 'R' else 'S' end as patient_sponsor_type, " +
		" coalesce((select patient_due_amnt from diag_report_sharing_on_bill_payment where report_id = tv.report_id limit 1), 0) as partial_patient_due ";

	private static final String REPORT_LIST_QUERY_COUNT =
		"SELECT COUNT(DISTINCT tv.report_id) ";

	private static final String REPORT_LIST_QUERY_TABLES =
		" FROM test_visit_reports tv " +
			" JOIN tests_prescribed tp on tv.report_id= tp.report_id " +
			" JOIN diagnostics d  on d.test_id = tp.test_id " +
			" JOIN diagnostics_departments dd on dd.ddept_id = d.ddept_id " +
			" LEFT JOIN test_histopathology_results thr USING(prescribed_id)" +
			" LEFT JOIN histo_impression_master him ON(him.impression_id = thr.impression_id)" +
			" LEFT JOIN patient_registration pr ON pr.patient_id = tp.pat_id " +
			" LEFT JOIN patient_details pd on pd.mr_no = pr.mr_no " +
			" LEFT JOIN tpa_master tpa ON (pr.primary_sponsor_id=tpa.tpa_id) " +
			" LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = tp.pat_id " +
			//" LEFT JOIN incoming_sample_registration_details isrd ON (isrd.prescribed_id = tp.prescribed_id AND " +
			//"	isr.incoming_visit_id = isrd.incoming_visit_id) " +
			// these are used to get the sponsor type of the original visit for internal lab patient.
			" LEFT JOIN tests_prescribed itp on (itp.prescribed_id=tp.coll_prescribed_id) " +
			" LEFT JOIN patient_registration ipr on (ipr.patient_id=itp.pat_id) " +
			" LEFT JOIN tpa_master itpa ON (itpa.tpa_id=ipr.primary_sponsor_id) " +

			" LEFT JOIN incoming_hospitals ih ON (ih.hospital_id = isr.orig_lab_name) " +
			" LEFT JOIN outsource_sample_details oh ON (oh.visit_id = tp.pat_id) " +
			" LEFT JOIN diag_outsource_master dom on (dom.outsource_dest_id = oh.outsource_dest_id) "+
			" LEFT JOIN outhouse_master om ON (om.oh_id = dom.outsource_dest)" ;

	private static final String GROUP_BY = "tp.mr_no,isr.mr_no, pd.salutation,pd.patient_name, pd.middle_name, " +
			" pd.last_name, tv.report_id,pd.email_id,tp.pat_id, pr.reference_docto_id, isr.referring_doctor," +
			" tv.report_date, tv.report_name, isr.patient_name,tv.addendum_signed_off, " +
			" tv.handed_over,tv.handed_over_to,tv.hand_over_time," +
			" tv.num_prints,tv.report_addendum,him.impression_id,him.short_impression, " +
			" ipr.center_id, pr.center_id, isr.incoming_source_type, " +
			" tv.signoff_center, isr.center_id, " +
			" case when coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' then 'R' else 'S' end, " +
			" CASE WHEN pr.patient_id IS NOT NULL THEN 'hospital' ELSE 'incoming' END  ";

	public PagedList getAllReports(String mrno, String labno, String firstName, String patientName,
			String department, String testName, int pageNum, List patient, List status, List testType,
			Date fromDate, Date toDate, Date fromRDate, Date toRDate, String sortFeild, boolean sortReverse,
			String category, boolean showOnlyInhosueVisits, List houses, String inHouse,
			String outHouse, boolean signedOff, String phoneNo,String[] readyForHandover,String handedOver,String sampelNo,
			String shortImpression,String collectionCenterId,int userSampleCollectionCenterId,
			String[] priority, String[] patientSponsorType, String reference_docto_id, List<String> reportSeveritySearchList, String presDoctor) throws SQLException {

		PreparedStatement ps = null;
		Connection con = null;
		List reportList = null;
		int totalCount = 0;
		try{
			con =  DataBaseUtil.getReadOnlyConnection();
			if(sortFeild != null){

			}else{
				sortFeild = " tv.report_id ";
				sortReverse = true;
			}

			SearchQueryBuilder qb = null;
			qb = new SearchQueryBuilder(con, REPORT_LIST_QUERY_FIELDS, REPORT_LIST_QUERY_COUNT,
					REPORT_LIST_QUERY_TABLES, REPORT_INIT_WHERE,GROUP_BY,  sortFeild, sortReverse, 20, pageNum);

			// add the value for the initial where clause
			qb.addInitValue(SearchQueryBuilder.STRING, category);

			qb.addFilter(SearchQueryBuilder.STRING, "tp.mr_no", "=", mrno);
			qb.addFilter(SearchQueryBuilder.STRING, "tp.labno", "=", labno);
			qb.addFilter(SearchQueryBuilder.STRING, "d.ddept_id", "=", department);
			qb.addFilter(SearchQueryBuilder.STRING, "COALESCE(pd.patient_name ||' '|| pd.last_name, isr.patient_name) ", "ilike", patientName);
			qb.addFilter(SearchQueryBuilder.STRING, "tp.prescription_type", "IN", testType);
			qb.addFilter(SearchQueryBuilder.STRING, "d.test_name", "ilike", testName);
			qb.addFilter(SearchQueryBuilder.STRING, "COALESCE(itp.sample_no, tp.sample_no)", "=", sampelNo);
			qb.addFilter(SearchQueryBuilder.DATE,   "date_trunc('day',tp.pres_date)", ">=", fromDate);
			qb.addFilter(SearchQueryBuilder.DATE,   "date_trunc('day',tp.pres_date)", "<=", toDate);
			qb.addFilter(SearchQueryBuilder.DATE,   "date_trunc('day', tv.report_date::date)", ">=", fromRDate == null ? null : fromRDate);
			qb.addFilter(SearchQueryBuilder.DATE,   "date_trunc('day', tv.report_date::date)", "<=", toRDate == null ? null : toRDate);
			qb.addFilter(SearchQueryBuilder.STRING, "tp.prescription_type", "IN", houses);
			qb.addFilter(SearchQueryBuilder.STRING, "ih.hospital_name", "ilike", inHouse);
			qb.addFilter(SearchQueryBuilder.STRING, "om.oh_name", "ilike", outHouse);
			qb.addFilter(SearchQueryBuilder.STRING, "him.short_impression", "ilike", shortImpression);
			qb.addFilter(SearchQueryBuilder.STRING, "tv.report_state", "!=", "D");//to avoid discarded reports
			qb.addFilter(SearchQueryBuilder.STRING, "tp.conducted", "!=", "RAS");//to avoid Reconducted After Signedoff tests
			qb.addFilter(SearchQueryBuilder.STRING, "tv.report_results_severity_status", "IN", reportSeveritySearchList);

			if (patientSponsorType != null && patientSponsorType.length != 0 && !patientSponsorType[0].equals(""))
				qb.addFilter(SearchQueryBuilder.STRING,
						" (case when coalesce(tpa.sponsor_type, itpa.sponsor_type, 'R')='R' then 'R' else 'S' end) ",
						"IN",  Arrays.asList(patientSponsorType));
			//mobile number filter			
			if (phoneNo != null && !phoneNo.isEmpty()) {
				ArrayList<Integer> mobileNumTypeList = new ArrayList<Integer>();
				mobileNumTypeList.add(SearchQueryBuilder.STRING);
				mobileNumTypeList.add(SearchQueryBuilder.STRING);
				ArrayList<String> mobileNumValueList = new ArrayList<String>();
				mobileNumValueList.add(phoneNo);
				mobileNumValueList.add(phoneNo);
				qb.appendExpression(" AND ( isr.phone_no = ? OR regexp_replace(isr.phone_no,'^\\' || COALESCE(isr.phone_no_country_code,' ') ,'') = ? ) ",
						mobileNumTypeList, mobileNumValueList);
			}
			
			int centerId = RequestContext.getCenterId();

			ArrayList centerTypesList = new ArrayList();
			centerTypesList.add(SearchQueryBuilder.INTEGER);
			centerTypesList.add(SearchQueryBuilder.INTEGER);

			ArrayList centerValuesList = new ArrayList();
			centerValuesList.add(centerId);
			centerValuesList.add(centerId);
			if (centerId != 0)
				qb.appendExpression( "AND ( pr.center_id = ? OR isr.center_id = ? )",centerTypesList,centerValuesList );

			if(null != collectionCenterId && !collectionCenterId.equals("")) {
				qb.addFilter(SearchQueryBuilder.INTEGER, "pr.collection_center_id","=",Integer.parseInt(collectionCenterId));
			} else {
				if(userSampleCollectionCenterId != -1){
					qb.addFilter(SearchQueryBuilder.INTEGER, "pr.collection_center_id","=",userSampleCollectionCenterId);
				}
			}

			if(handedOver != null &&  !handedOver.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "tv.handed_over", "=", handedOver);
			if(priority != null && !priority[0].equals("") && priority.length!=2)
				qb.addFilter(SearchQueryBuilder.STRING, "tp.priority", "=", priority[0]);

			qb.addFilter(SearchQueryBuilder.STRING, "tv.signed_off", "=", "Y");
			if(reference_docto_id != null && !reference_docto_id.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "COALESCE(pr.reference_docto_id,isr.referring_doctor) ", "=", reference_docto_id);

			if(presDoctor !=null && !presDoctor.equals(""))
				qb.addFilter(SearchQueryBuilder.STRING, "tp.pres_doctor","=",presDoctor);

			StringBuilder visitTypeConstratint = new StringBuilder();

			if ( patient != null )
			for (String type: (List<String>)patient)
				if (type.equals(Bill.BILL_VISIT_TYPE_IP))
					if  (visitTypeConstratint.length() > 0)
						visitTypeConstratint.append(" OR pr.visit_type = 'i'");
					else
						visitTypeConstratint.append("  ( pr.visit_type = 'i'");
				else if (type.equals(Bill.BILL_VISIT_TYPE_OP))
					if  (visitTypeConstratint.length() > 0)
						visitTypeConstratint.append(" OR pr.visit_type = 'o'");
					else
						visitTypeConstratint.append("  ( pr.visit_type = 'o'");
				else if (type.equals(Bill.BILL_VISIT_TYPE_INCOMING))
					if  (visitTypeConstratint.length() > 0)
						visitTypeConstratint.append(" OR isr.incoming_visit_id IS NOT NULL ");
					else
						visitTypeConstratint.append("  ( isr.incoming_visit_id IS NOT NULL ");

			if ( visitTypeConstratint.length() > 0) {
				visitTypeConstratint.append(" )");
				qb.appendToQuery(visitTypeConstratint.toString());
			}
			if(readyForHandover != null && !readyForHandover[0].equals("") && readyForHandover.length != 2){
				//Filter for ready for handover
				if(readyForHandover[0].equals("Y")){
					qb.appendToQuery(" (select patient_due_amnt from diag_report_sharing_on_bill_payment where report_id = tv.report_id limit 1)  IS NULL ");
				}
				else{
					qb.appendToQuery(" (select patient_due_amnt from diag_report_sharing_on_bill_payment where report_id = tv.report_id limit 1) IS NOT NULL  ");
				}				
			}

			qb.build();

			PreparedStatement psData = qb.getDataStatement();
			PreparedStatement psCount = qb.getCountStatement();
			reportList = DataBaseUtil.queryToDynaList(psData);
			totalCount = Integer.parseInt(DataBaseUtil.getStringValueFromDb(psCount));
			psData.close(); psCount.close();

		} finally {
			if(con != null) con.close();
			if(ps != null) ps.close();
		}

		return new PagedList(reportList, totalCount, 20, pageNum);
	}

	private static final String GET_TESTS_OF_REPORTS =
		" SELECT tv.report_id, tp.pat_id, test_name,prescribed_id,tp.priority,'(' ||pm.package_name|| ')' as package_name," +
		" tp.outsource_dest_prescribed_id, tp.mr_no, dom.outsource_dest_type " +
		" FROM test_visit_reports tv " +
		" JOIN tests_prescribed tp USING (report_id) LEFT JOIN patient_details pd ON(pd.mr_no=tp.mr_no)" +
		" LEFT JOIN package_prescribed PP ON(pp.prescription_id = tp.package_ref ) " +
		" LEFT JOIN packages pm USING(package_id) " +
		" JOIN diagnostics d USING (test_id) "+ 
		" LEFT JOIN diag_outsource_master dom ON (dom.outsource_dest_id = tp.outsource_dest_id) " +
		" WHERE tp.conducted != 'RAS' AND report_id IN (";

	public static List getTestNamesOfReports(List rIdList,String[] priority,String pres_doctor) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		List<BasicDynaBean> list = null;
		StringBuilder query = new StringBuilder(GET_TESTS_OF_REPORTS);
		boolean first = true;
		try{
			con =  DataBaseUtil.getReadOnlyConnection();
			if (rIdList.size() > 0) {
				for (Integer rId: (List<Integer>)rIdList) {
					if (first) {
						query.append("?");
						first = false;
					}
					else
						query.append(", ?");
				}
				query.append(" )");
				if(priority != null && !priority[0].equals("") && priority.length!=2) {
					query.append(" AND tp.priority=? ");
				}
				if(pres_doctor!=null && !pres_doctor.equals("")){
					query.append("AND tp.pres_doctor=? ");
				}
				int i = 1;
				ps = con.prepareStatement( query.toString() );
				for ( Integer rId: (List<Integer>)rIdList )
					ps.setInt(i++, rId);
				if(priority != null && priority.length!=2 && !priority[0].equals(""))
					ps.setString(i++, priority[0]);
				if(pres_doctor!=null && !pres_doctor.equals(""))
					ps.setString(i,pres_doctor);
				list = DataBaseUtil.queryToDynaList(ps);
			}


		}finally{
			if(ps != null) ps.close();
			if(con != null) con.close();
		}
		return list;
	}


	private static final String GET_REPORT = "SELECT report_id, report_name, report_mode, user_name, " +
		" to_char(report_date,'dd-mm-yyyy') as report_date, to_char(report_date, 'hh24:mi') as report_time, " +
		" category, pheader_template_id, patient_id, signed_off,report_addendum, handed_over, signoff_center, signedoff_by, " +
		" hl7_obr_segment, (SELECT string_agg(remarks, '<br/>')" + 
		" FROM report_review_history WHERE entity_id = ? " + 
		" GROUP BY entity_id) AS review_remarks FROM test_visit_reports  WHERE report_id = ?";

	public ArrayList getReport(int reportid)throws SQLException{
		PreparedStatement ps = null;
		Connection con = null;
		ArrayList list = null;
		try{
			con =  DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT );
			ps.setInt(1, reportid);
			ps.setInt(2, reportid);
			list = DataBaseUtil.queryToArrayList(ps);
		}finally{
			if(ps != null) ps.close();
			if(con != null) con.close();
		}
		return list;
	}

	public static BasicDynaBean getReportDynaBean(int reportid) throws SQLException {
		PreparedStatement ps = null;
		Connection con = null;
		try {
			con =  DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement(GET_REPORT );
			ps.setInt(1, reportid);
			ps.setInt(2, reportid);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static BasicDynaBean getReportDynaBean(Connection con,int reportid) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_REPORT );
			ps.setInt(1, reportid);
			ps.setInt(2, reportid);
			return DataBaseUtil.queryToDynaBean(ps);
		} finally {
			DataBaseUtil.closeConnections(null, ps);
		}
	}


	private static final String GET_REPORTS = "SELECT report_id, report_name, report_mode, user_name, " +
		" to_char(report_date,'dd-mm-yyyy') as report_date, to_char(report_date, 'hh24:mi') as report_time, " +
		" category, pheader_template_id, patient_id, signed_off,report_addendum, handed_over " +
		" FROM test_visit_reports ";
	public static List getReports(String[] reportIds) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where, "report_id", Arrays.asList(reportIds));

			ps = con.prepareStatement( GET_REPORTS + where );
			int i=1;
			for (String reportId : reportIds) {
				ps.setInt(i++, Integer.parseInt(reportId));
			}

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_REPORTS_FOR_VISIT = "SELECT DISTINCT tvr.report_id, tvr.report_name, tvr.report_mode, tvr.user_name, " +
		" to_char(tvr.report_date,'dd-mm-yyyy') as report_date, to_char(tvr.report_date, 'hh24:mi') as report_time, " +
		" tvr.category, tvr.pheader_template_id, tp.pat_id, tvr.signed_off, tvr.report_addendum, tvr.handed_over, " +
		" tp.pat_id AS patient_id " +
		" FROM test_visit_reports tvr" +
		" JOIN tests_prescribed tp ON (tp.report_id = tvr.report_id)";

	public static List getReports(String patientId, String category) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_REPORTS_FOR_VISIT + " WHERE tp.pat_id=? and tvr.category=? and tvr.signed_off='Y'");
			ps.setString(1, patientId);
			ps.setString(2, category);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String DELETE_REPORT = "DELETE FROM test_visit_reports  WHERE report_id = ? AND signed_off = 'N'";
	private static final String UPDATE_TESTS_PRESCRIBED = "UPDATE tests_prescribed SET report_id = NULL WHERE report_id = ?";
	public int deleteReport( String reportid)throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		int result = 0;
		try{
			con =  DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			ps = con.prepareStatement(DELETE_REPORT );
			ps.setInt(1, Integer.parseInt(reportid));
			result = ps.executeUpdate();
			if (result > 0){
				ps = con.prepareStatement(UPDATE_TESTS_PRESCRIBED);
				ps.setInt(1, Integer.parseInt(reportid));
				result = ps.executeUpdate();
			}
		}finally{
			if (result >0){
				con.commit();
			}else{
				con.rollback();
			}
			if(con != null) con.close();
			if(ps != null) ps.close();
		}
		return result;
	}

	private static final String GET_REPORT_IDS = "select mr_no ,report_id from TEST_VISIT_REPORTS tv " +
		"left join patient_registration pr on  pr.patient_id=tv.patient_id";

	public ArrayList gerReportIds(String sortFeild, boolean sortReverse)throws Exception{
		PreparedStatement ps = null;
		Connection con = null;
		ArrayList reportIds = null;
		try{
			con =  DataBaseUtil.getConnection();
			if (sortFeild == null) sortFeild = " report_id ";
			if (sortReverse)
				ps = con.prepareStatement(GET_REPORT_IDS + " order by "+ sortFeild);
			else
				ps = con.prepareStatement(GET_REPORT_IDS + " order by "+ sortFeild+" DESC");
			reportIds = DataBaseUtil.queryToArrayList(ps);
		}finally{
			if(con != null) con.close();
			if(ps != null) ps.close();
		}
		return reportIds;
	}

	/**
	 * This will return all pending testa of an in patients
	 * @return List of tests
	 * @throws SQLException
	 */
	public static List getAllPendingTests(String visittype)throws SQLException{
		Connection con =null;
		PreparedStatement ps = null;
		List tests = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement("SELECT MR_NO, PAT_ID FROM TESTS_PRESCRIBED WHERE PAT_ID LIKE ? AND CONDUCTED='N'");
			ps.setString(1, visittype.concat("%"));
			tests = DataBaseUtil.queryToArrayList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
		return  tests;
	}

	/**
	 * Returns true if that visitid has any pending test other wise false
	 * @param visitid
	 * @return
	 * @throws SQLException
	 */
	public static boolean isPendingTestExist(String visitid)throws SQLException{
		boolean testsexist = false;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs =null;
		try{
			con = DataBaseUtil.getReadOnlyConnection();
			ps = con.prepareStatement("SELECT TEST_ID FROM TESTS_PRESCRIBED WHERE PAT_ID=? and conducted != 'C'");
			ps.setString(1, visitid);
			rs = ps.executeQuery();
			while(rs.next()){
				testsexist = true;
				break;
			}
		}finally{
			DataBaseUtil.closeConnections(con, ps, rs);
		}

		return testsexist;
	}
	
	public static boolean isPendingTestExist(Connection con, String visitid)throws SQLException{
    boolean testsexist = false;
    PreparedStatement ps = null;
    ResultSet rs =null;
    try{
      ps = con.prepareStatement("SELECT TEST_ID FROM TESTS_PRESCRIBED WHERE PAT_ID=? and conducted != 'C'");
      ps.setString(1, visitid);
      rs = ps.executeQuery();
      while(rs.next()){
        testsexist = true;
        break;
      }
    }finally{
      DataBaseUtil.closeConnections(null, ps, rs);
    }

    return testsexist;
  }


	public static final String UPDATE_REPORT_PRESCRIPTION = "UPDATE tests_prescribed SET report_id =? WHERE " +
			" prescribed_id=?";

	public static boolean updateReportPrescription(Connection con , TestPrescribed tp)throws SQLException{
		boolean status = false;
		PreparedStatement ps = con.prepareStatement(UPDATE_REPORT_PRESCRIPTION);

		ps.setInt(1, tp.getReportId());
		ps.setInt(2, tp.getPrescribedId());

		int i = ps.executeUpdate();
		ps.close();

		if(i>0)status = true;

		return status;
	}

	public static String UPDATE_SIGN_OFF_REPORT = "update test_visit_reports SET signed_off='Y', report_date = LOCALTIMESTAMP(0),report_state = 'S'," +
			"signoff_center = ? , user_name = ?, signedoff_by = ?  where   report_id=?";
	public static String UPDATE_TEST_PRESCRIBED = "update tests_prescribed SET conducted='S', user_name = ? where report_id=? and conducted != 'X'";

  public boolean upDateSignofTests(Connection con, Integer reportId, String username)
      throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(UPDATE_TEST_PRESCRIBED)) {
      ps.setString(1, username);
      ps.setInt(2, reportId);
      return ps.executeUpdate() > 0;
    }
  }

	public static boolean upDateSignofReports(Connection con,
			ArrayList<TestVisitReports> signOfList, String username) throws SQLException {

		boolean status = false;
		DiagnosticsDAO diagDAO = new DiagnosticsDAO();
		PreparedStatement ps = con.prepareStatement(UPDATE_SIGN_OFF_REPORT);
		PreparedStatement ps1 = con.prepareStatement(UPDATE_TEST_PRESCRIBED);
		PreparedStatement ps2 = con.prepareStatement("");
		int centerId = RequestContext.getCenterId();

		Iterator<TestVisitReports> it = signOfList.iterator();
		try {
			while (it.hasNext()) {
				TestVisitReports tvr = it.next();
				ps.setInt(1, centerId);
				ps.setString(2, username);
				ps.setString(3, username);
				ps.setInt(4, tvr.getReportId());
				ps.addBatch();

				ps1.setString(1, username);
				ps1.setInt(2, tvr.getReportId());
				ps1.addBatch();


				//update test result status
				diagDAO.updateTestResultStatus(con, tvr.getReportId(), "S");
			}


			do{
				int[] p = ps.executeBatch();
				status = DataBaseUtil.checkBatchUpdates(p);
				if(!status)break;

				int[] p1 = ps1.executeBatch();
				status = DataBaseUtil.checkBatchUpdates(p1);
				if(!status)break;

			}while(false);



		} finally {
			ps.close();
			ps1.close();
		}
		return status;
	}

	private final static String NO_OF_REAGENTS_REQUIRED_QUERY = "SELECT COUNT(*) FROM diagnostics_reagents WHERE test_id = ? and status='A'";
	private final static String REAGENTS_REQUIRED_QUERY = "SELECT reagent_id,quantity_needed FROM diagnostics_reagents WHERE test_id=? and status='A'";
	private final static String NO_OF_REAGENTS_AVBL_IN_STOCK = "SELECT COUNT(*) FROM store_stock_details where medicine_id = ? and dept_id = 1";
	private final static String REAGENTS_AVBL_IN_STOCK = "SELECT item_identifier,qty_avbl FROM store_stock_details WHERE medicine_id=? AND dept_id=-1 ORDER BY stock_time";
	private final static String GET_TEST_IDS = "SELECT test_id, prescribed_id FROM tests_prescribed WHERE  report_id = ?";
	private final static String GET_ITEM_CAT_IDENTIFICATION = "select identification from store_category_master "
                          +" join store_item_details using(category_id) where medicine_id=? ";

	public static boolean updateReagents( Connection con, ArrayList<TestVisitReports> signOfList ,
			Preferences pref, String userName) throws SQLException, IOException{

		String invModAct = null;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		ResultSet rs = null;
		PreparedStatement identifiersPs = null;
		ResultSet identifiersRs = null;
		boolean status = true;
		boolean serialInsufficentQty = false;

		BasicDynaBean bean = null;
		BasicDynaBean reagentBean = null;

		if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
			invModAct = (String)pref.getModulesActivatedMap().get("mod_inventory");
			if(invModAct == null || invModAct == ""){
				invModAct = "N";
			}
		}
		if (invModAct.equalsIgnoreCase("Y")){
		Iterator<TestVisitReports> it = signOfList.iterator();
		ps = con.prepareStatement(GET_TEST_IDS);
		
		GenericDAO adjDAo = new GenericDAO("store_reagent_usage_details");
		
		while ( it.hasNext()) {
			ps.setInt(1, it.next().getReportId());
			List<BasicDynaBean> testIds = DataBaseUtil.queryToDynaList(ps);
			ps.close();
			for ( int i=0; i < testIds.size(); i++ ) {
					String testId = (String)testIds.get(i).get("test_id");
					int perscribedId = ((BigDecimal)testIds.get(i).get("prescribed_id")).intValue();
					PreparedStatement cPs = con.prepareStatement(NO_OF_REAGENTS_REQUIRED_QUERY);
					cPs.setString(1, testId);
					int noOfReagentsReq = Integer.parseInt(DataBaseUtil.getStringValueFromDb(cPs));
					int reagentSeq =  storeReagentUsageMainDAO.getNextSequence();

					if (noOfReagentsReq > 0) {
						bean = storeReagentUsageMainDAO.getBean();
						bean.set("store_id","ISTOREDIAG");
						java.util.Date d = new java.util.Date();
						java.sql.Timestamp dt =new java.sql.Timestamp(d.getTime());
						bean.set("date_time", dt);
						bean.set("consumer_id",testId);
						bean.set("user_name",userName);
						bean.set("ref_no", perscribedId);
						bean.set("reagent_usage_seq", reagentSeq);
						status = storeReagentUsageMainDAO.insert(con, bean);
					}
					int[] reagentRequired = new int[noOfReagentsReq];
					BigDecimal[] qtyRequired = new BigDecimal[noOfReagentsReq];

					ps1 = con.prepareStatement( REAGENTS_REQUIRED_QUERY );
					ps1.setString(1, testId);
					rs = ps1.executeQuery();
					int j = 0;
					while (rs.next()){
						reagentRequired[j] = rs.getInt(1);
						qtyRequired[j] = rs.getBigDecimal(2);
						j ++;
					}
					rs.close();
					ps1.close();

					int noOfReagents = j;
					String itemIdentification = "";
					for ( int reagent=0; reagent < noOfReagents; reagent++ ) {

						cPs = con.prepareStatement(NO_OF_REAGENTS_AVBL_IN_STOCK);
						cPs.setInt(1, reagentRequired[reagent] );
						int noOfItemIdents = Integer.parseInt(DataBaseUtil.getStringValueFromDb(cPs));

						identifiersPs = con.prepareStatement( REAGENTS_AVBL_IN_STOCK );
						identifiersPs.setInt(1, reagentRequired[reagent]);
						identifiersRs = identifiersPs.executeQuery();

						PreparedStatement identPs = con.prepareStatement(GET_ITEM_CAT_IDENTIFICATION);
						identPs.setInt(1, reagentRequired[reagent] );
						itemIdentification = DataBaseUtil.getStringValueFromDb(identPs);

						BigDecimal requiredQty = null;
						BigDecimal reduceQty = new BigDecimal(0);
						int identifierCount = 1;

						requiredQty = qtyRequired[reagent];
						while (identifiersRs.next()) {
							serialInsufficentQty = false;
							reduceQty = new BigDecimal(0);
							BigDecimal qtyAvblInStock = identifiersRs.getBigDecimal("qty_avbl");
							String identifier = identifiersRs.getString("item_identifier");
							BigDecimal remainQtyInStock = qtyAvblInStock.subtract(requiredQty);

							if (noOfItemIdents == identifierCount) {
								if (remainQtyInStock.floatValue() > 0)reduceQty = requiredQty;
								else {
									if (itemIdentification.equalsIgnoreCase("B")) reduceQty = requiredQty;
									else {
										if (qtyAvblInStock.floatValue() > 0) reduceQty = qtyAvblInStock;
										else reduceQty = new BigDecimal("0");
										serialInsufficentQty = true;
										if (qtyAvblInStock.floatValue() < requiredQty.floatValue())
											logger.error("@ Test Id :"+testId+",Conduction time Item Id :"+reagentRequired[reagent]+" has insufficient stock in DIAG STORE ");
									}
								}
							}else if (remainQtyInStock.compareTo(BigDecimal.ZERO) != -1 ){
								reduceQty = requiredQty;
							}else if (qtyAvblInStock.compareTo(BigDecimal.ZERO) == 1){
								reduceQty = qtyAvblInStock;
							}
							identifierCount++;

							if (status && reduceQty.compareTo(BigDecimal.ZERO) == 1)
								status = LaboratoryDAO.updateStock(con,reagentRequired[reagent],identifier,reduceQty);

							reagentBean = adjDAo.getBean();
							reagentBean.set("item_id",reagentRequired[reagent]);
							reagentBean.set("item_identifier",identifier);
							reagentBean.set("qty",reduceQty);
							reagentBean.set("ref_no",perscribedId);
							reagentBean.set("reagent_usage_seq", reagentSeq);

							if ((status && (reduceQty.compareTo(BigDecimal.ZERO) == 1)) || (status && serialInsufficentQty)) {
								status = adjDAo.insert(con, reagentBean);
							}
							reagentBean = null;

							requiredQty = requiredQty.subtract(reduceQty);
							if (requiredQty.compareTo(BigDecimal.ZERO) == 0 )
								break;
						}
						identifiersRs.close();
						identifiersPs.close();
					}
				}
			}
		}
		 return status;
	}


	/*
	 * Update or insert a report format for a prescribed ID
	 */
	private static final String CHECK_TEST_DETAILS ="SELECT count(*) from test_details " +
		" where test_detail_status not in ('A','S') and prescribed_id=? and test_details_id=? ";

	private static final String UPDATE_REPORT_FORMAT =
		"UPDATE test_details SET format_name=?,patient_report_file=? WHERE prescribed_id=? AND test_details_id = ?";

	private static final String INSERT_REPORT_FORMAT = "INSERT INTO test_details("+
	    " mr_no, patient_id, test_id, prescribed_id, "+
	    " conducted_in_reportformat, format_name, patient_report_file,"+
	    " withinnormal,test_details_id)"+
		" SELECT mr_no, pat_id, test_id, prescribed_id, 'Y', ?, ?, 'Y',? " +
		"  FROM tests_prescribed where prescribed_id=?";

	public static boolean updateReportFormat(Connection con, int presId, String fmtId, String content
			,int testDetailsId,List<Integer> newTestDetailsId,String testId)
		throws SQLException {

		boolean status  = false;

		PreparedStatement ps = con.prepareStatement(CHECK_TEST_DETAILS);
		ps.setInt(1, presId);
		ps.setInt(2,testDetailsId);
		String count = DataBaseUtil.getStringValueFromDb(ps);
		ps.close();

		if (count.equals("0")) {
			ps = con.prepareStatement(INSERT_REPORT_FORMAT);

			newTestDetailsId.add(new GenericDAO("test_details").getNextSequence());
			ps.setString(1, fmtId);
			ps.setString(2, content);
			ps.setInt(3, newTestDetailsId.get(0));
			ps.setInt(4, presId);

			int j = ps.executeUpdate();
			if(j>0){
				status = true;
			}
		} else {
			ps = con.prepareStatement(UPDATE_REPORT_FORMAT);
			ps.setString(1, fmtId);
			ps.setString(2, content);
			ps.setInt(3, presId);
			if(testDetailsId==0) {
				int testdetId = RadiologyDAO.getTestDetailId(testId, presId);
				ps.setInt(4, testdetId);
			}else{
				ps.setInt(4,testDetailsId);
			}
			int j = ps.executeUpdate();

			if(j>0){
				status = true;
			}
		}
		return status;
	}

	public static final String REPORTS_LIST =
		"SELECT report_id,report_addendum FROM test_visit_reports where patient_id=?";

	public static final String REPORTS_LIST_FOR_WEB_ACCESS = "SELECT distinct tvr.report_id,report_addendum  "+
			" FROM test_visit_reports tvr " +
			" JOIN tests_prescribed tp ON (tp.report_id = tvr.report_id) "+
			" WHERE tvr.report_id not in (SELECT report_id FROM tests_prescribed WHERE conducted in ('RAS','RBS')" +
			" AND report_id is not null AND pat_id = ?) " +
			" AND tp.pat_id=? ";

	public static List<BasicDynaBean> getReportList( String patientId,boolean signedOff ) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			if(signedOff) {
				ps = con.prepareStatement(REPORTS_LIST_FOR_WEB_ACCESS + " AND signed_off = 'Y' ");
				ps.setString(1, patientId);
				ps.setString(2, patientId);
			} else {
				ps = con.prepareStatement(REPORTS_LIST);
				ps.setString(1, patientId);
			}
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String GET_RESULT_LABELS =
			" SELECT td.resultlabel, method_name, td.resultlabel_id, td.resultlabel || COALESCE(method_name, '') AS concat_result_method" +
					" FROM test_details td " +
						" JOIN tests_prescribed USING (prescribed_id) " +
						" JOIN test_visit_reports USING (report_id)" +
						" LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id AND td.resultlabel_id = tm.resultlabel_id) " +
						" LEFT JOIN diag_methodology_master dmm ON(tm.method_id = dmm.method_id) " +
						" LEFT JOIN incoming_sample_registration isr ON(td.patient_id = isr.incoming_visit_id)" +
					"WHERE COALESCE(td.mr_no, isr.mr_no)=? AND signed_off = 'Y'" +
					"GROUP BY td.resultlabel, method_name, td.resultlabel_id "+
					"ORDER BY td.resultlabel";
	public static List getResultLabels(String mrno) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_RESULT_LABELS);
			ps.setString(1, mrno);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	 public static final String GET_TEST_VALUES =
			 " SELECT td.resultlabel || CASE WHEN dmm.method_name is not null THEN  ' (' || coalesce(dmm.method_name, '') ||')' " +
			 " ELSE '' end as resultname ," +
			 " td.resultlabel_id, report_value, conducted_date AS pres_date, " +
			 " to_char(conducted_date, 'dd-MM-yyyy HH24:mi') AS conducted_date_txt, " +
			 " td.units FROM test_details td " +
			 	" JOIN tests_prescribed USING (prescribed_id) " +
			 	" JOIN test_visit_reports USING (report_id) " +
			 	" JOIN tests_conducted tc USING (prescribed_id) " +
				" LEFT JOIN test_results_master tm ON (tm.test_id = td.test_id AND td.resultlabel_id = tm.resultlabel_id) " +
				" LEFT JOIN diag_methodology_master dmm ON(tm.method_id = dmm.method_id) " +
				" LEFT JOIN incoming_sample_registration isr ON(td.patient_id = isr.incoming_visit_id)" +
			" WHERE COALESCE(td.mr_no, isr.mr_no)=? AND signed_off = 'Y' "+
			" AND conducted != 'RAS' ";


	public static List getTestValues(String mrno, String[] testValues, Date fromDate, Date toDate, Integer limitOfRecords)
					throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		StringBuffer query = new StringBuffer(GET_TEST_VALUES);
		try {
			boolean first = true;
			if (testValues != null && testValues.length > 0) {
				query.append(" AND td.resultlabel || COALESCE(method_name, '') in (");
				for (String value: testValues) {
					if (first)
						query.append("?");
					else
						query.append(",?");
					first = false;
				}
				query.append(")");
			}
			if (fromDate != null){
				query.append(" AND tc.conducted_date::DATE >= ?");
				query.append(" AND tc.conducted_date::DATE <= ?");
			}
			query.append(" ORDER BY conducted_date,conducted_time,test_details_id");
			if(limitOfRecords != null)
				query.append(" limit "+limitOfRecords);
			int i = 1;
			ps = con.prepareStatement(query.toString());
			ps.setString(i++, mrno);

			if ( testValues != null) {
				for (String value: testValues) {
					ps.setString(i++, value);
				}
			}
			if (fromDate != null){
				ps.setDate(i++, fromDate);
				ps.setDate(i++, toDate);
			}

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static boolean isCalculateResult(int resultlable_id)throws SQLException{
		BasicDynaBean resultMaster = new GenericDAO("test_results_master").
											findByKey("resultlabel_id", resultlable_id);
		if((String)resultMaster.get("expr_4_calc_result") == null
				|| ((String)resultMaster.get("expr_4_calc_result")).trim().isEmpty())
			return false;
		else
			return true;
	}

	private static final String ANTIBIOTIC_DETAILS=
		" SELECT antibiotic_name,anti_results," +
		" CASE WHEN susceptibility = 'R' THEN 'RESISTANT' " +
		" WHEN susceptibility = 'I' THEN 'INTERMEDIATE' " +
		" WHEN susceptibility = 'S' THEN 'SENSITIVE' END AS susceptibility " +
		" FROM test_micro_antibiotic_details " +
		" JOIN micro_abst_antibiotic_master USING(antibiotic_id) " +
		" LEFT JOIN micro_antibiotic_master USING(antibiotic_id)"+
		" WHERE test_org_group_id = ? AND abst_panel_id = ?";

	public static List<BasicDynaBean > getAntibioticDetails(Integer test_org_group_id,Integer abst_panel_id)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(ANTIBIOTIC_DETAILS);
			ps.setInt(1, test_org_group_id);
			ps.setInt(2, abst_panel_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static String micro_org_grp_det =
		" SELECT org_group_name,organism_name,abst_panel_name,"+
		"  resistance_marker,comments,test_org_group_id,tmr.abst_panel_id "+
		" FROM test_micro_org_group_details tmr "+
		"  LEFT JOIN micro_org_group_master USING(org_group_id) "+
		"  LEFT JOIN micro_organism_master mom USING(organism_id) "+
		"  LEFT JOIN micro_abst_panel_master mapm ON(mapm.abst_panel_id = tmr.abst_panel_id) "+
		" WHERE test_micro_id=?";
	public static List<BasicDynaBean> getMicroOrgGroupDetails(int test_micro_id)
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement(micro_org_grp_det);
			ps.setInt(1, test_micro_id);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public boolean updateReportId(Connection con,int oldReportId,int newReportId)throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement("UPDATE tests_prescribed SET conducted = 'C',report_id = ? " +
					"	WHERE report_id = ?");
			ps.setInt(1, newReportId);
			ps.setInt(2, oldReportId);

			return ps.executeUpdate() > 0;
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public boolean updateReportDetails(Connection con,int oldReportId,int newReportId)throws SQLException{
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		boolean status = false;
		try{
			ps = con.prepareStatement("UPDATE tests_prescribed SET report_id=?, conducted='S'  WHERE report_id=? ");
			ps.setInt(1, oldReportId);
			ps.setInt(2, newReportId);
			status =  ps.executeUpdate() > 0;
			ps1 = con.prepareStatement("UPDATE test_visit_reports SET report_state='S',revised_report_id=null WHERE report_id=?");
			ps1.setInt(1, oldReportId);
			status = status && ps1.executeUpdate() > 0;
			return status;
		}finally{
			if(ps1!=null) ps1.close();
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static String REPORT_WITH_ABNORMAL_RESULTS  =
		" SELECT distinct(withinnormal) AS withinnormal " +
		" FROM test_details td   "  +
		" JOIN tests_prescribed tp USING(prescribed_id) "  +
		" WHERE tp.report_id = ? ";

	public List<BasicDynaBean> reportHasAnyAbnormalResults(Connection con,int reportId)throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(REPORT_WITH_ABNORMAL_RESULTS);
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private static final String PENDING_TESTS =
		" SELECT 1 as slno,test_name as name," +
		" (CASE WHEN   sflag = '0' AND sample_needed = 'y' AND conducted = 'N' THEN 'Pending Sample Collection'" +
		"       ELSE 'Not Completed' END) as test_status ,* " +
		" FROM tests_prescribed " +
		" JOIN diagnostics USING ( test_id ) " +
		" WHERE pat_id = ? AND conducted NOT IN ( 'X','S','U','RBS','RAS' ) ";

	public static List<BasicDynaBean> getPendingTests( String visitId )
	throws SQLException{
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = DataBaseUtil.getConnection();
			ps = con.prepareStatement( PENDING_TESTS );
			ps.setString(1, visitId );
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	/**
	 * Returns a bean with test details of passed prescribed_id
	 */
	public static final String GET_TEST_DETAILS =
		" SELECT * FROM tests_prescribed " +
		" JOIN diagnostics USING ( test_id) " ;

	public BasicDynaBean getTestDetailsOfPrescription(Connection con , int prescribedId)
	throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_TEST_DETAILS+" WHERE prescribed_id = ? ");
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaBean(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public List<BasicDynaBean> getNonValueBasedTestDetailsOfReport(Connection con , int reportId)
	throws SQLException {
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(GET_TEST_DETAILS+" WHERE report_id = ? AND conduction_format = 'T' ");
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}
	public static final String UPDATE_TEST_RESULT_STATUS_QUERY =
		" UPDATE test_details td SET test_detail_status = ? " +
		" FROM tests_prescribed tp WHERE tp.report_id = ? " +
		" AND tp.prescribed_id = td.prescribed_id AND td.test_detail_status != 'A'  ";

	public static final String TEST_RESULT_STATUS_QUERY =
		" SELECT prescribed_id FROM test_details td  " +
		" JOIN tests_prescribed tp USING(prescribed_id) " +
		" WHERE tp.report_id = ? ";

	public boolean updateTestResultStatus( Connection con,int reportId,String resultStatus )
	throws SQLException{
		PreparedStatement ps = null;
		boolean status = true;
		try{

			if ( DataBaseUtil.queryToDynaBean(TEST_RESULT_STATUS_QUERY, reportId) != null ) {
				ps = con.prepareStatement(UPDATE_TEST_RESULT_STATUS_QUERY);
				ps.setString(1, resultStatus);
				ps.setInt(2, reportId);
				status =  ps.executeUpdate()  > 0 ;
			}
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}

		return status;
	}
	/**
	 * Returns list of expression results details of passed prescribed id
	 */
	public final String EXPRESSION_RESULTS_OF_PRESCRIPTION_QUERY =
		" SELECT test_details.resultlabel,report_value,test_details_id,expr_4_calc_result" +
		",original_test_details_id,revised_test_details_id,resultlabel_id,calculated" +
		" FROM test_details" +
		" JOIN tests_prescribed tp USING(prescribed_id)" +
		" JOIN test_results_master USING(resultlabel_id) " +
		" WHERE tp.prescribed_id = ?  AND test_detail_status != 'A' " +
		" AND expr_4_calc_result IS NOT NULL AND expr_4_calc_result != '' ";

	public List<BasicDynaBean > getExpressionResultsForPrescription ( Connection con,int prescribedId )
	throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(EXPRESSION_RESULTS_OF_PRESCRIPTION_QUERY);
			ps.setInt(1, prescribedId);
			return DataBaseUtil.queryToDynaList(ps);
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	private final String  RESULT_AND_VALUES_QUERY =
		" SELECT resultlabel,report_value,test_detail_status,method_id FROM test_details" +
		" WHERE prescribed_id = ?" ;

	public void listResultsndValues(Connection con,int prescribedId,List<String> results,List<String> values)
	throws SQLException{
		PreparedStatement ps = null;
		try{
			ps = con.prepareStatement(RESULT_AND_VALUES_QUERY);
			ps.setInt(1, prescribedId);
			List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(ps);

			for ( BasicDynaBean details : list ) {
				if ( details.get("test_detail_status") != null
						&& !details.get("test_detail_status").equals("A") ) {//amended results are no more valid
					if(null!=details.get("method_id")){
						String methodName = (String)new GenericDAO("diag_methodology_master").findByKey(con,"method_id",
								(Integer)details.get("method_id")).get("method_name");
						String resLabel =  (String)details.get("resultlabel") + "." + methodName;
						results.add(resLabel);
					}else{
						results.add( (String)details.get("resultlabel") );
					}
					values.add( (String)details.get("report_value"));
				}
			}
		}finally{
			DataBaseUtil.closeConnections(null, ps);
		}
	}

	public InputStream getDiagReportPDFStream(BasicDynaBean report, String category)
		throws SQLException, DocumentException, ParseException, IOException, Exception {
		HttpServletRequest request = RequestContext.getHttpRequest();

		String printTemplateType = category.equals("DEP_LAB")?PatientHeaderTemplate.Lab.getType():
		PatientHeaderTemplate.Rad.getType();
		BasicDynaBean printPrefs= null;
		int printerId = 0;
		Integer centerID = DiagReportGenerator.getReportPrintCenter((Integer)report.get("report_id"), null);
		if (category.equals("DEP_LAB"))
			printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId, centerID);
		else
			printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD,printerId, centerID);

		String report_addendum = (String) report.get("report_addendum");
		String reportContent = (report_addendum == null ? "" : report_addendum) +
			DiagReportGenerator.getReport( (Integer)report.get("report_id"),
					(String) request.getSession(false).getAttribute("userid"), request.getContextPath(), false, false);
		String reportName = (String) report.get("report_name");
		String reportMode = (String) report.get("report_mode");
		Integer pheader_template_id = (Integer) report.get("pheader_template_id");
		String patientId = (String) report.get("patient_id");
		Boolean signedOff = ((String) report.get("signed_off")).equals("Y");
		Boolean isDuplicate = ((String) report.get("handed_over")).equals("Y");
		int reportId = (Integer) report.get("report_id");

		if (reportMode.equals("H")) {
			logger.debug("Report Name: "+reportName + " cann't be coverted to PDF. It is a old report.");
			return null;
		} else {

			String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, printTemplateType);
			Template t = new Template("PatientHeader.ftl", new StringReader(patientHeader), AppInit.getFmConfig());
			Map ftlParams = new HashMap();
			ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(patientId, reportId));
			StringWriter writer = new StringWriter();
			try {
				t.process(ftlParams, writer);
			} catch (TemplateException te) {
				logger.debug("Exception raised while processing the patient header for report Id : "+reportId);
				throw te;
			}
			StringBuilder printContent = new StringBuilder();
			printContent.append(writer.toString());
			printContent.append(reportContent);

			HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());

			// increases number of prints taken for this report if it is signed off report
			if(signedOff)
				new LaboratoryDAO().
					increaseNumPrints(reportId);

			ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();

			boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");

			hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
					repeatPatientHeader, true, true, signedOff, isDuplicate, centerID);

			InputStream stream = new ByteArrayInputStream(pdfBytes.toByteArray());
			return stream;
		}
	}

	private static final String IS_AMENDED_TESTS = "SELECT * FROM test_details  " +
	" WHERE patient_id =? AND test_id=? AND prescribed_id=? " +
	" AND  test_detail_status!='A' AND original_test_details_id != 0 ";

	public static boolean isAmendedTest(Connection con, String testId,int presId, String patientId)throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isAmendedTest = false;
		try {
			ps = con.prepareStatement(IS_AMENDED_TESTS);
			ps.setString(1, patientId);
			ps.setString(2, testId);
			ps.setInt(3, presId);
			rs = ps.executeQuery();
			if(rs.next()) isAmendedTest = true;
		}finally {
			if(rs!=null) rs.close();
			DataBaseUtil.closeConnections(null, ps);
		}
		return isAmendedTest;
	}

	public static final String PACKAGE_TEST_REPORTS_LIST =
	"SELECT distinct tvr.report_id,report_addendum  "
	+ " FROM package_prescribed pp "
	+ " JOIN tests_prescribed tp ON(tp.package_ref = pp.prescription_id) "
	+ " JOIN test_visit_reports tvr USING(report_id) "
	+ " WHERE tvr.report_id not in (SELECT report_id FROM tests_prescribed WHERE conducted in ('RAS','RBS')"
	+ " AND tvr.report_id is not null ) "
	+ " AND pp.patient_id=? AND pp.status = 'C' ";


	public static List<BasicDynaBean> getPackageTestReportList( String patientId ) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(PACKAGE_TEST_REPORTS_LIST + " AND signed_off = 'Y' ");
			ps.setString(1, patientId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_PRESC_IDS_FOR_DISCHARGEDREPORT = "SELECT prescribed_id, outsource_dest_prescribed_id " +
			"FROM tests_prescribed WHERE report_id = ? and conducted !='X'";

	public static boolean makeConductedStatusBackToN(Connection con, int dischargedReport)
				throws SQLException, IOException{
		PreparedStatement pstmt = null;
		boolean status = true;
		try {
			pstmt = con.prepareStatement(GET_PRESC_IDS_FOR_DISCHARGEDREPORT);
			pstmt.setInt(1, dischargedReport);
			Integer prescribedID = null;
			List<BasicDynaBean> list = DataBaseUtil.queryToDynaList(pstmt);
			GenericDAO testPrescDAO = new GenericDAO("tests_prescribed");
			Map<String, Object> columndata = new HashMap<String, Object>();
			Map<String, Object> keys = new HashMap<String, Object>();

			columndata.put("report_id", null);
			columndata.put("conducted", "N");
			if (list != null && !list.isEmpty() && list.size() >0) {
				for (BasicDynaBean bean : list) {
					prescribedID = (Integer)bean.get("prescribed_id");
					if (bean.get("outsource_dest_prescribed_id") != null &&
							!bean.get("outsource_dest_prescribed_id").toString().equals("")) {
						keys.put("prescribed_id", prescribedID);
						status &= testPrescDAO.update(con, columndata, keys)>0;
					}
				}
			}

		} finally {
			DataBaseUtil.closeConnections(null, pstmt);
		}
		return status;
	}

	private static final String GET_DIAG_REPORTS = "SELECT report_id, report_name, report_mode, user_name, " +
			" to_char(report_date,'dd-mm-yyyy') as report_date, to_char(report_date, 'hh24:mi') as report_time, " +
			" category, pheader_template_id, patient_id, signed_off,report_addendum, handed_over " +
			" FROM test_visit_reports ";
	public static List getDiagReports(String[] reportIds) throws SQLException{
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			StringBuilder where = new StringBuilder();
			DataBaseUtil.addWhereFieldInList(where, "report_id", Arrays.asList(reportIds));

			ps = con.prepareStatement( GET_DIAG_REPORTS + where + " ORDER BY report_id");
			int i=1;
			for (String reportId : reportIds) {
				ps.setInt(i++, Integer.parseInt(reportId));
			}

			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	public static final String REPORT = "SELECT patient_id FROM test_visit_reports WHERE report_id = ?";

	/*public static final String INCOMING_PATIENT_DETAILS = "SELECT isr.incoming_visit_id, isr.source_center_id, "
			+ "isr.incoming_source_type, tp.pat_id "
			+ " FROM incoming_sample_registration isr"
			+ " JOIN incoming_sample_registration_details isrd ON (isr.incoming_visit_id = isrd.incoming_visit_id)"
			+ " LEFT JOIN tests_prescribed tp ON (isrd.source_test_prescribed = tp.prescribed_id)"
			+ "WHERE isr.incoming_visit_id = ? LIMIT 1";*/
	
	public static final String INCOMING_PATIENT_DETAILS = "SELECT COALESCE(pr.center_id, isr.center_id) AS center_id, "
			+ " isr.incoming_source_type, tp.pat_id"
			+ " FROM test_visit_reports tvr"	
			+ " JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tvr.patient_id)"
			+"  JOIN tests_prescribed tp ON (tp.report_id = tvr.report_id)"
			+ " LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id)"
			+ " WHERE tvr.report_id = ? AND tp.coll_prescribed_id IS NULL LIMIT 1"; 

	private static final String GET_HOSPITAL_PATIENT_ID = "SELECT pat_id "
			+ " FROM tests_prescribed tp "
			+ " WHERE tp.report_id = ? AND tp.coll_prescribed_id IS NULL LIMIT 1";

	public static String getHospitalPatientID(int reportID)throws SQLException {
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_HOSPITAL_PATIENT_ID);
			ps.setInt(1, reportID);
			return DataBaseUtil.getStringValueFromDb(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	private static final String IS_TESTDETAILS_EXIST = "SELECT prescribed_id FROM test_details WHERE prescribed_id = ? ";
	
	public static boolean isTestDetailsExist(Connection con, int prescribedID)throws SQLException {
				
		return DataBaseUtil.queryToDynaBean(IS_TESTDETAILS_EXIST, prescribedID) != null;				
	}
	//new method to get sampledate and testconducted date
		private static String SAMPLE_REG_DATE_DETAILS ="select tc.conducted_date::date,sc.sample_date::date "+
				" from tests_prescribed tp "+
				" LEFT JOIN tests_conducted tc ON (tc.prescribed_id = tp.prescribed_id) " +
				" LEFT JOIN sample_collection sc ON (tp.sample_collection_id = sc.sample_collection_id) "+
				" where tp.report_id = ? LIMIT 1 ";

		public static BasicDynaBean getSampleAndConductionDate(int reportId) throws SQLException{
			Map dateMap = null;
			Connection con = DataBaseUtil.getReadOnlyConnection();
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(SAMPLE_REG_DATE_DETAILS);
				ps.setInt(1, reportId);
				return DataBaseUtil.queryToDynaBean(ps);
			}finally {
				DataBaseUtil.closeConnections(con, ps);
			}
		}
		
		private static String PENDING_TEST_REPORTID ="select tp.report_id  "+
				" from tests_prescribed tp "+
				" JOIN test_visit_reports tvr ON (tvr.report_id = tp.report_id)"+
				" WHERE pat_id = ? AND conducted NOT IN ( 'X','S','U','RBS','RAS' ) LIMIT 1 ";
		
		private static final String GET_PENDING_REPORT_COLLECTION_CENTER =
				" SELECT COALESCE(isr.center_id, pr.center_id) AS center_id " +
		        " FROM tests_prescribed tp " +
		        " LEFT JOIN incoming_sample_registration isr ON (isr.incoming_visit_id = tp.pat_id) " +
		        " LEFT JOIN patient_registration pr ON (pr.patient_id = tp.pat_id) " +
		        " WHERE tp.pat_id = ? AND conducted NOT IN ( 'X','S','U','RBS','RAS' ) AND tp.coll_prescribed_id IS NULL LIMIT 1 ";
		
		private static final String GET_PENDING_NONSIGNEDOFF_REPORT_COLLECTION_CENTER = 
				"select COALESCE(pr.center_id, isr.center_id) as center_id " +
	            " FROM tests_prescribed tp " +
	            " JOIN tests_prescribed tpc ON tpc.prescribed_id = tp.coll_prescribed_id " +
	            " LEFT JOIN patient_registration pr ON pr.patient_id = tpc.pat_id " +
	            " LEFT JOIN incoming_sample_registration isr ON isr.incoming_visit_id = tpc.pat_id " +
	            " WHERE tpc.pat_id = ? AND tpc.conducted NOT IN ( 'X','S','U','RBS','RAS' ) LIMIT 1;";

		public static Integer getPendingTestsCollectioncenterid(String visitId)  throws SQLException{
		Connection con = DataBaseUtil.getReadOnlyConnection();
		PreparedStatement ps = null;
		Integer center_id = -1;
		try {
			ps = con.prepareStatement(GET_PENDING_REPORT_COLLECTION_CENTER);
			ps.setString(1, visitId);
			center_id = DataBaseUtil.getIntValueFromDb(ps);
			if (center_id == null) {
				ps = con.prepareStatement(GET_PENDING_NONSIGNEDOFF_REPORT_COLLECTION_CENTER);
				ps.setString(1, visitId);
				center_id = DataBaseUtil.getIntValueFromDb(ps);				
			}
			return center_id;
		}finally {
			DataBaseUtil.closeConnections(con, ps);
		}
		}
		 private static final String LAB_SEQUENCE_PATTERN =
		    		" SELECT pattern_id " +
		    		" FROM (SELECT min(priority) as priority,  pattern_id FROM hosp_lab_number_seq_prefs " +
		    		" GROUP BY pattern_id ORDER BY priority limit 1) as foo";
		 private static final String RADIOLOGY_SEQUENCE_PATTERN =
		    		" SELECT pattern_id " +
		    		" FROM (SELECT min(priority) as priority,  pattern_id FROM hosp_radiology_number_seq_prefs " +
		    		" GROUP BY pattern_id ORDER BY priority limit 1) as foo";
	    public static String getNextSequenceNo(String type) throws SQLException {
	    	Connection con = null;
			PreparedStatement ps = null;
			
			try{
				con = DataBaseUtil.getConnection();
				if(!StringUtils.isEmpty(type) && type.equals("LABNO")) {
					ps = con.prepareStatement(LAB_SEQUENCE_PATTERN);
				} else if (!StringUtils.isEmpty(type) && type.equals("RADNO")) {
					ps = con.prepareStatement(RADIOLOGY_SEQUENCE_PATTERN);
				}
				
				BasicDynaBean b = DataBaseUtil.queryToDynaBean(ps);
				String patternId = (String) b.get("pattern_id");
	    		return DataBaseUtil.getNextPatternId(patternId);
			} finally {
				DataBaseUtil.closeConnections(con, ps);
			}
	    }

  private static final String GET_DIAG_OPTIMIZED_REPORTS = "SELECT coalesce(ctp.pat_id, tp.pat_id) as patient_id, tvr.report_id, tvr.report_name, "
      + " report_mode, tvr.user_name, "
      + " to_char(report_date,'dd-mm-yyyy') as report_date, to_char(report_date, 'hh24:mi') as report_time, "
      + " category, pheader_template_id, signed_off,report_addendum, handed_over "
      + " FROM test_visit_reports tvr " + " JOIN tests_prescribed tp using (report_id) "
      + " LEFT JOIN tests_prescribed ctp on (ctp.prescribed_id = tp.coll_prescribed_id) ";

  @SuppressWarnings("rawtypes")
  public static List getDiagOptimizedReports(String[] reportIds) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      StringBuilder where = new StringBuilder();
      DataBaseUtil.addWhereFieldInList(where, "tvr.report_id", Arrays.asList(reportIds));

      StringBuilder query = new StringBuilder();
      query.append(GET_DIAG_OPTIMIZED_REPORTS);
      query.append(where);
      query.append(
          " AND (CASE WHEN tvr.signed_off = 'Y' THEN tp.coll_prescribed_id is null ELSE TRUE END) ");
      query.append(" group by tvr.report_id, ctp.pat_id, tp.pat_id "); // HMS-25718
      query.append(" ORDER BY tvr.report_id");
      ps = con.prepareStatement(query.toString());
      int i = 1;
      for (String reportId : reportIds) {
        ps.setInt(i++, Integer.parseInt(reportId));
      }

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
  private static final String GET_TESTS_NOT_COMPLETED = "SELECT CASE WHEN ddept.category"
      + " = 'DEP_LAB' THEN 'Laboratory' ELSE 'Radiology' END AS type,"
      + " d.test_name AS item_name, tp.conducted AS status " 
      + " FROM tests_prescribed tp"
      + " JOIN diagnostics d ON d.test_id = tp.test_id"
      + " JOIN diagnostics_departments ddept ON ddept.ddept_id = d.ddept_id"
      + " WHERE tp.conducted in ('N','P','NRN','MA') AND tp.prescribed_id IN ("
      + "   SELECT bac.activity_id::int FROM bill_activity_charge bac "
      + "   JOIN bill_charge USING (charge_id) "
      + "   WHERE bill_no = ? AND bac.activity_code='DIA')";

  public static List getPendingTestsForBill(Connection con, String billNo) throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_TESTS_NOT_COMPLETED);
      ps.setString(1, billNo);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(null, ps);
    }
  }
}

