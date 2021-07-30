function patientimages(){
	var href = contextPath + "/pages/GenericDocuments/PatientGeneralImageAction.do?";
		href += "_method=getPatientImages&mr_no="+document.forms[0].mr_no.value;
	window.location.href = href;

}