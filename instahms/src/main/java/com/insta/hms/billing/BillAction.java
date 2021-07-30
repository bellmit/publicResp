package com.insta.hms.billing;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.ohsampleregistration.OhSampleRegistrationDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitCaseRateDetailDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDAO;
import com.insta.hms.adminmaster.packagemaster.PackageDocDAO;
import com.insta.hms.billing.ChangeRatePlanBO.ChargeGroup;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.patient.billing.BillMessagingJob;
import com.insta.hms.diagnosticmodule.common.DiagnosticsDAO;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.insurance.DavitaSponsorDAO;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.BillPrintTemplate.BillPrintTemplateDAO;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DiscountAuthorizerMaster.DiscountAuthorizerMasterAction;
import com.insta.hms.master.DiscountPlanMaster.DiscountPlanMasterDAO;
import com.insta.hms.master.DoctorMaster.DoctorMasterDAO;
import com.insta.hms.master.DynaPackage.DynaPackageDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.PatientVisitBillsPrintTemplateAction.PatientVisitBillsPrintTemplateDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PerDiemCodes.PerDiemCodesDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.SponsorProcedureMaster.SponsorProcedureMasterDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.mdm.taxsubgroups.TaxSubGroupRepository;
import com.insta.hms.messaging.InstaIntegrationDao;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.orders.OrderBO;
import com.insta.hms.orders.OrderDAO;
import com.insta.hms.stores.RetailCustomerDAO;
import com.insta.hms.usermanager.Role;
import com.insta.hms.wardactivities.doctorsnotes.DoctorsNotesDAO;
import com.insta.hms.exception.ValidationException;
import com.insta.hms.integration.easyrewards.EasyRewardRequest;
import com.insta.hms.integration.easyrewards.EasyRewardResponse;
import com.insta.hms.integration.easyrewards.EasyRewardService;

import flexjson.JSONSerializer;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

public class BillAction extends DispatchAction {

	final Logger log = LoggerFactory.getLogger(BillAction.class);
	final RetailCustomerDAO rcDao = new RetailCustomerDAO();
	final BillChargeClaimDAO billChargeClaimDao =  new BillChargeClaimDAO();
	final GenericDAO billChargeDao =  new GenericDAO("bill_charge");
	final BillClaimDAO billClaimDao =  new BillClaimDAO();
	final VisitDetailsDAO visitDetailsDao = new VisitDetailsDAO();
	final VisitCaseRateDetailDAO visitCaseRateDetailsDAO = new VisitCaseRateDetailDAO();
	final DrgUpdateDAO drgupdateDao = new DrgUpdateDAO();
	final GenericDAO patientDetailsDao = new GenericDAO("patient_details");
	final GenericDAO messageTypesDao = new GenericDAO("message_types");
	final GenericDAO serviceGroupsDao = new GenericDAO("service_groups");
	final GenericDAO serviceSubGroupsDao = new GenericDAO("service_sub_groups");
	final GenericDAO drgCodesMasterDao = new GenericDAO("drg_codes_master");
	final GenericDAO patientDepositsDao = new GenericDAO("patient_deposits_view");
	final BillChargeTaxDAO billChargeTaxDao = new BillChargeTaxDAO();
	final PlanMasterDAO planMasterDao = new PlanMasterDAO();
	final GenericDAO planDAO = new GenericDAO("insurance_plan_main");
	final PatientInsurancePlanDAO patientInsurancePlanDao = new PatientInsurancePlanDAO();
	final BillBO billBo = new BillBO();
	final GenericDAO multivisitPatientPackageViewDao = new GenericDAO("multivisit_patient_package_view");
	final BillDAO billDao = new BillDAO();
	final PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
	final GenericDAO insuranceClaimDao = new GenericDAO("insurance_claim");
	final DavitaSponsorDAO davitaSponsorDao = new DavitaSponsorDAO();
	final GenericDAO uUserDao = new GenericDAO("u_user");
	final SponsorDAO sponsorDAO = new SponsorDAO();
	final SponsorBO sponsorBO = new SponsorBO();
	final AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
	final GenericDAO depositTotalSetOffs = new GenericDAO("deposit_setoff_total");
	private static final BillChargeTransactionDao billChargeTransactionDao = new BillChargeTransactionDao();
    private static final CenterPreferencesDAO centerPreferencesDao = new CenterPreferencesDAO();

	final GenericDAO issuerefDao =  new GenericDAO("patient_issue_returns_issue_charge_details");
	final GenericDAO patientCustomizedPackageDetailsDAO = new GenericDAO("patient_customised_package_details");
	
	private static final String LOYALTY_CARD_OFFERS = "loyalty_offers";
	private static final String LOYALTY_CARD = "loyalty_card";
	static JobService jobService = JobSchedulingService.getJobService();
	
	/** The Constant ONEAPOLLO_OFFERS. */
	private static final String ONEAPOLLO_OFFERS = "oneapollo_offers";
		
	/** The Constant ONEAPOLLO_CARD. */
	private static final String ONEAPOLLO_CARD = "one_apollo_loyalty_card";

  /** The Constant PERMISSIBLE_DISC_CAP. */
  private static final String PERMISSIBLE_DISC_CAP = "permissible_discount_cap";

  /** The Constant Inventory ChargeHead. */
	private static final String INVENTORY_CHARGEHEAD = "INVITE";

  /** The Constant Package ChargeHead. */
    private static final String PACKAGE_CHARGEHEAD = "PKGPKG";

  /** The Constant Package ChargeGroup. */
  private static final String PACKAGE_CHARGEGROUP = "PKG";

  
  private static final String ALLOW_BILL_FINALIZATION = "allow_bill_fnlz_with_pat_due";
  
  private final AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");

  /** The bill charge tax BO. */
  private static BillChargeTaxBO billChargeTaxBO = new BillChargeTaxBO();

   private final EasyRewardService easyRewardService = (EasyRewardService) ApplicationContextProvider
      .getApplicationContext().getBean("easyRewardService");

  @IgnoreConfidentialFilters
	public ActionForward getCreditBillingCollectScreen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws SQLException, IOException, ParseException {
    try(Connection con = DataBaseUtil.getConnection()){   
		String billNo = request.getParameter("billNo");
		billNo = (billNo != null) ? billNo.trim() : null;
		GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
		HttpSession session = request.getSession();
		Integer roleID = null;
		JSONSerializer js = new JSONSerializer().exclude("class");
		
		List<BasicDynaBean> templateList = BillPrintTemplateDAO.getBillTemplateList();
		request.setAttribute("templateList", templateList);  
    request.setAttribute("templateListJSON", js.serialize(
          ConversionUtils.listBeanToListMap(templateList)));
		request.setAttribute("expenceTemplatesList", new PatientVisitBillsPrintTemplateDAO().
		    listAll(con,Collections.EMPTY_LIST, null, null,null));
		String isNewUX = request.getParameter("isNewUX");
		request.setAttribute("isNewUX", isNewUX);

		Boolean isViewConsolidatedBilling = request.getParameter("screenId") != null
						&& "view_consolidated_bill".equals(request.getParameter("screenId"));

		String userId = (String)session.getAttribute("userid");
    List<String> listColumns = new ArrayList<>();
    listColumns.add(PERMISSIBLE_DISC_CAP);
    listColumns.add(ALLOW_BILL_FINALIZATION);

    Map<String, Object> filtermap = new HashMap<>();
    filtermap.put("emp_username", userId);
    BigDecimal permissibleDiscountPercenatge = BigDecimal.ZERO;
    BasicDynaBean userBean = uUserDao.findByKey(con,listColumns, filtermap);
    if (userBean != null && userBean.get(PERMISSIBLE_DISC_CAP) != null) {
      permissibleDiscountPercenatge = (BigDecimal) userBean.get(PERMISSIBLE_DISC_CAP);
    }
    request.setAttribute("permissibleDiscountPercenatge", permissibleDiscountPercenatge);
    request.setAttribute("allowBillFinalization", userBean.get(ALLOW_BILL_FINALIZATION));
    request.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());
    
		if (null == billNo) {
			request.setAttribute("error", "");
			request.setAttribute("multiPlanExists", false);
			return mapping.findForward("getCreditBillingCollectScreen");
		}

		if (request.getParameter("receiptNo") != null)
			request.setAttribute("receiptNo", request.getParameter("receiptNo"));

		if (request.getParameter("message") != null)
			request.setAttribute("info", request.getParameter("message"));

		BillBO billBOObj = new BillBO();
		BillDetails billDetails = null;
		Bill bill = null;
		int centerId = (Integer) request.getSession(false).getAttribute("centerId");
		if (centerId != 0) {
		  billDetails = billBOObj.getBillDetails(con,billNo, centerId);
		} else {
		  billDetails = billBOObj.getBillDetails(con,billNo);
		}
		if (billDetails != null) {
			bill = billDetails.getBill();
			Boolean isInsurance = bill.getIs_tpa();
			if(isInsurance){
				Map<String, Object> keys = new HashMap<String, Object>();
				keys.put("bill_no", billNo);
				keys.put("priority", 1);
				BasicDynaBean priBillClaimBean = billClaimDao.findByKey(con,keys);
				keys.put("priority", 2);
				BasicDynaBean secBillClaimBean = billClaimDao.findByKey(con,keys);
				
				String priClaimId = null != priBillClaimBean ? (String) priBillClaimBean.get("claim_id"): null;
				String secClaimId = null != secBillClaimBean ? (String) secBillClaimBean.get("claim_id") : null;
			
				BasicDynaBean priSponsorBean = null != priClaimId ? billChargeClaimDao.getSponsorBean(con,priClaimId) : null;
				BasicDynaBean secSponsorBean = null != secClaimId  ? billChargeClaimDao.getSponsorBean(con,secClaimId) : null;
				BigDecimal pri_sponsor_amt =  (null != priSponsorBean) ?  (BigDecimal)priSponsorBean.get("sponsor_amt"): BigDecimal.ZERO;
				BigDecimal sec_sponsor_amt=  (null != secSponsorBean) ? (BigDecimal)secSponsorBean.get("sponsor_amt") : BigDecimal.ZERO;
				
				BigDecimal pri_recd_amt =  (null != priSponsorBean) ?  (BigDecimal)priSponsorBean.get("claim_recd_amt"): BigDecimal.ZERO;
				BigDecimal sec_recd_amt=  (null != secSponsorBean) ? (BigDecimal)secSponsorBean.get("claim_recd_amt") : BigDecimal.ZERO;
				
				BasicDynaBean sponsorsReceipts = billChargeClaimDao.getSponsorsReceipts(con,priClaimId, secClaimId);
				BigDecimal primarySponsorsReceipt=(BigDecimal) sponsorsReceipts.get("pri_sponsor_receipt_amt");
				BigDecimal secondarySponsorsReceipt=(BigDecimal) sponsorsReceipts.get("sec_sponsor_receipt_amt");
				
				request.setAttribute("pri_sponsor_amt",pri_sponsor_amt);
				request.setAttribute("sec_sponsor_amt",sec_sponsor_amt);
				
				request.setAttribute("pri_sponsors_receipt",primarySponsorsReceipt);
				request.setAttribute("sec_sponsors_receipt",secondarySponsorsReceipt);
				
				request.setAttribute("pri_recd_amt", pri_recd_amt);
				request.setAttribute("sec_recd_amt", sec_recd_amt);
				
			}
		} else {
			request.setAttribute("error", "There is no bill with number: " + billNo);
			request.setAttribute("multiPlanExists", false);
			return mapping.findForward("getCreditBillingCollectScreen");
		}

		if (bill.getAppModified() != null && bill.getAppModified().equals("Y")) {
			roleID = (Integer)session.getAttribute("roleId");
			if (roleID != 1 && roleID != 2) {
				return mapping.findForward("userUnauthorized");
			}
		}

        request.setAttribute("availableTemplates", BillPrintTemplateDAO.getAvailableTemplatesList(con,
					bill.getBillType(), bill.getIs_tpa() ? "Y" : "N"));

		BasicDynaBean visitBean = visitDetailsDao.findByKey(con,"patient_id", bill.getVisitId());
		String email = "";
		String phone = "";
		try {
			if (visitBean.get("mr_no") != null) {
				String mrNo = (String) visitBean.get("mr_no");
				
				
				List<String> columns = new ArrayList<>();
				columns.add("email_id");
				columns.add("patient_phone");
				
				Map<String, Object> finalmap = new HashMap<>();
				finalmap.put("mr_no", mrNo);
				
				BasicDynaBean bean = patientDetailsDao.findByKey(con,columns, finalmap);
				if(bean!=null){
					if(bean.get("email_id") != null) {
						email = (String) bean.get("email_id");
					}
					if(bean.get("patient_phone") != null) {
						phone = (String) bean.get("patient_phone");
					}
				}
				if ((null == isNewUX || (null!= isNewUX && !isNewUX.equals("Y"))) && !isViewConsolidatedBilling) {
					String flowType = visitBean.get("visit_type").equals("o") ? "opflow" : "ipflow";
            response.sendRedirect(
                flowType + "/index.htm#/filter/default/patient/" + mrNo + "/billing/billNo/"
                    + URLEncoder.encode(billNo, "UTF-8") + "?retain_route_params=true");
  				}
			}
		} catch (Exception e) {
		}
		BasicDynaBean dynaBean = paymentModeMasterDao.findByKey(con,"mode_id", -3);
		String url = "";
		int storeCode=0;
		if (dynaBean != null && dynaBean.get("status") != null) {
			String status = (String) dynaBean.get("status");
			if (status.equalsIgnoreCase("A")) {
				BasicDynaBean integrationBean = new InstaIntegrationDao()
						.getActiveBean(con, LOYALTY_CARD_OFFERS);
				if (integrationBean != null
						&& integrationBean.get("url") != null && integrationBean.get("status").equals("A")) {
					url = (String) integrationBean.get("url");
					storeCode=getStoreCode(con,LOYALTY_CARD);
				}
			}
		}
		
		BasicDynaBean oneApolloPaymentBean = paymentModeMasterDao.findByKey(con, "mode_id", -5);
		
		if (oneApolloPaymentBean != null && oneApolloPaymentBean.get("status") != null) {
			String status = (String) oneApolloPaymentBean.get("status");
			if (status.equalsIgnoreCase("A")) {
				BasicDynaBean integrationBean = new InstaIntegrationDao()
						.getActiveBean(con, ONEAPOLLO_OFFERS);
				if (integrationBean != null
						&& integrationBean.get("url") != null && integrationBean.get("status").equals("A")) {
					url = (String) integrationBean.get("url");
					storeCode=getStoreCode(con,ONEAPOLLO_CARD);
				}
			}
		}
		request.setAttribute("patientPhone",phone);
		request.setAttribute("patientEmail",email);
		request.setAttribute("loyaltyOfferURL",url);
		request.setAttribute("storeCode",storeCode);
		String manualEmailStatus = "I";
		String patientDueSMSStatus = "I";
		try{
			BasicDynaBean messageType = messageTypesDao.findByKey(con, "message_type_id","email_manual_op_bn_cash_bill" );
			manualEmailStatus=(String)messageType.get("status");
			
			BasicDynaBean patientDueMessageType = messageTypesDao.findByKey(con, "message_type_id","sms_patient_due_for_visit" );
			patientDueSMSStatus=(String)patientDueMessageType.get("status");
		}catch(Exception e){
        }
		request.setAttribute("manualEmailStatus",manualEmailStatus);
		request.setAttribute("patientDueSMSStatus",patientDueSMSStatus);
		List<BasicDynaBean> planListBean = billClaimDao.listAll(con, null, "bill_no", billNo, "priority");

		int planIds[] = null;
		int planId = 0;
		if (visitBean != null) {
			planId = (Integer) visitBean.get("plan_id");
		}
		planIds = null!= planListBean && planListBean.size() > 0 ? new int[planListBean.size()] : null;

		for(int i=0; i<planListBean.size(); i++){
			planIds[i] = (Integer)planListBean.get(i).get("plan_id");
		}
		boolean multiPlanExists = null != planListBean && planListBean.size() == 2;

		request.setAttribute("multiPlanExists", multiPlanExists);
		request.setAttribute("planList", js.serialize(ConversionUtils.listBeanToListMap(planListBean)));
		BasicDynaBean caseRateDetails = null;
		if (visitBean != null && (null != visitBean.get("primary_case_rate_id") 
		    || null != visitBean.get("secondary_case_rate_id"))) {
		  caseRateDetails  = visitDetailsDao.getVisitCaseRateDetials(con, bill.getVisitId());
		}
		request.setAttribute("caseRateDetails", null != caseRateDetails ? caseRateDetails.getMap() : null);

		if(null != planIds && planIds.length > 0) {
			List policyCharges = ConversionUtils.listBeanToListMap(new PlanDetailsDAO().getAllPlanCharges(con, planIds[0]));
			request.setAttribute("policyCharges", js.serialize(policyCharges));
		}
		else {
			request.setAttribute("policyCharges", js.serialize(new ArrayList()));
		}

		request.setAttribute("billDetails", billDetails);
		request.setAttribute("visitTotalPatientDue", BillDAO.getVisitPatientDue(con, bill.getVisitId()));
		request.setAttribute("exclVisitPatientDue", BillDAO.getVisitPatientDueByExcludeBill(con, bill.getVisitId(), bill.getBillNo()));
		Map<String, Object> creditLimitDetailsMap = visitDetailsDao.getCreditLimitDetails(con,bill.getVisitId());
		request.setAttribute("creditLimitDetailsMap", creditLimitDetailsMap);
		request.setAttribute("creditLimitDetailsJSON", js.serialize(creditLimitDetailsMap));
		request.setAttribute("otherUnpaidBillsJSON", js.serialize(ConversionUtils.listBeanToListMap
					(BillDAO.getOtherUnpaidBillNos(con, bill.getVisitId(), billNo))));
		request.setAttribute("pendingtest", DiagnosticsDAO.isPendingTestExist(con,bill.getVisitId()));
		request.setAttribute("pendingtestsforbill", js.serialize(
        ConversionUtils.copyListDynaBeansToMap(DiagnosticsDAO.getPendingTestsForBill(con,bill.getBillNo()))));
		request.setAttribute("pendingconsultationforbill", js.serialize(
        ConversionUtils.copyListDynaBeansToMap(BillDAO.getPendingConsultationForBill(con,bill.getBillNo()))));
		request.setAttribute("pendingEquipmentFinalization",
				OrderDAO.isBillEquipmentDetailsFinalized(con, bill.getVisitId(), billNo));
		request.setAttribute("pendingBedFinalization",
				OrderDAO.isBillBedDetailsFinalized(con, bill.getVisitId(), billNo));

		request.setAttribute("serviceGroups",
				serviceGroupsDao.listAll(con, null,"status","A","service_group_name"));
		request.setAttribute("serviceGroupsJSON", js.serialize(
					ConversionUtils.copyListDynaBeansToMap(serviceGroupsDao.listAll(con, null, null, null))));
		request.setAttribute("servicesSubGroupsJSON",
				js.serialize(ConversionUtils.copyListDynaBeansToMap(
				serviceSubGroupsDao.listAll(con, null,"status","A","service_sub_group_name"))));
		request.setAttribute("doctorConsultationTypes",
				js.serialize(ConversionUtils.copyListDynaBeansToMap
				(OrderDAO.getConsultationTypes(con, bill.getVisitType(), true))));
		request.setAttribute("allDoctorConsultationTypes",js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new ConsultationTypesDAO().listAll(con, null, null, null))));
		request.setAttribute("allServiceSubgroupsList",
				js.serialize(ConversionUtils.listBeanToListMap(
				serviceSubGroupsDao.listAll(con, null, null, null))));
		request.setAttribute("regPrefJSON",
				js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
		request.setAttribute("anaeTypesJSON",js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new GenericDAO("anesthesia_type_master").listAll(con, null,"status","A",null))));
		//R.C:change attribute name to CreditTypesJSON which is more meaningful
		request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils.copyListDynaBeansToMap(
				new CardTypeMasterDAO().listAll(con, null,"status","A",null))));
		request.setAttribute("visit_issue_return_references_JSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(
		    issuerefDao.findAllByKey("patient_id",bill.getVisitId()))));
		
		List<BasicDynaBean> taxSubGroups = billChargeTaxDao.getTaxSubGroupsDetails(con);
		request.setAttribute("taxSubGroupsJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(taxSubGroups)));
		request.setAttribute("taxSubGroups", taxSubGroups);

		Boolean multiVisitBill = billBOObj.checkMultiVisitBill(con, billNo, bill.getVisitId());
		request.setAttribute("multiVisitBill", multiVisitBill ? "Y" : "N");
		
		//tax sub groups
		request.setAttribute("itemGroupTypeList", ConversionUtils.listBeanToListMap(new GenericDAO("item_group_type").listAll(null, "status","A")));
		request.setAttribute("itemGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("item_groups").findAllByKey("status","A"))));
		List <BasicDynaBean> itemSubGroupList = new TaxSubGroupRepository().getItemSubGroup(new java.sql.Date(new java.util.Date().getTime()));
		Iterator<BasicDynaBean> itemSubGroupListIterator = itemSubGroupList.iterator();
    List<BasicDynaBean> validateItemSubGrouList = new ArrayList<BasicDynaBean>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String currentDateStr = sdf.format(new java.util.Date());
    while(itemSubGroupListIterator.hasNext()) {
      BasicDynaBean itenSubGroupbean = itemSubGroupListIterator.next();
      if(itenSubGroupbean.get("validity_end") != null){
        Date endDate = (Date)itenSubGroupbean.get("validity_end");
        
        try {
          if(sdf.parse(currentDateStr).getTime() <= endDate.getTime()) {
            validateItemSubGrouList.add(itenSubGroupbean);
          }
        } catch (ParseException e) {
          continue;
        }
      } else {
        validateItemSubGrouList.add(itenSubGroupbean);
      }
    }
    request.setAttribute("itemSubGroupListJson", js.serialize(ConversionUtils.listBeanToListMap(validateItemSubGrouList)));

		//Removed as part of AHLL optimization.
		/*BasicDynaBean patientBean = VisitDetailsDAO.getPatientVisitDetailsBean((String)bill.getVisitId());
		if ( null != patientBean) {
			request.setAttribute("sponserType", patientBean.get("sponsor_type"));
		}*/

		int patCenterId = 0;
		if (bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
			// retail customer, bed type and org are unavailable.
			log.debug("Retail visit: setting details");
			BasicDynaBean retailCustomer = rcDao.getRetailCustomerEx(con, bill.getVisitId());
			if (retailCustomer != null) {
				request.setAttribute("retailCustomer", retailCustomer.getMap());
				patCenterId = (Integer) retailCustomer.get("center_id");
			}
			setBillDetails(request, null, null, con);

		} else if (bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_INCOMING)) {
			BasicDynaBean incomingBean = OhSampleRegistrationDAO.getIncomingCustomer(con, bill.getVisitId());
			request.setAttribute("incomingCustomer", incomingBean);
			if (incomingBean != null) {
				patCenterId = (Integer) incomingBean.get("center_id");
			}
			setBillDetails(request, null, null, con);

		} else {

			log.debug("Visit type for bill " + billNo + ": " + bill.getVisitType());

			Map patientDetails = VisitDetailsDAO.getPatientVisitDetailsMap(con, bill.getVisitId());
			request.setAttribute("patient", patientDetails);

			String orgid = "ORG0001";
			String bedType = "GENERAL";
			List procedureNameList = null;

			if (patientDetails != null) {
				patCenterId = (Integer)patientDetails.get("center_id");

				Map<String, Object> filterMap = new HashMap<String, Object>();
				// In multi center schema theatre must belong to visit center.
				if ( GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() > 1 )
					filterMap.put("center_id", (Integer)patientDetails.get("center_id"));
				filterMap.put("status", "A");

				request.setAttribute("otlist_applicabletovisitcenter",
						new GenericDAO("theatre_master").listAll(con, null, filterMap,"theatre_name"));


				orgid = bill.getBillRatePlanId();

				if(orgid == null || orgid.isEmpty()){
					orgid = (String) patientDetails.get("org_id");
					if (orgid == null) orgid = "ORG0001";
				}

				bedType = (String) patientDetails.get("bill_bed_type");
				if (bedType == null) bedType = "GENERAL";
				String tpaid = (String)patientDetails.get("primary_sponsor_id");
				String sectpaid = (String)patientDetails.get("secondary_sponsor_id");

				if (tpaid != null && !tpaid.equals("")) {
					procedureNameList = SponsorProcedureMasterDAO.getAllTPAProcedureDetails(con,
							(String)patientDetails.get("primary_sponsor_id"),bill.getBillNo());

					BigDecimal billsApprovalTotal = BillDAO.getBillApprovalAmountsTotal(con, bill.getVisitId());
					request.setAttribute("billsApprovalTotal", billsApprovalTotal);
				}
				request.setAttribute("allowBillInsurance", BillDAO.isBillInsuranceAllowed(bill.getBillType(), true));
				Map drgCodeMap = ChargeDAO.getBillDRGCode(con, bill.getBillNo());
				if (drgCodeMap != null && drgCodeMap.get("drg_code") != null && !drgCodeMap.get("drg_code").equals("")) {
					String drgCode = (String)drgCodeMap.get("drg_code");
					request.setAttribute("drgBeanJSON",
						js.serialize((drgCodesMasterDao.findByKey(con, "drg_code", drgCode)).getMap()));
				}

				if(drgCodeMap !=  null && (drgCodeMap.get("drg_code") == null || drgCodeMap.equals(""))){
					drgCodeMap = ChargeDAO.getBillDRGMarginCode(con,billNo);

					if (drgCodeMap != null && drgCodeMap.get("drg_code") != null && !drgCodeMap.get("drg_code").equals("")) {
						String drgCode = (String)drgCodeMap.get("drg_code");
						request.setAttribute("drgBeanJSON",
								js.serialize((drgCodesMasterDao.findByKey(con, "drg_code", drgCode)).getMap()));
					}
				}
				request.setAttribute("drgCodeMap", drgCodeMap);
				
				BasicDynaBean planBean = planMasterDao.findByKey(con,"plan_id", planId);
				request.setAttribute("planBeanJSON",
						js.serialize(planBean != null ? planBean.getMap() : null));

				TpaMasterDAO tpaMaster = new TpaMasterDAO();
				if (tpaid != null && !tpaid.equals("")) {
					request.setAttribute("primarySponsorMap", tpaMaster.findByKey(con,"tpa_id", tpaid).getMap());
				}
				if (sectpaid != null && !sectpaid.equals("")) {
					request.setAttribute("secondarySponsorMap", tpaMaster.findByKey(con,"tpa_id", sectpaid).getMap());
				}

				int visitTpaBills = BillDAO.getVisitTpaBills(con, bill.getVisitId());
				request.setAttribute("visitTpaBills", visitTpaBills);
				Bill firstTpaBill = BillDAO.getFirstTpaBillLaterOrBillNow(con, bill.getVisitId());
				request.setAttribute("firstTpaBill", firstTpaBill);
				request.setAttribute("sponserType", patientDetails.get("sponsor_type"));
			}

			if (procedureNameList != null) {
				request.setAttribute("procedureNameList",
						js.serialize(ConversionUtils.listBeanToListMap(procedureNameList)));
			} else {
				request.setAttribute("procedureNameList", js.serialize(null));
			}
			request.setAttribute("procedureLimitList", procedureNameList);

			DynaPackageDAO dynadao = new DynaPackageDAO();
			List dpl = dynadao.getDynaPackages(con, orgid, bedType, bill.getDynaPkgId());
			request.setAttribute("dynaPkgNameList", dpl);
			request.setAttribute("dynaPkgNameJSON", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(dpl)));

			if (bill.getDynaPkgId() != 0) {
				boolean dynaPkgProcessedBefore = ChargeDAO.isPkgProcessedOldWay(con, bill.getBillNo());
				request.setAttribute("dynaPkgProcessedBefore", dynaPkgProcessedBefore);
			}

			request.setAttribute("perdiemCodesList", ConversionUtils.listBeanToListMap(PerDiemCodesDAO.getPerDiemCodes(con)));
			String visitPerdiemCode = patientDetails.get("per_diem_code") != null ? (String)patientDetails.get("per_diem_code") : null;
			request.setAttribute("perdiemCodeDetailsJSON", new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(
					new PerDiemCodesDAO().getPerdiemCharges(con,orgid, bedType, visitPerdiemCode))));

			request.setAttribute("hasPlanVisitCopayLimit", planMasterDao.hasPlanVisitCopayLimit(con, planId, bill.getVisitType()));
			setBillDetails(request, bedType, orgid, con);
			
			if(null != planIds && planIds.length > 0){
			  BasicDynaBean planBean = planDAO.findByKey(con, "plan_id", planIds[0]);
			  request.setAttribute("perdiemPlanBeanJSON", js.serialize(planBean.getMap()));
			  String limitType = (String)planBean.get("limit_type");
			  request.setAttribute("limitType", limitType);
			  if(limitType.equals("R")){
			    request.setAttribute("caseRateCount", (Integer)planBean.get("case_rate_count"));
			  }
			}
		}

		BasicDynaBean visitPrimPlanDetails = patientInsurancePlanDao.getVisitPrimaryPlan(con, bill.getVisitId());
		int visitDiscountPLanId = (bill.getIs_tpa() && visitPrimPlanDetails != null && visitPrimPlanDetails.get("discount_plan_id") != null
				? (Integer)visitPrimPlanDetails.get("discount_plan_id") : 0);
		List<BasicDynaBean> discCategories = DiscountPlanMasterDAO.getDiscountCategoryNames(con, visitDiscountPLanId);
		request.setAttribute("visitDiscountPlanId", visitDiscountPLanId);
		request.setAttribute("discountCategoriesJSON", js.serialize(ConversionUtils.listBeanToListMap(discCategories)));
		request.setAttribute("discountCategoires", discCategories);

		List<String> doctorsList = DoctorMasterDAO.getDoctorDepartmentsDynaList(con, patCenterId);
		request.setAttribute("doctorsJSON", js.serialize(ConversionUtils.listBeanToListMap(doctorsList)));

		BasicDynaBean printPref = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
		request.setAttribute("pref",printPref);

		List printType = PrintConfigurationsDAO.getPrinterTypes();
		request.setAttribute("printerType", printType);
		
		BasicDynaBean patientDepositBean = null;
		 BasicDynaBean ipDepositBean=null;
		  boolean ipDepositExists;
		
		if(null != visitBean && null != visitBean.get("mr_no")){
			patientDepositBean = patientDepositsDao.findByKey(con, "mr_no", (String)visitBean.get("mr_no"));
		}
		
		if(null != patientDepositBean) {
			//if the Deposit Availability needs to be shown at Center Level
		  if ("E".equals(dto.getEnablePatientDepositAvailability())) {
			  if (multiVisitBill) {
					request.setAttribute("depositDetails", DepositsDAO.getMultiPackageDepositDetails(con,billNo,centerId));
				} else {
					request.setAttribute("depositDetails", DepositsDAO.getBillDepositDetails(billNo, true, bill.getVisitType(),centerId));// uses read only connections
				}
		  } else {
			 if (multiVisitBill) {
				request.setAttribute("depositDetails", DepositsDAO.getMultiPackageDepositDetails(con,billNo));
			} else {
				request.setAttribute("depositDetails", DepositsDAO.getBillDepositDetails(billNo, true, bill.getVisitType()));// uses read only connections
			}
		  }
		//if the Deposit Availability needs to be shown at Center Level
		  if ("E".equals(dto.getEnablePatientDepositAvailability())) {
			ipDepositBean = DepositsDAO.getIPBillDepositDetails(con,billNo,centerId);
			ipDepositExists = ipDepositBean != null;
		  }else {
			 ipDepositBean = DepositsDAO.getIPBillDepositDetails(con,billNo);
			 ipDepositExists = ipDepositBean != null;
		  }
	
			request.setAttribute("ipDepositDetails", ipDepositBean);
			request.setAttribute("ipDepositExists", ipDepositExists);
		}else{
			request.setAttribute("depositDetails", null);
			request.setAttribute("ipDepositDetails", null);
			request.setAttribute("ipDepositExists", false);
		}

		if (multiVisitBill) {
		  BasicDynaBean mvbean = billDao.getMultivisitPatientPackageDetails(billNo, bill.getVisitId());
		  //to check weather it is a customized package
		  BasicDynaBean customPackage = patientCustomizedPackageDetailsDAO
				  .findByKey("patient_package_id", mvbean.get("pat_package_id"));
		  if(customPackage != null) {
			  mvbean.set("package_name", customPackage.get("package_name"));
		  }
		  request.setAttribute("mvpackage", mvbean);
		} else {
		  request.setAttribute("mvpackage", null);
		}

		boolean mod_reward_points = (Boolean)session.getAttribute("mod_reward_points");
		if (mod_reward_points && !bill.getIs_tpa())
			request.setAttribute("rewardPointDetails", RewardPointsDAO.getBillRewardPointsDetails(con, billNo));
		request.setAttribute("billLabelMasterMap", ConversionUtils.listBeanToListMap(new GenericDAO("bill_label_master").
		            listAll(con, null, "status", "A", "bill_label_id")));

		String sponsor_bill_receipt = billBOObj.getSponsorBillOrReceipt(con, billNo);
		if (bill.getClaim_id() == null && sponsor_bill_receipt == null && bill.getClaimRecdAmount() != null &&
				bill.getClaimRecdAmount().compareTo(BigDecimal.ZERO) > 0) {
			request.setAttribute("info","Received Claim amount "+bill.getClaimRecdAmount()+" is set to Zero.\n" +
					" You need to create sponsor receipt (or) \n" +
					" include this bill in a sponsor consolidated claim");
		}
		
		if(bill.getIs_tpa()){
			String billAdjAlertsErrorMsgs = setBillAdjustmentAlerts(con,bill.getVisitId());

			if(!billAdjAlertsErrorMsgs.trim().equals("")){
				request.setAttribute("error", billAdjAlertsErrorMsgs);
				boolean visitAdjExists = billAdjAlertsErrorMsgs.contains("Visit");
				request.setAttribute("visitAdjExists", visitAdjExists);
			}
		}
		BasicDynaBean creditNoteDetails = billDao.getCreditNoteDetails(con,billNo);
		
		if(null != creditNoteDetails){
			request.setAttribute("creditNoteDetails", creditNoteDetails.getMap());
		}

		BasicDynaBean babyDetails = null;
		Boolean isBaby = false;
		if (visitBean != null)
			babyDetails = PatientDetailsDAO.getBabyDateOfBirtsAndSalutationDetails(con,(String) visitBean.get("mr_no"), bill.getVisitId());
		if (babyDetails != null)
			isBaby = true;
		request.setAttribute("sponsor_bill_receipt", sponsor_bill_receipt);
		request.setAttribute("isCustomFieldsExist", BillBO.isMandatoryCustomFieldsExistForPatient(bill.getVisitId()));
		request.setAttribute("is_Baby", isBaby);
		
		Map drgCodeMap = ChargeDAO.getBillDRGCode(con,bill.getBillNo());
		boolean hasDRGCode = false;
		if (drgCodeMap != null && drgCodeMap.get("drg_charge_id") != null) {
			hasDRGCode = true;
		}
		
		if(hasDRGCode) {
			String baseRateplanId = drgupdateDao.getCenterHealthAuthBaseRatePlan(con);
			if (hasDRGCode && baseRateplanId==null) {
				String infoMsg = (String)request.getAttribute("info");
				String drgInfoMsg = "Center Health Authority Base Rate plan is NOT Mapped. DRG Outlier Amount is Calculated on Patient Rate Plan.";
				infoMsg = infoMsg != null ? infoMsg+"<br/>"+drgInfoMsg : drgInfoMsg;
				request.setAttribute("info", infoMsg);
			}
		}
		//On ConnectDisconnectTPA when Dyna Package is there in Bill, alert user to re-process the dyna package.
		String hasDynaPkg = request.getParameter("hasDynaPkg");
		if(null != hasDynaPkg &&  Boolean.valueOf(hasDynaPkg)) {
		  request.setAttribute("info", "Please process the Dyna Package again to recalculate all inclusions/exclusions as defined");
		}
		
		if(request.getParameter("screenId") !=null && "view_consolidated_bill".equals(request.getParameter("screenId"))){
			String mvid = request.getParameter("main_visitId");
			Map<String,Object> keyss = new HashMap<String, Object>();
			keyss.put("main_visit_id", mvid);
			keyss.put("is_consolidated_credit_note", true);
			BasicDynaBean consoBill = new ConsolidatedBillDAO().findByKey(con,keyss);
			request.setAttribute("consolidated_credit_note",false);
			if(consoBill != null){
				request.setAttribute("consolidated_credit_note",(Boolean) consoBill.get("is_consolidated_credit_note"));
			}
			request.setAttribute("consolidated_cn_label",request.getParameter("is_cn"));
			if(mvid !=null){
				BasicDynaBean patReg= visitDetailsDao.findByKey(con,"main_visit_id", mvid);
				if(patReg !=null){
					SimpleDateFormat datetFt = new SimpleDateFormat("dd/MM/yyyy");
					Date regDt = datetFt.parse(datetFt.format(patReg.get("reg_date")));
					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					//set the given date in one of the instance and current date in another
					cal1.setTime(regDt);
					cal2.setTime(new Date());
					//now compare the dates using functions
					request.setAttribute("current_month_bill",false);
					if(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
					    if(cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) {
					        // the date falls in current month
					    	request.setAttribute("current_month_bill",true);
					    }
					}
				}
			}
			return mapping.findForward("consolidtedVisitBillScreen");
		}else{
			return mapping.findForward("getCreditBillingCollectScreen");
		}
    }
	}

  /**
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public ActionForward getCaseRateDetails(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, IOException {
    // TODO Auto-generated method stub
    String visitId = (String) request.getParameter("visitId");
    Integer caseRateNo = Integer.parseInt((String) request.getParameter("caseRateNo"));

    Map keys = new HashMap<>();
    keys.put("patient_id", visitId);
    keys.put("priority", 1);
    BasicDynaBean priPlanBean = patientInsurancePlanDao.findByKey(keys);

    Integer planId = (Integer) priPlanBean.get("plan_id");
    String insCompId = (String) priPlanBean.get("insurance_co");
    Integer planTypeId = (Integer) priPlanBean.get("plan_type_id");
    
    String query = request.getParameter("query");

    List<BasicDynaBean> caseRateDetails =
        planMasterDao.getCaseRateDetails(query, planId, insCompId, planTypeId, caseRateNo);

    Map codeMap = new HashMap();
    codeMap.put("result", ConversionUtils.listBeanToListMap(caseRateDetails));
    String responseContent = new JSONSerializer().deepSerialize(codeMap);

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(responseContent);
    response.flushBuffer();
    return null;
  }
	
  /**
   * Method to get case amount allocated in bill
   *
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public ActionForward getCaseRateAmountInBill(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws SQLException, IOException {
    String visitId = (String) request.getParameter("visitId");
    String billNo = (String) request.getParameter("bill_no");
    Map<String, Map<String, BigDecimal>> caseRateCaAmtMap = new HashMap<>();

    caseRateCaAmtMap = billBo.getCaseRateAmountInBill(visitId, billNo);
    String responseContent = new JSONSerializer().deepSerialize(caseRateCaAmtMap);

    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter().write(responseContent);
    response.flushBuffer();
    return null;
  }

	@IgnoreConfidentialFilters
  public ActionForward getDynaPackageDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, SQLException {

		String dynaPackageId = request.getParameter("dynaPackageId");
		String bedType = request.getParameter("bedType");
		String orgId = request.getParameter("orgId");
		if (dynaPackageId == null || dynaPackageId.isEmpty()) {
			return null;
		}
		if (orgId == null || orgId.isEmpty()) {
			orgId = "ORG0001";
		}
		if (bedType == null || bedType.isEmpty()) {
			bedType = "GENERAL";
		}

		List<BasicDynaBean> dynaPackageDetail = DynaPackageDAO.getDynaPackageIdCharges(orgId, bedType,
				Integer.parseInt(dynaPackageId));

		response.setContentType("text/plain");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		JSONSerializer js = new JSONSerializer().exclude("class");
		response.getWriter().write(js.deepSerialize(ConversionUtils.listBeanToListMap(dynaPackageDetail)));
		response.flushBuffer();
		return null;
	}
  
  /**
   * @param mapping
   * @param form
   * @param request
   * @param response
   * @return
   * @throws IOException
   * @throws SQLException
   */
  public ActionForward getCaseRateCategoryLimits(
      ActionMapping mapping,
      ActionForm form,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException, SQLException {
    String visitId = (String) request.getParameter("visit_id");
    String caseRateId = (String) request.getParameter("case_rate_id");

    List<BasicDynaBean> caseRateCategoryLimits = new ArrayList<>();

    if (null != caseRateId && !caseRateId.isEmpty()) {
      caseRateCategoryLimits =
          visitCaseRateDetailsDAO.getCaseRateCategoryLimits(visitId, Integer.parseInt(caseRateId));

      Boolean isVisitCaseRateDetailsExists =
          null != caseRateCategoryLimits && caseRateCategoryLimits.size() > 0 ? true : false;

      if (!isVisitCaseRateDetailsExists) {
        caseRateCategoryLimits =
            visitCaseRateDetailsDAO.getCaseRateCategoryLimitsFromMaster(
                Integer.parseInt(caseRateId));
      }
    }

    response.setContentType("text/plain");
    response.setHeader("Cache-Control", "no-cache");
    JSONSerializer js = new JSONSerializer().exclude("class");
    response
        .getWriter()
        .write(js.deepSerialize(ConversionUtils.listBeanToListMap(caseRateCategoryLimits)));
    response.flushBuffer();
    return null;
  }

	/*
	 * Method to set the details of the bill in the request scope, for a JSP to use
	 * while displaying.
	 */
	private void setBillDetails(HttpServletRequest req, String bedType, String orgid, Connection con) throws SQLException {

		BillBO billBOObj = new BillBO();
		JSONSerializer js = new JSONSerializer().exclude("class");

		if ( (orgid == null) || (orgid.equals("")) )
			orgid = "ORG0001";

		if ( (bedType == null) || (bedType.equals("")) )
			bedType = "GENERAL";

		// get Constants
		List chargeGroupsList = billBOObj.getChargeGroupConstNames(con);
		List chargeHeadList = billBOObj.getChargeHeadConstNames(con);
		req.setAttribute("chargeHeadsJSON", js.serialize(chargeHeadList));
		req.setAttribute("chargeGroupsJSON", js.serialize(chargeGroupsList));
		req.setAttribute("chargeGroupConstList",chargeGroupsList);
		Preferences p = (Preferences) req.getSession().getAttribute("preferences");
		if (p != null) {
			req.setAttribute("modulesActivatedJSON", js.serialize(p.getModulesActivatedMap()));
		}

		/*
		 * Create a map of chargeHeadId => associated_module
		 */
		HashMap chargeHeadModule = new HashMap();
		Iterator it = chargeHeadList.iterator();
		while (it.hasNext()) {
			Hashtable chargeHead = (Hashtable) it.next();
			chargeHeadModule.put(chargeHead.get("CHARGEHEAD_ID"), chargeHead.get("ASSOCIATED_MODULE"));
		}
		req.setAttribute("chargeHeadModule", chargeHeadModule);

		/*
		 * JSON data for use in loading dept/ward
		 */
		req.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
		int centerId = (Integer) req.getSession(false).getAttribute("centerId");
		List<BasicDynaBean> discAuths = DiscountAuthorizerMasterAction.getDiscountAuthorizers(centerId);
		req.setAttribute("discountAuthorizersJSON", js.serialize(ConversionUtils.listBeanToListMap(discAuths)));
		req.setAttribute("discountAuthorizers", discAuths);

		req.setAttribute("discountPlansJSON", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("discount_plan_details").listAll(con, null, null,null,"priority"))));

		BasicDynaBean mst = (BasicDynaBean) new GenericDAO("master_timestamp").getRecord();
		req.setAttribute("masterTimeStamp", mst.get("master_count"));

		req.setAttribute("paymentModesJSON", new JSONSerializer().serialize(
				ConversionUtils.listBeanToListMap(paymentModeMasterDao.listAll(con, null, null, null))));
		BasicDynaBean centerPrefs = new CenterPreferencesDAO().getCenterPreferences(centerId);
		req.setAttribute("centerPrefs", centerPrefs);
	}

	
	/**
	 * Validates whether given claimID is sent. Allows bill reopen if not sent.
	 *
	 * @param ClaimID the claim ID
	 * @return the error message
	 * @throws SQLException
	 *             the SQL exception
	 */
	String reopenClaimValidation(String ClaimID) throws SQLException {
		String error = null;
		BasicDynaBean claimbean = new ClaimDAO().getClaimById(ClaimID);
		String submissionBatchID = (String) claimbean.get("last_submission_batch_id");
		String claimStatus = (String) claimbean.get("status");
		Map<String,Object> keyss = new  HashMap<String, Object>();
		keyss.put("submission_batch_id", submissionBatchID);
		keyss.put("claim_id", ClaimID);
		BasicDynaBean claimSubmissionBean = new GenericDAO("claim_submissions").findByKey(keyss);
		if (claimbean != null && claimSubmissionBean != null) {
			String claimSubStatus = (claimSubmissionBean.get("status") != null) ? (String) claimSubmissionBean.get("status") : null;

			if (claimSubStatus != null && !"D".equals(claimSubStatus) && (claimStatus.equals("B") || claimSubStatus.equals("S"))) {
				return error = " Cannot reopen bill. (Bill Insurance Claim:"
						+ ClaimID + " is Batched or marked as Sent.)";
			}
		}
		return error;
	}
	/*
	 * Save Bill Changes, includes the following:
	 * - Add a new charge element
	 * - Change one or more charge element's date, rate, amount, qty, discount, amtPaid
	 * - Change bill status, or OK to discharge
	 * - Make a payment
	 * All the above can be done in one transaction.
	 */
	public ActionForward saveBillDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException, SQLException, ParseException,Exception {

		FlashScope flash = FlashScope.getScope(request);
		String error = null;
		HttpSession session=request.getSession();
		String userid = (String)session.getAttribute("userid");
		CreditBillForm creditBillForm = (CreditBillForm)form;
		BillBO billBOObj = new BillBO();
		AbstractPaymentDetails bpImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.BILL_PAYMENT);
		List<Receipt> receiptList = null;
		Map<String, String[]> requestParams = request.getParameterMap();
		Map printParamMap = null;
		boolean removedDynaPkg = false;
		int exisistingDynaPkdId =0 ;
		String nokContact = request.getParameter("nok_contact");
		String isNewUX = request.getParameter("isNewUX");
		HttpSession  ses = request.getSession(false);
        String userName = (String)ses.getAttribute("userid");
        String billSignature = request.getParameter("fieldImgText");
		// checking whether module is active r not from pref..
        Preferences pref = (Preferences)request.getSession(false).getAttribute("preferences");
        String ipModAct = "N";
        // sometimes pref is null when app restarts
        if ( (pref!=null) && (pref.getModulesActivatedMap() != null) ) {
        	ipModAct = (String)pref.getModulesActivatedMap().get("mod_adt");
        	 if(ipModAct == null || ipModAct.equals("")){
        		 ipModAct = "N";
             }
        }
        
		/*
		 * The action can be:
		 * 1. Save:
		 *     Credit Bill: Bill status/ins/corp + charges + payments
		 *     Prepaid Bill: Bill status + charges (payments should be disallowed in frontend)
		 * 2. Pay and Close:
		 *     Credit Bill: NA
		 *     Prepaid Bill: Bill status = close + charges + payment details
		 * 3. Reopen:
		 *     Only for closed/cancelled bills: only change the status
		 */
		String action = request.getParameter("buttonAction");
		log.debug("Action to be performed is: " + action);
		HashMap actionRightsMap = new HashMap();
		Object roleID=null;
		Role role=new Role();
		actionRightsMap= (HashMap) request.getSession(false).getAttribute("actionRightsMap");
		roleID=  request.getSession(false).getAttribute("roleId");
		String actionRightStatus=(String) actionRightsMap.get(role.BILL_REOPEN);
		String actionEditOpenDateRights = (String)actionRightsMap.get("allow_edit_bill_open_date");
		String actionBillFinalizedDate = (String)actionRightsMap.get("modify_bill_finalized_date");
		String refundRights = (String) actionRightsMap.get("allow_refund");
		Integer centerId = Integer.valueOf(request.getSession(false).getAttribute("centerId").toString());

		if (actionRightStatus==null)
			actionRightStatus="N";
		if (actionEditOpenDateRights==null) actionEditOpenDateRights = "N";
		
    if (actionBillFinalizedDate == null) {
      actionBillFinalizedDate = "N";
    }

		boolean isPaymentByDeposit = false;
		boolean isPaymentByRewardPoints = false;
		String newStatus = null;
		Bill bill = null;
		
		do {

			bill = billBOObj.getBill(creditBillForm.getBillNo());
			exisistingDynaPkdId = bill.getDynaPkgId();
			long oldModtime = Long.parseLong(creditBillForm.getModTime());
			long currentModTime = bill.getModTime().getTime();

			// Compare with bill modtime long value.
			if (oldModtime != currentModTime) {
				error = "Your changes are not saved as there are some latest updates done by other user, Your screen got already refreshed now and you can continue making your changes.";
				break;
			}
			if (action.equals("reopen")) {
				if (actionRightStatus.equalsIgnoreCase("A")|| roleID.equals(1)|| roleID.equals(2)) {

					List<BasicDynaBean> billClaims = billClaimDao.listAll(null, "bill_no", creditBillForm.getBillNo(), "priority");
					String priClaimID = (null != billClaims && billClaims.size() > 0 && null != billClaims.get(0)) ? (String)billClaims.get(0).get("claim_id") : null;
					String secClaimID = (null != billClaims && billClaims.size() > 1 && null != billClaims.get(1)) ? (String)billClaims.get(1).get("claim_id") : null;

					if (priClaimID != null && !priClaimID.equals("")) {
						error = reopenClaimValidation(priClaimID);
						if (error != null)
							break;
					}

					if (secClaimID != null && !secClaimID.equals("")) {
						error = reopenClaimValidation(secClaimID);
						if (error != null)
							break;
					}

					String sponsorBillNo = bill.getSponsorBillNo();
					if (sponsorBillNo != null && !sponsorBillNo.equals("")) {
						error = " Cannot reopen bill. (This Bill belongs to Sponsor Consolidated Bill - "
							+ sponsorBillNo + ". Please cancel or delete from the claim bill to reopen)";
						break;
					}

					boolean success = billBOObj.reopenBill(creditBillForm.getBillNo(),
							creditBillForm.getReopenReason(), userid, creditBillForm.getSecondarySponsorExists());
					
					// This is called to reset the writeoff receipt amounts to zero.
					allocationService.resetWriteoffAmount(creditBillForm.getBillNo());
					
					Boolean drgCodeExisits = drgupdateDao.isDrgCodeExists(creditBillForm.getBillNo());
				    if(success && drgCodeExisits){
				    	drgupdateDao.unlockDRGItems(creditBillForm.getBillNo());
				    }
				    
				    sponsorDAO.unlockPackageChargeItem(creditBillForm.getBillNo());
				    
					if (!success) {
						error = "Bill could not be reopened: unknown error.";
					}
				} else {
					error = "You don't have Authorization to Reopen a Closed Bill ";
				}
				break;
			}

			/*if (action.equals("visitCopayProcess")) {
				BillDAO.resetTotalsOrReProcess(creditBillForm.getBillNo());
				break;
			}*/

			if (action.equals("perdiemProcess")) {
				BasicDynaBean visitBean = visitDetailsDao.findByKey("patient_id", bill.getVisitId());
				boolean usePerdiem = VisitDetailsDAO.visitUsesPerdiem(bill.getVisitId());
				String existingPerdiemCode = visitBean.get("per_diem_code") != null ? (String)visitBean.get("per_diem_code") : null;
				Connection con = null;
				boolean success = false;

				// Reset visit perdiem code as empty in order
				// to make bill charges reset to original claim amounts if code is changed.
				try {
					if (creditBillForm.getPer_diem_code() != null && existingPerdiemCode != null
							&& !existingPerdiemCode.equals(creditBillForm.getPer_diem_code())) {
						con = DataBaseUtil.getConnection();
						con.setAutoCommit(false);
						success = VisitDetailsDAO.updateVisitPerdiemCode(con, null,	bill.getVisitId(), userid);
					}
				}finally {
					DataBaseUtil.commitClose(con, success);
				}

				// To reset bill charges with original claim amounts and copay if code is changed.
				if (success) {
					String err = new BillBO().perdiemProcess(creditBillForm.getBillNo(), true, false);
					if (err != null) {
						error = err;
						break;
					}
				}

				// Update perdiem code.
				try {
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);
					success = VisitDetailsDAO.updateVisitPerdiemCode(con, creditBillForm.getPer_diem_code(),
							bill.getVisitId(), userid);
				}finally {
					DataBaseUtil.commitClose(con, success);
				}

				// Process perdiem only if visit uses perdiem and code is updated.
				if (usePerdiem && success) {
					String err = new BillBO().perdiemProcess(creditBillForm.getBillNo(), true, false);
					if (err != null) {
						error = err;
					}else {
						if (creditBillForm.getPer_diem_code() == null || creditBillForm.getPer_diem_code().equals(""))
							flash.put("info", "Bill Perdiem Code removed and charges claim amounts are reset successfully.");
						else
							flash.put("info", "Bill Perdiem Code processed successfully.");
					}
					break;
				}
			}

			if (action.equals("process")) {
				String err = new DynaPackageProcessor().process(creditBillForm.getBillNo(), false);
				sponsorBO.recalculateSponsorAmount(bill.getVisitId());
				if (err != null) {
					error = err;
				}else {
					flash.put("info", "Dyna Package processed successfully.");
				}
				break;
			}

			String[] paymentType = creditBillForm.getPaymentType();
			if (paymentType != null) {
				for (int i=0; i<paymentType.length; i++) {
					if (paymentType[i].equalsIgnoreCase(Receipt.REFUND)) {
						if (refundRights != null && !refundRights.equalsIgnoreCase("A") &&
								!roleID.equals(1) && !roleID.equals(2)) {
							error = "You don't have authorization to Refund Payments. ";
							break;
						}
					}
				}
			}
			

			// things we need for saving
			List insertBillChargeList = new ArrayList();
			List updateBillChargeList = new ArrayList();
			
			Map<String,List<BasicDynaBean>> insertBillChargeTaxMap = new HashMap<String, List<BasicDynaBean>>();
			Map<String,List<BasicDynaBean>> updateBillChargeTaxMap = new HashMap<String, List<BasicDynaBean>>();

			String origStatus = bill.getStatus();
			String origDischargeStatus = bill.getOkToDischarge();
			newStatus = creditBillForm.getBillStatus();

			if((origStatus.equals("C")) && !action.equals("reopen")){
				error = "Bill is closed please reopen for further modification";
				break;
			}

			/*
			 * Validate that user has rights to move from any other status to open, using
			 * bill_reopen action rights
			 */
			if ( !origStatus.equals(Bill.BILL_STATUS_OPEN) && newStatus.equals(Bill.BILL_STATUS_OPEN) ) {
				if (!actionRightStatus.equalsIgnoreCase("A") && !roleID.equals(1) && !roleID.equals(2)) {
					error = "You don't have authorization to Reopen a bill ";
					break;
				}
			}
			
			
			if ((bill.getBillType().equals("C") 
					|| ( bill.getBillType().equals("P") && bill.getIs_tpa() && (bill.getRestrictionType().equals("N") || bill.getRestrictionType().equals("P")) )) 
				&& ( (newStatus.equals("F") || newStatus.equals("C")) && (!origStatus.equals("F") && !origStatus.equals("C")) )  
			) {
				if ( !OrderDAO.isBillBedDetailsFinalized(bill.getVisitId(), bill.getBillNo()).equals("Finalized") ){
					error = "Cannot finalize bill. Bed finalization has to be done.";
					break;
				}
			}
			

			/*
			 * Do all bill updates except the main status change if any
			 */
			if (newStatus.equals("C")) {
				bill.setPaymentStatus(Bill.BILL_PAYMENT_PAID);
				bill.setPrimaryClaimStatus(Bill.BILL_CLAIM_RECEIVED);
				if (creditBillForm.getSecondarySponsorExists() != null
						&& creditBillForm.getSecondarySponsorExists().equals("Y")) {
					bill.setSecondaryClaimStatus(Bill.BILL_CLAIM_RECEIVED);
				}
			} else {
				bill.setPaymentStatus(creditBillForm.getPaymentStatus());
				if (bill.getIs_tpa() && newStatus.equals("A")) {
					bill.setPrimaryClaimStatus(Bill.BILL_CLAIM_OPEN);
					if (creditBillForm.getSecondarySponsorExists() != null
							&& creditBillForm.getSecondarySponsorExists().equals("Y")) {
						bill.setSecondaryClaimStatus(Bill.BILL_CLAIM_OPEN);
					}
				} else {
					bill.setPrimaryClaimStatus(creditBillForm.getPrimaryClaimStatus());
					if (creditBillForm.getSecondarySponsorExists() != null
							&& creditBillForm.getSecondarySponsorExists().equals("Y")) {
						bill.setSecondaryClaimStatus(creditBillForm.getSecondaryClaimStatus());
					}
				}
			}
			
			String sponsorWriteOffFlag = request.getParameter("sponsor_writeoff");
			if(null != sponsorWriteOffFlag && !sponsorWriteOffFlag.equals(""))
				bill.setSponsorWriteOff(sponsorWriteOffFlag);

			RegistrationPreferencesDTO regPrefs = RegistrationPreferencesDAO.getRegistrationPreferences();
			boolean doc_eandm_codification_required =
					regPrefs.getDoc_eandm_codification_required() != null
							&& regPrefs.getDoc_eandm_codification_required().equals("Y");

			// All the Bills have to be finalized automatically if E&M codification required preference is marked as "NO"
			if (origStatus.equals(Bill.BILL_STATUS_OPEN) && !doc_eandm_codification_required && action.equals("paysave")) {
				newStatus = Bill.BILL_STATUS_FINALIZED;
			}

			bill.setBillRemarks(creditBillForm.getBillRemarks());
			bill.setWriteOffRemarks(creditBillForm.getWriteOffRemarks());
			bill.setSpnrWriteOffRemarks(creditBillForm.getSpnrWriteOffRemarks());
			bill.setBillDiscountAuth(creditBillForm.getBillDiscountAuth());
			bill.setBillDiscountCategory(creditBillForm.getBillDiscountCategory());
			bill.setMrno(creditBillForm.getMrNo());
			bill.setCancelReason(creditBillForm.getCancelReason());
			bill.setBillLabelId(creditBillForm.getBillLabelId());

			if (creditBillForm.getPrimaryTotalClaim() != null && !(creditBillForm.getPrimaryTotalClaim()).equals(""))
				bill.setPrimaryTotalClaim(new BigDecimal(creditBillForm.getPrimaryTotalClaim()));
			else
				bill.setPrimaryTotalClaim(BigDecimal.ZERO);

			if (creditBillForm.getSecondaryTotalClaim() != null && !(creditBillForm.getSecondaryTotalClaim()).equals(""))
				bill.setSecondaryTotalClaim(new BigDecimal(creditBillForm.getSecondaryTotalClaim()));
			else
				bill.setSecondaryTotalClaim(BigDecimal.ZERO);

			bill.setVisitId(creditBillForm.getVisitId());

			if (bill.getIs_tpa()) {
				if (creditBillForm.getInsuranceDeduction() != null && !(creditBillForm.getInsuranceDeduction()).equals(""))
					bill.setInsuranceDeduction(new BigDecimal(creditBillForm.getInsuranceDeduction()));
			}else
				bill.setInsuranceDeduction(BigDecimal.ZERO);

			if (bill.getSponsorBillNo() != null && !bill.getSponsorBillNo().equals("") )
				bill.setClaimRecdAmount(creditBillForm.getClaimRecdAmount());

			if (creditBillForm.getPrimaryApprovalAmount() != null && !(creditBillForm.getPrimaryApprovalAmount()).equals(""))
				bill.setPrimaryApprovalAmount(new BigDecimal(creditBillForm.getPrimaryApprovalAmount()));
			else
				bill.setPrimaryApprovalAmount(null);

			if (creditBillForm.getSecondaryApprovalAmount() != null && !(creditBillForm.getSecondaryApprovalAmount()).equals(""))
				bill.setSecondaryApprovalAmount(new BigDecimal(creditBillForm.getSecondaryApprovalAmount()));
			else
				bill.setSecondaryApprovalAmount(null);

			if (bill.getPrimaryApprovalAmount() == null && bill.getSecondaryApprovalAmount() == null) {
				bill.setApprovalAmount(null);
			}else {
				BigDecimal priApprovalAmt = BigDecimal.ZERO;
				BigDecimal secApprovalAmt = BigDecimal.ZERO;

				if (bill.getPrimaryApprovalAmount() != null) priApprovalAmt = bill.getPrimaryApprovalAmount();
				if (bill.getSecondaryApprovalAmount() != null) secApprovalAmt = bill.getSecondaryApprovalAmount();
				bill.setApprovalAmount(priApprovalAmt.add(secApprovalAmt));
			}

			bill.setUserName(userid);
			bill.setProcedureNo(creditBillForm.getProcedure_no());
			if (creditBillForm.getOkToDischarge() == null) {
				if (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
					// bill now bill: always ok to discharge
					bill.setOkToDischarge("Y");
				} else {
					// bill later, secondary, OP: discharge is not allowed from here
					bill.setOkToDischarge("N");
				}
			} else {
				bill.setOkToDischarge(creditBillForm.getOkToDischarge());
			}

			bill.setDoctorId(creditBillForm.getDoctorId());
			bill.setDynaPkgId(creditBillForm.getDynaPkgId());
			bill.setDynaPkgCharge(creditBillForm.getDynaPkgCharge());

      Timestamp openDate = null;
      try {
        openDate = DateUtil.parseTimestamp(creditBillForm.getOpendate(),
            creditBillForm.getOpentime());
      } catch (ParseException pe) {
        log.error(" Open Date: " + creditBillForm.getOpendate());
        log.error(" Open Time: " + creditBillForm.getOpentime());
        error = "Invalid Open Date and Time";
        break;
      }

      if (openDate.after(DateUtil.getCurrentTimestamp())) {
        log.error(" Open Date: " + openDate);
        error = "Bill Open Date is in Future.";
        break;
      }
      BasicDynaBean visitBean = visitDetailsDao.findByKey("patient_id", bill.getVisitId());
      if(null != visitBean) {
        Timestamp regDateTime = DateUtil.timestampFromDateTime(
            (java.sql.Date) visitBean.get("reg_date"), (java.sql.Time) visitBean.get("reg_time"));
        if (openDate.before(regDateTime)) {
          log.error(" Open Date: " + openDate);
          error = "Bill Open Date is before admission date.";
          break;
        }
      }
      Calendar cal = Calendar.getInstance();  
      cal.setTime(bill.getOpenDate());
      cal.set(Calendar.SECOND, 0);  
      cal.set(Calendar.MILLISECOND, 0);  
      Date origOpenDate = cal.getTime();
      if (origOpenDate != null && openDate.before(origOpenDate) && !roleID.equals(1)
          && !roleID.equals(2) && "N".equals(actionEditOpenDateRights)) {
        log.error(" Original Open Date: " + origOpenDate);
        log.error(" Open Date: " + openDate);
        error = "You don't have authorization to Backdate a bill ";
        break;
      }

      bill.setOpenDate(openDate);

			BasicDynaBean ipDepositBean = DepositsDAO.getIPBillDepositDetails(bill.getBillNo());
			boolean ipDepositExists = ipDepositBean != null;

			setBillDepositSetOff(creditBillForm, bill,ipDepositExists);

			if(bill.getVisitType().equals("i") && ipDepositExists){
				if(creditBillForm.getDepositType() != null){
					if(creditBillForm.getDepositType().equals("i")){
						creditBillForm.setIpDepositSetOff(creditBillForm.getDepositSetOff());
						setIPBillDepositSetOff(creditBillForm, bill);
					}else{
						BigDecimal totDepositAvl = getTotalAvailableDeposit(creditBillForm);
						BigDecimal totIPDepsoitAvl = getTotalIPAvailableDeposit(creditBillForm);
						BigDecimal totGenDepositAvl = totDepositAvl.subtract(totIPDepsoitAvl);
						if(totGenDepositAvl.compareTo(BigDecimal.ZERO) >= 0 ){
							if(creditBillForm.getDepositSetOff().compareTo(totGenDepositAvl) > 0){
								creditBillForm.setIpDepositSetOff(creditBillForm.getDepositSetOff().subtract(totGenDepositAvl));
								setIPBillDepositSetOff(creditBillForm, bill);
							}
						}
					}
				}
			}

			if (creditBillForm.getDepositSetOff() != null) {
				if (bill.getDepositSetOff().compareTo(BigDecimal.ZERO) != 0)
					isPaymentByDeposit = (newStatus.equals(Bill.BILL_STATUS_CLOSED) && bill.getBillType().equals(Bill.BILL_TYPE_PREPAID));
			}

			if (creditBillForm.getRewardPointsRedeemedAmount() != null) {
				if (bill.getRewardPointsRedeemedAmount().compareTo(BigDecimal.ZERO) != 0)
					isPaymentByRewardPoints = (newStatus.equals(Bill.BILL_STATUS_CLOSED) && bill.getBillType().equals(Bill.BILL_TYPE_PREPAID));
			}
			/* If the bill is cancelled then make deposit set off amount to zero*/
			if(newStatus.equals(bill.BILL_STATUS_CANCELLED)){
				bill.setDepositSetOff(BigDecimal.ZERO);
				bill.setIpDepositSetOff(BigDecimal.ZERO);
			}

		//	BillBO.updateMultiVistDepositBalance(creditBillForm);

			int numCharges = 0;
			if (creditBillForm.getChargeId() != null) {
				numCharges = creditBillForm.getChargeId().length;
			}

			boolean updateDynaPkg = false;
			if (creditBillForm.getDynaPkgId() > 0 && (newStatus.equals("A") || newStatus.equals("F"))) {
				updateDynaPkg = true;
			}

			Map drgCodeMap = ChargeDAO.getBillDRGCode(bill.getBillNo());
			boolean hasDRGCode = false;
			if (drgCodeMap != null && drgCodeMap.get("drg_charge_id") != null) {
				hasDRGCode = true;
			}

			
			int planId = 0;
			if (visitBean != null) {
				planId = (Integer) visitBean.get("plan_id");
				log.info(" Reg  Date: " + DateUtil.formatDate((java.sql.Date) visitBean.get("reg_date"))
        + " Reg Time: " + DateUtil.formatSQlTime((java.sql.Time) visitBean.get("reg_time")));
			}

			List<BasicDynaBean> planListBean = patientInsurancePlanDao.listAll(null, "patient_id", bill.getVisitId(), "priority");
			int planIds[] = null;

			planIds = null!= planListBean && planListBean.size() > 0 ? new int[planListBean.size()] : null;

			for(int i=0; i<planListBean.size(); i++){
				planIds[i] = (Integer)planListBean.get(i).get("plan_id");
			}
			boolean multiPlanExists = null != planListBean && planListBean.size() == 2;

			boolean billHasPlanVisitCopayLimit = planMasterDao.hasPlanVisitCopayLimit(planId, bill.getVisitType());
			billHasPlanVisitCopayLimit = billHasPlanVisitCopayLimit && (bill.getIs_tpa() && bill.getRestrictionType().equals("N"));

			Timestamp postedDateTime = DateUtil.getCurrentTimestamp();
			String postedDate = null;
			String postedTime = null;

			for (int i = 0; i < numCharges; i++) {
				String chargeId = creditBillForm.getChargeId()[i];
				boolean isEdited = creditBillForm.getEdited()[i];

				// ignore the "template" charge: this is unavoidable. New charges id start with _
				if ((chargeId == null) || chargeId.equals(""))
					continue;

				//For Dynapackage Allow only one entry to table incase of multi user and multi tab.
				/*BasicDynaBean bill_Bean = billDao.findByKey("bill_no", bill.getBillNo());
				int dynaPkgId = (Integer)bill_Bean.get("dyna_package_id");
				if(creditBillForm.getChargeHeadId()[i].equals("MARPKG") && dynaPkgId > 0 ){
					if (chargeId.startsWith("_") && creditBillForm.getEdited()[i]){
						Map<String,Object> chargekeys = new HashMap<String, Object>();
						chargekeys.put("bill_no", bill.getBillNo());
						chargekeys.put("charge_head", "MARPKG");
						BasicDynaBean bill_chargeBean = billChargeDao.findByKey(chargekeys);
						chargeId = (String) bill_chargeBean.get("charge_id");
					}
				}*/
				//For Dynapackage Allow only one entry to table incase of multi user and multi tab.
				Map<String,Object> chargekeys = new HashMap<String, Object>();
				chargekeys.put("bill_no", bill.getBillNo());
				chargekeys.put("charge_head", "MARPKG");
				BasicDynaBean bill_Bean = billChargeDao.findByKey(chargekeys);
				if(creditBillForm.getChargeHeadId()[i].equals("MARPKG") && null != bill_Bean){
					isEdited = true;
					if (chargeId.startsWith("_")){
						chargeId = (String) bill_Bean.get("charge_id");
					}
				}
				
				// ignore charges that have not been edited or new (New charges will have _ as the id)
				if (!chargeId.startsWith("_") && !isEdited)
					continue;
				
				ChargeDTO charge = new ChargeDTO();
				charge.setBillNo(creditBillForm.getBillNo());
				charge.setChargeId(chargeId);
				charge.setChargeGroup(creditBillForm.getChargeGroupId()[i]);
				charge.setChargeHead(creditBillForm.getChargeHeadId()[i]);
				charge.setActRate(creditBillForm.getRate()[i]);
				charge.setActQuantity(creditBillForm.getQty()[i].setScale(2, BigDecimal.ROUND_HALF_UP));
				charge.setVisitType(bill.getVisitType());

				// set all editable fields
				postedDate = creditBillForm.getPostedDate()[i];
				postedTime = creditBillForm.getPostedTime()[i];

				try {
					postedDateTime = DateUtil.parseTimestamp(postedDate +" "+ postedTime);
				} catch (ParseException e) {
					log.error("Could not parse posted date: " + postedDate +" "+ postedTime, e);
				}
				SimpleDateFormat datetFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				Date postedDt = datetFmt.parse(datetFmt.format(postedDateTime));
				Date billOpenDt = datetFmt.parse(datetFmt.format(bill.getOpenDate()));

				// Compare with bill open date.
				if (postedDt.getTime() >= billOpenDt.getTime()) {
					charge.setPostedDate(postedDateTime);
				}else {
					// Compare with charge posted date if exists.
						charge.setPostedDate(new Timestamp(bill.getOpenDate().getTime()));

				}

				charge.setActRemarks(creditBillForm.getRemarks()[i]);
				charge.setUserRemarks(creditBillForm.getUserRemarks()[i]);
				charge.setItemRemarks(creditBillForm.getItemRemarks()[i]);
				charge.setActRatePlanItemCode(creditBillForm.getActRatePlanItemCode()[i]);
				charge.setAmount(creditBillForm.getAmt()[i]);
				charge.setDiscount(creditBillForm.getDisc()[i]);
				charge.setIsSystemDiscount(creditBillForm.getIsSystemDiscount()[i]);
				charge.setServiceSubGroupId(creditBillForm.getService_sub_group_id()[i]);
				charge.setInsuranceCategoryId(creditBillForm.getInsuranceCategoryId()[i]);
				charge.setConducting_doc_mandatory(creditBillForm.getConducting_doc_mandatory()[i]);

				BigDecimal drDiscount = creditBillForm.getDr_discount_amt()[i];
				if (drDiscount == null) drDiscount = BigDecimal.ZERO;
				BigDecimal presDiscount = creditBillForm.getPres_dr_discount_amt()[i];
				if (presDiscount == null) presDiscount = BigDecimal.ZERO;
				BigDecimal refDiscount = creditBillForm.getRef_discount_amt()[i];
				if (refDiscount == null) refDiscount = BigDecimal.ZERO;
				BigDecimal hospDiscount = creditBillForm.getHosp_discount_amt()[i];
				if (hospDiscount == null) hospDiscount = BigDecimal.ZERO;

				BigDecimal totalDiscount = drDiscount.add(presDiscount).add(refDiscount).add(hospDiscount);

				if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
					charge.setDiscount_auth_dr(creditBillForm.getDiscount_auth_dr()[i]);
					charge.setDr_discount_amt(creditBillForm.getDr_discount_amt()[i]);

					charge.setDiscount_auth_pres_dr(creditBillForm.getDiscount_auth_pres_dr()[i]);
					charge.setPres_dr_discount_amt(creditBillForm.getPres_dr_discount_amt()[i]);

					charge.setDiscount_auth_ref(creditBillForm.getDiscount_auth_ref()[i]);
					charge.setRef_discount_amt(creditBillForm.getRef_discount_amt()[i]);

					charge.setDiscount_auth_hosp(creditBillForm.getDiscount_auth_hosp()[i]);
					charge.setHosp_discount_amt(creditBillForm.getHosp_discount_amt()[i]);
				}else {
					charge.setOverall_discount_auth(creditBillForm.getOverall_discount_auth()[i]);
					charge.setOverall_discount_amt(creditBillForm.getOverall_discount_amt()[i]);
				}

				BigDecimal claimAmounts[] = null;
				BigDecimal claimTaxAmounts[] = null;
				String preAuthIds[] = null;
				Integer preAuthModeIds[] = null;
				String includeInClaimCalc[] = null;


				if(creditBillForm.getInsClaimAmt() != null){
					charge.setInsuranceClaimAmount(creditBillForm.getInsClaimAmt()[i]);
					charge.setPreAuthId(creditBillForm.getPreAuthId()[i]);
					charge.setPreAuthModeId(creditBillForm.getPreAuthModeId()[i]);
					boolean planExists = null != planIds && planIds.length > 0;
					if(planExists) {
						BigDecimal priInsClaimAmt = creditBillForm.getPriInsClaimAmt()[i];
						BigDecimal priInsClaimTaxAmt = creditBillForm.getPriInsClaimTaxAmt()[i];
						claimAmounts = new BigDecimal[planIds.length];
						claimTaxAmounts = new BigDecimal[planIds.length];
						preAuthIds = new String[planIds.length];
						preAuthModeIds = new Integer[planIds.length];
						includeInClaimCalc = new String[planIds.length];
						if(multiPlanExists && planExists) {
							BigDecimal secinsClaimAmt = creditBillForm.getSecInsClaimAmt()[i];
							BigDecimal secinsClaimTaxAmt = creditBillForm.getSecInsClaimTaxAmt()[i];
							claimAmounts[0] = priInsClaimAmt;
							claimAmounts[1] = secinsClaimAmt;
							claimTaxAmounts[0] = priInsClaimTaxAmt;
							claimTaxAmounts[1] = secinsClaimTaxAmt;
							includeInClaimCalc[0] = creditBillForm.getPriIncludeInClaim()[i];
							includeInClaimCalc[1] = creditBillForm.getSecIncludeInClaim()[i];
							preAuthIds[0] = creditBillForm.getPreAuthId()[i];
							preAuthIds[1] = creditBillForm.getSecPreAuthId()[i];
							preAuthModeIds[0] = creditBillForm.getPreAuthModeId()[i];
							preAuthModeIds[1] = creditBillForm.getSecPreAuthModeId()[i];
						} else if(planExists){
							claimAmounts[0] = priInsClaimAmt;
							claimTaxAmounts[0] = priInsClaimTaxAmt;
							includeInClaimCalc[0] = creditBillForm.getPriIncludeInClaim()[i];
							preAuthIds[0] = creditBillForm.getPreAuthId()[i];
							preAuthModeIds[0] = creditBillForm.getPreAuthModeId()[i];
						}
						charge.setClaimAmounts(claimAmounts);
						charge.setSponsorTaxAmounts(claimTaxAmounts);
						charge.setPreAuthIds(preAuthIds);
						charge.setPreAuthModeIds(preAuthModeIds);
						charge.setIncludeInClaimCalc(includeInClaimCalc);
					}
				}

				charge.setAllowDiscount(creditBillForm.getAllowDiscount()[i]);
				charge.setAllowRateVariation(creditBillForm.getAllowRateVariation()[i]);
				charge.setAllowRateDecrease(creditBillForm.getAllowRateDecrease()[i]);
				charge.setAllowRateIncrease(creditBillForm.getAllowRateIncrease()[i]);

				charge.setRedeemed_points(creditBillForm.getRedeemed_points()[i]);

				charge.setPayeeDoctorId(creditBillForm.getPayeeDocId()[i]);
				charge.setPrescribingDrId(creditBillForm.getPrescDocId()[i]);
				charge.setAmount_included(creditBillForm.getAmount_included()[i]);
				charge.setActivityConducted(creditBillForm.getActivityConducted());
				charge.setQty_included(creditBillForm.getQty_included()[i]);
				charge.setPackageFinalized(creditBillForm.getPackageFinalized()[i]);
				charge.setConsultation_type_id(creditBillForm.getConsultation_type_id()[i]);
				charge.setUsername(userid);
				charge.setFirstOfCategory(creditBillForm.getFirstOfCategory()[i]);
				charge.setModTime(DateUtil.getCurrentTimestamp());

				charge.setIsClaimLocked(creditBillForm.getIsClaimLocked()[i]);

				if (creditBillForm.getDelCharge()[i]) {
					charge.setStatus(ChargeDTO.CHARGE_STATUS_CANCELLED);
					if(!DoctorsNotesDAO.updateBillableConsultation(creditBillForm.getChargeId()[i]))
						break;
					// set the amount to 0 so that audit log catches this as a change, and
					// consequently, accrual report will reflect the change in revenue.
					charge.setAmount(BigDecimal.ZERO);
					charge.setActQuantity(BigDecimal.ZERO);
					charge.setInsuranceClaimAmount(BigDecimal.ZERO);
					charge.setDiscount(BigDecimal.ZERO);
					charge.setTaxAmt(BigDecimal.ZERO);
					charge.setActivityConducted("N");
					if(creditBillForm.getChargeHeadId()[i].equals("MARPKG") && exisistingDynaPkdId != 0) {
					  removedDynaPkg = true;
					}
				} else {
					charge.setStatus(ChargeDTO.CHARGE_STATUS_ACTIVE);
				}
				int packageId = 0;
				charge.setActDescription(creditBillForm.getDescription()[i]);
				charge.setActDescriptionId(creditBillForm.getDescriptionId()[i]);
				charge.setActUnit(creditBillForm.getUnits()[i]);
				charge.setOp_id(creditBillForm.getOp_id()[i]);
				if (PACKAGE_CHARGEGROUP.equals(creditBillForm.getChargeGroupId()[i])){
				  packageId  = Integer.valueOf(creditBillForm.getDescriptionId()[i]);
				charge.setPackageId(packageId);
				}
				if("BED".equalsIgnoreCase(creditBillForm.getChargeGroupId()[i])){
					charge.setBedType(creditBillForm.getDescriptionId()[i]);
				}
				String chargeHead = charge.getChargeHead();
				if(chargeHead.equals("ROF")){
				  charge.setIsClaimLocked(true);
				}
				charge.setDynaPackageExcluded(creditBillForm.getDynaPackageExcluded()[i]);
				
				List<BasicDynaBean> insertBillChargeTaxList = new ArrayList<BasicDynaBean>();
				List<BasicDynaBean> updateBillChargeTaxList = new ArrayList<BasicDynaBean>();
						
				if (charge.getChargeId().startsWith("_")) {
					// Package Charges added in the UI

					int plan_Id = 0;
					Connection con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);
					String bedType = request.getParameter("bedType");
					String orgId = bill.getBillRatePlanId();
					BigDecimal totalCharge = BigDecimal.ZERO;
					String mainChargeId = (String) chargeId;
					BigDecimal chargeValue = BigDecimal.ZERO;
					BigDecimal chargeDiscount = BigDecimal.ZERO;
					String submisionBatchType = null;
					String I = null;
					Integer panelId = null;
					if (PACKAGE_CHARGEGROUP.equals(charge.getChargeGroup())) {
						List<BasicDynaBean> packageContents = PackageDAO.getPackgeComponentDetails(packageId, orgId , bedType);
						BasicDynaBean pkgCharge = PackageDAO.getPackageDetails(packageId, orgId, bedType);
						Integer chargeInc = 0;
						for (BasicDynaBean packageContent : packageContents) {
							int packContentId = (int) packageContent.get("package_content_id");
							String actDesc = (String) packageContent.get("activity_description");
							String actRemarks = (String) packageContent.get("package_name");
							String chargeHeadName = (String) packageContent.get("charge_head");
							if (request.getParameter("planId") != null && !request.getParameter("planId").isEmpty()) {
								plan_Id = Integer.valueOf(request.getParameter("planId"));
							}
							submisionBatchType = (String) pkgCharge.get("submission_batch_type");
							panelId = (Integer) packageContent.get("panel_id");

							BasicDynaBean pkgContentsCharges = PackageDAO.getPackageContentDetails(packContentId, orgId,
									bedType);

							BigDecimal totalItemQty = new BigDecimal((int) packageContent.get("activity_qty"));
							BigDecimal chargeAmt = ((BigDecimal) pkgContentsCharges.get("charge")).divide(totalItemQty,
									2);
							totalCharge = totalCharge.add(chargeAmt);
							BigDecimal packageCharge = new BigDecimal(String.valueOf(pkgCharge.get("charge")));
							// discount will be package discount
							BigDecimal packageDiscount = new BigDecimal(String.valueOf(pkgCharge.get("discount")));
							BigDecimal discount = discountSplit((BigDecimal) pkgContentsCharges.get("charge"), packageCharge, packageDiscount);
							// Adding discount and charge for inventory
							if (INVENTORY_CHARGEHEAD.equals(chargeHeadName)) {
								chargeValue = chargeValue.add(chargeAmt.multiply(totalItemQty));
								chargeDiscount = chargeDiscount.add(discount);
							}
							if (!INVENTORY_CHARGEHEAD.equals(chargeHeadName)) {
								ChargeDAO chargeDAOObj = new ChargeDAO(con);
								totalDiscount = totalDiscount.add(discount);
								String packContChargeId = "_" + mainChargeId + "_" + String.valueOf(chargeInc);
								packContChargeId = chargeDAOObj.getNextChargeId();
                ChargeDTO contentCharge = new ChargeDTO(PACKAGE_CHARGEGROUP, chargeHeadName,
                    (BigDecimal) chargeAmt, (BigDecimal) totalItemQty, (BigDecimal) discount, "",
                    String.valueOf(packContentId), actDesc,
                    (String) packageContent.get("act_department_id"), bill.getIs_tpa(), plan_Id,
                    (int) pkgCharge.get("service_sub_group_id"),
                    (int) pkgCharge.get("insurance_category_id"), bill.getVisitType(),
                    bill.getVisitId(), charge.getFirstOfCategory());

								contentCharge.setChargeId(packContChargeId);
								if (null != packageContent.get("consultation_type_id")) {
									contentCharge
											.setConsultation_type_id((int) packageContent.get("consultation_type_id"));
								}
								if (charge.getBillingGroupId() != null) {
									contentCharge.setBillingGroupId(charge.getBillingGroupId());
								}
								if (null != packageContent.get("operation_id")) {
									contentCharge.setOp_id((String) packageContent.get("operation_id"));
								} else {
									contentCharge.setOp_id(null);
								}
								if (null != packageContent.get("panel_id")) {
									contentCharge.setPanelId((Integer) packageContent.get("panel_id"));
								} else {
									contentCharge.setPanelId(null);
								}

								if (null != pkgCharge.get("submission_batch_type")) {
									contentCharge
											.setSubmissionBatchType((String) pkgCharge.get("submission_batch_type"));
								}

								if (pkgCharge.get("code_type") != null) {
									contentCharge.setCodeType((String) packageContent.get("code_type"));
								}
								contentCharge.setActRatePlanItemCode((String) packageContent.get("ct_code"));
								if (null != packageContent.get("bed_id")) {
									contentCharge.setActDescriptionId(String.valueOf(packageContent.get("bed_id")));
								}
								contentCharge.setAllowRateIncrease((Boolean) charge.isAllowRateIncrease());
								contentCharge.setAllowRateDecrease((Boolean) charge.isAllowRateDecrease());
								contentCharge.setAllowDiscount((Boolean) charge.isAllowDiscount());
								contentCharge.setPackageId(packageId);
								contentCharge.setActRemarks(actRemarks);

								if (contentCharge.getItemExcludedFromDoctor() != null) {
									contentCharge.setItemExcludedFromDoctor(charge.getItemExcludedFromDoctor());
								}
								String allowZeroClaimfor = (String) packageContent.get("allow_zero_claim_amount");
								if ("I".equals(pkgCharge.get("submission_batch_type"))
										&& (bill.getVisitType().equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor))) {
									contentCharge.setAllowZeroClaim(true);
								}
								contentCharge.setChargeRef(charge.getChargeId());
								contentCharge.setBillNo(charge.getBillNo());
								contentCharge.setPackageId(packageId);
								contentCharge.setVisitId(creditBillForm.getVisitId());
								contentCharge.setVisitType(bill.getVisitType());
								contentCharge.setInsuranceAmt(planIds, (String) visitBean.get("visit_type"),
										contentCharge.getFirstOfCategory());
								contentCharge.setUsername(userid);
								contentCharge.setConducting_doc_mandatory(charge.getConducting_doc_mandatory());
								insertBillChargeList.add(contentCharge);
								charge.setCashRate(OrderBO.getCashRate(contentCharge, centerId,
										(String) visitBean.get("bed_type")));
								insertBillChargeTaxList = billChargeTaxBO.getBillChargeTaxBeans(con, contentCharge);
								insertBillChargeTaxMap.put(contentCharge.getChargeId(), insertBillChargeTaxList);
								chargeInc++;
							}

						}
					}
					// Charges added in the UI other than Package Contents
					charge.setOp_id(creditBillForm.getOp_id()[i]);
					charge.setFrom_date(creditBillForm.getFrom_date()[i] == null ? null
							: DateUtil.parseTimestamp(creditBillForm.getFrom_date()[i]));
					charge.setTo_date(creditBillForm.getTo_date()[i] == null ? null
							: DateUtil.parseTimestamp(creditBillForm.getTo_date()[i]));
					charge.setChargeRef(creditBillForm.getChargeRef()[i]);
					charge.setActDepartmentId(creditBillForm.getDepartmentId()[i]);
					charge.setActDescription(creditBillForm.getDescription()[i]);
					charge.setActDescriptionId(creditBillForm.getDescriptionId()[i]);
					charge.setActItemCode(creditBillForm.getActItemCode()[i]);
					charge.setCodeType(creditBillForm.getCodeType()[i]);
					charge.setActUnit(creditBillForm.getUnits()[i]);
					charge.setUsername(userid);
					charge.setOriginalRate(creditBillForm.getOriginalRate()[i]);
					charge.setHasActivity(false);
					charge.setVisitId(creditBillForm.getVisitId());
					charge.setVisitType(bill.getVisitType());
					charge.setInsuranceAmt(planIds, (String) visitBean.get("visit_type"), charge.getFirstOfCategory());
					if (PACKAGE_CHARGEHEAD.equals(chargeHead)) {
						charge.setActRate(chargeValue);
						charge.setOriginalRate(chargeValue);
						charge.setDiscount(chargeDiscount);
						charge.setAmount(chargeValue.subtract(chargeDiscount));
						charge.setInsuranceCategoryId(0);
						charge.setSubmissionBatchType(submisionBatchType);
					}
					insertBillChargeList.add(charge);

					charge.setCashRate(OrderBO.getCashRate(charge, centerId, (String) visitBean.get("bed_type")));
					if (PACKAGE_CHARGEHEAD.equals(chargeHead)) {
						insertBillChargeTaxList = billChargeTaxBO.getBillChargeTaxBeans(con, charge);
					} else {
						insertBillChargeTaxList = getBillChargeTaxList(charge, new Integer(i), request);
					}
					insertBillChargeTaxMap.put(charge.getChargeId(), insertBillChargeTaxList);
				} else {
					// this is an existing charge being updated, we only set values
					// that can be changed in the UI, ignoring the rest.
					
					updateBillChargeList.add(charge);
					
					updateBillChargeTaxList = getBillChargeTaxList(charge,new Integer(i),request);
					
					updateBillChargeTaxMap.put(charge.getChargeId(), updateBillChargeTaxList);
				}
			}


			// If User has no counter access then patient payment is unpaid even though deposits set off have been done.
			if (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
				if (!bpImpl.validateUserCounter(AbstractPaymentDetails.BILL_PAYMENT)) {
					bill.setPaymentStatus(Bill.BILL_PAYMENT_UNPAID);
				}
			}
			/*
			 * Build receipts if any payments done.
			 */

			receiptList = bpImpl.processReceiptParams(requestParams);

			/*
			 * Discharge Details
			 */
			if (bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)) {
				if (ipModAct.equalsIgnoreCase("Y") && bill.getVisitType().equals(Bill.BILL_VISIT_TYPE_IP)) {
					// We leave ADT to do the actual discharge
					// but we can set the discharge date from here if OK to discharge is set.
					if (bill.getOkToDischarge().equals("Y"))
						bill.setDischarge("D");
				} else {
					if (origDischargeStatus.equals("N") && bill.getOkToDischarge().equals("Y")) {
						// Status changed from not discharged to discharged: do the discharge
						bill.setDischarge("Y");
					} else if (bill.getOkToDischarge().equals("Y")) {
						// already discharged, update the date alone
						bill.setDischarge("D");
					} else {
						// not discharged or discharging: do nothing
						bill.setDischarge("N");
					}
				}
				// in either case, udpate the discharge date if it is given
				bill.setDisDate(creditBillForm.getDischargeDate());
				bill.setDisTime(creditBillForm.getDischargeTime());
			}

            //Setting bill signature when bill status is Finalised
			if (billSignature != null && !billSignature.isEmpty()) {
                bill.setBillSignature(billSignature);
			}

            //Setting empty when bill status is Cancelled.
            if (creditBillForm.getBillStatus().equals(Bill.BILL_STATUS_CANCELLED)) {
                bill.setBillSignature("");				
			}
			/*
			 * Update bill details
			 */
			boolean success = billBOObj.updateBillDetails(bill, newStatus, updateBillChargeList,
					insertBillChargeList, receiptList, updateDynaPkg, drgCodeMap, billHasPlanVisitCopayLimit, insertBillChargeTaxMap, updateBillChargeTaxMap);
			String mrNo = null;
			if (visitBean != null)
				 mrNo = (String) visitBean.get("mr_no");
			BasicDynaBean billAmounts = billDao.getTotalBillAmounts(bill.getVisitId());
			if (mrNo != null) {
				Map<String, Object> patientDataMap = billBOObj.getFinancialDischargeTokens(bill, mrNo, billAmounts);
				
				if (!patientDataMap.isEmpty()) {
					boolean dischargeStatus = (Boolean)patientDataMap.get("financial_discharge_status");
					boolean isSuccess = success && dischargeStatus && MessageUtil.allowMessageNotification(request, "general_message_send");
					patientDataMap.put("lang_code", PatientDetailsDAO.getContactPreference(mrNo));

					if (isSuccess && bill.getVisitType().equals("i")) {
						MessageManager mgr = new MessageManager();
						patientDataMap.put("recipient_mobile", patientDataMap.get("patient_phone"));
						patientDataMap.put("visit_id", bill.getVisitId());
			            mgr.processEvent("patient_on_discharge",patientDataMap);
					}
					if (isSuccess &&  bill.getVisitType().equals("i")) {
						MessageManager mgr = new MessageManager();
						patientDataMap.put("recipient_mobile", nokContact);
						patientDataMap.put("visit_id", bill.getVisitId());
			            mgr.processEvent("inform_nok_on_patient_discharge",patientDataMap);
					}
				}
			}

			if(success && null != planIds && planIds.length > 0 && bill.getIs_tpa()){
				BillChargeClaimTaxDAO chargeClaimTaxDAO = new BillChargeClaimTaxDAO();
				Connection con = null;
				boolean succ = true;
				try{
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);
					if(bill.getIs_tpa()) {
						billChargeClaimDao.insertBillChargeClaims(con, insertBillChargeList, planIds,
							(String)visitBean.get("patient_id"), bill.getBillNo());
					}
					billChargeClaimDao.updateBillChargeClaims(con,updateBillChargeList,bill.getVisitId(),bill.getBillNo(),planIds,true);
					billChargeClaimDao.cancelBillChargeClaims(con,updateBillChargeList);
					chargeClaimTaxDAO.cancelBillChargeClaimTax(con, updateBillChargeList);
					
					if(bill.getDynaPkgId() != 0){
						Map<String,Object> keys = new HashMap<String, Object>();
						keys.put("bill_no", bill.getBillNo());
						keys.put("charge_head", "MARPKG");
						
						BasicDynaBean pkgMarginBean = billChargeDao.findByKey(con, keys);
						
						ChargeDAO chargeDao = new ChargeDAO(con);
						ChargeDTO dynapkgCharge = chargeDao.getCharge((String)pkgMarginBean.get("charge_id"));
						dynapkgCharge.setIsClaimLocked(true);
	
						chargeDao.updateChargeAmounts(dynapkgCharge);

						billChargeClaimDao.updatepackageMarginInBillChgClaim(con, pkgMarginBean);
					}

				}finally{
					DataBaseUtil.commitClose(con, succ);
					BillDAO.resetSponsorTotals(bill.getBillNo());
					//new BillAdjustmentCalculator().postBillAdjustmentEntry(bill.getBillNo());
				}
				
				if(succ){
					if(drgCodeMap != null && drgCodeMap.get("drg_charge_id") != null && null != drgCodeMap.get("drg_code")){
						new DRGCalculator().processDRG(bill.getBillNo(), (String)drgCodeMap.get("drg_code"));
					}
				}
			}

			String billAdjAlertsErrorMsgs = "";
			String patientId= bill.getVisitId();
			if(request.getParameter("screenId") == null || (!"view_consolidated_bill".equals(request.getParameter("screenId"))) ){/* this code is not required for rate update from consolidated bill screen*/
				if(success && bill.getIs_tpa()) {
					if(!origStatus.equals(newStatus)){
						if(newStatus.equals(Bill.BILL_STATUS_FINALIZED) || newStatus.equals(Bill.BILL_STATUS_CLOSED))
							sponsorDAO.lockBillCharges(bill.getBillNo());
					}
					if(removedDynaPkg) {
					  sponsorDAO.unlockVisitBillsCharges(patientId);
            sponsorDAO.unlockVisitSaleItems(patientId);
            sponsorDAO.includeBillChargesInClaimCalc(patientId);
					}
					
					sponsorBO.recalculateSponsorAmount(bill.getVisitId());
					
					if(removedDynaPkg) {
					  sponsorDAO.setIssueReturnsClaimAmountTOZero(patientId);
					  sponsorDAO.insertOrUpdateBillChargeTaxesForSales(patientId);
            sponsorDAO.lockVisitSaleItems(patientId);
            sponsorDAO.updateSalesBillCharges(patientId);
            sponsorDAO.updateTaxDetails(patientId);
          }
					
					try(Connection con = DataBaseUtil.getConnection()){
					  billAdjAlertsErrorMsgs = setBillAdjustmentAlerts(con,bill.getVisitId());
					}
					
					if(bill.getDynaPkgId() != 0){
						updatePkgMarginSponsorAmount(bill);
					}
				}
			}
			if (!success) break;


			/*
			 * Status update
			 */
			if (!origStatus.equals(newStatus)) {
				java.sql.Timestamp finalizedDate = null;
				// set finalized date if origStatus is open, and new status is anything other
				// than open, ie, open->finalized, open->closed, open->canceled.
				if (origStatus.equals(Bill.BILL_STATUS_OPEN) && !newStatus.equals(Bill.BILL_STATUS_OPEN)) {
					if (actionBillFinalizedDate.equalsIgnoreCase("A") || roleID.equals(1) || roleID.equals(2)) {
						finalizedDate = DateUtil.parseTimestamp(
								creditBillForm.getFinalizedDate(),creditBillForm.getFinalizedTime());
					}
				} else {
					finalizedDate = DateUtil.parseTimestamp(
							creditBillForm.getFinalizedDate(),creditBillForm.getFinalizedTime());
				}
				if (finalizedDate == null) {
					finalizedDate = bill.getFinalizedDate() == null ?  DateUtil.getCurrentTimestamp() :
						new java.sql.Timestamp(bill.getFinalizedDate().getTime());
				}

				// Set the finalized by when the new status is finalized or closed
				// (And) the Bill is a Credit bill or Bill now with TPA (And) the finalized by is empty.
				if ((newStatus.equals(Bill.BILL_STATUS_FINALIZED)
						|| (newStatus.equals(Bill.BILL_STATUS_CLOSED)
							&& (bill.getBillType().equals(Bill.BILL_TYPE_CREDIT)
									|| (bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) && bill.getIs_tpa()))))
						&& (bill.getFinalizedBy() == null || bill.getFinalizedBy().equals("")))
					bill.setFinalizedBy(userid);

				String  dischargeStatus = creditBillForm.getOkToDischarge();
				if (dischargeStatus == null) {
					dischargeStatus = bill.getBillType().equals(Bill.BILL_TYPE_PREPAID) &&
						bill.getPaymentStatus().equals("P") ? "Y" : "N" ;
				}

				boolean paymentForceClose = request.getParameter("paymentForceClose") != null &&
							request.getParameter("paymentForceClose").equals("Y");

				boolean claimForceClose = request.getParameter("claimForceClose") != null &&
							request.getParameter("claimForceClose").equals("Y");

				error = new BillBO().updateBillStatus(bill, newStatus, bill.getPaymentStatus(),
						dischargeStatus, finalizedDate, userid, paymentForceClose, claimForceClose);
			}
			
			if("false".equals(request.getParameter("eClaimModule"))){
				String primaryClaimStatus=request.getParameter("primaryClaimStatus");
				String secondaryClaimStatus=request.getParameter("secondaryClaimStatus");
				Connection con = null;
				try{
					con = DataBaseUtil.getConnection();
					con.setAutoCommit(false);
						BasicDynaBean bean=billClaimDao.getPrimaryBillClaim(con,bill.getBillNo());
						if(bean!=null && (null!=primaryClaimStatus ||"C".equalsIgnoreCase(request.getParameter("priClaimStatusCheck")))){
							String claim_id=(String) bean.get("claim_id");
							primaryClaimStatus=primaryClaimStatus!=null?primaryClaimStatus:request.getParameter("priClaimStatusCheck");
							Map<String,Object> columndata = new HashMap<String, Object>();
							columndata.put("status",primaryClaimStatus);	
							insuranceClaimDao.update(con, columndata,"claim_id",claim_id);
						}
						
						bean=billClaimDao.getSecondaryBillClaim(con,bill.getBillNo());
						if(bean!=null && (null!=secondaryClaimStatus ||"C".equalsIgnoreCase(request.getParameter("secClaimStatusCheck")))){
							String claim_id=(String) bean.get("claim_id");	
							secondaryClaimStatus=secondaryClaimStatus!=null?secondaryClaimStatus:request.getParameter("secClaimStatusCheck");
							Map<String,Object> columndata = new HashMap<String, Object>();
							columndata.put("status",secondaryClaimStatus);	
							insuranceClaimDao.update(con, columndata,"claim_id",claim_id);
						}
					
					
				}finally{
					DataBaseUtil.commitClose(con, success);
				}
			}
			
			

			if (error == null || error.equals("")) {
				error = "";
				if (hasDRGCode && (!newStatus.equals(Bill.BILL_STATUS_CLOSED) && !newStatus.equals(Bill.BILL_STATUS_CANCELLED)))
					flash.put("info", "Bill has DRG Code. Charges are processed to calculate DRG Payment.");
			}
			else if(error.startsWith("Bill status")) {}
			else error = error + "<br/> Please modify the discount amount and finalize /close the bill again.";


			if(!billAdjAlertsErrorMsgs.equals("")){
				if(error == null || error.equals("")){
					error = billAdjAlertsErrorMsgs;
				}else{
					error = error + "<br/>"+billAdjAlertsErrorMsgs;
				}
			}

		} while (false);		// dummy loop to enable breaks in the middle.
		
		if ((receiptList != null && receiptList.size() > 0) || (isPaymentByDeposit || isPaymentByRewardPoints)) {
			String printerTypeStr = request.getParameter("printType");
			String customTemplate = request.getParameter("printBill"); // this you will get null if user is not having bill print rights.

			if (customTemplate != null && !customTemplate.equals("")) {
				printParamMap = new HashMap();
				printParamMap.put("printerTypeStr", printerTypeStr);
				printParamMap.put("customTemplate", customTemplate);
				if (isPaymentByDeposit || isPaymentByRewardPoints) {
					printParamMap.put("isPaymentByDeposit", isPaymentByDeposit);
					printParamMap.put("isPaymentByRewardPoints", isPaymentByRewardPoints);
					printParamMap.put("billNo", creditBillForm.getBillNo());
				}
				List<String> printURLs = bpImpl.generatePrintReceiptUrls(receiptList, printParamMap);
				request.getSession(false).setAttribute("printURLs", printURLs);
			}
		}
		Map<String, String> billData =null;
				if(MessageUtil.allowMessageNotification(request,"general_message_send")) {
				if(!bill.getRestrictionType().equalsIgnoreCase(Bill.BILL_RESTRICTION_PHARMACY) && 
						newStatus != null && (newStatus.equalsIgnoreCase(Bill.BILL_STATUS_CLOSED) || newStatus.equalsIgnoreCase(Bill.BILL_STATUS_FINALIZED))) {
						BigDecimal netPatientDue = BillDAO.getNetPatientDue(bill.getBillNo());
					    BigDecimal patientDue = BillDAO.getPatientDue(bill.getBillNo());
				        if(netPatientDue.floatValue() <= 0 || patientDue.floatValue() == 0) {
					       billData = getBillData(bill.getBillNo());
					        BigDecimal givendis=new BigDecimal(billData.get("total_discount"));
							if (Float.valueOf(billData.get("total_discount")) > 0) {
								// sending discount related SMS to management
								BigDecimal ratePlanTotalDiscount = BillDAO.getRatePlanTotalDiscount(bill.getBillNo())==null? BigDecimal.ZERO : BillDAO.getRatePlanTotalDiscount(bill.getBillNo());
								if (givendis.compareTo(ratePlanTotalDiscount) > 0) {
									String authId = billData.get("discount_auth");
									if (!(authId == null || authId.equals("") || authId.equals("0"))) {
										billData.put("authorizer_name","authorized by "+ billData.get("disc_auth_name"));
									} else
										billData.put("authorizer_name", "");
									Calendar cal = Calendar.getInstance();
									String discountDate = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(cal.getTime());
									billData.put("date_time", discountDate);
									MessageManager discountMgr = new MessageManager();
									String currency = GenericPreferencesDAO.getGenericPreferences().getCurrencySymbol();
									billData.put("currency_symbol", currency);
									scheduleBillMessage("discount_given", userName, billData);
								}
							}
					        StringWriter writer = new StringWriter();//"BUILTIN_HTML"
					        String template=null;
							if(bill.getBillType().equals("P")){ // Bill Now
								template = GenericPreferencesDAO.getGenericPreferences().getEmailBillNowTemplate();
							}
							else{  // Bill Later
								template = GenericPreferencesDAO.getGenericPreferences().getEmailBillLaterTemplate();
							}
							String[] templateName = template.split("-");
		    		        String[] returnVals = BillPrintHelper.processBillTemplate(writer, bill.getBillNo(), templateName[1], userid);
		        	        String report = writer.toString();
		        	        billData.put("_report_content", report);
		        	        billData.put("_message_attachment", report);

		        	        billData.put("message_attachment_name", "Bill_"+bill.getBillNo());
		        	        billData.put("printtype",String.valueOf(GenericPreferencesDAO.getGenericPreferences().getEmailBillPrint()));
		        	        billData.put("category", "Bill");
		        	        if(bill.getBillType().equalsIgnoreCase(Bill.BILL_TYPE_CREDIT)) {
		        	            billData.put("bill_date", billData.get("finalized_date"));
		        	        }
		        	        if(bill.getBillType().equalsIgnoreCase(Bill.BILL_TYPE_PREPAID)) {
		        	            billData.put("bill_date", billData.get("closed_date"));
		        	        }		        	    
							billData.put("recipient_name", billData.get("patient_name"));
							billData.put("recipient_phone", billData.get("patient_phone"));
							billData.put("recipient_mobile", billData.get("patient_phone"));
							billData.put("bill_amount", billData.get("total_amount"));
							billData.put("receipient_id__", billData.get("mr_no"));
							billData.put("receipient_type__", "PATIENT");

							if (bill.getVisitType().equalsIgnoreCase("o")) {
								if (bill.getBillType().equalsIgnoreCase(Bill.BILL_TYPE_PREPAID) && bill.getIs_tpa() == false && patientDue.intValue() == 0) {
									scheduleBillMessage("op_bn_cash_bill_paid_closed", userName, billData);
								}
								if (netPatientDue.floatValue() <= 0) {
									scheduleBillMessage("op_bill_paid", userName, billData);
									scheduleOPDiagReportPHR(bill.getBillNo());
								}
							}
							if (bill.getVisitType().equalsIgnoreCase("i")) {
								if (netPatientDue.floatValue() <= 0) {
									scheduleBillMessage("ip_bill_paid", userName, billData);
								}
							}
				   }
				}	
		}
				

		if(request.getParameter("screenId") == null || (!"view_consolidated_bill".equals(request.getParameter("screenId"))) ){/* this code is not required for rate update from consolidated bill screen*/
		/* Sending Payment received SMS */
		if(MessageUtil.allowMessageNotification(request,"general_message_send")) {
			MessageManager mgr = new MessageManager();
			Map<String, String> smsBillData = billData;
			if(smsBillData==null){
				smsBillData = getBillData(creditBillForm.getBillNo());
			}else{
				smsBillData.remove("_message_attachment");
				smsBillData.remove("_report_content");
			}
			try {
				BigDecimal totAmtPaid = BigDecimal.ZERO;
				BigDecimal totAmtRefund = BigDecimal.ZERO;
				BigDecimal totAdvanceAmtPaid = BigDecimal.ZERO;
				int l = creditBillForm.getTotPayingAmt() != null? creditBillForm.getTotPayingAmt().length : 0;
				String[] paymentType = creditBillForm.getPaymentType();
				for (int i = 0; i < l; i++) {
					if(creditBillForm.getTotPayingAmt()[i]==null){
						break;
					}
					if (paymentType[i].equalsIgnoreCase(Receipt.REFUND)) {
						totAmtPaid = totAmtPaid.subtract(creditBillForm.getTotPayingAmt()[i]);
						totAmtRefund = totAmtRefund.add(creditBillForm.getTotPayingAmt()[i]);
					}
					if (paymentType[i].equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_ADVANCE)
							|| paymentType[i].equalsIgnoreCase(Receipt.PRIMARY_SPONSOR_SETTLEMENT)
							|| paymentType[i].equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_ADVANCE)
							|| paymentType[i].equalsIgnoreCase(Receipt.SECONDARY_SPONSOR_SETTLEMENT)){
						continue;
					}
					else{
						totAmtPaid = totAmtPaid.add(creditBillForm.getTotPayingAmt()[i]);
						if (paymentType[i].equalsIgnoreCase(Receipt.PATIENT_ADVANCE))
							totAdvanceAmtPaid = totAdvanceAmtPaid.add(creditBillForm.getTotPayingAmt()[i]);
					}
				}

				if(totAmtRefund.compareTo(BigDecimal.ZERO) > 0){
            String amtRefund = totAmtRefund.toString();
            smsBillData.put("refund_amount", amtRefund);
            BigDecimal patientDue = BillDAO.getPatientDue(bill.getBillNo());
            String patientDueStr=patientDue.toString();
            smsBillData.put("patient_due", patientDueStr);
            
            String messageFooterToken = "SELECT message_footer from message_types WHERE message_type_id = 'sms_bill_refund'";
            String messageFooterTokenvalue = DataBaseUtil.getStringValueFromDb(messageFooterToken);
            smsBillData.put("message_footer", messageFooterTokenvalue);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar currDate = Calendar.getInstance();
            String currentDate = dateFormat.format(currDate.getTime());
            smsBillData.put("refund_date", currentDate);
            smsBillData.put("recipient_name", smsBillData.get("patient_name"));
            smsBillData.put("recipient_phone", smsBillData.get("patient_phone"));
            smsBillData.put("receipient_id__", smsBillData.get("mr_no"));
            smsBillData.put("receipient_type__", "PATIENT");
            smsBillData.put("lang_code",
                PatientDetailsDAO.getContactPreference(smsBillData.get("mr_no")));
            String currency = GenericPreferencesDAO.getGenericPreferences().getCurrencySymbol();
            smsBillData.put("currency_symbol", currency);
            if(!bill.getRestrictionType().equalsIgnoreCase(Bill.BILL_RESTRICTION_TEST)) {
              scheduleBillMessage("bill_refund_message", userName, smsBillData);
            }
          }
				if(totAmtPaid.compareTo(BigDecimal.ZERO) > 0){
				String amtPaid = totAmtPaid.toString();
				smsBillData.put("amount_paid", amtPaid);
				String advanceAmtmtPaid = totAdvanceAmtPaid.toString();
				smsBillData.put("advance_amount_paid", advanceAmtmtPaid);
				String messageFooterToken = "SELECT message_footer from message_types WHERE message_type_id = 'sms_bill_payment_received'";
				String messageFooterTokenvalue = DataBaseUtil.getStringValueFromDb(messageFooterToken);
				smsBillData.put("message_footer", messageFooterTokenvalue);
	
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Calendar currDate = Calendar.getInstance();
				String currentDate = dateFormat.format(currDate.getTime());
				smsBillData.put("payment_date", currentDate);
				BigDecimal patientDue = BillDAO.getPatientDue(bill.getBillNo());
				String patientDueStr=patientDue.toString();
				smsBillData.put("patient_due", patientDueStr);
				smsBillData.put("recipient_name", smsBillData.get("patient_name"));
				smsBillData.put("recipient_phone", smsBillData.get("patient_phone"));
		        smsBillData.put("receipient_id__", smsBillData.get("mr_no"));
		        smsBillData.put("receipient_type__", "PATIENT");
		        smsBillData.put("lang_code", PatientDetailsDAO.getContactPreference(smsBillData.get("mr_no")));
		        String currency=GenericPreferencesDAO.getGenericPreferences().getCurrencySymbol();
		        smsBillData.put("currency_symbol", currency);
				String patientMobileNo = smsBillData.get("recipient_phone");

				if (!bill.getRestrictionType().equalsIgnoreCase(Bill.BILL_RESTRICTION_TEST)) {
					smsBillData.put("recipient_mobile", patientMobileNo != null ? patientMobileNo.trim() : null);
					scheduleBillMessage("bill_payment_message", userName, smsBillData);
				} else {
					log.info("Patient mobile # not available, skipping SMS on admission");
				}
				//// sending SMS to owner if patient makes an advance payment
				if(totAdvanceAmtPaid.compareTo(BigDecimal.ZERO)>0 && !bill.getRestrictionType().equalsIgnoreCase(Bill.BILL_RESTRICTION_PHARMACY) && !bill.getRestrictionType().equalsIgnoreCase(Bill.BILL_RESTRICTION_TEST)){
					smsBillData.remove("recipient_mobile");
					scheduleBillMessage("advance_paid", userName, smsBillData);
				}
				}
				
			} catch (ParseException e) {
				log.error(e.getMessage(), e);
			} catch (Exception e) {
			  log.error(e.getMessage(), e);
			}
		}
		}
		
		if(request.getParameter("patientDueSMS")!=null && request.getParameter("patientDueSMS").equals("Y") 
				&& MessageUtil.allowMessageNotification(request, "general_message_send")){
			Map<String, String> patientDueSMSData=new HashMap<>();
			patientDueSMSData.put("visit_id",bill.getVisitId());
			MessageManager mgr = new MessageManager();
			mgr.processEvent("patient_due_for_visit",patientDueSMSData);
			}
		
		if(request.getParameter("screenId") !=null && "view_consolidated_bill".equals(request.getParameter("screenId"))){
			Connection con = null;
			boolean succ = true;
			try{
				con = DataBaseUtil.getConnection();
				con.setAutoCommit(false);
				BasicDynaBean visitBean = visitDetailsDao.findByKey(con , "patient_id", bill.getVisitId());
				String mvisitid = (String)visitBean.get("main_visit_id");
				davitaSponsorDao.recalculatePreviousVisitItems(con , mvisitid == null ? bill.getVisitId() : mvisitid);
				davitaSponsorDao.calculate(con , visitBean);
			}finally{
				DataBaseUtil.commitClose(con, succ);
			}
		}
		
		// Update the bill total amount.
		allocationService.updateBillTotal(bill.getBillNo());
		
		if (receiptList != null && receiptList.size() > 0) {
		  // set lock for the bill, lock will be released once allocation job is done
		  // during the lock, we shall restrict to save cancelled charges or refunding receipts.
		  try(Connection con = DataBaseUtil.getConnection()) {
		    Map<String,Object> columndata = new HashMap<String, Object>();
		    columndata.put("locked", Boolean.TRUE);
		    billDao.update(con, columndata , "bill_no",  bill.getBillNo());
		  }
		}
		// Call the Allocation method, instead of job.
		allocationService.allocate(bill.getBillNo(), centerId);
		
		Connection con = null;
		try{
  		con = DataBaseUtil.getConnection();
  		con.setAutoCommit(false);
      ReceiptRelatedDAO receiptDAO = new ReceiptRelatedDAO(con);
      BigDecimal depositSetOffTax = receiptDAO.getTotalTaxSetOffAmount(con,bill.getMrno());
      BasicDynaBean bean = depositTotalSetOffs.getBean();
      bean.set("hosp_total_setoffs_tax_amount", depositSetOffTax);
      Map updateKeys = new HashMap();
      updateKeys.put("mr_no", bill.getMrno());
      depositTotalSetOffs.update(con, bean.getMap(), updateKeys);
		} finally{
		  DataBaseUtil.commitClose(con, true);
		}
    
    	
    String originalBillStatus = bill.getStatus();
    String newBillStatus = newStatus;
    // We need to post accounting when bill status changed from
    // open->finalized, open->closed, saves on finalized state
    if (!(action.equals("reopen"))) {
      if ((originalBillStatus.equals(Bill.BILL_STATUS_OPEN) 
          && newBillStatus != null 
          && (newBillStatus.equals(Bill.BILL_STATUS_FINALIZED)
              || newBillStatus.equals(Bill.BILL_STATUS_CLOSED)))) {
        accountingJobScheduler.scheduleAccountingForBill(bill.getVisitId(), bill.getBillNo());
      }

      // If finalized bill or open bill is cancelled then we need to schedule only reversals
      if ((originalBillStatus.equals(Bill.BILL_STATUS_OPEN) || originalBillStatus
          .equals(Bill.BILL_STATUS_FINALIZED)) 
          && (newBillStatus != null && newBillStatus.equals(Bill.BILL_STATUS_CANCELLED))) {
        accountingJobScheduler.scheduleAccountingForBillReversalsOnly(bill.getVisitId(),
            bill.getBillNo());
      }
    }

		/* forwarding to same page with new path */
		flash.put("error", error);

		ActionRedirect redirect = new ActionRedirect( mapping.findForward("savedetails"));
		redirect.addParameter("billNo", request.getParameter("billNo"));
		redirect.addParameter("isNewUX", isNewUX);
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		if(request.getParameter("screenId") !=null && "view_consolidated_bill".equals(request.getParameter("screenId"))){
			redirect.addParameter("screenId", request.getParameter("screenId"));
		}
		return redirect;
	}
  private void scheduleOPDiagReportPHR(String billNo) throws SQLException, ParseException, IOException {	
		String contextPath = RequestContext.getRequest()
				.getServletContext().getRealPath("");
		Map<String, Object> jobData = new HashMap<>();
		jobData.put("billNo", billNo);
		jobData.put("forceResend", "Block");
		jobData.put("path", contextPath);
		MessageManager mgr = new MessageManager();
		mgr.processEvent("op_phr_diag_share", jobData);

	}

	private List<BasicDynaBean> getBillChargeTaxList(ChargeDTO charge, Integer itemIdx,
			HttpServletRequest request) throws SQLException{
		
		List<BasicDynaBean> billChargeTaxList = new ArrayList<BasicDynaBean>();
		String prefix = charge.getChargeId().startsWith("_") ? itemIdx.toString() : charge.getChargeId();
		String[] subGroupIds = request.getParameterValues(prefix+"_sub_group_id");
		String[] itemTaxAmts = request.getParameterValues(prefix+"_tax_amt");
		String[] itemTaxRates = request.getParameterValues(prefix+"_tax_rate");
		String[] taxChargeId = request.getParameterValues(prefix+"_charge_tax_id");
		
		if(null != subGroupIds){
			for(int i=0; i<subGroupIds.length; i++){
				BasicDynaBean taxBean = billChargeTaxDao.getBean();
				int subGrpId = Integer.parseInt(subGroupIds[i]);
				BigDecimal itemtaxAmt = new BigDecimal(itemTaxAmts[i]);
				BigDecimal itemTaxRate = new BigDecimal(itemTaxRates[i]);
				taxBean.set("charge_id", charge.getChargeId());
				taxBean.set("tax_sub_group_id", subGrpId);
				taxBean.set("tax_rate", itemTaxRate);
				if(null != taxChargeId && taxChargeId[i] != null && !taxChargeId[i].isEmpty())
					taxBean.set("charge_tax_id", Integer.parseInt(taxChargeId[i]));
				else
					taxBean.set("charge_tax_id", 0);
				
				if(charge.getStatus().equals(ChargeDTO.CHARGE_STATUS_CANCELLED))
					taxBean.set("tax_amount", BigDecimal.ZERO);
				else
					taxBean.set("tax_amount", itemtaxAmt);
				
				billChargeTaxList.add(taxBean);
			}
		}
		
		return billChargeTaxList;
	}

	private void updatePkgMarginSponsorAmount(Bill bill) throws SQLException, IOException{
		String billNo = bill.getBillNo();
		BasicDynaBean billBean = billDao.findByKey("bill_no", billNo);
		String visitId = (String)billBean.get("visit_id");
		Map<String,Object> keys = new HashMap<String, Object>();
		keys.put("patient_id", visitId);
		keys.put("priority", 1);
		BasicDynaBean visitplanBean = new GenericDAO("patient_insurance_plans").findByKey(keys);
		BigDecimal approvalLimit = (null != visitplanBean && null != visitplanBean.get("visit_limit")) ? (BigDecimal)visitplanBean.get("visit_limit") : BigDecimal.ZERO;
		BigDecimal totalClaim = ((BigDecimal)billBean.get("total_claim")).add((BigDecimal)billBean.get("total_claim_tax"));
		BigDecimal dynaPkgCharge = null !=  billBean.get("dyna_package_charge") ? (BigDecimal)billBean.get("dyna_package_charge") : BigDecimal.ZERO;
		
		if(approvalLimit.compareTo(BigDecimal.ZERO) != 0 && dynaPkgCharge.compareTo(approvalLimit) > 0
				&& totalClaim.compareTo(approvalLimit) > 0){
			BigDecimal diffAmount = totalClaim.subtract(approvalLimit); 
			
			Map<String,Object> marKeys = new HashMap<String, Object>();
			marKeys.put("bill_no", bill.getBillNo());
			marKeys.put("charge_head", "MARPKG");
			BasicDynaBean pkgMarginBean = billChargeDao.findByKey(marKeys);
			BigDecimal marginClaimAmt = (BigDecimal)pkgMarginBean.get("insurance_claim_amount");
			if(marginClaimAmt.compareTo(diffAmount) >= 0){
				marginClaimAmt = marginClaimAmt.subtract(diffAmount);
				pkgMarginBean.set("insurance_claim_amount", marginClaimAmt);
				billChargeClaimDao.updatepackageMarginInBillChgClaim(pkgMarginBean);
			}
		}
	}

	private String setBillAdjustmentAlerts(Connection con, String visitId) throws SQLException{
		Map<String,Map<String,String>> adjMap = new HashMap<String, Map<String,String>>();

		BasicDynaBean visitBean = VisitDetailsDAO.getVisitDetails(con,visitId);
		String opType = (String)visitBean.get("op_type");
		
    if (opType.equals("F") || opType.equals("D")) {
      if (isPlanIncludesFollowUpVisits(visitId)) {
        visitId = (String) visitBean.get("main_visit_id");
      }
    }

		adjMap = new BillBO().getBillAdjustmentAlerts(con, visitId);

		String billAdjAlertsErrorMsgs = "";

		for(String key : adjMap.keySet()){
			Map<String,String> m = adjMap.get(key);
			if(m.get("sponsor")!=null)
				billAdjAlertsErrorMsgs = "For "+m.get("sponsor")+" Sponsor, Below rules are not adjusted..</br>";
			for(String mkey : m.keySet()){
				if(!mkey.equals("sponsor")){
					if(!key.equals("visitRules"))
						billAdjAlertsErrorMsgs = billAdjAlertsErrorMsgs + mkey + " Rule could not be adjusted for "+m.get(mkey)+" </br>";
					else if(!m.get(mkey).equals(""))
						billAdjAlertsErrorMsgs = billAdjAlertsErrorMsgs + m.get(mkey) + "</br> ";
				}
			}
		}

		return billAdjAlertsErrorMsgs;

	}
	
  private Boolean isPlanIncludesFollowUpVisits(String visitId) throws SQLException {
    Boolean isPlanIncludesFollowupVisits = false;
    List<BasicDynaBean> planList = patientInsurancePlanDao.getPlanDetails(visitId);
    if (null != planList && planList.size() > 0) {
      for (BasicDynaBean planBean : planList) {
        int planId = (Integer) planBean.get("plan_id");
        BasicDynaBean bean = planDAO.findByKey("plan_id", planId);
        if (null != bean.get("limits_include_followup")) {
          String limitsIncludeFollowUp = (String) bean.get("limits_include_followup");

          isPlanIncludesFollowupVisits = isPlanIncludesFollowupVisits
              || limitsIncludeFollowUp.equals("Y");
        }
      }
    }
    return isPlanIncludesFollowupVisits;
  }

	private void setBillDepositSetOff(CreditBillForm creditBillForm, Bill bill, boolean ipDepositExists) throws SQLException {

		BasicDynaBean depositBean =  DepositsDAO.getDepositAmounts(creditBillForm.getMrNo());

		BigDecimal totalDepositAmt = BigDecimal.ZERO;
		BigDecimal billDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal totalDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal depositSetOffAmt = BigDecimal.ZERO;

		if (depositBean!= null && depositBean.get("hosp_total_deposits") != null)
			totalDepositAmt = (BigDecimal)depositBean.get("hosp_total_deposits");

		// Get the total deposit set off for the patient and the current bill deposit set off.
		BasicDynaBean billDepositBean = DepositsDAO.getDepositSetOffBillExcluded(
				creditBillForm.getMrNo(), creditBillForm.getBillNo());

		if (billDepositBean != null) {
			totalDepositSetOffAmt =
				billDepositBean.get("hosp_total_setoffs") != null ? (BigDecimal)billDepositBean.get("hosp_total_setoffs") : BigDecimal.ZERO;
			billDepositSetOffAmt =
				billDepositBean.get("deposit_set_off") != null ? (BigDecimal)billDepositBean.get("deposit_set_off") : BigDecimal.ZERO;
		}

		// bill deposit set off excluded
		depositSetOffAmt = totalDepositSetOffAmt.subtract(billDepositSetOffAmt);

		// calculate deposit remaining after the current bill set off is deducted.
		BigDecimal depositAmtRemaining = totalDepositAmt.subtract(depositSetOffAmt);

		// Set the bill deposit set off if the remaining set off is not negative.
		if (creditBillForm.getDepositSetOff() != null) {
			if (depositAmtRemaining.compareTo(BigDecimal.ZERO) > 0) {
				if (depositAmtRemaining.subtract(creditBillForm.getDepositSetOff())
						.compareTo(BigDecimal.ZERO) >= 0) {
					bill.setDepositSetOff(creditBillForm.getDepositSetOff());
				} else if(creditBillForm.getDepositSetOff().compareTo(BigDecimal.ZERO) > 0) {
					bill.setDepositSetOff(depositAmtRemaining);
				}
			}
			//insert in to patient_deposit_setoff for accounting info.
			patientDepositSetOffAdjustment(creditBillForm,bill,ipDepositExists,billDepositSetOffAmt);
		}
	}
	private void patientDepositSetOffAdjustment(CreditBillForm creditBillForm, Bill bill, boolean ipDepositExists,BigDecimal billDepositSetOffAmt) throws SQLException {
		
		BillBO billBOObj1 = new BillBO();
		Boolean multiVisitBill = billBOObj1.checkMultiVisitBill(creditBillForm.getBillNo(), bill.getVisitId());
		BasicDynaBean mvbean =  null ;
		if (multiVisitBill) {
		    billDao.getMultivisitPatientPackageDetails(creditBillForm.getBillNo(), bill.getVisitId());
		} 
		boolean ismultipkg=false;
		if(multiVisitBill && mvbean !=null){
			ismultipkg=true;
		}
		BigDecimal amount=BigDecimal.ZERO;
		if(bill.getVisitType().equals("i") && ipDepositExists){// deposit setoff for IP visit
			if(creditBillForm.getDepositType() != null){
				BigDecimal totDepositAvl = getTotalAvailableDeposit(creditBillForm);
				BigDecimal totIPDepsoitAvl = getTotalIPAvailableDeposit(creditBillForm);
				BigDecimal totGenDepositAvl = totDepositAvl.subtract(totIPDepsoitAvl);
				BigDecimal ipDepositBillSetOffAmt = BigDecimal.ZERO;
				BasicDynaBean billDeposit = DepositsDAO.getIPDepositSetOffBillExcluded(
						creditBillForm.getMrNo(), creditBillForm.getBillNo());
				if (billDeposit != null) {
					ipDepositBillSetOffAmt =
								billDeposit.get("ip_deposit_set_off") != null ? (BigDecimal)billDeposit.get("ip_deposit_set_off") : BigDecimal.ZERO;
				}
				BigDecimal genBillDeposit=billDepositSetOffAmt.subtract(ipDepositBillSetOffAmt);
				if(creditBillForm.getDepositType().equals("i")){// deposit setoff for IP visit & IP deposits
					
					if(creditBillForm.getDepositSetOff().compareTo(totIPDepsoitAvl) > 0){
						amount=totIPDepsoitAvl.subtract(ipDepositBillSetOffAmt);
						if(amount.compareTo(BigDecimal.ZERO) > 0 ){
							patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"I",false);	
						}
						amount=creditBillForm.getDepositSetOff().subtract(totIPDepsoitAvl).subtract(genBillDeposit);
						if(amount.compareTo(BigDecimal.ZERO) != 0 ){
							patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"B",false);
						}
					}else{
						if(genBillDeposit.compareTo(BigDecimal.ZERO) > 0 ){
							patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), BigDecimal.ZERO.subtract(genBillDeposit),"B",false);
						}
						amount=creditBillForm.getDepositSetOff().subtract(ipDepositBillSetOffAmt);
						if(amount.compareTo(BigDecimal.ZERO) != 0 ){
							patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"I",false);	
						}
					}
				}else{// deposit setoff for IP visit & General deposits
					if(totGenDepositAvl.compareTo(BigDecimal.ZERO) >= 0 ){
						if(creditBillForm.getDepositSetOff().compareTo(totGenDepositAvl) > 0){
							amount=totGenDepositAvl.subtract(genBillDeposit);
							if(amount.compareTo(BigDecimal.ZERO) > 0 ){
								patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"B",false);	
							}
							amount=creditBillForm.getDepositSetOff().subtract(totGenDepositAvl).subtract(ipDepositBillSetOffAmt);
							if(amount.compareTo(BigDecimal.ZERO) != 0 ){
								patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"I",false);
							}
						}else{
							if(ipDepositBillSetOffAmt.compareTo(BigDecimal.ZERO) > 0 ){
								patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), BigDecimal.ZERO.subtract(ipDepositBillSetOffAmt),"I",false);
							}
							amount=creditBillForm.getDepositSetOff().subtract(genBillDeposit);
							if(amount.compareTo(BigDecimal.ZERO) != 0 ){
								patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"B",false);
							}
						}
					}
				}
			}
		}else{// deposit setoff for other than IP visit
			amount=creditBillForm.getDepositSetOff().subtract(billDepositSetOffAmt);
			if(amount.compareTo(BigDecimal.ZERO) != 0 ){
				patientDepositSetOffAdjustment(creditBillForm.getMrNo(),creditBillForm.getBillNo(), amount,"B",ismultipkg);
			}
		}
	}
	private void patientDepositSetOffAdjustment(String mr_no, String bill_no,BigDecimal amount, String depositFor, boolean ismultipkg) throws SQLException{
		DepositsDAO.insertPatientDepositSetOffAdjustment(mr_no, bill_no, amount, depositFor, ismultipkg);
	}

	private BigDecimal getTotalAvailableDeposit(CreditBillForm creditBillForm) throws SQLException{

		BasicDynaBean depositBean =  DepositsDAO.getDepositAmounts(creditBillForm.getMrNo());

		BigDecimal totalDepositAmt = BigDecimal.ZERO;
		BigDecimal billDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal totalDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal depositSetOffAmt = BigDecimal.ZERO;

		if (depositBean!= null && depositBean.get("hosp_total_deposits") != null)
			totalDepositAmt = (BigDecimal)depositBean.get("hosp_total_deposits");

		// Get the total deposit set off for the patient and the current bill deposit set off.
		BasicDynaBean billDepositBean = DepositsDAO.getDepositSetOffBillExcluded(
				creditBillForm.getMrNo(), creditBillForm.getBillNo());

		if (billDepositBean != null) {
			totalDepositSetOffAmt =
				billDepositBean.get("hosp_total_setoffs") != null ? (BigDecimal)billDepositBean.get("hosp_total_setoffs") : BigDecimal.ZERO;
			billDepositSetOffAmt =
				billDepositBean.get("deposit_set_off") != null ? (BigDecimal)billDepositBean.get("deposit_set_off") : BigDecimal.ZERO;
		}

		// bill deposit set off excluded
		depositSetOffAmt = totalDepositSetOffAmt.subtract(billDepositSetOffAmt);

		// calculate deposit remaining after the current bill set off is deducted.
		BigDecimal depositAmtRemaining = totalDepositAmt.subtract(depositSetOffAmt);

		return depositAmtRemaining;
	}

	private BigDecimal getTotalIPAvailableDeposit(CreditBillForm creditBillForm) throws SQLException{

		BasicDynaBean depositBean =  DepositsDAO.getIPDepositAmounts(creditBillForm.getMrNo());

		BigDecimal totalIPDepositAmt = BigDecimal.ZERO;
		BigDecimal ipBillDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal totalIPDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal ipDepositSetOffAmt = BigDecimal.ZERO;

		if (depositBean!= null && depositBean.get("total_ip_deposits") != null)
			totalIPDepositAmt = (BigDecimal)depositBean.get("total_ip_deposits");

		// Get the total deposit set off for the patient and the current bill deposit set off.
		BasicDynaBean billDepositBean = DepositsDAO.getIPDepositSetOffBillExcluded(
				creditBillForm.getMrNo(), creditBillForm.getBillNo());

		if (billDepositBean != null) {
			totalIPDepositSetOffAmt =
				billDepositBean.get("total_ip_set_offs") != null ? (BigDecimal)billDepositBean.get("total_ip_set_offs") : BigDecimal.ZERO;
				ipBillDepositSetOffAmt =
				billDepositBean.get("ip_deposit_set_off") != null ? (BigDecimal)billDepositBean.get("ip_deposit_set_off") : BigDecimal.ZERO;
		}

		// bill deposit set off excluded
		ipDepositSetOffAmt = totalIPDepositSetOffAmt.subtract(ipBillDepositSetOffAmt);

		BigDecimal depositAmtRemaining = totalIPDepositAmt.subtract(ipDepositSetOffAmt);

		return depositAmtRemaining;
	}


	private void setIPBillDepositSetOff(CreditBillForm creditBillForm, Bill bill) throws SQLException {

		BasicDynaBean depositBean =  DepositsDAO.getIPDepositAmounts(creditBillForm.getMrNo());

		BigDecimal totalIPDepositAmt = BigDecimal.ZERO;
		BigDecimal ipBillDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal totalIPDepositSetOffAmt = BigDecimal.ZERO;
		BigDecimal ipDepositSetOffAmt = BigDecimal.ZERO;

		if (depositBean!= null && depositBean.get("total_ip_deposits") != null)
			totalIPDepositAmt = (BigDecimal)depositBean.get("total_ip_deposits");

		// Get the total deposit set off for the patient and the current bill deposit set off.
		BasicDynaBean billDepositBean = DepositsDAO.getIPDepositSetOffBillExcluded(
				creditBillForm.getMrNo(), creditBillForm.getBillNo());

		if (billDepositBean != null) {
			totalIPDepositSetOffAmt =
				billDepositBean.get("total_ip_set_offs") != null ? (BigDecimal)billDepositBean.get("total_ip_set_offs") : BigDecimal.ZERO;
				ipBillDepositSetOffAmt =
				billDepositBean.get("ip_deposit_set_off") != null ? (BigDecimal)billDepositBean.get("ip_deposit_set_off") : BigDecimal.ZERO;
		}

		// bill deposit set off excluded
		ipDepositSetOffAmt = totalIPDepositSetOffAmt.subtract(ipBillDepositSetOffAmt);

		// calculate deposit remaining after the current bill set off is deducted.
		BigDecimal depositAmtRemaining = totalIPDepositAmt.subtract(ipDepositSetOffAmt);

		// Set the bill deposit set off if the remaining set off is not negative.
		if (creditBillForm.getIpDepositSetOff() != null) {
			if (depositAmtRemaining.compareTo(BigDecimal.ZERO) > 0) {
				if (depositAmtRemaining.subtract(creditBillForm.getIpDepositSetOff())
						.compareTo(BigDecimal.ZERO) >= 0) {
					bill.setIpDepositSetOff(creditBillForm.getIpDepositSetOff());
				} else if(creditBillForm.getIpDepositSetOff().compareTo(BigDecimal.ZERO) > 0) {
					bill.setIpDepositSetOff(depositAmtRemaining);
				}
			}
		}
	}

	private void setBillRewardPoints(CreditBillForm creditBillForm, Bill bill) throws SQLException {

		BigDecimal redemptionRate = GenericPreferencesDAO.getGenericPreferences().getPoints_redemption_rate();
		redemptionRate = redemptionRate == null ? BigDecimal.ZERO : redemptionRate;

		BasicDynaBean rewardPointsBean = new GenericDAO("reward_points_status").findByKey("mr_no", creditBillForm.getMrNo());

		int totalPointsEarned = 0;
		int totalPointsRedeemed = 0;
		int totalOpenPointsRedeemed = 0;

		int billPointsRedeemed = 0;

		if (rewardPointsBean!= null) {
			if (rewardPointsBean.get("points_earned") != null)
				totalPointsEarned = (Integer)rewardPointsBean.get("points_earned");
			if (rewardPointsBean.get("points_redeemed") != null)
				totalPointsRedeemed = (Integer)rewardPointsBean.get("points_redeemed");
			if (rewardPointsBean.get("open_points_redeemed") != null)
				totalOpenPointsRedeemed = (Integer)rewardPointsBean.get("open_points_redeemed");

			totalPointsRedeemed = totalPointsRedeemed + totalOpenPointsRedeemed;
		}

		// Get the current bill points
		BasicDynaBean billBean = billDao.findByKey("bill_no", creditBillForm.getBillNo());

		billPointsRedeemed =
			billBean.get("points_redeemed") != null ? (Integer)billBean.get("points_redeemed") : 0;

		// Remaining points after the current bill points are deducted.
		int pointsRemaining = totalPointsEarned - totalPointsRedeemed + billPointsRedeemed;

		// bill points
		int points = creditBillForm.getRewardPointsRedeemed();

		// Set the bill redeemed points if the remaining points > 0
		if (points != 0) {
			if (pointsRemaining > 0) {
				if (pointsRemaining - points >= 0) {
					bill.setRewardPointsRedeemed(points);
				} else if(points > 0) {
					bill.setRewardPointsRedeemed(pointsRemaining);
				}
			}else {

			}
			bill.setRewardPointsRedeemedAmount(redemptionRate.multiply(new BigDecimal(bill.getRewardPointsRedeemed())));
		}else {
			bill.setRewardPointsRedeemed(0);
			bill.setRewardPointsRedeemedAmount(BigDecimal.ZERO);
		}
	}

	@IgnoreConfidentialFilters
	public ActionForward getPackChargeDetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException, SQLException, ParseException {

		JSONSerializer js = new JSONSerializer().exclude("class");

		String  bedType = (String)request.getParameter("bedType");
		String orgId = (String)request.getParameter("orgId");
		int packageId  = Integer.parseInt(request.getParameter("packageId"));

		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.setContentType("application/x-json");
		response.getWriter().write(js.serialize(PackageDAO.getPackageDeptCharges(packageId, orgId, bedType)));
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getItemDescription(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		String chargeGroup = (String)request.getParameter("chargeGroup");
		String chargeId = (String)request.getParameter("itemChargeId");

		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		String description = null;
		BasicDynaBean chargeBean = billChargeDao.findByKey("charge_id", chargeId);
		if (chargeBean != null) {

			BasicDynaBean itemBean = null;

			if (chargeGroup.endsWith("OPE")) {
				itemBean = new GenericDAO("operation_master").findByKey("op_id", chargeBean.get("act_description_id"));

			}else if (chargeGroup.endsWith("DIA")) {
				itemBean = new GenericDAO("diagnostics").findByKey("test_id", chargeBean.get("act_description_id"));

			}else if (chargeGroup.endsWith("SNP")) {
				itemBean = new GenericDAO("services").findByKey("service_id", chargeBean.get("act_description_id"));
			}

			if (itemBean != null)
				description = (String)itemBean.get("remarks");
		}
		response.getWriter().write(js.serialize(description));
		return null;
	}

	public ActionForward downloadCSVFile(ActionMapping m, ActionForm f, HttpServletRequest req,
			HttpServletResponse res)
			throws SQLException, java.io.IOException ,TemplateException, XPathExpressionException, TransformerException{
		HttpSession session = req.getSession();
		String orgTempalteName = "";
		String billNo = req.getParameter("billNo");
		BasicDynaBean bean = BillDAO.getBillBean(billNo);
		String templateName = req.getParameter("template_id");

		String userId = (String)session.getAttribute("userid");
		List<BasicDynaBean> billList = new ArrayList<BasicDynaBean>();
		billList.add(bean);
		StringWriter sWriter = new StringWriter();
		String[] returnVals = null;
		if (templateName.contains("CUSTOMEXP")){
			orgTempalteName = templateName.substring(10);
			returnVals = BillPrintHelper.processBillTemplate(sWriter, billNo, orgTempalteName, userId);
		} else if (templateName.contains("CUSTOM")) {
			orgTempalteName = templateName.substring(7);
			returnVals = BillPrintHelper.processBillTemplate(sWriter, billNo, orgTempalteName, userId);
		} else {
			returnVals = BillPrintHelper.processBillTemplate(sWriter, billNo, templateName, userId);
		}

		String billContent = sWriter.toString();

		HtmlConverter hc = new HtmlConverter();

		int printerId = (Integer.parseInt(req.getParameter("printerId")));

		BasicDynaBean printPref =
			PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_BILL, printerId);

		String textContent = null;
		String contentType = returnVals[2];
		String extn = returnVals[3];

		if (returnVals[0].equals("T")){
			// write the output as is.
			textContent = billContent;
		} else {
			if ("text/plain".equals(contentType)) {
				// convert from HTML to text
				textContent = new String(hc.getText(billContent, "Provisional Bill", printPref, true, true));
			} else {
				// return the content as is
				textContent = billContent;
			}
		}

		String fileName = "BILL-" + billNo;
		if (extn != null && !extn.equals(""))
			fileName = fileName + "." + extn;

		res.setHeader("Content-type", contentType);
		res.setHeader("Content-disposition","attachment; filename=" + fileName);
		res.setHeader("Readonly","true");

		res.getWriter().write(textContent);

		return null;
	}

	public ActionForward checkPackageDocuments(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		JSONSerializer js = new JSONSerializer().exclude("class");

		String visitId = request.getParameter("patient_id");
		Map result = PackageDocDAO.uploadedAllDocs(visitId);
		response.getWriter().write(js.deepSerialize(result));

		return null;
	}

	public ActionForward getClaimAmount(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		AdvanceInsuranceCalculator insCalculator = new AdvanceInsuranceCalculator();
		String strAmount = request.getParameter("amount");
		String strDiscount = request.getParameter("discount");
		String insPayable = request.getParameter("insPayable");

		BigDecimal amount = (null == strAmount || strAmount.trim().equals("")) ? BigDecimal.ZERO  : new BigDecimal(strAmount);
		BigDecimal discount = (null == strAmount || strAmount.trim().equals("")) ? BigDecimal.ZERO  : new BigDecimal(strDiscount);

		int planId = Integer.parseInt(request.getParameter("planId"));
		String billNo = request.getParameter("billNo");
		String firstOfCategory = request.getParameter("firstOfCategory");
		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		String visitType = request.getParameter("visitType");
		BigDecimal claimAmount = null;
		claimAmount = insCalculator.calculateClaim(amount, discount, billNo, planId,
				firstOfCategory.equals("true"), visitType, categoryId, insPayable.equals("Y"));
		response.getWriter().write(js.serialize(claimAmount));
		return null;

	}

	public ActionForward getInsClaimAmount(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		response.setContentType("application/x-json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		String strAmount = request.getParameter("amount");
		String strDiscount = request.getParameter("discount");
		String chargeHead = request.getParameter("charge_head");
		String plans[] = request.getParameterValues("planIds");
		int planIds[] =  new int[plans.length];

		int idx = 0;
		for(String planId : plans){
			planIds[idx++] = Integer.parseInt(planId);
		}

		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		String visitID = request.getParameter("visit_id");

		BigDecimal amount = (null == strAmount || strAmount.trim().equals("")) ? BigDecimal.ZERO  : new BigDecimal(strAmount);
		BigDecimal discount = (null == strAmount || strAmount.trim().equals("")) ? BigDecimal.ZERO  : new BigDecimal(strDiscount);

		BasicDynaBean billChargeBean = billChargeDao.getBean();
		BasicDynaBean billChargeClaimBean = billChargeClaimDao.getBean();

		List<BasicDynaBean> billChgClaimList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> visitInsBeanList = new ArrayList<BasicDynaBean>();

		billChargeBean.set("amount", amount);
		billChargeBean.set("discount", discount);

		for(int planId : planIds){
			//BasicDynaBean visitInsBean = billDao.getVisitInsDetails(planId, categoryId, visitID, chargeHead);
			BasicDynaBean visitInsBean = null;
			visitInsBeanList.add(visitInsBean);
			billChgClaimList.add(billChargeClaimBean);
		}

		//new ItemInsuranceCalculator().calculate(billChargeBean, billChgClaimList, visitInsBeanList);

		billChargeBean.set("charge_id", "_new");

		List<BasicDynaBean> billCharges = new ArrayList<BasicDynaBean>();
		billCharges.add(billChargeBean);

		Map<String, List<BasicDynaBean>> billChargeClaims = new HashMap<String, List<BasicDynaBean>>();
		billChargeClaims.put("_new", billChgClaimList);

		new AdvanceInsuranceCalculator().calculate(billCharges, billChargeClaims, visitInsBeanList, billChgClaimList);

		response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(billChgClaimList)));
		return null;

	}

	public ActionForward markForWriteoff(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {
		String billNo = request.getParameter("billNo");
		String isNewUX = request.getParameter("isNewUX");
		String remarks = request.getParameter("writeOffRemarks");
		BasicDynaBean billBean = billDao.getBean();
		billBean.set("patient_writeoff", "M");
		billBean.set("writeoff_remarks", remarks);
		BillDAO.markBillForWriteOff(billNo,billBean);
		ActionRedirect redirect = new ActionRedirect( mapping.findForward("savedetails"));
		redirect.addParameter("isNewUX", isNewUX);
		redirect.addParameter("billNo", request.getParameter("billNo"));
		return redirect;
	}
	
	public ActionForward processCaseRate(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    String visitId = request.getParameter("visitId");
    String[] caseRateIds = request.getParameterValues("case_rate_id");
    BasicDynaBean visitBean = visitDetailsDao.findByKey("patient_id", visitId);
    Integer existingPriCaseRate = null != visitBean.get("primary_case_rate_id") ? (Integer)visitBean.get("primary_case_rate_id") : null;
    Integer existingSecCaseRate = null != visitBean.get("secondary_case_rate_id") ? (Integer)visitBean.get("secondary_case_rate_id") : null;
    Boolean recalcSponsorRequired = false;
    if(null != caseRateIds && caseRateIds.length>0 && null != visitBean){
      if(null != caseRateIds[0])
        visitBean.set("primary_case_rate_id", !caseRateIds[0].isEmpty() ? Integer.parseInt(caseRateIds[0]) : null);
      if(caseRateIds.length > 1 && null != caseRateIds[1])
        visitBean.set("secondary_case_rate_id", !caseRateIds[1].isEmpty() ? Integer.parseInt(caseRateIds[1]) : null);

      visitDetailsDao.updateCaseRateDetails(visitBean);
      insertVisitCaseRateDetails(visitBean);
    }
    
    if(null == visitBean.get("primary_case_rate_id") && existingPriCaseRate != null){
      patientInsurancePlanDao.removeCaseRateLimits(existingPriCaseRate, visitId);
      visitCaseRateDetailsDAO.deleteCaseRatedetails(existingPriCaseRate, visitId);
      recalcSponsorRequired = true;
    }
    
    if(null == visitBean.get("secondary_case_rate_id") && existingSecCaseRate != null){
      patientInsurancePlanDao.removeCaseRateLimits(existingSecCaseRate, visitId);
      visitCaseRateDetailsDAO.deleteCaseRatedetails(existingSecCaseRate,visitId);
      recalcSponsorRequired = true;
    }
    
    if(null != visitBean.get("primary_case_rate_id") || null != visitBean.get("secondary_case_rate_id")){
      billBo.updateCaseRateLimts(visitBean);
      recalcSponsorRequired = true;
    }
    
    if(recalcSponsorRequired){
      sponsorDAO.unlockVisitBillsCharges(visitId);
      sponsorDAO.unlockVisitSaleItems(visitId);
      sponsorDAO.includeBillChargesInClaimCalc(visitId);
      sponsorBO.recalculateSponsorAmount(visitId);
      sponsorDAO.setIssueReturnsClaimAmountTOZero(visitId);
      sponsorDAO.insertOrUpdateBillChargeTaxesForSales(visitId);
      sponsorDAO.lockVisitSaleItems(visitId);
      sponsorDAO.updateSalesBillCharges(visitId);
      sponsorDAO.updateTaxDetails(visitId);
    }
    
    ActionRedirect redirect = new ActionRedirect( mapping.findForward("savedetails"));
    redirect.addParameter("billNo", request.getParameter("billNo"));
    redirect.addParameter("isNewUX", request.getParameter("isNewUX"));
    return redirect;
  }

  private void insertVisitCaseRateDetails(BasicDynaBean visitBean) throws SQLException, IOException{
    Integer priCaseRateId = null != visitBean.get("primary_case_rate_id") ? (Integer)visitBean.get("primary_case_rate_id") : null;
    Integer secCaseRateId = null != visitBean.get("secondary_case_rate_id") ? (Integer)visitBean.get("secondary_case_rate_id") : null;
    String visitId = (String) visitBean.get("patient_id");
    visitCaseRateDetailsDAO.deleteVisitCaseRateDetails(visitId);
    if(null != priCaseRateId){
      visitCaseRateDetailsDAO.insertVisitCaseRateDetails(visitId, priCaseRateId);
    }
    if(null != secCaseRateId){
      visitCaseRateDetailsDAO.insertVisitCaseRateDetails(visitId, secCaseRateId);
    }
  }

  public ActionForward requestForBillCancellation(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception {

		String centerId = request.getSession(false).getAttribute("centerId").toString();
		String billNo = request.getParameter("billNo");
		String isNewUX = request.getParameter("isNewUX");
		String discAuthStr = request.getParameter("billDiscountAuth");
		Integer discAuthInt = null;
		if (null != discAuthStr && !discAuthStr.equals(""))
			discAuthInt = Integer.parseInt(request.getParameter("billDiscountAuth"));
		BasicDynaBean billBean = billDao.getBean();
		billBean.set("cancellation_approval_status", "S");
		billBean.set("discount_auth", discAuthInt);

		ActionRedirect redirect = new ActionRedirect(mapping.findForward("savedetails"));
		if(sendBillCancellationNotification(billNo, centerId, discAuthStr, redirect,request)){
			BillDAO.requestForBillCancellation(billNo,billBean);
		}
		redirect.addParameter("billNo", request.getParameter("billNo"));
		redirect.addParameter("isNewUX", isNewUX);
		return redirect;
	}

	private boolean sendBillCancellationNotification(String billNo, String centerId, String discAuth,
			ActionRedirect redirect,HttpServletRequest request) throws SQLException, ParseException, IOException {
		boolean success = false;
		MessageManager mgr = new MessageManager();
		Map<String,String> billData = getBillData(billNo);
		List<BasicDynaBean> userListBeans = new ArrayList<BasicDynaBean>();
		if (null != discAuth && !discAuth.equals("") && !discAuth.equals("0")) {
			Integer discAuthId = Integer.parseInt(discAuth);
			userListBeans = uUserDao.findAllByKey("disc_auth_id", discAuthId);
		} else {
			List<BasicDynaBean> discAuthListBeans = BillDAO.getDiscountAuthList(centerId);
			for (BasicDynaBean discAuthBean : discAuthListBeans) {
				Integer discAuthId = (Integer) discAuthBean.get("disc_auth_id");
				userListBeans.addAll(uUserDao.findAllByKey("disc_auth_id", discAuthId));
			}
		}
		String userName = null;

		GenericDAO dao1 = new GenericDAO("message_log_batch_id");
		int batchId = dao1.getNextSequence();

		for (BasicDynaBean userBean : userListBeans) {
			// Send NOTIFICATION to User Discount Authorizer
			userName = (String) userBean.get("emp_username");
			if (null != userName && !userName.equals("")) {
				billData.put("total_approved_amt_for_current_month", BillDAO.getTotalApprovedAmount(userName, "M"));
				billData.put("total_approved_amt_for_current_year", BillDAO.getTotalApprovedAmount(userName, "Y"));
				billData.put("receipient_id__", userName);
				billData.put("receipient_type__", "DISC_AUTHORIZER");
				billData.put("entity_id", billNo);
				billData.put("batch_id", Integer.toString(batchId));
				success =  mgr.processEvent("bill_cancelled", billData, true) || success;
			} else {
				log.info ("User Discount Authorizer is not available for " + userName + ", skipping Notification on Bill Cancellation.");
			}
		}

		FlashScope flash = FlashScope.getScope(request);
		if(!success ){
			if(userListBeans.size() == 0)
				flash.error("Error in sending a message. Selected Discount Authorizer is not mapped to any user.");
			else
				flash.error("Error in sending a message.");
		}

		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
		return success;
	 }

	 private Map<String, String> getBillData(String billNo) throws SQLException {
		 BasicDynaBean billBean = BillDAO.getBillBean(billNo);
		 Map<String,String> billData = new HashMap<String, String>();
		 String restrictionType = (String)billBean.get("restriction_type");
		 if(restrictionType.equals("N")) {

			 BasicDynaBean patientBean = VisitDetailsDAO.getPatientVisitDetailsBean((String)billBean.get("visit_id"));

			 billData.put("mr_no", (String)patientBean.get("mr_no"));
			 billData.put("patient_name", (String)patientBean.get("full_name"));
			 billData.put("admission_date", DateUtil.formatDate((java.util.Date)patientBean.get("reg_date")));
			 billData.put("admission_date_yyyy_mm_dd", new DateUtil().getSqlDateFormatter().format((java.util.Date)patientBean.get("reg_date")));
			 billData.put("admission_time", (String)patientBean.get("reg_time").toString());
			 billData.put("admission_time_12hr", DateUtil.formatTimeMeridiem((java.sql.Time)patientBean.get("reg_time")));
			 billData.put("center_name", (String)patientBean.get("center_name"));
			 billData.put("center_address", (String)patientBean.get("center_address"));
			 billData.put("center_contact_phone", (String)patientBean.get("center_contact_phone"));
			 billData.put("admitted_by", (String)patientBean.get("admitted_by"));
			 billData.put("department", (String)patientBean.get("dept_name"));
			 billData.put("patient_phone", (String)patientBean.get("patient_phone"));
			 billData.put("next_of_kin_contact",(String)patientBean.get("patient_care_oftext"));
			 billData.put("next_of_kin_name", (String)patientBean.get("relation"));
			 billData.put("doctor_name", (String)patientBean.get("doctor_name"));
			 billData.put("referal_doctor", (String)patientBean.get("refdoctorname"));
			 billData.put("doctor_mobile", (String)patientBean.get("doctor_mobile"));
			 billData.put("referal_doctor_mobile", (String)patientBean.get("reference_docto_id"));
			 billData.put("admitting_doctor_id__", (String)patientBean.get("doctor"));
			 billData.put("referal_doctor_id__", (String)patientBean.get("reference_docto_id"));
			 billData.put("hospital_name", "");
			 billData.put("patient_gender", (String)patientBean.get("patient_gender"));
			 billData.put("incoming_visit_id", (String)patientBean.get("previous_visit_id"));
			 billData.put("recipient_email", (String)patientBean.get("email_id"));

		 }else if(restrictionType.equals("T")){

			 BasicDynaBean incomingPatientBean = OhSampleRegistrationDAO.getIncomingCustomer((String)billBean.get("visit_id"));

			 billData.put("mr_no", (String)incomingPatientBean.get("mr_no"));
			 billData.put("admission_date", "");
			 billData.put("admission_date_yyyy_mm_dd", "");
			 billData.put("admission_time", "");
			 billData.put("admission_time_12hr", "");
			 billData.put("center_name", "");
			 billData.put("admitted_by", "");
			 billData.put("department", "");
			 billData.put("patient_phone", (String)incomingPatientBean.get("phone_no"));
			 billData.put("next_of_kin_contact","");
			 billData.put("next_of_kin_name", "");
			 billData.put("doctor_name", "");
			 billData.put("referal_doctor", (String)incomingPatientBean.get("referral"));
			 billData.put("doctor_mobile", "");
			 billData.put("referal_doctor_mobile", "");
			 billData.put("admitting_doctor_id__", "");
			 billData.put("referal_doctor_id__", (String)incomingPatientBean.get("referring_doctor"));
			 billData.put("hospital_name", (String)incomingPatientBean.get("hospital_name"));
			 billData.put("patient_name", (String)incomingPatientBean.get("patient_name"));
			 billData.put("patient_gender", (String)incomingPatientBean.get("patient_gender"));
			 billData.put("incoming_visit_id", (String)incomingPatientBean.get("incoming_visit_id"));
		 }

		 billData.put("bill_no", (String)billBean.get("bill_no"));
		 billData.put("open_date", DateUtil.formatTimestamp((java.util.Date)billBean.get("open_date")));
		 billData.put("finalized_date",  DateUtil.formatTimestamp((java.util.Date)billBean.get("finalized_date")));
		 billData.put("closed_date",  DateUtil.formatTimestamp((java.util.Date)billBean.get("closed_date")));
		 billData.put("opened_by", (String)billBean.get("opened_by"));
		 billData.put("closed_by", (String)billBean.get("closed_by"));
		 billData.put("finalized_by", (String)billBean.get("finalized_by"));
		 billData.put("total_amount", billBean.get("total_amount").toString());
		 billData.put("total_amount_received", billBean.get("total_receipts").toString());
		 billData.put("total_discount", billBean.get("total_discount").toString());
		 billData.put("total_claim", billBean.get("total_claim").toString());
		 billData.put("claim_recd_amount", billBean.get("claim_recd_amount").toString());
		 billData.put("approval_amount", null != billBean.get("approval_amount") ? billBean.get("approval_amount").toString() : "");
		 billData.put("primary_approval_amount", null != billBean.get("primary_approval_amount") ? billBean.get("primary_approval_amount").toString() : "");
		 billData.put("secondary_approval_amount", null != billBean.get("secondary_approval_amount") ? billBean.get("secondary_approval_amount").toString() : "");
		 billData.put("primary_total_claim", billBean.get("primary_total_claim").toString());
		 billData.put("secondary_total_claim", billBean.get("secondary_total_claim").toString());
		 billData.put("insurance_deduction", billBean.get("insurance_deduction").toString());
		 billData.put("discount_auth", null != billBean.get("discount_auth") ? billBean.get("discount_auth").toString() : null);
		 billData.put("disc_auth_name", (String)billBean.get("disc_auth_name"));
		 billData.put("cancel_reason", (String)billBean.get("cancel_reason"));
		 billData.put("bill_signature", (String)billBean.get("bill_signature"));

		 return billData;
	 }
	 public ActionForward updateAndSendEmail(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws SQLException, Exception {
			Boolean isSuccess = false;
			String newEmail = request.getParameter("newEmail");
			String mr = request.getParameter("patientMrno");
			String BillNo = request.getParameter("billNo");
			boolean status = false;
			Connection con = DataBaseUtil.getConnection();
			con.setAutoCommit(false);
			try {
				Map<String, String> newcol = new HashMap<String, String>();
				newcol.put("email_id", newEmail);
				Map<String, String> refcol = new HashMap<String, String>();
				refcol.put("mr_no", mr);
				status = patientDetailsDao.update(con, newcol, refcol) > 0;
			} finally {
				DataBaseUtil.commitClose(con, status);
				if (status && MessageUtil.allowMessageNotification(request, "general_message_send")) {
					String userid = (String) request.getSession().getAttribute("userid");
					Map<String, String> billData = getBillData(BillNo);
					BillBO billBOObj = new BillBO();
					Bill bill = billBOObj.getBill(BillNo);
					String template=null;
					if(bill.getBillType().equals("P")){ // Bill Now
						template = GenericPreferencesDAO.getGenericPreferences().getEmailBillNowTemplate();
					}
					else{  // Bill Later
						template = GenericPreferencesDAO.getGenericPreferences().getEmailBillLaterTemplate();
					}
					String[] templateName = template.split("-");
					StringWriter writer = new StringWriter();// "BUILTIN_HTML"		
					String[] returnVals = BillPrintHelper.processBillTemplate(writer, BillNo, templateName[1], userid);
					String report = writer.toString();
					billData.put("_report_content", report);
					billData.put("_message_attachment", report);

					billData.put("message_attachment_name", "Bill_" + BillNo);
					billData.put("category", "Bill");
					if (bill.getBillType().equalsIgnoreCase(Bill.BILL_TYPE_CREDIT)) {
						billData.put("bill_date", billData.get("finalized_date"));
					}
					if (bill.getBillType().equalsIgnoreCase(Bill.BILL_TYPE_PREPAID)) {
						billData.put("bill_date", billData.get("closed_date"));
					}
					billData.put("recipient_name", billData.get("patient_name"));
					billData.put("recipient_phone", billData.get("patient_phone"));
					billData.put("recipient_mobile", billData.get("patient_phone"));
					billData.put("bill_amount", billData.get("total_amount"));
					billData.put("receipient_id__", billData.get("mr_no"));
					billData.put("receipient_type__", "PATIENT");
					billData.put("printtype",String.valueOf(GenericPreferencesDAO.getGenericPreferences().getEmailBillPrint()));
					MessageManager mgr = new MessageManager();
					isSuccess = mgr.processEvent("manual_op_bn_cash_bill_paid_closed", billData, true);
				}
			}
			response.setContentType("application/json");
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			String json ="{\"result\": \"" + isSuccess + "\",\"isUpdated\" : \"" + status +"\" }";
			response.getWriter().write(json);
			response.flushBuffer();
			return null;

		}
	 public ActionForward requestMoneyByPaytm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws SQLException, Exception {
			String mobileNumber = request.getParameter("mobileNumber");	
			String otp = request.getParameter("otp");
			String totalAmt = request.getParameter("totalAmt");
			String billNumber = request.getParameter("billNumber");
			String json = PaytmUtil.requestForMoney(mobileNumber,otp,totalAmt,billNumber);
			response.setContentType("application/json");	
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");	
			response.getWriter().write(json);
			response.flushBuffer();
			return null;
		}
	 
	 public ActionForward checkTransactionStatusPaytm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws SQLException, Exception {
			String json = PaytmUtil.checkTransaction(request.getParameter("transaction_id"));
			response.setContentType("application/json");	
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");	
			response.getWriter().write(json);
			response.flushBuffer();
			return null;
		}


	 // getting payed amount to check tranction limit ref bug:  
	 public ActionForward getpaymentModeAmount(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response)throws ServletException, IOException, SQLException {

			String visit_id = request.getParameter("visit_id");
			String mode_id = request.getParameter("mode_id");
			String amount = BillDAO.getpaymentmodeamount(visit_id,mode_id);
			amount = (amount == null || amount.isEmpty()) ? "0" : amount;
			response.setContentType("text/plain");
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			response.getWriter().write(amount);
			return null;
		}
	 
	 @IgnoreConfidentialFilters
	 public ActionForward fetchPointsInLoyaltyCard(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws SQLException, Exception {
			String mobileNumber = request.getParameter("mobileNumber");	
			Integer paymentModeId = Integer.parseInt(request.getParameter("paymentModeId"));  
            String json = null;
            if (paymentModeId==-3) {
                json = LoyaltyCardUtil.requestForMoney(mobileNumber,null,null,null,"GET_CUSTOMER_WALLET_BALANCE");
            }
            if (paymentModeId==-5) {
                json = OneApolloUtil.requestForMoney(mobileNumber,null,null,null,"GetByMobile",null);
            }			
	        response.setContentType("application/json");	
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");	
			response.getWriter().write(json);
			response.flushBuffer();
			return null;
		}
	 @IgnoreConfidentialFilters
	 public ActionForward requestOTPForLoyaltyCard(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws SQLException, Exception {
			String mobileNumber = request.getParameter("mobileNumber");	
			String totalAmt = request.getParameter("points");
			Integer paymentModeId = Integer.parseInt(request.getParameter("paymentModeId"));  
            String json = null;
            if (paymentModeId==-3) {
                json = LoyaltyCardUtil.requestForMoney(mobileNumber,null,totalAmt,null,"GET_WALLET_POINTS");
            }
            if (paymentModeId==-5) {
                String requestNumber = request.getParameter("requestNumber");
                json = OneApolloUtil.requestForMoney(mobileNumber,null,totalAmt,null,"sendOTP",requestNumber);
            }
			response.setContentType("application/json");	
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");	
			response.getWriter().write(json);
			response.flushBuffer();
			return null;
		}
	 @IgnoreConfidentialFilters
	 public ActionForward requestMoneyByLoyaltyCard(ActionMapping mapping, ActionForm form, HttpServletRequest request,
				HttpServletResponse response) throws SQLException, Exception {
			String mobileNumber = request.getParameter("mobileNumber");	
			String otp = request.getParameter("otp");
			String totalAmt = request.getParameter("totalAmt");
			String billNumber = request.getParameter("billNumber");
			Integer paymentModeId = Integer.parseInt(request.getParameter("paymentModeId"));
            String json = null;
            if (paymentModeId==-3) {
                json = LoyaltyCardUtil.requestForMoney(mobileNumber,otp,totalAmt,billNumber,"GET_WALLET_REDEMPTION");
            } else if(paymentModeId==-5) {
                String referenceNumber = request.getParameter("referenceNumber");
                json = OneApolloUtil.requestForMoney(mobileNumber,otp,totalAmt,billNumber,"validateOTP",referenceNumber);
            }
			response.setContentType("application/json");	
			response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");	
			response.getWriter().write(json);
			response.flushBuffer();
			return null;
		}

	private int getStoreCode(Connection con, String integrationName) {
		int storeCode = 0;
		try {
			BasicDynaBean bean = new InstaIntegrationDao()
					.getCenterIntegrationDetails(con, RequestContext.getCenterId(),integrationName);
			if (bean != null && bean.get("store_code") != null) {
				String storeCodeStr = (String) bean.get("store_code");
				storeCode = Integer.parseInt(storeCodeStr);
			}
		} catch (SQLException e) {
			log.debug(e.toString());
		}
		return storeCode;
	}
	
	private void scheduleBillMessage(String eventId, String userName, Map billData) throws ParseException {
		String jobMessage = null;
		if (eventId.equals("op_bill_paid") || eventId.equals("ip_bill_paid")) 
				jobMessage = "BillEmailJobPHR_";
		if (eventId.equals("discount_given"))
					jobMessage = "BillDiscountSMSJob_";
		if (eventId.equals("bill_payment_message"))
					jobMessage = "BillPaymentSMSJob_";
		if (eventId.equals("advance_paid"))
					jobMessage = "BillAdvancePaymentSMSJob_";
		if (eventId.equals("op_bn_cash_bill_paid_closed"))
			jobMessage = "BillEmailJob_";
		if (eventId.equals("bill_refund_message"))
      jobMessage = "BillRefundSMSJob_";
		
		Map<String,Object> jobData = new HashMap<String, Object>();
       	jobData.put("eventData", billData);
        jobData.put("userName", userName);
        jobData.put("schema", RequestContext.getSchema());
		jobData.put("eventId", eventId);
	    jobService.scheduleImmediate(buildJob(
	    		jobMessage + billData.get("bill_no").toString(),
	            BillMessagingJob.class, jobData));
	}
	
	public ActionForward depositsSetOffAjax(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {
		HashMap returnedData = new HashMap();
		String mrNo = request.getParameter("mr_no");
		String billNo = request.getParameter("bill_number");
		BasicDynaBean patientDepositBean = patientDepositsDao.findByKey("mr_no",mrNo);
		BillBO billBOObj = new BillBO();
		BillDetails billDetails = null;
		GenericPreferencesDTO dto = GenericPreferencesDAO.getGenericPreferences();
	    int center_Id = (Integer) request.getSession(false).getAttribute("centerId");
		Bill bill = null;
		billDetails = billBOObj.getBillDetails(billNo);
		if (billDetails != null) 
			bill = billDetails.getBill();

		Boolean multiVisitBill = billBOObj.checkMultiVisitBill(billNo, bill.getVisitId());
		request.setAttribute("multiVisitBill", multiVisitBill ? "Y" : "N");
		if(null != patientDepositBean) {
			//if the Deposit Availability needs to be shown at Center Level
		    if ("E".equals(dto.getEnablePatientDepositAvailability())) {
		      if (multiVisitBill) {
			    BasicDynaBean bean = DepositsDAO.getPackageDepositDetails(billNo,center_Id);
	            returnedData.put("depositDetails", bean.getMap());
		      } else {
			    BasicDynaBean bean = DepositsDAO.getBillDepositDetails(billNo, true, bill != null ? bill.getVisitType() : null,center_Id);
			    returnedData.put("depositDetails", bean.getMap());
		      }
		    }else {
		      if (multiVisitBill) {
		        BasicDynaBean bean = DepositsDAO.getPackageDepositDetails(billNo);
		        returnedData.put("depositDetails", bean.getMap());
		      } else {
		        BasicDynaBean bean = DepositsDAO.getBillDepositDetails(billNo, true, bill != null ? bill.getVisitType() : null);
		        returnedData.put("depositDetails", bean.getMap());
		       }
		    }

		  //if the Deposit Availability needs to be shown at Center Level
		    if ("E".equals(dto.getEnablePatientDepositAvailability())) {
		      BasicDynaBean ipDepositBean = DepositsDAO.getIPBillDepositDetails(billNo,center_Id);
			  if (null != ipDepositBean){
			    returnedData.put("ipDepositDetails", ipDepositBean.getMap());
			  }
		    }else {
		        BasicDynaBean ipDepositBean = DepositsDAO.getIPBillDepositDetails(billNo);
		        if (null != ipDepositBean){
		          returnedData.put("ipDepositDetails", ipDepositBean.getMap());
		        }
		     }
		}

		JSONSerializer js = new JSONSerializer();
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.deepSerialize(returnedData));
		response.flushBuffer();
		return null;
	}

	/**
	 * Split discount.
	 *
	 * @param charge          Main charge
	 * @param packageCharge   Package charge
	 * @param packageDiscount Package discount
	 *
	 * @return BigDecimal
	 */
	public BigDecimal discountSplit(BigDecimal charge, BigDecimal packageCharge, BigDecimal packageDiscount) {
		BigDecimal discount = BigDecimal.ZERO;
		if ((packageCharge.compareTo(BigDecimal.ZERO) != 0) && (packageDiscount.compareTo(BigDecimal.ZERO) != 0)) {
			BigDecimal newCharg = charge.divide(packageCharge, 10, RoundingMode.CEILING);
			discount = (BigDecimal) packageDiscount.multiply(newCharg);
		}
		return discount;
	}

	public ActionForward getCouponRedemptionWidgetUrl(
		      ActionMapping mapping,
		      ActionForm form,
		      HttpServletRequest request,
		      HttpServletResponse response)
			throws IOException, SQLException {

		String billNumber = request.getParameter("bill_number");
		String mobileNumber = request.getParameter("mobile_number");

		EasyRewardRequest easyRewardRequest = new EasyRewardRequest();
		easyRewardRequest.setBillNumber(billNumber);
		easyRewardRequest.setMobileNumber(mobileNumber);

		EasyRewardResponse easyRewardResponse = new EasyRewardResponse();

		try {
			easyRewardResponse = easyRewardService.couponRedemption(easyRewardRequest);
		} catch (Exception e) {

			if (e instanceof ValidationException) {

				ValidationException validationException = (ValidationException) e;
				String errorMsg = e.getMessage();
				easyRewardResponse.setErrorMsg(errorMsg);
			} else {
				log.error("Exception = " + e.getMessage(), e);
			}
		}
		String result = new ObjectMapper().writeValueAsString(easyRewardResponse);
		response.setContentType("application/json");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(result);
		return null;
	}
  
}

