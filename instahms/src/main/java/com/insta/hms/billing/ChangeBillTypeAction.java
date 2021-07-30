package com.insta.hms.billing;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;

public class ChangeBillTypeAction extends DispatchAction {

  AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");

	@IgnoreConfidentialFilters
	public ActionForward getChangeBillTypeScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws SQLException {
		String isNewUX = req.getParameter("isNewUX");
		req.setAttribute("isNewUX", isNewUX);
		return m.findForward("changebilltype");
	}

	/*
	 * Changing a bill now bill to bill later bill i.e Pre-paid to Credit bills.
	 */

	public ActionForward changeBillType(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, ParseException, IOException {

		String billNo = req.getParameter("billNo");
		String isNewUX = req.getParameter("isNewUX");
		String newBillNo = billNo;
		HttpSession session = req.getSession();
		Connection con = null;
		boolean success = false;
		String successMsg = null;
		String error = null;
		String oldPattern = null;
		String newPattern = null;
		String visitId = null;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BillDAO billDAO = new BillDAO(con);

			// if we are changing the bill type to credit, we need to check if there is any
			// other existing credit bill, and disallow if there is.
			Bill bill = billDAO.getBill(billNo);
			visitId = bill.getVisitId();
			bill.setUserName((String) session.getAttribute("userid"));
			if (!bill.getBillType().equals("P")) {
				error = "Bill is not a Hospital Bill Now Bill, cannot change type";
				req.setAttribute("error", error);
				return m.findForward("changebilltype");
			}

			String visitType = null;
			int centerId = 0;

			if (bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_INCOMING)) {
				visitType = Bill.BILL_VISIT_TYPE_INCOMING;
				BasicDynaBean incomingbean = new GenericDAO("incoming_sample_registration").findByKey("incoming_visit_id", visitId);
				centerId = (Integer)incomingbean.get("center_id");

			} else {
				visitType = VisitDetailsDAO.getVisitType(visitId);
				BasicDynaBean visitbean = VisitDetailsDAO.getVisitDetails(visitId);
				centerId = (Integer)visitbean.get("center_id");
			}

			oldPattern = BillDAO.getBillNoPattern("P", visitType, "N", centerId,bill.getIs_tpa(),false);
			newPattern = BillDAO.getBillNoPattern("C", visitType, "N", centerId,bill.getIs_tpa(),false);

			if ( oldPattern.equals(newPattern) ) {
				// just change the type and be done with it
				bill.setBillType(Bill.BILL_TYPE_CREDIT);
				billDAO.updateBill(bill);
				billDAO.setPrimaryBillConditional(bill.getBillNo());
				successMsg = "Bill type for " + billNo + " changed successfully";
			} else {

				// copy the bill over, and cancel the original bill
				Bill newBill = billDAO.getBill(billNo);
				newBill.setBillType(Bill.BILL_TYPE_CREDIT);
				// Copy deposits too to the new bill.
				newBill.setDepositSetOff(bill.getDepositSetOff());
				newBill.setIpDepositSetOff(bill.getIpDepositSetOff());
				// Set the deposits in previous bill as 0;
				bill.setDepositSetOff(BigDecimal.ZERO);
				bill.setIpDepositSetOff(BigDecimal.ZERO);

				Map msgMap = new BillBO().createNewBill(con, newBill, true);
	    		if (msgMap.get("error") != null && !msgMap.get("error").equals("")) {
	    			req.setAttribute("error", msgMap.get("error"));
					return m.findForward("changebilltype");
	    		}
	    		newBillNo = newBill.getBillNo();

				ChargeDAO cDao = new ChargeDAO(con);
				cDao.updateBillNo(billNo, newBillNo);
				BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
				billChgClaimDAO.updateBillClaimOnBillTypeChange(con,billNo, newBillNo);
				billChgClaimDAO.updateBillChargeClaimOnBillTypeChange(con,billNo, newBillNo);
				ReceiptRelatedDAO rDao = new ReceiptRelatedDAO(con);

				// Update bill receipts count as zero and receipt totals as zero
				rDao.updateBillReceiptTotals(billNo);

				rDao.updateReceiptBillNo(billNo, newBillNo);

				bill.setBillRemarks("Converted to Bill later, new bill no. : "+newBillNo);
				bill.setStatus("X");
				bill.setCancelReason("Converted to Bill later, new bill no. : "+newBillNo);
				billDAO.updateBill(bill);
				successMsg = "Bill type for " + billNo + " changed, new bill number is: " + newBillNo;
			}

			req.setAttribute("info", successMsg);
			success = true;

		} finally {
			DataBaseUtil.commitClose(con, success);
			// Update bill totals on new Bill
			allocationService.updateBillTotal(newBillNo);

			if (newBillNo != null && !(newBillNo).equals(""))
				BillDAO.resetTotalsOrReProcess(newBillNo);

			if (billNo != null && !(billNo).equals(""))
				BillDAO.resetTotalsOrReProcess(billNo);
		}

    /*
     * Schedule Accounting for bill On bill now to bill later if old pattern & new pattern are same
     * then at the time of finalizing the bill we will post both reversals and forward posts(We no
     * need to handle this case here). if both are different then old bill will get cancels and new
     * bill will be created with open state. So, We need to post only reversals for old bill. for
     * new bill at the time of finalization will posts
     */

    if (!(oldPattern != null && newPattern != null && oldPattern.equals(newPattern))) {
      if (billNo != null && !billNo.equals("")) {
        accountingJobScheduler.scheduleAccountingForBillNowToBillLaterChange(visitId, billNo,
            Boolean.TRUE);
      }
    }

		if (isNewUX.equals("Y")) {
			req.setAttribute("isNewUX", "Y");
			req.setAttribute("isNewUXSuccess", "Y");
			req.setAttribute("billNo", newBillNo);
			return m.findForward("changebilltype");
		}
		
		ActionRedirect redirect = new ActionRedirect(m.findForward("goToBill"));
		redirect.addParameter("billNo", newBillNo);

		FlashScope flash = FlashScope.getScope(req);
		flash.put("success", successMsg);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

}

