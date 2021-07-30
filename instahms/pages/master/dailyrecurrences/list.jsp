<%@page import="com.insta.hms.master.URLRoute"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Recurrence Master List-InstaHms</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagepath" value="<%= URLRoute.RECURRENCE_DAILY_MASTER_PATH %>" />
	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
			var toolbar = {
				Edit: {
					title: "View/Edit",
					imageSrc: "icons/Edit.png",
					href: '${pagepath}/show.htm?',
					description: "View or Edit Recurrency Daily details"
				}
			};

			function init() {
				createToolbar(toolbar);
			}

			function checkForSelected(elName, event) {
			if (!checkBoxesChecked(elName)) {
				return false;
			}
				document.showRecordsForm.submit();
				return true;
			}
			function doDelete(inputName) {
				checkForSelected(inputName);
			}
	</script>
</head>
<body onload="init()">
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Recurrence Daily Master</h1>
	<insta:feedback-panel/>
	<form name="RecurerncySearchForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}">
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}">
		<insta:search-lessoptions form="RecurerncySearchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Display Name: </div>
						<div class="sboFieldInput">
							<input type="text" name="display_name" value="${ifn:cleanHtmlAttribute(param.display_name)}">
						<input type="hidden" name="display_name@op" value="ico" />
						</div>
					</td>
					<td class="sboField" style="height:70px">
						<div class="sboFieldLabel">Status: </div>
						<div class="sboFieldInput">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
											opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
				</tr>
			  </table>
		</insta:search-lessoptions>

		</form>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<form name=showRecordsForm action="${cpath}/master/dailyrecurrences/delete.htm" method="POST">
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th><input type="checkbox" name="deleteAll" id="deleteAll" onclick="return checkOrUncheckAll('deleteRecord', this)"></th>
					<insta:sortablecolumn name="display_name" title="Display Name"/>
					<th>Timings</th>
					<th>No.Of Activites</th>
					<th>Medication Type</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{recurrence_daily_id: '${record.recurrence_daily_id}',display_name: '${record.display_name}'},'');" id="toolbarRow${st.index}">
							<td>
								<input type="checkbox" name="deleteRecord" id="deleteRecord" ${record.recurrence_daily_id== -1 ? 'disabled' : ''}
									value="${record.recurrence_daily_id}">
							</td>
							<td>
								<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
								<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>${record.display_name}
							</td>
							<td>${record.timings}</td>
							<td>${record.num_activities}</td>
							<td>
								<c:if test="${record.medication_type eq 'M'}">All Medicines</c:if>
								<c:if test="${record.medication_type eq 'IV'}">Only IV</c:if>
								<c:if test="${record.medication_type eq 'A'}">Only Additive</c:if>
							</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</div>

			<c:url var="url" value="${pagepath}/add.htm">
			</c:url>
			<table class="screenActions">
				<tr>
					<td>
						<button type="button" name="delete" accesskey="D" onclick="return doDelete('deleteRecord')"><b><u>D</u></b>elete</button>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td><a href="<c:out value='${url}' />">Add New Recurrence</a></td>

				</tr>
			</table>
			<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Inactive</div>
			</div>
		</form>
</body>
</html>
