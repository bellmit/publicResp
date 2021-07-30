/*
 * Used by AllocateBed.jsp as well as ShiftBed.jsp
 */
function filterDuplicates(list)
{
	var count = 0;
	var listvalues = new Array();
	var newlist = new Array();

	listvalues = list;
	var hash = new Object();
	for (var i=0; i<listvalues.length; i++){
		if (hash[listvalues[i].ward_name.toLowerCase()] != 1){
			newlist = newlist.concat(listvalues[i]);
			hash[listvalues[i].ward_name.toLowerCase()] = 1
		}else {
			count++;
		}
	}
	return newlist;
}
function populateBedTypes() {
	var bedTypeObj = document.bedform.bed_type;
	var bedType = "N";
	if (isBystander) {
		loadSelectBox(bedTypeObj, filterList(bedTypes, "is_icu", bedType),
				"bed_type_name", "bed_type_name", '-- Select --', '');
	} else {
		loadSelectBox(bedTypeObj, bedTypes, 'bed_type_name', 'bed_type_name',
				'--Select--');
	}
}
var wardList;
function onChangeBedType(){
	document.bedform.ward_no.value = '';
	document.bedform.bed_id.length = 1;
	
	var bedTypeObj = document.bedform.bed_type;
	var wardObj = document.bedform.ward_no;
	var selectedward = wardObj.value;
	var selectedbedtype = document.bedform.bed_type.value;


	// Empty wards
	wardObj.length = 1;
	wardObj.options[wardObj.length - 1].text = "--Select--";
	wardObj.options[wardObj.length - 1].value = "";

	if (selectedbedtype != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath
				+ '/pages/registration/regUtils.do?_method=getWardnamesForBedType&selectedbedtype='
				+ encodeURIComponent(selectedbedtype);
		var freebeds = 0;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var wards =" + ajaxobj.responseText);
					if (wards != null && wards != '') {
						loadSelectBox(document.bedform.ward_no, wards, 'ward_name', 'ward_no', '-- Select --', '');
						wardList = wards;
					}
				}
			}
		}
		refineChargedBedTypes();
	}
	
	if(document.getElementById('duty_doc_star') != null){
		if ((ipPrefs.duty_doctor_selection == 'I' && bedTypeName == 'ICU') || ipPrefs.duty_doctor_selection == 'A')
			document.getElementById('duty_doc_star').style.visibility = 'visible';
		else
			document.getElementById('duty_doc_star').style.visibility = 'hidden';
	}
}

function refineChargedBedTypes() {
	var bedType = findInList(allbedtypesJSON, "bed_type_name", document.bedform.bed_type.value);
	if(canSetChargedBedType)
		loadSelectBox(document.bedform.charged_bed_type,
				filterList(bedTypes, "is_icu", bedType.is_icu), "bed_type_name", "bed_type_name",
				'-- Select --','');
	document.bedform.charged_bed_type.value = document.bedform.bed_type.value;
	if(document.getElementById("chargedBedL"))
		document.getElementById("chargedBedL").innerHTML = document.bedform.bed_type.value;
}

function changewardname() {
  var ward_no = document.bedform.ward_no.value;
	var filterdWards = wardList.filter(ward => ward.ward_no === ward_no);
	var flag =0;
	if(!isBystander && ward_no !=''){
    if(filterdWards[0].allowed_gender !== 'ALL' && patientGender !== filterdWards[0].allowed_gender && !(patientGender === 'C' || patientGender === 'O' || patientGender === 'N')){
      var replaceWord;
            if(filterdWards[0].allowed_gender === 'F'){
               replaceWord = 'Female';
            }
            if(filterdWards[0].allowed_gender === 'M'){
             replaceWord = 'Male';
            }
      alert(getString('js.registration.patient.not.valid.ward.for.gender', replaceWord));
      document.bedform.ward_no.value = '';
      document.bedform.bed_id.value = '';
      flag =1;
    }
  }
  if(flag == 1 || ward_no =='') {
     loadSelectBox(document.bedform.bed_id, [], 'bed_name', 'bed_id',
      			'-- Select --', '');
    } else {
      var bedsOfSelectedBedtype = filterList(gFreeBeds, "bed_type", document.bedform.bed_type.value);
      var bedsOfSelectedWard = filterList(bedsOfSelectedBedtype, "ward_no", ward_no);
      loadSelectBox(document.bedform.bed_id, bedsOfSelectedWard, 'bed_name', 'bed_id',
    			'-- Select --', '');
  }
}

function allocateBed() {
	if (validate() && validateAllocate()) {
		document.bedform.submit();
	}
}

function shiftBed() {
	if (validate() && validateShift()) {
		document.bedform.submit();
	}
}

function validate() {
	var theForm = document.bedform;
	var valid = true;
	valid = valid && validateRequired(theForm.bed_type, "Select Bed Type");
	valid = valid && validateRequired(theForm.ward_no, getString("js.registration.patient.ward"));
	valid = valid && validateRequired(theForm.bed_id, getString("js.registration.patient.bed.name"));
	valid = valid && validateRequired(theForm.charged_bed_type, "Select Charged Bed Type");

	valid = valid && validateRequired(theForm.start_date_dt, "Enter Start Date");
	valid = valid && validateRequired(theForm.start_date_tm, "Enter Start Time");
	valid = valid && doValidateDateField(theForm.start_date_dt);
	valid = valid && doValidateTimeField(theForm.start_date_tm);
	valid = valid && doValidateDateField(theForm.end_date_dt);
	valid = valid && doValidateTimeField(theForm.end_date_tm);

	if(theForm.end_date_dt.value != '')
		valid = valid &&
			validateFromToDateTime(theForm.start_date_dt, theForm.start_date_tm, theForm.end_date_dt, theForm.end_date_tm, true, true);

	if(theForm.is_bystander)
		valid = valid && validateDutyDoctor();
	valid = valid && validateRemarks();
	//valid = valid && validateCreditLimitRule();
	return valid;
}
function validateAllocate() {
	var valid = true;
	valid = valid && validateCreditAmount();
	valid = valid && validateAdmissionDate();
	if(valid && !empty(prvStartDate))
		valid = validateBystanderDate();
	return valid;
}

function validateShift() {
	var valid = true;
	valid &= validateAdmissionDate();
	valid &= validateShiftDate();
	return valid;
}

function validateCreditAmount() {
	var totalcharge = 0;
 		var credit_amount = 0;
 		for(var i = 0;i<normalbed_payments.length;i++){
 			var normalBedCharges = normalbed_payments[i];
 			if(document.bedform.bed_id.value == normalBedCharges.BED_ID)
				totalcharge = getPaise(normalBedCharges.BED_CHARGE)+
							  getPaise(normalBedCharges.DUTY_CHARGE)+
							  getPaise(normalBedCharges.NURSING_CHARGE)+
							  getPaise(normalBedCharges.MAINTAINANCE_CHARGE)+
							  (getPaise(normalBedCharges.BED_CHARGE)*getPaise(normalBedCharges.LUXARY_TAX))/getPaise(100);
 		}

 		if(billDetails != null && !empty(billDetails)){
			credit_amount = getPaise(balance) + getPaise(billDetails.APPROVAL_AMOUNT) +getPaise(billDetails.DEPOSIT_SET_OFF);
		}

		var startDate = getDateTimeFromField(document.bedform.start_date_dt, document.bedform.start_date_tm);
		var endDate = getDateTimeFromField(document.bedform.end_date_dt, document.bedform.end_date_tm);

		if((totalcharge*parseInt(daysDiff(startDate, endDate))) > credit_amount){
			var ok = confirm("Bed Charge is greater than Existing Credits \n" +
						"Do you want to proceed?");
			if (!ok){
				return false;
			}
		}
	return true;
}

function validateDutyDoctor() {
	if (ipPrefs.duty_doctor_selection == 'N')
		return true;

	var bedTypeName = document.bedform.bed_type.value;
	var bedType = findInList(allbedtypesJSON, 'bed_type_name', bedTypeName);

	// no check required if check type if ICU only and bed is not icu
	if (ipPrefs.duty_doctor_selection == 'I' && bedType.is_icu == 'N')
		return true;

	if(document.bedform.duty_doctor_id)
		return validateRequired(document.bedform.duty_doctor_id, "Duty Doctor is required")

	return true;
}

function validateRemarks() {
	if (ipPrefs.force_remarks != 'Y')
		return true;
	return validateRequired(document.bedform.remarks, "Remarks is required");
}

function validateAdmissionDate() {
	var regDateTime = getDateTime(regDate, regTime);
	var admDateTime = getDateTimeFromField(document.bedform.start_date_dt,
			document.bedform.start_date_tm);

	if (admDateTime < regDateTime) {
		alert("Start date/time cannot be earlier than admission date/time (" + regDate + " " + regTime + ")");
		document.bedform.start_date_dt.focus();
		return false;
	}
	return true;
}

function validateShiftDate(){

		var prvBedDate = getDateTime(prvStartDate,prvStartTime);
		var admDateTime = getDateTimeFromField(document.bedform.start_date_dt,
				document.bedform.start_date_tm);

		if (admDateTime < prvBedDate) {
			alert("Start date/time cannot be earlier than previous Bed start date/time (" + prvStartDate + " " + prvStartTime + ")");
			document.bedform.start_date_dt.focus();
			return false;
		}
	return true;
}

function validateBystanderDate(){

		var regDateTime = getDateTime(regDate, regTime);
		var admDateTime = getDateTimeFromField(document.bedform.start_date_dt,
				document.bedform.start_date_tm);

		if (admDateTime < regDateTime) {
			alert("Start date/time cannot be earlier than admission date/time (" + regDate + " " + regTime + ")");
			document.bedform.start_date_dt.focus();
			return false;
		}
	return true;
}

/*
function validateCreditLimitRule() {
	//Credit limit rule is applicable for IP visits only
	if(visitType != 'i' || creditLimitDetailsJSON == undefined || creditLimitDetailsJSON == null) {
		return true;
	}
	
	var visitPatientDuePaise = getPaise(visitTotalPatientDue);
	var availableCreditLimit = parseFloat(creditLimitDetailsJSON.availableCreditLimit);
	if(ip_credit_limit_rule == 'B') {
		if(!(availableCreditLimit > 0)) {
			var msg=getString("js.billing.billlist.and.below.currentoutstanding");
			msg+=' '+ formatAmountPaise(visitPatientDuePaise);
			msg+="\n";
			msg+=getString("js.billing.billlist.ipcreditlimitis");
			msg+=' '+ formatAmountValue(availableCreditLimit);
			alert(msg);
			return false;
		}
	} else if (ip_credit_limit_rule == 'W') {
		if(!(availableCreditLimit > 0)) {
			var msg=getString("js.billing.billlist.and.below.currentoutstanding");
			msg+=' '+ formatAmountPaise(visitPatientDuePaise) ;
			msg+="\n";
			msg+=getString("js.billing.billlist.ipcreditlimitis");
			msg+=' '+ formatAmountValue(availableCreditLimit);
			msg+="\n";
			msg+=getString("js.billing.billlist.doyouwanttoproceed");
			var ok = confirm(msg);
			if(!ok)
				return false;
		}
	}
	return true;
}
*/