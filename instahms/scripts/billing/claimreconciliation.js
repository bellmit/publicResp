var crform = null;
var resubmitform = null;
/*values assigned in claimbillsactivities.jsp*/
var maxResubCount = null;
resubmissionCount = null;

function init() {
	initArrays();
	crform = document.claimReconciliationForm;
	resubmitform = document.claimResubmissionForm;
	Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
	createToolbar(toolbar);
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');
	var planId		= planObj.value;
	if (insCompObj.selectedIndex != 0) onChangeInsuranceCompany();
	if (tpaObj.selectedIndex != 0) onChangeTPA();
	if (catObj.selectedIndex != -1)
		onChangeInsuranceCategory();
	else
		setSelectedIndex(catObj, "");
	setSelectedIndex(planObj, planId);
	showFilterActive(document.claimReconciliationForm);
	initTooltip('resultTable', extraDetails);
	initFilterFieldSearch();
	initPlanTypeAutoComplete();
}

function initPlanTypeAutoComplete(){
	const  dataSource = new YAHOO.util.XHRDataSource(cpath + "/master/insuranceplans/planListByCategoriesAndSponsor.json");
	dataSource.responseSchema = {
		resultsList : "planList",
		fields : [  {key : "plan_name"}, {key : "plan_id"}]
	};
	const planNameAutoComplete = new YAHOO.widget.AutoComplete('plan_name', 'plan_name_dropdown', dataSource);
    planNameAutoComplete.allowBrowserAutocomplete = false;
    planNameAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
    planNameAutoComplete.typeAhead = false;
    planNameAutoComplete.useShadow = false;
    planNameAutoComplete.animVert = false;
    planNameAutoComplete.minQueryLength = 0;
	planNameAutoComplete.forceSelection = true;
	planNameAutoComplete.generateRequest = function(filterText) {
		return "?searchQuery=" + filterText + '&categoryList=' + getSelectedCategories().join(',');
	};

	
	planNameAutoComplete.itemSelectEvent.subscribe((type, args) => $('#plan_id').val(args[2][1]));
	
	planNameAutoComplete.textboxBlurEvent.subscribe(() => {
		if($('#plan_name').val() == ''){
			$('#plan_id').val('');
		}
	});
}


function onChangeInsuranceCategory(){
	var planObj		= document.getElementById('plan_id');

	var planNameObj		= document.getElementById('plan_name');
	var catList		= getSelectedCategories();
	if(catList && catList.length == 1 && catList[0] == '(All)'){
		planNameObj.disabled = true;
	}else{
		planNameObj.disabled = false;
	}
	planObj.value = "";
	planNameObj.value = "";

}

var toolbar = {
	Bills: {
		title: "Claim Bills",
		imageSrc: "icons/View.png",
		href: 'billing/claimReconciliation.do?_method=getClaimBillsActivities',
		onclick: null,
		description: "Show all claim bills"
	},
	Codification: {
		title: "Codification",
		imageSrc: "icons/Edit.png",
		href: 'pages/medicalrecorddepartment/MRDUpdate.do?_method=getMRDUpdateScreen',
		onclick: null,
		description: "Edit Codes",
		show: (eClaimModule == 'Y' && updateMRDRights == 'A')
	},
	VisitEMR: {
		title: "Visit EMR Search",
		imageSrc: "icons/View.png",
		href: '/emr/VisitEMRMainDisplay.do?_method=list',
		onclick: null,
		description: "View EMR Details",
		show: (visitEMRRights == 'A')
	},
	AddAttachment: {
		title: "Add/Edit Attachment",
		imageSrc: "icons/Upload.png",
		href: 'billing/claimReconciliation.do?_method=addOrEditAttachment',
		onclick: null,
		description: "Add or edit an attachment",
		show: (eClaimModule == 'Y')
	},
	Addresubmission: {
		title: "Add to Resubmission",
		imageSrc: "icons/Choose.png",
		href: 'billing/claimReconciliation.do?_method=addToResubmission',
		onclick: null,
		description: "Add to Resubmission"
	},
	Addsubmission: {
		title: "Add to Submission",
		imageSrc: "icons/Choose.png",
		href: 'billing/claimReconciliation.do?_method=addToSubmission',
		onclick: null,
		description: "Add to Submission"
	}
};

function checkAllClaims() {
	var all = resubmitform._allclaims;
	var claims = document.querySelectorAll("input[type='checkbox'][name='claim_id']");
	var success = false;
	if (all.checked) {
		if (claims.length == undefined) {
			if (!claims.disabled) {
				claims.checked = true;
				success = true;
			}
		} else {
			for (var i = 0; i < claims.length; i++) {
				var el = claims[i];
				if (!el.disabled) {
					el.checked = true;
					success = true;
				}
			}
		}
	} else {
		if (claims.length == undefined) {
			claims.checked = false;
		} else {
			for (var i = 0; i < claims.length; i++) {
				var el = claims[i];
				el.checked = false;
				success = false;
			}
		}
	}
	return success;
}

function initFilterFieldSearch(){
	// center_or_account_group
	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/insurance/claimSubmission/getaccountgroups.json");
	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "ac_name"}, {key : "id"}, {key : "accounting_company_name"} ]
	};
	centerOrAccountGroupAutoComplete = new YAHOO.widget.AutoComplete('center_or_account_group', 'center_or_account_group_dropdown', dataSource);
    centerOrAccountGroupAutoComplete.allowBrowserAutocomplete = false;
    centerOrAccountGroupAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
    centerOrAccountGroupAutoComplete.typeAhead = false;
    centerOrAccountGroupAutoComplete.useShadow = false;
    centerOrAccountGroupAutoComplete.animVert = false;
    centerOrAccountGroupAutoComplete.minQueryLength = 0;
	centerOrAccountGroupAutoComplete.forceSelection = true;
	centerOrAccountGroupAutoComplete.formatResult = resultData => {
		console.log(resultData)
		return resultData[0] + ' - ' + resultData[2]

	}
	centerOrAccountGroupAutoComplete.itemSelectEvent.subscribe(onSelectAccountCenterGroup);

    centerOrAccountGroupAutoComplete.textboxBlurEvent.subscribe(onDeSelectAccountCenterGroup);
}

function onSelectAccountCenterGroup(type, args) {
	$('#center_or_account_group_id').val(args[2][1]);
}

function onDeSelectAccountCenterGroup() {
	if($('#center_or_account_group').val() == ''){
		$('#center_or_account_group_id').val('');
	}
}

function checkResubmissionCount() {
	var claims = document.querySelectorAll("input[name='claim_id']:not([type='text'])"); //ignores the claim_id search bar
	var maxResubCounts = document.getElementsByName('max_resubmission_count');
	var resubCounts = document.getElementsByName('resubmission_count');
	var claimIds = document.getElementsByName('claim_ids');
	var lastClaimList = [], maxClaimList = [], finalClaimList = [];
	var lastResubFlag = false, maxResubFlag = false, finalResubFlag = false;
	var errMsg = "";
	for (var i = 0; i < claims.length; i++) {
		//checks max resubmision count on claim reconciliation screen, gets skipped on claim bills screen
		if(claims[i].checked == true && resubCounts[i].value != undefined  && maxResubCounts[i].value != undefined && maxResubCounts[i].value != '') {
			if( resubCounts[i].value == (maxResubCounts[i].value-1) - 1) {
				lastClaimList.push(claimIds[i].value);
				lastResubFlag = true;
			} else if( resubCounts[i].value == maxResubCounts[i].value-1) {
				finalClaimList.push(claimIds[i].value);
				finalResubFlag = true;
			} else if( resubCounts[i].value > maxResubCounts[i].value-1) {
				maxClaimList.push(claimIds[i].value);
				maxResubFlag = true;
			}	
		} 	
	}
	
	//checks max resubmission count on claim bills screen,
	if (resubmissionCount != null && maxResubCount != null && maxResubCount != '' && resubmissionCount == maxResubCount) {
		finalResubFlag = true;
		finalClaimList.push(claims[0].value)
	} else if (resubmissionCount != null && maxResubCount != null && maxResubCount != '' && (resubmissionCount > maxResubCount)) {
		maxResubFlag = true;
		maxClaimList.push(claims[0].value)
	} else if (resubmissionCount != null && maxResubCount != null && maxResubCount != '' && (resubmissionCount == maxResubCount - 1)) {
		lastResubFlag = true;
		lastClaimList.push(claims[0].value)
	}
		
	
	if(lastResubFlag == true) {
		errMsg = "Only one submission remaining for claims : " + lastClaimList;
	}
	if(finalResubFlag == true) {
		errMsg += "This is your last resubmission for claims : " + finalClaimList;
	}
	if(maxResubFlag == true) {
		errMsg += "Max resubmissions exceeded for claims : " + maxClaimList;
		alert(errMsg);
		return false;
	}
	if(errMsg != ""){
		alert(errMsg);
	}
		
	return true;
	
}

function validateResubmission() {
	var all = resubmitform._allclaims;
	var claims = document.querySelectorAll("input[type='checkbox'][name='claim_id']");
	var valid = true;
	if (all.checked && (claims.length == undefined || claims.length > 0)) {
		valid = checkAllClaims();
	} else {
		valid = isClaimChecked();
	}

	if (!valid) {
		alert("No claims are selected.");
		return false;
	}
	if (!checkResubmissionCount())
		return false;
	if (!validateComments())
		return false;
	resubmitform._method.value = "markForResubmission";
	resubmitform.submit();
	return true;
}

function validateClaimClosure() {
	var all = resubmitform._allclaims;
	var claims = document.querySelectorAll("input[type='checkbox'][name='claim_id']");
	var valid = true;
	if (all.checked && (claims.length == undefined || claims.length > 0)) {
		valid = checkAllClaims();
	} else {
		valid = isClaimChecked();
	}

	if (!valid) {
		alert("No claims are selected.");
		return false;
	}
	if (!validateClosureRemarks())
		return false;

	resubmitform._method.value = "claimClosure";
	resubmitform.submit();
	return true;
}

function isClaimChecked() {
	var claims = document.querySelectorAll("input[type='checkbox'][name='claim_id']");
	if (claims != null) {
		if (claims.length == undefined) {
			if (!claims.checked) {
				alert('Please select any claim');
				return false;
			}
		} else {
			if (!claimCheck()) {
				return false;
			}
		}
	}
	return true;
}

function claimCheck() {
	var success = false;
	var claims = document.querySelectorAll("input[type='checkbox'][name='claim_id']");
	for (var i = 0; i < claims.length; i++) {
		var el = claims[i];
		if (el.checked) {
			success = true;
		}
	}
	return success;
}

function FormatTextAreaValues(vText) {
	var vRtnText = vText;
	while (vRtnText.indexOf("\n") > -1) {
		vRtnText = vRtnText.replace("\n", " ");
	}
	while (vRtnText.indexOf("\r") > -1) {
		vRtnText = vRtnText.replace("\r", " ");
	}
	return vRtnText;
}

function formatCommentValue() {
	document.getElementById("_comments").value = FormatTextAreaValues(document.getElementById("_comments").value);
}

function formatClosureRemarksValue() {
	document.getElementById("_closure_remarks").value = FormatTextAreaValues(document.getElementById("_closure_remarks").value);
}

function UnFormatTextAreaValue(vText) {
	var vRtnText = vText;
	while (vRtnText.indexOf("~") > -1) {
		vRtnText = vRtnText.replace("~", "\n");
	}
	while (vRtnText.indexOf("^") > -1) {
		vRtnText = vRtnText.replace("^", "\r");
	}
	return vRtnText;
}

function validateComments() {
	formatCommentValue();
	var type = document.getElementById("_resubmission_type");
	var comment = document.getElementById("_comments");
	if (type.value == '') {
		alert("Please select resubmission type.");
		type.focus();
		return false;
	}
	if (trim(comment.value) == '') {
		alert("Please enter resubmission comments.");
		comment.focus();
		return false;
	}

	type.value = trim(type.value);
	comment.value = trim(comment.value);

	return true;
}

function validateClosureRemarks() {
	formatClosureRemarksValue();
	var type = document.getElementById("_closure_type");
	var rejCat = document.getElementById("_claim_rejection_reasons_drpdn");
	var remarks = document.getElementById("_closure_remarks");
	if (type.value == '') {
		alert("Please select closure type.");
		type.focus();
		return false;
	}
	if (trim(remarks.value) == '') {
		alert("Please enter closure remarks.");
		remarks.focus();
		return false;
	}
	if(type.value == 'D' && rejCat.value == ''){
		alert("Please Select Rejection Reason");
		rejCat.focus();
		return false
	}

	type.value = trim(type.value);
	remarks.value = trim(remarks.value);

	return true;
}

function enableResubmissionFields() {
	var type = document.getElementById("_resubmission_type");
	var comment = document.getElementById("_comments");
	var resubActionCheck = document.getElementById("resubActionChk");
	if (resubActionCheck != null) {
		if (resubActionCheck.checked && resubActionCheck.value == 'markForResubmission') {
			type.disabled = false;
			comment.disabled = false;
		} else {
			type.disabled = true;
			comment.disabled = true;
		}
	}
}

function clearResubmissionFields() {
	var type = document.getElementById("_resubmission_type");
	var comment = document.getElementById("_comments");
	var resubActionChk = document.getElementById("resubActionChk");
	if (resubActionChk != null) {
		if (resubActionChk.checked && resubActionChk.value == 'unmarkForResubmission') {
			type.value = "";
			comment.value = "";
		}
	}
}

function validateClaim() {
	var actionCheck = document.getElementById("actionChk");
	var resubActionCheck = document.getElementById("resubActionChk");
	var claimAction = "";
	if (actionCheck != null && resubActionCheck != null && actionCheck.checked && resubActionCheck.checked) {
		alert('Cannot select multiple actions. Please select only one.');
		actionCheck.checked = false;
		resubActionCheck.checked = false;
		return false;
	}
	else if (actionCheck != null && actionCheck.checked) {
			claimAction = actionCheck.value;
		} else if (resubActionCheck != null && resubActionCheck.checked){
			claimAction = resubActionCheck.value;
		} else {
			claimAction = "";
		}

	//Selecting rejection reason is mandatory when "Denial Accepted" closure type is selected at claim level.
	var claimClosureTypeDnObj = mainform.closure_type;
	var claimRejReasonDnObj = mainform.claim_rejection_reasons_drpdn;

	if(allowDenialAccepted && claimClosureTypeDnObj != null && claimRejReasonDnObj != null) {
		if(claimClosureTypeDnObj.value != null && claimClosureTypeDnObj.value != ''
							&& claimClosureTypeDnObj.value == 'D') {
			if(claimRejReasonDnObj.value != null && claimRejReasonDnObj.value != ''){}
			else {
				alert('Please Select Rejection Reason at claim level');
				return false;
			}
		}
	}

	// when all items denial accepted, dont allow resubmission with internal complaint.
	var type = document.getElementById("_resubmission_type");

	if(actionCheck != null && actionCheck.checked && type.value == 'internal complaint' && !isInterCompAllowed) {
		alert("\t No activities found to mark claim for resubmission\n\t\t\t with Internal Complaint");
		type.focus();
		return false;
	}

	if(actionCheck != null && actionCheck.checked && type.value == 'internal complaint' && hasExcessAmtNotDenialAcceptExist) {
		alert("\t Some activities received full/excess amount.\nPlease do activity denial acceptance to "+
		"mark claim for\n\tresubmission with Internal Complaint.");
		type.focus();
		return false;
	}

	if (claimAction == 'markForResubmission') {
		var closure = document.getElementById("closure_type");
		if(closure != null && closure !='')
			setSelectedIndex(closure, "");
		document.getElementById("action_remarks").value = "";
		if (!checkResubmissionCount())
			return false;
		if (!validateComments()) 
			return false;
	}
	if (claimStatus == 'M') {
		var resubtype = document.getElementById("_resubmission_type");
		var oldresubtype = document.getElementById("_old_resubmission_type");
		var comments = document.getElementById("_old_comments");
		var oldcomments = document.getElementById("_comments");
		if ((trim(oldresubtype.value) != trim(resubtype.value)) ||
			(trim(oldcomments.value) != trim(comments.value))) {

			resubtype.value = trim(resubtype.value);
			claimAction = "markForResubmission";
		}
	}
	if (actualClaimStatus == 'Denied') {
		var closure = document.getElementById("closure_type");
		if (closure != null && !closure.disabled && trim(closure.value) != "") {
			var ok = confirm("Are you sure you want to close the claim with " +
				closure.options[closure.options.selectedIndex].text + " ?");
			if (!ok) {
				closure.options.selectedIndex = 0;
				document.getElementById("action_remarks").value = "";
				return false;
			} else {
				var oldclosure = document.getElementById("old_closure_type");
				var actionremarks = document.getElementById("action_remarks");
				var oldactionremarks = document.getElementById("old_action_remarks");
				if ((null != actionremarks) &&
					(('' == trim(actionremarks.value))
						|| (trim(oldactionremarks.value) == trim(actionremarks.value))
						&& (oldclosure.value != closure.value))) {
					alert("Enter remarks");
					actionremarks.focus();
					return false;
				}
				claimAction = "updateClaim";
			}
		}
	}
	if (actualClaimStatus == 'Sent') {
		var actionCheckObj = document.getElementById("actionChk");
		if (actionCheckObj != null && actionCheckObj.checked) {
			var ok = confirm("Are you sure you want to mark the claim as Denied ?");
			if (!ok) {
				actionCheckObj.checked = false;
				return false;
			} else {
				var denialremarks = document.getElementById("denial_remarks");
				var olddenialremarks = document.getElementById("old_denial_remarks");
				if ((null != denialremarks) &&
					(('' == trim(denialremarks.value))
						|| (trim(olddenialremarks.value) == trim(denialremarks.value))	)) {
					alert("Enter denial remarks");
					denialremarks.focus();
					return false;
				}
				claimAction = "markAsDenied";
			}
		}
	}
	document.mainform._method.value = claimAction;
	return true;
}

function disableOrEnableReconClaimRejReason() {
	var claimClosureTypeDnObj = document.getElementById("_closure_type");
	var claimRejReasonDnObj = document.getElementById("_claim_rejection_reasons_drpdn");

	if(claimClosureTypeDnObj != null && claimClosureTypeDnObj.value == 'D')
		claimRejReasonDnObj.disabled = false;
	else {
		claimRejReasonDnObj.selectedIndex=0;
		claimRejReasonDnObj.disabled = true;
	}
}
