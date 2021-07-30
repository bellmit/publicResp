function populateBedTypes() {
	var bedTypeObj = document.bedform.bed_type;
		loadSelectBox(bedTypeObj, bedTypes, 'bed_type_name', 'bed_type_name',
				'--Select--');
}

function onChangeBedType(){
	var selectedbedtype = document.forms[0].bed_type.options[document.forms[0].bed_type.selectedIndex].value;
	var url = 'ShiftBed.do?_method=getWardNames&bed_type='+selectedbedtype;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
			getresponsebedcontentforshift(eval('('+reqObject.responseText+')'));
		}
	}
	return null;
}

function getresponsebedcontentforshift(wardsList){
	var selectedbedtype = document.forms[0].bed_type.options[document.forms[0].bed_type.selectedIndex].value;
	var wards = document.forms[0].ward_no;
	var wardList = wardsList;
	wards.length=1;
    wards.options[wards.length-1].value = "";
	var freeBeds = 0;
	for(var i = 0;i < wardList.length;i++){
		if(selectedbedtype == wardList[i].bed_type){
			wards.length = wards.length + 1;
			wards.options[wards.length-1].text = wardList[i].ward_name;
			wards.options[wards.length-1].value = wardList[i].ward_no;

			freeBeds = freeBeds+wardList[i].freebeds;
		}
	}
	if(freeBeds == 0 ) {
		if (selectedbedtype == "")
			return false;
		alert("No free beds to allocate");
		return false;
	}

}

function changewardname(){
 		var selectedbedtype = document.forms[0].bed_type.options[document.forms[0].bed_type.selectedIndex].value;
  	    var url = 'ShiftBed.do?_method=getBednamesForWard&bedType='+document.forms[0].bed_type.value+'&wardNo='+document.forms[0].ward_no.value;
  	    var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
				getresponsebedcontents(eval('('+reqObject.responseText+')'));
			}
		}
		return null;
}


function getresponsebedcontents(bedsList){

	var beds = bedsList;
	var objlist = document.forms[0].bed_id;
	objlist.length=1;
	objlist.options[objlist.length-1].value = "";

 	for(var i = 0;i < beds.length;i++){
		objlist.length = objlist.length + 1;
		objlist.options[objlist.length-1].text = beds[i].bed_name;
		objlist.options[objlist.length-1].value = beds[i].bed_id;
	}
}
function shiftBed(){
	var start_date = document.getElementById("start_date");
	var start_time = document.getElementById("start_time");

	var curr_date = document.getElementById("curr_date");
	var curr_time = document.getElementById("curr_time");

	var reg_date = document.getElementById("prev_date");
	var reg_time = document.getElementById("prev_time");

	if(!validateAdmissionDateTime(start_date,start_time,curr_date,curr_time,reg_date,reg_time)) return false;

	var covert = '';
	if(validate()){
		document.forms[0].action = "ShiftBed.do?method=shiftBed";
		document.forms[0].submit();
	}
}
function validate(){
   if(document.forms[0].ward_no.value == ""){
	   	alert("Select ward");
	   	return false;
   }
    if(document.forms[0].bed_type.value == ""){
	   	alert("Select bedtype");
	   	return false;
   }
    if(document.forms[0].bed_id.value == ""){
	   	alert("Select bed");
	   	return false;
   }
    if(document.forms[0].estimated_days.value == ""){
	   	alert("Enter expected days");
	   	return false;
   }
   if(document.getElementById("start_date").value == ''){
	   	alert("Shift day can not be empty");
	   	document.getElementById("shift_date").focus();
   }else{
		 if (!doValidateDateField(document.getElementById("start_date")))
	                return false;
	        var msg = validateDateStr(document.getElementById("start_date").value);
	        if (msg != null && msg!=""){
	               alert(msg);
	               return false;
	         }
		}
    if(document.getElementById("start_time").value == ''){
	   	alert("Start time can not be empty");
	   	document.getElementById("start_time").focus();
   }else{
		if (!validateTime(document.getElementById("start_time"))){
				return false;
			}

   }
    //var tpa_id = document.forms[0].tpa_id.value;
  	// if(tpa_id != null && tpa_id!=""){
	//	if(!validateInsAmount(document.forms[0].shiftexpecteddays.value))
	//		return false;
	//}
	if(!checkDutyDoctor(document.getElementById("bed_type").value,document.getElementById("duty_doctor_id").value)){
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
  return true;

}
function checkDutyDoctor(allocatingBedType,doctor_id){
	if(dutyDoctroSelectionFor == 'N')
		return true;
	var bedType = 'A';
	if(findInList(nonICUBedTypes,'bed_type',allocatingBedType) == null)
		bedType = 'I';
	if(dutyDoctroSelectionFor == 'A'){
		if(doctor_id == ''){
			return false;
		}
	}else{
		if(dutyDoctroSelectionFor == bedType){
			if(doctor_id == '')
				return false;
		}
	}
	return true;
}