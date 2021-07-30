package com.insta.hms.core.billing;

import com.bob.hms.common.DateUtil;
import com.insta.hms.billing.Bill;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.preferences.ippreferences.IpPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.clinical.adt.IpBedDetailsService;
import com.insta.hms.core.clinical.discharge.DischargeService;
import com.insta.hms.core.clinical.order.beditems.BedOrderItemService;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.insurance.InsuranceClaimRepository;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlanDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientInsurancePolicyDetailsService;
import com.insta.hms.core.patient.registration.RegistrationPreferencesService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.exception.HMSException;
import com.insta.hms.exception.ResourceLockedException;
import com.insta.hms.extension.dynapackage.DynaPackageProcessorService;
import com.insta.hms.extension.insurance.drg.DrgCalculatorService;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.integration.paymentgateway.GenericPaymentsAggregator;
import com.insta.hms.integration.paymentgateway.PaymentGatewayAggregatorFactory;
import com.insta.hms.integration.paymentgateway.PaymentTransactionService;
import com.insta.hms.integration.paymentgateway.TransactionRequirements;
import com.insta.hms.mdm.bedtypes.BedTypeService;
import com.insta.hms.mdm.billprinttemplates.BillPrintTemplateService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.edcmachines.EdcMachinesService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.patientcategories.PatientCategoryService;
import com.insta.hms.mdm.printerdefinition.PrinterDefinitionService;
import com.insta.hms.mdm.registrationcharges.RegistrationChargesService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
public class BillService {
	
	private static Logger log = LoggerFactory.getLogger(BillService.class);
	
	/*
	 * Constants
	 */
	public static final String BILL_TYPE_CREDIT	 = "C";
	public static final String BILL_TYPE_PREPAID = "P";

	public static final String BILL_VISIT_TYPE_IP = "i";
	public static final String BILL_VISIT_TYPE_OP = "o";
	public static final String BILL_VISIT_TYPE_RETAIL = "r";
	public static final String BILL_VISIT_TYPE_INCOMING = "t";

	public static final String BILL_RESTRICTION_HOSPITAL = "N";
	public static final String BILL_RESTRICTION_NONE = "N";
	public static final String BILL_RESTRICTION_PHARMACY = "P";
	public static final String BILL_RESTRICTION_TEST = "T";

	public static final String BILL_STATUS_OPEN = "A";
	public static final String BILL_STATUS_FINALIZED = "F";
	public static final String BILL_STATUS_CLOSED = "C";
	public static final String BILL_STATUS_CANCELLED = "X";

	public static final String BILL_DISCHARGE_OK = "Y";
	public static final String BILL_DISCHARGE_NOTOK = "N";

	public static final String BILL_APP_MODIFIED = "Y";
	public static final String BILL_APP_NOT_MODIFIED = "N";

	public static final String BILL_PAYMENT_UNPAID = "U";
	public static final String BILL_PAYMENT_PAID = "P";

	public static final String BILL_CLAIM_OPEN = "O";
	public static final String BILL_CLAIM_SENT = "S";
	public static final String BILL_CLAIM_RECEIVED = "R";

	public static final int BILL_DEFAULT_ACCOUNT_GROUP = 1;
	

	@LazyAutowired
	private BillRepository billRepository;
	
	@LazyAutowired
	private GenericPreferencesService genPrefService;
	
	@LazyAutowired
	private PatientInsurancePlansService patInsPlansService;
	
	@LazyAutowired
	private SessionService sessionService;
	
	@LazyAutowired
	private RegistrationService regService;
	
	@LazyAutowired
	private BillChargeService billChargeService;
	
	@LazyAutowired
	private BillChargeRepository billChargeRepo;
	
	@LazyAutowired
	private BillChargeClaimService billChargeClaimService;
	
	@LazyAutowired
	private OrderService orderService;
	
	@LazyAutowired
	private BillActivityChargeService billActivityChargeService;
	
	@LazyAutowired
	private DiscountService discountPlanService;
	
	@LazyAutowired
	private PatientCategoryService patientCategoryService;
	
	@LazyAutowired
	private BillClaimService billClaimService;
	
	/** The bill_charge_claim repository. */
	@LazyAutowired 
	BillChargeClaimRepository bccRepository;

	@LazyAutowired
	InsuranceClaimRepository icRepository;
		 
    @LazyAutowired
    private ChargeHeadsService chargeHeadsService;
    
    @LazyAutowired
    private RegistrationChargesService registrationChargesService;
    
    @LazyAutowired
    private BillAdjustmentsAlertRepository billAdjustmentAlertRepo;
    
    @LazyAutowired
    private RegistrationPreferencesService regPrefService;
    
    @LazyAutowired
    private SponsorService sponsorService;
    
    @LazyAutowired
	private InsurancePlanService insurancePlanService;
	
	@LazyAutowired
	private InsurancePlanDetailsService insurancePlanDetailsService;
	
	@LazyAutowired
	private PatientInsurancePlansService patientInsurancePlansService;
	
	@LazyAutowired
	private PatientInsurancePlanDetailsService patientInsurancePlanDetailsService;
	
	@LazyAutowired
	private PatientInsurancePolicyDetailsService patientInsurancePolicyDetailsService;
	
	@LazyAutowired
	private BillPrintTemplateService billPrintTemplateService;
	
	@LazyAutowired
	private CenterPreferencesService centerPrefService;
	
	@LazyAutowired
	private DynaPackageProcessorService dynaPkgProcessorService;
	
	@LazyAutowired
	private IpBedDetailsService ipBedDetailsService;
	
	@LazyAutowired
	private BedTypeService bedTypeService;
	
	@LazyAutowired
	private DiscountService discountService;
	
	@LazyAutowired
	private BedOrderItemService bedItemService;
	
	@LazyAutowired
	private IpPreferencesService ipPrefService;
	
	@LazyAutowired
	private BedChargeCalculationHelper bedChargeCalculationHelper;

	@LazyAutowired
	private EdcMachinesService edcMachineService;
	
	@LazyAutowired
	private InstaIntegrationService instaIntegrationService;
	
	@LazyAutowired
	private PaymentTransactionService paymentTransactionService;
	
	@LazyAutowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@LazyAutowired
	private PaymentGatewayAggregatorFactory paymentGatewayAggregatorFactory;
	
	@LazyAutowired
	private TpaService tpaService;
	 
	@LazyAutowired
	private PatientDetailsService patientDetailsService;
		
	@LazyAutowired
	private HospitalCenterService hospCenterService;
	
	@LazyAutowired
	private DischargeService dischargeService;
	
	/** The printer definition service. */
	@LazyAutowired
	private PrinterDefinitionService printerDefinitionService;
	
	/** The drg calculator service. */
	@LazyAutowired
	private DrgCalculatorService drgCalculatorService;
	
	/** The Receipt service. */
	@LazyAutowired
	private ReceiptService receiptService;
       
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<BasicDynaBean> listAll(Map keys) {	
		return billRepository.listAll(null, keys, null);
	}
	
	public BasicDynaBean getBillAndPaymentStatus(String visitId, String visitType) {
		return billRepository.getBillAndPaymentStatus(visitId, visitType);
	}

	public BasicDynaBean getFirstBillForVisit(String visitId) {
		return billRepository.getFirstBillForVisit(visitId);
	}

	public BasicDynaBean getBean(){
		return billRepository.getBean();
	}
	
	public List<BasicDynaBean> listByActivityCodeAndId(String activityCode, String activityId) {
		return billActivityChargeService.listByActivityCodeAndId(activityCode, activityId);
	}

	public Integer deleteBillActivityCharge(String key, String identifier) {
		return billActivityChargeService.delete(key, identifier);
	}

	public Integer updateBillChargeActivity(String chargeId, String status) {
		return billChargeService.updateActivity(chargeId, status);
	}

	public Integer insertBillChargeActivity(String chargeId, String activityId, String chargeHead) {
		return billChargeService.insertActivity(chargeId, activityId, chargeHead);
	}
	
	public BasicDynaBean getBillFromChargeId(String chargeId) {
		return billChargeService.getBillFromChargeId(chargeId);
	}
	
	public boolean isBillInsuranceAllowed(Object billType, boolean is_tpa) {
		if (billType != null && !billType.equals("") && is_tpa) {
			if (billType.equals(BILL_TYPE_PREPAID)) {			
				boolean billNowTpaAllowed = genPrefService.getPreferences().get("allow_bill_now_insurance").equals("Y");
				return billNowTpaAllowed;
			}else if (billType.equals(BILL_TYPE_CREDIT) && is_tpa) {
				return true;
			}
		}
		return false;
	}

	public boolean isBillLocked(String billNo) {
	  if (billNo != null && !billNo.equals("")) {
	    BasicDynaBean bill = findByKey("bill_no", billNo);
	    if (null != bill && Boolean.TRUE.equals(bill.get("locked"))) {
	      return true;
	    }
	  }
	  return false;
	}

	public BasicDynaBean createBill(Map<String, Object> patientDetailsParams,
			Map<String, Object> visitDetailsParams, String visitId,
			boolean is_tpa, List<BasicDynaBean> plansList, BasicDynaBean visitDetailsBean,
			String billType, String registrationChargesApplicable, BigDecimal estimatedAmt,
			String regAndBill, boolean noGenRegCharge) {

		// Bug # 22408: Depending on bill now tpa allowed pref., need to make bill now bill insured.
		boolean allowBillInsurance = isBillInsuranceAllowed(billType, is_tpa);
		BasicDynaBean bill = getBean();
		//HMS-24420
		//if (checkToCreatePrepaidBill(regAndBill, is_tpa, estimatedAmt, billType)) {

		BigDecimal billDeduction = (getValue("billDeduction", visitDetailsParams) != null &&
				!getValue("billDeduction", visitDetailsParams).equals("")) ? new BigDecimal("") :
				BigDecimal.ZERO;

		Timestamp regDateTime = Timestamp.valueOf(visitDetailsBean.get("reg_date").toString()
				+ " " + visitDetailsBean.get("reg_time").toString());
		generateBill(visitId, (String) visitDetailsBean.get("visit_type"),
				(String) visitDetailsBean.get("user_name"),
				bill, billType, allowBillInsurance, billDeduction,
				(String) visitDetailsBean.get("org_id"),
				(String) getValue("insurance_discount_plan", visitDetailsParams),
				regDateTime);
		/* Checking registration validity is expired or not. */
		boolean isRenewal = regService.isRegValidityExpired(visitDetailsBean,
				(String) getValue("mr_no", patientDetailsParams));
		String userMrNo = (String) getValue("user_provided_mr_no", patientDetailsParams);
		if (null != userMrNo && !userMrNo.isEmpty()) {
			visitDetailsBean.set("reg_charge_accepted", "N");
		}

		insertRegistrationCharges(bill, visitDetailsBean, plansList, isRenewal,
				registrationChargesApplicable, allowBillInsurance, noGenRegCharge);

		//}
		return bill;
	}

	public void generateBill(String visitId, String patientType,
			String userName, BasicDynaBean bill, String billType,
			boolean allowBillInsurance, BigDecimal billDeduction, String orgId,
			String discountPlanId) {

		generateBill(visitId, patientType,
				userName, bill, billType,
				allowBillInsurance, billDeduction, orgId, discountPlanId, null);

	}

	public void generateBill(String visitId, String patientType,
			String userName, BasicDynaBean bill, String billType,
			boolean allowBillInsurance, BigDecimal billDeduction, String orgId,
			String discountPlanId,
			Timestamp regDateTime) {

		setBean(visitId, patientType, userName, bill, billType, allowBillInsurance, billDeduction,
				orgId, discountPlanId, regDateTime);

		insert(bill);

		if (bill.get("bill_type").equals("C") && bill.get("restriction_type").equals("N")) {
			billRepository.setPrimaryBillConditional((String) bill.get("bill_no"));
		}

	}

	private void setBean(String visitId, String patientType,
			String userName, BasicDynaBean bill, String billType,
			boolean allowBillInsurance, BigDecimal billDeduction,
			String orgId, String discountPlanId, Timestamp regDateTime) {

		java.util.Date parsedDate = new java.util.Date();
		java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());

		if (billType == null || billType.equals("")) {
			return;
		}

		if (regDateTime != null) {
			if (regDateTime.compareTo(datetime) > 0) {
				datetime = regDateTime;
			}
		}

		bill.set("visit_id",visitId);
		bill.set("visit_type",patientType);
		bill.set("opened_by",userName);
		bill.set("username",userName);

		bill.set("open_date", new Timestamp(datetime.getTime()));
		bill.set("mod_time", new Timestamp(datetime.getTime()));
		bill.set("discharge_status",BILL_DISCHARGE_NOTOK);
		bill.set("deposit_set_off",BigDecimal.ZERO);
		bill.set("insurance_deduction",billDeduction);
		bill.set("bill_label_id",-1);
		
		if(bill.get("account_group") == null){
		  bill.set("account_group", 1);
		}

		bill.set("is_tpa",allowBillInsurance);
		bill.set("bill_rate_plan_id",orgId);
		bill.set("discount_category_id",discountPlanId != null && !discountPlanId.isEmpty() ? Integer.parseInt(discountPlanId) : 0);

    	if (billType.equals(BILL_TYPE_PREPAID)) {
    		bill.set("bill_type", BILL_TYPE_PREPAID);
			bill.set("is_primary_bill","N");
			bill.set("status", BILL_STATUS_OPEN);

    	} else if (billType.equals(BILL_TYPE_CREDIT)) {
    		bill.set("bill_type", BILL_TYPE_CREDIT);
			bill.set("is_primary_bill","Y");
    		bill.set("status", BILL_STATUS_OPEN);
    	}
    	
		Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
		Integer centerId = (Integer)sessionAttributes.get("centerId");
		
		allowBillInsurance = isBillInsuranceAllowed(billType, (Boolean)bill.get("is_tpa"));

		String restrictionType = BILL_RESTRICTION_NONE;
			
		Object[] obj = new Object[]{billType, patientType, restrictionType,
					centerId,(allowBillInsurance == true? "t":"f"),"f"};
				
		String billNo = billRepository.getNextPatternId(obj);
		
		if(billNo.isEmpty()){
			throw new HMSException(HttpStatus.BAD_REQUEST,"exception.bill.sequence.not.configured",null);
		}
		
		bill.set("bill_no",billNo);

		if (bill.get("status") == null) {
			bill.set("status",BILL_STATUS_OPEN);
		}

		bill.set("discharge_status",BILL_DISCHARGE_NOTOK);		
		bill.set("is_tpa",allowBillInsurance);		
		bill.set("restriction_type", restrictionType);

		if ((Boolean)bill.get("is_tpa"))
			bill.set("primary_claim_status",BILL_CLAIM_OPEN);

		bill.set("bill_rate_plan_id",orgId);
		
		BasicDynaBean planDetails = patInsPlansService.getVisitPrimaryPlan(visitId);
		
		if ( (Boolean)bill.get("is_tpa") && planDetails != null ){
			bill.set("discount_category_id",planDetails.get("discount_plan_id") != null ? (Integer)planDetails.get("discount_plan_id") : 0) ;
		}
		
	}
	
	private void insert(BasicDynaBean bill) {
		billRepository.insert(bill);		
	}

	@SuppressWarnings({"rawtypes","unchecked"})
	public void insertRegistrationCharges(BasicDynaBean bill,BasicDynaBean visitDetailsBean, List<BasicDynaBean> plansList,
			boolean isRenewal, String registrationChargesApplicable,boolean isInsurance, boolean noGenRegCharge){
		
		int[] planIds = plansList.size() > 0 ? new int[plansList.size()] : null;
		String[] preAuthIds = new String[plansList.size()];
		Integer[] preAuthModeIds = new Integer[plansList.size()];
		int planIdIdx = 0;
		for(BasicDynaBean bean : plansList){
				planIds[planIdIdx] = (Integer) bean.get("plan_id");
				preAuthIds[planIdIdx] = (String) bean.get("prior_auth_id");
				preAuthModeIds[planIdIdx] = (Integer) bean.get("prior_auth_mode_id");
				planIdIdx++;
		}
		List<BasicDynaBean> regCharges = new ArrayList();
		
		String visitType = (String)bill.get("visit_type");
		String visitId = (String)visitDetailsBean.get("patient_id");

		// BUG : 20027 - Post Reg charges if tpa selected and registration
		// charge applicable = 'Y'
		//Checks against patient category selected
		String regChargeApplicable = checkIfRegistrationChargeApplicable(visitDetailsBean);

		//override value from request if user un-check's flag
		if(registrationChargesApplicable.equals("N"))
			regChargeApplicable = "N";

		if (regChargeApplicable != null && regChargeApplicable.equals("Y")) {
			createCharges(visitDetailsBean, isRenewal, isInsurance, planIds,
					regCharges, visitType, visitId, noGenRegCharge);
		}
		
		createMlcCharge(visitDetailsBean, isInsurance, planIds, regCharges,
				visitType, visitId);

		// set the bill no, next charge id and username in all the charges
		//set other defaults
		ArrayList allCharges = setOtherDefaults(bill, preAuthIds,
				preAuthModeIds, regCharges, visitType);
		
		billChargeService.batchInsert(allCharges);

		insertBillActivity(regCharges, visitId);

		if((Boolean)bill.get("is_tpa")){
			billChargeClaimService.insertBillChargeClaims(allCharges,planIds,
					visitId,bill,preAuthIds,preAuthModeIds);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String checkIfRegistrationChargeApplicable(
			BasicDynaBean visitDetailsBean) {
		String regChargeApplicable = "Y";

		if (visitDetailsBean.get("patient_category_id") != null
				&& !(visitDetailsBean.get("patient_category_id").toString()).equals("")) {
				Map key = new HashMap();
				key.put("category_id", visitDetailsBean.get("patient_category_id"));
			BasicDynaBean bean = patientCategoryService.findByPk(key,true);
			if (bean != null
					&& bean.get("registration_charge_applicable") != null) 
				regChargeApplicable = (String) bean.get("registration_charge_applicable");
		}
		return regChargeApplicable;
	}

	private void createMlcCharge(BasicDynaBean visitDetailsBean,
			boolean isInsurance, int[] planIds, List<BasicDynaBean> regCharges,
			String visitType, String visitId){
		if ("Y".equals(visitDetailsBean.get("mlc_status"))) {
			List<BasicDynaBean> mlcCharges = addCharges(
					(String) visitDetailsBean.get("bed_type"),
					(String) visitDetailsBean.get("org_id"), "MLREG", false,
					isInsurance, planIds, true, visitType,
					visitId == null ? null
							: visitId,	null);
			regCharges.addAll(mlcCharges);
		}
	}

	private void insertBillActivity(List<BasicDynaBean> regCharges,
			String visitId) {
		for (BasicDynaBean charge : regCharges) {
			if (charge.get("charge_head").equals("MLREG")) {
				BasicDynaBean activityBean = billActivityChargeService.getBean();
				activityBean.set("charge_id", charge.get("charge_id"));
				activityBean.set("activity_id", visitId);
				activityBean.set("activity_code", "MLREG");					
				billActivityChargeService.insert(activityBean);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ArrayList setOtherDefaults(BasicDynaBean bill, String[] preAuthIds,
			Integer[] preAuthModeIds, List<BasicDynaBean> regCharges,
			String visitType){
		ArrayList allCharges = new ArrayList();
		for (BasicDynaBean chargeBean : regCharges) {
			chargeBean.set("bill_no",(String) bill.get("bill_no"));
			chargeBean.set("charge_id",billChargeService.getNextPrefixedId());//TODO Spring migration needed
			chargeBean.set("username",(String)bill.get("username"));
			
			discountPlanService.applyDiscountRule(chargeBean,(Integer) bill.get("discount_category_id"),visitType);	
			
			chargeBean.set("prior_auth_id",(preAuthIds != null && preAuthIds.length>0) ? preAuthIds[0]:"");
			chargeBean.set("prior_auth_mode_id",(preAuthModeIds != null && preAuthModeIds.length>0) ? preAuthModeIds[0]:null);
			chargeBean.set("amount_included", chargeBean.get("amount_included")== null ? BigDecimal.ZERO : chargeBean.get("amount_included"));
			chargeBean.set("qty_included", chargeBean.get("qty_included")== null ? BigDecimal.ZERO : chargeBean.get("qty_included"));
			chargeBean.set("orig_insurance_claim_amount", BigDecimal.ZERO);
			chargeBean.set("is_claim_locked", chargeBean.get("is_claim_locked") == null ? false : chargeBean.get("is_claim_locked"));
			chargeBean.set("include_in_claim_calc", chargeBean.get("include_in_claim_calc") == null ? true : chargeBean.get("include_in_claim_calc"));
			chargeBean.set("first_of_category", false);
			allCharges.add(chargeBean);
		}
		return allCharges;
	}

	private void createCharges(BasicDynaBean visitDetailsBean,
			boolean isRenewal, boolean isInsurance, int[] planIds,
			List<BasicDynaBean> regCharges, String visitType, String visitId, boolean noGenRegCharge){
		/*
		 * General Registration charge or renewal charge. (validity is
		 * checked before setting reg_charge_accepted)
		 */
		if ("Y".equals(visitDetailsBean.get("reg_charge_accepted")) && !noGenRegCharge) {

			List<BasicDynaBean> genRegCharges = addCharges(
					(String) visitDetailsBean.get("bed_type"),
					(String) visitDetailsBean.get("org_id"), "GREG",
					isRenewal, isInsurance, planIds, true, visitType, visitId == null ? null
							: visitId , null);

			if (isRenewal) {
				for (BasicDynaBean ch : genRegCharges) {
					ch.set("act_description","Registration Renewal charge");
				}
			}

			regCharges.addAll(genRegCharges);
		}

		/*
		 * Ip or Op visit charge.
		 */
		if ("o".equals(visitType)) {
			List<BasicDynaBean> opRegCharges = addCharges(
					(String) visitDetailsBean.get("bed_type"),
					(String) visitDetailsBean.get("org_id"), "OPREG",
					false, isInsurance, planIds, true,visitType,
					visitId == null ? null
							: visitId, null);
			regCharges.addAll(opRegCharges);
		}

		if ("i".equals(visitType)) {
			List<BasicDynaBean> ipRegCharges = addCharges(
					(String) visitDetailsBean.get("bed_type"),
					(String) visitDetailsBean.get("org_id"), "IPREG",
					false, isInsurance, planIds, true,visitType,
					visitId == null ? null
							: visitId, null);
			regCharges.addAll(ipRegCharges);
			
			addMrdCharges(visitDetailsBean, isInsurance, planIds, regCharges, visitType, visitId);
		}
	}

  private void addMrdCharges(BasicDynaBean visitDetailsBean, boolean isInsurance, int[] planIds,
      List<BasicDynaBean> regCharges, String visitType, String visitId) {
    List<BasicDynaBean> opRegCharges = addCharges(
        (String) visitDetailsBean.get("bed_type"),
        (String) visitDetailsBean.get("org_id"), "EMREG",
        false, isInsurance, planIds, true,visitType,
        visitId == null ? null
            : visitId, null);
      regCharges.addAll(opRegCharges);    
  }

	@SuppressWarnings("rawtypes")
	private Object getValue(String key, Map params, boolean sendNull) {
		Object obj = params.get(key);
		if(sendNull && obj == null)
			return null;
		else if (obj != null) {
			return obj;
		}
		return "";
	}
	
	@SuppressWarnings("rawtypes")
	private Object getValue(String key, Map params) {
		return getValue(key, params, false);
	}
		
	public boolean checkToCreatePrepaidBill(String regAndBill, boolean isTpa,
			BigDecimal estimatedAmt, String billType) {

		if (!isTpa && (regAndBill != null && regAndBill.equals("Y"))
				&& billType.equals(BILL_TYPE_PREPAID)
				&& estimatedAmt.compareTo(BigDecimal.ZERO) == 0)
			return false;
		return true;
	}

	public BasicDynaBean findByKey(String billNo) {	
		return billRepository.findByKey("bill_no", billNo);
	}

	public BasicDynaBean getDetails(String billNo) {		
		Map keys = new HashMap();
		keys.put("bill_no", billNo);
		List<BasicDynaBean> bills = billRepository.listAll(null, keys, null);
		if (bills == null) {
			return null;
		}
		return bills.get(0);
	}


	public BasicDynaBean getLatestOpenBillLaterElseBillNow(String visitId) {
		return billRepository.getLatestOpenBillLaterElseBillNow(visitId);
	}
	
	public BasicDynaBean getPrimaryOpenBillLaterElseBillNow(String visitId) {
		return billRepository.getPrimaryOpenBillLaterElseBillNow(visitId);
	}

	public BasicDynaBean findByKey(String key, String value) {
		return billRepository.findByKey(key	, value);

	}
	
	/**
	 * Update remittance charges.
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateRemittanceCharges(Integer remittanceId) {	
		bccRepository.updateCharges(remittanceId);
	}
	
	public void updateRemitRecoveryCharges(Integer remittanceId) {
		
		bccRepository.updateRecoveryCharges(remittanceId);
	}
	
	public void updateAggRemitCharges(Integer remittanceId) {
		
		bccRepository.updateAggRemitCharges(remittanceId);
	}
	
	/**
	 * update status of bill_charge_claim items based on insurance_recd_amounts
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateRemittanceStatusBillCharge(Integer remittanceId) {
		bccRepository.updateStatus(remittanceId);
	}
	
	public void updateRemittanceStatusForBills(Integer remittanceId) {
		billRepository.updateRemarksOnAutoCloseClaim(remittanceId);
		billRepository.updateSponsorWriteOff(remittanceId);
		billRepository.updateBillCloseStatus(remittanceId);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<BasicDynaBean> addCharges (String bedType, String orgId, String chargeHead,
			boolean isRenewal, boolean isInsurance, int[] planIds, boolean excludeZero, String visitType,
			String patientId, Boolean firstOfCategory){
		BigDecimal charge = null, discount = null;
		Map keyMap = new HashMap<>();
		keyMap.put("chargehead_id", chargeHead);
		BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(keyMap);

		Map<String,BigDecimal> map = new HashMap<>();
		map = getChargeandDiscount(chargeHead,isRenewal,orgId,bedType,visitType);
		charge = map.get("charge");
		discount = map.get("discount");
		List l = new ArrayList();
		BasicDynaBean chargeBean = billChargeService.getBean();

		addCharges("REG", chargeHead,
				(BigDecimal) charge, BigDecimal.ONE, (BigDecimal) discount, "",
				chargeHead, "", null, isInsurance,
				(Integer) chargeHeadBean.get("service_sub_group_id"),
				(Integer)chargeHeadBean.get("insurance_category_id"), firstOfCategory,chargeBean);
		chargeBean.set("prior_auth_mode_id",1);
		chargeBean.set("allow_rate_increase",(Boolean)chargeHeadBean.get("allow_rate_increase"));
		chargeBean.set("allow_rate_decrease",(Boolean)chargeHeadBean.get("allow_rate_decrease"));

		
		if ((charge.compareTo(BigDecimal.ZERO) != 0) || !excludeZero) {
			l.add(chargeBean);
		}
		return l;
	}
	
    public void addCharges(String group, String chargeHead,
			BigDecimal rate, BigDecimal qty, BigDecimal discount,
			String units, String descId, String desc, String deptId,
			boolean isInsurance, Integer serviceSubGroupId, int insuranceCategoryId,
			Boolean firstOfCategory, BasicDynaBean chargeBean) {
		
		chargeBean.set("charge_group", group);
		chargeBean.set("charge_head", chargeHead);
		chargeBean.set("act_rate", rate);
		chargeBean.set("orig_rate",rate);
		chargeBean.set("act_quantity", qty);
		chargeBean.set("act_unit", units);
		chargeBean.set("discount",discount);
		chargeBean.set("act_description_id", descId);
		chargeBean.set("act_description", desc);
		chargeBean.set("act_department_id", deptId);
		chargeBean.set("service_sub_group_id", serviceSubGroupId);
		chargeBean.set("insurance_category_id", insuranceCategoryId);
		//defaults
		chargeBean.set("status","A" );
		chargeBean.set("is_system_discount","Y" );
		
		if (discount.compareTo(BigDecimal.ZERO) != 0) {
			chargeBean.set("overall_discount_auth", -1);
			chargeBean.set("overall_discount_amt", discount);
		} else  {
			chargeBean.set("overall_discount_auth", null);
			chargeBean.set("overall_discount_amt", null);
		}
		chargeBean.set("hasactivity", false);
		
		Timestamp postedDate = DateUtil.getCurrentTimestamp();
		
		chargeBean.set("mod_time", postedDate);
		chargeBean.set("posted_date", postedDate);
		
		// calculated fields
		chargeBean.set("amount", (rate.multiply(qty)).subtract(discount));
		if (isInsurance) {
			chargeBean.set("first_of_category",firstOfCategory);
			chargeBean.set("insurance_claim_amount",chargeBean.get("amount"));
		} else {
			chargeBean.set("insurance_claim_amount",BigDecimal.ZERO);
		}
		
	}

	public  Map<String,BigDecimal> getChargeandDiscount(String chargeHead,boolean isRenewal,
			String orgId, String bedType,String visitType){

		BigDecimal charge = null, discount = null;
		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("bed_type", bedType);
		filterMap.put("org_id", orgId);
		BasicDynaBean regChargesBean = registrationChargesService.getRegistrationCharges(bedType, orgId);
		if (chargeHead.equals("GREG")) {
			if (isRenewal) {
				charge = (BigDecimal) regChargesBean.get("reg_renewal_charge");
				discount = (BigDecimal) regChargesBean.get("reg_renewal_charge_discount");
			} else {
				charge = (BigDecimal) regChargesBean.get("gen_reg_charge");
				discount = (BigDecimal) regChargesBean.get("gen_reg_charge_discount");
			}
		} else if (chargeHead.equals("IPREG")) {
			charge = (BigDecimal) regChargesBean.get("ip_reg_charge");
			discount = (BigDecimal) regChargesBean.get("ip_reg_charge_discount");
		} else if (chargeHead.equals("OPREG")) {
			charge = (BigDecimal) regChargesBean.get("op_reg_charge");
			discount = (BigDecimal) regChargesBean.get("op_reg_charge_discount");
		} else if (chargeHead.equals("MLREG")) {
			if (visitType.equalsIgnoreCase("i")) {
				charge = (BigDecimal) regChargesBean.get("ip_mlccharge");
				discount = (BigDecimal) regChargesBean.get("ip_mlccharge_discount");
			} else {
				charge = (BigDecimal) regChargesBean.get("op_mlccharge");
				discount = (BigDecimal) regChargesBean.get("op_mlccharge_discount");
			}
		} else if (chargeHead.equals("EMREG")) {
			charge = (BigDecimal) regChargesBean.get("mrcharge");
			discount = (BigDecimal) regChargesBean.get("mrcharge_discount");
		} else {
			log.error("Invalid registration charge head: " + chargeHead);
		}

		Map<String,BigDecimal> map = new HashMap<>();
		map.put("charge", charge);
		map.put("discount", discount);
		return map;
	}


	public List<BasicDynaBean> getVisitBillCharges(String visitId, Boolean includeFollowUpVisits, String followUpVisitIds) {
		return billRepository.getVisitBillCharges(visitId, includeFollowUpVisits, followUpVisitIds);
	}

	public List<BasicDynaBean> getVisitBillChargeClaims(String visitId, int planId, Boolean includeFollowUpVisits, String followUpVisitIds) {		
		return billRepository.getVisitBillChargeClaims(visitId, planId, includeFollowUpVisits, followUpVisitIds);
	}

	public void insertBillAdjustmentAlerts(Map<Integer, Map<Integer, Integer>> adjMap, String visitId) {
		billAdjustmentAlertRepo.delete( "visit_id", visitId);
		BasicDynaBean billAdjAlertBean = billAdjustmentAlertRepo.getBean();
		for(int planId : adjMap.keySet()){
			Map<Integer,Integer> catAdjMap = adjMap.get(planId);
			for(int categoryId : catAdjMap.keySet()){
				Integer adjStatus = catAdjMap.get(categoryId);
				billAdjAlertBean.set("visit_id", visitId);
				billAdjAlertBean.set("plan_id", planId);
				billAdjAlertBean.set("category_id", categoryId);
				billAdjAlertBean.set("adjstment_status", adjStatus);

				billAdjustmentAlertRepo.insert(billAdjAlertBean);
			}
		}		
	}

	public void updatePaymentAndBillStatus(String regAndBill, BasicDynaBean bill,
				boolean doc_eandm_codification_required,BigDecimal estimatedAmt) {
	
			if (null != bill && !bill.getMap().isEmpty() && bill.get("bill_type").equals(BILL_TYPE_PREPAID)) {
				bill.set("claim_recd_amount",BigDecimal.ZERO);
				bill.set("points_redeemed_amt",BigDecimal.ZERO);
	
				Timestamp now = DateUtil.getCurrentTimestamp();
				// Close the bill if register and pay.
				if (regAndBill.equals("Y")) {
					// register and pay: close the bill if non-insurance, keep open if insurance
					bill.set("payment_status",BILL_PAYMENT_PAID);
					if ((Boolean) bill.get("is_tpa")) {
						// For a insured pre-paid bill and registered using Register and Pay
						// when doc_eandm_codification_required then bill is open.
						// otherwise bill is finalized.
						if (doc_eandm_codification_required) {
							bill.set("discharge_status",BILL_DISCHARGE_NOTOK);
							bill.set("status",BILL_STATUS_OPEN);
						} else {
							bill.set("status",BILL_STATUS_FINALIZED);
							bill.set("finalized_date", now);
							bill.set("last_finalized_at", now);
							bill.set("finalized_by",bill.get("username"));
						}
					} else {
				        // Update payment status and close after receipts are generated successfully
						if (estimatedAmt.compareTo(BigDecimal.ZERO) == 0) {
						  bill.set("discharge_status",Bill.BILL_DISCHARGE_OK);
						  bill.set("status",Bill.BILL_STATUS_CLOSED);
						  bill.set("closed_by",bill.get("username"));
						  bill.set("closed_date",now);
						} else {
                          bill.set("payment_status", BILL_PAYMENT_UNPAID);
                          bill.set("discharge_status",Bill.BILL_DISCHARGE_OK);
						}
					}
					bill.set("mod_time",now);
					Map<String,Object> keys = new HashMap<>();
					keys.put("bill_no", (String)bill.get("bill_no"));
					billRepository.update(bill, keys);
				}
		}
	}

	public boolean getOkToDischargeBills(String patientId) {
		List<BasicDynaBean> bean = billRepository.getOkToDischarge(patientId);
		return bean != null && bean.isEmpty();
	}

	public BasicDynaBean getBillAmounts(String billNo) {
			return billRepository.getBillAmounts(billNo);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean update(BasicDynaBean billBean, Map keys) {
		return billRepository.update(billBean, keys)>0;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map estimateAmount(Map<String, Object> params)throws SQLException{

		Map map = new HashMap();
		ArrayList orderItems = (ArrayList) params.get("ordered_items");
		ArrayList insuranceList = (ArrayList) params.get("insurance_details"); 
		Map<String, Object> visitParams = (Map<String, Object>) params.get("visit");
		Map<String, BasicDynaBean> details = new HashMap<>();
		
		String visitId = (String)getValue("visit_id",visitParams);
		String regScreen = (String)getValue("reg_screen",visitParams);

		ArrayList<Map<String, Object>> newCharges = new ArrayList<Map<String,Object>>();

		if(null != regScreen  && regScreen.equals("Y")){
			Map<String, Object> docChargeMap = new HashMap<>();
			setDoctorChargeMap(visitParams,docChargeMap, newCharges, insuranceList);
			BasicDynaBean patientBean = patientDetailsService.getBean();
			BasicDynaBean visitBean = regService.getBean();
			visitBean.set("visit_type", visitParams.get("visit_type"));
			BasicDynaBean billBean = getBean();
			if(insuranceList.size()>0)
				billBean.set("is_tpa", true);
			patientBean.set("nationality_id", (String)getValue("nationality_id",visitParams));
			BasicDynaBean centerBean = hospCenterService.findByKey((Integer)sessionService.getSessionAttributes().get("centerId"));
			details.put("patient",patientBean);
			details.put("center", centerBean);
			details.put("bill", billBean);
			details.put("visit", visitBean);
		}
		
		List<Map<String, Object>> editedCharges = new ArrayList<Map<String, Object>>();
		orderService.addOrderCharges(newCharges, editedCharges, orderItems, insuranceList);
		
		Map<Integer, Map<Integer,Integer>> adjMap = new HashMap<Integer, Map<Integer,Integer>>();
		Map<Integer, List<BasicDynaBean>> billChargeClaimsMap = new HashMap<Integer, List<BasicDynaBean>>();
		Map<Integer, Object> sponsorTaxMap = new HashMap<Integer, Object>();
		if(null != regScreen  && regScreen.equals("Y")){
			List<BasicDynaBean> visitInsDetails = regService.getVisitInsDetails(insuranceList,visitId,visitParams);

			billChargeClaimsMap = sponsorService.getRegScreenOrderItemsSponosorAmount(newCharges, visitInsDetails, adjMap, sponsorTaxMap,details,visitId);
		}else{
			BasicDynaBean patientDetails = regService.getPatientVisitDetailsBean(visitId);
			validate(map, patientDetails);	
			if(!map.isEmpty())
				return map;
			Map<String, Object> additionalInfo = new HashMap<>();
			billChargeClaimsMap = sponsorService.getOrderedItemSponsorAmount(newCharges, editedCharges, visitId, adjMap, sponsorTaxMap, additionalInfo);
			if (!additionalInfo.isEmpty()) {
			  map.put("pre_auth_required", additionalInfo.get("preAuthRequiredMap"));
			  map.put("pre_authorized_amount_limit_reached", additionalInfo.get("preAuthAmountExceeded"));
			}
		}
		
		Map<Integer, Map> billChargesMap = sponsorTaxCalculationSplit(billChargeClaimsMap, sponsorTaxMap);

		map.put("estimate_amount", billChargesMap);

		return map;
	}

	private Map<Integer, Map> sponsorTaxCalculationSplit(
			Map<Integer, List<BasicDynaBean>> billChargeClaimsMap,
			Map<Integer, Object> sponsorTaxMap) {
		Map<String, String> adjTaxMap = new HashMap<>();
		for(Integer key : billChargeClaimsMap.keySet()){
			// process the tax amount
			Map<String,  Object> chargeAndSponsorTaxMap = (Map<String,  Object>)sponsorTaxMap.get(key);
			List<BasicDynaBean> billChgList = billChargeClaimsMap.get(key);
			for(BasicDynaBean billChgBean : billChgList) {
				String chargeId = (String)billChgBean.get("charge_id");
				BigDecimal totSponsorTax = BigDecimal.ZERO;
				BigDecimal sponsorAmount = (BigDecimal)billChgBean.get("insurance_claim_amt");
				totSponsorTax = (BigDecimal)billChgBean.get("tax_amt") == null ? BigDecimal.ZERO : (BigDecimal)billChgBean.get("tax_amt");
				Map<String, Object> sponsorTaxAndSplitMap = (Map<String, Object>)chargeAndSponsorTaxMap.get(chargeId);
				String adjTaxAmt = "N";
				if(sponsorTaxAndSplitMap != null && sponsorTaxAndSplitMap.size() > 0) {
					totSponsorTax = BigDecimal.ZERO;
					sponsorAmount = (sponsorTaxAndSplitMap.get("sponsorAmount") != null) ? 
							(BigDecimal)sponsorTaxAndSplitMap.get("sponsorAmount") : (BigDecimal)billChgBean.get("insurance_claim_amt");
					
					Map<Integer, Object> subGrpCodesTaxMap = (Map<Integer, Object>)sponsorTaxAndSplitMap.get("subGrpSponTaxDetailsMap");	
					for(Map.Entry<Integer, Object> subGrpTaxAmountsMap : subGrpCodesTaxMap.entrySet()) {
						Integer subGrpCodeId = subGrpTaxAmountsMap.getKey();
						Map<String , String> subgrpTaxDetails = (Map<String , String>)subGrpTaxAmountsMap.getValue();
						totSponsorTax = totSponsorTax.add(new BigDecimal((String)subgrpTaxDetails.get("amount"))); 
						if(null != subgrpTaxDetails.get("adjTaxAmt") && subgrpTaxDetails.get("adjTaxAmt").equals("Y")){
							adjTaxAmt = "Y";
						}
					}
				}
				billChgBean.set("insurance_claim_amt", sponsorAmount);
				billChgBean.set("tax_amt", totSponsorTax);
				adjTaxMap.put(chargeId, adjTaxAmt);
			}
		}
		Map<Integer, Map> billChargesMap = new HashMap<Integer, Map>();

		for(Integer key : billChargeClaimsMap.keySet()){
			List<BasicDynaBean> billChgList = billChargeClaimsMap.get(key);

			List listmap= ConversionUtils.listBeanToListMap(billChgList);

			billChargesMap.put(key, ConversionUtils.listMapToMapMap(listmap, "charge_id"));
		}

		billChargesMap.put(-2, adjTaxMap);	
		return billChargesMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validate(Map map, BasicDynaBean patientDetails) {
		if(null == patientDetails ||( null != patientDetails && patientDetails.getMap().isEmpty())){
			map.put("error", "Invalid visit ID");
		}			
	}

	private void setDoctorChargeMap(Map<String, Object> visitParams, Map<String, Object> docChargeMap,
			List<Map<String, Object>> newCharges, ArrayList insuranceList) {

		String chargeHead = (String)getValue("doc_chargehead",visitParams);
		String chargeGroup = (String)getValue("doc_chargegroup",visitParams);
		String itemId = (String)getValue("act_description_id",visitParams);
		Integer insCatId =  0;
		if(getValue("doc_insCategoryId",visitParams) != null && !getValue("doc_insCategoryId",visitParams).equals("")){	
				insCatId = Integer.parseInt((String)getValue("doc_insCategoryId",visitParams));
		}	
		BigDecimal amount = new BigDecimal((String)getValue("doc_amount",visitParams));
		BigDecimal discount = new BigDecimal((String)getValue("doc_discount",visitParams));
		BigDecimal taxAmt = new BigDecimal((String)getValue("doc_tax_amt", visitParams));
		Integer consId = (String)getValue("consultation_type_id",visitParams) != null && 
				!getValue("consultation_type_id",visitParams).equals("")? Integer.parseInt((String)getValue("consultation_type_id",visitParams)):0;
		 
		String isClaimAmtIncludesTax = "N";
		String isLimitIncludesTax = "N";
		BasicDynaBean tpaBean = null;
		if (!insuranceList.isEmpty()) {
			Map<String,Object> keys = Collections.singletonMap("tpa_id",
							( (Map) insuranceList.get(0)).get("sponsor_id"));
			tpaBean = tpaService.findByPk(keys);
		}
        
        if(null != tpaBean && !tpaBean.getMap().isEmpty()){
	        isClaimAmtIncludesTax = (String)tpaBean.get("claim_amount_includes_tax");
	        isLimitIncludesTax = (String)tpaBean.get("limit_includes_tax"); 
        }

		if(null != chargeHead && !chargeHead.equals("") &&
				null != insCatId && null != amount && !amount.equals("")) {
			docChargeMap.put("charge_id", "_0");
			docChargeMap.put("charge_head_id", chargeHead);
			docChargeMap.put("amount", null != amount ? amount : BigDecimal.ZERO);
			
			amount = null != amount ? amount : BigDecimal.ZERO;                      
	        docChargeMap.put("amount", amount);
	           
	        if(isClaimAmtIncludesTax.equals("Y") && isLimitIncludesTax.equals("Y")) {
	        	docChargeMap.put("amount", amount);
	        }else{
	            docChargeMap.put("amount", amount.subtract(taxAmt));
	        }
			
			docChargeMap.put("discount", null != discount ? discount : BigDecimal.ZERO);
			docChargeMap.put("insurance_category_id",insCatId);
			docChargeMap.put("is_insurance_payable", Boolean.TRUE); 
			docChargeMap.put("is_claim_locked", false);
			docChargeMap.put("primclaimAmt", "0");
			docChargeMap.put("secclaimAmt", "0");
			docChargeMap.put("pri_include_in_claim", true);
			docChargeMap.put("sec_include_in_claim", true);
			docChargeMap.put("consultation_type_id", consId);
			docChargeMap.put("act_description_id", itemId);
			docChargeMap.put("charge_group", chargeGroup);
			newCharges.add(docChargeMap);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Boolean isChargeHeadPayable(String chargeHead, String chargeGroup){
		Map filterKeys= new HashMap();
		if ("PKG".equals(chargeGroup)) {
		  chargeHead = "PKGPKG";
    }
		filterKeys.put("chargehead_id", chargeHead);
		BasicDynaBean chargeHeadBean = chargeHeadsService.findByPk(filterKeys);
		Boolean isChargeHeadPayable = true;
		if(null != chargeHeadBean && !chargeHeadBean.getMap().isEmpty()){
			String chargeHeadPayable = (String)chargeHeadBean.get("insurance_payable");
			if(null != chargeHeadPayable && chargeHeadPayable.equals("N"))
				isChargeHeadPayable = false;
		}
		return isChargeHeadPayable;
	}

	public String getBillType(String patientId) {
		return billRepository.getBillType(patientId);
	}

	public Map<String, String> getBillPrintTemplate() {
		Map<String,String> templateMap = new HashMap<>();
		List<BasicDynaBean> printTemplateBeanList = billPrintTemplateService.lookup(false);
		templateMap.put("BILL-DET-ALL","Bill - Detailed");
		templateMap.put("CUSTOM-BUILTIN_HTML","Built-in Default HTML template");
		templateMap.put("CUSTOM-BUILTIN_TEXT","Built-in Default Text template");
		for (BasicDynaBean template : printTemplateBeanList) {
			templateMap.put("CUSTOM-" + template.get("template_name").toString(), template.get("template_name").toString());
		}
		return templateMap;
	}

	public List<BasicDynaBean> listAll(List<String> columns, String filterBy, Object filterValue) {
		return billRepository.listAll(columns, filterBy, filterValue);
	}

	public List<BillOrReceipt> getBillOrReceiptForPatientPayments(MultiValueMap<String, String> params) {
		String visitId = params.getFirst("visit_id");
		String mrNo = params.getFirst("mr_no");
		if (visitId == null && mrNo == null) {
			//TODO: Raise exception
		}
		List<BasicDynaBean> results = billRepository.getBillOrReceiptForPatientPayments(visitId);
		List<BillOrReceipt> list = new ArrayList<BillOrReceipt>();
		if (results == null) {
			return list;
		}
		List<String> addedDocuments = new ArrayList<String>();
		for (BasicDynaBean result : results) {
			Boolean useReceipt = (Boolean) result.get("is_tpa");
			String billNo = (String) result.get("bill_no");
			String receiptNo = (String) result.get("receipt_no");
			if ((useReceipt && addedDocuments.contains(receiptNo)) || (!useReceipt && addedDocuments.contains(billNo))) {
				continue;
			}
			BillOrReceipt item = new BillOrReceipt();
			if (useReceipt) {
				item.setReceipt_no(receiptNo);
				item.setReceipt_payment_type((String) result.get("payment_type"));
				item.setReceipt_type((String) result.get("recpt_type"));
			} else {
				item.setBill_no(billNo);
				item.setBill_type((String) result.get("bill_type"));
				item.setBill_payment_status((String) result.get("payment_status"));
			}
			item.setDocument_type(useReceipt ? "RECEIPT" : "BILL");
			Object amount = result.get(useReceipt ? "amount" : "patient_amount");
			java.sql.Timestamp documentDateTime = (java.sql.Timestamp) result.get(useReceipt ? "display_date" : "open_date");
			item.setAmount(amount != null ? new BigDecimal(amount.toString()) : BigDecimal.ZERO);
			if (documentDateTime != null) {
				item.setGenerated_at(DateUtil.formatTimestamp(documentDateTime));
			}
			list.add(item);
		}
		return list;
	}

  /**
   * Returns the list of bills for active visits
   * 
   * @param visitId
   * @param visitType
   * @param includeMultiVisitPackageBills
   * @param orderDateTime 
   * @param pharmacy2
   * @return
   */
  public List<BasicDynaBean> getUnpaidBillsForVisit(String visitId, String visitType, String pharmacy,
      String includeMultiVisitPackageBills, Date orderDateTime) {
    return billRepository.getUnpaidBillsForVisit(visitId, visitType, pharmacy,
        includeMultiVisitPackageBills, orderDateTime);
  }

	public Map<String,String> resetServiceTaxClaimCharge(String billNo,String userId){

		Map<String,String> errorMap = new HashMap<>();
		List<BasicDynaBean> charges = billChargeService.getChargeDetailsBean(billNo);

		List<String> chargeHeads = new ArrayList<String>();
		chargeHeads.add(BillChargeService.CH_BILL_SERVICE_CHARGE);
		chargeHeads.add(BillChargeService.CH_CLAIM_SERVICE_TAX);

		BasicDynaBean billChExclTotBean = getExcludedChargesBean(billNo,
				chargeHeads);
		BigDecimal billAmt = (BigDecimal)billChExclTotBean.get("bill_total");
		BigDecimal claimAmt = (BigDecimal)billChExclTotBean.get("claim_total");

		// For Bed charges update user will be auto_update as session is null.
		String userid = userId != null ? userId : "auto_update";
		Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();
		BasicDynaBean genPrefs = genPrefService.getAllPreferences();

		// Recalculate bill service charge if exists.
		BasicDynaBean billServiceChBean = billChargeService.getChargeBean(charges, BillChargeService.CH_BILL_SERVICE_CHARGE);
		if (billServiceChBean != null) {
			
			BigDecimal genPrefChargePercent = genPrefs.get("bill_service_charge_percent") != null ?
					(BigDecimal)genPrefs.get("bill_service_charge_percent") : BigDecimal.ZERO;

			BigDecimal chargeableTotalAmt = billChargeService.getChargeApplicableTotal(charges,"service_charge_applicable","amount");		

			BigDecimal newAmount = ConversionUtils.setScale(genPrefChargePercent.multiply(chargeableTotalAmt)
							.divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));
			
			boolean update = newAmount.compareTo((BigDecimal) billServiceChBean.get("amount")) != 0;
			String remarks = "" + genPrefChargePercent + "% on " + chargeableTotalAmt;
			
			calculateServiceCharge((String)billServiceChBean.get("charge_id"),genPrefChargePercent,(BigDecimal)billServiceChBean.get("amount"),
					BillChargeService.CH_BILL_SERVICE_CHARGE,"service_charge_applicable", billServiceChBean,
					userid,currentTimestamp,billAmt,claimAmt,errorMap,billNo, newAmount, chargeableTotalAmt, update,remarks);
			
		}

		// Recalculate claim service tax if exists.
		BasicDynaBean claimTaxChBean =  billChargeService.getChargeBean(charges, BillChargeService.CH_CLAIM_SERVICE_TAX);
		if (claimTaxChBean != null) {

			BigDecimal genPrefChargePercent = genPrefs.get("claim_service_tax") != null ?
					(BigDecimal)genPrefs.get("claim_service_tax") : BigDecimal.ZERO;
			
			BigDecimal chargeableTotalAmt = billChargeService.getChargeApplicableTotal(charges,"claim_service_tax_applicable","insurance_claim_amount");		

			BigDecimal newAmount = ConversionUtils.setScale(genPrefChargePercent.multiply(chargeableTotalAmt)
									.divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));		
					
			boolean update = newAmount.compareTo((BigDecimal) claimTaxChBean.get("amount")) != 0;
			String remarks = "" + genPrefChargePercent + "% on " + chargeableTotalAmt;
			
			calculateServiceCharge((String)claimTaxChBean.get("charge_id"),genPrefChargePercent,(BigDecimal)claimTaxChBean.get("insurance_claim_amount"),
							BillChargeService.CH_CLAIM_SERVICE_TAX,"claim_service_tax_applicable", claimTaxChBean,
							userid,currentTimestamp,billAmt,claimAmt,errorMap,billNo, newAmount, chargeableTotalAmt, update, remarks);		
		}

		return errorMap ;
	}

	private BasicDynaBean getExcludedChargesBean(String billNo,
			List<String> chargeHeads) {
		BasicDynaBean billChExclTotBean = billChargeService.getBillChargeExcludeTotals(billNo, chargeHeads);
		return billChExclTotBean;
	}	
	
	public Map<String,String> resetRoundOff(String billNo , String userId) {
		boolean success = true;
		Map<String,String> err = new HashMap<String,String>();

		List<String> chargeHeads = new ArrayList<String>();
		chargeHeads.add(BillChargeService.CH_ROUND_OFF);

		BasicDynaBean billChExclTotBean = getExcludedChargesBean(billNo,
				chargeHeads);
		BigDecimal billAmt = (BigDecimal)billChExclTotBean.get("bill_total");
		BigDecimal claimAmt = (BigDecimal)billChExclTotBean.get("claim_total");
		BigDecimal billTax = (BigDecimal)billChExclTotBean.get("bill_tax");
		BigDecimal claimTax = (BigDecimal)billChExclTotBean.get("claim_tax");

		BigDecimal roundOff = BigDecimal.ZERO;
		BigDecimal insRoundOff = BigDecimal.ZERO;

		// For Bed charges update user will be auto_update as session is null.
		String userid = userId != null ? userId : "auto_update";
		Timestamp currentTimestamp = DateUtil.getCurrentTimestamp();

		List<BasicDynaBean> charges = billChargeService.getChargeDetailsBean(billNo);

		// Recalculate round-off if exists.
		BasicDynaBean roundOffChBean =  billChargeService.getChargeBean(charges, BillChargeService.CH_ROUND_OFF);
		if (roundOffChBean != null) {
		  String roundOffChargeId = (String)roundOffChBean.get("charge_id");
			roundOff = (BigDecimal)roundOffChBean.get("amount");
			insRoundOff = (BigDecimal)roundOffChBean.get("insurance_claim_amount");
			BigDecimal newRoundOff = ConversionUtils.getRoundOffAmount(billAmt.add(billTax));
			BigDecimal newInsRoundOff = ConversionUtils.getRoundOffAmount(claimAmt.add(claimTax));
			boolean update = newRoundOff.compareTo(roundOff) != 0 || newInsRoundOff.compareTo(insRoundOff) != 0;
			
			calculateServiceCharge((String)roundOffChBean.get("charge_id"),null,(BigDecimal)roundOffChBean.get("insurance_claim_amount"),
					BillChargeService.CH_ROUND_OFF,"claim_service_tax_applicable", roundOffChBean,
					userid,currentTimestamp,billAmt,claimAmt,err,billNo, newRoundOff, newInsRoundOff, update, "");
		}
		if (billAmt != null) {
			BasicDynaBean billbean = getBean();
			billbean.set("total_amount", billAmt);
			billbean.set("total_claim", claimAmt);
			billbean.set("username", userid);
			billbean.set("mod_time", currentTimestamp);
			billbean.set("bill_no", billNo);
			Map<String,Object> key = new HashMap<String,Object>();
			key.put("bill_no", billNo);
			int result = billRepository.update(billbean, key);
			success = success && (result > 0);

			if (!success){
				err.put("error" ,"Error while updating bill totals for bill no: "+billNo);
			}
		}
		return err;
	}
	

	public Map<String, Object> createBill(boolean isInsurance, String visitId, String billType) {
		Map<String, Object> map = new HashMap<>();
		BasicDynaBean bill = getBean();
		String userName = (String) sessionService.getSessionAttributes().get("userId");
		BasicDynaBean patientDetails = regService.getPatientVisitDetailsBean(visitId);
		if (null == patientDetails || patientDetails.getMap().isEmpty()) {
			throw new EntityNotFoundException(new String[] { "Patient", "Visit Id", visitId });
		} else if (patientDetails.get("visit_status").equals("I")) {
			throw new HMSException("exception.cannot.create.new.bill.for.inactive.visit");
		}

		Integer centerId = (Integer) patientDetails.get("center_id");
		String prefRatePlan = centerPrefService.getRatePlanForNonInsuredBills(centerId);
		String orgId = (String) patientDetails.get("org_id");
		boolean isTpa = false;
		String patientType = (String) patientDetails.get("visit_type");
		if (isInsurance) {
			isTpa = true;
			orgId = (String) patientDetails.get("org_id");
		}
		if (patientDetails.get("primary_sponsor_id") != null && !patientDetails.get("primary_sponsor_id").equals("")
				&& !isInsurance) {
			orgId = prefRatePlan != null ? prefRatePlan : (String) patientDetails.get("org_id");
		}

		generateBill(visitId, patientType, userName, bill, billType, isTpa, BigDecimal.ZERO, orgId, null);

		map.put("bill", bill);
		return map;
	}
	
	/* 
	 * Called before actual canceling charges method
	 * This method creates a necessary bean structure for cancelled items
	 * Necessary flags are passed on to canceling charges method based on with/without refund
	 */
	public Map<String,Object> cancel(List<Map<String, Object>> orderList){
		Map<String, Object> map = new HashMap<>();
		for(Map<String,Object> orderItem : orderList){
				
			String cancelled = (String) orderItem.get("cancelled");			
			String edited = (String) orderItem.get("edited");
			
			if(cancelled.equals("R") || cancelled.equals("C") || edited.equals("Y")){
				String activityId = (String) orderItem.get("prescribed_Id");
				boolean cancelCharge = cancelled.equals("R");
				boolean unlinkActivity = cancelled.equals("C");
				boolean docPartOfPack = orderItem.get("type").equals("Package");
				String presDocId = (String) orderItem.get("prescribing_docter_id");
				String remarks = (String) orderItem.get("remarks");
				String activityCode = orderService.getActivityCode((String) orderItem.get("type"));
				String chargeId = billActivityChargeService.getChargeId(activityCode, activityId);
			
				// If cancelling with charges (cancelCharges), check if bill is open
				// It is valid to cancel the activity or update user_remarks
				// after the bill is closed, by doing cancel without refund.
				if (chargeId != null && !chargeId.isEmpty()) {
					String billStatus = billChargeService.getBillStatus(chargeId);
					if ( billStatus != null && !billStatus.equals("A") ) {
						map.put("error", "Bill status is not open: cannot update/cancel charge");
					}
				}

				//cancelChildTestBean TODO 
				cancelCharges(null, cancelCharge, unlinkActivity, docPartOfPack, activityCode, activityId, chargeId);
				
				if(edited.equals("Y")){
					updateCharges(presDocId, chargeId, activityCode, activityId, remarks, orderItem);
				}	
			}
		 }
		return map;
	}
	
	/*
	 * Use this method to Update Charges for Cancelled Item With Refund/ Without refund only bill activity is updated.
	 * 
	 * @parameters
	 * Cancelled order bean BasicDynaBean
	 * Cancel with Refund flag boolean
	 * Cancel without refund flag boolean 
	 * 
	 */
	public void cancelCharges(BasicDynaBean cancelChildTestBean, boolean cancelCharge,
			boolean unlinkActivity,boolean docPartOfPack,String activityCode, String activityId, String chargeId){

		String userName = (String) sessionService.getSessionAttributes().get("userId");
		// cancel charge and associated charges
		if(cancelCharge){
			billChargeService.cancelBillCharge(chargeId, true, userName);// true indicates cancel refs also
//			if (cancelChildTestBean != null) // TODO for Internal Lab charges check with goutham
//				billChargeService.cancelBillCharge((String)cancelChildTestBean.get("charge_id"), true, userName);
	
			billChargeClaimService.cancelBillChargeClaim(chargeId);
			List<BasicDynaBean> associatedChargeList = billChargeService.getAssociatedCharges(chargeId);
			for(BasicDynaBean bean : associatedChargeList){
				String chargeRef = String.valueOf(bean.get("charge_id"));
				billChargeClaimService.cancelBillChargeClaim(chargeRef);
			}
		}
		else if (unlinkActivity) {
			// update the charge as hasActivity = false, when not canceling the charge.
			billChargeService.updateHasActivityStatus(chargeId, false, true);	// true: refs also
			billActivityChargeService.deleteActivity(activityCode, activityId);
	
//			if (cancelChildTestBean != null) {
//				billChargeService.updateHasActivityStatus((String)cancelChildTestBean.get("charge_id"), false, true);	// true: refs also
//				billActivityChargeService.deleteActivity(activityCode,
//						cancelChildTestBean.get("prescribed_id").toString());
//			}
	
		} 
		if (docPartOfPack) {
			// doctor is part of package, just delete the activity. this can be executed when
			// user changes the consulting doctor from op list.
			billActivityChargeService.deleteActivity(activityCode, activityId);
		}
	
	}
	
	/* For edited item, then amounts are updated.
	 * update the prescribing doctor ID in the billcharge
	 */
	public void updateCharges(String prescDrId, String chargeId,
			String activityCode, String activityId, String remarks, Map<String,Object> orderItem){

		billChargeService.updatePrescribingDoctor(chargeId, prescDrId, true);

		BasicDynaBean chargeBean = billChargeService.getCharge(chargeId);
		
		chargeBean.set("user_remarks",remarks);
		if (activityCode.equals("SER") ) {
			chargeBean.set("act_quantity",new BigDecimal((String)orderItem.get("quantity")));
			BigDecimal actRate = new BigDecimal((String)orderItem.get("act_rate"));
			BigDecimal actQuantity = new BigDecimal((String)orderItem.get("act_quantity"));
			BigDecimal discount = new BigDecimal((String)orderItem.get("discount"));
			chargeBean.set("amount",ConversionUtils.setScale(actRate.multiply(actQuantity).subtract(discount)));
			if (discount.compareTo(BigDecimal.ZERO) != 0) {
				chargeBean.set("overall_discount_amt", discount);
				chargeBean.set("overall_discount_auth", -1);
			}
		}

		billChargeService.updateChargeAmounts(chargeBean);
	}
	

	
	/* 
	 * This is for re-process/ re-calculation of bills whose items where cancelled/edited
	 * Get the edited or cancelled order charge related bills.
	 */
	public void getCancelledEditedItemsBills(List<String> editOrCancelOrderBills,Map<String, List<Object>> orderItems) {
		List<String> charges = new ArrayList<String>();
		List orderedItemList = orderService.addOrderItemCharges(orderItems);
		orderService.addOrderCharges(charges, orderedItemList);
		if (charges == null || charges.isEmpty()) {
		    return;
		}
		for(String charge : charges){
			BasicDynaBean chargeBean = billChargeService.findByKey("charge_id", charge);
			String billNo = (chargeBean != null) ? (String)chargeBean.get("bill_no") : null;
			if (billNo != null && !editOrCancelOrderBills.contains(billNo))
				editOrCancelOrderBills.add(billNo);
		}
		
	}
	
	private void calculateServiceCharge(String chargeId, BigDecimal genPrefChargePercent, BigDecimal amount,
			String chargeHead, String fieldToEvaluate, 
			BasicDynaBean billChargeBean, String userId, Timestamp currentTimestamp, BigDecimal billAmt, 
			BigDecimal claimAmt, Map<String, String> errorMap, String billNo, BigDecimal newAmount, BigDecimal chargeableTotalAmt, boolean update,
			String remarks){

		BasicDynaBean serviceChargeHeadBean = chargeHeadsService.getChargeHeadBean(chargeHead);
		
		boolean chargeInsuPayable= true;
		if(null != serviceChargeHeadBean)
			chargeInsuPayable = chargeHead.equals(BillChargeService.CH_BILL_SERVICE_CHARGE) && serviceChargeHeadBean.get(fieldToEvaluate).equals("Y");
		
		if (update) {
			billChargeBean = billChargeService.getBean();
			billChargeBean.set("charge_id", chargeId);
			billChargeBean.set("act_remarks", remarks);
			billChargeBean.set("mod_time", currentTimestamp);
			billChargeBean.set("username", userId);
			billChargeBean.set("act_rate", newAmount);
			billChargeBean.set("amount", newAmount);
			
			if(chargeInsuPayable){
				billChargeBean.set("insurance_claim_amount", newAmount);
			}
				
			if(chargeHead.equals(BillChargeService.CH_ROUND_OFF)){
				billChargeBean.set("insurance_claim_amount", chargeableTotalAmt);
				billChargeBean.set("is_claim_locked", true);
			}
			Map<String,Object> key = new HashMap<>();
			key.put("charge_id", chargeId);
			int result = billChargeService.update(billChargeBean, key);
			
			/**
			 * To update insurance claim amount in bill charge claim table for some of the direct 
			 * charge items like service charge, claim service tax and for round off item.
			 */
			Map<String,Object> keys = new HashMap<>();
      keys.put("bill_no", billNo);
      keys.put("priority", 1);
      BasicDynaBean billClaimBean = billClaimService.findByKey(keys);
      if(null != billClaimBean && null != billClaimBean.get("claim_id")){
        String claimId = (String)billClaimBean.get("claim_id");
        billChargeClaimService.updateInBillChargeClaim(billChargeBean, claimId);
      }

			if (result == 0){
				errorMap.put("error","Error while updating bill service charge for bill no: "+billNo);
			}	
			amount = newAmount;			
		}

		// add the service charge amount to the total
		billAmt = billAmt.add(amount);
		if(chargeInsuPayable){
			claimAmt = claimAmt.add(amount);
		}
		if(chargeHead.equals(BillChargeService.CH_ROUND_OFF)){
			claimAmt = claimAmt.add(chargeableTotalAmt);
		}
	}

	public void dynaPackageProcessor(String billNo) throws ParseException {
		dynaPkgProcessorService.process(billNo);
	}
 
	/**
	 * Calculate (or recalculate) ALL the bed charges applicable to a patient. This cuts across
	 * multiple ip_bed_details records since the whole set of beds results in one set of charges
	 * which may or may not directly relate to each bed detail.
	 * @param planIds 
	 * @param preAuthModeIds 
	 * @param preAuthIds 
	 * @throws ParseException 
	 * @throws SQLException 
	 */
	public void recalculateBedCharges(BasicDynaBean visitbean,
			BasicDynaBean bill, int[] planIds, String[] preAuthIds, Integer[] preAuthModeIds) throws ParseException{
		
		bedChargeCalculationHelper.recalculateBedCharges(visitbean,
				 bill, planIds, preAuthIds, preAuthModeIds);
	
	}
	/*public List<BasicDynaBean> getActiveUnpaidBills(String visitId,
		String billType) {		
		return getActiveUnpaidBills(visitId, billType, false, null);
	}
	
	private List<BasicDynaBean> getActiveUnpaidBills(String visitId,
		String billType, boolean includePhrmBills, Boolean isTpa) {		
		return billRepository.getActiveUnpaidBills(visitId,billType,includePhrmBills,isTpa);
	}*/
	
	public Map<String,String> doPineLabsTransaction(TransactionRequirements transactionReq, Map<String, String[]> paramMap) {
		try {
			
			GenericPaymentsAggregator pineLabsAggregator = paymentGatewayAggregatorFactory.getPaymentGatewayAggregatorInstance("PineLabs");
			BasicDynaBean edcBean = edcMachineService.findByUniqueName(paramMap.get("imei")[0], "imei"); 
			
			Map<String, String> configMap = new HashMap<>();
			configMap.put("userName", sessionService.getSessionAttributes().get("userId").toString());
			configMap.put("sequenceNumber", String.valueOf((paymentTransactionService.getNextSequence()%Integer.MAX_VALUE)));	
			configMap.put("allowedPaymentMode", "1|11");		// 1 for card payments
			configMap.put("autoCancelDurationInMinutes", "5");
			configMap.put("merchantId", edcBean.get("merchant_id").toString());
			configMap.put("securityToken", edcBean.get("security_token").toString());
			configMap.put("merchantPOScode", edcBean.get("merchant_pos_code").toString());
			configMap.put("IMEI", edcBean.get("imei").toString());
			pineLabsAggregator.setConfiguration(configMap);
			
			Map<String, String> urlMap = new HashMap<>();
			urlMap.put("uploadTxnUrl", instaIntegrationService.getActiveRecord("pineLabs-uploadTxn").get("url").toString());
			pineLabsAggregator.setEndpoints(urlMap);
			
			return pineLabsAggregator.doTransaction(transactionReq);
		} catch (IllegalAccessException | InstantiationException e) {
		  log.error("", e);
		}
		return null; 
	}
	
	public Map<String,String> cancelPineLabsTransaction(TransactionRequirements transactionReq, Map<String, String[]> paramMap) {
		try {
			
			GenericPaymentsAggregator pineLabsAggregator = paymentGatewayAggregatorFactory.getPaymentGatewayAggregatorInstance("PineLabs");
			BasicDynaBean edcBean = edcMachineService.findByUniqueName(paramMap.get("imei")[0], "imei");
			Map<String, String> configMap = new HashMap<>();
			configMap.put("merchantId", edcBean.get("merchant_id").toString());
			configMap.put("securityToken", edcBean.get("security_token").toString());
			configMap.put("merchantPOScode", edcBean.get("merchant_pos_code").toString());
			configMap.put("IMEI", edcBean.get("imei").toString());
			configMap.put("plutusTransactionReferenceID", paramMap.get("plutusTxnId")[0]);
			pineLabsAggregator.setConfiguration(configMap);
			
			Map<String, String> urlMap = new HashMap<>();
			urlMap.put("cancelTxnUrl", instaIntegrationService.getActiveRecord("pineLabs-cancelTxn").get("url").toString());
			pineLabsAggregator.setEndpoints(urlMap);
			
			return pineLabsAggregator.cancelTransaction(transactionReq);
			
		} catch (IllegalAccessException | InstantiationException e) {
		  log.error("", e);
		}
		return null;
	}
	
	public ArrayList<String> getCreditNoteList(String visitId) {
    // TODO Auto-generated method stub
    List<BasicDynaBean> creditNoteList = billRepository.getCreditNoteList(visitId);
    ArrayList<String> billNoList = new ArrayList<String>();
    
    for(BasicDynaBean billBean : creditNoteList){
      billNoList.add((String) billBean.get("bill_no"));
    }
    return billNoList;
	}
	
	public List<BasicDynaBean> getOpenTpaBills(String visitId) {
		// TODO Auto-generated method stub
		return billRepository.getOpenTpaBills(visitId);
	}

	public List<BasicDynaBean> getAllTpaBills(String visitId) {
		// TODO Auto-generated method stub
		return billRepository.getAllTpaBills(visitId);
	}

	public List<BasicDynaBean> getNonInsuranceOpenBills(String visitId) {
		// TODO Auto-generated method stub
		return billRepository.getNonInsuranceOpenBills(visitId);
	}

	public List<BasicDynaBean> getClosedAndFinalizedTpaBills(String visitId) {
		// TODO Auto-generated method stub
		return billRepository.getClosedAndFinalizedTpaBills(visitId);
	}

	public boolean reopenBill(String billNo, String reopenReason, String userName) {
		// TODO Auto-generated method stub
		boolean success = true;		
		BasicDynaBean billBean = billRepository.findByKey("bill_no", billNo);

		// set the status to open
		billBean.set("status", Bill.BILL_STATUS_OPEN);
		billBean.set("payment_status", Bill.BILL_PAYMENT_UNPAID);
		billBean.set("reopen_reason", reopenReason);
		billBean.set("discharge_status", "N");
		billBean.set("bill_printed","N");
		billBean.set("writeoff_remarks","");
		billBean.set("sponsor_writeoff_remarks","");
		billBean.set("patient_writeoff", "N");
		billBean.set("sponsor_writeoff", "N");
		
		java.util.Date parsedDate = new java.util.Date();
    java.sql.Timestamp datetime = new java.sql.Timestamp(parsedDate.getTime());
    
		billBean.set("mod_time", new Timestamp(datetime.getTime()));
		billBean.set("username", userName);
		
		Map<String, Object> keys = new HashMap<>();
		keys.put("bill_no", billNo);
		
		success = success && billRepository.update(billBean, keys) >= 0;
		
		success = success && billChargeService.unlockBillCharges(billNo);

		// Set financial discharge to false
		if(success && dischargeService.checkIfPatientDischargeEntryExists((String)billBean.get("visit_id"))) {
			success = success && dischargeService.insertOrUpdateFinancialDischargeDetails((String)billBean.get("visit_id"), false, userName, true);					
		}
		return success;
	}

	public List<BasicDynaBean> getVisitCreditNote(String visitId) {
		// TODO Auto-generated method stub
		return billRepository.getCreditNoteList(visitId);
	}

	public List<BasicDynaBean> getAllTpaBillsAndCreditNotes(String visitId) {
		// TODO Auto-generated method stub
		return billRepository.getAllTpaBillsAndCreditNotes(visitId);
	}

	public Boolean updateBillRatePlan(String insurancePlanDefaultRatePlan,
			String billNos) {
		// TODO Auto-generated method stub
		return billRepository.updateBillRatePlan(insurancePlanDefaultRatePlan, billNos);
	}

	public Map<String, String> checkPineLabsTransactionStatus(Map<String, String[]> paramMap) {
		try {

			GenericPaymentsAggregator pineLabsAggregator = paymentGatewayAggregatorFactory
					.getPaymentGatewayAggregatorInstance("PineLabs");
			BasicDynaBean edcBean = edcMachineService.findByUniqueName(paramMap.get("imei")[0], "imei");
			Map<String, String> configMap = new HashMap<>();
			configMap.put("merchantId", edcBean.get("merchant_id").toString());
			configMap.put("securityToken", edcBean.get("security_token").toString());
			configMap.put("merchantPOScode", edcBean.get("merchant_pos_code").toString());
			configMap.put("IMEI", edcBean.get("imei").toString());
			configMap.put("plutusTransactionReferenceID", paramMap.get("plutusTxnId")[0]);
			pineLabsAggregator.setConfiguration(configMap);

			Map<String, String> urlMap = new HashMap<>();
			urlMap.put("checkTxnStatusUrl",
					instaIntegrationService.getActiveRecord("pineLabs-checkTxnStatus").get("url").toString());
			pineLabsAggregator.setEndpoints(urlMap);

			return pineLabsAggregator.checkTransactionStatus();

		} catch (IllegalAccessException | InstantiationException e) {
		  log.error("", e);
		}
		return null;
	}

	public Map<String, String> cancelPendingPineLabsTransaction(String imei, boolean forceClose) {
		Map<String, Object> filterMap = new HashMap<>();
		filterMap.put("edc_imei", imei);
		filterMap.put("status_message", "TXN UPLOADED");
		if (!forceClose) {
		  filterMap.put("initiated_by", sessionService.getSessionAttributes().get("userId").toString());
		}		
		BasicDynaBean paymentBean = paymentTransactionService.findByKey(filterMap);

		if (!forceClose && paymentBean == null) {
		  filterMap = new HashMap<>();
	    filterMap.put("edc_imei", imei);
	    filterMap.put("status_message", "TXN UPLOADED");
	    paymentBean = paymentTransactionService.findByKey(filterMap);
	    String[] params = {paymentBean.get("initiated_by").toString(), paymentBean.get("bill_no").toString(), 
	        paymentBean.get("amount").toString()};
	    throw new ResourceLockedException(HttpStatus.LOCKED, "exception.resource.locked", params);

		}
		TransactionRequirements transactionReq = new TransactionRequirements();
		transactionReq.setAmount(Float.parseFloat(paymentBean.get("amount").toString()));
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put("imei", new String[] { imei });
		paramMap.put("plutusTxnId", new String[] { paymentBean.get("plutus_txn_id").toString() });
		return cancelPineLabsTransaction(transactionReq, paramMap);
	}

  public List<BasicDynaBean> getVisitOpenBillsExcludingPackageBills(String visitId) {
    return billRepository.getVisitOpenBillsExcludingPackageBills(visitId);
  }
	
  public BasicDynaBean getBillRatePlanAndBedType(String billNo) {
    return billRepository.getBillRatePlanAndBedType(billNo);
  }

  public boolean setBillPaidStatus(String billNo) {
    BasicDynaBean billAmountsBean = getBillAmounts(billNo);

    BigDecimal totalAmt = (BigDecimal) billAmountsBean.get("total_amount");
    BigDecimal totalInsAmt = (BigDecimal) billAmountsBean.get("total_claim");
    BigDecimal depSetoff = (BigDecimal) billAmountsBean.get("deposit_set_off");
    BigDecimal recpt = (BigDecimal) billAmountsBean.get("total_receipts");
    BigDecimal insRecpt = (BigDecimal) billAmountsBean.get("primary_total_sponsor_receipts");

    BigDecimal totAmtDue = totalAmt.subtract(totalInsAmt).subtract(depSetoff)
        .subtract((recpt.add(insRecpt)));

    if (totAmtDue.compareTo(BigDecimal.ZERO) == 0) {
      BasicDynaBean billBean = getBean();
      billBean.set("payment_status", "P");
      billBean.set("discharge_status", "Y");
      Map<String, Object> keys = new HashMap<>();
      keys.put("bill_no", billNo);
      return update(billBean, keys);
    }
    return false;
  }


  public void resetTotalsOrReProcess(String billNo) throws ParseException {
    resetTotalsOrReProcess(billNo, true, true, true);
  }

  public void resetTotalsOrReProcess(String billNo, boolean dynaReProcess, boolean perdiemReProcess,
      boolean perdiemRecalc) throws ParseException {


    String userid = (String) sessionService.getSessionAttributes().get("userId");
    if (perdiemReProcess || perdiemRecalc) {
      // TODO: perdiemProcess(billNo, perdiemReProcess, perdiemRecalc)
    }

    if (dynaReProcess) {
      dynaPackageProcessor(billNo);
    }

    // Reset service charge, claim service tax if exists in bill.
    resetServiceTaxClaimCharge(billNo,userid);

    // Reset round off if exists in bill.
    resetRoundOff(billNo,userid);

    //drgProcessing.
    Map drgCodeMap = getBillDRGCode(billNo);
    if(null != drgCodeMap && null != drgCodeMap.get("drg_charge_id")){
      drgCalculatorService.processDRG(billNo, (String)drgCodeMap.get("drg_code"));
    }

  }
  
  public void resetDirectChargesInBill(String billNo){
    
    String userid = (String) sessionService.getSessionAttributes().get("userId");
    
    // Reset service charge, claim service tax if exists in bill.
    resetServiceTaxClaimCharge(billNo,userid);

    // Reset round off if exists in bill.
      resetRoundOff(billNo,userid);
  }
  
  public void reProcessBill(String billNo) {
    // TODO Auto-generated method stub
   reProcessBill(billNo, true, true, true);
  }
  
  public void reProcessBill(String billNo, boolean dynaReProcess, boolean perdiemReProcess,
      boolean perdiemRecalc){
    if (perdiemReProcess || perdiemRecalc) {
      // TODO: perdiemProcess(billNo, perdiemReProcess, perdiemRecalc)
    }

    if (dynaReProcess) {
      try {
        dynaPackageProcessor(billNo);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    //drgProcessing.
    Map drgCodeMap = getBillDRGCode(billNo);
    if(null != drgCodeMap && null != drgCodeMap.get("drg_charge_id")){
      drgCalculatorService.processDRG(billNo, (String)drgCodeMap.get("drg_code"));
    }
  }
  
  public List<BasicDynaBean> getCommonOrderIds(String visitId) {
    return billRepository.getCommonOrderIds(visitId);
  }

  public List<BasicDynaBean> getMvpUnpaidBillsForVisit(String visitId, Integer patPackageId, Date orderDateTime) {
    return billRepository.getMvpUnpaidBillsForVisit(visitId, patPackageId, orderDateTime);
  }

  public List<BasicDynaBean> getMvpBillsForVisit(String visitId) {
    return billRepository.getMvpBillsForVisit(visitId);
  }
  
  public BigDecimal getVisitPatientDue(String visitId) {
    return billRepository.getVisitPatientDue(visitId);
  }
	
  public Boolean isMultiVisitPackageBill(String billNo) {
    // TODO Auto-generated method stub
    return billRepository.isMultiVisitPackageBill(billNo);
  }

  public List<BasicDynaBean> getOpenMultiVisitPkgTPABills(String visitId) {
    // TODO Auto-generated method stub
    return billRepository.getOpenMultiVisitPkgTPABills(visitId);
  }

  public List<BasicDynaBean> getAllMultiVisitPkgTPABills(String visitId) {
    // TODO Auto-generated method stub
    return billRepository.getAllMultiVisitPkgTPABills(visitId);
  }

  public Boolean updateInsurancePlanDefaultDiscountPlan(Integer defaultDiscPlanId, String billNos) {
    // TODO Auto-generated method stub
    return billRepository.updateInsurancePlanDefaultDiscountPlan(defaultDiscPlanId, billNos);
  }

	public void batchUpdate(List<BasicDynaBean> beans, Map<String,Object> keys){
		billRepository.batchUpdate(beans, keys);
	}

  public BigDecimal getAvailableGeneralAndIpDeposit(String visitId) {
    BigDecimal depositBal = BigDecimal.ZERO;
    BasicDynaBean depositBalBean = billRepository.getAvailableGeneralAndIpDeposit(visitId);
    if (depositBalBean != null) {
      depositBal = (BigDecimal) depositBalBean.get("hosp_available_deposit");
    }
    return depositBal;
  }
	
  public BigDecimal getAvailableCreditLimit(String visitId, boolean excludePatDue) {
    BigDecimal availableCreditLimit;
    BigDecimal sanctionedCreditLimit = regService.getIpCreditLimitAmount(visitId);
    BigDecimal availableDepositsBal = getAvailableGeneralAndIpDeposit(visitId);
    BigDecimal visitPatientDue = getVisitPatientDue(visitId);
    if (excludePatDue) {
      availableCreditLimit = sanctionedCreditLimit.add(availableDepositsBal);
    } else {
      availableCreditLimit = sanctionedCreditLimit.add(availableDepositsBal).subtract(
          visitPatientDue);
    }
    return availableCreditLimit;
  }
	
  public Map<String, Object> getCreditLimitDetails(String visitId) {
    Map<String, Object> creditLimitDetailsMap = new HashMap<>();
    BigDecimal visitPatientDue = getVisitPatientDue(visitId);
    creditLimitDetailsMap.put("visitPatientDue", visitPatientDue);
    BigDecimal availableDepositsBal = getAvailableGeneralAndIpDeposit(visitId);
    creditLimitDetailsMap.put("availableDeposit", availableDepositsBal);
    BigDecimal sanctionedCreditLimit = regService.getIpCreditLimitAmount(visitId);
    creditLimitDetailsMap.put("sanctionedCreditLimit", sanctionedCreditLimit);
    BigDecimal availableCreditLimitWithoutDue = getAvailableCreditLimit(visitId, true);
    creditLimitDetailsMap.put("availableCreditLimitWithoutDue", availableCreditLimitWithoutDue);
    BigDecimal availableCreditLimit = getAvailableCreditLimit(visitId, false);
    creditLimitDetailsMap.put("availableCreditLimit", availableCreditLimit);
    return creditLimitDetailsMap;
  }
  
  /**
   * Gets the visit id.
   *
   * @param billNo String
   * @return the visit id
   */
  public String getVisitId(String billNo) {
    return billRepository.getVisitId(billNo);
  }

  /**
   * Gets the DRG code.
   *
   * @param patientId String
   * @return the DRG code
   */
  public Map getDRGCode(String patientId) {
    return billRepository.getDRGCode(patientId);
  }
  
  /**
   * Gets the bill DRG code.
   *
   * @param billNo the bill no
   * @return the bill DRG code
   */
  public Map getBillDRGCode(String billNo) {
    return billRepository.getBillDRGCode(billNo);
  }

  /**
   * Gets the bill.
   *
   * @param billNo String
   * @return the bill
   */
  public BasicDynaBean getBill(String billNo) {
    return billRepository.getBill(billNo);
  }

  public List<BasicDynaBean> getVisitOpenMvpBills(String visitId) {
    return billRepository.getVisitOpenMvpBills(visitId);
  }
  
  public List<BasicDynaBean> getVisitOpenBills(String visitId) {
    // TODO Auto-generated method stub
    return billRepository.getVisitOpenBills(visitId);
  }
  
  /**
   * Gets the visit finalized bills.
   *
   * @param visitId the visit id
   * @return the visit finalized bills
   */
  public List<BasicDynaBean> getVisitFinalizedAndClosedBills(String visitId) {
    return billRepository.getVisitFinalizedAndClosedBills(visitId);
  }

  public void updatePkgMarginSponsorAmount(String visitId) {

    List<BasicDynaBean> billList = billRepository.getAllOpenDynaPkgBills(visitId);
    if( null != billList && billList.size() > 1)
      return;
    
    for(BasicDynaBean billBean : billList){
      Map<String,Object> keys = new HashMap<>();
      keys.put("patient_id", visitId);
      keys.put("priority", 1);
      BasicDynaBean visitplanBean = patientInsurancePlansService.findByKey(keys);
      BigDecimal approvalLimit = (null !=  visitplanBean && null != visitplanBean.get("visit_limit")) ? (BigDecimal)visitplanBean.get("visit_limit") : BigDecimal.ZERO;
      BigDecimal totalClaim = (BigDecimal)billBean.get("total_claim");
      BigDecimal dynaPkgCharge = null !=  billBean.get("dyna_package_charge") ? (BigDecimal)billBean.get("dyna_package_charge") : BigDecimal.ZERO;
      
      if(approvalLimit.compareTo(BigDecimal.ZERO) != 0 && dynaPkgCharge.compareTo(approvalLimit) > 0
          && totalClaim.compareTo(approvalLimit) > 0){
        BigDecimal diffAmount = totalClaim.subtract(approvalLimit); 
        
        Map<String,Object> marKeys = new HashMap<>();
        marKeys.put("bill_no", billBean.get("bill_no"));
        marKeys.put("charge_head", "MARPKG");
        BasicDynaBean pkgMarginBean = billChargeRepo.findByKey(marKeys);
        BigDecimal marginClaimAmt = (BigDecimal)pkgMarginBean.get("insurance_claim_amount");
        if(marginClaimAmt.compareTo(diffAmount) >= 0){
          marginClaimAmt = marginClaimAmt.subtract(diffAmount);
          pkgMarginBean.set("insurance_claim_amount", marginClaimAmt);
          billChargeClaimService.updatepackageMarginInBillChgClaim(pkgMarginBean);
        }
      }
    }
  }

  public int updateBillPrintedStatus(String billNo) {
    return billRepository.updateBillPrintedStatus(billNo);
  }
  
  public void updateCaseRateLimts(BasicDynaBean visitBean) {

    String visitId = (String)visitBean.get("patient_id");
    
    Integer priCaseRateId = null != visitBean.get("primary_case_rate_id") ? 
        (Integer)visitBean.get("primary_case_rate_id") : null;
    Integer secCaseRateId = null != visitBean.get("secondary_case_rate_id") ? 
        (Integer)visitBean.get("secondary_case_rate_id")  : null;
    
    if(null != priCaseRateId){
      patientInsurancePlansService.updateCaseRateLimits(priCaseRateId, visitId, 1);
    }
      
    if(null != secCaseRateId){
      patientInsurancePlansService.updateCaseRateLimits(secCaseRateId, visitId, 2);
    }
  }

  public Map<String, Object> getOpenFinalizedBillDetails(
		String mrNo, String visitType) {
	  
	  Map<String, Object> billsData = new HashMap<>();
	  billsData.put("bill_print_template_list", getBillPrintTemplate());
	  billsData.put("printer_definition", ConversionUtils.listBeanToListMap(printerDefinitionService.lookup(false)));
	  Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
	  Integer centerId = (Integer) sessionAttributes.get("centerId");
	  List<BasicDynaBean> billDetails = billRepository.getOpenFinalizedBillDetails(mrNo, visitType,
	      centerId);
		Integer centerIdFilterForLatestVisit = centerId == 0 ? null : centerId;
	  BasicDynaBean latestVisitBean = regService.getPatientLatestVisit(mrNo, null, visitType, centerIdFilterForLatestVisit);
		String latestVisitId = (null != latestVisitBean) ? (String) latestVisitBean.get("patient_id") : null;
	  List<BasicDynaBean> openBillsList = new ArrayList<BasicDynaBean>();
	  List<BasicDynaBean> finalizedClosedBillsList = new ArrayList<BasicDynaBean>();
	  for(BasicDynaBean bill : billDetails){
		  if(bill.get("status").equals("A")){
			  openBillsList.add(bill);
		  }else{
			  finalizedClosedBillsList.add(bill);
		  }
	  }
	  billsData.put("open_bills", ConversionUtils.listBeanToListMap(openBillsList));
	  billsData.put("finalized_closed_bills", ConversionUtils.listBeanToListMap(finalizedClosedBillsList));
	  billsData.put("latestVisitId", latestVisitId);
 	  return billsData;
  }

  public Boolean isBillNumberExist(String parameter) {
    return billRepository.exist("bill_no", parameter);
  }
  
  /**
   * get all visit Details for a patient on basis of visit type.
   * @param mrNo the mrNo
   * @param visitType the visitType
   * @return map
   */
  public Map<String, Object> getAllVisitDetails(String mrNo, String visitType) {
    return regService.getAllVisitDetails(mrNo, visitType);
  }

  public List<BasicDynaBean> getAllPharmacyBillsOfBill(String billNo) {
	return billRepository.getAllPharmacyBillsOfBill(billNo);
  }

  /**
   * get all bills not posted to hms accounting info.
   * @return List of Bills
   */
  public List<BasicDynaBean> getBillsNotInHmsAccountingInfo() {
    BasicDynaBean genPerfs = genPrefService.getPreferences();
    int relStart = (int) genPerfs.get("accounting_missing_data_scan_rel_start");
    int relEnd = (int) genPerfs.get("accounting_missing_data_scan_rel_end");
    return billRepository.getBillsNotInHmsAccountingInfo(relStart, relEnd);
  }

  /**
   * update the sponsor write off status
   * @param billNo the bill no
   *
   */

  public void updateSpnsrWriteOffStatus(String billNo, String remarks) {
	  BasicDynaBean billBean = getBean();
	  billBean.set("sponsor_writeoff", "A");
	  billBean.set("sponsor_writeoff_remarks", remarks);
      Map<String, Object> keys = new HashMap<>();
      keys.put("bill_no", billNo);
      billRepository.update(billBean, keys);
  }

  /**
   * update the sponsor write off status
   * @param billNo the bill no
   * @param ipModAct the ipModAct
   */

  public void closeBillAutoOnWriteOffApproval(String billNo, String ipModAct, String mrNo) {
    BigDecimal patientDue = billRepository.getNetPatientDue(billNo);
    BasicDynaBean billBean = billRepository.findByKey("bill_no", billNo);
    String patientWriteOff = (String)billBean.get("patient_writeoff");
    String userName = (String) sessionService.getSessionAttributes().get("userId");
    if(patientDue.compareTo(BigDecimal.ZERO) == 0 || patientWriteOff.equals("A")) {
      BasicDynaBean bill = getBean();
      String dischargeStatus = null;
        if(null != billBean.get("discharge_status")){
          dischargeStatus = (String)billBean.get("discharge_status");
        }
        bill.set("primary_claim_status", Bill.BILL_CLAIM_RECEIVED);
        String origDischargeStatus = (String) billBean.get("discharge_status");
        String okToDischarge = "N";
        if (billRepository.getBillType((String) billBean.get("visit_id")).equals(Bill.BILL_TYPE_PREPAID)) {
          okToDischarge = "Y";
        }
        if (billRepository.getBillType((String) billBean.get("visit_id")).equals(Bill.BILL_TYPE_CREDIT)
            && ipModAct.equalsIgnoreCase("Y") && billBean.get("visit_type").equals(Bill.BILL_VISIT_TYPE_IP)) {
          if (okToDischarge.equals("Y")) {
            dischargeStatus = "D";
          } else {
            if (origDischargeStatus.equals("N") && okToDischarge.equals("Y")) {
              dischargeStatus = "Y";
            } else if (okToDischarge.equals("Y")) {
            	dischargeStatus = "D";
            } else {
                dischargeStatus = "N";
            }
          }
		}
        Timestamp now = new Timestamp(new java.util.Date().getTime());
        Timestamp finalizedDate = (Timestamp) bill.get("finalized_date") == null ? now :
            (Timestamp) bill.get("finalized_date");
        Map<String, Object> keys = new HashMap<>();
        keys.put("bill_no", billNo);
        billRepository.update(bill, keys);
        receiptService.updatePayments(billBean, "C", Bill.BILL_PAYMENT_PAID, dischargeStatus, finalizedDate, userName, false, false,
                false, mrNo);
        icRepository.updateSponsorWriteOffClaimClose(billNo, "C");
      }
  }

}
