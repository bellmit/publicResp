/**
 *
 */
package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.insurance.claimhistory.ClaimHistoryService;
import com.insta.hms.insurance.RemittanceAdviceDAO;
import com.insta.hms.master.Accounting.AccountingGroupMasterDAO;
import com.insta.hms.master.Accounting.AccountingPrefsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.usermanager.Role;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lakshmi.p
 *
 */
public class ClaimReconciliationAction extends BaseAction {

	private static final Logger logger = LoggerFactory.getLogger(ClaimReconciliationAction.class);

	private static final ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
	private static final ClaimDAO claimdao = new ClaimDAO();
	private static final GenericDAO claimSubmissionsDAO = new GenericDAO("claim_submissions");
	private static final AccountingPrefsDAO acPrefsDAO = new AccountingPrefsDAO();
	private static final AccountingGroupMasterDAO accGroupDAO = new AccountingGroupMasterDAO();
	private static final RemittanceAdviceDAO remdao = new RemittanceAdviceDAO();
	private static final GenericDAO charge = new GenericDAO("bill_charge");
	private static final GenericDAO item = new GenericDAO("store_sales_details");
	private static final GenericDAO saleClaimDAO = new GenericDAO("sales_claim_details");
	private static final BillChargeClaimDAO bccDao = new BillChargeClaimDAO();
	private static final BillBO billBO = new BillBO();
	private static final AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
	private static final ClaimHistoryService claimHistoryService = ApplicationContextProvider
      .getBean(ClaimHistoryService.class);
	private static final GenericDAO accountGroupAndCenterViewDao =
			new GenericDAO("accountgrp_and_center_view");
	private static final ClaimReconciliationDAO claimReconciliationDao =
			new ClaimReconciliationDAO();
	private static final VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();
	private static final ClaimActivityDAO claimActivityDao = new ClaimActivityDAO();
	private static final BillDAO billDao = new BillDAO();
	private static final CenterMasterDAO centerMasterDao = new CenterMasterDAO();
	private static final GenericDAO rejectionReasonCategories =
			new GenericDAO("rejection_reason_categories");
	private static final ClaimSubmissionDAO claimSubmissionDao = new ClaimSubmissionDAO();
	private static final BillClaimDAO billClaimDao = new BillClaimDAO();
	private static final JSONSerializer js = new JSONSerializer().exclude("class");
	private static final GenericDAO tpaMasterDao = new GenericDAO("tpa_master");


	@IgnoreConfidentialFilters
	public ActionForward list(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		Map map= getParameterMap(req);

		if (req.getParameter("submission_batch_id")!=null && !req.getParameter("submission_batch_id").equals("")) {
			String[] submission_batch_id = {ClaimSubmissionDAO.fillSubmissionIDSearch(req.getParameter("submission_batch_id"))};
			map.put("submission_batch_id", submission_batch_id);
		}
		if (req.getParameter("claim_id")!=null && !req.getParameter("claim_id").equals("")) {
			String[] claim_id = {ClaimDAO.fillClaimIDSearch(req.getParameter("claim_id"))};
			map.put("claim_id", claim_id);
		}

		map.remove("center_or_account_group");
		map.remove("center_or_account_group_id");


		String centerOrAccountGroup   = req.getParameter("center_or_account_group_id");
		String accGrpOrCenterIdStr = (centerOrAccountGroup == null || centerOrAccountGroup.equals("")) ? "" : (centerOrAccountGroup.toString());
		int accGrpId = 0;
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		if (!accGrpOrCenterIdStr.equals("")) {
			if (accGrpOrCenterIdStr.startsWith("A")) {
				accGrpId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
			}else if (accGrpOrCenterIdStr.startsWith("C")) {
				centerId = Integer.parseInt(accGrpOrCenterIdStr.substring(1, accGrpOrCenterIdStr.length()));
			}

			if (accGrpId != 0) {
				map.put("account_group", new String[]{accGrpId+""});
				map.put("account_group@type", new String[]{"integer"});
			}else {
				map.put("center_id", new String[]{centerId+""});
				map.put("center_id@type", new String[]{"integer"});
			}
		}
		// setting the dropdown for Center/Account Group
		int userCenterId = (Integer) req.getSession(false).getAttribute("centerId");
		List<BasicDynaBean> accGrpAndCenterList = accountGroupAndCenterViewDao.listAll();
		boolean filterDisp_flag = false;
		Map accGrpAndCenterType = null;
		List<BasicDynaBean> accGrpAndCenterDropdn = new ArrayList<BasicDynaBean>();

		if(userCenterId != 0){
			for(int i=0;i<accGrpAndCenterList.size();i++){
				accGrpAndCenterType = new HashMap(((BasicDynaBean)accGrpAndCenterList.get(i)).getMap());
				String type=(String)accGrpAndCenterType.get("type");
				if(type.equals("C")){
					filterDisp_flag = true;
					int ac_id = Integer.parseInt(accGrpAndCenterType.get("ac_id")+"");
						if(ac_id == userCenterId){
							accGrpAndCenterDropdn.add( (BasicDynaBean)accGrpAndCenterList.get(i) ) ;
						}
				}
				else{
					if(null != accGrpAndCenterType.get("store_center_id")) {
						int storeCenterId = (Integer)accGrpAndCenterType.get("store_center_id");
						if(userCenterId == storeCenterId)
							accGrpAndCenterDropdn.add(accGrpAndCenterList.get(i)) ;
					} else {
						accGrpAndCenterDropdn.add(accGrpAndCenterList.get(i));
					}
				}
			}
		}

		if(userCenterId == 0) accGrpAndCenterDropdn = accGrpAndCenterList;
		if(filterDisp_flag){
			if(userCenterId != 0){
				map.put("center_id",new String[]{userCenterId+""});
				map.put("center_id@type", new String[]{"integer"});
			}
		}
		
		String date_range = req.getParameter("date_range");
		String monthdate = null;
		if (date_range != null && date_range.equals("month")) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	        Calendar cal = Calendar.getInstance();
	        cal.add(Calendar.DATE, -30);
	        Date openDt = cal.getTime();
	        monthdate = dateFormat.format(openDt);

			map.put("submission_date", new String[]{monthdate, ""});
			map.put("submission_date@op", new String[]{"ge,le"});
			map.put("submission_date@cast", new String[]{"y"});
			map.remove("date_range");
		}

		PagedList list = claimReconciliationDao
				.searchClaimReconciliations(map, ConversionUtils.getListingParameter(map));
		req.setAttribute("pagedList", list);
		List<String> columns = new ArrayList<>();
		columns.add("insurance_co_id");
		columns.add("insurance_co_name");


		columns.clear();
		columns.add("tpa_name");
		columns.add("tpa_id");
		req.setAttribute("tpaList",js.serialize(ConversionUtils.listBeanToListMap(
				tpaMasterDao.listAll(columns, "status", "A", "tpa_name"))));
		req.setAttribute("insCategoryList", js.serialize(ConversionUtils.listBeanToListMap(
				InsuranceCategoryMasterDAO.getInsCatCenter(centerId))));
		columns.clear();
		columns.add("plan_id");
		columns.add("plan_name");
		columns.add("category_id");

		req.setAttribute("acc_prefs", acPrefsDAO.getRecord());

		req.setAttribute("center_or_account_group", centerOrAccountGroup);
		req.setAttribute("accountGrpAndCenterList", accGrpAndCenterDropdn);
		req.setAttribute("inscatName", InsuranceCategoryMasterDAO.getInsCatCenter(centerId));
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		req.setAttribute("healthAuthority", healthAuthority);


		ActionForward forward = new ActionForward(mapping.findForward("list").getPath());
		// when ever user uses a pagination open_date should not append again.
		if (date_range != null && date_range.equals("month") && req.getParameter("submission_date") == null) {
			addParameter("submission_date", monthdate, forward);
	    }
		return forward;
	}

	@IgnoreConfidentialFilters
	public ActionForward getClaimBillsActivities(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String claim_id = req.getParameter("claim_id");
		String patient_id = req.getParameter("patient_id");
		String main_visit_id = null;

		List billsList = new ArrayList();
		List<BasicDynaBean> bills  = null;
		List<BasicDynaBean> charges  = null;

		BasicDynaBean claim = claimdao.findClaimDetailsById(claim_id);
    if (claim == null) {
      String referer = req.getHeader("Referer");
      FlashScope flash = FlashScope.getScope(req);
          ActionRedirect redirect = new ActionRedirect(referer.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
          redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
          flash.error("Claim ID : "+claim_id+" is invalid. Bills having this claim are not connected to TPA/Insurance.");
      return redirect;
    }
		if (StringUtils.isEmpty(patient_id)) {
		  patient_id = (String)claim.get("patient_id");
		}
		String resubmissionId = null;

		BasicDynaBean resubmitbean = submitdao.getSubmissionDetails(claim_id);
		if (resubmitbean != null)
			resubmissionId = (String)resubmitbean.get("submission_batch_id");	
		Integer priority = (Integer)claim.get("priority");


		main_visit_id = (String)claim.get("main_visit_id");

		List<BasicDynaBean> diagnosisList = submitdao.findAllDiagnosis(patient_id); // HMS-17934:Showing wrong diagnosis code in claim reconciliation screen for a followup visit.Here we replaced main_visit_id to patient_id
		List<BasicDynaBean> coderDiagnosisList = submitdao.findAllCoderDiagnosis(patient_id);
		BasicDynaBean visitBean = visitDetailsDao.getVisitDetails(main_visit_id);
		String mrno = visitBean!= null? (String)visitBean.get("mr_no") : null;
		boolean isInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientCardImage(main_visit_id, null) != null;
		BigDecimal drgAdjAmt = claimActivityDao.getDRGAdjustmentAmt(claim_id, priority);
		
		BasicDynaBean notConsumedInvItemBean = new ClaimActivityDAO().getNotConsumedInvItemAmt(claim_id);
		
		bills  = submitdao.findAllBills(claim_id);
		Map billMap = null;
		String bill_no = null;
		for (BasicDynaBean bill : bills) {
			billMap = new HashMap();

			bill_no = (String)bill.get("bill_no");
			charges  = claimdao.findAllBillChargesIncludingZeroClaim(bill_no, claim_id);
			
			billMap.put("bill", bill);
			billMap.put("charges", charges);
			billsList.add(billMap);
		}


		if (claim.get("last_submission_batch_id") != null && resubmissionId != null &&
				!((String)claim.get("last_submission_batch_id")).equals(resubmissionId))
		req.setAttribute("resubmissionId", resubmissionId);

		String batchId = null;
		if (req.getAttribute("resubmissionId") != null && !req.getAttribute("resubmissionId").equals(""))
			batchId = (String)req.getAttribute("resubmissionId");
		else if (claim.get("last_submission_batch_id") != null && !claim.get("last_submission_batch_id").equals(""))
			batchId = (String)claim.get("last_submission_batch_id");


		BasicDynaBean submissionbean = null;
		if (batchId != null)
			submissionbean = submitdao.findByKey("submission_batch_id", batchId);

		String service_reg_no = null;

		if (submissionbean != null) {
			if (submissionbean.get("account_group") != null && ((Integer)submissionbean.get("account_group")).intValue() != 0) {
				BasicDynaBean accbean = accGroupDAO.findByKey("account_group_id", (Integer)submissionbean.get("account_group"));
				service_reg_no = accbean.get("account_group_service_reg_no") != null ? (String)accbean.get("account_group_service_reg_no") : "";
			}else if (submissionbean.get("center_id") != null) {
				BasicDynaBean centerbean = centerMasterDao.findByKey("center_id", (Integer)submissionbean.get("center_id"));
				service_reg_no = centerbean.get("hospital_center_service_reg_no") != null ? (String)centerbean.get("hospital_center_service_reg_no") : "";
			}
		}
		BasicDynaBean creditNoteDetails = billDao.getCreditNoteDetails(bill_no);
		
		//HMS-18005 : 	Get all Submission/Resubmission batch Id
		List<BasicDynaBean> submissionBatchList = submitdao.getSubmissionId(claim_id);
		
		if(null != creditNoteDetails){
			req.setAttribute("creditNoteDetails", creditNoteDetails.getMap());
		}
		
		req.setAttribute("service_reg_no", service_reg_no);
		req.setAttribute("claim", claim);
		req.setAttribute("billsList", billsList);
		req.setAttribute("diagnosisList", diagnosisList);
		req.setAttribute("coderDiagnosisList", coderDiagnosisList);
		req.setAttribute("isInsuranceCardAvailable", isInsuranceCardAvailable);
		req.setAttribute("isInterCompAllowed", claimReconciliationDao.isInternalCompAllowed(claim_id));
		req.setAttribute("hasExcessAmtNotDenialAcceptExist", claimReconciliationDao.hasExcessAmtNotDenialAcceptedActivities(claim_id));
		req.setAttribute("drgAdjAmt", drgAdjAmt);
		req.setAttribute("submissionBatchList", submissionBatchList);
		req.setAttribute("healthAuthority", req.getSession(false).getAttribute("loginCenterHealthAuthority"));
		
		BigDecimal notConsumedAmt = BigDecimal.ZERO;
		BigDecimal notConsumedDiscount = BigDecimal.ZERO;
		BigDecimal notConsumedTaxAmt = BigDecimal.ZERO;
		
		if (null != notConsumedInvItemBean) {
		  notConsumedAmt = (BigDecimal)notConsumedInvItemBean.get("amount");
		  notConsumedDiscount =  (BigDecimal)notConsumedInvItemBean.get("discount");
		  notConsumedTaxAmt = (BigDecimal)notConsumedInvItemBean.get("tax_amt");
		}
		
		req.setAttribute("notConsumedAmt", notConsumedAmt);
		req.setAttribute("notConsumedDiscount", notConsumedDiscount);
		req.setAttribute("notConsumedTaxAmt", notConsumedTaxAmt);
		
		return mapping.findForward("billsActivities");
	}

	public ActionForward unmarkForResubmission(ActionMapping mapping, ActionForm f,
 HttpServletRequest req, HttpServletResponse res)
			throws SQLException, IOException {
		FlashScope flash = FlashScope.getScope(req);
		String claim = req.getParameterValues("claim_id")[0];
		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			// updates the status to batched and clears the resubmission type
			// and comments.
			if (claim != null ) {
				BasicDynaBean claimBean = claimdao.getBean();
				claimBean.set("claim_id", claim);
				claimBean.set("status", "B");
				claimBean.set("resubmission_type", "");
				claimBean.set("comments", "");
					int i = claimdao.updateWithName(con, claimBean.getMap(), "claim_id");
					if (i == 0)
						success = false;
			}
		} finally {
			DataBaseUtil.commitClose(con, success);
		}
		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer")
				.replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return redirect;
	}

	public ActionForward markForResubmission(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		FlashScope flash = FlashScope.getScope(req);
		//String referer = req.getHeader("Referer");
		String claims[] = req.getParameterValues("claim_id");
		String comments = req.getParameter("_comments");
		String resubmission_type = req.getParameter("_resubmission_type");

		StringBuilder errorMsg = null;
		List<BasicDynaBean> claimsList = null;
		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			List<String> errorList = new ArrayList<String>();

			if (claims != null && claims.length > 0) {
				claimsList = new ArrayList<BasicDynaBean>();
		    	for ( int i=0; i<claims.length; i++) {
		    		BasicDynaBean claimBean = claimdao.getBean();
		    		claimBean.set("claim_id", claims[i]);
		    		claimBean.set("status", "M");
		    		claimBean.set("resubmission_type", resubmission_type);
		    		claimBean.set("comments", comments);
		    		boolean allowIntComp = claimReconciliationDao.isInternalCompAllowed(claims[i]);
		    		boolean hasExcessAmtNotDenialAcceptExist = claimReconciliationDao.hasExcessAmtNotDenialAcceptedActivities(claims[i]);

					if ((resubmission_type.equalsIgnoreCase("internal complaint") || resubmission_type.equalsIgnoreCase("reconciliation")) &&
							(!allowIntComp || hasExcessAmtNotDenialAcceptExist)) {
						//add claim to failure list(errorList)
						errorList.add(claims[i]);
						continue;
					}
		    		else
		    			claimsList.add(claimBean);

		    		//denial accepted changes
		    		if(resubmission_type != null && !resubmission_type.equals("")){
			    		List<BasicDynaBean> bccBeanList = bccDao.findAllByKey("claim_id", claims[i]);
			    		List<BasicDynaBean> saleClaimBeanList = saleClaimDAO.findAllByKey("claim_id", claims[i]);

			    		for(BasicDynaBean bean : bccBeanList){
			    			if( ((String)bean.get("closure_type")).equals("M") ){
			    				bean.set("closure_type", "D");
			    				bccDao.updateWithNames(con, bean.getMap(), new String[] {"charge_id", "claim_id"});
			    			}
			    		}

			    		for(BasicDynaBean bean : saleClaimBeanList){
			    			if( ((String)bean.get("closure_type")).equals("M") ){
			    				bean.set("closure_type", "D");
			    				saleClaimDAO.updateWithNames(con, bean.getMap(), new String[] {"sale_item_id", "claim_id"});
			    			}
			    		}
		    		}
		    	}
			}

			for (BasicDynaBean b: claimsList) {
				int i = claimdao.updateWithName(con, b.getMap(), "claim_id");
				if (i == 0)
					success = false;
				if (!success)
					break;
			}
			//preparing error link for claims which are not processed
			String path = req.getContextPath();

			HttpSession session = RequestContext.getSession();
			java.util.HashMap urlRightsMap = (java.util.HashMap) session.getAttribute("urlRightsMap");
			java.util.HashMap actionUrlMap = (java.util.HashMap) session.getServletContext().getAttribute("actionUrlMap");

			if (null != urlRightsMap && null != actionUrlMap && urlRightsMap.get("insurance_claim_reconciliation").equals("A")) {
				String url = "";
				url = (String)actionUrlMap.get("insurance_claim_reconciliation");
				path = path +"/";

				errorMsg = new StringBuilder(" No activities found or activities has full/excess payment" +
						" to mark claim for resubmission with Internal Complaint for following claims : </br> ");
				for(String claim_id : errorList){
					errorMsg.append("<b><a target='_blank' href='"+path + url+"?_method=getClaimBillsActivities&claim_id="+claim_id+"'>");
					errorMsg.append(claim_id);
					errorMsg.append("</a></b> , ");
				}
			}
			else {
				errorMsg = new StringBuilder(" Claims which are not processed are : </br> ");
				for(String claim_id : errorList){
					errorMsg.append(claim_id);
					errorMsg.append(", ");
				}
			}

			if(errorList.size() > 0)
				flash.error(errorMsg.toString());
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		 ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer").
					replaceAll("&" + FlashScope.FLASH_KEY + "=[0-9A-Fa-f]+", ""));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
	 		return redirect;
	}

	public ActionForward claimClosure(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String referer = req.getHeader("Referer");
		FlashScope flash = FlashScope.getScope(req);
        ActionRedirect redirect = new ActionRedirect(referer);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		String claims[] = req.getParameterValues("claim_id");
		String remarks = req.getParameter("_closure_remarks");
		String closure_type = req.getParameter("_closure_type");
		String claim_rej_reason = req.getParameter("_claim_rejection_reasons_drpdn");
		BasicDynaBean reasonBean=null;
		if(claim_rej_reason != null && !claim_rej_reason.equals(""))
			reasonBean = (rejectionReasonCategories
					.findByKey("rejection_reason_category_id", Integer.parseInt(claim_rej_reason)));

		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BillDAO billDAO = new BillDAO(con);

			if (closure_type != null && !closure_type.trim().equals("")) {
				if (claims != null && claims.length > 0) {

					// Close claims and bills if closure type is given.
			    	for ( int i=0; i<claims.length; i++) {
			    		BasicDynaBean claimBean = claimdao.getBean();

			    		String claim_id = claims[i];
			    		claimBean.set("claim_id", claim_id);
			    		claimBean.set("status", "C"); // Close the claim
			    		claimBean.set("closure_type", closure_type);
			    		claimBean.set("action_remarks", remarks);
			    		claimBean.set("rejection_reason_category_id", (claim_rej_reason != null && !claim_rej_reason.equals("") ? Integer.parseInt(claim_rej_reason) : null));

			    		//update each activity as denial accepted
	    				if(closure_type.equals("D")){
	    					List<BasicDynaBean> bccBeanList = bccDao.findAllByKey("claim_id", claims[i]);
				    		List<BasicDynaBean> saleClaimBeanList = saleClaimDAO.findAllByKey("claim_id", claims[i]);

		    				for(BasicDynaBean bean : bccBeanList) {
		    					if(((String)bean.get("claim_status")).equals("D") &&
		    							!(bean.get("closure_type").equals("D") || bean.get("closure_type").equals("M"))){
		    						bean.set("closure_type", "D");
		    						bean.set("rejection_reason_category_id", Integer.parseInt(claim_rej_reason));
		    						bccDao.updateWithNames(con, bean.getMap(), new String[] {"charge_id", "claim_id"});
		    					}
		    				}

		    				for(BasicDynaBean bean : saleClaimBeanList) {
		    					if(((String)bean.get("claim_status")).equals("D") &&
		    							!(bean.get("closure_type").equals("D") || bean.get("closure_type").equals("M"))){
		    						bean.set("closure_type", "D");
		    						bean.set("rejection_reason_category_id", Integer.parseInt(claim_rej_reason));
		    						saleClaimDAO.updateWithNames(con, bean.getMap(), new String[] {"sale_item_id", "claim_id"});
		    					}
		    				}
	    				}

			    		List<BasicDynaBean> bills = claimSubmissionDao.findAllBills(claim_id);
			    		if (bills != null && bills.size() > 0) {
			    			for (BasicDynaBean bill : bills) {
			    				String billNo = (String)bill.get("bill_no");

			    				BillClaimDAO bcdao = new BillClaimDAO();
								bcdao.closeBillClaim(con, billNo, claim_id);
								List<BasicDynaBean> openClaims = bcdao.getOpenBillClaims(con, billNo);
								if (null != openClaims && openClaims.size() > 0) {
									continue;
								}
			    				Bill b = billDAO.getBill(billNo);

								if (closure_type != null && billNeedsClosure(con, b)) {
									if (closure_type.equals("F")) { // Fully Received

										b.setSpnrWriteOffRemarks(remarks+" Claim amount received. Bill is marked for sponsor due write off.");
									}else if (closure_type.equals("D")) { // Denial Accepted

										b.setSpnrWriteOffRemarks(remarks+" Bill claim denial accepted. Bill is marked for sponsor due write off. Rejection Reason : "+(reasonBean != null ? reasonBean.get("rejection_reason_category_name") : ""));

									}else if (closure_type.equals("W")) { // Written Off

										b.setSpnrWriteOffRemarks(remarks+" Claim amount write-off. Bill is marked for sponsor due write off.");
									}
								}
								success = success && billDAO.updateBill(b);
			    			}
			    		}

			    		int k = claimdao.updateWithName(con, claimBean.getMap(), "claim_id");
			    		success = success && k > 0;

			    		if (!success) {
			    			flash.error("Error while closing bill/claim..."+claim_id);
			    			break;
			    		}
			    	}
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

 		return redirect;
	}

	private boolean billNeedsClosure(Connection con, Bill b) throws SQLException {
		// Basic checks
		if (null == b) return false;
		String billNo = b.getBillNo();
		if (null == billNo) return false;

		// check if bill is already closed
		if (null != b.getStatus() && b.getStatus().equals("C")) {
			// bill already closed, return false
			return false;
		}

		// check if bill has any sponsor due
		BigDecimal dueAmt = BillDAO.getSponsorDue(con, billNo);
		if (null != dueAmt && dueAmt.compareTo(BigDecimal.ZERO) <= 0) {
			return false; // this bill is already settled earlier, return false
		}
		return true;
	}

	public ActionForward addToSubmission(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(req);

		boolean success = true;
		String claim_id = req.getParameter("claim_id");
		String screen = req.getParameter("screen");
		String submissionBatchID = null;
		String tpa = "";
		String ins_company = "";

		BasicDynaBean claim = claimdao.findClaimById(claim_id);
		tpa = (String)claim.get("tpa_name");
		ins_company = (String)claim.get("insurance_co_name");
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");

		BasicDynaBean submissionbean = claimReconciliationDao.getLatestOpenSubOrResub("N", claim_id, centerId);
		boolean isProdXmlGenerated = false;
		if (submissionbean != null ) {
			isProdXmlGenerated = (boolean) submissionbean.get("prod_xml_generated");
			submissionBatchID = (String)submissionbean.get("submission_batch_id");
		}
		List<String> claims = new ArrayList<String>();
		Connection con = null;
		try {
			if (submissionbean != null && !isProdXmlGenerated) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				claims.add(claim_id);
				success = claimdao.updateClaimSubmission(con, claims, submissionBatchID);
				//insert new mapping into claim_submissions
				BasicDynaBean submitBean = claimSubmissionsDAO.getBean();
				submitBean.set("claim_id", claim_id);
				submitBean.set("submission_batch_id", submissionBatchID);
				submitBean.set("status", "O");

				success = claimSubmissionsDAO.insert(con, submitBean);
			}
		} finally {
      		DataBaseUtil.commitClose(con, success);
		}
		
		// Copy records into coder diagnosis table (hospital_claim_diagnosis) if not already there
		ClaimSubmissionAction submissionAction = new ClaimSubmissionAction();
		submissionAction.insertRecordsIntoHospDiagnosis(submissionBatchID);

 		if (submissionbean == null) {
			flash.info("There is <b> No Open submission batch </b> with Insurance company as <b>" +
							ins_company+ "</b> and TPA as <b> "+tpa+ "</b> for adding the claim.");
 		} else if (submissionbean != null && isProdXmlGenerated) {
			flash.info("Production XML has already generated. Cannot add the Claim ID to Submission Batch Id :" +submissionBatchID);
		} else {
			if(!success) {
				flash.error("Error while adding to submission batch : "+submissionBatchID);
			} else {
				flash.info("Claim "+claim_id+" added to submission batch : "+submissionBatchID);
			}
		}

 		if (screen != null && screen.equals("activity")) {
 			redirect = new ActionRedirect("claimReconciliation.do");
 			redirect.addParameter("claim_id", claim_id);
 			redirect.addParameter("_method", "getClaimBillsActivities");
 		} else {
			redirect = new ActionRedirect(req.getHeader("Referer"));
		}
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

        return redirect;
	}

	public ActionForward removeFromSubmission(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(req);

		boolean success = true;
		String claim_id = req.getParameter("claim_id");
		String submissionBatchID = req.getParameter("_submission_batch_id");

		String screen = req.getParameter("screen");

		if (screen != null && screen.equals("activity")) {
 			redirect = new ActionRedirect("claimReconciliation.do");
 			redirect.addParameter("claim_id", claim_id);
 			redirect.addParameter("_method", "getClaimBillsActivities");
 		}else
 			redirect = new ActionRedirect(req.getHeader("Referer"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		List<String> claims = new ArrayList<String>();
		Connection con = null;
		try {
			if (submissionBatchID != null && !submissionBatchID.equals("")) {

				BasicDynaBean submissionbean = submitdao.findByKey("submission_batch_id", submissionBatchID);

				if (submissionbean != null && submissionbean.get("status") != null
						&& ((String)submissionbean.get("status")).equals("S")) {
					flash.error("Submission batch is marked as Sent. Cannot remove from submission batch : "+submissionBatchID);
					return redirect;
				}

				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				claims.add(claim_id);
				success = claimdao.removeClaimSubmission(con, claims);//remove submission_batch_id from insurance_claim and reset status to O
				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
				map.put("claim_id", claim_id);
				map.put("submission_batch_id", submissionBatchID);
				claimSubmissionsDAO.delete(con, map);//delete claim mapping from claim_submissions
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(!success)
			flash.error("Error while removing claim from submission batch : "+submissionBatchID);
		else
			flash.info("Claim "+claim_id+" removed from submission batch : "+submissionBatchID);

        return redirect;
	}

	public ActionForward addToResubmission(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(req);

		boolean success = true;
		String claim_id = req.getParameter("claim_id");
		String screen = req.getParameter("screen");
		String submissionBatchID = null;
		String tpa = "";
		String ins_company = "";

		BasicDynaBean claim = claimdao.findClaimById(claim_id);
		tpa = (String)claim.get("tpa_name");
		ins_company = (String)claim.get("insurance_co_name");
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");

		BasicDynaBean submissionbean = claimReconciliationDao.getLatestOpenSubOrResub("Y", claim_id, centerId);
		boolean isProdXmlGenerated = false;
		if (submissionbean != null ) {
			isProdXmlGenerated = (boolean) submissionbean.get("prod_xml_generated");
			submissionBatchID = (String)submissionbean.get("submission_batch_id");
		}
		List<String> claims = new ArrayList<String>();
		Connection con = null;
		try {
			if (submissionbean != null && !isProdXmlGenerated) {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				claims.add(claim_id);
				//insert mapping into claim_submissions table
				BasicDynaBean resubmitBean = claimSubmissionsDAO.getBean();
				resubmitBean.set("claim_id", claim_id);
				resubmitBean.set("submission_batch_id", submissionBatchID);
				resubmitBean.set("status", "O");

				success = claimSubmissionsDAO.insert(con, resubmitBean);
				if (success)
					success = claimdao.updateClaimResubmission(con, claims, submissionBatchID); //sets last_submission_batch_id and status of claim to B
			}

		} finally {
			DataBaseUtil.commitClose(con, success);
		}

		// Copy records into coder diagnosis table (hospital_claim_diagnosis) if not already there.
		ClaimSubmissionAction submissionAction = new ClaimSubmissionAction();
		submissionAction.insertRecordsIntoHospDiagnosis(submissionBatchID);
		

		if (submissionbean == null) {
			flash.info("There is <b> No Open resubmission batch </b> with Insurance company as <b>" +
					ins_company+ "</b> and TPA as <b> "+tpa+ "</b> for adding the claim.");
		} else if (submissionbean != null && isProdXmlGenerated) {
			flash.info("Production XML has already generated. Cannot add the Claim ID to Submission Batch Id :" +submissionBatchID);
		} else {
			if(!success)
				flash.error("Error while adding to re-submission batch : "+submissionBatchID);
			else
				flash.info("Claim "+claim_id+" added to resubmission batch : "+submissionBatchID);
		}

 		if (screen != null && screen.equals("activity")) {
 			redirect = new ActionRedirect("claimReconciliation.do");
 			redirect.addParameter("claim_id", claim_id);
 			redirect.addParameter("_method", "getClaimBillsActivities");
 		}else
 			redirect = new ActionRedirect(req.getHeader("Referer"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

        return redirect;
	}

	public ActionForward removeFromResubmission(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		ActionRedirect redirect = null;
		FlashScope flash = FlashScope.getScope(req);

		boolean success = true;
		String claim_id = req.getParameter("claim_id");
		String resubmissionBatchID = req.getParameter("_submission_batch_id");

		String screen = req.getParameter("screen");

		if (screen != null && screen.equals("activity")) {
 			redirect = new ActionRedirect("claimReconciliation.do");
 			redirect.addParameter("claim_id", claim_id);
 			redirect.addParameter("_method", "getClaimBillsActivities");
 		}else
 			redirect = new ActionRedirect(req.getHeader("Referer"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		List<String> claims = new ArrayList<String>();
		Connection con = null;
		try {
			if (resubmissionBatchID != null && !resubmissionBatchID.equals("")) {

				BasicDynaBean resubmissionbean = submitdao.findByKey("submission_batch_id", resubmissionBatchID);

				if (resubmissionbean != null && resubmissionbean.get("status") != null
						&& ((String)resubmissionbean.get("status")).equals("S")) {
					flash.error("Submission batch is marked as Sent. Cannot remove from submission batch : "+resubmissionBatchID);
					return redirect;
				}
			
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);

				claims.add(claim_id);

				LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
				map.put("claim_id", claim_id);
				map.put("submission_batch_id", resubmissionBatchID);
				success = claimSubmissionsDAO.delete(con, map);
				claimSubmissionsDAO.delete(con, map);//delete from claim_submissions

				if (success)
					success = claimdao.deleteClaimResubmission(con, claims);//sets last_submission_batch_id 
																			//to latest submission batch id from claimsubmissions and status to M
				
			}

		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		if(!success)
			flash.error("Error while removing from re-submission batch : "+resubmissionBatchID);
		else
			flash.info("Claim "+claim_id+" removed from resubmission batch : "+resubmissionBatchID);

        return redirect;
	}

	public ActionForward updateClaim(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		HttpSession session = req.getSession();
		String userid = (String)session.getAttribute("userid");

		String claim_id = req.getParameter("claim_id");
		String resubmission_type = req.getParameter("_resubmission_type");

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("claimbillsRedirect"));
		FlashScope flash = FlashScope.getScope(req);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		redirect.addParameter("claim_id", claim_id);

		String closure_type = req.getParameter("closure_type");
		String claimRejReason = req.getParameter("claim_rejection_reasons_drpdn");
		Integer claim_rej_reason = (claimRejReason != null && !claimRejReason.equals("")) ?
				Integer.parseInt(claimRejReason) : null ;
		BasicDynaBean reasonBean = (rejectionReasonCategories
				.findByKey("rejection_reason_category_id", claim_rej_reason));
		String action_remarks = req.getParameter("action_remarks");

		String[] activityIds = req.getParameterValues("activity_charge_id");
		String[] itemRates = req.getParameterValues("item_rate");
		String[] itemAmounts = req.getParameterValues("item_amount");
		String[] claimNetAmts = req.getParameterValues("claim_net_amount");
		String[] patientAmts = req.getParameterValues("patient_amount");
		String[] billNos = req.getParameterValues("bill_no");
		String[] billStatus = req.getParameterValues("bill_status");
		String[] activityClaimStatus = req.getParameterValues("activity_claim_status");
		String[] chargeIds = req.getParameterValues("charge_id");
		String[] saleItemIds = req.getParameterValues("sale_item_id");
		String[] itemEdited = req.getParameterValues("edited");
		String[] claimAmtEdited = req.getParameterValues("claimAmtEdited");
		String[] denialAccepted = req.getParameterValues("item_denial_accepted");
		String[] rejectionReasonCat = req.getParameterValues("item_rej_reason");

		Map<String, String> billNosMap = new HashMap<String, String>();

		ArrayList<String> activityBills = new ArrayList<String>();
		ArrayList<String> pharmacyCharges = new ArrayList<String>();

		ArrayList<BasicDynaBean> chargeBeansList = new ArrayList<BasicDynaBean>();
		ArrayList<BasicDynaBean> claimBeanList = new ArrayList<BasicDynaBean>();
		ArrayList<BasicDynaBean> parmacyItemBeansList = new ArrayList<BasicDynaBean>();
		ArrayList<BasicDynaBean> pharmacyClaimBeanList = new ArrayList<BasicDynaBean>();

		if (claimAmtEdited != null && claimAmtEdited.length > 0) {
			// Reopen bills if bill item is edited and bill status is finalized.
			for (int i = 0; i < claimAmtEdited.length; i++) {
				if (claimAmtEdited[i].equals("true") && billStatus[i].equals(Bill.BILL_STATUS_FINALIZED)) {
					billNosMap.put(billNos[i], billStatus[i]);
				}
			}
		}

		Connection con = null;
		boolean success = false;

		HashMap actionRightsMap = new HashMap();
		actionRightsMap = (HashMap)session.getAttribute("actionRightsMap");
		Object roleID = session.getAttribute("roleId");
		String actionRightStatus=(String) actionRightsMap.get(Role.BILL_REOPEN);

		// Reopen bills(bill status as Open) if bill item is edited.
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (billNosMap != null && billNosMap.size() > 0) {

				if (!roleID.equals(1) && !roleID.equals(2) && !actionRightStatus.equalsIgnoreCase("A")) {
					flash.put("error", "You don't have Authorization to Reopen Bill. Claim amount updation for activities cannot be done.");
					return redirect;
				}

				Set bills = billNosMap.keySet();
				BillDAO billDAO = new BillDAO(con);

				for (Iterator iter = bills.iterator(); iter.hasNext();) {
					String billNo = (String) iter.next();
					Bill bill = billBO.getBill(billNo);

					bill.setStatus(Bill.BILL_STATUS_OPEN);
					bill.setReopenReason("Claim reconciliation: Reopening bill for updating claim amounts.");
					bill.setModTime(new java.util.Date());
					bill.setUserName(userid);
					success = billDAO.updateBill(bill);

					if (!success) {
						flash.error("Claim amount updation failed. Error while reopening bill: "+billNo);
						return redirect;
					}
				}
			}
		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			java.sql.Timestamp modTime = DateUtil.getCurrentTimestamp();

			if (itemEdited != null && itemEdited.length > 0) {
				for (int i = 0; i < itemEdited.length; i++) {

					if (itemEdited[i].equals("true") || (closure_type != null && closure_type.equals("D")
							&& activityClaimStatus[i].equals("D"))) {
						String charge_id = chargeIds[i];
						String sale_item_id = saleItemIds[i];

						BigDecimal rate = (itemRates[i] != null && !itemRates[i].equals(""))
												? new BigDecimal(itemRates[i]) : BigDecimal.ZERO;
						BigDecimal amount = (itemAmounts[i] != null && !itemAmounts[i].equals(""))
												? new BigDecimal(itemAmounts[i]) : BigDecimal.ZERO;
						BigDecimal claim_amt = (claimNetAmts[i] != null && !claimNetAmts[i].equals(""))
												? new BigDecimal(claimNetAmts[i]) : BigDecimal.ZERO;
						String denial_accepted = (denialAccepted[i] != null && !denialAccepted[i].equals(""))
												? denialAccepted[i] : "O";
						denial_accepted = (resubmission_type != null && denial_accepted.equals("M") && !resubmission_type.equals(""))
												? "D" : denial_accepted;
						denial_accepted = (closure_type != null && (closure_type.equals("D") && activityClaimStatus[i].equals("D")) )
												? "D" : denial_accepted;

						Integer rejection_reason_cat = (rejectionReasonCat[i] != null && !rejectionReasonCat[i].equals(""))
						? Integer.parseInt(rejectionReasonCat[i]) : null;

						String orgiDenialAccepted = (denialAccepted[i] != null && !denialAccepted[i].equals(""))
												? denialAccepted[i] : "O";
						rejection_reason_cat = (closure_type != null && (closure_type.equals("D") && activityClaimStatus[i].equals("D")) && orgiDenialAccepted.equals("O"))
												? claim_rej_reason : rejection_reason_cat;

						if (activityIds[i].startsWith("A")) {

							 BasicDynaBean billChrgPaymentBean = remdao.getCharge(con, charge_id);
							 BasicDynaBean billChrgClaimBean = remdao.getChargeClaim(con, charge_id, claim_id);
							 if(claimAmtEdited[i].equals("true")) {
								 billChrgPaymentBean.set("act_rate", rate);
								 billChrgPaymentBean.set("amount", amount);
								 // billChrgPaymentBean.set("insurance_claim_amount", claim_amt);
								 billChrgPaymentBean.set("return_insurance_claim_amt", BigDecimal.ZERO);
								 billChrgPaymentBean.set("username", userid);
								 billChrgPaymentBean.set("mod_time", modTime);

								 billChrgClaimBean.set("insurance_claim_amt", claim_amt);

//								 Charge related bills to be updated.
								 if(!activityBills.contains((String)billChrgPaymentBean.get("bill_no")))
									 activityBills.add((String)billChrgPaymentBean.get("bill_no"));
							 }

							 billChrgClaimBean.set("closure_type",denial_accepted);
							 billChrgClaimBean.set("rejection_reason_category_id", rejection_reason_cat);

							 chargeBeansList.add(billChrgPaymentBean);
							 claimBeanList.add(billChrgClaimBean);

						 }else if (activityIds[i].startsWith("P")) {
							 Integer saleItemId = Integer.parseInt(sale_item_id);
							 BasicDynaBean saleItemPaymentBean = remdao.getSaleItem(con, saleItemId);
							 BasicDynaBean saleItemClaimBean = remdao.getSaleClaimItem(con, saleItemId, claim_id);
							 if(claimAmtEdited[i].equals("true")) {
								 saleItemPaymentBean.set("rate", rate);
								 saleItemPaymentBean.set("amount", amount);
								 //saleItemPaymentBean.set("insurance_claim_amt", claim_amt);

								 saleItemClaimBean.set("insurance_claim_amt", claim_amt);

//								 Item related charges to be updated.
								 if (!pharmacyCharges.contains(charge_id))
									 pharmacyCharges.add(charge_id);
							 }

							 saleItemClaimBean.set("closure_type", denial_accepted);
							 saleItemClaimBean.set("rejection_reason_category_id", rejection_reason_cat);

							 parmacyItemBeansList.add(saleItemPaymentBean);
							 pharmacyClaimBeanList.add(saleItemClaimBean);
						 }
					}
				}
			}

			// Update all pharmacy sale items.
			 for (BasicDynaBean pb : parmacyItemBeansList) {
				 item.updateWithName(con, pb.getMap(), "sale_item_id");
			 }

			 for (BasicDynaBean pcb : pharmacyClaimBeanList) {
				 saleClaimDAO.updateWithNames(con, pcb.getMap(), new String[] {"sale_item_id", "claim_id"});
			 }

			 // Update sale related charge in bill_charge by sale id.
			 for (String phCharge : pharmacyCharges) {

				 BasicDynaBean sale = remdao.findSaleCharge(con, phCharge);
				 String sale_id = (String)sale.get("sale_id");

				 List<BasicDynaBean> items = remdao.findItemsBySaleId(con, sale_id);

				 BigDecimal totalPhAmt = BigDecimal.ZERO;
				 BigDecimal totalPhInsAmt = BigDecimal.ZERO;
				 for (BasicDynaBean sb : items) {
					 totalPhAmt = totalPhAmt.add((BigDecimal)sb.get("amount"));
					 //totalPhInsAmt = totalPhInsAmt.add((BigDecimal)sb.get("insurance_claim_amt"));
				 }

				 List<BasicDynaBean> claimItems = remdao.findItemClaimsBySaleId(con, sale_id, claim_id);
				 for (BasicDynaBean ci : claimItems) {
					 totalPhInsAmt = totalPhInsAmt.add((BigDecimal)ci.get("insurance_claim_amt"));
				 }

				 BasicDynaBean phchargebean = remdao.getCharge(con, phCharge);
				 //phchargebean.set("insurance_claim_amount", totalPhInsAmt);
				 phchargebean.set("act_rate", totalPhAmt);
				 phchargebean.set("amount", totalPhAmt);
				 phchargebean.set("username", userid);
				 phchargebean.set("mod_time", modTime);

				 BasicDynaBean phClaimBean = remdao.getChargeClaim(con, phCharge, claim_id);
				 phClaimBean.set("insurance_claim_amt", totalPhInsAmt);

				 chargeBeansList.add(phchargebean);
				 claimBeanList.add(phClaimBean);

				 // Charge related bills to be updated.
				 if(!activityBills.contains((String)phchargebean.get("bill_no")))
					 activityBills.add((String)phchargebean.get("bill_no"));
			 }

			// Update all charges i.e activities
			for (BasicDynaBean b : chargeBeansList) {
				charge.updateWithName(con, b.getMap(), "charge_id");
			}

			for (BasicDynaBean c : claimBeanList) {
				bccDao.updateWithNames(con, c.getMap(), new String[] {"charge_id", "claim_id"});
			}

			success = true;

		}finally {
			DataBaseUtil.commitClose(con, success);
			if (success) {
				for (String billNo : activityBills) {
					BillDAO.resetTotalsOrReProcess(billNo);
				}
			}
		}

		try {
			if (billNosMap != null && billNosMap.size() > 0) {
				Set bills = billNosMap.keySet();

				for (Iterator iter = bills.iterator(); iter.hasNext();) {
					String billNo = (String) iter.next();
					Bill bill = billBO.getBill(billNo);
					Timestamp finalizedDate = new Timestamp(bill.getFinalizedDate().getTime());
					String paymentStatus = bill.getPaymentStatus();
					String dischargeStatus = null;
					if (dischargeStatus == null) {
						dischargeStatus = bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) &&
							paymentStatus.equals(Bill.BILL_PAYMENT_PAID) ? "Y" : "N" ;
					}

					String error = new BillBO().updateBillStatus(bill, Bill.BILL_STATUS_FINALIZED, paymentStatus,
							dischargeStatus, finalizedDate, userid, false, false);

					if (error == null || error.equals("")) error = "";
					else if(error.startsWith("Bill status")) {}
					else error = error + "<br/> Please modify the discount amount and finalize /close the bill again.";

					if (!error.equals("")) {
						success = false;
						flash.put("error", "Error while finalizing claim bill: "+billNo+"  "+ error);
						return redirect;
					}
				}
			}
		}catch (Exception e) {
			throw e;
		}

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			BillDAO billDAO = new BillDAO(con);

			// Close claims and bills if closure type is given.
			if (closure_type != null && !closure_type.trim().equals("")) {
				List<BasicDynaBean> bills = claimSubmissionDao.findAllBills(claim_id);
				if (bills != null && bills.size() > 0) {
					for (BasicDynaBean bill : bills) {
						String billNo = (String)bill.get("bill_no");

						BillClaimDAO bcdao = billClaimDao;
						bcdao.closeBillClaim(con, billNo, claim_id);
						List<BasicDynaBean> openClaims = bcdao.getOpenBillClaims(con, billNo);
						if (null != openClaims && openClaims.size() > 0) {
							continue;
						}

						Bill b = billDAO.getBill(billNo);

						if (closure_type != null && billNeedsClosure(con, b)) {
							if (closure_type.equals("F")) { // Fully Received
								//b.setPrimaryClaimStatus("R");
								b.setSpnrWriteOffRemarks("Claim amount received. Bill is marked for sponsor due write off. ");

							}else if (closure_type.equals("D")) { // Denial Accepted
								//b.setPrimaryClaimStatus("R");
								b.setSpnrWriteOffRemarks("Bill claim denial accepted. Bill is marked for sponsor due write off. Rejection Reason : "+reasonBean.get("rejection_reason_category_name"));

							}else if (closure_type.equals("W")) { // Written Off
								//b.setPrimaryClaimStatus("R");
								b.setSpnrWriteOffRemarks("Claim amount write-off. Bill is marked for sponsor due write off.");
							}
						}
						success = success && billDAO.updateBill(b);
					}
				}

	    		BasicDynaBean claimBean = claimdao.getBean();
	    		claimBean.set("status", "C"); // Close the claim
	    		claimBean.set("claim_id", claim_id);
	    		claimBean.set("closure_type", closure_type);
	    		claimBean.set("rejection_reason_category_id", claim_rej_reason);
	    		claimBean.set("action_remarks", action_remarks);

	    		int i = claimdao.updateWithName(con, claimBean.getMap(), "claim_id");
	    		success = success && i > 0;
			}
    		if (!success) flash.error("Error while updating claim...");

		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		//schedule the accounting for the claim amount edited bills
		if(billNosMap != null && billNosMap.size() > 0) {
		  List<BasicDynaBean> billsList = billBO.getBillBeans(billNosMap.keySet());
		  if(billsList != null && billsList.size() > 0) {
		    accountingJobScheduler.scheduleAccountingForBills(billsList);
		  }
		}

		return redirect;
	}

	public ActionForward addOrEditAttachment(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String referer = req.getHeader("Referer");
		String claim_id = req.getParameter("claim_id");
		BasicDynaBean claim = claimdao.findClaimDetailsById(claim_id);
		req.setAttribute("claim", claim);
		req.setAttribute("referer", referer);

		int size = ClaimReconciliationDAO.getFileSize(claim_id);
		req.setAttribute("fileSize", size);

		return mapping.findForward("addAttachment");

	}

	public ActionForward showAttachment(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String claim_id = req.getParameter("claim_id");
		Map attchMap = claimReconciliationDao.getAttachment(claim_id);

		String type = (String)attchMap.get("Type");
		res.setContentType(type);

		OutputStream os = res.getOutputStream();
		InputStream file = (InputStream)attchMap.get("Content");

		byte[] bytes = new byte[4096];
		int len = 0;
		while ( (len = file.read(bytes)) > 0) {
			os.write(bytes, 0, len);
		}

		os.flush();
		file.close();
		return null;
	}

	public ActionForward deleteAttachment(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException,ServletException, Exception {

		String claim_id = request.getParameter("claim_id");
		boolean success = ClaimReconciliationDAO.deleteAttachment(claim_id);
		FlashScope flash = FlashScope.getScope(request);
		if(!success) {
			flash.put("error", "Attachment could not be deleted.");
		}

        ActionRedirect redirect = new ActionRedirect("claimReconciliation.do");
        redirect.addParameter("_method", "addOrEditAttachment");
        redirect.addParameter("claim_id", claim_id);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 		return redirect;
	}


	@IgnoreConfidentialFilters
	public Map<String ,Object> getAttachmentMap(HttpServletRequest request)
	 throws FileUploadException, IOException	{

		Map<String,Object> params = new HashMap<String, Object>();
		String contentType = null;
		if (request.getContentType().split("/")[0].equals("multipart")){

			DiskFileItemFactory factory = new DiskFileItemFactory();

			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items  = upload.parseRequest(request);
			Iterator it = items.iterator();
			while (it.hasNext()){
				FileItem item = (FileItem) it.next();
				if (item.isFormField()){
					String name = item.getFieldName();
					String value = item.getString();
					params.put(name, new Object[]{value});
				}else {
					String fieldName = item.getFieldName();
					String fileName = item.getName();
					contentType = item.getContentType();
					boolean isInMempry = item.isInMemory();
					long sizeInBytes = item.getSize();
					if (!fileName.equals("")){
						params.put(fieldName, new InputStream[]{item.getInputStream()});
						params.put("attachment_content_type", new String[]{contentType});
						params.put("attachment_size", new Integer[]{item.getInputStream().available()});
					}
				}
			}
		}else {
			params.putAll(request.getParameterMap());
		}
		return params;
	}

	public ActionForward saveAttachment(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException,Exception {

		FlashScope flash = FlashScope.getScope(request);
		String claim_id = request.getParameter("claim_id");

		Map<String, Object> params = getAttachmentMap(request);
		boolean success = ClaimReconciliationDAO.updateAttachment(params, claim_id);
		if(!success)
			 flash.put("error", "Failed to update attachment");

		ActionRedirect redirect = new ActionRedirect("claimReconciliation.do");
        redirect.addParameter("_method", "addOrEditAttachment");
        redirect.addParameter("claim_id", claim_id);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 		return redirect;
	}

	public ActionForward markAsDenied(ActionMapping mapping, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception,SQLException {

		String referer = req.getHeader("Referer");
		String claims[] = req.getParameterValues("claim_id");
		String denial_remarks = req.getParameter("denial_remarks");
		String submissionBatchID = req.getParameter("_submission_batch_id");

		List<BasicDynaBean> claimsList = null;
		Connection con = null;
		boolean success = true;

		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (claims != null && claims.length > 0) {
				claimsList = new ArrayList<BasicDynaBean>();
		    	for ( int i=0; i<claims.length; i++) {
		    		//update status as Denied in mapping table (claim_submissions)
		    		BasicDynaBean submitBean = claimSubmissionsDAO.getBean();
					submitBean.set("claim_id", claims[i]);
					submitBean.set("submission_batch_id", submissionBatchID);
					submitBean.set("status", "D");
					claimSubmissionsDAO.updateWithNames(con, submitBean.getMap(), 
							new String[] {"claim_id","submission_batch_id"});	
					
		    		//Update denial remarks for claim in insurance_claim
		    		BasicDynaBean claimBean = claimdao.getBean();
		    		claimBean.set("claim_id", claims[i]);
		    		claimBean.set("denial_remarks", denial_remarks);
		    		claimsList.add(claimBean);
		    	}
			}

			for (BasicDynaBean b: claimsList) {
				int i = claimdao.updateWithName(con, b.getMap(), "claim_id");
				if (i == 0)
					success = false;
				if (!success)
					break;
			}

		}finally {
			DataBaseUtil.commitClose(con, success);
		}

		FlashScope flash = FlashScope.getScope(req);
        ActionRedirect redirect = new ActionRedirect(referer);
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
 		return redirect;
	}
	
	public ActionForward getClaimActivityHistory(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, SQLException {
    String chargeId = request.getParameter("charge_id");
    String claimId = request.getParameter("claim_id");
    Integer saleItemId = Integer.parseInt(request.getParameter("sale_item_id"));

    List<BasicDynaBean> claimActivityHistoryList = new ArrayList<>();
    if (claimId != null && chargeId != null) {
      claimActivityHistoryList = claimHistoryService.getClaimActivityHistory(claimId, chargeId, saleItemId);
    }

    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    response
        .getWriter()
        .write(js.deepSerialize(ConversionUtils.listBeanToListMap(claimActivityHistoryList)));
    response.flushBuffer();
    return null;
  }
	
}
