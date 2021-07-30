
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DynaPackage.DynaPackageDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;
import com.insta.hms.stores.StockPatientIssueReturnsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class ConnectDisconnectTPA extends DispatchAction {

	static Logger logger = LoggerFactory.getLogger(ConnectDisconnectTPA.class);

	private static final SponsorDAO sponsorDAO = new SponsorDAO();
	private static final SponsorBO sponsorBO = new SponsorBO();
	private static final BillChargeClaimDAO billChargeClaimDAO = new BillChargeClaimDAO();
	private static final GenericDAO billDAO = new GenericDAO("bill");
	private static final GenericDAO billClaimDAO = new GenericDAO("bill_claim");
	private static final VisitDetailsDAO visitDao = new VisitDetailsDAO();
	private static final BillBO billBo = new BillBO();
	private static final StockPatientIssueReturnsDAO stockPatientIssueReturnsDAO = new StockPatientIssueReturnsDAO();
	private static final GenericDAO stockPatientIssueDao = new GenericDAO("stock_issue_details");
	private static final BillingHelper billingHelper = new BillingHelper();
	private static final BillChargeTaxDAO billChargeTaxDAO = new BillChargeTaxDAO();
	
	private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
	
	public ActionForward getTPAConnectDisconnectScreen(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res) throws SQLException {

		String bill_no = req.getParameter("billNo");
		String isNewUX = req.getParameter("isNewUX");
		BasicDynaBean bill = billDAO.findByKey("bill_no", bill_no);
		req.setAttribute("isTpa", (Boolean)bill.get("is_tpa"));
		req.setAttribute("isNewUX", isNewUX);

		return m.findForward("connectDisconnectTpa");
	}

	public ActionForward tpaConnectDisconnect(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws IOException, SQLException, Exception {

		String billNo = req.getParameter("billNo");
		String isNewUX = req.getParameter("isNewUX");
		Connection con = null;
		boolean success = false;
		boolean isTpa = false;
		boolean hasDynaPkgId = false;

		ClaimDAO claimdao = new ClaimDAO();
		SponsorDAO sponsorDAO = new SponsorDAO();
		HttpSession session = req.getSession(false);
		String userName = (String)session.getAttribute("userid");
		try {
tpaupdate:
		   {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BasicDynaBean billBean = billDAO.findByKey("bill_no", billNo);

	    	if(billBean == null || billBean.get("bill_no")==null) {
				req.setAttribute("error", "There is no bill with number: "+billNo);
				return m.findForward("connectDisconnectTpa");
	    	}

	    	if (billBean != null) {
	    		String visitId      = (String)billBean.get("visit_id");
	    		String billStatus   = (String)billBean.get("status");
	    		String billType     = (String)billBean.get("bill_type");
	    		isTpa               = (Boolean)billBean.get("is_tpa");
	    		if((int)billBean.get("dyna_package_id") !=0 && !isTpa)
	    		  hasDynaPkgId =true;

	    		BasicDynaBean visitbean = new VisitDetailsDAO().findByKey("patient_id", visitId);
	    		String tpaId = (String)visitbean.get("primary_sponsor_id");
	    		String mainVisitId = visitbean.get("main_visit_id") != null ? (String)visitbean.get("main_visit_id") : null;
	    		String patientId   = visitbean.get("patient_id") != null ? (String)visitbean.get("patient_id") : null;
	    		String visitType   = visitbean.get("visit_type") != null ? (String)visitbean.get("visit_type") : null;
	    		String useDRG 	   = visitbean.get("use_drg") != null ? (String)visitbean.get("use_drg") : "N";
	    		String secSponsor  = (visitbean.get("secondary_sponsor_id") != null
										&& !visitbean.get("secondary_sponsor_id").equals(""))
										? (String)visitbean.get("secondary_sponsor_id") : null;
				int tpaBillsCount = BillDAO.getVisitTpaBills(patientId);

				PatientInsurancePlanDAO insPlanDAO = new PatientInsurancePlanDAO();
				int[] planIds = insPlanDAO.getPlanIds(con,visitId);
				boolean multiPlanExisits = null != planIds && planIds.length == 2;

				List<BasicDynaBean> tpaBills = BillDAO.getVisitBills(patientId, BillDAO.bill_type.BOTH, false, true);
	    		Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);

	    		String useperdiem  = visitbean.get("use_perdiem") != null ? (String)visitbean.get("use_perdiem") : "N";
	    		Map perdiemCodeMap = new MRDUpdateScreenBO().getPerdiemCode(visitId);

	    		String claim_id = null;

	    		if(mainVisitId == null || patientId == null) {
					req.setAttribute("error", "Invalid Visit/Main Visit Id. Cannot connect " +billNo+ " to sponsor.");
					return m.findForward("connectDisconnectTpa");
		    	}

	    		if(tpaId == null || tpaId.equals("")) {
					req.setAttribute("error", "Visit is not connected to sponsor. Cannot connect " +billNo+ " to sponsor.");
					return m.findForward("connectDisconnectTpa");
		    	}

	    		if(!billStatus.equals("A")) {
					req.setAttribute("error", "Bill is not Open. Cannot connect " +billNo+ " to sponsor.");
					return m.findForward("connectDisconnectTpa");
		    	}

	    		if(useDRG.equals("Y") && drgCodeMap != null
	    				&& drgCodeMap.get("drg_code") != null && !drgCodeMap.get("drg_code").equals("")
	    				&& !billNo.equals((String)drgCodeMap.get("drg_bill_no")) ) {
	    			String drgBillNo = (String)drgCodeMap.get("drg_bill_no");
					req.setAttribute("error", "Patient has DRG code required. Cannot connect this bill: " +billNo+ " to sponsor </br>" +
											  " Please check bill no. "+drgBillNo+ ". Cancel DRG Code and " +
											  " Disconnect bill no. "+drgBillNo+" from sponsor to connect this bill: " +billNo+ " to sponsor ");
					return m.findForward("connectDisconnectTpa");
		    	}

	    		if(useperdiem.equals("Y") && perdiemCodeMap != null
	    				&& perdiemCodeMap.get("per_diem_code") != null && !perdiemCodeMap.get("per_diem_code").equals("")
	    				&& !billNo.equals((String)perdiemCodeMap.get("perdiem_bill_no")) ) {
	    			String perdiemBillNo = (String)perdiemCodeMap.get("perdiem_bill_no");
					req.setAttribute("error", "This patient is a perdiem patient. Cannot connect this bill: " +billNo+ " to sponsor </br>" +
											  " Please check bill no. "+perdiemBillNo+ ". Cancel Perdiem Code and " +
											  " Disconnect bill no. "+perdiemBillNo+" from sponsor to connect this bill: " +billNo+ " to sponsor ");
					return m.findForward("connectDisconnectTpa");
		    	}

	    		boolean mod_adv_ins	= (Boolean)session.getAttribute("mod_adv_ins");
	    		BasicDynaBean planDetails = new PatientInsurancePlanDAO().getVisitPrimaryPlan(con, patientId);

	    		if (billStatus.equals("A") && tpaId != null && !tpaId.equals("")) {

    				if (!isTpa) {
    					boolean allowBillInsurance = BillDAO.isBillInsuranceAllowed(billType, true);

    					if(secSponsor != null && !multiPlanExisits) {
    						if (billType.equals("P") && !allowBillInsurance) {
	    						req.setAttribute("error", "Visit has secondary sponsor (Bill now insurance not allowed). Cannot connect this bill now bill: " +billNo+ " to sponsor." );
	    						return m.findForward("connectDisconnectTpa");
    						}

    						if (tpaBillsCount >= 1) {
	    						req.setAttribute("error", "Visit has secondary sponsor and a TPA bill. Cannot connect this bill: " +billNo+ " to sponsor </br>" +
	    												  " Disconnect visit bills from sponsor and connect this bill: " +billNo+ " to sponsor ");
	    						return m.findForward("connectDisconnectTpa");
    						}
    			    	}

    					// Connect TPA
						Map fields = new HashMap();
						fields.put("is_tpa", allowBillInsurance);
						fields.put("bill_rate_plan_id", (String)visitbean.get("org_id"));
						fields.put("discount_category_id", 0);

						//discount plan reference in the bill
						fields.put("discount_category_id", planDetails != null && planDetails.get("discount_plan_id") != null
			    				? (Integer)planDetails.get("discount_plan_id") : 0 );

						Map keys = new HashMap();
						keys.put("bill_no", billBean.get("bill_no"));

						setDynaPackageCharge(fields, (String)visitbean.get("org_id"), billBean, visitbean);

						success = DataBaseUtil.dynaUpdate(con, "bill", fields, keys) > 0;

						if (!success)
							break tpaupdate;

						EditVisitDetailsDAO.map = new HashMap<Integer, Integer>();
						billingHelper.resetInventoryCharges(con,mainVisitId);
						success = EditVisitDetailsDAO.checkDRGUpdateBillChargesForPolicy(con, patientId, true, (String)billBean.get("bill_no"), planIds);
						billingHelper.replayInventoryReturns(con,mainVisitId);
						if(null != planIds) {
							billChargeClaimDAO.changesToBillChargeClaim(con,  (String)billBean.get("bill_no"), visitId, planIds, visitType);
						}

						if (!success)
							break tpaupdate;

    				}else {
    					// Disconnect TPA
    					if (mod_adv_ins) {
    						List<BasicDynaBean> billClaimList = billClaimDAO.findAllByKey("bill_no", (String)billBean.get("bill_no"));
    						for(BasicDynaBean billClaim : billClaimList) {
    							claim_id = (String)billClaim.get("claim_id");
    							if (claim_id != null && !claim_id.equals("")) {
    		    					BasicDynaBean claimbean = claimdao.getClaimById(claim_id);
    		    					if (claimbean != null && !claimbean.get("status").equals("O")) {
    		    						req.setAttribute("error", "Bill is part of claim which is not Open. Cannot disconnect " +billNo+ " from sponsor.");
    		    						return m.findForward("connectDisconnectTpa");
    		    					}
    	    					}
    						}
    					}

    					String p_claim_status = (String) billBean.get("primary_claim_status");
    					String s_claim_status = (String) billBean.get("secondary_claim_status");
    					if ( (p_claim_status != null && p_claim_status.equals("S"))
								|| (s_claim_status != null && s_claim_status.equals("S")) ) {
    						req.setAttribute("error", "Bill claim status is Sent. Cannot disconnect " +billNo+ " from sponsor.");
    						return m.findForward("connectDisconnectTpa");
    					}

    					if(useDRG.equals("Y") && drgCodeMap != null
    							&& drgCodeMap.get("drg_code") != null && !drgCodeMap.get("drg_code").equals("")
    		    				&& billNo.equals((String)drgCodeMap.get("drg_bill_no")) ) {
    						req.setAttribute("error", "Patient has DRG code required. Cannot disconnect " +billNo+ " from TPA. </br>" +
    												  " Cancel DRG Code to disconnect from sponsor. ");
    						return m.findForward("connectDisconnectTpa");
    			    	}

    					if(useperdiem.equals("Y") && perdiemCodeMap != null
    							&& perdiemCodeMap.get("per_diem_code") != null && !perdiemCodeMap.get("per_diem_code").equals("")
    		    				&& billNo.equals((String)perdiemCodeMap.get("perdiem_bill_no")) ) {
    						req.setAttribute("error", "This patient is a perdiem patient. Cannot disconnect " +billNo+ " from TPA. </br>" +
    												  " Cancel Perdiem code to disconnect from sponsor. ");
    						return m.findForward("connectDisconnectTpa");
    			    	}

    					// Update claim_id.
						Map<String ,Object> fields = new HashMap<String, Object>();

						fields.put("is_tpa", false);
						fields.put("discount_category_id", 0);

						Integer centerId = (Integer)visitbean.get("center_id");
						String prefRatePlan =  CenterPreferencesDAO.getRatePlanForNonInsuredBills(centerId);

						String ratePlanId;
						if (visitbean.get("primary_sponsor_id") != null && prefRatePlan != null) {
							ratePlanId = prefRatePlan;
						} else {
							ratePlanId = (String) visitbean.get("org_id");
						}

						fields.put("bill_rate_plan_id", ratePlanId);
						Map<String ,Object> keys = new HashMap<String ,Object>();
						keys.put("bill_no", billBean.get("bill_no"));

						setDynaPackageCharge(fields, ratePlanId, billBean, visitbean);
						success = DataBaseUtil.dynaUpdate(con, "bill", fields, keys) > 0;
						ChargeDAO chargeDAO = new ChargeDAO(con);
						List<ChargeDTO> chargeList = chargeDAO.getBillCharges(billNo);
						billChargeTaxDAO.calculateAndUpdateBillChargeTaxes(con, chargeList, billBean);
						if (!success)
							break tpaupdate;
						billingHelper.resetInventoryCharges(con, mainVisitId);
						success = EditVisitDetailsDAO.updateBillChargesForPolicyNew(con, patientId, false, (String)billBean.get("bill_no"), null);
						billingHelper.replayInventoryReturns(con, mainVisitId);
						billClaimDAO.delete(con, "bill_no", (String)billBean.get("bill_no"));
						billChargeClaimDAO.delete(con, "bill_no", (String)billBean.get("bill_no"));

						new BillChargeTaxDAO().deleteClaimTax(con, (String)billBean.get("bill_no"));
						sponsorDAO.updateSalesClaimDetails((String)billBean.get("bill_no"));
						sponsorDAO.updateSalesClaimTaxDetails((String)billBean.get("bill_no"));

						if (!success)
							break tpaupdate;
    				}
	    		}
	    	 }// if
		  } // label tpaupdate

		  if (!success) {
				if (!isTpa) {
					req.setAttribute("error", "Error while connecting " +billNo+ " to sponsor. ");
				} else {
					req.setAttribute("error", "Error while disconnecting " +billNo+ " to sponsor. ");
				}
				return m.findForward("connectDisconnectTpa");
		   }
		} catch (Exception e) {
			throw e;
		} finally {
			DataBaseUtil.commitClose(con, success);
			 if (success && StringUtils.isNotBlank(billNo)) {
				BillDAO.resetTotalsOrReProcessNew(billNo);
			 }
		}

		String visitId = BillDAO.getVisitId(billNo);

		if(success && null != visitId && !visitId.equals("")){
			sponsorDAO.unlockVisitSaleItems(visitId);
			sponsorBO.recalculateSponsorAmount(visitId);
			sponsorDAO.setIssueReturnsClaimAmountTOZero(visitId);
			sponsorDAO.insertOrUpdateBillChargeTaxesForSales(visitId);
			sponsorDAO.lockVisitSaleItems(visitId);
			sponsorDAO.updateSalesBillCharges(visitId);
			sponsorDAO.updateTaxDetails(visitId);
			BillDAO.resetRoundOff(billNo);
		    // Call the Allocation method to update bill details.
            Integer centerId = (Integer) req.getSession(false).getAttribute("centerId");
            allocationService.allocate(billNo, centerId);
		}

		if (isNewUX.equals("Y")) {
			req.setAttribute("billNo", billNo);
			req.setAttribute("isNewUX", isNewUX);
			req.setAttribute("isNewUXSuccess", "Y");
			return m.findForward("connectDisconnectTpa");
		}
		ActionRedirect redirect = new ActionRedirect(m.findForward("showlist"));
		redirect.addParameter("billNo", billNo);
		FlashScope flash = FlashScope.getScope(req);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("hasDynaPkg", hasDynaPkgId);
		return redirect;
	}

	private Map setDynaPackageCharge(Map fields, String ratePlan, BasicDynaBean billBean, BasicDynaBean visitbean) throws Exception {
		if ((int) billBean.get("dyna_package_id") != 0) {
			List<String> charges = DynaPackageDAO.getDynaPackageCharge(
					(int)billBean.get("dyna_package_id"), (String)visitbean.get("org_id"),
					(String)visitbean.get("bed_type"));
			if(!charges.isEmpty() && StringUtils.isNotBlank(charges.get(0))) {
				fields.put("dyna_package_charge", Double.parseDouble(charges.get(0)));
			}else {
				fields.put("dyna_package_charge", BigDecimal.ZERO);
			}
		}
		return fields;
	}
}
