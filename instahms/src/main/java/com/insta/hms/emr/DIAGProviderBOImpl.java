package com.insta.hms.emr;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * This provider is for all investigation report documents related to a patient
 * visit.
 *
 */
public class DIAGProviderBOImpl implements EMRInterface {

	PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();

	public List<EMRDoc> listDocumentsByVisit(String visitId)
			throws Exception {

		List<EMRDoc> visitInvestigations = LaboratoryDAO.getConductedTestsListForEMR(visitId, null, false);
		return visitInvestigations;

	}


	public List<EMRDoc> listVisitDocumentsForMrNo(String mrNo) throws Exception {
		return LaboratoryDAO.getConductedTestsListForEMR(null, mrNo, true);
	}


	public List<EMRDoc> listDocumentsByMrno(String mrNo) throws Exception {
		return Collections.emptyList();
	}

	public byte[] getPDFBytes(String docid, int printerId) throws Exception {

		BasicDynaBean prefs =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG, printerId);

		int reportId = Integer.parseInt(docid);
		Integer pheader_template_id = null;
		String printTemplateType = null;
		ArrayList<Hashtable<String,String>> list = new DiagnosticsDAO().getReport(reportId);
		Hashtable ht = (Hashtable) list.get(0);
		String reportContent = (String) ht.get("REPORT_ADDENDUM") + (String) ht.get("REPORT_DATA");
		String templateId = (String) list.get(0).get("PHEADER_TEMPLATE_ID");
		String patientId = (String) ht.get("PATIENT_ID");
		boolean signedOff = ht.get("SIGNED_OFF").equals("Y");
		boolean isDuplicate = ht.get("HANDED_OVER").equals("Y");

		if (templateId != null && !templateId.equals(""))
			pheader_template_id = Integer.parseInt(templateId);
		printTemplateType = list.get(0).get("CATEGORY").equals("DEP_LAB")?PatientHeaderTemplate.Lab.getType():
			PatientHeaderTemplate.Rad.getType();

		String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, printTemplateType);
		Template t = new Template("PatientHeader.ftl", new StringReader(patientHeader), AppInit.getFmConfig());
		Map ftlParams = new HashMap();
		ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(patientId, reportId));
		StringWriter writer = new StringWriter();
		try {
			t.process(ftlParams, writer);
		} catch (TemplateException te) {
			throw te;
		}
		StringBuilder printContent = new StringBuilder();
		printContent.append(writer.toString());
		printContent.append(reportContent);

		HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());

		boolean repeatPHeader = ((String) prefs.get("repeat_patient_info"))
										.equalsIgnoreCase("Y");
		// todo: repeat header = false should probably come from some prefs.
		return hc.getPdfBytes(printContent.toString(), "Investigation Report", prefs, repeatPHeader, true, true, signedOff, isDuplicate);
	}

	private static final String GET_PRESCRIBED_TESTS = " SELECT d.test_name,tp.prescribed_id,tp.mr_no " +
											  		   "  FROM tests_prescribed tp " +
													   "  JOIN diagnostics d ON (tp.test_id = d.test_id) " +
													   "  WHERE tp.report_id = ? ";

	public static List getPrescribedTestsList(int reportId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_PRESCRIBED_TESTS);
			ps.setInt(1, reportId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}

	private static final String GET_TESTS_DOCUMENTS_LIST = " SELECT d.test_name, tp.prescribed_id, tp.mr_no " +
			   "  FROM tests_prescribed tp " +
			   "  JOIN diagnostics d ON (tp.test_id = d.test_id) " +
			   "  JOIN test_documents td ON (tp.prescribed_id = td.prescribed_id) " +
			   "  WHERE td.doc_id = ? ";

	public static List getTestsDocumentList(int docId) throws SQLException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(GET_TESTS_DOCUMENTS_LIST);
			ps.setInt(1, docId);
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	public static List<EMRDoc> listPatientViewDocForMrNo(String patientId) throws SQLException, ParseException  {
		return LaboratoryDAO.getConductedTestsListForEMR(patientId, null, false,true);
	}
	
}


