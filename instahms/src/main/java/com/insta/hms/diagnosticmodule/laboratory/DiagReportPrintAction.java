/**
 *
 */
package com.insta.hms.diagnosticmodule.laboratory;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.MimeTypeDetector;
import com.bob.hms.common.RequestContext;
import com.insta.instaapi.common.ServletContextUtil;
import com.insta.instaapi.common.JsonProcessor;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagReportGenerator;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.diagnosticmodule.common.SampleCollectionDAO;
import com.insta.hms.diagnosticsmasters.outhousemaster.OutHouseMasterDAO;
import com.insta.hms.imageretriever.DiagImageRetriever;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;

import flexjson.JSONSerializer;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author krishna
 *
 */
public class DiagReportPrintAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(DiagReportPrintAction.class);
	static PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
  private static final JSONSerializer js = JsonProcessor.getJSONParser();

	public ActionForward generateSampleCollectionReport(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws JRException, IOException, SQLException, ParseException, Exception{

			String templateContent = null;
		//	Template t = null;
			FtlReportGenerator ftlGen = null;
			String mrNo = null;
			String patientId = null;

			HashMap map = new HashMap();
			map.put("visitId", request.getParameter("visitid"));
			map.put("sampleNo",request.getParameter("sampleNo"));
			map.put("sampleType", request.getParameter("sampleTypes"));
			String sampleNos = request.getParameter("sampleNo");
			String visitId = request.getParameter("visitid");
			String template_name = request.getParameter("template_name");

			List<BasicDynaBean>  sampleDetails= new ArrayList<BasicDynaBean>();
			sampleDetails = new SampleCollectionDAO().getSampleCollectionPaperPrintDetails(visitId,sampleNos);
			if (sampleDetails.size() > 0) {
				mrNo = (String)((BasicDynaBean)sampleDetails.get(0)).get("mr_no");
				patientId = (String)((BasicDynaBean)sampleDetails.get(0)).get("patient_id");

				if (mrNo == null)
					map.put("patient", PatientDetailsDAO.getIncomingPatientDetails(patientId));
				else
					map.put("patient", com.insta.hms.Registration.
										PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo));
			}
			map.put("sampleDetails", sampleDetails);

			Configuration cfg = AppInit.getFmConfig();
			OutputStream os = null;
			HtmlConverter hc = new HtmlConverter();
			int printerId = 0;
			String printerIdStr = request.getParameter("printType");
			if ((printerIdStr != null) && !printerIdStr.equals("")) {
				printerId = Integer.parseInt(printerIdStr);
			}
			BasicDynaBean printPrefs = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_SAMPLE_COLLECTION, printerId);

			String printMode = "P";
			if (printPrefs.get("print_mode") != null) {
				printMode = (String) printPrefs.get("print_mode");
			}


			StringWriter writer = new StringWriter();
			PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
			PrintTemplate temp = PrintTemplate.Sample;
			templateContent = printtemplatedao.getCustomizedTemplate(temp);
			if (templateContent == null || templateContent.equals("")) {
					//t = AppInit.getFmConfig().getTemplate("SampleCollectionPaperPrint.ftl");
			//	t = cfg.getTemplate(PrintTemplate.Sample.getFtlName() + ".ftl");
				ftlGen = new FtlReportGenerator(PrintTemplate.Sample.getFtlName());
			} else {
				StringReader reader = new StringReader(templateContent);
			//	t = new Template("SampleCollectionPaperPrint.ftl", reader, AppInit.getFmConfig());
				ftlGen = new FtlReportGenerator("SampleCollectionPaperPrint",reader);
			}

		//	t.process(map, writer);
			ftlGen.setReportParams(map);
			ftlGen.process(writer);
			if (printMode.equals("P")) {
				os = response.getOutputStream();
				response.setContentType("application/pdf");
				boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
				hc.writePdf(os, writer.toString(), "SampleCollectionPaperPrint", printPrefs, false,repeatPatientHeader,true,
						true, true, false);
				os.close();
				return null;
			} else {
				String textReport = new String(hc.getText(writer.toString(),"SampleCollectionPaperPrint", printPrefs, true, true));
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", printPrefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}
	}

	public ActionForward generateSampleCollectionOutHouseReport(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response) throws Exception{
			HashMap map = new HashMap();
			String prescribedIds="";
			String sampleno="";
			String outSourceDestId=request.getParameter("outSourceName");
			String visitId= request.getParameter("visitid");
			String format=request.getParameter("format");
			String prescribedId=request.getParameter("prescribedId");
			String sampleNo=request.getParameter("sampleNo");

			if (format== null) {
				String splitprescibedIds[]=prescribedId.split(",");
				for (int i=0;i<splitprescibedIds.length;i++) {
					prescribedIds = prescribedIds+"'"+splitprescibedIds[i].trim()+"'"+',';
				}
				prescribedIds = prescribedIds.substring(0, prescribedIds.length()-1);
				prescribedId = prescribedIds;
			}
			BasicDynaBean pref= null;
			int printerId =0;
			String  printerIdStr = request.getParameter("printerId");

			if ((printerIdStr != null) &&  !printerIdStr.equals("")) {
				printerId = Integer.parseInt(printerIdStr);
			}

			pref =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId);

			// out house wise template selection
			if(sampleNo != null && !sampleNo.isEmpty() && sampleNo.endsWith("'") && sampleNo.startsWith("'")) {
			  sampleNo = sampleNo.substring(1, sampleNo.length() - 1);
			}
			Map outHouseSamplePatientDetails=LaboratoryDAO.getOutHouseSamplePatientDetailsDetails(visitId,prescribedId,outSourceDestId,sampleNo);
			List<BasicDynaBean> outHouseSampleDetails=LaboratoryDAO.getOutSourceSampleDetails(visitId,prescribedId,outSourceDestId,sampleNo);
			HashMap testResultLabelsNames=LaboratoryDAO.getTestResultLables(outHouseSampleDetails,outSourceDestId);
			BasicDynaBean outSourcebean = new GenericDAO("diag_outsource_master").findByKey("outsource_dest_id",
					Integer.parseInt(outSourceDestId));
			String outSourceDest  = (String)outSourcebean.get("outsource_dest");
			String outSourceDestType = (String)outSourcebean.get("outsource_dest_type");

			BasicDynaBean outHouse = new OutHouseMasterDAO().findByKey("oh_id", outSourceDest);
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			String templateName = (outSourceDestType.equals("O") || outSourceDestType.equals("IO"))?(String) outHouse.get("template_name"):null;
			String templateMode = null;
			String templateContent = null;
		//	Template t = null;
			FtlReportGenerator ftlGen = null;
			map.put("details", outHouseSamplePatientDetails);
			map.put("sampleDetails", outHouseSampleDetails);
			map.put("labels", testResultLabelsNames);
			map.put("visitId", request.getParameter("visitid"));
			map.put("sampleNo",request.getParameter("sampleNo"));
			map.put("hospName",dto.getHospitalName());
			map.put("format","pdf");
			if (templateName == null || templateName.equals("") || templateName.equals("BUILTIN_HTML")) {
			//	t = AppInit.getFmConfig().getTemplate("DiagOutHouseSamplePrint.ftl");
				ftlGen = new FtlReportGenerator("DiagOutHouseSamplePrint");
				templateMode = "H";
			} else if (templateName.equals("BUILTIN_TEXT")) {
			//	t = AppInit.getFmConfig().getTemplate("DiagOutHouseSampleTextPrint.ftl");
				ftlGen = new FtlReportGenerator("DiagOutHouseSampleTextPrint");
				templateMode = "T";
			} else {
				String templateCodeQuery =
					"SELECT diag_outhouse_template_content, template_mode FROM diagnostic_outhouse_print_template " +
					" WHERE template_name=?";
				List printTemplateList  = DataBaseUtil.queryToDynaList(templateCodeQuery,templateName);
				for (Object obj: printTemplateList){
					BasicDynaBean templateBean = (BasicDynaBean) obj;
					templateContent = (String)templateBean.get("diag_outhouse_template_content");
					templateMode = (String) templateBean.get("template_mode");
				}
				StringReader reader = new StringReader(templateContent);
			//	t = new Template("Custom Template", reader, AppInit.getFmConfig());
				ftlGen = new FtlReportGenerator("Custom Template",reader);
			}
			StringWriter writer = new StringWriter();
		//	t.process(map,writer);
			ftlGen.setReportParams(map);
			ftlGen.process(writer);
			String printContent = writer.toString();

			HtmlConverter hc = new HtmlConverter();
			if (pref.get("print_mode").equals("P")) {
				OutputStream os = response.getOutputStream();
				response.setContentType("application/pdf");
				try {
					if (templateMode != null && templateMode.equals("T")){
						hc.textToPDF(printContent, os, pref);
					}else{
						hc.writePdf(os, printContent, "OutHouse Samples", pref, false, false, true, true, true, false);
					}
				} catch (Exception e) {
					response.reset();
					log.error("Original Template:");
					log.error(templateContent);
					log.error("Generated HTML content:");
					log.error(printContent);
					throw(e);
				}
				os.close();

				return null;
			} else {
				String textReport = null;
				//text mode
				if (templateMode.equals("T")){
					textReport = printContent;
				}else{
					textReport = new String(hc.getText(printContent, "OutHouse Samples", pref, true, true));
				}
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", pref.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}

	}

	public ActionForward printReport(ActionMapping mapping, ActionForm form,HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, TemplateException,
			Exception {
		
		String reqHandlerKey = request.getParameter("request_handler_key");
		String error = APIUtility.setConnectionDetails(servlet.getServletContext(), 
				reqHandlerKey);
		if (error != null) {
			APIUtility.setInvalidLoginError(response, error);
			return null;
		}
    ServletContext ctx = servlet.getServletContext();
    Map<String, Object> sessionMap = ServletContextUtil.getContextParametersMap(ctx);
    Map<String,Object> sessionParameters = null;
    if (sessionMap != null && !sessionMap.isEmpty()) {
      sessionParameters = (Map<String,Object>) sessionMap.get(reqHandlerKey);
    }

		String userName = RequestContext.getUserName();
		String reportId = request.getParameter("reportId");
		int reportIntId = Integer.parseInt(reportId);
		String reportContent = null;
		String patientView = request.getParameter("patient_view");
		boolean isPatientView = (patientView!=null && patientView.equals("true"));
		String reportMode = null;
		Integer pheader_template_id = null;
		String printTemplateType = null;
		String patientId = null;
		String category=null;
		boolean signedOff = false;
		boolean isDuplicate = false;
		FtlReportGenerator ftlGen = null;
		Map<String, Object> data_map = new HashMap<String, Object>();
		
		BasicDynaBean visitReportBean  = DiagnosticsDAO.getReportDynaBean( reportIntId );
		reportMode = (String)visitReportBean.get("report_mode");
		pheader_template_id = (Integer)visitReportBean.get("pheader_template_id");
		printTemplateType = visitReportBean.get("category").equals("DEP_LAB")?PatientHeaderTemplate.Lab.getType():
			PatientHeaderTemplate.Rad.getType();
		patientId = (String)visitReportBean.get("patient_id");
		category= (String)visitReportBean.get("category");
		signedOff = visitReportBean.get("signed_off").equals("Y");
		isDuplicate = visitReportBean.get("handed_over").equals("Y");
		Integer loggedINcenterID = RequestContext.getCenterId();
		
		//Do we need to show patient collection center details always ?
		
		if (GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1
				&& visitReportBean != null && visitReportBean.get("signoff_center") != null) {
			if (visitReportBean.get("signoff_center").equals(loggedINcenterID)) {
				//No need to change the patientId
			} else {
				patientId = DiagnosticsDAO.getHospitalPatientID(reportIntId);			
			}
		}

		Map visit_details_map = DiagReportGenerator.getPatientDetailsMap(patientId, reportIntId);

    boolean isPatientLogin = sessionParameters != null && ((boolean) sessionParameters.get("patient_login"));
    if (isPatientLogin) {
      String mrNo = (String) sessionParameters.get("customer_user_id");
	    if (!((String)visit_details_map.get("mrNo")).equals(mrNo)) {
	      String successMsg = "Invalid input parameters supplied for reportId";
		    Map<String, Object> errorMap = new HashMap<>();
	      errorMap.put("return_code", "1021");
	      errorMap.put("return_message", successMsg);
	      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	      response.getWriter().write(js.deepSerialize(errorMap));
	      response.flushBuffer();
	      return null;
	    }
    }

		data_map.put("visit_details", visit_details_map);
		data_map.put("visit_report_bean", visitReportBean);
		data_map.put("hospital_patient_id", patientId);
		
		//addendum, provided as a token refer #BUG:41602
		if(reqHandlerKey != null && !reqHandlerKey.equals("")) {
			if(isPatientView){
				reportContent = DiagReportGenerator.getReport(reportIntId,
						userName, request.getContextPath(), true, true, data_map );
			}else{
				reportContent = DiagReportGenerator.getReport(reportIntId,
						userName, request.getContextPath(), false, true, data_map );
			}
		} else {
			reportContent = DiagReportGenerator.getReport(reportIntId,
				userName, request.getContextPath(), false, false, data_map);//report generated
		}
		
		Integer centerID = DiagReportGenerator.getReportPrintCenter(reportIntId, null);
		
		if (reportContent == null) {
			reportContent = "Report is Not Availble";
		}
		BasicDynaBean printPrefs= null;
		request.setAttribute("reportName", visitReportBean.get("report_name"));
		request.setAttribute("report",reportContent);

		int printerId = 0;
		String printerIdStr = request.getParameter("printerId");
		if ( (printerIdStr != null) && !printerIdStr.equals("") ) {
			printerId = Integer.parseInt(printerIdStr);
		}
		if(category.equals("DEP_LAB")) {
			if(reqHandlerKey != null && !reqHandlerKey.equals("")) {
			    printPrefs = PrintConfigurationsDAO.getCenterPageOptions(PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG, centerID);
			} else {
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId, centerID);
			}
		}
		else {
			if(reqHandlerKey != null && !reqHandlerKey.equals("")) {
			    printPrefs = PrintConfigurationsDAO.getCenterPageOptions(PrintConfigurationsDAO.PRINT_TYPE_WEB_DIAG, centerID);
			} else {
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD,printerId, centerID);
			}
		}

		String requestedMode;
		String forcePdf = request.getParameter("forcePdf");
		if ( (forcePdf != null) && forcePdf.equals("true")) {
			// force to PDF even if the print prefs say not PDF.
			requestedMode = "P";
		} else {
			requestedMode = (String)printPrefs.get("print_mode");
		}
		
		// api parameter
		String logoHeader = request.getParameter("logoHeader");
		if (logoHeader != null && !logoHeader.equals("") &&
				(logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
						|| logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
			printPrefs.set("logo_header", logoHeader.toUpperCase());
		}

		if (reportMode.equals("H") && requestedMode.equals("P")) {
			// It is an old report, it cannot be printed in PDF. Fallback to HTML print.
			request.setAttribute("prefs",printPrefs);
			request.setAttribute("reportId", reportId);
			return mapping.findForward("printReport");

		} else {
			PrintTemplatesDAO printTemplateDao = new PrintTemplatesDAO();
			if(reqHandlerKey != null && !reqHandlerKey.equals("")) {
				PrintTemplate template = null;
				if(isPatientView){
					printTemplateType = PatientHeaderTemplate.WebBased.getType();
					template = category.equals("DEP_LAB") ? PrintTemplate.WebLab : PrintTemplate.WebRad;
				}else{
					printTemplateType = category.equals("DEP_LAB") ? PatientHeaderTemplate.APIBASED_LAB.getType() : 
						PatientHeaderTemplate.APIBASED_RAD.getType(); 
					template = category.equals("DEP_LAB") ? PrintTemplate.APILAB : PrintTemplate.APIRAD;
				}
				pheader_template_id = printTemplateDao.getPatientHeaderTemplateId(template);
			}
			String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, printTemplateType);
		//	Template t = new Template("PatientHeader.ftl", new StringReader(patientHeader), AppInit.getFmConfig());
			ftlGen = new FtlReportGenerator("PatientHeader",new StringReader(patientHeader));
			Map ftlParams = new HashMap();
			ftlParams.put("visitDetails", visit_details_map);
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
			reportContent = reportContent.replace(request.getContextPath()+"/images/strikeoff.png",
					AppInit.getServletContext().getRealPath("/images/strikeoff.png"));//for background image of amended report
			printContent.append(reportContent);

			HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());

		//	increases number of prints taken for this report if it is signed off report
			if(signedOff)
				new LaboratoryDAO().
					increaseNumPrints(Integer.parseInt(reportId));

			if (requestedMode.equals("P")) {
				OutputStream os = response.getOutputStream();
				response.setContentType("application/pdf");

				boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");

				hc.writePdf(os, printContent.toString(), "Investigation Report", printPrefs, false,
						repeatPatientHeader, true, true, signedOff,isDuplicate, centerID);
				os.close();
				return null;
			} else {
				// text mode
				String textReport = new String(hc.getText(printContent.toString(), "Investigation Report",
						printPrefs, true, true, centerID));
				request.setAttribute("textReport", textReport);
				request.setAttribute("textColumns", printPrefs.get("text_mode_column"));
				request.setAttribute("printerType", "DMP");
				return mapping.findForward("textPrintApplet");
			}
		}
	}

	public ActionForward printSelectedReports(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception {

		String using = request.getParameter("using");
		List<BasicDynaBean> reportList = null;
		String category = request.getParameter("category");
		FtlReportGenerator ftlGen = null;
		Map<String, Map> visitDetailsMap = new HashMap<String, Map>();
		Map<String, Object> data_map = new HashMap<String, Object>();
		String visitId = request.getParameter("visitid");
		if (using.equals("reportId")) {
			String[] reportIds = request.getParameterValues("reportId");
			reportList = DiagnosticsDAO.getReports(reportIds);
		} else {
			reportList = DiagnosticsDAO.getReports(visitId, category);
		}
		
		String printTemplateType = category.equals("DEP_LAB")?PatientHeaderTemplate.Lab.getType():
			PatientHeaderTemplate.Rad.getType();
		BasicDynaBean printPrefs= null;
		int printerId = 0;
		String printerIdStr = request.getParameter("printerId");
		if ( (printerIdStr != null) && !printerIdStr.equals("") ) {
			printerId = Integer.parseInt(printerIdStr);
		}

		response.setContentType("application/pdf");
		OutputStream stream = response.getOutputStream();
		PdfCopyFields copy = new PdfCopyFields(stream);
		boolean added = false;

		if (reportList.isEmpty()) {
			// if there are no reports found then show the below message.
			// this will happen only when user selects the visit and tries take print of all the reports from
			// pending tests screen.
			int centerID = -1;
			List<String> visitColumnList= new ArrayList<String>();
			Map<String, Object> visitKeyColumn = new HashMap<String, Object>();
			visitKeyColumn.put("patient_id", visitId);
			visitColumnList.add("center_id");
			BasicDynaBean visitDetails = new GenericDAO("patient_registration").findByKey(visitColumnList, visitKeyColumn);
			if(visitDetails != null) {
				centerID = (Integer)visitDetails.get("center_id");
			} else {
				Map<String, Object> identifiers = new HashMap<String, Object>();
				identifiers.put("incoming_visit_id", visitId);
				BasicDynaBean bean = new GenericDAO("incoming_sample_registration").findByKey(identifiers);
				if(bean != null) {
					centerID = (Integer)bean.get("center_id");
				} else {
					centerID = 0;
				}
			}
			String reportContent = "Report is Not Available";
			ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
			if (category.equals("DEP_LAB"))
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId, centerID);
			else
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD,printerId, centerID);
			boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");
			HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());
			hc.writePdf(pdfBytes, reportContent, "Investigation Report", printPrefs, false,
					repeatPatientHeader, true, true, false, false, centerID);

			PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
			copy.addDocument(reader);
			added = true;

		}

		for (BasicDynaBean report : reportList) {
			
			String reportName = (String) report.get("report_name");
			String reportMode = (String) report.get("report_mode");
			Integer pheader_template_id = (Integer) report.get("pheader_template_id");
			String patientId = (String) report.get("patient_id");
			Boolean signedOff = ((String) report.get("signed_off")).equals("Y");
			Boolean isDuplicate = ((String) report.get("handed_over")).equals("Y");
			int reportId = (Integer) report.get("report_id");
			
			if (visitDetailsMap.get(patientId) == null) 
				visitDetailsMap.put(patientId, DiagReportGenerator.getPatientDetailsMap(patientId, reportId));
			data_map.put("visit_details", visitDetailsMap.get(patientId));
			
			String reportContent = DiagReportGenerator.getReport( (Integer)report.get("report_id"),
							(String) request.getSession(false).getAttribute("userid"), request.getContextPath(),false, false, data_map);
			Integer centerID = DiagReportGenerator.getReportPrintCenter((Integer)report.get("report_id"), null);
			if (category.equals("DEP_LAB"))
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId, centerID);
			else
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD,printerId, centerID);
			
			if (reportMode.equals("H")) {
				log.debug("Report Name: "+reportName + " cann't be coverted to PDF. It is a old report.");
			} else {

				String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, printTemplateType);
			//	Template t = new Template("PatientHeader.ftl", new StringReader(patientHeader), AppInit.getFmConfig());
				ftlGen = new FtlReportGenerator("PatientHeader",new StringReader(patientHeader));
				Map ftlParams = new HashMap();
				ftlParams.put("visitDetails", visitDetailsMap.get(patientId));
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
				reportContent = reportContent.replace(request.getContextPath()+"/images/strikeoff.png",
						AppInit.getServletContext().getRealPath("/images/strikeoff.png"));
				printContent.append(reportContent);

				HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());

				// increases number of prints taken for this report if it is signed off report
				if(signedOff)
					new LaboratoryDAO().
						increaseNumPrints(reportId);

				ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();

				boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");

				/* When signedOff&Print button is clicked from lab/rad dashboard then passing signedoff as true..*/
				String signedOffandPrint = (String)request.getParameter("signedOffandPrint");
				if(null!=signedOffandPrint && !signedOffandPrint.equals("") && signedOffandPrint.equals("Y")) {
					hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
							repeatPatientHeader, true, true, true, isDuplicate, centerID);
				}else{
					hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
							repeatPatientHeader, true, true, signedOff, isDuplicate, centerID);
				}

				PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
				copy.addDocument(reader);
				added = true;
			}
		}
		if (added)
			copy.close();

		stream.flush();
		stream.close();
		return null;
	}


	@IgnoreConfidentialFilters
	public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {
		String userName = request.getParameter("user_name");
		String doctorId = request.getParameter("doctor_id");

		String value = null;
		String colName = null;
		if (userName != null && !userName.equals("")) {
			colName = "emp_username";
			value = userName;
		}
		if (doctorId != null && !doctorId.equals("")) {
			colName = "doctor_id";
			value = doctorId;
		}
		GenericDAO	dao = new GenericDAO("user_images");
		BasicDynaBean bean = dao.getBean();
		dao.loadByteaRecords(bean, colName, value);
		InputStream is = (InputStream) bean.get("signature");

		byte[] b = DataBaseUtil.readInputStream(is);
		response.setContentType(MimeTypeDetector.getMimeTypes(is).toString());
		OutputStream os = response.getOutputStream();
		os.write(b);
		os.flush();
		os.close();

		return null;
	}

	public ActionForward printDiagStatusReports(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException, Exception {

		String using = request.getParameter("using");
		List<BasicDynaBean> reportList = null;
		FtlReportGenerator ftlGen = null;

		if (using.equals("reportId")) {
			String[] reportIds = request.getParameterValues("reportId");
			reportList = DiagnosticsDAO.getDiagReports(reportIds);
		}

		BasicDynaBean printPrefs= null;
		int printerId = 0;
		String printerIdStr = request.getParameter("printerId");
		if ( (printerIdStr != null) && !printerIdStr.equals("") ) {
			printerId = Integer.parseInt(printerIdStr);
		}

		response.setContentType("application/pdf");
		OutputStream stream = response.getOutputStream();
		PdfCopyFields copy = new PdfCopyFields(stream);
		boolean added = false;

		for (BasicDynaBean report : reportList) {
			String reportContent = DiagReportGenerator.getReport( (Integer)report.get("report_id"),
							(String) request.getSession(false).getAttribute("userid"), request.getContextPath(),false, false);
			Integer centerID = DiagReportGenerator.getReportPrintCenter((Integer)report.get("report_id"), null);
			String reportName = (String) report.get("report_name");
			String reportMode = (String) report.get("report_mode");
			Integer pheader_template_id = (Integer) report.get("pheader_template_id");
			String patientId = (String) report.get("patient_id");
			Boolean signedOff = ((String) report.get("signed_off")).equals("Y");
			Boolean isDuplicate = ((String) report.get("handed_over")).equals("Y");
			int reportId = (Integer) report.get("report_id");
			String category = (String) report.get("category");

			String printTemplateType = category.equals("DEP_LAB")?PatientHeaderTemplate.Lab.getType():
				PatientHeaderTemplate.Rad.getType();

			if (category.equals("DEP_LAB"))
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,printerId, centerID);
			else
				printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD,printerId, centerID);

			if (reportMode.equals("H")) {
				log.debug("Report Name: "+reportName + " cann't be coverted to PDF. It is a old report.");
			} else {

				String patientHeader = phTemplateDAO.getPatientHeader(pheader_template_id, printTemplateType);
			//	Template t = new Template("PatientHeader.ftl", new StringReader(patientHeader), AppInit.getFmConfig());
				ftlGen = new FtlReportGenerator("PatientHeader",new StringReader(patientHeader));
				Map ftlParams = new HashMap();
				ftlParams.put("visitDetails", DiagReportGenerator.getPatientDetailsMap(patientId, reportId));
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
				reportContent = reportContent.replace(request.getContextPath()+"/images/strikeoff.png",
						AppInit.getServletContext().getRealPath("/images/strikeoff.png"));
				printContent.append(reportContent);

				HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());

				// increases number of prints taken for this report if it is signed off report
				if(signedOff)
					new LaboratoryDAO().
						increaseNumPrints(reportId);

				ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();

				boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info")).equalsIgnoreCase("Y");

				String signedOffandPrint = (String)request.getParameter("signedOffandPrint");
				if(null!=signedOffandPrint && !signedOffandPrint.equals("") && signedOffandPrint.equals("Y")) {
					hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
							repeatPatientHeader, true, true, true, isDuplicate, centerID);
				}else{
					hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
							repeatPatientHeader, true, true, signedOff, isDuplicate, centerID);
				}

				PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
				copy.addDocument(reader);
				added = true;
			}
		}
		if (added)
			copy.close();

		stream.flush();
		stream.close();
		return null;
	}

  public ActionForward printOptimizedDiagReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    response.setContentType("application/pdf");
    OutputStream stream = response.getOutputStream();
    PdfCopyFields copy = new PdfCopyFields(stream);

    String using = request.getParameter("using");

    String[] reportIds = null;
    String visitId = null;
    String category = null;
    if (using.equals("reportId")) {
      reportIds = request.getParameterValues("reportId");
    } else {
      category = request.getParameter("category");
      visitId = request.getParameter("visitid");
    }

    Map<String, List<BasicDynaBean>> reportListBeans = prepareReportListGroup(reportIds, visitId,
        category);
    prepareReader(copy, reportListBeans, (String) request.getSession(false).getAttribute("userid"),
        request.getContextPath(), request.getParameter("printerId"),
        request.getParameter("signedOffandPrint"));
    copy.close();
    stream.flush();
    stream.close();
    return null;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private void prepareReader(PdfCopyFields copy, Map<String, List<BasicDynaBean>> reportListBeans,
      String userId, String contextPath, String printerIdStr, String signedOffandPrint)
      throws Exception {

    Set<String> keys = reportListBeans.keySet();
    Iterator it = keys.iterator();
    Map visitDetailsMap = new HashMap<>();
    while (it.hasNext()) {
      String key = (String) it.next();
      String[] keyArr = key.split("/");
      String patientId = keyArr[0];
      String center = keyArr[1];
      int centerId = Integer.parseInt(center);
      String category = keyArr[2];
      Boolean status = new Boolean(keyArr[3]);

      Map dataMap = new HashMap<>();

      List<BasicDynaBean> reportList = reportListBeans.get(key);
      BasicDynaBean printPrefs = null;
      int printerId = 0;
      if ((printerIdStr != null) && !printerIdStr.equals("")) {
        printerId = Integer.parseInt(printerIdStr);
      }

      if (category.equals("DEP_LAB"))
        printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG,
            printerId, centerId);
      else
        printPrefs = PrintConfigurationsDAO
            .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG_RAD, printerId, centerId);

      boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info"))
          .equalsIgnoreCase("Y");

      StringBuilder printContent = new StringBuilder();
      Boolean headerAdded = false;
      for (BasicDynaBean report : reportList) {
        String reportName = (String) report.get("report_name");
        String reportMode = (String) report.get("report_mode");
        Integer pheaderTemplateId = (Integer) report.get("pheader_template_id");
        String deptName = (String) report.get("category");
        int reportId = (Integer) report.get("report_id");
        if (visitDetailsMap.get(patientId) == null) {
          visitDetailsMap.put(patientId,
              DiagReportGenerator.getPatientDetailsMap(patientId, reportId));
          dataMap.put("visit_details", visitDetailsMap.get(patientId));
        }

        String reportContent = DiagReportGenerator.getReport((Integer) report.get("report_id"),
            userId, contextPath, false, false, dataMap);

        if (reportMode.equals("H")) {
          log.debug(
              "Report Name: " + reportName + " cann't be coverted to PDF. It is a old report.");
        } else {

          if (!headerAdded) {
            StringWriter writer = writePatientHeader((Map) visitDetailsMap.get(patientId), category,
                pheaderTemplateId);
            printContent.append(writer.toString());
            headerAdded = true;
          }
           reportContent = reportContent.replace(contextPath + "/images/strikeoff.png",
           AppInit.getServletContext().getRealPath("/images/strikeoff.png"));
           // No Optimization changes in any radiology reports
           if(! "DEP_RAD".equals(deptName)) {
             reportContent = reportContent.replaceAll("patientHeader", "patientTestHeader"); 
           }
          printContent.append(reportContent);
          // increases number of prints taken for this report if it is signed off report
          if (status)
            new LaboratoryDAO().increaseNumPrints(reportId);
        }
      }

      ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();

      HtmlConverter hc = new HtmlConverter(new DiagImageRetriever());
      /*
       * When signedOff&Print button is clicked from lab/rad dashboard then passing signedoff as
       * true..
       */
      if (null != signedOffandPrint && !signedOffandPrint.equals("")
          && signedOffandPrint.equals("Y")) {
        hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
            repeatPatientHeader, true, true, true, false, centerId);
      } else {
        hc.writePdf(pdfBytes, printContent.toString(), "Investigation Report", printPrefs, false,
            repeatPatientHeader, true, true, status, false, centerId);
      }

      PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes.toByteArray()));
      copy.addDocument(reader);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, List<BasicDynaBean>> prepareReportListGroup(String[] reportIds,
      String visitId, String category) throws SQLException {
    List<BasicDynaBean> reportList = null;
    if (reportIds != null) {
      reportList = DiagnosticsDAO.getDiagOptimizedReports(reportIds);
    } else {
      reportList = DiagnosticsDAO.getReports(visitId, category);
    }
    Map<String, List<BasicDynaBean>> reportListBean = new LinkedHashMap<>();
    for (BasicDynaBean bean : reportList) {
      String patientId = (String) bean.get("patient_id");
      Integer reportId = (Integer) bean.get("report_id");
      String dept = (String) bean.get("category");
      Boolean status = ((String) bean.get("signed_off")).equals("Y");
      Integer centerId = DiagReportGenerator.getReportPrintCenter(reportId, null);
      
      String mapKeys = patientId + "/" + centerId + "/" + dept + "/" + status;
      if("DEP_RAD".equals(dept)) {
        // No Optimization changes in any radiology reports
        List<BasicDynaBean> list = new ArrayList<>();
        list.add(bean);
        reportListBean.put(mapKeys+"/"+reportId, list);
      } else if (reportListBean.get(mapKeys) != null) {
        List<BasicDynaBean> list =  reportListBean.get(mapKeys);
        list.add(bean);
      } else {
        List<BasicDynaBean> list = new ArrayList<>();
        list.add(bean);
        reportListBean.put(mapKeys, list);
      }
    }
    return reportListBean;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private StringWriter writePatientHeader(Map visitDetails, String category,
      Integer pheaderTemplateId) throws SQLException, IOException, TemplateException {

    String printTemplateType = category.equals("DEP_LAB") ? PatientHeaderTemplate.Lab.getType()
        : PatientHeaderTemplate.Rad.getType();
    String patientHeader = phTemplateDAO.getPatientHeader(pheaderTemplateId, printTemplateType);
    FtlReportGenerator ftlGen = null;
    ftlGen = new FtlReportGenerator("PatientHeader", new StringReader(patientHeader));
    Map ftlParams = new HashMap();
    ftlParams.put("visitDetails", visitDetails);
    StringWriter writer = new StringWriter();
    try {
      ftlGen.setReportParams(ftlParams);
      ftlGen.process(writer);
    } catch (TemplateException te) {
      log.debug(
          "Exception raised while processing the patient header for report Id : " + visitDetails);
      throw te;
    }
    return writer;
  }
}
