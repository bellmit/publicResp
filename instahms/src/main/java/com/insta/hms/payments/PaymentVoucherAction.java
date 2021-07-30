package com.insta.hms.payments;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.ReportPrinter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;

import flexjson.JSONSerializer;

public class PaymentVoucherAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(PaymentVoucherAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getPaymentDues(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception {

		String type = m.getProperty("type");
		if (type.equalsIgnoreCase("paymentVoucher")) {
			return getPaymentVoucherScreen(m,f,req,res);
		}
		return m.findForward("paymentvoucherscreen");
	}

	@IgnoreConfidentialFilters
	private ActionForward getPaymentVoucherScreen(ActionMapping mapping, ActionForm form,
			            HttpServletRequest request, HttpServletResponse response)throws Exception{
		JSONSerializer js = new JSONSerializer();
		List payeesNameList = PaymentsDAO.getPayeeList();
		List payeeList = ConversionUtils.listBeanToListMap(payeesNameList);
		request.setAttribute("screen", "Payment");
		request.setAttribute("payeeList", js.serialize(payeeList));
		return mapping.findForward("paymentvoucherscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward getPaymentVouchers(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception{
		JSONSerializer js = new JSONSerializer();
		List payeesNameList = PaymentsDAO.getPayeeList();
		List payeeList = ConversionUtils.listBeanToListMap(payeesNameList);

    Map map = getParameterMap(request);

    HttpSession session = request.getSession();
    Integer centerId = (Integer) session.getAttribute("centerId");

    if (centerId != 0) {
      map.put("center_id", new String[] { centerId + "" });
      map.put("center_id@type", new String[] { "integer" });
    }
		
		PagedList list = PaymentsDAO.getPaymentVouchers(map, ConversionUtils.getListingParameter(map));
		request.setAttribute("pagedList",list);
		request.setAttribute("screen", "Payment");
		request.setAttribute("payeeList", js.serialize(payeeList));
		return mapping.findForward("paymentvoucherscreen");
	}

	public ActionForward makePayment(ActionMapping mapping,ActionForm form,
		HttpServletRequest request,HttpServletResponse response)throws Exception{
		FlashScope flash = FlashScope.getScope(request);
		ActionRedirect redirect = null;
		PaymentVoucherForm voucherForm =  (PaymentVoucherForm)form;
		HttpSession session = request.getSession();
		String voucherType = mapping.getProperty("vouchertype");
		HashMap params = new HashMap();
		if (voucherForm.getScreen().equals("Payment")){
			redirect = new ActionRedirect(mapping.findForward("paymentvoucherdashboard"));
		}
		if (null != redirect) {
		  redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		}
			BigDecimal paymentDetailsSum = new PaymentsDAO().getPayeeAmount(voucherForm.getPayeeId(),
					voucherForm.getPaymentType(), voucherForm.getScreen(), voucherType);
			if (paymentDetailsSum ==null){
				redirect =
					new ActionRedirect(mapping.findForward("paymentvoucherdashboard"));
				flash.put("error", "Voucher not created , Due amount is ZERO for payee : "+voucherForm.getPayeeName());
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			}else{

			String userid = (String)session.getAttribute("userid");
			Integer centerId = (Integer)session.getAttribute("centerId");
			PaymentsDTO payment = new PaymentsDTO();
			String payType = request.getParameter("paymentType");
			payment.setPayeeName(voucherForm.getPayeeId());
			String  stax = voucherForm.getServiceTax();
			String voucherNo = null;
			String tds = voucherForm.getTds();
			String totalAmount = voucherForm.getTotalAmount();
			BigDecimal staxAmount = new BigDecimal(0);
			BigDecimal tdsAmount = new BigDecimal(0);
			BigDecimal per = new BigDecimal(100);

			if (stax !=null && !stax.equals("") )
				staxAmount = ((new BigDecimal(totalAmount)).multiply(new BigDecimal(stax))).divide(per);

			if ((tds != null && !tds.equals("")) && (totalAmount != null) && (!totalAmount.equals("")))
			tdsAmount = ((new BigDecimal(totalAmount)).multiply(new BigDecimal(tds))).divide(per);

			if (((paymentDetailsSum.compareTo(new BigDecimal(totalAmount)))==0)){
				payment.setType("S");
				payment.setAmount(new BigDecimal(voucherForm.getNetPayment()));
				payment.setTaxType("S");
				payment.setTaxAmount(staxAmount);
				String paymentTime = request.getParameter("payTime");
				payment.setPaymentDtTime(DateUtil.parseTimestamp(voucherForm.getPayDate(),paymentTime));
				payment.setCounter(voucherForm.getCounter());
				payment.setUsername(userid);
				payment.setPaymentModeId(voucherForm.getPaymentModeId());
				payment.setCardTypeId(voucherForm.getCardTypeId());
				payment.setBank(voucherForm.getPaymentBank());
				payment.setReferenceNo(voucherForm.getPaymentRefNum());
				payment.setTdsAmount(tdsAmount);
				payment.setRemarks(voucherForm.getPaymentRemarks());
				payment.setPaymentType(payType);
				if (!voucherForm.getRoundOffAmt().equals("") && voucherForm.getRoundOffAmt() != null){
					payment.setRoundOff(new BigDecimal(voucherForm.getRoundOffAmt()));
				}else{
					payment.setRoundOff(BigDecimal.ZERO);
				}
				payment.setDate(DateUtil.parseDate(voucherForm.getPayDate()));
				payment.setDirectPayment(request.getParameter("directPayment"));
				voucherNo = PaymentsBO.creatVoucher(payment,payType, voucherForm.getScreen(), voucherType, centerId);
			}
			if(voucherNo == null){
				if (paymentDetailsSum != new BigDecimal(totalAmount)){

					redirect =  new ActionRedirect(mapping.findForward("createvoucherRedirect"));
					redirect.addParameter("payeeId", voucherForm.getPayeeId());
					redirect.addParameter("paymentType", payType);
					redirect.addParameter("totalAmount", paymentDetailsSum.toString());
					redirect.addParameter("payeeName", voucherForm.getPayeeName());
					redirect.addParameter("screen", voucherForm.getScreen());
					redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
					flash.put("error", "Voucher amount was "+" " +totalAmount +" "+" and  it is not matching to total due amount \n," +
							" so please redo your operation.");
				}else{
					flash.put("error", "The specified voucher has not be created");
					flash.put("voucherNo","Voucher not created");
				}
			}else{
				flash.put("success", "Voucher no " + voucherNo + " created successfully");
				flash.put("voucherNo",voucherNo);
				flash.put("printType", request.getParameter("voucherPrint"));
				flash.put("paymentType", payType);
			}
			}
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward createVoucher(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		PaymentVoucherForm voucherForm = (PaymentVoucherForm)form;
		Map hm = new HashMap();
		PaymentsDAO paymentDAO = new PaymentsDAO();
		String voucherType = mapping.getProperty("vouchertype");
		BigDecimal amount = new PaymentsDAO().getPayeeAmount(voucherForm.getPayeeId(),
				voucherForm.getPaymentType(), voucherForm.getScreen(), voucherType);
		String path = mapping.getPath()+".do";
		BasicDynaBean payeeDetails = paymentDAO.getPayersDetails(voucherForm.getPayeeId());
		hm.put("amount", amount);
		hm.put("payeeName",(String)payeeDetails.get("payee_name"));
		hm.put("payeeId",voucherForm.getPayeeId());
		hm.put("paymentType",voucherForm.getPaymentType());
		hm.put("screen",voucherForm.getScreen());
		request.setAttribute("paymentList",hm);

		BasicDynaBean printPref = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
		request.setAttribute("pref",printPref);
		request.setAttribute("voucherType", voucherType);
		request.setAttribute("actionUrl", path);

		return mapping.findForward("createvoucher");
	}

	@IgnoreConfidentialFilters
	public ActionForward printAllVouchers(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		HashMap params = new HashMap();
		String paymentCategory = "";
		String payeeId = request.getParameter("payeeId");
		String payDate = "";

		String payType = request.getParameter("payment_type");
		if (payType !=null && !payType.equals("")){
			String paymentType = " and pd.payment_type in ( " +payType+ " )";
			params.put("paymentType" , paymentType);
		}else{
			params.put("paymentType" ,"");
		}

		if (!request.getParameter("payeeId").equals("")) {
			payeeId = " and p.payee_name = '"+request.getParameter("payeeId")+"'";
		}

		if (!request.getParameter("category").equals("")){
			paymentCategory = " and p.voucher_category = '"+request.getParameter("category")+"'" ;
		}

		if ((request.getParameter("fDate")!=null && !request.getParameter("fDate").equals(""))
				&& (request.getParameter("tDate")!=null) && !request.getParameter("tDate").equals("") ) {
			payDate = " and date(date) between '"+DataBaseUtil.parseDate(request.getParameter("fDate"))+"'and '"+
					DataBaseUtil.parseDate(request.getParameter("tDate"))+"'";
		}

		String screen = null; screen = "";

		params.put("payeeId", payeeId);
		params.put("paymentCategory", paymentCategory);
		params.put("payDate", payDate);
		params.put("screen", screen);

		ReportPrinter.printPdfStream(request,response,"AllPaymentVouchersReport",params);
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward exportPaymentDetails(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		PaymentVoucherForm voucherForm = (PaymentVoucherForm) form;
		PaymentSearchCriteria psc = new PaymentSearchCriteria();
		String payeeName = null;
		BaseAction bc = new BaseAction();
		Map map = bc.getParameterMap(request);
		String screenType = request.getParameter("screen");

		response.reset();
		response.setHeader("Content-type","application/csv");
		response.setHeader("Content-disposition","attachment; filename=PaymentVoucher.csv");
		response.setHeader("Readonly","true");

		CSVWriter writer = new CSVWriter(response.getWriter(),CSVWriter.DEFAULT_SEPARATOR);
		PaymentsDAO.exportPayments(writer, map, screenType);
		return null;
	}

}
