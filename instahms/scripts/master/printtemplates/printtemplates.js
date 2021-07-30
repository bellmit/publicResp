function doReset() {
	var reason = document.customform.reason.value;
	if (reason != '' && reason.length >= 500) {
		alert("Reason for Customization should not exceed 500 chars");
		document.customform.reason.focus();
		return false;
	}
	document.customform.customized.value = false;
	document.customform.resetToDefault.value = true;
	document.customform.submit();
}

function doSave() {
	var reason = document.customform.reason.value;
	if (reason == '') {
		alert('Please enter the Reason for Customization');
		document.customform.reason.focus();
		return false;
	} else if (reason.length >= 500) {
		alert("Reason for Customization should not exceed 500 chars");
		document.customform.reason.focus();
		return false;
	}
	document.customform.customized.value = true;
	document.customform.submit();
}