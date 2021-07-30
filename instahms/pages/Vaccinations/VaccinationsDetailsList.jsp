<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="clinicaldata.vaccinations.list.clinicalvaccinationslist"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'clinical/Vaccinations.do?_method=show',
				onclick: null,
				description: "View and/or Edit Vaccinations details"
				}
		};
		var psAc = null;
		function init()
		{
			createToolbar(toolbar);
			psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
		}
	</script>
	<insta:js-bundle prefix="clinicaldata.vaccinations"/>
</head>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="patientName">
<insta:ltext key="ui.label.patient.name"/>
</c:set>
<c:set var="lastUpdated">
<insta:ltext key="clinicaldata.vaccinations.list.lastupdated"/>
</c:set>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1><insta:ltext key="clinicaldata.vaccinations.list.clinicalvaccinations"/></h1>

	<insta:feedback-panel/>

	<form name="VaccinationsSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="VaccinationsSearchForm">

			<div class="searchBasicOpts" >
				<div class="sboField" style="height:69px">
				<div class="sfLabel"><insta:ltext key="clinicaldata.vaccinations.list.mrno.patientname"/></div>
				<div class="sfField">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
			<div class="sboField">
				<div class="sfLabel" ><insta:ltext key="clinicaldata.vaccinations.list.lastupdated"/></div>
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="clinicaldata.vaccinations.list.from"/></div>
						<insta:datewidget name="mod_time" id="mod_time0" valid="past"	value="${paramValues.mod_time[0]}"/>
						<input type="hidden" name="mod_time@type" value="timestamp"/>
						<input type="hidden" name="mod_time@op" value="ge,le"/>
						<input type="hidden" name="mod_time@cast" value="y"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="clinicaldata.vaccinations.list.to"/></div>
						<insta:datewidget name="mod_time" id="mod_time1" valid="past"	value="${paramValues.mod_time[1]}"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<insta:sortablecolumn name="patient_name" title="${patientName}"/>
					<insta:sortablecolumn name="mod_time" title="${lastUpdated}"/>
					<th style="text-align: center"><insta:ltext key="clinicaldata.vaccinations.list.vaccinationrecords"/></th>
					<th>&nbsp;</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{mr_no: '${record.mr_no}',vaccination_id: '${record.vaccination_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							${record.mr_no}
						</td>
						<td>
							${record.patient_name}
						</td>
						<td>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${record.mod_time}"/>
							<fmt:formatDate pattern="HH:mm" value="${record.mod_time}"/>
						</td>
						<td style="text-align: center">
							${record.vaccination_count}
						</td>
						<td>&nbsp;</td>

					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="/clinical/Vaccinations.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href='<c:out value="${url}"/>'><insta:ltext key="clinicaldata.vaccinations.list.addnewvaccination"/></a></div>

	</form>

</body>
</html>