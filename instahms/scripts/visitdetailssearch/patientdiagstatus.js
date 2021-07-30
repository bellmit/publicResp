
function init() {
	initMrNoAutoComplete(cpath);
}

function PrintAll() {
	var checkBox = document.getElementsByName('reportId');
	var anyChecked = false;
	var reportParams = "";
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled && checkBox[i].checked) {
			anyChecked = true;
			reportParams += "&reportId="+checkBox[i].value;
		}
	}
	if (!anyChecked) {
		showMessage ("js.patient.diag.status.Selectoneormorereportsforprint");
		return false;
	}
	if(optimizedLabReportPrint == 'Y') {
		window.open(cpath + "/pages/DiagnosticModule/DiagReportPrint.do?_method=printOptimizedDiagReport&using=reportId" + reportParams);
	} else {
		window.open(cpath + "/pages/DiagnosticModule/DiagReportPrint.do?_method=printDiagStatusReports&using=reportId" + reportParams);
	}
}

function doSearch() {
	var mrNo = document.getElementById("mrno").value;
	if(empty(mrNo)) {
		showMessage ("js.patient.diag.status.entermrnotosearch");
		document.getElementById("mrno").focus();
		return false;
	}	
	return true;
}

function getExternalReport(visitId){
	document.patientDiagStatusForm._method.value = "getexternalreport";
	document.patientDiagStatusForm._external_visit_id.value = visitId;
	document.patientDiagStatusForm.submit();
	return true;
	
}