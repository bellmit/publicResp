function validateForm(print) {
	if (!validateSysGenForms()) return false;
	if (!validateComplaint()) return false;
	if (!validateAllergies()) return false;
	// validate mandatory fields in physician forms.
	if (!validateMandatoryFields()) return false;

	if (comp_immunization == 'Y') {
		// validate the immunization status for below age group of 15 yrs.
		var validateImmunizationStatus = false;
		if (document.triageform.age_in.value == 'Y') {
			if (parseInt(document.triageform.age.value) < 15) {
				validateImmunizationStatus = true;
			}
		} else {
			validateImmunizationStatus = true;
		}
		if (validateImmunizationStatus
			&& !document.triageform.immunization_status_upto_date[0].checked
			&& !document.triageform.immunization_status_upto_date[1].checked) {
			showMessage("js.outpatient.triageform.selectimmunizationstatus");
			return false;
		}
	}

	var emergency_category = document.getElementsByName('emergency_category');
	var categorySelected = false;
	for (var i=0; i<emergency_category.length; i++) {
		if (emergency_category[i].checked) {
			categorySelected = true;
			break;
		}
	}
	if (!categorySelected) {
		showMessage("js.outpatient.triageform.selectcategory");
		document.triageform.emergency_category[0].focus();
		return false;
	}
	document.triageform.printTriage.value = print;
	document.triageform.submit();
	return true;
}

