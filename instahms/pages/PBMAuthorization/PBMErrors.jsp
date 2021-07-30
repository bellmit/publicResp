<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="pbmStatusDisplay" class="java.util.HashMap"/>
<c:set target="${pbmStatusDisplay}" property="O" value="Open"/>
<c:set target="${pbmStatusDisplay}" property="S" value="Sent"/>
<c:set target="${pbmStatusDisplay}" property="D" value="Denied"/>
<c:set target="${pbmStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${pbmStatusDisplay}" property="C" value="Closed"/>

<html>
<head>
	<title>PBM Service Error - Insta HMS</title>
	<script type="text/javascript">
		function getPBMErrorReport() {
			document.errorform.submit();
			return true;
		}
	</script>
</head>
<body>

<div class="pageHeader">PBM Service Errors</div>
<insta:feedback-panel/>
<insta:patientdetails visitid="${not empty pbmPrescBean ? pbmPrescBean.map.patient_id : param.patient_id}"/>
<form action="./PBMRequests.do" method="POST" name="errorform">

<input type="hidden" name="pbm_presc_id" value="${pbmPrescBean.map.pbm_presc_id == 0 ? '' : pbmPrescBean.map.pbm_presc_id}" />
<input type="hidden" name="_method" value="getPBMErrorReport"/>
<textarea style="display:none" rows="20" cols="20" name="errorReport">${errorReport}</textarea>
<input type="hidden" name="fileName" value="${fileName}"/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">PBM Presc. Details</legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">PBM Presc. Id:</td>
			<td class="forminfo">${pbmPrescBean.map.pbm_presc_id == 0 ? '' : pbmPrescBean.map.pbm_presc_id}</td>
			<td class="formlabel">Status:</td>
			<td class="forminfo">${pbmStatusDisplay[pbmPrescBean.map.pbm_presc_status]}</td>
			<td class="formlabel">Drug Count:</td>
			<td class="forminfo">${pbmPrescBean.map.drug_count}</td>
		</tr>
		<tr>
			<td class="formlabel">PBM Request Id:</td>
			<td class="forminfo">${pbmPrescBean.map.pbm_request_id}</td>
			<td class="formlabel">Resubmission Request:</td>
			<td class="forminfo">${(not empty pbmPrescBean && not empty pbmPrescBean.map.pbm_request_id) ? ( pbmPrescBean.map.is_resubmit == 'Y' ? 'Yes' : 'No') : '' }</td>
			<td class="formlabel">Presc. Store:</td>
			<td class="forminfo">${pbmPrescBean.map.pbm_store_name}</td>
		</tr>
	</table>
</fieldset>

<br/>

<table class="screenActions">
	<tr>
		<td>
			<button name="errorbtn" ${not empty errorReport ? '' : 'disabled'}
	 			onclick="return getPBMErrorReport();">Download</button>
	 	</td>
	 	<td>
			<insta:screenlink target="#" screenId="pbm_presc" addPipe="true" label="PBM Prescription"
				extraParam="?_method=getPBMPrescription&pbm_presc_id=${pbmPrescBean.map.pbm_presc_id}"
				title="PBM Requests"/>
		</td>
	 	<td>
			<insta:screenlink screenId="pbm_requests" addPipe="true" label="PBM Requests"
				extraParam="?_method=getRequests&pbm_finalized=Y&pbm_presc_status=O&pbm_presc_status=R"
				title="PBM Requests"/>
		</td>
	</tr>
</table>
</form>
</body>
</html>
