package com.insta.hms.stores;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.RegistrationBO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.DepositsDAO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.RewardPointsDAO;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.core.inventory.StoresHelper;
import com.insta.hms.dischargemedication.DischargeMedicationDAO;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.insurance.SponsorBO;
import com.insta.hms.insurance.SponsorDAO;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.CenterPreferences.CenterPreferencesDAO;
import com.insta.hms.master.DiscountAuthorizerMaster.DiscountAuthorizerMasterAction;
import com.insta.hms.master.DiscountPlanMaster.DiscountPlanMasterDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDTO;
import com.insta.hms.master.GovtIdentifierMaster.GovtIdentifierMasterDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.MedicineRoute.MedicineRouteDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PlanMaster.PlanMasterDAO;
import com.insta.hms.master.PrescriptionsLabelPrintTemplates.PrescriptionsLabelPrintTemplateDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;
import com.insta.hms.master.StoreMaster.StoreMasterDAO;
import com.insta.hms.master.StoresItemMaster.StoresItemDAO;
import com.insta.hms.messaging.MessageManager;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.insta.hms.outpatient.OPPrescriptionFtlHelper;
import com.insta.hms.outpatient.PrescribeDAO;
import com.insta.hms.pbmauthorization.PBMApprovalsDAO;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;
import com.insta.hms.usermanager.RoleDAO;
import com.lowagie.text.DocumentException;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import flexjson.JSONSerializer;

public class MedicineSalesAction extends DispatchAction {

	static Logger log = LoggerFactory.getLogger(MedicineSalesAction.class);
	GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");
	GenericDAO presInstructionDao = new GenericDAO("presc_instr_master");
	static RetailCustomerDAO rDao = new RetailCustomerDAO();
	private GenericDAO billChargeDao = new GenericDAO("bill_charge");
	private PBMPrescriptionsDAO pbmPrescDAO = new PBMPrescriptionsDAO();
	private PatientInsurancePlanDAO patInsrPlanDao = new PatientInsurancePlanDAO();
	private PlanMasterDAO panMasterDAO = new PlanMasterDAO();
	private SalesClaimDetailsDAO salesClaimDAO = new SalesClaimDetailsDAO();
	GenericDAO pbmMedPrescDAO = new GenericDAO("pbm_medicine_prescriptions");
	GenericDAO pbmApprAmtDetailsDAO = new GenericDAO("pbm_approval_amount_details");
	GenericDAO storeItemDetailsDAO = new GenericDAO("store_item_details");
	GenericDAO storeItemRatesDAO = new GenericDAO("store_item_rates");
	GenericDAO storesDAO = new GenericDAO("stores");
	GenericDAO planDAO = new GenericDAO("insurance_plan_main");
	private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();
	private AllocationService allocationService = (AllocationService) ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");
  static AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);
  static BillBO billBO = new BillBO();
    static PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();

	MedicineRouteDAO routeDAO = new MedicineRouteDAO();
	JSONSerializer js = new JSONSerializer().exclude("class");
	
    private GenericDAO medicineRouteDAO =new GenericDAO("medicine_route");
    private GenericDAO billDao = new GenericDAO("bill");
    private GenericDAO storeSalesDao = new GenericDAO("store_sales_main");
    private GenericDAO patientRegistrationDAO = new GenericDAO("patient_registration");
	/**
	 * @param req - Http request
	 * @param pbmPrescId - Prescription Id of the PBM request
	 * @throws SQLException
	 */
	@IgnoreConfidentialFilters
	public void setMedicineThreshold(HttpServletRequest req, Integer pbmPrescId) throws SQLException{
		
		ArrayList<String> medicineNamesList =  new ArrayList();
		BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
		BigDecimal pbmPriceThresholdPercent = BigDecimal.ZERO;
		
		BigDecimal pbmPriceThreshold = BigDecimal.ZERO;
		try {
			pbmPriceThreshold = (BigDecimal) genericPrefs.get("pbm_price_threshold");
			pbmPriceThresholdPercent = (BigDecimal) pbmPriceThreshold.divide(new BigDecimal(100));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/* Nested loop that checks if the difference between net price for a medicine from 
		 * hospital and insurance claim if the difference in net rate crosses 
		 * a threshold (pbm_price_threshold, defined in Generic preferences)
		 * sets a request parameter containing a list of the medicine names */
		
		BasicDynaBean pbmBean = pbmPrescDAO.findByKey("pbm_presc_id", pbmPrescId);
		Integer storeId = (Integer) pbmBean.get("pbm_store_id");
		BasicDynaBean storesBean= storesDAO.findByKey("dept_id", storeId);
		Integer storeRatePlanId = (Integer) storesBean.get("store_rate_plan_id");

		
		List<BasicDynaBean> pbmMedPrescBeanList = pbmMedPrescDAO.findAllByKey("pbm_presc_id", pbmPrescId);
		
		for (BasicDynaBean pbmMedPrescBean : pbmMedPrescBeanList)
		{	
			//check if medicine has been issued already or not
			//If pbmPriceThreshold set in genPrefs is set to 0. Alert message is not shown.
			if(((String)pbmMedPrescBean.get("issued")).equals("N") && pbmPriceThresholdPercent.compareTo(BigDecimal.ZERO) == 1 )
			{
				//Get Item rate from store_item_rates with store_rate_plan_id & medicine_id
				Integer medicineId = (Integer) pbmMedPrescBean.get("medicine_id");
				Integer pbmMedicinePresId = (Integer)pbmMedPrescBean.get("pbm_medicine_pres_id");
				Map<String, Object> key = new HashMap<String, Object>();
				key.put("store_rate_plan_id", storeRatePlanId);
				key.put("medicine_id", medicineId);
				BasicDynaBean storeItemRatesBean = storeItemRatesDAO.findByKey(key);
				BigDecimal rate = ((BigDecimal)storeItemRatesBean.get("selling_price"));
				BigDecimal medicineQuantity = ((BigDecimal) pbmMedPrescBean.get("medicine_quantity"));
				BigDecimal netInsuranceAmt = BigDecimal.ZERO;
				BigDecimal netHospAmt = BigDecimal.ZERO;
				BigDecimal packageSize = BigDecimal.ZERO;
				if(((BigDecimal) pbmMedPrescBean.get("package_size")) != null)
				{
					packageSize = ((BigDecimal) pbmMedPrescBean.get("package_size"));
					netHospAmt = rate.multiply(medicineQuantity);
				}
	
				//get insurance values
				BasicDynaBean pbmPrescBean = pbmPrescDAO.findByKey("pbm_presc_id", pbmPrescId);
				String requestId = (String)pbmPrescBean.get("pbm_request_id");
				List<BasicDynaBean> pbmApprAmtDetailsBeanList = pbmApprAmtDetailsDAO.findAllByKey("pbm_request_id", requestId);
				for (BasicDynaBean pbmApprAmtDetailsBean : pbmApprAmtDetailsBeanList)
				{
					if(pbmMedicinePresId.equals((Integer)pbmApprAmtDetailsBean.get("pbm_medicine_pres_id")))
					{
						
						try {
							if(pbmApprAmtDetailsBean.get("net") != null)
								netInsuranceAmt = ((BigDecimal)pbmApprAmtDetailsBean.get("net"));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//check if difference between netHospAmt & netInsuranceAmt crosses threshold, if so add to medicineNamesList
						
						if(( netHospAmt.subtract(netInsuranceAmt)).compareTo((netHospAmt.multiply(pbmPriceThresholdPercent))) == 1) 
						{
							BasicDynaBean storeItemDetailsBean = storeItemDetailsDAO.findByKey("medicine_id", medicineId);
							String medicineName = (String)storeItemDetailsBean.get("medicine_name");
							medicineNamesList.add(medicineName);
						}
					}
				}
			}
		}
		if(!medicineNamesList.isEmpty())
			req.setAttribute("pbm_threshold_medicines", medicineNamesList);	
		
	}
	/*
	 * Show the sales screen
	 */
	@IgnoreConfidentialFilters
    public ActionForward getSalesScreen(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res) throws SQLException, ParseException {

    	HttpSession session = req.getSession(false);
    	String dept_id = (String) session.getAttribute("pharmacyStoreId");
    	boolean modPbm = (Boolean)session.getAttribute("mod_eclaim_pbm");
    	int centerId = (Integer) req.getSession(false).getAttribute("centerId");
    	if (dept_id != null && !dept_id.equals("")) {
    		BasicDynaBean dept = storesDAO.findByKey("dept_id", Integer.parseInt(dept_id));
    		String dept_name = dept.get("dept_name").toString();
    		req.setAttribute("dept_id", dept_id);
    		req.setAttribute("dept_name", dept_name);
    		req.setAttribute("allowed_raise_bill", dept.get("allowed_raise_bill"));
    		req.setAttribute("counter_id", dept.get("counter_id"));
    		req.setAttribute("is_sales_store", dept.get("is_sales_store"));
		}
		if (dept_id != null  &&  dept_id.equals("")) {
        	req.setAttribute("dept_id", dept_id);
        }

        String username = (String)session.getAttribute("userid");
        BasicDynaBean uBean = new GenericDAO("u_user").findByKey("emp_username", username);
        if (uBean != null)
        	req.setAttribute("isSharedLogIn", uBean.get("is_shared_login"));
        req.setAttribute("actionId", am.getProperty("action_id"));

		String msg = req.getParameter("message");
		String requestURI = req.getRequestURI();
		if (requestURI.contains("MedicineSales.do")){
			req.setAttribute("sale_return", false);
		}
		else if (requestURI.contains("MedicineSalesReturn.do")){
			req.setAttribute("sale_return", true);
		} else if (requestURI.contains("Estimate.do")) {
			req.setAttribute("transaction", "estimate");
		}
		if (msg != null) {
			req.setAttribute("message",msg);
		}		
		String pbm_presc_id = req.getParameter("pbm_presc_id");
		boolean modEclaimErx = (Boolean)req.getSession(false).getAttribute("mod_eclaim_erx");
		String erx_pbm_presc_id = req.getParameter("erx_pbm_presc_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		if (pbm_presc_id != null && modPbm && healthAuthority.equals("HAAD")) {
			setMedicineThreshold(req, Integer.parseInt(pbm_presc_id));
			List<String> columns = new ArrayList<String>();
			columns.add("pbm_presc_id");
			columns.add("pbm_store_id");
			columns.add("pbm_finalized");
			columns.add("status");

			Map<String, Object> key = new HashMap<String, Object>();
			key.put("pbm_presc_id", Integer.parseInt(pbm_presc_id));
			BasicDynaBean pbmbean = pbmPrescDAO.findByKey(columns, key);

			String finalized = (String)pbmbean.get("pbm_finalized");
			if (finalized.equals("N")) {
				req.setAttribute("error", "The PBM prescription <b>"+pbm_presc_id+"</b> is not finalized.");
			}else {
				Integer pbmStoreId = pbmbean.get("pbm_store_id") != null ? (Integer)pbmbean.get("pbm_store_id") : null;

				if (pbmStoreId == null) {
					req.setAttribute("error", "The PBM prescription <b>"+pbm_presc_id+"</b> has Invalid or No Store.");
				}

				BasicDynaBean dept = storesDAO.findByKey("dept_id", pbmStoreId);
	    		String pbm_store_name = dept.get("dept_name").toString();
	    		req.setAttribute("pbm_store_id", pbmStoreId);
	    		req.setAttribute("pbm_store_name", pbm_store_name);
			}
    		req.setAttribute("pbm_presc_id",pbm_presc_id);
		} else if (modEclaimErx && erx_pbm_presc_id != null  && healthAuthority.equals("DHA")) {
			req.setAttribute("erx_pbm_presc_id",erx_pbm_presc_id);
		}
		List medDosages = medDosageDao.listAll();
		req.setAttribute("medDosages", js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));
		List presInstructions = presInstructionDao.listAll();
		req.setAttribute("presInstructions", js.serialize(ConversionUtils.copyListDynaBeansToMap(presInstructions)));
		
	    RegistrationPreferencesDTO regPrefs =  RegistrationPreferencesDAO.getRegistrationPreferences();
		
		req.setAttribute("regPref", regPrefs);
		req.setAttribute("regPrefJSON", js.serialize(regPrefs));
		req.setAttribute("printTemplate", PrescriptionsTemplateDAO.getTemplateNames());
		req.setAttribute("printLblTemplate", PrescriptionsLabelPrintTemplateDAO.getTemplateNames());
		List<BasicDynaBean> discAuths = DiscountAuthorizerMasterAction.getDiscountAuthorizers(centerId);
		req.setAttribute("discountAuthorizersJSON", js.serialize(ConversionUtils.listBeanToListMap(discAuths)));
		req.setAttribute("discountAuthorizers", discAuths);
		req.setAttribute("getAllCreditTypes", new JSONSerializer().exclude("class").serialize(ConversionUtils.copyListDynaBeansToMap(
				new CardTypeMasterDAO().listAll(null,"status","A",null))));

		boolean mod_reward_points	= (Boolean)session.getAttribute("mod_reward_points");
		if (mod_reward_points) {
			// For pharmacy pre paid sales eligible amount,
			// we need charge service sub group's redeeming cap percent.
			BasicDynaBean phmedChargeHeadBean = new ChargeHeadsDAO().findByKey("chargehead_id", ChargeDTO.CH_PHARMACY_MEDICINE);
			int subGrpId = (phmedChargeHeadBean.get("service_sub_group_id") != null) ?
							(Integer)phmedChargeHeadBean.get("service_sub_group_id") : 0;
			BasicDynaBean serviceSubGrpBean = new ServiceSubGroupDAO().findByKey("service_sub_group_id", subGrpId);
			if (serviceSubGrpBean != null) {
				if (serviceSubGrpBean.get("eligible_to_redeem_points") != null
						&& serviceSubGrpBean.get("eligible_to_redeem_points").equals("Y")) {
					BigDecimal redeemingCapPercent = (serviceSubGrpBean.get("redemption_cap_percent") != null) ?
							(BigDecimal)serviceSubGrpBean.get("redemption_cap_percent") : BigDecimal.ZERO;
					req.setAttribute("redemption_cap_percent", redeemingCapPercent);
				}
			}
		}

		getSalesScreenDetails(req);
		Connection con = null;
		/* Following code is for determining if the logged in user has access to the View/Edit Bill Screen. If he has, then the link
		 * for Net Payments on Bill Selection will be a hyperlink taking the user to the Receipts screen, else it will simply be a label
		 */
		try {
			con = DataBaseUtil.getConnection();
			RoleDAO roleDao = new RoleDAO(con);
			int roleId= (Integer) session.getAttribute("roleId");
			if (roleDao.hasRights("credit_bill_collection", roleId)) {
				req.setAttribute("billAccess", "Y");
			} else{
				req.setAttribute("billAccess", "N");
			}
		} finally {
			DataBaseUtil.closeConnections(con, null);
		}
		List<BasicDynaBean> sugGroupList = PurchaseOrderDAO.getAllSubGroups();
		List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
		req.setAttribute("subGroupListJSON", js.serialize(ConversionUtils.listBeanToListMap(sugGroupList)));
		req.setAttribute("groupList", ConversionUtils.listBeanToListMap(groupList));
		req.setAttribute("groupListJSON", js.serialize(ConversionUtils.listBeanToListMap(groupList)));
		req.setAttribute("discountPlansJSON", js.serialize(ConversionUtils.listBeanToListMap(new GenericDAO("discount_plan_details").listAll(null,"priority"))));
		List<BasicDynaBean> discCategories = DiscountPlanMasterDAO.getDiscountCategoryNames(0);
		req.setAttribute("discountCategories", discCategories);
		BasicDynaBean printpref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
		Integer printerId = (Integer) printpref.get("printer_id");
		req.setAttribute("dischargePrinterId", printerId);
		
		String countryCode = centerMasterDAO.getCountryCode(centerId);
    if(StringUtil.isNullOrEmpty(countryCode)){
      countryCode = centerMasterDAO.getCountryCode(0);
    }
    req.setAttribute("defaultCountryCode", countryCode);
		
    req.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
    req.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());
		
		List govtIdentifier = new GovtIdentifierMasterDAO().listAll(null, "status", "A");
        req.setAttribute("govtIdentifierTypes", govtIdentifier);
        req.setAttribute("govtIdentifierTypesJSON", js.serialize(ConversionUtils.copyListDynaBeansToMap(govtIdentifier)));

        return am.findForward("getscreen");
    }

	@IgnoreConfidentialFilters
	public ActionForward getStockJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
        throws IOException, ServletException, SQLException {

		String[] medicineIdStr = req.getParameterValues("medicineId");
		String deptIdStr = req.getParameter("deptId");
		String includeZeroStr = req.getParameter("includeZeroStock");
		String visitType = req.getParameter("visitType");
		String planIdStr = req.getParameter("planId");
		String storeRatePlanIdStr = req.getParameter("storeRatePlanId");
		String output = req.getParameter("output");

		if ((medicineIdStr == null) || medicineIdStr.length == 0) {
			log.error("getStockJSON: Medicine id is required");
			return null;
		}

		List<Integer> medicineIds = new ArrayList<Integer>();
		for (String medicineId : medicineIdStr) {
			medicineIds.add(Integer.parseInt(medicineId));
		}
		log.debug("Num medicines in query: " + medicineIdStr.length + " array: " + medicineIds.size());

		if ( (deptIdStr == null) || deptIdStr.equals("") ) {
			log.error("getStockJSON: Store ID is required");
			return null;
		}
		int deptId = Integer.parseInt(deptIdStr);

		int planId = 0;
		if ((planIdStr != null) && !planIdStr.equals(""))
			planId = Integer.parseInt(planIdStr);

		int storeRatePlanId = 0;
		if ((storeRatePlanIdStr != null) && !storeRatePlanIdStr.equals(""))
			storeRatePlanId = Integer.parseInt(storeRatePlanIdStr);

		boolean includeZeroStock = true;
		if ((includeZeroStr != null) && !includeZeroStr.equals(""))
			includeZeroStock = includeZeroStr.equalsIgnoreCase("Y");

		boolean outputMap = false;
		if (output != null && output.equals("map"))
			outputMap = true;

		BasicDynaBean storeDetails = StoreDAO.findByStore(deptId);
		Integer centerId = (Integer)storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		List<BasicDynaBean> stock =  MedicineStockDAO.getMedicineStockWithPatAmtsInDept(
				medicineIds, deptId, planId, visitType, includeZeroStock, storeRatePlanId, healthAuthority);

		List<BasicDynaBean> route_list = PharmacymasterDAO.getRoutesOfAdministrationsList(medicineIds);
		JSONSerializer js = new JSONSerializer().exclude("class");
		String stockJSON = null;

		for (BasicDynaBean mbean : stock) {
			for (BasicDynaBean rbean: route_list) {
				if (((Integer) mbean.get("medicine_id")).intValue() ==
						((Integer) rbean.get("medicine_id")).intValue()) {
					mbean.set("route_id", rbean.get("route_id"));
					mbean.set("route_name", rbean.get("route_name"));
				}
			}
		}

		if (outputMap) {
			// for multiple medicines, output a map of medicineId => list of batches
			Map m = ConversionUtils.listBeanToMapListMap(stock, "medicine_id");
			log.debug("Num batches returned: " + stock.size() + " map size " + m.size());
			stockJSON = js.deepSerialize(m);
		} else {
			Map<String, Object> stockMapList = new HashMap<String, Object>();
			List<Map<String, Object>> stockList = ConversionUtils.listBeanToListMap(stock);
			//List<BasicDynaBean> subgroupList = StoresItemDAO.getStoreItemSubGroupDetails(medicineIds.get(0));
			List<BasicDynaBean> subgroupList = StoresItemDAO.getStoreItemSubGroupDetails(medicineIds.get(0), storeRatePlanId, deptId);
			stockMapList.put("batch", stockList);
			stockMapList.put("subgroups", ConversionUtils.listBeanToListMap(subgroupList));
			// just output the list of batches
			stockJSON = js.deepSerialize(stockMapList);
		}

        res.setContentType("text/plain");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        res.getWriter().write(stockJSON);
        res.flushBuffer();
        return null;
	}

	/** Get generic details for a given generic code*/
	@IgnoreConfidentialFilters
	public ActionForward getGenericJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
	    throws Exception {
		JSONSerializer js = new JSONSerializer().exclude("class");
		String genericName = req.getParameter("genericName");
		if ( (genericName==null) || genericName.equals("") ) {
			log.error("getGenericJSON: Generic name is required");
			return null;
		}

		String genericId = GenericMasterDAO.genericNameToId(genericName);


		if ( (genericId==null) || genericId.equals("") ) {
				log.error("getGenericJSON: Generic code is required");
				return null;
			}
			res.setContentType("text/plain");
	        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			js.serialize(GenericMasterDAO.getGenDetailsList(genericId), res.getWriter());

	        return null;
		}


	/*
	 * Submit from the sales screen: make a sale/sales return.
	 */
	public ActionForward makeSale(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException, java.text.ParseException,IOException {

		FlashScope flash = FlashScope.getScope(req);
		Map requestParams = req.getParameterMap();
		AbstractPaymentDetails ppdImpl = AbstractPaymentDetails.getReceiptImpl(AbstractPaymentDetails.PHARMACY_PAYMENT);
		Map resultMap = null;
		Map printParamMap = null;
		String preAuthId = "";

		MedicineSalesForm form = (MedicineSalesForm) af;
		HttpSession session = req.getSession();
		String saleId = null;
		String message = null;
		String forwardStatus = null;
		String status="false";
		Set excludedItems = new HashSet();
		String excludedItemsmsg =null;
		String refundRights=req.getParameter("refundRights");
		Object roleID=  req.getSession(false).getAttribute("roleId");
		String psStatus = req.getParameter("ps_status");
		String erxPbmPrescId = req.getParameter("erx_pbm_presc_id");
		String overallDiscountAuth = req.getParameter("discountAuthName");
		overallDiscountAuth = overallDiscountAuth == null || overallDiscountAuth.equals("")?"0":overallDiscountAuth;
		String storeId = form.getPhStore();
		String depositType = req.getParameter("depositType");
		String transactionType = "";
		if (storeId == null) {
			storeId = "0";
		}
		String visitType = null;
		BasicDynaBean visitBean = null;
		String visitId = req.getParameter("visitId");
		if(visitId != null) {
			visitBean = patientRegistrationDAO.findByKey("patient_id", visitId);
		}
		if (visitBean != null) {
			visitType = (String)visitBean.get("visit_type");
		}
		
		Map<String, String> indentDisStatusMap = new HashMap<String, String>();
        for(int i =0;i<form.getPatientIndentNoRef().length;i++){
          if(!form.getPatientIndentNoRef()[i].isEmpty()) {
            indentDisStatusMap.put(form.getPatientIndentNoRef()[i], form.getDispensedMedicine()[i]);
          }
        }


        String isDoctorExcluded [] = req.getParameterValues("item_excluded_from_doctor");
        String doctorExclusionRemarks[] = req.getParameterValues("item_excluded_from_doctor_remarks");
        MedicineSalesBO medicineSalesBO = new MedicineSalesBO();
		boolean isEstimate = form.getEstimate();

		String saleBasis = form.getSaleBasis();

		if ((form.getSalesReturn())&&("N".equals(refundRights))&&(!roleID.equals(1))&&(!roleID.equals(2))) {
			message="You are not authorized to refund payments. Contact your adminstrator";
			status="true";
			ActionRedirect redirect = new ActionRedirect(am.findForward("makePrint"));
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			redirect.addParameter("sale_return", status);
			flash.info(message);
			return redirect;
		}



		int pbm_presc_id = 0;
		String pbmPrescStr = form.getPbm_presc_id();
		if (pbmPrescStr != null && !pbmPrescStr.equals(""))
			pbm_presc_id = Integer.parseInt(pbmPrescStr);
		MedicineSalesMainDTO sale = new MedicineSalesMainDTO();
		ArrayList<MedicineSalesDTO> saleItems = new ArrayList<MedicineSalesDTO>();
		sale.setSaleItems(saleItems);
		sale.setSaleUnit(req.getParameter("sale_unit"));

		sale.setSaleDate(DateUtil.parseTimestamp(form.getPayDate(), form.getPayTime()));
		if (sale.getSaleDate() == null)
			sale.setSaleDate(DateUtil.getCurrentTimestamp());

		sale.setStoreId(storeId);
		sale.setCounter(form.getCounterId());
		if (requestParams.get("erxReferenceNo") != null) {
		  sale.setErxReferenceNo((String) ((String[]) requestParams.get("erxReferenceNo"))[0]);
		}
		sale.setIsExternalPbm(requestParams.get("isExternalPbm") == null ? false :  true);
		
		if (!form.getDisPer().equalsIgnoreCase("")) {
			sale.setDiscountPer((new BigDecimal(form.getDisPer())));
		}

		if (!form.getDisAmt().equalsIgnoreCase("")) {
			sale.setDiscount(new BigDecimal(form.getDisAmt()));
			sale.setDiscountRemarks(form.getDisRemark());
		} else {
			sale.setDiscount(BigDecimal.ZERO);
		}

		if (!form.getRoundOffAmt().equalsIgnoreCase("")) {
			sale.setRoundOffPaise(new BigDecimal(form.getRoundOffAmt()));
		} else {
			sale.setRoundOffPaise(BigDecimal.ZERO);
		}

		if (form.getSalesReturn() && form.getRbillNo() != null && !form.getRbillNo().equalsIgnoreCase("")) {
			BasicDynaBean saleBean = MedicineSalesDAO.getSalesMain(form.getRbillNo());
			if (saleBean == null) {
		     	flash.error("There is no sale bill with sale Id:"+form.getRbillNo());
		     	forwardStatus = "makePrint";

				ActionRedirect redirect = new ActionRedirect(am.findForward(forwardStatus));
				redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
				if (psStatus!= null)
					redirect.addParameter("ps_status", "active");
		        return redirect;
			}
			sale.setRBillNo(form.getRbillNo());
		} else {
			sale.setRBillNo(null);
		}

		sale.setUserRemarks(form.getAllUserRemarks());
		String username = (String)session.getAttribute("userid");

		if(req.getParameter("isSharedLogIn").equals("Y") && isEstimate == false  )
			 username = req.getParameter("authUser");

		sale.setUsername(username);
		List<HashMap> issuedMedicineList = new ArrayList<HashMap>();

		com.insta.hms.master.StoreMaster.StoreMasterDAO storedao = new com.insta.hms.master.StoreMaster.StoreMasterDAO();
		int centerId = (Integer) storedao.findByKey("dept_id", Integer.parseInt(storeId)).get("center_id");
		String prescByGenerics = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();
		Boolean prescriptions_by_generics = prescByGenerics.equals("Y");
		Map medAndQuantityMap = new LinkedHashMap();
		List<BasicDynaBean> groupList = PurchaseOrderDAO.getAllGroups();
		StoresHelper storeHelper = new StoresHelper();
		StockFIFODAO fifoDAO = new StockFIFODAO();
		BasicDynaBean batchQtyBean = null;
		boolean negativeStockDisAllow =
				(GenericPreferencesDAO.getGenericPreferences().getStockNegativeSale().equals("D"));
		for (int i=0; i<form.getMedicineId().length; i++) {
			if (!form.getMedicineId()[i].equals("") && form.getMedicineId()[i] != null) {
				MedicineSalesDTO m = new MedicineSalesDTO();
				String medicineId = form.getMedicineId()[i];
				BigDecimal medQty = new BigDecimal(form.getQty()[i]);
				batchQtyBean = fifoDAO.getBatchLotQtySum(Integer.parseInt(storeId), form.getItemBatchId()[i]);
				if ( (((BigDecimal)batchQtyBean.get("avlb_qty")).subtract(medQty)).compareTo(BigDecimal.ZERO) < 0 && negativeStockDisAllow && form.getSalesReturn() == false ){
						excludedItems.add(batchQtyBean.get("medicine_name").toString());
						forwardStatus = "makePrint";
						continue;//no sale		
				}
				String key = medicineId;
				ArrayList<Map<String, Object>> taxMap = new ArrayList<Map<String, Object>>();
				for(int j=0; j<groupList.size() ;j++) {
					BasicDynaBean groupBean = groupList.get(j);
					Map taxSubDetails = storeHelper.getTaxDetailsMap(requestParams, i, (Integer)groupBean.get("item_group_id"));
					if(taxSubDetails.size() > 0)
						taxMap.add(taxSubDetails);
				}
				m.setTaxMap(taxMap);
				PharmacymasterDAO pmDao = new PharmacymasterDAO();
				if (prescriptions_by_generics) {
					String genericCode = (String) pmDao.findByKey("medicine_id", Integer.parseInt(medicineId)).get("generic_name");
					key = genericCode;
				}

				if (medAndQuantityMap.containsKey(key)) {
					BigDecimal qty = (BigDecimal) medAndQuantityMap.get(key);
					medAndQuantityMap.put(key, qty.add(medQty));
				} else {
					medAndQuantityMap.put(key, medQty);
				}

				m.setMedicineId(medicineId);
				m.setBatchNo(form.getBatchNo()[i]);
				m.setQuantity(medQty);
				if(isDoctorExcluded[i] != null) {
					m.setItemExcludedFromDoctor(new Boolean(isDoctorExcluded[i]));
					m.setItemExcludedFromDoctorRemarks(doctorExclusionRemarks[i]);
				}

				String allowZeroClaimfor=(String) pmDao.findByKey("medicine_id", Integer.parseInt(medicineId)).get("allow_zero_claim_amount");
				if(visitType != null && (visitType.equalsIgnoreCase(allowZeroClaimfor) || "b".equals(allowZeroClaimfor))){
					m.setAllowZeroClaim(true);
				}
				if (saleBasis.equals("M"))
					m.setRate(new BigDecimal(form.getPkgmrp()[i]));
				else
					m.setRate(new BigDecimal(form.getPkgcp()[i]));

				m.setOrigRate(new BigDecimal(form.getOrigRate()[i]));
				m.setMrp(StockEntryDAO.getMRP(form.getItemBatchId()[i]));
				m.setCp(StockEntryDAO.getCP(Integer.parseInt(storeId), Integer.parseInt(form.getMedicineId()[i]), form.getBatchNo()[i]));
				m.setTax(new BigDecimal(form.getTax()[i]));
				m.setOrgTaxAmt(new BigDecimal(form.getOrgTaxAmt()[i]));
				m.setTaxPer(new BigDecimal(form.getTaxPer()[i]));
				m.setPackageUnit(new BigDecimal(form.getPkgUnit()[i]));
				m.setAmount(new BigDecimal(form.getAmt()[i]));
				BigDecimal patAmt = new BigDecimal(form.getPatCalcAmt()[i]);
//TO DO -MP
				ArrayList<BigDecimal> claimTaxAmts = new ArrayList<BigDecimal>();
				BigDecimal totalClaimTaxAmt = BigDecimal.ZERO;
				
				if(form.getPriClaimTaxAmt()[i]!=null && !form.getPriClaimTaxAmt()[i].isEmpty()) {
					totalClaimTaxAmt = totalClaimTaxAmt.add(form.getSalesReturn() ? new BigDecimal(form.getPriClaimTaxAmt()[i]).negate() : new BigDecimal(form.getPriClaimTaxAmt()[i]));
					claimTaxAmts.add(form.getSalesReturn() ? new BigDecimal(form.getPriClaimTaxAmt()[i]).negate() : new BigDecimal(form.getPriClaimTaxAmt()[i]));
				}
					
				if(form.getSecClaimTaxAmt()[i]!=null && !form.getSecClaimTaxAmt()[i].isEmpty()) {
					totalClaimTaxAmt = totalClaimTaxAmt.add(form.getSalesReturn() ? new BigDecimal(form.getSecClaimTaxAmt()[i]).negate() : new BigDecimal(form.getSecClaimTaxAmt()[i]));
					claimTaxAmts.add(form.getSalesReturn() ? new BigDecimal(form.getSecClaimTaxAmt()[i]).negate() : new BigDecimal(form.getSecClaimTaxAmt()[i]));
				}
					
				m.setClaimTaxAmt(claimTaxAmts);
				m.setSponsorTaxAmt(totalClaimTaxAmt);
				if ((form.getPrimclaimAmt()[i] != null 
				    && form.getPrimclaimAmt()[i].compareTo(BigDecimal.ZERO) != 0) 
				    || (form.getSecclaimAmt()[i] != null && form.getSecclaimAmt()[i]
				        .compareTo(BigDecimal.ZERO) != 0)) {
				  m.setInsuranceClaimAmt(m.getAmount().subtract(patAmt).subtract(new BigDecimal(form.getTax()[i])));
				} else {
				  m.setInsuranceClaimAmt(BigDecimal.ZERO);
				}
				m.setInsuranceCategoryId(Integer.parseInt(form.getInsuranceCategoryId()[i]));
				if(!form.getSalesReturn()) {
				  m.setBillingGroupId(form.getBillingGroupId()[i]);
				} 
				SimpleDateFormat fmt = new SimpleDateFormat("MMM-yyyy");
				m.setExpiryDate(!form.getExpiry()[i].equals("(---)") ? new java.sql.Date(fmt.parse(form.getExpiry()[i]).getTime()) : null);
				m.setMedDiscRS(new BigDecimal(form.getMedDiscRS()[i]));
				m.setMedDiscType(form.getMedDiscType()[i]);
				String disc = form.getMedDisc()[i];

				if ((disc != null) && !disc.equals("")) {
					m.setMedDisc(new BigDecimal(disc));
				} else {
					m.setMedDisc(BigDecimal.ZERO);
				}	
				m.setBasis(saleBasis);
//consultation and managment
				m.setFrequency(form.getFrequency()[i]);
				m.setDosage(form.getDosage()[i]);
				m.setDosage_unit(form.getDosageUnit()[i]);
				m.setDuration_unit(form.getDurationUnit()[i]);
				m.setDoctor_remarks(form.getDoctorRemarks()[i]);
				m.setSpecial_instr(form.getSpecial_instr()[i]);
				m.setSales_remarks(form.getSalesRemarks()[i]);
				m.setWarning_label(form.getWarningLabel()[i]);
				String duration = form.getDuration()[i];
				if(duration != null && !duration.equals(""))
					m.setDuration(new BigDecimal(duration));

				else
					m.setDuration(BigDecimal.ZERO);

				/*m.setDoctor_remarks(form.getDoctorRemarks()[i]);
				m.setSales_remarks(form.getSalesRemarks()[i]);
				m.setWarning_label(form.getWarningLabel()[i]);*/


				String route =form.getRouteOfAdmin()[i];
				if(route != null && !route.equals(""))
					m.setRoute_of_admin(new BigDecimal(route));
				else
					m.setRoute_of_admin(BigDecimal.ZERO);

				Integer storeCenterId = 0;
				BasicDynaBean storeBean = storesDAO.findByKey("dept_id", Integer.parseInt(storeId));
				if(storeBean != null)
					storeCenterId = (Integer)storeBean.get("center_id");
				String healthAuthority = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(storeCenterId)).getHealth_authority();
				String[] drugCodeTypes = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(healthAuthority).getDrug_code_type();
				BasicDynaBean medCodeBean = StoreItemCodesDAO.getDrugCodeType(Integer.parseInt(form.getMedicineId()[i]),drugCodeTypes);
				if (medCodeBean != null) {
					m.setItemCode((String) medCodeBean.get("item_code"));
					m.setCodeType((String) medCodeBean.get("code_type"));
				}

				String issueUnits = form.getIssueUnits()[i];
				if ((issueUnits != null) && !issueUnits.equals(""))
					m.setSaleUnit("P");
				else
					m.setSaleUnit("I");
				//m.setPreAuthId(form.getPrimpreAuthId()[i]);
				m.setPreAuthId(form.getPreAuthId()[i]);
				m.setPreAuthModeId(form.getPrimpreAuthModeId()[i]);
				m.setItemBatchId(form.getItemBatchId()[i]);

				ArrayList<BigDecimal> claimAmts = new ArrayList<BigDecimal>();
				if ( form.getPrimclaimAmt()[i] != null )
					claimAmts.add(form.getSalesReturn() ? form.getPrimclaimAmt()[i].negate() : form.getPrimclaimAmt()[i]);
				if ( form.getSecclaimAmt()[i] != null )
					claimAmts.add(form.getSalesReturn() ? form.getSecclaimAmt()[i].negate() : form.getSecclaimAmt()[i]);
				m.setClaimAmts(claimAmts);//set claim amts list
				
				ArrayList<Boolean> includeInClaim = new ArrayList<Boolean>();
				if ( form.getPriIncludeInClaim()[i] != null ) 
					includeInClaim.add(form.getPriIncludeInClaim()[i].equals("Y"));
				if ( form.getSecIncludeInClaim()[i] != null ) 
					includeInClaim.add(form.getSecIncludeInClaim()[i].equals("Y"));
				m.setInclude_in_claim_calc(includeInClaim);
				


				ArrayList<String> priAuthIds = new ArrayList<String>();
				priAuthIds.add(form.getPrimpreAuthId()[i]);
				priAuthIds.add(form.getSecpreAuthId()[i]);//set prior auth ids
				m.setPriorAuthIds(priAuthIds);

				ArrayList<Integer> priAuthModes = new ArrayList<Integer>();
				priAuthModes.add(form.getPrimpreAuthModeId()[i]);
				priAuthModes.add(form.getSecpreAuthModeId()[i]);//set prior auth modes
				m.setPriorAuthMode(priAuthModes);
				
				m.setErxActivityId(form.getErxActivityId()[i]);
				
				saleItems.add(m);
				preAuthId = m.getPreAuthId();

				HashMap consMap = new HashMap();
				int medicationId = ( (form.getMedicationId()[i] != null ) &&
						(!form.getMedicationId()[i].equals("")) && (!form.getMedicationId()[i].equals("undefined"))) ? Integer.parseInt(form.getMedicationId()[i]) : -1;
				int consId = ( (form.getConsultId()[i] != null ) &&
									(!form.getConsultId()[i].equals("")) ) ? Integer.parseInt(form.getConsultId()[i]) : -1;
				int medPrescribedId = ( (form.getMedPrescribedId()[i] != null ) &&
							(!form.getMedPrescribedId()[i].equals("")) ) ? Integer.parseInt(form.getMedPrescribedId()[i]) :-1;
				String pkgUnit = form.getPkgUnit()[i];
				consMap.put("medicine_name", form.getMedName()[i]);
				consMap.put("op_medicine_pres_id", medPrescribedId); // which is used to print prescription label print
				consMap.put("consultation_id", consId);
				consMap.put("medication_id", medicationId);
				consMap.put("issed_qty", form.getQty()[i]);
				consMap.put("pkgUnit", pkgUnit);
				issuedMedicineList.add(consMap);

			}
		}
		
		StringBuffer sb = new StringBuffer();
		if (excludedItems.size() > 0) {
			Iterator it = excludedItems.iterator();
			while (it.hasNext()) {
				sb.append((String) it.next());
				sb.append(", ");
			}
			sb.deleteCharAt(sb.length() - 2);
			excludedItemsmsg = sb.toString();
		}
		
		if ( excludedItems.size() > 0 && ( form.getBillType().equalsIgnoreCase("BN-I")
	            || form.getBillType().equalsIgnoreCase("BN") ) ){
			
			ActionRedirect redirect = new ActionRedirect(am.findForward(forwardStatus));
			if (psStatus!= null)
				redirect.addParameter("ps_status", "active");
			forwardStatus = "makePrint";
			flash.error("Insufficient Quantity for the following items:" +"<br/>"+excludedItemsmsg+"<br/>"+ " Failed to complete sales");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
			
	        return redirect;
		}
		
		if (sale.getSaleItems().isEmpty()){
			ActionRedirect redirect = new ActionRedirect(am.findForward(forwardStatus));
			if (psStatus!= null)
				redirect.addParameter("ps_status", "active");

			if (pbm_presc_id != 0) {
				redirect = new ActionRedirect(am.findForward("pbmApprovalsRedirect"));
				redirect.addParameter("pbm_finalized", "Y");
				redirect.addParameter("pbm_presc_status", "S");
				redirect.addParameter("pbm_presc_status", "D");
			}
	     	forwardStatus = "makePrint";
			flash.error("Insufficient Quantity for all items");
			redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

	        return redirect;
		}

		List<Receipt> receiptList= null;
		if( form.getBillType().equalsIgnoreCase("BN-I")
	            || form.getBillType().equalsIgnoreCase("BN") ){
	            receiptList = ppdImpl.processReceiptParams(requestParams);
	        }
		
		if (form.getSalesReturn() == true) {
			transactionType = "R";
			sale.setChange_source("SalesReturns");
		} else {
			transactionType = "S";
			sale.setChange_source("Sales");
		}

		String salesType = form.getSalesType();
		if (salesType.equals("returnBill")) {
			salesType = form.getReturnPatientType();
		}

		BasicDynaBean bedBean = null;
		int bedNo = 0;
		String wardNo = null;

		BasicDynaBean visitOrgDetails = null,genRatePlanDetails = null;
		if ( visitBean != null )
			visitOrgDetails = new OrgMasterDao().findByKey("org_id",visitBean.get("org_id"));
		genRatePlanDetails = new OrgMasterDao().findByKey("org_id","ORG0001");


		Object storeRatePlanId = null;
		BasicDynaBean storeBean = storesDAO.findByKey("dept_id", Integer.parseInt(storeId));

		if ( salesType.equals("hospital") )//patient sales
			storeRatePlanId = visitOrgDetails.get("store_rate_plan_id") == null
										? storeBean.get("store_rate_plan_id") : visitOrgDetails.get("store_rate_plan_id");
		else// retail/credit sales
			storeRatePlanId = storeBean.get("store_rate_plan_id");

		if (visitType != null && visitId!= null && visitType.equals("i")) {
			bedBean = new GenericDAO("admission").findByKey("patient_id", visitId);
		}
		if (bedBean != null) {
			bedNo = (Integer)bedBean.get("bed_id");
		}

		BasicDynaBean wardBean = new GenericDAO("bed_names").findByKey("bed_id", bedNo);

		if (wardBean != null) {
			wardNo = (String)wardBean.get("ward_no");
		}

		sale.setWardNo(wardNo);
		sale.setPreAuthId(preAuthId);
		if (StringUtils.isNotBlank(form.getDiscountPlanId())) {
			sale.setDiscountPlan(Integer.parseInt(form.getDiscountPlanId()));
		} else if (StringUtils.isNotBlank(form.getDiscountCategory())) {
			sale.setDiscountPlan(Integer.parseInt(form.getDiscountCategory()));
		}

		if (salesType.equals("retail")) {
			BasicDynaBean cust = rDao.getBean();
			cust.set("customer_name", form.getCustName());
			cust.set("visit_date", DateUtil.getCurrentTimestamp());
			cust.set("center_id", centerId);
			if (form.getSalesReturn()) {
			  cust.set("nationality_id", form.getrNationalityId());
			  cust.set("identifier_id", form.getrIdentifierId());
			} else {
			  cust.set("nationality_id", form.getNationalityId());
			  cust.set("identifier_id", form.getIdentifierId());
			}
			cust.set("government_identifier", form.getGovernmentIdentifier());
			cust.set("phone_no", form.getRetailPatientMobileNo());
			
			sale.setDoctor(form.getCustDoctorName());
			resultMap = MedicineSalesBO.retailMedicineSale(cust, "BN", form.getCreditBillNo(),
					form.getSalesReturn(), sale, receiptList, form.getExistingCustomer(),
					form.getCustDoctorName(), isEstimate, null,
					storeRatePlanId, overallDiscountAuth, 0, null);

			forwardStatus = "makePrint";

		} else if (salesType.equals("retailCredit")) {
			BasicDynaBean cust = rDao.getBean();
			cust.set("customer_name", form.getCustRetailCreditName());
			cust.set("visit_date", DateUtil.getCurrentTimestamp());
			cust.set("phone_no", form.getCustRCreditPhoneNo());
			cust.set("center_id", centerId);
			sale.setDoctor(form.getCustRetailCreditDocName());

			sale.setSponserName(form.getCustRetailSponsor());
			if (!form.getCustRCreditLimit().equalsIgnoreCase("")
					&& (!form.getCustRCreditLimit().equalsIgnoreCase("."))) {
				cust.set("credit_limit", new BigDecimal(form.getCustRCreditLimit()));

			} else {
				cust.set("credit_limit", BigDecimal.ZERO);
			}
			resultMap = MedicineSalesBO.retailMedicineSale(cust, "BL", form.getCreditBillNo(),
					form.getSalesReturn(), sale, receiptList, form.getExistingCustomer(),
					form.getCustRetailCreditDocName(), isEstimate, req.getParameter("retailCustomerId"),
					storeRatePlanId,overallDiscountAuth, 0, null);

			forwardStatus = "makePrint";

		} else {
			sale.setDoctor(form.getPatientDoctor());

			boolean returnAgstVisit = false;
			if (form.getSalesReturn()) {
				if ( (form.getReturnType() == null || form.getReturnType().equals("ROAOB"))
						&& (form.getRbillNo() == null || form.getRbillNo().isEmpty())
						&& (form.getVisitId() != null && !form.getVisitId().isEmpty()) ) {
					returnAgstVisit = true;
				}
			}

			boolean istpa = false;
			if (form.getIsTpa().equals("Y")){
				istpa = true;
			}
			String planId = "";
			if (form.getPlanId() != null && !(form.getPlanId().equals("null"))){
				planId = form.getPlanId();
			}
			int patientPbmPrescId = pbm_presc_id;
			boolean modEclaimErx = (Boolean)req.getSession(false).getAttribute("mod_eclaim_erx");
			if (modEclaimErx && erxPbmPrescId != null && !"".equals(erxPbmPrescId))
				patientPbmPrescId = Integer.parseInt(erxPbmPrescId);
			resultMap = MedicineSalesBO.patientMedicineSale( form.getVisitType(), form.getVisitId(),
					form.getBillType(), form.getCreditBillNo(), form.getSalesReturn(), returnAgstVisit, sale,
					receiptList,form.getConsultationId(),form.getDispensedMedicine(),
					isEstimate,form.getDepositsetoff(), form.getDispenseStatus(), planId, istpa, medAndQuantityMap,
					prescriptions_by_generics, storeRatePlanId,overallDiscountAuth,
					form.getRewardPointsRedeemed(),form.getQty(),indentDisStatusMap, 
					patientPbmPrescId, depositType, form.getMedicationId(), form.getDischargeId());

			if (resultMap.get("error") == null) {
				List<String> charges = (ArrayList)resultMap.get("chargesUpdated");
				List<String> bills = new ArrayList<String>();
				if ( charges != null ){
					for (String chargeId : charges) {
						BasicDynaBean chargeBean = billChargeDao.findByKey("charge_id", chargeId);
						String billNo = (chargeBean != null) ? (String)chargeBean.get("bill_no") : null;
						if (billNo != null && !bills.contains(billNo))
							bills.add(billNo);
					}
				}

				for (String billNo : bills) {
					BillDAO.resetTotalsOrReProcess(billNo);
				}

				if (sale.getBillNo() != null && !sale.getBillNo().equals("")) {
					if (form.getBillType().equals("BN") || form.getBillType().equals("BN-I")
						  || form.getCreditBillNo().equalsIgnoreCase("")
							|| form.getCreditBillNo().equalsIgnoreCase("BL")) {

						BillDAO.setDeductionAndSponsorClaimTotals(sale.getBillNo());

					}else {
						BillDAO.resetTotalsOrReProcess(sale.getBillNo());
					}
				}
			}

			if (form.getBillType().equalsIgnoreCase("BN")) {
				forwardStatus = "makePrint";
			} else {
				forwardStatus = "makePrint";
			}
		}
		
		SponsorDAO sponsorDAO = new SponsorDAO();
		
		if(!isEstimate && resultMap.get("error") == null) {
			BasicDynaBean billBean = medicineSalesBO.getBillDetails(sale.getSaleId());
			if(billBean != null) {
				String patientId = (String)billBean.get("visit_id");
				if(salesType.equals("hospital")) {
					//Recalculate the sponsor amount after sale/return.
					new SponsorBO().recalculateSponsorAmount(patientId);
					
					//Update the bill_charge_claim table with tax amount.
					new SponsorDAO().updateSalesBillChargeClaimTax(patientId);

				}
				//Added for KSA sales to over ride the item tax split with claim tax split.
				new MedicineSalesBO().updateStoreSalesTaxDetailsForSale(sale, transactionType);
				
				//Added for KSA because after returns sponsor tax amount are adjusted.
				if(transactionType.equals(MedicineSalesMainDTO.TYPE_SALES_RETURN))
					new MedicineSalesBO().updateStoreSalesTaxDetailsForVisit(patientId);
				
				//Update the bill_charge_tax and bill_charge_claim_tax table with sales entries.
				sponsorDAO.insertOrUpdateBillChargeTaxesForSales(patientId, false);	
				
				//update bill_charge with sales exact tax
			        sponsorDAO.addupSaleTaxtoBillChrgeTax(sale.getSaleId(),sale.getChargeId());
			        
			        //udpate bill charge claims with sales claim tases
			        sponsorDAO.addupSaleClaimTaxtoBillChrgeClaimTax(sale.getBillNo(), sale.getChargeId());
			        
			
			}
		}

		String printerTypeStr = req.getParameter("printerType");
		String labelPrintTypeStr = req.getParameter("labelPrinterType");

		if (resultMap.get("error") == null) {

			if (!form.getSalesReturn()) {
				if (!isEstimate)
					message = "Medicine sales has been done successfully";
				else
					message = "Estimation created";

				status="false";

			} else if (form.getSalesReturn()) {
				message="Medicine refunded successfully";
				status="true";
			}

			
			if (excludedItemsmsg != null && !excludedItemsmsg.isEmpty() && !form.getSalesReturn()) {
				
				flash.info(message + "<br/>"+" Pharmacy Sales has been completed excluding the below items due to insufficient quantity in the store: <b>" 
						+ excludedItemsmsg+"</b>");
			} else
				flash.info(message);

      // Schedule Accounting for sales
      // Get the bills for processing Accounting
      if (resultMap.get("error") == null) {
        List<String> charges = (ArrayList) resultMap.get("chargesUpdated");
        List<String> billsToProcessAccounting = new ArrayList<String>();
        if (charges != null) {
          for (String chargeId : charges) {
            BasicDynaBean chargeBean = billChargeDao.findByKey("charge_id", chargeId);
            String billNo = (chargeBean != null) ? (String) chargeBean.get("bill_no") : null;
            if (form.getSalesReturn() && !chargeBean.get("charge_group").equals("RET") ) {
            	BasicDynaBean thisChargeBill = BillDAO.getBillBean(billNo);
            	if (!(Boolean)thisChargeBill.get("is_tpa")) {
            		continue;
            	}
            }
            if (billNo != null && !billsToProcessAccounting.contains(billNo))
              billsToProcessAccounting.add(billNo);
          }
        }

        if (resultMap.containsKey("hospBillNo") && resultMap.get("hospBillNo") != null
            && !billsToProcessAccounting.contains(resultMap.get("hospBillNo"))) {
          billsToProcessAccounting.add((String) resultMap.get("hospBillNo"));
        }
        // Schedule the accounting
        if (!billsToProcessAccounting.isEmpty()) {
          Set<String> billsSet = new HashSet<>(billsToProcessAccounting);
          List<BasicDynaBean> billsList = billBO.getBillBeans(billsSet);
          accountingJobScheduler.scheduleAccountingForSales(billsList);
        }
      }

			// Print the sales bill (and/or) if receipts exists print the receipts.
			printParamMap = new HashMap();
			saleId = sale.getSaleId();

			GenericPreferencesDTO prefs = GenericPreferencesDAO.getGenericPreferences();
			if (prefs.getSalesReturnsPrintType().equalsIgnoreCase("O"))
				saleId = sale.getRBillNo() == null ? saleId : sale.getRBillNo();
			printParamMap.put("printerTypeStr", printerTypeStr);
			printParamMap.put("labelPrinterType", labelPrintTypeStr);
			printParamMap.put("issuedMedicineList", issuedMedicineList);
			printParamMap.put("consultationIds", form.getConsultationId());
			printParamMap.put("salePrintItems", prefs.getSalesPrintItems());
			printParamMap.put("templateName", req.getParameter("printTemplate"));
			printParamMap.put("visitId", visitId);
			

			printParamMap.put("lblTemplateName", storeBean.get("presc_lbl_template_name"));
			printParamMap.put("saleId", saleId!= null && !saleId.equals("") ? saleId.trim() : saleId);
			printParamMap.put("transaction", isEstimate ? "estimate" :"sales");
			printParamMap.put("visitType", (salesType.equals("retail") || salesType.equals("retailCredit")) ? "r" : "h");
			printParamMap.put("sale_return", status);
			printParamMap.put("saleUnit", req.getParameter("sale_unit"));
			printParamMap.put("medicationIds", form.getDischargeId());
			

			List<String> printURLs = ppdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
			req.getSession(false).setAttribute("printURLs", printURLs);
			
			// Call the allocation job and update the patient payments for the created bill.
			if (sale.getBillNo() != null) {
			  allocationService.updateBillTotal(sale.getBillNo());
			  Map<String,Object> billMap = new HashMap<>();
			  billMap.put("bill_no", sale.getBillNo());
			  BasicDynaBean billBean = billDao.findByKey(billMap);
			  if ("P".equals(billBean.get("payment_status"))) {
	            // Call the Allocation method.
	            allocationService.allocate(sale.getBillNo(), centerId);
			  }
			}
			
			// Calls the allocation job to update allocation for closed and finalized bills
            // if it have sales items.
			if(status.equals("true")) {
			    if (sale.getRBillNo() != null ) {
			      BasicDynaBean bean = storeSalesDao.findByKey("sale_id", sale.getRBillNo());
			      String billNo = bean.get("bill_no").toString();
			      allocationService.updateBillTotal(billNo);
			      // Call the Allocation method.
		        allocationService.allocate(billNo, centerId);
			    } else {
			      List<String> billNumber = BillDAO.getClosedAndFinalizedBillHavingChargeHead(visitId,
	                    "PHCMED");
			      HashSet<String> ubill = new HashSet<String>();
	              for (String bnumber : billNumber) {
	                ubill.add(bnumber);
	              }
	              
	              billNumber = BillDAO.getClosedAndFinalizedBillHavingChargeHead(visitId,
                      "PHMED");
	              for (String bnumber : billNumber) {
                    ubill.add(bnumber);
                  }
	              
	              for (String blNumber : ubill) {
	                allocationService.updateBillTotal(blNumber);
	                // Call the Allocation method.
	                allocationService.allocate(blNumber, centerId);
	              }
			    }
			}

		} else {
	     	message = (String)resultMap.get("error");
	     	flash.error(message);
	     	forwardStatus = "makePrint";
		}
		
		if(status.equals("true"))
			forwardStatus = "showSalesReturnScreen";
		
		ActionRedirect redirect = new ActionRedirect(am.findForward(forwardStatus));
		if (psStatus!= null)
			redirect.addParameter("ps_status", "active");

		if (pbm_presc_id != 0) {
			redirect = new ActionRedirect(am.findForward("pbmApprovalsRedirect"));
			redirect.addParameter("pbm_finalized", "Y");
			redirect.addParameter("pbm_presc_status", "S");
			redirect.addParameter("pbm_presc_status", "D");
		}
		redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

        return redirect;
	}

	/*
	 * Print the bill.
	 */
	public ActionForward getSalesPrint(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		Map paramMap = new HashMap();
		String saleId = req.getParameter("saleId");
		String visitTypeStr=req.getParameter("visitType");
		String templatename = req.getParameter("templatename");
		BasicDynaBean saleMain = MedicineSalesDAO.getSalesMain(saleId);

		if(saleMain==null)
		{
			req.setAttribute("error", (Object)"There is no bill with bill number "+saleId);
			return am.findForward("reportErrors");
		}
		
		if(templatename != null && !templatename.isEmpty()){
			saleMain.set("template_name", templatename);
		}

		paramMap.put("sale", saleMain);
		paramMap.put("userid",req.getSession().getAttribute("userid"));
		String billNo = (String) saleMain.get("bill_no");

		BasicDynaBean printprefs = null;
		String  printerId = req.getParameter("printerId");

		if ( printerId != null )
			printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY, Integer.parseInt(printerId));
		if ( printprefs == null )
			printprefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_STORE);

		 StringWriter writer = new StringWriter();
		 String templateMode=PharmacyBillPrintHelper.processPharmacyBillTemplate(req, saleMain, saleId,
					billNo, paramMap,writer);
		 String printContent = writer.toString();
		 if(saleMain.get("visit_type")!=null)
			 visitTypeStr=(String) saleMain.get("visit_type");
		//Sharing of Bill over PHR Practo Drive
		if (MessageUtil.allowMessageNotification(req, "general_message_send")) {
			if (saleMain.get("bill_type").equals("P")&& !(("r").equals(visitTypeStr))
					&& !(saleMain.get("status").toString().equals("A"))) {
				// getting printerId from stores table
				int storeId = (Integer) saleMain.get("store_id");
				BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id",storeId);
				String webPrinterId = store.get("web_printer").toString();

				StringWriter webWriter = new StringWriter();
				String webTemplateMode = PharmacyBillPrintHelper.processPharmacyBillTemplate(req, saleMain, saleId,
								billNo, paramMap, webWriter, true);
				String webContent = webWriter.toString();
				Map<String, String> emailBillData = new HashMap<String, String>();
				emailBillData.put("bill_no", billNo);
				emailBillData.put("_report_content", webContent);
				emailBillData.put("_message_attachment", webContent);
				emailBillData.put("message_attachment_name", "Bill_" + billNo);
				emailBillData.put("printtype", webPrinterId);
				emailBillData.put("category", "Pharmacy");
				MessageManager mgr = new MessageManager();
				mgr.processEvent("pharmacy_bill_paid", emailBillData);
			}
		}
		HtmlConverter hc = new HtmlConverter();
		boolean repeatPatHeader = ( printprefs != null ? ((String)printprefs.get("repeat_patient_info")).equals("Y") : false );
		if (printprefs.get("print_mode").equals("P")) {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/pdf");
			try {
				if (templateMode.equals("T")){
					hc.textToPDF(printContent, os, printprefs);
				}else{
					boolean isDuplicate= Boolean.parseBoolean(req.getParameter("duplicate"));
					hc.writePdf(os, printContent, "Pharmacy Bill", printprefs, false, repeatPatHeader, true, true, true, isDuplicate);
				}
			} catch (Exception e) {
				res.reset();
				log.error("Original Template:");
				log.error("Generated HTML content:");
				log.error(printContent);
				throw(e);
			}
			os.close();

			return null;
		} else {
			String textReport = null;
			//text mode
			if (templateMode.equals("T")){
				textReport = printContent;
			}else{
				textReport = new String(hc.getText(printContent, "Pharmacy Bill", printprefs, true, true));
			}
			req.setAttribute("textReport", textReport);
			req.setAttribute("textColumns", printprefs.get("text_mode_column"));
			req.setAttribute("printerType", "DMP");
			return am.findForward("textPrintApplet");
		}
	}

	@IgnoreConfidentialFilters
	public ActionForward getEstimatePrint(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		Map paramMap = new HashMap();
		String estiamteId = req.getParameter("estimateId");
		String visitType = req.getParameter("visitType");

		BasicDynaBean main = MedicineSalesDAO.getEstimateMain(estiamteId);
		paramMap.put("estimate", main);

		if (visitType.equals(Bill.BILL_VISIT_TYPE_RETAIL)) {
			paramMap.put("customer", rDao.getRetailCustomer((String)main.get("visit_id")));
		} else {
			paramMap.put("patient",  VisitDetailsDAO.getPatientVisitDetailsBean((String)main.get("visit_id")));
		}

		List<BasicDynaBean> saleDetails = MedicineSalesDAO.getEstimateList(estiamteId);
		paramMap.put("items",  saleDetails);

		/*
		 * Calculate the VAT for each rate of VAT, also check if discounts are being used
		 */
		HashMap<String, BigDecimal> vatDetails = new HashMap<String, BigDecimal>();
		boolean hasDiscounts = false;
		for (BasicDynaBean b: saleDetails) {
			String rate = b.get("tax_rate").toString();
			BigDecimal taxAmt = (BigDecimal) b.get("tax");
			BigDecimal totalTax = vatDetails.get(rate);
			if (totalTax == null) {
				vatDetails.put(rate, taxAmt);
			} else {
				vatDetails.put(rate, taxAmt.add(totalTax));
			}

			BigDecimal discount = (BigDecimal) b.get("discount");
			if (discount.compareTo(BigDecimal.ZERO) != 0)
				hasDiscounts = true;
		}
		paramMap.put("vatDetails",  vatDetails);
		paramMap.put("hasDiscounts",  hasDiscounts);

		paramMap.put("doctorName", saleDetails.get(0).get("doctor_name"));
		paramMap.put("duplicate", Boolean.parseBoolean(req.getParameter("duplicate")));
		paramMap.put("patientType", visitType);
		BasicDynaBean pref= null;
		int printerId =0;
		String  printerIdStr = req.getParameter("printerId");

		if ((printerIdStr != null) &&  !printerIdStr.equals("")) {
			printerId = Integer.parseInt(printerIdStr);
		}

		pref =  PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY,printerId);
		PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
		String templateContent = printtemplatedao.getCustomizedTemplate(PrintTemplate.EST_PHAR);
		Template t = null;
		if (templateContent == null || templateContent.equals("")) {
			t = AppInit.getFmConfig().getTemplate("PharmacyEstimatePrint.ftl");
		} else {
			StringReader reader = new StringReader(templateContent);
			t = new Template("EstimatePrintTemplate.ftl", reader, AppInit.getFmConfig());
		}
		BasicDynaBean printprefs = PrintConfigurationsDAO.getPageOptions(
				PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);

		StringWriter writer = new StringWriter();
		t.process(paramMap,writer);

		String title = "<div align='center'><b><u> Pharmacy Estimate</u></b></div>";
		String htmlContent = title + writer.toString();
		HtmlConverter hc = new HtmlConverter();
		if (pref.get("print_mode").equals("P")) {
			OutputStream os = res.getOutputStream();
			res.setContentType("application/pdf");
			try {
				hc.writePdf(os, htmlContent, "Pharmacy Estimate", printprefs, false, false, true, true, true, false);
			} catch (Exception e) {
				res.reset();
				log.error("Original Template:");
				log.error(templateContent);
				log.error("Generated HTML content:");
				log.error(htmlContent);
				throw(e);
			}
			os.close();

			return null;
		} else {
			//text mode
			String textReport = new String(hc.getText(htmlContent, "Pharmacy Estimate", printprefs, true, true));
			req.setAttribute("textReport", textReport);
			req.setAttribute("textColumns", printprefs.get("text_mode_column"));
			req.setAttribute("printerType", "DMP");
			return am.findForward("textPrintApplet");
		}
	}

	public ActionForward printPrescLabel(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws Exception {

		String labelPrinterIdStr = req.getParameter("labelPrinterType");
		String saleId = req.getParameter("saleId");

		BasicDynaBean prefs = null;
		String userName = (String)req.getSession(false).getAttribute("userid");

		int printerId = 0;
		if ( (labelPrinterIdStr !=null) && !labelPrinterIdStr.equals("")) {
			printerId = Integer.parseInt(labelPrinterIdStr);
		}
		else {
			printerId = Integer.parseInt(req.getParameter("printerId"));
		}
		// store wise template selection
		BasicDynaBean saleMain = MedicineSalesDAO.getPrescriptionLabelPrintName(saleId);
		int storeId = (Integer) saleMain.get("store_id");
		BasicDynaBean store = new StoreMasterDAO().findByKey("dept_id", storeId);	
		String templateName = (String) store.get("presc_lbl_template_name");

		prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PRESCRIPTION_LABEL,
				printerId);

		String printMode = "P";
		if (prefs.get("print_mode") != null) {
			printMode = (String) prefs.get("print_mode");
		}
		OPPrescriptionFtlHelper ftlHelper = new OPPrescriptionFtlHelper();
		if (printMode.equals("P")) {
			res.setContentType("application/pdf");
			OutputStream os = res.getOutputStream();
			ftlHelper.getPrescriptionLabelFtlReport(saleId, templateName,
					OPPrescriptionFtlHelper.ReturnType.PDF, prefs, os, "",userName);
			os.close();

		} else {
			String textReport = new String(ftlHelper.getPrescriptionLabelFtlReport(saleId, templateName,
					OPPrescriptionFtlHelper.ReturnType.TEXT_BYTES, prefs, 	null, "",userName));
			req.setAttribute("textReport", textReport);
			req.setAttribute("textColumns", prefs.get("text_mode_column"));
			req.setAttribute("printerType", "DMP");
			return am.findForward("textPrintApplet");

		}

		return null;
	}

	/*
	 * Private method common for getting the attributes required for the sales screen
	 */
	private void getSalesScreenDetails(HttpServletRequest req) throws SQLException, ParseException {
		MedicineSalesBO bo = new MedicineSalesBO();
		JSONSerializer js = new JSONSerializer().exclude("class");

		/*
		 * Attributes required for generating the page
		 */
		// none, as yet.

		/*
		 * Attributes required dynamically in the UI
		 */
		HttpSession session = req.getSession();
		int roleId= (Integer) session.getAttribute("roleId");
		List retailDocNames=RetailCustomerDAO.getDoctorNames();
		List prescribedDocNames = MedicineSalesDAO.getDoctorNames();

		BasicDynaBean bean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_PHARMACY);
		BasicDynaBean prescBean = PrintConfigurationsDAO.getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_PRESCRIPTION_LABEL);
		req.setAttribute("bean", bean);
		req.setAttribute("prescBean", prescBean);
		req.setAttribute("roleId", roleId);
    	req.setAttribute("retailDocNames",js.serialize(retailDocNames));
    	req.setAttribute("prescribedDocNames",js.serialize(prescribedDocNames));

    Integer centerId = (Integer)session.getAttribute("centerId");
    	
		List stores = DirectStockEntryDAO.getActiveDeptNames(centerId);
		req.setAttribute("stores", stores);
		req.setAttribute("storesJSON", js.serialize(stores));

		BasicDynaBean prefs = GenericPreferencesDAO.getAllPrefs();
		GenericPreferencesDTO prescPrefs = GenericPreferencesDAO.getGenericPreferences();
		
		BasicDynaBean centerPrefs = new CenterPreferencesDAO().getCenterPreferences(centerId);
		req.setAttribute("prefs", prefs.getMap());
		req.setAttribute("centerPrefs", centerPrefs);
		req.setAttribute("salePrintItems", prescPrefs.getSalesPrintItems());

		String defaultStoreIdStr = (String) session.getAttribute("pharmacyStoreId");
		if (defaultStoreIdStr != null && !defaultStoreIdStr.equals("")) {
			int defaultStoreId = Integer.parseInt(defaultStoreIdStr);
			req.setAttribute("stock_ts", MedicineStockDAO.getStoreStockTimestamp(defaultStoreId));
		}
		req.setAttribute("master_timestamp", PharmacymasterDAO.getItemMasterTimestamp());
		req.setAttribute("genericNames", js.serialize(
				ConversionUtils.copyListDynaBeansToMap(new GenericDAO("generic_name").listAll(null, "status","A"))));
		req.setAttribute("allCenterPrefsJson", js.serialize(ConversionUtils.listBeanToListMap(CenterPreferencesDAO.getAllCentersPreferences())));
	}

	public ActionForward getPatientDetails(ActionMapping am, ActionForm af, HttpServletRequest req,
			HttpServletResponse res) throws SQLException, IOException, ParseException,TemplateException,
		   ServletException,DocumentException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		DepositsDAO dao= new DepositsDAO();
		VisitDetailsDAO visitDao = new VisitDetailsDAO();
		res.setContentType("text/javascript");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		String visitId = req.getParameter("visitId");
		String visitType = "";
		int planId = 0;
		String patientIssue = req.getParameter("patientissue");
		String getSoldItemsList = req.getParameter("includeSoldItems");
		String storeIdStr = req.getParameter("storeId");
		int storeId = Integer.parseInt(storeIdStr);
		String patientIndentNo = req.getParameter("patient_indent_no");

		BasicDynaBean details = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
		if (details == null) {
			res.getWriter().write(js.serialize(null));
			return null;
		}


		//multi-payer
		List<BasicDynaBean> visitPlanDetails =  patInsrPlanDao.listAll(null, "patient_id",visitId,"priority");

		String mrno = (String) details.get("mr_no");
		visitType = (String) details.get("visit_type");
		if (details.get("plan_id") != null) planId = (Integer) details.get("plan_id");

		int storeRatePlanId = 0;
		if (details.get("store_rate_plan_id") != null) {
			storeRatePlanId = (Integer) details.get("store_rate_plan_id");
		}

		boolean PBMPriorAuthRequired = false;
		boolean modEclaimErx = (Boolean)req.getSession(false).getAttribute("mod_eclaim_erx");
		HashMap resultMap = new HashMap();
		if ( getSoldItemsList == null ) {//Prescriptions & Indents are not required in sales returns

			if (details != null && new Boolean(req.getParameter("get_prescriptions"))) {
				String pbmPrescStr = req.getParameter("pbm_presc_id");
				int pbmPrescId = (pbmPrescStr != null && !pbmPrescStr.equals(""))? Integer.parseInt(pbmPrescStr) : 0;
				boolean modEclaimPbm = (Boolean)req.getSession(false).getAttribute("mod_eclaim_pbm");
				boolean priorAuthRequired = false;

				/* Prior Auth is required only when mod_eclaim_pbm module is enabled (mod_eclaim_erx is disabled)
				 * and visit type is 'o' (or)
				 * mod_eclaim_erx module is enabled and visit type is 'o'
				 * Hence PBM Auth. required prescriptions sale is done via PBM Approvals.
				 */
				if (!modEclaimErx && modEclaimPbm && visitType.equals("o")) {
					BasicDynaBean planBean = planDAO.findByKey("plan_id", planId);
					if (planBean != null && ((String)planBean.get("require_pbm_authorization")).equals("Y")) {
						priorAuthRequired = true;
						PBMPriorAuthRequired = true;
					}
				} else if (modEclaimErx && visitType.equals("o")) {
					priorAuthRequired = true;
				}

				getStockJSONForPrescription(req, resultMap, details.get("patient_id").toString(), planId,
						storeRatePlanId, priorAuthRequired, pbmPrescId);
				
				getStockJSONForDischargeMedication(req, resultMap, details.get("patient_id").toString(), planId,storeRatePlanId);

				BasicDynaBean pbmPrescDetails = pbmPrescDAO.getPBMPresc(pbmPrescId);
				resultMap.put("pbmPrescDetails", pbmPrescDetails != null ? pbmPrescDetails.getMap() : null);
			}

			if (details != null && new Boolean(req.getParameter("get_indents"))) {
				if (details.get("plan_id") != null) planId = (Integer) details.get("plan_id");
				getIndentDetails(req, resultMap, details.get("patient_id").toString(), planId,
						storeRatePlanId,"I",storeId,null,patientIndentNo);
			}
		}

		// Warn the user to get prior authorization when there are no prescribed medicines.
		// And patient is selected for sale.
		if (resultMap.get("presDetails") != null && ((ArrayList<HashMap>)resultMap.get("presDetails")).size() != 0){}
		else
			resultMap.put("PBMPriorAuthRequired", PBMPriorAuthRequired ? "Y" : "N");

		String seperateCreditBill = "NOTEXISTS";
		String pharmacySeparateCreditBill = "N";
		GenericPreferencesDTO prefs = GenericPreferencesDAO.getGenericPreferences();
		if (patientIssue == null || !patientIssue.equals("Y"))
			pharmacySeparateCreditBill = prefs.getPharmacySeperateCreditbill();

		/*
		 * Get All credit bills of the patient
		 * based on the preference of Separate Pharmacy Credit Bill: 'Y' or 'N'
		 */
		List<BasicDynaBean> patientCreditBills = null;
        // Seperated Two Methods b'coz of In sales, we want to block adding to Bill Now bills, but in issue, we want to allow...
		if (patientIssue != null && patientIssue.equals("Y"))
			patientCreditBills = BillDAO.getVisitOpenBills(visitId);
		else
			patientCreditBills = BillDAO.getVisitCreditBills(null, visitId, pharmacySeparateCreditBill);

		resultMap.put("patientDetails", details.getMap());
		resultMap.put("patient_details_plan_details", ConversionUtils.listBeanToListMap(patInsrPlanDao.getVisitPlanSponsorsDetails(visitId)));
		resultMap.put("visitTotalPatientDue", BillDAO.getVisitPatientDue(visitId));
		resultMap.put("creditLimitDetailsJSON", visitDao.getCreditLimitDetails(visitId));
		
		//multi-payer : set visit plan details
		setPlanDetails(resultMap,visitId);
		resultMap.put("hasPlanVisitCopayLimit", new PlanMasterDAO().hasPlanVisitCopayLimit(planId, visitType));
		resultMap.put("bills", ConversionUtils.listBeanToListMap(patientCreditBills));

		/*
		 * Get the total deposit details, only if deposits are available for pharmacy to use
		 */
		if ("P".equals(prefs.getDeposit_avalibility()) || "B".equals(prefs.getDeposit_avalibility())) {
		    BasicDynaBean depositDetails = dao.getPatientDepositDetails(mrno, true, visitType);
			if (depositDetails != null)
				resultMap.put("deposit", depositDetails.getMap());

			BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrno);
			BigDecimal totalIPDepAvl = BigDecimal.ZERO;
			BigDecimal totalIPsetoff = BigDecimal.ZERO;
			BigDecimal ipDepositAvailable = BigDecimal.ZERO;

			if(null != ipDepositBean) {
				totalIPDepAvl = null != ipDepositBean.get("total_ip_deposits") ? (BigDecimal)ipDepositBean.get("total_ip_deposits") : BigDecimal.ZERO;
				totalIPsetoff = null != ipDepositBean.get("total_ip_set_offs") ? (BigDecimal)ipDepositBean.get("total_ip_set_offs") : BigDecimal.ZERO;
				ipDepositAvailable = totalIPDepAvl.subtract(totalIPsetoff);
			}

			boolean ipDepositExists = ipDepositBean != null && visitType.equals("i") && ipDepositAvailable.compareTo(BigDecimal.ZERO) > 0;

			resultMap.put("ipdeposit", null != ipDepositBean ? ipDepositBean.getMap() : null);
			resultMap.put("ipDepositExists", ipDepositExists);

		}


		/*
		 * Get the reward points details.
		 */
		HttpSession session	= req.getSession();
		boolean mod_reward_points	= (Boolean)session.getAttribute("mod_reward_points");
		BasicDynaBean chargeheadBean = new ChargeHeadsDAO().findByKey("chargehead_id", ChargeDTO.CH_PHARMACY_MEDICINE);
		int serviceSubGroupId = (Integer)chargeheadBean.get("service_sub_group_id");
		BasicDynaBean subgrpbean = new ServiceSubGroupDAO().findByKey("service_sub_group_id", serviceSubGroupId);
		if (subgrpbean != null && ((String)subgrpbean.get("eligible_to_redeem_points")).equals("Y")
				&& mod_reward_points && mrno != null) {
			BasicDynaBean rewardPointsDetails = RewardPointsDAO.getPatientRewardPointsDetails(mrno);
			if (rewardPointsDetails != null)
				resultMap.put("rewardpoints", rewardPointsDetails.getMap());

		}

		/*
		 * check for the current active visit seperate pharmacy bill exists
		 * if Separate Pharmacy Credit Bill preference is 'Y'
		 */
		if (prefs.getPharmacySeperateCreditbill().equals("Y")) {
			BasicDynaBean pbean = patientRegistrationDAO.findByKey("patient_id", visitId);
			if(pbean.get("status").toString().equals("A")) {
				for(BasicDynaBean bean:patientCreditBills) {
					if(visitId.equals(bean.get("visit_id").toString())) {
						seperateCreditBill = "EXISTS";
						break;
					}
				}
			}
		}

		if (getSoldItemsList != null && getSoldItemsList.equals("Y")) {
			List<BasicDynaBean> soldList = null;
			List<BasicDynaBean> soldGrpdList = null;
			soldList = MedicineSalesDAO.getVisitSoldItems(visitId, storeId, planId);

			resultMap.put("soldItemList", ConversionUtils.listBeanToListMap(soldList));
			setSoldItemsMedicineMap(resultMap,soldList);//sold items map with medicineid as key and list of solditems of the medicines as list

			if ( patientIndentNo != null &&  new Boolean(req.getParameter("get_indents")) ) {
				getIndentDetails(req, resultMap, details.get("patient_id").toString(), planId,
						storeRatePlanId,"R",storeId,"S",patientIndentNo);
			}

			setSoldItemsClaimDetails(visitId, storeId,resultMap);;
		}
		if (patientIndentNo != null) {
        	List<BasicDynaBean> returnIndentItems = new StockPatientIssueReturnsDAO().getReturnIndentItemsDetails(patientIndentNo);
        	resultMap.put("returnIndentItems", ConversionUtils.listBeanToMapListMap(returnIndentItems, "item_batch_id"));
        }

		resultMap.put("seperateCreditBill", seperateCreditBill);			// the bill number
		res.getWriter().write(js.deepSerialize(resultMap));

		return null;
	}


	public ActionForward getPatientDetailsBill(ActionMapping am, ActionForm af, HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		JSONSerializer js = new JSONSerializer().exclude("class");
		res.setContentType("text/javascript");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		BasicDynaBean pmsmBean = null;

		String saleId = req.getParameter("saleId");
		int planId = 0;
		if (saleId != null && !saleId.isEmpty()) {
			pmsmBean = storeSalesDao.findByKey("sale_id", saleId);
			if (pmsmBean == null) {
				res.getWriter().write(js.serialize(null));
				return null;
			}
		} else {
			res.getWriter().write(js.serialize(null));
			return null;
		}

		String billNo = (String) pmsmBean.get("bill_no");
		BasicDynaBean billBean = billDao.findByKey("bill_no", billNo);
		String visitId = (String) billBean.get("visit_id");

		BasicDynaBean patBean =  patientRegistrationDAO.findByKey("patient_id", visitId);
		GenericPreferencesDTO prefs = GenericPreferencesDAO.getGenericPreferences();
		String mrno = null;

		if (patBean != null) {
			mrno = (String)patBean.get("mr_no");
		}

		HashMap returnVal = new HashMap();
		if (mrno != null) {
			String seperateCreditBill = "NOTEXISTS";
			BasicDynaBean details = VisitDetailsDAO.getPatientVisitDetailsBean(visitId);
			returnVal.put("retail", "N");
			returnVal.put("patientDetails", details.getMap());
			returnVal.put("patient_details_plan_details", ConversionUtils.listBeanToListMap(patInsrPlanDao.getVisitPlanSponsorsDetails(visitId)));
			if (details.getMap().get("plan_id") != null) planId = (Integer)details.getMap().get("plan_id");
			List<BasicDynaBean> patientCreditBills = BillDAO.getVisitCreditBills(null, visitId,
					prefs.getPharmacySeperateCreditbill());

			returnVal.put("bills", ConversionUtils.listBeanToListMap(patientCreditBills));

			/*
			 * Get all pharmacy deposit details
			 */
			HashMap depositMap = new HashMap();
			depositMap.put("total_deposit_set_off", billBean.get("deposit_set_off"));
			returnVal.put("deposit", depositMap);

			if (prefs.getPharmacySeperateCreditbill().equals("Y")) {
				if(patBean.get("status").toString().equals("A")) {
					for(BasicDynaBean bean:patientCreditBills) {
						if(visitId.equals(bean.get("visit_id").toString())) {
							seperateCreditBill = "EXISTS";
							break;
						}
					}
				}
			}

			returnVal.put("seperateCreditBill", seperateCreditBill);
			setSoldItemsClaimDetailsOfASale(saleId,returnVal);;
			//multi-payer : set visit plan details
			setPlanDetails(returnVal,visitId);

		} else {
			RetailCustomerDAO dao = new RetailCustomerDAO();
			returnVal.put("retail", "Y");
			returnVal.put("retailDetails", dao.getRetailCustomerEx(visitId).getMap());
			if (billBean.get("status").equals("A"))
				returnVal.put("retailBillNo", billNo);
			else
				returnVal.put("retailBillNo", "");
		}

		Integer storeIdStr = (Integer)pmsmBean.get("store_id");
		BasicDynaBean storeDetails = StoreDAO.findByStore(storeIdStr);
		int centerId = (Integer)storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

		List<BasicDynaBean> soldList = MedicineSalesDAO.getSoldItems(saleId, planId, healthAuthority);
		returnVal.put("soldItemList", ConversionUtils.listBeanToListMap(soldList));
		returnVal.put("store", pmsmBean.get("store_id"));
		returnVal.put("doctorName", pmsmBean.get("doctor_name"));
		returnVal.put("sale", pmsmBean.getMap());
		returnVal.put("billType", billBean.get("bill_type"));
		returnVal.put("isTPA", billBean.get("is_tpa"));
		res.getWriter().write(js.deepSerialize(returnVal));

		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getActiveRetailCustomersJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException, IOException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		String deptId = req.getParameter("storeId");
		String query = req.getParameter("query");
		com.insta.hms.master.StoreMaster.StoreMasterDAO dao = new com.insta.hms.master.StoreMaster.StoreMasterDAO();
		int centerId = (Integer) dao.findByKey("dept_id", Integer.parseInt(deptId)).get("center_id");
		res.setContentType("text/plain");
		Map resultMap = new HashMap();
		resultMap.put("result", ConversionUtils.copyListDynaBeansToMap(
				rDao.getActiveCreditCustomers(centerId, query)));

		res.getWriter().write(js.deepSerialize(resultMap));
		return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getRetailSponsorsJSON(ActionMapping am, ActionForm af,
			HttpServletRequest req, HttpServletResponse res)
		throws SQLException, IOException {

		JSONSerializer js = new JSONSerializer().exclude("class");
		res.setContentType("text/javascript");
        res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

		GenericDAO sponsorDAO = new GenericDAO("store_retail_sponsors");
		res.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(
						sponsorDAO.listAll(null,"status","A","sponsor_name"))));
		return null;
	}

	private void getIndentDetails(HttpServletRequest req, HashMap patientIndentDetails,
			String visitId, int planId, int storeRatePlanId, String indentType, int storeId,
			String processType, String patientIndentNo)	throws SQLException {

		StoresPatientIndentDAO indentDAO = new StoresPatientIndentDAO();

		List<BasicDynaBean> indents = null;
			if ( processType == null )//for sales
				indents = indentDAO.getIndentsForProcess(visitId,indentType,storeId,patientIndentNo);
			else//for sales returns
				indents = indentDAO.getIndentsForReturnProcess(patientIndentNo,storeId,"S");

		List<BasicDynaBean> indentDetailsLIst = new ArrayList<BasicDynaBean>();
		List<Integer> medicines = new ArrayList<Integer>();
		String visitType = VisitDetailsDAO.getVisitType(visitId);
		String deptIdStr = req.getParameter("storeId");
		if ( (deptIdStr == null) || deptIdStr.equals("") ) {
			log.error("getStockJSON: Store ID is required");
			return;
		}
		int deptId = Integer.parseInt(deptIdStr);
		BasicDynaBean storeDetails = StoreDAO.findByStore(deptId);
		Integer centerId = (Integer)storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

		if ( processType == null )//for sales
			indentDetailsLIst.addAll(StoresPatientIndentDAO.getIndentDetailsForProcessOfIndentStore(visitId,"F",indentType,storeId,patientIndentNo));
		else
			indentDetailsLIst.addAll(StoresPatientIndentDAO.getIndentDetailsForReturnProcess(patientIndentNo,"F","S"));

		for (BasicDynaBean indentDet : indentDetailsLIst ) {
			medicines.add((Integer)indentDet.get("medicine_id"));
		}

		if ( indentDetailsLIst.size() > 0 ) {
			List<BasicDynaBean> stock = MedicineStockDAO.getMedicineStockWithPatAmtsInDept(
					medicines, deptId, planId, visitType, true, storeRatePlanId, healthAuthority);
				HashMap medBatches = patientIndentDetails.get("medBatches") == null ? new HashMap() : (HashMap)patientIndentDetails.get("medBatches");
				medBatches.putAll(ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
			patientIndentDetails.put("medBatches", medBatches);
		}

		patientIndentDetails.put("patIndentDetails", ConversionUtils.copyListDynaBeansToMap(indentDetailsLIst));
		patientIndentDetails.put("indentsList", ConversionUtils.copyListDynaBeansToMap(indents));
	}

	private void getStockJSONForPrescription(HttpServletRequest req, HashMap patientPrescDetails,
			String visitId, int planId, int storeRatePlanId, boolean pbmAuthRequired, int pbmPrescId)
	throws IOException, SQLException {

		String deptIdStr = req.getParameter("storeId");
		if ( (deptIdStr == null) || deptIdStr.equals("") ) {
			log.error("getStockJSON: Store ID is required");
			return;
		}
		int deptId = Integer.parseInt(deptIdStr);
		BasicDynaBean storeDetails = StoreDAO.findByStore(deptId);
		Integer centerId = (Integer)storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
		boolean modPbm = (Boolean)req.getSession(false).getAttribute("mod_eclaim_pbm");
		boolean modErx = (Boolean)req.getSession(false).getAttribute("mod_eclaim_erx");


		com.insta.hms.master.StoreMaster.StoreMasterDAO storedao = new com.insta.hms.master.StoreMaster.StoreMasterDAO();
		centerId = (Integer) storedao.findByKey("dept_id", deptId).get("center_id");
		String prescByGenerics = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();

		String saleType=req.getParameter("saleType");
		BasicDynaBean visitInsDet = VisitDetailsDAO.getVisitDetails(visitId);
		String visitType = (String)visitInsDet.get("visit_type");
		String orgId = (String)visitInsDet.get("org_id");
		GenericDAO dao = new GenericDAO("doctor_consultation");
		List<BasicDynaBean> consultationList = dao.findAllByKey("patient_id", visitId);
		List<BasicDynaBean> prescriptionList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> genericPresList = new ArrayList<BasicDynaBean>();
		boolean negativeStockAllow =
			(GenericPreferencesDAO.getGenericPreferences().getStockNegativeSale().equals("A"));
		boolean dischargeMedicationExists = false;

		// If patient has NO PBM Auth required then all prescriptions for the patient are fetched.
		// If sale is via PBM Approvals, chosen pbm prescription details are fetched
		// if both mod_eclaim_pbm and mod_eclaim_erx are enabled. check the center level HealthAuthority as well.
		// Currently DHA is for Erx and HAAD is for PBM
        if (!pbmAuthRequired || (modErx && healthAuthority.equals("DHA"))) {
          // For Discharge Mediciation Prescriptions HMS-33076
          if (visitType.equals("i")) {
            List prescriptions = PrescribeDAO.getPharmaMedicinePrescriptions(visitId, modErx);
            if (prescriptions != null || !prescriptions.isEmpty()) {
              prescriptionList.addAll(prescriptions);
            }
          } else {
			for (BasicDynaBean consultbean: consultationList) {
				Boolean prescriptions_by_generics = prescByGenerics.equals("Y");
				if (prescriptions_by_generics) {
					List prescriptions = PrescribeDAO.getPharmaGenericPrescriptions((Integer) consultbean.get("consultation_id"));
					if (prescriptions != null || !prescriptions.isEmpty()) {
						genericPresList.addAll(prescriptions);
					}
				} else {
					List<BasicDynaBean> prescriptions = PrescribeDAO.getPharmaMedicinePrescriptions((Integer) consultbean.get("consultation_id"), modErx);
					if (prescriptions != null || !prescriptions.isEmpty()) {
						for (BasicDynaBean bean: prescriptions) {
							if ((boolean)bean.get("is_discharge_medication")) {
								dischargeMedicationExists = true;
								break;
							}
						}
						prescriptionList.addAll(prescriptions);
					}
				}
			}
          }
		}else if (pbmPrescId != 0) {
			List prescriptions = PrescribeDAO.getPBMMedicinePrescription(pbmPrescId);
			if (prescriptions != null || !prescriptions.isEmpty()) {
				prescriptionList.addAll(prescriptions);
			}
		}

		int pbmPriorAuthModeId = DataBaseUtil.getIntValueFromDb("SELECT prior_auth_mode_id FROM prior_auth_modes WHERE  " +
				"  upper(prior_auth_mode_name)  = 'ELECTRONIC'");
		List<Integer> medicineIds = new ArrayList<Integer>();

		List<HashMap> prescDetailsList = new ArrayList<HashMap>();
		for (BasicDynaBean presciption: prescriptionList) {
			int medicineId = (Integer) presciption.get("medicine_id");
			medicineIds.add(medicineId);

			HashMap patientPrescInfo = new HashMap();

			BigDecimal medicine_quantity = BigDecimal.ONE;
			//prescription can come from different tables where datatype is Integer and BigDecimal
			if (presciption.get("medicine_quantity") != null && presciption.get("medicine_quantity") instanceof Integer) {
			  medicine_quantity = BigDecimal.valueOf((Integer)(presciption.get("medicine_quantity")));
			} else if (presciption.get("medicine_quantity") !=null && presciption.get("medicine_quantity") instanceof BigDecimal) {
			  medicine_quantity = (BigDecimal)(presciption.get("medicine_quantity"));
			}

			BigDecimal issued_qty = ((BigDecimal)presciption.get("issued_qty"));
			BigDecimal reqQty = medicine_quantity.subtract(issued_qty);

			patientPrescInfo.put("medicineId", medicineId);
			patientPrescInfo.put("medicineName", presciption.get("pres_medicine_name"));
			patientPrescInfo.put("qty", reqQty);
			patientPrescInfo.put("total_issed_qty", issued_qty);
			patientPrescInfo.put("qty", reqQty);
			patientPrescInfo.put("consutationId", presciption.get("consultation_id"));
			patientPrescInfo.put("frequency",presciption.get("frequency"));
			patientPrescInfo.put("doctorRemarks",presciption.get("medicine_remarks"));
			patientPrescInfo.put("special_instr",presciption.get("special_instr"));
			patientPrescInfo.put("dosage",presciption.get("strength"));
			patientPrescInfo.put("durationUnit",presciption.get("duration_units"));
			patientPrescInfo.put("erxActivityId",presciption.get("erx_activity_id"));
			patientPrescInfo.put("erxReferenceNo",presciption.get("erx_reference_no"));
			patientPrescInfo.put("item_excluded_from_doctor",presciption.get("item_excluded_from_doctor"));
			patientPrescInfo.put("item_excluded_from_doctor_remarks",presciption.get("item_excluded_from_doctor_remarks"));

   			BigDecimal duration = presciption.get("duration") == null
                     	? BigDecimal.ZERO : new BigDecimal((Integer)presciption.get("duration"));
            BigDecimal routeOfAdmin = presciption.get("route_of_admin") == null
            			? BigDecimal.ZERO : new BigDecimal((Integer)presciption.get("route_of_admin"));
            patientPrescInfo.put("duration",duration);
            patientPrescInfo.put("route",routeOfAdmin);
            if(routeOfAdmin.intValue() != 0){
            	BasicDynaBean routeBean=  medicineRouteDAO.findByKey("route_id", routeOfAdmin.intValue());
                String route_name = (String)routeBean.get("route_name");
                patientPrescInfo.put("routeName", route_name);
            } else {
            	patientPrescInfo.put("routeName", "");
            }

         	String pbmPriorAuthId = "";
			if (presciption.get("pbm_presc_id") != null
					&& (Integer)presciption.get("pbm_presc_id") != 0
					&& modPbm && healthAuthority.equals("HAAD")
					&& pbmAuthRequired) {

				List<String> columns = new ArrayList<String>();
				columns.add("pbm_presc_id");
				columns.add("pbm_store_id");

				Map<String, Object> key = new HashMap<String, Object>();
				key.put("pbm_presc_id", pbmPrescId);
				BasicDynaBean pbmbean = pbmPrescDAO.findByKey(columns, key);

				Integer pbmStoreId = pbmbean.get("pbm_store_id") == null ? 0 : (Integer)pbmbean.get("pbm_store_id");

				String status = (String)presciption.get("pbm_status");
				boolean calcRate = (!status.equals("C"));

				BasicDynaBean activityRateBean = pbmPrescDAO.getPBMPrescItemRateBean(medicineId, reqQty, (String)presciption.get("user_unit"),
						visitId, visitType, orgId, pbmStoreId, planId, calcRate);

				if (!status.equals("C")) {
					patientPrescInfo.put("rate", activityRateBean.get("rate"));
					patientPrescInfo.put("discount", activityRateBean.get("discount"));
					patientPrescInfo.put("amount", activityRateBean.get("amount"));
					patientPrescInfo.put("claim_net_amount", BigDecimal.ZERO);
				}else {
					patientPrescInfo.put("rate", presciption.get("rate"));
					patientPrescInfo.put("discount", presciption.get("discount"));
					patientPrescInfo.put("amount", presciption.get("amount"));
					patientPrescInfo.put("claim_net_amount", presciption.get("claim_net_amount"));
				}
				patientPrescInfo.put("pbmPrescId", presciption.get("pbm_presc_id"));
				patientPrescInfo.put("pbmStatus", presciption.get("pbm_status"));
				patientPrescInfo.put("user_unit", presciption.get("user_unit"));
				patientPrescInfo.put("issue_units", presciption.get("issue_units"));
				patientPrescInfo.put("package_uom", presciption.get("package_uom"));
				patientPrescInfo.put("claim_net_approved_amount", presciption.get("claim_net_approved_amount"));

	    		int pbm_medicine_pres_id = (Integer)presciption.get("pbm_medicine_pres_id");
				BasicDynaBean pbmreqAmtBean = PBMApprovalsDAO.getPBMApprovalBean(pbm_medicine_pres_id);

				BigDecimal pbmPatientShare = BigDecimal.ZERO;
				BigDecimal pbmPaymentAmount = BigDecimal.ZERO;
				BigDecimal pbmNetAmount = BigDecimal.ZERO;
				BigDecimal pbmListPrice = BigDecimal.ZERO;
				BigDecimal pbmMedicineQuantity = BigDecimal.ZERO;

				if (pbmreqAmtBean != null) {
					if (pbmreqAmtBean.get("pbm_auth_id_payer") != null)
						pbmPriorAuthId = (String)pbmreqAmtBean.get("pbm_auth_id_payer");
					if (pbmreqAmtBean.get("patient_share") != null)
						pbmPatientShare = (BigDecimal)pbmreqAmtBean.get("patient_share");
					if (pbmreqAmtBean.get("payment_amount") != null)
						pbmPaymentAmount = (BigDecimal)pbmreqAmtBean.get("payment_amount");
					if (pbmreqAmtBean.get("net") != null)
						pbmNetAmount = (BigDecimal)pbmreqAmtBean.get("net");
					if (pbmreqAmtBean.get("list") != null)
						pbmListPrice = (BigDecimal)pbmreqAmtBean.get("list");

					// For PBM, reset the quantity since quantity should be approved quantity
					// and not prescribed quantity
					// If paymentAmount is greater than 0 for this medicine
					if (pbmPaymentAmount.compareTo(BigDecimal.ZERO) == 1 &&
							pbmreqAmtBean.get("quantity") != null) {
						pbmMedicineQuantity = (BigDecimal)pbmreqAmtBean.get("quantity");
						// if approval qty =0 and has approved amount then make qty as requested qty 
						//for showing that item in sales screen. refer:HMS-4169 
						if(pbmMedicineQuantity.compareTo(BigDecimal.ZERO) == 0)
							reqQty = (BigDecimal)patientPrescInfo.get("qty");
						else
							reqQty = pbmMedicineQuantity.subtract(issued_qty);
					} else
						reqQty = BigDecimal.ZERO;
				}
				patientPrescInfo.put("qty", reqQty);
				patientPrescInfo.put("pbmPatientShare", pbmPatientShare);
				patientPrescInfo.put("pbmPaymentAmount", pbmPaymentAmount);
				patientPrescInfo.put("pbmNetAmount", pbmNetAmount);
				patientPrescInfo.put("pbmListPrice", pbmListPrice);
				patientPrescInfo.put("approvednet",presciption.get("net"));
			}
			patientPrescInfo.put("pbmPriorAuthId", pbmPriorAuthId);
			patientPrescInfo.put("pbmPriorAuthModeId",
					(pbmPriorAuthId != null && !pbmPriorAuthId.trim().equals("")) ? pbmPriorAuthModeId  : "");
			patientPrescInfo.put("medicine_name", presciption.get("pres_medicine_name") == null ?
					presciption.get("generic_name") : presciption.get("pres_medicine_name"));
			prescDetailsList.add(patientPrescInfo);
		}

		patientPrescDetails.put("presDetails", prescDetailsList);
		patientPrescDetails.put("prescriptions_exists", (!prescriptionList.isEmpty() ||
					!genericPresList.isEmpty()));
		patientPrescDetails.put("dischargeMedication_exists", dischargeMedicationExists);

		// details of medicine batches in the prescription
		if (prescriptionList.size() > 0) {
			List<BasicDynaBean> stock = MedicineStockDAO.getMedicineStockWithPatAmtsInDept(
				medicineIds, deptId, planId, visitType, true, storeRatePlanId, healthAuthority);
			patientPrescDetails.put("medBatches", ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));

			List<BasicDynaBean> route_list = PharmacymasterDAO.getRoutesOfAdministrationsList(medicineIds);
			JSONSerializer js = new JSONSerializer().exclude("class");

			for (BasicDynaBean mbean : stock) {
				for (BasicDynaBean rbean: route_list) {
					if (((Integer) mbean.get("medicine_id")).intValue() ==
							((Integer) rbean.get("medicine_id")).intValue()) {
						mbean.set("route_id", rbean.get("route_id"));
						mbean.set("route_name", rbean.get("route_name"));
					}
				}
			}
		}

		if (pbmPrescId != 0) {
			String opType = DataBaseUtil.getStringValueFromDb(" SELECT op_type FROM patient_registration WHERE patient_id = ? ", visitId);
			patientPrescDetails.put("consultantList",
				ConversionUtils.copyListDynaBeansToMap(DoctorConsultationDAO.getPBMConsltDetails(visitId, opType)));
		}else {
			patientPrescDetails.put("consultantList",
				ConversionUtils.copyListDynaBeansToMap(DoctorConsultationDAO.getOPConsltDetails(visitId)));
		}
		return ;
	}

	@IgnoreConfidentialFilters
	public ActionForward search(ActionMapping am,ActionForm af,
			HttpServletRequest req,HttpServletResponse res) throws Exception {

		String searchInput = req.getParameter("query");
		String type = req.getParameter("type");
		List l = null;
	    Map map = new HashMap();
		searchInput = searchInput+ "%";

		List<BasicDynaBean> medicineNames = MedicineSalesDAO.getMedicineNames(searchInput);
	    map.put("result",ConversionUtils.listBeanToListMap(medicineNames));
	    String responseContent = new JSONSerializer().deepSerialize(map);

	    res.setContentType("application/json");
	    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	    res.getWriter().write(responseContent);
	    res.flushBuffer();

		return null;
	}

	 @IgnoreConfidentialFilters
	public ActionForward getEquivalentMedicinesList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException, SQLException {

		String medicineName = request.getParameter("medicineName");
		String genericName = request.getParameter("genericName");
		String storeId = request.getParameter("storeId");
		String saleType=request.getParameter("saleType");
		/*
		 * if allstores is true: it ignores the storeid and search for equivalent medicines from all stores.
		 * which is used in op/ip consultation screen.
		 */
		Boolean allStores = new Boolean(request.getParameter("allStores"));

		response.setContentType("text/javascript");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		List<BasicDynaBean> medicineNames = MedicineStockDAO.getEquivalentMedicinesList(medicineName,
				genericName, storeId, allStores,saleType);
		JSONSerializer js = new JSONSerializer().exclude("class");
		response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(medicineNames)));
		response.flushBuffer();
		return null;
	}

	public ActionForward isSponsorBill(ActionMapping mapping, ActionForm form,
    		HttpServletRequest request, HttpServletResponse res)
    		throws IOException, SQLException, ParseException {
			JSONSerializer js = new JSONSerializer().exclude("class");
    		String billNo = request.getParameter("billNo");
    		String medId = request.getParameter("medName");
    		Boolean isInsurance = false;
    		boolean claimable = false;
    		BasicDynaBean itemBean = storeItemDetailsDAO.findByKey("medicine_id", Integer.parseInt(medId));
    		if (itemBean != null){
    			BasicDynaBean cBean = new GenericDAO("store_category_master").findByKey("category_id",  itemBean.get("med_category_id"));
    			if (cBean != null)
    				claimable = (Boolean)cBean.get("claimable");
    		}
    		if (BillDAO.checkIfsponsorBill(billNo) && claimable){
    			isInsurance = true;
    		}
    		String resp = js.serialize(isInsurance);
    		res.setContentType("text/plain");
            res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    		res.getWriter().write(resp);
            res.flushBuffer();
            return null;
    }

	 @IgnoreConfidentialFilters
	public ActionForward isPostDiscountOrPreDiscountPayable(ActionMapping mapping, ActionForm form,
    		HttpServletRequest request, HttpServletResponse res)
    		throws IOException, SQLException, ParseException {
		String planIdStr = request.getParameter("planId");
		JSONSerializer js = new JSONSerializer().exclude("class");
		res.setContentType("text/javascript");
		res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		if(planIdStr != null && !planIdStr.equals("") && !planIdStr.equals("0")) {
			BasicDynaBean planBean = planDAO.findByKey("plan_id", Integer.parseInt(planIdStr));
			if(planBean != null){
				res.getWriter().write(js.serialize(planBean.get("is_copay_pc_on_post_discnt_amt")));
			}
		}
		res.flushBuffer();
		return null;
	}

	@IgnoreConfidentialFilters
	public void setSoldItemsMedicineMap(Map resultMap , List soldList){

		Map batchWiseMap = ConversionUtils.listBeanToMapListMap(soldList,"batch_no");

		Set<String> batchNokeys = batchWiseMap.keySet();
		List l = new ArrayList<BasicDynaBean>();

		//sum the quantity of same batch
		for(String batchNo : batchNokeys){
			List<DynaBeanMapDecorator> batchWiseList = (List<DynaBeanMapDecorator>)batchWiseMap.get(batchNo);
			BigDecimal qty = BigDecimal.ZERO;

			for(DynaBeanMapDecorator soldBatchDetails : batchWiseList){
				qty = qty.add((BigDecimal)soldBatchDetails.get("qty"));
			}
			Map b = new HashMap(batchWiseList.get(0));
			b.put("qty", qty);
			l.add(b);

		}
		Map medicineWiseMap = ConversionUtils.listMapToMapListMap(l,"medicine_id");


		resultMap.put("soldItemsGrpByMedIdMap", medicineWiseMap);

	}

	/**
	 * Sets insurance plan related details in the map
	 * @param resultMap
	 */
	private void setPlanDetails(Map resultMap,String patientId) throws SQLException{
		Map keys = new HashMap();
		List<BasicDynaBean> visitPlans = patInsrPlanDao.getPlanDetails(patientId);
		resultMap.put("patient_plan_details", ConversionUtils.listBeanToListMap(visitPlans));

		String visitType = VisitDetailsDAO.getVisitType(patientId);
		Map visitPlanMasterDetailsMap = new HashMap<Integer, List<BasicDynaBean>>();
		for(BasicDynaBean visitPlan : visitPlans){
				visitPlanMasterDetailsMap.put(visitPlan.get("plan_id"),ConversionUtils.listBeanToListMap(
						panMasterDAO.getInsuPlanDetails((Integer)visitPlan.get("plan_id"), visitType)));
		}

		resultMap.put("visit_plans_master_details", visitPlanMasterDetailsMap);
	}

	private void setSoldItemsClaimDetails(String visitId, int storeId,Map resultMap)
	throws SQLException{
		resultMap.put("visit_sold_items_claim_details", ConversionUtils.listBeanToListMap(
				salesClaimDAO.getVisitSoldItemsClaimDetails(visitId, storeId)));
	}

	private void setSoldItemsClaimDetailsOfASale(String saleId,Map resultMap)
	throws SQLException{
		resultMap.put("visit_sold_items_claim_details", ConversionUtils.listBeanToListMap(
				salesClaimDAO.getVisitSoldItemsClaimDetailsOfASale(saleId)));
	}

	 @IgnoreConfidentialFilters
	public ActionForward getClaimAmount(ActionMapping am, ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws Exception{

		 int planId = req.getParameter("plan_id") != null ? Integer.parseInt(req.getParameter("plan_id")) : 0;
		 BigDecimal amount = req.getParameter("amount") != null ? new BigDecimal(req.getParameter("amount")) : BigDecimal.ZERO;
		 String visitType = req.getParameter("visit_type");
		 int categoryId = req.getParameter("category_id") != null ? Integer.parseInt(req.getParameter("category_id")) : 0;
		 boolean firstOfCategory = req.getParameter("foc").equals("true");
		 BigDecimal discount = req.getParameter("discount") != null ? new BigDecimal(req.getParameter("discount")) : BigDecimal.ZERO;
	
		 BigDecimal claimAMt = new AdvanceInsuranceCalculator().calculateClaim(amount, discount, null,
				 planId, firstOfCategory, visitType, categoryId);
	
	
		 res.setContentType("text/plain");
	     res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
			 res.getWriter().write(js.serialize(claimAMt));
	     res.flushBuffer();
		 return null;
	}

	@IgnoreConfidentialFilters
	public ActionForward getInsuranceCategoryPayableStatus(ActionMapping am, ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws Exception{

		 int medicineId = req.getParameter("medicineId") != null ? Integer.parseInt(req.getParameter("medicineId")) : 0;
		 String visitId = req.getParameter("visitId") != null ? req.getParameter("visitId") : "";
		 String visitType = req.getParameter("visitType") != null ? req.getParameter("visitType") : "o";
		 List<BasicDynaBean>  calimableStatusList = new ArrayList<BasicDynaBean>();
		 if(!visitId.isEmpty() && !visitType.isEmpty()) {
			 BasicDynaBean calimableStatus = MedicineSalesDAO.getInsuranceCategoryPayableStatus(visitId, medicineId, visitType);
			 JSONSerializer js = new JSONSerializer().exclude("class");
			 calimableStatusList.add(calimableStatus);
		 }
		 res.setContentType("text/plain");
	     res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	     res.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(calimableStatusList)));
	     res.flushBuffer();
		 return null;

	}
	
	@IgnoreConfidentialFilters
	public ActionForward getRatePlanDetails(ActionMapping mapping, ActionForm form,
	    HttpServletRequest request, HttpServletResponse response) throws Exception {
	  String orgId = request.getParameter("orgId");
	  BasicDynaBean orgBean = OrgMasterDao.getOrgdetailsDynaBean(orgId);
	  response.setContentType("text/plain");
	  response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	  response.getWriter().write(js.serialize(orgBean.getMap()));
	  response.flushBuffer();
    return null;
	}
	
	public ActionForward getPatientPolicyId(ActionMapping am, ActionForm af,
            HttpServletRequest req, HttpServletResponse res) throws Exception{

		 String visitId = req.getParameter("visitId");
		 String mrNo = req.getParameter("mrNo");
		 
		 List patientList = ConversionUtils.listBeanToListMap(RegistrationBO.getPatientPlansDetails(mrNo, visitId));
		 
		 JSONSerializer js = new JSONSerializer().exclude("class");
		 res.setContentType("text/plain");
	     res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
	     res.getWriter().write(js.serialize(patientList));
	     res.flushBuffer();
		 return null;

	}
	
	private void getStockJSONForDischargeMedication(HttpServletRequest req, HashMap patientDischrageMedicationDetails,
			String visitId, int planId, int storeRatePlanId)
	throws IOException, SQLException {

		String deptIdStr = req.getParameter("storeId");
		if ( (deptIdStr == null) || deptIdStr.equals("") ) {
			log.error("getStockJSON: Store ID is required");
			return;
		}
		int deptId = Integer.parseInt(deptIdStr);
		BasicDynaBean storeDetails = StoreDAO.findByStore(deptId);
		Integer centerId = (Integer)storeDetails.get("center_id");
		String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);

		com.insta.hms.master.StoreMaster.StoreMasterDAO storedao = new com.insta.hms.master.StoreMaster.StoreMasterDAO();
		centerId = (Integer) storedao.findByKey("dept_id", deptId).get("center_id");
		String prescByGenerics = HealthAuthorityPreferencesDAO.getHealthAuthorityPreferences(
				CenterMasterDAO.getHealthAuthorityForCenter(centerId)).getPrescriptions_by_generics();

		String saleType=req.getParameter("saleType");
		BasicDynaBean visitInsDet = VisitDetailsDAO.getVisitDetails(visitId);
		String visitType = (String)visitInsDet.get("visit_type");
		String orgId = (String)visitInsDet.get("org_id");
		GenericDAO dao = new GenericDAO("discharge_medication");
		List<BasicDynaBean> dischargeList = dao.findAllByKey("visit_id", visitId);
		List<BasicDynaBean> dischargeMedicationList = new ArrayList<BasicDynaBean>();
		List<BasicDynaBean> genericDischargeMedicationList = new ArrayList<BasicDynaBean>();
		boolean negativeStockAllow = (GenericPreferencesDAO.getGenericPreferences().getStockNegativeSale().equals("A"));

		for (BasicDynaBean dischargeMedicationBean: dischargeList) {
			Boolean prescriptions_by_generics = prescByGenerics.equals("Y");
			if (prescriptions_by_generics) {
				List dischargeMedication = DischargeMedicationDAO.getPharmaGenericPrescriptions((Integer) dischargeMedicationBean.get("medication_id"));
				if (dischargeMedication != null || !dischargeMedication.isEmpty()) {
					genericDischargeMedicationList.addAll(dischargeMedication);
				}
			} else {
				List dischargeMedication = DischargeMedicationDAO.getDischargePharmaMedicines((Integer) dischargeMedicationBean.get("medication_id"));
				if (dischargeMedication != null || !dischargeMedication.isEmpty()) {
					dischargeMedicationList.addAll(dischargeMedication);
				}
			}
			
		}

		List<Integer> medicineIds = new ArrayList<Integer>();

		List<HashMap> dischargeMedicationDetailsList = new ArrayList<HashMap>();
		for (BasicDynaBean dischargeMedication: dischargeMedicationList) {
			int medicineId = (Integer) dischargeMedication.get("medicine_id");
			medicineIds.add(medicineId);

			HashMap patientPrescInfo = new HashMap();

			BigDecimal medicine_quantity = BigDecimal.ONE;
			medicine_quantity = dischargeMedication.get("medicine_quantity") == null
				? BigDecimal.ONE : new BigDecimal((Long)dischargeMedication.get("medicine_quantity"));
	
			BigDecimal issued_qty = new BigDecimal((Long)dischargeMedication.get("issued_qty"));
			BigDecimal reqQty = medicine_quantity.subtract(issued_qty);

			patientPrescInfo.put("medicineId", medicineId);
			patientPrescInfo.put("qty", reqQty);
			patientPrescInfo.put("total_issed_qty", issued_qty);
			patientPrescInfo.put("medicationId", dischargeMedication.get("medication_id"));
			patientPrescInfo.put("frequency",dischargeMedication.get("frequency"));
			patientPrescInfo.put("doctorRemarks",dischargeMedication.get("medicine_remarks"));
			patientPrescInfo.put("special_instr",dischargeMedication.get("special_instr"));
			patientPrescInfo.put("dosage",dischargeMedication.get("strength"));
			patientPrescInfo.put("durationUnit",dischargeMedication.get("duration_units"));

   			BigDecimal duration = dischargeMedication.get("duration") == null
                     	? BigDecimal.ZERO : new BigDecimal((Integer)dischargeMedication.get("duration"));
            BigDecimal routeOfAdmin = dischargeMedication.get("route_of_admin") == null
            			? BigDecimal.ZERO : new BigDecimal((Integer)dischargeMedication.get("route_of_admin"));
            patientPrescInfo.put("duration",duration);
            patientPrescInfo.put("route",routeOfAdmin);
            if(routeOfAdmin.intValue() != 0){
            	BasicDynaBean routeBean=  medicineRouteDAO.findByKey("route_id", routeOfAdmin.intValue());
                String route_name = (String)routeBean.get("route_name");
                patientPrescInfo.put("routeName", route_name);
            } else {
            	patientPrescInfo.put("routeName", "");
            }

         	patientPrescInfo.put("medicine_name", dischargeMedication.get("pres_medicine_name"));
         	dischargeMedicationDetailsList.add(patientPrescInfo);
		}

		patientDischrageMedicationDetails.put("dischargeMedicationDetails", dischargeMedicationDetailsList);
		//patientDischrageMedicationDetails.put("dischargeMedication_exists", (!dischargeMedicationDetailsList.isEmpty() ||
        // !genericDischargeMedicationList.isEmpty()));


		// details of medicine batches in the prescription
		if (dischargeMedicationDetailsList.size() > 0) {
			List<BasicDynaBean> stock = MedicineStockDAO.getMedicineStockWithPatAmtsInDept(
				medicineIds, deptId, planId, visitType, true, storeRatePlanId, healthAuthority);
			HashMap medBatches = patientDischrageMedicationDetails.get("medBatches") == null ? new HashMap() : (HashMap)patientDischrageMedicationDetails.get("medBatches");
			medBatches.putAll(ConversionUtils.listBeanToMapListMap(stock, "medicine_id"));
			patientDischrageMedicationDetails.put("medBatches", medBatches);

			List<BasicDynaBean> route_list = PharmacymasterDAO.getRoutesOfAdministrationsList(medicineIds);
		
			for (BasicDynaBean mbean : stock) {
				for (BasicDynaBean rbean: route_list) {
					if (((Integer) mbean.get("medicine_id")).intValue() ==
							((Integer) rbean.get("medicine_id")).intValue()) {
						mbean.set("route_id", rbean.get("route_id"));
						mbean.set("route_name", rbean.get("route_name"));
					}
				}
			}
		}

		patientDischrageMedicationDetails.put("dischargeMedicationList",
			ConversionUtils.copyListDynaBeansToMap(DischargeMedicationDAO.getDischargeMedicationDetails(visitId)));
	}
	public ActionForward depositsSetOffAjax(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {
		HashMap returnedData = new HashMap();
		String mrNo = request.getParameter("mr_no");
		String visitType = request.getParameter("visit_type");
		DepositsDAO dao= new DepositsDAO();
	    BasicDynaBean depositDetails = dao.getPatientDepositDetails(mrNo, true, visitType);
		returnedData.put("deposit", null != depositDetails ? depositDetails.getMap() : null);
		BasicDynaBean ipDepositBean = DepositsDAO.getIPDepositAmounts(mrNo);
		returnedData.put("ipdeposit", null != ipDepositBean ? ipDepositBean.getMap() : null);		
		BigDecimal totalIPDepAvl = BigDecimal.ZERO;
		BigDecimal totalIPsetoff = BigDecimal.ZERO;
		BigDecimal ipDepositAvailable = BigDecimal.ZERO;
		if(null != ipDepositBean) {
			totalIPDepAvl = null != ipDepositBean.get("total_ip_deposits") ? (BigDecimal)ipDepositBean.get("total_ip_deposits") : BigDecimal.ZERO;
			totalIPsetoff = null != ipDepositBean.get("total_ip_set_offs") ? (BigDecimal)ipDepositBean.get("total_ip_set_offs") : BigDecimal.ZERO;
			ipDepositAvailable = totalIPDepAvl.subtract(totalIPsetoff);
		}
		
		boolean ipDepositExists = ipDepositBean != null && visitType.equals("i") && ipDepositAvailable.compareTo(BigDecimal.ZERO) > 0;
		returnedData.put("ipDepositExists", ipDepositExists);
		
		JSONSerializer js = new JSONSerializer();
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.deepSerialize(returnedData));
		response.flushBuffer();
		return null;
	}

	 @IgnoreConfidentialFilters
	public ActionForward getSaleItemTaxdetails(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)throws Exception {
		HashMap returnedData = new HashMap();
		int saleItemId = Integer.valueOf(request.getParameter("sale_item_id"));
		String reqQty = request.getParameter("quantity");
		MedicineSalesBO medicineSalesBO = new MedicineSalesBO();
		Map<String, Object> resData = medicineSalesBO.getSaleTaxDetails(saleItemId, reqQty);
		JSONSerializer js = new JSONSerializer();
		response.setContentType("text/plain");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.getWriter().write(js.deepSerialize(resData));
		response.flushBuffer();
		return null;
	}
}
