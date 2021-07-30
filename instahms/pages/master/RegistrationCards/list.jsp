<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title>Registration Cards List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="dashboardsearch.js"/>

	<style type="text/css">
		.InactiveColor{background-color: #F9966B}
	</style>

	<script type="text/javascript">

		var toolbar = {
			Edit: {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/RegistrationCards.do?_method=show&from=edit',
				onclick: null,
				description: "Edit Registration Card details"
				}
		};

		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.CardSearchForm);
		}
	</script>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

<body class="yui-skin-sam" onload="init()">

	<h1>Registration Cards Dashboard</h1>

	<c:out value="${message}" />

	<insta:feedback-panel/>


	<form method="GET" name="CardSearchForm">

	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="CardSearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Card Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="card_name" Id="cardName" value="${ifn:cleanHtmlAttribute(param.card_name)}"/>
						<input type="hidden" name="card_name@op" value="ico" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Rate Plan:</div>
							<div class="sfField">
								<input type="text" name="rate_plan_name" id="ratePlanName" value="${ifn:cleanHtmlAttribute(param.rate_plan_name)}"/>
								<input type="hidden" name="rate_plan_name@op" value="ico" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Card Status:</div>
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
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="card_name" title="Card Name"/>
					<th>Visit Type</th>
					<insta:sortablecolumn name="rate_plan_name" title="Rate Plan"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{card_id: '${record.card_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${ifn:cleanHtml(record.card_name)}
						</td>
						<c:if test="${record.visit_type eq 'I'}">
							<td>In Patient</td>
						</c:if>
						<c:if test="${record.visit_type eq 'O'}">
							<td>Out Patient</td>
						</c:if>
						<c:if test="${record.visit_type eq 'A'}">
							<td>Any Patient</td>
						</c:if>
						<td>${record.rate_plan_name}</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

	    <c:url value="RegistrationCards.do" var="UrlcreateRegCard">
			<c:param name="_method" value="add" />
			<c:param name="from" value="add" />
		</c:url>

	<div class="screenActions" style="float:left"><a href="${UrlcreateRegCard}">Add New Registration Card</a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>
</body>
</html>
