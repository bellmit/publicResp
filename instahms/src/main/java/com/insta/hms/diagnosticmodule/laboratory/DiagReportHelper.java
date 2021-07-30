/**
 * 
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnostics.diagnosticreportdocket.PendingTestsReportGenerator;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna
 *
 */
public class DiagReportHelper {
	
	static Logger log = LoggerFactory.getLogger(DiagReportHelper.class);
	
	public static final String REPORTS_LIST_FOR_WEB_ACCESS = 
			" SELECT distinct tvr.report_id,report_addendum, " +
			"	report_name, report_mode, tvr.user_name, " +
			" 	to_char(report_date,'dd-mm-yyyy') as report_date, to_char(report_date, 'hh24:mi') as report_time, " +
			" 	category, pheader_template_id, patient_id, signed_off, handed_over " +
			" FROM test_visit_reports tvr " +
			" 	LEFT JOIN tests_prescribed tp ON (tp.report_id = tvr.report_id) "+
			" WHERE tvr.report_id not in (SELECT report_id FROM tests_prescribed WHERE conducted in ('RAS','RBS')" +
			" 	AND report_id is not null AND pat_id=?) " +
			" 	AND tp.pat_id=? AND signed_off = 'Y'";

	public List<BasicDynaBean> getReportList( String patientId ) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(REPORTS_LIST_FOR_WEB_ACCESS);
			ps.setString(1, patientId);
			ps.setString(2, patientId);
			
			return DataBaseUtil.queryToDynaList(ps);
		} finally {
			DataBaseUtil.closeConnections(con, ps);
		}
	}
	
	/**
	 * 
	 * @param stream
	 * @param reportList
	 * @param webbased
	 * @param printerId
	 * @param userId
	 * @param cPath
	 * @param printPendingTests
	 * @param visitId -- used for generating pending tests content
	 * @throws Exception
	 */
	public void generateReport(OutputStream stream, String visitId,  
			String userId, String cPath, String logoHeader) throws Exception {
		
		PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
		PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();
		
		 //string of all signed-off reports of the visit
        List<BasicDynaBean> signedOffReports = getReportList( visitId );
        
		PdfCopyFields copy = new PdfCopyFields(stream);
		boolean added = false;
		FtlReportGenerator ftlGen = null;
		int collCenterID = -1;
		for (BasicDynaBean report : signedOffReports) {
			String reportContent = DiagReportGenerator.getReport( (Integer)report.get("report_id"),
							userId, cPath, false, true);
			Integer centerID = DiagReportGenerator.getReportPrintCenter((Integer)report.get("report_id"), null);
			if(collCenterID == -1) {
			    collCenterID = DiagReportGenerator.getReportPrintCenter((Integer)report.get("report_id"), "col");
			}
			BasicDynaBean printPrefs = PrintConfigurationsDAO.getCenterPageOptions(PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG, centerID);
			
			if (logoHeader != null && !logoHeader.equals("") &&
					(logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
							|| logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
				printPrefs.set("logo_header", logoHeader.toUpperCase());
			}
			
			String reportName = (String) report.get("report_name");
			String reportMode = (String) report.get("report_mode");
			Boolean isDuplicate = ((String) report.get("handed_over")).equals("Y");
			int reportId = (Integer) report.get("report_id");
			
			if (reportMode.equals("H")) {
				log.debug("Report Name: "+reportName + " cann't be coverted to PDF. It is a old report.");
			} else {
				String category = (String) report.get("category");
				String patHeaderTemplateType = category.equals("DEP_LAB") ? PatientHeaderTemplate.APIBASED_LAB.getType() : 
					PatientHeaderTemplate.APIBASED_RAD.getType(); 
				PrintTemplate template = category.equals("DEP_LAB") ? PrintTemplate.APILAB : PrintTemplate.APIRAD;
				
				Integer pheader_template_id = (Integer) printTemplateDao.getPatientHeaderTemplateId(template);
				String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, patHeaderTemplateType);
				ftlGen = new FtlReportGenerator("PatientHeader",new StringReader(patientHeader));
				Map ftlParams = new HashMap();
				ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(visitId, reportId));
				StringWriter writer = new StringWriter();
				try {
				//	t.process(ftlParams, writer);
					ftlGen.setReportParams(ftlParams);
					ftlGen.process(writer);
				} catch (TemplateException te) {
					log.debug("Exception raised while processing the patient header for report Id : "+reportId);
					throw te;
				}
				StringBuilder printContent = new StringBuilder();
				printContent.append(writer.toString());
				reportContent = reportContent.replace(cPath+"/images/strikeoff.png",
						AppInit.getServletContext().getRealPath("/images/strikeoff.png"));
				printContent.append(reportContent);

				HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());

				// increases number of prints taken for this report if it is signed off report
				new LaboratoryDAO().
						increaseNumPrints(reportId);

				ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();

				boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");

				hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
							repeatPatientHeader, true, true, true, isDuplicate, centerID);
				
				PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
				copy.addDocument(reader);
				added = true;
			}
		}
        BasicDynaBean printPrefs =
    			PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG,	0, collCenterID);
		String pendingTests = new PendingTestsReportGenerator().getPendingTestsReport( visitId );
		if (!pendingTests.isEmpty()) {
			ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
			
			String patHeaderTemplateType = PatientHeaderTemplate.APIBASED_LAB.getType(); 
			PrintTemplate template = PrintTemplate.APILAB;
			
			Integer pheader_template_id = (Integer) printTemplateDao.getPatientHeaderTemplateId(template);
			String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, patHeaderTemplateType);
			ftlGen = new FtlReportGenerator("PatientHeader",new StringReader(patientHeader));
			
			Map ftlParams = new HashMap();
			ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(visitId, 0));
			StringWriter writer = new StringWriter();
			try {
			//	t.process(ftlParams, writer);
				ftlGen.setReportParams(ftlParams);
				ftlGen.process(writer);
			} catch (TemplateException te) {
				log.debug("Exception raised while processing the patient header for pending tests patient header ");
				throw te;
			}
			StringBuilder printContent = new StringBuilder();
			printContent.append(writer.toString());
			pendingTests = pendingTests.replace(cPath+"/images/strikeoff.png",
					AppInit.getServletContext().getRealPath("/images/strikeoff.png"));
			printContent.append(pendingTests);
			
			boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
			HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());
			hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
					repeatPatientHeader, true, true, false, false, collCenterID);

			PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
			copy.addDocument(reader);
			added = true;
		}
		
		if (!added) {
			// if there are no reports found then show the below message.
			// this will happen only when user selects the visit and tries take print of all the reports from
			// pending tests screen.
			String reportContent = "Report is Not Available";
			ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();

			boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
			HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());
			hc.writePdf(pdfBytes, reportContent, "Investigation Report", printPrefs, false,
					repeatPatientHeader, true, true, false, false, collCenterID);

			PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
			copy.addDocument(reader);
			added = true;

		}

		if (added)
			copy.close();

	}

}
