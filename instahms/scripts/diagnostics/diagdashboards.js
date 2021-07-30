var patientToolbar = null;
var collectSampleUrl = (urlRights.lab_pending_samples_search == 'A' ? 'PendingSamplesSearch' : 'PendingSamples');

var baseUrl = 'pages/DiagnosticModule/laboratory.do';
var basePrintUrl = 'pages/DiagnosticModule/DiagReportPrint.do';
var extraDetails = [];

var acManageReports = false;
var acAssignOuthouse = false;
var acEditResults = false;
var acCancelTests = false;
if (category == 'DEP_LAB') {
	acManageReports = urlRights.lab_manage_reports == 'A';
	acAssignOuthouse = urlRights.lab_select_outhouse == 'A';
	acEditResults = urlRights.lab_edit_results == 'A';
	acCancelTests = urlRights.cancel_pescription == 'A';
} else {
	acManageReports = urlRights.rad_manage_reports == 'A';
	acAssignOuthouse = urlRights.rad_select_outhouse == 'A';
	acEditResults = urlRights.rad_edit_results == 'A';
	acCancelTests = urlRights.cancel_radiology == 'A';
}

if (category == "DEP_RAD") {
	baseUrl = 'pages/DiagnosticModule/radiology.do';
}
var baseModule = category == 'DEP_LAB' ? 'Laboratory' : 'Radiology';
var module = category == 'DEP_LAB'? 'Lab' : 'Radiology';

patientToolbar = {};
	patientToolbar.AssignOuthouse = { title: toolbarOptions["selectouthouse"]["name"],
		imageSrc: 'icons/Edit.png', href: baseModule + '/selectouthouse.do?_method=getOuthouseScreen',
		show: acAssignOuthouse
	};
	patientToolbar.Manage = { title: toolbarOptions["managereports"]["name"],
		imageSrc: 'icons/Edit.png', href: baseModule + '/managereports.do?_method=getLabReport',
		show: acManageReports
	};
	patientToolbar.collectsample = { title: toolbarOptions["collectsample"]["name"],
		imageSrc: 'icons/Edit.png',
		href: 'Laboratory/'+collectSampleUrl+'.do?_method=getSampleCollectionScreen&title=Collect Sample&default_sample_status=C',
		show: (category == 'DEP_LAB' && (urlRights.lab_pending_samples == 'A' || urlRights.lab_pending_samples_search == 'A'))
	};
	patientToolbar.Cancel = { title: toolbarOptions["canceltests"]["name"],
		imageSrc: 'icons/Edit.png',
		href: baseModule + '/canceltest.do?_method=cancelPrescription',
		show: acCancelTests
	};
	patientToolbar.PrintWorkSheet = { title: toolbarOptions["printsampleworksheet"]["name"],
		imageSrc: 'icons/Print.png',
		href: 'Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet',
		show: (category == 'DEP_LAB'),
		target: '_blank'
	};

var testToolbar = {}
	testToolbar.Edit = { title: toolbarOptions["vieweditresults"]["name"],
		imageSrc: 'icons/Edit.png', href: baseModule + '/editresults.do?_method=getBatchConductionScreen',
		onclick : 'changeURL',
		show: acEditResults
	};
	testToolbar.ReconductTest ={
		title: toolbarOptions["reconduct"]["name"],
		imageSrc: 'icons/Redo.png',
		onclick : 'changeReconductURL',
		href:'DiagnosticLabModule/'+module+'ReconductTestList.do?_method=getReconductTestListScreen',
		target:'_blank'
	};
	testToolbar.Print = { title: toolbarOptions["printreport"]["name"],
		imageSrc: 'icons/Print.png', href: basePrintUrl + '?_method=printReport', target: '_blank'
	};


function create() {
	createToolbar(patientToolbar, 'patient');
	createToolbar(testToolbar, 'test');
	initMrNoAutoComplete(cpath);
}

function init(){
	 autoCompleteTest();
	 autoCompleteHistoCytoShortImpression();
	 autoCompleteOutHouse();
	 autoCompleteInHouse();
	 if (category == 'DEP_LAB'){
	 	document.getElementById('sample_no').focus();
	 	loadPrescribedDoctors();
	 	refDocAutoComplete(cpath, '_referaldoctorName', 'reference_docto_id', 'referalNameContainer', '/Laboratory/schedulesearch.do');
		prescribedDoAutoComplete('pres_doctor', '_doctor_name', 'prescribedDocContainer');
	 }

	 if(category == 'DEP_RAD'){
     	    loadPrescribedDoctors();
     	    prescribedDoAutoComplete('pres_doctor', '_doctor_name', 'prescribedDocContainer');
     }

     if ( document.getElementById('dashboardTable') != null){
			initTooltip('dashboardTable', extraDetails);
	 }
}


function clearSearch() {

	var theForm = document.diagcenterform;
	theForm.fdate.value = "";
	theForm.tdate.value = "";
	theForm.rfdate.value = "";
	theForm.rtdate.value = "";
	theForm.department.value = "";
	theForm.diagname.value = "";
	theForm.patientAll.checked = true;
	theForm.mrno.value = "";
	theForm.labno.value = "";
	theForm.patientName.value = "";
	theForm.inhouse.value = "";
	theForm.outhouse.value = "";
	theForm.showOnlyInhouseTests.checked = false;
	theForm.showOnlyouthouseTests.checked = false;
}

function changeReconductURL( anchor, params, id, toolbar ){

	var reportId = null;
	var incoming = false;
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'reportId')
			reportId = paramvalue;
		if ( paramname == 'hospital' && paramvalue == 'incoming' )
			incoming = true;
	}
	if (reportId != '') {
		// selecting a report for reconduction
		if ( incoming ) {
			var href = anchor.href;
			href = href.replace(module+'ReconductTestList',module+'IncomingReconductTestList');
			anchor.href = href;
		}
		return true;
	}

	// selecting a "no report" row, need to add all the checked test Ids to the URL
	// as parameters.
	var row = document.getElementById('toolbarRow' + id);
	var checkBoxes = getElementsByName(row, 'forEdit');
	var testIds = new Array();
	if (checkBoxes != null) {
		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].checked)
				testIds.push(checkBoxes[i].value);
		}
	}

	if (testIds.length == 0) {
		showMessage("js.laboratory.radiology.reportlist.selecttest.edit");
		return false;
	}
	if ( incoming )
		anchor.href = 'DiagnosticLabModule/'+module+'IncomingReconductTestList.do?_method=getReconductTestListScreen';

	var href = anchor.href;
	for (var i=0; i<testIds.length; i++) {
		href = href + "&prescId=" + testIds[i];
	}
	anchor.href = href;
	return true;
}

function changeURL(anchor, params, id, toolbar) {
	var reportId = null;
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'reportId')
			reportId = paramvalue;
	}

	if ( reportId != '' && reportId != 0 ) {
		// selecting a report for editing, nothing to do.
		return true;
	}

	// selecting a "no report" row, need to add all the checked test Ids to the URL
	// as parameters.
	var row = document.getElementById('toolbarRow' + id);
	var checkBoxes = getElementsByName(row, 'forEdit');
	var testIds = new Array();
	if (checkBoxes != null) {
		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].checked)
				testIds.push(checkBoxes[i].value);
		}
	}

	if (testIds.length == 0) {
		showMessage("js.laboratory.radiology.reportlist.selecttest.edit");
		return false;
	}

	var href = anchor.href;
	for (var i=0; i<testIds.length; i++) {
		href = href + "&prescId=" + testIds[i];
	}
	anchor.href = href;
	return true;
}


function SignOff(printNeeded) {
	var checkBox = document.getElementsByName('signOff');
	var anyChecked = false;
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled && checkBox[i].checked) {
			anyChecked = true;
			break;
		}
	}
	if (!anyChecked) {
		showMessage ("js.diagnostics.diagdashboards.selectoneormoretestsforsignoff");
		return false;
	}

	if ( !checkTestStatusOfReport() ) {
		showMessage("js.diagnostics.diagdashboards.reportshaveteststhatarenotcompleted");
		return false;
	}

	document.signOffForm.submit();

	if(printNeeded =='Y')
		PrintAll(printNeeded);
}

function checkTestStatusOfReport() {
	var checkBox = document.getElementsByName('signOff');
	var reportWithIncompleteTests = document.getElementsByName("reportWithIncompleteTests");
	var valid = true;

	for (var i=0; i<checkBox.length; i++) {
		if ( !checkBox[i].disabled && checkBox[i].checked && reportWithIncompleteTests[i].value == 'N' ) {
			valid = false;
			break;
		}
	}

	return valid;
}

function PrintAll(printNeeded) {
	var checkBox = document.getElementsByName('signOff');
	var anyChecked = false;
	var reportParams = "";
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled && checkBox[i].checked) {
			anyChecked = true;
			reportParams += "&reportId="+checkBox[i].value;
		}
	}
	if (!anyChecked) {
		showMessage ("js.diagnostics.diagdashboards.selectoneormorereportsforprint");
		return false;
	}
	if(optimizedLabReportPrint == 'Y') {
		window.open(cpath + "/pages/DiagnosticModule/DiagReportPrint.do?_method=printOptimizedDiagReport&using=reportId" + reportParams +"&category="+category+"&signedOffandPrint="+printNeeded);
	} else {
		window.open(cpath + "/pages/DiagnosticModule/DiagReportPrint.do?_method=printSelectedReports&using=reportId" + reportParams +"&category="+category+"&signedOffandPrint="+printNeeded);
	}
}

function disableOther(){
	var form = document.diagcenterform;
	var disableInhouse = form.showOnlyouthouseTests.checked;
	form.showOnlyInhouseTests.disabled = disableInhouse;
}

function autoCompleteTest() {
	dataSource = new YAHOO.util.LocalDataSource(allTestNames)
	dataSource.responseSchema = {fields : ["TEST"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('test_name', 'test_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function autoCompleteHistoCytoShortImpression() {
	dataSource = new YAHOO.util.LocalDataSource(HistoCytoNames)
	dataSource.responseSchema = {fields : ["SHORT_IMPRESSION"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('short_impression', 'shot_impression_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function autoCompleteInHouse() {
	dataSource = new YAHOO.util.LocalDataSource(inHouses,{ queryMatchContains : true })
	dataSource.responseSchema = {fields : ["hospital_name"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('ih_name', 'inhouse_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
	oAutoComp1.forceSelection = true;
}

function autoCompleteOutHouse() {
	dataSource = new YAHOO.util.LocalDataSource(outHouses,{ queryMatchContains : true })
	dataSource.responseSchema = {fields : ["OUTSOURCE_NAME"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('oh_name', 'outhouse_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
	oAutoComp1.forceSelection = true;
}


function validateOrderId() {
	orderId = document.getElementById('common_order_id');
	if (!validateInteger(orderId, "Order Number should be an Integer.")) {
		orderId.value = "";
		orderId.focus();
		return false;
	}
	return true;
}

function setConductingDoc() {
	var checkBox = document.getElementsByName('forEdit');
	var commonConductingDoctor =
		document.getElementById('commonConductingDoctor').options[document.getElementById('commonConductingDoctor').selectedIndex].value;
	var anyChecked = false;
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled && checkBox[i].checked) {
			anyChecked = true;
			break;
		}
	}

	if (!anyChecked) {
		showMessage ("js.diagnostics.diagdashboards.selectoneormoretestsforconductingdoctor");
		return false;
	}
	document.signOffForm.action = '?_method=setConductingDoctorForSelectedTests';
	document.signOffForm.submit();

}
