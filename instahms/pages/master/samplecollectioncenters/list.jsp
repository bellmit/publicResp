<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="pagePath" value="<%=URLRoute.SAMPLE_COLLECTION_CENTER%>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Sample Collection Centers List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<script type="text/javascript">
		var max_centers_inc_default = ${max_centers_inc_default};
		var centerId = ${centerId};
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "${pagePath}/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of this Department"
				}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Sample Collection Centers Master</h1>

<insta:feedback-panel/>

<form name="CollectionCenterForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="CollectionCenterForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Collection Center Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="collection_center" value="${ifn:cleanHtmlAttribute(param.collection_center)}" />
						<input type="hidden" name="collection_center@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
				<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
					<div class="sboField">
						<div class="sboFieldLabel">Center:</div>
						<div class="sboFieldInput">
							<select class="dropdown" name="center_id" id="center_id">
								<option value="">-- Select --</option>
									<c:forEach items="${centers}" var="center">
										<option value="${center.center_id}"
											${param['center_id'] == center.center_id ? 'selected' : ''}>${center.center_name}</option>
									</c:forEach>
							</select>
							<input type="hidden" name="center_id@cast" value="y"/>
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
			<insta:sortablecolumn name="collection_center" title="Collection Center"/>
			<c:if test="${max_centers_inc_default > 1 && centerId == 0}" >
				<insta:sortablecolumn name="center_name" title="Center Name" />
			</c:if>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {collection_center_id: '${record.collection_center_id}'},[${record.collection_center_id != -1}],'');">

				<td>
					${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
				</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.collection_center}
				</td>
				<c:if test="${max_centers_inc_default > 1 && centerId == 0}" >
					<td>${record.center_name}</td>
				</c:if>
			</tr>
		</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="${pagePath}/add.htm">
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Add New Sample Collection Center</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>
</body>
</html>
