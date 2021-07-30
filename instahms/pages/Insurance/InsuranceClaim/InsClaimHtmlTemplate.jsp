<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insurance Claim</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="tiny_mce/tiny_mce.js" />
<insta:link type="js" file="editor.js" />

<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<script>
		/* initialize the tinyMCE editor: todo: font and size to be customizable */
		initEditor("doc_content_html", '${cpath}', 'sans-serif', 12);

	function dashboard() {
			window.location.href = "${cpath}/Insurance/InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true";
	}
	function printClaim(){
		var mode = document.forms[0].mode.value;

		if(mode == "insert"){
			alert("Please save the details to Print");
			return false;
		}

		var insurance_id = document.forms[0].insurance_id.value;
		var printDefType=document.getElementById("printDefType").value;
		window.open("InsuranceClaim.do?_method=printHTML&insurance_id="+insurance_id+"&printDefType="+printDefType);
		return false;
	}
	function funRegenerate(){
		document.forms["mainform"].action="${cpath}/Insurance/InsuranceClaim.do?_method=regenerateHTML";
		document.forms["mainform"].submit();
	}
	function saveValues(){
		tinyMCE.triggerSave();
		document.forms["mainform"].submit();
	}

</script>
</head>
<body>
<div class="pageHeader">Claim Template</div>
<span align="center" class="error">${ifn:cleanHtml(error)}</span>
<insta:feedback-panel/>
<div>
	<c:choose>
		<c:when test="${not empty ClaimDetails.map.patient_id}">
			<insta:patientdetails  visitid="${ClaimDetails.map.patient_id}" />
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${ClaimDetails.map.mr_no}" />
		</c:otherwise>
	</c:choose>
</div>
<form name="mainform"  action="InsuranceClaim.do?_method=addOrEditHTMl" method="POST">
<input type="hidden" name="insurance_id" id="insurance_id" value="${ClaimDetails.map.insurance_id}">
<input type="hidden" name="mode" id="mode" value="${ifn:cleanHtmlAttribute(mode)}">

<div>
	<table class="formtable"><tr><td colspan="2">
		<tr>
			<td>
				<textarea id="doc_content_html" name="doc_content_html" style="width: 600; height: 450;">
					<c:out value="${ClaimDetails.map.doc_content_html}"/>
				</textarea>
			</td>
		</tr>
	</table>
</div>
<div class="screenActions">
	<input type="button" name="save" id="save" value="Save" onClick="return saveValues();"/> |
	<a href="javascript:void(0)" onclick="dashboard();">Case List</a> |
	<a href="javascript:void(0)" onclick="funRegenerate();">Regenerate</a>
</div>
<div style="float: right">
	<insta:selectdb name="printDefType" id="printDefType" table="printer_definition"
			valuecol="printer_id"  displaycol="printer_definition_name"
			value="${pref.map.printer_id}"/>
	<a href="javascript:void(0)" onclick="printClaim();">Print</a>
</div>
	</form>
</body>
</html>
