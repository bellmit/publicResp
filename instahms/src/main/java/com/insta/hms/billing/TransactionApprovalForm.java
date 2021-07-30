/**
 *
 */
package com.insta.hms.billing;

import org.apache.struts.action.ActionForm;

/**
 * @author kalpana.muvvala
 *
 */
public class TransactionApprovalForm extends ActionForm{



	private boolean patTypeAll;
	private boolean patTypeIp;
	private boolean patTypeOp;
	private boolean patTypeInsu;
    private boolean patTypeInsuAll;
    private boolean patTypeInsuNone;

	private boolean billStatusAll;
	private boolean billStatusOpen;
	private boolean billStatusFinalized;
	private boolean billStatusSettled;
	private boolean billStatusClosed;
	private boolean billStatusCancelld;

	private boolean chrGrpAll;
	private boolean chrGrpReg;
	private boolean chrGrpDoc;
	private boolean chrGrpOpe;
	private boolean chrGrpWard;
	private boolean chrGrpIcu;
	private boolean chrGrpOtherchr;
	private boolean chrGrpMed;
	private boolean chrGrpServ;
	private boolean chrGrpTest;

	private boolean chrStatsAll;
	private boolean chrStatsActive;
	private boolean chrStatsCancelled;

	private String fdate;
	private String tdate;

	private String pageNum;
	private String sortByIndex;
	private String nonApproved;
	private String pageSize;

	private String[] chargeId;
	private float[] rate;
	private float[] qty;
	private float[] discount;
	private float[] amount;
	private float[] origAmount;
	private float[] amountPaid;
	private String[] updatedRow;
	private String[] deletedRow;
	private String[] approvedRow;
	private String[] billNo;
	private String[] postedDate;
	private String[] remarks;
	private String[] chargeStatus;
	private String[] billStatus;



	public String[] getBillStatus() {
		return billStatus;
	}
	public void setBillStatus(String[] billStatus) {
		this.billStatus = billStatus;
	}
	public boolean isBillStatusAll() {
		return billStatusAll;
	}
	public void setBillStatusAll(boolean billStatusAll) {
		this.billStatusAll = billStatusAll;
	}
	public boolean isBillStatusCancelld() {
		return billStatusCancelld;
	}
	public void setBillStatusCancelld(boolean billStatusCancelld) {
		this.billStatusCancelld = billStatusCancelld;
	}
	public boolean isBillStatusClosed() {
		return billStatusClosed;
	}
	public void setBillStatusClosed(boolean billStatusClosed) {
		this.billStatusClosed = billStatusClosed;
	}
	public boolean isBillStatusFinalized() {
		return billStatusFinalized;
	}
	public void setBillStatusFinalized(boolean billStatusFinalized) {
		this.billStatusFinalized = billStatusFinalized;
	}
	public boolean isBillStatusOpen() {
		return billStatusOpen;
	}
	public void setBillStatusOpen(boolean billStatusOpen) {
		this.billStatusOpen = billStatusOpen;
	}
	public boolean isBillStatusSettled() {
		return billStatusSettled;
	}
	public void setBillStatusSettled(boolean billStatusSettled) {
		this.billStatusSettled = billStatusSettled;
	}
	public boolean isChrGrpAll() {
		return chrGrpAll;
	}
	public void setChrGrpAll(boolean chrGrpAll) {
		this.chrGrpAll = chrGrpAll;
	}
	public boolean isChrGrpDoc() {
		return chrGrpDoc;
	}
	public void setChrGrpDoc(boolean chrGrpDoc) {
		this.chrGrpDoc = chrGrpDoc;
	}
	public boolean isChrGrpIcu() {
		return chrGrpIcu;
	}
	public void setChrGrpIcu(boolean chrGrpIcu) {
		this.chrGrpIcu = chrGrpIcu;
	}
	public boolean isChrGrpMed() {
		return chrGrpMed;
	}
	public void setChrGrpMed(boolean chrGrpMed) {
		this.chrGrpMed = chrGrpMed;
	}
	public boolean isChrGrpOpe() {
		return chrGrpOpe;
	}
	public void setPatTypeInsu(boolean patTypeInsu) {
		this.patTypeInsu = patTypeInsu;
	}
	public boolean isPatTypeInsu() {
		return patTypeInsu;
	}

	public boolean isPatTypeInsuNone() {
		return patTypeInsuNone;
	}
	public void setPatTypeInsuNone(boolean patTypeInsuNone) {
		this.patTypeInsuNone = patTypeInsuNone;
	}

	public void setChrGrpOpe(boolean chrGrpOpe) {
		this.chrGrpOpe = chrGrpOpe;
	}
	public boolean isChrGrpOtherchr() {
		return chrGrpOtherchr;
	}
	public void setChrGrpOtherchr(boolean chrGrpOtherchr) {
		this.chrGrpOtherchr = chrGrpOtherchr;
	}
	public boolean isChrGrpReg() {
		return chrGrpReg;
	}
	public void setChrGrpReg(boolean chrGrpReg) {
		this.chrGrpReg = chrGrpReg;
	}
	public boolean isChrGrpServ() {
		return chrGrpServ;
	}
	public void setChrGrpServ(boolean chrGrpServ) {
		this.chrGrpServ = chrGrpServ;
	}
	public boolean isChrGrpWard() {
		return chrGrpWard;
	}
	public void setChrGrpWard(boolean chrGrpWard) {
		this.chrGrpWard = chrGrpWard;
	}
	public boolean isChrStatsActive() {
		return chrStatsActive;
	}
	public void setChrStatsActive(boolean chrStatsActive) {
		this.chrStatsActive = chrStatsActive;
	}
	public boolean isChrStatsAll() {
		return chrStatsAll;
	}
	public void setChrStatsAll(boolean chrStatsAll) {
		this.chrStatsAll = chrStatsAll;
	}
	public boolean isChrStatsCancelled() {
		return chrStatsCancelled;
	}
	public void setChrStatsCancelled(boolean chrStatsCancelled) {
		this.chrStatsCancelled = chrStatsCancelled;
	}

	public boolean isChrGrpTest() {
		return chrGrpTest;
	}
	public void setChrGrpTest(boolean chrGrpTest) {
		this.chrGrpTest = chrGrpTest;
	}

	public String getFdate() {
		return fdate;
	}
	public void setFdate(String fdate) {
		this.fdate = fdate;
	}
	public boolean isPatTypeAll() {
		return patTypeAll;
	}
	public void setPatTypeAll(boolean patTypeAll) {
		this.patTypeAll = patTypeAll;
	}
	public boolean isPatTypeIp() {
		return patTypeIp;
	}
	public void setPatTypeIp(boolean patTypeIp) {
		this.patTypeIp = patTypeIp;
	}
	public boolean isPatTypeOp() {
		return patTypeOp;
	}
	public void setPatTypeOp(boolean patTypeOp) {
		this.patTypeOp = patTypeOp;
	}

	public boolean isPatTypeInsuAll() {
		return patTypeInsuAll;
	}
	public void setPatTypeInsuAll(boolean patTypeInsuAll) {
		this.patTypeInsuAll = patTypeInsuAll;
	}

	public String getTdate() {
		return tdate;
	}
	public void setTdate(String tdate) {
		this.tdate = tdate;
	}
	public String[] getChargeStatus() {
		return chargeStatus;
	}
	public void setChargeStatus(String[] chargeStatus) {
		this.chargeStatus = chargeStatus;
	}
	public String[] getRemarks() {
		return remarks;
	}
	public void setRemarks(String[] remarks) {
		this.remarks = remarks;
	}
	public String[] getBillNo() {
		return billNo;
	}
	public void setBillNo(String[] billNo) {
		this.billNo = billNo;
	}
	public float[] getAmountPaid() {
		return amountPaid;
	}
	public void setAmountPaid(float[] amountPaid) {
		this.amountPaid = amountPaid;
	}
	public String[] getChargeId() {
		return chargeId;
	}
	public void setChargeId(String[] chargeId) {
		this.chargeId = chargeId;
	}
	public float[] getDiscount() {
		return discount;
	}
	public void setDiscount(float[] discount) {
		this.discount = discount;
	}
	public float[] getQty() {
		return qty;
	}
	public void setQty(float[] qty) {
		this.qty = qty;
	}
	public float[] getRate() {
		return rate;
	}
	public void setRate(float[] rate) {
		this.rate = rate;
	}
	public String[] getApprovedRow() {
		return approvedRow;
	}
	public void setApprovedRow(String[] approvedRow) {
		this.approvedRow = approvedRow;
	}
	public String[] getDeletedRow() {
		return deletedRow;
	}
	public void setDeletedRow(String[] deletedRow) {
		this.deletedRow = deletedRow;
	}
	public String[] getUpdatedRow() {
		return updatedRow;
	}
	public void setUpdatedRow(String[] updatedRow) {
		this.updatedRow = updatedRow;
	}
	public String[] getPostedDate() {
		return postedDate;
	}
	public void setPostedDate(String[] postedDate) {
		this.postedDate = postedDate;
	}
	public float[] getAmount() {
		return amount;
	}
	public void setAmount(float[] amount) {
		this.amount = amount;
	}
	public String getNonApproved() {
		return nonApproved;
	}
	public void setNonApproved(String nonApproved) {
		this.nonApproved = nonApproved;
	}
	public String getPageNum() {
		return pageNum;
	}
	public void setPageNum(String pageNum) {
		this.pageNum = pageNum;
	}
	public String getPageSize() {
		return pageSize;
	}
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}
	public String getSortByIndex() {
		return sortByIndex;
	}
	public void setSortByIndex(String sortByIndex) {
		this.sortByIndex = sortByIndex;
	}
	public float[] getOrigAmount() { return origAmount; }
	public void setOrigAmount(float[] v) { origAmount = v; }

}
