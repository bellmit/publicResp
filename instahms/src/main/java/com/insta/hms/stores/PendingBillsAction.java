package com.insta.hms.stores;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillActivityChargeDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DepositsDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.RewardPointsDAO;
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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PendingBillsAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(PendingBillsAction.class);
	static RetailCustomerDAO rcDao = new RetailCustomerDAO();
	static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
    static PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
    
    private static final GenericDAO billDAO = new GenericDAO("bill");


	/** Screen:Pending Bill Lists, Method: getPendingSaleBillsList, Action Id: pharma_sale_duplicate_bill,
	 *  Path: /pages/pharmacy/salebill.
	 */
	@IgnoreConfidentialFilters
	public  ActionForward getPendingSaleBillsList(ActionMapping mapping,ActionForm fm,
			HttpServletRequest request,HttpServletResponse response) throws SQLException,Exception{

		Map map= getParameterMap(request);
		PagedList list = StoresDashBoardsDAO.getPendingSaleList(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList", list);
		return mapping.findForward("getPendingBillScreen");
	}

	public ActionForward getDetailSaleList(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse res)
			throws SQLException, ParseException {

		String saleId = request.getParameter("saleId");
		String mrno = request.getParameter("mrno");
		String saleType=request.getParameter("saleType");
		BasicDynaBean saleBean = MedicineSalesDAO.getSaleIdBillNo(saleId);
		BillBO bo = new BillBO();
		DepositsDAO dao=new DepositsDAO();
		BillDetails billDetails = bo.getBillDetails(saleBean.get("bill_no").toString());
		JSONSerializer js = new JSONSerializer().exclude("class");
		
		String visitType = billDetails.getBill().getVisitType();
		String visitId = billDetails.getBill().getVisitId();

		if (visitType.equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
			request.setAttribute("rc", rcDao.getRetailCustomer(visitId).getMap());
		}
		request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new CardTypeMasterDAO().listAll(null,"status","A",null))));
		
		BasicDynaBean pref =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);
		if(saleType.equals("S")){
			String depositAvailFor=GenericPreferencesDAO.getGenericPreferences().getDeposit_avalibility();
			BasicDynaBean depositDetails=dao.getPatientDepositDetails(mrno,true);
			request.setAttribute("depositAmt", depositDetails);
		}else{
		 	String retBillNo = saleBean.get("return_bill_no") != null ? (String) saleBean.get("return_bill_no") : null;
		    BasicDynaBean returnSaleBean = MedicineSalesDAO.getSaleIdBillNo(retBillNo);
		    if(returnSaleBean!=null){
				BillDetails billObj = bo.getBillDetails(returnSaleBean.get("bill_no").toString());
				if(billObj.getBill().getDepositSetOff().compareTo(BigDecimal.ZERO)>0)
			    request.setAttribute("depositAmt", billObj.getBill().getDepositSetOff());
			}
		}
		
		BasicDynaBean billbean = billDAO.findByKey("bill_no", (saleBean.get("bill_no").toString()));
		BigDecimal totalReciptsAmt =  (BigDecimal) billbean.get("total_receipts") ;
		request.setAttribute("totalReciepts", totalReciptsAmt);
		HttpSession session	= request.getSession();
		boolean mod_reward_points	= (Boolean)session.getAttribute("mod_reward_points");
		GenericPreferencesDTO prefs = GenericPreferencesDAO.getGenericPreferences();

		if (mod_reward_points) {
			BasicDynaBean rewardPointsDetails = RewardPointsDAO.getPatientRewardPointsDetails(mrno);
			if (rewardPointsDetails != null) {
				int total_points_earned = (Integer)rewardPointsDetails.get("total_points_earned");
				int total_points_redeemed = (Integer)rewardPointsDetails.get("total_points_redeemed");
				int total_open_points_redeemed = (Integer)rewardPointsDetails.get("total_open_points_redeemed");
				int total_points = total_points_earned - (total_points_redeemed + total_open_points_redeemed);
				BigDecimal redemptionRate = (prefs.getPoints_redemption_rate() != null)
												? (BigDecimal)prefs.getPoints_redemption_rate() : BigDecimal.ZERO;
				/* BigDecimal total_points_amount = redemptionRate.multiply(new BigDecimal(total_points)); */

				List<ChargeDTO> charges = billDetails.getCharges();
				BigDecimal totalEligibleAmtToRedeem = BigDecimal.ZERO;
				for (ChargeDTO charge : charges) {
					String eligibleToRedeem = charge.getEligible_to_redeem_points();
					BigDecimal redemptionCapPer = (charge.getRedemption_cap_percent() != null)
											? charge.getRedemption_cap_percent() : BigDecimal.ZERO;
					if (eligibleToRedeem.equals("Y")) {
						BigDecimal eligibleAmt = ConversionUtils.setScale(
								charge.getAmount().multiply(redemptionCapPer).divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));

						eligibleAmt = eligibleAmt.subtract(eligibleAmt.remainder(redemptionRate));
						totalEligibleAmtToRedeem = totalEligibleAmtToRedeem.add(eligibleAmt);
					}
				}
				request.setAttribute("total_points", total_points);
				request.setAttribute("total_points_amount", totalEligibleAmtToRedeem);
			}
		}

		request.setAttribute("saleDetails", MedicineSalesDAO.getSalesList1(saleId));
		request.setAttribute("saleId", saleId);
		request.setAttribute("saleType", saleType);
		request.setAttribute("billNo", saleBean.get("bill_no").toString());
		request.setAttribute("bean", pref);
		request.setAttribute("screenId", am.getProperty("action_id"));
		request.setAttribute("billDetails", billDetails);
		request.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(new PaymentModeMasterDAO().listAll())));
		request.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());

		return am.findForward("getsaleListScreen");
	}

	public ActionForward collectSalePayments(ActionMapping am, ActionForm form,
			HttpServletRequest request, HttpServletResponse res)
		throws SQLException,IOException, ParseException {

		PendingBillsForm pForm = (PendingBillsForm) form;
		HttpSession session = request.getSession();
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = new ActionRedirect("pendingSalesBill.do?_method=getPendingSaleBillsList&sortOrder=mr_no");

		List<Receipt> receiptList = null;
		Map requestParams = request.getParameterMap();
		Map printParamMap = null;
		AbstractPaymentDetails ppdImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.PHARMACY_PAYMENT);

		String userName = (String) session.getAttribute("userid");
		java.sql.Timestamp payDateTime = DateUtil.getCurrentTimestamp();
		if (pForm.getPayDate() != null && pForm.getPayTime() != null)
			payDateTime = DateUtil.parseTimestamp(pForm.getPayDate(), pForm.getPayTime());

		BigDecimal redemptionRate = GenericPreferencesDAO.getGenericPreferences().getPoints_redemption_rate();
		redemptionRate = redemptionRate == null ? BigDecimal.ZERO : redemptionRate;

		Connection con = null;
		boolean success = true;

		Map payAmtMap = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BillDAO bdao = new BillDAO(con);
			Bill bill = bdao.getBill(pForm.getBillNo());

			receiptList = ppdImpl.processReceiptParams(requestParams);

			String billStatus = Bill.BILL_STATUS_CLOSED;
			String dischargeStatus=Bill.BILL_DISCHARGE_OK;

			// Create receipts
			if (receiptList != null && receiptList.size()>0) {
				success = ppdImpl.createReceipts(con, receiptList, bill, bill.getVisitType(), bill.getStatus());
			}
			
			// Get updated bill deposits and redeem points etc.
			bill = bdao.getBill(pForm.getBillNo());

			// close the bill
			if (success) {
				if (bill.getBillType().equals("P") && bill.getIs_tpa())
					billStatus = Bill.BILL_STATUS_FINALIZED;

				bill.setFinalizedDate(payDateTime);
				bill.setLastFinalizedAt(DateUtil.getCurrentTimestamp());

				bill.setStatus(billStatus);
				bill.setDischarge(dischargeStatus);
				bill.setOkToDischarge(dischargeStatus);
				bill.setClosedDate(payDateTime);
				bill.setClosedBy(userName);

				bill.setUserName(userName);
				bill.setPrimaryClaimStatus("O");
				bill.setInsuranceDeduction(new BigDecimal(0));
				bill.setPaymentStatus(Bill.BILL_PAYMENT_PAID);
				
			}

            BasicDynaBean billbean = billDAO.findByKey("bill_no", pForm.getBillNo());
            String bStatus = "";
            if (billbean != null && billbean.get("status").equals("C")) {
            	success = false;
            	bStatus = "C";
            }

            success = success && bdao.updateBill(bill);

            if (success) {
            	if (bill.getBillType().equals("P") && !bill.getIs_tpa()) {
            		BillActivityChargeDAO billActivityDAO = new BillActivityChargeDAO(con);
            		ChargeDTO chargeDto = billActivityDAO.getCharge("PHS", pForm.getSaleId());
            		if (chargeDto != null) {
	            		String chargeId = chargeDto.getChargeId();
	            		GenericDAO chargeDao = new GenericDAO("bill_charge");
	            		if (chargeId != null && !chargeId.equals("")) {
	            			BasicDynaBean chargebean = chargeDao.findByKey("charge_id", chargeId);
	                     	if (chargebean != null) {
	                     		chargebean.set("redeemed_points", bill.getRewardPointsRedeemed());
	                     		int result = chargeDao.updateWithName(con, chargebean.getMap(), "charge_id");
	                     		success = success && (result > 0);
	                     	}
	            		}
            		}
            	}
			}

			if (success) {
			  
			  // Update the bill total amount.
			  allocationService.updateBillTotal(bill.getBillNo());
                Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");
			    // Call the Allocation method.
                allocationService.allocate(bill.getBillNo(), centerId);
				printParamMap = new HashMap();
				printParamMap.put("printerTypeStr", request.getParameter("printerId"));
				printParamMap.put("saleId", pForm.getSaleId());

				List<String> printURLs = ppdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
				request.getSession(false).setAttribute("printURLs", printURLs);
			} else {
				flash.put("error", bStatus.equals("C") ? "Bill is already closed" : "Transaction Failure");
			}

		} catch (SQLException e) {
			flash.put("error","Transaction Failure");
		} finally {
			DataBaseUtil.commitClose(con, success);
      if (success) {
        BasicDynaBean billbean = billDAO.findByKey("bill_no", pForm.getBillNo());
        if (billbean != null) {
          List<BasicDynaBean> billBeanList = new ArrayList<>();
          billBeanList.add(billbean);
          accountingJobScheduler.scheduleAccountingForSales(billBeanList);
        }
      }
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
		return redirect;
	}
}
