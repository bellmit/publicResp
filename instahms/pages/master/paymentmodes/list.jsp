<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Payment Mode - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.PAYMENT_MODE_PATH %>"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "${pagePath}/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of this Payment Mode"
				}
		};

		function init() {

			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Payment Mode Master</h1>

<insta:feedback-panel/>

<form name="PaymentModeForm" method="GET">

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="PaymentModeForm" >
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Payment Mode:</div>
					<div class="sboFieldInput">
						<input type="text" name="payment_mode" value="${ifn:cleanHtmlAttribute(param.payment_mode)}" />
						<input type="hidden" name="payment_mode@op" value="ico"/>
					</div>
				</div>
			</div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="payment_mode" title="Payment Mode"/>
				<th>Status</th>
				<insta:sortablecolumn name="displayorder" title="Display Order"/>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {mode_id: '${record.mode_id}'},'');">
					<td>
						${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
					</td>
					<td>${record.payment_mode}</td>
					<td>${record.status}</td>
					<td>${record.displayorder}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<c:if test="${empty pagedList.dtoList}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>

	<c:url var="url" value="${pagePath}/add.htm">
		</c:url>

	<div class="screenActions" style="float: left">
		<a href="${url}">Add New Payment Mode</a>
	</div>
</form>
</body>
</html>
