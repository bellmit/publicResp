package com.insta.hms.payments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PaymentReversalAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(PaymentReversalAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getPaymentReversalScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		return m.findForward("paymentreversalscreen");
	}
	
	@IgnoreConfidentialFilters
	public void getPayeesList(ActionMapping mapping, ActionForm form,HttpServletRequest request,
      HttpServletResponse response) throws SQLException,IOException,ParseException {
	  JSONSerializer js = new JSONSerializer().exclude("class");
	  Map<String, String[]> params = request.getParameterMap();
	  String payeeType = null;
	  if (params.get("payee_type") != null) {
	    payeeType = params.get("payee_type")[0];
	  }
    List payeeList = PaymentsDAO.getPaidPayeesList(payeeType);
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(payeeList)));
    response.flushBuffer();
    return;
	}
	

	public ActionForward reversalPayment(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {
		PaymentReversalForm reversalForm =  (PaymentReversalForm) f;
		HttpSession session = req.getSession();
		HashMap params = new HashMap();
		String userid = (String)session.getAttribute("userid");
		PaymentsDTO payment = new PaymentsDTO();
		String payType = req.getParameter("paymentType");
		String accountHead = req.getParameter("accountHead");
		int centerId = (Integer)req.getSession(false).getAttribute("centerId");
		int accountHeadId = 0;
		if (null != accountHead && !accountHead.equals("") && payType.equals("C")) {
			accountHeadId = Integer.parseInt(accountHead);
		}
		String[] delPayments = req.getParameterValues("delPayment");

		BigDecimal totalAmount = BigDecimal.ZERO;

			/***************PAYMENT DETAILS *****************/

		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		for (int i=0; i<reversalForm.getAmount().length-1;i++){
			if (reversalForm.getAmount()[i]!=null && delPayments[i].equals("false")){
				totalAmount = totalAmount.add(new BigDecimal(reversalForm.getAmount()[i]));
				PaymentDetailsDTO pd = new PaymentDetailsDTO();
				pd.setDescription(reversalForm.getDescription()[i]);
				pd.setPostedDate(DataBaseUtil.parseDate(reversalForm.getPayDate()));
				pd.setAmount((BigDecimal.ZERO.subtract(new BigDecimal(reversalForm.getAmount()[i]))));
				pd.setPaymentType(payType);
				pd.setUsername(userid);
				pd.setCenterId(centerId);
				pd.setPayeeName(reversalForm.getPayeeId());
				if (payType.equals("C"))
					pd.setAccountHead(accountHeadId);
				paymentDetails.add(pd);
			}
		}
					/*******VOUCHER DETAILS ****/

		payment.setPayeeName(reversalForm.getPayeeId());
		payment.setType("R");
		payment.setAmount(BigDecimal.ZERO.subtract(totalAmount));
		payment.setTaxType("S");
		payment.setTaxAmount(BigDecimal.ZERO);
		String paymentTime = req.getParameter("payTime");
		payment.setPaymentDtTime(DateUtil.parseTimestamp(reversalForm.getPayDate(), paymentTime));
		payment.setCounter(reversalForm.getCounter());
		payment.setUsername(userid);
		payment.setPaymentModeId(reversalForm.getPaymentModeId());
		payment.setCardTypeId(reversalForm.getCardTypeId());

		payment.setBank(reversalForm.getPaymentBank());
		payment.setReferenceNo(reversalForm.getPaymentRefNum());
		BigDecimal tds = new BigDecimal(reversalForm.getTds()==null||reversalForm.getTds().equals("")?"0":reversalForm.getTds());
		payment.setTdsAmount(BigDecimal.ZERO.subtract(tds));
		payment.setRemarks(reversalForm.getPaymentRemarks());
		payment.setVoucherCategory(PaymentDetails.REVERSAL_PAYMENT_VOUCHER);
		payment.setPaymentType(payType);


		String voucherNo = PaymentsBO.createReversalVoucher(payment,payType, paymentDetails);
		if(voucherNo == null){
			req.setAttribute("error", "The specified voucher has not be created");
			req.setAttribute("voucherNo","Voucher not created");
		}else{
			req.setAttribute("success", "Voucher no " + voucherNo + " created successfully");
			req.setAttribute("voucherNo",voucherNo);
			req.setAttribute("printType", req.getParameter("voucherPrint"));
		}
	//	ActionRedirect redirect = new ActionRedirect(m.findForward("paymentreversalscreen"));
		return m.findForward("paymentreversalpaid");
	}
}
