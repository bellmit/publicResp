package com.insta.hms.core.billing;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.common.taxation.ItemTaxDetails;
import com.insta.hms.common.taxation.TaxContext;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.paymentgateway.TransactionRequirements;
import com.insta.hms.integration.salucro.SalucroService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xpath.operations.Bool;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(URLRoute.BILL)
public class BillController extends BaseRestController{
  
  static Logger log = LoggerFactory.getLogger(BillController.class);
	
	@LazyAutowired
	private BillHelper billHelper;
	
	@LazyAutowired
	private TaxSubGroupRepository taxSubGrp;
	
	@LazyAutowired
	private SessionService sessionService;
	
	@LazyAutowired
	private BillService billService;
	
	@LazyAutowired
	private HospitalCenterService centerService;
	
	@LazyAutowired
	private PatientDetailsService patientDetailsService;
	
	@LazyAutowired
  private RegistrationService regService;
	
	@LazyAutowired
	private PatientInsurancePlansService patInsPlansService;
	
	@LazyAutowired
	private TpaService tpaService;

	@LazyAutowired
	private AllocationService allocationService;
 
	@LazyAutowired
	private SalucroService salucroService;
	
  private String flowType;

  BillController(String flowType) {
    super();
    this.flowType = flowType;
  }

  BillController() { super(); }

  @GetMapping(URLRoute.VIEW_INDEX_URL)
  public ModelAndView getIndexPage() {
    String bundle = "v12";
    if (this.flowType == "ipFlow") {
      bundle = "ipFlow";
    }
    return renderFlowUi("Billing", bundle, "withFlow", this.flowType, "billing", false);
  }
	
	@PostMapping(value = URLRoute.ITEM_TAX)
	public Object getItemTaxDetails(HttpServletRequest request) throws Exception{
		
		Map<String, Object> itemMap = new HashMap<>();
		
		String chargeGroup = null;
		String itemId = null;
		String chargeHead = null;
		int consId = 0;
		String opId= null;
		String subGrpIds[] = null;
		
		List<BigDecimal> amounts = new ArrayList<>();
		
		Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
		Integer centerId = (Integer) sessionAttributes.get("centerId");
		BasicDynaBean centerBean = centerService.findByKey(centerId);
		
		Map<String, Object> reqMap = ConversionUtils.flatten(request.getParameterMap());
		BasicDynaBean tpaBean = null;
		subGrpIds = request.getParameterValues("sub_group_ids");
		String mrNo = (String)reqMap.get("mr_no");
		BasicDynaBean patientBean = patientDetailsService.findByKey(mrNo);
		
		if (null == patientBean && billHelper.isNotNull(reqMap.get("nationality_id"))) {
			patientBean = patientDetailsService.getBean();
		}
		if (billHelper.isNotNull(reqMap.get("nationality_id"))) {
			patientBean.set("nationality_id", reqMap.get("nationality_id"));
		}
		String billNo = (String)reqMap.get("bill_no");
		BasicDynaBean billBean = billService.findByKey(billNo);
		if (null == billBean) {
			billBean = billService.getBean();
			Boolean isTpa = false;
			if(billHelper.isNotNull(reqMap.get("is_tpa"))) {
				isTpa = Boolean.parseBoolean((String) reqMap.get("is_tpa"));
			}
			billBean.set("is_tpa", isTpa);
		}
		
		BasicDynaBean visitBean = regService.findByKey((String)billBean.get("visit_id"));
		Map<String,Object> patFilterkeys = new HashMap<>();
		patFilterkeys.put("patient_id", (String)billBean.get("visit_id"));
		patFilterkeys.put("priority", 1);
		BasicDynaBean patientTpaBean = patInsPlansService.findByKeys(patFilterkeys);
		if(null != patientTpaBean){
			Map<String,Object> tpaFilterKeys= new HashMap<>();
			String sponsorId = null;
			if(billHelper.isNotNull(reqMap.get("sponsor_id"))) {
				sponsorId = (String) reqMap.get("sponsor_id");
			} else {
				sponsorId = (String) patientTpaBean.get("sponsor_id");
			}
			tpaFilterKeys.put("tpa_id", sponsorId);
			tpaBean = tpaService.findByPk(tpaFilterKeys);
		}
		
		if(billHelper.isNotNull(reqMap.get("item_id"))) {
			itemId = (String)reqMap.get("item_id");
		}
		if(billHelper.isNotNull(reqMap.get("charge_group"))) {
			chargeGroup = (String)reqMap.get("charge_group");
		}
		boolean isBulk = false;
		if(billHelper.isNotNull((String)reqMap.get("isBulk"))) {
			isBulk = Boolean.parseBoolean((String)reqMap.get("isBulk"));
		}

		Object amountObject = reqMap.get("amount");
		if (amountObject instanceof String && billHelper.isNotNull((String) amountObject)) {
			amounts.add(new BigDecimal((String) amountObject));
		} else if (amountObject instanceof String[] && billHelper
				.isNotNull((String[]) amountObject)) {
			for (String amount : (String[]) amountObject) {
				amounts.add(new BigDecimal(amount));
			}
		}

		if(billHelper.isNotNull(reqMap.get("charge_head"))) {
			chargeHead = (String)reqMap.get("charge_head");
		}
		if(billHelper.isNotNull((String)reqMap.get("consultation_type_id"))) {
			consId = Integer.parseInt((String)reqMap.get("consultation_type_id"));
		}
		if(billHelper.isNotNull(reqMap.get("op_id"))) {
			opId = (String)reqMap.get("op_id");
		}
		
		String chargeId = (String)reqMap.get("charge_id");

		TaxContext taxContext = new TaxContext();
		taxContext.setCenterBean(centerBean);
		taxContext.setBillBean(billBean);
		taxContext.setPatientBean(patientBean);
		taxContext.setVisitBean(visitBean);
		taxContext.setItemBean(tpaBean);
		
		if(null != itemId && null != chargeGroup) {
			// Used to get the Item sub group codes.
			
			List<BasicDynaBean> subGroupCodes = new ArrayList<>();
			
			if(null != subGrpIds && subGrpIds.length > 0){
				String[] subGroups = null;
				
				if(null != subGrpIds[0]){
					subGroups = subGrpIds[0].split(",");
				}
				if(null != subGroups){
					for(String subGrpId : subGroups){
						if(!subGrpId.isEmpty() && !subGrpId.equals("undefined")){
							BasicDynaBean bean = taxSubGrp.getDetails(Integer.parseInt(subGrpId));
							bean.set("item_subgroup_id", Integer.parseInt(subGrpId));
							subGroupCodes.add(bean);
						}

					}
				}
			}
			
			if(subGroupCodes.isEmpty()){
				if(!chargeId.startsWith("_"))
					subGroupCodes = billHelper.getItemSubgroupCodes(chargeId);
				
				if(subGroupCodes.isEmpty()){
					subGroupCodes = billHelper.getItemSubgroupCodes(itemId, chargeGroup, chargeHead, consId, opId);
				}else{
					taxContext.setTransactionId(chargeId);
				}
			}

			ItemTaxDetails itemTaxDetails = new ItemTaxDetails();
			if(amounts.isEmpty()){
				itemTaxDetails.setAmount(BigDecimal.ZERO);
				Map<Integer, Object> taxMap = billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
				return isBulk ? Collections.singletonList(taxMap) : taxMap;
			}else if(!isBulk){
				itemTaxDetails.setAmount(amounts.get(0));
				return billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes);
			}else{
				List<Map<Integer, Object>> taxList = new ArrayList<>();
				for(BigDecimal amount : amounts){
					itemTaxDetails.setAmount(amount);
					taxList.add( billHelper.getTaxChargesMap(itemTaxDetails, taxContext, subGroupCodes));
				}
				return taxList;
			}
		}
	
		return Collections.emptyMap();
	}

	@PostMapping(value = URLRoute.DO_TRANSACTION)
	public Map<String,String> doTransaction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		TransactionRequirements transactionReq = new TransactionRequirements();
		transactionReq.setAmount(Float.parseFloat(request.getParameter("pay")));
		transactionReq.setBillNumber(request.getParameter("billNo"));
		int mode = Integer.parseInt(request.getParameter("mode"));
		
		if(mode == -4) {
			return billService.doPineLabsTransaction(transactionReq,request.getParameterMap()); 
		} else if(mode == -3) {
			return null;
			// do loyalty card transaction
		} else if(mode == -2) {
			return null;
			// do payTm transaction
		} else if(mode == -10) {
			//do  salucro transaction
			if(request.getParameter("paymentType").equals("receipt_settlement") || 
					request.getParameter("paymentType").equals("receipt_advance")){
				return salucroService.doSalucroSettlement(transactionReq,request.getParameterMap());		
			} else if(request.getParameter("paymentType").equals("refund")){
				return salucroService.doSalucroRefund(transactionReq,request.getParameterMap());
			}
		} else {
			throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
		}
        return null;	
	}
	
	@PostMapping(value = URLRoute.CANCEL_TRANSACTION)
	public Map<String,String> cancelTransaction(HttpServletRequest request, HttpServletResponse response) {
		TransactionRequirements transactionReq = new TransactionRequirements();
		transactionReq.setAmount(Float.parseFloat(request.getParameter("pay")));
		int mode = Integer.parseInt(request.getParameter("mode"));
		
		if(mode == -4) {
			return billService.cancelPineLabsTransaction(transactionReq,request.getParameterMap()); 
		} else if(mode == -3) {
			return null;
			// cancel loyalty card transaction
		} else if(mode == -2) {
			return null;
			// cancel payTm transaction
		} else {
			throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
		}
	}
	
	@PostMapping(value = URLRoute.CANCEL_PENDING_TRANSACTION)
	public Map<String,String> cancelPendingTransaction(HttpServletRequest request, HttpServletResponse response) {
		
		int mode = Integer.parseInt(request.getParameter("mode"));
		boolean forceClose = false;
		
		if(mode == -4) {
			String imei = request.getParameter("imei");
			if(request.getParameter("forceClose") != null){
			  forceClose = Boolean.parseBoolean(request.getParameter("forceClose"));
			}
			return billService.cancelPendingPineLabsTransaction(imei, forceClose); 
		} else {
			throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
		}
	}
	
	@PostMapping(value = URLRoute.CHECK_TRANSACTION_STATUS)
	public Map<String,String> checkTransactionStatus(HttpServletRequest request, HttpServletResponse response) {
		int mode = Integer.parseInt(request.getParameter("mode"));
		
		if(mode == -4) {
			return billService.checkPineLabsTransactionStatus(request.getParameterMap()); 
		} else if(mode == -3) {
			return null;
			// cancel loyalty card transaction
		} else if(mode == -2) {
			return null;
			// cancel payTm transaction
		} else {
			throw new HMSException(HttpStatus.BAD_REQUEST, "exception.bad.request", null);
		}
	}
	
	 @PostMapping(value = URLRoute.SET_PRINTED_STATUS)
	  public void setPrintedStatus(HttpServletRequest request, HttpServletResponse response) {
	    String billNo = request.getParameter("bill_no");
	    if(billNo == null || billNo.equals("")) {
	      throw new ValidationException("exception.bad.request");
	    }
	    int result = billService.updateBillPrintedStatus(billNo);
	    if(result == 0) {
	      throw new ValidationException("exception.bill.not.found");
	    }
	    
	  }
	 
	 @GetMapping(value = URLRoute.GET_OPEN_FINALIZED_BILL_DETAILS)
	 public Map<String,Object> getOpenFinalizedBillDetails(HttpServletRequest request, ModelMap mmap,
				HttpServletResponse response,@RequestParam(value = "mr_no") String mrNo){
  		String visitType = this.flowType.equals("opFlow") ? "o" : "i";
  		return billService.getOpenFinalizedBillDetails(mrNo, visitType);
	 }
	 
	 @GetMapping(value = URLRoute.GET_IS_BILL_LOCKED)
	 public Map<String,Boolean> getIsBillLocked(HttpServletRequest request, ModelMap mmap,
	     HttpServletResponse response,@RequestParam(value = "bill_no") String billNo){
	   Map<String, Boolean> returnMap = new HashMap<>();
	   returnMap.put("isLocked", billService.isBillLocked(billNo));
	   return returnMap;
	 }
	 
	 @GetMapping(value = URLRoute.RUN_ALLOCATION)
	 public List<BillChargeReceiptAllocationModel> runAllocation(@RequestParam("bill_no") String billNo) {
	   return allocationService.allocate(billNo);
	 }

	/**
	* Creates the salucro payment transaction.
	*
	* @param requestBody the request body
	* @return the response entity
	*/
	@PostMapping(value = "/paymentTransaction")
	public  Map<String, Object> paymentTransaction(
	    HttpServletRequest requestBody) {
	  return salucroService.processTransaction(requestBody);
	}	 
}
