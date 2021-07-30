package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.PagedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChargeBO {

	static Logger logger = LoggerFactory.getLogger(ChargeBO.class);

	public ChargeBO() {}

	/*
	 * Get a list of charges that are approved by the given approval Id
	 */
	public List getApprovedCharges(String approvalId) throws SQLException, IOException {
		Connection con = DataBaseUtil.getConnection();
		try {
			ChargeDAO dao = new ChargeDAO(con);
			return dao.getApprovedCharges(approvalId);
		} finally {
			if (con != null) con.close();
		}
	}

	/*
	 * Search the list of approvals for matching criteria (filter) and return a list
	 * of Approval DTOs.
	 */
	public PagedList searchApprovals(java.sql.Date fromDate, java.sql.Date toDate,
				String username, String exportStatus,
				int sortOrder, int pageSize, int pageNum) throws SQLException {

		Connection con = DataBaseUtil.getConnection();
		try {
			ApprovalDAO dao = new ApprovalDAO(con);
			return dao.searchApprovals(fromDate, toDate, username, exportStatus,
					sortOrder, pageSize, pageNum);
		} finally {
			if (con != null) con.close();
		}
	}

	/*
	 * Batch update a set of charges: delete, update and approve. Note that for deletion
	 * and approval, we only need the chargeIds, so it is a list of Strings. For updating,
	 * we need the charge DTO itself.
	 *
	 * If there are any charges in the delete list as well as other lists, the deletion will
	 * happen first, and therefore will be not be updated or upproved. If there are any charges
	 * in both update and approve lists, it will be updated as well as approved.
	 *
	 * Returns a string with the new approval ID, if any approvals were done.
	 */
	public String updateCharges(List deleteChargeList, List updateChargeList, List approveChargeIdList,
			String username)
		throws SQLException {

		boolean success = false;
		String approvalId = null;
		String result = null;
		boolean updateReceipts = true;
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);


		try {
			ChargeDAO chargeDAO = new ChargeDAO(con);
			ApprovalDAO apprDao = new ApprovalDAO(con);
			do {
				/*
				 * Delete the charges that need to be deleted first
				 */
				if ( (deleteChargeList != null) && (deleteChargeList.size() > 0) ) {
					ArrayList deleteChargeIdList = new ArrayList();
					Iterator itr = deleteChargeList.iterator();
					while(itr.hasNext()) {
						ChargeDTO charge = (ChargeDTO) itr.next();
						deleteChargeIdList.add(charge.getChargeId());
					}

					success = chargeDAO.setBillModified(deleteChargeIdList);
					if (!success) break;

					success = chargeDAO.deleteCharges(deleteChargeIdList);
					if (!success) break;

					if(success) {
						updateReceipts = updateBillReceipts(con, deleteChargeList);
						success = updateReceipts;
						if (!success) break;
					}
				}

				/*
				 * Next, update any charges that need to be updated to new amounts/values
				 */
				ArrayList<ChargeDTO> modifiedChargeList = new ArrayList();
				if ( (updateChargeList != null) && (updateChargeList.size() > 0) ) {
					ArrayList modifiedIdList = new ArrayList();
					Iterator it = updateChargeList.iterator();
					while (it.hasNext()) {
						ChargeDTO charge = (ChargeDTO) it.next();
						modifiedIdList.add(charge.getChargeId());
						// get the original charge so that we don't overwrite existing data
						ChargeDTO newCharge = chargeDAO.getCharge(charge.getChargeId());
						newCharge.setActRate(charge.getActRate());
						newCharge.setActQuantity(charge.getActQuantity());
						newCharge.setDiscount(charge.getDiscount());
						newCharge.setAmount(charge.getAmount());
						newCharge.setStatus(charge.getStatus());
						// automatically set some fields
						newCharge.setUsername(charge.getUsername());
						newCharge.setModTime(DateUtil.getCurrentTimestamp());
						modifiedChargeList.add(newCharge);
					}
					success = chargeDAO.setBillModified(modifiedIdList);
					if (!success) break;
					success = chargeDAO.updateChargeAmountsList(modifiedChargeList);
					if (!success) break;

					if(success) {
						updateReceipts = updateBillReceipts(con, updateChargeList);
						success = updateReceipts;
						if (!success) break;
					}
				}

				/*
				 * Create an approval record, if there are any charges to be approved.
				 */
				if ( (approveChargeIdList != null) && (approveChargeIdList.size() > 0) ) {

					Approval appr = new Approval();
					approvalId = apprDao.getNextApprovalId();

					appr.setApprovalId(approvalId);
					appr.setUsername(username);
					appr.setNumTransactions(approveChargeIdList.size());

					java.sql.Date minTxnDate = new java.sql.Date(0);
					java.sql.Date maxTxnDate = new java.sql.Date(0);
					chargeDAO.getMinMaxPostedDate(approveChargeIdList, minTxnDate, maxTxnDate);

					appr.setMinTxnDate(minTxnDate);
					appr.setMaxTxnDate(maxTxnDate);

					success = apprDao.insertApproval(appr);
					if (!success)
						break;

					success = chargeDAO.approveCharges(approveChargeIdList, approvalId);
					// approval does not affect the bill's app_modified status
				}

			} while (false);		// fake loop to enable breaks on failure.

		} catch (SQLException e) {
			success = false;
			throw e;
		} finally {
			if (success) {
				logger.info("Charge list update success");
				con.commit(); con.close();
				result = "success";
			} else {
				logger.error("Charge List Update failed.");
				if(!updateReceipts) result = "Redo";
				else result = "Failed";
				con.rollback(); con.close();
			}
		}
		return result;
	}

	/*
	 * Check if a charge is cancellable based on internal rules of when
	 * a charge cancellation is allowed. This does not check the activity associated, only
	 * the bill related checks are performed.
	 */
	public boolean isCancellable(String chargeId) throws SQLException {
		// a charge can be cancelled if the bill is open or if the charge is a
		// not a package charge (package cancellation is specially handled).
		// Otherwise, it cannot be cancelled
		Connection con = DataBaseUtil.getConnection();
		try {
			ChargeDAO cdao = new ChargeDAO(con);
			BillDAO bdao = new BillDAO(con);

			ChargeDTO c = cdao.getCharge(chargeId);
			Bill b = bdao.getBill(c.getBillNo());

			if (!b.getStatus().equals(Bill.BILL_STATUS_OPEN))
				return false;
			if (c.getChargeHead().equals(c.CH_PACKAGE))
				return false;
			return true;
		} finally {
			if (con != null) con.close();
		}
	}

	public boolean isCancelable(String activityCode, int activityId) throws SQLException {
		// a charge can be cancelled if the bill is open or if the bill is a
		// not a package bill. Otherwise, it cannot be cancelled
		Connection con = DataBaseUtil.getConnection();
		try {
			BillActivityChargeDAO bacdao = new BillActivityChargeDAO(con);
			BillDAO bdao = new BillDAO(con);

			ChargeDTO c = bacdao.getCharge(activityCode, activityId);
			if (c != null) {
				Bill b = bdao.getBill(c.getBillNo());

				if (!b.getStatus().equals(Bill.BILL_STATUS_OPEN))
					return false;
				if (c.getChargeHead().equals(c.CH_PACKAGE))
					return false;
			}
			return true;
		} finally {
			if (con != null) con.close();
		}
	}


	// updates the receipt amounts if the  bill amounts are
	// updated for closed bills from transactions.
	public boolean updateBillReceipts(Connection con, List list) throws SQLException {

		ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);
		List newChargeList = new ArrayList();
		List billList = new ArrayList();

		String biNum;
		String billStatus;
		BigDecimal newBillAmt = new BigDecimal(0);
		BigDecimal actBillAmt = new BigDecimal(0);
		boolean dup = false;

		for(int i=0; i< list.size(); i++) {
			dup = false;
			for (int k=i; k<list.size(); k++) {
				ChargeDTO c3= (ChargeDTO) list.get(k);
				if(billList.contains(c3.getBillNo())){
					dup = true;
					break;
				}
			}

			if(!dup){
				ChargeDTO oldChrObj= (ChargeDTO) list.get(i);
				biNum = oldChrObj.getBillNo();
				billStatus = oldChrObj.getBillStatus();

				newBillAmt = oldChrObj.getAmount();
				actBillAmt = oldChrObj.getActualAmount();
				if(i < list.size()) {
					for (int j=i+1; j<list.size(); j++) {
						ChargeDTO newChrObj= (ChargeDTO) list.get(j);
						if (newChrObj.getBillNo().equalsIgnoreCase(biNum)) {
							newBillAmt = newBillAmt.add(newChrObj.getAmount());
							actBillAmt = actBillAmt.add(newChrObj.getActualAmount());
						}
					}
				}

				ChargeDTO charge = new ChargeDTO();
				charge.setBillNo(biNum);
				charge.setAmount(newBillAmt);
				charge.setActualAmount(actBillAmt);
				billList.add(biNum);
				if(billStatus.equalsIgnoreCase("C"))
					newChargeList.add(charge);
			}
		}
		return newChargeList.isEmpty()?true:receiptDAO.updateBillReceipts(newChargeList);
	}

	public boolean updateOhPayment(Connection con, String chargeId, int centerID)throws Exception{
		BigDecimal	ohAmount = ChargeDAO.getOuthouseCharge(con, chargeId, centerID);
		if (ohAmount != null) {
			ChargeDAO.updateOuthouseCharge(con, chargeId, ohAmount);
		}else{
			ChargeDAO.updateOuthouseCharge(con, chargeId, new BigDecimal(0));
		}
		return true;
	}
	
	public boolean updateOhPayment(Connection con, String chargeId, int outsourceDestId, int sourceCenterId, 
			String testId)throws Exception{
		BigDecimal	ohAmount = ChargeDAO.getOuthouseCharge(con, sourceCenterId, outsourceDestId, testId);
		if (ohAmount != null) {
			ChargeDAO.updateOuthouseCharge(con, chargeId, ohAmount);
		} else{
			ChargeDAO.updateOuthouseCharge(con, chargeId, new BigDecimal(0));
		}
		return true;
	}

	public List<String> getChargesByPostedDateRange(String visitId,Timestamp fromDateTime,
								Timestamp toDateTime) throws SQLException {
		Connection con=null;
		try {
			con = DataBaseUtil.getConnection();
			ChargeDAO dao = new ChargeDAO(con);
			return dao.getChargesByPostedDateRange(visitId,fromDateTime, toDateTime);
		} finally {
			if (con != null) con.close();
		}
	}
}

