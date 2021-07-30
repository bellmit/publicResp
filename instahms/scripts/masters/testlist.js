function testAutoComplete() {
	YAHOO.example.testNamesArray = [];
	YAHOO.example.testNamesArray.length =testNames.length;

	for (var i=0;i<testNames.length;i++) {
		var item = testNames[i]
			YAHOO.example.testNamesArray[i] = item["test_name"];
	}

	YAHOO.example.ACJSArray = new function() {
		// Instantiate first JS Array DataSource
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.testNamesArray);
		var autoComp = new YAHOO.widget.AutoComplete('test_name','testContainer', datasource);
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = true;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 1;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = false;
		autoComp.forceSelection = false;
		autoComp.textboxFocusEvent.subscribe(function() {
				var sInputValue = YAHOO.util.Dom.get('test_name').value;
				if(sInputValue.length === 0) {
					var oSelf = this;
					setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
				}

		});
	}
}

function changeRatePlan(){
	document.searchform.submit();
}

function searchValidate() {
	return true;
}

function selectAllBedTypes(){
	var selected = document.updateform.allBedTypes.checked;
	var bedTypesLen = document.updateform.selectBedType.length;

	for (i=bedTypesLen-1;i>=0;i--) {
		document.updateform.selectBedType[i].selected = selected;
	}
}


function deselectAllBedTypes(){
	document.updateform.allBedTypes.checked = false;
}


function selectAllPageTests() {
	var checked = document.listform.allPageTests.checked;
	var length = document.listform.selectTest.length;

	if (length == undefined) {
		document.listform.selectTest.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectTest[i].checked = checked;
		}
	}
}

function onChangeAllTests() {
	var val = getRadioSelection(document.updateform.allTests);
	// if allTests = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPageTests.disabled = disabled;
	listform.allPageTests.checked = false;

	var length = listform.selectTest.length;

	if (length == undefined) {
		listform.selectTest.disabled = disabled;
		listform.selectTest.checked  = false;
	} else {
		for (var i=0;i<length;i++) {
			listform.selectTest[i].disabled = disabled;
			listform.selectTest[i].checked = false;
		}
	}
}


function doGroupUpdate() {

	var updateform = document.updateform;
	var listform = document.listform;
	updateform.orgId.value = document.searchform.org_id.value;

	var anyTests = false;
	var allTests = getRadioSelection(document.updateform.allTests);
	if (allTests == 'yes') {
		anyTests = true;
	} else {
		var div = document.getElementById("testListInnerHtml");
		while (div.hasChildNodes())
			div.removeChild(div.firstChild);

		var length = listform.selectTest.length;
		if (length == undefined) {
			if (listform.selectTest.checked ) {
				anyTests = true;
				div.appendChild(makeHidden("selectTest", "", listform.selectTest.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectTest[i].checked){
					anyTests = true;
					div.appendChild(makeHidden("selectTest", "", listform.selectTest[i].value));
				}
			}
		}
	}

	if (!anyTests) {
		alert('Select at least one test for updation');
		return;
	}

	var anyBedTypes = false;
	if (updateform.allBedTypes.checked) {
		anyBedTypes = true;
	} else {
		var bedTypeLength = updateform.selectBedType.length;

		for (var i=0; i<bedTypeLength ; i++) {
			if(updateform.selectBedType.options[i].selected){
				anyBedTypes = true;
				break;
			}
		}
	}

	if (!anyBedTypes) {
		alert('Bed Types are required');
		return ;
	}

	if (!updateOption()) {
		alert("Select any update option");
		updateform.updateTable[0].focus();
		return ;
	}

	if (updateform.amount.value=="") {
		alert("Value required for Amount");
		updateform.amount.focus();
		return ;
	}

	if(updateform.amtType.value == '%') {
		if(getAmount(updateform.amount.value) > 100){
			alert("Discount percent cannot be more than 100");
			updateform.amount.focus();
			return false;
		}
	}

	updateform.submit();
}

function doExport() {
	document.exportform.orgId.value = document.searchform.org_id.value;
	return true;
}

function doUpload() {
	document.uploadtestform.orgId.value = document.searchform.org_id.value;
	var fileType = document.uploadtestform.fileupload.value;
	var file = document.uploadtestform.uploadFile.value;
	if (file == null || file == '') {
		alert('Please browse the file to upload.');
		document.uploadtestform.uploadFile.focus();
		return false;
	}
	if (!file.endsWith(".csv")) {
		alert('Please upload a valid CSV file.');
		document.uploadtestform.uploadFile.focus();
		return false;
	}
	if (fileType == 'TESTTEMPLATE') {
		document.uploadtestform.action = cpath+"/master/diagnostics/importTemplates.htm";
		document.uploadtestform.method = 'POST';
		document.uploadtestform.submit();
	} else if (fileType == 'TESTRESULTS') {
		document.uploadtestform.action = cpath+"/master/diagnostics/importResultLabels.htm";
		document.uploadtestform.method = 'POST';
		document.uploadtestform.submit();
	} else if (fileType == 'TESTTAT') {
		document.uploadtestform.action = cpath+"/master/diagnostics/importTatDetails.htm";
		document.uploadtestform.method = 'POST';
		document.uploadtestform.submit();
	} else if (fileType == 'TEST') {
		document.uploadtestform.action = cpath+"/master/diagnostics/importTestDetails.htm";
		document.uploadtestform.method = 'POST';
		document.uploadtestform.submit();
	} else if (fileType == 'TESTCHARGE') {
		var orgId = document.searchform.org_id;
		var orgName = orgId.options[orgId.selectedIndex].text;
		var fileName = file.substring(0, file.length - 4);
		var files = fileName.split('_');
		var firstNameLength = files[0].length;
		var orgNameFile = fileName.substring(firstNameLength+1);
		if (orgName != orgNameFile) {
			var ok = confirm("The current rate plan "+orgName+" the upload file is having different rate plan. \n" +
				"Are you sure you want to upload? ");
		
			if (!ok){
			return false;
			}
		}
		document.uploadtestform.action = cpath+"/master/diagnostics/importTestCharges.htm";
		document.uploadtestform.method = 'POST';
		document.uploadtestform.submit();
	}
}

function retryJobSchedule(testId) {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/master/addeditdiagnostics/retrychargeschedule.json?entity=DIAGNOSTIC&entity_id="+testId;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			document.getElementById('entity_status_'+testId).innerHTML = 'Processing';
			document.getElementById('retry_job_'+testId).innerHTML = '';
			document.getElementById('error_status_'+testId).innerHTML = '';
			alert("Retry processing");
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function doDownload() {
	var fileType = document.getElementById('filedownload').value;
	if (fileType == 'TEST') {
		document.exporttestform.action = cpath+"/master/diagnostics/testDetailsExport.htm";
		document.exporttestform.submit();
	} else if (fileType == 'TESTTEMPLATE') {
		document.exporttestform.action = cpath+"/master/diagnostics/testTemplateExport.htm";
		document.exporttestform.submit();
	} else if (fileType == 'TESTRESULTS') {
		document.exporttestform.action = cpath+"/master/diagnostics/testResultsExport.htm";
		document.exporttestform.submit();
	} else if (fileType == 'TESTTAT') {
		document.exporttestform.action = cpath+"/master/diagnostics/testTatExport.htm";
		document.exporttestform.submit();
	} else if (fileType == 'TESTCHARGE') {
		document.exporttestform.action = cpath+"/master/diagnostics/testChargesExport.htm";
		document.exporttestform.submit();
	}
}
function updateOption() {
	for (var i=0; i<updateform.updateTable.length ; i++) {
		if(updateform.updateTable[i].checked){
			return true;
		}
	}
	return false;
}

var toolBar = {
		
	Test : {
		title : 'View/Edit',
		imageSrc : 'icons/Edit.png',
		href : '/master/addeditdiagnostics/show.htm?',
		onclick : null,
		description : 'View and/or Edit the contents of this Test'
	},
	Charges : {
		title : 'Edit Charges',
		imageSrc : 'icons/Edit.png',
		href : '/master/addeditdiagnostics/editcharge.htm?',
		onclick : null,
		description : 'View and/or Edit the contents of this Test Charges'
	},
	TestAuditLog : {
		title : 'Audit Log',
		imageSrc : 'icons/Edit.png',
		href : 'diagnosticTests/auditlog/AuditLogSearch.do?_method=getAuditLogDetails&al_table=diagnostics_audit_log_view',
		description: 'View changes made to this test'
	},
	EditResultRanges : {
		title : 'Add/Edit Result Ranges',
		imageSrc : 'icons/Edit.png',
		href : 'masters/resultranges.do?_method=list',
		onclick : null,
		description : 'Add/Edit Result Ranges of this Test'
	},
	EditTestTAT : {
		title : 'Add/Edit Test TAT',
		imageSrc : 'icons/Edit.png',
		href : '/master/testtatmaster.do?_method=getScreen',
		onclick : null,
		description : 'View and/or Edit TAT of this Test'
	}

}

function init() {
	createToolbar(toolBar);
}
