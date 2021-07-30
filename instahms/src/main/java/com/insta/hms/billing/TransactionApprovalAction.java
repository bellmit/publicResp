/**
 *
 */
package com.insta.hms.billing;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

/**
 * @author kalpana.muvvala
 *
 */
public class TransactionApprovalAction extends DispatchAction{

	static Logger log = LoggerFactory.getLogger(TransactionApprovalAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getTransactionApprovalScreen(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		PagedList pagedList = ChargeDAO.searchChargesExtended(request.getParameterMap(),
				ConversionUtils.getListingParameter(request.getParameterMap()));

		request.setAttribute("pagedList", pagedList);

		return mapping.findForward("populatedetails");
	}

	public ActionForward saveTransactionApprovals(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String dateStr = null;
		ChargeBO chargeBOObj = new ChargeBO();
		List deleteChargeIdList = new ArrayList();
		List updateChargeList = new ArrayList();
		List approveChargeIdList = new ArrayList();
		int j;
		TransactionApprovalForm transAppForm = (TransactionApprovalForm)form;

		HttpSession session=request.getSession();
		String userid = (String)session.getAttribute("userid");

		int deleteChargeIdListlength = Integer.parseInt(request.getParameter("noOfDeletes"));
		int updateChargeListlength = Integer.parseInt(request.getParameter("noOfUpdates"));
		int approveChargeIdListlength = Integer.parseInt(request.getParameter("noOfApprovals"));
		//int totalNoOfRows = request.getParameterValues("patientName").length;

		for(int i=0;i<deleteChargeIdListlength;i++){
			j=Integer.parseInt(transAppForm.getDeletedRow()[i]);
			j = j-1;
			ChargeDTO charge = new ChargeDTO();
			charge.setChargeId(transAppForm.getChargeId()[j]);
			charge.setBillNo(transAppForm.getBillNo()[j]);
			charge.setBillStatus(transAppForm.getBillStatus()[j]);
			if(transAppForm.getBillStatus()[j].equalsIgnoreCase("C")) {
				charge.setAmount(toBigDecimal(0));
			} else {
				charge.setAmount(toBigDecimal(transAppForm.getAmount()[j]));
			}
			charge.setActualAmount(toBigDecimal(transAppForm.getOrigAmount()[j]));
			deleteChargeIdList.add(charge);
			//deleteChargeIdList.add(transAppForm.getChargeId()[j]);
		}
		Timestamp postedDate = null;
		for(int i=0;i<updateChargeListlength;i++){
    		ChargeDTO charge = new ChargeDTO();
			try {
				postedDate = DateUtil.parseTimestamp(transAppForm.getPostedDate()[i]);
			} catch (ParseException e) {
				log.error("Could not parse date: " + dateStr, e);
			}

			j=Integer.parseInt(transAppForm.getUpdatedRow()[i]);
			j = j-1;
			charge.setChargeId(transAppForm.getChargeId()[j]);
			charge.setBillNo(transAppForm.getBillNo()[j]);
			charge.setBillStatus(transAppForm.getBillStatus()[j]);
			charge.setActRate(toBigDecimal(transAppForm.getRate()[j]));
			charge.setActQuantity(toBigDecimal(transAppForm.getQty()[j]));
			charge.setDiscount(toBigDecimal(transAppForm.getDiscount()[j]));
			charge.setAmount(toBigDecimal(transAppForm.getAmount()[j]));
			charge.setActualAmount(toBigDecimal(transAppForm.getOrigAmount()[j]));
			charge.setActRemarks(transAppForm.getRemarks()[j]);
			charge.setDiscountReason("");
			charge.setStatus(transAppForm.getChargeStatus()[j]);
			charge.setUsername(userid);
			charge.setPostedDate(postedDate);
			charge.setModTime(DateUtil.getCurrentTimestamp());
    		updateChargeList.add(charge);
    	}

    	for(int i=0;i<approveChargeIdListlength;i++){
    		j=Integer.parseInt(transAppForm.getApprovedRow()[i]);
			j = j-1;
			approveChargeIdList.add(transAppForm.getChargeId()[j]);
    	}
    	String result = chargeBOObj.updateCharges(deleteChargeIdList, updateChargeList, approveChargeIdList, userid);
    	if(result.equalsIgnoreCase("Redo"))
    		request.setAttribute("error", "Redo the adjustments, Insufficient receipt amounts");
    	else if(result.equalsIgnoreCase("success"))
    		request.setAttribute("success", "Transactions saved successfully");
    	else request.setAttribute("resultMsg", "");

		return mapping.findForward("populatedetails");
	}

	/*
	 * Retrieve the basic search screen only: initial default search values,
	 * no search executed.
	 */
	@IgnoreConfidentialFilters
	public ActionForward getScreen(ActionMapping mapping, ActionForm f, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		/*TransactionApprovalForm taForm = (TransactionApprovalForm) f;
		taForm.setPatTypeAll(true);
		taForm.setBillStatusAll(true);
		taForm.setChrGrpAll(true);
		taForm.setChrStatsActive(true);
		taForm.setPatTypeInsuAll(true);*/

		return mapping.findForward("populatedetails");
	}

	public static java.sql.Date parseDate(String dateStr) throws java.text.ParseException {
		if ( (dateStr != null) && !dateStr.equals("")) {
		  SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dt = dateFormatter.parse(dateStr);
			return new java.sql.Date(dt.getTime());
		} else {
			return null;
		}
	}

	private BigDecimal toBigDecimal(float f){
		return BigDecimal.valueOf(f);
	}
}
