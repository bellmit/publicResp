/*
 * Functions used by BillList.jsp
 */
var theForm = document.ReceiptSearchForm;

function init() {
	theForm = document.ReceiptSearchForm;
	enableClaimStatus();
	enableBillStatus();
	enablePatientType();
}

function clearSearch() {
	theForm.fdate.value = "";
	theForm.tdate.value = "";
	theForm.claimAll.checked = true;
	theForm.statusAll.checked = true;
	theForm.typeAll.checked = true;
	theForm.patientAll.checked = true;
	theForm.mrno.value = "";
	theForm.billNo.value = "";
	enableMainType();
	enableBillStatus();
	enableBillType();
	enablePatientType();
}

/*
 * Complete the MRNO
 */
function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = theForm.mrno;

	// complete
	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		theForm.mrno.value = ""
		theForm.mrno.focus();
		return false;
	}
}

/*
 * Complete the Bill No.
 */
function onKeyPressBillNo(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeBillNo();
	} else {
		return true;
	}
}

function onChangeBillNo() {
	var billNoBox = theForm.billNo;

	// complete
	var valid = addPrefix(billNoBox, gBillNoPrefix, gBillNoDigits);

	if (!valid) {
		alert("Invalid Bill No. Format");
		theForm.billNo.value = ""
		theForm.billNo.focus();
		return false;
	}
}

function enableClaimStatus() {
	var disabled = theForm.claimAll.checked;

	theForm.claimOpen.disabled = disabled;
	theForm.claimSent.disabled = disabled;
	theForm.claimReceived.disabled = disabled;
}

function enableBillStatus() {
	var disabled = theForm.statusAll.checked;

	theForm.statusOpen.disabled = disabled;
	theForm.statusFinalized.disabled = disabled;
	theForm.statusSettled.disabled = disabled;
	theForm.statusClosed.disabled = disabled;
	theForm.statusCancelled.disabled = disabled;
}

function enableBillType() {
	var disabled = theForm.typeAll.checked;

	theForm.typeBillNow.disabled = disabled;
	theForm.typeBillLater.disabled = disabled;
	theForm.typePharmacy.disabled = disabled;
	theForm.typePharmacyReturn.disabled = disabled;
}

function enablePatientType() {
	var disabled = theForm.patientAll.checked;

	theForm.patientIp.disabled = disabled;
	theForm.patientOp.disabled = disabled;
	theForm.patientRetail.disabled = disabled;
}

function doSearch() {
	if (!doValidateDateField(theForm.fdate))
		return false;
	if (!doValidateDateField(theForm.tdate))
		return false;
	return true;
}
