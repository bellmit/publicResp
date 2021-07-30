/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.usermanager.Role;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @author lakshmi
 *
 */
public class EditInsuranceHelper {

	public enum BillsRequired {
		all_bills, open_bills, none;
	};

	public int memberShipValidityExists(List<BasicDynaBean> existingPatPolicyDetailsList, String policyNo,
			int planId, Date endValidity, BasicDynaBean sponsorTypeBean) {
		if (existingPatPolicyDetailsList == null || existingPatPolicyDetailsList.size() == 0)
			return 0;
		if(null != sponsorTypeBean){
			if(sponsorTypeBean.get("member_id_show").equals("Y") && sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(b.get("status").equals("A")){
						if(null != b.get("member_id") && null != b.get("policy_validity_end") && null!=policyNo && null!=endValidity){
							if(((String)b.get("member_id")).equals(policyNo)
								&& ((Integer)b.get("plan_id")).equals(planId)
								&& ((Date)b.get("policy_validity_end")).getTime() == endValidity.getTime()) {
								return (Integer)b.get("patient_policy_id");
							}
						}else if(null == b.get("member_id") && (null == policyNo || policyNo.equals("")) && null ==b.get("policy_validity_end")
								&& (null == endValidity) && ((Integer)b.get("plan_id")).equals(planId)){
							return (Integer)b.get("patient_policy_id");
						}else if ((null == b.get("member_id") && null !=policyNo && !policyNo.equals("")) ||
								(null == b.get("policy_validity_end"))&& null != endValidity){
							return 0;
						}
					}
				}
			}
			else if(sponsorTypeBean.get("member_id_show").equals("Y") && !sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(b.get("status").equals("A")){
						if(null != b.get("member_id") && null!=policyNo){
							if(((String)b.get("member_id")).equals(policyNo)
								&& ((Integer)b.get("plan_id")).equals(planId)){
								return (Integer)b.get("patient_policy_id");
							}
						}else if(null == b.get("member_id") && (null==policyNo || policyNo.equals("")) && ((Integer)b.get("plan_id")).equals(planId)){
							return (Integer)b.get("patient_policy_id");
						}else if(null == b.get("member_id") && null != policyNo && !(policyNo.equals(""))){
							return 0;
						}
					}
				}
			}
			else if(!sponsorTypeBean.get("member_id_show").equals("Y") && sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(b.get("status").equals("A")){
					  if(null != b.get("policy_validity_end") && null!=endValidity){
							if(((Integer)b.get("plan_id")).equals(planId)
								&& ((Date)b.get("policy_validity_end")).getTime() == endValidity.getTime()) {
								return (Integer)b.get("patient_policy_id");
							}
						}else if(null == b.get("policy_validity_end") && (null==endValidity) && ((Integer)b.get("plan_id")).equals(planId)){
							return (Integer)b.get("patient_policy_id");
						}else if(null == b.get("policy_validity_end") && null != endValidity){
							return 0;
						}
					}
				}
			}
			else if(!sponsorTypeBean.get("member_id_show").equals("Y") && !sponsorTypeBean.get("validity_period_show").equals("Y")){
				for(BasicDynaBean b: existingPatPolicyDetailsList) {
					if(((Integer)b.get("plan_id")).equals(planId)) {
						return (Integer)b.get("patient_policy_id");
					}
				}
			}
			return 0;
		}
		return 0;
	}

	public int corporateValidityExists(List<BasicDynaBean> existingPatPolicyDetailsList, String employeeId,
			String sponsorId, Date endValidity) {
		if (existingPatPolicyDetailsList == null || existingPatPolicyDetailsList.size() == 0)
			return 0;

		for (BasicDynaBean b : existingPatPolicyDetailsList) {
			if (((String) b.get("employee_id")).equals(employeeId)
					&& ((String) b.get("sponsor_id")).equals(sponsorId)) {
				return (Integer) b.get("patient_corporate_id");
			}
		}
		return 0;
	}

	public int nationalValidityExists(List<BasicDynaBean> existingPatPolicyDetailsList, String nationalId,
			String sponsorId, Date endValidity) {
		if (existingPatPolicyDetailsList == null || existingPatPolicyDetailsList.size() == 0)
			return 0;

		for (BasicDynaBean b : existingPatPolicyDetailsList) {
			if (((String) b.get("national_id")).equals(nationalId)
					&& ((String) b.get("sponsor_id")).equals(sponsorId)) {
				return (Integer) b.get("patient_national_sponsor_id");
			}
		}
		return 0;
	}

	public BasicDynaBean findByKeyDescending(Connection con, String table,
			String keycolumn, Object identifier, String orderField) throws SQLException {

		StringBuilder query = new StringBuilder();
		keycolumn = DataBaseUtil.quoteIdent(keycolumn);
		query.append("SELECT * FROM ").append(table).append(" WHERE ").append(
				keycolumn).append("=?").append(" ORDER BY " + orderField + " DESC ");

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement(query.toString());
			ps.setObject(1, identifier);
			List list = DataBaseUtil.queryToDynaList(ps);
			if (list.size() > 0) {
				return (BasicDynaBean) list.get(0);
			} else {
				return null;
			}
		} finally {
			if(null != ps) {
			  ps.close();
			}
		}
	}

	public BasicDynaBean findLatestPlanDocumentBean(Connection con, Integer docId) throws SQLException {
		return findByKeyDescending(con, "plan_docs_details", "patient_policy_id", docId, "doc_id");
	}

	public BasicDynaBean findLatestCorporateDocumentBean(Connection con, Integer docId) throws SQLException {
		return findByKeyDescending(con, "corporate_docs_details", "patient_corporate_id", docId, "doc_id");
	}

	public BasicDynaBean findLatestNationalDocumentBean(Connection con, Integer docId) throws SQLException {
		return findByKeyDescending(con, "national_sponsor_docs_details", "patient_national_sponsor_id", docId, "doc_id");
	}

	public List<BasicDynaBean> getMainAndFollowUpVisits(String visitId) throws SQLException {
		VisitDetailsDAO visitdao = new VisitDetailsDAO();
		BasicDynaBean visitDetailsBean = visitdao.findByKey("patient_id", visitId);
		List<BasicDynaBean> followupVisits = new ArrayList<BasicDynaBean>();
		String op_type = visitDetailsBean.get("op_type") != null ? (String) visitDetailsBean.get("op_type") : "M";
		if (op_type != null && (op_type.equals("M") || op_type.equals("R")))
			followupVisits = visitdao.getEpisodeAllFollowUpVisitsOnly(visitId);

		// Main Visit
		BasicDynaBean mainVisit = visitdao.findByKey("patient_id", visitId);

		// List of all visits (Follow up and the Main visit)
		List<BasicDynaBean> allVisits = new ArrayList<BasicDynaBean>();
		allVisits.add(mainVisit);

		if (followupVisits != null && followupVisits.size() > 0) {
			allVisits.addAll(followupVisits);
		}
		return allVisits;
	}

	public List<BasicDynaBean> getMainAndFollowUpVisitTPABills(String visitId, String billsRequired) throws SQLException {
		List<BasicDynaBean> allVisits = getMainAndFollowUpVisits(visitId);

		// List of all bills (Followup visit and Main visit TPA bills)
		List<BasicDynaBean> tpaBills = new ArrayList<BasicDynaBean>();

		if (allVisits != null && allVisits.size() > 0) {
			for (BasicDynaBean b : allVisits) {

				String patientId = (String) b.get("patient_id");

				if (billsRequired.equals(BillsRequired.all_bills.toString())) {
					List<BasicDynaBean> bills = BillDAO.getVisitBills(patientId,
							BillDAO.bill_type.BOTH, true, true);
					tpaBills.addAll(bills);

				}else if (billsRequired.equals(BillsRequired.open_bills.toString())) {
					List<BasicDynaBean> bills = BillDAO.getActiveUnpaidBills(patientId,
							BillDAO.bill_type.BOTH, true, true);
					tpaBills.addAll(bills);

				}else if (billsRequired.equals(BillsRequired.none.toString())) {

				}
			}
		}
		return tpaBills;
	}

	public List<BasicDynaBean> getVisitTPABills(String visitId, String billsRequired) throws SQLException {
		List<BasicDynaBean> tpaBills = null;
		if (billsRequired.equals(BillsRequired.all_bills.toString())) {
			tpaBills = BillDAO.getVisitBills(visitId, BillDAO.bill_type.BOTH, true, true);

		}else if (billsRequired.equals(BillsRequired.open_bills.toString())) {
			tpaBills = BillDAO.getActiveUnpaidBills(visitId, BillDAO.bill_type.BOTH, true, true);

		}else if (billsRequired.equals(BillsRequired.none.toString())) {

		}
		return tpaBills;
	}

	public boolean updateSponsorClaimTotals(List<BasicDynaBean> tpaBills) throws SQLException, IOException {

		if (tpaBills != null && tpaBills.size() > 0) {
			for(BasicDynaBean billbean: tpaBills) {
				String billNo = (String)billbean.get("bill_no");
				BillDAO.resetTotalsOrReProcessNew(billNo, false, true, true);
			}
		}
		return true;
	}

	public boolean removeInsuranceFromBills(Connection con, BasicDynaBean visitDetailsBean,
						String orgId, List<BasicDynaBean> tpaBills) throws Exception{

		String err = null;
		boolean allowBillNowInsurance = BillDAO.isBillInsuranceAllowed(Bill.BILL_TYPE_PREPAID, true);
		if (tpaBills != null && tpaBills.size() > 0) {
			for(BasicDynaBean b: tpaBills) {
				String billNo = (String)b.get("bill_no");
				if(err == null) {
					EditVisitDetailsDAO.policyUpdate(con, (String)visitDetailsBean.get("primary_sponsor_id"),
									billNo, null, allowBillNowInsurance, orgId);
				}
			}
		}
		return true;
	}

	public boolean updateInsuranceApprovalAmountsForBill(Connection con, BigDecimal priApprovalAmt,
			BigDecimal secApprovalAmt, List<BasicDynaBean> visitTPABills) throws SQLException, Exception {
		boolean success = true;

		// As per 28867, A bill should be updated, if only one of it exists.
		if (visitTPABills != null && visitTPABills.size() > 1) {
			return success;
		}

		BillDAO dao = new BillDAO(con);
		Bill bill = null;
		if (visitTPABills != null && visitTPABills.size() > 0) {
			for (BasicDynaBean b : visitTPABills) {
				String billNo = (String) b.get("bill_no");
				bill = dao.getBill(billNo);
				bill.setPrimaryApprovalAmount(priApprovalAmt);
				bill.setSecondaryApprovalAmount(secApprovalAmt);
				// Update approval amt for all bills
				if (success) {
					success = dao.updateBill(bill);
				}
			}
		}
		return success;
	}

	public boolean reopenBills(Connection con, List<BasicDynaBean> tpaBills,
						String hasSecSponsor, List<String> unreopenedBills) throws SQLException, IOException {

		boolean success = true;
		BillBO billBO = new BillBO();
		ClaimDAO claimDAO = new ClaimDAO();
		HashMap actionRightsMap = new HashMap();
		HttpSession session = RequestContext.getSession();
		actionRightsMap = (HashMap)session.getAttribute("actionRightsMap");
		Object roleID = session.getAttribute("roleId");
		String actionRightStatus=(String) actionRightsMap.get(Role.BILL_REOPEN);
		String userid = (String)session.getAttribute("userid");

		GenericDAO billClaimDAO = new GenericDAO("bill_claim");

		if (roleID.equals(1) || roleID.equals(2) || actionRightStatus.equalsIgnoreCase("A")) {

			if (tpaBills != null && tpaBills.size() > 0) {
				for (BasicDynaBean bill : tpaBills) {

					String billNo = (String)bill.get("bill_no");
					boolean isClaimsent = false;

					// Reopen if bill is not open.
					if (!((String)bill.get("status")).equals(Bill.BILL_STATUS_OPEN)) {
						List<BasicDynaBean> billClaimList = billClaimDAO.findAllByKey(con, "bill_no", billNo);
						for(BasicDynaBean billClaim : billClaimList) {
							String claimId = (String)billClaim.get("claim_id");
							if (claimId != null && !claimId.equals("")) {
								GenericDAO claimSubmissionsDAO = new GenericDAO("claim_submissions");
								BasicDynaBean claimbean = claimDAO.getClaimById(claimId);
								String submissionBatchID = null;
								if (claimbean != null)
									submissionBatchID = (String)claimbean.get("last_submission_batch_id");
								
								Map<String, Object> identifiers = new HashMap<String, Object>();
								identifiers.put("claim_id", claimId);
								identifiers.put("submission_batch_id", submissionBatchID);
								BasicDynaBean claimSubmissionBean = claimSubmissionsDAO.findByKey(identifiers); 
								
								
								if (claimSubmissionBean  != null) {
									String claimStatus = (claimSubmissionBean.get("status") != null)
													? (String)claimSubmissionBean.get("status") : null;
									if (claimStatus != null && claimStatus.equals(Bill.BILL_CLAIM_SENT)) {
										unreopenedBills.add(billNo);
										isClaimsent = true;
									}
								}
							}
						}
						if(isClaimsent) continue;

						// Do not include pharmacy bill now insured bills.
						if (((String)bill.get("bill_type")).equals(Bill.BILL_TYPE_PREPAID)
								&& ((String)bill.get("restriction_type")).equals(Bill.BILL_RESTRICTION_PHARMACY)) {
							continue;
						}

						success = billBO.reopenBill(con, billNo,
								"Reopening bill for updating Insurance details.", userid, hasSecSponsor);
						new SponsorDAO().unlockBillCharges(billNo);

						if (!success) {
							return success;
						}
					}
				}
			}
		}else {
			if (tpaBills != null && tpaBills.size() > 0) {
				for (int i = 0; i < tpaBills.size(); i++) {
					BasicDynaBean bill = (BasicDynaBean)tpaBills.get(i);
					if (!((String)bill.get("status")).equals(Bill.BILL_STATUS_OPEN)) {
						tpaBills.remove(i);
					}
				}
			}
		}
		return success;
	}
}
