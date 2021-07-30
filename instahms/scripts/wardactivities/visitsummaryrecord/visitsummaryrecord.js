function init() {
	captureSubmitEvent()
	complaintForm = document.VisitSummaryRecord;
	initLoginDialog();
}
var collapsiblePanels = {};

function captureSubmitEvent() {
	var form = document.VisitSummaryRecord;
	form.validateFormSubmit = form.submit;

	form.submit = function validatedSubmit() {
		if (!blockSubmit()) {
			var e = xGetElementById(document.VisitSummaryRecord);
			YAHOO.util.Event.stopEvent(e);
			return false;
		}
		form.validateFormSubmit();
		return true;
	};
}
function blockSubmit() {
	if (document.getElementById('patient_discharged').value == 'true') {
		alert("Patient is inactive or discharged, editing of Visit Summary is not allowed.");
		return false;
	}
	return true;
}

// this method gets called from shared login dialog.
function submitHandler() {
	document.getElementById('authUser').value = document.getElementById('login_user').value;
	document.VisitSummaryRecord.submit();
	return false;
}

function validateSysGenForms() {
	var forms_exists = document.getElementsByName('sys_gen_section_id');
	for (var i=0; i<sys_generated_forms.length; i++) {
		if (sys_generated_forms[i].form_mandatory) {
			for (var j=0; j<forms_exists.length; j++) {
				if (forms_exists[j].value == sys_generated_forms[i].form_id) {
					// component exists for this consultation.
					if (sys_generated_forms[i].form_name == 'Complaint (Sys)') {
						if (!complaintEntered()) {
							alert("Please enter atleast one complaint.");
							return false;
						}
					} else if (sys_generated_forms[i].form_name == 'Allergies (Sys)') {
						if (!allergyEntered()) {
							alert("Please enter atleast one Allergy.");
							return false;
						}
					}
					// diagnosis details checked only when the mod_mrd_icd is enabled.
				}
			}
		}
	}
	return true;
}

// this method called when user click on save button.
function chkFormDetailsEdited(printVisitSummary) {
	document.getElementById('printVisitSummary').value = printVisitSummary;
	if (!validateComplaint()) return false;
	if (!validateSysGenForms()) return false;
	// validate mandatory fields in physician forms.
	if (!validateMandatoryFields()) return false;
	if (!validateAllergies()) return false;


	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.VisitSummaryRecord.submit();
	}
	// do not put any code after this line.
	return true;
}

