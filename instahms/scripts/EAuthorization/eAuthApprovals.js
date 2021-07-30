var planList       = [];
var toolbar = {
	EAuthPresc: {
		title: "Edit Prior Auth Presc.",
		imageSrc: "icons/Edit.png",
		href: 'EAuthorization/EAuthPresc.do?_method=getEAuthPrescription',
		target:'_blank',
		onclick: null,
		description: "View/Edit Prior Auth Prescription"
	}
	/*,
	PriorAuthDownloaded: {
		title: "Download Prior Auth.",
		imageSrc: "icons/Download.png",
		href: 'EAuthorization/EAuthApproval.do?_method=setTransactionDownloaded',
		target:'',
		onclick: null,
		description: "Download Prior Authorization file and set as downloaded."
	}*/
};

function approvalSearch() {
	document.preauthNewApprovalsForm.submit();
	return true;
}

psAc = null;

function init() {
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
	createToolbar(toolbar);

	initArrays();
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');
	if (insCompObj.selectedIndex != 0) onChangeInsuranceCompany();
	if (tpaObj.selectedIndex != 0) onChangeTPA();
	if (catObj.selectedIndex != -1)
		onChangeOfInsuranceCategory();
	else
		setSelectedIndex(catObj, "");
	var urlParams = new URLSearchParams(window.location.search);
	var planId = urlParams.get('plan_id');
	setSelectedIndex(planObj, planId);
	showFilterActive(document.preauthApprovalSearchForm);
	setTimeout("getApprovalsOnLoad()", 50000);
}

function getApprovalsOnLoad() {
	document.preauthNewApprovalsForm.submit();
	return true;
}

function onChangeOfInsuranceCategory() {
	var catList = getSelectedCategories();
	var tpaId = document.getElementById('tpa_id').value;
	planList = getPlanList(catList,tpaId)
	onChangeInsuranceCategory();
}

function getPlanList(categoryList, sponsorId) {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	url = cpath
			+ "/master/insuranceplans/planListByCategoriesAndSponsor.json?categoryList="
			+ categoryList;
	if (sponsorId) {
		url += "&sponsorId=" + sponsorId;
	}
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			var responseObj = JSON.parse(ajaxobj.responseText);
			return responseObj.planList;
		}
	}
	return [];
}
