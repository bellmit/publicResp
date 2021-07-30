/*****************
 * functions for dynamic event handling to make screen changes on the fly
 ****************/

/*
 * Switch the UI to an edit operation
 */

$(function() {
		$('input[name="ssoOnlyUser"]').click(function(ev){
			if (this.checked) {
				$('input[name="password"]').attr("disabled","disabled").addClass("disabled");
				$('input[name="confirmPassword"]').attr("disabled","disabled").addClass("disabled");
				document.userForm.forcePasswordChange.checked=false;
				$('input[name="forcePasswordChange"]').attr("disabled","disabled").addClass("disabled");
			} else {
				$('input[name="password"]').removeAttr("disabled").removeClass("disabled");
				$('input[name="confirmPassword"]').removeAttr("disabled").removeClass("disabled");
				$('input[name="forcePasswordChange"]').removeAttr("disabled").removeClass("disabled");
			}
		});
});
function setForEdit(){
	resetFields();
	document.userForm.selUserName.disabled = false;
	document.userForm.name.disabled = true;
}

/*
 * Switch to a create operation: opposit of above
 */
function setForCreate(){
	resetFields();
	document.userForm.selUserName.disabled = true;
	document.userForm.name.disabled = false;
}

/*
 * reset all fields to initial values
 */
function resetFields(){
	document.userForm.name.value = "";
	document.userForm.forcePasswordChange.checked=true;
	document.userForm.status.selectedIndex = 0;
	document.userForm.sharedLogin.selectedIndex = 1;
	document.userForm.password.value = "";
	document.userForm.password.readOnly = false;
	document.userForm.confirmPassword.value = "";
	document.userForm.confirmPassword.readOnly = false;
	document.userForm.roleId.selectedIndex = 0;
	document.userForm.roleRemarks.value = "";
	document.userForm.remarks.value = "";
	//document.userForm.counterId.selectedIndex = 0;
	document.userForm.specialization.selectedIndex = 0;
	document.userForm.labDepartment.selectedIndex = 0;
	document.userForm.pharmacycounterId.selectedIndex = 0;
	document.userForm.pharmacyStoreId.selectedIndex = 0;
	document.getElementById("disMsg").style.display = "none"
	document.userForm.fullname.value="";
	document.userForm.schedulerDepartment.selectedIndex = 0;
	document.userForm.schedulerDefaultDoctor.value = '';
	document.userForm.poApprovalLimit.value = '';
	document.userForm.writeOffLimit.value = '';
	document.userForm.emailId.value = "";
	document.userForm.mobileNo.value = "";
	setSelectedIndex(document.userForm.userCenter, "0");
}

/*
 * updateUserDetails: when a new user is selected from the dropdown, the details
 * are displayed for editing purpose.
 */
function updateUserDetails() {
	var form = document.userForm;
	var userName = form.selUserName.value;

	setSelectedIndex(document.userForm.userCenter, "0");
	document.userForm.sharedLogin.selectedIndex = 1;
	if ( (userName != null) && (userName!="") ) {
	    form.name.value=userName;
	    form.name.disabled=true;
	    form.op.value="edit";
		var userData = userDetailsMap[userName];
		setSelectedIndex(form.status, userData.status);
		setSelectedIndex(form.sharedLogin, userData.sharedLogin);
		form.ssoOnlyUser.checked = userData.ssoOnlyUser;
		form.forcePasswordChange.checked = userData.forcePasswordChange;
		form.password.value = userData.password;
		form.confirmPassword.value = userData.password;
		form.password.disabled = userData.ssoOnlyUser;
		form.confirmPassword.disabled = userData.ssoOnlyUser;
		setSelectedIndex(form.roleId, userData.roleId);
		form.roleRemarks.value = userData.roleRemarks;
		form.remarks.value = userData.remarks;
		setSelectedIndex(form.specialization, userData.specialization);
		setSelectedIndex(form.labDepartment, userData.labDepartment);
		form.fullname.value = userData.fullname
		setSelectedIndex(form.doctorId, userData.doctorId);
		setSelectedIndex(form.schedulerDepartment, userData.schedulerDepartment);
		setSelectedIndex(form.bedViewDefaultWard, userData.bedViewDefaultWard);

		setSelectedIndex(form.userCenter, userData.userCenter);
		setSelectedIndex(form.reportCenter, userData.reportCenter);
		filterMultiStores(userData.userCenter);
		filterMultiTheatres(userData.userCenter);
		setMultipleSelectedIndexs(form.multiTheatreId,userTheatreList);
		filterBillingAuthorization(userData.userCenter); //getting the billing authorizer dropdown on onload
		loadSelectBox( document.userForm.sampleCollectionCenter,filterList(sampleCollectionCentersJSON, "center_id", userData.userCenter),
					'collection_center','collection_center_id','-- Select --',null );
		setSelectedIndex(form.sampleCollectionCenter, userData.sampleCollectionCenter);
		filterCounters(userData.pharmacycounterId, userData.counterId);
		setMultipleSelectedIndexs(form.multiStoreId,userData.multiStoreId);
		filterDefaultPharmStore();
		filterDefaultTheatre();
		setSelectedIndex(form.pharmacyStoreId, userData.pharmacyStoreId);
		setSelectedIndex(form.defaultTheatresId,defaultTheatreId);
		//setSelectedIndex(form.counterId, userData.counterId);
		setSelectedIndex(form.pharmacycounterId, userData.pharmacycounterId);

		form.schedulerDefaultDoctor.value = userData.schedulerDefaultDoctor;
		setSelectedIndex(form.discAuthorizer, userData.discAuthorizer); //set the selected index of billing authorizer
		form.loginControlsApplicable.value = userData.loginControlsApplicable;
		initDoctorDept(userData.schedulerDepartment);
		document.getElementById('mod_user').innerHTML = userData.modUser;
		document.getElementById('mod_date').innerHTML = userData.modDate;
		document.userForm.poApprovalLimit.value = userData.poApprovalLimit;
		document.userForm.writeOffLimit.value = userData.writeOffLimit;
		document.userForm.permissibleDiscountCap.value = userData.permissibleDiscountCap;
		document.userForm.firstName.value = userData.firstName;
		document.userForm.middleName.value = userData.middleName;
		document.userForm.lastName.value = userData.lastName;
		setSelectedIndex(form.gender, userData.gender);
		document.userForm.emailId.value = userData.emailId;
		document.userForm.mobileNo.value = userData.mobileNo;
		document.userForm.employeeId.value = userData.employeeId;
		document.userForm.profession.value = userData.profession;
		document.userForm.employeeCategory.value = userData.employeeCategory;
		document.userForm.employeeMajor.value = userData.employeeMajor;
		document.userForm.allow_sig_usage_by_others.value = userData.allow_sig_usage_by_others;
		document.userForm.allowBillFinalization.value = userData.allowBillFinalization;

		if ( form.userCenter.value == 0 && max_centers_inc_default > 1) {
			document.getElementById("sampleCollectionTR").style.display = "none";
		 }

	} else {
		filterMultiStores(0);
		filterMultiTheatres(0);
		filterCounters(0);
		filterDefaultPharmStore();
		filterBillingAuthorization(0)
		if(max_centers_inc_default > 1)
			document.getElementById("sampleCollectionTR").style.display = "none";
	}


}

function filterMultiStores(centerId) {
	if (centerId == 0) {
		loadSelectBox(document.userForm.multiStoreId, storesJSON, "DEPT_NAME", "DEPT_ID", null, null);
	} else {
		var stores = filterList(storesJSON, "CENTER_ID", centerId);
		loadSelectBox(document.userForm.multiStoreId, stores, "DEPT_NAME", "DEPT_ID", null, null);
	}
}

function filterMultiTheatres(centerId){
	if (centerId == 0) {
		loadSelectBox(document.userForm.multiTheatreId, theatresJSON, "THEATRE_NAME", "THEATRE_ID", null, null);
	} else {
		var theatres = filterList(theatresJSON, "CENTER_ID", centerId);
		loadSelectBox(document.userForm.multiTheatreId, theatres, "THEATRE_NAME", "THEATRE_ID", null, null);
	}
	setMultipleSelectedIndexs(document.userForm.multiTheatreId,userTheatreList);
}

function filterDefaultPharmStore() {
	var multiStore = document.userForm.multiStoreId;
	var dPharmacyStore = document.userForm.pharmacyStoreId;
	var j=1;
	dPharmacyStore.length = j;
	dPharmacyStore.options[0].value = "";
	dPharmacyStore.options[0].text = '-- Select --';
	for (var i=0; i<multiStore.options.length; i++) {
		if (multiStore.options[i].selected) {
			dPharmacyStore.length = ++j;
			dPharmacyStore.options[j-1].value = multiStore.options[i].value;
			dPharmacyStore.options[j-1].text = multiStore.options[i].text;
		}
	}
}

function filterDefaultTheatre() {
	var theatre = document.userForm.multiTheatreId;
	var dtheatreid = document.userForm.defaultTheatresId;
	var j=1;
	dtheatreid.length = j;
	dtheatreid.options[0].value = "";
	dtheatreid.options[0].text = '-- Select --';
	for (var i=0; i<theatre.options.length; i++) {
		if (theatre.options[i].selected) {
			dtheatreid.length = ++j;
			dtheatreid.options[j-1].value = theatre.options[i].value;
			dtheatreid.options[j-1].text = theatre.options[i].text;
		}
	}
}

function filterCounters(onload_ph_counter_id, onload_bill_counter_id) {
	var centerId = document.userForm.userCenter.value;
	if (centerId == 0) {
		loadSelectBox(document.userForm.pharmacycounterId, pharmacy_counters, "counter_no", "counter_id", "-- Select --", "");
		//loadSelectBox(document.userForm.counterId, billing_counters, "counter_no", "counter_id", "-- Select --", "");
	} else {
		//var billingCounters = filterList(billing_counters, "center_id", centerId);
		var pharmacyCounters = filterList(pharmacy_counters, "center_id", centerId);
		loadSelectBox(document.userForm.pharmacycounterId, pharmacyCounters, "counter_no", "counter_id", "-- Select --", "");
		//loadSelectBox(document.userForm.counterId, billingCounters, "counter_no", "counter_id", "-- Select --", "");
	}
}

function filterBillingAuthorization(centerId){
	if (centerId == 0) {
		loadSelectBox(document.userForm.discAuthorizer, billingAuthorizerJSON, "DISC_AUTH_NAME", "DISC_AUTH_ID", "-- Select --", "");
	} else {
		var billingAuth = filterListWithCommaValues(billingAuthorizerJSON, "CENTER_ID", centerId);
		loadSelectBox(document.userForm.discAuthorizer, billingAuth, "DISC_AUTH_NAME", "DISC_AUTH_ID", "-- Select --", "");
	}
}

function filterListWithCommaValues(list, varName, varValue) {
	if (list == null) return null;
	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		varNameArr = list[i][varName].split(",");
		for(var j=0; j<varNameArr.length; j++) {
			if ( (varNameArr[j] == varValue || varNameArr[j] == 0) && varNameArr[j] != '' ) {
				filteredList.push(list[i]);
				break;
			}
		}
	}
	return filteredList;
}

/*
 * updateRoleRemarks: when a different role is chosen, that role's remarks
 * are displayed as info about the role.
 */
function updateRoleRemarks() {
	var form = document.userForm;
	var roleId = form.roleId.value;

	if ( (roleId != null) && (roleId != "") ) {
		var remarks = roleDetailsMap[roleId];
		form.roleRemarks.value = remarks;
	}

}
/*
 * Populate user details using AJAX call to get the user's details.
 */
function getUserDetails() {
	var userName	=	document.userForm.selUserName.value;

	if (userName!="") {
		var ajaxReqObject = newXMLHttpRequest();
		var url="../../pages/usermanager/UserAction.do?method=getUserDetails&userName="+userName;
		getResponseHandlerText(ajaxReqObject, handleAjaxResponse, url);
	}
}

function handleAjaxResponse(responseText) {
	/*
	 * We are expecting a JSON response like this:
	 * { name: "user name", roleId: 3, ...}
	 * Update the corresponding form element based on the values received
	 */

	eval("var userData = " + responseText);
	var form = document.userForm;

	setSelectedIndex(form.status, userData.status);
	setSelectedIndex(form.sharedLogin, userData.sharedLogin);
	form.password.value = userData.password;
	form.confirmPassword.value = userData.password;
	setSelectedIndex(form.roleId, userData.roleId);
	form.roleRemarks.value = userData.roleRemarks;
	form.remarks.value = userData.remarks;
	//setSelectedIndex(form.counterId, userData.counterId);
	setSelectedIndex(form.specialization, userData.specialization);
	setSelectedIndex(form.labDepartment, userData.labDepartment);
	setMultipleSelectedIndexs(form.multiStoreId,userData.multiStoreId);
	form.fullname.value = userData.fullname;
}

var MANDATORY_MALAFFI_FIELDS = [
	'firstName',
	'middleName',
	'lastName',
	'gender',
	'emailId',
	'mobileNo',
	'employeeId',
	'profession',
	'employeeCategory',
	'employeeMajor',
];

function validateMandatoryMalaffiFieldFilled(valid, field) {
	if (!document.userForm[field].value) {
		document.userForm[field].focus();
		return false;
	}
	return valid;
}

function mandatoryMalaffiFieldsFilled() {
	return MANDATORY_MALAFFI_FIELDS.reduce(validateMandatoryMalaffiFieldFilled, true);
}

function submitFun() {

	// user editing
	var superUser=document.userForm.selUserName.value
	document.userForm.name.value = removeSpaces(document.userForm.name.value);
	var userName = document.userForm.name.value;

	if (userName == "") {
		alert("Please Enter user name");
		document.userForm.name.focus();
		return false;
	}
	if (document.userForm.name.value!="") {
		if(document.userForm.name.value.length<4) {
			alert("User name should be atleast 4 characters");
			document.userForm.name.focus();
			return false;
	 	 }
	 	 if(document.userForm.name.value == 'admin' || document.userForm.name.value == 'InstaAdmin') {
			alert("Super user name already exists. ");
			document.userForm.name.focus();
			return false;
	 	 }
	}

	if (!document.userForm.ssoOnlyUser.checked && document.userForm.password.value == "") {
		alert("Please enter the Password");
		document.userForm.password.focus();
		return false;
	}
	if (!document.userForm.ssoOnlyUser.checked && document.userForm.confirmPassword.value == "") {
		alert("Please enter the Confirm Password");
		document.userForm.confirmPassword.focus();
		return false;
	}
	var fullname = trim(document.userForm.fullname.value);
	document.userForm.fullname.value = fullname;
	if (fullname == '') {
		alert("Please enter the display name.");
		document.userForm.fullname.focus();
		return false;
	}
	var checkStrength = true;
	if(document.userForm.op.value=='edit') {
		var userData = userDetailsMap[userName];
		if(!document.userForm.ssoOnlyUser.checked && userData.password == document.userForm.password.value)
			checkStrength = false;
	}

	if(!document.userForm.ssoOnlyUser.checked && checkStrength) {
		if(!checkPasswordStrength(document.userForm.password)) {
			return false;
		}
	}
	if (document.userForm.roleId.selectedIndex == 0) {
		alert("Please select the application role");
		document.userForm.roleId.focus();
		return false;
	}
	if (document.userForm.remarks.value!="") {
		if (document.userForm.remarks.value.length>99) {
			alert("Remarks length cannot be more than 100 characters");
			document.userForm.remarks.focus();
			return false;
		}
	}

	if(!document.userForm.ssoOnlyUser.checked && !passequal()){
		return false;
	 }

	var roleId = document.userForm.roleId.value;
	if (document.userForm.multiStoreId.value != '' && document.userForm.pharmacyStoreId.value == '') {
		alert("Please select the default store.");
		document.userForm.pharmacyStoreId.focus();
		return false;
	}

	if (modMalaffiEnabled) {
		var selectedHospitalRoles = document.userForm.hospitalRoleIds.selectedOptions;
		for (var i=0; i < selectedHospitalRoles.length; i++) {
			if (malaffiMappedHospitalRoleIds.indexOf(Number(selectedHospitalRoles[i].value)) >= 0 && !mandatoryMalaffiFieldsFilled()) {
				alert ('As per Malaffi Integration requirements some of the mandatory fields are missing.');
				return false;
			}
		}
	}

	if ( (superUser == 'InstaAdmin') || (superUser == 'admin') ) {
		alert ('Superusers cannot be modified');
		return false;
	}

	 var re = /^[0-9]+\.?[0-9]*$/;
	var poLimit=document.userForm.poApprovalLimit.value;
	if (!empty(poLimit) && !re.test(poLimit) ){
		alert("PO Approval Limit should be valid numeric digit");
		document.userForm.poApprovalLimit.focus();
		return false;
	}
	var writeOffLimit = document.userForm.writeOffLimit.value;
	if(!empty(writeOffLimit) && !re.test(writeOffLimit)){
		alert("Writ Off Limit should be valid numeric digit");
		document.userForm.writeOffLimit.focus();
		return false;
	}
	var permissibleDiscountCap = document.userForm.permissibleDiscountCap;
	if (!validateDecimal(permissibleDiscountCap, "Permissible Discount Cap% must be a valid number", 2)) {
		return false;
	}
	if (permissibleDiscountCap.value > 100) {
		alert("Permissible Discount Cap% cannot be greater than 100%");
		document.userForm.permissibleDiscountCap.focus();
		return false;
	}
	document.userForm.submit();
}

/* this method checks the password and confirm password fields*/
function passequal(){
	var str = document.userForm.password.value
		var str1= document.userForm.confirmPassword.value
		if(!(str==str1)){
			alert("Passwords mismatch. Fill again");
			document.userForm.password.value = "";
			document.userForm.confirmPassword.value = "";
			document.userForm.password.focus();
			return false;
		}
	return true;
}



var docAutoComp = null;
function initDoctorDept(dept) {
	if (docAutoComp != null) {
		docAutoComp.destroy();
	}

	var docDeptNameArray = [];
	var jDeptDocList = null;

	if(jDocDeptNameList !=null && jDocDeptNameList.length > 0) {
		if (dept != null && dept != '')
			jDeptDocList = filterList(jDocDeptNameList, 'dept_id', dept);
		else
			jDeptDocList = jDocDeptNameList;

		docDeptNameArray.length = jDeptDocList.length;

		for ( i=0 ; i< jDeptDocList.length; i++){
			var item = jDeptDocList[i];
			docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")";
		}
	}
	if(document.userForm.doctor_name != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(docDeptNameArray);
			docAutoComp = new YAHOO.widget.AutoComplete('doctor_name', 'doc_dept_dropdown', dataSource);
			docAutoComp.maxResultsDisplayed = 20;
			docAutoComp.queryMatchContains = true;
			docAutoComp.allowBrowserAutocomplete = false;
			docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			docAutoComp.typeAhead = false;
			docAutoComp.useShadow = false;
			docAutoComp.minQueryLength = 0;
			docAutoComp.forceSelection = false;
			docAutoComp.textboxBlurEvent.subscribe(function() {
				var dName = document.userForm.doctor_name.value;
				if (dName == '') {
					document.userForm.schedulerDefaultDoctor.value = '';
				}
			});

			docAutoComp.itemSelectEvent.subscribe(function() {
				var dName = document.userForm.doctor_name.value;
				if (dName != '') {
					for ( var i=0 ; i< jDeptDocList.length; i++){
						if(dName == jDeptDocList[i]["doctor_name"]+" ("+jDeptDocList[i]["dept_name"]+")"){
							document.userForm.schedulerDefaultDoctor.value = jDeptDocList[i]["doctor_id"];
							setSelectedIndex(document.userForm.schedulerDepartment, jDeptDocList[i]["dept_id"]);
							break;
						}
					}
				} else {
					document.userForm.schedulerDefaultDoctor.value = '';
					document.userForm.schedulerDepartment.selectedIndex = 0;
				}
			});
		}
	}
}
function setSelectedIndex(opt, set_value) {
  var index=0;
  for(var i=0; i<opt.options.length; i++) {
    var opt_value = opt.options[i].value;
    if (opt_value == set_value) {
      opt.selectedIndex = i;
      return;
    }
  }
}

function sampleCollectionCenterChanges ( userCenter ) {
	if ( userCenter.value == 0 && max_centers_inc_default > 1)
		document.getElementById("sampleCollectionTR").style.display = 'none';
	else {
	/** couple of things should be done like
		1.Showing sample colelction center section
		2.Filter the sample collection center according to centers
	**/

		document.getElementById("sampleCollectionTR").style.display = 'table-row';
		filterSampleCollectionCenters( userCenter );
	}
}

function filterSampleCollectionCenters( userCenter ){
	loadSelectBox( document.userForm.sampleCollectionCenter,filterList(sampleCollectionCentersJSON, "center_id", userCenter.value),
					'collection_center','collection_center_id','-- Select --',null );
}
