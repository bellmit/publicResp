
function init(){
	fillTimeFormat();
	if (document.getElementById("tpa_id") != null && document.getElementById("tpa_id").type != "hidden")
		sortDropDown(document.getElementById("tpa_id"));
}

function clearFormIfInsurancePresent(isInsurancePresent) {
	if(isInsurancePresent == true){
		clearForm(document.addNewCase);
	}
}

// Add or Edit related functions
function getDetailsToAddCase(){
	document.forms[0].submit();
	return true;
}


function funValidateAndSubmit(){
	var form = document.addNewCase;

	if(document.addNewCase.whichScreen.value == 'AddNewCase'){
		var mr_no = document.addNewCase.mr_no.value;

		if(mr_no == ""){
			alert("Please enter MR NO");
			return false;
		}

		var visitCase = false;

		if(visitCase && (form.insurance_id.value != '' || document.getElementById('casePresent').value == 'Y') ){
			alert("Case with Visit or a Non-Tpa Bill exists, Case Creation is allowed only for MR NO");
			return false;
		} else if(visitCase && form.visit_id.value!='' && form.bill_nos.value == ''){
			alert("No Credit Bill Connected to TPA Exists, Case Creation is allowed only for MR NO");
			return false;
		}else if(visitCase && form.visit_id.value==''){
			if(document.getElementById('paramVisitId').value != '' && form.bill_nos.value == ''){
				alert("No Credit Bill Connected to TPA Exists, Case Creation is allowed only for MR NO");
			} else {
				alert("No Active Visit Without Case Found, Case Creation is allowed only for MR NO");
			}
			return false;
		}
	}

	var tpa_id = form.tpa_id.value;
	var status = form.status.value;
	var status_reason = form.status_reason.value;
	var finalized_date = form.finalizedDate.value;
	var finalized_time = form.finalizedTime.value;
	var totalApprovalAmtLblObj = document.getElementById("totalApprovalAmtLbl");
	var totalApprovalAmt = '';
	if (totalApprovalAmtLblObj != null) totalApprovalAmt = totalApprovalAmt.textContent;

	if(tpa_id == null || tpa_id == ""){
		form.tpa_id.focus();
		alert("Please select TPA to proceed");
		return false;
	}

	if( status =='F' && totalApprovalAmt == ''){
		alert("Approval amount is not given for some bill(s).\nPlease enter approval amount for bills");
		return false;
	}

	if(status == "F" && finalized_date==""){
		form.finalizedDate.focus();
		alert("Please Enter Finalized Date");
		return false;
	}

	if(status == "F" && (finalized_time=="" || finalized_time=="HH:MM")){
		form.finalizedTime.focus();
		alert("Please Enter Finalized Time");
		return false;
	}else{
		if(!validateTimeIns(form.finalizedTime))
			return false;
	}
	if(status == 'C' && isBillOpen == 'Y') {
		alert("One or more bill(s) is open...\nCase cannot be closed.");
		setSelectedIndex(form.status,form.caseStatus.value);
		return false;
	}
	if((status == "C" || status == "D") && status_reason== ""){
		form.status_reason.focus();
		alert("Please Enter Status Reason");
		return false;
	}

	if(form.finalizedDate.value!=null && form.finalizedDate.value != "")
		form.finalized_date.value = form.finalizedDate.value + " " + ((form.finalizedTime.value =='' || form.finalizedTime.value=='HH:MM')?"00:00":form.finalizedTime.value);

	form.action = "AddOrEditCase.do?_method=addOrEdit";
	form.submit();
	return true;
}

function funNullifyValues(formName){
	var form ;
	if(formName == 'addNewCase')
		 form = document.addNewCase;
	else
		 form = document.forms[0];

	if(form.mr_no !=null)
		form.mr_no.value="";
	if(form.insurance_id !=null)
		form.insurance_id.value="";
	if(form.tpa_id !=null)
		form.tpa_id.value="";
}
function funAddOrEditCancel(){
	var form = document.addNewCase;
	funNullifyValues('addNewCase');
	form.action = "InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true&visit_type=";
 	form.submit();

}
function funClose(){
	funNullifyValues('Others');
	document.forms[0].action = "InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true";
 	document.forms[0].submit();
}
function checkForCreditBill(anchor,billNo,insurance_id){
	var href1 = anchor.getAttribute("href");
	if(insurance_id !=""){
		anchor.setAttribute("href", "#");
		//alert("Case already exists for this visit");
		enableToolbarItem('Visit', false);
		return false;
	}
	if(billNo == "" || billNo== null){
		anchor.setAttribute("href", "#");
		//alert("No Credit Bill Available for this patient");
		enableToolbarItem('Visit', false);
		return false;
	}else{
		enableToolbarItem('Visit', true);
		anchor.setAttribute("href", href1);
	}
}

function funCheckForCaseConnected(anchor,billNo,ClaimTemplateId,defaultClaimTemplate){
	var href1 = anchor.getAttribute("href");
	if(billNo == "" || billNo== null) {
		anchor.setAttribute("href", "");
		alert("Please connect case to latest insurance visit to View/Edit Claim");
		return false;
	}else if(ClaimTemplateId == '0' && defaultClaimTemplate=="N") {
		alert("Please Create Claim Template and connect it to TPA to Proceed");
		return false;
	}else {
		anchor.setAttribute("href", href1);
	}
}

function funCheckForPreauthForm(anchor,sysPdfForm){
	var href1 = anchor.getAttribute("href");
	if(sysPdfForm == "" || sysPdfForm == null || sysPdfForm == '0'){
		alert("Prior Auth Form not available,Please select a Prior Auth form in Tpa Master");
		return false;
	}
	anchor.setAttribute("href", href1);
}
function validateTimeIns(timeField) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '' || strTime == 'HH:MM') {
		return true;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			alert("Incorrect time format : hour is not a number");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			alert("Incorrect time format : minute is not a number");
			timeField.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			alert("Incorrect hour : please enter 0-23 for hour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			alert("Incorrect minutes : please enter 0-59 for minutes");
			timeField.focus();
			return false;
		}
	} else {
		alert("Incorrect time format : please enter HH:MM");
		timeField.focus();
		return false;
	}
	return true;
}

function makeblank(timefield){
	if(timefield.value == 'HH:MM') timefield.value="";
}

function setTime(time){
	if(time.value == '') time.value = 'HH:MM';
}

function fillTimeFormat(){
	setTime(document.getElementById("finalizedTime"));

}
//Upload Screen Related Functions
function funValidateAndUpload(){
	var doc_name = document.forms[0].document_name.value;

	if(doc_name == ""){
		alert("Please Enter Doc Name");
		document.forms[0].document_name.focus();
		return false;
	}
	var doc_content = document.getElementById("doc_content").value;
	if(doc_content == ""){
		alert("Please upload file(s) to save");
		document.forms[0].doc_content.focus();
		return false;
	}
	document.forms[0].action = "UploadReceivedDocs.do?_method=upload";
	document.forms[0].save.disabled = true;
 	document.forms[0].submit();
}

function funCancelUpload(){

}
//Send To TPA Screen
function selectAll() {
			var sendCheckBoxs = document.getElementsByName("sendToTpa");
			for (var i=0; i<sendCheckBoxs.length; i++) {
				if (!sendCheckBoxs[i].checked && !sendCheckBoxs[i].disabled) {
					sendCheckBoxs[i].checked = true;
				}
			}
		}

		function unSelectAll() {
			var sendCheckBoxs = document.getElementsByName("sendToTpa");
			for (var i=0; i<sendCheckBoxs.length; i++) {
				if (sendCheckBoxs[i].checked) {
					sendCheckBoxs[i].checked = false;
				}
			}
		}
function checkDocuments() {
	var to = document.SendToTpaForm.email_to.value;
	if(to == ""){
		alert("Please Enter TO Address");
		return false;
	}
 	document.SendToTpaForm.submit();
}
//History

function fundeleteRecdDocs(){
	var deleteCheckBoxs = document.getElementsByName("deleteRecdDoc");
	var deleteStaus =false;

	for (var i=0; i<deleteCheckBoxs.length; i++) {
		if (deleteCheckBoxs[i].checked) {
			deleteStaus = true;
		}
	}

	if(!deleteStaus){
		alert("Please Select Documents to delete");
		return false;
	}else{
		document.history.action = "InsuranceHistory.do?_method=deleteRecdDoc";
	 	document.history.submit();
	 	return true;
	 }
}

//Insurnace Documents

function deleteSelected(e) {
	var deleteEl = document.getElementsByName("deleteDocument");
	for (var i=0; i< deleteEl.length; i++) {
		if (deleteEl[i].checked) {
			document.forms[0].action="InsuranceGenericDocuments.do?_method=deleteDocuments";
			document.forms[0].submit();
			return true;
		}
	}
	alert("Select at least one image for delete");
	YAHOO.util.Event.stopEvent(e);
	return false;
}


