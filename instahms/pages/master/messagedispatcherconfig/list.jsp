<%@page import="com.insta.hms.master.URLRoute"%>
<%@page import="com.insta.hms.mdm.messagedispatcherconfig.MessageDispatcherConfigConstants"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Message Dispatcher Configuration List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>
	<c:set var="pagePath" value="<%=URLRoute.MESSAGE_DISPATCHER_CONFIG_SHOW %>"/>
	<c:set var="messageModes" value="<%=MessageDispatcherConfigConstants.MESSAGE_MODES %>"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}.htm?',
				onclick: null,
				description: "View and/or Edit Message Dispatcher Configuration"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.MessageDispatcherConfigForm);
		}
	</script>
</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Message Dispatcher Configuration</h1>

	<insta:feedback-panel/>

	<form name="CitySearchForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:find form="MessageDispatcherConfigForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Message Mode:</div>
					<div class="sboFieldInput">
						<input type="text" name=message_mode value="${ifn:cleanHtmlAttribute(param.message_mode)}">
						<input type="hidden" name="message_mode@op" value="ico" />
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
		</insta:find>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="message_mode" title="Message Mode"/>
					<insta:sortablecolumn name="display_name" title="Display Name"/>
					<insta:sortablecolumn name="protocol" title="Protocol"/>
					<insta:sortablecolumn name="host_name" title="Host"/>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{message_mode: '${record.message_mode}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.message_mode}
						</td>
						<td>${record.display_name}</td>
						<td>${record.protocol}</td>
						<td>${record.host_name}</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</c:forEach>
			</table>
				<insta:noresults hasResults="${hasResults}"/>
		</div>
		
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>
</body>
</html>
