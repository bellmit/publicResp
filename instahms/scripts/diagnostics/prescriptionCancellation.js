
function validate() {
var form = document.forms.TestprescriptionCancellation;
var cancelledBy = document.getElementById('cancelledBy');
document.getElementById('cancelledDate').value = document.getElementById('toDate').value;
	if(form._method.value == 'cancellPrescriptionDetails') {
		if(cancelledBy.value == '') {
			showMessage("js.laboratory.radiology.canceltests.enter.cancelledname");
			cancelledBy.focus();
			return false;
		}
	}
	if(document.getElementById("testNamestable").rows.length == 1) {
		showMessage("js.laboratory.radiology.canceltests.notest.cancel");
		return false;
	}
}
