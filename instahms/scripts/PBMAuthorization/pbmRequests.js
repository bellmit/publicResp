var toolbar = {
	ViewPBMReq: {
		title: "View",
		imageSrc: "icons/View.png",
		href: 'PBMAuthorization/PBMRequests.do?_method=viewPBMRequestXML',
		target:'_blank',
		onclick: null,
		description: "View PBM Request XML."
	},
	SendPBMReq: {
		title: "PBM Request",
		imageSrc: "icons/Send.png",
		href: 'PBMAuthorization/PBMRequests.do?_method=sendPBMRequest',
		target:'',
		onclick: null,
		description: "Generate and Sent PBM Request XML."
	},
	ResubmitPBMReq: {
		title: "PBM Resubmit",
		imageSrc: "icons/Resend.png",
		href: 'PBMAuthorization/PBMRequests.do?_method=sendPBMRequest',
		target:'',
		onclick: null,
		description: "Re-Generate and Resubmit PBM Request XML."
	},
	CancelPBMReq: {
		title: "PBM Cancel",
		imageSrc: "icons/Cancel.png",
		href: 'javascript:void',
		onclick: 'checkAllItemsReturned',
		description: "Cancel PBM Authorization."
	},
	PBMClone: {
		title: "Clone PBM Presc.",
		imageSrc: "icons/Edit.png",
		href: 'PBMAuthorization/PBMPresc.do?_method=clonePrescription',
		target:'_blank',
		onclick: null,
		description: "Clone PBM Prescription"
	}
};

psAc = null;

function checkAllItemsReturned(anchor, params, id, toolbar){

	// var row = document.getElementById('toolbarRow' + id);
	var prescID = null;
	var consultationID = null;
	var patientID = null;
	var allItemsReturned = null;
	var prescStatus = null;
	for (var paramname in params) {
		var paramvalue = params[paramname];
		if(paramname == 'pbm_presc_id')
			prescID = paramvalue;
		if(paramname == 'consultation_id')
			consultationID = paramvalue;
		if(paramname == 'patient_id')
			patientID = paramvalue;
		if(paramname == 'all_items_returned')
			allItemsReturned = paramvalue;
		if(paramname = 'presc_status')
			prescStatus = paramvalue;
	}

	if(prescStatus != 'N'){
		if(allItemsReturned){
			anchor.href = 'PBMRequests.do?_method=cancelPBMRequest&pbm_presc_id='+prescID+'&consultation_id='+consultationID+'&patient_id='+patientID;
		}else{
			var medicineNames = getMedicinesNotReturned(prescID);
			anchor.href = '#';
			alert("Please return medicines "+ medicineNames +" to cancel a pbm request.");
		}
	}else{
		anchor.href = 'PBMRequests.do?_method=cancelPBMRequest&pbm_presc_id='+prescID+'&consultation_id='+consultationID+'&patient_id='+patientID;
	}
	return true;
}

function getMedicinesNotReturned(prescID){

	var medicineNames = null;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/PBMAuthorization/PBMRequests.do?_method=getNotReturnedMedicineNames&pbm_presc_id='+prescID;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var medicines =" + ajaxobj.responseText);
				if (!empty(medicines)) {
					medicineNames = medicines;
				}
			}
		}
	}
	return medicineNames;

}

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
	showFilterActive(document.pbmRequestSearchForm);
}
