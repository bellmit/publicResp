package com.insta.hms.payments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SupplierPaymentAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(SupplierPaymentAction.class);

	SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	@IgnoreConfidentialFilters
	public ActionForward getSupplierPaymentScreen(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		SupplierPaymentForm supplierForm = (SupplierPaymentForm)form;
		ArrayList list = PaymentsDAO.getSuppliers();
		JSONSerializer js= new JSONSerializer().exclude("class");
		request.setAttribute("supplierList",js.serialize(list));
		return mapping.findForward("supplierscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward makeDirectPayment(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		SupplierPaymentForm supplierForm = (SupplierPaymentForm)form;

		HttpSession session = request.getSession();
		String userId = (String)session.getAttribute("userid");
	 	String counterName = (String) session.getAttribute("billingcounterName");
	 	int centerId = (Integer)session.getAttribute("centerId");
		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		for (int i=0;i<supplierForm.getAmount().length;i++){
			java.util.Date postedDate = new java.util.Date();
			java.sql.Date pdate =new java.sql.Date(postedDate.getTime());
				if(supplierForm.getAmount()[i]!=null){
					PaymentDetailsDTO pd = new PaymentDetailsDTO();
					pd.setDescription(supplierForm.getDescription()[i]);
					pd.setAmount(new BigDecimal(supplierForm.getAmount()[i]));
					pd.setCategory("supplier");
					pd.setPostedDate(pdate);
					pd.setPaymentType(PaymentDetails.PAYMENT_TYPE_SUPPLIER);
					pd.setUsername(userId);
					pd.setPayeeName(supplierForm.getSupplier_id());
					pd.setCenterId(centerId);
					paymentDetails.add(pd);
				}//end if
		}//end for
		PaymentsDTO payment  = new PaymentsDTO();
		PaymentsBO.makePayments(paymentDetails);
		Map hm = new HashMap();
		BigDecimal amount =  new PaymentsDAO().getPayeeAmount(supplierForm.getSupplier_id(),
			 PaymentDetails.PAYMENT_TYPE_SUPPLIER,"Payment", "others" );
		hm.put("amount", amount);
		hm.put("payeeName",supplierForm.get_supplierName());
		hm.put("payeeId",supplierForm.getSupplier_id());
		hm.put("counterName",counterName);
		hm.put("paymentType",PaymentDetails.PAYMENT_TYPE_SUPPLIER);
		hm.put("screen", "Payment");
		hm.put("directPayment", "Y");

		request.setAttribute("paymentList",hm);
	return new ActionRedirect("PaymentDashboard.do?_method=getPaymentDues");

	}//end method

	@IgnoreConfidentialFilters
	public ActionForward searchSupplierCharges(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		ArrayList list = PaymentsDAO.getSuppliers();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("supplierList",js.serialize(list));

		Map map = getParameterMap(request);

		String[] chargePageSize = {"10"};
		map.put("pageSize", chargePageSize);
		map.put("pageNum",  request.getParameterValues("_chargePageNum"));

		String invoiceType =request.getParameter("_invoice_type");

		String[] op = {"ge,le"};
		String[] type = {"date"};

		if (invoiceType != null && invoiceType.equals("due")){
			map.put("due_date", request.getParameterValues("_invoice_date"));
			map.put("due_date@op", op);
			map.put("due_date@type", type);
		}else if(invoiceType!= null){
			map.put("invoice_date", request.getParameterValues("_invoice_date"));
			map.put("invoice_date@op", op);
			map.put("invoice_date@type", type);
		}
		String cashPurchase = (null == request.getParameter("_cashPurchase")) ? "" : "Y";
		String[] op1 = {"eq"};
		if (cashPurchase.equals("Y")){
			map.put("cash_purchase",request.getParameterValues("_cashPurchase"));
			map.put("cash_purchase@op",op1);
		}

		PagedList supplierChargeList = PaymentsDAO.getSupplierPendingCharges(map,
				ConversionUtils.getListingParameter(map), "search");

		String[] paymentPageSize = {"10"};
		map.put("pageSize", paymentPageSize);
		map.put("pageNum",  request.getParameterValues("_paymentPageNum"));

		PagedList supplierPaymentList = PaymentsDAO.getSupplierPostedCharges(map,
				ConversionUtils.getListingParameter(map));
		Map mapWithoutSort = map;
		mapWithoutSort.remove("sortOrder");//Removing sortOrder to make invoice_date sortable (Ref: HMS-20137)
		//replaced map with mapWithoutSort
		BigDecimal totalAmount = PaymentsDAO.getSupplierPaidAmount(mapWithoutSort,ConversionUtils.getListingParameter(mapWithoutSort));

		request.setAttribute("supplierChargeList",supplierChargeList);
		request.setAttribute("supplierPaymentList",supplierPaymentList);
		request.setAttribute("totalPaidAmount", totalAmount);

		return mapping.findForward("supplierscreen");
	}


	public ActionForward createPayments(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		SupplierPaymentForm supplierForm = (SupplierPaymentForm)form;
		HttpSession session = request.getSession();
		String userId = (String)session.getAttribute("userid");
		Map map = getParameterMap(request);
		Map<String, Object> queryParams = new HashMap<String, Object>();

		int i = 0;


		int supplierChargeLen = Integer.parseInt(supplierForm.get_addCharge());
		String option = request.getParameter("_allCharges");

		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		if (option.equals("all")){
			PagedList chargeList = PaymentsDAO.getSupplierPendingCharges(map,
					ConversionUtils.getListingParameter(map), "all");
			if (chargeList!=null){
				List supplierList = chargeList.getDtoList();
				Iterator supIt = supplierList.iterator();
				while (supIt.hasNext()){
					java.util.Date postedDate = new java.util.Date();
					java.sql.Date pdate =new java.sql.Date(postedDate.getTime());
					Map supBean = (Map) supIt.next();
					PaymentDetailsDTO pd = new PaymentDetailsDTO();
					pd.setDescription("");
					pd.setAmount((BigDecimal)supBean.get("final_amt"));
					pd.setCategory("supplier");
					pd.setPostedDate(pdate);
					pd.setPaymentType(PaymentDetails.PAYMENT_TYPE_SUPPLIER);
					pd.setUsername(userId);
					pd.setPayeeName((String)supBean.get("supplier_id"));
					pd.setGrnNo((String)supBean.get("invoice_no"));
					pd.setInvoiceType((String)supBean.get("invoice_type"));
					pd.setAccountGroup(((Integer)supBean.get("account_group")).toString());
					pd.setConsignmentStatus((String)supBean.get("consignment_status"));
					pd.setIssueId((Integer) supBean.get("issue_id"));
					pd.setCenterId((Integer)supBean.get("center_id"));
					paymentDetails.add(pd);
				}
			}
		}else{
		for (int j=0;j<supplierChargeLen;j++){
				java.util.Date postedDate = new java.util.Date();
				java.sql.Date pdate =new java.sql.Date(postedDate.getTime());

				PaymentDetailsDTO pd = new PaymentDetailsDTO();
				i = Integer.parseInt(supplierForm.getPaymentCheckBox()[j]);
				i = i-1;

				queryParams.put("grnNo", supplierForm.get_invoice_no()[i]);
				queryParams.put("supplier", supplierForm.getSupplier_id());
				queryParams.put("issueId", supplierForm.get_issueId()[i]);
				java.util.Date dt = dateFormatter.parse(supplierForm.get_invoice_date()[i]);
				java.sql.Date invDate =new java.sql.Date(dt.getTime());
				queryParams.put("invoice_date", invDate);

				BasicDynaBean bean = PaymentsDAO.isPaymentIdExists(supplierForm.get_invoiceType()[i], supplierForm.getConsignment_status()[i], queryParams);
				if (bean != null && (bean.get("payment_id") == null || bean.get("payment_id").equals(""))) {

					pd.setDescription(supplierForm.get_sdescription()[i]);
					pd.setAmount(new BigDecimal(supplierForm.get_pendingAmount()[i]));
					pd.setCategory("supplier");
					pd.setPostedDate(pdate);
					pd.setPaymentType(PaymentDetails.PAYMENT_TYPE_SUPPLIER);
					pd.setUsername(userId);
					pd.setPayeeName(supplierForm.getSupplier_id());
					pd.setGrnNo(supplierForm.get_invoice_no()[i]);
					pd.setInvoiceType(supplierForm.get_invoiceType()[i]);
					pd.setAccountGroup(supplierForm.get_accountGroup()[i]);
					pd.setConsignmentStatus(supplierForm.getConsignment_status()[i]);
					pd.setIssueId(supplierForm.get_issueId()[i]);
					pd.setCenterId(supplierForm.get_centerId()[i]);
					pd.setInvoiceDate(invDate);
					paymentDetails.add(pd);
				}
		}//end for
		}
		PaymentsBO.makePayments(paymentDetails);
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer"));
		redirect.addParameter("supplier_id", supplierForm.getSupplier_id());
		return redirect;
	}//end method


	public ActionForward deleteSupplierCharge(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{
		SupplierPaymentForm supplierForm = (SupplierPaymentForm)form;

		HttpSession session = request.getSession();
		String userId =(String) session.getAttribute("userid");
		int j=0;

		int deletedRowsLength = Integer.parseInt(supplierForm.get_deleteCharge());
		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		for(int i=0;i<deletedRowsLength;i++){
			PaymentDetailsDTO pddto = new PaymentDetailsDTO();
			j = Integer.parseInt(supplierForm.getPaidCheckBox()[i]);
			j=j-1;
			pddto.setPaymentId(supplierForm.get_delPaymentId()[j]);
			pddto.setGrnNo(supplierForm.get_delGrnNo()[j]);
			pddto.setPaymentType(PaymentDetails.PAYMENT_TYPE_SUPPLIER);
			pddto.setInvoiceType(supplierForm.get_delinvoiceType()[j]);
			pddto.setPayeeName(supplierForm.getSupplier_id());
			pddto.setConsignmentStatus(supplierForm.get_delConsignment_status()[i]);
			pddto.setIssueId(supplierForm.get_delIssueId()[i]);
			java.util.Date dt = dateFormatter.parse(supplierForm.get_delGrnDate()[i]);
			java.sql.Date invDate =new java.sql.Date(dt.getTime());
			pddto.setInvoiceDate(invDate);
			paymentDetails.add(pddto);

		}
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		try{
			success = PaymentsDAO.deletePaymentItems(con, paymentDetails);
			success &= PaymentsBO.updateDoctorChargeId(con, paymentDetails);
			if (success) {
				con.commit();
			}
			else {
				con.rollback();
			}
		}
		finally {
			DataBaseUtil.closeConnections(con, null);
		}
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer"));
		redirect.addParameter("supplier_id", supplierForm.getSupplier_id());
		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getAllSupplierAmount(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Map filter = getParameterMap(req);
		BigDecimal amount = PaymentsDAO.getSuppliersAmount(filter,
				ConversionUtils.getListingParameter(filter));
		JSONSerializer js = new JSONSerializer().exclude("class");
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		res.getWriter().write(js.serialize(amount));

		return null;
	}

}//end class


