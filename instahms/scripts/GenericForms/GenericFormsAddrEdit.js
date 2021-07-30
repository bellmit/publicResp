function init() {
	complaintForm = document.patient_generic_form;
}

function saveGenericForm(printGenericForm) {
	document.getElementById('printGenericForm').value = printGenericForm;
	if (!validateComplaint()) return false;
	if (!validateSysGenForms()) return false;
	// validate mandatory fields in physician forms.
	if (!validateMandatoryFields()) return false;
	if (!validateAllergies()) return false;

	document.patient_generic_form.submit();
	return true;
}