package com.insta.hms.payments;

import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.PrintPageOptions;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.lowagie.text.DocumentException;
import flexjson.JSONSerializer;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class PaymentDashboardAction extends DispatchAction{

	static Logger logger = LoggerFactory.getLogger(PaymentDashboardAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getPaymentDues(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res)throws Exception {

		String paymentType = m.getProperty("payment_type");
		if (paymentType.equalsIgnoreCase("regular")) {
			return getPaymentDashboard(m,f,req,res);
		}
		return m.findForward("paymentvoucherdashboard");
	}

	@IgnoreConfidentialFilters
	private ActionForward getPaymentDashboard(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		JSONSerializer js = new JSONSerializer();
		List payeeNamesList = PaymentsDAO.getPayeeList();
		List payeeList =  ConversionUtils.listBeanToListMap(payeeNamesList);
		Map map = request.getParameterMap();

		PagedList paymentDues = PaymentsDAO.getPaymentDetails(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("paymentDues", paymentDues);
		request.setAttribute("payeeList", js.serialize(payeeList));
		request.setAttribute("screen", "Payment");
		return mapping.findForward("paymentvoucherdashboard");
	}

	@IgnoreConfidentialFilters
	public ActionForward viewVoucherDetails(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		String voucherNo = request.getParameter("voucherno");
		String screen = request.getParameter("screen");
		List list = null;
		list = PaymentsDAO.getVoucherDetails(voucherNo);
		request.setAttribute("vouchersList",list);

		BasicDynaBean printPref = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
		request.setAttribute("pref",printPref);
		request.setAttribute("screen", screen);
		return mapping.findForward("paymentvoucherview");
	}

	@IgnoreConfidentialFilters
	public ActionForward printVoucher(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws IOException, JRException,
		   SQLException, ParseException, TemplateException, XPathExpressionException, DocumentException,
		   TransformerException, Exception {

			   GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
			   HashMap params = new HashMap();
			   params.put("currencySymbol", dto.getCurrencySymbol());
			   String error = null;
			   String printType = request.getParameter("printType");
			   String voucherType = request.getParameter("voucherType");
			   String paymentType = request.getParameter("paymentType");
			   String voucherNo = request.getParameter("voucherno");

			   params.put("voucherNo",request.getParameter("voucherno"));
			   params.put("printType", printType);

			   BasicDynaBean printPref = null;
			   int printerId = 0;
			   String report = null;
			   if ( (null != request.getParameter("printDefType")) && ! ("").equals(request.getParameter("printDefType"))) {
				   printerId = Integer.parseInt(request.getParameter("printDefType"));
			   }
			   printPref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL,printerId);

			   if (printerId != 0) {
				   logger.debug("Printer ID from UI: " + printerId);
				   params.put("printerId", printerId);
			   } else {
				   logger.debug("Printer ID from default: " + printPref.get("printer_id"));
				   params.put("printerId", printPref.get("printer_id"));
			   }
			   if (voucherType==null){
				   voucherType="P";
			   }
			   if (paymentType == null){
				   paymentType = "D";
			   }

			   //Global variables

			   BigDecimal amount = BigDecimal.ZERO;
			   BigDecimal tds = BigDecimal.ZERO;
			   String bank = null;
			   String referenceNo=null;
			   String mode = null;
			   String card = null;
			   String username = null;

			   /**
			    *  Payment types :
			    *  1. Miscellaneous  - C
			    *  2. Doctor - D
			    *  3. Referral - R
			    *  4. Prescribing Doctor - P
			    *  5. Other referral - F
			    *  6. Out House - O
			    *  7. Supplier - S
			    *
			    *  Voucher types :
			    *  1. Payment Voucher - S
			    *  2. Payment Reversal Voucher - R
			    *
			    *  Print types :
			    *  1. summary
			    *  2. detail
			    */

			   /* If print type is summary  */
			   /*  PRINT PAYMENT VOUCHER - ALL PAYMENT TYPES INCLUDING REVERSAL PAYMENTS */

			   if (printType.equals("summary")) {
				   List<BasicDynaBean> voucherSummary = PaymentsDAO.getPaymentVoucherSummary(voucherNo);
				   params.put("voucherDetails", voucherSummary);
				   if (!voucherSummary.isEmpty()){
					   for (BasicDynaBean summary : voucherSummary){
						   amount = (BigDecimal)summary.get("totalamount");
						   tds = (BigDecimal) summary.get("tds_amount");
						   bank = (String) summary.get("bank");
						   mode = (String) summary.get("payment_mode");
						   card = summary.get("card_type") == null ? "" : (String) summary.get("card_type");
						   referenceNo = (String) summary.get("reference_no");
						   username = (String) summary.get("username");
					   }
					   if (voucherType.equals("R")) {
						   amount = amount.subtract(tds);
					   }
				   }
			   }


			   /* If print type is detail  */

			   /* REVERSAL PAYMENT DETAILS - voucher type is reversal (R)*/

			   if (printType.equals("detail")) {
				   if (voucherType.equals("R")) {
					   List<BasicDynaBean> paymentReversal = PaymentsDAO.getPaymentReversalDetails(voucherNo);
					   params.put("voucherDetails", paymentReversal);
					   if (!paymentReversal.isEmpty()){
						   for (BasicDynaBean reversal : paymentReversal){
							   amount = (BigDecimal) reversal.get("voucher_amount");
							   tds = (BigDecimal) reversal.get("tds_amount");
							   bank = (String) reversal.get("bank");
							   mode = (String) reversal.get("payment_mode");
							   card = reversal.get("card_type") == null ? "" : (String) reversal.get("card_type");
							   referenceNo = (String) reversal.get("reference_no");
							   username = (String)reversal.get("username");
						   }
						   amount = amount.add(tds);
					   }
				   }else

				   /* PAYMENT DETAILS FOR MISCELLANEOUS */

				   if (paymentType.equals("C")){
					   List<BasicDynaBean> miscVoucherDetails =
						   PaymentsDAO.getMiscPaymentVoucherDetails(voucherNo);
					   params.put("voucherDetails", miscVoucherDetails);
					   if (!miscVoucherDetails.isEmpty()){
						   for (BasicDynaBean miscPay : miscVoucherDetails){
							   amount = (BigDecimal)miscPay.get("totalamount");
							   bank = (String) miscPay.get("bank");
							   mode = (String) miscPay.get("payment_mode");
							   card = miscPay.get("card_type") == null ? "" : (String) miscPay.get("card_type");
							   referenceNo = (String) miscPay.get("reference_no");
							   username = (String)miscPay.get("username");
						   }
					   }
				   }else

				   /* PAYMENT DETAILS FOR DOCTOR  */

				   if ((paymentType.equals("D"))
							   || (paymentType.equals("R")) || (paymentType.equals("P"))
							   || (paymentType.equals("F")) || (paymentType.equals("O"))) {
					   List<BasicDynaBean> voucherDetails = PaymentsDAO.getPaymentVoucherBreakup(voucherNo);
					   params.put("voucherDetails", voucherDetails);
					   if (!voucherDetails.isEmpty()){
						   for (BasicDynaBean voucher : voucherDetails){
							   amount = (BigDecimal)voucher.get("voucher_amount");
							   bank = (String) voucher.get("bank");
							   mode = (String) voucher.get("payment_mode");
							   card = voucher.get("card_type") == null ? "" : (String) voucher.get("card_type");
							   referenceNo = (String) voucher.get("reference_no");
							   username = (String)voucher.get("username");
						   }
					   }
				   }else

				   /* PAYMENT DETAILS FOR SUPPLIER */

				   if (paymentType.equals("S")) {
					   List<BasicDynaBean> supplierDetails = PaymentsDAO.getSupplierPaymentVoucher(voucherNo);
					   params.put("voucherDetails" , supplierDetails);

					   if (!supplierDetails.isEmpty()){
						   for (BasicDynaBean supplier : supplierDetails){
							   amount = (BigDecimal)supplier.get("totalamount");
							   bank = (String) supplier.get("bank");
							   mode = (String) supplier.get("payment_mode");
							   card = supplier.get("card_type") == null ? "" : (String) supplier.get("card_type");
							   referenceNo = (String) supplier.get("reference_no");
							   username = (String)supplier.get("username");
						   }
					   }
				   }
			   }

			   params.put("voucherTotalAmount", NumberToWordFormat.wordFormat().toRupeesPaise(amount));
			   params.put("bankName" ,bank);
			   params.put("paymentMode", mode);
			   params.put("cardType", card);
			   params.put("referenceNo", referenceNo);
			   params.put("voucherType", voucherType);
			   params.put("printType", printType);
			   params.put("paymentType", paymentType);
			   params.put("username", username);

			   Template t = null;
			   PrintTemplatesDAO templateDAO = new PrintTemplatesDAO();
			   String templateContent = templateDAO.getCustomizedTemplate(PrintTemplate.Voucher);
			   if (templateContent == null || templateContent.equals(""))
				   t = AppInit.getFmConfig().getTemplate(PrintTemplate.Voucher.getFtlName()+ ".ftl");
			   else {
				   StringReader reader = new StringReader(templateContent);
				   t = new Template("PaymentVoucherPrint.ftl", reader, AppInit.getFmConfig());
			   }

			   StringWriter writer = new StringWriter();
			   t.process(params, writer);
			   String textContent = writer.toString();
			   HtmlConverter hc = new HtmlConverter();

			   PrintPageOptions opts = new PrintPageOptions(printPref);
			   OutputStream os = null;
			   try{
				   if (printPref.get("print_mode").equals("T")) {
					   String textReport =new String(hc.getText(textContent, "Voucher Details", printPref,
								   true, true));
					   request.setAttribute("textReport", textReport);
					   request.setAttribute("textColumns", printPref.get("text_mode_column"));
						request.setAttribute("printerType", "DMP");
					   return mapping.findForward("textPrintApplet");
				   } else {
					   os = response.getOutputStream();
					   response.setContentType("application/pdf");
					   hc.writePdf(os, textContent, "Voucher Details", printPref, false, false, true,
							   false, true, false);
					   return null;
				   }
			   }
			   finally{
				   	if (os != null)
				   os.close();
			   }
	}

	public ActionForward printVoucherBreakup(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{

		HashMap params = new HashMap();
		params.put("voucherNo",request.getParameter("voucherno"));
		ReportPrinter.printPdfStream(request,response,"PaymentVoucherBreakup",params);
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward voucherDescription(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");
		String query = req.getParameter("query");

		List deptList = PaymentsDAO.getDescription(query,0);
		Map map = new HashMap();
		map.put("result", ConversionUtils.copyListDynaBeansToMap(deptList));
		js.deepSerialize(map, res.getWriter());
		res.flushBuffer();
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward voucherCategory(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		JSONSerializer js = new JSONSerializer().exclude("class");
		String query = req.getParameter("query");

		List deptList = PaymentsDAO.getCategory(query,0);
		Map map = new HashMap();
		map.put("result", ConversionUtils.copyListDynaBeansToMap(deptList));
		js.deepSerialize(map, res.getWriter());
		res.flushBuffer();
		return null;
	}

}
