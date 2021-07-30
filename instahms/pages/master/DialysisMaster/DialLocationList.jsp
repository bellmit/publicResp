<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="hospitaladminmasters.locationmaster.list.locationlist"/></title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<insta:js-bundle prefix="dialysismodule.dialysislocation"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.dialysislocation.toolbar");
	</script>


	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc : "icons/Edit.png",
				href : "/master/locationMaster.do?_method=show",
				onclick : null,
				description: toolbarOptions["editvisit"]["description"]
				}
		};

		function init() {

			createToolbar(toolBar);

		}
	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>

</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="location">
 		<insta:ltext key="hospitaladminmasters.locationmaster.list.location"/>
	</c:set>

<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>

	<c:set var="centername">
 <insta:ltext key="hospitaladminmasters.locationmaster.list.centername"/>
</c:set>
<h1><insta:ltext key="hospitaladminmasters.locationmaster.list.locationmaster"/></h1>

<insta:feedback-panel/>

<form name="LocationForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="LocationForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="hospitaladminmasters.locationmaster.list.locationname"/></div>
					<div class="sboFieldInput">
						<input type="text" name="location_name" value="${ifn:cleanHtmlAttribute(param.location_name)}" />
						<input type="hidden" name="location_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel"><insta:ltext key="hospitaladminmasters.locationmaster.list.status"/></div>
					<div class="sboFieldInput">
						<insta:checkgroup name="lm.status" opvalues="A,I" optexts="${status}" selValues="${paramValues['lm.status']}"/>
							<input type="hidden" name="lm.status@op" value="in"/>
					</div>
				</div>
				<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
					<div class="sboField" style="height:69">
						<div class="sboFieldLabel"><insta:ltext key="hospitaladminmasters.locationmaster.list.center"/></div>
						<div class="sboFieldInput">
							<select class="dropdown" name="lm.center_id" id="center_id">
								<option value="">-- Select --</option>
								<c:forEach items="${centers}" var="center">
									<option value="${center.map.center_id}"
										${param['lm.center_id'] == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
								</c:forEach>
							</select>
							<input type="hidden" name="lm.center_id@cast" value="y"/>
						</div>
					</div>
				</c:if>
			</div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="location_name" title="${location}"/>
			<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
				<insta:sortablecolumn name="center_name" title="${centername}"/>
			</c:if>
			<th><insta:ltext key="hospitaladminmasters.locationmaster.list.remarks"/></th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {location_id: '${record.location_id}'},'');">

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${record.location_name}
				</td>
				<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
					<td>${record.center_name}</td>
				</c:if>
				<td>${fn:substring(record.remarks, 0, 20)}</td>
			</tr>
		</c:forEach>
		</table>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	</div>

		<c:url var="Url" value="locationMaster.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left;">
			<a href="${Url}"><insta:ltext key="hospitaladminmasters.locationmaster.list.addnewlocation"/></a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="hospitaladminmasters.locationmaster.list.inactive"/></div>
		</div>

</form>
</body>
</html>
