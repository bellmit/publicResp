package com.insta.hms.billing;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.PatientPolicyDAO;
import com.insta.hms.Registration.RegistrationBO;
import com.insta.hms.Registration.VisitCaseRateDetailDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.EditInsuranceHelper.BillsRequired;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.editvisitdetails.EditVisitDetailsDAO;
import com.insta.hms.insurance.InsuranceDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.InsuranceCategoryMaster.InsuranceCategoryMasterDAO;
import com.insta.hms.master.InsuranceCompMaster.InsuCompMasterDAO;
import com.insta.hms.master.InsuranceCompanyTPAMaster.InsuranceCompanyTPAMasterDAO;
import com.insta.hms.master.PatientCategory.PatientCategoryDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsRepository;
import com.insta.hms.mdm.insuranceplans.InsurancePlanRepository;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanValidator;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenBO;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author lakshmi
 *
 */
public class EditInsuranceAction extends BaseAction {

	static Logger log = LoggerFactory.getLogger(EditInsuranceAction.class);
	
	static EditInsuranceHelper editInsHelper = new EditInsuranceHelper();
	static RegistrationBO regBO = new RegistrationBO();
	static ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();
	
	static SponsorBO sponsorBO = new SponsorBO();
	static SponsorDAO sponsorDAO = new SponsorDAO();
	static DrgUpdateDAO drgUpdateDAO = new DrgUpdateDAO();
	static DRGCalculator drgCalculator = new DRGCalculator();
	static VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();
	static VisitCaseRateDetailDAO visitCaseRateDetDAO = new VisitCaseRateDetailDAO();
	static BillBO billBo = new BillBO();
	static InsurancePlanRepository insurancePlanRepository =
					new InsurancePlanRepository();
	static InsurancePlanDetailsRepository insurancePlanDetailsRepository =
					new InsurancePlanDetailsRepository();
	static InsurancePlanValidator insurancePlanValidator = new InsurancePlanValidator();
	static InsurancePlanService insurancePlanService = new InsurancePlanService(
					insurancePlanRepository, insurancePlanValidator, insurancePlanDetailsRepository
	);
	
	static GenericDAO billDAO = new GenericDAO("bill");
	static GenericDAO chargeDAO = new GenericDAO("bill_charge");
	
	static GenericDAO patientPolicyDetails = new GenericDAO("patient_policy_details");
	static PatientInsurancePlanDAO patientInsPlanDAO = new PatientInsurancePlanDAO();
	static GenericDAO patientInsurancePlanDetails = new GenericDAO("patient_insurance_plan_details");

	static InsuranceCompanyTPAMasterDAO insCompTPAMasterDAO = new InsuranceCompanyTPAMasterDAO();
	static PlanMasterDAO planMasterDAO = new PlanMasterDAO();
	private static final GenericDAO sponsorTypeDAO = new GenericDAO("sponsor_type");
	private static final GenericDAO tpaMasterDAO = new GenericDAO("tpa_master");

	static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
	
	private static InterfaceEventMappingService interfaceEventMappingService =
	      ApplicationContextProvider.getBean(InterfaceEventMappingService.class);
	
	public ActionForward changeTpa(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException, ParseException {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String visitId = request.getParameter("visitId");
		BasicDynaBean visitbean = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
		if (visitbean == null) {
			return null;
		}
		String mrno = visitbean != null ? (String) visitbean.get("mr_no") : null;
		//If credit notes exist throw confirmation that all credit notes for current visit will be cancelled
		List<BasicDynaBean> creditNoteList = VisitDetailsDAO.getCreditNoteList(visitId);
		ArrayList<String> billNoList = new ArrayList<String>();
		for(BasicDynaBean billBean : creditNoteList){
			billNoList.add((String) billBean.get("bill_no"));
		}
		request.setAttribute("creditNoteList", billNoList);
		request.setAttribute("isNewUX", request.getParameter("isNewUX"));
		
		String priSponsorType = visitbean.get("sponsor_type") != null ? (String)visitbean.get("sponsor_type") : null;
		String secSponsorType = visitbean.get("sec_sponsor_type") != null ? (String)visitbean.get("sec_sponsor_type") : null;

		boolean isPrimaryInsuranceCardAvailable = false;
		if (priSponsorType != null)
			isPrimaryInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientCardImage(visitId, priSponsorType) != null;

		boolean isSecondaryInsuranceCardAvailable = false;
		if (secSponsorType != null)
			isSecondaryInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientSecCardImage(visitId, secSponsorType) != null;

		request.setAttribute("isPrimaryInsuranceCardAvailable", isPrimaryInsuranceCardAvailable);
		request.setAttribute("isSecondaryInsuranceCardAvailable", isSecondaryInsuranceCardAvailable);

		// get existing member IDs for all sponsor types for the patient.
		List<BasicDynaBean> existingPoliciesList = new ArrayList<BasicDynaBean>();
		existingPoliciesList = patientPolicyDetails.findAllByKey("mr_no", mrno);

		request.setAttribute("existingPoliciesList",  
				ConversionUtils.listBeanToListMap(existingPoliciesList));

		Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
		request.setAttribute("billNo", request.getParameter("billNo"));
		request.setAttribute("patient", patientDetails);

		request.setAttribute("tpaList", ConversionUtils
				.copyListDynaBeansToMap(new TpaMasterDAO().listAll(null, "status", "A") ));

		String visitType = (String) visitbean.get("visit_type");
		int patientCategoryId = patientDetails.get("patient_category") == null ? 0 :
				(Integer) patientDetails.get("patient_category");

		List<DynaBeanMapDecorator> categoryLists = ConversionUtils.listBeanToListMap(
						InsuranceCategoryMasterDAO.getEditInsCatCenter(visitId, null));
		request.setAttribute("categoryLists", categoryLists);
		LinkedHashMap<Integer, List<String>> networkTypeSponsorIdListMap = new LinkedHashMap<>();
		for (DynaBeanMapDecorator category: categoryLists) {
			Integer categoryId = (Integer) category.get("category_id");
			List<String> sponsorIdList = new ArrayList<>();
			Map<String, Object> insurancePlansFilterMap = new HashMap<>();
			insurancePlansFilterMap.put("category_id", categoryId);
			List<BasicDynaBean> planBeans = insurancePlanService.listAll(insurancePlansFilterMap);
			for (BasicDynaBean planBean: planBeans) {
				sponsorIdList.add((String) planBean.get("sponsor_id"));
			}
			networkTypeSponsorIdListMap.put(categoryId, sponsorIdList);
		}
		request.setAttribute("networkTypeSponsorIdListMap",
						new JSONSerializer().deepSerialize(networkTypeSponsorIdListMap));
		request.setAttribute("categoryJSON", ConversionUtils
				.listBeanToListMap(PatientCategoryDAO.getAllCategoriesIncSuperCenter(RequestContext.getCenterId())));
		request.setAttribute("tpaNamesForSelectedMrno", ConversionUtils
				.listBeanToListMap(TpaMasterDAO.getTpaNamesForMrno(mrno)));

		request.setAttribute("ratePlanList", ConversionUtils.listBeanToListMap(OrgMasterDao
				.getOrganizations()));
		request.setAttribute("policyNos", ConversionUtils.listBeanToListMap(RegistrationBO
				.getPatientPlansDetailsForEditIns((String) visitbean.get("mr_no"), visitId)));
		request.setAttribute("allPolicyNos", ConversionUtils.listBeanToListMap(RegistrationBO
				.getAllPatientPlansDetailsUsingMRNO((String) visitbean.get("mr_no"))));
		request.setAttribute("policyNosMap", ConversionUtils.listBeanToListMap(RegistrationBO
				.getPatientPlansDetailsForEditIns((String) visitbean.get("mr_no"), visitId)));
		request.setAttribute("corporateIds", ConversionUtils.listBeanToListMap(RegistrationBO
				.getPatientCorporateDetails((String) visitbean.get("mr_no"))));
		request.setAttribute("nationalIds", ConversionUtils.listBeanToListMap(RegistrationBO
				.getPatientNationalDetails((String) visitbean.get("mr_no"))));
		request.setAttribute("docs", VisitDetailsDAO.getDocsUpload());

		request.setAttribute("allTpaBills", ConversionUtils.listBeanToListMap(
				editInsHelper.getMainAndFollowUpVisitTPABills(visitId, "all_bills")));

		request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
		request.setAttribute("regPrefJSON", js.serialize(RegistrationPreferencesDAO
				.getRegistrationPreferences()));
		request.setAttribute("itemCatlist", ConversionUtils.listBeanToListMap(
				new GenericDAO("item_insurance_categories").listAll()));
        request.setAttribute("sponsorTypelist",
            ConversionUtils.listBeanToListMap(sponsorTypeDAO.listAll()));
		request.setAttribute("tpanames", ConversionUtils
				.listBeanToListMap(TpaMasterDAO.gettpanames()));

		BigDecimal billsApprovalTotal = null;
		if (visitId != null) {
			billsApprovalTotal = BillDAO.getBillApprovalAmountsTotal(visitId);
			request.setAttribute("billsApprovalTotal", billsApprovalTotal);
			request.setAttribute("allowSecSponsor", allowAddOfSecondarySponsor(visitbean) == true? "Y":"N");
		}
		request.setAttribute("oldEditIns", true);
		return mapping.findForward("changeTpa");
	}

	public boolean allowAddOfSecondarySponsor(BasicDynaBean visitDetailsBean)
			throws SQLException {

		String visitId = (String) visitDetailsBean.get("patient_id");
		List<BasicDynaBean> allVisits = editInsHelper.getMainAndFollowUpVisits(visitId);

		boolean allow = false;

		for (BasicDynaBean visit : allVisits) {
			List<BasicDynaBean> visitTpaBills = BillDAO.getVisitBills((String)visit.get("patient_id"), BillDAO.bill_type.BOTH, true, true);

			// Secondary sponsor can be added if visit does not have more than one insured bill.
			if (visitTpaBills == null || visitTpaBills.size() <= 1)
				allow = true;
			else
				allow = false;

			if (!allow)
				return allow;
		}
		return allow;
	}

	/** Update rate plan and claim amounts for corresponding charges. */

	@SuppressWarnings("unchecked")
	public String updatePatientChargeDetails(Connection con,
			BasicDynaBean visitDetailsBean, String userName, int[] planIds,
			String priTpaId, String priInsuranceCompId, String secTpaId, String secInsuranceCompId,
			Integer categoryId, String useDRG, Date policyValidityStart, Date policyValidityEnd,
			String policyHolder, String policyNumber, String policyNo, String patRelation,
			String priorAuthId, Integer priorAuthModeId,
			BigDecimal primaryInsuranceApproval, BigDecimal secondaryInsuranceApproval,
			List<BasicDynaBean> tpaBills, List<BasicDynaBean> allVisits, String billsRequired,
			String usePerdiem ) throws SQLException, IOException, Exception {

		String orgId = (String)visitDetailsBean.get("org_id");
		String patientId = (String)visitDetailsBean.get("patient_id");
		String mrNo = (String)visitDetailsBean.get("mr_no");

		// Update all visits & bills
		ChangeRatePlanBO chRatePlanBO = new ChangeRatePlanBO();
		StringBuilder successMsg = new StringBuilder();
		ArrayList<String> ratePlanNotApplicableList = new ArrayList<String>();
		if (!billsRequired.equals("none")) {
			if (tpaBills != null && tpaBills.size() > 0) {
				successMsg.append("Updated Charges for Open Bill(s) : <br/>");
			}else {
				successMsg.append("No Open TPA Bill(s) to update charges.");
			}
		}
		chRatePlanBO.setSuccessMsg(successMsg);
		chRatePlanBO.setRatePlanNotApplicableList(ratePlanNotApplicableList);
		chRatePlanBO.setEditVisits(true);

		Integer insuranceId = visitDetailsBean.get("insurance_id") == null
							|| visitDetailsBean.get("insurance_id").equals("")? 0 : (Integer)visitDetailsBean.get("insurance_id");

		String err = EditVisitDetailsDAO.updateVisitAndBillInsuranceDetails(con, visitDetailsBean,
				allVisits, tpaBills, userName, insuranceId, priTpaId, priInsuranceCompId, secTpaId, secInsuranceCompId,
				planIds, categoryId, policyValidityStart, policyValidityEnd,
				policyHolder, policyNumber, policyNo, patRelation,
				primaryInsuranceApproval, secondaryInsuranceApproval, priorAuthId, priorAuthModeId,
				orgId, useDRG, usePerdiem);

		if (!billsRequired.equals("none")) {
			if (err == null && allVisits != null && allVisits.size() > 0) {
				for (BasicDynaBean visitBean : allVisits) {
					String visitId = (String) visitBean.get("patient_id");
          BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
          boolean shouldUpdateCharge = "Y"
              .equals(genericPrefs.get("update_charge_on_rate_plan_change"));
          if (shouldUpdateCharge) {
            err = chRatePlanBO.updateChargesBedAndRateWiseNew(con, visitId, null);
          }
					if (err != null) {
						return "Failed to update rate plan for Visit.";
					}
				}
			} else {
				return "Failed to update charges for Visit.";
			}
		}

		/*if (tpaBills != null && tpaBills.size() > 0) {
			// Update claim_id for all bills
			if (err == null) {
				EditVisitDetailsDAO.updateBillsClaimId(con, tpaBills);
			}
		} */

		// Basic Insurance details
		err = EditVisitDetailsDAO.insertOrUpdateInsurance(con, userName,insuranceId, mrNo, patientId, null,
				priTpaId, policyValidityStart, policyValidityEnd, policyHolder, policyNo, policyNumber, patRelation,
				primaryInsuranceApproval,secondaryInsuranceApproval, priorAuthId, priorAuthModeId);

		String msg = "";

		if (err != null) {
			return "Error updating Insurance case..";
		} else {

			if (priTpaId != null && !priTpaId.equals(""))
				msg = msg + " TPA added/edited for Patient " + (String)visitDetailsBean.get("mr_no");
			else
				msg = msg + " TPA removed from Patient " + (String)visitDetailsBean.get("mr_no");
			if (EditVisitDetailsDAO.billsWithStoreCharges != null
					&& !EditVisitDetailsDAO.billsWithStoreCharges.isEmpty())
				msg = msg
						+ "<br /> Please adjust the medicine sales and item issue amounts in the following bills for insurance : <br/>"
						+ join(",", EditVisitDetailsDAO.billsWithStoreCharges);
			
			interfaceEventMappingService.editInsuranceDetailsEvent(patientId);
		}

		msg = msg + "<br/>" + chRatePlanBO.getSuccessMsg().toString();

		if (chRatePlanBO.getRatePlanNotApplicableList() != null
				&& chRatePlanBO.getRatePlanNotApplicableList().size() > 0) {
			BasicDynaBean orgBean = new GenericDAO("organization_details").findByKey("org_id", orgId);
			String newRatePlanName = (String) orgBean.get("org_name");
			StringBuffer sb = new StringBuffer(
					"There are some charges in bill(s) which are not applicable for rate plan: "
							+ newRatePlanName + "</br>");
			if (chRatePlanBO.getRatePlanNotApplicableList().size() > 5)
				msg = msg + sb.toString();
			else {
				for (String chargeDesc : (ArrayList<String>) chRatePlanBO.getRatePlanNotApplicableList()) {
					sb.append(chargeDesc + "</br>");
				}
				msg = msg + sb.toString();
			}
		}

		return msg;
	}

	public static String join(String delimiter,	Iterable<? extends CharSequence> objs) {
		Iterator<? extends CharSequence> iter = objs.iterator();
		StringBuilder buffer = new StringBuilder();
		buffer.append(iter.next());
		while (iter.hasNext()) {
			buffer.append(delimiter).append(iter.next());
		}
		return buffer.toString();
	}

	public boolean updateInsurancePolicyDetails(Connection con, BasicDynaBean visitDetailsBean,
			int planId, Map policyDetailsMap, BasicDynaBean sponsorTypeBean) throws SQLException, IOException {

		boolean success = true;
		int patientPolicyId = 0;

		if (planId > 0) {
			String mrNo = (String) visitDetailsBean.get("mr_no");
			String policyNo = (String) policyDetailsMap.get("policy_no");
			String policyNumber = (String) policyDetailsMap.get("policy_number");
			String policyHolder = (String) policyDetailsMap.get("policy_holder_name");
			String relation = (String) policyDetailsMap.get("patient_relationship");
			Date startValidity = (Date) policyDetailsMap.get("policy_validity_start");
			Date endValidity = (Date) policyDetailsMap.get("policy_validity_end");

			String documentUsage = (String) policyDetailsMap.get("document_usage");

			GenericDAO patPolicyDao = patientPolicyDetails;
			BasicDynaBean patientPolicyBean = patPolicyDao.getBean();
			patientPolicyBean.set("mr_no", mrNo);
			patientPolicyBean.set("member_id", policyNo);
			patientPolicyBean.set("policy_number", policyNumber);
			patientPolicyBean.set("policy_holder_name", policyHolder);
			patientPolicyBean.set("policy_validity_start", startValidity);
			patientPolicyBean.set("policy_validity_end", endValidity);
			patientPolicyBean.set("plan_id", planId);
			patientPolicyBean.set("patient_relationship", relation);

			boolean insertPatientPolicy = false;
			List<BasicDynaBean> existingPatPolicyDetailsList = patPolicyDao.findAllByKey("mr_no", mrNo);

			// Get the patient policy id if the membership has validity.
			patientPolicyId = editInsHelper.memberShipValidityExists(existingPatPolicyDetailsList,
									policyNo, planId, endValidity ,sponsorTypeBean);

			if (documentUsage.equals("Update")) {
				patientPolicyId = (Integer)visitDetailsBean.get("patient_policy_id");
			}else if (documentUsage.equals("New")) {

			}

			if (patientPolicyId == 0) {
				insertPatientPolicy = true;
			} else {
				insertPatientPolicy = false;
				visitDetailsBean.set("patient_policy_id", patientPolicyId);
			}

			// Update the expired policies as Inactive.
			Map<String, Object> keys = new HashMap<String, Object>();
			Map<String, Object> fields = new HashMap<String, Object>();

			fields.put("status", "I");
			keys.put("mr_no", mrNo);
			keys.put("member_id", policyNo);
			patPolicyDao.update(con, fields, keys);

			// Activate current policy
			keys = new HashMap<String, Object>();
			fields = new HashMap<String, Object>();
			fields.put("status", "A");
			keys.put("patient_policy_id", patientPolicyId);
			patPolicyDao.update(con, fields, keys);

			if (insertPatientPolicy) {

				int nextPatientPolicyDetId = DataBaseUtil.getNextSequence(con, "patient_policy_details_patient_policy_id_seq");

				// Insert the policy with new policy Validity end date.
				visitDetailsBean.set("patient_policy_id", nextPatientPolicyDetId);
				patientPolicyBean.set("patient_policy_id", nextPatientPolicyDetId);
				success = patPolicyDao.insert(con, patientPolicyBean);

				patientPolicyId = nextPatientPolicyDetId;

			} else {
				int i = patPolicyDao.update(con, patientPolicyBean.getMap(), "patient_policy_id", patientPolicyId);
				success = i > 0;
			}
		}
		return success;
	}

	public boolean updateInsurancePolicyDetailsN(Connection con, BasicDynaBean visitDetailsBean,
			int planId, Map policyDetailsMap, List<BasicDynaBean> allVisits, int priority) throws SQLException, IOException {

		boolean success = true;
		String mrNo = (String)visitDetailsBean.get("mr_no");
		String visitId =(String)visitDetailsBean.get("patient_id");
		String policyNo = (String)policyDetailsMap.get("policy_no");
		String policyNumber = (String)policyDetailsMap.get("policy_number");
		String policyHolder = (String)policyDetailsMap.get("policy_holder_name");
		String relation = (String)policyDetailsMap.get("patient_relationship");
		Date startValidity = (Date)policyDetailsMap.get("policy_validity_start");
		Date endValidity = (Date)policyDetailsMap.get("policy_validity_end");

		if(planId > 0) {
			BasicDynaBean patientPolicyBean = patientPolicyDetails.getBean();
			patientPolicyBean.set("mr_no", mrNo);
			patientPolicyBean.set("member_id", policyNo != null ? policyNo.trim() : null );
			patientPolicyBean.set("policy_number", policyNumber);
			patientPolicyBean.set("policy_holder_name", policyHolder);
			patientPolicyBean.set("policy_validity_start", startValidity);
			patientPolicyBean.set("policy_validity_end", endValidity);
			patientPolicyBean.set("plan_id", planId);
			patientPolicyBean.set("patient_relationship", relation);

			Map identifiers = new HashMap();
			identifiers.put("visit_id", visitId);
			identifiers.put("patient_policy_id", visitDetailsBean.get("patient_policy_id"));
			BasicDynaBean existingPatPolicyDetails = patientPolicyDetails.findByKey(identifiers);

			if(null != existingPatPolicyDetails && existingPatPolicyDetails.getMap().size()>0){
				if(null != allVisits && allVisits.size()>0){
					for(BasicDynaBean bean1:allVisits){
						Map keys2 = new HashMap();
						keys2.put("mr_no", mrNo);
						keys2.put("priority", priority);
						keys2.put("patient_id",bean1.get("patient_id"));
						BasicDynaBean existingPolicyDetailsFollowUps = patientInsPlanDAO.findByKey(keys2);
						if(null != existingPolicyDetailsFollowUps && existingPolicyDetailsFollowUps.getMap().size()>0){
							Map keys = new HashMap();
							keys.put("mr_no", mrNo);
							keys.put("patient_policy_id", existingPolicyDetailsFollowUps.get("patient_policy_id"));
							keys.put("visit_id",bean1.get("patient_id"));
							patientPolicyDetails.update(con, patientPolicyBean.getMap(), keys);

							Map keys1 = new HashMap();
							keys1.put("mr_no", mrNo);
							keys1.put("plan_id", planId);
							keys1.put("patient_id",bean1.get("patient_id"));
							Map fields = new HashMap();
							fields.put("patient_policy_id", existingPolicyDetailsFollowUps.get("patient_policy_id"));
							patientInsPlanDAO.update(con, fields, keys1);

						}
					}
				}
			}else{
				if(null != allVisits && allVisits.size()>0){
					for(BasicDynaBean bean2:allVisits){
						Map keys = new HashMap();
						keys.put("visit_id",  bean2.get("patient_id"));
						keys.put("plan_id", planId);
						BasicDynaBean existingPolicyDetailsFollowUps = patientPolicyDetails.findByKey(keys);
						int nextPatientPolicyDetId=0;
						if(null == existingPolicyDetailsFollowUps){
							nextPatientPolicyDetId = DataBaseUtil.getNextSequence(con, "patient_policy_details_patient_policy_id_seq");
							patientPolicyBean.set("patient_policy_id", nextPatientPolicyDetId);
							patientPolicyBean.set("visit_id", bean2.get("patient_id"));
							success = patientPolicyDetails.insert(con, patientPolicyBean);
							visitDetailsBean.set("patient_policy_id", nextPatientPolicyDetId);
						}
					}
				}
			}
		}		
		return true;
	}

	@IgnoreConfidentialFilters
	public ActionForward getdetailsAJAX(ActionMapping mapping, ActionForm form,
										HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

		HttpSession session = (HttpSession) request.getSession(false);

		String patientCategoryId = request.getParameter("patient_category");
		String visitType = request.getParameter("visit_type");

		Map responseMap = new HashMap();
		Integer centerId = (Integer) session.getAttribute("centerId");
		response.setContentType("text/plain");
		response.setHeader("Cache-Control", "no-cache");

		/*List<BasicDynaBean> planApplicableList = new ArrayList<BasicDynaBean>();
		Map<String, String> filterMap = new HashMap<String, String>();
		filterMap.put("status", "A");
		if (visitType != null && visitType.equals("i")) {
			filterMap.put("ip_applicable", "Y");
			planApplicableList = planMasterDAO.listAll(null, filterMap, "plan_name");
		} else {
			filterMap.put("op_applicable", "Y");
			planApplicableList = planMasterDAO.listAll(null, filterMap, "plan_name");
		}

		responseMap.put("policynames", ConversionUtils
				.listBeanToListMap(planApplicableList)); */

		responseMap.put("companyTpaList", ConversionUtils
				.listBeanToListMap(insCompTPAMasterDAO.getCompanyTpaList(centerId)));

		responseMap.put("insCompTpaListAll", ConversionUtils.
				listBeanToListMap(insCompTPAMasterDAO.getCompanyTpaList()));

		List compList = InsuCompMasterDAO.getActiveCompanyNames();
		List<BasicDynaBean> patCatCompaniesList = PatientCategoryDAO.getAllowedInsCompanies(Integer.parseInt(patientCategoryId), visitType);

		compList = (patCatCompaniesList == null || patCatCompaniesList.size() == 0) ? compList
				: ConversionUtils.listBeanToListMap(patCatCompaniesList);
		responseMap.put("insuranceCompaniesLists", compList);

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.getWriter().write(js.deepSerialize(responseMap));
		response.flushBuffer();
		return null;
	}

	public boolean updateCorporateDetails(Connection con, BasicDynaBean visitDetailsBean,
			HashMap<String, Object> corporateDetailsMap, boolean isPrimary
		) throws SQLException, IOException, ParseException {

		boolean success = true;
		String sponsorId = (String) corporateDetailsMap.get("sponsor_id");
		String mrNo = (String) visitDetailsBean.get("mr_no");
		String employeeId = (String) corporateDetailsMap.get("employee_id");
		String employeeName = (String) corporateDetailsMap.get("employee_name");
		String relation = (String) corporateDetailsMap.get("patient_relationship");

		GenericDAO corporateDetDao= new GenericDAO("patient_corporate_details");

		String documentUsage = (String) corporateDetailsMap.get("document_usage");

		List<BasicDynaBean> existingCorporateDetailsList = corporateDetDao.findAllByKey("mr_no", mrNo);
		int patientCorporateId = editInsHelper.corporateValidityExists(existingCorporateDetailsList,
				employeeId, sponsorId, null);

		boolean insertCorporateDet = false;

		if (documentUsage.equals("Update")) {
			if(isPrimary)
				patientCorporateId = (Integer)visitDetailsBean.get("patient_corporate_id");
			else
				patientCorporateId = (Integer)visitDetailsBean.get("secondary_patient_corporate_id");
		}else if (documentUsage.equals("New")) { }


		if (patientCorporateId == 0)
			insertCorporateDet = true;

		if (sponsorId != null && !sponsorId.equals("")) {
			BasicDynaBean corporateBean = corporateDetDao.getBean();
			corporateBean.set("mr_no", mrNo);
			corporateBean.set("employee_id", employeeId);
			corporateBean.set("employee_name", employeeName);
			corporateBean.set("sponsor_id", sponsorId);
			corporateBean.set("patient_relationship", relation);

			if(insertCorporateDet) {

				int nextpatientCorporateId = DataBaseUtil.getNextSequence(con, "patient_corporate_id_seq");

				if(isPrimary)
					visitDetailsBean.set("patient_corporate_id", nextpatientCorporateId);
				else
					visitDetailsBean.set("secondary_patient_corporate_id", nextpatientCorporateId);

				corporateBean.set("patient_corporate_id", nextpatientCorporateId);
				success = corporateDetDao.insert(con, corporateBean);

			} else {

				if(isPrimary)
					visitDetailsBean.set("patient_corporate_id", patientCorporateId);
				else
					visitDetailsBean.set("secondary_patient_corporate_id", patientCorporateId);

				corporateBean.set("patient_corporate_id", patientCorporateId);
				success = corporateDetDao.update(con, corporateBean.getMap(), "patient_corporate_id", patientCorporateId)>=1;
			}
		}
		return success;
	}

	public boolean updateNationalDetails(Connection con, BasicDynaBean visitDetailsBean,
			HashMap<String, Object> nationalDetailsMap, boolean isPrimary
		) throws SQLException, IOException, ParseException {

		boolean success = true;
		String sponsorId = (String) nationalDetailsMap.get("sponsor_id");
		String mrNo = (String) visitDetailsBean.get("mr_no");
		String nationalId = (String) nationalDetailsMap.get("national_id");
		String citizenName = (String) nationalDetailsMap.get("citizen_name");
		String relation = (String) nationalDetailsMap.get("patient_relationship");
		GenericDAO nationalDetDao = new GenericDAO("patient_national_sponsor_details");

		String documentUsage = (String) nationalDetailsMap.get("document_usage");

		List<BasicDynaBean> existingNationalDetailsList = nationalDetDao.findAllByKey("mr_no", mrNo);
		int patientNationalId = editInsHelper.nationalValidityExists(existingNationalDetailsList,
				nationalId, sponsorId, null);

		boolean insertNationalDet = false;

		if (documentUsage.equals("Update")) {
			if(isPrimary)
				patientNationalId = (Integer)visitDetailsBean.get("patient_national_sponsor_id");
			else
				patientNationalId = (Integer)visitDetailsBean.get("secondary_patient_national_sponsor_id");
		}else if (documentUsage.equals("New")) { }


		if (patientNationalId == 0)
			insertNationalDet = true;

		if (sponsorId != null && !sponsorId.equals("")) {
			BasicDynaBean nationalBean = nationalDetDao.getBean();
			nationalBean.set("mr_no", mrNo);
			nationalBean.set("national_id", nationalId);
			nationalBean.set("citizen_name", citizenName);
			nationalBean.set("sponsor_id", sponsorId);
			nationalBean.set("patient_relationship", relation);

			if(insertNationalDet) {

				int nextpatientNationalId = DataBaseUtil.getNextSequence(con, "patient_national_sponsor_id_seq");

				if(isPrimary) {
					visitDetailsBean.set("patient_national_sponsor_id", nextpatientNationalId);
				} else {
					visitDetailsBean.set("secondary_patient_national_sponsor_id", nextpatientNationalId);
				}

				nationalBean.set("patient_national_sponsor_id", nextpatientNationalId);
				success = nationalDetDao.insert(con, nationalBean);

			} else {

				if(isPrimary) {
					visitDetailsBean.set("patient_national_sponsor_id", patientNationalId);
				} else {
					visitDetailsBean.set("secondary_patient_national_sponsor_id", patientNationalId);
				}

				nationalBean.set("patient_national_sponsor_id", patientNationalId);
				success = nationalDetDao.update(con, nationalBean.getMap(), "patient_national_sponsor_id", patientNationalId)>=1;
			}
		}
		return success;
	}

	public ActionForward updateTpa(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException, ParseException,
			Exception {

		String path = request.getContextPath();
		Connection con  = null;
		boolean removeDrg = false;
		
		Map genPrefMap = GenericPreferencesDAO.getAllPrefs().getMap();
		String msg = "";
		String visitId = request.getParameter("visitId");
		String billNo = request.getParameter("billNo");
		String isNewUX = request.getParameter("isNewUX");

		try{

			String billsRequired = request.getParameter("bills_to_change_sponsor_amounts");	

			String primarySponsor = request.getParameter("primary_sponsor");
			String secondarySponsor = request.getParameter("secondary_sponsor");
			List<BasicDynaBean> patPrimaryInsuranceDetailsBeanList = new ArrayList<BasicDynaBean>();
			List<BasicDynaBean> patSecondaryInsuranceDetailsBeanList =new ArrayList<BasicDynaBean>();

			 int primary_policy_id = (request.getParameter("primary_policy_id") != null
					 && !request.getParameter("primary_policy_id").equals("")) ? Integer.parseInt(request.getParameter("primary_policy_id")) : 0;
			 int secondary_policy_id = (request.getParameter("secondary_policy_id") != null
					&& !request.getParameter("secondary_policy_id").equals("")) ? Integer.parseInt(request.getParameter("secondary_policy_id")) : 0;

			String primarySponsorId = null;
			String primaryInsCompId = null;

			String secondarySponsorId = null;
			String secondaryInsCompId = null;

			HttpSession session = request.getSession();
			String userName = (String)session.getAttribute("userid");

			int planId = 0;
			int categoryId = 0;
			String useDRG = "N";
			String usePerdiem = "N";

			int secPlanId = 0;
			String secUseDRG = "N";
			String secUsePerdiem = "N";

			String priorAuthId = null;
			int priorAuthModeId = 0;

			int patientPolicyId = 0;

			String primaryInsuranceApprovalStr = null;
			String secondaryInsuranceApprovalStr = null;

			BigDecimal primaryInsuranceApproval = null;
			BigDecimal secondaryInsuranceApproval = null;

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			BasicDynaBean primaryInsuranceBean = null;
			BasicDynaBean secondaryInsBean = null;
			PatientPolicyDAO patPolicyDao = new PatientPolicyDAO();
			// Remove Insurance details
			EditVisitDetailsDAO.removeInsurance(con, visitId, userName);
			String insDetailsEdited=request.getParameter("insEdited");
			String visitLimitsEdited=request.getParameter("visitLimitsChanged");
			BasicDynaBean priSponsorTypeBean = null,secSponsorTypeBean = null;
			Boolean insuranceDetailChanged = false;
			
			// Get Primary Sponsor Details
			if (primarySponsor != null) {
				if (primarySponsor.equals("I")) {
					primarySponsorId = request.getParameter("primary_sponsor_id");
                    BasicDynaBean priSponsorBean =
                        tpaMasterDAO.findByKey("tpa_id", primarySponsorId);
					if(null != priSponsorBean)
                      priSponsorTypeBean = sponsorTypeDAO.findByKey("sponsor_type_id",
                          priSponsorBean.get("sponsor_type_id"));
					primaryInsCompId = request.getParameter("primary_insurance_co");
					primaryInsuranceBean = patientInsPlanDAO.getBean();
					ConversionUtils.copyToDynaBean(request.getParameterMap(), primaryInsuranceBean);
					primaryInsuranceBean = getPrimarySponsorDetails(request, primaryInsuranceBean);

					primaryInsuranceApprovalStr = getParamDefault(request, "primary_insurance_approval", null);

			    	planId = request.getParameter("primary_plan_id")==null || request.getParameter("primary_plan_id").equals("")?0
		    				: Integer.parseInt(request.getParameter("primary_plan_id"));
			    	categoryId = request.getParameter("primary_plan_type")==null || request.getParameter("primary_plan_type").equals("")?0
		    				: Integer.parseInt(request.getParameter("primary_plan_type"));

			    	useDRG = (request.getParameter("primary_use_drg") != null
							&& !request.getParameter("primary_use_drg").equals("")) ? request.getParameter("primary_use_drg") : useDRG;

					usePerdiem = (request.getParameter("primary_use_perdiem") != null
							&& !request.getParameter("primary_use_perdiem").equals("")) ? request.getParameter("primary_use_perdiem") : usePerdiem;

					priorAuthId = request.getParameter("primary_prior_auth_id");
					priorAuthModeId = request.getParameter("primary_prior_auth_mode_id") == null
							|| request.getParameter("primary_prior_auth_mode_id").equals("") ? 0
							: Integer.parseInt(request.getParameter("primary_prior_auth_mode_id"));


				}
				primaryInsuranceApproval = primaryInsuranceApprovalStr != null ? new BigDecimal(primaryInsuranceApprovalStr) : null;
			}
							
			// Get Secondary Sponsor Details
			if (secondarySponsor != null) {
				if (secondarySponsor.equals("I")) {
					secondarySponsorId = request.getParameter("secondary_sponsor_id");
                    BasicDynaBean secSponsorBean =
                        tpaMasterDAO.findByKey("tpa_id", secondarySponsorId);
					if(null != secSponsorBean)
                      secSponsorTypeBean = sponsorTypeDAO.findByKey("sponsor_type_id",
                          secSponsorBean.get("sponsor_type_id"));
					secondaryInsCompId = request.getParameter("secondary_insurance_co");
					secondaryInsBean = patientInsPlanDAO.getBean();
					ConversionUtils.copyToDynaBean(request.getParameterMap(), secondaryInsBean);
					secondaryInsBean = getSecondarySponsorDetails(request, secondaryInsBean);
					secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_insurance_approval", null);

					// Primary sponsor plan does not exists then choose secondary plan

					/*planId = request.getParameter("secondary_plan_id")==null || request.getParameter("secondary_plan_id").equals("")?0
		    				: Integer.parseInt(request.getParameter("secondary_plan_id"));*/

					secPlanId = null != secondaryInsBean.get("plan_id") && !secondaryInsBean.get("plan_id").equals("")
							? (Integer)(secondaryInsBean.get("plan_id")) : 0;

					categoryId = request.getParameter("secondary_plan_type")==null || request.getParameter("secondary_plan_type").equals("")?0
		    				: Integer.parseInt(request.getParameter("secondary_plan_type"));

					secUseDRG = (request.getParameter("secondary_use_drg") != null
							&& !request.getParameter("secondary_use_drg").equals("")) ? request.getParameter("secondary_use_drg") : secUseDRG;

					usePerdiem = (request.getParameter("secondary_use_perdiem") != null
							&& !request.getParameter("secondary_use_perdiem").equals("")) ? request.getParameter("secondary_use_perdiem") : usePerdiem;


					if (priorAuthId == null && priorAuthModeId == 0) {
						priorAuthId = request.getParameter("secondary_prior_auth_id");
						priorAuthModeId = request.getParameter("secondary_prior_auth_mode_id") == null
								|| request.getParameter("secondary_prior_auth_mode_id").equals("") ? 0
								: Integer.parseInt(request.getParameter("secondary_prior_auth_mode_id"));
					}

				}

				secondaryInsuranceApproval = secondaryInsuranceApprovalStr != null ? new BigDecimal(secondaryInsuranceApprovalStr) : null;
			}

			HashMap<String, Object> policyDetailsMap = new HashMap<String, Object>();
			HashMap<String, Object> secPolicyDetailsMap = new HashMap<String, Object>();

			regBO.createInsuranceDetailsMap(request, policyDetailsMap, secPolicyDetailsMap);			

			Map requestParams = request.getParameterMap();		

			BasicDynaBean visitDetailsBean = VisitDetailsDAO.getVisitDetails(visitId);
			boolean success = false;
			List<BasicDynaBean> tpaBills = null;
			List<String> unreopenedBills = new ArrayList<String>();
			List<BasicDynaBean> existingSponsorDetails = patientInsPlanDAO.getPlanDetails(visitId);
			int exisitngPriPlan = 0;
      int existingSecPlan = 0;

			boolean isRatePlanChanged = false;
			do {

				visitDetailsBean.set("org_id" , getParamDefault(request,"organization","ORG0001"));
				HashMap<String, String> orgMap = new HashMap<String, String>();
				orgMap.put("org_id", getParamDefault(request,"organization","ORG0001"));
				orgMap.put("user_name", userName);

				new GenericDAO("patient_registration").update(con, orgMap , "patient_id", visitId);

				//success = patientInsurancePlansInsertOrUpdate(con, primaryInsuranceBean, secondaryInsBean);	

				visitDetailsBean.set("primary_sponsor_id", primarySponsorId);
				visitDetailsBean.set("primary_insurance_co", primaryInsCompId);
				visitDetailsBean.set("secondary_sponsor_id", secondarySponsorId);
				visitDetailsBean.set("secondary_insurance_co", secondaryInsCompId);
				visitDetailsBean.set("secondary_insurance_approval", secondaryInsuranceApproval);
				visitDetailsBean.set("primary_insurance_approval", primaryInsuranceApproval);
				visitDetailsBean.set("prior_auth_id", priorAuthId);
				visitDetailsBean.set("prior_auth_mode_id", priorAuthModeId);
				visitDetailsBean.set("user_name", userName);
				
				if(useDRG != null)
					visitDetailsBean.set("use_drg", useDRG.equals("") ? 'N' : useDRG);
				
				/*if(secUseDRG!= null && !secUseDRG.equals(""))
					visitDetailsBean.set("use_drg", secUseDRG);*/

				if(usePerdiem!= null && usePerdiem.equals(""))
					visitDetailsBean.set("use_perdiem", usePerdiem);
				if(secUsePerdiem!= null && secUsePerdiem.equals(""))
					visitDetailsBean.set("use_perdiem", secUsePerdiem);

				String hasSecSponsor = (visitDetailsBean.get("secondary_sponsor_id") != null
						&& !visitDetailsBean.get("secondary_sponsor_id").equals("")) ? "Y" : "N";

				// Existing visit details (Insurance details)
				BasicDynaBean existingVisit = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
				
				String exisitnPriSponsor = null;
				String existingSecSponsor = null;
				
				for(int i = 0;i<existingSponsorDetails.size();i++){
					if( i == 0 ){//primary sponsor details
						exisitnPriSponsor = (String)existingSponsorDetails.get(i).get("sponsor_id");
						exisitngPriPlan = (Integer)existingSponsorDetails.get(i).get("plan_id");
					} else {
						existingSecSponsor = (String)existingSponsorDetails.get(i).get("sponsor_id");
						existingSecPlan = (Integer)existingSponsorDetails.get(i).get("plan_id");
					}
				}

				if (exisitnPriSponsor == null || exisitnPriSponsor.equals("")) {

					exisitnPriSponsor = (existingVisit.get("primary_sponsor_id") != null && !existingVisit.get("primary_sponsor_id").equals("")) ?
							(String)existingVisit.get("primary_sponsor_id") : null;
				}

				if (existingSecSponsor == null || existingSecSponsor.equals("")) {

					existingSecSponsor = (existingVisit.get("secondary_sponsor_id") != null && !existingVisit.get("secondary_sponsor_id").equals("")) ?
							(String)existingVisit.get("secondary_sponsor_id") : null;
				}

				String visitSponsor		= visitDetailsBean.get("primary_sponsor_id") != null ? (String)visitDetailsBean.get("primary_sponsor_id") : "";
				String visitRatePlan	= visitDetailsBean.get("org_id") != null ? (String)visitDetailsBean.get("org_id") : "";
				int visitPlan			= planId > 0 ? planId : (secPlanId > 0 ? secPlanId : 0);

				String existingVisitSponsor		= existingVisit.get("primary_sponsor_id") != null ? (String)existingVisit.get("primary_sponsor_id") : "";
				String existingVisitRatePlan	= existingVisit.get("org_id") != null ? (String)existingVisit.get("org_id") : "";
				int existingVisitPlan			= (Integer)existingVisit.get("plan_id");
				String existingVisitUseDRG		= existingVisit.get("use_drg") != null ? (String)existingVisit.get("use_drg") : "N";
				String existingVisitUsePerdiem	= existingVisit.get("use_perdiem") != null ? (String)existingVisit.get("use_perdiem") : "N";
				
				if(!visitRatePlan.equals(existingVisitRatePlan)) {
					isRatePlanChanged = true;
				}
				if(!useDRG.equals(existingVisitUseDRG) && useDRG.equals("N"))  removeDrg = true;
				
				// In order to copy the Main Visit Insurance details changes to all
				// Follow Up Visits, get all episode followup visits.
				List<BasicDynaBean> allVisits = editInsHelper.getMainAndFollowUpVisits(visitId);

				// If no rate plan change (or) no insurance details change
				// then insurance changes are not required
				// so, do not reopen or get any bills to update claim amounts.
				if (( (exisitnPriSponsor != null && primarySponsorId != null && exisitnPriSponsor.equals(primarySponsorId))
							&& ( (existingSecSponsor == null && secondarySponsorId == null )
								|| (existingSecSponsor != null && secondarySponsorId != null && existingSecSponsor.equals(secondarySponsorId)) ))
						&& ( ( exisitngPriPlan == planId ) && (existingSecPlan == secPlanId) )
						&& visitRatePlan.equals(existingVisitRatePlan)
						&& useDRG.equals(existingVisitUseDRG)
						&& usePerdiem.equals(existingVisitUsePerdiem))
				{
					if(insDetailsEdited.equalsIgnoreCase("true") || visitLimitsEdited.equals("Y")){
						tpaBills = editInsHelper.getMainAndFollowUpVisitTPABills(visitId, billsRequired);
						editInsHelper.reopenBills(con, tpaBills, hasSecSponsor, unreopenedBills);
					}

				}
				else {
					tpaBills = editInsHelper.getMainAndFollowUpVisitTPABills(visitId, billsRequired);
					editInsHelper.reopenBills(con, tpaBills, hasSecSponsor, unreopenedBills);
				}

				if (!billsRequired.equals("none")) {
					if (unreopenedBills != null && unreopenedBills.size() > 0) {
						String billNos = "";
						for (String bill_no : unreopenedBills) {
							billNos = submitdao.urlString(path, "bill", bill_no, null) +", "+billNos;
						}
						msg+= "The following bills are not reopened as claim is marked as Sent : <br/>" + billNos;
					}
				}
				/* Insurance details. */
				if (primarySponsor != null && !primarySponsor.equals("") && primarySponsor.equals("I")) {
					primaryInsuranceBean.set("mr_no", visitDetailsBean.get("mr_no"));
					primaryInsuranceBean.set("patient_id", visitDetailsBean.get("patient_id"));

					if (planId > 0) {
						success = updateInsurancePolicyDetailsN(con, primaryInsuranceBean, planId, policyDetailsMap,allVisits,1);
					}
				}
				
				if (secondarySponsor != null && !secondarySponsor.equals("") && secondarySponsor.equals("I")) {
					secondaryInsBean.set("mr_no", visitDetailsBean.get("mr_no"));
		    		secondaryInsBean.set("patient_id", visitDetailsBean.get("patient_id"));

					if (secPlanId > 0) {
						success = updateInsurancePolicyDetailsN(con, secondaryInsBean, secPlanId, secPolicyDetailsMap,allVisits,2);
					}
				}


				visitDetailsBean.set("plan_id", planId);
				patientPolicyId = visitDetailsBean.get("patient_policy_id") != null ? (Integer)visitDetailsBean.get("patient_policy_id") : 0;
				visitDetailsBean.set("patient_policy_id", patientPolicyId);

				Date policyValidityStart = null;
				Date policyValidityEnd = null;
				String policyHolder = null;
				String policyNumber = null;
				String policyNo = null;
				String patRelation = null;

				if (primarySponsor != null) {
					if (primarySponsor.equals("I")) {
						policyValidityStart = policyDetailsMap.get("policy_validity_start") != null ? (java.sql.Date)policyDetailsMap.get("policy_validity_start") : null;
						policyValidityEnd = policyDetailsMap.get("policy_validity_end") != null ? (java.sql.Date)policyDetailsMap.get("policy_validity_end") : null;
						policyHolder = policyDetailsMap.get("policy_holder_name") != null ? (String)policyDetailsMap.get("policy_holder_name") : null;
						policyNumber = policyDetailsMap.get("policy_number") != null ? (String)policyDetailsMap.get("policy_number") : null;
						policyNo = policyDetailsMap.get("policy_no") != null ? (String)policyDetailsMap.get("policy_no") : null;
						patRelation = policyDetailsMap.get("patient_relationship") != null ? (String)policyDetailsMap.get("patient_relationship") : null;

					}
				}

				int noOfPlans = 0;
				int planIds[] = null;
				int planIdIdx = 0;
				boolean priPlanExists = null != primaryInsuranceBean && null != primaryInsuranceBean.get("plan_id") &&
					(Integer)primaryInsuranceBean.get("plan_id") > 0;
				boolean secPlanExisits = null != secondaryInsBean && null != secondaryInsBean.get("plan_id") &&
					(Integer)secondaryInsBean.get("plan_id") > 0;
				if(priPlanExists) noOfPlans++;
				if(secPlanExisits) noOfPlans++;

				planIds = noOfPlans > 0 ? new int[noOfPlans] : null;
				if(priPlanExists) planIds[planIdIdx++] = (Integer)primaryInsuranceBean.get("plan_id");
				if(secPlanExisits)planIds[planIdIdx++] = (Integer)secondaryInsBean.get("plan_id");
								
				for(BasicDynaBean bean1 : allVisits){
					Map filterMap = new HashMap();
					filterMap.put("visit_id", (String)bean1.get("patient_id"));
					List<BasicDynaBean> planDetailsForVisit = new ArrayList<BasicDynaBean>();
					planDetailsForVisit = patientInsurancePlanDetails.listAll(null, filterMap, null);
					patPrimaryInsuranceDetailsBeanList.addAll(getPrimaryInsuranceDetails(request,(String)bean1.get("patient_id"),(String)visitDetailsBean.get("visit_type")));
					patSecondaryInsuranceDetailsBeanList.addAll(getSecondaryInsuranceDetails(request,(String)bean1.get("patient_id"),(String)visitDetailsBean.get("visit_type")));
				}
				
				patientInsPlanDAO.updatePlanDetails(con, allVisits, primaryInsuranceBean, secondaryInsBean,planIds,patPrimaryInsuranceDetailsBeanList,
						patSecondaryInsuranceDetailsBeanList);
				
				//If update insurance and credit notes exist. All credit notes for current visit will be cancelled
				cancelCreditNotes(visitId, con);
				// Visit bills
				List<BasicDynaBean> visitTPABills = editInsHelper.getMainAndFollowUpVisitTPABills(visitId, billsRequired);

				if (( (exisitnPriSponsor != null && primarySponsorId != null && exisitnPriSponsor.equals(primarySponsorId))
						&& ( (existingSecSponsor == null && secondarySponsorId == null )
							|| (existingSecSponsor != null && secondarySponsorId != null && existingSecSponsor.equals(secondarySponsorId)) ))
					&& ( ( exisitngPriPlan == planId ) && (existingSecPlan == secPlanId) )
					&& visitRatePlan.equals(existingVisitRatePlan)
					&& useDRG.equals(existingVisitUseDRG)
					&& usePerdiem.equals(existingVisitUsePerdiem)) {}
				else {
					BillChargeClaimDAO billChgClaimDAO = new BillChargeClaimDAO();
					if(null != visitTPABills) {
						for(BasicDynaBean tpaBill : visitTPABills){
							billChgClaimDAO.changesToBillChargeClaimOnEditIns(con, (String)tpaBill.get("bill_no"), (String)tpaBill.get("visit_id"),
									planIds, (String)tpaBill.get("visit_type"),exisitngPriPlan,existingSecPlan);
						}
					}
					if(billsRequired.equals(BillsRequired.open_bills.toString()) || billsRequired.equals(BillsRequired.none.toString())){
						List<BasicDynaBean> mainAndFollowUpVisits = editInsHelper.getMainAndFollowUpVisits(visitId);
						for(BasicDynaBean visit : mainAndFollowUpVisits) {
							List<BasicDynaBean> allInsBills = BillDAO.getAllInsuranceBills(con, (String)visit.get("patient_id"));
							List<BasicDynaBean> planList = patientInsPlanDAO.getPlanDetails(con, (String)visit.get("patient_id"));
							for(BasicDynaBean insbillBean : allInsBills){
								for(BasicDynaBean planBean : planList){
									int priority = (Integer) planBean.get("priority");
									int pnId = (Integer) planBean.get("plan_id");
									String sponsorId = (String) planBean.get("sponsor_id");
									billChgClaimDAO.updateInsuranceBillClaim(con,(String)insbillBean.get("bill_no"), pnId, sponsorId, priority);
								}
							}
						}
					}
					insuranceDetailChanged = true;
				}
				
				// Update rate plan and claim details for main and follow up visits.
				msg = updatePatientChargeDetails(con, visitDetailsBean,userName, planIds,
						primarySponsorId, primaryInsCompId, secondarySponsorId, secondaryInsCompId, categoryId, useDRG,
						policyValidityStart, policyValidityEnd, policyHolder, policyNumber, policyNo, patRelation,
						priorAuthId, priorAuthModeId, primaryInsuranceApproval, secondaryInsuranceApproval,
						tpaBills, allVisits, billsRequired, usePerdiem);

				String specializedDocType = mapping.getProperty("documentType");
				Boolean specialized = new Boolean(mapping.getProperty("specialized"));

				ChangeTpaForm tpaForm = (ChangeTpaForm)form;

				FormFile priInsDocBytea = tpaForm.getPrimary_insurance_doc_content_bytea1();

				FormFile secInsDocBytea = tpaForm.getSecondary_insurance_doc_content_bytea1();

				VisitDetailsDAO.updateSponsorDetails(con, visitDetailsBean);

				String primaryDocUpdated = request.getParameter("primaryDocUpdated");
				String secondaryDocUpdated = request.getParameter("secondaryDocUpdated");

				boolean updatePrimaryDoc = primaryDocUpdated != null && (primaryDocUpdated.equals("X") || primaryDocUpdated.equals("Y"));
				boolean updateSecondaryDoc = secondaryDocUpdated != null && (secondaryDocUpdated.equals("X") || secondaryDocUpdated.equals("Y"));

				if (updatePrimaryDoc && !regBO.regPrimarySponsorDocs(con, allVisits, requestParams,
						priInsDocBytea, specializedDocType, specialized, userName)) {
					success = false;
		    		break;
				}

				// Upload scanned secondary document
				if (updateSecondaryDoc && !regBO.regSecondarySponsorDocs(con, allVisits, requestParams,
						secInsDocBytea, specializedDocType, specialized, userName)) {
					success = false;
		    		break;
				}

				Integer prevPolicyId =  (Integer) existingVisit.get("patient_policy_id");
				boolean isprevPolicyPresent = prevPolicyId != null &&  prevPolicyId!= 0;
				boolean iscurrentPolicyPresent = visitDetailsBean.get("patient_policy_id") != null
											&& ((Integer)visitDetailsBean.get("patient_policy_id")) !=0 ;
				boolean isEmptySecSponsor = secondarySponsor == null || secondarySponsor.equals("");
				boolean isEmptyPriSponsor = primarySponsor == null || primarySponsor.equals("");

				if((isEmptySecSponsor && isEmptyPriSponsor) || (isprevPolicyPresent && !iscurrentPolicyPresent)) {
					String orgId =  getParamDefault(request,"organization","ORG0001");
					editInsHelper.removeInsuranceFromBills(con, visitDetailsBean, orgId, tpaBills);
				}

			} while (false);
			
	    DataBaseUtil.commitClose(con, true);
	    
	    updateCaseRateLimits(visitId, exisitngPriPlan, planId);
	    
	    editInsHelper.updateSponsorClaimTotals(tpaBills);

			List<BasicDynaBean> allVisits = editInsHelper.getMainAndFollowUpVisits(visitId);
			
			Map drgCodeMap = new MRDUpdateScreenBO().getDRGCode(visitId);

			if(insDetailsEdited.equalsIgnoreCase("true") || visitLimitsEdited.equals("Y") 
			    || isRatePlanChanged || insuranceDetailChanged) {

              if ((usePerdiem.equals("Y")
                      && StringUtils.isNotBlank((String)visitDetailsBean.get("per_diem_code")))
						|| (useDRG.equals("Y") && null != drgCodeMap && null != drgCodeMap.get("drg_charge_id"))) {
					sponsorBO.recalculateSponsorAmount(visitId);
					sponsorDAO.setIssueReturnsClaimAmountTOZero(visitId);
				} else  {
					for (BasicDynaBean visit : allVisits) {
						String patientId = (String) visit.get("patient_id");
						sponsorDAO.unlockVisitBillsCharges(patientId);
						sponsorDAO.unlockVisitSaleItems(patientId);
						sponsorDAO.includeBillChargesInClaimCalc(patientId);
						sponsorBO.recalculateSponsorAmount(patientId);
						sponsorDAO.setIssueReturnsClaimAmountTOZero(patientId);
						sponsorDAO.insertOrUpdateBillChargeTaxesForSales(visitId);
						sponsorDAO.lockVisitSaleItems(patientId);
						sponsorDAO.updateSalesBillCharges(patientId);
						sponsorDAO.updateTaxDetails(patientId);
					}
				}
				//schedule accounting
				List<BasicDynaBean> billsList = billBo.getVisitFinalizedAndClosedBills(visitId);
				accountingJobScheduler.scheduleAccountingForBills(billsList);
			}

      if (null != tpaBills) {
        for (BasicDynaBean billBean : tpaBills) {
          String tpaBillNo = (String) billBean.get("bill_no");
          BillDAO.resetRoundOff(tpaBillNo);
        }
      }
			
			List<BasicDynaBean> cashBills = BillDAO.getVisitCashbills(visitId);
			
      if (null != cashBills) {
        for (BasicDynaBean billBean : cashBills) {
          String cashBillNo = (String) billBean.get("bill_no");
          BillDAO.resetRoundOff(cashBillNo);
        }
      }

			ActionRedirect redirect = new ActionRedirect(mapping.findForward("changeTpaRedirect"));
			FlashScope flash = FlashScope.getScope(request);
			flash.info(msg);
			
			String visitType = (String)visitDetailsBean.get("visit_type");
      BigDecimal visitPatientDue = BillDAO.getVisitPatientDue(visitId);
      BigDecimal availableCreditLimit = visitDetailsDao.getAvailableCreditLimit(visitId, false);
      String crLimitRulePref = genPrefMap.get("ip_credit_limit_rule") != null ? (String)genPrefMap.get("ip_credit_limit_rule") : "A";
      if(visitType.equals("i") && availableCreditLimit.compareTo(BigDecimal.ZERO) < 0 && (crLimitRulePref.equals("W") || crLimitRulePref.equals("B"))) {
        String crLimitMsg = "The current patient outstanding is : "+visitPatientDue+" Available Credit Limit is : "+ availableCreditLimit;
        flash.info(flash.get("info") == null || ((String)flash.get("info")).equals("") ? crLimitMsg :
                    (String)flash.get("info")+" <br/> "+crLimitMsg);
      }
      
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("visitId", visitId);
			redirect.addParameter("billNo", billNo);
			redirect.addParameter("isNewUX", isNewUX);
			return redirect;
		} catch(Exception e){
			if (con != null && !con.isClosed())
				DataBaseUtil.commitClose(con, false);
			throw e;
		}  finally {
			DataBaseUtil.closeConnections(con, null);
			 
			if(removeDrg) {
				String drgBillNo = drgUpdateDAO.getDRGBillNo(visitId);
				
				if(null == drgBillNo){
					drgBillNo = drgUpdateDAO.getMarginDRGBillInVisit(visitId);
				}
				
				if(null != drgBillNo && !drgBillNo.equals("")){
					drgCalculator.removeDRG(drgBillNo);
					drgUpdateDAO.lockOrUnlockSaleItems(drgBillNo, false);
					drgUpdateDAO.includeSaleItemsInClaim(drgBillNo);
					sponsorBO.recalculateSponsorAmount(visitId);
					drgUpdateDAO.lockOrUnlockSaleItems(drgBillNo, true);
					sponsorDAO.updateSalesBillCharges(visitId);
					BillDAO.resetRoundOff(drgBillNo);
				}

        // schedule accounting
        List<BasicDynaBean> billsList = billBo.getVisitFinalizedAndClosedBills(visitId);
        accountingJobScheduler.scheduleAccountingForBills(billsList);
			}
		}
	}
	
  private void updateCaseRateLimits(String visitId, int exisitngPriPlan, int planId)
      throws SQLException, IOException {
    Connection con = null;
    Boolean success = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      BasicDynaBean visitBean = visitDetailsDao.findByKey(con, "patient_id", visitId);
      Boolean isCaseRateExists = null != visitBean.get("primary_case_rate_id");
      if (isCaseRateExists) {
        if (exisitngPriPlan == planId) {
          success = billBo.updateCaseRateLimts(visitBean);
        } else {
          visitBean.set("primary_case_rate_id", null);
          visitBean.set("secondary_case_rate_id", null);
          success = visitDetailsDao.update(con, visitBean.getMap(), "patient_id", visitId) >= 0;
          visitCaseRateDetDAO.delete(con, "visit_id", visitId);
        }
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }

  }

  //Cancels all credit notes associated to a visitId 
	public void cancelCreditNotes(String visitId, Connection con) throws SQLException, IOException{
		List<BasicDynaBean> creditNoteList = VisitDetailsDAO.getCreditNoteList(visitId);
		for(BasicDynaBean billBean : creditNoteList){
			String creditNoteNo = (String)billBean.get("bill_no");
			
			List<BasicDynaBean> chargeList = chargeDAO.findAllByKey(con, "bill_no", creditNoteNo);
			
			for(BasicDynaBean chargeBean : chargeList){
				chargeBean.set("amount", BigDecimal.ZERO);
				chargeBean.set("act_quantity", BigDecimal.ZERO);
				chargeBean.set("insurance_claim_amount", BigDecimal.ZERO);
				chargeBean.set("discount", BigDecimal.ZERO);
				chargeBean.set("status", ChargeDTO.CHARGE_STATUS_CANCELLED);
				chargeDAO.update(con, chargeBean.getMap(),"charge_id", (String)chargeBean.get("charge_id"));
			}
			
			
			billBean.set("total_amount", BigDecimal.ZERO);
			billBean.set("total_discount", BigDecimal.ZERO);
			billBean.set("total_claim", BigDecimal.ZERO);
			billBean.set("claim_recd_amount", BigDecimal.ZERO);
			billBean.set("status", Bill.BILL_STATUS_CANCELLED);
			billDAO.update(con, billBean.getMap(), "bill_no", creditNoteNo);
		}
		
	}
	
	public ActionForward updateFollowUpApproval(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException, ParseException,
			Exception {

		String visitId = request.getParameter("visitId");
		String billNo = request.getParameter("billNo");

		String billsRequired = request.getParameter("bills_to_change_sponsor_amounts");
		String primarySponsor = request.getParameter("primary_sponsor");
		String secondarySponsor = request.getParameter("secondary_sponsor");

		String primaryInsuranceApprovalStr = null;
		String secondaryInsuranceApprovalStr = null;

		String usePerdiem = "N";
		String secUsePerdiem = "N";
		String insDetailsEdited=request.getParameter("insEdited");
		List<BasicDynaBean> patPrimaryInsuranceDetailsBeanList = null;
		List<BasicDynaBean> patSecondaryInsuranceDetailsBeanList =null;

		Connection con  = null;
		BasicDynaBean primaryInsuranceBean = null;
		BasicDynaBean secondaryInsBean = null;
		List<BasicDynaBean> noOfPatientPlanBeans = patientInsPlanDAO.listAll(null, "patient_id", visitId, "priority");

		try{
			BigDecimal primaryInsuranceApproval = null;
			BigDecimal secondaryInsuranceApproval = null;
			// Get Primary Sponsor Details
			if (primarySponsor != null) {

				if (primarySponsor.equals("I")) {
					primaryInsuranceBean = noOfPatientPlanBeans.get(0);
					primaryInsuranceApprovalStr = getParamDefault(request, "primary_insurance_approval", null);
					usePerdiem = (request.getParameter("primary_use_perdiem") != null
							&& !request.getParameter("primary_use_perdiem").equals("")) ? request.getParameter("primary_use_perdiem") : usePerdiem;


				}else if (primarySponsor.equals("C"))
					primaryInsuranceApprovalStr = getParamDefault(request, "primary_corporate_approval", null);

				else if (primarySponsor.equals("N"))
					primaryInsuranceApprovalStr = getParamDefault(request, "primary_national_approval", null);

				primaryInsuranceApproval = primaryInsuranceApprovalStr != null ? new BigDecimal(primaryInsuranceApprovalStr) : null;
			}

			// Get Secondary Sponsor Details
			if (secondarySponsor != null) {
				if (secondarySponsor.equals("I")) {

					if (noOfPatientPlanBeans.size() > 1)
						secondaryInsBean = noOfPatientPlanBeans.get(1);
					else
						secondaryInsBean = noOfPatientPlanBeans.get(0);
					secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_insurance_approval", null);
					usePerdiem = (request.getParameter("secondary_use_perdiem") != null
							&& !request.getParameter("secondary_use_perdiem").equals("")) ? request.getParameter("secondary_use_perdiem") : usePerdiem;

				}else if (secondarySponsor.equals("C"))
					secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_corporate_approval", null);

				else if (secondarySponsor.equals("N"))
					secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_national_approval", null);

				secondaryInsuranceApproval = secondaryInsuranceApprovalStr != null ? new BigDecimal(secondaryInsuranceApprovalStr) : null;
			}


			setPlanDetailsForFollowupPatientPlan(request, primaryInsuranceBean, secondaryInsBean);

			VisitDetailsDAO visitDAO = new VisitDetailsDAO();
			BasicDynaBean visitDetailsBean = visitDAO.findByKey("patient_id", visitId);
			boolean success = false;
			List<BasicDynaBean> tpaBills = null;

			BigDecimal existingPrimaryInsuranceApproval =
				visitDetailsBean.get("primary_insurance_approval") != null
				? (BigDecimal)visitDetailsBean.get("primary_insurance_approval") : null;
			BigDecimal existingSecondaryInsuranceApproval =
				visitDetailsBean.get("secondary_insurance_approval") != null
				? (BigDecimal)visitDetailsBean.get("secondary_insurance_approval") : null;

			if(insDetailsEdited.equalsIgnoreCase("true")){
					patPrimaryInsuranceDetailsBeanList=getPrimaryInsuranceDetails(request,(String)visitDetailsBean.get("patient_id"),(String)visitDetailsBean.get("visit_type"));
					patSecondaryInsuranceDetailsBeanList=getSecondaryInsuranceDetails(request,(String)visitDetailsBean.get("patient_id"),(String)visitDetailsBean.get("visit_type"));
			}

			if(usePerdiem!= null && usePerdiem.equals(""))
				visitDetailsBean.set("use_perdiem", usePerdiem);
			if(secUsePerdiem!= null && secUsePerdiem.equals(""))
				visitDetailsBean.set("use_perdiem", secUsePerdiem);

			int visitTpaBillsCnt = BillDAO.getVisitTpaBillsCountExcludePrimary(visitId);

			if (visitDetailsBean.get("use_perdiem").equals("Y") && visitTpaBillsCnt != 0) {
				visitDetailsBean.set("use_perdiem", "N");
			}else {
				visitDetailsBean.set("use_perdiem", usePerdiem);
			}

			do {
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				visitDetailsBean.set("primary_insurance_approval", primaryInsuranceApproval);
				visitDetailsBean.set("secondary_insurance_approval", secondaryInsuranceApproval);

				success = (visitDAO.updateWithName(con, visitDetailsBean.getMap(), "patient_id")) > 0;

				if (primaryInsuranceBean != null)
					success &= patientInsPlanDAO.updateWithName(con, primaryInsuranceBean.getMap(), "patient_insurance_plans_id") > 0;

				if (secondaryInsBean != null)
					success &= patientInsPlanDAO.updateWithName(con, secondaryInsBean.getMap(), "patient_insurance_plans_id") > 0;

				if(null != patPrimaryInsuranceDetailsBeanList && patPrimaryInsuranceDetailsBeanList.size()>0)
						insertORUpdatePatientInsuranceDetails(con,patPrimaryInsuranceDetailsBeanList);

				if(null != patSecondaryInsuranceDetailsBeanList && patSecondaryInsuranceDetailsBeanList.size()>0)
					   insertORUpdatePatientInsuranceDetails(con,patSecondaryInsuranceDetailsBeanList);

				tpaBills = editInsHelper.getVisitTPABills(visitId, billsRequired);

				if (success && ((existingPrimaryInsuranceApproval == null || primaryInsuranceApproval == null
						|| existingPrimaryInsuranceApproval.compareTo(primaryInsuranceApproval) <= 0)
					||	(existingSecondaryInsuranceApproval == null || secondaryInsuranceApproval == null
						|| existingSecondaryInsuranceApproval.compareTo(secondaryInsuranceApproval) <= 0))) {


					if (tpaBills != null && tpaBills.size() > 0) {

						for (BasicDynaBean b : tpaBills) {
							String bill_no = (String)b.get("bill_no");
							BasicDynaBean billbean = billDAO.findByKey("bill_no", bill_no);
							billbean.set("primary_approval_amount", null);
							billbean.set("secondary_approval_amount", null);

							success &= (billDAO.updateWithName(con, billbean.getMap(), "bill_no")) > 0;
						}
					}
				}
			} while (false);

			DataBaseUtil.commitClose(con, success);

			editInsHelper.updateSponsorClaimTotals(tpaBills);

			sponsorBO.recalculateSponsorAmount(visitId);

			ActionRedirect redirect = new ActionRedirect(mapping.findForward("changeTpaRedirect"));
			FlashScope flash = FlashScope.getScope(request);
			if (success) {

				if (usePerdiem.equals("Y") && visitTpaBillsCnt != 0) {

					flash.error("Perdiem Code cannot be added. <br/> "
								+" This visit has "+visitTpaBillsCnt+ " bill(s) which are connected to TPA. <br/>"
								+" Please disconnect them from TPA. <br/> "
								+" Connect TPA to the primary bill later bill and use perdiem.");
				}else {
					flash.info("Updated Visit approval amounts. Sponsor claim amounts are recalculated. <br/>");
				}

			}else
				flash.error("Failed to update approval amounts for Visit.");

			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("visitId", visitId);
			redirect.addParameter("billNo", billNo);
			return redirect;

		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
	}

	private void insertORUpdatePatientInsuranceDetails(Connection con, List<BasicDynaBean> insuranceDetailsBeanList) throws SQLException, IOException {

		List<BasicDynaBean> planDetailsList = new ArrayList<BasicDynaBean>();
		Map filterMap = new HashMap();
		int planid = 0;
		String visitid = null;
		
	  if(insuranceDetailsBeanList.size() > 0){
	    BasicDynaBean bean = insuranceDetailsBeanList.get(0);
      planid=(Integer) bean.get("plan_id");
      visitid=(String) bean.get("visit_id");
      filterMap.put("plan_id",(Integer) bean.get("plan_id"));
      filterMap.put("visit_id", (String) bean.get("visit_id"));
	  }
			
			planDetailsList = patientInsurancePlanDetails.listAll(null, filterMap,"plan_id");

			if(planDetailsList.size()>0){
				patientInsurancePlanDetails.delete(con, "visit_id", visitid, "plan_id", planid);
				patientInsurancePlanDetails.insertAll(con, insuranceDetailsBeanList);
			}else{
				patientInsurancePlanDetails.insertAll(con, insuranceDetailsBeanList);
			}

	}

	/****************************  Edit MembershipID screen actions    ******************************/

	public ActionForward editMemberId(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		String visitId = request.getParameter("patient_id");
		String patientPolicyIDStr = request.getParameter("patient_policy_id");
		Integer patientPolicyID = (null != patientPolicyIDStr && !patientPolicyIDStr.equals("")) ? Integer.parseInt(patientPolicyIDStr) : 0;
		Map visitbean = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);
		BasicDynaBean patientPlanDetails = null;
		if (visitbean == null) {
			ActionRedirect redirect = new ActionRedirect("editMemberId.do");
			FlashScope flash = FlashScope.getScope(request);
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("_method", "editMemberId");
			flash.error("No Patient with Id:" + visitId);
			return redirect;
		}

		int patient_policy_id = (visitbean.get("patient_policy_id") != null && (Integer)visitbean.get("patient_policy_id") > 0) ?
					(Integer) visitbean.get("patient_policy_id") : patientPolicyID;

		if (VisitDetailsDAO.isVisitActive(visitId)) {
			request.setAttribute("visit_active", true);
		} else {
			request.setAttribute("visit_active", false);
		}

		// Patient has policy
		if (patient_policy_id > 0) {

			boolean policy_validity_expired = false;
			patientPlanDetails = patientInsPlanDAO.patientPlanDetails(patient_policy_id);
			if (patientPlanDetails != null) {
				Date policyEndDate = (Date) patientPlanDetails.get("policy_validity_end");
				long dateDiff = 0L;

				if (null != policyEndDate) {
					dateDiff = DateUtil.getCurrentDate().getTime() - policyEndDate.getTime();
				}
				policy_validity_expired = dateDiff > 0;
			}
			request.setAttribute("policy_active", true);
			request.setAttribute("policy_validity_expired", policy_validity_expired);

			if(null != patientPlanDetails) {
			  if(1 == (Integer)patientPlanDetails.get("priority")){
	        request.setAttribute("tpa_id", visitbean.get("primary_sponsor_id"));
	      }
	      else if(2 == (Integer)patientPlanDetails.get("priority")){
	        request.setAttribute("tpa_id", visitbean.get("secondary_sponsor_id"));
	      }
			}
		} else {
			// Patient has no policy (or mod_adv_ins disabled)
			request.setAttribute("policy_active", true);
			request.setAttribute("policy_validity_expired", false);
		}

		String[] sponsnorTypes = patientInsPlanDAO.getSponsortypes(visitId);
		String priSponsorType = sponsnorTypes[0];
		String secSponsorType = sponsnorTypes[1];

		boolean isPrimaryInsuranceCardAvailable = false;
		if (priSponsorType != null)
			isPrimaryInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientInsCardImageMap(visitId, priSponsorType, patient_policy_id) != null;

		boolean isSecondaryInsuranceCardAvailable = false;
		if (secSponsorType != null)
			isSecondaryInsuranceCardAvailable = PatientDetailsDAO.getCurrentPatientInsSecCardImageMap(visitId, secSponsorType, patient_policy_id) != null;

		request.setAttribute("isPrimaryInsuranceCardAvailable", isPrimaryInsuranceCardAvailable);
		request.setAttribute("isSecondaryInsuranceCardAvailable", isSecondaryInsuranceCardAvailable);

		request.setAttribute("visitId", visitId);
		request.setAttribute("visitbean", visitbean);
		request.setAttribute("planDetails", patientPlanDetails != null ? patientPlanDetails.getMap() : visitbean);

		request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
		request.setAttribute("regPrefJSON", js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
		request.setAttribute("sponsorTypelist",js.serialize(ConversionUtils.listBeanToListMap(sponsorTypeDAO.listAll())));
		request.setAttribute("tpanames", js.serialize(ConversionUtils.listBeanToListMap(TpaMasterDAO.gettpanames())));

		return mapping.findForward("editMemberId");
	}

	public ActionForward saveMemberId(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ActionRedirect redirect = new ActionRedirect("editMemberId.do");
		FlashScope flash = FlashScope.getScope(request);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

		InsuranceDAO idao = new InsuranceDAO();
		String visitId = request.getParameter("patient_id");
		Map visitbean = VisitDetailsDAO.getPatientVisitDetailsMap(visitId);

		String policyHolder = request.getParameter("policy_holder_name");
		String patientRelationship = request.getParameter("patient_relationship");
		String policyNo = request.getParameter("policy_no");
		String policyNumber = request.getParameter("policy_number");
		String patientPolicyIDStr = request.getParameter("patient_policy_id");
		Integer patientPolicyID = (null != patientPolicyIDStr && !patientPolicyIDStr.equals("")) ? Integer.parseInt(patientPolicyIDStr) : 0;

		java.sql.Date fromDate = DataBaseUtil.parseDate(request.getParameter("policy_validity_start"));
		java.sql.Date toDate = DataBaseUtil.parseDate(request.getParameter("policy_validity_end"));

		int patient_policy_id = (visitbean.get("patient_policy_id") != null && (Integer)visitbean.get("patient_policy_id") > 0) ?
						(Integer) visitbean.get("patient_policy_id") : patientPolicyID;
		int insurance_id = visitbean.get("insurance_id") != null ?
						(Integer) visitbean.get("insurance_id")	: 0;

		boolean success = false;
		Connection con = null;

		try {

			con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);

			if (patient_policy_id > 0) {
				BasicDynaBean patientPolicyBean = patientPolicyDetails.getBean();
				patientPolicyBean.set("member_id", policyNo);
				patientPolicyBean.set("policy_number", policyNumber);
				patientPolicyBean.set("policy_holder_name", policyHolder);
				patientPolicyBean.set("patient_relationship", patientRelationship);
				patientPolicyBean.set("policy_validity_start", fromDate);
				patientPolicyBean.set("policy_validity_end", toDate);

				patientPolicyBean.set("patient_policy_id", patient_policy_id);
				success = (patientPolicyDetails.updateWithName(con, patientPolicyBean.getMap(), "patient_policy_id")) > 0;
			} else {

				BasicDynaBean insuranceBean = idao.getBean();
				insuranceBean.set("policy_no", policyNo);
				insuranceBean.set("insurance_no", policyNumber);
				insuranceBean.set("policy_holder_name", policyHolder);
				insuranceBean.set("patient_relationship", patientRelationship);
				insuranceBean.set("policy_validity_start", fromDate);
				insuranceBean.set("policy_validity_end", toDate);

				insuranceBean.set("insurance_id", insurance_id);
				success = (idao.updateWithName(con, insuranceBean.getMap(), "insurance_id")) > 0;
			}

		} finally {
			DataBaseUtil.commitClose(con, true);
		}

		if (!success) {
			flash.error("Member Id details updation unsuccessful...");
		}

		redirect.addParameter("_method", "editMemberId");
		redirect.addParameter("patient_id", visitId);
		redirect.addParameter("patient_policy_id", patientPolicyID);
		return redirect;
	}

	private BasicDynaBean getPrimarySponsorDetails(HttpServletRequest request, BasicDynaBean primarySponsorBean) throws SQLException{

		String primarySponsorId = null;
		String primaryInsCompId = null;
		BigDecimal primaryInsuranceApproval = null;
		String priorAuthId = null;
		int priorAuthModeId = 0;
		String primaryInsuranceApprovalStr = null;
		Integer planId = null;
		Integer categoryId = null;
		String useDRG = "N";
		String usePerdiem = "N";
		Integer primary_policy_id = 0;
		String visitType= request.getParameter("visitType");

		primarySponsorId = request.getParameter("primary_sponsor_id");
		primaryInsCompId = getParamDefault(request, "primary_insurance_co", null);

		primaryInsuranceApprovalStr = getParamDefault(request, "primary_insurance_approval", null);

		planId = request.getParameter("primary_plan_id")==null || request.getParameter("primary_plan_id").equals("") ? null
				 : Integer.parseInt(request.getParameter("primary_plan_id"));
		categoryId = request.getParameter("primary_plan_type")==null || request.getParameter("primary_plan_type").equals("") ? null
		 			: Integer.parseInt(request.getParameter("primary_plan_type"));

		useDRG = (request.getParameter("primary_use_drg") != null
				 && !request.getParameter("primary_use_drg").equals("")) ? request.getParameter("primary_use_drg") : useDRG;

		usePerdiem = (request.getParameter("primary_use_perdiem") != null
						 && !request.getParameter("primary_use_perdiem").equals("")) ? request.getParameter("primary_use_perdiem") : usePerdiem;

		primary_policy_id = (request.getParameter("primary_policy_id") != null
						 && !request.getParameter("primary_policy_id").equals("")) ? Integer.parseInt(request.getParameter("primary_policy_id")) : 0;

		priorAuthId = request.getParameter("primary_prior_auth_id");
		priorAuthModeId = request.getParameter("primary_prior_auth_mode_id") == null
		 			|| request.getParameter("primary_prior_auth_mode_id").equals("") ? 0
		 						: Integer.parseInt(request.getParameter("primary_prior_auth_mode_id"));
		primaryInsuranceApproval = primaryInsuranceApprovalStr != null ? new BigDecimal(primaryInsuranceApprovalStr) : null;

		String primaryPlanLimitStr = getParamDefault(request, "primary_plan_limit", null);
		BigDecimal primaryPlanLimit = primaryPlanLimitStr != null ? new BigDecimal(primaryPlanLimitStr) : null;

		String primaryVisitLimitStr = getParamDefault(request, "primary_visit_limit", null);
		BigDecimal primaryVisitLimit = primaryVisitLimitStr != null ? new BigDecimal(primaryVisitLimitStr) : null;

		String primaryVisitDeductibleStr = getParamDefault(request, "primary_visit_deductible", null);
		BigDecimal primaryVisitDeductible = primaryVisitDeductibleStr != null ? new BigDecimal(primaryVisitDeductibleStr) : null;

		String primaryVisitCopayStr = getParamDefault(request, "primary_visit_copay", null);
		BigDecimal primaryVisitCopay = primaryVisitCopayStr != null ? new BigDecimal(primaryVisitCopayStr) : null;

		String primaryMaxCopayStr = getParamDefault(request, "primary_max_copay", null);
		BigDecimal primaryMaxCopay = primaryMaxCopayStr != null ? new BigDecimal(primaryMaxCopayStr) : null;

		String primaryPerDayLimitStr = getParamDefault(request, "primary_perday_limit", null);
		BigDecimal primaryPerDayLimit = primaryPerDayLimitStr != null ? new BigDecimal(primaryPerDayLimitStr) : null;
		
		String limit_include_followUp = request.getParameter("primary_limits_include_followUps");

		String primaryUtilizationLimitStr = getParamDefault(request, "primary_plan_utilization", null);
		BigDecimal primaryUtilizationAmount = primaryUtilizationLimitStr != null ? new BigDecimal(primaryUtilizationLimitStr) : null;

		primarySponsorBean.set("insurance_co", primaryInsCompId);
		primarySponsorBean.set("sponsor_id", primarySponsorId);
		primarySponsorBean.set("plan_id", planId);
		primarySponsorBean.set("plan_type_id", categoryId);
		primarySponsorBean.set("insurance_approval", primaryInsuranceApproval);
		primarySponsorBean.set("prior_auth_id", priorAuthId);
		primarySponsorBean.set("prior_auth_mode_id", priorAuthModeId);
		primarySponsorBean.set("use_drg", useDRG);
		primarySponsorBean.set("use_perdiem", usePerdiem);
		primarySponsorBean.set("patient_policy_id", primary_policy_id);
		primarySponsorBean.set("priority", 1);
		primarySponsorBean.set("plan_limit",primaryPlanLimit );
		primarySponsorBean.set("visit_per_day_limit",primaryPerDayLimit);
		primarySponsorBean.set("utilization_amount", primaryUtilizationAmount);
		
    if (null != limit_include_followUp && !limit_include_followUp.equals("")
        && limit_include_followUp.equals("Y") && visitType.equalsIgnoreCase("o")) {
      primarySponsorBean.set("episode_limit", primaryVisitLimit);
      primarySponsorBean.set("episode_deductible", primaryVisitDeductible);
      primarySponsorBean.set("episode_copay_percentage", primaryVisitCopay);
      primarySponsorBean.set("episode_max_copay_percentage", primaryMaxCopay);
    } else {
      primarySponsorBean.set("visit_limit", primaryVisitLimit);
      primarySponsorBean.set("visit_deductible", primaryVisitDeductible);
      primarySponsorBean.set("visit_copay_percentage", primaryVisitCopay);
      primarySponsorBean.set("visit_max_copay_percentage", primaryMaxCopay);
    }
		return primarySponsorBean;
	}


	private BasicDynaBean getSecondarySponsorDetails(HttpServletRequest request, BasicDynaBean secondarySponsorBean) throws SQLException{

		String secondarySponsorId = null;
		String secondaryInsCompId = null;
		BigDecimal secondaryInsuranceApproval = null;
		String priorAuthId = null;
		int priorAuthModeId = 0;
		String secondaryInsuranceApprovalStr = null;
		Integer planId = null;
		Integer categoryId = null;
		String useDRG = "N";
		String usePerdiem = "N";
		Integer secondary_policy_id = 0;
		String visitType= request.getParameter("visitType");

		secondarySponsorId = request.getParameter("secondary_sponsor_id");
		secondaryInsCompId = getParamDefault(request, "secondary_insurance_co", null);

		secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_insurance_approval", null);

		planId = request.getParameter("secondary_plan_id")==null || request.getParameter("secondary_plan_id").equals("") ? null
				: Integer.parseInt(request.getParameter("secondary_plan_id"));

		categoryId = request.getParameter("secondary_plan_type")==null || request.getParameter("secondary_plan_type").equals("") ? null
				: Integer.parseInt(request.getParameter("secondary_plan_type"));

		useDRG = (request.getParameter("secondary_use_drg") != null
				&& !request.getParameter("secondary_use_drg").equals("")) ? request.getParameter("secondary_use_drg") : useDRG;

		usePerdiem = (request.getParameter("secondary_use_perdiem") != null
				&& !request.getParameter("secondary_use_perdiem").equals("")) ? request.getParameter("secondary_use_perdiem") : usePerdiem;

		secondary_policy_id = (request.getParameter("secondary_policy_id") != null
				&& !request.getParameter("secondary_policy_id").equals("")) ? Integer.parseInt(request.getParameter("secondary_policy_id")) : 0;


		priorAuthId = request.getParameter("secondary_prior_auth_id");
		priorAuthModeId = request.getParameter("secondary_prior_auth_mode_id") == null
					|| request.getParameter("secondary_prior_auth_mode_id").equals("") ? 0
					: Integer.parseInt(request.getParameter("secondary_prior_auth_mode_id"));


		secondaryInsuranceApproval = secondaryInsuranceApprovalStr != null ? new BigDecimal(secondaryInsuranceApprovalStr) : null;
		String secondaryPlanLimitStr = getParamDefault(request, "secondary_plan_limit", null);
		BigDecimal secondaryPlanLimit = secondaryPlanLimitStr != null ? new BigDecimal(secondaryPlanLimitStr) : null;

		String secondaryVisitLimitStr = getParamDefault(request, "secondary_visit_limit", null);
		BigDecimal secondaryVisitLimit = secondaryVisitLimitStr != null ? new BigDecimal(secondaryVisitLimitStr) : null;

		String secondaryVisitDeductibleStr = getParamDefault(request, "secondary_visit_deductible", null);
		BigDecimal secondaryVisitDeductible = secondaryVisitDeductibleStr != null ? new BigDecimal(secondaryVisitDeductibleStr) : null;

		String secondaryVisitCopayStr = getParamDefault(request, "secondary_visit_copay", null);
		BigDecimal secondaryVisitCopay = secondaryVisitCopayStr != null ? new BigDecimal(secondaryVisitCopayStr) : null;

		String secondaryMaxCopayStr = getParamDefault(request, "secondary_max_copay", null);
		BigDecimal secondaryMaxCopay = secondaryMaxCopayStr != null ? new BigDecimal(secondaryMaxCopayStr) : null;

		String secondaryPerDayLimitStr = getParamDefault(request, "secondary_perday_limit", null);
		BigDecimal secondaryPerDayLimit = secondaryPerDayLimitStr != null ? new BigDecimal(secondaryPerDayLimitStr) : null;
		
		String limit_include_followUp = request.getParameter("secondary_limits_include_followUps");

		String secondaryUtilizationLimitStr = getParamDefault(request, "secondary_plan_utilization", null);
		BigDecimal secondaryUtilizationAmount = secondaryUtilizationLimitStr != null ? new BigDecimal(secondaryUtilizationLimitStr) : null;

		secondarySponsorBean.set("insurance_co", secondaryInsCompId);
		secondarySponsorBean.set("sponsor_id", secondarySponsorId);
		secondarySponsorBean.set("plan_id", planId);
		secondarySponsorBean.set("plan_type_id", categoryId);
		secondarySponsorBean.set("insurance_approval", secondaryInsuranceApproval);
		secondarySponsorBean.set("prior_auth_id", priorAuthId);
		secondarySponsorBean.set("prior_auth_mode_id", priorAuthModeId);
		secondarySponsorBean.set("use_drg", useDRG);
		secondarySponsorBean.set("use_perdiem", usePerdiem);
		secondarySponsorBean.set("patient_policy_id", secondary_policy_id);
		secondarySponsorBean.set("priority", 2);
		secondarySponsorBean.set("plan_limit",secondaryPlanLimit );
		secondarySponsorBean.set("visit_per_day_limit",secondaryPerDayLimit);
		secondarySponsorBean.set("utilization_amount",secondaryUtilizationAmount);
		
    if (null != limit_include_followUp && !limit_include_followUp.equals("")
        && limit_include_followUp.equals("Y") && visitType.equalsIgnoreCase("o")) {
      secondarySponsorBean.set("episode_limit", secondaryVisitLimit);
      secondarySponsorBean.set("episode_deductible", secondaryVisitDeductible);
      secondarySponsorBean.set("episode_copay_percentage", secondaryVisitCopay);
      secondarySponsorBean.set("episode_max_copay_percentage", secondaryMaxCopay);
    } else {
      secondarySponsorBean.set("visit_limit", secondaryVisitLimit);
      secondarySponsorBean.set("visit_deductible", secondaryVisitDeductible);
      secondarySponsorBean.set("visit_copay_percentage", secondaryVisitCopay);
      secondarySponsorBean.set("visit_max_copay_percentage", secondaryMaxCopay);
    }
		return secondarySponsorBean;

	}

	private void setPlanDetailsForFollowupPatientPlan(HttpServletRequest request, BasicDynaBean primaryInsuranceBean, BasicDynaBean secondaryInsBean)throws SQLException {

		String visitId = request.getParameter("visitId");
		String primarySponsor = request.getParameter("primary_sponsor");
		String secondarySponsor = request.getParameter("secondary_sponsor");

		String primaryInsuranceApprovalStr = null;
		String secondaryInsuranceApprovalStr = null;

		String usePerdiem = "N";
		String secUsePerdiem = "N";
		BigDecimal primaryInsuranceApproval = null;
		BigDecimal secondaryInsuranceApproval = null;
		int visitTpaBillsCnt = BillDAO.getVisitTpaBillsCountExcludePrimary(visitId);

		if (primarySponsor != null && primarySponsor.equals("I")) {

			primaryInsuranceApprovalStr = getParamDefault(request, "primary_insurance_approval", null);
			usePerdiem = (request.getParameter("primary_use_perdiem") != null
					&& !request.getParameter("primary_use_perdiem").equals("")) ? request.getParameter("primary_use_perdiem") : usePerdiem;
			primaryInsuranceApproval = primaryInsuranceApprovalStr != null ? new BigDecimal(primaryInsuranceApprovalStr) : null;

			if(usePerdiem != null && usePerdiem.equals(""))
				primaryInsuranceBean.set("use_perdiem", usePerdiem);

			if (primaryInsuranceBean.get("use_perdiem").equals("Y") && visitTpaBillsCnt != 0) {
				primaryInsuranceBean.set("use_perdiem", "N");
			}else {
				primaryInsuranceBean.set("use_perdiem", usePerdiem);
			}

			primaryInsuranceBean.set("insurance_approval", primaryInsuranceApproval);
			String primaryPlanLimitStr = getParamDefault(request, "primary_plan_limit", null);
			BigDecimal primaryPlanLimit = primaryPlanLimitStr != null ? new BigDecimal(primaryPlanLimitStr) : null;

			String primaryVisitLimitStr = getParamDefault(request, "primary_visit_limit", null);
			BigDecimal primaryVisitLimit = primaryVisitLimitStr != null ? new BigDecimal(primaryVisitLimitStr) : null;

			String primaryVisitDeductibleStr = getParamDefault(request, "primary_visit_deductible", null);
			BigDecimal primaryVisitDeductible = primaryVisitDeductibleStr != null ? new BigDecimal(primaryVisitDeductibleStr) : null;

			String primaryVisitCopayStr = getParamDefault(request, "primary_visit_copay", null);
			BigDecimal primaryVisitCopay = primaryVisitCopayStr != null ? new BigDecimal(primaryVisitCopayStr) : null;

			String primaryMaxCopayStr = getParamDefault(request, "primary_max_copay", null);
			BigDecimal primaryMaxCopay = primaryMaxCopayStr != null ? new BigDecimal(primaryMaxCopayStr) : null;

			String primaryPerDayLimitStr = getParamDefault(request, "primary_perday_limit", null);
			BigDecimal primaryPerDayLimit = primaryPerDayLimitStr != null ? new BigDecimal(primaryPerDayLimitStr) : null;
			
			String limit_include_followUp = request.getParameter("primary_limits_include_followUps");

			String primaryUtilizationLimitStr = getParamDefault(request, "primary_plan_utilization", null);
			BigDecimal primaryUtilizationAmount = primaryUtilizationLimitStr != null ? new BigDecimal(primaryUtilizationLimitStr) : null;


			primaryInsuranceBean.set("plan_limit",primaryPlanLimit );
			primaryInsuranceBean.set("visit_per_day_limit",primaryPerDayLimit);
			primaryInsuranceBean.set("utilization_amount", primaryUtilizationAmount);
      if (null != limit_include_followUp && !limit_include_followUp.equals("")
          && limit_include_followUp.equals("Y")) {
        primaryInsuranceBean.set("episode_limit", primaryVisitLimit);
        primaryInsuranceBean.set("episode_deductible", primaryVisitDeductible);
        primaryInsuranceBean.set("episode_copay_percentage", primaryVisitCopay);
        primaryInsuranceBean.set("episode_max_copay_percentage", primaryMaxCopay);
      } else {
        primaryInsuranceBean.set("visit_limit", primaryVisitLimit);
        primaryInsuranceBean.set("visit_deductible", primaryVisitDeductible);
        primaryInsuranceBean.set("visit_copay_percentage", primaryVisitCopay);
        primaryInsuranceBean.set("visit_max_copay_percentage", primaryMaxCopay);
      }
		}

		if (secondarySponsor != null && secondarySponsor.equals("I")) {

			secondaryInsuranceApprovalStr = getParamDefault(request, "secondary_insurance_approval", null);
			secUsePerdiem = (request.getParameter("secondary_use_perdiem") != null
					&& !request.getParameter("secondary_use_perdiem").equals("")) ? request.getParameter("secondary_use_perdiem") : secUsePerdiem;
			secondaryInsuranceApproval = secondaryInsuranceApprovalStr != null ? new BigDecimal(secondaryInsuranceApprovalStr) : null;

			if(secUsePerdiem!= null && secUsePerdiem.equals(""))
				secondaryInsBean.set("use_perdiem", secUsePerdiem);

			if (secondaryInsBean.get("use_perdiem").equals("Y") && visitTpaBillsCnt != 0) {
				secondaryInsBean.set("use_perdiem", "N");
			}else {
				secondaryInsBean.set("use_perdiem", usePerdiem);
			}

			secondaryInsBean.set("insurance_approval", secondaryInsuranceApproval);
			String secondaryPlanLimitStr = getParamDefault(request, "secondary_plan_limit", null);
			BigDecimal secondaryPlanLimit = secondaryPlanLimitStr != null ? new BigDecimal(secondaryPlanLimitStr) : null;

			String secondaryVisitLimitStr = getParamDefault(request, "secondary_visit_limit", null);
			BigDecimal secondaryVisitLimit = secondaryVisitLimitStr != null ? new BigDecimal(secondaryVisitLimitStr) : null;

			String secondaryVisitDeductibleStr = getParamDefault(request, "secondary_visit_deductible", null);
			BigDecimal secondaryVisitDeductible = secondaryVisitDeductibleStr != null ? new BigDecimal(secondaryVisitDeductibleStr) : null;

			String secondaryVisitCopayStr = getParamDefault(request, "secondary_visit_copay", null);
			BigDecimal secondaryVisitCopay = secondaryVisitCopayStr != null ? new BigDecimal(secondaryVisitCopayStr) : null;

			String secondaryMaxCopayStr = getParamDefault(request, "secondary_max_copay", null);
			BigDecimal secondaryMaxCopay = secondaryMaxCopayStr != null ? new BigDecimal(secondaryMaxCopayStr) : null;

			String secondaryPerDayLimitStr = getParamDefault(request, "secondary_perday_limit", null);
			BigDecimal secondaryPerDayLimit = secondaryPerDayLimitStr != null ? new BigDecimal(secondaryPerDayLimitStr) : null;
			
			String limit_include_followUp = request.getParameter("secondary_limits_include_followUps");

			String secondaryUtilizationLimitStr = getParamDefault(request, "secondary_plan_utilization", null);
			BigDecimal secondaryUtilizationAmount = secondaryUtilizationLimitStr != null ? new BigDecimal(secondaryUtilizationLimitStr) : null;

			secondaryInsBean.set("plan_limit",secondaryPlanLimit );
			secondaryInsBean.set("visit_per_day_limit",secondaryPerDayLimit);
			secondaryInsBean.set("utilization_amount",secondaryUtilizationAmount);
      if (null != limit_include_followUp && !limit_include_followUp.equals("")
          && limit_include_followUp.equals("Y")) {
        secondaryInsBean.set("episode_limit", secondaryVisitLimit);
        secondaryInsBean.set("episode_deductible", secondaryVisitDeductible);
        secondaryInsBean.set("episode_copay_percentage", secondaryVisitCopay);
        secondaryInsBean.set("episode_max_copay_percentage", secondaryMaxCopay);
      } else {
        secondaryInsBean.set("visit_limit", secondaryVisitLimit);
        secondaryInsBean.set("visit_deductible", secondaryVisitDeductible);
        secondaryInsBean.set("visit_copay_percentage", secondaryVisitCopay);
        secondaryInsBean.set("visit_max_copay_percentage", secondaryMaxCopay);
      }
		}
	}

	@IgnoreConfidentialFilters
	public ActionForward getviewInsuDocument(ActionMapping m, ActionForm f,
			HttpServletRequest req, HttpServletResponse res) throws Exception {

		String id = req.getParameter("inscoid");
		String fileName = "";
		String contentType = "";

		if (id == null) {
			return m.findForward("error");
		}

		Map<String,Object> uploadMap = InsuCompMasterDAO.getUploadedDocInfo(id);

		if (uploadMap.isEmpty()) {
			return m.findForward("error");
		}

		fileName = (String)uploadMap.get("filename");
		contentType = (String)uploadMap.get("contenttype");
		res.setContentType(contentType);

		if (!fileName.equals("")) {
			res.setHeader("Content-disposition", "attachment; filename=\""+fileName+"\"");
		}

		OutputStream os = res.getOutputStream();

		InputStream is = (InputStream)uploadMap.get("uploadfile");
		if (is != null) {
			byte[] bytes = new byte[4096];
			int len = 0;
			while ( (len = is.read(bytes)) > 0) {
				os.write(bytes, 0, len);
			}
		}

		os.flush();
		if(null != is){
		  is.close();
		}
		return null;
	}

	private List<BasicDynaBean> getPrimaryInsuranceDetails(HttpServletRequest request,String patientID, String visitType) throws SQLException{

	 	ArrayList <BasicDynaBean> priInsDeatils =  new ArrayList<BasicDynaBean>();
		String[] categoryNames = request.getParameterValues("P_cat_name");
		String[] categoryIds = request.getParameterValues("P_cat_id");
		String[] sponserLimits = request.getParameterValues("P_sponser_limit");
		String[] catDeducts = request.getParameterValues("P_cat_deductible");
		String[] itemDeducts = request.getParameterValues("P_item_deductible");
		String[] copayPercent = request.getParameterValues("P_copay_percent");
		String[] maxCopayPercent = request.getParameterValues("P_max_copay");

		BasicDynaBean primaryInsSponsorBean;

		Integer primarySponsorId = null;
		Integer insurance_category_id = null;
		BigDecimal patient_amount = null;
		BigDecimal patient_percent = null;
		BigDecimal patient_amount_cap = null;
		BigDecimal per_treatment_limit = null;
		BigDecimal patient_amount_per_category = null;
		String patient_type = visitType;

		if (categoryNames != null && categoryNames.length > 0) {
			for (int j = 0; j < categoryNames.length; j++) {
				primaryInsSponsorBean = patientInsurancePlanDetails.getBean();
				primarySponsorId = request.getParameter("primary_plan_id")==null || request.getParameter("primary_plan_id").equals("") ? null
						 : Integer.parseInt(request.getParameter("primary_plan_id"));

				insurance_category_id=categoryIds[j]==null || categoryIds[j].equals("") ? new Integer(0)
						 : Integer.parseInt(categoryIds[j]);

				patient_amount=sponserLimits[j]==null || sponserLimits[j].equals("") ? null:
					new BigDecimal(sponserLimits[j]);

				patient_percent = catDeducts[j]==null || catDeducts[j].equals("") ? new BigDecimal(0):
					new BigDecimal(catDeducts[j]);

				patient_amount_cap = itemDeducts[j]==null || itemDeducts[j].equals("") ? new BigDecimal(0):
					new BigDecimal(itemDeducts[j]);

				per_treatment_limit = copayPercent[j]==null || copayPercent[j].equals("") ? new BigDecimal(0):
					new BigDecimal(copayPercent[j]);

				patient_amount_per_category = maxCopayPercent[j]==null || maxCopayPercent[j].equals("") ? null:
					new BigDecimal(maxCopayPercent[j]);

				primaryInsSponsorBean.set("visit_id",patientID);
				primaryInsSponsorBean.set("plan_id", primarySponsorId);
				primaryInsSponsorBean.set("insurance_category_id", insurance_category_id);
				primaryInsSponsorBean.set("patient_amount", patient_amount_cap);
				primaryInsSponsorBean.set("patient_percent", per_treatment_limit);
				primaryInsSponsorBean.set("patient_amount_cap", patient_amount_per_category);
				primaryInsSponsorBean.set("per_treatment_limit", patient_amount);
				primaryInsSponsorBean.set("patient_amount_per_category", patient_percent);
				primaryInsSponsorBean.set("patient_type", patient_type);

				priInsDeatils.add(primaryInsSponsorBean);

			}
		}

		return priInsDeatils;
	 }

	private List<BasicDynaBean> getSecondaryInsuranceDetails(HttpServletRequest request,String patientID, String visitType) throws SQLException{

	 	ArrayList <BasicDynaBean> secInsDeatils = new ArrayList<BasicDynaBean>();
		String[] categoryNames = request.getParameterValues("S_cat_name");
		String[] categoryIds = request.getParameterValues("S_cat_id");
		String[] sponserLimits = request.getParameterValues("S_sponser_limit");
		String[] catDeducts = request.getParameterValues("S_cat_deductible");
		String[] itemDeducts = request.getParameterValues("S_item_deductible");
		String[] copayPercent = request.getParameterValues("S_copay_percent");
		String[] maxCopayPercent = request.getParameterValues("S_max_copay");

		BasicDynaBean secondaryInsSponsorBean;

		Integer secondarySponsorId = null;
		Integer insurance_category_id = null;
		BigDecimal patient_amount = null;
		BigDecimal patient_percent = null;
		BigDecimal patient_amount_cap = null;
		BigDecimal per_treatment_limit = null;
		BigDecimal patient_amount_per_category = null;
		String patient_type = visitType;

		if (categoryNames != null && categoryNames.length > 0) {
			for (int j = 0; j < categoryNames.length; j++) {
				secondaryInsSponsorBean = patientInsurancePlanDetails.getBean();
				secondarySponsorId = request.getParameter("secondary_plan_id")==null || request.getParameter("secondary_plan_id").equals("") ? null
						 : Integer.parseInt(request.getParameter("secondary_plan_id"));

				insurance_category_id=request.getParameter("S_cat_id")==null || request.getParameter("S_cat_id").equals("") ? new Integer(0)
						 : Integer.parseInt(request.getParameter("S_cat_id"));

				insurance_category_id=categoryIds[j]==null || categoryIds[j].equals("") ? new Integer(0)
				 : Integer.parseInt(categoryIds[j]);

				patient_amount=sponserLimits[j]==null || sponserLimits[j].equals("") ? null:
					new BigDecimal(sponserLimits[j]);

				patient_percent = catDeducts[j]==null || catDeducts[j].equals("") ? new BigDecimal(0):
					new BigDecimal(catDeducts[j]);

				patient_amount_cap = itemDeducts[j]==null || itemDeducts[j].equals("") ? new BigDecimal(0):
					new BigDecimal(itemDeducts[j]);

				per_treatment_limit = copayPercent[j]==null || copayPercent[j].equals("") ? new BigDecimal(0):
					new BigDecimal(copayPercent[j]);

				patient_amount_per_category = maxCopayPercent[j]==null || maxCopayPercent[j].equals("") ? null:
					new BigDecimal(maxCopayPercent[j]);

				secondaryInsSponsorBean.set("visit_id",patientID);
				secondaryInsSponsorBean.set("plan_id", secondarySponsorId);
				secondaryInsSponsorBean.set("insurance_category_id", insurance_category_id);
				secondaryInsSponsorBean.set("patient_amount", patient_amount_cap);
				secondaryInsSponsorBean.set("patient_percent", per_treatment_limit);
				secondaryInsSponsorBean.set("patient_amount_cap", patient_amount_per_category);
				secondaryInsSponsorBean.set("per_treatment_limit", patient_amount);
				secondaryInsSponsorBean.set("patient_amount_per_category", patient_percent);
				secondaryInsSponsorBean.set("patient_type", patient_type);

				secInsDeatils.add(secondaryInsSponsorBean);

			}
		}

		return secInsDeatils;
	 }
	
	public ActionForward getInsurancePlanDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
    		HttpServletResponse response) throws IOException, ServletException, SQLException {
	 	PlanDetailsDAO planDetailsDao = new PlanDetailsDAO();
	 	String planId = request.getParameter("plan_id");
	 	String visitType = request.getParameter("visitType");
	 	String visitId=request.getParameter("visitId");
        JSONSerializer js = new JSONSerializer().exclude("class");
        response.setContentType("text/plain");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.getWriter().write(js.serialize(ConversionUtils.listBeanToListMap(
        				planDetailsDao.getAllPlanChargesBasedonPatientType(Integer.parseInt(planId),visitType, visitId))));
        response.flushBuffer();
    	return null;
	}

    public ActionForward getInsurancePlanType(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException, SQLException {
            Map responseMap = new HashMap();
            response.setContentType("text/plain");
            response.setHeader("Cache-Control", "no-cache");
            String insCompanyId = request.getParameter("insCompanyId");
            String visitId=request.getParameter("visitId");

            List<DynaBeanMapDecorator> categoryLists = ConversionUtils
                            .listBeanToListMap(InsuranceCategoryMasterDAO.getEditInsCatCenter(visitId,insCompanyId));
            responseMap.put("categoryLists", categoryLists);
            JSONSerializer js = new JSONSerializer().exclude("class");
            response.getWriter().write(js.deepSerialize(responseMap));
            response.flushBuffer();
            return null;
    }

}
