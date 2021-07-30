package com.insta.hms.stores;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.billing.paymentdetails.PharmacyPaymentDetailsImpl;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;

import flexjson.JSONSerializer;
import freemarker.template.Template;

public class RetailPendingSaleAction extends BaseAction {

    static Logger log = LoggerFactory.getLogger(RetailPendingSaleAction.class);

	static RetailCustomerDAO rcDao = new RetailCustomerDAO();
  static GenericDAO billDao = new GenericDAO("bill");
  static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  static PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
  private static final GenericDAO billChargeDAO = new GenericDAO("bill_charge");

	@IgnoreConfidentialFilters
  public ActionForward getRetailPendingSaleBillsList(ActionMapping mapping,ActionForm fm,
            HttpServletRequest request,HttpServletResponse response) throws Exception {

		Map map= getParameterMap(request);
		PagedList list = StoresDashBoardsDAO.getRetailPendingSaleList(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		return mapping.findForward("getRetailPendingBillScreen");
	}

	public ActionForward getRetailPendingSaleList(ActionMapping am,
			ActionForm form, HttpServletRequest request, HttpServletResponse res)
			throws SQLException, ParseException {

		String billNo=request.getParameter("billno");
		String customerId=request.getParameter("customerid");

		BillBO bo = new BillBO();
		request.setAttribute("retailcustomer", rcDao.getRetailCustomer(customerId).getMap());
		List<BasicDynaBean> l = (ArrayList<BasicDynaBean>)MedicineSalesDAO.getSaleListForPendingBill(billNo);
		Map<String,Object> identifiers = new HashMap<String, Object>();
		identifiers.put("bill_no", billNo);
		identifiers.put("charge_head", "ROF");
		BasicDynaBean billChargeBean = billChargeDAO.findByKey(identifiers);
		request.setAttribute("billChargeBean", billChargeBean);
		request.setAttribute("saleDetails", l);
		request.setAttribute("billNo", billNo);
		request.setAttribute("customerId", customerId);
		BillDetails billDetails = bo.getBillDetails(billNo);
		request.setAttribute("billDetails", billDetails);
		request.setAttribute("screenId", am.getProperty("screen_id"));
		request.setAttribute("doctor", (l != null && l.size() > 0)? l.get(0).get("doctor_name") : "");
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new CardTypeMasterDAO().listAll(null,"status","A",null))));
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		request.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(new PaymentModeMasterDAO().listAll())));
		request.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());
		return am.findForward("getRetailSaleScreen");
	}


	public ActionForward collectSaleRetailPayments(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse res)
			throws SQLException, ParseException,IOException {

		RetailPendingCreditForm rForm = (RetailPendingCreditForm) form;
		AbstractPaymentDetails ppdImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.PHARMACY_PAYMENT);
		ActionRedirect redirect = null;
		List<Receipt> receiptList = null;
		Map requestParams = request.getParameterMap();
		Map printParamMap = null;
		FlashScope flash = FlashScope.getScope(request);

		HttpSession session = request.getSession();
		String userName = (String) session.getAttribute("userid");

		String customerId = request.getParameter("customerId");
		String doctor = request.getParameter("doctor");
		String printType = request.getParameter("printerType");
		boolean close = rForm.isClose();
		String billNo = rForm.getBillNo();
		String roundOff = request.getParameter("c_round_off");
		String amount = request.getParameter("round_off");
		amount = (amount == null || amount.isEmpty()) ? "0" : amount;
		roundOff = (roundOff == null || roundOff.isEmpty()) ? null : roundOff;
		String chargeId = request.getParameter("chargeId");
		chargeId =(chargeId == null || chargeId.isEmpty()) ? null : chargeId;
		BasicDynaBean insertBean = null;
		Map columndata = new HashMap();
		Map keys = new HashMap();
		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BillDAO bdao = new BillDAO(con);

			Bill bill = bdao.getBill(billNo);
			ChargeDAO cdao = new ChargeDAO(con);
			receiptList = ppdImpl.processReceiptParams(requestParams);

			if(roundOff != null) {
				if(chargeId == null) {
					chargeId = cdao.getNextChargeId();
					insertBean = billChargeDAO.getBean();
					insertBean.set("charge_id", chargeId);
					insertBean.set("bill_no", billNo);
					insertBean.set("charge_group", "DIS");
					insertBean.set("charge_head", "ROF");
					insertBean.set("act_rate", new BigDecimal(amount));
					insertBean.set("act_quantity",BigDecimal.ONE);
					insertBean.set("amount",new BigDecimal(amount));
					insertBean.set("hasactivity",false);
					insertBean.set("conducting_doc_mandatory", "N");
					insertBean.set("consultation_type_id", 0);
					insertBean.set("insurance_category_id", -1);
					insertBean.set("allow_discount",true);
					insertBean.set("username",userName);
					insertBean.set("posted_date", DateUtil.getCurrentTimestamp());
					insertBean.set("mod_time", DateUtil.getCurrentTimestamp());
					insertBean.set("act_description", "Pharmacy retail sales bill");
					success = billChargeDAO.insert(con, insertBean);

				}
			}

			if(success && chargeId != null) {
				keys.put("charge_id", chargeId);
				columndata.put("username", userName);
				columndata.put("mod_time", DateUtil.getCurrentTimestamp());
				columndata.put("amount", new BigDecimal(amount));
				columndata.put("act_rate", new BigDecimal(amount));
				if(roundOff == null)
					columndata.put("status", "X");
				else
					columndata.put("status", "A");
				success = billChargeDAO.update(con, columndata, keys) > 0;
			}

			if (receiptList != null && receiptList.size()>0) {
				success = ppdImpl.createReceipts(con, receiptList, bill, bill.getVisitType(), bill.getStatus());
				if (success) {
					printParamMap = new HashMap();

					printParamMap.put("billNo", billNo);
					printParamMap.put("customerId", customerId);
					printParamMap.put("doctor", doctor);
					printParamMap.put("printerTypeStr", printType);
					printParamMap.put("transaction", "payment");
					printParamMap.put("creditPatientType", "RETAIL");

	                // Update the bill total amount.
	                allocationService.updateBillTotal(bill.getBillNo());
	                Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
                    // Call the allocation method
                    allocationService.allocate(bill.getBillNo(), centerId);
					List<String> printURLs = ppdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
					request.getSession(false).setAttribute("printURLs", printURLs);
				}
			}

			if (close) {
				if (success) {
					String billStatus = bill.BILL_STATUS_CLOSED;
					String dischargeStatus=bill.BILL_DISCHARGE_OK;
					String paymentStatus = bill.BILL_PAYMENT_PAID;
					String finalizedBy = bill.getFinalizedBy();

					//	Set the finalized by when the status is closed
					// (And) the Bill is a Credit bill or Bill now with TPA (And) the finalized by is empty.
					if ((bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)
							|| (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) && bill.getIs_tpa()))
							&& (finalizedBy == null || finalizedBy.equals("")))
						finalizedBy = userName;

					java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());

					bill.setBillRemarks(rForm.getBillRemarks());
					success = success && bdao.updateBill(bill);
					if (success)
					success = success && bdao.updateBillStatus(
					    userName, billNo, billStatus, paymentStatus, dischargeStatus,
					    now, now, userName, finalizedBy, now);
				}
			} else {
				
				if ( bill != null ){
					bill.setModTime(new java.util.Date());
					success = success && bdao.updateBill(bill);
				}
			}

		}finally {
			DataBaseUtil.commitClose(con, success);

			if (success) {
        BasicDynaBean billbean = billDao.findByKey("bill_no", billNo);
        if (billbean != null) {
          List<BasicDynaBean> billBeanList = new ArrayList<>();
          billBeanList.add(billbean);
          accountingJobScheduler.scheduleAccountingForSales(billBeanList);
        }
      }
		}

		// If bill is closed redirect to Pending sales bill list.
		if (close && success) {
			redirect = new ActionRedirect("RetailpendingSalesBill.do?_method=getRetailPendingSaleBillsList&sortOrder=bill_no&sortReverse=true");

		}else {
			redirect = new ActionRedirect("RetailpendingSalesBill.do?_method=getRetailPendingSaleList");
			redirect.addParameter("billno", billNo);
			redirect.addParameter("customerid", customerId);
		}

		if (!success){
			flash.put("error", "Transaction Failure");
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public static final Integer PRINTER_ID=3;

	/*
	 * Common method for credit bill payment receipt print.
	 * Allowed values for creditPatientType are HOSPITAL, RETAIL
	 */

	@IgnoreConfidentialFilters
	public ActionForward getCreditReceiptPrint(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		String receiptNo = req.getParameter("receiptNo");
		String billNo = req.getParameter("billNo");
		String customerId=req.getParameter("customerId");
		String payType=req.getParameter("payType");
		String creditPatientType = req.getParameter("creditPatientType");
		String printTypeStr = req.getParameter("printerType");
		int printType = (printTypeStr.equals("null") || printTypeStr == null) ? PRINTER_ID : Integer.parseInt(printTypeStr);

		Map paramMap = new HashMap();
		BillBO bo = new BillBO();

		RetailCustomerDAO rcDao = new RetailCustomerDAO();
		BillDetails billDetails = bo.getBillDetails(billNo);
		paramMap.put("billDetails", billDetails);
		paramMap.put("receiptNo", receiptNo);
		paramMap.put("payType", payType);

		if (creditPatientType.equals((PharmacyPaymentDetailsImpl.CreditPaymentType.HOSPITAL).toString())) {
			paramMap.put("cust", PharmacyItemsCreditBillDAO.getPatientVisitDetailsBean(customerId));
		}else {
			paramMap.put("cust", rcDao.getRetailCustomer(customerId).getMap());
		}
		paramMap.put("doctor", req.getParameter("doctor"));
		
		/** Taxation Details */
		List<BasicDynaBean> itemsSaleTaxDetails = MedicineSalesDAO.getItemsSaleTaxDetailsByBillNo(billNo);
		paramMap.put("itemsSaleTaxDetails",  itemsSaleTaxDetails);
		
		List<BasicDynaBean> itemsReturnTaxDetails = MedicineSalesDAO.getItemsReturnTaxDetailsByBillNo(billNo);
		paramMap.put("itemsReturnTaxDetails",  itemsReturnTaxDetails);

		// Get the print mode of the print type
		BasicDynaBean pref =  PrintConfigurationsDAO.getPageOptions(
				PrintConfigurationsDAO.PRINT_TYPE_PHARMACY,printType);

		if (pref.get("print_mode").equals("P")) {
			paramMap.put("printMode","Y");
		}else {
			paramMap.put("printMode","N");
		}

		// Get the default print settings
		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
				PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);

		PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
		String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.RetPhar);

		Template t = null;
		if (templateContent == null || templateContent.equals("")) {
			t = AppInit.getFmConfig().getTemplate(PrintTemplate.RetPhar.getFtlName() + ".ftl");
		} else {
			StringReader reader = new StringReader(templateContent);
			t = new Template("retailCreditPaymentTemplate.ftl", reader, AppInit.getFmConfig());
		}

		StringWriter writer = new StringWriter();
		t.process(paramMap,writer);
		String htmlContent = writer.toString();

		HtmlConverter hc = new HtmlConverter();

		if (pref.get("print_mode").equals("P")) {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/pdf");
			try {
				if (creditPatientType.equals((PharmacyPaymentDetailsImpl.CreditPaymentType.HOSPITAL).toString())) {
					hc.writePdf(os, htmlContent, "Pharmacy Credit Bill Payment ", printprefs, false, false, true, true, true, false);
				}else {
					hc.writePdf(os, htmlContent, "Retail Credit Pharmacy Payment Bill", printprefs, false, false, true, true, true, false);
				}
			} catch (Exception e) {
				res.reset();
				log.error("Original Template:");
				log.error(templateContent);
				log.error("Generated HTML content:");
				log.error(htmlContent);
				throw(e);
			}
			os.close();

			return null;
		} else {
			//text mode
			String textReport = null;
			if (creditPatientType.equals((PharmacyPaymentDetailsImpl.CreditPaymentType.HOSPITAL).toString())) {
				textReport = new String(hc.getText(htmlContent, "Pharmacy Credit Bill Payment", printprefs, true, true));
			}else {
				textReport = new String(hc.getText(htmlContent, "Retail Credit Pharmacy Payment Bill", printprefs, true, true));
			}

			req.setAttribute("textReport", textReport);
			req.setAttribute("textColumns", printprefs.get("text_mode_column"));
			req.setAttribute("printerType", "DMP");
			return am.findForward("textPrintApplet");
		}
	}

	public ActionForward printRetailCreditBill(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		Map paramMap = new HashMap();
		RetailCustomerDAO rDao = new RetailCustomerDAO();
		String billNo = req.getParameter("billNo");
		String customerId=req.getParameter("customerId");
		String amount = req.getParameter("round_off");
		amount = (amount == null || amount.isEmpty()) ? "0" : amount;

		List<BasicDynaBean> saleDetails = MedicineSalesDAO.getSalesList(billNo);
		BillBO bo = new BillBO();
		BillDetails billDetails = bo.getBillDetails(billNo);
		paramMap.put("bill", billDetails.getBill());
		paramMap.put("customer", rDao.getRetailCustomer(customerId));
		paramMap.put("items",  saleDetails);

		/*
		 * Calculate the VAT for each rate of VAT, also check if discounts are being used
		 */
		HashMap<String, BigDecimal> vatDetails = new HashMap<String, BigDecimal>();
		boolean hasDiscounts = false;
		for (BasicDynaBean b: saleDetails) {
			String rate = b.get("tax_rate").toString();
			BigDecimal taxAmt = (BigDecimal) b.get("tax");
			BigDecimal totalTax = vatDetails.get(rate);
			if (totalTax == null) {
				vatDetails.put(rate, taxAmt);
			} else {
				vatDetails.put(rate, taxAmt.add(totalTax));
			}

			BigDecimal discount = (BigDecimal) b.get("discount");
			if (discount.compareTo(BigDecimal.ZERO) != 0)
				hasDiscounts = true;
		}
		paramMap.put("vatDetails",  vatDetails);
		paramMap.put("hasDiscounts",  hasDiscounts);

		paramMap.put("doctorName", saleDetails.get(0).get("doctor_name"));
		paramMap.put("duplicate", Boolean.parseBoolean(req.getParameter("duplicate")));
		paramMap.put("round_off", new BigDecimal(amount));
		/** Taxation Details */
		List<BasicDynaBean> itemsSaleTaxDetails = MedicineSalesDAO.getItemsSaleTaxDetailsByBillNo(billNo);
		paramMap.put("itemsSaleTaxDetails",  itemsSaleTaxDetails);
		
		List<BasicDynaBean> itemsReturnTaxDetails = MedicineSalesDAO.getItemsReturnTaxDetailsByBillNo(billNo);
		paramMap.put("itemsReturnTaxDetails",  itemsReturnTaxDetails);
		
		boolean isDuplicate = Boolean.parseBoolean(req.getParameter("duplicate"));

		BasicDynaBean pref= null;
		int printerId =0;
		String  printerIdStr = req.getParameter("printerId");

		if ((printerIdStr != null) &&  !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}

		pref =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY,printerId);
		String templateContent = null;
		Template t = null;
		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions( PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);
		PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
		templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.RetPharBill);

		if (templateContent == null || templateContent.equals("")) {
			t = AppInit.getFmConfig().getTemplate(PrintTemplate.RetPharBill.getFtlName() + ".ftl");
		} else {
			StringReader reader = new StringReader(templateContent);
			t = new Template("RetailCreditBillTemplate.ftl", reader, AppInit.getFmConfig());
		}

		StringWriter writer = new StringWriter();
		t.process(paramMap,writer);
		String htmlContent = writer.toString();

		HtmlConverter hc = new HtmlConverter();
		if (pref.get("print_mode").equals("P")) {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/pdf");
			try {
				hc.writePdf(os, htmlContent, "Retail Credit Pharmacy Payment Bill", printprefs, false, false, true, true, true, isDuplicate);
			} catch (Exception e) {
				res.reset();
				log.error("Original Template:");
				log.error(templateContent);
				log.error("Generated HTML content:");
				log.error(htmlContent);
				throw(e);
			}
			os.close();

			return null;
		} else {
			//text mode
			String textReport = new String(hc.getText(htmlContent, "Retail Credit Pharmacy Payment Bill", printprefs, true, true));
			req.setAttribute("textReport", textReport);
			req.setAttribute("textColumns", printprefs.get("text_mode_column"));
			req.setAttribute("printerType", "DMP");
			return am.findForward("textPrintApplet");
		}
	}
}
