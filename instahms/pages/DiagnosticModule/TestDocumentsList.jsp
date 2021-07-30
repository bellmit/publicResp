<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="laboratory.patienttestdocuments.addshow.title"/></title>
<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<inta:link type="js" file="ajax.js"/>
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="sample_assertion" value='<%=GenericPreferencesDAO.getAllPrefs().get("sample_assertion")%>'/>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>

	<style type="text/css">
		table.search td { white-space: nowrap }
	</style>
	<script>
		var cpath = '${cpath}';
		var roleId = '${roleId}';
		var loggedInUser = '${ifn:cleanJavaScript(userid)}';
		var category = '${ifn:cleanJavaScript(category)}';
		function setTemplateParams(radio, templateId, format) {
			document.testDocForm.template_id.value = templateId;
			document.testDocForm.format.value = format;
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
			document.testDocForm._method.value = 'add';
			document.testDocForm.submit();
			return true;
		}
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<c:set var="pendingTests">
 <insta:ltext key="laboratory.pendingtests.list.pendingtests"/>
</c:set>
<body onload="init('Test');ajaxForPrintUrls();" >
	<c:set var="testdoclist" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty testdoclist}"/>
	<div class="pageHeader"><insta:ltext key="laboratory.patienttestdocuments.addshow.patienttestdocuments"/></div>
	<c:choose>
		<c:when test="${isIncomingPatient}">
			<insta:incomingpatientdetails incomingVisitId="${testdetails.pat_id}" />
		</c:when>
		<c:when test="${not empty testdetails.pat_id}">
			<insta:patientdetails  visitid="${testdetails.pat_id}" showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${testdetails.mr_no}" showClinicalInfo="true"/>
		</c:otherwise>
	</c:choose>
	<fieldset class="fieldSetBorder" style="margin-bottom: 5px;">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.patienttestdocuments.addshow.otherdetails"/></legend>
		<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.patienttestdocuments.addshow.test"/>: </td>
				<td class="forminfo">
					<div title="${testdetails.test_name}">${testdetails.test_name}</div>
				</td>
				<td class="formlabel"></td>
				<td></td>
				<td class="formlabel"></td>
				<td></td>
			</tr>
		</table>
	</fieldset>

	<form action="${cpath}/${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/AddrEditTestDocuments.do" method="GET" name="testDocForm">
	<input type="hidden" name="_method" value=""/>
	<input type="hidden" name="mr_no" value="${testdetails.mr_no}"/>
	<input type="hidden" name="patient_id" value="${testdetails.pat_id}"/>
	<input type="hidden" name="prescribed_id" value="${ifn:cleanHtmlAttribute(param.prescribed_id)}"/>
	<input type="hidden" name="template_id" value=""/>
	<input type="hidden" name="format" value=""/>
	<input type="hidden" name="isIncomingPatient" value="${isIncomingPatient}"/>



<c:if test="${!param.defaultScreen}">
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" align="center" width="100%" id="resultTable" cellspacing="0" cellpadding="0">
			<tr onmouseover="hideToolBar('');">
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.select"/></th>
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.visitno"/></th>
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.documentname"/></th>
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.template"/></th>
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.date"/></th>
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.user"/></th>
			</tr>

			<c:forEach var="testdoc" items="${testdoclist}" varStatus="st">


				<tr  class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${ifn:cleanJavaScript(param.mr_no)}', doc_id: '${testdoc.map.doc_id}', template_id: '${testdoc.map.template_id}',
							format: '${testdoc.map.doc_format}', patient_id: '${testdoc.map.pat_id}',
							printerId: '${printpreferences.map.printer_id}', access_rights: '${testdoc.map.access_rights}',
							username: '${testdoc.map.username}', prescribed_id: '${testdoc.map.prescribed_id}', isIncomingPatient: ${isIncomingPatient},
							docLocation: '${testdoc.map.doc_location}'},
							[true, true]);"
						onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
					<td><input type="checkbox" name="deleteDocument" id="deleteDocument" value="${testdoc.map.doc_id},${testdoc.map.doc_format}"></td>
					<td>${testdoc.map.pat_id}</td>
					<td><insta:truncLabel value="${testdoc.map.doc_name}" length="45"/></td>
					<td><font class="${testdoc.map.doc_format}">
							<insta:truncLabel value="${testdoc.map.template_name}" length="45"/>
						</font>
					</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy" value="${testdoc.map.doc_date}"/></td>
					<td><insta:truncLabel value="${testdoc.map.username}" length="20"/></td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults message="No Documents Found" hasResults="${results}"/>
	</div>
	<div class="screenActions" style="float: left; display: ${results?'block':'none'}">
		<button type="button"  name="deleteDocuments" accesskey="D" onclick="return deleteSelected(event, document.testDocForm);">
			<b><u><insta:ltext key="laboratory.patienttestdocuments.addshow.d"/></u></b><insta:ltext key="laboratory.patienttestdocuments.addshow.elete"/>
		</button>
	</div>
	<div style="clear: both"></div>
	<h2 style="margin: 10px 0px 10px 0px"><insta:ltext key="laboratory.patienttestdocuments.addshow.selectatemplate"/>: </h2>
	<div class="resultList">
		<table class="dataTable" cellspacing="0" cellpadding="0" width="100%">
			<tr>
				<th width="50px"><insta:ltext key="laboratory.patienttestdocuments.addshow.select"/></th>
				<th><insta:ltext key="laboratory.patienttestdocuments.addshow.templatename"/></th>
			</tr>
			<c:if test="${uploadFile == true}">
				<tr>
					<td>
						<input type="radio" name="selectTemplate" onclick="setTemplateParams(this, '${template.map.template_id}', 'doc_fileupload')"/>
					</td>
					<td ><insta:ltext key="laboratory.patienttestdocuments.addshow.uploadfile"/></td>
				</tr>
			</c:if>
			<c:if test="${docLink == true}">
				<tr>
					<td>
						<input type="radio" name="selectTemplate" onclick="setTemplateParams(this, '${template.map.template_id}', 'doc_link')"/>
					</td>
					<td ><insta:ltext key="laboratory.patienttestdocuments.addshow.linktoexternaldocument"/></td>
				</tr>
			</c:if>
		</table>
	</div>
	<c:set var="sampleAssertion" value="${sample_assertion == 'Y'}" />
	<c:set var="blockAsserted" value="${(sampleAssertion == true && testdetails.sample_collection_status ne 'A') && testdetails.sample_status ne 'U' }"/>
	<c:set var="testingCenterTest" value="${(category eq 'DEP_LAB' && max_centers_inc_default > 1) ? (testdetails.hospital eq 'incoming' && testdetails.incoming_source_type eq 'C') : false}" />
	<c:set var="collectionCenterTest"
							value="${max_centers_inc_default > 1 && category eq 'DEP_LAB' && testdetails.house_status eq 'O' && testdetails.outsource_dest_type eq 'C'}"/>
	<c:set var="blockForInternalLab"
							value="${(collectionCenterTest || testingCenterTest) ? (collectionCenterTest ? false : centerId ne 0) : true}" />
	<c:set var="outHouseAssigned" value="${testdetails.house_status eq 'O' ? testdetails.is_outhouse_selected eq 'Y' : true}" />
	<c:set var="sampleTransferred" value="${(testdetails.sample_transfer_status == 'T' && testdetails.outsource_dest_type eq 'C')}"/>	
	<c:set var="isInternalLab" value="${category eq 'DEP_LAB' and testdetails.outsource_dest_type eq 'C'}"/>
	<div class="clrboth"></div>

	<div class="screenActions" >
		<c:url value="editresults.do" var="editResultsUrl">
			<c:param name="_method" value="getBatchConductionScreen"/>
			<c:param name="reportId" value="${empty param.reportId ? testdetails.report_id : param.reportId}"/>
			<c:param name="prescId" value="${param.prescribed_id}"/>
			<c:param name="visitid" value="${testdetails.pat_id}"/>
			<c:param name="category" value="${category}"/>
		</c:url>
		<button type="button" name="add" accesskey="A" onclick="return validate()">
			<label><u><b><insta:ltext key="laboratory.patienttestdocuments.addshow.a"/></b></u><insta:ltext key="laboratory.patienttestdocuments.addshow.dd"/></label>
		</button>
		<c:if test ="${testdetails.conducted != 'NRN' && testdetails.conducted != 'CRN' && !blockAsserted && blockForInternalLab && outHouseAssigned && !sampleTransferred
		&& !(isInternalLab ? test.outsource_dest ne centerId : false)}">
			<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_edit_results' : 'rad_edit_results'}"
				addPipe="true" label="Edit Test Results" extraParam="?_method=getBatchConductionScreen
				&reportId=${empty param.reportId ? testdetails.report_id : param.reportId}&prescId=${param.prescribed_id}
				&visitid=${testdetails.pat_id}
				&category=${category}"/></c:if>
		<c:if test ="${testdetails.conducted != 'NRN' && testdetails.conducted != 'CRN'}">
			<c:set var="reportlistLink" value="${category == 'DEP_LAB' ? 'Laboratory Reports List' : 'Radiology Reports List'}"/>
			<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_schedules_list' : 'rad_schedules_list'}"
			addPipe="true" label="${reportlistLink}" extraParam="?_method=getScheduleList&patient_id=${testdetails.pat_id}"/>
		</c:if>
		<insta:screenlink screenId="${category == 'DEP_LAB' ? 'lab_unfinished_tests' : 'rad_unfinished_tests'}"
			addPipe="true" label="${pendingTests}" extraParam="?_method=unfinishedTestsList&conducted=N&conducted=P&conducted=NRN&sortOrder=pres_date&patient_id=${testdetails.pat_id}"/>
	</div>
</c:if>



</form>
</body>
</html>
