var bednamesAC;
var toolbar = {
	ModifyBedStatus:{
		title: 'Modify Bed Status',
		imageSrc: "icons/Replace.png",
		href: 'pages/ipservices/BedView.do?_method=getBedStatusScreen',
		description: 'Put bed under Cleaning, Maintainance or Available '
	},
	AllocateBed:{
		title: 'Allocate Bed',
		imageSrc: "icons/Add.png",
		href: 'pages/ipservices/BedView.do?_method=getAllocateBedScreen',
		description: 'Allocate A patient into this bed'
	},
	Bed: {
		title: "Bed Details",
		imageSrc: "icons/View.png",
		href: 'pages/ipservices/BedDetails.do?_method=getIpBedDetailsScreen',
		onclick: 'validateBillStatus',
		description: "View or Edit Bed Details"
	},
	Order: { title: "Order",
		imageSrc: "icons/Order.png",
		href: 'patients/orders',
		onclick: null,
		description: "",
		show: (ipOrder=='A')
	},
	PrescribeDiet: { title: "Prescribe Diet",
		imageSrc: "icons/Order.png",
		href: 'pages/ipservices/dietPrescribe.do?_method=getPrescriptionScreen',
		onclick: null,
		description: "",
		show: (dietPrescAvailable == 'Y')
	},
	Issue: {
		title: "Issue",
		imageSrc: "icons/Collect.png",
		href: 'stores/StockPatientIssue.do?_method=getPatientIssueScreen',
		onclick: null,
		description: "Issue Inventory items to patient",
		show: (issueRights == 'A')
	},

	DischargeSummary: { title: "Discharge Summary",
		imageSrc: "icons/Edit.png",
		href: 'dischargesummary/discharge.do?_method=addOrEdit',
		onclick: null,
		description: "",
		show : (DischargeModuleEnabled == 'Y')
	},
	Discharge: {
			title: "Discharge",
			imageSrc: "icons/Signoff.png",
			href: 'discharge/DischargePatient.do?_method=getDischargeDetails',
			description: null,
			show : (DischargeModuleEnabled == 'Y')
		}
};
function validateDischarge (anchor, params, id, toolbar) {
		return funDischarge(params.mrno, params.patientid, params.billStatusOk,params.paymentOk, id);
	}

function validateBillStatus(anchor, params, id, toolbar) {
	if(params != null){
		return checkBillStatus('Bed Details', params.mrno, params.patientid);
	}else{
		return true;
	}
}
function funDischarge(d_mrno,d_patId,d_billstatusOk,d_paymentOk,index){
	var mesage = "Do you want to Discharge?";
	if (d_billstatusOk != 'true') {
  		alert("There are pending bills for this patient which need action. Cannot discharge.");
  		return false;
  	}else {
  		var ajaxobj = newXMLHttpRequest();
		var url = cpath+'/pages/ipservices/Ipservices.do?_method=ajaxPendingTestsCheck&visitId='+d_patId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST",url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
				if (reqObject.responseText == 'Pending') {
					mesage = "Some tests are pending for this visit,Still want to Discharge?";
				}
			}
		}
		if(!confirm(mesage))
	    	return false;
	}
	return true;
}
function init() {
		initMrNoAutoComplete(cpath, 'mrno', 'mrnoContainer', 'active');
		initBedNamesAutocomplete();
		createToolbar(toolbar);
	}
function initBedNamesAutocomplete(){
			if (bednamesAC != null)
				bednamesAC.destroy();
			var wardWiseBednames = filterBedNames(bedNames);
			var datasource = new YAHOO.widget.DS_JSArray(wardWiseBednames);
			bednamesAC = new YAHOO.widget.AutoComplete('bed_name','bedNamesContainer', datasource);
			bednamesAC.maxResultsDisplayed = 15;
			bednamesAC.allowBrowserAutocomplete = false;
			bednamesAC.prehighlightClassName = "yui-ac-prehighlight";
			bednamesAC.typeAhead = false;
			bednamesAC.useShadow = false;
			bednamesAC.minQueryLength = 1;
			bednamesAC.animVert = false;
			bednamesAC.autoHighlight = false;
			bednamesAC.forceSelection = true;
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
		}  else if ( key == 'PrescribeDiet' ) {
			// credit bill not required for prescriptions
			return true;
		}
	}
}
function checkBillStatus(whichLink,mrno,visitid,link){
	var ajaxobj = newXMLHttpRequest();
	var url = cpath+'/pages/ipservices/Ipservices.do?_method=ajaxCreditBillCheck&visitId='+visitid;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			if (reqObject.responseText != 'CreditBillExists') {
				if ((whichLink=='Bed Details' && link=='Allocate Bed') || whichLink=='OT' || whichLink=='Prescribe' || whichLink == 'Add Doctor Visit') {
					if (link == 'Discharge Summary' || link == 'OT Report' || link == 'Prescribe Diet') {
						return true;
					} else {
						alert("Patient does not have open credit bill");
						return false;
					}
				}
			}
		}
	}
	return true;
}
function filterBedNames(bedNamesList){
	var filteredList = new Array();
	for (var i=0; i<bedNamesList.length; i++) {
		if (bedNames[i]['ward_no'] == document.ipdashboardform.ward_no.value) {
			filteredList.push(bedNamesList[i].bed_name);
		}else if(document.ipdashboardform.ward_no.value == ''){
			filteredList.push(bedNamesList[i].bed_name);
		}
	}
	return filteredList;
}