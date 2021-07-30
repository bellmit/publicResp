package com.insta.hms.emr;

import com.bob.hms.common.APIUtility;
import com.bob.hms.common.RequestContext;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper.DefaultType;
import com.lowagie.text.DocumentException;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class EmrPrintAction extends DispatchAction {

	static PatientHeaderTemplateDAO phTemplateDAO = new PatientHeaderTemplateDAO();
	static OPPrescriptionFtlHelper ftlHelper = new OPPrescriptionFtlHelper();
	public ActionForward printConsultation(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException, SQLException,
			DocumentException, TemplateException, XPathExpressionException, TransformerException, ParseException {
		
		String error = APIUtility.setConnectionDetails(servlet.getServletContext(), 
				request.getParameter("request_handler_key"));
		if (error != null) {
			APIUtility.setInvalidLoginError(response, error);
			return null;
		}
		
		String consIdStr = request.getParameter("consultation_id");
		int consId = Integer.parseInt(consIdStr);
		String printerIdStr = request.getParameter("printerId");
		BasicDynaBean prefs = null;
		String allFields = request.getParameter("allFields");
		String userName = RequestContext.getUserName();

		Integer printerId = null;
		if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}
		String template = request.getParameter("templateName");
		if(template == null || template.isEmpty())
			template = (String)GenericPreferencesDAO.getAllPrefs().get("default_prescription_print_template");
		prefs = DocumentPrintConfigurationsDAO.getPrescriptionPrintPreferences(template,printerId);

		String printMode = "P";
		if (prefs.get("print_mode") != null) {
			printMode = (String) prefs.get("print_mode");
		}
		
		// api parameter
		String logoHeader = request.getParameter("logoHeader");
		if (logoHeader != null && !logoHeader.equals("") &&
				(logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
						|| logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
			prefs.set("logo_header", logoHeader.toUpperCase());
		}
		
		
		String templateName = request.getParameter("templateName");
		String fromEmr = request.getParameter("fromEmr");
		OPPrescriptionFtlHelper.DefaultType templateType = null;
		Boolean usePrescPrintTemplate = new Boolean(request.getParameter("isPrescription"));
		if (usePrescPrintTemplate) {
			templateType = DefaultType.PRESCRIPTION;
		} else if (null != fromEmr && !fromEmr.equals("")) {
			templateType = DefaultType.EMR;
		} else {
			templateType = DefaultType.CONSULTATION;
		}
		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			ftlHelper.getConsultationFtlReport(consId, templateName, OPPrescriptionFtlHelper.ReturnType.PDF,
					prefs, allFields.equals("Y"), os, userName, templateType);
			os.close();

		} else {
			String textReport = new String(ftlHelper.getConsultationFtlReport(consId, templateName,
					OPPrescriptionFtlHelper.ReturnType.TEXT_BYTES, prefs, allFields.equals("Y"), null,
					userName, templateType));
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");

		}

		return null;
	}

	public ActionForward printClinicalInfo(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, DocumentException,
			XPathExpressionException, TemplateException, TransformerException, ParseException {
		String consIdStr = request.getParameter("consultation_id");
		int consId = Integer.parseInt(consIdStr);
		String printerIdStr = request.getParameter("printerId");
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

		String userName = (String)request.getSession(false).getAttribute("userid");
		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			ftlHelper.getTriageAndClinicalInfoFtlReport(consId, OPPrescriptionFtlHelper.ReturnType.PDF,
					prefs, os, userName, false);
			os.close();

		} else {
			String textReport = new String(ftlHelper.getTriageAndClinicalInfoFtlReport(consId,
					OPPrescriptionFtlHelper.ReturnType.TEXT_BYTES, prefs, null, userName, false));
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");

		}

		return null;
	}


	public ActionForward printTriageSummary(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException, DocumentException,
			XPathExpressionException, TemplateException, TransformerException, ParseException {
		
		String error = APIUtility.setConnectionDetails(servlet.getServletContext(), 
				request.getParameter("request_handler_key"));
		if (error != null) {
			APIUtility.setInvalidLoginError(response, error);
			return null;
		}
		
		String consIdStr = request.getParameter("consultation_id");
		int consId = Integer.parseInt(consIdStr);
		String printerIdStr = request.getParameter("printerId");
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
		
		// api parameter
		String logoHeader = request.getParameter("logoHeader");
		if (logoHeader != null && !logoHeader.equals("") &&
				(logoHeader.equalsIgnoreCase("Y") || logoHeader.equalsIgnoreCase("L")
						|| logoHeader.equalsIgnoreCase("H") || logoHeader.equalsIgnoreCase("N"))) {
			prefs.set("logo_header", logoHeader.toUpperCase());
		}

		String userName = RequestContext.getUserName();
		if (printMode.equals("P")) {
			response.setContentType("application/pdf");
			OutputStream os = response.getOutputStream();
			ftlHelper.getTriageAndClinicalInfoFtlReport(consId, OPPrescriptionFtlHelper.ReturnType.PDF,
					prefs, os, userName, true);
			os.close();

		} else {
			String textReport = new String(ftlHelper.getTriageAndClinicalInfoFtlReport(consId,
					OPPrescriptionFtlHelper.ReturnType.TEXT_BYTES, prefs, null, userName, true));
			request.setAttribute("textReport", textReport);
			request.setAttribute("textColumns", prefs.get("text_mode_column"));
			request.setAttribute("printerType", "DMP");
			return mapping.findForward("textPrintApplet");

		}

		return null;
	}
}