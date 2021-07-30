/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.insurance.RemittanceAdviceDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lakshmi.p
 *
 */
public class BillRemittanceAction extends DispatchAction {

	public GenericDAO insAllocateDAO = new GenericDAO("insurance_payment_allocation");
	RemittanceAdviceDAO remDAO = new RemittanceAdviceDAO();

	public ActionForward getBillRemittance(ActionMapping am, ActionForm af,
				HttpServletRequest req, HttpServletResponse res) throws Exception {

		String billNo = req.getParameter("billNo").trim();

		JSONSerializer js = new JSONSerializer().exclude("class");
		BillBO billBOObj = new BillBO();
		BillDetails billDetails = null;
		Bill bill = null;
		billDetails = billBOObj.getBillDetails(billNo);
		if (billDetails != null) {
			bill = billDetails.getBill();
		} else {
			req.setAttribute("error", "There is no bill with number: " + billNo);
			return am.findForward("editBillRemittanceAmount");
		}

		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(bill.getVisitId());
		req.setAttribute("patient", patientDetails);

		req.setAttribute("billDetails", billDetails);

		List<BasicDynaBean> billPayments = BillRemittanceDAO.getBillPayments(billNo);
		req.setAttribute("billPayments", ConversionUtils.listBeanToListMap(billPayments));


		List<BasicDynaBean> chargePayments = BillRemittanceDAO.getChargeAllocPayments(billNo);
		if (chargePayments == null || chargePayments.size() == 0) {
			chargePayments = BillRemittanceDAO.getChargePayments(billNo);
		}
		req.setAttribute("chargePayments", ConversionUtils.listBeanToListMap(chargePayments));
		req.setAttribute("chargePaymentsJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(chargePayments)));

		req.setAttribute("pendingEquipmentFinalization",
				OrderDAO.isBillEquipmentDetailsFinalized(bill.getVisitId(), billNo));
		req.setAttribute("pendingBedFinalization",
				OrderDAO.isBillBedDetailsFinalized(bill.getVisitId(), billNo));

		boolean isDetailLevelAllocation = false;
		List<BasicDynaBean> chargeItemLevelList =  BillRemittanceDAO.getChargeItemLevelRemittance(billNo);
		isDetailLevelAllocation = chargeItemLevelList != null && chargeItemLevelList.size() > 0;
		req.setAttribute("isDetailLevelAllocation", isDetailLevelAllocation);

		if (isDetailLevelAllocation)
			req.setAttribute("error", "This Bill received remittance at each item level. Cannot allocate again at bill level.");

		return am.findForward("editBillRemittanceAmount");
	}


	public ActionForward saveBillRemittance(ActionMapping am, ActionForm af,
				HttpServletRequest req, HttpServletResponse res) throws Exception {
		ActionRedirect redirect = new ActionRedirect(am.findForward("showRedirect"));
		FlashScope flash = FlashScope.getScope(req);
		String error = null;

		GenericDAO billdao = new GenericDAO("bill");
		GenericDAO charge = new GenericDAO("bill_charge");
		GenericDAO item = new GenericDAO("store_sales_details");
		GenericDAO insUnallocateDAO = new GenericDAO("insurance_payment_unalloc_amount");
		String billNo = req.getParameter("billNo");
		BillBO billBOObj = new BillBO();
		Bill bill = billBOObj.getBill(billNo);
		boolean success = false;
		Connection con = null;

		String newStatus = req.getParameter("billStatus");
		String paymentStatus = (req.getParameter("paymentStatus") == null) ? bill.getPaymentStatus() : req.getParameter("paymentStatus");

		String[] paymentIdSeqs			= req.getParameterValues("paymentIdSeq");
		String[] paymentRemittanceIds	= req.getParameterValues("paymentRemittanceId");
		String[] paymentReferences		= req.getParameterValues("paymentReference");
		String[] origPaymentReferences	= req.getParameterValues("origPaymentReference");
		String[] paymentRecdDates		= req.getParameterValues("paymentRecdDate");
		String[] paymentAmounts			= req.getParameterValues("paymentAmount");
		String[] paymentAllocAmounts	= req.getParameterValues("paymentAllocAmount");
		String[] paymentUnallocAmounts	= req.getParameterValues("paymentUnallocAmount");

		String[] chargeIds = req.getParameterValues("chargeId");
		String[] allocatedClaimAmts = req.getParameterValues("allocatedClaimAmt");
		String[] charge_paymentref_amounts = req.getParameterValues("chargeid_paymentref_amount");

		List<BasicDynaBean> unallocUpdateList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> unallocInsertList = new ArrayList<BasicDynaBean>();

		List<BasicDynaBean> insurancePaymentList = new ArrayList<BasicDynaBean>();

		BigDecimal totalPaymentRecd = BigDecimal.ZERO;
		BigDecimal totalPaymentAllocated = BigDecimal.ZERO;
		BigDecimal totalPaymentUnallocated = BigDecimal.ZERO;

billremittance: {
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (paymentRemittanceIds == null || paymentRemittanceIds.length == 0) {
				success = false;
				error = "No remittance received. Cannot allocate claim amounts.";
				break billremittance;
			}

			for (int i = 0; i < paymentRemittanceIds.length; i++) {
				int remId = paymentRemittanceIds[i] != null
					&& !(paymentRemittanceIds[i].equals(""))
					&& !(paymentRemittanceIds[i].equals("_")) ? new Integer(paymentRemittanceIds[i]) : 0;
				BasicDynaBean rbean = remDAO.findByKey(con, "remittance_id", remId);
				if (rbean != null && ((String)rbean.get("detail_level")).equals("I")) {
					success = false;
					error = "This Bill received remittance at each item level. Cannot allocate again at bill level.";
					break billremittance;
				}
			}


			for (int i = 0; i < paymentRemittanceIds.length; i++) {
				BasicDynaBean ubean = null;
				if (paymentRemittanceIds[i] != null && paymentRemittanceIds[i].equals("_")) {
					ubean = insUnallocateDAO.getBean();
					ubean.set("payment_id", insUnallocateDAO.getNextSequence());
					ubean.set("remittance_id", 0);
					ubean.set("bill_no", billNo);
					ubean.set("payment_reference", paymentReferences[i]);
					ubean.set("payment_recd_date", DateUtil.parseDate(paymentRecdDates[i]));
					ubean.set("is_recovery", "N");
					ubean.set("denial_remarks", null);
					ubean.set("amount_recd", new BigDecimal(paymentAmounts[i]));
					ubean.set("unalloc_amount", new BigDecimal(paymentUnallocAmounts[i]));
					unallocInsertList.add(ubean);

				}else {
					if (paymentIdSeqs[i] != null && !paymentIdSeqs[i].equals("")) {
						ubean = insUnallocateDAO.findByKey("payment_id", new Integer(paymentIdSeqs[i]));
						ubean.set("payment_reference", paymentReferences[i]);
						ubean.set("payment_recd_date", DateUtil.parseDate(paymentRecdDates[i]));
						ubean.set("amount_recd", new BigDecimal(paymentAmounts[i]));
						ubean.set("unalloc_amount", new BigDecimal(paymentUnallocAmounts[i]));
						unallocUpdateList.add(ubean);
					}
				}

				if (paymentAmounts[i] != null && !paymentAmounts[i].equals(""))
					totalPaymentRecd = totalPaymentRecd.add(new BigDecimal(paymentAmounts[i]));

				if (paymentUnallocAmounts[i] != null && !paymentUnallocAmounts[i].equals(""))
					totalPaymentUnallocated = totalPaymentUnallocated.add(new BigDecimal(paymentUnallocAmounts[i]));
			}

			totalPaymentAllocated = totalPaymentRecd.subtract(totalPaymentUnallocated);

			boolean paymentRefEdited = false;

			for (int r = 0; r < origPaymentReferences.length; r++) {
				if (!origPaymentReferences[r].equals(paymentReferences[r])) {
					paymentRefEdited = true;
					break;
				}
			}

			 // Update all Payment references
			 for (BasicDynaBean pay : unallocUpdateList) {
				 insUnallocateDAO.updateWithName(con, pay.getMap(), "payment_id");
			 }

			 BasicDynaBean billbean = billdao.findByKey("bill_no", billNo);
			 billbean.set("claim_recd_amount", totalPaymentAllocated);
			 billbean.set("claim_recd_unalloc_amount", totalPaymentUnallocated);

			 // Update claim and bill status according to payment total received.
			BigDecimal billAmt = (BigDecimal) billbean.get("total_amount");
			BigDecimal insAmt = (BigDecimal) billbean.get("total_claim");
			BigDecimal priClaimAmt = ((BigDecimal) billbean.get("primary_total_claim"));
			BigDecimal secClaimAmt = ((BigDecimal) billbean.get("secondary_total_claim"));
			BigDecimal dedn = (BigDecimal) billbean.get("insurance_deduction");

			BigDecimal totalReceipts = (BigDecimal) billbean.get("total_receipts");
			BigDecimal depositSetOff = (BigDecimal) billbean.get("deposit_set_off");
			BigDecimal pointsRedeemedAmt = (BigDecimal) billbean.get("points_redeemed_amt");

			BigDecimal totalClaimReturns = billbean.get("total_claim_return") == null ?
					BigDecimal.ZERO : (BigDecimal) billbean.get("total_claim_return");

			BigDecimal patientAmt = billAmt.subtract(priClaimAmt).subtract(secClaimAmt);
			BigDecimal patientCredits = totalReceipts.add(depositSetOff).add(pointsRedeemedAmt);

			// For remittance, deduction is not considered.
			// Hence, bill is not closed if deduction exists but can be force closed from billing.

			//insAmt = insAmt.subtract(dedn);

			BigDecimal totalInsAmt = insAmt.add(totalClaimReturns);

			// Claim amount check
			if ((newStatus.equals("F") || billbean.get("status").equals("F")) && (totalInsAmt.compareTo(totalPaymentAllocated) <= 0)) {
				 billbean.set("primary_claim_status", "R");
			}

			// Payment amount && Claim amount check

			if (paymentStatus.equals("U") && patientAmt.compareTo(patientCredits) == 0) {
				paymentStatus = "P";
			}

			if (((paymentStatus.equals("P") || billbean.get("payment_status").equals("P")) && patientAmt.compareTo(patientCredits) == 0)
						&& (totalInsAmt.compareTo(totalPaymentAllocated) <= 0)) {
				 billbean.set("primary_claim_status", "R");
				 newStatus = "C"; // Bill is closed if totals payments are received.
			}

			 // Update bill
			 billdao.updateWithName(con, billbean.getMap(), "bill_no");

			 // Insert payment details into insurance payment unalloc amount table.
			 insUnallocateDAO.insertAll(con, unallocInsertList);

			 // Delete existing charge payment details from insurance payment allocation table.
			 BillRemittanceDAO.deleteAllocations(con, billNo);

			 List<String> chargeid_paymentref_amount_list = new ArrayList<String>();
			 String[] chargeid_paymentref_amount = null;

			 // Create new chargeid_paymentref_amount String array
			 int k = 0;
			 if (paymentRefEdited) {
				 for (int i = 0; i < charge_paymentref_amounts.length; i++) {
					 String chrg = charge_paymentref_amounts[i].split("_")[0];
					 String payref = charge_paymentref_amounts[i].split("_")[1];
					 String amt = charge_paymentref_amounts[i].split("_")[2];
					 for (int j = 0; j < paymentReferences.length; j++) {
						 if (payref.equals(origPaymentReferences[j])) {
							 chargeid_paymentref_amount_list.add(chrg+"_"+paymentReferences[j]+"_"+amt);
							 break;
						 }
					}
				}
				 chargeid_paymentref_amount = new String[chargeid_paymentref_amount_list.size()];
				 for (String chpmt : chargeid_paymentref_amount_list) {
					 chargeid_paymentref_amount[k] = chpmt;
					 k++;
				 }
			 }else {
				 chargeid_paymentref_amount = charge_paymentref_amounts;
			 }

			 ArrayList<BasicDynaBean> chargeBeansList = new ArrayList<BasicDynaBean>();
			 ArrayList<String> pharmacyCharges = new ArrayList<String>();

			 ArrayList<BasicDynaBean> parmacyItemBeansList = new ArrayList<BasicDynaBean>();

			 String denial_remarks = "Charge claim amount not equal to recd. amount";

			 for (int i = 0; i < chargeIds.length; i++) {
				 String chargeId = chargeIds[i];
				 BasicDynaBean billChrgPaymentBean = charge.findByKey("charge_id", chargeId);

				 BigDecimal insuClaimAmt = billChrgPaymentBean.get("insurance_claim_amount") != null ?
						 (BigDecimal)(billChrgPaymentBean.get("insurance_claim_amount")) : BigDecimal.ZERO;

				 BigDecimal tot_alloc_amount = BigDecimal.ZERO;

				if (chargeid_paymentref_amount != null && chargeid_paymentref_amount.length > 0) {
					for (int j = 0; j < chargeid_paymentref_amount.length; j++) {
						String[] pChArr = chargeid_paymentref_amount[j].split("_");
						String pay_charge_id = pChArr[0];
						String pay_ref_id = pChArr[1];
						BigDecimal pay_amount = new BigDecimal(pChArr[2]);

						int remittance_id = 0;

						for (int r = 0; r < paymentReferences.length; r++) {
							if (!pay_ref_id.equals("") && pay_ref_id.equals(paymentReferences[r])) {
								pay_ref_id = paymentReferences[r];
								remittance_id = new Integer(paymentRemittanceIds[r]);
								break;
							}
						}

						if (pay_charge_id.equals(chargeId)) {
							tot_alloc_amount = tot_alloc_amount.add(pay_amount);

							 BasicDynaBean chrgeAllocateBean =
								 getChargeAllocateBean(remittance_id, chargeId, null, pay_ref_id, pay_amount, "A", 0, null);
							 insurancePaymentList.add(chrgeAllocateBean);
						}
					}
				}

				billChrgPaymentBean.set("claim_recd_total", tot_alloc_amount);

				if (insuClaimAmt.compareTo(tot_alloc_amount) == 0 ) {
					billChrgPaymentBean.set("claim_status", "C");

				}else {
					 billChrgPaymentBean.set("denial_code", null);
					 billChrgPaymentBean.set("claim_status", "D");
					 billChrgPaymentBean.set("denial_remarks", denial_remarks);
				 }

				chargeBeansList.add(billChrgPaymentBean);

				String charge_head = (String)billChrgPaymentBean.get("charge_head");

				if (charge_head.equals("PHMED") || charge_head.equals("PHCMED") || charge_head.equals("PHRET") || charge_head.equals("PHCRET")) {
					pharmacyCharges.add(chargeId);
				}
			}

			 // Update all charge's claim recd total in the bill
			 for (BasicDynaBean b : chargeBeansList) {
				 charge.updateWithName(con, b.getMap(), "charge_id");
			 }

			 // Update sale related charge in bill_charge by fetching sale id from bill activity charge.
			 for (String phCharge : pharmacyCharges) {

				 List<BasicDynaBean> currentPhChargePayList = new ArrayList<BasicDynaBean>();

				 for (BasicDynaBean insbean : insurancePaymentList) {
					 if (insbean.get("charge_id").equals(phCharge)) {
						 currentPhChargePayList.add(insbean);
					 }
				 }

				 BasicDynaBean sale = remDAO.findSaleCharge(con, phCharge);
				 String sale_id = (String)sale.get("sale_id");

				 int phPayLen = currentPhChargePayList.size();

				 int p = 0;
				 BasicDynaBean insPhChrgPaymentBean = null;
				 BigDecimal totalChargeClaimRecdAmt = null;
				 int remittanceID = 0;

				 ArrayList<String> paymentRefList = new ArrayList<String>();

				 if (phPayLen > 0) {
					 insPhChrgPaymentBean = currentPhChargePayList.get(p);
					 totalChargeClaimRecdAmt = (BigDecimal)insPhChrgPaymentBean.get("amount");
					 remittanceID = (Integer)insPhChrgPaymentBean.get("remittance_id");
				 }

				 List<BasicDynaBean> items = remDAO.findItemsBySaleId(con, sale_id);
				 int itemLen = items.size();
				 int i = 0;

				 BasicDynaBean sbItem = null;
				 BigDecimal itemClaimAmt = null;
				 int saleItemId = 0;

				 if (itemLen > 0) {
					 sbItem = getSaleItemBean(items, i);
					 itemClaimAmt = ((BigDecimal)sbItem.get("insurance_claim_amt")).add((BigDecimal)sbItem.get("return_insurance_claim_amt"));
					 saleItemId = (Integer)sbItem.get("sale_item_id");
				 }

				 Map phPaymentMap = getNextPaymentReferenceMap(chargeid_paymentref_amount, phCharge, paymentRefList);
				 String pay_ref_id = (String)phPaymentMap.get("pay_ref_id");
				 BigDecimal pay_amount = (BigDecimal)phPaymentMap.get("pay_amount");

				 while (p < phPayLen && i < itemLen && totalPaymentAllocated.compareTo(BigDecimal.ZERO) > 0) {

					 if (totalChargeClaimRecdAmt.compareTo(BigDecimal.ZERO) > 0) {

						 if (totalChargeClaimRecdAmt.compareTo(itemClaimAmt) <= 0) {

							 sbItem.set("claim_recd_total", totalChargeClaimRecdAmt);
							 sbItem.set("claim_status", (itemClaimAmt.compareTo(totalChargeClaimRecdAmt) == 0) ? "C" : "D");
							 sbItem.set("denial_code", null);
							 sbItem.set("denial_remarks", (itemClaimAmt.compareTo(totalChargeClaimRecdAmt) == 0) ? null : denial_remarks);

							 if (pay_amount.compareTo(totalChargeClaimRecdAmt) <= 0) {

								BasicDynaBean chrgeAllocateBean =
									 getChargeAllocateBean(remittanceID, phCharge, null, pay_ref_id,
											 pay_amount, "P", saleItemId,
											 (itemClaimAmt.compareTo(pay_amount) == 0) ? null : denial_remarks);

								 insurancePaymentList.add(chrgeAllocateBean);

								 itemClaimAmt = itemClaimAmt.subtract(pay_amount);
								 totalChargeClaimRecdAmt = totalChargeClaimRecdAmt.subtract(pay_amount);
								 pay_amount = pay_amount.subtract(pay_amount);

							}else if (pay_amount.compareTo(totalChargeClaimRecdAmt) > 0) {

								BasicDynaBean chrgeAllocateBean =
									 getChargeAllocateBean(remittanceID, phCharge, null, pay_ref_id,
											 totalChargeClaimRecdAmt, "P", saleItemId,
											 (itemClaimAmt.compareTo(totalChargeClaimRecdAmt) == 0) ? null : denial_remarks);

								 insurancePaymentList.add(chrgeAllocateBean);

								 itemClaimAmt = itemClaimAmt.subtract(totalChargeClaimRecdAmt);
								 pay_amount = pay_amount.subtract(totalChargeClaimRecdAmt);
								 totalChargeClaimRecdAmt = BigDecimal.ZERO;

							}

		 					parmacyItemBeansList.add(sbItem);

							 if (itemClaimAmt.compareTo(BigDecimal.ZERO) <= 0) {
								 i++;
								 if (i < itemLen) {
									 sbItem = getSaleItemBean(items, i);
									 itemClaimAmt = ((BigDecimal)sbItem.get("insurance_claim_amt")).add((BigDecimal)sbItem.get("return_insurance_claim_amt"));
									 saleItemId = (Integer)sbItem.get("sale_item_id");
								 }
							 }

							 if (pay_amount.compareTo(BigDecimal.ZERO) <= 0) {
								 phPaymentMap = getNextPaymentReferenceMap(chargeid_paymentref_amount, phCharge, paymentRefList);
								 pay_ref_id = (String)phPaymentMap.get("pay_ref_id");
								 pay_amount = (BigDecimal)phPaymentMap.get("pay_amount");
							 }

	 						if (totalChargeClaimRecdAmt.compareTo(BigDecimal.ZERO) <= 0) {
	 							p++;
								if (p < phPayLen) {
									insPhChrgPaymentBean = currentPhChargePayList.get(p);
									totalChargeClaimRecdAmt = (BigDecimal)insPhChrgPaymentBean.get("amount");
									remittanceID = (Integer)insPhChrgPaymentBean.get("remittance_id");
								}
	 						}

						 }else if (totalChargeClaimRecdAmt.compareTo(itemClaimAmt) > 0) {
							sbItem.set("claim_recd_total", itemClaimAmt);
							sbItem.set("claim_status", "C");
							sbItem.set("denial_code", null);
							sbItem.set("denial_remarks", denial_remarks);

							if (pay_amount.compareTo(itemClaimAmt) <= 0) {

								BasicDynaBean chrgeAllocateBean =
									 getChargeAllocateBean(remittanceID, phCharge, null, pay_ref_id,
											 pay_amount, "P", saleItemId,
											 (itemClaimAmt.compareTo(pay_amount) == 0) ? null : denial_remarks);

								 insurancePaymentList.add(chrgeAllocateBean);

								 itemClaimAmt = itemClaimAmt.subtract(pay_amount);
								 totalChargeClaimRecdAmt = totalChargeClaimRecdAmt.subtract(pay_amount);
								 pay_amount = pay_amount.subtract(pay_amount);

							}else if (pay_amount.compareTo(itemClaimAmt) > 0) {

								BasicDynaBean chrgeAllocateBean =
									 getChargeAllocateBean(remittanceID, phCharge, null, pay_ref_id,
											 itemClaimAmt, "P", saleItemId,
											 (itemClaimAmt.compareTo(itemClaimAmt) == 0) ? null : denial_remarks);

								 insurancePaymentList.add(chrgeAllocateBean);

								 totalChargeClaimRecdAmt = totalChargeClaimRecdAmt.subtract(itemClaimAmt);
								 pay_amount = pay_amount.subtract(itemClaimAmt);
								 itemClaimAmt = itemClaimAmt.subtract(itemClaimAmt);
							}

						 	parmacyItemBeansList.add(sbItem);

							if (itemClaimAmt.compareTo(BigDecimal.ZERO) <= 0) {
								i++;
								if (i < itemLen) {
									sbItem = getSaleItemBean(items, i);
									itemClaimAmt = ((BigDecimal)sbItem.get("insurance_claim_amt")).add((BigDecimal)sbItem.get("return_insurance_claim_amt"));
									saleItemId = (Integer)sbItem.get("sale_item_id");
								}
							}

							if (pay_amount.compareTo(BigDecimal.ZERO) <= 0) {
								phPaymentMap = getNextPaymentReferenceMap(chargeid_paymentref_amount, phCharge, paymentRefList);
								pay_ref_id = (String)phPaymentMap.get("pay_ref_id");
								pay_amount = (BigDecimal)phPaymentMap.get("pay_amount");
							}

						 	if (totalChargeClaimRecdAmt.compareTo(BigDecimal.ZERO) <= 0) {
	 							p++;
								if (p < phPayLen) {
									insPhChrgPaymentBean = currentPhChargePayList.get(p);
									totalChargeClaimRecdAmt = (BigDecimal)insPhChrgPaymentBean.get("amount");
									remittanceID = (Integer)insPhChrgPaymentBean.get("remittance_id");
								}
	 						}
						 }
					 }else {
						 p++;
						 if (p < phPayLen) {
							 insPhChrgPaymentBean = currentPhChargePayList.get(p);
							 totalChargeClaimRecdAmt = (BigDecimal)insPhChrgPaymentBean.get("amount");
							 remittanceID = (Integer)insPhChrgPaymentBean.get("remittance_id");
						 }
					 }
				 }// while p < phPayLen
			 }

			 // Update all pharmacy sale items.
			 for (BasicDynaBean pb : parmacyItemBeansList) {
				 item.updateWithName(con, pb.getMap(), "sale_item_id");
			 }

			 List<BasicDynaBean> insPayBeansList = new ArrayList<BasicDynaBean>();

			 for (BasicDynaBean insbean : insurancePaymentList) {
				 if (pharmacyCharges.size() > 0) {
					 for (String phCharge : pharmacyCharges) {
						 if (insbean.get("charge_id").equals(phCharge) && ((Integer)insbean.get("sale_item_id")).intValue() == 0) {}
						 else insPayBeansList.add(insbean);
					 }
				 }else {
					 insPayBeansList.add(insbean);
				 }
			 }

			 // Insert activity details into insurance payment allocation table.
			 insAllocateDAO.insertAll(con, insPayBeansList);

			 success = true;

		}finally {
			DataBaseUtil.commitClose(con, success);
		}
	}// billremittance label

		String origStatus = bill.getStatus();

		HttpSession session = req.getSession();
		String userid = (String)session.getAttribute("userid");

		HashMap actionRightsMap = new HashMap();
		actionRightsMap = (HashMap) req.getSession(false).getAttribute("actionRightsMap");
		Object roleID = req.getSession(false).getAttribute("roleId");
		String actionBackDateRights = (String)actionRightsMap.get("allow_backdate");
		if (actionBackDateRights==null) actionBackDateRights = "N";

		/*
		 * Status update
		 */
		if (!origStatus.equals(newStatus)) {
			java.sql.Timestamp finalizedDate = null;
			// set finalized date if origStatus is open, and new status is anything other
			// than open, ie, open->finalized, open->closed, open->canceled.
			if (origStatus.equals(Bill.BILL_STATUS_OPEN) && !newStatus.equals(Bill.BILL_STATUS_OPEN)) {
				if (actionBackDateRights.equalsIgnoreCase("A") || roleID.equals(1) || roleID.equals(2)) {
					finalizedDate = DateUtil.parseTimestamp(
							req.getParameter("finalizedDate"), req.getParameter("finalizedTime"));
				}
			} else {
				finalizedDate = DateUtil.parseTimestamp(
						req.getParameter("finalizedDate"), req.getParameter("finalizedTime"));
			}
			if (finalizedDate == null) {
				finalizedDate = bill.getFinalizedDate() == null ?  DateUtil.getCurrentTimestamp() :
					new java.sql.Timestamp(bill.getFinalizedDate().getTime());
			}

			// Set the finalized by when the new status is finalized or closed
			// (And) the Bill is a Credit bill or Bill now with TPA (And) the finalized by is empty.
			if ((newStatus.equals(Bill.BILL_STATUS_FINALIZED)
					|| (newStatus.equals(Bill.BILL_STATUS_CLOSED)
						&& (bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)
								|| (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) && bill.getIs_tpa()))))
					&& (bill.getFinalizedBy() == null || bill.getFinalizedBy().equals("")))
				bill.setFinalizedBy(userid);

			String  dischargeStatus = null;
			if (dischargeStatus == null) {
				dischargeStatus = bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) &&
					paymentStatus.equals("P") ? "Y" : "N" ;
			}

			boolean paymentForceClose = req.getParameter("paymentForceClose") != null &&
						req.getParameter("paymentForceClose").equals("Y");

			boolean claimForceClose = req.getParameter("claimForceClose") != null &&
						req.getParameter("claimForceClose").equals("Y");

			error = new BillBO().updateBillStatus(bill, newStatus, paymentStatus,
					dischargeStatus, finalizedDate, userid, paymentForceClose, claimForceClose);

			if (error == null || error.equals("")) error = "";
			else if(error.startsWith("Bill status")) {}
			else error = error + "<br/> Please modify the discount amount and finalize /close the bill again.";
		}

		redirect.addParameter("billNo", billNo);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		flash.put("error", error);

		return redirect;
	}

	private BasicDynaBean getSaleItemBean(List<BasicDynaBean> items, int i) {
		return (BasicDynaBean)items.get(i);
	}

	private static Map getNextPaymentReferenceMap(String[] chargeid_paymentref_amount, String chargeId, ArrayList paymentRefList)  {
		Map payMap = new HashMap();
		for (int j = 0; j < chargeid_paymentref_amount.length; j++) {
			 String[] pChArr = chargeid_paymentref_amount[j].split("_");
			 String pay_charge_id = pChArr[0];
			 String pay_ref_id = pChArr[1];
			 BigDecimal pay_amount = new BigDecimal(pChArr[2]);
			 if (chargeId.equals(pay_charge_id) && !paymentRefList.contains(pay_ref_id)) {
				 payMap.put("pay_ref_id", pay_ref_id);
				 payMap.put("pay_amount", pay_amount);
				 paymentRefList.add(pay_ref_id);
				 break;
			 }
		 }
		return payMap;
	}

	private BasicDynaBean getChargeAllocateBean(int remittance_id, String chargeId, String denial_code,
				String pay_ref_id, BigDecimal pay_amount, String charge_type, int sale_item_id, String denial_remarks) throws SQLException {
		 BasicDynaBean chrgeAllocateBean = insAllocateDAO.getBean();
		 chrgeAllocateBean.set("remittance_id", remittance_id);
		 chrgeAllocateBean.set("charge_id", chargeId);
		 chrgeAllocateBean.set("denial_code", null);
		 chrgeAllocateBean.set("payment_reference", pay_ref_id);
		 chrgeAllocateBean.set("amount", pay_amount);
		 chrgeAllocateBean.set("charge_type", charge_type);
		 chrgeAllocateBean.set("sale_item_id", sale_item_id);
		 chrgeAllocateBean.set("denial_remarks", denial_remarks);
		 return chrgeAllocateBean;
	}
}
