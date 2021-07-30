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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DoctorPaymentsAction extends BaseAction{

	static Logger logger = LoggerFactory.getLogger(DoctorPaymentsAction.class);

	@IgnoreConfidentialFilters
	public ActionForward paymentDues(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception {

		String type = m.getProperty("type");
		if (type.equalsIgnoreCase("docPayment")) {
			return getDoctorPaymentScreen(m,f,req,res);
		}

		return m.findForward("doctorpaymentscreen");
	}

	@IgnoreConfidentialFilters
	private ActionForward getDoctorPaymentScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		List doctorlist = PaymentsDAO.getAllDoctorsList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		request.setAttribute("doctorlist",js.serialize(doctorlist));

		List docDeptNameList = PaymentsDAO.getDoctorDeptList();
        request.setAttribute("docDeptNameList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));

		ChargeHeadsDAO chargesDAO = new ChargeHeadsDAO();
		request.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		request.setAttribute("screen", "Payment");
		return mapping.findForward("doctorpaymentscreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward getDoctorSearch(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		List doctorlist = PaymentsDAO.getAllDoctorsList();
		JSONSerializer js = new JSONSerializer().exclude("class");
		ChargeHeadsDAO chargesDAO = new ChargeHeadsDAO();
		Map map =  getParameterMap(request);

		String[] chargePageSize = {"10"};
		map.put("pageSize", chargePageSize);
		map.put("pageNum", request.getParameterValues("_chargePageNum"));

		//doctor posted charges
		Map chargeMap = PaymentsDAO.getDoctorCharges(map, ConversionUtils.getListingParameter(map), "search");
		PagedList chargeList =(PagedList) chargeMap.get("doctorChargesList");
		request.setAttribute("chargeAmount", (BigDecimal) chargeMap.get("doctorAmount"));
		request.setAttribute("chargeList",chargeList);

		List docDeptNameList = PaymentsDAO.getDoctorDeptList();
        request.setAttribute("docDeptNameList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));

		String[] paymentPageSize = {"10"};
		map.put("pageSize", paymentPageSize);
		map.put("pageNum", request.getParameterValues("_paymentPageNum"));
		//doctor paid charges
		Map paidDoctorChargeMap = PaymentsDAO.getDoctorPaymentCharges(map,
				ConversionUtils.getListingParameter(map));
		PagedList paidChargeList = (PagedList) paidDoctorChargeMap.get("doctorPaymentPaidList");

		request.setAttribute("docPaidAmount", (BigDecimal) paidDoctorChargeMap.get("docPaidAmount"));
		request.setAttribute("paymentChargeList",paidChargeList);

		request.setAttribute("doctorlist",js.serialize(doctorlist));
		request.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		request.setAttribute("map", request.getParameterMap());
		request.setAttribute("screen", "Payment");
		return mapping.findForward("doctorpaymentscreen");
	}

	public ActionForward createPaymentDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		DoctorPaymentsForm doctorForm = (DoctorPaymentsForm) form;
		HttpSession session = request.getSession();
		int j=0;
		Map chargeMap = null;
		PagedList chargeList = null;
		String userId = (String) session.getAttribute("userid");
		String screen = request.getParameter("_screen");
		int doctorChargeLength = Integer.parseInt(doctorForm.get_noOfCharges());
		GenericDAO billActivityDao = new GenericDAO("bill_activity_charge");
		GenericDAO chargeDao = new GenericDAO("bill_charge");
		LinkedHashMap<String, Object> lMap = new LinkedHashMap<String, Object>();
		List<String> colList = new ArrayList<String>();

		Map map = getParameterMap(request);

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		String option = request.getParameter("_allCharges");
		ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
		if (option.equals("all")){
			chargeMap = PaymentsDAO.getDoctorCharges(map,
					ConversionUtils.getListingParameter(map), "allItems");
			chargeList =(PagedList) chargeMap.get("doctorChargesList");

			if (chargeList != null ){
				List docChargeList = chargeList.getDtoList();
				Iterator docIt = docChargeList.iterator();
				while ( docIt.hasNext()){
					java.util.Date posteDate =  new java.util.Date();
					java.sql.Date pdate = new java.sql.Date(posteDate.getTime());
					Map docBean = (Map)	docIt.next();
					PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
					pdDTO.setDescription((String)docBean.get("act_description"));
					pdDTO.setCategory((String) docBean.get("chargehead_name"));
					pdDTO.setAmount((BigDecimal) docBean.get("doctor_amount"));
					pdDTO.setPostedDate(pdate);
					pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_DOCTOR);
					pdDTO.setUsername(userId);
					pdDTO.setPayeeName((String) docBean.get("payee_doctor_id"));
					pdDTO.setChargeId((String) docBean.get("charge_id"));
					pdDTO.setPackagCharge((String) docBean.get("package_charge"));
					pdDTO.setPkgActivityId((String) docBean.get("bac_activity_id"));
					pdDTO.setPkgActivityCode((String) docBean.get("bac_activity_code"));
					pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
					pdDTO.setCenterId((Integer)docBean.get("center_id"));
					paymentDetails.add(pdDTO);
				}
			}
		}else{
			for(int i=0;i<doctorChargeLength;i++){
				java.util.Date posteDate =  new java.util.Date();
				java.sql.Date pdate = new java.sql.Date(posteDate.getTime());

				PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
				j = Integer.parseInt(doctorForm.getStatusCheck()[i]);
				j =j-1;

				BasicDynaBean bean = null;
				String paymentId = null;
				lMap.put("activity_code", doctorForm.get_pkgActivityCode()[j]);
				lMap.put("activity_id", doctorForm.get_pkgActivityId()[j]);

				if (doctorForm.get_packageCharge()[j].equals("Y")) {
					bean = billActivityDao.findByKey(colList, lMap);
					paymentId = (String)bean.get("doctor_payment_id");
				} else {
					bean = chargeDao.findByKey("charge_id", doctorForm.get_chargeId()[j]);
					paymentId = (String)bean.get("doc_payment_id");
				}

				if (paymentId == null || paymentId.equals("")) {
					pdDTO.setDescription(doctorForm.get_actDescription()[j]);
					pdDTO.setCategory(doctorForm.get_chargeHeadName()[j]);
					pdDTO.setAmount(new BigDecimal(doctorForm.get_doctorFees()[j].trim()));
					pdDTO.setPostedDate(pdate);
					pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_DOCTOR);
					pdDTO.setUsername(userId);
					pdDTO.setPayeeName(doctorForm.getPayee_doctor_id());
					pdDTO.setChargeId(doctorForm.get_chargeId()[j]);

					//For Package Charges
					pdDTO.setPackagCharge(doctorForm.get_packageCharge()[j]);
					pdDTO.setPkgActivityId(doctorForm.get_pkgActivityId()[j]);
					pdDTO.setPkgActivityCode(doctorForm.get_pkgActivityCode()[j]);
					pdDTO.setCenterId(Integer.parseInt(doctorForm.get_centerId()[j]));

					pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
					paymentDetails.add(pdDTO);
				}
			}
		}
		PaymentsBO.makePayments(paymentDetails);
		FlashScope flash = FlashScope.getScope(request);
		flash.put("sucess", "doctor payment has done ");
		ActionRedirect redirect = null;
		
		redirect = new ActionRedirect(request.getHeader("Referer"));

		String[] docId = request.getParameterValues("payee_doctor_id");
		for(int i=0;i<docId.length;i++){
			redirect.addParameter("payee_doctor_id", docId[i]);
		}

		redirect.addParameter("mr_no", request.getParameter("_mr_no"));

		redirect.addParameter("doctor_amount", request.getParameter("_doctor_amount"));
		redirect.addParameter("doctor_amount@op", "gt");
		redirect.addParameter("doctor_amount@type", "integer");


		String[] postedDate =  request.getParameterValues("_bc_posted_date");
		if (postedDate != null ){
			for (int p=0; p<postedDate.length; p++){
				redirect.addParameter("bc_posted_date", postedDate[p]);
			}
			redirect.addParameter("bc_posted_date@op", request.getParameter("_bc_posted_date@op"));
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

		DoctorPaymentsForm doctorForm = (DoctorPaymentsForm)form;
		ArrayList doctorlist = PaymentsDAO.getAllDoctorsList();
		request.setAttribute("doctorlist",doctorlist);
		HttpSession session = request.getSession();
		String screen = request.getParameter("_screen");
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

			//For Package Charges
			pddto.setPackagCharge(doctorForm.get_delpackageCharge()[j]);
			pddto.setPkgActivityId(doctorForm.get_delpkgActivityId()[j]);
			pddto.setPkgActivityCode(doctorForm.get_delpkgActivityCode()[j]);


			pddto.setPaymentType(PaymentDetails.PAYMENT_TYPE_DOCTOR);
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

		FlashScope flash = FlashScope.getScope(request);
		flash.put("sucess", "doctor payment has done ");
		ActionRedirect redirect = null;

		redirect = new ActionRedirect(request.getHeader("Referer"));

		String[] docId = request.getParameterValues("payee_doctor_id");
		for(int i=0;i<docId.length;i++){
			redirect.addParameter("payee_doctor_id", docId[i]);
		}

		redirect.addParameter("mr_no", request.getParameter("_mr_no"));

		redirect.addParameter("doctor_amount", request.getParameter("_doctor_amount"));
		redirect.addParameter("doctor_amount@op", "gt");
		redirect.addParameter("doctor_amount@type", "integer");

		String [] postedDate =  request.getParameterValues("_bc_posted_date");
		if (postedDate != null){
			for (int p=0; p<postedDate.length; p++){
				redirect.addParameter("bc_posted_date", postedDate[p]);
			}
			redirect.addParameter("bc_posted_date@op", request.getParameter("_bc_posted_date@op"));
		}
		String[] visitType = request.getParameterValues("_visit_type");
		if (visitType != null){
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
		String screenType = req.getParameter("screen");
		List chargeHeads = null;
		JSONSerializer js = new JSONSerializer();
		List chargeHeadValues = null;
		if (chargeHead.equals("LTDIA")){
			chargeHeadValues = PaymentsDAO.getChargeHeadValues("DEP_LAB", screenType);
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else if (chargeHead.equals("RTDIA")){
			chargeHeadValues = PaymentsDAO.getChargeHeadValues("DEP_RAD",screenType);
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else if (chargeHead.equals("SERSNP")) {
			chargeHeadValues = PaymentsDAO.getServiceList(screenType);
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else{

		}
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		res.getWriter().write(js.serialize(chargeHeads));
		return null;
	}

}//Action ends
