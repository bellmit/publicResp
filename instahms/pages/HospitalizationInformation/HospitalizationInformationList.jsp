<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=iso-8859-1"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="clinicaldata.hospitalizationinformation.list.clinicalhospitalizationinformationlist"/></title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
	<insta:js-bundle prefix="clinicaldata.hospitalizationinformation"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.clinicaldata.hospitalizationinformation.toolbar");
	</script>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'dialysis/HospitalizationInformation.do?_method=show',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
				}
		};

		var psAc = null;
		function init()
		{
			createToolbar(toolbar);
			psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
		}
	</script>

</head>

<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="patientname">
 <insta:ltext key="ui.label.patient.name"/>
</c:set>
<c:set var="lastupdated">
 <insta:ltext key="clinicaldata.hospitalizationinformation.list.lastupdated"/>
</c:set>

	<h1><insta:ltext key="clinicaldata.hospitalizationinformation.list.clinicalhospitalizationinformation"/></h1>

	<insta:feedback-panel/>

	<form name="HospitalizationInformationSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="HospitalizationInformationSearchForm">

			<div class="searchBasicOpts" >
				<div class="sboField" style="height:69px">
				<div class="sfLabel"><insta:ltext key="clinicaldata.hospitalizationinformation.list.mrno.patientname"/></div>
				<div class="sfField">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
			<div class="sboField">
				<div class="sfLabel" ><insta:ltext key="clinicaldata.hospitalizationinformation.list.lastupdated"/>:</div>
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="clinicaldata.hospitalizationinformation.list.from"/></div>
						<insta:datewidget name="mod_time" id="mod_time0" valid="past"	value="${paramValues.mod_time[0]}"/>
						<input type="hidden" name="mod_time@type" value="timestamp"/>
						<input type="hidden" name="mod_time@op" value="ge,le"/>
						<input type="hidden" name="mod_time@cast" value="y"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="clinicaldata.hospitalizationinformation.list.to"/></div>
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
					<insta:sortablecolumn name="patient_name" title="${patientname}"/>
					<insta:sortablecolumn name="last_updated" title="${lastupdated}"/>
					<th style="text-align: center"><insta:ltext key="clinicaldata.hospitalizationinformation.list.hospitalizationcount"/></th>
					<th>&nbsp;</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{mr_no: '${record.mr_no}',hospitalization_id: '${record.hospitalization_id}'},'');" id="toolbarRow${st.index}">
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
							${record.hospitalization_count}
						</td>
						<td>&nbsp;</td>

					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="HospitalizationInformation.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href='<c:out value="${url}"/>'><insta:ltext key="clinicaldata.hospitalizationinformation.list.addnewhospitalizationdetails"/></a></div>

	</form>

</body>
</html>