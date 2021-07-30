
function changeURL(anchor, params, id, toolbar) {
	var reportId = '';
	var visitId = '';
	var visitType = '';
	var prescribed_id = '';
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'reportId')
			reportId = paramvalue;
		if (paramname == 'visitid')
			visitId = paramvalue
		if (paramname == 'visit_type')
			visitType = paramvalue;
		if (paramname == 'prescribed_id')
			prescribed_id = paramvalue;
	}

	var form = document.resultsForm;
	var check = form.prescId;
	form.visitid.value = visitId
	form.visitType.value = visitType;
	var flag = false;
	var prescriptionArray = [];
	var index = 0;
  	if (parseInt(reportId) == 0) {
		form.reportId.value = '';
	 	form.prescId.value = prescribed_id;
	} else {
	   //when user tries to edit testList corresponding to Report.
		form.reportId.value = reportId;
		flag = true;
	}


	var href = anchor.href;
	href = href + "&prescId=" + prescribed_id;
	anchor.href = href;
	return true;

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
}

function appendTemplateName(obj,sampleNo,visitId,mrNo,sampleDate,sampleType){
	var href = cpath+"/pages/DiagnosticModule/DiagReportPrint.do?_method=generateSampleCollectionReport&visitid="+visitId+"&sampleNo='"+sampleNo+"'&template_name="+document.resultsForm.sampleBardCodeTemplate.value+"&sampleDates="+sampleDate+"&sampleTypes="+sampleType;;
	if(samplePrintType != "SL"){
		href = cpath+"/Laboratory/GenerateSamplesBarCodePrint.do?method=execute&mrno="+mrNo+"&sampleNo='"+sampleNo+"'&barcodeType=sample&template_name="+document.resultsForm.sampleBardCodeTemplate.value+"&sampleDates="+sampleDate+"&sampleTypes="+sampleType+"&visitId="+visitId;
	}
	obj.setAttribute('href',href);
	return true;
}

function setConductingDoc() {
	var checkBox = document.getElementsByName('completeCheck');
	
	var screenId = document.getElementsByName('screenId');
	var commonConductingDoctor =
		document.getElementById('commonConductingDoctor').options[document.getElementById('commonConductingDoctor').selectedIndex].value;
	var anyChecked = false;
	var condDocMandatory = document.getElementsByName('_conducting_doc_mandatory');
	var resultEntryApplicable = document.getElementsByName('_results_entry_applicable');
	var testNames = document.getElementsByName('_test_name');
	var testNamesList = "";
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled && checkBox[i].checked) {
			if (commonConductingDoctor == '' && resultEntryApplicable[i].value == 'No' && (condDocMandatory[i].value == 'O'
						|| condDocMandatory[i].value == 'C')) {
				testNamesList += "\n" + testNames[i].value;
			}

			anyChecked = true;
		}
	}

	if (!anyChecked) {
		showMessage ("js.diagnostics.diagdashboards.selectoneormoretestsforconductingdoctor");
		return false;
	}

	if (testNamesList != "") {
		var msg = getString("js.diagnostics.diagdashboards.conductingdoctormanadatory")+":"+ testNamesList;
		alert(msg);
		return false;
	}
	if(screenId!=null && screenId!=undefined && screenId[0].value!=null && screenId[0].value!=undefined && screenId[0].value=='lab_unfinished_tests_search')
	document.resultsForm.action = cpath +"/"+baseModule+"/unfinishedTestsSearch.do?_method=setConductingDoctorForSelectedTests";
	else
		document.resultsForm.action = cpath +"/"+baseModule+"/unfinishedTests.do?_method=setConductingDoctorForSelectedTests";
	document.resultsForm.submit();

}
