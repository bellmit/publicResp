package com.insta.hms.payments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class OuthouseTestPaymentsAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(OuthouseTestPaymentsAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getOutHousePaymentScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws Exception {
		List ohList = PaymentsDAO.getOutHouseList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("outhouselist", js.serialize(ohList));
		return m.findForward("outhousepaymentscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward outHouseSearch(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res)throws Exception {
		List ohList = PaymentsDAO.getOutHouseList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("outhouselist", js.serialize(ohList));

		Map map = getParameterMap(req);

		String[] chargePageSize = {"10"};
		map.put("pageSize", chargePageSize);
		map.put("pageNum", req.getParameterValues("_chargePageNum"));

		PagedList ohChargeList = PaymentsDAO.getOhPaymentCharges(map,
				ConversionUtils.getListingParameter(map), "search");

		String[] paymentPageSize = {"10"};
		map.put("pageSize", paymentPageSize);
		map.put("pageNum", req.getParameterValues("_paymentPageNum"));

		PagedList ohPaymentPostedList = PaymentsDAO.getOhPostedCharges(map,
				ConversionUtils.getListingParameter(map));

		req.setAttribute("ohChargeList", ohChargeList);
		req.setAttribute("ohPostedList", ohPaymentPostedList);
		return	m.findForward("outhousepaymentscreen");
	}


	public ActionForward createOhPaymentDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		OuthouseTestPaymentsForm ohform  = (OuthouseTestPaymentsForm) form;
		HttpSession session = request.getSession();
		int j=0;
		String userId = (String) session.getAttribute("userid");
		int ohChargeLength = Integer.parseInt(ohform.get_noOfCharges());

		Map filter = getParameterMap(request);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String selectOption = request.getParameter("_allCharges");
		java.util.Date posteDate =  new java.util.Date();
		java.sql.Date pdate = new java.sql.Date(posteDate.getTime());
		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		GenericDAO billChargeDao = new GenericDAO("bill_charge");

		if (selectOption.equals("all")){
			PagedList ohChargeList = PaymentsDAO.getOhPaymentCharges(filter,
					ConversionUtils.getListingParameter(filter), "allItems");
			if (ohChargeList != null){
				List chargeList  = ohChargeList.getDtoList();
				Iterator it = chargeList.iterator();
				while (it.hasNext()){
					Map chargeMap = (Map)it.next();
					PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
					pdDTO.setDescription((String) chargeMap.get("act_description"));
					pdDTO.setCategory((String) chargeMap.get("chargehead_name"));
					pdDTO.setAmount((BigDecimal)(chargeMap.get("out_house_amount")));
					pdDTO.setPostedDate(pdate);
					pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_OT);
					pdDTO.setUsername(userId);
					pdDTO.setPayeeName((String) chargeMap.get("outhouse_id"));
					pdDTO.setChargeId((String) chargeMap.get("charge_id"));
					pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
					pdDTO.setCenterId((Integer)chargeMap.get("center_id"));
					paymentDetails.add(pdDTO);
				}
			}

		}else{
			for(int i=0;i<ohChargeLength;i++){
				PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
				j = Integer.parseInt(ohform.getStatusCheck()[i]);
				j =j-1;
				BasicDynaBean bean = null;
				bean = billChargeDao.findByKey("charge_id", ohform.get_chargeId()[j]);
				if (bean.get("oh_payment_id") == null || bean.get("oh_payment_id").equals("")) {

					pdDTO.setDescription(ohform.get_actDescription()[j]);
					pdDTO.setCategory(ohform.get_chargeHeadName()[j]);
					pdDTO.setAmount(new BigDecimal(ohform.get_ohPayment()[j].trim()));
					pdDTO.setPostedDate(pdate);
					pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_OT);
					pdDTO.setUsername(userId);
					pdDTO.setPayeeName(ohform.getOuthouse_id());
					pdDTO.setChargeId(ohform.get_chargeId()[j]);
					pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
					pdDTO.setCenterId(Integer.parseInt(ohform.get_centerId()[j]));
					paymentDetails.add(pdDTO);
				}
			}
		}
		PaymentsBO.makePayments(paymentDetails);
		FlashScope flash = FlashScope.getScope(request);
		flash.put("msg", "payment done");
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer"));

		String[] docId = request.getParameterValues("outhouse_id");
		for(int i=0;i<docId.length;i++){
			redirect.addParameter("outhouse_id", docId[i]);
		}

		redirect.addParameter("mr_no", request.getParameter("_mr_no"));

		String[] postedDate =  request.getParameterValues("_finalized_date");
		if (postedDate != null ){
			for (int p=0; p<postedDate.length; p++){
				redirect.addParameter("finalized_date", postedDate[p]);
			}
			redirect.addParameter("finalized_date@op", "ge,le");
		}
		return redirect;
	}

	public ActionForward deleteOhCharge(ActionMapping mapping,ActionForm form,
			HttpServletRequest req, HttpServletResponse res)throws Exception{

		OuthouseTestPaymentsForm ohForm = (OuthouseTestPaymentsForm)form;
		List<BasicDynaBean> bean = PaymentsDAO.getOutHouseList();
		req.setAttribute("outhouselist", bean);
		HttpSession session = req.getSession();
		String userId =(String) session.getAttribute("userid");
		int j=0;

		int deletedRowsLength = Integer.parseInt(ohForm.get_deleteRows());
		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		for(int i=0;i<deletedRowsLength;i++){
			PaymentDetailsDTO pddto = new PaymentDetailsDTO();
			j = Integer.parseInt(ohForm.getDeleteCharge()[i]);
			j=j-1;
			pddto.setPaymentId(ohForm.get_delPaymentId()[j]);
			pddto.setChargeId(ohForm.get_delchargeId()[j]);
			pddto.setPaymentType(PaymentDetails.PAYMENT_TYPE_OT);
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
		FlashScope flash = FlashScope.getScope(req);
		flash.put("msg", "payments deleted");
		ActionRedirect redirect = new ActionRedirect(req.getHeader("Referer"));

		String[] docId = req.getParameterValues("outhouse_id");
		for(int i=0;i<docId.length;i++){
			redirect.addParameter("outhouse_id", docId[i]);
		}

		redirect.addParameter("mr_no", req.getParameter("_mr_no"));

		String[] postedDate =  req.getParameterValues("_finalized_date");
		if (postedDate != null ){
			for (int p=0; p<postedDate.length; p++){
				redirect.addParameter("finalized_date", postedDate[p]);
			}
			redirect.addParameter("finalized_date@op", "ge,le");
		}

		return redirect;
	}

	public ActionForward outhouseTotalAmount(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception{
		Map filter = getParameterMap(req);
		String[] fromDate = (String[])filter.get("_fromDate");
		String[] toDate = (String[]) filter.get("_toDate");
		String[] finalDate = new String[2];
		if (fromDate[0] != null && !fromDate[0].equals("")){
			String[] op = {"ge","le"};
			String[] type = {"date"};
			finalDate[0] = fromDate[0];
			finalDate[1] = toDate[0];
			filter.put("finalized_date", finalDate);
			filter.put("finalized_date@op" , op);
			filter.put("finalized_date@type", type);
		}

		BigDecimal totalAmount = PaymentsDAO.outhouseAmount(filter,
				ConversionUtils.getListingParameter(filter));
		JSONSerializer js = new JSONSerializer().exclude("class");
		res.setContentType("appication/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		res.getWriter().write(js.serialize(totalAmount));
		return null;
	}


}
