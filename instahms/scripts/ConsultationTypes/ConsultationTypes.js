
 var toolbar = {
	Edit: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/consultTypes.do?_method=edit',
		onclick: null,
		description: "View and/or Edit Consultation Type Details"
		}
 };

 function init() {
 	if (document.getElementById('doctorChargeType')!=null && document.getElementById('doctorChargeType').value!="") {
 		setSelectedIndex(document.forms[0].doctor_charge_type, document.getElementById('doctorChargeType').value);
 	}
 	if (document.getElementById('chargeHeadId')!=null && document.getElementById('chargeHeadId').value!="") {
 		setSelectedIndex(document.forms[0].charge_head, document.getElementById('chargeHeadId').value);
 	}
 }
 
 function validate() {
 	if (document.getElementById('consultation_type').value=="") {
 		alert("Consultation Type Name is required");
 		document.getElementById('consultation_type').focus();
 		return false;
 	}
 	if (document.getElementById('doctor_charge_type').selectedIndex==0) {
 		alert("Doctor charge Type is required");
 		document.getElementById('doctor_charge_type').focus();
 		return false;
 	}
 	if (document.getElementById('charge_head').selectedIndex==0) {
 		alert("Charge Head is required");
 		document.getElementById('charge_head').focus();
 		return false;
 	}
 	if (document.getElementById('patient_type').selectedIndex==0) {
 		alert("Patient Type is required");
 		document.getElementById('patient_type').focus();
 		return false;
 	}

	var isInsuranceCatIdSelected = false;
	var insuranceCatId = document.getElementById('insurance_category_id');
	for (var i=0; i<insuranceCatId.options.length; i++) {
	  if (insuranceCatId.options[i].selected) {
		  isInsuranceCatIdSelected = true;
	  }
	}
	if (!isInsuranceCatIdSelected) {
		alert("Please select at least one insurance category");
		return false;
	}

 	if(document.getElementById('duration').value === '' || document.getElementById('duration').value === null){
 	    alert("Consultation Duration is Mandatory");
 	    document.getElementById('duration').focus();
        return false;
 	}
 	if(!(document.getElementById('duration').value === '' || document.getElementById('duration').value === null) && (document.getElementById('duration').value > 150 || document.getElementById('duration').value <= 0)){
         alert("Consultation Duration should be greater than 0 and cannot be more than 150 minutes");
         document.getElementById('duration').focus();
         return false;
    }

 	document.forms[0].submit();
 }

 function setSkipForFollowUpCount(){
	var skipForFollowup = document.ConsultationTypesForm.skip_for_followup_count;
	var skipForFollowupCheckbox = document.ConsultationTypesForm.skip_for_followup_count_checkbox;
	if (skipForFollowupCheckbox.checked) {
		skipForFollowup.value = 'Y';
	} else {
		skipForFollowup.value = 'N';
	}
 }