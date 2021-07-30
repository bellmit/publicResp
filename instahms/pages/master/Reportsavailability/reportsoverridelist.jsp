<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="i18nSupport" content="true"/>
<title>Reports Availability Overrides - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="reportsdelivery/reportsdeliverytiming.js"/>

	<insta:js-bundle prefix="scheduler.resourceavailability"/>

	<script>
		var toolbarOptions = getToolbarBundle("js.scheduler.resourceavailability.toolbar");
	</script>
	<script>
	var centerId= '${centerId}';
	var toolbar = {
		Edit: {
			title: "Edit Override",
			imageSrc: "icons/Edit.png",
			href: 'master/reportscenteroverrides.do?_method=getScreen',
			onclick: null,
			description: ""
		},
		Delete: {
			title: "Delete Override",
			imageSrc: "icons/Edit.png",
			href: 'master/reportscenteroverrides.do?_method=deleteReportsAvailability',
			onclick: null,
			description: ""
			}
		}; 
		
		function init()
		{
			createToolbar(toolbar);
		}
	</script>
 <insta:js-bundle prefix="scheduler.doctornonavailability"/>
<insta:js-bundle prefix="scheduler.resourceavailability"/>
<insta:js-bundle prefix="scheduler.doctorscheduler"/>
<insta:js-bundle prefix="scheduler.todaysappointment"/>
<insta:js-bundle prefix="scheduler.schedulerdashboard"/>
<insta:js-bundle prefix="scheduler.portalappointment"/>

</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<h1>Reports Override List</h1>
	<insta:feedback-panel/>

	<form name="ReportsSearchForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="center_id" id="center_id" value="${ifn:cleanHtmlAttribute(param.center_id)}">
	<fieldset class="fieldsetborder">
	    <legend class="fieldsetlabel">Reports Override List</legend>
		<table class="formtable">
				<tr>
					<td class="formlabel" style="width:70px;">Center Name:</td>
					<td class="forminfo" style="width : 300px">${bean.map.center_name}</td>
				</tr>
			</table>
		</fieldset>
		
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<fieldset class="fieldsetborder">
    	<legend class="fieldsetlabel">Report Delivery Overrides</legend>
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
					<tr>
						<th style="padding-top: 0px;padding-bottom: 0px">
						<input type="checkbox" name="_checkAllForDelete" onclick="return checkOrUncheckAll('_cancelResourceAvailability', this)"/>
					</th>
					<th>Report Overridden Date</th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{rep_deliv_override_id : '${record.rep_deliv_override_id}',center_id : '${record.center_id}'},'');" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="_cancelResourceAvailability" value="${record.rep_deliv_override_id}"/></td>
						<td>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${record.day}"/>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
			</c:forEach>
		</table>
		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
		</fieldset>
	</div>
	<c:url var="url" value="reportscenteroverrides.do">
			<c:param name="_method" value="getScreen"/>
			<c:param name="center_id" value="${param.center_id}"/>
	</c:url>
	 <c:url var="url1" value="reportscenteroverrides.do">
		<c:param name="_method" value="showDefaultTimings"/>
		<c:param name="center_id" value="${param.center_id}"/>
	</c:url> 

	<table class="screenActions">
		<tr>
			<c:if test="${not empty pagedList.dtoList}">
				<td colspan="2">
						  <insta:accessbutton buttonkey="patient.resourcescheduler.resourceavailabilitylist.del" name="ConfirmedAppointment" type="button" onclick="deleteAllSelectedRows();" />
						  </td>
			</c:if>
			<td>&nbsp;</td>
				<td>
					<a href="<c:out value='${url}' />">Add New Report Overrides</a>
				</td>
			
		 <c:if test="${not empty pagedList.dtoList}">
				<td>&nbsp;|&nbsp;</td>
				<td>
					<a href="${url1}">Default Timings</a>
				</td>
			</c:if> 
			<c:if test="${not empty screenName && not empty referer}">
				<td>&nbsp;|&nbsp;</td>
				<td>
					<a href="<c:out value='${referer}' />">Back To Center</a>
				</td>
			</c:if>
		</tr>
	</table>
</form>
</body>
</html>
