package com.insta.hms.OTServices.OtRecord;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.OTServices.OperationDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.imageretriever.DoctorConsultImageRetriever;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.OTForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.outpatient.PhysicianFormValuesDAO;
import com.insta.hms.outpatient.SecondaryComplaintDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
/**
 * @author nikunj.s
 *
 */
public class OtRecordPrintAction extends DispatchAction {

	SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();
	OtRecordDAO otDAO = new OtRecordDAO();

	public ActionForward print(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, ServletException, IOException, ParseException,
			DocumentException, XPathExpressionException, TransformerException, TemplateException, Exception {
		String mrNo = request.getParameter("mr_no");
		String patientId = request.getParameter("patient_id");
		String printerIdStr = request.getParameter("printerId");
		Integer opDetailsId = Integer.parseInt(request.getParameter("operation_details_id"));
		BasicDynaBean prefs = null;
		int printerId = 0;
		if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}
		prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
				printerId);

		String printMode = "P";
		if (prefs.get("print_mode") != null) {
			printMode = (String) prefs.get("print_mode");
		}
		Map pDetails = new HashMap();
		GenericDocumentsFields.copyPatientDetails(pDetails, mrNo, patientId, false);
		Map ftlParamMap = new HashMap();
		ftlParamMap.put("visitdetails", pDetails);
		ftlParamMap.put("modules_activated",
				((Preferences) RequestContext.getSession().getAttribute("preferences")).getModulesActivatedMap());

		ftlParamMap.put("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
		ftlParamMap.put("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

		ftlParamMap.put("surgery_details", OperationDetailsDAO.getSurgeryDetailsForFTL(opDetailsId));
		ftlParamMap.put("operation_team_details", OperationDetailsDAO.getOperationTeam(opDetailsId));
		ftlParamMap.put("operation_anaethesia_details", OperationDetailsDAO.getOperationAnaesthesiaDetails(opDetailsId));
		ftlParamMap.put("opeartionsList", OperationDetailsDAO.getSurgeryListForFTL(opDetailsId));

		List<BasicDynaBean> operationsProcIdsList = otDAO.getOperations(patientId, opDetailsId);
		List opCompDetails = new ArrayList<BasicDynaBean>();
		Map operationWiseCompValues = new HashMap<String,Map<Object, List<List>>>();
		Map<Integer,Integer> sysCompDetails = new HashMap<Integer,Integer>();
		Map<String,String> operationNames = new HashMap<String,String>();
		BasicDynaBean components = null;
		AbstractInstaForms formDAO = new OTForms();
		PatientSectionDetailsDAO psdDAO = new PatientSectionDetailsDAO();
		String itemType = (String) formDAO.getKeys().get("item_type");
		for (BasicDynaBean operationIDS : operationsProcIdsList) {
			int operationProcId = (Integer)operationIDS.get("operation_proc_id");
			String operationName = (String)operationIDS.get("operation_name");

			Map params = new HashMap();
			params.put("operation_proc_id", new String[]{operationProcId+""});
			components = formDAO.getComponents(params);

			opCompDetails.add(components);
			String sectionIds = (String) components.get("sections");
			String[] sectionIdsArray = sectionIds.split(",");
			for (int i = 0 ; i < sectionIdsArray.length ; i++) {
				int sectionId = Integer.parseInt(sectionIdsArray[i]);
				if (sectionId < 0) {
					sysCompDetails.put(sectionId, sectionId);
				}
			}
			List<BasicDynaBean> sectionValues = psdDAO.getAllSectionDetails((String) pDetails.get("mr_no"),
					(String) pDetails.get("patient_id"), operationProcId, 0, (Integer) components.get("form_id"), itemType);

			Map<Object, List<List>> map = ConversionUtils.listBeanToMapListListBean(sectionValues, "str_section_detail_id", "field_id");

			operationWiseCompValues.put(operationProcId+"", map);
			operationNames.put((new Integer(operationProcId)).toString(), operationName);
		}

		ftlParamMap.put("ot_record_components", opCompDetails);
		ftlParamMap.put("opCompSectionValues", operationWiseCompValues);
		ftlParamMap.put("system_components", sysCompDetails);
		ftlParamMap.put("operationNames", operationNames);

		String templateName = request.getParameter("printTemplate");
		if (templateName == null || templateName.equals("")) {
			templateName = "BUILTIN_HTML";
		}
		Template t = null;
		String templateMode = null;
		if (templateName.equals("BUILTIN_HTML")) {
			t = AppInit.getFmConfig().getTemplate("OtRecordDetails.ftl");
			templateMode = "H";
		} else if (templateName.equals("BUILTIN_TEXT")) {
			t = AppInit.getFmConfig().getTemplate("OtRecordDetailsText.ftl");
			templateMode = "T";
		} else {
			BasicDynaBean pbean = PrintTemplatesDAO.getTemplateContent(templateName);

			if (pbean == null) {
				return null;
			}
			String templateContent =(String) pbean.get("template_content");
			templateMode = (String)pbean.get("template_mode");
			StringReader reader = new StringReader(templateContent);
			t = new Template("OtRecordPrint.ftl", reader, AppInit.getFmConfig());
		}
		StringWriter writer = new StringWriter();
		t.process(ftlParamMap, writer);

		HtmlConverter hc = new HtmlConverter(new DoctorConsultImageRetriever());
		Boolean repeatPHeader = ((String)prefs.get("repeat_patient_info")).equals("Y");

		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			if (templateMode != null && templateMode.equals("T")) {
				hc.textToPDF(writer.toString(), os,  prefs);
			} else {
				hc.writePdf(os, writer.toString(), "OT Record Details", prefs, false, repeatPHeader, true, true,
					false, false);
			}
			os.close();

		} else {
			String textReport = null;
			if (templateMode != null && templateMode.equals("T")) {
				textReport = new String(writer.toString().getBytes());
			} else {
				textReport = new String(hc.getText(writer.toString(), "OT Record Details", prefs, true, true));
			}
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");
		}
		return null;
	}

}
