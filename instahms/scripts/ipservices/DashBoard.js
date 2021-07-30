var toolbar = {

	PendingOperations: { title: 'Pending Operations', imageSrc: 'icons/Edit.png',
		href: 'otservices/PendingOperations.do?_method=getPendingrConductScreen',
		show: pending_operation_rights
	},
	IPEmr: {
		title: "IP EMR(Doctor Order)",
		imageSrc: "icons/Order.png",
		href: '/wardactivities/PatientSummary.do?_method=list',
		onclick: 'changeIPCaseSheetURL',
		description: "IP EMR(Doctor Order/Case Sheet)",
		show : new_ipemr
	},
	WardServices: { title: "Patient Ward Activities",
		imageSrc: "icons/Order.png",
		href: 'pages/wardactivities/PatientActivitiesAction.do?_method=list',
		onclick: null,
		description: "View Patient Ward Activities",
		show :(wardServicesActivated == 'Y')
	},
	PrescribeDiet: { title: "Prescribe Diet",
		imageSrc: "icons/Order.png",
		href: 'pages/ipservices/dietPrescribe.do?_method=getPrescriptionScreen',
		onclick: null,
		description: "",
		show: (dietPrescAvailable == 'Y')
	},
	VisitEMR : {
		title: "Visit EMR Search",
		imageSrc: "images/view.gif",
		href: '/emr/VisitEMRMainDisplay.do?_method=list',
		onclick: null,
		description: "View EMR pertaining to this visit",
		target:"_blank"
	},
	EMR : {
		title: "Patient EMR",
		imageSrc: "icons/View.png",
		href: '/emr/EMRDisplay.do?_method=list',
		onclick: null,
		description: "View EMR pertaining to this patient",
		target:"_blank"
	},
	AddGenDocument : {
		title : "Add Generic Document",
		imageSrc : "icons/Add.png",
		href: 'pages/GenericDocuments/GenericDocumentsAction.do?_method=addPatientDocument&addDocFor=visit',
		description : "Add Patient Generic Document",
		show : (generic_docs_list  == 'A')
	},
	GenFormList : {
		title : "Generic Forms List",
		imageSrc : 'icons/Edit.png',
		href : '/GenericForms/GenericFormsAction.do?_method=list',
		description : 'Patient Generic Forms List',
		show : (generic_forms_list == 'A')
	},
	AddGenForm : {
		title : "Add Generic Form",
		imageSrc : "icons/Add.png",
		href: '/GenericForms/GenericFormsAction.do?_method=getChooseGenericFormScreen',
		description :'Add Patient Generic Form',
		show : (generic_forms_list == 'A')
	},
	Issue: {
		title: "Issue",
		imageSrc: "icons/Collect.png",
		href: 'patientissues/add.htm?',
		onclick: null,
		description: "Issue Inventory items to patient",
		show: (issueRights == 'A')
	},
	Indent: {
		title: "Patient Indent",
		imageSrc: "icons/Add.png",
		href: 'stores/PatientIndentAdd.do?_method=addshow',
		onclick: null,
		description: "Add Patient Indent",
		show: patient_indent_rights
	},
	Return: {
		title: "Patient Return Indent",
		imageSrc: "icons/Add.png",
		href: 'stores/PatientIndentAddReturn.do?_method=addshow',
		onclick: null,
		description: "Add Patient Return Indent",
		show: patient_return_indent_rights
	},
	Discharge : {
		title : "Discharge",
		imageSrc : "icons/Edit.png",
		href: '/discharge/DischargePatient.do?_method=getDischargeDetails',
		description :'Edit Discharge Screen',
		show : (DischargeModuleEnabled == 'Y')
	},
	DischargeMedication : {
		title : "Discharge Medication",
		imageSrc : "icons/Add.png",
		href: '/pages/dischargeMedication.do?_method=getDischargeMedicationScreen',
		description :'Add/Edit Discharge Medication',
		show : (DischargeModuleEnabled == 'Y')
	},
	DischargeSummary: { title: "Discharge Summary",
		imageSrc: "icons/Edit.png",
		href: 'dischargesummary/discharge.do?_method=addOrEdit',
		onclick: 'changeDischargeURL',
		description: "",
		show : (DischargeModuleEnabled == 'Y')
	}
};

function changeIPCaseSheetURL( anchor, params, id, toolbar ) {
    anchor.href = cpath + '/ipemr/index.htm#/filter/default/patient/' + params.mr_no + '/ipemr/visit/'  + params.patientId + '?retain_route_params=true';
 }

function changeDischargeURL( anchor, params, id, toolbar ) {
	anchor.href = cpath + '/inpatients/dischargesummary/index.htm#/filter/default/patient/' + encodeURIComponent(params.mr_no) + '/dischargesummary?retain_route_params=true';
}

function setOnClickEvent(mrNo, patientId) {
	var status = true;
	var anchor;

	for (var key in toolbar) {
		anchor = document.getElementById('toolbarAction_default'+key);
		if (anchor == null)
			continue;

		if (key == 'Order') {
			anchor.onclick = "return checkBillStatus('Prescribe','" + mrNo + "','" + patientId + "','Order');";
		}else if ( key == 'Discharge' ) {
			anchor.onclick = "return checkBillStatus('Prescribe','" + mrNo+"','"+ patientId+"', 'Discharge Summary');";
		} else if (key == 'OTServices' ) {
			anchor.onclick = "return checkBillStatus('OT','" + mrNo + "','" + patientId + "', 'OT');";
		} else if ( key == 'PrescribeDiet' ) {
			// credit bill not required for prescriptions
			return true;
		}
	}
}

	function onKeyPressMrno(e) {
		if (isEventEnterOrTab(e)) {
			return onChangeMrno();
		} else {
			return true;
		}
	}

	function onChangeMrno() {
		var theForm = document.ipdashboardform;
		var mrnoBox = theForm.mr_no;

		// complete
		var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

		if (!valid) {
			alert("Invalid MR No. Format");
			theForm.mrno.value = ""
			theForm.mrno.focus();
			return false;
		}
	}

	function getValue(obj){
		for(var i=0; i<obj.length; i++){
		alert(obj[i].selected);
		if(obj[i].selected){
			obj[i].selected = false;
		}else{
			obj[i].selected = true;
		}
		}
	}

	function clearSearch() {
		var theForm = document.ipdashboardform;
		theForm.fdate.value = "";
		theForm.tdate.value = "";
		theForm.firstName.value = "";
		theForm.lastName.value = "";
		theForm.mrNo.value = "";
		for (i=0;i<theForm.wards.length; i++){
			theForm.wards[i].selected = false;
		}
		for (i=0;i<theForm.doctors.length; i++){
			theForm.doctors[i].selected = false;
		}
	}

	function init() {
		initMrNoAutoComplete(cpath, 'mrno', 'mrnoContainer', 'active');
		createToolbar(toolbar);
	}
