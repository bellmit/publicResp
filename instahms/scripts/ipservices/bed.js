
var wardAutoComplete;

function initEditDialog() {
	editDialog = new YAHOO.widget.Dialog("editDialog", {
					width:"650px",
					context :["", "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true,
				});
	editDialog.cfg.setProperty("context", ["myContextEl", "tr", "tl"]);

	editDialog.render();
	subscribeEscKeyEvent(editDialog);
}

function subscribeEscKeyEvent(dialog) {
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
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

function validateFinalizeDate(){

	var startDates = document.getElementsByName("start_date_dt");
	var endDate = document.getElementById("action_date")+" "
			document.getElementById("action_time");
	var bedState = document.getElementsByName("bed_state");

	var endDate = getDateTimeFromField(document.getElementById("action_date"), document.getElementById("action_time"));

	for(var i = 0;i<bedState.length;i++){
		if(bedState[i].value == 'O'){
		var startDate = getDateTimeFromField(document.getElementsByName("startDate")[i],document.getElementsByName("startTime")[i]);
		var diff = daysDiff(startDate, endDate );
			if(diff < 0){
				alert("Date&Time can not be less than any occupied beds Start Date&Time");
				return false;
			}
		}
	}
	return true;


}

function finaliseBedDetails(){
	var valid = true;

	valid = valid && validateFinalizeDate();

	var start_date = document.getElementById("action_date");
	var start_time = document.getElementById("action_time");

	var curr_date = document.getElementById("action_date");
	var curr_time = document.getElementById("action_time");

	var reg_date = document.getElementById("reg_date");
	var reg_time = document.getElementById("reg_time");

	valid = valid && validateAdmissionDate(start_date,start_time );

	valid = valid && validateEquipmentFinalization();
	if(document.ipbedform.daycare.value == 'Y'){
		valid = valid && warnExeedingDaycare();
	}
	valid = valid && confirm(getString("js.adt.beddetailsdate.finalize.lastaction")+"\n"+getString("js.adt.beddetailsdate.bedactivity.notallow"));
	return valid;
}


function validateEquipmentFinalization() {
	var ajaxobj = newXMLHttpRequest();
	var patient_id = document.getElementById("patient_id").value;
	var url = 'ipbeddetails.do?method=ajaxEquipmentFinalizationCheck&visitId='+patient_id;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			if (reqObject.responseText != 'Finalized') {
				alert("Cannot finalize bed. Some equipments need to be finalized. ");
				return false;
			}
		}
	}
	return true;
}

function alertBedtype(id){
	if(document.getElementById(id).checked){
		if(!confirm("change in base bed type will effect the charges")){
			document.getElementById(id).checked = false;
			document.getElementById("change_basebed").value="false";
			return false;
		}
		document.getElementById(id).checked = true;
		document.getElementById("change_basebed").value="true";
		return true;
	}
}
function makeblank(timefield){
	timefield.value="";
}
function setMethod(method){
	document.getElementById("updatemethod").value = method;
	document.ipbedform.action = "ipbeddetails.do?method=update";
	document.ipbedform.submit();
}
function updateDuration(){
	var start_date = document.getElementById("action_date");
	var start_time = document.getElementById("action_time");

	var curr_date = document.getElementById("curr_date");
	var curr_time = document.getElementById("curr_time");

	var reg_date = document.getElementById("prev_date");
	var reg_time = document.getElementById("prev_time");

	if(!validateAdmissionDate(start_date,start_time)) return false;

	if(!validateFinalizeDate()) return false;

	return true;
}
function setTime(time){
	if(time.value.length == 2){
			time.value = time.value+":00";
		}
		if(time.value.length == 1){
			time.value = "0"+time.value+":00";
		}
}

function checkDiff(start_date,end_date,prevbedstatus){
	if(end_date != ''){
		if(start_date != end_date){
				if(!confirm("valid bed charge will be lost,want to continue?")){
					return false;
				}
			}
      }
      return true;
}

var retain_bed_name = '' ;
var retain_admit_id = 0 ;
var retain_bed_id = 0 ;

function checkRetainBed(canclebed,prevbedstatus){
	var row = getThisRow(canclebed);
	var id = getRowIndex(row);
	var retainsBeds  = document.getElementsByName("retainbed");
	var retainedBedNames = document.getElementsByName("retainbedname");
	var retainedAdmitId = document.getElementsByName("retainbedadmitid");
	var retainBedid = document.getElementsByName("retainbedid");
	var cancelled  = document.getElementsByName("cancelled");

	for ( var i = 0;i<retainsBeds.length ;i++ ) {
	 	if ( prevbedstatus == 'C' || prevbedstatus == 'A' ) {
			 if ( retainsBeds[i] != null ) {
				  if ( retainsBeds[i].checked && !isCancelled(retainedAdmitId[i].value) ) {
				  	if ( getElementByName(row,"cancelled").value == 'Y' ) {
					       retain_bed_name = retainedBedNames[i].value;
					       retain_admit_id = retainedAdmitId[i].value;
					       retain_bed_id = retainBedid[i].value;
					       setIndexedValue("cancelled", id, "Y");
						   if(!confirm("Retained bed "+retainedBedNames[i].value+" is selected as current bed,still want to continue?")){
								canclebed.checked = false;
								setIndexedValue("cancelled", id, "N");
								return false;
						  }
					  } else {
						   retain_bed_name = '';
					       retain_admit_id = 0;
					       retain_bed_id = 0;
					  }
				  }
			 }
		 }
	}
	return true;
}

function getretainbedname(retain_bedId,retainadmit_id)
{
  retain_bed_id= retain_bedId;
  retain_admit_id= retainadmit_id;
  return true;
}

function enable(dateObj,timeObj,checkbox,index){
	if(checkbox.checked){
		alert(dateObj[index]);
		dateObj.disabled = false;
		timeObj.disabled = false;
	}else{
		dateObj.disabled = true;
		timeObj.disabled = true;
	}
}
var wardAndBedDomDocObj = null;

function updateDutyDoctor(){
	document.ipbedform.updatemethod.value = "updateDutyDoctor";
	document.ipbedform.action = "ipbeddetails.do?method=update";
	document.ipbedform.submit();
}
function init(){
	initEditDialog();
}

function warnExeedingDaycare(){
	var fromDateTime = getDateTime(admitDate.split(" ")[0],admitDate.split(" ")[1]);
	var toDateTime = getDateTime(document.getElementById("action_date").value,
						document.getElementById("action_time").value);
	var noOfHrs = (toDateTime.getTime()-fromDateTime.getTime())/60/60/1000;
	if(noOfHrs > parseInt(daycarehrs) ){
		return confirm("Maximum Daycare hrs has been exceeded,\nBed hrly charge will be posted for the period of stay\nStill want to continue");
	}
	return true;

}

function validateAdmissionDate(date ,time ) {
	// check if adm date is > reg date
	var regDateTime = getDateTimeFromField(document.getElementById("reg_date"),
			document.getElementById("reg_time"));
	var admDateTime = getDateTimeFromField(document.ipbedform.action_date,
			document.ipbedform.action_time);

	if (admDateTime < regDateTime) {
		alert("Finalize date/time cannot be earlier than admission date/time (" + document.getElementById("reg_date").value + " " + document.getElementById("reg_time").value + ")");
		document.ipbedform.finaliseDate.focus();
		return false;
	}
	return true;
}

function getRowIndex(row) {
	return row.rowIndex - 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(ipbedform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(ipbedform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function setEdited( id ){
	setIndexedValue("edited" , id,  "Y" );
}

function setReleased(obj){
	var row = getThisRow(obj);
	var id = getRowIndex(row);
	setIndexedValue("released" ,id, obj.checked ? "Y" : "N" );
}

function onSave(){
	var valid = true;
	if(document.getElementById("action_finalze").checked)
		valid = finaliseBedDetails();
	else if(document.getElementById("action_update").checked)
		valid = updateDuration();
	
	/*if(document.getElementById("action_finalze").checked || document.getElementById("action_update").checked) {
		valid = valid && validateCreditLimitRule();
	}*/

	if(valid && validateDates()){
		document.ipbedform.action = "ipbeddetails.do?method=updateBedDetails&shift_admit_id="+retain_admit_id;
		document.ipbedform.submit();
	}
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function showEditDialog(obj){

	var row = getThisRow(obj);
	var id = getRowIndex(row);
	document.editForm.editRowId.value = id;
	var editForm = document.editForm;

	var bedStatus = getIndexedValue( "status", id );
	var bedState = getIndexedValue( "bed_state", id );
	var dutyDoctor = getIndexedValue( "duty_doctor_id", id );
	var isIcu = getIndexedValue( "is_icu", id );
	var chargedBedType = getIndexedValue( "charged_bed_type", id );
	var isBystander = getIndexedValue( "is_bystander", id );

	editForm.eDutyDoctor.value = dutyDoctor;
	editForm.eDutyDoctor.disabled = !(bedStatus == 'A' || bedStatus == 'C' || (bedStatus == 'R' && isBystander == 'false'));
	editForm.eReleaseBed.disabled = (bedStatus == 'R' && bedState == 'F') || !(bedStatus == 'R' && bedState == 'O');
	editForm.eEndDate.disabled = (bedState == 'F' || editForm.eReleaseBed.disabled);
	editForm.eEndTime.disabled = (bedState == 'F' || editForm.eReleaseBed.disabled);
	editForm.eRemarks.value = getIndexedValue( "remarks", id );
	editForm.eEndDate.value = bedStatus == 'R' ? document.getElementById("curr_date").value : getIndexedValue("endDate", id );
	editForm.eEndTime.value = bedStatus == 'R' ? document.getElementById("curr_time").value : getIndexedValue("endTime", id );

	var chargedBedTypes =  filterList(bedTypes, "is_icu", isIcu);
	loadSelectBox(editForm.eChargedBedType, chargedBedTypes, "bed_type_name", "bed_type_name", "-- Select --", "");
	editForm.eChargedBedType.value = chargedBedType;

	if(!canSetChargedBedType)
		editForm.eChargedBedType.disabled = true;
	else
		editForm.eChargedBedType.disabled = false;

	editDialog.cfg.setProperty("context", [row.cells[9], "tr", "br"], false);
	editDialog.show();
	return false;

}

function onEdit(){
	var index = document.editForm.editRowId.value;
	var row = getEditableRow(index);
	var editForm = document.editForm;

	if(validateEdit()){
		setEdited( index );
		var dutyDoctor = editForm.eDutyDoctor.value == "" ? '' :
						  editForm.eDutyDoctor.options[editForm.eDutyDoctor.selectedIndex].text;
		setNodeText(row.cells[3], dutyDoctor);
		setIndexedValue("duty_doctor_id", index, editForm.eDutyDoctor.value);

		setNodeText(row.cells[7], editForm.eRemarks.value);
		setIndexedValue("remarks", index, editForm.eRemarks.value);


		if(document.editForm.eReleaseBed.checked){
			setNodeText(row.cells[5], editForm.eEndDate.value +" "+editForm.eEndTime.value);
			setIndexedValue("end_date_dt", index, editForm.eEndDate.value+" "+editForm.eEndTime.value);
		}
		setIndexedValue("charged_bed_type", index, editForm.eChargedBedType.value);
		editDialog.hide();
	}


}

function validateEndDate(){
	var index = document.editForm.editRowId.value;

	var startDateTime = getDateTimeFromField(getIndexedFormElement(document.ipbedform,"startDate",index),
			getIndexedFormElement(document.ipbedform,"startTime",index));
	var ednDateTime = getDateTimeFromField(document.editForm.eEndDate,
			document.editForm.eEndTime);
	var regDateTime = getDateTimeFromField(document.ipbedform.reg_date,
			document.ipbedform.reg_time);

	if (ednDateTime < startDateTime) {
		alert("End date/time cannot be earlier than Start date/time");
		document.editForm.eEndDate.focus();
		return false;
	}else if( ednDateTime < regDateTime ){
		alert("End date/time cannot be earlier than Start date/time");
		document.editForm.eEndDate.focus();
		return false;
	}

	return doValidateDateField(document.editForm.eEndDate,validType);

	return true;
}

function validateEdit(){
	var index = document.editForm.editRowId.value;
	if(document.editForm.eReleaseBed.checked){
		setIndexedValue("released", index, "Y");
		if( document.editForm.eEndDate.value == '' || document.editForm.eEndTime.value == ''){
			alert("Please Enter End Date for released bed");
			document.editForm.eEndDate.focus();
			return false;
		}else{
			return validateEndDate();
		}

	}else{
		setIndexedValue("released", index, "N");
	}

	if(document.editForm.eChargedBedType.value == ''){
		showMessage("js.adt.beddetailsdate.chargedbedtype.notempty");
		return false;
	}
	return true
}

function closeDialog() {
	editDialog.cancel();
}

function getEditableRow(i) {
	i = parseInt(i);
	var table = document.getElementById("releasetable");
	return table.rows[i + 1];
}

function cancelBed(obj){

	var row = getThisRow(obj);
	var id = getRowIndex(row);
	var canceled = getIndexedValue( "cancelled", id ) == 'Y';

	if ( canceled ) {

		setIndexedValue("cancelled", id, "N" );
		checkRetainBed(obj,getIndexedValue("status", id ));
	} else {

		canceled = checkDiff(getIndexedValue("start_date_dt", id ),getIndexedValue("end_date_dt", id ),getIndexedValue("status", id ));

		if ( canceled ) {
			setIndexedValue("cancelled", id, !canceled ? "N" : "Y" );
			checkRetainBed(obj,getIndexedValue("status", id ));
		}
	}

	setCanceled(obj);

}

function setCanceled(obj){

	var row = getThisRow(obj);
	var id = getRowIndex(row);
	var trashImgs = null;
	var admitId = getIndexedValue("admit_id", id) ;
	var status = getElementByName(row,"status").value;
	var bystander = getElementByName( row,"is_bystander" ).value;

	if (typeof(8) != 'undefined')
		trashImgs = row.cells[8].getElementsByTagName("img");
	var cancelled = getIndexedValue("cancelled", id ) == 'Y';

	var flagTag = row.cells[0].getElementsByTagName("img");

	var flagSrc ;

	if(cancelled)
		flagSrc = cpath + '/images/red_flag.gif';
	else if ( status == 'P' )
		 flagSrc = cpath + '/images/grey_flag.gif';
    else if ( status == 'R' && bystander == 'false' )
    	 flagSrc = cpath + '/images/yellow_flag.gif';
    else if ( status == 'C' || status == 'A' )
    	 flagSrc = cpath + '/images/empty_flag.gif';
    else if ( status == 'X' )
    	 flagSrc = cpath + '/images/red_flag.gif';
    else
	     flagSrc = cpath + '/images/green_flag.gif';

	var trashSrc;
	if (!cancelled) {
		trashSrc = cpath + '/icons/delete.gif';		// grey trash icon
	} else {
		trashSrc = cpath + '/icons/undo_delete.gif';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;

	if (flagTag && flagTag[0])
		flagTag[0].src = flagSrc;
}

function validateDates(){
	var valid = true;

	valid = doValidateDateField(document.getElementById("action_date"),validType);
	valid = valid & validateTime(document.getElementById("action_time"));

	return valid;
}

function isCancelled(admitId){
	var admitIds = document.getElementsByName("admit_id");
	var cancelled = document.getElementsByName("cancelled");
	var cancled = false;
	for(var i = 0;i< admitIds.length;i++){
		if(admitIds[i].value == admitId && cancelled[i].value == 'Y'){
			cancled = true;
			break;
		}
	}

	return cancled;

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
			msg+=' '+ formatAmountPaise(visitPatientDuePaise);
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