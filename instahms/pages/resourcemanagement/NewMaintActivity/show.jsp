<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Maint. Activities List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<script>
	var multiStoreAccess = ${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')};
	var gRoleId = '${ifn:cleanJavaScript(roleId)}';

	function init() {
		createToolbar(toolbar);
	}
	function checkstoreallocation() {
		if (gRoleId != 1 && gRoleId != 2) {
		var asset_dept = document.getElementById('asset_dept');
		if (multiStoreAccess) {
		// here it is a select box.
			if (asset_dept.options.length == 0) {
				alert("There is no assigned store, hence you dont have any access to this screen");
				document.getElementById("storecheck").style.display = 'none';
				return false;
			}
		} else {
		// here it is a hidden field
				if (asset_dept.value == '') {
					alert("There is no assigned store, hence you dont have any access to this screen");
					document.getElementById("storecheck").style.display = 'none';
					return false;
				}
			}
		}
	return true;
	}
	var toolbar = {
		Edit: {
			title: 'View/Edit',
			imageSrc: 'icons/Edit.png',
			href: 'resourcemanagement/NewMaintActivity.do?method=list'
		}
	}
	</script>
</head>
<body onload="checkstoreallocation(); init();showFilterActive(document.validateSearch);">
<c:set var="filterclosed" value="${not empty pagedList.dtoList}"></c:set>
<h1>Maintenance Activity</h1>
<insta:feedback-panel/>
<div id="storecheck" style="display: block;" >
<form name="validateSearch">
	<input type="hidden" name="method" value="show">
	<input type="hidden" name="_method" value="show">
	<input type="hidden" name="_searchMethod" value="show">
	<c:set var="today" value="${param.currdate}"/>
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
	<insta:search form="validateSearch" optionsId="optionalFilter" closed="${filterclosed}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Asset:</div>
				<div class="sboFieldInput">
					<input type="text" name="asset_name" value="${ifn:cleanHtmlAttribute(param.asset_name)}"/>
					<input type="hidden" name="asset_name@op" value="like"/>
				</div>
			</div>
			<div class=:sboField">
			<div class="sboFieldLabel">Store:</div>
			<c:choose>
		 		<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
					<insta:userstores username="${userid}" elename="asset_dept" val="${param.asset_dept}" id="asset_dept"  defaultVal="-- Select --"/>
					<input type="hidden" name="asset_dept@type"  value="integer" />
					<input type="hidden" name="asset_dept@cast"  value="y" />
				</c:when>
				<c:otherwise>
					<b><insta:getStoreName store_id="${dept_id}"/></b>
					<input type="hidden" name="asset_dept" id="asset_dept" value="${dept_id}" />
					<input type="hidden" name="asset_dept@type"  value="integer" />
					<input type="hidden" name="asset_dept@cast"  value="y" />
				</c:otherwise>
			</c:choose>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${filterclosed ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Scheduled Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="scheduled_date" id="scheduled_date0"
												value="${paramValues.scheduled_date[0]}" calButton="true"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="scheduled_date" id="scheduled_date1"
												value="${paramValues.scheduled_date[1]}" calButton="true"/>
								<input type="hidden" name="scheduled_date@op" value="ge,le"/>
							</div>
						</td>

						<td class="last">
							<div class="sfLabel">Completed Date:</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="maint_date" id="maint_date0"
												value="${paramValues.maint_date[0]}" calButton="true"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="maint_date" id="maint_date1"
												value="${paramValues.maint_date[1]}" calButton="true"/>
								<input type="hidden" name="maint_date@op" value="ge,le"/>
							</div>
						</td>

						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="overdue,pending,completed" optexts="Overdue,Pending,Completed" selValues="${paramValues.status}"/>
							</div>
						</td>

						<td class="last">
							&nbsp;
						</td>

						<td class="last">
							&nbsp;
						</td>
					</tr>
				</table>
			</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<table class="formTable" align="center" style="width: 100%">

		<tr>
			<td colspan="2" align="center">
				<input type="hidden" id="currdate" name="currdate" value=""/>

				<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable">
					<tr>
						<insta:sortablecolumn name="maint_activity_id" title="Activity ID"/>
						<insta:sortablecolumn name="asset_name" title="Asset"/>
						<insta:sortablecolumn name="scheduled_date" title="Scheduled Date"/>
						<insta:sortablecolumn name="maint_date" title="Completed Date"/>
						<insta:sortablecolumn name="dept_name" title="Store"/>
						<th style="text-align: right;padding-right:4em;">Cost</th>
						<insta:sortablecolumn name="maint_by" title="Maint By"/>
					</tr>
					<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<c:set var="ediEnabled"
					value="${record.map['maint_date'] eq null}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'recordsTable',
							{maint_activity_id: '${record.map['maint_activity_id']}', asset_name: '${record.map['asset_name']}',edit:'${ediEnabled}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>
						<c:choose>
						<c:when test="${record.map['overdue'] eq 'Yes'}">
							<c:choose>
							<c:when test="${record.map['maint_date'] eq null}">
							<img class="flag" src="${cpath}/images/red_flag.gif"/>
							</c:when>
							<c:otherwise>
							<img class="flag" src="${cpath}/images/green_flag.gif"/>
							</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<img class="flag" src="${cpath}/images/empty_flag.gif"/>
						</c:otherwise>
						</c:choose>
						${record.map['maint_activity_id']}
						</td>
						<td><c:out value="${record.map['asset_str']}"/> </td>
						<td><fmt:formatDate value="${record.map['scheduled_date']}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${record.map['maint_date']}" pattern="dd-MM-yyyy"/></td>
						<td><c:out value="${record.map['dept_name']}"/></td>
						<td style="text-align: right;padding-right:4em;"><c:out value="${ifn:afmt(record.map['cost'])}"/></td>
						<td style="padding-right:2em;"><c:out value="${record.map['maint_by']}"/></td>
					</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>

	<insta:noresults hasResults="${filterclosed}" />
	<c:url var="url" value="NewMaintActivity.do">
		<c:param name="method" value="list"/>
		<c:param name="edit" value="true"/>
	</c:url>
	<div class="screenActions">
		<a href="<c:out value='${url}'/>">Add New Maintenance Activity</a>
	</div>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Overdue</div>
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText">Pending</div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText">Completed</div>

</form>
</div>
</body>
</html>