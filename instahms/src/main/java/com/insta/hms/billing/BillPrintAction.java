package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PatientVisitBillsPrintTemplateAction.PatientVisitBillsPrintTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ReceiptRefundPrintTemplate.ReceiptRefundPrintTemplateDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.stores.MedicineSalesDAO;
import com.insta.hms.stores.RetailCustomerDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class BillPrintAction extends DispatchAction{

	static Logger logger = LoggerFactory.getLogger(BillPrintAction.class);

	/*
	 * It is good to specify the sub-reports applicable to these prints, so that
	 * they get compiled before running. If there are no sub-reports, we could call
	 * DirectReportAction.do directly, without having to have methods in this Action class.
	 */

	public ActionForward getEstimatePrint(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception   {
		HashMap hm = new HashMap();
		String insuranceId = request.getParameter("insuranceId");
		String estimateId = request.getParameter("estimateId");
		hm.put("estimateId", estimateId);
		hm.put("insuranceId",new Integer(insuranceId));
		ReportPrinter.printPdfStream(request, response,"EstimatePrintSummary", hm);
		return null;
	}

	/*
	 * Bill print. Parameters handled:
	 *  detail: DET, CHS, SUM (full details, charge head summary, charge group summary)
	 *  option: Filters based on option, it is an extract of the bill
	 *    For extracts, we shouldn't show payment information. Only bill amounts and discounts.
	 */

	public ActionForward billPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		HttpSession session = req.getSession();

		Map params = BillPrintHelper.getBillJrxmlParams(req.getParameter("billNo"),
				req.getParameter("detailed"), req.getParameter("option"),
				(String) session.getAttribute("userid"));
		String printerIdStr = req.getParameter("printerType");
		return doPrint(m, req, res, "BillPrint", params, printerIdStr);
	}

	/*
	 * Patient expense statement: combines multiple bills for the patient.
	 * Options:
	 *  detail: DET, CHS, SUM (full details, charge head summary, charge group summary)
	 *  option: Filters based on option, it is an extract of the bill
	 *    For extracts, we shouldn't show payment information. Only bill amounts and discounts.
	 */
	public ActionForward expenseStatement(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		HttpSession session = req.getSession();

		Map params = BillPrintHelper.getBillJrxmlParams(req.getParameter("billNo"),
				req.getParameter("detailed"), req.getParameter("option"),
				(String) session.getAttribute("userid"));

		params.put("visitId", req.getParameter("visitId"));

		String printerIdStr = req.getParameter("printerType");
		return doPrint(m, req, res, "BillExpStmt", params, printerIdStr);
	}

	/*
	 * Pharmacy Breakup Bill
	 */
	public ActionForward pharmaBreakupBill(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
		HashMap params = new HashMap();
		params.put("billNo", req.getParameter("billNo"));
		params.put("currencySymbol", dto.getCurrencySymbol());
		String printerIdStr = req.getParameter("printerType");
		return doPrint(m, req, res, "BillPharmaBreakup", params, printerIdStr);
	}

	/*
	 * Pharmacy Expense Statement
	 */

	public ActionForward pharmaExpenseStmt(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
		HashMap params = new HashMap();
		params.put("visitId", req.getParameter("visitId"));
		params.put("currencySymbol", dto.getCurrencySymbol());
		String printerIdStr = req.getParameter("printerType");
		return doPrint(m, req, res, "BillPharmaExpStmt", params, printerIdStr);
	}


	public ActionForward receiptPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {
		HashMap params = new HashMap();
		String error = null ;
		try{
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			params.put("receiptNo", req.getParameter("receiptNo"));
			params.put("printUserName", dto.getUserNameInBillPrint());
			params.put("currencySymbol", dto.getCurrencySymbol());
			int printerId =0;
			String printerType = req.getParameter("printerType");
			if ((printerType !=null) && !printerType.equals("")){
				printerId = Integer.parseInt(printerType);
			}

			BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);
			PrintPageOptions opts = new PrintPageOptions(printPref);

			if (printerId != 0) {
				params.put("printerId", printerId);
			} else {
				params.put("printerId", printPref.get("printer_id"));
			}

			if (printPref.get("print_mode").equals("T")) {
				String textReport = ReportPrinter.printTextReport("BillReceiptPrint", params, opts, null);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
				req.setAttribute("textReport", textReport);
				req.setAttribute("printerType", "DMP");
				return m.findForward("textPrintApplet");
			} else {
				ReportPrinter.printPdfStream(req, res, "BillReceiptPrint", params, opts, null);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
			}
		} catch (JRException e){
			if (DataBaseUtil.isReportDesignInvalid(e)){
				error = "Unable to generate the report: please check the report margins in print definition " +
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print report: ", e);
			}else{
				throw(e);
			}
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward refundPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		HashMap params = new HashMap();
		String error = null ;
		try {
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			params.put("receiptNo", req.getParameter("receiptNo"));
			params.put("printUserName", dto.getUserNameInBillPrint());
			params.put("currencySymbol", dto.getCurrencySymbol());
			int printerId = 0;
			String  printerType = req.getParameter("printerType");
			if ((printerType !=null) && !printerType.equals("")){
				printerId = Integer.parseInt(printerType);
			}

			BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);
			PrintPageOptions opts = new PrintPageOptions(printPref);

			if (printerId != 0) {
				params.put("printerId", printerId);
			} else {
				params.put("printerId", printPref.get("printer_id"));
			}

			if (printPref.get("print_mode").equals("T")){
				String textReport = ReportPrinter.printTextReport("BillRefundPrint", params, opts, null);
				req.setAttribute("textReport", textReport);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
				req.setAttribute("printerType", "DMP");
				return m.findForward("textPrintApplet");
			}else{
				ReportPrinter.printPdfStream(req, res, "BillRefundPrint", params, opts, null);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
			}
		} catch (JRException e){
			if (DataBaseUtil.isReportDesignInvalid(e)){
				error = "Unable to generate the report: please check the report margins in print definition " +
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print report: ", e);
			}else{
				throw(e);
			}
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

	// patient deposits
	public ActionForward depositReceiptPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		HashMap params = new HashMap();
		String error = null ;
		try{
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			params.put("receiptNo", req.getParameter("receiptNo"));
			params.put("printUserName", dto.getUserNameInBillPrint());
			params.put("currencySymbol", dto.getCurrencySymbol());
			int printerId =0;
			String printerType = req.getParameter("printerType");
			if ((printerType !=null) && !printerType.equals("")){
				printerId = Integer.parseInt(printerType);
			}

			BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);
			PrintPageOptions opts = new PrintPageOptions(printPref);

			if (printerId != 0) {
				params.put("printerId", printerId);
			} else {
				params.put("printerId", printPref.get("printer_id"));
			}

			//	 For bug : 14369 Ignoring print_mode(Text, PDF)
			//   PDF Print is generated since Deposit receipt has no customized print template.

			ReportPrinter.printPdfStream(req, res, "DepositReceiptPrint", params, opts, null);

		} catch (JRException e){
			if (DataBaseUtil.isReportDesignInvalid(e)){
				error = "Unable to generate the report: please check the report margins in print definition " +
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print report: ", e);
			}else{
				throw(e);
			}
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward depositRefundPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		HashMap params = new HashMap();
		String error = null ;
		try {
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			params.put("receiptNo", req.getParameter("receiptNo"));
			params.put("printUserName", dto.getUserNameInBillPrint());
			params.put("currencySymbol", dto.getCurrencySymbol());
			int printerId = 0;
			String  printerType = req.getParameter("printerType");
			if ((printerType !=null) && !printerType.equals("")){
				printerId = Integer.parseInt(printerType);
			}

			BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);
			PrintPageOptions opts = new PrintPageOptions(printPref);

			if (printerId != 0) {
				params.put("printerId", printerId);
			} else {
				params.put("printerId", printPref.get("printer_id"));
			}

			// For bug : 14369 Ignoring print_mode(Text, PDF)
			// PDF Print is generated since Deposit refund has no customized print template.

			ReportPrinter.printPdfStream(req, res, "DepositRefundPrint", params, opts, null);

		} catch (JRException e){
			if (DataBaseUtil.isReportDesignInvalid(e)){
				error = "Unable to generate the report: please check the report margins in print definition " +
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print report: ", e);
			}else{
				throw(e);
			}
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;

	}

	public ActionForward tpReceiptPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException {

		HashMap params = new HashMap();
		String error = null ;
		try{
			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			params.put("receiptNo", req.getParameter("receiptNo"));
			params.put("printUserName", dto.getUserNameInBillPrint());
			params.put("currencySymbol", dto.getCurrencySymbol());
			int printerId = 0;
			String  printerType = req.getParameter("printerType");
			if ((printerType !=null) && !printerType.equals("")){
				printerId = Integer.parseInt(printerType);
			}

			BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);
			PrintPageOptions opts = new PrintPageOptions(printPref);

			if (printerId != 0) {
				params.put("printerId", printerId);
			} else {
				params.put("printerId", printPref.get("printer_id"));
			}

			if (printPref.get("print_mode").equals("T")){
				String textReport = ReportPrinter.printTextReport("BillTPReceiptPrint", params, opts, null);
				req.setAttribute("textReport", textReport);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
				req.setAttribute("printerType", "DMP");
				return m.findForward("textPrintApplet");
			} else {
				ReportPrinter.printPdfStream(req, res, "BillTPReceiptPrint", params, opts, null);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
			}
		} catch(JRException e) {
			if (DataBaseUtil.isReportDesignInvalid(e)){
				error = "Unable to generate the report: please check the report margins in print definition " +
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print report: ", e);
			}else{
				throw(e);
			}
		}
		FlashScope flash = FlashScope.getScope(req);
		flash.put("error", error);
		ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward getScreen(ActionMapping map, ActionForm form,
			HttpServletRequest req, HttpServletResponse res) throws SQLException,Exception,ServletException {
		return map.findForward("showTemplate");
	}

	public ActionForward doPrint(ActionMapping m, HttpServletRequest req, HttpServletResponse res,
			String report, Map params, String printerIdStr)
		throws IOException, JRException, SQLException, ParseException {

		String error = null;
		try {
			BasicDynaBean printPref = null;

			int printerId = 0;
			if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
				printerId = Integer.parseInt(printerIdStr);
			}

			printPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL,
					printerId);

			// printerId is required in the jrxml for the header/footer to know whether to
			// print the logo etc. or not.
			if (printerId != 0) {
				logger.debug("Printer ID from UI: " + printerId);
				params.put("printerId", printerId);
			} else {
				logger.debug("Printer ID from default: " + printPref.get("printer_id"));
				params.put("printerId", printPref.get("printer_id"));
			}

			PrintPageOptions opts = new PrintPageOptions(printPref);
			if (printPref.get("print_mode").equals("T")) {
				String textReport =	ReportPrinter.printTextReport(report, params, opts, null);
				req.setAttribute("textReport", textReport);
				req.setAttribute("textColumns", printPref.get("text_mode_column"));
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
				req.setAttribute("printerType", "DMP");
				return m.findForward("textPrintApplet");
			} else {
				ReportPrinter.printPdfStream(req, res, report, params, opts, null);
				BillDAO.updateBillPrintedStatus((String)params.get("billNo"));
				return null;
			}

		} catch (JRException e) {
			if (DataBaseUtil.isReportDesignInvalid(e)) {
				error = "Unable to generate the report: please check the report margins in print definition "+
					" and ensure that the page width/height are sufficient to accomodate the report";
				logger.warn("Unable to print report: ", e);
			} else {
				throw(e);
			}
		}

		if (error != null) {
			FlashScope flash = FlashScope.getScope(req);
			flash.put("error", error);
			ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}
		return null;
	}

	public ActionForward billPrintTemplate(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException, IOException, TemplateException, DocumentException, ParseException, Exception {

		HttpSession session = req.getSession();
		String billNo = req.getParameter("billNo");
		String visitId = req.getParameter("visitId");

		String templateName = req.getParameter("billType");
		String printerIdStr = req.getParameter("printerType");
		String userId = (String) session.getAttribute("userid");
		boolean PrintedStatusUnset = "N".equals(req.getParameter("setPrinted"));

		StringWriter writer = new StringWriter();
		String[] returnVals = BillPrintHelper.processBillTemplate(writer, billNo, templateName, userId);
		String templateMode = null;
		boolean isFinalized = false;
		boolean isDuplicate = false;
		
		if (returnVals != null) {
		    templateMode = returnVals[0];
		    isFinalized = !returnVals[1].equals("A");
		    isDuplicate =  !returnVals[4].equals("N");
		}
		if (templateMode == null) {
			// couldn't find the template in the db, bail out with error.
			String error = "Template " + templateName + " does not exist. Please select valid template";
			logger.error(error);
			FlashScope flash = FlashScope.getScope(req);
			flash.put("error", error);
			ActionRedirect redirect = new ActionRedirect(m.findForward("reportErrors"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			return redirect;
		}

		String billContent = writer.toString();

		int printerId = 0;
		if ((printerIdStr !=null) && !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}

		BasicDynaBean printPref =
			PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

		HtmlConverter hc = new HtmlConverter();

		if (printPref.get("print_mode").equals("T")) {
			String textContent = null;
			if (templateMode.equals("T")){
				// write the output as is.
				textContent = billContent;
			} else {
				// convert from HTML to text
				textContent = new String(hc.getText(billContent, "Provisional Bill", printPref, true, true));
			}
			req.setAttribute("textReport", textContent);
			req.setAttribute("textColumns", printPref.get("text_mode_column"));
			if(!PrintedStatusUnset) {
			  BillDAO.updateBillPrintedStatus(billNo);
			}
			req.setAttribute("printerType", "DMP");
			return m.findForward("textPrintApplet");

		} else {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/pdf");
			try {
				if (templateMode.equals("T")){
					// convert text to PDF
					hc.textToPDF(billContent, os, printPref);
				} else {
					// convert html to PDF
					Boolean repeatPHeader = ((String) printPref.get("repeat_patient_info")).equals("Y");
					hc.writePdf(os, billContent, "Provisional Bill", printPref, false, repeatPHeader, true, true,
							isFinalized, isDuplicate);
				}
				if(!PrintedStatusUnset) {
	        BillDAO.updateBillPrintedStatus(billNo);
	      }

			} catch (Exception e) {
				logger.error("Generated HTML content:");
				logger.error(billContent);
				throw(e);
			} finally {
				os.close();
			}

			return null;
		}
	}

	public ActionForward visitExpenceStatement(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException,
		    TemplateException, DocumentException, ParseException, Exception{

		String visitId = req.getParameter("visitId");
		String templateName = req.getParameter("billType");
		String printerIdStr = req.getParameter("printerType");
		String userId = (String) req.getSession(false).getAttribute("userid");

		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
		Map params = new HashMap();
		params.put("patient", patientDetails);

		int printerId = 0;
	   if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
		   printerId = Integer.parseInt(printerIdStr);
	   }
	   BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
			   PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

	   if (templateName == null || templateName.equals("")) {
			   templateName = "BUILTIN_HTML";
	   }

	   Template ftlTemplate = null;
		String templateMode = null;

		if (templateName.equals("BUILTIN_HTML")) {
			ftlTemplate = AppInit.getFmConfig().getTemplate("PatientVisitBillsPrintTemplate.ftl");
			templateMode = "H";

		} else if (templateName.equals("BUILTIN_TEXT")) {
			ftlTemplate = AppInit.getFmConfig().getTemplate("PatientVisitBillsPrintTextTemplate.ftl");
			templateMode="T";

		} else {
			PatientVisitBillsPrintTemplateDAO templateDao = new PatientVisitBillsPrintTemplateDAO();
			BasicDynaBean tmpBean = templateDao.findByKey("template_name", templateName);

			if (tmpBean == null) {
				return null;
			}

			String templateContent = (String)tmpBean.get("template_content");
			templateMode = (String) tmpBean.get("template_mode");

			StringReader reader =  new StringReader(templateContent);
			ftlTemplate = new Template(templateName+".ftl", reader, AppInit.getFmConfig());
		}

		StringWriter writer = new StringWriter();
		params.put("user", userId);
		setVisitExpenceStmtParams(params,visitId);

		ftlTemplate.process(params, writer);

		String content = writer.toString();

		HtmlConverter hc = new HtmlConverter();

		if (printPref.get("print_mode").equals("T")) {
			String textContent = null;
			if (templateMode.equals("T")){
				// write the output as is.
				textContent = content;
			} else {
				// convert from HTML to text
				textContent = new String(hc.getText(content, "Patient Visit Bills", printPref, true, true));
			}
			req.setAttribute("textReport", textContent);
			req.setAttribute("textColumns", printPref.get("text_mode_column"));
			req.setAttribute("printerType", "DMP");
			return m.findForward("textPrintApplet");

		} else {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/pdf");
			try {
				if (templateMode.equals("T")){
					// convert text to PDF
					hc.textToPDF(content, os, printPref);
				} else {
					// convert html to PDF
					Boolean repeatPHeader = ((String) printPref.get("repeat_patient_info")).equals("Y");
					hc.writePdf(os, content, "Patient Visit Bills", printPref, false, repeatPHeader, true, true,true, false);
				}
			} catch (Exception e) {
				logger.error("Generated HTML content:");
				logger.error(content);
				throw(e);
			} finally {
				os.close();
			}
			return null;
		}

	}

	public ActionForward receiptRefundPrintTemplate(ActionMapping m, ActionForm f,
	HttpServletRequest req, HttpServletResponse res)throws SQLException, IOException,
    TemplateException, DocumentException, ParseException, Exception {

	   String receiptNo = req.getParameter("receiptNo");
	   String printerIdStr = req.getParameter("printerType");
	   String templateName = req.getParameter("printTemplate");
	   String paymentType = req.getParameter("type");
	   BigDecimal netPayments = BigDecimal.ZERO;
	   boolean PrintedStatusUnset = "N".equals(req.getParameter("visitId"));

	   // params to be passed to the template processor
	   Map params = new HashMap();
	   // Get Generic Preferences

	   GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
	   params.put("currencySymbol", dto.getCurrencySymbol());

    BasicDynaBean bill = null;
    String visitType;
    String visitId;
    Map patientDetails = null;
	   /*
		* Get the receipt or refund details and patient details
		*/
    BasicDynaBean receiptOrrefund = ReceiptRelatedDAO.getReceiptDetails(receiptNo, paymentType);
    params.put("receiptOrrefund", receiptOrrefund);
    netPayments = (BigDecimal) receiptOrrefund.get("amount");

    if (receiptOrrefund.get("bill_no") == null) {
      visitType = req.getParameter("visitType");
      visitId = req.getParameter("patientId");
    } else {
      bill = BillDAO.getBillBean((String) receiptOrrefund.get("bill_no"));
      visitType = (String) bill.get("visit_type");
      visitId = (String) receiptOrrefund.get("visit_id");
    }
    if (visitType.equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
      BasicDynaBean retailCustomer = new RetailCustomerDAO().getRetailCustomer(visitId);
      if (retailCustomer != null) {
        patientDetails = retailCustomer.getMap();
      }
    } else if (visitType.equals(Bill.BILL_VISIT_TYPE_INCOMING)) {
      patientDetails = (Map) OhSampleRegistrationDAO.getIncomingCustomer(visitId);
    } else {
      patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
    }

    params.put("receiptOrrefund", receiptOrrefund);
    params.put("netPayments", NumberToWordFormat.wordFormat().toRupeesPaise(netPayments));
    params.put("patient", patientDetails);
    params.put("visitType", visitType);
    params.put("type", paymentType);
    params.put("bill", bill);

	   /*
		* Get the template for the print
		*/
	   int printerId = 0;
	   if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
		   printerId = Integer.parseInt(printerIdStr);
	   }
	   BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
			   PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

	   if (templateName == null || templateName.equals("")) {
		   BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();
		   String defaultTemplate = (String) prefs.get("receipt_refund_print_default");
		   if (defaultTemplate != null && !defaultTemplate.equals(""))
			   templateName = defaultTemplate;
		   else
			   templateName = "BUILTIN_HTML";
	   }

	   ReceiptRefundPrintTemplateDAO templateDao = new ReceiptRefundPrintTemplateDAO();
	   BasicDynaBean tmpBean = templateDao.getTemplateContent(templateName);
	   String templateMode = null;
	   String templateContent = null;
	   if (tmpBean!=null){
		   templateContent = (String)tmpBean.get("template_content");
		   templateMode = (String) tmpBean.get("template_mode");
	   }

	   FtlReportGenerator ftlGen = null;
	    if (templateContent != null) {
	      StringReader  reader =  new StringReader(templateContent);
	      if (templateMode.equals("H")){
	        ftlGen = new FtlReportGenerator("ReceiptRefundPrintTemplate",reader);
	      }else {
	        ftlGen = new FtlReportGenerator("ReceiptRefundPrintTextTemplate",reader);
	      }
	    } else {
	      if (templateName.equals("BUILTIN_HTML")){
	        ftlGen = new FtlReportGenerator("ReceiptRefundPrintTemplate");
	        templateMode = "H";
	      }else{
	        ftlGen = new FtlReportGenerator("ReceiptRefundPrintTextTemplate");
	        templateMode="T";
	      }
	    }

	   /*
		* Process the template and get the html
		*/
	   StringWriter writer = new StringWriter();
	   ftlGen.setReportParams(params);
	   ftlGen.process(writer);
	   String textContent = writer.toString();

	   /*
		* Conver the html to text or PDF and send it as response
		*/
	   HtmlConverter hc = new HtmlConverter();
	   PrintPageOptions opts = new PrintPageOptions(printPref);

	   if (printPref.get("print_mode").equals("T")) {
		   String textReport = null;
		   if (templateMode.equals("T")){
			   textReport = textContent;
		   }else{
			   textReport = new String(hc.getText(textContent, "Receipt", printPref,
						   true, true));
		   }
		   req.setAttribute("textReport", textReport);
		   req.setAttribute("textColumns", printPref.get("text_mode_column"));
		   req.setAttribute("printerType", "DMP");
		   return m.findForward("textPrintApplet");

	   } else if (printPref.get("print_mode").equals("H")) {
		   res.setContentType("text/html");
		   res.getWriter().write(textContent);
		   return null;
	   } else if (printPref.get("print_mode").equals("R")) {
		   res.setContentType("application/rtf");
		   res.getWriter().write(textContent);
		   return null;
	   } else {
		   OutputStream os = res.getOutputStream();
		   res.setContentType("application/pdf");
		   try {
			   if (templateMode.equals("T")){
				   hc.textToPDF(textContent, os, printPref);
			   }else{
				   hc.writePdf(os, textContent, "Receipt", printPref, false, false, true, true, true, false);
			   }
			   if(!PrintedStatusUnset) {
		        BillDAO.updateBillPrintedStatus((String)receiptOrrefund.get("bill_no"));
		      }
		   } catch (Exception e) {
			   logger.error("Original Template:");
			   logger.error(templateContent);
			   logger.error("Generated HTML content:");
			   logger.error(textContent);
			   throw(e);
		   } finally {
			   os.close();
		   }
		   return null;
	   }
	}

	public void setVisitExpenceStmtParams(Map params,String visitId)throws SQLException{
		GenericPreferencesDTO prefsDto = GenericPreferencesDAO.getGenericPreferences();
		BasicDynaBean genPrefs = GenericPreferencesDAO.getAllPrefs();
		List diagnosisDetails = MRDDiagnosisDAO.getAllDiagnosisDetails(visitId);
		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
		params.put("patient", patientDetails);

		params.put("currencySymbol", prefsDto.getCurrencySymbol());
		params.put("genPrefs", genPrefs);

		List<Bill > billsList = new BillDAO().getVisitBills(visitId);

		params.put("bill", billsList);
		params.put("patient", patientDetails);
		params.put("diagnosisDetails", diagnosisDetails);

		List<BasicDynaBean> salesItemsList = new ArrayList<BasicDynaBean>();

		Map saleIDMap = ConversionUtils.listBeanToMapListBean(salesItemsList, "sale_id");
		params.put("saleItemsMap", saleIDMap);
		params.put("saledIDs", saleIDMap.keySet());
		List chargeGrpList = new ArrayList();
		List chargeHeadList = new ArrayList();
		List grpList = new ArrayList();
		List suGrpList = new ArrayList();
		List recList = new ArrayList();
		Map<String,HashMap> chargeGrpMap = new HashMap<String,HashMap>();
		Map<String,Set> chargeGrpKeysMap = new HashMap<String,Set>();

		Map<String,HashMap> chargeHeadsMap = new HashMap<String,HashMap>();
		Map<String,Set> chargeHeadsKeyMap = new HashMap<String,Set>();

		Map<String,HashMap> serGrpMap = new HashMap<String,HashMap>();
		Map<String,Set> serGrpKeysMap = new HashMap<String,Set>();

		Map<String,HashMap> serSubGrpMap = new HashMap<String,HashMap>();
		Map<String,Set> serSubGrpKeysMap = new HashMap<String,Set>();

		Map<String,HashMap> receiptsMap = new HashMap<String,HashMap>();
		Map<String ,List<BasicDynaBean>> receiptsMapForPayment = new HashMap<String, List<BasicDynaBean>>();

		List<BasicDynaBean> charges = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> receipts = new ArrayList<BasicDynaBean>();
		Map<String,List<BasicDynaBean>> salesItemsListMap = new HashMap<String, List<BasicDynaBean>>();
		Map<String,Map> receiptTypeMap = new HashMap<String, Map>();
		List<BasicDynaBean> salesItems_l = null;
		List<BasicDynaBean> receipts_l = null,charge_l = null;
		for( Bill bill : billsList ){
			charge_l = ChargeDAO.getPrintChargeDetailsBean(bill.getBillNo());
			receipts_l = ReceiptRelatedDAO.getReceptRefundList(bill.getBillNo());
			salesItems_l = MedicineSalesDAO.getPharmaBreakupList(bill.getBillNo());
			charges.addAll(charge_l);
			salesItemsList.addAll(salesItems_l);
			salesItemsListMap.put(bill.getBillNo(),salesItems_l);
			chargeGrpMap.put(bill.getBillNo(),ConversionUtils.listBeanToMapListBean(charge_l, "chargegroup_name"));
			chargeGrpKeysMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "chargegroup_name").keySet());
			chargeHeadsMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "chargehead_name"));
			chargeHeadsKeyMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "chargehead_name").keySet());
			serGrpMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "service_group_name"));
			serGrpKeysMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "service_group_name").keySet());
			serSubGrpMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "service_sub_group_name"));
			serSubGrpKeysMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(charge_l, "service_sub_group_name").keySet());
			receiptsMapForPayment.put(bill.getBillNo(), receipts_l);
			receipts.addAll(receipts_l);
			receiptTypeMap.put(bill.getBillNo(), ConversionUtils.listBeanToMapListBean(receipts_l, "payment_type"));
			receiptsMap.put(bill.getBillNo(),ConversionUtils.listBeanToMapListBean(receipts_l, "payment_type"));
		}

		Map<String,List<BasicDynaBean>> chargesMap = ConversionUtils.listBeanToMapListBean(charges,"bill_no");
		params.put("salesItemsList", salesItemsListMap);
		params.put("allsalesItemsList", salesItemsList);
		params.put("charges", chargesMap);
		params.put("allcharges",charges);

		Map allChargeGroupMap = ConversionUtils.listBeanToMapListBean(charges, "chargegroup_name");
		params.put("chargeGroupMap",chargeGrpMap);
		params.put("chargeGroups", chargeGrpKeysMap);
		params.put("allchargeGroupMap", allChargeGroupMap);
		params.put("allchargeGroups", allChargeGroupMap.keySet());

		// organize into Charge Head based map, this will result in a map
		// like GREG => [bean1], LTDIA => [bean2, bean3], ...
		Map allChargeHeadMap = ConversionUtils.listBeanToMapListBean(charges, "chargehead_name");
		params.put("chargeHeadMap", chargeHeadsMap);
		params.put("chargeHeads", chargeHeadsKeyMap);
		params.put("allchargeHeadMap", allChargeHeadMap);
		params.put("allchargeHeads", allChargeHeadMap.keySet());

		// organize into Service Group based map, this will result in a map
		// like Direct Charge => [bean1, bean2], Laboratory => [bean3, bean4], ...
		Map serviceGroupMap = ConversionUtils.listBeanToMapListBean(charges, "service_group_name");
		params.put("serviceGroupMap", serGrpMap);
		params.put("serviceGroups", serGrpMap.keySet());
		params.put("allserviceGroupMap", serviceGroupMap);
		params.put("allserviceGroups", serviceGroupMap.keySet());

		// organize into Service Sub Group based map, this will result in a map
		// like Dept1 => [bean1], Dept2 => [bean2, bean3], ...
		Map serviceSubGroupMap = ConversionUtils.listBeanToMapListBean(charges, "service_sub_group_name");
		params.put("serviceSubGroupMap", serSubGrpMap);
		params.put("serviceSubGroups", serSubGrpMap.keySet());
		params.put("allserviceSubGroupMap", serviceSubGroupMap);
		params.put("allserviceSubGroups", serviceSubGroupMap.keySet());

		/*
		 * Receipts for this bill
		 */
		params.put("receipts", receiptsMap);
		params.put("allreceipts", receipts);

		// organize receipts by main_type
		Map allReceiptTypeMap = ConversionUtils.listBeanToMapListBean(receipts, "payment_type");
		params.put("allreceiptTypeMap", allReceiptTypeMap);
		params.put("receiptTypeMap", receiptTypeMap);
		// no need of all receipt types, this list is static: R,S,F

		List<Integer> pkgIdList = new ArrayList<Integer>();
		List<String> ratePlanList = new ArrayList<String>();
		List<String> bedTypeList = new ArrayList<String>();

		/*
		 * Some totals: total bill amount, claim amount, total discounts, net payments
		 */
		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal totalDiscount = BigDecimal.ZERO;
		BigDecimal totalClaimAmount = BigDecimal.ZERO;
		BigDecimal patientRoundOff = BigDecimal.ZERO;
		BigDecimal insRoundOff = BigDecimal.ZERO;

		BigDecimal alltotalAmount = BigDecimal.ZERO;
		BigDecimal alltotalDiscount = BigDecimal.ZERO;
		BigDecimal alltotalClaimAmount = BigDecimal.ZERO;
		BigDecimal allroundOff = BigDecimal.ZERO;

		Map totalAmountMap = new HashMap();
		Map totalDiscountMap = new HashMap();
		Map totalClaimAmountMap = new HashMap();
		Map patientRoundOffMap = new HashMap();
		Map insRoundOffMap = new HashMap();
		Map hasDiscountsMap = new HashMap();
		Map packageDetMap = new HashMap();
		boolean hasDiscounts = false;
		Connection con = null;
		BillActivityCharge activity = null;
		try{
			con = DataBaseUtil.getConnection();
			for( Bill bill : billsList ){
				totalAmount = BigDecimal.ZERO;
				totalDiscount = BigDecimal.ZERO;
				totalClaimAmount = BigDecimal.ZERO;
				patientRoundOff = BigDecimal.ZERO;
				if(chargesMap.get(bill.getBillNo()) != null){
				for (BasicDynaBean charge : chargesMap.get(bill.getBillNo())) {
					String chargeStatus = (String) charge.get("status");
					if (!chargeStatus.equals("X")) {
						if (((String) charge.get("charge_head")).equals("ROF")) {
							patientRoundOff = ((BigDecimal) charge.get("amount")).subtract((BigDecimal) charge.get("insurance_claim_amount"));
							insRoundOff = (BigDecimal) charge.get("insurance_claim_amount");
						} else {
							BigDecimal chargeAmount = (BigDecimal) charge.get("amount");
							BigDecimal discount = (BigDecimal) charge.get("discount");
							BigDecimal claimAmount = (BigDecimal) charge.get("insurance_claim_amount");

							totalAmount = totalAmount.add(chargeAmount);
							totalDiscount = totalDiscount.add(discount);
							totalClaimAmount = totalClaimAmount.add(claimAmount);
							if (discount.compareTo(BigDecimal.ZERO) != 0)
								hasDiscounts = true;

							BillActivityChargeDAO bacDAO = new BillActivityChargeDAO(con);
							activity = bacDAO.getActivity((String) charge.get("charge_id"));

							//Search if PKGPKG charge exists in the bill charges.
							if(((String) charge.get("charge_head")).equals("PKGPKG")
									&& activity != null) {
								pkgIdList.add(new Integer((String) charge.get("act_description_id")));
								ratePlanList.add((String) charge.get("bill_rate_plan_id"));
								bedTypeList.add((String) patientDetails.get("bill_bed_type"));
							}
						}
					}
				}
			}
				if (null != pkgIdList && pkgIdList.size() > 0) {
					List pkgComponentDetails = ChargeDAO.getPackageComponentsList(pkgIdList, ratePlanList, bedTypeList);
					Map pkgDetails = ConversionUtils.listBeanToMapListBean(pkgComponentDetails, "package_id");
					packageDetMap.put(bill.getBillNo(), pkgDetails);
				}

				totalAmountMap.put(bill.getBillNo(), totalAmount);
				alltotalAmount= alltotalAmount.add(totalAmount);
				totalDiscountMap.put(bill.getBillNo(), totalDiscount);
				alltotalDiscount = alltotalDiscount.add(totalDiscount);
				totalClaimAmountMap.put(bill.getBillNo(), totalClaimAmount);
				alltotalClaimAmount = alltotalClaimAmount.add(totalClaimAmount);
				patientRoundOffMap.put(bill.getBillNo(), patientRoundOff);
				insRoundOffMap.put(bill.getBillNo(), insRoundOff);
				allroundOff = allroundOff.add(patientRoundOff.add(insRoundOff));
				hasDiscountsMap.put(bill.getBillNo(), hasDiscounts);
			}
		}finally{
			DataBaseUtil.closeConnections(con, null);
		}

		params.put("totalAmount", totalAmountMap);
		params.put("totalDiscount", totalDiscountMap);
		params.put("totalClaimAmount", totalClaimAmountMap);
		params.put("hasDiscounts", hasDiscountsMap);
		params.put("patientRoundOff", patientRoundOffMap);
		params.put("insRoundOff", insRoundOffMap);

		params.put("alltotalAmount", alltotalAmount);
		params.put("alltotalDiscount", alltotalDiscount);
		params.put("alltotalClaimAmount", alltotalClaimAmount);
		params.put("allhasDiscounts", hasDiscounts);
		params.put("allroundOff", allroundOff);

		params.put("packageDetailsMap", packageDetMap);
		Map<String,BigDecimal> netPaymentsMap = new HashMap<String, BigDecimal>();
		Map<String,String> netPaymentsWordsMap = new HashMap<String, String>();
		BigDecimal netPayments = BigDecimal.ZERO;
		Map<String,BigDecimal> depositSetOffMap = new HashMap<String, BigDecimal>();
		BasicDynaBean bean = null;
		BigDecimal depSetOff = BigDecimal.ZERO;

		for( Bill bill : billsList ){

			bean = null;
			netPayments = BigDecimal.ZERO;
			for (BasicDynaBean receipt : receiptsMapForPayment.get(bill.getBillNo())) {
				BigDecimal amt = (BigDecimal) receipt.get("amount");
				netPayments = netPayments.add(amt);
			}
			netPaymentsMap.put(bill.getBillNo(), netPayments);
			netPaymentsWordsMap.put(bill.getBillNo(), NumberToWordFormat.wordFormat()
					.toRupeesPaise(netPayments));

			bean = DepositsDAO.getBillDepositDetails(bill.getBillNo());
			BigDecimal bilDepSetOff = ( bean == null ? BigDecimal.ZERO : (BigDecimal)bean.getMap().get("deposit_set_off") );
			depositSetOffMap.put(bill.getBillNo(), bilDepSetOff);
			depSetOff = depSetOff.add(bilDepSetOff);
		}
		params.put("netPayments", netPaymentsMap);
		params.put("netPaymentsWords", netPaymentsWordsMap);
		params.put("allnetPayments", netPayments);
		params.put("allnetPaymentsWords",   NumberToWordFormat.wordFormat()
				.toRupeesPaise(netPayments));

		params.put("billDeposits", depositSetOffMap);
		params.put("allbillDeposits", depSetOff);
	}
}


