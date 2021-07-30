var patientDetails = null;
function initAllocateBed() {
	refineChargedBedTypes();
	Insta.initVisitAcSearch(cpath, 'patient_id', 'patientIdContainer', 'active', 'i',
	getPatientDetails, null,false, 'visit', true);
	if(patientId != null && bedName == '')
		alertInitialPaymentAdvance();
}

function getPatientDetails() {
	document.forms[0]._method.value = "getAllocateBedScreen";
	document.forms[0].submit();
}

function changeDaysToHrs(checked){
	if(checked.checked){
		document.getElementById("dayhrth").innerHTML = 'Hrs';
		dayorhr = 'Estimated Hrs';
		}
	else{
	    document.getElementById("dayhrth").innerHTML = 'Days';
	    dayorhr = 'Estimated Days';
	    }
}

function alertInitialPaymentAdvance(){
	var bedtype = false;
	if(balance < 0){
		alert("Bed initial payment is exceeding the balance amount in bill");
		return false;
	}else{
		for(var i = 0;i<normalbed_payments.length;i++){
			if(document.forms[0].bed_type.value == normalbed_payments[i].BED_TYPE){
				if(parseFloat(normalbed_payments[i].INITIAL_PAYMENT) > parseFloat(balance) && !bedtype){
					bedtype = true;
					alert("Bed initial payment is exceeding the balance amount in bill");
					return false;
				}
			}
		}
		if(!bedtype){
			for(var i = 0;i<icubed_payments.length;i++){
			if(document.forms[0].bed_type.value == icubed_payments[i].INTENSIVE_BED_TYPE){
				if(parseFloat(icubed_payments[i].INITIAL_PAYMENT) > parseFloat(balance) && !bedtype){
					bedtype = true;
					alert("Bed initial payment is exceeding the balance amount in bill");
					return false;
				}
			}
			}
		}
	}
}

function validateAllocation(){
	var startDateFld = document.getElementById("start_date_dt");
	var startTimeFld = document.getElementById("start_date_tm");
	var currentDateFld = document.getElementById("current_date");
	var currentTimeFld = document.getElementById("current_time");
	var regDateFld = document.getElementById("reg_date");
	var regTimeFld = document.getElementById("reg_time");
	if(!checkDutyDoctor(document.bedviewform.bed_type.value,document.bedviewform.duty_doctor_id.value)){
		alert("Select Duty Doctor");
		return false;
	}
	if(forceRemarks == 'Y'){
		if(document.getElementById("remarks").value == ''){
			alert("Please Enter remarks");
			document.getElementById("remarks").focus();
			return false;
		}
	}
	if(validateFields()){

		if(!validateBedAdmissionDate()) return false;
		document.forms[0].action = "./AllocateBed.do?_method=allocateBed";
		document.forms[0].method = "POST";
		document.forms[0].submit();
	}
}

function validateFields(){
	var form = document.bedviewform;
	var valid = true;

	valid = valid && validateRequired(form.patient_id, "Select patient");
	valid = valid && validateRequired(form.charged_bed_type, "Select Charged Bed Type");
	valid = valid && validateRequired(form.estimated_days, "Enter Estimated Days");
	valid = valid && validateRequired(form.start_date_dt, "Enter Start Date");
	valid = valid && validateRequired(form.start_date_tm, "Enter Start Time");

	valid = valid && doValidateDateField(document.getElementById("start_date_dt"));
  	valid = valid && validateTime(document.getElementById("start_date_tm"));
  	valid = valid && doValidateDateField(document.getElementById("end_date_dt"));
  	valid = valid && validateTime(document.getElementById("end_date_tm"));

   if(tpaId != null && tpaId !="")
		valid = valid && checkInsAmount(document.forms[0].estimated_days.value);
	return valid;
}

function checkBillStatus(visitid){
	var ajaxobj = newXMLHttpRequest();
	var url = cpath+'/pages/ipservices/Ipservices.do?_method=ajaxCreditBillCheck&visitId='+visitid;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			if (reqObject.responseText != 'CreditBillExists') {
					alert("Patient does not have open credit bill");
					return false;
				}
			}
		}
	return true;
}

function checkInsAmount(NoOfDays){
		var totalcharge = 0;
		var credit_amount = 0;
		for(var i = 0;i<normalbed_payments.length;i++){
			if(beddetailsJSON.bedname == normalbed_payments[i].BED_NAME)
			totalcharge = getPaise(normalbed_payments[i].BED_CHARGE)+getPaise(normalbed_payments[i].DUTY_CHARGE)+
			getPaise(normalbed_payments[i].NURSING_CHARGE)+getPaise(normalbed_payments[i].LUXARY_TAX);
		}
	for(var i=0;i<billDetails.length;i++){
		if(billno = billDetails[i].BILL_NO){
			credit_amount = getPaise(balance) + getPaise(billDetails[i].APPROVAL_AMOUNT) +getPaise(billDetails[i].DEPOSIT_SET_OFF);
		}
	}

	if((totalcharge*NoOfDays) > credit_amount){
		var ok = confirm("Bed Charge is greater than Existing Credits \n" +
					"Do you want to proceed?");
		if (!ok){
			return false;
		}
	}
	return true;
}

function validateBedAdmissionDate() {
	// check if adm date is > reg date
	var regDateTime = getDateTimeFromField(document.bedviewform.reg_date,document.bedviewform.reg_time);
	var admDateTime = getDateTimeFromField(document.bedviewform.start_date_dt,document.bedviewform.start_date_dt);

	if (admDateTime < regDateTime) {
		alert("Start date/time cannot be earlier than admission date/time (" + regDate + " " + regTime + ")");
		document.bedviewform.start_date_dt.focus();
		return false;
	}
	return true;
}

function refineChargedBedTypes() {
	var bedType = findInList(gBedTypes, "bed_type_name", document.bedviewform.bed_type.value);
	if(canSetChargedBedType)
		loadSelectBox(document.bedviewform.charged_bed_type,
				filterList(gBedTypes, "is_icu", bedType.is_icu), "bed_type_name", "bed_type_name",
				'-- Select --','');
	document.bedviewform.charged_bed_type.value = document.bedviewform.bed_type.value;
	if(document.getElementById("chargedBedL") != null)
		document.getElementById("chargedBedL").innerHTML = document.bedviewform.bed_type.value;
}
