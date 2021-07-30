package com.insta.hms.billing;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Logger;
import com.bob.hms.common.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.utils.JsonUtility;
import com.insta.hms.orders.OrderAction;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.orders.TestDocumentDTO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.insurance.DavitaSponsorDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.resourcescheduler.ResourceBO;

import flexjson.JSONSerializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.text.SimpleDateFormat;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.collections.map.HashedMap;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DialysisOrderAction extends BaseAction {

	static GenericDAO patReg = new GenericDAO("patient_registration");
	static GenericDAO patDet = new GenericDAO("patient_details");
	static GenericDAO patInsPlans = new GenericDAO("patient_insurance_plans");
	static GenericDAO patPolyDet = new GenericDAO("patient_policy_details");
	static GenericDAO billDao = new GenericDAO("bill");
	static GenericDAO billChargeDao = new GenericDAO("bill_charge");
	static GenericDAO billReceiptsDao = new GenericDAO("bill_receipts");
	static GenericDAO consoBillDao = new GenericDAO("consolidated_patient_bill");
	static DialysisOrderDao diaDao = new DialysisOrderDao();
	private AllocationService allocationService = (AllocationService) ApplicationContextProvider
            .getApplicationContext().getBean("allocationService");
	private static final GenericDAO serviceGroupsDAO = new GenericDAO("service_groups");

  @IgnoreConfidentialFilters
	public ActionForward showDialysisOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception{

		HttpSession session = request.getSession();
		JSONSerializer js = new JSONSerializer().exclude("class");
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();

		String mrNo = request.getParameter("mrNo");
		String mainVisitId = null;
		String visitId = null;

    BasicDynaBean dischargedDueToDeathDetails = diaDao.checkPatientDischargeStatus(mrNo);
    if (dischargedDueToDeathDetails != null) {
      SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy");
      String deathDate = DATE_FORMAT.format(dischargedDueToDeathDetails.get("death_date"));

      SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
      String deathTime = TIME_FORMAT.format(dischargedDueToDeathDetails.get("death_time"));

      request.setAttribute("deathDate", js.serialize(deathDate));
      request.setAttribute("deathTime", js.serialize(deathTime));
      request.setAttribute("dischargedAsDead", Boolean.TRUE.toString());
      
      request.setAttribute("doctorsList", js.serialize(""));
      request.setAttribute("anaeTypesJSON", js.serialize(""));
      request.setAttribute("serviceGroupsJSON", js.serialize(""));
      request.setAttribute("servicesSubGroupsJSON", js.serialize(""));
      request.setAttribute("ordrClaimAmtsMapJSON", js.serialize(""));
      request.setAttribute("doctorConsultationTypes", js.serialize(""));
      request.setAttribute("allDoctorConsultationTypes", js.serialize(""));
      request.setAttribute("consultationsAcrVisits", js.serialize(""));
      
      return mapping.findForward("showDialysisOrder");
    }
    request.setAttribute("dischargedAsDead", Boolean.FALSE.toString());
		int multiCenterCnt = (Integer)genericPrefs.get("max_centers_inc_default");
		boolean isMultiCenter = multiCenterCnt > 1;
		int userCenter = (Integer) session.getAttribute("centerId");
		List<String> doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList(userCenter);
		request.setAttribute("doctorsJSON", js.deepSerialize(ConversionUtils.listBeanToListMap(doctorsList)));
		request.setAttribute("doctorsList", js.serialize(ConversionUtils.listBeanToListMap(DoctorMasterDAO.getDoctorDepartmentsDynaList())));
		request.setAttribute("regPrefJSON",
				js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
		request.setAttribute("serviceGroups", serviceGroupsDAO.listAll(null,"status","A","service_group_name"));
		request.setAttribute("serviceGroupsJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
		    serviceGroupsDAO.listAll(null,"status","A",null))));
		request.setAttribute("servicesSubGroupsJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new GenericDAO("service_sub_groups").listAll(null,"status","A","service_sub_group_name"))));
		request.setAttribute("doctorConsultationTypes",js.serialize(ConversionUtils.copyListDynaBeansToMap(
				OrderDAO.getConsultationTypes("o", false))));
		request.setAttribute("allDoctorConsultationTypes",js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new ConsultationTypesDAO().listAll())));
		request.setAttribute("consultationsAcrVisits",
				js.serialize(ConversionUtils.copyListDynaBeansToMap(
						new OrderDAO().consultationsEncounterWise(mainVisitId))));
		request.setAttribute("anaeTypesJSON",js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new GenericDAO("anesthesia_type_master").listAll(null,"status","A",null))));
		request.setAttribute("ordrClaimAmtsMapJSON", js.serialize(new HashMap()));

		request.setAttribute("isMultiCenter", isMultiCenter);
		if(userCenter == 0 && isMultiCenter) {
			request.setAttribute("error", "Dialysis Order is not allowed for default center users");
			return mapping.findForward("showDialysisOrder");
		}

		if(mrNo == null) return mapping.findForward("showDialysisOrder");

		BasicDynaBean consoBillBean = null;
		VisitDetailsDAO visitDAO = new VisitDetailsDAO();
		BasicDynaBean latestvisitBean = null;

		BasicDynaBean mainVisitBean = visitDAO.getMainVisitOfCurrentMonth(mrNo);
		if(mainVisitBean != null) {
			if((String)mainVisitBean.get("main_visit_id") != null) {
				mainVisitId = (String)mainVisitBean.get("main_visit_id");
				latestvisitBean = visitDAO.getLatestActiveVisitForMainVisit(mrNo,mainVisitId);
				visitId = latestvisitBean == null ? null : (String) latestvisitBean.get("patient_id");
			}
		}
    BasicDynaBean lastVisitOrgIdBean = visitDAO.getLastVisitOrgId(mrNo);
    request.setAttribute("lastVisitOrgId",
        lastVisitOrgIdBean != null ? lastVisitOrgIdBean.get("org_id") : "");

		OrderDAO oDao = new OrderDAO();
		List allOrders = null;
		List allOrdersMultiVisitPack = null;
		if (visitId != null && !visitId.equals("")) {
			allOrders = oDao.getAllOrders(visitId);
		}

		//set the chargeid, amount to allOrders if they dont have charge and amount.
		//on delete of item we dont have any information to map order item to charge.
		//hence we are using item_id mapping with act_description_id to get charge and amount then we are setting to allOrders
		List<BasicDynaBean> billChargeList = diaDao.getBillChargeList(visitId);
		Map<String, BasicDynaBean> billChargeMap = ConversionUtils.listBeanToMapBean(billChargeList, "charge_id");

		if(allOrders != null) {
			for(Object obj : allOrders) {
				BasicDynaBean bean = (BasicDynaBean)obj;
				String chargeId = (String)bean.get("charge_id");
				String chgIdTobeRemoved = null;
				if(chargeId == null){
					String itemId = (String)bean.get("item_id");
					for(String key : billChargeMap.keySet()){
						BasicDynaBean chgBean = billChargeMap.get(key);
						String actDescId = (String)chgBean.get("act_description_id");
						String itemChgId = (String)chgBean.get("charge_id");
						if(actDescId.equals(itemId)){
							bean.set("charge_id", itemChgId);
							bean.set("amount", (BigDecimal)chgBean.get("amount"));
							chgIdTobeRemoved = itemChgId;
						}
					}
				} else {
					billChargeMap.remove(chargeId);
				}
				if(null != chgIdTobeRemoved)
					billChargeMap.remove(chgIdTobeRemoved);
			}
		}

		billChargeMap = ConversionUtils.listBeanToMapBean(billChargeList, "charge_id");

		BasicDynaBean preDialysisBean = null;
		if(allOrders != null) {
			for(Object obj : allOrders) {
				BasicDynaBean bean = (BasicDynaBean)obj;
				if(null != ((String)bean.get("isdialysis")) && ((String)bean.get("type")).equals("Service") &&
						((String)bean.get("isdialysis")).equals("D")) {
					preDialysisBean =bean;
					break;
				}
			}
		}
		request.setAttribute("preDialysisBean", preDialysisBean);
		// get the billdetails
		BasicDynaBean billBean = billDao.findByKey("visit_id", visitId);

		if(mainVisitId != null) {
			consoBillBean = consoBillDao.findByKey("main_visit_id", mainVisitId);
		}

		request.setAttribute("mrNo", (String)request.getParameter("mrNo"));
		request.setAttribute("msg", (String)request.getAttribute("msg"));
		request.setAttribute("allOrders", allOrders);
		request.setAttribute("allOrdersJSON", js.serialize(ConversionUtils.listBeanToListMap(allOrders)));
    HashedMap billChargeAmountMap = new HashedMap();
    for (Entry<String, BasicDynaBean> chargeEntry : billChargeMap.entrySet()) {
      billChargeAmountMap.put(chargeEntry.getKey(),
          String.valueOf(chargeEntry.getValue().get("insurance_claim_amount")));
    }
    request.setAttribute("ordrClaimAmtsMapJSON", js.serialize(billChargeAmountMap));
		request.setAttribute("billBean", billBean = billBean == null ? billDao.getBean() : billBean);
		request.setAttribute("visitDet", latestvisitBean);
		request.setAttribute("consoBillBean", consoBillBean = consoBillBean == null ? consoBillDao.getBean() : consoBillBean);

		request.setAttribute("ratePlanList", ConversionUtils.listBeanToListMap(diaDao.getRatePlanList(visitId == null ? mainVisitId : visitId)));

		List<BasicDynaBean> patientApprovals = diaDao.getAllPatientSponsorApprovals((String)request.getParameter("mrNo"),mainVisitId);
		//Map<Integer , BigDecimal> usedQtyMap = diaDao.getUsedQtyMapForApprovals(patientApprovals);
		request.setAttribute("approvals", patientApprovals);
		//request.setAttribute("usedQtyMap", usedQtyMap);

		List<BasicDynaBean> prevUnpaidBills = diaDao.getPreviousUnbalaceBills(mrNo);
		request.setAttribute("unpaidbills", prevUnpaidBills);
    BigDecimal grossPatientDue = diaDao.getGrossPatientDue(mrNo);
    request.setAttribute("grossPatientDue", grossPatientDue);
		request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());

		BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
		request.setAttribute("masterTimeStamp", mst.get("master_count"));

		request.setAttribute("mainVisitId", mainVisitId);
		request.setAttribute("mainVisitBean", mainVisitBean);
		request.setAttribute("prevBillsReceiptsTotal",
				(BigDecimal)((BasicDynaBean)diaDao.getPreviousBillsPaymentsSum(mrNo)).get("receipts_total"));

		String appointmentId = request.getParameter("appointment_id");
		String category = request.getParameter("category");
        String registrationType = request.getParameter("registrationType");

		if (appointmentId != null && !appointmentId.equals("")) {
        	BasicDynaBean apptBean = new GenericDAO("scheduler_appointments").findByKey("appointment_id", Integer.parseInt(appointmentId));
        	if(null != apptBean){
        		request.setAttribute("scheduleName", apptBean.get("prim_res_id"));
        		request.setAttribute("presDocId", apptBean.get("presc_doc_id"));
        	}
		}

		request.setAttribute("appointmentId", appointmentId);
		request.setAttribute("category", category);
		request.setAttribute("registrationType", registrationType);

		/*Map<String, Object> filterMap = new HashMap<String, Object>();
		//in multi center scheema theatre must belong to visit center.
		if ( GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1 )
			filterMap.put("center_id", session.getAttribute("centerId"));
		filterMap.put("status", "A");
		request.setAttribute("otlist_applicabletovisitcenter",
				new GenericDAO("theatre_master").listAll(null, filterMap,"theatre_name")); */

		return mapping.findForward("showDialysisOrder");
	}

  @IgnoreConfidentialFilters
	public ActionForward saveDialysisOrders(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws SQLException,Exception{
		//logic for create the visit
		HttpSession session = request.getSession();
        String userName = (String) session.getAttribute("username");
		VisitDetailsDAO visitDAO = new VisitDetailsDAO();
		OrderBO orderBo = new OrderBO();
		AbstractPaymentDetails bpImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
		List<Receipt> newReceiptList;
		List<Receipt> receiptListForPrint = new ArrayList<Receipt>();;
		Map requestParams = request.getParameterMap();
		BillBO billBOObj = new BillBO();
		BillDAO billDAO = null;
		BasicDynaBean consoBillBean = null;
		int userCenter = (Integer) session.getAttribute("centerId");

		String[] servGrpId = (String[])request.getParameterValues("serviceGroupId");
		String[] itemId = (String[])request.getParameterValues("item_id");
		String[] isNew = (String[])request.getParameterValues("new");

		String[] newlyAddedItemApprovalDetailIds = request.getParameterValues("newly_added_item_approval_detail_ids");
		String[] newlyAddedItemApprovalLimitValues = request.getParameterValues("newly_added_item_approval_limit_values");

		String mrNo = (String)request.getParameter("mr_no");
		String patientId = null;
		String mainVisitId = null;
		String currentVisitId= null;
		String orgId = (String)request.getParameter("organization_details");
		Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");

		BasicDynaBean mainVisit = visitDAO.getMainVisitOfCurrentMonth(mrNo);
		if(mainVisit != null) {
			if((String)mainVisit.get("main_visit_id") != null) {
				mainVisitId = (String)mainVisit.get("main_visit_id");
				BasicDynaBean latestvisit = visitDAO.getLatestActiveVisitForMainVisit(mrNo,mainVisitId);
				patientId = latestvisit == null ? null : (String) latestvisit.get("patient_id");

				String mainVisitOrgId = (String)mainVisit.get("org_id");
				if(orgId == null && mainVisitOrgId != null){
					orgId = mainVisitOrgId;
				}
			}
		}
		currentVisitId=mainVisitId;
		Connection con = null;
		String error = null;
		boolean success = true;
		try {
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean patRegBean = patReg.getBean();
			if(patientId == null || patientId.equals("")) {


				patRegBean.set("mr_no", mrNo);
				patRegBean.set("patient_id", VisitDetailsDAO.getNextVisitId("o", 0));
				patRegBean.set("reg_date", DateUtil.parseDate((String)request.getParameter("visitDate")));
				patRegBean.set("reg_time", DateUtil.parseTime((String)request.getParameter("visitTime")));
				patRegBean.set("status", "A");
				patRegBean.set("visit_type", "o");
				patRegBean.set("bed_type", "GENERAL");
				patRegBean.set("org_id", orgId);
				patRegBean.set("main_visit_id",
						mainVisitId == null ? (String)patRegBean.get("patient_id") : mainVisitId);
				patRegBean.set("op_type",
						mainVisitId == null ? "M" : "F");
				patRegBean.set("center_id", RequestContext.getCenterId());
				patRegBean.set("user_name", (String) session.getAttribute("userid"));
				
				success = patReg.insert(con, patRegBean);
				currentVisitId = (String)patRegBean.get("patient_id");
			}

			boolean consolidatedBillExists = consoBillDao.exist("main_visit_id", mainVisitId, false);
//			 create the consolidated bill entry
			if(mainVisitId == null || !consolidatedBillExists) {
				consoBillBean = consoBillDao.getBean();

				String consoBillNo = DataBaseUtil.getNextPatternId("CONSOLIDATED_BILL");

				consoBillBean.set("consolidated_bill_no", consoBillNo);

				if(!consolidatedBillExists && mainVisitId != null)
					consoBillBean.set("main_visit_id", mainVisitId);
				else
					consoBillBean.set("main_visit_id", (String)patRegBean.get("patient_id"));

				consoBillBean.set("open_date", DateUtil.getCurrentTimestamp());
			}

			//logic for creating patient_insurance_plans entry.
			/*order item -> approval details -> sponsor
			 *Fetch the patient approvals which are expires soon and get the service group id
			 *get the order item service group id if matches then get respective sponsor id from
			 *patient approvals.
				 * */

			orderBo.setNewlyAddedItemApprovalDetailIds(newlyAddedItemApprovalDetailIds);
			orderBo.setNewlyAddedItemApprovalLimitValues(newlyAddedItemApprovalLimitValues);

			BasicDynaBean visitBean = visitDAO.getLatestActiveVisit(con , mrNo);
			patientId = (String)visitBean.get("patient_id");
			int idx = 0;

// In recalculation logic we are inserting required records in patient_insurance_plans.

//			for(int i=0 ; i < isNew.length ; i++) {
				
				/*
				if(!isNew[i].equals("Y"))
					continue;

				String[] approvalDetailIds = getRequiredApprovalIds(idx,newlyAddedItemApprovalDetailIds);
				String[] approvalDetailsLimitValues = getRequiredApprovalIds(idx,newlyAddedItemApprovalLimitValues);
				idx++;

				BasicDynaBean approvalBean = diaDao.getSponsorApprovalDetails(con,mrNo,servGrpId[i],
						itemId[i], (String)visitBean.get("main_visit_id"), approvalDetailIds, approvalDetailsLimitValues);
				if(approvalBean == null ) continue;
				// if visit_id,sponsor_id record is already there in patInsPlans then no need to insert
				// record in patInsPlans
				Map insPlankeyMap = new HashMap();
				insPlankeyMap.put("patient_id", patientId);
				insPlankeyMap.put("sponsor_id", (String)approvalBean.get("sponsor_id"));
				BasicDynaBean planExist = patInsPlans.findByKey(con, insPlankeyMap);
				if(planExist != null) continue;

				BasicDynaBean insPlanBean = patInsPlans.getBean();
				insPlanBean.set("patient_insurance_plans_id", patInsPlans.getNextSequence());
				insPlanBean.set("mr_no", mrNo);
				insPlanBean.set("patient_id", patientId);
				insPlanBean.set("insurance_co",
						new InsuCompMasterDAO().
							getInsuranceCompaniesNamesAndIdsMap().
							get("Default Insurance Company"));
				insPlanBean.set("sponsor_id", (String)approvalBean.get("sponsor_id"));
				insPlanBean.set("patient_policy_id", DataBaseUtil.getNextSequence("patient_policy_details_patient_policy_id_seq"));
				
				//insPlanBean.set("plan_id",(new PlanMasterDAO().findPlan("plan_name",
						//"Default Insurance Company Plan")).get("plan_id"));
				BasicDynaBean planBean = diaDao.getPlanId((String)approvalBean.get("sponsor_id"));
				if(planBean != null) {
					insPlanBean.set("plan_id", (Integer)planBean.get("plan_id"));
					insPlanBean.set("plan_type_id", (Integer)planBean.get("category_id"));
				} else {
					insPlanBean.set("plan_id",(new PlanMasterDAO().findPlan("plan_name",
					"Default Insurance Company Plan")).get("plan_id"));
					insPlanBean.set("plan_type_id", (new PlanMasterDAO().findPlan("plan_name",
					"Default Insurance Company Plan")).get("category_id"));
				}

				List<BasicDynaBean> PlansList = patInsPlans.findAllByKey(con , "patient_id", patientId);
				insPlanBean.set("priority", PlansList.size()+1);

				success = patInsPlans.insert(con, insPlanBean);
				
				// insert patient_policy_details entries.
				BasicDynaBean patPolyBean = patPolyDet.getBean();
				patPolyBean.set("mr_no", mrNo);
				patPolyBean.set("plan_id", (Integer)insPlanBean.get("plan_id"));
				patPolyBean.set("patient_policy_id", insPlanBean.get("patient_policy_id"));
				patPolyBean.set("status", "A");
				patPolyBean.set("visit_id", patientId);
				
				success = patPolyDet.insert(con, patPolyBean);
			*/
				
//			}
			
			/*
			//update the primary and secondary insurance plans from patinent
			//patient insurance plans to patient registration
			BasicDynaBean prBean = patReg.findByKey(con, "patient_id", patientId);
			List<BasicDynaBean> PlansList = patInsPlans.findAllByKey(con , "patient_id", patientId);
			for(BasicDynaBean planBean : PlansList) {
				int priority = (Integer)planBean.get("priority");
				if(priority == 1) {
					prBean.set("primary_sponsor_id", (String)planBean.get("sponsor_id"));
					prBean.set("primary_insurance_co", (String)planBean.get("insurance_co"));
				} else if(priority == 2) {
					prBean.set("secondary_sponsor_id", (String)planBean.get("sponsor_id"));
					prBean.set("secondary_insurance_co", (String)planBean.get("insurance_co"));
				}
			}

			patReg.updateWithName(con, prBean.getMap(), "patient_id");
			 */
			
			
			//logic for creating the new bill
			Map keyMap = new HashMap();
			keyMap.put("visit_id", patientId);
			keyMap.put("status", "A");
			BasicDynaBean billBean = billDao.findByKey(con, keyMap);

			if(billBean == null) {
				error = orderBo.setBillInfo(con, (String)visitBean.get("patient_id"),
						null, false, (String) request.getSession(true).getAttribute("userid"), "C");
			} else {
				error = orderBo.setBillInfo(con, (String)visitBean.get("patient_id"),
						(String)billBean.get("bill_no"), (Boolean)billBean.get("is_tpa"),
						(String) request.getSession(true).getAttribute("userid"), "C");
			}

			if (error != null) success = false;

			String billNo = (orderBo.getBill() != null) ? (String)((BasicDynaBean)orderBo.getBill()).get("bill_no") : null;
			if(mainVisitId == null || !consolidatedBillExists) consoBillBean.set("bill_no", billNo);
			else {
				keyMap.clear();
				keyMap.put("main_visit_id", mainVisitId);
				keyMap.put("bill_no", billNo);
				BasicDynaBean billExist = consoBillDao.findByKey(con, keyMap);
				if(billExist == null) {
					keyMap.remove("bill_no");
					BasicDynaBean consoBillDet = consoBillDao.findByKey(con, keyMap);
					if(null != consoBillDet) {
						consoBillBean = consoBillDao.getBean();
						consoBillBean.set("consolidated_bill_no", consoBillDet.get("consolidated_bill_no"));
						consoBillBean.set("bill_no", billNo);
						consoBillBean.set("main_visit_id", consoBillDet.get("main_visit_id"));
						consoBillBean.set("open_date", consoBillDet.get("open_date"));
					}
				}
			}
			if(consoBillBean != null){
				success = consoBillDao.insert(con, consoBillBean);
			}

			List errorList = new ArrayList();
			List newOrders = new ArrayList();
			List newPreAuths = new ArrayList();
			List<Integer> preAuthModeList = new ArrayList<Integer>();
			List firstOfCategoryList = new ArrayList();
			List<String> condDoctrsList = new ArrayList<String>();
			List<Boolean> multiVisitPackageList = new ArrayList<Boolean>();
			List secNewPreAuths = new ArrayList();
			List<Integer> secPreAuthModeList = new ArrayList<Integer>();
			List<Map<String,Object>> operationAnaesTypesList = new ArrayList<Map<String,Object>>();
			List<List<TestDocumentDTO>> testAdditionalDocList = new ArrayList<List<TestDocumentDTO>>();

			OrderAction.getNewOrderBeans(request.getParameterMap(),newOrders,newPreAuths,preAuthModeList,
					firstOfCategoryList,condDoctrsList,	multiVisitPackageList, errorList, secNewPreAuths,
					secPreAuthModeList, operationAnaesTypesList, testAdditionalDocList, form);

			if(newOrders.size() > 0)
				error = orderBo.orderItems(con, newOrders,newPreAuths,preAuthModeList,firstOfCategoryList,
						condDoctrsList,	multiVisitPackageList, 0, secNewPreAuths, secPreAuthModeList,
						operationAnaesTypesList, testAdditionalDocList);
			if (error != null) success = false;

			List<BasicDynaBean> cancelItemOrders = new ArrayList<BasicDynaBean>();
			List<BasicDynaBean> cancelItemChargeOrders = new ArrayList<BasicDynaBean>();
			List<BasicDynaBean> editOrders = new ArrayList<BasicDynaBean>();
			List<Map<String,Object>> opEditAnaesTypesList = new ArrayList<Map<String,Object>>();
			List<String> editOrCancelOrderBills = new ArrayList<String>();
			List<TestDocumentDTO> editTestAdditionalDocList = new ArrayList<TestDocumentDTO>();

			new OrderAction().getModifiedOrderBeans(orderBo, request.getParameterMap(),
					(String) request.getSession(true).getAttribute("userid"),
					cancelItemOrders, cancelItemChargeOrders, editOrders, opEditAnaesTypesList, editTestAdditionalDocList, form);

			error = orderBo.updateOrders(con, cancelItemOrders, true, false, true, editOrCancelOrderBills, opEditAnaesTypesList,
					editTestAdditionalDocList);
			if (error != null) success = false;

			error = orderBo.updateOrders(con, cancelItemChargeOrders, true, true, false, editOrCancelOrderBills,
					opEditAnaesTypesList, null);
			if (error != null) success = false;

			error = orderBo.updateOrders(con, editOrders, false, false, false, editOrCancelOrderBills, opEditAnaesTypesList, null);
			if (error != null) success = false;

			//manage bill_charge_claim entries and
			// sponsor calculation
			BasicDynaBean visitDetBean = visitDAO.getPatientVisitDetailsBean(con, patientId);
			new DavitaSponsorDAO().recalculatePreviousVisitItems(con , mainVisitId == null ? patientId : mainVisitId);
			new DavitaSponsorDAO().insertBillChargeClaimEntries(con , visitDetBean);
			new DavitaSponsorDAO().calculate(con , visitDetBean);

			/*	List<BasicDynaBean> billExist=null;
			List<BasicDynaBean> PlansList = patInsPlans.findAllByKey(con , "patient_id", currentVisitId);
			if(PlansList.size() == 0){
				billExist = billDao.findAllByKey(con, "visit_id", currentVisitId);
				for(BasicDynaBean b:billExist){
					Bill bii=new BillDAO(con).getBill((String)b.get("bill_no"));
					bii.setIs_tpa(false);
					new BillDAO(con).updateBill(bii);
				}
			}else{
				billExist = billDao.findAllByKey(con, "visit_id", currentVisitId);
				for(BasicDynaBean b:billExist){
					Bill bii=new BillDAO(con).getBill((String)b.get("bill_no"));
					bii.setIs_tpa(true);
					new BillDAO(con).updateBill(bii);
				}
			}*/
		}
		catch(Exception e) {
			Logger.logException("Exception in DialysisOrderAction class", e);
			success = false;
			throw e;
		}finally{
			DataBaseUtil.commitClose(con, success);
		}

    String billNo =
        (orderBo.getBill() != null) ? (String) (orderBo.getBill()).get("bill_no") : null;
    Set<String> allocationRequiringBillNos = new HashSet<>();
    allocationRequiringBillNos.add(billNo);
		try{
			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

      ReceiptRelatedDAO receiptRelatedDAO = new ReceiptRelatedDAO(con);
//			Logic for bill payment
			billDAO = new BillDAO(con);
      Bill bill = billDAO.getBill(billNo);
			newReceiptList = bpImpl.processReceiptParams(requestParams);
			BigDecimal totalUnallocatedAmount = BigDecimal.ZERO;

			List<Receipt> receiptList = new ArrayList<>();
      List<BasicDynaBean> receiptBeansWithUnAllocatedAmount = diaDao
          .getReceiptWithUnAllocatedAmount(mrNo);
      for (BasicDynaBean receiptBeanWithUnAllocatedAmount : receiptBeansWithUnAllocatedAmount) {
        Receipt receiptWithUnAllocatedAmount = new Receipt();
        receiptWithUnAllocatedAmount.setReceiptNo(
            (String) receiptBeanWithUnAllocatedAmount.get("receipt_id"));
        // Using the amount field here to store the unallocated amount as there no unAllocatedAmount field present in the Object
        receiptWithUnAllocatedAmount.setAmount(
            (BigDecimal) receiptBeanWithUnAllocatedAmount.get("unallocated_amount"));
        receiptList.add(receiptWithUnAllocatedAmount);
        totalUnallocatedAmount = totalUnallocatedAmount.add(
            (BigDecimal) receiptBeanWithUnAllocatedAmount.get("unallocated_amount"));
      }
      
			if(newReceiptList != null) {
        for (Receipt receipt : newReceiptList) {
          receiptList.add(receipt);
          totalUnallocatedAmount = totalUnallocatedAmount.add(receipt.getAmount());
          receipt.setBillNo(billNo);
          receipt.setMrno(bill.getMrno());

          switch (receipt.getPaymentType()) {
            case "R":
              receipt.setReceiptPrintFormat(Receipt.RECEIPT_PRINT);
              if (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
                receipt.setReceiptType("R");
              }
              break;
            case "F":
              receipt.setReceiptPrintFormat(Receipt.REFUND_PRINT);
              break;
            case "S":
              receipt.setIsSettlement(true);
              receipt.setReceiptPrintFormat(Receipt.SPONSOR_RECEIPT_PRINT);
              break;
          }
          String receiptNo = ReceiptRelatedDAO
              .getNextReceiptNo(bill.getBillType(), bill.getVisitType(), "N",
                  receipt.getPaymentType(), centerId != null ? centerId : 0);
          receipt.setReceiptNo(receiptNo);
          //if bill's Visit Type is "t", Then set visit_id as receipt's Incoming Visit Id
          if (bill.getVisitType().equals("t")) {
            receipt.setIncomingVisitId(bill.getVisitId());
          }
          //if bill's Visit Type is "r", Then set visit_id as receipt's Store retail custormer Id
          if (bill.getVisitType().equals("r")) {
            receipt.setStoreRetailCustomerId(bill.getVisitId());
          }
          receiptRelatedDAO.createReceiptEntry(receipt);
          // Have to commit so that the receipt is availabe to hibernate.
          con.commit();
          if ("F".equals(receipt.getReceiptType())) {
            // HMS-30409, HMS-30871
            // Returns false if unable to create complete reference for the refund amount.
            //allocationService.createReceiptRefundReference(receipt);
          }
          Receipt receiptToPrint = setReceiptDetails(receipt);
          receiptListForPrint.add(receiptToPrint);
        }
      }
			
      List<BasicDynaBean> billsWithPatientDues =
          diaDao.getAllBillsWithPatientDues(mrNo);
      List<MinimalBillVO> existingBills =
          beanToReceipt(billsWithPatientDues);

      for (MinimalBillVO existingBill : existingBills) {
        BigDecimal patientDue = existingBill.getPatientdue();
        if (patientDue.compareTo(BigDecimal.ZERO) != 0 
            && receiptList != null
            && receiptList.size() > 0) {
          for (Receipt newReceipt : receiptList) {
            allocationRequiringBillNos.add(existingBill.getBillno());
            if (totalUnallocatedAmount.compareTo(BigDecimal.ZERO) > 0
                && patientDue.compareTo(BigDecimal.ZERO) > 0) {
              if (!diaDao.receiptUsageExist(newReceipt.getReceiptNo(),
                  BillConstants.Restrictions.BILL_NO, existingBill.getBillno())) {
                Receipt billReceipt = new Receipt();
                billReceipt.setReceiptNo(newReceipt.getReceiptNo());
                billReceipt.setBillNo(existingBill.getBillno());
                billReceipt.setReceiptDate(DateUtil.getCurrentTimestamp());
                billReceipt.setUsername(RequestContext.getUserName());
                success = receiptRelatedDAO.createBillReceipt(billReceipt);
                con.commit();
              }
              if (newReceipt.getAmount().compareTo(patientDue) >= 0) {
                totalUnallocatedAmount = totalUnallocatedAmount.subtract(patientDue);
                newReceipt.setAmount(newReceipt.getAmount().subtract(patientDue));
                patientDue = BigDecimal.ZERO;
              } else {
                totalUnallocatedAmount = totalUnallocatedAmount.subtract(newReceipt.getAmount());
                patientDue = patientDue.subtract(newReceipt.getAmount());
                newReceipt.setAmount(BigDecimal.ZERO);
              }
            }
          }
        }
      }
      allocationService.updateBillTotal(billNo);

			int appointmentId = 0;
			if (request.getParameter("appointmentId") != null && !request.getParameter("appointmentId").equals("")) {
				appointmentId =  new Integer(request.getParameter("appointmentId"));
			}
			BasicDynaBean patientDetailsBean = new PatientDetailsDAO().getPatientGeneralDetailsBean(con, mrNo);
			int consultationTypeId = 0;
			String userId = (String)session.getAttribute("userid");
			String presDocId = request.getParameter("presDocId");
			 if (appointmentId > 0) {
				 ResourceBO.updateScheduler(con, appointmentId, mrNo, patientId, patientDetailsBean, null,userId,
						 consultationTypeId, null,presDocId,"Reg");
		    }
		}
		///*
		catch(Exception e) {
		  Logger.logException("Exception occurred in DialysisOrderAction", e);
			success = false;
			throw e;
		}
		//*/
		finally {
			DataBaseUtil.commitClose(con, success);
		}

    for (String billNumber : allocationRequiringBillNos) {
      allocationService.allocate(billNumber, centerId);
    }
		GenericPreferencesDTO genpref = GenericPreferencesDAO.getGenericPreferences();

		if (receiptListForPrint != null && receiptListForPrint.size() > 0) {
			String printerTypeStr = genpref.getDefault_printer_for_bill_later();
			String customTemplate = genpref.getBillLaterPrintDefault();

				//request.getParameter("printBill"); // this you will get null if user is not having bill print rights.

			if (customTemplate != null && !customTemplate.equals("")) {
				Map printParamMap = new HashMap();
				printParamMap.put("printerTypeStr", printerTypeStr);
				printParamMap.put("customTemplate", customTemplate);
        printParamMap.put("patient_id", patientId);
        printParamMap.put("visit_type", "o");

				List<String> printURLs = bpImpl.generatePrintReceiptUrls(receiptListForPrint, printParamMap);
				request.getSession(false).setAttribute("printURLs", printURLs);
			}
		}

		//logic for create the tests_prescribed entry
		//logic for create the bill_charge entry

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
		redirect.addParameter("mrNo", mrNo);
		redirect.addParameter("patient_id", patientId);
    redirect.addParameter("visit_type", "o");
		return redirect;
	}

	private Receipt setReceiptDetails(Receipt receipt) {
		Receipt rct = new Receipt();
		rct.setAmount(receipt.getAmount());
		rct.setBankBatchNo(receipt.getBankBatchNo());
		rct.setBankName(receipt.getBankName());
		rct.setBillNo(receipt.getBillNo());
		rct.setBillStatus(receipt.getBillStatus());
		rct.setBillType(receipt.getBillType());
		rct.setCardAuthCode(receipt.getCardAuthCode());
		rct.setCardExpDate(receipt.getCardExpDate());
		rct.setCardHolderName(receipt.getCardHolderName());
		rct.setCardNumber(receipt.getCardNumber());
		rct.setCardType(receipt.getCardType());
		rct.setCardTypeId(receipt.getCardTypeId());
		rct.setCommissionAmount(receipt.getCommissionAmount());
		rct.setCommissionPercentage(receipt.getCommissionPercentage());
		rct.setCounter(receipt.getCounter());
		rct.setCounterType(receipt.getCounterType());
		rct.setCurrency(receipt.getCurrency());
		rct.setCurrencyAmt(receipt.getCurrencyAmt());
		rct.setCurrencyId(receipt.getCurrencyId());
		rct.setCustomerName(receipt.getCustomerName());

		rct.setExchangeDateTime(receipt.getExchangeDateTime());
		rct.setExchangeRate(receipt.getExchangeRate());
		rct.setMrno(receipt.getMrno());
		rct.setPackageId(receipt.getPackageId());
		rct.setPaidBy(receipt.getPaidBy());
		rct.setPatientAge(receipt.getPatientAge());
		rct.setPatientAgeIn(receipt.getPatientAgeIn());
		rct.setPatientGender(receipt.getPatientGender());
		rct.setPatientLastName(receipt.getPatientLastName());
		rct.setPatientName(receipt.getPatientName());
		rct.setPatientTitle(receipt.getPatientTitle());
		rct.setPaymentMode(receipt.getPaymentMode());
		rct.setPaymentModeId(receipt.getPaymentModeId());
		rct.setPaymentType(receipt.getPaymentType());
		rct.setReceiptDate(receipt.getReceiptDate());
		rct.setReceiptNo(receipt.getReceiptNo());
		rct.setReceiptPrintFormat(receipt.getReceiptPrintFormat());
		rct.setReceiptType(receipt.getReceiptType());
		rct.setReferenceNo(receipt.getReferenceNo());
		rct.setRemarks(receipt.getRemarks());
		rct.setSponsorId(receipt.getSponsorId());
		rct.setSponsorIndex(receipt.getSponsorIndex());
		rct.setTdsAmt(receipt.getTdsAmt());
		rct.setUsername(receipt.getUsername());
		rct.setVisitId(receipt.getVisitId());
		rct.setVisitType(receipt.getVisitType());
		return rct;
	}

	private String[] getRequiredApprovalIds(int i, String[] newlyAddedItemApprovalDetailIds) {
		if(i == 0) return null;

		String[] appDetailIds =  new String[i];
		for(int k=0; k<i; k++){
			appDetailIds[k] = newlyAddedItemApprovalDetailIds[k];
		}
		return appDetailIds;
	}

  /**
   * @param beans List of BasicDynaBean with MinimalBillVO details
   * @return List of MinimalBillVO
   * @throws IOException Exception from ObjectMapper.readValue()
   */
  private List<MinimalBillVO> beanToReceipt(List<BasicDynaBean> beans) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    List<MinimalBillVO> receipts = new ArrayList<>();
    for (BasicDynaBean bean : beans) {
      MinimalBillVO receipt = objectMapper
          .readValue(JsonUtility.toJson(bean.getMap()), MinimalBillVO.class);
      receipts.add(receipt);
    }
    return receipts;
  }

  /**
   * A class with minimal bill details required for saveDialysisOrders()
   */
  static class MinimalBillVO {

    String billno;
    BigDecimal amount;
    BigDecimal patientdue;

    public String getBillno() {
      return billno;
    }

    public void setBillno(String billno) {
      this.billno = billno;
    }

    public BigDecimal getAmount() {
      return amount;
    }

    public void setAmount(BigDecimal amount) {
      this.amount = amount;
    }

    public BigDecimal getPatientdue() {
      return patientdue;
    }

    public void setPatientdue(BigDecimal patientdue) {
      this.patientdue = patientdue;
    }

  }
}
