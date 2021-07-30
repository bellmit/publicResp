<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Store - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<c:set var="pagePath" value="<%=URLRoute.STORE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "${pagePath}/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents of this Store"
				}
		};

		function init() {

			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Store Master</h1>

<insta:feedback-panel/>

<form name="StoreForm" method="GET">

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="StoreForm" >
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Store Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="dept_name" value="${ifn:cleanHtmlAttribute(param.dept_name)}" />
						<input type="hidden" name="dept_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:68">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
				<c:if test="${genPrefs.max_centers_inc_default > 1 && centerId == 0}">
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
				<insta:sortablecolumn name="dept_name" title="Store Name"/>
				<th>Counter Name</th>
				<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
					<insta:sortablecolumn name="center_name" title="Center Name"/>
				</c:if>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {dept_id: '${record.dept_id}'},'');">

					<td>
						${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
					</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:choose>
							<c:when test="${record.status eq 'A' && record.allow_auto_po_generation eq 'Y'}">
								<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/green_flag.gif'></c:if>
							</c:when>
							<c:otherwise>
								<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							</c:otherwise>
						</c:choose>
						
						${record.dept_name}
					</td>
					<td>${record.counter_no}</td>
					<c:if test="${max_centers_inc_default > 1 && centerId == 0}">
							<td>${record.center_name}</td>
					</c:if>
				</tr>
			</c:forEach>
		</table>
	</div>

	<c:if test="${empty pagedList.dtoList}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>

	<c:url var="Url" value="${pagePath}/add.htm" />
	
	<div class="screenActions" style="float: left">
		<a href="${Url}">Add New Store</a>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Auto PO Generation</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>
</form>

<insta:CsvDataHandler divid="upload1" action="StoreMaster"/>

</body>
</html>
