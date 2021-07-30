<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<script language="javascript" type="text/javascript">


	var printUrl = "dischargesummaryPrint.do?_method=print";
	printUrl += "&patient_id=${ifn:cleanJavaScript(patientID)}";
	printUrl += "&docid=${ifn:cleanJavaScript(docid)}";
	printUrl += "&printerId=${ifn:cleanJavaScript(printerId)}";
  	window.open(printUrl);

	var myUrl = "discharge.do?_method=show";
	myUrl += "&msg=${ifn:cleanJavaScript(msg)}";
	myUrl += "&templateType=${templateType}";
	myUrl += "&patient_id=${ifn:cleanJavaScript(patientID)}";
	myUrl += "&docid=${ifn:cleanJavaScript(docid)}";
	myUrl += "&format=T";
	myUrl += "&showPrinter=${ifn:cleanJavaScript(printerId)}";
	window.location.href=myUrl;
</script>
