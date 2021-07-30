package com.insta.hms.payments;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;

import flexjson.JSONSerializer;


public class MiscellaneousPaymentsAction extends DispatchAction{

	 static Logger logger = LoggerFactory.getLogger(MiscellaneousPaymentsAction.class);

	 @IgnoreConfidentialFilters
	 public ActionForward getMiscPaymentScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		    saveToken(request);
			JSONSerializer js = new JSONSerializer().exclude("class");
			List accountHeads = ConversionUtils.listBeanToListMap(PaymentsDAO.getAccountHeads());
			request.setAttribute("accountHeadsJSON", js.serialize(accountHeads));
			return mapping.findForward("miscpaymentscreen");
	 }


	 @IgnoreConfidentialFilters
	 public ActionForward saveMiscPayments(ActionMapping mapping,ActionForm form,
			 HttpServletRequest request,HttpServletResponse response)throws Exception{
			 MiscellaneousPaymentsForm miscForm = (MiscellaneousPaymentsForm)form;
 	      	HttpSession session = request.getSession();
			String userid = (String)session.getAttribute("userid");
			int centerId = (Integer)session.getAttribute("centerId");
			String counterName = (String) session.getAttribute("billingcounterName");
			String screenAction = request.getParameter("screenAction");

			ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
			Map hm = new HashMap();

			int accountHead = 0;
			for (int i=0; i<miscForm.getAmount().length-1; i++){

				if(miscForm.getAmount()[i]!=null){
					if (miscForm.getAccountHead()[i] !=null &&
							!miscForm.getAccountHead()[i].equals("")){
						accountHead = Integer.parseInt(miscForm.getAccountHead()[i]);
					}else{
						accountHead = 0;
					}
					java.sql.Date postDate = new java.sql.Date(
							DateUtil.parseTimestamp(miscForm.getPaydate(),miscForm.getPayTime())
							.getTime() );
					PaymentDetailsDTO pd = new PaymentDetailsDTO();
					pd.setDescription(miscForm.getDescription()[i]);
					pd.setCategory(miscForm.getCategory()[i]);
					pd.setPostedDate(postDate);
					pd.setAmount(new BigDecimal(miscForm.getAmount()[i]));
					pd.setPaymentType(PaymentDetails.PAYMENT_TYPE_CASH);
					pd.setUsername(userid);
					pd.setPayeeName(miscForm.getName().trim());
					pd.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
					pd.setAccountHead(accountHead);
					pd.setCenterId(centerId);
					paymentDetails.add(pd);
				}
			}

			PaymentsDTO payment = new PaymentsDTO();
			PaymentsBO.makePayments(paymentDetails);
			if (screenAction.equals("save")){
			return new ActionRedirect("MiscPayments.do?_method=getMiscPaymentScreen");
			}else{

			BigDecimal amount =  new PaymentsDAO().getPayeeAmount(miscForm.getName().trim(), PaymentDetails.PAYMENT_TYPE_CASH ,"Payment", "others");
			hm.put("amount", amount);
			hm.put("payeeName",miscForm.getName().trim());
			hm.put("payeeId",miscForm.getName());
			hm.put("paymentType",PaymentDetails.PAYMENT_TYPE_CASH);
			hm.put("counterName",counterName);
			hm.put("screen", "Payment");
			request.setAttribute("paymentList",hm);
			request.setAttribute("actionUrl", "pages/payments/PaymentVoucherForOthers.do");
			return mapping.findForward("createvoucher");
			}
	 }
 }


