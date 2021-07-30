<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
	<title>Claim ${claim.map.claim_id} Attachment</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js" />
	<insta:link type="script" file="billing/claimreconciliation.js"/>

	<script>
		function showAttachment(){
			window.open("${cpath}/billing/claimReconciliation.do?_method=showAttachment&claim_id=${claim.map.claim_id}&cache=false");
		}
	
		function deleteAttachment(){
			if (!confirm("Do you want to delete the attachment"))
			return false;
			window.location.href ="${cpath}/billing/claimReconciliation.do?_method=deleteAttachment&claim_id=${claim.map.claim_id}";
		}

		function fileValidate() {
			if (document.getElementById("attachment").value == '') {
				alert("Browse to add the file.");
				return false;
			}
			return true;
		}
		var claimStatus		= '${claim.map.status}';
		var claimBatchStatus = '${claim.map.claim_batch_status}';
		var actualClaimStatus = '${claim.map.claim_status}';
	</script>
</head>

<body onload="" class="yui-skin-sam">

<form name="claimForm" action="claimReconciliation.do">
	<input type="hidden" name="_method" value="addOrEditAttachment">
	<table width="100%">
		<tr>
			<td width="100%"><h1>Add/Edit Attachment</h1></td>
			<td>Claim&nbsp;No:&nbsp;</td>
			<td><input type="text" name="claim_id" id="claim_id" style="width: 80px"></td>
			<td><input type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<div><insta:feedback-panel/></div>
<insta:patientdetails visitid="${claim.map.patient_id}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Other Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Emirates Id:</td>
			<td class="forminfo">${claim.map.emirates_id_number}</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Claim Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Claim Id:</td>
			<td class="forminfo">${claim.map.claim_id}</td>
			<td class="formlabel">Submission Id:</td>
			<td class="forminfo">${claim.map.last_submission_batch_id}</td>
			<td class="formlabel">Payer Id:</td>
			<td class="forminfo">${claim.map.payer_id}</td>
		</tr>
		<tr>
			<td class="formlabel">Gross:</td>
			<td class="forminfo">${claim.map.gross}</td>
			<td class="formlabel">Patient Share:</td>
			<td class="forminfo">${claim.map.patient_share}</td>
			<td class="formlabel">Net:</td>
			<td class="forminfo">${claim.map.net}</td>
		</tr>
		<tr>
			<td class="formlabel">Claim Status:</td>
			<td class="forminfo">${claim.map.claim_status}</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
	</table>
</fieldset>
<br/>

<form method="POST" enctype="multipart/form-data" action="claimReconciliation.do?_method=saveAttachment&claim_id=${claim.map.claim_id}" name="mainform">

<table>
	<tr>
		<td>Attachment :</td>
		<td>
			<c:if test="${!(fileSize > 0)}">
			<c:set var="btndisabled" value="disabled"/>
			</c:if>
			<input type="file" name="attachment" id="attachment" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
			<button type="button" name="viewAttachment" accesskey="V" value="" ${btndisabled} onclick="showAttachment();"><b><u>V</u></b>iew </button>
			<button type="button" name="delAttachment" accesskey="D" value="" ${btndisabled} onclick="deleteAttachment();"><b><u>D</u></b>elete</button>
			<button type="submit" name="save" accesskey="S" onclick="return fileValidate();"><b><u>S</u></b>ave</button>
		</td>
	</tr>
</table>

<table class="screenActions">
	<tr>
		<td>
			<a href="./claimReconciliation.do?_method=getClaimBillsActivities&claim_id=${claim.map.claim_id}&patient_id=${claim.map.patient_id}">Claim ${claim.map.claim_id}</a>
			<c:if test="${not empty referer}">
				<label>|</label>
				<a href='<c:out value="${referer}"/>'>Back</a>
			</c:if>
		</td>
	 </tr>
</table>

</form>
</body>
</html>