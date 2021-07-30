/**
 *
 */
package com.insta.hms.pbmauthorization;

import com.insta.hms.common.AppInit;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.CommonPrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * @author mithun.saha
 *
 */
public class PBMPresciptionPrintAction extends DispatchAction{

	public ActionForward printPbmPrescriptions(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			DocumentException, TemplateException, XPathExpressionException, TransformerException, ParseException,Exception {
		String pbmPrescIdStr = request.getParameter("pbm_presc_id");
		String visitId = request.getParameter("visit_id");
		int pbmPrescId = Integer.parseInt(pbmPrescIdStr);
		String printerIdStr = request.getParameter("printerId");
		String userName = (String)request.getSession(false).getAttribute("userid");
		BasicDynaBean prefs = null;
		Map pDetails = new HashMap();
		GenericDocumentsFields.copyPatientDetails(pDetails, null,visitId,false);
		Map ftlParamMap = new HashMap();
		ftlParamMap.put("patientdetails", pDetails);
		List<BasicDynaBean> allPbmPrescriptions = PBMPrescriptionsDAO.getPbmPrescriptionDetails(pbmPrescId);
		ftlParamMap.put("secondary_complaints", new SecondaryComplaintDAO().getSecondaryComplaints((String) visitId));
		ftlParamMap.put("presMedicines", allPbmPrescriptions);
		ftlParamMap.put("userName", userName);
		ftlParamMap.put("pbm_request_id", request.getParameter("pbm_request_id"));
		ftlParamMap.put("approval_status", request.getParameter("approval_status"));
		ftlParamMap.put("approval_comments", request.getParameter("approval_comments"));


		Integer printerId = null;
		if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}
		String templateName = request.getParameter("printTemplate");
		if (templateName == null || templateName.equals("")) {
			templateName = "BUILTIN_HTML";
		}
		prefs = DocumentPrintConfigurationsDAO.getPrescriptionPrintPreferences(templateName,printerId);

		Template t = null;
		String templateMode = null;
		if (templateName.equals("BUILTIN_HTML")) {
			t = AppInit.getFmConfig().getTemplate("PbmPrescription.ftl");
			templateMode = "H";
		} else if (templateName.equals("BUILTIN_TEXT")) {
			t = AppInit.getFmConfig().getTemplate("PbmPrescriptionText.ftl");
			templateMode = "T";
		} else {
			BasicDynaBean pbean = PrintTemplatesDAO.getTemplateContent(templateName);

			if (pbean == null) {
				return null;
			}
			String templateContent =(String) pbean.get("template_content");
			templateMode = (String)pbean.get("template_mode");
			StringReader reader = new StringReader(templateContent);
			t = new Template("DentalConsultationPrint.ftl", reader, AppInit.getFmConfig());
		}

		String printMode = "P";
		if (prefs.get("print_mode") != null) {
			printMode = (String) prefs.get("print_mode");
		}
		StringWriter writer = new StringWriter();
		t.process(ftlParamMap, writer);
		HtmlConverter hc = new HtmlConverter();
		Boolean repeatPHeader = ((String)prefs.get("repeat_patient_info")).equals("Y");
		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			if (templateMode != null && templateMode.equals("T")) {
				hc.textToPDF(writer.toString(), os,  prefs);
			} else {
				hc.writePdf(os, writer.toString(), "", prefs, false, repeatPHeader, true, true,
					false, false);
			}
			os.close();

		} else {
			String textReport = null;
			if (templateMode != null && templateMode.equals("T")) {
				textReport = new String(writer.toString().getBytes());
			} else {
				textReport = new String(hc.getText(writer.toString(), "Dental Treatment Details", prefs, true, true));
			}
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");
		}

		return null;
	}


}
