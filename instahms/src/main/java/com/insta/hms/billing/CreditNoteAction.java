package com.insta.hms.billing;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.patient.billing.BillMessagingJob;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.DiscountAuthorizerMaster.DiscountAuthorizerMasterAction;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.stores.RetailCustomerDAO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class CreditNoteAction extends DispatchAction{
	
	static Logger log = LoggerFactory.getLogger(CreditNoteAction.class);
	static RetailCustomerDAO rcDao = new RetailCustomerDAO();
	static JobService jobService = JobSchedulingService.getJobService();
	static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
	
	private static final GenericDAO billClaimDAO = new GenericDAO("bill_claim");
	
	public ActionForward getCreditNoteScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException, IOException, ParseException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		HttpSession session = request.getSession();
		
		String billNo = request.getParameter("billNo");
		billNo = (billNo != null) ? billNo.trim() : null;
		
		BillBO billBOObj = new BillBO();
		BillDetails billDetails = null;
		Bill bill = null;
		billDetails = billBOObj.getBillDetails(billNo);
		if (billDetails != null) {
			bill = billDetails.getBill();
		}
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", bill.getVisitId());
		
		List<BasicDynaBean> planListBean = billClaimDAO.listAll(null, "bill_no", billNo, "priority");

		int[] planIds = null;
		int planId = 0;
		if (visitBean != null) {
			planId = (Integer) visitBean.get("plan_id");
		}
		planIds = null!= planListBean && planListBean.size() > 0 ? new int[planListBean.size()] : null;

		if(planListBean != null) {
  		for(int i=0; i<planListBean.size(); i++){
  		  if(planIds != null) {
  		    planIds[i] = (Integer)planListBean.get(i).get("plan_id");
  		  }
  		}
		}
		boolean multiPlanExists = null != planListBean && planListBean.size() == 2;

		request.setAttribute("multiPlanExists", multiPlanExists);
		request.setAttribute("billDetails", billDetails);
		
		if(null != bill){
			Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(bill.getVisitId());
			request.setAttribute("patient", patientDetails);
		}
		BasicDynaBean planBean = new PlanMasterDAO().findByKey("plan_id", planId);
		request.setAttribute("planBeanJSON",
				js.serialize(planBean != null ? planBean.getMap() : null));

		PaymentModeMasterDAO paymentModeMasterDAO = new PaymentModeMasterDAO();
		List<BasicDynaBean> specificPaymentModeList = paymentModeMasterDAO.getSpecificPaymentModeDetails();
		request.setAttribute("specificPaymentModeList", js.serialize(ConversionUtils.listBeanToListMap(specificPaymentModeList)));
		
		BasicDynaBean creditNoteDetails = new BillDAO().getCreditNoteDetails(billNo);
		if(null != creditNoteDetails){
			request.setAttribute("creditNoteDetails", creditNoteDetails.getMap());
		}
		
		Boolean multiVisitBill = billBOObj.isMultiVisitBill(billNo);
		request.setAttribute("multiVisitBill", multiVisitBill ? "Y" : "N");
		if (multiVisitBill) {
			request.setAttribute("depositDetails", DepositsDAO.getPackageDepositDetails(billNo));
		} else {
			request.setAttribute("depositDetails", DepositsDAO.getBillDepositDetails(billNo, true, bill.getVisitType()));
		}
		
		boolean mod_reward_points = (Boolean)session.getAttribute("mod_reward_points");
		if (mod_reward_points && !bill.getIs_tpa())
			request.setAttribute("rewardPointDetails", RewardPointsDAO.getBillRewardPointsDetails(billNo));
		
		List<BasicDynaBean> pat_charge_amt = new BillDAO().getPatientChargeAmount(billNo); 
		request.setAttribute("pat_charge_amt", pat_charge_amt);
		
		List<BasicDynaBean> pri_charge_amt = new BillDAO().getSponsorChargeAmount(billNo, 1); 
		request.setAttribute("pri_charge_amt", pri_charge_amt);
		
		List<BasicDynaBean> sec_charge_amt = new BillDAO().getSponsorChargeAmount(billNo ,2); 
		request.setAttribute("sec_charge_amt", sec_charge_amt);
		
		List<BasicDynaBean> pri_claim_recieved_list = new BillDAO().getInsuranceRecievedAmt(billNo ,1); 
		request.setAttribute("pri_claim_recieved_list", pri_claim_recieved_list);
		
		List<BasicDynaBean> sec_claim_recieved_list = new BillDAO().getInsuranceRecievedAmt(billNo ,2); 
		request.setAttribute("sec_claim_recieved_list", sec_claim_recieved_list);
		
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		return mapping.findForward("getCreditNoteScreen");
	}
	
	public ActionForward saveCreditNoteDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException, SQLException, ParseException,Exception {
		
		GenericDAO creditNoteDAO= new GenericDAO("bill_credit_notes");
		Connection con = null;
		con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);		
		BillDAO billDAO = new BillDAO(con);
		BasicDynaBean creditBean=creditNoteDAO.getBean();
		String billNo=request.getParameter("billNo");
		HttpSession session = (HttpSession)request.getSession(false);
		Integer centerId = (Integer)session.getAttribute("centerId"); 
		BillBO billBOObj = new BillBO();
		Bill bill = billBOObj.getBill(billNo);
		CreditBillForm creditBillForm = (CreditBillForm)form;
		String userName = (String)session.getAttribute("userid");
		Map printParamMap = null;
		AbstractPaymentDetails bpImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
		String newStatus = creditBillForm.getBillStatus();
		String userid = (String)session.getAttribute("userid");
		String error = null;
		
		boolean flag = false;
		FlashScope flash = FlashScope.getScope(request);
		if(bill.getPatientWriteOff().equals("A") && request.getParameter("creditType").equals("P")) {
		    flag = true;
		    error = "Bill is already approved for Patient Write off";
		} else if(bill.getSponsorWriteOff().equals("A") && request.getParameter("creditType").equals("S")) {
		    flag = true;
		    error = "Bill is already approved for Sponsor Write off";
		}
		
		if(flag == true) {
		    ActionRedirect redirect = null;
		    redirect = new ActionRedirect(mapping.findForward("getBillscreen"));
		    redirect.addParameter("billNo", billNo);
		    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		    flash.error(error);
		    return redirect;
		}
		
		boolean allowBillNowIns = !request.getParameter("creditType").equals("P");
		String creditNo = BillDAO.getNextBillNo(con, "P", bill.getVisitType(), "N", centerId,allowBillNowIns,true);
		String creditAmt =request.getParameter("creditNoteTotalAmt");
		if (creditAmt == null || creditAmt.equals("") || creditAmt.isEmpty())
			log.error("Credit Amount is null");
		java.util.Date parsedDate = new java.util.Date();
		java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());
		
		GenericDAO creditBillDAO = new GenericDAO("bill");
		BasicDynaBean creditBill = creditBillDAO.getBean();
		creditBill.set("bill_no",creditNo);
		creditBill.set("visit_id",bill.getVisitId());
		creditBill.set("visit_type",bill.getVisitType());
		creditBill.set("username",userName);
		creditBill.set("opened_by",userName);
		creditBill.set("status","C");
		creditBill.set("discharge_status","Y");
		creditBill.set("open_date",new Timestamp(parsedDate.getTime()));
		creditBill.set("mod_time",new Timestamp(datetime.getTime()));
		creditBill.set("finalized_date",new Timestamp(parsedDate.getTime()));
		creditBill.set("last_finalized_at", new Timestamp(new java.util.Date().getTime()));
		creditBill.set("closed_date",new Timestamp(parsedDate.getTime()));
		creditBill.set("is_tpa",allowBillNowIns);
		creditBill.set("bill_rate_plan_id",bill.getBillRatePlanId());
		creditBill.set("bill_type",bill.getBillType());
		creditBill.set("payment_status","P");
		creditBill.set("remarks",request.getParameter("creditNoteRemarks"));
		creditBill.set("credit_note_reasons",request.getParameter("creditNoteReasons"));
		creditBill.set("total_amount",new BigDecimal(creditAmt).negate());
		creditBill.set("discharge_status", "Y");
		if(request.getParameter("creditType").equals("S")){
			creditBill.set("total_claim",new BigDecimal(creditAmt).negate());
		}
		
		creditBean.set("bill_no", billNo);
		creditBean.set("credit_note_bill_no", creditNo);
		
		GenericDAO billChrgDAO= new GenericDAO("bill_charge");
		ChargeDAO chargeDAOObj = new ChargeDAO(con);	
		boolean success =false;
		List insertBillChargeList = new ArrayList();
		BasicDynaBean visitBean = new VisitDetailsDAO().findByKey("patient_id", bill.getVisitId());
		int planId = 0;
		if (visitBean != null) {
			planId = (Integer) visitBean.get("plan_id");
		}

		List<BasicDynaBean> planListBean = billClaimDAO.listAll(null, "bill_no", bill.getBillNo(), "priority");
		int planIds[] = null;

		planIds = null!= planListBean && planListBean.size() > 0 ? new int[planListBean.size()] : null;

		for(int i=0; i<planListBean.size(); i++){
			planIds[i] = (Integer)planListBean.get(i).get("plan_id");
		}
		boolean multiPlanExists = null != planListBean && planListBean.size() == 2;
		
		try {
			success = creditBillDAO.insert(con, creditBill);
			success =creditNoteDAO.insert(con, creditBean);	
			
			int numCharges = 0;
			if (null != creditBillForm.getChargeId()) {
				numCharges = creditBillForm.getChargeId().length;
			}
			

			for (int j = 0; j < numCharges; j++) {
				BigDecimal creditAmt1 = BigDecimal.ZERO;
				BigDecimal[] claimAmounts = null;

				if(creditBillForm.getIsEdited()[j].equals("t") ){
					if(null != creditBillForm.getPricreditNote()[j] &&  !creditBillForm.getPricreditNote()[j].equals(""))
						creditAmt1=new BigDecimal(creditBillForm.getPricreditNote()[j]);
					if(creditAmt1.compareTo(BigDecimal.ZERO) > 0){
						String chargeId = creditBillForm.getChargeId()[j];
						BasicDynaBean bean = billChrgDAO.findByKey("charge_id", chargeId);
						bean.set("bill_no",creditNo);
			    		bean.set("charge_id", chargeDAOObj.getNextChargeId());
			    		bean.set("act_quantity",new BigDecimal(1));
			    		bean.set("act_rate", new BigDecimal(creditBillForm.getPricreditNote()[j]).negate());	
			    		bean.set("amount", new BigDecimal(creditBillForm.getPricreditNote()[j]).negate());
			    		bean.set("discount",new BigDecimal(0));
			    		bean.set("posted_date",new Timestamp(parsedDate.getTime()));
			    		bean.set("mod_time",new Timestamp(datetime.getTime()));
			    		bean.set("discount",new BigDecimal(0));
			    		bean.set("username",userName);
			    		bean.set("insurance_claim_amount",new BigDecimal(0));
			    		bean.set("claim_recd_total",new BigDecimal(0));
			    		bean.set("orig_rate",new BigDecimal(0));
			    		bean.set("return_insurance_claim_amt",new BigDecimal(0));
			    		bean.set("return_amt",new BigDecimal(0));
			    		bean.set("return_qty",new BigDecimal(0));
			    		bean.set("amount_included",new BigDecimal(0));
			    		bean.set("qty_included",new BigDecimal(0));
			    		bean.set("orig_insurance_claim_amount",new BigDecimal(0));
			    		bean.set("copay_ded_adj",new BigDecimal(0));
			    		bean.set("max_copay_adj",new BigDecimal(0));
			    		bean.set("sponsor_limit_adj",new BigDecimal(0));
			    		bean.set("copay_perc_adj",new BigDecimal(0));
			    		bean.set("code_type",creditBillForm.getCodeType()[j]);
			    		bean.set("insurance_category_id",creditBillForm.getInsuranceCategoryId()[j]);
			    		bean.set("first_of_category", creditBillForm.getFirstOfCategory()[j]);
			    		bean.set("amount_included", new BigDecimal(0));
			    		bean.set("orig_charge_id", chargeId);
			    		billChrgDAO.insert(con, bean);
			    		
			    		if(request.getParameter("creditType").equals("S")){
			    			if(creditBillForm.getInsClaimAmt() != null){
								boolean planExists = null != planIds && planIds.length > 0;
								if(planExists) {
									claimAmounts = new BigDecimal[planIds.length];
									if(multiPlanExists && planExists) {
										if(request.getParameter("sponsor_type").equals("Primary")){
											claimAmounts[0] = new BigDecimal(creditBillForm.getPricreditNote()[j]).negate();
											claimAmounts[1] = new BigDecimal(0);
										}else if(request.getParameter("sponsor_type").equals("Secondary")){
											claimAmounts[1] = new BigDecimal(creditBillForm.getPricreditNote()[j]).negate();
											claimAmounts[0] = new BigDecimal(0);
										}
									} else if(planExists){
										claimAmounts[0] = new BigDecimal(creditBillForm.getPricreditNote()[j]).negate();
									}
								}	
					    		insertBillChargeList.add(setToChargeList(bean,claimAmounts));
			    			}
			    		}
					}
				}
    		}

			BillChargeClaimDAO chargeClaimDAO = new BillChargeClaimDAO();
				
			if(request.getParameter("creditType").equals("S")) {
					chargeClaimDAO.insertBillChargeClaims(con, insertBillChargeList, planIds,
						(String)bill.getVisitId(), creditNo);
			}
			
		} catch(Exception e) {
		  success = false;
		  throw e;
		} finally{
			DataBaseUtil.commitClose(con, success);
		}
		
		String creditType = request.getParameter("creditType");
		List<Receipt> receiptList = null;
		if(request.getParameter("billingcounterId") != null && !request.getParameter("billingcounterId").isEmpty() && creditType.equals("P") ) {
		    receiptList = createCreditNoteRecipt(mapping, form, request, response,billNo);
		} 
		
		if ((receiptList != null && receiptList.size() > 0)) {
			String printerTypeStr = request.getParameter("printType");
			String customTemplate = request.getParameter("printBill"); // this you will get null if user is not having bill print rights.

			if (customTemplate != null && !customTemplate.equals("")) {
				printParamMap = new HashMap();
				printParamMap.put("printerTypeStr", printerTypeStr);
				printParamMap.put("customTemplate", customTemplate);
				printParamMap.put("billNo", creditNo);
				printParamMap.put("patient_credit_note", true);
				
				List<String> printURLs = bpImpl.generatePrintReceiptUrls(receiptList, printParamMap);
				request.getSession(false).setAttribute("printURLs", printURLs);
			}
		}
		
    /* Sending Refund SMS */
    if (MessageUtil.allowMessageNotification(request, "general_message_send") && creditType.equals("P")) {
      MessageManager mgr = new MessageManager();
      Map<String, String> smsBillData = getBillData(creditBillForm.getBillNo());
      BigDecimal refundAmt = BigDecimal.ZERO;
      if(creditBillForm.getTotPayingAmt() != null && creditBillForm.getTotPayingAmt()[0] != null) {
        refundAmt = creditBillForm.getTotPayingAmt()[0];
      }
      if (refundAmt.compareTo(BigDecimal.ZERO) == 1) {
        String amtRefund = refundAmt.toString();
        smsBillData.put("refund_amount", amtRefund);
        String messageFooterToken = "SELECT message_footer from message_types WHERE message_type_id = 'sms_bill_refund'";
        String messageFooterTokenvalue = DataBaseUtil.getStringValueFromDb(messageFooterToken);
        smsBillData.put("message_footer", messageFooterTokenvalue);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar currDate = Calendar.getInstance();
        String currentDate = dateFormat.format(currDate.getTime());
        smsBillData.put("refund_date", currentDate);
        BigDecimal patientDue = BillDAO.getPatientDue(bill.getBillNo());
        String patientDueStr=patientDue.toString();
        smsBillData.put("patient_due", patientDueStr);
        smsBillData.put("recipient_name", smsBillData.get("patient_name"));
        smsBillData.put("recipient_phone", smsBillData.get("patient_phone"));
        smsBillData.put("receipient_id__", smsBillData.get("mr_no"));
        smsBillData.put("receipient_type__", "PATIENT");
        smsBillData.put("lang_code",
            PatientDetailsDAO.getContactPreference(smsBillData.get("mr_no")));
        String currency = GenericPreferencesDAO.getGenericPreferences().getCurrencySymbol();
        smsBillData.put("currency_symbol", currency);
        if(!bill.getRestrictionType().equalsIgnoreCase(Bill.BILL_RESTRICTION_TEST)) {
          scheduleBillMessage("bill_refund_message", userName, smsBillData);
        }
      }
    }

    // Schedule accounting for credit note bill
    accountingJobScheduler.scheduleAccountingForBill(bill.getVisitId(), creditNo);

    ActionRedirect redirect = new ActionRedirect( mapping.findForward("getCreditBillingCollectScreen"));
		redirect.addParameter("billNo", creditNo);
		return redirect;
	}	
	
	 private ChargeDTO setToChargeList(BasicDynaBean bean, BigDecimal[] claimAmounts) {
		 ChargeDTO charge = new ChargeDTO();
		 charge.setChargeId((String) bean.get("charge_id"));
		 charge.setBillNo((String) bean.get("bill_no"));
		 charge.setCodeType((String) bean.get("code_type"));
		 charge.setInsuranceCategoryId((Integer) bean.get("insurance_category_id"));
		 charge.setFirstOfCategory((Boolean) bean.get("first_of_category"));
		 charge.setAmount_included((BigDecimal) bean.get("amount_included"));
		 charge.setClaimAmounts(claimAmounts);
		 charge.setPreAuthIds(null);
		 charge.setPreAuthModeIds(null);
		 charge.setIncludeInClaimCalc(null);
		 charge.setChargeGroup((String)bean.get("charge_group"));
		 charge.setChargeHead((String)bean.get("charge_head"));
		 charge.setConsultation_type_id((Integer)bean.get("consultation_type_id"));
		 charge.setOp_id((String)bean.get("op_id"));
		 charge.setActDescriptionId((String)bean.get("act_description_id"));
		return charge;
	}

	public ActionForward viewCreditNote(ActionMapping mapping, ActionForm form,
		 HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ParseException {
	     	
	    	//Connection con = null;
		//con = DataBaseUtil.getConnection();
		//con.setAutoCommit(false);		
		BillDAO billDAO = new BillDAO();
	        String billNo = request.getParameter("billNo");
		billNo = (billNo != null) ? billNo.trim() : null;
		Integer roleID = null;
		HttpSession session = request.getSession();
		JSONSerializer js = new JSONSerializer().exclude("class");
		
		if (null == billNo) {
			request.setAttribute("error", "");
			request.setAttribute("multiPlanExists", false);
			return mapping.findForward("getCreditBillingCollectScreen");
		}
		
		BillBO billBOObj = new BillBO();
		BillDetails billDetails = null;
		Bill bill = null;
		billDetails = billBOObj.getBillDetails(billNo);
		if (billDetails != null) {
			bill = billDetails.getBill();
		} else {
			request.setAttribute("error", "There is no bill with number: " + billNo);
			request.setAttribute("multiPlanExists", false);
			return mapping.findForward("getCreditBillingCollectScreen");
		}
		
		List<BasicDynaBean> templateList = BillPrintTemplateDAO.getBillTemplateList();
		request.setAttribute("templateListJSON", js.serialize(
					ConversionUtils.listBeanToListMap(templateList)));

		if (bill.getAppModified() != null && bill.getAppModified().equals("Y")) {
			roleID = (Integer)session.getAttribute("roleId");
			if (roleID != 1 && roleID != 2) {
				return mapping.findForward("userUnauthorized");
			}
		}

		request.setAttribute("availableTemplates", BillPrintTemplateDAO.getAvailableTemplatesList(
					bill.getBillType(), bill.getIs_tpa() ? "Y" : "N"));
    
		request.setAttribute("billDetails", billDetails);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		
		int patCenterId = 0;
		if (bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
			// retail customer, bed type and org are unavailable.
			log.debug("Retail visit: setting details");
			BasicDynaBean retailCustomer = rcDao.getRetailCustomerEx(bill.getVisitId());
			if (retailCustomer != null) {
				request.setAttribute("retailCustomer", retailCustomer.getMap());
				patCenterId = (Integer) retailCustomer.get("center_id");
			}
			setBillDetails(request, null, null);

		} else if (bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_INCOMING)) {
			BasicDynaBean incomingBean = OhSampleRegistrationDAO.getIncomingCustomer(bill.getVisitId());
			request.setAttribute("incomingCustomer", incomingBean);
			if (incomingBean != null) {
				patCenterId = (Integer) incomingBean.get("center_id");
			}
			setBillDetails(request, null, null);

		} else {

			log.debug("Visit type for bill " + billNo + ": " + bill.getVisitType());
			Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(bill.getVisitId());
			request.setAttribute("patient", patientDetails);
		}
		
		if(bill.getTotalClaim().compareTo(BigDecimal.ZERO) < 0) {
		    BasicDynaBean primarysponsorType = billDAO.getSponsorType(billNo, 1);
		    BasicDynaBean secondarysponsorType = billDAO.getSponsorType(billNo, 2);
		    
		    BigDecimal primarySponsorValue= (BigDecimal)primarysponsorType.get("sum");
		    BigDecimal secondarySponsorValue= (BigDecimal)secondarysponsorType.get("sum");
		    
		    if(primarysponsorType != null && primarysponsorType.get("sum") != null && primarySponsorValue.compareTo(BigDecimal.ZERO) < 0) {
			request.setAttribute("sponsorType", "Primary");
		    } else if (secondarysponsorType != null && secondarysponsorType.get("sum") != null && secondarySponsorValue.compareTo(BigDecimal.ZERO) < 0) {
			request.setAttribute("sponsorType", "Secondary");
		    }
		}
		
	     	return mapping.findForward("displaycreditNote");
         } 
	 
	 private void setBillDetails(HttpServletRequest req, String bedType, String orgid) throws SQLException {

		BillBO billBOObj = new BillBO();
		JSONSerializer js = new JSONSerializer().exclude("class");

		if ( (orgid == null) || (orgid.equals("")) )
			orgid = "ORG0001";

		if ( (bedType == null) || (bedType.equals("")) )
			bedType = "GENERAL";

		// get Constants
		List chargeGroupsList = billBOObj.getChargeGroupConstNames();
		List chargeHeadList = billBOObj.getChargeHeadConstNames();
		req.setAttribute("chargeHeadsJSON", js.serialize(chargeHeadList));
		req.setAttribute("chargeGroupsJSON", js.serialize(chargeGroupsList));
		req.setAttribute("chargeGroupConstList",chargeGroupsList);
		Preferences p = (Preferences) req.getSession().getAttribute("preferences");
		if (p != null) {
			req.setAttribute("modulesActivatedJSON", js.serialize(p.getModulesActivatedMap()));
		}

		/*
		 * Create a map of chargeHeadId => associated_module
		 */
		HashMap chargeHeadModule = new HashMap();
		Iterator it = chargeHeadList.iterator();
		while (it.hasNext()) {
			Hashtable chargeHead = (Hashtable) it.next();
			chargeHeadModule.put(chargeHead.get("CHARGEHEAD_ID"), chargeHead.get("ASSOCIATED_MODULE"));
		}
		req.setAttribute("chargeHeadModule", chargeHeadModule);

		/*
		 * JSON data for use in loading dept/ward
		 */
		req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		List<BasicDynaBean> discAuths = DiscountAuthorizerMasterAction.getDiscountAuthorizers(centerId);
		req.setAttribute("discountAuthorizersJSON", js.serialize(ConversionUtils.listBeanToListMap(discAuths)));
		req.setAttribute("discountAuthorizers", discAuths);

		req.setAttribute("discountPlansJSON", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("discount_plan_details").listAll(null,"priority"))));

		BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
		req.setAttribute("masterTimeStamp", mst.get("master_count"));

		req.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(new PaymentModeMasterDAO().listAll())));
	}
	 
	public List<Receipt> createCreditNoteRecipt(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,String billNo)
	throws IOException, ServletException, SQLException, ParseException,Exception {
	     
	    List<Receipt> receiptList = null;
	     boolean success = false;
	     Connection con = null;
	     String newStatus = null;
	     try {
		 con = DataBaseUtil.getConnection();
		 con.setAutoCommit(false);
		 BillBO billBOObj = new BillBO();
		 Bill bill = billBOObj.getBill(billNo);
		 CreditBillForm creditBillForm = (CreditBillForm)form;
		 Map<String, String[]> requestParams = request.getParameterMap();
		 AbstractPaymentDetails bpImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
		 receiptList = bpImpl.processReceiptParams(requestParams);
		 newStatus = creditBillForm.getBillStatus();
		 success = bpImpl.createReceipts(con, receiptList, bill, bill.getVisitType(), newStatus);
	     } finally {
		 DataBaseUtil.commitClose(con, success);
	     }
	    
	     return receiptList;
	}
	
  private Map<String, String> getBillData(String billNo) throws SQLException {
    BasicDynaBean billBean = BillDAO.getBillBean(billNo);
    Map<String, String> billData = new HashMap<String, String>();
    String restrictionType = (String) billBean.get("restriction_type");
    if (restrictionType.equals("N")) {

      BasicDynaBean patientBean = VisitDetailsDAO
          .getPatientVisitDetailsBean((String) billBean.get("visit_id"));

      billData.put("mr_no", (String) patientBean.get("mr_no"));
      billData.put("patient_name", (String) patientBean.get("full_name"));
      billData.put("admission_date",
          DateUtil.formatDate((java.util.Date) patientBean.get("reg_date")));
      billData.put("admission_date_yyyy_mm_dd", new DateUtil().getSqlDateFormatter()
          .format((java.util.Date) patientBean.get("reg_date")));
      billData.put("admission_time", (String) patientBean.get("reg_time").toString());
      billData.put("admission_time_12hr",
          DateUtil.formatTimeMeridiem((java.sql.Time) patientBean.get("reg_time")));
      billData.put("center_name", (String) patientBean.get("center_name"));
      billData.put("center_address", (String) patientBean.get("center_address"));
      billData.put("center_contact_phone", (String) patientBean.get("center_contact_phone"));
      billData.put("admitted_by", (String) patientBean.get("admitted_by"));
      billData.put("department", (String) patientBean.get("dept_name"));
      billData.put("patient_phone", (String) patientBean.get("patient_phone"));
      billData.put("next_of_kin_contact", (String) patientBean.get("patient_care_oftext"));
      billData.put("next_of_kin_name", (String) patientBean.get("relation"));
      billData.put("doctor_name", (String) patientBean.get("doctor_name"));
      billData.put("referal_doctor", (String) patientBean.get("refdoctorname"));
      billData.put("doctor_mobile", (String) patientBean.get("doctor_mobile"));
      billData.put("referal_doctor_mobile", (String) patientBean.get("reference_docto_id"));
      billData.put("admitting_doctor_id__", (String) patientBean.get("doctor"));
      billData.put("referal_doctor_id__", (String) patientBean.get("reference_docto_id"));
      billData.put("hospital_name", "");
      billData.put("patient_gender", (String) patientBean.get("patient_gender"));
      billData.put("incoming_visit_id", (String) patientBean.get("previous_visit_id"));
      billData.put("recipient_email", (String) patientBean.get("email_id"));

    } else if (restrictionType.equals("T")) {

      BasicDynaBean incomingPatientBean = OhSampleRegistrationDAO
          .getIncomingCustomer((String) billBean.get("visit_id"));

      billData.put("mr_no", (String) incomingPatientBean.get("mr_no"));
      billData.put("admission_date", "");
      billData.put("admission_date_yyyy_mm_dd", "");
      billData.put("admission_time", "");
      billData.put("admission_time_12hr", "");
      billData.put("center_name", "");
      billData.put("admitted_by", "");
      billData.put("department", "");
      billData.put("patient_phone", (String) incomingPatientBean.get("phone_no"));
      billData.put("next_of_kin_contact", "");
      billData.put("next_of_kin_name", "");
      billData.put("doctor_name", "");
      billData.put("referal_doctor", (String) incomingPatientBean.get("referral"));
      billData.put("doctor_mobile", "");
      billData.put("referal_doctor_mobile", "");
      billData.put("admitting_doctor_id__", "");
      billData.put("referal_doctor_id__", (String) incomingPatientBean.get("referring_doctor"));
      billData.put("hospital_name", (String) incomingPatientBean.get("hospital_name"));
      billData.put("patient_name", (String) incomingPatientBean.get("patient_name"));
      billData.put("patient_gender", (String) incomingPatientBean.get("patient_gender"));
      billData.put("incoming_visit_id", (String) incomingPatientBean.get("incoming_visit_id"));
    }

    billData.put("bill_no", (String) billBean.get("bill_no"));
    billData.put("open_date", DateUtil.formatTimestamp((java.util.Date) billBean.get("open_date")));
    billData.put("finalized_date",
        DateUtil.formatTimestamp((java.util.Date) billBean.get("finalized_date")));
    billData.put("closed_date",
        DateUtil.formatTimestamp((java.util.Date) billBean.get("closed_date")));
    billData.put("opened_by", (String) billBean.get("opened_by"));
    billData.put("closed_by", (String) billBean.get("closed_by"));
    billData.put("finalized_by", (String) billBean.get("finalized_by"));
    billData.put("total_amount", billBean.get("total_amount").toString());
    billData.put("total_amount_received", billBean.get("total_receipts").toString());
    billData.put("total_discount", billBean.get("total_discount").toString());
    billData.put("total_claim", billBean.get("total_claim").toString());
    billData.put("claim_recd_amount", billBean.get("claim_recd_amount").toString());
    billData.put("approval_amount",
        null != billBean.get("approval_amount") ? billBean.get("approval_amount").toString() : "");
    billData.put("primary_approval_amount", null != billBean.get("primary_approval_amount")
        ? billBean.get("primary_approval_amount").toString() : "");
    billData.put("secondary_approval_amount", null != billBean.get("secondary_approval_amount")
        ? billBean.get("secondary_approval_amount").toString() : "");
    billData.put("primary_total_claim", billBean.get("primary_total_claim").toString());
    billData.put("secondary_total_claim", billBean.get("secondary_total_claim").toString());
    billData.put("insurance_deduction", billBean.get("insurance_deduction").toString());
    billData.put("discount_auth",
        null != billBean.get("discount_auth") ? billBean.get("discount_auth").toString() : null);
    billData.put("disc_auth_name", (String) billBean.get("disc_auth_name"));
    billData.put("cancel_reason", (String) billBean.get("cancel_reason"));

    return billData;
  }

  private void scheduleBillMessage(String eventId, String userName, Map billData) {
    String jobMessage = null;
    jobMessage = "BillRefundSMSJob_";

    Map<String, Object> jobData = new HashMap<>();
    jobData.put("eventData", billData);
    jobData.put("userName", userName);
    jobData.put("schema", RequestContext.getSchema());
    jobData.put("eventId", eventId);
    jobService.scheduleImmediate(
        buildJob(jobMessage + billData.get("bill_no").toString(), BillMessagingJob.class, jobData));
  }
	 
}
