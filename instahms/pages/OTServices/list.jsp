<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="cPath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Pending Surgeries List - Insta HMS</title>
<insta:link type="css" file="hmsNew.css" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="dashboardColors.js" />
<insta:link type="js" file="OTServices/OTServicesList.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
<style type="text/css">
	.status_Scheduled{background-color: #E4C89C}
	.Completed{background-color: #DDDA8A}
</style>
<script>
	function onChangeMrno(){
		var theForm = document.searchFilter;
		var mrnoBox = theForm.mrno;
		// complete
		var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

		if (!valid) {
			alert("Invalid MR No. Format");
			theForm.mrno.value = ""
			theForm.mrno.focus();
			return false;
		}
	}
</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="serviceList" value="${list.dtoList}" />
<c:set var="hasResults" value="${not empty serviceList}"/>
<body onload="init();">
<h1>Pending Surgeries</h1>
<insta:feedback-panel />
<form method="GET"  name="searchFilter" action="${cPath}/otservices/operations.do" >

	<input type="hidden" name="_method" value="getScheduledOperations">
	<input type="hidden" name="_searchMethod" value="getScheduledOperations"/>
	<insta:search form="searchFilter" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
			<div class="sboField">
			<div class="sboFieldLabel">Operation Name</div>
				<div class="sboFieldInput">
				<div id="opAutocomplete">
					<input type="text" name="operation" id="operation" value="${ifn:cleanHtmlAttribute(param.operation)}"/>
					<div id="opConatainer" style="width: 169px;"></div>
				</div>
				<input type="hidden" name="operation@op" value="ilike">
			</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Operation Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="start_datetime" valid="past"	id="start_datetime0" value="${paramValues.start_datetime[0]}" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="start_datetime" valid="past"	id="start_datetime1" value="${paramValues.start_datetime[1]}" />
							<input type="hidden" name="start_datetime@op" value="ge,le">
							<input type="hidden" name="start_datetime@type" value="date">
						</div>
					</td>

					<td>
						<div class="sfLabel">Department</div>
						<div class="sfField">
							<insta:selectdb name="dept_id" table="department" dummyvalue="----Department----"
								valuecol="dept_id"  displaycol="dept_name"
								value="${param.dept_id}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Operation Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" optexts="Completed,Not Completed" opvalues="C,N" selValues="${paramValues.status}" />
						</div>
					</td>
					<td>
						<div class="sfLabel">Report Status</div>
						<div class="sfField">
							<insta:checkgroup name="signed_off" optexts="SignedOff Reports" opvalues="true" selValues="${paramValues.signed_off}" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Bill Status</div>
						<div class="sfField">
							<insta:checkgroup name="bill_status" optexts="UnPaid Bills" opvalues="A" selValues="${paramValues.bill_status}" />
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${list.pageNumber}" numPages="${list.numPages}"  totalRecords="${list.totalRecords}"/>
	<c:if test="${not empty _searchMethod}">
	<insta:noresults hasResults="${hasResults}"/>
	</c:if>
</form>

<form method="GET" action="operations.do?" name="mainForm">
<input type="hidden" name="_method" value="editOperations"/>
<input type="hidden" name="visitId">
<input type="hidden" name="reportId">
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
<c:set var="serviceList" value="${list.dtoList}" />
<div class="resultList">
<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">

 	 <tbody>
		<tr>


             <insta:sortablecolumn name="mr_no" title="MR NO."/>
             <insta:sortablecolumn name="patient_id" title="Visit ID"/>
             <th>Patient Name</th>
			<th style="padding-top: 0px;padding-bottom: 0px">
				<input type="checkbox" name="checkAllForSignOff" onclick="return checkOrUncheckAll('signedOff', this)"/>
			</th>
			<th>Report Name</th>
			<th>Operation Name</th>
		</tr>
		<c:forEach var="prescriptions" items="${serviceList}" varStatus="st">
			<c:set var="canEditOption" value="N" />
			<c:set var="signedOff" value="false"/>
			<c:if test="${not empty prescriptions.signed_off  }"><c:set var="signedOff" value="${prescriptions.signed_off}"/> </c:if>
			<c:if test="${prescriptions.status ne 'C' and (not signed_off) and prescriptions.bill_status ne 'A'}">
				<c:set var="canEditOption" value="Y" />
			</c:if>
			<c:set var="editEnabled" value="${canEditOption eq 'Y'}" />
			<c:set var="otReportEnabled" value="${canEditOption eq 'Y'}" />

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick=" showToolbar(${st.index}, event, 'resultTable',
						{ patient_id:'${prescriptions.patient_id}', visitId:'${prescriptions.patient_id}', patientId:'${prescriptions.patient_id}', prescription_id:'${prescriptions.prescribed_id}', reportId:'${prescriptions.report_id}'},
						[${editEnabled}, true]);"
							onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

				<td valign="top">${prescriptions.mr_no}</td>
				<td valign="top">${prescriptions.patient_id}</td>
				<td valign="top">${prescriptions.patient_name}</td>

				<td valign="top">
					<c:choose>
						<c:when	test="${not empty prescriptions.report_id and not prescriptions.signed_off}">
						<input type="checkbox" name="signedOff" value="${prescriptions.report_id}">
						</c:when>
						<c:when test="${prescriptions.signed_off}">
							 ok
						</c:when>
					</c:choose>
				</td>
				<td>${prescriptions.report_name}</td>

				<td valign="top">
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${prescriptions.status eq 'C'}">
								yellow
							</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>

					<c:if test="${prescriptions.bill_status eq 'A'}">
						<c:set var="flagColor">grey</c:set>
					</c:if>
					<img src='${cpath}/images/${flagColor}_flag.gif' />	${prescriptions.operation}<br/>
				</td>

				<c:url value="operations.do" var="otreporturl">
					<c:param name="_method" value="getOTReportScreen" />
					<c:param name="patientId" value="${prescriptions.patient_id}" />
				</c:url>

			</tr>
		</c:forEach>
	</tbody>
	</table>

	<table class="pageList">
			<tr ><td><input type="button" value="SignOff Reports" onclick="SignOff()">	</td></tr>
	</table>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">UnPaid Bills</div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText">Completed</div>
	</div>

</form>
<script type="text/javascript">
var scheduledOperationsJSON = ${scheduledOperationsJSON};
var cPath = '${cPath}';
var operations = ${operations};

</script>
</body>
</html>
