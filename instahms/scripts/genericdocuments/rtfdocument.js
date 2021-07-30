
function getRtfDocument() {
	var url = actionUrl + "?_method=getRtfDocument" ;
	url += "&mr_no=" + mr_no ;
	url += "&template_id=" + template_id ;
	url += "&format=" + format;
	url += "&doc_id="+doc_id;
	url += "&insurance_id="+insurance_id;
	url += "&patient_id="+patient_id;
	url += "&consultation_id="+ consultation_id;
	url +="&prescription_id="+ prescription_id;
	window.open(url);
}
