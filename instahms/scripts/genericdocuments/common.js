function selectTemplate(){
	if(documentType == "mlc") {
		document.forms[0].action= contextPath +"/pages/registration/ManageVisits.do";
		document.forms[0].method.value = "searchVisits";
	} else if(documentType == 'reg') {
		document.forms[0].action= contextPath +"/pages/RegistrationDocuments.do";
		document.forms[0]._method.value = "addPatientDocument";
	} else if(documentType == 'dietary') {
		document.forms[0].action = contextPath + "/pages/ipservices/dietPrescribe.do";
		document.forms[0]._method.value = "getPrescriptionScreen";

	}else if(documentType == 'insurnace') {
		document.forms[0].action= contextPath +"InsuranceGenericDocuments.do";
		document.forms[0]._method.value = "addPatientDocument";
	}else if(documentType == 'op_case_form_template') {
		document.forms[0].action= contextPath +"/outpatient/OpCaseFormAction.do";
		document.forms[0]._method.value = "show";
	}else if(documentType == 'tpapreauth') {
		funNullifyValues();
		document.forms[0].action= contextPath +"/Insurance/InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true";
	} else {
		document.forms[0].format.value = "All";
		document.forms[0]._method.value = "addPatientDocument";
	}
	document.forms[0].setAttribute("method", "GET");
	document.forms[0].submit();
}
function funNullifyValues(){
	if(document.forms[0].mr_no !=null)
		document.forms[0].mr_no.value="";
	if(document.forms[0].insurance_id !=null)
		document.forms[0].insurance_id.value="";
	if(document.forms[0].tpa_id !=null)
		document.forms[0].tpa_id.value="";
}
