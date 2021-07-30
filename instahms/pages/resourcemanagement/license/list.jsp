<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Licenses List - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>
	<script>
		function init() {
			createToolbar(toolbar);
		}
		var toolbar = {
			Edit: {
				title: 'View/Edit',
				imageSrc: 'icons/Edit.png',
				href: '/resourcemanagement/license.do?method=show'
			}
		}
	</script>

</head>
<body onload="init(); showFilterActive(document.validateSearch);">
<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<form name="validateSearch">
<input type="hidden" name="method" value="list">
<input type="hidden" name="_method" value="list">
<input type="hidden" name="_searchMethod" value="list">
<h1>Licenses</h1>
<insta:feedback-panel/>

<insta:search form="validateSearch" optionsId="optionalFilter" closed="${filterclosed}">
	<div class="searchBasicOpts" >
		<div class="sboField">
			<div class="sboFieldLabel">Status:</div>
				<div class="sboFieldInput">
					<input type="checkbox" name="statusAll" onclick="enableStatus()" value="on"
						${status== 'All'?'checked':''}>(All)<br/>
					<input type="checkbox" name="statusActive" value="on"
						${status== 'A'?'checked':''}>Active<br/>
					<input type="checkbox" name="statusInActive" value="on"
						${status== 'I'?'checked':''}>InActive<br/>
				</div>
			</div>
		<div class="sboField">
			<div class="sboFieldLabel">License Type:</div>
				<select name="licenseTypeFilter" multiple="true" size="4">
					<c:forEach var="item" items="${requestScope.licenseTypes}">
						<option value="${item.LICENSE_TYPE}">${item.LICENSE_TYPE} </option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Renewal Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="renewalFrom" id="renewalFrom"  />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="renewalTo" id="renewalTo"  />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">End Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="expiryFrom" id="expiryFrom"  />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="expiryTo" id="expiryTo"  />
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<table align="center" class="formtable" width="100%" id="recordsTable">

	<tr>
		<td>
			<table class="dashboard"  width="100%">
				<tr>
					<th>License</th>
					<th>License Type</th>
					<th>Start Date</th>
					<th>End Date</th>
					<th>Renewal Date</th>
					<th>Value</th>
					<th>Status</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'recordsTable',
							{license_id: '${record.map['license_id']}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<c:choose>
						<c:when test="${record.map['license_status'] eq 'A'}">
							<c:set var="flagColor" value="green"/>
						</c:when>
						<c:when test="${record.map['license_status'] eq 'I'}">
							<c:set var="flagColor" value="red"/>
						</c:when>
					</c:choose>
					<td><img src="${cpath}/images/${flagColor}_flag.gif"/>${record.map['license_desc']}</td>
					<td>${record.map['license_type']}</td>
					<td><fmt:formatDate value="${record.map['license_start_date']}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${record.map['license_end_date']}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${record.map['license_renewal_date']}" pattern="dd-MM-yyyy"/></td>
					<td>${record.map['license_value']}</td>
					<td>
						<c:if test="${record.map['license_status'] eq 'I'}">Inactive</c:if>
						<c:if test="${record.map['license_status'] eq 'A'}">Active</c:if>
					</td>
				</tr>
				</c:forEach>
			</table>
		</td>
	</tr>
</table>
	<insta:noresults hasResults="${filterclosed}"/>
	<div class="screenActions">
		<c:url var="addnew" value="license.do">
			<c:param name="method" value="add"/>
		</c:url>
		<a href="${addnew}">Add New License</a>
	</div>

	<table align="right">
		<tr>
			<td><img class="flag" src="${cpath}/images/green_flag.gif"/></td>
			<td>Active&nbsp;</td>
			<td><img class="flag" src="${cpath}/images/red_flag.gif"/></td>
			<td>InActive</td>
		</tr>
	</table>
</form>
</body>
</html>
