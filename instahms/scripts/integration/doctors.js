function checkIsDocSelected() {
	var checkBoxes = document.publishForm._selectDoctor;
	var anyChecked = false;
	var disabledCount = 0;
	var totalConsultations = 1;
	if (checkBoxes.length) {
		totalConsultations = checkBoxes.length;
		for (var i=0; i<checkBoxes.length; i++) {
			if (!checkBoxes[i].disabled && checkBoxes[i].checked) {
				anyChecked = true;
				break;
			}
		}

		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].disabled)
				disabledCount++;
		}

	} else {
		var checkBox = document.publishForm._selectDoctor;
		if (!checkBox.disabled && checkBox.checked)
			anyChecked = true;
		if (checkBox.disabled)
			disabledCount++;
	}
	if (!anyChecked) {
		if (disabledCount == totalConsultations) {
			showMessage("js.outpatientlist.patientsconsultation.cons.closed");
			return false;
		}
		alert("Check one or more doctors");
		return false;
	}

	//document.publishForm.submit();
}