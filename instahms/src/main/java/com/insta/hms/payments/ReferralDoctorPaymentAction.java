package com.insta.hms.payments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



public class ReferralDoctorPaymentAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(ReferralDoctorPaymentAction.class);
	@IgnoreConfidentialFilters
	public ActionForward getReferralDoctorPaymentScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession session = (HttpSession)request.getSession(false);
		Integer centerId = (Integer)session.getAttribute("centerId");
		ReferralDoctorPaymentForm doctorForm = (ReferralDoctorPaymentForm)form;
		JSONSerializer js = new JSONSerializer().exclude("class");
		List doctorlist = PaymentsDAO.getReferralDoctorsArrayList();
		request.setAttribute("doctorlist",js.serialize((doctorlist)));
		request.setAttribute("refPayment", "update");

		ChargeHeadsDAO chargesDAO = new ChargeHeadsDAO();
		request.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		request.setAttribute("chargeHeads", chargesDAO.getPayableChargeHeads());

		return mapping.findForward("referraldoctorscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward getReferralDoctorSearch(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		saveToken(request);
		JSONSerializer js = new JSONSerializer().exclude("class");
		List doctorlist = PaymentsDAO.getReferralDoctorsArrayList();
		request.setAttribute("doctorlist",js.serialize(doctorlist));

		Map map = getParameterMap(request);

		//referral doctor posted charges

		String[] chargePageSize = {"10"};
		map.put("pageSize", chargePageSize);
		map.put("pageNum", request.getParameterValues("_chargePageNum"));

		PagedList chargeList = PaymentsDAO.getReferralDoctorCharges(map,
				ConversionUtils.getListingParameter(map), "search");
		BigDecimal chargeAmount = PaymentsDAO.getReferralDoctorPayment(map,
				ConversionUtils.getListingParameter(map));
		request.setAttribute("chargeList",chargeList);
		request.setAttribute("chargeAmount", chargeAmount);


		String[] paymentPageSize = {"10"};
		map.put("pageSize", paymentPageSize);
		map.put("pageNum", request.getParameterValues("_paymentPageNum"));

		//referral doctor paid charges
		PagedList paymentChargeList = PaymentsDAO.getReferralDoctorPaymentCharges(
				map, ConversionUtils.getListingParameter(map));
		BigDecimal paidAmount = PaymentsDAO.getReferralDoctorPaidCharges(map,
				ConversionUtils.getListingParameter(map));
		request.setAttribute("paymentChargeList",paymentChargeList);
		request.setAttribute("refPaidAmount", paidAmount);

		request.setAttribute("refPayment", "edit");
		ChargeHeadsDAO chargesDAO = new ChargeHeadsDAO();
		request.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		request.setAttribute("chargeHeads", chargesDAO.getPayableChargeHeads());
		request.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		return mapping.findForward("referraldoctorscreen");
	}//doctor Search ends


	public ActionForward createPaymentDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		ReferralDoctorPaymentForm doctorForm = (ReferralDoctorPaymentForm) form;
		HttpSession session = request.getSession();
		int j=0;
		String userId = (String) session.getAttribute("userid");
		int doctorChargeLength = Integer.parseInt(doctorForm.get_noOfCharges());
		String option = request.getParameter("_allCharges");
		GenericDAO billChargeDao = new GenericDAO("bill_charge");


		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
			if (option.equals("all")){
				Map map = getParameterMap(request);
				PagedList chargeList = PaymentsDAO.getReferralDoctorCharges(map,
						ConversionUtils.getListingParameter(map), "allItems");
				if (chargeList != null){
					List refChargeList = chargeList.getDtoList();
					Iterator refIt = refChargeList.iterator();
					while (refIt.hasNext()){
						java.util.Date posteDate =  new java.util.Date();
						java.sql.Date pdate = new java.sql.Date(posteDate.getTime());
						PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
						Map refBean = (Map) refIt.next();
						String doctor = ((String) refBean.get("reference_docto_id"));
					//		.substring(0,doctorForm.getReference_docto_id().indexOf("-"));

						pdDTO.setDescription((String) refBean.get("act_description"));
						pdDTO.setCategory((String) refBean.get("chargehead_name"));
						pdDTO.setAmount((BigDecimal) refBean.get("referal_amount"));
						pdDTO.setPostedDate(pdate);
						if (doctor.startsWith("DOC")){
							pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_REFERRAL);
						}else{
							pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_OTHER_REFERRAL);
						}
						pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
						pdDTO.setUsername(userId);
						pdDTO.setPayeeName((String) refBean.get("reference_docto_id"));
						pdDTO.setChargeId((String) refBean.get("charge_id"));
						pdDTO.setCenterId((Integer)refBean.get("center_id"));
						paymentDetails.add(pdDTO);
					}
				}
			}else{
				for(int i=0;i<doctorChargeLength;i++){
					java.util.Date posteDate =  new java.util.Date();
					java.sql.Date pdate = new java.sql.Date(posteDate.getTime());

					String doctor =  doctorForm.getReference_docto_id();
					//.substring(0,doctorForm.getReference_docto_id().indexOf("-"));

					PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
					j = Integer.parseInt(doctorForm.get_statusCheck()[i]);
					j =j-1;
					BasicDynaBean bean = null;
					bean = billChargeDao.findByKey("charge_id", doctorForm.get_chargeId()[j]);
					if (bean.get("ref_payment_id") == null || bean.get("ref_payment_id").equals("")) {
						pdDTO.setDescription(doctorForm.get_actDescription()[j]);
						pdDTO.setCategory(doctorForm.get_chargeHeadName()[j]);
						pdDTO.setAmount(new BigDecimal(doctorForm.get_doctorFees()[j].trim()));
						pdDTO.setPostedDate(pdate);
						if (doctor.startsWith("DOC")){
							pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_REFERRAL);
						}else{
							pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_OTHER_REFERRAL);
						}
						pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
						pdDTO.setUsername(userId);
						pdDTO.setPayeeName(doctor);
						pdDTO.setChargeId(doctorForm.get_chargeId()[j]);
						pdDTO.setCenterId(Integer.parseInt(doctorForm.get_centerId()[j]));
						paymentDetails.add(pdDTO);
					}
				}
			}
			PaymentsBO.makePayments(paymentDetails);

		FlashScope flash = FlashScope.getScope(request);
		flash.put("msg", "Referral Payment Done");
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer"));

		String[] docId = request.getParameterValues("reference_docto_id");
		for(int i=0;i<docId.length;i++){
			redirect.addParameter("reference_docto_id", docId[i]);
		}

		redirect.addParameter("mr_no", request.getParameter("_mr_no"));

		redirect.addParameter("referal_amount", request.getParameter("_referal_amount"));
		redirect.addParameter("referal_amount@op", "gt");
		redirect.addParameter("referal_amount@type", "integer");


		String[] postedDate =  request.getParameterValues("_finalized_date");
		if (postedDate != null ){
			for (int p=0; p<postedDate.length; p++){
				redirect.addParameter("finalized_date", postedDate[p]);
			}
			redirect.addParameter("finalized_date@op", "ge,le");
		}

		String[] visitType = request.getParameterValues("_visit_type");
		if (visitType != null) {
			for(int v=0;v<visitType.length;v++){
				redirect.addParameter("visit_type", visitType[v]);
			}
		}
		String[] chargegroup = request.getParameterValues("_charge_group");
		if (chargegroup != null){
			for(int cg=0;cg<chargegroup.length;cg++){
				redirect.addParameter("charge_group", chargegroup[cg]);
			}
		}

		String[] insurancestatus = request.getParameterValues("_insurancestatus");
		if (insurancestatus != null){
			for(int t=0;t<insurancestatus.length;t++){
				redirect.addParameter("insurancestatus", insurancestatus[t]);
			}
		}

		return redirect;
	}


	public ActionForward deleteDoctorCharge(ActionMapping mapping,ActionForm form,
			HttpServletRequest request,HttpServletResponse response)throws Exception{

		ReferralDoctorPaymentForm doctorForm = (ReferralDoctorPaymentForm)form;
		ArrayList doctorlist = PaymentsDAO.getAllDoctorsList();
		request.setAttribute("doctorlist",doctorlist);
		HttpSession session = request.getSession();
		String userId =(String) session.getAttribute("userid");
		int j=0;

		int deletedRowsLength = Integer.parseInt(doctorForm.get_deleteRows());
		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		for(int i=0;i<deletedRowsLength;i++){
			PaymentDetailsDTO pddto = new PaymentDetailsDTO();
			j = Integer.parseInt(doctorForm.getDeleteCharge()[i]);
			j=j-1;
			pddto.setPaymentId(doctorForm.get_delPaymentId()[j]);
			pddto.setChargeId(doctorForm.get_delchargeId()[j]);
			pddto.setPackagCharge(doctorForm.get_delpackageCharge()[j]);
			pddto.setPaymentType(PaymentDetails.PAYMENT_TYPE_REFERRAL);
			paymentDetails.add(pddto);
		}
		Connection con = DataBaseUtil.getConnection();
		con.setAutoCommit(false);
		boolean success = false;
		try{
			success = PaymentsDAO.deletePaymentItems(con, paymentDetails);
			success &= PaymentsBO.updateDoctorChargeId(con, paymentDetails);
			if (success) con.commit();
			else con.rollback();
		}finally {
			DataBaseUtil.closeConnections(con, null);
		}

		request.setAttribute("refPayment", "edit");
		FlashScope flash = FlashScope.getScope(request);
		flash.put("msg", "Deleted Refferal doctor payments");
		ActionRedirect redirect = new ActionRedirect(request.getHeader("Referer"));

		String[] docId = request.getParameterValues("reference_docto_id");
		for(int i=0;i<docId.length;i++){
			redirect.addParameter("reference_docto_id", docId[i]);
		}

		redirect.addParameter("mr_no", request.getParameter("_mr_no"));

		redirect.addParameter("referal_amount", request.getParameter("_referal_amount"));
		redirect.addParameter("referal_amount@op", "gt");
		redirect.addParameter("referal_amount@type", "integer");


		String[] postedDate =  request.getParameterValues("_finalized_date");
		if (postedDate != null ){
			for (int p=0; p<postedDate.length; p++){
				redirect.addParameter("finalized_date", postedDate[p]);
			}
			redirect.addParameter("finalized_date@op", "ge,le");
		}

		String[] visitType = request.getParameterValues("_visit_type");
		if (visitType != null) {
			for(int v=0;v<visitType.length;v++){
				redirect.addParameter("visit_type", visitType[v]);
			}
		}
		String[] chargegroup = request.getParameterValues("_charge_group");
		if (chargegroup != null){
			for(int cg=0;cg<chargegroup.length;cg++){
				redirect.addParameter("charge_group", chargegroup[cg]);
			}
		}

		String[] insurancestatus = request.getParameterValues("_insurancestatus");
		if (insurancestatus != null){
			for(int t=0;t<insurancestatus.length;t++){
				redirect.addParameter("insurancestatus", insurancestatus[t]);
			}
		}

		return redirect;
	}

	@IgnoreConfidentialFilters
	public ActionForward getChargeHeadValues(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
		String chargeHead = req.getParameter("chargeHead");
		List chargeHeads = null;
		JSONSerializer js = new JSONSerializer();
		List chargeHeadValues = null;
		if (chargeHead.equals("LTDIA")){
			chargeHeadValues = PaymentsDAO.getChargeHeadValues("DEP_LAB", "Payment");
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else if (chargeHead.equals("RTDIA")){
			chargeHeadValues = PaymentsDAO.getChargeHeadValues("DEP_RAD","Payment");
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else if (chargeHead.equals("SERSNP")) {
			chargeHeadValues = PaymentsDAO.getServiceList("Payment");
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else{

		}
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		js.serialize(chargeHeads, res.getWriter());
		return null;
	}

}

