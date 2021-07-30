<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Health Authority Preferences List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagePath" value="<%=URLRoute.HEALTH_AUTH_PREFERENCES_PATH %>"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Health Authority Preference details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Health Authority Preferences List</h1>

	<insta:feedback-panel/>

	<form name="HealthAuthorityPreferencesForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="HealthAuthorityPreferencesForm">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Health Authority:</div>
					<div class="sboFieldInput">
						<input type="text" name="health_authority" value="${ifn:cleanHtmlAttribute(param.health_authority)}">
						<input type="hidden" name="health_authority@op" value="ico" />
					</div>
				</div>
			</div>

		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="health_authority" title="Health Authority"/>
					<insta:sortablecolumn name="diagnosis_code_type" title="Diag Code Type"/>
					<th>Prescribed by Generics</th>
					<th>Consultation Code Types</th>
					<th>Drug Code Type</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{health_authority: '${record.health_authority}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							${record.health_authority}
						</td>
						<td>
							${record.diagnosis_code_type}
						</td>
						<td>
							${record.prescriptions_by_generics == 'N' ? 'No' : 'Yes'}
						</td>
						<td>
							<insta:truncLabel value="${record.consultation_code_types}" length="30"/>
						</td>
						<td>
							<insta:truncLabel value="${record.drug_code_type}" length="30"/>
						</td>

					</tr>
				</c:forEach>

			</table>

		</div>
	</form>

</body>
</html>