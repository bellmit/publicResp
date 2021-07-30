package com.insta.hms.api.models;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="voucher")
public class HmsAccountingInfoXMLBinding {

    @XmlElement(name="center_id")
    private Integer centerId;
    
    @XmlElement(name="center_name")
    private String centerName;
    
    @XmlElement(name="visit_type")
    private String visitType;
    
    @XmlElement(name="mr_no")
    private String mrNo;
    
    @XmlElement(name="visit_id")
    private String visitId;
    
    @XmlElement(name="charge_group")
    private String chargeGroup;
    
    @XmlElement(name="charge_head")
    private String chargeHead;
    
    @XmlElement(name="account_group")
    private Integer accountGroup;
    
    @XmlElement(name="service_group")
    private String serviceGroup;
    
    @XmlElement(name="service_sub_group")
    private String serviceSubGroup;
    
    @XmlElement(name="bill_no")
    private String billNo;
    
    @XmlElement(name="audit_control_number")
    private String auditControlNumber;
    
    @XmlElement(name="voucher_no")
    private String voucherNo;
    
    @XmlElement(name="voucher_type")
    private String voucherType;
    
    @XmlElement(name="voucher_date")
    private String voucherDate;
    
    @XmlElement(name="item_code")
    private String itemCode;
    
    @XmlElement(name="item_name")
    private String itemName;
    
    @XmlElement(name="receipt_store")
    private String receiptStore;
    
    @XmlElement(name="issue_store")
    private String issueStore;
    
    @XmlElement(name="currency")
    private String currency;
    
    @XmlElement(name="currency_conversion_rate")
    private BigDecimal currencyConversionRate;
    
    @XmlElement(name="quantity")
    private BigDecimal quantity;
    
    @XmlElement(name="unit")
    private String unit;
    
    @XmlElement(name="unit_rate")
    private BigDecimal unitRate;
    
    @XmlElement(name="gross_amount")
    private BigDecimal grossAmount;
    
    @XmlElement(name="round_of_amount")
    private BigDecimal roundOffAmount;
    
    @XmlElement(name="discount_amount")
    private BigDecimal discountAmount;
    
    @XmlElement(name="points_redeemed")
    private Integer pointsRedeemed;
    
    @XmlElement(name="points_redeemed_rate")
    private BigDecimal pointsRedeemedRate;
    
    @XmlElement(name="points_redeemed_amount")
    private BigDecimal pointsRedeemedAmount;
    
    @XmlElement(name="item_category_id")
    private Integer itemCategoryId;
    
    @XmlElement(name="purchase_vat_amount")
    private BigDecimal purchaseVatAmount;
    
    @XmlElement(name="purchase_vat_percent")
    private BigDecimal purchaseVatPercent;
    
    @XmlElement(name="sales_vat_amount")
    private BigDecimal salesVatAmount;
    
    @XmlElement(name="sales_vat_percent")
    private BigDecimal salesVatPercent;
    
    @XmlElement(name="debit_account")
    private String debitAccount;
    
    @XmlElement(name="credit_account")
    private String creditAccount;
    
    @XmlElement(name="tax_amount")
    private BigDecimal taxAmount;
    
    @XmlElement(name="net_amount")
    private BigDecimal netAmount;
    
    @XmlElement(name="admitting_doctor")
    private String admittingDoctor;
    
    @XmlElement(name="prescribing_doctor")
    private String prescribingDoctor;
    
    @XmlElement(name="conducting_doctor")
    private String conductiongDoctor;
    
    @XmlElement(name="referral_doctor")
    private String referralDoctor;
    
    @XmlElement(name="payee_doctor")
    private String payeeDoctor;
    
    @XmlElement(name="outhouse_name")
    private String outhouseName;
    
    @XmlElement(name="incoming_hospital")
    private String incoimngHospital;
    
    @XmlElement(name="admitting_department")
    private String admittingDepartment;
    
    @XmlElement(name="conducting_department")
    private String conductingDepartment;
    
    @XmlElement(name="cost_amount")
    private BigDecimal costAmount;
    
    @XmlElement(name="supplier_name")
    private String supplierName;
    
    @XmlElement(name="invoice_no")
    private String invoiceNo;
    
    @XmlElement(name="invoice_date")
    private String invoiceDate;
    
    @XmlElement(name="voucher_ref")
    private String voucherRef;
    
    @XmlElement(name="remarks")
    private String remarks;
    
    @XmlElement(name="mod_time")
    private String modTime;
    
    @XmlElement(name="counter_no")
    private String counterNo;
    
    @XmlElement(name="bill_open_date")
    private String billOpenDate;
    
    @XmlElement(name="bill_finalize_date")
    private String billFinalizedDate;
    
    @XmlElement(name="bill_last_finalized_date")
    private String billLastFinalizedDate;
    
    @XmlElement(name="created_at")
    private String createdAt;
    
    @XmlElement(name="charge_reference_id")
    private String chargeReferenceId;
    
    @XmlElement(name="primary_id_reference_table")
    private String primaryIdReferenceTable;
    
    @XmlElement(name="is_tpa")
    private String isTpa;
    
    @XmlElement(name="insurance_co")
    private String insuranceCo;
    
    @XmlElement(name="old_mr_no")
    private String oldMrNo;
    
    @XmlElement(name="issue_store_center")
    private String issueStoreCenter;
    
    @XmlElement(name="receipt_store_center")
    private String receiptStoreCenter;
    
    @XmlElement(name="po_number")
    private String poNumber;
    
    @XmlElement(name="po_date")
    private Date poDate;
    
    @XmlElement(name="transaction_type")
    private String transactionType;
    
    @XmlElement(name="custom_1")
    private String custom1;
    
    @XmlElement(name="custom_2")
    private String custom2;
    
    @XmlElement(name="custom_3")
    private String custom3;
    
    @XmlElement(name="custom_4")
    private String custom4;
    
    @XmlElement(name="cust_supplier_code")
    private String custSupplierCode;
    
    @XmlElement(name="grn_date")
    private String grnDate;
    
    @XmlElement(name="cust_item_code")
    private String custItemCode;
    
    @XmlElement(name="prescribing_doctor_dept_name")
    private String prescribingDoctorDeptName;
    
    @XmlElement(name="custom_8")
    private String custom8;
    
    @XmlElement(name="custom_9")
    private String custom9;
    
    @XmlElement(name="custom_10")
    private String custom10;
    
    @XmlElement(name="custom_11")
    private String custom11;
    
    @XmlElement(name="guid")
    private String guid;
    
    @XmlElement(name="update_status")
    private Integer updateStatus;

    @XmlElement(name="patient_name")
    private String patientName;
    
    @XmlElement(name="job_transaction")
    private Integer jobTransaction;
    
    @XmlElement(name="voucher_sub_type")
    private String voucherSubType;

    @XmlElement(name="ha_item_code")
    private String haItemCode;
    
    @XmlElement(name="ha_code_type")
    private String haCodeType;
    
    public HmsAccountingInfoXMLBinding(){}
    public HmsAccountingInfoXMLBinding(Map<String,Object> voucher){
        this.setCenterId(Integer.valueOf(String.valueOf(voucher.get("center_id")).trim()));
        this.setCenterName(String.valueOf(voucher.get("center_name")).trim());
        this.setVisitType(String.valueOf(voucher.get("visit_type")).trim());
        this.setMrNo(String.valueOf(voucher.get("mr_no")).trim());
        this.setVisitId(String.valueOf(voucher.get("visit_id")).trim());
        this.setChargeGroup(String.valueOf(voucher.get("charge_group")).trim());
        this.setChargeHead(String.valueOf(voucher.get("charge_head")).trim());
        this.setAccountGroup((Integer)voucher.get("account_group"));
        this.setServiceGroup(String.valueOf(voucher.get("service_group")).trim());
        this.setServiceSubGroup(String.valueOf(voucher.get("service_sub_group")).trim());
        this.setBillNo(String.valueOf(voucher.get("bill_no")).trim());
        this.setAuditControlNumber(String.valueOf(voucher.get("audit_control_number")).trim());
        this.setVoucherNo(String.valueOf(voucher.get("voucher_no")).trim());
        this.setVoucherType(String.valueOf(voucher.get("voucher_type")).trim());
        this.setVoucherDate(String.valueOf(voucher.get("voucher_date")));
        this.setItemCode(String.valueOf(voucher.get("item_code")).trim());
        this.setItemName(String.valueOf(voucher.get("item_name")).trim());
        this.setReceiptStore(String.valueOf(voucher.get("receipt_store")).trim());
        this.setIssueStore(String.valueOf(voucher.get("issue_store")).trim());
        this.setCurrency(String.valueOf(voucher.get("currency")).trim());
        this.setCurrencyConversionRate((BigDecimal) voucher.get("currency_conversion_rate"));
        this.setQuantity((BigDecimal) voucher.get("quantity"));
        this.setUnit(String.valueOf(voucher.get("unit")).trim());
        this.setUnitRate((BigDecimal) voucher.get("unit_rate"));
        this.setGrossAmount((BigDecimal) voucher.get("gross_amount"));
        this.setRoundOffAmount((BigDecimal) voucher.get("round_off_amount"));
        this.setDiscountAmount((BigDecimal) voucher.get("discount_amount"));
        this.setPointsRedeemed((Integer) voucher.get("points_redeemed"));
        this.setPointsRedeemedRate((BigDecimal) voucher.get("points_redeemed_rate"));
        this.setPointsRedeemedAmount((BigDecimal) voucher.get("points_redeemed_amount"));
        this.setItemCategoryId((Integer) voucher.get("item_category_id"));
        this.setPurchaseVatAmount((BigDecimal) voucher.get("purchase_vat_amount"));
        this.setPurchaseVatPercent((BigDecimal) voucher.get("purchase_vat_percent"));
        this.setSalesVatAmount((BigDecimal) voucher.get("sales_vat_amount"));
        this.setSalesVatPercent((BigDecimal) voucher.get("sales_vat_percent"));
        this.setDebitAccount(String.valueOf(voucher.get("debit_account")).trim());
        this.setCreditAccount(String.valueOf(voucher.get("credit_account")).trim());
        this.setTaxAmount((BigDecimal) voucher.get("tax_amount"));
        this.setNetAmount((BigDecimal) voucher.get("net_amount"));
        this.setAdmittingDoctor(String.valueOf(voucher.get("admitting_doctor")).trim());
        this.setPrescribingDoctor(String.valueOf(voucher.get("prescribing_doctor")).trim());
        this.setConductiongDoctor(String.valueOf(voucher.get("conductiong_doctor")).trim());
        this.setReferralDoctor(String.valueOf(voucher.get("referral_doctor")).trim());
        this.setPayeeDoctor(String.valueOf(voucher.get("payee_doctor")).trim());
        this.setOuthouseName(String.valueOf(voucher.get("outhouse_name")).trim());
        this.setIncoimngHospital(String.valueOf(voucher.get("incoimng_hospital")).trim());
        this.setAdmittingDepartment(String.valueOf(voucher.get("admitting_department")).trim());
        this.setConductingDepartment(String.valueOf(voucher.get("conducting_department")).trim());
        this.setCostAmount((BigDecimal) voucher.get("cost_amount"));
        this.setSupplierName(String.valueOf(voucher.get("supplier_name")).trim());
        this.setInvoiceNo(String.valueOf(voucher.get("invoice_no")).trim());
        this.setInvoiceDate(String.valueOf(voucher.get("invoice_date")));
        this.setVoucherRef(String.valueOf(voucher.get("voucher_ref")).trim());
        this.setRemarks(String.valueOf(voucher.get("remarks")).trim());
        this.setModTime( String.valueOf(voucher.get("mod_time")));
        this.setCounterNo(String.valueOf(voucher.get("counter_no")).trim());
        this.setBillOpenDate(String.valueOf(voucher.get("bill_open_date")));
        this.setBillFinalizedDate(String.valueOf(voucher.get("bill_finalized_date")));
        this.setBillLastFinalizedDate(String.valueOf(voucher.get("bill_last_finalized_date")));
        this.setIsTpa(String.valueOf(voucher.get("is_tpa")).trim());
        this.setInsuranceCo(String.valueOf(voucher.get("insurance_co")).trim());
        this.setOldMrNo(String.valueOf(voucher.get("old_mr_no")).trim());
        this.setIssueStoreCenter(String.valueOf(voucher.get("issue_store_center")).trim());
        this.setReceiptStoreCenter(String.valueOf(voucher.get("receipt_store_center")).trim());
        this.setPoNumber(String.valueOf(voucher.get("po_number")).trim());
        this.setPoDate((Date) voucher.get("po_date"));
        this.setTransactionType(String.valueOf(voucher.get("transaction_type")).trim());
        this.setCustom1(String.valueOf(voucher.get("custom_1")).trim());
        this.setCustom2(String.valueOf(voucher.get("custom_2")).trim());
        this.setCustom3(String.valueOf(voucher.get("custom_3")).trim());
        this.setCustom4(String.valueOf(voucher.get("custom_4")).trim());
        this.setCustSupplierCode(String.valueOf(voucher.get("cust_supplier_code")).trim());
        this.setGrnDate(String.valueOf(voucher.get("grn_date")));
        this.setCustItemCode(String.valueOf(voucher.get("cust_item_code")).trim());
        this.setPrescribingDoctorDeptName(String.valueOf(voucher.get("prescribing_doctor_dept_name")).trim());
        this.setCustom8(String.valueOf(voucher.get("custom_8")).trim());
        this.setCustom9(String.valueOf(voucher.get("custom_9")).trim());
        this.setCustom10(String.valueOf(voucher.get("custom_10")).trim());
        this.setCustom11(String.valueOf(voucher.get("custom_11")).trim());
        this.setGuid(String.valueOf(voucher.get("guid")).trim());
        this.setUpdateStatus((Integer) voucher.get("update_status"));
        this.setCreatedAt(String.valueOf(voucher.get("created_at")));
        this.setPrimaryIdReferenceTable(String.valueOf(voucher.get("primary_id_reference_table")));
        this.setChargeReferenceId(String.valueOf(voucher.get("charge_reference_id")));
        this.setCreatedAt(String.valueOf(voucher.get("created_at")));
        this.setPatientName(String.valueOf(voucher.get("patient_name")).trim());
        this.setJobTransaction((Integer) voucher.get("job_transaction"));
        this.setVoucherSubType(String.valueOf(voucher.get("voucher_sub_type")).trim());
        this.setHaItemCode(String.valueOf(voucher.get("ha_item_code")).trim());
        this.setHaCodeType(String.valueOf(voucher.get("ha_code_type")).trim());

    }

    public Integer getCenterId() {
        return centerId;
    }

    public void setCenterId(Integer centerId) {
        this.centerId = centerId;
    }

    
    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    
    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    
    public String getMrNo() {
        return mrNo;
    }

    public void setMrNo(String mrNo) {
        this.mrNo = mrNo;
    }

    
    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    
    public String getChargeGroup() {
        return chargeGroup;
    }

    public void setChargeGroup(String chargeGroup) {
        this.chargeGroup = chargeGroup;
    }

    
    public String getChargeHead() {
        return chargeHead;
    }

    public void setChargeHead(String chargeHead) {
        this.chargeHead = chargeHead;
    }

    
    public Integer getAccountGroup() {
        return accountGroup;
    }

    public void setAccountGroup(Integer accountGroup) {
        this.accountGroup = accountGroup;
    }

    
    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    
    public String getServiceSubGroup() {
        return serviceSubGroup;
    }

    public void setServiceSubGroup(String serviceSubGroup) {
        this.serviceSubGroup = serviceSubGroup;
    }

    
    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    
    public String getAuditControlNumber() {
        return auditControlNumber;
    }

    public void setAuditControlNumber(String auditControlNumber) {
        this.auditControlNumber = auditControlNumber;
    }

    
    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    
    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    
    public String getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(String voucherDate) {
        this.voucherDate = voucherDate;
    }

    
    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    
    public String getReceiptStore() {
        return receiptStore;
    }

    public void setReceiptStore(String receiptStore) {
        this.receiptStore = receiptStore;
    }

    
    public String getIssueStore() {
        return issueStore;
    }

    public void setIssueStore(String issueStore) {
        this.issueStore = issueStore;
    }

    
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    
    public BigDecimal getCurrencyConversionRate() {
        return currencyConversionRate;
    }

    public void setCurrencyConversionRate(BigDecimal currencyConversionRate) {
        this.currencyConversionRate = currencyConversionRate;
    }

    
    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    
    public BigDecimal getUnitRate() {
        return unitRate;
    }

    public void setUnitRate(BigDecimal unitRate) {
        this.unitRate = unitRate;
    }

    
    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    
    public BigDecimal getRoundOffAmount() {
        return roundOffAmount;
    }

    public void setRoundOffAmount(BigDecimal roundOffAmount) {
        this.roundOffAmount = roundOffAmount;
    }

    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    
    public Integer getPointsRedeemed() {
        return pointsRedeemed;
    }

    public void setPointsRedeemed(Integer pointsRedeemed) {
        this.pointsRedeemed = pointsRedeemed;
    }

    
    public BigDecimal getPointsRedeemedRate() {
        return pointsRedeemedRate;
    }

    public void setPointsRedeemedRate(BigDecimal pointsRedeemedRate) {
        this.pointsRedeemedRate = pointsRedeemedRate;
    }

    
    public BigDecimal getPointsRedeemedAmount() {
        return pointsRedeemedAmount;
    }

    public void setPointsRedeemedAmount(BigDecimal pointsRedeemedAmount) {
        this.pointsRedeemedAmount = pointsRedeemedAmount;
    }

    
    public Integer getItemCategoryId() {
        return itemCategoryId;
    }

    public void setItemCategoryId(Integer itemCategoryId) {
        this.itemCategoryId = itemCategoryId;
    }

    
    public BigDecimal getPurchaseVatAmount() {
        return purchaseVatAmount;
    }

    public void setPurchaseVatAmount(BigDecimal purchaseVatAmount) {
        this.purchaseVatAmount = purchaseVatAmount;
    }

    
    public BigDecimal getPurchaseVatPercent() {
        return purchaseVatPercent;
    }

    public void setPurchaseVatPercent(BigDecimal purchaseVatPercent) {
        this.purchaseVatPercent = purchaseVatPercent;
    }

    
    public BigDecimal getSalesVatAmount() {
        return salesVatAmount;
    }

    public void setSalesVatAmount(BigDecimal salesVatAmount) {
        this.salesVatAmount = salesVatAmount;
    }

    
    public BigDecimal getSalesVatPercent() {
        return salesVatPercent;
    }

    public void setSalesVatPercent(BigDecimal salesVatPercent) {
        this.salesVatPercent = salesVatPercent;
    }

    
    public String getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(String debitAccount) {
        this.debitAccount = debitAccount;
    }

    
    public String getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(String creditAccount) {
        this.creditAccount = creditAccount;
    }

    
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    
    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    
    public String getAdmittingDoctor() {
        return admittingDoctor;
    }

    public void setAdmittingDoctor(String admittingDoctor) {
        this.admittingDoctor = admittingDoctor;
    }

    
    public String getPrescribingDoctor() {
        return prescribingDoctor;
    }

    public void setPrescribingDoctor(String prescribingDoctor) {
        this.prescribingDoctor = prescribingDoctor;
    }

    
    public String getConductiongDoctor() {
        return conductiongDoctor;
    }

    public void setConductiongDoctor(String conductiongDoctor) {
        this.conductiongDoctor = conductiongDoctor;
    }

    
    public String getReferralDoctor() {
        return referralDoctor;
    }

    public void setReferralDoctor(String referralDoctor) {
        this.referralDoctor = referralDoctor;
    }

    
    public String getPayeeDoctor() {
        return payeeDoctor;
    }

    public void setPayeeDoctor(String payeeDoctor) {
        this.payeeDoctor = payeeDoctor;
    }

    
    public String getOuthouseName() {
        return outhouseName;
    }

    public void setOuthouseName(String outhouseName) {
        this.outhouseName = outhouseName;
    }

    
    public String getIncoimngHospital() {
        return incoimngHospital;
    }

    public void setIncoimngHospital(String incoimngHospital) {
        this.incoimngHospital = incoimngHospital;
    }

    
    public String getAdmittingDepartment() {
        return admittingDepartment;
    }

    public void setAdmittingDepartment(String admittingDepartment) {
        this.admittingDepartment = admittingDepartment;
    }

    
    public String getConductingDepartment() {
        return conductingDepartment;
    }

    public void setConductingDepartment(String conductingDepartment) {
        this.conductingDepartment = conductingDepartment;
    }

    
    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount) {
        this.costAmount = costAmount;
    }

    
    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    
    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    
    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    
    public String getVoucherRef() {
        return voucherRef;
    }

    public void setVoucherRef(String voucherRef) {
        this.voucherRef = voucherRef;
    }

    
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    
    public String getModTime() {
        return modTime;
    }

    public void setModTime(String modTime) {
        this.modTime = modTime;
    }

    
    public String getCounterNo() {
        return counterNo;
    }

    public void setCounterNo(String counterNo) {
        this.counterNo = counterNo;
    }

    
    public String getBillOpenDate() {
        return billOpenDate;
    }

    public void setBillOpenDate(String billOpenDate) {
        this.billOpenDate = billOpenDate;
    }

    
    public String getBillFinalizedDate() {
        return billFinalizedDate;
    }

    public void setBillFinalizedDate(String billFinalizedDate) {
        this.billFinalizedDate = billFinalizedDate;
    }

    
    public String getBillLastFinalizedDate() {
        return billLastFinalizedDate;
    }

    public void setBillLastFinalizedDate(String billLastFinalizedDate) {
        this.billLastFinalizedDate = billLastFinalizedDate;
    }

    
    public String getIsTpa() {
        return isTpa;
    }

    public void setIsTpa(String isTpa) {
        this.isTpa = isTpa;
    }

    
    public String getInsuranceCo() {
        return insuranceCo;
    }

    public void setInsuranceCo(String insuranceCo) {
        this.insuranceCo = insuranceCo;
    }

    
    public String getOldMrNo() {
        return oldMrNo;
    }

    public void setOldMrNo(String oldMrNo) {
        this.oldMrNo = oldMrNo;
    }

    
    public String getIssueStoreCenter() {
        return issueStoreCenter;
    }

    public void setIssueStoreCenter(String issueStoreCenter) {
        this.issueStoreCenter = issueStoreCenter;
    }

    
    public String getReceiptStoreCenter() {
        return receiptStoreCenter;
    }

    public void setReceiptStoreCenter(String receiptStoreCenter) {
        this.receiptStoreCenter = receiptStoreCenter;
    }

    
    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    
    public Date getPoDate() {
        return poDate;
    }

    public void setPoDate(Date poDate) {
        this.poDate = poDate;
    }

    
    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    
    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    
    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    
    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    
    public String getCustom4() {
        return custom4;
    }

    public void setCustom4(String custom4) {
        this.custom4 = custom4;
    }

    
    public String getCustSupplierCode() {
        return custSupplierCode;
    }

    public void setCustSupplierCode(String custSupplierCode) {
        this.custSupplierCode = custSupplierCode;
    }

    
    public String getGrnDate() {
        return grnDate;
    }

    public void setGrnDate(String grnDate) {
        this.grnDate = grnDate;
    }

    
    public String getCustItemCode() {
        return custItemCode;
    }

    public void setCustItemCode(String custItemCode) {
        this.custItemCode = custItemCode;
    }

    
    public String getPrescribingDoctorDeptName() {
        return prescribingDoctorDeptName;
    }

    public void setPrescribingDoctorDeptName(String prescribingDoctorDeptName) {
        this.prescribingDoctorDeptName = prescribingDoctorDeptName;
    }

    
    public String getCustom8() {
        return custom8;
    }

    public void setCustom8(String custom8) {
        this.custom8 = custom8;
    }

    
    public String getCustom9() {
        return custom9;
    }

    public void setCustom9(String custom9) {
        this.custom9 = custom9;
    }

    
    public String getCustom10() {
        return custom10;
    }

    public void setCustom10(String custom10) {
        this.custom10 = custom10;
    }

    
    public String getCustom11() {
        return custom11;
    }

    public void setCustom11(String custom11) {
        this.custom11 = custom11;
    }

    
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    
    public Integer getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(Integer updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getCreatedAt() {
      return createdAt;
    }
    public void setCreatedAt(String createdAt) {
      this.createdAt = createdAt;
    }
    public String getChargeReferenceId() {
      return chargeReferenceId;
    }
    public void setChargeReferenceId(String chargeReferenceId) {
      this.chargeReferenceId = chargeReferenceId;
    }
    public String getPrimaryIdReferenceTable() {
      return primaryIdReferenceTable;
    }
    public void setPrimaryIdReferenceTable(String primaryIdReferenceTable) {
      this.primaryIdReferenceTable = primaryIdReferenceTable;
    }
    
    public String getPatientName() {
      return patientName;
  }

  public void setPatientName(String patientName) {
      this.patientName = patientName;
  }
  
  public String getHaItemCode() {
    return haItemCode;
  }

  public void setHaItemCode(String haItemCode) {
    this.haItemCode = haItemCode;
  }
    
  public String getVoucherSubType() {
    return voucherSubType;
  }
  public void setVoucherSubType(String voucherSubType) {
    this.voucherSubType = voucherSubType;
  }
  
  public String getHaCodeType() {
    return haCodeType;
  }

  public void setHaCodeType(String haCodeType) {
    this.haCodeType = haCodeType;
  }

  public Integer getJobTransaction() {
    return jobTransaction;
  }
  public void setJobTransaction(Integer jobTransaction) {
    this.jobTransaction = jobTransaction;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HmsAccountingInfoXMLBinding that = (HmsAccountingInfoXMLBinding) o;
    return Objects.equals(centerId, that.centerId) &&
      Objects.equals(centerName, that.centerName) &&
      Objects.equals(visitType, that.visitType) &&
      Objects.equals(mrNo, that.mrNo) &&
      Objects.equals(visitId, that.visitId) &&
      Objects.equals(chargeGroup, that.chargeGroup) &&
      Objects.equals(chargeHead, that.chargeHead) &&
      Objects.equals(accountGroup, that.accountGroup) &&
      Objects.equals(serviceGroup, that.serviceGroup) &&
      Objects.equals(serviceSubGroup, that.serviceSubGroup) &&
      Objects.equals(billNo, that.billNo) &&
      Objects.equals(auditControlNumber, that.auditControlNumber) &&
      Objects.equals(voucherNo, that.voucherNo) &&
      Objects.equals(voucherType, that.voucherType) &&
      Objects.equals(voucherDate, that.voucherDate) &&
      Objects.equals(itemCode, that.itemCode) &&
      Objects.equals(itemName, that.itemName) &&
      Objects.equals(receiptStore, that.receiptStore) &&
      Objects.equals(issueStore, that.issueStore) &&
      Objects.equals(currency, that.currency) &&
      Objects.equals(currencyConversionRate, that.currencyConversionRate) &&
      Objects.equals(quantity, that.quantity) &&
      Objects.equals(unit, that.unit) &&
      Objects.equals(unitRate, that.unitRate) &&
      Objects.equals(grossAmount, that.grossAmount) &&
      Objects.equals(roundOffAmount, that.roundOffAmount) &&
      Objects.equals(discountAmount, that.discountAmount) &&
      Objects.equals(pointsRedeemed, that.pointsRedeemed) &&
      Objects.equals(pointsRedeemedRate, that.pointsRedeemedRate) &&
      Objects.equals(pointsRedeemedAmount, that.pointsRedeemedAmount) &&
      Objects.equals(itemCategoryId, that.itemCategoryId) &&
      Objects.equals(purchaseVatAmount, that.purchaseVatAmount) &&
      Objects.equals(purchaseVatPercent, that.purchaseVatPercent) &&
      Objects.equals(salesVatAmount, that.salesVatAmount) &&
      Objects.equals(salesVatPercent, that.salesVatPercent) &&
      Objects.equals(debitAccount, that.debitAccount) &&
      Objects.equals(creditAccount, that.creditAccount) &&
      Objects.equals(taxAmount, that.taxAmount) &&
      Objects.equals(netAmount, that.netAmount) &&
      Objects.equals(admittingDoctor, that.admittingDoctor) &&
      Objects.equals(prescribingDoctor, that.prescribingDoctor) &&
      Objects.equals(conductiongDoctor, that.conductiongDoctor) &&
      Objects.equals(referralDoctor, that.referralDoctor) &&
      Objects.equals(payeeDoctor, that.payeeDoctor) &&
      Objects.equals(outhouseName, that.outhouseName) &&
      Objects.equals(incoimngHospital, that.incoimngHospital) &&
      Objects.equals(admittingDepartment, that.admittingDepartment) &&
      Objects.equals(conductingDepartment, that.conductingDepartment) &&
      Objects.equals(costAmount, that.costAmount) &&
      Objects.equals(supplierName, that.supplierName) &&
      Objects.equals(invoiceNo, that.invoiceNo) &&
      Objects.equals(invoiceDate, that.invoiceDate) &&
      Objects.equals(voucherRef, that.voucherRef) &&
      Objects.equals(remarks, that.remarks) &&
      Objects.equals(modTime, that.modTime) &&
      Objects.equals(counterNo, that.counterNo) &&
      Objects.equals(billOpenDate, that.billOpenDate) &&
      Objects.equals(billFinalizedDate, that.billFinalizedDate) &&
      Objects.equals(isTpa, that.isTpa) &&
      Objects.equals(insuranceCo, that.insuranceCo) &&
      Objects.equals(oldMrNo, that.oldMrNo) &&
      Objects.equals(issueStoreCenter, that.issueStoreCenter) &&
      Objects.equals(receiptStoreCenter, that.receiptStoreCenter) &&
      Objects.equals(poNumber, that.poNumber) &&
      Objects.equals(poDate, that.poDate) &&
      Objects.equals(transactionType, that.transactionType) &&
      Objects.equals(custom1, that.custom1) &&
      Objects.equals(custom2, that.custom2) &&
      Objects.equals(custom3, that.custom3) &&
      Objects.equals(custom4, that.custom4) &&
      Objects.equals(custSupplierCode, that.custSupplierCode) &&
      Objects.equals(grnDate, that.grnDate) &&
      Objects.equals(custItemCode, that.custItemCode) &&
      Objects.equals(prescribingDoctorDeptName, that.prescribingDoctorDeptName) &&
      Objects.equals(custom8, that.custom8) &&
      Objects.equals(custom9, that.custom9) &&
      Objects.equals(custom10, that.custom10) &&
      Objects.equals(custom11, that.custom11) &&
      Objects.equals(guid, that.guid) &&
      Objects.equals(updateStatus, that.updateStatus) &&
      Objects.equals(patientName, that.patientName) && 
      Objects.equals(jobTransaction, that.jobTransaction) &&
      Objects.equals(haItemCode, that.haItemCode) && 
      Objects.equals(haCodeType, that.haCodeType) && 
      Objects.equals(voucherSubType, that.voucherSubType);
  }

  public int hashCode() {
    return Objects.hash(centerId, centerName, visitType, mrNo, visitId, chargeGroup, chargeHead, accountGroup, serviceGroup, serviceSubGroup, billNo, auditControlNumber, voucherNo, voucherType, voucherDate, itemCode, itemName, receiptStore, issueStore, currency, currencyConversionRate, quantity, unit, unitRate, grossAmount, roundOffAmount, discountAmount, pointsRedeemed, pointsRedeemedRate, pointsRedeemedAmount, itemCategoryId, purchaseVatAmount, purchaseVatPercent, salesVatAmount, salesVatPercent, debitAccount, creditAccount, taxAmount, netAmount, admittingDoctor, prescribingDoctor, conductiongDoctor, referralDoctor, payeeDoctor, outhouseName, incoimngHospital, admittingDepartment, conductingDepartment, costAmount, supplierName, invoiceNo, invoiceDate, voucherRef, remarks, modTime, counterNo, billOpenDate, billFinalizedDate, isTpa, insuranceCo, oldMrNo, issueStoreCenter, receiptStoreCenter, poNumber, poDate, transactionType, custom1, custom2, custom3, custom4, custSupplierCode, grnDate, custItemCode, prescribingDoctorDeptName, custom8, custom9, custom10, custom11, guid, updateStatus, patientName, jobTransaction, haItemCode, haCodeType,voucherSubType);
  }
}
