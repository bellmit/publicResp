<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Test Documents - Insta HMS</title>
<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<inta:link type="js" file="ajax.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<style type="text/css">
		table.search td { white-space: nowrap }
	</style>
	<script>
		var cpath = '${cpath}';
		var roleId = '${roleId}';
		var loggedInUser = '${ifn:cleanJavaScript(userid)}';
		var category = '${ifn:cleanJavaScript(category)}';
		function setTemplateParams(radio, templateId, format) {
			document.operationDocForm.template_id.value = templateId;
			document.operationDocForm.format.value = format;
		}
		function validate() {
			var flag = false;
			var selectTemplate = document.getElementsByName("selectTemplate");
			for (var i=0; i<selectTemplate.length; i++) {
				if (selectTemplate[i].checked) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				alert("Please select the template");
				return false;
			}
			document.operationDocForm._method.value = 'add';
			document.operationDocForm.submit();
			return true;
		}
		function signOffSelected(e, form) {
			var operation_status = document.getElementById('operation_status').value;
			if (operation_status != 'C') {
				alert("Operation conduction is not complete, you can't sign-off the document");
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
			var signOffEl = document.getElementsByName("signOffList");
			for (var i=0; i< signOffEl.length; i++) {
				if (!signOffEl[i].disabled && signOffEl[i].checked) {
					form.action = cpath + '/otservices/OperationDocumentsList.do';
					form._method.value = "signOffDocuments";
					form.submit();
					return true;
				}
			}
			alert("Select at least one document for signoff");
			YAHOO.util.Event.stopEvent(e);
			return false;
		}
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init('Operation');ajaxForPrintUrls();" >
	<c:set var="operationdoclist" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty operationdoclist}"/>
	<c:set var="hasSignOffRights" value="${(roleId le 2) || actionRightsMap['sign_off_lab_reports'] eq 'A'}"/>

	<div class="pageHeader">Patient Operation Documents</div>
	<c:choose>
		<c:when test="${not empty operationdetails.patient_id}">
			<insta:patientdetails  visitid="${operationdetails.patient_id}"  showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${operationdetails.mr_no}" showClinicalInfo="true"/>
		</c:otherwise>
	</c:choose>
	<fieldset class="fieldSetBorder" style="margin-bottom: 5px;">
		<legend class="fieldSetLabel">Other Details</legend>
		<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td class="formlabel">Operation: </td>
				<td class="forminfo">
					<div title="${operationdetails.operation_name}">${operationdetails.operation_name}</div>
				</td>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
			</tr>
		</table>
	</fieldset>
	<form action="${cpath}/otservices/AddrEditOperationDocuments.do" method="GET" name="operationDocForm" autocomplete="off">
	<input type="hidden" name="_method" value=""/>
	<input type="hidden" name="mr_no" value="${operationdetails.mr_no}"/>
	<input type="hidden" name="patient_id" value="${operationdetails.patient_id}"/>
	<input type="hidden" name="prescription_id" value="${ifn:cleanHtmlAttribute(param.prescription_id)}"/>
	<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribed_id)}"/>
	<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(param.operation_details_id)}"/>
	<input type="hidden" name="visitId" value="${ifn:cleanHtmlAttribute(param.visitId)}"/>
	<input type="hidden" name="template_id" value=""/>
	<input type="hidden" name="format" value=""/>
	<input type="hidden" name="operation_status" id="operation_status" value="${operationdetails.conducted_status}"/>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" align="center" width="100%" id="resultTable" cellspacing="0" cellpadding="0">
			<tr onmouseover="hideToolBar('');">
				<th>Delete</th>
				<c:if test="${hasSignOffRights}">
					<th>Sign Off</th>
				</c:if>
				<th>Visit No</th>
				<th>Document Name</th>
				<th>Template</th>
				<th>Date</th>
				<th>User</th>
			</tr>

			<c:forEach var="operationdoc" items="${operationdoclist}" varStatus="st">

				<c:set var="printDoc" value="${operationdoc.map.doc_format != 'doc_link'}"/>
				<c:set var="disableEdit" value="${operationdoc.map.signed_off}"/>
				<tr  class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${operationdetails.mr_no}', doc_id: '${operationdoc.map.doc_id}', template_id: '${operationdoc.map.template_id}',
							format: '${operationdoc.map.doc_format}', patient_id: '${operationdetails.patient_id}',
							printerId: '${printpreferences.map.printer_id}', access_rights: '${operationdoc.map.access_rights}',
							username: '${operationdoc.map.username}', prescription_id: '${operationdoc.map.prescription_id}',
							operation_details_id:'${ifn:cleanJavaScript(param.operation_details_id)}',visitId:'${ifn:cleanJavaScript(param.visitId)}',prescribed_id:'${ifn:cleanJavaScript(param.prescribed_id)}'},
							[${printDoc}, !${disableEdit}]);"
						onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
					<td><input type="checkbox" name="deleteDocument" id="deleteDocument" ${operationdoc.map.signed_off ? 'disabled' : ''} value="${operationdoc.map.doc_id},${operationdoc.map.doc_format}"></td>
					<c:if test="${hasSignOffRights}">
						<td><input type="checkbox" name="signOffList" id="signOffList" value="${operationdoc.map.doc_id}" ${operationdoc.map.signed_off ? 'disabled checked' : ''}/></td>
					</c:if>
					<td>${operationdetails.patient_id}</td>
					<td><insta:truncLabel value="${operationdoc.map.doc_name}" length="45"/></td>
					<td><font class="${operationdoc.map.doc_format}">
							<insta:truncLabel value="${operationdoc.map.template_name}" length="45"/>
						</font>
					</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy" value="${operationdoc.map.doc_date}"/></td>
					<td><insta:truncLabel value="${operationdoc.map.username}" length="20"/></td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults message="No Documents Found" hasResults="${results}"/>
	</div>
	<div class="screenActions" style="float: left; display: ${results?'block':'none'}">
		<button type="button"  style="float: left" name="deleteDocuments" accesskey="D" onclick="return deleteSelected(event, document.operationDocForm);">
			<b><u>D</u></b>elete
		</button>
		<button type="button" style="float: left; margin-left: 5px; display: ${hasSignOffRights ? 'block' : 'none'}" accessKey="S" name="signOffDocuments"
			onclick="return signOffSelected(event, document.operationDocForm);">
			<b><u>S</u></b>ign Off
		</button>
	</div>
	<div style="clear: both"></div>
	<h2 style="margin: 10px 0px 10px 0px">Select a Template: </h2>
	<div class="resultList">
		<table class="dataTable" cellspacing="0" cellpadding="0" width="100%">
			<tr>
				<th width="50px">Select</th>
				<th>Template Name</th>
				<th>Format</th>
			</tr>
			<c:forEach var="template" items="${templatesList}" varStatus="status">
				<c:set var="templateFormat" value=""/>
				<c:set var="templateColor" value=""/>

				<c:choose>
					<c:when test="${template.map.format == 'doc_hvf_templates'}">
						<c:set var="templateFormat" value="HVF Template"/>
						<c:set var="templateColor" value="hvf"/>
					</c:when>
					<c:when test="${template.map.format == 'doc_rich_templates'}">
						<c:set var="templateFormat" value="Rich Text Template"/>
						<c:set var="templateColor" value="richtext"/>
					</c:when>
					<c:when test="${template.map.format == 'doc_pdf_form_templates'}">
						<c:set var="templateFormat" value="PDF Form Template"/>
						<c:set var="templateColor" value="pdfform"/>
					</c:when>
					<c:otherwise >
						<c:set var="templateFormat" value="RTF Template"/>
						<c:set var="templateColor" value="rtf"/>
					</c:otherwise>
				</c:choose>
				<tr class="${status.first ? 'firstRow' : ''}">
					<td>
						<input type="radio" name="selectTemplate" onclick="setTemplateParams(this, ${template.map.template_id}, '${template.map.format}')"/>
					</td>
					<td>${template.map.template_name}</td>
					<td><font class="${templateColor}">${templateFormat}</font></td>
				</tr>
			</c:forEach>

		</table>
	</div>

	<div class="clrboth"></div>

	<div class="screenActions" >
		<button type="button" name="add" accesskey="A" onclick="return validate()">
			<label><u><b>A</b></u>dd</label>
		</button>
		<c:if test="${preferences.modulesActivatedMap['mod_advanced_ot'] != 'Y'}">
			<insta:screenlink screenId="conduct_operation"
				addPipe="true" label="Conduct Operation" extraParam="?_method=getOperationsConductionScreen
				&prescription_id=${param.prescription_id}&visitId=${operationdetails.patient_id}"/>

			<insta:screenlink screenId="${'operations_pending_list'}"
				addPipe="true" label="Pending Operations" extraParam="?_method=pendingList&sortOrder=mr_no&patient_id=${operationdetails.patient_id}"/>

			<insta:screenlink screenId="${'operations_conducted_list'}"
				addPipe="true" label="Conducted Operations" extraParam="?_method=conductedList&sortOrder=mr_no&sortReverse=true&patient_id=${operationdetails.patient_id}"/>
		</c:if>
		<c:if test="${preferences.modulesActivatedMap['mod_advanced_ot'] == 'Y'}">
			<insta:screenlink screenId="get_ot_management_screen" extraParam="?_method=getOtManagementScreen&prescription_id=${param.prescribed_id}&visit_id=${param.visitId}&operation_details_id=${param.operation_details_id}"
				label="OT Management" addPipe="true"/>
		</c:if>
	</div>

</form>
</body>
</html>
