<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="max_centers_inc_default" value="${genPrefs[0].max_centers_inc_default}" scope="request" />
<c:set var="pagePath" value="<%=URLRoute.TEST_EQUIPMENT_PATH %>"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Test Equipment Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/testequipments/show.htm?',
				onclick: null,
				description: "View and/or Edit Test Equipment details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			showFilterActive(document.TestEquipmentSearchForm);
		}

	</script>

</head>

<body onload="init()">
	
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Test Equipment Master</h1>

	<insta:feedback-panel/>

	<form name="TestEquipmentSearchForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>

			<insta:find form="TestEquipmentSearchForm" optionsId="optionalFilter" closed="${hasResults}">

			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Equipment Name</div>
					<div class="sboFieldInput">
						<input type="text" name="equipment_name" value="${ifn:cleanHtmlAttribute(param.equipment_name)}">
						<input type="hidden" name="equipment_name@op" value="ico" />
					</div>
				</div>
			</div>

			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Diagnostic Department</div>
								<div class="sfField">
								<select id="tm.ddept_id" name="tm.ddept_id" class="dropdown" >
								<option value="">-- Select --</option>
								<c:forEach items="${diagdepts}" var="diagdept">
									 <option value="${diagdept.get('dept_id')}" ${ifn:arrayFind(paramValues['tm.ddept_id'], diagdept.get('dept_id')) != -1 ? 'selected' : ''}>${diagdept.get('dept_name')}</option>
								</c:forEach>
								</select>
							</div>
						</td>
						<td>
							<div class="sfLabel">Schedulable:</div>
							<div class="sfField">
								<insta:selectoptions name="schedule" value="${param.schedule}"
											opvalues="false,true"  optexts="NO,Yes" dummyvalue="--Select--"/>
								<input type="hidden" name="schedule@type" value="boolean" />
							</div>
						</td>
						<td>
							<div class="sfLabel">Overbooked:</div>
							<div class="sfField">
								<insta:selectoptions name="_overbook_limit" value="${param._overbook_limit}"
											opvalues="false,true"  optexts="NO,Yes" dummyvalue="--Select--"/>
								<input type="hidden" name="_overbook_limit@type" value="boolean" />
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="tm.status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues['tm.status']}"/>
								<input type="hidden" name="tm.status@op" value="in" />
							</div>
						</td>
						<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
							<td class="last">
								<div class="sfLabel">Center:</div>
								<div class="sfField">
									<select class="dropdown" name="tm.center_id" id="center_id">
										<option value="">-- Select --</option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.get('center_id')}"
												${param['tm.center_id'] == center.get('center_id') ? 'selected' : ''}>${center.get('center_name')}</option>
										</c:forEach>
									</select>
									<input type="hidden" name="tm.center_id@cast" value="y"/>
								</div>
							</div>
						</c:if>
					</tr>
				</table>
			</div>
		</insta:find>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="equipment_name" title="Equipment Name"/>
					<th>hl7 Export Code</th>
					<th>Diag. Department</th>
					<th>Schedulable</th>
					<th>Overbooked</th>
					<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
						<insta:sortablecolumn name="center_name" title="Center Name"/>
					</c:if>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{eq_id: '${record.eq_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if> ${record.equipment_name}
						</td>
						<td>
							${record.hl7_export_code}
						</td>
						<td>
							${record.ddept_name}
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

		</div>
		<insta:noresults hasResults="${hasResults}"/>
		<div class="screenActions" style="float:left"><a href="<c:out value='${cpath}/master/testequipments/add.htm' />">Add New Equipment</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>