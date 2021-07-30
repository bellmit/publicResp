function doSaveReceiveSampleDetails() {
	if (document.receiveSamplesDetailsForm._sample_receive.checked) {
		if (document.receiveSamplesDetailsForm.receiptDate.value == "" || document.receiveSamplesDetailsForm.receiptTime.value == "") {
			showMessage("js.laboratory.receivesample.receiptdatetime");
			if (document.receiveSamplesDetailsForm.receiptDate.value == "")
				document.receiveSamplesDetailsForm.receiptDate.focus();
			if (document.receiveSamplesDetailsForm.receiptTime.value == "")
				document.receiveSamplesDetailsForm.receiptTime.focus();
			return false;
		} else {
			if (!doValidateDateField(document.receiveSamplesDetailsForm.receiptDate, 'past')) return false;
			if (!validateTime(document.receiveSamplesDetailsForm.receiptTime)) return false;
		}
	}
	if (document.receiveSamplesDetailsForm.receiptOtherDetails.value.length > 2000) {
		showMessage("js.laboratory.receivesample.charsallowed.otherdetails")
		document.receiveSamplesDetailsForm.receiptOtherDetails.focus();
	    return false;
	}
	document.receiveSamplesDetailsForm.action = cpath +"/Laboratory/ReceiveSamples.do?_method=saveReceiveSamplesDetails";
	document.receiveSamplesDetailsForm.submit();
}

function receiveSampleTime() {

	if (document.receiveSamplesDetailsForm._sample_receive.checked) {
		document.receiveSamplesDetailsForm.receiptDate.value = curDate;
		document.receiveSamplesDetailsForm.receiptTime.value = curTime;
	}else {
		document.receiveSamplesDetailsForm.receiptDate.value = '';
		document.receiveSamplesDetailsForm.receiptTime.value = '';
	}
}