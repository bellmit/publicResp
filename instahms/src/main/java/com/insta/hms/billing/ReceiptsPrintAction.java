package com.insta.hms.billing;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bob.hms.common.NumberToWordFormat;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.master.DepositReceiptRefundTemplate.DepositReceiptRefundPrintTemplateDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.ReceiptRefundPrintTemplate.ReceiptRefundPrintTemplateDAO;
import com.insta.hms.stores.RetailCustomerDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.TemplateException;

public class ReceiptsPrintAction extends DispatchAction{

	static Logger logger = LoggerFactory.getLogger(ReceiptsPrintAction.class);

	// patient deposits
	public ActionForward depositReceiptPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws IOException, JRException, SQLException, ParseException ,TemplateException,
		  DocumentException, XPathExpressionException ,TransformerException, Exception {

			HashMap params = new HashMap();
			String printerIdStr = req.getParameter("printerType");
			String receiptNo =  req.getParameter("deposit_no");
			String templateName = req.getParameter("printTemplate");
			String mrNo = req.getParameter("mrNo");
			params.put("receiptNo", receiptNo);
			params.put("printUserName", req.getSession().getAttribute("userid"));

			String depositPrintName = null;
			List<BasicDynaBean> depositsList = DepositsDAO.depositReceiptRefundPrint(receiptNo);
			params.put("depositsList", depositsList);
			BigDecimal depositAmount = BigDecimal.ZERO;
			if (!depositsList.isEmpty()){
				for(BasicDynaBean deposit : depositsList){
					depositAmount = (BigDecimal) deposit.get("amount");
				}
			}

			params.put("AmountinWords", NumberToWordFormat.wordFormat()
					.toRupeesPaise(depositAmount));
			String type = req.getParameter("type");

			GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			  params.put("currencySymbol", dto.getCurrencySymbol());

			if(type.equals("DR")){
				depositPrintName = "Deposit Receipt";
				params.put("depositPrintName", depositPrintName);

			}else if (type.equals("DF")){
				depositPrintName = "Deposit Refund";
				params.put("depositPrintName", depositPrintName);

			}

			/*
			 * Get the template for the print
			 */
			int printerId = 0;
			if ( (printerIdStr !=null) && !printerIdStr.equals("")) {
				printerId = Integer.parseInt(printerIdStr);
			}
			BasicDynaBean printPref = PrintConfigurationsDAO.getPageOptions(
					PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

			BasicDynaBean bean = GenericPreferencesDAO.getAllPrefs();

			if(templateName != null && templateName.equals("")) {
				if(bean.get("receipt_refund_print_default") != null && !bean.get("receipt_refund_print_default").equals(""))
					templateName = (String)bean.get("receipt_refund_print_default");
				else
					templateName = "BUILTIN_HTML";
			}

			DepositReceiptRefundPrintTemplateDAO templateDao = new DepositReceiptRefundPrintTemplateDAO();
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
					ftlGen = new FtlReportGenerator("DepositReceiptRefundPrint",reader);
				}else {
					ftlGen = new FtlReportGenerator("DepositReceiptRefundTextPrint",reader);
				}
			} else {
				if (templateName.equals("BUILTIN_HTML")){
					ftlGen = new FtlReportGenerator("DepositReceiptRefundPrint");
					templateMode = "H";
				}else{
					ftlGen = new FtlReportGenerator("DepositReceiptRefundTextPrint");
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
						hc.writePdf(os, textContent, "Receipt", printPref, false, false,
								true, true, true, false);
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


	public ActionForward receiptPrint(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException, IOException, TemplateException, DocumentException, ParseException, Exception {

		String receiptNo = req.getParameter("receiptNo");
		String printerIdStr = req.getParameter("printerType");
		String templateName = req.getParameter("printTemplate");
		String type = req.getParameter("type");
		BigDecimal netPayments = BigDecimal.ZERO;
		// params to be passed to the template processor
		Map params = new HashMap();

		// Get Generic Preferences
		GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
		params.put("currencySymbol", dto.getCurrencySymbol());

		/*
		 * Get the receipt or refund details and patient details
		 */
		BasicDynaBean receiptOrrefund = null;

		receiptOrrefund = ReceiptRelatedDAO.getReceiptDetails(receiptNo, type);

		params.put("receiptOrrefund", receiptOrrefund);
		netPayments=(BigDecimal) receiptOrrefund.get("amount");
		params.put("netPayments", NumberToWordFormat.wordFormat()
				.toRupeesPaise(netPayments));
		params.put("commissionAmount", receiptOrrefund.get("credit_card_commission_amount"));
		params.put("commissionPercentage", receiptOrrefund.get("credit_card_commission_percentage"));

		BasicDynaBean bill = BillDAO.getBillBean((String)receiptOrrefund.get("bill_no"));
		String visitType = (String)bill.get("visit_type");
		if (visitType.equals(Bill.BILL_VISIT_TYPE_RETAIL) ) {
			BasicDynaBean retailCustomer = new RetailCustomerDAO().getRetailCustomer((String)receiptOrrefund.get("visit_id"));
		if (retailCustomer != null)
			params.put("patient", retailCustomer.getMap());
		} else if (visitType.equals(Bill.BILL_VISIT_TYPE_INCOMING)) {
			params.put("patient",OhSampleRegistrationDAO.getIncomingCustomer((String)receiptOrrefund.get("visit_id")));
		} else {
			Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap((String)receiptOrrefund.get("visit_id"));
		params.put("patient", patientDetails);
		}
		params.put("visitType", visitType);
		params.put("type", type);
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

		BasicDynaBean bean = GenericPreferencesDAO.getAllPrefs();

		if(templateName != null && templateName.equals("")) {
			if(bean.get("receipt_refund_print_default") != null && !bean.get("receipt_refund_print_default").equals(""))
				templateName = (String)bean.get("receipt_refund_print_default");
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
					hc.writePdf(os, textContent, "Receipt", printPref, false, false,
							true, true, true, false);
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
}
