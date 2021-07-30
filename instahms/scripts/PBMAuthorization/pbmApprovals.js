var toolbar = {
	PBMPresc: {
		title: "Edit PBM Presc.",
		imageSrc: "icons/View.png",
		href: 'PBMAuthorization/PBMPresc.do?_method=getPBMPrescription',
		target:'_blank',
		onclick: null,
		description: "View/Edit PBM Prescription",
		show: (hasPbmFinalizeRights == 'true')
	},
	PriorAuthDownloaded: {
		title: "Download Prior-Auth.",
		imageSrc: "icons/Download.png",
		href: 'PBMAuthorization/PBMApprovals.do?_method=setTransactionDownloaded',
		target:'_blank',
		onclick: null,
		description: "Download Prior Authorization file and set as downloaded."
	},
	UploadTestingAuth: {
		title: "Upload Test Prior-Auth XML.",
		imageSrc: "icons/Upload.png",
		href: '',
		target:'',
		onclick: 'showUploadAuthDialog',
		description: "Upload Prior Authorization file for a patient with testing member id (1116528).",
		//show: (shafafiyaPBMLive == '')
		show: (shafafiyaPBMLive == 'N')
	},
	Sale: {
		title: "Sales",
		imageSrc: "icons/Edit.png",
		href: 'PBMAuthorization/PBMApprovals.do?_method=getSalesScreen',
		target:'',
		onclick: null,
		description: "Sale the PBM Auth. Received Items"
	},
	PresPrint: {
		title: "PBM Prescription Print",
		imageSrc: "icons/Print.png",
		href: '/PBMAuthorization/PBMPrescriptionPrint.do?_method=printPbmPrescriptions',
		target:'_blank',
		onclick: null,
		description: "PBM Print Prescription"
	}
};


function initUploadAuthDialog() {
	uploadDialog = new YAHOO.widget.Dialog("uploadDialog", {
		width: "350px",
		context: ["resultTable", "tr", "br"],
		visible: false,
		modal: true,
		constraintoviewport: true,
	});
	uploadDialog.render();
}

function showUploadAuthDialog(href, params) {

	var id = params['row_id'];

	document.uploadTestingAuthForm.uploadDialogId.value = id;

	var button = document.getElementById("toolbarRow" + id);
	uploadDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	uploadDialog.show();
	return false;
}

function setToolbarRowClass() {
	document.getElementById('uploadDialog').onmouseover = function set() {
	var id = document.uploadTestingAuthForm.uploadDialogId.value;
	if (document.getElementById('uploadDialog_c') != null) {
		if (document.getElementById('uploadDialog_c').style.visibility == 'visible'){
			document.getElementById('toolbarRow' + id).className = 'rowbgToolBar';
		}else
			document.getElementById('toolbarRow' + id).className = '';
		}
	}
}

function uploadSubmit() {
	var id = document.uploadTestingAuthForm.uploadDialogId.value;

	var pbm_presc_id = document.getElementById('pid' + id).value;
	var pbm_request_id = document.getElementById('prid' + id).value;

	if (trim(document.uploadTestingAuthForm.prior_auth_file.value) == '') {
		alert('Upload a Prior-Auth XML file.');
		document.uploadTestingAuthForm.prior_auth_file.focus();
		return false;
	}
	document.uploadTestingAuthForm.action =
		cpath+'/PBMAuthorization/PBMApprovals.do?_method=uploadTestPriorAuth&pbm_presc_id='+pbm_presc_id+'&pbm_request_id='+pbm_request_id;
	document.uploadTestingAuthForm.submit();

	document.getElementById('toolbarRow' + id).className = '';
	uploadDialog.hide();

	return true;
}

function uploadCancel() {
	document.uploadTestingAuthForm.prior_auth_file.value = '';
	var id = document.uploadTestingAuthForm.uploadDialogId.value;
	document.getElementById('toolbarRow' + id).className = '';
	uploadDialog.cancel();
}

function approvalSearch() {
	document.pbmNewApprovalsForm.submit();
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
	var planId		= planObj.value;
	if (insCompObj.selectedIndex != 0) onChangeInsuranceCompany();
	if (tpaObj.selectedIndex != 0) onChangeTPA();
	if (catObj.selectedIndex != -1)
		onChangeInsuranceCategory();
	else
		setSelectedIndex(catObj, "");
	setSelectedIndex(planObj, planId);
	showFilterActive(document.pbmApprovalSearchForm);
	initUploadAuthDialog();
	setToolbarRowClass();
	initTooltip('resultTable', extraDetails);
	setTimeout("getApprovalsOnLoad()", 600000);
}

function getApprovalsOnLoad() {
	document.pbmNewApprovalsForm.submit();
	return true;
}
