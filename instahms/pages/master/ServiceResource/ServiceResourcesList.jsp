<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Service Resources List - Insta HMS</title>

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
				href: 'masters/serviceresources.do?_method=show',
				onclick: null,
				description: "View and/or Edit Service Resource details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.ServiceResourceSearchForm);
		}

	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedlist.dtoList}"></c:set>

	<h1>Service Resources Master</h1>

	<insta:feedback-panel/>

	<form name="ServiceResourceSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="ServiceResourceSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Service Resource Name</div>
					<div class="sboFieldInput">
						<input type="text" name="serv_resource_name" value="${ifn:cleanHtmlAttribute(param.serv_resource_name)}">
						<input type="hidden" name="serv_resource_name@op" value="ico" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Schedulable:</div>
							<div class="sfField">
								<insta:selectoptions name="schedule" value="${param.schedule}"
											opvalues="false,true"  optexts="No,Yes" dummyvalue="--Select--"/>
								<input type="hidden" name="schedule@type" value="boolean" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Overbooked:</div>
							<div class="sfField">
								<insta:selectoptions name="_overbook_limit" value="${param._overbook_limit}"
											opvalues="false,true"  optexts="No,Yes" dummyvalue="--Select--"/>
								<input type="hidden" name="_overbook_limit@type" value="boolean" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="srm.status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['srm.status']}"/>
								<input type="hidden" name="srm.status@op" value="in" />
							</div>
						</td>
						<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
							<td class="last">
								<div class="sfLabel">Center:</div>
								<div class="sfField">
									<select class="dropdown" name="srm.center_id" id="center_id">
										<option value="">-- Select --</option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}"
												${param['srm.center_id'] == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
										</c:forEach>
									</select>
									<input type="hidden" name="srm.center_id@cast" value="y"/>
								</div>
							</div>
						</c:if>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedlist.pageNumber}" numPages="${pagedlist.numPages}" totalRecords="${pagedlist.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="serv_resource_name" title="Service Resource Name"/>
					<th>Schedulable</th>
					<th>Overbooked</th>
					<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
						<insta:sortablecolumn name="center_name" title="Center Name"/>
					</c:if>
				</tr>

				<c:forEach var="record" items="${pagedlist.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{serv_res_id: '${record.serv_res_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedlist.pageNumber-1) * pagedlist.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> ${ifn:cleanHtml(record.serv_resource_name)}
						</td>
						<td>
							<c:if test="${record.schedule}"> Yes</c:if>
							<c:if test="${!record.schedule}"> No</c:if>
						</td>
						<td>
							<c:if test="${empty record.overbook_limit || record.overbook_limit > 0}"> Yes</c:if>
							<c:if test="${record.overbook_limit == 0}"> No</c:if>
						</td>
						<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
							<td>${record.center_name}</td>
						</c:if>

					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="serviceresources.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add New Service Resource</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>