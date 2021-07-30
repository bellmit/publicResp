<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Patient Documents - Insta HMS</title>
<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<inta:link type="js" file="ajax.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<style type="text/css">
		table.search td { white-space: nowrap }
	</style>
	<script>
		var cpath = '${cpath}';
		var roleId = '${ifn:cleanJavaScript(roleId)}';
		var loggedInUser = '${ifn:cleanJavaScript(userid)}';
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="init('Generic');ajaxForPrintUrls();" >
	<c:set var="patientdoclist" value="${pagedList.dtoList}"/>
	<c:set var="results" value="${not empty patientdoclist}"/>
	<div class="pageHeader" style="float: left;">Patient General Documents</div>
	<c:url var="searchUrl" value="GenericDocumentsAction.do" />
	<insta:patientsearch searchType="mrNo" fieldName="mr_no" searchUrl="${searchUrl}" buttonLabel="Find"
		searchMethod="searchPatientGeneralDocuments"/>
	<insta:feedback-panel/>
	<c:choose>
		<c:when test="${not empty param.patient_id}">
			<insta:patientdetails  visitid="${param.patient_id}" showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<insta:patientgeneraldetails  mrno="${param.mr_no}" showClinicalInfo="true"/>
		</c:otherwise>
	</c:choose>

	<form action="GenericDocumentsAction.do" method="GET" name="generalDocForm">
	<input type="hidden" name="_method" value="searchPatientGeneralDocuments"/>
	<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
	<input type="hidden" name="_searchMethod" value="searchPatientGeneralDocuments"/>

<c:if test="${!param.defaultScreen}">
	<insta:search form="generalDocForm" optionsId="optionalFilter" closed="${results}" >
			<div class="sboField">
				<div class="sfLabel">Document Name:</div>
						<div class="sfField">
							<input type="text" name="doc_name" value="${ifn:cleanHtmlAttribute(param.doc_name)}" />
						</div>
			</div>
		<div id="optionalFilter" style="clear: both; display: ${results ? 'none' : 'block'}">
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Document Type:</div>
						<div class="sfField">
							<insta:selectdb name="doc_type_id" table="doc_type" valuecol="doc_type_id" displaycol="doc_type_name"
								dummyvalue="---Select---" values="${paramValues.doc_type_id}" orderby="doc_type_name"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Template Name:</div>
						<div class="sfField">
							<input type="text" name="template_name" value="${ifn:cleanHtmlAttribute(param.template_name)}" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="doc_from_date" valid="past" id="doc_from_date" value="${paramValues['doc_from_date'][0]}"/>
							<input type="hidden" name="doc_from_date@op" value="ge,le"/>
							<input type="hidden" name="doc_from_date@type" value="date">
							<input type="hidden" name="doc_from_date@cast" value="y">
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="doc_to_date" valid="past" id="doc_to_date" value="${paramValues['doc_to_date'][0]}"/>
							<input type="hidden" name="doc_to_date@op" value="ge,le"/>
							<input type="hidden" name="doc_to_date@type" value="date">
							<input type="hidden" name="doc_to_date@cast"  value="y"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="F,P" optexts="${'Finalized,Open'}"/>
						</div>
					</td>

				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" align="center" width="100%" id="resultTable" cellspacing="0" cellpadding="0">
			<tr onmouseover="hideToolBar('');">
				<th>Select</th>
				<th>Visit No</th>
				<th>Document Name</th>
				<th>Template</th>
				<th>Date</th>
				<th>User</th>
			</tr>

			<c:forEach var="patientdoc" items="${patientdoclist}" varStatus="st">
				<c:choose>
					<c:when test="${patientdoc.map.status == 'A'}">
						<c:set var="flagColor" value="empty"/>
					</c:when>
					<c:when test="${patientdoc.map.status == 'I'}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
					<c:when test="${empty patientdoc.map.patient_id}">
						<c:set var="flagColor" value="green"/>
					</c:when>
				</c:choose>

				<c:set var="enableEdit" value="${patientdoc.map.doc_status == 'P'}"/>
				<c:set var="printDoc" value="${patientdoc.map.doc_format != 'doc_link'}"/>
				<tr  class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${ifn:cleanJavaScript(param.mr_no)}', doc_id: '${patientdoc.map.doc_id}', template_id: '${patientdoc.map.template_id}',
							format: '${patientdoc.map.doc_format}', patient_id: '${patientdoc.map.patient_id}',
							printerId: '${printpreferences.map.printer_id}', access_rights: '${patientdoc.map.access_rights}',
							username: '${patientdoc.map.username}'},
							[${printDoc}, ${enableEdit}]);"
						onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
					<td><input type="checkbox" name="deleteDocument" id="deleteDocument" value="${patientdoc.map.doc_id},${patientdoc.map.doc_format}" ></td>
					<td>
						<div style="width: 15px; float: left"><img src="${cpath}/images/${flagColor}_flag.gif"/></div>
						${patientdoc.map.patient_id}
					</td>
					<td><insta:truncLabel value="${patientdoc.map.doc_name}" length="45"/></td>
					<td><font class="${patientdoc.map.doc_format}">
							<insta:truncLabel value="${patientdoc.map.template_name}" length="45"/>
						</font>
					</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy" value="${patientdoc.map.doc_date}"/></td>
					<td><insta:truncLabel value="${patientdoc.map.username}" length="20"/></td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults message="No Documents Found" hasResults="${results}"/>
	</div>
	<div class="screenActions" style="float: left; display: ${results?'block':'none'}">
	<button type="button"  name="deleteDocuments" accesskey="D" onclick="return deleteSelected(event, document.generalDocForm);" ${allowToDelPatDoc == 'N' ? 'disabled' : '' } >
	<b><u>D</u></b>elete</button>
	<button type="button"  name="finalizeDocuments" accesskey="Z" onclick="return finalizeSelected(event, document.generalDocForm);">
	Finali<b><u>Z</u></b>e</button>

	</div>
	<div class="legend " style=" float: right; display: ${results?'block':'none'}" >
		<div class="flag" ><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Inactive visit documents</div>
		<div class="flag "><img src="${cpath}/images/green_flag.gif"/></div>
		<div class="flagText">Patient documents</div>
	</div>
	<div class="clrboth"></div>

	<div id="actions" class="screenActions">
		<c:url var="imageUrl" value="PatientGeneralImageAction.do">
			<c:param name="_method" value="getPatientImages"/>
			<c:param name="mr_no" value="${param.mr_no}"/>
		</c:url>
		<c:url var="docUrl" value="GenericDocumentsAction.do">
			<c:param name="_method" value="addPatientDocument"/>
			<c:param name="mr_no" value="${param.mr_no}"/>
		</c:url>
		<a href='<c:out value="${docUrl}"/>'>Add Document</a>
		| <a href='<c:out value="${imageUrl}"/>'>View/Add Images</a>
	</div>
</c:if>



</form>
</body>
</html>
