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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrescribingDrPaymentsAction extends BaseAction{

	static Logger log = LoggerFactory.getLogger(PrescribingDrPaymentsAction.class);

	@IgnoreConfidentialFilters
	public ActionForward getPaymentDues(ActionMapping m,ActionForm f,
			HttpServletRequest req,HttpServletResponse res) throws Exception {

		String type = m.getProperty("type");
		if (type.equalsIgnoreCase("drPayment")) {
			return prescribingDrPaymentsScreen(m,f,req,res);
		}
		return m.findForward("prescribingDrScreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward prescribingDrPaymentsScreen(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException,
		   SQLException {
		PrescribingDrPaymentsForm doctorForm = (PrescribingDrPaymentsForm) f;
		List doctorlist = PaymentsDAO.getPrescribingDoctors();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("doctorlist",js.serialize(ConversionUtils.listBeanToListMap(doctorlist)));
		List docDeptNameList = PaymentsDAO.getPresDoctorDeptList();
        req.setAttribute("docDeptNameList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));
		ChargeHeadsDAO chargesDAO = new ChargeHeadsDAO();
		req.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		req.setAttribute("chargeHeads", chargesDAO.getPayableChargeHeads());
		req.setAttribute("screen", "Payment");
		return m.findForward("prescribingDrScreen");
	}

	@IgnoreConfidentialFilters
	public ActionForward prescribingDoctorSearch(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException,
		   ParseException, SQLException, Exception{
		saveToken(req);
		List doctorlist = PaymentsDAO.getPrescribingDoctors();
		JSONSerializer js = new JSONSerializer().exclude("class");
		req.setAttribute("doctorlist",js.serialize(ConversionUtils.listBeanToListMap(doctorlist)));
		List docDeptNameList = PaymentsDAO.getPresDoctorDeptList();
        req.setAttribute("docDeptNameList", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(docDeptNameList)));

		Map map = getParameterMap(req);

		// Prescribing doctor posted payments;

		String[] chargePageSize = {"10"};
		map.put("pageSize", chargePageSize);
		map.put("pageNum", req.getParameterValues("_chargePageNum"));


		Map presChargeMap = PaymentsDAO.getPrescribingDoctorCharges(map,
				ConversionUtils.getListingParameter(map), "search");
		PagedList presChargeList =(PagedList) presChargeMap.get("presDrPaymentList");
		BigDecimal presDrAmount = (BigDecimal) presChargeMap.get("presDrPaymentAmount");

		req.setAttribute("chargeList",presChargeList);
		req.setAttribute("presDrAmount", presDrAmount);

		// Prescribing doctor paid Payment;

		String[] paymentPageSize = {"10"};
		map.put("pageSize", paymentPageSize);
		map.put("pageNum", req.getParameterValues("_paymentPageNum"));

		Map paidChargeMap = PaymentsDAO.getPrescribingDrPaymentCharges(map,
				ConversionUtils.getListingParameter(map));
		PagedList paymentChargeList =(PagedList) paidChargeMap.get("presDrPaidPaymentList");
		BigDecimal presDrPaidAmount = (BigDecimal) paidChargeMap.get("presDrPaidPaymentAmount");

		req.setAttribute("paymentChargeList",paymentChargeList);
		req.setAttribute("presDrPaidAmount", presDrPaidAmount);

		ChargeHeadsDAO chargesDAO = new ChargeHeadsDAO();
		req.setAttribute("screen", "Payment");
		req.setAttribute("chargeGroups", chargesDAO.getPayableChargeGroups());
		req.setAttribute("chargeHeads", chargesDAO.getPayableChargeHeads());

		return m.findForward("prescribingDrScreen");
	}


	@IgnoreConfidentialFilters
	public ActionForward createPrescribedDrPaymentDetails(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException,
		   SQLException, ParseException, Exception {
			   PrescribingDrPaymentsForm doctorForm = (PrescribingDrPaymentsForm) f;
			   HttpSession session = req.getSession();
			   int j=0;
			   String userId = (String) session.getAttribute("userid");
			   int doctorChargeLength = Integer.parseInt(doctorForm.get_noOfCharges());

			   SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
			   String option = req.getParameter("_allCharges");
			   String screen = req.getParameter("_screen");

			   ArrayList<PaymentDetailsDTO> paymentDetails = new ArrayList<PaymentDetailsDTO>();
			   java.util.Date posteDate =  new java.util.Date();
			   java.sql.Date pdate = new java.sql.Date(posteDate.getTime());
			   GenericDAO billChargeDao = new GenericDAO("bill_charge");

			   Map presChargeMap= null;
			   PagedList presChargeList = null;

			   if (option.equals("all")){

				   Map map = getParameterMap(req);

				   presChargeMap = PaymentsDAO.getPrescribingDoctorCharges(map,
						   ConversionUtils.getListingParameter(map), "allItems");
				   presChargeList =(PagedList) presChargeMap.get("presDrPaymentList");

				   if (presChargeList != null){
					   List presDrList = presChargeList.getDtoList();
					   Iterator prescItr = presDrList.iterator();
					   while (prescItr.hasNext()){
						   Map presBean = (Map) prescItr.next();
						   PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
						   pdDTO.setDescription((String) presBean.get("act_description"));
						   pdDTO.setCategory((String) presBean.get("chargehead_name"));
						   pdDTO.setAmount((BigDecimal) presBean.get("prescribing_dr_amount"));
						   pdDTO.setPostedDate(pdate);
						   pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_PRESCRIBING_DOCTOR);
						   pdDTO.setUsername(userId);
						   pdDTO.setPayeeName((String) presBean.get("prescribing_dr_id"));
						   pdDTO.setChargeId((String) presBean.get("charge_id"));
						   pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
						   pdDTO.setCenterId((Integer)presBean.get("center_id"));
						   paymentDetails.add(pdDTO);
					   }
				   }
			   }else{
				   for(int i=0;i<doctorChargeLength;i++){
					   BasicDynaBean bean = null;

					   PaymentDetailsDTO pdDTO = new PaymentDetailsDTO();
					   j = Integer.parseInt(doctorForm.getStatusCheck()[i]);
					   j =j-1;
					   bean = billChargeDao.findByKey("charge_id", doctorForm.get_chargeId()[j]);
					   if (bean.get("prescribing_dr_payment_id") == null || bean.get("prescribing_dr_payment_id").equals("")) {
						   pdDTO.setDescription(doctorForm.get_actDescription()[j]);
						   pdDTO.setCategory(doctorForm.get_chargeHeadName()[j]);
						   pdDTO.setAmount(new BigDecimal(doctorForm.get_doctorFees()[j].trim()));
						   pdDTO.setPostedDate(pdate);
						   pdDTO.setPaymentType(PaymentDetails.PAYMENT_TYPE_PRESCRIBING_DOCTOR);
						   pdDTO.setUsername(userId);
						   pdDTO.setPayeeName(doctorForm.getPrescribing_dr_id());
						   pdDTO.setChargeId(doctorForm.get_chargeId()[j]);
						   pdDTO.setVoucherCategory(PaymentDetails.PAYMENT_VOUCHER);
						   pdDTO.setCenterId(doctorForm.get_centerId()[j]);
						   paymentDetails.add(pdDTO);
					   }
				   }
			   }
			   PaymentsBO.makePayments(paymentDetails);
			   FlashScope flash = FlashScope.getScope(req);
			   flash.put("sucess", "doctor payment has done ");
			   ActionRedirect redirect = null;

			   redirect = new ActionRedirect(req.getHeader("Referer"));

			   String[] docId = req.getParameterValues("prescribing_dr_id");
			   for(int i=0;i<docId.length;i++){
				   redirect.addParameter("prescribing_dr_id", docId[i]);
			   }

			   redirect.addParameter("mr_no", req.getParameter("_mr_no"));

			   redirect.addParameter("prescribing_dr_amount", req.getParameter("_prescribing_dr_amount"));
			   redirect.addParameter("prescribing_dr_amount@op", "gt");
			   redirect.addParameter("prescribing_dr_amount@type", "integer");

			   String [] postedDate =  req.getParameterValues("_");
			   if (postedDate != null){
				   for (int p=0; p<postedDate.length; p++){
					   redirect.addParameter("bc_posted_date", postedDate[p]);
				   }
				   redirect.addParameter("bc_posted_date@op", req.getParameter("_bc_posted_date@op"));
			   }
			   String[] visitType = req.getParameterValues("_visit_type");
			   if (visitType != null){
				   for(int v=0;v<visitType.length;v++){
					   redirect.addParameter("visit_type", visitType[v]);
				   }
			   }
			   String[] chargegroup = req.getParameterValues("_charge_group");
			   if (chargegroup != null){
				   for(int cg=0;cg<chargegroup.length;cg++){
					   redirect.addParameter("charge_group", chargegroup[cg]);
				   }
			   }

			   String[] insurancestatus = req.getParameterValues("_insurancestatus");
			   if (insurancestatus != null){
				   for(int t=0;t<insurancestatus.length;t++){
					   redirect.addParameter("insurancestatus", insurancestatus[t]);
				   }
			   }
			return redirect;
	}

	public ActionForward deletePrescribingDrCharges(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException,
		   SQLException,Exception {
			   PrescribingDrPaymentsForm doctorForm = (PrescribingDrPaymentsForm)f;
			   ArrayList doctorlist = PaymentsDAO.getAllDoctorsList();
			   String screen = req.getParameter("_screen");
			   req.setAttribute("doctorlist",doctorlist);
			   HttpSession session = req.getSession();
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
				   pddto.setPaymentType(PaymentDetails.PAYMENT_TYPE_PRESCRIBING_DOCTOR);
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
			   flash.put("sucess", "prescrining doctor payment has done ");
			   ActionRedirect redirect = null;

			   redirect = new ActionRedirect(req.getHeader("Referer"));

			   String[] docId = req.getParameterValues("prescribing_dr_id");
			   for(int i=0;i<docId.length;i++){
				   redirect.addParameter("prescribing_dr_id", docId[i]);
			   }

			   redirect.addParameter("mr_no", req.getParameter("_mr_no"));

			   redirect.addParameter("prescribing_dr_amount", req.getParameter("_prescribing_dr_amount"));
			   redirect.addParameter("prescribing_dr_amount@op", "gt");
			   redirect.addParameter("prescribing_dr_amount@type", "integer");

			   String [] postedDate =  req.getParameterValues("_");
			   if (postedDate != null){
				   for (int p=0; p<postedDate.length; p++){
					   redirect.addParameter("bc_posted_date", postedDate[p]);
				   }
				   redirect.addParameter("bc_posted_date@op", req.getParameter("_bc_posted_date@op"));
			   }
			   String[] visitType = req.getParameterValues("_visit_type");
			   if (visitType != null){
				   for(int v=0;v<visitType.length;v++){
					   redirect.addParameter("visit_type", visitType[v]);
				   }
			   }
			   String[] chargegroup = req.getParameterValues("_charge_group");
			   if (chargegroup != null){
				   for(int cg=0;cg<chargegroup.length;cg++){
					   redirect.addParameter("charge_group", chargegroup[cg]);
				   }
			   }

			   String[] insurancestatus = req.getParameterValues("_insurancestatus");
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
			chargeHeadValues = PaymentsDAO.getChargeHeadValues("DEP_RAD", screenType);
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else if (chargeHead.equals("SERSNP")) {
			chargeHeadValues = PaymentsDAO.getServiceList(screenType);
			chargeHeads = ConversionUtils.listBeanToListMap(chargeHeadValues);
		}else{

		}
		res.setContentType("application/json");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		js.serialize(chargeHeads, res.getWriter());
		return null;
	}

}
