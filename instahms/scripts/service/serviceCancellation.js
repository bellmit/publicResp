
function validate() {
var form = document.forms[0];
	if(form.method.value == 'cancellPrescriptionDetails') {
		if(form.cancelledBy.value == '') {
			alert("Enter the cancelled by name");
			form.cancelledBy.focus();
			return false;
		}
	}
	if(document.getElementById("testNamestable").rows.length == 1) {
		alert("No test to cancel");
		return false;
	}
}

