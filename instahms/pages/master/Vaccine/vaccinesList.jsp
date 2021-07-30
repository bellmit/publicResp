<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Vaccine Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			EditVaccine: {
				title: "Edit Vaccine",
				imageSrc: "icons/Edit.png",
				href: '/master/vaccineMaster.do?_method=showVaccineDetails',
				description: "View and/or Edit Vaccine details"
				},
			EditDose: {
				title: "Edit Dose Details",
				imageSrc: "icons/Edit.png",
				href: "/master/vaccineMaster.do?_method=showDossageDetails",
				description: "View and/or Edit Vaccine Dose Details "
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

	<h1>Vaccine Master</h1>

	<insta:feedback-panel/>

	<form name="vaccineSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="vaccineSearchForm" >
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Vaccine Name</div>
					<div class="sboFieldInput">
						<input type="text" name="vaccine_name" value="${ifn:cleanHtmlAttribute(param.vaccine_name)}" />
						<input type="hidden" name="vaccine_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height: 70px">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="vaccine_name" title="Vaccine"/>
					<th>Dose</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{vaccine_id: '${record.vaccine_id}', single_dose: '${record.single_dose}', status: 'A'}, '');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>${record.vaccine_name}
						</td>
						<td>
							${record.single_dose eq 'Y' ? 'Single' : 'Multiple'}
						</td>

					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="vaccineMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add New Vaccine</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>