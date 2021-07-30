var toolbar = {
	PBMPresc: {
		title: "Edit PBM Presc.",
		imageSrc: "icons/Edit.png",
		href: 'PBMAuthorization/PBMPresc.do?_method=getPBMPrescription',
		target:'_blank',
		onclick: null,
		description: "Add/Edit PBM Prescription",
		show: (mod_elaim_pbm_enabled =='Y' && hasPbmFinalizeRights == 'true')
	},
	ERxCons: {
		title: "Edit ERx Cons.",
		imageSrc: "icons/Edit.png",
		href: 'outpatient/OpPrescribeAction.do?_method=list',
		target:'_blank',
		onclick: null,
		description: "Add/Edit ERx Consultation",
		show: (mod_elaim_erx_enabled == 'Y' && hasERxConsAccessRights == 'true')
	},
	ReceiveErxAuth: {
		title: "Download/Receive ERx Auth.",
		imageSrc: "icons/Download.png",
		href: 'ERxPrescription/ERxApproval.do?_method=getERxPriorAuthResponse',
		target:'_blank',
		onclick: null,
		description: "Download/Receive ERx Prior Authorization"
	},
	PresPrint: {
		title: "PBM Prescription Print",
		imageSrc: "icons/Print.png",
		href: '/PBMAuthorization/PBMPrescriptionPrint.do?_method=printPbmPrescriptions',
		target:'_blank',
		onclick: null,
		description: "PBM Print Prescription",
		show: (mod_elaim_pbm_enabled =='Y' && hasPbmFinalizeRights == 'true')
	}
};

var pbmListForm = document.pbmListSearchForm;
psAc = null;

function init() {
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
	createToolbar(toolbar);
	initDocAutoComplete ();

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
	showFilterActive(document.pbmListSearchForm);
	initTooltip('resultTable', extraDetails);

	var phStoreObj = document.getElementById("_phStore");

	if (phStoreObj != null) {
		if (phStoreObj.type == "text" && phStoreObj.value == "") {
		 	document.getElementById('storeErrorDiv').style.display = 'block';
		 	document.getElementById('updateStoreBtn').disabled = true;

		}else if (phStoreObj.type == "select-one" && phStoreObj.options.length == 0) {
			document.getElementById('storeErrorDiv').style.display = 'block';
			document.getElementById('updateStoreBtn').disabled = true;
		}
	}else {
		if (document.getElementById('storeErrorDiv') != null) {
			document.getElementById('storeErrorDiv').style.display = 'none';
			document.getElementById('updateStoreBtn').disabled = false;
		}
	}

	setTimeout("erxApprovalSearch()", 300000);
}

function initDocAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(docList);
		oAutoComp = new YAHOO.widget.AutoComplete('doctor', 'doc_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 20;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
	}

}

function checkAllPrescriptions() {
	var all = pbmFinalizeForm._allPrescriptions;
	var prescs = document.getElementsByName('_pbm_presc_id');
	var success = false;
	if (all.checked) {
		if (prescs.length == undefined) {
			if (!prescs.disabled) {
				prescs.checked = true;
				success = true;
			}
		} else {
			for (var i = 0; i < prescs.length; i++) {
				var el = prescs[i];
				if (!el.disabled) {
					el.checked = true;
					success = true;
				}
			}
		}
	} else {
		if (prescs.length == undefined) {
			prescs.checked = false;
		} else {
			for (var i = 0; i < prescs.length; i++) {
				var el = prescs[i];
				el.checked = false;
				success = false;
			}
		}
	}
	return success;
}

function validateStore() {
	var userStore = document.getElementById("_phStore");
	if (userStore == null || userStore.value == '') {
		alert("No store is selected to save PBM Prescription. Please assign store for the user.");
		return false;
	}
	var all = pbmFinalizeForm._allPrescriptions;
	var prescs = document.getElementsByName('_pbm_presc_id');
	var valid = true;
	if (all.checked && (prescs.length == undefined || prescs.length > 0)) {
		valid = checkAllPrescriptions();
	} else {
		valid = isPrescriptionChecked();
	}

	if (!valid) {
		alert("No prescriptions are selected.");
		return false;
	}

	pbmFinalizeForm._method.value = "savePBMPrescriptionStore";
	pbmFinalizeForm.submit();
	return true;
}


function validateFinalize() {
	var all = pbmFinalizeForm._allPrescriptions;
	var prescs = document.getElementsByName('_pbm_presc_id');
	var valid = true;
	if (all.checked && (prescs.length == undefined || prescs.length > 0)) {
		valid = checkAllPrescriptions();
	} else {
		valid = isPrescriptionChecked();
	}

	if (!valid) {
		alert("No prescriptions are selected.");
		return false;
	}

	pbmFinalizeForm._method.value = "finalizePrescriptions";
	pbmFinalizeForm.submit();
	return true;
}

function isPrescriptionChecked() {
	var prescs = document.getElementsByName('_pbm_presc_id');
	if (prescs != null) {
		if (prescs.length == undefined) {
			if (!prescs.checked) {
				alert('Please select any prescription');
				return false;
			}
		} else {
			if (!prescriptionCheck()) {
				return false;
			}
		}
	}
	return true;
}

function prescriptionCheck() {
	var success = false;
	var prescs = document.getElementsByName('_pbm_presc_id');
	for (var i = 0; i < prescs.length; i++) {
		var el = prescs[i];
		if (el.checked) {
			success = true;
		}
	}
	return success;
}


function erxApprovalSearch() {
	var url = cpath+'/';
	url += 'ERxPrescription/ERxApproval.do';
	url += '?_method=getERxList';
	pbmFinalizeForm.action = url;
	pbmFinalizeForm.submit();
	return true;
}
