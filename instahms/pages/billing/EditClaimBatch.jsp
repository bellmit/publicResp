<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="batchResubStatusDisplay" class="java.util.HashMap"/>
<c:set target="${batchResubStatusDisplay}" property="Y" value="Yes"/>
<c:set target="${batchResubStatusDisplay}" property="N" value="No"/>

<jsp:useBean id="batchStatusDisplay" class="java.util.HashMap"/>
<c:set target="${batchStatusDisplay}" property="O" value="Open"/>
<c:set target="${batchStatusDisplay}" property="S" value="Sent"/>
<c:set target="${batchStatusDisplay}" property="X" value="Rejected"/>

<jsp:useBean id="patientTypeDisplay" class="java.util.HashMap"/>
<c:set target="${patientTypeDisplay}" property="*" value="(All)"/>
<c:set target="${patientTypeDisplay}" property="o" value="OP"/>
<c:set target="${patientTypeDisplay}" property="i" value="IP"/>

<html>
<head>
	<title>Add/Edit Batch Ref.</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
<script>
function doSave() {
	document.editBatchForm.buttonAction.value = 'save';
	document.getElementById("saveButton").disabled = true;
	document.editBatchForm.submit();
	return true;
}
</script>
</head>

<body>

<form name="batchForm" action="claimSubmissionsList.do">
	<input type="hidden" name="_method" value="getAddorEditBatchRefScreen">
	<table width="100%">
		<tr>
			<td width="100%"><h1>Add/Edit Submission Batch Reference No.</h1></td>
			<td>Batch&nbsp;ID:&nbsp;</td>
			<td><input type="text" name="submission_batch_id" id="submission_batch_id" style="width: 80px"></td>
			<td><input type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel />

<form name="editBatchForm">
	<input type="hidden" name="submission_batch_id" id="submission_batch_id" value="${batch.map.submission_batch_id}" />
	<input type="hidden" name="buttonAction" value="save">
	<input type="hidden" name="_method" value="addoreditBatchRef">

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Submission Batch Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Batch Id:</td>
			<td class="forminfo">${batch.map.submission_batch_id}</td>
			<td class="formlabel">Status:</td>
			<td class="forminfo">${batchStatusDisplay[batch.map.status]}</td>
			<td class="formlabel">Resubmission:</td>
			<td class="forminfo">${batchResubStatusDisplay[batch.map.is_resubmission]}</td>
		</tr>
		<tr>
			<td class="formlabel">Created Date:</td>
			<td class="forminfo">
				<fmt:formatDate var="createddate" pattern="dd-MM-yyyy" value="${batch.map.created_date}"/>
				${createddate}
			</td>
			<td class="formlabel">Submit Date:</td>
			<td class="forminfo">
				<fmt:formatDate var="submissiondate" pattern="dd-MM-yyyy" value="${batch.map.submission_date}"/>
				${submissiondate}
			</td>
			<td class="formlabel">File Name:</td>
			<td class="forminfo"><div title="${batch.map.file_name}">${batch.map.file_name}</div></td>
		</tr>
		<tr>
			<td class="formlabel">Account Group:</td>
			<td class="forminfo">${batch.map.account_group_name}</td>
			<td class="formlabel">Service RegNo.:</td>
			<td class="forminfo">
				${(empty batch.map.account_group_service_reg_no || batch.map.account_group_service_reg_no == '') ? batch.map.hospital_center_service_reg_no : batch.map.account_group_service_reg_no}</td>
			<td class="formlabel">Patient Type:</td>
			<td class="forminfo">${patientTypeDisplay[batch.map.patient_type]}</td>
		</tr>
		<tr>
			<td class="formlabel">TPA/Sponsor:</td>
			<td class="forminfo">${empty batch.map.tpa_name ? '(All)' : batch.map.tpa_name}</td>
			<td class="formlabel">Insurance Co.:</td>
			<td class="forminfo">${empty batch.map.insurance_co_name ? '(All)' : batch.map.insurance_co_name}</td>
		</tr>
		<tr>
			<td class="formlabel">Plan Name:</td>
			<td class="forminfo">
				<div title="${empty batch.map.plan_name ? '(All)' : batch.map.plan_name}">
				${empty batch.map.plan_name ? '(All)' : batch.map.plan_name}
				</div>
			</td>
			<td class="formlabel">Net./Plan Type:</td>
			<td class="forminfo">
				<div title="${empty batch.map.category_name ? '(All)' : batch.map.category_name}">
					${empty batch.map.category_name ? '(All)' : batch.map.category_name}
				</div>
			</td>
		</tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder">
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Reference No.:</td>
			<td class="forminfo" colspan="3" title="${batch.map.reference_number}">
				<input type="text" style="width: 200px" name="reference_number" id="reference_number"
						value="${batch.map.reference_number}" maxlength="100"/>
			</td>
			<td class="formlabel"></td>
			<td class="forminfo"></td>
		</tr>
	</table>
</fieldset>

<table>
<tr>
	<td>
		<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u>S</u></b>ave</button>
	</td>
	<td>
		<insta:screenlink screenId="insurance_claim_submission" addPipe="true" label="Claim Submissions"
			extraParam="?_method=list&sortOrder=created_date&sortReverse=true&status=O"
			title="Claim Submissions List."/>
	</td>
</tr>
</table>

</form>
</body>
</html>