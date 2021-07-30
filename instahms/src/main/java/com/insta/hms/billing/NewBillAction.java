package com.insta.hms.billing;

import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

	public class NewBillAction extends DispatchAction{
		
		@IgnoreConfidentialFilters
		public ActionForward getNewPrepaidBillScreen(ActionMapping mapping,ActionForm form,HttpServletRequest req,
				HttpServletResponse res) throws IOException, ServletException,SQLException{
			saveToken(req);
			return mapping.findForward("getNewBillScreen");
	}


	public ActionForward createNewBill(ActionMapping mapping, ActionForm form, HttpServletRequest req,
			HttpServletResponse res) throws IOException, ServletException, SQLException, ParseException{
		BillBO bo=new BillBO();
		String visitId = req.getParameter("visitId");
		String billType= req.getParameter("creditprepaid");
		String isTpa= req.getParameter("istpa");
		HttpSession session = req.getSession(false);
		String userid = (String)session.getAttribute("userid");
		BasicDynaBean patient = new GenericDAO("patient_registration").findByKey("patient_id", visitId);
		String tpa_id = patient.get("primary_sponsor_id") != null ? (String)patient.get("primary_sponsor_id") :null;
		Integer centerId = (Integer)patient.get("center_id");

		boolean is_tpa = isTpa != null && isTpa.equals("Y") && tpa_id != null && !tpa_id.equals("");
		boolean allowBillInsurance = BillDAO.isBillInsuranceAllowed(billType, is_tpa);
		String orgId = (String)patient.get("org_id");
		String prefRatePlan =  CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);

		if((patient.get("primary_sponsor_id") != null && !patient.get("primary_sponsor_id").equals(""))
				&& !allowBillInsurance && prefRatePlan != null)
			orgId = prefRatePlan;

		Bill bill = new Bill();
		bill.setBillType(billType);
		bill.setOpenDate(new java.util.Date());
		bill.setStatus("A");
		bill.setOpenedBy(userid);
		bill.setUserName(userid);
		bill.setIs_tpa(allowBillInsurance);
		bill.setIsPrimaryBill("N");
		bill.setVisitId(visitId);
		bill.setVisitType((String)patient.get("visit_type"));
		bill.setBillRatePlanId(orgId);

		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(req);

		if (isTokenValid(req, true)) {
			String errorMsg = bo.createNewBill(bill, true);
			if (errorMsg == null) {

				// New bill's deduction and claim amount should be reset newly.
				if (bill.getBillNo() != null && !bill.getBillNo().equals(""))
					BillDAO.setDeductionAndSponsorClaimTotals(bill.getBillNo());

				redirect = new ActionRedirect(mapping.findForward("getbillingscreen"));
				redirect.addParameter("billNo", bill.getBillNo());
			} else {
				flash.error("Error while creating bill: " + errorMsg);
				redirect = new ActionRedirect(mapping.findForward("getScreen"));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			}
		} else {
			redirect = new ActionRedirect(mapping.findForward("getScreen"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.error("Bill created enter mrno for another bill");
		}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getPatientVisitDetails(ActionMapping mapping, ActionForm form,HttpServletRequest request,
            HttpServletResponse response)throws IOException, ServletException, SQLException, ParseException {

		String visitId = request.getParameter("patient_id");
		HttpSession session = (HttpSession) request.getSession(false);
		Integer centerId = (Integer) session.getAttribute("centerId");
		Map visitbean = null;
		BasicDynaBean patientBean = VisitDetailsDAO.getDetailsForMrnoOrVisitId(visitId);
		if (patientBean != null) {
			if (patientBean.get("visit_id") == null || patientBean.get("visit_id").equals("")) {
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("getScreen"));
				FlashScope flash = FlashScope.getScope(request);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				flash.error("No Patient with Id:"+visitId);
		    	return redirect;
			} else {
				if(centerId != 0){
					visitbean = VisitDetailsDAO.getPatientVisitDetailsMapByCenter((String)patientBean.get("visit_id"), centerId);
				}else{
					visitbean = VisitDetailsDAO.getPatientVisitDetailsMap((String)patientBean.get("visit_id"));
				}
				if (visitbean == null) {
					ActionRedirect redirect = new ActionRedirect(mapping.findForward("getScreen"));
					FlashScope flash = FlashScope.getScope(request);
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					flash.error("No Patient with Id:"+visitId);
			    	return redirect;
				}
			}
		} else {
			if(centerId != 0){
				visitbean = VisitDetailsDAO.getPatientVisitDetailsMapByCenter(visitId,centerId);
			}else{
				visitbean = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
			}
			if (visitbean == null) {
				ActionRedirect redirect = new ActionRedirect(mapping.findForward("getScreen"));
				FlashScope flash = FlashScope.getScope(request);
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				flash.error("No Patient with Id:"+visitId);
		    	return redirect;
			}
		}

		visitId = (String)visitbean.get("patient_id");

		 java.util.HashMap actionRightsMap = (java.util.HashMap) session.getAttribute("actionRightsMap");
		int roleid = (Integer) session.getAttribute("roleId");
		if ((roleid>2) && (actionRightsMap.get("create_bill_for_closed_visit")== null
				|| !actionRightsMap.get("create_bill_for_closed_visit").equals("A"))
				&&  visitbean.get("visit_status").equals("I")){
			ActionRedirect redirect = new ActionRedirect(mapping.findForward("getScreen"));
			FlashScope flash = FlashScope.getScope(request);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			flash.error("Rights to create bill for inactive visit:"+visitId+ " not available.");
	    	return redirect;
		}

		request.setAttribute("visitbean", visitbean);
		String visitType = (String) visitbean.get("visit_type");

		PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
		int[] planIds = insPlanDAO.getPlanIds(visitId);
		boolean multiPlanExisits = null != planIds && planIds.length == 2;
		boolean hasPlanVisitCopayLimit = false;
		if(null != planIds){
			for(int i=0; i<planIds.length; i++){
				hasPlanVisitCopayLimit = new PlanMasterDAO().hasPlanVisitCopayLimit(planIds[i], visitType);
				if(hasPlanVisitCopayLimit) break;
			}
		}

		request.setAttribute("hasPlanVisitCopayLimit", hasPlanVisitCopayLimit);
		request.setAttribute("multiPlanExisits", multiPlanExisits);
		request.setAttribute("allowBillNowInsurance", BillDAO.isBillInsuranceAllowed(Bill.BILL_TYPE_PREPAID, true));
		return mapping.findForward("getNewBillScreen");
	}
}
