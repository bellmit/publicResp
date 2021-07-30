<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="textValue" class="java.util.HashMap">
<c:set target="${textValue}" property="Y" value="Yes" />
<c:set target="${textValue}" property="N" value="No" />
</jsp:useBean>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Encounter Type Master - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardLookup.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="pagePath" value="<%=URLRoute.ENCOUNTER_TYPE_PATH %>"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "${pagePath}/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of this Encounter Type"
				}
		};

		function init() {

			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

<h1>Encounter Type Master</h1>

<insta:feedback-panel/>

<form name="EncounterTypeForm" method="GET">

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:find form="EncounterTypeForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Encounter Type</div>
					<div class="sboFieldInput">
						<input type="text" name="encounter_type_desc" value="${ifn:cleanHtmlAttribute(param.encounter_type_desc)}" />
						<input type="hidden" name="encounter_type_desc@op" value="ico"/>
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Status</div>
							<div class="sfField">
									<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
									<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td>
							<div class="sfLabel">OP Applicable:</div>
							<div class="sfField">
								<insta:selectoptions name="op_applicable" opvalues="Y,N" optexts="Yes,No" value="${param.op_applicable}" dummyvalue="--Select--"/>
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">IP Applicable:</div>
							<div class="sfField">
								<insta:selectoptions name="ip_applicable" opvalues="Y,N" optexts="Yes,No" value="${param.ip_applicable}" dummyvalue="--Select--"/>
							</div>
						</td>
						<td >
							<div class="sfLabel">Daycare Applicable:</div>
							<div class="sfField">
								<insta:selectoptions name="daycare_applicable" opvalues="Y,N" optexts="Yes,No" value="${param.daycare_applicable}" dummyvalue="--Select--"/>
							</div>
						</td>
						<td class="last">&nbsp;</td>
						<td class="last">&nbsp;</td>
					</tr>
				</table>
			</div>
	</insta:find>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<th>Encounter Type</th>
				<th>Status</th>
				<th>OP Applicable</th>
				<th>IP Applicable</th>
				<th>Daycare Applicable</th>
				<th>OP Default Enc.</th>
				<th>IP Default Enc.</th>
				<th>Daycare Default Enc.</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {encounter_type_id :'${record.encounter_type_id}'},'');">
					<td>
						${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
					</td>
					<td>${record.encounter_type_desc}</td>
					<td>${record.status}</td>
					<td>${textValue[record.op_applicable]}</td>
					<td>${textValue[record.ip_applicable]}</td>
					<td>${textValue[record.daycare_applicable]}</td>
					<td>${textValue[record.op_encounter_default]}</td>
					<td>${textValue[record.ip_encounter_default]}</td>
					<td>${textValue[record.daycare_encounter_default]}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<c:if test="${empty pagedList.dtoList}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>

</form>
</body>
</html>
