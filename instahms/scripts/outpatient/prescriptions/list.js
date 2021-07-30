var psAc = null;
function init() {
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	document.getElementById('_mr_no').checked = true;
	var itemToolbar = {};
	itemToolbar.EditPresc = {
		title: toolbarOptions["editpresc"]["name"], imageSrc: "icons/Edit.png",
		href: uriString+'?_method=show',
		description: toolbarOptions["editpresc"]["description"]
	}

	var patientToolbar = {};
	patientToolbar.order = {
		title: toolbarOptions["order"]["name"], imageSrc: "icons/Edit.png",
		href: 'orders/order.do?_method=getOrders',
		description: toolbarOptions["order"]["description"],
		onclick : 'changeOrderURL',
		target: '_blank'
	};
	patientToolbar.PrintPresc = {
		title: toolbarOptions["printpresc"]["name"], imageSrc: "icons/Edit.png",
		href: 'outpatient/PendingPrescriptionsPrint.do?_method=print',
		description: toolbarOptions["printpresc"]["description"]
	}

	createToolbar(patientToolbar, 'patient');
	createToolbar(itemToolbar, 'item');
}

function changeOrderURL(anchor, params, id, toolbar) {
	var patientId = '';
	var consultationId = '';
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'patient_id')
			patientId = paramvalue;
		if (paramname == 'consultation_id')
			consultationId = paramvalue;
	}

	// selecting a "no report" row, need to add all the checked test Ids to the URL
	// as parameters.
	var checkBoxes = document.getElementsByName('_presc_id_'+patientId+"_"+consultationId);
	var patientPrescIds = new Array();
	if (checkBoxes != null) {
		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].checked)
				patientPrescIds.push(checkBoxes[i].value);
		}
	}

	if (patientPrescIds.length == 0) {
		alert("Please select one or more items to order");
		return false;
	}

	var href = anchor.href;
	for (var i=0; i<patientPrescIds.length; i++) {
		href = href + "&patient_presc_id=" + patientPrescIds[i];
	}
	anchor.href = href;
	return true;
}

function changeStatus() {
 	var status = '';

	if (document.getElementById('_mr_no').checked) {
		status = 'active';
	} else {
		status = 'all';
	}
	if (status == 'active') {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	} else {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	}
 }