/* *******************************************************************************************
 * Copyright 2008-2009, All rights reserved.
 * This software is the confidential and proprietary information of Insta health solutions pvt ltd.
 * ********************************************************************************************/

/*
 * ChargeDTO: simple DTO containing a charge
 */

package com.insta.hms.billing;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.insurance.AdvanceInsuranceCalculator;
import com.insta.hms.master.Accounting.ChargeHeadsDAO;
import com.insta.hms.master.PlanMaster.PlanDetailsDAO;
import com.insta.hms.master.ServiceSubGroup.ServiceSubGroupDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class ChargeDTO {

    static Logger log = LoggerFactory.getLogger(ChargeDTO.class);

	private String chargeId;
	private String billNo;
	private String chargeGroup;				// see CG_xxx static final variables
	private String chargeHead;				// see CH_xxx static final variables

	private String actDepartmentId;
	private String actDescriptionId;
	private String actDescription;
	private String actRemarks;
	private BigDecimal actRate = new BigDecimal(0);
	private String actUnit;
	private BigDecimal actQuantity = new BigDecimal(1);

	private boolean isInsuranceBill;

	private BigDecimal amount = new BigDecimal(0);
	private BigDecimal actualAmount = new BigDecimal(0); // Amount before modified & used in transactions
	private BigDecimal discount = new BigDecimal(0);
	private BigDecimal taxAmt = new BigDecimal(0);
	private BigDecimal returnTaxAmt = new BigDecimal(0);
	private BigDecimal originalTaxAmt = new BigDecimal(0);
	private BigDecimal returnOriginalTaxAmt = new BigDecimal(0);
	
	private BigDecimal sponsorTaxAmt = new BigDecimal(0);
	private BigDecimal priTaxAmt = new BigDecimal(0);
	private BigDecimal secTaxAmt = new BigDecimal(0);

	private String discountReason;

	private String chargeRef;
	private BigDecimal doctorAmount = new BigDecimal(0);

	private Timestamp postedDate;
    private String status;				// see CHARGE_STATUS static final variables
    private String username;
	private Timestamp modTime;

	private String approvalId;
	private BigDecimal originalRate = new BigDecimal(0);
	private BigDecimal packageUnit = new BigDecimal(1);		// applicable for medicines

	private String chargeActivityId; //Activity Id from bill_activity_chagre table

	private boolean hasActivity;
	private int activityId;				// used only in the case there is one single activity
	private String activityCode;		// used only in the case there is one single activity
	private String activityConducted;	// used only in the case there is one single activity
	private Date conductedDateTime;		// used only in the case there is one single activity
	private int consultationToken;

	private String docPaymentId;
	private String refPaymentId;
	private String ohPaymentId;
	private String payeeDoctorId;		// conducting doctor

	private BigDecimal prescribingDrAmount = new BigDecimal(0);
	private String prescribingDrPaymentId;
	private String prescribingDrId;

	private BigDecimal referalAmount = new BigDecimal(0);
	private BigDecimal ohAmount = new BigDecimal(0);

	private BigDecimal insuranceClaimAmount = null;
	private String insurancePayable;
	private String insuranceClaimTaxable;
	private BigDecimal origInsuranceClaimAmount = null;

	private BigDecimal[] claimAmounts = null;
	private BigDecimal[] sponsorTaxAmounts = null;
	
	private String[] includeInClaimCalc = null;
	
	private List<BasicDynaBean> billChargeTaxes = null;
	private boolean allowZeroClaim = false ; // used for E-Claim xml generation
	private String bedType;
	private String isSystemDiscount;

	public List<BasicDynaBean> getBillChargeTaxes() {
		return billChargeTaxes;
	}

	public void setBillChargeTaxes(List<BasicDynaBean> billChargeTaxes) {
		this.billChargeTaxes = billChargeTaxes;
	}

	public String[] getIncludeInClaimCalc() {
		return includeInClaimCalc;
	}

	public void setIncludeInClaimCalc(String[] includeInClaimCalc) {
		this.includeInClaimCalc = includeInClaimCalc;
	}

	//New columns for insurance (returns)
	private BigDecimal returnInsuranceClaimAmt;
	private BigDecimal returnAmt;
	private BigDecimal returnQty;

	private BigDecimal claimRecdAmount;
	private String claimStatus;

	private int discount_auth_dr;
	private String discount_auth_dr_name;
	private BigDecimal dr_discount_amt = new BigDecimal(0);

	private int discount_auth_pres_dr;
	private String discount_auth_pres_dr_name;
	private BigDecimal pres_dr_discount_amt = new BigDecimal(0);

	private int discount_auth_ref;
	private String discount_auth_ref_name;
	private BigDecimal ref_discount_amt = new BigDecimal(0);

	private int discount_auth_hosp;
	private String discount_auth_hosp_name;
	private BigDecimal hosp_discount_amt = new BigDecimal(0);

	private int overall_discount_auth;
	private String overall_discount_auth_name;
	private BigDecimal overall_discount_amt = new BigDecimal(0);
	private int account_group;

	private String actItemCode;
	private String actRatePlanItemCode;

	private boolean allowDiscount = true;
	private boolean allowRateVariation = true;

	private Integer orderNumber;
	private int serviceGroupId;
	private int serviceSubGroupId;
	private int insuranceCategoryId;

	private String serviceGroupName;
	private String serviceSubGroupName;
	private String codeType;

	private BigDecimal amount_included = new BigDecimal(0);
	private BigDecimal qty_included = new BigDecimal(0);
	private String chargeExcluded;
	private String packageFinalized = "N";
	private String conducting_doc_mandatory;
	private boolean conduction_required;
	private int consultation_type_id;
	private String op_id;
	private Timestamp from_date;
	private Timestamp to_date;

	private String userRemarks;
	private String itemRemarks;

	private String serviceChrgApplicable;
	private Integer surgeryAnesthesiaDetailsId;
	private Integer billingGroupId;
	private String revenueDepartmentId;

	/*
	 * Expanded attributes, not part of the table. These may or may not be filled
	 * depending on the getXXX method that is being used.
	 */
	// ids to names
    private String actDepartmentName;
	private String chargeGroupName;
	private String chargeHeadName;

	// some useful attributes of the bill
	private String billType;
	private String billStatus;
	private java.sql.Date billFinalizedDate;

	// some useful attributes of the patient/visit
	private String visitId;
	private String visitType;
	private String mrNo;
	private String patientName;
	private String patientLastName;
	private String doctorName;
	private String wardName;
	private String customerName;

	private String preAuthId;
	private String[] preAuthIds = null;
	private Integer preAuthModeId;
	private Integer[] preAuthModeIds = null;
	private Boolean firstOfCategory = true;

	private boolean allowRateDecrease;
	private boolean allowRateIncrease;

	private int redeemed_points;
	private String eligible_to_redeem_points;
	private BigDecimal redemption_cap_percent = new BigDecimal(0);

	private Boolean isClaimLocked;
	private Boolean itemExcludedFromDoctor;
	private String doctorExclusionRemarks;
	private Integer packageId;
	private String billDisplayType;
	private BigDecimal cashRate;
	private String submissionBatchType;
	private Integer panelId;
	private String dynaPackageExcluded;

	public BigDecimal getCashRate() {
    return cashRate;
  }

  public void setCashRate(BigDecimal cashRate) {
    this.cashRate = cashRate;
  }

  /*
	 * Static variables (constants)
	 */
	// charge group constants
	public static final String CG_REGISTRATION = "REG";
	public static final String CG_DIAGNOSTICS = "DIA";
	public static final String CG_DOCTOR = "DOC";
	public static final String CG_BED = "BED";
	public static final String CG_ICU = "ICU";
	public static final String CG_OPERATION = "OPE";
	public static final String CG_MEDICINES = "MED";
	public static final String CG_INVENTORY = "ITE";
	public static final String CG_OTHERS = "OTC";
	public static final String CG_TAXES = "TAX";
	public static final String CG_SERVICE_PROCEDURES = "SNP";
	public static final String CG_DISCOUNTS = "DIS";
	public static final String CG_PACKAGE = "PKG";
	public static final String CG_RETURNS = "RET";
	public static final String CG_DIETARY = "DIE";

	// charge head constants
	public static final String CH_GENERAL_REGISTRATION = "GREG";
	public static final String CH_IP_REGISTRATION = "IPREG";
	public static final String CH_OP_REGISTRATION = "OPREG";
	public static final String CH_DIAG_REGISTRATION = "DREG";
	public static final String CH_EMR_REGISTRATION = "EMREG";
	public static final String CH_MLC_REGISTRATION = "MLREG";

	public static final String CH_OP_CONSULTATION = "OPDOC";
	public static final String CH_OP_REVISIT_CONSULTATION = "ROPDOC";
	public static final String CH_IP_CONSULTATION = "IPDOC";
	public static final String CH_NIGHT_IP_CONSULTATION = "NIPDOC";

	public static final String CH_DIAG_LAB = "LTDIA";
	public static final String CH_DIAG_RAD = "RTDIA";

	public static final String CH_BED = "BBED";
	public static final String CH_BYBED = "BYBED";
	public static final String CH_NURSE = "NCBED";
	public static final String CH_DUTY_DOCTOR = "DDBED";
	public static final String CH_PROFESSIONAL = "PCBED";

	public static final String CH_BED_ICU = "BICU";
	public static final String CH_NURSE_ICU = "NCICU";
	public static final String CH_DUTY_DOCTOR_ICU = "DDICU";
	public static final String CH_PROFESSIONAL_ICU = "PCICU";

	public static final String CH_SURGEON = "SUOPE";
	public static final String CH_THEATRE =  "TCOPE";
	public static final String CH_ANAESTHETIST = "ANAOPE";
	public static final String CH_OT_CONSUMABLE = "CONOPE";
	public static final String CH_OT_EQUIPMENT = "EQOPE";
	public static final String CH_SURGICAL_ASSISTANCE = "SACOPE";
	public static final String CH_ASSISTANT_SURGEON = "ASUOPE";
	public static final String CH_ASSISTANT_ANAESTHETIST = "AANOPE";
	public static final String CH_CO_OP_SURGEON = "COSOPE";
	public static final String CH_ANASTHESIA_TYPE = "ANATOPE";

	public static final String CH_ANASTHETIST = "ANAOPE";		// duplicate for convenience
	public static final String CH_THEATER =  "TCOPE";		// duplicate for convenience

	public static final String CH_MEDICINE = "MEMED";
	public static final String CH_PHARMACY_MEDICINE = "PHMED";
	public static final String CH_PHARMACY_CREDIT_MEDICINE = "PHCMED";		// for add to bill
	public static final String CH_MED_CONSUMABLE = "CONMED";

	public static final String CH_INVENTORY_ITEM = "INVITE";

	public static final String CH_OTHER = "OCOTC";
	public static final String CH_CONSUMABLE = "CONOTC";
	public static final String CH_MISCELLANEOUS = "MISOTC";
	public static final String CH_EQUIPMENT = "EQUOTC";
	public static final String CH_IMPLANT = "IMPOTC";

  public static final String CH_SERVICE = "SERSNP";
  public static final String CH_BILL_DISCOUNTS = "BIDIS";
  public static final String CH_PACKAGE = "PKGPKG";
  public static final String CH_PHARMACY_RETURNS = "PHRET";
  public static final String CH_PHARMACY_CREDIT_RETURNS = "PHCRET";
  public static final String CH_INVENTORY_RETURNS = "INVRET";
  public static final String CH_DIETARY = "MDIE";
  
  // package charge heads
  public static final String CH_PKG_GENERAL_REGISTRATION = "PGREG";
  public static final String CH_PKG_IP_REGISTRATION = "PIPREG";
  public static final String CH_PKG_OP_REGISTRATION = "POPREG";
  public static final String CH_PKG_DIAG_REGISTRATION = "PDREG";
  public static final String CH_PKG_EMR_REGISTRATION = "PEMREG";
  public static final String CH_PKG_MLC_REGISTRATION = "PMLREG";

  public static final String CH_PKG_OP_CONSULTATION = "POPDOC";
  public static final String CH_PKG_OP_REVISIT_CONSULTATION = "PROPDOC";
  public static final String CH_PKG_IP_CONSULTATION = "PIPDOC";
  public static final String CH_PKG_NIGHT_IP_CONSULTATION = "PNIPDOC";

  public static final String CH_PKG_DIAG_LAB = "PLTDIA";
  public static final String CH_PKG_DIAG_RAD = "PRTDIA";

  public static final String CH_PKG_BED = "PBBED";
  public static final String CH_PKG_BYBED = "PBYBED";
  public static final String CH_PKG_NURSE = "PNCBED";
  public static final String CH_PKG_DUTY_DOCTOR = "PDDBED";
  public static final String CH_PKG_PROFESSIONAL = "PPCBED";

  public static final String CH_PKG_BED_ICU = "PBICU";
  public static final String CH_PKG_NURSE_ICU = "PNCICU";
  public static final String CH_PKG_DUTY_DOCTOR_ICU = "PDDICU";
  public static final String CH_PKG_PROFESSIONAL_ICU = "PPCICU";

  public static final String CH_PKG_SURGEON = "PSUOPE";
  public static final String CH_PKG_THEATRE = "PTCOPE";
  public static final String CH_PKG_ANAESTHETIST = "PANAOPE";
  public static final String CH_PKG_OT_CONSUMABLE = "PCONOPE";
  public static final String CH_PKG_OT_EQUIPMENT = "PEQOPE";
  public static final String CH_PKG_SURGICAL_ASSISTANCE = "PSACOPE";
  public static final String CH_PKG_ASSISTANT_SURGEON = "PASUOPE";
  public static final String CH_PKG_ASSISTANT_ANAESTHETIST = "PAANOPE";
  public static final String CH_PKG_CO_OP_SURGEON = "PCOSOPE";
  public static final String CH_PKG_ANASTHESIA_TYPE = "PANATOPE";

  public static final String CH_PKG_ANASTHETIST = "PANAOPE";
  public static final String CH_PKG_THEATER = "PTCOPE";

  public static final String CH_PKG_MEDICINE = "PMEMED";
  public static final String CH_PKG_PHARMACY_MEDICINE = "PPHMED";
  public static final String CH_PKG_PHARMACY_CREDIT_MEDICINE = "PPHCMED";
  public static final String CH_PKG_MED_CONSUMABLE = "PCONMED";

  public static final String CH_PKG_INVENTORY_ITEM = "PINVITE";

  public static final String CH_PKG_OTHER = "POCOTC";
  public static final String CH_PKG_CONSUMABLE = "PCONOTC";
  public static final String CH_PKG_MISCELLANEOUS = "PMISOTC";
  public static final String CH_PKG_EQUIPMENT = "PEQUOTC";
  public static final String CH_PKG_IMPLANT = "PIMPOTC";


  public static final String CH_PKG_SERVICE = "PSERSNP";
  public static final String CH_PKG_BILL_DISCOUNTS = "PBIDIS";
  public static final String CH_PKG_PACKAGE = "PPKGPKG";
  public static final String CH_PKG_PHARMACY_RETURNS = "PPHRET";
  public static final String CH_PKG_PHARMACY_CREDIT_RETURNS = "PPHCRET";
  public static final String CH_PKG_INVENTORY_RETURNS = "PINVRET";
  public static final String CH_PKG_DIETARY = "PMDIE";

	public static final String CH_SERVICE_TAX = "STAX";
	public static final String CH_LUXURY_TAX = "LTAX";
	public static final String CH_VAT = "VTAX";


	public static final String CHARGE_STATUS_ACTIVE = "A";
	public static final String CHARGE_STATUS_CANCELLED = "X";

	public static final String CH_DYNA_PACKAGE_MARGIN = "MARPKG";
	public static final String CH_CLAIM_SERVICE_TAX = "CSTAX";
	public static final String CH_BILL_SERVICE_CHARGE = "BSTAX";
	public static final String CH_ROUND_OFF = "ROF";
	public static final String CH_DRG_BASE_MARGIN = "MARDRG";
	public static final String CH_DRG_OUTLIER = "OUTDRG";

	/*
	 * Default constructor with some usual defaults
	 */
	public ChargeDTO() {
		hasActivity = false;
		insuranceClaimAmount = BigDecimal.ZERO;
		postedDate = DateUtil.getCurrentTimestamp();
		modTime = DateUtil.getCurrentTimestamp();
		status = "A";
		isSystemDiscount = "Y";
	}

	/*
	 * Normally used constructor: this also automatically calculates the insurance claim amount
	 * based on various rules.
	 */
	public ChargeDTO (String group, String head,
			BigDecimal rate, BigDecimal qty, BigDecimal discount, String units,
			String descId, String desc, String deptId,
			boolean isInsurance, int planId, int serviceSubGroupId, int insuranceCategoryId,
			String visitType, String visitId, Boolean firstOfCategory)

		throws SQLException {

		this.chargeGroup = group;
		this.chargeHead = head;
		this.actRate = rate;
		this.originalRate = this.actRate;
		this.actQuantity = qty;
		this.actUnit = units;
		this.discount = discount;
		this.actDescriptionId = descId;
		this.actDescription = desc;
		this.actDepartmentId = deptId;
		this.serviceSubGroupId = serviceSubGroupId;
		this.insuranceCategoryId = insuranceCategoryId;
		this.visitType = visitType;
		this.visitId = visitId;
		this.isInsuranceBill = isInsurance;
		// some defaults
		this.status = "A";
		if (this.discount.compareTo(BigDecimal.ZERO) != 0) {
			this.overall_discount_amt = discount;
			this.overall_discount_auth = -1;
		}
		this.hasActivity = false;
		this.isSystemDiscount = "Y";
		postedDate = DateUtil.getCurrentTimestamp();
		modTime = DateUtil.getCurrentTimestamp();

		// calculated fields follow
		this.amount = (actRate.multiply(actQuantity)).subtract(discount);
		if (isInsurance) {
			BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",this.chargeHead);

			boolean isInsurancePayable = chrgbean.get("insurance_payable") != null &&
				((String)chrgbean.get("insurance_payable")).equals("Y") ;
			if (firstOfCategory!= null)
				this.firstOfCategory = firstOfCategory;
			if (isInsurancePayable) {
				setInsuranceAmtForPlan(planId, visitType, firstOfCategory);
			} else {
				this.insuranceClaimAmount = BigDecimal.ZERO;
			}
		} else {
			this.insuranceClaimAmount = BigDecimal.ZERO;
		}

		BasicDynaBean subgrpbean = new ServiceSubGroupDAO().findByKey("service_sub_group_id",
				this.serviceSubGroupId);

		if (subgrpbean != null) {
			this.eligible_to_redeem_points = (String)subgrpbean.get("eligible_to_redeem_points");
			this.redemption_cap_percent = (subgrpbean.get("redemption_cap_percent") != null)
								? (BigDecimal)subgrpbean.get("redemption_cap_percent") : this.redemption_cap_percent;
		}
	}

/* This constructor is used when multiple plans exists. Here first we are initializing a charge object by passing
 * plan id as 0. In this case insurance amount calculation is not happening in the constructor. Insurance amount
 * calculation is happening after object initialization.
 */

	public ChargeDTO (String group, String head,
			BigDecimal rate, BigDecimal qty, BigDecimal discount, String units,
			String descId, String desc, String deptId,
			boolean isInsurance, int serviceSubGroupId, int insuranceCategoryId,
			String visitType, String visitId, Boolean firstOfCategory) throws SQLException {

		this(group,head,rate,qty,discount,units,descId,desc,deptId,isInsurance,0,serviceSubGroupId,insuranceCategoryId,
				visitType,visitId,firstOfCategory);
	}


	public void setOrderAttributes(String chargeId, String billNo, String user,
			String remarks, String presDrId, Timestamp postedDate) {

		this.chargeId = chargeId;
		this.billNo = billNo;
		this.username = user;
		// this method will be called for new charges so its ok to keep old remarks which are been set
		this.userRemarks = this.userRemarks == null ? remarks:this.userRemarks;
		this.prescribingDrId = presDrId;
		this.postedDate = postedDate;
		this.modTime = DateUtil.getCurrentTimestamp();
	}

	public void setActivityDetails(String activityCode, int activityId, String activityConducted,
			Date conductedDateTime) {
		this.activityId = activityId;
		this.activityCode = activityCode;
		if (activityId != 0)
			this.hasActivity = true;
		else
			this.hasActivity = false;
		this.activityConducted = activityConducted;
		this.conductedDateTime = conductedDateTime;
	}

	public void setInsuranceAmt(int[] planIds, String visitType, Boolean isFirstOfCategory) throws SQLException {
		setInsuranceAmt(planIds, visitType, isFirstOfCategory, true);
	}

	public void setInsuranceAmt(int planIds[], String visitType, Boolean isfirstOfCategory, Boolean checkForFOC) throws SQLException {
		if (null != planIds && this.isInsuranceBill) {
			AdvanceInsuranceCalculator insCalculator = new AdvanceInsuranceCalculator();
			claimAmounts = new BigDecimal[planIds.length];
			sponsorTaxAmounts = new BigDecimal[planIds.length];
			//BigDecimal claimAmounts[] = new BigDecimal[planIds.length];
			this.firstOfCategory = getIsFirstOfCategory(isfirstOfCategory, visitId, checkForFOC);
			BigDecimal remainingAmount = this.amount;

			BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",this.chargeHead);
			boolean isInsurancePayable = chrgbean.get("insurance_payable") != null &&
			((String)chrgbean.get("insurance_payable")).equals("Y") ;

			if(!isInsurancePayable){
				this.insuranceClaimAmount = BigDecimal.ZERO;
				for (int i =0; i < planIds.length; i++) {
					claimAmounts[i] = BigDecimal.ZERO;
					sponsorTaxAmounts[i] = BigDecimal.ZERO;
				}
				return;
			}

			for (int i =0; i < planIds.length; i++) {
				// initInsuranceAmount();
				if (planIds[i] == 0) {
					claimAmounts[i] = this.amount;
					sponsorTaxAmounts[i] = BigDecimal.ZERO;
					continue;
				}
				BigDecimal discount =  i == 0 ? this.discount : BigDecimal.ZERO;
				claimAmounts[i] = insCalculator.calculateClaim(remainingAmount, discount, this.billNo, planIds[i],
							firstOfCategory, visitType, this.insuranceCategoryId, isInsurancePayable);
				sponsorTaxAmounts[i] = BigDecimal.ZERO;
				remainingAmount = remainingAmount.subtract(claimAmounts[i]);
			}

			if(null != claimAmounts && claimAmounts.length > 0 && null != claimAmounts[0])
				this.insuranceClaimAmount = claimAmounts[0]; // assuming first one is the primary plan
		}
	}

	public void setInsuranceAmtForPlan(int planId, String visitType, Boolean isfirstOfCategory) throws SQLException {
		setInsuranceAmtForPlan(planId, visitType,  isfirstOfCategory, true);
	}

	public void setInsuranceAmtForPlan(int planId, String visitType) throws SQLException {
		setInsuranceAmtForPlan(planId, visitType,  null, true);
	}

	public void setInsuranceAmtForPlan(int planId, String visitType, Boolean isfirstOfCategory, Boolean checkForFOC) throws SQLException {

		BigDecimal claimAmount = BigDecimal.ZERO;
		//initInsuranceAmount();
		BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",this.chargeHead);
		boolean isInsurancePayable = chrgbean.get("insurance_payable") != null &&
		((String)chrgbean.get("insurance_payable")).equals("Y") ;

		if(!isInsurancePayable){
			this.insuranceClaimAmount = BigDecimal.ZERO;
			return;
		}

		if (planId == 0) {
			this.insuranceClaimAmount = this.amount;
			return;
		}

		this.firstOfCategory = getIsFirstOfCategory(isfirstOfCategory, visitId, checkForFOC);

		claimAmount = getClaimAmountForPlan(this.amount, this.discount, this.billNo, planId,
					firstOfCategory, visitType, this.insuranceCategoryId);

		this.insuranceClaimAmount = claimAmount;
	}

	private Boolean getIsFirstOfCategory(Boolean isFirstOfCategory, String visitId, Boolean checkForFOC) throws SQLException {
		Boolean firstOfCategory = isFirstOfCategory == null? true : isFirstOfCategory;
		if(visitId!= null && !visitId.equals("") && checkForFOC) {
			if(firstOfCategory == true) {
				firstOfCategory = VisitDetailsDAO.getIsFirstOfCategory(visitId, this.insuranceCategoryId);
			}
		}
		return firstOfCategory;
	}

	private String getBillVisitType(String billNo) throws SQLException {
			// try and find out ourselves: but this is inefficient. Prefer the caller to pass it in.
			String visitType = null;
			if (billNo!=null && !billNo.equals("")) {
				visitType = BillDAO.getBillTypeAndVisitType(billNo).getVisitType();
			}
			return visitType;
	}

	private void initInsuranceAmount() throws SQLException {

		BasicDynaBean chrgbean = new ChargeHeadsDAO().findByKey("chargehead_id",this.chargeHead);
		boolean isInsurancePayable = chrgbean.get("insurance_payable") != null &&
		((String)chrgbean.get("insurance_payable")).equals("Y") ;

		if(!isInsurancePayable){
			this.insuranceClaimAmount = BigDecimal.ZERO;
			return;
		}

	}

	public BigDecimal getClaimAmountForPlan(BigDecimal amount, BigDecimal discount, String billNo, int planId, Boolean firstOfCategory,
			String visitType, int categoryId)throws SQLException {
		BigDecimal claimAmount = BigDecimal.ZERO;

		if (visitType == null || visitType.equals("")) {
			visitType = getBillVisitType(billNo);
		}

		BasicDynaBean planDetails = PlanDetailsDAO.getChargeAmtForPlan(planId,
				categoryId, visitType);

		// this means that there is no row in the plan details for this category.
		// Assume full amount paid by insurance
		if (null == planDetails) {
			return amount;
		}

		BigDecimal patAmt = (BigDecimal) planDetails.get("patient_amount");
		BigDecimal catPatAmt = (BigDecimal) planDetails.get("patient_amount_per_category");
		BigDecimal patPer = (BigDecimal) planDetails.get("patient_percent");
		BigDecimal patCap = (BigDecimal) planDetails.get("patient_amount_cap");
		BigDecimal copayPerc = (BigDecimal) planDetails.get("copay_percentage") != null
				? (BigDecimal) planDetails.get("copay_percentage") : BigDecimal.ZERO;

		BigDecimal chargeAmount = amount;
		boolean isNegativeAmt = false;
		if (chargeAmount.compareTo(BigDecimal.ZERO) < 0)
			isNegativeAmt = true;

		if (isNegativeAmt)
			chargeAmount = chargeAmount.negate();
		BigDecimal percentChargeAmount = ((String)planDetails.get("is_copay_pc_on_post_discnt_amt")).equals("Y")?chargeAmount: amount.add(discount);

		// Calculate patient co-pay
		BigDecimal coPay = ConversionUtils.setScale(
				copayPerc.multiply(percentChargeAmount).divide(new BigDecimal("100"),BigDecimal.ROUND_HALF_UP));

		// add fixed patient amount, cap it to charge amount
		BigDecimal patientAmount = coPay;

		if(firstOfCategory == true) {
			patientAmount = coPay.add(patAmt).add(catPatAmt).min(chargeAmount);
		} else {
			patientAmount = patientAmount.add(patAmt).min(chargeAmount);
		}
		// cap it to max patient amount
		if (patCap != null)
			patientAmount = patientAmount.min(patCap);

		//this.insuranceClaimAmount = chargeAmount.subtract(patientAmount);
		claimAmount =  chargeAmount.subtract(patientAmount);
		if (isNegativeAmt)
			claimAmount = claimAmount.negate();

		return claimAmount;
	}


	public void copyChargeAmountsFrom(ChargeDTO from, boolean setModTime) {
		this.setActRate(from.getActRate());
		this.setActQuantity(from.getActQuantity());
		this.setActUnit(from.getActUnit());
		this.setDiscount(from.getDiscount());
		this.setOverall_discount_amt(from.getDiscount());
		this.setAmount(from.getAmount());
		this.setInsuranceClaimAmount(from.getInsuranceClaimAmount());
		if (setModTime)
			this.setModTime(DateUtil.getCurrentTimestamp());
		this.setPayeeDoctorId(from.getPayeeDoctorId());
		this.setActivityConducted(from.getActivityConducted());
		this.setActDescription(from.getActDescription());
		this.setActDescriptionId(from.getActDescriptionId());
		this.setActRemarks(from.getActRemarks());
		this.setUsername(from.getUsername());
		if ((from.getChargeGroup().equals("BED")
				&& (from.getChargeHead().equals("BBED") || from.getChargeHead().equals("BYBED")))
				|| (from.getChargeGroup().equals("ICU") && from.getChargeHead().equals("BICU"))) {
			this.setActRatePlanItemCode(from.getActRatePlanItemCode());
		}
		
	}

	public void copyChargeAmountsWithoutDiscountFrom(ChargeDTO from, boolean setModTime) {
		this.setActRate(from.getActRate());
		this.setActQuantity(from.getActQuantity());
		this.setActUnit(from.getActUnit());
		//this.setDiscount(from.getDiscount());
		this.setAmount(from.getAmount());
		this.setInsuranceClaimAmount(from.getInsuranceClaimAmount());
		if (setModTime)
			this.setModTime(DateUtil.getCurrentTimestamp());
		this.setPayeeDoctorId(from.getPayeeDoctorId());
		this.setActivityConducted(from.getActivityConducted());
		this.setActDescription(from.getActDescription());
		this.setActDescriptionId(from.getActDescriptionId());
		this.setActRemarks(from.getActRemarks());
	}

	public int compareTo(ChargeDTO to){
		if( to != null && this.getActDescriptionId().equals(to.getActDescriptionId())
				&& to.getChargeHead().equals(this.getChargeHead())
					&& to.getActUnit().equals(this.getActUnit()))
			return 0;
		else
			return 1;
	}


	/*
	 * re-calculate the amount based on rate * qty. If discount is non-zero, will set
	 * discount auth to -1 (ie, rate-plan discount)
	 */
	public void recalcAmount() {
		this.amount = ConversionUtils.setScale(actRate.multiply(actQuantity).subtract(discount));
		if (this.discount.compareTo(BigDecimal.ZERO) != 0) {
			this.overall_discount_amt = discount;
			this.overall_discount_auth = -1;
		}
	}

    /*
     * Accessors
     */
    public String getChargeId() { return chargeId; }
    public void setChargeId(String chargeId) { this.chargeId = chargeId; }

	public String getBillNo() { return billNo; }
	public void setBillNo(String billNo) { this.billNo = billNo; }

	public String getChargeGroup() { return chargeGroup; }
	public void setChargeGroup(String chargeGroup) { this.chargeGroup = chargeGroup; }

	public String getChargeHead() { return chargeHead; }
	public void setChargeHead(String chargeHead) { this.chargeHead = chargeHead; }

	public String getActDepartmentId() { return actDepartmentId; }
	public void setActDepartmentId(String actDepartmentId) { this.actDepartmentId = actDepartmentId; }

	public String getActDescriptionId() { return actDescriptionId; }
	public void setActDescriptionId(String v) { actDescriptionId = v; }

	public String getActDescription() { return actDescription; }
	public void setActDescription(String actDescription) { this.actDescription = actDescription; }

	public String getActRemarks() { return actRemarks; }
	public void setActRemarks(String actRemarks) { this.actRemarks = actRemarks; }

	public BigDecimal getActRate() {
        return actRate;
    }
	public void setActRate(BigDecimal actRate) {
        this.actRate = actRate;
		if (originalRate.compareTo(BigDecimal.ZERO) == 0) {
			// also set the original rate: this is a good default, but do it
			// only if the original rate is not zero, meaning the caller has set it himself
			this.originalRate = actRate;
		}
    }
//	public void setActRate(float actRate) {
//        setActRate(new BigDecimal(actRate));
//    }

	public String getActUnit() { return actUnit; }
	public void setActUnit(String actUnit) { this.actUnit = actUnit; }

	public BigDecimal getActQuantity() { return actQuantity; }
	public void setActQuantity(BigDecimal actQuantity) { this.actQuantity = actQuantity; }
//	public void setActQuantity(float actQuantity) { setActQuantity(new BigDecimal(actQuantity)); }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

//	public void setAmount(float amount) { setAmount(new BigDecimal(amount)); }

	public BigDecimal getDiscount() { return discount; }
	public void setDiscount(BigDecimal discount) {
		this.discount = discount;
	}

//	public void setDiscount(float discount) throws SQLException {
//		this.discount = new BigDecimal(discount);
//	}

	public String getDiscountReason() { return discountReason; }
	public void setDiscountReason(String discountReason) { this.discountReason = discountReason; }

	public String getChargeRef() { return chargeRef; }
	public void setChargeRef(String chargeRef) { this.chargeRef = chargeRef; }

	public BigDecimal getOriginalRate() { return originalRate; }
	public void setOriginalRate(BigDecimal originalRate) { this.originalRate = originalRate; }
//	public void setOriginalRate(float originalRate) { this.originalRate = new BigDecimal(originalRate); }

	public Timestamp getPostedDate() { return postedDate; }
	public void setPostedDate(Timestamp postedDate) { this.postedDate = postedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

	public Timestamp getModTime() { return modTime; }
	public void setModTime(Timestamp modTime) { this.modTime = modTime; }

    public String getActDepartmentName() { return actDepartmentName; }
    public void setActDepartmentName(String actDepartmentName) { this.actDepartmentName = actDepartmentName; }

	public String getChargeGroupName() { return chargeGroupName; }
	public void setChargeGroupName(String v) { chargeGroupName = v; }

	public String getChargeHeadName() { return chargeHeadName; }
	public void setChargeHeadName(String v) { chargeHeadName = v; }

	public String getBillType() { return billType; }
	public void setBillType (String v) { billType = v; }

	public String getBillStatus() { return billStatus; }
	public void setBillStatus (String v) { billStatus = v; }

	public java.sql.Date getBillFinalizedDate() { return billFinalizedDate; }
	public void setBillFinalizedDate(java.sql.Date v) { billFinalizedDate = v; }

	public String getVisitId() { return visitId; }
	public void setVisitId (String v) { visitId = v; }

	public String getVisitType() { return visitType; }
	public void setVisitType (String v) { visitType = v; }

	public String getPatientName() { return patientName; }
	public void setPatientName (String v) { patientName = v; }

	public String getPatientLastName() { return patientLastName; }
	public void setPatientLastName (String v) { patientLastName = v; }

	public String getDoctorName() { return doctorName; }
	public void setDoctorName(String v) { doctorName = v; }

	public String getWardName() { return wardName; }
	public void setWardName(String v) { wardName = v; }

	public String getMrNo() { return mrNo; }
	public void setMrNo (String v) { mrNo = v; }

	public String getApprovalId() { return approvalId; }
	public void setApprovalId(String v) { approvalId = v; }

	public BigDecimal getPackageUnit() { return packageUnit; }
	public void setPackageUnit(BigDecimal v) { packageUnit = v; }

	public BigDecimal getDoctorAmount() { return doctorAmount; }
	public void setDoctorAmount(BigDecimal doctorAmount) {this.doctorAmount = doctorAmount;}

	public String getDocPaymentId() { return docPaymentId; }
	public void setDocPaymentId(String v) { docPaymentId = v; }

	public String getRefPaymentId() { return refPaymentId; }
	public void setRefPaymentId(String v) { refPaymentId = v; }

	public String getOhPaymentId() { return ohPaymentId; }
	public void setOhPaymentId(String v) { ohPaymentId = v; }

	public boolean getHasActivity() { return hasActivity; }
	public void setHasActivity(boolean hasActivity) { this.hasActivity = hasActivity; }

	public int getActivityId() { return activityId; }
	public void setActivityId(int v) { activityId = v; }

	public String getActivityCode() { return activityCode; }
	public void setActivityCode(String v) { activityCode = v; }

	public BigDecimal getActualAmount() { return actualAmount; }
	public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
//	public void setActualAmount(float actualAmount) { setActualAmount(new BigDecimal(actualAmount)); }

  	public BigDecimal getInsuranceClaimAmount() { return insuranceClaimAmount; }

	public void setInsuranceClaimAmount(BigDecimal v) {
		this.insuranceClaimAmount=v;
	}

	public BigDecimal getOrigInsuranceClaimAmount() {
		return origInsuranceClaimAmount;
	}

	public void setOrigInsuranceClaimAmount(BigDecimal origInsuranceClaimAmount) {
		this.origInsuranceClaimAmount = origInsuranceClaimAmount;
	}

	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getPayeeDoctorId() { return payeeDoctorId; }
	public void setPayeeDoctorId(String v) { payeeDoctorId = v; }


	public BigDecimal getReferalAmount() { return referalAmount; }
	public void setReferalAmount(BigDecimal v) { referalAmount = v; }

	public BigDecimal getOhAmount(){ return ohAmount; }
	public void setOhAmount(BigDecimal v) { ohAmount = v ; }

	public void setDiscountAndChangeAmount(BigDecimal discount){
		this.amount = this.amount.subtract(discount);
	}
	public int getConsultationToken() {
		return consultationToken;
	}
	public void setConsultationToken(int consultationToken) {
		this.consultationToken = consultationToken;
	}

	public BigDecimal getPrescribingDrAmount() { return prescribingDrAmount; }
	public void setPrescribingDrAmount(BigDecimal v) { prescribingDrAmount = v; }

	public String getPrescribingDrPaymentId() { return prescribingDrPaymentId; }
	public void setPrescribingDrPaymentId(String v) { prescribingDrPaymentId = v; }

	public String getPrescribingDrId() { return prescribingDrId; }
	public void setPrescribingDrId(String v) { prescribingDrId = v; }

	public int getDiscount_auth_dr() {
		return discount_auth_dr;
	}
	public void setDiscount_auth_dr(int discount_auth_dr) {
		this.discount_auth_dr = discount_auth_dr;
	}
	public int getDiscount_auth_hosp() {
		return discount_auth_hosp;
	}
	public void setDiscount_auth_hosp(int discount_auth_hosp) {
		this.discount_auth_hosp = discount_auth_hosp;
	}
	public int getDiscount_auth_pres_dr() {
		return discount_auth_pres_dr;
	}
	public void setDiscount_auth_pres_dr(int discount_auth_pres_dr) {
		this.discount_auth_pres_dr = discount_auth_pres_dr;
	}
	public int getDiscount_auth_ref() {
		return discount_auth_ref;
	}
	public void setDiscount_auth_ref(int discount_auth_ref) {
		this.discount_auth_ref = discount_auth_ref;
	}
	public int getOverall_discount_auth() {
		return overall_discount_auth;
	}
	public void setOverall_discount_auth(int overall_discount_auth) {
		this.overall_discount_auth = overall_discount_auth;
	}
	public void setOverall_discount_auth(String overall_discount_auth) {
		setOverall_discount_auth(new Integer(overall_discount_auth));
	}
	public BigDecimal getDr_discount_amt() {
		return dr_discount_amt;
	}
	public void setDr_discount_amt(BigDecimal dr_discount_amt) {
		this.dr_discount_amt = dr_discount_amt;
	}
	public BigDecimal getHosp_discount_amt() {
		return hosp_discount_amt;
	}
	public void setHosp_discount_amt(BigDecimal hosp_discount_amt) {
		this.hosp_discount_amt = hosp_discount_amt;
	}
	public BigDecimal getOverall_discount_amt() {
		return overall_discount_amt;
	}
	public void setOverall_discount_amt(BigDecimal overall_discount_amt) {
		this.overall_discount_amt = overall_discount_amt;
	}
	public BigDecimal getPres_dr_discount_amt() {
		return pres_dr_discount_amt;
	}
	public void setPres_dr_discount_amt(BigDecimal pres_dr_discount_amt) {
		this.pres_dr_discount_amt = pres_dr_discount_amt;
	}
	public BigDecimal getRef_discount_amt() {
		return ref_discount_amt;
	}
	public void setRef_discount_amt(BigDecimal ref_discount_amt) {
		this.ref_discount_amt = ref_discount_amt;
	}
	public String getDiscount_auth_dr_name() {
		return discount_auth_dr_name;
	}
	public void setDiscount_auth_dr_name(String discount_auth_dr_name) {
		this.discount_auth_dr_name = discount_auth_dr_name;
	}
	public String getDiscount_auth_hosp_name() {
		return discount_auth_hosp_name;
	}
	public void setDiscount_auth_hosp_name(String discount_auth_hosp_name) {
		this.discount_auth_hosp_name = discount_auth_hosp_name;
	}
	public String getDiscount_auth_pres_dr_name() {
		return discount_auth_pres_dr_name;
	}
	public void setDiscount_auth_pres_dr_name(String discount_auth_pres_dr_name) {
		this.discount_auth_pres_dr_name = discount_auth_pres_dr_name;
	}
	public String getDiscount_auth_ref_name() {
		return discount_auth_ref_name;
	}
	public void setDiscount_auth_ref_name(String discount_auth_ref_name) {
		this.discount_auth_ref_name = discount_auth_ref_name;
	}
	public Boolean getFirstOfCategory() {
		return firstOfCategory;
	}

	public void setFirstOfCategory(Boolean firstOfCategory) {
		this.firstOfCategory = firstOfCategory;
	}

	public String getOverall_discount_auth_name() {
		return overall_discount_auth_name;
	}
	public void setOverall_discount_auth_name(String overall_discount_auth_name) {
		this.overall_discount_auth_name = overall_discount_auth_name;
	}
	public String getInsurancePayable() {
		return insurancePayable;
	}
	public void setInsurancePayable(String insurancePayable) {
		this.insurancePayable = insurancePayable;
	}
	public String getInsuranceClaimTaxable() {
		return insuranceClaimTaxable;
	}
	public void setInsuranceClaimTaxable(String insuranceClaimTaxable) {
		this.insuranceClaimTaxable = insuranceClaimTaxable;
	}
	public String getChargeActivityId() {
		return chargeActivityId;
	}
	public void setChargeActivityId(String chargeActivityId) {
		this.chargeActivityId = chargeActivityId;
	}
	public int getAccount_group() {
		return account_group;
	}
	public void setAccount_group(int account_group) {
		this.account_group = account_group;
	}

	public String getActivityConducted() { return activityConducted; }
	public void setActivityConducted(String v) { activityConducted = v; }

	public Date getConductedDateTime() { return conductedDateTime; }
	public void setConductedDateTime(Date v) { conductedDateTime = v; }

	public String getActItemCode() { return actItemCode; }
	public void setActItemCode(String v) { actItemCode = v; }

	public String getActRatePlanItemCode() { return actRatePlanItemCode; }
	public void setActRatePlanItemCode(String v) { actRatePlanItemCode = v; }

	public boolean isAllowDiscount() {
		return allowDiscount;
	}

	public void setAllowDiscount(boolean allowDiscount) {
		this.allowDiscount = allowDiscount;
	}

	public boolean isAllowRateVariation() {
		return allowRateVariation;
	}

	public void setAllowRateVariation(boolean allowRateVariation) {
		this.allowRateVariation = allowRateVariation;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getUserRemarks() { return userRemarks; }
	public void setUserRemarks(String v) { userRemarks = v; }

	public int getInsuranceCategoryId() {
		return insuranceCategoryId;
	}

	public void setInsuranceCategoryId(int insuranceCategoryId) {
		this.insuranceCategoryId = insuranceCategoryId;
	}

	public String getConducting_doc_mandatory() {
		return conducting_doc_mandatory;
	}

	public void setConducting_doc_mandatory(String conducting_doc_mandatory) {
		this.conducting_doc_mandatory = conducting_doc_mandatory;
	}

	public String getChargeExcluded() {
		return chargeExcluded;
	}

	public void setChargeExcluded(String chargeExcluded) {
		this.chargeExcluded = chargeExcluded;
	}

	public String getPackageFinalized() {
		return packageFinalized;
	}

	public void setPackageFinalized(String packageFinalized) {
		this.packageFinalized = packageFinalized;
	}

	public int getConsultation_type_id() {
		return consultation_type_id;
	}

	public void setConsultation_type_id(int consultation_type_id) {
		this.consultation_type_id = consultation_type_id;
	}

	public String getOp_id() {
		return op_id;
	}

	public void setOp_id(String op_id) {
		this.op_id = op_id;
	}

	public Timestamp getFrom_date() {
		return from_date;
	}

	public void setFrom_date(Timestamp from_date) {
		this.from_date = from_date;
	}

	public Timestamp getTo_date() {
		return to_date;
	}

	public void setTo_date(Timestamp to_date) {
		this.to_date = to_date;
	}

	public String getCodeType() {
		return codeType;
	}

	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	public String getServiceGroupName() {
		return serviceGroupName;
	}

	public void setServiceGroupName(String serviceGroupName) {
		this.serviceGroupName = serviceGroupName;
	}

	public String getServiceSubGroupName() {
		return serviceSubGroupName;
	}

	public void setServiceSubGroupName(String serviceSubGroupName) {
		this.serviceSubGroupName = serviceSubGroupName;
	}

	public int getServiceGroupId() {
		return serviceGroupId;
	}

	public void setServiceGroupId(int serviceGroupId) {
		this.serviceGroupId = serviceGroupId;
	}

	public Integer getServiceSubGroupId() {
		return serviceSubGroupId;
	}

	public void setServiceSubGroupId(Integer serviceSubGroupId) {
		this.serviceSubGroupId = serviceSubGroupId;
	}

	public String getPreAuthId() {
		return preAuthId;
	}

	public void setPreAuthId(String preAuthId) {
		this.preAuthId = preAuthId;
	}

	public boolean getConduction_required() {
		return conduction_required;
	}

	public void setConduction_required(boolean conduction_required) {
		this.conduction_required = conduction_required;
	}

	public BigDecimal getReturnAmt() {
		return returnAmt;
	}

	public void setReturnAmt(BigDecimal returnAmt) {
		this.returnAmt = returnAmt;
	}

	public BigDecimal getReturnInsuranceClaimAmt() {
		return returnInsuranceClaimAmt;
	}

	public void setReturnInsuranceClaimAmt(BigDecimal returnInsuranceClaimAmt) {
		this.returnInsuranceClaimAmt = returnInsuranceClaimAmt;
	}

	public BigDecimal getReturnQty() {
		return returnQty;
	}

	public void setReturnQty(BigDecimal returnQty) {
		this.returnQty = returnQty;
	}

	public Integer getPreAuthModeId() {
		return preAuthModeId;
	}

	public void setPreAuthModeId(Integer preAuthModeId) {
		this.preAuthModeId = preAuthModeId;
	}

	public String getItemRemarks() {
		return itemRemarks;
	}

	public void setItemRemarks(String itemRemarks) {
		this.itemRemarks = itemRemarks;
	}

	public boolean isAllowRateDecrease() {
		return allowRateDecrease;
	}

	public void setAllowRateDecrease(boolean allowRateDecrease) {
		this.allowRateDecrease = allowRateDecrease;
	}

	public boolean isAllowRateIncrease() {
		return allowRateIncrease;
	}

	public void setAllowRateIncrease(boolean allowRateIncrease) {
		this.allowRateIncrease = allowRateIncrease;
	}

	public BigDecimal getClaimRecdAmount() {
		return claimRecdAmount;
	}

	public void setClaimRecdAmount(BigDecimal claimRecdAmount) {
		this.claimRecdAmount = claimRecdAmount;
	}

	public String getClaimStatus() {
		return claimStatus;
	}

	public void setClaimStatus(String claimStatus) {
		this.claimStatus = claimStatus;
	}

	public String getEligible_to_redeem_points() {
		return eligible_to_redeem_points;
	}

	public void setEligible_to_redeem_points(String eligible_to_redeem_points) {
		this.eligible_to_redeem_points = eligible_to_redeem_points;
	}

	public BigDecimal getRedemption_cap_percent() {
		return redemption_cap_percent;
	}

	public void setRedemption_cap_percent(BigDecimal redemption_cap_percent) {
		this.redemption_cap_percent = redemption_cap_percent;
	}

	public int getRedeemed_points() {
		return redeemed_points;
	}

	public void setRedeemed_points(int redeemed_points) {
		this.redeemed_points = redeemed_points;
	}

	public BigDecimal getAmount_included() {
		return amount_included;
	}

	public void setAmount_included(BigDecimal amount_included) {
		this.amount_included = amount_included;
	}

	public BigDecimal getQty_included() {
		return qty_included;
	}

	public void setQty_included(BigDecimal qty_included) {
		this.qty_included = qty_included;
	}

	public String getServiceChrgApplicable() {
		return serviceChrgApplicable;
	}

	public void setServiceChrgApplicable(String serviceChrgApplicable) {
		this.serviceChrgApplicable = serviceChrgApplicable;
	}

	public BigDecimal[] getClaimAmounts() {
		return claimAmounts;
	}

	public void setClaimAmounts(BigDecimal[] claimAmounts) {
		this.claimAmounts = claimAmounts;
	}

	public String[] getPreAuthIds() {
		return preAuthIds;
	}

	public void setPreAuthIds(String[] preAuthIds) {
		this.preAuthIds = preAuthIds;
	}

	public Integer[] getPreAuthModeIds() {
		return preAuthModeIds;
	}

	public void setPreAuthModeIds(Integer[] preAuthModeIds) {
		this.preAuthModeIds = preAuthModeIds;
	}

	public boolean isInsuranceBill() {
		return isInsuranceBill;
	}

	public void setInsuranceBill(boolean isInsuranceBill) {
		this.isInsuranceBill = isInsuranceBill;
	}

	public Integer getSurgeryAnesthesiaDetailsId() {
		return surgeryAnesthesiaDetailsId;
	}

	public void setSurgeryAnesthesiaDetailsId(Integer surgeryAnesthesiaDetailsId) {
		this.surgeryAnesthesiaDetailsId = surgeryAnesthesiaDetailsId;
	}

	public Boolean getIsClaimLocked() {
		return isClaimLocked;
	}

	public void setIsClaimLocked(Boolean isClaimLocked) {
		this.isClaimLocked = isClaimLocked;
	}
	
	public BigDecimal getTaxAmt() {
		return taxAmt;
	}

	public void setTaxAmt(BigDecimal taxAmt) {
		this.taxAmt = taxAmt;
	}

	public BigDecimal[] getSponsorTaxAmounts() {
		return sponsorTaxAmounts;
	}

	public void setSponsorTaxAmounts(BigDecimal[] sponsorTaxAmounts) {
		this.sponsorTaxAmounts = sponsorTaxAmounts;
	}

	public BigDecimal getSponsorTaxAmt() {
		return sponsorTaxAmt;
	}

	public void setSponsorTaxAmt(BigDecimal sponsorTaxAmt) {
		this.sponsorTaxAmt = sponsorTaxAmt;
	}

	public BigDecimal getPriTaxAmt() {
		return priTaxAmt;
	}

	public void setPriTaxAmt(BigDecimal priTaxAmt) {
		this.priTaxAmt = priTaxAmt;
	}

	public BigDecimal getSecTaxAmt() {
		return secTaxAmt;
	}

	public void setSecTaxAmt(BigDecimal secTaxAmt) {
		this.secTaxAmt = secTaxAmt;
	}

	public BigDecimal getReturnTaxAmt() {
		return returnTaxAmt;
	}

	public void setReturnTaxAmt(BigDecimal returnTaxAmt) {
		this.returnTaxAmt = returnTaxAmt;
	}

	public BigDecimal getOriginalTaxAmt() {
		return originalTaxAmt;
	}

	public void setOriginalTaxAmt(BigDecimal originalTaxAmt) {
		this.originalTaxAmt = originalTaxAmt;
	}

	public BigDecimal getReturnOriginalTaxAmt() {
	  return returnOriginalTaxAmt;
	}
	
	public void setReturnOriginalTaxAmt(BigDecimal returnOriginalTaxAmt) {
	  this.returnOriginalTaxAmt = returnOriginalTaxAmt;
	}

	public boolean isAllowZeroClaim() {
		return allowZeroClaim;
	}

	public void setAllowZeroClaim(boolean allowZeroClaim) {
		this.allowZeroClaim = allowZeroClaim;
	}

	public String getBedType() {
		return bedType;
	}

	public void setBedType(String bedType) {
		this.bedType = bedType;
	}
	public Boolean getItemExcludedFromDoctor() {
		return itemExcludedFromDoctor;
	}

	public void setItemExcludedFromDoctor(Boolean itemExcludedFromDoctor) {
		this.itemExcludedFromDoctor = itemExcludedFromDoctor;
	}
	
  public String getIsSystemDiscount() {
    return isSystemDiscount;
  }

  public void setIsSystemDiscount(String isSystemDiscount) {
    this.isSystemDiscount = isSystemDiscount;
  }
	
	public Integer getBillingGroupId() {
		return this.billingGroupId;
	}

	public void setBillingGroupId(Integer billingGroupId) {
		this.billingGroupId = billingGroupId;
	}

	public String getRevenueDepartmentId() {
		return this.revenueDepartmentId;
	}

	public void setRevenueDepartmentId(String revenueDepartmentId) {
		this.revenueDepartmentId = revenueDepartmentId;
	}

	public String getDoctorExclusionRemarks() {
		return doctorExclusionRemarks;
	}

	public void setDoctorExclusionRemarks(String doctorExclusionRemarks) {
		this.doctorExclusionRemarks = doctorExclusionRemarks;
	}

  public Integer getPackageId() {
    return packageId;
  }

  public void setPackageId(Integer packageId) {
    this.packageId = packageId;
  }
  
  public String getBillDisplayType(){
    return this.billDisplayType;
  }
  
  public void setBillDisplayType(String billDisplayType){
    this.billDisplayType = billDisplayType;
  }
  public String getSubmissionBatchType(){
	    return this.submissionBatchType;
  }
  public void setSubmissionBatchType(String submissionBatchType){
	    this.submissionBatchType = submissionBatchType;
  }
  public Integer getPanelId(){
	    return this.panelId;
  }
  public void setPanelId(Integer panelId){
	    this.panelId = panelId;
  }
	public String getDynaPackageExcluded() {
		return dynaPackageExcluded;
	}

	public void setDynaPackageExcluded(String dynaPackageExcluded) {
		this.dynaPackageExcluded = dynaPackageExcluded;
	}
}

