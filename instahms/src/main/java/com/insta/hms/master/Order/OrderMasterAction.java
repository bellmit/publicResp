package com.insta.hms.master.Order;

import com.bob.hms.common.DateUtil;
import com.bob.hms.otmasters.opemaster.OperationMasterDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillChargeTaxBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DiscountPlanBO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.EquipmentMaster.EquipmentMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.orders.ConsultationTypesDAO;
import com.insta.hms.orders.OrderBO;

import flexjson.JSONSerializer;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class OrderMasterAction extends DispatchAction {

    static Logger log = LoggerFactory.getLogger(OrderMasterAction.class);
    
    private static final GenericDAO billDAO = new GenericDAO("bill");

	/*
	 * Ajax call returns charges applicable for an item other than operation
	 */
	public ActionForward getItemCharges(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, SQLException, ParseException,Exception {

		String itemType = request.getParameter("type");              // like Laboratory, Equipment etc.
		String itemId = request.getParameter("id");                  // like DGC0001, DOC0002 etc.
		String chargeType = request.getParameter("chargeType");      // one of consultation type IDs
		if (chargeType == null || chargeType.isEmpty()) {
			chargeType = request.getParameter("charge_type");
		}
		String quantity = request.getParameter("quantity");
		String orgId = request.getParameter("orgId");
		if (orgId == null || orgId.isEmpty()) {
			orgId = request.getParameter("org_id");
		}
		String bedType = request.getParameter("bedType");
		if (bedType == null || bedType.isEmpty()) {
			bedType = request.getParameter("bed_type");
		}
		String fromDate = request.getParameter("fromDate");          // for equipment, OT and meal
		if (fromDate == null || fromDate.isEmpty()) {
			fromDate = request.getParameter("from_date");
		}
		String toDate = request.getParameter("toDate");              // for equipment and OT
		if (toDate == null || toDate.isEmpty()) {
			toDate = request.getParameter("to_date");
		}
		String units = request.getParameter("units");                // for equipment and OT
		String visitType = request.getParameter("visitType");
		if (visitType == null || visitType.isEmpty()) {
			visitType = request.getParameter("visit_type");
		}
		
		String admitingDept = request.getParameter("dept_name");    
		
		String finalized = request.getParameter("finalized");

		String ot = request.getParameter("ot");                      // for OT
		String surgeon = request.getParameter("surgeon");            // for OT
		String anaesthetist = request.getParameter("anaesthetist");  // for OT
		String operationId = request.getParameter("operationId");        // some doc charges require this
		String[] anesthesiaTypes = request.getParameterValues("anesthesiaType");
		String[] anesthesiaTypesFromDates = request.getParameterValues("anesthesiaTypeFromDate");
		String[] anesthesiaTypesToDates = request.getParameterValues("anesthesiaTypeToDate");
		String[] anesthesiaTypesFromTimes = request.getParameterValues("anesthesiaTypeFromTime");
		String[] anesthesiaTypesToTimes = request.getParameterValues("anesthesiaTypeToTime");
		String patientId =  request.getParameter("patientId");
		if (patientId == null || patientId.isEmpty()) {
			patientId = request.getParameter("patient_id");
		}
		String firstOfCategoryStr = request.getParameter("firstOfCategory");
		if (firstOfCategoryStr == null || firstOfCategoryStr.isEmpty()) {
			firstOfCategoryStr = request.getParameter("first_of_category");
		}
		Boolean firstOfCategory = firstOfCategoryStr==null || firstOfCategoryStr.equals("")? null: Boolean.valueOf(firstOfCategoryStr);
		String billNo = request.getParameter("billNo");
		if (billNo == null || billNo.isEmpty()) {
			billNo = request.getParameter("bill_no");
		}
		Boolean multiVisitPackage = new Boolean(request.getParameter("multi_visit_package"));
		String packObId = request.getParameter("pack_ob_id");
		String packageId = request.getParameter("package_id");
		String nationalityId = request.getParameter("nationality_id");

		if (orgId == null || orgId.isEmpty())
			orgId = "ORG0001";
		if (bedType == null || bedType.isEmpty())
			bedType = "GENERAL";

		DateUtil dateUtil = new DateUtil();
		Timestamp from = dateUtil.parseTheTimestamp(fromDate);
		Timestamp to = toDate == null ? null : dateUtil.parseTheTimestamp(toDate);

		BigDecimal qty = BigDecimal.ONE;
		if (quantity != null && !quantity.isEmpty()) {
			qty = new BigDecimal(quantity);
		}
		if (units == null || units.isEmpty()) {
			units = "D";
		}

		String planIdsStr = request.getParameter("planIds");
		if (planIdsStr == null || planIdsStr.isEmpty()) {
			planIdsStr = request.getParameter("plan_ids");
		}
		int planIds[] = null;
		if(null != planIdsStr && !planIdsStr.equals("")){
			String[] planIdsStrArray = planIdsStr.split(",");
			planIds = null != planIdsStrArray && planIdsStrArray.length > 0 ? new int[planIdsStrArray.length] : null;
			int planIdx = 0;
			if(null != planIdsStrArray && planIdsStrArray.length > 0) {
				for(int i=0; i < planIdsStrArray.length; i++){
					if ((planIdsStrArray[i] != null) && !planIdsStrArray[i].equals(""))
						planIds[planIdx++] = Integer.parseInt(planIdsStrArray[i]);
				}
			}
		}

		boolean isInsurance = null != planIds && planIds.length > 0;
		if (!isInsurance) {
			String isInsuranceStr = request.getParameter("insurance");
			if ((isInsuranceStr != null) && isInsuranceStr.equalsIgnoreCase("true"))
				isInsurance = true;
		}

		boolean isNonInsuBill = !isInsurance;

		// use "new" for non insurance bill
		isNonInsuBill = billNo != null && !billNo.equalsIgnoreCase("null") && !billNo.equals("newInsurance");

		List<ChargeDTO> charge = OrderBO.getItemCharges(orgId, bedType, itemType, itemId, chargeType, qty,
				from, to, units, ot, surgeon, anaesthetist, visitType, operationId, isInsurance,
				finalized,anesthesiaTypes, patientId, firstOfCategory, billNo, isNonInsuBill,multiVisitPackage,packObId,packageId,anesthesiaTypesFromDates,
				anesthesiaTypesToDates,anesthesiaTypesFromTimes, anesthesiaTypesToTimes);
		
		List<BasicDynaBean> planListBean = new PatientInsurancePlanDAO().getPlanDetails((patientId));

		if(null == planIds) {
			planIds = null!= planListBean && planListBean.size() > 0 ? new int[planListBean.size()] : null;
			for(int i=0; i<planListBean.size(); i++){
				planIds[i] = (Integer)planListBean.get(i).get("plan_id");
			}
		}

		Boolean isTpaBill = false;
		BasicDynaBean billBean = billDAO.findByKey("bill_no", billNo);
		if(null != billBean){
			isTpaBill = (Boolean)billBean.get("is_tpa");
		}else if(billNo != null && !billNo.equalsIgnoreCase("null") && billNo.equals("newInsurance")){
			isTpaBill = true;
		}

		int visitDiscountPlanId = 0;
		boolean isSystemDisc =false;
		if(null != patientId && !patientId.equals("") && !patientId.equalsIgnoreCase("undefined")){
			BasicDynaBean visitPrimPlanDetails = new PatientInsurancePlanDAO().getVisitPrimaryPlan(patientId);
			visitDiscountPlanId = (isTpaBill && visitPrimPlanDetails != null && visitPrimPlanDetails.get("discount_plan_id") != null
					? (Integer)visitPrimPlanDetails.get("discount_plan_id") : 0);
			if(null !=  visitPrimPlanDetails && null != visitPrimPlanDetails.get("discount_plan_id")) {
			  isSystemDisc = true;
			}
		}else{
			String discPlanStr = (String)request.getParameter("insurance_discount_plan");
			visitDiscountPlanId = (null != discPlanStr && !discPlanStr.equals("")) ? Integer.parseInt(discPlanStr) : 0;
		}

		if(visitDiscountPlanId == 0 && billNo != null && !billNo.equalsIgnoreCase("null")  && null != billBean){
			if(null != billBean.get("discount_category_id")){
				visitDiscountPlanId = (Integer)billBean.get("discount_category_id");
			}
		}

		List<BasicDynaBean> discountPlanDetails = new GenericDAO("discount_plan_details").
			listAll(null,"discount_plan_id", visitDiscountPlanId,"priority");

		for(ChargeDTO chg : charge){
			boolean isItemCategoryPayable = true;
			if(visitType != null && !visitType.equals("")) {
				isItemCategoryPayable = new DiscountPlanBO().
						isItemCategoryPayable(planIds != null && planIds.length >= 1 ? planIds[0] : 0 
								, visitType , chg.getInsuranceCategoryId() , 
								planIds != null && planIds.length >= 1);
			}
			
			chg.setInsuranceAmt(planIds, visitType, firstOfCategory);
			if(chg.isAllowDiscount()){
				BasicDynaBean discountRuleBean = getDiscountRule(chg, discountPlanDetails);
				if (null != discountRuleBean && isItemCategoryPayable) {
				  setDiscountRule(chg, discountRuleBean);
				  if(isSystemDisc)
				    chg.setIsSystemDiscount("Y");
				  else
				    chg.setIsSystemDiscount("N");
				}
			}
		}
		boolean newUi = this.parseBool((String)request.getParameter("new_ui"));
		BasicDynaBean visitBean = null;
		BasicDynaBean patientBean = null;
		BasicDynaBean tpaBean = null;
		if(null != billBean){
			visitBean = new VisitDetailsDAO().findByKey("patient_id", (String)billBean.get("visit_id"));
			patientBean = new PatientDetailsDAO().findPatientByMrno((String)visitBean.get("mr_no"));
		}else{
			if(isNonInsuBill){
				billBean = billDAO.getBean();
				billBean.set("is_tpa", false);
				visitBean = new VisitDetailsDAO().findByKey("patient_id", patientId);
				if(null != visitBean){
					patientBean = new PatientDetailsDAO().findPatientByMrno((String)visitBean.get("mr_no"));
				}
			}
		}
		if (newUi) {
			patientBean = new PatientDetailsDAO().getBean();
			patientBean.set("nationality_id", nationalityId);
			billBean = billDAO.getBean();
			billBean.set("is_tpa", isInsurance);
			String tpaId = request.getParameter("sponsor_id");
			tpaBean = new TpaMasterDAO().findByKey("tpa_id", tpaId);
			
		}
		if( visitBean == null ) {
		  visitBean = new VisitDetailsDAO().getBean();
		  visitBean.set("dept_name", admitingDept);
		}
		Integer centerId = com.bob.hms.common.RequestContext.getCenterId();
		BasicDynaBean centerBean = new GenericDAO("hospital_center_master").findByKey("center_id", centerId);
		for(ChargeDTO chg : charge){
			new BillChargeTaxBO().setTaxAmounts(chg,patientBean,billBean,centerBean,visitBean,tpaBean);
			chg.setAmount(chg.getAmount().add(chg.getTaxAmt()));
		}

		
		response.setContentType(newUi ? "application/json" : "text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		
		if (newUi) {
			List<Map<String,Object>> charge4NewUi = new ArrayList<Map<String,Object>>();
			ObjectMapper om = new ObjectMapper();
			for (ChargeDTO chg : charge) {
				Map<String,Object> map4NewUi = new HashMap<String,Object>(); 
				Map<String,Object> map = om.convertValue(chg, Map.class);
				for (Map.Entry<String,Object> entry : map.entrySet()) {
					map4NewUi.put(entry.getKey().replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(), entry.getValue());
				}
				charge4NewUi.add(map4NewUi);
			}
			response.getWriter().write(new ObjectMapper().writeValueAsString(charge4NewUi));
		} else {
			response.getWriter().write(new ObjectMapper().writeValueAsString(charge));
		}
		response.flushBuffer();
		return null;
	}

	private void setDiscountRule(ChargeDTO charge, BasicDynaBean discountRule) {
		BigDecimal disPerc =  BigDecimal.ZERO;
		BigDecimal disaAmt =  BigDecimal.ZERO;

		if ( discountRule != null ) {

			if( ((String)discountRule.get("discount_type")).equals("P") ) {
				disPerc = (BigDecimal)discountRule.get("discount_value");
			}

			if ( ((String)discountRule.get("discount_type")).equals("A") ) {
				disaAmt = (BigDecimal)discountRule.get("discount_value");
			} else {
				disaAmt = (charge.getActRate().multiply(charge.getActQuantity())).multiply(disPerc).divide(new BigDecimal(100));
			}
		}

		BigDecimal amt = charge.getActRate().multiply(charge.getActQuantity());

		if((disaAmt.compareTo(amt)) != -1)
			disaAmt = amt;
			
		amt = amt.subtract(disaAmt);
		charge.setAmount(amt);
		charge.setDiscount(disaAmt);
		charge.setOverall_discount_amt(disaAmt);
		charge.setOverall_discount_auth(0);

	}

	public BasicDynaBean getDiscountRule(ChargeDTO cdto, List<BasicDynaBean> discountPlanDetails) throws SQLException{

		BasicDynaBean discountRuleBean = null;

		/*
		 * applicable_type tells on which to apply discount rule.It can have 3 values.
		 * N  : insurance category id of item in the charge
		 * C  : charge head of the charge
		 * I  : item id of the charge.
		 *    : if it is item id there is one more parameter which will decide which type of item to look at it.
		 */
		for (BasicDynaBean detailBean :  discountPlanDetails ){
			if ( (detailBean.get("applicable_type").equals("N")
						&& cdto.getInsuranceCategoryId() == Integer.parseInt(((String)detailBean.get("applicable_to_id")).trim()) )
			  || (detailBean.get("applicable_type").equals("C")
					  	&& cdto.getChargeHead().equals(((String)detailBean.get("applicable_to_id")).trim()) )
			  || (detailBean.get("applicable_type").equals("I")
					&& cdto.getActDescriptionId() != null
					&& cdto.getActDescriptionId().equals(((String)detailBean.get("applicable_to_id")).trim()) )
					) {
				discountRuleBean = detailBean;
				break;
			}
		}

		return discountRuleBean;

	}

	public ActionForward getTotalPackageItemCost(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, SQLException, ParseException,Exception {

		String packageId = request.getParameter("item_id");
		List<BasicDynaBean> packageComponentDetails = PackageDAO.getPackageComponents(Integer.parseInt(packageId));
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		//response.getWriter().write(new JSONSerializer().exclude("class").serialize(charge));
		response.flushBuffer();
		return null;
	}

	private boolean parseBool(String value) {
		return value != null && Arrays.asList("true", "y", "yes", "1").contains(value.toLowerCase());
	}

	/*
	 * Cacheable list of orderable items.
	 */
	public ActionForward getOrderableItems(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, SQLException, ParseException {

		boolean newUi = this.parseBool((String)request.getParameter("new_ui"));
		boolean asJson = newUi || this.parseBool((String)request.getParameter("json"));
		
		if (!asJson && null != request.getHeader("If-Modified-Since")) {
			// the browser is just requesting the same thing, only if modified.
			// Since we encode the timestamp in the request, if we get a request,
			// it CANNOT have been modified. So, just say 304 not modified without checking.
			response.setStatus(304);
			return null;
		}
		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
		String orgId = (String)request.getParameter("orgId");
		if (orgId == null || orgId.isEmpty()) {
			orgId = (String)request.getParameter("org_id");
		}
		String dbMts = mst.get("master_count").toString();
		String mtsReceived = (String)request.getParameter("mts");
		
		if (asJson && mtsReceived != null && mtsReceived.equals(dbMts)) {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put(newUi ? "orderable_items" : "result", new ArrayList<String>());
			map.put(newUi ? "org_id" : "result", orgId);
			map.put("mts", Integer.parseInt(mtsReceived));
			response.setContentType("application/json");
			js.deepSerialize(map, response.getWriter());
			return null;
		}
		
		List orderableItems = null;

		String billNo = (String)request.getParameter("bill_no");
		String filter = (String)request.getParameter("filter");
		String orderable = (String)request.getParameter("orderable");
		String directBilling = (String)request.getParameter("directBilling");
		if (directBilling == null || directBilling.isEmpty()) {
			directBilling = (String)request.getParameter("direct_billing");
		}
		Boolean ignoreCenter = new Boolean(request.getParameter("ignoreCenter"));

		String visitType = (String)request.getParameter("visitType");
		if (visitType == null || visitType.isEmpty()) {
			visitType = (String)request.getParameter("visit_type");
		}
		String operationApplicable = (String)request.getParameter("operationApplicable");
		if (operationApplicable == null || operationApplicable.isEmpty()) {
			operationApplicable = (String)request.getParameter("operation_applicable");
		}
		String scriptvar = (String)request.getParameter("scriptvar");
		String packageApplicable = (String)request.getParameter("packageApplicable");
		if (packageApplicable == null || packageApplicable.isEmpty()) {
			packageApplicable = (String)request.getParameter("package_applicable");
		}
		Boolean isMultiVisitPackage = new Boolean(request.getParameter("isMultiVisitPackage"));
		if (request.getParameter("isMultiVisitPackage") == null || request.getParameter("isMultiVisitPackage").isEmpty()) {
			isMultiVisitPackage = new Boolean(request.getParameter("is_multi_visit_package"));
		}
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();

		// where patient is associated with a center patient center is used, else user center is used.
		// ex: in registration patient is not yet registered against the center, so in this screen user center is used.
		// where as in order screen patient is associated with perticular center there we will consider the patient center.
		String centerIdStr = (String) request.getParameter("center_id");
		Integer centerId = -1; // all centers
		if ((Integer) genericPrefs.get("max_centers_inc_default") > 1 && centerIdStr != null && !centerIdStr.equals(""))
			centerId = Integer.parseInt(centerIdStr);
		String tpaIdStr = (String) request.getParameter("tpaId");
		String tpaId = "-1"; // all tpas
		String deptId = (String) request.getParameter("dept_id");
		deptId = deptId != null && !"".equals(deptId) ? deptId: "*";
		String genderApplicability = StringUtils.isEmpty((String) request.getParameter("gender_applicability")) 
				? null : (String) request.getParameter("gender_applicability");
		if (genderApplicability == null) {
			genderApplicability = StringUtils.isEmpty((String)request.getParameter("genderApplicability")) 
				? null : (String)request.getParameter("genderApplicability");
		}
		Integer age = ((StringUtils.isNotEmpty(request.getParameter("age"))) && request.getParameter("age") != null)  
				? Integer.parseInt((String) request.getParameter("age")) : 0;
		String ageIn = ((StringUtils.isNotEmpty(request.getParameter("ageIn"))) && request.getParameter("ageIn") != null)
				? (String) request.getParameter("ageIn") : "";
		if (tpaIdStr != null && !tpaIdStr.equals(""))
			tpaId = tpaIdStr;
		else
			tpaId = "0";

		if ( billNo != null && !billNo.isEmpty()) {
			String visitId = (String)request.getParameter("visit_id");
			BasicDynaBean orgBean = BillBO.getRatePlanId(billNo, visitId, centerId);
			if (orgBean != null)
				orgId = (String)orgBean.get("org_id");
		}
		String planIdStr = (String) request.getParameter("planId");
		Integer planId = -1;
		if (planIdStr != null && !("").equals(planIdStr)) {
			planId = Integer.parseInt(request.getParameter("planId"));
		} else {
			planId = 0;
		}

		if (orgId == null || orgId.equals("")) orgId = "ORG0001";
		if (filter == null) filter = "";
		if (orderable == null) orderable = "";
		if (directBilling == null) directBilling = "";
		if (visitType == null) visitType = "";
		if (operationApplicable == null) operationApplicable = "";
		if (scriptvar == null) scriptvar = "rateplanwiseitems";
		if(packageApplicable == null)packageApplicable = "";
		if(packageApplicable.equals("i") || packageApplicable.equals("i"))
				packageApplicable = "Y";

		HashMap<String, Object> map = new HashMap<String, Object>();
		
		HttpSession session = request.getSession();
		String userControlApplicability = request.getParameter("order_controls_applicable");
		List userId  = null;
		if(userControlApplicability != null && userControlApplicability.equals("Y")) {
		  userId = (List)session.getAttribute("hospital_role_ids");
		} 
    
		orderableItems = OrderMasterDAO.getAllOrderableItems(orgId, visitType, filter, orderable, directBilling,
				operationApplicable,  packageApplicable, tpaId, centerId, deptId, genderApplicability, isMultiVisitPackage, userId, ignoreCenter, planId,
				age, ageIn);


		map.put(newUi ? "orderable_items" : "result", ConversionUtils.copyListDynaBeansToMap(orderableItems));
		map.put(newUi ? "org_id" : "orgId", orgId);

		if (newUi) {
			map.put("mts", dbMts);
			response.setContentType("application/json");
			js.deepSerialize(map, response.getWriter());
			return null;
		}
		// Last-Modified is required, cache-control is good to have to enable caching
		response.setHeader("Last-modified", "Thu, 1 Jan 2009 00:00:00 GMT");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "public; max-age=360000");
		response.setContentType("text/javascript");

		response.getWriter().write("var "+ scriptvar +"= ");
		js.deepSerialize(map, response.getWriter());
		response.getWriter().write(";");
		return null;
	}


	public ActionForward getPackageComponents (ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, SQLException, ParseException {

		String packageId = request.getParameter("packageId");
		if (packageId == null || packageId.isEmpty()) {
			packageId = request.getParameter("id");
		}
		String mrNo = request.getParameter("mr_no");
		boolean multiVisitPackage = Boolean.valueOf(new String(request.getParameter("multi_visit_package")));
		List multiVisitPackageComponentsQuantityDetails = null;
		JSONSerializer js = new JSONSerializer().exclude("class");
		boolean newUi = this.parseBool((String)request.getParameter("new_ui"));
		response.setContentType(newUi ? "application/json" : "text/plain");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        if(multiVisitPackage) {
        	multiVisitPackageComponentsQuantityDetails =
        		PackageDAO.getMultiVisitPackageComponentQuantityDetails(Integer.parseInt(packageId), mrNo);
        	if(multiVisitPackageComponentsQuantityDetails == null || multiVisitPackageComponentsQuantityDetails.size() < 1) {
        		multiVisitPackageComponentsQuantityDetails = PackageDAO.getFreshMultiVisitPackageComponentQuantityDetails(Integer.parseInt(packageId));
        	}
        }
        List packageComponentDetails = PackageDAO.getPackageComponents(Integer.parseInt(packageId));
        Map<String,Object> packComponentDetMap = new HashMap<String, Object>();

        packComponentDetMap.put(newUi ? "components" : "packComponentDetails", ConversionUtils.listBeanToListMap(packageComponentDetails));
        packComponentDetMap.put(newUi ? "usage" : "multVisitPackComponentQtyDetails", ConversionUtils.listBeanToListMap(multiVisitPackageComponentsQuantityDetails));
        response.getWriter().write(js.deepSerialize(packComponentDetMap));
        response.flushBuffer();
    	return null;
	}

	/**
	 * Writes an item details
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws ParseException
	 */
	public ActionForward getItemDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, SQLException, ParseException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		boolean newUi = this.parseBool((String)request.getParameter("new_ui"));
		response.setContentType(newUi ? "application/json" : "text/plain");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        String type = request.getParameter("type");
        List<Hashtable<String,String>> itemDetails = null;
        if(type.equalsIgnoreCase("Equipment"))
        	itemDetails = new EquipmentMasterDAO().getEquipmentDef(request.getParameter("id"));
        

		if (newUi) {
    		Map<String, Object> responseMap = new HashMap<String, Object>();
        	if (itemDetails.size() > 0) {
        		Hashtable<String,String> map = itemDetails.get(0);
        		for (Map.Entry<String, String> entry: map.entrySet()) {
        			responseMap.put(entry.getKey().toLowerCase(), entry.getValue());
        		}
        	} else {
        		responseMap.put("id", type + " not found");
        		response.setStatus(404);
        	}
            response.getWriter().write(js.serialize(responseMap));
        } else {
            response.getWriter().write(js.serialize(itemDetails.get(0)));
        }
        response.flushBuffer();
    	return null;

	}

	public ActionForward getOrderAlias(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, SQLException, ParseException{
		String type = request.getParameter("type");
		String deptId = request.getParameter("deptId");
		String groupId = request.getParameter("group");
		String subGrpId = request.getParameter("subgroup");
		BasicDynaBean masterCounts = new OrderMasterDAO().getMastersCounts(type, deptId);
		BasicDynaBean serviceGroup = new GenericDAO("service_groups").findByKey("service_group_id", new Integer(groupId));
		BasicDynaBean serviceSubGroup = new GenericDAO("service_sub_groups").findByKey("service_sub_group_id",  new Integer(subGrpId));
		String groupCode = (String)serviceGroup.get("service_group_code") == null?"":(String)serviceGroup.get("service_group_code");
		String subGrpCode = (String)serviceSubGroup.get("service_sub_group_code") == null ?"":(String)serviceSubGroup.get("service_sub_group_code");
		String count = (masterCounts == null)?"":masterCounts.get("count").toString();

		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write((groupCode)+(subGrpCode)+count);
        response.flushBuffer();
		return null;
	}

	public ActionForward getAllPlanCharges(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException {
		Integer planId = req.getParameter("planId")== null || req.getParameter("planId").equals("") ? 0 : Integer.parseInt(req.getParameter("planId"));
		if (planId == 0) {
			planId = req.getParameter("plan_id")== null || req.getParameter("plan_id").equals("") ? 0 : Integer.parseInt(req.getParameter("plan_id"));			
		}
		String visitType=req.getParameter("visitType");
		if (visitType == null || visitType.isEmpty()) {
			visitType=req.getParameter("visit_type");
		}
		if ( (visitType==null) || visitType.equals("") ) {
			log.error("getChargeAmtForPlanJSON: Visit Type could not be resolved");
			return null;
		}
		JSONSerializer js = new JSONSerializer().exclude("class");
		List lst= null;
		List planBean = new PlanDetailsDAO().getAllPlanCharges(planId, visitType);
		 lst = planBean== null? null : ConversionUtils.listBeanToListMap(planBean);
		String planJSON = js.serialize(lst);

        res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(planJSON);
        res.flushBuffer();
        return null;
	}

	public ActionForward getChargeHeadJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		BillBO billBOObj = new BillBO();
		List chargeHeadList = billBOObj.getChargeHeadConstNames();
		boolean newUi = this.parseBool((String)req.getParameter("new_ui"));
		res.setContentType(newUi ? "application/json" : "text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        String chargeHeadsJSON;
		if (newUi) {
			List<Map<String,Object>> chargeHeadList4NewUi = new ArrayList<Map<String,Object>>();
			ObjectMapper om = new ObjectMapper();
			for (Object chargeHead : chargeHeadList) {
				Map<String,Object> chargeHead4NewUi = new HashMap<String,Object>(); 
				Map<String,Object> map = om.convertValue(chargeHead, Map.class);
				for (Map.Entry<String,Object> entry : map.entrySet()) {
					chargeHead4NewUi.put(entry.getKey().replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase(), entry.getValue());
				}
				chargeHeadList4NewUi.add(chargeHead4NewUi);
			}
			chargeHeadsJSON = js.serialize(chargeHeadList4NewUi);
		} else {
			chargeHeadsJSON = js.serialize(chargeHeadList);
		}
		
        res.getWriter().write(chargeHeadsJSON);
        res.flushBuffer();
        return null;
	}

	public ActionForward getBillItemsCount(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException {
		String billNo = req.getParameter("bill_no");
		int itemCount = 0;
		itemCount = BillDAO.getBillItemsCount(billNo);
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(itemCount+"");
        res.flushBuffer();
        return null;
	}

	public ActionForward getMvPackageStatus(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException,Exception {
		String patPackId = req.getParameter("pat_pack_id");
		String packId = req.getParameter("package_id");

		String status = PackageDAO.getMultiVisitPackageStatus(Integer.parseInt(patPackId), Integer.parseInt(packId));
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(status);
        res.flushBuffer();
        return null;
	}

	public ActionForward getMultiVisitPackageBillDetails(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
    throws IOException, ServletException, SQLException,Exception {

		String billNo = req.getParameter("bill_no");
		Integer packageId = new BillBO().getMultiVisitBillPackageId(billNo);
		String status = (packageId != null && !packageId.equals(0)) ? packageId+"" : "";
		res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(status);
        res.flushBuffer();
		return null;
	}

	public ActionForward getDoctorAndOtDoctorCharges(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException{

		String includeOtDocCharges = req.getParameter("includeOtDocCharges");
		String orgId = req.getParameter("org_id");
		String visitType = req.getParameter("visit_type");

		if (includeOtDocCharges == null || includeOtDocCharges.equals("")) includeOtDocCharges = "N";
		if (orgId == null || orgId.isEmpty()) orgId = "ORG0001";
		if (visitType == null || visitType.isEmpty()) visitType = "i";
		java.util.List docCharges = null;
		java.util.List otDocCharges = null;

		Integer centerId = com.bob.hms.common.RequestContext.getCenterId();
		String healthAuthorityForCenter = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthorityForCenter).getHealth_authority();

	/*	java.util.Map patient = null;
		String moduleId = request.getParameter("moduleId");
		if (moduleId == null || (moduleId != null && !moduleId.equals("mod_insurance"))) {
			patient = (java.util.Map)request.getAttribute("patient");
		}*/

	/*	if(patient == null)//possible for Quickestimate screen
			orgId = (String)request.getAttribute("rate_plan") == null?
					(String)request.getParameter("rate_plan"):(String)request.getAttribute("rate_plan");
		if(patient != null && request.getAttribute("rate_plan") == null)
			orgId = (String)patient.get("org_id");

		orgId = orgId == null?"ORG0001":orgId;*/

		String operationApplicability = GenericPreferencesDAO.getGenericPreferences().getOperationApplicableFor();
		if (visitType.equals("o")) {
			if(( operationApplicability.equals("b") || operationApplicability.equals("o")) && includeOtDocCharges.equals("Y"))
				docCharges = ConsultationTypesDAO.getConsultationTypes("o", "ot",orgId,healthAuthority);
			else
				docCharges = ConsultationTypesDAO.getConsultationTypes("o",orgId,healthAuthority);
			otDocCharges = ChargeHeadsDAO.getOtDoctorChargeHeads();
		} else if (includeOtDocCharges.equals("Y")) {
			/* combined list of ip and ot types for billing */
			if( operationApplicability.equals("b") || operationApplicability.equals("i"))
				docCharges = ConsultationTypesDAO.getConsultationTypes("i", "ot",orgId,healthAuthority);
			else
				docCharges = ConsultationTypesDAO.getConsultationTypes("i",orgId,healthAuthority);
		} else if(visitType.equals("i")){
			/* only ip types from consultation types */
			docCharges = ConsultationTypesDAO.getConsultationTypes("i",orgId,healthAuthority);
			/* OT DOC types from charge heads */
			otDocCharges = ChargeHeadsDAO.getOtDoctorChargeHeads();
		}else{
			docCharges = ConsultationTypesDAO.getConsultationTypes("i", "o",orgId,healthAuthority);
		}
		Map<String,List<BasicDynaBean>> chargesMap = new HashMap<String, List<BasicDynaBean>>();
		chargesMap.put("doctor_charges", ConversionUtils.listBeanToListMap(docCharges));
		chargesMap.put("ot_doctor_charges", ConversionUtils.listBeanToListMap(otDocCharges));

        res.setContentType("application/json");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(new JSONSerializer().exclude("class").deepSerialize(chargesMap));
        res.flushBuffer();

		return null;
	}

	public ActionForward getOperationCharge(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException, IOException {

		String opId = request.getParameter("id");
		String bedType = request.getParameter("bedType");
		String orgId = request.getParameter("orgId");
		BasicDynaBean bean = new OperationMasterDAO().getOperationChargeBean(opId, bedType, orgId);

		response.setContentType("application/json");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.getWriter().write(new JSONSerializer().exclude("class").deepSerialize(bean.getMap()));
        response.flushBuffer();

		return null;

	}
	
	public ActionForward getOrderPackageDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, SQLException {
		String packageId = request.getParameter("pkg_id");

			Map<LISTING, Object> listingParams = ConversionUtils.getListingParameter(request.getParameterMap());
			PagedList pagedList = PackageDAO.getStaticPackgeComponentDetails(Integer.parseInt(packageId),listingParams);

			response.setContentType("text/plain");
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

			JSONSerializer js = new JSONSerializer().exclude("class");

			js.deepSerialize(pagedList, response.getWriter());
			return null;
	}
}

