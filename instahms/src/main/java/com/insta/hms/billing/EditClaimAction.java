package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.stores.SalesClaimDetailsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EditClaimAction extends BaseAction {

	static Logger logger = LoggerFactory
			.getLogger(EditClaimAction.class);
	private static SalesClaimDetailsDAO salesClaimDAO = new SalesClaimDetailsDAO();
	private static final GenericDAO billDAO = new GenericDAO("bill");

	@IgnoreConfidentialFilters
	public ActionForward getClaims(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		String billNo = req.getParameter("bill_no");
		if (billNo == null || billNo.trim().equals("")) {
			return mapping.findForward("editClaim");
		}

		billNo = billNo.trim();
		BasicDynaBean billbean = billDAO.findByKey("bill_no",
				billNo);
		if (billbean == null) {
			req
					.setAttribute("error", "There is no bill with number: "
							+ billNo);
			return mapping.findForward("editClaim");
		}
		int account_group = (Integer) billbean.get("account_group");
		String visitId = (String) billbean.get("visit_id");

		/**
		 * Note : For the visit id of the bill, if follow up (F) then get the
		 * follow up visit claims. if not follow up then get the main visit
		 * claims. and get all bills of the selected visit.
		 */
		// Get Bills list
		List<BasicDynaBean> billsList = BillDAO.getVisitAccountGroupTpaBills(
				visitId, account_group);
		if(billsList !=null){
			billsList = filterNonCreditNoteBill(billsList,visitId);
		}
		BasicDynaBean visitbean = new VisitDetailsDAO().findByKey("patient_id",
				visitId);
		String op_type = (String) visitbean.get("op_type");
		String main_visit_id = (String) visitbean.get("main_visit_id");
		String patient_id = (String) visitbean.get("patient_id");

		// Get Claims list
		List<BasicDynaBean> claimsList = ClaimDAO.searchClaims(main_visit_id,
				patient_id, op_type, account_group);
		req.setAttribute("claimsList", claimsList);
		req.setAttribute("billsList", billsList);
		req.setAttribute("visitId", visitId);
		req.setAttribute("billNo", billNo);
		return mapping.findForward("editClaim");
	}

	private List<BasicDynaBean> filterNonCreditNoteBill(List<BasicDynaBean> billsList, String visitId) throws SQLException {
		List<BasicDynaBean> listBeans = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> filterListBeans = new ArrayList<BasicDynaBean>();
		filterListBeans.addAll(billsList);
		List<BasicDynaBean> billbeans = billDAO.findAllByKey("visit_id",visitId);
		if(billbeans != null){
			for(BasicDynaBean billbean: billbeans){
				listBeans= new GenericDAO("bill_credit_notes").findAllByKey("bill_no", (String)billbean.get("bill_no"));
				if(listBeans !=null){
					for(BasicDynaBean b: listBeans){
						if (billsList != null && billsList.size() > 0) {
							for (int i = 0; i < billsList.size(); i++) {
								BasicDynaBean bill = (BasicDynaBean)billsList.get(i);
								if(((String)b.get("credit_note_bill_no")).equals(((String)bill.get("bill_no")))){
									filterListBeans.remove(bill);
								}
							}
						}
					}
				}
			}
			
		}
		
		return filterListBeans;
	}

	public ActionForward createNewClaim(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, Exception {

		Map map = request.getParameterMap();
		String visitId = request.getParameter("patient_id");
		String billno = request.getParameter("bill_no");
		BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("patient_id", visitId);
		redirect.addParameter("bill_no", billno);

		FlashScope flash = FlashScope.getScope(request);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		BasicDynaBean visitbean = new VisitDetailsDAO().findByKey("patient_id", visitId);

		if (visitbean == null || visitId == null) {
			flash.error("No Patient with Id:" + visitId);
			return redirect;
		}

		Connection con = null;
		Boolean success = false;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			GenericDAO billClaimDAO = new GenericDAO("bill_claim");

			String mainVisitId = visitbean.get("main_visit_id") != null ? (String) visitbean.get("main_visit_id") : null;
			String patientId = visitbean.get("patient_id") != null ? (String) visitbean.get("patient_id") : null;
			String opType = visitbean.get("op_type") != null ? (String) visitbean.get("op_type") : null;
			String tpaId = visitbean.get("primary_sponsor_id") != null ? (String) visitbean.get("primary_sponsor_id") : null;
			if (mainVisitId == null || patientId == null) {
				flash.error("Invalid Visit/Main Visit Id. Cannot create/update claim.");
				return redirect;
			}

			if (tpaId == null || tpaId.equals("")) {
				flash.error("Visit is not connected to TPA. Cannot create/update claim.");
				return redirect;
			}

			String[] ClaimIds = (String[]) map.get("selected_claim_id");
			String[] planIds = (String[]) map.get("plan_id");

			if (ClaimIds != null) {
				for (int i = 0; i < ClaimIds.length; i++) {

					String claimId = ClaimIds[i];
					int planId = Integer.parseInt(planIds[i]);
					if (!claimId.equals("")) {
						if (claimId.endsWith("New")) {
							String[] billNo = claimId.split("_");
							BillDAO billdao = new BillDAO(con);
							Bill bill = billdao.getBill(billNo[0]);
							Map<String,Object> keys = new HashMap<String, Object>();
							keys.put("bill_no", billNo[0]);
							keys.put("plan_id", planId);
							String oldClaimId = (String)billClaimDAO.findByKey(con,keys).get("claim_id");

							// TODO : Review -> Using Bill account group
							int account_group = bill.getAccount_group();

							ClaimDAO claimdao = new ClaimDAO();
							BasicDynaBean claimbean = null;

							int centerId = VisitDetailsDAO.getCenterId(con, visitId);
							String new_claim_id = claimdao.getGeneratedClaimIdBasedonCenterId(centerId, account_group);

							claimbean = claimdao.getBean();
							claimbean.set("claim_id", new_claim_id);
							claimbean.set("main_visit_id", mainVisitId);
							claimbean.set("patient_id", patientId);
							claimbean.set("op_type", opType);
							claimbean.set("status", "O");
							claimbean.set("account_group", account_group);
							claimbean.set("plan_id", planId);
							success = claimdao.insert(con, claimbean);

							success = billChgClaimDAO.updateClaimIds(con, billNo[0], new_claim_id, planId);


							List<BasicDynaBean> salesDetails = salesClaimDAO.getSalesClaimDetails(con, billNo[0],oldClaimId);
							salesClaimDAO.updateSalesClaimId(con, salesDetails,new_claim_id);

						} else {

							String[] ClaimandBillNo = claimId.split("_");
							Map<String,Object> keys = new HashMap<String, Object>();
							keys.put("bill_no", ClaimandBillNo[1]);
							keys.put("plan_id", planId);
							String oldClaimId = (String)billClaimDAO.findByKey(con,keys).get("claim_id");

							String claim_no = ClaimandBillNo[0];
							String bill_no = ClaimandBillNo[1];

							success = billChgClaimDAO.updateClaimIds(con, bill_no, claim_no, planId);

							List<BasicDynaBean> salesDetails = salesClaimDAO.getSalesClaimDetails(con, bill_no,oldClaimId);
							salesClaimDAO.updateSalesClaimId(con, salesDetails,claim_no);
						}
					}
				}
			}
			return redirect;

		} finally {
			DataBaseUtil.commitClose(con, success);
		}
	}
}
