var csform = null;
function init() {
	initArrays();
	csform = document.claimSubmissionsForm;
	createToolbar(toolbar);
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');
	var planId		= planObj.value;
	if (insCompObj.selectedIndex != 0)	onChangeInsuranceCompany();
	if (tpaObj.selectedIndex != 0)  onChangeTPA();
	if (catObj.selectedIndex != -1)
		onChangeInsuranceCategory();
	else
		setSelectedIndex(catObj, "");
	setSelectedIndex(planObj, planId);
	showFilterActive(document.claimSubmissionsForm);
	initFilterFieldSearch();
	initPlanTypeAutoComplete();
}


function initFilterFieldSearch(){
	// center_or_account_group
	const dataSource = new YAHOO.util.XHRDataSource(cpath + "/insurance/claimSubmission/getaccountgroups.json");
	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "ac_name"}, {key : "id"}, {key : "accounting_company_name"} ]
	};
	const centerOrAccountGroupAutoComplete = new YAHOO.widget.AutoComplete('center_or_account_group', 'center_or_account_group_dropdown', dataSource);
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

function onSelectAccountCenterGroup(type, args) {
	$('#center_or_account_group_id').val(args[2][1]);
}

function onDeSelectAccountCenterGroup() {
	if($('#center_or_account_group').val() == ''){
		$('#center_or_account_group_id').val('');
	}
}

var toolbar = {
	AccumedUpdate : {
		title : "Accumed Update XML",
		imageSrc : "icons/Run.png",
		href : 'billing/generateEClaim.do?_method=eClaim&testing=N&isAccumed=true&New=N',
		onclick : null,
		description : "Generate Update Accumed XML",
		show : (isAccumed == 'Y')
	},
	AccumedNew : {
		title : "Accumed New XML",
		imageSrc : "icons/Run.png",
		href : 'billing/generateEClaim.do?_method=eClaim&testing=N&isAccumed=true&New=Y',
		onclick : null,
		description : "Generate New Accumed XML",
		show : (isAccumed == 'Y')
	},
	Test: {
		title: "Test",
		imageSrc: "icons/Run.png",
		href: 'billing/generateEClaim.do?_method=eClaim&testing=Y',
		onclick: null,
		description: "Test E-Claim Generation",
		show: (eClaimModule == 'Y' && haadClaimRights == 'A')
	},
	EClaim: {
		title: "Generate E-Claim",
		imageSrc: "icons/Run.png",
		href: '/billing/generateEClaim.do?_method=eClaim&testing=N',
		onclick: null,
		description: "Generate E-Claim",
		show: (eClaimModule == 'Y' && haadClaimRights == 'A')
	},
	ClaimSent: {
		title: "Sent Claim",
		imageSrc: "icons/Send.png",
		href: 'insurance/claimSubmission/markClaimAsSent.htm?',
		onclick: null,
		description: "Sent Claim Submission"
	},
	AddEditBatch: {
		title: "Add/Edit Batch Ref.",
		imageSrc: "icons/Edit.png",
		href: 'billing/claimSubmissionsList.do?_method=getAddorEditBatchRefScreen',
		onclick: null,
		description: "Add/Edit Submission Batch Reference number"
	},
	Rejected: {
		title: "Rejected",
		imageSrc: "icons/Cancel.png",
		href: 'billing/claimSubmissionsList.do?_method=reject',
		onclick: null,
		description: "Submission Rejected"
	},
	Delete: {
		title: "Delete",
		imageSrc: "icons/Delete.png",
		href: 'billing/claimSubmissionsList.do?_method=delete',
		onclick: null,
		description: "Delete submission"
	},
	Download: {
		title: "Download Documents",
		imageSrc: "images/arrow_down.png",
		href: 'billing/claimSubmissionsList.do?_method=downloadDocuments',
		target: "_blank",
		onclick: null,
		show: (advPackagesMod == 'Y'),
		description: "Download Claim Documents"
	},
	DownloadEclaim: {
		title: "Download E-Claim",
		imageSrc: "images/arrow_down.png",
		href: 'billing/claimSubmissionsList.do?_method=downloadEClaimXmlFile',
		onclick: null,
		target: "_blank",
		show: (isAccumed =='Y' ||(eClaimModule == 'Y' && haadClaimRights == 'A')),
		description: "Download EClaim XML"
	},
	UploadEClaim: {
		title: "Upload E-Claim",
		imageSrc: "icons/Run.png",
		href: '/billing/generateEClaim.do?_method=uploadEClaim&testing=N',
		onclick: 'uploadEClaimConfirm',
		target: "_blank",
		description: "Upload E-Claim",
		show: (isAccumed =='Y'|| (eClaimModule == 'Y' && haadClaimRights == 'A'))
	},
	ShowEClaimError: {
		title: "View Error Log",
		imageSrc: "icons/Run.png",
		href: '/billing/generateEClaim.do?_method=showEClaimError',
		onclick: null,
		target: "_blank",
		description: "E-Claim Generation Error",
		show: (isAccumed =='Y' || (eClaimModule == 'Y' && haadClaimRights == 'A'))
	}
};

function uploadEClaimConfirm(anchor, params, id, toolbar) {
	//alert(submission_batch_id);
	//alert(processing_type);
	var processingType = '';
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'processing_type')
			processingType = paramvalue;
	}
	var ok = true;
	if(processingType == 'T')
		ok = confirm("Are you sure want to Upload Test E-Claim!");

	return ok;
}
function doSearch() {
    if (!doValidateDateField(document.getElementById('created_date0'))) {
    	document.getElementById('created_date0').focus();
    	return false;
	}
	if (!doValidateDateField(document.getElementById('created_date1'))){
		document.getElementById('created_date1').focus();
		return false;
	}
    if (!doValidateDateField(document.getElementById('submission_date0'))){
    	document.getElementById('submission_date0').focus();
    	return false;
	}
   	if (!doValidateDateField(document.getElementById('submission_date1'))){
	   	document.getElementById('submission_date1').focus();
   		return false;
	}
	return true;
}
