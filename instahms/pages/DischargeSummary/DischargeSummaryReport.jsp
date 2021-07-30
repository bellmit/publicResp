<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<script language="javascript" type="text/javascript">


var reportUrl = "dischargesummaryPrint.do?_method=print";
	reportUrl += "&patient_id=${ifn:cleanJavaScript(patient_id)}"
	reportUrl += "&docid=${ifn:cleanJavaScript(docid)}";
	reportUrl += "&printerId=${ifn:cleanJavaScript(printerId)}"
  window.open(reportUrl);

	var myUrl = "discharge.do?_method=show";
	myUrl += "&msg=${ifn:cleanJavaScript(success)}";
	myUrl += "&templateType=${templateType}";
	myUrl += "&patient_id=${ifn:cleanJavaScript(patient_id)}";
	myUrl += "&docid=${ifn:cleanJavaScript(docid)}";
	myUrl += "&format=F";
	myUrl += "&showPrinter=${ifn:cleanJavaScript(printerId)}";
	window.location.href=myUrl;

</script>
