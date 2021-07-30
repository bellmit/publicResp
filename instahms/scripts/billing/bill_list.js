/*
 * Functions used by BillList.jsp
 */

/*
 * NOTE: The toolbar description: one entry per menu item should be added. Following fields inside
 * every menu item are supported:
 *   title: What shows in the menu
 *   imageSrc: the path to the image
 *   disabledImageSrc (optional): the disabled image file name is derived from the main image name.
 *     If the disabled image does not follow the convention, then, you can specify a separate
 *     disabled image icon src.
 *   href: Base URL of the screen to be called. All request Params will be appended to this URL.
 *   onclick: event handler to call onclick.
 *   description: a title that will be shown on hovering over the menu item.
 */
var toolbar = {}
	toolbar.Edit={
		title: toolbarOptions["vieweditbill"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'billing/BillAction.do?_method=getCreditBillingCollectScreen',
		onclick: null,
		description: toolbarOptions["vieweditbill"]["description"]
	};

	toolbar.Order= {
		title: toolbarOptions["order"]["name"],
		imageSrc: "icons/Order.png",
		href: 'patients/orders',
		onclick: null,
		description: toolbarOptions["order"]["description"],
		show: (opOrder == 'A'|| ipOrder=='A')
	};

	toolbar.Issue= {
		title: toolbarOptions["issue"]["name"],
		imageSrc: "icons/Collect.png",
		href: 'patientissues/add.htm?',
		onclick: null,
		description: toolbarOptions["issue"]["description"],
		show: (issueRights == 'A')
	};

	toolbar.ChangeBillType= {
		title: toolbarOptions["tobilllater"]["name"],
		imageSrc: "icons/Change.png",
		href: 'pages/BillDischarge/ChangeBillType.do?_method=getChangeBillTypeScreen',
		onclick: null,
		description: toolbarOptions["tobilllater"]["description"],
		show: (allowcreditbilllater == 'A' || roleId == 1 || roleId ==2)
	};

	toolbar.ChangeBillPrimary= {
		title: toolbarOptions["changeprimarystatus"]["name"],
		imageSrc: "icons/Change.png",
		href: 'billing/ChangeBillPrimary.do?_method=getScreen',
		onclick: null,
		description: toolbarOptions["changeprimarystatus"]["description"]
	};

	toolbar.toggleTPA= {
		title: toolbarOptions["changeinsurancestatus"]["name"],
		imageSrc: "icons/Get.png",
		href: 'pages/BillDischarge/ConnectDisconnectTPA.do?_method=getTPAConnectDisconnectScreen',
		onclick: null,
		description: toolbarOptions["changeinsurancestatus"]["description"]
	};

	toolbar.ChangeRatePlan={
		title: toolbarOptions["editrateplanbedtype"]["name"],
		imageSrc: "icons/Change.png",
		href: '/editVisit/changeRatePlan.do?_method=updateRatePlan',
		onclick: null,
		description: toolbarOptions["editrateplanbedtype"]["description"],
		show: (changeRatePlanRights == 'A')
	};

	toolbar.EditClaim= {
		title: toolbarOptions["editclaim"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'billing/editClaim.do?_method=getClaims',
		onclick: null,
		description: toolbarOptions["editclaim"]["description"]
	};

	toolbar.PrintDefault={
		title: toolbarOptions["printdefault"]["name"],
		imageSrc: "icons/Print.png",
		href: 'pages/Enquiry/billprint.do?',
		onclick: 'billPrint',
		description: toolbarOptions["printdefault"]["description"],
		show: (billPrintRights == 'A' || roleId == '1' || roleId == '2')
	};

	toolbar.PrintOther={
		title: toolbarOptions["printother"]["name"],
		imageSrc: "icons/Print.png",
		href: 'pages/BillDischarge/BillList.do?_method=getBillTemplateScreen',
		onclick: null,
		description: toolbarOptions["printother"]["description"],
		show: (billPrintRights == 'A' || roleId == '1' || roleId == '2')
	};
	
	toolbar.CreditNote={
		title: toolbarOptions["createcreditnote"]["name"],
		imageSrc: "icons/Edit.png",
		href: '/billing/'+patientSponsorCreditNotePath+'.do?_method=getCreditNoteScreen',
		onclick: null,
		description: toolbarOptions["createcreditnote"]["description"]		
	};
	
	toolbar.ViewCreditNote={
		title: toolbarOptions["viewcreditnote"]["name"],
		imageSrc: "icons/Edit.png",
		href: '/billing/viewCreditNote.do?_method=viewCreditNote',
		onclick: null,
		description: toolbarOptions["viewcreditnote"]["description"]
	};
	

/* NOTE:
 * In case the menu is not available for any row, then we don't even add it to the toolbar.
 * In case the menu is available for some rows but not others, then we add the menu, but
 * enable/disable it on a per-row basis. For enabling/disabling, pass parameters in the
 * showToolbar call, whereas for hide/show, use show: in the menu definition above.
 */

function billPrint(href, params) {

	var billNo = params['billNo'];
	var visitId = params['visitId'];
	var billType = params['bill_type'];
	var printerType = document.BillSearchForm._printer.value;
	var templateName = billType == 'C' ? billLaterDefaultPrint : billNowDefaultPrint;
	var optionParts  = (templateName).split('-');
	
	href += "&_method=";

	if (optionParts[0] == 'BILL')
		href += "billPrint";
	else if (optionParts[0] == 'EXPS')
		href += "expenseStatement";
	else if (optionParts[0] == 'PHBI')
		href += "pharmaBreakupBill";
	else if (optionParts[0] == 'PHEX')
		href += "pharmaExpenseStmt";
	else if (optionParts[0] == "CUSTOM")
		href += "billPrintTemplate";
	else	{
		alert("Unknown bill print type: " + optionParts[0]);
		return false;
	}
	href += "&printUserName="+userNameInBillPrint;
	if (optionParts[1])
		href += "&detailed="+optionParts[1];

	if (optionParts[2])
		href += "&option="+optionParts[2];
	href +="&billType="+templateName.substring(parseInt(optionParts[0].length)+1,templateName.length);
	href +="&printerType="+printerType;
	window.open(href);
	return false;
}
var theForm = document.BillSearchForm;
var psAc = null;

function init() {
	theForm = document.BillSearchForm;
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	setFocus();
	initBedNames();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
	initTooltip('resultTable', extraDetails);
	document.getElementById('_mr_no').checked = true;
	sortDropDown(theForm.bed_type, '(All)');
}

function setFocus() {
	theForm.mr_no.focus();
}

function doSearch() {
    if (!doValidateDateField(document.getElementById('open_date0'))) {
    	return false;
	}
	if (!doValidateDateField(document.getElementById('open_date1'))){
		return false;
	}
    if (!doValidateDateField(document.getElementById('f_date0'))){
    	return false;
	}
   	if (!doValidateDateField(document.getElementById('f_date1'))){
   		return false;
	}
	return true;
}

function clearFields(bedType) {
	theForm._bed_ward_name.value = '';
	theForm.bed_name.value = '';
	theForm.ward_name.value = '';
}

var autoComp = null;
var bedNamesArray = [];
var bedNamesList = null;

function initBedNames(bedType) {

	if (autoComp != undefined) {
		autoComp.destroy();
	}

	if(bedNames !=null && bedNames.length > 0) {
		if (bedType != null && bedType.value != '') {
			var type = bedType.options[bedType.selectedIndex].value;
			bedNamesList = filterList(bedNames, 'bed_type', type);
		}
		else {
			bedNamesList = bedNames;
		}
		bedNamesArray.length = bedNamesList.length;
		for ( i=0 ; i< bedNamesList.length; i++) {
			var item = bedNamesList[i];
			bedNamesArray[i] = item["bed_name"]+" ("+item["ward_name"]+")";
		}
	}
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(bedNamesArray);
		autoComp = new YAHOO.widget.AutoComplete('_bed_ward_name', 'bedNamesDropDown', dataSource);
		autoComp.maxResultsDisplayed = 10;
		autoComp.queryMatchContains = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = false;
		autoComp.useShadow = false;
		autoComp.minQueryLength = 0;
		autoComp.forceSelection = true;
		autoComp._bItemSelected = true;
		autoComp.formatResult = Insta.autoHighlightWordBeginnings;
		autoComp.textboxBlurEvent.subscribe(function() {
			if (theForm._bed_ward_name.value != '') {
				for (i=0 ; i< bedNamesList.length; i++) {
					var item = bedNamesList[i];
					if (item["bed_name"]+" ("+item["ward_name"]+")" == trim(theForm._bed_ward_name.value)) {
						theForm.bed_name.value = item["bed_name"];
						theForm.ward_name.value = item["ward_name"];
						break;
					}
				}
			}else {
				theForm.bed_name.value = '';
				theForm.ward_name.value = '';
			}
		});
		autoComp.itemSelectEvent.subscribe(function() {
			if (theForm._bed_ward_name.value != '') {
				for (i=0 ; i< bedNamesList.length; i++) {
					var item = bedNamesList[i];
					if (item["bed_name"]+" ("+item["ward_name"]+")" == trim(theForm._bed_ward_name.value)) {
						theForm.bed_name.value = item["bed_name"];
						theForm.ward_name.value = item["ward_name"];
						break;
					}
				}
			}else {
				theForm.bed_name.value = '';
				theForm.ward_name.value = '';
			}
		});
	}
}

function clearSearch(form) {
	clearForm(form);
	// also clear hidden fields that clearForm will ignore
	form.bed_name.value = "";
	form.ward_name.value = "";
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

 function changePrinter() {
	var printer = document.BillSearchForm._printer.value;
	toolbar.Print.href += "printerType="+printer;
}


function updateCreditNotePath(obj) {
	var index = getThisRow(obj).rowIndex - 1;
	var patientWriteofId= "patientWriteoff"+index;
	var sponsorWriteofId= "sponsorWriteoff"+index;
	var patientWriteOffValue = document.getElementById(patientWriteofId).value;
	var sponsorWriteOffValue = document.getElementById(sponsorWriteofId).value;
	
	if(patientWriteOffValue == 'A' && sponsorWriteOffValue != 'A' && sponsorWriteOffValue != 'N') {
		patientSponsorCreditNotePath = 'SponsorCreditNote';
		toolbar.CreditNote={
			title: toolbarOptions["createcreditnote"]["name"],
			imageSrc: "icons/Edit.png",
			disabledImageSrc: "icons/Edit1.png",
			href: '/billing/'+patientSponsorCreditNotePath+'.do?_method=getCreditNoteScreen',
			onclick: null,
			description: toolbarOptions["createcreditnote"]["description"]		
		};
	} else if (patientWriteOffValue != 'A' && (sponsorWriteOffValue == 'A' || sponsorWriteOffValue == 'N') ){
		patientSponsorCreditNotePath = 'PatientCreditNote';
		toolbar.CreditNote={
			title: toolbarOptions["createcreditnote"]["name"],
			imageSrc: "icons/Edit.png",
			disabledImageSrc: "icons/Edit1.png",
			href: '/billing/'+patientSponsorCreditNotePath+'.do?_method=getCreditNoteScreen',
			onclick: null,
			description: toolbarOptions["createcreditnote"]["description"]		
		};
	}
}
		