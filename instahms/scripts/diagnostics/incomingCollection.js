function validate(action) {
    document.IncomingPendingBillForm.action.value = action;

    if (action == 'Pay' || action == 'Save') {
        if (!validatePay()) return false;
    }
    if (action == 'Reopen') {
        if (!validateReopen()) return false;
    }
    if (action == 'Print') {}

    document.IncomingPendingBillForm.submit();
    return true;
}

function validatePay() {

    var valid = true;

	valid = valid && validatePayDates();
	valid = valid && validateCounter();
	valid = valid && validatePaymentRefund();
	valid = valid && validatePaymentTagFields();
	valid = valid && validateAllNumerics();
    valid = valid && validatePaymentAmount();

    return valid;
}

function validateReopen() {

	if ( (null != document.IncomingPendingBillForm.billRemarks) && (
		('' == trimAll(document.IncomingPendingBillForm.billRemarks.value)) ||
		(trimAll(document.IncomingPendingBillForm.oldRemarks.value) == trimAll(document.IncomingPendingBillForm.billRemarks.value)))) {
		alert("Enter Remarks for reopening bill.");
		document.IncomingPendingBillForm.billRemarks.focus();
		return false;
	}
    return true;
}

// TO DO : check this write -off
function validatePatientPayment() {
    var billtype = document.IncomingPendingBillForm.billType.value;
    var billstatus = document.IncomingPendingBillForm.billStatus.value;

    var patientAmt = getPaise(document.getElementById("totalLable").textContent);
    var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund');

    var existingReceiptAmt = getPaise(existingReceipts);

    payingAmt = existingReceiptAmt + payingAmt;

    if (patientAmt == payingAmt) {
        document.IncomingPendingBillForm.close.checked = true;
        return true;
    }

    if (billtype == 'C' && writeOffAmountRights == 'A') {
        if (patientAmt < payingAmt && document.IncomingPendingBillForm.close.checked) {
            var ok = confirm("Warning: total bill amount and net payment amount do not match.\n" +
            		"The balance amount will be considered as Write-Off. Do you want to proceed?");
            if (!ok) {
                document.IncomingPendingBillForm.close.checked = false;
                return false;
            } else {
                document.IncomingPendingBillForm.close.checked = true;
            }
        } else if (patientAmt > payingAmt && document.IncomingPendingBillForm.close.checked) {
            alert("Total bill amount exceeds net payment amount.\n" +
            	"Cannot close the bill. Please refund the excess amount.");
            document.IncomingPendingBillForm.close.checked = false;
        }
    } else if (billtype == 'C' && document.IncomingPendingBillForm.close.checked) {
        alert("Total bill amount and net payment amount do not match.\n" +
        		"You are not authorized to Write-Off the balance amount.");
        document.IncomingPendingBillForm.close.checked = false;
        return false;
    } else if (billtype == 'P') {
        document.IncomingPendingBillForm.close.checked = true;
        // write-off not allowed for prepaid bills at all
        alert("Total bill amount and net payment amount do not match.\n" + "Cannot close the bill.");
        document.IncomingPendingBillForm.close.checked = false;
        return false;
    }

    return true;
}

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 */

function resetPayments() {

    resetTotalsForPayments();

    // for bill now bill, auto-set the amount to be paid by patient.
    if (document.IncomingPendingBillForm.billType.value == 'P') {
        // if a single payment mode exists, update that with the due amount automatically
        setTotalPayAmount();
    }
}

function getTotalAmount() {
    return getPaise(document.getElementById("amountdue").value);
}

function getTotalAmountDue() {
    return getPaise(document.getElementById("amountdue").value);
}