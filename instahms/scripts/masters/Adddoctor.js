
function setPaymentEligible(){
	if(document.forms[0].doctor_type.value=="CONSULTANT"){
		document.forms[0].payment_eligible.options.selectedIndex=1;
	}else{
		document.forms[0].payment_eligible.options.selectedIndex=0;
	}
}

function autoCompleteOrgs(){

	YAHOO.example.orgNamesArray = [];
	YAHOO.example.orgNamesArray.length =orgNames.length;

	for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
				YAHOO.example.orgNamesArray[i] = item["ORG_NAME"];
	}

	YAHOO.example.ACJSArray = new function(){
				  // Instantiate first JS Array DataSource
				  datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.orgNamesArray);
				  // Instantiate first AutoComplete
				  var autoComp = new YAHOO.widget.AutoComplete('orgName','orgContainer', datasource);
				  autoComp.prehighlightClassName = "yui-ac-prehighlight";
				  autoComp.typeAhead = true;
				  // Enable a drop-shadow under the container element
				  autoComp.useShadow = true;
				  // Disable the browser's built-in autocomplete caching mechanism
				  autoComp.allowBrowserAutocomplete = false;
				  // Require user to type at least 0 characters before triggering a query
				  autoComp.minQueryLength = 0;
				  //commas and/or spaces may delimited queries
				  //autoComp.delimChar = [];
				  // Display up to 20 results in the container
				  autoComp.maxResultsDisplayed = 20;
				  // Do not automatically highlight the first result item in the container
				  autoComp.autoHighlight = false;
				  // disable force selection,user can type his/her own complaint(which is not there in master)
				  autoComp.forceSelection = true;
				  autoComp.textboxFocusEvent.subscribe(function(){
					   var sInputValue = YAHOO.util.Dom.get('orgName').value;
					   if(sInputValue.length === 0) {
						         var oSelf = this;
						         setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					  }

			     });

			     //autoComp.dataReturnEvent.subscribe(populateOrgId);
			}

}

function autoCompleteDoctors() {
	var datasource = new YAHOO.widget.DS_JSArray(doctorNames);
	var autoComp = new YAHOO.widget.AutoComplete('doctor_name','doctorContainer', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 1;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = false;
	autoComp.forceSelection = false;
	autoComp.queryMatchContains = true;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;
}

function populateOrgId(){
	//alert(document.DocForm.orgName.value);
	 for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
		if(trim(item["ORG_NAME"])==trim(document.DocForm.orgName.value)){
			document.DocForm.orgId.value = item["ORG_ID"];
		}
	  }
}

/**
 * Enabling/Disabling available_for_online_consults and overbook_limit fields
 * on the basis of scheduleable_by value
 */
function changeOverbookValue() {
	if (document.forms[0].scheduleable_by.value != 'N') {
		document.forms[0].overbook_limit.disabled = false;
		document.forms[0].available_for_online_consults.disabled = false;
	} else {
		document.forms[0].overbook_limit.disabled = true;
		document.forms[0].overbook_limit.value = 0;
		document.forms[0].available_for_online_consults.disabled = true;
	}
}

function populateDoctorId(){
	document.searchform.doctorId.value = "";

	if(trim(document.searchform.doctor_name.value) != ""){
	for(var i=0;i<doctorNames.length;i++){
			var item = doctorNames[i];
			alert(item);
			alert("document.searchform.doctor_name.value======"+document.searchform.doctor_name.value);
		if (trim(item["DOCTOR_NAME"]) == trim(document.searchform.doctor_name.value)){
			document.searchform.doctorId.value = item["DOCTOR_ID"];
		}
	}

	}
}

function doSearch(){
    var form = document.DocForm;

	var orgName = form.orgName.value;
	var chargeType = form.chargeType.options[form.chargeType.selectedIndex].value;
		populateDoctorId();
		form.orgName.value = form.orgId.options[form.orgId.selectedIndex].value;
		//alert(form.orgId.value);


	var deptLength = form.dept_id.length;
	var checked = false;
	for(var i=0;i<deptLength;i++){
		if(form.dept_id.options[i].selected){
			checked = true;
			break;
		}
	}

	if(!checked){
		alert('department name is required');
		form.dept_id.focus();
		return false;
	}

}

function ValidateGropUpdate(){
	if(document.listform.allPageOperations == null){
		alert("No Doctors available to update charges");
		return false;
	}

	var checked = false;
	var anyOperations = false;
		var allOperations = getRadioSelection(document.updateform.allDoctors);
	if (allOperations == 'yes') {
		anyOperations = true;
	} else {
		var div = document.getElementById("doctorListInnerHtml");
		while (div.hasChildNodes()) {
			div.removeChild(div.firstChild);
		}

		var length = listform.selectDoctor.length;
		if (length == undefined) {
			if (listform.selectDoctor.checked ) {
				anyOperations = true;
				div.appendChild(makeHidden("selectDoctor", "", listform.selectDoctor.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectDoctor[i].checked){
					anyOperations = true;
					div.appendChild(makeHidden("selectDoctor", "", listform.selectDoctor[i].value));
				}
			}
		}

	}

	if(!anyOperations){
		alert('at least one doctor has to checked for updation');
		return;
	}

	var anyBedTypes = false;
	if (updateform.allBedTypes.checked) {
		anyBedTypes = true;
	} else {
		var bedTypeLength = updateform.selectBedType.length;

		for (var i=0; i<bedTypeLength ; i++) {
			if(updateform.selectBedType.options[i].selected){
				anyBedTypes = true;
				break;
			}
		}
	}

	if (!anyBedTypes) {
		alert('Select at least one Bed Type for updation');
		return ;
	}

	if (!updateOption()) {
		alert("Select any update option");
		updateform.updateTable[0].focus();
		return ;
	}

	if (updateform.amount.value=="") {
		alert("Value required for Amount");
		updateform.amount.focus();
		return ;
	}

	if(updateform.amtType.value == '%') {
		if(getAmount(updateform.amount.value) > 100){
			alert("Discount percent cannot be more than 100");
			updateform.amount.focus();
			return false;
		}
	}

	var deptLength = document.searchform.dept_id.length;
	var div = document.getElementById("doctorListInnerHtml");

	for(var i=0;i<deptLength;i++){
		if(document.searchform.dept_id.options[i].selected){
			div.appendChild(makeHidden("dept_id", "", document.searchform.dept_id[i].value));
		}
	}

	document.updateform.org_id.value = document.searchform.org_id.value;
	document.updateform.charge_type.value = document.searchform._charge_type.value;
	document.updateform.submit();
}

function updateOption() {
	for (var i=0; i<updateform.updateTable.length ; i++) {
		if(updateform.updateTable[i].checked){
			return true;
		}
	}
	return false;
}

function submitPayments(){
	 var form = document.DocForm;
	 var paymentType = form.paymentType;

	var checked = false;
	 for(var i=0;i<paymentType.length;i++){
		if(paymentType[i].checked){
			checked = true;
			break;
		}
	 }

	 if(!checked){
	 	alert("payment type is required");
	 	return false
	 }

	 if(document.getElementById('paymentTypePercentage').checked==true){
		if(document.forms[0].docPayForOP.value>100){
			alert("OP charge of Doctor payment should be less than 100");
			document.forms[0].docPayForOP.focus();
			return false;
		}
		if(document.forms[0].docPayForIP.value>100){
			alert("IP charge of Doctor payment should be less than 100");
			document.forms[0].docPayForIP.focus();
			return false;
		}
		if(document.forms[0].docPayForOperation.value>100){
			alert("Operation charge of Doctor payment should be less than 100");
			document.forms[0].docPayForOperation.focus();
			return false;
		}
	 }
	 document.forms[0].submit();

}

function changeRate(){
document.forms[0].submit();
}

function selectAllBedTypes(){
	var selected = document.updateform.allBedTypes.checked;
	var bedTypesLen = document.updateform.selectBedType.length;

	for (i=bedTypesLen-1;i>=0;i--) {
		document.updateform.selectBedType[i].selected = selected;
	}
}


function selectbedtypes(){

	document.updateform.selectBedType.checked = true;
}

function doExport() {
	document.exportform.org_id.value = document.searchform.org_id.value;
	return true;
}

function doUpload(formType) {

   if(formType == "uploadChargesform"){
	var form = document.uploadChargesform;
	if (form.xlsFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	}
	form.org_id.value = document.searchform.org_id.value;
   }else{
   var form = document.uploaddoctorform;
   	if (form.xlsDoctorFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	  }
   }
	form.submit();
}

function onChangeAllDoctors() {
	var val = getRadioSelection(document.updateform.allDoctors);
	// if allOperations = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPageOperations.disabled = disabled;
	listform.allPageOperations.checked = false;

	if(listform.selectDoctor !=null){
		var length = listform.selectDoctor.length;

		if (length == undefined) {
			listform.selectDoctor.disabled = disabled;
			listform.selectDoctor.checked  = false;
		} else {
			for (var i=0;i<length;i++) {
				listform.selectDoctor[i].disabled = disabled;
				listform.selectDoctor[i].checked = false;
			}
		}
	}
}
function selectAllPageDoctors() {
	var checked = document.listform.allPageOperations.checked;
	var length = document.listform.selectDoctor.length;

	if (length == undefined) {
		document.listform.selectDoctor.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectDoctor[i].checked = checked;
		}
	}
}

var toolbar = {
			Edit: {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '/master/DoctorMasterCharges.do?_method=getDoctorDetailsScreen',
				onclick: null,
				description: "View and/or Edit the contents of this Doctor Details"
			},
			View: {
				title: "Edit Charges",
				imageSrc: "icons/Edit.png",
				href: '/master/DoctorMasterCharges.do?_method=getDoctorChargesScreen',
				onclick: null,
				description: "View and/or Edit the contents of this Doctor Charge"
			},
};
function validations(isStrictValidateDoctorMobile) {
	var salutation = document.getElementById("doc_salutation_id").options.selectedIndex;
	if (salutation == '0') {
		alert("Salutation is Required");
		salutation.focus();
		return false;
	}
	
	var firstName = document.getElementById("doc_first_name").value;
	if (!firstName) {
		alert("First Name is Required");
		firstName.focus();
		return false;
	}
	var lastName = document.getElementById("doc_last_name").value;
	if (!lastName) {
		alert("Last Name is Required");
		lastName.focus();
		return false;
	}
	if (firstName.length > 50) {
		alert("First Name is too long");
		firstName.focus();
		return false;
	}
	var middleName = document.getElementById("doc_middle_name").value;
	if (middleName.length > 100) {
		alert("Middle Name is too long");
		middleName.focus();
		return false;
	}
	if (lastName.length > 50) {
		alert("Last Name is too long");
		lastName.focus();
		return false;
	}
	var clinic_phone = document.forms[0].clinic_phone.value;
	if (clinic_phone != '' && !validatePhoneNo(clinic_phone)) {
		document.forms[0].clinic_phone.focus();
		return false;
	}
	var doctor_mobile = document.forms[0].doctor_mobile.value;
	if (doctor_mobile != '' && !validatePhoneNo(doctor_mobile)) {
		document.forms[0].doctor_mobile.focus(); 
		return false;
	}
	if(isStrictValidateDoctorMobile == true && doctor_mobile != ''){
		if($("#doctor_mobile_valid").val() != 'Y'){
			$("#doctor_mobile_national").focus();
			return false;
		}
		
	}
	var res_phone = document.forms[0].res_phone.value;
	if (res_phone != '' && !validatePhoneNo(res_phone)) {
		document.forms[0].res_phone.focus();
		return false;
	}

	var overbook = document.forms[0].overbook_limit.value;
	
	if(overbook.length > 10){
		alert("Please enter only 10 digits number for overbook Limit");
		document.forms[0].overbook_limit.focus();
		return false;
	}
	
	var registrationNo = document.getElementById("registration_no").value;
	if (registrationNo) {
		ajaxDocDetails = checkDoctorExistsAjax();
		doctorName = ajaxDocDetails.doctor_name;
		if (doctorName) {
			res = confirm("Doctor having Registartion Number "+registrationNo+" already exists with doctor name : "+doctorName);
			if (!res) {
				registrationNo.focus();
				return false;
			}
		}
	}

	var available_for_online_consults = document.getElementById("available_for_online_consults").value;
    if(available_for_online_consults === 'N'){
		var onlineExistsAjax = checkAppointmentExists();
		var onlineExists = onlineExistsAjax.result;
		if (!onlineExists || onlineExists === "true") {
			res = alert("Please ensure no online appointment scheduled or Availability / Override is defined before setting \"Schedulable for Online Consults\" to \"No\" for this doctor.");
			return false;
		}
	}
	document.forms[0].submit();
	return true;
}

function checkDoctorExistsAjax() {
	var doctor_id = document.getElementById("doctor_id").value;
	var registrationNo = document.getElementById("registration_no").value;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath
			+ "/master/DoctorMasterCharges.do?_method=getDoctorExistingDetailsAjax&registration_no="+registrationNo+"&doctor_id="+doctor_id;
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function checkAppointmentExists() {
	var doctor_id = document.getElementById("doctor_id").value;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/master/DoctorMasterCharges.do?_method=getDoctorExistingDetails&doctor_id="+doctor_id;
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
	return null;
}
function init() {
	toolbar.ConsultFavourites = {
				title : "Consultation Favourites",
				imageSrc: "icons/Edit.png",
				href: '/master/ConsultationFavourites.do?_method=list',
				onclick: null,
				description : 'Add/Edit/View Consultation Favourites',
				show: op_prescribe
	};
	toolbar.DiagnosisCodeFavourites = {
	            title : "Diagnosis Code Favourites",
	            imageSrc : "icons/Edit.png",
	            href : 'master/DiagnosisCodeFavourites.do?_method=list',
	            onclick : null,
	            description : 'ADD/Edit/View Diagnosis Code Favourites',
	            show : op_prescribe
   };
   toolbar.DoctorCenterApplicability = {
				title: "Center Applicability",
				imageSrc : "icons/Edit.png",
				href: '/master/DoctorCenterApplicability.do?_method=getScreen',
				onclick: null,
				description : 'Center Applicability of this Doctor',
				show : doctorCenterapplicable
	};
	createToolbar(toolbar);
	showFilterActive(document.searchform);
}

function ChanellingValues(obj) {
	document.getElementById('chanelling').value = obj.checked ? 'Y' : 'N';
}

function setDoctorName() {
	var salutation = document.getElementById("doc_salutation_id");
	var salutationText = salutation.options[salutation.selectedIndex].text;
	var firstName = document.getElementById("doc_first_name").value;
	var middleName = document.getElementById("doc_middle_name").value;
	var lastName = document.getElementById("doc_last_name").value;
	var strings = [salutationText, firstName, middleName, lastName];
	if (!middleName) {
		strings = [salutationText, firstName, lastName];
	}
	document.getElementById("doctor_name").value = strings.join(" ");
}

function getSortedSalutationByLengthAjax() {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/master/DoctorMaster.do?_method=getSortedSalutationByLengthAjax";
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function splitDocName() {
	var firstName = document.getElementById("doc_first_name").value;
	var doctorName = document.getElementById("doctor_name").value;
	var salutationT;
	if (!firstName && doctorName) {
		doctorName = doctorName.trim();
		var salutationOptions = getSortedSalutationByLengthAjax().salutations;
		
		for (var i = 0; i < salutationOptions.length; i++) {
			var saluText = salutationOptions[i].salutation;
			if (saluText && saluText !== '') {
				var regExp = new RegExp('^' + saluText, 'gi');
				if (regExp.test(doctorName)) {
					document.getElementById("doc_salutation_id").value = salutationOptions[i].salutation_id;
					salutationT = salutationOptions[i].salutation_id;
					var docName = doctorName.substring(salutationOptions[i].salutation.length).trim().split(/\s+/);
					var midIndex = 1;
					var reg = new RegExp('^\\.');
					if (docName[0].match(reg) && docName[0].length > 1) {
						document.getElementById("doc_first_name").value = docName[0].substring(1);
					} else if (docName[0] === '.') {
						document.getElementById("doc_first_name").value = docName[1];
						midIndex = 2;
					} else {
						document.getElementById("doc_first_name").value = docName[0];
					}
					var docNameLength = docName.length;
					if (docNameLength > 1) {
						document.getElementById("doc_middle_name").value = docName.slice(midIndex, docNameLength - 1).join(" ");
						document.getElementById("doc_last_name").value = docName[docNameLength - 1];
					}
					break;
				}
			}
		}
		if (!salutationT) {
			doctorName = doctorName.trim().split(/\s+/);
			document.getElementById("doc_first_name").value = doctorName[0];
			document.getElementById("doc_middle_name").value = doctorName.slice(1, doctorName.length - 1).join(" ");
			if (doctorName.length > 1) {
				document.getElementById("doc_last_name").value = doctorName[doctorName.length - 1];
			}
		}
	}
}
