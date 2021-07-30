function doSaveTransferDetails() {
	if (document.transferSamplesDetailsForm._sample_transferred.checked) {
		if (document.transferSamplesDetailsForm.outsource_id.value == "") {
			showMessage("js.laboratory.transfersample.selectoutsource");
			document.transferSamplesDetailsForm.outsource_id.focus();
			return false;
		} else {
			document.transferSamplesDetailsForm.outsource_dest_id.value = document.transferSamplesDetailsForm.outsource_id.value;
		}
		if (document.transferSamplesDetailsForm.transferDate.value == "" || document.transferSamplesDetailsForm.transferTime.value == "") {
			showMessage("js.laboratory.transfersample.transferdatetime");
			if(document.transferSamplesDetailsForm.transferDate.value == "")
				document.transferSamplesDetailsForm.transferDate.focus();
			if(document.transferSamplesDetailsForm.transferTime.value == "")
				document.transferSamplesDetailsForm.transferTime.focus();
			return false;
		} else {
			if (!doValidateDateField(document.transferSamplesDetailsForm.transferDate, 'past')) return false;
			if (!validateTime(document.transferSamplesDetailsForm.transferTime)) return false;
			var transferDate = document.transferSamplesDetailsForm.transferDate.value;
			var transferTime = document.transferSamplesDetailsForm.transferTime.value;
			var sampleDateTime = formatDateTime(new Date(sampleDate));
			var parts = sampleDateTime.split(" ");
			if(getDateTime(transferDate, transferTime) - getDateTime(parts[0], parts[1]) < 0) {
				showMessage("js.laboratory.transfersample.transfertimecheck");
				return false;
			}
		}
	}
	if (document.transferSamplesDetailsForm.transferOtherDetails.value.length > 2000) {
		showMessage("js.laboratory.transfersample.charsallowed.otherdetails")
		document.transferSamplesDetailsForm.transferOtherDetails.focus();
	    return false;
	}
	document.transferSamplesDetailsForm.action = cpath +"/Laboratory/TransferSamples.do?_method=saveTransferSamplesDetails";
	document.transferSamplesDetailsForm.submit();
}

function transferSampleTime() {

	if (document.transferSamplesDetailsForm._sample_transferred.checked) {
		document.transferSamplesDetailsForm.transferDate.value = curDate;
		document.transferSamplesDetailsForm.transferTime.value = curTime;
	}else {
		document.transferSamplesDetailsForm.transferDate.value = '';
		document.transferSamplesDetailsForm.transferTime.value = '';
	}
}