/**
 *
 */
package com.insta.hms.master.GenericPreferences;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * @author krishna.t
 *
 */
public class GenericPreferencesDTO {
	private String hospMailID;
	private String protocol;
	private String hostName;
	private String portNo;
	private String authRequired;
	private String mailUserName;
	private String mailPassword;
	private String stockNegativeSale;
	private String pharmaAllowCpSale;
	private String pharmaAutoRoundOff;
	private String pharmacySeperateCreditbill;
	private String pharmacyValidateCostPrice;
	private String pharmacySaleMargin;
	private String seWithPO;
	private String recTransIndent;
	private String consumableStockNegative;
	private String poroundoff;

	public String getPoroundoff() {
		return poroundoff;
	}
	public void setPoroundoff(String poroundoff) {
		this.poroundoff = poroundoff;
	}
	private String userNameInBillPrint;
	private String allowBillReopen;

	private String sampleFlowRequired;
	private String autoSampleIdRequired;
	private String sampleNoBase;
	private String sampleCollectionPrintType;
	private String sampleassertion;
	private String independentGenerationOfSampleId;
	private String show_tests_in_emr;
	private String hospitalName;
	private String hospitalAddress;

	private String deposit_avalibility;
	
	private int pbmPriceThreshold;

	private String billLaterPrintDefault;
	private String billNowPrintDefault;
	private String receiptRefundPrintDefault;
	private String depositReceiptRefundPrintDefault;
	private String luxTax;
	private String depositChequeRealizationFlow;
	private String serviceTaxOnClaimAmount;
	private String serviceChargePercent;

	private String saleOfExpiredItems;
	private String warnForExpiry;

	private BigDecimal tdsAmount= new BigDecimal(0.0);
	//private String invVatType;
	private String domainName;
	private String pharmaReturnRestricted;
	private String hdrugAlert;
	private String pharmacyPatientType;

	private String doctorCustomField1;
	private String doctorCustomField2;
	private String doctorCustomField3;
	private String doctorCustomField4;
	private String doctorCustomField5;

	private String hospitalTin;
	private String hospitalPan;
	private String hospitalServiceRegnNo;
	private String prescribingDoctorRequired;

	private String default_prescribe_doctor;
	private String showHospAmtsOnPharmaSales;
	private String qtyDefaultToIssueUnit;
	private String allowdecimalsforqty;
	private String forceRemarksForPoItemReject;
	private String returnAgainstSpecificSupplier;
	private String salesReturnsPrintType;
	private String showCED;
	private String showVAT;
	private String showCESS;
	private String barcodeForItem;
	private String allowDecimalsInQtyForIssues;
	private String poToBeValidated;

	private String currencySymbol;
	private String whole;
	private String decimal;
	private String currencyFormat;
	private String fixedOtCharges;
	private String hijriCalendar;
	private String isMobileValidate;
	private String mobileStartPattern;
	private String mobileLengthPattern;

	private String allowIndentBasedIssue;

	private String forceSubGroupSelection;
	private String operationApplicableFor;
	private String defBillLaterCreditSales;
	private Date goLiveDate;
	private String default_printer_for_bill_later;
	private String default_printer_for_bill_now;
	private String default_prescription_print_template;
	private String default_prescription_web_template;
	private String default_prescription_web_printer;
	private String default_consultation_print_template;
	private String default_dental_cons_print_template;
	private String default_emr_print_template;
	private String default_po_print_template;
	private int uploadLimitInMB;
	private String salesPrintItems;
	private String validateDiagnosisCodification;
	private String hospUsesDynamicAddress; // hospital uses dynamic addresses.
	private String serviceNameRequired; //Preference For Service Scheduler.
	private String surgeryNameRequired; //Preference For Service Scheduler.
	private String schedulerGenerateOrder;

	//Test Results Html Color Codes Variable
	private String testNormalResult;
	private String testAbnormalResult;
	private String testCriticalResult;
	private String testImprobableResult;
	private String default_voucher_print;

	private String allowBillNowInsurance;

	private int decimalDigits;

	private BigDecimal poApprovalLimit = new BigDecimal(0.0);
	private String returnValidDays;

	private String issueUOM;
	private String packageUOM;
	private BigDecimal packageSize = new BigDecimal(1);

	private int max_centers_inc_default; // max centers allowed for hospital including default center
	private int max_active_hosp_users; // max hospital users allowed to create.

	private String use_smart_card;

	private String gen_token_for_lab;
	private String gen_token_for_rad;
	private String autoCloseNoChargeOpBills;
	private String autoCloseVisits;

	private String billingBedTypeForOP;

	private BigDecimal auto_close_claims_with_difference;

	private int points_earning_points;
	private BigDecimal points_earning_amt;
	private BigDecimal points_redemption_rate;
	private String allowConstantConsumableQtyIncrease; // Preference For allowing consumables qty greater than master qty.
	private String indent_approval_by;
	private String issuetodeptonly;
	private String allow_cross_center_indents;
	private int fin_year_end_month;
	private int fin_year_start_month;
	private int finYearEndMonth;
	private String[] diag_images;
	private String billcancellationrequiresapproval;
	private String aggregate_amt_on_remittance;
	private int bloodExpiry;
	private String corporate_insurance;
	private String op_one_presc_doc;
	private String enable_force_selection_for_mrno_search;

	private String emr_url_date;
	private String procurement_tax_label;
	private String restrictInactiveIpVisit;
	private String expiredItemsProcurement;
	private int procurementExpiryDays;
	private String stock_entry_agnst_do;
	private String check_insu_card_exp_in_sales;
	private String auto_mail_po_to_sup; 
	private String diag_report_print_center;
	private String is_return_against_grnno;
	private String apply_supplier_tax_rules;
	private String separator_type;
	private String currency_format;
	private int no_of_credit_debit_card_digits; 
	
	private int emailBillPrint;
	private String emailBillNowTemplate;
	private String emailBillLaterTemplate;
	private String mod_username;
	private String default_grn_print_template;
	private String billLabelForBillLaterBills;
	private String incomeTaxCashLimitApplicability;
	private String enablePatientDepositAvailability;
	private String billPendingValidationActivityTypes;
	private int raAutoProcessLastNumberOfDays;
	private String applyCpValidationForPo;
	
	public String getBillPendingValidationActivityTypes() {
    return billPendingValidationActivityTypes;
  }
  public void setBillPendingValidationActivityTypes(String billPendingValidationActivityTypes) {
    this.billPendingValidationActivityTypes = billPendingValidationActivityTypes;
  }
  public String getDefault_grn_print_template() {
		return default_grn_print_template;
	}
	public void setDefault_grn_print_template(String default_grn_print_template) {
		this.default_grn_print_template = default_grn_print_template;
	}
	public int getNo_of_credit_debit_card_digits() {
		return no_of_credit_debit_card_digits;
	}
	public void setNo_of_credit_debit_card_digits(int no_of_credit_debit_card_digits) {
		this.no_of_credit_debit_card_digits = no_of_credit_debit_card_digits;
	}
	public String getCurrency_format() {
		return currency_format;
	}
	public void setCurrency_format(String currency_format) {
		this.currency_format = currency_format;
	}
	public String getSeparator_type() {
		return separator_type;
	}
	public void setSeparator_type(String separator_type) {
		this.separator_type = separator_type;
	}
	public String getApply_supplier_tax_rules() {
		return apply_supplier_tax_rules;
	}
	public void setApply_supplier_tax_rules(String apply_supplier_tax_rules) {
		this.apply_supplier_tax_rules = apply_supplier_tax_rules;
	}
	
	public String getIs_return_against_grnno() {
		return is_return_against_grnno;
	}
	public void setIs_return_against_grnno(String is_return_against_grnno) {
		this.is_return_against_grnno = is_return_against_grnno;
	}
	public String getDiag_report_print_center() {
		return diag_report_print_center;
	}
	public void setDiag_report_print_center(String diag_report_print_center) {
		this.diag_report_print_center = diag_report_print_center;
	}
	public String getAuto_mail_po_to_sup() {
		return auto_mail_po_to_sup;
	}
	public void setAuto_mail_po_to_sup(String auto_mail_po_to_sup) {
		this.auto_mail_po_to_sup = auto_mail_po_to_sup;
	}
	public String getExpiredItemsProcurement() {
		return expiredItemsProcurement;
	}
	public void setExpiredItemsProcurement(String expiredItemsProcurement) {
		this.expiredItemsProcurement = expiredItemsProcurement;
	}
	public int getProcurementExpiryDays() {
		return procurementExpiryDays;
	}
	public void setProcurementExpiryDays(int procurementExpiryDays) {
		this.procurementExpiryDays = procurementExpiryDays;
	}
	
	public String getSchedulerGenerateOrder() {
		return schedulerGenerateOrder;
	}
	public void setSchedulerGenerateOrder(String schedulerGenerateOrder) {
		this.schedulerGenerateOrder = schedulerGenerateOrder;
	}

	public String getRestrictInactiveIpVisit() {
		return restrictInactiveIpVisit;
	}
	public void setRestrictInactiveIpVisit(String restrictInactiveIpVisit) {
		this.restrictInactiveIpVisit = restrictInactiveIpVisit;
	}
	public String getProcurement_tax_label() {
		return procurement_tax_label;
	}
	public void setProcurement_tax_label(String procurement_tax_label) {
		this.procurement_tax_label = procurement_tax_label;
	}
	public String getOp_one_presc_doc() {
		return op_one_presc_doc;
	}
	public void setOp_one_presc_doc(String op_one_presc_doc) {
		this.op_one_presc_doc = op_one_presc_doc;
	}
	public String[] getDiag_images() {
		return diag_images;
	}
	public void setDiag_images(String[] diag_images) {
		this.diag_images = diag_images;
	}
	public String getAllow_cross_center_indents() {
		return allow_cross_center_indents;
	}
	public void setAllow_cross_center_indents(String allow_cross_center_indents) {
		this.allow_cross_center_indents = allow_cross_center_indents;
	}
	public String getIssuetodeptonly() {
		return issuetodeptonly;
	}
	public void setIssuetodeptonly(String issuetodeptonly) {
		this.issuetodeptonly = issuetodeptonly;
	}
	public String getIndent_approval_by() {
		return indent_approval_by;
	}
	public void setIndent_approval_by(String indent_approval_by) {
		this.indent_approval_by = indent_approval_by;
	}

	public String getAllowConstantConsumableQtyIncrease() {
		return allowConstantConsumableQtyIncrease;
	}
	public void setAllowConstantConsumableQtyIncrease(
			String allowConstantConsumableQtyIncrease) {
		this.allowConstantConsumableQtyIncrease = allowConstantConsumableQtyIncrease;
	}
	public String getAutoCloseNoChargeOpBills() {
		return autoCloseNoChargeOpBills;
	}
	public void setAutoCloseNoChargeOpBills(String autoCloseNoChargeOpBills) {
		this.autoCloseNoChargeOpBills = autoCloseNoChargeOpBills;
	}
	public String getAutoCloseVisits() {
		return autoCloseVisits;
	}
	public void setAutoCloseVisits(String autoCloseVisits) {
		this.autoCloseVisits = autoCloseVisits;
	}
	public String getIssueUOM() {
		return issueUOM;
	}
	public void setIssueUOM(String issueUOM) {
		this.issueUOM = issueUOM;
	}
	public BigDecimal getPackageSize() {
		return packageSize;
	}
	public void setPackageSize(BigDecimal packageSize) {
		this.packageSize = packageSize;
	}
	public String getPackageUOM() {
		return packageUOM;
	}
	public void setPackageUOM(String packageUOM) {
		this.packageUOM = packageUOM;
	}
	public BigDecimal getPoApprovalLimit() {
		return poApprovalLimit;
	}
	public void setPoApprovalLimit(BigDecimal poApprovalLimit) {
		this.poApprovalLimit = poApprovalLimit;
	}
	public String getDefault_printer_for_bill_later() {
		return default_printer_for_bill_later;
	}
	public void setDefault_printer_for_bill_later(
			String default_printer_for_bill_later) {
		this.default_printer_for_bill_later = default_printer_for_bill_later;
	}
	public String getDefault_printer_for_bill_now() {
		return default_printer_for_bill_now;
	}
	public void setDefault_printer_for_bill_now(String default_printer_for_bill_now) {
		this.default_printer_for_bill_now = default_printer_for_bill_now;
	}
	public String getOperationApplicableFor() {
		return operationApplicableFor;
	}
	public void setOperationApplicableFor(String operationApplicableFor) {
		this.operationApplicableFor = operationApplicableFor;
	}

	public String getForceSubGroupSelection() {
		return forceSubGroupSelection;
	}
	public void setForceSubGroupSelection(String forceSubGroupSelection) {
		this.forceSubGroupSelection = forceSubGroupSelection;
	}
	public String getAllowdecimalsforqty() {
		return allowdecimalsforqty;
	}
	public void setAllowdecimalsforqty(String allowdecimalsforqty) {
		this.allowdecimalsforqty = allowdecimalsforqty;
	}
	public String getQtyDefaultToIssueUnit() {
		return qtyDefaultToIssueUnit;
	}
	public void setQtyDefaultToIssueUnit(String qtyDefaultToIssueUnit) {
		this.qtyDefaultToIssueUnit = qtyDefaultToIssueUnit;
	}
	public String getDefault_prescribe_doctor() {
		return default_prescribe_doctor;
	}
	public void setDefault_prescribe_doctor(String default_prescribe_doctor) {
		this.default_prescribe_doctor = default_prescribe_doctor;
	}
	public String getPharmacyPatientType() {
		return pharmacyPatientType;
	}
	public void setPharmacyPatientType(String pharmacyPatientType) {
		this.pharmacyPatientType = pharmacyPatientType;
	}
	public String getHdrugAlert() {
		return hdrugAlert;
	}
	public void setHdrugAlert(String hdrugAlert) {
		this.hdrugAlert = hdrugAlert;
	}
	public String getPharmaReturnRestricted() {
		return pharmaReturnRestricted;
	}
	public void setPharmaReturnRestricted(String pharmaReturnRestricted) {
		this.pharmaReturnRestricted = pharmaReturnRestricted;
	}
	/**
	 * @return the authRequired
	 */
	public String getAuthRequired() {
		return authRequired;
	}
	/**
	 * @param authRequired the authRequired to set
	 */
	public void setAuthRequired(String authRequired) {
		this.authRequired = authRequired;
	}
	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}
	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the portNo
	 */
	public String getPortNo() {
		return portNo;
	}
	/**
	 * @param portNo the portNo to set
	 */
	public void setPortNo(String portNo) {
		this.portNo = portNo;
	}
	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}
	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the hospMailID
	 */
	public String getHospMailID() {
		return hospMailID;
	}
	/**
	 * @param hospMailID the hospMailID to set
	 */
	public void setHospMailID(String hospMailID) {
		this.hospMailID = hospMailID;
	}
	/**
	 * @return the mailPassword
	 */
	public String getMailPassword() {
		return mailPassword;
	}
	/**
	 * @param mailPassword the mailPassword to set
	 */
	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}
	/**
	 * @return the mailUserName
	 */
	public String getMailUserName() {
		return mailUserName;
	}
	/**
	 * @param mailUserName the mailUserName to set
	 */
	public void setMailUserName(String mailUserName) {
		this.mailUserName = mailUserName;
	}
	/**
	 * @return the stockNegativeSale
	 */
	public String getStockNegativeSale() {
		return stockNegativeSale;
	}
	/**
	 * @param stockNegativeSale the stockNegativeSale to set
	 */
	public void setStockNegativeSale(String stockNegativeSale) {
		this.stockNegativeSale = stockNegativeSale;
	}

	public String getPharmaAllowCpSale() { return pharmaAllowCpSale; }
	public void setPharmaAllowCpSale(String v) { pharmaAllowCpSale = v; }

	public String getPharmaAutoRoundOff() { return pharmaAutoRoundOff; }
	public void setPharmaAutoRoundOff(String v) { pharmaAutoRoundOff = v; }
	public String getPharmacySeperateCreditbill() {
		return pharmacySeperateCreditbill;
	}
	public void setPharmacySeperateCreditbill(String pharmacySeperateCreditbill) {
		this.pharmacySeperateCreditbill = pharmacySeperateCreditbill;
	}
	public BigDecimal getTdsAmount() { return tdsAmount; }
	public void setTdsAmount(BigDecimal v) { tdsAmount = v; }
	public String getAutoSampleIdRequired() {
		return autoSampleIdRequired;
	}

	public void setAutoSampleIdRequired(String autoSampleIdRequired) {
		this.autoSampleIdRequired = autoSampleIdRequired;
	}
	public String getSampleFlowRequired() {
		return sampleFlowRequired;
	}
	public void setSampleFlowRequired(String sampleFlowRequired) {
		this.sampleFlowRequired = sampleFlowRequired;
	}

	public String getBillLaterPrintDefault() { return billLaterPrintDefault; }
	public void setBillLaterPrintDefault(String v) { billLaterPrintDefault = v; }

	public String getBillNowPrintDefault() { return billNowPrintDefault; }
	public void setBillNowPrintDefault(String v) { billNowPrintDefault = v; }

	public String getReceiptRefundPrintDefault() { return receiptRefundPrintDefault; }
	public void setReceiptRefundPrintDefault(String v) { receiptRefundPrintDefault = v; }

	public String getHospitalName() {
		return hospitalName;
	}
	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}
	public String getSaleOfExpiredItems() {
		return saleOfExpiredItems;
	}
	public void setSaleOfExpiredItems(String saleOfExpiredItems) {
		this.saleOfExpiredItems = saleOfExpiredItems;
	}
	public String getWarnForExpiry() {
		return warnForExpiry;
	}
	public void setWarnForExpiry(String warnForExpiry) {
		this.warnForExpiry = warnForExpiry;
	}
	public void setDomainName(String domain) {
		this.domainName = domain;
	}
	public String getDomainName() {
		return domainName;
	}
	public String getUserNameInBillPrint() {
		return userNameInBillPrint;
	}
	public void setUserNameInBillPrint(String userNameInBillPrint) {
		this.userNameInBillPrint = userNameInBillPrint;
	}
	
	public String getAllowBillReopen() {
		return allowBillReopen;
	}
	public void setAllowBillReopen(String allowBillReopen) {
		this.allowBillReopen = allowBillReopen;
	}
	
	public String getLuxTax() {
		return luxTax;
	}
	public void setLuxTax(String luxTax) {
		this.luxTax = luxTax;
	}
	public String getDepositChequeRealizationFlow() {
		return depositChequeRealizationFlow;
	}
	public void setDepositChequeRealizationFlow(String depositChequeRealizationFlow) {
		this.depositChequeRealizationFlow = depositChequeRealizationFlow;
	}
	public String getdoctorCustomField1() {
		return doctorCustomField1;
	}
	public void setdoctorCustomField1(String doctorCustomField1) {
		this.doctorCustomField1 = doctorCustomField1;
	}
	public String getdoctorCustomField2() {
		return doctorCustomField2;
	}
	public void setdoctorCustomField2(String doctorCustomField2) {
		this.doctorCustomField2 = doctorCustomField2;
	}
	public String getdoctorCustomField3() {
		return doctorCustomField3;
	}
	public void setdoctorCustomField3(String doctorCustomField3) {
		this.doctorCustomField3 = doctorCustomField3;
	}
	public String getdoctorCustomField4() {
		return doctorCustomField4;
	}
	public void setdoctorCustomField4(String doctorCustomField4) {
		this.doctorCustomField4 = doctorCustomField4;
	}
	public String getdoctorCustomField5() {
		return doctorCustomField5;
	}
	public void setdoctorCustomField5(String doctorCustomField5) {
		this.doctorCustomField5 = doctorCustomField5;
	}
	public String getPharmacySaleMargin() {
		return pharmacySaleMargin;
	}
	public void setPharmacySaleMargin(String pharmacySaleMargin) {
		this.pharmacySaleMargin = pharmacySaleMargin;
	}
	public String getPharmacyValidateCostPrice() {
		return pharmacyValidateCostPrice;
	}
	public void setPharmacyValidateCostPrice(String pharmacyValidateCostPrice) {
		this.pharmacyValidateCostPrice = pharmacyValidateCostPrice;
	}
	public String getSampleCollectionPrintType() {
		return sampleCollectionPrintType;
	}
	public void setSampleCollectionPrintType(String sampleCollectionPrintType) {
		this.sampleCollectionPrintType = sampleCollectionPrintType;
	}
	public String getServiceTaxOnClaimAmount() {
		return serviceTaxOnClaimAmount;
	}
	public void setServiceTaxOnClaimAmount(String serviceTaxOnClaimAmount) {
		this.serviceTaxOnClaimAmount = serviceTaxOnClaimAmount;
	}
	public String getDeposit_avalibility() {
		return deposit_avalibility;
	}
	public void setDeposit_avalibility(String deposit_avalibility) {
		this.deposit_avalibility = deposit_avalibility;
	}
	public String getSeWithPO() {
		return seWithPO;
	}
	public void setSeWithPO(String seWithPO) {
		this.seWithPO = seWithPO;
	}
	public String getRecTransIndent() {
		return recTransIndent;
	}
	public void setRecTransIndent(String recTransIndent) {
		this.recTransIndent = recTransIndent;
	}
	public String getHospitalPan() {
		return hospitalPan;
	}
	public void setHospitalPan(String hospitalPan) {
		this.hospitalPan = hospitalPan;
	}
	public String getHospitalServiceRegnNo() {
		return hospitalServiceRegnNo;
	}
	public void setHospitalServiceRegnNo(String hospitalServiceRegnNo) {
		this.hospitalServiceRegnNo = hospitalServiceRegnNo;
	}
	public String getHospitalTin() {
		return hospitalTin;
	}
	public void setHospitalTin(String hospitalTin) {
		this.hospitalTin = hospitalTin;
	}

	public String getPrescribingDoctorRequired() { return prescribingDoctorRequired; }
	public void setPrescribingDoctorRequired(String v) { prescribingDoctorRequired = v; }

	public String getShowHospAmtsOnPharmaSales() {
		return showHospAmtsOnPharmaSales;
	}
	public void setShowHospAmtsOnPharmaSales(String showHospAmtsOnPharmaSales) {
		this.showHospAmtsOnPharmaSales = showHospAmtsOnPharmaSales;
	}


	public String getCurrencySymbol() { return currencySymbol; }
	public void setCurrencySymbol(String v) { currencySymbol = v; }

	public String getWhole() { return whole; }
	public void setWhole(String v) { whole = v; }

	public String getDecimal() { return decimal; }
	public void setDecimal(String v) { decimal = v; }

	public String getForceRemarksForPoItemReject() {
		return forceRemarksForPoItemReject;
	}
	public void setForceRemarksForPoItemReject(String forceRemarksForPoItemReject) {
		this.forceRemarksForPoItemReject = forceRemarksForPoItemReject;
	}
	public String getReturnAgainstSpecificSupplier() {
		return returnAgainstSpecificSupplier;
	}
	public void setReturnAgainstSpecificSupplier(
			String returnAgainstSpecificSupplier) {
		this.returnAgainstSpecificSupplier = returnAgainstSpecificSupplier;
	}
	public String getFixedOtCharges() {
		return fixedOtCharges;
	}
	public void setFixedOtCharges(String fixedOtCharges) {
		this.fixedOtCharges = fixedOtCharges;
	}

	public String getAllowIndentBasedIssue() {
		return allowIndentBasedIssue;
	}
	public void setAllowIndentBasedIssue(String allowIndentBasedIssue) {
		this.allowIndentBasedIssue = allowIndentBasedIssue;
	}

	public String getSalesReturnsPrintType() {
		return salesReturnsPrintType;
	}
	public void setSalesReturnsPrintType(String salesReturnsPrintType) {
		this.salesReturnsPrintType = salesReturnsPrintType;
	}
	public String getShowCED() {
		return showCED;
	}
	public void setShowCED(String showCED) {
		this.showCED = showCED;
	}

	public Date getGoLiveDate() { return goLiveDate; }
	public void setGoLiveDate(Date v) { goLiveDate = v; }

	public String getDefBillLaterCreditSales() {
		return defBillLaterCreditSales;
	}

	public void setDefBillLaterCreditSales(String defBillLaterCreditSales) {
		this.defBillLaterCreditSales = defBillLaterCreditSales;
	}

	public String getHospitalAddress() {
		return hospitalAddress;
	}

	public void setHospitalAddress(String hospitalAddress) {
		this.hospitalAddress = hospitalAddress;
	}
	public String getShowCESS() {
		return showCESS;
	}
	public void setShowCESS(String showCESS) {
		this.showCESS = showCESS;
	}
	public String getShowVAT() {
		return showVAT;
	}
	public void setShowVAT(String showVAT) {
		this.showVAT = showVAT;
	}
	public String getBarcodeForItem() {
		return barcodeForItem;
	}
	public void setBarcodeForItem(String barcodeForItem) {
		this.barcodeForItem = barcodeForItem;
	}
	public int getDecimalDigits() {
		return decimalDigits;
	}
	public void setDecimalDigits(int decimalDigits) {
		this.decimalDigits = decimalDigits;
	}
	public String getDefault_prescription_print_template() {
		return default_prescription_print_template;
	}
	public void setDefault_prescription_print_template(
			String default_prescription_print_template) {
		this.default_prescription_print_template = default_prescription_print_template;
	}
	
	public String getDefault_prescription_web_template() {
		return default_prescription_web_template;
	}
	public void setDefault_prescription_web_template(
			String default_prescription_web_template) {
		this.default_prescription_web_template = default_prescription_web_template;
	}
	public String getDefault_prescription_web_printer() {
		return default_prescription_web_printer;
	}
	public void setDefault_prescription_web_printer(
			String default_prescription_web_printer) {
		this.default_prescription_web_printer = default_prescription_web_printer;
	}
	public int getUploadLimitInMB() {
		return uploadLimitInMB;
	}
	public void setUploadLimitInMB(int uploadLimitInMB) {
		this.uploadLimitInMB = uploadLimitInMB;
	}
	public String getSalesPrintItems() {
		return salesPrintItems;
	}
	public void setSalesPrintItems(String salesPrintItems) {
		this.salesPrintItems = salesPrintItems;
	}
	public String getDefault_consultation_print_template() {
		return default_consultation_print_template;
	}
	public void setDefault_consultation_print_template(
			String default_consultation_print_template) {
		this.default_consultation_print_template = default_consultation_print_template;
	}
	public String getValidateDiagnosisCodification() {
		return validateDiagnosisCodification;
	}
	public void setValidateDiagnosisCodification(
			String validateDiagnosisCodification) {
		this.validateDiagnosisCodification = validateDiagnosisCodification;
	}

	public String getAllowBillNowInsurance() {
		return allowBillNowInsurance;
	}
	public void setAllowBillNowInsurance(String allowBillNowInsurance) {
		this.allowBillNowInsurance = allowBillNowInsurance;
	}
	public String getReturnValidDays() {
		return returnValidDays;
	}
	public void setReturnValidDays(String returnValidDays) {
		this.returnValidDays = returnValidDays;
	}
	public String getHospUsesDynamicAddress() {
		return hospUsesDynamicAddress;
	}
	public void setHospUsesDynamicAddress(String hospUsesDynamicAddress) {
		this.hospUsesDynamicAddress = hospUsesDynamicAddress;
	}
	public String getDefault_po_print_template() {
		return default_po_print_template;
	}
	public void setDefault_po_print_template(String default_po_print_template) {
		this.default_po_print_template = default_po_print_template;
	}
	public int getMax_centers_inc_default() {
		return max_centers_inc_default;
	}
	public void setMax_centers_inc_default(int max_centers_inc_default) {
		this.max_centers_inc_default = max_centers_inc_default;
	}
	public int getMax_active_hosp_users() {
		return max_active_hosp_users;
	}
	public void setMax_active_hosp_users(int max_active_hosp_users) {
		this.max_active_hosp_users = max_active_hosp_users;
	}
	public String getShow_tests_in_emr() {
		return show_tests_in_emr;
	}
	public void setShow_tests_in_emr(String show_tests_in_emr) {
		this.show_tests_in_emr = show_tests_in_emr;
	}
	public String getSampleNoBase() {
		return sampleNoBase;
	}
	public void setSampleNoBase(String sampleNoBase) {
		this.sampleNoBase = sampleNoBase;
	}
	public String getIndependentGenerationOfSampleId() {
		return independentGenerationOfSampleId;
	}
	public void setIndependentGenerationOfSampleId(
			String independentGenerationOfSampleId) {
		this.independentGenerationOfSampleId = independentGenerationOfSampleId;
	}
	public String getAllowDecimalsInQtyForIssues() {
		return allowDecimalsInQtyForIssues;
	}
	public void setAllowDecimalsInQtyForIssues(String allowDecimalsInQtyForIssues) {
		this.allowDecimalsInQtyForIssues = allowDecimalsInQtyForIssues;
	}
	public String getServiceNameRequired() {
		return serviceNameRequired;
	}
	public void setServiceNameRequired(String serviceNameRequired) {
		this.serviceNameRequired = serviceNameRequired;
	}
	public String getTestAbnormalResult() {
		return testAbnormalResult;
	}
	public void setTestAbnormalResult(String testAbnormalResult) {
		this.testAbnormalResult = testAbnormalResult;
	}
	public String getTestCriticalResult() {
		return testCriticalResult;
	}
	public void setTestCriticalResult(String testCriticalResult) {
		this.testCriticalResult = testCriticalResult;
	}
	public String getTestImprobableResult() {
		return testImprobableResult;
	}
	public void setTestImprobableResult(String testImprobableResult) {
		this.testImprobableResult = testImprobableResult;
	}
	public String getTestNormalResult() {
		return testNormalResult;
	}
	public void setTestNormalResult(String testNormalResult) {
		this.testNormalResult = testNormalResult;
	}
	public String getSurgeryNameRequired() {
		return surgeryNameRequired;
	}
	public void setSurgeryNameRequired(String surgeryNameRequired) {
		this.surgeryNameRequired = surgeryNameRequired;
	}
	public String getUse_smart_card() {
		return use_smart_card;
	}
	public void setUse_smart_card(String use_smart_card) {
		this.use_smart_card = use_smart_card;
	}
	public String getGen_token_for_lab() {
		return gen_token_for_lab;
	}
	public void setGen_token_for_lab(String gen_token_for_lab) {
		this.gen_token_for_lab = gen_token_for_lab;
	}
	public String getGen_token_for_rad() {
		return gen_token_for_rad;
	}
	public void setGen_token_for_rad(String gen_token_for_rad) {
		this.gen_token_for_rad = gen_token_for_rad;
	}
	public String getDefault_emr_print_template() {
		return default_emr_print_template;
	}
	public void setDefault_emr_print_template(String default_emr_print_template) {
		this.default_emr_print_template = default_emr_print_template;
	}
	public String getBillingBedTypeForOP() {
		return billingBedTypeForOP;
	}
	public void setBillingBedTypeForOP(String billingBedTypeForOP) {
		this.billingBedTypeForOP = billingBedTypeForOP;
	}
	public BigDecimal getAuto_close_claims_with_difference() {
		return auto_close_claims_with_difference;
	}
	public void setAuto_close_claims_with_difference(
			BigDecimal auto_close_claims_with_difference) {
		this.auto_close_claims_with_difference = auto_close_claims_with_difference;
	}
	public String getDepositReceiptRefundPrintDefault() {
		return depositReceiptRefundPrintDefault;
	}
	public void setDepositReceiptRefundPrintDefault(
			String depositReceiptRefundPrintDefault) {
		this.depositReceiptRefundPrintDefault = depositReceiptRefundPrintDefault;
	}
	public BigDecimal getPoints_earning_amt() {
		return points_earning_amt;
	}
	public void setPoints_earning_amt(BigDecimal points_earning_amt) {
		this.points_earning_amt = points_earning_amt;
	}
	public int getPoints_earning_points() {
		return points_earning_points;
	}
	public void setPoints_earning_points(int points_earning_points) {
		this.points_earning_points = points_earning_points;
	}
	public BigDecimal getPoints_redemption_rate() {
		return points_redemption_rate;
	}
	public void setPoints_redemption_rate(BigDecimal points_redemption_rate) {
		this.points_redemption_rate = points_redemption_rate;
	}
	public String getDefault_dental_cons_print_template() {
		return default_dental_cons_print_template;
	}
	public void setDefault_dental_cons_print_template(
			String default_dental_cons_print_template) {
		this.default_dental_cons_print_template = default_dental_cons_print_template;
	}
	public String getConsumableStockNegative() {
		return consumableStockNegative;
	}
	public void setConsumableStockNegative(String consumableStockNegative) {
		this.consumableStockNegative = consumableStockNegative;
	}
	public String getServiceChargePercent() {
		return serviceChargePercent;
	}
	public void setServiceChargePercent(String serviceChargePercent) {
		this.serviceChargePercent = serviceChargePercent;
	}
	public String getPoToBeValidated() {
		return poToBeValidated;
	}
	public void setPoToBeValidated(String poToBeValidated) {
		this.poToBeValidated = poToBeValidated;
	}
	public String getDefault_voucher_print() {
		return default_voucher_print;
	}
	public void setDefault_voucher_print(String default_voucher_print) {
		this.default_voucher_print = default_voucher_print;
	}
	public String getSampleassertion() {
		return sampleassertion;
	}
	public void setSampleassertion(String sampleassertion) {
		this.sampleassertion = sampleassertion;
	}
	public int getFin_year_end_month() {
		return fin_year_end_month;
	}
	public void setFin_year_end_month(int fin_year_end_month) {
		this.fin_year_end_month = fin_year_end_month;
	}
	public int getFin_year_start_month() {
		return fin_year_start_month;
	}
	public void setFin_year_start_month(int fin_year_start_month) {
		this.fin_year_start_month = fin_year_start_month;
	}
	public int getFinYearEndMonth() {
		return finYearEndMonth;
	}
	public void setFinYearEndMonth(int finYearEndMonth) {
		this.finYearEndMonth = finYearEndMonth;
	}

	public String getCurrencyFormat() {
		return currencyFormat;
	}
	public void setCurrencyFormat(String currencyFormat) {
		this.currencyFormat = currencyFormat;
	}
	public String getBillcancellationrequiresapproval() {
		return billcancellationrequiresapproval;
	}
	public void setBillcancellationrequiresapproval(
			String billcancellationrequiresapproval) {
		this.billcancellationrequiresapproval = billcancellationrequiresapproval;
	}


	public int getbloodExpiry() {
		return bloodExpiry;
	}
	public void setbloodExpiry(int bloodExpiry) {
		this.bloodExpiry = bloodExpiry;
	}
	public String getCorporate_insurance() {
		return corporate_insurance;
	}
	public void setCorporate_insurance(String corporate_insurance) {
		this.corporate_insurance = corporate_insurance;
	}
	public String getEnable_force_selection_for_mrno_search() {
		return enable_force_selection_for_mrno_search;
	}
	public void setEnable_force_selection_for_mrno_search(
			String enable_force_selection_for_mrno_search) {
		this.enable_force_selection_for_mrno_search = enable_force_selection_for_mrno_search;
	}
	public String getEmr_url_date() {
		return emr_url_date;
	}
	public void setEmr_url_date(String emr_url_date) {
		this.emr_url_date = emr_url_date;
	}

	public String getAggregate_amt_on_remittance() {
		return aggregate_amt_on_remittance;
	}
	public void setAggregate_amt_on_remittance(String aggregate_amt_on_remittance) {
		this.aggregate_amt_on_remittance = aggregate_amt_on_remittance;
	}
	public String getHijriCalendar() {
		return hijriCalendar;
	}
	public void setHijriCalendar(String hijriCalendar) {
		this.hijriCalendar = hijriCalendar;
	}
	public String getStock_entry_agnst_do() {
		return stock_entry_agnst_do;
	}
	public void setStock_entry_agnst_do(String stock_entry_agnst_do) {
		this.stock_entry_agnst_do = stock_entry_agnst_do;
	}
	public String getCheck_insu_card_exp_in_sales() {
		return check_insu_card_exp_in_sales;
	}
	public void setCheck_insu_card_exp_in_sales(
			String check_insu_card_exp_in_sales) {
		this.check_insu_card_exp_in_sales = check_insu_card_exp_in_sales;
	}
	public int getPbmPriceThreshold() {
		return pbmPriceThreshold;
	}
	public void setPbmPriceThreshold(int pbmPriceThreshold) {
		this.pbmPriceThreshold = pbmPriceThreshold;
	}
	public String getIsMobileValidate() {
		return isMobileValidate;
	}
	public void setIsMobileValidate(String isMobileValidate) {
		this.isMobileValidate = isMobileValidate;
	}
	public String getMobileStartPattern() {
		return mobileStartPattern;
	}
	public void setMobileStartPattern(String mobileStartPattern) {
		this.mobileStartPattern = mobileStartPattern;
	}
	public String getMobileLengthPattern() {
		return mobileLengthPattern;
	}
	public void setMobileLengthPattern(String mobileLengthPattern) {
		this.mobileLengthPattern = mobileLengthPattern;
	}
	public void setEmailBillPrint(int emailBillPrint){
		this.emailBillPrint = emailBillPrint;
	}
	public int getEmailBillPrint() {
		return emailBillPrint;
	}
	public String getEmailBillNowTemplate() {
		return emailBillNowTemplate;
	}
	public void setEmailBillNowTemplate(String emailBillNowTemplate) {
		this.emailBillNowTemplate = emailBillNowTemplate;
	}
	public String getEmailBillLaterTemplate() {
		return emailBillLaterTemplate;
	}
	public void setEmailBillLaterTemplate(String emailBillLaterTemplate) {
		this.emailBillLaterTemplate = emailBillLaterTemplate;
	}
	public String getMod_username() {
		return mod_username;
	}
	public void setMod_username(String mod_username) {
		this.mod_username = mod_username;
	}
	public String getBillLabelForBillLaterBills() {
		return billLabelForBillLaterBills;
	}
	public void setBillLabelForBillLaterBills(String billLabelForBillLaterBills) {
		this.billLabelForBillLaterBills = billLabelForBillLaterBills;
	}
	public String getIncomeTaxCashLimitApplicability() {
		return incomeTaxCashLimitApplicability;
	}
	public void setIncomeTaxCashLimitApplicability(String incomeTaxCashLimitApplicability) {
		this.incomeTaxCashLimitApplicability = incomeTaxCashLimitApplicability;
	}
	public String getEnablePatientDepositAvailability() {
		return enablePatientDepositAvailability;
	}
	public void setEnablePatientDepositAvailability(String enablePatientDepositAvailability) {
		this.enablePatientDepositAvailability = enablePatientDepositAvailability;
	}

  public int getRaAutoProcessLastNumberOfDays() {
    return raAutoProcessLastNumberOfDays;
  }

  public void setRaAutoProcessLastNumberOfDays(int raAutoProcessLastNumberOfDays) {
    this.raAutoProcessLastNumberOfDays = raAutoProcessLastNumberOfDays;
  }
  public String getApplyCpValidationForPo() {
		return applyCpValidationForPo;
  }
  public void setApplyCpValidationForPo(String applyCpValidationForPo) {
		this.applyCpValidationForPo = applyCpValidationForPo;
  }
}
