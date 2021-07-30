<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="preauthStatusDisplay" class="java.util.HashMap"/>
<c:set target="${preauthStatusDisplay}" property="O" value="Open"/>
<c:set target="${preauthStatusDisplay}" property="S" value="Sent"/>
<c:set target="${preauthStatusDisplay}" property="D" value="Denied"/>
<c:set target="${preauthStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${preauthStatusDisplay}" property="C" value="Closed"/>
<c:set target="${preauthStatusDisplay}" property="X" value="Cancelled"/>

<html>
<head>
	<title>Prior Auth ${preauthPrescBean.map.preauth_presc_id} Prescription Attachment</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js" />

	<script>
		function showAttachment(){
			window.open("${cpath}/EAuthorization/EAuthPresc.do?_method=showAttachment&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}&cache=false");
		}

		function deleteAttachment(){
			if (!confirm("Do you want to delete the attachment"))
			return false;
			window.location.href ="${cpath}/EAuthorization/EAuthPresc.do?_method=deleteAttachment&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}";
		}

		function fileValidate() {
			if (document.getElementById("attachment").value == '') {
				alert("Browse to add the file.");
				return false;
			}
			return true;
		}
	</script>
</head>

<body onload="" class="yui-skin-sam">

<form name="preauthPrescFindForm" action="./EAuthPrescAttachFile.do">
	<input type="hidden" name="_method" value="addOrEditAttachment">
	<table width="100%">
		<tr>
			<td width="100%"><h1>Add/Edit Attachment</h1></td>
			<td>Prior Auth&nbsp;Presc.&nbsp;Id:&nbsp;</td>
			<td><input type="text" name="preauth_presc_id" id="preauth_presc_id" style="width: 80px"></td>
			<td><input type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel/>
<insta:patientdetails visitid="${preauthPrescBean.map.patient_id}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Other Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Emirates Id:</td>
			<td class="forminfo">${preauthPrescBean.map.emirates_id_number}</td>
			<td class="formlabel">Provider Id:</td>
			<td class="forminfo">${service_reg_no}</td>
			<td class="formlabel">Encounter Type:</td>
			<td class="forminfo" colspan="2">${preauthPrescBean.map.encounter_type_desc}</td>
		</tr>
	</table>
</fieldset>
<form method="POST" enctype="multipart/form-data"
	action="./EAuthPrescAttachFile.do?_method=saveAttachment&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}" name="mainform">

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Prior Auth Presc. Details</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Prior Auth Presc. Id:</td>
			<td class="forminfo">${preauthPrescBean.map.preauth_presc_id == 0 ? '' : preauthPrescBean.map.preauth_presc_id}</td>
			<td class="formlabel">Prior Auth Status:</td>
			<td class="forminfo">${preauthStatusDisplay[preauthPrescBean.map.preauth_status]} </td>
			<td class="formlabel">Prior Auth Request Id:</td>
			<td class="forminfo">${preauthPrescBean.map.preauth_request_id}</td>
		</tr>
		<tr>
			<td class="formlabel">Resubmission Request:</td>
			<td class="forminfo">${(not empty preauthPrescBean && not empty preauthPrescBean.map.preauth_request_id) ? ( preauthPrescBean.map.is_resubmit == 'Y' ? 'Yes' : 'No') : '' }</td>
		</tr>
	</table>
</fieldset>

<br/>
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
			<a href="./EAuthPresc.do?_method=getEAuthPrescription&preauth_presc_id=${preauthPrescBean.map.preauth_presc_id}&insurance_co_id=${preauthPrescBean.map.insurance_co_id}">Prior Auth Prescription ${preauthPrescBean.map.preauth_presc_id}</a>
		</td>
	 </tr>
</table>

</form>
</body>
</html>