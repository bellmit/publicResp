package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.DepositsDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class PharmacyItemsCreditBillAction extends BaseAction{

	static Logger log = LoggerFactory.getLogger(PharmacyItemsCreditBillAction.class);
	static GenericDAO billDao = new GenericDAO("bill");
	static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
	static PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();

	@IgnoreConfidentialFilters
	public  ActionForward getCreditBillsList(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		Map map= getParameterMap(request);
		PagedList list = PharmacyItemsCreditBillDAO.getCreditBillList(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		request.setAttribute("screenId", mapping.getProperty("action_id"));
		return mapping.findForward("getphcreditBilldb");
	}

	public ActionForward getBillItemList(ActionMapping am,
			ActionForm form, HttpServletRequest request, HttpServletResponse res)
			throws SQLException, ParseException {

		String billNo=request.getParameter("billno");
		String mrno=request.getParameter("mr_no");
		String visitId = request.getParameter("visitid");
		BasicDynaBean statusBean = billDao.findByKey("bill_no", billNo);
		String status = (String) statusBean.get("status");
		BillBO bo = new BillBO();
		DepositsDAO depositDao = new DepositsDAO();
		List<BasicDynaBean> l = PharmacyItemsCreditBillDAO.getItemsList(billNo);
		request.setAttribute("billIems", l);
		request.setAttribute("billNo", billNo);
		request.setAttribute("visitId", visitId);
		request.setAttribute("mr_no", mrno);
		request.setAttribute("status", status);
		BillDetails billDetails = bo.getBillDetails(billNo);
		Map patient = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
		request.setAttribute("doctor", patient.get("doctor_name"));
		request.setAttribute("billDetails", billDetails);

		/* Get all pharmacy deposit details */
		GenericPreferencesDTO prefs = GenericPreferencesDAO.getGenericPreferences();
		if("P".equals(prefs.getDeposit_avalibility()) || "B".equals(prefs.getDeposit_avalibility())) {
		    BasicDynaBean depositDetails=depositDao.getPatientDepositDetails(mrno, true);
			if (depositDetails != null) {
				request.setAttribute("total_deposits",depositDetails.get("total_deposits"));
				request.setAttribute("total_deposit_set_off", depositDetails.get("total_deposit_set_off"));
				request.setAttribute("total_balance", depositDetails.get("total_balance"));
			}
		} else {
		  request.setAttribute("total_deposits", null);
      request.setAttribute("total_deposit_set_off", null);
		}
		BasicDynaBean bean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);
		request.setAttribute("bean", bean);
		request.setAttribute("screenId", am.getProperty("screen_id"));
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new CardTypeMasterDAO().listAll(null,"status","A",null))));
		request.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(new PaymentModeMasterDAO().listAll())));
		request.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());
		return am.findForward("getphcreditBillscreen");
	}


	public ActionForward getBillItemDetails(ActionMapping am,
			ActionForm form, HttpServletRequest request, HttpServletResponse res)
			throws SQLException,  IOException, ParseException {
			// TODO: remove floats
		float phRecpAmt = 0; float hospRecpAmt = 0,phRefAmt = 0, phCredit = 0, hospCredit = 0, hospAmt = 0;
		HashMap map = new HashMap();
		JSONSerializer js = new JSONSerializer().exclude("class");
		res.setContentType("text/javascript");
		String billNo=request.getParameter("billno");

		BillBO bo = new BillBO();
		List<BasicDynaBean> l = PharmacyItemsCreditBillDAO.getItemsList(billNo);
		BillDetails billDetails = bo.getBillDetails(billNo);
		if (billDetails == null) {
			// no credit bills for this patient (yet)
			map.put("pharmabills", null);
			res.getWriter().write(js.serialize(map));
			return null;
		}
		hospRecpAmt = billDetails.getBill().getTotalReceipts().floatValue() ;
		hospAmt = billDetails.getBill().getTotalAmount().floatValue();
		List receipts = billDetails.getReceipts();
		List refunds = billDetails.getRefunds();
		/** Get the total receipt amount*/
		Iterator i = receipts.iterator();
		while (i.hasNext()){
			Receipt recp = (Receipt)i.next();
			if ((null != recp.getCounterType()) && (recp.getCounterType().equals("P"))){
				phRecpAmt = phRecpAmt + recp.getAmount().floatValue();
			}

		}
		/** Get the total refund amount*/
		Iterator j = refunds.iterator();
		while(j.hasNext()){
			Receipt recp = (Receipt)j.next();
			if ((null != recp.getCounterType()) && (recp.getCounterType().equals("P"))){
				phRefAmt = phRefAmt + recp.getAmount().floatValue();
			}

		}
		BasicDynaBean billBean = BillDAO.getBillBean(billNo);
		/** Calculate existing receipts/total credits*/
		phCredit = phRecpAmt + phRefAmt;
		hospCredit = hospRecpAmt + ((BigDecimal)billBean.get("total_claim")).floatValue();
		if (billDetails.getBill().getDepositSetOff() != null)
			hospCredit += billDetails.getBill().getDepositSetOff().floatValue();
		List<BasicDynaBean> hosp = PharmacyItemsCreditBillDAO.getItemsListForHosp(billNo);
		map.put("pharmabills", ConversionUtils.listBeanToListMap(l));
		map.put("pharmCredit", new Float(phCredit));
		map.put("hospCredit", new Float(hospCredit));
		map.put("hospRecpAmt", hospRecpAmt);
		map.put("phRecpAmt", phRecpAmt);
		map.put("hospAmt", hospAmt);
		map.put("hospdetails", ConversionUtils.listBeanToListMap(hosp));
		res.getWriter().write(js.deepSerialize(map));
		return null;
	}


	public ActionForward collectCreditBillPayments(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse res)
			throws SQLException, ParseException {

		ActionRedirect redirect = new ActionRedirect("PhItemsCreditBill.do?_method=getBillItemList");
		AbstractPaymentDetails ppdImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.PHARMACY_PAYMENT);
		List<Receipt> receiptList = null;
		Map requestParams = request.getParameterMap();
		Map printParamMap = null;
		FlashScope flash = FlashScope.getScope(request);

		String billNo = request.getParameter("billNo");
		String visitId = request.getParameter("customerId");
		String doctor = request.getParameter("doctor");
		String mr_no = request.getParameter("mr_no");
		String printType = request.getParameter("printerType");
    
		HttpSession session = request.getSession();
        String userName = (String) session.getAttribute("userid");

		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BillDAO bdao = new BillDAO(con);
			Bill bill = bdao.getBill(billNo);

			receiptList = ppdImpl.processReceiptParams(requestParams);

			if (receiptList != null && receiptList.size()>0) {
				success = ppdImpl.createReceipts(con, receiptList, bill, bill.getVisitType(), bill.getStatus());
				if (success) {

	        // Update the bill total amount.
	        allocationService.updateBillTotal(bill.getBillNo());
			        Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
			        // Call the allocation method
			        allocationService.allocate(bill.getBillNo(), centerId);
					printParamMap = new HashMap();

					printParamMap.put("billNo", billNo);
					printParamMap.put("customerId", visitId);
					printParamMap.put("doctor", doctor);
					printParamMap.put("printerTypeStr", printType);
					printParamMap.put("transaction", "payment");
					printParamMap.put("creditPatientType", "HOSPITAL");

					List<String> printURLs = ppdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
					request.getSession(false).setAttribute("printURLs", printURLs);
				}
			}

			/** Update the deposit setoff amounts if any*/
			success = success && PharmacyItemsCreditBillDAO.updateDepositSetOffInBill(con, billNo, request.getParameter("depositsetoff"));

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

		redirect.addParameter("billno", billNo);
		redirect.addParameter("visitid", visitId);
		redirect.addParameter("mr_no", mr_no);

		if (!success){
			flash.put("error", "Transaction Failure");
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}
}